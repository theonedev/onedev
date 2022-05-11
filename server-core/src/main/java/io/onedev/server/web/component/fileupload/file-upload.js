onedev.server.fileUpload = {
	onDomReady: function(inputId, hint, iconUrl) {
		var $input = $("#" + inputId);
		$input.change(function() {
			$input.next().children(".upload-hint").text($input[0].files[0].name);
		});
		$input.after("<label for='" + inputId + "'><svg class='icon'><use xlink:href='" + iconUrl + "'/></svg> <span class='upload-hint'>" + hint + "</span></label>");
	}
}