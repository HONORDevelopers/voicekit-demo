/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.hihonor.voicekitdemo.utils;

import android.util.Log;

import com.google.gson.Gson;

/**
 * utils for Gson
 *
 * @since 2024-07-18
 */
public final class GsonUtils {
    private static final String TAG = GsonUtils.class.getSimpleName();

    private GsonUtils() {
    }

    /**
     * createInstanceByDefault
     *
     * @return gson
     */
    public static Gson createInstance() {
        return new Gson();
    }

    /**
     * Parse object into string
     *
     * @param object the object for which Json representation is to be created setting for Gson
     * @return Json representation of object.
     */
    public static <T> String toString(T object) {
        if (object == null) {
            Log.e(TAG, "object is null");
            return "";
        }
        return createInstance().toJson(object);
    }
}
