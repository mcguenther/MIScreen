package de.volzo.miscreen;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.Map;

/**
 * Created by volzotan on 11.07.16.
 */
public class Client {

    private static final String TAG = Client.class.getName();
    private Context context;

    private static Client client = null;
    private Client() {}


    public static Client getInstance() {
        if(client == null) {
            client = new Client();
        }
        return client;
    }

    public static Boolean exists() {
        return client != null;
    }

    public static void destroy() {
        if(client != null) {
            client = null;
        }
    }

    public void manuallyInjectContext(Context context) {
        this.context = context;
    }

    public void send(InetAddress addr, JSONObject obj) {
        // TODO volley stuff

        Log.i(TAG, "send json");

        try {

            final String url = "http:/" + addr.toString();
            final JSONObject jsonTestObject = new JSONObject("{\"type\":\"example\"}");

            JsonObjectRequest request = new JsonObjectRequest(url, jsonTestObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(TAG, response.toString());
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

        }  catch (Exception e) {
            Log.e(TAG, exists().toString());
        }
    }

}
