import React from "react";
import { NativeModules } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

module.exports = {
	FIRAnalytics: {
		analytics: () => {
			Firebase.analytics();
		},
		logEvent: (event, param) => {
			Firebase.logEvent(event, param);
		}
	}
};