package models;

import java.util.List;

import java.util.*;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;

@Entity
public class User extends Model {

  @Id
  public Long id;

  @Required
  public String name;

  @OneToOne(cascade=CascadeType.ALL)
  @JoinColumn(name = "model_id")
  public SignatureModel signatureModel;

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
      Signature signature = new Signature(rawSignature, this, Signature.Type.TRAINING, "undefined");    
      // @TODO : Write in db here =)
      trainingSet.add(signature);
    }

    signatureModel = new SignatureModel();
    return signatureModel.train(trainingSet);
  }

  public double probability(List<double[]> rawSignature) {
    Signature signature = new Signature(rawSignature, this, Signature.Type.TRAINING, "undefined");
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