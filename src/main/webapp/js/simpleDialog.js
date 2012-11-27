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

YUI.add('yes-no-dialog', function(Y) {
    YUI.namespace('com.imaginea.mongoV');
    var MV = YUI.com.imaginea.mongoV;
    MV.showYesNoDialog = function(msg, handleYes, handleNo) {
        var simpleDialog = new YAHOO.widget.SimpleDialog("simpledialog", {
            width: "300px",
            fixedcenter: true,
            visible: false,
            draggable: true,
            close: false,
            text: msg,
            icon: YAHOO.widget.SimpleDialog.ICON_WARN,
            constraintoviewport: true,
            buttons: [
                {
                    text: "Yes",
                    handler: handleYes,
                    isDefault: true},
                {
                    text: "No",
                    handler: handleNo}
            ]
        });
        simpleDialog.setHeader("Are you sure?");
        simpleDialog.render("simpleDialogContainer");
        simpleDialog.show();
    };
}, '3.3.0', {
    requires: []
});
