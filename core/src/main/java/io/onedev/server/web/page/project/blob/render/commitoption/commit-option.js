onedev.server.commitOption = {
	onBlobChange: function(containerId, defaultCommitMessage, blobModified) {
		var $commitOption = $("#" + containerId + ">.commit-option");
		$commitOption.find(".summary-commit-message").attr("placeholder", defaultCommitMessage);
		var $submitBtn = $commitOption.find("input[type=submit]");
		if (blobModified === true)
			$submitBtn.removeAttr("disabled");
		else
			$submitBtn.attr("disabled", "disabled");
		
	}
};
