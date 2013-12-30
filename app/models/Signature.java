package models;

import java.util.List;

import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.EnumMapping;

import java.sql.Timestamp;

import javax.persistence.*;



@Entity
public class Signature extends Model {

  @Id
  public Long id;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  public User owner;

  public enum Type {
          TRAINING,
          VALIDATION,
          TEST,
          PRODUCTION
  }

  public Type type;

  public enum AcquisitionMethod {
    MOUSE,
    TOUCHSCREEN_FINGER,
    TOUCHSCREEN_STYLUS,
    TOUCHPAD_FINGER,
    TOUCHPAD_STYLUS,
    TABLET_STYLUS,
    TABLET_PEN,
    GESTURE,
    OTHER,
    UNKNOWN
  }

  public AcquisitionMethod acquisitionMethod;

  public enum DeviceType {
    SMARTPHONE,
    TABLET,
    PC,
    OTHER,
    UNKNOWN
  }

  public DeviceType deviceType;

  public String deviceName;

  public String deviceDetails;

  @Column(columnDefinition = "NUMERIC")
  public int deviceHeight;

  @Column(columnDefinition = "NUMERIC")
  public int deviceWidth;

  @CreatedTimestamp
  Timestamp cretime;

  @Version
  Timestamp updtime;

  @Column(columnDefinition = "TEXT")
  public Samples samples; 

  public Signature(
    List<double[]> vectorList,
    User owner,
    Type type,
    AcquisitionMethod acquisitionMethod,
    DeviceType deviceType,
    String deviceName,
    String deviceDetails,
    int deviceHeight,
    int deviceWidth
  ) {
    this.samples = new Samples(vectorList, vectorList.get(0));
    this.owner = owner;
    this.type = type;
    this.acquisitionMethod = acquisitionMethod;
    this.deviceType = deviceType;
    this.deviceName = deviceName;
    this.deviceDetails = deviceDetails;
    this.deviceHeight = deviceHeight;
    this.deviceWidth = deviceWidth;
  }

  public Features extractFeatures() {
    return new Features(samples);
  }

  
  public static Finder<Long,Signature> find = new Finder<Long,Signature>(
    Long.class, Signature.class
  );

  public static List<Signature> all() {
    return find.all();
  }

  public static void create(Signature signature) {
    signature.save();
  }

  public static void delete(Long id) {
    find.ref(id).delete();
  }
}
