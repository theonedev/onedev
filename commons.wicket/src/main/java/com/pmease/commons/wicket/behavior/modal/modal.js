function setupModalTrigger(triggerId, modalId, contentLoader) {
	var trigger = $("#" + triggerId);
	var modal = $("#" + modalId);

	// This script can still be called if CollapseBehavior is added to a 
	// a component enclosed in an invisible wicket:enclosure. So we 
	// should check if relevant element exists.
	if (!trigger[0] || !modal[0])
		return;
	
	$("#" + triggerId).click(function() {
		showModal(modalId, contentLoader);
	});
}

function setupModal(modalId, modalWidth, showImmediately) {
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
		showModal(modalId, undefined);
}

function showModal(modalId, contentLoader) {
	var modal = $("#" + modalId);
	$("body").append(modal);
	
	modal.modal("show");
	
	var backdrop = $(".modal-backdrop:last");
	var prevPopup = modal.prevAll(".popup:visible");
	
	if (prevPopup[0]) {
		var prevPopupZIndex = parseInt(prevPopup.css("z-index"));
		var backdropZIndex = prevPopupZIndex + 10;
		backdrop.css("z-index", backdropZIndex);
		modal.css("z-index", backdropZIndex + 10);
	}
	
	if (!modal.find(">.modal-dialog>.content")[0])
		contentLoader();
	else
		modal.find("input[type=text], input[type=textarea]").filter(":visible:first").focus();
}

function modalLoaded(modalId) {
	$("#" + modalId).find("input[type=text], input[type=textarea]").filter(":visible:first").focus();
}

function hideModal(modalId) {
	var modal = $("#" + modalId);
	modal.modal("hide");
	
	$("#" + modalId + "-placeholder").after(modal);
}

$(function() {
	$(document).keyup(function(e) {
		if (e.keyCode == 27) { // esc
			var topmostPopup = $("body>.popup:visible:last");
			if (topmostPopup.hasClass("modal"))
				hideModal(topmostPopup[0].id);
		}
	});

	Wicket.Event.subscribe('/ajax/call/success', function() {
		$("body>.modal:visible").each(function() {
			if (!$("#" + this.id + "-placeholder")[0]) {
				$(this).modal("hide");
				$(this).remove();
			}
		});
	});
	
});
