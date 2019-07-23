package com.pkb.unit;

import static com.pkb.unit.Command.CLEAR_DESIRED_STATE;
import static com.pkb.unit.Command.DISABLE;
import static com.pkb.unit.Command.ENABLE;
import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.ConsumerWithoutParameter.consumer;
import static com.pkb.unit.DesiredState.DISABLED;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.DesiredState.UNSET;
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
import java.util.concurrent.TimeUnit;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Dependencies;
import com.pkb.unit.message.payload.NewUnit;
import com.pkb.unit.message.payload.ReportDependenciesRequest;
import com.pkb.unit.message.payload.ReportStateRequest;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.vavr.collection.List;

/**
 * A Unit is a base class for a restartable, stoppable service that can have depencencies and
 * that other Unitscan depend on. When a Unit is started then it tries the start its dependencies.
 * Once all of its dependencies has been started it starts itself.
 */
public abstract class Unit {
    /**
     * id uniquely identifies a unit in the whole system.
     */
    private final String id;

    /**
     * state represents the current state of the unit.
     */
    private State state = STOPPED;

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
            new DisableHandler(),
            new ClearDesiredStateHandler()
    );

    /**
     * A list of {@link Unit}s mapped to their {@link Unit#state}s that this Unit depends on.
     * This map is used to specify if all the dependencies have been started so that this Unit
     * is eligible for starting as well.
     */
    private Map<String, Optional<State>> mandatoryDependencies = new ConcurrentHashMap<>();

    /**
     *
     * @param id The string uniquely identifying this unit on the bus.
     * @param bus the bus which this unit will communicate on
     * @param retryPeriod the interval at which the desired state will be checked
     * @param retryTimeUnit the interval time unit at which the desired state will be checked
     */
    public Unit(String id, Bus bus, long retryPeriod, TimeUnit retryTimeUnit) {
        this.id = id;
        this.bus = bus;

        Filters.payloads(bus.events(), Command.class, id)
                .observeOn(Schedulers.computation())
                .subscribe(this::handle);

        Filters.payloads(bus.events(), Transition.class)
                .observeOn(Schedulers.computation())
                .subscribe(this::handleDependencyTransition);

        Filters.messages(bus.events(), ReportStateRequest.class, id)
                .observeOn(Schedulers.computation())
                .subscribe(consumer(this::handleReportState));

        Filters.messages(bus.events(), ReportDependenciesRequest.class, id)
                .observeOn(Schedulers.computation())
                .subscribe(consumer(this::handleReportDependencies));

        Observable.interval(retryPeriod, retryTimeUnit, Schedulers.computation())
                .subscribe(consumer(this::handleRetry));

        // Advertise our full current state
        unchecked(() -> bus.sink().accept(message(NewUnit.class).withPayload(newUnit(id))));
        handleReportDependencies();
        handleReportState();
    }

    /**
     * Adds a dependency to this unit.
     * This unit can only start if all its dependencies are started.
     */
    public void addDependency(String dependency) {
        mandatoryDependencies.put(dependency, Optional.empty());

        // Publish our new list of dependencies
        handleReportDependencies();

        // Prompt the dependency to report its state
        unchecked(() -> bus.sink().accept(message(ReportStateRequest.class).withTarget(dependency)));
    }

    /**
     * Removes a dependency from this unit.
     */
    public void removeDependency(String dependency) {
        mandatoryDependencies.remove(dependency);
        // Publish our new list of dependencies
        handleReportDependencies();
    }

    /**
     * Enable this unit
     */
    public void enable() {
        sendCommand(id, ENABLE);
    }

    /**
     * Disable this unit
     */
    public void disable() {
        sendCommand(id, DISABLE);
    }

    /**
     * Clear the desired state (so this unit is neither enabled or disabled)
     */
    public void clearDesiredState() {
        sendCommand(id, CLEAR_DESIRED_STATE);
    }

    /**
     * Starts the unit.
     * Note that unless the unit is ENABLED, it will not retry in case
     * it is stopped or it fails
     */
    public void start() {
        sendCommand(id, START);
    }

    /**
     * Stops the unit.
     * Note that unless the unit is DISABLED, it may be started again
     * at any time.
     */
    public void stop() {
        sendCommand(id, STOP);
    }

    /**
     * Access the ID of this unit
     * @return the unique identifier for the unit
     */
    protected String id() {
        return id;
    }

    /**
     * Access the current state of this unit
     * @return the current state of the unit.
     */
    protected State state() {
        return state;
    }

    /**
     * Checks if the unit is in its desired state and sends the appropriate {@link Command}
     * to itself to try to get into it. E.g. if the Unit's desired state is {@link DesiredState}
     * but the unit is in FAILED state then it sends START to itself.
     */
    private void handleRetry() {
        if (desiredState == ENABLED && state != STARTED) {
            sendCommand(id, START);
        } else if (desiredState == DISABLED && state != STOPPED && state != FAILED) {
            sendCommand(id, STOP);
        }
    }

    /**
     * Sends a Message to the bus containing the dependencies of this unit.
     */
    private void handleReportDependencies() {
        unchecked(() ->
            bus.sink().accept(message(Dependencies.class).withPayload(dependencies(id, mandatoryDependencies.keySet()))));
    }

    /**
     * Sends a Message to the bus containing its state and desired state.
     */
    private void handleReportState() {
        publishState();
    }

    /**
     * Called every time a unit sends a transition event to the bus. If the transitioning
     * unit is a dependency of this unit then it start or stops itself upon certain conditions.
     * @param transition the event that was sent to the bus
     */
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

    /**
     * @return true when all the units are in STARTED state which this unit depends on. False otherwise.
     */
    private boolean allDepsHaveStarted() {
        return mandatoryDependencies.values().stream().allMatch(s -> s.isPresent() && s.get() == STARTED);
    }

    /**
     * Searches for an appropriate {@link CommandHandler} to pass the command to. If no command
     * handler found that can process the given command then it publishes current state of
     * the Unit with a comment and returns without further operation.
     * @param command the command the method searches handler for and passes to
     */
    private synchronized void handle(Command command) {
        commandHandlers
                .find(ch -> ch.handles(command))
                .peek(commandHandler -> commandHandler.handle(command))
                .onEmpty(() -> publishState("No handler found for command " + command));

    }

    /**
     * Handles {@link Command}s based on the implementation of its implementors.
     */
    private interface CommandHandler {
        boolean handles(Command c);

        void handle(Command c);
    }

    /**
     * Handles {@link Command#ENABLE} via setting the desired state of the Unit and sending
     * START command to the Unit if it is not in STARTED state.
     */
    private class EnableHandler implements CommandHandler {
        @Override
        public boolean handles(Command c) {
            return c == ENABLE;
        }

        @Override
        public void handle(Command c) {
            DesiredState previousDesired = desiredState;
            desiredState = ENABLED;
            publishState(state, previousDesired);

            if (state != STARTED) {
                sendCommand(id, START);
            }
        }
    }

    /**
     * Handles {@link Command#DISABLE} via setting the desired state of the Unit and sending
     * STOP command to the Unit if it is not in STOPPED state.
     */
    private class DisableHandler implements CommandHandler {
        @Override
        public boolean handles(Command c) {
            return c == DISABLE;
        }

        @Override
        public void handle(Command c) {
            DesiredState previousDesired = desiredState;
            desiredState = DISABLED;
            publishState(state, previousDesired);

            if (state != STOPPED) {
                sendCommand(id, STOP);
            }
        }
    }

    /**
     * Handles {@link Command#START} after it checks if the Unit is eligible for transitioning to
     * STARTED state. The actual startup logic, that is implemnted by the clients, is run on an IO
     * thread.
     */
    private class StartHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            return c == START;
        }

        @Override
        public void handle(Command c) {
            if (desiredState == DISABLED) {
                publishState("Not starting, this unit is DISABLED");
                return;
            }

            if (state == STARTED) {
                publishState("Already STARTED. No operation executed");
                return;
            }

            if (state == STOPPING) {
                publishState("Stopping in progress, cannot be started");
                return;
            }

            setAndPublishState(STARTING);
            if (!allDepsHaveStarted()) {
                // this could be better
                mandatoryDependencies.keySet().forEach(dep -> sendCommand(dep, START));
                return;
            }

            Observable.fromCallable(Unit.this::handleStart)
                    .subscribeOn(Schedulers.io())
                    .subscribe(outcome -> {
                        if (outcome == HandleOutcome.SUCCESS) {
                            setAndPublishState(STARTED);
                        } else {
                            setAndPublishState(FAILED);
                        }
                    });
        }
    }

    /**
     * Handles {@link Command#STOP} after it checks if the Unit is eligible for transitioning to
     * STOPPED state. The actual stop logic, that is implemnted by the clients, is run on an IO
     * thread. This handler also tries to START the Unit if it is in ENABLED state.
     */
    private class StopHandler implements CommandHandler {

        @Override
        public boolean handles(Command c) {
            return c == STOP;
        }

        @Override
        public void handle(Command c) {
            if (state == STOPPED) {
                publishState("Already STOPPED. No operation executed.");
                return;
            }

            if (state == FAILED) {
                setAndPublishState(state, "FAILED unit cannot be stopped.");
                return;
            }

            if (state == STARTING) {
                publishState("Starting in progress, cannot be STOPPED");
                return;
            }

            setAndPublishState(STOPPING);
            Observable.fromCallable(Unit.this::handleStop)
                    .subscribeOn(Schedulers.io())
                    .subscribe(outcome -> {
                        if (outcome == HandleOutcome.SUCCESS) {
                            setAndPublishState(STOPPED);
                        } else {
                            setAndPublishState(FAILED);
                        }

                        // Take action to return to the desired state.
                        if (desiredState == ENABLED) {
                            sendCommand(id, START);
                        }
                    });
        }
    }

    /**
     * Handles {@link Command#CLEAR_DESIRED_STATE} command that can be consumed by the unit.
     */
    private class ClearDesiredStateHandler implements CommandHandler {
        @Override
        public boolean handles(Command c) {
            return c == CLEAR_DESIRED_STATE;
        }

        @Override
        public void handle(Command c) {
            DesiredState previousDesired = desiredState;
            desiredState = UNSET;
            publishState(state, previousDesired);
        }
    }

    /**
     * Changes the current state of the Unit to newState and sends a Message with the
     * previous and new {@link State} and {@link DesiredState} to the bus.
     *
     * @param newState the state to change Unit's current state to. Pass the current state if not changed.
     */
    private void setAndPublishState(State newState) {
        setAndPublishState(newState, "");
    }

    /**
     * Changes the current state of the Unit to newState and sends a Message with the
     * previous and new {@link State}, the current {@link DesiredState} and the given
     * comment to the {@link Bus}.
     *
     * @param newState the state to change Unit's current state to. Pass the current
     *                 state if not changed.
     * @param comment a text to include into the message. Pass the current state
     *                if not changed.
     */
    private void setAndPublishState(State newState, String comment) {
        State previous = this.state;
        this.state = newState;
        publishState(previous, desiredState, comment);
    }

    /**
     * Sends a Message with the current {@link State} and current {@link DesiredState}
     * to the {@link Bus}.
     */
    private void publishState() {
        publishState("");
    }

    /**
     * Sends a Message with the current {@link State} and current {@link DesiredState} and
     * the given comment to the {@link Bus}.
     */
    private void publishState(String comment) {
        publishState(state, desiredState, comment);
    }

    /**
     * Sends a Message with the previous and current {@link State}, and with the previous and
     * the current {@link DesiredState} and the given comment to the {@link Bus}.
     *
     * @param previous the {@link State} of the Unit before change. Pass the current state
     *                 if not changed.
     * @param previousDesired the {@link DesiredState} of the Unit before change. Pass the
     *                        current state if not changed.
     */
    private void publishState(State previous, DesiredState previousDesired) {
        publishState(previous, previousDesired, "");
    }

    /**
     * Sends a Message with the previous and current {@link State}, and with the previous and
     * the current {@link DesiredState} and the given comment to the {@link Bus}.
     *
     * @param previous the {@link State} of the Unit before change. Pass the current state
     *                 if not changed.
     * @param previousDesired the {@link DesiredState} of the Unit before change. Pass the
     *                        current state if not changed.
     * @param comment a text to include into the message. Pass the current state if not
     *                changed.
     */
    private void publishState(State previous, DesiredState previousDesired, String comment) {
        unchecked(() -> this.bus.sink().accept(makeTransitionEvent(previous, previousDesired, comment)));
    }

    /**
     * Indicates the result of Command handling.
     */
    public enum HandleOutcome {
        SUCCESS, FAILURE
    }

    /**
     * Implements the logic that should be done in order to start the service that this Unit
     * wraps.
     *
     * @return the result of the startup process that should be {@link HandleOutcome#SUCCESS}
     * if the startup succeeded or {@link HandleOutcome#FAILURE} otherwise.
     */
    protected abstract HandleOutcome handleStart();

    /**
     * Implements the logic that should be done in order to tear down the service that this Unit
     * wraps.
     *
     * @return the result of the stopping process that should be {@link HandleOutcome#SUCCESS}
     * if the tear down succeeded or {@link HandleOutcome#FAILURE} otherwise.
     *
     */
    protected abstract HandleOutcome handleStop();

    /**
     * Implementations may call this to inform the system that they have failed and that dependents should
     * stop.
     */
    protected void failed() {
        setAndPublishState(FAILED);
    }


    /**
     * Creates a Message with the previous and current {@link State}, and with the previous and
     * the current {@link DesiredState} and the given comment.
     *
     * @param previous the {@link State} of the Unit before change. Pass the current state
     *                 if not changed.
     * @param previousDesired the {@link DesiredState} of the Unit before change. Pass the
     *                        current state if not changed.
     * @param comment a text to include into the message. Pass the current state if not
     *                changed.
     * @return the compiled Message that can be sent to the Bus
     */
    private Message<Transition> makeTransitionEvent(State previous, DesiredState previousDesired, String comment) {
        return message(Transition.class)
                .withPayload(transition(id, state, previous, desiredState, previousDesired, Optional.of(comment)));
    }

    /**
     * Sends a Command the the given Unit through the {@link Bus}
     * @param id the identifier of the Unit to send the command to
     * @param command the command to send to the given unit.
     */
    private void sendCommand(String id, Command command) {
        unchecked(() -> bus.sink().accept(message(Command.class).withTarget(id).withPayload(command)));
    }

}
