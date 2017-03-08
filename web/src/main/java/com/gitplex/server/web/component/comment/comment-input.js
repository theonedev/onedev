gitplex.server.comment = function(inputId, atWhoLimit, callback) {
	var $input = $("#" + inputId);

	$input.prevAll(".md-help").html(
			"<a href='https://help.github.com/articles/github-flavored-markdown/' target='_blank'>GitHub flavored markdown</a> " +
			"is accepted here. You can also input:" +
			"<ul>" +
			"<li><b>@user_login_name</b> to mention/notify an user. " +
			"<li><b>#pull_request_number</b> to reference a pull request. " +
			"<li><b>:emoji:</b> to insert an emoji." +
			"</ul>");

    $input.atwho({
    	at: '@',
    	searchKey: "searchKey",
        callbacks: {
        	remoteFilter: function(query, renderCallback) {
            	$input.data("atWhoUserRenderCallback", renderCallback);
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
    
    $input.atwho({
    	at: '#',
    	searchKey: "searchKey",
        callbacks: {
        	remoteFilter: function(query, renderCallback) {
            	$input.data("atWhoRequestRenderCallback", renderCallback);
            	callback("requestQuery", query);
        	}
        },
        displayTpl: "<li><span class='text-muted'>#${requestNumber}</span> - ${requestTitle}</li>",
        insertTpl: '#${requestNumber}', 
        limit: atWhoLimit
    });		
};
