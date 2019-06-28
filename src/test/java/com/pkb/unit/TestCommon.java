package com.pkb.unit;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;
import static com.pkb.unit.DOT.toDOTFormat;

public class TestCommon {
    static void assertRegistry(Registry registry) {
        assertThat(toDOTFormat(registry), sameContentAsApproved());
    }
}
