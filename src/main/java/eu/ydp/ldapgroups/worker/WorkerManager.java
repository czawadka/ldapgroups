package eu.ydp.ldapgroups.worker;

import com.yammer.dropwizard.lifecycle.Managed;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WorkerManager implements Managed {
    ScheduledExecutorService scheduledExecutorService;
    int period;
    Runnable worker;

    ScheduledFuture<?> scheduledFuture;

    @Inject
    public WorkerManager(ScheduledExecutorService scheduledExecutorService, int period, Runnable worker) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.period = period;
        this.worker = worker;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public Runnable getWorker() {
        return worker;
    }

    @Override
    public void start() throws Exception {
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(worker, 0, period, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        scheduledFuture.cancel(true);
    }
}
