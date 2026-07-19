(function() {
	var compactMode = "project-path-compact-mode";
	var truncatedMode = "project-path-truncated-mode";
	var hiddenMode = "project-path-hidden-mode";
	var minimumTruncatedPathWidth = 48;

	function isTruncated(element) {
		return element.scrollWidth > element.clientWidth + 1;
	}

	function fitProjectPath(title) {
		var $title = $(title);
		var fullPath = $title.find(".project-path-full")[0];
		if (!fullPath)
			return;

		$title.removeClass(compactMode + " " + truncatedMode + " " + hiddenMode);
		var fullPathIsTruncated = isTruncated(fullPath) || Array.prototype.some.call(
			fullPath.querySelectorAll("a.text-truncate"), isTruncated);
		if (!fullPathIsTruncated)
			return;

		$title.addClass(compactMode);
		if (title.scrollWidth <= title.clientWidth + 1)
			return;

		$title.addClass(truncatedMode);
		var compactPath = $title.find(".project-path-compact")[0];
		if (compactPath.clientWidth < minimumTruncatedPathWidth)
			$title.addClass(hiddenMode);
	}

	function setup() {
		var title = document.querySelector(".topbar-title");
		if (!title || !title.querySelector(".project-path-full"))
			return;

		var scheduled = false;
		function scheduleFit() {
			if (!scheduled) {
				scheduled = true;
				window.requestAnimationFrame(function() {
					scheduled = false;
					fitProjectPath(title);
				});
			}
		}

		if (window.ResizeObserver)
			new ResizeObserver(scheduleFit).observe(title);
		$(window).on("resize", scheduleFit);
		scheduleFit();
	}

	$(setup);
})();
