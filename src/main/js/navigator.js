(function() {

    var actualCode = function(Y) {
        var MV = YUI.com.imaginea.mongoV;
        var sm = MV.StateManager;
        var selectedElement;

        function Navigator(selectorString) {
            // associative array to hold the navigable links
            this.regions = [];
            this.selectorString = selectorString;
            var self = this;
            var navigatorCallback = function() {self.init();};
            sm.subscribe(sm.events.collectionsChanged, navigatorCallback);
            sm.subscribe(sm.events.dbsChanged, navigatorCallback);
            this._bindKeysYUI();
            this.init();
        }
        function addChildren(node, regionManager) {
            node.all('input, ul li').each(
                function(item) {
                    regionManager.add(item);
                }
            );
        }

        Navigator.prototype = {
            init: function() {
                Y.log("About to init the navigator", "debug");
                var selectorString = this.selectorString;
                var self = this;
                Y.all(selectorString).each(function(item) {
                    self.add(item);
                });
            },
            add: function(section) {
                var regionId = section.get('id');
                if (regionId && regionId.length > 0) {
                    this.regions[this.regions.length] = section;
                    addChildren(section, this);
                }
            },
            getRegions : function() {
                return this.regions;
            },
            showCommandBar: function(type, args) {
                var caSelector = '.floatingFooter';
                Y.one(caSelector + ' input').set('value','');
                Y.one(caSelector).show();
                Y.one(caSelector + ' input').focus();
                Y.log("CA displayed and navigator knows [0] magic keys".format(this.regions.length), "debug");
            },
            hideCommandBar:function(type, args) {
                Y.one('.floatingFooter').hide();
                Y.all('.shadow').each(function(item) {
                    item.removeClass('shadow');
                    item.removeClass('simulatedHover');
                });
            },
            _bindKeysYUI: function() {
                // TODO instead of keylistener use event listener
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
            highlight: function(self) {
                var regionName = Y.one('.assistText').get('value');
                if (regionName && regionName.length > 0) {
                    self.clearStyles();
                    var index = 0;
                    for (;index < self.regions.length; index++) {
                        if (self.regions[index].get('id').toUpperCase().indexOf(regionName.toUpperCase()) === 0) {
                            self.regions[index].addClass('shadow');
                            self.regions[index].addClass('simulatedHover');
                            selectedElement = self.regions[index];
                        }
                    }
                }
            },
            selectElement: function(self) {
                if (selectedElement) {
                    Y.log(selectedElement,"debug");
                    selectedElement.simulate('click');
                }
                self.hideCommandBar();
            },
            clearStyles: function() {
                Y.all('.shadow').each(function(item) {
                    item.removeClass('shadow');
                    item.removeClass('simulatedHover');
                });
                selectedElement = null;
            },
            rebuild: function() {
                this.init();
            }
        };

        var navigator =  new Navigator('.navigable');
    };

    // lets start the fun stuff with YUI
    YUI().use('event-key','node', 'utility', 'node-event-simulate', actualCode);

}());