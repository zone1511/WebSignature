package models;

import play.api.*;
import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.Writer;
import java.io.Reader;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;
import org.apache.commons.lang.*;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;

@Entity
public class SignatureModel {

  @Id
  public Long id;

  //Put in new class and use interface ScalarTypeConverter<B,S>
  @Column(columnDefinition = "TEXT")
  public String hmm;
  // VERY UGLY CHANGE THIS ASAP :
  public double meanFeature1;
  public double meanFeature2;
  public double meanFeature3;
  public double meanFeature4;
  public double meanFeature5;
  public double stdFeature1;
  public double stdFeature2;
  public double stdFeature3;
  public double stdFeature4;
  public double stdFeature5;

  public double averageTrainingScore;
  @Transient
  private int nbFeatures = 5;

  @Transient
  public double[] mean = {1,2,3,4,5};
  @Transient
  public double[] std = new double[nbFeatures];

  @Transient
  private Hmm<ObservationVector> hiddenMarkovModel;



  @Transient
  private final int nbStates = 4;
  @Transient
  private final int nbGaussians = 3;

  private List<double[]> extractFeatures(List<double[]> signature) {
    List<double[]> featureVectors = new ArrayList();
    for (int i=0; i<signature.size(); i++) {
      double[] sample = signature.get(i);
      double[] features = new double[nbFeatures];
      //Sample value [-1, -1, -1] is only here to indicate a discontinuity.
      if (sample[0] != -1) {
        features[0] = sample[0];
        features[1] = sample[1];
        /*features[2] = (getRelativeSample(i,1,signature)[0]-getRelativeSample(i,-1,signature)[0])/(getRelativeSample(i,1,signature)[2]-getRelativeSample(i,-1,signature)[2]);
        features[3] = (getRelativeSample(i,1,signature)[1]-getRelativeSample(i,-1,signature)[1])/(getRelativeSample(i,1,signature)[2]-getRelativeSample(i,-1,signature)[2]);*/
        features[2] = (getRelativeSample(i,1,signature)[0]-getRelativeSample(i,-1,signature)[0])/2.;
        features[3] = (getRelativeSample(i,1,signature)[1]-getRelativeSample(i,-1,signature)[1])/2.;
        features[4] = Math.sqrt(Math.pow(features[2],2)+Math.pow(features[3],2));
        featureVectors.add(features);
      }
    }
    return featureVectors;
  }

  private void regularizePosition(List<double[]> signature) {
    double[] initialPosition = {signature.get(0)[0], signature.get(0)[1]};
    //List<double[]> regularizedSignature = new ArrayList();
    //double[] currentPosition = new double[2];
    for(double[] position : signature) {
      if(position[0] != -1) {
        position[0] -= initialPosition[0];
        position[1] -= initialPosition[1];
      }
    }
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
      regularizePosition(samples);
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

    for(List<ObservationVector> trainingSignature : tracesNormalized) {
      averageTrainingScore += hiddenMarkovModel.lnProbability(trainingSignature);
    }
    averageTrainingScore /= tracesNormalized.size();
    System.out.println("Average training score : "+averageTrainingScore);
    //TODO remove this line
    hmm = getHiddenMarkovModel();
    setValues();

    //System.out.println(hiddenMarkovModel.toString());
    //String sHMM = getHmm();
    //readHmm(sHMM);
    //System.out.println(getHmm());
    //System.out.println(hiddenMarkovModel.toString());
    /*
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
    */
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
    //TODO remove this line
    setHiddenMarkovModel(hmm);
    getValues();
    
    regularizePosition(trace);
    for(double[] features : extractFeatures(trace)) {
      System.out.println(Arrays.toString(features));
    }
    List<ObservationVector> normalizedSignature = normalize_sign(extractFeatures(trace));
    double probability = hiddenMarkovModel.lnProbability(normalizedSignature);
    double score = (averageTrainingScore/probability);
    System.out.println("Probab");
    System.out.println(hiddenMarkovModel.toString());
    System.out.println("Traces : "+normalizedSignature.toString());
    System.out.println("Probability : "+probability);
    System.out.println("Score : "+score);
    System.out.println("State sequence : "+Arrays.toString(hiddenMarkovModel.mostLikelyStateSequence(normalizedSignature)));
    return score;
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

  public String getHiddenMarkovModel(){
    HmmWriter hmmWriter = new HmmWriter();
    Writer writer = new StringWriter();
    OpdfMultiGaussianMixtureWriter oPdfWriter = new OpdfMultiGaussianMixtureWriter();
    try {
      hmmWriter.write(writer, oPdfWriter, hiddenMarkovModel);
    } catch (IOException e) {
      System.out.println("Oops ! An exception occured while saving the HMM.");
    }
    return writer.toString();
  }

  public void setHiddenMarkovModel(String hmm){
    HmmReader hmmReader = new HmmReader();
    Reader reader = new StringReader(hmm);
    OpdfMultiGaussianMixtureReader oPdfReader = new OpdfMultiGaussianMixtureReader();
    try {
      hiddenMarkovModel = hmmReader.read(reader, oPdfReader);
    } catch (Exception e) {
      System.out.println("Oops ! An exception occured while reading the HMM.");
    }
  }
  /*
  public List<Double> getMeans(){
    return Arrays.asList(ArrayUtils.toObject(mean));
  }

  public List<Double> getStds(){
    return Arrays.asList(ArrayUtils.toObject(std));
  }

  public void setMeans(List<Double> out) {
    mean = ArrayUtils.toPrimitive(out.toArray(new Double[out.size()]));
  }

  public void setStds(List<Double> out) {
    std = ArrayUtils.toPrimitive(out.toArray(new Double[out.size()]));
  }*/
// VERY UGLY CHANGE THIS ASAP :
  public void setValues() {
    meanFeature1 = mean[0];
    meanFeature2 = mean[1];
    meanFeature3 = mean[2];
    meanFeature4 = mean[3];
    meanFeature5 = mean[4];
    stdFeature1 = std[0];
    stdFeature2 = std[1];
    stdFeature3 = std[2];
    stdFeature4 = std[3];
    stdFeature5 = std[4];
  }

  public void getValues() {
    mean[0] = meanFeature1;
    mean[1] = meanFeature2;
    mean[2] = meanFeature3;
    mean[3] = meanFeature4;
    mean[4] = meanFeature5;
    std[0] = stdFeature1;
    std[1] = stdFeature2;
    std[2] = stdFeature3;
    std[3] = stdFeature4;
    std[4] = stdFeature5;
  }
}

