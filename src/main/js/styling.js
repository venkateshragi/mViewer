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
YUI.add('stylize', function (Y) {
    YUI.namespace('com.imaginea.mongoV');
    var MV = YUI.com.imaginea.mongoV;
    /**
     * <p>toggleClass adds a class to the menu option as soon as it is clicked</p>
     *
     * @module mViewer
     * @namespace com.imaginea.mongov
     * @requires node
     * @constructor
     * @param node
     *		  <dd>(required) The node that is clicked</dd>
     * @param otherNodes
     *		  <dd>(required) Nodes which are there in the menu </dd>
     */
    MV.toggleClass = function (node, otherNodes) {
        otherNodes.removeClass('sel');
        $(node._node).closest('li').addClass('sel')
    };
}, '3.3.0', {
    requires: ["node"]
});