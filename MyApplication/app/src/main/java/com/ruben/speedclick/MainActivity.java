package com.ruben.speedclick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button btnStart, btnMain;
    private TextView tvInfo;
    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Sensor sensorMagnet;
    private long startTime, endTime, currentTime, bestTime = 10000;
    private int rotation;
	private static final int ROTATION_MULTIPLIER = 10;
    private float[] startRotation = new float[3];
    private float[] inR = new float[9];
    private float[] outR = new float[9];
    private float[] valuesAccel = new float[3];
    private float[] valuesMagnet = new float[3];
    private float[] valuesResult = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        btnStart = (Button) findViewById(R.id.b_start);
        btnMain = (Button) findViewById(R.id.b_main);
        btnStart.setEnabled(true);
        btnMain.setEnabled(false);

        tvInfo = (TextView) findViewById(R.id.tv_info);
        tvInfo.setText(getString(R.string.best) + bestTime + getString(R.string.ms));

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDeviceOrientation();
                System.arraycopy(valuesResult, 0, startRotation, 0, valuesResult.length);
                btnStart.setEnabled(false);
                btnMain.setText(R.string.press);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startTime = System.currentTimeMillis();
                        btnMain.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        btnMain.setText(R.string.press);
                        btnMain.setEnabled(true);

                    }
                }, 2000);
            }
        });

        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                endTime = System.currentTimeMillis();
                currentTime = endTime - startTime;
                btnMain.setBackgroundColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.red));
                btnStart.setEnabled(true);
                btnMain.setEnabled(false);
                updateDeviceOrientation();
                int result = compareRotation() * ROTATION_MULTIPLIER;
				long resultTime = result + currentTime;
				String txt = currentTime + getString(R.string.ms) + " + " + result + getString(R.string.ms) + "=" + resultTime + getString(R.string.ms);
				btnMain.setText(txt);
                if (currentTime < bestTime) {
                    bestTime = currentTime;
                    tvInfo.setText(getString(R.string.best) + bestTime + getString(R.string.ms));
                }
            }
        });
    }

    private int compareRotation() {
        int result = 0;
        for (int i = 0; i < startRotation.length; i++) {
            result += (valuesResult[i] - startRotation[i]);
        }
        return Math.abs(result);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL);

        WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
        Display display = windowManager.getDefaultDisplay();
        rotation = display.getRotation();

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
    }

    private void updateDeviceOrientation() {
        SensorManager.getRotationMatrix(inR, null, valuesAccel, valuesMagnet);
        int axisX = SensorManager.AXIS_X;
        int axisY = SensorManager.AXIS_Y;
        switch (rotation) {
            case (Surface.ROTATION_0):
                break;
            case (Surface.ROTATION_90):
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
                break;
            case (Surface.ROTATION_180):
                axisY = SensorManager.AXIS_MINUS_Y;
                break;
            case (Surface.ROTATION_270):
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
                break;
            default:
                break;
        }
        SensorManager.remapCoordinateSystem(inR, axisX, axisY, outR);
        SensorManager.getOrientation(outR, valuesResult);
        valuesResult[0] = (float) Math.toDegrees(valuesResult[0]);
        valuesResult[1] = (float) Math.toDegrees(valuesResult[1]);
        valuesResult[2] = (float) Math.toDegrees(valuesResult[2]);
    }

    private SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
					System.arraycopy(event.values, 0, valuesAccel, 0, valuesAccel.length);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
					System.arraycopy(event.values, 0, valuesMagnet, 0, valuesMagnet.length);
                    break;
            }
        }
    };
}
