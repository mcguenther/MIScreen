package de.volzo.miscreen.arbitraryBoundingBox;

import android.support.annotation.NonNull;

import org.jblas.DoubleMatrix;

/**
 * Created by Johannes on 11.07.2016.
 */
public class ArbitrarilyOrientedBoundingBox extends BoundingBox {
    private double rot;


    public DoubleMatrix getRealWorldPoints() {
        // calc upper left and lower right corner
        DoubleMatrix pointMatrix = new DoubleMatrix(2, 2,
                minX, maxY,
                maxX, minY);
        pointMatrix = rotatePoints(pointMatrix, rot);
        return pointMatrix;
    }

    // private Edge2D[] edges;

    public double getRot() {
        return rot;
    }

    // using rotating calipers approach as proposed by Toussaint, G. T (1983)
    public ArbitrarilyOrientedBoundingBox(MIPoint2D[] displayCornerPoints) {
        super();
        MIPoint2D[] cvHull = ConvexHull.convex_hull(displayCornerPoints);
        if (cvHull != null) {

            DoubleMatrix pointMatrix = MIPoint2D.toDoubleMatrix(cvHull);

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
                DoubleMatrix rotatedHull = rotatePoints(pointMatrix, rot);

                BoundingBox bb = new BoundingBox(rotatedHull);
                double curArea = bb.getArea();
                if (curArea < minArea || isFirstMinimum) {
                    isFirstMinimum = false;
                    minArea = curArea;
                    bestFittingBB = bb;
                    bestFittingRot = rot;
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

}
