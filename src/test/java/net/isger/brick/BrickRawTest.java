package net.isger.brick;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.isger.brick.raw.Depository;
import net.isger.brick.raw.Depot;

public class BrickRawTest extends TestCase {

    public BrickRawTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(BrickRawTest.class);
    }

    public void testRaw() {
        System.out.println(Depository.seek(Depot.LAB_FILE, "brick-core.json"));
        assertTrue(true);
    }
}
