package com.giangblackk.helloble.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.giangblackk.helloble.profile.callback.BlinkyButtonDataCallback;
import com.giangblackk.helloble.profile.callback.BlinkyLedDataCallback;
import com.giangblackk.helloble.profile.data.BlinkyLED;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;


public class BlinkyManager extends ObservableBleManager {
    // Nordic Blinky Service UUID
    public final static UUID LBS_UUID_SERVICE = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
    // Button characteristic UUID
    private final static UUID LBS_UUID_BUTTON_CHAR = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
    // LED characteristic UUID
    private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("00001525-1212-efde-1523-785feabcd123");

    private final MutableLiveData<Boolean> ledState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> buttonState = new MutableLiveData<>();

    private BluetoothGattCharacteristic buttonCharacteristic, ledCharacteristic;

    private LogSession logSession;
    private boolean supported;
    private boolean ledOn;

    public BlinkyManager(@NonNull final Context context) {
        super(context);
    }

    public final LiveData<Boolean> getLedState() {
        return ledState;
    }

    public final LiveData<Boolean> getButtonState() {
        return buttonState;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new BlinkyBleManagerGattCallback();
    }

    public void setLogger(@Nullable final LogSession session) {
        logSession = session;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        super.log(priority, message);
        Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), message);
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return !supported;
    }

    private final BlinkyButtonDataCallback buttonCallback = new BlinkyButtonDataCallback() {
        @Override
        public void onButtonStateChanged(@NonNull BluetoothDevice device, boolean pressed) {
            log(LogContract.Log.Level.APPLICATION, "Button " + (pressed ? "pressed" : "released"));
            buttonState.setValue(pressed);
        }

        @Override
        public void onInvalidDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    private final BlinkyLedDataCallback ledCallback = new BlinkyLedDataCallback() {
        @Override
        public void onLedStateChanged(@NonNull BluetoothDevice device, boolean on) {
            ledOn = on;
            log(LogContract.Log.Level.APPLICATION, "LED " + (on ? "ON" : "OFF"));
            ledState.setValue(on);
        }

        @Override
        public void onInvalidDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    private class BlinkyBleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
            setNotificationCallback(buttonCharacteristic).with(buttonCallback);
            readCharacteristic(buttonCharacteristic).with(buttonCallback).enqueue();
            enableIndications(buttonCharacteristic).enqueue();
            readCharacteristic(ledCharacteristic).with(ledCallback).enqueue();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
            if (service != null) {
                buttonCharacteristic = service.getCharacteristic(LBS_UUID_BUTTON_CHAR);
                ledCharacteristic = service.getCharacteristic(LBS_UUID_LED_CHAR);
            }
            boolean writeRequest = false;

            if (ledCharacteristic != null) {
                final int rxProperties = ledCharacteristic.getProperties();
                writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }

            supported = buttonCharacteristic != null && ledCharacteristic != null && writeRequest;
            return supported;
        }

        @Override
        protected void onDeviceDisconnected() {
            buttonCharacteristic = null;
            ledCharacteristic = null;
        }
    }

    public void turnLed(final boolean on) {
        if (ledCharacteristic == null)
            return;

        if (ledOn == on)
            return;

        log(Log.VERBOSE, "Turning LED " + (on ? "ON" : "OFF" + "..."));
        writeCharacteristic(ledCharacteristic, on ? BlinkyLED.turnOn() : BlinkyLED.turnOff()).with(ledCallback).enqueue();
    }
}
