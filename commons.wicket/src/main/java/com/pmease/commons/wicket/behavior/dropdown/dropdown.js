function setupDropdown(triggerId, dropdownInfo, hoverDelay, alignmentIndicatorMode, alignmentTargetId, 
		alignmentTargetX, alignmentTargetY, alignmentDropdownX, alignmentDropdownY, alignmentGap,
		dropdownLoader) {
	var alignment = {};
	alignment.target = $("#" + alignmentTargetId)[0];
	alignment.targetX = alignmentTargetX;
	alignment.targetY = alignmentTargetY;
	alignment.dropdownX = alignmentDropdownX;
	alignment.dropdownY = alignmentDropdownY;
	alignment.gap = alignmentGap;
	alignment.indicatorMode = alignmentIndicatorMode;
	
	$(alignment.target).addClass("dropdown-alignment");
	
	var trigger = $("#" + triggerId);
	trigger.addClass("dropdown-toggle");
	if (hoverDelay >= 0)
		trigger.addClass("dropdown-hover");
	
	var dropdownId;
	var dropdown;
	if (dropdownLoader != undefined) { 
		/*
		 * Otherwise, dropdownInfo represents id of the dropdown element, and its content has to be 
		 * loaded by calling dropdownLoader.
		 */
		dropdownId = dropdownInfo;
		dropdown = $("#" + dropdownId);
	} else {
		/*
		 * In case dropdownLoader is not defined, the dropdownInfo itself represents content of the
		 * dropdown element.  
		 */
		dropdown = $(dropdownInfo); 
		dropdownId = dropdown[0].id;
		trigger.append(dropdown);
	}
			
	if (alignment.gap < 0) {
		if (trigger.parent().parent().hasClass("nav") && trigger.closest(".navbar")[0])
			alignment.gap = 2;
		else
			alignment.gap = 4;
	}
	
	// dropdown can associate with multiple triggers, and we should initialize it once here.
	if (!dropdown.hasClass("dropdown-panel")) { 
		dropdown.addClass("dropdown-panel popup");
		dropdown[0].trigger = trigger[0];
		dropdown.before("<div id='" + dropdownId + "-placeholder' class='hide'></div>");
	}

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
			if (!trigger.hasClass("open")) 
				showDropdown(trigger, dropdown, alignment, dropdownLoader);
			else 
				hideDropdown(dropdownId);
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

	$(alignment.target).addClass("open");

	var inNavbar = trigger.parent().parent().hasClass("nav") && trigger.closest(".navbar")[0];
	
	if (alignment.indicatorMode == "SHOW" || alignment.indicatorMode == "AUTO" && inNavbar) {
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
}

function dropdownLoaded(dropdownId) {
}

function hideDropdown(dropdownId) {
	var dropdown = $("#" + dropdownId);
	var childDropdown = dropdown.nextAll(".dropdown-panel:visible");

	if (childDropdown[0])
		hideDropdown(childDropdown[0].id);
	
	var trigger = $(dropdown[0].trigger);
	trigger.removeClass("open");
	trigger.parent().removeClass("open");
	
	$(dropdown[0].alignment.target).removeClass("open");
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
	
	Wicket.Event.subscribe('/ajax/call/success', function() {
		$("body>.dropdown-panel:visible:last").align();
	});
	
});

