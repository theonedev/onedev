onedev.server.test = {
	onDomReady: function() {
		$(".card").draggable({
			helper: "clone", 
			start: function(event, ui) {
				$(this).hide();
			}, 
			stop: function(event, ui) {
				$(this).show();
			}
		});
	}
}