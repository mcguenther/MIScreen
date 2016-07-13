package de.volzo.miscreen.arbitraryBoundingBox;

import android.graphics.Point;

import org.ejml.simple.SimpleMatrix;


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

    public SimpleMatrix toSimpleMatrix() {
        return new SimpleMatrix(2, 1, true, this.getX(), this.getY());
    }

    public static SimpleMatrix toSimpleMatrix(MIPoint2D[] points) {
        SimpleMatrix mat = new SimpleMatrix(2, 0);
        for (int i = 0; i < points.length; ++i) {
            mat = mat.combine(0, SimpleMatrix.END, points[i].toSimpleMatrix());
        }
        return mat;
    }

    // cross product of two vectors
    public int cross(MIPoint2D p) {
        return x * p.y - y * p.x;
    }

    // subtraction of two points
    public MIPoint2D sub(MIPoint2D p) {
        return new MIPoint2D(x - p.x, y - p.y);
    }
}
