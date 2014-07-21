var SourceHighlighter = SourceHighlighter || {};

SourceHighlighter.highlight = function(element, hasLineId) {
	var highlighter = ace.require("ace/ext/static_highlight");
	var dom = ace.require("ace/lib/dom")
	function qsa(sel) {
		return [].slice.call(document.querySelectorAll(sel));
	}
	
	qsa(element).forEach(function(el) {
		var m = el.className.match(/lang-(\w+)|(text)/);
	    //if (!m) return
	    //var mode = "ace/mode/" + (m[1] || m[2]);
	    highlighter.highlight(el, {mode: "ace/mode/java", theme: "ace/theme/xcode", hasLineId: hasLineId});
	});
    
    $(function() {
    	//This emulates a click on the correct button on page load
        if(document.location.hash){
        	var lineNo = document.location.hash.substr(2);
        	$("#LC" + lineNo).addClass("selected");
        }

        //Click a button to change the hash
        $(".blob-lineno a").click(function(){
        		var oldLineNo = document.location.hash.substr(2);
                $("#LC" + oldLineNo).removeClass('selected');
                var currentLineNo = $(this).attr("id").substr("LL".length);
                $("#LC" + currentLineNo).addClass("selected");
                document.location.hash="L"+currentLineNo;
                //return false;
        });
    });
};

