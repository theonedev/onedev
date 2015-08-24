gitplex.textdiff = {
	init: function(containerId, addCommentCallback) {
		var $container = $("#" + containerId);
		$container[0].addComment = addCommentCallback;
		$container.find(">.text-diff>.comment").each(function() {
			var oldLineNo = $(this).data("oldlineno");
			var newLineNo = $(this).data("newlineno");
			gitplex.textdiff.placeComment($container, oldLineNo, newLineNo, $(this));
		});
	},
	removeComment: function(commentId) {
		var $comment = $("#" + commentId);
		var $tr = $comment.closest("tr");
		if ($tr.children(".comment").length == 1)
			$tr.remove();
		else 
			$comment.closest("td").empty().addClass("removed").append("This comment has been removed");
	},
	placeComment: function($container, oldLineNo, newLineNo, $comment) {
		var $content;
		if (newLineNo != -1)
			$content = $container.find(".content.new" + newLineNo); 
		else
			$content = $container.find(".content.old" + oldLineNo);

		var $line = $content.closest("tr");
		var $currentLine = $line;
		var $nextLine = $line.next();
		
		// check if we should place the comment into left side of right side 
		// of existing comment line
		while ($nextLine.hasClass("comment")) {
			var $seat = $nextLine.children(".content").eq(oldLineNo!=-1?0:1);
			if ($seat.length == 0 || $seat.hasClass("comment")) {
				$currentLine = $nextLine;
				$nextLine = $nextLine.next();
			} else {
				$seat.prev().remove();
				$seat.attr("colspan", "2").addClass("comment").empty().append($comment);
				return;
			}
		} 
		
		var $commentLine = $("<tr class='comment'></tr>").insertAfter($currentLine);
		if ($line.children().length == 3)
			$commentLine.append("<td colspan='3' tabindex='0' class='content comment'></td>");
		else if (!$content.hasClass("old") && !$content.hasClass("new"))
			$commentLine.append("<td colspan='4' tabindex='0' class='content comment'></td>");
		else if (newLineNo != -1)
			$commentLine.append("<td class='number'>&nbsp;</td><td tabindex='0' class='content'>&nbsp;</td><td colspan='2' tabindex='0' class='content comment'></td>");
		else
			$commentLine.append("<td colspan='2' tabindex='0' class='content comment'></td><td class='number'>&nbsp;</td><td class='content' tabindex='0'>&nbsp;</td>");

		$commentLine.children(".content.comment").append($comment);
	}
}