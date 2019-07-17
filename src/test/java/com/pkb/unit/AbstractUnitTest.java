package com.pkb.unit;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.pkb.unit.tracker.Tracker.track;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.pkb.unit.dot.DOT;
import com.pkb.unit.tracker.SystemState;
import com.pkb.unit.tracker.Tracker;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.TestScheduler;

public class AbstractUnitTest {
    protected Bus bus;
    protected TestScheduler testScheduler;

    protected TestScheduler testComputationScheduler;
    protected TestScheduler testIOScheduler;

    private Disposable trackerSubscription;
    private List<SystemState> systemStateHistory;

    @Before
    public void setup() {
        bus = new LocalBus();
        systemStateHistory = new ArrayList<>();
        trackerSubscription = Tracker.track(bus).subscribe(systemStateHistory::add);
    }

    @After
    public void tearDown() {
        trackerSubscription.dispose();
        RxJavaPlugins.reset();
    }

    @Rule
    public TestWatcher logHistoryOnFailure = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            System.out.println("System state history:");
            System.out.print(systemStateHistory.stream()
                    .map(DOT::toDOTFormat)
                    .collect(joining("\n\n")));
        }
    };

    protected TestObserver<SystemState> testObserver() {
        return track(bus)
                .test();
    }

    protected void assertExpectedState(TestObserver<SystemState> testObserver, SystemState expected) {
        List<SystemState> values = testObserver.values();
        assertThat(values.get(values.size() - 1), sameBeanAs(expected));
    }

    protected void setupTestScheduler() {
        testScheduler = new TestScheduler();
        RxJavaPlugins.setComputationSchedulerHandler(i -> testScheduler);
        RxJavaPlugins.setIoSchedulerHandler(i -> testScheduler);
    }

    protected void setupComputationTestScheduler() {
        testComputationScheduler = new TestScheduler();
        RxJavaPlugins.setComputationSchedulerHandler(i -> testComputationScheduler);
    }

    protected void setupIOTestScheduler() {
        testIOScheduler = new TestScheduler();
        RxJavaPlugins.setIoSchedulerHandler(i -> testIOScheduler);
    }
}
