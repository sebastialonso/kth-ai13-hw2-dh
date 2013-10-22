package sebastialonso;

import java.util.Vector;

public class Evaluator {

    private Double[][] transitionMatrix;
    private Double[][] emissionMatrix;
    private Double[] initialVector;
    private Vector<Integer> observationsVector;
    private int numberOfStates;
    private int numberOfObservations;


    public Evaluator(Double[][] transition, Double[][] emission, Double[] initial, Vector<Integer> observations){
        this.transitionMatrix = transition;
        this.emissionMatrix = emission;
        this.initialVector = initial;
        this.observationsVector = observations;
        this.numberOfStates = transitionMatrix.length;
        this.numberOfObservations = observationsVector.size();
    }

    /**
     * Solves Problem 2: returns the probability for an observation sequence given the Model
     * @return
     */
    public Double evaluate(){
        return sumElements(alphaPass()[numberOfObservations -1]);
    }

    /**
     * Used to solve Problem 2: Evaluation.
     * @return
     */
    public Double[][] alphaPass(){
        Double[][] alpha = new Double[numberOfObservations][numberOfStates];

        for (int i=0; i < numberOfStates; i++){
            alpha[0][i] = Extended.eproduct(
                    Extended.eln(initialVector[i]),
                    Extended.eln(emissionMatrix[i][observationsVector.get(0)]));

        }

        //compute a_t(i)
        for (int t = 1; t< numberOfObservations; t++){
            for (int i=0; i < numberOfStates; i++){
                Double val = Double.NaN;
                for (int j=0; j < numberOfStates; j++){
                    val = Extended.esum(val, Extended.eproduct(alpha[t - 1][j], Extended.eln(transitionMatrix[j][i])));
                }
                alpha[t][i] = Extended.eproduct(val, Extended.eln(emissionMatrix[i][observationsVector.get(t)]));

            }
        }

        return alpha;
    }

    /**
     * Performs the beta-pass algorithm
     * @return A Vector<Vector<Double>> with the rows being each beta_t
     */
   public Double[][] betaPass(){
        Double[][] betaMatrix = new Double[numberOfObservations][numberOfStates];

        for (int i=0; i < numberOfStates; i++){
            betaMatrix[numberOfObservations - 1][i] = 0.0;
        }

        for (int t = numberOfObservations-2; t >= 0; t--){
            for (int i=0; i < numberOfStates; i++){
                Double value = 0.0;
                for (int j=0; j < numberOfStates; j++){
                    value += Extended.esum( value,Extended.eproduct(
                                    Extended.eln(transitionMatrix[i][j]),
                                                 Extended.eproduct(
                                                    emissionMatrix[j][observationsVector.get(t+1)],
                                                    Extended.eln(betaMatrix[t+1][j]))));
                }
                betaMatrix[t][i] = value;
            }
        }
        return betaMatrix;
   }

    /**
     * Computes the log space Xi Matrix
     * @param alpha
     * @param beta
     * @return
     */

    public Double[][][] createXi(Double[][] alpha, Double[][] beta){
        Double[][][] xiMatrix = new Double[numberOfObservations][numberOfStates][numberOfStates];

        for (int t = 0; t < numberOfObservations - 1; t++){
            Double normalize = Double.NaN;
            for (int i = 0; i < numberOfStates; i++){
                for (int j = 0; j < numberOfStates; j++){
                    xiMatrix[t][i][j] = Extended.eproduct(
                            alpha[t][i],
                            Extended.eproduct(
                                    Extended.eln(transitionMatrix[i][j]),
                                    Extended.eproduct(
                                            Extended.eln(emissionMatrix[j][observationsVector.get(t+1)]),
                                            beta[t+1][j]
                                    )
                            )
                    );
                    normalize = Extended.esum(normalize, xiMatrix[t][i][j]);
                }
            }

            for (int i = 0; i < numberOfStates; i++){
                for (int j = 0; j < numberOfStates; j++){
                    xiMatrix[t][i][j] = Extended.eproduct(xiMatrix[t][i][j], -normalize);
                }
            }
        }

        return xiMatrix;
    }

    /**
     * Computes the log space Gamma Matrix
     * @param alpha
     * @param beta
     * @return
     */
    public Double[][] createGamma(Double[][] alpha, Double[][] beta){

        Double[][] gamma = new Double[numberOfObservations][numberOfStates];
        for (int t = 0; t < numberOfObservations; t++){
            Double normalizer = Double.NaN;
            for (int i = 0; i < numberOfStates; i++){
                gamma[t][i] = Extended.eproduct(alpha[t][i], beta[t][i]);
                normalizer = Extended.esum(normalizer, gamma[t][i]);
            }
            for (int i = 1; i < numberOfStates; i++){
                gamma[t][i] = Extended.eproduct(gamma[t][i], - normalizer);
            }
        }

        return gamma;
    }

    /**
     * Sums the element of a vector
     * @param vector Vector<Double> on which the internal esum is desired
     * @return Double esum of the elements of the vector
     */
    public Double sumElements(Double[] vector){
        Double response = 0.0;
        for (int i=0; i< numberOfStates; i++){
            response = Extended.esum(response, vector[i]);
        }
        return Extended.eexp(response)-1;
    }
}


