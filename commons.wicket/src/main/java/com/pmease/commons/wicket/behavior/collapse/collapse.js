function setupCollapse(triggerId, targetId) {
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
}

function setupAccordion(accordionId) {
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
}
