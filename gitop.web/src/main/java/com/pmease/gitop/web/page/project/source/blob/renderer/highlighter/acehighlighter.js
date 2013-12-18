var SourceHighlighter = SourceHighlighter || {};

SourceHighlighter.highlight = function(element) {
	var el = $(element)[0];
    
	var highlighter = ace.require("ace/ext/static_highlight")
    var m = el.className.match(/language-(\w+)|(text)/);
    if (!m) return
    var mode = "ace/mode/" + (m[1] || m[2]);
    highlighter.highlight(el, {mode: mode, theme: "ace/theme/textmate"});
    
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

