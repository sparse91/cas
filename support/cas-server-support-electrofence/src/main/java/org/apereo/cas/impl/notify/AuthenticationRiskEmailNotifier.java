package org.apereo.cas.impl.notify;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

/**
 * This is {@link AuthenticationRiskEmailNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AuthenticationRiskEmailNotifier extends BaseAuthenticationRiskNotifier {
    private final CommunicationsManager communicationsManager;

    public AuthenticationRiskEmailNotifier(final CasConfigurationProperties casProperties,
                                           final CommunicationsManager communicationsManager) {
        super(casProperties);
        this.communicationsManager = communicationsManager;
    }

    @Override
    public void publish() {
        val mail = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMail();

        val principal = authentication.getPrincipal();
        if (!principal.getAttributes().containsKey(mail.getAttributeName())) {
            LOGGER.debug("Could not send email to [{}]. Either no addresses could be found or email settings are not configured.", principal.getId());
            return;
        }
        val addresses = (List) principal.getAttributes().get(mail.getAttributeName());
        val body = EmailMessageBodyBuilder.builder().properties(mail).build().get();
        val emailRequest = EmailMessageRequest.builder().emailProperties(mail)
            .to(addresses).body(body).build();
        addresses.forEach(address -> this.communicationsManager.email(emailRequest));

    }
}
