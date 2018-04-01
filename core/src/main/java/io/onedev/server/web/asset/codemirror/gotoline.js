(function(mod) {
  if (typeof exports == "object" && typeof module == "object") // CommonJS
    mod(require("../../lib/codemirror"), require("../dialog/dialog"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../lib/codemirror", "../dialog/dialog"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {
  "use strict";

  function dialog(cm, text, shortText, deflt, f) {
    if (cm.openDialog) cm.openDialog(text, f, {value: deflt});
    else f(prompt(shortText, deflt));
  }
  function validateQuery(query) {
    if (/\d+/.test(query)) return true;
    return false; 
  }
  var queryDialog =
    'Go to Line :  <input type="text" style="width: 10em"/> <span style="color: #888">(Press Enter)</span>';
    
  function jumpToLine(cm) {
    dialog(cm, queryDialog, "Go to Line:", 0, function(query) {
      if (validateQuery(query)) {
        var line = parseInt(query);
        line = line -1;
        cm.setCursor({line:line,ch:0});
        var myHeight = cm.getScrollInfo().clientHeight; 
        var coords = cm.charCoords({line: line, ch: 0}, "local"); 
        cm.scrollTo(null, (coords.top + coords.bottom - myHeight) / 2); 
      }
    });
  }
  
  CodeMirror.commands.gotoLine = function(cm) {jumpToLine(cm);};
});
