package eu.ydp.ldapgroups.worker;

import com.yammer.metrics.core.HealthCheck;

import javax.inject.Inject;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WorkerHealthCheck extends HealthCheck {
    WorkerManager workerManager;

    public WorkerHealthCheck(WorkerManager workerManager) {
        super("WorkerManager:"+workerManager.getWorker().toString());
        this.workerManager = workerManager;
    }

    @Override
    protected Result check() throws Exception {
        ScheduledFuture scheduledFuture = workerManager.getScheduledFuture();
        if (scheduledFuture==null)
            return Result.unhealthy("not started");
        if (scheduledFuture.isCancelled())
            return Result.unhealthy("cancelled");
        if (scheduledFuture.getDelay(TimeUnit.NANOSECONDS)<=0)
            return Result.healthy("is running");
        return Result.healthy("next run in "+scheduledFuture.getDelay(TimeUnit.SECONDS)+" seconds");
    }
}
