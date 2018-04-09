onedev.server.colorPicker = {
	onDomReady: function(inputId) {
		var $input = $("#" + inputId);
		if ($input.val().length == 0)
			$input.val("#000");
		$input.spectrum({
		    preferredFormat: "hex"
		});		
	}
}