import App from "./app";
import Auth from "./auth";
import Database from "./database";

module.exports = {
	...App,
	...Auth,
	...Database
};