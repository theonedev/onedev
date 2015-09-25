pmease.commons.markdown = {
	init: function(inputId, atWhoLimit, callback, uploadUrl, attachmentSupport) {
		var $input = $("#" + inputId);

		$input.markdown({
			onShow: function(e) {
				$input.data("markdown", e);
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
		        	   name: "cmdImage2",
		        	   title: "Image2",
		               icon: "fa fa-picture-o",
		               callback: function(e){
		            	   var $modal = $("" +
		            	   		"<div class='modal'>" +
		            	   		"<div class='modal-dialog'>" +
		            	   		"<div class='modal-content'>" +
		            	   		"<div id='" + inputId + "-imageselector'></div>" +
		            	   		"</div>" +
		            	   		"</div>" +
		            	   		"</div>");
		            	   $input.after($modal);
		            	   $modal.modal({show: true, backdrop: "static", keyboard: true});
		            	   $modal.on('hidden.bs.modal', function (e) {
		            		   $modal.remove();
		            	   });
		            	   callback("selectImage");
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
		$btnGroup2.find(".fa-picture-o").parent().remove();
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
					}
					break;
				}
			});
			
			input.addEventListener("dragover", function(e) {
				e.stopPropagation();
				e.preventDefault();		
			}, false);
			
			input.addEventListener("dragleave", function(e) {
				e.stopPropagation();
				e.preventDefault();		
			}, false);
			
			input.addEventListener("drop", function(e) {
				e.stopPropagation();
				e.preventDefault();		
				var files = e.target.files || e.dataTransfer.files;
				if (files && files.length != 0)
					uploadFile(files[0]);
			}, false);
			
			function uploadFile(file) {
				var xhr = new XMLHttpRequest();
				xhr.upload.onprogress = function(e) {
					var percentComplete = (e.loaded / e.total) * 100;
					console.log("Uploaded: " + percentComplete + "%");
				};
				xhr.onload = function() {
					if (xhr.status == 200) {
						callback("insertImage", xhr.responseText);
					} else {
						alert("Error! Upload failed");
					}
				};
				xhr.onerror = function() {
					alert("Error! Upload failed. Can not connect to server.");
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
	
	insertImage: function(inputId, imageUrl) {
		var e = $("#" + inputId).data("markdown");
        // Give ![] surround the selection and prepend the image link
        var chunk, cursor, selected = e.getSelection(), content = e.getContent();

        if (selected.length === 0) {
			// Give extra word
			chunk = e.__localize('enter image description here');
        } else {
        	chunk = selected.text;
        }

        if (imageUrl !== null && imageUrl !== '' && imageUrl !== 'http://' && imageUrl.substr(0,4) === 'http') {
        	var sanitizedLink = $('<div>'+imageUrl+'</div>').text();

        	// transform selection and set the cursor into chunked text
        	e.replaceSelection('!['+chunk+']('+sanitizedLink+' "'+e.__localize('enter image title here')+'")');
        	cursor = selected.start+2;

        	// Set the next tab
        	e.setNextTab(e.__localize('enter image title here'));

        	// Set the cursor
        	e.setSelection(cursor,cursor+chunk.length);
        }
	}
}
