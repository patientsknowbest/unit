package com.pkb.unit;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;
import static com.pkb.unit.DOT.toDOTFormat;

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
        registry.sink().accept(ImmutableUnicastMessageWithPayload.<Command>builder().target("someDBClientCode").messageType(Command.class).payload(Command.START).build());
        Thread.sleep(5000);
        registry.sink().accept(ImmutableUnicastMessageWithPayload.<Command>builder().target("databaseConnection").messageType(Command.class).payload(Command.STOP).build());
        Thread.sleep(3000);
    }

    @Test
    public void testToGraph() {
        LocalRegistry registry = new LocalRegistry();
        FakeUnit a1 = new FakeUnit("someDBClientCode", registry);
        FakeUnit a2 = new FakeUnit("databaseConnection", registry);
        a1.addDependency("databaseConnection");
        assertThat(toDOTFormat(registry), sameContentAsApproved());
    }
}
