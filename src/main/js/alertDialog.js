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
// TODO send a custom handler if not provided run the default one
YUI.add('alert-dialog', function (Y) {
    Y.namespace('com.imaginea.mongoV');
    Y.com.imaginea.mongoV.showAlertDialog = function (msg, ico, handler) {
        var simpleDialog = new YAHOO.widget.SimpleDialog("simpledialog", {
            width: "300px",
            fixedcenter: true,
            visible: false,
            draggable: true,
            close: false,
            text: msg,
            icon: ico || YAHOO.widget.SimpleDialog.ICON_INFO,
            constraintoviewport: true,
            zIndex: 10,
            buttons: [{
                text: "OK",
                handler: handler ||
                function () {
                    this.hide();
                },
                isDefault: true
            }]
        });
        simpleDialog.setHeader("Info");
        simpleDialog.render("alertDialogContainer");
        simpleDialog.show();
    };
}, '3.3.0', {
    requires: []
});