onedev.server.sideInfo = {
    onDomReady: function(containerId) {
        var $container = $("#" + containerId);
		$(document).on("mouseup touchstart", function(e) {
			if ($(".flatpickr-calendar.open").length == 0 
					&& $(".pcr-app.visible").length == 0 
					&& $container.find(".dropdown-open").length == 0) {
				var x = e.pageX;
				var y = e.pageY;
				
				var contains = $container.offset().left<x && $container.offset().left+$container.outerWidth()>x
						&& $container.offset().top<y && $container.offset().top+$container.outerHeight()>y;
						
			    if (!$container.is(e.target) && $container.has(e.target).length === 0 && !contains) {
                    $container.addClass("closed");
			    }
			}            
        });
        $(document).on("keydown", function(e) {
            if (e.keyCode == 27 && $(e.target).closest(".flatpickr-calendar").length == 0 
                    && $container.find(".dropdown-open").length == 0
                    && $(".select2-drop:visible").length == 0 
                    && $(".flatpickr-calendar.open").length == 0
                    && $(".pcr-app.visible").length == 0) {
                $container.addClass("closed");
            }
        });
    }
}
