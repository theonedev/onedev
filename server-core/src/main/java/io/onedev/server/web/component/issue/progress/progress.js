onedev.server.issueProgress = {
	onWorkingTimeDomReady: function(containerId, workedMillis) {
		var $container = $("#" + containerId);
		var startMillis = Date.now();
		function updateWorkingTime() {
			if (document.body.contains($container[0])) {
				const workingMinutes = Math.floor((Date.now() - startMillis + workedMillis) / 60000);
				const workingHours = Math.floor(workingMinutes / 60);
				const remainingMinutes = workingMinutes % 60;
				$container.html((workingHours + "").padStart(2, "0") + ":" + (remainingMinutes + "").padStart(2, "0"));
				setTimeout(updateWorkingTime, 60000);
			}
		}
		updateWorkingTime();
	}
}