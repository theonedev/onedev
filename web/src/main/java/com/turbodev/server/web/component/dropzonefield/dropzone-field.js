turbodev.server.onDropzoneDomReady = function(containerId, uploadUrl, deleteCallback, maxFilesize) {
	var $input = $("#" + containerId + ">.dropzone");
	$input.dropzone({
		url: uploadUrl,
		addRemoveLinks: true,
		maxFilesize: maxFilesize, 
		dictDefaultMessage: "Drop files here or click to upload",
		removedfile: function(file) {
			deleteCallback(file.name);
			$(file.previewElement).remove();
		},
		headers: {
			"Wicket-Ajax": true,
			"Wicket-Ajax-BaseURL": Wicket.Ajax.baseUrl
		}
	});
}