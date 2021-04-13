package ch.m3ts.util;

import org.junit.Test;

import ch.m3ts.tabletennis.helper.Side;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CSVStringBuilderTest {
    @Test
    public void testMakeCSVString() {
        assertNotNull(CSVStringBuilder.builder());
        assertEquals("", CSVStringBuilder.builder().toString());
        assertEquals("some;random;values;", CSVStringBuilder.builder()
                .add("some")
                .add("random")
                .add("values")
                .toString());

        assertEquals(";", CSVStringBuilder.builder().add("").toString());

        assertEquals("TOP;1;3;312;LEFT;RIGHT;BOTTOM;", CSVStringBuilder.builder()
                .add(Side.TOP)
                .add(1)
                .add(3)
                .add(312)
                .add(Side.LEFT)
                .add(Side.RIGHT)
                .add(Side.BOTTOM)
                .toString());
    }
}