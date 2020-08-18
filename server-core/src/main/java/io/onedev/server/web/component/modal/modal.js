onedev.server.modal = {
	onDomReady: function(containerId, closeCallback) {
		var $modal = $("#" + containerId);
		
		$modal.data("closeCallback", closeCallback);
		$modal.on("shown.bs.modal", function(e) {
			$(window).resize();
		});		
		
		$modal.data("keydown", function(e) {
			if (e.keyCode == 27 
					&& $(".select2-drop:visible").length == 0 
					&& $(".dropdown.open").length == 0
					&& $("body>.floating").length == 0 
					&& $(".sp-container:visible").length == 0
					&& $(".atwho-view:visible").length == 0
					&& $(".flatpickr-calendar.open").length == 0
					&& $modal.nextAll(".modal").length == 0
					&& !$("body").hasClass("fullscreen")) {
				if ($modal.find(".leave-confirm.dirty").length != 0) {
					if (confirm("There are unsaved changes, do you want to close?")) {
						onedev.server.modal.close($modal, true);
					}
				} else {
					onedev.server.modal.close($modal, true);
				}
			}
		});
		
		// Use keydown as keypress does not work in chrome/safari
		$(document).on("keydown", $modal.data("keydown"));
		
		$modal.modal({backdrop: "static", "keyboard": false});
		
		$(document.activeElement).blur();
	}, 
	
	close: function($modal, callCloseCallback) {
		if (callCloseCallback)
			$modal.data("closeCallback")();
		
		$(document).off("keydown", $modal.data("keydown"));
		
		setTimeout(function() {
			$modal.modal("hide").remove();
		}, 0);
	}
	
};