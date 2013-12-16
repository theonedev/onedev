function setupCollapse(triggerId, targetId, collapsibleIds) {
	var trigger = $("#" + triggerId);
	var target = $("#" + targetId);
	if (target.hasClass("in"))
		trigger.addClass("expanded");
	
	target[0].trigger = trigger[0];

	trigger.click(function() {
		if (collapsibleIds == undefined) {
			target.collapse("toggle");
		} else if (!target.hasClass("in")) {
			for (var i in collapsibleIds) {
				var collapsible = $("#" + collapsibleIds[i]);
				collapsible.collapse("hide");
				$(collapsible[0].trigger).removeClass("expanded");
			}
			target.collapse("show");
			$(target[0].trigger).addClass("expanded");
		}
	});
}