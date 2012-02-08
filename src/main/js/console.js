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

YUI({
    filter: 'raw'
}).use("console", "console-filters", "dd-plugin", function(Y) {
    // creating a console screen
    var globalConsole = new Y.Console({
        logSource: Y.Global,
        strings: {
            title: 'Console',
            pause: 'Pause',
            clear: 'Clear',
            collapse: 'Collapse',
            expand: 'Expand'
        },
        visible: false
    }).plug(Y.Plugin.ConsoleFilters).plug(Y.Plugin.Drag, {
        handles: ['.yui3-console-hd']
    }).render();
    /**
     * Event listener for show/hide console button
     */
    function toggle(e, globalConsole) {
        if (globalConsole.get('visible')) {
            globalConsole.hide();
            this.set('innerHTML', 'Show console');
        } else {
            globalConsole.show();
            globalConsole.syncUI(); // to handle any UI changes queued while hidden.
            this.set('innerHTML', 'Hide console');
        }
    }
    Y.on('click', toggle, '#toggle_console', null, globalConsole);
});