package de.volzo.miscreen.arbitraryBoundingBox;

import android.graphics.Point;

import java.util.Arrays;


// taken from http://www.algorithmist.com/index.php/Monotone_Chain_Convex_Hull.java
public class ConvexHull {

    public static long cross(MIPoint2D O, MIPoint2D A, MIPoint2D B) {
        return (A.getX() - O.getX()) * (B.getY() - O.getY()) - (A.getY() - O.getY()) * (B.getX() - O.getX());
    }

    public static MIPoint2D[] convex_hull(MIPoint2D[] points) {

        int n = points.length;
        Arrays.sort(points);
        MIPoint2D[] ans = new MIPoint2D[2 * n];                // In between we may have a 2n points
        int k = 0;
        int start = 0;                    // start is the first insertion point


        for (int i = 0; i < n; i++)                     // Finding lower layer of hull
        {
            MIPoint2D p = points[i];
            while (k - start >= 2 && p.sub(ans[k - 1]).cross(p.sub(ans[k - 2])) > 0)
                k--;
            ans[k++] = p;
        }

        k--;                        // drop off last point from lower layer
        start = k;

        for (int i = n - 1; i >= 0; i--)                // Finding top layer from hull
        {
            MIPoint2D p = points[i];
            while (k - start >= 2 && p.sub(ans[k - 1]).cross(p.sub(ans[k - 2])) > 0)
                k--;
            ans[k++] = p;
        }
        k--;                        // drop off last point from top layer

        return Arrays.copyOf(ans, k);                   // convex hull is of size k

    }

}