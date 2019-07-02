package com.pkb.unit;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;
import static com.pkb.unit.dot.DOT.toDOTFormat;

public class TestCommon {
    static void assertRegistry(Bus bus) {
        assertThat(toDOTFormat(bus), sameContentAsApproved());
    }
}
