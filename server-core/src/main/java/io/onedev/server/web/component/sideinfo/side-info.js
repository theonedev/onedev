onedev.server.sideInfo = {
	isOverlayMode: function($container) {
		return window.matchMedia("(max-width: 990px)").matches
				|| $container.closest(".hide-side-info").length != 0;
	},
	
	close: function($container) {
		$container.addClass("closed");
		setTimeout(function(){$(window).resize();}, 350);
	},
	
    onDomReady: function(containerId) {
        var $container = $("#" + containerId);
        
        // Wide pinned layouts start open; overlay layouts stay closed until opened
        if (!onedev.server.sideInfo.isOverlayMode($container))
        	$container.removeClass("closed");
        
		$(document).on("mouseup touchstart", function(e) {
			if (!onedev.server.sideInfo.isOverlayMode($container))
				return;
			if ($(".flatpickr-calendar.open").length == 0 
					&& $(".pcr-app.visible").length == 0 
					&& $container.find(".dropdown-open").length == 0
					&& $(".select2-drop:visible").length == 0) {
				var x = e.pageX;
				var y = e.pageY;
				
				var contains = $container.offset().left<x && $container.offset().left+$container.outerWidth()>x
						&& $container.offset().top<y && $container.offset().top+$container.outerHeight()>y;
						
			    if (!$container.is(e.target) && $container.has(e.target).length === 0 && !contains) {
                    onedev.server.sideInfo.close($container);
			    }
			}            
        });
        $(document).on("keydown", function(e) {
            if (e.keyCode == 27 && $(e.target).closest(".flatpickr-calendar").length == 0 
                    && $container.find(".dropdown-open").length == 0
                    && $(".select2-drop:visible").length == 0 
                    && $(".flatpickr-calendar.open").length == 0
                    && $(".pcr-app.visible").length == 0) {
                onedev.server.sideInfo.close($container);
            }
        });
    }
}
