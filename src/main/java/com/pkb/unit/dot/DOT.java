package com.pkb.unit.dot;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.pkb.unit.Bus;
import com.pkb.unit.State;

/**
 * Class for transforming the current state of the registry into a DOT format graph
 * https://graphviz.gitlab.io/_pages/doc/info/lang.html
 */
public class DOT {
    public static String toDOTFormat(Bus bus) {
//        List<String> units = bus.units();
//        TestObserver<Transition> statesTestObserver = bus.events()
//                .filter(msg -> msg.messageType().equals(Transition.class))
//                .map(msg -> (Transition)msg.payload())
//                .test();
//
//        TestObserver<Dependencies> dependenciesTestObserver = bus.events()
//                .filter(msg -> msg.messageType().equals(Dependencies.class))
//                .map(msg -> (Dependencies)msg.payload())
//                .test();
//
//        // Collect all the status' and dependencies from each unit
//        units.forEach(id -> {
//            try {
//                bus.sink().accept(ImmutableUnicastMessage.<ReportStateRequest>builder().messageType(ReportStateRequest.class).target(id).build());
//                bus.sink().accept(ImmutableUnicastMessage.<ReportDependenciesRequest>builder().messageType(ReportDependenciesRequest.class).target(id).build());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        Map<String, State> states = statesTestObserver
//                .awaitCount(units.size())
//                .values().stream()
//                .collect(Collectors.toMap(Transition::id, Transition::current));
//
//        Map<String, Map<String, State>> dependencies = dependenciesTestObserver
//                .awaitCount(units.size())
//                .values().stream()
//                .collect(Collectors.toMap(Dependencies::id, Dependencies::dependencies));
//
//        return units.stream()
//                .map(unitID -> toDOTFormat(unitID, states.get(unitID), dependencies.get(unitID)))
//                .collect(Collectors.joining("\n", "digraph { \n", "\n}"));
        return "";
    }

    private static String toDOTFormat(String unitId, State state, Map<String, State> dependencies) {
        return Stream.concat(
                Stream.of(unitId + " [label=\"" + unitId + " " + state + "\"]"), // Node attributes
                dependencies.entrySet().stream()
                        .map(dependency -> unitId + " -> " + dependency.getKey() + " [label=\"" + dependency.getValue() + "\"]")
        ).collect(Collectors.joining("\n"));
    }
}
