/**
 * 
 */
package uk.ac.cam.cruk.bamCompare;

/**
 * @author brown22
 *
 */
public abstract class BamDiscrepancy {
  private String readName = null;
  private String message  = null;

  public BamDiscrepancy(String rn, String msg) {
    readName = rn;
    message = msg;
  }

  public BamDiscrepancy(String rn) {
    this(rn, "");
  }

  public String toString() {
    return "Discrepancy: " + readName + " " + message;
  };
}
