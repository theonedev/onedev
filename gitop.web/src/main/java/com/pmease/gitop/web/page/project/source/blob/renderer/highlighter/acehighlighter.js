var SourceHighlighter = SourceHighlighter || {};

SourceHighlighter.highlight = function(element) {
	var el = $(element)[0];
    
	var highlighter = ace.require("ace/ext/static_highlight")
    var m = el.className.match(/language-(\w+)|(text)/);
    if (!m) return
    var mode = "ace/mode/" + (m[1] || m[2]);
    highlighter.highlight(el, {mode: mode, theme: "ace/theme/textmate"});
    
    $('.blob-lineno span').each(function() {
    	var $self = $(this);
    	$self.click(function(e) {
    		removeActive();
    		$self.addClass('active');
    	});
    });
    
    var removeActive = function() {
    	$('.blob-lineno span').each(function() {
    		var $self = $(this);
    		$self.removeClass('active');
    	});
    };
};

