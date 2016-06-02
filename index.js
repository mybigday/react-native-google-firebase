import React from "react";
import { NativeModules } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

import FIRApp from "./app";
import FIRAuth from "./auth";
import FIRDatabase from "./database";

module.exports = {
	FIRApp: FIRApp,
	FIRAuth: FIRAuth,
	FIRDatabase: FIRDatabase
}