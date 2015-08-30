gitplex.markdown = {
	setup: function(inputId, atWhoLimit, callback) {
		var $input = $("#" + inputId);

		$input.markdown({
			onFullscreen: function(e) {
				$("body").append($input.closest(".md-editor"));
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
		           }]			
			}]], 
			iconlibrary: "fa"
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

		$input.parent().find(".md-header .btn-group:nth-child(2)").append($input.parent().find(".md-header .fa-smile-o").parent()); 	
		
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
	}
}
