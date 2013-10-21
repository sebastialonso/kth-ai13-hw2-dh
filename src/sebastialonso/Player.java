package sebastialonso;

import java.util.Random;

class Player {
    private int numberOfIterations = 0;
    private Learner[] shooter;
    // /constructor

	// /There is no data in the beginning, so not much should be done here.
	public Player() {
	}

	/**
	 * Shoot!
	 * 
	 * This is the function where you start your work.
	 * 
	 * You will receive a variable pState, which contains information about all
	 * birds, both dead and alive. Each birds contains all past actions.
	 * 
	 * The state also contains the scores for all players and the number of
	 * time steps elapsed since the last time this function was called.
	 * 
	 * @param pState the GameState object with observations etc
	 * @param pDue time before which we must have returned
	 * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
	 */
	public Action shoot(GameState pState, Deadline pDue) {
        //First round: create the learners and model

        if (this.numberOfIterations == 1) {

            shooter = new Learner[pState.getNumBirds()];
            Double[][] transition = new Double[5][5];
            Double[][] emission = new Double[5][8];
            Double[] initial = new Double[5];
            Random random = new Random();

            //Initialize randomly
            //Transition
            for (int i = 0; i < 5; i++){
                Double ranDelta = 1.0/ 5.0 - Math.abs(random.nextGaussian() * 0.0001);
                for (int j = 0; j < 5; j++){
                    transition[i][j] = ranDelta;
                    ranDelta -= Math.abs(random.nextGaussian() * 0.0001);
                }
            }

            //Emission
            for (int i = 0; i < 5; i++){
                Double ranDelta = 1.0/8.0;
                for (int j = 0; j <  8; j++){
                    emission[i][j] = ranDelta;
                    ranDelta -= Math.abs(random.nextGaussian() * 0.0001);
                }
            }

            //Initial
            for (int i = 0; i < 5; i++){
                Double ranDelta = 1.0/5.0;
                initial[i] = ranDelta;
                ranDelta -= Math.abs(random.nextGaussian() * 0.001);
            }

            //for (int i=0; i < shooter.length; i++){
            //    shooter[i] = new Learner(transition, emission, initial);
            //}
        } else{

            //We have enough observations to start the training
            /*if (this.numberOfIterations == 50){

                for (int i=0; i < pState.getNumBirds(); i++){

                }

            }*/
        }
        this.numberOfIterations++;
		/*
		 * Here you should write your clever algorithms to get the best action.
		 * This skeleton never shoots.
		 */
        System.err.println(pState.getNumBirds());

		// This line choose not to shoot
		//return cDontShoot;

		// This line would predict that bird 0 will move right and shoot at it
		return new Action(0, Constants.MOVE_RIGHT);
	}

	/**
	 * Guess the species!
	 * This function will be called at the end of each round, to give you
	 * a chance to identify the species of the birds for extra points.
	 * 
	 * Fill the vector with guesses for the all birds.
	 * Use SPECIES_UNKNOWN to avoid guessing.
	 * 
	 * @param pState the GameState object with observations etc
	 * @param pDue time before which we must have returned
	 * @return a vector with guesses for all the birds
	 */
	public int[] guess(GameState pState, Deadline pDue) {
		/*
		 * Here you should write your clever algorithms to guess the species of
		 * each bird. This skeleton makes no guesses, better safe than sorry!
		 */

		int[] lGuess = new int[pState.getNumBirds()];
		for (int i = 0; i < pState.getNumBirds(); ++i)
			lGuess[i] = Constants.SPECIES_UNKNOWN;
		return lGuess;
	}

	/**
	 * If you hit the bird you were trying to shoot, you will be notified
	 * through this function.
	 * 
	 * @param pState the GameState object with observations etc
	 * @param pBird the bird you hit
	 * @param pDue time before which we must have returned
	 */
	public void hit(GameState pState, int pBird, Deadline pDue) {
		System.err.println("HIT BIRD!!!");
	}

	/**
	 * If you made any guesses, you will find out the true species of those
	 * birds through this function.
	 * 
	 * @param pState the GameState object with observations etc
	 * @param pSpecies the vector with species
	 * @param pDue time before which we must have returned
	 */
	public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
	}

	public static final Action cDontShoot = new Action(-1, -1);
}
