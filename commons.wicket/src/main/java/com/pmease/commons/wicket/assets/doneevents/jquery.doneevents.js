/*
 * 2015, Robin Shen
 */
(function ( $ ) {
 
    $.fn.doneEvents = function(monitorEvents, callback, timeout) {
    	if (timeout == undefined)
    		timeout = 250;
		this.on(monitorEvents, function() {
			var $this = $(this);
			var doneTimer = $this.data("doneTimer");
			if (doneTimer) 
				clearTimeout(doneTimer);
			$this.data("doneTimer", setTimeout(function() {
				callback.call($this[0]);
			}, timeout));
		});
    	
    	return this;
    };
 
}( jQuery ));
