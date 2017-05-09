package com.example.wojtek.testy;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // dodana nowa linia
    public static final float ALPHA = 0.20f;
    private static final int REQUEST_ENABLE_BT = 3;
    public AcceptThread mirror;
    public ConnectThread polaczenie;
    public Charset charset = Charset.forName("UTF-8");
    protected float[] accelVals;
    protected float[] magVals;
    TextView xval;
    TextView yval;
    TextView status;
    TextView statusard;
    ToggleButton toggle;
    Button arduino;
    long poprzedni;
    String text;
    TextView macadres;
    BluetoothDevice device;
    String adresMac;
    private float[] Rot = new float[9];
    private float[] results = new float[3];
    private float[] resultsdeg = new float[3];
    private BluetoothAdapter mBluetoothAdapter = null;


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            text = msg.obj.toString();
            switch (msg.what) {
                case 1: //czekam na połączenie
                    Log.d("handler", " czekam: " + text);
                    status.setText(text);
                    break;
                case 2: // połączyłem
                    Log.d("handler", " połączony: " + text);
                    status.setText(text + mirror.mDevice.getAddress() + mirror.mDevice.getName());
                    break;
                case 3: //rozłączony
                    Log.d("handler", " rozłączony");
                    status.setText(text);
                    mirror.cancel();
                    mirror = null;
                    mirror = new AcceptThread(mHandler);
                    mirror.start();
                    break;
                case 4: //nie połączono z arduino
                    statusard.setText("Nie można połączyć\n z " + text);
                    polaczenie = null;
                    break;
                case 5:  // rozkazy z mirrora
                    //text = text.substring(2);
                    Log.d("case 5: ", "rozkaz: " + text);
                    if (text.equals("on")) {
                        toggle.toggle();
                    } else if (text.equals("off")) {
                        toggle.toggle();
                    } else if (text.equals("getBonded")) {
                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            ArrayList<String> mystring = new ArrayList<String>();
                            for (BluetoothDevice mydevice : pairedDevices) {
                                String name = mydevice.getName();
                                if (name.length() > 20) {
                                    name = name.substring(0, 20);
                                }
                                mystring.add(mydevice.getAddress() + "\n" + name);
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try {
                                ObjectOutputStream oos = new ObjectOutputStream(baos);
                                oos.writeObject(mystring);
                                oos.close();
                                byte[] bondedbuff = baos.toByteArray();
                                Log.d("bonded:", "dlugosc listy: " + bondedbuff.length);
                                Log.d("bonded:", "lista: " + Arrays.toString(bondedbuff));

                                //String bondhex
                                //for (int i =0; i< bondedbuff.length;i++)
                                mirror.writeBonded(bondedbuff);


                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    } else {
                        Log.d("read", "Nie znana komenda: " + text);
                    }
                    break;
                case 6:
                    statusard.setText("Połączono z:\n" + text);
                    break;
                case 7:     //połącz z arduino;
                    Log.d("hand 7", "połacz z Ard: " + text);
                    polaczZArduino(text);
                    break;


            }
        }
    };
    private SensorManager sm;
    private Sensor accel, magnet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xval = (TextView) findViewById(R.id.xval);
        yval = (TextView) findViewById(R.id.yval);
        status = (TextView) findViewById(R.id.textView2);
        statusard = (TextView) findViewById(R.id.textView4);
        status.setText("Not conected.");
        statusard.setText("Not conected.");
        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        arduino = (Button) findViewById(R.id.arduino);
        toggle.setOnCheckedChangeListener(this);
        toggle.setTextColor(Color.RED);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Urządzenie nie obsługuje Bluetooth", Toast.LENGTH_LONG).show();
            this.finish();
        }
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnet = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mirror = new AcceptThread(mHandler);
        mirror.start();
        xval.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String roz = "x:";
                String msg = charSequence.toString();// + "\n";
                //byte[] bytes = msg.getBytes(charset);
                mirror.writes(roz, msg);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        yval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String roz = "y:";
                String msg = charSequence.toString(); // + "\n";
                //byte[] bytes = msg.getBytes(charset);
                mirror.writes(roz, msg);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        arduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListaUrzadzen.class);
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toggle.isChecked()) {
            sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            sm.registerListener(this, magnet, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d("click", "onClick d: kliknieto - " + v.toString());

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelVals = lowPass(event.values.clone(), accelVals);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magVals = lowPass(event.values.clone(), magVals);
        }
        if (accelVals != null && magVals != null) {
            SensorManager.getRotationMatrix(Rot, null, accelVals, magVals);
            SensorManager.getOrientation(Rot, results);
            resultsdeg[1] = (float) (((results[1] * 180 / Math.PI)));
            resultsdeg[2] = (float) (((results[2] * 180 / Math.PI)));
            if (resultsdeg[2] > 90) {
                resultsdeg[2] = 180 - resultsdeg[2];
            }
            if (resultsdeg[2] < -90) {
                resultsdeg[2] = -180 - resultsdeg[2];
            }
            String y = String.format("%.01f", resultsdeg[1]);
            String x = String.format("%.01f", resultsdeg[2]);
            if (y.equals("-0,0")) {
                y = "0,0";
            }
            if (x.equals("-0,0")) {
                x = "0,0";
            }
            xval.setText(x);
            yval.setText(y);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Log.d("check", "onCheck d: kliknieto - checked " + buttonView.toString());
            toggle.setTextColor(0xff669900);
            sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            sm.registerListener(this, magnet, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d("check", "onCheck d: kliknieto - un_checked " + buttonView.toString());
            toggle.setTextColor(Color.RED);
            sm.unregisterListener(this);
            xval.setText("- - -");
            yval.setText("- - -");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            adresMac = data.getStringExtra("adres");
            //String nazwa = data.getStringExtra(("nazwa"));
            //statusard.setText("Łączę z: " + nazwa.concat(adresMac));
            //device = mBluetoothAdapter.getRemoteDevice(adresMac);
            //polaczenie = new ConnectThread(device, mHandler);
            //polaczenie.start();
            polaczZArduino(adresMac);
        } else {
            statusard.setText("Nie wybrano adresu.\nPołącz jeszcze raz.");
        }
    }

    protected void polaczZArduino(String address) {
        if (statusard.getText().toString().startsWith("Połączono")) {
            Toast.makeText(this, "Jastem już połączony z Arduino.", Toast.LENGTH_LONG).show();
            return;
        }
        device = mBluetoothAdapter.getRemoteDevice(address);
        String nazwa = device.getName();
        statusard.setText("Łączę z: " + nazwa.concat(address));
        polaczenie = new ConnectThread(device, mHandler);
        polaczenie.start();
    }


}
