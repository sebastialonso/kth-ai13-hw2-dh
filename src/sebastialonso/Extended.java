package sebastialonso;

public class Extended {

    public static Double eexp(Double value){
        if (value.isNaN()) return 0.0;
        else return Math.exp(value);

    }

    public static Double eln(Double value){
        if (value.equals(0.0)) return Double.NaN;
        else{
            if (value > 0.0) return Math.log(value);
            else {
                throw new RuntimeException("Negative input error");
            }
        }
    }

    public static Double esum(Double value, Double otherValue){
        if (value.isNaN() || otherValue.isNaN()){
            if (value.isNaN()){
                return otherValue;
            }
            else return value;

        }
        else {
            if (value > otherValue){
                return value + eln(1.0 + eexp(otherValue - value));
            }else {
                return otherValue + eln(1.0 + eexp(value - otherValue));
            }

        }
    }

    public static double eproduct(Double value, Double otherValue){
        if (value.isNaN() || value.isNaN()) return Double.NaN;
        else return value + otherValue;
    }
}
