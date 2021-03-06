package sebastialonso;


import java.util.Vector;

public class Learner {
    private Double[][] transitionMatrix;
    private Double[][] emissionMatrix;
    private Double[] initialState;
    private Vector<Integer> observationsVector;
    private int numberOfStates;
    private int numberOfObservations;
    private int numberOfSymbols;
    private  double DELTA = 1e-7;
    private Double evaluationLogProb = Double.NEGATIVE_INFINITY;
    private boolean hasConverged = false;
    private Double[] gammaTMinusTwo;


    public Learner(Double[][] transition, Double[][] emission, Double[] initial, Vector<Integer> observations){
        this.transitionMatrix = transition;
        this.emissionMatrix = emission;
        this.initialState = initial;
        this.observationsVector = observations;
        this.numberOfStates = transition.length;
        this.numberOfObservations = observations.size();
        this.numberOfSymbols = emission[0].length;
    }

    public void setObservationVector(Vector<Integer> observations){
        this.observationsVector = observations;
        this.numberOfObservations = observationsVector.size();
    }

    public Learner(Double[][] transition, Double[][] emission, Double[] initial){
        this.transitionMatrix = transition;
        this.emissionMatrix = emission;
        this.initialState = initial;
        this.numberOfStates = transition.length;
        this.numberOfSymbols = emission[0].length;
    }

    /**
     * Trains the model for a certain number of iterations, and then sets the instance's matrices has the estimated ones
     * @param iterations
     */
    public void learnReload(int iterations){

        Double[] initial = initialState;
        Double[][] transition = getTransitionMatrix();
        Double[][] emission = getEmissionMatrix();

        //System.err.println("Training");
        for (int iteration = 0; iteration < iterations; iteration++){
            //System.err.println("iter: " + iteration);
            Double[] estimatedInitial = new Double[numberOfStates];
            Double[][] estimatedTransition = new Double[numberOfStates][numberOfStates];
            Double[][] estimatedEmission = new Double[numberOfStates][numberOfSymbols];

            double[] scalingFactor = new double[numberOfObservations];
            double[][] alpha = new double[numberOfObservations][numberOfStates];
            double[][] beta = new double[numberOfObservations][numberOfStates];
            Double[][] createGamma= new Double[numberOfObservations][numberOfStates];
            double[][][] diGamma = new double[numberOfObservations][numberOfStates][numberOfStates];


            //Compute alpha[0]

            scalingFactor[0] = 0.0;

            for (int i = 0; i < numberOfStates; i++){
                alpha[0][i] = initial[i] * emission[i][observationsVector.get(0)];
                scalingFactor[0] += alpha[0][i];
            }

            //Scale alpha[0]
            scalingFactor[0] = 1/scalingFactor[0];
            for (int i = 0; i < numberOfStates; i++){
                alpha[0][i] *= scalingFactor[0];
            }

            //compute a_t(i)
            for (int t = 1; t< numberOfObservations; t++){
                scalingFactor[t] = 0.0;
                for (int i = 0; i < numberOfStates; i++){
                    alpha[t][i] = 0.0;
                    for (int j = 0; j < numberOfStates; j++){
                        alpha[t][i] += alpha[t-1][j] * transition[j][i];
                    }
                    alpha[t][i] *= emission[i][observationsVector.get(t)];
                    scalingFactor[t] += alpha[t][i];
                }
                //Scale a_t(i)
                scalingFactor[t] = 1/scalingFactor[t];
                for (int i = 0; i < numberOfStates; i++){
                    alpha[t][i] *= scalingFactor[t];
                }
            }

            ///The beta pass
            //Scale beta
            for (int i = 0; i < numberOfStates; i++){
                beta[numberOfObservations - 1][i] = scalingFactor[numberOfObservations-1];
            }

            //beta pass
            for (int t = numberOfObservations - 2; t >= 0; t--){
                for (int i = 0; i < numberOfStates; i++){
                    beta[t][i] = 0.0;
                    for (int j = 0; j < numberOfStates; j++){
                        beta[t][i] = beta[t][i] + transition[i][j] * emission[j][observationsVector.get(t+1)] * beta[t+1][j];
                    }
                    //scale beta_t
                    beta[t][i] *= scalingFactor[t];
                }
            }

            ///compute createGamma och diggama
            for (int t = 0; t < numberOfObservations - 1; t++){
                double denominator = 0.0;
                for (int i = 0; i < numberOfStates; i++){
                    for (int j = 0; j< numberOfStates; j++){
                        denominator += alpha[t][i] * transition[i][j] * emission[j][observationsVector.get(t+1)] * beta[t+1][j];
                    }
                }
                for (int i = 0; i < numberOfStates; i++){
                    createGamma[t][i] = 0.0;
                    for (int j = 0; j < numberOfStates; j++){
                        diGamma[t][i][j] = (alpha[t][i] * transition[i][j] * emission[j][observationsVector.get(t+1)] * beta[t+1][j])/ denominator;
                        createGamma[t][i] += diGamma[t][i][j];
                    }
                }
            }

            //Set gamma for posterior use
            this.gammaTMinusTwo = createGamma[numberOfObservations -2];


            ///Re-estimate model
            //Re.estimate pi
            for (int i = 0; i < numberOfStates; i++){
                estimatedInitial[i] = createGamma[0][i];
            }

            //Re-estimate A
            for (int i = 0; i < numberOfStates; i++){
                for (int j = 0; j < numberOfStates; j++){
                    double numerator = 0.0;
                    double denominator = 0.0;

                    for (int t = 0; t < numberOfObservations-1; t++){
                        numerator += diGamma[t][i][j];
                        denominator += createGamma[t][i];
                    }
                    estimatedTransition[i][j] =  numerator/denominator;
                }
            }

            //Re-estimate B
            for (int i = 0; i < numberOfStates; i++){
                for (int j = 0; j < numberOfSymbols; j++){
                    double numerator = 0.0;
                    double denominator = 0.0;
                    for (int t = 0; t < numberOfObservations -1; t++){
                        if (observationsVector.get(t) ==  j){
                            numerator += createGamma[t][i];
                        }
                        denominator += createGamma[t][i];
                    }
                    estimatedEmission[i][j] = numerator/denominator;
                }
            }

            ///Compute log[P(O|lambda)]
            double logProb = 0.0;
            for (int t = 0; t < numberOfObservations; t++){
                logProb += Math.log(scalingFactor[t]);
            }
            logProb = -logProb;

            //Move the values
            transition = estimatedTransition;
            emission = estimatedEmission;
            initial = estimatedInitial;

            if (Math.abs(logProb - evaluationLogProb) < DELTA ){
                System.err.println("Convergence achieved...");
                this.hasConverged = true;
                break;

            }
            else {
                this.evaluationLogProb = logProb;
            }

        }

        this.transitionMatrix = transition;
        this.emissionMatrix = emission;
    }

    /* Cancer code down here
    public void learn(int iterations){

        Double currentProb = Double.NEGATIVE_INFINITY;

        //Initialize evaluator with matrices from the Learner
        Evaluator currentModel = new Evaluator(transitionMatrix, emissionMatrix, initialState, observationsVector);
        Double[][] alpha = currentModel.alphaPass();
        Double[][] beta = currentModel.betaPass();

        //Store here the final matrices
        System.err.println("Iteraciones en learn");
        System.err.println("numberOfObservations: "+ numberOfObservations);
        System.err.println("numberOfSymbols: "+ numberOfSymbols);

        for (int iter = 0; iter < iterations; iter++){
            System.err.println("iter: " + iter);
            System.out.println(iter);
            Double[][][] xi = currentModel.createXi(alpha, beta);
            Double[][] gamma = currentModel.createGamma(alpha, beta);


            Double[] estimatedInitial = estimatePi(gamma);
            Double[][] estimatedTransition = estimateTransition(xi, gamma);
            Double[][] estimatedEmission = estimateEmission(gamma);

            //Initialize the new evaluator with the estimated matrices
            Evaluator newModel = new Evaluator(estimatedTransition, estimatedEmission, estimatedInitial, observationsVector);

            //Calculate probability of seen observation sequence given Lambda
            Double newProb = newModel.evaluate();

            //System.out.println("oldProb :" + currentProb);
            //System.out.println("newProb :" + newProb);

            if (Math.abs(currentProb - newProb) < DELTA || iter == iterations - 1){
                this.transitionMatrix = estimatedTransition;
                this.emissionMatrix = estimatedEmission;
                this.initialState = estimatedInitial;
                break;
            }
            else {

                //Probabilty value for the next round is this round new probabilty
                currentProb = newProb;

                //Calculates alpha-beta pass for the new model
                alpha = newModel.alphaPass();
                beta = newModel.betaPass();

                //The old model is now the current one
                currentModel = newModel;
            }
        }
    }*/

    /**
     * Compute Pi, the initial probability vector
     * @param gamma
     * @return
     */
    private Double[] estimatePi(Double[][] gamma){
        Double[] pi = new Double[numberOfStates];
        for (int i = 0; i < numberOfStates; i++){
            pi[i]  = Extended.eexp(gamma[0][i]);
        }

        return pi;
    }

    /**
     * Computes the estimation for the transition matrix
     * @param xi
     * @param gamma
     * @return
     */
    private Double[][] estimateTransition(Double[][][] xi, Double[][] gamma){

        Double[][] transition = new Double[numberOfStates][numberOfStates];
        Double numerator = Double.NaN;
        Double denominator = Double.NaN;

        for (int t = 0; t < numberOfObservations - 1; t++){
            for (int i = 0; i < numberOfStates; i++){
                for (int j = 0; j < numberOfStates; j++){
                    numerator = Extended.esum(numerator, xi[t][i][j]);
                    denominator = Extended.esum(denominator, gamma[t][i]);
                }
            }

        }

        for (int i = 0; i < numberOfStates; i++){
            for (int j = 0; j < numberOfStates; j++){
                transition[i][j] = Extended.eexp(
                        Extended.eproduct(numerator, -denominator)
                );
            }
        }

        return transition;
    }

    /**
     * Computes the estimation for the emission matrix
     * @param gamma
     * @return
     */
    private Double[][] estimateEmission(Double[][] gamma){
        Double[][] emission = new Double[numberOfStates][numberOfSymbols];

        Double numerator = Double.NaN;
        Double denominator = Double.NaN;

        for (int j = 0; j < numberOfStates; j++){
            for (int k = 0; k < numberOfSymbols; k++){
                for (int t = 0; t < numberOfObservations; t++){
                    if (observationsVector.get(t) == k){
                        numerator = Extended.esum(numerator, gamma[t][j]);
                    }
                    denominator = Extended.esum(denominator, gamma[t][j]);
                }
                emission[j][k] = Extended.eexp(Extended.eproduct(numerator, - denominator));
            }
        }

        return emission;
    }

    public Double evaluation(){
        return this.evaluationLogProb;
    }


    public Double[] getGammaTMinusTwo() {
        return gammaTMinusTwo;
    }

    public Double[][] getTransitionMatrix() {
        return transitionMatrix;
    }

    public Double[][] getEmissionMatrix() {
        return emissionMatrix;
    }

    public boolean isHasConverged() {
        return hasConverged;
    }
}

