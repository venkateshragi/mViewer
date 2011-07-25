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
}).use("alert-dialog", "utility", "dialog-box", "yes-no-dialog", "io-base", "node", "json-parse", "event-delegate", "node-event-simulate", "stylize", "custom-datatable", function (Y) {
   // TODO: make loading panel module
   var dbDiv, userName, hostName, loadingPanel;
   var MV = Y.com.imaginea.mongoV;
    /* HANDLER FUNCTIONS */
   var parseAddCollResponse = function (responseObject) {
         var response = Y.JSON.parse(responseObject.responseText);
         if (response.result === "success") {
            MV.showAlertDialog("Collection created successfully!", MV.infoIcon);
            Y.log("Collection created successfully", "info");
            Y.one("#" + MV.currentDB).simulate("click");
         } else {
            MV.showAlertDialog("Collection creation failed! \n [0]".format(response.result), MV.warnIcon);
            Y.log("Collection creation failed: [0]".format(response.result), "error");
         }
       };
   var showError = function (responseObject) {
         MV.showAlertDialog("Collection creation failed!", MV.warnIcon);
         Y.log("Collection creation failed. Response Status: [0]".format(responseObject.status), "error");
       };
   var sendDropDBRequest = function () {
         Y.log("Preparing to send request to drop DB", "info");
         var request = Y.io(MV.URLMap.dropDB(), {
            method: "POST",
            on: {
               success: function (ioId, responseObject) {
                  if (responseObject.responseText === "success") {
                     MV.showAlertDialog("[0] dropped".format(MV.currentDB), MV.infoIcon, function () {
                        window.location = "home.html";
                     });
                     Y.log("[0] dropped".format(MV.currentDB), "info");
                     MV.currentDB = undefined;
                  } else {
                     MV.showAlertDialog("Could not drop: [0]".format(MV.currentDB), MV.warnIcon);
                     Y.log("Could not drop: [0], Response Recieved: [1]".format(MV.currentDB, data.result), "error");
                  }
               },
               failure: function (ioId, responseObject) {
                  Y.log("Could not drop: [0]".format(MV.currentDB) + data.result + "\n Status Text:" + responseObj.statusText, "error");
                  MV.showAlertDialog("Could not drop: [0], Response Recieved: [1], Status Text: [2]".format(MV.currentDB, data.result, responseObj.statusText), MV.warnIcon);
               }
            }
         });
         this.hide();
       };
   var handleNo = function (dialog) {
         this.hide();
       };
   var executeContextMenuOption = function (eventType, args) {
         var menuItem = args[1]; // The MenuItem that was clicked
         MV.currentDB = this.contextEventTarget.id;
         MV.toggleClass(Y.one("#" + MV.currentDB), Y.all("#dbNames li"));
         switch (menuItem.index) {
         case 0:
            // Delete database
            dialog = MV.showYesNoDialog("Do you really want to drop the Database?", sendDropDBRequest, handleNo);
            break;
         case 1:
            // add collection
            var form = "addColDialog";
            MV.getDialog(form, parseAddCollResponse, showError);
            break;
         case 2:
            // show statistics
            MV.hideQueryForm();
            MV.createDatatable(MV.URLMap.dbStatistics(), MV.currentDB);
            break;
         }
       };
       
       var dbContextMenu = new YAHOO.widget.ContextMenu("dbContextMenuID", {
      trigger: "dbNames",
      itemData: ["Delete Database", "Add Collection", "Statistics"]
   });
   dbContextMenu.render("dbContextMenu");
   dbContextMenu.clickEvent.subscribe(executeContextMenuOption);
   // A function handler to use for successful requests to get DB names:

   function parseGetDBResponse(ioId, responseObject) {
      Y.log("Response Recieved of get DB request", "info");
      if (responseObject.responseText !== undefined) {
         var info, parsedResponse, index, dbNames = "";
         try {
            parsedResponse = Y.JSON.parse(responseObject.responseText);
         } catch (e) {
            MV.showAlertDialog("Cannot load Databases \nResponse Recieved : [0]".format(responseObject.resposeText), MV.warnIcon, function () {
               window.location = "index.html";
            });
         }
         for (index = 0; index < parsedResponse.Name.length; index++) {
            dbNames += "<li id='[0]' >[1]</li>".format(parsedResponse.Name[index], parsedResponse.Name[index]);
         }
         dbDiv.set("innerHTML", dbNames);
         userName.set("innerHTML", parsedResponse.UserName);
         hostName.set("innerHTML", parsedResponse.Host);
         loadingPanel.hide();
      }
   }
   // A function handler to use for failed requests to get DB
   // names:

   function displayError(ioId, responseObject) {
      Y.log("Could not load the databases", "error");
      if (o.responseText !== undefined) {
         Y.log("Could not load the databases", "error");
         Y.log("Status code message: [0]".format(responseObject.statusText), "error");
         loadingPanel.hide();
         MV.showAlertDialog("Error: Could not load collections", MV.warnIcon);
      }
   }

   function requestDBNames() {
      loadingPanel = new LoadingPanel("Loading Databases...");
      loadingPanel.show();
      dbDiv = Y.one('#dbNames ul.lists');
      userName = Y.one('#username');
      hostName = Y.one('#hostname');
      var request = Y.io(MV.URLMap.getDBs(),
      // configuration for loading the database names
      {
         method: "GET",
         on: {
            success: parseGetDBResponse,
            failure: displayError
         }
      });
      Y.log("Sending request to load DB names", "info");
   }
   /*
    * Function to show dialog box for creating a collection or
    * a database
    */

   function showDialog(item, parent) {
      var form, dialog;
      var handleAddDBSuccess = function (o) {
            var data = Y.JSON.parse(o.responseText);
            if (data.result === "success") {
               alert("Creation Successful");
               window.location = "home.html";
            } else {
               alert("Creation failed:\n " + data.result);
            }
          };
      var handleAddCollSuccess = function (o) {
            var data = Y.JSON.parse(o.responseText);
            if (data.result === "success") {
               alert("Creation Successful");
               Y.one("#" + data.db).simulate("click");
            } else {
               alert("Creation failed:\n " + data.result);
            }
          };
      var handleFailure = function (o) {
            alert("Creation failed: " + o.status);
          };
      if (item === "database") {
         form = "addDBDialog";
         dialog = new Dialog(form, handleAddDBSuccess, handleFailure);
      } else if (item === "collection") {
         form = "addColDialog";
         dialog = new Dialog(form, handleAddCollSuccess, handleFailure);
      }
      dialog.show();
   }
   var executeHeaderOption = function (e) {
         menuOpt = e.currentTarget.get("id");
         MV.toggleClass(e.currentTarget, Y.all(".nav-cont li a"));
         // Clearing the content of main Body
         MV.mainBody.set("innerHTML", "");
         Y.one('#queryForm').set("innerHTML", "");
         // Y.one('#execQueryButton').set("innerHTML", "");
         if (menuOpt === "home") {
            window.location = "home.html";
         } else if (menuOpt === "collStats") {
            MV.createDatatable("collStats", e.currentTarget.get("id"));
            MV.header.set("innerHTML", "Collection Statistics");
         } else if (menuOpt === 'chart') {
            MV.header.set("innerHTML", "");
            window.open('chart2.html', '_newtab');
         }
       }; /* EVENT LISTENERS */
   // Make a request to load Database names when the page loads
   Y.on("load", requestDBNames);
   // Make request to load collection names when a database name is clicked
   Y.delegate("click", executeHeaderOption, ".nav-cont", "li a");
});
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