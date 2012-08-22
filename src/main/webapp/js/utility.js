var gRegistry = [];
YUI.add('utility', function (Y) {
	YUI.namespace('com.imaginea.mongoV');
	var MV = YUI.com.imaginea.mongoV;
	// Check if String.prototype.format already exists because in future
	// versions
	// format function can be added
	if (typeof String.prototype.format !== 'function') {
		String.prototype.format = function () {
			var formatted = this,
					i;
			for (i = 0; i < arguments.length; i++) {
				formatted = formatted.replace("[" + i + "]", arguments[i]);
			}
			return formatted;
		};
	}
	if (typeof String.prototype.trim !== 'function') {
		String.prototype.trim = function () {
			var str = this;
			// AN typeof recipe for disaster in IE8
			// if (!str || typeof str !== 'string') {
			if (!str) {
				return "";
			} else {
				str = str.toString();
				return str.replace(/^[\s]+/, '').replace(/[\s]+$/, '').replace(/[\s]{2,}/, ' ');
			}
		};
	}

	MV.getProperties = function (doc) {
		var key, name, allKeys = [];
		for (key in doc) {
			if (doc.hasOwnProperty(key)) {
				allKeys.push(key);
			}
		}
		return allKeys;
	};
	MV.isArray = function (o) {
		return Object.prototype.toString.call(o) === '[object Array]';
	};
	MV.isObject = function (o) {
		return (typeof o === "object");
	};

	MV.mainBody = Y.one("#mainBody");
	MV.header = Y.one("#mainBodyHeader");
	MV.warnIcon = "warnIcon";
	MV.infoIcon = "infoIcon";

	MV.openFileEvent = new YAHOO.util.CustomEvent("OpenFile");
	MV.deleteFileEvent = new YAHOO.util.CustomEvent("DeleteFile");
	MV.deleteDocEvent = new YAHOO.util.CustomEvent("DeleteDoc");

	MV.StateManager = (function() {
		var self = this;
		var stateVariables = ['currentDB', 'currentColl', 'currentBucket', 'host', 'port','dbInfo','newName'];
		var i = 0;
		var exports = {};

		function getVal(key) {
			return Y.one('#' + key).get("value");
		}

		function setVal(key, value) {
			Y.one('#' + key).set("value", value);
		}

		function deliverEvent(eventName, eventArgs) {
			var i = 0;
			var callbackArray = gRegistry[eventName];
			for (; i < callbackArray.length; i++) {
				callbackArray[i].call(this, eventArgs);
			}
		}

		function methodMaker(stateVariable) {
			exports[stateVariable] = function() {
				return getVal(stateVariable);
			};
			exports[stateVariable + "AsNode"] = function() {
				var id = getVal(stateVariable).replace(/ /g, '_').replace('.', '_');
				return Y.one('#' + id);
			};

			var upcasedVar = stateVariable.substring(0, 1).toUpperCase() + stateVariable.substring(1);
			exports['set' + upcasedVar] = function(newValue) {
				return setVal(stateVariable, newValue);
			};
			exports['clear' + upcasedVar] = function(newValue) {
				return setVal(stateVariable, "");
			};
		}

		// AN stupid IE8 does not understand forEach
		for (i = 0; i < stateVariables.length; i++) {
			var currVariable = stateVariables[i];
			methodMaker(currVariable);
		}

		exports.dbInfo = function() {
			var currDBInfo = getVal('dbInfo');
			if (currDBInfo === undefined || currDBInfo.length === 0) {
				currDBInfo = getVal('host') + "_" + getVal('port');
			}
			return currDBInfo;
		};
		exports.publish = function(eventName, eventArgs) {
			if (gRegistry[eventName]) {
				deliverEvent(eventName, eventArgs);
			}
		};
		exports.subscribe = function(callback, eventArgs) {
			var i = 0, eventNames;
			// if it is not a number assume it is an array
			if (isNaN(eventArgs)) {
				eventNames = eventArgs;
			} else {
				eventNames = [eventArgs];
			}

			for (; i < eventNames.length; i++) {
				eventName = eventNames[i];
				if (gRegistry[eventName] === undefined) {
					gRegistry[eventName] = [];
				}
				gRegistry[eventName].push(callback);
			}
		};
		exports.recordLastArrowNavigation = function() {
			self.lastArrowNavTS = new Date().getTime();
		};
		exports.isNavigationSideEffect = function() {
			return ((self.lastArrowNavTS) ? ((new Date().getTime() - self.lastArrowNavTS) < 100) : false);
		};
		exports.now = function() {
			return new Date().getTime().toString();
		};
		exports.events = {
			collectionsChanged : 1,
			dbsChanged : 2,
			queryFired : 3,
			openFile: 4
		};
		return exports;

	}());

	var sm = MV.StateManager;

	MV.URLMap = {
		getDBs: function () {
			return "services/db?dbInfo=[0]&ts=[1]".format(sm.dbInfo());
		},
		insertDB: function () {
			return "services/db/[0]?dbInfo=[1]&action=PUT&ts=[2]".format(sm.newName(), sm.dbInfo(), sm.now());
		},
		dropDB: function () {
			return "services/db/[0]?dbInfo=[1]&action=DELETE&ts=[2]".format(sm.currentDB(), sm.dbInfo(), sm.now());
		},
		dbStatistics: function () {
			return "services/stats/db/[0]?dbInfo=[1]&ts=[2]".format(sm.currentDB(), sm.dbInfo(), sm.now());
		},
		getColl: function () {
			return "services/[0]/collection?dbInfo=[1]&ts=[2]".format(sm.currentDB(), sm.dbInfo(), sm.now());
		},
		insertColl: function () {
			return "services/[0]/collection/[1]?dbInfo=[2]&action=PUT&ts=[3]".format(sm.currentDB(), sm.newName(), sm.dbInfo(), sm.now());
		},
		dropColl: function () {
			return "services/[0]/collection/[1]?dbInfo=[2]&action=DELETE&ts=[3]".format(sm.currentDB(), sm.currentColl(), sm.dbInfo(), sm.now());
		},
		collStatistics: function () {
			return "services/stats/db/[0]/collection/[1]?dbInfo=[2]&ts=[3]".format(sm.currentDB(), sm.currentColl(), sm.dbInfo(), sm.now());
		},
		getDocKeys: function () {
			return "services/[0]/[1]/document/keys?dbInfo=[2]&ts=[3]".format(sm.currentDB(), sm.currentColl(), sm.dbInfo(), sm.now());
		},
		getDocs: function (params) {
			return "services/[0]/[1]/document?dbInfo=[2]&ts=[3][4]".format(sm.currentDB(), sm.currentColl(), sm.dbInfo(), sm.now(), params);
		},
		insertDoc: function () {
			return "services/[0]/[1]/document?dbInfo=[2]&action=PUT&ts=[3]".format(sm.currentDB(), sm.currentColl(), sm.dbInfo(), sm.now());
		},
		updateDoc: function () {
			return "services/[0]/[1]/document?dbInfo=[2]&ts=[3]".format(sm.currentDB(), sm.currentColl(), sm.dbInfo(), sm.now());
		},
		deleteDoc: function () {
			return "services/[0]/[1]/document?dbInfo=[2]&action=DELETE&ts=[3]".format(sm.currentDB(), sm.currentColl(), sm.dbInfo(), sm.now());
		},
		addGridFS: function (bucketName) {
			return "services/[0]/[1]/gridfs/create?dbInfo=[2]&ts=[3]".format(sm.currentDB(), bucketName, sm.dbInfo(), sm.now());
		},
		getFilesCount: function () {
			return "services/[0]/[1]/gridfs/count?dbInfo=[2]&ts=[3]".format(sm.currentDB(), sm.currentBucket(), sm.dbInfo(), sm.now());
		},
		getFiles: function () {
			return "services/[0]/[1]/gridfs/getfiles?dbInfo=[2]&ts=[3]".format(sm.currentDB(), sm.currentBucket(), sm.dbInfo(), sm.now());
		},
		getFile: function (id, download) {
			return "services/[0]/[1]/gridfs/getfile?id=[2]&download=[3]&dbInfo=[4]&ts=[5]".format(sm.currentDB(), sm.currentBucket(), id, download, sm.dbInfo(), sm.now());
		},
		insertFile: function () {
			return "services/[0]/[1]/gridfs/uploadfile?dbInfo=[2]&ts=[3]".format(sm.currentDB(), sm.currentBucket(), sm.dbInfo(), sm.now());
		},
		deleteFile: function (id) {
			return "services/[0]/[1]/gridfs/dropfile?id=[2]&dbInfo=[3]&ts=[4]".format(sm.currentDB(), sm.currentBucket(), id, sm.dbInfo(), sm.now());
		},
		dropBucket: function () {
			return "services/[0]/[1]/gridfs/dropbucket?dbInfo=[2]&ts=[3]".format(sm.currentDB(), sm.currentBucket(), sm.dbInfo(), sm.now());
		},
		bucketStatistics: function (ext) {
			return "services/stats/db/[0]/collection/[1]?dbInfo=[2]&ts=[3]".format(sm.currentDB(), sm.currentBucket() + ext, sm.dbInfo(), sm.now());
		},
		login: function () {
			return "services/login";
		},
		disconnect: function () {
			return "services/disconnect?dbInfo=[0]".format(sm.dbInfo());
		},
		serverStatistics: function () {
			return "services/stats?dbInfo=[0]&ts=[1]".format(sm.dbInfo(), sm.now());
		},
		help: function () {
			return "http://imaginea.github.com/mViewer/";
		},
		troubleShootPage: function () {
			return "troubleshoot.html";
		},
		troubleShoot: function () {
			return "admin";
		},
		graphs: function () {
			return "graphs.html?dbInfo=[0]&ts=[1]".format(sm.dbInfo(), sm.now());
		},
		graphInitiate: function () {
			return "graphs/initiate?dbInfo=[0]&ts=[1]".format(sm.dbInfo(), sm.now());
		},
		graphQuery: function () {
			return "graphs/query?dbInfo=[0]&ts=[1]".format(sm.dbInfo(), sm.now());
		}
	};

	MV.errorCodeMap = {
		"HOST_UNKNOWN": "Unkown Host. Please check if MongoDB is running on the given host and port !",
		"MISSING_LOGIN_FIELDS": "Please fill in all the login fields !",
		"ERROR_PARSING_PORT": "You have entered an invalid port number !",
		"INVALID_ARGUMENT": "You have entered an invalid input data !",
		"INVALID_USERNAME": "You have entered an invalid username and password combination ! To access you need to add user in admin database of mongodb.",
        "NEED_AUTHORISATION": "Mongo DB is running in auth mode. Please enter username and password.",
		"INVALID_SESSION": "Your session has timed out ! Please login again.",
		"GET_DB_LIST_EXCEPTION": "Could not load the DB list ! Please check if mongo is still running and then refresh the page.",
		"GET_COLLECTION_LIST_EXCEPTION": "Please check if mongod is still running and then refresh the page.",
		"DB_DELETION_EXCEPTION": "Please check if mongo is running and then refresh the page and try again.",
		"DB_DOES_NOT_EXISTS": "The db you are trying to delete does not exist! Refresh the page.",
		"DB_NAME_EMPTY": "Received an empty name for the database which is invalid",
		"DB_ALREADY_EXISTS": "A database with the given name already exist ! Try another name.",
		"COLLECTION_ALREADY_EXISTS": "A collection with the given name already exists ! Try another name.",
		"COLLECTION_DOES_NOT_EXIST": "The collection you are trying to delete does not exist !",
		"COLLECTION_NAME_EMPTY": "Recieved an empty collection name.",
		"GET_DOCUMENT_LIST_EXCEPTION": "Please check if mongod is running and refresh the page.",
		"DOCUMENT_DELETION_EXCEPTION": " Please check if mongod is running and refresh the page.",
		"DOCUMENT_CREATION_EXCEPTION": "Please check if mongod is running and refresh the page.",
		"DOCUMENT_UPDATE_EXCEPTION": "Please check if mongod is running and refresh the page.",
		"DOCUMENT_DOES_NOT_EXIST": "Document does not exist !",
		"INVALID_USER": "Your session is corrupted or timed out ! Please login again from the login page.",
		"DB_INFO_ABSENT": "Mongo Config details are not provided in session ! Please login again from the login page.",
		"GET_DB_STATS_EXCEPTION": "Please check if mongod is running and refresh the page.",
		"GET_COLL_STATS_EXCEPTION": "Please check if mongod is running and refresh the page.",
		"COLLECTION_CREATION_EXCEPTION": "Please check if mongod is running and refresh the page.",
		"COLLECTION_DELETION_EXCEPTION": "Please check if mongod is running and refresh the page.",
		"INVALID_OBJECT_ID": "Value provided for '_id' is invalid .",
		"UPDATE_OBJECT_ID_EXCEPTION" : "_id cannot be updated.",
		"ANY_OTHER_EXCEPTION": "An unknown exception occured ! Please try to login again.",
		"ERROR_INITIATING_GRAPH": "Could not initiate the graph ! Please check if mongod is running.",
		"FILE_NOT_FOUND": "Logger Config File or Mongo Config File cannot be found ! Please check if they are present in the resources of src/main and src/test respectively.",
		"IO_EXCEPTION": "An IO Exception Occured ! Please Refresh the page.",
		"ERROR_PARSING_POLLING_INTERVAL": "An error occured while initiating graph ! Please chcek if polling interval is passed.",
		"LOGGING_LEVEL_UNDEFINED": "The Logging level that you are trying to change to is undefined for log4j logger. Please select from the given options only.",
		"DELETING_FROM_CAPPED_COLLECTION": "Deleting from a Capped Collection is not allowed by database. Drop the whole Collection instead."
	};
}, '3.3.0', {
	requires: ["node"]
});
