// CodeMirror, copyright (c) by Marijn Haverbeke and others
// Distributed under an MIT license: http://codemirror.net/LICENSE

(function(mod) {
	if (typeof exports == "object" && typeof module == "object") // CommonJS
		mod(require("../../lib/codemirror"));
	else if (typeof define == "function" && define.amd) // AMD
		define(["../../lib/codemirror"], mod);
	else // Plain browser env
		mod(CodeMirror);
})(function(CodeMirror) {
	"use strict";

	var DEFAULT_TOKEN_STYLE = "identifierhighlight";
	var DEFAULT_DELAY = 100;

	function State(options) {
		if (typeof options == "object") {
			this.style = options.style;
			this.delay = options.delay;
		}
	    if (this.style == null) 
	    	this.style = DEFAULT_TOKEN_STYLE;
	    if (this.delay == null) 
	    	this.delay = DEFAULT_DELAY;
	    this.overlay = this.timeout = null;
	}

	CodeMirror.defineOption("highlightIdentifiers", false, function(cm, val, old) {
		if (old && old != CodeMirror.Init) {
			var over = cm.state.identifierHighlighter.overlay;
			if (over) 
				cm.removeOverlay(over);
			clearTimeout(cm.state.identifierHighlighter.timeout);
			cm.state.identifierHighlighter = null;
			cm.off("cursorActivity", cursorActivity);
		}
		if (val) {
			cm.state.identifierHighlighter = new State(val);
			highlightIdentifiers(cm);
			cm.on("cursorActivity", cursorActivity);
		}
	});

	function cursorActivity(cm) {
		var state = cm.state.identifierHighlighter;
		clearTimeout(state.timeout);
		state.timeout = setTimeout(function() {highlightIdentifiers(cm);}, state.delay);
	}

  	function highlightIdentifiers(cm) { 
  		cm.operation(function() {
  			var state = cm.state.identifierHighlighter;
  			if (state.overlay) {
  				cm.removeOverlay(state.overlay);
  				state.overlay = null;
  			}
  			if (!cm.somethingSelected()) {
  				clearScrollbarMatches(state);
  				if (CodeMirror.commands.clearSearch)
  					CodeMirror.commands.clearSearch(cm);
  				var re = /[\w$]/;
  				var cur = cm.getCursor(), line = cm.getLine(cur.line), start = cur.ch, end = start;
  				while (start && re.test(line.charAt(start - 1))) --start;
  				while (end < line.length && re.test(line.charAt(end))) ++end;
  				if (start < end) {
  					var tokenType = cm.getTokenTypeAt(new CodeMirror.Pos(cur.line, (start+end)/2));
  					if (tokenType != null) {
  	  					if (tokenType.indexOf("variable") > -1 
  	  							|| tokenType.indexOf("property") > -1 
  	  							|| tokenType.indexOf("def") > -1) {
		  					var query = line.slice(start, end);
		  					cm.addOverlay(state.overlay = makeOverlay(query, re, state.style));
	
		  					if (cm.showMatchesOnScrollbar) {
		  						clearScrollbarMatches(state);
		  						
		  						var queryRe = new RegExp("\\b" + query + "\\b");
		  						state.annotate = cm.showMatchesOnScrollbar(queryRe, false);
		  					}
  	  					}
  					}
  				}
  			}
  		});
  	}
  	
  	function clearScrollbarMatches(state) {
  		if (state.annotate) {
  			state.annotate.clear();
  			state.annotate = null;
  		}
  	}

  	function boundariesAround(stream, re) {
  		return (!stream.start || !re.test(stream.string.charAt(stream.start - 1))) &&
  			(stream.pos == stream.string.length || !re.test(stream.string.charAt(stream.pos)));
  	}

  	function makeOverlay(query, hasBoundary, style) {
  		return {token: function(stream) {
  			if (stream.match(query) && (!hasBoundary || boundariesAround(stream, hasBoundary)))
  				return style;
  			stream.next();
  			stream.skipTo(query.charAt(0)) || stream.skipToEnd();
  		}};
  	}
});
