package sebastialonso;

public class Helpers {

    /**
     * Builds a matrix out of a string with matrix information
     * @param matrixLine The String that contains number of rows, number of columns and elements
     * @return A double[][] as a matrix
     */
    public static Double[][] buildMatrix(String matrixLine){
        String[] matrixContent = matrixLine.split(" ");
        Double[][] matrix = new Double[Integer.parseInt(matrixContent[0])][Integer.parseInt(matrixContent[1])];
        int colIndex = 0;
        int rowIndex = 0;
        for (int i=2; i < matrixContent.length; i++){
            if (rowIndex == 0 || rowIndex%(Integer.parseInt(matrixContent[0])) != 0){
                matrix[rowIndex][colIndex] = Double.parseDouble(matrixContent[i]);
                colIndex++;
                if (colIndex != 0 && colIndex%(Integer.parseInt(matrixContent[1])) == 0){
                    rowIndex++;
                    colIndex = 0;
                }
            }
        }
        return matrix;
    }

    /**
     * Buils a vector with the observations
     * @param vectorLine The String that contains the number of observations and the observations
     * @return String[] that contains the observations as String elements
     */
    public static String[] buildObservationVector(String vectorLine){
        String[] vectorContent = vectorLine.split(" ");
        String[] observations = new String[Integer.parseInt(vectorContent[0])];

        for (int i=1; i< vectorContent.length; i++){
            observations[i-1] = vectorContent[i];
        }
        return observations;
    }

    /**
     * Buils a initial state vector for the HMM
     * @param vectorLine The String that contains the number of rows, columns and the probability distribution
     * @return A double[] with the initial probability distribution
     */
    public static Double[] buildVector(String vectorLine){
        String[] vectorContent = vectorLine.split(" ");
        Double[] vector = new Double[Integer.parseInt(vectorContent[1])];
        for (int i=2; i< vectorContent.length; i++ ){
            vector[i-2] = Double.parseDouble(vectorContent[i]);
        }
        return vector;
    }

    public static String matrixToString(Double[][] mat){
        String st= mat.length + " " + mat[0].length +" ";
        for (Double[] row : mat){
            for (Double element : row){
                st += element + " ";
            }
        }

        return st;
    }

    public static String vectorToString(Double[] vector){
        String st = "";
        for (Double element : vector){
            st += element + " ";
        }
        return st;
    }

    public static String printMatrixes(String[] matrixes){
        String st = "";
        for (String mat : matrixes){
            st += mat + "\n";
        }

        return st;
    }

    public static Double sumElements(Double[] row){
        Double value = 0.0;
        for (Double number : row){
            value += number;
        }
        return value;
    }
    public static Double[] vectorTimesMatrix(Double[] vector, Double[][] matrix){
        //System.err.println(vector.length + " == " + matrix.length);
            Double[] result = new Double[matrix[0].length];
            for (int colIndex = 0; colIndex < vector.length; colIndex++){
                Double auxValue = 0.0;
                for (int rowIndex = 0; rowIndex < vector.length; rowIndex++){
                    auxValue += vector[rowIndex] * matrix[rowIndex][colIndex];
                }
                result[colIndex] = auxValue;
            }
            return result;
    }

    /**
     * Here it chosen where to shoot. If the most likely observation probability is less than 0.75,
     * we don't take the shot.
     * @param gammaTMinusTwo
     * @param transition
     * @param emission
     * @return
     */
    public static Integer whereToShoot(Double[] gammaTMinusTwo, Double[][] transition, Double[][] emission){
        Double[] gammaTMinusOne = vectorTimesMatrix(gammaTMinusTwo, transition);
        gammaTMinusOne = vectorTimesMatrix(gammaTMinusOne, transition);

        Double[] mostLikely = new Double[emission[0].length];
        //System.err.println(mostLikely.length);
        for (int colIndex=0; colIndex < mostLikely.length; colIndex++){
            Double auxValue = 0.0;
            for (int rowIndex = 0; rowIndex < emission.length; rowIndex++){
                auxValue += gammaTMinusOne[rowIndex] * emission[rowIndex][colIndex];
            }
            mostLikely[colIndex] = auxValue;
        }

        Double maxProbability = 0.0;
        Integer arg = -1;

        for (int i = 0; i < mostLikely.length; i++){
            if (mostLikely[i] > maxProbability && mostLikely[i] >= 0.70){
                maxProbability = mostLikely[i];
                arg = i;
            }
        }
        System.err.println("most likely sequence probability: " + maxProbability);
        return arg;
    }

}
