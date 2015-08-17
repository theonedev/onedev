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

		var $line = $content.closest(".line");
		var $commentLine = $("<tr class='line comments'></tr>").insertAfter($line);
		if ($line.children.length == 3)
			$commentLine.append("<td colspan='3' class='content'></td>");
		else if (oldLineNo != -1 && newLineNo != -1)
			$commentLine.append("<td colspan='4' class='content'></td>");
		else if (oldLineNo != -1)
			$commentLine.append("<td colspan='2' class='content'></td><td colspan='2'>&nbsp;</td>");
		else
			$commentLine.append("<td colspan='2'>&nbsp;</td><td colspan='2' class='content'></td>");
			
		var $addComment = $container.find("form.add-comment");
		
		var $prevCommentLine = $addComment.closest(".line");
		$addComment.find("textarea").val("");
		$commentLine.children(".content").append($addComment);
		
		$prevCommentLine.prev().find("a.add-comment").show();
		$prevCommentLine.remove();
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