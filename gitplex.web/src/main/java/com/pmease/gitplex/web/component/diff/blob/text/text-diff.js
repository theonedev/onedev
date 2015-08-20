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
	placeComment: function($container, oldLineNo, newLineNo, $comment) {
		var $content;
		if (newLineNo != -1)
			$content = $container.find(".content.new" + newLineNo); 
		else
			$content = $container.find(".content.old" + oldLineNo);

		var $line = $content.closest("tr");
		var $lastLine = $line;
		while ($lastLine.next().hasClass("comment")) 
			$lastLine = $lastLine.next();
		
		var $commentLine = $("<tr class='comment'></tr>").insertAfter($lastLine);
		if ($line.children().length == 3)
			$commentLine.append("<td colspan='3' class='content comment'></td>");
		else if (!$content.hasClass("old") && !$content.hasClass("new"))
			$commentLine.append("<td colspan='4' class='content comment'></td>");
		else if (newLineNo != -1)
			$commentLine.append("<td class='number'>&nbsp;</td><td class='content'>&nbsp;</td><td colspan='2' class='content comment'></td>");
		else
			$commentLine.append("<td colspan='2' class='content comment'></td><td class='number'>&nbsp;</td><td class='content'>&nbsp;</td>");

		$commentLine.children(".content.comment").append($comment);
	}
}