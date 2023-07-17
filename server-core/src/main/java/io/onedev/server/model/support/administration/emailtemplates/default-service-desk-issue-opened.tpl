<%
    if (htmlVersion)
        print "Issue <a href='${issue.url}'>${issue.FQN}</a> is created. You may reply this email to add more comments"
    else
        print "Issue ${issue.FQN} is created. You may reply this email to add more comments"
%>
