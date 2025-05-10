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

		// Auto contents
		m.put("6-digits passcode", "6 位数代码");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", "<a wicket:id=\"verifyRecoveryCode\">通过恢复代码验证</a>（如果您无法访问 TOTP 认证器）");
		m.put("Access Tokens", "访问令牌");
		m.put("Add child project", "添加子项目");
		m.put("Add project", "添加项目");
		m.put("Administration", "管理");
		m.put("Agents", "任务代理");
		m.put("Alert Settings", "提醒设置");
		m.put("Alerts", "提醒");
		m.put("Authenticator", "认证器");
		m.put("Branding", "品牌设置");
		m.put("Build Notification", "构建通知");
		m.put("Builds", "构建");
		m.put("Check Workflow Integrity", "检查工作流完整性");
		m.put("Code Search", "代码搜索");
		m.put("Commit Message Fix Patterns", "提交消息修复模式");
		m.put("Commit Notification", "提交通知");
		m.put("Confirm password here", "在此确认密码");
		m.put("Dashboards", "仪表盘");
		m.put("Database Backup", "数据库备份");
		m.put("Default Boards", "默认看板");
		m.put("Delete All Queried Projects", "删除所有查询的项目");
		m.put("Delete Selected Projects", "删除选中的项目");
		m.put("Description Templates", "描述模板");
		m.put("Don't have an account yet?", "没有账户？");
		m.put("Edit Avatar", "编辑头像");
		m.put("Edit saved queries", "编辑保存的查询");
		m.put("Email Addresses", "电子邮件地址");
		m.put("Email Templates", "邮件模板");
		m.put("Email Verification", "邮件验证");
		m.put("Enter your details to login to your account", "输入您的详细信息登录到您的账户");
		m.put("Exit Impersonation", "退出假装");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", "需要一个或多个 <tt>&lt;数字&gt;(h|m)</tt>。例如 <tt>1h 1m</tt> 表示 1 小时 1 分钟");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", "需要一个或多个 <tt>&lt;数字&gt;(w|d|h|m)</tt>。例如 <tt>1w 1d 1h 1m</tt> 表示 1 周（{0} 天）、1 天（{1} 小时）、1 小时和 1 分钟");
		m.put("External Auth Source", "外部认证源");
		m.put("External Issue Transformers", "外部工单转换");
		m.put("Fields", "字段");
		m.put("Files", "文件");
		m.put("Filter", "过滤");
		m.put("Finish", "完成");
		m.put("Forgot Password?", "忘记密码？");
		m.put("GPG Keys", "GPG 密钥");
		m.put("GPG Signing Key", "GPG 签名密钥");
		m.put("GPG Trusted Keys", "GPG 受信任密钥");
		m.put("Global Views", "全局视图");
		m.put("Groovy Scripts", "Groovy 脚本");
		m.put("Group Management", "组管理");
		m.put("Hide saved queries", "隐藏保存的查询");
		m.put("IMPORTANT:", "重要：");
		m.put("Import", "导入");
		m.put("Invalid credentials", "无效的凭据");
		m.put("Invitations", "邀请");
		m.put("Issue Notification", "工单通知");
		m.put("Issue Notification Unsubscribed", "工单通知退订");
		m.put("Issue Settings", "工单设置");
		m.put("Issue Stopwatch Overdue", "工单计时器超时");
		m.put("Issues", "工单");
		m.put("Job Executors", "任务执行器");
		m.put("Label Management", "标签管理");
		m.put("Language", "语言");
		m.put("Links", "链接");
		m.put("Login name or email address", "登录名或电子邮件地址");
		m.put("Mail Service", "邮件服务");
		m.put("Move All Queried Projects To...", "移动所有查询的项目到...");
		m.put("Move Selected Projects To...", "移动选中的项目到...");
		m.put("My Profile", "我的个人资料");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", "新版本可用。红色表示安全/关键更新，黄色表示错误修复，蓝色表示功能更新。点击查看更改。可在系统设置中禁用");
		m.put("Next", "下一步");
		m.put("Ok", "确定");
		m.put("Operations", "操作");
		m.put("Order By", "排序");
		m.put("Package Notification", "包通知");
		m.put("Packages", "包");
		m.put("Password", "密码");
		m.put("Password Reset", "密码重置");
		m.put("Performance Settings", "性能设置");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", "请<a wicket:id=\"download\" class=\"font-weight-bolder\">下载</a>下方的恢复代码并妥善保管。这些代码可用于在您无法访问认证应用程序时提供一次性账户访问权限。它们<b>不会</b>再次显示");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", "请输入在启用两步验证时保存的恢复代码");
		m.put("Previous", "上一步");
		m.put("Profile", "个人资料");
		m.put("Projects", "项目");
		m.put("Pull Request Notification", "拉取请求通知");
		m.put("Pull Request Notification Unsubscribed", "拉取请求通知退订");
		m.put("Pull Requests", "拉取请求");
		m.put("Query", "查询");
		m.put("Query Watches", "订阅的查询");
		m.put("Query/order projects", "查询/排序项目");
		m.put("Recovery code", "恢复代码");
		m.put("Remember Me", "记住我");
		m.put("Role Management", "角色管理");
		m.put("SSH & GPG Keys", "SSH 和 GPG 密钥");
		m.put("SSH Keys", "SSH 密钥");
		m.put("SSH Server Key", "SSH 服务器密钥");
		m.put("Save Query", "保存查询");
		m.put("Saved Queries", "保存的查询");
		m.put("Scan below QR code with your TOTP authenticators", "使用您的 TOTP 认证器扫描下方二维码");
		m.put("Security Settings", "安全设置");
		m.put("Server Information", "服务器信息");
		m.put("Server Log", "服务器日志");
		m.put("Service Desk Issue Open Failed", "服务台工单开启失败");
		m.put("Service Desk Issue Opened", "服务台工单已开启");
		m.put("Service Desk Settings", "服务台设置");
		m.put("Set All Queried As Root Projects", "设置所有查询的项目为根项目");
		m.put("Set Selected As Root Projects", "设置选中的项目为根项目");
		m.put("Set up two-factor authentication", "设置两步验证");
		m.put("Show Saved Queries", "显示保存的查询");
		m.put("Sign In", "登录");
		m.put("Sign In To", "登录到");
		m.put("Sign Out", "登出");
		m.put("Sign Up!", "注册！");
		m.put("Sign in", "登录");
		m.put("Single Sign On", "单点登录");
		m.put("State Transitions", "状态转换");
		m.put("States", "状态");
		m.put("Step {0} of {1}: ", "步骤 {0}/{1}：");
		m.put("Storage not found", "未找到存储");
		m.put("Symbols", "符号");
		m.put("System Alert", "系统提醒");
		m.put("System Maintenance", "系统维护");
		m.put("System Settings", "系统设置");
		m.put("Text", "文本");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "然后输入 TOTP 认证器中显示的验证码进行验证");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", "这些认证器通常在您的手机上运行，例如 Google Authenticator、Microsoft Authenticator、Authy、1Password 等");
		m.put("This account is disabled", "此账户已禁用");
		m.put("This is a disabled service account", "这是一个已禁用的服务账户");
		m.put("This is a service account for task automation purpose", "这是一个用于任务自动化的服务账户");
		m.put("Time Tracking", "时间跟踪");
		m.put("Toggle dark mode", "切换暗模式");
		m.put("Two-factor Authentication", "两步验证");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", "两步验证已启用。请输入显示在您的 TOTP 身份验证器上的代码。如果您遇到问题，请确保 OneDev 服务器和您的设备运行 TOTP 身份验证器的时间同步");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", "为增强安全性，您的账户已强制启用两步验证。请按照以下步骤进行设置");
		m.put("Two-factor authentication is now configured", "两步验证现已配置完成");
		m.put("Type password here", "在此输入密码");
		m.put("User Invitation", "用户邀请");
		m.put("User Management", "用户管理");
		m.put("User name", "用户名");
		m.put("Users", "用户");
		m.put("Verify", "验证");
		m.put("You are authenticating via external system", "您正在通过外部系统进行认证");
		m.put("You are authenticating via internal database", "您正在通过内部数据库进行认证");
		m.put("You've been logged out", "您已登出");
		m.put("cmd-k to show command palette", "cmd-k 显示命令面板");
		m.put("ctrl-k to show command palette", "ctrl-k 显示命令面板");

		// Manual contents
		m.put("Create Administrator Account", "创建管理员账户");
		m.put("Server Setup", "服务器设置");	
		m.put("Specify System Settings", "指定系统设置");
	}
	
	@Override
	protected Map<String, String> getContents() {
		return m;
	}
	
}
