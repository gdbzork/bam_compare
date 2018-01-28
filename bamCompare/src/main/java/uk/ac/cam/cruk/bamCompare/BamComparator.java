package uk.ac.cam.cruk.bamCompare;

import java.io.IOException;
import java.util.List;

import htsjdk.samtools.SAMRecord;

/**
 * Hello world!
 *
 */
public class BamComparator {

  protected boolean compareGroups(List<SAMRecord> rec1, List<SAMRecord> rec2) {
    boolean okay = true;

    String rec1name = rec1.size() > 0 ? rec1.get(0).getReadName() : null;
    String rec2name = rec2.size() > 0 ? rec2.get(0).getReadName() : null;

    if (!(rec1name.equals(rec2name))) {
      okay = false;
      System.out.println("Name Mismatch: " + rec1name + " != " + rec2name);
    }
    if (rec1.size() != rec2.size()) {
      okay = false;
      System.out.println("Size Mismatch: " + rec1name + "=" + rec1.size() + " -- " + rec2name + "=" + rec2.size());
    }
    return okay;
  }

  public int compareBams(String fn1, String fn2) {
    int discrepancies = 0;
    BamFile bf1 = new BamFile(fn1);
    BamFile bf2 = new BamFile(fn2);
    List<SAMRecord> rec1;
    List<SAMRecord> rec2;

    bf1.open();
    bf2.open();
    rec1 = bf1.nextNameGroup();
    rec2 = bf2.nextNameGroup();

    while (true) {
      String n1 = rec1.size() > 0 ? rec1.get(0).getReadName() : null;
      String n2 = rec2.size() > 0 ? rec2.get(0).getReadName() : null;
      if (n1 == null) {
        if (n2 == null) {
          // end of both files
          break;
        } else {
          System.out.println("Trailing reads in " + fn2 + ": " + n2);
          rec2 = bf2.nextNameGroup();
        }
      } else if (n2 == null) {
        System.out.println("Trailing reads in " + fn1 + ": " + n1);
        rec1 = bf1.nextNameGroup();
      } else {
        int comp = n1.compareTo(n2);
        if (comp == 0) { // names match
          if (!compareGroups(rec1, rec2)) {
            discrepancies++;
          }
          rec1 = bf1.nextNameGroup();
          rec2 = bf2.nextNameGroup();
        } else if (comp < 0) { // name missing in bf2
          System.out.println("Extra Read in " + fn1 + ": " + n1);
          rec1 = bf1.nextNameGroup();
        } else {
          System.out.println("Extra Read in " + fn2 + ": " + n2);
          rec2 = bf2.nextNameGroup();
        }
      }
    }
    try {
      bf1.close();
      bf2.close();
    } catch (IOException ioe) {
      System.err.println("Got error closing BAM files: " + ioe.toString());
    }
    return discrepancies;
  }

  public static void main(String[] args) {
    BamComparator bc = new BamComparator();

    int discrepancies = bc.compareBams(args[0], args[1]);
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
