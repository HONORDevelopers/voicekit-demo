/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.hihonor.voicekitdemo.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.hihonor.mcs.intelligence.voice.SupportListener;
import com.hihonor.mcs.intelligence.voice.Voices;
import com.hihonor.mcs.intelligence.voice.asr.interfaces.AsrListener;
import com.hihonor.mcs.intelligence.voice.asr.interfaces.AsrRecognizer;
import com.hihonor.mcs.intelligence.voice.asr.result.AsrResult;
import com.hihonor.voicekitdemo.R;
import com.hihonor.voicekitdemo.utils.SingleThreadPool;

/**
 * 基础语音能力示例
 * 使用步骤：
 * 1、创建asr对象
 * 2、创建asr回调对象
 * 3、设置初始化参数、使用回调对象初始化asr引擎
 * 4、初始化成功回调之后，调用开始识别startRecognize
 * 5、写入音频流，调用writeAudio
 * 6、停止写入音频流，停止/取消识别（与startRecognize对应）
 * 7、循环使用步骤4-6
 * 8、销毁引擎
 *
 * @since 2024-07-18
 */
public class AsrDemoActivity extends BaseDemoActivity {
    private static final String TAG = AsrDemoActivity.class.getSimpleName();

    private static final int SAMPLE_RATE_IN_HZ = 16000;

    private Button btnInit;

    private Button btnStartRecognize;

    private Button btnStartRecord;

    private Button btnStopRecord;

    private Button btnStopRecognize;

    private Button btnCancelRecognize;

    private Button btnDestroy;

    private AsrRecognizer asrRecognizer;

    private AsrListener asrListener;

    // 指定最小录音缓冲区大小
    private int mBufferSizeInBytes;

    // 录音对象
    private AudioRecord audioRecord;

    private boolean isRecording = false;

    private volatile boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_asr_demo);
        initView();
        initClickListener();
    }

    private void initView() {
        btnInit = findViewById(R.id.asr_init);
        btnStartRecognize = findViewById(R.id.asr_start_recognize);
        btnStartRecord = findViewById(R.id.asr_start_record);
        btnStopRecord = findViewById(R.id.asr_stop_record);
        btnStopRecognize = findViewById(R.id.asr_stop_recognize);
        btnCancelRecognize = findViewById(R.id.asr_cancel_recognize);
        btnDestroy = findViewById(R.id.asr_destroy);
        tvShowResult = findViewById(R.id.asr_show_result);
    }

    private void initClickListener() {
        btnInit.setOnClickListener(view -> init());
        btnStartRecognize.setOnClickListener(view -> startRecognize());
        btnStartRecord.setOnClickListener(view -> startRecord());
        btnStopRecord.setOnClickListener(view -> stopRecord());

        // 调用stopRecognize/cancelRecognize之后如果想再次开启语音识别，需要先调用startRecognize
        btnStopRecognize.setOnClickListener(view -> stopRecognize());
        btnCancelRecognize.setOnClickListener(view -> cancelRecognize());
        btnDestroy.setOnClickListener(view -> destroy());
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        destroy();
        super.onDestroy();
    }

    private void init() {
        if (asrRecognizer == null) {
            Log.d(TAG, "getAsrClient");
            asrRecognizer = Voices.getAsrClient(getApplicationContext());
        }
        isInitialized = false;
        initAsrListener();
        // 使用初始化参数、回调对象初始化ASR引擎
        asrRecognizer.init(new SupportListener() {
            /**
             * 初始化成功
             */
            @Override
            public void onSupport() {
                // 初始化成功之后，需要调用startRecognize开始识别，并开始录音调用writeAudio写入音频流数据
                isInitialized = true;
                Log.d(TAG, "onSupport");
                showToast("Init Success");
            }

            @Override
            public void onError(int code, String msg) {
                // 初始化失败
                isInitialized = false;
                Log.w(TAG, "SupportListener onError, code: " + code + ", msg: " + msg);
                showToast("Init onError");
            }
        });
    }

    private void initAsrListener() {
        asrListener = new AsrListener() {
            /**
             * 初始化成功回调
             */
            @Override
            public void onReady() {
                Log.d(TAG, "onReady");
            }

            /**
             * 用户开始说话
             */
            @Override
            public void onSpeechStart() {
                Log.d(TAG, "onSpeechStart");
            }

            /**
             * 音量发生变化
             *
             * @param value 音量值
             */
            @Override
            public void onRmsChanged(float value) {
                Log.d(TAG, "onRmsChanged, value:" + value);
            }

            /**
             * 用户停止说话
             */
            @Override
            public void onSpeechEnd() {
                Log.d(TAG, "onSpeechEnd");
            }

            /**
             * 中间态结果回调
             *
             * @param asrResult 中间态结果
             */
            @Override
            public void onPartialResult(AsrResult asrResult) {
                if (asrResult != null) {
                    String text = asrResult.getText();
                    Log.d(TAG, "partial result is " + text);
                    showText("PartialResult:" + text);
                } else {
                    Log.w(TAG, "partial result is null");
                }
            }

            /**
             * 识别结果回调
             *
             * @param asrResult 识别结果
             */
            @Override
            public void onResult(AsrResult asrResult) {
                if (asrResult != null) {
                    String text = asrResult.getText();
                    Log.d(TAG, "final result is " + text);
                    showText("FinalResult:" + text);
                } else {
                    Log.w(TAG, "final result is null");
                }
            }

            /**
             * 错误信息回调
             *
             * @param code 错误码
             * @param msg 错误信息
             */
            @Override
            public void onError(int code, String msg) {
                Log.w(TAG, "AsrListener onError, code:" + code + ", msg:" + msg);
            }
        };
    }

    private void startRecognize() {
        if (asrRecognizer != null && isInitialized) {
            Log.d(TAG, "startRecognize");
            asrRecognizer.startRecognize(asrListener);
        } else {
            Log.i(TAG, "asrRecognizer is null");
            showToast("Not Init!!!");
        }
    }

    /**
     * 停止识别，立即回调最终结果
     */
    private void stopRecognize() {
        stopRecord();
        if (asrRecognizer != null && isInitialized) {
            Log.d(TAG, "asrRecognizer stopRecognize");
            asrRecognizer.stopRecognize();
        } else {
            Log.i(TAG, "asrRecognizer is null");
        }
    }

    /**
     * 取消识别，无最终结果回调
     */
    private void cancelRecognize() {
        stopRecord();
        if (asrRecognizer != null && isInitialized) {
            Log.d(TAG, "asrRecognizer cancelRecognize");
            asrRecognizer.cancelRecognize();
        } else {
            Log.i(TAG, "asrRecognizer is null");
        }
    }

    /**
     * 开始录音
     */
    private void startRecord() {
        Log.d(TAG, "startRecord");
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            showToast("no RECORD_AUDIO permission!");
            return;
        }
        if (asrRecognizer == null || !isInitialized) {
            showToast("Not Init!!!");
            return;
        }
        if (audioRecord == null) {
            initAudioRecord();
        }
        if (audioRecord != null) {
            isRecording = true;
            audioRecord.startRecording();
            SingleThreadPool.getInstance().execute(this::loopWriteAudio, "AsrRecord");
        } else {
            Log.w(TAG, "audioRecord is null");
        }
    }

    /**
     * 循环从AudioRecord读取音频流，并写入到Asr引擎
     */
    private void loopWriteAudio() {
        while (isRecording) {
            byte[] audioData = new byte[mBufferSizeInBytes];
            int read = audioRecord.read(audioData, 0, audioData.length);
            if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                // 写入音频流
                if (asrRecognizer != null && isInitialized) {
                    asrRecognizer.writeAudio(audioData, audioData.length);
                } else {
                    Log.w(TAG, "asrRecognizer is null");
                }
            } else {
                Log.e(TAG, "AudioRecord ERROR_INVALID_OPERATION");
            }
        }
    }

    /**
     * 创建默认 audioRecord
     * 音频参数请参考本设置
     */
    private void initAudioRecord() {
        // buffer size in bytes 1280
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
        Log.d(TAG, "MinBufferSize:" + mBufferSizeInBytes);
        if (mBufferSizeInBytes == AudioRecord.ERROR || mBufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
            Log.w(TAG, "Audio buffer can't initialize!");
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "No RECORD_AUDIO permission");
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, mBufferSizeInBytes);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.w(TAG, "Audio Record state error");
            return;
        }
        Log.d(TAG, "Record init okay");
    }

    private void stopRecord() {
        isRecording = false;
        if (audioRecord != null) {
            Log.d(TAG, "stopRecord");
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        } else {
            Log.i(TAG, "audioRecord is null");
        }
    }

    /**
     * 销毁AsrRecognizer
     */
    private void destroy() {
        stopRecord();
        if (asrRecognizer != null) {
            // 销毁引擎，释放资源
            Log.d(TAG, "asrRecognizer destroy");
            asrRecognizer.destroy();
            asrRecognizer = null;
            asrListener = null;
        } else {
            Log.i(TAG, "asrRecognizer already null");
        }
    }
}