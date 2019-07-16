package com.pkb.unit;

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

public class AbstractUnitTest {
    protected Bus bus;

    private Disposable trackerSubscription;
    private List<SystemState> systemStateHistory;
    private TestObserver<SystemState> systemStateTestObserver;

    @Before
    public void setup() {
        bus = new LocalBus();
        systemStateHistory = new ArrayList<>();
        trackerSubscription = Tracker.track(bus).subscribe(systemStateHistory::add);
    }

    @After
    public void tearDown() {
        trackerSubscription.dispose();
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

    protected TestObserver<SystemState> testObserver(SystemState expected) {
        return track(bus)
                .filter(state -> state.equals(expected))
                .test();
    }

    protected void assertExpectedState(TestObserver<SystemState> testObserver, SystemState expected) {
        testObserver.awaitCount(1);
        testObserver.assertValue(expected);
    }
}
