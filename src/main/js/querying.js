/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd.
 * Hyderabad, India
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following condition
 * is met:
 *
 *     + Neither the name of Imaginea, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
YUI({
    filter: 'raw'
}).use("alert-dialog", "io-base", "json-parse", "node", "event-delegate", "stylize", "json-stringify", "utility", function(Y) {
    Y.namespace('com.imaginea.mongoV');
    var MV = Y.com.imaginea.mongoV;
    MV.treebleData = {};
    var showTabView = function(e) {
        MV.toggleClass(e.currentTarget, Y.all("#collNames li"));
        MV.currentColl = e.currentTarget.get("id");
        var collName = e.currentTarget.get("id");
        MV.mainBody.empty(true);

        var showDocuments = function(request, responseObject) {
            Y.log("Preparing to write on JSON tab", "info");
            writeOnJSONTab(responseObject.results);
            Y.log("Preparing the treeTable data", "info");
            prepareTreebleData(responseObject);
            makeTreeble();
        };

        var sendDocumentRequest = function(param) {
            MV.data.sendRequest(param, {
                success: showDocuments,
                failure: function(request, responseObject) {
                    MV.showAlertDialog("Failed: Documents could not be loaded", MV.warnIcon);
                    Y.log("Documents could not be loaded. Response: [0]".format(responseObject.responseText), "error");
                },
                scope: tabView
            });
        };

        var executeQuery = function(e) {
            var parsedQuery, query = Y.one('#queryBox').get("value"),
                limit = Y.one('#limit').get("value"),
                skip = Y.one('#skip').get("value"),
                fields = Y.all('#fields input'),
                index = 0;
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
                sendDocumentRequest("startIndex=0&results=100&limit=[0]&skip=[1]&fields=[2]&query=[3]&queryExec=true".format(limit, skip, checkedFields, query));
            } catch (error) {
                Y.log("Could not Execute query. Reason: [0]".format(error), "error");
                 MV.showAlertDialog("Failed:Could Not execute Query. [0]".format(error), MV.warnIcon);
            }
        };

        var showQueryBox = function(ioId, responseObject) {
            Y.log("Preparing to show QueryBox", "info");
            if (responseObject.responseText !== undefined) {
                var queryForm = Y.one('#queryForm');
                queryForm.addClass('form-cont');
                var keys, index, form = "";
                try {
                    Y.log("Parsing the JSON response to get the keys", "info");
                    keys = Y.JSON.parse(responseObject.responseText);
                } catch (e) {
                    Y.log("Could not parse the JSON response to get the keys", "error");
                    Y.log("Response received: [0]".format(responseObject.resposeText), "error");
                    MV.showAlertDialog("Cannot parse Response to get keys!", MV.warnIcon);
                }
                queryForm.set("innerHTML", MV.getForm(keys));
                Y.log("QueryBox loaded", "info");
                Y.on("click", executeQuery, "#execQueryButton");
            } else {
                Y.log("JSON response to get the keys is undefined", "error");
                Y.log("Response status: [0]".format(responseObjet.responseStatus), "error");
                MV.showAlertDialog("Unexpected Error: Could not load the query Box", MV.warnIcon);
            }
        };

        //TODO IS it necessary to send request. Can this be done on client side?
        var getKeyRequest = Y.io(MV.URLMap.collectionKeys(),
        // configuration for loading the queryBox
        {
            method: "GET",
            on: {
                success: showQueryBox,
                failure: function(ioId, responseObject) {
                    MV.showAlertDialog("Unexpected Error: Could not load the query Box", MV.warnIcon);
                    Y.log("Could not send the request to get the keys in the collection.", "error");
                }
            }
        });
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

        MV.data = new YAHOO.util.XHRDataSource(MV.URLMap.getCollections(), {
            responseType: YAHOO.util.XHRDataSource.TYPE_JSON,
            responseSchema: {
                resultsList: "results",
                metaFields: {
                    startIndex: 'first_index',
                    recordsReturned: 'records_returned',
                    totalRecords: 'total_records'
                }
            }
        });
        var saveHandle;
        var saveDoc = function(e) {
            var targetNode = e.currentTarget;
            var saveBtnID = targetNode.get("id");
            targetNode.set("innerHTML", "Edit");
            targetNode.removeClass('savebtn');
            targetNode.addClass('editbtn');
            var match = saveBtnID.match(/\d+/);
            var index = parseInt(match[0], 10);
            targetNode.set("id", "edit" + index);
            var doc = Y.one('#doc' + index).one("pre").one("textarea");
            inner = doc.get("value");
            doc.addClass('disabled');
            //            var parentNode = doc.get("parentNode");
            //            parentNode.empty(true);
            //            parentNode.append(inner);
            doc.setAttribute("disabled", "disabled");
            //            doc.append(inner);
            //     Y.detach(saveHandle);
            //            Y.on("click", editDoc, "button .editbtn");
        };

        var editHandle;

        var editDoc = function(e) {
            Y.log(e);
            var targetNode = e.currentTarget;
            var editBtnID = targetNode.get("id");
            targetNode.set("innerHTML", "save");
            var match = editBtnID.match(/\d+/);
            var index = parseInt(match[0], 10);
            targetNode.removeClass('editbtn');
            targetNode.addClass('savebtn');
            targetNode.set("id", "save" + index);
            var doc = Y.one('#doc' + index).one("pre").one("textarea");
            doc.removeAttribute("disabled");
            doc.removeClass('disabled');
            //            doc.append(inner);
            //            doc.append("<textarea style='width:"+width+"px; height:"+height+"px;'>" + inner + "</textarea>");
            //     Y.detach(editHandle);
            saveHandle = Y.on("click", saveDoc, ".savebtn");
        };

        var writeOnJSONTab = function(response) {
            var data = "<table>",
                i;
            for (i = 0; i < response.length; i++) {
                data = data + "<tr><td id='doc" + i + "'><pre><textarea class='disabled' rows='10' cols='80' disabled='disabled'>" + Y.JSON.stringify(response[i], null, 4) + "</textarea></pre></td>" + "<td><button id='edit" + i + "' class='btn editbtn'>Edit</button></td></tr>";
            }
            if (i === 0) {
                data = data + "NO DATA TO BE DISPLAYED";
            }
            data = data + "</table>";
            tabView.getTab(0).setAttributes({
                content: data
            }, false);
            Y.log("The documents written on the JSON tab", "info");
            editHandle = Y.on("click", editDoc, ".editbtn");
        };

        //TODO: IS this function redundant

        function newObject() {
            return {};
        }
        var getChildrenArray;
        getChildrenArray = function(doc) {
            var i, tempObject, key, value, newArray, childrenArray = [];
            var allKeys = MV.getProperties(doc);
            for (i = 0; i < allKeys.length; i++) {
                tempObject = newObject();
                key = allKeys[i];
                tempObject.key = key;
                value = doc[key];
                if (MV.isArray(value)) {
                    newArray = [];
                    tempObject.type = "BasicDBList";
                    newArray = getChildrenArray(value);
                    tempObject.kiddies = newArray;
                } else if (MV.isObject(value)) {
                    newArray = [];
                    tempObject.type = "BasicDBObject";
                    newArray = getChildrenArray(value);
                    tempObject.kiddies = newArray;
                } else {
                    tempObject.value = value;
                    if (key === "_id") {
                        tempObject.type = "ObjectID";
                    }
                    else {
                        tempObject.type = typeof value;
                    }
                }
                childrenArray.push(tempObject);
            }
            return childrenArray;
        };
        var prepareTreebleData = function(response) {
            var allDocs = response.results,
                aDoc, docCopy, parentNode, childrenArray, result = [],
                finalObject, resultObject;
            var i;
            for (i = 0; i < allDocs.length; i++) {
                aDoc = allDocs[i];
                parentNode = newObject();
                childrenArray = [];
                parentNode.key = "Document [0]".format(i + 1);
                //                parentNode.value = aDoc._id;
                //                parentNode.type = "ObjectID";
                childrenArray = getChildrenArray(aDoc);
                parentNode.kiddies = childrenArray;
                result.push(parentNode);
            }
            resultObject = newObject();
            finalObject = newObject();
            resultObject.results = result;
            finalObject.response = resultObject;
            finalObject.total_records = response.meta.totalRecords;
            finalObject.records_returned = response.meta.recordsReturned;
            finalObject.first_index = response.meta.startIndex;
            MV.treebleData = finalObject;
            Y.log("Tree table data prepared", "info");
        };
        sendDocumentRequest("startIndex=0&results=5");
        var makeTreeble = function() {
            var Dom = YAHOO.util.Dom,
                Event = YAHOO.util.Event,
                DT = YAHOO.widget.DataTable;

            function localGenerateRequest(state, path) {
                return state;
            }
            MV.treeTable = new YAHOO.widget.DataTable(
            // Root element id
            "table",
            // Column configuration
            [{
                key: "toggle_column",
                label: "",
                formatter: function(
                elCell, oRecord, oColumn, oData) {
                    Dom.addClass(
                    elCell.parentNode, 'treeble-nub');
                    if (oRecord.getData('kiddies')) {
                        var path = oRecord.getData('_yui_node_path');
                        var open = this.rowIsOpen(path);
                        var clazz = open ? 'row-open' : 'row-closed';
                        Dom.addClass(
                        elCell, 'row-toggle');
                        Dom.replaceClass(
                        elCell, /row-(open|closed)/, clazz);
                        elCell.innerHTML = '<a class="treeble-collapse-nub" href="javascript:void(0);"></a>';
                        Event.on(
                        elCell, 'click', function(
                        e, path) {
                            this.toggleRow(path);
                        }, path, this);
                    }
                }},
            {
                key: "key",
                label: "Key",
                width: MV.mainBody.get('scrollWidth') / 3 - 38,
                formatter: function(
                elCell, oRecord, oColumn, oData) {
                    elCell.innerHTML = '<span style="font-weight: bolder;padding-left:' + oRecord.getData('_yui_node_depth') * 15 + 'px;">' + oData + '</span>';
                }},
            {
                key: "value",
                label: "Value",
                width: MV.mainBody.get('scrollWidth') / 3 - 38,
                //     edit: true,
                editor: new YAHOO.widget.TextboxCellEditor()},
            {
                key: "type",
                label: "Type",
                width: MV.mainBody.get('scrollWidth') / 3 - 38}],
            // Data Source
            new YAHOO.util.TreebleDataSource(new YAHOO.util.DataSource(MV.treebleData.response.results, {
                responseType: YAHOO.util.DataSource.TYPE_JSARRAY,
                responseSchema: {
                    fields: [
                        "key", "value", "type",
                    {
                        key: 'kiddies',
                        parser: 'datasource'}
                    ]
                },
                treebleConfig: {
                    generateRequest: localGenerateRequest,
                    totalRecordsReturnExpr: '.meta.totalRecords'
                }
            }), {
                paginateChildren: false
            }),
            // Attribute Config
            {
                paginator: new YAHOO.widget.Paginator({
                    rowsPerPage: 5,
                    // totalRecords:oResponse.meta.totalRecords,
                    rowsPerPageOptions: [
                        1, 2, 5, 10, 25],
                    containers: 'table-pagination',
                    template: '{FirstPageLink}{PreviousPageLink}{PageLinks}{NextPageLink}{LastPageLink}{RowsPerPageDropdown}',
                    pageLinks: 5
                }),
                initialLoad: false,
                initialRequest: {
                    startIndex: 0,
                    results: 5
                },
                dynamicData: true,
                displayAllRecords: true,
                generateRequest: DT.generateTreebleDataSourceRequest
            });
            MV.treeTable.handleDataReturnPayload = function(
            oRequest, oResponse, oPayload) {
                oPayload.totalRecords = oResponse.meta.totalRecords;
                return oPayload;
            };
            // Cell editing
            var editors = {
                string: new YAHOO.widget.TextboxCellEditor(),
                number: new YAHOO.widget.TextboxCellEditor({
                    validator: function(
                    val) {
                        val = parseFloat(val);
                        if (YAHOO.lang.isNumber(val)) {
                            return val;
                        }
                    }
                }),
                Date: new YAHOO.widget.DateCellEditor()
            };

            MV.treeTable.load();
            Y.log("Tree table view loaded", "info");
            var saveValue = function(oArgs) {
                alert("saved");
                var rec = oArgs.editor.getRecord();
                var ds = rec.getData('_yui_node_ds');
                var path = rec.getData('_yui_node_path');
                alert("rec:[0]".format(rec));
                alert("path:[0]".format(path));
            };

            MV.treeTable.subscribe("cellClickEvent", function(oArgs) {
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
            MV.treeTable.subscribe("cellMouseoverEvent", function(oArgs) {
                var target = oArgs.target;
                var column = this.getColumn(target);
                var record = this.getRecord(target);
                var type = record.getData('type');
                if (column.key === 'value' && type !== "ObjectID") {
                    this.highlightCell(target);
                }
            });


            MV.treeTable.subscribe("editorSaveEvent", saveValue);
            MV.treeTable.subscribe("cellMouseoutEvent", MV.treeTable.onEventUnhighlightCell);

        };
        MV.header.set("innerHTML", "Contents of " + MV.currentColl);
        tabView.appendTo(MV.mainBody.get('id'));
    };
    Y.delegate("click", showTabView, "#collNames", "li");

});