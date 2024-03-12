onedev.server.commitOption = {
	onBlobChange: function(containerId, blobModified) {
		var $commitOption = $("#" + containerId + ">.commit-option");
		var $submitBtn = $commitOption.find("input[type=submit]");
		if (blobModified === true)
			$submitBtn.removeAttr("disabled");
		else
			$submitBtn.attr("disabled", "disabled");
		
	}
};
