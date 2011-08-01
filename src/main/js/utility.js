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
YUI.add('utility', function (Y) {
    Y.namespace('com.imaginea.mongoV');
    var MV = Y.com.imaginea.mongoV;
    // Check if String.prototype.format already exists because in future versions
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
            if (!str || typeof str !== 'string') {
                return null;
            }
            return str.replace(/^[\s]+/, '').replace(/[\s]+$/, '').replace(/[\s]{2,}/, ' ');
        };
    }
    //TODO: IS this function redundant

    function newObject() {
        return {};
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
    var getChildrenArray;
    getChildrenArray = function (doc) {
        var i, tempObject, key, value, newArray, childrenArray = [];
        var allKeys = MV.getProperties(doc);
        for (i = 0; i < allKeys.length; i++) {
            tempObject = newObject();
            key = allKeys[i];
            tempObject.key = key;
            value = doc[key];
            if (MV.isArray(value)) {
                newArray = [];
                //                    tempObject.type = "BasicDBList";
                newArray = getChildrenArray(value);
                tempObject.kiddies = newArray;
            } else if (MV.isObject(value)) {
                newArray = [];
                //                    tempObject.type = "BasicDBObject";
                newArray = getChildrenArray(value);
                tempObject.kiddies = newArray;
            } else {
                tempObject.value = value;
            }
            childrenArray.push(tempObject);
        }
        return childrenArray;
    };
    var prepareReturnObject = function (response, result) {
            var returnObject = newObject();
            var resultObject = newObject();
            resultObject.results = result;
            returnObject.response = resultObject;
            returnObject.total_records = response.meta.totalRecords;
            returnObject.records_returned = response.meta.recordsReturned;
            returnObject.first_index = response.meta.startIndex;
            Y.log("Tree table data prepared", "info");
            return (returnObject);
        };
    MV.getTreebleDataforDocs = function (response) {
        var allDocs = response.results,
            aDoc, parentNode, childrenArray, result = [];
        var i;
        for (i = 0; i < allDocs.length; i++) {
            aDoc = allDocs[i];
            parentNode = newObject();
            childrenArray = [];
            parentNode.key = "Document [0]".format(i + 1);
            childrenArray = getChildrenArray(aDoc);
            parentNode.kiddies = childrenArray;
            result.push(parentNode);
        }
        return (prepareReturnObject(response, result));
    };
    MV.getTreebleDataForServerStats = function (response) {
        var data = response.results[0];
        var aDoc, docCopy, parentNode, childrenArray, result = [],
            finalObject, resultObject;
        var i;
        var allKeys = MV.getProperties(data);
        for (i = 0; i < allKeys.length; i++) {
            parentNode = newObject();
            parentNode.key = allKeys[i];
            var value = data[allKeys[i]];
            if (MV.isObject(value)) {
                newArray = [];
                newArray = getChildrenArray(value);
                parentNode.kiddies = newArray;
            } else {
                parentNode.value = value;
            }
            result.push(parentNode);
        }
        return (prepareReturnObject(response, result));
    };
    MV.getTreeble = function (dataSource) {
        var Dom = YAHOO.util.Dom,
            Event = YAHOO.util.Event,
            DT = YAHOO.widget.DataTable;

        function localGenerateRequest(state, path) {
            return state;
        }
        var treeTable = new YAHOO.widget.DataTable(
        // Root element id
        "table",
        // Column configuration
        [{
            key: "toggle_column",
            label: "",
            formatter: function (
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
                    elCell, 'click', function (
                    e, path) {
                        this.toggleRow(path);
                    }, path, this);
                }
            }
        }, {
            key: "key",
            label: "Key",
            width: MV.mainBody.get('scrollWidth') / 2 - 48,
            formatter: function (
            elCell, oRecord, oColumn, oData) {
                elCell.innerHTML = '<span style="font-weight: bolder;padding-left:' + oRecord.getData('_yui_node_depth') * 15 + 'px;">' + oData + '</span>';
            }
        }, {
            key: "value",
            label: "Value",
            width: MV.mainBody.get('scrollWidth') / 2 - 48,
            editor: new YAHOO.widget.TextboxCellEditor()
        }
/*,
        {
            key: "type",
            label: "Type",
            width: MV.mainBody.get('scrollWidth') / 3 - 38}*/
        ],
        // Data Source
        new YAHOO.util.TreebleDataSource(new YAHOO.util.DataSource(dataSource.response.results, {
            responseType: YAHOO.util.DataSource.TYPE_JSARRAY,
            responseSchema: {
                fields: ["key", "value", // "type",
                {
                    key: 'kiddies',
                    parser: 'datasource'
                }]
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
                rowsPerPage: 25,
                rowsPerPageOptions: [1, 2, 5, 10, 25, 50],
                containers: 'table-pagination',
                template: '{FirstPageLink}{PreviousPageLink}{PageLinks}{NextPageLink}{LastPageLink}{RowsPerPageDropdown}'
            }),
            initialLoad: false,
            initialRequest: {
                startIndex: 0,
                results: 25
            },
            dynamicData: true,
            displayAllRecords: true,
            generateRequest: DT.generateTreebleDataSourceRequest
        });
        treeTable.handleDataReturnPayload = function (oRequest, oResponse, oPayload) {
            oPayload.totalRecords = oResponse.meta.totalRecords;
            return oPayload;
        };
        return treeTable;
    };
    var formUpperPart = "",
        formLowerPart = "";
    formUpperPart += "<textarea id='queryBox' name='queryBox' rows='3' cols='60' >";
    formUpperPart += "{}";
    formUpperPart += "</textarea>";
    formUpperPart += "<label for='fields' ></label><ul id='fields' class='checklist'>";
    formLowerPart += "</ul><br>";
    formLowerPart += "<label for='limit'> Limit: </label><input id='limit' type='text' name='limit' value='0' size='5' />";
    formLowerPart += "<label for='skip'> Skip: </label><input id='skip' type='text' name='skip' value='0' size='5' />";
    formLowerPart += "<button id='execQueryButton' class='btn'>Execute Query</button>";
    MV.getForm = function (data) {
        var checkList = "";
        for (index = 0; index < data.length; index++) {
            checkList += "<li><label for='" + data[index] + "'><input id='" + data[index] + "' name='" + data[index] + "' type='checkbox' checked=true />" + data[index] + "</label></li>";
        }
        return formUpperPart + checkList + formLowerPart;
    };
    MV.hideQueryForm = function () {
        var queryForm = Y.one('#queryForm');
        queryForm.removeClass('form-cont');
        queryForm.set("innerHTML", "");
    };
    MV.mainBody = Y.one("#mainBody");
    MV.header = Y.one("#mainBodyHeader");
    MV.warnIcon = YAHOO.widget.SimpleDialog.ICON_WARN;
    MV.infoIcon = YAHOO.widget.SimpleDialog.ICON_INFO;
    MV.URLMap = {
        insertColl: function () {
            return "services/" + Y.one("#currentDB").get("value") + "/collection/" + Y.one("#newName").get("value") + "?tokenId=" + Y.one("#tokenID").get("value") + "&action=PUT";
        },
        insertDB: function () {
            return "services/db/" + Y.one("#newName").get("value") + "?tokenId=" + Y.one("#tokenID").get("value") + "&action=DELETE";
        },
        dropColl: function () {
            return "services/" + Y.one("#currentDB").get("value") + "/collection/" + Y.one("#currentColl").get("value") + "?tokenId=" + Y.one("#tokenID").get("value") + "&action=DELETE";
        },
        getColl: function () {
            return "services/" + Y.one("#currentDB").get("value") + "/collection?tokenId=" + Y.one("#tokenID").get("value");
        },
        collStatistics: function () {
            return "services/stats/db/" + Y.one("#currentDB").get("value") + "/collection/" + Y.one("#currentColl").get("value") + "?tokenId=" + Y.one("#tokenID").get("value");
        },
        dbStatistics: function () {
            return "services/stats/db/" + Y.one("#currentDB").get("value") + "?tokenId=" + Y.one("#tokenID").get("value");
        },
        dropDB: function () {
            return "services/db/" + Y.one("#currentDB").get("value") + "?tokenId=" + Y.one("#tokenID").get("value") + "&action=DELETE";
        },
        getDBs: function () {
            return "services/db?tokenId=" + Y.one("#tokenID").get("value");
        },
        documentKeys: function () {
            return "services/" + Y.one("#currentDB").get("value") + "/" + Y.one("#currentColl").get("value") + "/document/keys?tokenId=" + Y.one("#tokenID").get("value");
        },
        getDocs: function () {
            return "services/" + Y.one("#currentDB").get("value") + "/" + Y.one("#currentColl").get("value") + "/document?tokenId=" + Y.one("#tokenID").get("value");
        },
        serverStatistics: function () {
            return "services/stats?tokenId=" + Y.one("#tokenID").get("value");
        },
        login: function () {
            return "services/login";
        },
        logout: function () {
            return "services/logout?tokenId=" + Y.one("#tokenID").get("value");
        },
        graphs: function () {
            return "graphs.html?tokenId=" + Y.one("#tokenID").get("value");
        },
        graphInitiate: function () {
            return "graphs/initiate?tokenId=" + Y.one("#tokenID").get("value");
        },
        graphQuery: function () {
            return "graphs/query?tokenId=" + Y.one("#tokenID").get("value");
        },
        insertDoc: function () {
            return "services/" + Y.one("#currentDB").get("value") + "/" + Y.one("#currentColl").get("value") + "/document?tokenId=" + Y.one("#tokenID").get("value") + "&action=PUT";
        },
        updateDoc: function () {
            return "services/" + Y.one("#currentDB").get("value") + "/" + Y.one("#currentColl").get("value") + "/document?tokenId=" + Y.one("#tokenID").get("value");
        },
        deleteDoc: function () {
            return "services/" + Y.one("#currentDB").get("value") + "/" + Y.one("#currentColl").get("value") + "/document?tokenId=" + Y.one("#tokenID").get("value") + "&action=DELETE";
        },
        help: function () {
            return "help.html";
        },
        troubleShootPage: function () {
            return "troubleshoot.html";
        },
        troubleShoot: function () {
            return "admin";
        },
    };
    MV.errorCodeMap = {
        "HOST_UNKNOWN": "Please check if Mongod is running on the given host and port !",
        "MISSING_LOGIN_FIELDS": "Please fill in all the login fields !",
        "ERROR_PARSING_PORT": "You have entered an invalid port number !",
        "PORT_OUT_OF_RANGE": "You have entered an invalid port number !",
        "INVALID_USERNAME": "You have entered an invalid username and password combination ! To access you need to add user in admin database of mongodb OR leave both the fields empty.",
        "INVALID_SESSION": "Your session is corrupted or timed out! Please login again from the login page.",
        "GET_DB_LIST_EXCEPTION": "Could not load the DB list! Please check if mongo is still running and then refresh the page.",
        "GET_COLLECTION_LIST_EXCEPTION": "Please check if mongod is still running and then refresh the page.",
        "DB_DELETION_EXCEPTION": "Please check if mongo is running and then refresh the page and try again.",
        "DB_DOES_NOT_EXISTS": "The db you are trying to delete does not exist! Refresh the page.",
        "DB_NAME_EMPTY": "Received an empty name for the database which is invalid",
        "DB_ALREADY_EXISTS": "A database with the given name already exist! Try another name.",
        "COLLECTION_ALREADY_EXISTS": "A collection with the given name already exists! Try another name.",
        "COLLECTION_DOES_NOT_EXIST": "The collection you are trying to delete does not exist! Refresh the page.",
        "COLLECTION_NAME_EMPTY": "Recieved an empty collection name",
        "GET_DOCUMENT_LIST_EXCEPTION": "Please check if mongod is running and refresh the page.",
        "DOCUMENT_DELETION_EXCEPTION": " Please check if mongod is running and refresh the page.",
        "DOCUMENT_CREATION_EXCEPTION": "Please check if mongod is running and refresh the page.",
        "DOCUMENT_UPDATE_EXCEPTION": "Please check if mongod is running and refresh the page.",
        "INVALID_USER": "Your session is corrupted or timed out! Please login again from the login page.",
        "TOKEN_ID_ABSENT": "Your session is corrupted or timed out! Please login again from the login page.",
        "GET_DB_STATS_EXCEPTION": "Please check if mongod is running and refresh the page.",
        "GET_COLL_STATS_EXCEPTION": "Please check if mongod is running and refresh the page.",
        "COLLECTION_CREATION_EXCEPTION": "Please check if mongod is running and refresh the page.",
        "COLLECTION_DELETION_EXCEPTION": "Please check if mongod is running and refresh the page.",
        "INVALID_OBJECT_ID": "You cannot update the _id key",
        "ANY_OTHER_EXCEPTION": "An unknown exception occured. Please try to login again.",
        "ERROR_INITIATING_GRAPH": "Could not initiate the graph. Please check if mongod is running"
    };
}, '3.3.0', {
    requires: ["node"]
});