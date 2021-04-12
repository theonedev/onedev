onedev.server.stepEdit = {
	renderStepIndex: function() {
		$(".steps>.step").each(function() {
			$(this).find(".index").text("#" + ($(this).index()+1));
		});
	}
}