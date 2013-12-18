function setupDropdown(triggerId, dropdownId, hoverDelay, alignment, dropdownLoader) {
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
				hideDropdown(dropdownId);
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
						showDropdown(trigger, dropdown, alignment, dropdownLoader);
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
				showDropdown(trigger, dropdown, alignment, dropdownLoader);
			} else {
				hideDropdown(dropdownId);
			} 
			return false;
		});
	}
	
	return this;
}

/*
 * Hide all dropdowns not relevant to this trigger.
 */
function hideDropdowns(trigger) {
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
		hideDropdown(childDropdownEl.id);
}

function showDropdown(trigger, dropdown, alignment, dropdownLoader) {
	hideDropdowns(trigger);
	
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
}

function dropdownLoaded(dropdownId) {
}

function hideDropdown(dropdownId) {
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

$(function() {
	$(document).click(function(event) {
		var source = $(event.target);
		if (!source.closest(".dropdown-toggle")[0])
			hideDropdowns(source);
	});
	
	$(document).keyup(function(e) {
		if (e.keyCode == 27) { // esc
			var topmostPopup = $("body>.popup:visible:last");
			if (topmostPopup.hasClass("dropdown-panel"))
				hideDropdown(topmostPopup[0].id);
		}
	});
	
	Wicket.Event.subscribe('/ajax/call/success', function() {
		$("body>.dropdown-panel:visible").each(function() {
			if (!$("#" + this.id + "-placeholder")[0])
				$(this).remove();
		});
		$("body>.dropdown-panel:visible:last").align();
	});
	
});

