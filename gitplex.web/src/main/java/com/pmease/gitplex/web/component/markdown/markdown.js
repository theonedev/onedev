gitplex.markdown = function(elementId, atWhoLimit, callback) {
	var $element = $("#" + elementId);

	$element.markdown({
		onPreview: function(e) {
			callback("markdownPreview", e.getContent());
			return "Loading...";
		}				
	});
	
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
}
