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

  @CreatedTimestamp
  Timestamp cretime;

  @Version
  Timestamp updtime;

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
  private HiddenMarkovModel hiddenMarkovModel;

  @Transient
  private final int nbStates = 4;
  @Transient
  private final int nbGaussians = 3;

  public boolean train(TrainingSet signatures) {

    mean = signatures.meanVector();
    std = signatures.stdVector();

    signatures.normalize();

    System.out.println("means = "+Arrays.toString(mean));
    System.out.println("std = "+Arrays.toString(std));
    //System.out.println("Traces : "+tracesNormalized.toString());

    List<List<ObservationVector>> trainingVectors = signatures.toObservationVectorLists();

    hiddenMarkovModel = new HiddenMarkovModel(nbGaussians, nbFeatures, nbStates, trainingVectors);

    hiddenMarkovModel.train(trainingVectors);

    computeAverageTrainingScore(signatures);

    //TODO remove this line, use converters instead
    hmm = getHiddenMarkovModel();
    setValues();

    return true;
  }

  public void computeAverageTrainingScore(TrainingSet normTrainingSet) {

    for(List<ObservationVector> trainingSignature : normTrainingSet.toObservationVectorLists()) {
      averageTrainingScore += hiddenMarkovModel.probability(trainingSignature);
    }

    averageTrainingScore /= normTrainingSet.getSize();

    System.out.println("Average training score : "+averageTrainingScore);

  }

  public double probability(FeaturesList signature) {
    //TODO remove this line
    setHiddenMarkovModel(hmm);
    getValues();
    
    signature.normalize(mean, std);

    double probability = hiddenMarkovModel.probability(signature.toObservationVectorList());
    double score = (averageTrainingScore/probability);
    
    return score;
  }

  // dirty little secrets :

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

  public String getHiddenMarkovModel(){
    return hiddenMarkovModel.getHiddenMarkovModel();
  }

  public void setHiddenMarkovModel(String hmm){
    hiddenMarkovModel = new HiddenMarkovModel(nbGaussians, nbFeatures);
    hiddenMarkovModel.setHiddenMarkovModel(hmm);
  }
}

