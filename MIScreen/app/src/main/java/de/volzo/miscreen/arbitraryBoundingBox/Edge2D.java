package de.volzo.miscreen.arbitraryBoundingBox;

/**
 * Created by Johannes on 11.07.2016.
 */
public class Edge2D {
    private MIPoint2D start, end;

    public Edge2D(MIPoint2D start, MIPoint2D end) {
        this.start = start;
        this.end = end;
    }

    public MIPoint2D getStart() {
        return start;
    }

    public void setStart(MIPoint2D start) {
        this.start = start;
    }

    public MIPoint2D getEnd() {
        return end;
    }

    public void setEnd(MIPoint2D end) {
        this.end = end;
    }

    public double getAngle() {
        return Math.atan(end.getY() - start.getY()) / (end.getX() - end.getY());
    }

}
