onedev.server.chat = {
	onDomReady: function() {
		var $chat = $(".chat");
		var $resizeHandle = $chat.children(".ui-resizable-handle");
		$chat.resizable({
			autoHide: false,
			handles: {"w": $resizeHandle},
			minWidth: 300,
			stop: function(e, ui) {
				Cookies.set("chat.width", ui.size.width, {expires: Infinity});
			}
		});

		var $textarea = $chat.find(">.body>.send textarea");
		var $submit = $chat.find(">.body>.send a.submit");
		
		function updateSubmit() {
			var $responding = $chat.find(">.body>.messages>.responding");		
			var isEmpty = $textarea.val().trim() === "";
			$submit.toggleClass("disabled", isEmpty);
			if (isEmpty && !$responding.is(":visible")) {
				$submit.attr("disabled", "disabled");
			} else {
				$submit.removeAttr("disabled");
			}
		}
		
		updateSubmit();
		
		$textarea.on("input", updateSubmit);
		
		$textarea.keydown(function(e) {
			if (e.keyCode == 13 && !e.shiftKey) {
				e.preventDefault();
				if ($textarea.val().trim() !== "" && !$submit.hasClass("disabled")) {
					$submit.click();
				}
			}
		});
	}
}
