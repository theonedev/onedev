function setupModalTrigger(triggerId, modalId, contentLoader) {
	$("#" + triggerId).click(function() {
		showModal(modalId, contentLoader);
	});
}

function setupModal(modalId, modalWidth, showImmediately) {
	var modal = $("#" + modalId);
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
}

function modalLoaded(modalId) {
}

function hideModal(modalId) {
	var modal = $("#" + modalId);
	modal.modal("hide");
	
	$("#" + modalId + "-placeholder").after(modal);
}

$(function() {
	Wicket.Event.subscribe('/ajax/call/success', function() {
		$("body>.modal:visible").each(function() {
			if (!$("#" + this.id + "-placeholder")[0]) {
				$(this).modal("hide");
				$(this).remove();
			}
		});
	});
	
});
