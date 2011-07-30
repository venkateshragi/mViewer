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
    var trySetLevel = function (e) {
            var index = document.getElementById("logLevel").selectedIndex;
            var logValue = document.getElementById("logLevel").options[index].text;
            var request = Y.io(MV.URLMap.troubleShoot(), {
                data: "level=" + logValue,
                method: "GET",
                on: {
                    success: function (ioId, responseObject) {
                        parsedResponse = Y.JSON.parse(responseObject.responseText);
                        var response = parsedResponse.response.result;
                        if (response !== undefined) {
                            Y.log("Successfully set the logging level", "info");
                            Y.one("#logMessage").set("innerHTML", response + "<br> Please send us mViewer.log stored in the logs directory of the application server in case of any problems.<br> agaur111@gmail.com , mtlrachit@gmail.com");
                        } else {
                            var error = parsedResponse.response.error;
                            var errorDiv = Y.one("#logMessage");
                            errorDiv.set("innerHTML", MV.errorCodeMap[error.code] || "Error!");
                            Y.log("Could not set logging level. Message: [0]".format(error.message), "error");
                        }
                    },
                    failure: function (ioId, responseObject) {
                        alert("Could not send request! Please check if application server is running.");
                        Y.log("Could not send request.", "error");
                    }
                }
            });
        };
    Y.one("#logLevelChangeBtn").on('click', trySetLevel);
});