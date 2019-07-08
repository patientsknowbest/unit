package com.pkb.unit.message.payload;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@Style(allParameters = true, of = "dependencies")
public interface Dependencies extends Serializable {
    String unitId();
    List<String> dependencies();
}
