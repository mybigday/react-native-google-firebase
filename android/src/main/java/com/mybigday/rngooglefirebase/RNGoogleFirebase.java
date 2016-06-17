package com.mybigday.rngooglefirebase;

import android.app.Activity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import com.google.firebase.database.*;

import android.content.Context;
import android.widget.Toast;

public class RNGoogleFirebase extends ReactContextBaseJavaModule {

    private Context context;

    public RNGoogleFirebase(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "RNGoogleFirebase";
    }

    @ReactMethod
    public void configure() {
        Toast.makeText(getReactApplicationContext(), "Hello", 1000).show();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("hello_world");
        myRef.setValue("HAHAHA!!!");
    }
}
