package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_zh extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>() {

		@Override
		public String put(String key, String value) {
			//return super.put(key, value.replace("docs.onedev.io", "docs.onedev.io/zh-Hans"));
			return super.put(key, value);
		}

	};

	static {
		init(m);
		Translation.watchUpdate(Translation_zh.class, () -> {
			init(m);
		});
	}

	@SystemPrompt("You are good at translating from English to Simplified Chinese in DevOps software area, with below rules:\n" + 
			"'issue' should be translated to '工单' or '问题' depending on context\n" + 
			"'image' should be translated to '图片' or '镜像' depending on context\n" + 
			"'docker aware executor' should be translated to 'docker 相关执行器'\n" + 
			"When used together with 'subscription', word 'active' should be translated to '有效'\n" + 
			"'two factor authentication' should be translated to '两阶段验证'\n" + 
			"Space should be added between English words and Chinese words\n" + 
			"'SSO provider' should be translated as SSO 提供方\n" + 
			"'post build' should be translated as 构建后\n" + 
			"'artifact' should be translated as 制品\n" + 
			"'job' should be translated as 任务")
	public static void init(Map<String, String> m) {
		m.clear();
		m.put(" Project path can be omitted if reference from current project", "如果从当前项目引用，则可以省略项目路径");
		m.put("'..' is not allowed in the directory", "'..' 不允许在目录中使用");
		m.put("(* = any string, ? = any character)", "(* = 任意字符串, ? = 任意字符)");
		m.put("(on behalf of <b>{0}</b>)", "（代表 <b>{0}</b>）");
		m.put("** Enterprise edition is disabled as the subscription was expired. Renew to enable **", 
			"** 企业版已禁用，因为订阅已过期。续订以启用 **");
		m.put("** Enterprise edition is disabled as the trial subscription was expired, order subscription to enable or contact support@onedev.io if you need to extend your trial **", 
			"** 企业版已禁用，因为试用订阅已过期，订购订阅以启用或联系 support@onedev.io 如果您需要延长试用期 **");
		m.put("** Enterprise edition is disabled as there is no remaining user months. Order more to enable **", 
			"** 企业版已禁用，因为没有剩余的用户月数。订购更多以启用 **");
		m.put("1. To use this package, add below to project pom.xml", "1. 要使用此包，请在项目 pom.xml 中添加以下内容");
		m.put("1. Use below repositories in project pom.xml", "1. 在项目 pom.xml 中使用以下仓库");
		m.put("1w 1d 1h 1m", "1w 1d 1h 1m");
		m.put("2. Add below to <code>$HOME/.m2/settings.xml</code> if you want to deploy from command line", 
			"2. 如果要从命令行部署，请将以下内容添加到 <code>$HOME/.m2/settings.xml</code>");
		m.put("2. Also add below to $HOME/.m2/settings.xml if you want to compile project from command line", 
			"2. 如果要从命令行编译项目，请将以下内容添加到 $HOME/.m2/settings.xml");
		m.put("3. For CI/CD job, it is more convenient to use a custom settings.xml, for instance via below code in a command step:", 
			"3. 对于 CI/CD 任务，使用自定义 settings.xml 更方便，例如在命令步骤中使用以下代码");
		m.put("6-digits passcode", "6 位数代码");
		m.put("7 days", "7 天");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for", 
			"重置密码的 <a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">用户</a>");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for", 
			"验证邮箱的 <a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">用户</a>");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"可使用 <a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub 风格的 markdown</a>，并支持 <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid 和 katex</a>。");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification", 
			"触发通知的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>事件对象</a>");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display", 
			"显示的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>告警</a>");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a> overdue", 
			"过期的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a>");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> 于 <span wicket:id=\"date\"></span> 提交");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed with <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> 于 <span wicket:id=\"date\"></span> 由 <a wicket:id=\"committer\" class=\"name link-gray\"></a> 提交");
		m.put("<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depends on me", "<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> 依赖于我");
		m.put("<a wicket:id=\"removePassword\">Remove password</a> to force the user to authenticate via external system", 
			"<a wicket:id=\"removePassword\">移除密码</a> 以强制用户通过外部系统进行身份验证");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">通过恢复代码验证</a>（如果您无法访问 TOTP 认证器）");
		m.put("<b class='text-danger'>NOTE: </b> This requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>注意：</b> 这需要企业订阅。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a> 30 天");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>注意：</b> 此步骤需要企业版订阅。<a href='https://onedev.io/pricing' target='_blank'>免费试用 30 天</a>");
		m.put("<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>注意：</b>SendGrid 集成是企业功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a> 30 天");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>注意：</b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>时间跟踪</a>是企业版功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a>30天");
		m.put("<b>NOTE: </b> Service desk only takes effect if <a wicket:id=\"mailConnector\">mail service</a> is defined and its <tt>check incoming email</tt> option is enabled. Also <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>sub addressing</a> needs to be enabled for the system email address. Check <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>this tutorial</a> for details", 
			"<b>注意：</b> 服务台仅在 <a wicket:id=\"mailConnector\">邮件服务</a> 定义且 <tt>检查收件邮箱</tt> 选项启用时生效。此外，系统邮箱地址需要启用 <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>子地址</a>。请查看 <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>此教程</a> 获取详细信息");
		m.put("<b>NOTE:</b> Batch editing issues will not cause state transitions of other issues even if transition rule matches", 
			"<b>注意：</b> 批量编辑工单不会导致其他工单的状态转换，即使匹配转换规则");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>项目所有者</b>是一个内置角色，拥有项目的所有权限");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>提示: </b> 输入 <tt>@</tt> <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>插入变量</a>。使用 <tt>@@</tt> 插入字符 <tt>@</tt>");
		m.put("<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>搜索文件</span> <span class='font-size-sm text-muted'>在默认分支中</span></div>");
		m.put("<div><span>Search Symbols</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>搜索符号</span> <span class='font-size-sm text-muted'>在默认分支中</span></div>");
		m.put("<div><span>Search Text</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>搜索文本</span> <span class='font-size-sm text-muted'>在默认分支中</span></div>");
		m.put("<i>No Name</i>", "<i>无名称</i>");
		m.put("<span class=\"keycap ml-3 mr-1\">esc</span> to close", "<span class=\"keycap ml-3 mr-1\">esc</span> 关闭");
		m.put("<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> to move", 
			"<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> 移动");
		m.put("<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> to navigate. <span class=\"keycap\">Esc</span> to close", 
			"按 <span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> 导航。按 <span class=\"keycap\">Esc</span> 关闭");
		m.put("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete.", 
			"按 <span class='keycap'>Tab</span> 或 <span class='keycap'>Enter</span> 完成。");
		m.put("<span class='keycap'>Tab</span> to complete.", "按 <span class='keycap'>Tab</span> 完成。");
		m.put("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>", "<span class='text-nowrap'><span class='keycap'>Enter</span> 进入</span>");
		m.put("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>", "<span class='text-nowrap'><span class='keycap'>Tab</span> 搜索</span>");
		m.put("<span wicket:id=\"activityCount\"></span> activities", "<span wicket:id=\"activityCount\"></span> 个活动");
		m.put("<svg class='icon mr-2'><use xlink:href='%s'/></svg> Define job secrets to be used in build spec. Secrets with <b>same name</b> can be defined. For a particular name, the first authorized secret with that name will be used (search in current project first, then search in parent projects). Note that secret value containing line breaks or less than <b>%d</b> characters will not be masked in build log", 
			"<svg class='icon mr-2'><use xlink:href='%s'/></svg> 定义要在构建规范中使用的任务密钥。可以定义具有<b>相同名称</b>的密钥。对于特定名称，将使用具有该名称的第一个授权密钥（首先在当前项目中搜索，然后在父项目中搜索）。注意，包含换行符或少于<b>%d</b>个字符的密钥值在构建日志中不会被屏蔽");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here", 
			"这里需要一个 <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java 正则表达式</a>");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"用于验证提交信息页脚的<a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java正则表达式</a>");
		m.put("A child project with name \"{0}\" already exists under \"{1}\"", "在 \"{1}\" 下已存在名为 \"{0}\" 的子项目");
		m.put("A file exists where you’re trying to create a subdirectory. Choose a new path and try again..", 
			"您尝试创建子目录的地方已存在一个文件。请选择一个新路径并重试。");
		m.put("A path with same name already exists.Please choose a different name and try again.", 
			"具有相同名称的路径已存在。请选择一个不同的名称并重试。");
		m.put("A pull request is open for this change", "一个合并请求正在审查此更改");
		m.put("A root project with name \"{0}\" already exists", "已存在名为 \"{0}\" 的根项目");
		m.put("A {0} used as body of address verification email", "用作邮箱验证邮件正文的 {0}");
		m.put("A {0} used as body of build notification email", "用作构建通知邮件正文的 {0}");
		m.put("A {0} used as body of commit notification email", "用作提交通知邮件正文的 {0}");
		m.put("A {0} used as body of feedback email when failed to open issue via service desk", "用作通过服务台创建工单失败时反馈邮件正文的 {0}");
		m.put("A {0} used as body of feedback email when issue is opened via service desk", "用作通过服务台创建工单时反馈邮件正文的 {0}");
		m.put("A {0} used as body of feedback email when unsubscribed from issue notification", "用作取消订阅工单通知时反馈邮件正文的 {0}");
		m.put("A {0} used as body of feedback email when unsubscribed from pull request notification", 
			"用作取消订阅合并请求通知时反馈邮件正文的 {0}");
		m.put("A {0} used as body of issue stopwatch overdue notification email", "用作工单计时器超时通知邮件正文的 {0}");
		m.put("A {0} used as body of package notification email", "用作包通知邮件正文的 {0}");
		m.put("A {0} used as body of password reset email", "用作密码重置邮件正文的 {0}");
		m.put("A {0} used as body of system alert email", "用作系统告警邮件正文的 {0}");
		m.put("A {0} used as body of user invitation email", "用作用户邀请邮件正文的 {0}");
		m.put("A {0} used as body of various issue notification emails", "用作各种工单通知邮件正文的 {0}");
		m.put("A {0} used as body of various pull request notification emails", "用作各种合并请求通知邮件正文的 {0}");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"你的JIRA云实例的API地址，例如：<tt>https://your-domain.atlassian.net/rest/api/3</tt>");
		m.put("Able to merge without conflicts", "没有冲突，可以合并");
		m.put("Absolute or relative url of the image", "图片的绝对或相对 URL");
		m.put("Absolute or relative url of the link", "链接的绝对或相对 URL");
		m.put("Access Anonymously", "匿名访问");
		m.put("Access Build Log", "访问构建日志");
		m.put("Access Build Pipeline", "访问构建流水线");
		m.put("Access Build Reports", "访问构建报告");
		m.put("Access Confidential Issues", "访问机密工单");
		m.put("Access Time Tracking", "访问时间追踪");
		m.put("Access Token", "访问令牌");
		m.put("Access Token Authorization Bean", "访问令牌授权Bean");
		m.put("Access Token Edit Bean", "访问令牌编辑Bean");
		m.put("Access Token Secret", "访问令牌密钥");
		m.put("Access Token for Target Project", "目标项目的访问令牌");
		m.put("Access Tokens", "访问令牌");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"访问令牌用于 API 访问和仓库拉取/推送。它不能用于登录 Web 界面");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"访问令牌用于 API 访问或仓库拉取/推送。它不能用于登录 Web 界面");
		m.put("Access token regenerated successfully", "访问令牌重新生成成功");
		m.put("Access token regenerated, make sure to update the token at agent side", "访问令牌已重新生成，请确保在代理端更新令牌");
		m.put("Account Email", "账户邮箱");
		m.put("Account Name", "账户名称");
		m.put("Account is disabled", "账户已被禁用");
		m.put("Account set up successfully", "账户设置成功");
		m.put("Active Directory", "Active Directory");
		m.put("Active Since", "活跃起");
		m.put("Activities", "活动");
		m.put("Activity by type", "按类型统计的活动");
		m.put("Add", "添加");
		m.put("Add Executor", "新建任务执行器");
		m.put("Add GPG key", "添加 GPG 密钥");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "在此添加 GPG 密钥以验证此用户的代码提交/标签签名");
		m.put("Add GPG keys here to verify commits/tags signed by you", "在此添加 GPG 密钥以验证由您签名的提交/标签");
		m.put("Add GPG public keys to be trusted here. Commits signed with trusted keys will be shown as verified.", 
			"添加 GPG 公钥到受信任的公钥列表中。受信任的公钥签名的提交将显示为已验证。");
		m.put("Add Issue...", "添加工单...");
		m.put("Add Issues to Iteration", "将工单添加到迭代");
		m.put("Add New", "添加");
		m.put("Add New Board", "新建看板");
		m.put("Add New Email Address", "添加新电子邮件地址");
		m.put("Add New Timesheet", "新建时间表");
		m.put("Add Rule", "添加规则");
		m.put("Add SSH key", "添加 SSH 密钥");
		m.put("Add SSO provider", "添加 SSO 提供者");
		m.put("Add Spent Time", "添加已用时间");
		m.put("Add Timesheet", "新建时间表");
		m.put("Add Widget", "添加小组件");
		m.put("Add a GPG Public Key", "添加 GPG 公钥");
		m.put("Add a SSH Key", "添加 SSH 密钥");
		m.put("Add a package source like below", "添加一个包源，如下所示");
		m.put("Add after", "添加到后面");
		m.put("Add agent", "添加代理");
		m.put("Add all cards to specified iteration", "将所有卡片添加到指定迭代");
		m.put("Add all commits from source branch to target branch with a merge commit", "将源分支的所有提交添加到目标分支，并创建合并提交");
		m.put("Add assignee...", "添加分配人员...");
		m.put("Add before", "添加到前面");
		m.put("Add below to allow accessing via http protocol in new Maven versions", "添加以下内容以在新 Maven 版本中允许通过 http 协议访问");
		m.put("Add child project", "添加子项目");
		m.put("Add comment", "添加评论");
		m.put("Add comment on this selection", "在此选择上添加评论");
		m.put("Add custom field", "添加自定义字段");
		m.put("Add dashboard", "添加仪表板");
		m.put("Add default issue board", "添加默认工单看板");
		m.put("Add files to current directory", "添加文件到当前目录");
		m.put("Add files via upload", "通过上传添加文件");
		m.put("Add groovy script", "添加 Groovy 脚本");
		m.put("Add issue description template", "添加工单描述模板");
		m.put("Add issue link", "添加工单链接");
		m.put("Add issue state", "添加工单状态");
		m.put("Add issue state transition", "添加工单状态转换");
		m.put("Add link", "添加链接");
		m.put("Add new", "添加");
		m.put("Add new card to this column", "在此列添加新卡片");
		m.put("Add new file", "添加新文件");
		m.put("Add new import", "添加新导入");
		m.put("Add new issue creation setting", "添加新工单创建设置");
		m.put("Add new job dependency", "添加新任务依赖");
		m.put("Add new param", "添加新参数");
		m.put("Add new post-build action", "添加新构建后操作");
		m.put("Add new project dependency", "添加新项目依赖");
		m.put("Add new step", "添加新步骤");
		m.put("Add new trigger", "添加新触发器");
		m.put("Add project", "添加项目");
		m.put("Add reviewer...", "添加审查人员...");
		m.put("Add to batch to commit with other suggestions later", "稍后与其他建议一起提交");
		m.put("Add to group...", "添加到组...");
		m.put("Add to iteration...", "添加到迭代...");
		m.put("Add user to group...", "将用户添加到组...");
		m.put("Add value", "添加值");
		m.put("Add {0}", "添加 {0}");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "代码提交 \"{0}\"（<i class='text-danger'>仓库中不存在</i>）");
		m.put("Added commit \"{0}\" ({1})", "代码提交 \"{0}\"（{1}）");
		m.put("Added to group", "已添加到组");
		m.put("Additions", "新增");
		m.put("Administration", "管理");
		m.put("Administrative permission over a project", "项目的管理权限");
		m.put("Advanced Search", "高级搜索");
		m.put("After modification", "修改后");
		m.put("Agent", "代理");
		m.put("Agent Attribute", "代理属性");
		m.put("Agent Count", "代理数量");
		m.put("Agent Edit Bean", "代理编辑Bean");
		m.put("Agent Selector", "代理选择器");
		m.put("Agent is designed to be maintenance free. Once connected to server, it will be updated automatically upon server upgrade", 
			"代理被设计为维护免费。一旦连接到服务器，它将在服务器升级时自动更新");
		m.put("Agent removed", "代理已删除");
		m.put("Agent tokens are used to authorize agents. It should be configured via environment variable <tt>agentToken</tt> if agent runs as docker container, or property <tt>agentToken</tt> in file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> if agent runs on bare metal/virtual machine. A token will be in-use and removed from this list if agent using it connects to server", 
			"代理令牌用于授权代理。如果代理作为容器运行，则应通过环境变量 <tt>agentToken</tt> 配置；如果代理在裸机/虚拟机上运行，则应在文件 <tt>&lt;agent dir&gt;/conf/agent.properties</tt> 中配置属性 <tt>agentToken</tt>。当代理使用此令牌连接到服务器时，令牌将处于使用状态并从列表中删除");
		m.put("Agents", "任务代理");
		m.put("Agents can be used to execute jobs on remote machines. Once started it will update itself from server automatically when necessary", 
			"代理可以用于在远程机器上执行任务。一旦启动，它在需要时将自动从服务器更新自身");
		m.put("Aggregated from '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':", "聚合自 '<span wicket:id=\"estimatedTimeAggregationLink\"></span>'");
		m.put("Aggregated from '<span wicket:id=\"spentTimeAggregationLink\"></span>':", "聚合自 '<span wicket:id=\"spentTimeAggregationLink\"></span>'");
		m.put("Aggregation Link", "聚合链接");
		m.put("Alert", "警报");
		m.put("Alert Setting", "警报设置");
		m.put("Alert Settings", "告警设置");
		m.put("Alert settings have been updated", "告警设置已更新");
		m.put("Alerts", "提醒");
		m.put("All", "全部");
		m.put("All Issues", "所有工单");
		m.put("All RESTful Resources", "所有RESTful资源");
		m.put("All accessible", "所有可访问的");
		m.put("All builds", "所有构建");
		m.put("All changes", "所有更改");
		m.put("All except", "除外所有");
		m.put("All files", "所有文件");
		m.put("All groups", "所有分组");
		m.put("All issues", "所有工单");
		m.put("All platforms in OCI layout", "所有 OCI 布局的平台");
		m.put("All platforms in image", "所有镜像中的平台");
		m.put("All possible classes", "所有可能的类");
		m.put("All projects", "所有项目");
		m.put("All projects with code read permission", "所有具有代码读取权限的项目");
		m.put("All pull requests", "所有合并请求");
		m.put("All users", "所有用户");
		m.put("Allow Empty", "允许为空");
		m.put("Allow Empty Value", "允许空值");
		m.put("Allow Multiple", "允许多个");
		m.put("Allowed Licenses", "允许的许可证");
		m.put("Allowed Self Sign-Up Email Domain", "允许自助注册的邮箱域名");
		m.put("Always", "总是");
		m.put("Always Pull Image", "始终拉取镜像");
		m.put("An issue already linked for {0}. Unlink it first", "已链接到 {0}。先取消链接");
		m.put("An unexpected exception occurred", "发生意外异常");
		m.put("And configure auth token of the registry", "并配置注册表的授权令牌");
		m.put("Another pull request already open for this change", "另一个合并请求已为此更改打开");
		m.put("Any agent", "任何代理");
		m.put("Any branch", "任何分支");
		m.put("Any commit message", "任何提交信息");
		m.put("Any domain", "任何域");
		m.put("Any file", "任何文件");
		m.put("Any issue", "任何工单");
		m.put("Any job", "任何任务");
		m.put("Any project", "任何项目");
		m.put("Any ref", "任何引用");
		m.put("Any sender", "任何发送者");
		m.put("Any state", "任何状态");
		m.put("Any tag", "任何标签");
		m.put("Any user", "任何用户");
		m.put("Api Key", "API密钥");
		m.put("Api Token", "API令牌");
		m.put("Api Url", "API地址");
		m.put("Append", "追加");
		m.put("Applicable Branches", "适用分支");
		m.put("Applicable Builds", "适用构建");
		m.put("Applicable Code Comments", "适用代码评论");
		m.put("Applicable Commit Messages", "适用的提交信息");
		m.put("Applicable Commits", "适用提交");
		m.put("Applicable Images", "适用镜像");
		m.put("Applicable Issues", "适用工单");
		m.put("Applicable Jobs", "适用任务");
		m.put("Applicable Names", "适用名称");
		m.put("Applicable Projects", "适用项目");
		m.put("Applicable Pull Requests", "适用合并请求");
		m.put("Applicable Senders", "适用发送者");
		m.put("Applicable Users", "适用用户");
		m.put("Application (client) ID", "应用（客户端）ID");
		m.put("Apply suggested change from code comment", "从代码评论应用建议的更改");
		m.put("Apply suggested changes from code comments", "从代码评论应用建议的更改");
		m.put("Approve", "批准");
		m.put("Approved", "已批准");
		m.put("Approved pull request \"{0}\" ({1})", "批准合并请求 \"{0}\"（{1}）");
		m.put("Arbitrary scope", "任意范围");
		m.put("Arbitrary type", "任意类型");
		m.put("Arch Pull Command", "Arch 拉取命令");
		m.put("Archived", "已归档");
		m.put("Arguments", "参数");
		m.put("Artifacts", "制品");
		m.put("Artifacts to Retrieve", "要获取的制品");
		m.put("As long as a feature can be accessed via url, you can input part of the url to match and jump", 
			"只要功能可以通过 url 访问，您可以输入部分 url 进行匹配和跳转");
		m.put("Ascending", "升序");
		m.put("Assignees", "分配给");
		m.put("Assignees Issue Field", "受让人工单字段");
		m.put("Assignees are expected to merge the pull request", "合并请求的负责人应合并合并请求");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"分配人员具有代码写入权限，并负责合并合并请求");
		m.put("Asymmetric", "非对称");
		m.put("At least one branch or tag should be selected", "至少选择一个分支或标签");
		m.put("At least one choice need to be specified", "至少需要指定一个选项");
		m.put("At least one email address should be configured, please add a new one first", "至少应配置一个电子邮件地址，请先添加一个新地址");
		m.put("At least one email address should be specified", "至少需要指定一个电子邮件地址");
		m.put("At least one entry should be specified", "至少需要指定一个条目");
		m.put("At least one event type needs to be selected", "至少需要选择一个事件类型");
		m.put("At least one field needs to be specified", "至少需要指定一个字段");
		m.put("At least one project should be authorized", "至少需要授权一个项目");
		m.put("At least one project should be selected", "至少需要选择一个项目");
		m.put("At least one repository should be selected", "至少需要选择一个仓库");
		m.put("At least one role is required", "至少需要一个角色");
		m.put("At least one role must be selected", "至少需要选择一个角色");
		m.put("At least one state should be specified", "至少需要指定一个状态");
		m.put("At least one tab should be added", "至少需要添加一个标签");
		m.put("At least one user search base should be specified", "至少需要指定一个用户搜索根节点");
		m.put("At least one value needs to be specified", "至少需要指定一个值");
		m.put("At least two columns need to be defined", "至少需要定义两个列");
		m.put("Attachment", "附件");
		m.put("Attributes", "属性");
		m.put("Attributes (can only be edited when agent is online)", "属性（仅当代理在线时可编辑）");
		m.put("Attributes saved", "属性已保存");
		m.put("Audit", "审计");
		m.put("Audit Log", "审计日志");
		m.put("Audit Setting", "审计设置");
		m.put("Audit log will be preserved for the specified number of days. This setting applies to all audit events, including system level and project level", 
			"审计日志将保留指定的天数。此设置适用于所有审计事件，包括系统级别和项目级别");
		m.put("Auth Source", "认证源");
		m.put("Authenticate to Bitbucket Cloud", "Bitbucket Cloud 认证");
		m.put("Authenticate to GitHub", "GitHub 认证");
		m.put("Authenticate to GitLab", "GitLab 认证");
		m.put("Authenticate to Gitea", "Gitea 认证");
		m.put("Authenticate to JIRA cloud", "JIRA cloud 认证");
		m.put("Authenticate to YouTrack", "YouTrack 认证");
		m.put("Authentication", "身份验证");
		m.put("Authentication Required", "需要身份验证");
		m.put("Authentication Test", "认证测试");
		m.put("Authentication Token", "身份验证令牌");
		m.put("Authenticator", "认证器");
		m.put("Authenticator Bean", "身份验证器Bean");
		m.put("Author", "作者");
		m.put("Author date", "作者日期");
		m.put("Authored By", "作者");
		m.put("Authorization", "授权");
		m.put("Authorizations", "授权");
		m.put("Authorize user...", "授权用户...");
		m.put("Authorized Projects", "已授权项目");
		m.put("Authorized Roles", "已授权角色");
		m.put("Auto Merge", "自动合并");
		m.put("Auto Spec", "自动规范");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"自动更新检查通过从 onedev.io 请求一个图像来完成，该图像会指示新版本的可用性，颜色表示更新的严重程度。其工作方式与 gravatar 请求头像图片相同。如果禁用，强烈建议你定期手动检查更新（可通过屏幕左下角的帮助菜单执行），以确保获得安全/关键修复");
		m.put("Auto-discovered executor", "自动发现的执行器");
		m.put("Available Agent Tokens", "可用的代理令牌");
		m.put("Available Choices", "可选项");
		m.put("Avatar", "头像");
		m.put("Avatar Service Url", "头像服务地址");
		m.put("Avatar and name", "头像和名称");
		m.put("Back To Home", "返回主页");
		m.put("Backlog", "待办");
		m.put("Backlog Base Query", "待办工单基础查询");
		m.put("Backup", "备份");
		m.put("Backup Now", "立即备份");
		m.put("Backup Schedule", "备份计划");
		m.put("Backup Setting", "备份设置");
		m.put("Backup Setting Holder", "备份设置持有者");
		m.put("Backup settings updated", "备份设置已更新");
		m.put("Bare Metal", "裸机");
		m.put("Base", "基准");
		m.put("Base Gpg Key", "基础GPG密钥");
		m.put("Base Query", "基础查询");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Base64 编码的 PEM 格式，以 -----BEGIN CERTIFICATE----- 开头，以 -----END CERTIFICATE----- 结尾");
		m.put("Basic Info", "基本信息");
		m.put("Basic Settings", "基本设置");
		m.put("Basic settings updated", "基本设置已更新");
		m.put("Batch Edit All Queried Issues", "批量编辑所有查询的工单");
		m.put("Batch Edit Selected Issues", "批量编辑选定的工单");
		m.put("Batch Editing {0} Issues", "批量编辑 {0} 个工单");
		m.put("Batched suggestions", "批量建议");
		m.put("Before modification", "修改前");
		m.put("Belonging Groups", "所属组");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"以下是一些常见的条件。在搜索框中输入以查看完整列表和可用组合。");
		m.put("Below content is restored from an unsaved change. Clear to discard", "以下内容从未保存的更改中恢复。清除以丢弃");
		m.put("Below information will also be sent", "以下信息也将被发送");
		m.put("Binary file.", "二进制文件");
		m.put("Bitbucket App Password", "Bitbucket应用密码");
		m.put("Bitbucket Login Name", "Bitbucket登录名");
		m.put("Bitbucket Repositories to Import", "要导入的Bitbucket仓库");
		m.put("Bitbucket Workspace", "Bitbucket工作区");
		m.put("Bitbucket app password should be generated with permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>", 
			"Bitbucket应用密码应使用权限<b>account/read</b>、<b>repositories/read</b>和<b>issues:read</b>生成");
		m.put("Blame", "更改记录");
		m.put("Blob", "Blob");
		m.put("Blob hash", "Blob哈希");
		m.put("Blob index version", "Blob索引版本");
		m.put("Blob name", "Blob名称");
		m.put("Blob path", "Blob路径");
		m.put("Blob primary symbols", "Blob主符号");
		m.put("Blob secondary symbols", "Blob次符号");
		m.put("Blob symbol list", "Blob符号列表");
		m.put("Blob text", "Blob文本");
		m.put("Blob unknown", "未知Blob");
		m.put("Blob upload invalid", "Blob上传无效");
		m.put("Blob upload unknown", "Blob上传未知错误");
		m.put("Board", "看板");
		m.put("Board Columns", "看板列");
		m.put("Board Spec", "看板规范");
		m.put("Boards", "看板");
		m.put("Body", "正文");
		m.put("Bold", "加粗");
		m.put("Both", "两者");
		m.put("Bottom", "底部");
		m.put("Branch", "分支");
		m.put("Branch \"{0}\" already exists, please choose a different name", "分支 \"{0}\" 已存在，请选择不同的名称");
		m.put("Branch \"{0}\" created", "分支 \"{0}\" 已创建");
		m.put("Branch \"{0}\" deleted", "分支 \"{0}\" 已删除");
		m.put("Branch <a wicket:id=\"targetBranch\"></a> is up to date with all commits from <a wicket:id=\"sourceBranch\"></a>. Try <a wicket:id=\"swapBranches\">swap source and target</a> for the comparison.", 
			"分支 <a wicket:id=\"targetBranch\"></a> 与 <a wicket:id=\"sourceBranch\"></a> 的所有提交一致。尝试 <a wicket:id=\"swapBranches\">交换源和目标</a> 进行比较。");
		m.put("Branch Choice Bean", "分支选择Bean");
		m.put("Branch Name", "分支名称");
		m.put("Branch Protection", "分支保护");
		m.put("Branch Revision", "分支修订");
		m.put("Branch update", "分支更新");
		m.put("Branches", "分支");
		m.put("Brand Setting Edit Bean", "品牌设置编辑Bean");
		m.put("Branding", "品牌设置");
		m.put("Branding settings updated", "品牌设置已更新");
		m.put("Browse Code", "浏览代码");
		m.put("Browse code", "浏览代码");
		m.put("Bug Report", "错误报告");
		m.put("Build", "构建");
		m.put("Build #{0} already finished", "构建 #{0} 已完成");
		m.put("Build #{0} deleted", "构建 #{0} 已删除");
		m.put("Build #{0} not finished yet", "构建 #{0} 未完成");
		m.put("Build Artifact Storage", "构建制品存储");
		m.put("Build Commit", "构建提交");
		m.put("Build Context", "构建上下文");
		m.put("Build Description", "构建描述");
		m.put("Build Filter", "构建筛选器");
		m.put("Build Image", "构建镜像");
		m.put("Build Image (Kaniko)", "构建镜像（Kaniko）");
		m.put("Build Management", "构建管理");
		m.put("Build Notification", "构建通知");
		m.put("Build Notification Template", "构建通知模板");
		m.put("Build Number", "构建编号");
		m.put("Build On Behalf Of", "构建发起");
		m.put("Build Path", "构建路径");
		m.put("Build Preservation", "构建保留策略");
		m.put("Build Preservations", "构建保留策略");
		m.put("Build Preservations Bean", "构建保留策略Bean");
		m.put("Build Preserve Rules", "构建保留规则");
		m.put("Build Provider", "构建提供者");
		m.put("Build Spec", "构建规范");
		m.put("Build Statistics", "构建统计");
		m.put("Build Version", "构建版本");
		m.put("Build Volume Storage Class", "构建卷存储类");
		m.put("Build Volume Storage Size", "构建卷存储大小");
		m.put("Build administrative permission for all jobs inside a project, including batch operations over multiple builds", 
			"项目中所有任务的构建管理权限，包括多个构建的批量操作");
		m.put("Build docker image with docker buildx. This step can only be executed by server docker executor or remote docker executor, and it uses the buildx builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko step instead", 
			"使用docker buildx构建docker镜像。此步骤只能由服务器docker执行器或远程docker执行器执行，并使用这些执行器中指定的buildx构建器进行构建。如需使用Kubernetes执行器构建镜像，请改用kaniko步骤");
		m.put("Build docker image with kaniko. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"使用kaniko构建docker镜像。此步骤需由服务器docker执行器、远程docker执行器或Kubernetes执行器执行");
		m.put("Build duration statistics", "构建时长统计");
		m.put("Build frequency statistics", "构建频率统计");
		m.put("Build is successful", "构建成功");
		m.put("Build list", "构建列表");
		m.put("Build not exist or access denied", "构建不存在或访问被拒绝");
		m.put("Build number", "构建编号");
		m.put("Build preserve rules saved", "构建保留规则已保存");
		m.put("Build required for deletion. Submit pull request instead", "需要构建才能删除。请提交合并请求");
		m.put("Build required for this change. Please submit pull request instead", "此更改需要构建。请提交拉取请求。");
		m.put("Build required for this change. Submit pull request instead", "需要构建才能更改。请提交合并请求");
		m.put("Build spec not defined", "构建规范未定义");
		m.put("Build spec not defined (import project: {0}, import revision: {1})", "构建规范未定义（导入项目：{0}，导入版本：{1}）");
		m.put("Build spec not found in commit of this build", "构建规范未在构建提交中找到");
		m.put("Build statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"构建统计是企业版功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a> 30 天");
		m.put("Build version", "构建版本");
		m.put("Build with Persistent Volume", "使用持久卷构建");
		m.put("Builds", "构建");
		m.put("Builds are {0}", "构建是 {0}");
		m.put("Buildx Builder", "Buildx构建器");
		m.put("Built In Fields Bean", "内建字段Bean");
		m.put("Burndown", "燃尽图");
		m.put("Burndown chart", "燃尽图");
		m.put("Button Image Url", "按钮图像地址");
		m.put("By Group", "按组");
		m.put("By User", "按用户");
		m.put("By day", "按日");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"默认情况下，代码通过自动生成的凭证进行克隆，该凭证仅对当前项目具有读取权限。如需让任务<a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>推送代码到服务器</a>，请在此提供具有相应权限的自定义凭证");
		m.put("By default issues of parent and child projects will also be listed. Use query <code>&quot;Project&quot; is current</code> to show issues only belong to this project", 
			"默认情况下，父项目和子项目的工单也会被列出。使用查询 <code>&quot;Project&quot; is current</code> 来只显示属于此项目的工单");
		m.put("By month", "按月");
		m.put("By week", "按周");
		m.put("Bypass Certificate Check", "绕过证书检查");
		m.put("CANCELLED", "已取消");
		m.put("CORS Allowed Origins", "CORS 允许的来源");
		m.put("CPD Report", "CPD 报告");
		m.put("CPU", "CPU");
		m.put("CPU Intensive Task Concurrency", "CPU 密集型任务并发数");
		m.put("CPU capability in millis. This is normally (CPU cores)*1000", "CPU 能力（毫秒）。通常为 (CPU 核心数)*1000");
		m.put("Cache Key", "缓存键");
		m.put("Cache Management", "缓存管理");
		m.put("Cache Paths", "缓存路径");
		m.put("Cache Setting Bean", "缓存设置 Bean");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "缓存在未访问达到指定天数后将被删除以节省空间");
		m.put("Calculating merge preview...", "计算合并预览...");
		m.put("Callback URL", "回调 URL");
		m.put("Can Be Used By Jobs", "可被任务使用");
		m.put("Can Create Root Projects", "可以创建根项目");
		m.put("Can Edit Estimated Time", "可以编辑预估时间");
		m.put("Can not convert root user to service account", "无法将 root 用户转换为服务账户");
		m.put("Can not convert yourself to service account", "无法将自己转换为服务账户");
		m.put("Can not delete default branch", "不能删除默认分支");
		m.put("Can not delete root account", "不能删除根账户");
		m.put("Can not delete yourself", "不能删除自己");
		m.put("Can not disable root account", "不能禁用根账户");
		m.put("Can not disable yourself", "不能禁用自己");
		m.put("Can not find issue board: ", "找不到工单看板：");
		m.put("Can not move project \"{0}\" to be under itself or its descendants", "不能将项目 \"{0}\" 移动到其自身或其子项目下");
		m.put("Can not perform this operation now", "当前无法执行此操作");
		m.put("Can not reset password for service account or disabled user", "无法重置服务账户或禁用用户的密码");
		m.put("Can not reset password for user authenticating via external system", "无法重置通过外部系统认证的用户密码");
		m.put("Can not save malformed query", "无法保存格式错误的查询");
		m.put("Can not use current or descendant project as parent", "不能使用当前或子项目作为父项目");
		m.put("Can only compare with common ancestor when different projects are involved", "跨项目比较时只能使用共同祖先");
		m.put("Cancel", "取消");
		m.put("Cancel All Queried Builds", "取消所有查询的构建");
		m.put("Cancel Selected Builds", "取消选定的构建");
		m.put("Cancel invitation", "取消邀请");
		m.put("Cancel request submitted", "取消请求已提交");
		m.put("Cancel this build", "取消此构建");
		m.put("Cancelled", "已取消");
		m.put("Cancelled By", "已取消");
		m.put("Case Sensitive", "区分大小写");
		m.put("Certificates to Trust", "信任的证书");
		m.put("Change", "更改");
		m.put("Change Detection Excludes", "变更检测排除项");
		m.put("Change My Password", "更改我的密码");
		m.put("Change To", "更改到");
		m.put("Change already merged", "更改已合并");
		m.put("Change not updated yet", "更改尚未更新");
		m.put("Change property <code>serverUrl</code> in file <code>conf/agent.properties</code> if necessary. The default value is taken from OneDev server url specified in <i>Administration / System Setting</i>", 
			"如果需要，请更改文件 <code>conf/agent.properties</code> 中的属性 <code>serverUrl</code>。默认值取自 <i>管理 / 系统设置</i> 中指定的 OneDev 服务器 URL");
		m.put("Change to another field", "更改为其他字段");
		m.put("Change to another state", "更改为其他状态");
		m.put("Change to another value", "更改为其他值");
		m.put("Changes since last review", "上次审查后的更改");
		m.put("Changes since last visit", "自上次访问以来的更改");
		m.put("Changes since this action", "此操作后的变更");
		m.put("Changes since this comment", "此评论后的变更");
		m.put("Channel Notification", "频道通知");
		m.put("Chart Metadata", "Chart 元数据");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"检查 <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub 的指南</a> 如何生成和使用 GPG 密钥来签署您的提交");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including instructions on how to run agent as service", 
			"查看 <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">代理管理</a> 了解更多详细信息，包括如何作为服务运行代理的说明");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including list of supported environment variables", 
			"查看 <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">代理管理</a> 了解更多详细信息，包括支持的环境变量列表");
		m.put("Check Commit Message Footer", "检查提交消息尾部");
		m.put("Check Incoming Email", "检查接收的电子邮件");
		m.put("Check Issue Integrity", "检查工单完整性");
		m.put("Check Update", "检查更新");
		m.put("Check Workflow Integrity", "检查工作流完整性");
		m.put("Check out to local workspace", "Checkout 到本地");
		m.put("Check this to compare right side with common ancestor of left and right", "勾选此项以将右侧与左右两侧的共同祖先进行比较");
		m.put("Check this to enforce two-factor authentication for all users in the system", "勾选此项以强制系统中所有用户启用两阶段身份验证");
		m.put("Check this to enforce two-factor authentication for all users in this group", "勾选此项以强制本组所有用户启用两阶段身份验证");
		m.put("Check this to prevent branch creation", "勾选此项以禁止创建分支");
		m.put("Check this to prevent branch deletion", "勾选此项以禁止删除分支");
		m.put("Check this to prevent forced push", "勾选此项以禁止强制推送");
		m.put("Check this to prevent tag creation", "勾选此项以禁止创建标签");
		m.put("Check this to prevent tag deletion", "勾选此项以禁止删除标签");
		m.put("Check this to prevent tag update", "勾选此项以禁止更新标签");
		m.put("Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>. Note this is applicable for non-merge commits", 
			"勾选此项以要求使用<a href='https://www.conventionalcommits.org' target='_blank'>规范化提交</a>。注意，该设置仅适用于非合并提交");
		m.put("Check this to require valid signature of head commit", "勾选此项以要求主提交具有有效签名");
		m.put("Check this to retrieve Git LFS files", "勾选此项以获取 Git LFS 文件");
		m.put("Checkbox", "复选框");
		m.put("Checking field values...", "检查字段值...");
		m.put("Checking fields...", "检查字段...");
		m.put("Checking state and field ordinals...", "检查状态和字段排序...");
		m.put("Checking state...", "检查状态...");
		m.put("Checkout Code", "签出代码");
		m.put("Checkout Path", "签出路径");
		m.put("Checkout Pull Request Head", "检出合并请求的头部提交");
		m.put("Checkout Pull Request Merge Preview", "检出合并请求的预览提交");
		m.put("Checkstyle Report", "Checkstyle 报告");
		m.put("Cherry-Pick", "cherry-pick");
		m.put("Cherry-picked successfully", "cherry-pick 成功");
		m.put("Child Projects", "子项目");
		m.put("Child Projects Of", "子项目");
		m.put("Choice Provider", "选项提供器");
		m.put("Choose", "选择");
		m.put("Choose JIRA project to import issues from", "选择要导入工单的 JIRA 项目");
		m.put("Choose Revision", "选择版本");
		m.put("Choose YouTrack project to import issues from", "选择要导入工单的 YouTrack 项目");
		m.put("Choose a project...", "选择项目...");
		m.put("Choose a user...", "选择用户...");
		m.put("Choose branch...", "选择分支...");
		m.put("Choose branches...", "选择分支...");
		m.put("Choose build...", "选择构建...");
		m.put("Choose file", "选择文件");
		m.put("Choose group...", "选择组...");
		m.put("Choose groups...", "选择组...");
		m.put("Choose issue...", "选择工单...");
		m.put("Choose issues...", "选择工单...");
		m.put("Choose iteration...", "选择迭代...");
		m.put("Choose iterations...", "选择迭代...");
		m.put("Choose job...", "选择任务...");
		m.put("Choose jobs...", "选择任务...");
		m.put("Choose project", "选择项目");
		m.put("Choose projects...", "选择项目...");
		m.put("Choose pull request...", "选择合并请求...");
		m.put("Choose repository", "选择仓库");
		m.put("Choose role...", "选择角色...");
		m.put("Choose roles...", "选择角色...");
		m.put("Choose users...", "选择用户...");
		m.put("Choose...", "选择...");
		m.put("Circular build spec imports ({0})", "循环构建规范导入（{0}）");
		m.put("Click to select a commit, or shift-click to select multiple commit", "点击选择一个提交，或按住 Shift 点击选择多个提交");
		m.put("Click to show comment of marked text", "点击显示标记文本的评论");
		m.put("Click to show issue details", "点击显示工单详情");
		m.put("Client ID of this OneDev instance registered in Google cloud", "在 Google Cloud 注册的本 OneDev 实例的客户端 ID");
		m.put("Client Id", "客户端 ID");
		m.put("Client Secret", "客户端密钥");
		m.put("Client secret of this OneDev instance registered in Google cloud", "在 Google Cloud 注册的本 OneDev 实例的客户端密钥");
		m.put("Clippy Report", "Clippy 报告");
		m.put("Clone", "克隆");
		m.put("Clone Credential", "克隆凭据");
		m.put("Clone Depth", "克隆深度");
		m.put("Clone in IntelliJ", "在 IntelliJ 中克隆");
		m.put("Clone in VSCode", "在 VSCode 中克隆");
		m.put("Close", "关闭");
		m.put("Close Iteration", "关闭迭代");
		m.put("Close this iteration", "关闭此迭代");
		m.put("Closed", "关闭");
		m.put("Closed Issue State", "已关闭的工单状态");
		m.put("Closest due date", "最近的截止日期");
		m.put("Clover Coverage Report", "Clover 覆盖率报告");
		m.put("Cluster Role", "集群角色");
		m.put("Cluster Setting", "集群设置");
		m.put("Cluster setting", "集群设置");
		m.put("Clustered Servers", "集群服务器");
		m.put("Cobertura Coverage Report", "Cobertura 覆盖率报告");
		m.put("Code", "代码");
		m.put("Code Analysis", "代码分析");
		m.put("Code Analysis Setting", "代码分析设置");
		m.put("Code Analysis Settings", "代码分析设置");
		m.put("Code Changes", "代码变更");
		m.put("Code Comment", "代码评论");
		m.put("Code Comment Management", "代码评论管理");
		m.put("Code Comments", "代码评论");
		m.put("Code Compare", "代码比较");
		m.put("Code Contribution Statistics", "代码贡献统计");
		m.put("Code Coverage", "代码覆盖率");
		m.put("Code Line Statistics", "代码行统计");
		m.put("Code Management", "代码管理");
		m.put("Code Privilege", "代码权限");
		m.put("Code Problem Statistics", "代码问题统计");
		m.put("Code Search", "代码搜索");
		m.put("Code Statistics", "代码统计");
		m.put("Code analysis settings updated", "代码分析设置已更新");
		m.put("Code changes since...", "代码变更自...");
		m.put("Code clone or download", "克隆或下载代码");
		m.put("Code comment", "代码评论");
		m.put("Code comment #{0} deleted", "代码评论 #{0} 已删除");
		m.put("Code comment administrative permission inside a project, including batch operations over multiple code comments", 
			"项目内的代码评论管理权限，包括对多个代码评论的批量操作");
		m.put("Code commit", "代码提交");
		m.put("Code is committed", "代码已提交");
		m.put("Code push", "代码推送");
		m.put("Code read permission is required to import build spec (import project: {0}, import revision: {1})", 
			"需要代码读取权限才能导入构建规范（导入项目：{0}，导入版本：{1})");
		m.put("Code suggestion", "代码建议");
		m.put("Code write permission is required for this operation", "需要代码写入权限才能执行此操作");
		m.put("Collapse all", "全部折叠");
		m.put("Color", "颜色");
		m.put("Columns", "列");
		m.put("Command Palette", "命令面板");
		m.put("Commands", "命令");
		m.put("Comment", "评论");
		m.put("Comment Content", "评论内容");
		m.put("Comment on File", "评论文件");
		m.put("Comment too long", "评论过长");
		m.put("Commented code is outdated", "评论的代码已过时");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "在项目 \"{1}\" 的文件 \"{0}\" 上评论");
		m.put("Commented on issue \"{0}\" ({1})", "在工单 \"{0}\" 上评论（{1}）");
		m.put("Commented on pull request \"{0}\" ({1})", "在合并请求 \"{0}\" 上评论（{1}）");
		m.put("Comments", "评论");
		m.put("Commit", "提交");
		m.put("Commit &amp; Insert", "提交并插入");
		m.put("Commit Batched Suggestions", "提交批量建议");
		m.put("Commit Message", "提交信息");
		m.put("Commit Message Bean", "提交信息 Bean");
		m.put("Commit Message Fix Patterns", "提交消息修复模式");
		m.put("Commit Message Footer Pattern", "提交信息尾部模式");
		m.put("Commit Notification", "提交通知");
		m.put("Commit Notification Template", "提交通知模板");
		m.put("Commit Scopes", "提交作用域");
		m.put("Commit Signature Required", "需要提交签名");
		m.put("Commit Suggestion", "提交建议");
		m.put("Commit Types", "提交类型");
		m.put("Commit Types For Footer Check", "用于尾部检查的提交类型");
		m.put("Commit Your Change", "提交您的更改");
		m.put("Commit date", "提交日期");
		m.put("Commit hash", "提交哈希");
		m.put("Commit history of current path", "当前路径的提交历史");
		m.put("Commit index version", "提交索引版本");
		m.put("Commit message can be used to fix issues by prefixing and suffixing issue number with specified pattern. Each line of the commit message will be matched against each entry defined here to find issues to be fixed", 
			"提交消息可以用于通过在指定模式下前缀和后缀工单编号来修复工单。提交消息的每一行将与定义在此处的每个条目进行匹配，以找到需要修复的工单");
		m.put("Commit not exist or access denied", "提交不存在或访问被拒绝");
		m.put("Commit of the build is missing", "构建的提交缺失");
		m.put("Commit signature required but no GPG signing key specified", "提交签名需要但未指定 GPG 签名密钥");
		m.put("Commit suggestion", "提交建议");
		m.put("Commits", "提交");
		m.put("Commits are taken from default branch of non-forked repositories", "代码提交取自非分叉仓库的默认分支");
		m.put("Commits generated by OneDev previously will be shown as unverified if this key is deleted. Type <code>yes</code> below if you want to continue.", 
			"如果删除此 GPG 公钥，OneDev 生成的提交将显示为未验证。如果继续，请在下面输入 <code>yes</code>。");
		m.put("Commits were merged into target branch", "提交已合并到目标分支");
		m.put("Commits were merged into target branch outside of this pull request", "提交已合并到目标分支，但不在当前合并请求中");
		m.put("Commits were rebased onto target branch", "提交已 rebase 到目标分支");
		m.put("Commits were squashed into a single commit on target branch", "提交已 squash 到目标分支");
		m.put("Committed After", "提交后");
		m.put("Committed Before", "提交前");
		m.put("Committed By", "提交者");
		m.put("Committer", "提交者");
		m.put("Compare", "比较");
		m.put("Compare with base revision", "与基线版本比较");
		m.put("Compare with this parent", "与此父节点比较");
		m.put("Concurrency", "并发");
		m.put("Condition", "条件");
		m.put("Confidential", "保密");
		m.put("Config File", "配置文件");
		m.put("Configuration Discovery Url", "配置发现 URL");
		m.put("Configure your scope to use below registry", "配置您的范围以使用以下注册表");
		m.put("Confirm Approve", "确认批准");
		m.put("Confirm Delete Source Branch", "确认删除源分支");
		m.put("Confirm Discard", "确认放弃");
		m.put("Confirm Reopen", "确认重新打开");
		m.put("Confirm Request For Changes", "确认请求修改");
		m.put("Confirm Restore Source Branch", "确认恢复源分支");
		m.put("Confirm password here", "在此确认密码");
		m.put("Confirm your action", "确认你的操作");
		m.put("Connect New Agent", "连接新代理");
		m.put("Connect with your SSO account", "使用您的 SSO 账户连接");
		m.put("Contact Email", "联系邮箱");
		m.put("Contact Name", "联系人");
		m.put("Container Image", "容器镜像");
		m.put("Container Image(s)", "容器镜像");
		m.put("Container default", "容器默认");
		m.put("Content", "内容");
		m.put("Content Type", "内容类型");
		m.put("Content is identical", "内容相同");
		m.put("Continue to add other user after create", "创建后继续添加其他用户");
		m.put("Contributed settings", "贡献的设置");
		m.put("Contributions", "贡献");
		m.put("Contributions to {0} branch, excluding merge commits", "在 {0} 分支上的贡献，不包括合并提交");
		m.put("Convert All Queried to Service Accounts", "将所有查询的用户转换为服务账户");
		m.put("Convert Selected to Service Accounts", "将选定的用户转换为服务账户");
		m.put("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm", 
			"转换为服务账户将移除密码、电子邮件地址、所有分配和关注。输入<code>yes</code>以确认");
		m.put("Copy", "复制");
		m.put("Copy All Queried Issues To...", "复制所有查询的工单到...");
		m.put("Copy Files with SCP", "使用 SCP 复制文件");
		m.put("Copy Selected Issues To...", "复制选定的工单到...");
		m.put("Copy dashboard", "复制仪表板");
		m.put("Copy issue number and title", "复制工单编号和标题");
		m.put("Copy public key", "复制公钥");
		m.put("Copy selected text to clipboard", "复制选中的文本到剪贴板");
		m.put("Copy to clipboard", "复制到剪贴板");
		m.put("Count", "数量");
		m.put("Coverage Statistics", "代码覆盖率统计");
		m.put("Covered", "覆盖");
		m.put("Covered by tests", "被测试覆盖");
		m.put("Cppcheck Report", "Cppcheck 报告");
		m.put("Cpu Limit", "CPU 限制");
		m.put("Cpu Request", "CPU 请求");
		m.put("Create", "创建");
		m.put("Create Administrator Account", "创建管理员账户");
		m.put("Create Branch", "创建分支");
		m.put("Create Branch Bean", "创建分支 Bean");
		m.put("Create Branch Bean With Revision", "带版本的创建分支 Bean");
		m.put("Create Child Project", "创建子项目");
		m.put("Create Child Projects", "创建子项目");
		m.put("Create Issue", "创建工单");
		m.put("Create Iteration", "创建迭代");
		m.put("Create Merge Commit", "创建合并提交");
		m.put("Create Merge Commit If Necessary", "仅当必要时创建合并提交");
		m.put("Create New", "新建");
		m.put("Create New File", "创建新文件");
		m.put("Create New User", "创建新用户");
		m.put("Create Project", "创建项目");
		m.put("Create Pull Request", "创建合并请求");
		m.put("Create Pull Request for This Change", "为此变更创建合并请求");
		m.put("Create Tag", "创建标签");
		m.put("Create Tag Bean", "创建标签 Bean");
		m.put("Create Tag Bean With Revision", "带版本的创建标签 Bean");
		m.put("Create User", "创建用户");
		m.put("Create body", "创建正文");
		m.put("Create branch <b>{0}</b> from {1}", "创建分支 <b>{0}</b>（基于 {1}）");
		m.put("Create child projects under a project", "在项目下创建子项目");
		m.put("Create issue", "创建工单");
		m.put("Create merge commit", "创建合并提交");
		m.put("Create merge commit if necessary", "如有必要，创建合并提交");
		m.put("Create new issue", "创建新工单");
		m.put("Create tag", "创建标签");
		m.put("Create tag <b>{0}</b> from {1}", "创建标签 <b>{0}</b>（基于 {1}）");
		m.put("Created At", "创建于");
		m.put("Creation of this branch is prohibited per branch protection rule", "此分支的创建受分支保护规则限制");
		m.put("Critical", "严重");
		m.put("Critical Severity", "非常严重");
		m.put("Cron Expression", "Cron 表达式");
		m.put("Cron schedule", "Cron 调度");
		m.put("Curl Location", "Curl 路径");
		m.put("Current Iteration", "当前迭代");
		m.put("Current Value", "现值");
		m.put("Current avatar", "当前头像");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"当前上下文与添加此评论时的上下文不同，点击显示评论上下文");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"当前上下文与添加此回复时的上下文不同，点击显示回复上下文");
		m.put("Current context is different from this action, click to show the comment context", "当前上下文与此操作不同，点击显示评论上下文");
		m.put("Current platform", "当前平台");
		m.put("Current project", "当前项目");
		m.put("Custom Linux Shell", "自定义 Linux Shell");
		m.put("DISCARDED", "已放弃");
		m.put("Dashboard Share Bean", "仪表盘共享 Bean");
		m.put("Dashboard name", "仪表板名称");
		m.put("Dashboards", "仪表盘");
		m.put("Database Backup", "数据库备份");
		m.put("Date", "日期");
		m.put("Date Time", "日期时间");
		m.put("Days Per Week", "每周工作天数");
		m.put("Deactivate Subscription", "停用订阅");
		m.put("Deactivate Trial Subscription", "停用试用订阅");
		m.put("Default", "默认");
		m.put("Default (Shell on Linux, Batch on Windows)", "默认（Linux 上为 Shell，Windows 上为 Batch）");
		m.put("Default Assignees", "默认负责人");
		m.put("Default Boards", "默认看板");
		m.put("Default Fixed Issue Filter", "默认修复工单过滤器");
		m.put("Default Fixed Issue Filters", "默认修复工单过滤器");
		m.put("Default Fixed Issue Filters Bean", "默认修复工单过滤器 Bean");
		m.put("Default Group", "默认分组");
		m.put("Default Issue Boards", "默认工单看板");
		m.put("Default Merge Strategy", "默认合并策略");
		m.put("Default Multi Value Provider", "默认多值提供器");
		m.put("Default Project", "默认项目");
		m.put("Default Project Setting", "默认项目设置");
		m.put("Default Roles", "默认角色");
		m.put("Default Roles Bean", "默认角色 Bean");
		m.put("Default Value", "默认值");
		m.put("Default Value Provider", "默认值提供器");
		m.put("Default Values", "默认值");
		m.put("Default branch", "默认分支");
		m.put("Default branding settings restored", "已恢复默认品牌设置");
		m.put("Default fixed issue filters saved", "默认固定工单过滤器已保存");
		m.put("Default merge strategy", "默认合并策略");
		m.put("Default roles affect default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"默认角色影响系统中每个人的默认权限。实际的默认权限将是该项目及其所有父项目中默认角色包含的<b class='text-warning'>所有权限</b>");
		m.put("Define all custom issue fields here. Each project can decide to use all or a subset of these fields via its issue transition setting. <b class=\"text-warning\">NOTE: </b> Newly defined fields by default only appear in new issues. Batch edit existing issues from issue list page if you want them to have these new fields", 
			"在此处定义所有自定义工单字段。每个项目可以通过其工单状态设置决定使用所有或这些字段的一个子集。<b class=\"text-warning\">注意：</b> 默认情况下，新定义的字段仅出现在新工单中。如果您希望这些新字段出现在现有工单中，请从工单列表页面批量编辑这些工单");
		m.put("Define all custom issue states here. The first state will be used as initial state of created issues", 
			"在此定义所有自定义工单状态。第一个状态将用作创建的工单的初始状态");
		m.put("Define branch protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given branch and user, the first matching rule will take effect", 
			"定义分支保护规则。父项目中定义的规则被视为在此处定义的规则之后定义的规则。对于给定的分支和用户，第一个匹配的规则将生效");
		m.put("Define default issue boards for all projects here. A certain project can override this setting to define its own issue boards.", 
			"在此定义所有项目的默认工单看板。特定项目可以覆盖此设置以定义其自己的工单看板。");
		m.put("Define how issue states should be transited from one to another, either manually or automatically when some events happen. And the rule can be configured to apply to certain projects and issues via the applicable issues setting", 
			"定义工单状态如何从一个状态转换到另一个状态，无论是手动还是自动当某些事件发生时。规则可以通过应用工单设置配置为应用于某些项目和工单");
		m.put("Define issue templates here. When a new issue is created, the first matching template will be used.", 
			"在此定义工单模板。当创建新工单时，将使用第一个匹配的模板");
		m.put("Define labels to be assigned to project, build or pull request. For issues, custom fields can be used which is much more powerful than labels", 
			"定义要分配给项目、构建或合并请求的标签。对于工单，可以使用自定义字段，这比标签更强大");
		m.put("Define properties to be used in build spec. Properties will be inherited by child projects, and can be overridden by child properties with same name.", 
			"定义要在构建规范中使用的属性。属性将继承子项目，并且可以被具有相同名称的子属性覆盖。");
		m.put("Define rules to preserve builds. A build will be preserved as long as one rule defined here or in parent projects preserves it. All builds will be preserved if no rules are defined here and in parent projects", 
			"定义构建保留规则。构建将保留，只要此处或父项目中定义的规则保留它。如果此处和父项目中都没有定义规则，则所有构建都将保留");
		m.put("Define tag protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given tag and user, the first matching rule will take effect", 
			"定义标签保护规则。父项目中定义的规则被视为在此处定义的规则之后定义的规则。对于给定的标签和用户，第一个匹配的规则将生效");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"首次重试的延迟时间（秒）。后续重试的延迟时间将基于该值进行指数退避计算");
		m.put("Delete", "删除");
		m.put("Delete All", "删除所有");
		m.put("Delete All Queried Builds", "删除所有查询到的构建");
		m.put("Delete All Queried Comments", "删除所有查询到的评论");
		m.put("Delete All Queried Issues", "删除所有查询到的工单");
		m.put("Delete All Queried Packages", "删除所有查询到的包");
		m.put("Delete All Queried Projects", "删除所有查询到的项目");
		m.put("Delete All Queried Pull Requests", "删除所有查询到的合并请求");
		m.put("Delete All Queried Users", "删除所有查询到的用户");
		m.put("Delete Build", "删除构建");
		m.put("Delete Comment", "删除评论");
		m.put("Delete Pull Request", "删除合并请求");
		m.put("Delete SSO account here to reconnect corresponding SSO subject upon next login. Note that SSO subject with verified email will be connected to user with same verified email automatically", 
			"在此删除 SSO 账户以便在下次登录时重新连接相应的 SSO 主题。请注意，具有已验证电子邮件的 SSO 主题将自动连接到具有相同已验证电子邮件的用户");
		m.put("Delete Selected", "删除选中的");
		m.put("Delete Selected Builds", "删除选定的构建");
		m.put("Delete Selected Comments", "删除选中的评论");
		m.put("Delete Selected Issues", "删除选定的工单");
		m.put("Delete Selected Packages", "删除选中的包");
		m.put("Delete Selected Projects", "删除选中的项目");
		m.put("Delete Selected Pull Requests", "删除选定的合并请求");
		m.put("Delete Selected Users", "删除选定的用户");
		m.put("Delete Source Branch", "删除源分支");
		m.put("Delete Source Branch After Merge", "合并后删除源分支");
		m.put("Delete dashboard", "删除仪表板");
		m.put("Delete from branch {0}", "从分支 {0} 删除");
		m.put("Delete this", "删除");
		m.put("Delete this GPG key", "删除此 GPG 密钥");
		m.put("Delete this access token", "删除此访问令牌");
		m.put("Delete this branch", "删除此分支");
		m.put("Delete this executor", "删除此执行器");
		m.put("Delete this field", "删除该字段");
		m.put("Delete this import", "删除此导入");
		m.put("Delete this iteration", "删除此迭代");
		m.put("Delete this key", "删除此密钥");
		m.put("Delete this link", "删除此链接");
		m.put("Delete this rule", "删除此规则");
		m.put("Delete this secret", "删除此密钥");
		m.put("Delete this state", "删除该状态");
		m.put("Delete this tag", "删除此标签");
		m.put("Delete this value", "删除该值");
		m.put("Deleted source branch", "已删除源分支");
		m.put("Deletion not allowed due to branch protection rule", "由于分支保护规则，不允许删除此分支");
		m.put("Deletion not allowed due to tag protection rule", "由于标签保护规则，删除不允许");
		m.put("Deletions", "删除");
		m.put("Denied", "拒绝");
		m.put("Dependencies & Services", "依赖与服务");
		m.put("Dependency Management", "依赖管理");
		m.put("Dependency job finished", "依赖任务已完成");
		m.put("Dependent Fields", "依赖字段");
		m.put("Depends on <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>", "依赖于 <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>");
		m.put("Descending", "降序");
		m.put("Description", "描述");
		m.put("Description Template", "描述模板");
		m.put("Description Templates", "描述模板");
		m.put("Description too long", "描述过长");
		m.put("Destination Path", "目标路径");
		m.put("Destinations", "目标");
		m.put("Detect Licenses", "检测许可证");
		m.put("Detect Secrets", "检测敏感信息");
		m.put("Detect Vulnerabilities", "检测漏洞");
		m.put("Diff is too large to be displayed.", "差异太大，无法显示");
		m.put("Diff options", "比较选项");
		m.put("Digest", "摘要");
		m.put("Digest invalid", "摘要无效");
		m.put("Directories to Skip", "要跳过的目录");
		m.put("Directory", "目录");
		m.put("Directory (tenant) ID", "目录（租户）ID");
		m.put("Disable", "禁用");
		m.put("Disable All Queried Users", "禁用所有查询的用户");
		m.put("Disable Auto Update Check", "禁用自动更新检查");
		m.put("Disable Dashboard", "禁用仪表盘");
		m.put("Disable Selected Users", "禁用选定的用户");
		m.put("Disabled", "已禁用");
		m.put("Disabled users and service accounts are excluded from user-month calculation", "禁用的用户和服务账户不计入用户月计算");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"禁用账户将重置密码，清除访问令牌，并删除除过去活动外的所有其他实体的引用。您真的要继续吗？");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"禁用账户将重置密码，清除访问令牌，并从其他实体中删除除过去活动外的所有引用。输入 <code>yes</code> 确认");
		m.put("Disallowed File Types", "不允许的文件类型");
		m.put("Disallowed file type(s): {0}", "不允许的文件类型：{0}");
		m.put("Discard", "丢弃");
		m.put("Discard All Queried Pull Requests", "放弃所有查询的合并请求");
		m.put("Discard Selected Pull Requests", "放弃选定的合并请求");
		m.put("Discarded", "已丢弃");
		m.put("Discarded pull request \"{0}\" ({1})", "放弃合并请求 \"{0}\"（{1}）");
		m.put("Discord", "Discord");
		m.put("Discord Notifications", "Discord 通知");
		m.put("Display Fields", "显示字段");
		m.put("Display Links", "显示链接");
		m.put("Display Months", "显示月份");
		m.put("Display Params", "显示参数");
		m.put("Do Not Retrieve Groups", "不获取用户组");
		m.put("Do not ignore", "不要忽略");
		m.put("Do not ignore whitespace", "不忽略空白");
		m.put("Do not retrieve", "不获取");
		m.put("Do not retrieve groups", "不获取组");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "您真的要取消邀请 \"{0}\" 吗？");
		m.put("Do you really want to cancel this build?", "您确定要取消此构建吗？");
		m.put("Do you really want to change target branch to {0}?", "确定要将目标分支更改为 {0} 吗？");
		m.put("Do you really want to delete \"{0}\"?", "您真的要删除 \"{0}\" 吗？");
		m.put("Do you really want to delete SSO provider \"{0}\"?", "您真的想删除 SSO 提供方 \"{0}\" 吗？");
		m.put("Do you really want to delete board \"{0}\"?", "您确定要删除看板 \"{0}\" 吗？");
		m.put("Do you really want to delete build #{0}?", "您确定要删除构建 #{0} 吗？");
		m.put("Do you really want to delete group \"{0}\"?", "您真的要删除组 \"{0}\" 吗？");
		m.put("Do you really want to delete iteration \"{0}\"?", "您确定要删除迭代 \"{0}\" 吗？");
		m.put("Do you really want to delete job secret \"{0}\"?", "您确定要删除任务密钥 \"{0}\" 吗？");
		m.put("Do you really want to delete pull request #{0}?", "您确定要删除合并请求 #{0} 吗？");
		m.put("Do you really want to delete role \"{0}\"?", "确定要删除角色 \"{0}\" 吗？");
		m.put("Do you really want to delete selected query watches?", "您真的要删除选中的查询订阅吗？");
		m.put("Do you really want to delete tag {0}?", "确定要删除标签 {0} 吗？");
		m.put("Do you really want to delete this GPG key?", "您确定要删除此 GPG 密钥吗？");
		m.put("Do you really want to delete this SSH key?", "您确定要删除此 SSH 密钥吗？");
		m.put("Do you really want to delete this SSO account?", "您真的想删除此 SSO 账户吗？");
		m.put("Do you really want to delete this access token?", "您确定要删除此访问令牌吗？");
		m.put("Do you really want to delete this board?", "您确定要删除此看板吗？");
		m.put("Do you really want to delete this build?", "您确定要删除此构建吗？");
		m.put("Do you really want to delete this code comment and all its replies?", "您真的要删除此代码评论及其所有回复吗？");
		m.put("Do you really want to delete this code comment?", "您真的要删除此代码评论吗？");
		m.put("Do you really want to delete this directory?", "您确定要删除此目录吗？");
		m.put("Do you really want to delete this email address?", "您真的要删除这个电子邮件地址吗？");
		m.put("Do you really want to delete this executor?", "你确定要删除这个执行器吗？");
		m.put("Do you really want to delete this field?", "您真的要删除这个字段吗？");
		m.put("Do you really want to delete this file?", "您确定要删除此文件吗？");
		m.put("Do you really want to delete this issue?", "您真的要删除这个工单吗？");
		m.put("Do you really want to delete this link?", "您真的要删除此链接吗？");
		m.put("Do you really want to delete this package?", "确定要删除这个包吗？");
		m.put("Do you really want to delete this privilege?", "确定要删除此权限吗？");
		m.put("Do you really want to delete this protection?", "您确定要删除此保护吗？");
		m.put("Do you really want to delete this pull request?", "确定要删除此合并请求吗？");
		m.put("Do you really want to delete this reply?", "您真的要删除此回复吗？");
		m.put("Do you really want to delete this script?", "您确定要删除此脚本吗？");
		m.put("Do you really want to delete this state?", "您真的要删除此状态吗？");
		m.put("Do you really want to delete this template?", "您真的要删除此模板吗？");
		m.put("Do you really want to delete this transition?", "您真的要删除此转换吗？");
		m.put("Do you really want to delete timesheet \"{0}\"?", "您真的要删除时间表 \"{0}\" 吗？");
		m.put("Do you really want to delete unused tokens?", "你确定要删除未使用的令牌吗？");
		m.put("Do you really want to discard batched suggestions?", "您真的要丢弃批量建议吗？");
		m.put("Do you really want to enable this account?", "您真的要启用此账户吗？");
		m.put("Do you really want to rebuild?", "您确定要重建此构建吗？");
		m.put("Do you really want to remove assignee \"{0}\"?", "确定要移除指派人 \"{0}\" 吗？");
		m.put("Do you really want to remove password of this user?", "您真的要删除此用户的密码吗？");
		m.put("Do you really want to remove the issue from iteration \"{0}\"?", "您真的要从迭代 \"{0}\" 中移除这个工单吗？");
		m.put("Do you really want to remove this account?", "您确定要删除此账户吗？");
		m.put("Do you really want to remove this agent?", "你确定要删除这个代理吗？");
		m.put("Do you really want to remove this link?", "您确定要删除此链接吗？");
		m.put("Do you really want to restart this agent?", "你确定要重启这个代理吗？");
		m.put("Do you really want to unauthorize user \"{0}\"?", "您真的要取消用户 \"{0}\" 的授权吗？");
		m.put("Do you really want to use default template?", "您确定要使用默认模板吗？");
		m.put("Docker", "Docker");
		m.put("Docker Executable", "Docker 可执行文件");
		m.put("Docker Hub", "Docker Hub");
		m.put("Docker Image", "Docker 镜像");
		m.put("Docker Sock Path", "Docker Sock 路径");
		m.put("Dockerfile", "Dockerfile");
		m.put("Documentation", "文档");
		m.put("Don't have an account yet?", "没有账户？");
		m.put("Download", "下载");
		m.put("Download <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> or <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. A new agent token will be included in the package", 
			"下载 <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> 或 <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>。新代理令牌将包含在包中");
		m.put("Download archive of this branch", "下载此分支的归档");
		m.put("Download full log", "下载完整日志");
		m.put("Download log", "下载日志");
		m.put("Download patch", "下载补丁");
		m.put("Download tag archive", "下载标签归档");
		m.put("Dry Run", "试运行");
		m.put("Due Date", "截止日期");
		m.put("Due Date Issue Field", "截止日期工单字段");
		m.put("Due date", "截止日期");
		m.put("Duplicate authorizations found: ", "找到重复的授权：");
		m.put("Duplicate authorizations found: {0}", "找到重复的授权：{0}");
		m.put("Duration", "持续时间");
		m.put("Durations", "持续时间");
		m.put("ESLint Report", "ESLint 报告");
		m.put("Edit", "编辑");
		m.put("Edit <code>$HOME/.gem/credentials</code> to add a source", "编辑 <code>$HOME/.gem/credentials</code> 以添加一个源");
		m.put("Edit <code>$HOME/.pypirc</code> to add a package repository like below", "编辑 <code>$HOME/.pypirc</code> 以添加一个包仓库，如下所示");
		m.put("Edit Avatar", "编辑头像");
		m.put("Edit Estimated Time", "编辑预计时间");
		m.put("Edit Executor", "编辑任务执行器");
		m.put("Edit Iteration", "编辑迭代");
		m.put("Edit Job Secret", "编辑任务密钥");
		m.put("Edit My Avatar", "编辑我的头像");
		m.put("Edit Rule", "编辑规则");
		m.put("Edit Timesheet", "编辑工时表");
		m.put("Edit dashboard", "编辑仪表板");
		m.put("Edit issue title", "编辑工单标题");
		m.put("Edit job", "编辑任务");
		m.put("Edit on branch {0}", "在分支 {0} 编辑");
		m.put("Edit on source branch", "在源分支上编辑");
		m.put("Edit plain", "编辑纯文本");
		m.put("Edit saved queries", "编辑保存的查询");
		m.put("Edit this access token", "编辑此访问令牌");
		m.put("Edit this executor", "编辑此执行器");
		m.put("Edit this iteration", "编辑此迭代");
		m.put("Edit this rule", "编辑此规则");
		m.put("Edit this secret", "编辑此密钥");
		m.put("Edit this state", "编辑此状态");
		m.put("Edit title", "编辑标题");
		m.put("Edit with AI", "使用 AI 编辑");
		m.put("Edit {0}", "编辑 {0}");
		m.put("Editable Issue Fields", "可编辑的工单字段");
		m.put("Editable Issue Links", "可编辑的工单链接");
		m.put("Edited by {0} {1}", "由 {0} 于 {1} 编辑");
		m.put("Editor", "编辑器");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "目标分支或源分支有新的提交，请重新检查。");
		m.put("Email", "邮箱");
		m.put("Email Address", "邮箱地址");
		m.put("Email Address Verification", "邮箱地址验证");
		m.put("Email Addresses", "电子邮件地址");
		m.put("Email Templates", "邮件模板");
		m.put("Email Verification", "邮件验证");
		m.put("Email Verification Template", "邮箱验证模板");
		m.put("Email address", "电子邮件地址");
		m.put("Email address \"{0}\" already used by another account", "电子邮件地址 \"{0}\" 已被另一个账户使用");
		m.put("Email address \"{0}\" used by account \"{1}\"", "电子邮件地址 \"{0}\" 被账户 \"{1}\" 使用");
		m.put("Email address \"{0}\" used by disabled account \"{1}\"", "电子邮件地址 \"{0}\" 被已禁用的账户 \"{1}\" 使用");
		m.put("Email address already in use: {0}", "电子邮件地址已被使用: {0}");
		m.put("Email address already invited: {0}", "电子邮件地址已被邀请: {0}");
		m.put("Email address already used by another user", "电子邮件地址已被另一个用户使用");
		m.put("Email address already used: ", "邮箱地址已被使用：");
		m.put("Email address to verify", "待验证的邮箱地址");
		m.put("Email addresses with <span class=\"badge badge-warning badge-sm\">ineffective</span> mark are those not belong to or not verified by key owner", 
			"带有 <span class=\"badge badge-warning badge-sm\">无效</span> 标记的电子邮件地址是那些不属于或未被密钥所有者验证的电子邮件地址");
		m.put("Email templates", "邮箱模板");
		m.put("Empty file added.", "空文件已添加");
		m.put("Empty file removed.", "空文件已删除");
		m.put("Enable", "启用");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets", 
			"为此项目启用<a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>时间跟踪</a>以跟踪进度并生成时间表");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project", 
			"为此项目启用<a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>包管理</a>");
		m.put("Enable Account Self Removal", "启用账户自删除");
		m.put("Enable Account Self Sign-Up", "启用账户自注册");
		m.put("Enable All Queried Users", "启用所有查询的用户");
		m.put("Enable Anonymous Access", "启用匿名访问");
		m.put("Enable Auto Backup", "启用自动备份");
		m.put("Enable Html Report Publish", "启用 Html 报告发布");
		m.put("Enable Selected Users", "启用选定的用户");
		m.put("Enable Site Publish", "启用站点发布");
		m.put("Enable TTY Mode", "启用 TTY 模式");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "通过 <a wicket:id=\"addFile\" class=\"link-primary\"></a> 启用构建支持");
		m.put("Enable if visibility of this field depends on other fields", "如果字段的可见性取决于其他字段，则启用");
		m.put("Enable if visibility of this param depends on other params", "如果参数的可见性取决于其他参数，则启用");
		m.put("Enable this if the access token has same permissions as the owner", "如果访问令牌具有与所有者相同的权限，则启用");
		m.put("Enable this option to merge the pull request automatically when ready (all reviewers approved, all required jobs passed etc.)", 
			"启用此选项以在准备好时自动合并合并请求（所有审查者批准，所有必需的任务通过等）");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"启用此选项以允许运行 HTML 报告发布步骤。为避免 XSS 攻击，请确保此执行器只能由受信任的任务使用");
		m.put("Enable this to allow to run site publish step. OneDev will serve project site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"启用此选项以允许运行站点发布步骤。OneDev 将按原样提供项目站点文件。为避免 XSS 攻击，请确保此执行器只能由受信任的任务使用");
		m.put("Enable this to place intermediate files required by job execution on dynamically allocated persistent volume instead of emptyDir", 
			"启用此选项以将任务执行所需的中间文件放置在动态分配的持久卷而不是 emptyDir 中");
		m.put("Enable this to process issue or pull request comments posted via email", "启用此选项以处理通过电子邮件发布的工单或合并请求评论");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"启用此选项以处理通过电子邮件发布的工单或合并请求评论。<b class='text-danger'>注意：</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> 需要为系统电子邮件地址启用，因为 OneDev 使用它来跟踪工单和合并请求上下文");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"启用此选项以处理通过电子邮件发布的工单或合并请求评论。<b class='text-danger'>注意：</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>子地址</a> 需要为上述系统电子邮件地址启用，因为 OneDev 使用它来跟踪工单和合并请求上下文");
		m.put("Enable to allow to upload build cache generated during CI/CD job. Uploaded cache can be used by subsequent builds of the project as long as cache key matches", 
			"启用此选项以允许上传在 CI/CD 任务期间生成的构建缓存。上传的缓存可以被项目后续的构建使用，只要缓存密钥匹配");
		m.put("End Point", "端点");
		m.put("Enforce Conventional Commits", "强制执行 Conventional Commits");
		m.put("Enforce Password Policy", "强制执行密码策略");
		m.put("Enforce Two-factor Authentication", "强制执行两阶段认证");
		m.put("Enforce password policy for new users", "对新用户强制执行密码策略");
		m.put("Enter New Password", "输入新密码");
		m.put("Enter description here", "输入描述");
		m.put("Enter your details to login to your account", "输入您的详细信息登录到您的账户");
		m.put("Enter your user name or email to reset password", "输入您的用户名或邮箱以重置密码");
		m.put("Entries", "条目");
		m.put("Entry", "条目");
		m.put("Enumeration", "枚举");
		m.put("Env Var", "环境变量");
		m.put("Environment Variables", "环境变量");
		m.put("Environment variable <code>serverUrl</code> in above command is taken from OneDev server url specified in <i>Administration / System Setting</i>. Change it if necessary", 
			"上面的命令中的环境变量 <code>serverUrl</code> 取自 <i>管理 / 系统设置</i> 中指定的 OneDev 服务器 URL。如果需要，请更改它");
		m.put("Equal", "等于");
		m.put("Error authenticating user", "用户认证错误");
		m.put("Error calculating commits: check log for details", "计算提交时出错：请检查日志");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "cherry-pick 到 {0} 时出错：合并冲突检测到");
		m.put("Error cherry-picking to {0}: {1}", "cherry-pick 到 {0} 时出错：{1}");
		m.put("Error detail of content type &quot;text/plain&quot;", "内容类型为\"text/plain\"的错误详情");
		m.put("Error discovering OIDC metadata", "发现 OIDC 元数据时出错");
		m.put("Error executing task", "执行任务时发生错误");
		m.put("Error parsing %sbase query: ", "解析%s基准查询时出错：");
		m.put("Error parsing %squery: ", "解析%s查询时出错：");
		m.put("Error parsing build spec", "解析构建规范时出错");
		m.put("Error rendering widget, check server log for details", "渲染小部件时出错，请查看服务器日志了解详情");
		m.put("Error reverting on {0}: Merge conflicts detected", "revert 到 {0} 时出错：合并冲突检测到");
		m.put("Error reverting on {0}: {1}", "revert 到 {0} 时出错：{1}");
		m.put("Error validating auto merge commit message: {0}", "验证自动合并提交信息时出错：{0}");
		m.put("Error validating build spec (location: {0}, error message: {1})", "验证构建规范时发生错误（位置：{0}，错误消息：{1}）");
		m.put("Error validating build spec: {0}", "验证构建规范时发生错误：{0}");
		m.put("Error validating commit message of \"{0}\": {1}", "验证提交信息 \"{0}\" 时出错：{1}");
		m.put("Error validating commit message of <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}", 
			"验证提交信息 <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a> 时出错：{2}");
		m.put("Error verifying GPG signature", "验证 GPG 签名时出错");
		m.put("Estimated Time", "预计时间");
		m.put("Estimated Time Edit Bean", "预计时间编辑 Bean");
		m.put("Estimated Time Issue Field", "预计时间工单字段");
		m.put("Estimated Time:", "预计时间");
		m.put("Estimated time", "预计时间");
		m.put("Estimated/Spent time. Click for details", "预计/已用时间。点击查看详情");
		m.put("Evaluate script to get choices", "运行脚本以获取选项");
		m.put("Evaluate script to get default value", "运行脚本以获取默认值");
		m.put("Evaluate script to get value or secret", "运行脚本以获取值或密钥");
		m.put("Evaluate script to get values or secrets", "运行脚本以获取值或密钥");
		m.put("Event Types", "事件类型");
		m.put("Events", "事件");
		m.put("Ever Used Since", "上次使用时间");
		m.put("Everything inside this project and all child projects will be deleted and can not be recovered, please type project path <code>{0}</code> below to confirm deletion.", 
			"此项目及其所有子项目将被删除且无法恢复，请在下方输入项目路径 <code>{0}</code> 以确认删除。");
		m.put("Example", "示例");
		m.put("Example Plugin Setting", "示例插件设置");
		m.put("Example Property", "示例属性");
		m.put("Exclude Param Combos", "排除参数组合");
		m.put("Exclude States", "排除状态");
		m.put("Excluded", "排除");
		m.put("Excluded Fields", "排除字段");
		m.put("Executable", "可执行文件");
		m.put("Execute Commands", "执行命令");
		m.put("Execute Commands via SSH", "通过 SSH 执行命令");
		m.put("Exit Impersonation", "退出假装");
		m.put("Exited impersonation", "退出假装");
		m.put("Expand all", "展开所有");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", 
			"需要一个或多个 <tt>&lt;数字&gt;(h|m)</tt>。例如 <tt>1h 1m</tt> 表示 1 小时 1 分钟");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", 
			"需要一个或多个 <tt>&lt;数字&gt;(w|d|h|m)</tt>。例如 <tt>1w 1d 1h 1m</tt> 表示 1 周（{0} 天）、1 天（{1} 小时）、1 小时和 1 分钟");
		m.put("Expiration Date:", "到期日期：");
		m.put("Expire Date", "过期日期");
		m.put("Expired", "已过期");
		m.put("Explicit SSL (StartTLS)", "显式 SSL (StartTLS)");
		m.put("Export", "导出");
		m.put("Export All Queried Issues To...", "导出所有查询的工单到...");
		m.put("Export CSV", "导出 CSV");
		m.put("Export XLSX", "导出 XLSX");
		m.put("Export as OCI layout", "导出为 OCI 布局");
		m.put("Extend Trial Subscription", "延长试用订阅");
		m.put("External Authentication", "外部认证");
		m.put("External Issue Transformers", "外部工单转换");
		m.put("External Participants", "外部参与者");
		m.put("External Password Authenticator", "外部密码认证器");
		m.put("External System", "外部系统");
		m.put("External authenticator settings saved", "外部认证器设置已保存");
		m.put("External participants do not have accounts and involve in the issue via email", "外部参与者没有账户，通过电子邮件参与工单");
		m.put("Extract the package into a folder. <b class=\"text-danger\">Warning:</b> On Mac OS X, do not extract to Mac managed folders such as Downloads, Desktop, Documents; otherwise you may encounter permission issues starting agent", 
			"将包提取到文件夹中。<b class=\"text-danger\">警告：</b> 在 Mac OS X 上，不要提取到 Mac 管理的文件夹，如 Downloads、Desktop、Documents；否则您可能会遇到启动代理的权限问题");
		m.put("FAILED", "失败");
		m.put("Fail Threshold", "失败阈值");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"如果存在或严重性高于指定严重性级别的漏洞，则失败构建");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"如果存在或严重性高于指定严重性级别的漏洞，则失败构建。注意：此选项仅在构建未被其他步骤失败时生效");
		m.put("Failed", "失败");
		m.put("Failed to validate build spec import. Check server log for details", "无法验证构建规范导入。请检查服务器日志");
		m.put("Failed to verify your email address", "未能验证您的邮箱地址");
		m.put("Field Bean", "字段 Bean");
		m.put("Field Instance", "字段实例");
		m.put("Field Name", "字段名称");
		m.put("Field Spec", "字段规范");
		m.put("Field Specs", "字段规范");
		m.put("Field Value", "字段值");
		m.put("Fields", "字段");
		m.put("Fields & Links", "字段和链接");
		m.put("Fields And Links Bean", "字段和链接 Bean");
		m.put("Fields to Change", "要更改的字段");
		m.put("File", "文件");
		m.put("File Changes", "文件变更");
		m.put("File Name", "文件名");
		m.put("File Name Patterns (separated by comma)", "文件名模式（用逗号分隔）");
		m.put("File Path", "文件路径");
		m.put("File Patterns", "文件模式");
		m.put("File Protection", "文件保护");
		m.put("File Protections", "文件保护");
		m.put("File and Symbol Search", "文件和符号搜索");
		m.put("File changes", "文件更改");
		m.put("File is too large to edit here", "文件太大，无法在此编辑");
		m.put("File missing or obsolete", "文件缺失或过时");
		m.put("File name", "文件名");
		m.put("File name patterns such as *.java, *.c", "文件名模式，如 *.java, *.c");
		m.put("Files", "文件");
		m.put("Files to Be Analyzed", "要分析的文件");
		m.put("Filter", "过滤");
		m.put("Filter Issues", "过滤工单");
		m.put("Filter actions", "筛选操作");
		m.put("Filter backlog issues", "过滤待办工单");
		m.put("Filter branches...", "过滤分支...");
		m.put("Filter by name", "按名称过滤");
		m.put("Filter by name or email address", "按名称或电子邮件地址过滤");
		m.put("Filter by name...", "按名称过滤...");
		m.put("Filter by path", "按路径过滤");
		m.put("Filter by test suite", "按测试套件筛选");
		m.put("Filter date range", "筛选日期范围");
		m.put("Filter files...", "过滤文件...");
		m.put("Filter groups...", "过滤组...");
		m.put("Filter issues", "过滤工单");
		m.put("Filter pull requests", "过滤合并请求");
		m.put("Filter roles", "过滤角色");
		m.put("Filter tags...", "过滤标签...");
		m.put("Filter targets", "筛选目标");
		m.put("Filter users", "筛选用户");
		m.put("Filter...", "筛选...");
		m.put("Filters", "过滤器");
		m.put("Find branch", "查找分支");
		m.put("Find or create branch", "查找或创建分支");
		m.put("Find or create tag", "查找或创建标签");
		m.put("Find tag", "查找标签");
		m.put("Fingerprint", "指纹");
		m.put("Finish", "完成");
		m.put("First applicable executor", "第一个适用的执行器");
		m.put("Fix", "修复");
		m.put("Fix Type", "修复类型");
		m.put("Fix Undefined Field Values", "修复未定义的字段值");
		m.put("Fix Undefined Fields", "修复未定义的字段");
		m.put("Fix Undefined States", "修复未定义的状态");
		m.put("Fixed Issues", "修复的工单");
		m.put("Fixed issues since...", "修复工单自...");
		m.put("Fixing Builds", "修复构建");
		m.put("Fixing Commits", "修复提交");
		m.put("Fixing...", "修复中...");
		m.put("Float", "浮点数");
		m.put("Follow below instructions to publish packages into this project", "按照以下说明将包发布到此项目");
		m.put("Follow below steps to install agent on remote machine (supports Linux/Windows/Mac OS X/FreeBSD):", 
			"按照以下步骤在远程机器上安装代理（支持 Linux/Windows/Mac OS X/FreeBSD）：");
		m.put("For CI/CD job, add this gem to Gemfile like below", "对于 CI/CD 任务，将此 gem 添加到 Gemfile 中，如下所示");
		m.put("For CI/CD job, add this package to requirements.txt and run below to install the package via command step", 
			"对于 CI/CD 任务，将此包添加到 requirements.txt 中，并运行以下命令通过命令步骤安装包");
		m.put("For CI/CD job, run below to add package repository via command step", "对于 CI/CD 任务，运行以下命令通过命令步骤添加包仓库");
		m.put("For CI/CD job, run below to add package source via command step", "对于 CI/CD 任务，运行以下命令通过命令步骤添加包源");
		m.put("For CI/CD job, run below to add source via command step", "对于 CI/CD 任务，运行以下命令通过命令步骤添加源");
		m.put("For CI/CD job, run below to install chart via command step", "对于 CI/CD 任务，运行以下命令通过命令步骤安装 Chart");
		m.put("For CI/CD job, run below to publish package via command step", "对于 CI/CD 任务，运行以下命令通过命令步骤发布包");
		m.put("For CI/CD job, run below to push chart to the repository via command step", "对于 CI/CD 任务，运行以下命令通过命令步骤推送 Chart 到仓库");
		m.put("For CI/CD job, run below via a command step", "对于 CI/CD 任务，运行以下命令通过命令步骤");
		m.put("For a particular project, the first matching entry will be used", "对于特定项目，第一个匹配的条目将被使用");
		m.put("For all issues", "适用所有工单");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"对于从默认分支无法到达的构建提交，应该指定一个具有创建分支权限的访问令牌作为任务密钥");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"对于从默认分支无法到达的构建提交，应该指定一个具有创建标签权限的访问令牌作为任务密钥");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"对于从默认分支无法到达的构建提交，应该指定一个具有管理工单权限的访问令牌作为任务密钥");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"对于 docker 相关执行器，此路径在容器内，并接受绝对路径和相对路径（相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>）。对于直接在主机机器上运行的 shell 相关执行器，只接受相对路径");
		m.put("For each build, OneDev calculates a list of fixed issues since previous build automatically. This setting provides a default query to further filter/order this list. For a given job, the first matching entry will be used.", 
			"对于每个构建，OneDev 自动计算自上一个构建以来的固定工单列表。此设置提供默认查询以进一步过滤/排序此列表。对于给定的任务，第一个匹配的条目将被使用。");
		m.put("For each selected branch/tag, a separate build will be generated with branch/tag set to corresponding value", 
			"对于每个选定的分支/标签，将生成一个单独的构建，分支/标签设置为相应的值");
		m.put("For issues matching: ", "适用工单：");
		m.put("For very large git repository, you may need to tune options here to reduce memory usage", 
			"对于非常大的 git 仓库，您可能需要调整选项以减少内存使用");
		m.put("For web hooks defined here and in parent projects, OneDev will post event data in JSON format to specified URLs when subscribed events happen", 
			"对于在此处和父项目中定义的 Web hooks，当订阅的事件发生时，OneDev 将以 JSON 格式将事件数据发送到指定的 URL");
		m.put("Force", "强制");
		m.put("Force Garbage Collection", "强制垃圾回收");
		m.put("Forgot Password?", "忘记密码？");
		m.put("Forgotten Password?", "忘记密码？");
		m.put("Fork Project", "分叉项目");
		m.put("Fork now", "立即分叉");
		m.put("Forks Of", "分叉");
		m.put("Frequencies", "频率");
		m.put("From Directory", "从目录");
		m.put("From States", "从状态");
		m.put("From extracted folder, run <code>bin\\agent.bat console</code> as administrator on Windows or <code>bin/agent.sh console</code> on other OS", 
			"从提取的文件夹中，在 Windows 上以管理员身份运行 <code>bin\\agent.bat console</code>，在其他操作系统上运行 <code>bin/agent.sh console</code>");
		m.put("From {0}", "从 {0}");
		m.put("Full Name", "全名");
		m.put("Furthest due date", "最晚截止日期");
		m.put("GPG Keys", "GPG 密钥");
		m.put("GPG Public Key", "GPG 公钥");
		m.put("GPG Signing Key", "GPG 签名密钥");
		m.put("GPG Trusted Keys", "GPG 受信任密钥");
		m.put("GPG key deleted", "GPG 密钥已删除");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "GPG 公钥以 '-----BEGIN PGP PUBLIC KEY BLOCK-----' 开头，以 '-----END PGP PUBLIC KEY BLOCK-----' 结尾");
		m.put("GPG signing key will be used to sign commits generated by OneDev, including pull request merge commits, user commits created via web UI or RESTful api.", 
			"GPG 签名密钥将用于签署 OneDev 生成的提交，包括合并请求合并提交、通过 Web UI 或 RESTful API 创建的用户提交。");
		m.put("Gem Info", "Gem 信息");
		m.put("General", "常规");
		m.put("General Settings", "常规设置");
		m.put("General settings updated", "常规设置已更新");
		m.put("Generate", "生成");
		m.put("Generate File Checksum", "生成文件校验和");
		m.put("Generate New", "新建");
		m.put("Generic LDAP", "通用 LDAP");
		m.put("Get", "获取");
		m.put("Get Groups Using Attribute", "使用属性获取组");
		m.put("Git", "Git");
		m.put("Git Command Line", "Git 命令行");
		m.put("Git Credential", "Git 凭证");
		m.put("Git LFS Storage", "Git LFS 存储");
		m.put("Git Lfs Lock", "Git LFS 锁定");
		m.put("Git Location", "Git 位置");
		m.put("Git Pack Config", "Git 打包配置");
		m.put("Git Path", "Git 路径");
		m.put("Git email address will be used as git author/committer for commits created on web UI", 
			"Git 电子邮件地址将用于在 Web UI 上创建的提交的作者/提交者");
		m.put("Git pack config updated", "Git 包配置已更新");
		m.put("GitHub", "GitHub");
		m.put("GitHub API URL", "GitHub API URL");
		m.put("GitHub Issue Label", "GitHub 工单标签");
		m.put("GitHub Organization", "GitHub 组织");
		m.put("GitHub Personal Access Token", "GitHub 个人访问令牌");
		m.put("GitHub Repositories to Import", "GitHub 要导入的仓库");
		m.put("GitHub Repository", "GitHub 仓库");
		m.put("GitHub personal access token should be generated with scope <b>repo</b> and <b>read:org</b>", 
			"GitHub 个人访问令牌应该使用 <b>repo</b> 和 <b>read:org</b> 范围生成");
		m.put("GitLab API URL", "GitLab API URL");
		m.put("GitLab Group", "GitLab 组");
		m.put("GitLab Issue Label", "GitLab 工单标签");
		m.put("GitLab Personal Access Token", "GitLab 个人访问令牌");
		m.put("GitLab Project", "GitLab 项目");
		m.put("GitLab Projects to Import", "GitLab 要导入的项目");
		m.put("GitLab personal access token should be generated with scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by user of specified access token will be listed", 
			"GitLab 个人访问令牌应该使用 <b>read_api</b>、<b>read_user</b> 和 <b>read_repository</b> 范围生成。注意：只有指定访问令牌的用户拥有的组/项目才会被列出");
		m.put("Gitea API URL", "Gitea API URL");
		m.put("Gitea Issue Label", "Gitea 工单标签");
		m.put("Gitea Organization", "Gitea 组织");
		m.put("Gitea Personal Access Token", "Gitea 个人访问令牌");
		m.put("Gitea Repositories to Import", "Gitea 要导入的仓库");
		m.put("Gitea Repository", "Gitea 仓库");
		m.put("Github Access Token Secret", "GitHub 访问令牌密钥");
		m.put("Global", "全局");
		m.put("Global Build Setting", "全局构建设置");
		m.put("Global Issue Setting", "全局工单设置");
		m.put("Global Pack Setting", "全局打包设置");
		m.put("Global Views", "全局视图");
		m.put("Gmail", "Gmail");
		m.put("Go Back", "返回");
		m.put("Google Test Report", "Google 测试报告");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Gpg 密钥");
		m.put("Great, your mail service configuration is working", "太好了，您的邮件服务配置正常工作");
		m.put("Groovy Script", "Groovy 脚本");
		m.put("Groovy Scripts", "Groovy 脚本");
		m.put("Groovy script to be evaluated. It should return a <i>Date</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个 <i>Date</i> 值。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. It should return a <i>Float</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个 <i>Float</i> 值。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. It should return a <i>Integer</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个 <i>Integer</i> 值。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. It should return a <i>String</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个 <i>String</i> 值。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. It should return a <i>boolean</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个 <i>boolean</i> 值。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. It should return a <i>string</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个 <i>string</i> 值。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. It should return name of a group. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个组名。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. It should return string or list of string. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个字符串或字符串列表。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个组 facade 对象列表，用于作为选择。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. The return value should be a list of user login names to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个用户登录名列表，用于作为选择。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy script to be evaluated. The return value should be a value to color map, for instance:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> if the value does not have a color. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy 脚本应该返回一个值到颜色的映射，例如：<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>，如果值没有颜色，请使用 <tt>null</tt>。请参阅 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> 了解更多详情");
		m.put("Groovy scripts", "Groovy 脚本");
		m.put("Group", "组");
		m.put("Group \"{0}\" deleted", "组 \"{0}\" 已删除");
		m.put("Group Authorization Bean", "组授权 Bean");
		m.put("Group Authorizations", "组授权");
		m.put("Group Authorizations Bean", "组授权 Bean");
		m.put("Group By", "按组");
		m.put("Group Management", "组管理");
		m.put("Group Name Attribute", "组名属性");
		m.put("Group Retrieval", "组查询");
		m.put("Group Search Base", "组搜索根节点");
		m.put("Group Search Filter", "组搜索过滤器");
		m.put("Group authorizations updated", "组授权已更新");
		m.put("Group created", "组已创建");
		m.put("Groups", "组");
		m.put("Groups Claim", "组声明");
		m.put("Guide Line", "引导线");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "HTTP(S) 克隆 URL");
		m.put("Has Owner Permissions", "有所有者权限");
		m.put("Has Running Builds", "有运行中的构建");
		m.put("Heap Memory Usage", "堆内存使用情况");
		m.put("Helm(s)", "Helm");
		m.put("Help", "帮助");
		m.put("Hide", "隐藏");
		m.put("Hide Archived", "隐藏已归档");
		m.put("Hide comment", "隐藏评论");
		m.put("Hide saved queries", "隐藏保存的查询");
		m.put("High", "高");
		m.put("High Availability & Scalability", "高可用和可扩展");
		m.put("High Severity", "严重");
		m.put("History", "历史");
		m.put("History of comparing revisions is unrelated", "比较的版本历史不相关");
		m.put("History of target branch and source branch is unrelated", "目标分支和源分支的历史记录不相关");
		m.put("Host name or ip address of remote machine to run commands via SSH", "远程机器的名称或 IP 地址，用于通过 SSH 运行命令");
		m.put("Hours Per Day", "每天小时数");
		m.put("How to Publish", "如何发布");
		m.put("Html Report", "HTML 报告");
		m.put("Http Method", "Http方法");
		m.put("I didn't eat it. I swear!", "我没吃它。我发誓！");
		m.put("ID token was expired", "ID 令牌已过期");
		m.put("IMAP Host", "IMAP 主机");
		m.put("IMAP Password", "IMAP 密码");
		m.put("IMAP User", "IMAP 用户");
		m.put("IMPORTANT:", "重要：");
		m.put("IP Address", "IP 地址");
		m.put("Id", "ID");
		m.put("Identify Field", "区分字段");
		m.put("If enabled, scheduled backup will run on lead server which is <span wicket:id=\"leadServer\"></span> currently", 
			"如果启用，计划备份将在当前为 <span wicket:id=\"leadServer\"></span> 的主服务器上运行");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"如果启用，在用户有权限的情况下，源分支将在合并合并请求后自动删除");
		m.put("If specified, OneDev will only display iterations with this prefix", "如果指定，OneDev 将只显示具有此前缀的迭代");
		m.put("If specified, all public and internal projects imported from GitLab will use these as default roles. Private projects are not affected", 
			"如果指定，从 GitLab 导入的所有公共和内部项目将使用这些作为默认角色。私人项目不受影响");
		m.put("If specified, all public repositories imported from GitHub will use these as default roles. Private repositories are not affected", 
			"如果指定，从 GitHub 导入的所有公共仓库将使用这些作为默认角色。私人仓库不受影响");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"如果指定，工单的预计/已用时间也将包括此类型的链接工单");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"如果启用此选项，git lfs 命令需要安装在 OneDev 服务器上（即使此步骤在其他节点上运行）");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"如果勾选，此组将能够在启用了工时跟踪的情况下编辑相应工单的预计工时");
		m.put("Ignore", "忽略");
		m.put("Ignore File", "忽略文件");
		m.put("Ignore activities irrelevant to me", "忽略与我无关的活动");
		m.put("Ignore all", "忽略所有");
		m.put("Ignore all whitespace", "忽略所有空白");
		m.put("Ignore change", "忽略更改");
		m.put("Ignore change whitespace", "忽略空白变化");
		m.put("Ignore leading", "忽略前缀");
		m.put("Ignore leading whitespace", "忽略开始的空白");
		m.put("Ignore this field", "忽略此字段");
		m.put("Ignore this param", "忽略此参数");
		m.put("Ignore trailing", "忽略后缀");
		m.put("Ignore trailing whitespace", "忽略结束的空白");
		m.put("Ignored Licenses", "忽略的许可证");
		m.put("Image", "容器镜像");
		m.put("Image Labels", "镜像标签");
		m.put("Image Manifest", "镜像 Manifest");
		m.put("Image Size", "镜像大小");
		m.put("Image Text", "图片文本");
		m.put("Image URL", "图片 URL");
		m.put("Image URL should be specified", "图片 URL 应该指定");
		m.put("Imap Ssl Setting", "IMAP SSL 设置");
		m.put("Imap With Ssl", "IMAP 使用 SSL");
		m.put("Impersonate", "模拟");
		m.put("Implicit SSL", "隐式 SSL");
		m.put("Import", "导入");
		m.put("Import All Projects", "导入所有项目");
		m.put("Import All Repositories", "导入所有仓库");
		m.put("Import Group", "导入组");
		m.put("Import Issues", "导入工单");
		m.put("Import Option", "导入选项");
		m.put("Import Organization", "导入组织");
		m.put("Import Project", "导入项目");
		m.put("Import Projects", "导入项目");
		m.put("Import Repositories", "导入仓库");
		m.put("Import Repository", "导入仓库");
		m.put("Import Server", "导入服务器");
		m.put("Import Workspace", "导入工作区");
		m.put("Import build spec elements (jobs, services, step templates and properties) from other projects. Imported elements are treated as if they are defined locally. Locally defined elements will override imported elements with same name", 
			"从其他项目导入构建规范元素（任务、服务、步骤模板和属性）。导入的元素被视为本地定义的元素。本地定义的元素将覆盖具有相同名称的导入元素");
		m.put("Importing Issues from {0}", "从 {0} 导入工单");
		m.put("Importing from {0}", "从 {0} 导入");
		m.put("Importing issues into currrent project. Please note that issue numbers will only be retained if the whole project fork graph does not have any issues to avoid duplicate issue numbers", 
			"正在将工单导入当前项目。请注意，如果整个项目分叉图没有工单，则工单号将仅保留以避免重复的工单号");
		m.put("Importing projects from {0}", "从 {0} 导入项目");
		m.put("Imports", "导入");
		m.put("In Projects", "在项目中");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"如果 IMAP 主机证书是自签名的或其 CA 根证书不被接受，您可以告诉 OneDev 绕过证书检查。<b class='text-danger'>警告：</b> 在不受信任的网络中，这可能会导致中间人攻击，您应该<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>将证书导入 OneDev</a> 而不是");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"如果 SMTP 主机证书是自签名的或其 CA 根证书不被接受，您可以告诉 OneDev 绕过证书检查。<b class='text-danger'>警告：</b> 在不受信任的网络中，这可能会导致中间人攻击，您应该<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>将证书导入 OneDev</a> 而不是");
		m.put("In case anonymous access is disabled or anonymous user does not have enough permission for a resource operation, you will need to authenticate by providing user name and password (or access token) via http basic auth header", 
			"如果匿名访问被禁用或匿名用户没有足够的权限进行资源操作，您需要通过http基本认证头提供用户名和密码（或访问令牌）进行认证");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"如果缓存未通过上述密钥命中，OneDev 将按顺序遍历此处定义的加载密钥，直到在项目层次结构中找到匹配的缓存。如果多个缓存匹配，将返回最新的缓存。");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"如果需要上传缓存，此属性指定上传的目标项目。留空表示当前项目");
		m.put("In case the pull request status is out of sync with underlying repository, you may synchronize them manually here", 
			"如果合并请求状态与底层仓库不同步，您可以在此手动同步");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"如果用户组在组侧维护，此属性指定组搜索的基础节点。例如：<i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"如果用户组在组侧维护，此过滤器用于确定当前用户的所属组。例如：<i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>。在此示例中，<i>{0}</i> 表示当前用户的 DN");
		m.put("In case you are using external issue tracker, you can define transformers to transform external issue references into external issue links in various places, such as commit messages and pull request descriptions", 
			"如果您使用外部工单跟踪器，您可以定义转换器，将外部工单引用转换为外部工单链接，例如在提交消息和合并请求描述中");
		m.put("In rare cases, your issues might be out of sync with workflow settings (undefined state/field etc.). Run integrity check below to find problems and get them fixed.", 
			"在极少数情况下，您的工单可能与工作流设置不同步（未定义状态/字段等）。运行完整性检查以查找问题并修复它们");
		m.put("Inbox Poll Setting", "Inbox 轮询设置");
		m.put("Include Child Projects", "包含子项目");
		m.put("Include Disabled", "包含禁用");
		m.put("Include Forks", "包含分支");
		m.put("Include When Issue is Opened", "在工单打开时包含");
		m.put("Incompatibilities", "不兼容性");
		m.put("Inconsistent issuer in provider metadata and ID token", "提供者元数据中的发行者与 ID 令牌中的发行者不一致");
		m.put("Indicator", "指示器");
		m.put("Inherit from parent", "从父级继承");
		m.put("Inherited", "继承");
		m.put("Input Spec", "输入规范");
		m.put("Input URL", "输入 URL");
		m.put("Input allowed CORS origin, hit ENTER to add", "输入允许的 CORS 来源，按回车键添加");
		m.put("Input revision", "输入版本");
		m.put("Input title", "输入标题");
		m.put("Input title here", "输入标题");
		m.put("Input user search base. Hit ENTER to add", "输入用户搜索根节点。按回车键添加");
		m.put("Input user search bases. Hit ENTER to add", "输入用户搜索根节点。按回车键添加");
		m.put("Insert", "插入");
		m.put("Insert Image", "插入图片");
		m.put("Insert Link", "插入链接");
		m.put("Insert link to this file", "插入链接到此文件");
		m.put("Insert this image", "插入此图片");
		m.put("Install Subscription Key", "安装订阅密钥");
		m.put("Integer", "整数");
		m.put("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"交互式 Web shell 访问运行中的任务是企业功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a> 30 天");
		m.put("Internal Database", "内部数据库");
		m.put("Interpreter", "解释器");
		m.put("Invalid GPG signature", "无效的 GPG 签名");
		m.put("Invalid PCRE syntax", "无效的 PCRE 语法");
		m.put("Invalid access token: {0}", "无效的访问令牌：{0}");
		m.put("Invalid credentials", "无效的凭据");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "无效的日期范围，期望 \"yyyy-MM-dd to yyyy-MM-dd\"");
		m.put("Invalid email address: {0}", "无效的电子邮件地址: {0}");
		m.put("Invalid invitation code", "无效的邀请代码");
		m.put("Invalid issue date of ID token", "ID 令牌的发行日期无效");
		m.put("Invalid issue number: {0}", "无效的工单编号: {0}");
		m.put("Invalid pull request number: {0}", "无效的合并请求编号：{0}");
		m.put("Invalid request path", "无效的请求路径");
		m.put("Invalid selection, click for details", "无效的选择，点击查看详情");
		m.put("Invalid ssh signature", "无效的 ssh 签名");
		m.put("Invalid state response", "状态响应无效");
		m.put("Invalid state. Please make sure you are visiting OneDev using server url specified in system setting", 
			"无效的状态。请确保您使用系统设置中指定的服务器URL访问OneDev");
		m.put("Invalid subscription key", "无效的订阅密钥");
		m.put("Invalid working period", "无效的工作时间");
		m.put("Invitation sent to \"{0}\"", "邀请已发送至 \"{0}\"");
		m.put("Invitation to \"{0}\" deleted", "邀请已取消: \"{0}\"");
		m.put("Invitations", "邀请");
		m.put("Invitations sent", "邀请已发送");
		m.put("Invite", "邀请");
		m.put("Invite Users", "邀请用户");
		m.put("Is Site Admin", "是否为站点管理员");
		m.put("Issue", "工单");
		m.put("Issue #{0} deleted", "工单 #{0} 已删除");
		m.put("Issue Board", "看板");
		m.put("Issue Boards", "工单看板");
		m.put("Issue Close States", "工单关闭状态");
		m.put("Issue Creation Setting", "工单创建设置");
		m.put("Issue Creation Settings", "工单创建设置");
		m.put("Issue Custom Fields", "自定义工单字段");
		m.put("Issue Description", "工单描述");
		m.put("Issue Description Templates", "工单描述模板");
		m.put("Issue Details", "工单详情");
		m.put("Issue Field", "工单字段");
		m.put("Issue Field Mapping", "工单字段映射");
		m.put("Issue Field Mappings", "工单字段映射");
		m.put("Issue Field Set", "工单字段集");
		m.put("Issue Fields", "工单字段");
		m.put("Issue Filter", "工单过滤器");
		m.put("Issue Import Option", "工单导入选项");
		m.put("Issue Label Mapping", "工单标签映射");
		m.put("Issue Label Mappings", "工单标签映射");
		m.put("Issue Link", "工单链接");
		m.put("Issue Link Mapping", "工单链接映射");
		m.put("Issue Link Mappings", "工单链接映射");
		m.put("Issue Links", "工单链接");
		m.put("Issue Management", "工单管理");
		m.put("Issue Notification", "工单通知");
		m.put("Issue Notification Template", "工单通知模板");
		m.put("Issue Notification Unsubscribed", "工单通知退订");
		m.put("Issue Notification Unsubscribed Template", "工单通知取消订阅模板");
		m.put("Issue Pattern", "工单模式");
		m.put("Issue Priority Mapping", "工单优先级映射");
		m.put("Issue Priority Mappings", "工单优先级映射");
		m.put("Issue Query", "工单查询");
		m.put("Issue Settings", "工单设置");
		m.put("Issue State", "工单状态");
		m.put("Issue State Mapping", "工单状态映射");
		m.put("Issue State Mappings", "工单状态映射");
		m.put("Issue State Transition", "工单状态转换");
		m.put("Issue State Transitions", "工单状态转换");
		m.put("Issue States", "工单状态");
		m.put("Issue Statistics", "工单统计");
		m.put("Issue Stats", "工单统计");
		m.put("Issue Status Mapping", "工单状态映射");
		m.put("Issue Status Mappings", "工单状态映射");
		m.put("Issue Stopwatch Overdue", "工单计时器超时");
		m.put("Issue Stopwatch Overdue Notification Template", "工单计时器超时通知模板");
		m.put("Issue Tag Mapping", "工单标签映射");
		m.put("Issue Tag Mappings", "工单标签映射");
		m.put("Issue Template", "工单模板");
		m.put("Issue Transition ({0} -> {1})", "工单状态转换 ({0} -> {1})");
		m.put("Issue Type Mapping", "工单类型映射");
		m.put("Issue Type Mappings", "工单类型映射");
		m.put("Issue Votes", "工单投票");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"工单管理权限，包括对多个工单的批量操作");
		m.put("Issue count", "工单数量");
		m.put("Issue in state", "工单状态");
		m.put("Issue list", "工单列表");
		m.put("Issue management not enabled in this project", "工单管理未启用此项目");
		m.put("Issue management permission required to move issues", "移动工单需要管理员权限");
		m.put("Issue not exist or access denied", "工单不存在或访问被拒绝");
		m.put("Issue number", "工单编号");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"工单查询订阅仅影响新工单。要批量管理现有工单的订阅状态，请在工单页面中按订阅状态过滤工单，然后采取适当行动");
		m.put("Issue state duration statistics", "工单状态持续时间统计");
		m.put("Issue state frequency statistics", "工单状态频率统计");
		m.put("Issue state trend statistics", "工单状态趋势统计");
		m.put("Issue statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"工单统计是企业版功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a> 30 天");
		m.put("Issue workflow changed, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliation</a> needs to be performed to make data consistent. You may do this after making all necessary changes", 
			"工单工作流已更改，需要执行 <a wicket:id=\"reconcile\" class=\"link-primary\">一致性检查</a> 以使数据一致。您可以在完成所有必要更改后执行此操作");
		m.put("Issues", "工单");
		m.put("Issues can be created in this project by sending email to this address", "通过发送电子邮件到此地址，可以在本项目中创建工单");
		m.put("Issues copied", "工单已复制");
		m.put("Issues moved", "工单已移动");
		m.put("Italic", "斜体");
		m.put("Iteration", "迭代");
		m.put("Iteration \"{0}\" closed", "迭代 \"{0}\" 已关闭");
		m.put("Iteration \"{0}\" deleted", "迭代 \"{0}\" 已删除");
		m.put("Iteration \"{0}\" is closed", "迭代 \"{0}\" 已关闭");
		m.put("Iteration \"{0}\" is reopened", "迭代 \"{0}\" 已重新打开");
		m.put("Iteration \"{0}\" reopened", "迭代 \"{0}\" 已重新打开");
		m.put("Iteration Edit Bean", "迭代编辑Bean");
		m.put("Iteration Name", "迭代名称");
		m.put("Iteration Names", "迭代名称");
		m.put("Iteration Prefix", "迭代前缀");
		m.put("Iteration list", "迭代列表");
		m.put("Iteration saved", "迭代已保存");
		m.put("Iteration spans too long to show burndown chart", "迭代时间跨度太长，无法显示燃尽图");
		m.put("Iteration start and due date should be specified to show burndown chart", "迭代开始和截止日期应指定以显示燃尽图");
		m.put("Iteration start date should be before due date", "迭代开始日期应早于截止日期");
		m.put("Iterations", "迭代");
		m.put("Iterations Bean", "迭代Bean");
		m.put("JIRA Issue Priority", "JIRA 工单优先级");
		m.put("JIRA Issue Status", "JIRA 工单状态");
		m.put("JIRA Issue Type", "JIRA 工单类型");
		m.put("JIRA Project", "JIRA 项目");
		m.put("JIRA Projects to Import", "JIRA 项目导入");
		m.put("JUnit Report", "JUnit 报告");
		m.put("JVM", "JVM");
		m.put("JaCoCo Coverage Report", "JaCoCo 覆盖率报告");
		m.put("Jest Coverage Report", "Jest 覆盖率报告");
		m.put("Jest Test Report", "Jest 测试报告");
		m.put("Job", "任务");
		m.put("Job \"{0}\" associated with the build not found.", "与构建关联的任务 \"{0}\" 未找到。");
		m.put("Job Authorization", "任务授权");
		m.put("Job Cache Management", "任务缓存管理");
		m.put("Job Dependencies", "任务依赖");
		m.put("Job Dependency", "任务依赖");
		m.put("Job Executor", "任务执行器");
		m.put("Job Executor Bean", "任务执行器Bean");
		m.put("Job Executors", "任务执行器");
		m.put("Job Name", "任务名称");
		m.put("Job Names", "任务名称");
		m.put("Job Param", "任务参数");
		m.put("Job Parameters", "任务参数");
		m.put("Job Privilege", "任务权限");
		m.put("Job Privileges", "任务权限");
		m.put("Job Properties", "任务属性");
		m.put("Job Properties Bean", "任务属性Bean");
		m.put("Job Property", "任务属性");
		m.put("Job Secret", "任务密钥");
		m.put("Job Secret Edit Bean", "任务密钥编辑 Bean");
		m.put("Job Secrets", "任务密钥");
		m.put("Job Trigger", "任务触发器");
		m.put("Job Trigger Bean", "任务触发器Bean");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"任务管理权限，包括删除任务的构建。它隐含所有其他任务权限");
		m.put("Job cache \"{0}\" deleted", "任务缓存 \"{0}\" 已删除");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"任务依赖决定不同任务的顺序和并发性。您还可以指定从上游任务获取的制品");
		m.put("Job executor tested successfully", "任务执行器测试成功");
		m.put("Job executors", "任务执行器");
		m.put("Job name", "任务名称");
		m.put("Job properties saved", "任务属性已保存");
		m.put("Job secret \"{0}\" deleted", "任务密钥 \"{0}\" 已删除");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission", 
			"任务密钥 access-token 应该在项目构建设置中定义，作为具有包 ${permission} 权限的访问令牌");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package read permission", 
			"任务密钥 access-token 应该在项目构建设置中定义，作为具有包读权限的访问令牌");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package write permission", 
			"任务密钥 access-token 应该在项目构建设置中定义，作为具有包写权限的访问令牌");
		m.put("Job token", "任务令牌");
		m.put("Job will run on head commit of default branch", "任务将在默认分支的头部提交上运行");
		m.put("Job will run on head commit of target branch", "任务将在目标分支的头部提交上运行");
		m.put("Job will run on merge commit of target branch and source branch", "任务将在目标分支和源分支的合并提交上运行");
		m.put("Job will run on merge commit of target branch and source branch.<br><b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"任务将在目标分支和源分支的合并提交上运行。<br><b class='text-info'>注意：</b>除非受分支保护规则的要求，否则此触发器将忽略包含 <code>[skip ci]</code>、<code>[ci skip]</code>、<code>[no ci]</code>、<code>[skip job]</code>、<code>[job skip]</code> 或 <code>[no job]</code> 的消息的提交");
		m.put("Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"任务将在代码提交时运行。<b class='text-info'>注意：</b>此触发器将忽略包含 <code>[skip ci]</code>、<code>[ci skip]</code>、<code>[no ci]</code>、<code>[skip job]</code>、<code>[job skip]</code> 或 <code>[no job]</code> 的消息的提交");
		m.put("Job workspace", "任务工作区");
		m.put("Jobs", "任务");
		m.put("Jobs marked with <span class=\"text-danger\">*</span> are required to be successful", 
			"标记为 <span class=\"text-danger\">*</span> 的任务需要成功");
		m.put("Jobs required to be successful on merge commit: ", "合并提交时需要成功的任务：");
		m.put("Jobs required to be successful: ", "需要成功的任务：");
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"具有相同顺序组和执行器的任务将按顺序执行。例如，您可以为此属性指定 <tt>@project_path@:prod</tt> 用于执行相同执行器并在当前项目的 prod 环境中部署任务以避免冲突的部署");
		m.put("Key", "键");
		m.put("Key Fingerprint", "密钥指纹");
		m.put("Key ID", "密钥 ID");
		m.put("Key Secret", "密钥密钥");
		m.put("Key Type", "密钥类型");
		m.put("Kubectl Config File", "Kubectl 配置文件");
		m.put("Kubernetes", "Kubernetes");
		m.put("Kubernetes Executor", "Kubernetes 执行器");
		m.put("LDAP URL", "LDAP URL");
		m.put("Label", "标签");
		m.put("Label Management", "标签管理");
		m.put("Label Management Bean", "标签管理Bean");
		m.put("Label Name", "标签名称");
		m.put("Label Spec", "标签规范");
		m.put("Label Value", "标签值");
		m.put("Labels", "标签");
		m.put("Labels Bean", "标签Bean");
		m.put("Labels can be defined in Administration / Label Management", "标签可以在管理 / 标签管理中定义");
		m.put("Labels have been updated", "标签已更新");
		m.put("Language", "语言");
		m.put("Last Accessed", "最后访问");
		m.put("Last Finished of Specified Job", "指定任务的最后完成");
		m.put("Last Modified", "最后修改");
		m.put("Last Published", "最后发布时间");
		m.put("Last Update", "最后更新");
		m.put("Last commit", "最后提交");
		m.put("Last commit hash", "最后提交哈希");
		m.put("Last commit index version", "最后提交索引版本");
		m.put("Leaf Projects", "叶子项目");
		m.put("Least branch coverage", "最小的分支覆盖率");
		m.put("Least line coverage", "最小的行覆盖率");
		m.put("Leave a comment", "留下评论");
		m.put("Leave a note", "留下备注");
		m.put("Left", "左");
		m.put("Less", "更少");
		m.put("License Agreement", "许可协议");
		m.put("License Setting", "许可证设置");
		m.put("Licensed To", "授权给");
		m.put("Licensed To:", "授权给：");
		m.put("Line", "行");
		m.put("Line changes", "行变化");
		m.put("Line: ", "行：");
		m.put("Lines", "行");
		m.put("Link", "链接");
		m.put("Link Existing User", "链接现有用户");
		m.put("Link Spec", "链接规范");
		m.put("Link Spec Opposite", "链接规范相反");
		m.put("Link Text", "链接文本");
		m.put("Link URL", "链接 URL");
		m.put("Link URL should be specified", "链接 URL 应该指定");
		m.put("Link User Bean", "链接用户 Bean");
		m.put("Linkable Issues", "可链接的工单");
		m.put("Linkable Issues On the Other Side", "另一侧可链接的工单");
		m.put("Links", "链接");
		m.put("Links can be used to associate different issues. For instance, an issue can be linked to sub issues or related issues", 
			"链接可用于关联不同的工单。例如，工单可以关联子工单或相关工单");
		m.put("List", "列表");
		m.put("Literal", "字面量");
		m.put("Literal default value", "字面量默认值");
		m.put("Literal value", "字面量值");
		m.put("Load Keys", "加载密钥");
		m.put("Loading emojis...", "加载表情符号...");
		m.put("Loading...", "加载...");
		m.put("Log", "日志");
		m.put("Log Work", "记录工作");
		m.put("Log not available for offline agent", "离线代理的日志不可用");
		m.put("Log work", "记录工作");
		m.put("Login Name", "登录名称");
		m.put("Login and generate refresh token", "登录并生成刷新令牌");
		m.put("Login name already used by another account", "登录名已被另一个账户使用");
		m.put("Login name or email", "登录名或电子邮件");
		m.put("Login name or email address", "登录名或电子邮件地址");
		m.put("Login to OneDev docker registry", "登录到 OneDev Docker 注册表");
		m.put("Login to comment", "登录以评论");
		m.put("Login to comment on selection", "登录以在选择上添加评论");
		m.put("Login to vote", "登录以投票");
		m.put("Login user needs to have package write permission over the project below", "登录用户需要对以下项目具有包写权限");
		m.put("Login with {0}", "通过 {0} 登录");
		m.put("Logo for Dark Mode", "暗黑模式下的Logo");
		m.put("Logo for Light Mode", "亮色模式下的Logo");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"用于生成访问 Gmail 访问令牌的长寿命刷新令牌。<b class='text-info'>提示：</b>您可以在此字段右侧的按钮生成刷新令牌。请注意，每当 client id、client secret 或帐户名称发生变化时，刷新令牌应重新生成");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"用于生成访问 office 365 邮件服务器访问令牌的长寿命刷新令牌。<b class='text-info'>提示：</b>您可以在此字段右侧的按钮登录到您的 office 365 帐户并生成刷新令牌。请注意，每当 tenant id、client id、client secret 或用户主体名称发生变化时，刷新令牌应重新生成");
		m.put("Longest Duration First", "最长持续时间优先");
		m.put("Looks like a GPG signature but without necessary data", "看起来像 GPG 签名但缺少必要数据");
		m.put("Low", "低");
		m.put("Low Severity", "轻微");
		m.put("MERGED", "已合并");
		m.put("MS Teams Notifications", "MS Teams 通知");
		m.put("Mail", "邮件");
		m.put("Mail Connector", "邮件连接器");
		m.put("Mail Connector Bean", "邮件连接器 Bean");
		m.put("Mail Service", "邮件服务");
		m.put("Mail Service Test", "邮件服务测试");
		m.put("Mail service not configured", "邮件服务未配置");
		m.put("Mail service settings saved", "邮件服务设置已保存");
		m.put("Make sure <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 or higher</a> is installed", 
			"确保 <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 或更高版本</a> 已安装");
		m.put("Make sure current user has permission to run docker containers", "确保当前用户有权运行 Docker 容器");
		m.put("Make sure docker engine is installed and docker command line is available in system path", 
			"确保 Docker 引擎已安装且 docker 命令行在系统路径中可用");
		m.put("Make sure git version 2.11.1 or higher is installed and available in system path", "确保 git 版本 2.11.1 或更高版本已安装且在系统路径中可用");
		m.put("Make sure git-lfs is installed and available in system path if you want to retrieve LFS files", 
			"如果您想获取 LFS 文件，请确保 git-lfs 已安装且在系统路径中可用");
		m.put("Make sure the access token has package read permission over the project", "确保访问令牌对项目具有包读权限");
		m.put("Make sure the access token has package write permission over the project", "确保访问令牌对项目具有包写权限");
		m.put("Make sure the access token has package write permission over the project. Also make sure to run command <code>chmod 0600 $HOME/.gem/credentials</code> after creating the file", 
			"确保访问令牌对项目具有包写权限。另外，确保在创建文件后运行命令 <code>chmod 0600 $HOME/.gem/credentials</code>");
		m.put("Make sure the account has package ${permission} permission over the project", "确保账户对项目具有包 ${permission} 权限");
		m.put("Make sure the account has package read permission over the project", "确保账户对项目具有包读权限");
		m.put("Make sure the user has package write permission over the project", "确保用户对项目具有包写权限");
		m.put("Malformed %sbase query", "解析%s基准查询时出错");
		m.put("Malformed %squery", "解析%s查询时出错");
		m.put("Malformed build spec (import project: {0}, import revision: {1})", "构建规范格式错误（导入项目：{0}，导入版本：{1}）");
		m.put("Malformed email address", "格式错误的电子邮件地址");
		m.put("Malformed filter", "格式错误的过滤器");
		m.put("Malformed name filter", "名称过滤格式错误");
		m.put("Malformed query", "查询语法错误");
		m.put("Malformed ssh signature", "格式错误的 ssh 签名");
		m.put("Malformed test suite filter", "测试套件过滤格式错误");
		m.put("Manage Job", "管理任务");
		m.put("Manager DN", "管理员 DN");
		m.put("Manager Password", "管理员密码");
		m.put("Manifest blob unknown", "Manifest blob 未知");
		m.put("Manifest invalid", "Manifest 无效");
		m.put("Manifest unknown", "Manifest 未知");
		m.put("Many commands print outputs with ANSI colors in TTY mode to help identifying problems easily. However some commands running in this mode may wait for user input to cause build hanging. This can normally be fixed by adding extra options to the command", 
			"许多命令在 TTY 模式下使用 ANSI 颜色打印输出，以帮助轻松识别问题。然而，某些在 TTY 模式下运行的命令可能会等待用户输入，导致构建挂起。这通常可以通过在命令中添加额外选项来修复");
		m.put("Mark a property archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived properties will not be shown by default", 
			"如果当前构建规范不再使用该属性，但仍需要存在以重现旧构建，则将该属性标记为已存档。已存档的属性将不会默认显示");
		m.put("Mark a secret archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived secrets will not be shown by default", 
			"如果当前构建规范不再使用该密钥，但仍需要存在以重现旧构建，则将该密钥标记为已存档。已存档的密钥将不会默认显示");
		m.put("Markdown", "Markdown");
		m.put("Markdown Report", "Markdown 报告");
		m.put("Markdown from file", "Markdown（基于文件）");
		m.put("Maven(s)", "Maven");
		m.put("Max Code Search Entries", "最大代码搜索条目");
		m.put("Max Commit Message Line Length", "最大提交消息行长度");
		m.put("Max Git LFS File Size (MB)", "最大 Git LFS 文件大小 (MB)");
		m.put("Max Retries", "最大重试次数");
		m.put("Max Upload File Size (MB)", "最大上传文件大小 (MB)");
		m.put("Max Value", "最大值");
		m.put("Maximum number of entries to return when search code in repository", "搜索代码库时返回的最大条目数");
		m.put("Maximum of retries before giving up", "放弃前的最大重试次数");
		m.put("May not be empty", "不能为空");
		m.put("Medium", "中等");
		m.put("Medium Severity", "中等");
		m.put("Members", "成员");
		m.put("Memory", "内存");
		m.put("Memory Limit", "内存限制");
		m.put("Memory Request", "内存请求");
		m.put("Mention Someone", "提及某人");
		m.put("Mention someone", "提及某人");
		m.put("Merge", "合并");
		m.put("Merge Strategy", "合并策略");
		m.put("Merge Target Branch into Source Branch", "将目标分支合并到源分支");
		m.put("Merge branch \"{0}\" into branch \"{1}\"", "将分支 \"{0}\" 合并到分支 \"{1}\"");
		m.put("Merge branch \"{0}\" of project \"{1}\" into branch \"{2}\"", "将项目 \"{1}\" 的分支 \"{0}\" 合并到分支 \"{2}\"");
		m.put("Merge preview not calculated yet", "合并预览尚未计算完成");
		m.put("Merged", "合并");
		m.put("Merged pull request \"{0}\" ({1})", "合并合并请求 \"{0}\"（{1}）");
		m.put("Merges pull request", "合并请求");
		m.put("Meta", "元");
		m.put("Meta Info", "元数据");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "最小值");
		m.put("Minimum length of the password", "密码的最小长度");
		m.put("Missing Commit", "缺失提交");
		m.put("Missing Commits", "缺失提交");
		m.put("Month", "月");
		m.put("Months", "月");
		m.put("Months to Display", "显示的月数");
		m.put("More", "更多");
		m.put("More Options", "更多选项");
		m.put("More Settings", "更多设置");
		m.put("More commits", "更多提交");
		m.put("More info", "更多信息");
		m.put("More operations", "更多操作");
		m.put("Most branch coverage", "最小的分支覆盖率");
		m.put("Most line coverage", "最小的行覆盖率");
		m.put("Most probably there are import errors in the <a wicket:id=\"buildSpec\">build spec</a>", 
			"最有可能的是<a wicket:id=\"buildSpec\">构建规范</a>中存在导入错误");
		m.put("Mount Docker Sock", "挂载 Docker Sock");
		m.put("Move All Queried Issues To...", "移动所有查询的工单到...");
		m.put("Move All Queried Projects To...", "移动所有查询的项目到...");
		m.put("Move Selected Issues To...", "移动选定的工单到...");
		m.put("Move Selected Projects To...", "移动选中的项目到...");
		m.put("Multiple Lines", "多行");
		m.put("Multiple On the Other Side", "在另一侧允许链接多个工单");
		m.put("Must not be empty", "不能为空");
		m.put("My Access Tokens", "我的访问令牌");
		m.put("My Basic Settings", "我的基本设置");
		m.put("My Email Addresses", "我的电子邮件地址");
		m.put("My GPG Keys", "我的 GPG 密钥");
		m.put("My Profile", "我的个人资料");
		m.put("My SSH Keys", "我的 SSH 密钥");
		m.put("My SSO Accounts", "我的 SSO 账户");
		m.put("Mypy Report", "Mypy 报告");
		m.put("N/A", "不适用");
		m.put("NPM(s)", "NPM");
		m.put("Name", "名称");
		m.put("Name Of Empty Value", "空值的名称");
		m.put("Name On the Other Side", "另一侧的名称");
		m.put("Name Prefix", "名称前缀");
		m.put("Name already used by another access token of the owner", "此名称已被其他访问令牌的所有者使用");
		m.put("Name already used by another link", "名称已被另一个链接使用");
		m.put("Name and name on the other side should be different", "名称和另一侧的名称应该不同");
		m.put("Name containing spaces or starting with dash needs to be quoted", "名称包含空格或以破折号开头需要用引号包围");
		m.put("Name invalid", "名称无效");
		m.put("Name of the link", "链接的名称");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"另一侧的链接名称。例如，如果名称是 <tt>sub issues</tt>，另一侧的名称可以是 <tt>parent issue</tt>");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"提供方的名称将服务于两个目的：<ul><li>显示在登录按钮上<li>形成授权回调 URL，格式为 <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>");
		m.put("Name reversely", "名称反向");
		m.put("Name unknown", "名称未知");
		m.put("Name your file", "命名您的文件");
		m.put("Named Agent Queries Bean", "命名代理查询Bean");
		m.put("Named Agent Query", "命名代理查询");
		m.put("Named Build Queries Bean", "命名构建查询Bean");
		m.put("Named Build Query", "命名构建查询");
		m.put("Named Code Comment Queries Bean", "命名代码评论查询Bean");
		m.put("Named Code Comment Query", "命名代码评论查询");
		m.put("Named Commit Queries Bean", "命名提交查询Bean");
		m.put("Named Commit Query", "命名提交查询");
		m.put("Named Element", "命名元素");
		m.put("Named Issue Queries Bean", "命名工单查询Bean");
		m.put("Named Issue Query", "命名工单查询");
		m.put("Named Pack Queries Bean", "命名包查询Bean");
		m.put("Named Pack Query", "命名包查询");
		m.put("Named Project Queries Bean", "命名项目查询Bean");
		m.put("Named Project Query", "命名项目查询");
		m.put("Named Pull Request Queries Bean", "命名合并请求查询Bean");
		m.put("Named Pull Request Query", "命名合并请求查询");
		m.put("Named Query", "命名查询");
		m.put("Network Options", "网络选项");
		m.put("Never", "从不");
		m.put("Never expire", "永不过期");
		m.put("New Board", "新建看板");
		m.put("New Invitation Bean", "新邀请Bean");
		m.put("New Issue", "新工单");
		m.put("New Password", "新密码");
		m.put("New State", "新状态");
		m.put("New User Bean", "新用户Bean");
		m.put("New Value", "新值");
		m.put("New issue board created", "新建工单看板");
		m.put("New project created", "新项目已创建");
		m.put("New user created", "新用户创建成功");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"新版本可用。红色表示安全/关键更新，黄色表示错误修复，蓝色表示功能更新。点击查看更改。可在系统设置中禁用");
		m.put("Next", "下一步");
		m.put("Next commit", "下一个提交");
		m.put("Next {0}", "后一个{0}");
		m.put("No", "否");
		m.put("No Activity Days", "无活动天数");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"没有配置 SSH 密钥。您可以 <a wicket:id=\"sshKeys\" class=\"link-primary\">添加一个密钥</a> 或切换到 <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> 克隆 URL");
		m.put("No SSL", "无SSL");
		m.put("No accessible reports", "无访问报告");
		m.put("No activity for some time", "无活动一段时间");
		m.put("No agents to pause", "没有要暂停的任务代理");
		m.put("No agents to remove", "没有要删除的任务代理");
		m.put("No agents to restart", "没有要重启的任务代理");
		m.put("No agents to resume", "没有要恢复的任务代理");
		m.put("No aggregation", "无聚合");
		m.put("No any", "无任何");
		m.put("No any matches", "没有任何匹配项");
		m.put("No applicable transitions or no permission to transit", "没有适用的转换或没有权限转换");
		m.put("No attributes defined (can only be edited when agent is online)", "没有定义属性（仅当代理在线时可编辑）");
		m.put("No audits", "无审计记录");
		m.put("No authorized job secret found (project: {0}, job secret: {1})", "未找到授权的任务密钥（项目：{0}，任务密钥：{1}）");
		m.put("No branch to cherry-pick to", "没有分支可以 cherry-pick");
		m.put("No branch to revert on", "没有分支可以 revert");
		m.put("No branches Found", "未找到分支");
		m.put("No branches found", "未找到分支");
		m.put("No build in query context", "在查询上下文中没有构建");
		m.put("No builds", "没有构建");
		m.put("No builds to cancel", "没有构建可以取消");
		m.put("No builds to delete", "没有构建可以删除");
		m.put("No builds to re-run", "没有构建可以重新运行");
		m.put("No comment", "无评论");
		m.put("No comments to delete", "没有要删除的评论");
		m.put("No comments to set as read", "没有要设置为已读的评论");
		m.put("No comments to set resolved", "没有评论可以设置为已解决");
		m.put("No comments to set unresolved", "没有评论可以设置为未解决");
		m.put("No commit in query context", "在查询上下文中没有提交");
		m.put("No config file", "无配置文件");
		m.put("No current build in query context", "在查询上下文中没有当前构建");
		m.put("No current commit in query context", "在查询上下文中没有当前提交");
		m.put("No current pull request in query context", "在查询上下文中没有当前合并请求");
		m.put("No data", "没有数据");
		m.put("No default branch", "没有默认分支");
		m.put("No default group", "无默认组");
		m.put("No default roles", "没有默认角色");
		m.put("No default value", "无默认值");
		m.put("No description", "无描述");
		m.put("No diffs", "没有差异");
		m.put("No diffs to navigate", "没有差异可导航");
		m.put("No directories to skip", "无要跳过的目录");
		m.put("No disallowed file types", "没有不允许的文件类型");
		m.put("No executors defined. Jobs will use auto-discovered executors instead", "未定义执行器。作业将使用自动发现的执行器");
		m.put("No external password authenticator", "没有外部密码认证器");
		m.put("No external password authenticator to authenticate user \"{0}\"", "没有外部密码认证器来认证用户 \"{0}\"");
		m.put("No fields to prompt", "无要提示的字段");
		m.put("No fields to remove", "无要删除的字段");
		m.put("No file attachments", "没有文件附件");
		m.put("No group by", "无分组");
		m.put("No groups claim returned", "没有返回组声明");
		m.put("No groups to remove from", "没有可移除的组");
		m.put("No ignore file", "无忽略文件");
		m.put("No ignored licenses", "无忽略的许可证");
		m.put("No image attachments", "没有图片附件");
		m.put("No imports defined", "无导入");
		m.put("No issue boards defined", "未定义工单看板");
		m.put("No issues in iteration", "迭代中没有工单");
		m.put("No issues to copy", "没有工单可复制");
		m.put("No issues to delete", "没有工单可删除");
		m.put("No issues to edit", "没有工单可编辑");
		m.put("No issues to export", "没有工单可导出");
		m.put("No issues to move", "没有工单可移动");
		m.put("No issues to set as read", "没有工单可设置为已读");
		m.put("No issues to sync estimated/spent time", "没有工单可同步估计/已用时间");
		m.put("No issues to watch/unwatch", "没有工单可关注/取消关注");
		m.put("No jobs defined", "未定义任务");
		m.put("No jobs found", "未找到作业");
		m.put("No limit", "无限制");
		m.put("No mail service", "无邮件服务");
		m.put("No obvious changes", "无明显改动");
		m.put("No one", "无任何人");
		m.put("No packages to delete", "没有可删除的包");
		m.put("No parent", "无父级");
		m.put("No previous successful build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a> to calculate fixed issues since", 
			"还没有<a href=\"https://docs.onedev.io/concepts#构建流\" class=\"link-primary\" target=\"_blank\">相同流上的</a>成功构建，所以无法计算修复的工单");
		m.put("No projects found", "没有找到项目");
		m.put("No projects to delete", "没有要删除的项目");
		m.put("No projects to modify", "没有要修改的项目");
		m.put("No projects to move", "没有要移动的项目");
		m.put("No properties defined", "未定义属性");
		m.put("No proxy", "无代理");
		m.put("No pull request in query context", "在查询上下文中没有合并请求");
		m.put("No pull requests to delete", "没有要删除的合并请求");
		m.put("No pull requests to discard", "没有要放弃的合并请求");
		m.put("No pull requests to set as read", "没有要设置为已读的合并请求");
		m.put("No pull requests to watch/unwatch", "没有要监视/取消监视的合并请求");
		m.put("No refs to build on behalf of", "没有要构建的引用");
		m.put("No required services", "无所需服务");
		m.put("No response body", "无响应体");
		m.put("No secret config", "无秘密配置");
		m.put("No services defined", "未定义服务");
		m.put("No start/due date", "无开始/截止日期");
		m.put("No step templates defined", "未定义步骤模板");
		m.put("No suggestions", "没有建议");
		m.put("No tags found", "未找到标签");
		m.put("No timesheets defined", "未定义时间表");
		m.put("No user found with login name or email: ", "未找到登录名或邮箱为的用户：");
		m.put("No users to convert to service accounts", "没有用户可转换为服务账户");
		m.put("No users to delete", "没有要删除的用户");
		m.put("No users to disable", "没有要禁用的用户");
		m.put("No users to enable", "没有要启用的用户");
		m.put("No users to remove from group", "没有用户可从组中移除");
		m.put("No valid query to show progress", "没有有效的查询来显示进度");
		m.put("No valid signature for head commit", "头部提交没有有效的签名");
		m.put("No valid signature for head commit of target branch", "目标分支的最新提交没有有效签名");
		m.put("No value", "未指定值");
		m.put("No verified primary email address", "没有验证的主电子邮件地址");
		m.put("Node Selector", "节点选择器");
		m.put("Node Selector Entry", "节点选择器条目");
		m.put("None", "无");
		m.put("Not Active Since", "不活跃起");
		m.put("Not Used Since", "未使用时间");
		m.put("Not a verified email of signing GPG key", "不是签署 GPG 密钥的已验证电子邮件");
		m.put("Not a verified email of signing ssh key owner", "不是签署 ssh 密钥所有者的验证邮箱");
		m.put("Not allowed file type: {0}", "不允许的文件类型：{0}");
		m.put("Not assigned", "未分配");
		m.put("Not authorized to create project under \"{0}\"", "无权限在 \"{0}\" 下创建项目");
		m.put("Not authorized to create root project", "无权限创建根项目");
		m.put("Not authorized to move project under this parent", "无权将项目移动到此父项目下");
		m.put("Not authorized to set as root project", "无权设置为根项目");
		m.put("Not covered", "未覆盖");
		m.put("Not covered by any test", "未被任何测试覆盖");
		m.put("Not displaying any fields", "不显示任何字段");
		m.put("Not displaying any links", "不显示任何链接");
		m.put("Not passed", "未通过");
		m.put("Not rendered in failsafe mode", "在故障安全模式下未渲染");
		m.put("Not run", "未运行");
		m.put("Not specified", "未指定");
		m.put("Note", "备注");
		m.put("Nothing to preview", "没有预览内容");
		m.put("Notification", "通知");
		m.put("Notifications", "通知");
		m.put("Notify Build Events", "通知构建事件");
		m.put("Notify Code Comment Events", "通知代码评论事件");
		m.put("Notify Code Push Events", "通知代码推送事件");
		m.put("Notify Issue Events", "通知工单事件");
		m.put("Notify Own Events", "通知自己");
		m.put("Notify Pull Request Events", "通知合并请求");
		m.put("Notify Users", "通知用户");
		m.put("Ntfy.sh Notifications", "Ntfy.sh 通知");
		m.put("NuGet(s)", "NuGet");
		m.put("NuSpec", "NuSpec");
		m.put("Number of CPU Cores", "CPU 核心数");
		m.put("Number of SSH Keys", "SSH 密钥数量");
		m.put("Number of builds to preserve", "保留的构建数量");
		m.put("Number of project replicas, including primary and backups", "项目副本数量，包括主节点和备份节点");
		m.put("Number of recent months to show statistics for", "显示统计信息的最近几个月");
		m.put("OAuth2 Client information | CLIENT ID", "OAuth2 客户端信息 | CLIENT ID");
		m.put("OAuth2 Client information | CLIENT SECRET", "OAuth2 客户端信息 | CLIENT SECRET");
		m.put("OCI Layout Directory", "OCI 布局目录");
		m.put("OIDC error: Inconsistent sub in ID token and userinfo", "ID 令牌中的 sub 与用户信息中的 sub 不一致");
		m.put("OOPS! There Is An Error", "哎呀！发生错误");
		m.put("OPEN", "打开");
		m.put("OS", "操作系统");
		m.put("OS Arch", "操作系统架构");
		m.put("OS User Name", "操作系统用户名");
		m.put("OS Version", "操作系统版本");
		m.put("OS/ARCH", "操作系统/架构");
		m.put("Offline", "离线");
		m.put("Ok", "确定");
		m.put("Old Name", "旧名称");
		m.put("Old Password", "旧密码");
		m.put("On Behalf Of", "发起");
		m.put("On Branches", "在分支上");
		m.put("OneDev Issue Field", "OneDev 工单字段");
		m.put("OneDev Issue Link", "OneDev 工单链接");
		m.put("OneDev Issue State", "OneDev 工单状态");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"OneDev 分析仓库文件以进行代码搜索、行统计和代码贡献统计。此设置告诉要分析哪些文件，并期望以空格分隔的 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径模式</a>。可以通过在前面添加 '-' 来排除某个模式，例如 <code>-**/vendors/**</code> 将排除所有包含 vendors 的路径。<b>注意：</b> 更改此设置仅影响新提交。要将更改应用到历史提交，请停止服务器并删除 <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>项目存储目录</a> 下的 <code>index</code> 和 <code>info/commit</code> 文件夹。当服务器启动时，仓库将重新分析");
		m.put("OneDev configures git hooks to communicate with itself via curl", "OneDev 配置 git 钩子以通过 curl 与自身通信");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"OneDev 需要搜索和确定用户 DN，以及在启用组获取时搜索用户组信息。如果这些操作需要认证，请勾选此选项并指定 'manager' DN 和密码");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"OneDev 需要 git 命令行来管理仓库。最低要求版本为 2.11.1。另外，如果要在构建任务中获取 LFS 文件，请确保安装了 git-lfs");
		m.put("Online", "在线");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", 
			"仅当目标分支不能 fast-forward 到源分支时才创建合并提交");
		m.put("Only projects manageable by access token owner can be authorized", "只有受访问令牌所有者管理的项目才能被授权");
		m.put("Only system level audit events are displayed here. To view audit events for a specific project, please visit the project audit log page", 
			"这里只显示系统级别的审计事件。要查看特定项目的审计事件，请访问项目审计日志页面");
		m.put("Only users able to authenticate via password can be linked", "只有能够通过密码认证的用户才能被链接");
		m.put("Open", "打开");
		m.put("Open new pull request", "创建新的合并请求");
		m.put("Open terminal of current running step", "打开当前运行步骤的终端");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"OpenID 客户端标识将在注册此 OneDev 实例作为客户端应用程序时由您的 OpenID 提供方分配");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"OpenID 客户端密钥将在注册此 OneDev 实例作为客户端应用程序时由您的 OpenID 提供方生成");
		m.put("OpenSSH Public Key", "OpenSSH 公钥");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"OpenSSH 公钥以 'ssh-rsa'、'ecdsa-sha2-nistp256'、'ecdsa-sha2-nistp384'、'ecdsa-sha2-nistp521'、'ssh-ed25519'、'sk-ecdsa-sha2-nistp256@openssh.com' 或 'sk-ssh-ed25519@openssh.com' 开头");
		m.put("Opened issue \"{0}\" ({1})", "创建工单 \"{0}\"（{1}）");
		m.put("Opened pull request \"{0}\" ({1})", "创建合并请求 \"{0}\"（{1}）");
		m.put("Operation", "操作");
		m.put("Operation Failed", "操作失败");
		m.put("Operation Successful", "操作成功");
		m.put("Operations", "操作");
		m.put("Optional", "可选");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"可选择指定要在其中创建工单的项目。留空则在当前项目中创建");
		m.put("Optionally add new users to specified default group", "可选择将新用户添加到指定的默认组");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"如果未查询到用户组信息，可选择将新认证的用户添加到指定组");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"如果未获取用户组信息，可选择将新认证的用户添加到指定组");
		m.put("Optionally choose required builds. You may also input jobs not listed here, and press ENTER to add them", 
			"可选：选择所需验证的任务。您也可以在此处输入未列出的任务，并按回车添加它们");
		m.put("Optionally configure proxy to access remote repository. Proxy should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;", 
			"可选择为访问远程仓库配置代理。代理应为 <i>&lt;代理主机&gt;:&lt;代理端口&gt;</i> 格式");
		m.put("Optionally define a unique key for the project with two or more upper case letters. This key can be used to reference issues, builds, and pull requests with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of <code>&lt;project path&gt;#&lt;number&gt;</code>", 
			"可选择为项目定义一个唯一的键，该键由两个或更多大写字母组成。此键可用于引用工单、构建和合并请求，使用稳定的短格式 <code>&lt;project key&gt;-&lt;number&gt;</code> 代替 <code>&lt;project path&gt;#&lt;number&gt;</code>");
		m.put("Optionally define parameter specifications of the job", "可选择为任务定义参数");
		m.put("Optionally define parameter specifications of the step template", "可选择为步骤模板定义参数");
		m.put("Optionally describe the group", "可选择描述组");
		m.put("Optionally describes the custom field. Html tags are accepted", "可选择描述自定义字段。接受 HTML 标签");
		m.put("Optionally describes the param. Html tags are accepted.", "可选择描述参数。接受 HTML 标签");
		m.put("Optionally filter builds", "可选择过滤构建");
		m.put("Optionally filter issues", "可选择过滤工单");
		m.put("Optionally filter pull requests", "可选择过滤合并请求");
		m.put("Optionally leave a note", "可选择留一个备注");
		m.put("Optionally mount directories or files under job workspace into container", "可选择将任务工作区下的目录或文件挂载到容器中");
		m.put("Optionally select fields to prompt when this button is pressed", "可选择在点击此按钮时提示字段");
		m.put("Optionally select fields to remove when this transition happens", "可选择在转换发生时删除字段");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"可选择指定用户 LDAP 条目中属性的名称，其值将被用作用户电子邮件。此字段通常设置为 <i>mail</i> 根据 RFC 2798");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"可选择指定用户 LDAP 条目中属性的名称，其值将被用作用户全名。此字段通常设置为 <i>displayName</i> 根据 RFC 2798。如果留空，用户的全名将不会被获取");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"可选择指定 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 用作 GitHub 访问令牌。这用于获取托管在 GitHub 上的依赖项的发布说明，并且经过身份验证的访问将获得更高的速率限制");
		m.put("Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko", 
			"可选择为 kaniko 指定 <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>附加选项</a>");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane", 
			"可选择为 crane 指定 <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>附加选项</a>");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane", 
			"可选择为 crane 指定 <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>附加选项</a>");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to build for platform of the node running the job", 
			"可选择指定 <span class='text-info'>逗号分隔</span> 的平台，例如 <tt>linux/amd64,linux/arm64</tt>。留空则构建为运行任务的节点的平台");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to scan, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to scan all platforms in OCI layout", 
			"可选择指定 <span class='text-info'>逗号分隔</span> 的平台，例如 <tt>linux/amd64,linux/arm64</tt>。留空则扫描 OCI 布局中的所有平台");
		m.put("Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use file <tt>Dockerfile</tt> under build path specified above", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 的 Dockerfile。留空则使用上面指定的构建路径下的文件 <tt>Dockerfile</tt>");
		m.put("Optionally specify JavaScript config to be used by Renovate CLI", "可选择指定用于 Renovate CLI 的 JavaScript 配置");
		m.put("Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. Leave empty to derive from server url", 
			"可选择指定 SSH 根 URL，用于通过 SSH 协议构造项目克隆 URL。留空则从服务器 URL 推导");
		m.put("Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of the text input", 
			"可选择指定一个 <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>正则表达式模式</a> 用于验证文本输入的有效值");
		m.put("Optionally specify a OneDev project to be used as parent of imported projects. Leave empty to import as root projects", 
			"可选择指定一个 OneDev 项目作为导入项目的父项目。留空则作为根项目导入");
		m.put("Optionally specify a OneDev project to be used as parent of imported repositories. Leave empty to import as root projects", 
			"可选择指定一个 OneDev 项目作为导入仓库的父项目。留空则作为根项目导入");
		m.put("Optionally specify a base query for the list", "可选择指定列表的基础查询");
		m.put("Optionally specify a base query to filter/order issues in backlog. Backlog issues are those not associating with current iteration", 
			"可选择指定基础查询以过滤/排序待办事项列表中的工单。待办事项是那些不与当前迭代关联的工单");
		m.put("Optionally specify a base query to filter/order issues of the board", "可选择指定基础查询以过滤/排序看板中的工单");
		m.put("Optionally specify a cron expression to schedule database auto-backup. The cron expression format is <em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>.For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz tutorial</a>.The backup files will be placed into <em>db-backup</em> folder under OneDev installation directory. In case multiple servers connect to form a cluster, auto-backup takes place on the <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>lead server</a>. Leave this property empty if you do not want to enable database auto backup.", 
			"可选择指定一个 cron 表达式来调度数据库自动备份。cron 表达式格式为 <em>&lt;秒&gt; &lt;分钟&gt; &lt;小时&gt; &lt;日期&gt; &lt;月份&gt; &lt;星期&gt;</em>。例如，<em>0 0 1 * * ?</em> 表示每天凌晨 1 点。有关格式的详细信息，请参阅 <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz 教程</a>。备份文件将放置在 OneDev 安装目录下的 <em>db-backup</em> 文件夹中。如果多个服务器连接形成集群，自动备份将在 <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>主服务器</a> 上进行。如果不想启用数据库自动备份，请将此属性留空。");
		m.put("Optionally specify a date field to hold due date information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"可选择指定一个日期字段来保存截止日期信息。<br><b>注意：</b>如果这里没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to put retrieved artifacts. Leave empty to use job workspace itself", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的路径来放置获取的制品。留空则使用任务工作区本身");
		m.put("Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. <b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, as the volume is only used to hold temporary build files", 
			"可选择指定一个存储类来动态分配构建卷。留空则使用默认存储类。<b class='text-warning'>注意：</b>存储类的回收策略应设置为 <code>Delete</code>，因为该卷仅用于保存临时构建文件");
		m.put("Optionally specify a working period field to hold estimated time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"可选择指定一个工作周期字段来保存估计时间信息。<br><b>注意：</b>如果这里没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Optionally specify a working period field to hold spent time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"可选择指定一个工作周期字段来保存已用时间信息。<br><b>注意：</b>如果这里没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Optionally specify a working period field to hold time estimate infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"可选择指定一个工作周期字段来保存时间估计信息。<br><b>注意：</b>如果这里没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Optionally specify a working period field to hold time spent infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"可选择指定一个工作周期字段来保存已用时间信息。<br><b>注意：</b>如果这里没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Optionally specify additional options for buildx build command", "可选择为 buildx build 命令指定其他选项");
		m.put("Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, the response header <code>Access-Control-Allow-Origin</code> will be set to the same value", 
			"可选择指定允许的 <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> 来源。对于 CORS 简单请求或预检请求，如果请求头 <code>Origin</code> 的值包含在此处，响应头 <code>Access-Control-Allow-Origin</code> 将设置为相同的值");
		m.put("Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match", 
			"可选择指定允许自行注册用户的电子邮件域名。使用 '*' 或 '?' 进行模式匹配");
		m.put("Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). Leave empty to all types", 
			"可选择指定适用于提交消息页脚检查的提交类型（按 ENTER 添加值）。留空则适用于所有类型");
		m.put("Optionally specify applicable jobs of this executor", "可选地指定此执行器适用的作业");
		m.put("Optionally specify applicable users who pushed the change", "可选地指定推送更改的适用用户");
		m.put("Optionally specify arguments to run above image", "可选择指定运行上述镜像的参数");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"可选择指定从依赖项拷贝到 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的制品。只能拷贝已发布的制品（通过制品发布步骤）。留空则不拷贝任何制品");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"可选择指定允许按此按钮的授权角色。如果未指定，则允许所有用户");
		m.put("Optionally specify base query of the list", "可选择指定列表的基本查询");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"可选择指定允许访问此密钥的分支/用户/组。如果留空，任何任务都可以访问此密钥，包括通过外部合并请求触发的任务");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 的构建上下文路径。留空则使用任务工作区本身。除非使用选项 <code>--dockerfile</code> 指定不同位置，否则文件 <code>Dockerfile</code> 应存在于构建上下文目录中");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 的构建路径。留空则使用任务工作区本身");
		m.put("Optionally specify cluster role the job pods service account binding to. This is necessary if you want to do things such as running other Kubernetes pods in job command", 
			"可选择指定任务 pod 服务账户绑定的集群角色。如果要执行诸如在任务命令中运行其他 Kubernetes pod 之类的操作，这是必需的");
		m.put("Optionally specify comma separated licenses to be ignored", "可选择指定要忽略的以逗号分隔的许可证");
		m.put("Optionally specify container arguments separated by space. Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse this with container options which should be specified in executor setting", 
			"可选择指定以空格分隔的容器参数。包含空格的单个参数应加引号。<b class='text-warning'>注意：</b>不要与应在执行器设置中指定的容器选项混淆");
		m.put("Optionally specify cpu limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"可选择为使用此执行器的每个任务/服务指定 CPU 限制。详情请查看 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes 资源管理</a>");
		m.put("Optionally specify cpu limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> of relevant containers", 
			"可选择为使用此执行器的每个任务/服务指定 CPU 限制。这将用作相关容器的选项 <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a>");
		m.put("Optionally specify criteria of issues which can be linked", "可选择指定可以链接的工单的条件");
		m.put("Optionally specify criteria of issues which can be linked on the other side", "可选择指定可以在另一端链接的工单的条件");
		m.put("Optionally specify custom fields allowed to edit when open new issues", "可选择指定在打开新工单时允许编辑的自定义字段");
		m.put("Optionally specify depth for a shallow clone in order to speed up source retrieval", 
			"可选择指定浅克隆的深度，以加快源代码获取速度");
		m.put("Optionally specify description of the issue", "可选择指定工单的描述");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"可选择指定要跳过的扫描路径内的目录或 glob 模式。多个跳过项应以空格分隔");
		m.put("Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types", 
			"可选择通过扩展名指定不允许的文件类型（按 ENTER 键添加值），例如 <code>exe</code>, <code>bin</code>。留空以允许所有文件类型");
		m.put("Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. Leave empty to use docker executable in PATH", 
			"可选择指定 docker 可执行文件，例如 <i>/usr/local/bin/docker</i>。留空则使用 PATH 中的 docker 可执行文件");
		m.put("Optionally specify docker options to create network. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"可选择指定创建网络的 docker 选项。多个选项应以空格分隔，包含空格的单个选项应加引号");
		m.put("Optionally specify docker options to run container. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"可选择指定运行容器的 docker 选项。多个选项应以空格分隔，包含空格的单个选项应加引号");
		m.put("Optionally specify docker sock to use. Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows", 
			"可选择指定要使用的 docker sock。在 Linux 上默认为 <i>/var/run/docker.sock</i>，在 Windows 上默认为 <i>//./pipe/docker_engine</i>");
		m.put("Optionally specify environment variables for the container", "可选择为容器指定环境变量");
		m.put("Optionally specify environment variables for this step", "可选择为此步骤指定环境变量");
		m.put("Optionally specify environment variables of the service", "可选择指定服务的环境变量");
		m.put("Optionally specify estimated time.", "可选择指定估计时间。");
		m.put("Optionally specify executor for this job. Leave empty to use auto-discover executor", 
			"可选地为此作业指定执行器。留空以使用自动发现的执行器");
		m.put("Optionally specify executor for this job. Leave empty to use first applicable executor", 
			"可选地为此作业指定执行器。留空以使用第一个适用的执行器");
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"可选择指定相对于缓存路径的文件，在检测缓存更改时忽略。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。多个文件应以空格分隔，包含空格的单个文件应加引号");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"如果要获取用户的组成员资格信息，可选择指定组搜索根节点。例如：<i>cn=Users, dc=example, dc=com</i>。要给 Active Directory 组适当的权限，应定义一个同名的 OneDev 组。留空则在 OneDev 端管理组成员资格");
		m.put("Optionally specify issue links allowed to edit", "可选择指定允许编辑的工单链接");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "可选择指定适用于此模板的工单。留空则适用于所有工单");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"可选择指定适用于此转换的工单。留空则适用于所有工单");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"可选择指定适用于此转换的工单。留空则适用于所有工单。");
		m.put("Optionally specify jobs allowed to use this script", "可选择指定允许使用此脚本的任务");
		m.put("Optionally specify memory limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"可选择为使用此执行器的每个任务/服务指定内存限制。详情请查看 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes 资源管理</a>");
		m.put("Optionally specify memory limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> of relevant containers", 
			"可选择为使用此执行器的每个任务/服务指定内存限制。这将用作相关容器的选项 <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a>");
		m.put("Optionally specify merge strategy of created pull request. Leave empty to use default strategy of each project", 
			"可选择指定创建的合并请求的合并策略。留空则使用每个项目的默认策略");
		m.put("Optionally specify message of the tag", "可选择指定标签的消息");
		m.put("Optionally specify name of the attribute inside the user LDAP entry whose values will be taken as user SSH keys. SSH keys will be managed by LDAP only if this field is set", 
			"可选择指定用户 LDAP 条目中属性的名称，其值将被用作用户 SSH 密钥。只有设置了此字段，SSH 密钥才会由 LDAP 管理");
		m.put("Optionally specify node selector of the job pods", "可选择指定任务 pod 的节点选择器");
		m.put("Optionally specify options for docker builder prune command", "可选择为 docker builder prune 命令指定选项");
		m.put("Optionally specify options for scp command. Multiple options need to be separated with space", 
			"可选择为 scp 命令指定选项。多个选项需要用空格分隔");
		m.put("Optionally specify options for ssh command. Multiple options need to be separated with space", 
			"可选择为 ssh 命令指定选项。多个选项需要用空格分隔");
		m.put("Optionally specify options passed to renovate cli. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"可选择指定传递给 renovate cli 的选项。多个选项应以空格分隔，包含空格的单个选项应加引号");
		m.put("Optionally specify osv scanner <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. You may ignore particular vulnerabilities via this file", 
			"可选择在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 下指定 osv 扫描器 <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>配置文件</a>。您可以通过此文件忽略特定漏洞");
		m.put("Optionally specify path protection rules", "可选择指定路径保护规则");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的路径，用作 trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>忽略文件</a>");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的路径，用作 trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>密钥配置</a>");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的路径来发布制品。留空则使用任务工作区本身");
		m.put("Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. Leave empty to pull all platforms in image", 
			"可选择指定要拉取的平台，例如 <tt>linux/amd64</tt>。留空则拉取镜像中的所有平台");
		m.put("Optionally specify project to show builds of. Leave empty to show builds of all projects with permissions", 
			"可选择指定要显示构建的项目。留空则显示所有有权限的项目的构建");
		m.put("Optionally specify project to show issues of. Leave empty to show issues of all accessible projects", 
			"可选择指定要显示工单的项目。留空则显示所有可访问项目的工单");
		m.put("Optionally specify project to show packages of. Leave empty to show packages of all projects with permissions", 
			"可选择指定要显示包的项目。留空则显示所有有权限的项目的包");
		m.put("Optionally specify ref of above job, for instance <i>refs/heads/main</i>. Use * for wildcard match", 
			"可选择指定上述任务的引用，例如 <i>refs/heads/main</i>。使用 * 进行通配符匹配");
		m.put("Optionally specify registry logins to override those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token secret for password secret", 
			"可选择指定注册表登录信息，以覆盖任务执行器中定义的登录信息。对于内置注册表，使用 <code>@server_url@</code> 作为注册表 URL，<code>@job_token@</code> 作为用户名，以及访问令牌密钥作为密码密钥");
		m.put("Optionally specify relative directory to put uploaded files", "可选地指定要上传的文件的相对目录");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to clone code into. Leave empty to use job workspace itself", 
			"可选择指定 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 下的相对路径，用于克隆代码。留空则使用任务工作区本身");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"可选择指定 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 下的相对路径进行扫描。留空则使用任务工作区本身");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"可选择指定 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 下的相对路径，用于扫描依赖项漏洞。可以指定多个路径，应以空格分隔。留空则使用任务工作区本身");
		m.put("Optionally specify required reviewers for changes of specified branch", "可选择为指定分支的更改指定所需的审阅者");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"可选择指定用于创建分支的修订版本。留空则从构建提交创建");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"可选择指定单独的目录来存储构建制品。非绝对目录被视为相对于站点目录");
		m.put("Optionally specify separate directory to store git lfs files. Non-absolute directory is considered to be relative to site directory", 
			"可选择指定单独的目录来存储 git lfs 文件。非绝对目录被视为相对于站点目录");
		m.put("Optionally specify separate directory to store package files. Non-absolute directory is considered to be relative to site directory", 
			"可选择指定单独的目录来存储包文件。非绝对目录被视为相对于站点目录");
		m.put("Optionally specify services required by this job. <b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors (server docker executor, remote docker executor, or kubernetes executor)", 
			"可选择指定此任务所需的服务。<b class='text-warning'>注意：</b> 服务仅由支持 docker 的执行器（服务器 docker 执行器、远程 docker 执行器或 kubernetes 执行器）支持");
		m.put("Optionally specify space-separated branches applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"可选择指定适用于此转换的以空格分隔的分支。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则匹配所有");
		m.put("Optionally specify space-separated branches applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for default branch", 
			"可选择指定适用于此触发器的以空格分隔的分支。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则为默认分支");
		m.put("Optionally specify space-separated branches to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"可选择指定以空格分隔的要检查的分支。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则匹配所有分支");
		m.put("Optionally specify space-separated commit messages applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"可选地指定适用于此转换的以空格分隔的提交信息。使用 '*' 或 '?' 进行通配符匹配。以 '-' 为前缀排除。留空以匹配所有");
		m.put("Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all files", 
			"可选择指定以空格分隔的要检查的文件。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则匹配所有文件");
		m.put("Optionally specify space-separated jobs applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"可选择指定适用于此转换的以空格分隔的任务。使用 '*' 或 '?' 进行通配符匹配。前缀 '-' 表示排除。留空则匹配所有");
		m.put("Optionally specify space-separated projects applicable for this trigger. This is useful for instance when you want to prevent the job from being triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all projects", 
			"可选择指定适用于此触发器的以空格分隔的项目。例如，当您想要防止任务在分叉项目中被触发时，这非常有用。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则匹配所有项目");
		m.put("Optionally specify space-separated projects to search in. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to search in all projects with code read permission", 
			"可选择指定要搜索的以空格分隔的项目。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则搜索所有具有代码读取权限的项目");
		m.put("Optionally specify space-separated reports. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"可选择指定以空格分隔的报告。使用 '*' 或 '?' 进行通配符匹配。前缀 '-' 表示排除");
		m.put("Optionally specify space-separated service images applicable for this locator. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"可选择指定适用于此定位器的以空格分隔的服务镜像。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则匹配所有");
		m.put("Optionally specify space-separated service names applicable for this locator. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"可选择指定适用于此定位器的以空格分隔的服务名称。使用 '*' 或 '?' 进行通配符匹配。前缀 '-' 表示排除。留空则匹配所有");
		m.put("Optionally specify space-separated tags to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all tags", 
			"可选择指定要检查的以空格分隔的标签。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则匹配所有标签");
		m.put("Optionally specify space-separated target branches of the pull requests to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"可选择指定要检查的以空格分隔的合并请求的目标分支。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空则匹配所有分支");
		m.put("Optionally specify the OpenID claim to retrieve groups of authenticated user. Depending on the provider, you may need to request additional scopes above to make this claim available", 
			"可选择指定要获取已认证用户的组。根据提供方，您可能需要请求额外的范围以使此声明可用");
		m.put("Optionally specify the maximum value allowed.", "可选择指定允许的最大值");
		m.put("Optionally specify the minimum value allowed.", "可选择指定允许的最小值");
		m.put("Optionally specify the project to publish site files to. Leave empty to publish to current project", 
			"可选择指定要发布站点文件的项目。留空则发布到当前项目");
		m.put("Optionally specify uid:gid to run container as. <b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or using user namespace remapping", 
			"可选择指定要作为容器运行的 uid:gid。<b class='text-warning'>注意：</b>如果容器运行时是 rootless 或使用用户命名空间重新映射，则应留空此设置");
		m.put("Optionally specify user name to access remote repository", "可选择指定用于访问远程仓库的用户名");
		m.put("Optionally specify valid scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope", 
			"可选择指定有效的常规提交范围（按 ENTER 添加值）。留空则允许任意范围");
		m.put("Optionally specify valid types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type", 
			"可选择指定有效的常规提交类型（按 ENTER 添加值）。留空则允许任意类型");
		m.put("Optionally specify value of git config <code>pack.packSizeLimit</code> for the repository", 
			"可选择指定用于仓库的 git config <code>pack.packSizeLimit</code> 的值");
		m.put("Optionally specify value of git config <code>pack.threads</code> for the repository", 
			"可选择指定用于仓库的 git config <code>pack.threads</code> 的值");
		m.put("Optionally specify value of git config <code>pack.window</code> for the repository", 
			"可选择指定用于仓库的 git config <code>pack.window</code> 的值");
		m.put("Optionally specify value of git config <code>pack.windowMemory</code> for the repository", 
			"可选择指定用于仓库的 git config <code>pack.windowMemory</code> 的值");
		m.put("Optionally specify where to run service pods specified in job. The first matching locator will be used. If no any locators are found, node selector of the executor will be used", 
			"可选择指定要在其中运行任务中指定的服务 pod 的位置。第一个匹配的定位器将被使用。如果没有找到任何定位器，则将使用执行器的节点选择器");
		m.put("Optionally specify working directory of the container. Leave empty to use default working directory of the container", 
			"可选择指定容器的默认工作目录。留空则使用容器的默认工作目录");
		m.put("Options", "选项");
		m.put("Or manually enter the secret key below in your authenticator app", "或者在您的身份验证应用中手动输入下面的密钥");
		m.put("Order By", "排序");
		m.put("Order More User Months", "购买更多用户月数");
		m.put("Order Subscription", "购买订阅");
		m.put("Ordered List", "有序列表");
		m.put("Ordered list", "有序列表");
		m.put("Osv License Scanner", "OSV 许可证扫描器");
		m.put("Osv Vulnerability Scanner", "OSV 漏洞扫描器");
		m.put("Other", "其他");
		m.put("Outline", "大纲");
		m.put("Outline Search", "大纲搜索");
		m.put("Output", "输出");
		m.put("Overall", "总体");
		m.put("Overall Estimated Time:", "总体预计时间：");
		m.put("Overall Spent Time:", "总体已用时间：");
		m.put("Overview", "概览");
		m.put("Own:", "自身");
		m.put("Ownered By", "拥有者");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "PEM 私钥以 '-----BEGIN RSA PRIVATE KEY-----' 开头");
		m.put("PENDING", "待定");
		m.put("PMD Report", "PMD 报告");
		m.put("Pack", "包");
		m.put("Pack Notification", "包通知");
		m.put("Pack Size Limit", "包大小限制");
		m.put("Pack Type", "包类型");
		m.put("Package", "包");
		m.put("Package Management", "包管理");
		m.put("Package Notification", "包通知");
		m.put("Package Notification Template", "包通知模板");
		m.put("Package Privilege", "包权限");
		m.put("Package Storage", "包存储");
		m.put("Package list", "包列表");
		m.put("Package {0} deleted", "包 {0} 已删除");
		m.put("Packages", "包");
		m.put("Page Not Found", "页面未找到");
		m.put("Page is in error, reload to recover", "页面发生错误，请刷新页面");
		m.put("Param Instance", "参数实例");
		m.put("Param Instances", "参数实例");
		m.put("Param Map", "参数映射");
		m.put("Param Matrix", "参数矩阵");
		m.put("Param Name", "参数名称");
		m.put("Param Spec", "参数");
		m.put("Param Spec Bean", "参数Bean");
		m.put("Parameter", "参数");
		m.put("Parameter Specs", "参数");
		m.put("Params", "参数");
		m.put("Params & Triggers", "参数与触发器");
		m.put("Params to Display", "显示参数");
		m.put("Parent Bean", "父Bean");
		m.put("Parent OneDev Project", "父OneDev项目");
		m.put("Parent Project", "父项目");
		m.put("Parent project not found", "找不到父项目");
		m.put("Parents", "父项目");
		m.put("Partially covered", "部分覆盖");
		m.put("Partially covered by some tests", "部分被测试覆盖");
		m.put("Passcode", "Passcode");
		m.put("Passed", "通过");
		m.put("Password", "密码");
		m.put("Password Authenticator", "密码认证器");
		m.put("Password Edit Bean", "密码编辑 Bean");
		m.put("Password Must Contain Digit", "密码必须包含数字");
		m.put("Password Must Contain Lowercase", "密码必须包含小写字母");
		m.put("Password Must Contain Special Character", "密码必须包含特殊字符");
		m.put("Password Must Contain Uppercase", "密码必须包含大写字母");
		m.put("Password Policy", "密码策略");
		m.put("Password Reset", "密码重置");
		m.put("Password Reset Bean", "密码重置 Bean");
		m.put("Password Reset Template", "密码重置模板");
		m.put("Password Secret", "密码密钥");
		m.put("Password and its confirmation should be identical.", "密码和其确认应相同。");
		m.put("Password changed. Please login with your new password", "密码已更改。请使用新密码登录");
		m.put("Password has been changed", "密码已更改");
		m.put("Password has been removed", "密码已删除");
		m.put("Password has been set", "密码已设置");
		m.put("Password of the user", "用户的密码");
		m.put("Password or Access Token for Remote Repository", "密码或远程仓库的访问令牌");
		m.put("Password reset request has been sent", "密码重置请求已发送");
		m.put("Password reset url is invalid or obsolete", "密码重置URL无效或已过期");
		m.put("PasswordMinimum Length", "密码最小长度");
		m.put("Paste subscription key here", "在此粘贴订阅密钥");
		m.put("Path containing spaces or starting with dash needs to be quoted", "路径包含空格或以破折号开头时需要用引号括起来");
		m.put("Path placeholder", "路径占位符");
		m.put("Path to kubectl", "kubectl 的路径");
		m.put("Paths", "路径");
		m.put("Pattern", "模式");
		m.put("Pause", "暂停");
		m.put("Pause All Queried Agents", "暂停所有查询的代理");
		m.put("Pause Selected Agents", "暂停选定的代理");
		m.put("Paused", "已暂停");
		m.put("Paused all queried agents", "已暂停所有查询的代理");
		m.put("Paused selected agents", "已暂停选定的代理");
		m.put("Pem Private Key", "PEM 私钥");
		m.put("Pending", "待处理");
		m.put("Performance", "性能");
		m.put("Performance Setting", "性能设置");
		m.put("Performance Settings", "性能设置");
		m.put("Performance settings have been saved", "性能设置已保存");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\"", 
			"执行模糊查询。在搜索文本周围使用 '~' 添加更多条件，例如：~text to search~ 和 \"State\" is \"Open\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"", 
			"执行模糊查询。用'~'包围搜索文本以添加更多条件，例如：~text to search~ 且 \"类型\" 为 \"NPM\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online", 
			"执行模糊查询。在搜索文本周围使用 '~' 添加更多条件，例如：~text to search~ 和 online");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"执行模糊查询。使用 '~' 包围搜索文本以添加更多条件，例如：~text to search~ and open");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and owned by me", 
			"执行模糊查询。用 '~' 包围搜索文本以添加更多条件，例如：~要搜索的文本~ 且 由我拥有");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved", 
			"执行模糊查询。在搜索文本周围使用 '~' 添加更多条件，例如：~text to search~ 和 unresolved");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"执行模糊查询。使用 '~' 包围搜索文本以添加更多条件，例如：~text to search~ author(robin)");
		m.put("Permanent link", "永久链接");
		m.put("Permanent link of this selection", "此选择的永久链接");
		m.put("Permission denied", "权限不足");
		m.put("Permission will be checked upon actual operation", "权限将在实际操作时检查");
		m.put("Physical memory in mega bytes", "物理内存（兆字节）");
		m.put("Pick Existing", "选择现有");
		m.put("Pin this issue", "置顶此工单");
		m.put("Pipeline", "流水线");
		m.put("Placeholder", "占位符");
		m.put("Plain text expected", "需要纯文本");
		m.put("Platform", "平台");
		m.put("Platforms", "平台");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"请<a wicket:id=\"download\" class=\"font-weight-bolder\">下载</a>下方的恢复代码并妥善保管。这些代码可用于在您无法访问认证应用程序时提供一次性账户访问权限。它们<b>不会</b>再次显示");
		m.put("Please Confirm", "请确认");
		m.put("Please Note", "请注意");
		m.put("Please check your email for password reset instructions", "请检查您的邮箱以获取密码重置说明");
		m.put("Please choose revision to create branch from", "请选择要从中创建分支的版本");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "请先配置 <a wicket:id=\"mailSetting\">邮件设置</a>");
		m.put("Please confirm", "请确认");
		m.put("Please confirm the password.", "请确认密码。");
		m.put("Please follow <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">this instruction</a> to resolve the conflicts", 
			"请遵循 <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">此说明</a> 以解决冲突");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"请输入在启用两阶段验证时保存的恢复代码");
		m.put("Please login to perform this operation", "请登录才能执行此操作");
		m.put("Please login to perform this query", "请登录以执行此查询");
		m.put("Please resolve undefined field values below", "请解决以下未定义的字段值");
		m.put("Please resolve undefined fields below", "请解决以下未定义的字段");
		m.put("Please resolve undefined states below. Note that if you select to delete an undefined state, all issues with that state will be deleted", 
			"请解决以下未定义的状态。请注意，如果您选择删除未定义的状态，所有具有该状态的工单都将被删除");
		m.put("Please select agents to pause", "请选择要暂停的代理");
		m.put("Please select agents to remove", "请选择要删除的代理");
		m.put("Please select agents to restart", "请选择要重启的代理");
		m.put("Please select agents to resume", "请选择要恢复的代理");
		m.put("Please select branches to create pull request", "请选择分支创建合并请求");
		m.put("Please select builds to cancel", "请选择要取消的构建");
		m.put("Please select builds to delete", "请选择要删除的构建");
		m.put("Please select builds to re-run", "请选择要重新运行的构建");
		m.put("Please select comments to delete", "请选择要删除的评论");
		m.put("Please select comments to set resolved", "请选择要设置为已解决的评论");
		m.put("Please select comments to set unresolved", "请选择要设置为未解决的评论");
		m.put("Please select different branches", "请选择不同的分支");
		m.put("Please select fields to update", "请选择要更新的字段");
		m.put("Please select groups to remove from", "请选择要移除的组");
		m.put("Please select issues to copy", "请选择要复制的工单");
		m.put("Please select issues to delete", "请选择要删除的工单");
		m.put("Please select issues to edit", "请选择要编辑的工单");
		m.put("Please select issues to move", "请选择要移动的工单");
		m.put("Please select issues to sync estimated/spent time", "请选择要同步估计/已用时间的工单");
		m.put("Please select packages to delete", "请选择要删除的包");
		m.put("Please select projects to delete", "请选择要删除的项目");
		m.put("Please select projects to modify", "请选择要修改的项目");
		m.put("Please select projects to move", "请选择要移动的项目");
		m.put("Please select pull requests to delete", "请选择要删除的合并请求");
		m.put("Please select pull requests to discard", "请选择要放弃的合并请求");
		m.put("Please select pull requests to watch/unwatch", "请选择要监视/取消监视的合并请求");
		m.put("Please select query watches to delete", "请选择要删除的查询订阅");
		m.put("Please select revision to create tag from", "请选择要从中创建标签的版本");
		m.put("Please select revisions to compare", "请选择要比较的版本");
		m.put("Please select users to convert to service accounts", "请选择要转换为服务账户的用户");
		m.put("Please select users to disable", "请选择要禁用的用户");
		m.put("Please select users to enable", "请选择要启用的用户");
		m.put("Please select users to remove from group", "请选择要从组中移除的用户");
		m.put("Please specify file name above before editing content", "请在编辑内容之前指定文件名");
		m.put("Please switch to packages page of a particular project for the instructions", "请切换到特定项目的包页面以查看说明");
		m.put("Please wait...", "请稍候...");
		m.put("Please waiting...", "请稍候...");
		m.put("Plugin metadata not found", "插件元数据未找到");
		m.put("Poll Interval", "轮询间隔");
		m.put("Populate Tag Mappings", "填充标签映射");
		m.put("Port", "端口");
		m.put("Post", "发布");
		m.put("Post Build Action", "构建后操作");
		m.put("Post Build Action Bean", "构建后操作 Bean");
		m.put("Post Build Actions", "构建后操作");
		m.put("Post Url", "发布 URL");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "前缀模式");
		m.put("Prefix the title with <code>WIP</code> or <code>[WIP]</code> to mark the pull request as work in progress", 
			"在标题前加上 <code>WIP</code> 或 <code>[WIP]</code> 以标记拉取请求为进行中的工作");
		m.put("Prepend", "前缀");
		m.put("Preserve Days", "保留天数");
		m.put("Preset Commit Message", "预设提交信息");
		m.put("Preset commit message updated", "预设提交信息已更新");
		m.put("Press 'y' to get permalink", "按 'y' 获取永久链接");
		m.put("Prev", "上一");
		m.put("Prevent Creation", "防止创建");
		m.put("Prevent Deletion", "防止删除");
		m.put("Prevent Forced Push", "防止强制推送");
		m.put("Prevent Update", "防止更新");
		m.put("Preview", "预览");
		m.put("Previous", "上一步");
		m.put("Previous Value", "前值");
		m.put("Previous commit", "上一个提交");
		m.put("Previous {0}", "前一个{0}");
		m.put("Primary", "主要");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "未指定主 <a wicket:id=\"noPrimaryAddressLink\">电子邮件地址</a>");
		m.put("Primary Email", "主电子邮件");
		m.put("Primary email address not specified", "未指定主要邮箱地址");
		m.put("Primary email address of your account is not specified yet", "您的账户尚未指定主要电子邮件地址");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"主电子邮件地址将用于接收通知、显示 Gravatar（如果启用）等。");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"主或别名邮箱地址作为各种电子邮件通知的发件人地址。如果启用了 <code>Check Incoming Email</code> 选项，用户也可以回复此地址通过电子邮件发布问题或合并请求评论");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"登录到 office 365 邮件服务器发送/接收电子邮件的帐户的主名称。确保此帐户 <b>拥有</b> 上述应用程序 ID 指示的注册应用程序");
		m.put("Private Key Secret", "私钥密钥");
		m.put("Private key regenerated and SSH server restarted", "私钥已重新生成并重启 SSH 服务器");
		m.put("Privilege", "权限");
		m.put("Privilege Settings", "权限设置");
		m.put("Product Version", "产品版本");
		m.put("Profile", "个人资料");
		m.put("Programming language", "编程语言");
		m.put("Project", "项目");
		m.put("Project \"{0}\" deleted", "项目 \"{0}\" 已删除");
		m.put("Project Authorization Bean", "项目授权 Bean");
		m.put("Project Authorizations Bean", "项目授权 Bean");
		m.put("Project Build Setting", "项目构建设置");
		m.put("Project Dependencies", "项目依赖");
		m.put("Project Dependency", "项目依赖");
		m.put("Project Id", "项目 ID");
		m.put("Project Import Option", "项目导入选项");
		m.put("Project Issue Setting", "项目问题设置");
		m.put("Project Key", "键");
		m.put("Project Management", "项目管理");
		m.put("Project Pack Setting", "项目打包设置");
		m.put("Project Path", "项目路径");
		m.put("Project Pull Request Setting", "项目合并请求设置");
		m.put("Project Replicas", "项目副本");
		m.put("Project authorizations updated", "项目授权已更新");
		m.put("Project does not have any code yet", "项目还没有代码");
		m.put("Project forked", "项目已分叉");
		m.put("Project id", "项目 ID");
		m.put("Project list", "项目列表");
		m.put("Project manage privilege required to delete \"{0}\"", "删除 \"{0}\" 需要项目管理员权限");
		m.put("Project manage privilege required to modify \"{0}\"", "修改 \"{0}\" 需要项目管理员权限");
		m.put("Project manage privilege required to move \"{0}\"", "移动 \"{0}\" 需要项目管理员权限");
		m.put("Project name", "项目名称");
		m.put("Project not specified yet", "项目未指定");
		m.put("Project or revision not specified yet", "项目或版本未指定");
		m.put("Project overview", "项目概览");
		m.put("Project path", "项目路径");
		m.put("Projects", "项目");
		m.put("Projects Bean", "项目 Bean");
		m.put("Projects deleted", "项目已删除");
		m.put("Projects modified", "项目已修改");
		m.put("Projects moved", "项目已移动");
		m.put("Projects need to be redistributed when cluster members are added/removed. OneDev does not do this automatically as this is resource intensive, and you may only want to do it after cluster is finalized and stable.", 
			"当添加/删除集群成员时，需要重新分配项目。OneDev 不会自动执行此操作，因为这是资源密集型的，您可能只想在集群最终确定并稳定后进行此操作。");
		m.put("Promotions", "晋升");
		m.put("Prompt Fields", "提示字段");
		m.put("Properties", "属性");
		m.put("Provide server id (guild id) to restrict access only to server members", "提供服务器 ID（公会 ID）以仅限制对服务器成员的访问");
		m.put("Proxy", "代理");
		m.put("Prune Builder Cache", "清理构建器缓存");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"此步骤调用 docker builder prune 命令来删除服务器 docker 执行器或远程 docker 执行器中指定的 buildx 构建器缓存");
		m.put("Public", "公开");
		m.put("Public Key", "公钥");
		m.put("Public Roles", "公共角色");
		m.put("Publish", "发布");
		m.put("Publish Coverage Report Step", "发布覆盖率报告步骤");
		m.put("Publish Problem Report Step", "发布问题报告步骤");
		m.put("Publish Report Step", "发布报告步骤");
		m.put("Publish Unit Test Report Step", "发布单元测试报告步骤");
		m.put("Published After", "发布时间晚于");
		m.put("Published At", "发布于");
		m.put("Published Before", "发布时间早于");
		m.put("Published By", "发布者");
		m.put("Published By Project", "发布项目");
		m.put("Published By User", "发布用户");
		m.put("Published File", "已发布文件");
		m.put("Pull Command", "拉取命令");
		m.put("Pull Image", "拉取镜像");
		m.put("Pull Request", "合并请求");
		m.put("Pull Request Branches", "合并请求分支");
		m.put("Pull Request Description", "合并请求描述");
		m.put("Pull Request Filter", "合并请求过滤");
		m.put("Pull Request Management", "合并请求管理");
		m.put("Pull Request Markdown Report", "合并请求 Markdown 报告");
		m.put("Pull Request Notification", "合并请求通知");
		m.put("Pull Request Notification Template", "合并请求通知模板");
		m.put("Pull Request Notification Unsubscribed", "合并请求通知退订");
		m.put("Pull Request Notification Unsubscribed Template", "合并请求通知取消订阅模板");
		m.put("Pull Request Settings", "合并请求设置");
		m.put("Pull Request Statistics", "合并请求统计");
		m.put("Pull Request Title", "合并请求标题");
		m.put("Pull Requests", "合并请求");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"此步骤需要由服务器 docker 执行器、远程 docker 执行器或 Kubernetes 执行器执行");
		m.put("Pull from Remote", "从远程拉取");
		m.put("Pull request", "合并请求");
		m.put("Pull request #{0} already closed", "合并请求 #{0} 已关闭");
		m.put("Pull request #{0} deleted", "合并请求 #{0} 已删除");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"在项目中对多个合并请求进行批量操作的权限");
		m.put("Pull request already closed", "合并请求已关闭");
		m.put("Pull request already opened", "合并请求已打开");
		m.put("Pull request and code review", "合并请求和代码评审");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not finished yet", 
			"合并请求现在无法合并，因为<a class=\"more-info d-inline link-primary\">一些必需的构建</a>尚未完成");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not successful", 
			"合并请求现在无法合并，因为<a class=\"more-info d-inline link-primary\">一些必需的构建</a>不成功");
		m.put("Pull request can not be merged now as it is <a class=\"more-info d-inline link-primary\">pending review</a>", 
			"合并请求现在无法合并，因为它正<a class=\"more-info d-inline link-primary\">等待审查</a>");
		m.put("Pull request can not be merged now as it was <a class=\"more-info d-inline link-primary\">requested for changes</a>", 
			"合并请求现在无法合并，因为它<a class=\"more-info d-inline link-primary\">请求了更改</a>");
		m.put("Pull request can not be merged now as valid signature is required for head commit", 
			"合并请求现在无法合并，因为头部提交需要有效的签名");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "合并请求只能在获得所有审查人员的批准后合并");
		m.put("Pull request can only be merged by users with code write permission", "合并请求只能由具有代码写入权限的用户合并");
		m.put("Pull request discard", "合并请求丢弃");
		m.put("Pull request duration statistics", "合并请求持续时间统计");
		m.put("Pull request frequency statistics", "合并请求频率统计");
		m.put("Pull request is discarded", "合并请求被丢弃");
		m.put("Pull request is in error: {0}", "合并请求处于错误状态: {0}");
		m.put("Pull request is merged", "合并请求被合并");
		m.put("Pull request is opened", "合并请求被打开");
		m.put("Pull request is still a work in progress", "拉取请求仍在进行中");
		m.put("Pull request is work in progress", "拉取请求正在进行中");
		m.put("Pull request list", "合并请求列表");
		m.put("Pull request merge", "合并请求合并");
		m.put("Pull request not exist or access denied", "合并请求不存在或访问被拒绝");
		m.put("Pull request not merged", "合并请求未合并");
		m.put("Pull request number", "合并请求编号");
		m.put("Pull request open or update", "合并请求打开或更新");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"合并请求查询订阅仅影响新合并请求。要批量管理现有合并请求的订阅状态，请在合并请求页面中按订阅状态过滤合并请求，然后采取适当行动");
		m.put("Pull request settings updated", "合并请求设置已更新");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"合并请求统计是企业版功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a> 30 天");
		m.put("Pull request synchronization submitted", "合并请求同步已提交");
		m.put("Pull request will be merged automatically when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"合并请求将在准备好时自动合并。此选项将在添加新提交、更改合并策略或切换目标分支时禁用");
		m.put("Pull request will be merged automatically with a preset <a wicket:id=\"commitMessage\">commit message</a> when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"合并请求将在准备好时自动合并，并使用预设的<a wicket:id=\"commitMessage\">提交消息</a>。此选项将在添加新提交、更改合并策略或切换目标分支时禁用");
		m.put("Push Image", "推送镜像");
		m.put("Push chart to the repository", "推送 Chart 到仓库");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"此步骤需要由服务器 docker 执行器、远程 docker 执行器或 Kubernetes 执行器执行");
		m.put("Push to Remote", "推送至远程");
		m.put("Push to container registry", "推送至容器注册表");
		m.put("PyPI(s)", "PyPI");
		m.put("Pylint Report", "Pylint 报告");
		m.put("Queries", "查询");
		m.put("Query", "查询");
		m.put("Query Parameters", "查询参数");
		m.put("Query Watches", "订阅的查询");
		m.put("Query commits", "查询提交");
		m.put("Query not submitted", "查询未提交");
		m.put("Query param", "查询参数");
		m.put("Query/order agents", "查询/排序代理");
		m.put("Query/order builds", "查询/排序构建");
		m.put("Query/order comments", "查询/排序评论");
		m.put("Query/order issues", "查询/排序工单");
		m.put("Query/order packages", "查询/排序包");
		m.put("Query/order projects", "查询/排序项目");
		m.put("Query/order pull requests", "查询/排序合并请求");
		m.put("Queueing Takes", "排队中");
		m.put("Quick Search", "快速搜索");
		m.put("Quote", "引用");
		m.put("RESTful API", "RESTful API");
		m.put("RESTful API Help", "RESTful API帮助");
		m.put("Ran On Agent", "在代理上运行");
		m.put("Re-run All Queried Builds", "重新运行所有查询的构建");
		m.put("Re-run Selected Builds", "重新运行选定的构建");
		m.put("Re-run request submitted", "重新运行请求已提交");
		m.put("Re-run this build", "重新运行此构建");
		m.put("Read", "读取");
		m.put("Read body", "读取正文");
		m.put("Readiness Check Command", "准备检查命令");
		m.put("Really want to delete this code comment?", "您确定要删除此代码评论吗？");
		m.put("Rebase", "变基");
		m.put("Rebase Source Branch Commits", "重置源分支提交");
		m.put("Rebase all commits from source branch onto target branch", "将源分支的所有提交 Rebase 到目标分支");
		m.put("Rebase source branch commits", "Rebase 源分支上的提交");
		m.put("Rebuild manually", "手动重建");
		m.put("Receive Posted Email", "接收已发布邮件");
		m.put("Received test mail", "已收到测试邮件");
		m.put("Receivers", "接收者");
		m.put("Recovery code", "恢复代码");
		m.put("Recursive", "递归");
		m.put("Redundant", "冗余");
		m.put("Ref", "引用");
		m.put("Ref Name", "引用名称");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup", 
			"参考此<a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>教程</a>获取示例设置");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> for an example setup", 
			"参考此<a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>教程</a>获取示例设置");
		m.put("Reference", "引用");
		m.put("Reference Build", "引用构建");
		m.put("Reference Issue", "引用工单");
		m.put("Reference Pull Request", "引用合并请求");
		m.put("Reference this {0} in markdown or commit message via below string.", "在 markdown 或提交信息中可通过以下字符串来引用该{0}。");
		m.put("Refresh", "刷新");
		m.put("Refresh Token", "刷新令牌");
		m.put("Refs", "引用");
		m.put("Regenerate", "重新生成");
		m.put("Regenerate Private Key", "重新生成私钥");
		m.put("Regenerate this access token", "重新生成此访问令牌");
		m.put("Registry Login", "注册表登录");
		m.put("Registry Logins", "注册表登录");
		m.put("Registry Url", "注册表 URL");
		m.put("Regular Expression", "正则表达式");
		m.put("Remaining User Months", "剩余用户月数");
		m.put("Remaining User Months:", "剩余用户月数：");
		m.put("Remaining time", "剩余时间");
		m.put("Remember Me", "记住我");
		m.put("Remote Docker Executor", "远程 Docker 执行器");
		m.put("Remote Machine", "远程机器");
		m.put("Remote Shell Executor", "远程 Shell 执行器");
		m.put("Remote URL", "远程 URL");
		m.put("Remote Url", "远程 URL");
		m.put("Remove", "删除");
		m.put("Remove All Queried Agents", "删除所有查询的代理");
		m.put("Remove All Queried Users from Group", "从组中移除所有查询的用户");
		m.put("Remove Fields", "删除字段");
		m.put("Remove From Current Iteration", "从当前迭代中删除");
		m.put("Remove Selected Agents", "删除选定的代理");
		m.put("Remove Selected Users from Group", "移除选定用户从组中");
		m.put("Remove from All Queried Groups", "从所有查询的组中移除");
		m.put("Remove from Selected Groups", "从选定的组中移除");
		m.put("Remove from batch", "从批量中删除");
		m.put("Remove issue from this iteration", "从此迭代中移除工单");
		m.put("Remove this assignee", "移除此指派人");
		m.put("Remove this external participant from issue", "从工单中移除此外部参与者");
		m.put("Remove this file", "删除此文件");
		m.put("Remove this image", "删除此图片");
		m.put("Remove this reviewer", "移除此审查者");
		m.put("Removed all queried agents. Type <code>yes</code> below to confirm", "已删除所有查询的代理。在下面输入 <code>yes</code> 确认");
		m.put("Removed selected agents. Type <code>yes</code> below to confirm", "已删除选定的代理。在下面输入 <code>yes</code> 确认");
		m.put("Rename {0}", "重命名 {0}");
		m.put("Renew Subscription", "续订订阅");
		m.put("Renovate CLI Options", "Renovate CLI 选项");
		m.put("Renovate JavaScript Config", "Renovate JavaScript 配置");
		m.put("Reopen", "重新打开");
		m.put("Reopen this iteration", "重新打开此迭代");
		m.put("Reopened pull request \"{0}\" ({1})", "重新打开合并请求 \"{0}\"（{1}）");
		m.put("Replace With", "替换为");
		m.put("Replica Count", "副本数量");
		m.put("Replicas", "副本");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "在项目 \"{1}\" 的文件 \"{0}\" 上回复评论");
		m.put("Reply", "回复");
		m.put("Report Name", "报告名称");
		m.put("Report format changed. You may re-run this build to generate the report in new format", 
			"报告格式已更改。您可以重新运行此构建以生成新格式的报告");
		m.put("Repository Sync", "仓库同步");
		m.put("Request Body", "请求体");
		m.put("Request For Changes", "请求更改");
		m.put("Request Scopes", "请求范围");
		m.put("Request Trial Subscription", "申请试用订阅");
		m.put("Request review", "请求审查");
		m.put("Request to sync", "请求同步");
		m.put("Requested For changes", "请求修改");
		m.put("Requested changes to pull request \"{0}\" ({1})", "请求更改合并请求 \"{0}\"（{1}）");
		m.put("Requested for changes", "请求更改");
		m.put("Requested to sync estimated/spent time", "请求同步估计/已用时间");
		m.put("Require Autentication", "要求认证");
		m.put("Require Strict Pull Request Builds", "要求严格合并请求构建");
		m.put("Require Successful", "要求成功");
		m.put("Required", "必需");
		m.put("Required Builds", "要求构建");
		m.put("Required Reviewers", "要求审查者");
		m.put("Required Services", "所需服务");
		m.put("Resend Verification Email", "重新发送验证电子邮件");
		m.put("Resend invitation", "重新发送邀请");
		m.put("Reset", "重置");
		m.put("Resolution", "解决");
		m.put("Resolved", "已解决");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "将项目 \"{1}\" 的文件 \"{0}\" 上的评论标记为已解决");
		m.put("Resource", "资源");
		m.put("Resource Settings", "资源设置");
		m.put("Resources", "资源");
		m.put("Response", "响应");
		m.put("Response Body", "响应体");
		m.put("Restart", "重启");
		m.put("Restart All Queried Agents", "重启所有查询的代理");
		m.put("Restart Selected Agents", "重启选定的代理");
		m.put("Restart command issued", "重启命令已发出");
		m.put("Restart command issued to all queried agents", "已向所有查询的代理发出重启命令");
		m.put("Restart command issued to selected agents", "已向选定的代理发出重启命令");
		m.put("Restore Source Branch", "恢复源分支");
		m.put("Restored source branch", "已恢复源分支");
		m.put("Resubmitted manually", "手动重新提交");
		m.put("Resume", "恢复");
		m.put("Resume All Queried Agents", "恢复所有查询的代理");
		m.put("Resume Selected Agents", "恢复选定的代理");
		m.put("Resumed all queried agents", "已恢复所有查询的代理");
		m.put("Resumed selected agents", "已恢复选定的代理");
		m.put("Retried At", "重试于");
		m.put("Retrieve Groups", "查询组");
		m.put("Retrieve LFS Files", "获取 LFS 文件");
		m.put("Retrieve Submodules", "获取子模块");
		m.put("Retry Condition", "重试条件");
		m.put("Retry Delay", "重试延迟");
		m.put("Revert", "回滚");
		m.put("Reverted successfully", "revert 成功");
		m.put("Review required for deletion. Submit pull request instead", "需要审查才能删除。请提交合并请求");
		m.put("Review required for this change. Please submit pull request instead", "此更改需要审核。请提交拉取请求。");
		m.put("Review required for this change. Submit pull request instead", "需要审查才能更改。请提交合并请求");
		m.put("Reviewers", "审查者");
		m.put("Revision", "修订");
		m.put("Revision indexing in progress...", "正在索引当前版本...");
		m.put("Revision indexing in progress... (symbol navigation in revisions will be accurate after indexed)", 
			"版本索引中... (当前版本中的符号导航在索引后才会准确)");
		m.put("Right", "右");
		m.put("Role", "角色");
		m.put("Role \"{0}\" deleted", "角色 \"{0}\" 已删除");
		m.put("Role \"{0}\" updated", "角色 \"{0}\" 已更新");
		m.put("Role Management", "角色管理");
		m.put("Role created", "角色已创建");
		m.put("Roles", "角色");
		m.put("Root Projects", "根项目");
		m.put("Roslynator Report", "Roslynator 报告");
		m.put("RubyGems(s)", "RubyGems");
		m.put("Ruff Report", "Ruff 报告");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "规则将在用户操作标签时应用此处指定的条件");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"规则仅在用户更改分支时应用此处指定的条件");
		m.put("Run As", "作为");
		m.put("Run Buildx Image Tools", "运行 Buildx 镜像工具");
		m.put("Run Docker Container", "运行 Docker 容器");
		m.put("Run In Container", "在容器中运行");
		m.put("Run Integrity Check", "运行完整性检查");
		m.put("Run Job", "运行任务");
		m.put("Run Options", "运行选项");
		m.put("Run below commands from within your git repository:", "从您的 git 仓库中运行以下命令：");
		m.put("Run below commands to install this gem", "运行以下命令安装此 Gem");
		m.put("Run below commands to install this package", "运行以下命令安装此包");
		m.put("Run below commands to use this chart", "运行以下命令使用此 Chart");
		m.put("Run below commands to use this package", "运行以下命令使用此包");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"使用指定参数运行 docker buildx imagetools 命令。此步骤只能由服务器 docker 执行器或远程 docker 执行器执行");
		m.put("Run job", "运行任务");
		m.put("Run job in another project", "在另一个项目中运行任务");
		m.put("Run on Bare Metal/Virtual Machine", "在裸机/虚拟机上运行");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"运行 OSV 扫描器扫描各种 <a href='https://deps.dev/' target='_blank'>依赖项</a> 使用的违反许可证。它只能由 dock er执行器执行。");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"运行 OSV 扫描器扫描各种 <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>锁定文件</a> 中的漏洞。它只能由 docker 相关执行器执行。");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"运行指定的 docker 容器。<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> 挂载到容器中，其路径放置在环境变量 <code>ONEDEV_WORKSPACE</code> 中。<b class='text-warning'>注意：</b> 此步骤只能由服务器 docker 执行器或远程 docker 执行器执行");
		m.put("Run specified step template", "运行指定的步骤模板");
		m.put("Run this job", "运行此任务");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by docker aware executor.", 
			"运行 trivy 容器镜像扫描器以查找指定镜像中的问题。对于漏洞，它会检查各种<a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>分发文件</a>。它只能由 docker 相关执行器执行。");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"运行 trivy 文件系统扫描器扫描各种 <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>锁定文件</a>。它只能由 docker 相关执行器执行，建议在 <span class='text-warning'>依赖项解析后</span> 运行（例如 npm install）。与 OSV 扫描器相比，它的设置稍微冗长，但可以提供更准确的结果");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"运行 trivy 根文件系统扫描器扫描各种 <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>分布文件</a>。它只能由 docker 相关执行器执行，建议在项目暂存区域运行。");
		m.put("Run via Docker Container", "通过 Docker 容器运行");
		m.put("Running", "运行中");
		m.put("Running Takes", "运行中");
		m.put("SLOC on {0}", "在 {0} 上的源代码行数");
		m.put("SMTP Host", "SMTP 主机");
		m.put("SMTP Password", "SMTP 密码");
		m.put("SMTP User", "SMTP 用户");
		m.put("SMTP/IMAP", "SMTP/IMAP");
		m.put("SSH", "SSH");
		m.put("SSH & GPG Keys", "SSH 和 GPG 密钥");
		m.put("SSH Clone URL", "SSH 克隆 URL");
		m.put("SSH Keys", "SSH 密钥");
		m.put("SSH Root URL", "SSH 根 URL");
		m.put("SSH Server Key", "SSH 服务器密钥");
		m.put("SSH key deleted", "SSH 密钥已删除");
		m.put("SSH settings have been saved and SSH server restarted", "SSH 设置已保存并重启 SSH 服务器");
		m.put("SSL Setting", "SSL 设置");
		m.put("SSO Accounts", "SSO 账户");
		m.put("SSO Providers", "SSO 提供方");
		m.put("SSO account deleted", "SSO 账户已删除");
		m.put("SSO provider \"{0}\" deleted", "SSO 提供方 \"{0}\" 已删除");
		m.put("SSO provider created", "SSO 提供方已创建");
		m.put("SSO provider updated", "SSO 提供方已更新");
		m.put("SUCCESSFUL", "成功");
		m.put("Save", "保存");
		m.put("Save Query", "保存查询");
		m.put("Save Query Bean", "保存查询 Bean");
		m.put("Save Settings", "保存设置");
		m.put("Save Settings & Redistribute Projects", "保存设置并重新分布项目");
		m.put("Save Template", "保存模板");
		m.put("Save as Mine", "保存为我的");
		m.put("Saved Queries", "保存的查询");
		m.put("Scan Path", "扫描路径");
		m.put("Scan Paths", "扫描路径");
		m.put("Scan below QR code with your TOTP authenticators", "使用您的 TOTP 认证器扫描下方二维码");
		m.put("Schedule Issues", "工单排程");
		m.put("Script Name", "脚本名称");
		m.put("Scripting Value", "脚本值");
		m.put("Search", "搜索");
		m.put("Search For", "搜索");
		m.put("Search Groups Using Filter", "使用过滤器搜索组");
		m.put("Search branch", "搜索分支");
		m.put("Search files, symbols and texts", "搜索文件、符号和文本");
		m.put("Search for", "搜索");
		m.put("Search inside current tree", "在当前树内搜索");
		m.put("Search is too general", "搜索过于笼统");
		m.put("Search job", "搜索任务");
		m.put("Search project", "搜索项目");
		m.put("Secret", "密钥");
		m.put("Secret Config File", "密钥配置文件");
		m.put("Secret Setting", "密钥设置");
		m.put("Security", "安全");
		m.put("Security & Compliance", "安全与合规");
		m.put("Security Setting", "安全设置");
		m.put("Security Settings", "安全设置");
		m.put("Security settings have been updated", "安全设置已更新");
		m.put("Select", "选择");
		m.put("Select Branch to Cherry Pick to", "选择分支进行 cherry-pick");
		m.put("Select Branch to Revert on", "选择分支进行 revert");
		m.put("Select Branch/Tag", "选择分支/标签");
		m.put("Select Existing", "选择已有");
		m.put("Select Job", "选择作业");
		m.put("Select Project", "选择项目");
		m.put("Select below...", "从下面选择...");
		m.put("Select iteration to schedule issues into", "选择迭代调度问题");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"选择要从中导入的组织。留空以从当前帐户下的存储库导入");
		m.put("Select project and revision first", "先选择项目和版本");
		m.put("Select project first", "先选择项目");
		m.put("Select project to import from", "选择要从中导入的项目");
		m.put("Select project to sync to. Leave empty to sync to current project", "选择要同步到的项目。留空以同步到当前项目");
		m.put("Select repository to import from", "选择要从中导入的存储库");
		m.put("Select users to send alert email upon events such as database auto-backup failure, cluster node unreachable etc", 
			"选择在数据库自动备份失败、集群节点不可达等事件时发送警报邮件的用户");
		m.put("Select workspace to import from", "选择要从中导入的工作区");
		m.put("Send Notifications", "发送通知");
		m.put("Send Pull Request", "发送合并请求");
		m.put("Send notification", "发送通知");
		m.put("SendGrid", "SendGrid");
		m.put("Sendgrid Webhook Setting", "SendGrid Webhook 设置");
		m.put("Sending invitation to \"{0}\"...", "正在邀请 \"{0}\"...");
		m.put("Sending test mail to {0}...", "正在发送测试邮件到 {0}...");
		m.put("Sequential Group", "顺序组");
		m.put("Server", "服务器");
		m.put("Server Docker Executor", "服务器 Docker 执行器");
		m.put("Server Id", "服务器 ID");
		m.put("Server Information", "服务器信息");
		m.put("Server Log", "服务器日志");
		m.put("Server Setup", "服务器设置");
		m.put("Server Shell Executor", "服务器 Shell 执行器");
		m.put("Server URL", "服务器 URL");
		m.put("Server fingerprint", "服务器指纹");
		m.put("Server host", "服务器主机");
		m.put("Server is Starting...", "服务器正在启动...");
		m.put("Server url", "服务器 URL");
		m.put("Service", "服务");
		m.put("Service Account", "服务帐户");
		m.put("Service Desk", "服务台");
		m.put("Service Desk Email Address", "服务台电子邮件地址");
		m.put("Service Desk Issue Open Failed", "服务台工单开启失败");
		m.put("Service Desk Issue Open Failed Template", "服务台工单创建失败模板");
		m.put("Service Desk Issue Opened", "服务台工单已开启");
		m.put("Service Desk Issue Opened Template", "服务台工单创建模板");
		m.put("Service Desk Setting", "服务台设置");
		m.put("Service Desk Setting Holder", "服务台设置持有者");
		m.put("Service Desk Settings", "服务台设置");
		m.put("Service Locator", "服务定位器");
		m.put("Service Locators", "服务定位器");
		m.put("Service account not allowed to login", "服务账户不允许登录");
		m.put("Service desk setting", "服务台设置");
		m.put("Service desk settings have been saved", "服务台设置已保存");
		m.put("Services", "服务");
		m.put("Session Timeout", "会话超时");
		m.put("Set", "设置");
		m.put("Set All Queried As Root Projects", "设置所有查询的项目为根项目");
		m.put("Set All Queried Comments as Read", "将所有查询的评论设置为已读");
		m.put("Set All Queried Comments as Resolved", "设置所有选中的评论为已解决");
		m.put("Set All Queried Comments as Unresolved", "设置所有选中的评论为未解决");
		m.put("Set All Queried Issues as Read", "将所有查询的工单设置为已读");
		m.put("Set All Queried Pull Requests as Read", "设置所有查询的合并请求为已读");
		m.put("Set As Primary", "设置为主要");
		m.put("Set Build Description", "设置构建描述");
		m.put("Set Build Version", "设置构建版本");
		m.put("Set Resolved", "设置为已解决");
		m.put("Set Selected As Root Projects", "设置选中的项目为根项目");
		m.put("Set Selected Comments as Resolved", "设置选中的评论为已解决");
		m.put("Set Selected Comments as Unresolved", "设置选中的评论为未解决");
		m.put("Set Unresolved", "设置为未解决");
		m.put("Set Up Cache", "设置缓存");
		m.put("Set Up Renovate Cache", "设置 Renovate 缓存");
		m.put("Set Up Trivy Cache", "设置 Trivy 缓存");
		m.put("Set Up Your Account", "设置您的账户");
		m.put("Set as Private", "设为私有");
		m.put("Set as Public", "设为公开");
		m.put("Set description", "设置描述");
		m.put("Set reviewed", "设置为已审阅");
		m.put("Set unreviewed", "设置为未审阅");
		m.put("Set up Microsoft Teams notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url. ", 
			"设置 Microsoft Teams 通知设置。设置将继承给子项目，并可以通过定义具有相同 webhook url 的设置来覆盖。");
		m.put("Set up discord notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"设置 Discord 通知设置。设置将继承给子项目，并可以通过定义具有相同 Webhook URL 的设置来覆盖");
		m.put("Set up job cache to speed up job execution. Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> on how to use job cache", 
			"设置任务缓存以加快任务执行。检查<a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>此教程</a>了解如何使用任务缓存");
		m.put("Set up ntfy.sh notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"设置 ntfy.sh 通知设置。设置将继承给子项目，并可以通过定义具有相同 Webhook URL 的设置来覆盖");
		m.put("Set up slack notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"设置 Slack 通知设置。设置将继承给子项目，并可以通过定义具有相同 Webhook URL 的设置来覆盖");
		m.put("Set up two-factor authentication", "设置两阶段验证");
		m.put("Setting", "设置");
		m.put("Setting has been saved", "设置已保存");
		m.put("Settings", "设置");
		m.put("Settings and permissions of parent project will be inherited by this project", "父项目的设置和权限将继承给此项目");
		m.put("Settings saved", "设置已保存");
		m.put("Settings saved and project redistribution scheduled", "设置已保存并已安排项目重新分配");
		m.put("Settings updated", "设置已更新");
		m.put("Share dashboard", "共享仪表板");
		m.put("Share with Groups", "与组共享");
		m.put("Share with Users", "与用户共享");
		m.put("Shell", "Shell");
		m.put("Show Archived", "显示已归档");
		m.put("Show Branch/Tag", "显示分支/标签");
		m.put("Show Build Status", "显示构建状态");
		m.put("Show Closed", "显示已关闭");
		m.put("Show Code Stats", "显示代码统计");
		m.put("Show Command", "显示命令");
		m.put("Show Condition", "显示条件");
		m.put("Show Conditionally", "显示条件");
		m.put("Show Description", "显示描述");
		m.put("Show Duration", "显示持续时间");
		m.put("Show Emojis", "显示表情符号");
		m.put("Show Error Detail", "显示错误详情");
		m.put("Show Issue Status", "显示问题状态");
		m.put("Show Package Stats", "显示包统计");
		m.put("Show Pull Request Stats", "显示合并请求统计");
		m.put("Show Saved Queries", "显示保存的查询");
		m.put("Show States By", "显示状态");
		m.put("Show Works Of", "显示工作量");
		m.put("Show changes", "显示变更");
		m.put("Show commented code snippet", "显示评论的代码片段");
		m.put("Show commit of this parent", "显示此父节点的提交");
		m.put("Show emojis", "显示表情符号");
		m.put("Show in build list", "在构建列表中显示");
		m.put("Show issues in list", "在列表中显示工单");
		m.put("Show issues not scheduled into current iteration", "显示未安排到当前迭代的工单");
		m.put("Show matching agents", "显示匹配的代理");
		m.put("Show more", "显示更多");
		m.put("Show more lines", "显示更多行");
		m.put("Show next match", "显示下一个匹配");
		m.put("Show previous match", "显示上一个匹配");
		m.put("Show test cases of this test suite", "显示此测试套件的测试用例");
		m.put("Show total estimated/spent time", "显示总预计/已用时间");
		m.put("Showing first {0} files as there are too many", "显示前 {0} 个文件，因为文件太多");
		m.put("Sign In", "登录");
		m.put("Sign In To", "登录到");
		m.put("Sign Out", "登出");
		m.put("Sign Up", "注册");
		m.put("Sign Up Bean", "注册 Bean");
		m.put("Sign Up!", "注册！");
		m.put("Sign in", "登录");
		m.put("Signature required for this change, but no signing key is specified", "此更改需要签名，但未指定签名密钥");
		m.put("Signature required for this change, please generate system GPG signing key first", "需要签名才能更改。请先生成系统 GPG 签名密钥");
		m.put("Signature verified successfully with OneDev GPG key", "使用 OneDev GPG 密钥成功验证签名");
		m.put("Signature verified successfully with committer's GPG key", "使用提交者的 GPG 密钥成功验证签名");
		m.put("Signature verified successfully with committer's SSH key", "使用提交者的 SSH 密钥成功验证签名");
		m.put("Signature verified successfully with tagger's GPG key", "使用标记者的 GPG 密钥成功验证签名");
		m.put("Signature verified successfully with tagger's SSH key", "使用标记者的 SSH 密钥成功验证签名");
		m.put("Signature verified successfully with trusted GPG key", "使用可信 GPG 密钥成功验证签名");
		m.put("Signed with an unknown GPG key ", "使用未知的 GPG 密钥签名");
		m.put("Signed with an unknown ssh key", "使用未知的 ssh 密钥签名");
		m.put("Signer Email Addresses", "签署者邮箱地址");
		m.put("Signing Key ID", "签署密钥 ID");
		m.put("Similar Issues", "相似工单");
		m.put("Single Sign On", "单点登录");
		m.put("Single Sign-On", "单点登录");
		m.put("Single sign on via discord.com", "通过 discord.com 单点登录");
		m.put("Single sign on via twitch.tv", "通过 twitch.tv 单点登录");
		m.put("Site", "站点");
		m.put("Size", "大小");
		m.put("Size invalid", "大小无效");
		m.put("Slack Notifications", "Slack 通知");
		m.put("Smtp Ssl Setting", "SMTP SSL 设置");
		m.put("Smtp With Ssl", "SMTP SSL");
		m.put("Some builds are {0}", "某些构建是 {0}");
		m.put("Some jobs are hidden due to permission policy", "由于权限策略，部分任务被隐藏");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "有人修改了您正在编辑的内容。重新加载页面并重试。");
		m.put("Some other pull requests are opening to this branch", "其他合并请求正在打开此分支");
		m.put("Some projects might be hidden due to permission policy", "由于权限策略，某些项目可能被隐藏");
		m.put("Some related commits of the code comment is missing", "与代码评论相关的提交缺失");
		m.put("Some related commits of the pull request are missing", "与合并请求相关的提交缺失");
		m.put("Some required builds not passed", "一些必需的构建未通过");
		m.put("Someone made below change since you started editing", "自您开始编辑以来，有人进行了以下更改");
		m.put("Sort", "排序");
		m.put("Source", "源");
		m.put("Source Docker Image", "Docker 镜像源");
		m.put("Source Lines", "源代码行");
		m.put("Source Path", "源路径");
		m.put("Source branch already exists", "源分支已存在");
		m.put("Source branch already merged into target branch", "源分支已合并到目标分支");
		m.put("Source branch commits will be rebased onto target branch", "源分支提交将 rebase 到目标分支");
		m.put("Source branch is default branch", "源分支是默认分支");
		m.put("Source branch is outdated", "源分支已过时");
		m.put("Source branch no longer exists", "源分支不再存在");
		m.put("Source branch updated successfully", "源分支更新成功");
		m.put("Source project no longer exists", "源项目不再存在");
		m.put("Specified Value", "指定值");
		m.put("Specified choices", "指定选项");
		m.put("Specified default value", "指定默认值");
		m.put("Specified fields", "指定字段");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"指定 Active Directory 服务器的 LDAP URL，例如：<i>ldap://ad-server</i> 或 <i>ldaps://ad-server</i>。如果您的 LDAP 服务器使用自签名证书进行 ldaps 连接，您需要<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>配置 OneDev 信任证书</a>");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"指定 LDAP URL，例如：<i>ldap://localhost</i> 或 <i>ldaps://localhost</i>。如果您的 LDAP 服务器使用自签名证书进行 ldaps 连接，您需要<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>配置 OneDev 信任证书</a>");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"指定一个或多个用于用户搜索的根节点。例如：<i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"指定用户 LDAP 条目中包含属于组的识别名称的属性名称。例如，某些 LDAP 服务器使用属性 <i>memberOf</i> 列出组");
		m.put("Specifies password of above manager DN", "指定上述管理员 DN 的密码");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"指定在找到的组 LDAP 条目中包含组名称的属性。此属性的值将映射到 OneDev 组。此属性通常设置为 <i>cn</i>");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"指定 .net TRX 测试结果文件相对 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>，例如 <tt>TestResults/*.trx</tt>。使用 * 或 ? 进行模式匹配");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"指定 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 的值是具有代码写入权限的访问令牌。提交、问题和合并请求也将以访问令牌所有者的名义创建");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"指定 <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json 输出文件相对 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>。此文件可以通过 clippy json 输出选项生成，例如 <code>cargo clippy --message-format json>check-result.json</code>。使用 * 或 ? 进行模式匹配");
		m.put("Specify Build Options", "指定构建选项");
		m.put("Specify CPD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match", 
			"指定 CPD 结果 xml 文件相对 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>，例如 <tt>target/cpd.xml</tt>。使用 * 或 ? 进行模式匹配");
		m.put("Specify Commit Message", "指定提交消息");
		m.put("Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. Use * or ? for pattern match", 
			"指定 ESLint 报告文件在 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> 中以 checkstyle 格式生成。此文件可以通过 ESLint 选项 <tt>'-f checkstyle'</tt> 和 <tt>'-o'</tt> 生成。使用 * 或 ? 进行模式匹配");
		m.put("Specify GitHub API url, for instance <tt>https://api.github.com</tt>", "指定 GitHub API URL，例如 <tt>https://api.github.com</tt>");
		m.put("Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>", "指定 GitLab API URL，例如 <tt>https://gitlab.example.com/api/v4</tt>");
		m.put("Specify Gitea API url, for instance <tt>https://gitea.example.com/api/v1</tt>", "指定 Gitea API URL，例如 <tt>https://gitea.example.com/api/v1</tt>");
		m.put("Specify GoogleTest XML result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This report can be generated with environment variable <tt>GTEST_OUTPUT</tt> when running tests, For instance, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * or ? for pattern match", 
			"指定 GoogleTest XML 结果文件相对 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>。此报告可以通过在运行测试时使用环境变量 <tt>GTEST_OUTPUT</tt> 生成，例如 <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>。使用 * 或 ? 进行模式匹配");
		m.put("Specify IMAP user name.<br><b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system email address specified above", 
			"指定 IMAP 用户名。<br><b class='text-danger'>注意：</b> 此帐户应能够接收发送到上面指定的系统电子邮件地址的电子邮件");
		m.put("Specify JUnit test result file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> 的 JUnit XML 格式测试结果文件，例如 <tt>target/surefire-reports/TEST-*.xml</tt>。使用 * 或 ? 进行模式匹配");
		m.put("Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> 的 JaCoCo 覆盖率 xml 报告文件，例如 <tt>target/site/jacoco/jacoco.xml</tt>。使用 * 或 ? 进行模式匹配");
		m.put("Specify Jest coverage report file in clover format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> 的 clover 格式 Jest 覆盖率报告文件，例如 <tt>coverage/clover.xml</tt>。此文件可以通过 Jest 选项 <tt>'--coverage'</tt> 生成。使用 * 或 ? 进行模式匹配");
		m.put("Specify Jest test result file in json format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> 的 json 格式 Jest 测试结果文件。此文件可以通过 Jest 选项 <tt>'--json'</tt> 和 <tt>'--outputFile'</tt> 生成。使用 * 或 ? 进行模式匹配");
		m.put("Specify OCI layout directory of the image to scan. This directory can be generated via build image step or pull image step. It should be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"指定要扫描的镜像的 OCI 布局目录。此目录可以通过构建镜像步骤或拉取镜像步骤生成。它应该相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>");
		m.put("Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> 的 OCI 布局目录以推送");
		m.put("Specify OpenID scopes to request", "指定要请求的 OpenID 作用域");
		m.put("Specify PMD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/pmd.xml</tt>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> 的 PMD 结果 xml 文件，例如 <tt>target/pmd.xml</tt>。使用 * 或 ? 进行模式匹配");
		m.put("Specify PowerShell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of your script<br>", 
			"指定在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> 下执行的 PowerShell 命令。<br><b class='text-warning'>注意：</b> OneDev 检查脚本的退出代码以确定步骤是否成功。由于 PowerShell 即使有脚本错误也总是以 0 退出，您应该在脚本中处理错误并以非零代码退出，或在脚本开头添加行 <code>$ErrorActionPreference = &quot;Stop&quot;</code><br>");
		m.put("Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with <i>-o</i> option. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> 的 XML 格式 Roslynator 诊断输出文件。此文件可以通过 <i>-o</i> 选项生成。使用 * 或 ? 进行模式匹配");
		m.put("Specify Shell/Batch Commands to Run", "指定要运行的 Shell/Batch 命令");
		m.put("Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> 的 SpotBugs 结果 xml 文件，例如 <tt>target/spotbugsXml.xml</tt>。使用 * 或 ? 进行模式匹配");
		m.put("Specify System Settings", "指定系统设置");
		m.put("Specify URL of remote git repository. Only http/https protocol is supported", "指定远程 git 仓库的 URL。仅支持 http/https 协议");
		m.put("Specify YouTrack login name. This account should have permission to:<ul><li>Read full information and issues of the projects you want to import<li>Read issue tags<li>Read user basic information</ul>", 
			"指定 YouTrack 登录名。此账户应具有以下权限：<ul><li>读取您要导入的项目的完整信息和工单<li>读取工单标签<li>读取用户基本信息</ul>");
		m.put("Specify YouTrack password or access token for above user", "指定上述用户的 YouTrack 密码或访问令牌");
		m.put("Specify a &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regular expression&lt;/a&gt; to match issue references. For instance:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;", 
			"指定一个 <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>正则表达式</a> 来匹配工单引用。例如：<br> &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> after issue number", 
			"指定一个 <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>正则表达式</a> 在工单号之后");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> before issue number", 
			"指定一个 <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>正则表达式</a> 在工单号之前");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 作为 SSH 私钥");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 作为访问令牌");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 作为访问令牌以从上述项目导入构建规范，如果其代码不是公开可访问的");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 作为密码或访问令牌的注册表");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 作为访问远程仓库的密码或访问令牌");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 作为 SSH 认证的私钥。<b class='text-info'>注意：</b>不支持带密码短语的私钥");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 作为上述用户 SSH 认证的私钥。<b class='text-info'>注意：</b>不支持带密码短语的私钥");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a>，其值是具有上述项目管理权限的访问令牌。注意，如果同步到当前或子项目且构建提交可从默认分支访问，则不需要访问令牌");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a>，其值是具有上述项目上传缓存权限的访问令牌。注意，如果上传缓存到当前或子项目且构建提交可从默认分支访问，则不需要此属性");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"指定一个 <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron 计划</a>以自动触发任务。<b class='text-info'>注意：</b>为节省资源，cron 表达式中的秒数将被忽略，最小调度间隔为一分钟");
		m.put("Specify a Docker Image to Test Against", "指定要测试的 Docker 镜像");
		m.put("Specify a custom field of Enum type", "指定枚举类型的自定义字段");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "指定用于过滤/排序指定任务修复工单的默认查询");
		m.put("Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> 的文件以写入校验和");
		m.put("Specify a multi-value user field to hold assignees information.<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"指定一个多值用户字段以保存受理人信息。<b>注意：</b>如果这里没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Specify a multi-value user field to hold assignees information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"指定一个多值用户字段以保存受理人信息。<br><b>注意：</b>如果这里没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Specify a path inside container to be used as mount target", "指定容器内用作挂载目标的路径");
		m.put("Specify a path relative to job workspace to be used as mount source. Leave empty to mount job workspace itself", 
			"指定相对于任务工作区用作挂载源的路径。留空以挂载任务工作区本身");
		m.put("Specify a secret to be used as access token to create issue in above project if it is not publicly accessible", 
			"如果上述项目不是公开可访问的，指定一个用作访问令牌的密钥以在其中创建工单");
		m.put("Specify a secret to be used as access token to retrieve artifacts from above project. If not specified, project artifacts will be accessed anonymously", 
			"指定一个用作访问令牌的密钥以从上述项目拷贝制品。如果未指定，将匿名访问项目制品");
		m.put("Specify a secret to be used as access token to trigger job in above project", "指定一个秘密作为访问令牌以触发上述项目中的任务");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"指定一个密钥，其值是具有上述项目上传缓存权限的访问令牌。注意，如果上传缓存到当前或子项目且构建提交可从默认分支访问，则不需要此属性");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"指定 kubectl 用于访问集群的配置文件的绝对路径。留空让 kubectl 自动确定集群访问信息");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"指定 kubectl 工具的绝对路径，例如：<i>/usr/bin/kubectl</i>。如果留空，OneDev 将尝试从系统路径中查找该工具");
		m.put("Specify account name to login to Gmail to send/receive email", "指定用于登录 Gmail 发送/接收电子邮件的账户名");
		m.put("Specify additional users able to access this confidential issue besides those granted via role. Users mentioned in the issue will be authorized automatically", 
			"指定额外用户可以访问此机密工单，除了通过角色授予的用户。提及在工单中的用户将自动被授权");
		m.put("Specify agents applicable for this executor", "指定适用于此执行器的代理");
		m.put("Specify allowed <a href='https://spdx.org/licenses/' target='_blank'>spdx license identifiers</a> <span class='text-warning'>separated by comma</span>", 
			"指定允许的 <a href='https://spdx.org/licenses/' target='_blank'>SPDX 许可证标识符</a> <span class='text-warning'>以逗号分隔</span>");
		m.put("Specify an email address sharing same inbox as the system email address in mail setting definition. Emails sent to this address will be created as issues in this project. The default value takes form of <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>", 
			"指定一个与邮件设置定义中的系统电子邮件地址共享同一收件箱的电子邮件地址。发送到此地址的电子邮件将在此项目中创建为工单。默认值采用以下形式：<tt>&lt;系统电子邮件地址名称&gt;+&lt;项目路径&gt;@&lt;系统电子邮件地址域名&gt;</tt>");
		m.put("Specify applicable projects for above option. Multiple projects should be separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"指定上述选项适用的项目。多个项目应以空格分隔。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空表示所有项目");
		m.put("Specify applicable projects separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"指定以空格分隔的适用项目。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀 '-' 表示排除。留空表示所有项目");
		m.put("Specify application (client) ID of the app registered in Entra ID", "指定在 Entra ID 中注册的应用程序（客户端）ID");
		m.put("Specify arguments for imagetools. For instance <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>", 
			"指定 imagetools 的参数。例如 <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 清单摘要&gt; myorg/myrepo@&lt;amd64 清单摘要&gt;</code>");
		m.put("Specify artifacts to retrieve into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved.", 
			"指定要拷贝到 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的制品。只能拷贝已发布的制品（通过制品发布步骤）。");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"指定至少 10 个字母数字字符作为密钥，然后在 SendGrid 端添加入站解析条目：<ul><li><code>目标 URL</code> 应设置为 <i>&lt;OneDev 根 URL&gt;/~sendgrid/&lt;密钥&gt;</i>，例如 <i>https://onedev.example.com/~sendgrid/1234567890</i>。注意在生产环境中，应<a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>启用 https</a> 以保护密钥</li><li><code>接收域名</code> 应与上面指定的系统电子邮件地址的域名部分相同</li><li>启用选项 <code>POST 原始完整 MIME 消息</code></li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"指定用户搜索的基础节点。例如：<i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "指定要提交建议更改的分支");
		m.put("Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"指定要运行任务的分支。可以指定分支或标签，但不能同时指定。如果两者都未指定，将使用默认分支");
		m.put("Specify branch, tag or commit in above project to import build spec from", "指定要从上述项目导入构建规范的分支、标签或提交");
		m.put("Specify by Build Number", "按构建编号指定");
		m.put("Specify cache upload strategy after build successful. <var>Upload If Not Hit</var> means to upload when cache is not found with cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files in cache path are changed", 
			"指定构建成功后的缓存上传策略。<var>未命中时上传</var>表示当使用缓存键（非加载键）未找到缓存时上传，<var>更改时上传</var>表示当缓存路径中的某些文件发生更改时上传");
		m.put("Specify certificate to trust if you are using self-signed certificate for remote repository", 
			"如果您对远程仓库使用自签名证书，请指定要信任的证书");
		m.put("Specify certificates to trust if you are using self-signed certificates for your docker registries", 
			"如果您对 Docker 注册表使用自签名证书，请指定要信任的证书");
		m.put("Specify checkstyle result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/checkstyle-result.xml</tt>. Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> on how to generate the result xml file. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的 checkstyle 结果 xml 文件，例如 <tt>target/checkstyle-result.xml</tt>。参考 <a href='https://checkstyle.org/'>checkstyle 文档</a> 了解如何生成结果 xml 文件。使用 * 或 ? 进行模式匹配");
		m.put("Specify client secret of the app registered in Entra ID", "指定在 Entra ID 中注册的应用程序的客户端密钥");
		m.put("Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/clover/clover.xml</tt>. Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> on how to generate clover xml file. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的 clover 覆盖率 xml 报告文件，例如 <tt>target/site/clover/clover.xml</tt>。参考 <a href='https://openclover.org/documentation'>OpenClover 文档</a> 了解如何生成 clover xml 文件。使用 * 或 ? 进行模式匹配");
		m.put("Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的 cobertura 覆盖率 xml 报告文件，例如 <tt>target/site/cobertura/coverage.xml</tt>。使用 * 或 ? 进行模式匹配");
		m.put("Specify color of the state for displaying purpose", "指定状态的显示颜色");
		m.put("Specify columns of the board. Each column corresponds to a value of the issue field specified above", 
			"指定看板的列。每列对应于上面指定的工单字段的一个值");
		m.put("Specify command to check readiness of the service. This command will be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be executed repeatedly until a zero code is returned to indicate service ready", 
			"指定检查服务就绪状态的命令。此命令在 Windows 镜像上由 cmd.exe 解释，在 Linux 镜像上由 shell 解释。它将重复执行，直到返回零代码表示服务就绪");
		m.put("Specify commands to be executed on remote machine. <b class='text-warning'>Note:</b> user environments will not be picked up when execute these commands, set up them explicitly in commands if necessary", 
			"指定要在远程机器上执行的命令。<b class='text-warning'>注意：</b>执行这些命令时不会获取用户环境，如有必要，请在命令中显式设置它们");
		m.put("Specify condition to retry build upon failure", "指定失败时重试构建的条件");
		m.put("Specify configuration discovery url of your OpenID provider, for instance: <code>https://openid.example.com/.well-known/openid-configuration</code>. Make sure to use HTTPS protocol as OneDev relies on TLS encryption to ensure token validity", 
			"指定您的 OpenID 提供者的配置发现 URL，例如：<code>https://openid.example.com/.well-known/openid-configuration</code>。确保使用 HTTPS 协议，因为 OneDev 依赖 TLS 加密来确保令牌有效性");
		m.put("Specify container image to execute commands inside", "指定用于执行命令的容器镜像");
		m.put("Specify container image to run", "指定要运行的容器镜像");
		m.put("Specify cppcheck xml result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的 cppcheck xml 结果文件。此文件可以通过 cppcheck xml 输出选项生成，例如 <code>cppcheck src --xml 2>check-result.xml</code>。使用 * 或 ? 进行模式匹配");
		m.put("Specify cpu request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"指定使用此执行器的每个任务/服务的 CPU 请求。查看 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes 资源管理</a> 了解详情");
		m.put("Specify default assignees of pull requests submitted to this project. Only users with the write code permission to the project can be selected", 
			"指定提交到此项目的合并请求的默认受理人。只能选择具有项目写代码权限的用户");
		m.put("Specify default merge strategy of pull requests submitted to this project", "指定提交到此项目的合并请求的默认合并策略");
		m.put("Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple destinations should be separated with space", 
			"指定目标，例如 <tt>registry-server:5000/myorg/myrepo:latest</tt>。如果要推送到内置注册表，请确保使用与系统设置中服务器 URL 指定的<b>相同主机</b>，或者简单地使用格式 <tt>@server@/&lt;项目路径&gt;/&lt;仓库名称&gt;:&lt;标签名称&gt;</tt>。多个目标应以空格分隔");
		m.put("Specify directory (tenant) ID of the app registered in Entra ID", "指定在 Entra ID 中注册的应用程序的目录（租户）ID");
		m.put("Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 的目录以存储 OCI 布局");
		m.put("Specify docker image of the service", "指定服务的 Docker 镜像");
		m.put("Specify dockerx builder used to build docker image. OneDev will create the builder automatically if it does not exist. Check <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> on how to customize the builder for instance to allow publishing to insecure registries", 
			"指定用于构建 Docker 镜像的 dockerx 构建器。如果不存在，OneDev 将自动创建构建器。查看<a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>此教程</a>了解如何自定义构建器，例如允许发布到不安全的注册表");
		m.put("Specify email addresses to send invitations, with one per line", "指定要发送邀请的电子邮件地址，每行一个");
		m.put("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\"", 
			"指定预计时间 <b class='text-warning'>仅针对此工单</b>，不包括 \"{0}\"");
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"指定由 Renovate 创建的各种工单的字段以协调依赖更新");
		m.put("Specify fields to be displayed in the issue list", "指定要在工单列表中显示的字段");
		m.put("Specify fields to display in board card", "指定在看板卡片中显示的字段");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 要发布的文件。使用 * 或 ? 进行模式匹配");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"指定要创建 md5 校验和的文件。多个文件应以空格分隔。接受 <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> 模式。非绝对路径文件被视为相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a>");
		m.put("Specify files under above directory to be published", "指定要发布的目录下的文件");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be included in these files to be served as site start page", 
			"指定要发布的上述目录下的文件。使用 * 或 ? 进行模式匹配。<b>注意：</b>这些文件中应包含 <code>index.html</code> 作为站点起始页");
		m.put("Specify group to import from. Leave empty to import from projects under current account", 
			"指定要从中导入的组。留空则从当前账户下的项目导入");
		m.put("Specify how to map GitHub issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"指定如何将 GitHub 工单标签映射到 OneDev 自定义字段。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Specify how to map GitLab issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"指定如何将 GitLab 工单标签映射到 OneDev 自定义字段。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Specify how to map Gitea issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"指定如何将 Gitea 工单标签映射到 OneDev 自定义字段。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Specify how to map JIRA issue priorities to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"指定如何将 JIRA 工单优先级映射到 OneDev 自定义字段。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Specify how to map JIRA issue statuses to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"指定如何将 JIRA 工单状态映射到 OneDev 自定义字段。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单状态");
		m.put("Specify how to map JIRA issue types to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"指定如何将 JIRA 工单类型映射到 OneDev 自定义字段。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Specify how to map YouTrack issue fields to OneDev. Unmapped fields will be reflected in issue description.<br><b>Note: </b><ul><li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, for instance <tt>Priority::Critical</tt><li>You may customize OneDev issue fields in case there is no appropriate option here</ul>", 
			"指定如何将 YouTrack 工单字段映射到 OneDev。未映射的字段将反映在工单描述中。<br><b>注意：</b><ul><li>枚举字段需要以 <tt>&lt;字段名&gt;::&lt;字段值&gt;</tt> 的形式映射，例如 <tt>Priority::Critical</tt><li>如果没有合适的选项，您可以自定义 OneDev 工单字段</ul>");
		m.put("Specify how to map YouTrack issue links to OneDev issue links.<br><b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here", 
			"指定如何将 YouTrack 工单链接映射到 OneDev 工单链接。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单链接");
		m.put("Specify how to map YouTrack issue state to OneDev issue state. Unmapped states will use the initial state in OneDev.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"指定如何将 YouTrack 工单状态映射到 OneDev 工单状态。未映射的状态将使用 OneDev 中的初始状态。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单状态");
		m.put("Specify how to map YouTrack issue tags to OneDev issue custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"指定如何将 YouTrack 工单标签映射到 OneDev 工单自定义字段。<br><b>注意：</b>如果没有合适的选项，您可以自定义 OneDev 工单字段");
		m.put("Specify image on the login button", "指定登录按钮上的图像");
		m.put("Specify image tag to pull from, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to pull from built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"指定要拉取的镜像标签，例如 <tt>registry-server:5000/myorg/myrepo:latest</tt>。如果要从内置注册表拉取，请确保使用与系统设置中服务器 URL 相同的<b>主机</b>，或直接使用 <tt>@server@/&lt;项目路径&gt;/&lt;仓库名&gt;:&lt;标签名&gt;</tt> 的形式");
		m.put("Specify image tag to push to, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"指定要推送的镜像标签，例如 <tt>registry-server:5000/myorg/myrepo:latest</tt>。如果要推送到内置注册表，请确保使用与系统设置中服务器 URL 相同的<b>主机</b>，或直接使用 <tt>@server@/&lt;项目路径&gt;/&lt;仓库名&gt;:&lt;标签名&gt;</tt> 的形式");
		m.put("Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple tags should be separated with space", 
			"指定要推送的镜像标签，例如 <tt>registry-server:5000/myorg/myrepo:latest</tt>。如果要推送到内置注册表，请确保使用与系统设置中服务器 URL 指定的<b>相同主机</b>，或直接使用格式 <tt>@server@/&lt;项目路径&gt;/&lt;仓库名&gt;:&lt;标签名&gt;</tt>。多个标签应以空格分隔");
		m.put("Specify import option", "指定导入选项");
		m.put("Specify incoming email poll interval in seconds", "指定接收电子邮件的轮询间隔（秒）");
		m.put("Specify issue creation settings. For a particular sender and project, the first matching entry will take effect. Issue creation will be disallowed if no matching entry found", 
			"指定工单创建设置。对于特定的发送者和项目，第一个匹配的条目将生效。如果未找到匹配条目，将禁止创建工单");
		m.put("Specify issue field to identify different columns of the board. Only state and single-valued enumeration field can be used here", 
			"指定用于标识看板不同列的工单字段。此处只能使用状态字段和单值枚举字段");
		m.put("Specify links to be displayed in the issue list", "指定在工单列表中显示的链接");
		m.put("Specify links to display in board card", "指定在看板卡片中显示的链接");
		m.put("Specify manager DN to authenticate OneDev itself to Active Directory. The manager DN should be specified in form of <i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>manager@example.com</i>", 
			"指定管理器 DN 以将 OneDev 自身认证到 Active Directory。管理器 DN 应以 <i>&lt;账户名&gt;@&lt;域名&gt;</i> 的形式指定，例如：<i>manager@example.com</i>");
		m.put("Specify manager DN to authenticate OneDev itself to LDAP server", "指定用于 OneDev 向 LDAP 服务器进行身份验证的管理者 DN");
		m.put("Specify markdown file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的 markdown 文件以进行发布");
		m.put("Specify max git LFS file size in mega bytes", "指定 Git LFS 文件的最大大小（以兆字节为单位）");
		m.put("Specify max number of CPU intensive tasks the server can run concurrently, such as Git repository pull/push, repository index, etc.", 
			"指定服务器可并发运行的 CPU 密集型任务的最大数量，例如 Git 仓库拉取/推送、仓库索引等");
		m.put("Specify max number of jobs this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"指定此执行器在每个匹配代理上可并发运行的任务最大数量。留空以设置为代理 CPU 核心数");
		m.put("Specify max number of jobs this executor can run concurrently. Leave empty to set as CPU cores", 
			"指定此执行器可并发运行的任务最大数量。留空以设置为 CPU 核心数");
		m.put("Specify max number of jobs/services this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"指定此执行器在每个匹配代理上可并发运行的任务/服务最大数量。留空以设置为代理 CPU 核心数");
		m.put("Specify max number of jobs/services this executor can run concurrently. Leave empty to set as CPU cores", 
			"指定此执行器可并发运行的任务/服务最大数量。留空以设置为 CPU 核心数");
		m.put("Specify max size of uploaded file in mega bytes via web interface. This applies to file uploaded to repository, markdown content (issue comment etc), and build artifacts", 
			"指定通过 Web 界面上传文件的最大大小（以兆字节为单位）。这适用于上传到仓库的文件、markdown 内容（工单评论等）以及构建产物");
		m.put("Specify memory request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"指定使用此执行器的每个任务/服务的内存请求。详情请查看 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes 资源管理</a>");
		m.put("Specify mypy output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated by redirecting mypy output <b>without option '--pretty'</b>, for instance <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的 mypy 输出文件。此文件可通过重定向 mypy 输出生成，且<b>不使用 '--pretty' 选项</b>，例如 <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>。使用 * 或 ? 进行模式匹配");
		m.put("Specify name of the branch", "指定分支名称");
		m.put("Specify name of the environment variable", "指定环境变量名称");
		m.put("Specify name of the iteration", "指定迭代名称");
		m.put("Specify name of the job", "指定任务名称");
		m.put("Specify name of the report to be displayed in build detail page", "指定在构建详情页面显示的报告名称");
		m.put("Specify name of the saved query", "指定保存查询的名称");
		m.put("Specify name of the service, which will be used as host name to access the service", 
			"指定服务名称，将用作访问服务的主机名");
		m.put("Specify name of the tag", "指定标签名称");
		m.put("Specify network timeout in seconds when authenticate through this system", "指定通过此系统进行身份验证时的网络超时时间（秒）");
		m.put("Specify node selector of this locator", "指定此定位器的节点选择器");
		m.put("Specify password or access token of specified registry", "指定指定注册表的密码或访问令牌");
		m.put("Specify password to authenticate with", "指定用于身份验证的密码");
		m.put("Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>", "指定 curl 可执行文件的路径，例如：<tt>/usr/bin/curl</tt>");
		m.put("Specify path to git executable, for instance: <tt>/usr/bin/git</tt>", "指定 git 可执行文件的路径，例如：<tt>/usr/bin/git</tt>");
		m.put("Specify powershell executable to be used", "指定要使用的 PowerShell 可执行文件");
		m.put("Specify project to import build spec from", "指定从中导入构建规范的项目");
		m.put("Specify project to import into at OneDev side", "指定在 OneDev 端导入的项目");
		m.put("Specify project to retrieve artifacts from", "指定从中拷贝制品的项目");
		m.put("Specify project to run job in", "指定要运行任务的项目");
		m.put("Specify projects", "指定项目");
		m.put("Specify projects to update dependencies. Leave empty for current project", "指定要更新依赖的项目。留空表示当前项目");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的 pylint JSON 结果文件。此文件可使用 pylint JSON 输出格式选项生成，例如 <code>--exit-zero --output-format=json:pylint-result.json</code>。注意，违反规则时我们不会使 pylint 命令失败，因为此步骤将根据配置的阈值使构建失败。使用 * 或 ? 进行模式匹配");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"如有必要，指定注册表登录信息。对于内置注册表，使用 <code>@server_url@</code> 作为注册表 URL，<code>@job_token@</code> 作为用户名，访问令牌作为密码");
		m.put("Specify registry url. Leave empty for official registry", "指定注册表 URL。留空表示官方注册表");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"指定在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 下存储 OCI 布局的相对路径");
		m.put("Specify repositories", "指定仓库");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"指定更改指定路径时所需的审阅者。注意，提交更改的用户被视为已自动审阅该更改");
		m.put("Specify root URL to access this server", "指定访问此服务器的根 URL");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的 ruff JSON 结果文件。此文件可使用 ruff JSON 输出格式选项生成，例如 <code>--exit-zero --output-format json --output-file ruff-result.json</code>。注意，违反规则时我们不会使 ruff 命令失败，因为此步骤将根据配置的阈值使构建失败。使用 * 或 ? 进行模式匹配");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"指定在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 下执行的 shell 命令（在 Linux/Unix 上）或批处理命令（在 Windows 上）");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"指定在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 下执行的 shell 命令");
		m.put("Specify shell to be used", "指定要使用的 shell");
		m.put("Specify source param for SCP command, for instance <code>app.tar.gz</code>", "指定 SCP 命令的源参数，例如 <code>app.tar.gz</code>");
		m.put("Specify space separated refs to pull from remote. '*' can be used in ref name for wildcard match<br><b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update branches/tags via this step", 
			"指定从远程拉取的以空格分隔的引用。引用名称中可使用 '*' 进行通配符匹配<br><b class='text-danger'>注意：</b> 通过此步骤更新分支/标签时，将忽略分支/标签保护规则");
		m.put("Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"指定要保护的以空格分隔的分支。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。以 '-' 为前缀表示排除");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"指定以空格分隔的任务。使用 '*' 或 '?' 进行通配符匹配。以 '-' 为前缀表示排除");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts will be granted implicitly in matched jobs even if no other permissions are specified here", 
			"指定以空格分隔的任务。使用 '*' 或 '?' 进行通配符匹配。以 '-' 为前缀表示排除。<b class='text-danger'>注意：</b> 即使此处未指定其他权限，匹配的任务也将隐式授予访问构建产物的权限");
		m.put("Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"指定要保护的以空格分隔的路径。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。以 '-' 为前缀表示排除");
		m.put("Specify space-separated projects applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all projects", 
			"指定适用于此条目的以空格分隔的项目。使用 '*' 或 '?' 进行通配符匹配。以 '-' 为前缀表示排除。留空表示匹配所有项目");
		m.put("Specify space-separated sender email addresses applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all senders", 
			"指定适用于此条目的以空格分隔的发送者电子邮件地址。使用 '*' 或 '?' 进行通配符匹配。以 '-' 为前缀表示排除。留空表示匹配所有发送者");
		m.put("Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"指定要保护的以空格分隔的标签。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。以 '-' 为前缀表示排除");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的报告起始页面，例如：<tt>manual/index.md</tt>");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的报告起始页面，例如：api/index.html");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"指定为构建卷请求的存储大小。大小应符合 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes 资源容量格式</a>，例如 <i>10Gi</i>");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"指定用于计算报告中发现问题列值的制表符宽度");
		m.put("Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"指定要运行任务的标签。可以指定分支或标签，但不能同时指定。如果两者都未指定，将使用默认分支");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"指定 SCP 命令的目标参数，例如 <code>user@@host:/app</code>。<b class='text-info'>注意：</b> 确保远程主机上已安装 scp 命令");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"指定用于替换匹配工单引用的文本，例如：&lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;这里 $1 和 $2 表示示例工单模式中的捕获组（参见工单模式帮助）");
		m.put("Specify the condition current build must satisfy to execute this action", "指定当前构建必须满足的条件以执行此操作");
		m.put("Specify the condition preserved builds must match", "指定保留的构建必须匹配的条件");
		m.put("Specify the private key (in PEM format) used by SSH server to establish connections with client", 
			"指定用于与客户端建立连接的 SSH 服务器使用的私钥（在 PEM 格式中）");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"指定查询组成员信息的策略。要为 LDAP 组分配适当权限，应定义一个同名的 OneDev 组。如果您希望在 OneDev 端管理组成员关系，请使用策略 <tt>不查询组</tt>");
		m.put("Specify timeout in seconds when communicating with mail server", "指定与邮件服务器通信时的超时时间（秒）");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "指定超时时间（秒）。从任务提交时开始计算");
		m.put("Specify title of the issue", "指定工单的标题");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "指定 YouTrack API 的 URL。例如 <tt>http://localhost:8080/api</tt>");
		m.put("Specify user name of above machine for SSH authentication", "指定上述机器的用户名用于 SSH 身份验证");
		m.put("Specify user name of specified registry", "指定指定注册表的用户名");
		m.put("Specify user name of the registry", "指定注册表的用户名");
		m.put("Specify user name to authenticate with", "指定用于身份验证的用户名");
		m.put("Specify value of the environment variable", "指定环境变量的值");
		m.put("Specify web UI session timeout in minutes. Existing sessions will not be affected after changing this value.", 
			"指定 Web UI 会话超时时间（分钟）。更改此值后，现有会话将不受影响。");
		m.put("Specify webhook url to post events", "指定发布事件的 Webhook URL");
		m.put("Specify which issue state to use for closed GitHub issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"指定用于已关闭 GitHub 工单的工单状态。<br><b>注意：</b> 如果此处没有合适的选项，您可以自定义 OneDev 工单状态");
		m.put("Specify which issue state to use for closed GitLab issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"指定用于已关闭 GitLab 工单的工单状态。<br><b>注意：</b> 如果此处没有合适的选项，您可以自定义 OneDev 工单状态");
		m.put("Specify which issue state to use for closed Gitea issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"指定用于已关闭 Gitea 工单的工单状态。<br><b>注意：</b> 如果此处没有合适的选项，您可以自定义 OneDev 工单状态");
		m.put("Specify which states are considered as closed for various issues created by Renovate to orchestrate dependency update. Additionally, when Renovate closes the issue, OneDev will transit the issue to first state specified here", 
			"指定由 Renovate 创建的用于协调依赖更新的各种工单的关闭状态。此外，当 Renovate 关闭工单时，OneDev 会将工单转换为此处指定的第一个状态");
		m.put("Specify working days per week. This will affect parsing and displaying of working periods. For instance <tt>1w</tt> is the same as <tt>5d</tt> if this property is set to <tt>5</tt>", 
			"指定每周的工作天数。这将影响工作周期的解析和显示。例如，如果此属性设置为 <tt>5</tt>，则 <tt>1w</tt> 等同于 <tt>5d</tt>");
		m.put("Specify working hours per day. This will affect parsing and displaying of working periods. For instance <tt>1d</tt> is the same as <tt>8h</tt> if this property is set to <tt>8</tt>", 
			"指定每天的工作小时数。这将影响工作周期的解析和显示。例如，如果此属性设置为 <tt>8</tt>，则 <tt>1d</tt> 等同于 <tt>8h</tt>");
		m.put("Spent", "已花费");
		m.put("Spent Time", "已用时间");
		m.put("Spent Time Issue Field", "工单耗时字段");
		m.put("Spent Time:", "已用时间");
		m.put("Spent time / estimated time", "已用时间 / 预计时间");
		m.put("Split", "拆分");
		m.put("Split view", "分割视图");
		m.put("SpotBugs Report", "SpotBugs 报告");
		m.put("Squash Source Branch Commits", "压缩源分支提交");
		m.put("Squash all commits from source branch into a single commit in target branch", "将源分支的所有提交压缩成一个提交，并添加到目标分支");
		m.put("Squash source branch commits", "Squash 源分支上的提交");
		m.put("Ssh", "SSH");
		m.put("Ssh Key", "SSH 密钥");
		m.put("Ssh Setting", "SSH 设置");
		m.put("Ssl Setting", "SSL 设置");
		m.put("Sso Connector", "SSO 连接器");
		m.put("Sso Provider Bean", "Sso 提供方 Bean");
		m.put("Start At", "开始于");
		m.put("Start Date", "开始日期");
		m.put("Start Page", "起始页面");
		m.put("Start agent on remote Linux machine by running below command:", "在远程 Linux 机器上通过运行以下命令启动代理：");
		m.put("Start date", "开始日期");
		m.put("Start to watch once I am involved", "一旦我参与，开始观察");
		m.put("Start work", "开始工作");
		m.put("Start/Due Date", "开始/截止日期");
		m.put("State", "状态");
		m.put("State Durations", "状态持续时间");
		m.put("State Frequencies", "状态频率");
		m.put("State Spec", "状态规范");
		m.put("State Transitions", "状态转换");
		m.put("State Trends", "状态趋势");
		m.put("State of an issue is transited", "工单的状态已转换");
		m.put("States", "状态");
		m.put("Statistics", "统计");
		m.put("Stats", "统计");
		m.put("Stats Group", "统计组");
		m.put("Status", "状态");
		m.put("Status Code", "状态码");
		m.put("Status code", "状态码");
		m.put("Status code other than 200 indicating the error type", "非200的状态码表示错误类型");
		m.put("Step", "步骤");
		m.put("Step Template", "步骤模板");
		m.put("Step Templates", "步骤模板");
		m.put("Step {0} of {1}: ", "步骤 {0}/{1}：");
		m.put("Steps", "步骤");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"步骤将在同一节点上按顺序执行，共享同一 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a>");
		m.put("Stop work", "停止工作");
		m.put("Stopwatch Overdue", "秒表逾期");
		m.put("Storage Settings", "存储设置");
		m.put("Storage file missing", "存储文件缺失");
		m.put("Storage not found", "未找到存储");
		m.put("Stored with Git LFS", "使用 Git LFS 存储");
		m.put("Sub Keys", "子密钥");
		m.put("Subject", "主题");
		m.put("Submit", "提交");
		m.put("Submit Reason", "提交原因");
		m.put("Submit Support Request", "提交支持请求");
		m.put("Submitted After", "提交后");
		m.put("Submitted At", "提交于");
		m.put("Submitted Before", "提交前");
		m.put("Submitted By", "提交者");
		m.put("Submitted manually", "手动构建");
		m.put("Submitter", "提交者");
		m.put("Subscription Key", "订阅密钥");
		m.put("Subscription Management", "订阅管理");
		m.put("Subscription data", "订阅数据");
		m.put("Subscription key installed successfully", "订阅密钥安装成功");
		m.put("Subscription key not applicable: this key is intended to activate a trial subscription", 
			"订阅密钥不适用：此密钥用于激活试用订阅");
		m.put("Subscription key not applicable: this key is intended to renew a user based subscription", 
			"订阅密钥不适用：此密钥用于续订基于用户的订阅");
		m.put("Subscription key not applicable: this key is intended to renew an unlimited users subscription", 
			"订阅密钥不适用：此密钥用于续订无限用户订阅");
		m.put("Subscription key not applicable: this key is intended to update licensee of an existing subscription", 
			"订阅密钥不适用：此密钥用于更新现有订阅的被许可人");
		m.put("Success Rate", "成功率");
		m.put("Successful", "成功");
		m.put("Suffix Pattern", "后缀模式");
		m.put("Suggest changes", "建议更改");
		m.put("Suggested change", "建议更改");
		m.put("Suggestion is outdated either due to code change or pull request close", "建议由于代码更改或合并请求关闭而过时");
		m.put("Suggestions", "建议");
		m.put("Summary", "摘要");
		m.put("Support & Bug Report", "技术支持与错误报告");
		m.put("Support Request", "支持请求");
		m.put("Swap", "交换");
		m.put("Switch to HTTP(S)", "切换到 HTTP(S)");
		m.put("Switch to SSH", "切换到 SSH");
		m.put("Symbol Name", "符号名");
		m.put("Symbol name", "符号名称");
		m.put("Symbols", "符号");
		m.put("Sync Replica Status and Back to Home", "同步副本状态并返回主页");
		m.put("Sync Repository", "同步仓库");
		m.put("Sync Timing of All Queried Issues", "同步所有查询的工单的时间");
		m.put("Sync Timing of Selected Issues", "同步选定的工单的时间");
		m.put("Sync requested. Please check status after a while", "同步请求已发送。请稍后检查状态");
		m.put("Synchronize", "同步");
		m.put("System", "系统");
		m.put("System Alert", "系统提醒");
		m.put("System Alert Template", "系统告警模板");
		m.put("System Date", "系统日期");
		m.put("System Email Address", "系统电子邮件地址");
		m.put("System Maintenance", "系统维护");
		m.put("System Setting", "系统设置");
		m.put("System Settings", "系统设置");
		m.put("System email address defined in mail setting should be used as recipient of such email, and project name may be appended to this address using to indicate where to create issues. For instance, if system email address is specified as <tt>support@example.com</tt>, sending email to <tt>support+myproject@example.com</tt> will create issue in <tt>myproject</tt>. If project name is not appended, OneDev will look up the project using project designation information below", 
			"邮件设置中定义的系统邮箱地址应作为此类邮件的收件人，并且可以使用项目名称附加到此地址以指示创建工单的位置。例如，如果系统邮箱地址指定为 <tt>support@example.com</tt>，发送邮件到 <tt>support+myproject@example.com</tt> 将在 <tt>myproject</tt> 中创建工单。如果未附加项目名称，OneDev 将使用下面的项目指定信息查找项目");
		m.put("System settings have been saved", "系统设置已保存");
		m.put("System uuid", "系统 UUID");
		m.put("TIMED_OUT", "已超时");
		m.put("TRX Report (.net unit test)", "TRX 报告（.NET 单元测试）");
		m.put("Tab Width", "制表符宽度");
		m.put("Tag", "标签");
		m.put("Tag \"{0}\" already exists, please choose a different name", "标签 \"{0}\" 已存在，请选择不同的名称");
		m.put("Tag \"{0}\" already exists, please choose a different name.", "标签 \"{0}\" 已存在，请选择不同的名称");
		m.put("Tag \"{0}\" created", "标签 \"{0}\" 已创建");
		m.put("Tag \"{0}\" deleted", "标签 \"{0}\" 已删除");
		m.put("Tag Message", "标签注释");
		m.put("Tag Name", "标签名称");
		m.put("Tag Protection", "标签保护");
		m.put("Tag creation", "标签创建");
		m.put("Tags", "标签");
		m.put("Target", "目标");
		m.put("Target Branches", "目标分支");
		m.put("Target Docker Image", "目标 Docker 镜像");
		m.put("Target File", "目标文件");
		m.put("Target Path", "目标路径");
		m.put("Target Project", "目标项目");
		m.put("Target branch no longer exists", "目标分支不再存在");
		m.put("Target branch was fast-forwarded to source branch", "目标分支已 fast-forward 到源分支");
		m.put("Target branch will be fast-forwarded to source branch", "目标分支将被 fast-forward 到源分支");
		m.put("Target containing spaces or starting with dash needs to be quoted", "包含空格或以破折号开头的目标需要用引号包围");
		m.put("Target or source branch is updated. Please try again", "目标或源分支已更新，请重试");
		m.put("Task List", "任务列表");
		m.put("Task list", "任务列表");
		m.put("Tell user to reset password", "告诉用户重置密码");
		m.put("Template Name", "模板名称");
		m.put("Template saved", "模板已保存");
		m.put("Terminal close", "终端关闭");
		m.put("Terminal input", "终端输入");
		m.put("Terminal open", "终端打开");
		m.put("Terminal output", "终端输出");
		m.put("Terminal ready", "终端就绪");
		m.put("Terminal resize", "终端调整大小");
		m.put("Test", "测试");
		m.put("Test Case", "测试用例");
		m.put("Test Cases", "测试用例");
		m.put("Test Settings", "测试设置");
		m.put("Test Suite", "测试套件");
		m.put("Test Suites", "测试套件");
		m.put("Test importing from {0}", "测试从 {0} 导入");
		m.put("Test mail has been sent to {0}, please check your mail box", "测试邮件已发送到 {0}，请检查您的邮箱");
		m.put("Test successful: authentication passed", "认证成功: 认证通过");
		m.put("Test successful: authentication passed with below information retrieved:", "认证成功: 认证通过，已查询到以下信息:");
		m.put("Text", "文本");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "接收 Webhook POST 请求的服务器端点 URL");
		m.put("The change contains disallowed file type(s): {0}", "更改包含不允许的文件类型：{0}");
		m.put("The first board will be the default board", "第一个看板将成为默认看板");
		m.put("The first timesheet will be the default timesheet", "第一个时间表将成为默认时间表");
		m.put("The object you are deleting/disabling is still being used", "您正在删除/禁用的对象仍在使用中");
		m.put("The password reset url is invalid or obsolete", "密码重置URL无效或已过期");
		m.put("The permission to access build log", "访问构建日志的权限");
		m.put("The permission to access build pipeline", "访问构建流水线的权限");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"手动运行任务的权限。这也隐含访问构建日志、构建流水线和所有已发布报告的权限");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"确保发送到有效负载 URL 的 POST 请求来自 OneDev 的密钥。设置密钥后，您将在 Webhook POST 请求中收到 X-OneDev-Signature 头部");
		m.put("The service desk feature enables user to create issues by sending emails to OneDev. Issues can be discussed over email completely, without the need of logging to OneDev.", 
			"服务台功能使用户能够通过向 OneDev 发送邮件来创建工单。工单可以完全通过邮件进行讨论，无需登录 OneDev。");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "然后输入 TOTP 认证器中显示的验证码进行验证");
		m.put("Then publish package from project directory like below", "然后从项目目录中发布包，如下所示");
		m.put("Then push gem to the source", "然后推送 Gem 到源");
		m.put("Then push image to desired repository under specified project", "然后推送镜像到指定项目下的目标仓库");
		m.put("Then push package to the source", "然后推送包到源");
		m.put("Then resolve dependency via command step", "然后通过命令步骤解析依赖");
		m.put("Then upload package to the repository with twine", "然后通过命令步骤上传包到仓库");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"分支 <span wicket:id=\"branch\"></span> 有 <a wicket:id=\"openRequests\">打开的合并请求</a>。如果分支被删除，这些合并请求将被丢弃。");
		m.put("There are incompatibilities since your upgraded version", "由于您的版本升级，存在不兼容性");
		m.put("There are merge conflicts", "存在合并冲突");
		m.put("There are merge conflicts.", "有合并冲突。");
		m.put("There are merge conflicts. You can still create the pull request though", "有合并冲突。您仍然可以创建合并请求");
		m.put("There are unsaved changes, discard and continue?", "有未保存的更改，是否丢弃并继续？");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"这些认证器通常在您的手机上运行，例如 Google Authenticator、Microsoft Authenticator、Authy、1Password 等");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"此 <span wicket:id=\"elementTypeName\"></span> 是从 <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a> 导入的");
		m.put("This Month", "本月");
		m.put("This Week", "本周");
		m.put("This account is disabled", "此账户已禁用");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"此地址应在 SendGrid 中为 <code>已验证的发送者</code>，将用作各种电子邮件通知的发送者地址。如果下面启用了 <code>接收已发布电子邮件</code> 选项，用户也可以回复此地址以发布工单或合并请求评论");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"此地址将用作各种电子邮件通知的发送者地址。如果下面启用了 <code>检查接收电子邮件</code> 选项，用户也可以通过电子邮件回复此地址以发布工单或合并请求评论");
		m.put("This change is already opened for merge by pull request {0}", "此变更已有合并请求 {0}");
		m.put("This change is squashed/rebased onto base branch via a pull request", "此更改已通过合并请求压缩/重定基到基础分支");
		m.put("This change is squashed/rebased onto base branch via pull request {0}", "此变更已通过合并请求 {0} squash/rebase 到基准分支");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "此更改需要由某些任务验证。请提交合并请求");
		m.put("This commit is rebased", "此提交已变基");
		m.put("This date is using <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 format</a>", 
			"此日期使用<a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601格式</a>");
		m.put("This email address is being used", "此电子邮件地址正在使用");
		m.put("This executor runs build jobs as docker containers on OneDev server", "此执行器在 OneDev 服务器上以 Docker 容器运行构建任务");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"此执行器通过 <a href='/~administration/agents' target='_blank'>代理</a> 在远程机器上以 Docker 容器运行构建任务");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"此执行器在 Kubernetes 集群中以 Pod 形式运行构建任务，无需任何代理。<b class='text-danger'>注意：</b> 确保在系统设置中正确指定服务器 URL，因为任务 Pod 需要访问它以下载源代码和产物");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs", 
			"此执行器使用 OneDev 服务器的 shell 功能运行构建作业。<br><b class='text-danger'>警告</b>：使用此执行器运行的作业具有与 OneDev 服务器进程相同的权限。请确保它仅能被可信的作业使用");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. Make sure it can only be used by trusted jobs", 
			"此执行器通过 <a href='/~administration/agents' target='_blank'>代理</a>使用远程机器的 shell 功能运行构建作业<br><b class='text-danger'>警告</b>：使用此执行器运行的作业具有与 OneDev 代理进程相同的权限。请确保它仅能被可信的作业使用");
		m.put("This field is required", "此字段是必填的");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"此过滤器用于确定当前用户的 LDAP 条目。例如：<i>(&(uid={0})(objectclass=person))</i>。在此示例中，<i>{0}</i> 表示当前用户的登录名");
		m.put("This installation does not have an active subscription and runs as community edition. To access <a href=\"https://onedev.io/pricing\">enterprise features</a>, an active subscription is required", 
			"此安装没有有效订阅，并作为社区版运行。要访问<a href=\"https://onedev.io/pricing\">企业功能</a>，需要有效订阅");
		m.put("This installation has a trial subscription and is now running as enterprise edition", 
			"此安装有试用订阅，现在以企业版运行");
		m.put("This installation has an active subscription and runs as enterprise edition", "此安装有有效订阅，并作为企业版运行");
		m.put("This installation has an expired subscription, and runs as community edition", "此安装的订阅已过期，以社区版运行");
		m.put("This installation has an unlimited users subscription and is now running as enterprise edition", 
			"此安装具有无限用户订阅，现在以企业版运行");
		m.put("This installation's subscription has expired and is now running as the community edition", 
			"此安装的订阅已过期，现在以社区版运行");
		m.put("This is a Git LFS object, but the storage file is missing", "这是一个 Git LFS 对象，但存储文件缺失");
		m.put("This is a built-in role and can not be deleted", "这是一个内置角色，无法删除");
		m.put("This is a disabled service account", "这是一个已禁用的服务账户");
		m.put("This is a layer cache. To use the cache, add below option to your docker buildx command", 
			"这是一个层缓存。要使用缓存，请将以下选项添加到您的 docker buildx 命令中");
		m.put("This is a service account for task automation purpose", "这是一个用于任务自动化的服务账户");
		m.put("This is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"这是一个企业功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a>30天");
		m.put("This key has already been used by another project", "此键已被其他项目使用");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"此密钥与 {0} 相关联，然而它不是此用户的已验证电子邮件地址");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here.<br><b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated from these lock files with the <b>generate checksum step</b> defined before this step", 
			"此密钥用于确定项目层次结构中是否存在缓存命中（按顺序从当前项目搜索到根项目，下面的加载密钥也是如此）。如果其密钥与此处定义的密钥完全相同，则认为缓存命中。<br><b>注意：</b>如果您的项目有能够表示缓存状态的锁定文件（package.json、pom.xml 等），则此密钥应定义为 &lt;cache name&gt;-@file:checksum.txt@，其中 checksum.txt 是从这些锁定文件生成的，并在此步骤之前定义了<b>生成校验和步骤</b>");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"此密钥用于在项目层次结构中下载和上传缓存（按顺序从当前项目搜索到根项目）");
		m.put("This key or one of its sub key is already added", "此密钥或其子密钥已添加");
		m.put("This key or one of its subkey is already in use", "此密钥或其子密钥已被使用");
		m.put("This line has confusable unicode character modification", "此行有可疑的 Unicode 字符修改");
		m.put("This might happen when project points to a wrong git repository, or the commit is garbage collected.", 
			"这可能发生在项目指向错误的 git 仓库，或者这些提交被垃圾回收。");
		m.put("This might happen when project points to a wrong git repository, or these commits are garbage collected.", 
			"这可能发生在项目指向错误的 git 仓库，或者这些提交被垃圾回收。");
		m.put("This name has already been used by another board", "此名称已被另一个看板使用");
		m.put("This name has already been used by another group", "此名称已被另一个组使用");
		m.put("This name has already been used by another issue board in the project", "此名称已被项目中的另一个工单看板使用");
		m.put("This name has already been used by another job executor", "此名称已被另一个任务执行器使用");
		m.put("This name has already been used by another project", "此名称已被其他项目使用");
		m.put("This name has already been used by another provider", "此名称已被另一个提供者使用");
		m.put("This name has already been used by another role", "此名称已被另一个角色使用");
		m.put("This name has already been used by another role.", "此名称已被另一个角色使用。");
		m.put("This name has already been used by another script", "此名称已被另一个脚本使用");
		m.put("This name has already been used by another state", "此名称已被另一个状态使用");
		m.put("This operation is disallowed by branch protection rule", "此操作被分支保护规则禁止");
		m.put("This page lists changes since previous build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a>", 
			"此页面列出了自上一个构建以来的更改，在 <a href=\"https://docs.onedev.io/concepts#构建流\" class=\"link-primary\" target=\"_blank\">相同流</a> 上");
		m.put("This page lists recent commits fixing the issue", "此页面列出了修复工单的最近提交");
		m.put("This permission enables one to access confidential issues", "此权限允许访问机密工单");
		m.put("This permission enables one to schedule issues into iterations", "此权限允许将工单调度到迭代中");
		m.put("This property is imported from {0}", "此属性是从 {0} 导入的");
		m.put("This pull request has been discarded", "此合并请求已丢弃");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"如果构建由合并请求触发，此报告将显示在合并请求概览页面中");
		m.put("This server is currently accessed via http protocol, please configure your docker daemon or buildx builder to <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">work with insecure registry</a>", 
			"当前服务器通过 http 协议访问，请配置您的 docker 守护进程或 buildx 构建器以 <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">发布镜像到不安全注册表</a>");
		m.put("This shows average duration of different states over time", "显示不同状态的平均持续时间");
		m.put("This shows average duration of merged pull requests over time", "此图表显示合并请求的平均持续时间");
		m.put("This shows number of <b>new</b> issues in different states over time", "显示不同状态的新工单数量");
		m.put("This shows number of issues in various states over time", "显示不同状态的工单数量");
		m.put("This shows number of open and merged pull requests over time", "此图表显示打开和合并的合并请求的数量");
		m.put("This step can only be executed by a docker aware executor", "此步骤只能由支持 Docker 的执行器执行");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"此步骤只能由支持 Docker 的执行器执行。它在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a> 下运行");
		m.put("This step copies files from job workspace to build artifacts directory, so that they can be accessed after job is completed", 
			"此步骤将文件从作业工作区复制到构建工件目录，以便在作业完成后可以访问它们");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"此步骤发布指定文件作为项目网站。项目网站可以通过 <code>http://&lt;onedev 基础 url&gt;/path/to/project/~site</code> 公开访问");
		m.put("This step pulls specified refs from remote", "此步骤从远程拉取指定的引用");
		m.put("This step pushes current commit to same ref on remote", "此步骤将当前提交推送至远程的同一引用");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"此步骤设置 Renovate 缓存。如果需要使用，请将其放置在 Renovate 步骤之前");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"此步骤设置 trivy 数据库缓存以加速各种扫描步骤。如果需要使用，请将其放置在扫描步骤之前");
		m.put("This subscription key was already used", "此订阅密钥已被使用");
		m.put("This subscription key was expired", "此订阅密钥已过期");
		m.put("This tab shows the pipeline containing current build. Check <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">this article</a> to understand how build pipeline works", 
			"此标签显示包含当前构建的流水线。请查看 <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">这篇文章</a> 以了解流水线的工作原理");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"此触发器仅在标记的提交可从此处指定的分支访问时适用。多个分支应以空格分隔。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。以 '-' 为前缀表示排除。留空表示匹配所有分支");
		m.put("This user is authenticating via external system.", "此用户通过外部系统进行身份验证。");
		m.put("This user is authenticating via internal database.", "此用户通过内部数据库进行身份验证。");
		m.put("This user is currently authenticating via external system. Setting password will switch to use internal database", 
			"该用户当前通过外部系统进行身份验证。设置密码将切换到使用内部数据库");
		m.put("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?", 
			"这将停用当前订阅，所有企业功能将被禁用，您确定要继续吗？");
		m.put("This will discard all project specific boards, do you want to continue?", "这将丢弃所有项目特定的看板，您确定要继续吗？");
		m.put("This will restart SSH server. Do you want to continue?", "这将重启 SSH 服务器。您确定要继续吗？");
		m.put("Threads", "线程");
		m.put("Time Estimate Issue Field", "工单时间估算字段");
		m.put("Time Range", "时间范围");
		m.put("Time Spent Issue Field", "工单耗时字段");
		m.put("Time Tracking", "时间跟踪");
		m.put("Time Tracking Setting", "时间跟踪设置");
		m.put("Time Tracking Settings", "时间跟踪设置");
		m.put("Time tracking settings have been saved", "时间跟踪设置已保存");
		m.put("Timed out", "超时");
		m.put("Timeout", "超时");
		m.put("Timesheet", "时间表");
		m.put("Timesheet Setting", "时间表设置");
		m.put("Timesheets", "时间表");
		m.put("Timing", "时间");
		m.put("Title", "标题");
		m.put("To Everyone", "对所有人");
		m.put("To State", "到状态");
		m.put("To States", "到状态");
		m.put("To authenticate via internal database, <a wicket:id=\"setPasswordForUser\">set password for user</a> or <a wicket:id=\"tellUserToResetPassword\">tell user to reset password</a>", 
			"通过内部数据库进行身份验证，<a wicket:id=\"setPasswordForUser\">设置用户密码</a> 或 <a wicket:id=\"tellUserToResetPassword\">告诉用户重置密码</a>");
		m.put("To avoid duplication, estimated/remaining time showing here does not include those aggregated from \"{0}\"", 
			"为避免重复，此处显示的估计/剩余时间不包括从 \"{0}\" 聚合的时间");
		m.put("To avoid duplication, spent time showing here does not include those aggregated from \"{0}\"", 
			"为避免重复，这里显示的时间不包括从 \"{0}\" 聚合的时间");
		m.put("Toggle change history", "切换更改历史");
		m.put("Toggle comments", "切换评论");
		m.put("Toggle commits", "切换提交");
		m.put("Toggle dark mode", "切换暗模式");
		m.put("Toggle detail message", "切换详细信息");
		m.put("Toggle fixed width font", "切换固定宽度字体");
		m.put("Toggle full screen", "切换全屏");
		m.put("Toggle matched contents", "切换匹配内容");
		m.put("Toggle navigation", "切换导航");
		m.put("Toggle work log", "切换工作日志");
		m.put("Tokens", "令牌");
		m.put("Too many commits to load", "加载的提交太多");
		m.put("Too many commits, displaying recent {0}", "提交太多，显示最近的 {0}");
		m.put("Too many log entries, displaying recent {0}", "日志条目太多，显示最近的 {0}");
		m.put("Too many problems, displaying first {0}", "问题太多，仅显示前 {0} 个");
		m.put("Toomanyrequests", "请求过多");
		m.put("Top", "顶部");
		m.put("Topo", "拓扑");
		m.put("Total Heap Memory", "总堆内存");
		m.put("Total Number", "总数");
		m.put("Total Problems", "总共问题");
		m.put("Total Size", "总大小");
		m.put("Total Test Duration", "总测试持续时间");
		m.put("Total estimated time", "总预计时间");
		m.put("Total spent time", "总已用时间");
		m.put("Total spent time / total estimated time", "总已用时间 / 总预计时间");
		m.put("Total time", "总时间");
		m.put("Total:", "总计");
		m.put("Touched File", "触及的文件");
		m.put("Touched Files", "触及的文件");
		m.put("Transfer LFS Files", "传输 LFS 文件");
		m.put("Transit manually", "手动转换");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "将工单 \"{0}\" 的状态转换为 \"{1}\"（{2}）");
		m.put("Transition Edit Bean", "转换编辑 Bean");
		m.put("Transition Spec", "转换规范");
		m.put("Trial Expiration Date", "试用到期日期");
		m.put("Trial subscription key not applicable for this installation", "试用订阅密钥不适用于此安装");
		m.put("Triggers", "触发器");
		m.put("Trivy Container Image Scanner", "Trivy 容器镜像扫描器");
		m.put("Trivy Filesystem Scanner", "Trivy 文件系统扫描器");
		m.put("Trivy Rootfs Scanner", "Trivy 根文件系统扫描器");
		m.put("Try EE", "试用企业版");
		m.put("Try Enterprise Edition", "试用企业版");
		m.put("Twitch", "Twitch");
		m.put("Two Factor Authentication", "两阶段验证");
		m.put("Two-factor Authentication", "两阶段验证");
		m.put("Two-factor authentication already set up. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Request to set up again", 
			"两阶段验证已设置。<a wicket:id=\"requestToSetupAgain\"><wicket:t>请求重新设置");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"两阶段验证已启用。请输入显示在您的 TOTP 身份验证器上的代码。如果您遇到问题，请确保 OneDev 服务器和您的设备运行 TOTP 身份验证器的时间同步");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"为增强安全性，您的账户已强制启用两阶段验证。请按照以下步骤进行设置");
		m.put("Two-factor authentication is now configured", "两阶段验证现已配置完成");
		m.put("Two-factor authentication not enabled", "两阶段验证未启用");
		m.put("Type", "类型");
		m.put("Type <code>yes</code> below to cancel all queried builds", "在下面输入 <code>yes</code> 以取消所有查询的构建");
		m.put("Type <code>yes</code> below to cancel selected builds", "在下面输入 <code>yes</code> 以取消选定的构建");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "在下方输入 <code>yes</code> 确认删除所有查询的用户");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "在下方输入 <code>yes</code> 确认删除选定的用户");
		m.put("Type <code>yes</code> below to copy all queried issues to project \"{0}\"", "在下方输入 <code>yes</code> 以将所有查询的工单复制到项目 \"{0}\"");
		m.put("Type <code>yes</code> below to copy selected issues to project \"{0}\"", "在下方输入 <code>yes</code> 以将选定的工单复制到项目 \"{0}\"");
		m.put("Type <code>yes</code> below to delete all queried builds", "在下面输入 <code>yes</code> 以删除所有查询的构建");
		m.put("Type <code>yes</code> below to delete all queried comments", "在下面输入 <code>yes</code> 以删除所有查询的评论");
		m.put("Type <code>yes</code> below to delete all queried issues", "在下方输入 <code>yes</code> 以删除所有查询的工单");
		m.put("Type <code>yes</code> below to delete all queried packages", "在下方输入 <code>yes</code> 以删除所有查询到的包");
		m.put("Type <code>yes</code> below to delete all queried projects", "在下方输入 <code>yes</code> 以删除所有查询到的项目");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "在下方输入 <code>yes</code> 以删除所有查询的合并请求");
		m.put("Type <code>yes</code> below to delete selected builds", "在下面输入 <code>yes</code> 以删除选定的构建");
		m.put("Type <code>yes</code> below to delete selected comments", "在下方输入 <code>yes</code> 以删除选中的评论");
		m.put("Type <code>yes</code> below to delete selected issues", "在下方输入 <code>yes</code> 以删除选定的工单");
		m.put("Type <code>yes</code> below to delete selected packages", "在下方输入 <code>yes</code> 以删除选中的包");
		m.put("Type <code>yes</code> below to delete selected projects", "在下方输入 <code>yes</code> 以删除选中的项目");
		m.put("Type <code>yes</code> below to delete selected pull requests", "在下方输入 <code>yes</code> 以删除选定的合并请求");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "在下方输入 <code>yes</code> 以放弃所有查询的合并请求");
		m.put("Type <code>yes</code> below to discard selected pull requests", "在下方输入 <code>yes</code> 以放弃选定的合并请求");
		m.put("Type <code>yes</code> below to move all queried issues to project \"{0}\"", "在下方输入 <code>yes</code> 以将所有查询的工单移动到项目 \"{0}\"");
		m.put("Type <code>yes</code> below to move all queried projects to be under \"{0}\"", "在下方输入 <code>yes</code> 以将所有查询到的项目移动到 \"{0}\" 下");
		m.put("Type <code>yes</code> below to move selected issues to project \"{0}\"", "在下方输入 <code>yes</code> 以将选定的工单移动到项目 \"{0}\"");
		m.put("Type <code>yes</code> below to move selected projects to be under \"{0}\"", "在下方输入 <code>yes</code> 以将选中的项目移动到 \"{0}\" 下");
		m.put("Type <code>yes</code> below to pause all queried agents", "在下面输入 <code>yes</code> 确认暂停所有查询的代理");
		m.put("Type <code>yes</code> below to re-run all queried builds", "在下面输入 <code>yes</code> 以重新运行所有查询的构建");
		m.put("Type <code>yes</code> below to re-run selected builds", "在下面输入 <code>yes</code> 以重新运行选定的构建");
		m.put("Type <code>yes</code> below to remove all queried users from group", "在下方输入<code>yes</code>以从组中移除所有查询的用户");
		m.put("Type <code>yes</code> below to remove from all queried groups", "在下方输入<code>yes</code>以从所有查询的组中移除");
		m.put("Type <code>yes</code> below to remove from selected groups", "在下方输入<code>yes</code>以从选定的组中移除");
		m.put("Type <code>yes</code> below to remove selected users from group", "在下方输入<code>yes</code>以从组中移除选定用户");
		m.put("Type <code>yes</code> below to restart all queried agents", "在下面输入 <code>yes</code> 确认重启所有查询的代理");
		m.put("Type <code>yes</code> below to restart selected agents", "在下面输入 <code>yes</code> 确认重启选定的代理");
		m.put("Type <code>yes</code> below to resume all queried agents", "在下面输入 <code>yes</code> 确认恢复所有查询的代理");
		m.put("Type <code>yes</code> below to set all queried as root projects", "在下方输入 <code>yes</code> 以将所有查询到的项目设置为根项目");
		m.put("Type <code>yes</code> below to set selected as root projects", "在下方输入 <code>yes</code> 以将选中的项目设置为根项目");
		m.put("Type password here", "在此输入密码");
		m.put("Type to filter", "过滤");
		m.put("Type to filter...", "输入以筛选...");
		m.put("URL", "URL");
		m.put("Unable To Delete/Disable Right Now", "目前无法删除/禁用");
		m.put("Unable to apply change as otherwise you will not be able to manage this project", "无法应用更改，否则您将无法管理此项目");
		m.put("Unable to change password as you are authenticating via external system", "无法更改密码，因为您正在通过外部系统进行认证");
		m.put("Unable to comment", "无法添加评论");
		m.put("Unable to connect to server", "无法连接到服务器");
		m.put("Unable to create protected branch", "无法创建受保护的分支");
		m.put("Unable to create protected tag", "无法创建受保护的标签");
		m.put("Unable to diff as some line is too long.", "无法比较，因为某些行太长");
		m.put("Unable to diff as the file is too large.", "无法比较，因为文件太大");
		m.put("Unable to find SSO provider: ", "无法找到 SSO 提供方：");
		m.put("Unable to find agent {0}", "无法找到代理 {0}");
		m.put("Unable to find build #{0} in project {1}", "在项目 {1} 中找不到构建 #{0}");
		m.put("Unable to find commit to import build spec (import project: {0}, import revision: {1})", 
			"无法找到要导入的构建规范的提交（导入项目：{0}，导入版本：{1}）");
		m.put("Unable to find issue #{0} in project {1}", "在项目 {1} 中找不到工单 #{0}");
		m.put("Unable to find project to import build spec: {0}", "无法找到要导入的构建规范的项目：{0}");
		m.put("Unable to find pull request #{0} in project {1}", "在项目 {1} 中找不到合并请求 #{0}");
		m.put("Unable to find timesheet: ", "找不到时间表：");
		m.put("Unable to get guilds info", "无法获取 guilds 信息");
		m.put("Unable to import build spec (import project: {0}, import revision: {1}): {2}", "无法导入构建规范（导入项目：{0}，导入版本：{1}）：{2}");
		m.put("Unable to notify user as mail service is not configured", "无法通知用户，因为邮件服务未配置");
		m.put("Unable to send password reset email as mail service is not configured", "由于邮件服务未配置，无法发送密码重置邮件");
		m.put("Unable to send verification email as mail service is not configured yet", "无法发送验证电子邮件，因为邮件服务未配置");
		m.put("Unauthorize this user", "取消此用户的授权");
		m.put("Unauthorized", "未授权");
		m.put("Undefined", "未定义");
		m.put("Undefined Field Resolution", "未定义字段解析");
		m.put("Undefined Field Value Resolution", "未定义字段值解析");
		m.put("Undefined State Resolution", "未定义状态解析");
		m.put("Undefined custom field: ", "未定义自定义字段：");
		m.put("Under which condition this step should run. <b>SUCCESSFUL</b> means all non-optional steps running before this step are successful", 
			"此步骤应在何种条件下运行。<b>SUCCESSFUL</b> 表示在此步骤之前运行的所有非可选步骤均已成功");
		m.put("Unexpected setting: {0}", "未知设置：{0}");
		m.put("Unexpected ssh signature hash algorithm: ", "意外的 ssh 签名哈希算法：");
		m.put("Unexpected ssh signature namespace: ", "意外的 ssh 签名命名空间：");
		m.put("Unified", "统一");
		m.put("Unified view", "统一视图");
		m.put("Unit Test Statistics", "单元测试统计");
		m.put("Unlimited", "无限制");
		m.put("Unlink this issue", "取消链接此工单");
		m.put("Unordered List", "无序列表");
		m.put("Unordered list", "无序列表");
		m.put("Unpin this issue", "取消置顶此工单");
		m.put("Unresolved", "未解决");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "将项目 \"{1}\" 的文件 \"{0}\" 上的评论标记为未解决");
		m.put("Unscheduled", "未计划");
		m.put("Unscheduled Issues", "未计划工单");
		m.put("Unsolicited OIDC authentication response", "未主动请求的 OIDC 认证响应");
		m.put("Unsolicited OIDC response", "未主动请求的 OIDC 响应");
		m.put("Unsolicited discord api response", "未主动请求的 discord api 响应");
		m.put("Unspecified", "未指定");
		m.put("Unsupported", "不支持");
		m.put("Unsupported ssh signature algorithm: ", "不支持的 ssh 签名算法：");
		m.put("Unsupported ssh signature version: ", "不支持的 ssh 签名版本：");
		m.put("Unverified", "未验证");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "未验证的电子邮件地址不适用于上述功能");
		m.put("Unvote", "取消投票");
		m.put("Unwatched. Click to watch", "未关注。点击关注");
		m.put("Update", "更新");
		m.put("Update Dependencies via Renovate", "通过 Renovate 更新依赖");
		m.put("Update Source Branch", "更新源分支");
		m.put("Update body", "更新主体");
		m.put("Upload", "上传");
		m.put("Upload Access Token Secret", "上传访问令牌密钥");
		m.put("Upload Cache", "上传缓存");
		m.put("Upload Files", "上传文件");
		m.put("Upload Project Path", "上传项目路径");
		m.put("Upload Strategy", "上传策略");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "上传一个 128x128 的透明 PNG 文件，用作暗模式下的标志");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "上传一个 128x128 的透明 PNG 文件，用作亮模式下的标志");
		m.put("Upload artifacts", "上传制品");
		m.put("Upload avatar", "上传头像");
		m.put("Upload should be less than {0} Mb", "上传应该小于 {0} Mb");
		m.put("Upload to Project", "上传到项目");
		m.put("Uploaded Caches", "上传的缓存");
		m.put("Uploading file", "上传文件");
		m.put("Url", "URL");
		m.put("Use '*' for wildcard match", "使用 '*' 进行通配符匹配");
		m.put("Use '*' or '?' for wildcard match. Prefix with '-' to exclude", "使用 '*' 或 '?' 进行通配符匹配。前缀 '-' 排除");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"使用 '**'、'*' 或 '?' 进行<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"使用 '**', '*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀为 '-' 以排除");
		m.put("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"使用 '**'、'*' 或 '?' 进行<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>");
		m.put("Use '\\' to escape brackets", "使用 '\\' 转义括号");
		m.put("Use '\\' to escape quotes", "使用 '\\' 转义引号");
		m.put("Use @@ to reference scope in job commands to avoid being interpreted as variable", "使用 @@ 引用任务命令中的范围以避免被解释为变量");
		m.put("Use Avatar Service", "使用头像服务");
		m.put("Use Default", "使用默认");
		m.put("Use Default Boards", "使用默认看板");
		m.put("Use For Git Operations", "用于 Git 操作");
		m.put("Use Git in System Path", "使用系统路径中的 Git");
		m.put("Use Hours And Minutes Only", "仅使用小时和分钟");
		m.put("Use Specified Git", "使用指定的 Git");
		m.put("Use Specified curl", "使用指定的 curl");
		m.put("Use Step Template", "使用步骤模板");
		m.put("Use curl in System Path", "使用系统路径中的 curl");
		m.put("Use default", "使用默认");
		m.put("Use default storage class", "使用默认存储类");
		m.put("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages", 
			"使用任务令牌作为用户名，以便 OneDev 知道哪个构建是 ${permission.equals(\"write\")? \"deploying\": \"using\"} 包");
		m.put("Use job token to tell OneDev the build publishing the package", "使用任务令牌告诉 OneDev 哪个构建正在发布包");
		m.put("Use job token to tell OneDev the build pushing the chart", "使用任务令牌告诉 OneDev 哪个构建正在推送 Chart");
		m.put("Use job token to tell OneDev the build pushing the package", "使用任务令牌告诉 OneDev 哪个构建正在推送包");
		m.put("Use job token to tell OneDev the build using the package", "使用任务令牌告诉 OneDev 哪个构建正在使用包");
		m.put("Use project dependency to retrieve artifacts from other projects", "使用项目依赖从其他项目拷贝制品");
		m.put("Use specified choices", "使用指定的选项");
		m.put("Use specified default value", "使用指定的默认值");
		m.put("Use specified value or job secret", "使用指定的值或任务密钥");
		m.put("Use specified values or job secrets", "使用指定的值或任务密钥");
		m.put("Use triggers to run the job automatically under certain conditions", "使用触发器在特定条件下自动运行任务");
		m.put("Use value of specified parameter/secret", "使用指定参数/密钥的值");
		m.put("Used Heap Memory", "已用堆内存");
		m.put("User", "用户");
		m.put("User \"{0}\" unauthorized", "用户 \"{0}\" 已取消授权");
		m.put("User Authorization Bean", "用户授权 Bean");
		m.put("User Authorizations", "用户授权");
		m.put("User Authorizations Bean", "用户授权 Bean");
		m.put("User Count", "用户数量");
		m.put("User Email Attribute", "用户电子邮件属性");
		m.put("User Full Name Attribute", "用户全名属性");
		m.put("User Groups Attribute", "用户组属性");
		m.put("User Invitation", "用户邀请");
		m.put("User Invitation Template", "用户邀请模板");
		m.put("User Management", "用户管理");
		m.put("User Match Criteria", "用户匹配条件");
		m.put("User Name", "用户名");
		m.put("User Principal Name", "用户主体名称");
		m.put("User Profile", "用户个人资料");
		m.put("User SSH Key Attribute", "用户 SSH 密钥属性");
		m.put("User Search Bases", "用户搜索根节点");
		m.put("User Search Filter", "用户搜索过滤器");
		m.put("User added to group", "用户已添加到组");
		m.put("User authorizations updated", "用户授权已更新");
		m.put("User authorized", "用户已授权");
		m.put("User avatar will be requested by appending a hash to this url", "用户头像将通过在此 URL 后附加哈希值来请求");
		m.put("User can sign up if this option is enabled", "如果启用此选项，用户可以注册");
		m.put("User disabled", "用户已禁用");
		m.put("User name", "用户名");
		m.put("User name already used by another account", "用户名已被另一个账户使用");
		m.put("Users", "用户");
		m.put("Users converted to service accounts successfully", "用户成功转换为服务账户");
		m.put("Users deleted successfully", "用户删除成功");
		m.put("Users disabled successfully", "用户禁用成功");
		m.put("Users enabled successfully", "用户启用成功");
		m.put("Utilities", "工具");
		m.put("Valid signature required for head commit of this branch per branch protection rule", 
			"此分支的头部提交需要有效的签名，受分支保护规则限制");
		m.put("Value", "值");
		m.put("Value Matcher", "值匹配器");
		m.put("Value Provider", "值提供者");
		m.put("Values", "值");
		m.put("Values Provider", "值提供者");
		m.put("Variable", "变量");
		m.put("Verification Code", "验证码");
		m.put("Verification email sent, please check it", "验证电子邮件已发送，请检查");
		m.put("Verify", "验证");
		m.put("View", "查看");
		m.put("View Source", "查看源码");
		m.put("View source", "查看源代码");
		m.put("View statistics", "查看统计信息");
		m.put("Viewer", "查看者");
		m.put("Volume Mount", "卷挂载");
		m.put("Volume Mounts", "卷挂载");
		m.put("Vote", "投票");
		m.put("Votes", "投票");
		m.put("WAITING", "等待");
		m.put("WARNING:", "警告：");
		m.put("Waiting", "等待");
		m.put("Waiting for approvals", "等待审批");
		m.put("Waiting for test mail to come back...", "等待测试邮件返回...");
		m.put("Watch", "观察");
		m.put("Watch Status", "订阅状态");
		m.put("Watch if involved", "若参与则观察");
		m.put("Watch if involved (default)", "若参与则观察（默认）");
		m.put("Watch status changed", "监视状态已更改");
		m.put("Watch/Unwatch All Queried Issues", "关注/取消关注所有查询的工单");
		m.put("Watch/Unwatch All Queried Pull Requests", "监视/取消监视所有查询的合并请求");
		m.put("Watch/Unwatch Selected Pull Requests", "监视/取消监视选定的合并请求");
		m.put("Watched. Click to unwatch", "已关注。点击取消关注");
		m.put("Watchers", "观察者");
		m.put("Web Hook", "Web Hook");
		m.put("Web Hooks", "Web Hooks");
		m.put("Web Hooks Bean", "Web Hooks Bean");
		m.put("Web hooks saved", "Web hooks 已保存");
		m.put("Webhook Url", "Webhook URL");
		m.put("Week", "周");
		m.put("When", "何时");
		m.put("When authorize a group, the group will also be authorized with the role for all child projects", 
			"当授权一个组时，所有子项目也同时被授权");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"当授权一个项目时，所有子项目也将被授权分配的角色");
		m.put("When authorize a user, the user will also be authorized with the role for all child projects", 
			"当授权一个用户时，所有子项目也同时被授权");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"确定用户是否是 git 提交的作者/提交者时，将检查此处列出的所有电子邮件");
		m.put("When evaluating this template, below variables will be available:", "使用此模板时，以下变量将可用：");
		m.put("When login via OneDev's built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database", 
			"通过 OneDev 的内置表单登录时，提交的用户凭据可以在此处定义的认证器以及内部数据库中进行检查");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"当合并请求的目标分支有新提交时，合并请求的合并提交将被重新计算，此选项决定是否接受在之前合并提交上运行的合并请求构建。如果启用，您需要在新的合并提交上重新运行所需的构建。此设置仅在指定了所需构建时生效");
		m.put("When this work starts", "此工作开始时");
		m.put("When {0}", "当 {0} 时");
		m.put("Whether or not created issue should be confidential", "创建的工单是否应为机密");
		m.put("Whether or not multiple issues can be linked", "是否可以链接多个工单");
		m.put("Whether or not multiple issues can be linked on the other side. For instance sub issues on the other side means parent issue, and multiple should be false on that side if only one parent is allowed", 
			"另一侧是否可以链接多个工单。例如，另一侧的子工单表示父工单，如果只允许一个父工单，则该侧应为 false");
		m.put("Whether or not multiple values can be specified for this field", "此字段是否可以指定多个值");
		m.put("Whether or not multiple values can be specified for this param", "此参数是否可以指定多个值");
		m.put("Whether or not the issue should be confidential", "工单是否应为机密");
		m.put("Whether or not the link is asymmetric. A asymmetric link has different meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric", 
			"链接是否为非对称的。非对称链接在不同侧具有不同的含义。例如，“父子”链接是非对称的，而“相关”链接是对称的");
		m.put("Whether or not this field accepts empty value", "此字段是否接受空值");
		m.put("Whether or not this param accepts empty value", "此参数是否接受空值");
		m.put("Whether or not this script can be used in CI/CD jobs", "此脚本是否可用于 CI/CD 任务");
		m.put("Whether or not this step is optional. Execution failure of an optional step will not cause the build to fail, and successful condition of subsequent steps will not take optional step into account", 
			"是否此步骤是可选的。如果可选步骤失败，不会导致构建失败，后续步骤的执行条件也不会将可选步骤考虑在内");
		m.put("Whether or not to allow anonymous users to access this server", "是否允许匿名用户访问此服务器");
		m.put("Whether or not to allow creating root projects (project without parent)", "是否允许创建根项目（无父项目的项目）");
		m.put("Whether or not to also include children of above projects", "是否同时包含上述项目的子项目");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same machine", 
			"是否在运行容器或构建镜像时始终拉取镜像。应启用此选项，以避免同一机器上运行的恶意任务替换镜像");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same node", 
			"是否在运行容器或构建镜像时始终拉取镜像。应启用此选项，以避免同一节点上运行的恶意任务替换镜像");
		m.put("Whether or not to be able to access time tracking info of issues", "是否能够访问工单的时间跟踪信息");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities", 
			"是否创建为任务自动化目的的服务账户。服务账户没有密码和电子邮件地址，也不会为其活动生成通知");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"是否创建为任务自动化目的的服务账户。服务账户没有密码和电子邮件地址，也不会为其活动生成通知。<b class='text-warning'>注意：</b>服务账户是企业版功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a> 30 天");
		m.put("Whether or not to enable code management for the project", "是否为项目启用代码管理");
		m.put("Whether or not to enable issue management for the project", "是否为项目启用工单管理");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project.", 
			"如果启用此选项，git lfs 命令需要安装在 OneDev 服务器上（即使此步骤在其他节点上运行）");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project. If this option is enabled, git lfs command needs to be installed on OneDev server", 
			"如果启用此选项，git lfs 命令需要安装在 OneDev 服务器上（即使此步骤在其他节点上运行）");
		m.put("Whether or not to import forked Bitbucket repositories", "是否导入分叉的 Bitbucket 仓库");
		m.put("Whether or not to import forked GitHub repositories", "是否导入分叉的 GitHub 仓库");
		m.put("Whether or not to import forked GitLab projects", "是否导入分叉的 GitLab 项目");
		m.put("Whether or not to import forked Gitea repositories", "是否导入分叉的 Gitea 仓库");
		m.put("Whether or not to include forked repositories", "是否包含分叉的仓库");
		m.put("Whether or not to include this field when issue is initially opened. If not, you may include this field later when issue is transited to other states via issue transition rule", 
			"在工单首次打开时是否包含此字段。如果不包含，您可以在工单通过工单转换规则转换到其他状态时再包含此字段");
		m.put("Whether or not to input and display estimated/spent time in hours/minutes only", "是否仅以小时/分钟输入和显示估计/耗费时间");
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. Make sure this executor can only be used by trusted jobs if this option is enabled", 
			"是否将 docker sock 挂载到作业容器中以支持作业命令中的 docker 操作<br><b class='text-danger'>警告</b>：恶意作业可以通过操作挂载的 docker sock 控制整个 OneDev。请确保如果启用此选项，此执行器仅能被可信的作业使用");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"是否在下一页预填充标签映射。如果要显示的标签过多，您可能希望禁用此选项");
		m.put("Whether or not to require this dependency to be successful", "是否要求此依赖成功");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"是否查询登录用户的组。如果启用此选项，请确保通过在 Entra ID 中注册的应用程序的令牌配置添加组声明。在这种情况下，组声明应通过各种令牌类型返回组 ID（默认选项）");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"是否获取子模块。请参阅 <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>此教程</a>，了解如何设置上述克隆凭据以获取子模块");
		m.put("Whether or not to run this step inside container", "是否在容器内运行此步骤");
		m.put("Whether or not to scan recursively in above paths", "是否在上述路径中递归扫描");
		m.put("Whether or not to send notifications for events generated by yourself", "是否为自身生成的事件发送通知");
		m.put("Whether or not to send notifications to issue watchers for this change", "是否为此更改向工单观察者发送通知");
		m.put("Whether or not to show branch/tag column", "是否显示分支/标签列");
		m.put("Whether or not to show duration column", "是否显示持续时间列");
		m.put("Whether or not to use user avatar from a public service", "是否使用公共服务提供的用户头像");
		m.put("Whether or not use force option to overwrite changes in case ref updating can not be fast-forwarded", 
			"在引用更新无法快进的情况下，是否使用强制选项覆盖更改");
		m.put("Whether or not user can remove own account", "用户是否可以删除自己的账户");
		m.put("Whether the password must contain at least one lowercase letter", "密码是否必须至少包含一个小写字母");
		m.put("Whether the password must contain at least one number", "密码是否必须至少包含一个数字");
		m.put("Whether the password must contain at least one special character", "密码是否必须至少包含一个特殊字符");
		m.put("Whether the password must contain at least one uppercase letter", "密码是否必须至少包含一个大写字母");
		m.put("Whole Word", "全词");
		m.put("Widget", "小组件");
		m.put("Widget Tab", "小组件 Tab");
		m.put("Widget Timesheet Setting", "小组件时间表设置");
		m.put("Will be prompted to set up two-factor authentication upon next login", "下次登录时将提示设置两阶段验证");
		m.put("Will be transcoded to UTF-8", "将被转码为 UTF-8");
		m.put("Window", "窗口");
		m.put("Window Memory", "窗口内存");
		m.put("With current number of users ({0}), the subscription will be active until <b>{1}</b>", 
			"根据当前用户数（{0}），订阅将有效至<b>{1}</b>");
		m.put("Workflow reconciliation completed", "工单工作流一致性检查完成");
		m.put("Working Directory", "工作目录");
		m.put("Write", "写入");
		m.put("YAML", "YAML");
		m.put("Yes", "是");
		m.put("You are not member of discord server", "您不是 discord 服务器的成员");
		m.put("You are rebasing source branch on top of target branch", "您正在将源分支 rebase 到目标分支之上");
		m.put("You are viewing a subset of all changes. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">show all changes</a>", 
			"您正在查看部分更改。<a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">显示所有更改</a>");
		m.put("You can also achieve this by adding a build docker image step to your CI/CD job and configuring the built-in registry login with an access token secret that has package write permissions", 
			"您还可以通过添加一个构建 docker 镜像步骤到您的 CI/CD 任务，并配置内置注册表登录，使用具有包写权限的访问令牌密钥来实现");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "您有未验证的 <a wicket:id=\"hasUnverifiedLink\">电子邮件地址</a>");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "您也可以将文件/图像拖到输入框中，或从剪贴板粘贴图像");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"您可以通过 <a wicket:id=\"addFiles\" class=\"link-primary\">添加文件</a>、<a wicket:id=\"setupBuildSpec\" class=\"link-primary\">定义构建规范</a> 或 <a wicket:id=\"pushInstructions\" class=\"link-primary\">推送现有仓库</a> 来初始化项目");
		m.put("You selected to delete branch \"{0}\"", "您选择删除分支 \"{0}\"");
		m.put("You will be notified of any activities", "您将收到任何活动的通知");
		m.put("You've been logged out", "您已登出");
		m.put("YouTrack API URL", "YouTrack API URL");
		m.put("YouTrack Issue Field", "YouTrack 工单字段");
		m.put("YouTrack Issue Link", "YouTrack 工单链接");
		m.put("YouTrack Issue State", "YouTrack 工单状态");
		m.put("YouTrack Issue Tag", "YouTrack 工单标签");
		m.put("YouTrack Login Name", "YouTrack 登录名");
		m.put("YouTrack Password or Access Token", "YouTrack 密码或访问令牌");
		m.put("YouTrack Project", "YouTrack 项目");
		m.put("YouTrack Projects to Import", "要导入的 YouTrack 项目");
		m.put("Your email address is now verified", "您的邮箱地址已验证");
		m.put("Your primary email address is not verified", "您的主要邮箱地址未验证");
		m.put("[Any state]", "[任何状态]");
		m.put("[Reset Password] Please Reset Your OneDev Password", "[重置密码] 请重置您的 OneDev 密码");
		m.put("a boolean indiciating whether or not topic comment can be created directly by replying the email", 
			"一个布尔值，表示是否可以通过回复邮件直接创建主题评论");
		m.put("a new agent token will be generated each time this button is pressed", "每次按下此按钮时，将生成一个新的代理令牌");
		m.put("a string representing body of the event. May be <code>null</code>", "表示事件正文的字符串。可能为 <code>null</code>");
		m.put("a string representing event detail url", "表示事件详情 url 的字符串");
		m.put("a string representing summary of the event", "表示事件摘要的字符串");
		m.put("access [{0}]", "访问 [{0}]");
		m.put("active", "活跃");
		m.put("add another order", "添加另一个排序");
		m.put("adding .onedev-buildspec.yml", "添加 .onedev-buildspec.yml");
		m.put("after specified date", "在指定日期之后");
		m.put("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information.  A <code>null</code> value means that the notification can not be unsubscribed", 
			"一个包含取消订阅信息的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>对象</a>。<code>null</code> 值表示该通知无法取消订阅");
		m.put("and more", "更多");
		m.put("archived", "已归档");
		m.put("artifacts", "制品");
		m.put("assign to me", "分配给我");
		m.put("authored by", "作者是");
		m.put("backlog ", "待办");
		m.put("base", "基准");
		m.put("before specified date", "在指定日期之前");
		m.put("branch the build commit is merged into", "构建提交合并到的分支");
		m.put("branch the job is running against", "任务运行的分支");
		m.put("branch {0}", "分支 {0}");
		m.put("branches", "分支");
		m.put("build", "构建");
		m.put("build is successful for any job and branch", "任何任务和分支的构建都成功");
		m.put("build is successful for any job on branches \"{0}\"", "分支 \"{0}\" 上的任何任务构建都成功");
		m.put("build is successful for jobs \"{0}\" on any branch", "任何分支上的任务 \"{0}\" 构建都成功");
		m.put("build is successful for jobs \"{0}\" on branches \"{1}\"", "分支 \"{1}\" 上的任务 \"{0}\" 构建都成功");
		m.put("builds", "构建");
		m.put("cURL Example", "cURL示例");
		m.put("choose a color for this state", "选择一个颜色");
		m.put("cluster:lead", "主节点");
		m.put("cmd-k to show command palette", "cmd-k 显示命令面板");
		m.put("code commit", "代码提交");
		m.put("code is committed", "代码已提交");
		m.put("code is committed to branches \"{0}\"", "代码提交到分支 \"{0}\"");
		m.put("code is committed to branches \"{0}\" with message \"{1}\"", "代码已提交到分支 \"{0}\"，提交信息为 \"{1}\"");
		m.put("code is committed with message \"{0}\"", "代码已提交，提交信息为 \"{0}\"");
		m.put("commit message contains", "提交信息包含");
		m.put("commits", "提交");
		m.put("committed by", "提交者是");
		m.put("common", "公共");
		m.put("common ancestor", "共同祖先");
		m.put("container:image", "容器镜像");
		m.put("copy", "复制");
		m.put("ctrl-k to show command palette", "ctrl-k 显示命令面板");
		m.put("curl Command Line", "curl 命令行");
		m.put("curl Path", "curl 路径");
		m.put("default", "默认");
		m.put("descending", "降序");
		m.put("disabled", "禁用");
		m.put("does not have any value of", "没有下列任何值");
		m.put("duration", "持续时间");
		m.put("enclose with ~ to query hash/message", "使用 ~ 包围以查询哈希/提交信息");
		m.put("enclose with ~ to query job/version", "使用 ~ 包围以查询任务/版本");
		m.put("enclose with ~ to query name/ip/os", "使用 ~ 包围以查询名称/IP/操作系统");
		m.put("enclose with ~ to query name/path", "使用 ~ 包围以查询名称/路径");
		m.put("enclose with ~ to query name/version", "使用 ~ 包围以查询名称/版本");
		m.put("enclose with ~ to query path/content/reply", "使用 ~ 包围以查询路径/内容/回复");
		m.put("enclose with ~ to query title/description/comment", "使用 ~ 包围以查询标题/描述/评论");
		m.put("exclude", "排除");
		m.put("false", "假");
		m.put("files with ext \"{0}\"", "具有扩展名 \"{0}\" 的文件");
		m.put("find build by number", "通过编号查找构建");
		m.put("find build with this number", "查找具有此编号的构建");
		m.put("find issue by number", "通过编号查找工单");
		m.put("find pull request by number", "通过编号查找合并请求");
		m.put("find pull request with this number", "查找具有此编号的合并请求");
		m.put("forked from <a wicket:id=\"forkedFrom\"></a>", "从 <a wicket:id=\"forkedFrom\"></a> 分叉");
		m.put("found 1 agent", "找到 1 个代理");
		m.put("found 1 build", "找到 1 个构建");
		m.put("found 1 comment", "找到 1 条评论");
		m.put("found 1 issue", "找到 1 个工单");
		m.put("found 1 package", "找到 1 个包");
		m.put("found 1 project", "找到 1 个项目");
		m.put("found 1 pull request", "找到 1 个合并请求");
		m.put("found 1 user", "找到 1 个用户");
		m.put("found {0} agents", "找到 {0} 个代理");
		m.put("found {0} builds", "找到 {0} 个构建");
		m.put("found {0} comments", "找到 {0} 条评论");
		m.put("found {0} issues", "找到 {0} 个工单");
		m.put("found {0} packages", "找到 {0} 个包");
		m.put("found {0} projects", "找到 {0} 个项目");
		m.put("found {0} pull requests", "找到 {0} 个合并请求");
		m.put("found {0} users", "找到 {0} 个用户");
		m.put("has any value of", "具有下列任何值");
		m.put("head", "头部");
		m.put("in current commit", "在当前提交中");
		m.put("ineffective", "无效");
		m.put("inherited", "继承");
		m.put("initial", "初始");
		m.put("is empty", "为空");
		m.put("is not empty", "不为空");
		m.put("issue", "工单");
		m.put("issue:Number", "工单号");
		m.put("issues", "工单");
		m.put("job", "任务");
		m.put("key ID: ", "密钥 ID:");
		m.put("lines", "行");
		m.put("link:Multiple", "允许链接多个工单");
		m.put("log", "日志");
		m.put("manage job", "管理任务");
		m.put("markdown:heading", "标题");
		m.put("markdown:image", "图片");
		m.put("may not be empty", "不能为空");
		m.put("merged", "已合并");
		m.put("month:Apr", "四月");
		m.put("month:Aug", "八月");
		m.put("month:Dec", "十二月");
		m.put("month:Feb", "二月");
		m.put("month:Jan", "一月");
		m.put("month:Jul", "七月");
		m.put("month:Jun", "六月");
		m.put("month:Mar", "三月");
		m.put("month:May", "五月");
		m.put("month:Nov", "十一月");
		m.put("month:Oct", "十月");
		m.put("month:Sep", "九月");
		m.put("n/a", "不适用");
		m.put("new field", "新字段");
		m.put("no activity for {0} days", "{0} 天内无活动");
		m.put("on file {0}", "在文件 {0} 上");
		m.put("opened", "已创建");
		m.put("opened <span wicket:id=\"submitDate\"></span>", "于 <span wicket:id=\"submitDate\"></span> 创建");
		m.put("or match another value", "或匹配另一个值");
		m.put("order more", "订购更多");
		m.put("outdated", "过时");
		m.put("pack", "包");
		m.put("package", "包");
		m.put("packages", "包");
		m.put("personal", "个人");
		m.put("pipeline", "流水线");
		m.put("project of the running job", "任务运行的项目");
		m.put("property", "属性");
		m.put("pull request", "合并请求");
		m.put("pull request #{0}", "合并请求 #{0}");
		m.put("pull request and code review", "合并请求和代码评审");
		m.put("pull request to any branch is discarded", "任何分支的合并请求被放弃");
		m.put("pull request to any branch is merged", "任何分支的合并请求被合并");
		m.put("pull request to any branch is opened", "任何分支的合并请求被创建");
		m.put("pull request to branches \"{0}\" is discarded", "分支 \"{0}\" 的合并请求被放弃");
		m.put("pull request to branches \"{0}\" is merged", "分支 \"{0}\" 的合并请求被合并");
		m.put("pull request to branches \"{0}\" is opened", "分支 \"{0}\" 的合并请求被创建");
		m.put("pull requests", "合并请求");
		m.put("reconciliation (need administrator permission)", "一致性检查（需要管理员权限）");
		m.put("reports", "报告");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> object to be notified", 
			"表示要通知的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>构建</a> 对象");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk", 
			"表示通过服务台打开的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>工单</a>");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified", 
			"表示要通知的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>工单</a> 对象");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> object to be notified", 
			"表示要通知的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>包</a> 对象");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified", 
			"表示要通知的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>合并请求</a> 对象");
		m.put("represents the <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> object to be notified", 
			"表示要通知的 <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>提交</a> 对象");
		m.put("represents the exception encountered when open issue via service desk", "表示通过服务台打开工单时遇到的异常");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>", 
			"取消订阅的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>工单</a>");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>", 
			"取消订阅的 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>合并请求</a>");
		m.put("request to change", "请求更改");
		m.put("root", "根");
		m.put("root url of OneDev server", "OneDev 服务器的根 URL");
		m.put("run job", "运行任务");
		m.put("search in this revision will be accurate after indexed", "索引完成后，在当前版本中的搜索将更准确");
		m.put("service", "服务");
		m.put("severity:CRITICAL", "非常严重");
		m.put("severity:HIGH", "严重");
		m.put("severity:LOW", "轻微");
		m.put("severity:MEDIUM", "中度");
		m.put("skipped {0} lines", "跳过 {0} 行");
		m.put("space", "空格");
		m.put("state of an issue is transited", "工单的状态已转换");
		m.put("step template", "步骤模板");
		m.put("submit", "提交");
		m.put("tag the job is running against", "任务运行的标签");
		m.put("tag {0}", "标签 {0}");
		m.put("tags", "标签");
		m.put("the url to set up user account", "设置用户账户的 URL");
		m.put("time aggregation link", "时间聚合链接");
		m.put("touching specified path", "涉及指定路径");
		m.put("transit manually by any user", "由任何用户手动转换");
		m.put("transit manually by any user of roles \"{0}\"", "由具有角色 \"{0}\" 的任何用户手动转换");
		m.put("true", "真");
		m.put("true for html version, false for text version", "HTML 版本时为真，文本版本时为假");
		m.put("up to date", "最新");
		m.put("url following which to verify email address", "验证邮箱地址的 URL");
		m.put("url to reset password", "重置密码的 URL");
		m.put("value needs to be enclosed in brackets", "值需要用括号包围");
		m.put("value needs to be enclosed in parenthesis", "值需要用括号包围");
		m.put("value should be quoted", "值应该用引号包围");
		m.put("w%02d", "第 %d 周");
		m.put("week:Fri", "周五");
		m.put("week:Mon", "周一");
		m.put("week:Sat", "周六");
		m.put("week:Sun", "周日");
		m.put("week:Thu", "周四");
		m.put("week:Tue", "周二");
		m.put("week:Wed", "周三");
		m.put("widget:Tabs", "标签页");
		m.put("you may show this page later via incompatibilities link in help menu", "您可以稍后通过帮助菜单中的不兼容性链接显示此页面");
		m.put("{0} Month(s)", "{0} 个月");
		m.put("{0} activities on {1}", "{1} 有 {0} 个活动");
		m.put("{0} additions & {1} deletions", "{0} 个添加，{1} 个删除");
		m.put("{0} ahead", "{0} 领先");
		m.put("{0} behind", "{0} 落后");
		m.put("{0} branches", "{0} 个分支");
		m.put("{0} build(s)", "{0} 个构建");
		m.put("{0} child projects", "{0} 个子项目");
		m.put("{0} commits", "{0} 个提交");
		m.put("{0} commits ahead of base branch", "{0} 个提交领先于基准分支");
		m.put("{0} commits behind of base branch", "{0} 个提交落后于基准分支");
		m.put("{0} day", "{0} 天");
		m.put("{0} days", "{0} 天");
		m.put("{0} edited {1}", "{0} 编辑于 {1}");
		m.put("{0} files", "{0} 个文件");
		m.put("{0} forks", "{0} 个分叉");
		m.put("{0} hour", "{0} 小时");
		m.put("{0} hours", "{0} 小时");
		m.put("{0} inaccessible activities", "{0} 个无权查看的活动");
		m.put("{0} minute", "{0} 分钟");
		m.put("{0} minutes", "{0} 分钟");
		m.put("{0} reviewed", "{0} 已审查");
		m.put("{0} second", "{0} 秒");
		m.put("{0} seconds", "{0} 秒");
		m.put("{0} tags", "{0} 个标签");
		m.put("{0}d", "{0} 天");
		m.put("{0}h", "{0} 小时");
		m.put("{0}m", "{0} 分钟");
		m.put("{0}s", "{0} 秒");
		m.put("{0}w", "{0}周");
		m.put("{javax.validation.constraints.NotEmpty.message}", "不能为空");
		m.put("{javax.validation.constraints.NotNull.message}", "不能为空");
		m.put("{javax.validation.constraints.Size.message}", "至少需要指定一个值");
	}
		
	@Override
	protected Map<String, String> getContents() {
		return m;		
	}
	
}
