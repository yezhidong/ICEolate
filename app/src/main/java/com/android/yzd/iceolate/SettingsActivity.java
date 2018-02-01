package com.android.yzd.iceolate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;

import com.kyleduo.switchbutton.SwitchButton;

public class SettingsActivity extends AppCompatActivity {

    public static final String IS_SHESHI = "IS_SHESHI";

    private boolean isSheShi = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        isSheShi = (boolean) SharedPreferencesUtils.getParam(this, IS_SHESHI, true);

        SwitchButton sheShiButton = (SwitchButton) findViewById(R.id.sb_use_delay);
        sheShiButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isSheShi = false;
                } else {
                    isSheShi = true;
                }
                SharedPreferencesUtils.setParam(SettingsActivity.this, IS_SHESHI, isSheShi);
            }
        });
    }
}
