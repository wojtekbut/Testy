package com.example.wojtek.testy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        czytaj();
        /*byte[] buffer = new byte[10];
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
        }*/
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

    public void writeb(String rozkaz, byte[] bity) {
        String poczatek, koniec;
        byte[] bpoczatek, bkoniec, brozkaz, wiadomosc;
        poczatek = "stArt:";
        koniec = ":koNiec";
        bpoczatek = poczatek.getBytes();
        bkoniec = koniec.getBytes();
        brozkaz = rozkaz.getBytes();
        byte[] objetosc = new byte[2];
        byte dlrozkazu = (byte) brozkaz.length;


        int dlugosc = bity.length;
        if (dlugosc < 256) {
            objetosc[0] = 0;
            objetosc[1] = (byte) dlugosc;
        } else if (dlugosc > 255 && dlugosc < 65535) {
            objetosc[0] = (byte) (dlugosc / 256);
            objetosc[1] = (byte) (dlugosc - ((int) objetosc[0] * 256));
        } else {
            Log.e("Write", "Wiadomość za długa.");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(bpoczatek);
            outputStream.write(dlrozkazu);
            outputStream.write(objetosc);
            outputStream.write(brozkaz);
            outputStream.write(bity);
            outputStream.write(bkoniec);

        } catch (IOException e) {
            e.printStackTrace();
        }
        wiadomosc = outputStream.toByteArray();
        Log.d("writebonded:", "wiadomość: " + Arrays.toString(wiadomosc));

        write(wiadomosc);
    }


    public void writes(String rozkaz, String string) {
        String poczatek, koniec;
        byte[] bpoczatek, bkoniec, brozkaz, bstring, wiadomosc;
        poczatek = "stArt:";
        koniec = ":koNiec";
        bpoczatek = poczatek.getBytes();
        bkoniec = koniec.getBytes();
        brozkaz = rozkaz.getBytes();
        bstring = string.getBytes();
        byte[] objetosc = new byte[2];
        byte dlrozkazu = (byte) brozkaz.length;

        int dlugosc = bstring.length;
        if (dlugosc < 256) {
            objetosc[0] = 0;
            objetosc[1] = (byte) dlugosc;
        } else if (dlugosc > 255 && dlugosc < 65535) {
            objetosc[0] = (byte) (dlugosc / 256);
            objetosc[1] = (byte) (dlugosc - ((int) objetosc[0] * 256));
        } else {
            Log.e("Write", "Wiadomość za długa.");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(bpoczatek);
            outputStream.write(dlrozkazu);
            outputStream.write(objetosc);
            outputStream.write(brozkaz);
            outputStream.write(bstring);
            outputStream.write(bkoniec);

        } catch (IOException e) {
            e.printStackTrace();
        }
        wiadomosc = outputStream.toByteArray();
        write(wiadomosc);
    }

    public void writeBonded(byte[] bondedbuff) {
        String rozkaz = "lista:";
        writeb(rozkaz, bondedbuff);
        /*try {

            mmOutStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void czytaj() {
        byte[] buffer = new byte[2];
        byte[] poczatek = new byte[6];
        byte dlrozkazu;
        byte[] rozkaz;// = new byte[];
        byte[] objetosc = new byte[2];
        byte[] koniec = new byte[7];
        int dlugosc;
        while (socket.isConnected()) {
            try {
                poczatek[0] = (byte) mmInStream.read();
                Log.d("czytaj", "Przeczytałem " + (char) poczatek[0]);
                if ((char) poczatek[0] == 's') {
                    for (int i = 1; i < 6; i++) {
                        poczatek[i] = (byte) mmInStream.read();
                    }
                    String spocz = new String(poczatek);
                    Log.d("czytaj", "Przeczytałem " + spocz);
                    if (spocz.equals("stArt:")) {
                        dlrozkazu = (byte) mmInStream.read();
                        rozkaz = new byte[dlrozkazu];
                        for (int i = 0; i < 2; i++) {
                            objetosc[i] = (byte) mmInStream.read();
                        }
                        dlugosc = objetosc[0] * 256 + objetosc[1];
                        buffer = new byte[dlugosc];
                        for (int i = 0; i < dlrozkazu; i++) {
                            rozkaz[i] = (byte) mmInStream.read();
                        }
                        for (int i = 0; i < dlugosc; i++) {
                            buffer[i] = (byte) mmInStream.read();
                        }
                        for (int i = 0; i < 7; i++) {
                            koniec[i] = (byte) mmInStream.read();
                        }
                        String skoniec = new String(koniec);
                        if (skoniec.equals(":koNiec")) {
                            wiadomosc(new String(rozkaz), buffer);
                            Arrays.fill(buffer, (byte) 0);
                            Arrays.fill(poczatek, (byte) 0);
                            Arrays.fill(koniec, (byte) 0);
                            Arrays.fill(objetosc, (byte) 0);
                        } else {
                            Arrays.fill(buffer, (byte) 0);
                            Arrays.fill(poczatek, (byte) 0);
                            Arrays.fill(koniec, (byte) 0);
                            Arrays.fill(objetosc, (byte) 0);
                            continue;
                        }
                    } else {
                        Arrays.fill(buffer, (byte) 0);
                        Arrays.fill(poczatek, (byte) 0);
                        Arrays.fill(koniec, (byte) 0);
                        Arrays.fill(objetosc, (byte) 0);
                        continue;
                    }
                }
            } catch (IOException e) {
                Log.e("rozłączony", "disconnected", e);
                mhandler.obtainMessage(3, "Rozłączony").sendToTarget();
                break;
            }
        }
    }

    public void wiadomosc(String rozkaz, byte[] wiadomosc) {
        Log.d("wiadomość", rozkaz + " ");
        if (rozkaz.startsWith("run:")) {
            Log.d("wiadomosc", "polacz: mam bajty: " + new String(wiadomosc));
            mhandler.obtainMessage(5, new String(wiadomosc)).sendToTarget();
        } else if (rozkaz.startsWith("lista:")) {
            mhandler.obtainMessage(5, new String(wiadomosc)).sendToTarget();
        } else if (rozkaz.startsWith("l:")) {
            mhandler.obtainMessage(5, wiadomosc).sendToTarget();
        } else if (rozkaz.startsWith("polArd")) {
            Log.d("wiadomość", rozkaz + " " + new String(wiadomosc));
            mhandler.obtainMessage(7, new String(wiadomosc)).sendToTarget();
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
