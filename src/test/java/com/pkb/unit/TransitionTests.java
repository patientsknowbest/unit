package com.pkb.unit;

import static com.pkb.unit.TestCommon.assertRegistry;
import static com.pkb.unit.message.ImmutableMessage.message;
import static com.pkb.unit.message.payload.ImmutableTransition.transition;

import java.util.Optional;

import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.observers.TestObserver;

public class TransitionTests {

    /**
     * START command should trigger the
     * ffc147
     */
    @Test
    public void startCommandTransitionsSingleUnitToStarting() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        String unitID = "unit1";
        new FakeUnit(unitID, bus);
        TestObserver<Message> testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        bus.sink().accept(command(unitID, Command.START));

        // THEN
        testTransitionObserver
                .awaitCount(1)
                .assertValues(
                        transitionMessage(unitID, State.CREATED, State.STARTING, "")
                );
        assertRegistry(bus);
    }

    private TestObserver<Message> testTransitionObserver(Bus bus) {
        return bus.events()
                .filter(it -> it.messageType().equals(Transition.class))
                .test();
    }

    // 9070ca
    @Test
    public void startCommandTransitionsUnitToStarting() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);

        // WHEN
        TestObserver<Message> transitionObserver1 = testTransitionObserver(bus);
        unit1.addDependency(unit2ID);
        // THEN
        transitionObserver1.awaitCount(1)
                .assertValue(transitionMessage(unit2ID, State.CREATED, State.CREATED, ""))
                .dispose();

        // WHEN
        TestObserver<Message> transitionObserver2 = testTransitionObserver(bus);
        bus.sink().accept(command(unit1ID, Command.START));

        // THEN
        transitionObserver2
                .awaitCount(2)
                .assertValues(
                        transitionMessage(unit1ID, State.CREATED, State.STARTING, ""),
                        transitionMessage(unit2ID, State.CREATED, State.STARTING, "")
                ).dispose();
        assertRegistry(bus);
    }

    private Message<Command> command(String targetUnitId, Command start) {
        return message(Command.class)
                .withTarget(targetUnitId)
                .withPayload(start);
    }

    private Message<Transition> transitionMessage(String id, State previous, State current, String comment) {
        return message(Transition.class)
                .withPayload(transition(current, previous, id, Optional.of(comment)));
    }
}
