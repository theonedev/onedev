onedev.server.imageUpload = {
	onDomReady: function(containerId, callback) {
		var $container = $("#" + containerId + ">.image-upload");
		var $data = $container.find("input[type=text]")
		var $image = $container.find("img");
		var $file = $container.find("input[type=file]");
		if ($data.val()) {
			$image.attr("src", $data.val());
		} else {
			$container.addClass("no-image");
		}
		$file.change(function() {
			if ($file[0].files && $file[0].files.length != 0) {
				var file = $file[0].files[0];
				var reader = new FileReader();
				reader.readAsDataURL(file);
				reader.addEventListener("load", function () {
					callback();
					$container.removeClass("no-image");
					$image.attr("src", reader.result);
					$data.val(reader.result);
				}, false);
			}
		});
	}
}