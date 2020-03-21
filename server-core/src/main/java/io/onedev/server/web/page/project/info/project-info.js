onedev.server.projectInfo = {
	onDomReady: function(switchId) {
		var alternativeProtocol = 'SSH';
		
		$("#" + switchId).click(function() {
			$(".clone").toggleClass('hidden');
			$("#noKeyWarning").toggleClass('hidden');			
			
			if (alternativeProtocol === 'HTTPS') {
				alternativeProtocol = 'SSH';
			} else {
				alternativeProtocol = 'HTTPS';
			}
			
			$(this).text('Use ' + alternativeProtocol);
		});
	}
};