package de.timokl.bluetoothestw;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;

public class globaleVariablen extends Application {
    public static OutputStream stream_out = null;
    private static InputStream stream_in = null;
    private static BluetoothSocket socket = null;
    private static boolean is_connected = false;
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        globaleVariablen.context = context;
    }

    public static BluetoothSocket getSocket() {
        return socket;
    }

    public static void setSocket(BluetoothSocket socket) {
        globaleVariablen.socket = socket;
    }

    public static InputStream getStream_in() {
        return stream_in;
    }

    public static void setStream_in(InputStream stream_in) {
        globaleVariablen.stream_in = stream_in;
    }

    public static OutputStream getStream_out() {
        return stream_out;
    }

    public static void setStream_out(OutputStream stream_out) {
        globaleVariablen.stream_out = stream_out;
    }

    public static boolean getIs_connected() {
        return is_connected;
    }

    public static void setIs_connected(boolean is_connected) {
        globaleVariablen.is_connected = is_connected;
    }
}
