import React from "react";
import { NativeModules } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

exports.createUserWithEmail = async (email, password) => {
	return await Firebase.createUserWithEmail(email, password);
}

// exports.updateUser