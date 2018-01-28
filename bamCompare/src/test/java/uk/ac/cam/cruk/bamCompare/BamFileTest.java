package uk.ac.cam.cruk.bamCompare;

import java.nio.file.Path;
import java.nio.file.Paths;

//import junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

public class BamFileTest extends TestCase {

  protected Path testDataHisat = Paths.get("src/test/testData/test_data_HISAT2.bam");
  protected Path testDataStar = Paths.get("src/test/testData/test_data_STAR.bam");

  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public BamFileTest(String testName) {
    super( testName );
  }
  
  public BamFileTest() {
    super();
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite( BamFileTest.class );
  }

  public void testSanity() {
    // just test if we can open a BAM file and read a record from it
    SamReader rdr = SamReaderFactory.makeDefault().open(testDataHisat.toFile());
    SAMRecordIterator rdrIt = rdr.iterator();
    SAMRecord rec = rdrIt.next();
    assertEquals("HISEQ:165:C80FKANXX:1:1101:1001:1362",rec.getReadName());
    try {
      rdr.close();
    } catch (java.io.IOException ioe) {
      fail("Should not receive an IOException");
    }
  }
  
  public void testReadGroups() {
    BamFile bf = new BamFile(testDataHisat.toString());
    bf.open();
    assertEquals(1,bf.nextNameGroup().size());
    assertEquals(1,bf.nextNameGroup().size());
    assertEquals(1,bf.nextNameGroup().size());
    assertEquals(2,bf.nextNameGroup().size());
  }
}
