/**
 * 
 */

YUI({
		filter : 'raw'
	}).use("console", "console-filters", "dd-plugin", function(Y) {
		// creating a console screen
		var globalConsole;
		globalConsole = new Y.Console({
			logSource : Y.Global,
			strings : {
				title : 'Console',
				pause : 'Pause',
				clear : 'Clear',
				collapse : 'Collapse',
				expand : 'Expand'
			},
			visible : false
		}).plug(Y.Plugin.ConsoleFilters).plug(Y.Plugin.Drag, {
			handles : ['.yui3-console-hd']
		}).render();
		// Set up the button listeners
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