package de.volzo.miscreen;

import java.util.UUID;

/**
 * Created by volzotan on 14.07.16.
 */
public class Support {

    String uuid = UUID.randomUUID().toString();

    private static final String TAG = Support.class.getName();

    private static Support support = null;

    private Support() {
    }

    public static Support getInstance() {
        if (support == null) {
            support = new Support();
        }
        return support;
    }
}
