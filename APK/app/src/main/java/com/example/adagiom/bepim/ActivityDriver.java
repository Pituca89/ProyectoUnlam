package com.example.adagiom.bepim;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class ActivityDriver extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_training);

        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, int strength) {
                //mTextViewCoordinateRight.setText(
                //       String.format("x%03d:y%03d",
                //                joystickRight.getNormalizedX(),
                //                joystickRight.getNormalizedY())
                //);
            }
        });
    }

    @Override
    public void onBackPressed(){
       startActivity(new Intent(ActivityDriver.this,TabsActivity.class));
       finish();
    }
}
