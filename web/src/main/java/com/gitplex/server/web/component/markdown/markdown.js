gitplex.server.markdown = {
	getCookiePrefix: function($container) {
		if ($container.hasClass("compact-mode"))
			return "markdownEditor.compactMode";
		else
			return "markdownEditor.normalMode";
	},
	fireInputEvent: function($input) {
		if(document.createEventObject) {
			$input[0].fireEvent("input");
		} else {
		    var evt = document.createEvent("HTMLEvents");
		    evt.initEvent("input", false, true);
		    $input[0].dispatchEvent(evt);
		}
	},
	autofit: function(containerId, width, height) {
		var $container = $("#" + containerId);
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $edit = $body.children(".edit");
		var $input = $edit.children("textarea");
		var $preview = $body.children(".preview");
		var $rendered = $preview.children(".markdown-rendered");

		$container.outerWidth(width).outerHeight(height);

		width = $container.width();
		height = $container.height();
		
		height -= $head.outerHeight();
		if ($container.hasClass("compact-mode")) {
			height = height/2;
		}
		$input.outerHeight(height);
		$edit.outerHeight($input.outerHeight());
		$rendered.outerHeight($input.outerHeight());
		$preview.outerHeight($rendered.outerHeight());
	},
	onDomReady: function(containerId, callback, atWhoLimit, attachmentSupport, 
			attachmentMaxSize, canMentionUser, canReferencePullRequest, autosaveKey) {
		var $container = $("#" + containerId);
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $editLink = $head.find(".edit");
		var $previewLink = $head.find(".preview");
		var $splitLink = $head.find(".split");
		var $emojis = $head.children(".emojis");
		var $help = $head.children(".help");
		var $warning = $head.children(".warning");
		var $edit = $body.children(".edit");
		var $input = $edit.children("textarea");
		var $preview = $body.children(".preview");
		var $rendered = $preview.children(".markdown-rendered");
		var $resizeHandles = $container.find(".ui-resizable-handle");

		$container.data("autosaveKey", autosaveKey);
		
		$head.find(".dropdown>button").dropdown();
		
		$editLink.click(function() {
			$container.removeClass("preview-mode").removeClass("split-mode").addClass("edit-mode");
			$input.focus();
			$editLink.addClass("active");
			$previewLink.removeClass("active");
			$splitLink.removeClass("active");
			onLayoutChange();
			Cookies.set(gitplex.server.markdown.getCookiePrefix($container)+".split", false, {expires: Infinity});
		});
		$previewLink.click(function() {
			$container.removeClass("edit-mode").removeClass("split-mode").addClass("preview-mode");
			
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
			$rendered.data("caret", caret);
			$rendered.data("caretOffset", caretOffset);
			
			$rendered.html("<div class='message'>Loading...</div>");
			$editLink.removeClass("active");
			$previewLink.addClass("active");
			$splitLink.removeClass("active");
			onLayoutChange();
			callback("render", $input.val());
		});
		$splitLink.click(function() {
			$container.removeClass("edit-mode").removeClass("preview-mode").addClass("split-mode");
			
			$input.focus();
			
			$rendered.html("<div class='message'>Loading...</div>");
			$editLink.removeClass("active");
			$previewLink.removeClass("active");
			$splitLink.addClass("active");
			onLayoutChange();
			callback("render", $input.val());
			Cookies.set(gitplex.server.markdown.getCookiePrefix($container)+".split", true, {expires: Infinity});
		});
		
		function onLayoutChange() {
			if ($container.hasClass("normal-mode") && $container.hasClass("split-mode")) {
				$rendered.outerHeight($input.outerHeight());
				$preview.outerHeight($edit.outerHeight());
			}
		}
		
		$input.doneEvents("input inserted.atwho", function() {
			if ($preview.is(":visible")) {
				callback("render", $input.val());
			}
			if (autosaveKey) {
				var content = $input.val();
				if (content.trim().length != 0)
					localStorage.setItem(autosaveKey, content);
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
		
	    var fontSize = parseInt(getComputedStyle($input[0]).getPropertyValue('font-size'));
	    
		/*
		 * We intercept the "Enter" key event to do below things to make editing more conveniently:
		 * 
		 * 1. When there is a list item in current line, then add an empty list item automatically 
		 *    to next line. For instance, if current input is:
		 *    
		 *    - item
		 *    
		 *    The new input will be as below upon "Enter":
		 * 
		 *    - item
		 *    -  
		 * 
		 * 2. When there is an empty item in current line, then remove that empty item upon "Enter":
		 *    
		 *    - item
		 *    (blank line)
		 *    (blank line)
		 * 
		 * 3. When current line has leading spaces, make new line has same leading spaces as well. 
		 *    For instance if current line is:
		 *    
		 *    void main() {
		 *      for (int i=0; i<100; i++) {
		 *      
		 *    The new input will be as below upon "Enter":
		 *
		 *    void main() {
		 *      for (int i=0; i<100; i++) {
		 *      (line starts here)
		 *    
		 * 
		 */
		$input.on("keydown", function(e) {
			if (e.keyCode == 13) {
				e.preventDefault();
				var input = $input.val();
				var caret = $input.caret();
				var inputBeforeCaret = input.substring(0, caret);
				var inputAfterCaret = input.substring(caret);
				var lastLineBreakPos = inputBeforeCaret.lastIndexOf('\n');
				var spaces = "";
				for (var i=lastLineBreakPos+1; i<inputBeforeCaret.length; i++) {
					if (inputBeforeCaret[i] == ' ') {
						spaces += " ";
					} else {
						break;
					}
				}
				
				var nonSpacePosInCurrentLine = lastLineBreakPos + spaces.length + 1;
				var nonSpaceInCurrentLine = input.substring(nonSpacePosInCurrentLine, caret);
				
				function clearLastLine() {
					var newInputBeforeCaret;
					if (lastLineBreakPos != -1) {
						newInputBeforeCaret = input.substring(0, lastLineBreakPos) + "\n\n";
					} else {
						newInputBeforeCaret = "\n";
					}
					$input.val(newInputBeforeCaret + inputAfterCaret);
					$input.caret(newInputBeforeCaret.length);
				}

				// match against task items
				var match = /^[-*]\s+\[[x ]\] /.exec(nonSpaceInCurrentLine);
				if (match != null) {
					if (nonSpaceInCurrentLine.length > match[0].length) {
						if (nonSpaceInCurrentLine.indexOf("*") == 0)
							$input.caret("\n" + spaces + "* [ ] ");
						else
							$input.caret("\n" + spaces + "- [ ] ");
					} else {
						clearLastLine();
					}
				} else {
					if (nonSpaceInCurrentLine.indexOf("* ") == 0) {
						if (nonSpaceInCurrentLine.length > 2) {
							$input.caret("\n" + spaces + "* ");
						} else {
							clearLastLine();
						}
					} else if (nonSpaceInCurrentLine.indexOf("- ") == 0) {
						if (nonSpaceInCurrentLine.length > 2) {
							$input.caret("\n" + spaces + "- ");
						} else {
							clearLastLine();
						}
					} else {
						// match against ordered list items
						match = /^\d+\. /.exec(nonSpaceInCurrentLine);
						if (match != null) {
							if (nonSpaceInCurrentLine.length > match[0].length) {
								$input.caret("\n" + spaces + (parseInt(match[0])+1) +". ");
							} else {
								clearLastLine();
							}
						} else if (nonSpacePosInCurrentLine == inputBeforeCaret.length) {
							$input.caret("\n");
						} else {
							$input.caret("\n" + spaces);
						}
					}
				}
				
				// Scroll if necessary to make cursor visible
				var caretBottom = getCaretCoordinates($input[0], $input.caret()).top + fontSize;
				if (caretBottom > $input.scrollTop() + $input.height()) {
					$input.scrollTop(caretBottom - $input.height());
				}
				
				gitplex.server.markdown.fireInputEvent($input);
			}
		});

		$edit.resizable({
			autoHide: false,
			handles: {"s": $edit.children(".ui-resizable-handle")},
			minHeight: 75,
			resize: function(e, ui) {
				$input.outerHeight($edit.height());
				if ($container.hasClass("normal-mode") && $container.hasClass("split-mode")) {
					$rendered.outerHeight($input.outerHeight());
					$preview.outerHeight($edit.outerHeight());
				}
			},
			stop: function(e, ui) {
				Cookies.set(gitplex.server.markdown.getCookiePrefix($container)+".inputHeight", 
						$input.outerHeight(), {expires: Infinity});
				if ($container.hasClass("normal-mode") && $container.hasClass("split-mode")) {
					Cookies.set(gitplex.server.markdown.getCookiePrefix($container)+".renderedHeight", 
							$rendered.outerHeight(), {expires: Infinity});
				}
			}
		});
		
		$preview.resizable({
			handles: {"s": $preview.children(".ui-resizable-handle")},
			minHeight: 75,
			resize: function(e, ui) {
				$rendered.outerHeight($preview.height());
				if ($container.hasClass("normal-mode") && $container.hasClass("split-mode")) {
					$input.outerHeight($rendered.outerHeight());
					$edit.outerHeight($preview.outerHeight());
				}
			},
			stop: function(e, ui) {
				Cookies.set(gitplex.server.markdown.getCookiePrefix($container)+".renderedHeight", 
						$rendered.outerHeight(), {expires: Infinity});
				if ($container.hasClass("normal-mode") && $container.hasClass("split-mode")) {
					Cookies.set(gitplex.server.markdown.getCookiePrefix($container)+".inputHeight", 
							$input.outerHeight(), {expires: Infinity});
				}
			}
		});

		$warning.on("closed.bs.alert", function () {
			if (!$resizeHandles.is(":visible"))
				gitplex.server.markdown.autofit(containerId, $container.outerWidth(), $container.outerHeight());
		})
		
		$container.on("autofit", function(e, width, height) {
			$resizeHandles.hide();
			gitplex.server.markdown.autofit(containerId, width, height);
		});

		$head.find(".do-bold").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("**" + selected.text + "**").range(selected.start+2, selected.end+2);
			} else {
				$input.range("**strong text**").range(selected.start+2, selected.end+2+"strong text".length);
			}
			$input.focus();
			gitplex.server.markdown.fireInputEvent($input);
		});
		
		$head.find(".do-italic").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("_" + selected.text + "_").range(selected.start+1, selected.end+1);
			} else {
				$input.range("_emphasized text_").range(selected.start+1, selected.end+1+"emphasized text".length);
			}
			$input.focus();
			gitplex.server.markdown.fireInputEvent($input);
		});
		
		$head.find(".do-header").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("### " + selected.text).range(selected.start+4, selected.end+4);
			} else {
				$input.range("### heading text").range(selected.start+4, selected.end+4+"heading text".length);
			}
			$input.focus();
			gitplex.server.markdown.fireInputEvent($input);
		});
		
		$head.find(".do-list, .do-orderlist, .do-tasklist").click(function() {
			var leading;
			if ($(this).hasClass(".do-list"))
				leading = "-";
			else if ($(this).hasClass(".do-tasklist"))
				leading = "1.";
			else
				leading = "- [ ]";
			var selected = $input.range();
			if (selected.length != 0) {
				var splitted = selected.text.split("\n");
				var insert = "";
				for (var i in splitted) {
					if (i != 0) 
						insert += "\n";
					insert += leading + " " + splitted[i];
				}
				$input.range(insert).range(selected.start+leading.length+1, selected.start+leading.length+1+splitted[0].length);
			} else {
				var text;
				if ($(this).hasClass("do-tasklist"))
					text = " task text here";
				else
					text = " list text here";
				$input.range(leading + text).range(selected.start+leading.length+1, selected.start+leading.length+1+text.length);
			}
			$input.focus();
			gitplex.server.markdown.fireInputEvent($input);
		});

		$head.find(".do-code").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				var value = $input.val();
				if (selected.start-1>=0 && selected.end<=value.length-1 
						&& value.charAt(selected.start-1) === '`' 
						&& value.charAt(selected.end) === '`') {
					$input.range(selected.start-1, selected.end+1).range(selected.text);
				} else {
					$input.range('`' + selected.text + '`').range(selected.start+1, selected.end+1);
				}
			} else {
				$input.range("`code text here`").range(selected.start+1, selected.end+1+"code text here".length);
			}
			$input.focus();
			gitplex.server.markdown.fireInputEvent($input);
		});
		
		$head.find(".do-quote").click(function() {
			var selected = $input.range();
			if (selected.length != 0)
				$input.range("> " + selected.text).range(selected.start+2, selected.end+2);
			else
				$input.range("> quote here").range(selected.start+2, selected.start+2+"quote here".length);
			$input.focus();
			gitplex.server.markdown.fireInputEvent($input);
		});
		
		$head.find(".do-emoji").click(function() {
			if (!$emojis.hasClass("loaded") && !$emojis.hasClass("loading")) {
				$emojis.addClass("loading");
				$emojis.html("Loading emojis...");
				callback("loadEmojis");
			}
			$emojis.toggle();
			$head.find(".do-emoji").toggleClass("active");
			if (!$resizeHandles.is(":visible"))
				gitplex.server.markdown.autofit(containerId, $container.outerWidth(), $container.outerHeight());
		});
		
		$head.find(".do-help").click(function() {
			$(this).toggleClass("active");
			$help.toggle();
			if (!$resizeHandles.is(":visible"))
				gitplex.server.markdown.autofit(containerId, $container.outerWidth(), $container.outerHeight());
		});

		function getReplacement() {
			var $replacement = $container.closest("form");
			if ($replacement.length == 0)
				$replacement = $container;
			return $replacement;
		}
		
		$head.find(".do-fullscreen").click(function() {
			var $replacement = getReplacement();
			if ($container.hasClass("fullscreen")) {
				$("body").removeClass("fullscreen");
				$container.removeClass("fullscreen");
				var $placeholder = $("#" + containerId + "-placeholder");
				
				$replacement.insertBefore($placeholder);
				$placeholder.remove();
				
				$(this).removeClass("active");
				if ($container.data("compactModePreviously")) {
					$container.removeClass("normal-mode");
					$container.addClass("compact-mode");
				} 
				if ($container.data("resizeHandlesVisiblePreviously"))  
					$resizeHandles.show();
				
				if ($resizeHandles.is(":visible")) {
					$container.css("width", "inherit").css("height", "inherit");
					gitplex.server.markdown.restoreSizeFromCookie($container);
				} else {
					$(window).resize();
				}
			} else {
				$container.addClass("fullscreen");
				$container.data("resizeHandlesVisiblePreviously", $resizeHandles.is(":visible"));
				if ($container.hasClass("compact-mode")) {
					$container.removeClass("compact-mode");
					$container.addClass("normal-mode");
					$container.data("compactModePreviously", true);
				} else {
					$container.data("compactModePreviously", false);
				}
				$resizeHandles.hide();
				var $placeholder = $("<div id='" + containerId + "-placeholder'></div>");
				$placeholder.insertAfter($replacement);
				$("body").append($replacement).addClass("fullscreen");
				$(this).addClass("active");
				$(window).resize();
			}
			if ($input.is(":visible"))
				$input.focus();
		});
		
		$(window).resize(function() {
			if ($container.hasClass("fullscreen")) {
				gitplex.server.markdown.autofit(containerId, $(window).width(), $(window).height());
			}
		});

		if (!canMentionUser)
			$head.find(".do-mention").remove();
		if (!canReferencePullRequest)
			$head.find(".do-hashtag").remove();
			
		$head.find(".do-mention, .do-hashtag").click(function() {
			if (!$edit.is(":visible")) 
				return;

			var atChar = $(this).hasClass("do-mention")? "@": "#";
			var prevChar;
			var caret = $input.caret();
			if (caret != 0) {
				prevChar = $input.val().charAt(caret-1);
			}
			if (prevChar === undefined || prevChar === ' ') {
				$input.caret(atChar);
			} else {
				$input.caret(" " + atChar);
			}
			$input.atwho("run");
			gitplex.server.markdown.fireInputEvent($input);
		});
		
		$head.find(".do-image, .do-link").click(function() {
	       	if ($(this).hasClass("do-image"))
	       		callback("selectImage");
	       	else
	       		callback("selectLink");
		});

		$input[0].cachedEmojis = [];

	    $input.atwho({
	    	at: ':',
	        callbacks: {
	        	remoteFilter: function(query, renderCallback) {
            		$container.data("atWhoEmojiRenderCallback", renderCallback);
                	callback("emojiQuery", query);
	        	}
	        },
	        displayTpl: "<li><i class='emoji' style='background-image:url(${url})'></i> ${name} </li>",
	        insertTpl: ':${name}:',
	        limit: atWhoLimit
	    });		
	    
	    if (canMentionUser) {
		    $input.atwho({
		    	at: '@',
		    	searchKey: "searchKey",
		        callbacks: {
		        	remoteFilter: function(query, renderCallback) {
		        		$container.data("atWhoUserRenderCallback", renderCallback);
		            	callback("userQuery", query);
		        	}
		        },
		        displayTpl: function(dataItem) {
		        	if (dataItem.fullName) {
		        		return "<li><span class='avatar'><img src='${avatarUrl}'/></span> ${name} <small>${fullName}</small></li>";
		        	} else {
		        		return "<li><span class='avatar'><img src='${avatarUrl}'/></span> ${name}</li>";
		        	}
		        },
		        limit: atWhoLimit
		    });	
	    } 

	    if (canReferencePullRequest) {
		    $input.atwho({
		    	at: '#',
		    	searchKey: "searchKey",
		        callbacks: {
		        	remoteFilter: function(query, renderCallback) {
		        		$container.data("atWhoRequestRenderCallback", renderCallback);
		            	callback("requestQuery", query);
		        	}
		        },
		        displayTpl: "<li><span class='text-muted'>#${requestNumber}</span> - ${requestTitle}</li>",
		        insertTpl: '#${requestNumber}', 
		        limit: atWhoLimit
		    });		
	    }
	    
	    if (attachmentSupport) {
	    	var inputEl = $input[0];
	    	
			inputEl.addEventListener("paste", function(e) {
				for (var i = 0; i < e.clipboardData.items.length; i++) {
					var item = e.clipboardData.items[i];
					if (item.type.indexOf("image") != -1) {
						var file = item.getAsFile();
						if (!file.name) {
							if (item.type.indexOf("png") != -1)
								file.name = "image.png";
							else if (item.type.indexOf("gif") != -1)
								file.name = "image.gif";
							else
								file.name = "image.jpg";
						}
						uploadFile(file);
						break;
					}
				}
			});
			
			inputEl.addEventListener("dragover", function(e) {
				$input.addClass("drag-over");
				e.stopPropagation();
				e.preventDefault();		
			}, false);
			
			inputEl.addEventListener("dragleave", function(e) {
				$input.removeClass("drag-over");
				e.stopPropagation();
				e.preventDefault();		
			}, false);
			
			inputEl.addEventListener("drop", function(e) {
				$input.removeClass("drag-over");
				e.stopPropagation();
				e.preventDefault();		
				var files = e.target.files || e.dataTransfer.files;
				if (files && files.length != 0)
					uploadFile(files[0]);
			}, false);
			
			function uploadFile(file) {
				if (file.size> attachmentMaxSize) {
					var message = "!!Upload should be less than " + Math.round(attachmentMaxSize/1024/1024) + " Mb!!";
					gitplex.server.markdown.updateUploadMessage($input, message);
				} else {
					var xhr = new XMLHttpRequest();
					var val = $input.val();
					var i=1;
					var message = "[Uploading file...]";
					while (val.indexOf(message) != -1) {
						message = "[Uploading file" + (++i) + "...]";
					}

					xhr.replaceMessage = message;
					if ($input.range().length == 0) {
						$input.caret(message);
					} else {
						$input.range(message);
						$input.caret($input.caret()+message.length);
					}
					
					xhr.onload = function() {
						if (xhr.status == 200) { 
							callback("insertUrl", xhr.responseText, xhr.replaceMessage);
						} else { 
							gitplex.server.markdown.updateUploadMessage($input, 
									"!!" + xhr.responseText + "!!", xhr.replaceMessage);
						}
					};
					xhr.onerror = function() {
						gitplex.server.markdown.updateUploadMessage($input, 
								"!!Unable to connect to server!!", xhr.replaceMessage);
					};
					xhr.open("POST", "/attachment_upload", true);
					xhr.setRequestHeader("File-Name", encodeURIComponent(file.name));
					xhr.setRequestHeader("Attachment-Support", attachmentSupport);
					xhr.send(file);
				}
			}
	    }		
	},
	
	/*
	 * Sync preview scroll bar with input scroll bar so that the text at input caret
	 * is always visible in preview window
	 */ 
	syncPreviewScroll: function(containerId) {
		var $preview = $("#" + containerId + ">.body>.preview");
		var $rendered = $preview.children(".markdown-rendered");
		var $edit = $("#" + containerId + ">.body>.edit");
		var $input = $edit.children("textarea");
		var caret;
		var caretOffset; // offset in pixel from caret to input top border
		if ($edit.is(":visible")) {
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
			caret = $rendered.data("caret");
			caretOffset = $rendered.data("caretOffset");
		}
		var $blockNearCaret;
		$rendered.find("[data-sourcestart]").each(function() {
			var sourceStart = parseInt($(this).data("sourcestart"));
			if (sourceStart <= caret) {
				$blockNearCaret = $(this);
			}
		});
		
		if ($blockNearCaret) {
			/*
			 * Found a block nearby caret. Below logic adjusts the scroll offset to make sure that
			 * the block is visible and try to adjust its position to stay on the same height with
			 * input caret for better user experience  
			 */
			var blockTop = $blockNearCaret.offset().top + $rendered.scrollTop() - $rendered.offset().top;
			var blockBottom = blockTop + $blockNearCaret.outerHeight();

			var scrollTop;
			if (parseInt($blockNearCaret.data("sourceend")) <= caret) {
				/*
				 * We are behind the block, so we will make sure that bottom of the block is 
				 * always visible
				 */
				scrollTop = blockTop - caretOffset;
				if (blockBottom - scrollTop > $rendered.height()) {
					scrollTop = blockBottom - $rendered.height(); 
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
		} else {
			scrollTop = 0;
		}

		$rendered.scrollTop(scrollTop);
    },
    restoreSizeFromCookie: function($container) {
    	var $body = $container.children(".body");
		var $edit = $body.children(".edit");
		var $input = $edit.children("textarea");
		var $preview = $body.children(".preview");
		var $rendered = $preview.children(".markdown-rendered");

		var inputHeight = Cookies.get(gitplex.server.markdown.getCookiePrefix($container)+".inputHeight");
		if (inputHeight) {
			$input.outerHeight(parseInt(inputHeight));
		} else {
			$input.outerHeight(100);
		}
		$edit.outerHeight($input.outerHeight());
		var renderedHeight = Cookies.get(gitplex.server.markdown.getCookiePrefix($container)+".renderedHeight");
		if (renderedHeight) {
			$rendered.outerHeight(parseInt(renderedHeight));
		} else {
			$rendered.outerHeight(100);
		}
		$preview.outerHeight($rendered.outerHeight());
    },
	onWindowLoad: function(containerId) {
		var $container = $("#" + containerId);
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $warning = $head.children(".warning");
		var $rendered = $body.find(">.preview>.markdown-rendered");
		var $input = $body.find(">.edit>textarea");
		var $resizeHandles = $container.find(".ui-resizable-handle");

		if ($resizeHandles.is(":visible")) {
			gitplex.server.markdown.restoreSizeFromCookie($container);
		}
		
		var autosaveKey = $container.data("autosaveKey");
		if (autosaveKey) {
			gitplex.server.form.registerAutosaveKey($container.closest("form.leave-confirm"), autosaveKey);
			var autosaveValue = localStorage.getItem(autosaveKey);
			if (autosaveValue) {
				$input.val(autosaveValue);
				$warning.show();		
				if (!$resizeHandles.is(":visible"))
					gitplex.server.markdown.autofit(containerId, $container.outerWidth(), $container.outerHeight());
				gitplex.server.markdown.fireInputEvent($input);
			}
		}
	},
	onRendered: function(containerId, html) {
		var $preview = $("#" + containerId + ">.body>.preview");
		var $rendered = $preview.children(".markdown-rendered");
		
		var existingImages = {};
		$rendered.find("img").each(function() {
			var key = this.outerHTML;
			var elements = existingImages[key];
			if (!elements)
				elements = [];
			elements.push(this);
			existingImages[key] = elements;
		});
		
		$rendered.html(html);
		gitplex.server.markdown.initRendered($rendered);

		// Avoid loading existing image
		$rendered.find("img").each(function() {
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
		
		$rendered.find("img").on("load", function() {
            gitplex.server.markdown.syncPreviewScroll(containerId);
        });
        
	},
	initRendered: function($rendered) {
		gitplex.server.highlight($rendered);

		$rendered.find("a").click(function() {
			gitplex.server.viewState.getFromViewAndSetToHistory();
		});
	},
	onViewerDomReady: function(containerId, taskCallback, taskSourcePositionDataAttribute) {
		var $container = $("#" + containerId);
		
		if (taskCallback) {
			var $task = $container.find(".task-list-item");
			var $taskCheckbox = $task.children("input");
			$taskCheckbox.removeAttr("disabled").removeAttr("readonly");
			$taskCheckbox.change(function() {
				taskCallback($(this).parent().data(taskSourcePositionDataAttribute), $(this).prop("checked"));
			});	
		}
		
		gitplex.server.markdown.initRendered($container.find(".markdown-rendered"));
	},
	onEmojisLoaded: function(containerId, emojis) {
		var $container = $("#" + containerId);
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $edit = $body.children(".edit");
		var $input = $edit.children("textarea");
		var $emojis = $head.children(".emojis");
		var $resizeHandles = $container.find(".ui-resizable-handle");
		
		var contentHtml = "";
		for (var i in emojis) {
			var emoji = emojis[i];
			contentHtml += "<a class='emoji' title='" + emoji.name + "'><img src='" + emoji.url + "'></img></a> ";
		}
		$emojis.html(contentHtml);
		$emojis.removeClass("loading");
		$emojis.addClass("loaded");
		$emojis.find(".emoji").click(function() {
			if (!$edit.is(":visible")) 
				return;
			
			$input.caret(":" + $(this).attr("title") + ": ");
			gitplex.server.markdown.fireInputEvent($input);
		});
		if (!$resizeHandles.is(":visible"))
			gitplex.server.markdown.autofit(containerId, $container.outerWidth(), $container.outerHeight());
	},
	insertUrl: function(containerId, isImage, url, name, replaceMessage) {
		var $head = $("#" + containerId + ">.head");
		var $body = $("#" + containerId + ">.body");
		var $input = $body.find(">.edit>textarea");

    	var sanitizedUrl = $('<div>'+url+'</div>').text();
    	var message;
    	var defaultDescription = "Enter description here";
    	if (name)
    		message = '['+name+']('+sanitizedUrl+')';
    	else
    		message = '[' + defaultDescription + ']('+sanitizedUrl+')';

    	if (isImage)
    		message = "!" + message;
    	
    	gitplex.server.markdown.updateUploadMessage($input, message, replaceMessage);
    	
    	if (!name) {
    		var offset = isImage?2:1;
    		$input.range($input.caret()-message.length+offset, $input.caret()-message.length+defaultDescription.length+offset);
    	}
    	
		gitplex.server.markdown.fireInputEvent($input);
	}, 
	updateUploadMessage: function($input, message, replaceMessage) {
		var isError = message.indexOf("!!") == 0;
		var pos = $input.val().indexOf(replaceMessage);
		if (pos != -1) {
			var currentPos = $input.caret();
			$input.range(pos, pos+ replaceMessage.length).range(message);
			if (!isError) {
				if (currentPos<pos)
					$input.caret(currentPos);
				else if (currentPos>pos+replaceMessage.length)
					$input.caret(currentPos + message.length - replaceMessage.length);
				else 
					$input.caret($input.caret()+message.length);
			}
		} else {
			if ($input.range().length != 0) {
				$input.range(message);
				if (!isError)
					$input.caret($input.caret() + message.length);
			} else {
				$input.caret(message);
				if (isError)
					$input.range($input.caret()-message.length, $input.caret());
			}
		} 
	},
	onFileUploadDomReady: function(uploadId, maxSize, maxSizeForDisplay) {
		var $upload = $('#' + uploadId);
		$upload.change(function() {
			var $feedback = $upload.closest('form').find(".feedback");
			if ($upload[0].files[0].size>maxSize) {
				$feedback.html("<div class='alert alert-danger'>Size of upload file should be less than " 
						+ maxSizeForDisplay + "<button type='button' class='close' data-dismiss='alert' aria-label='Close'>" +
								"<span aria-hidden='true'>&times;</span></button></div>");
			} else {
				$feedback.empty();
				$upload.next().click();
			}
		})
	}
	
};