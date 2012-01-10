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
}).use("loading-panel","alert-dialog", "utility", "dialog-box", "yes-no-dialog", "io-base", "node", "json-parse", "event-delegate", "node-event-simulate", "stylize", "custom-datatable", function (Y) {
    // TODO: make loading panel module
    var dbDiv;
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
            MV.showAlertDialog("[0] added to [1]".format(sm.newName(), sm.currentDB()), MV.infoIcon);
            Y.log("[0] created in [1]".format(Y.one("#newName").get("value"), Y.one("#currentDB").get("value")), "info");
            sm.clearCurrentColl();
            Y.one("#" + Y.one("#currentDB").get("value")).simulate("click");
        } else {
            error = parsedResponse.response.error;
            MV.showAlertDialog("Could not add Collection! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
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
                        MV.showAlertDialog("[0] is dropped! ".format(Y.one("#currentDB").get("value")), MV.infoIcon, function () {
                            window.location = "home.html?dbInfo=" + Y.one("#host").get("value")+"_" + Y.one("#port").get("value") + "_" + Y.one("#username").get("value");
                        });
                        Y.log("[0] dropped".format(Y.one("#currentDB").get("value")), "info");
                        Y.one("#currentDB").set("value", "");
                    } else {
                        error = parsedResponse.response.error;
                        MV.showAlertDialog("Could not drop: [0]. [1]".format(Y.one("#currentDB").get("value"), MV.errorCodeMap[error.code]), MV.warnIcon);
                        Y.log("Could not drop: [0], Response Recieved: [1], ErrorCode: [2]".format(Y.one("#currentDB").get("value"), error.message, error.code), "error");
                    }
                },
                failure: function (ioId, responseObject) {
                    Y.log("Could not drop: [0]. Status Text: [1]".format(Y.one("#currentDB").get("value"), responseObject.statusText), "error");
                    MV.showAlertDialog("Could not drop: [0], Status Text: [2]".format(Y.one("#currentDB").get("value"), responseObject.statusText), MV.warnIcon);
                }
            }
        });
    }
    
    /**
     * The function handles event on the context menu for the database
     * @param eventType The event type
     * @param args the arguments containing information about which menu item was clicked
     */

    function handleContextMenu(eventType, args) {
        var menuItem = args[1], // The MenuItem that was clicked
        	dialog,
        	form,
        	showErrorMessage;
        Y.one("#currentDB").set("value", this.contextEventTarget.id);
        MV.toggleClass(Y.one("#" + Y.one("#currentDB").get("value")), Y.all("#dbNames li"));
        switch (menuItem.index) {
        case 0:
            // Delete database
            dialog = MV.showYesNoDialog("Do you really want to drop the Database?", sendDropDBRequest, function(dialog){this.hide();});
            break;
        case 1:
            // add collection
            form = "addColDialog";
            showErrorMessage = function(responseObject) {
                MV.showAlertDialog("Collection creation failed! Please check if app server is runnning.", MV.warnIcon);
                Y.log("Collection creation failed. Response Status: [0]".format(responseObject.statusText), "error");
            };
            MV.getDialog(form, addCollection, showErrorMessage);
            break;
        case 2:
            // show statistics
            MV.hideQueryForm();
            MV.createDatatable(MV.URLMap.dbStatistics(), Y.one("#currentDB").get("value"));
            break;
        }
    }

    var dbContextMenu = new YAHOO.widget.ContextMenu("dbContextMenuID", {
        trigger: "dbNames",
        itemData: ["Delete Database", "Add Collection", "Statistics"]
    });
    dbContextMenu.render("dbContextMenu");
    dbContextMenu.clickEvent.subscribe(handleContextMenu);
   
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
                for (index = 0; index < parsedResponse.response.result.length; index++) {
                    dbNames += "<li id='[0]' >[1]</li>".format(parsedResponse.response.result[index], parsedResponse.response.result[index]);
                }
                if (index === 0) {
                    dbDiv.set("innerHTML", "No Databases");
                }
                dbDiv.set("innerHTML", dbNames);
                MV.hideLoadingPanel();
                Y.log("Database Names succesfully loaded", "info");
                sm.publish(sm.events.dbsChanged);
            } else {
                var error = parsedResponse.response.error;
                Y.log("Could not load databases. Message from server: [0]. Error Code from server:[1] ".format(error.message, error.code), "error");
                MV.showAlertDialog(MV.errorCodeMap[error.code], MV.warnIcon);
                MV.hideLoadingPanel();
            }
        } catch (e) {
            MV.showAlertDialog(e, MV.warnIcon);
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
        MV.showAlertDialog("Could not load collections! Please check if the app server is running. Status Text: [0]".format(responseObject.statustext), MV.warnIcon);
    }
    
    /**
     * The function handles the onLoad event for the home page.
     * It sends request to get the DB names
     */
    function requestDBNames() {
    	MV.showLoadingPanel("Loading Databases...");
        setUserInfo();
        dbDiv = Y.one('#dbNames ul.lists');
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

    // Make a request to load Database names when the page loads
    Y.on("load", requestDBNames);
});