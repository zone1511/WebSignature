package models;

import play.api.*;
import java.util.List;
import be.ac.ulg.montefiore.run.jahmm.*;

import java.lang.Math;

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

  @Column(columnDefinition = "TEXT")
  public HiddenMarkovModel gaussianMixtureModel;

  @Column(columnDefinition = "NUMERIC")
  public double averageTrainingScoreLocal;

  @Column(columnDefinition = "NUMERIC")
  public double averageTrainingScoreGlobal;

  @Column(columnDefinition = "TEXT")
  public ObservationVector meanLocal;

  @Column(columnDefinition = "TEXT")
  public ObservationVector stdLocal;

  @Column(columnDefinition = "TEXT")
  public ObservationVector meanGlobal;

  @Column(columnDefinition = "TEXT")
  public ObservationVector stdGlobal;

  @Column(columnDefinition = "NUMERIC")
  public final int nbFeaturesLocal = 5;
  
  @Column(columnDefinition = "NUMERIC")
  public final int nbStatesLocal = 4;
  
  @Column(columnDefinition = "NUMERIC")
  public final int nbGaussiansLocal = 3;

  @Column(columnDefinition = "NUMERIC")
  public final int nbFeaturesGlobal = 5;
  
  @Column(columnDefinition = "NUMERIC")
  public final int nbGaussiansGlobal = 2;

  @CreatedTimestamp
  Timestamp cretime;

  @Version
  Timestamp updtime;

  public boolean train(TrainingSet signatures) {

    meanLocal = new ObservationVector(signatures.meanLocalVector());
    stdLocal = new ObservationVector(signatures.stdLocalVector());

    signatures.normalizeLocalFeatures();

    List<List<ObservationVector>> trainingVectorsLocal = signatures.toLocalObservationVectorLists();

    hiddenMarkovModel = new HiddenMarkovModel(trainingVectorsLocal, nbGaussiansLocal, nbFeaturesLocal, nbStatesLocal);

    hiddenMarkovModel.train(trainingVectorsLocal);

    meanGlobal = new ObservationVector(signatures.meanGlobalVector());
    stdGlobal = new ObservationVector(signatures.stdGlobalVector());

    signatures.normalizeGlobalFeatures();

    List<List<ObservationVector>> trainingVectorsGlobal = signatures.toGlobalObservationVectorLists();

    gaussianMixtureModel = new HiddenMarkovModel(trainingVectorsGlobal, nbGaussiansGlobal, nbFeaturesGlobal, 1);

    gaussianMixtureModel.train(trainingVectorsGlobal);

    computeAverageTrainingScore(signatures);

    return true;
  }

  public double probability(Features signature) {

    signature.normalizeLocalFeatures(meanLocal.values(), stdLocal.values());

    double probabilityLocal = hiddenMarkovModel.probability(signature.toLocalObservationVectorList());
    double scoreLocal = averageTrainingScoreLocal/probabilityLocal;

    signature.normalizeGlobalFeatures(meanGlobal.values(), stdGlobal.values());

    double probabilityGlobal = gaussianMixtureModel.probability(signature.toGlobalObservationVectorList());
    double scoreGlobal = averageTrainingScoreGlobal/probabilityGlobal;

    System.out.println("Score local : "+scoreLocal);
    System.out.println("Score global : "+scoreGlobal+" p: "+probabilityGlobal);
    
    return bound(scoreLocal)*0.6+bound(scoreGlobal)*0.4;
  }

  private double bound(double score) {
    return Math.min(1.,Math.max(0.,score));
  }

  private void computeAverageTrainingScore(TrainingSet normTrainingSet) {

    for(List<ObservationVector> trainingSignature : normTrainingSet.toLocalObservationVectorLists()) {
      averageTrainingScoreLocal += hiddenMarkovModel.probability(trainingSignature);
    }

    averageTrainingScoreLocal /= normTrainingSet.getSize();

    for(List<ObservationVector> trainingSignature : normTrainingSet.toGlobalObservationVectorLists()) {
      averageTrainingScoreGlobal += gaussianMixtureModel.probability(trainingSignature);
    }

    averageTrainingScoreGlobal /= normTrainingSet.getSize();

    System.out.println("Average training score local : "+averageTrainingScoreLocal);
    System.out.println("Average training score global : "+averageTrainingScoreGlobal);

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

