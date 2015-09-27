pmease.commons.markdown = {
	init: function(inputId, atWhoLimit, callback, uploadUrl, attachmentSupport) {
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
       	   $input.closest("form").after($modal);
       	   $modal.modal({show: true, backdrop: "static", keyboard: true});
       	   $modal.on('hidden.bs.modal', function (e) {
       		   $modal.remove();
       		   $input.focus();
       	   });
       	   if (isImage)
       		   callback("selectImage");
       	   else
       		   callback("selectLink");
		}
		
		$input.markdown({
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
		        	   title: "Image",
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
                    var queryEmojis = $input[0].cachedEmojis[query];
                    if(typeof queryEmojis == "object") {
                        renderCallback(queryEmojis);
                    } else if (typeof queryEmojis != "string") {
                    	// indicates that emoji query is ongoing and subsequent 
                    	// query using same query string should be waiting
	                    $input[0].cachedEmojis[query] = "";
	                    
                		$input[0].atWhoEmojiRenderCallback = renderCallback;
                		$input[0].atWhoEmojiQuery = query;
                    	callback("emojiQuery", query);
                    }                             
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
						if (item.type.indexOf("jpeg") != -1 || item.type.indexOf("jpg") != -1)
							file.name = "clipboard.png";
						else if (item.type.indexOf("png") != -1)
							file.name = "clipboard.png";
						else if (item.type.indexOf("gif") != -1)
							file.name = "clipboard.gif";
						
						if (file.name)
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
			
			function appendAndSelect(message) {
				if ($input.range().length == 0) {
					$input.caret(message);
					$input.range($input.caret()-message.length, $input.caret());
				} else {
					$input.range(message);
				}
			}
			
			function uploadFile(file) {
				var xhr = new XMLHttpRequest();
				xhr.upload.onprogress = function(e) {
					var percentComplete = (e.loaded / e.total) * 100;
					appendAndSelect("Uploaded: " + percentComplete + "%");
				};
				xhr.onload = function() {
					if (xhr.status == 200) 
						callback("insertUrl", xhr.responseText);
					else 
						appendAndSelect("!!" + xhr.responseText + "!!");
				};
				xhr.onerror = function() {
					appendAndSelect("!!Unable to connect to server!!");
				};
				xhr.open("POST", uploadUrl, true);
				xhr.setRequestHeader("File-Name", file.name);
				xhr.setRequestHeader("Attachment-Support", attachmentSupport);
				xhr.send(file);
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
	
	insertUrl: function(inputId, isImage, url, name) {
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
    	
    	if ($input.range().length == 0) {
			$input.caret(message);
		} else {
			$input.range(message);
	    	$input.caret($input.caret()+message.length);
		}
    	if (!name)
    		$input.range($input.caret()-message.length+2, $input.caret()-message.length+defaultDescription.length+2);
    	pmease.commons.form.markDirty($input.closest("form.leave-confirm"));
	} 
}
