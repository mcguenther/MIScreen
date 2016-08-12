package de.volzo.miscreen.arbitraryBoundingBox;

import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

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

    public BoundingBox() {
    }

    public BoundingBox(SimpleMatrix rotatedHull) {
        // assume that rotatedHull is 2xn, carrying n points
        SimpleMatrix xVals =  new SimpleMatrix(CommonOps.extractRow(rotatedHull.getMatrix(),0, null));
        this.minX = CommonOps.elementMin(xVals.getMatrix());
        this.maxX = CommonOps.elementMax(xVals.getMatrix());
        SimpleMatrix yVals = new SimpleMatrix(CommonOps.extractRow(rotatedHull.getMatrix(),1, null));
        this.minY = CommonOps.elementMin(yVals.getMatrix());
        this.maxY = CommonOps.elementMax(yVals.getMatrix());
    }

    public double getArea() {
        return Math.abs((maxX - minX) * (maxY - minY));
    }

    public SimpleMatrix getPoints() {
        // calc upper left and lower right corner
        SimpleMatrix pointMatrix = new SimpleMatrix(2, 2, false,
                minX, maxY,
                maxX, minY);
        return pointMatrix;
    }

    protected static SimpleMatrix rotatePoints(SimpleMatrix pointMatrix, Double rot) {
        // creating rotation matrix
        SimpleMatrix rotMatrix = new SimpleMatrix(2, 2, true,
                Math.cos(rot), -1 * Math.sin(rot), //first row
                Math.sin(rot), Math.cos(rot)); // second row

        // apply rotation (rotation centre doesn't matter for area computation
        SimpleMatrix rotatedPoints = rotMatrix.mult(pointMatrix);
        return rotatedPoints;
    }

    public double getWidth() {
        int maxWidth = -1;
        double xWidth = Math.abs(this.getMaxX() - this.getMinX());

        return xWidth;
    }
    public double getHeight() {
        int maxHeight = -1;
        double yWidth = Math.abs(this.getMaxY() - this.getMinY());

        return yWidth;
    }
    
    public SimpleMatrix getCenter() {
        double meanX = (getMinX() + getMaxX()) / 2;
        double meanY = (getMinY() + getMaxY()) / 2;
        SimpleMatrix centerPoint = new SimpleMatrix(2, 1, true, meanX, meanY);
        return centerPoint;
    }
}
