package com.pkb.unit;

import static com.pkb.unit.TestCommon.assertRegistry;

import org.junit.Test;

import io.reactivex.observers.TestObserver;

public class TransitionTests {

    /**
     * START command should trigger the
     * ffc147
     */
    @Test
    public void startCommandTransitionsSingleUnitToStarting() throws Exception {
        // GIVEN
        Registry registry = new LocalRegistry();
        String unitID = "unit1";
        new FakeUnit(unitID, registry);
        TestObserver<Message> testTransitionObserver = testTransitionObserver(registry);

        // WHEN
        registry.sink().accept(command(unitID, Command.START));

        // THEN
        testTransitionObserver
                .awaitCount(1)
                .assertValues(
                        transitionMessage(unitID, State.CREATED, State.STARTING, "")
                );
        assertRegistry(registry);
    }

    private TestObserver<Message> testTransitionObserver(Registry registry) {
        return registry.events()
                .filter(it -> it.messageType().equals(Transition.class))
                .test();
    }

    // 9070ca
    @Test
    public void startCommandTransitionsUnitToStarting() throws Exception {
        // GIVEN
        Registry registry = new LocalRegistry();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, registry);
        FakeUnit unit2 = new FakeUnit(unit2ID, registry);

        // WHEN
        TestObserver<Message> transitionObserver1 = testTransitionObserver(registry);
        unit1.addDependency(unit2ID);
        // THEN
        transitionObserver1.awaitCount(1)
                .assertValue(transitionMessage(unit2ID, State.CREATED, State.CREATED, ""))
                .dispose();

        // WHEN
        TestObserver<Message> transitionObserver2 = testTransitionObserver(registry);
        registry.sink().accept(command(unit1ID, Command.START));

        // THEN
        transitionObserver2
                .awaitCount(2)
                .assertValues(
                        transitionMessage(unit1ID, State.CREATED, State.STARTING, ""),
                        transitionMessage(unit2ID, State.CREATED, State.STARTING, "")
                ).dispose();
        assertRegistry(registry);
    }

    private UnicastMessageWithPayload<Command> command(String targetUnitId, Command start) {
        return ImmutableUnicastMessageWithPayload.<Command>builder().target(targetUnitId).messageType(Command.class).payload(start).build();
    }

    private MessageWithPayload<Transition> transitionMessage(String id, State previous, State current, String comment) {
        return ImmutableMessageWithPayload
                .<Transition>builder().payload(ImmutableTransition.builder()
                        .previous(previous)
                        .current(current)
                        .comment(comment)
                        .id(id)
                        .build())
                .messageType(Transition.class)
                .build();
    }
}
