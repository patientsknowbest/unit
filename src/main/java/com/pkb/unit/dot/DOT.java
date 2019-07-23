package com.pkb.unit.dot;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import com.pkb.unit.tracker.SystemState;
import com.pkb.unit.tracker.Unit;

/**
 * Class for transforming the current state of the registry into a DOT format graph
 * https://graphviz.gitlab.io/_pages/doc/info/lang.html
 */
public class DOT {
    private DOT() {}

    public static String toDOTFormat(SystemState systemState) {
        return systemState.units().stream()
                .map(DOT::toDOTFormat)
                .collect(joining("\n", "digraph { \n", "\n}"));
    }

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
