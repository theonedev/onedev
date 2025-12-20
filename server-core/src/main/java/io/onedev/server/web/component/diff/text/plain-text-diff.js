onedev.server.plainTextDiff = {
	onDomReady: function(containerId, callback, fileName) {
		var $container = $("#" + containerId);
		$container.data("callback", callback);
		$container.data("fileName", fileName);
		
		$container.find("td.expander a").each(function() {
			tippy(this, {
				delay: [500, 0],
				placement: 'auto'
			});
		});
		
		if (fileName) {
			onedev.server.plainTextDiff.highlightSyntax($container, fileName);
		}
	},
	expand: function(containerId, blockIndex, expandedHtml) {
		var $container = $("#" + containerId);
		var $expanderTr = $container.find(".expander" + blockIndex);
		var $prevTr = $expanderTr.prev();
		var $nextTr = $expanderTr.next();
		$expanderTr.replaceWith(expandedHtml);
		
		var $expandedTrs;
		if ($prevTr.length != 0 && $nextTr.length != 0) {
			$expandedTrs = $prevTr.nextAll().filter($nextTr.prevAll());
		} else if ($prevTr.length != 0) {
			$expandedTrs = $prevTr.nextAll();
		} else {
			$expandedTrs = $nextTr.prevAll();
		}
		
		$container.find("td.expander a").each(function() {
			tippy(this, {
				delay: [500, 0],
				placement: 'auto'
			});
		});
		
		var fileName = $container.data("fileName");
		if (fileName) {
			var $textDiff = $container.find("table.text-diff");
			$textDiff.removeClass("syntax-highlighted");
			onedev.server.plainTextDiff.highlightSyntax($container, fileName);
		}
		$(document).trigger('afterElementReplace', containerId);
	},
	highlightSyntax: function($container, fileName) {
		var $textDiff = $container.find("table.text-diff");
		if ($textDiff.hasClass("syntax-highlighted"))
			return;
			
		var $trs = $textDiff.find("tr.code");
		
		onedev.server.textDiff.highlightSyntaxForUnifiedDiff($trs, fileName, fileName, function() {
			$textDiff.addClass("syntax-highlighted");
		});
	}
};

