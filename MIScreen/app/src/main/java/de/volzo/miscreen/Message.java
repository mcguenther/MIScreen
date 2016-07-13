package de.volzo.miscreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by volzotan on 03.07.16.
 */
public class Message {

    List<float[]> transformationMatrix3D;
    List<float[]> transformationMatrix2D;
    List<float[]> transformationMatrixImage;

    public Message() {}

    public Message(JSONObject o) throws JSONException {
        this.transformationMatrix3D = decodeMatrices(o, "matrix3D");
        this.transformationMatrix2D = decodeMatrices(o, "matrix2D");
        this.transformationMatrixImage = decodeMatrices(o, "matrixImage");
    }


    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();

        encodeMatrices(obj, "matrix3D", transformationMatrix3D);
        encodeMatrices(obj, "matrix2D", transformationMatrix2D);
        encodeMatrices(obj, "matrixImage", transformationMatrixImage);

        return obj;
    }

    private List<float[]> decodeMatrices(JSONObject obj, String key) throws JSONException{
        JSONArray matrices = obj.getJSONArray(key);
        List<float[]> list = new ArrayList<float[]>();
        for (int listindex=0; listindex < matrices.length(); listindex++) {
            float[] arr = new float[16];

            JSONArray matrix = matrices.getJSONArray(listindex);
            for (int arrindex=0; arrindex < matrix.length(); arrindex++) {
                arr[arrindex] = (float) matrix.getDouble(arrindex);
            }
            list.add(arr);
        }

        return list;
    }

    private void encodeMatrices(JSONObject obj, String key, List<float[]> payload) throws JSONException {
        JSONArray arr = new JSONArray();
        for (float[] payloadMat : payload) {
            arr.put(new JSONArray(payloadMat));
        }

        obj.put(key, arr);
    }
}
