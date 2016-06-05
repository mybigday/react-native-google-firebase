import React from "react";
import { NativeModules } from "react-native";

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

FIRDatabase.database = async (app) => {
	if(app){
		throw new Error("Not implement yet.");
	}
	else if(!currentDatabase){
		// Get default database
		const result = await Firebase.databaseAndReference();
		let rootReference = new FIRDatabaseReference(result);
		currentDatabase = new FIRDatabase({
			appKey: result.defaultAppKey,
			rootReference: rootReference
		});
	}
	return currentDatabase;
};

class FIRDatabaseReference {
	constructor(props){
		Object.assign(this, {
			referenceKey: props.referenceKey,
			path: props.path
		});
	}
	async child(path){
		const result = await Firebase.childFromReference(this.referenceKey, path);
		return new FIRDatabaseReference(result);
	}
	async setValue(value){
		await Firebase.setValueForReference(this.referenceKey, value);
	}
}

module.exports = {
	FIRDatabase: FIRDatabase,
	FIRDatabaseReference: FIRDatabaseReference
};