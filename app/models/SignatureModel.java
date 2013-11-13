package models;

import play.api.*;
import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SignatureModel {

  private double[] mean;
  private double[] std;

  private int nbFeatures = 5;

  private Hmm<ObservationVector> hiddenMarkovModel;

  private final int nbStates;
  private final int nbGaussians;

  public SignatureModel(int nbStates, int nbGaussians) {
    this.nbStates = nbStates;
    this.nbGaussians = nbGaussians;
  }

  private List<double[]> extractFeatures(List<double[]> signature) {
    List<double[]> featureVectors = new ArrayList();
    for (int i=0; i<signature.size(); i++) {
      double[] sample = signature.get(i);
      double[] features = new double[nbFeatures];
      //Sample value [-1, -1, -1] is only here to indicate a discontinuity.
      if (sample[0] != -1) {
        features[0] = sample[0];
        features[1] = sample[1];
        features[2] = (getRelativeSample(i,1,signature)[0]-getRelativeSample(i,-1,signature)[0])/(getRelativeSample(i,1,signature)[2]-getRelativeSample(i,-1,signature)[2]);
        features[3] = (getRelativeSample(i,1,signature)[1]-getRelativeSample(i,-1,signature)[1])/(getRelativeSample(i,1,signature)[2]-getRelativeSample(i,-1,signature)[2]);
        features[4] = Math.sqrt(Math.pow(features[2],2)+Math.pow(features[3],2));
        featureVectors.add(features);
      }
    }
    return featureVectors;
  }


  private double[] getRelativeSample(int startIndex, int offset, List<double[]> signature) {
    int finalIndex = startIndex+offset;
    /*if (finalIndex < 0 || finalIndex >= signature.size()) {
      double[] defaultSample = new double[nbFeatures];
      Arrays.fill(defaultSample, 0.);
      return defaultSample;
    }*/
    if (finalIndex < 0)
      return signature.get(0);
    if (finalIndex >= signature.size())
      return signature.get(signature.size()-1);
    double[] sample = signature.get(finalIndex);
    if(sample[0] == -1)
      return signature.get(startIndex);
    return sample;
  }


  public boolean train(List<List<double[]>> traces) {

    List<List<double[]>> tracesFeatures = new ArrayList();
    for(List<double[]> samples : traces) {
      tracesFeatures.add(extractFeatures(samples));
    }

    computeParameters(tracesFeatures);
    List<List<ObservationVector>> tracesNormalized = normalize(tracesFeatures);

    System.out.println("means = "+Arrays.toString(mean));
    System.out.println("std = "+Arrays.toString(std));
    System.out.println("Traces : "+tracesNormalized.toString());

    KMeansLearner<ObservationVector> kml =
      new KMeansLearner(
        nbStates,
        new OpdfMultiGaussianMixtureFactory(nbGaussians, nbFeatures),
        tracesNormalized);

    hiddenMarkovModel = kml.iterate();
    System.out.println(hiddenMarkovModel.toString());
    
    hiddenMarkovModel.setPi(0, 1);
    for(int i=1; i<nbStates; i++) {
      hiddenMarkovModel.setPi(i, 0);
    }

    for(int i=0; i<(nbStates); i++) {
      for(int j=0; j<(nbStates); j++) {
        hiddenMarkovModel.setAij(i,j, 0);
      }
    }
    for(int i=0; i<(nbStates-1); i++) {
      hiddenMarkovModel.setAij(i,i, 0.5);
      hiddenMarkovModel.setAij(i,i+1, 0.5);
    }
    hiddenMarkovModel.setAij(nbStates-1,nbStates-1, 0.5);
    System.out.println(hiddenMarkovModel.toString());

    train_BW(tracesNormalized);
    System.out.println(hiddenMarkovModel.toString());
    System.out.println("Traces : "+tracesNormalized.get(0).toString());
    System.out.println("Probability : "+hiddenMarkovModel.lnProbability(tracesNormalized.get(0)));
    System.out.println("State sequence : "+Arrays.toString(hiddenMarkovModel.mostLikelyStateSequence(tracesNormalized.get(0))));
    System.out.println("Traces : "+tracesNormalized.get(1).toString());
    System.out.println("Probability : "+hiddenMarkovModel.lnProbability(tracesNormalized.get(1)));
    System.out.println("State sequence : "+Arrays.toString(hiddenMarkovModel.mostLikelyStateSequence(tracesNormalized.get(1))));
    System.out.println("Traces : "+tracesNormalized.get(2).toString());
    System.out.println("Probability : "+hiddenMarkovModel.lnProbability(tracesNormalized.get(2)));
    System.out.println("State sequence : "+Arrays.toString(hiddenMarkovModel.mostLikelyStateSequence(tracesNormalized.get(2))));
    return true;
  }

  private void train_BW(List<List<ObservationVector>> normalizedTraces) {
    System.out.println("Training...");
    BaumWelchScaledLearner bwl =
      new BaumWelchScaledLearner();
    try {
      hiddenMarkovModel = bwl.learn(hiddenMarkovModel.clone(), normalizedTraces);
    } catch(CloneNotSupportedException e) {
      System.out.println("Impossible to clone this HMM.");
    }
    
  }

  public double probability(List<double[]> trace) {
    for(double[] features : extractFeatures(trace)) {
      System.out.println(Arrays.toString(features));
    }
    List<ObservationVector> normalizedSignature = normalize_sign(extractFeatures(trace));
    System.out.println("Probab");
    System.out.println(hiddenMarkovModel.toString());
    System.out.println("Traces : "+normalizedSignature.toString());
    System.out.println("Probability : "+hiddenMarkovModel.lnProbability(normalizedSignature));
    System.out.println("State sequence : "+Arrays.toString(hiddenMarkovModel.mostLikelyStateSequence(normalizedSignature)));
    return hiddenMarkovModel.lnProbability(normalizedSignature);
  }

  private List<List<ObservationVector>> normalize(List<List<double[]>> signatures) {
    List<List<ObservationVector>> normalizedSignatures = new ArrayList();
    for(List<double[]> sign : signatures) {
      List<ObservationVector> signat = new ArrayList();;
      for(double[] sample : sign) {
        for(int i=0; i<nbFeatures; i++) {
          sample[i] = (sample[i]-mean[i])/std[i];
        }
        ObservationVector vect = new ObservationVector(sample);
        signat.add(vect);
      }
      normalizedSignatures.add(signat);
    }

    return normalizedSignatures;
  }

  private List<ObservationVector> normalize_sign(List<double[]> signature) {
      List<ObservationVector> signat = new ArrayList();
      for(double[] sample : signature) {
        for(int i=0; i<nbFeatures; i++) {
          sample[i] = (sample[i]-mean[i])/std[i];
        }
        ObservationVector vect = new ObservationVector(sample);
        signat.add(vect);
      }

    return signat;
  }

  private void computeParameters(List<List<double[]>> signatures) {
    double[] means = new double[nbFeatures];
    double[] stds = new double[nbFeatures];

    int nbSignatures = signatures.size();
    int[] nbSamples = new int[nbSignatures];

    List<double[]> meanPerSignature = new ArrayList();
    List<double[]> stdPerSignature = new ArrayList();

    int signatureCounter = 0;
    int sampleCounter = 0;

    for(List signature : signatures) {
      means = computeMeanVector(signature);
      meanPerSignature.add(means);
      stdPerSignature.add(computeStandardDeviation(signature, means));
    }

    this.mean = computeMeanVector(meanPerSignature);
    this.std = computeMeanVector(stdPerSignature);
  }

  private double[] computeMeanVector(List<double[]> signature) {

    double[] means = new double[nbFeatures];
    Arrays.fill(means, 0);

    int sampleCounter = 0;
    for(double[] sample : signature) {
      for(int featureCounter=0; featureCounter<nbFeatures; featureCounter++) {
        means[featureCounter] += sample[featureCounter];
      }
      sampleCounter++;
    }

    for(int featureCounter=0; featureCounter<nbFeatures; featureCounter++) {
      means[featureCounter] /= sampleCounter;
    }

    return means;
  }

  private double[] computeStandardDeviation(List<double[]> signature, double[] means) {

    double[] std = new double[nbFeatures];
    Arrays.fill(std, 0);

    int sampleCounter = 0;
    for(double[] sample : signature) {
      for(int featureCounter=0; featureCounter<nbFeatures; featureCounter++) {
        std[featureCounter] += Math.pow((sample[featureCounter]-means[featureCounter]),2);
      }
      sampleCounter++;
    }

    for(int featureCounter=0; featureCounter<nbFeatures; featureCounter++) {
      std[featureCounter] = Math.sqrt(std[featureCounter]/sampleCounter);
    }

    return std;
  }


}

