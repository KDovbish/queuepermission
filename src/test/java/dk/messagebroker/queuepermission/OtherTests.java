package dk.messagebroker.queuepermission;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class OtherTests {

    @Test
    void stringHashCode() {
        assertNotEquals("черныйкот".hashCode(), "котчерный".hashCode());
        assertNotEquals("172.17.64.10pullerpusher".hashCode(), "172.17.64.10pusherpuller".hashCode());
        assertEquals("172.17.64.10pusher".hashCode(), "172.17.64.10pusher".hashCode());
    }

    @Test
    void steamReduce() {

        String[] actionAsString = {"a", "b"};
        String name = "172.17.64.10";
        assertEquals( "172.17.64.10ab", name + Arrays.stream(actionAsString).sorted().reduce("", (s1, s2) -> (s1 + s2)) );

        actionAsString[0] = "b"; actionAsString[1] = "a";
        assertEquals( "172.17.64.10ab", name + Arrays.stream(actionAsString).sorted().reduce("", (s1, s2) -> (s1 + s2)) );

        actionAsString = new String[4];
        actionAsString[3] = "d"; actionAsString[2] = "c"; actionAsString[1] = "b"; actionAsString[0] = "a";
        assertEquals( "172.17.64.10abcd", name + Arrays.stream(actionAsString).sorted().reduce("", (s1, s2) -> (s1 + s2)) );
        assertEquals( "172.17.64.10_abcd", name + Arrays.stream(actionAsString).sorted().reduce("_", (s1, s2) -> (s1 + s2)) );

    }

}
