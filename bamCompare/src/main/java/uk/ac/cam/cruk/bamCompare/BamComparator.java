package uk.ac.cam.cruk.bamCompare;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class BamComparator {

  public static void main(String[] args) {
    for (int i = 0; i < args.length; i++) {
      System.out.println("BAM file: " + args[i]);
      BamFile bf = new BamFile(args[i]);
      try {
        int count = bf.count();
        System.out.println("count (all): " + count);
        count = bf.count(true);
        System.out.println("count (mapped): " + count);
      } catch (IOException ioe) {
        System.out.println("Caught IO exception while counting " + args[i]);
      }
    }
  }
}
