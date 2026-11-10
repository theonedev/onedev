onedev.server.revisionDiff = {
	onFileDiffReady: function() {
		const $detail = $(".revision-diff>.body>.detail");
		$detail.find(".file-tree .tree-content>a.active").removeClass("active");
		const path = $detail.children(".file-diff").attr("data-selected-file");
		const link = path && document.getElementById("diff-link-" + path);
		if (link) {
			link.classList.add("active");
			this.scrollTreeSelectionIntoView();
		}
	},
	scrollFileDiffIntoView: function() {
		const diff = document.querySelector(".revision-diff>.body>.detail>.file-diff");
		diff?.scrollIntoView({block: "start", inline: "nearest"});
	},
	scrollTreeSelectionIntoView: function() {
		const content = document.querySelector(".revision-diff>.body>.detail>.file-tree>.content");
		const link = content?.querySelector(".tree-content>a.active");
		if (link) {
			const bounds = content.getBoundingClientRect();
			const linkBounds = link.getBoundingClientRect();
			if (linkBounds.top < bounds.top)
				content.scrollTop += linkBounds.top - bounds.top;
			else if (linkBounds.bottom > bounds.bottom)
				content.scrollTop += linkBounds.bottom - bounds.bottom;
		}
	},
	initComment: function(commentId, diffId, leftSide, startLine, endLine) {
		const $revisionDiff = $(".revision-diff");
		$revisionDiff.data("inlineComment", {commentId, diffId, leftSide, startLine, endLine});
		this.positionComment($revisionDiff);
	},
	positionComment: function($revisionDiff) {
		const state = $revisionDiff.data("inlineComment");
		if (!state)
			return;

		const $comment = $(document.getElementById(state.commentId));
		const $diff = $(document.getElementById(state.diffId)).find(".blob-text-diff").first();
		const $codeRow = $diff.length && state.endLine != null
			? onedev.server.blobTextDiff.getLineCell($diff, state.leftSide, state.endLine).parent() : $();
		const $inlineRows = $revisionDiff.find(".blob-text-diff>table.text-diff>tbody>tr.inline-comment-row");
		if (!$comment.hasClass("code-comment-thread") || !$codeRow.length) {
			// Outdated comments may no longer have a line in the current comparison.
			const $home = $revisionDiff.find(">.body>.comment-home");
			if (!$comment.parent().is($home))
				$comment.appendTo($home);
			$inlineRows.remove();
			return;
		}
		if ($codeRow.next().is($comment.closest("tr.inline-comment-row")))
			return;

		const $inlineRow = $("<tr class='inline-comment-row'></tr>");
		$("<td class='inline-comment-cell'></td>").attr("colspan", $codeRow.children().length)
			.append($comment).appendTo($inlineRow);
		$inlineRows.remove();
		$codeRow.after($inlineRow);
	},
	onWindowLoad: function() {
		requestAnimationFrame(function() {
			if (onedev.server.canUseCustomScrollOnLoad())
				onedev.server.revisionDiff.scrollCommentIntoView({activity: onedev.server.codeComment.getLinkedActivity()});
		});
	},
	scrollCommentIntoView: function({activity, editor} = {}) {
		const $revisionDiff = $(".revision-diff");
		const state = $revisionDiff.data("inlineComment");
		const $thread = state ? $("#" + state.commentId) : $();
		if (!$thread.is(":visible"))
			return;

		const $diff = $thread.closest(".blob-text-diff");
		const code = $diff.length
			? onedev.server.blobTextDiff.getLineCell($diff, state.leftSide, state.startLine)[0]
				|| $thread.closest("tr.inline-comment-row").prev("tr.code")[0]
			: undefined;
		const $scrollParent = $thread.scrollParent();
		const scroller = $scrollParent[0];
		const viewport = scroller === document ? null : scroller.getBoundingClientRect();
		const viewportTop = viewport ? Math.max(0, viewport.top + scroller.clientTop) : 0;
		const viewportBottom = viewport ? Math.min(window.innerHeight, viewport.top + scroller.clientTop + scroller.clientHeight)
			: window.innerHeight;
		const headerHeight = $revisionDiff.children(".head").outerHeight()
			+ ($diff.closest(".blob-diff").children(".head").outerHeight() || 0);
		const sticky = $diff.children(".diff-symbol-sticky")[0];

		const scroll = function(stickyHeight) {
			const offset = onedev.server.codeComment.getScrollOffset($thread[0], {
				codeTop: code?.getBoundingClientRect().top,
				viewportTop: viewportTop + headerHeight + stickyHeight,
				viewportBottom, activity, editor
			});
			$scrollParent.scrollTop($scrollParent.scrollTop() + offset);
		};

		const stickyHeight = sticky?.getBoundingClientRect().height || 0;
		scroll(stickyHeight);
		if (sticky) {
			onedev.server.diffSymbolContext.update($diff);
			const updatedHeight = sticky.getBoundingClientRect().height;
			if (updatedHeight !== stickyHeight)
				scroll(updatedHeight);
		}
	},
	initTree: function() {
		var $tree = $(".revision-diff>.body>.detail>.file-tree");
		if ($tree.is(":visible")) {
			var $treeResizeHandle = $tree.children(".ui-resizable-handle");
			$tree.resizable({
				autoHide: false,
				handles: {"e": $treeResizeHandle},
				minWidth: 200,
				stop: function(e, ui) {
					Cookies.set("revisionDiff.navigation.width", ui.size.width, {expires: Infinity});
				}
			});
			this.scrollTreeSelectionIntoView();
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

// Keep the thread available when its surrounding diff is refreshed.
$(document).on('/dom/node/removing', function(event, node) {
	const $revisionDiff = $(node).closest(".revision-diff");
	const state = $revisionDiff.data("inlineComment");
	if (!state)
		return;
	const comment = document.getElementById(state.commentId);
	const home = $revisionDiff.find(">.body>.comment-home")[0];
	if (comment && home && node !== comment && node.contains(comment) && !node.contains(home))
		home.append(comment);
});
$(document).on('/dom/node/added', function(event, node) {
	const $revisionDiff = $(node).closest(".revision-diff");
	const state = $revisionDiff.data("inlineComment");
	if (!state)
		return;
	const replacedCode = node.closest(".blob-text-diff") && !node.closest(".code-comment-thread");
	if (node.id === state.commentId || replacedCode || node.querySelector(".blob-text-diff"))
		onedev.server.revisionDiff.positionComment($revisionDiff);
});
