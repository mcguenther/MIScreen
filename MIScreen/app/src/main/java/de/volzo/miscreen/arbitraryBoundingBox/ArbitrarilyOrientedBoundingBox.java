package de.volzo.miscreen.arbitraryBoundingBox;

import android.support.annotation.NonNull;

import org.ejml.simple.SimpleMatrix;


/**
 * Created by Johannes on 11.07.2016.
 */
public class ArbitrarilyOrientedBoundingBox extends BoundingBox {
    private double rot;


    public MIPoint2D[] getRealWorldPoints() {
        // calc upper left and lower right corner
        SimpleMatrix pointMatrix = new SimpleMatrix(2, 2, false,
                minX, maxY,
                maxX, minY);
        pointMatrix = rotatePoints(pointMatrix, this.getRot());

        MIPoint2D[] points = new MIPoint2D[2];
        points[0] = new MIPoint2D((int) Math.round(pointMatrix.get(0, 0)), (int) Math.round(pointMatrix.get(1, 0)));
        points[1] = new MIPoint2D((int) Math.round(pointMatrix.get(0, 1)), (int) Math.round(pointMatrix.get(1, 1)));

        return points;
    }


    /**
     * Describes how an axis aligned bounding box would need to be
     * rotated to be parallel to this oriented bounding box
     */
    public double getRot() {
        return rot;
    }

    // using rotating calipers approach as proposed by Toussaint, G. T (1983)
    public ArbitrarilyOrientedBoundingBox(MIPoint2D[] displayCornerPoints) {
        super();
        MIPoint2D[] cvHull = ConvexHull.convex_hull(displayCornerPoints);
        if (cvHull != null) {

            SimpleMatrix pointMatrix = MIPoint2D.toSimpleMatrix(cvHull);

            // first determining edges
            Edge2D[] edges = getEdgesFromPoints(cvHull);

            // then check bounding boxes that share a border with one of the edges
            Boolean isFirstMinimum = true;
            BoundingBox bestFittingBB = null;

            Double bestFittingRot = 0d;
            double minArea = 0;
            for (int i = 0; i < edges.length; ++i) {
                Edge2D curEdge = edges[i];
                Double rot = curEdge.getAngle();
                SimpleMatrix rotatedHull = rotatePoints(pointMatrix, rot);

                BoundingBox bb = new BoundingBox(rotatedHull);
                double curArea = bb.getArea();
                if (curArea < minArea || isFirstMinimum) {
                    isFirstMinimum = false;
                    minArea = curArea;
                    bestFittingBB = bb;
                    bestFittingRot = -rot;
                }
            }

            this.rot = bestFittingRot;
            this.maxX = bestFittingBB.maxX;
            this.maxY = bestFittingBB.maxY;
            this.minX = bestFittingBB.minX;
            this.minY = bestFittingBB.minY;
        }
    }

    @NonNull
    private Edge2D[] getEdgesFromPoints(MIPoint2D[] cvHull) {
        Edge2D[] edges = new Edge2D[cvHull.length];
        for (int i = 0; i < cvHull.length; ++i) {
            edges[i] = new Edge2D(cvHull[i], cvHull[(i + 1) % cvHull.length]);
        }
        return edges;
    }

    @Override

    public SimpleMatrix getCenter() {
        SimpleMatrix centerPoint = super.getCenter();
        SimpleMatrix rotatedCenterPoint = rotatePoints(centerPoint, this.getRot());
        return rotatedCenterPoint;
    }
}
