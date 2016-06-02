import React from "react";
import { NativeModules } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

export default class FIRApp {
	construtor(props){
		Object.assign(this, {
			key: ""
		}, props);
	}
}

FIRApp.configure = async (name, option) => {
	// TODO: must add option
	if(name && name != ""){
		// let appList = await Firebase.configure();
		throw new Error("Not implement yet.");
	}
	else{
		let appList = await Firebase.configure();
	}
	appList.map((appProp) => {
		return new FIRApp(appProp);
	});
	console.log(appList);
}