onedev.server.avatarUpload = {
	onDomReady: function(containerId, callback) {
		var $container = $("#" + containerId + ">.avatar-upload");
		var $data = $container.find("input[type=text]")
		var $image = $container.find(".cropping img");
		var $file = $container.find("input[type=file]");
		if ($data.val()) {
			$image.attr("src", $data.val());
		} else {
			$container.addClass("no-avatar");
		}
		function preview() {
			if ($image[0].hasAttribute("src")) {
				if ($image[0].cropper) {
					$image[0].cropper.replace($image.attr("src"));
				} else {
					new Cropper($image[0], {
						aspectRatio: 1,
						autoCropArea: 1,
						crop: function(event) {
							var $cropped = $container.find(".cropped");
							var data = this.cropper.getCroppedCanvas({
								width: $cropped.width(),
								height: $cropped.height(), 
								fillColor: "#FFF" 
							}).toDataURL("image/jpeg");
							$cropped.find("img").attr("src", data);
							$data.val(data);
						}
					});
				}

			}
		}
		preview();

		$file.change(function() {
			if ($file[0].files && $file[0].files.length != 0) {
				var file = $file[0].files[0];
				var reader = new FileReader();
				reader.readAsDataURL(file);
				reader.addEventListener("load", function () {
					callback();
					$container.removeClass("no-avatar");
					$image.attr("src", reader.result);
					preview();
				}, false);
			}
		});
	}
}