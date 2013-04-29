package eu.ydp.ldapgroups.worker;

import com.yammer.metrics.core.HealthCheck;

import javax.inject.Inject;
import java.util.concurrent.ScheduledFuture;

public class WorkerHealthCheck extends HealthCheck {
    WorkerManager workerManager;

    @Inject
    protected WorkerHealthCheck(WorkerManager workerManager) {
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
        if (!scheduledFuture.isDone())
            return Result.healthy("not done yet");
        return Result.healthy("done");
    }
}
