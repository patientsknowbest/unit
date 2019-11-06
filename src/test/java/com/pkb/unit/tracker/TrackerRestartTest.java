package com.pkb.unit.tracker;

import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;
import static junit.framework.TestCase.fail;

import com.pkb.unit.Bus;
import com.pkb.unit.LocalBus;
import org.junit.Test;

import com.pkb.unit.AbstractUnitTest;
import com.pkb.unit.FakeUnit;

import io.reactivex.observers.TestObserver;

import java.util.concurrent.TimeUnit;

public class TrackerRestartTest extends AbstractUnitTest {
    @Test
    public void restartTrackerEmitsWhenUnitIsRestarted() {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.enable();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        TestObserver<Boolean> testRestartedObserver = Tracker.unitRestarted(bus, "unit1").test();
        unit1.stop();
        unit1.completeStop();
        unit1.start();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        testRestartedObserver.assertResult(true);
    }

    @Test
    public void restartTrackerDoesntEmitWhenUnitFailsToRestart() {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.enable();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        TestObserver<Boolean> testRestartedObserver = Tracker.unitRestarted(bus, "unit1").test();
        unit1.stop();
        unit1.completeStop();
        unit1.start();
        unit1.failStart();
        testScheduler.triggerActions();

        // THEN
        testRestartedObserver.assertEmpty();
    }

    class TestUnit extends com.pkb.unit.Unit {

        private String UNIT_ID;
        private boolean delayStartup;

        public TestUnit(Bus bus, String unitId, boolean delayStartup) {
            super(unitId, bus, 1, TimeUnit.SECONDS);
            UNIT_ID = unitId;
            this.delayStartup = delayStartup;
        }

        @Override
        protected synchronized com.pkb.unit.Unit.HandleOutcome handleStart() {
            System.out.println(UNIT_ID + " handleStart()");
            if (delayStartup) {
                try {
                    System.out.println("before wait");
                    wait(2000L);
                    System.out.println("after wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return com.pkb.unit.Unit.HandleOutcome.SUCCESS;
        }

        @Override
        protected com.pkb.unit.Unit.HandleOutcome handleStop() {
            System.out.println(UNIT_ID + " handleStop()");
            return com.pkb.unit.Unit.HandleOutcome.SUCCESS;
        }

        public void failed() {
            super.failed();
        }

        public String getUnitId() { return UNIT_ID;}
    }

    @Test
    public synchronized void restartTrackerWithNestedUnitsWitDelay() {
        // GIVEN
        LocalBus bus = new LocalBus();
        TestUnit unit1 = new TestUnit(bus, "unit1", false);
        TestUnit unit2 = new TestUnit(bus, "unit2", true);
        TestUnit unit3 = new TestUnit(bus, "unit3", false);
        TestUnit unit4 = new TestUnit(bus, "unit4", false);
        unit1.addDependency(unit2.getUnitId());
        unit2.addDependency(unit3.getUnitId());
        unit3.addDependency(unit4.getUnitId());
        unit1.enable();
        TestObserver<Boolean> testRestartedObserver = Tracker.unitRestarted(bus, unit1.getUnitId()).test();

        // WHEN
        unit2.failed();

        // THEN
        testRestartedObserver.awaitCount(1);
        testRestartedObserver.assertResult(true);
        System.out.println("finished");

        try {
            System.out.println("before wait");
            wait(2000L);
            System.out.println("after wait");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fail("Look at the system output ... the 'finished' comes before `unit1 handleStart()");

    }
}
