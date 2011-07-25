/**
 *
 */
YUI.add('utility', function (Y) {
   Y.namespace('com.imaginea.mongoV');
   var MV = Y.com.imaginea.mongoV;
   MV.getProperties = function (doc) {
      var key, name, allKeys = [];
      for (key in doc) {
         if (doc.hasOwnProperty(key)) {
            allKeys.push(key);
         }
      }
      return allKeys;
   };
   MV.isArray = function (o) {
      return Object.prototype.toString.call(o) === '[object Array]';
   };
   MV.isObject = function (o) {
      return (typeof o === "object");
   };
   var formUpperPart = "",
       formLowerPart = "";
   formUpperPart += "<textarea id='queryBox' name='queryBox' rows='3' cols='80' >";
   formUpperPart += "{}";
   formUpperPart += "</textarea>";
   formUpperPart += "<label for='fields' ></label><ul id='fields' class='checklist'>";
   formLowerPart += "</ul><br>";
   formLowerPart += "<label for='limit'> Limit: </label><input id='limit' type='text' name='limit' value='5' size='5' />";
   formLowerPart += "<label for='skip'> Skip: </label><input id='skip' type='text' name='skip' value='0' size='5' />";
   formLowerPart += "<button id='execQueryButton' class='btn'>Execute Query</button>";
   MV.getForm = function (data) {
      var checkList = "";
      for (index = 0; index < data.Keys.length; index++) {
         checkList += "<li><label for='" + data.Keys[index] + "'><input id='" + data.Keys[index] + "' name='" + data.Keys[index] + "' type='checkbox' checked=true />" + data.Keys[index] + "</label></li>";
      }
      return formUpperPart + checkList + formLowerPart;
   };
   
   MV.hideQueryForm = function (){
	   var queryForm = Y.one('#queryForm');
       queryForm.removeClass('form-cont');
       queryForm.set("innerHTML", "");
   };
   MV.currentDB = undefined;
   MV.currentColl = undefined;
   
   MV.URLMap = {
			dropColl: function(){
				   return "CollectionOperations/dropColl?collName="+MV.currentColl+"&dbName="+MV.currentDB;
			   },   
			getColl :  function(){
				   return "CollectionOperations/coll?dbName="+MV.currentDB;
			   },
			collStatistics : function (){
					return "CollectionOperations/collStats?collName="+MV.currentColl+"&dbName="+MV.currentDB;  
			} ,
			dbStatistics:function (){
					return "DBOperations/dbStats?dbName="+MV.currentDB;  
			} , 
			dropDB: function(){
				   return "DBOperations/dropDB?dbName="+MV.currentDB;
			   },  
			getDBs:	function(){
				   return "DBOperations/dbs";
			   },
			collectionKeys:	function(){ 
					return "DocumentOperations/getAllKeys?coll=" + MV.currentColl;
			},
			getCollections: function(){
					return "DocumentOperations/getDocs?coll=" + MV.currentColl + "&";
			} 
			
	};
   
   MV.mainBody =  Y.one("#mainBody");
   MV.header =  Y.one("#mainBodyHeader");
   MV.warnIcon = YAHOO.widget.SimpleDialog.ICON_WARN;
   MV.infoIcon = YAHOO.widget.SimpleDialog.ICON_INFO;
   
}, '3.3.0', {
   requires: ["node"]
});