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
}).use("loading-panel", "alert-dialog", "utility", "submit-dialog", "yes-no-dialog", "io", "node", "node-menunav", "json-parse", "event-delegate", "node-event-simulate", "stylize", "custom-datatable", function(Y) {
	var MV = YUI.com.imaginea.mongoV,
			sm = MV.StateManager,
			collDiv = Y.one("#collNames ul.lists"),
			gridFSDiv = Y.one("#bucketNames ul.lists");
	collDiv.delegate('click', handleCollectionClickEvent, 'a.onclick');
	gridFSDiv.delegate('click', handleBucketClickEvent, 'a.onclick');

	/**
	 * Click event handler on the database name. It sets the current DB and
	 * sends he request to get the list of collections 
	 * @param e The event Object
	 */
	function requestCollNames(e) {
		Y.one("#currentDB").set("value", e.currentTarget.get("text"));
		Y.one("#currentColl").set("value", "");
		Y.one("#collNames").unplug(Y.Plugin.NodeMenuNav);
		Y.one("#bucketNames").unplug(Y.Plugin.NodeMenuNav);
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

	/**
	 * A function handler to use for successful get Collection Names requests.
	 * It parses the response and checks if correct response is received. If and error is received
	 * then notify the user.
	 * @param oId the event Id object
	 * @param responseObject The response Object
	 */
	function displayCollectionNames(oId, responseObject) {
		Y.log("Response Recieved of get collection request", "info");
		var parsedResponse, parsedResult, info, index, error, collections = "", gridFSBuckets = "";
		try {
			parsedResponse = Y.JSON.parse(responseObject.responseText);
			parsedResult = parsedResponse.response.result;

			var collTemplate = '' +
					'<li class="yui3-menuitem" label="[0]"> \
	                <a id=[1] label="[2]" href="#[3]" class="collectionLabel yui3-menu-label navigable">[4]</a> \
					<div id="[5]" class="yui3-menu">\
						<div class="yui3-menu-content">\
							<ul>\
								<li class="yui3-menuitem">\
									<a index="1" class="yui3-menuitem-content onclick">Add Document</a>\
								</li>\
								<li class="yui3-menuitem">\
									<a index="2" class="yui3-menuitem-content onclick">Drop Collection</a>\
								</li>\
								<li class="yui3-menuitem">\
									<a index="3" class="yui3-menuitem-content onclick">Statistics</a>\
								</li>\
							</ul>\
						</div>\
					</div>\
		            </li>';
			var bucketTemplate = '' +
					'<li class="yui3-menuitem" label="[0]"> \
	                <a id=[1] label="[2]" href="#[3]" class="collectionLabel yui3-menu-label">[4]</a> \
					<div id="[5]" class="yui3-menu">\
						<div class="yui3-menu-content">\
							<ul>\
								<li class="yui3-menuitem">\
									<a index="1" class="yui3-menuitem-content onclick">Add File(s)</a>\
								</li>\
								<li class="yui3-menuitem">\
									<a index="2" class="yui3-menuitem-content onclick">Drop Bucket</a>\
								</li>\
								<li class="yui3-menuitem">\
									<a index="3" class="yui3-menuitem-content onclick">Statistics</a>\
								</li>\
							</ul>\
						</div>\
					</div>\
		            </li>';

			var hasCollections = false, hasFiles = false;
			if (parsedResult) {
				for (index = 0; index < parsedResult.length; index++) {
					var collectionName = parsedResult[index];
					var formattedName = collectionName.length > 20 ? collectionName.substring(0, 20) + "..." : collectionName;
					var pos = collectionName.lastIndexOf(".files");
					var id;
					if (pos > 0) {
						collectionName = collectionName.substring(0, pos);
						formattedName = collectionName.length > 20 ? collectionName.substring(0, 20) + "..." : collectionName;						
						id = collectionName.replace(/ /g, '_');
						id = id.replace('.', '_');
						gridFSBuckets += bucketTemplate.format(collectionName, id, collectionName, id + "_subMenu", formattedName, id + "_subMenu");
						hasFiles = true;
					}
					// Issue 17 https://github.com/Imaginea/mViewer/issues/17
					if (pos < 0 && collectionName.search(".chunks") < 0) {
						id = collectionName.replace(/ /g, '_');
						collections += collTemplate.format(collectionName, id, collectionName, id + "_subMenu", formattedName, id + "_subMenu");
						hasCollections = true;
					}
				}

				if (!hasFiles) gridFSBuckets = "&nbsp&nbsp No Files present.";
				if (!hasCollections)	collections = "&nbsp&nbsp No Collections present.";

				collDiv.set("innerHTML", collections);
				gridFSDiv.set("innerHTML", gridFSBuckets);

				var menu1 = Y.one("#collNames");
				menu1.plug(Y.Plugin.NodeMenuNav);
				menu1.set("style.display", "block");
				var menu2 = Y.one("#bucketNames");
				menu2.plug(Y.Plugin.NodeMenuNav);
				menu2.set("style.display", "block");
				sm.publish(sm.events.collectionsChanged);
				MV.hideLoadingPanel();
				Y.log("Collection Names succesfully loaded", "info");
			} else {
				error = parsedResponse.response.error;
				Y.log("Could not load collections. Message: [0]".format(error.message), "error");
				MV.hideLoadingPanel();
				MV.showAlertMessage("Could not load Collections! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
			}
		} catch (e) {
			MV.hideLoadingPanel();
			MV.showAlertMessage(e, MV.warnIcon);
		}
	}

	/**
	 * The function handles click event on the menu item for the collection
	 * @param eventType The event type
	 * @param args the arguments containing information about which menu item was clicked
	 */
	function handleCollectionClickEvent(event) {
		var label = $(event.currentTarget._node).closest("ul").closest("li")[0].attributes["label"].value;
		var index = parseInt(event.currentTarget._node.attributes["index"].value);
		Y.one("#currentColl").set("value", label);
		MV.toggleClass(sm.currentCollAsNode(), Y.all("#collNames li"));
		switch (index) {
			case 1:
				// Add Document
				var showError = function(responseObject) {
					MV.showAlertMessage("Document creation failed! Please check if your app server is running and then refresh the page.", MV.warnIcon);
					Y.log("Document creation failed. Response Status: [0]".format(responseObject.statusText), "error");
				};
				MV.showSubmitDialog("addDocDialog", addDocument, showError);
				break;
			case 2:
				// Delete
				MV.showYesNoDialog("Do you really want to drop the Collection - " + Y.one("#currentColl").get("value") + "?", dropCollection, function() {
					this.hide();
				});
				break;
			case 3:
				// click to view details
				MV.hideQueryForm();
				MV.createDatatable(MV.URLMap.collStatistics(), Y.one("#currentColl").get("value"));
				break;
		}
	}

	/**
	 * The function handles event on the context menu for the bucket
	 * @param eventType The event type
	 * @param args the arguments containing information about which menu item was clicked
	 */
	function handleBucketClickEvent(event) {
		var label = $(event.currentTarget._node).closest("ul").closest("li")[0].attributes["label"].value;
		var index = parseInt(event.currentTarget._node.attributes["index"].value);
		Y.one("#currentBucket").set("value", label);
		MV.toggleClass(sm.currentBucketAsNode(), Y.all("#bucketNames li"));
		switch (index) {
			case 1:
				// Add File
				var showErrorMessage = function(responseObject) {
					MV.showAlertMessage("File upload failed! Please check if your app server is running and then refresh the page.", MV.warnIcon);
					Y.log("File upload failed. Response Status: [0]".format(responseObject.statusText), "error");
				};
				MV.showUploadDialog("addFileDialog");
				break;
			case 2:
				// Delete
				MV.showYesNoDialog("Do you really want to drop all files in this bucket - " + Y.one("#currentBucket").get("value") + "?", sendDropBucketRequest, function() {
					this.hide();
				});
				break;
			case 3:
				// click to view details
				MV.hideQueryForm();
				MV.createDatatable(MV.URLMap.bucketStatistics(".files"), Y.one("#currentBucket").get("value"));
				MV.createDatatable(MV.URLMap.bucketStatistics(".chunks"), Y.one("#currentBucket").get("value"));
				break;
		}
	}

	/**
	 * Handler for drop bucket request.
	 * @param responseObject The response Object
	 */
	function sendDropBucketRequest() {
		//"this" refers to the Yes/No dialog box
		this.hide();
		Y.log("Preparing to send request to drop bucket", "info");
		var request = Y.io(MV.URLMap.dropBucket(), {
			on: {
				success: function(ioId, responseObj) {
					var parsedResponse = Y.JSON.parse(responseObj.responseText);
					response = parsedResponse.response.result;
					if (response !== undefined) {
						Y.log(response, "info");
						MV.showAlertMessage(response, MV.infoIcon);
						Y.one("#" + Y.one("#currentDB").get("value")).simulate("click");
					} else {
						var error = parsedResponse.response.error;
						MV.showAlertMessage("Could not delete all files : [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
						Y.log("Could not delete all files, Error message: [0], Error Code: [1]".format(error.message, error.code), "error");
					}
				},
				failure: function(ioId, responseObj) {
					Y.log("Could not delete the file. Status text: ".format(Y.one("#currentBucket").get("value"), responseObj.statusText), "error");
					MV.showAlertMessage("Could not drop the file! Please check if your app server is running and try again. Status Text: [1]".format(responseObj.statusText), MV.warnIcon);
				}
			}
		});
	}

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
						MV.showAlertMessage(response, MV.infoIcon);
						Y.log("[0] dropped. Response: [1]".format(Y.one("#currentColl").get("value"), response), "info");
						sm.clearCurrentColl();
						Y.one("#" + Y.one("#currentDB").get("value")).simulate("click");
					} else {
						error = parsedResponse.response.error;
						MV.showAlertMessage("Could not drop: [0]. [1]".format(Y.one("#currentColl").get("value"), MV.errorCodeMap[error.code]), MV.warnIcon);
						Y.log("Could not drop [0], Error message: [1], Error Code: [2]".format(Y.one("#currentColl").get("value"), error.message, error.code), "error");
					}
				},
				failure: function(ioId, responseObj) {
					Y.log("Could not drop [0].Status text: ".format(Y.one("#currentColl").get("value"), responseObj.statusText), "error");
					MV.showAlertMessage("Could not drop [0]!  Please check if your app server is running and try again. Status Text: [1]".format(Y.one("#currentColl").get("value"), responseObj.statusText), MV.warnIcon);
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
			MV.showAlertMessage("New document added successfully to collection '[0]'".format(Y.one("#currentColl").get("value")), MV.infoIcon);
			Y.log("New document added to [0]".format(Y.one("#currentColl").get("value"), "info"));
			sm.currentCollAsNode().simulate("click");
		} else {
			error = parsedResponse.response.error;
			MV.showAlertMessage("Could not add Document ! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
			Y.log("Could not add Document! [0]".format(MV.errorCodeMap[error.code]), "error");
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
			MV.showAlertMessage("Could not load collections! Check if your app server is running and refresh the page.", MV.warnIcon);
		}
		MV.hideLoadingPanel();
	}

	// Make request to load collection names when a database name is clicked
	Y.delegate("click", requestCollNames, "#dbNames", "a.dbLabel");
});