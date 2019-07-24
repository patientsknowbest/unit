package com.pkb.unit.dot;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import com.pkb.unit.tracker.SystemState;
import com.pkb.unit.tracker.Unit;

/**
 * Class for transforming the current state of the system into a DOT format graph
 * See <a href="https://graphviz.gitlab.io/_pages/doc/info/lang.html">the DOT language</a>
 */
public class DOT {
    private DOT() {}

    /**
     * Converts the given {@link SystemState} into a DOT format graph.
     * @see <a href="https://graphviz.gitlab.io/_pages/doc/info/lang.html">DOT language</a>
     *
     * @param systemState the current system state to convert
     * @return the DOT format graph reprensenting the given system state
     */
    public static String toDOTFormat(SystemState systemState) {
        return systemState.units().stream()
                .map(DOT::toDOTFormat)
                .collect(joining("\n", "digraph { \n", "\n}"));
    }

    /**
     * Converts the Unit and its dependencies into a DOT graph node.
     *
     * @param unit to convert to graph node
     * @return the Unit converted to a DOT graph node
     */
    private static String toDOTFormat(Unit unit) {
        return Stream.concat(
                Stream.of(unit.id() + " [label=\"id=" + unit.id() + " desiredState=" + desiredStateString(unit) + " actualState=" + unit.state() + "\"]"),
                unit.dependencies().stream()
                        .map(dependency -> unit.id() + " -> " + dependency)
        ).collect(joining("\n"));
    }

    private static String desiredStateString(Unit unit) {
        return String.valueOf(unit.desiredState()).toLowerCase();
    }

}
