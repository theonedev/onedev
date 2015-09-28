gitplex.comment = function(inputId, atWhoLimit, callback) {
	var $input = $("#" + inputId);

	$input.prevAll(".md-help").html(
			"<a href='https://help.github.com/articles/github-flavored-markdown/' target='_blank'>GitHub flavored markdown</a> " +
			"is accepted here. You can also input:" +
			"<ul>" +
			"<li><b>@user_login_name</b> to mention/notify an user. " +
			"<li><b>#pull_request_id</b> to reference a pull request. " +
			"<li><b>:emoji:</b> to insert an emoji." +
			"</ul>");

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
    
    $input[0].cachedRequests = [];
    $input.atwho({
    	at: '#',
    	searchKey: "searchKey",
        callbacks: {
        	remoteFilter: function(query, renderCallback) {
                var queryRequests = $input[0].cachedRequests[query];
                if(typeof queryRequests == "object") {
                    renderCallback(queryRequests);
                } else if (typeof queryRequests != "string") {
                	// indicates that request query is ongoing and subsequent 
                	// query using same query string should be waiting
                	$input[0].cachedRequests[query] = "";
                    
                	$input[0].atWhoRequestRenderCallback = renderCallback;
                	$input[0].atWhoRequestQuery = query;
                	callback("requestQuery", query);
                }                             
        	}
        },
        displayTpl: "<li><span class='text-muted'>#${requestId}</span> - ${requestTitle}</li>",
        insertTpl: '#${requestId}', 
        limit: atWhoLimit
    });		
}
