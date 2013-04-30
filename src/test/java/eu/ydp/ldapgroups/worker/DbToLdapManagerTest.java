package eu.ydp.ldapgroups.worker;

import com.yammer.dropwizard.util.Duration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DbToLdapManagerTest {
    DbToLdapManager manager;
    ScheduledExecutorService scheduler;
    Duration period = Duration.seconds(10);
    DbToLdapWorker worker;

    @Before
    public void setUp() throws Exception {
        scheduler = createSchedulederMock();
        worker = createWorkerMock();
        manager = new DbToLdapManager(scheduler, period, worker);
    }

    private DbToLdapWorker createWorkerMock() {
        DbToLdapWorker mock = Mockito.mock(DbToLdapWorker.class);
        return mock;
    }

    private ScheduledExecutorService createSchedulederMock() {
        ScheduledExecutorService mock = Mockito.mock(ScheduledExecutorService.class);
        return mock;
    }

    @Test
    public void shouldStartInitilizeExecutor() throws Exception {
        ScheduledFuture scheduledFuture = Mockito.mock(ScheduledFuture.class);
        Mockito.when(scheduler.scheduleAtFixedRate(worker, 0, period.getQuantity(), period.getUnit()))
                .thenReturn(scheduledFuture);

        manager.start();
        
        Mockito.verify(scheduler).scheduleAtFixedRate(worker, 0, period.getQuantity(), period.getUnit());
        MatcherAssert.assertThat(manager.getScheduledFuture(), Matchers.equalTo(scheduledFuture));
    }

    @Test
    public void shouldStopCancelFuture() throws Exception {
        ScheduledFuture scheduledFuture = Mockito.mock(ScheduledFuture.class);

        Mockito.when(scheduler.scheduleAtFixedRate(worker, 0, period.getQuantity(), period.getUnit()))
                .thenReturn(scheduledFuture);

        manager.start();
        manager.stop();

        Mockito.verify(scheduledFuture).cancel(true);
    }
}
