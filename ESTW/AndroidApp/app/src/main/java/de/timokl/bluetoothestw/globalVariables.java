package de.timokl.bluetoothestw;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.io.OutputStream;

public class globalVariables extends Application {
    public static OutputStream stream_out = null;
    private static InputStream stream_in = null;
    private static BluetoothSocket socket = null;
    private static boolean is_connected = false;

    public static BluetoothSocket getSocket() {
        return socket;
    }

    public static void setSocket(BluetoothSocket socket) {
        globalVariables.socket = socket;
    }

    public static InputStream getStreamIn() {
        return stream_in;
    }

    public static void setStreamIn(InputStream stream_in) {
        globalVariables.stream_in = stream_in;
    }

    public static OutputStream getStreamOut() {
        return stream_out;
    }

    public static void setStream_out(OutputStream stream_out) {
        globalVariables.stream_out = stream_out;
    }

    public static boolean isConnected() {
        return is_connected;
    }

    public static void setIsConnected(boolean is_connected) {
        globalVariables.is_connected = is_connected;
    }
}
