package models;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import be.ac.ulg.montefiore.run.jahmm.*;

public class Features {

  private List<double[]> localFeatures;

  private double[] globalFeatures;

  private int nbLocalFeatures = 2;

  private int nbGlobalFeatures = 1;

  private double[] meansLocal = null;

  private double[] stdsLocal = null;

  public Features(Samples samples) {
    localFeatures = new ArrayList<double[]>();

    globalFeatures = new double[nbGlobalFeatures];
    globalFeatures[0] = samples.getSize();
    System.out.println("Global feature : "+globalFeatures[0]);

    samples.setFromOrigin();
    double nbDiscontinuities = 0.;
    for (int i=0; i<samples.getSize(); i++) {

      double[] sample = samples.get(i);
      double[] features = new double[nbLocalFeatures];

      //Sample value [-1, -1, -1] is only here to indicate a discontinuity.
      if (sample[0] != -1) {
        features[0] = sample[0];
        features[1] = sample[1];
        //features[2] = (samples.get(i,1)[0]-samples.get(i,-1)[0])/2.;
        //features[3] = (samples.get(i,1)[1]-samples.get(i,-1)[1])/2.;
        //features[4] = Math.sqrt(Math.pow(features[2],2)+Math.pow(features[3],2));
        localFeatures.add(features);
      } else {
        nbDiscontinuities+=1.;
      }
    }
    //globalFeatures[1] = nbDiscontinuities;

    meanLocalVector();
    stdLocalVector();

    //globalFeatures[1] = meansLocal[2];
    //globalFeatures[2] = meansLocal[3];
    //globalFeatures[3] = stdsLocal[2];
    //globalFeatures[4] = stdsLocal[3];
  }

  public Features() {
    localFeatures = new ArrayList();
    nbLocalFeatures = 0;
  }

  public void addVector(double[] v) {
    localFeatures.add(v);
    nbLocalFeatures = v.length;
    System.out.println("NB : "+nbLocalFeatures);
  }

  public double[] getVector(int i) {
    return localFeatures.get(i).clone();
  }

  public int getSize() {
    return localFeatures.size();
  }

  public int getNbLocalFeatures() {
    return nbLocalFeatures;
  }

  public void normalizeLocalFeatures(double[] meanVector, double[] stdVector) {

    for(double[] vector : localFeatures) {
      for(int i=0; i<nbLocalFeatures; i++) {
        vector[i] = (vector[i]-meanVector[i])/stdVector[i];
      }
    }
  }

  public void normalizeGlobalFeatures(double[] meanVector, double[] stdVector) {

      for(int i=0; i<nbGlobalFeatures; i++) {
        System.out.println("glob : "+i+" : "+globalFeatures[i]);
        System.out.println("mean : "+meanVector[i]);
        System.out.println("std : "+stdVector[i]);

        globalFeatures[i] = (globalFeatures[i]-meanVector[i])/stdVector[i];
      }
  }

  public List<ObservationVector> toLocalObservationVectorList() {
    List<ObservationVector> observationVectorList = new ArrayList();

    for(double[] vector : localFeatures) {
      ObservationVector observationVector = new ObservationVector(vector);
      observationVectorList.add(observationVector);
    }

    return observationVectorList;
  }

  public ObservationVector toGlobalObservationVector() {

    ObservationVector observationVector = new ObservationVector(globalFeatures);

    return observationVector;
  }

  public List<ObservationVector> toGlobalObservationVectorList() {
    List<ObservationVector> observationVectorList = new ArrayList();
    ObservationVector observationVector = new ObservationVector(globalFeatures);
    observationVectorList.add(observationVector);

    return observationVectorList;
  }

  public double[] meanLocalVector() {

    if (meansLocal != null)
      return meansLocal;

    meansLocal = new double[nbLocalFeatures];
    Arrays.fill(meansLocal, 0);

    for(double[] vector : localFeatures) {
      for(int d=0; d<nbLocalFeatures; d++) {
        meansLocal[d] += vector[d];
      }
    }

    for(int d=0; d<nbLocalFeatures; d++) {
      meansLocal[d] /= localFeatures.size();
    }

    return meansLocal.clone();
  }

  public double[] stdLocalVector(double[] meansLocal) {

    if (stdsLocal != null)
      return stdsLocal;

    stdsLocal = new double[nbLocalFeatures];
    Arrays.fill(stdsLocal, 0);

    for(double[] vector : localFeatures) {
      for(int d=0; d<nbLocalFeatures; d++) {
        stdsLocal[d] += Math.pow((vector[d]-meansLocal[d]),2);
      }
    }

    for(int d=0; d<nbLocalFeatures; d++) {
      stdsLocal[d] = Math.sqrt(stdsLocal[d]/localFeatures.size());
    }

    return stdsLocal.clone();
  }

  public double[] stdLocalVector() {
    if (stdsLocal != null)
      return stdsLocal;
    else
      return stdLocalVector(meanLocalVector());
  }

  public double[] getGlobalFeatures() {
    return globalFeatures.clone();
  }

  public String toString() {
    return Arrays.toString(globalFeatures);
  }

}