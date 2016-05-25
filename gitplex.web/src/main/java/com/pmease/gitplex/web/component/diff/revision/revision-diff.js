gitplex.revisionDiff = {
	init: function() {
		var cookieName = "revisionDiff.showDiffStats";
		var $body = $(".revision-diff>.body");
		var $diffStats = $body.children(".diff-stats");
		var $diffStatsToggle = $body.find(">.title a");
		$diffStatsToggle.click(function() {
			if ($diffStats.is(":visible")) {
				$diffStats.hide();
				$diffStatsToggle.removeClass("expanded");
				Cookies.set(cookieName, "no", {expires: Infinity});
			} else {
				$diffStats.show();
				$diffStatsToggle.addClass("expanded");
				Cookies.set(cookieName, "yes", {expires: Infinity});
			}
		});
		gitplex.revisionDiff.reposition();
	},
	reposition: function(e) {
		if (e) {
			e.stopPropagation();
			if (e.target && $(e.target).hasClass("ui-resizable"))
				return;
		}
		
		var $detail = $(".revision-diff>.body>.detail");
		if ($detail.length != 0) {
			var $comment = $detail.children(".comment");
			var $diffs = $detail.children(".diffs");
			if ($comment.is(":visible")) {
				$diffs.css("left", $comment.outerWidth(true));
				$diffs.outerWidth($detail.width() - $comment.outerWidth(true));
				
				var scrollTop = $(window).scrollTop();
				var commentOffset = scrollTop - $diffs.offset().top;
				if (commentOffset > 0) {
					$comment.css("top", commentOffset);
				} else {
					$comment.css("top", 0);
				}
				var $lastDiff = $diffs.children().last();
				var commentHeight;
				if ($lastDiff.length != 0) {
					commentHeight = $lastDiff.offset().top + $lastDiff.height() - scrollTop;
				} else {
					commentHeight = $diffs.offset().top + $diffs.height() - scrollTop;
				}
				var windowHeight = $(window).height();
				var minCommentHeight = windowHeight - 100;
				if (commentHeight < minCommentHeight)
					commentHeight = minCommentHeight;
				else if (commentHeight > windowHeight)
					commentHeight = windowHeight;
				var $commentResizeHandle = $comment.children(".ui-resizable-handle");
				$commentResizeHandle.outerHeight(commentHeight - 2);
				var $commentHead = $comment.find(">.content>.head");
				$comment.find(">.content>.body").outerHeight(commentHeight-2-$commentHead.outerHeight());
			} else {
				$diffs.css("left", "0");
				$diffs.outerWidth($detail.width());
			}
			var diffsHeight = $diffs.outerHeight();
			$detail.height(commentHeight>diffsHeight?commentHeight:diffsHeight);
		}
	},
	initComment: function() {
		var $comment = $(".revision-diff>.body>.detail>.comment");
		
		if ($comment.is(":visible")) {
			var commentWidthCookieKey = "revisionDiff.comment.width";
			var commentWidth = Cookies.get(commentWidthCookieKey);
			if (!commentWidth)
				commentWidth = 400;
			$comment.outerWidth(commentWidth);
			var $commentResizeHandle = $comment.children(".ui-resizable-handle");
			var $diffs = $(".revision-diff>.body>.detail>.diffs");
			$comment.resizable({
				autoHide: false,
				handles: {"e": $commentResizeHandle},
				minWidth: 200,
				resize: function(e, ui) {
					var diffsWidth = $diffs.outerWidth();
				    if(diffsWidth < 300)
				    	$(this).resizable({maxWidth: ui.size.width});
				},
				stop: function(e, ui) {
					$(this).resizable({maxWidth: undefined});
					Cookies.set(commentWidthCookieKey, ui.size.width, {expires: Infinity});
					$(window).resize();
				}
			});
		}
	}
}
$(function() {
	$(window).on("scroll resize", gitplex.revisionDiff.position);	
});
