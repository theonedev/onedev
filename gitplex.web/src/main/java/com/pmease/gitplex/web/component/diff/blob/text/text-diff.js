gitplex.textdiff = {
	placeComments: function(containerId) {
		var $container = $("#" + containerId);
		$("#comments-placeholder").children("table").each(function() {
			var $lineComments = $(this);
			var id = $lineComments.attr("id");
			var lineId = id.substring("comment-".length);
			var $line = $("#" + lineId);
			var $tr = $("<tr class='line comments'><td colspan='3'></td></tr>").insertAfter($line);
			$tr.children().append($lineComments);
			$line.find("a.add-comment").hide();
		});
	},
	beforeAddComments: function(containerId, oldLineNo, newLineNo) {
		var $container = $("#" + containerId);
		
		var $content;
		if (newLineNo)
			$content = $container.find(".new" + newLineNo); 
		else
			$content = $container.find(".old" + oldLineNo);

		var $tr = $("<tr class='line comments'><td colspan='3'></td></tr>").insertAfter($line);
		
		var $addComment = $("#add-comment");
		var $commentsRow = $addComment.closest("tr.line.comments");
		$addComment.find("textarea").val("");
		$tr.children().append($addComment);
		$commentsRow.prev().find("a.add-comment").show();
		$commentsRow.remove();
		$addComment.find("textarea").focus();
		pmease.commons.form.trackDirty($addComment);
		$line.find("a.add-comment").hide();
	},
	afterAddComments: function(index) {
		$("#comments-placeholder").append($("#add-comment"));
		var $line = $("#diffline-" + index);
		$line.next().find("td").append($("#comment-diffline-" + index));
		$("#add-comment").areYouSure({"silent": "true"});
	},
	cancelAddComments: function(index) {
		$("#comments-placeholder").append($("#add-comment"));
		var $line = $("#diffline-" + index);
		$line.next().remove(); 
		$line.find("a.add-comment").show();
		$("#add-comment").areYouSure({"silent": "true"});
	},
	afterRemoveComments: function(index) {
		$("#comment-diffline-" + index).closest("tr").remove();
		$("#diffline-" + index).find("a.add-comment").show();
	}		
}