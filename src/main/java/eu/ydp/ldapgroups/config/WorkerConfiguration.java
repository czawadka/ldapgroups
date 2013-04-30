package eu.ydp.ldapgroups.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.util.Duration;

import javax.validation.constraints.NotNull;

public class WorkerConfiguration {
    /**
     * Period of executing worker in seconds
     */
    @NotNull
    @JsonProperty
    private Duration period = Duration.seconds(600);

    public Duration getPeriod() {
        return period;
    }
}
