(function() {

    // (function(jQuery){
    //     jQuery.noConflict();
    // })(jQuery);

    var actualCode = function(Y) {
        function Navigator(nodes) {
            // associative array to hold the navigable links
            this.regions = [];
            var self = this;
            var i = 0;
            nodes.each(function(item) {
                self.regions[self.regions.length] = item;
            });
            this._bindKeysYUI();
        }

        Navigator.prototype = {
            add: function(section) {
                this.regions[section.id] = section;
            },
            getRegions : function() {
                return this.regions;
            },
            showCommandBar: function(type, args) {
                Y.log("Content assist displayed", "debug");
                var caSelector = '.floatingFooter';
                Y.one(caSelector).show();
                Y.one(caSelector + ' input').focus();
                
            },
            hideCommandBar:function(type, args) {
                Y.one('.floatingFooter').hide();
                Y.all('.shadow').each(function(item) {
                    item.removeClass('shadow');
                });
            },
            _bindKeysYUI: function() {
                var self = this;
                var spaceListener = new YAHOO.util.KeyListener(document, { ctrl:true, keys:' '.charCodeAt(0) }, {fn:self.showCommandBar});
                spaceListener.enable();
                var escapeListener = new YAHOO.util.KeyListener(document, { keys:27}, {fn:self.hideCommandBar}, 'keyup');
                escapeListener.enable();
                Y.all(".floatingFooter input").on("keyup", function (eventObject) {
                    self.highlight(self);
                    // for enter key submit the form
                    if (eventObject.keyCode === 13) {
                        self.selectElement(self);
                    }
                });

            },
            _bindKeysJQuery: function() {
                //jQuery(document).bind('keydown', 'Ctrl+32', self.showCommandBar);
            },
            _getContentAssist: function() {
                // TODO check how to get around the limitiation of making function calls from callbacks
                //return Y.one('.floatingFooter');
            },
            highlight: function(self) {
                var regionName = Y.one('.assistText').get('value');
                if (regionName && regionName.length > 0) {
                    self.clearStyles();
                    var index = 0;
                    for (;index < self.regions.length; index++) {
                        if (self.regions[index].get('id').indexOf(regionName) == 0) {
                            self.regions[index].addClass('shadow');
                        }
                    }
                }
            },
            selectElement: function(self) {
                
            },
            clearStyles: function() {
                Y.all('.shadow').each(function(item) {
                    item.removeClass('shadow');
                });
            }
        };

        var navigator =  new Navigator(Y.all(".navigable"));
    };

    // lets start the fun stuff with YUI
    YUI().use('event-key','node', actualCode);

}());