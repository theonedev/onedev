pmease.commons.initMarkdownViewer = function($preview, taskCallback) {
	pmease.commons.highlight($preview);
	var $task = $preview.find('.task-list-item input');
	if (taskCallback) {
		$task.change(function() {
			taskCallback($(this).data("mdstart"), $(this).data("mdend"), $(this).prop("checked"));
		});	
	} else {
		$task.attr("disabled", "disabled");
	}
}