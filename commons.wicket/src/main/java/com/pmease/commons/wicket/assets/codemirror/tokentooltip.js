(function(mod) {
	if (typeof exports == "object" && typeof module == "object") // CommonJS
		mod(require("../../lib/codemirror"));
	else if (typeof define == "function" && define.amd) // AMD
		define(["../../lib/codemirror"], mod);
	else // Plain browser env
		mod(CodeMirror);
})(function(CodeMirror) {
	"use strict";

	var DEFAULT_TOOLTIP_CLASS = "CodeMirror-tokentooltip";
	var DEFAULT_DELAY = 250;

	function TokenHoverState(cm, options) {
		this.options = options;
		if (!this.options.delay)
			this.options.delay = DEFAULT_DELAY;
		if (!this.options.tooltipClass)
			this.options.tooltipClass = DEFAULT_TOOLTIP_CLASS;
		this.onMouseOver = function(e) {onMouseOver(cm, e);};	
	}

	function prepareToHide(state) {
		if (state.tooltip) {
			if (state.tooltip.hideTimeout) 
				clearTimeout(state.tooltip.hideTimeout);
			state.tooltip.hideTimeout = setTimeout(function(){
				if (state.tooltip) {
					$(state.tooltip).remove();
					state.tooltip = null;
				}
			}, state.options.delay);
		}
	}
	
	function cancelHide(state) {
		if (state.tooltip && state.tooltip.hideTimeout) {
			clearTimeout(state.tooltip.hideTimeout);
			state.tooltip.hideTimeout = null;				
		} 
	}
	
	function cancelShow(state) {
		if (state.showTimeout) {
			clearTimeout(state.showTimeout);
			state.showTimeout = null;
		}
	}
	
	function isMouseNearbyCursor(e) {
		var $cursor = $(".CodeMirror-cursor");
		if ($cursor.length != 0) {
			var offset = $cursor.offset();
			var left = offset.left, top = offset.top;
			var width = $cursor.outerWidth(), height = $cursor.outerHeight();
			var tolerance = 2;
			return e.clientX>=left-tolerance && e.clientX<=left+width+tolerance 
					&& e.clientY>=top-tolerance && e.clientY<=top+height+tolerance;
		} else {
			return false;
		}
	}
	
	function onMouseOver(cm, e) {
		var state = cm.state.tokenHover;
		var node = e.target || e.srcElement, $node = $(node);
		if ($node.hasClass("cm-property") || $node.hasClass("cm-variable") 
				|| $node.hasClass("cm-variable-2") || $node.hasClass("cm-variable-3")
				|| $node.hasClass("cm-def")) {
			if (!isMouseNearbyCursor(e) && !state.showTimeout) {
				state.onMouseOutOrClick = function(e) {
					prepareToHide(state);
					cancelShow(state);
					$node.off("mouseout click", state.onMouseOutOrClick);
					$node.off("mousemove", state.onMouseMove);
				};
				$node.on("mouseout click", state.onMouseOutOrClick);
				
				state.onMouseMove = function() {
					if (state.tooltip && state.tooltip.node == node)
						cancelHide(state);
				};
				$node.on("mousemove", state.onMouseMove);
				
				state.showTimeout = setTimeout(function() {
					if (!state.tooltip) {
						state.tooltip = state.options.getTooltip(node);
						state.tooltip.node = node;
						
						var $tooltip = $(state.tooltip);
						$tooltip.addClass(state.options.tooltipClass);
						document.body.appendChild(state.tooltip);
						state.tooltip.alignment = {x: 0, y:0, offset:2, showIndicator: false, target: {element: node, x: 0, y: 100}};
						$tooltip.align();
						
						$tooltip.mouseover(function() {
							cancelHide(state);
						});
						$tooltip.mouseout(function(event) {
							if (event.pageX<$tooltip.offset().left+5 || event.pageX>$tooltip.offset().left+$tooltip.width()-5 
									|| event.pageY<$tooltip.offset().top+5 || event.pageY>$tooltip.offset().top+$tooltip.height()-5) {
								prepareToHide(state);
							}
						});
						cancelHide(state);
					}
					state.showTimeout = null;
					
				}, state.options.delay);
			}
		}
	}
	
	CodeMirror.defineOption("tokenHover", false, function(cm, val, old) {
		if (old && old != CodeMirror.Init) {
			CodeMirror.off(cm.getWrapperElement(), "mouseover", cm.state.tokenHover.onMouseOver);
			delete cm.state.tokenHover;
		}

		if (val) {
			var state = cm.state.tokenHover = new TokenHoverState(cm, val);
			CodeMirror.on(cm.getWrapperElement(), "mouseover", state.onMouseOver);
		}
	});
	
});