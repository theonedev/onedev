onedev.server.issueBoards = {
	onColumnDomReady: function(containerId, callback) {
		if (callback) {
			var $body = $("#" + containerId);
			$body.data("dropCallback", callback);
			$body.droppable({
				accept: function($draggable) {
					return $draggable.is(".issue-boards .board-card");
				},
				over: function(event, ui) {
					$(ui.helper).data("droppable", this);
				}
			});
		}
	}, 
	onCardDomReady: function(cardId, callback) {
		var $card = $("#" + cardId);
        var $container = $card.closest(".columns");
		if (callback) {
			var deleteAnimationHelper, moveAnimationHelper, addAnimationHelper;
			var moveTimeout;
			function finishAnimation() {
				if (deleteAnimationHelper) {
					$(deleteAnimationHelper).stop().remove();
					deleteAnimationHelper = undefined;
				}
				if (moveAnimationHelper) {
					$(moveAnimationHelper).stop().remove();
					moveAnimationHelper = undefined;
				}
				if (addAnimationHelper) {
					$(addAnimationHelper).stop().remove();
					addAnimationHelper = undefined;
				}
				$card.removeClass("invisible");
			}
			$card.draggable({
				helper: "clone", 
				appendTo: $container,
				scroll: false,
				start: function(event, ui) {
					// pretend that we are in ajax operation to prevent websocket auto-update while dragging
					onedev.server.ajaxRequests.count++;
					$card.addClass("placeholder");
					$(ui.helper).outerWidth($card.outerWidth());
					callback($card.data("issue"));
				}, 
				drag: function(event, ui) {
					var $uiHelper = $(ui.helper);
					var uiHelperLeft = $uiHelper.offset().left;
					var containerLeft = $container.offset().left;
					var containerContentWidth = $container.prop("scrollWidth");
					
					if (uiHelperLeft < containerLeft) { 
						var scrollLeft = $container.scrollLeft();
						var newScrollLeft = scrollLeft - (containerLeft - uiHelperLeft);
						if (newScrollLeft > 0)
							$container.scrollLeft(newScrollLeft);
						else
							$container.scrollLeft(0);
					}
					
					uiHelperLeft = $uiHelper.offset().left;
					containerLeft = $container.offset().left;
					
					var uiHelperRight = uiHelperLeft + $uiHelper.outerWidth();
					var containerWidth = $container.outerWidth();
					var containerRight = containerLeft + containerWidth;
					
					if (uiHelperRight > containerRight) {
						var scrollLeft = $container.scrollLeft();
						var newScrollLeft = scrollLeft + (uiHelperRight - containerRight);
						if (newScrollLeft <= containerContentWidth - containerWidth)
							$container.scrollLeft(newScrollLeft);
						else
							$container.scrollLeft(containerContentWidth - containerWidth);
					}
					
					var $droppable = $($uiHelper.data("droppable"));
					if ($droppable.hasClass("issue-droppable")) {
						var uiHelperTop = $uiHelper.offset().top;
						var droppableTop = $droppable.offset().top;
						var uiHelperHeight = $uiHelper.outerHeight();
						var uiHelperBottom = uiHelperTop + uiHelperHeight;
						var droppableHeight = $droppable.outerHeight();
						var droppableBottom = droppableTop + droppableHeight;
						
						if (uiHelperTop < droppableTop) {
							var scrollTop = $droppable.scrollTop();
							var newScrollTop = scrollTop - (droppableTop - uiHelperTop);
							if (newScrollTop > 0)
								$droppable.scrollTop(newScrollTop);
							else
								$droppable.scrollTop(0);
						}

						if (uiHelperBottom > droppableBottom) {
							var scrollTop = $droppable.scrollTop();
							var newScrollTop = scrollTop + (uiHelperBottom - droppableBottom);
							var droppableContentHeight = $droppable.prop("scrollHeight");
							if (newScrollTop <= droppableContentHeight - droppableHeight)
								$droppable.scrollTop(newScrollTop);
							else
								$droppable.scrollTop(droppableContentHeight - droppableHeight);
						}
						
						var uiHelperCenter = uiHelperTop + uiHelperHeight / 2;
						var $cards = $droppable.children(".board-card");
						var insertBeforeCard;
						$cards.each(function() {
							var $card = $(this);
							var cardCenter = $card.offset().top + $card.outerHeight() / 2;
							if (uiHelperCenter < cardCenter) {
								insertBeforeCard = this;
								return false;
							}
						});
						function beforeMoveCard() {
							finishAnimation();
							var $deleteAnimationHelper = $("<div></div>");
							$deleteAnimationHelper.css("padding-bottom", $card.outerHeight(true) + "px");
							var $moveAnimationHelper = $card.clone();
							$moveAnimationHelper.css("position", "absolute");
							$moveAnimationHelper.css("z-index", "0");
							$moveAnimationHelper.outerWidth($card.outerWidth());
							$moveAnimationHelper.outerHeight($card.outerHeight());
							$moveAnimationHelper.css($card.offset());
							$card.addClass("invisible");
							$deleteAnimationHelper.hide();
							$deleteAnimationHelper.insertBefore($card);
							$("body").append($moveAnimationHelper);
							deleteAnimationHelper = $deleteAnimationHelper[0];
							moveAnimationHelper = $moveAnimationHelper[0];
						}
						function afterMoveCard() {
							var offset = $card.offset();
							var $deleteAnimationHelper = $(deleteAnimationHelper);
							var $moveAnimationHelper = $(moveAnimationHelper);
							$deleteAnimationHelper.show();
							var $addAnimationHelper = $("<div></div>");
							$addAnimationHelper.insertBefore($card);
							addAnimationHelper = $addAnimationHelper[0];

							var animationDuration = 100;

							var paddingBottom = $deleteAnimationHelper.css("padding-bottom");
							
							$deleteAnimationHelper.animate({
								"padding-bottom": "0",
							}, animationDuration, finishAnimation);

							$moveAnimationHelper.animate({
								left: offset.left,
								top: offset.top
							}, animationDuration, finishAnimation);

							$addAnimationHelper.animate({
								"padding-bottom": paddingBottom,
							}, animationDuration, finishAnimation);
						}

						if (insertBeforeCard) {
							if (insertBeforeCard !== $card[0] && insertBeforeCard !== $card.next()[0]) {
								if ($card.hasClass("invisible")) {
									$card.insertBefore(insertBeforeCard);
								} else {
									beforeMoveCard();
									$card.insertBefore(insertBeforeCard);
									afterMoveCard();
								}
							}
						} else {
							if ($droppable.children().last()[0] !== $card[0]) {
								if ($card.hasClass("invisible")) {
									$droppable.append($card);
								} else {
									beforeMoveCard();
									$droppable.append($card);
									afterMoveCard();
								}
							}
						}
					}
				},
				stop: function(event, ui) {
					finishAnimation();
					if (moveTimeout) {
						clearTimeout(moveTimeout);
						moveTimeout = undefined;
					}
					var callback = $card.closest(".ui-droppable").data("dropCallback");
					if (callback) 
						callback($card.data("issue"), $card.parent().children(".board-card").index($card));						
					$card.removeData("droppable").removeClass("placeholder");
					$(".issue-boards .ui-droppable").removeClass("issue-droppable");
				}
			});
		}
	},
	onCardDropped: function(fromDroppableId, fromIndex, toDroppableId, toIndex, accepted) {
		var $fromDroppable = $("#" + fromDroppableId);
		var $toDroppable = $("#" + toDroppableId);
		onedev.server.ajaxRequests.count--;
		if (accepted) {
			var $cardCount = $fromDroppable.parent().find(".card-count");
			$cardCount.text(parseInt($cardCount.text()) - 1);
			onedev.server.issueBoards.updateAddToIteration(fromDroppableId);
			$cardCount = $toDroppable.parent().find(".card-count");
			$cardCount.text(parseInt($cardCount.text()) + 1);
			onedev.server.issueBoards.updateAddToIteration(toDroppableId);
		} else {
			var $card = $toDroppable.children().eq(toIndex+1);
			var $insertBeforeCard = $fromDroppable.children().eq(fromIndex + 1);
			if ($insertBeforeCard.length != 0)
				$card.insertBefore($insertBeforeCard);
			else
				$fromDroppable.append($card);
		}			
	},
	changeCardCount: function(droppableId, change) {
		var $cardCount = $("#" + droppableId).parent().find(".card-count");
		$cardCount.text(parseInt($cardCount.text()) + change);
		onedev.server.issueBoards.updateAddToIteration(droppableId);
	},
	updateAddToIteration: function(droppableId) {
		var $droppable = $("#" + droppableId);
		var $addToIteration = $droppable.parent().find(".add-to-iteration");
		var cardCount = parseInt($droppable.parent().find(".card-count").text());
		if (cardCount > 0)
			$addToIteration.show();
		else
			$addToIteration.hide();
	}
}