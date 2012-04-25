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
 * The module provides function <tt>showAlertMessage</tt> to show a simple information message.
 */
YUI.add('alert-dialog', function (Y) {
	var timeoutId;

	YUI.com.imaginea.mongoV.showAlertMessage = function (msg, icon, handler) {
		document.getElementById('infoMsg').style.display = 'inline-block';
		clearTimeout(timeoutId);
		Y.one('#infoIcon').set("className", icon);
		Y.one('#infoText').set("innerHTML", msg);
		timeoutId = setTimeout("document.getElementById('infoMsg').style.display='none'", 12000);
	};
}, '3.3.0', {
	requires: ["node"]
});
