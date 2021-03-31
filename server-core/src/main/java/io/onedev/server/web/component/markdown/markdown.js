onedev.server.markdown = {
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
	onDomReady: function(containerId, callback, atWhoLimit, attachmentUploadUrl, 
			attachmentMaxSize, canMentionUser, canReferenceEntity, 
			projectNamePattern, autosaveKey) {
		var $container = $("#" + containerId);
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

		$head.find(".dropdown>a").dropdown();
		
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
			$rendered.html("<div class='message'>Loading...</div>");
			
			$editLink.removeClass("active");
			$previewLink.addClass("active");
			$splitLink.removeClass("active");
			callback("render", $input.val());
		});
		$splitLink.click(function() {
			$container.removeClass("edit-mode").removeClass("preview-mode").addClass("split-mode");
			
			$input.focus();
			
			$rendered.html("<div class='message'>Loading...</div>");
			
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

			if (autosaveKey) {
				var content = $input.val();
				if (content.trim().length != 0)
					localStorage.setItem(autosaveKey, content);
			}
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
			if (e.keyCode == 13 && $(".atwho-view").filter(":visible").length == 0) {
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
				
				onedev.server.markdown.fireInputEvent($input);
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
				if ((e.ctrlKey|e.metaKey) && e.keyCode == 76) {
					e.preventDefault();
					callback("selectLink");
				} else if ((e.ctrlKey|e.metaKey) && e.keyCode == 73) {
					e.preventDefault();
					callback("selectImage");	
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

		var referencePattern = "(^|\\W+)((pull\\s*request|issue|build)\\s+)?(" + projectNamePattern + ")?#(\\S*)$";
		
	    if (canReferenceEntity) {
	    	function matchReference() {
	    		var input = $input.val().substring(0, $input.caret()).trim();
	    		var match = new RegExp(referencePattern, 'gi').exec(input);
	    		if (match) {
	    			var referenceType = match[3];
	    			if (referenceType)
	    				referenceType = referenceType.replace(/\s+/g, '').toLowerCase();
	    			return {
	    				type: referenceType,
	    				project: match[4],
	    				query: match[6]
	    			}
	    		} else { 
	    			return undefined;   		
	    		}
	    	}
	    	
		    $input.atwho({
		    	at: '#',
		    	startWithSpace: false,
		    	searchKey: "searchKey",
		        callbacks: {
		        	remoteFilter: function(query, renderCallback) {
		        		$container.data("atWhoReferenceRenderCallback", renderCallback);
		        		var match = matchReference();
		        		if (match) 
                            callback("referenceQuery", match.query, match.type, match.project);
		        	}
		        },
		        displayTpl: function() {
	        		if (matchReference().type) 
			    		return "<li><span>#${referenceNumber}</span> - ${referenceTitle}</li>";
	        		else
                        return "<li><span>${referenceType} #${referenceNumber}</span> - ${referenceTitle}</li>";
		        },
		        insertTpl: function() {
	        		if (matchReference().type) 
		    			return "#${referenceNumber}";
	        		else
                        return '${referenceType} #${referenceNumber}';
		        }, 
		        limit: atWhoLimit
		    });		
	    }
	    
	    if (attachmentUploadUrl) {
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
					onedev.server.markdown.updateUploadMessage($input, message);
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
								"!!Unable to connect to server!!", xhr.replaceMessage);
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
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("**" + selected.text + "**").range(selected.start+2, selected.end+2);
			} else {
				$input.range("**strong text**").range(selected.start+2, selected.end+2+"strong text".length);
			}
			$input.focus();
			onedev.server.markdown.fireInputEvent($input);
			closeMenu();			
		});
		
		$actionMenu.find(".do-italic").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("_" + selected.text + "_").range(selected.start+1, selected.end+1);
			} else {
				$input.range("_emphasized text_").range(selected.start+1, selected.end+1+"emphasized text".length);
			}
			$input.focus();
			onedev.server.markdown.fireInputEvent($input);
			closeMenu();			
		});
		
		$actionMenu.find(".do-header").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("### " + selected.text).range(selected.start+4, selected.end+4);
			} else {
				$input.range("### heading text").range(selected.start+4, selected.end+4+"heading text".length);
			}
			$input.focus();
			onedev.server.markdown.fireInputEvent($input);
			closeMenu();			
		});
		
		$actionMenu.find(".do-list, .do-orderlist, .do-tasklist").click(function() {
			var leading;
			if ($(this).hasClass("do-list"))
				leading = "-";
			else if ($(this).hasClass("do-orderlist"))
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
			onedev.server.markdown.fireInputEvent($input);
			closeMenu();			
		});

		$actionMenu.find(".do-code").click(function() {
			var langHint = "programming language";
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("\n```" + langHint + "\n" + selected.text + "\n```\n").range(selected.start+4, selected.start+4+langHint.length);
			} else {
				$input.range("\n```" + langHint + "\ncode text here\n```\n").range(selected.start+4, selected.start+4+langHint.length);
			}
			$input.focus();
			onedev.server.markdown.fireInputEvent($input);
			closeMenu();			
		});
		
		$actionMenu.find(".do-quote").click(function() {
			var selected = $input.range();
			if (selected.length != 0)
				$input.range("> " + selected.text).range(selected.start+2, selected.end+2);
			else
				$input.range("> quote here").range(selected.start+2, selected.start+2+"quote here".length);
			$input.focus();
			onedev.server.markdown.fireInputEvent($input);
			closeMenu();			
		});
		
		$actionMenu.find(".do-emoji").click(function() {
			if (!$emojis.hasClass("loaded") && !$emojis.hasClass("loading")) {
				$emojis.addClass("loading");
				$emojis.html("Loading emojis...");
				callback("loadEmojis");
			}
			$emojis.toggle();
			$actionMenu.find(".do-emoji").toggleClass("active");
			closeMenu();			
		});
		
		$actionMenu.find(".do-mention, .do-reference").click(function() {
			closeMenu();			
			
			if (!$edit.is(":visible")) 
				return;

			var atChar = $(this).hasClass("do-mention")? "@": "#";	
			var prevChar;
			var caret = $input.caret();
			if (caret != 0) 
				prevChar = $input.val().charAt(caret-1);
			
			var prefix = $(this).data("reference");
			if (prefix === undefined)
				prefix = "";
			else 
				prefix = prefix + " ";
			
			if (prevChar === undefined || prevChar === ' ' || prevChar === '\n') 
				$input.caret(prefix + atChar);
			else 
				$input.caret(" " + prefix + atChar);
			
			$input.atwho("run");
			onedev.server.markdown.fireInputEvent($input);
		});
		
		$actionMenu.find(".do-image, .do-link").click(function() {
	       	if ($(this).hasClass("do-image"))
	       		callback("selectImage");
	       	else
	       		callback("selectLink");
			closeMenu();			
		});
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
			onedev.server.form.registerAutosaveKey($container.closest("form.leave-confirm"), autosaveKey);
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
		
		onedev.server.markdown.initRendered($rendered);

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
	initRendered: function($rendered) {
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
		    		var $loadingIndicator = $("<div class='markdown-image-loading'></div>");
		    		$loadingIndicator.css("width", $image.width()).css("height", $image.height());
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
		    	    actualImage.src = $image.attr("src");    		
				});
		    	$image.css("cursor", "pointer");
			}
		});
		
		$(window).resize(function() {
			$rendered.find(".CodeMirror").each(function() {
				$(this)[0].CodeMirror.refresh();
			});
		});
	},
	onViewerDomReady: function(containerId, taskCallback, taskSourcePositionDataAttribute, referenceCallback) {
		var $container = $("#" + containerId);
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
				var $tooltip = $("<div id='reference-tooltip'>Loading...</div>");
				$tooltip.data("trigger", this);
				$tooltip.data("alignment", alignment);
				$("body").append($tooltip);
				referenceCallback(referenceType, referenceId);
				return $tooltip;
			}
		}, alignment);
		
		onedev.server.markdown.initRendered($rendered);
		
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
		$tooltip.empty().append("" +
				"<div class='d-flex issue align-items-center'>" +
				"  <span class='state badge mr-3'></span> <span class='title font-weight-bold'></span>" +
				"</div>");
		$tooltip.find(".state").css({
			"color": stateFontColor,
			"background": stateBackgroundColor
		}).text(state);
		$tooltip.find(".title").text(title);
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
	},
	renderPullRequestTooltip: function(title, status, statusCss) {
		var $tooltip = $("#reference-tooltip");
		$tooltip.empty().append("" +
				"<div class='d-flex align-items-center'>" +
				"  <span class='badge status mr-3'></span> <span class='title font-weight-bold'></span>" +
				"</div>");
		$tooltip.find(".status").addClass(statusCss).text(status);
		$tooltip.find(".title").text(title);
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
	},
	renderBuildTooltip: function(title, iconHref, iconCss) {
		var $tooltip = $("#reference-tooltip");
		$tooltip.empty().append("" +
				"<div class='d-flex align-items-center'>" +
				"  <svg class='mr-2 " + iconCss + "'><use xlink:href='" + iconHref + "'/></svg> <span class='title font-weight-bold'></span>" +
				"</div>");
		$tooltip.find(".title").text(title);
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
	},
	renderUserTooltip: function(avatarUrl, name, email) {
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
		$tooltip.empty().append("" +
				"  <div class='font-weight-bolder mb-2'><span class='author'></span> <span class='date'></span></div>" +
				"  <pre class='body mb-0'></pre>");
		$tooltip.find(".author").text(author);
		$tooltip.find(".date").text(date);
		$tooltip.find(".body").text(commitMessage);
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
			contentHtml += "<a class='emoji' title='" + emoji.name + "'><img src='" + emoji.url + "'></img></a> ";
		}
		$emojis.html(contentHtml);
		$emojis.removeClass("loading");
		$emojis.addClass("loaded");
		$emojis.find(".emoji").click(function() {
			if (!$edit.is(":visible")) 
				return;
			
			$input.caret(":" + $(this).attr("title") + ": ");
			onedev.server.markdown.fireInputEvent($input);
		});
	},
	insertUrl: function(containerId, isImage, url, name, replaceMessage) {
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
    	
    	onedev.server.markdown.updateUploadMessage($input, message, replaceMessage);
    	
    	if (!name) {
    		var offset = isImage?2:1;
    		$input.range($input.caret()-message.length+offset, $input.caret()-message.length+defaultDescription.length+offset);
    	}
    	
		onedev.server.markdown.fireInputEvent($input);
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
				// use range instead of caret here as otherwise the editor will be scrolled to the bottom
				$input.range(message); 
				if (isError)
					$input.range($input.caret()-message.length, $input.caret());
			}
		} 
	},
	onInputUrlDomReady: function(containerId) {
		var $container = $("#"+containerId);
		var $url = $container.find(".url");
		var $text = $container.find(".text");
		$url.on("input", function() {
			$text.attr("placeholder", onedev.server.util.describeUrl($url.val()));
		});
	}
	
};