package eu.ydp.ldapgroups.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class WorkerConfiguration {
    /**
     * Period of executing worker in seconds
     */
    @NotNull
    @JsonProperty
    private int period = 600;

    public int getPeriod() {
        return period;
    }
}
