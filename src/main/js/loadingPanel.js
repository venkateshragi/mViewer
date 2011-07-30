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


function LoadingPanel(message) {
	// TODO: make loading panel module
    this.panel = new YAHOO.widget.Panel("wait", {
        width: "240px",
        fixedcenter: true,
        close: false,
        draggable: false,
        visible: false,
        constraintoviewport: true,
        iframe: true
    });

    this.panel.setHeader(message);
    this.panel.setBody("<img src='images/loading.gif'>");
    this.panel.render(document.getElementById("mainBody"));
}
LoadingPanel.prototype.show = function () {
    this.panel.show();
};
LoadingPanel.prototype.hide = function () {
    this.panel.hide();
};