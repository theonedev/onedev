gitplex.server.markdown = {
	getSplitCookieKey: function($body) {
		if ($body.hasClass("compact"))
			return "markdownEditor.compactSplitView";
		else
			return "markdownEditor.splitView";
	},
	onDomReady: function(containerId, callback) {
		var $container = $("#" + containerId);
		var $markdownEditor = $container.children(".markdown-editor");
		var $head = $markdownEditor.children(".head");
		var $body = $markdownEditor.children(".body");
		var $editLink = $head.find(".edit");
		var $previewLink = $head.find(".preview");
		var $splitLink = $head.find(".split");
		var $input = $body.children(".input");
		var $preview = $body.children(".preview");
		
		$input.caret(0);

		$editLink.click(function() {
			$preview.hide();
			$input.show().focus();
			$editLink.addClass("active");
			$previewLink.removeClass("active");
			$splitLink.removeClass("active");
			$body.removeClass("preview-mode").removeClass("split-mode").addClass("edit-mode");
			onLayoutChange();
			Cookies.set(gitplex.server.markdown.getSplitCookieKey($body), false, {expires: Infinity});
		});
		$previewLink.click(function() {
			var caret = $input.caret();
			if ($input.val().substring(0, caret).trim().length == 0) {
				/*
				 * If Caret is at the beginning of the input, we should not scroll preview at all 
				 * for better user experience 
				 */
				caret = -1;
				caretOffset = 0;
			} else {
				caretOffset = getCaretCoordinates($input[0], caret).top - $input.scrollTop();
			}
			$preview.data("caret", caret);
			$preview.data("caretOffset", caretOffset);
			
			$preview.html("<div class='message'>Loading...</div>");
			$preview.show();
			$input.hide();
			$editLink.removeClass("active");
			$previewLink.addClass("active");
			$splitLink.removeClass("active");
			$body.removeClass("edit-mode").removeClass("split-mode").addClass("preview-mode");
			onLayoutChange();
			callback("render", $input.val());
		});
		$splitLink.click(function() {
			$input.show().focus();
			$preview.html("<div class='message'>Loading...</div>");
			$preview.show();
			$editLink.removeClass("active");
			$previewLink.removeClass("active");
			$splitLink.addClass("active");
			$body.removeClass("edit-mode").removeClass("preview-mode").addClass("split-mode");
			onLayoutChange();
			callback("render", $input.val());
			Cookies.set(gitplex.server.markdown.getSplitCookieKey($body), true, {expires: Infinity});
		});
		
		$input.on("autosize:resized", function() {
			$preview.outerHeight($input.outerHeight());
		});

		$input.doneEvents("input", function() {
			if ($preview.is(":visible")) {
				callback("render", $input.val());
			}
		}, 500);
		
		$input.doneEvents("keydown", function(e) {
			if (e.keyCode>=33 && e.keyCode<=40 && $preview.is(":visible")) {
				// Only sync preview scroll when we moved cursor
				gitplex.server.markdown.syncPreviewScroll(containerId);
			}
		}, 500);
		
		$input.doneEvents("click focus", function(e) {
			if ($preview.is(":visible")) {
				gitplex.server.markdown.syncPreviewScroll(containerId);
			}
		}, 500);
		
		/*
		 * Padding same leading spaces as last line when add a new line. This is useful when 
		 * add several list items 
		 */
		$input.on("keydown", function(e) {
			if (e.keyCode == 13) {
				e.preventDefault();
				var input = $input.val();
				var caret = $input.caret();
				var inputBeforeCaret = input.substring(0, caret);
				var inputAfterCaret = input.substring(caret);
				var lastLineBreak = inputBeforeCaret.lastIndexOf('\n');
				var spaces = "";
				for (var i=lastLineBreak+1; i<inputBeforeCaret.length; i++) {
					if (inputBeforeCaret[i] == ' ') {
						spaces += " ";
					} else {
						break;
					}
				}
				if (lastLineBreak + spaces.length + 1 == inputBeforeCaret.length) {
					$input.val(inputBeforeCaret + "\n" + inputAfterCaret);
					$input.caret(caret+1);
				} else {
					$input.val(inputBeforeCaret + "\n" + spaces + inputAfterCaret);
					$input.caret(caret + 1 + spaces.length);
				}
				$input.trigger("input", e);
			}
		});
		
		$markdownEditor.on("autofit", function(e, width, height) {
			height -= $head.outerHeight();
			if ($body.hasClass("compact")) {
				$input.outerHeight(height/2);
			} else {
				$input.outerHeight(height);
			}
			$preview.outerHeight($input.outerHeight());
		});

		function onLayoutChange() {
			if (!($body.hasClass("compact"))) {
				if ($preview.is(":visible") && $input.is(":visible")) {
					$preview.css("width", "50%");
					$input.css("width", "50%");
				} else if ($preview.is(":visible")) {
					$preview.css("width", "100%");
				} else {
					$input.css("width", "100%");
				}
			}
		}
		
	},
	/*
	 * Sync preview scroll bar with input scroll bar so that the text at input caret
	 * is always visible in preview window
	 */ 
	syncPreviewScroll: function(containerId) {
		var $preview = $("#" + containerId + ">.markdown-editor>.body>.preview");
		var $input = $("#" + containerId + ">.markdown-editor>.body>.input");
		var caret;
		var caretOffset; // offset in pixel from caret to input top border
		if ($input.is(":visible")) {
			caret = $input.caret();
			if ($input.val().substring(0, caret).trim().length == 0) {
				/*
				 * If Caret is at the beginning of the input, we should not scroll preview at all 
				 * for better user experience 
				 */
				caret = -1;
				caretOffset = 0;
			} else {
				caretOffset = getCaretCoordinates($input[0], caret).top - $input.scrollTop();
			}
		} else {
			caret = $preview.data("caret");
			caretOffset = $preview.data("caretOffset");
		}
		var $blockNearCaret;
		$preview.find("[data-sourcestart]").each(function() {
			var sourceStart = parseInt($(this).data("sourcestart"));
			if (sourceStart <= caret) {
				$blockNearCaret = $(this);
			}
			$(this).removeClass("focusing");
		});
		
		if ($blockNearCaret) {
			/*
			 * Found a block nearby caret. Below logic adjusts the scroll offset to make sure that
			 * the block is visible and try to adjust its position to stay on the same height with
			 * input caret for better user experience  
			 */
			var blockTop = $blockNearCaret.offset().top + $preview.scrollTop() - $preview.offset().top;
			var blockBottom = blockTop + $blockNearCaret.outerHeight();

			var scrollTop;
			if (parseInt($blockNearCaret.data("sourceend")) <= caret) {
				/*
				 * We are behind the block, so we will make sure that bottom of the block is 
				 * always visible
				 */
				scrollTop = blockTop - caretOffset;
				if (blockBottom - scrollTop > $preview.height()) {
					scrollTop = blockBottom - $preview.height(); 
				}
			} else {
				/*
				 * We are at the beginning or in the middle of the block, so make sure that top of 
				 * the block is always visible
				 */
				scrollTop = blockBottom - caretOffset;
				if (blockTop - scrollTop < 0) {
					scrollTop = blockTop; 
				}
			}
			// Highlight the block so that user can easily find matching block while editing
			$blockNearCaret.addClass("focusing");
		} else {
			scrollTop = 0;
		}

		$preview.scrollTop(scrollTop);
    },
	onWindowLoad: function(containerId) {
		var $head = $("#" + containerId + ">.markdown-editor>.head");
		var $body = $("#" + containerId + ">.markdown-editor>.body");
		var $preview = $body.children(".preview");
		var $input = $body.children(".input");
		$preview.outerHeight($input.outerHeight());

		if (Cookies.get(gitplex.server.markdown.getSplitCookieKey($body)) === "true")
			$head.find(".split").trigger("click");
	},
	onRendered: function(containerId, html) {
		var $preview = $("#" + containerId + ">.markdown-editor>.body>.preview");

		var existingImages = {};
		$preview.find("img").each(function() {
			var key = this.outerHTML;
			var elements = existingImages[key];
			if (!elements)
				elements = [];
			elements.push(this);
			existingImages[key] = elements;
		});
		
		$preview.html(html);
		gitplex.server.markdown.initRendered($preview);

		// Avoid loading existing image
		$preview.find("img").each(function() {
			var key = this.outerHTML;
			var elements = existingImages[key];
			if (elements) {
				var element = elements.shift();
				if (element) {
					$(this).removeAttr("src");
					$(this).replaceWith(element);
				}
			}
		});
		
		gitplex.server.markdown.syncPreviewScroll(containerId);
		
        $preview.find("img").load(function() {
            gitplex.server.markdown.syncPreviewScroll(containerId);
        });
        
	},
	
	initRendered: function($rendered) {
		gitplex.server.highlight($rendered);

		$rendered.find("h1, h2, h3, h4, h5, h6").each(function() {
			var $this = $(this);
			var $anchor = $this.find(">a[name]");
			if ($anchor.length != 0) {
				$this.addClass("permalinked").append($anchor.html());
				$anchor.empty();
				$this.append("<a href='#" + $anchor.attr("name") + "' class='permalink'><i class='fa fa-link'></i></a>");
			} else {
				var anchorName = encodeURIComponent($this.text());
				$this.addClass("permalinked").prepend("<a name='" + anchorName + "'></a>");
				$this.append("<a href='#" + anchorName + "' class='permalink'><i class='fa fa-link'></i></a>");
			}
		});
		
		$rendered.find("a").click(function() {
			gitplex.server.viewState.getFromViewAndSetToHistory();
		});
	},
	
	initResponsiveTask: function(containerId, taskCallback, taskClass, 
			taskSourcePositionDataAttribute) {
		var $rendered = $("#" + containerId);
		
		var $task = $rendered.find("." + taskClass);
		var $taskCheckbox = $task.children("input");
		$taskCheckbox.removeAttr("disabled").removeAttr("readonly");
		$taskCheckbox.change(function() {
			taskCallback($(this).parent().data(taskSourcePositionDataAttribute), $(this).prop("checked"));
		});	
	}

}