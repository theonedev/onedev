onedev.server.colorPicker = {
	onDomReady: function(inputId, allowEmpty) {
		var $input = $("#" + inputId);
		var pickrElementId = inputId + "-pickr";
		var $pickrEl = $("<span id='" + pickrElementId + "'></span>");
		$input.after($pickrEl);

		var pickr = Pickr.create({
		    el: '#' + pickrElementId,
		    theme: 'classic', 
			lockOpacity: true,
			default: $input.val()?$input.val():null,
			defaultRepresentation: 'HEX',
		
		    swatches: [
		        'rgb(244, 67, 54)',
		        'rgb(233, 30, 99)',
		        'rgb(156, 39, 176)',
		        'rgb(103, 58, 183)',
		        'rgb(63, 81, 181)',
		        'rgb(33, 150, 243)',
		        'rgb(3, 169, 244)',
		        'rgb(0, 188, 212)',
		        'rgb(0, 150, 136)',
		        'rgb(76, 175, 80)',
		        'rgb(139, 195, 74)',
		        'rgb(205, 220, 57)',
		        'rgb(255, 235, 59)',
		        'rgb(255, 193, 7)',		    
				'rgb(128, 128, 128)'
			],

		    components: {
		
		        // Main components
		        preview: true,
		        opacity: false,
		        hue: true,
		
		        // Input / output Options
		        interaction: {
		            hex: false,
		            input: true,
		            clear: true,
		            save: true
		        }
		    }
		});		
		pickr.on("save", function(color) {
			function toHex(decimal) {
				var hex = Math.floor(decimal).toString(16);
				if (hex.length < 2)
					hex = "0" + hex;
				return hex;
			}
			if (color) {
				var rgba = color.toRGBA();
				$input.val("#" + toHex(rgba[0]) + toHex(rgba[1]) + toHex(rgba[2]));
				console.log($input.val());
			} else {
				$input.val("");
			}
			$input.change();
			pickr.hide();
		});
	}
}