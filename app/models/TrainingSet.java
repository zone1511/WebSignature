package models;

import java.util.List;
import java.util.ArrayList;

import be.ac.ulg.montefiore.run.jahmm.*;

public class TrainingSet {

  List<Features> trainingSignatures;

  private double[] means = null;
  private double[] stds = null;

  private boolean normalized = false;

  public TrainingSet() {
    trainingSignatures = new ArrayList<Features>();
  }

  public void addSignature(Signature signature){
    trainingSignatures.add(signature.extractFeatures());
  }

  public double[] meanVector() {
    if (means != null)
      return means;

    Features meanPerSignature = new Features();

    for(Features signature : trainingSignatures) {
      meanPerSignature.addVector(signature.meanVector());
    }

    this.means = meanPerSignature.meanVector();

    return means;
  }

  public double[] stdVector() {
    if (stds != null)
      return stds;

    Features stdPerSignature = new Features();

    for(Features signature : trainingSignatures) {
      stdPerSignature.addVector(signature.stdVector());
    }

    this.stds = stdPerSignature.meanVector();

    return stds;
  }

  public void normalize() {

    assert !normalized : "Already normalized !";

    if (means == null)
      meanVector();
    if (stds == null)
      stdVector();

    System.out.println("Mean Vector size : "+means.length);
    System.out.println("Std Vector size : "+stds.length);


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
      signaturesSet.add(signature.toObservationVectorList());
    }

    return signaturesSet;
  }

}