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
	beforeAddComment: function(containerId, commentId, oldLineNo, newLineNo) {
		var $container = $("#" + containerId);
		
		var $content;
		if (newLineNo != -1)
			$content = $container.find(".content.new" + newLineNo); 
		else
			$content = $container.find(".content.old" + oldLineNo);

		var $line = $content.closest(".line");
		while ($line.next().hasClass("comment")) 
			$line = $line.next();
		
		var $commentLine = $("<tr class='line comment'></tr>").insertAfter($line);
		if ($line.children().length == 3)
			$commentLine.append("<td colspan='3' class='content comment'></td>");
		else if (oldLineNo != -1 && newLineNo != -1)
			$commentLine.append("<td colspan='4' class='content comment'></td>");
		else if (oldLineNo != -1)
			$commentLine.append("<td colspan='2' class='content comment'></td><td class='number'>&nbsp;</td><td class='content'>&nbsp;</td>");
		else
			$commentLine.append("<td class='number'>&nbsp;</td><td class='content'>&nbsp;</td><td colspan='2' class='content comment'></td>");
			
		$commentLine.children(".content.comment").append("<div id=" + commentId + "></div>");
	},
	afterAddComment: function(index) {
		var $line = $("#diffline-" + index);
		$line.next().find("td").append($("#comment-diffline-" + index));
	}
}