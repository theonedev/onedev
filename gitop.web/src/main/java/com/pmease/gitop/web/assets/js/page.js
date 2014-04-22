
var gitop = {
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
		user: {
			formatSelection: function(user) {
				return "<img class='img-thumbnail avatar' src='" + user.avatar + "' /> " + user.name + (user.fullName?" (" + user.fullName + ")": "");
			},
			
			formatResult: function(user) {
				return "<div class='user-choice-row'><img class='img-thumbnail avatar avatar-big' src='" + user.avatar + "' />" 
						+ "<p>"+ user.name + (user.fullName?" (" + user.fullName + ")": "") + "</p>"
						+ "<p class='text-muted'>" + user.emailAddress + "</p>"
						+ "</div>";
			},
			
			escapeMarkup: function(m) {
				return m;
			},
		}, 
		
		comparableRepository: {
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

		repository: {
			formatSelection: function(repository) {
				return repository.name
			},
			
			formatResult: function(repository) {
				return repository.owner + '/' + repository.name
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
	
	form: {
		/**
		 * Focus on the first focusable field (input, textarea),
		 * or focus on the first error field
		 * 
		 * @param {String} form selector
		 */
		setupFocus: function(form) {
			var $form = $(form);
			$form.find('.focusable:first').focus();
			$form.find('.has-error:first .focusable').focus();
		},
		
		/**
		 * Enable/disable the submit button when this form is dirty or not
		 * 
		 * @param {String} form selector
		 */
		areYouSure: function(form) {
			var $form = $(form);
			$form.find('.btn-submit').attr('disabled', 'disabled');
			
			$form.areYouSure({
				silent: 'true',
				change: function() {
					if ($(this).hasClass('dirty')) {
						$(this).find('.btn-submit').removeAttr('disabled');
					} else {
						$(this).find('.btn-submit').attr('disabled', 'disabled')
					}
				}
			});
		},
		
		/**
		 * Initialize the form's focus and install dirty check for submit button
		 * 
		 * @param {String} form selector
		 */
		init: function(form) {
			gitop.form.setupFocus(form);
			gitop.form.areYouSure(form);
		},
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
	}
};

$(document).ready(function() {
	$('#globalheader a').tooltip({placement: 'bottom'});
	$('#main .has-tip').tooltip();
	
	$(window).on("beforeunload", function() {
		$(":focus").trigger("blur");
	});
});
