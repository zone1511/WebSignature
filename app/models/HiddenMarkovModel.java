package models;

import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;

import java.io.Writer;
import java.io.Reader;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;

public class HiddenMarkovModel {

  private Hmm<ObservationVector> hiddenMarkovModel;

  public HiddenMarkovModel(
    List<List<ObservationVector>> observations,
    int nbGaussians,
    int nbFeatures,
    int nbStates
  ) {

    KMeansLearner<ObservationVector> kml =
      new KMeansLearner(
        nbStates,
        new OpdfMultiGaussianMixtureFactory(nbGaussians, nbFeatures),
        observations);

    hiddenMarkovModel = kml.iterate();
    
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
  }

  public HiddenMarkovModel(String hmm) {
    this.setHiddenMarkovModel(hmm);
  }

  public boolean train(List<List<ObservationVector>> observations) {

    System.out.println("Training...");

    BaumWelchScaledLearner bwl = new BaumWelchScaledLearner();

    try {
      hiddenMarkovModel = bwl.learn(hiddenMarkovModel.clone(), observations);
    } catch(CloneNotSupportedException e) {
      System.out.println("Impossible to clone this HMM.");
    }

    return true;

  }

  public double probability(List<ObservationVector> observation) {
    
    return hiddenMarkovModel.lnProbability(observation);
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
}