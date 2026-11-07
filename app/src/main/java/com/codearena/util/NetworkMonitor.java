package com.codearena.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * NetworkMonitor: Emits connectivity changes using Android ConnectivityManager callbacks.
 */
public class NetworkMonitor {

    private final ConnectivityManager connectivityManager;
    private final MutableLiveData<Boolean> isConnectedLiveData = new MutableLiveData<>(true);

    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        updateInitialState();
        registerCallback();
    }

    private void updateInitialState() {
        if (connectivityManager == null) {
            isConnectedLiveData.postValue(false);
            return;
        }
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            isConnectedLiveData.postValue(false);
            return;
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        boolean connected = capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        isConnectedLiveData.postValue(connected);
    }

    private void registerCallback() {
        if (connectivityManager == null) return;
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                isConnectedLiveData.postValue(true);
            }

            @Override
            public void onLost(Network network) {
                isConnectedLiveData.postValue(false);
            }
        });
    }

    public LiveData<Boolean> isConnected() {
        return isConnectedLiveData;
    }
}

