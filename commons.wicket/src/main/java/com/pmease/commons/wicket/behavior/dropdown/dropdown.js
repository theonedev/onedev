function setupDropdown(triggerId, dropdownId, hoverMode, alignmentTargetId, 
		alignmentTargetX, alignmentTargetY, alignmentDropdownX, alignmentDropdownY, 
		dropdownLoader) {
	var alignment = {};
	alignment.target = $("#" + alignmentTargetId)[0];
	alignment.targetX = alignmentTargetX;
	alignment.targetY = alignmentTargetY;
	alignment.dropdownX = alignmentDropdownX;
	alignment.dropdownY = alignmentDropdownY;
	
	$(alignment.target).addClass("dropdown-alignment");
	
	var trigger = $("#" + triggerId);
	trigger.addClass("dropdown-trigger");
	if (hoverMode)
		trigger.addClass("dropdown-hover");
	
	var dropdown = $("#" + dropdownId);

	// dropdown can associate with multiple triggers, and we should initialize it once here.
	if (!dropdown.hasClass("dropdown")) { 
		dropdown.addClass("dropdown popup hide");
		dropdown[0].trigger = trigger[0];
		dropdown.before("<div id='" + dropdownId + "-placeholder' class='hide'></div>");
	}

	if (hoverMode) {
		function hide() {
			var topmostDropdown = $("body>.dropdown:visible:last");
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
			}, 350);
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
				}, 350);
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

function showDropdown(trigger, dropdown, alignment, dropdownLoader) {
	dropdown[0].trigger = trigger[0];
	dropdown[0].alignment = alignment;
	
	trigger.addClass("open");

	$(alignment.target).addClass("open");
	
	var topmostPopup = $("body>.popup:visible:last");
	if (topmostPopup[0])
		dropdown.css("z-index", parseInt(topmostPopup.css("z-index")) + 10);

	$("body").append(dropdown);
	dropdown.align(alignment).show();

	if (!dropdown.find(".dropdown-loaded")[0]) 
		dropdownLoader();
}

function dropdownLoaded(dropdownId) {
}

function hideDropdown(dropdownId) {
	var dropdown = $("#" + dropdownId);
	var childDropdown = dropdown.nextAll(".dropdown:visible");

	if (childDropdown[0])
		hideDropdown(childDropdown[0].id);
	
	var trigger = $(dropdown[0].trigger);
	trigger.removeClass("open");
	
	$(dropdown[0].alignment.target).removeClass("open");
	
	dropdown.hide();
	
	$("#" + dropdownId + "-placeholder").after(dropdown);
}

$(document).click(function(event) {
	var source = $(event.target);
	
	if (!source.closest(".dropdown-trigger")[0]) {
		var start;
		var dropdown = source.closest(".dropdown");
		if (dropdown[0]) {
			start = dropdown;
		} else {
			var topmostModal = $("body>.modal:visible:last");
			if (topmostModal[0]) 
				start = topmostModal;
			else 
				start = $("body").children(":first");
		}		
		var childDropdownEl = start.nextAll(".dropdown:visible")[0];
		if (childDropdownEl)
			hideDropdown(childDropdownEl.id);
	}
});
