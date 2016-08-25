package de.volzo.miscreen;

import android.graphics.Matrix;
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

    public Matrix convertSimpleMatrixToGraphicsMatrix(SimpleMatrix sm) {
        float[] matrixData = Host.doubleArray2floatArray(sm.getMatrix().getData());
        Matrix graphicsMatrix = new Matrix();
        graphicsMatrix.setValues(matrixData);

        return graphicsMatrix;
    }

    public List<SimpleMatrix> getDeviceCornersTransformations() {
        Log.i(TAG, "device ID: " + android.os.Build.MODEL);

        SimpleMatrix ulCorner = SimpleMatrix.identity(4);
        SimpleMatrix urCorner = SimpleMatrix.identity(4);
        SimpleMatrix llCorner = SimpleMatrix.identity(4);
        SimpleMatrix lrCorner = SimpleMatrix.identity(4);

        switch(android.os.Build.MODEL) {
            case "XT1069": // Moto G2

                ulCorner.set(3, -55);
                ulCorner.set(7,   8);

                urCorner.set(3,   8);
                urCorner.set(7,   8);

                llCorner.set(3, -55);
                llCorner.set(7, 118);

                lrCorner.set(3,   8);
                lrCorner.set(7, 118);

                break;

            case "Nexus 5":

                ulCorner.set(3, -10);
                ulCorner.set(7,   5);

                urCorner.set(3,  50);
                urCorner.set(7,   5);

                llCorner.set(3, -10);
                llCorner.set(7,  80);

                lrCorner.set(3,  50);
                lrCorner.set(7,  80);

                break;

            case "MotoE2(4G-LTE)": // Moto E2

                ulCorner.set(3, -50);
                ulCorner.set(7,   9);

                urCorner.set(3,   7);
                urCorner.set(7,   9);

                llCorner.set(3, -50);
                llCorner.set(7, 108);

                lrCorner.set(3,   7);
                lrCorner.set(7, 108);

                break;

            case "ONEPLUS A3003":

                // TODO

                ulCorner.set(3, -13);
                ulCorner.set(7,  9);

                urCorner.set(3,  52);
                urCorner.set(7,  9);

                llCorner.set(3, -13);
                llCorner.set(7,  130);

                lrCorner.set(3,  52);
                lrCorner.set(7,  130);

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
