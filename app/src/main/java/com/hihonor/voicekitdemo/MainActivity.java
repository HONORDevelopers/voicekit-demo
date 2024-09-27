/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.hihonor.voicekitdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.hihonor.voicekitdemo.demo.AsrDemoActivity;
import com.hihonor.voicekitdemo.demo.NluDemoActivity;
import com.hihonor.voicekitdemo.demo.ScenarioDemoActivity;

/**
 * MainActivity
 *
 * @since 2024-07-18
 */
public class MainActivity extends Activity {
    @Override
    protected void onStart() {
        super.onStart();
        requestAudioPermissions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 基础语音能力
        findViewById(R.id.to_asr_demo).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AsrDemoActivity.class);
            startActivity(intent);
        });

        // 场景化语音能力
        findViewById(R.id.to_scenario_demo).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScenarioDemoActivity.class);
            startActivity(intent);
        });

        // 自然语言能力
        findViewById(R.id.to_nlu_demo).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NluDemoActivity.class);
            startActivity(intent);
        });
    }

    private void requestAudioPermissions() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, 1);
        }
    }
}