package com.pkb.unit;

import static com.pkb.unit.Command.ENABLE;
import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.State.STOPPED;
import static com.pkb.unit.message.ImmutableMessage.message;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.Test;

import com.pkb.unit.message.Message;

public class TransitionTests extends AbstractUnitTest {
    @Test
    public void enableCommandTransitionsSingleUnitToStarting() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        // WHEN
        new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", ENABLE));
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(ENABLED))
            .build());
    }

    @Test
    public void enableCommandTransitionsUnitAndDependencyToStarting() throws Exception {
        // GIVEN
        setupComputationTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", ENABLE));
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STARTING).withDesiredState(UNSET)).build());
    }

    @Test
    public void completeStartTransitionsUnitsToStarted() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", ENABLE));
        unit2.completeStart();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                        unit("unit1").withState(STARTED).withDesiredState(ENABLED).withDependencies("unit2"),
                        unit("unit2").withState(STARTED).withDesiredState(UNSET)).build());
    }

    @Test
    public void whenSingleUnitAlreadyStartedThenStartDoesNothing() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", START));
        unit1.completeStart();

        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET)).build());

        // WHEN
        bus.sink().accept(command("unit1", START));

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET)).build());
    }

    @Test
    public void whenDependencyAlreadyStartedThenStartDoesNothing() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", START));
        unit2.completeStart();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(STARTED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STARTED)).build());

        // WHEN
        bus.sink().accept(command("unit2", START));
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(STARTED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STARTED)).build());
    }

    // ceb5f8
    @Test
    public void whenSingleUnitAlreadyStoppedThenStopDoesNothing() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", START));
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET)).build());

        // WHEN
        bus.sink().accept(command("unit1", STOP));
        unit1.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET)).build());

        // WHEN
        bus.sink().accept(command("unit1", STOP));
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET)).build());
    }

    @Test
    public void whenDependencyAlreadyStoppedThenStopDoesNothing() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", START));
        unit2.completeStart();
        unit1.completeStart();

        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(STARTED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STARTED)).build());

        // WHEN
        bus.sink().accept(command("unit2", STOP));
        unit2.completeStop();
        unit1.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(STOPPED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STOPPED)).build());

        // WHEN
        bus.sink().accept(command("unit2", STOP));
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(STOPPED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STOPPED)).build());
    }

    private Message<Command> command(String target, Command start) {
        return message(Command.class)
                .withTarget(target)
                .withPayload(start);
    }
}
