package com.penn.jba;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by penn on 09/04/2017.
 */

public class PPApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}