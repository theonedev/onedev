(function($) {
	var highlighter = ace.require("ace/ext/static_highlight");
	
	$.fn.highlight = function(options) {
	    var settings = $.extend({
	    	theme: "ace/theme/github"
	    }, options);
		
	    return this.each(function() {
			highlighter.highlight(this, settings);
	    });
	    
	};
	
}(jQuery));
