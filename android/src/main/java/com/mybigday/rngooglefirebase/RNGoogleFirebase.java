package com.mybigday.rngooglefirebase;

import android.app.Activity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.google.firebase.database.*;
import com.google.gson.*;

import android.content.Context;
import android.widget.Toast;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RNGoogleFirebase extends ReactContextBaseJavaModule {

    private static final String E_DATABASE_REFERENCE_NOT_EXIST = "E_DATABASE_REFERENCE_NOT_EXIST";
    private static final String FIRDataEventTypeValue = "FIRDataEventTypeValue";
    private static final String FIRDataEventTypeChildAdded = "FIRDataEventTypeChildAdded";
    private static final String FIRDataEventTypeChildChanged = "FIRDataEventTypeChildChanged";
    private static final String FIRDataEventTypeChildRemoved = "FIRDataEventTypeChildRemoved";
    private static final String FIRDataEventTypeChildMoved = "FIRDataEventTypeChildMoved";
    private static int ListenerCount = 0;

    private ReactApplicationContext context;
    private String defaultAppKey = "__FIRAPP_DEFAULT";
    private HashMap<String, Object> appMap;
    private FirebaseDatabase defaultDatabase;


    public RNGoogleFirebase(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "RNGoogleFirebase";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(FIRDataEventTypeValue, FIRDataEventTypeValue);
        constants.put(FIRDataEventTypeChildAdded, FIRDataEventTypeChildAdded);
        constants.put(FIRDataEventTypeChildChanged, FIRDataEventTypeChildChanged);
        constants.put(FIRDataEventTypeChildRemoved, FIRDataEventTypeChildRemoved);
        constants.put(FIRDataEventTypeChildMoved, FIRDataEventTypeChildMoved);
        return constants;
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    @ReactMethod
    public void configure() {
        this.appMap = new HashMap<String, Object>();
    }

    @ReactMethod
    public void database(Boolean persistenceEnabled, Promise promise) {
        String rootReferenceKey = "DatabaseReference:/";
        if (!this.appMap.containsKey(rootReferenceKey)) {
            this.defaultDatabase = FirebaseDatabase.getInstance();
            DatabaseReference rootRef = defaultDatabase.getReference();
            this.appMap.put(rootReferenceKey, rootRef);

            // Connection
            DatabaseReference connectedRef = this.defaultDatabase.getReference(".info/connected");

            final RNGoogleFirebase context = this;
            ValueEventListener connectedRefListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    WritableMap result = Arguments.createMap();
                    Boolean connectionStatus = dataSnapshot.getValue(Boolean.class);
                    result.putBoolean("status", connectionStatus);
                    context.sendEvent("FIRConnectionEvent", result);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getReactApplicationContext(), "Connection onCancelled", 3000).show();
                }
            };
            connectedRef.addValueEventListener(connectedRefListener);
        }

        WritableMap result = Arguments.createMap();
        result.putString("appKey", defaultAppKey);
        result.putString("referenceKey", rootReferenceKey);
        result.putString("path", "/");

        promise.resolve(result);
    }

    @ReactMethod
    public void childFromReference(String referenceKey, String path, Promise promise) {
        if (this.appMap.containsKey(referenceKey)) {
            DatabaseReference source = (DatabaseReference) this.appMap.get(referenceKey);
            DatabaseReference target = source.child(path);
            String newKey = referenceKey + path;
            this.appMap.put(newKey, target);

            WritableMap result = Arguments.createMap();
            result.putString("appKey", defaultAppKey);
            result.putString("referenceKey", newKey);
            result.putString("path", newKey.replace("DatabaseReference:", ""));
            promise.resolve(result);
        } else {
            promise.reject(E_DATABASE_REFERENCE_NOT_EXIST, "Reference not exist:" + referenceKey);
        }
    }

    @ReactMethod
    public void childByAutoIdFromReference(String referenceKey, Promise promise) {
        if (this.appMap.containsKey(referenceKey)) {
            DatabaseReference source = (DatabaseReference) this.appMap.get(referenceKey);
            DatabaseReference target = source.push();
            String newKey = referenceKey + target.getKey();
            this.appMap.put(newKey, target);

            WritableMap result = Arguments.createMap();
            result.putString("appKey", defaultAppKey);
            result.putString("referenceKey", newKey);
            result.putString("path", newKey.replace("DatabaseReference:", ""));
            promise.resolve(result);
        } else {
            promise.reject(E_DATABASE_REFERENCE_NOT_EXIST, "Reference not exist:" + referenceKey);
        }
    }

    @ReactMethod
    public void setValueForReference(String referenceKey, ReadableMap value) {
        if (this.appMap.containsKey(referenceKey)) {
            DatabaseReference source = (DatabaseReference) this.appMap.get(referenceKey);
            Toast.makeText(getReactApplicationContext(), value.toString(), 10000).show();
            Gson gson = new Gson();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map = (HashMap<String, Object>)gson.fromJson(value.toString(), map.getClass());
            source.setValue(map.get("NativeMap"));
        }
    }

    @ReactMethod
    public void removeValueForReference(String referenceKey) {
        if (this.appMap.containsKey(referenceKey)) {
            DatabaseReference source = (DatabaseReference) this.appMap.get(referenceKey);
            source.removeValue();
        }
    }

    @ReactMethod
    public void observeEventTypeForReference(String referenceKey, String eventType, final Promise promise) {
        if (this.appMap.containsKey(referenceKey)) {
            DatabaseReference source = (DatabaseReference) this.appMap.get(referenceKey);
            final int handleNumber = ++ListenerCount;
            final RNGoogleFirebase context = this;
            ValueEventListener sourceListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Toast.makeText(getReactApplicationContext(), "Event: " + handleNumber, 3000).show();
                    WritableMap result = Arguments.createMap();
                    result.putInt("handle", handleNumber);
                    result.putMap("value", (WritableMap) context.castSnapshot(dataSnapshot));
                    context.sendEvent("FIRDataEvent", result);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getReactApplicationContext(), "ValueEventListener Error", 3000).show();
                }
            };
//            switch(eventType){
//                case "FIRDataEventTypeValue":
//                    final int handleNumber = ++ListenerCount;
//                    sourceListener =
//                    break;
//            }
            source.addValueEventListener(sourceListener);

            WritableMap result = Arguments.createMap();
            result.putInt("handle", handleNumber);
            promise.resolve(result);
        } else {
            promise.reject(E_DATABASE_REFERENCE_NOT_EXIST, "Reference not exist:" + referenceKey);
        }
    }

    // Thanks for VonD
    // http://stackoverflow.com/questions/36289315/how-can-i-pass-a-hashmap-to-a-react-native-android-callback
    private <Any> Any castSnapshot(DataSnapshot snapshot) {
        if (snapshot.hasChildren()) {
            WritableMap data = Arguments.createMap();
            for (DataSnapshot child : snapshot.getChildren()) {
                Any castedChild = castSnapshot(child);
                switch (castedChild.getClass().getName()) {
                    case "java.lang.Boolean":
                        data.putBoolean(child.getKey(), (Boolean) castedChild);
                        break;
                    case "java.lang.Integer":
                        data.putInt(child.getKey(), (Integer) castedChild);
                        break;
                    case "java.lang.Double":
                        data.putDouble(child.getKey(), (Double) castedChild);
                        break;
                    case "java.lang.String":
                        data.putString(child.getKey(), (String) castedChild);
                        break;
                    case "com.facebook.react.bridge.WritableNativeMap":
                        data.putMap(child.getKey(), (WritableMap) castedChild);
                        break;
                }
            }
            return (Any) data;
        } else {
            String type = snapshot.getValue().getClass().getName();
            switch (type) {
                case "java.lang.Boolean":
                    return (Any) ((Boolean) snapshot.getValue());
                case "java.lang.Long":
                    // TODO check range errors
                    return (Any) ((Integer) (((Long) snapshot.getValue()).intValue()));
                case "java.lang.Double":
                    return (Any) ((Double) snapshot.getValue());
                case "java.lang.String":
                    return (Any) ((String) snapshot.getValue());
                default:
                    return (Any) null;
            }
        }
    }
}