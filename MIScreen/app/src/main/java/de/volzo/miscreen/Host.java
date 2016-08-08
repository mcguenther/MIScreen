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

        SimpleMatrix originVector = new SimpleMatrix(3, 1, true, 0, 0, 0);
        List<MIPoint2D> cornerPoints = new ArrayList<>();

        for (double[] clientFArrayDouble : fList) {
            SimpleMatrix clientF = new SimpleMatrix(3, 3, false, clientFArrayDouble);

            // TODO: calc 2D points from fMatrices
            SimpleMatrix relativeF = getRelativeTransformation(hostF, clientF);

            // project tranformation into xy-plane
            SimpleMatrix planarF = projectIntoXYPlane(relativeF);

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

    private static SimpleMatrix projectIntoXYPlane(SimpleMatrix relativeF) {
        // create projetion matrix to project from 4x4 (homogeneous 3D space) to 3x3 (homogeneous 2D space)
        SimpleMatrix p = new SimpleMatrix(3, 4, true,
                1, 0, 0, 0,
                0, 1, 0, 0,
                //0, 0, 0, 0,
                0, 0, 0, 1);


        SimpleMatrix f = p.mult(relativeF);

        SimpleMatrix xCol = f.extractVector(false, 0);
        SimpleMatrix yCol = f.extractVector(false, 1);
        SimpleMatrix homoCol = f.extractVector(false, 3);

        SimpleMatrix xyCols = xCol.combine(0, SimpleMatrix.END, yCol);
        SimpleMatrix planarF = xyCols.combine(0, SimpleMatrix.END, homoCol);
        return planarF;
    }

    private static List<double[]> convertMsgs2ListOfDoubles(Collection<Message> messages) {
        List<double[]> matrixlist = new ArrayList<double[]>();

        for (Message msg : messages) {
            for (double[] matrix : msg.transformationMatrix3D) {
                matrixlist.add(matrix);
            }
        }

        return matrixlist;
    }

    private static List<double[]> convertMsg2DoubleArrays(Message message) {
        ArrayList<double[]> doubleList = new ArrayList<double[]>();
        for (int i = 0; i < message.transformationMatrix3D.size(); ++i) {
            double[] newMatrix = message.transformationMatrix3D.get(i);
            doubleList.add(newMatrix);
        }

        return doubleList;
    }

    public static double[] floatArray2doubleArray(float[] clientFArray) {
        double[] resultArray = new double[clientFArray.length];
        for (int i = 0; i < clientFArray.length; i++) {
            resultArray[i] = clientFArray[i];
        }
        return resultArray;
    }

    public static float[] doubleArray2floatArray(double[] clientFArray) {
        float[] resultArray = new float[clientFArray.length];
        for (int i = 0; i < clientFArray.length; i++) {
            resultArray[i] = (float) clientFArray[i];
        }
        return resultArray;
    }


    private static SimpleMatrix getRelativeTransformation(SimpleMatrix fromT, SimpleMatrix toT) {
        SimpleMatrix relativeT = fromT.mult(toT.invert());
        return relativeT;
    }


    private Message matricesIncoming(Message msg) throws Exception {

        Log.i(TAG, "Host received matrix");

        messageVault.put(msg.deviceIdentifier, msg);

        String uuid = Support.getInstance().uuid;
        if (!this.messageVault.containsKey(uuid)) {
            throw new Exception("Host has no location yet");
        }

        Message hostMsg = this.messageVault.get(uuid);
        List<double[]> hostCorners = convertMsg2DoubleArrays(hostMsg);
        double[] topLeftHostCorner = hostCorners.get(0);
        List<double[]> clientCorners = convertMsg2DoubleArrays(msg);
        double[] topLeftClientCorner = clientCorners.get(0);
        List<double[]> allCorners = convertMsgs2ListOfDoubles(this.messageVault.values());

        // TODO apply results from bounding box calculation
        //ArbitrarilyOrientedBoundingBox aobb = getAOBB(topLeftHostCorner, allCorners);
        //double width = aobb.getMaxWidth();

        SimpleMatrix hostMatrix = new SimpleMatrix(4, 4, true, topLeftHostCorner);
        SimpleMatrix clientMatrix = new SimpleMatrix(4, 4, true, topLeftClientCorner);
        SimpleMatrix t = getRelativeTransformation(hostMatrix, clientMatrix);
        SimpleMatrix returnT = projectIntoXYPlane(t);

        Message outMsg = new Message();
        double[] matrixArray = Message.convertSimpleMatrixToArray(returnT);
        outMsg.transformationMatrix2D.add(matrixArray);
        outMsg.transformationMatrixImage.add(SimpleMatrix.identity(3).getMatrix().data);
        return outMsg;
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
                Log.e(TAG, "HTTP serving failed: " + e.toString());
                e.printStackTrace();
                //return new Response(Response.Status.INTERNAL_ERROR);
                return null;
            }
        }
    }
}


