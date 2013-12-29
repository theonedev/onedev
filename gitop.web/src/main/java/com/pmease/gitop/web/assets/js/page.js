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
				return "<img class='img-thumbnail avatar' src='" + user.avatar + "' /> " + user.name + " (" + user.displayName + ")";
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
	
};

$(document).ready(function() {
	$('#globalheader a').tooltip({placement: 'bottom'});
	$('#main [data-toggle="tooltip"]').tooltip();
	$('.focusable:first').focus();
	$('.has-error:first .focusable').focus();
	$('body').on('click', function (e) {
	    $('.popover-link').each(function () {
	        //the 'is' for buttons that trigger popups
	        //the 'has' for icons within a button that triggers a popup
	        if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
	            $(this).popover('hide');
	        }
	    });
	});
	
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
