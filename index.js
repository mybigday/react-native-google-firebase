import React from "react";
import { NativeModules } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

import Auth from "./auth";

const App = {
	configure: () => {
		Firebase.configure();
	}
};

module.exports = {
	App: App,
	Auth: Auth
}