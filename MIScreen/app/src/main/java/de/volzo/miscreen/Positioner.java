package de.volzo.miscreen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

public class Positioner extends ARActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 55;
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
    }
    // ARToolkit

    @Override
    protected ARRenderer supplyRenderer() {
        return new SimpleRenderer();
        /* return null;*/
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout)this.findViewById(R.id.frameLayoutPositioner);
    }
}
