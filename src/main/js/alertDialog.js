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
 * The module provides function <tt>showAlertDialog</tt> to show a simple information message.
 */
YUI.add('alert-dialog', function (Y) {
    YUI.com.imaginea.mongoV.showAlertDialog = function (msg, ico, handler) {
	document.getElementById('informmsg').style.display='inline';
	Y.one('#informmsg').set("innerHTML", msg);
	window.setTimeout("document.getElementById('informmsg').style.display='none'", 6000);
    };
}, '3.3.0', {
    requires: ["node"]
});
