package de.volzo.miscreen;

import de.volzo.miscreen.arbitraryBoundingBox.ArbitrarilyOrientedBoundingBox;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import de.volzo.miscreen.arbitraryBoundingBox.MIPoint2D;
import fi.iki.elonen.NanoHTTPD;


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
        SimpleMatrix hostF = new SimpleMatrix(4, 4, true, hostFArrayDouble);

        SimpleMatrix originVector = new SimpleMatrix(3, 1, true, 0, 0, 1);
        List<MIPoint2D> cornerPoints = new ArrayList<>();

        for (double[] clientFArrayDouble : fList) {
            SimpleMatrix clientF = new SimpleMatrix(4, 4, true, clientFArrayDouble);

            // TODO: calc 2D points from fMatrices
            SimpleMatrix relativeF = getRelativeTransformation(hostF, clientF);

            // project tranformation into xy-plane
            SimpleMatrix planarF = projectIntoXYPlane(relativeF);

            // normalize planarF by last value
            planarF = normalizeMatrix(planarF);

            // transform point from origin
            SimpleMatrix planarPoint = planarF.mult(originVector);

            int pointX = (int) Math.round(planarPoint.get(0));
            int pointY = (int) Math.round(planarPoint.get(1));
            MIPoint2D point = new MIPoint2D(pointX, pointY);
            cornerPoints.add(point);
        }

        MIPoint2D[] cornerArray = cornerPoints.toArray(new MIPoint2D[cornerPoints.size()]);
        ArbitrarilyOrientedBoundingBox aobb = new ArbitrarilyOrientedBoundingBox(cornerArray);

        return aobb;
    }

    private static SimpleMatrix normalizeMatrix(SimpleMatrix homogenMat) {
        return homogenMat.divide(homogenMat.get(homogenMat.getNumElements() - 1));
    }

    private static SimpleMatrix projectIntoXYPlane(SimpleMatrix relativeF) {
        // create projetion matrix to project from 4x4 (homogeneous 3D space) to 3x3 (homogeneous 2D space)
        SimpleMatrix p = new SimpleMatrix(3, 4, true,
                1, 0, 0, 0,
                0, 1, 0, 0,
                /*0, 0, 0, 0,*/
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
        SimpleMatrix relativeT = toT.mult(fromT.invert());
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

        // get required corners
        List<double[]> hostCorners = convertMsg2DoubleArrays(hostMsg);
        double[] topLeftHostCorner = hostCorners.get(0);
        List<double[]> clientCorners = convertMsg2DoubleArrays(msg);
        double[] topLeftClientCorner = clientCorners.get(0);
        List<double[]> allCorners = convertMsgs2ListOfDoubles(this.messageVault.values());

        // read image dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile("Data/flunder.jpg", options);

        // get bounding box to compute image transformation
        ArbitrarilyOrientedBoundingBox aobb = getAOBB(topLeftHostCorner, allCorners);
        List<SimpleMatrix> imgTransformationHomo = calcImgTransformation(options, aobb);


        // calc transformation matrix for current client
        SimpleMatrix hostMatrix = new SimpleMatrix(4, 4, true, topLeftHostCorner);
        SimpleMatrix clientMatrix = new SimpleMatrix(4, 4, true, topLeftClientCorner);
        SimpleMatrix hostToClientT = getRelativeTransformation(hostMatrix, clientMatrix);
        SimpleMatrix projectedHostToClientT = projectIntoXYPlane(hostToClientT);


        Message outMsg = new Message();
        double[] matrixHostToClient = Message.convertSimpleMatrixToArray(projectedHostToClientT);
        outMsg.transformationMatrix2D.add(matrixHostToClient);

        for(int i=0; i<imgTransformationHomo.size(); ++i) {
            double[] matrixImgT = Message.convertSimpleMatrixToArray(imgTransformationHomo.get(i));
            outMsg.transformationMatrixImage.add(matrixImgT);
            Log.d(TAG, "Sending Host->Image TransMat #" + i + ": " + imgTransformationHomo.toString());
        }
        
        Log.d(TAG, "Sending Host->Client TransMat: " + projectedHostToClientT.toString());
        return outMsg;
    }

    private List<SimpleMatrix> calcImgTransformation(BitmapFactory.Options imgOptions, ArbitrarilyOrientedBoundingBox aobb) {
        double boxWidth = aobb.getWidth();
        double boxHeight = aobb.getHeight();
        double boxRot = aobb.getRot();
        SimpleMatrix imgRotationHomo = getRotatingHomography(boxRot);

        double imgWidth = (double) imgOptions.outWidth;
        double imgHeight = (double) imgOptions.outHeight;

        double dpi;
        double boxRatio = boxWidth / boxHeight;
        double imgRatio = imgWidth / imgHeight;

        if (boxRatio >= 1 && imgRatio < 1 || boxRatio < 1 && imgRatio >= 1) {
            // From of box and image, one is in landscape, one is in portrait alignment;
            // by rotating the image by 90° = PI/2 before fitting to AOBB,
            // the required scaling can kept to a minimum
            imgRatio = 1 / imgRatio;
            double buffer = imgHeight;
            imgHeight = imgWidth;
            imgWidth = buffer;
            SimpleMatrix preRotationHomo = getRotatingHomography(Math.PI / 2);
            // do pre-rotation before main rotation
            imgRotationHomo = imgRotationHomo.mult(preRotationHomo);
        }
        if (imgRatio >= boxRatio) {
            // image is "wider" than box; scale by height
            dpi = imgHeight / boxHeight;
        } else {
            // image is more "narrow" than box; scale by width
            dpi = imgWidth / boxWidth;
        }
        SimpleMatrix imgScalingHomo = new SimpleMatrix(3, 3, true,
                dpi, 0, 0,
                0, dpi, 0,
                0, 0, 1);
        SimpleMatrix imgCenter = new SimpleMatrix(3, 3, true,
                1, 0, (imgWidth / dpi) / 2,
                0, 1, (imgHeight / dpi) / 2,
                0, 0, 1);

        SimpleMatrix boxCenterPoint = aobb.getCenter();
        SimpleMatrix boxCenter = new SimpleMatrix(3, 3, true,
                1, 0, boxCenterPoint.get(0),
                0, 1, boxCenterPoint.get(1),
                0, 0, 1);

        //translation from image center to box center
        SimpleMatrix imgTranslHomo = getRelativeTransformation(imgCenter, boxCenter);

        List<SimpleMatrix> multOrderList = new ArrayList<>();
        multOrderList.add(imgScalingHomo);
        multOrderList.add(imgRotationHomo);
        multOrderList.add(imgTranslHomo);

        //return imgTranslHomo.mult(imgRotationHomo.mult(imgScalingHomo));
        return multOrderList;
    }

    @NonNull
    private SimpleMatrix getRotatingHomography(double rot) {
        return new SimpleMatrix(3, 3, true,
                Math.cos(rot), -1 * Math.sin(rot), 0,
                Math.sin(rot), Math.cos(rot), 0,
                0, 0, 1);
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


