onedev.server.referenceTooltip = {
	init: function($references, callback, translations) {
		onedev.server.referenceTooltip.translations = translations;
		var alignment = {targetX: 0, targetY: 0, x: 0, y: 100};
		$references.hover(function() {
			var $tooltip = $("<div id='reference-tooltip' class='bg-white border rounded'>" + translations["loading"] + "</div>");
			$tooltip.data("trigger", this);
			$tooltip.data("alignment", alignment);
			$("body").append($tooltip);
			callback.call(this);
			return $tooltip;
		}, alignment);
	},
	align: function($tooltip) {
		if ($tooltip.length == 0)
			return;
		var left = 0;
		var right = document.documentElement.clientWidth;
		// Keep clear of scrollbars in the trigger's scroll containers as well as the page scrollbar.
		$($tooltip.data("trigger")).parents().each(function() {
			if (/^(auto|scroll)$/.test($(this).css("overflow-y"))) {
				var rect = this.getBoundingClientRect();
				left = Math.max(left, rect.left + this.clientLeft);
				right = Math.min(right, rect.left + this.clientLeft + this.clientWidth);
			}
		});
		$tooltip[0].style.setProperty("--reference-tooltip-available-width", (right - left) + "px");
		// Measure against the full viewport instead of the space remaining at the loading position.
		$tooltip.css("left", 0);
		$tooltip.align({placement: $tooltip.data("alignment"), target: {element: $tooltip.data("trigger")}});
		var offset = $tooltip.offset();
		offset.left = Math.max(left + 12, Math.min(offset.left - window.scrollX,
				right - 12 - $tooltip.outerWidth())) + window.scrollX;
		$tooltip.offset(offset);
	},
	renderIssueTooltip: function(title, state, stateFontColor, stateBackgroundColor) {
		var $tooltip = $("#reference-tooltip");
		if (title) {
			$tooltip.empty().append("" +
					"<div class='d-flex issue align-items-start'>" +
					"  <span class='state badge mr-3'></span> <span class='title font-weight-bold'></span>" +
					"</div>");
			$tooltip.find(".state").css({
				"color": stateFontColor,
				"background": stateBackgroundColor
			}).text(state);
			$tooltip.find(".title").text(title);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.referenceTooltip.translations["issue-not-exist-or-access-denied"] + "</i>");
		}
		onedev.server.referenceTooltip.align($tooltip);
	},
	renderPullRequestTooltip: function(title, status, statusCss) {
		var $tooltip = $("#reference-tooltip");
		if (title) {
			$tooltip.empty().append("" +
					"<div class='d-flex align-items-start'>" +
					"  <span class='badge status mr-3'></span> <span class='title font-weight-bold'></span>" +
					"</div>");
			$tooltip.find(".status").addClass(statusCss).text(status);
			$tooltip.find(".title").text(title);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.referenceTooltip.translations["pull-request-not-exist-or-access-denied"] + "</i>");
		}
		onedev.server.referenceTooltip.align($tooltip);
	},
	renderBuildTooltip: function(title, iconHref, iconCss) {
		var $tooltip = $("#reference-tooltip");
		if (title) {
			$tooltip.empty().append("" +
					"<div class='d-flex align-items-center'>" +
					"  <svg class='mr-2 " + iconCss + "'><use xlink:href='" + iconHref + "'/></svg> <span class='title font-weight-bold'></span>" +
					"</div>");
			$tooltip.find(".title").text(title);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.referenceTooltip.translations["build-not-exist-or-access-denied"] + "</i>");
		}
		onedev.server.referenceTooltip.align($tooltip);
	},
	renderWorkspaceTooltip: function(title) {
		var $tooltip = $("#reference-tooltip");
		if (title) {
			$tooltip.empty().append("<span class='title'></span>");
			$tooltip.find(".title").text(title);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.referenceTooltip.translations["workspace-not-exist-or-access-denied"] + "</i>");
		}
		onedev.server.referenceTooltip.align($tooltip);
	},
	renderUserTooltip: function(avatarUrl, name) {
		var $tooltip = $("#reference-tooltip");
		$tooltip.empty().append("" +
				"<div class='d-flex align-items-center'>" +
				"  <img class='avatar mr-2'></img> <div class='name font-weight-bold'></div>" +
				"</div>");
		$tooltip.find(".avatar").attr("src", avatarUrl);
		$tooltip.find(".name").text(name);
		onedev.server.referenceTooltip.align($tooltip);
	},
	renderCommitTooltip: function(author, date, dateTitle, commitMessage) {
		var $tooltip = $("#reference-tooltip");
		if (commitMessage) {
			$tooltip.empty().append("" +
					"  <div class='font-weight-bolder mb-2'><span class='author'></span> <span class='date'></span></div>" +
					"  <pre class='body mb-0'></pre>");
			$tooltip.find(".author").text(author);
			var $date = $tooltip.find(".date");
			$date.text(date).attr("data-tippy-content", dateTitle);
			tippy($date[0], {
				delay: [500, 0],
				placement: "auto"
			});
			$tooltip.find(".body").text(commitMessage);
		} else {
			$tooltip.empty().append("<i>" + onedev.server.referenceTooltip.translations["commit-not-exist-or-access-denied"] + "</i>");
		}
		onedev.server.referenceTooltip.align($tooltip);
	}
};
