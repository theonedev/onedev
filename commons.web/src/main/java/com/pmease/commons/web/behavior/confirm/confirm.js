function confirmModal(message, callback) {
	var html = 
		"<div class='modal'>" + 
		"	<div class='modal-body'>" +
		"		<p>" + message + "</p>" +
		"	</div>" +
		"	<div class='modal-footer'>" +
		"		<button class='btn' onclick='$(this).closest(\".modal\").modal(\"hide\").remove();'>Cancel</button>" +
		"		<button class='btn-primary btn'>Ok</button>" +
		"	</div>" +
		"</div>";
	
	var modal = $(html);

	$("body").append(modal);

	modal.find(".btn-primary").bind("click", function() {
		modal.modal("hide").remove();
		callback();
	});
	
	modal.modal({keyboard: false, backdrop: "static"});
	afterShowModal(modal);
}

/**
 * Set up confirmation handler for ajax link
 * @param triggerId
 */
function setupConfirm(triggerId, message) {
	var trigger = $("#" + triggerId);
	
	var previousClick;

	var handlers = $._data(trigger[0], 'events').click;

	$.each(handlers, function(i,f) {
		previousClick = f.handler; 
		return false; 
	});
	
	trigger.unbind('click');

	trigger.click(function(event){
		confirmModal(message, function() {
			previousClick(event);
		});
	});
}