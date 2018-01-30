package uk.ac.cam.cruk.bamCompare;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class BamComparatorTest extends TestCase {

  /**
   * Create the test case
   *
   * @param testName
   *          name of the test case
   */
  public BamComparatorTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(BamComparatorTest.class);
  }

  /**
   * Rigourous Test :-)
   */
  public void testSanity() {
    assertTrue(true);
  }
}
