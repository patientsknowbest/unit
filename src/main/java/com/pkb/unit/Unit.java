package com.pkb.unit;

import static com.pkb.unit.Command.DISABLE;
import static com.pkb.unit.Command.ENABLE;
import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.DesiredState.DISABLED;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.CREATED;
import static com.pkb.unit.State.FAILED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.State.STOPPED;
import static com.pkb.unit.State.STOPPING;
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

import io.reactivex.schedulers.Schedulers;
import io.vavr.collection.List;

/**
 * A Unit is a base class for a restartable,
 */
public abstract class Unit {
    /**
     * id uniquely identifies a unit in the whole system.
     */
    private final String id;

    /**
     * state represents the current state of the unit
     */
    private State state = CREATED;

    /**
     * desiredState indicates the desired state of the unit
     * within the system. The unit will issue commands, retry from
     * failure, etc in order to match the state to the desired state.
     */
    private DesiredState desiredState = UNSET;

    /**
     * bus is the communication channel for units to send & receive messages.
     */
    private final Bus bus;

    private final List<CommandHandler> commandHandlers = List.of(
            new StartHandler(),
            new StopHandler(),
            new EnableHandler(),
            new DisableHandler()
    );

    private Map<String, Optional<State>> mandatoryDependencies = new ConcurrentHashMap<>();

    public Unit(String id, Bus bus) {
        this.id = id;
        this.bus = bus;

        // Commands might result in IO bound operations, e.g.
        // opening a database connection or connecting to an HTTP API.
        // Use the io() scheduler for this reason
        Filters.payloads(bus.events(), Command.class, id)
                .observeOn(Schedulers.io())
                .subscribe(this::handle);

        // Handling transitions & reporting state should _not_ do any IO work.
        // so we can execute these with the io() scheduler.
        Filters.payloads(bus.events(), Transition.class)
                .observeOn(Schedulers.computation())
                .subscribe(this::handleDependencyTransition);

        Filters.messages(bus.events(), ReportStateRequest.class, id)
                .observeOn(Schedulers.computation())
                .subscribe(ignored -> handleReportState());

        Filters.messages(bus.events(), ReportDependenciesRequest.class, id)
                .observeOn(Schedulers.computation())
                .subscribe(ignored -> handleReportDependencies());

        unchecked(() -> bus.sink().accept(message(NewUnit.class).withPayload(newUnit(id))));
    }

    private void handleReportDependencies() {
        unchecked(() ->
            bus.sink().accept(message(Dependencies.class).withPayload(dependencies(id, mandatoryDependencies.keySet()))));
    }

    private void handleReportState() {
        setAndPublishState(state);
    }

    private void handleDependencyTransition(Transition transition) {
        if (!mandatoryDependencies.containsKey(transition.unitId())) {
            return;
        }
        mandatoryDependencies.put(transition.unitId(), Optional.of(transition.current()));
        if (transition.previous() != STARTED && allDepsHaveStarted()) {
            sendCommand(id, START);
        }
        if (transition.previous() == STARTED && transition.current() != STARTED) {
            sendCommand(id, STOP);
        }
    }

    private boolean allDepsHaveStarted() {
        return mandatoryDependencies.values().stream().allMatch(s -> s.isPresent() && s.get() == STARTED);
    }

    /**
     * @return true if this set did not already contain the specified element
     */
    public Optional<State> addDependency(String dependency) {
        Optional<State> ret = mandatoryDependencies.put(dependency, Optional.empty());

        // Publish our new list of dependencies
        handleReportDependencies();

        // Prompt the dependency to report it's state
        unchecked(() -> bus.sink().accept(message(ReportStateRequest.class).withTarget(dependency)));

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
        DesiredState previousDesired = desiredState;
        try {
            commandHandlers
                    .find(ch -> ch.handles(command))
                    .getOrElseThrow(() -> new IllegalStateException("No handler found for command " + command))
                    .handle(command);
        } catch (Exception e) {
            state = FAILED;
            unchecked(() -> bus.sink().accept(makeTransitionEvent(previous, previousDesired, e.getMessage())));
        }
    }

    private interface CommandHandler {
        boolean handles(Command c);

        void handle(Command c);
    }

    private class EnableHandler implements CommandHandler {
        @Override
        public boolean handles(Command c) {
            return c == ENABLE;
        }

        @Override
        public void handle(Command c) {
            desiredState = ENABLED;

            if (state != STARTED) {
                sendCommand(id, START);
            }
        }
    }

    private class DisableHandler implements CommandHandler {
        @Override
        public boolean handles(Command c) {
            return c == DISABLE;
        }

        @Override
        public void handle(Command c) {
            desiredState = DISABLED;

            if (state != STOPPED) {
                sendCommand(id, STOP);
            }
        }
    }

    private class StartHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            // only start if current state is stopped
            return c == START && (state == State.STOPPED || state == CREATED || state == STARTING || state == STARTED);
        }

        @Override
        public void handle(Command c) {
            if (desiredState == DISABLED) {
                setAndPublishState(state, "Not starting, this unit is DISABLED");
                return;
            }

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

            // Take action to return to the desired state.
            if (desiredState == ENABLED) {
                sendCommand(id, START);
            }
        }
    }

    private void setAndPublishState(State state) {
        setAndPublishState(state, "");
    }

    private void setAndPublishState(State state, String comment) {
        this.state = state;
        unchecked(() -> this.bus.sink().accept(makeTransitionEvent(this.state, desiredState, comment)));
    }

    public enum HandleOutcome {
        SUCCESS, FAILURE
    }

    abstract HandleOutcome handleStart();

    abstract HandleOutcome handleStop();


    private Message<Transition> makeTransitionEvent(State previous, DesiredState previousDesired, String comment) {
        return message(Transition.class)
                .withPayload(transition(id, state, previous, desiredState, previousDesired, Optional.of(comment)));
    }

    private void sendCommand(String id, Command command) {
        unchecked(() -> bus.sink().accept(message(Command.class).withTarget(id).withPayload(command)));
    }

    String id() {
        return id;
    }
}
