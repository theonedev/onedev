<%
    if (htmlVersion) {
		print """
		    Hello,
			<p style='margin: 16px 0;'>
			Your stopwatch on issue ${stopwatch.issue.FQN} runs overnight,
			and was stopped by OneDev automatically. Elapsed time
			until midnight was added to your work. Please correct the work time if necessary by visiting below link:
			<br>
			<br>
			<a href='${stopwatch.issue.url}'>${stopwatch.issue.url}</a>
    	"""
    } else {
		print """
		    Hello,

			Your stopwatch on issue ${stopwatch.issue.FQN} runs overnight,
			and was stopped by OneDev automatically. Elapsed time
			until midnight was added to your work. Please correct the work time if necessary by visiting below link:

			${stopwatch.issue.url}
    	"""
    }
%>
