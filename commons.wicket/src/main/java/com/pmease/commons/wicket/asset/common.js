/**************************************************************
 * Define common javascript functions and setups here.
 * 
 **************************************************************/
jQuery.fn.align = function(alignment) {
	if (this[0] == undefined)
		return this;
	
	if (alignment == undefined) 
		alignment = this[0].alignment;
	
	var indicator = this.find(">.indicator");
	
	var borderTop = jQuery(window).scrollTop();
	var borderBottom = borderTop + jQuery(window).height();
	var borderLeft = jQuery(window).scrollLeft();
	var borderRight = borderLeft + jQuery(window).width();

	var width = this.outerWidth();
	var height = this.outerHeight();
	
	var indicatorSize = 6;
	
	var left, top;
	var targetLeft, targetTop;
	var targetWidth, targetHeight;
	var targetX, targetY;

	if (alignment.target.element == undefined) { // align to mouse
		targetLeft = alignment.target.pageX;
		targetTop = alignment.target.pageY;
		targetWidth = 0;
		targetHeight = 0;
		targetX = 0;
		targetY = 0;
	} else {
		var target = $(alignment.target.element);
		targetLeft = target.offset().left;
		targetTop = target.offset().top;
		targetWidth = target.outerWidth();
		targetHeight = target.outerHeight();
		targetX = alignment.target.x;
		targetY = alignment.target.y;
	}		
	
	var anchor = targetWidth*targetX/100.0 + targetLeft;
	left = anchor - alignment.x*width/100.0;
	if (left < borderLeft || left + width > borderRight) {
		// flip horizontally to see if width can fit into screen
		anchor = targetWidth*(100-targetX)/100.0 + targetLeft;
		left = anchor - (100 - alignment.x) * width / 100.0;
		if (left < borderLeft || left + width > borderRight) {
			// does not fit even flipped, so we revert back
			anchor = targetWidth*targetX/100.0 + targetLeft;
			left = anchor - alignment.x*width/100.0;
		}
		if (left + width > borderRight)
			left = borderRight - width;
		if (left < borderLeft)
			left = borderLeft;
	}
	
	anchor = targetHeight * targetY / 100.0 + targetTop;
	top = anchor - alignment.y * height / 100.0;
	if (top < borderTop || top + height > borderBottom) {
		// flip vertically to see if height can fit into screen
		anchor = targetHeight * (100 - targetY) / 100.0 + targetTop;
		top = anchor - (100 - alignment.y) * height / 100.0;
		if (top < borderTop || top+height > borderBottom) {
			// does not fit even flipped, so we revert back
			anchor = targetHeight * targetY / 100.0 + targetTop;
			top = anchor - alignment.y * height / 100.0;
		}
		if (top + height > borderBottom)
			top = borderBottom - height;
		if (top < borderTop)
			top = borderTop;
	}

	if (indicator[0] && indicator[1]) {
		indicator.removeClass("left right top bottom");
		if (left >= targetLeft + targetWidth) {
			if (left + width + alignment.offset <= borderRight) {
				indicator.addClass("right");
				left += alignment.offset;
				var targetCenter = targetTop + targetHeight/2.0;
				var indicatorTop = targetCenter - top - indicatorSize;
				if (indicatorTop < indicatorSize)
					indicatorTop = indicatorSize;
				if (indicatorTop > height - 3 * indicatorSize)
					indicatorTop = height - 3 * indicatorSize;
				$(indicator[0]).css({top: (indicatorTop - 1) + "px"});
				$(indicator[1]).css({top: indicatorTop + "px"});
			}
		} else if (left + width <= targetLeft) {
			if (left - alignment.offset >= borderLeft) {
				indicator.addClass("left");
				left -= alignment.offset;
				var targetCenter = targetTop + targetHeight/2.0;
				var indicatorTop = targetCenter - top - indicatorSize;
				if (indicatorTop < indicatorSize)
					indicatorTop = indicatorSize;
				if (indicatorTop > height - 3 * indicatorSize)
					indicatorTop = height - 3 * indicatorSize;
				$(indicator[0]).css({top: (indicatorTop - 1) + "px"});
				$(indicator[1]).css({top: indicatorTop + "px"});
			}
		} else if (top >= targetTop + targetHeight) {
			if (top + height + alignment.offset <= borderBottom) {
				indicator.addClass("bottom");
				top += alignment.offset;
				var targetCenter = targetLeft + targetWidth/2.0;
				var indicatorLeft = targetCenter - left - indicatorSize;
				if (indicatorLeft < indicatorSize)
					indicatorLeft = indicatorSize;
				if (indicatorLeft > width - 3 * indicatorSize)
					indicatorLeft = width - 3 * indicatorSize;
				$(indicator[0]).css({left: (indicatorLeft - 1) + "px"});
				$(indicator[1]).css({left: indicatorLeft + "px"});
			}
		} else if (top + height <= targetTop) {
			if (top - alignment.offset >= borderTop) {
				indicator.addClass("top");
				top -= alignment.offset;
				var targetCenter = targetLeft + targetWidth/2.0;
				var indicatorLeft = targetCenter - left - indicatorSize;
				if (indicatorLeft < indicatorSize)
					indicatorLeft = indicatorSize;
				if (indicatorLeft > width - 3 * indicatorSize)
					indicatorLeft = width - 3 * indicatorSize;
				$(indicator[0]).css({left: (indicatorLeft - 1) + "px"});
				$(indicator[1]).css({left: indicatorLeft + "px"});
			}
		}
	} else {
		if (left >= targetLeft + targetWidth) {
			if (left + width + alignment.offset <= borderRight)
				left += alignment.offset;
		} else if (left + width <= targetLeft) {
			if (left - alignment.offset >= borderLeft)
				left -= alignment.offset;
		} else if (top >= targetTop + targetHeight) {
			if (top + height + alignment.offset <= borderBottom) 
				top += alignment.offset;
		} else if (top + height <= targetTop) {
			if (top - alignment.offset >= borderTop) 
				top -= alignment.offset;
		}
	}
	this.css({left:left, top:top});		
	return this;
};

var pmease = {};

pmease.commons = {
	modal: {
		setupTrigger: function(triggerId, modalId, contentLoader) {
			var trigger = $("#" + triggerId);
			var modal = $("#" + modalId);

			// This script can still be called if CollapseBehavior is added to a 
			// a component enclosed in an invisible wicket:enclosure. So we 
			// should check if relevant element exists.
			if (!trigger[0] || !modal[0])
				return;
			
			$("#" + triggerId).click(function() {
				pmease.commons.modal.show(modalId, contentLoader);
			});
		},

		setup: function(modalId, modalWidth, showImmediately) {
			var modal = $("#" + modalId);
			
			// This script can still be called if CollapseBehavior is added to a 
			// a component enclosed in an invisible wicket:enclosure. So we 
			// should check if relevant element exists.
			if (!modal[0])
				return;
			
			modal.before("<div id='" + modalId + "-placeholder' class='hide'></div>");
			modal.modal({backdrop: "static", keyboard: false, show: false});
			if (modalWidth)
				modal.find(".modal-dialog").css({"width": modalWidth});
			if (showImmediately)
				pmease.commons.modal.show(modalId, undefined);
		},

		show: function(modalId, contentLoader) {
			var modal = $("#" + modalId);
			$("body").append(modal);
			
			modal.modal("show");

			pmease.commons.modal.afterShow(modal);
			
			if (!modal.find(">.modal-dialog>.content")[0])
				contentLoader();
			else
				modal.find("input[type=text], input[type=textarea]").filter(":visible:first").focus();
		},
		
		afterShow: function(modal) {
			var backdrop = $(".modal-backdrop:last");
			var prevPopup = modal.prevAll(".popup:visible");
			
			if (prevPopup[0]) {
				var prevPopupZIndex = parseInt(prevPopup.css("z-index"));
				var backdropZIndex = prevPopupZIndex + 10;
				backdrop.css("z-index", backdropZIndex);
				modal.css("z-index", backdropZIndex + 10);
			}
		},

		loaded: function(modalId) {
			$("#" + modalId).find("input[type=text], input[type=textarea]").filter(":visible:first").focus();
		},

		hide: function(modalId) {
			var modal = $("#" + modalId);
			modal.modal("hide");
			
			$("#" + modalId + "-placeholder").after(modal);
		}
	},
	
	confirm: {
		show: function(trigger, message, callback) {
			var html = 
				"<div class='modal popup'><div class='modal-dialog'><div class='modal-content'>" + 
				"	<div class='modal-body'>" +
				"		<p>" + message + "</p>" +
				"	</div>" +
				"	<div class='modal-footer'>" +
				"		<button class='btn btn-default' onclick='$(this).closest(\".modal\").modal(\"hide\").remove();'>Cancel</button>" +
				"		<button class='btn btn-primary'>Ok</button>" +
				"	</div>" +
				"</div></div></div>";
			
			var modal = $(html);

			$("body").append(modal);
			modal[0].confirm = {};
			modal[0].confirm.trigger= trigger[0];
			modal[0].confirm.callback = callback;

			modal.find(".btn-primary").on("click", function() {
				modal.modal("hide").remove();
				callback();
			});
			
			trigger.blur();
			
			modal.modal({keyboard: false, backdrop: "static"});
			
			pmease.commons.modal.afterShow(modal);
		},
		
		setup: function(triggerId, message) {
			$(function() {
				var trigger = $("#" + triggerId);
				
				var previousClick;

				var handlers = $._data(trigger[0], 'events').click;

				$.each(handlers, function(i,f) {
					previousClick = f.handler; 
					return false; 
				});
				
				trigger.unbind('click');

				trigger.click(function(event){
					pmease.commons.confirm.show(trigger, message, function() {
						previousClick(event);
					});
				});
			});
		}
		
	},
	
	decorateTable: function(table) {
		if ($(table).attr("class") == undefined)
			$(table).addClass("table table-striped table-hover table-condensed");
	},
	
	decorateElements: function(scope) {
		if ($(scope).is("table"))
			pmease.commons.decorateTable(scope);
		$(scope).find("table").each(function() {
			pmease.commons.decorateTable(this);
		});
	},
	
	setupCollapse: function(triggerId, targetId) {
		var trigger = $("#" + triggerId);
		var target = $("#" + targetId);

		// This script can still be called if CollapseBehavior is added to a 
		// a component enclosed in an invisible wicket:enclosure. So we 
		// should check if relevant element exists.
		if (!trigger[0] || !target[0])
			return;
		
		target[0].trigger = trigger[0];

		target.on("shown.bs.collapse hidden.bs.collapse", function() {
			var dropdown = target.closest(".dropdown-panel");
			if (dropdown.length != 0) {
				var borderTop = $(window).scrollTop();
				var borderBottom = borderTop + $(window).height();
				var borderLeft = $(window).scrollLeft();
				var borderRight = borderLeft + $(window).width();

				var left = dropdown.position().left;
				var top = dropdown.position().top;
				var width = dropdown.outerWidth();
				var height = dropdown.outerHeight();
				
				if (left < borderLeft || left + width > borderRight || top < borderTop || top + height > borderBottom)
					dropdown.align();
			}
			
		});
		trigger.click(function() {
			if (target[0].collapsibleIds == undefined) {
				if (!target.hasClass("in")) {
					target.collapse("show");
					$(target[0].trigger).removeClass("collapsed");
				} else {
					target.collapse("hide");
					$(target[0].trigger).addClass("collapsed");
				}
			} else if (!target.hasClass("in")) {
				for (var i in target[0].collapsibleIds) {
					var collapsible = $("#" + target[0].collapsibleIds[i]);
					if (collapsible.hasClass("in")) {
						collapsible.collapse("hide");
						$(collapsible[0].trigger).addClass("collapsed");
					}
				}
				target.collapse("show");
				$(target[0].trigger).removeClass("collapsed");
			}
		});
	},
	
	setupAccordion: function(accordionId) {
		var accordion = $("#" + accordionId);
		var collapsibleIds = new Array();
		accordion.find(".collapse:not(#" + accordionId + " .collapse .collapse, #" + accordionId + " .accordion .collapse)").each(function() {
			collapsibleIds.push(this.id);
		});
		if (collapsibleIds[0]) {
			var collapsible = $("#" + collapsibleIds[0]);
			collapsible.removeClass("collapse");
			collapsible.addClass("in");
		}
		for (var i in collapsibleIds) {
			var collapsible = $("#" + collapsibleIds[i]);
			if (i == 0) {
				$(collapsible[0].trigger).removeClass("collapsed");
				collapsible.removeClass("collapse");
				collapsible.addClass("in");
			} else {
				$(collapsible[0].trigger).addClass("collapsed");
			}
			collapsible[0].collapsibleIds = collapsibleIds;
		}
	},
	
	dropdown: {
		setup: function(triggerId, dropdownId, hoverDelay, alignment, dropdownLoader) {
			if (alignment.target) {
				var target = $("#" + alignment.target.id);
				if (!target[0])
					return;
				alignment.target.element = target[0];
				$(alignment.target.element).addClass("dropdown-alignment");
			}
			
			var trigger = $("#" + triggerId);
			
			// This script can still be called if CollapseBehavior is added to a 
			// a component enclosed in an invisible wicket:enclosure. So we 
			// should check if relevant element exists.
			if (!trigger[0])
				return;
			
			trigger.addClass("dropdown-toggle");
			if (hoverDelay >= 0)
				trigger.addClass("dropdown-hover");
			
			var dropdown = $("#" + dropdownId);

			if (!dropdown[0])
				return;
					
			// Dropdown can associate with multiple triggers, and we should initialize it only once.
			if (!$("#" + dropdownId + "-placeholder")[0]) 
				dropdown.before("<div id='" + dropdownId + "-placeholder' class='hide'></div>");

			if (hoverDelay >= 0) {
				function hide() {
					var topmostDropdown = $("body>.dropdown-panel:visible:last");
					if (topmostDropdown[0] == dropdown[0]) 
						pmease.commons.dropdown.hide(dropdownId);
					trigger.hideTimer = null;
				}
				function prepareToHide() {
					if (trigger.hideTimer != null) 
						clearTimeout(trigger.hideTimer);
					trigger.hideTimer = setTimeout(function(){
						if (trigger.hasClass("open"))
							hide();
					}, hoverDelay);
				}
				function cancelHide() {
					if (trigger.hideTimer != null) {
						clearTimeout(trigger.hideTimer);
						trigger.hideTimer = null;				
					} 
				}
				function cancelShow() {
					if (trigger.showTimer != null) {
						clearTimeout(trigger.showTimer);
						trigger.showTimer = null;
					}
				}
				trigger.mouseover(function(mouse){
					if (!trigger.showTimer) {
						trigger.showTimer = setTimeout(function() {
							if (!trigger.hasClass("open")) {
								if (alignment.target == undefined || alignment.target.id == undefined)
									alignment.target = mouse;
								pmease.commons.dropdown.show(trigger, dropdown, alignment, dropdownLoader);
								cancelHide();
							}
							trigger.showTimer = null;
						}, hoverDelay);
					}
				});
				dropdown.mouseover(function() {
					cancelHide();
				});
				trigger.mouseout(function() {
					prepareToHide();
					cancelShow();
				});
				trigger.mousemove(function() {
					cancelHide();
				});
				dropdown.mouseout(function(event) {
					if (event.pageX<dropdown.offset().left+5 || event.pageX>dropdown.offset().left+dropdown.width()-5 
							|| event.pageY<dropdown.offset().top+5 || event.pageY>dropdown.offset().top+dropdown.height()-5) {
						prepareToHide();
					}
				});
			} else {
				trigger.click(function(mouse) {
					if (!trigger.hasClass("open")) {
						if (alignment.target == undefined || alignment.target.id == undefined)
							alignment.target = mouse;
						pmease.commons.dropdown.show(trigger, dropdown, alignment, dropdownLoader);
					} else {
						pmease.commons.dropdown.hide(dropdownId);
					} 
					return false;
				});
			}
			
			return this;
		},

		// hide all dropdowns except the dropdown attached to specified trigger
		hideExcept: function(trigger) {
			var start;
			var dropdown = trigger.closest(".dropdown-panel");
			if (dropdown[0]) {
				start = dropdown;
			} else {
				var topmostModal = $("body>.modal:visible:last");
				if (topmostModal[0]) 
					start = topmostModal;
				else 
					start = $("body").children(":first");
			}		
			var childDropdownEl = start.nextAll(".dropdown-panel:visible")[0];
			if (childDropdownEl)
				pmease.commons.dropdown.hide(childDropdownEl.id);
		},

		show: function(trigger, dropdown, alignment, dropdownLoader) {
			pmease.commons.dropdown.hideExcept(trigger);
			
			dropdown[0].trigger = trigger[0];
			dropdown[0].alignment = alignment;
			
			trigger.addClass("open");
			trigger.parent().addClass("open");

			if (alignment.target.element)
				$(alignment.target.element).addClass("open");

			if (alignment.showIndicator) {
				dropdown.prepend("<div class='indicator'></div>");
				dropdown.append("<div class='indicator'></div>");
			}
			
			var topmostPopup = $("body>.popup:visible:last");
			if (topmostPopup[0])
				dropdown.css("z-index", parseInt(topmostPopup.css("z-index")) + 10);

			$("body").append(dropdown);
			dropdown.align(alignment).show();

			if (!dropdown.find(">.content")[0]) {
				dropdown.find(">div").addClass("content");
				dropdownLoader();
			}
			dropdown.trigger("show");
		},
		
		loaded: function(dropdownId) {
		},
		
		hide: function(dropdownId) {
			var dropdown = $("#" + dropdownId);
			dropdown.trigger("hide");
			
			var childDropdown = dropdown.nextAll(".dropdown-panel:visible");

			if (childDropdown[0])
				hideDropdown(childDropdown[0].id);
			
			var trigger = $(dropdown[0].trigger);
			trigger.removeClass("open");
			trigger.parent().removeClass("open");
			
			if ($(dropdown[0].alignment.target.element))
				$(dropdown[0].alignment.target.element).removeClass("open");
			dropdown.find(">.indicator").remove();
			
			dropdown.hide();
			
			$("#" + dropdownId + "-placeholder").after(dropdown);
		}
	},
	
	editable: {
		adjustReflectionEditor: function(editorId) {
			var editor = $("#" + editorId);
			var input = editor.find(".value>input[type=checkbox]");
			input.parent().prev("label.name").addClass("pull-left");
			input.after("<div style='clear:both;'/>")
			
			input = editor.find(".value>div>input[type=checkbox]");
			input.parent().parent().prev("label.name").addClass("pull-left");
			input.after("<div style='clear:both;'/>")
		},		
	},
};

$(function() {
	$(document).mousedown(function(event) {
		var source = $(event.target);
		if (!source.closest(".dropdown-toggle")[0])
			pmease.commons.dropdown.hideExcept(source);
	});
	
	Wicket.Event.subscribe("/dom/node/added", function(jqEvent, element) {
		pmease.commons.decorateElements(element);
	});	
	pmease.commons.decorateElements(document);
	
	$(document).keypress(function(e) {
		if (e.keyCode == 27) { // esc
			if ($(".select2-drop:visible").length == 0) {
				var topmostPopup = $("body>.popup:visible:last");
				if (topmostPopup.hasClass("modal")) {
					if (!topmostPopup[0].confirm)
						pmease.commons.modal.hide(topmostPopup[0].id);
					else
						topmostPopup.modal("hide").remove();
				}
				if (topmostPopup.hasClass("dropdown-panel"))
					pmease.commons.dropdown.hide(topmostPopup[0].id);
			}
		} else if (e.keyCode == 13) {
			var topmostPopup = $("body>.popup:visible:last");
			if (topmostPopup[0].confirm) {
				topmostPopup[0].confirm.callback();
				topmostPopup.modal("hide").remove();
			}
		}
	});

	Wicket.Event.subscribe('/ajax/call/success', function() {
		$("body>.dropdown-panel:visible").each(function() {
			if (!$("#" + this.id + "-placeholder")[0])
				$(this).remove();
		});
		
		$("body>.dropdown-panel:visible:last").align();

		$("body>.modal:visible").each(function() {
			if (!this.confirm) {
				if (!$("#" + this.id + "-placeholder")[0]) {
					$(this).modal("hide");
					$(this).remove();
				}
			} else {
				if (!$("#" + this.confirm.trigger.id)[0])
					$(this).modal("hide").remove();
			}
		});
	});
	
});
