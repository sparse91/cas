const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const jimp = require("jimp");
const fs = require('fs');
const qrCode = require('qrcode-reader');
const SockJS = require('sockjs-client');
const StompJS = require('@stomp/stompjs');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");

    await cas.assertTextContent(page, "#qrlogin .card-title span", "Login with QR Code");
    await cas.assertVisibility(page, '#qrlogin .card-text img');
    await cas.assertVisibility(page, '#qrchannel');

    let src = await page.$eval("#qrcode", element => element.getAttribute("src"));
    let data = src.replace(/^data:image\/jpeg;base64,/, "");
    await fs.writeFileSync(`${__dirname}/out.ignore`, data, 'base64');
    let buffer = fs.readFileSync(`${__dirname}/out.ignore`);

    await jimp.read(buffer, (err, image) => {
        if (err) {
            cas.logr(err);
        }
        let qrcode = new qrCode();
        qrcode.callback = (err, channelIdResult) => {
            if (err) {
                cas.logr(err);
            }
            let channelId = channelIdResult.result;
            cas.logg(`QR channel code is ${channelId}`);
            connectAndLogin(channelId, page);
        };
        qrcode.decode(image.bitmap);
    });
    await browser.close();
})();


async function connectAndLogin(channelId, page) {
    const client = new StompJS.Client({
        brokerURL: 'ws://localhost:8443/cas/qr-websocket',
        debug: str => {
            console.log(str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
    });

    if (typeof WebSocket !== 'function') {
        client.webSocketFactory = () => new SockJS('https://localhost:8443/cas/qr-websocket');
    }
    
    client.onConnect = frame => {
        const deviceId = `QRDevicePuppeteer`;
        cas.logg(`We have now connected ${frame.headers['message']}`);
        const parameters = `username=casuser&password=Mellon&token=true&QR_AUTHENTICATION_DEVICE_ID=${deviceId}`;
        executeRequest(`https://localhost:8443/cas/v1/tickets?${parameters}`, 201)
            .then(token => {
                console.log(`Received token ${token}`);
                let payload = JSON.stringify({'token': token});
                client.publish({
                    destination: "/qr/accept",
                    headers: {
                        'QR_AUTHENTICATION_CHANNEL_ID': channelId,
                        'QR_AUTHENTICATION_DEVICE_ID': deviceId
                    },
                    body: payload
                });
                cas.assertInnerText(page, '#content div h2', "Log In Successful");
                cas.assertCookie(page);
                client.deactivate();
                client.forceDisconnect();
                process.exit(0);
            })
    };

    client.onStompError = frame => {
        cas.logr(`Broker reported error: ${frame.headers['message']}`);
        cas.logr(`Additional details: ${frame.body}`);
    };

    client.activate();
}


async function executeRequest(url, statusCode) {
    return await cas.doRequest(url, "POST", {
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded'
    }, statusCode);
}
