onedev.server.revisionDiff = {
	scrollToFilesTop: function() {
		var $textDiffContent = $(".blob-text-diff>tbody").first();
		if ($textDiffContent.length != 0) {
			var $head = $(".revision-diff>.head");
			var $textDiffCaption = $textDiffContent.parent().prev();
			$textDiffContent.css("scroll-margin-top", $head.outerHeight() + $textDiffCaption.outerHeight());
			$textDiffContent[0].scrollIntoView();
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
	},
	onSuggestionBatchChanged: function() {
		var $batch = $(".revision-diff .batched-suggestions");
		$batch.removeClass("btn-light");
		$batch.addClass("btn-warning"); 
		setTimeout(function() {
			$batch.removeClass("btn-warning");
			$batch.addClass("btn-light"); 
		}, 200);
	}
};
