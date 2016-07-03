package de.volzo.miscreen;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by volzotan on 03.07.16.
 */
public class Message {

    //MATRIX[] screenEdges;

    float[] centerpoint = new float[2];
    Float rotation      = null;
    Float scaling       = null;

    public Message() {}

    public Message(JSONObject o) throws JSONException {
        // TODO: centerpoint

        this.rotation = Float.parseFloat((String) o.get("rotation"));
        this.scaling = Float.parseFloat((String) o.get("scaling"));
    }

    public JSONObject toJson() {
        return null;
    }
}
