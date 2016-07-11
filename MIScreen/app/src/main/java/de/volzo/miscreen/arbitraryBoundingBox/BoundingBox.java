package de.volzo.miscreen.arbitraryBoundingBox;

import org.jblas.DoubleMatrix;

/**
 * Created by Johannes on 11.07.2016.
 */
public class BoundingBox {
    protected double minX;
    protected double minY;
    protected double maxX;
    protected double maxY;

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public BoundingBox() {}

    public BoundingBox(DoubleMatrix rotatedHull) {
        // assume that rotatedHull is 2xn, carrying n points

        DoubleMatrix xVals = rotatedHull.getRow(0);
        this.minX = xVals.min();
        this.maxX = xVals.max();
        DoubleMatrix yVals = rotatedHull.getRow(1);
        this.minY = yVals.min();
        this.maxY = yVals.max();

    }

    public double getArea() {
        return Math.abs((maxX - minX) * (maxY - minY));
    }

    public DoubleMatrix getPoints() {
        // calc upper left and lower right corner
        DoubleMatrix pointMatrix = new DoubleMatrix(2, 2,
                minX, maxY,
                maxX, minY);
        return pointMatrix;
    }

    protected static DoubleMatrix rotatePoints(DoubleMatrix pointMatrix, Double rot) {
        // creating rotation matrix
        DoubleMatrix rotMatrix = new DoubleMatrix(2, 2,
                Math.cos(rot), -1 * Math.sin(rot), //first row
                Math.sin(rot), Math.cos(rot)); // second row

        // apply rotation (rotation centre doesn't matter for area computation
        return rotMatrix.mmul(pointMatrix);
    }
}
