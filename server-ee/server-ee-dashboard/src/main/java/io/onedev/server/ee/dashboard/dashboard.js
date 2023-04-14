onedev.server.dashboard = {
	xCellCount: 0,
	cellMargin: 6,
	cellHeight: 28,
	getCellWidth: function() {
		var xCellCount = onedev.server.dashboard.xCellCount;
		var cellMargin = onedev.server.dashboard.cellMargin;
		return ($(".dashboard>.body>.content>.grid").width() - (xCellCount+1)*cellMargin) / xCellCount;
	},
	placeWidgets: function() {
		/* 
		 * Reposition all widgets as long as there are something changed as add/move/resize of one widget  
	     * may affect other widgets, for instance, it may cause scrollbar to appear
         */  
		var cellMargin = onedev.server.dashboard.cellMargin;
		var cellHeight = onedev.server.dashboard.cellHeight;
		var cellWidth = onedev.server.dashboard.getCellWidth();
		var bottomMost = onedev.server.dashboard.getBottomMost() * (cellHeight + cellMargin);
		
		$(".dashboard>.body>.content>.widget").each(function() {
			var $widget = $(this);
			var left = $widget.data("left") * (cellWidth + cellMargin) + cellMargin; 
			var top = $widget.data("top") * (cellHeight + cellMargin) + cellMargin; 
			var right = $widget.data("right") * (cellWidth + cellMargin); 
			var bottom = $widget.data("bottom") * (cellHeight + cellMargin);
			
			$widget.outerWidth(right - left);
			var height = bottom - top;
			if ($(".dashboard").hasClass("dashboard-editor")) {
				$widget.outerHeight(height);
			} else {
				var hasBeneathWidgets = false;
				var $body = $(".dashboard>.body");
				var $content = $body.children(".content");
				$content.children(".widget").each(function() {
					if (this != $widget[0] 
							&& $(this).data("bottom") > $widget.data("bottom") 
							&& $(this).data("right") > $widget.data("left") 
							&& $(this).data("left") < $widget.data("right")) {
						hasBeneathWidgets = true;
						return false;
					}
				});
				
				if (!hasBeneathWidgets) {
					var marginTop = $content.css("margin-top");
					marginTop = parseInt(marginTop.substring(0, marginTop.length-2));
					var marginBottom = $content.css("margin-bottom");
					marginBottom = parseInt(marginBottom.substring(0, marginBottom.length-2));
					var screenBottom = $body.height() - marginTop - marginBottom;
					if (bottomMost > screenBottom)
						$widget.outerHeight(bottomMost - top);
					else
						$widget.outerHeight(screenBottom - top);
				} else {
					$widget.outerHeight(height);
				}
			}
	
			$widget.css({
				left: left,
				top: top
			}).show();
		});		
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
			if ($(this).data("bottom") > bottomMost)
				bottomMost = $(this).data("bottom");
		});
		return bottomMost;		
	},
	adjustGridHeight: function() {
		var $grid = $(".dashboard>.body>.content>.grid");
		
		var bottomMost = onedev.server.dashboard.getBottomMost();
		
		if ($(".dashboard").hasClass("dashboard-editor"))
			bottomMost += 8;
		
		var expectedHeight = bottomMost * (onedev.server.dashboard.cellHeight + onedev.server.dashboard.cellMargin);
		if ($grid.height() != expectedHeight) {
			$grid.height(expectedHeight);
			return true;
		} else {
			return false;
		}
	},
	onLoad: function(xCellCount) {
		onedev.server.dashboard.xCellCount = xCellCount;

		onedev.server.dashboard.adjustGridHeight();
		
		var editMode = $(".dashboard").hasClass("dashboard-editor");
		if (editMode) 		
			onedev.server.dashboard.drawAlignGrid();
		
		onedev.server.dashboard.placeWidgets();
		
		$(".dashboard>.body>.content").on("resized", function() {
			setTimeout(function() {
				if (editMode) 
					onedev.server.dashboard.drawAlignGrid();			
				onedev.server.dashboard.placeWidgets();
			});
		});
	},
	onWidgetDomReady: function(widgetId, left, top, right, bottom, callback) {
		var $widget = $("#" + widgetId);
		$widget.data("left", left).data("top", top).data("right", right).data("bottom", bottom).data("callback", callback);
		if (callback) {
			var initialWidth, initialHeight;
			$widget.resizable({
				minWidth: 100, 
				minHeight: 80,
				start: function() {
					initialWidth = $widget.outerWidth();
					initialHeight = $widget.outerHeight();
				},
				stop: function() {
					var $content = $(".dashboard>.body>.content");
					var $grid = $content.children(".grid");
					var left = $widget.offset().left - $grid.offset().left; 
					var top = $widget.offset().top - $grid.offset().top;
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
						$widget.effect("size", {
							to: {
								width: initialWidth,
								height: initialHeight
							}
						}, 250, function() {
							$widget.removeClass("ui-resizable-resizing");
						});
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
		
		if (left < 0) {
			right -= left;
			left = 0;
		} 
			
		if (top < 0) { 
			bottom -= top;
			top = 0;
		} 
			
		var maxWidth = $content.outerWidth();
		var maxHeight = $content.outerHeight();
		
		if (right > maxWidth) 
			right = maxWidth;
		if (bottom > maxHeight) 
			bottom = maxHeight;

		right = onedev.server.dashboard.getCellSpan(right, onedev.server.dashboard.getCellWidth());				
		bottom = onedev.server.dashboard.getCellSpan(bottom, onedev.server.dashboard.cellHeight);

		if (move) {		
			left = right - $widget.data("right") + $widget.data("left");
			top = bottom - $widget.data("bottom") + $widget.data("top");
		} else {
			left = $widget.data("left");
			top = $widget.data("top");
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
	},
	getMoveRect: function(event) {
		var widgetData = onedev.server.dashboard.draggingWidgetData;
		var $widget = $("#" + widgetData.id);
		var $grid = $(".dashboard>.body>.content>.grid");
		
		var left = event.pageX - $grid.offset().left - widgetData.offsetX;
		var top = event.pageY - $grid.offset().top - widgetData.offsetY;

		return {
			left: left,
			top: top,
			right: left + $widget.outerWidth(),
			bottom: top + $widget.outerHeight()
		}		
	},
	onDragStart: function(event) {
		event.dataTransfer.setData("widget", "");
		onedev.server.dashboard.draggingWidgetData = {
			id: event.target.id,
			offsetX: event.offsetX,
			offsetY: event.offsetY
		};
		event.dataTransfer.effectAllowed = "move";
	},
	onDragOver: function(event) {
		if (event.dataTransfer.types.includes("widget")) {
			event.dataTransfer.dropEffect = "move";
			
			var widgetData = onedev.server.dashboard.draggingWidgetData;
			var $widget = $("#" + widgetData.id);
			
			if (onedev.server.dashboard.getCoordination($widget, onedev.server.dashboard.getMoveRect(event), true))
				event.preventDefault();
		}
	},
	onDrop: function(event) {
		if (event.dataTransfer.types.includes("widget")) {
			var $widget = $("#" + onedev.server.dashboard.draggingWidgetData.id);
			var coordination = onedev.server.dashboard.getCoordination($widget, onedev.server.dashboard.getMoveRect(event), true);
			if (coordination) {
				$widget.data("left", coordination.left).data("top", coordination.top)
						.data("right", coordination.right).data("bottom", coordination.bottom);
				if (onedev.server.dashboard.adjustGridHeight())
					onedev.server.dashboard.drawAlignGrid();
				onedev.server.dashboard.placeWidgets();
				
				$widget.data("callback")(coordination.left, coordination.top, coordination.right, coordination.bottom);
				var $content = $(".dashboard>.body>.content");
				onedev.server.form.markDirty($content.closest(".body").prev().find("form"));
				
				event.preventDefault();
			}			
		}
	}
}