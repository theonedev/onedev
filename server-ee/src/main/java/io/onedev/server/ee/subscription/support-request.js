onedev.server.supportRequest = {
	onDomReady: function() {
		$(".support-request .submit").click(function() {
			if (onedev.server.supportRequest.validate()) {
				var $form = $(".support-request");
				onedev.server.form.markClean($form);
				$form.submit();
			} else {
				$(".modal").scrollTop(0);
			}
			return false;			
		});
	},
	validate: function() {
		function isEmpty(value) {
			return value == null || value.trim().length == 0;
		}
		
		var hasErrors = false;

		var $contactEmail = $(".support-request .contact-email");
		if (isEmpty($contactEmail.val())) {
			$contactEmail.parent().addClass("is-invalid");
			hasErrors = true;
		} else {
			$contactEmail.parent().removeClass("is-invalid");
		}
		
		var $contactName = $(".support-request .contact-name");
		if (isEmpty($contactName.val())) {
			$contactName.parent().addClass("is-invalid");
			hasErrors = true;
		} else {
			$contactName.parent().removeClass("is-invalid");
		}
		
		var $summary = $(".support-request .summary");
		if (isEmpty($summary.val())) {
			$summary.parent().addClass("is-invalid");
			hasErrors = true;
		} else {
			$summary.parent().removeClass("is-invalid");
		}
		
		return !hasErrors;
	}
}