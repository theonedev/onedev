<%
    if (htmlVersion) {
        print """
            Hello,
            <p style='margin: 16px 0;'>
            OneDev account "${user.name}" tries to use email address "${emailAddress}", please visit below link to verify if this is you:
            <br><br>
            <a href='${verificationUrl}'>${verificationUrl}</a>
        """
    } else {
        print "Hello,\n\n"
        print "OneDev account \"${user.name}\" tries to use email address \"${emailAddress}\", please visit below link to verify if this is you:\n\n"
        print verificationUrl
    }
%>
