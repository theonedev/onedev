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

		var marginTop = $content.css("margin-top");
		marginTop = parseInt(marginTop.substring(0, marginTop.length-2));
		var marginBottom = $content.css("margin-bottom");
		marginBottom = parseInt(marginBottom.substring(0, marginBottom.length-2));

		function isVerticalIntersect($widget1, $widget2) {
			return $widget1[0] != $widget2[0] 
				&& $widget1.data("right") > $widget2.data("left") 
				&& $widget1.data("left") < $widget2.data("right")
		}
		$content.children(".widget").sort(function (a, b) {
			return $(a).data("top") - $(b).data("top");
		}).each(function() {
			var $widget = $(this);
			
			var left = onedev.server.dashboard.getPositionScalar($widget.data("left"), cellWidth);
			var right = onedev.server.dashboard.getPositionScalar($widget.data("right"), cellWidth) - cellMargin;
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
		$(".dashboard>.body>.content>.widget, .dashboard>.body>.content>.placeholder").each(function() {
			var $widget = $(this);
			if ($widget.is(":visible") && !$widget.hasClass("ui-resizable-resizing") 
					&& !$widget.hasClass("widget-dragging")) {
				var bottom = $widget.position().top + $widget.outerHeight();
				if (bottom > bottomMost)
					bottomMost = bottom;
			} 
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
	showPlaceholder: function(rect, dimension) {
		var $content = $(".dashboard>.body>.content");
		var $placeholder = $content.children(".placeholder");
		
		var left = rect.left;
		var top = rect.top;

		var right = rect.right;
		var bottom = rect.bottom;

		var maxWidth = $content.outerWidth();
		var maxHeight = $content.outerHeight();

		if (left < 0) {
			if (dimension)
				right -= left;
			left = 0;
		}
		if (top < 0) {
			if (dimension)
				bottom -= top;
			top = 0;
		}
		if (right > maxWidth) {
			if (dimension)
				left -= right - maxWidth;
			right = maxWidth;
		}
		if (bottom > maxHeight) {
			if (dimension)
				top -= bottom - maxHeight;
			bottom = maxHeight;
		}

		left = onedev.server.dashboard.getCoordinationScalar(left, onedev.server.dashboard.getCellWidth());
		top = onedev.server.dashboard.getCoordinationScalar(top, onedev.server.dashboard.cellHeight);

		if (dimension) {
			right = left + dimension.width;
			bottom = top + dimension.height;
		} else {
			right = onedev.server.dashboard.getCoordinationScalar(right, onedev.server.dashboard.getCellWidth());
			bottom = onedev.server.dashboard.getCoordinationScalar(bottom, onedev.server.dashboard.cellHeight);
		}
		
		var cellMargin = onedev.server.dashboard.cellMargin;
		var cellHeight = onedev.server.dashboard.cellHeight;
		var cellWidth = onedev.server.dashboard.getCellWidth();

		$placeholder.data("left", left);
		$placeholder.data("top", top);
		$placeholder.data("right", right);
		$placeholder.data("bottom", bottom);

		left = onedev.server.dashboard.getPositionScalar(left, cellWidth);
		right = onedev.server.dashboard.getPositionScalar(right, cellWidth) - cellMargin;
		$placeholder.outerWidth(right - left);

		top = onedev.server.dashboard.getPositionScalar(top, cellHeight);
		bottom = onedev.server.dashboard.getPositionScalar(bottom, cellHeight) - cellMargin;
		$placeholder.outerHeight(bottom - top);

		$placeholder.css({
			left: left,
			top: top
		}).show();
	},
	adjustWidgets: function() {
		var $content = $(".dashboard>.body>.content");
		var $widgets = $content.children(".widget").not(".widget-dragging").not(".ui-resizable-resizing");
		var $placeholder = $content.children(".placeholder");
		
		$placeholder.data("newLeft", $placeholder.data("left"));
		$placeholder.data("newTop", $placeholder.data("top"));
		$placeholder.data("newRight", $placeholder.data("right"));
		$placeholder.data("newBottom", $placeholder.data("bottom"));
		
		$widgets.each(function() {
			var $this = $(this);
			$this.data("newLeft", $this.data("left"));
			$this.data("newTop", $this.data("top"));
			$this.data("newRight", $this.data("right"));
			$this.data("newBottom", $this.data("bottom"));
		});

		var adjustedWidgets = [$placeholder[0]];
		var widgetsToAdjust = [];
		
		function getOverlappingUnadjustedWidgets(widget) {
			var overlappingWidgets = [];
			$widgets.each(function() {
				var $this = $(this);
				if (!adjustedWidgets.includes(this) && this !== widget) {
					var rectThis = {
						left: $this.data("left"),
						top: $this.data("top"),
						right: $this.data("right"),
						bottom: $this.data("bottom")
					}
					var $widget = $(widget);
					var rectWidget = {
						left: $widget.data("newLeft"),
						top: $widget.data("newTop"),
						right: $widget.data("newRight"),
						bottom: $widget.data("newBottom")
					}
					if (onedev.server.dashboard.isRectIntersected(rectThis, rectWidget))
						overlappingWidgets.push(this);
				}
			});
			return overlappingWidgets;
		}
		
		function getOverlappingAdjustedWidgets(widget) {
			var overlappingWidgets = [];
			adjustedWidgets.forEach(function(item) {
				var $item = $(item);
				var rectItem = {
					left: $item.data("newLeft"),
					top: $item.data("newTop"),
					right: $item.data("newRight"),
					bottom: $item.data("newBottom")
				}
				var $widget = $(widget);
				var rectWidget = {
					left: $widget.data("newLeft"),
					top: $widget.data("newTop"),
					right: $widget.data("newRight"),
					bottom: $widget.data("newBottom")
				}
				if (onedev.server.dashboard.isRectIntersected(rectItem, rectWidget))
					overlappingWidgets.push(item);
			});
			return overlappingWidgets;
		}

		var maxWidth = onedev.server.dashboard.xCellCount;
		function evalWidgetAdjustion(widgetToAdjust, evalResult) {
			var $widgetToAdjust = $(widgetToAdjust);
			if ($widgetToAdjust.data("newLeft") >= 0 
					&& $widgetToAdjust.data("newRight") <= maxWidth
					&& $widgetToAdjust.data("newTop") >= 0
					&& getOverlappingAdjustedWidgets(widgetToAdjust).length === 0) {
				var overlapped = getOverlappingUnadjustedWidgets(widgetToAdjust).length;
				if (evalResult.overlapped === -1 || evalResult.overlapped > overlapped) {
					evalResult.overlapped = overlapped;
					evalResult.coordination = {
						left: $widgetToAdjust.data("newLeft"),
						top: $widgetToAdjust.data("newTop"),
						right: $widgetToAdjust.data("newRight"),
						bottom: $widgetToAdjust.data("newBottom")
					}
				}
			}
		}
		
		[].push.apply(widgetsToAdjust, getOverlappingUnadjustedWidgets($placeholder[0]));
		while ((widgetToAdjust = widgetsToAdjust.shift()) !== undefined) {
			var $widgetToAdjust = $(widgetToAdjust);
			var left = $widgetToAdjust.data("left");
			
			// make sure coordination is valid to avoid possible endless loop below
			if (left < 0)
				left = 0;
			var top = $widgetToAdjust.data("top");
			if (top < 0)
				top = 0;
			var right = $widgetToAdjust.data("right");
			if (right > maxWidth)
				right = maxWidth;
			var bottom = $widgetToAdjust.data("bottom");
			if (left >= right) {
				left = 0;
				right = maxWidth;
			}
			if (top >= bottom) {
				top = 0;
				bottom = 8;
			}
			var moveDistance = 1;
			while (true) {
				var evalResult = {
					overlapped: -1
				};
				$widgetToAdjust.data("newLeft", left - moveDistance);
				$widgetToAdjust.data("newTop", top);
				$widgetToAdjust.data("newRight", right - moveDistance);
				$widgetToAdjust.data("newBottom", bottom);
				evalWidgetAdjustion(widgetToAdjust, evalResult);

				$widgetToAdjust.data("newLeft", left + moveDistance);
				$widgetToAdjust.data("newTop", top);
				$widgetToAdjust.data("newRight", right + moveDistance);
				$widgetToAdjust.data("newBottom", bottom);
				evalWidgetAdjustion(widgetToAdjust, evalResult);

				$widgetToAdjust.data("newLeft", left);
				$widgetToAdjust.data("newTop", top - moveDistance);
				$widgetToAdjust.data("newRight", right);
				$widgetToAdjust.data("newBottom", bottom - moveDistance);
				evalWidgetAdjustion(widgetToAdjust, evalResult);

				$widgetToAdjust.data("newLeft", left);
				$widgetToAdjust.data("newTop", top + moveDistance);
				$widgetToAdjust.data("newRight", right);
				$widgetToAdjust.data("newBottom", bottom + moveDistance);
				evalWidgetAdjustion(widgetToAdjust, evalResult);

				if (evalResult.coordination) {
					$widgetToAdjust.data("newLeft", evalResult.coordination.left);
					$widgetToAdjust.data("newTop", evalResult.coordination.top);
					$widgetToAdjust.data("newRight", evalResult.coordination.right);
					$widgetToAdjust.data("newBottom", evalResult.coordination.bottom);
					break;
				}
				moveDistance ++;
			}
			
			adjustedWidgets.push(widgetToAdjust);
			getOverlappingUnadjustedWidgets(widgetToAdjust).forEach(function(item) {
				if (!widgetsToAdjust.includes(item))
					widgetsToAdjust.push(item);
			});
		}
		
		var cellHeight = onedev.server.dashboard.cellHeight;
		var cellWidth = onedev.server.dashboard.getCellWidth();

		$widgets.each(function() {
			var $widget = $(this);

			var left = onedev.server.dashboard.getPositionScalar($widget.data("newLeft"), cellWidth);
			var top = onedev.server.dashboard.getPositionScalar($widget.data("newTop"), cellHeight);

			$widget.stop(true);
			$widget.animate({
				left: left,
				top: top,
				display: "flex"
			}, 120);
		});

		if (onedev.server.dashboard.adjustGridHeight())
			onedev.server.dashboard.drawAlignGrid();
	},
	dropWidget($widget) {
		var $content = $(".dashboard>.body>.content");
		var $placeholder = $content.children(".placeholder");
		
		$widget.css({
			left: $placeholder.position().left,
			top: $placeholder.position().top
		});
		$widget.outerWidth($placeholder.outerWidth());
		$widget.outerHeight($placeholder.outerHeight());

		$placeholder.hide();
		$widget.removeClass("widget-dragging");
		$widget.data("newLeft", $placeholder.data("left"))
			.data("newTop", $placeholder.data("top"))
			.data("newRight", $placeholder.data("right"))
			.data("newBottom", $placeholder.data("bottom"));

		$content.children(".widget").each(function() {
			var $this = $(this);
			var coordinationChanged = false;
			if ($this.data("left") != $this.data("newLeft")) {
				$this.data("left", $this.data("newLeft"));
				coordinationChanged = true;
			}
			if ($this.data("top") != $this.data("newTop")) {
				$this.data("top", $this.data("newTop"));
				coordinationChanged = true;
			}
			if ($this.data("right") != $this.data("newRight")) {
				$this.data("right", $this.data("newRight"));
				coordinationChanged = true;
			}
			if ($this.data("bottom") != $this.data("newBottom")) {
				$this.data("bottom", $this.data("newBottom"));
				coordinationChanged = true;
			}
			if (coordinationChanged) {
				$this.data("callback")($this.data("left"), $this.data("top"), $this.data("right"), $this.data("bottom"));
				onedev.server.form.markDirty($content.closest(".body").prev().find("form"));
			}
		});
	},
	onWidgetDomReady: function(widgetId, left, top, right, bottom, autoHeight, callback) {
		var $widget = $("#" + widgetId);
		$widget.data("left", left).data("newLeft", left)
			.data("top", top).data("newTop", top)
			.data("right", right).data("newRight", right)
			.data("bottom", bottom).data("newBottom", bottom)
			.data("autoHeight", autoHeight).data("callback", callback);
		if (callback) {
			var $dashboard = $(".dashboard");
			var $body = $dashboard.children(".body");
			var $content = $body.children(".content");
			var $placeholder = $content.children(".placeholder");
			
			var adjustWidgetsTimer = undefined;
			function widgetMovingOrResizing($widget, dimension) {
				if (adjustWidgetsTimer !== undefined)
					clearTimeout(adjustWidgetsTimer);
				adjustWidgetsTimer = setTimeout(function() {
					var left = $widget.offset().left - $content.offset().left;
					var top = $widget.offset().top - $content.offset().top;
					var rect = {
						left: left,
						top: top,
						right: left + $widget.outerWidth(),
						bottom: top + $widget.outerHeight()
					}
					onedev.server.dashboard.showPlaceholder(rect, dimension);
					onedev.server.dashboard.adjustWidgets();
				}, 50);
			}
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
				resize: function() {
					widgetMovingOrResizing($widget);
				},
				stop: function() {
					if (adjustWidgetsTimer !== undefined)
						clearTimeout(adjustWidgetsTimer);
					if ($placeholder.is(":visible")) {
						onedev.server.dashboard.dropWidget($widget);
						$widget.find(".resize-aware").trigger("resized");
					} else {
						var left = $widget.offset().left - $content.offset().left;
						var top = $widget.offset().top - $content.offset().top;
						$widget.addClass("ui-resizable-resizing");
						$widget.animate({
							left: initialLeft + "px",
							top: initialTop + "px",
							width: initialWidth + "px",
							height: initialHeight + "px"
						}, 250, function() {
							$widget.removeClass("ui-resizable-resizing");
						});
					}
				}
			});

			var lastUiHelperTop = undefined;
			var containerContentHeight;
			$widget.draggable({
				helper: "clone",
				appendTo: $body,
				scroll: false,
				start: function(event, ui) {
					$widget.addClass("widget-dragging");
					containerContentHeight = $body.prop("scrollHeight");
				},
				drag: function(event, ui) {
					var $uiHelper = $(ui.helper);
					var uiHelperTop = $uiHelper.offset().top;
					if (lastUiHelperTop === undefined)
						lastUiHelperTop = uiHelperTop;
					
					var containerTop = $body.offset().top;

					var uiHelperBottom = uiHelperTop + $uiHelper.outerHeight();
					var containerBottom = containerTop + $body.outerHeight();

					if (uiHelperTop < containerTop && uiHelperTop < lastUiHelperTop) {
						var scrollChange = Math.min(lastUiHelperTop - uiHelperTop, containerTop - uiHelperTop);
						$body.scrollTop($body.scrollTop() - scrollChange);
					}
					if (uiHelperBottom > containerBottom && uiHelperTop > lastUiHelperTop) {
						var scrollChange = Math.min(uiHelperTop - lastUiHelperTop, uiHelperBottom - containerBottom);
						$body.scrollTop($body.scrollTop() + scrollChange);
					}
					lastUiHelperTop = uiHelperTop;
					
					widgetMovingOrResizing($uiHelper, {
						width: $widget.data("right") - $widget.data("left"),
						height: $widget.data("bottom") - $widget.data("top")
					});
				},
				stop: function(event, ui) {
					lastUiHelperTop = undefined;
					
					if (adjustWidgetsTimer !== undefined)
						clearTimeout(adjustWidgetsTimer);
					if ($placeholder.is(":visible")) {
						onedev.server.dashboard.dropWidget($widget);
					} else {
						var originalPosition = $widget.position();
						$widget.css({
							left: $(ui.helper).offset().left - $content.offset().left, 
							top: $(ui.helper).offset().top - $content.offset().top, 
							"z-index": 10
						});
						$widget.removeClass("widget-dragging");
						$widget.animate({
							left: originalPosition.left,
							top: originalPosition.top
						}, 250, function() {
							$widget.css("z-index", 1);
						});
					}
				}
			});
			
			$widget.on("resize", function(e) {
				e.stopPropagation();
			});
		}
	},
	getCoordinationScalar: function(positionScalar, cellSize) {
		var cellMargin = onedev.server.dashboard.cellMargin;
		var count = Math.floor(positionScalar / (cellSize + cellMargin));
		if (Math.abs(positionScalar - count * (cellSize + cellMargin)) > Math.abs(positionScalar - (count + 1) * (cellSize + cellMargin))) 
			return count + 1;
		else
			return count;
	},
	getPositionScalar: function(coordinationScalar, cellSize) {
		var cellMargin = onedev.server.dashboard.cellMargin;
		return coordinationScalar * (cellSize + cellMargin) + cellMargin;
	},
	onWidgetAdded: function(widgetId) {
		var $widget = $("#" + widgetId);
		onedev.server.dashboard.placeWidgets();
		if (onedev.server.dashboard.adjustGridHeight())
			onedev.server.dashboard.drawAlignGrid();
		$widget[0].scrollIntoViewIfNeeded(false); 
		$widget.effect("bounce", {distance: 10});
		$(window).resize();
	},
	isRectIntersected: function(rect1, rect2) {
		return !(rect2.left >= rect1.right || rect2.right <= rect1.left || rect2.top >= rect1.bottom || rect2.bottom <= rect1.top);
	}
}