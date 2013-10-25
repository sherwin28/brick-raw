package net.isger.brick;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BrickRawTest extends TestCase {

    public BrickRawTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(BrickRawTest.class);
    }

    public void testRaw() {
        assertTrue(true);
    }

}
