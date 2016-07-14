package de.volzo.miscreen;

import android.content.Context;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by volzotan on 03.07.16.
 */
public class Message {

    String deviceIdentifier;

    List<double[]> transformationMatrix3D;
    List<double[]> transformationMatrix2D;
    List<double[]> transformationMatrixImage;

    public Message() {
        transformationMatrix2D = new ArrayList<>();
        transformationMatrix3D = new ArrayList<>();
        transformationMatrixImage = new ArrayList<>();
        this.deviceIdentifier = Support.getInstance().uuid;
    }

    public Message(JSONObject o) throws JSONException {
        this.deviceIdentifier = (String) o.getString("deviceIdentifier");

        this.transformationMatrix3D = decodeMatrices(o, "matrix3D");
        this.transformationMatrix2D = decodeMatrices(o, "matrix2D");
        this.transformationMatrixImage = decodeMatrices(o, "matrixImage");
    }


    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("deviceIdentifier", deviceIdentifier);

        encodeMatrices(obj, "matrix3D", transformationMatrix3D);
        encodeMatrices(obj, "matrix2D", transformationMatrix2D);
        encodeMatrices(obj, "matrixImage", transformationMatrixImage);

        return obj;
    }

    private List<double[]> decodeMatrices(JSONObject obj, String key) throws JSONException{
        JSONArray matrices = obj.getJSONArray(key);
        List<double[]> list = new ArrayList<double[]>();
        for (int listindex=0; listindex < matrices.length(); listindex++) {
            double[] arr = new double[16];

            JSONArray matrix = matrices.getJSONArray(listindex);
            for (int arrindex=0; arrindex < matrix.length(); arrindex++) {
                arr[arrindex] = (double) matrix.getDouble(arrindex);
            }
            list.add(arr);
        }

        return list;
    }

    private void encodeMatrices(JSONObject obj, String key, List<double[]> payload) throws JSONException {
        JSONArray arr = new JSONArray();

        if (payload != null) {
            for (double[] payloadMat : payload) {
                arr.put(new JSONArray(payloadMat));
            }
        }

        obj.put(key, arr);
    }
}
