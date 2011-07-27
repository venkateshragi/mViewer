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
}).use("alert-dialog", "utility", "node", "event-delegate", "stylize", "custom-datatable", function (Y) {
    Y.namespace('com.imaginea.mongoV');
    var MV = Y.com.imaginea.mongoV;
    var showServerStats = function () {
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
                    treeble.load();
                },
                failure: function (request, responseObject) {
                    MV.showAlertDialog("Failed: ServerStats could not be loaded! Please check if the app server is still running.", MV.warnIcon);
                    Y.log("Server Statistics could not be loaded. Response Status: [0]".format(responseObject.statusText), "error");
                },
            });
        };
    var executeHeaderOption = function (eventObject) {
            menuOpt = eventObject.currentTarget.get("id");
            MV.toggleClass(eventObject.currentTarget, Y.all(".nav-cont li a"));
            // Clearing the content of main Body
            MV.mainBody.set("innerHTML", "");
            Y.one('#queryForm').set("innerHTML", "");
            if (menuOpt === "home") {
                window.location = "home.html?tokenID=" + Y.one("#tokenID").get("value") + "&username=" + Y.one("#username").get("value") + "&host=" + Y.one("#host").get("value");
            } else if (menuOpt === "serverStats") {
                showServerStats();
                MV.header.set("innerHTML","Server Statistics");
            } else if (menuOpt === 'graphs') {
                MV.header.set("innerHTML", "");
                window.open(MV.URLMap.graphs(), '_newtab');
            }
        };
    var logout = function (eventObject) {
            var request = Y.io(MV.URLMap.logout(), {
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
                            MV.showAlertDialog("Cannot logout! [0]".format(MV.errorCodeMap[error.code]), MV.warnIcon);
                            Y.log("Could not logout. Message: [0], Code: [1]".format(error.message, error.code), "error");
                        }
                    },
                    failure: function (ioId, responseObject) {
                        MV.showAlertDialog("Could not send request. Check if app server is running. Response message: [0]".format(responseObject.statusText), MV.warnIcon);
                        Y.log("Could not send request to logout. Status text: [0] ".format(responseObject.statusText), "error");
                    }
                }
            });
        };
    Y.delegate("click", executeHeaderOption, ".nav-cont", "li a");
    Y.on("click", logout, "#logout");
});