onedev.server.bootstrapSwitch = {
	onDomReady: function(elementId) {
		var $switch = $("#" + elementId);
		var $clone = $switch.clone();
		$clone.insertAfter($switch);
		$switch.hide();
		$clone.bootstrapSwitch();
		$clone.on("switchChange.bootstrapSwitch", function(e) {
			$switch.trigger("click");
		});
	}
}