/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.hihonor.voicekitdemo.demo;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 基础Activity类，公共功能
 *
 * @since 2024-07-18
 */
public class BaseDemoActivity extends Activity {
    /**
     * 用于展示识别结果的TextView
     */
    protected TextView tvShowResult;

    /**
     * 屏幕显示文本
     *
     * @param result 结果文本
     */
    protected void showText(String result) {
        runOnUiThread(() -> {
            if (tvShowResult != null) {
                tvShowResult.setText(result);
            }
        });
    }

    /**
     * Toast显示文本
     *
     * @param text 信息文本
     */
    protected void showToast(String text) {
        runOnUiThread(() -> Toast.makeText(BaseDemoActivity.this, text, Toast.LENGTH_SHORT).show());
    }
}
