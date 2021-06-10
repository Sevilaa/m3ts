package ch.m3ts.detection;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import ch.m3ts.util.Table;
import edu.princeton.cs.algs4.LinearRegression;

/**
 * Implementation with Linear Regression as prediction method.
 */
public class LinearBallCurvePredictor implements BallCurvePredictor {
    LinearRegression linearRegression;

    @Override
    public boolean willBallMoveIntoNet(int[] cx, int[] cy, Table table) {
        linearRegression = new LinearRegression(Doubles.toArray(Ints.asList(cx)), Doubles.toArray(Ints.asList(cy)));
        double predictionY = linearRegression.predict(table.getNetBottom().x);
        return (predictionY > table.getNetTop().y && predictionY < table.getNetBottom().y);
    }
}