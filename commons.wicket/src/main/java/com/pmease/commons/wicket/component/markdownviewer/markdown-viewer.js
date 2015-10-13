pmease.commons.initMarkdownViewer = function($preview, taskCallback) {
	pmease.commons.highlight($preview);
	var $task = $preview.find('.task-list-item input');
	if (taskCallback) {
		$task.change(function() {
			taskCallback($(this).data("mdstart"), $(this).prop("checked"));
		});	
	} else {
		$task.attr("disabled", "disabled");
	}
	
	$preview.find("h1, h2, h3, h4, h5, h6").each(function() {
		var $this = $(this);
		var $anchor = $this.find(">a[name]");
		if ($anchor.length != 0) {
			$this.addClass("anchored-h").append($anchor.html());
			$anchor.empty();
			$this.prepend("<a href='#" + $anchor.attr("name") + "' class='anchor'><i class='fa fa-link'></i></a>");
		} else {
			var anchorName = encodeURIComponent($this.text());
			$this.addClass("anchored-h").prepend("<a name='" + anchorName + "'></a>");
			$this.prepend("<a href='#" + anchorName + "' class='anchor'><i class='fa fa-link'></i></a>");
		}
	});
}