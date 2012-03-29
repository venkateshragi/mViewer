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
 *
 * Author: Srinath Anantha
 */

YUI.add('upload-dialog', function(Y) {
	YUI.namespace('com.imaginea.mongoV');
	var MV = YUI.com.imaginea.mongoV;

	MV.showUploadDialog = function(form) {
		YAHOO.util.Dom.removeClass(form, "yui-pe-content");

		var uploadDialog = new YAHOO.widget.SimpleDialog(form, {
			width: "40em",
			fixedcenter: true,
			visible: false,
			draggable: true,
			effect: {
				effect: YAHOO.widget.ContainerEffect.SLIDE,
				duration: 0.25
			},
			constraintoviewport: true,
			buttons: [
				{
					text: "Close",
					handler: handleClose
				}
			]
		});
		uploadDialog.setHeader("File Upload");
		uploadDialog.render();
		uploadDialog.show();

		// Initialize the jQuery File Upload widget:
		$('#fileupload').fileupload({
			url: MV.URLMap.insertFile(),
			sequentialUploads: true
		});
		// Clear table body
		$('#fileupload-body').empty();
	}

	function handleClose() {
		$('#fileupload').fileupload('destroy');
		this.cancel();
		/*setTimeout(function() {
		 }, 5000);
		 Y.one("#" + Y.one("#currentBucket").get("value").replace(/ /g, '_')).simulate("click");*/
	}

}, '3.3.0', {
	requires: []
});

