var gitplex = {
	selectDirectory: function(inputId, triggerId, path, append) {
		var input = $("#" + inputId);
		if (!append) {
			input.val(path);
			pmease.commons.dropdown.hide($("#" + triggerId).closest(".dropdown-panel")[0].id);
		} else {
			var value = input.val();
			if (value.match(/.*,\s*$/g) || $.trim(value).length == 0)
				input.val(value + path);
			else
				input.val(value + ", " + path);
		}
	},
	
	choiceFormatter: {
		repository: {
			formatSelection: function(repository) {
				return repository.name;
			},
			
			formatResult: function(repository) {
				return repository.name;
			},
			
			escapeMarkup: function(m) {
				return m;
			}
		}, 

		branch: {
			formatSelection: function(branch) {
				return branch.name;
			},
			
			formatResult: function(branch) {
				return branch.name;
			},
			
			escapeMarkup: function(m) {
				return m;
			}
		},

		team: {
			formatSelection: function(team) {
				return team.name;
			},
			
			formatResult: function(team) {
				return team.name;
			},
			
			escapeMarkup: function(m) {
				return m;
			}
		},
		
	},
	
	commitMessage: {
		toggle: function(toggle) {
			$(toggle).click(function(e) { 
				var $self = $(this); 
				$self.toggleClass('collapsed'); 
				$self.parent().siblings('.detailed-message').toggle(); 
			});
		}
	},
	
	utils: {
		/**
		 * Trigger a callback when the selected images are loaded:
		 * @param {String} selector
		 * @param {Function} callback
		 */
		 onImageLoad: function(selector, callback){
		    $(selector).each(function(){
		        if (this.complete || /*for IE 10-*/ $(this).height() > 0) {
		            callback.apply(this);
		        }
		        else {
		            $(this).on('load', function(){
		                callback.apply(this);
		            });
		        }
		    });
		}
	}, 
	
	comments: {
		position: function() {
			$("#comments-placeholder").children("table").each(function() {
				var $lineComments = $(this);
				var id = $lineComments.attr("id");
				var lineId = id.substring("comment-".length);
				var $line = $("#" + lineId);
				var $tr = $("<tr class='line comments'><td colspan='3'></td></tr>").insertAfter($line);
				$tr.children().append($lineComments);
				$line.find("a.add-comment").hide();
			});
		},
		beforeAdd: function(index) {
			var $line = $("#diffline-" + index); 

			var $tr = $("<tr class='line comments'><td colspan='3'></td></tr>").insertAfter($line);
			var $addComment = $("#add-comment");
			var $commentsRow = $addComment.closest("tr.line.comments");
			$addComment.find("textarea").val("");
			$tr.children().append($addComment);
			$commentsRow.prev().find("a.add-comment").show();
			$commentsRow.remove();
			$addComment.find("textarea").focus();
			pmease.commons.form.trackDirty($addComment);
			$line.find("a.add-comment").hide();
		},
		afterAdd: function(index) {
			$("#comments-placeholder").append($("#add-comment"));
			var $line = $("#diffline-" + index);
			$line.next().find("td").append($("#comment-diffline-" + index));
			$("#add-comment").areYouSure({"silent": "true"});
		},
		cancelAdd: function(index) {
			$("#comments-placeholder").append($("#add-comment"));
			var $line = $("#diffline-" + index);
			$line.next().remove(); 
			$line.find("a.add-comment").show();
			$("#add-comment").areYouSure({"silent": "true"});
		},
		afterRemove: function(index) {
			$("#comment-diffline-" + index).closest("tr").remove();
			$("#diffline-" + index).find("a.add-comment").show();
		}
	},
	
	spaceGreedy: {
		mouseDown: false,
		check: function() {
			var $topHideable = $(".top.hideable");
			if (gitplex.spaceGreedy.getScrollTop && !gitplex.spaceGreedy.mouseDown && !$topHideable.is(":animated")) {
				if ($topHideable.is(":visible")) {
					var height = 0;
					$(".hideable").each(function() {
						height += $(this).outerHeight();
					});
					if (gitplex.spaceGreedy.getScrollTop()>height+10) {
						var completed = 0;
						$topHideable.slideUp(100, function() {
							completed++;
							if (completed == $topHideable.length) {
								$(".bottom.hideable").hide();
								$(window).resize();
							}
						});
					}
				} else if (gitplex.spaceGreedy.getScrollTop() < 5) {
					var completed = 0;
					$topHideable.slideDown(100, function() {
						completed++;
						if (completed == $topHideable.length) {
							$(".bottom.hideable").show();
							$(window).resize();
						}
					});
				}
			}
			setTimeout(gitplex.spaceGreedy.check, 100);
		}
	}
};

$(document).ready(function() {
	$(window).on("beforeunload", function() {
		$(":focus").trigger("blur");
	});
	
	$(window).load(function() {
		$(document).mousedown(function() { 
			console.log("down");
			gitplex.spaceGreedy.mouseDown = true;
		});
		$(document).mouseup(function() {
			console.log("up");
			gitplex.spaceGreedy.mouseDown = false;
		});		
		gitplex.spaceGreedy.check();
	});
});
