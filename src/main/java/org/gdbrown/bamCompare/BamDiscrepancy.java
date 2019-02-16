/**
 * 
 */
package uk.ac.cam.cruk.bamCompare;

/**
 * @author Gord Brown
 *
 */
public abstract class BamDiscrepancy {
  protected String readName = null;
  protected String fileName = null;
  protected int count = 0;
  protected String message  = null;

  public BamDiscrepancy(String rn, String fn, int ct, String msg) {
    readName = rn;
    fileName = fn;
    count = ct;
    message = msg;
  }

  public String toString() {
    return "Discrepancy: " + readName + " " + message;
  }
}
