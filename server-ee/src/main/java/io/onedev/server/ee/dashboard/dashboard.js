onedev.server.dashboard = {
	xCellCount: 0,
	cellMargin: 6,
	cellHeight: 64,
	getCellWidth: function() {
		var xCellCount = onedev.server.dashboard.xCellCount;
		var cellMargin = onedev.server.dashboard.cellMargin;
		return ($(".dashboard>.body>.content>.grid").width() - (xCellCount+1)*cellMargin) / xCellCount;
	},
	isEditMode() {
		return $(".dashboard").hasClass("dashboard-editor");	
	},
	placeWidgets: function() {
		/* 
		 * Reposition all widgets as long as there are something changed as add/move/resize of one widget  
	     * may affect other widgets, for instance, it may cause scrollbar to appear
         */  
		var cellMargin = onedev.server.dashboard.cellMargin;
		var cellHeight = onedev.server.dashboard.cellHeight;
		var cellWidth = onedev.server.dashboard.getCellWidth();

		var $body = $(".dashboard>.body");
		var $content = $body.children(".content");
		
		function isVerticalIntersect($widget1, $widget2) {
			return $widget1[0] != $widget2[0] 
				&& $widget1.data("right") > $widget2.data("left") 
				&& $widget1.data("left") < $widget2.data("right")
		}
		$content.children(".widget").sort(function (a, b) {
			return +a.dataset.top - +b.dataset.top;
		}).each(function() {
			var $widget = $(this);
			
			var left = $widget.data("left") * (cellWidth + cellMargin) + cellMargin;
			var right = $widget.data("right") * (cellWidth + cellMargin);
			$widget.outerWidth(right - left);
			
			var top = $widget.data("top") * (cellHeight + cellMargin) + cellMargin;
			var bottom = $widget.data("bottom") * (cellHeight + cellMargin);
			var height = bottom - top;
			
			if (onedev.server.dashboard.isEditMode()) {
				$widget.outerHeight(height);
			} else {
				top = 0;
				$content.children(".widget").each(function() {
					var $this = $(this);
					if (isVerticalIntersect($this, $widget) && $this.data("bottom") < $widget.data("bottom")) 
						top = Math.max(top, $this.position().top + $this.outerHeight() + cellMargin);
				});

				if ($widget.data("autoHeight")) 
					$widget.css({height: "auto"});
				else 
					$widget.outerHeight(height);
			}
			
			$widget.css({
				left: left,
				top: top,
				display: "flex"
			});
		});

		if (!onedev.server.dashboard.isEditMode()) {
			var marginTop = $content.css("margin-top");
			marginTop = parseInt(marginTop.substring(0, marginTop.length-2));
			var marginBottom = $content.css("margin-bottom");
			marginBottom = parseInt(marginBottom.substring(0, marginBottom.length-2));
			var screenBottom = $body.height() - marginTop - marginBottom;
			var bottomMost = Math.max(onedev.server.dashboard.getBottomMost(), screenBottom);

			$content.children(".widget").each(function() {
				var $widget = $(this);
				if ($widget.data("autoHeight")) {
					var bottom = bottomMost;
					$content.children(".widget").each(function() {
						var $this = $(this);
						if (isVerticalIntersect($this, $widget) && $this.data("top") > $widget.data("top"))
							bottom = Math.min(bottom, $this.position().top - cellMargin);
					});
					$widget.outerHeight(bottom - $widget.position().top);
				}
			});
		}
	},
	drawAlignGrid: function() {
		var $grid = $(".dashboard>.body>.content>.grid");

		var xCellCount = onedev.server.dashboard.xCellCount;		
		var cellMargin = onedev.server.dashboard.cellMargin;
		var cellHeight = onedev.server.dashboard.cellHeight;
		var cellWidth = onedev.server.dashboard.getCellWidth();
		
		var paper = Snap($grid[0]);
		
		$grid.empty();
		
		for (var i=0; i<=xCellCount; i++) {
			var left = (cellWidth + cellMargin) * i + cellMargin/2;
			var line = paper.line(left, 0, left, $grid.height());
			line.attr({
				stroke: onedev.server.isDarkMode()? "#1e1e2d": "white",
				strokeWidth: cellMargin
			});
		}
		
		var top = cellMargin/2;
		while (top < $grid.height() - cellMargin/2) {
			var line = paper.line(0, top, $grid.width(), top);
			line.attr({
				stroke: onedev.server.isDarkMode()? "#1e1e2d": "white",
				strokeWidth: cellMargin
			});
			top += cellHeight + cellMargin;
		}
	},
	getBottomMost: function() {
		var bottomMost = 0;
		$(".dashboard>.body>.content>.widget").each(function() {
			var $widget = $(this);
			var bottom = $widget.position().top + $widget.outerHeight();
			if (bottom > bottomMost)
				bottomMost = bottom;
		});
		return bottomMost;
	},
	adjustGridHeight: function() {
		var gridHeight = onedev.server.dashboard.getBottomMost();
		if (onedev.server.dashboard.isEditMode())
			gridHeight += 8 * (onedev.server.dashboard.cellHeight + onedev.server.dashboard.cellMargin);
		
		var $grid = $(".dashboard>.body>.content>.grid");
		if ($grid.height() != gridHeight) {
			$grid.height(gridHeight);
			return true;
		} else {
			return false;
		}
	},
	onLoad: function(xCellCount) {
		onedev.server.dashboard.xCellCount = xCellCount;
		var $dashboard = $(".dashboard");
		var $body = $dashboard.children(".body");
		var $content = $body.children(".content");
		
		if (onedev.server.dashboard.isEditMode()) 
			onedev.server.dashboard.drawAlignGrid();
		onedev.server.dashboard.placeWidgets();
		onedev.server.dashboard.adjustGridHeight();
		
		$content.on("resized", function() {
			setTimeout(function() {
				if (onedev.server.dashboard.isEditMode()) 
					onedev.server.dashboard.drawAlignGrid();			
				onedev.server.dashboard.placeWidgets();
				onedev.server.dashboard.adjustGridHeight();
			});
		});
	},
	onWidgetDomReady: function(widgetId, left, top, right, bottom, autoHeight, callback) {
		var $widget = $("#" + widgetId);
		$widget.data("left", left).data("top", top).data("right", right).data("bottom", bottom).data("autoHeight", autoHeight).data("callback", callback);
		if (callback) {
			var $dashboard = $(".dashboard");
			var $body = $dashboard.children(".body");
			var $content = $body.children(".content");
			var initialLeft, initialTop, initialWidth, initialHeight;
			$widget.resizable({
				handles: "n, e, s, w, ne, se, sw, nw",
				minWidth: 100, 
				minHeight: 80,
				start: function() {
					initialLeft = $widget.offset().left - $content.offset().left;
					initialTop = $widget.offset().top - $content.offset().top;
					initialWidth = $widget.outerWidth();
					initialHeight = $widget.outerHeight();
				},
				stop: function() {
					var left = $widget.offset().left - $content.offset().left; 
					var top = $widget.offset().top - $content.offset().top;
					var coordination = onedev.server.dashboard.getCoordination($widget, {
						left: left, 
						top: top,
						right: left + $widget.outerWidth(),
						bottom: top + $widget.outerHeight()
					}, false);
					if (coordination) {
						$widget.data("left", coordination.left).data("top", coordination.top)
								.data("right", coordination.right).data("bottom", coordination.bottom);
						if (onedev.server.dashboard.adjustGridHeight())
							onedev.server.dashboard.drawAlignGrid();
						onedev.server.dashboard.placeWidgets();
						$widget.find(".resize-aware").trigger("resized");
						$widget.data("callback")(coordination.left, coordination.top, coordination.right, coordination.bottom);
						onedev.server.form.markDirty($content.closest(".body").prev().find("form"));
					} else {
						$widget.addClass("ui-resizable-resizing");
						$widget.animate({
							left: initialLeft + "px",
							top: initialTop + "px",
							width: initialWidth + "px", 
							height: initialHeight + "px"
						}, 250, "swing", function() {
							$widget.removeClass("ui-resizable-resizing");
						});
					}	
				}
			});

			$widget.draggable({
				appendTo: $content,
				scroll: false,
				start: function(event, ui) {
					$widget.outerWidth($widget.outerWidth());
					$widget.outerHeight($widget.outerHeight());
				},
				drag: function(event, ui) {
					var widgetTop = ui.position.top + $content.offset().top;
					var bodyTop = $body.offset().top;

					var widgetBottom = widgetTop + $widget.outerHeight();
					var bodyBottom = bodyTop + $body.outerHeight();

					var originalWidgetTop = ui.originalPosition.top + $content.offset().top;
					var scrollThreshold = 10;
					if (widgetTop < bodyTop && widgetTop < originalWidgetTop - scrollThreshold) {
						var scrollChange = Math.min(originalWidgetTop - widgetTop, bodyTop - widgetTop);
						$body.scrollTop($body.scrollTop() - scrollChange);
					}
					if (widgetBottom > bodyBottom && widgetTop > originalWidgetTop + scrollThreshold) {
						var scrollChange = Math.min(widgetTop - originalWidgetTop, widgetBottom - bodyBottom);
						$body.scrollTop($body.scrollTop() + scrollChange);
					}
				},
				stop: function(event, ui) {
					var left = ui.position.left;
					var top = ui.position.top;

					var rect = {
						left: left,
						top: top,
						right: left + $widget.outerWidth(),
						bottom: top + $widget.outerHeight()
					}

					var coordination = onedev.server.dashboard.getCoordination($widget, rect, true);
					if (coordination) {
						$widget.data("left", coordination.left).data("top", coordination.top)
							.data("right", coordination.right).data("bottom", coordination.bottom);
						if (onedev.server.dashboard.adjustGridHeight())
							onedev.server.dashboard.drawAlignGrid();
						onedev.server.dashboard.placeWidgets();

						$widget.data("callback")(coordination.left, coordination.top, coordination.right, coordination.bottom);
						var $content = $(".dashboard>.body>.content");
						onedev.server.form.markDirty($content.closest(".body").prev().find("form"));
					} else {
						$widget.animate({
							left: ui.originalPosition.left + "px",
							top: ui.originalPosition.top + "px"
						}, 250);
					}
				}
			});
			
			$widget.on("resize", function(e) {
				e.stopPropagation();
			});
		}
	},
	getCellSpan: function(position, cellSize) {
		var cellMargin = onedev.server.dashboard.cellMargin;
		var count = Math.floor(position / (cellSize + cellMargin));
		if (Math.abs(position - count * (cellSize + cellMargin)) > Math.abs(position - (count + 1) * (cellSize + cellMargin))) 
			return count + 1;
		else
			return count;
	},
	onWidgetAdded: function(widgetId) {
		var $widget = $("#" + widgetId);
		if (onedev.server.dashboard.adjustGridHeight())
			onedev.server.dashboard.drawAlignGrid();
		onedev.server.dashboard.placeWidgets();
		$widget[0].scrollIntoViewIfNeeded(false); 
		$widget.effect("bounce", {distance: 10});
		$(window).resize();
	},
	isRectIntersected: function(rect1, rect2) {
		return !(rect2.left >= rect1.right || rect2.right <= rect1.left || rect2.top >= rect1.bottom || rect2.bottom <= rect1.top);
	},
	getCoordination: function($widget, rect, move) {
		var $content = $(".dashboard>.body>.content");
		
		var left = rect.left;
		var top = rect.top;
		
		var right = rect.right; 
		var bottom = rect.bottom;

		var maxWidth = $content.outerWidth();
		var maxHeight = $content.outerHeight();

		if (left < 0) {
			if (move)
				right -= left;
			left = 0;
		} 			
		if (top < 0) { 
			if (move)
				bottom -= top;
			top = 0;
		}
		if (right > maxWidth) {
			if (move)
				left -= right - maxWidth;
			right = maxWidth;
		}
		if (bottom > maxHeight) {
			if (move)
				top -= bottom - maxHeight;
			bottom = maxHeight;
		}

		left = onedev.server.dashboard.getCellSpan(left, onedev.server.dashboard.getCellWidth());
		top = onedev.server.dashboard.getCellSpan(top, onedev.server.dashboard.cellHeight);
		if (move) {
			right = left + $widget.data("right") - $widget.data("left");
			bottom = top + $widget.data("bottom") - $widget.data("top");
		} else {
			right = onedev.server.dashboard.getCellSpan(right, onedev.server.dashboard.getCellWidth());
			bottom = onedev.server.dashboard.getCellSpan(bottom, onedev.server.dashboard.cellHeight);
		}

		if ($widget.data("left") != left || $widget.data("top") != top 
				|| $widget.data("right") != right || $widget.data("bottom") != bottom)  {
			
			var hasIntersection = false;
			$(".dashboard>.body>.content>.widget").each(function() {
				if (this != $widget[0]) {
					var thisRect = {
						left: $(this).data("left"),
						top: $(this).data("top"),
						right: $(this).data("right"),
						bottom: $(this).data("bottom")
					};
					var widgetRect = {
						left: left,
						top: top,
						right: right,
						bottom: bottom
					} 
					if (onedev.server.dashboard.isRectIntersected(thisRect, widgetRect)) {
						hasIntersection = true;
						return false;
					}
				}
			});

			if (!hasIntersection) {
				return {
					left: left,
					top: top,
					right: right,
					bottom: bottom
				}
			}
		}
	}
}