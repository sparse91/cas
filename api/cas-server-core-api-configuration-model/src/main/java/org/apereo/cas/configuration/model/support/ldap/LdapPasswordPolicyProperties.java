package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link LdapPasswordPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-ldap")
@Accessors(chain = true)
@JsonFilter("LdapPasswordPolicyProperties")
public class LdapPasswordPolicyProperties extends PasswordPolicyProperties {
    private static final long serialVersionUID = -1878237508646993100L;

    /**
     * An implementation of a policy class that knows how to handle LDAP responses.
     * The class must be an implementation of {@code org.ldaptive.auth.AuthenticationResponseHandler}.
     */
    private String customPolicyClass;

    /**
     * LDAP type.
     */
    private AbstractLdapProperties.LdapType type = AbstractLdapProperties.LdapType.GENERIC;

    /**
     * This is used to calculate an expiration period for the account password.
     * When defined, LDAP password policy handling will use the {@code pwdLastSet} attribute
     * which must be returned from the LDAP authentication attempt.
     * LDAP password policy handling will emit a warning for the {@code pwdLastSet}
     * value plus the expiration amount.
     */
    private int passwordExpirationNumberOfDays = 180;
}
