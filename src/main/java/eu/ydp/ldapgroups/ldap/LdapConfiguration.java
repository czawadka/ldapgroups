package eu.ydp.ldapgroups.ldap;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class LdapConfiguration {
    @NotNull
    @JsonProperty
    private String url = "ldap://localhost:389";

    /**
     * Eg. CN=userX,CN=Users,DC=intranet,DC=company
     */
    @JsonProperty
    private String user = null;

    @JsonProperty
    private String password = null;

    /**
     * Eg. DC=intranet,DC=company
     */
    @NotNull
    @JsonProperty
    private String baseDn = "";

    @NotNull
    @JsonProperty
    private String validationGroup;

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public String getValidationGroup() {
        return validationGroup;
    }
}
