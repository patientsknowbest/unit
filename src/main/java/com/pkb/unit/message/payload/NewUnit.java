package com.pkb.unit.message.payload;

import java.io.Serializable;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@Style(allParameters = true, of = "newUnit")
public interface NewUnit extends Serializable {
    String id();
}
