onedev.server.issueBoards = {
	onColumnDomReady: function(containerId, callback) {
		var $body = $("#" + containerId);

		if (callback) {
			$body.droppable({
				accept: function($draggable) {
					return $draggable.is(".issue-boards .board-card") && !$draggable.is("#"+containerId + " .board-card");
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
	}, 
	onCardDomReady: function(cardId, callback) {
		var $card = $("#" + cardId);
        var $container = $card.closest(".columns");
		var containerContentWidth;
		if (callback) {
			$card.draggable({
				helper: "clone", 
				appendTo: $container,
				scroll: false,
				start: function(event, ui) {
					// pretend that we are in ajax operation to prevent websocket auto-update while dragging
					onedev.server.ajaxRequests.count++;					
					$card.addClass("issue-dragging");
					$(ui.helper).outerWidth($card.outerWidth());
					containerContentWidth = $container.prop("scrollWidth");
					callback($card.data("issue"));
				}, 
				drag: function(event, ui) {
					var left = ui.position.left;
					var right = left + $(ui.helper).outerWidth();
					if (left < $container.scrollLeft()) { 
						if (left >= 0)
							$container.scrollLeft(left);
						else
							$container.scrollLeft(0);
					}
					if ($container.scrollLeft() < right - $container.outerWidth()) {
						if (right <= containerContentWidth)
							$container.scrollLeft(right - $container.outerWidth());
						else
							$container.scrollLeft(containerContentWidth - $container.outerWidth());
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
								if (!accepted) {
									$card.removeClass("issue-dragging");
								}
								onedev.server.ajaxRequests.count--;					
							} else {
								setTimeout(checkAccepted, 10);
							}
							$card.removeData("accepted");
						}
						checkAccepted();
					} else {
						$card.removeClass("issue-dragging");
						onedev.server.ajaxRequests.count--;		
					}
					$card.removeData("droppedTo");
					$(".issue-boards .body .body").removeClass("issue-droppable");
				}
			});
		}
	}, 
	markAccepted: function(issueId, accepted) {
		var $card = $(".issue-boards .column .body .ui-draggable.issue-dragging[data-issue='" + issueId + "']");
		$card.data("accepted", accepted);
	} 
}