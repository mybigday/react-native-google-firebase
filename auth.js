import React from "react";
import { NativeModules } from "react-native";

const Firebase = NativeModules.RNGoogleFirebase;

let currentAuth;
let currentUser;

class FIRAuth {
	createUserWithEmail = async (email, password) => {
		const userProps = await Firebase.createUserWithEmail(auth, email, password);
		if(userProps){
			currentUser = new FIRUser(userProps);
			currentAuth.currentUser = currentUser;
		}
	}
}

FIRAuth.auth = async (app) => {
	if(currentAuth){
		return currentAuth;
	}
	else{
		if(app){
			throw new Error("Not implement yet.");
		}
		else{
			authProps = new await Firebase.auth();
			if(authProps){
				currentAuth = new FIRAuth(authProps);
			}
		}
	}
}

class FIRUser {
	constructor(props){
		Object.assign(this, {

		});
	}
}

module.exports = {
	
};