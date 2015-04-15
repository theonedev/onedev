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
	var DEFAULT_HOVER_CLASS = "CodeMirror-tokenhover";
	var DEFAULT_DELAY = 500;

	function TokenHoverState(cm, options) {
		this.options = options;
		if (!this.options.delay)
			this.options.delay = DEFAULT_DELAY;
		if (!this.options.tooltipClass)
			this.options.tooltipClass = DEFAULT_TOOLTIP_CLASS;
		if (!this.options.hoverClass)
			this.options.hoverClass = DEFAULT_HOVER_CLASS;
		this.onMouseOver = function(e) {onMouseOver(cm, e);};	
		
		var self = this;
		self.mousePressed = 0;
		this.onMouseMove = function(e) {self.mouseMoved = true;};
		this.onMouseDown = function(e) {self.mousePressed++; self.mouseMoved=false;};
		this.onMouseUp = function(e) {self.mousePressed--; self.mouseMoved=false;};
		this.onScroll = function(e) {self.mouseMoved = false;};
	}

	function prepareToHide(state) {
		if (state.tooltip) {
			if (state.tooltip.hideTimeout) 
				clearTimeout(state.tooltip.hideTimeout);
			state.tooltip.hideTimeout = setTimeout(function(){
				if (state.tooltip) {
					$(state.tooltip).remove();
					$(state.tooltip.node).removeClass(state.options.hoverClass);
					state.tooltip = null;
				}
			}, 200);
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
	
	function onMouseOver(cm, e) {
		var state = cm.state.tokenHover;
		var node = e.target || e.srcElement, $node = $(node);
		if ($node.hasClass("cm-property") || $node.hasClass("cm-variable") || $node.hasClass("cm-variable-2") 
				|| $node.hasClass("cm-variable-3") || $node.hasClass("cm-def")) {
			// we do not want to show token tooltip too frequently to trouble the user, so only display 
			// it if you moves the mouse intentionally over to the element without pressing the mouse
			// button
			if (!state.mousePressed && state.mouseMoved && !state.showTimeout) {
				state.onNodeMouseOutOrUpOrDown = function(e) {
					prepareToHide(state);
					cancelShow(state);
					$node.off("mouseout mousedown mouseup", state.onNodeMouseOutOrUpOrDown);
					$node.off("mousemove", state.onNodeMouseMove);
				};
				$node.on("mouseout mousedown mouseup", state.onNodeMouseOutOrUpOrDown);
				
				state.onNodeMouseMove = function() {
					if (state.tooltip && state.tooltip.node == node)
						cancelHide(state);
				};
				$node.on("mousemove", state.onNodeMouseMove);
				
				state.showTimeout = setTimeout(function() {
					// node is not visible for some reason when we triple click to select a line, in this case
					// we should not display the tooltip as we can not align to an invisible node
					if (!state.tooltip && $node.is(":visible")) {  
						state.tooltip = state.options.getTooltip(node);
						state.tooltip.node = node;
						$node.addClass(state.options.hoverClass);
						
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
			CodeMirror.off(cm.getWrapperElement(), "mousemove", cm.state.tokenHover.onMouseMove);
			CodeMirror.off(cm.getWrapperElement(), "mousedown", cm.state.tokenHover.onMouseDown);
			CodeMirror.off(cm.getWrapperElement(), "mouseup", cm.state.tokenHover.onMouseUp);
			cm.off("scroll", state.onScroll);
			delete cm.state.tokenHover;
		}

		if (val) {
			var state = cm.state.tokenHover = new TokenHoverState(cm, val);
			CodeMirror.on(cm.getWrapperElement(), "mouseover", state.onMouseOver);
			CodeMirror.on(cm.getWrapperElement(), "mousemove", state.onMouseMove);
			CodeMirror.on(cm.getWrapperElement(), "mousedown", state.onMouseDown);
			CodeMirror.on(cm.getWrapperElement(), "mouseup", state.onMouseUp);
			cm.on("scroll", state.onScroll);
		}
	});
	
	CodeMirror.defineExtension("hideTokenHover", function() {
		prepareToHide(this.state.tokenHover);
	});
	
});