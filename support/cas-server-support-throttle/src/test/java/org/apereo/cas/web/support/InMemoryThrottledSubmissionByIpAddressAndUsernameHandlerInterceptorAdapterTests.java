package org.apereo.cas.web.support;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Unit test for {@link InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@EnableScheduling
@SpringBootTest(classes = BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.throttle.core.username-parameter=username",
        "cas.authn.throttle.failure.range-seconds=5"
    }
)
@Getter
@Tag("AuthenticationThrottling")
public class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests
    extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;
}
