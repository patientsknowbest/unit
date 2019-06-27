package com.pkb.unit;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class for transforming the current state of the registry into a DOT format graph
 * https://graphviz.gitlab.io/_pages/doc/info/lang.html
 */
public class DOT {
    public static String toDOTFormat(Registry registry) {
        return registry.units().stream()
                .map(DOT::toDOTFormat)
                .collect(Collectors.joining("\n", "digraph { \n", "\n}"));
    }

    private static String toDOTFormat(Unit unit) {
        return Stream.concat(
                Stream.of(unit.id() + " [label=\"" + unit.id() + " " + unit.state() + "\"]"), // Node attributes
                unit.dependencies().entrySet().stream()
                        .map(dependency -> unit.id() + " -> " + dependency.getKey() + " [label=\"" + dependency.getValue() + "\"]")
        ).collect(Collectors.joining("\n"));
    }
}
