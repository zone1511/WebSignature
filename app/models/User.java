package models;

import models.SignatureModel;
import java.util.List;
import be.ac.ulg.montefiore.run.jahmm.*;

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

  public User(String name) {
    this.name = name;
  }

  public boolean enroll(List<List<double[]>> traces) {
    signatureModel = new SignatureModel();
    return signatureModel.train(traces);
  }

  public double probability(List<double[]> trace) {
    return signatureModel.probability(trace);
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