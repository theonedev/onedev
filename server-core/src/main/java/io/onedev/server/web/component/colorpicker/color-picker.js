onedev.server.colorPicker = {
	onDomReady: function(inputId, allowEmpty) {
		var $input = $("#" + inputId);
		$input.spectrum({
		    preferredFormat: "hex",
		    showInput: true,
		    showInitial: true,
		    allowEmpty: allowEmpty,
		    hideAfterPaletteSelect:true,
		    showPalette: false
		});		
	}
}