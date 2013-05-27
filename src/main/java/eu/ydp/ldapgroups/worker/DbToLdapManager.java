package eu.ydp.ldapgroups.worker;

import com.yammer.dropwizard.util.Duration;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ScheduledExecutorService;

@Named
public class DbToLdapManager extends WorkerManager {
    @Inject
    public DbToLdapManager(ScheduledExecutorService scheduler,
                           @Value("#{dw.worker.period}") Duration period,
                           DbToLdapWorker worker) {
        super(scheduler, period, worker);
    }

    @Override
    public DbToLdapWorker getWorker() {
        return (DbToLdapWorker)super.getWorker();
    }
}
