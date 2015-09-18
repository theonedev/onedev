initUpload = function(url) {
	var pasteTarget = document.getElementById("pasteTarget");
	pasteTarget.addEventListener("paste", handlePaste);
	
	$("#file").change(function() {
		console.log("hello");
		uploadFile(this.files[0]);
	});

	pasteTarget.addEventListener("dragover", function(e) {
		e.stopPropagation();
		e.preventDefault();		
	}, false);
	
	pasteTarget.addEventListener("dragleave", function(e) {
		e.stopPropagation();
		e.preventDefault();		
	}, false);
	
	pasteTarget.addEventListener("drop", function(e) {
		e.stopPropagation();
		e.preventDefault();		
		var files = e.target.files || e.dataTransfer.files;
		uploadFile(files[0]);
	}, false);
	
	function handlePaste(e) {
		for (var i = 0; i < e.clipboardData.items.length; i++) {
			var item = e.clipboardData.items[i];
			console.log("Item type: " + item.type);
			if (item.type.indexOf("image") != -1) {
				uploadFile(item.getAsFile());
			} else {
				alert("Discarding non-image paste data");
			}
		}
	}
	function uploadFile(file) {
		var xhr = new XMLHttpRequest();
		xhr.upload.onprogress = function(e) {
			var percentComplete = (e.loaded / e.total) * 100;
			console.log("Uploaded: " + percentComplete + "%");
		};
		xhr.onload = function() {
			if (xhr.status == 200) {
				alert("Sucess! Upload completed.\n\n" + xhr.responseText);
			} else {
				alert("Error! Upload failed");
			}
		};
		xhr.onerror = function() {
			alert("Error! Upload failed. Can not connect to server.");
		};
		console.log("post to: " + url);
		xhr.open("POST", url, true);
		xhr.setRequestHeader("Content-Type", file.type);
		xhr.send(file);
	}
}
