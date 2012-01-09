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
}).use("yes-no-dialog", "alert-dialog", "io-base", "json-parse", "node-event-simulate", "node", "event-delegate", "stylize", "json-stringify", "utility", "event-key", "event-focus", "node-focusmanager", function (Y) {
    YUI.namespace('com.imaginea.mongoV');
    var MV = YUI.com.imaginea.mongoV,
    	sm = MV.StateManager;
    MV.treebleData = {};
    
    /**
     * The function is an event handler to show the documents whenever a column name is clicked  
     * @param {object} e It is an event object
     * 
     */
    var showTabView = function (e) {
        MV.toggleClass(e.currentTarget, Y.all("#collNames li"));
        sm.setCurrentColl(e.currentTarget.getContent());
        MV.mainBody.empty(true);
        
        /**
         * This function gets the query parameters from the query box. It takes the 
         * query string, the limit value, skip value and the fields selected and return a 
         * query parameter string which will be added to the request URL 
         * @returns {String} Query prameter string
         * 
         */
        function getQueryParameters() {
            var parsedQuery,
            	query = Y.one('#queryBox').get("value"),
            	limit = Y.one('#limit').get("value"),
            	skip = Y.one('#skip').get("value"),
            	fields = Y.all('#fields input'),
            	index = 0,
            	checkedFields = [],
            	item;

            if (query === "") {
                query = "{}";
            }
            
            //replace the single quotes (') in the query string by double quotes (") 
            query = query.replace(/'/g, '"');
            
            try {
                parsedQuery = Y.JSON.parse(query);
                for (index = 0; index < fields.size(); index++) {
                    item = fields.item(index);
                    if (item.get("checked")) {
                        checkedFields.push(item.get("name"));
                    }
                }
                return ("&limit=[0]&skip=[1]&fields=[2]&query=[3]".format(limit, skip, checkedFields, query));
            } catch (error) {
                Y.log("Could not parse query. Reason: [0]".format(error), "error");
                MV.showAlertDialog("Failed:Could not parse query. [0]".format(error), MV.warnIcon);
            }
        }
        
        /**
         * The function creates and XHR data source which will get all the documents.
         * A data source is created so that we don't have to send separate requests to load
         * the JSON view and the Treeble view 
         * 
         */
        function defineDatasource() {
            MV.data = new YAHOO.util.XHRDataSource(MV.URLMap.getDocs(), {
                responseType: YAHOO.util.XHRDataSource.TYPE_JSON,
                responseSchema: {
                    resultsList: "response.result",
                    metaFields: {
                        startIndex: 'first_index',
                        recordsReturned: 'records_returned',
                        totalRecords: 'total_records'
                    }
                }
            });
        }
        
        /**
         * The function sends a request to the data source create by function <tt>defineDatasource</tt>
         * to get all the documents.
         * @param {String} param The query parameter string that has to be sent to get the documents 
         */
        
        function requestDocuments(param) {
            MV.data.sendRequest(param, {
                success: showDocuments,
                failure: function (request, responseObject) {
                    MV.showAlertDialog("Failed: Documents could not be loaded", MV.warnIcon);
                    Y.log("Documents could not be loaded. Response: [0]".format(responseObject.responseText), "error");
                },
                scope: tabView
            });
        }
        
        /**
         *The function is an event handler for the execute query button. It gets the query parameters 
         *and sends a request to get the documents 
         * @param {Object} e The event object 
         */
        function executeQuery(e) {
            var queryParams = getQueryParameters();
            if (queryParams !== undefined) {
                requestDocuments(queryParams);
            }
        }
        
        /**
         *The function is success handler for the request of getting all the keys in a collections.
         *It parses the response, gets the keys and makes the query box. It also sends the request to load the 
         *documents after the query box has been populated,
         * @param {Number} e Id 
         * @param {Object} The response Object
         */
        function showQueryBox(ioId, responseObject) {
        	var parsedResponse,
        		keys,
        		queryForm,
        		error;
            Y.log("Preparing to show QueryBox", "info");
            try {
                Y.log("Parsing the JSON response to get the keys", "info");
                parsedResponse = Y.JSON.parse(responseObject.responseText);
                keys = parsedResponse.response.result;
                if (keys !== undefined) {
                    queryForm = Y.one('#queryForm');
                    queryForm.addClass('form-cont');
                    queryForm.set("innerHTML", MV.getForm(keys));
                    // insert a ctrl + enter listener for query evaluation
                    Y.one("#queryBox").on("keyup", function (eventObject) {
                        if (eventObject.ctrlKey && eventObject.keyCode === 13) {
                            Y.one('#execQueryButton').simulate('click');
                        }
                    });
                    Y.log("QueryBox loaded", "info");
                    //TODO instead of assigning it every time delegate it
                    Y.on("click", executeQuery, "#execQueryButton");
                    defineDatasource();
                    requestDocuments(getQueryParameters());
                } else {
                    error = parsedResponse.response.error;
                    Y.log("Could not get keys. Message: [0]".format(error.message), "error");
                    MV.showAlertDialog("Could not load the query Box! [0]".format(MV.errorCodeMap(error.code)), MV.warnIcon);
                }
            } catch (e) {
                Y.log("Could not parse the JSON response to get the keys", "error");
                Y.log("Response received: [0]".format(responseObject.resposeText), "error");
                MV.showAlertDialog("Cannot parse Response to get keys!", MV.warnIcon);
            }
        }
        
        /**
         * It sends request to get all  the keys of the  current collection
         */
        var getKeyRequest = Y.io(MV.URLMap.documentKeys(), {
            method: "GET",
            on: {
                success: showQueryBox,
                failure: function (ioId, responseObject) {
                    MV.showAlertDialog("Unexpected Error: Could not load the query Box", MV.warnIcon);
                    Y.log("Could not send the request to get the keys in the collection. Response Status: [0]".format(responseObject.statusText), "error");
                }
            }
        });
        
        /**
         * Sets the size of the text area according to the content in the text area.
         * @param maxHeight The maximum height if the text area
         * @param text The text of the text area
         */
        function fitToContent(maxHeight, text) {
            if (text) {
                var adjustedHeight = text.clientHeight;
                if (!maxHeight || maxHeight > adjustedHeight) {
                    adjustedHeight = Math.max(text.scrollHeight, adjustedHeight);
                    if (maxHeight) {
                        adjustedHeight = Math.min(maxHeight, adjustedHeight);
                    }
                    if (adjustedHeight > text.clientHeight) {
                        text.style.height = adjustedHeight + "px";
                    }
                }
            }
        }
        
        /**
         * The function loads the treeble view and subscibes it to the mouse over event.
         * When the mouse over over the rows the complete row is highlighted
         * @param treeble the treeble structure to be loaded
         */
        function loadAndSubscribe(treeble) {
            treeble.load();
            treeble.subscribe("rowMouseoverEvent", treeble.onEventHighlightRow);
            treeble.subscribe("rowMouseoutEvent", treeble.onEventUnhighlightRow);
        }
        
        /**
         * The function is the success handler for the request document call.
         * It calls function to write on the JSON tab and to create the treeble structure 
         * from the response data
         * @param {Object} request The request Object 
         * @param {Object} responseObject The response object containing the response of the get documents request
         * 
         */
        function showDocuments(request, responseObject) {
            Y.log("Preparing to write on JSON tab", "info");
            writeOnJSONTab(responseObject.results);
            Y.log("Preparing the treeTable data", "info");
            var treebleData = MV.getTreebleDataforDocs(responseObject);
            var treeble = MV.getTreeble(treebleData);
            loadAndSubscribe(treeble);
            Y.log("Tree table view loaded", "info");
            sm.publish(sm.events.queryFired);
        }
        
        
        var tabView = new YAHOO.widget.TabView();
        tabView.addTab(new YAHOO.widget.Tab({
            label: 'JSON',
            cacheData: true,
            active: true
        }));
        tabView.addTab(new YAHOO.widget.Tab({
            label: 'Tree Table',
            content: ' <div id="table"></div><div id="table-pagination"></div> '
        }));
        var actionMap = {
            save: "save",
            edit: "edit"
        };
        
        var idMap = {};
        function getButtonIndex(targetNode) {
            var btnID = targetNode.get("id");
            var match = btnID.match(/\d+/);
            return (parseInt(match[0], 10));
        }
        
        /**
         * The function toggles the save/cancel and edit/delete buttons. It just adds/removes class invisible.
         * Also it makes the textArea disabled/enabled based on the condition
         * @param targetNode The dom element on which is clicked
         * @param index the index number of the node that is clicked
         * @param action The action (save/edit) that has been performed
         */
        function toggleSaveEdit(targetNode, index, action) {
        	var textArea = Y.one('#doc' + index).one("pre").one("textarea");
            if (action === actionMap.save) {
            	textArea.addClass('disabled');
                textArea.setAttribute("disabled", "disabled");
            	Y.one("#save"+index).addClass("invisible");
            	Y.one("#cancel"+index).addClass("invisible");
            	Y.one("#edit"+index).removeClass("invisible");
            	Y.one("#delete"+index).removeClass("invisible");
            } else {
            	textArea.removeAttribute("disabled");
                textArea.removeClass('disabled');
            	Y.one("#edit"+index).addClass("invisible");
            	Y.one("#delete"+index).addClass("invisible");
            	Y.one("#save"+index).removeClass("invisible");
            	Y.one("#cancel"+index).removeClass("invisible");
            }
            targetNode.focus();
        }
        /**
         * Success handler for update document request. The function checks the response
         * if the document has been updated successfully or not. If not shows the error.
         * @param ioId the event ID
         * @param responseObject the response object
         */
        function parseUpdateDocResponse(ioId, responseObject) {
            var parsedResponse = Y.JSON.parse(responseObject.responseText);
            response = parsedResponse.response.result;
            if (response !== undefined) {
                MV.showAlertDialog("Document updated", MV.infoIcon);
                Y.log("Document update to [0]".format(response), "info");
                Y.one("#" + Y.one("#currentColl").get("value").replace(/ /g, '_')).simulate("click");
            } else {
                var error = parsedResponse.response.error;
                MV.showAlertDialog("Could not update Document! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon, function () {
                    Y.one("#" + Y.one("#currentColl").get("value").replace(/ /g, '_')).simulate("click");
                });
                Y.log("Could not update Document! [0]".format(MV.errorCodeMap[error.code]), "error");
            }
        }
        
        /**
         * The function sends the update Document request.
         * @param doc The updated document 
         * @param id The id of the updated document
         */
        function sendUpdateDocRequest(doc, id) {
            var updateDocumentRequest = Y.io(MV.URLMap.updateDoc(), {
                method: "POST",
                data: "_id=" + id + "&keys=" + doc,
                on: {
                    success: parseUpdateDocResponse,
                    failure: function (ioId, responseObject) {
                        MV.showAlertDialog("Unexpected Error: Could not update the document. Check if app server is running", MV.warnIcon);
                        Y.log("Could not send the request to update the document. Response Status: [0]".format(responseObject.statusText), "error");
                    }
                }
            });
        }
        
        /**
         * The function checks of all keys are selected in the fields list of the query box
         */
        function allKeysSelected() {
            var fields = Y.all('#fields input');
            var index;
            for (index = 0; index < fields.size(); index++) {
                var item = fields.item(index);
                if (!item.get("checked")) {
                    return false;
                }
            }
            return true;
        }
        /**
         * The function marks all the keys in the query box as checkd
         */
        function selectAllKeys() {
            var fields = Y.all('#fields input');
            var index;
            for (index = 0; index < fields.size(); index++) {
                var item = fields.item(index);
                item.set("checked", "true");
            }
            executeQuery();
            this.hide();
        }
        /**
         * The function is an event handler to handle the delete button click.
         * It sends request to delete the document
         * @param eventObject The event Object 
         */
        function deleteDoc(eventObject) {
            var btnIndex;
            var sendDeleteDocRequest = function () {
                var targetNode = eventObject.currentTarget;
                var index = getButtonIndex(targetNode);
                var doc = Y.one('#doc' + index).one("pre").one("textarea").get("value");
                parsedDoc = Y.JSON.parse(doc);
                var id = parsedDoc._id;
                var request = Y.io(MV.URLMap.deleteDoc(),
                                   // configuration for dropping the document
                                   {
                                       method: "POST",
                                       data: "_id=" + id,
                                       on: {
                                           success: function (ioId, responseObj) {
                                               var parsedResponse = Y.JSON.parse(responseObj.responseText);
                                               response = parsedResponse.response.result;
                                               if (response !== undefined) {
                                                   MV.showAlertDialog("Document deleted", MV.infoIcon);
                                                   Y.log("Document with _id= [0] deleted. Response: [1]".format(id, response), "info");
                                                   Y.one("#" + Y.one("#currentColl").get("value").replace(/ /g, '_')).simulate("click");
                                               } else {
                                                   var error = parsedResponse.response.error;
                                                   MV.showAlertDialog("Could not delete the document with _id [0]. [1]".format(id, MV.errorCodeMap[error.code]), MV.warnIcon);
                                                   Y.log("Could not delete the document with _id =  [0], Error message: [1], Error Code: [2]".format(id, error.message, error.code), "error");
                                               }
                                           },
                                           failure: function (ioId, responseObj) {
                                               Y.log("Could not delete the document .Status text: ".format(Y.one("#currentColl").get("value"), responseObj.statusText), "error");
                                               MV.showAlertDialog("Could not drop the document! Please check if your app server is running and try again. Status Text: [1]".format(responseObj.statusText), MV.warnIcon);
                                           }
                                       }
                                   });
                this.hide();
            };
            if (eventObject.currentTarget.hasClass('deletebtn')) {
                MV.showYesNoDialog("Do you really want to drop the document ?", sendDeleteDocRequest, function () {
                    this.hide();
                });
            } else {
                //get the sibling save/edit btn and toggle using that
                btnIndex = getButtonIndex(eventObject.currentTarget);
                toggleSaveEdit(Y.one('#delete'+btnIndex).get('parentNode').one('button'), btnIndex, actionMap.save);
            }
        }
        /**
         * The function is an event handler for the save button click.
         * @param eventObject The event Object
         */
        function saveDoc(eventObject) {
            var parsedDoc;
            var targetNode = eventObject.currentTarget;
            var index = getButtonIndex(targetNode);
            var textArea = Y.one('#doc' + index).one("pre").one("textarea");
            var doc = textArea.get("value");
            doc = doc.replace(/'/g, '"');
            try {
                parsedDoc = Y.JSON.parse(doc);
                sendUpdateDocRequest(Y.JSON.stringify(parsedDoc), idMap[index].id);
                toggleSaveEdit(targetNode, index, actionMap.save);
            } catch (e) {
                MV.showAlertDialog("The document entered is not in the correct JSON format",MV.warnIcon,function(){
                	textArea.focus();
                	this.hide();
                });
                
            }
        }
        /**
         * The function is an event handler for the cancel button click
         * @param eventObject The event Object
         */
        function cancelSave(eventObject){
        	 var targetNode = eventObject.currentTarget;
             var index = getButtonIndex(targetNode);
             var textArea = Y.one('#doc' + index).one("pre").one("textarea");
             textArea.set("value",idMap[index].originalDoc);
             toggleSaveEdit(targetNode, index, actionMap.save);
        }
        /**
         * The function is an event handler for the edit button click
         * @param eventObject The event Object
         */
        function editDoc(eventObject) {
            if (!allKeysSelected()) {
                MV.showYesNoDialog("To edit a document you need check all keys in query box. Click YES to do so, NO to cancel", selectAllKeys, function () {
                    this.hide();
                });
            } else {
                var targetNode = eventObject.currentTarget;
                var index = getButtonIndex(targetNode);
                var textArea = Y.one('#doc' + index).one("pre").one("textarea");
                var doc = textArea.get("value");
                parsedDoc = Y.JSON.parse(doc);
                idMap[index] = {};
                idMap[index].id = parsedDoc._id;
                idMap[index].originalDoc = doc;
                toggleSaveEdit(targetNode, index, actionMap.edit);
                textArea.focus();
            }
        }
        /**
         * The function creates the json view and adds the edit,delete,save and cancel buttons for each document
         * @param response The response Object containing all the documents
         */
        function writeOnJSONTab(response) {
            var jsonView = "<div class='buffer jsonBuffer navigable navigateTable' id='jsonBuffer'>";
            var i;
            var trTemplate = ["<tr>",
                              "  <td id='doc[0]'>",
                              "      <pre> <textarea id='ta[1]' class='disabled non-navigable' disabled='disabled' cols='75'>[2]</textarea></pre>",
                              "  </td>",
                              "  <td>",
                              "  <button id='edit[3]'class='btn editbtn non-navigable'>edit</button>",
                              "   <button id='delete[4]'class='btn deletebtn non-navigable'>delete</button>",
                              "   <button id='save[5]'class='btn savebtn non-navigable invisible'>save</button>",
                              "   <button id='cancel[6]'class='btn cancelbtn non-navigable invisible'>cancel</button>",
                              "   <br/>",
                              "  </td>",
                              "</tr>"].join('\n');
            jsonView += "<table class='jsonTable'><tbody>";
            
            for (i = 0; i < response.length; i++) {
                jsonView += trTemplate.format(i, i, Y.JSON.stringify(response[i], null, 4),i, i, i, i);
            }
            if (i === 0) {
                jsonView = jsonView + "No documents to be displayed";
            }
            jsonView = jsonView + "</tbody></table></div>";
            tabView.getTab(0).setAttributes({
                content: jsonView
            }, false);
            for (i = 0; i < response.length; i++) {
                Y.on("click", editDoc, "#edit" + i);
                Y.on("click", deleteDoc, "#delete" + i);
                Y.on("click", saveDoc, "#save" + i);
                Y.on("click", cancelSave, "#cancel" + i);
            }
            for (i = 0; i < response.length; i++) {
                fitToContent(500, document.getElementById("ta" + i));
            }
            var trSelectionClass = 'selected';
            // add click listener to select and deselect rows.
            Y.all('.jsonTable tr').on("click", function (eventObject) {
                var currentTR = eventObject.currentTarget;
                var alreadySelected = currentTR.hasClass(trSelectionClass);

                Y.all('.jsonTable tr').each(function(item) {
                    item.removeClass(trSelectionClass);
                });

                if (!alreadySelected) {
                    currentTR.addClass(trSelectionClass);
                    var editBtn = currentTR.one('button.editbtn');
                    if (editBtn) {
                        editBtn.focus();
                    }
                }
            });
            Y.on('blur', function(eventObject) {
                var resetAll = true;
                // FIXME ugly hack for avoiding blur when scroll happens
                if (sm.isNavigationSideEffect()) {
                    resetAll = false;
                }
                if (resetAll) {
                    Y.all('tr.selected').each(function(item) {
                        item.removeClass(trSelectionClass);
                    });
                }
            }, 'div.jsonBuffer');

            Y.on('keyup', function(eventObject) {
                var firstItem;
                // escape edit mode
                if (eventObject.keyCode === 27) {
                    Y.all("button.savebtn").each(function(item) {
                        toggleSaveEdit(item, getButtonIndex(item), actionMap.save);
                        if (!(firstItem)) {
                            firstItem = item;
                        }
                    });
                }
            }, 'div.jsonBuffer');
            Y.log("The documents written on the JSON tab", "debug");
        }
        MV.header.set("innerHTML", "Contents of " + Y.one("#currentColl").get("value"));
        tabView.appendTo(MV.mainBody.get('id'));
    };
    Y.delegate("click", showTabView, "#collNames", "li");
});