<%
    if (htmlVersion) {
        print """
            Dear ${user.displayName},
            <p style='margin: 16px 0;'>
            Please access below url to reset your password:
            <br><br>
			${passwordResetUrl}
        """
    } else {
        print "Dear ${user.displayName},\n\n"
        print "Please access below url to reset your password:\n\n"
        print "${passwordResetUrl}\n\n"
    }
%>
