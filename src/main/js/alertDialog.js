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
 * @module alert-dialog
 * The module provides function <tt>showAlertDialog</tt> to show the simple dialog box.
 */
YUI.add('alert-dialog', function (Y) {
    YUI.namespace('com.imaginea.mongoV');
    var MV = YUI.com.imaginea.mongoV;
    if(!MV.simpleDialog){
    	 // Simple Dialog object
    	MV.simpleDialog = new YAHOO.widget.SimpleDialog("alertDialog", {
            width: "300px",
            fixedcenter: true,
            visible: false,
            draggable: true,
            close: false,
            constraintoviewport: true,
            zIndex: 10,
        });
        MV.simpleDialog.setHeader("Info");
        MV.simpleDialog.render("alertDialogContainer");
    }
    /**
     * The function pops up a simple dialog box 
     * @param msg {String} This is the message that the alert dialog box will show
     * @param ico {Object} The icon to be show with the message. The icon can either be a Warn icon
     * or a Info icon
     * @param handler {Function} The call back function when <tt>OK</tt> is clicked on the dialog box   
     */
    YUI.com.imaginea.mongoV.showAlertDialog = function (msg, ico, handler) {
        var buttons = [{
            text: "OK",
            handler: handler ||
            function () {
                this.hide();
            },
            isDefault: true
        }];
        MV.simpleDialog.cfg.setProperty("buttons", buttons);
        MV.simpleDialog.setBody(msg);
        MV.simpleDialog.cfg.setProperty("icon", ico || YAHOO.widget.SimpleDialog.ICON_INFO);
        MV.simpleDialog.show();
    };
}, '3.3.0', {
    requires: []
});