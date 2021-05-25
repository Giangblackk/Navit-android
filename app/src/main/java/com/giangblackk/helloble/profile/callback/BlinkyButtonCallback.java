package com.giangblackk.helloble.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BlinkyButtonCallback {
    void onButtonStateChanged(@NonNull final BluetoothDevice device, final boolean pressed);
}