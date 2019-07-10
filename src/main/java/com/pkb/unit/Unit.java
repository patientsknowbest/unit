package com.pkb.unit;

import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.State.CREATED;
import static com.pkb.unit.State.FAILED;
import static com.pkb.unit.State.SHUTDOWN;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.State.STOPPED;
import static com.pkb.unit.State.STOPPING;
import static com.pkb.unit.State.UNKNOWN;
import static com.pkb.unit.Unchecked.unchecked;
import static com.pkb.unit.message.ImmutableMessage.message;
import static com.pkb.unit.message.payload.ImmutableDependencies.dependencies;
import static com.pkb.unit.message.payload.ImmutableNewUnit.newUnit;
import static com.pkb.unit.message.payload.ImmutableTransition.transition;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Dependencies;
import com.pkb.unit.message.payload.NewUnit;
import com.pkb.unit.message.payload.ReportDependenciesRequest;
import com.pkb.unit.message.payload.ReportStateRequest;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.vavr.collection.List;

public abstract class Unit {

    private final String id;
    private final Disposable commandSubscription;
    private final Disposable dependenciesSubscription;
    private final Disposable reportStateSubscription;
    private final Disposable reportDependenciesSubscription;
    private State state;
    private final Bus owner;

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

    Bus owner() {
        return owner;
    }

    private Map<String, State> mandatoryDependencies = new ConcurrentHashMap<>();

    public Unit(String id, Bus bus) {
        this.id = id;
        this.state = CREATED;
        this.owner = bus;
        // Commands might result in IO bound operations, e.g.
        // opening a database connection or connecting to an HTTP API.
        // Use the io() scheduler for this reason
        commandSubscription = Filters.payloads(bus.events(), Command.class, id)
                .observeOn(Schedulers.io())
                .subscribe(this::handle);

        // Handling transitions & reporting state should _not_ do any IO work.
        // so we can execute these with the io() scheduler.
        dependenciesSubscription = Filters.payloads(bus.events(), Transition.class)
                .observeOn(Schedulers.computation())
                .subscribe(this::handleDependencyTransition);

        reportStateSubscription = Filters.messages(bus.events(), ReportStateRequest.class, id)
                .observeOn(Schedulers.computation())
                .subscribe(ignored -> handleReportState());

        reportDependenciesSubscription = Filters.messages(bus.events(), ReportDependenciesRequest.class, id)
                .observeOn(Schedulers.computation())
                .subscribe(ignored -> handleReportDependencies());

        unchecked(() -> bus.sink().accept(message(NewUnit.class).withPayload(newUnit(id))));
    }

    private void handleReportDependencies() {
        unchecked(() ->
            owner.sink().accept(message(Dependencies.class).withPayload(dependencies(id, mandatoryDependencies.keySet()))));
    }

    private void handleReportState() {
        setAndPublishState(state);
    }

    private void handleDependencyTransition(Transition transition) {
        if (!mandatoryDependencies.containsKey(transition.unitId())) {
            return;
        }
        mandatoryDependencies.put(transition.unitId(), transition.current());
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

        // Publish our new list of dependencies
        handleReportDependencies();

        // Prompt the dependency to report it's state
        unchecked(() -> owner.sink().accept(message(ReportStateRequest.class).withTarget(dependency)));

        return ret;
    }

    /**
     * @return true if this set contained the specified element
     */
    public void removeDependency(String dependency) {
        mandatoryDependencies.remove(dependency);
        // Publish our new list of dependencies
        handleReportDependencies();
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
            unchecked(() -> owner.sink().accept(makeTransitionEvent(previous, state, e.getMessage())));
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
            return c == START && (state == State.STOPPED || state == CREATED || state == STARTING || state == STARTED);
        }

        @Override
        public void handle(Command c) {
            if (state == STARTED) {
                setAndPublishState(state, "Already STARTED. No operation executed");
                return;
            }
            setAndPublishState(STARTING);
            if (!allDepsHaveStarted()) {
                // this could be better
                mandatoryDependencies.keySet().forEach(dep -> sendCommand(dep, START));
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
            return c == STOP && (state == State.STARTED || state == FAILED || state == STOPPING || state == STARTING || state == STOPPED);
        }

        @Override
        public void handle(Command c) {
            if (state == STOPPED) {
                setAndPublishState(state, "Already STOPPED. No operation executed.");
                return;
            }
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
        unchecked(() -> this.owner.sink().accept(makeTransitionEvent(this.state, state, comment)));
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
            reportDependenciesSubscription.dispose();
        }
    }

    public enum HandleOutcome {
        SUCCESS, FAILURE
    }

    abstract HandleOutcome handleStart();

    abstract HandleOutcome handleStop();

    // utility
    private Message<Transition> makeTransitionEvent(State previous, State current) {
        return makeTransitionEvent(previous, current, "");
    }

    private Message<Transition> makeTransitionEvent(State previous, State current, String comment) {
        return message(Transition.class)
                .withPayload(transition(current, previous, id, Optional.of(comment)));
    }

    private void sendCommand(String id, Command command) {
        unchecked(() -> owner.sink().accept(message(Command.class).withTarget(id).withPayload(command)));
    }
}
