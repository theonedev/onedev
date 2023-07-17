<%
    if (htmlVersion) {
		print """
		    Hello,
			<p style='margin: 16px 0;'>
			You are invited to use OneDev, please visit below link to set up account:
			<br>
			<br>
			<a href='${setupAccountUrl}'>${setupAccountUrl}</a>
    	"""
    } else {
		print "Hello,\n\n"
		print "You are invited to use OneDev, please visit below link to set up account:\n\n"
		print setupAccountUrl
    }
%>
