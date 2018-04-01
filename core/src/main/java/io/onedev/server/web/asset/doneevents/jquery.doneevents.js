/*
 * 2015, Robin Shen
 */
(function ( $ ) {
 
    $.fn.doneEvents = function(monitorEvents, callback, timeout) {
    	if (timeout == undefined)
    		timeout = 250;
    	var doneTimer;
		this.on(monitorEvents, function(e) {
			var $this = $(this);
			if (doneTimer) 
				clearTimeout(doneTimer);
			doneTimer = setTimeout(function() {
				callback.call($this[0], e);
			}, timeout);
		});
    	
    	return this;
    };
 
}( jQuery ));
