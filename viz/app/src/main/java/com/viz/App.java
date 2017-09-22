package com.viz;

import android.app.Application;
import android.content.Context;

/**
 * Created by yaelbilueran on 19/09/2017.
 */

public class App extends Application {

    private static Context sContext = null;

    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Context getAppContext(){
        return sContext;
    }
}
