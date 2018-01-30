package uk.ac.cam.cruk.bamCompare;

public class MissingMatch extends BamDiscrepancy {

  public MissingMatch(String rn, String fn, int ct, String msg) {
    super(rn, fn, ct, msg);
    // TODO Auto-generated constructor stub
  }

  @Override
  public String toString() {
    return "Missing Alignment: "+fileName+": "+readName+" ("+count+")"+(message == null || message.equals("") ? "" : " "+message);
  }
}
