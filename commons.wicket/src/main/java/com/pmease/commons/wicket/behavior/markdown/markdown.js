pmease.commons.markdown = {
	init: function(inputId, atWhoLimit, callback, uploadUrl, attachmentSupport, attachmentMaxSize) {
		var $input = $("#" + inputId);
		function onSelectUrl(isImage) {
     	   var $modal = $("" +
       	   		"<div class='modal'>" +
       	   		"<div class='modal-dialog'>" +
       	   		"<div class='modal-content'>" +
       	   		"<div id='" + inputId + "-urlselector'></div>" +
       	   		"</div>" +
       	   		"</div>" +
       	   		"</div>");
     	   // make sure to append to body to avoid z-index issues causing modal to sit in background
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
		
		$input.markdown({
			onShow: function(e) {
				var $mdEditor = $input.closest(".md-editor");
				$mdEditor.attr("tabindex", "0");
				$mdEditor.bind("keydown", "Ctrl+E", function(e) {
					$mdEditor.find(">.md-header .fa-search").parent().click();
					e.preventDefault();
					e.stopPropagation();
			 	});
				$input.bind("keydown", "F11", function(e) {
					if ($mdEditor.hasClass("md-fullscreen-mode"))
						$mdEditor.find(".exit-fullscreen").click();
					else
						$mdEditor.find(".md-control-fullscreen").click();
					e.preventDefault();
					e.stopPropagation();
				});
			},
			onFullscreen: function(e) {
				$input.trigger("fullscreen");
			},
			onExitFullscreen: function(e) {
				$input.trigger("exitFullscreen");
			},
			onPreview: function(e) {
				callback("markdownPreview", e.getContent());
				return "";
			},
			onFocus: function(e) {
				var $mask = $input.prevAll(".md-emojis").find(".mask");
				if ($input.is(":visible"))
					$mask.hide();
				else 
					$mask.show();
			},
			additionalButtons: [[{
				name: "custom",
		        data: [{
		        	   name: "cmdHelp",
			           toggle: true,
			           title: "Help",
			           icon: "fa fa-question",
			           callback: function(e){
			        	   $input.prevAll(".md-help").toggle();
			        	   $input.trigger("resized");
			           }
		           }, {
		        	   name: "cmdEmojis",
			           toggle: true,
			           title: "Emojis",
			           icon: "fa fa-smile-o",
			           callback: function(e){
			        	   var $emojis = $input.prevAll(".md-emojis");
			        	   if ($emojis.find(".emoji").length == 0)
			        		   callback("loadEmojis");
			        	   $emojis.toggle();
			        	   $input.trigger("resized");
			           }
		           }, {
		        	   name: "cmdURL2",
		        	   title: "Attachment",
		               hotkey: 'Ctrl+L',
		               icon: "fa fa-link",
		               callback: function(e){
		            	   onSelectUrl(false);
		               }
		           }, {
		        	   name: "cmdImage2",
		        	   title: "Image",
		               hotkey: 'Ctrl+G',
		               icon: "fa fa-picture-o",
		               callback: function(e){
		            	   onSelectUrl(true);
		               }
		           }]			
			}]], 
			iconlibrary: "fa", 
			resize: "vertical"
		});
		
		$input.css({resize: "vertical"});
		$input.before(
				"<div class='md-help'>" +
				"  <a href='https://help.github.com/articles/github-flavored-markdown/' target='_blank'>GitHub flavored markdown</a> " +
				"  is accepted here. You can also input <b>:<em>emoji</em>:</b> to insert an emoji." +
				"</div>");
		$input.before(
				"<div class='md-emojis'>" +
				"  <div class='loading'>Loading emojis...</div>" +
				"</div>");

		var $btnGroup2 = $input.parent().find(".md-header .btn-group:nth-child(2)");
		$btnGroup2.find(".fa-link").parent().remove();
		$btnGroup2.find(".fa-picture-o").parent().remove();
		$btnGroup2.append($input.parent().find(".md-header .fa-link").parent()); 	
		$btnGroup2.append($input.parent().find(".md-header .fa-picture-o").parent()); 	
		$btnGroup2.append($input.parent().find(".md-header .fa-smile-o").parent()); 	
		
		$input[0].cachedEmojis = [];

	    $input.atwho({
	    	at: ':',
	        callbacks: {
	        	remoteFilter: function(query, renderCallback) {
            		$input.data("atWhoEmojiRenderCallback", renderCallback);
                	callback("emojiQuery", query);
	        	}
	        },
	        displayTpl: "<li><i class='emoji' style='background-image:url(${url})'></i> ${name} </li>",
	        insertTpl: ':${name}:', 
	        limit: atWhoLimit
	    });		
	    
	    if (attachmentSupport) {
	    	var input = $input[0];
	    	
			input.addEventListener("paste", function(e) {
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
			
			input.addEventListener("dragover", function(e) {
				$input.addClass("drag-over");
				e.stopPropagation();
				e.preventDefault();		
			}, false);
			
			input.addEventListener("dragleave", function(e) {
				$input.removeClass("drag-over");
				e.stopPropagation();
				e.preventDefault();		
			}, false);
			
			input.addEventListener("drop", function(e) {
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
					pmease.commons.markdown.updateUploadMessage($input, message);
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
							pmease.commons.markdown.updateUploadMessage($input, 
									"!!" + xhr.responseText + "!!", xhr.replaceMessage);
						}
					};
					xhr.onerror = function() {
						pmease.commons.markdown.updateUploadMessage($input, 
								"!!Unable to connect to server!!", xhr.replaceMessage);
					};
					xhr.open("POST", uploadUrl, true);
					xhr.setRequestHeader("File-Name", encodeURIComponent(file.name));
					xhr.setRequestHeader("Attachment-Support", attachmentSupport);
					xhr.send(file);
				}
			}
	    }
	},
	onEmojisLoaded: function(inputId, emojis) {
		var $input = $("#" + inputId);
		var $emojis = $input.prevAll(".md-emojis");
		$emojis.html("<div class='mask'></div><div class='content'><div>");
		var contentHtml = "";
		for (var i in emojis) {
			var emoji = emojis[i];
			contentHtml += "<a class='emoji' title='" + emoji.name + "'><img src='" + emoji.url + "'></img></a> ";
		}
		var $content = $emojis.find(".content");
		$content.html(contentHtml);
		$content.find(".emoji").click(function() {
			if (!$input.is(":visible")) 
				return;
			
			var emojiName = $(this).attr("title");
			
			var beforeChar, afterChar;
			var index = $input.caret();
			var content = $input.val();
			var length = content.length;
			if (length == 0) {
				beforeChar = afterChar = ' ';
			} else if (index == 0) {
				beforeChar = ' ';
				afterChar = content.charAt(0);
			} else if (index == length) {
				beforeChar = content.charAt(index-1);
				afterChar = ' ';
			} else {
				beforeChar = content.charAt(index-1);
				afterChar = content.charAt(index);
			}
			
			if (!/\s/.test(beforeChar))
				beforeChar = ' ';
			else
				beforeChar = '';
			
			if (!/\s/.test(afterChar))
				afterChar = ' ';
			else
				afterChar = '';
			
			$input.caret(beforeChar + ":" + emojiName + ":" + afterChar);
		});
 	   $input.trigger("resized");
	},
	
	insertUrl: function(inputId, isImage, url, name, replaceMessage) {
		var $input = $("#" + inputId);

    	var sanitizedUrl = $('<div>'+url+'</div>').text();
    	var message;
    	var defaultDescription = "Enter description here";
    	if (name)
    		message = '['+name+']('+sanitizedUrl+')';
    	else
    		message = '[' + defaultDescription + ']('+sanitizedUrl+')';

    	if (isImage)
    		message = "!" + message;
    	
    	pmease.commons.markdown.updateUploadMessage($input, message, replaceMessage);
    	if (!name)
    		$input.range($input.caret()-message.length+1, $input.caret()-message.length+defaultDescription.length+1);
    	
    	pmease.commons.form.markDirty($input.closest("form.leave-confirm"));
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
	
	initFileUpload: function(uploadId, maxSize, maxSizeForDisplay) {
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
};
