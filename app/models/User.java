package models;

import models.SignatureModel;
import java.util.List;
import be.ac.ulg.montefiore.run.jahmm.*;

public class User {

  private String name;
  private SignatureModel signatureModel;

  public User(String name) {
    this.name = name;
  }

  public void enroll(List<List<double[]>> traces) {
    //signatureModel = new SignatureModel(traces);
  }

  public void probability(List<double[]> trace) {
    //signatureModel.probability(trace);

  }
}