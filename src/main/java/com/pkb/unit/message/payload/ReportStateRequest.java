package com.pkb.unit.message.payload;

import java.io.Serializable;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@Style(of = "reportStateRequest")
public interface ReportStateRequest extends Serializable {}
