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
			$content = $container.find(".new" + newLineNo); 
		else
			$content = $container.find(".old" + oldLineNo);

		var $line = $content.closest(".line");
		var $commentLine = $("<tr class='line comments'></tr>").insertAfter($line);
		if ($line.children().length == 3)
			$commentLine.append("<td colspan='3' class='content comments'></td>");
		else if (oldLineNo != -1 && newLineNo != -1)
			$commentLine.append("<td colspan='4' class='content comments'></td>");
		else if (oldLineNo != -1)
			$commentLine.append("<td colspan='2' class='content comments'></td><td class='number'>&nbsp;</td><td class='content'>&nbsp;</td>");
		else
			$commentLine.append("<td class='number'>&nbsp;</td><td class='content'>&nbsp;</td><td colspan='2' class='content comments'></td>");
			
		$commentLine.children(".content.comments").append("<div id=" + commentId + "></div>");
		
		$line.find(".add-comment").addClass("hidden");
	},
	afterAddComment: function(index) {
		var $line = $("#diffline-" + index);
		$line.next().find("td").append($("#comment-diffline-" + index));
	},
	cancelAddComment: function(commentId) {
		var $commentLine = $("#" + commentId).closest(".line");
		$commentLine.prev().find(".add-comment").removeClass("hidden");
		$commentLine.remove();
	},
	afterRemoveComments: function(index) {
		$("#comment-diffline-" + index).closest("tr").remove();
		$("#diffline-" + index).find("a.add-comment").show();
	}		
}