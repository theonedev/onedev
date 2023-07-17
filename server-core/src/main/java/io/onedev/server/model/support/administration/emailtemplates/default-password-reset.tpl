<%
    if (htmlVersion) {
        print """
            Dear ${user.displayName},
            <p style='margin: 16px 0;'>
            Per your request, password of account \"${user.name}\" at <a href='${serverUrl}'>${serverUrl}</a> has been reset to:
            <br><br>
			${newPassword}
			<br><br>
			Please login and change the password in your earliest convenience.
        """
    } else {
        print "Dear ${user.displayName},\n\n"
        print "Per your request, password of account \"${user.name}\" at \"${serverUrl}\" has been reset to:\n\n"
        print newPassword + "\n\n"
        print "Please login and change the password in your earliest convenience."
    }
%>
