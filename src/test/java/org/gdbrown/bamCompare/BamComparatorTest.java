package org.gdbrown.bamCompare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

/**
 * Unit tests for BamComparator.
 */
public class BamComparatorTest {

  protected Path testDataHisat = Paths.get("src/test/testData/test_data_HISAT2.bam");
  protected Path testDataStar  = Paths.get("src/test/testData/test_data_STAR.bam");

  /**
   * Test basic sanity: can we compare two records for equality?
   */
  @Test
  public void testSanity() {
    BamComparator bc = new BamComparator();
    SamReader rdr1 = SamReaderFactory.makeDefault().open(testDataHisat.toFile());
    SAMRecordIterator rdr1It = rdr1.iterator();
    SAMRecord rec1 = rdr1It.next();
    SamReader rdr2 = SamReaderFactory.makeDefault().open(testDataHisat.toFile());
    SAMRecordIterator rdr2It = rdr2.iterator();
    SAMRecord rec2 = rdr2It.next();
    try {
      rdr1.close();
      rdr2.close();
    } catch (IOException ioe) {
      fail("testSanity file closure should not throw an exception");
    }
    assertEquals(true, bc.samEqual(rec1, rec2));
  }

  @Test
  public void testUnequal() {
    BamComparator bc = new BamComparator();
    SamReader rdr1 = SamReaderFactory.makeDefault().open(testDataHisat.toFile());
    SAMRecordIterator rdr1It = rdr1.iterator();
    SAMRecord rec1 = rdr1It.next();
    SamReader rdr2 = SamReaderFactory.makeDefault().open(testDataHisat.toFile());
    SAMRecordIterator rdr2It = rdr2.iterator();
    rdr2It.next();
    SAMRecord rec2 = rdr2It.next();
    try {
      rdr1.close();
      rdr2.close();
    } catch (IOException ioe) {
      fail("testSanity file closure should not throw an exception");
    }
    assertEquals(false, bc.samEqual(rec1, rec2));
  }

  @Test
  public void testRemoveMatchesSanity() {
    BamComparator bc = new BamComparator();
    BamFile rdr1 = new BamFile(testDataHisat.toString());
    rdr1.open();
    List<SAMRecord> rec1 = rdr1.nextNameGroup();
    BamFile rdr2 = new BamFile(testDataHisat.toString());
    rdr2.open();
    List<SAMRecord> rec2 = rdr2.nextNameGroup();
    bc.removeMatches(rec1, rec2);
    assertEquals(0, rec1.size());
    assertEquals(0, rec2.size());
    try {
      rdr1.close();
      rdr2.close();
    } catch (IOException ioe) {
      fail("testRemoveMatchesSanity file closure should not throw an exception");
    }
  }

  @Test
  public void testRemoveMatchesMismatch() {
    BamComparator bc = new BamComparator();
    BamFile rdr1 = new BamFile(testDataHisat.toString());
    rdr1.open();
    List<SAMRecord> rec1 = rdr1.nextNameGroup();
    BamFile rdr2 = new BamFile(testDataHisat.toString());
    rdr2.open();
    rdr2.nextNameGroup();
    List<SAMRecord> rec2 = rdr2.nextNameGroup();
    bc.removeMatches(rec1, rec2);
    assertEquals(1, rec1.size());
    assertEquals(1, rec2.size());
    try {
      rdr1.close();
      rdr2.close();
    } catch (IOException ioe) {
      fail("testRemoveMatchesMismatch file closure should not throw an exception");
    }
  }
}
