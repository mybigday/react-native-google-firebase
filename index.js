import App from "./app";
import Auth from "./auth";
import Database from "./database";
import Analytics from "./analytics";

module.exports = {
	...App,
	...Auth,
	...Database,
	...Analytics
};