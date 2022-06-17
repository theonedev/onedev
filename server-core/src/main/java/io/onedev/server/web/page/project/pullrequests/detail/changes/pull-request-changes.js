onedev.server.requestChanges = {
	initCommitSelector: function(callback, baseCommit, fromIndex, toIndex) {
		var $commits = $(".commits-selector ul a");
		if (fromIndex!=undefined && toIndex!=undefined) 
			$commits.slice(fromIndex, toIndex+1).addClass("selected");
			
		fromIndex = toIndex = undefined;
			
		function onSelectionChanged() {
			var oldCommit;
			if (fromIndex == 0)
				oldCommit = baseCommit;
			else
				oldCommit = $($(".commits-selector ul li").get(fromIndex-1)).data("hash");
			var newCommit = $($(".commits-selector ul li").get(toIndex)).data("hash");
			callback(oldCommit, newCommit);
		}
		
		$commits.mouseover(function(e) {
			if (e.shiftKey) {
				if (fromIndex != undefined) {
					$commits.removeClass("shift-selected");
					var index = $commits.index(this);
					if (fromIndex < index) 
						$commits.slice(fromIndex, index+1).addClass("shift-selected");
					else 
						$commits.slice(index, fromIndex+1).addClass("shift-selected");
				}
			} else {
				$commits.removeClass("shift-selected");
				if (fromIndex != undefined)
					$commits.eq(fromIndex).addClass("shift-selected");
			}
		});
		
		$commits.click(function(e) {
			var index = $commits.index(this);
			if (e.shiftKey) {
				if (fromIndex == undefined) { 
					fromIndex = index;
					$(this).addClass("shift-selected");
				} else if (fromIndex == index) {
					fromIndex = undefined;
					$(this).removeClass("shift-selected");
				} else {
					if (fromIndex < index) {
						toIndex = index;
					} else {
						toIndex = fromIndex;
						fromIndex = index;
					}
					onSelectionChanged();						
				}
			} else {
				fromIndex = toIndex = index;
				onSelectionChanged();
			}
		});
	}
};