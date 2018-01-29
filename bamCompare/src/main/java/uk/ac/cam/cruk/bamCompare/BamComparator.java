package uk.ac.cam.cruk.bamCompare;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import htsjdk.samtools.SAMRecord;

/**
 * Hello world!
 *
 */
public class BamComparator {

  protected boolean compareGroups(List<SAMRecord> rec1, String rec1name, List<SAMRecord> rec2, String rec2name) {
    boolean okay = true;

    if (rec1.size() != rec2.size()) {
      okay = false;
      System.out.println("Size Mismatch: " + rec1name + "=" + rec1.size() + " -- " + rec2name + "=" + rec2.size());
    }
    return okay;
  }

  public void report(List<SAMRecord> recs,String fn) {
    System.out.println(fn+"\t"+recs.size()+"\t"+recs.get(0).getReadName());
//    for (SAMRecord rec : recs) {
//      System.out.println("U: " + rec.getContig() + ":" + rec.getAlignmentStart() + "-" + rec.getAlignmentEnd() + " : "
//          + rec.getReadName());
//    }
  }

  public int filterGroup(List<SAMRecord> recs) {
    ListIterator<SAMRecord> recIt = recs.listIterator();
    int removed = 0;
    while (recIt.hasNext()) {
      SAMRecord rec = recIt.next();
      boolean remove = rec.getReadUnmappedFlag() || rec.getIntegerAttribute("NM") > 0 || rec.getCigarLength() > 1;
//        remove = rec.getReadUnmappedFlag() || rec.getIntegerAttribute("NM") > 0;
//      } catch (NullPointerException npe) {
//        System.err.println("NPE: " + npe.toString());
//      }
      if (remove) {
        recIt.remove();
        removed++;
      }
    }
    return removed;
  }

  public int compareBams(String fn1, String fn2) {
    int discrepancies = 0;
    BamFile bf1 = new BamFile(fn1);
    BamFile bf2 = new BamFile(fn2);
    List<SAMRecord> rec1;
    List<SAMRecord> rec2;
    boolean lastRec1 = false;
    boolean lastRec2 = false;
    String name1 = null;
    String name2 = null;
    int unique1 = 0;
    int unique2 = 0;

    bf1.open();
    bf2.open();
    rec1 = bf1.nextNameGroup();
    lastRec1 = rec1.size() == 0;
    name1 = lastRec1 ? null : rec1.get(0).getReadName();
    rec2 = bf2.nextNameGroup();
    lastRec2 = rec2.size() == 0;
    name2 = lastRec2 ? null : rec2.get(0).getReadName();
    filterGroup(rec1);
    filterGroup(rec2);

    while (true) {
      if (lastRec1) {
        if (lastRec2) {
          // end of both files
          break;
        } else {
          System.out.println("Trailing reads in " + fn2 + ": " + name2);
          rec2 = bf2.nextNameGroup();
          lastRec2 = rec2.size() == 0;
          name2 = lastRec2 ? null : rec2.get(0).getReadName();
          filterGroup(rec2);
          discrepancies++;
        }
      } else if (lastRec2) {
        System.out.println("Trailing reads in " + fn1 + ": " + name1);
        rec1 = bf1.nextNameGroup();
        lastRec1 = rec1.size() == 0;
        name1 = lastRec1 ? null : rec1.get(0).getReadName();
        filterGroup(rec1);
        discrepancies++;
      } else {
        int comp = name1.compareTo(name2);
        if (comp == 0) { // names match
          if (rec1.size() == 0 && rec2.size() == 0) { // no exact matches (or unaligned)
            // nothing to do
          } else if (rec1.size() == 0) {
            report(rec2,fn2);
            discrepancies++;
            unique2++;
          } else if (rec2.size() == 0) {
            report(rec1,fn1);
            discrepancies++;
            unique1++;
          } else if (!compareGroups(rec1, name1, rec2, name2)) {
            discrepancies++;
          }
          rec1 = bf1.nextNameGroup();
          lastRec1 = rec1.size() == 0;
          name1 = lastRec1 ? null : rec1.get(0).getReadName();
          rec2 = bf2.nextNameGroup();
          lastRec2 = rec2.size() == 0;
          name2 = lastRec2 ? null : rec2.get(0).getReadName();
          filterGroup(rec1);
          filterGroup(rec2);
        } else if (comp < 0) { // name missing in bf2
          System.out.println("Extra Read in " + fn1 + ": " + name1);
          rec1 = bf1.nextNameGroup();
          lastRec1 = rec1.size() == 0;
          name1 = lastRec1 ? null : rec1.get(0).getReadName();
          filterGroup(rec1);
          discrepancies++;
        } else {
          System.out.println("Extra Read in " + fn2 + ": " + name2);
          rec2 = bf2.nextNameGroup();
          lastRec2 = rec2.size() == 0;
          name2 = lastRec2 ? null : rec2.get(0).getReadName();
          filterGroup(rec2);
          discrepancies++;
        }
      }
    }
    try {
      bf1.close();
      bf2.close();
    } catch (IOException ioe) {
      System.err.println("Got error closing BAM files: " + ioe.toString());
    }
    System.out.println(fn1+": "+unique1+" unique");
    System.out.println(fn2+": "+unique2+" unique");
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
