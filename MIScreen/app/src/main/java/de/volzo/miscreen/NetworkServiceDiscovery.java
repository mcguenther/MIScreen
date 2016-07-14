package de.volzo.miscreen;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Parts of code taken from https://developer.android.com/training/connect-devices-wirelessly/nsd.html
 */
public class NetworkServiceDiscovery {

    private static final String TAG = NetworkServiceDiscovery.class.getName();

    public InetAddress hostAddress;
    public int hostPort = -1;

    private static final String SERVICE_TYPE = "_http._tcp.";
    private NsdManager.RegistrationListener registrationListener;
    private NsdManager nsdManager;
    private String serviceName;

    private NsdManager.DiscoveryListener discoveryListener;
    private ServerSocket serverSocket;

    private NsdManager.ResolveListener resolveListener;

    private MainActivity activity;

    public NetworkServiceDiscovery(Context context) {
        activity = (MainActivity) context;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void kill() {
        // call in case app is put in background or destroyed
        //connection.tearDown();
        if (serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "something went terribly wrong! closing socket failed");
                // https://www.youtube.com/watch?v=t3otBjVZzT0
            }
        }
        serviceName = null;

        try {
            nsdManager.unregisterService(registrationListener);
        } catch (java.lang.IllegalArgumentException e) {
            // listener is not registered, everything is fine
        }

        try {
            nsdManager.stopServiceDiscovery(discoveryListener);
        } catch (java.lang.IllegalArgumentException e) {
            // listener is not registered, everything is fine
        }
    }

    // Discovering Service

    public void discoverService() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success: " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(serviceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + serviceName);
                } else if (service.getServiceName().contains("MIScreen")){
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "NetworkServiceDiscovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "NetworkServiceDiscovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "NetworkServiceDiscovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };

        initializeResolveListener();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void advertiseService() {
        if (serviceName != null) {
            Log.i(TAG, "service already registered");
        }

        initializeServerSocket();
        initializeRegistrationListener();
        if (serverSocket != null) {
            registerService(serverSocket.getLocalPort());
        }
    }

    // Internal Helper Functions

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(serviceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                NsdServiceInfo service = serviceInfo;
                hostPort = service.getPort();
                hostAddress = service.getHost();
                activity.serviceDiscovered();
            }
        };
    }

    private void initializeServerSocket() {
        try {
            // Initialize a server socket on the next available port.
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = NsdServiceInfo.getServiceName();
                Log.i(TAG, "NSD Registered: " + serviceName + " port: " + serverSocket.getLocalPort());
                hostPort = serverSocket.getLocalPort();

                // Service Registering Finished! Move on to advertising
                Log.i(TAG, "STATUS: service registered. [port: " + hostPort + "]");
                activity.startServing();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "NSD Registration failed. Code " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.i(TAG, "NSD Unregistered");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "NSD Unregistration failed. Code " + errorCode);
            }
        };
    }

    private void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName("MIScreen");
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }
}
