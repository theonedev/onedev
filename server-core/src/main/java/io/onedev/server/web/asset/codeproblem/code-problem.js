onedev.server.codeProblem = {
	getIcon: function(problems) {
		if (Array.isArray(problems)) {
			var icon = "warning-o";
			for (var i in problems) {
				if (problems[i].severity == "ERROR") {
					icon = "times-circle-o";
					break;
				}
			}
			return icon;
		} else {
			if (problems.severity == "ERROR")
				return "times-circle-o";
			else
				return "warning-o";
		}
	},
	getLinkClass: function(problems) {
		if (Array.isArray(problems)) {
			var hoverClass = "link-warning";
			for (var i in problems) {
				if (problems[i].severity == "ERROR") {
					hoverClass = "link-danger";
					break;
				}
			}
			return hoverClass;
		} else {
			if (problems.severity == "ERROR")
				return "link-danger";
			else
				return "link-warning";			
		}
	},
	getTextClass: function(problems) {
		if (Array.isArray(problems)) {
			var hoverClass = "text-warning";
			for (var i in problems) {
				if (problems[i].severity == "ERROR") {
					hoverClass = "text-danger";
					break;
				}
			}
			return hoverClass;
		} else {
			if (problems.severity == "ERROR")
				return "text-danger";
			else
				return "text-warning";			
		}
	},
	renderProblems: function(problems) {
		if (problems.length != 1) {
			var $container = $("<div></div>");
			for (var i in problems) {
				var problem = problems[i];
				var icon = onedev.server.codeProblem.getIcon(problem);
				var $content = $("<pre class='problem-content mb-0 font-size-sm'></pre>");
				$content.addClass(onedev.server.codeProblem.getTextClass(problem));
				$container.append($content);
				$content.text(problem.content);
				$content.prepend("<svg class='icon icon-sm mr-2'><use xlink:href='" + onedev.server.icons + "#" + icon + "'/></svg>");
				$content.append("<a title='Add comment' class='add-comment link-gray ml-2'><svg class='icon icon-sm mr-2'><use xlink:href='" + onedev.server.icons + "#comment'/></svg></a>");
			}
			return $container.html();
		} else {
			var $container = $("<div><pre class='problem-content mb-0 font-size-sm'></pre></div>");
			var $content = $container.children('.problem-content');
			$content.text(problems[0].content);
			$content.addClass(onedev.server.codeProblem.getTextClass(problems[0]));
			$content.append("<a title='Add comment' class='add-comment link-gray ml-2'><svg class='icon icon-sm mr-2'><use xlink:href='" + onedev.server.icons + "#comment'/></svg></a>");
			return $container.html();
		}
	} 
}