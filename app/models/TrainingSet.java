package models;

public class TrainingSet {

  List<Features> trainingSignatures;

  private double[] means = null;
  private double[] stds = null;

  private boolean normalized = false;

  public void addSignature(Signature signature){
    trainingSignatures.add(signature);
  }

  public double[] meanVector() {
    if (means != null)
      return means;

    Features meanPerSignature = new Features();

    for(Features signature : trainingSignatures) {
      meanPerSignature.addVector(signature.meanVector());
    }

    means = meanPerSignature.meanVector();

    return means;
  }

  public double[] stdVector() {
    if (stds != null)
      return stds;

    Features stdPerSignature = new Features();

    for(Features signature : trainingSignatures) {
      stdPerSignature.addVector(signature.stdVector());
    }

    stds = computeMeanVector.meanVector();

    return stds;
  }

  public void normalize() {

    assert !normalized : "Already normalized !";

    if (means == null)
      meanVector();
    if (std == null)
      stdVector();

    for(Features signature : trainingSignatures) {
      signature.normalize(means, stds);
    }

    normalized = true;
  }

  public int getSize() {
    return trainingSignatures.size();
  }

  public List<List<ObservationVector>> toObservationVectorLists() {
    List<List<ObservationVector>> signaturesSet = new ArrayList();

    for(Features signature : trainingSignatures) {
      signatureSet.add(signature.toObservationVectorList());
    }

    return signaturesSet;
  }

}