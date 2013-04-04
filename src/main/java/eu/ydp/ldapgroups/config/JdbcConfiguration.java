package eu.ydp.ldapgroups.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class JdbcConfiguration {
    @NotNull
    @JsonProperty
    private String driverClass = null;

    @NotNull
    @JsonProperty
    private String url = null;

    @NotNull
    @JsonProperty
    private String user = null;

    @JsonProperty
    private String password = "";

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
