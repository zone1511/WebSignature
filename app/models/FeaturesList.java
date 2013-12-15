package models;

public class FeaturesList {

  private List<double[]> vectorList;

  private int dimension;

  private double[] means = null;

  private double[] stds = null;

  private boolean normalized = false;

  public FeaturesList(List<double[]> vectorList, int dimension) {
    this.vectorList = vectorList;
    this.dimension = dimension;
  }

  public FeaturesList(SamplesList samples) {
    vectorList = new ArrayList<double[]>();
    dimension = 5;

    for (int i=0; i<sample.getSize(); i++) {
      double[] sample = samples.get(i);

      sample.setFromOrigin();

      double[] features = new double[dimension];

      //Sample value [-1, -1, -1] is only here to indicate a discontinuity.
      if (sample[0] != -1) {
        features[0] = sample[0];
        features[1] = sample[1];
        features[2] = (samples.get(i,1)[0]-samples.get(i,-1)[0])/2.;
        features[3] = (samples.get(i,1)[1]-samples.get(i,-1)[1])/2.;
        features[4] = Math.sqrt(Math.pow(features[2],2)+Math.pow(features[3],2));
        vectorList.addVector(features);
      }
    }
  }

  public FeaturesList() {
    vectorList = new ArrayList();
  }

  public void addVector(int i) {
    vectorList.add(i);
  }

  public double[] getVector(int i) {
    return vectorList.get(i);
  }

  public int getSize() {
    return vectorList.size();
  }

  public int getDimension() {
    return dimension;
  }

  public void normalize(double[] meanVector, double[] stdVector) {

    assert !normalized : "Already normalized !";

    for(double[] vector : vectorList) {
      for(int i=0; i<dimension; i++) {
        vector[i] = (vector[i]-meanVector[i])/stdVector[i];
      }
    }

    normalized = true;
  }

  public List<ObservationVector> toObservationVectorList() {
    List<ObservationVector> observationVectorList = new ArrayList();

    for(double[] vector : vectorList) {
      ObservationVector observationVector = new ObservationVector(vector);
      observationVectorList.add(observationVector);
    }

    return observationVectorList;
  }

  public List<ObservationVector> getNormalizedList() {
    return getNormalizedList(meanVector(), stdVector());
  }

  public double[] meanVector() {

    if (means != null)
      return means;

    means = new double[dimension];
    Arrays.fill(means, 0);

    for(double[] vector : vectorList) {
      for(int d=0; d<dimension; d++) {
        means[d] += vector[d];
      }
    }

    for(int d=0; d<dimension; d++) {
      means[d] /= vectorList.size();
    }

    return means;
  }

  public double[] stdVector(double[] means) {

    if (stds != null)
      return stds;

    stds = new double[dimension];
    Arrays.fill(stds, 0);

    for(double[] vector : vectorList) {
      for(int d=0; d<dimension; d++) {
        stds[d] += Math.pow((vector[d]-means[d]),2);
      }
    }

    for(int d=0; d<dimension; d++) {
      stds[d] = Math.sqrt(stds[d]/vectorList.size());
    }

    return stds;
  }

  public double[] stdVector() {
    if (stds != null)
      return stds;
    else
      return std(meanVector());

  }

  public List<double[]> getVectorList() {
    return vectorList;
  }

}