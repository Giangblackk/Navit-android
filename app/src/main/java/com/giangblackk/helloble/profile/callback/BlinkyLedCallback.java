package com.giangblackk.helloble.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BlinkyLedCallback {
    void onLedStateChanged(@NonNull final BluetoothDevice device, final boolean on);
}
