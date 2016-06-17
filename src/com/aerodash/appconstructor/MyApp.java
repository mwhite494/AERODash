package com.aerodash.appconstructor;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class MyApp extends Application {
	
	public MyApp() {
        // This method fires only once per application start. 
        // getApplicationContext returns null here
		
        Log.i("main", "Constructor fired");
    }

    @Override
    public void onCreate() {
        super.onCreate();    

        // This method fires once as well as constructor 
        // Application has context here
        
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Set the update count to 0 when opening the app for the first time
        editor.putInt("updateCount", 0);
        editor.commit();

        Log.i("main", "onCreate fired"); 
    }
}

