package models;

import java.util.List;

public class Samples {

  private List<double[]> vectorList;

  public Samples(List<double[]> vectorList) {
    this.vectorList = vectorList;
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

}