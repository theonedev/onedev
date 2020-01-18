onedev.server.dropzone = {
	onDomReady: function(containerId, uploadUrl, deleteCallback, acceptedFiles, maxFiles, maxFilesize) {
		var input = document.querySelector("#" + containerId + ">.dropzone");
		
		var dropzone = new Dropzone(input, {
			url: uploadUrl,
			addRemoveLinks: true,
			acceptedFiles: acceptedFiles,
			maxFiles: maxFiles,
			maxFilesize: maxFilesize, 
			dictDefaultMessage: "Drop files here or click to upload",
			success: function() {
				onedev.server.form.markDirty($(input).closest("form"));
			},
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
}