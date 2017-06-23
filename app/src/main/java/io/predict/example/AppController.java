package io.predict.example;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class AppController extends MultiDexApplication {

    private PredictIOManager mPredictIOManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        //PredictIO SDK code
        mPredictIOManager = new PredictIOManager(this);
        mPredictIOManager.onApplicationCreate();
    }
}