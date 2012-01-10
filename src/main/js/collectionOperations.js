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

/**
 * Contains all the collection related operations
 */
YUI({
    filter: 'raw'
}).use("loading-panel","alert-dialog", "utility", "dialog-box", "yes-no-dialog", "io", "node", "json-parse", "event-delegate", "node-event-simulate", "stylize", "custom-datatable", function(Y) {
    var MV = YUI.com.imaginea.mongoV,
        sm = MV.StateManager,
        collDiv = Y.one("#collNames ul.lists");

    // The Collection context menu object
    var collContextMenu = new YAHOO.widget.ContextMenu("collContextMenuID", {
        trigger: "collNames",
        itemData: ["Delete", "Add Document", "Statistics"],
        lazyload: true
    });

    /**
     * The function is the handler function for dropping the collection. This function is called
     * when the user clicks on "YES" on the YesNO dialog box for confirming if the user wants to
     * drop the collection or not.   
     */

    function dropCollection() {
        //"this" refers to the YesNO dialog box
        this.hide();
        var request = Y.io(MV.URLMap.dropColl(),
        // configuration for dropping the collection
        {
            method: "POST",
            on: {
                success: function(ioId, responseObj) {
                    var parsedResponse = Y.JSON.parse(responseObj.responseText),
                    	response = parsedResponse.response.result,
                    	error;
                    if (response !== undefined) {
                        MV.showAlertDialog(Y.one("#currentColl").get("value") + " dropped", MV.infoIcon);
                        Y.log("[0] dropped. Response: [1]".format(Y.one("#currentColl").get("value"), response), "info");
                        sm.clearCurrentColl();
                        Y.one("#" + Y.one("#currentDB").get("value")).simulate("click");
                    } else {
                        error = parsedResponse.response.error;
                        MV.showAlertDialog("Could not drop: [0]. [1]".format(Y.one("#currentColl").get("value"), MV.errorCodeMap[error.code]), MV.warnIcon);
                        Y.log("Could not drop [0], Error message: [1], Error Code: [2]".format(Y.one("#currentColl").get("value"), error.message, error.code), "error");
                    }
                },
                failure: function(ioId, responseObj) {
                    Y.log("Could not drop [0].Status text: ".format(Y.one("#currentColl").get("value"), responseObj.statusText), "error");
                    MV.showAlertDialog("Could not drop [0]!  Please check if your app server is running and try again. Status Text: [1]".format(Y.one("#currentColl").get("value"), responseObj.statusText), MV.warnIcon);
                }
            }
        });
    }

    /**
     * THe function handles the successful sending of the add Document request
     * @param responseObject the response object
     */

    function addDocument(responseObject) {
        var parsedResponse = Y.JSON.parse(responseObject.responseText),
            response = parsedResponse.response.result,
            error;
        if (response !== undefined) {
            MV.showAlertDialog("New document added to [0]".format(Y.one("#currentColl").get("value")), MV.infoIcon);
            Y.log("New document added to [0]".format(Y.one("#currentColl").get("value"), "info"));
            sm.currentCollAsNode().simulate("click");
        } else {
            error = parsedResponse.response.error;
            MV.showAlertDialog("Could not add Document! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
            Y.log("Could not add Document! [0]".format(MV.errorCodeMap[error.code]), "error");
        }
    }

    /**
     * The function handles event on the context menu for the collection
     * @param eventType The event type
     * @param args the arguments containing information about which menu item was clicked
     */

    function handleContextMenu(eventType, args) {
        var menuItem = args[1],	// The MenuItem that was clicked
        	form,
        	showErrorMessage;
        sm.setCurrentColl(this.contextEventTarget.innerHTML);
        MV.toggleClass(sm.currentCollAsNode(), Y.all("#collNames li"));
        switch (menuItem.index) {
        case 0:
            // Delete
            MV.showYesNoDialog("Do you really want to drop the Collection - " + Y.one("#currentColl").get("value") + "?", dropCollection, function() {
                this.hide();
            });
            break;
        case 1:
            // Add Document
            form = "addDocDialog";
            showErrorMessage = function(responseObject) {
                MV.showAlertDialog("Document creation failed! Please check if your app server is running and then refresh the page.", MV.warnIcon);
                Y.log("Document creation failed. Response Status: [0]".format(responseObject.statusText), "error");
            };
            MV.getDialog(form, addDocument, showErrorMessage);
            break;
        case 2:
            // click to view details
            MV.hideQueryForm();
            MV.createDatatable(MV.URLMap.collStatistics(), Y.one("#currentColl").get("value"));
            break;
        }
    }

    collContextMenu.subscribe("render", function(eventType, args) {
        this.subscribe("click", handleContextMenu);
    });

    /**
     * A function handler to use for successful get Collection Names requests.
     * It parses the response and checks if correct response is received. If and error is received
     * then notify the user.
     * @param oId the event Id object
     * @param responseObject The response Object
     */

    function displayCollectionNames(oId, responseObject) {
        Y.log("Response Recieved of get collection request", "info");
        var parsedResponse, parsedResult, info, index, error, collections = "";
        try {
            parsedResponse = Y.JSON.parse(responseObject.responseText);
            parsedResult = parsedResponse.response.result;

            if (parsedResult) {
                for (index = 0; index < parsedResult.length; index++) {
                    var collectionName = parsedResult[index];
                    // Issue 17 https://github.com/Imaginea/mViewer/issues/17
                    collections += "<li id='[0]' >[1]</li>".format(collectionName.replace(/ /g, '_'), collectionName);
                }
                if (index === 0) {
                    collections = "No Collections";
                }
                collDiv.set("innerHTML", collections);
                MV.hideLoadingPanel();
                sm.publish(sm.events.collectionsChanged);
                Y.log("Collection Names succesfully loaded", "info");
            } else {
                error = parsedResponse.response.error;
                Y.log("Could not load collections. Message: [0]".format(error.message), "error");
                MV.showAlertDialog("Could not load Collections! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
                MV.hideLoadingPanel();
            }
        } catch (e) {
            MV.showAlertDialog(e, MV.warnIcon);
        }
    }

    /**
     *  A function handler to use for unsuccessful get Collection request.
     *  This function is called whenever sending request for getting collection list fails.
     *  @param oId the event Id object
     * @param responseObject The response Object
     */

    function displayError(ioId, responseObj) {
        if (responseObj.responseText) {
            Y.log("Could not load collections. Status message: [0]".format(responseObj.statusText), "error");
            MV.showAlertDialog("Could not load collections! Check if your app server is running and refresh the page.", MV.warnIcon);
        }
        MV.hideLoadingPanel();
    }

    /**
     * The function is click event handler on the database name. It sets the current DB and
     * sends he request to get collection list
     * @param e The event Object
     */

    function requestCollNames(e) {
        Y.one("#currentDB").set("value", e.currentTarget.get("id"));
        Y.one("#currentColl").set("value", "");
        MV.createDatatable(MV.URLMap.dbStatistics(), Y.one("#currentDB").get("value"));
        MV.toggleClass(e.currentTarget, Y.all("#dbNames li"));
        MV.hideQueryForm();
        MV.showLoadingPanel("Loading Collections...");
        Y.log("Initiating request to load collections.", "info");
        var request = Y.io(MV.URLMap.getColl(), {
            // configuration for loading the collections
            method: "GET",
            on: {
                success: displayCollectionNames,
                failure: displayError
            }
        });
    }
    // Make request to load collection names when a database name is clicked
    Y.delegate("click", requestCollNames, "#dbNames", "li");
});