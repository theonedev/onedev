gitplex.textdiff = {
	placeComments: function() {
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
	beforeAddComment: function(index) {
		var $line = $("#diffline-" + index); 

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
	afterAddComment: function(index) {
		$("#comments-placeholder").append($("#add-comment"));
		var $line = $("#diffline-" + index);
		$line.next().find("td").append($("#comment-diffline-" + index));
		$("#add-comment").areYouSure({"silent": "true"});
	},
	cancelAddComment: function(index) {
		$("#comments-placeholder").append($("#add-comment"));
		var $line = $("#diffline-" + index);
		$line.next().remove(); 
		$line.find("a.add-comment").show();
		$("#add-comment").areYouSure({"silent": "true"});
	},
	afterRemoveComment: function(index) {
		$("#comment-diffline-" + index).closest("tr").remove();
		$("#diffline-" + index).find("a.add-comment").show();
	}
}
	