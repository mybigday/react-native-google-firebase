package com.mybigday.rngooglefirebase;

import android.app.Activity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import android.widget.Toast;

public class RNGoogleFirebase extends ReactContextBaseJavaModule {
  public RNGoogleFirebase(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "RNGoogleFirebase";
  }

  @ReactMethod
  public void configure() {
    Toast.makeText(getReactApplicationContext(), "Hello", 1000).show();
  }
}
