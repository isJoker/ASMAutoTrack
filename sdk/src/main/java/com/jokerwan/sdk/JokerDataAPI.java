package com.jokerwan.sdk;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.Map;


@Keep
public class JokerDataAPI {
    private final String TAG = this.getClass().getSimpleName();
    public static final String SDK_VERSION = "1.0.0";
    private static JokerDataAPI INSTANCE;
    private static final Object mLock = new Object();
    private static Map<String, Object> mDeviceInfo;
    private String mDeviceId;

    @Keep
    @SuppressWarnings("UnusedReturnValue")
    public static JokerDataAPI init(Application application) {
        synchronized (mLock) {
            if (null == INSTANCE) {
                INSTANCE = new JokerDataAPI(application);
            }
            return INSTANCE;
        }
    }

    @Keep
    public static JokerDataAPI getInstance() {
        return INSTANCE;
    }

    private JokerDataAPI(Application application) {
        mDeviceId = JokerDataPrivate.getAndroidID(application.getApplicationContext());
        mDeviceInfo = JokerDataPrivate.getDeviceInfo(application.getApplicationContext());
    }

    /**
     * Track 事件
     *
     * @param eventName  String 事件名称
     * @param properties JSONObject 事件属性
     */
    @Keep
    public void track(@NonNull final String eventName, @Nullable JSONObject properties) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", eventName);
            jsonObject.put("device_id", mDeviceId);

            JSONObject sendProperties = new JSONObject(mDeviceInfo);

            if (properties != null) {
                JokerDataPrivate.mergeJSONObject(properties, sendProperties);
            }

            jsonObject.put("properties", sendProperties);
            jsonObject.put("time", System.currentTimeMillis());

            Log.i(TAG, JokerDataPrivate.formatJson(jsonObject.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
