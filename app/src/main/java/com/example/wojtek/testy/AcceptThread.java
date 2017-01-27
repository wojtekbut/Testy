package com.example.wojtek.testy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by wojtek on 18.01.17.
 */

public class AcceptThread extends Thread {

    private static final UUID MY_UUID =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothAdapter mAdapter;
    public BluetoothDevice mDevice;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothSocket socket;
    private Handler mhandler;
    private String state;

    public AcceptThread(Handler handler) {
        mhandler = handler;

        BluetoothServerSocket tmp = null;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            tmp = mAdapter.listenUsingRfcommWithServiceRecord("Klon",
                    MY_UUID);
        } catch (IOException e) {
        }
        mmServerSocket = tmp;


    }

    public void run() {
        mhandler.obtainMessage(1, "Czekam na połączenie...").sendToTarget();
        socket = null;
        state = "waiting";
        try {

            socket = mmServerSocket.accept();
            mDevice = socket.getRemoteDevice();
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();

        } catch (IOException e) {
        }
        mhandler.obtainMessage(2, "Połączony z: ").sendToTarget();
        state = "connected";
//        if (socket != null) {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[10];
        int bytes;
        bytes = 0;
        while (state == "connected") {
            try {
                // Read from the InputStream
                buffer[bytes] = (byte) mmInStream.read();   // czytanie z mirrora
                if (buffer[bytes] == 10) {
                    //bytes = 0;

                    // Send the obtained bytes to the UI Activity
                    mhandler.obtainMessage(5, new String(buffer, 0, bytes))
                            .sendToTarget();
                    bytes = 0;
                    Arrays.fill(buffer, (byte) 0);
                } else {
                    bytes += 1;
                }
                if (bytes == 10) {
                    Arrays.fill(buffer, (byte) 0);
                    bytes = 0;
                }
            } catch (IOException e) {
                Log.e("rozłączony", "disconnected", e);
                mhandler.obtainMessage(3, "Rozłączony").sendToTarget();
                break;
            }
        }
    }

    public void write(byte[] buffer) {
        if (socket == null) {
            //Log.d("socket" ,"null");
            return;
        }
        try {
            if (!socket.isConnected()) {
                Log.d("state", state);
                if (state.equals("connected")) {
                    socket.close();
                    mhandler.obtainMessage(3, "Rozłączony").sendToTarget();

                }
                return;
            } else {
                mmOutStream.write(buffer);
            }

            // Share the sent message back to the UI Activity
            //mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
            //        .sendToTarget();
        } catch (IOException e) {
            //Log.e(TAG, "Exception during write", e);
        }
    }

    public void writeBonded(ArrayList list) {
        try {
            mmOutStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        //Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
        try {
            socket.close();
            //AcceptThread.this.start();
        } catch (IOException e) {
            //   Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
        }
    }


}
