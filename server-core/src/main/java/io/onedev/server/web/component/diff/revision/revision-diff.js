onedev.server.revisionDiff = {
	scrollToFilesTop: function() {
		var $textDiffContent = $(".text-diff>tbody").first();
		if ($textDiffContent.length != 0) {
			var $head = $(".revision-diff>.head");
			var $textDiffCaption = $textDiffContent.parent().prev();
			$textDiffContent.css("scroll-margin-top", $head.outerHeight() + $textDiffCaption.outerHeight());
			$textDiffContent[0].scrollIntoView();
			console.log("shit");
		}
	},
	initComment: function() {
		var $comment = $(".revision-diff>.body>.detail>.comment");
		if ($comment.is(":visible")) {
			var $commentResizeHandle = $comment.children(".ui-resizable-handle");
			$comment.resizable({
				autoHide: false,
				handles: {"e": $commentResizeHandle},
				minWidth: 200,
				stop: function(e, ui) {
					Cookies.set("revisionDiff.comment.width", ui.size.width, {expires: Infinity});
				}
			});
		}
	}
};
