package eu.ydp.ldapgroups.worker;

import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WorkerManager implements Managed {
    Logger log = LoggerFactory.getLogger(getClass());
    ScheduledExecutorService scheduler;
    Runnable worker;
    Duration period;

    ScheduledFuture<?> scheduledFuture;

    public WorkerManager(ScheduledExecutorService scheduler, Duration period, Runnable worker) {
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

    @Override
    public void start() throws Exception {
        log.info("Starting worker {} run every {} seconds", worker.toString(), period);
        scheduledFuture = scheduler.scheduleAtFixedRate(worker, 0, period.getQuantity(), period.getUnit());
    }

    @Override
    public void stop() throws Exception {
        log.info("Stop worker {}", worker.toString());
        scheduledFuture.cancel(true);
    }
}
