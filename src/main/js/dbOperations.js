/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd.
 * Hyderabad, India
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
YUI({
    filter: 'raw'
}).use("loading-panel","alert-dialog", "utility", "submit-dialog", "yes-no-dialog", "io-base", "node", "node-menunav", "json-parse", "event-delegate", "node-event-simulate", "stylize", "custom-datatable", function (Y) {
    // TODO: make loading panel module
    var dbDiv = Y.one('#dbNames ul.lists');
	dbDiv.delegate('click',handleClickEvent, 'a.onclick');
    YUI.namespace('com.imaginea.mongoV');
    var MV = YUI.com.imaginea.mongoV; 
    var sm = YUI.com.imaginea.mongoV.StateManager;

    /**
     * The function handles the successful sending of add Collection request.
     * It parses the response and checks if the collection is successfully added. If not, 
     * then prompt the user
     * @param responseObject The response Object
     */
    function addCollection(responseObject) {
        var parsedResponse = Y.JSON.parse(responseObject.responseText),
        	response = parsedResponse.response.result,
        	error;
        if (response !== undefined) {
            MV.showAlertMessage(response, MV.infoIcon);
            Y.log("[0] created in [1]".format(Y.one("#newName").get("value"), Y.one("#currentDB").get("value")), "info");
            sm.clearCurrentColl();
            Y.one("#" + Y.one("#currentDB").get("value")).simulate("click");
        } else {
            error = parsedResponse.response.error;
            MV.showAlertMessage("Could not add Collection! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
            Y.log("Could not add Collection! [0]".format(MV.errorCodeMap[error.code]), "error");
        }
    }

    /**
     * Sends the dropDb requests and handles it. It is actually the callback for the Yes button click
     * on the YesNo dialog box
     * @param responseObject The response Object
     */
    function sendDropDBRequest() {
    	//"this" refers to the Yes/No dialog box
    	this.hide();
        Y.log("Preparing to send request to drop DB", "info");
        var request = Y.io(MV.URLMap.dropDB(), {
            method: "POST",
            on: {
                success: function (ioId, responseObject) {
                    var parsedResponse = Y.JSON.parse(responseObject.responseText),
                    	error;
                    if (parsedResponse.response.result !== undefined) {
	                    MV.showAlertMessage(parsedResponse.response.result, MV.infoIcon, function () {
		                    window.location = "home.html?dbInfo=" + Y.one("#host").get("value") + "_" + Y.one("#port").get("value") + "_" + Y.one("#username").get("value");
	                    });
                        Y.log("[0] dropped".format(Y.one("#currentDB").get("value")), "info");
                        Y.one("#currentDB").set("value", "");
                        requestDBNames();
                    } else {
                        error = parsedResponse.response.error;
                        MV.showAlertMessage("Could not drop: [0]. [1]".format(Y.one("#currentDB").get("value"), MV.errorCodeMap[error.code]), MV.warnIcon);
                        Y.log("Could not drop: [0], Response Recieved: [1], ErrorCode: [2]".format(Y.one("#currentDB").get("value"), error.message, error.code), "error");
                    }
                },
                failure: function (ioId, responseObject) {
                    Y.log("Could not drop: [0]. Status Text: [1]".format(Y.one("#currentDB").get("value"), responseObject.statusText), "error");
                    MV.showAlertMessage("Could not drop: [0], Status Text: [2]".format(Y.one("#currentDB").get("value"), responseObject.statusText), MV.warnIcon);
                }
            }
        });
    }
    
    /**
     * The function handles event on the menu item for the database
     * @param eventType The event type
     * @param args the arguments containing information about which menu item was clicked
     */

    function handleClickEvent(event) {
        var	dialog,	showErrorMessage;
		var label = $(event.currentTarget._node).closest("ul").closest("li")[0].attributes["label"].value;
		var index = parseInt(event.currentTarget._node.attributes["index"].value);
        Y.one("#currentDB").set("value", label);
        MV.toggleClass(Y.one("#" + Y.one("#currentDB").get("value")), Y.all("#dbNames li"));
	    switch (index) {
		    case 1:
			    // add collection
			    showErrorMessage = function(responseObject) {
				    MV.showAlertMessage("Collection creation failed! Please check if app server is runnning.", MV.warnIcon);
				    Y.log("Collection creation failed. Response Status: [0]".format(responseObject.statusText), "error");
			    };
			    MV.showSubmitDialog("addColDialog", addCollection, showErrorMessage);
			    break;
		    case 2:
			    // add gridfs store
			    var onSuccess = function(response) {
				    var parsedResponse = Y.JSON.parse(response.responseText);
				    var result = parsedResponse.response.result;
				    if (result !== undefined) {
					    MV.showAlertMessage(result, MV.infoIcon);
					    Y.one("#" + Y.one("#currentDB").get("value")).simulate("click");
				    }
			    };
			    showErrorMessage = function(responseObject) {
				    MV.showAlertMessage("GridFS bucket creation failed! Please check if app server is runnning.", MV.warnIcon);
				    Y.log("GridFS bucket creation failed. Response Status: [0]".format(responseObject.statusText), "error");
			    };
			    MV.showSubmitDialog("addGridFSDialog", onSuccess, showErrorMessage);
			    break;
		    case 3:
			    // Delete database
			    dialog = MV.showYesNoDialog("Do you really want to drop the Database?", sendDropDBRequest, function(dialog) {
				    this.hide();
			    });
			    break;
		    case 4:
			    // show statistics
			    MV.hideQueryForm();
			    MV.createDatatable(MV.URLMap.dbStatistics(), Y.one("#currentDB").get("value"));
			    break;
	    }
    }

    /**
     * Gets the parameters from the URL
     */
    function getParameters() {
        var params = [], token;
        var fullUrl = window.location.search;
        var dbInfo= fullUrl.substring(fullUrl.indexOf("=")+1);
        while (dbInfo.indexOf("_") !== -1) {
            token = dbInfo.substring(0, dbInfo.indexOf("_"));
			dbInfo = dbInfo.substring(dbInfo.indexOf("_")+1);
            params.push(token);
        }
        params.push(dbInfo); // last token
        return params;
    }
    
    /**
     * Gets the user information from the URL and sets it
     */
    function setUserInfo(){
    	var params = getParameters();
    	Y.one("#host").set("value", params[0]);
        Y.one("#port").set("value", params[1]);
        Y.one("#username").set("value", params[2]);
    	Y.one('#user').set("innerHTML", Y.one("#username").get("value"));
        Y.one('#hostname').set("innerHTML", Y.one("#host").get("value"));
    }
    /**
     *  A function handler to use for successful requests to get DB names.
     *  It parses the response and checks if DB list is successfully received. If not,
     *  then prompt the user that an error occurred.
     *  @param ioId eventId
     *  @param responseObject The response Object
     */
    function showDBs(ioId, responseObject) {
        Y.log("Response Recieved of get DB request", "info");
        try {
            var parsedResponse = Y.JSON.parse(responseObject.responseText);
            if (parsedResponse.response.result !== undefined) {
	            var info, index, dbNames = "";
	            var dbTemplate = '' +
			            '<li class="yui3-menuitem" label=[0]> \
						  <a id=[1] href="#[2]" class="yui3-menu-label dbLabel navigable">[3]</a> \
						  <div id="[4]" class="yui3-menu">\
							  <div class="yui3-menu-content">\
								  <ul>\
									  <li class="yui3-menuitem">\
										  <a index="1" class="yui3-menuitem-content onclick">Add Collection</a>\
									  </li>\
									  <li class="yui3-menuitem">\
										  <a index="2" class="yui3-menuitem-content onclick">Add GridFS Bucket</a>\
									  </li>\
	                                  <li class="yui3-menuitem">\
										  <a index="3" class="yui3-menuitem-content onclick">Drop Database</a>\
									  </li>\
									  <li class="yui3-menuitem">\
										  <a index="4" class="yui3-menuitem-content onclick">Statistics</a>\
									  </li>\
								  </ul>\
							  </div>\
						  </div>\
						  </li>';
                for (index = 0; index < parsedResponse.response.result.length; index++) {
	                var id = parsedResponse.response.result[index];
	                dbNames += dbTemplate.format(id, id, id + "_subMenu", id, id + "_subMenu");
                }
                if (index === 0) {
                    dbDiv.set("innerHTML", "No Databases");
                }
                dbDiv.set("innerHTML", dbNames);	            
				var menu = Y.one("#dbNames");
	            menu.unplug(Y.Plugin.NodeMenuNav);
				menu.plug(Y.Plugin.NodeMenuNav);
	            menu.set("style.display", "block");
                MV.hideLoadingPanel();
                Y.log("Database Names succesfully loaded", "info");
                sm.publish(sm.events.dbsChanged);
            } else {
	            MV.hideLoadingPanel();
                var error = parsedResponse.response.error;
                Y.log("Could not load databases. Message from server: [0]. Error Code from server:[1] ".format(error.message, error.code), "error");
                MV.showAlertMessage(MV.errorCodeMap[error.code], MV.warnIcon);                
            }
        } catch (e) {
            MV.showAlertMessage(e, MV.warnIcon);
        }
    }

    /**
     * A function handler to use for failed requests to get DB names.
     * @param ioId
     * @param responseObject The response Object
     */ 
    function displayError(ioId, responseObject) {
        Y.log("Could not load the databases", "error");
        Y.log("Status code message: [0]".format(responseObject.statusText), "error");
        MV.hideLoadingPanel();
        MV.showAlertMessage("Could not load collections! Please check if the app server is running. Status Text: [0]".format(responseObject.statustext), MV.warnIcon);
    }
    
    /**
     * The function handles the onLoad event for the home page.
     * It sends request to get the DB names
     */
	function requestDBNames(response, a, b, c) {
		var parsedResponse = (response.responseText != undefined) ? Y.JSON.parse(response.responseText) : null;
		var error = parsedResponse == undefined ? undefined : parsedResponse.response.error;
		if (error) {                         
			MV.showAlertMessage("DB creation failed ! [0].".format(error.message), MV.warnIcon);
			Y.log("DB creation failed. Response Status: [0]".format(error.message), "error");
		} else {
			MV.showLoadingPanel("Loading Databases...");
			setUserInfo();
			var request = Y.io(MV.URLMap.getDBs(),
				// configuration for loading the database names
			{
				method: "GET",
				on: {
					success: showDBs,
					failure: displayError
				}
			});
			Y.log("Sending request to load DB names", "info");
		}
	}
        
    /**
     * The function shows a dialog that takes input (i.e. Db name) from user
     */
    function createDB()	{
        MV.showSubmitDialog("addDBDialog", requestDBNames, null);
    }

    // Make a request to load Database names when the page loads
    Y.on("load", requestDBNames);
    
    //Adding click handler for new DB button that calls createDB()
    Y.on("click", createDB, "#createDB");
});