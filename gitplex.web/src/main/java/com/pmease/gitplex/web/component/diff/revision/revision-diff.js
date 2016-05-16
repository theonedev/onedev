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
		$diffStats.find("a.file").each(function() {
			var $this = $(this);
			var uri = URI(window.location.href);
			uri.removeSearch("mark-file").removeSearch("mark-pos");
			uri.removeSearch("jump-file").addSearch("jump-file", $this.data("file"));
			$this.attr("href", uri.toString());
			$this.click(function(e) {
				e.preventDefault();
				pmease.commons.history.pushState(uri.toString());
				gitplex.revisionDiff.jumpToFile($this.data("file"));
			});
		});

		function onWindowResizeOrScroll(e) {
			if (e && e.target && $(e.target).hasClass("ui-resizable"))
				return;
			
			var $detail = $body.children(".detail");
			$detail.show();
			$body.children(".loading").hide();

			var $comment = $detail.children(".comment");
			var $diffs = $detail.children(".diffs");
			var diffsHeight = $diffs.outerHeight();
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
				var commentHeight = $lastDiff.offset().top + $lastDiff.height() - scrollTop;
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
			$detail.height(commentHeight>diffsHeight?commentHeight:diffsHeight);
		}
		
		onWindowResizeOrScroll();
		$(window).on("scroll resize", onWindowResizeOrScroll);
	},
	initComment: function() {
		var $comment = $(".revision-diff>.body>.detail>.comment");
		
		// we do not use $comment.is(":visible") here as comment can also be invisible 
		// when its parent is not visible initially (in order not to display a weild 
		// page before we adjust the width and height of comment and diffs)
		if ($comment.css("display") != "none") {
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
	},
	scroll: function() {
		var uri = URI(window.location.href); 
		var search = uri.search(true);
		if (search["jump-file"])
			gitplex.revisionDiff.jumpToFile(search["jump-file"]);
	},
	jumpToFile: function(file) {
		var $detail = $(".revision-diff>.body>.detail");
		var $fileDiff = $detail.find('li[data-file="' + file.escape() + '"]');
		$(window).scrollTop($fileDiff.offset().top);
		return false;
	},
	onOpenComment: function(commentInfo) {
		$(window).resize();
	}	
}
