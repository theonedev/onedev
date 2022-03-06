onedev.server.buildSpec = {
	onTabDomReady: function(tabClass) {
		$(".build-spec>.head>a").removeClass("active").filter(tabClass).addClass("active");		
	},
	markElementActive: function(elementIndex) {
        var $navs = $(".build-spec>.body>.elements>.side>.navs");
		$navs.children().removeClass("active").eq(elementIndex).addClass("active");
	}	
}