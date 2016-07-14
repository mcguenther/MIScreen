package de.volzo.miscreen;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by volzotan on 11.07.16.
 */
public class Client {

    private static final String TAG = Client.class.getName();
    private Context context;
    private Positioner positioner;

    private static Client client = null;

    private Client() {
    }

    InetAddress hostAddress;
    Integer hostPort;

    public static Client getInstance() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }

    public static Boolean exists() {
        return client != null;
    }

    public static void destroy() {
        if (client != null) {
            client = null;
        }
    }

    public void manuallyInjectContext(Context context) {
        this.context = context;
    }

    public void manuallyInjectPositioner(Positioner positioner) {
        this.positioner = positioner;
    }

    public void send(JSONObject obj) {
        String url;

        if (hostAddress == null) {
            Log.w(TAG, "no Host known. assuming localhost.");
            url = "http://127.0.0.1:" + (hostPort + 1);
        } else {
            url = "http:/" + hostAddress.toString() + ":" + (hostPort + 1);
        }

        Log.i(TAG, "send json");

        try {

            JsonObjectRequest request = new JsonObjectRequest(url, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(TAG, response.toString());
                    // TODO do something with the returned JSON obj
                    if (positioner != null) {

                        try {
                            Message rtMsg = new Message(response);
                            positioner.receivedResponseFromHost(rtMsg);
                        } catch (org.json.JSONException e) {
                            Log.e(TAG, "Message could not be retrieved; received JSON: " + response.toString());
                        }
                    } else {
                        Log.w(TAG, "no Positioner found for callback");
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, error.toString());
                        }
                    });

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

}
