### ![#f03c15](https://via.placeholder.com/15/f03c15/000000?text=+) NOTE: We develop OneDev at https://code.onedev.io/projects/onedev-server for sake of dogfooding. All issues and pull requests should be created there

<hr/>

# Super Easy All-In-One DevOps Platform 

With Issue Tracking, Git Management, Pull Request, and Build Farm. Simple yet Powerful.

<div style="font-size: 18px;">
| <a href="https://code.onedev.io/projects/onedev-manual/blob/main/pages/quickstart.md">5-minute Quickstart</a> 
| <a href="https://code.onedev.io/projects/onedev-manual/blob/main/pages/installation-guide.md">Installation Guide</a> 
| <a href="https://code.onedev.io/projects/onedev-manual/blob/main/pages/tutorials.md">Tutorials</a>
| <a href="https://code.onedev.io/projects/onedev-manual/blob">All Documentations</a> |
</div>
<p style="margin-bottom: 20px;">

### Docker First Kubernetes Native Build Farm

Trivial to set up a build farm to run massive CI builds as pods in Kubernetes. No agents or runners required. Transparent support for Linux and Windows containers. 

![Job Executor](doc/images/job-executor.png)

### Create Build Spec in a Breeze

Build spec is defined in a yaml file together with your code, and OneDev provides a GUI to create the build spec without any learning curve.

![Cispec](doc/images/cispec.gif)

### Flexible Build Workflow and Pipeline

Conditional build parameters. Create matrix builds by combining different parameters. Connect jobs to create pipelines. Run jobs concurrently to speed up processing. Run jobs automatically upon certain events, or manually by authorized users

![Build Workflow](doc/images/build-workflow.gif)

### Custom Issue States and Fields

Add custom issue states and fields. Configure field dependencies. Transit issue state automatically upon certain events, or manually by authorized users 

![Custom Issue](doc/images/custom-issue.gif)

### Auto-Refreshing Issue Boards

Define issue boards to show issues in columns based on values of specified field. Perform issue operations without leaving the board. Refresh board automatically to reflect most recent issue state.

![Issue Boards](doc/images/issue-boards.gif)

### Code Intelligence

Symbol search and navigation in source code and diff. Outline display and search. Works for every commit. Currently supports Java, JavaScript, C, C++, CSharp, Go, PHP, Python, CSS, SCSS, LESS and R. 

![Symbol Search](doc/images/symbol-search.gif)

### Code Discussions and Comprehensions

Instant and lightweight code discussions on code and diff blocks without creating pull requests. Discussions stay with the code to help code comprehension even if file is changed or renamed. 

![Code Comments](doc/images/code-comments.gif)

### Flexible Pull Request Review Policies

Enforce pull request review with branch protections. Specify required reviewers and builds for desired file patterns. 

![Branch Protection](doc/images/branch-protection.gif)

### Painless Pull Request Review

Review pull request commits as a whole or one by one. Organize code discussions in threads to show changes since last review explicitly

![Increment Review](doc/images/increment-review.gif)

### Powerful Query Language

Perform sophisticated queries against projects, commits, builds, issues, pull requests or code comments with a powerful query language. Save query for quick access. Subscribe to saved query to get notified of interesting events.

![Powerful Query](doc/images/powerful-query.gif)

### Deep Integration of Code, Issue, Pull Request and Build

Configure issues to transit to desired state upon commit, build or pull request events. See commit and pull request events in issue activities. Query fixed issues between builds. Find builds or pull requests fixing particular issues or commits

![Issue Query](doc/images/issue-code-build-integration.gif)

### Fine Grained Permission Control

Define permission schemes best suiting your development workflow. For instance, you may specify who can change particular source folder, who can assign issues, who can run release builds, or who can access build log, etc.

![Role](doc/images/role.png)