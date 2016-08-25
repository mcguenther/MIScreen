package de.volzo.miscreen;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Handler;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
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

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.activity_positioner);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


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
                } catch (MarkerException e) {
                    Log.d(TAG, "marker: " + e.toString());
                } catch (Exception e) {
                    Log.d(TAG, "Sending transformation matrices failed: " + e.toString());
                    e.printStackTrace();
                }
            }
        }, 0, 500);

        drawImage(null, null, null, null);
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

        SimpleMatrix scaleMM = new SimpleMatrix(3, 3, true, msg.transformationMatrixImage.get(0));
        SimpleMatrix rotationMM = new SimpleMatrix(3, 3, true, msg.transformationMatrixImage.get(1));
        SimpleMatrix translationMM = new SimpleMatrix(3, 3, true, msg.transformationMatrixImage.get(2));

        SimpleMatrix clientM = new SimpleMatrix(3, 3, true, msg.transformationMatrix2D.get(0));
        //SimpleMatrix imageT = new SimpleMatrix(3, 3, true, msg.transformationMatrixImage.get(0));

        //SimpleMatrix combinedT = imageT.mult(transMM.invert());

        Support s = Support.getInstance();

        //translationMM = translationMM.divide(scaleMM.get(0) / calculatePixelSize());

        drawImage(clientM, scaleMM, rotationMM, translationMM);
    }

    public List<SimpleMatrix> getDeviceCornersTransformationsFromMarker() throws Exception {
        float[] fMarkerToCameraFloat = getTranformationMatrixMarkerToCamera();

        SimpleMatrix fMarkerToCamera = new SimpleMatrix(4, 4, false, Host.floatArray2doubleArray(fMarkerToCameraFloat));
        List<SimpleMatrix> fCameraToCorners = getDeviceCornersTransformationsFromCamera();
        List<SimpleMatrix> fMarkerToCorners = new ArrayList<>();

        for (int i = 0; i < fCameraToCorners.size(); ++i) {
            SimpleMatrix fCameraToOneCorner = fCameraToCorners.get(i);
            SimpleMatrix fMarkerToOneCorner = fCameraToOneCorner.mult(fMarkerToCamera);
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
            throw new MarkerException("Marker not defined yet");
        }

        float[] t = ARToolKit.getInstance().queryMarkerTransformation(markerID);

        if (t == null || t.length == 0) {
            throw new MarkerException("Marker not visible in camera image");
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

    protected void onDestroy() {
        super.onDestroy();
        this.timer.cancel();
    }


    private double calculatePixelSize() {
        List<SimpleMatrix> corners = Support.getInstance().getDeviceCornersTransformations();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int heightInPX = 640; //Math.max(size.x, size.y);

        SimpleMatrix ulCorner = corners.get(0);
        SimpleMatrix llCorner = corners.get(2);

        // May need to remove * -1 in case display corner Coordinate System
        // is changed

        double heightInMM = llCorner.get(1, 3) - ulCorner.get(1, 3);

        return Math.abs(heightInMM) / heightInPX;
    }

    private void drawImage(SimpleMatrix clientM, SimpleMatrix scale, SimpleMatrix rotation, SimpleMatrix translation) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        imageView.setImageResource(R.drawable.hauptgebaeude_lowres_square);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required

        Matrix matrix = new Matrix();


        if (scale != null) {
            // set inverse client transformation matrix in order
            // to get relative T:top left display corner to top left image corner
            SimpleMatrix clientTBackwards = clientM.invert();
            matrix.set(Support.getInstance().convertSimpleMatrixToGraphicsMatrix(clientTBackwards));

            double cosPhi = rotation.get(0, 0);
            double radianPhi = Math.acos(cosPhi);
            double degreePhi = radianPhi * (180 / Math.PI);


            ImageView im = (ImageView) this.findViewById(R.id.imageView);
            matrix.postRotate((float) degreePhi, im.getDrawable().getIntrinsicWidth()/2, im.getDrawable().getIntrinsicHeight()/2);

            double pixelsize = calculatePixelSize();
            double pxPerMmImage = scale.get(0, 0);
            float factor = (float) (1 / (pxPerMmImage * pixelsize));

            matrix.postScale(factor, factor);
            //Log.wtf(TAG, scale.get(0,0) + " PointsPerMM | " + Float.toString(factor));



            SimpleMatrix translationInPx = translation.divide(calculatePixelSize());

            matrix.postTranslate((float) translationInPx.get(0, 2), (float) translationInPx.get(1, 2));

            imageView.setImageMatrix(matrix);

            float[] matrixValues = new float[9];
            matrix.getValues(matrixValues);

            String desc = "Translate X: " + (int) (matrixValues[Matrix.MTRANS_X] * calculatePixelSize()) + "px\n" +
                    "Translate Y: " + (int) (matrixValues[Matrix.MTRANS_Y] * calculatePixelSize()) + "px\n" +
                    "Scale:       " + Math.round(matrixValues[Matrix.MSCALE_X] / Math.cos(degreePhi / (180 / Math.PI))) + "image px / screen px";


            TextView tvMatrixInternals = (TextView) findViewById(R.id.tvMatrixInternals);
            tvMatrixInternals.setText(desc);
        } else {
            //matrix.postScale((720f/1330f), (720f/1330f));
            matrix.postScale(1f, 1f);
            imageView.setImageMatrix(matrix);
        }


    }
}
