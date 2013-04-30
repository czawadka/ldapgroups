package eu.ydp.ldapgroups.worker;

import com.yammer.dropwizard.lifecycle.Managed;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WorkerManager implements Managed {
    ScheduledExecutorService scheduler;
    Runnable worker;
    int period;

    ScheduledFuture<?> scheduledFuture;

    public WorkerManager(ScheduledExecutorService scheduler, int period, Runnable worker) {
        this.scheduler = scheduler;
        this.worker = worker;
        this.period = period;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public Runnable getWorker() {
        return worker;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public void start() throws Exception {
        scheduledFuture = scheduler.scheduleAtFixedRate(worker, 0, period, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        scheduledFuture.cancel(true);
    }
}
