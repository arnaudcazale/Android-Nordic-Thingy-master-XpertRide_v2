/*
 * Copyright (c) 2010 - 2017, Nordic Semiconductor ASA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form, except as embedded into a Nordic
 *    Semiconductor ASA integrated circuit in a product or a software update for
 *    such product, must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. Neither the name of Nordic Semiconductor ASA nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * 4. This software, with or without modification, must only be used with a
 *    Nordic Semiconductor ASA integrated circuit.
 *
 * 5. Any software provided in binary form under this license must not be reverse
 *    engineered, decompiled, modified and/or disassembled.
 *
 * THIS SOFTWARE IS PROVIDED BY NORDIC SEMICONDUCTOR ASA "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY, NONINFRINGEMENT, AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NORDIC SEMICONDUCTOR ASA OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfthingy;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.widgets.Renderer;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class MotionServiceFragment extends Fragment implements ScannerFragmentListener {
    private Toolbar mQuaternionToolbar;

    private Button mButtonSend;
    private EditText mTextCommand;
    private TextView mTestView;
    private String m_string;
    private String m_string_command;
    private String m_string_argument;

    private ImageView mLedRgb1;
    private ImageView mLedRgb2;
    private ImageView mLedRgb3;
    private ImageView mLedRgb4;
    private ImageView mLedRgbResult;

    private GradientDrawable mRgbDrawable1;
    private GradientDrawable mRgbDrawable2;
    private GradientDrawable mRgbDrawable3;
    private GradientDrawable mRgbDrawable4;
    private GradientDrawable mRgbDrawableResult;

    //private int mSelectedRgbColorIntensity;
    private short ThresholdVoltageLow = 200;
    private short ThresholdVoltageHigh = 3000;

    private TextView mTapCount;
    private TextView mTapDirection;
    private TextView mOrientation;
    private TextView mHeading;
    private TextView mPedometerSteps;
    private TextView mPedometerDuration;
    private TextView mHeadingDirection;

    private ImageView mPortraitImage;

    private RajawaliSurfaceView mGlSurfaceView;
    private BluetoothDevice mDevice;

    private DatabaseHelper mDatabaseHelper;
    private ThingySdkManager mThingySdkManager = null;
    private boolean mIsConnected = false;

    private ImageView mHeadingImage;
    private LineChart mLineChartGravityVector;
    private boolean mIsFragmentAttached = false;
    private Renderer mRenderer;

    private ThingyListener mThingyListener = new ThingyListener() {
        float mCurrentDegree = 0.0f;
        private float mHeadingDegrees;
        private RotateAnimation mHeadingAnimation;

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            //Connectivity callbacks handled by main activity
            //Allow command characteristic to answer
            //enableCommandNotifications(true);
            //Log.e("APP SERVICE DISCOVERY COMPLETED", "enable cccd command");
            //Request long size MTU for sending longer write command (set to 20 bytes if not)
            //mThingySdkManager.requestMtu(mDevice);
            //while(mThingySdkManager.mtuSizeGet(mDevice) != 276);
            //Log.e("APP SERVICE DISCOVERY COMPLETED", "mTu Size = " + mThingySdkManager.mtuSizeGet(mDevice) );
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            if (mDevice.equals(device)) {
                mRenderer.setConnectionState(false);
                if (Utils.checkIfVersionIsAboveJellyBean()) {
                    mRenderer.setNotificationEnabled(false);
                }
            }
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            if (mDevice.equals(device)) {
                mIsConnected = true;
                enableCommandNotifications(true);
                Log.e("APP SERVICE DISCOVERY COMPLETED", "enable cccd command");
                //Request long size MTU for sending longer write command (set to 20 bytes if not)
                mThingySdkManager.requestMtu(mDevice);
                Log.e("APP SERVICE DISCOVERY COMPLETED", "requestMtu");
                if (Utils.checkIfVersionIsAboveJellyBean()) {
                    mRenderer.setConnectionState(true);
                    if (mDatabaseHelper.getNotificationsState(mDevice.getAddress(), DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
                        mRenderer.setNotificationEnabled(true);
                    }
                }
            }
        }

        @Override
        public void onBatteryLevelChanged(final BluetoothDevice bluetoothDevice, final int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, final String pressure) {
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, final String humidity) {
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, final int eco2, final int tvoc) {
        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, final float red, final float green, final float blue, final float alpha) {
        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int tapCount) {
            if (mIsFragmentAttached) {
                mTapCount.setText(String.valueOf(tapCount));
                switch (direction) {
                    case ThingyUtils.TAP_X_UP:
                        mTapDirection.setText(ThingyUtils.X_UP);
                        break;
                    case ThingyUtils.TAP_X_DOWN:
                        mTapDirection.setText(ThingyUtils.X_DOWN);
                        break;
                    case ThingyUtils.TAP_Y_UP:
                        mTapDirection.setText(ThingyUtils.Y_UP);
                        break;
                    case ThingyUtils.TAP_Y_DOWN:
                        mTapDirection.setText(ThingyUtils.Y_DOWN);
                        break;
                    case ThingyUtils.TAP_Z_UP:
                        mTapDirection.setText(ThingyUtils.Z_UP);
                        break;
                    case ThingyUtils.TAP_Z_DOWN:
                        mTapDirection.setText(ThingyUtils.Z_DOWN);
                        break;
                }
            }
        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {
            mPortraitImage.setPivotX(mPortraitImage.getWidth() / 2.0f);
            mPortraitImage.setPivotY(mPortraitImage.getHeight() / 2.0f);
            mPortraitImage.setRotation(0);

            if (mIsFragmentAttached) {
                switch (orientation) {
                    case ThingyUtils.PORTRAIT_TYPE:
                        mPortraitImage.setRotation(0);

                        mOrientation.setText(ThingyUtils.PORTRAIT);
                        break;
                    case ThingyUtils.LANDSCAPE_TYPE:
                        mPortraitImage.setRotation(90);

                        mOrientation.setText(ThingyUtils.LANDSCAPE);
                        break;
                    case ThingyUtils.REVERSE_PORTRAIT_TYPE:
                        mPortraitImage.setRotation(-180);
                        mOrientation.setText(ThingyUtils.REVERSE_PORTRAIT);
                        break;
                    case ThingyUtils.REVERSE_LANDSCAPE_TYPE:
                        mPortraitImage.setRotation(-90);

                        mOrientation.setText(ThingyUtils.REVERSE_LANDSCAPE);
                        break;
                }
            }
        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {
            if (mIsFragmentAttached) {
                if (mGlSurfaceView != null) {
                    mRenderer.setQuaternions(x, y, z, w);
                }
            }
        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {
            if (mIsFragmentAttached) {
                mPedometerSteps.setText(String.valueOf(steps));
                mPedometerDuration.setText(ThingyUtils.TIME_FORMAT_PEDOMETER.format(duration));
            }
        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {
        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {
            if (mIsFragmentAttached) {
                mHeadingDegrees = heading;
                if (mHeadingAnimation != null) {
                    mHeadingAnimation.reset();
                }

                if (mHeadingDegrees >= 0 && mHeadingDegrees <= 10) {
                    mHeadingDirection.setText(R.string.north);
                } else if (mHeadingDegrees >= 35 && mHeadingDegrees <= 55) {
                    mHeadingDirection.setText(R.string.north_east);
                } else if (mHeadingDegrees >= 80 && mHeadingDegrees <= 100) {
                    mHeadingDirection.setText(R.string.east);
                } else if (mHeadingDegrees >= 125 && mHeadingDegrees <= 145) {
                    mHeadingDirection.setText(R.string.south_east);
                } else if (mHeadingDegrees >= 170 && mHeadingDegrees <= 190) {
                    mHeadingDirection.setText(R.string.south);
                } else if (mHeadingDegrees >= 215 && mHeadingDegrees <= 235) {
                    mHeadingDirection.setText(R.string.south_west);
                } else if (mHeadingDegrees >= 260 && mHeadingDegrees <= 280) {
                    mHeadingDirection.setText(R.string.west);
                } else if (mHeadingDegrees >= 305 && mHeadingDegrees <= 325) {
                    mHeadingDirection.setText(R.string.north_west);
                } else if (mHeadingDegrees >= 350 && mHeadingDegrees <= 359) {
                    mHeadingDirection.setText(R.string.north);
                }

                mHeadingAnimation = new RotateAnimation(mCurrentDegree, -mHeadingDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                mHeadingAnimation.setFillAfter(true);
                mHeadingImage.startAnimation(mHeadingAnimation);
                mHeading.setText(getString(R.string.degrees_2, mHeadingDegrees));
                mCurrentDegree = -mHeadingDegrees;
            }
        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            //Log.e("APP", "GRAVITY DATA");
            //addGravityVectorEntry(x, y, z);
        }

        @Override
        public void onFsrDataValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] answer) {
            Log.e("THINGY CONNECTION", "FSR DATA RECEIVED" + Arrays.toString(answer));

            switch(m_string_command){
                case "START":
                    if( m_string_argument != null){
                        String string_argument = m_string_argument.substring(0, m_string_argument.indexOf(' '));
                        Log.e("APP", "STRING ARGUMENT START " + string_argument);
                        if(string_argument.compareTo("V") == 0){
                            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                            short mFSR1 = mByteBuffer.getShort(0);
                            short mFSR2 = mByteBuffer.getShort(2);
                            short mFSR3 = mByteBuffer.getShort(4);
                            short mFSR4 = mByteBuffer.getShort(6);
                            short mFSR5 = mByteBuffer.getShort(8);
                            short mFSR6 = mByteBuffer.getShort(10);
                            short mFSR7 = mByteBuffer.getShort(12);
                            short mFSR8 = mByteBuffer.getShort(14);
                            addGravityVectorEntry(mFSR1, mFSR2, mFSR3, mFSR4, mFSR5, mFSR6, mFSR7, mFSR8);
                            //updateLedColor_voltage(mFSR1, mFSR2, mFSR3, mFSR4);
                        }else if(string_argument.compareTo("F") == 0){
                            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                            float mFSR1 = mByteBuffer.getFloat(0);
                            float mFSR2 = mByteBuffer.getFloat(4);
                            float mFSR3 = mByteBuffer.getFloat(8);
                            float mFSR4 = mByteBuffer.getFloat(12);
                            float mFSR5 = mByteBuffer.getFloat(16);
                            float mFSR6 = mByteBuffer.getFloat(20);
                            float mFSR7 = mByteBuffer.getFloat(24);
                            float mFSR8 = mByteBuffer.getFloat(28);
                            addGravityVectorEntry_float(mFSR1, mFSR2, mFSR3, mFSR4, mFSR5, mFSR6, mFSR7, mFSR8);
                            //updateLedColor_force(mFSR1, mFSR2, mFSR3, mFSR4);
                        }else if(string_argument.compareTo("FC") == 0){
                            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                            float mFSR1 = mByteBuffer.getFloat(0);
                            float mFSR2 = mByteBuffer.getFloat(4);
                            float mFSR3 = mByteBuffer.getFloat(8);
                            float mFSR4 = mByteBuffer.getFloat(12);
                            float mFSR5 = mByteBuffer.getFloat(16);
                            float mFSR6 = mByteBuffer.getFloat(20);
                            float mFSR7 = mByteBuffer.getFloat(24);
                            float mFSR8 = mByteBuffer.getFloat(28);
                            addGravityVectorEntry_float(mFSR1, mFSR2, mFSR3, mFSR4, mFSR5, mFSR6, mFSR7, mFSR8);
                            //updateLedColor_force_calculated(mFSR1, mFSR2, mFSR3, mFSR4);
                        }else{
                            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floatingg
                            float mFSR1 = mByteBuffer.getFloat(0);
                            float mFSR2 = mByteBuffer.getFloat(4);
                            float mFSR3 = mByteBuffer.getFloat(8);
                            float mFSR4 = mByteBuffer.getFloat(12);
                            float mFSR5 = mByteBuffer.getFloat(16);
                            float mFSR6 = mByteBuffer.getFloat(20);
                            float mFSR7 = mByteBuffer.getFloat(24);
                            float mFSR8 = mByteBuffer.getFloat(28);
                            addGravityVectorEntry_float(mFSR1, mFSR2, mFSR3, mFSR4, mFSR5, mFSR6, mFSR7, mFSR8);
                            //updateLedColor_force_calculated(mFSR1, mFSR2, mFSR3, mFSR4);
                        }
                    }else{
                        final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                        mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                        float mFSR1 = mByteBuffer.getFloat(0);
                        float mFSR2 = mByteBuffer.getFloat(4);
                        float mFSR3 = mByteBuffer.getFloat(8);
                        float mFSR4 = mByteBuffer.getFloat(12);
                        float mFSR5 = mByteBuffer.getFloat(16);
                        float mFSR6 = mByteBuffer.getFloat(20);
                        float mFSR7 = mByteBuffer.getFloat(24);
                        float mFSR8 = mByteBuffer.getFloat(28);
                        //Log.e("APP", "FSR DATA RECEIVED " + mFSR1 + "-" + mFSR2 + "-" + mFSR3 + "-" + mFSR4 + "-" + mFSR5 + "-" + mFSR6 + "-" + mFSR7 + "-" + mFSR8);
                        addGravityVectorEntry_float(mFSR1, mFSR2, mFSR3, mFSR4, mFSR5, mFSR6, mFSR7, mFSR8);
                        //updateLedColor_force_calculated(mFSR1, mFSR2, mFSR3, mFSR4, mFSR5, mFSR6, mFSR7, mFSR8);
                    }
                    break;
                case "RSTART":
                    if( m_string_argument != null){
                        Log.e("APP", "STRING ARGUMENT START " + m_string_argument);
                        String string_argument = m_string_argument.substring(0, m_string_argument.indexOf(' '));
                        if(string_argument.compareTo("V") == 0){
                            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                            short mFSR1 = mByteBuffer.getShort(0);
                            short mFSR2 = mByteBuffer.getShort(2);
                            addGravityVectorEntry_head(mFSR1, mFSR2);
                            //updateLedColor_voltage(mFSR1, mFSR2, mFSR3, mFSR4);
                        }else if(string_argument.compareTo("F") == 0){
                            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                            float mFSR1 = mByteBuffer.getFloat(0);
                            float mFSR2 = mByteBuffer.getFloat(4);
                            addGravityVectorEntry_head_float(mFSR1, mFSR2);
                            //updateLedColor_force(mFSR1, mFSR2, mFSR3, mFSR4);
                        }else if(string_argument.compareTo("FC") == 0){
                            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                            float mFSR1 = mByteBuffer.getFloat(0);
                            float mFSR2 = mByteBuffer.getFloat(4);
                            addGravityVectorEntry_head_float(mFSR1, mFSR2);
                            //updateLedColor_force_calculated(mFSR1, mFSR2, mFSR3, mFSR4);
                        }else{
                            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floatingg
                            float mFSR1 = mByteBuffer.getFloat(0);
                            float mFSR2 = mByteBuffer.getFloat(4);
                            addGravityVectorEntry_head_float(mFSR1, mFSR2);
                            //updateLedColor_force_calculated(mFSR1, mFSR2, mFSR3, mFSR4);
                        }
                    }else{
                        final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
                        mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                        float mFSR1 = mByteBuffer.getFloat(0);
                        float mFSR2 = mByteBuffer.getFloat(4);
                        //Log.e("APP", "FSR DATA RECEIVED " + mFSR1 + "-" + mFSR2 + "-" + mFSR3 + "-" + mFSR4 + "-" + mFSR5 + "-" + mFSR6 + "-" + mFSR7 + "-" + mFSR8);
                        addGravityVectorEntry_head_float(mFSR1, mFSR2);
                        //updateLedColor_force_calculated(mFSR1, mFSR2, mFSR3, mFSR4, mFSR5, mFSR6, mFSR7, mFSR8);
                    }
                    break;
            }

        }

        @Override
        public void onCommandValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] answer) {
            Log.e("MOTION SERVICE FRAGMENT", "STRING LENGTH " + answer.length);
            Log.e("MOTION SERVICE FRAGMENT", "COMMAND ANSWER DATA RECEIVED TO STRING" + Arrays.toString(answer));
            Log.e("MOTION SERVICE FRAGMENT", "COMMAND ANSWER DATA RECEIVED " + answer);

            final ByteBuffer mByteBuffer = ByteBuffer.wrap(answer);
            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            switch(m_string_command){
                case "DEBUG":
                    int [] int_array_debug = new int[3];
                    float[] float_array_debug = new float[2];
                    String string_debug = new String();
                    String string_concat_debug = new String();
                    for(int j = 0; j < 8; j++){
                        int_array_debug[0] = mByteBuffer.getInt(j*20);
                        int_array_debug[1] = mByteBuffer.getInt(j*20 + 4 );
                        int_array_debug[2] = mByteBuffer.getInt(j*20 + 8);
                        float_array_debug[0] = mByteBuffer.getFloat(j*20 + 12);
                        float_array_debug[1] = mByteBuffer.getFloat(j*20 + 16);
                        if(j == 7){
                            string_debug = Arrays.toString(int_array_debug)+ ", " + Arrays.toString(float_array_debug);
                            string_concat_debug += "[" + string_debug.replace("[", "").replace("]", "") + "]";
                        }else{
                            string_debug = Arrays.toString(int_array_debug)+ ", " + Arrays.toString(float_array_debug);
                            string_concat_debug += "[" + string_debug.replace("[", "").replace("]", "") + "]" + "\n";
                        }
                    }
                    //Log.e("MOTION SERVICE FRAGMENT", "COMMAND ANSWER DATA RECEIVED " + string_concat_debug);
                    mTestView.setTextSize(15);
                    mTestView.setText(string_concat_debug);
                    break;
                case "RSN":
                    String string_rsn = new String(answer);
                    mTestView.setTextSize(20);
                    mTestView.setText(string_rsn);
                    break;
                case "RTYPE":
                    String string_rtype = new String(answer);
                    mTestView.setTextSize(20);
                    mTestView.setText(string_rtype);
                    break;
                case "SINGLE":
                    if( m_string_argument != null){
                        if(m_string_argument.compareTo("V") == 0){
                            short[] short_array_single = new short[answer.length/2];
                            for(int i=0; i<answer.length/2; i++){
                                short_array_single[i] = mByteBuffer.getShort(i*2);
                            }
                            mTestView.setTextSize(20);
                            mTestView.setText(Arrays.toString(short_array_single));
                        }else if(m_string_argument.compareTo("F") == 0){
                            float[] float_array_single_f = new float[answer.length/4];
                            for(int i=0; i<answer.length/4; i++){
                                float_array_single_f[i] = mByteBuffer.getFloat(i*4);
                            }
                            mTestView.setTextSize(20);
                            mTestView.setText(Arrays.toString(float_array_single_f));
                        }else if(m_string_argument.compareTo("FC") == 0){
                            float[] float_array_single_fc = new float[answer.length/4];
                            for(int i=0; i<answer.length/4; i++){
                                float_array_single_fc[i] = mByteBuffer.getFloat(i*4);
                            }
                            mTestView.setTextSize(20);
                            mTestView.setText(Arrays.toString(float_array_single_fc));
                        }else{
                            float[] float_array_single_fc = new float[answer.length/4];
                            for(int i=0; i<answer.length/4; i++){
                                float_array_single_fc[i] = mByteBuffer.getFloat(i*4);
                            }
                            mTestView.setTextSize(20);
                            mTestView.setText(Arrays.toString(float_array_single_fc));
                        }
                    }else{
                        float[] float_array_single_fc = new float[answer.length/4];
                        for(int i=0; i<answer.length/4; i++){
                            float_array_single_fc[i] = mByteBuffer.getFloat(i*4);
                        }
                        mTestView.setTextSize(20);
                        mTestView.setText(Arrays.toString(float_array_single_fc));
                    }
                    break;
                case "RFACTLIN":
                    int int_number_rfactlin = mByteBuffer.getInt(0);
                    float[] float_array_rfactlin = new float[(answer.length/4)-1];
                    for(int i=0; i<answer.length/4-1; i++){
                        float_array_rfactlin[i] = mByteBuffer.getFloat(i*4+4);
                    }
                    String float_string_rfactlin = Arrays.toString(float_array_rfactlin);
                    String int_string_rfactlin = Integer.toString(int_number_rfactlin);
                    mTestView.setTextSize(20);
                    mTestView.setText(int_string_rfactlin + float_string_rfactlin);
                    break;
                case "RTARE":
                case "TARE":
                    float[] float_array_tare = new float[(answer.length/4)];
                    for(int i=0; i<answer.length/4; i++){
                        float_array_tare[i] = mByteBuffer.getFloat(i*4);
                    }
                    mTestView.setTextSize(15);
                    mTestView.setText(Arrays.toString(float_array_tare));
                    break;
                case "RCAL":
                    float[] float_array_calw = new float[(answer.length/4)];
                    for(int i=0; i<answer.length/4; i++){
                        float_array_calw[i] = mByteBuffer.getFloat(i*4);
                    }
                    mTestView.setTextSize(20);
                    mTestView.setText(Arrays.toString(float_array_calw));
                    break;
                case "RBRIDGE":
                    short resistor = mByteBuffer.getShort(0);
                    mTestView.setTextSize(20);
                    mTestView.setText(Short.toString(resistor));
                    break;
                case "RGAIN":
                    String gain = Arrays.toString(answer);
                    mTestView.setTextSize(20);
                    mTestView.setText(gain);
                    break;
            }
        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, final byte[] data) {

        }
    };

    public static MotionServiceFragment newInstance(final BluetoothDevice device) {
        final  MotionServiceFragment fragment = new MotionServiceFragment();

        final Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper = new DatabaseHelper(getActivity());
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_motion, container, false);
        mThingySdkManager = ThingySdkManager.getInstance();

        mTextCommand = rootView.findViewById(R.id.command_text);
        mButtonSend = rootView.findViewById(R.id.button_send);
        mTestView = rootView.findViewById(R.id.text_view);

        mLedRgb1 = rootView.findViewById(R.id.img_led_rgb1);
        mLedRgb2 = rootView.findViewById(R.id.img_led_rgb2);
        mLedRgb3 = rootView.findViewById(R.id.img_led_rgb3);
        mLedRgb4 = rootView.findViewById(R.id.img_led_rgb4);
        mLedRgbResult = rootView.findViewById(R.id.img_led_rgb_result);
        mRgbDrawable1 = (GradientDrawable) mLedRgb1.getDrawable();
        mRgbDrawable2 = (GradientDrawable) mLedRgb2.getDrawable();
        mRgbDrawable3 = (GradientDrawable) mLedRgb3.getDrawable();
        mRgbDrawable4 = (GradientDrawable) mLedRgb4.getDrawable();
        mRgbDrawableResult = (GradientDrawable) mLedRgbResult.getDrawable();

        //Allow first command characteristic to answer
        enableCommandNotifications(true);
        Log.e("APP SERVICE DISCOVERY COMPLETED", "enable cccd command");
        //Request long size MTU for sending longer write command (set to 20 bytes if not)
        mThingySdkManager.requestMtu(mDevice);
        Log.e("APP SERVICE DISCOVERY COMPLETED", "request mtu");


        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Disable gravity cccd
                enableGravityVectorNotifications(false);

                //Convert string to uppercase
                m_string_command = null;
                m_string_argument = null;
                m_string = mTextCommand.getText().toString();
                m_string = m_string.toUpperCase();

                if( m_string != null && !m_string.isEmpty() && m_string.charAt(0) == '*'){
                    try {
                        m_string_command = m_string.substring(1, m_string.indexOf(' '));
                        m_string_argument = m_string.substring(m_string.indexOf(' ')+1);
                    } catch(StringIndexOutOfBoundsException e){
                        m_string_command = m_string.substring(1);
                    }
                    Log.e("APP", "STRING COMMAND " + m_string_command);
                    Log.e("APP", "STRING ARGUMENT " + m_string_argument);
                    // Good command format
                    parseCommand();
                    byte[] data_bytes = m_string.getBytes();
                    mThingySdkManager.sendCommandData(mDevice, data_bytes);
                    Toast msg = Toast.makeText(getContext(), "send data", Toast.LENGTH_LONG);
                    msg.show();
                }else{
                    // Wrong command format
                    Toast msg = Toast.makeText(getContext(), "WRONG FORMAT", Toast.LENGTH_SHORT);
                    msg.setGravity(Gravity.TOP, 0, 0);
                    msg.show();
                }
            }
        });

        mTapCount = rootView.findViewById(R.id.tap_count);
        mTapDirection = rootView.findViewById(R.id.tap_direction);
        mOrientation = rootView.findViewById(R.id.orientation);
        mPedometerSteps = rootView.findViewById(R.id.step_count);
        mPedometerDuration = rootView.findViewById(R.id.duration);
        mHeading = rootView.findViewById(R.id.heading);
        mHeadingDirection = rootView.findViewById(R.id.heading_direction);

        mOrientation.setText(ThingyUtils.PORTRAIT);

        mHeadingImage = rootView.findViewById(R.id.heading_image);
        mPortraitImage = rootView.findViewById(R.id.portrait_image);

        mLineChartGravityVector = rootView.findViewById(R.id.line_chart_gravity_vector);

        mIsConnected = isConnected(mDevice);
        if (Utils.checkIfVersionIsAboveJellyBean()) {
            mQuaternionToolbar = rootView.findViewById(R.id.card_toolbar_euler);
            mGlSurfaceView = rootView.findViewById(R.id.rajwali_surface);
            mRenderer = new Renderer(getActivity());
            mGlSurfaceView.setSurfaceRenderer(mRenderer);
            mRenderer.setConnectionState(mIsConnected);
            if (mDatabaseHelper.getNotificationsState(mDevice.getAddress(), DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
                mRenderer.setNotificationEnabled(true);
            }
        }

        if (mQuaternionToolbar != null) {
            mQuaternionToolbar.inflateMenu(R.menu.quaternion_card_menu);

            if (mDevice != null) {
                updateQuaternionCardOptionsMenu(mQuaternionToolbar.getMenu());
            }

            mQuaternionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        case R.id.action_quaternion_angles_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableQuaternionNotifications(item.isChecked());
                            break;
                    }
                    return true;
                }
            });
            loadFeatureDiscoverySequence();
        }

        final Toolbar motionToolbar = rootView.findViewById(R.id.card_toolbar_motion);
        if (motionToolbar != null) {
            motionToolbar.inflateMenu(R.menu.motion_card_menu);

            if (mDevice != null) {
                updateMotionCardOptionsMenu(motionToolbar.getMenu());
            }

            motionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        case R.id.action_about:
                            final MotionServiceInfoDialogFragment info = MotionServiceInfoDialogFragment.newInstance();
                            info.show(getChildFragmentManager(), null);
                            break;
                        case R.id.action_pedometer_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enablePedometerNotifications(item.isChecked());
                            break;
                        case R.id.action_tap_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableTapNotifications(item.isChecked());
                            break;
                        case R.id.action_heading_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableHeadingNotifications(item.isChecked());
                            break;
                        case R.id.action_orientation_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableOrientationNotifications(item.isChecked());
                            break;
                    }
                    return true;
                }
            });
        }

        final Toolbar gravityToolbar = rootView.findViewById(R.id.card_toolbar_gravity);
        if (gravityToolbar != null) {
            gravityToolbar.inflateMenu(R.menu.gravity_card_menu);

            if (mDevice != null) {
                updateGravityCardOptionsMenu(gravityToolbar.getMenu());
            }

            gravityToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        case R.id.action_gravity_vector_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableGravityVectorNotifications(item.isChecked());
                            break;
                    }
                    return true;
                }
            });
        }

        prepareGravityVectorChart();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mDevice);
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        mIsFragmentAttached = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onPause();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsFragmentAttached = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        ThingyListenerHelper.unregisterThingyListener(getContext(), mThingyListener);
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
    }

    @Override
    public void onNothingSelected() {

    }

    private boolean isConnected(final BluetoothDevice device) {
        if (mThingySdkManager != null) {
            final List<BluetoothDevice> connectedDevices = mThingySdkManager.getConnectedDevices();
            for (BluetoothDevice dev : connectedDevices) {
                if (device.getAddress().equals(dev.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void prepareGravityVectorChart() {
        if (!mLineChartGravityVector.isEmpty()) {
            mLineChartGravityVector.clearValues();
        }
        //mLineChartGravityVector.setDescription(getString(R.string.title_gravity_vector));
        mLineChartGravityVector.setDescription("");
        mLineChartGravityVector.setTouchEnabled(true);
        mLineChartGravityVector.setVisibleXRangeMinimum(5);
        mLineChartGravityVector.setVisibleXRangeMaximum(5);
        // enable scaling and dragging
        mLineChartGravityVector.setDragEnabled(true);
        mLineChartGravityVector.setPinchZoom(true);
        mLineChartGravityVector.setScaleEnabled(true);
        mLineChartGravityVector.setAutoScaleMinMaxEnabled(true);
        mLineChartGravityVector.setDrawGridBackground(false);
        mLineChartGravityVector.setBackgroundColor(Color.WHITE);
        /*final ChartMarker marker = new ChartMarker(getActivity(), R.layout.marker_layout_temperature);
        mLineChartGravityVector.setMarkerView(marker);*/

        LineData data = new LineData();
        data.setValueFormatter(new GravityVectorChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        mLineChartGravityVector.setData(data);

        Legend legend = mLineChartGravityVector.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = mLineChartGravityVector.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = mLineChartGravityVector.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new GravityVectorYValueFormatter());
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(0); //(-10f)
        leftAxis.setAxisMaxValue(3500);  //(10f)
        leftAxis.setLabelCount(3, false); //
        leftAxis.setDrawZeroLine(true);

        YAxis rightAxis = mLineChartGravityVector.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet[] createGravityVectorDataSet() {
        final LineDataSet[] lineDataSets = new LineDataSet[8];
        LineDataSet lineDataSet1 = new LineDataSet(null, "FSR1");
        lineDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet1.setColor(ContextCompat.getColor(requireContext(), R.color.red));
        lineDataSet1.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet1.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSet1.setDrawValues(false);
        lineDataSet1.setDrawCircles(true);
        lineDataSet1.setDrawCircleHole(false);
        lineDataSet1.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet1.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[0] = lineDataSet1;

        LineDataSet lineDataSet2 = new LineDataSet(null, "FSR2");
        lineDataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet2.setColor(ContextCompat.getColor(requireContext(), R.color.green));
        lineDataSet2.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet2.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSet2.setDrawValues(false);
        lineDataSet2.setDrawCircles(true);
        lineDataSet2.setDrawCircleHole(false);
        lineDataSet2.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet2.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[1] = lineDataSet2;

        LineDataSet lineDataSet3 = new LineDataSet(null, "FSR3");
        lineDataSet3.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet3.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
        lineDataSet3.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet3.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSet3.setDrawValues(false);
        lineDataSet3.setDrawCircles(true);
        lineDataSet3.setDrawCircleHole(false);
        lineDataSet3.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet3.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[2] = lineDataSet3;

        LineDataSet lineDataSet4 = new LineDataSet(null, "FSR4");
        lineDataSet4.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet4.setColor(ContextCompat.getColor(requireContext(), R.color.black));
        lineDataSet4.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet4.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSet4.setDrawValues(false);
        lineDataSet4.setDrawCircles(true);
        lineDataSet4.setDrawCircleHole(false);
        lineDataSet4.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet4.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[3] = lineDataSet4;

        LineDataSet lineDataSet5= new LineDataSet(null, "FSR5");
        lineDataSet5.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet5.setColor(ContextCompat.getColor(requireContext(), R.color.black));
        lineDataSet5.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet5.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSet5.setDrawValues(false);
        lineDataSet5.setDrawCircles(true);
        lineDataSet5.setDrawCircleHole(false);
        lineDataSet5.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet5.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[4] = lineDataSet5;

        LineDataSet lineDataSet6 = new LineDataSet(null, "FSR6");
        lineDataSet6.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet6.setColor(ContextCompat.getColor(requireContext(), R.color.black));
        lineDataSet6.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet6.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSet6.setDrawValues(false);
        lineDataSet6.setDrawCircles(true);
        lineDataSet6.setDrawCircleHole(false);
        lineDataSet6.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet6.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[5] = lineDataSet6;

        LineDataSet lineDataSet7 = new LineDataSet(null, "FSR7");
        lineDataSet7.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet7.setColor(ContextCompat.getColor(requireContext(), R.color.black));
        lineDataSet7.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet7.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSet7.setDrawValues(false);
        lineDataSet7.setDrawCircles(true);
        lineDataSet7.setDrawCircleHole(false);
        lineDataSet7.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet7.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[6] = lineDataSet7;

        LineDataSet lineDataSet8 = new LineDataSet(null, "FSR8");
        lineDataSet8.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet8.setColor(ContextCompat.getColor(requireContext(), R.color.black));
        lineDataSet8.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet8.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSet8.setDrawValues(false);
        lineDataSet8.setDrawCircles(true);
        lineDataSet8.setDrawCircleHole(false);
        lineDataSet8.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet8.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[7] = lineDataSet8;

        return lineDataSets;
    }

    private void addGravityVectorEntry_float(final float gravityVector1, final float gravityVector2, final float gravityVector3, final float gravityVector4, final float gravityVector5, final float gravityVector6, final float gravityVector7, final float gravityVector8) {
        LineData data = mLineChartGravityVector.getData();

        if (data != null) {
            ILineDataSet set1 = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            ILineDataSet set3 = data.getDataSetByIndex(2);
            ILineDataSet set4 = data.getDataSetByIndex(3);
            ILineDataSet set5 = data.getDataSetByIndex(4);
            ILineDataSet set6 = data.getDataSetByIndex(5);
            ILineDataSet set7 = data.getDataSetByIndex(6);
            ILineDataSet set8 = data.getDataSetByIndex(7);

            if (set1 == null || set2 == null || set3 == null || set4 == null || set5 == null || set6 == null || set7 == null || set8 == null) {
                final LineDataSet[] dataSets = createGravityVectorDataSet();
                set1 = dataSets[0];
                set2 = dataSets[1];
                set3 = dataSets[2];
                set4 = dataSets[3];
                set5 = dataSets[4];
                set6 = dataSets[5];
                set7 = dataSets[6];
                set8 = dataSets[7];
                data.addDataSet(set1);
                data.addDataSet(set2);
                data.addDataSet(set3);
                data.addDataSet(set4);
                data.addDataSet(set5);
                data.addDataSet(set6);
                data.addDataSet(set7);
                data.addDataSet(set8);
            }

            //data.addXValue(ThingyUtils.TIME_FORMAT_PEDOMETER.format(new Date()));
            data.addXValue("");
            data.addEntry(new Entry(gravityVector1, set1.getEntryCount()), 0);
            data.addEntry(new Entry(gravityVector2, set2.getEntryCount()), 1);
            data.addEntry(new Entry(gravityVector3, set3.getEntryCount()), 2);
            data.addEntry(new Entry(gravityVector4, set4.getEntryCount()), 3);
            data.addEntry(new Entry(gravityVector5, set5.getEntryCount()), 4);
            data.addEntry(new Entry(gravityVector6, set6.getEntryCount()), 5);
            data.addEntry(new Entry(gravityVector7, set7.getEntryCount()), 6);
            data.addEntry(new Entry(gravityVector8, set8.getEntryCount()), 7);

            mLineChartGravityVector.notifyDataSetChanged();
            mLineChartGravityVector.setVisibleXRangeMaximum(10);
            mLineChartGravityVector.moveViewToX(data.getXValCount() - 11);
        }
    }

    private void addGravityVectorEntry_head_float(final float gravityVector1, final float gravityVector2) {
        LineData data = mLineChartGravityVector.getData();

        if (data != null) {
            ILineDataSet set1 = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);

            if (set1 == null || set2 == null) {
                final LineDataSet[] dataSets = createGravityVectorDataSet();
                set1 = dataSets[0];
                set2 = dataSets[1];
                data.addDataSet(set1);
                data.addDataSet(set2);
            }

            //data.addXValue(ThingyUtils.TIME_FORMAT_PEDOMETER.format(new Date()));
            data.addXValue("");
            data.addEntry(new Entry(gravityVector1, set1.getEntryCount()), 0);
            data.addEntry(new Entry(gravityVector2, set2.getEntryCount()), 1);

            mLineChartGravityVector.notifyDataSetChanged();
            mLineChartGravityVector.setVisibleXRangeMaximum(10);
            mLineChartGravityVector.moveViewToX(data.getXValCount() - 11);
        }
    }

    private void addGravityVectorEntry(final short gravityVector1, final short gravityVector2, final short gravityVector3, final short gravityVector4, final short gravityVector5, final short gravityVector6, final short gravityVector7, final short gravityVector8) {
        LineData data = mLineChartGravityVector.getData();

        if (data != null) {
            ILineDataSet set1 = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            ILineDataSet set3 = data.getDataSetByIndex(2);
            ILineDataSet set4 = data.getDataSetByIndex(3);
            ILineDataSet set5 = data.getDataSetByIndex(4);
            ILineDataSet set6 = data.getDataSetByIndex(5);
            ILineDataSet set7 = data.getDataSetByIndex(6);
            ILineDataSet set8 = data.getDataSetByIndex(7);

            if (set1 == null || set2 == null || set3 == null || set4 == null || set5 == null || set6 == null || set7 == null || set8 == null) {
                final LineDataSet[] dataSets = createGravityVectorDataSet();
                set1 = dataSets[0];
                set2 = dataSets[1];
                set3 = dataSets[2];
                set4 = dataSets[3];
                set5 = dataSets[4];
                set6 = dataSets[5];
                set7 = dataSets[6];
                set8 = dataSets[7];
                data.addDataSet(set1);
                data.addDataSet(set2);
                data.addDataSet(set3);
                data.addDataSet(set4);
                data.addDataSet(set5);
                data.addDataSet(set6);
                data.addDataSet(set7);
                data.addDataSet(set8);
            }

            //data.addXValue(ThingyUtils.TIME_FORMAT_PEDOMETER.format(new Date()));
            data.addXValue("");
            data.addEntry(new Entry(gravityVector1, set1.getEntryCount()), 0);
            data.addEntry(new Entry(gravityVector2, set2.getEntryCount()), 1);
            data.addEntry(new Entry(gravityVector3, set3.getEntryCount()), 2);
            data.addEntry(new Entry(gravityVector4, set4.getEntryCount()), 3);
            data.addEntry(new Entry(gravityVector5, set5.getEntryCount()), 4);
            data.addEntry(new Entry(gravityVector6, set6.getEntryCount()), 5);
            data.addEntry(new Entry(gravityVector7, set7.getEntryCount()), 6);
            data.addEntry(new Entry(gravityVector8, set8.getEntryCount()), 7);

            mLineChartGravityVector.notifyDataSetChanged();
            mLineChartGravityVector.setVisibleXRangeMaximum(10);
            mLineChartGravityVector.moveViewToX(data.getXValCount() - 11);
        }
    }

    private void addGravityVectorEntry_head(final short gravityVector1, final short gravityVector2) {
        Log.e("APP", "addGravityVectorEntry_head " + gravityVector1 + gravityVector2);
        LineData data = mLineChartGravityVector.getData();

        if (data != null) {
            ILineDataSet set1 = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);

            if (set1 == null || set2 == null ) {
                final LineDataSet[] dataSets = createGravityVectorDataSet();
                set1 = dataSets[0];
                set2 = dataSets[1];
                data.addDataSet(set1);
                data.addDataSet(set2);

            }

            //data.addXValue(ThingyUtils.TIME_FORMAT_PEDOMETER.format(new Date()));
            data.addXValue("");
            data.addEntry(new Entry(gravityVector1, set1.getEntryCount()), 0);
            data.addEntry(new Entry(gravityVector2, set2.getEntryCount()), 1);

            mLineChartGravityVector.notifyDataSetChanged();
            mLineChartGravityVector.setVisibleXRangeMaximum(10);
            mLineChartGravityVector.moveViewToX(data.getXValCount() - 11);
        }
    }

    private void updateLedColor_voltage(final short gravityVector1, final short gravityVector2, final short gravityVector3, final short gravityVector4) {
        if(gravityVector1 > ThresholdVoltageHigh){
            mRgbDrawable1.setColor(Color.RED);
        }else if (gravityVector1 > ThresholdVoltageLow && gravityVector1 < ThresholdVoltageHigh){
            mRgbDrawable1.setColor(Color.MAGENTA);
        }else{
            mRgbDrawable1.setColor(Color.GREEN);
        }

        if(gravityVector2 > ThresholdVoltageHigh){
            mRgbDrawable2.setColor(Color.RED);
        }else if (gravityVector2 > ThresholdVoltageLow && gravityVector2 < ThresholdVoltageHigh){
            mRgbDrawable2.setColor(Color.MAGENTA);
        }else{
            mRgbDrawable2.setColor(Color.GREEN);
        }

        if(gravityVector3 > ThresholdVoltageHigh){
            mRgbDrawable3.setColor(Color.RED);
        }else if (gravityVector3 > ThresholdVoltageLow && gravityVector3 < ThresholdVoltageHigh){
            mRgbDrawable3.setColor(Color.MAGENTA);
        }else{
            mRgbDrawable3.setColor(Color.GREEN);
        }

        if(gravityVector4 > ThresholdVoltageHigh){
            mRgbDrawable4.setColor(Color.RED);
        }else if (gravityVector4 > ThresholdVoltageLow && gravityVector4 < ThresholdVoltageHigh){
            mRgbDrawable4.setColor(Color.MAGENTA);
        }else{
            mRgbDrawable4.setColor(Color.GREEN);
        }

        if( (gravityVector1 > ThresholdVoltageHigh) ||
                (gravityVector2 > ThresholdVoltageHigh) ||
                (gravityVector3 > ThresholdVoltageHigh) ||
                (gravityVector4 > ThresholdVoltageHigh) ){
            mRgbDrawableResult.setColor(Color.RED);
        }else if(  (gravityVector1 > ThresholdVoltageLow && gravityVector1 < ThresholdVoltageHigh) ||
                        (gravityVector2 > ThresholdVoltageLow && gravityVector2 < ThresholdVoltageHigh) ||
                        (gravityVector3 > ThresholdVoltageLow && gravityVector3 < ThresholdVoltageHigh) ||
                        (gravityVector4 > ThresholdVoltageLow && gravityVector4 < ThresholdVoltageHigh) ){
            mRgbDrawableResult.setColor(Color.MAGENTA);
        }else{
            mRgbDrawableResult.setColor(Color.GREEN);
        }
    }

    private void updateLedColor_force(final float gravityVector1, final float gravityVector2, final float gravityVector3, final float gravityVector4) {

    }


    private void updateLedColor_force_calculated(final float gravityVector1, final float gravityVector2, final float gravityVector3, final float gravityVector4) {

    }


    class GravityVectorYValueFormatter implements YAxisValueFormatter {
        private DecimalFormat mFormat;

        GravityVectorYValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0.00");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value); //
        }
    }

    class GravityVectorChartValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        GravityVectorChartValueFormatter() {
            mFormat = new DecimalFormat("#0.00");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    private void updateQuaternionCardOptionsMenu(final Menu eulerCardMotion) {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
            eulerCardMotion.findItem(R.id.action_quaternion_angles_notification).setChecked(true);
        } else {
            eulerCardMotion.findItem(R.id.action_quaternion_angles_notification).setChecked(false);
        }
    }

    private void updateMotionCardOptionsMenu(final Menu motionCardMenu) {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_PEDOMETER)) {
            motionCardMenu.findItem(R.id.action_pedometer_notification).setChecked(true);
        } else {
            motionCardMenu.findItem(R.id.action_pedometer_notification).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_TAP)) {
            motionCardMenu.findItem(R.id.action_tap_notification).setChecked(true);
        } else {
            motionCardMenu.findItem(R.id.action_tap_notification).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HEADING)) {
            motionCardMenu.findItem(R.id.action_heading_notification).setChecked(true);
        } else {
            motionCardMenu.findItem(R.id.action_heading_notification).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_ORIENTATION)) {
            motionCardMenu.findItem(R.id.action_orientation_notification).setChecked(true);
        } else {
            motionCardMenu.findItem(R.id.action_orientation_notification).setChecked(false);
        }
    }

    private void updateGravityCardOptionsMenu(final Menu gravityCardMotion) {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR)) {
            gravityCardMotion.findItem(R.id.action_gravity_vector_notification).setChecked(true);
        } else {
            gravityCardMotion.findItem(R.id.action_gravity_vector_notification).setChecked(false);
        }
    }

    @SuppressWarnings("unused")
    public void enableRawDataNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableRawDataNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_RAW_DATA);
    }

    private void enableOrientationNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableOrientationNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_ORIENTATION);
    }

    private void enableHeadingNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableHeadingNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HEADING);
    }

    private void enableTapNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableTapNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_TAP);
    }

    private void enablePedometerNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enablePedometerNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_PEDOMETER);
    }

    private void enableQuaternionNotifications(final boolean notificationEnabled) {
        mRenderer.setNotificationEnabled(notificationEnabled);
        mThingySdkManager.enableQuaternionNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION);
    }

    private void enableGravityVectorNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableGravityVectorNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR);
    }

    @SuppressWarnings("unused")
    private void enableEulerNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableEulerNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_EULER);
    }

    private void enableCommandNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableCommandNotifications(mDevice, notificationEnabled);
        //mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_FSR_DATA);
    }

    private void enableFsrDataNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableFsrDataNotifications(mDevice, notificationEnabled);
        //mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_FSR_DATA);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadFeatureDiscoverySequence() {
        if (!Utils.checkIfSequenceIsCompleted(requireContext(), Utils.INITIAL_MOTION_TUTORIAL)) {

            final SpannableString desc = new SpannableString(getString(R.string.start_stop_motion_sensors));

            final TapTargetSequence sequence = new TapTargetSequence(requireActivity());
            sequence.continueOnCancel(true);
            sequence.targets(
                    TapTarget.forToolbarOverflow(mQuaternionToolbar, desc).
                            dimColor(R.color.grey).
                            outerCircleColor(R.color.accent).id(0)).listener(new TapTargetSequence.Listener() {
                @Override
                public void onSequenceFinish() {
                    Utils.saveSequenceCompletion(requireContext(), Utils.INITIAL_MOTION_TUTORIAL);
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {

                }
            }).start();
        }
    }

    private void parseCommand() {
        switch(m_string_command){
            case "RSTART":
            case "START":
                enableQuaternionNotifications(true);
                Log.e("APP", "enable cccd quaternions");
                //enable FSR cccd AFTER others, if not, timers problesm happened
                enableFsrDataNotifications(true);
                Log.e("APP", "enable cccd FSR");

                //changement d'echelle
                if(m_string_argument!= null){
                    if(m_string_argument.charAt(0) == 'V'){
                        YAxis leftAxis = mLineChartGravityVector.getAxisLeft();
                        leftAxis.setTextColor(Color.BLACK);
                        leftAxis.setValueFormatter(new GravityVectorYValueFormatter());
                        leftAxis.setDrawLabels(true);
                        leftAxis.setAxisMinValue(0); //(-10f)
                        leftAxis.setAxisMaxValue(3500);  //(10f)
                    }else{
                        YAxis leftAxis = mLineChartGravityVector.getAxisLeft();
                        leftAxis.setTextColor(Color.BLACK);
                        leftAxis.setValueFormatter(new GravityVectorYValueFormatter());
                        leftAxis.setDrawLabels(true);
                        leftAxis.setAxisMinValue(0); //(-10f)
                        leftAxis.setAxisMaxValue(100);  //(10f)
                    }
                }else{
                    YAxis leftAxis = mLineChartGravityVector.getAxisLeft();
                    leftAxis.setTextColor(Color.BLACK);
                    leftAxis.setValueFormatter(new GravityVectorYValueFormatter());
                    leftAxis.setDrawLabels(true);
                    leftAxis.setAxisMinValue(0); //(-10f)
                    leftAxis.setAxisMaxValue(100);  //(10f)
                }

                break;
            default:
                enableQuaternionNotifications(false);
                Log.e("APP", "disable cccd quaternions");
                enableFsrDataNotifications(false);
                Log.e("APP", "disable cccd FSR");
        }
    }
}