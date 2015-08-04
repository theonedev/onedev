gitplex.textdiff = {
	placeComments: function(containerId) {
		var $container = $("#" + containerId);
		$container.find(".comments-placeholder").children("table").each(function() {
			var $lineComments = $(this);
			var $line = $container.find("." + $lineComments.attr("data-line"));
			var $tr = $("<tr class='line comments'><td colspan='3'></td></tr>").insertAfter($line);
			$tr.children().append($lineComments);
			$line.find("a.add-comment").hide();
		});
	},
	beforeAddComment: function(containerId, index) {
		var $container = $("#" + containerId);
		var $line = $container.find(".diffline-" + index); 

		var $tr = $("<tr class='line comments'><td colspan='3'></td></tr>").insertAfter($line);
		var $addComment = $container.find("form.add-comment");
		var $commentsRow = $addComment.closest("tr.line.comments");
		$addComment.find("textarea").val("");
		$tr.children().append($addComment);
		$commentsRow.prev().find("a.add-comment").show();
		$commentsRow.remove();
		$addComment.find("textarea").focus();
		pmease.commons.form.trackDirty($addComment);
		$line.find("a.add-comment").hide();
	},
	afterAddComment: function(containerId, index) {
		var $container = $("#" + containerId);
		$container.find(".comments-placeholder").append($container.find("form.add-comment"));
		var $line = $container.find(".diffline-" + index);
		$line.next().find("td").append($container.find(".comment-diffline-" + index));
		$container.find("form.add-comment").areYouSure({"silent": "true"});
	},
	cancelAddComment: function(containerId, index) {
		var $container = $("#" + containerId);
		$container.find(".comments-placeholder").append($container.find("form.add-comment"));
		var $line = $container.find(".diffline-" + index);
		$line.next().remove(); 
		$line.find("a.add-comment").show();
		$container.find("form.add-comment").areYouSure({"silent": "true"});
	},
	afterRemoveComment: function(containerId, index) {
		var $container = $("#" + containerId);
		$container.find(".comment-diffline-" + index).closest("tr").remove();
		$container.find(".diffline-" + index).find("a.add-comment").show();
	}
}
	