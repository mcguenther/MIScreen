package de.volzo.miscreen;

import android.os.Handler;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.Cube;

/**
 * A very simple Renderer that adds a marker and draws a cube on it.
 */
public class SimpleRenderer extends org.artoolkit.ar.base.rendering.ARRenderer {
    private static final String TAG = "SimpleRenderer";
    private int markerID = -1;
    private Cube cube = new Cube(80.0f, 0.0f, 0.0f, 40.0f);
    private Handler mHandler;
    private int markerSize;

    private SimpleRenderer() {
    }

    public SimpleRenderer(Handler mHandler, int markerSize) {
        this.mHandler = mHandler;
        this.markerSize = markerSize;
    }

    /**
     * Markers can be configured here.
     */
    @Override
    public boolean configureARScene() {
        markerID = ARToolKit.getInstance().addMarker("single;Data/hiro.patt;" + Integer.toString(markerSize));
        if (markerID < 0) {
            return false;
        }
        return true;
    }

    /**
     * Override the draw function from ARRenderer.
     */
    @Override
    public void draw(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        // Apply the ARToolKit projection matrix
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(ARToolKit.getInstance().getProjectionMatrix(), 0);

        // If the marker is visible, apply its transformation, and draw a cube
        if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadMatrixf(ARToolKit.getInstance().queryMarkerTransformation(markerID), 0);
            cube.draw(gl);
            mHandler.obtainMessage().sendToTarget();
        }
    }

    public int getMarkerID() {
        return markerID;
    }
}
