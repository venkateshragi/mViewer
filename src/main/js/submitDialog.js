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

/**
 * Show a dialog box
 * @module dialog-box
 */

YUI.add('submit-dialog', function(Y) {
    YUI.namespace('com.imaginea.mongoV');
    var MV = YUI.com.imaginea.mongoV;

    MV.showSubmitDialog = function Dialog(form, successHandler, failureHandler) {
        YAHOO.util.Dom.removeClass(form, "yui-pe-content");

        function cancelCurrent() {
            this.cancel();
        }

        function addCollection() {
            Y.log("Submit handler for add collection called", "info");
            var newCollInfo = this.getData();
            if (newCollInfo.name === "") {
                MV.showAlertDialog("Please enter the name.");
            } else {
                Y.one("#newName").set("value", newCollInfo.name);
                Y.one("#" + form + " .bd form").setAttribute("action", MV.URLMap.insertColl());
                this.submit();
            }
        }

	    function addGridFS() {
		    Y.log("Submit handler for adding gridFS bucket called", "info");
		    var newCollInfo = this.getData();
		    if (newCollInfo.name === "") {
			    MV.showAlertDialog("Please enter the bucket name.");
		    } else {
			    Y.one("#" + form + " .bd form").setAttribute("action", MV.URLMap.addGridFS(newCollInfo.name));
			    this.submit();
		    }
	    }

        function addDB() {
            var newDBInfo = this.getData();
            if (newDBInfo.name === "") {
                MV.showAlertDialog("Please enter the name.");
            } else {
                Y.one("#newName").set("value", newDBInfo.name);
                Y.one("#" + form + " .bd form").setAttribute("action", MV.URLMap.insertDB());
                this.submit();
            }
        }

        function addDocument() {
            var newDoc = this.getData().document;
            try {
                Y.JSON.parse(newDoc);
                Y.one("#" + form + " .bd form").setAttribute("action", MV.URLMap.insertDoc());
                this.submit();
            } catch (e) {
                MV.showAlertDialog("Please enter the new document in JSON format", MV.warnIcon);
                Y.log("New Document format not JSON", "error");
            }
        }
        var sumbitHandlerMap = {
            "addColDialogSubmitHandler": addCollection,
	        "addGridFSDialogSubmitHandler": addGridFS,
            "addDBDialogSubmitHandler": addDB,
            "addDocDialogSubmitHandler": addDocument
        };
	    var dialogBox = new YAHOO.widget.Dialog(form, {
		    width: "30em",
		    fixedcenter: true,
		    visible: false,
		    effect: {
			    effect: YAHOO.widget.ContainerEffect.SLIDE,
			    duration: 0.25
		    },
		    constraintoviewport: true,
		    buttons: [
			    {
				    text: "Submit",
				    handler: sumbitHandlerMap[form + "SubmitHandler"] ||
						    function() {
							    this.submit();
						    },
				    isDefault: true
			    },
			    {
				    text: "Cancel",
				    handler: cancelCurrent
			    }
		    ]
	    });
        dialogBox.callback = {
            success: successHandler,
            failure: failureHandler
        };
        dialogBox.render();
        dialogBox.show();
        return dialogBox;
    };
}, '3.3.0', {
    requires: ["utility", "node", "alert-dialog"]
});