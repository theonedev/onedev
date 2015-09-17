gitplex.comment = function(inputId, atWhoLimit, callback) {
	var $input = $("#" + inputId);

	$input.prevAll(".md-help").html(
			"<a href='https://help.github.com/articles/github-flavored-markdown/' target='_blank'>GitHub flavored markdown</a> " +
			"is accepted here. You can also input <b>@<em>user_login_name</em></b> to mention an user (and email notification " +
			"will be sent to the user), or use <b>:<em>emoji</em>:</b> to insert an emoji.");

	$input[0].cachedUsers = [];
    $input.atwho({
    	at: '@',
    	searchKey: "searchKey",
        callbacks: {
        	remoteFilter: function(query, renderCallback) {
                var queryUsers = $input[0].cachedUsers[query];
                if(typeof queryUsers == "object") {
                    renderCallback(queryUsers);
                } else if (typeof queryUsers != "string") {
                	// indicates that user query is ongoing and subsequent 
                	// query using same query string should be waiting
                	$input[0].cachedUsers[query] = "";
                    
                	$input[0].atWhoUserRenderCallback = renderCallback;
                	$input[0].atWhoUserQuery = query;
                	callback("userQuery", query);
                }                             
        	}
        },
        displayTpl: "<li><span class='avatar'><img src='${avatarUrl}'/></span> ${name} <small>${fullName}</small></li>",
        limit: atWhoLimit
    });		
}
