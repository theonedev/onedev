onedev.server.newPullRequest = {    
    onCanSendLoad: function(titleAndDescriptionSuggestionCallback, translations) {
        // Do below logic in onLoad as unsaved description will be set in onLoad event of markdown editor, 
        // and we do not want to suggest description if unsaved description is loaded
        if (titleAndDescriptionSuggestionCallback) {
            var icon = onedev.server.isDarkMode()? "sparkle.gif": "sparkle-dark.gif";
            var indicatorHtml = "<div class='ajax-loading-indicator suggesting-indicator'><img src='/~img/" + icon + "' width='16' height='16'></div>";

            var $title = $(".pull-request-title");
            var $titleInput = $title.find("input");
            if ($titleInput.val().length == 0) {
                $title.css("position", "relative");
                $titleInput.prop("readonly", true);
                $titleInput.data("placeholder", $titleInput.attr("placeholder"));
                $titleInput.removeAttr("placeholder");
                
                var $indicator = $(indicatorHtml);
                $indicator.append("<span class='text-muted'>" + translations["suggesting-title"] + "</span>");
    
                var titleCoord = $title.offset();
                var titleInputCoord = $titleInput.offset();
                var left = titleInputCoord.left - titleCoord.left + 5;
                var top = titleInputCoord.top - titleCoord.top + 10;
        
                $indicator.css({
                    position: "absolute",
                    left: left + "px",
                    top: top + "px",
                    zIndex: 1000
                });    
                $title.append($indicator);
            }

            var $description = $(".pull-request-description");
            var $descriptionInput = $description.find("textarea");
            if ($descriptionInput.val().length == 0) {
                $description.css("position", "relative");
                $descriptionInput.prop("readonly", true);
    
                $indicator = $(indicatorHtml);
                $indicator.append("<span class='text-muted'>" + translations["suggesting-description"] + "</span>");
    
                var descriptionCoord = $description.offset();
                var descriptionInputCoord = $descriptionInput.offset();
                var left = descriptionInputCoord.left - descriptionCoord.left + 5;
                var top = descriptionInputCoord.top - descriptionCoord.top + 10;
        
                $indicator.css({
                    position: "absolute",
                    left: left + "px",
                    top: top + "px",
                    zIndex: 1000
                });    
                $description.append($indicator);    
            }
            
            titleAndDescriptionSuggestionCallback($titleInput.val().length == 0, $descriptionInput.val().length == 0);
        }
    },
    titleAndDescriptionSuggested: function(title, description) {
        var $title = $(".pull-request-title");    
        var $titleInput = $title.find("input");
		$titleInput.prop("readonly", false);
		$title.children(".suggesting-indicator").remove();
        var placeholder = $titleInput.data("placeholder");
        if (placeholder)
            $titleInput.attr("placeholder", placeholder);

        if (title)
            $titleInput.val(title);

        var $description = $(".pull-request-description");
        var $descriptionInput = $description.find("textarea");
        $descriptionInput.prop("readonly", false);
        $description.children(".suggesting-indicator").remove();

        if (description) {
            $descriptionInput.val(description);
            $descriptionInput.trigger("input");
        }
    }

}