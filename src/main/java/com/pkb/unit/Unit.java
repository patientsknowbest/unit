package com.pkb.unit;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.vavr.collection.List;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.pkb.unit.Command.*;
import static com.pkb.unit.State.*;
import static com.pkb.unit.State.SHUTDOWN;

public abstract class Unit {

    private final String id;
    private final Disposable commandSubscription;
    private final Disposable dependenciesSubscription;
    private final Disposable reportStateSubscription;
    private State state;
    private final Registry owner;

    private final List<CommandHandler> commandHandlers = List.of(
            new StartHandler(),
            new StopHandler(),
            new ShutdownHandler()
    );


    String id() {
        return id;
    }

    State state() {
        return state;
    }

    Registry owner() {
        return owner;
    }

    private Map<String, State> mandatoryDependencies = new ConcurrentHashMap<>();

    public Unit(String id, Registry owner) {
        this.id = id;
        this.state = CREATED;
        this.owner = owner;
        commandSubscription = owner.events()
                .filter(e -> e instanceof UnicastMessageWithPayload)
                .map(e -> (UnicastMessageWithPayload)e)
                .filter(e -> e.messageType() == Command.class)
                .filter(e -> e.target() == id)
                .observeOn(Schedulers.computation())
                .subscribe(ee -> handle((Command)ee.payload()));
        dependenciesSubscription = owner.events()
                .filter(e -> e instanceof MessageWithPayload)
                .map(e -> (MessageWithPayload)e)
                .filter(e -> e.messageType() == Transition.class)
                .observeOn(Schedulers.computation())
                .subscribe(te -> handleDependencyTransition((Transition) te.payload()));
        reportStateSubscription = owner.events()
                .filter(e -> e instanceof UnicastMessage)
                .map(e -> (UnicastMessage)e)
                .filter(e -> e.messageType() == ReportStateRequest.class)
                .filter(e -> Objects.equals(id, e.target()))
                .observeOn(Schedulers.computation())
                .subscribe(ce -> handleReportState());

    }

    private void handleReportState() {
        setAndPublishState(state);
    }

    private void handleDependencyTransition(Transition transition) {
        if (!mandatoryDependencies.containsKey(transition.id())) {
            return;
        }
        mandatoryDependencies.put(transition.id(), transition.current());
        if (transition.previous() != STARTED && allDepsHaveStarted()) {
            sendCommand(id, START);
        }
        if (transition.previous() == STARTED && transition.current() != STARTED) {
            sendCommand(id, STOP);
        }
    }

    private boolean allDepsHaveStarted() {
        return mandatoryDependencies.values().stream().allMatch(s -> s == STARTED);
    }

    /**
     * @return true if this set did not already contain the specified element
     */
    public State addDependency(String dependency) {
        State ret = mandatoryDependencies.put(dependency, UNKNOWN);
        // ask for the current state
        try {
            owner.sink().accept(ImmutableUnicastMessage.<ReportStateRequest>builder().messageType(ReportStateRequest.class).target(dependency).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    /**
     * @return true if this set contained the specified element
     */
    public State removeDependency(String dependency) {
        return mandatoryDependencies.remove(dependency);
    }

    private synchronized void handle(Command command) {
        State previous = state;
        try {
            commandHandlers
                    .find(ch -> ch.handles(command))
                    .getOrElseThrow(() -> new IllegalStateException("No handler found for command " + command))
                    .handle(command);
        } catch (Exception e) {
            state = FAILED;
            try {
                owner.sink().accept(makeTE(previous, state, e.getMessage()));
            } catch (Exception e1) {
                throw new RuntimeException(e); // ?
            }
        }
    }

    private interface CommandHandler {
        boolean handles(Command c);

        void handle(Command c);
    }

    private class StartHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            // only start if current state is stopped
            return c == START && (state == State.STOPPED || state == CREATED || state == STARTING);
        }

        @Override
        public void handle(Command c) {
            setAndPublishState(STARTING);
            if (!allDepsHaveStarted()) {
                // this could be better
                mandatoryDependencies.keySet().stream().forEach(dep -> sendCommand(dep, START));
                return;
            }
            HandleOutcome outcome = handleStart();
            if (outcome == HandleOutcome.SUCCESS) {
                setAndPublishState(STARTED);
            } else {
                setAndPublishState(FAILED);
            }
        }
    }

    private class StopHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            return c == STOP && (state == State.STARTED || state == FAILED || state == STOPPING || state == STARTING);
        }

        @Override
        public void handle(Command c) {
            setAndPublishState(STOPPING);
            HandleOutcome outcome = handleStop();
            if (outcome == HandleOutcome.SUCCESS) {
                setAndPublishState(STOPPED);
            } else {
                setAndPublishState(FAILED);
            }
        }
    }


    private void setAndPublishState(State state) {
        setAndPublishState(state, "");
    }

    private void setAndPublishState(State state, String comment) {
        try {
            this.owner.sink().accept(makeTE(this.state, state));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.state = state;
    }

    private class ShutdownHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            return c == Command.SHUTDOWN && (state == State.STOPPED || state == FAILED);
        }

        @Override
        public void handle(Command c) {
            setAndPublishState(SHUTDOWN);
            commandSubscription.dispose();
            dependenciesSubscription.dispose();
            reportStateSubscription.dispose();
        }
    }

    public enum HandleOutcome {
        SUCCESS, FAILURE
    }

    abstract HandleOutcome handleStart();

    abstract HandleOutcome handleStop();

    // utility
    private MessageWithPayload<Transition>  makeTE(State previous, State current) {
        return makeTE(previous, current, "");
    }

    private MessageWithPayload<Transition> makeTE(State previous, State current, String comment) {
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

    private void sendCommand(String id, Command command) {
        try {
            owner.sink().accept(ImmutableUnicastMessageWithPayload.<Command>builder().messageType(Command.class).target(id).payload(command).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
