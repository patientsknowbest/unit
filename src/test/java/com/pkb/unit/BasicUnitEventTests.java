package com.pkb.unit;

import org.junit.Test;

public class BasicUnitEventTests {

    class FakeUnit extends Unit {
        public FakeUnit(String id, Registry owner) {
            super(id, owner);
        }

        @Override
        Unit.HandleOutcome handleStart() {
            System.out.println(id() + " initializing for 1 second");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(id() + " started successfully");
            return HandleOutcome.SUCCESS;
        }

        @Override
        Unit.HandleOutcome handleStop() {
            System.out.println(id() + " stopping in 1 second");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(id() + " stopped successfully");
            return HandleOutcome.SUCCESS;
        }
    }

    @Test
    public void transition1() throws Exception {
        LocalRegistry registry = new LocalRegistry();
        FakeUnit a1 = new FakeUnit("someDBClientCode", registry);
        FakeUnit a2 = new FakeUnit("databaseConnection", registry);
        a1.addDependency("databaseConnection");
        registry.events().subscribe(System.out::println);
        registry.sink().accept(ImmutableCommandEvent.builder().targetId("someDBClientCode").value(Command.START).build());
        Thread.sleep(5000);
    }
}
