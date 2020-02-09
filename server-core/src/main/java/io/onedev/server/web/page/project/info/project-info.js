onedev.server.projectInfo = {
	onDomReady: function(switchId) {
		var alternativeProtocol = 'HTTPS';
		
		$("#" + switchId).click(function() {
			$(".clone").toggleClass('hidden');
			
			if (alternativeProtocol === 'SSH') {
				alternativeProtocol = 'HTTPS';
			} else {
				alternativeProtocol = 'SSH';
			}
			
			$(this).text('Use ' + alternativeProtocol);
		});
	}
};