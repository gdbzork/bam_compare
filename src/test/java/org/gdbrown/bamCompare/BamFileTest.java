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

public class BamFileTest {

  protected Path testDataHisat = Paths.get("src/test/testData/test_data_HISAT2.bam");
  protected Path testDataStar  = Paths.get("src/test/testData/test_data_STAR.bam");

  @Test
  public void testSanity() {
    // just test if we can open a BAM file and read a record from it
    SamReader rdr = SamReaderFactory.makeDefault().open(testDataHisat.toFile());
    SAMRecordIterator rdrIt = rdr.iterator();
    SAMRecord rec = rdrIt.next();
    assertEquals("HISEQ:165:C80FKANXX:1:1101:1001:1362", rec.getReadName());
    try {
      rdr.close();
    } catch (java.io.IOException ioe) {
      fail("Should not receive an IOException");
    }
  }

  @Test
  public void testReadGroups() {
    BamFile bf = new BamFile(testDataHisat.toString());
    bf.open();
    assertEquals(1, bf.nextNameGroup().size());
    assertEquals(1, bf.nextNameGroup().size());
    assertEquals(1, bf.nextNameGroup().size());
    List<SAMRecord> nameGroup = bf.nextNameGroup();
    assertEquals(2, nameGroup.size());
    assertEquals("HISEQ:165:C80FKANXX:1:1101:1001:60014", nameGroup.get(0).getReadName());
    for (int i = 0; i < 6; i++) {
      bf.nextNameGroup();
    }
    nameGroup = bf.nextNameGroup();
    assertEquals(30, nameGroup.size());
    assertEquals("HISEQ:165:C80FKANXX:1:1101:1003:60664", nameGroup.get(0).getReadName());
  }

  @Test
  public void testCountAll() {
    BamFile bf = new BamFile(testDataHisat.toString());
    int num = 0;
    try {
      num = bf.count(false); // count all reads
    } catch (IOException ioe) {
      fail("Count (all) should not throw an exception");
    }
    assertEquals(885, num);
  }

  @Test
  public void testCountMapped() {
    BamFile bf = new BamFile(testDataHisat.toString());
    int num = 0;
    try {
      num = bf.count(true); // count all reads
    } catch (IOException ioe) {
      fail("Count (mapped) should not throw an exception");
    }
    assertEquals(844, num);
  }
}
