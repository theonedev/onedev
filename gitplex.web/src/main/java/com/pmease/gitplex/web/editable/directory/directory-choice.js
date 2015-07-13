gitplex.selectDirectory = function(inputId, triggerId, path, append) {
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
};