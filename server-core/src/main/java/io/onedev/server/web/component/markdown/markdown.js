onedev.server.markdown = {
	shortcuts: { // cmd (Mac) or ctrl (non-Mac) plus below keys
		"b": ".do-bold",
		"shift-k": ".do-link",
		"shift-i": ".do-image",
		"i": ".do-italic",
		"e": ".do-code",
		"shift-.": ".do-quote",
		"shift-7": ".do-orderlist",
		"shift-8": ".do-list",
		"shift-l": ".do-tasklist"
	},
	getCookiePrefix: function($container) {
		if ($container.hasClass("compact-mode"))
			return "markdownEditor.compactMode";
		else
			return "markdownEditor.normalMode";
	},
	getBlockPrefix: function($input) {
		var selected = $input.range();
		var input = $input.val();
		var pos = selected.start;
		if (pos == 0
				|| pos == 1 && input.charAt(0) == '\n'	
				|| pos >= 2 && input.charAt(pos-1) == '\n' && input.charAt(pos-2) == '\n') {
			return ""; 
		} else if (pos >= 2 && input.charAt(pos-1) == '\n' && input.charAt(pos-2) != '\n') {
			return "\n";			
		} else {
			return "\n\n";				
		}
	},
	getAtWhoPrefix: function($input) {
		var selected = $input.range();
		var beforeSelection = $input.val().substring(0, selected.start);
		if (/(^|\s|\p{P})$/u.test(beforeSelection)) 
			return "";
		else
			return " ";
	},
	getBlockSuffix: function($input) {
		var selected = $input.range();
		var input = $input.val();
		var pos = selected.end;
		var len = input.length;
		if (pos == len
			|| pos == len-1 && input.charAt(len-1) == '\n'
			|| pos <= len-2 && input.charAt(pos) == '\n' && input.charAt(pos+1) == '\n') {
			return "";
		} else if (pos <= len-2 && input.charAt(pos) == '\n' && input.charAt(pos+1) != '\n') {
			return "\n";
		} else {
			return "\n\n";
		}
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
	onDomReady: function(containerId, callback, atWhoLimit, attachmentUploadUrl, 
			attachmentMaxSize, canMentionUser, canReferenceEntity, 
			projectPathPattern, projectKeyPattern, autosaveKey, translations) {
		onedev.server.markdown.translations = translations;
		var $container = $("#" + containerId);
		var useFixedWidthFontCookieName = "markdownEditor.useFixedWidthFont";
		var useFixedWidthFont = Cookies.get(useFixedWidthFontCookieName);
		if (useFixedWidthFont && useFixedWidthFont == "true")	
			$container.addClass("fixed-width");
		
		$container.data("callback", callback);
		$container.data("autosaveKey", autosaveKey);
		
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $editLink = $head.find(".edit");
		var $previewLink = $head.find(".preview");
		var $splitLink = $head.find(".split");
		var $edit = $body.children(".edit");
		var $input = $edit.children("textarea");
		var $preview = $body.children(".preview");
		var $rendered = $preview.children(".markdown-rendered");
		var $help = $head.children(".help");
		var $suggestion = $head.find(".do-suggestion");
		
		$head.find(".dropdown>a").dropdown();
		
		if ($suggestion.attr("disabled") == "disabled") {
			$suggestion.attr("data-tippy-content", onedev.server.markdown.translations["commented-code-outdated"]);
		} else {
			if (onedev.server.util.isMac())		
				$suggestion.attr("data-tippy-content", onedev.server.markdown.translations["suggest-changes"] + " (cmd-g)");
			else
				$suggestion.attr("data-tippy-content", onedev.server.markdown.translations["suggest-changes"] + " (ctrl-g)");
				
			$suggestion.click(function() {
				var content = $(this).data("content");
				var from = parseInt($(this).data("from"));
				var to = parseInt($(this).data("to"));
				var langHint = "suggestion";
				$input.focus();
				var selected = $input.range();
				var prefix = onedev.server.markdown.getBlockPrefix($input);
				var suffix = onedev.server.markdown.getBlockSuffix($input);
				if (suffix == "")
					suffix = "\n";
				document.execCommand("insertText", false, prefix + "```" + langHint + "\n" + content + "\n```" + suffix);		
				$input.range(selected.start + prefix.length + 4 + langHint.length + from, selected.start + prefix.length + 4 + langHint.length + to);
				onedev.server.markdown.fireInputEvent($input);
			});
		}

		$editLink.click(function() {
			$container.removeClass("preview-mode").removeClass("split-mode").addClass("edit-mode");
			$input.focus();
			$editLink.addClass("active");
			$previewLink.removeClass("active");
			$splitLink.removeClass("active");
			Cookies.set(onedev.server.markdown.getCookiePrefix($container)+".split", false, {expires: Infinity});
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
			
			onedev.server.perfectScrollbar.empty($rendered);
			$rendered.html("<div class='message'>" + onedev.server.markdown.translations["loading"] + "</div>");
			
			$editLink.removeClass("active");
			$previewLink.addClass("active");
			$splitLink.removeClass("active");
			callback("render", $input.val());
		});
		$splitLink.click(function() {
			$container.removeClass("edit-mode").removeClass("preview-mode").addClass("split-mode");
			
			$input.focus();
			
			$rendered.html("<div class='message'>" + onedev.server.markdown.translations["loading"] + "</div>");
			
			$editLink.removeClass("active");
			$previewLink.removeClass("active");
			$splitLink.addClass("active");
			callback("render", $input.val());
			Cookies.set(onedev.server.markdown.getCookiePrefix($container)+".split", true, {expires: Infinity});
		});
		
		onedev.server.markdown.setupActionMenu($container, $head.find(".normal-mode"));

		var previewTimeout = 500;
		$input.doneEvents("input inserted.atwho", function() {
			function render() {
				if (autosaveKey) {
					var content = $input.val();
					if (content.trim().length != 0)
						localStorage.setItem(autosaveKey, content);
					else
						localStorage.removeItem(autosaveKey);
				}
				
				/* 
				 * in case an ajax call is ongoing we postpone the render 
				 * as the ongoing call may alter the component layout
				 */
				if (onedev.server.ajaxRequests.count != 0) {  
					setTimeout(render, 10);
				} else if ($preview.is(":visible")) {
					callback("render", $input.val());
				}
			}

			render();
		}, previewTimeout);
		
		$input.doneEvents("keydown", function(e) {
			if (e.keyCode>=33 && e.keyCode<=40 && $preview.is(":visible") 
					&& $(".atwho-view").filter(":visible").length == 0) {
				// Only sync preview scroll when we moved cursor
				onedev.server.markdown.syncPreviewScroll(containerId);
			}
		}, previewTimeout);
		
		$input.doneEvents("click focus", function(e) {
			if ($preview.is(":visible")) {
				onedev.server.markdown.syncPreviewScroll(containerId);
			}
		}, previewTimeout);
		
		function getSubmit() {
			return $input.closest("form")
					.find(">.btn-primary, :last-child>.btn-primary")
					.not("[disabled=disabled]");
		}
		
	    var fontSize = parseInt(getComputedStyle($input[0]).getPropertyValue('font-size'));
	    
		/*
		 * We intercept the "Enter" key event to do below things to make editing more convenient:
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
			if ((!e.metaKey && !e.ctrlKey || getSubmit().length == 0) && $(".atwho-view, .floating>.content>.input-assist").filter(":visible").length == 0) {
				if (e.keyCode == 13) {
					var scrollTop = $input.scrollTop();

					e.preventDefault();
					var input = $input.val();
					var caret = $input.caret();
					var inputBeforeCaret = input.substring(0, caret);
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
						$input.range(lastLineBreakPos+1, caret);
						document.execCommand("insertText", false, "\n");
					}

					// match against task items
					var match = /^[-*]\s+\[[x ]\] /.exec(nonSpaceInCurrentLine);
					if (match != null) {
						if (nonSpaceInCurrentLine.length > match[0].length) {
							if (nonSpaceInCurrentLine.indexOf("*") == 0)
								document.execCommand("insertText", false, "\n" + spaces + "* [ ] ");
							else
								document.execCommand("insertText", false, "\n" + spaces + "- [ ] ");
						} else {
							clearLastLine();
						}
					} else {
						if (nonSpaceInCurrentLine.indexOf("* ") == 0) {
							if (nonSpaceInCurrentLine.length > 2) {
								document.execCommand("insertText", false, "\n" + spaces + "* ");
							} else {
								clearLastLine();
							}
						} else if (nonSpaceInCurrentLine.indexOf("- ") == 0) {
							if (nonSpaceInCurrentLine.length > 2) {
								document.execCommand("insertText", false, "\n" + spaces + "- ");
							} else {
								clearLastLine();
							}
						} else {
							// match against ordered list items
							match = /^\d+\. /.exec(nonSpaceInCurrentLine);
							if (match != null) {
								if (nonSpaceInCurrentLine.length > match[0].length) {
									document.execCommand("insertText", false, "\n" + spaces + (parseInt(match[0])+1) +". ");
								} else {
									clearLastLine();
								}
							} else if (nonSpacePosInCurrentLine == inputBeforeCaret.length) {
								document.execCommand("insertText", false, "\n");
							} else {
								document.execCommand("insertText", false, "\n" + spaces);
							}
						}
					}

					// Scroll if necessary to make cursor visible
					var caretBottom = getCaretCoordinates($input[0], $input.caret()).top + fontSize;
					if (caretBottom > scrollTop + $input.height()) {
						$input.scrollTop(caretBottom - $input.height());
					} else {
						$input.scrollTop(scrollTop);
					}
				} else if (e.keyCode == 9) {
					var selection = $input.range();
					if (selection.length != 0) {
						var content = $input.val();
						var selectedContent = content.substring(selection.start, selection.end);
						var tabbedContent = "";
						selectedContent.split("\n").forEach(function(line) {
							if (e.shiftKey) {
								if (line.startsWith("\t"))
									line = line.substring(1);
								else if (line.startsWith(" "))
									line = line.substring(1);
							} else {
								line = " " + line;
							}
							if (tabbedContent.length != 0)
								tabbedContent += "\n";
							tabbedContent += line;
						});
						document.execCommand("insertText", false, tabbedContent);
						$input.range(selection.start, selection.start + tabbedContent.length);
						return false;
					} 
				}	
			}
		});

		$container.on("nameChanging", function() {
			/*
			 * Re-render the markdown in case of name changing, as rendering process 
			 * may depend on name associated with the markdown. For instance, the 
			 * markdown editor might be used as a blob editor, and it might be 
			 * referencing relative paths to other blob files. A name change might 
			 * change the directory structure of the file being edited as well, hence
			 * affect the relative path rendering
			 */
			onedev.server.markdown.fireInputEvent($input);
		});
		
		var minHeight = 75;
		$body.resizable({
			autoHide: false,
			handles: {"s": $body.children(".ui-resizable-handle")},
			minHeight: minHeight,
			stop: function() {
				Cookies.set(onedev.server.markdown.getCookiePrefix($container)+".bodyHeight", 
						$body.height(), {expires: Infinity});
			}
		});
		
		$edit.resizable({
			autoHide: false,
			handles: {"s": $edit.children(".ui-resizable-handle")},
			minHeight: minHeight,
			stop: function() {
				Cookies.set(onedev.server.markdown.getCookiePrefix($container)+".editHeight", 
						$edit.height(), {expires: Infinity});
			}
		});
		
		$preview.resizable({
			autoHide: false,
			handles: {"s": $preview.children(".ui-resizable-handle")},
			minHeight: minHeight,
			stop: function() {
				Cookies.set(onedev.server.markdown.getCookiePrefix($container)+".previewHeight", 
						$preview.height(), {expires: Infinity});
			}
		});

		var $doFixedWidth = $head.find(".do-fixedwidth");
		if ($container.hasClass("fixed-width"))
			$doFixedWidth.addClass("active");
		$doFixedWidth.click(function() {
			$container.toggleClass("fixed-width");
			$(this).toggleClass("active");
			Cookies.set(useFixedWidthFontCookieName, $container.hasClass("fixed-width"), {expires: Infinity});
		});
		$head.find(".do-help").click(function() {
			$(this).toggleClass("active");
			$help.toggle();
		});

		$head.find(".do-fullscreen").click(function() {
			var $replacement = $container.closest("form");
			if ($replacement.length == 0)
				$replacement = $container;
			
			if ($container.hasClass("fullscreen")) {
				$container.removeClass("fullscreen");
				var $placeholder = $("#" + containerId + "-placeholder");
				
				$replacement.insertBefore($placeholder);
				$placeholder.remove();
				
				$(this).removeClass("active");
				if ($container.data("compactModePreviously")) {
					$container.removeClass("normal-mode");
					$container.addClass("compact-mode");
				} 
			} else {
				$container.addClass("fullscreen");
				if ($container.hasClass("compact-mode")) {
					$container.removeClass("compact-mode");
					$container.addClass("normal-mode");
					$container.data("compactModePreviously", true);
				} else {
					$container.data("compactModePreviously", false);
				}
				var $placeholder = $("<div id='" + containerId + "-placeholder'></div>");
				$placeholder.insertAfter($replacement);
				$("body").append($replacement);
				$(this).addClass("active");
			}
			
			if ($input.is(":visible"))
				$input.focus();
		});

		$input.on("keydown", function(e) {
			if ($(".atwho-view").filter(":visible").length == 0) {
				if (onedev.server.util.isMac() && e.metaKey || !onedev.server.util.isMac() && e.ctrlKey) {
					var key;
					if (e.keyCode >= 48 && e.keyCode <= 57)
						key = String.fromCharCode(e.keyCode);
					else if (e.keyCode >= 65 && e.keyCode <= 90)
						key = String.fromCharCode(e.keyCode+32);
					else if (e.keyCode == 190)
						key = ".";
					else
						key = "";
					if (e.shiftKey)
						key = "shift-" + key;
					if (key == "g") {
						e.preventDefault();
						$suggestion.click();						
					} else {
						var selector = onedev.server.markdown.shortcuts[key];
						if (selector) {
							e.preventDefault();
							$head.find(selector).click();
						}
					}
				}
				var $submit = getSubmit();
				if ((e.metaKey || e.ctrlKey) && e.keyCode == 13 && $submit.length != 0) {
					e.preventDefault();
					$submit.click();
				}		
			}
			
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
	        displayTpl: "<li><span class='emoji'>${unicode}</span> ${name} </li>",
	        insertTpl: ":${name}: ",
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
				insertTpl: "@${name} ",
		        limit: atWhoLimit
		    });	
	    } 	    

	    if (canReferenceEntity) {
	    	function matchReference(atChar) {
	    		var input = $input.val().substring(0, $input.caret());
	    		var match;
				if (atChar === '#')
					match = new RegExp("(^|\\W+)((?<type>pull\\s*request|pr|issue|build)\\s+)?(?<project>" + projectPathPattern + ")?#(?<query>\\S*)$", 'gi').exec(input);
				else
					match = new RegExp("(^|\\W+)((?<type>pull\\s*request|pr|issue|build)\\s+)?(?<project>" + projectKeyPattern + ")-(?<query>\\S*)$", 'gi').exec(input);					
	    		if (match) {
					var index = match.index + match[1].length;
					if (match[2])
						index += match[2].length;
	    			var type = match.groups.type;
	    			if (type)
	    				type = type.replace(/\s+/g, '').toLowerCase();
	    			return {
	    				type: type,
	    				project: match.groups.project,
	    				query: match.groups.query,
						index: index
	    			}
	    		}
	    	}
			function remoteFilterReference(atChar, query, renderCallback) {
				$container.data("atWhoReferenceRenderCallback", renderCallback);
				var match = matchReference(atChar);
				if (match)
					callback("referenceQuery", atChar, match.query, match.type, match.project);
			}
			function beforeInsertReference(atChar) {
				$input.focus();
				var match = matchReference(atChar);
				if (match) {
					var content = $input.val();
					if ($input.caret() != content.length) {
						$input.range(match.index, $input.caret());
						document.execCommand("insertText", false, "");
					} else {
						// Fix OD-2225
						$input.val(content.substring(0, match.index));
						$input.caret(content.length-1);
					}
				}
			}
		    $input.atwho({
		    	at: '#',
		    	startWithSpace: false,
		    	searchKey: "searchKey",
		        callbacks: {
		        	remoteFilter: function(query, renderCallback) {
						remoteFilterReference('#', query, renderCallback);
		        	},
					beforeInsert: function(value) {
						beforeInsertReference('#');
						return value;
					}
		        },
		        displayTpl: function() {
					return "<li>${title} (${reference})</li>";
		        },
		        insertTpl: function() {
					return "${reference} ";
		        }, 
		        limit: atWhoLimit
		    });		   
			$input.atwho({
				at: '-',
				startWithSpace: false,
				searchKey: "searchKey",
				callbacks: {
					remoteFilter: function(query, renderCallback) {
						remoteFilterReference('-', query, renderCallback);
					},
					beforeInsert: function(value) {
						beforeInsertReference('-');
						return value;
					}
				},
				displayTpl: function() {
					return "<li>${title} (${reference})</li>";
				},
				insertTpl: function() {
					return "${reference} ";
				},
				limit: atWhoLimit
			});
		}
	    
	    if (attachmentUploadUrl) {
	    	var inputEl = $input[0];
	    	
			inputEl.addEventListener("paste", function(e) {
				var hasImage = false;
				for (var i = 0; i < e.clipboardData.items.length; i++) {
					var item = e.clipboardData.items[i];
					if (item.type.indexOf("image") != -1) {
						hasImage = true;
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
				if (hasImage) {
					e.preventDefault();
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
					var message = "!!" + onedev.server.markdown.translations["upload-should-be-less-than"] + "!!";
					onedev.server.markdown.updateUploadMessage($input, message.replace("{0}", Math.round(attachmentMaxSize/1024/1024)));
				} else {
					var xhr = new XMLHttpRequest();
					var val = $input.val();
					var i=1;
					var message = "[" + onedev.server.markdown.translations["uploading-file"] + "...]";
					while (val.indexOf(message) != -1) {
						message = "[" + onedev.server.markdown.translations["uploading-file"] + (++i) + "...]";
					}

					xhr.replaceMessage = message;
					$input.focus();
					if ($input.range().length == 0) {
						document.execCommand("insertText", false, message);
					} else {
						document.execCommand("insertText", false, message);
						$input.caret($input.caret()+message.length);
					}
					
					xhr.onload = function() {
						var response = xhr.responseText;
						var index = response.indexOf("<?xml");
						if (index != -1)
							response = response.substring(0, index);
						if (xhr.status == 200) { 
							callback("insertUrl", response, xhr.replaceMessage);
						} else { 
							onedev.server.markdown.updateUploadMessage($input, 
									"!!" + response + "!!", xhr.replaceMessage);
						}
					};
					xhr.onerror = function() {
						onedev.server.markdown.updateUploadMessage($input, 
								"!!" + onedev.server.markdown.translations["unable-to-connect-to-server"] + "!!", xhr.replaceMessage);
					};
					
					xhr.open("POST", attachmentUploadUrl, true);
					xhr.setRequestHeader("File-Name", encodeURIComponent(file.name));
					xhr.setRequestHeader("Wicket-Ajax", "true");
					xhr.setRequestHeader("Wicket-Ajax-BaseURL", Wicket.Ajax.baseUrl);
					
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
	setupActionMenu: function($container, $actionMenu) {
		var callback = $container.data("callback");		
		
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $emojis = $head.children(".emojis");
		var $edit = $body.children(".edit");
		var $input = $edit.children("textarea");
			
		function closeMenu() {
			var closeCallback = $actionMenu.closest(".floating").data("closeCallback");	
			if (closeCallback)
				closeCallback();
		}
		
		$actionMenu.find(".do-bold").click(function() {
			$input.focus();
			var selected = $input.range();
			if (selected.length != 0) {
				document.execCommand("insertText", false, "**" + selected.text + "**");
			} else {
				document.execCommand("insertText", false, "****");
				$input.caret(selected.start + 2);
			}
			closeMenu();
			onedev.server.markdown.fireInputEvent($input);
		});
		
		$actionMenu.find(".do-italic").click(function() {
			$input.focus();
			var selected = $input.range();
			if (selected.length != 0) {
				document.execCommand("insertText", false, "_" + selected.text + "_");
			} else {
				document.execCommand("insertText", false, "__");
				$input.caret(selected.start + 1);
			}
			closeMenu();			
			onedev.server.markdown.fireInputEvent($input);
		});
		
		$actionMenu.find(".do-header").click(function() {
			$input.focus();
			var selected = $input.range();
			var prefix = onedev.server.markdown.getBlockPrefix($input);
			var suffix = onedev.server.markdown.getBlockSuffix($input);
			if (selected.length != 0) {
				document.execCommand("insertText", false, prefix + "### " + selected.text + suffix);
			} else {
				document.execCommand("insertText", false, prefix + "### " + suffix);
				$input.caret(selected.start + prefix.length + 4);
			}
			closeMenu();			
			onedev.server.markdown.fireInputEvent($input);
		});
		
		$actionMenu.find(".do-list, .do-orderlist, .do-tasklist").click(function() {
			$input.focus();
			var leading;
			if ($(this).hasClass("do-list"))
				leading = "- ";
			else if ($(this).hasClass("do-orderlist"))
				leading = "1. ";
			else
				leading = "- [ ] ";
			var prefix = onedev.server.markdown.getBlockPrefix($input);
			var suffix = onedev.server.markdown.getBlockSuffix($input);
			var selected = $input.range();
			if (selected.length != 0) {
				var splitted = selected.text.split("\n");
				var insert = "";
				for (var i in splitted) {
					if (splitted[i].trim().length != 0) {
						if (insert.length != 0)
							insert += "\n";
						insert += leading + " " + splitted[i];
					}
				}
				document.execCommand("insertText", false, prefix + insert + suffix);
			} else {
				document.execCommand("insertText", false, prefix + leading + suffix);
				$input.caret(selected.start + prefix.length + leading.length);
			}
			closeMenu();			
			onedev.server.markdown.fireInputEvent($input);
		});

		$actionMenu.find(".do-code").click(function() {
			$input.focus();
			var langHint = onedev.server.markdown.translations["programming-language"];
			var selected = $input.range();
			var prefix = onedev.server.markdown.getBlockPrefix($input);
			var suffix = onedev.server.markdown.getBlockSuffix($input);
			if (suffix == "")
				suffix = "\n";
			if (selected.length != 0) {
				document.execCommand("insertText", false, prefix + "```" + langHint + "\n" + selected.text + "\n```" + suffix);				
				$input.range(selected.start + prefix.length + 3, selected.start + prefix.length + 3 + langHint.length);
			} else {
				document.execCommand("insertText", false, prefix + "```" + langHint + "\n```" + suffix);				
				$input.range(selected.start + prefix.length + 3, selected.start + prefix.length + 3 + langHint.length);
			}
			closeMenu();			
			onedev.server.markdown.fireInputEvent($input);
		});
		
		$actionMenu.find(".do-quote").click(function() {
			$input.focus();
			var selected = $input.range();
			var prefix = onedev.server.markdown.getBlockPrefix($input);
			var suffix = onedev.server.markdown.getBlockSuffix($input);
			if (selected.length != 0) {
				document.execCommand("insertText", false, 
					prefix + onedev.server.markdown.getQuoted(selected.text) + suffix);				
			} else {
				document.execCommand("insertText", false, 
					prefix + "> " + suffix);				
				$input.caret(selected.start + prefix.length + 2);
			}
			
			closeMenu();			
			onedev.server.markdown.fireInputEvent($input);
		});
		
		$actionMenu.find(".do-emoji").click(function() {
			if (!$emojis.hasClass("loaded") && !$emojis.hasClass("loading")) {
				$emojis.addClass("loading");
				$emojis.html(onedev.server.markdown.translations["loading-emojis"]);
				callback("loadEmojis");
			}
			$emojis.toggle();
			$actionMenu.find(".do-emoji").toggleClass("active");
			closeMenu();			
		});
		
		$actionMenu.find(".do-mention").click(function() {
			closeMenu();
			if (!$edit.is(":visible")) 
				return;
			var prefix = onedev.server.markdown.getAtWhoPrefix($input);
			$input.focus();
			document.execCommand("insertText", false, prefix + "@");
			onedev.server.markdown.fireInputEvent($input);
			$input.atwho("run");
		});
		$actionMenu.find(".do-reference").click(function() {
			closeMenu();
			if (!$edit.is(":visible"))
				return;
			var text = onedev.server.markdown.getAtWhoPrefix($input);
			var type = $(this).data("type");
			if (type !== undefined)
				text += type + " ";
			var key = $(this).data("key");
			if (key !== undefined)
				text += key + "-";
			else
				text += "#";
			$input.focus();
			document.execCommand("insertText", false, text);
			onedev.server.markdown.fireInputEvent($input);
			$input.atwho("run");
		});
		
		$actionMenu.find(".do-image, .do-link").click(function() {
	       	if ($(this).hasClass("do-image"))
	       		callback("selectImage", $input.range().text);
	       	else
	       		callback("selectLink", $input.range().text);
			closeMenu();			
		});

		var prefix = onedev.server.util.isMac()?"cmd-":"ctrl-";		
		for (var key in onedev.server.markdown.shortcuts) {
			var selector = onedev.server.markdown.shortcuts[key];
			var $action = $actionMenu.find(selector);
			if ($actionMenu.closest(".floating").length != 0) 
				$action.append("<span class='float-right ml-4 text-monospace font-size-sm'>" + prefix + key + "</span>");
			else 
				$action.attr("data-tippy-content", $action.data("tippy-content") + " (" + prefix + key + ")");
		}

	},
	onLoad: function(containerId) {
		var $container = $("#" + containerId);
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $warning = $head.children(".warning");
		var $edit = $body.children(".edit");
		var $preview = $body.children(".preview");
		var $input = $edit.children("textarea");

		if ($body.find(".ui-resizable-handle:visible").length != 0) {
			var defaultHeight = 200;
			if ($container.hasClass("normal-mode")) {
				var bodyHeight = Cookies.get(onedev.server.markdown.getCookiePrefix($container)+".bodyHeight");
				if (bodyHeight) 
					$body.height(parseInt(bodyHeight));
				else 
					$body.height(defaultHeight);
			} else {
				var editHeight = Cookies.get(onedev.server.markdown.getCookiePrefix($container)+".editHeight");
				if (editHeight) 
					$edit.height(parseInt(editHeight));
				else
					$edit.height(defaultHeight);
					
				var previewHeight = Cookies.get(onedev.server.markdown.getCookiePrefix($container)+".previewHeight");
				if (previewHeight) 
					$preview.height(parseInt(previewHeight));
				else
					$preview.height(defaultHeight);
			}
		}
		var autosaveKey = $container.data("autosaveKey");
		if (autosaveKey) {
			var autosaveValue = localStorage.getItem(autosaveKey);
			if (autosaveValue && $input.val() != autosaveValue) {
				$input.val(autosaveValue);
				$warning.show();
				onedev.server.markdown.fireInputEvent($input);
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
		
		onedev.server.markdown.initRendered(containerId);

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
		
		onedev.server.markdown.syncPreviewScroll(containerId);
		
		$rendered.find("img").on("load", function() {
            onedev.server.markdown.syncPreviewScroll(containerId);
        });
	},
	initRendered: function(containerId, translations) {
		if (translations)
			onedev.server.markdown.translations = translations;

		var $container = $("#" + containerId);
		var $rendered = $container.find(".markdown-rendered");
		
		$rendered.find("span.header-anchor").parent().addClass("header-anchor");
		$rendered.find("a.header-anchor").each(function() {
			var $headerAnchor = $(this);
			$headerAnchor.before("<a href='" + $headerAnchor.attr("href") 
				+ "' class='header-link'><svg class='icon'><use xlink:href='" 
				+ onedev.server.icons + "#link'/></svg></a>");
		});
		
		$rendered.find("a").click(function() {
			onedev.server.viewState.getFromViewAndSetToHistory();
		});
		
		$rendered.find("img").each(function() {
			var $image = $(this);
			if ($image.closest("a").length == 0) {
		    	$image.click(function() {
		    		var $image = $(this);
		    		$image.parent().css("position", "relative");
					var width = $image.width();
					var height = $image.height();
					if (width < 64)
						width = 64;
					if (height < 64)
						height = 64;
		    		var $loadingIndicator = $("<div class='markdown-image-loading'></div>");
		    		$loadingIndicator.css("width", width).css("height", height);
		    		$image.parent().append($loadingIndicator);
		    		
		    	    var actualImage = new Image();
		    	    actualImage.onload = function() {	
						$loadingIndicator.remove();
						var $modal = $("" +
								"<div class='modal fade' role='dialog' tabindex='-1'>" +
								"  <div class='modal-dialog' style='width: " + (actualImage.width+2) + "px; max-width: 90%;'>" +
								"    <div class='modal-content' style='border-radius: 0;'>" +
								"      <div class='modal-body' style='padding: 0;'>" +
								"        <img src='" + actualImage.src + "' style='width: 100%;'></img>" +
								"      </div>" +
								"    </div>" +
								"  </div>" +
								"</div>");
						$("body").append($modal);
						$modal.find("img").click(function() {
							$modal.modal("hide");
						});
						$modal.modal('show').on('show.bs.modal', function() {
						}).on('hidden.bs.modal', function () {
							$modal.remove();
						});				
		    	    }
		    	    actualImage.onerror = function() {
		    	    	$loadingIndicator.remove();
		    	    }
		    	    actualImage.src = $image.attr("src");    		
				});
		    	$image.css("cursor", "pointer");
			}
		});
		
		$rendered.find("pre>code").each(function() {
			var $this = $(this);
			var suggestionFile = $this.data("suggestionfile");
			if (suggestionFile) {
				onedev.server.diff.highlightSyntax($this.children(".text-diff"), suggestionFile);
			} else {
				var modeName = $this.data("language");
				if (modeName) {
				    var modeInfo = CodeMirror.findModeByName(modeName);
			        if (modeInfo) { 
						onedev.server.codemirror.highlightSyntax($this.text(), modeInfo, this);
						$this.addClass("cm-s-eclipse");
					}
				}
			}
			var $actions = $("<div class='actions'></div>");
			$this.parent().append($actions);
			
			if (!$this.parent().hasClass("suggestion")) {
				var icon = "<svg class='icon'><use xlink:href='" + onedev.server.icons + "#copy'/></svg>";
				var $copy = $("<a class='pressable' data-tippy-content='" + onedev.server.markdown.translations["copy-to-clipboard"] + "'>" + icon + "</a>");
				$actions.append($copy);
				var options = {
					text: function() {
						return $this.text();
					}
				};
				var $modal = $copy.closest(".modal-dialog");
				if ($modal.length != 0) 
					options.container = $modal[0];		
				new ClipboardJS($copy[0], options);			
			}
			
			var suggestionCallback = $container.data("suggestionCallback");
			if (suggestionCallback) {
				if ($this.data("suggestionoutdated")) {
					var icon = "<svg class='icon icon-sm'><use xlink:href='" + onedev.server.icons + "#warning'/></svg>";
					var $warning = $("<a class='ml-2 link-gray' data-tippy-content='" + onedev.server.markdown.translations["suggestion-outdated"] + "'>" + icon + "</a>");
					$actions.append($warning);
				} else if ($this.data("suggestionapplyinbatch")) {
					var icon = "<svg class='icon'><use xlink:href='" + onedev.server.icons + "#minus-square'/></svg>";
					var $removeFromBatch = $("<a class='pressable ml-2' data-tippy-content='" + onedev.server.markdown.translations["remove-from-batch"] + "'>" + icon + "</a>");
					$removeFromBatch.click(function() {
						suggestionCallback("removeFromBatch");
					});
					$actions.append($removeFromBatch);
				} else if ($this.data("suggestionappliable")) {
					var icon = "<svg class='icon'><use xlink:href='" + onedev.server.icons + "#add-to-git'/></svg>";
					var $apply = $("<a class='pressable ml-2' data-tippy-content='" + onedev.server.markdown.translations["commit-suggestion"] + "'>" + icon + "</a>");
					$actions.append($apply);
					$apply.click(function() {
						suggestionCallback("apply", $this.data("suggestion"));
					});
					
					if ($this.data("suggestionbatchappliable")) {
						var icon = "<svg class='icon flip-x'><use xlink:href='" + onedev.server.icons + "#plus-square'/></svg>";
						var $addToBatch = $("<a class='pressable ml-2' data-tippy-content='" + onedev.server.markdown.translations["add-to-batch"] + "'>" + icon + "</a>");
						$addToBatch.click(function() {
							suggestionCallback("addToBatch", $this.data("suggestion"));
						});
						$actions.append($addToBatch);
					}
				}
			}
		});
		var $mermaid = $container.find(".mermaid");
		if ($mermaid.length != 0) 
			mermaid.init(undefined, $mermaid);
			
		var $katex = $container.find(".katex");
		if ($katex.legnth != 0) {
			$katex.each(function() {
	            katex.render($(this).text(), this, {throwOnError: false, displayMode: this.nodeName !== 'SPAN'});
			});
		}
	},
	onViewerDomReady: function(containerId, taskCallback, taskSourcePositionDataAttribute, referenceCallback, 
			suggestionCallback, translations) {
		onedev.server.markdown.translations = translations;
		var $container = $("#" + containerId);
		$container.data("suggestionCallback", suggestionCallback);
		
		var $rendered = $container.find(".markdown-rendered");
		
		var content = $rendered.data("content");
		
		if (content) 
			$rendered.html(content).removeData("content");
		
		if (taskCallback) {
			var $task = $container.find(".task-list-item");
			var $taskCheckbox = $task.children("input");
			$taskCheckbox.removeAttr("disabled").removeAttr("readonly");
			$taskCheckbox.change(function() {
				taskCallback($(this).parent().data(taskSourcePositionDataAttribute), $(this).prop("checked"));
			});	
		}
		
		var alignment = {targetX: 0, targetY: 0, x: 0, y: 100};
		$container.find(".reference").hover(function() {
			var $reference = $(this);
			var referenceType;
			var referenceId = $reference.data("reference");
			if ($reference.hasClass("issue")) {
				referenceType = "issue";
			} else if ($reference.hasClass("pull-request")) {
				referenceType = "pull request";
			} else if ($reference.hasClass("build")) {
				referenceType = "build";
			} else if ($reference.hasClass("mention")) {
				referenceType = "user";
			} else if ($reference.hasClass("commit")) {
				referenceType = "commit";
			}
			if (referenceType) {
				var $tooltip = $("<div id='reference-tooltip'>" + onedev.server.markdown.translations["loading"] + "</div>");
				$tooltip.data("trigger", this);
				$tooltip.data("alignment", alignment);
				$("body").append($tooltip);
				referenceCallback(referenceType, referenceId);
				return $tooltip;
			}
		}, alignment);
		
		onedev.server.markdown.initRendered(containerId);
		
		var $img = $rendered.find("img");
		$img.each(function() {
			var $this = $(this);
			var src = $this.attr("src");
			$this.removeAttr("src");
			$this.attr("data-src", src);
		});
		
		lozad("#" + containerId + " .markdown-rendered img").observe();
	},
	renderIssueTooltip: function(title, state, stateFontColor, stateBackgroundColor) {
		var $tooltip = $("#reference-tooltip");
		if (title) {
			$tooltip.empty().append("" +
					"<div class='d-flex issue align-items-center'>" +
					"  <span class='state badge mr-3'></span> <span class='title font-weight-bold'></span>" +
					"</div>");
			$tooltip.find(".state").css({
				"color": stateFontColor,
				"background": stateBackgroundColor
			}).text(state);
			$tooltip.find(".title").text(title);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.markdown.translations["issue-not-exist-or-access-denied"] + "</i>");			
		}
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
	},
	renderPullRequestTooltip: function(title, status, statusCss) {
		var $tooltip = $("#reference-tooltip");
		if (title) {
			$tooltip.empty().append("" +
					"<div class='d-flex align-items-center'>" +
					"  <span class='badge status mr-3'></span> <span class='title font-weight-bold'></span>" +
					"</div>");
			$tooltip.find(".status").addClass(statusCss).text(status);
			$tooltip.find(".title").text(title);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.markdown.translations["pull-request-not-exist-or-access-denied"] + "</i>");			
		}
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
	},
	renderBuildTooltip: function(title, iconHref, iconCss) {
		var $tooltip = $("#reference-tooltip");
		if (title) {
			$tooltip.empty().append("" +
					"<div class='d-flex align-items-center'>" +
					"  <svg class='mr-2 " + iconCss + "'><use xlink:href='" + iconHref + "'/></svg> <span class='title font-weight-bold'></span>" +
					"</div>");
			$tooltip.find(".title").text(title);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.markdown.translations["build-not-exist-or-access-denied"] + "</i>");			
		}
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
	},
	renderUserTooltip: function(avatarUrl, name) {
		var $tooltip = $("#reference-tooltip");
		$tooltip.empty().append("" +
				"<div class='d-flex align-items-center'>" +
				"  <img class='avatar mr-2'></img> <div class='name font-weight-bold'></div>" +
				"</div>");
		$tooltip.find(".avatar").attr("src", avatarUrl);
		$tooltip.find(".name").text(name);
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
	},
	renderCommitTooltip: function(author, date, commitMessage) {
		var $tooltip = $("#reference-tooltip");
		if (commitMessage) {
			$tooltip.empty().append("" +
					"  <div class='font-weight-bolder mb-2'><span class='author'></span> <span class='date'></span></div>" +
					"  <pre class='body mb-0'></pre>");
			$tooltip.find(".author").text(author);
			$tooltip.find(".date").text(date);
			$tooltip.find(".body").text(commitMessage);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.markdown.translations["commit-not-exist-or-access-denied"] + "</i>");			
		}
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
	},
	onEmojisLoaded: function(containerId, emojis) {
		var $container = $("#" + containerId);
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $edit = $body.children(".edit");
		var $input = $edit.children("textarea");
		var $emojis = $head.children(".emojis");
		
		var contentHtml = "";
		for (var i in emojis) {
			var emoji = emojis[i];
			contentHtml += "<a class='emoji' data-tippy-content='" + emoji.name + "'>" + emoji.unicode + "</a> ";
		}
		$emojis.html(contentHtml);
		$emojis.removeClass("loading");
		$emojis.addClass("loaded");
		tippy($emojis[0].querySelectorAll('.emoji'), {
			delay: [500, 0],
			placement: 'auto'
		});
		$emojis.find(".emoji").click(function() {
			if (!$edit.is(":visible")) 
				return;
			$input.focus();
			var prefix = onedev.server.markdown.getAtWhoPrefix($input);
			document.execCommand("insertText", false, prefix + ":" + $(this).data("tippy-content") + ": ");
			onedev.server.markdown.fireInputEvent($input);
		});
	},
	insertText: function(containerId, text) {
		var $body = $("#" + containerId + ">.body");
		var $input = $body.find(">.edit>textarea");
		$input.focus();
		document.execCommand("insertText", false, text);
		onedev.server.markdown.fireInputEvent($input);
	},
	insertUrl: function(containerId, isImage, url, name, replaceMessage) {
		var $body = $("#" + containerId + ">.body");
		var $input = $body.find(">.edit>textarea");

    	var sanitizedUrl = $('<div>'+url+'</div>').text();
    	var message;
    	var defaultDescription = onedev.server.markdown.translations["enter-description-here"];
    	if (name)
    		message = '['+name+']('+sanitizedUrl+')';
    	else
    		message = '[' + defaultDescription + ']('+sanitizedUrl+')';

    	if (isImage)
    		message = "!" + message;
    	
    	onedev.server.markdown.updateUploadMessage($input, message, replaceMessage);
    	
    	if (!name) {
    		var offset = isImage?2:1;
    		$input.range($input.caret()-message.length+offset, $input.caret()-message.length+defaultDescription.length+offset);
    	}
	}, 
	updateUploadMessage: function($input, message, replaceMessage) {
		$input.focus();
		var isError = message.indexOf("!!") == 0;
		var pos = $input.val().indexOf(replaceMessage);
		if (pos != -1) {
			var currentPos = $input.caret();
			$input.range(pos, pos+ replaceMessage.length);
			document.execCommand("insertText", false, message);
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
				document.execCommand("insertText", false, message);
				if (!isError)
					$input.caret($input.caret() + message.length);
			} else {
				document.execCommand("insertText", false, message);
				if (isError)
					$input.range($input.caret()-message.length, $input.caret());
			}
		} 
		onedev.server.markdown.fireInputEvent($input);
	},
	onInputUrlDomReady: function(containerId) {
		var $container = $("#"+containerId);
		var $url = $container.find(".url");
		var $text = $container.find(".text");
		$url.on("input", function() {
			$text.attr("placeholder", onedev.server.util.describeUrl($url.val()));
		});
	},
	getQuoted: function(content) {
		var quoted = [];
		content.split('\n').forEach(function(line) {
			quoted.push("> " + line); 
		});
		return quoted.join('\n');
	},
	onQuote: function(containerId, content) {
		var quoted = onedev.server.markdown.getQuoted(content).replace(/\r/g, "");
		var $input = $("#" + containerId + ">.body>.edit>textarea");
		$input.focus();
		document.execCommand("insertText", false, quoted + "\n\n");
		onedev.server.markdown.fireInputEvent($input);
		$input.blur();
		$input.focus();
	}
	
};