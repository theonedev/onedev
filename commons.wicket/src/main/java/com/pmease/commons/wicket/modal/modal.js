function openModal(triggerId, modalId, modalLoader) {
	var trigger = $("#" + triggerId);
	var modal = $("#" + modalId);
	
	if (!modal[0]) {
		trigger.after("<div id='" + modalId + "'/>");
		modalLoader();
	} else {
		showModal(trigger, modal);
	}
}

function showModal(trigger, modal) {
	$("body").append(modal);
	modal[0].triggerId = trigger[0].id;
	modal.modal("show");
	
	var backdrop = $(".modal-backdrop:last");
	var prevModal = modal.prevAll(".modal");
	
	if (prevModal[0]) {
		var prevModalZIndex = parseInt(prevModal.css("z-index"));
		var backdropZIndex = prevModalZIndex + 10;
		backdrop.css("z-index", backdropZIndex);
		modal.css("z-index", backdropZIndex + 10);
	}
}

function modalLoaded(triggerId, modalId, modalWidth) {
	var trigger = $("#" + triggerId);
	var modal = $("#" + modalId);

	modal
		.modal({backdrop: "static", keyboard: false, show: false})
		.css({"width": modalWidth, "margin-left": function() {return -($(this).width() / 2);}});
	
	showModal(trigger, modal);
}

function hideModal(modalId) {
	var modal = $("#" + modalId);
	var trigger = $("#" + modal[0].triggerId);
	modal.modal("hide");
	trigger.after(modal);
}
