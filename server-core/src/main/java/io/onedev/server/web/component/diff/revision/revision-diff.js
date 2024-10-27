onedev.server.revisionDiff = {
	onBodyDomReady: function() {
		var $anchors = $(".revision-diff>.body li.diff, .revision-diff>.body li.diff>div");
		$anchors.css("scroll-margin-top", $(".revision-diff>.head").outerHeight() + "px");
		if ($anchors.length != 0)
			$anchors[0].scrollIntoView();
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
	initNavigation: function() {
		var $navigation = $(".revision-diff>.body>.detail>.navigation");
		if ($navigation.is(":visible")) {
			var $navigationResizeHandle = $navigation.children(".ui-resizable-handle");
			$navigation.resizable({
				autoHide: false,
				handles: {"e": $navigationResizeHandle},
				minWidth: 200,
				stop: function(e, ui) {
					Cookies.set("revisionDiff.navigation.width", ui.size.width, {expires: Infinity});
				}
			});
		}
	},
	onToggleNavigation: function() {
		$(".navigation-toggle").prop("checked", $(".revision-diff>.body>.detail>.navigation").is(":visible"));
		$(window).resize();
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
