package de.volzo.miscreen.arbitraryBoundingBox;

import android.graphics.Point;

import org.jblas.DoubleMatrix;

/**
 * Created by Johannes on 11.07.2016.
 */
public class MIPoint2D implements Comparable<MIPoint2D> {


    private int x, y;

    public MIPoint2D() {
    }

    public MIPoint2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int compareTo(MIPoint2D p) {
        if (this.x == p.x) {
            return this.y - p.y;
        } else {
            return this.x - p.x;
        }
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {

        return x;
    }

    public int getY() {
        return y;
    }

    public DoubleMatrix toDoubleMatrix() {
        return new DoubleMatrix(2, 1, this.getX(), this.getY());
    }

    public static DoubleMatrix toDoubleMatrix(MIPoint2D[] points) {
        DoubleMatrix mat = new DoubleMatrix(0, 2);
        for (int i = 0; i < points.length; ++i) {
            mat = DoubleMatrix.concatHorizontally(mat, points[i].toDoubleMatrix());
        }
        return mat;
    }
}
