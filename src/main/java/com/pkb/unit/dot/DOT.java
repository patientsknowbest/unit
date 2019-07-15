package com.pkb.unit.dot;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.stream.Stream;

import com.pkb.unit.Tracker;

/**
 * Class for transforming the current state of the registry into a DOT format graph
 * https://graphviz.gitlab.io/_pages/doc/info/lang.html
 */
public class DOT {

    public static final String STATE_UNKNOWN = "UNKNOWN";

    public static String toDOTFormat(Collection<Tracker.Unit> units) {
        return units.stream()
                .map(DOT::toDOTFormat)
                .collect(joining("\n", "digraph { \n", "\n}"));
    }

    private static String toDOTFormat(Tracker.Unit unit) {

        return Stream.concat(
                Stream.of(unit.getId() + " [label=\"id=" + unit.getId() + " desiredState=" + desiredStateString(unit) + " actualState=" + unit.getState() + "\"]"),
                unit.getDependencies().stream()
                        .map(dependency -> unit.getId() + " -> " + dependency)
        ).collect(joining("\n"));
    }

    private static String desiredStateString(Tracker.Unit unit) {
        return String.valueOf(unit.getDesired()).toLowerCase();
    }

}
