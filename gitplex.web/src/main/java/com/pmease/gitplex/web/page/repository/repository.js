gitplex.repository = {
	initClipboard: function(copyButtonId) {
		var client = new ZeroClipboard(document.getElementById(copyButtonId));
		client.on("ready", function(readyEvent) {
			client.on("aftercopy", function(event) {
				console.log("copied");
			});
		});		
	}
};
