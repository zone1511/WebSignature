package models;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import be.ac.ulg.montefiore.run.jahmm.*;

public class TrainingSet {

  List<Features> trainingSignatures;

  private double[] meansLocal = null;
  private double[] stdsLocal = null;

  private double[] meansGlobal = null;
  private double[] stdsGlobal = null;

  public TrainingSet() {
    trainingSignatures = new ArrayList<Features>();
  }

  public void addSignature(Signature signature){
    trainingSignatures.add(signature.extractFeatures());
  }

  public double[] meanLocalVector() {
    if (meansLocal != null)
      return meansLocal;

    Features meanPerSignature = new Features();

    for(Features signature : trainingSignatures) {
      meanPerSignature.addVector(signature.meanLocalVector());
    }

    this.meansLocal = meanPerSignature.meanLocalVector();

    return meansLocal.clone();
  }

  public double[] meanGlobalVector(){
    if (meansGlobal != null)
      return meansGlobal;

    Features meanPerSignature = new Features();

    for(Features signature : trainingSignatures) {
      meanPerSignature.addVector(signature.getGlobalFeatures());
    }

    this.meansGlobal = meanPerSignature.meanLocalVector();

    return meansGlobal.clone();
  }


  public double[] stdGlobalVector(){
    if (stdsGlobal != null)
      return stdsGlobal;

    Features stdPerSignature = new Features();

    for(Features signature : trainingSignatures) {
      stdPerSignature.addVector(signature.getGlobalFeatures());
    }

    this.stdsGlobal = stdPerSignature.stdLocalVector();

    return stdsGlobal.clone();
  }

  public double[] stdLocalVector() {
    if (stdsLocal != null)
      return stdsLocal;

    Features stdPerSignature = new Features();

    for(Features signature : trainingSignatures) {
      stdPerSignature.addVector(signature.stdLocalVector());
    }

    this.stdsLocal = stdPerSignature.meanLocalVector();

    return stdsLocal.clone();
  }

  public void normalizeLocalFeatures() {

    if (meansLocal == null)
      meanLocalVector();
    if (stdsLocal == null)
      stdLocalVector();

    System.out.println("Mean Vector size : "+meansLocal.length);
    System.out.println("Std Vector size : "+stdsLocal.length);


    for(Features signature : trainingSignatures) {
      signature.normalizeLocalFeatures(meansLocal, stdsLocal);
    }

  }


  public void normalizeGlobalFeatures() {
  
    if (meansGlobal == null)
      meanGlobalVector();
    if (stdsGlobal == null)
      stdGlobalVector();

    System.out.println("Mean Vector : "+Arrays.toString(meansGlobal));
    System.out.println("Std Vector : "+Arrays.toString(stdsGlobal));

    for(Features signature : trainingSignatures) {
      signature.normalizeGlobalFeatures(meansGlobal, stdsGlobal);
    }
    
  }

  public int getSize() {
    return trainingSignatures.size();
  }

  public List<List<ObservationVector>> toLocalObservationVectorLists() {
    List<List<ObservationVector>> signaturesSet = new ArrayList();

    for(Features signature : trainingSignatures) {
      signaturesSet.add(signature.toLocalObservationVectorList());
    }

    return signaturesSet;
  }

  public List<List<ObservationVector>> toGlobalObservationVectorLists() {
    List<ObservationVector> signaturesSet = new ArrayList();

    for(Features signature : trainingSignatures) {
      System.out.println(signature.toString());
      signaturesSet.add(signature.toGlobalObservationVector());
      System.out.println("ADD");
    }

    List<List<ObservationVector>> signs = new ArrayList();
    signs.add(signaturesSet);
    return signs;
  }
}