onedev.server.symboltooltip = {
	init: function(containerId, queryCallback, ajaxIndicatorUrl) {
		var container = document.getElementById(containerId);
		
		var showTimer;

		var cancelShow = function() {
			if (showTimer) {
				clearTimeout(showTimer);
				showTimer = null;
			}
		};
		
		var prepareToHide = function() {
			if (container.tooltip) {
				if (container.tooltip.hideTimer) 
					clearTimeout(container.tooltip.hideTimer);
				container.tooltip.hideTimer = setTimeout(function(){
					onedev.server.symboltooltip.removeTooltip(container);
				}, 200);
			}
		};
		
		var cancelHide = function() {
			if (container.tooltip && container.tooltip.hideTimer) {
				clearTimeout(container.tooltip.hideTimer);
				container.tooltip.hideTimer = null;				
			} 
		};
		
		container.onMouseOverSymbol = function(revision, symbolEl) {
			var $symbol = $(symbolEl);
			if (onedev.server.mouseState.pressed || !onedev.server.mouseState.moved || $symbol.text() == "@")
				return;
			
			cancelShow();
			cancelHide();

			var onSymbolMouseOutOrUpOrDown = function(e) {
				prepareToHide();
				cancelShow();
				$symbol.off("mouseout mousedown mouseup", onSymbolMouseOutOrUpOrDown);
			};
			$symbol.on("mouseout mousedown mouseup", onSymbolMouseOutOrUpOrDown);
			
			showTimer = setTimeout(function() {
				if (!$symbol.is(":visible")) 
					return;
				if (container.tooltip) {
					if (container.tooltip.symbolEl == symbolEl)
						return;
					else 
						onedev.server.symboltooltip.removeTooltip(container);
				}

				var $tooltip = $("<div class='symbol-tooltip overflow-auto' id='" + containerId 
						+ "-symbol-tooltip'><img src=" + ajaxIndicatorUrl + "></img></div>");
				container.tooltip = $tooltip[0];
				container.tooltip.symbolEl = symbolEl;
				document.body.appendChild(container.tooltip);
				
				$tooltip.mouseover(function() {
					cancelHide();
				});
				$tooltip.mouseout(function(event) {
					if (event.pageX<$tooltip.offset().left+5 || event.pageX>$tooltip.offset().left+$tooltip.width()-5 
							|| event.pageY<$tooltip.offset().top+5 || event.pageY>$tooltip.offset().top+$tooltip.height()-5) {
						prepareToHide();
					}
				});

				$tooltip.data("alignment", {placement: {x: 0, y:0, offset:2, targetX: 0, targetY: 100}, target: {element: symbolEl}});
				$tooltip.align($tooltip.data("alignment"));

				queryCallback(revision, $symbol.text());
				
				showTimer = null;
			}, 500);
		};
	},
	doneQuery: function(contentId) {
		var $content = $("#" + contentId);
		var $container = $content.parent();
		var $tooltip = $("#" + $container.attr("id") + "-symbol-tooltip");
		if ($tooltip.length != 0) 
			$tooltip.html($content.children()).align($tooltip.data("alignment"));
	},
	
	// this is public API which can be called from other components using this component
	removeTooltip: function(container) {
		if (container.tooltip) {
			$(container.tooltip).remove();
			container.tooltip = null;
		}
	}
};

$(function() {
	$(window).scroll(function() {
		$(".symbol-tooltip-container").each(function() {
			onedev.server.symboltooltip.removeTooltip(this);
		});
	});
});