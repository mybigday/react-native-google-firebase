import React from "react";
import { NativeModules, NativeAppEventEmitter, DeviceEventEmitter } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

let currentDatabase;

class FIRDatabase {
	constructor(props){
		Object.assign(this, {
			appKey: props.defaultAppKey,
			rootReference: props.rootReference
		});
	}
}
FIRDatabase.FIRDataEventType = {
	FIRDataEventTypeChildAdded: Firebase.FIRDataEventTypeChildAdded,
	FIRDataEventTypeChildChanged: Firebase.FIRDataEventTypeChildChanged,
	FIRDataEventTypeChildMoved: Firebase.FIRDataEventTypeChildMoved,
	FIRDataEventTypeChildRemoved: Firebase.FIRDataEventTypeChildRemoved,
	FIRDataEventTypeValue: Firebase.FIRDataEventTypeValue
};

FIRDatabase.database = async (persistenceEnabled = false, app) => {
	if(app){
		throw new Error("Not implement yet.");
	}
	else if(!currentDatabase){
		// Get default database
		const result = await Firebase.database(persistenceEnabled);
		let rootReference = new FIRDatabaseReference(result);
		currentDatabase = new FIRDatabase({
			appKey: result.defaultAppKey,
			rootReference: rootReference
		});
	}
	return currentDatabase;
};

let eventHandler = {};
NativeAppEventEmitter.addListener("FIRDataEvent", (event) => {
	if(eventHandler[event.handle]){
		eventHandler[event.handle](event.value);
	}
});
DeviceEventEmitter.addListener("FIRDataEvent", (event) => {
	console.log("Event:", event);
	if(eventHandler[event.handle]){
		eventHandler[event.handle](event.value);
	}
});

NativeAppEventEmitter.addListener("FIRConnectionEvent", (event) => {
	console.log("Emit from NativeAppEventEmitter");
	console.log(event);
});

DeviceEventEmitter.addListener("FIRConnectionEvent", (event) => {
	console.log("Emit from DeviceEventEmitter");
	console.log(event);
});

class FIRDatabaseReference {
	constructor(props){
		Object.assign(this, {
			referenceKey: props.referenceKey,
			path: props.path
		});
	}
	async child(path){
		const result = await Firebase.childFromReference(this.referenceKey, path);
		console.log("FIRDatabaseReference child", result);
		return new FIRDatabaseReference(result);
	}
	async childByAutoId(){
		const result = await Firebase.childByAutoIdFromReference(this.referenceKey);
		console.log(result);
		return new FIRDatabaseReference(result);
	}
	setValue(value){
		Firebase.setValueForReference(this.referenceKey, value);
	}
	removeValue(){
		Firebase.removeValueForReference(this.referenceKey);
	}
	async observeEventType(type, callback){
		const result = await Firebase.observeEventTypeForReference(this.referenceKey, type);
		eventHandler[result.handle] = callback;
		return result.handle;
	}
	async removeObserverWithHandle(handle){
		return await Firebase.removeObserverWithHandleForReference(this.referenceKey, handle);
	}
}

// subscribe(channel, listener){
// 		// TODO: Must test channel
// 		return this.emitter.addListener(channel, listener);
// 	}
// 	unsubscribe(subscription){
// 		subscription.remove();
// 	}

module.exports = {
	FIRDatabase: FIRDatabase,
	FIRDatabaseReference: FIRDatabaseReference
};