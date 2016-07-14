package de.volzo.miscreen;

import de.volzo.miscreen.arbitraryBoundingBox.ArbitrarilyOrientedBoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.os.Handler;
import android.util.Log;

import com.android.volley.Response;

import org.json.JSONException;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONObject;

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

    Map<String, Message> messageVault = new HashMap<String, Message>();

    private static final String TAG = Host.class.getName();

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
    private static ArbitrarilyOrientedBoundingBox getAOBB(double[] hostFArrayDouble, List<double[]> fList) {
        SimpleMatrix hostF = new SimpleMatrix(3, 3, false, hostFArrayDouble);

        // create projetion matrix to project from 4x4 (homogeneous 3D space) to 3x3 (homogeneous 2D space)
        SimpleMatrix p = new SimpleMatrix(4, 4, true,
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1);

        SimpleMatrix originVector = new SimpleMatrix(3, 1, true, 0, 0, 0);
        List<MIPoint2D> cornerPoints = new ArrayList<>();

        for (double[] clientFArrayDouble : fList) {
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

    private static List<double[]> convertMsgs2ListOfDoubles(Collection<Message> messages) {
        // TODO implement function
        return new ArrayList<>();
    }

    public static double[] floatArray2doubleArray(float[] clientFArray) {
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


    private Message matricesIncoming(Message msg) {
        messageVault.put(msg.deviceIdentifier, msg);

        for (Map.Entry<String, Message> entry : messageVault.entrySet()) {
            // TODO ...
        }

        return new Message();
    }


    public void serve(int port) throws Exception {
        nano = new Nano(port);
    }

    private class Nano extends NanoHTTPD {

        public Nano(int port) throws IOException {
            super(port);
            start();
            Log.d(TAG, "serving on Port: " + port);
        }

        public Response serve(IHTTPSession session) {
            try {
                HashMap<String, String> request = new HashMap<String, String>();
                session.parseBody(request);

                Message payload = new Message(new JSONObject(request.get("postData")));

                Message responseMessage = matricesIncoming(payload);

                return new Response(Response.Status.OK, "application/json", responseMessage.toJson().toString());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                //return new Response(Response.Status.INTERNAL_ERROR);
                return null;
            }
        }
    }
}


