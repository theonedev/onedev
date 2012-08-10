function openModal(triggerId, modalId, modalWidth, modalLoader) {
	var trigger = $("#" + triggerId);
	var modal = $("#" + modalId);
	modal[0].triggerId = trigger[0].id;

	$("body").append(modal);
	modal.modal({backdrop: "static", keyboard: false});
	
	afterShowModal(modal);
	
	if (!modal.find(".loaded")[0]) 
		modalLoader();
	else if (modalWidth != "null")
		positionModal(modal, modalWidth);
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
	var trigger = $("#" + modal[0].triggerId);
	modal.modal("hide");
	trigger.after(modal);
}

function afterShowModal(modal) {
	var backdrop = $(".modal-backdrop:last");
	var prevModal = modal.prevAll(".modal");
	
	if (prevModal[0]) {
		var prevModalZIndex = parseInt(prevModal.css("z-index"));
		var backdropZIndex = prevModalZIndex + 10;
		backdrop.css("z-index", backdropZIndex);
		modal.css("z-index", backdropZIndex + 10);
	}
}
