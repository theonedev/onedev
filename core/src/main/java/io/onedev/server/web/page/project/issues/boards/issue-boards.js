onedev.server.issueBoards = {
	onDomReady: function() {
		$("body").css("overflow-y", "hidden");
		var $head = $("#issue-boards>.head");
		var $body = $("#issue-boards>.body");
		if ($body.length != 0) {
			// Do not use perfect scroll on Device as it may cause scroll issue
			if (onedev.server.util.isDevice()) {
				$(window).resize(function() {
					$body.outerHeight($(window).height()-$body.offset().top);
				});
			} else {
				var ps = new PerfectScrollbar($body[0]);
				$(window).resize(function() {
					$body.outerHeight($(window).height()-$body.offset().top);
					ps.update();
				});
			}
		}
	}, 
	onColumnDomReady: function(containerId, callback) {
		var $body = $("#" + containerId);
		
		// Do not use perfect scroll on Device as it may cause scroll issue
		if (!onedev.server.util.isDevice()) {
			var ps = new PerfectScrollbar($body[0]);
			
			// Scroll bar will not show in the flex box without this line
			setTimeout(function() {ps.update();}, 100);
			
			$(window).resize(function() {
				ps.update();
			});
		}

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
					// pretend that we are in ajax operation to prevent websocket auto-update while dragging
					onedev.server.ajaxRequests.count++;					
					$card.addClass("issue-dragging");
					$(ui.helper).outerWidth($card.outerWidth());
					bodyContentWidth = $body.prop("scrollWidth");
					callback($card.data("issue"));
				}, 
				drag: function(event, ui) {
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
					$("#issue-boards .body .body").removeClass("issue-droppable");
				}
			});
		}
	}, 
	markAccepted: function(issueId, accepted) {
		var $card = $("#issue-boards .column .body .ui-draggable.issue-dragging[data-issue='" + issueId + "']");
		$card.data("accepted", accepted);
	} 
}