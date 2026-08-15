onedev.server.pagingNavigator = {
	onDomReady: function(containerId) {
		var $container = $('#' + containerId);
		$container.find('a[disabled=disabled]').each(function() {
			$(this).addClass("disabled");
	  		$(this).parent().addClass('disabled');
		});

		function updateEllipses() {
			$container.find('li.page-forced-visible').removeClass('page-forced-visible');
			var lastIndex = Number($container.find('a.last').attr('data-page-index'));
			var $activePage = $container.find('li.active a.page-number, a.page-number.active').first();
			var activeIndex = Number($activePage.attr('data-page-index'));
			var $visiblePages = $container.find('a.page-number:visible');
			if ($visiblePages.length < 3 && lastIndex >= 2) {
				var $nearestInteriorPage;
				var nearestDistance = Number.MAX_VALUE;
				$container.find('li a.page-number').each(function() {
					var pageIndex = Number($(this).attr('data-page-index'));
					var distance = Math.abs(pageIndex - activeIndex);
					if (pageIndex > 0 && pageIndex < lastIndex && distance < nearestDistance) {
						$nearestInteriorPage = $(this);
						nearestDistance = distance;
					}
				});
				if ($nearestInteriorPage)
					$nearestInteriorPage.parent().addClass('page-forced-visible');
				$visiblePages = $container.find('a.page-number:visible');
			}

			var interiorIndexes = [];
			$visiblePages.each(function() {
				var pageIndex = Number($(this).attr('data-page-index'));
				if (pageIndex > 0 && pageIndex < lastIndex)
					interiorIndexes.push(pageIndex);
			});

			var hasInteriorPages = interiorIndexes.length !== 0;
			var showLeft = hasInteriorPages
				? interiorIndexes[0] > 1
				: lastIndex > 1 && activeIndex === 0;
			var showRight = hasInteriorPages
				? interiorIndexes[interiorIndexes.length - 1] < lastIndex - 1
				: lastIndex > 1 && activeIndex === lastIndex;

			$container.find('.page-ellipsis-left').toggleClass('d-none', !showLeft);
			$container.find('.page-ellipsis-right').toggleClass('d-none', !showRight);
		}

		updateEllipses();
		var resizeNamespace = '.onedev-paging-' + containerId.replace(/[^a-zA-Z0-9_-]/g, '-');
		$(window).off('resize' + resizeNamespace).on('resize' + resizeNamespace, updateEllipses);
	}
}
