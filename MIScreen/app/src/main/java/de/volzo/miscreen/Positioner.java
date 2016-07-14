package de.volzo.miscreen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Positioner extends ARActivity {
    public static int MARKER_SIZE = 80;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 55;
    private int markerID = -1;
    private SimpleRenderer arRenderer;
    private TextView spRole;
    private int counter = 0;
    private final Map<String, List<SimpleMatrix>> DISPLAY_CORNERS = new HashMap<>();


    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            displayPosition();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_positioner);


        // check for camera permissions!!
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        spRole = (TextView) findViewById(R.id.tvStatus);

        writePossibleDisplayCorners();
    }
    
    public void sendTransMatrices() throws Exception {
        List<SimpleMatrix> matrices = getDeviceCornersTransformationsFromMarker();
        Client.getInstance();
        // TODO send matrices
    }


    public List<SimpleMatrix> getDeviceCornersTransformationsFromMarker() throws Exception {
        float[] fMarkerToCameraFloat = getTranformationMatrixMarkerToCamera();
        SimpleMatrix fMarkerToCamera = new SimpleMatrix(4, 4, false, Host.floatArray2doubleArray(fMarkerToCameraFloat));
        List<SimpleMatrix> fCameraToCorners = getDeviceCornersTransformationsFromCamera();
        List<SimpleMatrix> fMarkerToCorners = new ArrayList<>();

        for(int i = 0; i < fCameraToCorners.size(); ++i) {
            SimpleMatrix fCameraToOneCorner = fCameraToCorners.get(i);
            SimpleMatrix fMarkerToOneCorner = fMarkerToCamera.mult(fCameraToOneCorner);
            fMarkerToCorners.add(fMarkerToOneCorner);
        }

        return fMarkerToCorners;
    }

    private void writePossibleDisplayCorners() {
        SimpleMatrix ulCorner = new SimpleMatrix(4, 4, true,
                1, 0, 0, -10,
                0, 1, 0, -50,
                0, 0, 0, 0,
                0, 0, 0, 1);

        SimpleMatrix urCorner = new SimpleMatrix(4, 4, true,
                1, 0, 0, 50,
                0, 1, 0, -5,
                0, 0, 0, 0,
                0, 0, 0, 1);
        SimpleMatrix llCorner = new SimpleMatrix(4, 4, true,
                1, 0, 0, -10,
                0, 1, 0, -80,
                0, 0, 0, 0,
                0, 0, 0, 1);
        SimpleMatrix lrCorner = new SimpleMatrix(4, 4, true,
                1, 0, 0, 50,
                0, 1, 0, -80,
                0, 0, 0, 0,
                0, 0, 0, 1);

        List<SimpleMatrix> corners = new ArrayList<>();
        corners.add(ulCorner);
        corners.add(urCorner);
        corners.add(llCorner);
        corners.add(lrCorner);
        DISPLAY_CORNERS.put("Nexus 5", corners);
    }

    private List<SimpleMatrix> getDeviceCornersTransformationsFromCamera() {
        Boolean debug = true;

        String deviceName = android.os.Build.MODEL;
        if (debug) {
            deviceName = "Nexus 5";
        }
        List<SimpleMatrix> corners = null;
        if(this.DISPLAY_CORNERS.containsKey(deviceName)) {
            corners = this.DISPLAY_CORNERS.get(deviceName);
        }
        return corners;
    }

    
    // ARToolkit
    @Override
    protected ARRenderer supplyRenderer() {
        this.arRenderer = new SimpleRenderer(mHandler, MARKER_SIZE);
        return arRenderer;
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
        //this.findViewById(R.id.frameLayoutPositioner).setVisibility(View.INVISIBLE);
        return (FrameLayout) this.findViewById(R.id.frameLayoutPositioner);
    }

    public float[] getTranformationMatrixMarkerToCamera() throws Exception {
        if (markerID == -1) {
            throw new Exception("Marker not defined yet");
        }

        float[] t = ARToolKit.getInstance().queryMarkerTransformation(markerID);

        if (t == null || t.length == 0) {
            throw new Exception("Marker not visible in camera image");
        }

        return t;
    }

    public void displayPosition() {
        if (arRenderer != null) {
            markerID = arRenderer.getMarkerID();
            if (markerID > -1) {
                float[] t = ARToolKit.getInstance().queryMarkerTransformation(markerID);

                if (t != null) {
                    if (counter > 0) {
                        float offsetX = t[12];
                        float offsetY = t[13];
                        float offsetZ = t[14];

                        double dist = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
                        String text = "Distance: " + Double.toString(dist);

                        spRole.setText(text);
                        Log.d(TAG, Arrays.toString(t));
                        Log.d(TAG, text + "(X:" + offsetX + " Y:" + offsetY + " Z:" + offsetZ + ")");
                        counter = 0;
                    } else {
                        counter++;
                    }
                }
            }
        }
    }
}
