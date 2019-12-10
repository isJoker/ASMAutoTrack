package com.jokerwan.testasm;

import android.app.Application;

import com.jokerwan.sdk.JokerDataAPI;

/**
 * Created by JokerWan on 2019-12-05.p
 * Function:
 */
public class JokerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initSensorsDataAPI(this);
    }

    /**
     * 初始化埋点 SDK
     *
     * @param application Application
     */
    private void initSensorsDataAPI(Application application) {
        JokerDataAPI.init(application);
    }
}
