package de.volzo.miscreen;

import android.app.Application;

import org.artoolkit.ar.base.assets.AssetHelper;

/**
 * Created by Johannes on 20.06.2016.
 */
public class MIScreenApplication extends Application {

    private static Application sInstance;

    // Anywhere in the application where an instance is required, this method
    // can be used to retrieve it.
    public static Application getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        ((MIScreenApplication) sInstance).initializeInstance();
    }

    // Here we do one-off initialisation which should apply to all activities
    // taken from http://artoolkit.org/documentation/doku.php?id=4_Android:android_developing
    // in the application.
    protected void initializeInstance() {

        // Unpack assets to cache directory so native library can read them.
        // N.B.: If contents of assets folder changes, be sure to increment the
        // versionCode integer in the AndroidManifest.xml file.
        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(getInstance(), "Data");
    }
}
