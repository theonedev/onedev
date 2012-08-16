function setupModal(triggerId, modalId, modalWidth, modalLoader) {
	var trigger = $("#" + triggerId);
	var modal = $("#" + modalId);

	// modal can associate with multiple triggers, and we should initialize it once here.
	if (!modal.hasClass("modal")) {
		modal.addClass("modal hide popup");
		modal.before("<div id='" + modalId + "-placeholder'></div>");
		
		modal.modal({backdrop: "static", keyboard: false, show: false});
	}

	trigger.click(function() {
		$("body").append(modal);
		modal.modal("show");
		
		afterShowModal(modal);
		
		if (!modal.find(".modal-loaded")[0]) 
			modalLoader();
		else if (modalWidth != "null")
			positionModal(modal, modalWidth);
	});
}

function modalLoaded(modalId, modalWidth) {
	if (modalWidth != "null") {
		var modal = $("#" + modalId);
		positionModal(modal, modalWidth);
	}
}

function positionModal(modal, modalWidth) {
	modal.css({"width": modalWidth, "margin-left": function() {return -($(this).width() / 2);}});
}

function hideModal(modalId) {
	var modal = $("#" + modalId);
	modal.modal("hide");
	
	$("#" + modalId + "-placeholder").after(modal);
}

function afterShowModal(modal) {
	var backdrop = $(".modal-backdrop:last");
	var prevPopup = modal.prevAll(".popup:visible");
	
	if (prevPopup[0]) {
		var prevPopupZIndex = parseInt(prevPopup.css("z-index"));
		var backdropZIndex = prevPopupZIndex + 10;
		backdrop.css("z-index", backdropZIndex);
		modal.css("z-index", backdropZIndex + 10);
	}
}
