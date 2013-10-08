import java.util.Vector;

public class HiddenMarkovModel {

  private final static int N = 3; // Number of Hidden States
  private final static int M = 9; // Number of possible sampled values
  private final static int F = 2; // Number of features 

  private final static int maxIters = 40;
  // 12 et 200 iter

  private int T;

  private double[] pi = { 0.33, 0.32, 0.35 };
  private double[][] a = { // Hidden state to hidden state transition probab
    { 0.8, 0.11, 0.09 }, 
    { 0.11, 0.8, 0.09 },
    { 0.11, 0.09, 0.8 }
  }; // TODO : Init procedurally in a right/left way

  private double[][] b = new double[N][M][F]; //probab of emission of symbol given hidden state
  // should be 3d

  private int[] o;
  private double[] c;

  private int iters;
  private double oldLogProb;

  private double[][][] alpha;
  private double[][][] beta;

  public HiddenMarkovModel() {
    initMatrix(b);
    o = new int[T]; // init with observation here
    oldLogProb = Double.NEGATIVE_INFINITY;
    iters = 0;
    c = new double[T];

  }

  private void initMatrix(double[][] matrix) {
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[0].length; j++) {
        matrix[i][j] = Math.random();
      }
    }
    for (int i = 0; i < matrix.length; i++) {
      double sum = 0;
      for (int j = 0; j < matrix[0].length; j++) {
        sum += matrix[i][j];
      }
      for (int j = 0; j < matrix[0].length; j++) {
        matrix[i][j] /= sum;
      }
    }
  }

  private void baulmWelch() {
    do {
      alpha = alphaPass();
      beta = betaPass();
      double[][] gamma = new double[N][T];
      double[][][] gammaIJ = new double[N][N][T];
      computeGamma(gamma, gammaIJ);
      reEstimatePi(gamma);
      reEstimateA(gamma, gammaIJ);
      reEstimateB(gamma);
    } while (toIterateOrNotToIterateThatIsTheQuestion(computeLogProbOfOGivenLamba()));
  }

  private double[][] alphaPass() {
    double[][] alpha = new double[N][T];
    c[0] = 0;
    for (int i = 0; i < N; i++) {
      alpha[i][0] = pi[i] * b[i][o[0]];
      c[0] += alpha[i][0];
    }
    if (c[0] != 0)
      c[0] = 1. / c[0];
    for (int i = 0; i < N; i++) {
      alpha[i][0] *= c[0];
    }
    for (int t = 1; t < T; t++) {
      c[t] = 0;
      for (int i = 0; i < N; i++) {
        alpha[i][t] = 0;
        for (int j = 0; j < N; j++) {
          alpha[i][t] += (alpha[j][t - 1] * a[j][i]);
        }
        alpha[i][t] *= b[i][o[t]];
        c[t] += alpha[i][t];
      }
      if (c[t] != 0)
        c[t] = (1. / c[t]);
      for (int i = 0; i < N; i++) {
        alpha[i][t] *= c[t];
      }
    }
    return alpha;
  }

  private double[][] betaPass() {
    double[][] beta = new double[N][T];
    for (int i = 0; i < N; i++) {
      beta[i][T - 1] = c[T - 1];
    }
    for (int t = T - 2; t >= 0; t--) {
      for (int i = 0; i < N; i++) {
        beta[i][t] = 0;
        for (int j = 0; j < N; j++) {
          beta[i][t] += (a[i][j] * b[j][o[t + 1]] * beta[j][t + 1]);
        }
        beta[i][t] *= c[t];
      }
    }
    return beta;
  }

  private void computeGamma(double[][] gamma, double[][][] gammaIJ) {
    for (int t = 0; t < T - 1; t++) {
      double denom = 0;
      for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
          denom += alpha[i][t] * a[i][j] * b[j][o[t + 1]]
              * beta[j][t + 1];
        }
      }
      for (int i = 0; i < N; i++) {
        gamma[i][t] = 0;
        for (int j = 0; j < N; j++) {
          gammaIJ[i][j][t] = (alpha[i][t] * a[i][j] * b[j][o[t + 1]] * beta[j][t + 1])
              / denom;
          gamma[i][t] += gammaIJ[i][j][t];
        }
      }
    }
  }

  private void reEstimatePi(double[][] gamma) {
    for (int i = 0; i < N; i++) {
      pi[i] = gamma[i][0];
    }
  }

  private void reEstimateA(double[][] gamma, double[][][] gammaIJ) {
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N; j++) {
        double numer = 0;
        double denom = 0;
        for (int t = 0; t < T - 1; t++) {
          numer += gammaIJ[i][j][t];
          denom += gamma[i][t];
        }
        a[i][j] = numer / denom;
      }
    }
  }

  private void reEstimateB(double[][] gamma) {
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < M; j++) {
        double numer = 0;
        double denom = 0;
        for (int t = 0; t < T - 1; t++) {
          if (o[t] == j)
            numer += gamma[i][t];
          denom += gamma[i][t];
        }
        b[i][j] = numer / denom;
      }
    }
  }

  private double computeLogProbOfOGivenLamba() {
    double logProb = 0;
    for (int i = 0; i < T; i++) {
      logProb += Math.log(c[i]);
    }
    logProb = -logProb;
    return logProb;
  }

  private boolean toIterateOrNotToIterateThatIsTheQuestion(double logProb) {
    iters++;
    if (iters < maxIters && logProb > oldLogProb) {
      oldLogProb = logProb;
      return true;
    } else
      return false;
  }
}