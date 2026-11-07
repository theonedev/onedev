onedev.server.codeComment = {
	getLinkedActivity: function() {
		const activity = document.getElementById(decodeURIComponent(location.hash.slice(1)));
		if (activity?.matches(".code-comment>.activity"))
			return activity;
	},
	getThreadView: function(thread) {
		if (thread.closest(".source-view"))
			return onedev.server.sourceView;
		if (thread.closest(".revision-diff"))
			return onedev.server.revisionDiff;
	},
	getScrollOffset: function(thread, {viewportTop, viewportBottom, codeTop, activity, editor}) {
		const linkedActivity = activity && thread.contains(activity) ? activity : null;
		const message = editor ?? linkedActivity ?? thread.querySelector(".code-comment>.comment") ?? thread;
		const body = editor ?? message.querySelector(":scope>.detail>.body") ?? message;
		const threadBottom = thread.getBoundingClientRect().bottom;
		// Include the space above the card when the original comment is the target.
		const threadTop = thread.parentElement.getBoundingClientRect().top;
		const messageBounds = message.getBoundingClientRect();
		const priorityTop = linkedActivity || editor ? messageBounds.top : threadTop;
		const priorityBottom = body.getBoundingClientRect().bottom;
		const viewportHeight = viewportBottom - viewportTop;
		if (priorityBottom - priorityTop >= viewportHeight)
			return priorityTop - viewportTop;

		const contextTop = Math.min(codeTop ?? threadTop, threadTop);
		const firstTopWithThreadVisible = threadBottom - viewportHeight;
		if (firstTopWithThreadVisible <= contextTop) {
			// Keep the current position if the marked code and the thread already fit.
			return Math.min(contextTop, Math.max(viewportTop, firstTopWithThreadVisible)) - viewportTop;
		}

		// Include the thread or the target's own controls when there is room.
		const preferredBottom = firstTopWithThreadVisible <= threadTop ? threadBottom : messageBounds.bottom;
		const visibleBottom = preferredBottom - priorityTop <= viewportHeight ? preferredBottom : priorityBottom;
		return Math.max(contextTop, visibleBottom - viewportHeight) - viewportTop;
	}
};

$(window).on("hashchange", function() {
	const activity = onedev.server.codeComment.getLinkedActivity();
	const thread = activity?.closest(".code-comment-thread");
	if (thread)
		onedev.server.codeComment.getThreadView(thread)?.scrollCommentIntoView({activity});
});

$(document).on("afterElementReplace", function(event, componentId) {
	const $component = $(document.getElementById(componentId));
	const editor = $component.find(".code-comment-editor").addBack(".code-comment-editor")[0];
	const thread = editor?.closest(".code-comment-thread");
	if (!thread)
		return;

	requestAnimationFrame(function() {
		if (!editor.isConnected)
			return;
		onedev.server.codeComment.getThreadView(thread)?.scrollCommentIntoView({editor});

		const textarea = editor.querySelector(".markdown-editor>.body>.edit>textarea");
		if (textarea) {
			textarea.focus({preventScroll: true});
			if (editor.classList.contains("new-comment"))
				textarea.setSelectionRange(textarea.value.length, textarea.value.length);
		}
	});
});
