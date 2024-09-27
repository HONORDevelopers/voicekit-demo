/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.hihonor.voicekitdemo.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hihonor.mcs.intelligence.voice.SupportListener;
import com.hihonor.mcs.intelligence.voice.Voices;
import com.hihonor.mcs.intelligence.voice.nlu.constant.NluConstants;
import com.hihonor.mcs.intelligence.voice.nlu.data.EntityResult;
import com.hihonor.mcs.intelligence.voice.nlu.data.WordsResult;
import com.hihonor.mcs.intelligence.voice.nlu.interfaces.NluProcessor;
import com.hihonor.voicekitdemo.R;
import com.hihonor.voicekitdemo.utils.GsonUtils;

import java.util.ArrayList;

/**
 * 自然语言能力示例类
 *
 * @since 2024-07-18
 */
public class NluDemoActivity extends BaseDemoActivity {
    private static final String TAG = NluDemoActivity.class.getSimpleName();

    private Button btnInit;

    private Button btnDestroy;

    private EditText etInputContent;

    private EditText etEntitySource;

    private Spinner spEntitySource;

    private Button btnSplit;

    private EditText etWordType;

    private Spinner spWordType;

    private CheckBox cbUrl;

    private CheckBox cbEmail;

    private CheckBox cbExpressNo;

    private CheckBox cbIdNo;

    private CheckBox cbFlightNo;

    private CheckBox cbPhoneNum;

    private CheckBox cbLocation;

    private Button btnEntity;

    private TextView mTvShowSplit;

    private TextView mTvShowEntity;

    private NluProcessor nluProcessor;

    private long wordType = NluConstants.TYPE_WORDS_LOW;

    private String entitySource = NluConstants.SOURCE_COPY;

    private volatile boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + android.os.Build.MANUFACTURER);
        setContentView(R.layout.activity_nlu_demo);
        iniView();
        initSpinner();
        initClickListener();
    }

    private void iniView() {
        btnInit = findViewById(R.id.nlu_init);
        btnDestroy = findViewById(R.id.nlu_destroy);
        etInputContent = findViewById(R.id.nlu_input_content_et);

        etWordType = findViewById(R.id.nlu_word_type_et);
        spWordType = findViewById(R.id.nlu_word_type_sp);
        btnSplit = findViewById(R.id.nlu_split);

        etEntitySource = findViewById(R.id.nlu_entity_source_et);
        spEntitySource = findViewById(R.id.nlu_entity_source_sp);
        cbUrl = findViewById(R.id.nlu_url_cb);
        cbEmail = findViewById(R.id.nlu_email_cb);
        cbExpressNo = findViewById(R.id.nlu_express_no_cb);
        cbIdNo = findViewById(R.id.nlu_id_no_cb);
        cbFlightNo = findViewById(R.id.nlu_flight_no_cb);
        cbPhoneNum = findViewById(R.id.nlu_phone_num_cb);
        cbLocation = findViewById(R.id.nlu_location_cb);
        btnEntity = findViewById(R.id.nlu_entity);

        mTvShowSplit = findViewById(R.id.nlu_show_split_tv);
        mTvShowSplit.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTvShowEntity = findViewById(R.id.nlu_show_entity_tv);
        mTvShowEntity.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void initSpinner() {
        // 分词粒度 下拉选择框
        String[] typeList = {String.valueOf(NluConstants.TYPE_WORDS_LOW), String.valueOf(NluConstants.TYPE_WORDS_MIXED),
            String.valueOf(NluConstants.TYPE_WORDS_HIGHT)};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWordType.setAdapter(typeAdapter);
        spWordType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                etWordType.setText(typeList[i]);
                wordType = parseWordType();
                Log.d(TAG, "onItemSelected, WordType:" + wordType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected, WordType");
            }
        });

        // 实体来源 下拉选择框
        String[] sourceList = {NluConstants.SOURCE_COPY, NluConstants.SOURCE_OCR};
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sourceList);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEntitySource.setAdapter(sourceAdapter);
        spEntitySource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                etEntitySource.setText(sourceList[i]);
                entitySource = sourceList[i];
                Log.d(TAG, "onItemSelected, EntitySource:" + entitySource);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected, EntitySource ");
            }
        });
    }

    private void initClickListener() {
        btnInit.setOnClickListener(view -> init());
        btnDestroy.setOnClickListener(view -> destroy());
        btnSplit.setOnClickListener(view -> splitWords());
        btnEntity.setOnClickListener(view -> recognizeEntity());
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        destroy();
        super.onDestroy();
    }

    /**
     * 初始化NluClient
     */
    private void init() {
        Log.d(TAG, "init...");
        if (nluProcessor == null) {
            nluProcessor = Voices.getNluClient(getApplication().getApplicationContext());
            isInitialized = false;
            nluProcessor.init(new SupportListener() {
                @Override
                public void onSupport() {
                    // 初始化成功
                    isInitialized = true;
                    Log.d(TAG, "SupportListener onSupport");
                    showToast("Init Success");
                }

                @Override
                public void onError(int code, String msg) {
                    // 初始化失败回调，比如手机不支持等
                    isInitialized = false;
                    Log.d(TAG, "SupportListener onError");
                    showToast("Init Fail");
                }
            });
        }
    }

    /**
     * 调用分词接口
     */
    private void splitWords() {
        String inputText = etInputContent.getText().toString();
        if (TextUtils.isEmpty(inputText)) {
            showToast("请输入测试文本...");
            return;
        }
        Log.d(TAG, "text:" + inputText);
        if (nluProcessor != null && isInitialized) {
            // nluProcessor.init 未初始化成功前，请勿调用接口
            WordsResult wordsResult;
            if (TextUtils.isEmpty(etWordType.getText().toString())) {
                Log.d(TAG, "default type " + NluConstants.TYPE_WORDS_LOW);
                wordsResult = nluProcessor.splitWords(inputText);
            } else {
                wordType = parseWordType();
                Log.d(TAG, "type:" + wordType);
                // 带有分词粒度接口
                wordsResult = nluProcessor.splitWords(inputText, wordType);
            }
            String result = GsonUtils.toString(wordsResult);
            Log.i(TAG, "SplitWords:" + result);
            runOnUiThread(() -> mTvShowSplit.setText("SplitWords:" + result));
        } else {
            Log.i(TAG, "nluClient is null");
            showToast("Not Init!!!");
        }
    }

    private long parseWordType() {
        long type = NluConstants.TYPE_WORDS_LOW;
        String text = etWordType.getText().toString();
        try {
            type = Long.parseLong(text);
        } catch (NumberFormatException exception) {
            Log.e(TAG, "NumberFormatException:" + text);
        }
        return type;
    }

    /**
     * 调用实体识别接口
     */
    private void recognizeEntity() {
        String inputText = etInputContent.getText().toString();
        if (TextUtils.isEmpty(inputText)) {
            showToast("请输入测试文本...");
            return;
        }
        Log.d(TAG, "text:" + inputText);
        if (nluProcessor != null && isInitialized) {
            // nluProcessor.init 未初始化成功前，请勿调用接口
            EntityResult entityResult;
            ArrayList<String> moduleList = getModuleList();
            if (moduleList.isEmpty()) {
                Log.d(TAG, "module is null");
                Log.d(TAG, "default source " + NluConstants.SOURCE_COPY);
                entityResult = nluProcessor.recognizeEntity(inputText);
            } else {
                entitySource = etEntitySource.getText().toString();
                Log.d(TAG, "module:" + GsonUtils.toString(moduleList));
                // 带有实体类别和文本来源接口
                if (TextUtils.isEmpty(entitySource)) {
                    // entitySource未传入时默认使用"fromCopy"
                    Log.d(TAG, "default source " + NluConstants.SOURCE_COPY);
                    entityResult = nluProcessor.recognizeEntity(inputText, moduleList);
                } else {
                    Log.d(TAG, "source:" + entitySource);
                    entityResult = nluProcessor.recognizeEntity(inputText, moduleList, entitySource);
                }
            }
            String result = GsonUtils.toString(entityResult);
            Log.i(TAG, "recognizeEntity:" + result);
            runOnUiThread(() -> mTvShowEntity.setText("RecognizeEntity:" + result));
        } else {
            Log.i(TAG, "nluClient is null");
            showToast("Not Init!!!");
        }
    }

    private ArrayList<String> getModuleList() {
        ArrayList<String> modulesList = new ArrayList<>();
        if (cbEmail.isChecked()) {
            modulesList.add(NluConstants.MODULE_EMAIL);
        }
        if (cbExpressNo.isChecked()) {
            modulesList.add(NluConstants.MODULE_EXPRESS);
        }
        if (cbFlightNo.isChecked()) {
            modulesList.add(NluConstants.MODULE_FLIGHT);
        }
        if (cbLocation.isChecked()) {
            modulesList.add(NluConstants.MODULE_LOCATION);
        }
        if (cbIdNo.isChecked()) {
            modulesList.add(NluConstants.MODULE_ID);
        }
        if (cbPhoneNum.isChecked()) {
            modulesList.add(NluConstants.MODULE_PHONE);
        }
        if (cbUrl.isChecked()) {
            modulesList.add(NluConstants.MODULE_URL);
        }
        return modulesList;
    }

    /**
     * 销毁NluClient
     */
    private void destroy() {
        Log.d(TAG, "destroy...");
        if (nluProcessor != null) {
            isInitialized = false;
            nluProcessor.destroy();
            nluProcessor = null;
        }
    }
}