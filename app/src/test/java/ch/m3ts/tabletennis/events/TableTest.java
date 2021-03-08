package ch.m3ts.tabletennis.events;

import android.graphics.Point;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import ch.m3ts.tabletennis.Table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TableTest {
    private Table table;
    private Point[] corners;
    private Point net;
    @Before
    public void setUp() {
        corners = new Point[2];
        corners[0] = new Point(1,2);
        corners[1] = new Point(4,5);
        net = new Point(2,3);
        table = new Table(corners, net);
    }

    @After
    public void tearDown() {
        table = null;
        corners = null;
        net = null;
    }

    @Test
    public void getCornerDownLeft() {
        assertNotNull(table.getCornerDownLeft());
        assertEquals(table.getCornerDownLeft(), corners[0]);
    }

    @Test
    public void getCornerDownRight() {
        assertNotNull(table.getCornerDownRight());
        assertEquals(table.getCornerDownRight(), corners[1]);
    }

    @Test
    public void getCloseNetEnd() {
        assertNotNull(table.getCloseNetEnd());
        assertEquals(table.getCloseNetEnd(), net);
    }

    @Test
    public void getCorners() {
        assertEquals(2, table.getCorners().length);
        assertSame(corners, table.getCorners());
    }

    @Test
    public void testThrowsExceptionOnInvalidAmountOfCorners() {
        for (int i=0; i<100; i++) {
            if(i > 2) {
                try {
                    table = new Table(new Point[i], new Point(i,i));
                    fail();
                } catch (Table.NotTwoCornersException ex) {
                    // should throw an error message
                    assertTrue(ex.getMessage().contains(String.valueOf(i)));
                }
            }
        }
    }

    @Test
    public void makeTableFromProperties() {
        Properties properties = new Properties();
        properties.setProperty("c1_x","147");
        properties.setProperty("c1_y","488");
        properties.setProperty("c2_x","1192");
        properties.setProperty("c2_y","487");
        properties.setProperty("c3_x","940");
        properties.setProperty("c3_y","367");
        properties.setProperty("c4_x","363");
        properties.setProperty("c4_y","365");
        properties.setProperty("n1_x","986");
        properties.setProperty("n1_y","491");
        properties.setProperty("n2_x","686");
        properties.setProperty("n2_y","490");
        table = Table.makeTableFromProperties(properties);
        assertEquals(2, table.getCorners().length);
        assertEquals(147, table.getCornerDownLeft().x);
        assertEquals(488, table.getCornerDownLeft().y);
        assertEquals(1192, table.getCornerDownRight().x);
        assertEquals(487, table.getCornerDownRight().y);
        assertEquals(986, table.getCloseNetEnd().x);
        assertEquals(491, table.getCloseNetEnd().y);
    }
}