package de.volzo.miscreen;

import de.volzo.miscreen.arbitraryBoundingBox.ArbitrarilyOrientedBoundingBox;


import java.util.List;

import de.volzo.miscreen.arbitraryBoundingBox.ArbitrarilyOrientedBoundingBox;

import android.os.Handler;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;


/**
 * Created by Johannes on 01.06.2016.
 */
public class Host {

    private static final String TAG = Host.class.getName();

    private static Host mHost = null;
    private Host() {}

    private Nano nano;

    public static Host getInstance() {
        if(mHost == null) {
            mHost = new Host();
        }
        return mHost;
    }

    public static Boolean exists() {
        return mHost != null;
    }

    public static void destroy() {
        if(mHost != null) {
            mHost = null;
        }
    }

    // input: transformation matrices
    //TODO check if matrices have the correct format
    private static ArbitrarilyOrientedBoundingBox getAOBB(float[] hostF, List<float[]> fList) {

        // TODO: calc transformation matrices relative to fMatrix of host (hostF)
        // TODO: calc 2D points from fMatrices
        // TODO: call constructor of AOBB with 2D points

        return null;
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

        public Response serve(String uri, String method, Properties header, Properties params, Properties files) {
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

            String body = "narf";
            try {
                body = (new Message()).toJson().toString();
            } catch (JSONException e) {
                Log.e(TAG, "acquiring JSON failed: " + e.toString());
            }

            return new NanoHTTPD.Response(new Response.IStatus() {
                @Override
                public int getRequestStatus() {
                    return 400;
                }

                @Override
                public String getDescription() {
                    return null;
                }
            }, MIME_HTML, body);
        }
    }
}


