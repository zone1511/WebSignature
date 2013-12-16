package models;

import java.util.List;
import java.util.ArrayList;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

public class Samples {

  private List<double[]> vectorList;

  // Instance parameter is here just to fix Java erasure issue.
  public Samples(List<double[]> vectorList, double[] instance) {
    // TODO Should clone instead
    this.vectorList = vectorList;
  }

  public Samples(List<ObservationVector> vectorList, ObservationVector instance) {
    this.vectorList = new ArrayList<double[]>(); 
    for(ObservationVector vector : vectorList) {
      this.vectorList.add(vector.values());
    }
  }

  public void setFromOrigin() {

    double[] initialPosition = {vectorList.get(0)[0], vectorList.get(0)[1]};

    for(double[] position : vectorList) {
      if(position[0] != -1) {
        position[0] -= initialPosition[0];
        position[1] -= initialPosition[1];
      }
    }
  }

  public double[] get(int startIndex, int offset) {

    int finalIndex = startIndex+offset;
    int size = vectorList.size();

    if (finalIndex < 0)
      return vectorList.get(0);
    if (finalIndex >= size)
      return vectorList.get(size-1);
    double[] sample = vectorList.get(finalIndex);
    if(sample[0] == -1)
      return vectorList.get(startIndex); //TODO Get the closest != [-1 -1]
    return sample;
  }

  public double[] get(int startIndex) {
    return this.get(startIndex, 0);
  }

  public int getSize() {
    return vectorList.size();
  }

  public List<ObservationVector> toObservationVectorList() {
    List<ObservationVector> toObservationVectorList = new ArrayList<ObservationVector>();
    for(double[] vector : vectorList) {
      toObservationVectorList.add(new ObservationVector(vector));
    }
    return toObservationVectorList;
  }
}
