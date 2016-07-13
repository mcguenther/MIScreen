package de.volzo.miscreen;

import java.util.List;

import de.volzo.miscreen.arbitraryBoundingBox.ArbitrarilyOrientedBoundingBox;

/**
 * Created by Johannes on 01.06.2016.
 */
public class Host {

    private static Host mHost = null;
    private Host() {}

    public static Host getInstance() {
        if(mHost == null) {
            mHost = new Host();
        }
        return mHost;
    }

    public static Boolean exists() {
        return mHost != null;
    }

    public static void destroy() {
        if(mHost != null) {
            mHost = null;
        }
    }

    // input: transformation matrices
    //TODO check if matrices have the correct format
    private static ArbitrarilyOrientedBoundingBox getAOBB(float[] hostF, List<float[]> fList) {

        // TODO: calc transformation matrices relative to fMatrix of host (hostF)
        // TODO: calc 2D points from fMatrices
        // TODO: call constructor of AOBB with 2D points




        return null;
    }
}
