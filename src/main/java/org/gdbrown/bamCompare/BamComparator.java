package uk.ac.cam.cruk.bamCompare;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;

import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;

/**
 * Compare BAM files to see whether they find the same (or at least equivalent)
 * hits.
 *
 */
public class BamComparator {

  /**
   * Test SAM records for equality.
   * 
   * Use a less stringent test than the SAMRecord.equals() method, because all we
   * care about is if it's in the same location.
   * 
   */
  protected boolean samEqual(SAMRecord read1, SAMRecord read2) {
    boolean okay = true;
    if (!read1.getReadName().equals(read2.getReadName())) {
      // should never happen; is handled by the calling code. Just a cross-check.
      okay = false;
    } else if (read1.getReadUnmappedFlag() == read2.getReadUnmappedFlag()) {
      // unmapped flags are equal
      if (read1.getReadUnmappedFlag()) {
        // flags are equal, so if read1 is unmapped, so is read2, so equal
      } else {
        // both mapped, so test fields for equality: chrom, start, cigar, strand is all
        // we
        // need
        okay = read1.getContig().equals(read2.getContig()) && read1.getAlignmentStart() == read2.getAlignmentStart()
            && read1.getCigarString().equals(read2.getCigarString())
            && read1.getReadNegativeStrandFlag() == read2.getReadNegativeStrandFlag();
      }
    } else {
      // one mapped, other not, so unequal
      okay = false;
    }
    return okay;
  }

  /**
   * Remove alignments from two lists (destructively) that are in both lists.
   * 
   * Result is two (possibly empty) lists of alignments that occur in one file but
   * not the other.
   */
  protected void removeMatches(List<SAMRecord> rec1list, List<SAMRecord> rec2list) {
    ListIterator<SAMRecord> rec1it = rec1list.listIterator();
    while (rec1it.hasNext()) {
      SAMRecord rec1 = rec1it.next();
      ListIterator<SAMRecord> rec2it = rec2list.listIterator();
      while (rec2it.hasNext()) {
        SAMRecord rec2 = rec2it.next();
        if (samEqual(rec1, rec2)) {
          rec1it.remove();
          rec2it.remove();
          break;
        }
      }
    }
  }

  /**
   * Compare BAM records for a given read (based on read name).
   * 
   * @param rec1
   *          the first group.
   * @param rec1name
   *          the read name of the first group.
   * @param rec2
   *          the second group.
   * @param rec2name
   *          the read name of the second group (had better match the first
   *          group).
   * @return
   */
  /*
   * protected List<BamDiscrepancy> compareGroups(List<SAMRecord> rec1, String
   * rec1name, List<SAMRecord> rec2, String rec2name) { List<BamDiscrepancy>
   * problems = new ArrayList<BamDiscrepancy>();
   * 
   * if (rec1.size() == 1 && rec2.size() == 1) { if
   * (rec1.get(0).getReadUnmappedFlag() && rec2.get(0).getReadUnmappedFlag()) {
   * 
   * } } if (rec1.size() != rec2.size()) { okay = false;
   * System.out.println("Size Mismatch: " + rec1name + "=" + rec1.size() + " -- "
   * + rec2name + "=" + rec2.size()); } return okay; }
   */

  public void report(List<SAMRecord> recs, String fn, SAMFileWriter dest) {
    if (recs.size() > 0) {
      System.out.println(fn + "\t" + recs.size() + "\t" + recs.get(0).getReadName());
      for (SAMRecord rec : recs) {
        dest.addAlignment(rec);
      }
    }
    // for (SAMRecord rec : recs) {
    // System.out.println("U: " + rec.getContig() + ":" + rec.getAlignmentStart() +
    // "-" + rec.getAlignmentEnd() + " : "
    // + rec.getReadName());
    // }
  }

  public boolean unmapped(List<SAMRecord> rec) {
    return (rec.size() == 1 && rec.get(0).getReadUnmappedFlag());
  }

  public List<SAMRecord> perfectMatches(List<SAMRecord> recs) {
    ListIterator<SAMRecord> recIt = recs.listIterator();
    while (recIt.hasNext()) {
      SAMRecord rec = recIt.next();
      boolean remove = rec.getReadUnmappedFlag() || rec.getIntegerAttribute("NM") > 0 || rec.getCigarLength() > 1;
      if (remove) {
        recIt.remove();
      }
    }
    return recs;
  }

  public int compareBams(String fn1, String fn2, String out1, String out2) {
    int discrepancies = 0;
    BamFile bf1 = new BamFile(fn1);
    BamFile bf2 = new BamFile(fn2);
    SAMFileWriter out1FD;
    SAMFileWriter out2FD;
    List<SAMRecord> rec1;
    List<SAMRecord> rec2;
    String name1 = null;
    String name2 = null;
    int unique1 = 0;
    int unique2 = 0;

    bf1.open();
    out1FD = (new SAMFileWriterFactory()).makeSAMWriter(bf1.header(), false, Paths.get(out1).toFile());
    bf2.open();
    out2FD = (new SAMFileWriterFactory()).makeSAMWriter(bf2.header(), false, Paths.get(out2).toFile());

    rec1 = bf1.nextNameGroup();
    name1 = (rec1.size() == 0) ? null : rec1.get(0).getReadName();
    rec2 = bf2.nextNameGroup();
    name2 = (rec2.size() == 0) ? null : rec2.get(0).getReadName();

    while (true) {
      if (rec1.size() == 0) {
        if (rec2.size() == 0) {
          // end of both files, so we're done
          break;
        } else {
          rec2 = perfectMatches(rec2); // remove all but perfect matches
          report(rec2, fn2, out2FD); // report rec2 as extra alignments versus no hits for rec1
          rec2 = bf2.nextNameGroup();
          name2 = (rec2.size() == 0) ? null : rec2.get(0).getReadName();
          unique2++;
          discrepancies++;
        }
      } else if (rec2.size() == 0) {
        rec1 = perfectMatches(rec1);
        report(rec1, fn1, out1FD);
        rec1 = bf1.nextNameGroup();
        name1 = (rec1.size() == 0) ? null : rec1.get(0).getReadName();
        unique1++;
        discrepancies++;
      } else { // both non-empty
        int comp = name1.compareTo(name2);
        if (comp == 0) { // names match
          removeMatches(rec1, rec2); // remove all alignments that match
          if (rec1.size() == 0 && rec2.size() == 0) { // everything matches
            // nothing to do
          } else if (rec1.size() == 0) {
            rec2 = perfectMatches(rec2);
            report(rec2, fn2, out2FD);
            unique2++;
            discrepancies++;
          } else if (rec2.size() == 0) {
            rec1 = perfectMatches(rec1);
            report(rec1, fn1, out1FD);
            unique1++;
            discrepancies++;
          } else {
            // should do something interesting here: both have matches, but different. For
            // now let's skip it.
            discrepancies++;
          }
          rec1 = bf1.nextNameGroup();
          name1 = (rec1.size() == 0) ? null : rec1.get(0).getReadName();
          rec2 = bf2.nextNameGroup();
          name2 = (rec2.size() == 0) ? null : rec2.get(0).getReadName();
        } else if (comp < 0) { // name missing in bf2
          rec1 = perfectMatches(rec1);
          report(rec1, fn1, out1FD);
          rec1 = bf1.nextNameGroup();
          name1 = (rec1.size() == 0) ? null : rec1.get(0).getReadName();
          unique1++;
          discrepancies++;
        } else {
          rec2 = perfectMatches(rec2);
          report(rec2, fn2, out2FD);
          rec2 = bf2.nextNameGroup();
          name2 = (rec2.size() == 0) ? null : rec2.get(0).getReadName();
          unique2++;
          discrepancies++;
        }
      }
    }
    out1FD.close();
    out2FD.close();
    try {
      bf1.close();
      bf2.close();
    } catch (IOException ioe) {
      System.err.println("Got error closing BAM files: " + ioe.toString());
    }
    System.out.println(fn1 + ": " + unique1 + " unique");
    System.out.println(fn2 + ": " + unique2 + " unique");
    return discrepancies;
  }

  public static void main(String[] args) {
    BamComparator bc = new BamComparator();

    int discrepancies = bc.compareBams(args[0], args[1], args[2], args[3]);
    System.out.println("Discrepancies: " + discrepancies);
    /*
     * for (int i = 0; i < args.length; i++) { System.out.println("BAM file: " +
     * args[i]); BamFile bf = new BamFile(args[i]); try { int count = bf.count();
     * System.out.println("count (all): " + count); count = bf.count(true);
     * System.out.println("count (mapped): " + count); } catch (IOException ioe) {
     * System.out.println("Caught IO exception while counting " + args[i]); } }
     */

  }
}
