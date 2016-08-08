package de.volzo.miscreen;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Handler;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.volzo.miscreen.Message;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Positioner extends ARActivity {
    public static int MARKER_SIZE = 80;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 55;
    //private int markerID = -1;
    private SimpleRenderer arRenderer;
    private TextView spRole;
    private int counter = 0;
    private final Map<String, List<SimpleMatrix>> DISPLAY_CORNERS = new HashMap<>();

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            displayPosition();
        }
    };
    private Timer timer;

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
        Client.getInstance().manuallyInjectPositioner(this);

        Log.d(TAG, "Setting Camera preferences:");

        this.timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    sendTransMatrices();
                } catch (Exception e) {
                    Log.d(TAG, "Sending transformation matrices failed: " + e.toString());
                    //e.printStackTrace();
                }
            }
        }, 0, 10 * 1000);

        drawImage(null);
    }

    private void drawImage(Matrix matrix) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        imageView.setImageResource(R.drawable.flunder_lowres);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required

        if (matrix == null) {
            matrix = new Matrix();
            matrix.postRotate(45, 1280, 720);
            matrix.postTranslate(200, 200);
            matrix.postScale(0.5f, 0.5f);
        }

        imageView.setImageMatrix(matrix);

        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);

        String desc =   "Translate X: " + (int) matrixValues[Matrix.MTRANS_X] + "mm\n" +
                        "Translate Y: " + (int) matrixValues[Matrix.MTRANS_Y] + "mm\n" +
                        "Scale:       " + matrixValues[Matrix.MSCALE_X];

        TextView tvMatrixInternals = (TextView) findViewById(R.id.tvMatrixInternals);
        tvMatrixInternals.setText(desc);

    }


    public void sendTransMatrices() throws Exception {
        List<SimpleMatrix> matrices = getDeviceCornersTransformationsFromMarker();
        Message msg = new Message();

        for (SimpleMatrix m : matrices) {
            double[] array = Message.convertSimpleMatrixToArray(m);
            msg.transformationMatrix3D.add(array);
        }
        JSONObject json = msg.toJson();

        Client.getInstance().send(json);
    }


    public void receivedResponseFromHost(Message msg) {
        // msg should carry one 2D transformation matrix
        double[] transM = msg.transformationMatrix2D.get(0);
        SimpleMatrix transMM = new SimpleMatrix(3,3, true, transM);
        String printM = transMM.toString();

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, printM, duration);
        toast.show();

        float[] matrixData = Host.doubleArray2floatArray(transMM.getMatrix().getData());
        Matrix graphicsMatrix = new Matrix();
        graphicsMatrix.setValues(matrixData);
        drawImage(graphicsMatrix);
    }

    public List<SimpleMatrix> getDeviceCornersTransformationsFromMarker() throws Exception {
        float[] fMarkerToCameraFloat = getTranformationMatrixMarkerToCamera();

        SimpleMatrix fMarkerToCamera = new SimpleMatrix(4, 4, false, Host.floatArray2doubleArray(fMarkerToCameraFloat));
        List<SimpleMatrix> fCameraToCorners = getDeviceCornersTransformationsFromCamera();
        List<SimpleMatrix> fMarkerToCorners = new ArrayList<>();

        for (int i = 0; i < fCameraToCorners.size(); ++i) {
            SimpleMatrix fCameraToOneCorner = fCameraToCorners.get(i);
            SimpleMatrix fMarkerToOneCorner = fMarkerToCamera.mult(fCameraToOneCorner);
            fMarkerToCorners.add(fMarkerToOneCorner);
        }

        return fMarkerToCorners;
    }

    private List<SimpleMatrix> getDeviceCornersTransformationsFromCamera() {
        return Support.getInstance().getDeviceCornersTransformations();
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
        int markerID = arRenderer.getMarkerID();
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
            int markerID = arRenderer.getMarkerID();
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

    protected void onDestroy () {
        super.onDestroy();
        this.timer.cancel();
    }
}
