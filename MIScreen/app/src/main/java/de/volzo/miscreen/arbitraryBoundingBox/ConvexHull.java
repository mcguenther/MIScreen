package de.volzo.miscreen.arbitraryBoundingBox;

import java.util.Arrays;


// taken from https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
public class ConvexHull {

    public static long cross(MIPoint2D O, MIPoint2D A, MIPoint2D B) {
        return (A.getX() - O.getX()) * (B.getY() - O.getY()) - (A.getY() - O.getY()) * (B.getX() - O.getX());
    }

    public static MIPoint2D[] convex_hull(MIPoint2D[] P) {

        if (P.length > 1) {
            int n = P.length, k = 0;
            MIPoint2D[] H = new MIPoint2D[2 * n];

            Arrays.sort(P);

            // Build lower hull
            for (int i = 0; i < n; ++i) {
                while (k >= 2 && cross(H[k - 2], H[k - 1], P[i]) <= 0)
                    k--;
                H[k++] = P[i];
            }

            // Build upper hull
            for (int i = n - 2, t = k + 1; i >= 0; i--) {
                while (k >= t && cross(H[k - 2], H[k - 1], P[i]) <= 0)
                    k--;
                H[k++] = P[i];
            }
            if (k > 1) {
                H = Arrays.copyOfRange(H, 0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
            }
            return H;
        } else if (P.length <= 1) {
            return P;
        } else {
            return null;
        }
    }

}