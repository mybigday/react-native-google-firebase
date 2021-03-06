package com.mybigday.rngooglefirebase;

import android.app.Activity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import com.google.gson.*;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RNGoogleFirebase extends ReactContextBaseJavaModule {

    private static final String TAG = "RNGoogleFirebase";

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
    private FirebaseStorage defaultStorage;
    private FirebaseAnalytics defaultAnalytics;


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

            ValueEventListener valueEventListener = null;
            ChildEventListener childEventListener = null;

            switch(eventType){
                case "FIRDataEventTypeValue":
                    valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
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
                    break;
                case "FIRDataEventTypeChildAdded":
                    childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                            WritableMap result = Arguments.createMap();
                            result.putInt("handle", handleNumber);
                            result.putMap("value", (WritableMap) context.castSnapshot(dataSnapshot));
                            context.sendEvent("FIRDataEvent", result);
                        }
                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}
                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {}
                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getReactApplicationContext(), "ValueEventListener Error", 3000).show();
                        }
                    };
                    break;
                case "FIRDataEventTypeChildChanged":
                    childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                            WritableMap result = Arguments.createMap();
                            result.putInt("handle", handleNumber);
                            result.putMap("value", (WritableMap) context.castSnapshot(dataSnapshot));
                            context.sendEvent("FIRDataEvent", result);
                        }
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {}
                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {}
                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getReactApplicationContext(), "ValueEventListener Error", 3000).show();
                        }
                    };
                    break;
                case "FIRDataEventTypeChildRemoved":
                    childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            WritableMap result = Arguments.createMap();
                            result.putInt("handle", handleNumber);
                            result.putMap("value", (WritableMap) context.castSnapshot(dataSnapshot));
                            context.sendEvent("FIRDataEvent", result);
                        }
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {}
                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}
                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getReactApplicationContext(), "ValueEventListener Error", 3000).show();
                        }
                    };
                    break;
                case "FIRDataEventTypeChildMoved":
                    childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                            WritableMap result = Arguments.createMap();
                            result.putInt("handle", handleNumber);
                            result.putMap("value", (WritableMap) context.castSnapshot(dataSnapshot));
                            context.sendEvent("FIRDataEvent", result);
                        }
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {}
                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}
                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {}

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getReactApplicationContext(), "ValueEventListener Error", 3000).show();
                        }
                    };
                    break;
            }
            if(valueEventListener != null) {
                source.addValueEventListener(valueEventListener);
            }
            else if(childEventListener != null){
                source.addChildEventListener(childEventListener);
            }
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
        if (snapshot != null) {
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
                if(snapshot.getValue() != null) {
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
                else{
                    return (Any) null;
                }
            }
        }
        else{
            return (Any) Arguments.createMap();
        }
    }

    // Analytics
    @ReactMethod
    public void analytics(){
        defaultAnalytics = FirebaseAnalytics.getInstance(getReactApplicationContext());
    }
    @ReactMethod
    public void logEvent(String event, ReadableMap params){
        Bundle result = new Bundle();
        ReadableMapKeySetIterator iterator = params.keySetIterator();
        while(iterator.hasNextKey()){
            String key = iterator.nextKey();
            ReadableType type = params.getType(key);
            switch (type) {
                case String:
                    result.putString(key, params.getString(key));
                    break;
            }
        }
        defaultAnalytics.logEvent(event, result);
    }

    // Storage
    @ReactMethod
    public void storage(Promise promise){
        this.defaultStorage = FirebaseStorage.getInstance();
        WritableMap result = Arguments.createMap();
        promise.resolve(result);
    }
    @ReactMethod
    public void storageReferenceForURL(String url, Promise promise){
        StorageReference reference = this.defaultStorage.getReferenceFromUrl(url);

        String rootStorageReferenceKey = "StorageReference:/";

        WritableMap result = Arguments.createMap();
        result.putString("path", url);
        promise.resolve(result);
    }
}