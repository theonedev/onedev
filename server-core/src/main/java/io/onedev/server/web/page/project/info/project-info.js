onedev.server.projectInfo = {
	selectedProtocol: '',
	cookieKey: 'onedev.server.projectInfo.selectedProtocol',
	onDomReady: function(switchId) {
		//by default we start with HTTPS protocol
		this.selectedProtocol = 'HTTPS';
		var savedProtocol = Cookies.get(this.cookieKey) || 'HTTPS';
		
		if (savedProtocol === 'SSH') {
			this.switchCloneURL(switchId);
		}
		
		//handle the 'switch protocol' click
		const projectInfo = this;
		$("#" + switchId).click(function() {
			projectInfo.switchCloneURL(switchId);
		});
	},
	switchCloneURL : function(switchId) {
		$(".clone").toggleClass('hidden');
		$("#noKeyWarning").toggleClass('hidden');			
		
		var oldProtocol = this.selectedProtocol;
		
		if (this.selectedProtocol === 'HTTPS') {
			this.selectedProtocol = 'SSH';
		} else {
			this.selectedProtocol = 'HTTPS';
		}
		
		Cookies.set(this.cookieKey, this.selectedProtocol);
		$("#" + switchId).text('Use ' + oldProtocol);		
	}
};