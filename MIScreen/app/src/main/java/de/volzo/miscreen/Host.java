package de.volzo.miscreen;

/**
 * Created by Johannes on 01.06.2016.
 */
public class Host {

    private static Host mHost = null;
    private Host() {}

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

}
