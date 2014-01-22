
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
				return "<img class='img-thumbnail avatar' src='" + user.avatar + "' /> " + user.name + (user.displayName?" (" + user.displayName + ")": "");
			},
			
			formatResult: function(user) {
				return "<div class='user-choice-row'><img class='img-thumbnail avatar avatar-big' src='" + user.avatar + "' />" 
						+ "<p>"+ user.name + (user.displayName?" (" + user.displayName + ")": "") + "</p>"
						+ "<p class='text-muted'>" + user.email + "</p>"
						+ "</div>";
			},
			
			escapeMarkup: function(m) {
				return m;
			},
		}, 
		
		comparableProject: {
			formatSelection: function(project) {
				return project.name;
			},
			
			formatResult: function(project) {
				return project.name;
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

		project: {
			formatSelection: function(project) {
				return project.name
			},
			
			formatResult: function(project) {
				return project.owner + '/' + project.name
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
	
	form: {
		init: function(form) {
			var $form = $(form);
			$form.find('.focusable:first').focus();
			$form.find('.has-error:first .focusable').focus();
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
		},
		
		insertAfter: function(newElement,targetElement) {
			//target is what you want it to go after. Look for this elements parent.
			var parent = targetElement.parentNode;
			if(parent.lastchild == targetElement)
				parent.appendChild(newElement);
			else 
				parent.insertBefore(newElement, targetElement.nextSibling);
		},
		
		insertBefore: function(newElement, targetElement) {
			
		}
	}
};

$(document).ready(function() {
	$('#globalheader a').tooltip({placement: 'bottom'});
	$('#main [data-toggle="tooltip"]').tooltip();
	
	$(document).on('click', function (e) {
        $('.popup-marker').each(function () {
            //the 'is' for buttons that trigger popups
            //the 'has' for icons within a button that triggers a popup
            if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
                $(this).popover('toggle');
            }
        });
    });
});
