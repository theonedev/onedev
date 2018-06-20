onedev.server.issueBoards = {
	onDomReady: function() {
		$("body").css("overflow-y", "hidden");
		var $head = $("#issue-boards>.head");
		var $body = $("#issue-boards>.body");
		var ps = new PerfectScrollbar($body[0]);
		$(window).resize(function() {
			$body.outerHeight($(window).height()-$body.offset().top);
			ps.update();
		});
	}, 
	onColumnDomReady: function(containerId, callback) {
		var $body = $("#" + containerId);
		var ps = new PerfectScrollbar($body[0]);
		
		// Scroll bar will not show in the flex box without this line
		setTimeout(function() {ps.update();}, 100);
		
		$(window).resize(function() {
			ps.update();
		});
		
		if (callback) {
			$body.droppable({
				accept: function($draggable) {
					return $draggable.is("#issue-boards .card") && !$draggable.is("#"+containerId + " .card");
				}, 
				drop: function(event, ui) {
					if ($body.hasClass("issue-droppable")) {
						ui.draggable.data("droppedTo", this);
						var issue = ui.draggable.data("issue");
						if (!ui.draggable.parent().is(this))
							callback(issue);
					}
				}
			});
		}
		$body.children(".card").removeClass("first").first().addClass("first");
	}, 
	onCardDomReady: function(cardId, callback) {
		var $card = $("#" + cardId);
		var $body = $card.closest(".body");
		var bodyContentWidth;
		if (callback) {
			$card.draggable({
				helper: "clone", 
				appendTo: $body.parent().closest(".body"),
				scroll: false,
				start: function(event, ui) {
					$card.addClass("issue-dragging");
					$(ui.helper).outerWidth($card.outerWidth());
					bodyContentWidth = $body.prop("scrollWidth");
					callback($card.data("issue"));
				}, 
				drag: function(event, ui) {
					if ($card[0] != $card.parent().children().first()[0])
						ui.position.top = ui.position.top + 16;
					var left = ui.position.left;
					var right = left + $(ui.helper).outerWidth();
					if (left < $body.scrollLeft()) { 
						if (left >= 0)
							$body.scrollLeft(left);
						else
							$body.scrollLeft(0);
					}
					if ($body.scrollLeft() < right - $body.outerWidth()) {
						if (right <= bodyContentWidth)
							$body.scrollLeft(right - $body.outerWidth());
						else
							$body.scrollLeft(bodyContentWidth - $body.outerWidth());
					}
				},
				stop: function(event, ui) {
					var droppedTo = $card.data("droppedTo");
					if (droppedTo && !$card.parent().is(droppedTo)) {
						/*
						 * After dragging issues to a column, a dialog may open to ask for options. User may 
						 * cancel the dialog and the accept flag will be set to false 
						 */
						function checkAccepted() {
							var accepted = $card.data("accepted");
							if (accepted != undefined) {
								if (accepted) {
									var $countBadge = $card.parent().prev().find(".badge");
									$countBadge.text(parseInt($countBadge.text()) - 1);
									var bodyId = $card.parent().attr("id");
									$card.remove();
									$body.children(".card").removeClass("first").first().addClass("first");
									onedev.server.infiniteScroll.check(bodyId);
								} else {
									$card.removeClass("issue-dragging");
								}
							} else {
								setTimeout(checkAccepted, 10);
							}
							$card.removeData("accepted");
						}
						checkAccepted();
					} else {
						$card.removeClass("issue-dragging");
					}
					$card.removeData("droppedTo");
					$("#issue-boards .body .body").removeClass("issue-droppable");
				}
			});
		}
		$body.children(".card").removeClass("first").first().addClass("first");
	}, 
	markAccepted: function(issueId, accepted) {
		$("#issue-boards .column .body .ui-draggable[data-issue='" + issueId + "']").data("accepted", accepted);
	}
}