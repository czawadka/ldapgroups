package eu.ydp.ldapgroups.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class LdapConfiguration {
    @NotNull
    @JsonProperty
    private String server;

    @NotNull
    @JsonProperty
    private int port;

    @NotNull
    @JsonProperty
    private String baseDsn;

    @JsonProperty
    private String user = null;

    @JsonProperty
    private String password = null;

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public String getBaseDsn() {
        return baseDsn;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
