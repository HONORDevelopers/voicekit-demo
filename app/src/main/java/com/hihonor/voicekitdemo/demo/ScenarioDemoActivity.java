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
import com.hihonor.mcs.intelligence.voice.asr.AsrErrorCode;
import com.hihonor.mcs.intelligence.voice.asr.ScenarioOption;
import com.hihonor.mcs.intelligence.voice.asr.interfaces.AsrScenarioListener;
import com.hihonor.mcs.intelligence.voice.asr.interfaces.AsrScenarioRecognizer;
import com.hihonor.mcs.intelligence.voice.asr.result.AsrCommandResult;
import com.hihonor.voicekitdemo.R;
import com.hihonor.voicekitdemo.utils.SingleThreadPool;

/**
 * 场景化语音能力示例类
 * 使用步骤：
 * 1、创建AsrScenarioClient对象
 * 2、创建AsrScenarioListener回调对象
 * 3、设置初始化参数、使用回调对象初始化AsrScenario引擎，注册要监听的关键词
 * 4、初始化成功回调之后，调用开始识别startRecognize
 * 5、写入音频流，调用writeAudio
 * 6、当用户说出被监听的关键词时，回调关键词内容
 * 7、使用完毕，销毁引擎
 *
 * @since 2024-07-18
 */
public class ScenarioDemoActivity extends BaseDemoActivity {
    private static final String TAG = ScenarioDemoActivity.class.getSimpleName();

    private static final String COMMAND_KEYWORD_LIST = "你好；拍照；茄子；接听电话；挂断电话；关闭闹钟";

    private static final int SAMPLE_RATE_IN_HZ = 16000;

    private Button btnInit;

    private Button btnStartRecognize;

    private Button btnWriteAudio;

    private Button btnStopWrite;

    private Button btnDestroy;

    private AsrScenarioRecognizer asrScenarioRecognizer;

    private AsrScenarioListener asrScenarioListener;

    // 指定最小录音缓冲区大小
    private int bufferSizeInBytes;

    // 录音对象
    private AudioRecord audioRecord;

    private boolean isRecording = false;

    private volatile boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_scenario_demo);
        initView();
        initClickListener();
    }

    private void initView() {
        btnInit = findViewById(R.id.scenario_init);
        btnStartRecognize = findViewById(R.id.scenario_start_recognize);
        btnWriteAudio = findViewById(R.id.scenario_start_record);
        btnStopWrite = findViewById(R.id.scenario_stop_record);
        btnDestroy = findViewById(R.id.scenario_destroy);
        tvShowResult = findViewById(R.id.scenario_show_result);
    }

    private void initClickListener() {
        btnInit.setOnClickListener(view -> init());
        btnStartRecognize.setOnClickListener(view -> startRecognize());
        btnWriteAudio.setOnClickListener(view -> startRecord());
        btnStopWrite.setOnClickListener(view -> stopRecord());
        btnDestroy.setOnClickListener(view -> destroy());
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        destroy();
        super.onDestroy();
    }

    private void init() {
        if (asrScenarioRecognizer == null) {
            asrScenarioRecognizer = Voices.getAsrScenarioClient(getApplicationContext());
        }
        isInitialized = false;
        ScenarioOption scenarioOption = new ScenarioOption.Builder().setCommands(COMMAND_KEYWORD_LIST).build();
        initAsrScenarioListener();
        // 使用初始化参数、回调对象初始化ASR引擎
        asrScenarioRecognizer.init(scenarioOption, new SupportListener() {
            @Override
            public void onSupport() {
                if (asrScenarioRecognizer != null) {
                    // 初始化成功之后，需要调用startRecognize开始识别，并开始录音调用writeAudio写入音频流数据
                    isInitialized = true;
                    Log.d(TAG, "startRecognize");
                    showToast("Init Success");
                }
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

    /**
     * 创建ASR回调对象
     */
    private void initAsrScenarioListener() {
        asrScenarioListener = new AsrScenarioListener() {
            @Override
            public void onReady() {
                // 场景词引擎成功回调
                Log.i(TAG, "onReady");
            }

            @Override
            public void onResult(AsrCommandResult result) {
                // 获取场景词识别结果
                if (result != null) {
                    String text = result.getKeyWords();
                    Log.d(TAG, "final result is " + text);
                    showText("KeyWord:" + text);
                } else {
                    Log.w(TAG, "onResult is null");
                }
            }

            @Override
            public void onError(int code, String msg) {
                // 获取错误回调
                Log.w(TAG, "AsrScenarioListener onError, code: " + code + ", msg: " + msg);
                if (code == AsrErrorCode.ASR_PHRASE_ERROR) {
                    // 涉及到场景词识别底层引擎释放资源，报50006时调用destroy释放。
                    if (asrScenarioRecognizer != null) {
                        Log.d(TAG, "onError, call destroy");
                        asrScenarioRecognizer.destroy();
                        asrScenarioRecognizer = null;
                    } else {
                        Log.i(TAG, "asrScenarioRecognizer already null");
                    }
                }
            }
        };
    }

    private void startRecognize() {
        if (asrScenarioRecognizer != null && isInitialized) {
            Log.d(TAG, "startRecognize");
            asrScenarioRecognizer.startRecognize(asrScenarioListener);
        } else {
            Log.i(TAG, "asrRecognizer is null");
            showToast("Not Init!!!");
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
        if (asrScenarioRecognizer == null || !isInitialized) {
            showToast("Not Init!!!");
            return;
        }
        if (audioRecord == null) {
            initAudioRecord();
        }
        if (audioRecord != null) {
            isRecording = true;
            audioRecord.startRecording();
            SingleThreadPool.getInstance().execute(this::loopWriteAudio, "ScenarioRecord");
        } else {
            Log.w(TAG, "audioRecord is null");
        }
    }

    /**
     * 循环从AudioRecord读取音频流，并写入到AsrScenario引擎
     */
    private void loopWriteAudio() {
        while (isRecording) {
            byte[] audioData = new byte[bufferSizeInBytes];
            int read = audioRecord.read(audioData, 0, audioData.length);
            if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                // 写入音频流
                if (asrScenarioRecognizer != null && isInitialized) {
                    asrScenarioRecognizer.writeAudio(audioData, audioData.length);
                } else {
                    Log.w(TAG, "asrScenarioRecognizer is null");
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
        bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
        Log.d(TAG, "MinBufferSize:" + bufferSizeInBytes);
        if (bufferSizeInBytes == AudioRecord.ERROR || bufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
            Log.w(TAG, "Audio buffer can't initialize!");
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "No RECORD_AUDIO permission");
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.w(TAG, "Audio Record can't initialize!");
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
     * 销毁AsrScenarioRecognizer
     */
    private void destroy() {
        stopRecord();
        if (asrScenarioRecognizer != null) {
            // 销毁引擎，释放资源
            Log.d(TAG, "asrScenarioListener destroy");
            asrScenarioRecognizer.destroy();
            asrScenarioRecognizer = null;
            asrScenarioListener = null;
        } else {
            Log.i(TAG, "asrScenarioListener already null");
        }
    }
}