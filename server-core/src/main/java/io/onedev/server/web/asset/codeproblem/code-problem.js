onedev.server.codeProblem = {
	getIconInfo: function(problems) {
		if (Array.isArray(problems)) {
			var hasErrors = false;
			var hasWarnings = false;
			for (var i in problems) {
				if (problems[i].severity == "ERROR") {
					hasErrors = true;
					break;
				} else if (problems[i].severity == "WARNING") {
					hasWarnings = true;
				}
			}
			if (hasErrors)
				return ["times-circle-o", "link-danger", "text-danger"];
			else if (hasWarnings)
				return ["warning-o", "link-warning", "text-warning"];
			else
				return ["info-circle-o", "link-info", "text-info"];
		} else {
			if (problems.severity == "ERROR")
				return ["times-circle-o", "link-danger", "text-danger"];
			else if (problems.severity == "WARNING")
				return ["warning-o", "link-warning", "text-warning"];
			else
				return ["info-circle-o", "link-info", "text-info"];
		}
	},
	renderProblems: function(problems) {
		if (problems.length != 1) {
			var $container = $("<div></div>");
			for (var i in problems) {
				var problem = problems[i];
				var iconInfo = onedev.server.codeProblem.getIconInfo(problem);
				var $content = $("<pre class='problem-content mb-0 font-size-sm'></pre>");
				$container.append($content);
				$content.text(problem.content);
				
				$content.prepend(`<svg class='icon icon-sm mr-2 ${iconInfo[2]}'><use xlink:href='${onedev.server.icons}#${iconInfo[0]}'/></svg>`);
				$content.append("<a title='Add comment' class='add-comment ml-2'><svg class='icon icon-sm mr-2'><use xlink:href='" + onedev.server.icons + "#comment'/></svg></a>");
			}
			return $container.html();
		} else {
			var $container = $("<div><pre class='problem-content mb-0 font-size-sm'></pre></div>");
			var $content = $container.children('.problem-content');
			$content.text(problems[0].content);
			$content.append("<a title='Add comment' class='add-comment ml-2'><svg class='icon icon-sm mr-2'><use xlink:href='" + onedev.server.icons + "#comment'/></svg></a>");
			return $container.html();
		}
	} 
}