import React from "react";
import { NativeModules } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

let currentApp;

class FIRApp {
	constructor(props){
		Object.assign(this, {
			name: ""
		}, props);
	}
}

FIRApp.configure = async (name, option) => {
	// TODO: need use option
	let appProps;
	if(name && name != ""){
		throw new Error("Not implement custom app name yet");
	}
	else if(!currentApp){
		appProps = await Firebase.configure();
	}
	if(appProps){
		currentApp = new FIRApp(appProps);
	}
	return currentApp;
};

FIRApp.currentApp = () => currentApp;

module.exports = {
	FIRApp: FIRApp
};