package de.volzo.miscreen;

import android.util.Log;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by volzotan on 14.07.16.
 */
public class Support {

    String uuid = UUID.randomUUID().toString();

    private static final String TAG = Support.class.getName();

    private static Support support = null;

    private Support() {
    }

    public static Support getInstance() {
        if (support == null) {
            support = new Support();
        }
        return support;
    }

    public List<SimpleMatrix> getDeviceCornersTransformations() {
        Log.i(TAG, "device ID: " + android.os.Build.MODEL);

        SimpleMatrix ulCorner = new SimpleMatrix(4, 4, true);
        SimpleMatrix urCorner = new SimpleMatrix(4, 4, true);
        SimpleMatrix llCorner = new SimpleMatrix(4, 4, true);
        SimpleMatrix lrCorner = new SimpleMatrix(4, 4, true);

        switch(android.os.Build.MODEL) {
            case "XT1069": // Moto G2

                ulCorner.set(3, 7,  50);
                ulCorner.set(3, 7, -50);

                urCorner.set(3, 7, -10);
                urCorner.set(3, 7, -05);

                llCorner.set(3, 7,  50);
                llCorner.set(3, 7, -80);

                lrCorner.set(3, 7, -10);
                lrCorner.set(3, 7, -80);

                break;

            case "NEXUS 5":

                ulCorner.set(3, 7, -10);
                ulCorner.set(3, 7, -50);

                urCorner.set(3, 7,  50);
                urCorner.set(3, 7, -05);

                llCorner.set(3, 7, -10);
                llCorner.set(3, 7, -80);

                lrCorner.set(3, 7,  50);
                lrCorner.set(3, 7, -80);

                break;

            case "XT1506": // Moto E2

                // TODO

                ulCorner.set(3, 7, -10);
                ulCorner.set(3, 7, -50);

                urCorner.set(3, 7,  50);
                urCorner.set(3, 7, -05);

                llCorner.set(3, 7, -10);
                llCorner.set(3, 7, -80);

                lrCorner.set(3, 7,  50);
                lrCorner.set(3, 7, -80);

                break;
            default:
                Log.wtf(TAG, "No Device ID found! \n When in doubt, run in circles, scream and shout!");
        }

        List<SimpleMatrix> corners = new ArrayList<>();
        corners.add(ulCorner);
        corners.add(urCorner);
        corners.add(llCorner);
        corners.add(lrCorner);

        return corners;
    }
}
