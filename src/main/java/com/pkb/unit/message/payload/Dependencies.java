package com.pkb.unit.message.payload;

import java.io.Serializable;
import java.util.Map;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;
import org.immutables.value.Value.Style;

import com.pkb.unit.State;

@Immutable
@Style(allParameters = true, of = "dependencies")
public interface Dependencies extends Serializable {
    String id();
    Map<String, State> dependencies();
}
