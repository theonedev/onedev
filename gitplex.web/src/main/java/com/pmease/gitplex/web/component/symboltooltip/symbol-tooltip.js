gitplex.symboltooltip = {
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
					gitplex.symboltooltip.removeTooltip(container);
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
			if (gitplex.mouseState.pressed || !gitplex.mouseState.moved)
				return;
			
			cancelShow();
			cancelHide();

			var $symbol = $(symbolEl);
			var onSymbolMouseOutOrUpOrDown = function(e) {
				prepareToHide();
				cancelShow();
				$symbol.off("mouseout mousedown mouseup", onSymbolMouseOutOrUpOrDown);
			};
			$symbol.on("mouseout mousedown mouseup", onSymbolMouseOutOrUpOrDown);
			
			if (container.tooltip) {
				if (container.tooltip.symbolEl == symbolEl)
					return;
				else 
					gitplex.symboltooltip.removeTooltip(container);
			}

			showTimer = setTimeout(function() {
				if (container.tooltip || !$symbol.is(":visible")) 
					return;
				
				var $tooltip = $("<div class='symbol-tooltip'><img src=" + ajaxIndicatorUrl + "></img></div>");
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
		var $tooltip = $(".symbol-tooltip");
		$tooltip.empty().append($("#" + contentId).children());
		$tooltip.align($tooltip.data("alignment"));
	},
	removeTooltip: function(container) {
		if (container.tooltip) {
			$(container.tooltip).remove();
			container.tooltip = null;
		}
	}
}