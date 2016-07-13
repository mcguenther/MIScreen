package de.volzo.miscreen;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Positioner extends ARActivity {

    public static int MARKER_SIZE = 80;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 55;
    private int markerID = -1;
    private SimpleRenderer arRenderer;
    private TextView spRole;
    private int counter = 0;

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

        Log.d(TAG, "Setting Camera preferences:");
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

    public float[] getTranformationMatrix() {
        if (markerID == -1) {
            throw new IllegalStateException();
        }

        float[] t = ARToolKit.getInstance().queryMarkerTransformation(markerID);

        if (t == null || t.length == 0) {
            throw new IllegalStateException();
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
