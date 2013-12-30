package models;

import java.util.List;

import java.util.*;

import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;

import com.avaje.ebean.annotation.CreatedTimestamp;

import java.sql.Timestamp;

import javax.persistence.*;

@Entity
public class User extends Model {

  @Id
  public Long id;

  @Required
  public String name;

  public String email;

  @OneToOne(cascade=CascadeType.ALL)
  @JoinColumn(name = "model_id")
  public SignatureModel signatureModel;

  @OneToMany(cascade=CascadeType.ALL)
  @JoinColumn(name = "owner_id")
  public List<Signature> signatures;

  @CreatedTimestamp
  Timestamp cretime;

  @Version
  Timestamp updtime;

  public User(String name) {
    this.name = name;
  }

  public boolean enroll(List<List<double[]>> rawSignatures) {
    TrainingSet trainingSet = new TrainingSet();

    for(List<double[]> rawSignature : rawSignatures) {
      Signature signature = new Signature(
        rawSignature,
        this,
        Signature.Type.TRAINING,
        Signature.AcquisitionMethod.UNKNOWN,
        Signature.DeviceType.UNKNOWN,
        "undefined", "", -1, -1);    
      signatures.add(signature);
      trainingSet.addSignature(signature);
    }

    signatureModel = new SignatureModel();
    return signatureModel.train(trainingSet);
  }

  public boolean enroll() {

    TrainingSet trainingSet = new TrainingSet(signatures);

    signatureModel = new SignatureModel();

    return signatureModel.train(trainingSet);
  }

  public boolean addSignature(List<double[]> rawSignature) {
    Signature signature = new Signature(
      rawSignature,
      this,
      Signature.Type.TRAINING,
      Signature.AcquisitionMethod.UNKNOWN,
      Signature.DeviceType.UNKNOWN,
      "undefined", "", -1, -1);    
    signatures.add(signature);
    this.save();
    
    return true;
  }

  public double probability(List<double[]> rawSignature) {
    System.out.println("Probability");
    Signature signature = new Signature(
        rawSignature,
        this,
        Signature.Type.TEST,
        Signature.AcquisitionMethod.UNKNOWN,
        Signature.DeviceType.UNKNOWN,
        "undefined", "", -1, -1); 
    Signature.create(signature);   
    return signatureModel.probability(signature.extractFeatures());
  }

  public static Finder<Long,User> find = new Finder(
    Long.class, User.class
  );

  public static List<User> all() {
  return find.all();
  }

  public static void create(User user) {
    user.save();
  }

  public static void delete(Long id) {
    find.ref(id).delete();
  }
}