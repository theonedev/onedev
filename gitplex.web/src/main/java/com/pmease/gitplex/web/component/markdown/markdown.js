gitplex.markdown = {
	setup: function(elementId, atWhoLimit, callback) {
		var $element = $("#" + elementId);

		$element.markdown({
			onPreview: function(e) {
				callback("markdownPreview", e.getContent());
				return "";
			},
			onFocus: function(e) {
				var $mask = $element.prevAll(".md-emojis").find(".mask");
				if ($element.is(":visible"))
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
			        	   $element.prevAll(".md-help").toggle();
			           }
		           }, {
		        	   name: "cmdEmojis",
			           toggle: true,
			           title: "Emojis",
			           icon: "fa fa-smile-o",
			           callback: function(e){
			        	   callback("toggleEmojis");
			           }
		           }]			
			}]], 
			iconlibrary: "fa"
		});
		
		var $parent = $element.parent();
		$element.before($parent.parent().find(".md-help"));
		$element.before($parent.parent().find(".md-emojis"));
		$parent.find(".md-header .btn-group:nth-child(2)").append($parent.find(".md-header .fa-smile-o").parent()); 	
		
		$element[0].cachedMentions = [];
		$element[0].cachedEmojis = [];

	    $element.atwho({
	    	at: ':',
	        callbacks: {
	        	remoteFilter: function(query, renderCallback) {
	        		var $this = $(this);
	                if(!$this.data('active')){
	                    $this.data('active', true);                            
	                    var queryEmojis = $element[0].cachedEmojis[query];
	                    if(typeof queryEmojis == "object") {
	                        renderCallback(queryEmojis);
	                    } else {
	                		$element[0].atWhoEmojiRenderCallback = renderCallback;
	                		$element[0].atWhoEmojiQuery = query;
	                    	callback("emojiQuery", query);
	                    }                             
	                    $this.data('active', false);                            
	                }                    
	        	}
	        },
	        displayTpl: "<li><i class='emoji' style='background-image:url(${url})'></i> ${name} </li>",
	        insertTpl: ':${name}:', 
	        limit: atWhoLimit
	    }).atwho({
	    	at: '@',
	        callbacks: {
	        	remoteFilter: function(query, renderCallback) {
	        		var $this = $(this);
	                if(!$this.data('active')){
	                    $this.data('active', true);                            
	                    var queryMentions = $element[0].cachedMentions[query];
	                    if(typeof queryMentions == "object") {
	                        renderCallback(queryMentions);
	                    } else {
	                		$element[0].atWhoUserRenderCallback = renderCallback;
	                		$element[0].atWhoUserQuery = query;
	                    	callback("userQuery", query);
	                    }                             
	                    $this.data('active', false);                            
	                }                    
	        	}
	        },
	        displayTpl: "<li><span class='avatar'><img src='${avatarUrl}'/></span> ${name} <small>${fullName}</small></li>",
	        limit: atWhoLimit
	    });		
	},
	setupEmojiSelection: function(elementId) {
		$("#" + elementId).prevAll(".md-emojis").find(".emoji").click(function() {
			var $input = $("#" + elementId);
			if (!$input.is(":visible")) 
				return;
			
			var emojiName = $(this).attr("name");
			
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
	}
}
