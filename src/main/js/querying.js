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
}).use("yes-no-dialog", "alert-dialog", "io-base", "json-parse", "node-event-simulate", "node", "event-delegate", "stylize", "json-stringify", "utility", function (Y) {
    Y.namespace('com.imaginea.mongoV');
    var MV = Y.com.imaginea.mongoV;
    MV.treebleData = {};
    var showTabView = function (e) {
            var treeble;
            MV.toggleClass(e.currentTarget, Y.all("#collNames li"));
            Y.one("#currentColl").set("value", e.currentTarget.get("id"));
            MV.mainBody.empty(true);
            var getQueryParameters = function () {
                    var parsedQuery, query = Y.one('#queryBox').get("value");
                    var limit = Y.one('#limit').get("value");
                    var skip = Y.one('#skip').get("value");
                    var fields = Y.all('#fields input');
                    var index = 0;
                    if (query === "") {
                        query = "{}";
                    }
                    query = query.replace(/'/g, '"');
                    var checkedFields = [];
                    try {
                        parsedQuery = Y.JSON.parse(query);
                        for (index = 0; index < fields.size(); index++) {
                            var item = fields.item(index);
                            if (item.get("checked")) {
                                checkedFields.push(item.get("name"));
                            }
                        }
                        return ("&limit=[0]&skip=[1]&fields=[2]&query=[3]".format(limit, skip, checkedFields, query));
                    } catch (error) {
                        Y.log("Could not parse query. Reason: [0]".format(error), "error");
                        MV.showAlertDialog("Failed:Could not parse query. [0]".format(error), MV.warnIcon);
                    }
                };
            var defineDatasource = function () {
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
                };
            var sendDocumentRequest = function (param) {
                    MV.data.sendRequest(param, {
                        success: showDocuments,
                        failure: function (request, responseObject) {
                            MV.showAlertDialog("Failed: Documents could not be loaded", MV.warnIcon);
                            Y.log("Documents could not be loaded. Response: [0]".format(responseObject.responseText), "error");
                        },
                        scope: tabView
                    });
                };
            var executeQuery = function (e) {
                    var queryParams = getQueryParameters();
                    if (queryParams !== undefined) {
                        sendDocumentRequest(queryParams);
                    }
                };
            var showQueryBox = function (ioId, responseObject) {
                    Y.log("Preparing to show QueryBox", "info");
                    try {
                        Y.log("Parsing the JSON response to get the keys", "info");
                        var parsedResponse = Y.JSON.parse(responseObject.responseText);
                        var keys = parsedResponse.response.result;
                        if (keys !== undefined) {
                            var queryForm = Y.one('#queryForm');
                            queryForm.addClass('form-cont');
                            queryForm.set("innerHTML", MV.getForm(keys));
                            Y.log("QueryBox loaded", "info");
                            Y.on("click", executeQuery, "#execQueryButton");
                            defineDatasource();
                            sendDocumentRequest(getQueryParameters());
                        } else {
                            var error = parsedResponse.response.error;
                            Y.log("Could not get keys. Message: [0]".format(error.message), "error");
                            MV.showAlertDialog("Could not load the query Box! [0]".format(MV.errorCodeMap(error.code)), MV.warnIcon);
                        }
                    } catch (e) {
                        Y.log("Could not parse the JSON response to get the keys", "error");
                        Y.log("Response received: [0]".format(responseObject.resposeText), "error");
                        MV.showAlertDialog("Cannot parse Response to get keys!", MV.warnIcon);
                    }
                };
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
            var fitToContent = function (maxHeight, text) {
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
                };
            var loadAndSubscribe = function (treeble) {
                    treeble.load();
                    treeble.subscribe("rowMouseoverEvent", treeble.onEventHighlightRow);
                    treeble.subscribe("rowMouseoutEvent", treeble.onEventUnhighlightRow);
                };
            var showDocuments = function (request, responseObject) {
                    Y.log("Preparing to write on JSON tab", "info");
                    writeOnJSONTab(responseObject.results);
                    Y.log("Preparing the treeTable data", "info");
                    var treebleData = MV.getTreebleDataforDocs(responseObject);
                    treeble = MV.getTreeble(treebleData);
                    loadAndSubscribe(treeble);
                    Y.log("Tree table view loaded", "info");
                };
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
            var getButtonIndex = function (targetNode) {
                    var btnID = targetNode.get("id");
                    var match = btnID.match(/\d+/);
                    return (parseInt(match[0], 10));
                };
            var toggleSaveEdit = function (targetNode, index, action) {
                    var textArea = Y.one('#doc' + index).one("pre").one("textarea");
                    var antiAction;
                    if (action === actionMap.save) {
                        antiAction = actionMap.edit;
                        textArea.addClass('disabled');
                        textArea.setAttribute("disabled", "disabled");
                        Y.on("click", editDoc, "#edit" + index);
                    } else {
                        antiAction = actionMap.save;
                        textArea.removeAttribute("disabled");
                        textArea.removeClass('disabled');
                        Y.on("click", saveDoc, "#save" + index);
                    }
                    targetNode.set("innerHTML", antiAction);
                    targetNode.removeClass(action + 'btn');
                    targetNode.addClass(antiAction + 'btn');
                    targetNode.set("id", antiAction + index);
                };
            var parseUpdateDocResponse = function (ioId, responseObject) {
                    var parsedResponse = Y.JSON.parse(responseObject.responseText);
                    response = parsedResponse.response.result;
                    if (response !== undefined) {
                        MV.showAlertDialog("Document updated", MV.infoIcon);
                        Y.log("Document update to [0]".format(response), "info");
                        Y.one("#" + Y.one("#currentColl").get("value")).simulate("click");
                    } else {
                        var error = parsedResponse.response.error;
                        MV.showAlertDialog("Could not update Document! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon, function () {
                            Y.one("#" + Y.one("#currentColl").get("value")).simulate("click");
                        });
                        Y.log("Could not update Document! [0]".format(MV.errorCodeMap[error.code]), "error");
                    }
                };
            var sendUpdateDocRequest = function (doc, id) {
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
                };
            var allKeysSelected = function () {
                    var fields = Y.all('#fields input');
                    var index;
                    for (index = 0; index < fields.size(); index++) {
                        var item = fields.item(index);
                        if (!item.get("checked")) {
                            return false;
                        }
                    }
                    return true;
                };
            var selectAllKeys = function () {
                    var fields = Y.all('#fields input');
                    var index;
                    for (index = 0; index < fields.size(); index++) {
                        var item = fields.item(index);
                        item.set("checked", "true");
                    }
                    executeQuery();
                    this.hide();
                };
            var deleteDoc = function (eventObject) {
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
                                            Y.one("#" + Y.one("#currentColl").get("value")).simulate("click");
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
                    MV.showYesNoDialog("Do you really want to drop the document ?", sendDeleteDocRequest, function () {
                        this.hide();
                    });
                };
            var saveDoc = function (eventObject) {
                    var parsedDoc;
                    var targetNode = eventObject.currentTarget;
                    var index = getButtonIndex(targetNode);
                    toggleSaveEdit(targetNode, index, actionMap.save);
                    var doc = Y.one('#doc' + index).one("pre").one("textarea").get("value");
                    doc = doc.replace(/'/g, '"');
                    try {
                        parsedDoc = Y.JSON.parse(doc);
                    } catch (e) {
                        MV.showAlertDialog("The document entered is not in the correct JSON format");
                    }
                    sendUpdateDocRequest(Y.JSON.stringify(parsedDoc), idMap.index);
                };
            var editDoc = function (eventObject) {
                    if (!allKeysSelected()) {
                        MV.showYesNoDialog("To edit a document you need check all keys in query box. Click YES to do so, NO to cancel", selectAllKeys, function () {
                            this.hide();
                        });
                    } else {
                        var targetNode = eventObject.currentTarget;
                        var index = getButtonIndex(targetNode);
                        toggleSaveEdit(targetNode, index, actionMap.edit);
                        var doc = Y.one('#doc' + index).one("pre").one("textarea").get("value");
                        parsedDoc = Y.JSON.parse(doc);
                        idMap.index = parsedDoc._id;
                    }
                };
            var writeOnJSONTab = function (response) {
                    var documents = "<table>",
                        i;
                    for (i = 0; i < response.length; i++) {
                        documents = documents + "<tr><td id='doc" + i + "'><pre><textarea id='ta" + i + "' class='disabled' disabled='disabled' cols='80' >" + Y.JSON.stringify(response[i], null, 4) + "</textarea></pre></td>" + "<td><button id='edit" + i + "' class='btn editbtn'>Edit</button></td>" + "<td><button id='delete" + i + "' class='btn deletebtn'>delete</button></td></tr>";
                    }
                    if (i === 0) {
                        documents = documents + "No documents to be displayed";
                    }
                    documents = documents + "</table>";
                    tabView.getTab(0).setAttributes({
                        content: documents
                    }, false);
                    for (i = 0; i < response.length; i++) {
                        Y.on("click", editDoc, "#edit" + i);
                        Y.on("click", deleteDoc, "#delete" + i);
                    }
                    for (i = 0; i < response.length; i++) {
                        fitToContent(500, document.getElementById("ta" + i));
                    }
                    Y.log("The documents written on the JSON tab", "info");
                };
            // Cell editing
/*            var editors = {
                string: new YAHOO.widget.TextboxCellEditor(),
                number: new YAHOO.widget.TextboxCellEditor({
                    validator: function (
                    val) {
                        val = parseFloat(val);
                        if (YAHOO.lang.isNumber(val)) {
                            return val;
                        }
                    }
                }),
                Date: new YAHOO.widget.DateCellEditor()
            };
           
                  var saveValue = function (oArgs) {
                    alert("saved");
                    var rec = oArgs.editor.getRecord();
                    var ds = rec.getData('_yui_node_ds');
                    var path = rec.getData('_yui_node_path');
                    alert("rec:[0]".format(rec));
                    alert("path:[0]".format(path));
                };
          treeble.subscribe("cellClickEvent", function (oArgs) {
                var target = oArgs.target;
                var record = this.getRecord(target);
                var column = this.getColumn(target);
                //                this.showCellEditor(target);
                if (column.key === 'value') {
                    //                    var type = record.getData('type');
                    //                    column.editor = editors[type];
                    //                    this.showCellEditor(target);
                    this.showCellEditor(target);
                }
            });
            treeble.subscribe("cellMouseoverEvent", function (oArgs) {
                var target = oArgs.target;
                var column = this.getColumn(target);
                var record = this.getRecord(target);
                var type = record.getData('type');
                if (column.key === 'value' && type !== "ObjectID") {
                    this.highlightCell(target);
                }
            });
            treeble.subscribe("editorSaveEvent", saveValue);
            treeble.subscribe("cellMouseoutEvent", MV.treeTable.onEventUnhighlightCell);*/
            MV.header.set("innerHTML", "Contents of " + Y.one("#currentColl").get("value"));
            tabView.appendTo(MV.mainBody.get('id'));
        };
    Y.delegate("click", showTabView, "#collNames", "li");
});