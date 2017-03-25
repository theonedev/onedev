gitplex.server.markdown = {
	getSplitCookieKey: function($container) {
		if ($container.hasClass("compact"))
			return "markdownEditor.compactSplitView";
		else
			return "markdownEditor.splitView";
	},
	onDomReady: function(containerId, callback, atWhoLimit, attachmentSupport, attachmentMaxSize, 
			canMentionUser, canReferencePullRequest) {
		var $container = $("#" + containerId);
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $editLink = $head.find(".edit");
		var $previewLink = $head.find(".preview");
		var $splitLink = $head.find(".split");
		var $emojis = $container.children(".emojis");
		var $help = $container.children(".help");
		var $input = $body.children(".input");
		var $preview = $body.children(".preview");
		
		$head.find(".dropdown>button").dropdown();
		
		$input.caret(0);

		$editLink.click(function() {
			$head.find(".pull-left .btn").removeAttr("disabled");
			
			$preview.hide();
			$input.show().focus();
			$editLink.addClass("active");
			$previewLink.removeClass("active");
			$splitLink.removeClass("active");
			$container.removeClass("preview-mode").removeClass("split-mode").addClass("edit-mode");
			onLayoutChange();
			Cookies.set(gitplex.server.markdown.getSplitCookieKey($body), false, {expires: Infinity});
		});
		$previewLink.click(function() {
			$head.find(".pull-left .btn").attr("disabled", "disabled");
			
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
			$container.removeClass("edit-mode").removeClass("split-mode").addClass("preview-mode");
			onLayoutChange();
			callback("render", $input.val());
		});
		$splitLink.click(function() {
			$head.find(".pull-left .btn").removeAttr("disabled");
			
			$input.show().focus();
			$preview.html("<div class='message'>Loading...</div>");
			$preview.show();
			$editLink.removeClass("active");
			$previewLink.removeClass("active");
			$splitLink.addClass("active");
			$container.removeClass("edit-mode").removeClass("preview-mode").addClass("split-mode");
			onLayoutChange();
			callback("render", $input.val());
			Cookies.set(gitplex.server.markdown.getSplitCookieKey($body), true, {expires: Infinity});
		});
		
		$input.on("autosize:resized", function() {
			$preview.outerHeight($input.outerHeight());
		});

		$input.doneEvents("input inserted.atwho", function() {
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
		
		function dispatchInputEvent() {
			if(document.createEventObject) {
				$input[0].fireEvent("input");
			} else {
			    var evt = document.createEvent("HTMLEvents");
			    evt.initEvent("input", false, true);
			    $input[0].dispatchEvent(evt);
			}
		}
		
	    var fontSize = parseInt(getComputedStyle($input[0]).getPropertyValue('font-size'));
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
					$input.caret("\n");
				} else {
					$input.caret("\n" + spaces);
				}
				
				var caretBottom = getCaretCoordinates($input[0], $input.caret()).top + fontSize;
				if (caretBottom > $input.scrollTop() + $input.height()) {
					$input.scrollTop(caretBottom - $input.height());
				}
				
				dispatchInputEvent();
			}
		});
		
		$container.on("autofit", function(e, width, height) {
			height -= $head.outerHeight();
			if ($emojis.is(":visible"))
				height -= $emojis.outerHeight();
			if ($help.is(":visible"))
				height -= $help.outerHeight();
			if ($container.hasClass("compact")) {
				$input.outerHeight(height/2);
			} else {
				$input.outerHeight(height);
			}
			$preview.outerHeight($input.outerHeight());
		});

		function onLayoutChange() {
			if (!($container.hasClass("compact"))) {
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
		
		function onSelectUrl(isImage) {
			var $modal = $("" +
					"<div class='modal'>" +
	       	   		"<div class='modal-dialog'>" +
	       	   		"<div class='modal-content'>" +
	       	   		"<div id='" + containerId + "-urlselector'></div>" +
	       	   		"</div>" +
	       	   		"</div>" +
	       	   		"</div>");
			// Make sure to append to body to avoid z-index issues causing modal to sit in background
	       	$("body").append($modal);
	       	$modal.modal({show: true, backdrop: "static", keyboard: true});
	       	$modal.on('hidden.bs.modal', function (e) {
	       		$modal.remove();
	       		$input.focus();
	       	});
	       	$modal.keydown(function(e) {
	       		if (e.keyCode == 27) 
	       			$input.data("ignoreEsc", true);
	       	});
	       	$modal.keyup(function(e) {
	       		if (e.keyCode == 27)
	       			$input.data("ignoreEsc", true);
	       	});
	       	if (isImage)
	       		callback("selectImage");
	       	else
	       		callback("selectLink");
		}
		
		$head.find(".do-bold").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("**" + selected.text + "**").range(selected.start+2, selected.end+2);
			} else {
				$input.range("**strong text**").range(selected.start+2, selected.end+2+"strong text".length);
			}
			$input.focus();
			dispatchInputEvent();
		});
		
		$head.find(".do-italic").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("_" + selected.text + "_").range(selected.start+1, selected.end+1);
			} else {
				$input.range("_emphasized text_").range(selected.start+1, selected.end+1+"emphasized text".length);
			}
			$input.focus();
			dispatchInputEvent();
		});
		
		$head.find(".do-header").click(function() {
			var selected = $input.range();
			if (selected.length != 0) {
				$input.range("### " + selected.text).range(selected.start+4, selected.end+4);
			} else {
				$input.range("### heading text").range(selected.start+4, selected.end+4+"heading text".length);
			}
			$input.focus();
			dispatchInputEvent();
		});
		
		$head.find(".do-list, .do-orderlist").click(function() {
			var leading = $(this).hasClass("do-list")?"-":"1.";
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
				$input.range(leading + " list text here").range(selected.start+leading.length+1, selected.start+leading.length+1+"list text here".length);
			}
			$input.focus();
			dispatchInputEvent();
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
			dispatchInputEvent();
		});
		
		$head.find(".do-quote").click(function() {
			var selected = $input.range();
			if (selected.length != 0)
				$input.range("> " + selected.text).range(selected.start+2, selected.end+2);
			else
				$input.range("> quote here").range(selected.start+2, selected.start+2+"quote here".length);
			$input.focus();
			dispatchInputEvent();
		});
		
		$head.find(".do-emoji").click(function() {
			if (!$emojis.hasClass("loaded") && !$emojis.hasClass("loading")) {
				$emojis.addClass("loading");
				$emojis.html("Loading emojis...");
				callback("loadEmojis");
			}
			$emojis.toggle();
			$(this).toggleClass("active");
			$(window).resize();
		});
		
		$head.find(".do-help").click(function() {
			$(this).toggleClass("active");
			$help.toggle();
			$(window).resize();
		});
		
		$head.find(".do-mention, .do-hashtag").click(function() {
			if (!$input.is(":visible")) 
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
			dispatchInputEvent();
		});
		
		$head.find(".do-image, .do-link").click(function() {
			onSelectUrl($(this).hasClass("do-image"));
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
		var $input = $("#" + containerId + ">.body>.input");
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
		var $head = $("#" + containerId + ">.head");
		var $body = $("#" + containerId + ">.body");
		var $preview = $body.children(".preview");
		var $input = $body.children(".input");
		$preview.outerHeight($input.outerHeight());

		if (Cookies.get(gitplex.server.markdown.getSplitCookieKey($body)) === "true")
			$head.find(".split").trigger("click");
	},
	onRendered: function(containerId, html) {
		var $preview = $("#" + containerId + ">.body>.preview");

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
	onViewerDomReady: function(containerId, taskCallback, taskClass, taskSourcePositionDataAttribute) {
		var $container = $("#" + containerId);
		
		var $task = $container.find("." + taskClass);
		var $taskCheckbox = $task.children("input");
		$taskCheckbox.removeAttr("disabled").removeAttr("readonly");
		$taskCheckbox.change(function() {
			taskCallback($(this).parent().data(taskSourcePositionDataAttribute), $(this).prop("checked"));
		});	
		
		gitplex.server.markdown.initRendered($container.find(".markdown-rendered"));
	},
	onEmojisLoaded: function(containerId, emojis) {
		var $container = $("#" + containerId);
		var $head = $container.children(".head");
		var $body = $container.children(".body");
		var $input = $body.children(".input");
		var $emojis = $container.children(".emojis");
		
		var contentHtml = "";
		for (var i in emojis) {
			var emoji = emojis[i];
			contentHtml += "<a class='emoji' title='" + emoji.name + "'><img src='" + emoji.url + "'></img></a> ";
		}
		$emojis.html(contentHtml);
		$emojis.removeClass("loading");
		$emojis.addClass("loaded");
		$emojis.find(".emoji").click(function() {
			if (!$input.is(":visible")) 
				return;
			
			$input.caret(":" + $(this).attr("title") + ": ");
			dispatchInputEvent();
		});
		$(window).resize();
	},
	insertUrl: function(containerId, isImage, url, name, replaceMessage) {
		var $head = $("#" + containerId + ">.head");
		var $body = $("#" + containerId + ">.body");
		var $input = $body.children(".input");

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
    	
		dispatchInputEvent();
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
			if ($upload[0].files[0].size>maxSize) {
				$upload.closest('form').prepend("<div class='alert alert-danger'>Size of upload file should be less than " 
						+ maxSizeForDisplay + "<button type='button' class='close' data-dismiss='alert' aria-label='Close'>" +
								"<span aria-hidden='true'>&times;</span></button></div>");
			} else {
				$upload.closest('form').children(".alert").remove();
				$upload.next().click();
			}
		})
	}
	
}