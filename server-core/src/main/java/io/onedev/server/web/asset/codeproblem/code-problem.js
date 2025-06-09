onedev.server.codeProblem = {
	getSeverityInfo: function(problems) {
		if (Array.isArray(problems)) {
			var hasCriticalSeverities = false;
			var hasHighSeverities = false;
			var hasMediumSeverities = false;
			for (var i in problems) {
				if (problems[i].severity == "CRITICAL") {
					hasCriticalSeverities = true;
					break;
				} if (problems[i].severity == "HIGH") {
					hasHighSeverities = true;
					break;
				} else if (problems[i].severity == "MEDIUM") {
					hasMediumSeverities = true;
					break;
				}
			}
			if (hasCriticalSeverities || hasHighSeverities)
				return "link-danger";
			else if (hasMediumSeverities)
				return "link-warning";
			else
				return "link-muted";
		} else {
			if (problems.severity == "CRITICAL" || problems.severity == "HIGH")
				return "badge-danger";
			else if (problems.severity == "MEDIUM")
				return "badge-warning";
			else
				return "badge-secondary";
		}
	},
	renderProblems: function(problems, translations) {
		var $container = $("<div></div>");
		for (var i in problems) {
			var problem = problems[i];
			var severityInfo = onedev.server.codeProblem.getSeverityInfo(problem);
			var $content = $("<pre class='problem-content mb-0 font-size-sm'></pre>");
			$container.append($content);
			$content.html(problem.message);
			
			$content.prepend(`<span class='badge badge-sm mr-2 ${severityInfo}'>${translations[problem.severity]}</span>`);
			$content.append(`<a data-tippy-content='${translations["add-problem-comment"]}' class='add-comment ml-2'><svg class='icon icon-sm mr-2'><use xlink:href='${onedev.server.icons}#comment'/></svg></a>`);
		}
		return $container.html();
	} 
}