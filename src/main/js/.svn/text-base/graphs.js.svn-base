YUI({
    filter: 'raw'
}).use("io-base", "node", "json-parse", "utility", function (Y) {
    Y.namespace('com.imaginea.mongoV');
    var MV = Y.com.imaginea.mongoV;
    var combinedChart;
//    var toggleChartVisibility = function (e) {
//            var target = e.currentTarget;
//            var match = target.get("id").match(/\d+/);
//            var index = parseInt(match[0], 10);
//            alert(index);
//            combinedChart.setSeriesStylesByIndex(index, {
//                visibility: target.get("checked") ? "visible" : "hidden"
//            });
//        };
//    var chartOptions = "<table width='30%'>";
//    chartOptions += "<tr>";
//    chartOptions += "<td style='color:#FF4848;font-weight:bold'> Show Query/Sec:</td>";
//    chartOptions += "<td><input id='checkbutton1' type='checkbox' name='series1' value='1' checked='true'></td>";
//    chartOptions += "</tr>";
//    chartOptions += "<tr>";
//    chartOptions += "<td style='color:#AE70ED;font-weight:bold'> Show Update/Sec:</td>";
//    chartOptions += "<td><input id='checkbutton2' type='checkbox' name='series2' value='2' checked='true'></td>";
//    chartOptions += "</tr>";
//    chartOptions += "<tr>";
//    chartOptions += "<td style='color:#62D0FF;font-weight:bold'>Show Delete/Sec:</td>";
//    chartOptions += "<td><input id='checkbutton3' type='checkbox' name='series3' value='3' checked='true' ></td>";
//    chartOptions += "</tr>";
//    chartOptions += "<tr>";
//    chartOptions += "<td style='color:#2DC800;font-weight:bold'>Show Insert/Sec:</td>";
//    chartOptions += "<td><input id='checkbutton4' type='checkbox' name='series4' value='4' checked='true' ></td>";
//    chartOptions += "</tr>";
//    chartOptions += "<tr>";
//    chartOptions += "<td><button id='animation' class='button'>Stop Animation</button></td>";
//    chartOptions += "</tr>";
//    chartOptions += "</table>";
    var drawChart = function () {
            YAHOO.widget.Chart.SWFURL = "lib/yui2/build/charts/assets/charts.swf";
            //Create a TabView
            var tabView = new YAHOO.widget.TabView();
            //Add a tab for the Query
            tabView.addTab(new YAHOO.widget.Tab({
                label: 'Combined View',
                content: '<span class="chart_title">Combined View</span> <div class="chart" id="combinedChart"></div><center><span class="chart_title">Time Stamp</span></center>' /*+ chartOptions*/,
                active: true,
                width: '100%'
            }));
            tabView.addTab(new YAHOO.widget.Tab({
                label: 'Queries',
                content: '<span class="chart_title">Queries/Second</span><div class="chart" id="queryChart"></div><center><span class="chart_title">Time Stamp</span></center>',
                width: '100%'
            }));
            //    Add a tab for the Update
            tabView.addTab(new YAHOO.widget.Tab({
                label: 'Updates',
                content: '<span class="chart_title">Updates/Second</span><div class="chart" id="updateChart"></div><center><span class="chart_title">Time Stamp</span></center>',
                width: '100%'
            }));
            //    Add a tab for the Delete
            tabView.addTab(new YAHOO.widget.Tab({
                label: 'Deletes',
                content: '<span class="chart_title">Deletes/Second</span><div class="chart" id="deleteChart"></div><center><span class="chart_title">Time Stamp</span></center>',
                width: '100%'
            }));
            //    Add a tab for the Insert
            tabView.addTab(new YAHOO.widget.Tab({
                label: 'Inserts',
                content: '<span class="chart_title">Inserts/Second</span><div class="chart" id="insertChart"></div><center><span class="chart_title">Time Stamp</span></center>',
                  width: '100%'
            }));
            //Append TabView to its container div
            tabView.appendTo('tabContainer');
            //--- data
            var queryData = new YAHOO.util.DataSource(MV.URLMap.graphQuery());
            queryData.connMethodPost = false;
            queryData.responseType = YAHOO.util.DataSource.TYPE_JSON;
            queryData.responseSchema = {
                resultsList: "response.result",
                fields: ["TimeStamp", "QueryValue", "UpdateValue", "DeleteValue", "InsertValue"]
            };
            var pollingTime = 1000;
            var seriesDef = [{
                displayName: "Query",
                yField: "QueryValue",
                style: {
                    color: 0xFF4848
                }
            }, {
                displayName: "Update",
                yField: "UpdateValue",
                style: {
                    color: 0xAE70ED
                }
            }, {
                displayName: "Delete",
                yField: "DeleteValue",
                style: {
                    color: 0x62D0FF
                }
            }, {
                displayName: "Insert",
                yField: "InsertValue",
                style: {
                    color: 0x2DC800
                }
            }];
            combinedChart = new YAHOO.widget.LineChart("combinedChart", queryData, {
                xField: "TimeStamp",
                series: seriesDef
            });
            var queryChart = new YAHOO.widget.LineChart("queryChart", queryData, {
                xField: "TimeStamp",
                yField: "QueryValue"
            });
            var updateChart = new YAHOO.widget.LineChart("updateChart", queryData, {
                xField: "TimeStamp",
                yField: "UpdateValue"
            });
            var deleteChart = new YAHOO.widget.LineChart("deleteChart", queryData, {
                xField: "TimeStamp",
                yField: "DeleteValue"
            });
            var insertChart = new YAHOO.widget.LineChart("insertChart", queryData, {
                xField: "TimeStamp",
                yField: "InsertValue"
            });
            var myCallback = {
                success: function () {
                    queryChart._loadDataHandler.apply(queryChart, arguments);
                    updateChart._loadDataHandler.apply(updateChart, arguments);
                    deleteChart._loadDataHandler.apply(deleteChart, arguments);
                    insertChart._loadDataHandler.apply(insertChart, arguments);
                    combinedChart._loadDataHandler.apply(combinedChart, arguments);
                },
                failure: function () {
                    Y.log("Polling failure for getting Graph info", "error");
                }
            };
            queryData.setInterval(pollingTime, null, myCallback);
//            Y.on("click", toggleChartVisibility, ["#checkButton1", "#checkButton2", "#checkButton3", "#checkbutton4"]);
        };
    //A function handler to use for successful requests:
    var handleSuccess = function (ioId, responseObject) {
            var parsedResponse = Y.JSON.parse(responseObject.responseText);
            response = parsedResponse.response.result;
            if (response !== undefined) {
                Y.log("Chart initiated");
                drawChart();
            } else {
                MV.showAlertDialog("Error: [0]".format(parsedResponse.response.error.message), MV.warnIcon);
                Y.log("Error: [0]".format(parsedResponse.response.error.message), "error");
            }
        };
    //A function handler to use for failed requests:
    var handleFailure = function (ioId, o) {
            MV.showAlertDialog("Could not send request!", MV.warnIcon);
            Y.log("Sending request to initiate the graph failed. Response Status: [0]".format(responseObject.statusText), "error", "chart");
        };
    var cfg = {
        method: "GET"
    };
    var makeRequest = function () {
            var fullUrl = window.location.search;
            Y.one("#tokenID").set("value", fullUrl.substring(fullUrl.indexOf("=") + 1));
            var sUrl = MV.URLMap.graphInitiate();
            var request = Y.io(sUrl, cfg);
        };
    var stopAnimation = function (e) {
            if (e.currentTarget.get("innerHTML") === 'Stop Animation') {
                combinedChart.setStyle("animationEnabled", false);
                e.currentTarget.set("innerHTML", "Start Animation");
            } else {
                combinedChart.setStyle("animationEnabled", true);
                e.currentTarget.set("innerHTML", "Stop Animation");
            }
        };
    //Subscribe our handlers to IO's global custom events:
    Y.on('io:success', handleSuccess);
    Y.on('io:failure', handleFailure);
    Y.on('click', stopAnimation, '#animation');
    // Make a request when the page loads
    Y.on("load", makeRequest);
});