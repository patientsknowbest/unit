package com.pkb.unit;

import static com.pkb.unit.TestCommon.assertRegistry;

import org.junit.Test;

public class BasicUnitEventTests {

    // d02f15
    @Test
    public void initialStateIsCreated() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        // WHEN
        new FakeUnit("unit1", bus);
        // THEN
        assertRegistry(bus);
    }

}
