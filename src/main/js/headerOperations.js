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
}).use("alert-dialog", "utility", "treeble-paginator", "node", "event-delegate", "stylize", "custom-datatable", function (Y) {
    YUI.namespace('com.imaginea.mongoV');
    var MV = YUI.com.imaginea.mongoV;

    function handleHeaderOption(eventObject) {
        var menuOpt = eventObject.currentTarget.get("id");
        MV.toggleClass(eventObject.currentTarget, Y.all(".nav-cont li a"));
        _clearContents(MV.mainBody);
        _clearContents(Y.one('#queryForm'));
        if (menuOpt === "home") {
            window.location = "home.html?dbInfo=" + Y.one("#host").get("value")+"_" + Y.one("#port").get("value") + "_" + Y.one("#username").get("value");
        } else if (menuOpt === "serverStats") {
            showServerStats();
            MV.header.set("innerHTML", "Server Statistics");
        } else if (menuOpt === 'graphs') {
            _clearContents(MV.header);
            window.open(MV.URLMap.graphs(), '_newtab');
        } else if (menuOpt === 'help') {
            _clearContents(MV.header);
            window.open(MV.URLMap.help(), '_newtab');
        } else if (menuOpt === 'troubleshoot') {
            _clearContents(MV.header);
            window.open(MV.URLMap.troubleShootPage(), '_newtab');
        }
    }

    function showServerStats() {
        var data = new YAHOO.util.XHRDataSource(MV.URLMap.serverStatistics(), {
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
        data.sendRequest("", {
            success: function (request, responseObject) {
                MV.mainBody.set("innerHTML", '<div id="table"></div><div id="table-pagination"></div>');
                var treebleData = MV.getTreebleDataForServerStats(responseObject);
	            treeble = MV.getTreeble(treebleData);
	            // Remove download & delete columns for server statistics
	            treeble.removeColumn(treeble.getColumn("download_column"));
	            treeble.removeColumn(treeble.getColumn("delete_column"));
                treeble.load();
            },
            failure: function (request, responseObject) {
                MV.showAlertMessage("Failed: ServerStats could not be loaded! Please check if the app server is still running.", MV.warnIcon);
                Y.log("Server Statistics could not be loaded. Response Status: [0]".format(responseObject.statusText), "error");
            }
        });
    }

    function logout(eventObject) {
        Y.io(MV.URLMap.logout(), {
            method: "GET",
            on: {
                success: function (ioId, responseObject) {
                    parsedResponse = Y.JSON.parse(responseObject.responseText);
                    var response = parsedResponse.response.result;
                    if (response !== undefined) {
                        Y.log("Successfully logging out.", "info");
                        window.location = "loggedOut.html";
                    } else {
                        var error = parsedResponse.response.error;
                        MV.showAlertMessage("Cannot logout! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
                        Y.log("Could not logout. Message: [0], Code: [1]".format(error.message, error.code), "error");
                    }
                },
                failure: function (ioId, responseObject) {
                    MV.showAlertMessage("Could not send request. Check if app server is running. Response message: [0]".format(responseObject.statusText), MV.warnIcon);
                    Y.log("Could not send request to logout. Status text: [0] ".format(responseObject.statusText), "error");
                }
            }
        });
    }

    // TODO replace with a better equivalent
    function _clearContents(element) {
        element.set("innerHTML","");
    }

    Y.delegate("click", handleHeaderOption, ".nav-cont", "li a");
    Y.on("click", logout, "#logout");
});