/**
 * 
 */
package uk.ac.cam.cruk.bamCompare;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

/**
 * @author Gord Brown
 *
 */
class BamFile {

  Path bamFileFN = null;
  SamReader rdr = null;
  SAMRecordIterator rdrIt = null;
  SAMRecord current = null;

  /**
   * 
   */
  public BamFile() {
    super();
  }

  public BamFile(String fn) {
    bamFileFN = Paths.get(fn);
  }

  public void open() {
    rdr = SamReaderFactory.makeDefault().open(bamFileFN.toFile());
    rdrIt = rdr.iterator();
    current = rdrIt.next();
  }
  
  public List<SAMRecord> nextNameGroup() {
    List<SAMRecord> nameGroup = new ArrayList<SAMRecord>();
    String name = current.getReadName();
    nameGroup.add(current);
    while ((current = rdrIt.next()).getReadName().equals(name)) {
      nameGroup.add(current);
    }
    return nameGroup;
  }
  public void close() throws IOException {
    rdr.close();
  }
  
  public int count() throws IOException {
    return count(false);
  }
  
  public int count(boolean mapped) throws IOException {
    int n = 0;
    SamReader fd = SamReaderFactory.makeDefault().open(bamFileFN.toFile());
    for (SAMRecord rec : fd) {
      if (!mapped || !rec.getReadUnmappedFlag()) {
        n += 1;
      }
    }
    fd.close();
    return n;
  }

}
