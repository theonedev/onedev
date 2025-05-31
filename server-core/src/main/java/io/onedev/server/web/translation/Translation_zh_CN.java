package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_zh_CN extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init();
		Translation.watchUpdate(Translation_zh_CN.class, () -> {
			init();
		});
	}

	private static void init() {
		m.clear();

		// Extracted keys
		m.put("'..' is not allowed in the directory", "'..' 不允许在目录中使用");
		m.put("(on behalf of <b>{0}</b>)", "（代表 <b>{0}</b>）");
		m.put("6-digits passcode", "6 位数代码");
		m.put("7 days", "7 天");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"可使用 <a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub 风格的 markdown</a>，并支持 <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid 和 katex</a>。");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">通过恢复代码验证</a>（如果您无法访问 TOTP 认证器）");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>注意：</b> 此步骤需要企业版订阅。<a href='https://onedev.io/pricing' target='_blank'>免费试用 30 天</a>");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>注意：</b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>时间跟踪</a>是企业版功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a>30天");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>项目所有者</b>是一个内置角色，拥有项目的所有权限");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>提示: </b> 输入 <tt>@</tt> 插入变量。使用 <tt>@@</tt> 插入字符 <tt>@</tt>");
		m.put("<i>No Name</i>", "<i>无名称</i>");
		m.put("<span wicket:id=\"submitterName\" class=\"user\"></span> opened <span wicket:id=\"submitDate\"></span>", 
			"<span wicket:id=\"submitterName\" class=\"user\"></span> 于 <span wicket:id=\"submitDate\"></span> 创建");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"用于验证提交信息页脚的<a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java正则表达式</a>");
		m.put("A pull request is open for this change", "一个合并请求正在审查此更改");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"你的JIRA云实例的API地址，例如：<tt>https://your-domain.atlassian.net/rest/api/3</tt>");
		m.put("Able to merge without conflicts", "可以合并而没有冲突");
		m.put("Absolute or relative url of the image", "图片的绝对或相对 URL");
		m.put("Absolute or relative url of the link", "链接的绝对或相对 URL");
		m.put("Access Anonymously", "匿名访问");
		m.put("Access Build Log", "访问构建日志");
		m.put("Access Build Pipeline", "访问构建流水线");
		m.put("Access Build Reports", "访问构建报告");
		m.put("Access Confidential Issues", "访问机密工单");
		m.put("Access Time Tracking", "访问时间追踪");
		m.put("Access Token Authorization Bean", "访问令牌授权Bean");
		m.put("Access Token Edit Bean", "访问令牌编辑Bean");
		m.put("Access Token Secret", "访问令牌密钥");
		m.put("Access Token for Target Project", "目标项目的访问令牌");
		m.put("Access Tokens", "访问令牌");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"访问令牌用于 API 访问和仓库拉取/推送。它不能用于登录 Web UI");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"访问令牌用于 API 访问或仓库拉取/推送。它不能用于登录 Web UI");
		m.put("Account Email", "账户邮箱");
		m.put("Account Name", "账户名称");
		m.put("Active Directory", "Active Directory");
		m.put("Active Since", "活跃起");
		m.put("Activities", "活动");
		m.put("Activity by type", "按类型统计的活动");
		m.put("Add", "添加");
		m.put("Add GPG Key", "添加 GPG 密钥");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "在此添加 GPG 密钥以验证此用户的代码提交/标签签名");
		m.put("Add GPG keys here to verify commits/tags signed by you", "在此添加 GPG 密钥以验证由您签名的提交/标签");
		m.put("Add Issues to Iteration", "将工单添加到迭代");
		m.put("Add New", "添加");
		m.put("Add New Email Address", "添加新电子邮件地址");
		m.put("Add SSH Key", "添加 SSH 密钥");
		m.put("Add Spent Time", "添加已用时间");
		m.put("Add a GPG Public Key", "添加一个 GPG 公钥");
		m.put("Add a SSH Key", "添加 SSH 密钥");
		m.put("Add after", "添加到后面");
		m.put("Add assignee...", "添加分配人员...");
		m.put("Add before", "添加到前面");
		m.put("Add child project", "添加子项目");
		m.put("Add comment on this selection", "在此选择上添加注释");
		m.put("Add dashboard", "添加仪表板");
		m.put("Add files via upload", "通过上传添加文件");
		m.put("Add member...", "添加成员...");
		m.put("Add new", "添加");
		m.put("Add new file", "添加新文件");
		m.put("Add new import", "添加新导入");
		m.put("Add project", "添加项目");
		m.put("Add reviewer...", "添加审查人员...");
		m.put("Add to batch to commit with other suggestions later", "稍后与其他建议一起提交");
		m.put("Add to group...", "添加到组...");
		m.put("Add {0}", "添加 {0}");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "代码提交 \"{0}\"（<i class='text-danger'>仓库中不存在</i>）");
		m.put("Added commit \"{0}\" ({1})", "代码提交 \"{0}\"（{1}）");
		m.put("Additions", "新增");
		m.put("Administration", "管理");
		m.put("Administrative permission over a project", "项目的管理权限");
		m.put("Advanced Search", "高级搜索");
		m.put("Agent", "代理");
		m.put("Agent Attribute", "代理属性");
		m.put("Agent Edit Bean", "代理编辑Bean");
		m.put("Agent Selector", "代理选择器");
		m.put("Agents", "任务代理");
		m.put("Aggregation Link", "聚合链接");
		m.put("Alert", "警报");
		m.put("Alert Setting", "警报设置");
		m.put("Alert Settings", "提醒设置");
		m.put("Alerts", "提醒");
		m.put("All", "全部");
		m.put("All accessible", "所有可访问的");
		m.put("All builds", "所有构建");
		m.put("All except", "除外所有");
		m.put("All files", "所有文件");
		m.put("All groups", "所有分组");
		m.put("All issues", "所有工单");
		m.put("All platforms in OCI layout", "所有 OCI 布局的平台");
		m.put("All platforms in image", "所有镜像中的平台");
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
		m.put("Any agent", "任何代理");
		m.put("Any branch", "任何分支");
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
		m.put("Approved", "已批准");
		m.put("Approved pull request \"{0}\" ({1})", "批准合并请求 \"{0}\"（{1}）");
		m.put("Arbitrary scope", "任意范围");
		m.put("Arbitrary type", "任意类型");
		m.put("Archived", "已归档");
		m.put("Arguments", "参数");
		m.put("Artifacts", "构件");
		m.put("Artifacts to Retrieve", "要检索的构件");
		m.put("Ascending", "升序");
		m.put("Assignees", "分配给");
		m.put("Assignees Issue Field", "受让人工单字段");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"分配人员具有代码写入权限，并负责合并合并请求");
		m.put("Asymmetric", "非对称");
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
		m.put("At least one state needs to be specified", "至少需要指定一个状态");
		m.put("At least one state should be specified", "至少需要指定一个状态");
		m.put("At least one tab should be added", "至少需要添加一个标签");
		m.put("At least one user search base should be specified", "至少需要指定一个用户搜索基础");
		m.put("At least one value needs to be specified", "至少需要指定一个值");
		m.put("At least two columns need to be defined", "至少需要定义两个列");
		m.put("Attachment", "附件");
		m.put("Attributes", "属性");
		m.put("Auth Source", "认证源");
		m.put("Authentication", "身份验证");
		m.put("Authentication Required", "需要身份验证");
		m.put("Authentication Token", "身份验证令牌");
		m.put("Authenticator", "认证器");
		m.put("Authenticator Bean", "身份验证器Bean");
		m.put("Author", "作者");
		m.put("Author date", "作者日期");
		m.put("Authored By", "作者");
		m.put("Authorization", "授权");
		m.put("Authorizations", "授权项");
		m.put("Authorized Projects", "已授权项目");
		m.put("Authorized Roles", "已授权角色");
		m.put("Auto Spec", "自动规范");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"自动更新检查通过从 onedev.io 请求一个图像来完成，该图像会指示新版本的可用性，颜色表示更新的严重程度。其工作方式与 gravatar 请求头像图片相同。如果禁用，强烈建议你定期手动检查更新（可通过屏幕左下角的帮助菜单执行），以确保获得安全/关键修复");
		m.put("Auto-discovered executor", "自动发现的执行器");
		m.put("Available Choices", "可选项");
		m.put("Avatar", "头像");
		m.put("Avatar Service Url", "头像服务地址");
		m.put("Avatar and name", "头像和名称");
		m.put("Backlog", "待办事项");
		m.put("Backlog Base Query", "待办基础查询");
		m.put("Backup", "备份");
		m.put("Backup Schedule", "备份计划");
		m.put("Backup Setting", "备份设置");
		m.put("Backup Setting Holder", "备份设置持有者");
		m.put("Base", "基准");
		m.put("Base Gpg Key", "基础GPG密钥");
		m.put("Base Query", "基础查询");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Base64 编码的 PEM 格式，以 -----BEGIN CERTIFICATE----- 开头，以 -----END CERTIFICATE----- 结尾");
		m.put("Belonging Groups", "所属组");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"以下是一些常见的条件。在搜索框中输入以查看完整列表和可用组合。");
		m.put("Below content is restored from an unsaved change. Clear to discard", "以下内容从未保存的更改中恢复。清除以丢弃");
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
		m.put("Browse Code", "浏览代码");
		m.put("Browse code", "浏览代码");
		m.put("Bug Report", "错误报告");
		m.put("Build", "构建");
		m.put("Build Artifact Storage", "构建构件存储");
		m.put("Build Commit", "构建提交");
		m.put("Build Context", "构建上下文");
		m.put("Build Description", "构建描述");
		m.put("Build Filter", "构建筛选器");
		m.put("Build Image", "构建镜像");
		m.put("Build Image (Kaniko)", "构建镜像（Kaniko）");
		m.put("Build Management", "构建管理");
		m.put("Build Notification", "构建通知");
		m.put("Build Number", "构建编号");
		m.put("Build Path", "构建路径");
		m.put("Build Preservation", "构建保留策略");
		m.put("Build Preservations", "构建保留策略");
		m.put("Build Preservations Bean", "构建保留策略Bean");
		m.put("Build Preserve Rules", "构建保留规则");
		m.put("Build Provider", "构建提供者");
		m.put("Build Spec", "构建规范");
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
		m.put("Build required for deletion. Submit pull request instead", "需要构建才能删除。请提交合并请求");
		m.put("Build required for this change. Submit pull request instead", "需要构建才能更改。请提交合并请求");
		m.put("Build version", "构建版本");
		m.put("Build with Persistent Volume", "使用持久卷构建");
		m.put("Builds", "构建");
		m.put("Buildx Builder", "Buildx构建器");
		m.put("Built In Fields Bean", "内建字段Bean");
		m.put("Burndown chart", "燃尽图");
		m.put("Button Image Url", "按钮图像地址");
		m.put("By Group", "按组");
		m.put("By User", "按用户");
		m.put("By day", "按日");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"默认情况下，代码通过自动生成的凭证进行克隆，该凭证仅对当前项目具有读取权限。如需让任务<a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>推送代码到服务器</a>，请在此提供具有相应权限的自定义凭证");
		m.put("By month", "按月");
		m.put("By week", "按周");
		m.put("Bypass Certificate Check", "绕过证书检查");
		m.put("CORS Allowed Origins", "CORS 允许的来源");
		m.put("CPD Report", "CPD 报告");
		m.put("CPU Intensive Task Concurrency", "CPU 密集型任务并发数");
		m.put("Cache Key", "缓存键");
		m.put("Cache Management", "缓存管理");
		m.put("Cache Paths", "缓存路径");
		m.put("Cache Setting Bean", "缓存设置 Bean");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "缓存在未访问达到指定天数后将被删除以节省空间");
		m.put("Calculating merge preview...", "计算合并预览...");
		m.put("Can Be Used By Jobs", "可被任务使用");
		m.put("Can Create Root Projects", "可以创建根项目");
		m.put("Can Edit Estimated Time", "可以编辑预估时间");
		m.put("Can not delete default branch", "不能删除默认分支");
		m.put("Can not delete root account", "不能删除根账户");
		m.put("Can not delete yourself", "不能删除自己");
		m.put("Can not disable root account", "不能禁用根账户");
		m.put("Can not disable yourself", "不能禁用自己");
		m.put("Can not link to self", "不能链接到自己");
		m.put("Can not save malformed query", "无法保存格式错误的查询");
		m.put("Cancel", "取消");
		m.put("Cancel invitation", "取消邀请");
		m.put("Cancelled", "已取消");
		m.put("Certificates to Trust", "信任的证书");
		m.put("Change", "更改");
		m.put("Change Detection Excludes", "变更检测排除项");
		m.put("Change My Password", "更改我的密码");
		m.put("Change to another field", "更改为其他字段");
		m.put("Change to another state", "更改为其他状态");
		m.put("Change to another value", "更改为其他值");
		m.put("Changes since last visit", "自上次访问以来的更改");
		m.put("Channel Notification", "频道通知");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"检查 <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub 的指南</a> 如何生成和使用 GPG 密钥来签署您的提交");
		m.put("Check Commit Message Footer", "检查提交消息尾部");
		m.put("Check Incoming Email", "检查接收的电子邮件");
		m.put("Check Update", "检查更新");
		m.put("Check Workflow Integrity", "检查工作流完整性");
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
		m.put("Checkout Code", "签出代码");
		m.put("Checkout Path", "签出路径");
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
		m.put("Choose issue...", "选择工单...");
		m.put("Choose iterations...", "选择迭代...");
		m.put("Choose...", "选择...");
		m.put("Click to show comment of marked text", "点击显示标记文本的评论");
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
		m.put("Closed Issue State", "已关闭的工单状态");
		m.put("Closest due date", "最近的截止日期");
		m.put("Clover Coverage Report", "Clover 覆盖率报告");
		m.put("Cluster Role", "集群角色");
		m.put("Cluster Setting", "集群设置");
		m.put("Cluster setting", "集群设置");
		m.put("Cobertura Coverage Report", "Cobertura 覆盖率报告");
		m.put("Code", "代码");
		m.put("Code Analysis", "代码分析");
		m.put("Code Analysis Setting", "代码分析设置");
		m.put("Code Comment", "代码评论");
		m.put("Code Comment Management", "代码评论管理");
		m.put("Code Comments", "代码评论");
		m.put("Code Compare", "代码比较");
		m.put("Code Management", "代码管理");
		m.put("Code Privilege", "代码权限");
		m.put("Code Search", "代码搜索");
		m.put("Code comment", "代码评论");
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
		m.put("Commands", "命令");
		m.put("Comment", "评论");
		m.put("Comment too long", "评论过长");
		m.put("Commented code is outdated", "注释的代码已过时");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "在项目 \"{1}\" 的文件 \"{0}\" 上评论");
		m.put("Commented on issue \"{0}\" ({1})", "在工单 \"{0}\" 上评论（{1}）");
		m.put("Commented on pull request \"{0}\" ({1})", "在合并请求 \"{0}\" 上评论（{1}）");
		m.put("Commit", "提交");
		m.put("Commit &amp; Insert", "提交并插入");
		m.put("Commit Batched Suggestions", "提交批量建议");
		m.put("Commit Message", "提交信息");
		m.put("Commit Message Bean", "提交信息 Bean");
		m.put("Commit Message Fix Patterns", "提交消息修复模式");
		m.put("Commit Message Footer Pattern", "提交信息尾部模式");
		m.put("Commit Notification", "提交通知");
		m.put("Commit Scopes", "提交作用域");
		m.put("Commit Signature Required", "需要提交签名");
		m.put("Commit Suggestion", "提交建议");
		m.put("Commit Types", "提交类型");
		m.put("Commit Types For Footer Check", "用于尾部检查的提交类型");
		m.put("Commit Your Change", "提交您的更改");
		m.put("Commit date", "提交日期");
		m.put("Commit hash", "提交哈希");
		m.put("Commit index version", "提交索引版本");
		m.put("Commit not exist or access denied", "提交不存在或访问被拒绝");
		m.put("Commit signature required but no GPG signing key specified", "提交签名需要但未指定 GPG 签名密钥");
		m.put("Commit suggestion", "提交建议");
		m.put("Commits", "提交");
		m.put("Commits are taken from default branch of non-forked repositories", "代码提交取自非分叉仓库的默认分支");
		m.put("Committed After", "提交后");
		m.put("Committed Before", "提交前");
		m.put("Committed By", "提交者");
		m.put("Committer", "提交者");
		m.put("Compare with base revision", "与基线版本比较");
		m.put("Concurrency", "并发");
		m.put("Condition", "条件");
		m.put("Confidential", "保密");
		m.put("Config File", "配置文件");
		m.put("Configuration Discovery Url", "配置发现 URL");
		m.put("Confirm password here", "在此确认密码");
		m.put("Confirm your action", "确认你的操作");
		m.put("Connector", "连接器");
		m.put("Container default", "容器默认");
		m.put("Content", "内容");
		m.put("Content is identical", "内容相同");
		m.put("Continue to add other user after create", "创建后继续添加其他用户");
		m.put("Contributed settings", "贡献的设置");
		m.put("Copy", "复制");
		m.put("Copy Files with SCP", "使用 SCP 复制文件");
		m.put("Copy dashboard", "复制仪表板");
		m.put("Copy selected text to clipboard", "复制选中的文本到剪贴板");
		m.put("Copy to clipboard", "复制到剪贴板");
		m.put("Count", "数量");
		m.put("Covered", "覆盖");
		m.put("Covered by tests", "被测试覆盖");
		m.put("Cppcheck Report", "Cppcheck 报告");
		m.put("Cpu Limit", "CPU 限制");
		m.put("Cpu Request", "CPU 请求");
		m.put("Create", "创建");
		m.put("Create Branch", "创建分支");
		m.put("Create Branch Bean", "创建分支 Bean");
		m.put("Create Branch Bean With Revision", "带版本的创建分支 Bean");
		m.put("Create Child Projects", "创建子项目");
		m.put("Create Group", "创建组");
		m.put("Create New", "新建");
		m.put("Create New File", "创建新文件");
		m.put("Create Pull Request", "创建合并请求");
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
		m.put("Create tag", "创建标签");
		m.put("Create tag <b>{0}</b> from {1}", "创建标签 <b>{0}</b>（基于 {1}）");
		m.put("Created At", "创建于");
		m.put("Creation of this branch is prohibited per branch protection rule", "此分支的创建受分支保护规则限制");
		m.put("Critical", "严重");
		m.put("Cron Expression", "Cron 表达式");
		m.put("Cron schedule", "Cron 调度");
		m.put("Curl Location", "Curl 路径");
		m.put("Current Iteration", "当前迭代");
		m.put("Current avatar", "当前头像");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"当前上下文与添加此评论时的上下文不同，点击显示评论上下文");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"当前上下文与添加此回复时的上下文不同，点击显示回复上下文");
		m.put("Current context is different from this action, click to show the comment context", "当前上下文与此操作不同，点击显示评论上下文");
		m.put("Current platform", "当前平台");
		m.put("Current project", "当前项目");
		m.put("Custom Linux Shell", "自定义 Linux Shell");
		m.put("Dashboard Share Bean", "仪表盘共享 Bean");
		m.put("Dashboards", "仪表盘");
		m.put("Database Backup", "数据库备份");
		m.put("Date", "日期");
		m.put("Date Time", "日期时间");
		m.put("Days Per Week", "每周工作天数");
		m.put("Default", "默认");
		m.put("Default (Shell on Linux, Batch on Windows)", "默认（Linux 上为 Shell，Windows 上为 Batch）");
		m.put("Default Assignees", "默认负责人");
		m.put("Default Boards", "默认看板");
		m.put("Default Fixed Issue Filter", "默认已修复工单过滤器");
		m.put("Default Fixed Issue Filters", "默认已修复工单过滤器");
		m.put("Default Fixed Issue Filters Bean", "默认已修复工单过滤器 Bean");
		m.put("Default Group", "默认分组");
		m.put("Default Merge Strategy", "默认合并策略");
		m.put("Default Multi Value Provider", "默认多值提供器");
		m.put("Default Project", "默认项目");
		m.put("Default Project Setting", "默认项目设置");
		m.put("Default Role", "默认角色");
		m.put("Default Role Bean", "默认角色 Bean");
		m.put("Default Value", "默认值");
		m.put("Default Value Provider", "默认值提供器");
		m.put("Default Values", "默认值");
		m.put("Default branch", "默认分支");
		m.put("Default merge strategy", "默认合并策略");
		m.put("Default role affects default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"默认角色影响系统中所有用户获得的默认权限。实际的默认权限将是该项目及其所有父项目中的默认角色所包含的<b class='text-warning'>所有权限</b>");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"首次重试的延迟时间（秒）。后续重试的延迟时间将基于该值进行指数退避计算");
		m.put("Delete", "删除");
		m.put("Delete All", "删除所有");
		m.put("Delete All Queried Memberships", "删除所有查询的成员");
		m.put("Delete All Queried Projects", "删除所有查询的项目");
		m.put("Delete All Queried Pull Requests", "删除所有查询的合并请求");
		m.put("Delete All Queried Users", "删除所有查询的用户");
		m.put("Delete Selected", "删除选中的");
		m.put("Delete Selected Comments", "删除选中的评论");
		m.put("Delete Selected Memberships", "删除选定的成员");
		m.put("Delete Selected Projects", "删除选中的项目");
		m.put("Delete Selected Pull Requests", "删除选定的合并请求");
		m.put("Delete Selected Users", "删除选定的用户");
		m.put("Delete Source Branch After Merge", "合并后删除源分支");
		m.put("Delete dashboard", "删除仪表板");
		m.put("Delete from branch {0}", "从分支 {0} 删除");
		m.put("Delete this", "删除");
		m.put("Delete this access token", "删除此访问令牌");
		m.put("Delete this branch", "删除此分支");
		m.put("Delete this field", "删除该字段");
		m.put("Delete this group", "删除此组");
		m.put("Delete this import", "删除此导入");
		m.put("Delete this key", "删除此密钥");
		m.put("Delete this state", "删除该状态");
		m.put("Delete this tag", "删除此标签");
		m.put("Delete this value", "删除该值");
		m.put("Deletion not allowed due to branch protection rule", "由于分支保护规则，不允许删除此分支");
		m.put("Deletion not allowed due to tag protection rule", "由于标签保护规则，删除不允许");
		m.put("Deletions", "删除数");
		m.put("Denied", "拒绝");
		m.put("Dependencies & Services", "依赖与服务");
		m.put("Dependency job finished", "依赖任务已完成");
		m.put("Descending", "降序");
		m.put("Description", "描述");
		m.put("Description Templates", "描述模板");
		m.put("Description too long", "描述过长");
		m.put("Destination Path", "目标路径");
		m.put("Destinations", "目标");
		m.put("Detect Licenses", "检测许可证");
		m.put("Detect Secrets", "检测敏感信息");
		m.put("Detect Vulnerabilities", "检测漏洞");
		m.put("Diff is too large to be displayed.", "差异太大，无法显示");
		m.put("Diff options", "差异选项");
		m.put("Digest invalid", "摘要无效");
		m.put("Directories to Skip", "要跳过的目录");
		m.put("Directory", "目录");
		m.put("Directory (tenant) ID", "目录（租户）ID");
		m.put("Disable", "禁用");
		m.put("Disable All Queried Users", "禁用所有查询的用户");
		m.put("Disable Auto Update Check", "禁用自动更新检查");
		m.put("Disable Dashboard", "禁用仪表盘");
		m.put("Disable Selected Users", "禁用选定的用户");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"禁用账户将重置密码，清除访问令牌，并删除除过去活动外的所有其他实体的引用。您真的要继续吗？");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"禁用账户将重置密码，清除访问令牌，并从其他实体中删除除过去活动外的所有引用。输入 <code>yes</code> 确认");
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
		m.put("Do Not Retrieve Groups", "不获取用户组");
		m.put("Do not ignore", "不要忽略");
		m.put("Do not retrieve", "不检索");
		m.put("Do not retrieve groups", "不检索组");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "您真的要取消邀请 \"{0}\" 吗？");
		m.put("Do you really want to delete \"{0}\"?", "您真的要删除 \"{0}\" 吗？");
		m.put("Do you really want to delete group \"{0}\"?", "您真的要删除组 \"{0}\" 吗？");
		m.put("Do you really want to delete role \"{0}\"?", "确定要删除角色 \"{0}\" 吗？");
		m.put("Do you really want to delete selected query watches?", "您真的要删除选中的查询订阅吗？");
		m.put("Do you really want to delete tag {0}?", "确定要删除标签 {0} 吗？");
		m.put("Do you really want to delete this SSH key?", "您确定要删除此 SSH 密钥吗？");
		m.put("Do you really want to delete this code comment and all its replies?", "您真的要删除此代码评论及其所有回复吗？");
		m.put("Do you really want to delete this code comment?", "您真的要删除此代码评论吗？");
		m.put("Do you really want to delete this email address?", "您真的要删除这个电子邮件地址吗？");
		m.put("Do you really want to delete this privilege?", "确定要删除此权限吗？");
		m.put("Do you really want to delete this reply?", "您真的要删除此回复吗？");
		m.put("Do you really want to discard batched suggestions?", "您真的要丢弃批量建议吗？");
		m.put("Do you really want to enable this account?", "您真的要启用此账户吗？");
		m.put("Do you really want to remove password of this user?", "您真的要删除此用户的密码吗？");
		m.put("Do you really want to remove this account?", "您确定要删除此账户吗？");
		m.put("Do you really want to remove this link?", "您确定要删除此链接吗？");
		m.put("Docker Executable", "Docker 可执行文件");
		m.put("Docker Image", "Docker 镜像");
		m.put("Docker Sock Path", "Docker Sock 路径");
		m.put("Docker hub", "Docker 中心");
		m.put("Dockerfile", "Dockerfile");
		m.put("Documentation", "文档");
		m.put("Don't have an account yet?", "没有账户？");
		m.put("Download", "下载");
		m.put("Download archive of this branch", "下载此分支的归档");
		m.put("Download tag archive", "下载标签归档");
		m.put("Due Date", "截止日期");
		m.put("Due Date Issue Field", "截止日期工单字段");
		m.put("Duplicate authorizations found: {0}", "找到重复的授权：{0}");
		m.put("Durations", "持续时间");
		m.put("ESLint Report", "ESLint 报告");
		m.put("Edit", "编辑");
		m.put("Edit Avatar", "编辑头像");
		m.put("Edit Estimated Time", "编辑预计时间");
		m.put("Edit My Avatar", "编辑我的头像");
		m.put("Edit Source", "编辑源码");
		m.put("Edit Timesheet", "编辑工时表");
		m.put("Edit dashboard", "编辑仪表板");
		m.put("Edit issue title", "编辑工单标题");
		m.put("Edit on branch {0}", "在分支 {0} 编辑");
		m.put("Edit on source branch", "在源分支编辑");
		m.put("Edit plain", "编辑纯文本");
		m.put("Edit saved queries", "编辑保存的查询");
		m.put("Edit this access token", "编辑此访问令牌");
		m.put("Edit {0}", "编辑 {0}");
		m.put("Editable Issue Fields", "可编辑的工单字段");
		m.put("Editable Issue Links", "可编辑的工单链接");
		m.put("Edited by {0} {1}", "由 {0} 于 {1} 编辑");
		m.put("Editor", "编辑器");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "目标分支或源分支有新的提交，请重新检查。");
		m.put("Email Address", "邮箱地址");
		m.put("Email Addresses", "电子邮件地址");
		m.put("Email Templates", "邮件模板");
		m.put("Email Verification", "邮件验证");
		m.put("Email address", "电子邮件地址");
		m.put("Email address already in use: {0}", "电子邮件地址已被使用: {0}");
		m.put("Email address already invited: {0}", "电子邮件地址已被邀请: {0}");
		m.put("Email address already used by another user", "电子邮件地址已被另一个用户使用");
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
		m.put("Enable Selected Users", "启用选定的用户");
		m.put("Enable TTY Mode", "启用 TTY 模式");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "通过 <a wicket:id=\"addFile\" class=\"link-primary\"></a> 启用构建支持");
		m.put("Enable if visibility of this field depends on other fields", "如果字段的可见性取决于其他字段，则启用");
		m.put("Enable if visibility of this param depends on other params", "如果参数的可见性取决于其他参数，则启用");
		m.put("Enable this if the access token has same permissions as the owner", "如果访问令牌具有与所有者相同的权限，则启用");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attach, make sure this executor can only be used by trusted jobs", 
			"启用此选项以允许运行 HTML 报告发布步骤。为避免 XSS 附件，请确保此执行器只能由受信任的任务使用");
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
		m.put("Enforce Conventional Commits", "强制执行 Conventional Commits");
		m.put("Enforce Two-factor Authentication", "强制执行两阶段认证");
		m.put("Enter description here", "输入描述");
		m.put("Enter your details to login to your account", "输入您的详细信息登录到您的账户");
		m.put("Entries", "条目");
		m.put("Entry", "条目");
		m.put("Enumeration", "枚举");
		m.put("Env Var", "环境变量");
		m.put("Environment Variables", "环境变量");
		m.put("Equal", "等于");
		m.put("Error calculating commits: check log for details", "计算提交时出错：请检查日志");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "cherry-pick 到 {0} 时出错：合并冲突检测到");
		m.put("Error cherry-picking to {0}: {1}", "cherry-pick 到 {0} 时出错：{1}");
		m.put("Error executing task", "执行任务时发生错误");
		m.put("Error parsing build spec", "解析构建规范时出错");
		m.put("Error reverting on {0}: Merge conflicts detected", "revert 到 {0} 时出错：合并冲突检测到");
		m.put("Error reverting on {0}: {1}", "revert 到 {0} 时出错：{1}");
		m.put("Estimated Time", "预计时间");
		m.put("Estimated Time Edit Bean", "预计时间编辑 Bean");
		m.put("Estimated Time Issue Field", "预计时间工单字段");
		m.put("Evaluate script to get choices", "运行脚本以获取选项");
		m.put("Evaluate script to get default value", "运行脚本以获取默认值");
		m.put("Evaluate script to get value or secret", "运行脚本以获取值或密钥");
		m.put("Evaluate script to get values or secrets", "运行脚本以获取值或密钥");
		m.put("Event Types", "事件类型");
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
		m.put("Expire Date", "过期日期");
		m.put("Expired", "已过期");
		m.put("Explicit SSL (StartTLS)", "显式 SSL (StartTLS)");
		m.put("Export as OCI layout", "导出为 OCI 布局");
		m.put("External Auth Source", "外部认证源");
		m.put("External Issue Transformers", "外部工单转换");
		m.put("Fail Threshold", "失败阈值");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"如果存在或严重性高于指定严重性级别的漏洞，则失败构建");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"如果存在或严重性高于指定严重性级别的漏洞，则失败构建。注意：此选项仅在构建未被其他步骤失败时生效");
		m.put("Failed", "失败");
		m.put("Field Bean", "字段 Bean");
		m.put("Field Instance", "字段实例");
		m.put("Field Spec", "字段规范");
		m.put("Field Specs", "字段规范");
		m.put("Fields", "字段");
		m.put("Fields And Links Bean", "字段和链接 Bean");
		m.put("File", "文件");
		m.put("File Changes", "文件变更");
		m.put("File Path", "文件路径");
		m.put("File Patterns", "文件模式");
		m.put("File Protection", "文件保护");
		m.put("File Protections", "文件保护");
		m.put("File changes", "文件更改");
		m.put("File is too large to edit here", "文件太大，无法在此编辑");
		m.put("File name", "文件名");
		m.put("Files", "文件");
		m.put("Files to Be Analyzed", "要分析的文件");
		m.put("Filter", "过滤");
		m.put("Filter Issues", "过滤工单");
		m.put("Filter branches...", "过滤分支...");
		m.put("Filter by name", "按名称过滤");
		m.put("Filter by name or email address", "按名称或电子邮件地址过滤");
		m.put("Filter by name...", "按名称过滤...");
		m.put("Filter by path", "按路径过滤");
		m.put("Filter roles...", "筛选角色...");
		m.put("Filter tags...", "过滤标签...");
		m.put("Find branch", "查找分支");
		m.put("Find or create branch", "查找或创建分支");
		m.put("Find or create tag", "查找或创建标签");
		m.put("Find tag", "查找标签");
		m.put("Fingerprint", "指纹");
		m.put("Finish", "完成");
		m.put("First applicable executor", "第一个适用的执行器");
		m.put("Fix Type", "修复类型");
		m.put("Fixing Builds", "修复构建");
		m.put("Fixing Commits", "修复提交");
		m.put("Float", "浮点数");
		m.put("For a particular project, the first matching entry will be used", "对于特定项目，第一个匹配的条目将被使用");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"对于从默认分支无法到达的构建提交，应该指定一个具有创建分支权限的访问令牌作为任务密钥");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"对于从默认分支无法到达的构建提交，应该指定一个具有创建标签权限的访问令牌作为任务密钥");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"对于从默认分支无法到达的构建提交，应该指定一个具有管理工单权限的访问令牌作为任务密钥");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"对于 docker 感知执行器，此路径在容器内，并接受绝对路径和相对路径（相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>）。对于直接在主机机器上运行的 shell 相关执行器，只接受相对路径");
		m.put("Force", "强制");
		m.put("Forgot Password?", "忘记密码？");
		m.put("Forks Of", "分叉");
		m.put("Frequencies", "频率");
		m.put("From Directory", "从目录");
		m.put("From States", "从状态");
		m.put("From {0}", "从 {0}");
		m.put("Full Name", "全名");
		m.put("Full screen", "全屏");
		m.put("Furthest due date", "最晚截止日期");
		m.put("GPG Keys", "GPG 密钥");
		m.put("GPG Public Key", "GPG 公钥");
		m.put("GPG Signing Key", "GPG 签名密钥");
		m.put("GPG Trusted Keys", "GPG 受信任密钥");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "GPG 公钥以 '-----BEGIN PGP PUBLIC KEY BLOCK-----' 开头，以 '-----END PGP PUBLIC KEY BLOCK-----' 结尾");
		m.put("General", "常规");
		m.put("General Settings", "常规设置");
		m.put("General settings updated", "常规设置已更新");
		m.put("Generate File Checksum", "生成文件校验和");
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
		m.put("Google Test Report", "Google 测试报告");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Gpg 密钥");
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
		m.put("Group Authorizations Bean", "组授权 Bean");
		m.put("Group By", "按组");
		m.put("Group Management", "组管理");
		m.put("Group Name Attribute", "组名属性");
		m.put("Group Retrieval", "组检索");
		m.put("Group Search Base", "组搜索基础");
		m.put("Group Search Filter", "组搜索过滤器");
		m.put("Group added", "组已添加");
		m.put("Group created", "组已创建");
		m.put("Groups", "组");
		m.put("Groups Claim", "组声明");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "HTTP(S) 克隆 URL");
		m.put("Has Owner Permissions", "有所有者权限");
		m.put("Help", "帮助");
		m.put("Hide comment", "隐藏注释");
		m.put("Hide saved queries", "隐藏保存的查询");
		m.put("High", "高");
		m.put("History", "历史");
		m.put("History of target branch and source branch is unrelated", "目标分支和源分支的历史记录不相关");
		m.put("Host name or ip address of remote machine to run commands via SSH", "远程机器的名称或 IP 地址，用于通过 SSH 运行命令");
		m.put("Hours Per Day", "每天小时数");
		m.put("Html Report", "HTML 报告");
		m.put("Html Report Publish Enabled", "HTML 报告发布启用");
		m.put("IMAP Host", "IMAP 主机");
		m.put("IMAP Password", "IMAP 密码");
		m.put("IMAP User", "IMAP 用户");
		m.put("IMPORTANT:", "重要：");
		m.put("Id", "ID");
		m.put("Identify Field", "识别字段");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"如果启用，在用户有权限的情况下，源分支将在合并合并请求后自动删除");
		m.put("If specified, OneDev will only display iterations with this prefix", "如果指定，OneDev 将只显示具有此前缀的迭代");
		m.put("If specified, all public and internal projects imported from GitLab will use this as default role. Private projects are not affected", 
			"如果指定，所有从 GitLab 导入的公共和内部项目将使用此作为默认角色。私有项目不受影响");
		m.put("If specified, all public repositories imported from GitHub will use this as default role. Private repositories are not affected", 
			"如果指定，所有从 GitHub 导入的公共仓库将使用此作为默认角色。私有仓库不受影响");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"如果指定，工单的预计/已用时间也将包括此类型的链接工单");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"如果启用此选项，git lfs 命令需要安装在 OneDev 服务器上（即使此步骤在其他节点上运行）");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"如果勾选，此组将能够在启用了工时跟踪的情况下编辑相应工单的预计工时");
		m.put("Ignore", "忽略");
		m.put("Ignore File", "忽略文件");
		m.put("Ignore all", "忽略所有");
		m.put("Ignore change", "忽略更改");
		m.put("Ignore leading", "忽略前缀");
		m.put("Ignore this field", "忽略此字段");
		m.put("Ignore this param", "忽略此参数");
		m.put("Ignore trailing", "忽略后缀");
		m.put("Ignored Licenses", "忽略的许可证");
		m.put("Image", "容器镜像");
		m.put("Image Text", "图片文本");
		m.put("Image URL", "图片 URL");
		m.put("Image URL should be specified", "图片 URL 应该指定");
		m.put("Imap Ssl Setting", "IMAP SSL 设置");
		m.put("Imap With Ssl", "IMAP 使用 SSL");
		m.put("Impersonate this user", "假装成此用户");
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
		m.put("Imports", "导入");
		m.put("In Projects", "在项目中");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"如果 IMAP 主机证书是自签名的或其 CA 根证书不被接受，您可以告诉 OneDev 绕过证书检查。<b class='text-danger'>警告：</b> 在不受信任的网络中，这可能会导致中间人攻击，您应该<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>将证书导入 OneDev</a> 而不是");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"如果 SMTP 主机证书是自签名的或其 CA 根证书不被接受，您可以告诉 OneDev 绕过证书检查。<b class='text-danger'>警告：</b> 在不受信任的网络中，这可能会导致中间人攻击，您应该<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>将证书导入 OneDev</a> 而不是");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"如果缓存未通过上述密钥命中，OneDev 将按顺序遍历此处定义的加载密钥，直到在项目层次结构中找到匹配的缓存。如果多个缓存匹配，将返回最新的缓存。");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"如果需要上传缓存，此属性指定上传的目标项目。留空表示当前项目");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"如果用户组在组侧维护，此属性指定组搜索的基础节点。例如：<i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"如果用户组在组侧维护，此过滤器用于确定当前用户的所属组。例如：<i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>。在此示例中，<i>{0}</i> 表示当前用户的 DN");
		m.put("Inbox Poll Setting", "Inbox 轮询设置");
		m.put("Include Child Projects", "包含子项目");
		m.put("Include Disabled", "包含禁用");
		m.put("Include Forks", "包含分支");
		m.put("Include When Issue is Opened", "在工单打开时包含");
		m.put("Incompatibilities", "不兼容性");
		m.put("Indicator", "指示器");
		m.put("Inherit from parent", "从父级继承");
		m.put("Input Spec", "输入规范");
		m.put("Input URL", "输入 URL");
		m.put("Input allowed CORS origin, hit ENTER to add", "输入允许的 CORS 来源，按回车键添加");
		m.put("Input title here", "输入标题");
		m.put("Input user search base. Hit ENTER to add", "输入用户搜索基础。按回车键添加");
		m.put("Insert", "插入");
		m.put("Insert Image", "插入图片");
		m.put("Insert Link", "插入链接");
		m.put("Insert link to this file", "插入链接到此文件");
		m.put("Insert this image", "插入此图片");
		m.put("Install Subscription Key", "安装订阅密钥");
		m.put("Integer", "整数");
		m.put("Interpreter", "解释器");
		m.put("Invalid PCRE syntax", "无效的 PCRE 语法");
		m.put("Invalid credentials", "无效的凭据");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "无效的日期范围，期望 \"yyyy-MM-dd to yyyy-MM-dd\"");
		m.put("Invalid email address: {0}", "无效的电子邮件地址: {0}");
		m.put("Invalid request path", "无效的请求路径");
		m.put("Invalid selection, click for details", "无效的选择，点击查看详情");
		m.put("Invitation sent to \"{0}\"", "邀请已发送至 \"{0}\"");
		m.put("Invitation to \"{0}\" deleted", "邀请已取消: \"{0}\"");
		m.put("Invitations", "邀请");
		m.put("Invitations sent", "邀请已发送");
		m.put("Invite", "邀请");
		m.put("Invite Users", "邀请用户");
		m.put("Is Site Admin", "是否为站点管理员");
		m.put("Issue", "工单");
		m.put("Issue Close States", "工单关闭状态");
		m.put("Issue Creation Setting", "工单创建设置");
		m.put("Issue Creation Settings", "工单创建设置");
		m.put("Issue Description", "工单描述");
		m.put("Issue Details", "工单详情");
		m.put("Issue Field Mapping", "工单字段映射");
		m.put("Issue Field Mappings", "工单字段映射");
		m.put("Issue Field Set", "工单字段集");
		m.put("Issue Fields", "工单字段");
		m.put("Issue Filter", "工单过滤器");
		m.put("Issue Import Option", "工单导入选项");
		m.put("Issue Label Mapping", "工单标签映射");
		m.put("Issue Label Mappings", "工单标签映射");
		m.put("Issue Link Mapping", "工单链接映射");
		m.put("Issue Link Mappings", "工单链接映射");
		m.put("Issue Management", "工单管理");
		m.put("Issue Notification", "工单通知");
		m.put("Issue Notification Unsubscribed", "工单通知退订");
		m.put("Issue Pattern", "工单模式");
		m.put("Issue Priority Mapping", "工单优先级映射");
		m.put("Issue Priority Mappings", "工单优先级映射");
		m.put("Issue Query", "工单查询");
		m.put("Issue Settings", "工单设置");
		m.put("Issue State Mapping", "工单状态映射");
		m.put("Issue State Mappings", "工单状态映射");
		m.put("Issue Status Mapping", "工单状态映射");
		m.put("Issue Status Mappings", "工单状态映射");
		m.put("Issue Stopwatch Overdue", "工单计时器超时");
		m.put("Issue Tag Mapping", "工单标签映射");
		m.put("Issue Tag Mappings", "工单标签映射");
		m.put("Issue Template", "工单模板");
		m.put("Issue Type Mapping", "工单类型映射");
		m.put("Issue Type Mappings", "工单类型映射");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"工单管理权限，包括对多个工单的批量操作");
		m.put("Issue already linked", "工单已链接");
		m.put("Issue in state", "工单状态");
		m.put("Issue list", "工单列表");
		m.put("Issue not exist or access denied", "工单不存在或访问被拒绝");
		m.put("Issue number", "工单编号");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"工单查询订阅仅影响新工单。要批量管理现有工单的订阅状态，请在工单页面中按订阅状态过滤工单，然后采取适当行动");
		m.put("Issue state duration statistics", "工单状态持续时间统计");
		m.put("Issue state frequency statistics", "工单状态频率统计");
		m.put("Issue state trend statistics", "工单状态趋势统计");
		m.put("Issues", "工单");
		m.put("Italic", "斜体");
		m.put("Iteration", "迭代");
		m.put("Iteration Edit Bean", "迭代编辑Bean");
		m.put("Iteration Name", "迭代名称");
		m.put("Iteration Names", "迭代名称");
		m.put("Iteration Prefix", "迭代前缀");
		m.put("Iteration list", "迭代列表");
		m.put("Iterations", "迭代");
		m.put("Iterations Bean", "迭代Bean");
		m.put("JIRA Issue Priority", "JIRA 工单优先级");
		m.put("JIRA Issue Status", "JIRA 工单状态");
		m.put("JIRA Issue Type", "JIRA 工单类型");
		m.put("JIRA Project", "JIRA 项目");
		m.put("JIRA Projects to Import", "JIRA 项目导入");
		m.put("JUnit Report", "JUnit 报告");
		m.put("JaCoCo Coverage Report", "JaCoCo 覆盖率报告");
		m.put("Jest Coverage Report", "Jest 覆盖率报告");
		m.put("Jest Test Report", "Jest 测试报告");
		m.put("Job", "任务");
		m.put("Job Dependencies", "任务依赖");
		m.put("Job Dependency", "任务依赖");
		m.put("Job Executor", "任务执行器");
		m.put("Job Executor Bean", "任务执行器Bean");
		m.put("Job Executors", "任务执行器");
		m.put("Job Name", "任务名称");
		m.put("Job Names", "任务名称");
		m.put("Job Privilege", "任务权限");
		m.put("Job Privileges", "任务权限");
		m.put("Job Properties", "任务属性");
		m.put("Job Properties Bean", "任务属性Bean");
		m.put("Job Property", "任务属性");
		m.put("Job Requirement", "任务需求");
		m.put("Job Secret", "任务密钥");
		m.put("Job Secret Edit Bean", "任务密钥编辑Bean");
		m.put("Job Secrets", "任务密钥");
		m.put("Job Trigger", "任务触发器");
		m.put("Job Trigger Bean", "任务触发器Bean");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"任务管理权限，包括删除任务的构建。它隐含所有其他任务权限");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"任务依赖决定不同任务的顺序和并发性。您还可以指定从上游任务检索的工件");
		m.put("Job executors", "任务执行器");
		m.put("Job name", "任务名称");
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
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"具有相同顺序组和执行器的任务将按顺序执行。例如，您可以为此属性指定 <tt>@project_path@:prod</tt> 用于执行相同执行器并在当前项目的 prod 环境中部署任务以避免冲突的部署");
		m.put("Key Secret", "密钥密钥");
		m.put("Kubectl Config File", "Kubectl 配置文件");
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
		m.put("Language", "语言");
		m.put("Last Finished of Specified Job", "指定任务的最后完成");
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
		m.put("Line changes", "行变化");
		m.put("Link", "链接");
		m.put("Link Spec", "链接规范");
		m.put("Link Spec Opposite", "链接规范相反");
		m.put("Link Text", "链接文本");
		m.put("Link URL", "链接 URL");
		m.put("Link URL should be specified", "链接 URL 应该指定");
		m.put("Link issues", "链接工单");
		m.put("Linkable Issues", "可链接的工单");
		m.put("Linkable Issues On the Other Side", "可链接的工单在另一侧");
		m.put("Links", "链接");
		m.put("List", "列表");
		m.put("Literal", "字面量");
		m.put("Literal default value", "字面量默认值");
		m.put("Literal value", "字面量值");
		m.put("Load Keys", "加载密钥");
		m.put("Loading emojis...", "加载表情符号...");
		m.put("Loading...", "加载...");
		m.put("Log Work", "记录工作");
		m.put("Login Name", "登录名称");
		m.put("Login name already used by another account", "登录名已被另一个账户使用");
		m.put("Login name or email", "登录名或电子邮件");
		m.put("Login name or email address", "登录名或电子邮件地址");
		m.put("Login to comment", "登录以评论");
		m.put("Login to comment on selection", "登录以在选择上添加注释");
		m.put("Logo for Dark Mode", "暗黑模式下的Logo");
		m.put("Logo for Light Mode", "亮色模式下的Logo");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"用于生成访问 Gmail 访问令牌的长寿命刷新令牌。<b class='text-info'>提示：</b>您可以在此字段右侧的按钮生成刷新令牌。请注意，每当 client id、client secret 或帐户名称发生变化时，刷新令牌应重新生成");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"用于生成访问 office 365 邮件服务器访问令牌的长寿命刷新令牌。<b class='text-info'>提示：</b>您可以在此字段右侧的按钮登录到您的 office 365 帐户并生成刷新令牌。请注意，每当 tenant id、client id、client secret 或用户主体名称发生变化时，刷新令牌应重新生成");
		m.put("Low", "低");
		m.put("MS Teams Notifications", "MS Teams 通知");
		m.put("Mail Service", "邮件服务");
		m.put("Mail Service Bean", "邮件服务Bean");
		m.put("Mail service", "邮件服务");
		m.put("Mail service not configured", "邮件服务未配置");
		m.put("Malformed email address", "格式错误的电子邮件地址");
		m.put("Malformed query", "查询语法错误");
		m.put("Manage Job", "管理任务");
		m.put("Manager DN", "管理员DN");
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
		m.put("Member added", "成员已添加");
		m.put("Members", "成员");
		m.put("Memory Limit", "内存限制");
		m.put("Memory Request", "内存请求");
		m.put("Mention Someone", "提及某人");
		m.put("Mention someone", "提及某人");
		m.put("Merge Strategy", "合并策略");
		m.put("Merged", "合并");
		m.put("Merged pull request \"{0}\" ({1})", "合并合并请求 \"{0}\"（{1}）");
		m.put("Meta", "元");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "最小值");
		m.put("Month", "月");
		m.put("Months", "月");
		m.put("Months to Display", "显示的月数");
		m.put("More", "更多");
		m.put("More Activities", "更多活动");
		m.put("More Options", "更多选项");
		m.put("More Settings", "更多设置");
		m.put("More commits", "更多提交");
		m.put("Most branch coverage", "最小的分支覆盖率");
		m.put("Most line coverage", "最小的行覆盖率");
		m.put("Mount Docker Sock", "挂载 Docker Sock");
		m.put("Move All Queried Projects To...", "移动所有查询的项目到...");
		m.put("Move Selected Projects To...", "移动选中的项目到...");
		m.put("Multiple", "多");
		m.put("Multiple Lines", "多行");
		m.put("Multiple On the Other Side", "多行在另一侧");
		m.put("Must not be empty", "不能为空");
		m.put("My Access Tokens", "我的访问令牌");
		m.put("My GPG Keys", "我的 GPG 密钥");
		m.put("My Overview", "我的概览");
		m.put("My Profile", "我的个人资料");
		m.put("My SSH Keys", "我的 SSH 密钥");
		m.put("Mypy Report", "Mypy 报告");
		m.put("N/A", "不适用");
		m.put("Name", "名称");
		m.put("Name Of Empty Value", "空值的名称");
		m.put("Name On the Other Side", "另一侧的名称");
		m.put("Name Prefix", "名称前缀");
		m.put("Name already used by another access token of the owner", "此名称已被其他访问令牌的所有者使用");
		m.put("Name invalid", "名称无效");
		m.put("Name of the link", "链接的名称");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"另一侧的链接名称。例如，如果名称是 <tt>sub issues</tt>，另一侧的名称可以是 <tt>parent issue</tt>");
		m.put("Name of the provider will be displayed on login button", "提供商的名称将显示在登录按钮上");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"提供商的名称将服务于两个目的：<ul><li>显示在登录按钮上<li>形成授权回调 URL，格式为 <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>");
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
		m.put("Never expire", "永不过期");
		m.put("New Invitation Bean", "新邀请Bean");
		m.put("New Password", "新密码");
		m.put("New State", "新状态");
		m.put("New User Bean", "新用户Bean");
		m.put("New Value", "新值");
		m.put("New user created", "新用户创建成功");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"新版本可用。红色表示安全/关键更新，黄色表示错误修复，蓝色表示功能更新。点击查看更改。可在系统设置中禁用");
		m.put("Next", "下一步");
		m.put("No", "否");
		m.put("No Activity Days", "无活动天数");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"没有配置 SSH 密钥。您可以 <a wicket:id=\"sshKeys\" class=\"link-primary\">添加一个密钥</a> 或切换到 <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> 克隆 URL");
		m.put("No SSL", "无SSL");
		m.put("No accessible reports", "无访问报告");
		m.put("No activity for some time", "无活动一段时间");
		m.put("No aggregation", "无聚合");
		m.put("No any", "无任何");
		m.put("No branch to cherry-pick to", "没有分支可以 cherry-pick");
		m.put("No branch to revert on", "没有分支可以 revert");
		m.put("No branches Found", "未找到分支");
		m.put("No branches found", "未找到分支");
		m.put("No comment", "无评论");
		m.put("No comments to set resolved", "没有评论可以设置为已解决");
		m.put("No comments to set unresolved", "没有评论可以设置为未解决");
		m.put("No config file", "无配置文件");
		m.put("No default group", "无默认组");
		m.put("No default role", "无默认角色");
		m.put("No default value", "无默认值");
		m.put("No description", "无描述");
		m.put("No diffs", "没有差异");
		m.put("No diffs to navigate", "没有差异可导航");
		m.put("No directories to skip", "无要跳过的目录");
		m.put("No external authenticator", "无外部认证器");
		m.put("No fields to prompt", "无要提示的字段");
		m.put("No fields to remove", "无要删除的字段");
		m.put("No file attachments", "没有文件附件");
		m.put("No group by", "无分组");
		m.put("No ignore file", "无忽略文件");
		m.put("No ignored licenses", "无忽略的许可证");
		m.put("No image attachments", "没有图片附件");
		m.put("No imports defined", "无导入");
		m.put("No jobs defined", "未定义任务");
		m.put("No limit", "无限制");
		m.put("No mail service", "无邮件服务");
		m.put("No memberships to delete", "没有要删除的成员");
		m.put("No obvious changes", "无明显改动");
		m.put("No one", "无任何人");
		m.put("No parent", "无父级");
		m.put("No properties defined", "未定义属性");
		m.put("No proxy", "无代理");
		m.put("No pull requests to delete", "没有要删除的合并请求");
		m.put("No pull requests to discard", "没有要放弃的合并请求");
		m.put("No pull requests to set as read", "没有要设置为已读的合并请求");
		m.put("No pull requests to watch/unwatch", "没有要监视/取消监视的合并请求");
		m.put("No required services", "无所需服务");
		m.put("No secret config", "无秘密配置");
		m.put("No services defined", "未定义服务");
		m.put("No step templates defined", "未定义步骤模板");
		m.put("No tags found", "未找到标签");
		m.put("No users to delete", "没有要删除的用户");
		m.put("No users to disable", "没有要禁用的用户");
		m.put("No users to enable", "没有要启用的用户");
		m.put("No verified primary email address", "没有验证的主电子邮件地址");
		m.put("Node Selector", "节点选择器");
		m.put("Node Selector Entry", "节点选择器条目");
		m.put("None", "无");
		m.put("Not Active Since", "不活跃起");
		m.put("Not assigned", "未分配");
		m.put("Not covered", "未覆盖");
		m.put("Not covered by any test", "未被任何测试覆盖");
		m.put("Not displaying any fields", "不显示任何字段");
		m.put("Not displaying any links", "不显示任何链接");
		m.put("Not passed", "未通过");
		m.put("Not run", "未运行");
		m.put("Not specified", "未指定");
		m.put("Note", "备注");
		m.put("Nothing to preview", "没有预览内容");
		m.put("Notifications", "通知");
		m.put("Notify Build Events", "通知构建事件");
		m.put("Notify Code Comment Events", "通知代码评论事件");
		m.put("Notify Code Push Events", "通知代码推送事件");
		m.put("Notify Issue Events", "通知工单事件");
		m.put("Notify Own Events", "通知自己");
		m.put("Notify Pull Request Events", "通知合并请求");
		m.put("Notify Users", "通知用户");
		m.put("Ntfy.sh Notifications", "Ntfy.sh 通知");
		m.put("Number of CPU Cores", "CPU 核心数");
		m.put("Number of builds to preserve", "保留的构建数量");
		m.put("Number of project replicas, including primary and backups", "项目副本数量，包括主节点和备份节点");
		m.put("Number of recent months to show statistics for", "显示统计信息的最近几个月");
		m.put("OAuth2 Client information | CLIENT ID", "OAuth2 客户端信息 | CLIENT ID");
		m.put("OAuth2 Client information | CLIENT SECRET", "OAuth2 客户端信息 | CLIENT SECRET");
		m.put("OCI Layout Directory", "OCI 布局目录");
		m.put("Ok", "确定");
		m.put("Old Name", "旧名称");
		m.put("Old Password", "旧密码");
		m.put("On Branches", "在分支上");
		m.put("OneDev Issue Field", "OneDev 工单字段");
		m.put("OneDev Issue Link", "OneDev 工单链接");
		m.put("OneDev Issue State", "OneDev 工单状态");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"OneDev 分析仓库文件以进行代码搜索、行统计和代码贡献统计。此设置告诉要分析哪些文件，并期望以空格分隔的 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径模式</a>。可以通过在前面添加 '-' 来排除某个模式，例如 <code>-**/vendors/**</code> 将排除所有包含 vendors 的路径。<b>注意：</b> 更改此设置仅影响新提交。要将更改应用到历史提交，请停止服务器并删除 <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>项目存储目录</a> 下的 <code>index</code> 和 <code>info/commit</code> 文件夹。当服务器启动时，仓库将重新分析");
		m.put("OneDev configures git hooks to communicate with itself via curl", "OneDev 配置 git 钩子以通过 curl 与自身通信");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"OneDev 需要搜索和确定用户 DN，以及在启用组检索时搜索用户组信息。如果这些操作需要认证，请勾选此选项并指定 'manager' DN 和密码");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"OneDev 需要 git 命令行来管理仓库。最低要求版本为 2.11.1。另外，如果要在构建任务中检索 LFS 文件，请确保安装了 git-lfs");
		m.put("Only projects manageable by access token owner can be authorized", "只有受访问令牌所有者管理的项目才能被授权");
		m.put("Open", "打开");
		m.put("Open new pull request", "创建新的合并请求");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"OpenID 客户端标识将在注册此 OneDev 实例作为客户端应用程序时由您的 OpenID 提供商分配");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"OpenID 客户端密钥将在注册此 OneDev 实例作为客户端应用程序时由您的 OpenID 提供商生成");
		m.put("OpenSSH Public Key", "OpenSSH 公钥");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"OpenSSH 公钥以 'ssh-rsa'、'ecdsa-sha2-nistp256'、'ecdsa-sha2-nistp384'、'ecdsa-sha2-nistp521'、'ssh-ed25519'、'sk-ecdsa-sha2-nistp256@openssh.com' 或 'sk-ssh-ed25519@openssh.com' 开头");
		m.put("Opened issue \"{0}\" ({1})", "创建工单 \"{0}\"（{1}）");
		m.put("Opened pull request \"{0}\" ({1})", "创建合并请求 \"{0}\"（{1}）");
		m.put("Operations", "操作");
		m.put("Optional", "可选");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"可选择指定要在其中创建工单的项目。留空则在当前项目中创建");
		m.put("Optionally add new users to specified default group", "可选择将新用户添加到指定的默认组");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"如果未检索到成员资格信息，可选择将新认证的用户添加到指定组");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"如果未检索到成员资格信息，可选择将新认证的用户添加到指定组");
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
		m.put("Optionally mount directories or files under job workspace into container", "可选择将任务工作空间下的目录或文件挂载到容器中");
		m.put("Optionally select fields to prompt when this button is pressed", "可选择在点击此按钮时提示字段");
		m.put("Optionally select fields to remove when this transition happens", "可选择在转换发生时删除字段");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"可选择指定用户 LDAP 条目中属性的名称，其值将被用作用户电子邮件。此字段通常设置为 <i>mail</i> 根据 RFC 2798");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"可选择指定用户 LDAP 条目中属性的名称，其值将被用作用户全名。此字段通常设置为 <i>displayName</i> 根据 RFC 2798。如果留空，用户的全名将不会被检索");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"可选择指定 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>任务密钥</a> 用作 GitHub 访问令牌。这用于检索托管在 GitHub 上的依赖项的发布说明，并且经过身份验证的访问将获得更高的速率限制");
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
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作空间</a> 的 Dockerfile。留空则使用上面指定的构建路径下的文件 <tt>Dockerfile</tt>");
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
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的路径来放置检索的构件。留空则使用任务工作空间本身");
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
		m.put("Optionally specify arguments to run above image", "可选择指定运行上述镜像的参数");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"可选择指定从依赖项检索到 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的构件。只能检索已发布的构件（通过构件发布步骤）。留空则不检索任何构件");
		m.put("Optionally specify authorized executor for this job. Leave empty to use first applicable executor (or use auto-discovered executor if no executors are defined)", 
			"可选择指定此任务的授权执行器。留空则使用第一个适用的执行器（如果未定义执行器，则使用自动发现的执行器）");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"可选择指定允许按此按钮的授权角色。如果未指定，则允许所有用户");
		m.put("Optionally specify base query of the list", "可选择指定列表的基本查询");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"可选择指定允许访问此密钥的分支/用户/组。如果留空，任何任务都可以访问此密钥，包括通过外部合并请求触发的任务");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作空间</a> 的构建上下文路径。留空则使用任务工作空间本身。除非使用选项 <code>--dockerfile</code> 指定不同位置，否则文件 <code>Dockerfile</code> 应存在于构建上下文目录中");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作空间</a> 的构建路径。留空则使用任务工作空间本身");
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
			"可选择指定浅克隆的深度，以加快源代码检索速度");
		m.put("Optionally specify description of the issue", "可选择指定工单的描述");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"可选择指定要跳过的扫描路径内的目录或 glob 模式。多个跳过项应以空格分隔");
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
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"可选择指定相对于缓存路径的文件，在检测缓存更改时忽略。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。多个文件应以空格分隔，包含空格的单个文件应加引号");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"如果要检索用户的组成员资格信息，可选择指定组搜索基础。例如：<i>cn=Users, dc=example, dc=com</i>。要给 Active Directory 组适当的权限，应定义一个同名的 OneDev 组。留空则在 OneDev 端管理组成员资格");
		m.put("Optionally specify issue links allowed to edit", "可选择指定允许编辑的工单链接");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "可选择指定适用于此模板的工单。留空则适用于所有工单");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"可选择指定适用于此转换的工单。留空则适用于所有工单");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"可选择指定适用于此转换的工单。留空则适用于所有工单。");
		m.put("Optionally specify job requirement of this executor", "可选择指定此执行器的任务要求");
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
			"可选择在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作空间</a> 下指定 osv 扫描器 <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>配置文件</a>。您可以通过此文件忽略特定漏洞");
		m.put("Optionally specify path protection rules", "可选择指定路径保护规则");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的路径，用作 trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>忽略文件</a>");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的路径，用作 trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>密钥配置</a>");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"可选择指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的路径来发布构件。留空则使用任务工作空间本身");
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
			"可选择指定 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 下的相对路径，用于克隆代码。留空则使用任务工作空间本身");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"可选择指定 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 下的相对路径进行扫描。留空则使用任务工作空间本身");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"可选择指定 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 下的相对路径，用于扫描依赖项漏洞。可以指定多个路径，应以空格分隔。留空则使用任务工作空间本身");
		m.put("Optionally specify required reviewers for changes of specified branch", "可选择为指定分支的更改指定所需的审阅者");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"可选择指定用于创建分支的修订版本。留空则从构建提交创建");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"可选择指定单独的目录来存储构建构件。非绝对目录被视为相对于站点目录");
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
			"可选择指定要检索已认证用户的组。根据提供商，您可能需要请求额外的范围以使此声明可用");
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
		m.put("Order By", "排序");
		m.put("Ordered List", "有序列表");
		m.put("Ordered list", "有序列表");
		m.put("Osv License Scanner", "OSV 许可证扫描器");
		m.put("Osv Vulnerability Scanner", "OSV 漏洞扫描器");
		m.put("Other", "其他");
		m.put("Outline", "大纲");
		m.put("Outline Search", "大纲搜索");
		m.put("Output", "输出");
		m.put("Overview", "概览");
		m.put("Ownered By", "拥有者");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "PEM 私钥以 '-----BEGIN RSA PRIVATE KEY-----' 开头");
		m.put("PMD Report", "PMD 报告");
		m.put("Pack", "包");
		m.put("Pack Notification", "包通知");
		m.put("Pack Size Limit", "包大小限制");
		m.put("Pack Type", "包类型");
		m.put("Package", "包");
		m.put("Package Management", "包管理");
		m.put("Package Notification", "包通知");
		m.put("Package Privilege", "包权限");
		m.put("Package Storage", "包存储");
		m.put("Package list", "包列表");
		m.put("Packages", "包");
		m.put("Page is in error, reload to recover", "页面发生错误，请刷新页面");
		m.put("Param Instance", "参数实例");
		m.put("Param Instances", "参数实例");
		m.put("Param Map", "参数映射");
		m.put("Param Matrix", "参数矩阵");
		m.put("Param Name", "参数名称");
		m.put("Param Spec", "参数");
		m.put("Param Spec Bean", "参数Bean");
		m.put("Parameter Specs", "参数");
		m.put("Params", "参数");
		m.put("Params & Triggers", "参数与触发器");
		m.put("Parent Bean", "父Bean");
		m.put("Parent OneDev Project", "父OneDev项目");
		m.put("Parent Project", "父项目");
		m.put("Parents", "父项目");
		m.put("Partially covered", "部分覆盖");
		m.put("Partially covered by some tests", "部分被测试覆盖");
		m.put("Passcode", "Passcode");
		m.put("Passed", "通过");
		m.put("Password", "密码");
		m.put("Password Edit Bean", "密码编辑 Bean");
		m.put("Password Reset", "密码重置");
		m.put("Password Reset Bean", "密码重置 Bean");
		m.put("Password Secret", "密码密钥");
		m.put("Password and its confirmation should be identical.", "密码和其确认应相同。");
		m.put("Password has been changed", "密码已更改");
		m.put("Password has been removed", "密码已删除");
		m.put("Password has been set", "密码已设置");
		m.put("Password or Access Token for Remote Repository", "密码或远程仓库的访问令牌");
		m.put("Password reset request has been sent", "密码重置请求已发送");
		m.put("Paste subscription key here", "在此粘贴订阅密钥");
		m.put("Path containing spaces or starting with dash needs to be quoted", "路径包含空格或以破折号开头时需要用引号括起来");
		m.put("Path placeholder", "路径占位符");
		m.put("Path to kubectl", "kubectl 的路径");
		m.put("Paths", "路径");
		m.put("Pattern", "模式");
		m.put("Pem Private Key", "PEM 私钥");
		m.put("Pending", "待处理");
		m.put("Performance", "性能");
		m.put("Performance Setting", "性能设置");
		m.put("Performance Settings", "性能设置");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"执行模糊查询。使用 '~' 包围搜索文本以添加更多条件，例如：~text to search~ and open");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"执行模糊查询。使用 '~' 包围搜索文本以添加更多条件，例如：~text to search~ author(robin)");
		m.put("Permanent link", "永久链接");
		m.put("Permanent link of this selection", "此选择的永久链接");
		m.put("Pick Existing", "选择现有");
		m.put("Platform", "平台");
		m.put("Platforms", "平台");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"请<a wicket:id=\"download\" class=\"font-weight-bolder\">下载</a>下方的恢复代码并妥善保管。这些代码可用于在您无法访问认证应用程序时提供一次性账户访问权限。它们<b>不会</b>再次显示");
		m.put("Please choose revision to create branch from", "请选择要从中创建分支的版本");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "请先配置 <a wicket:id=\"mailSetting\">邮件设置</a>");
		m.put("Please confirm", "请确认");
		m.put("Please confirm the password.", "请确认密码。");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"请输入在启用两阶段验证时保存的恢复代码");
		m.put("Please login to perform this operation", "请登录才能执行此操作");
		m.put("Please select branches to create pull request", "请选择分支创建合并请求");
		m.put("Please select comments to delete", "请选择要删除的评论");
		m.put("Please select comments to set resolved", "请选择要设置为已解决的评论");
		m.put("Please select comments to set unresolved", "请选择要设置为未解决的评论");
		m.put("Please select different branches", "请选择不同的分支");
		m.put("Please select memberships to delete", "请选择要删除的成员");
		m.put("Please select pull requests to delete", "请选择要删除的合并请求");
		m.put("Please select pull requests to discard", "请选择要放弃的合并请求");
		m.put("Please select pull requests to watch/unwatch", "请选择要监视/取消监视的合并请求");
		m.put("Please select query watches to delete", "请选择要删除的查询订阅");
		m.put("Please select revision to create tag from", "请选择要从中创建标签的版本");
		m.put("Please select users to disable", "请选择要禁用的用户");
		m.put("Please select users to enable", "请选择要启用的用户");
		m.put("Please specify file name above before editing content", "请在编辑内容之前指定文件名");
		m.put("Please wait...", "请稍候...");
		m.put("Poll Interval", "轮询间隔");
		m.put("Populate Tag Mappings", "填充标签映射");
		m.put("Port", "端口");
		m.put("Post", "发布");
		m.put("Post Build Action", "发布构建操作");
		m.put("Post Build Action Bean", "发布构建操作 Bean");
		m.put("Post Build Actions", "发布构建操作");
		m.put("Post Url", "发布 URL");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "前缀模式");
		m.put("Prepend", "前缀");
		m.put("Preserve Days", "保留天数");
		m.put("Press 'y' to get permalink", "按 'y' 获取永久链接");
		m.put("Prevent Creation", "防止创建");
		m.put("Prevent Deletion", "防止删除");
		m.put("Prevent Forced Push", "防止强制推送");
		m.put("Prevent Update", "防止更新");
		m.put("Preview", "预览");
		m.put("Previous", "上一步");
		m.put("Primary", "主要");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "未指定主 <a wicket:id=\"noPrimaryAddressLink\">电子邮件地址</a>");
		m.put("Primary Email", "主电子邮件");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"主电子邮件地址将用于接收通知、显示 Gravatar（如果启用）等。");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"主或别名邮箱地址作为各种电子邮件通知的发件人地址。如果启用了 <code>Check Incoming Email</code> 选项，用户也可以回复此地址通过电子邮件发布问题或合并请求评论");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"登录到 office 365 邮件服务器发送/接收电子邮件的帐户的主名称。确保此帐户 <b>拥有</b> 上述应用程序 ID 指示的注册应用程序");
		m.put("Private Key Secret", "私钥密钥");
		m.put("Privilege", "权限");
		m.put("Privilege Settings", "权限设置");
		m.put("Profile", "基本信息");
		m.put("Profile updated", "配置已更新");
		m.put("Programming language", "编程语言");
		m.put("Project", "项目");
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
		m.put("Project authorizations updated", "项目授权已更新");
		m.put("Project does not have any code yet", "项目还没有代码");
		m.put("Project id", "项目 ID");
		m.put("Project list", "项目列表");
		m.put("Project name", "项目名称");
		m.put("Project not specified yet", "项目未指定");
		m.put("Project overview", "项目概览");
		m.put("Project path", "项目路径");
		m.put("Projects", "项目");
		m.put("Projects Bean", "项目 Bean");
		m.put("Prompt Fields", "提示字段");
		m.put("Properties", "属性");
		m.put("Provide server id (guild id) to restrict access only to server members", "提供服务器 ID（公会 ID）以仅限制对服务器成员的访问");
		m.put("Proxy", "代理");
		m.put("Prune Builder Cache", "清理构建器缓存");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"此步骤调用 docker builder prune 命令来删除服务器 docker 执行器或远程 docker 执行器中指定的 buildx 构建器缓存");
		m.put("Public", "公开");
		m.put("Public Role", "公共角色");
		m.put("Publish Coverage Report Step", "发布覆盖率报告步骤");
		m.put("Publish Problem Report Step", "发布问题报告步骤");
		m.put("Publish Report Step", "发布报告步骤");
		m.put("Publish Unit Test Report Step", "发布单元测试报告步骤");
		m.put("Pull Image", "拉取镜像");
		m.put("Pull Request", "合并请求");
		m.put("Pull Request Filter", "合并请求过滤");
		m.put("Pull Request Management", "合并请求管理");
		m.put("Pull Request Markdown Report", "合并请求 Markdown 报告");
		m.put("Pull Request Notification", "合并请求通知");
		m.put("Pull Request Notification Unsubscribed", "合并请求通知退订");
		m.put("Pull Request Statistics", "合并请求统计");
		m.put("Pull Requests", "合并请求");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"此步骤需要由服务器 docker 执行器、远程 docker 执行器或 Kubernetes 执行器执行");
		m.put("Pull from Remote", "从远程拉取");
		m.put("Pull request", "合并请求");
		m.put("Pull request #{0} already closed", "合并请求 #{0} 已关闭");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"在项目中对多个合并请求进行批量操作的权限");
		m.put("Pull request and code review", "合并请求和代码评审");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "合并请求只能在获得所有审查人员的批准后合并");
		m.put("Pull request discard", "合并请求丢弃");
		m.put("Pull request duration statistics", "合并请求持续时间统计");
		m.put("Pull request frequency statistics", "合并请求频率统计");
		m.put("Pull request is discarded", "合并请求被丢弃");
		m.put("Pull request is merged", "合并请求被合并");
		m.put("Pull request is opened", "合并请求被打开");
		m.put("Pull request list", "合并请求列表");
		m.put("Pull request merge", "合并请求合并");
		m.put("Pull request not exist or access denied", "合并请求不存在或访问被拒绝");
		m.put("Pull request number", "合并请求编号");
		m.put("Pull request open or update", "合并请求打开或更新");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"合并请求查询订阅仅影响新合并请求。要批量管理现有合并请求的订阅状态，请在合并请求页面中按订阅状态过滤合并请求，然后采取适当行动");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"合并请求统计是企业版功能。<a href='https://onedev.io/pricing' target='_blank'>免费试用</a> 30 天");
		m.put("Push Image", "推送镜像");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"此步骤需要由服务器 docker 执行器、远程 docker 执行器或 Kubernetes 执行器执行");
		m.put("Push to Container registry", "推送至容器注册表");
		m.put("Push to Remote", "推送至远程");
		m.put("Pylint Report", "Pylint 报告");
		m.put("Queries", "查询");
		m.put("Query", "查询");
		m.put("Query Watches", "订阅的查询");
		m.put("Query commits", "查询提交");
		m.put("Query not submitted", "查询未提交");
		m.put("Query param", "查询参数");
		m.put("Query/order projects", "查询/排序项目");
		m.put("Query/order pull requests", "查询/排序合并请求");
		m.put("Quick Search", "快速搜索");
		m.put("Quote", "引用");
		m.put("RESTful API", "RESTful API");
		m.put("Read", "读取");
		m.put("Read body", "读取正文");
		m.put("Readiness Check Command", "准备检查命令");
		m.put("Rebase source branch commits", "Rebase 源分支上的提交");
		m.put("Receive Posted Email", "接收已发布邮件");
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
		m.put("Reference Build", "引用构建");
		m.put("Reference Issue", "引用工单");
		m.put("Reference Pull Request", "引用合并请求");
		m.put("Refresh Token", "刷新令牌");
		m.put("Refs", "引用");
		m.put("Regenerate this access token", "重新生成此访问令牌");
		m.put("Registry Login", "注册登录");
		m.put("Registry Logins", "注册登录");
		m.put("Registry Url", "注册 URL");
		m.put("Remember Me", "记住我");
		m.put("Remote Docker Executor", "远程 Docker 执行器");
		m.put("Remote Machine", "远程机器");
		m.put("Remote Shell Executor", "远程 Shell 执行器");
		m.put("Remote URL", "远程 URL");
		m.put("Remote Url", "远程 URL");
		m.put("Remove", "删除");
		m.put("Remove Fields", "删除字段");
		m.put("Remove From Current Iteration", "从当前迭代中删除");
		m.put("Remove from batch", "从批量中删除");
		m.put("Remove this file", "删除此文件");
		m.put("Remove this image", "删除此图片");
		m.put("Rename {0}", "重命名 {0}");
		m.put("Renovate CLI Options", "Renovate CLI 选项");
		m.put("Renovate JavaScript Config", "Renovate JavaScript 配置");
		m.put("Reopened pull request \"{0}\" ({1})", "重新打开合并请求 \"{0}\"（{1}）");
		m.put("Replace With", "替换为");
		m.put("Replica Count", "副本数量");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "在项目 \"{1}\" 的文件 \"{0}\" 上回复评论");
		m.put("Reply", "回复");
		m.put("Report Name", "报告名称");
		m.put("Request Scopes", "请求范围");
		m.put("Requested changes to pull request \"{0}\" ({1})", "请求更改合并请求 \"{0}\"（{1}）");
		m.put("Requested for changes", "请求更改");
		m.put("Require Autentication", "要求认证");
		m.put("Require Strict Pull Request Builds", "要求严格合并请求构建");
		m.put("Require Successful", "要求成功");
		m.put("Required Builds", "要求构建");
		m.put("Required Reviewers", "要求审查者");
		m.put("Required Services", "所需服务");
		m.put("Resend Verification Email", "重新发送验证电子邮件");
		m.put("Resend invitation", "重新发送邀请");
		m.put("Resolved", "已解决");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "将项目 \"{1}\" 的文件 \"{0}\" 上的评论标记为已解决");
		m.put("Resource Settings", "资源设置");
		m.put("Retrieve Groups", "检索组");
		m.put("Retrieve LFS Files", "检索 LFS 文件");
		m.put("Retrieve Submodules", "检索子模块");
		m.put("Retry Condition", "重试条件");
		m.put("Retry Delay", "重试延迟");
		m.put("Revert", "回滚");
		m.put("Reverted successfully", "revert 成功");
		m.put("Review required for deletion. Submit pull request instead", "需要审查才能删除。请提交合并请求");
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
		m.put("Ruff Report", "Ruff 报告");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "规则将在用户操作标签时应用此处指定的条件");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"规则仅在用户更改分支时应用此处指定的条件");
		m.put("Run As", "作为");
		m.put("Run Buildx Image Tools", "运行 Buildx 镜像工具");
		m.put("Run Docker Container", "运行 Docker 容器");
		m.put("Run In Container", "在容器中运行");
		m.put("Run Job", "运行任务");
		m.put("Run Options", "运行选项");
		m.put("Run below commands from within your git repository:", "从您的 git 仓库中运行以下命令：");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"使用指定参数运行 docker buildx imagetools 命令。此步骤只能由服务器 docker 执行器或远程 docker 执行器执行");
		m.put("Run job", "运行任务");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"运行 OSV 扫描器扫描各种 <a href='https://deps.dev/' target='_blank'>依赖项</a> 使用的违反许可证。它只能由 docker 感知执行器执行。");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"运行 OSV 扫描器扫描各种 <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>锁定文件</a> 中的漏洞。它只能由 docker 感知执行器执行。");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"运行指定的 docker 容器。<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> 挂载到容器中，其路径放置在环境变量 <code>ONEDEV_WORKSPACE</code> 中。<b class='text-warning'>注意：</b> 此步骤只能由服务器 docker 执行器或远程 docker 执行器执行");
		m.put("Run specified step template", "运行指定的步骤模板");
		m.put("Run this job", "运行此任务");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>It can only be executed by docker aware executor.", 
			"运行 trivy 容器镜像扫描器扫描指定镜像中的问题。对于漏洞，它检查各种 <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>分布文件</a>。它只能由 docker 感知执行器执行。");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"运行 trivy 文件系统扫描器扫描各种 <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>锁定文件</a>。它只能由 docker 感知执行器执行，建议在 <span class='text-warning'>依赖项解析后</span> 运行（例如 npm install）。与 OSV 扫描器相比，它的设置稍微冗长，但可以提供更准确的结果");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"运行 trivy 根文件系统扫描器扫描各种 <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>分布文件</a>。它只能由 docker 感知执行器执行，建议在项目暂存区域运行。");
		m.put("Running", "运行中");
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
		m.put("SSL Setting", "SSL 设置");
		m.put("Save", "保存");
		m.put("Save Query", "保存查询");
		m.put("Save Query Bean", "保存查询 Bean");
		m.put("Save as Mine", "保存为我的");
		m.put("Saved Queries", "保存的查询");
		m.put("Scan Path", "扫描路径");
		m.put("Scan Paths", "扫描路径");
		m.put("Scan below QR code with your TOTP authenticators", "使用您的 TOTP 认证器扫描下方二维码");
		m.put("Schedule Issues", "工单排程");
		m.put("Script Name", "脚本名称");
		m.put("Scripting Value", "脚本值");
		m.put("Search", "搜索");
		m.put("Search Groups Using Filter", "使用过滤器搜索组");
		m.put("Search branch", "搜索分支");
		m.put("Search for", "搜索");
		m.put("Search is too general", "搜索过于笼统");
		m.put("Secret", "密钥");
		m.put("Secret Config File", "密钥配置文件");
		m.put("Secret Setting", "密钥设置");
		m.put("Security", "安全");
		m.put("Security Setting", "安全设置");
		m.put("Security Settings", "安全设置");
		m.put("Select", "选择");
		m.put("Select Branch to Cherry Pick to", "选择分支进行 cherry-pick");
		m.put("Select Branch to Revert on", "选择分支进行 revert");
		m.put("Select Existing", "选择已有");
		m.put("Select Project", "选择项目");
		m.put("Select iteration to schedule issues into", "选择迭代调度问题");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"选择要从中导入的组织。留空以从当前帐户下的存储库导入");
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
		m.put("Sequential Group", "顺序组");
		m.put("Server", "服务器");
		m.put("Server Docker Executor", "服务器 Docker 执行器");
		m.put("Server Id", "服务器 ID");
		m.put("Server Information", "服务器信息");
		m.put("Server Log", "服务器日志");
		m.put("Server Shell Executor", "服务器 Shell 执行器");
		m.put("Server URL", "服务器 URL");
		m.put("Server fingerprint", "服务器指纹");
		m.put("Server host", "服务器主机");
		m.put("Server url", "服务器 URL");
		m.put("Service", "服务");
		m.put("Service Account", "服务帐户");
		m.put("Service Desk", "服务台");
		m.put("Service Desk Email Address", "服务台电子邮件地址");
		m.put("Service Desk Issue Open Failed", "服务台工单开启失败");
		m.put("Service Desk Issue Opened", "服务台工单已开启");
		m.put("Service Desk Setting", "服务台设置");
		m.put("Service Desk Setting Holder", "服务台设置持有者");
		m.put("Service Desk Settings", "服务台设置");
		m.put("Service Locator", "服务定位器");
		m.put("Service Locators", "服务定位器");
		m.put("Service desk setting", "服务台设置");
		m.put("Services", "服务");
		m.put("Set", "设置");
		m.put("Set All Queried As Root Projects", "设置所有查询的项目为根项目");
		m.put("Set All Queried Comments as Resolved", "设置所有选中的评论为已解决");
		m.put("Set All Queried Comments as Unresolved", "设置所有选中的评论为未解决");
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
		m.put("Set as Private", "设为私有");
		m.put("Set as Public", "设为公开");
		m.put("Set reviewed", "设置为已审阅");
		m.put("Set unreviewed", "设置为未审阅");
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
		m.put("Settings", "设置");
		m.put("Settings and permissions of parent project will be inherited by this project", "父项目的设置和权限将继承给此项目");
		m.put("Share dashboard", "共享仪表板");
		m.put("Share with Groups", "与组共享");
		m.put("Share with Users", "与用户共享");
		m.put("Shell", "Shell");
		m.put("Show Branch/Tag", "显示分支/标签");
		m.put("Show Build Status", "显示构建状态");
		m.put("Show Code Stats", "显示代码统计");
		m.put("Show Condition", "显示条件");
		m.put("Show Conditionally", "显示条件");
		m.put("Show Description", "显示描述");
		m.put("Show Duration", "显示持续时间");
		m.put("Show Emojis", "显示表情符号");
		m.put("Show Issue Status", "显示问题状态");
		m.put("Show Package Stats", "显示包统计");
		m.put("Show Pull Request Stats", "显示合并请求统计");
		m.put("Show Saved Queries", "显示保存的查询");
		m.put("Show Works Of", "显示工作量");
		m.put("Show commented code snippet", "显示注释的代码片段");
		m.put("Show emojis", "显示表情符号");
		m.put("Show more lines", "显示更多行");
		m.put("Showing first {0} files as there are too many", "显示前 {0} 个文件，因为文件太多");
		m.put("Sign In", "登录");
		m.put("Sign In To", "登录到");
		m.put("Sign Out", "登出");
		m.put("Sign Up Bean", "注册 Bean");
		m.put("Sign Up!", "注册！");
		m.put("Sign in", "登录");
		m.put("Signature required for this change, please generate system GPG signing key first", "需要签名才能更改。请先生成系统 GPG 签名密钥");
		m.put("Similar Issues", "相似工单");
		m.put("Single Sign On", "单点登录");
		m.put("Single sign on via discord.com", "通过 discord.com 单点登录");
		m.put("Single sign on via twitch.tv", "通过 twitch.tv 单点登录");
		m.put("Site", "站点");
		m.put("Site Publish Enabled", "站点发布已启用");
		m.put("Size invalid", "大小无效");
		m.put("Slack Notifications", "Slack 通知");
		m.put("Smtp Ssl Setting", "SMTP SSL 设置");
		m.put("Smtp With Ssl", "SMTP SSL");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "有人修改了您正在编辑的内容。重新加载页面并重试。");
		m.put("Source", "源");
		m.put("Source Docker Image", "Docker 镜像源");
		m.put("Source Path", "源路径");
		m.put("Specified Value", "指定值");
		m.put("Specified choices", "指定选项");
		m.put("Specified default value", "指定默认值");
		m.put("Specified fields", "指定字段");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"指定 Active Directory 服务器的 LDAP URL，例如：<i>ldap://ad-server</i> 或 <i>ldaps://ad-server</i>。如果您的 LDAP 服务器使用自签名证书进行 ldaps 连接，您需要<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>配置 OneDev 信任证书</a>");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"指定 LDAP URL，例如：<i>ldap://localhost</i> 或 <i>ldaps://localhost</i>。如果您的 LDAP 服务器使用自签名证书进行 ldaps 连接，您需要<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>配置 OneDev 信任证书</a>");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"指定用于用户搜索的基础节点。例如：<i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"指定用户 LDAP 条目中包含属于组的识别名称的属性名称。例如，某些 LDAP 服务器使用属性 <i>memberOf</i> 列出组");
		m.put("Specifies password of above manager DN", "指定上述管理员 DN 的密码");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"指定在找到的组 LDAP 条目中包含组名称的属性。此属性的值将映射到 OneDev 组。此属性通常设置为 <i>cn</i>");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"指定 .net TRX 测试结果文件相对 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>，例如 <tt>TestResults/*.trx</tt>。使用 * 或 ? 进行模式匹配");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"指定 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> 的值是具有代码写入权限的访问令牌。提交、问题和合并请求也将以访问令牌所有者的名义创建");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"指定 <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json 输出文件相对 <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>。此文件可以通过 clippy json 输出选项生成，例如 <code>cargo clippy --message-format json>check-result.json</code>。使用 * 或 ? 进行模式匹配");
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
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> 作为 SSH 私钥");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> 作为访问令牌");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> 作为访问令牌以从上述项目导入构建规范，如果其代码不是公开可访问的");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> 作为密码或访问令牌的注册表");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> 作为访问远程仓库的密码或访问令牌");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> 作为 SSH 认证的私钥。<b class='text-info'>注意：</b>不支持带密码短语的私钥");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> 作为上述用户 SSH 认证的私钥。<b class='text-info'>注意：</b>不支持带密码短语的私钥");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a>，其值是具有上述项目管理权限的访问令牌。注意，如果同步到当前或子项目且构建提交可从默认分支访问，则不需要访问令牌");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"指定一个 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a>，其值是具有上述项目上传缓存权限的访问令牌。注意，如果上传缓存到当前或子项目且构建提交可从默认分支访问，则不需要此属性");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"指定一个 <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron 计划</a>以自动触发任务。<b class='text-info'>注意：</b>为节省资源，cron 表达式中的秒数将被忽略，最小调度间隔为一分钟");
		m.put("Specify a Docker Image to Test Against", "指定要测试的 Docker 镜像");
		m.put("Specify a custom field of Enum type", "指定枚举类型的自定义字段");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "指定用于过滤/排序指定任务已修复工单的默认查询");
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
			"指定一个用作访问令牌的密钥以从上述项目检索制品。如果未指定，将匿名访问项目制品");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"指定一个密钥，其值是具有上述项目上传缓存权限的访问令牌。注意，如果上传缓存到当前或子项目且构建提交可从默认分支访问，则不需要此属性");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"指定 kubectl 用于访问集群的配置文件的绝对路径。留空让 kubectl 自动确定集群访问信息");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"指定 kubectl 工具的绝对路径，例如：<i>/usr/bin/kubectl</i>。如果留空，OneDev 将尝试从系统路径中查找该工具");
		m.put("Specify account name to login to Gmail to send/receive email", "指定用于登录 Gmail 发送/接收电子邮件的账户名");
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
			"指定要检索到 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 的制品。只能检索已发布的制品（通过制品发布步骤）。");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"指定至少 10 个字母数字字符作为密钥，然后在 SendGrid 端添加入站解析条目：<ul><li><code>目标 URL</code> 应设置为 <i>&lt;OneDev 根 URL&gt;/~sendgrid/&lt;密钥&gt;</i>，例如 <i>https://onedev.example.com/~sendgrid/1234567890</i>。注意在生产环境中，应<a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>启用 https</a> 以保护密钥</li><li><code>接收域名</code> 应与上面指定的系统电子邮件地址的域名部分相同</li><li>启用选项 <code>POST 原始完整 MIME 消息</code></li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"指定用户搜索的基础节点。例如：<i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "指定要提交建议更改的分支");
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
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"指定由 Renovate 创建的各种工单的字段以协调依赖更新");
		m.put("Specify fields to be displayed in the issue list", "指定要在工单列表中显示的字段");
		m.put("Specify fields to display in board card", "指定在看板卡片中显示的字段");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作区</a> 要发布的文件。使用 * 或 ? 进行模式匹配");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"指定要创建 md5 校验和的文件。多个文件应以空格分隔。接受 <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> 模式。非绝对路径文件被视为相对于 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作区</a>");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match", 
			"指定要发布的上述目录下的文件。使用 * 或 ? 进行模式匹配");
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
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的 markdown 文件以进行发布");
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
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的 mypy 输出文件。此文件可通过重定向 mypy 输出生成，且<b>不使用 '--pretty' 选项</b>，例如 <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>。使用 * 或 ? 进行模式匹配");
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
		m.put("Specify project to retrieve artifacts from", "指定从中检索产物的项目");
		m.put("Specify projects to update dependencies. Leave empty for current project", "指定要更新依赖的项目。留空表示当前项目");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的 pylint JSON 结果文件。此文件可使用 pylint JSON 输出格式选项生成，例如 <code>--exit-zero --output-format=json:pylint-result.json</code>。注意，违反规则时我们不会使 pylint 命令失败，因为此步骤将根据配置的阈值使构建失败。使用 * 或 ? 进行模式匹配");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"如有必要，指定注册表登录信息。对于内置注册表，使用 <code>@server_url@</code> 作为注册表 URL，<code>@job_token@</code> 作为用户名，访问令牌作为密码");
		m.put("Specify registry url. Leave empty for official registry", "指定注册表 URL。留空表示官方注册表");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"指定在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作空间</a> 下存储 OCI 布局的相对路径");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"指定更改指定路径时所需的审阅者。注意，提交更改的用户被视为已自动审阅该更改");
		m.put("Specify root URL to access this server", "指定访问此服务器的根 URL");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的 ruff JSON 结果文件。此文件可使用 ruff JSON 输出格式选项生成，例如 <code>--exit-zero --output-format json --output-file ruff-result.json</code>。注意，违反规则时我们不会使 ruff 命令失败，因为此步骤将根据配置的阈值使构建失败。使用 * 或 ? 进行模式匹配");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"指定在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作空间</a> 下执行的 shell 命令（在 Linux/Unix 上）或批处理命令（在 Windows 上）");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"指定在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作空间</a> 下执行的 shell 命令");
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
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的报告起始页面，例如：<tt>manual/index.md</tt>");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"指定相对于 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a> 的报告起始页面，例如：api/index.html");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"指定为构建卷请求的存储大小。大小应符合 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes 资源容量格式</a>，例如 <i>10Gi</i>");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"指定用于计算报告中发现问题列值的制表符宽度");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"指定 SCP 命令的目标参数，例如 <code>user@@host:/app</code>。<b class='text-info'>注意：</b> 确保远程主机上已安装 scp 命令");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"指定用于替换匹配工单引用的文本，例如：&lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;这里 $1 和 $2 表示示例工单模式中的捕获组（参见工单模式帮助）");
		m.put("Specify the condition current build must satisfy to execute this action", "指定当前构建必须满足的条件以执行此操作");
		m.put("Specify the condition preserved builds must match", "指定保留的构建必须匹配的条件");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"指定检索组成员信息的策略。要为 LDAP 组分配适当权限，应定义一个同名的 OneDev 组。如果您希望在 OneDev 端管理组成员关系，请使用策略 <tt>不检索组</tt>");
		m.put("Specify timeout in seconds when communicating with mail server", "指定与邮件服务器通信时的超时时间（秒）");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "指定超时时间（秒）。从任务提交时开始计算");
		m.put("Specify title of the issue", "指定工单的标题");
		m.put("Specify url for your brand", "指定您品牌的 URL");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "指定 YouTrack API 的 URL。例如 <tt>http://localhost:8080/api</tt>");
		m.put("Specify user name of above machine for SSH authentication", "指定上述机器的用户名用于 SSH 身份验证");
		m.put("Specify user name of specified registry", "指定指定注册表的用户名");
		m.put("Specify user name of the registry", "指定注册表的用户名");
		m.put("Specify user name to authenticate with", "指定用于身份验证的用户名");
		m.put("Specify value of the environment variable", "指定环境变量的值");
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
		m.put("Spent Time Issue Field", "工单耗时字段");
		m.put("Split", "拆分");
		m.put("Split view", "分割视图");
		m.put("SpotBugs Report", "SpotBugs 报告");
		m.put("Squash source branch commits", "Squash 源分支上的提交");
		m.put("Ssh", "SSH");
		m.put("Ssh Key", "SSH 密钥");
		m.put("Ssh Setting", "SSH 设置");
		m.put("Ssl Setting", "SSL 设置");
		m.put("Sso Connector", "SSO 连接器");
		m.put("Sso Connector Bean", "SSO 连接器 Bean");
		m.put("Sso connectors", "SSO 连接器");
		m.put("Start At", "开始于");
		m.put("Start Date", "开始日期");
		m.put("Start Page", "起始页面");
		m.put("State", "状态");
		m.put("State Spec", "状态规范");
		m.put("State Transitions", "状态转换");
		m.put("State of other issue is transited to", "其他工单的状态转换为");
		m.put("States", "状态");
		m.put("Statistics", "统计");
		m.put("Stats", "统计");
		m.put("Stats Group", "统计组");
		m.put("Step", "步骤");
		m.put("Step Template", "步骤模板");
		m.put("Step Templates", "步骤模板");
		m.put("Step {0} of {1}: ", "步骤 {0}/{1}：");
		m.put("Steps", "步骤");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"步骤将在同一节点上按顺序执行，共享同一 <a href='https://docs.onedev.io/concepts#job-workspace'>任务工作空间</a>");
		m.put("Stopwatch Overdue", "秒表逾期");
		m.put("Storage Setting", "存储设置");
		m.put("Storage file missing", "存储文件缺失");
		m.put("Storage not found", "未找到存储");
		m.put("Stored with Git LFS", "使用 Git LFS 存储");
		m.put("Subject", "主题");
		m.put("Subscription Key", "订阅密钥");
		m.put("Subscription data", "订阅数据");
		m.put("Successful", "成功");
		m.put("Suffix Pattern", "后缀模式");
		m.put("Suggest changes", "建议更改");
		m.put("Suggested change", "建议更改");
		m.put("Suggestion is outdated either due to code change or pull request close", "建议由于代码更改或合并请求关闭而过时");
		m.put("Support & Bug Report", "技术支持与错误报告");
		m.put("Support Request", "支持请求");
		m.put("Swap", "交换");
		m.put("Switch to HTTP(S)", "切换到 HTTP(S)");
		m.put("Switch to SSH", "切换到 SSH");
		m.put("Symbol name", "符号名称");
		m.put("Symbols", "符号");
		m.put("Sync Repository", "同步仓库");
		m.put("System", "系统");
		m.put("System Alert", "系统提醒");
		m.put("System Email Address", "系统电子邮件地址");
		m.put("System Maintenance", "系统维护");
		m.put("System Setting", "系统设置");
		m.put("System Settings", "系统设置");
		m.put("System uuid", "系统 UUID");
		m.put("TRX Report (.net unit test)", "TRX 报告（.NET 单元测试）");
		m.put("Tab Width", "制表符宽度");
		m.put("Tabs", "制表符");
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
		m.put("Task List", "任务列表");
		m.put("Task list", "任务列表");
		m.put("Template Name", "模板名称");
		m.put("Terminal close", "终端关闭");
		m.put("Terminal input", "终端输入");
		m.put("Terminal open", "终端打开");
		m.put("Terminal output", "终端输出");
		m.put("Terminal ready", "终端就绪");
		m.put("Terminal resize", "终端调整大小");
		m.put("Text", "文本");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "接收 Webhook POST 请求的服务器端点 URL");
		m.put("The permission to access build log", "访问构建日志的权限");
		m.put("The permission to access build pipeline", "访问构建流水线的权限");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"手动运行任务的权限。这也隐含访问构建日志、构建流水线和所有已发布报告的权限");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"确保发送到有效负载 URL 的 POST 请求来自 OneDev 的密钥。设置密钥后，您将在 Webhook POST 请求中收到 X-OneDev-Signature 头部");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "然后输入 TOTP 认证器中显示的验证码进行验证");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"分支 <span wicket:id=\"branch\"></span> 有 <a wicket:id=\"openRequests\">打开的合并请求</a>。如果分支被删除，这些合并请求将被丢弃。");
		m.put("There are merge conflicts. You can still create the pull request though", "有合并冲突。您仍然可以创建合并请求");
		m.put("There are unsaved changes, discard and continue?", "有未保存的更改，是否丢弃并继续？");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"这些认证器通常在您的手机上运行，例如 Google Authenticator、Microsoft Authenticator、Authy、1Password 等");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"此 <span wicket:id=\"elementTypeName\"></span> 是从 <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a> 导入的");
		m.put("This account is disabled", "此账户已禁用");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"此地址应在 SendGrid 中为 <code>已验证的发送者</code>，将用作各种电子邮件通知的发送者地址。如果下面启用了 <code>接收已发布电子邮件</code> 选项，用户也可以回复此地址以发布工单或合并请求评论");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"此地址将用作各种电子邮件通知的发送者地址。如果下面启用了 <code>检查接收电子邮件</code> 选项，用户也可以通过电子邮件回复此地址以发布工单或合并请求评论");
		m.put("This change is squashed/rebased onto base branch via a pull request", "此更改已通过合并请求压缩/重定基到基础分支");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "此更改需要由某些任务验证。请提交合并请求");
		m.put("This email address is being used", "此电子邮件地址正在使用");
		m.put("This executor runs build jobs as docker containers on OneDev server", "此执行器在 OneDev 服务器上以 Docker 容器运行构建任务");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"此执行器通过 <a href='/~administration/agents' target='_blank'>代理</a> 在远程机器上以 Docker 容器运行构建任务");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"此执行器在 Kubernetes 集群中以 Pod 形式运行构建任务，无需任何代理。<b class='text-danger'>注意：</b> 确保在系统设置中正确指定服务器 URL，因为任务 Pod 需要访问它以下载源代码和产物");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs via job requirement setting", 
			"此执行器使用 OneDev 服务器的 shell 设施运行构建任务。<br><b class='text-danger'>警告</b>：使用此执行器运行的任务具有与 OneDev 服务器进程相同的权限。确保通过任务要求设置仅允许受信任的任务使用");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as corresponding agent process. Make sure it can only be used by trusted jobs via job requirement setting", 
			"此执行器通过 <a href='/~administration/agents' target='_blank'>代理</a> 使用远程机器的 shell 设施运行构建任务。<br><b class='text-danger'>警告</b>：使用此执行器运行的任务具有与相应代理进程相同的权限。确保通过任务要求设置仅允许受信任的任务使用");
		m.put("This field is required", "此字段是必填的");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"此过滤器用于确定当前用户的 LDAP 条目。例如：<i>(&(uid={0})(objectclass=person))</i>。在此示例中，<i>{0}</i> 表示当前用户的登录名");
		m.put("This is a Git LFS object, but the storage file is missing", "这是一个 Git LFS 对象，但存储文件缺失");
		m.put("This is a built-in role and can not be deleted", "这是一个内置角色，无法删除");
		m.put("This is a disabled service account", "这是一个已禁用的服务账户");
		m.put("This is a service account for task automation purpose", "这是一个用于任务自动化的服务账户");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"此密钥与 {0} 相关联，然而它不是此用户的已验证电子邮件地址");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here", 
			"此密钥用于确定项目层次结构中是否存在缓存命中（按顺序从当前项目搜索到根项目，以下加载密钥同理）。如果缓存的密钥与此处定义的密钥完全相同，则视为缓存命中");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"此密钥用于在项目层次结构中下载和上传缓存（按顺序从当前项目搜索到根项目）");
		m.put("This key or one of its subkey is already in use", "此密钥或其子密钥已被使用");
		m.put("This line has confusable unicode character modification", "此行有可疑的 Unicode 字符修改");
		m.put("This name has already been used by another group", "此名称已被另一个组使用");
		m.put("This name has already been used by another role", "此名称已被另一个角色使用");
		m.put("This name has already been used by another role.", "此名称已被另一个角色使用。");
		m.put("This operation is disallowed by branch protection rule", "此操作被分支保护规则禁止");
		m.put("This permission enables one to access confidential issues", "此权限允许访问机密工单");
		m.put("This permission enables one to schedule issues into iterations", "此权限允许将工单调度到迭代中");
		m.put("This property is imported from {0}", "此属性是从 {0} 导入的");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"如果构建由合并请求触发，此报告将显示在合并请求概览页面中");
		m.put("This shows average duration of merged pull requests over time", "此图表显示合并请求的平均持续时间");
		m.put("This shows number of open and merged pull requests over time", "此图表显示打开和合并的合并请求的数量");
		m.put("This step can only be executed by a docker aware executor", "此步骤只能由支持 Docker 的执行器执行");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"此步骤只能由支持 Docker 的执行器执行。它在 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>任务工作空间</a> 下运行");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"此步骤发布指定文件作为项目网站。项目网站可以通过 <code>http://&lt;onedev 基础 url&gt;/path/to/project/~site</code> 公开访问");
		m.put("This step pulls specified refs from remote. For security reason, it is only allowed to run from default branch", 
			"此步骤从远程拉取指定引用。出于安全原因，仅允许从默认分支运行");
		m.put("This step pushes current commit to same ref on remote", "此步骤将当前提交推送至远程的同一引用");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"此步骤设置 Renovate 缓存。如果需要使用，请将其放置在 Renovate 步骤之前");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"此步骤设置 trivy 数据库缓存以加速各种扫描步骤。如果需要使用，请将其放置在扫描步骤之前");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"此触发器仅在标记的提交可从此处指定的分支访问时适用。多个分支应以空格分隔。使用 '**'、'*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。以 '-' 为前缀表示排除。留空表示匹配所有分支");
		m.put("This user is authenticating via external system.", "此用户通过外部系统进行身份验证。");
		m.put("This user is authenticating via internal database.", "此用户通过内部数据库进行身份验证。");
		m.put("Threads", "线程");
		m.put("Time Estimate Issue Field", "工单时间估算字段");
		m.put("Time Range", "时间范围");
		m.put("Time Spent Issue Field", "工单耗时字段");
		m.put("Time Tracking", "时间跟踪");
		m.put("Time Tracking Setting", "时间跟踪设置");
		m.put("Timed out", "超时");
		m.put("Timeout", "超时");
		m.put("Timesheet", "时间表");
		m.put("Timesheet Setting", "时间表设置");
		m.put("Title", "标题");
		m.put("To Everyone", "对所有人");
		m.put("To State", "到状态");
		m.put("To States", "到状态");
		m.put("Toggle change history", "切换更改历史");
		m.put("Toggle comments", "切换评论");
		m.put("Toggle commits", "切换提交");
		m.put("Toggle dark mode", "切换暗模式");
		m.put("Toggle fixed width font", "切换固定宽度字体");
		m.put("Toggle navigation", "切换导航");
		m.put("Toggle work log", "切换工作日志");
		m.put("Too many commits to load", "加载的提交太多");
		m.put("Toomanyrequests", "请求过多");
		m.put("Top", "顶部");
		m.put("Topo", "拓扑");
		m.put("Touched File", "触及的文件");
		m.put("Touched Files", "触及的文件");
		m.put("Transfer LFS Files", "传输 LFS 文件");
		m.put("Transit manually", "手动转换");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "将工单 \"{0}\" 的状态转换为 \"{1}\"（{2}）");
		m.put("Transition Edit Bean", "转换编辑 Bean");
		m.put("Transition Spec", "转换规范");
		m.put("Triggers", "触发器");
		m.put("Trivy Container Image Scanner", "Trivy 容器镜像扫描器");
		m.put("Trivy Filesystem Scanner", "Trivy 文件系统扫描器");
		m.put("Trivy Rootfs Scanner", "Trivy 根文件系统扫描器");
		m.put("Try EE", "试用企业版");
		m.put("Try Enterprise Edition", "试用企业版");
		m.put("Twitch", "Twitch");
		m.put("Two-factor Authentication", "两阶段验证");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"两阶段验证已启用。请输入显示在您的 TOTP 身份验证器上的代码。如果您遇到问题，请确保 OneDev 服务器和您的设备运行 TOTP 身份验证器的时间同步");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"为增强安全性，您的账户已强制启用两阶段验证。请按照以下步骤进行设置");
		m.put("Two-factor authentication is now configured", "两阶段验证现已配置完成");
		m.put("Type", "类型");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "在下方输入 <code>yes</code> 确认删除所有查询的用户");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "在下方输入 <code>yes</code> 确认删除选定的用户");
		m.put("Type <code>yes</code> below to delete all queried memberships", "在下方输入 <code>yes</code> 删除所有查询的成员");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "在下方输入 <code>yes</code> 以删除所有查询的合并请求");
		m.put("Type <code>yes</code> below to delete selected comments", "在下方输入 <code>yes</code> 以删除选中的评论");
		m.put("Type <code>yes</code> below to delete selected memberships", "在下方输入 <code>yes</code> 删除选定的成员");
		m.put("Type <code>yes</code> below to delete selected pull requests", "在下方输入 <code>yes</code> 以删除选定的合并请求");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "在下方输入 <code>yes</code> 以放弃所有查询的合并请求");
		m.put("Type <code>yes</code> below to discard selected pull requests", "在下方输入 <code>yes</code> 以放弃选定的合并请求");
		m.put("Type password here", "在此输入密码");
		m.put("Type to filter", "过滤");
		m.put("URL", "URL");
		m.put("Unable to change password as you are authenticating via external system", "无法更改密码，因为您正在通过外部系统进行认证");
		m.put("Unable to comment", "无法添加评论");
		m.put("Unable to connect to server", "无法连接到服务器");
		m.put("Unable to create protected branch", "无法创建受保护的分支");
		m.put("Unable to create protected tag", "无法创建受保护的标签");
		m.put("Unable to diff as some line is too long.", "无法比较，因为某些行太长");
		m.put("Unable to diff as the file is too large.", "无法比较，因为文件太大");
		m.put("Unable to notify user as mail service is not configured", "无法通知用户，因为邮件服务未配置");
		m.put("Unable to send verification email as mail service is not configured yet", "无法发送验证电子邮件，因为邮件服务未配置");
		m.put("Unauthorized", "未授权");
		m.put("Undefined Field Resolution", "未定义字段解析");
		m.put("Undefined Field Value Resolution", "未定义字段值解析");
		m.put("Undefined State Resolution", "未定义状态解析");
		m.put("Under which condition this step should run. <b>Successful</b> means all non-optional steps running before this step are successful", 
			"在什么条件下应运行此步骤。<b>成功</b>表示在此步骤之前运行的所有非可选步骤都已成功");
		m.put("Unified", "统一");
		m.put("Unlimited", "无限制");
		m.put("Unlink this issue", "取消链接此工单");
		m.put("Unordered List", "无序列表");
		m.put("Unordered list", "无序列表");
		m.put("Unresolved", "未解决");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "将项目 \"{1}\" 的文件 \"{0}\" 上的评论标记为未解决");
		m.put("Unspecified", "未指定");
		m.put("Unsupported", "不支持");
		m.put("Unverified", "未验证");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "未验证的电子邮件地址不适用于上述功能");
		m.put("Update", "更新");
		m.put("Update Dependencies via Renovate", "通过 Renovate 更新依赖");
		m.put("Update body", "更新主体");
		m.put("Upload", "上传");
		m.put("Upload Access Token Secret", "上传访问令牌密钥");
		m.put("Upload Cache", "上传缓存");
		m.put("Upload Files", "上传文件");
		m.put("Upload Project Path", "上传项目路径");
		m.put("Upload Strategy", "上传策略");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "上传一个 128x128 的透明 PNG 文件，用作暗模式下的标志");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "上传一个 128x128 的透明 PNG 文件，用作亮模式下的标志");
		m.put("Upload avatar", "上传头像");
		m.put("Upload should be less than {0} Mb", "上传应该小于 {0} Mb");
		m.put("Upload to Project", "上传到项目");
		m.put("Uploading file", "上传文件");
		m.put("Url", "URL");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"使用 '**', '*' 或 '?' 进行 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>路径通配符匹配</a>。前缀为 '-' 以排除");
		m.put("Use Avatar Service", "使用头像服务");
		m.put("Use Default", "使用默认");
		m.put("Use For Git Operations", "用于 Git 操作");
		m.put("Use Git in System Path", "使用系统路径中的 Git");
		m.put("Use Hours And Minutes Only", "仅使用小时和分钟");
		m.put("Use Specified Git", "使用指定的 Git");
		m.put("Use Specified curl", "使用指定的 curl");
		m.put("Use Step Template", "使用步骤模板");
		m.put("Use curl in System Path", "使用系统路径中的 curl");
		m.put("Use default", "使用默认");
		m.put("Use default storage class", "使用默认存储类");
		m.put("Use project dependency to retrieve artifacts from other projects", "使用项目依赖从其他项目检索产物");
		m.put("Use specified choices", "使用指定的选项");
		m.put("Use specified default value", "使用指定的默认值");
		m.put("Use specified value or job secret", "使用指定的值或任务密钥");
		m.put("Use specified values or job secrets", "使用指定的值或任务密钥");
		m.put("Use triggers to run the job automatically under certain conditions", "使用触发器在特定条件下自动运行任务");
		m.put("Use value of specified parameter/secret", "使用指定参数/密钥的值");
		m.put("User", "用户");
		m.put("User Authorization Bean", "用户授权 Bean");
		m.put("User Authorizations Bean", "用户授权 Bean");
		m.put("User Email Attribute", "用户电子邮件属性");
		m.put("User Full Name Attribute", "用户全名属性");
		m.put("User Groups Attribute", "用户组属性");
		m.put("User Invitation", "用户邀请");
		m.put("User Management", "用户管理");
		m.put("User Match Criteria", "用户匹配条件");
		m.put("User Name", "用户名");
		m.put("User Overview", "用户概览");
		m.put("User Principal Name", "用户主体名称");
		m.put("User SSH Key Attribute", "用户 SSH 密钥属性");
		m.put("User Search Bases", "用户搜索基础");
		m.put("User Search Filter", "用户搜索过滤器");
		m.put("User avatar will be requested by appending a hash to this url", "用户头像将通过在此 URL 后附加哈希值来请求");
		m.put("User can sign up if this option is enabled", "如果启用此选项，用户可以注册");
		m.put("User disabled", "用户已禁用");
		m.put("User name", "用户名");
		m.put("User name already used by another account", "用户名已被另一个账户使用");
		m.put("Users", "用户");
		m.put("Users deleted successfully", "用户删除成功");
		m.put("Users disabled successfully", "用户禁用成功");
		m.put("Users enabled successfully", "用户启用成功");
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
		m.put("Viewer", "查看者");
		m.put("Volume Mount", "卷挂载");
		m.put("Volume Mounts", "卷挂载");
		m.put("Waiting", "等待");
		m.put("Watch", "观察");
		m.put("Watch Status", "订阅状态");
		m.put("Watch status changed", "监视状态已更改");
		m.put("Watch/Unwatch All Queried Pull Requests", "监视/取消监视所有查询的合并请求");
		m.put("Watch/Unwatch Selected Pull Requests", "监视/取消监视选定的合并请求");
		m.put("Web Hook", "Web Hook");
		m.put("Web Hooks", "Web Hooks");
		m.put("Web Hooks Bean", "Web Hooks Bean");
		m.put("Webhook Url", "Webhook URL");
		m.put("Week", "周");
		m.put("When", "何时");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"当授权一个项目时，所有子项目也将被授权分配的角色");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"确定用户是否是 git 提交的作者/提交者时，将检查此处列出的所有电子邮件");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"当合并请求的目标分支有新提交时，合并请求的合并提交将被重新计算，此选项决定是否接受在之前合并提交上运行的合并请求构建。如果启用，您需要在新的合并提交上重新运行所需的构建。此设置仅在指定了所需构建时生效");
		m.put("When this work starts", "此工作开始时");
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
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. You should configure job requirement above to make sure the executor can only be used by trusted jobs if this option is enabled", 
			"是否将 Docker sock 挂载到任务容器中以支持任务命令中的 Docker 操作<br><b class='text-danger'>警告</b>：恶意任务通过操作挂载的 Docker sock 可以控制整个 OneDev。如果启用此选项，您应配置上述任务要求以确保执行器仅被受信任的任务使用");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"是否在下一页预填充标签映射。如果要显示的标签过多，您可能希望禁用此选项");
		m.put("Whether or not to require this dependency to be successful", "是否要求此依赖成功");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"是否检索登录用户的组。如果启用此选项，请确保通过在 Entra ID 中注册的应用程序的令牌配置添加组声明。在这种情况下，组声明应通过各种令牌类型返回组 ID（默认选项）");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"是否检索子模块。请参阅 <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>此教程</a>，了解如何设置上述克隆凭据以检索子模块");
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
		m.put("Widget", "小部件");
		m.put("Widget Tab", "小部件标签");
		m.put("Widget Timesheet Setting", "小部件时间表设置");
		m.put("Will be transcoded to UTF-8", "将被转码为 UTF-8");
		m.put("Window", "窗口");
		m.put("Window Memory", "窗口内存");
		m.put("Working Directory", "工作目录");
		m.put("Write", "写入");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "您有未验证的 <a wicket:id=\"hasUnverifiedLink\">电子邮件地址</a>");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "您也可以将文件/图像拖到输入框中，或从剪贴板粘贴图像");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"您可以通过 <a wicket:id=\"addFiles\" class=\"link-primary\">添加文件</a>、<a wicket:id=\"setupBuildSpec\" class=\"link-primary\">定义构建规范</a> 或 <a wicket:id=\"pushInstructions\" class=\"link-primary\">推送现有仓库</a> 来初始化项目");
		m.put("You selected to delete branch \"{0}\"", "您选择删除分支 \"{0}\"");
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
		m.put("[Reset Password] Please Reset Your OneDev Password", "[重置密码] 请重置您的 OneDev 密码");
		m.put("access [{0}]", "访问 [{0}]");
		m.put("artifacts", "制品");
		m.put("assign to me", "分配给我");
		m.put("branches", "分支");
		m.put("builds", "构建");
		m.put("choose a color for this state", "选择一个颜色");
		m.put("cmd-k to show command palette", "cmd-k 显示命令面板");
		m.put("common", "公共");
		m.put("container-image", "容器镜像");
		m.put("ctrl-k to show command palette", "ctrl-k 显示命令面板");
		m.put("curl Command Line", "curl 命令行");
		m.put("curl Path", "curl 路径");
		m.put("default", "默认");
		m.put("descending", "降序");
		m.put("disabled", "禁用");
		m.put("does not have any value of", "没有下列任何值");
		m.put("duration", "持续时间");
		m.put("exclude", "排除");
		m.put("false", "假");
		m.put("found 1 comment", "找到 1 条评论");
		m.put("found 1 project", "找到 1 个项目");
		m.put("found 1 pull request", "找到 1 个合并请求");
		m.put("found 1 user", "找到 1 个用户");
		m.put("found {0} comments", "找到 {0} 条评论");
		m.put("found {0} projects", "找到 {0} 个项目");
		m.put("found {0} pull requests", "找到 {0} 个合并请求");
		m.put("found {0} users", "找到 {0} 个用户");
		m.put("has any value of", "具有下列任何值");
		m.put("ineffective", "无效");
		m.put("is empty", "为空");
		m.put("is not empty", "不为空");
		m.put("issues", "工单");
		m.put("lines", "行");
		m.put("log", "日志");
		m.put("manage job", "管理任务");
		m.put("may not be empty", "不能为空");
		m.put("md.heading", "标题");
		m.put("md.image", "图片");
		m.put("merged", "已合并");
		m.put("month.Apr", "四月");
		m.put("month.Aug", "八月");
		m.put("month.Dec", "十二月");
		m.put("month.Feb", "二月");
		m.put("month.Jan", "一月");
		m.put("month.Jul", "七月");
		m.put("month.Jun", "六月");
		m.put("month.Mar", "三月");
		m.put("month.May", "五月");
		m.put("month.Nov", "十一月");
		m.put("month.Oct", "十月");
		m.put("month.Sep", "九月");
		m.put("new field", "新字段");
		m.put("on file {0}", "在文件 {0} 上");
		m.put("opened", "已创建");
		m.put("outdated", "过时");
		m.put("packages", "包");
		m.put("personal", "个人");
		m.put("pipeline", "流水线");
		m.put("pull request #{0}", "合并请求 #{0}");
		m.put("pull requests", "合并请求");
		m.put("reports", "报告");
		m.put("root", "根");
		m.put("run job", "运行任务");
		m.put("search in this revision will be accurate after indexed", "索引完成后，在当前版本中的搜索将更准确");
		m.put("service", "服务");
		m.put("skipped {0} lines", "跳过 {0} 行");
		m.put("space", "空格");
		m.put("submit", "提交");
		m.put("tags", "标签");
		m.put("true", "真");
		m.put("w%02d", "第 %d 周");
		m.put("week.Fri", "周五");
		m.put("week.Mon", "周一");
		m.put("week.Sat", "周六");
		m.put("week.Sun", "周日");
		m.put("week.Thu", "周四");
		m.put("week.Tue", "周二");
		m.put("week.Wed", "周三");
		m.put("{0} Month(s)", "{0} 个月");
		m.put("{0} activities on {1}", "{1} 有 {0} 个活动");
		m.put("{0} ahead", "{0} 领先");
		m.put("{0} behind", "{0} 落后");
		m.put("{0} branches", "{0} 个分支");
		m.put("{0} commits", "{0} 个提交");
		m.put("{0} commits ahead of base branch", "{0} 个提交领先于基准分支");
		m.put("{0} commits behind of base branch", "{0} 个提交落后于基准分支");
		m.put("{0} edited {1}", "{0} 编辑于 {1}");
		m.put("{0} files", "{0} 个文件");
		m.put("{0} inaccessible activities", "{0} 个无权查看的活动");
		m.put("{0} reviewed", "{0} 已审查");
		m.put("{0} tags", "{0} 个标签");
		m.put("{0}d", "{0} 天");
		m.put("{0}h", "{0} 小时");
		m.put("{0}m", "{0} 分钟");
		m.put("{0}s", "{0} 秒");
		m.put("{javax.validation.constraints.NotEmpty.message}", "不能为空");
		m.put("{javax.validation.constraints.NotNull.message}", "不能为空");
		m.put("{javax.validation.constraints.Size.message}", "至少需要指定一个值");

		// Manually added keys
		m.put("Create Administrator Account", "创建管理员账户");
		m.put("Server Setup", "服务器设置");	
		m.put("Specify System Settings", "指定系统设置");
		m.put("adding .onedev-buildspec.yml", "添加 .onedev-buildspec.yml");
		m.put("WAITING", "等待");
		m.put("PENDING", "待定");
		m.put("FAILED", "失败");
		m.put("CANCELLED", "已取消");
		m.put("TIMED_OUT", "已超时");
		m.put("SUCCESSFUL", "成功");		
		m.put("OPEN", "打开");
		m.put("MERGED", "已合并");
		m.put("DISCARDED", "已放弃");
		m.put("Container Image(s)", "容器镜像");
		m.put("RubyGems(s)", "RubyGems");
		m.put("NPM(s)", "NPM");
		m.put("Maven(s)", "Maven");
		m.put("NuGet(s)", "NuGet");
		m.put("PyPI(s)", "PyPI");
		m.put("Helm(s)", "Helm");
		m.put("job", "任务");
		m.put("service", "服务");
		m.put("step template", "步骤模板");
		m.put("property", "属性");		
		m.put("Successful", "成功");
		m.put("Always", "总是");
		m.put("Never", "从不");
		m.put("Yes", "是");
		m.put("Security & Compliance", "安全与合规");
		m.put("Dependency Management", "依赖管理");
		m.put("Publish", "发布");
		m.put("Repository Sync", "仓库同步");
		m.put("Utilities", "工具");
		m.put("Docker Image", "Docker 镜像");
		m.put("Unified view", "统一视图");
		m.put("Split view", "分割视图");
		m.put("Ignore all whitespace", "忽略所有空白");
		m.put("Ignore change whitespace", "忽略空白变化");
		m.put("Ignore leading whitespace", "忽略开始的空白");
		m.put("Ignore trailing whitespace", "忽略结束的空白");
		m.put("Do not ignore whitespace", "不忽略空白");
		m.put("Internal Database", "内部数据库");
		m.put("External System", "外部系统");
		m.put("Tell user to reset password", "告诉用户重置密码");
		m.put("Commits are taken from default branch of non-forked repositories", "代码提交取自非分叉仓库的默认分支");
		m.put("issue", "工单");
		m.put("code commit", "代码提交");
		m.put("pull request and code review", "合并请求和代码评审");
		m.put("Filter pull requests", "过滤合并请求");
		m.put("Filter issues", "过滤工单");
		m.put("Filter issues", "过滤工单");
		m.put("Add all commits from source branch to target branch with a merge commit", "将源分支的所有提交添加到目标分支，并创建合并提交");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", "仅当目标分支不能 fast-forward 到源分支时才创建合并提交");
		m.put("Squash all commits from source branch into a single commit in target branch", "将源分支的所有提交压缩成一个提交，并添加到目标分支");
		m.put("Rebase all commits from source branch onto target branch", "将源分支的所有提交 Rebase 到目标分支");
	}
	
	@Override
	protected Map<String, String> getContents() {
		return m;
	}
	
}
