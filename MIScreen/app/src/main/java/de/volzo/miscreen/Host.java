package de.volzo.miscreen;

import de.volzo.miscreen.arbitraryBoundingBox.ArbitrarilyOrientedBoundingBox;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

import org.ejml.simple.SimpleMatrix;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import de.volzo.miscreen.arbitraryBoundingBox.MIPoint2D;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;


/**
 * Created by Johannes on 01.06.2016.
 */
public class Host {

    private static Host mHost = null;

    private Host() {
    }

    private Nano nano;

    public static Host getInstance() {
        if (mHost == null) {
            mHost = new Host();
        }
        return mHost;
    }

    public static Boolean exists() {
        return mHost != null;
    }

    public static void destroy() {
        if (mHost != null) {
            mHost = null;
        }
    }

    // input: transformation matrices
    // matrices from ARToolkit are column-major (elements are listed column-wise)
    // --> use new SimpleMatrix(4, 4, false,  <<arToolkitArray>>);
    private static ArbitrarilyOrientedBoundingBox getAOBB(float[] hostFArray, List<float[]> fList) {
        double[] hostFArrayDouble = floatArray2doubleArray(hostFArray);
        SimpleMatrix hostF = new SimpleMatrix(3, 3, false, hostFArrayDouble);

        // create projetion matrix to project from 4x4 (homogeneous 3D space) to 3x3 (homogeneous 2D space)
        SimpleMatrix p = new SimpleMatrix(4, 4, true,
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1);

        SimpleMatrix originVector = new SimpleMatrix(3, 1, true, 0, 0, 0);
        List<MIPoint2D> cornerPoints = new ArrayList<>();

        for (float[] clientFArray : fList) {
            double[] clientFArrayDouble = floatArray2doubleArray(clientFArray);
            SimpleMatrix clientF = new SimpleMatrix(3, 3, false, clientFArrayDouble);

            // TODO: calc 2D points from fMatrices
            SimpleMatrix relativeF = getRelativeTransformation(hostF, clientF);

            // project tranformation into xy-plane
            SimpleMatrix planarF = p.mult(relativeF);

            // transform point from origin
            SimpleMatrix planarPoint = planarF.mult(originVector);

            int pointX = (int) Math.round(planarF.get(0));
            int pointY = (int) Math.round(planarF.get(1));
            MIPoint2D point = new MIPoint2D(pointX, pointY);
            cornerPoints.add(point);
        }


        MIPoint2D[] cornerArray = cornerPoints.toArray(new MIPoint2D[cornerPoints.size()]);

        ArbitrarilyOrientedBoundingBox aobb = new ArbitrarilyOrientedBoundingBox(cornerArray);


        return aobb;
    }

    private static double[] floatArray2doubleArray(float[] clientFArray) {
        double[] resultArray = new double[clientFArray.length];


        for (int i = 0; i < clientFArray.length; i++) {
            resultArray[i] = clientFArray[i];
        }

        return resultArray;
    }

    private static SimpleMatrix getRelativeTransformation(SimpleMatrix fromT, SimpleMatrix toT) {
        SimpleMatrix relativeT = fromT.mult(toT.invert());
        return relativeT;
    }

    public void serve() throws Exception {
        nano = new Nano();
    }

    private class Nano extends NanoHTTPD {

        private final static int PORT = 80;

        public Nano() throws IOException {
            super(PORT);
            ServerRunner.run(Nano.class);
            //start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        }

        public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
            final StringBuilder buf = new StringBuilder();
            for (Map.Entry<Object, Object> kv : header.entrySet())
                buf.append(kv.getKey() + " : " + kv.getValue() + "\n");
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    System.out.println(buf);
                }
            });

            final String html = "<html><head><head><body><h1>Hello, World</h1></body></html>";
            return new NanoHTTPD.Response(new Response.IStatus() {
                @Override
                public int getRequestStatus() {
                    return 400;
                }

                @Override
                public String getDescription() {
                    return null;
                }
            }, MIME_HTML, html);
        }
    }
}


