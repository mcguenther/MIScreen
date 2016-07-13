package de.volzo.miscreen;

import android.os.Handler;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;

/**
 * Created by Johannes on 01.06.2016.
 */
public class Host {

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


