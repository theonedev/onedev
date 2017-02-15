/*
 * 2015, Robin Shen
 */
(function ( $ ) {
 
    $.fn.doneEvents = function(monitorEvents, callback, timeout) {
    	if (timeout == undefined)
    		timeout = 250;
		this.on(monitorEvents, function(e) {
			var $this = $(this);
			var doneTimer = $this.data("doneTimer");
			if (doneTimer) 
				clearTimeout(doneTimer);
			$this.data("doneTimer", setTimeout(function() {
				callback.call($this[0], e);
			}, timeout));
		});
    	
    	return this;
    };
 
}( jQuery ));
