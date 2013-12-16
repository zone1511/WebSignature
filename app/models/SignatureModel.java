package models;

import play.api.*;
import java.util.List;
import be.ac.ulg.montefiore.run.jahmm.*;

import org.apache.commons.lang.*;

import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;

import javax.persistence.*;

import com.avaje.ebean.annotation.CreatedTimestamp;

import java.sql.Timestamp;

@Entity
public class SignatureModel extends Model{

  @Id
  public Long id;

  @OneToOne
  public User owner;

  // @Lob might be necessary for bigger HMMs,
  // but TEXT allows to inspect content of the HMM in the database
  // See HiddenMarkovModelConverter
  @Column(columnDefinition = "TEXT")
  public HiddenMarkovModel hiddenMarkovModel;

  public double averageTrainingScore;

  @Column(columnDefinition = "TEXT")
  public ObservationVector mean;

  @Column(columnDefinition = "TEXT")
  public ObservationVector std;

  public final int nbFeatures = 5;
  
  public final int nbStates = 4;
  
  public final int nbGaussians = 3;

  @CreatedTimestamp
  Timestamp cretime;

  @Version
  Timestamp updtime;

  public boolean train(TrainingSet signatures) {

    mean = new ObservationVector(signatures.meanVector());
    std = new ObservationVector(signatures.stdVector());

    //System.out.println("means = "+mean);
    //System.out.println("std = "+std);

    signatures.normalize();

    //System.out.println("Traces : "+tracesNormalized.toString());

    List<List<ObservationVector>> trainingVectors = signatures.toObservationVectorLists();

    hiddenMarkovModel = new HiddenMarkovModel(trainingVectors, nbGaussians, nbFeatures, nbStates);

    hiddenMarkovModel.train(trainingVectors);

    computeAverageTrainingScore(signatures);

    return true;
  }

  public double probability(Features signature) {

    signature.normalize(mean.values(), std.values());

    double probability = hiddenMarkovModel.probability(signature.toObservationVectorList());
    double score = (averageTrainingScore/probability);
    
    return score;
  }

  private void computeAverageTrainingScore(TrainingSet normTrainingSet) {

    for(List<ObservationVector> trainingSignature : normTrainingSet.toObservationVectorLists()) {
      averageTrainingScore += hiddenMarkovModel.probability(trainingSignature);
    }

    averageTrainingScore /= normTrainingSet.getSize();

    System.out.println("Average training score : "+averageTrainingScore);

  }

  public static Finder<Long,SignatureModel> find = new Finder<Long,SignatureModel>(
    Long.class, SignatureModel.class
  );

  public static List<SignatureModel> all() {
    return find.all();
  }

  public static void create(SignatureModel signatureModel) {
    signatureModel.save();
  }

  public static void delete(Long id) {
    find.ref(id).delete();
  }
}

