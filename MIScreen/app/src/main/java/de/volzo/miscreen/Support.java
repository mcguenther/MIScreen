package de.volzo.miscreen;

import android.util.Log;

import org.ejml.simple.SimpleMatrix;

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

        switch(android.os.Build.MODEL) {
            case "XT1069":
                break;
        }

        return null;
    }
}
