import React from "react";
import { NativeModules } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

let currentAuth;
let currentUser;

class FIRAuth {
	createUserWithEmail = async (email, password) => {
		const userProps = await Firebase.createUserWithEmail(email, password);
		if(userProps){
			currentUser = new FIRUser(userProps);
			currentAuth.currentUser = currentUser;
			return currentUser;
		}
	};
	signInWithEmail = async (email, password) => {
		const userProps = await Firebase.signInWithEmail(email, password);
		if(userProps){
			currentUser = new FIRUser(userProps);
			currentAuth.currentUser = currentUser;
			return currentUser;
		}
	};
	signOut = async () => {
		await Firebase.signOut();
	};
}

FIRAuth.auth = async (app) => {
	if(app){
		throw new Error("Not implement yet.");
	}
	else if(!currentAuth){
		currentAuth = new FIRAuth(await Firebase.auth());
	}
	return currentAuth;
};

FIRAuth.currentAuth = () => currentAuth;

class FIRUser {
	constructor(props){
		Object.assign(this, {
			anonymous: false,
			emailVerified: false,
			refreshToken: "",
			providerID: "",
			uid: "",
			displayName: "",
			photoURL: "",
			email: ""
		}, props);
	}
}

module.exports = {
	FIRAuth: FIRAuth,
	FIRUser: FIRUser
};