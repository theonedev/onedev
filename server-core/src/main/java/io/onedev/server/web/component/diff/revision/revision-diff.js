onedev.server.revisionDiff = {
	onDomReady: function() {
		$(".revision-diff").on("resized scrolled", onedev.server.revisionDiff.reposition);
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
			onedev.server.revisionDiff.reposition();
		});
		
		var $image = $body.find("img");
		if ($image.length == 0) {
			onedev.server.revisionDiff.reposition();
		} else {
			var loadedImages = 0;
			$image.on("load", function() {
				loadedImages++;
				if (loadedImages == $image.length) {
					onedev.server.revisionDiff.reposition();
				}
			});
		}

		onedev.server.revisionDiff.reposition();
	},
	onWindowLoad: function() {
		if (window.location.hash) {
			var $anchor = $(window.location.hash);
			if ($anchor.length != 0) {
				var detailOffset = $(".revision-diff>.body>.detail").offset().top;
				if ($(window).scrollTop() <= detailOffset)
					$(window).scrollTop(detailOffset);
				$anchor[0].scrollIntoView();
			}
		} 
	},
	reposition: function(e) {
		if ($(".revision-diff>.body>.detail").length == 0)
			return;
		
		if (e) {
			e.stopPropagation();
			if (e.target && $(e.target).hasClass("ui-resizable"))
				return;
		}
		
		var additionalWidth = 0;
		if ($(".revision-diff>.body>.detail>.comment").length != 0)
			additionalWidth++;
		if ($(".revision-diff>.body>.detail td.blame").length != 0)
			additionalWidth++;
		if ($(".diff-mode-split>.revision-diff").length != 0)
			additionalWidth++;

		$(".revision-diff").removeClass("additionalWidth1").removeClass("additionalWidth2").removeClass("additionalWidth3");
		if (additionalWidth != 0)
			$(".revision-diff").addClass("additionalWidth" + additionalWidth);

		var $detail = $(".revision-diff>.body>.detail");
		var $comment = $detail.children(".comment");
		var $diffs = $detail.children(".diffs");
		var windowHeight;
		if ($comment.is(":visible")) {
			windowHeight = $(window).height();
			$detail.css("padding-left", $comment.outerWidth(true));
			var scrollLeft = $(window).scrollLeft();
			var scrollTop = $(window).scrollTop();
			$comment.css("left", $detail.offset().left - scrollLeft);
			var topOffset = $diffs.offset().top - scrollTop;
			var headHeight = $("#layout>.navbar").outerHeight();
			if (topOffset <= headHeight) {
				$comment.css("top", headHeight);
			} else {
				$comment.css("top", topOffset);
			}
			var $lastDiff = $diffs.children().last();
			var commentHeight;
			if ($lastDiff.length != 0) {
				commentHeight = $lastDiff.offset().top + $lastDiff.height() - scrollTop - headHeight;
			} else {
				commentHeight = $diffs.offset().top + $diffs.height() - scrollTop - headHeight;
			}
			var maxCommentHeight = windowHeight - headHeight;
			var minCommentHeight = maxCommentHeight - 20;
			if (commentHeight < minCommentHeight)
				commentHeight = minCommentHeight;
			else if (commentHeight > maxCommentHeight)
				commentHeight = maxCommentHeight;
			var $commentResizeHandle = $comment.children(".ui-resizable-handle");
			$commentResizeHandle.outerHeight(commentHeight - 2);
			var $commentHead = $comment.find(">.content>.head");
			$comment.find(">.content>.body").outerHeight(commentHeight-2-$commentHead.outerHeight());
		} else {
			windowHeight = 0;
			$detail.css("padding-left", "0");
		}
		var diffsHeight = $diffs.outerHeight();
		$detail.height(windowHeight>diffsHeight?windowHeight:diffsHeight);
	},
	initComment: function() {
		var $comment = $(".revision-diff>.body>.detail>.comment");
		
		if ($comment.is(":visible")) {
			var commentWidthCookieKey = "revisionDiff.comment.width";
			var commentWidth = Cookies.get(commentWidthCookieKey);
			if (!commentWidth)
				commentWidth = $(".revision-diff").outerWidth()/3;
			$comment.outerWidth(commentWidth);
			var $commentResizeHandle = $comment.children(".ui-resizable-handle");
			var $diffs = $(".revision-diff>.body>.detail>.diffs");
			$comment.resizable({
				autoHide: false,
				handles: {"e": $commentResizeHandle},
				minWidth: 200,
				stop: function(e, ui) {
					Cookies.set(commentWidthCookieKey, ui.size.width, {expires: Infinity});
					$(window).resize();
				}
			});
		}
	}
};
