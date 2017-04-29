package com.example.wojtek.testy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by wojtek on 25.01.17.
 */
public class ConnectThread extends Thread {

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final Handler mHandler;
    private final String TAG = "connectThread";
    InputStream mmInStream;
    OutputStream mmOutStream;

    public ConnectThread(BluetoothDevice device, Handler handler) {

        mmDevice = device;
        mHandler = handler;
        BluetoothSocket tmp = null;

        try {

            tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

        } catch (IOException e) {
            Log.d("connectThread", "Socket's create() method failed", e);
        }
        mmSocket = tmp;

    }


    @Override
    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try {

            mmSocket.connect();
        } catch (IOException connectException) {
            Log.e("connectThread", "Nie mogę połączyć.", connectException);
            mHandler.obtainMessage(4, mmDevice.toString()).sendToTarget();
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("connectThread", "Could not close the client socket", closeException);
            }
            return;
        }

        InputStream tmpIn = null;
        OutputStream tmpOut = null;


        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {

        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        czytaj();

    }

    public void czytaj() {  // czytanie z arduino

        byte[] buffer = new byte[10];
        int bytes;
        bytes = 0;
        Log.d("connectThread", "polacz: polaczylem z: " + mmDevice.toString());
        mHandler.obtainMessage(6, mmDevice.toString()).sendToTarget();
        while (mmSocket.isConnected()) {
            try {
                buffer[bytes] = (byte) mmInStream.read();
                //Log.d(TAG, "polacz: mam bajty: " + new String(buffer));
                if (buffer[bytes] == 10) {
                    bytes = 0;
                    if ((char) buffer[0] == 'x') {
                        Log.d(TAG, "polacz: mam bajty: " + new String(buffer));

                        mHandler.obtainMessage(1, new String(buffer)).sendToTarget();


                    } else if ((char) buffer[0] == 'y') {

                        mHandler.obtainMessage(2, new String(buffer)).sendToTarget();


                    }
                    Arrays.fill(buffer, (byte) 0);
                } else {
                    bytes += 1;
                }

                if (bytes == 10) {
                    Arrays.fill(buffer, (byte) 0);
                    bytes = 0;
                }

            } catch (IOException e) {
                mHandler.obtainMessage(4, mmDevice.toString()).sendToTarget();
                cancel();
                return;
                // break;
            }
        }

    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public void write(byte[] buffer) {
        if (mmSocket == null) {
            //Log.d("socket" ,"null");
            return;
        }
        try {
            if (!mmSocket.isConnected()) {

                return;
            } else {
                Log.d(TAG, "Wysyłam: " + new String(buffer));

                mmOutStream.write(buffer);
            }

        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

}
