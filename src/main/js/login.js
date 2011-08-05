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
}).use("io", "json", "node", "utility", function (Y) {
    var MV = Y.com.imaginea.mongoV;
    var tryLogin = function (e) {
            var username = Y.one("#username").get("value").trim();
            var password = Y.one("#password").get("value").trim();
            var port = Y.one("#port").get("value").trim();
            var host = Y.one("#host").get("value").trim();
            var resetBGColor = function () {
                    Y.all("input").setStyle("background", "#FFFFFF");
                };
            var errorHandlerMap = {
                "HOST_UNKNOWN": function () {
                    resetBGColor();
                    Y.one("#port").setStyle("background", "#FFEBE8");
                    Y.one("#host").setStyle("background", "#FFEBE8");
                },
                "MISSING_LOGIN_FIELDS": function () {
                    resetBGColor();
                    var fields = Y.all("input");
                    fields.each(function () {
                        if (this.get("value") === "") {
                            this.setStyle("backgroundColor", "#FFEBE8");
                        }
                    });
                },
                "ERROR_PARSING_PORT": function () {
                    resetBGColor();
                    Y.one("#port").setStyle("background", "#FFEBE8");
                },
                "PORT_OUT_OF_RANGE": function () {
                    resetBGColor();
                    Y.one("#port").setStyle("background", "#FFEBE8");
                },
                "INVALID_USERNAME": function () {
                    resetBGColor();
                    Y.one("#username").setStyle("background", "#FFEBE8");
                    Y.one("#password").setStyle("background", "#FFEBE8");
                }
            };
            var request = Y.io(MV.URLMap.login(), {
                data: "username=" + username + "&password=" + password + "&port=" + port + "&host=" + host,
                method: "POST",
                on: {
                    success: function (ioId, responseObject) {
                        parsedResponse = Y.JSON.parse(responseObject.responseText);
                        var response = parsedResponse.response.result;
                        if (response !== undefined) {
                            Y.log("Successfully logging in", "info");
                            window.location = "home.html?dbInfo="+ host + "_" +port+"_"+username;
                        } else {
                            var error = parsedResponse.response.error;
                            var errorDiv = Y.one("#errorMsg");
                            errorDiv.setStyle("display", "inline");
                            errorHandlerMap[error.code]();
                            errorDiv.set("innerHTML", MV.errorCodeMap[error.code] || "Error!");
                            Y.log("Could not login. Message: [0]".format(error.message), "error");
                        }
                    },
                    failure: function (ioId, responseObject) {
                        alert("Could not send request! Please check if application server is running.");
                        Y.log("Could not send request.", "error");
                    }
                }
            });
        };

    Y.all("input").on("keyup", function (eventObject) {
        // for enter key submit the form
        if (eventObject.keyCode === 13) {
            tryLogin();
        }
    });

    Y.one("#login").on('click', tryLogin);
});