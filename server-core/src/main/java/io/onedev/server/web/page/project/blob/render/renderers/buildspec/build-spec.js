onedev.server.buildSpec = {
	onTabDomReady: function(tabClass) {
		$(".build-spec>.head>a").removeClass("active").filter(tabClass).addClass("active");		
	},
    onJobDomReady: function(jobIndex, nameChangeCallback) {
        var $jobs = $(".build-spec>.jobs");
        var $navs = $jobs.find(">.side>.navs");
		$navs.children().removeClass("active").eq(jobIndex).addClass("active");
		
		if (nameChangeCallback) {
	        var $main = $jobs.children(".main");
	        
	        var $input = $main.find(">div>div>.property-name input");
	
	        function syncName() {
	            var name = $input.val().trim();
	            var $name = $navs.children(".active").find("a.select>.label");
	            if (name.length != 0) 
	                $name.text(name);
	            else
	                $name.html("<i>Name not specified</i>");
	            $name.closest(".nav").data("name", name);
	        }
	
	        $input.on("input", syncName);
	
			$input.doneEvents("input", function() {
				nameChangeCallback($input.val());
			}, 250);
			
			syncName();
		}
    }
}