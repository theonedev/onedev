package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_ja extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init(m);
		Translation.watchUpdate(Translation_ja.class, () -> {
			init(m);
		});
	}

	@SystemPrompt("You are good at translating from English to Japanese in DevOps software area.")
	public static void init(Map<String, String> m) {
		m.clear();
		m.put(" Project path can be omitted if reference from current project", "プロジェクトパスは現在のプロジェクトから参照する場合、省略可能です");
		m.put("'..' is not allowed in the directory", "'..' はディレクトリ内で使用できません");
		m.put("(* = any string, ? = any character)", "(* = 任意の文字列, ? = 任意の文字)");
		m.put("(on behalf of <b>{0}</b>)", "(<b>{0}</b> の代理として)");
		m.put("** Enterprise edition is disabled as the subscription was expired. Renew to enable **", 
			"** サブスクリプションが期限切れのため、エンタープライズエディションは無効になっています。更新して有効にしてください **");
		m.put("** Enterprise edition is disabled as the trial subscription was expired, order subscription to enable or contact support@onedev.io if you need to extend your trial **", 
			"** トライアルサブスクリプションが期限切れのため、エンタープライズ版は無効化されています。有効化するにはサブスクリプションを注文するか、トライアル延長が必要な場合は support@onedev.io にお問い合わせください **");
		m.put("** Enterprise edition is disabled as there is no remaining user months. Order more to enable **", 
			"** 残りのユーザ月がないため、エンタープライズ版は無効化されています。有効化するには追加注文してください **");
		m.put("1. To use this package, add below to project pom.xml", "1. このパッケージを使用するには、以下をプロジェクトの pom.xml に追加してください");
		m.put("1. Use below repositories in project pom.xml", "1. プロジェクトの pom.xml に以下のリポジトリを使用してください");
		m.put("1w 1d 1h 1m", "1w 1d 1h 1m");
		m.put("2. Add below to <code>$HOME/.m2/settings.xml</code> if you want to deploy from command line", 
			"2. コマンドラインからデプロイする場合、<code>$HOME/.m2/settings.xml</code> に以下を追加してください");
		m.put("2. Also add below to $HOME/.m2/settings.xml if you want to compile project from command line", 
			"2. コマンドラインからプロジェクトをコンパイルする場合、$HOME/.m2/settings.xml に以下を追加してください");
		m.put("3. For CI/CD job, it is more convenient to use a custom settings.xml, for instance via below code in a command step:", 
			"3. CI/CD ジョブでは、カスタム settings.xml を使用する方が便利です。例えば、以下のコードをコマンドステップで使用してください:");
		m.put("6-digits passcode", "6桁のパスコード");
		m.put("7 days", "7日間");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">ユーザー</a>のパスワードをリセットするため");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">ユーザー</a>のメールを確認するため");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub フレーバードマークダウン</a>が使用可能で、<a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid と katex のサポート</a>があります。");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>通知をトリガーするイベントオブジェクト</a>");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>表示するアラート</a>");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a> overdue", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>ストップウォッチ</a>が期限切れ");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> が <span wicket:id=\"date\"></span> にコミットしました");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed with <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> が <a wicket:id=\"committer\" class=\"name link-gray\"></a> と共に <span wicket:id=\"date\"></span> にコミットしました");
		m.put("<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depends on me", "<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> が私に依存しています");
		m.put("<a wicket:id=\"removePassword\">Remove password</a> to force the user to authenticate via external system", 
			"<a wicket:id=\"removePassword\">パスワードを削除</a>して、ユーザーが外部システムを介して認証するように強制します");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">リカバリーコードで確認</a>してください。TOTP認証器にアクセスできない場合");
		m.put("<b class='text-danger'>NOTE: </b> This requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>注意: </b> これはエンタープライズサブスクリプションが必要です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料で試す</a>");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>注意: </b> このステップはエンタープライズサブスクリプションが必要です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料で試す</a>");
		m.put("<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>注意: </b>SendGridの統合はエンタープライズ機能です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料で試す</a>");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>注意: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>タイムトラッキング</a>はエンタープライズ機能です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料で試す</a>");
		m.put("<b>NOTE: </b> Service desk only takes effect if <a wicket:id=\"mailConnector\">mail service</a> is defined and its <tt>check incoming email</tt> option is enabled. Also <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>sub addressing</a> needs to be enabled for the system email address. Check <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>this tutorial</a> for details", 
			"<b>注意: </b> サービスデスクは、<a wicket:id=\"mailConnector\">メールサービス</a>が定義され、その<tt>受信メールを確認</tt>オプションが有効になっている場合にのみ有効になります。また、システムメールアドレスの<a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>サブアドレッシング</a>を有効にする必要があります。詳細は<a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>このチュートリアル</a>を確認してください");
		m.put("<b>NOTE:</b> Batch editing issues will not cause state transitions of other issues even if transition rule matches", 
			"<b>注意:</b> 問題の一括編集は、遷移ルールが一致しても他の問題の状態遷移を引き起こしません");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>プロジェクトオーナー</b>は、プロジェクトに対する完全な権限を持つ組み込みの役割です");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>ヒント: </b> <tt>@</tt> を入力して<a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>変数を挿入</a>します。リテラル<tt>@</tt>には<tt>@@</tt>を使用してください");
		m.put("<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>ファイルを検索</span> <span class='font-size-sm text-muted'>デフォルトブランチ内</span></div>");
		m.put("<div><span>Search Symbols</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>シンボルを検索</span> <span class='font-size-sm text-muted'>デフォルトブランチ内</span></div>");
		m.put("<div><span>Search Text</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>テキストを検索</span> <span class='font-size-sm text-muted'>デフォルトブランチ内</span></div>");
		m.put("<i>No Name</i>", "<i>名前なし</i>");
		m.put("<span class=\"keycap ml-3 mr-1\">esc</span> to close", "<span class=\"keycap ml-3 mr-1\">esc</span> で閉じる");
		m.put("<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> to move", 
			"<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> で移動");
		m.put("<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> to navigate. <span class=\"keycap\">Esc</span> to close", 
			"<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> でナビゲートします。<span class=\"keycap\">Esc</span> で閉じます");
		m.put("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete.", 
			"<span class='keycap'>Tab</span> または <span class='keycap'>Enter</span> で完了します。");
		m.put("<span class='keycap'>Tab</span> to complete.", "<span class='keycap'>Tab</span> で完了します。");
		m.put("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>", "<span class='text-nowrap'><span class='keycap'>Enter</span> で進む</span>");
		m.put("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>", "<span class='text-nowrap'><span class='keycap'>Tab</span> で検索</span>");
		m.put("<span wicket:id=\"activityCount\"></span> activities", "<span wicket:id=\"activityCount\"></span> アクティビティ");
		m.put("<svg class='icon mr-2'><use xlink:href='%s'/></svg> Define job secrets to be used in build spec. Secrets with <b>same name</b> can be defined. For a particular name, the first authorized secret with that name will be used (search in current project first, then search in parent projects). Note that secret value containing line breaks or less than <b>%d</b> characters will not be masked in build log", 
			"<svg class='icon mr-2'><use xlink:href='%s'/></svg> ビルド仕様で使用するジョブシークレットを定義します。同じ<b>名前</b>のシークレットを定義できます。特定の名前については、その名前で最初に認可されたシークレットが使用されます（まず現在のプロジェクトで検索し、次に親プロジェクトで検索します）。行の改行を含むか、<b>%d</b>文字未満のシークレット値はビルドログでマスクされませんので注意してください");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here", 
			"ここには<a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Javaパターン</a>が必要です");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"コミットメッセージのフッターを検証するための<a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java正規表現</a>");
		m.put("A child project with name \"{0}\" already exists under \"{1}\"", "\"{1}\" の下に \"{0}\" という名前の子プロジェクトがすでに存在します");
		m.put("A file exists where you’re trying to create a subdirectory. Choose a new path and try again..", 
			"サブディレクトリを作成しようとしている場所にファイルが存在します。新しいパスを選択して再試行してください。");
		m.put("A path with same name already exists.Please choose a different name and try again.", 
			"同じ名前のパスが既に存在します。別の名前を選択して再試行してください。");
		m.put("A pull request is open for this change", "この変更に対するプルリクエストが開かれています");
		m.put("A root project with name \"{0}\" already exists", "\"{0}\" という名前のルートプロジェクトがすでに存在します");
		m.put("A {0} used as body of address verification email", "アドレス確認メールの本文として使用される {0}");
		m.put("A {0} used as body of build notification email", "ビルド通知メールの本文として使用される {0}");
		m.put("A {0} used as body of commit notification email", "コミット通知メールの本文として使用される {0}");
		m.put("A {0} used as body of feedback email when failed to open issue via service desk", "サービスデスク経由で問題を開けなかった場合のフィードバックメールの本文として使用される {0}");
		m.put("A {0} used as body of feedback email when issue is opened via service desk", "サービスデスク経由で問題が開かれた場合のフィードバックメールの本文として使用される {0}");
		m.put("A {0} used as body of feedback email when unsubscribed from issue notification", "問題通知から退会した場合のフィードバックメールの本文として使用される {0}");
		m.put("A {0} used as body of feedback email when unsubscribed from pull request notification", 
			"プルリクエスト通知から退会した場合のフィードバックメールの本文として使用される {0}");
		m.put("A {0} used as body of issue stopwatch overdue notification email", "問題のストップウォッチ期限切れ通知メールの本文として使用される {0}");
		m.put("A {0} used as body of package notification email", "パッケージ通知メールの本文として使用される {0}");
		m.put("A {0} used as body of password reset email", "パスワードリセットメールの本文として使用される {0}");
		m.put("A {0} used as body of system alert email", "システムアラートメールの本文として使用される {0}");
		m.put("A {0} used as body of user invitation email", "ユーザー招待メールの本文として使用される {0}");
		m.put("A {0} used as body of various issue notification emails", "さまざまな問題通知メールの本文として使用される {0}");
		m.put("A {0} used as body of various pull request notification emails", "さまざまなプルリクエスト通知メールの本文として使用される {0}");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"例えば、<tt>https://your-domain.atlassian.net/rest/api/3</tt> のような JIRA クラウドインスタンスの API URL");
		m.put("Able to merge without conflicts", "競合なしでマージ可能");
		m.put("Absolute or relative url of the image", "画像の絶対または相対 URL");
		m.put("Absolute or relative url of the link", "リンクの絶対または相対 URL");
		m.put("Access Anonymously", "匿名でアクセス");
		m.put("Access Build Log", "ビルドログにアクセス");
		m.put("Access Build Pipeline", "ビルドパイプラインにアクセス");
		m.put("Access Build Reports", "ビルドレポートにアクセス");
		m.put("Access Confidential Issues", "機密問題にアクセス");
		m.put("Access Time Tracking", "タイムトラッキングにアクセス");
		m.put("Access Token", "アクセストークン");
		m.put("Access Token Authorization Bean", "アクセストークン認証Bean");
		m.put("Access Token Edit Bean", "アクセストークン編集Bean");
		m.put("Access Token Secret", "アクセストークンシークレット");
		m.put("Access Token for Target Project", "ターゲットプロジェクト用アクセストークン");
		m.put("Access Tokens", "アクセストークン");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"アクセストークンは API アクセスおよびリポジトリのプル/プッシュを目的としています。Web UI にサインインするためには使用できません");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"アクセストークンは API アクセスまたはリポジトリのプル/プッシュを目的としています。Web UI にサインインするためには使用できません");
		m.put("Access token regenerated successfully", "アクセストークンが正常に再生成されました");
		m.put("Access token regenerated, make sure to update the token at agent side", "アクセストークンが再生成されました。エージェント側でトークンを更新することを確認してください");
		m.put("Account Email", "アカウントメール");
		m.put("Account Name", "アカウント名");
		m.put("Account is disabled", "アカウントが無効になっています");
		m.put("Account set up successfully", "アカウントが正常に設定されました");
		m.put("Active Directory", "Active Directory");
		m.put("Active Since", "アクティブ開始日");
		m.put("Activities", "アクティビティ");
		m.put("Activity by type", "タイプ別アクティビティ");
		m.put("Add", "追加");
		m.put("Add Executor", "エグゼキュータを追加");
		m.put("Add GPG key", "GPGキーを追加");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "このユーザーが署名したコミット/タグを確認するために、ここにGPGキーを追加してください");
		m.put("Add GPG keys here to verify commits/tags signed by you", "あなたが署名したコミット/タグを確認するために、ここにGPGキーを追加してください");
		m.put("Add GPG public keys to be trusted here. Commits signed with trusted keys will be shown as verified.", 
			"信頼されるGPG公開鍵をここに追加してください。信頼された鍵で署名されたコミットは確認済みとして表示されます。");
		m.put("Add Issue...", "問題を追加...");
		m.put("Add Issues to Iteration", "イテレーションに問題を追加");
		m.put("Add New", "新規追加");
		m.put("Add New Board", "新しいボードを追加");
		m.put("Add New Email Address", "新しいメールアドレスを追加");
		m.put("Add New Timesheet", "新しいタイムシートを追加");
		m.put("Add Rule", "ルールを追加");
		m.put("Add SSH key", "SSHキーを追加");
		m.put("Add SSO provider", "SSOプロバイダーを追加");
		m.put("Add Spent Time", "使用時間を追加");
		m.put("Add Timesheet", "タイムシートを追加");
		m.put("Add Widget", "ウィジェットを追加");
		m.put("Add a GPG Public Key", "GPG公開鍵を追加");
		m.put("Add a SSH Key", "SSHキーを追加");
		m.put("Add a package source like below", "以下のようにパッケージソースを追加");
		m.put("Add after", "後に追加");
		m.put("Add agent", "エージェントを追加");
		m.put("Add all cards to specified iteration", "すべてのカードを指定されたイテレーションに追加");
		m.put("Add all commits from source branch to target branch with a merge commit", "ソースブランチのすべてのコミットをマージコミットでターゲットブランチに追加");
		m.put("Add assignee...", "担当者を追加...");
		m.put("Add before", "前に追加");
		m.put("Add below to allow accessing via http protocol in new Maven versions", "新しいMavenバージョンでHTTPプロトコル経由のアクセスを許可するために以下を追加");
		m.put("Add child project", "子プロジェクトを追加");
		m.put("Add comment", "コメントを追加");
		m.put("Add comment on this selection", "この選択にコメントを追加");
		m.put("Add custom field", "カスタムフィールドを追加");
		m.put("Add dashboard", "ダッシュボードを追加");
		m.put("Add default issue board", "デフォルトの課題ボードを追加");
		m.put("Add files to current directory", "現在のディレクトリにファイルを追加");
		m.put("Add files via upload", "アップロードでファイルを追加");
		m.put("Add groovy script", "Groovyスクリプトを追加");
		m.put("Add issue description template", "課題説明テンプレートを追加");
		m.put("Add issue link", "課題リンクを追加");
		m.put("Add issue state", "課題状態を追加");
		m.put("Add issue state transition", "課題状態遷移を追加");
		m.put("Add link", "リンクを追加");
		m.put("Add new", "新規追加");
		m.put("Add new card to this column", "この列に新しいカードを追加");
		m.put("Add new file", "新しいファイルを追加");
		m.put("Add new import", "新しいインポートを追加");
		m.put("Add new issue creation setting", "新しい課題作成設定を追加");
		m.put("Add new job dependency", "新しいジョブ依存関係を追加");
		m.put("Add new param", "新しいパラメータを追加");
		m.put("Add new post-build action", "新しいポストビルドアクションを追加");
		m.put("Add new project dependency", "新しいプロジェクト依存関係を追加");
		m.put("Add new step", "新しいステップを追加");
		m.put("Add new trigger", "新しいトリガーを追加");
		m.put("Add project", "プロジェクトを追加");
		m.put("Add reviewer...", "レビュアーを追加...");
		m.put("Add to batch to commit with other suggestions later", "後で他の提案と一緒にコミットするためにバッチに追加");
		m.put("Add to group...", "グループに追加...");
		m.put("Add to iteration...", "イテレーションに追加...");
		m.put("Add user to group...", "グループにユーザーを追加...");
		m.put("Add value", "値を追加");
		m.put("Add {0}", "{0}を追加");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "コミット \"{0}\" を追加 (<i class='text-danger'>リポジトリに存在しない</i>)");
		m.put("Added commit \"{0}\" ({1})", "コミット \"{0}\" を追加 ({1})");
		m.put("Added to group", "グループに追加されました");
		m.put("Additions", "追加");
		m.put("Administration", "管理");
		m.put("Administrative permission over a project", "プロジェクトに対する管理権限");
		m.put("Advanced Search", "高度な検索");
		m.put("After modification", "変更後");
		m.put("Agent", "エージェント");
		m.put("Agent Attribute", "エージェント属性");
		m.put("Agent Count", "エージェント数");
		m.put("Agent Edit Bean", "エージェント編集Bean");
		m.put("Agent Selector", "エージェントセレクター");
		m.put("Agent is designed to be maintenance free. Once connected to server, it will be updated automatically upon server upgrade", 
			"エージェントはメンテナンスフリーに設計されています。サーバーに接続すると、サーバーのアップグレード時に自動的に更新されます");
		m.put("Agent removed", "エージェントが削除されました");
		m.put("Agent tokens are used to authorize agents. It should be configured via environment variable <tt>agentToken</tt> if agent runs as docker container, or property <tt>agentToken</tt> in file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> if agent runs on bare metal/virtual machine. A token will be in-use and removed from this list if agent using it connects to server", 
			"エージェントトークンはエージェントを認証するために使用されます。エージェントがDockerコンテナとして実行される場合は環境変数<tt>agentToken</tt>を介して、またはベアメタル/仮想マシンで実行される場合はファイル<tt>&lt;agent dir&gt;/conf/agent.properties</tt>のプロパティ<tt>agentToken</tt>を介して設定する必要があります。トークンは使用中であり、エージェントがそれを使用してサーバーに接続するとこのリストから削除されます");
		m.put("Agents", "エージェント");
		m.put("Agents can be used to execute jobs on remote machines. Once started it will update itself from server automatically when necessary", 
			"エージェントはリモートマシンでジョブを実行するために使用できます。起動すると必要に応じてサーバーから自動的に更新されます");
		m.put("Aggregated from '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':", "'<span wicket:id=\"estimatedTimeAggregationLink\"></span>'から集計:");
		m.put("Aggregated from '<span wicket:id=\"spentTimeAggregationLink\"></span>':", "'<span wicket:id=\"spentTimeAggregationLink\"></span>'から集計:");
		m.put("Aggregation Link", "集計リンク");
		m.put("Alert", "アラート");
		m.put("Alert Setting", "アラート設定");
		m.put("Alert Settings", "アラート設定");
		m.put("Alert settings have been updated", "アラート設定が更新されました");
		m.put("Alerts", "アラート");
		m.put("All", "すべて");
		m.put("All Issues", "すべての課題");
		m.put("All RESTful Resources", "すべてのRESTfulリソース");
		m.put("All accessible", "すべてアクセス可能");
		m.put("All builds", "すべてのビルド");
		m.put("All changes", "すべての変更");
		m.put("All except", "すべて除外");
		m.put("All files", "すべてのファイル");
		m.put("All groups", "すべてのグループ");
		m.put("All issues", "すべての課題");
		m.put("All platforms in OCI layout", "OCIレイアウトのすべてのプラットフォーム");
		m.put("All platforms in image", "イメージ内のすべてのプラットフォーム");
		m.put("All possible classes", "すべての可能なクラス");
		m.put("All projects", "すべてのプロジェクト");
		m.put("All projects with code read permission", "コード読み取り権限を持つすべてのプロジェクト");
		m.put("All pull requests", "すべてのプルリクエスト");
		m.put("All users", "すべてのユーザー");
		m.put("Allow Empty", "空を許可");
		m.put("Allow Empty Value", "空の値を許可");
		m.put("Allow Multiple", "複数を許可");
		m.put("Allowed Licenses", "許可されたライセンス");
		m.put("Allowed Self Sign-Up Email Domain", "許可されたセルフサインアップメールドメイン");
		m.put("Always", "常に");
		m.put("Always Pull Image", "常にイメージをプル");
		m.put("An issue already linked for {0}. Unlink it first", "{0}に既にリンクされた課題があります。まずリンクを解除してください");
		m.put("An unexpected exception occurred", "予期しない例外が発生しました");
		m.put("And configure auth token of the registry", "そしてレジストリの認証トークンを設定");
		m.put("Another pull request already open for this change", "この変更に対するプルリクエストはすでに開かれています");
		m.put("Any agent", "任意のエージェント");
		m.put("Any branch", "任意のブランチ");
		m.put("Any commit message", "任意のコミットメッセージ");
		m.put("Any domain", "任意のドメイン");
		m.put("Any file", "任意のファイル");
		m.put("Any issue", "任意の課題");
		m.put("Any job", "任意のジョブ");
		m.put("Any project", "任意のプロジェクト");
		m.put("Any ref", "任意の参照");
		m.put("Any sender", "任意の送信者");
		m.put("Any state", "任意の状態");
		m.put("Any tag", "任意のタグ");
		m.put("Any user", "任意のユーザー");
		m.put("Api Key", "APIキー");
		m.put("Api Token", "APIトークン");
		m.put("Api Url", "API URL");
		m.put("Append", "追加");
		m.put("Applicable Branches", "適用可能なブランチ");
		m.put("Applicable Builds", "適用可能なビルド");
		m.put("Applicable Code Comments", "適用可能なコードコメント");
		m.put("Applicable Commit Messages", "適用可能なコミットメッセージ");
		m.put("Applicable Commits", "適用可能なコミット");
		m.put("Applicable Images", "適用可能な画像");
		m.put("Applicable Issues", "適用可能な課題");
		m.put("Applicable Jobs", "適用可能なジョブ");
		m.put("Applicable Names", "適用可能な名前");
		m.put("Applicable Projects", "適用可能なプロジェクト");
		m.put("Applicable Pull Requests", "適用可能なプルリクエスト");
		m.put("Applicable Senders", "適用可能な送信者");
		m.put("Applicable Users", "適用可能なユーザー");
		m.put("Application (client) ID", "アプリケーション（クライアント）ID");
		m.put("Apply suggested change from code comment", "コードコメントから提案された変更を適用");
		m.put("Apply suggested changes from code comments", "コードコメントから提案された変更を適用");
		m.put("Approve", "承認");
		m.put("Approved", "承認済み");
		m.put("Approved pull request \"{0}\" ({1})", "承認済みプルリクエスト「{0}」({1})");
		m.put("Arbitrary scope", "任意のスコープ");
		m.put("Arbitrary type", "任意のタイプ");
		m.put("Arch Pull Command", "Arch Pullコマンド");
		m.put("Archived", "アーカイブ済み");
		m.put("Arguments", "引数");
		m.put("Artifacts", "アーティファクト");
		m.put("Artifacts to Retrieve", "取得するアーティファクト");
		m.put("As long as a feature can be accessed via url, you can input part of the url to match and jump", 
			"機能がURLでアクセス可能であれば、URLの一部を入力して一致させてジャンプできます");
		m.put("Ascending", "昇順");
		m.put("Assignees", "担当者");
		m.put("Assignees Issue Field", "担当者課題フィールド");
		m.put("Assignees are expected to merge the pull request", "担当者はプルリクエストをマージすることが期待されています");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"担当者はコードの書き込み権限を持ち、プルリクエストのマージを担当します");
		m.put("Asymmetric", "非対称");
		m.put("At least one branch or tag should be selected", "少なくとも1つのブランチまたはタグを選択する必要があります");
		m.put("At least one choice need to be specified", "少なくとも1つの選択肢を指定する必要があります");
		m.put("At least one email address should be configured, please add a new one first", "少なくとも1つのメールアドレスを設定する必要があります。まず新しいものを追加してください");
		m.put("At least one email address should be specified", "少なくとも1つのメールアドレスを指定する必要があります");
		m.put("At least one entry should be specified", "少なくとも1つのエントリを指定する必要があります");
		m.put("At least one event type needs to be selected", "少なくとも1つのイベントタイプを選択する必要があります");
		m.put("At least one field needs to be specified", "少なくとも1つのフィールドを指定する必要があります");
		m.put("At least one project should be authorized", "少なくとも1つのプロジェクトを認可する必要があります");
		m.put("At least one project should be selected", "少なくとも1つのプロジェクトを選択する必要があります");
		m.put("At least one repository should be selected", "少なくとも1つのリポジトリを選択する必要があります");
		m.put("At least one role is required", "少なくとも1つの役割が必要です");
		m.put("At least one role must be selected", "少なくとも1つの役割を選択する必要があります");
		m.put("At least one state should be specified", "少なくとも1つの状態を指定する必要があります");
		m.put("At least one tab should be added", "少なくとも1つのタブを追加する必要があります");
		m.put("At least one user search base should be specified", "少なくとも1つのユーザー検索ベースを指定する必要があります");
		m.put("At least one value needs to be specified", "少なくとも1つの値を指定する必要があります");
		m.put("At least two columns need to be defined", "少なくとも2つの列を定義する必要があります");
		m.put("Attachment", "添付ファイル");
		m.put("Attributes", "属性");
		m.put("Attributes (can only be edited when agent is online)", "属性（エージェントがオンラインのときのみ編集可能）");
		m.put("Attributes saved", "属性が保存されました");
		m.put("Audit", "監査");
		m.put("Audit Log", "監査ログ");
		m.put("Audit Setting", "監査設定");
		m.put("Audit log will be preserved for the specified number of days. This setting applies to all audit events, including system level and project level", 
			"監査ログは指定された日数間保存されます。この設定は、システムレベルおよびプロジェクトレベルを含むすべての監査イベントに適用されます");
		m.put("Auth Source", "認証ソース");
		m.put("Authenticate to Bitbucket Cloud", "Bitbucket Cloudに認証");
		m.put("Authenticate to GitHub", "GitHubに認証");
		m.put("Authenticate to GitLab", "GitLabに認証");
		m.put("Authenticate to Gitea", "Giteaに認証");
		m.put("Authenticate to JIRA cloud", "JIRA Cloudに認証");
		m.put("Authenticate to YouTrack", "YouTrackに認証");
		m.put("Authentication", "認証");
		m.put("Authentication Required", "認証が必要です");
		m.put("Authentication Test", "認証テスト");
		m.put("Authentication Token", "認証トークン");
		m.put("Authenticator", "認証者");
		m.put("Authenticator Bean", "認証者Bean");
		m.put("Author", "作成者");
		m.put("Author date", "作成日");
		m.put("Authored By", "作成者");
		m.put("Authorization", "認可");
		m.put("Authorizations", "認可");
		m.put("Authorize user...", "ユーザーを認可...");
		m.put("Authorized Projects", "認可されたプロジェクト");
		m.put("Authorized Roles", "認可された役割");
		m.put("Auto Merge", "自動マージ");
		m.put("Auto Spec", "自動仕様");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"自動更新チェックは、ブラウザでonedev.ioから画像をリクエストすることで新しいバージョンの利用可能性を確認し、更新の重要度を色で示します。これは、Gravatarがアバター画像をリクエストする方法と同じです。無効にした場合は、セキュリティ/重要な修正があるかどうかを確認するために、定期的に手動で更新を確認することを強くお勧めします（画面左下のヘルプメニューから実行可能）。");
		m.put("Auto-discovered executor", "自動検出されたエグゼキューター");
		m.put("Available Agent Tokens", "利用可能なエージェントトークン");
		m.put("Available Choices", "利用可能な選択肢");
		m.put("Avatar", "アバター");
		m.put("Avatar Service Url", "アバターサービスURL");
		m.put("Avatar and name", "アバターと名前");
		m.put("Back To Home", "ホームに戻る");
		m.put("Backlog", "バックログ");
		m.put("Backlog Base Query", "バックログ基本クエリ");
		m.put("Backup", "バックアップ");
		m.put("Backup Now", "今すぐバックアップ");
		m.put("Backup Schedule", "バックアップスケジュール");
		m.put("Backup Setting", "バックアップ設定");
		m.put("Backup Setting Holder", "バックアップ設定ホルダー");
		m.put("Backup settings updated", "バックアップ設定が更新されました");
		m.put("Bare Metal", "ベアメタル");
		m.put("Base", "ベース");
		m.put("Base Gpg Key", "ベースGpgキー");
		m.put("Base Query", "ベースクエリ");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Base64でエンコードされたPEM形式、-----BEGIN CERTIFICATE-----で始まり、-----END CERTIFICATE-----で終わる");
		m.put("Basic Info", "基本情報");
		m.put("Basic Settings", "基本設定");
		m.put("Basic settings updated", "基本設定が更新されました");
		m.put("Batch Edit All Queried Issues", "クエリされたすべての課題を一括編集");
		m.put("Batch Edit Selected Issues", "選択された課題を一括編集");
		m.put("Batch Editing {0} Issues", "{0}件の課題を一括編集中");
		m.put("Batched suggestions", "一括提案");
		m.put("Before modification", "変更前");
		m.put("Belonging Groups", "所属グループ");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"以下は一般的な条件です。上の検索ボックスに入力して完全なリストと利用可能な組み合わせを表示してください。");
		m.put("Below content is restored from an unsaved change. Clear to discard", "以下の内容は未保存の変更から復元されました。クリアして破棄してください");
		m.put("Below information will also be sent", "以下の情報も送信されます");
		m.put("Binary file.", "バイナリファイル");
		m.put("Bitbucket App Password", "Bitbucketアプリパスワード");
		m.put("Bitbucket Login Name", "Bitbucketログイン名");
		m.put("Bitbucket Repositories to Import", "インポートするBitbucketリポジトリ");
		m.put("Bitbucket Workspace", "Bitbucketワークスペース");
		m.put("Bitbucket app password should be generated with permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>", 
			"Bitbucketアプリパスワードは<b>account/read</b>、<b>repositories/read</b>、<b>issues:read</b>の権限で生成する必要があります");
		m.put("Blame", "責任追跡");
		m.put("Blob", "Blob");
		m.put("Blob hash", "Blobハッシュ");
		m.put("Blob index version", "Blobインデックスバージョン");
		m.put("Blob name", "Blob名");
		m.put("Blob path", "Blobパス");
		m.put("Blob primary symbols", "Blob主要シンボル");
		m.put("Blob secondary symbols", "Blob副次シンボル");
		m.put("Blob symbol list", "Blobシンボルリスト");
		m.put("Blob text", "Blobテキスト");
		m.put("Blob unknown", "Blob不明");
		m.put("Blob upload invalid", "Blobアップロード無効");
		m.put("Blob upload unknown", "Blobアップロード不明");
		m.put("Board", "ボード");
		m.put("Board Columns", "ボード列");
		m.put("Board Spec", "ボード仕様");
		m.put("Boards", "ボード一覧");
		m.put("Body", "本文");
		m.put("Bold", "太字");
		m.put("Both", "両方");
		m.put("Bottom", "下部");
		m.put("Branch", "ブランチ");
		m.put("Branch \"{0}\" already exists, please choose a different name", "ブランチ「{0}」はすでに存在します。別の名前を選んでください");
		m.put("Branch \"{0}\" created", "ブランチ「{0}」が作成されました");
		m.put("Branch \"{0}\" deleted", "ブランチ「{0}」が削除されました");
		m.put("Branch <a wicket:id=\"targetBranch\"></a> is up to date with all commits from <a wicket:id=\"sourceBranch\"></a>. Try <a wicket:id=\"swapBranches\">swap source and target</a> for the comparison.", 
			"ブランチ<a wicket:id=\"targetBranch\"></a>は<a wicket:id=\"sourceBranch\"></a>のすべてのコミットと一致しています。比較のために<a wicket:id=\"swapBranches\">ソースとターゲットを入れ替える</a>を試してください。");
		m.put("Branch Choice Bean", "ブランチ選択Bean");
		m.put("Branch Name", "ブランチ名");
		m.put("Branch Protection", "ブランチ保護");
		m.put("Branch Revision", "ブランチリビジョン");
		m.put("Branch update", "ブランチ更新");
		m.put("Branches", "ブランチ一覧");
		m.put("Brand Setting Edit Bean", "ブランド設定編集Bean");
		m.put("Branding", "ブランディング");
		m.put("Branding settings updated", "ブランディング設定が更新されました");
		m.put("Browse Code", "コードを閲覧");
		m.put("Browse code", "コードを閲覧");
		m.put("Bug Report", "バグ報告");
		m.put("Build", "ビルド");
		m.put("Build #{0} already finished", "ビルド#{0}はすでに終了しています");
		m.put("Build #{0} deleted", "ビルド#{0}が削除されました");
		m.put("Build #{0} not finished yet", "ビルド#{0}はまだ終了していません");
		m.put("Build Artifact Storage", "ビルドアーティファクトストレージ");
		m.put("Build Commit", "ビルドコミット");
		m.put("Build Context", "ビルドコンテキスト");
		m.put("Build Description", "ビルド説明");
		m.put("Build Filter", "ビルドフィルター");
		m.put("Build Image", "ビルドイメージ");
		m.put("Build Image (Kaniko)", "ビルドイメージ（Kaniko）");
		m.put("Build Management", "ビルド管理");
		m.put("Build Notification", "ビルド通知");
		m.put("Build Notification Template", "ビルド通知テンプレート");
		m.put("Build Number", "ビルド番号");
		m.put("Build On Behalf Of", "代理でビルド");
		m.put("Build Path", "ビルドパス");
		m.put("Build Preservation", "ビルド保存");
		m.put("Build Preservations", "ビルド保存一覧");
		m.put("Build Preservations Bean", "ビルド保存Bean");
		m.put("Build Preserve Rules", "ビルド保存ルール");
		m.put("Build Provider", "ビルドプロバイダー");
		m.put("Build Spec", "ビルド仕様");
		m.put("Build Statistics", "ビルド統計");
		m.put("Build Version", "ビルドバージョン");
		m.put("Build Volume Storage Class", "ビルドボリュームストレージクラス");
		m.put("Build Volume Storage Size", "ビルドボリュームストレージサイズ");
		m.put("Build administrative permission for all jobs inside a project, including batch operations over multiple builds", 
			"プロジェクト内のすべてのジョブに対するビルド管理権限、複数のビルドに対する一括操作を含む");
		m.put("Build docker image with docker buildx. This step can only be executed by server docker executor or remote docker executor, and it uses the buildx builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko step instead", 
			"docker buildxを使用してDockerイメージをビルドします。このステップはサーバーDockerエグゼキューターまたはリモートDockerエグゼキューターによってのみ実行可能で、これらのエグゼキューターで指定されたbuildxビルダーを使用してジョブを実行します。Kubernetesエグゼキューターでイメージをビルドするには、kanikoステップを使用してください");
		m.put("Build docker image with kaniko. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"kanikoを使用してDockerイメージをビルドします。このステップはサーバーDockerエグゼキューター、リモートDockerエグゼキューター、またはKubernetesエグゼキューターによって実行する必要があります");
		m.put("Build duration statistics", "ビルド時間統計");
		m.put("Build frequency statistics", "ビルド頻度統計");
		m.put("Build is successful", "ビルドが成功しました");
		m.put("Build list", "ビルドリスト");
		m.put("Build not exist or access denied", "ビルドが存在しないか、アクセスが拒否されました");
		m.put("Build number", "ビルド番号");
		m.put("Build preserve rules saved", "ビルド保持ルールが保存されました");
		m.put("Build required for deletion. Submit pull request instead", "削除にはビルドが必要です。代わりにプルリクエストを送信してください");
		m.put("Build required for this change. Please submit pull request instead", "この変更にはビルドが必要です。代わりにプルリクエストを提出してください。");
		m.put("Build required for this change. Submit pull request instead", "この変更にはビルドが必要です。代わりにプルリクエストを送信してください");
		m.put("Build spec not defined", "ビルド仕様が定義されていません");
		m.put("Build spec not defined (import project: {0}, import revision: {1})", "ビルド仕様が定義されていません（インポートプロジェクト: {0}, インポートリビジョン: {1}）");
		m.put("Build spec not found in commit of this build", "このビルドのコミットにビルド仕様が見つかりません");
		m.put("Build statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"ビルド統計はエンタープライズ機能です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料でお試しください</a>");
		m.put("Build version", "ビルドバージョン");
		m.put("Build with Persistent Volume", "永続ボリュームを使用したビルド");
		m.put("Builds", "ビルド");
		m.put("Builds are {0}", "ビルドは{0}です");
		m.put("Buildx Builder", "Buildxビルダー");
		m.put("Built In Fields Bean", "組み込みフィールドBean");
		m.put("Burndown", "バーンダウン");
		m.put("Burndown chart", "バーンダウンチャート");
		m.put("Button Image Url", "ボタン画像URL");
		m.put("By Group", "グループ別");
		m.put("By User", "ユーザー別");
		m.put("By day", "日別");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"デフォルトではコードは自動生成された資格情報を介してクローンされ、現在のプロジェクトに対して読み取り権限のみを持ちます。ジョブが<a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>コードをサーバーにプッシュ</a>する必要がある場合は、適切な権限を持つカスタム資格情報をここで提供してください");
		m.put("By default issues of parent and child projects will also be listed. Use query <code>&quot;Project&quot; is current</code> to show issues only belong to this project", 
			"デフォルトでは親プロジェクトと子プロジェクトの課題もリストされます。クエリ<code>&quot;Project&quot; is current</code>を使用して、このプロジェクトにのみ属する課題を表示してください");
		m.put("By month", "月別");
		m.put("By week", "週別");
		m.put("Bypass Certificate Check", "証明書チェックをバイパス");
		m.put("CANCELLED", "キャンセル済み");
		m.put("CORS Allowed Origins", "CORS許可オリジン");
		m.put("CPD Report", "CPDレポート");
		m.put("CPU", "CPU");
		m.put("CPU Intensive Task Concurrency", "CPU集約タスクの並行性");
		m.put("CPU capability in millis. This is normally (CPU cores)*1000", "ミリ秒単位のCPU能力。通常は(CPUコア数)*1000");
		m.put("Cache Key", "キャッシュキー");
		m.put("Cache Management", "キャッシュ管理");
		m.put("Cache Paths", "キャッシュパス");
		m.put("Cache Setting Bean", "キャッシュ設定Bean");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "この日数間アクセスされない場合、キャッシュはスペースを節約するために削除されます");
		m.put("Calculating merge preview...", "マージプレビューを計算中...");
		m.put("Callback URL", "コールバックURL");
		m.put("Can Be Used By Jobs", "ジョブで使用可能");
		m.put("Can Create Root Projects", "ルートプロジェクトを作成可能");
		m.put("Can Edit Estimated Time", "推定時間を編集可能");
		m.put("Can not convert root user to service account", "ルートユーザーをサービスアカウントに変換できません");
		m.put("Can not convert yourself to service account", "自分自身をサービスアカウントに変換できません");
		m.put("Can not delete default branch", "デフォルトブランチを削除できません");
		m.put("Can not delete root account", "ルートアカウントを削除できません");
		m.put("Can not delete yourself", "自分自身を削除できません");
		m.put("Can not disable root account", "ルートアカウントを無効化できません");
		m.put("Can not disable yourself", "自分自身を無効化できません");
		m.put("Can not find issue board: ", "課題ボードが見つかりません:");
		m.put("Can not move project \"{0}\" to be under itself or its descendants", "プロジェクト\"{0}\"を自身またはその子孫の下に移動することはできません");
		m.put("Can not perform this operation now", "現在この操作を実行することはできません");
		m.put("Can not reset password for service account or disabled user", "サービスアカウントまたは無効なユーザーのパスワードをリセットできません");
		m.put("Can not reset password for user authenticating via external system", "外部システムで認証するユーザーのパスワードをリセットできません");
		m.put("Can not save malformed query", "不正なクエリを保存することはできません");
		m.put("Can not use current or descendant project as parent", "現在のプロジェクトまたは子孫プロジェクトを親として使用することはできません");
		m.put("Can only compare with common ancestor when different projects are involved", "異なるプロジェクトが関与している場合、共通の祖先との比較のみ可能です");
		m.put("Cancel", "キャンセル");
		m.put("Cancel All Queried Builds", "クエリされたすべてのビルドをキャンセル");
		m.put("Cancel Selected Builds", "選択されたビルドをキャンセル");
		m.put("Cancel invitation", "招待をキャンセル");
		m.put("Cancel request submitted", "キャンセルリクエストが送信されました");
		m.put("Cancel this build", "このビルドをキャンセル");
		m.put("Cancelled", "キャンセル済み");
		m.put("Cancelled By", "キャンセルした人");
		m.put("Case Sensitive", "大文字小文字を区別");
		m.put("Certificates to Trust", "信頼する証明書");
		m.put("Change", "変更");
		m.put("Change Detection Excludes", "変更検出除外");
		m.put("Change My Password", "パスワードを変更");
		m.put("Change To", "変更先");
		m.put("Change already merged", "変更はすでにマージされています");
		m.put("Change not updated yet", "変更はまだ更新されていません");
		m.put("Change property <code>serverUrl</code> in file <code>conf/agent.properties</code> if necessary. The default value is taken from OneDev server url specified in <i>Administration / System Setting</i>", 
			"必要に応じてファイル<code>conf/agent.properties</code>のプロパティ<code>serverUrl</code>を変更してください。デフォルト値は<i>管理 / システム設定</i>で指定されたOneDevサーバーURLから取得されます");
		m.put("Change to another field", "別のフィールドに変更");
		m.put("Change to another state", "別の状態に変更");
		m.put("Change to another value", "別の値に変更");
		m.put("Changes since last review", "最後のレビュー以降の変更");
		m.put("Changes since last visit", "最後の訪問以降の変更");
		m.put("Changes since this action", "このアクション以降の変更");
		m.put("Changes since this comment", "このコメント以降の変更");
		m.put("Channel Notification", "チャンネル通知");
		m.put("Chart Metadata", "チャートメタデータ");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"コミットに署名するためのGPGキーを生成して使用する方法については、<a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHubのガイド</a>を確認してください");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including instructions on how to run agent as service", 
			"詳細については<a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">エージェント管理</a>を確認してください。サービスとしてエージェントを実行する方法を含む指示があります");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including list of supported environment variables", 
			"詳細については<a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">エージェント管理</a>を確認してください。サポートされている環境変数のリストを含みます");
		m.put("Check Commit Message Footer", "コミットメッセージフッターを確認");
		m.put("Check Incoming Email", "受信メールを確認");
		m.put("Check Issue Integrity", "課題の整合性を確認");
		m.put("Check Update", "更新を確認");
		m.put("Check Workflow Integrity", "ワークフローの整合性を確認");
		m.put("Check out to local workspace", "ローカルワークスペースにチェックアウト");
		m.put("Check this to compare right side with common ancestor of left and right", "左側と右側の共通祖先と比較するにはこれをチェックしてください");
		m.put("Check this to enforce two-factor authentication for all users in the system", "システム内のすべてのユーザーに対して二要素認証を強制するにはこれをチェックしてください");
		m.put("Check this to enforce two-factor authentication for all users in this group", "このグループ内のすべてのユーザーに対して二要素認証を強制するにはこれをチェックしてください");
		m.put("Check this to prevent branch creation", "ブランチ作成を防止するにはこれをチェックしてください");
		m.put("Check this to prevent branch deletion", "ブランチ削除を防止するにはこれをチェックしてください");
		m.put("Check this to prevent forced push", "強制プッシュを防止するにはこれをチェックしてください");
		m.put("Check this to prevent tag creation", "タグ作成を防止するにはこれをチェックしてください");
		m.put("Check this to prevent tag deletion", "タグ削除を防止するにはこれをチェックしてください");
		m.put("Check this to prevent tag update", "タグ更新を防止するにはこれをチェックしてください");
		m.put("Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>. Note this is applicable for non-merge commits", 
			"これをチェックして<a href='https://www.conventionalcommits.org' target='_blank'>従来のコミット</a>を要求します。これは非マージコミットに適用されます");
		m.put("Check this to require valid signature of head commit", "これをチェックしてヘッドコミットの有効な署名を要求します");
		m.put("Check this to retrieve Git LFS files", "これをチェックしてGit LFSファイルを取得します");
		m.put("Checkbox", "チェックボックス");
		m.put("Checking field values...", "フィールド値を確認中...");
		m.put("Checking fields...", "フィールドを確認中...");
		m.put("Checking state and field ordinals...", "状態とフィールドの序数を確認中...");
		m.put("Checking state...", "状態を確認中...");
		m.put("Checkout Code", "コードをチェックアウト");
		m.put("Checkout Path", "チェックアウトパス");
		m.put("Checkout Pull Request Head", "プルリクエストヘッドをチェックアウト");
		m.put("Checkout Pull Request Merge Preview", "プルリクエストマージプレビューをチェックアウト");
		m.put("Checkstyle Report", "Checkstyleレポート");
		m.put("Cherry-Pick", "チェリーピック");
		m.put("Cherry-picked successfully", "チェリーピックが成功しました");
		m.put("Child Projects", "子プロジェクト");
		m.put("Child Projects Of", "の子プロジェクト");
		m.put("Choice Provider", "選択プロバイダー");
		m.put("Choose", "選択");
		m.put("Choose JIRA project to import issues from", "JIRAプロジェクトを選択して課題をインポート");
		m.put("Choose Revision", "リビジョンを選択");
		m.put("Choose YouTrack project to import issues from", "YouTrackプロジェクトを選択して課題をインポート");
		m.put("Choose a project...", "プロジェクトを選択...");
		m.put("Choose a user...", "ユーザーを選択...");
		m.put("Choose branch...", "ブランチを選択...");
		m.put("Choose branches...", "ブランチを選択...");
		m.put("Choose build...", "ビルドを選択...");
		m.put("Choose file", "ファイルを選択");
		m.put("Choose group...", "グループを選択...");
		m.put("Choose groups...", "グループを選択...");
		m.put("Choose issue...", "課題を選択...");
		m.put("Choose issues...", "課題を選択...");
		m.put("Choose iteration...", "イテレーションを選択...");
		m.put("Choose iterations...", "イテレーションを選択...");
		m.put("Choose job...", "ジョブを選択...");
		m.put("Choose jobs...", "ジョブを選択...");
		m.put("Choose project", "プロジェクトを選択");
		m.put("Choose projects...", "プロジェクトを選択...");
		m.put("Choose pull request...", "プルリクエストを選択...");
		m.put("Choose repository", "リポジトリを選択");
		m.put("Choose role...", "役割を選択...");
		m.put("Choose roles...", "役割を選択...");
		m.put("Choose users...", "ユーザーを選択...");
		m.put("Choose...", "選択...");
		m.put("Circular build spec imports ({0})", "循環ビルド仕様インポート ({0})");
		m.put("Click to select a commit, or shift-click to select multiple commit", "クリックしてコミットを選択、またはシフトクリックして複数のコミットを選択");
		m.put("Click to show comment of marked text", "クリックしてマークされたテキストのコメントを表示");
		m.put("Click to show issue details", "クリックして課題の詳細を表示");
		m.put("Client ID of this OneDev instance registered in Google cloud", "Googleクラウドに登録されたこのOneDevインスタンスのクライアントID");
		m.put("Client Id", "クライアントID");
		m.put("Client Secret", "クライアントシークレット");
		m.put("Client secret of this OneDev instance registered in Google cloud", "Googleクラウドに登録されたこのOneDevインスタンスのクライアントシークレット");
		m.put("Clippy Report", "Clippyレポート");
		m.put("Clone", "クローン");
		m.put("Clone Credential", "クローン資格情報");
		m.put("Clone Depth", "クローン深度");
		m.put("Clone in IntelliJ", "IntelliJでクローン");
		m.put("Clone in VSCode", "VSCodeでクローン");
		m.put("Close", "閉じる");
		m.put("Close Iteration", "イテレーションを閉じる");
		m.put("Close this iteration", "このイテレーションを閉じる");
		m.put("Closed", "閉じられた");
		m.put("Closed Issue State", "閉じられた課題の状態");
		m.put("Closest due date", "最も近い期限");
		m.put("Clover Coverage Report", "Cloverカバレッジレポート");
		m.put("Cluster Role", "クラスター役割");
		m.put("Cluster Setting", "クラスター設定");
		m.put("Cluster setting", "クラスター設定");
		m.put("Clustered Servers", "クラスタ化されたサーバー");
		m.put("Cobertura Coverage Report", "Coberturaカバレッジレポート");
		m.put("Code", "コード");
		m.put("Code Analysis", "コード分析");
		m.put("Code Analysis Setting", "コード分析設定");
		m.put("Code Analysis Settings", "コード分析設定");
		m.put("Code Changes", "コード変更");
		m.put("Code Comment", "コードコメント");
		m.put("Code Comment Management", "コードコメント管理");
		m.put("Code Comments", "コードコメント");
		m.put("Code Compare", "コード比較");
		m.put("Code Contribution Statistics", "コード貢献統計");
		m.put("Code Coverage", "コードカバレッジ");
		m.put("Code Line Statistics", "コード行統計");
		m.put("Code Management", "コード管理");
		m.put("Code Privilege", "コード権限");
		m.put("Code Problem Statistics", "コード問題統計");
		m.put("Code Search", "コード検索");
		m.put("Code Statistics", "コード統計");
		m.put("Code analysis settings updated", "コード分析設定が更新されました");
		m.put("Code changes since...", "以降のコード変更...");
		m.put("Code clone or download", "コードのクローンまたはダウンロード");
		m.put("Code comment", "コードコメント");
		m.put("Code comment #{0} deleted", "コードコメント #{0} が削除されました");
		m.put("Code comment administrative permission inside a project, including batch operations over multiple code comments", 
			"プロジェクト内のコードコメント管理権限、複数のコードコメントに対するバッチ操作を含む");
		m.put("Code commit", "コードコミット");
		m.put("Code is committed", "コードがコミットされました");
		m.put("Code push", "コードプッシュ");
		m.put("Code read permission is required to import build spec (import project: {0}, import revision: {1})", 
			"ビルド仕様をインポートするにはコード読み取り権限が必要です (インポートプロジェクト: {0}, インポートリビジョン: {1})");
		m.put("Code suggestion", "コード提案");
		m.put("Code write permission is required for this operation", "この操作にはコード書き込み権限が必要です");
		m.put("Collapse all", "すべて折りたたむ");
		m.put("Color", "色");
		m.put("Columns", "列");
		m.put("Command Palette", "コマンドパレット");
		m.put("Commands", "コマンド");
		m.put("Comment", "コメント");
		m.put("Comment Content", "コメント内容");
		m.put("Comment on File", "ファイルへのコメント");
		m.put("Comment too long", "コメントが長すぎます");
		m.put("Commented code is outdated", "コメントされたコードは古いです");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "プロジェクト「{1}」のファイル「{0}」にコメントしました");
		m.put("Commented on issue \"{0}\" ({1})", "課題「{0}」({1})にコメントしました");
		m.put("Commented on pull request \"{0}\" ({1})", "プルリクエスト「{0}」({1})にコメントしました");
		m.put("Comments", "コメント");
		m.put("Commit", "コミット");
		m.put("Commit &amp; Insert", "コミット &amp; 挿入");
		m.put("Commit Batched Suggestions", "バッチ提案をコミット");
		m.put("Commit Message", "コミットメッセージ");
		m.put("Commit Message Bean", "コミットメッセージBean");
		m.put("Commit Message Fix Patterns", "コミットメッセージ修正パターン");
		m.put("Commit Message Footer Pattern", "コミットメッセージフッターパターン");
		m.put("Commit Notification", "コミット通知");
		m.put("Commit Notification Template", "コミット通知テンプレート");
		m.put("Commit Scopes", "コミットスコープ");
		m.put("Commit Signature Required", "コミット署名が必要です");
		m.put("Commit Suggestion", "コミット提案");
		m.put("Commit Types", "コミットタイプ");
		m.put("Commit Types For Footer Check", "フッターチェック用コミットタイプ");
		m.put("Commit Your Change", "変更をコミット");
		m.put("Commit date", "コミット日付");
		m.put("Commit hash", "コミットハッシュ");
		m.put("Commit history of current path", "現在のパスのコミット履歴");
		m.put("Commit index version", "コミットインデックスバージョン");
		m.put("Commit message can be used to fix issues by prefixing and suffixing issue number with specified pattern. Each line of the commit message will be matched against each entry defined here to find issues to be fixed", 
			"コミットメッセージは、指定されたパターンで課題番号を接頭辞および接尾辞として使用することで課題を修正できます。コミットメッセージの各行は、ここで定義された各エントリと照合され、修正すべき課題を見つけます");
		m.put("Commit not exist or access denied", "コミットが存在しないか、アクセスが拒否されました");
		m.put("Commit of the build is missing", "ビルドのコミットが欠落しています");
		m.put("Commit signature required but no GPG signing key specified", "コミット署名が必要ですが、GPG署名キーが指定されていません");
		m.put("Commit suggestion", "コミット提案");
		m.put("Commits", "コミット");
		m.put("Commits are taken from default branch of non-forked repositories", "コミットは非フォークリポジトリのデフォルトブランチから取得されます");
		m.put("Commits generated by OneDev previously will be shown as unverified if this key is deleted. Type <code>yes</code> below if you want to continue.", 
			"以前にOneDevによって生成されたコミットは、このキーが削除されると未確認として表示されます。続行する場合は<code>yes</code>と入力してください。");
		m.put("Commits were merged into target branch", "コミットはターゲットブランチにマージされました");
		m.put("Commits were merged into target branch outside of this pull request", "コミットはこのプルリクエスト外でターゲットブランチにマージされました");
		m.put("Commits were rebased onto target branch", "コミットはターゲットブランチにリベースされました");
		m.put("Commits were squashed into a single commit on target branch", "コミットはターゲットブランチで単一のコミットにスクワッシュされました");
		m.put("Committed After", "以降にコミット");
		m.put("Committed Before", "以前にコミット");
		m.put("Committed By", "コミットした人");
		m.put("Committer", "コミッター");
		m.put("Compare", "比較");
		m.put("Compare with base revision", "ベースリビジョンと比較");
		m.put("Compare with this parent", "この親と比較");
		m.put("Concurrency", "並行性");
		m.put("Condition", "条件");
		m.put("Confidential", "機密");
		m.put("Config File", "設定ファイル");
		m.put("Configuration Discovery Url", "構成検出URL");
		m.put("Configure your scope to use below registry", "以下のレジストリを使用するようスコープを設定してください");
		m.put("Confirm Approve", "承認を確認");
		m.put("Confirm Delete Source Branch", "ソースブランチの削除を確認");
		m.put("Confirm Discard", "破棄を確認");
		m.put("Confirm Reopen", "再オープンを確認");
		m.put("Confirm Request For Changes", "変更要求を確認");
		m.put("Confirm Restore Source Branch", "ソースブランチの復元を確認");
		m.put("Confirm password here", "ここでパスワードを確認");
		m.put("Confirm your action", "アクションを確認");
		m.put("Connect New Agent", "新しいエージェントを接続");
		m.put("Connect with your SSO account", "SSOアカウントで接続");
		m.put("Contact Email", "連絡先メール");
		m.put("Contact Name", "連絡先名");
		m.put("Container Image", "コンテナイメージ");
		m.put("Container Image(s)", "コンテナイメージ(s)");
		m.put("Container default", "コンテナデフォルト");
		m.put("Content", "コンテンツ");
		m.put("Content Type", "コンテンツタイプ");
		m.put("Content is identical", "コンテンツは同一です");
		m.put("Continue to add other user after create", "作成後に他のユーザーを追加し続ける");
		m.put("Contributed settings", "寄与された設定");
		m.put("Contributions", "寄与");
		m.put("Contributions to {0} branch, excluding merge commits", "マージコミットを除く{0}ブランチへの寄与");
		m.put("Convert All Queried to Service Accounts", "すべてのクエリをサービスアカウントに変換");
		m.put("Convert Selected to Service Accounts", "選択したものをサービスアカウントに変換");
		m.put("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm", 
			"サービスアカウントに変換すると、パスワード、メールアドレス、すべての割り当てとウォッチが削除されます。確認するには<code>yes</code>と入力してください");
		m.put("Copy", "コピー");
		m.put("Copy All Queried Issues To...", "クエリされたすべての課題を...にコピー");
		m.put("Copy Files with SCP", "SCPでファイルをコピー");
		m.put("Copy Selected Issues To...", "選択された課題を...にコピー");
		m.put("Copy dashboard", "ダッシュボードをコピー");
		m.put("Copy issue number and title", "課題番号とタイトルをコピー");
		m.put("Copy public key", "公開鍵をコピー");
		m.put("Copy selected text to clipboard", "選択されたテキストをクリップボードにコピー");
		m.put("Copy to clipboard", "クリップボードにコピー");
		m.put("Count", "カウント");
		m.put("Coverage Statistics", "カバレッジ統計");
		m.put("Covered", "カバー済み");
		m.put("Covered by tests", "テストでカバー済み");
		m.put("Cppcheck Report", "Cppcheckレポート");
		m.put("Cpu Limit", "CPU制限");
		m.put("Cpu Request", "CPUリクエスト");
		m.put("Create", "作成");
		m.put("Create Administrator Account", "管理者アカウントを作成");
		m.put("Create Branch", "ブランチを作成");
		m.put("Create Branch Bean", "ブランチBeanを作成");
		m.put("Create Branch Bean With Revision", "リビジョン付きブランチBeanを作成");
		m.put("Create Child Project", "子プロジェクトを作成");
		m.put("Create Child Projects", "子プロジェクトを作成");
		m.put("Create Issue", "課題を作成");
		m.put("Create Iteration", "イテレーションを作成");
		m.put("Create Merge Commit", "マージコミットを作成");
		m.put("Create Merge Commit If Necessary", "必要に応じてマージコミットを作成");
		m.put("Create New", "新規作成");
		m.put("Create New File", "新しいファイルを作成");
		m.put("Create New User", "新しいユーザーを作成");
		m.put("Create Project", "プロジェクトを作成");
		m.put("Create Pull Request", "プルリクエストを作成");
		m.put("Create Pull Request for This Change", "この変更のプルリクエストを作成");
		m.put("Create Tag", "タグを作成");
		m.put("Create Tag Bean", "タグBeanを作成");
		m.put("Create Tag Bean With Revision", "リビジョン付きタグBeanを作成");
		m.put("Create User", "ユーザーを作成");
		m.put("Create body", "本文を作成");
		m.put("Create branch <b>{0}</b> from {1}", "{1}から<b>{0}</b>ブランチを作成");
		m.put("Create child projects under a project", "プロジェクト内に子プロジェクトを作成");
		m.put("Create issue", "課題を作成");
		m.put("Create merge commit", "マージコミットを作成");
		m.put("Create merge commit if necessary", "必要に応じてマージコミットを作成");
		m.put("Create new issue", "新しい課題を作成");
		m.put("Create tag", "タグを作成");
		m.put("Create tag <b>{0}</b> from {1}", "{1}から<b>{0}</b>タグを作成");
		m.put("Created At", "作成日時");
		m.put("Creation of this branch is prohibited per branch protection rule", "ブランチ保護ルールによりこのブランチの作成は禁止されています");
		m.put("Critical", "重大");
		m.put("Critical Severity", "重大な深刻度");
		m.put("Cron Expression", "Cron式");
		m.put("Cron schedule", "Cronスケジュール");
		m.put("Curl Location", "Curlの場所");
		m.put("Current Iteration", "現在のイテレーション");
		m.put("Current Value", "現在の値");
		m.put("Current avatar", "現在のアバター");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"このコメントが追加された時のコンテキストと現在のコンテキストが異なります。コメントのコンテキストを表示するにはクリックしてください");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"この返信が追加された時のコンテキストと現在のコンテキストが異なります。返信のコンテキストを表示するにはクリックしてください");
		m.put("Current context is different from this action, click to show the comment context", "このアクションと現在のコンテキストが異なります。コメントのコンテキストを表示するにはクリックしてください");
		m.put("Current platform", "現在のプラットフォーム");
		m.put("Current project", "現在のプロジェクト");
		m.put("Custom Linux Shell", "カスタムLinuxシェル");
		m.put("DISCARDED", "破棄済み");
		m.put("Dashboard Share Bean", "ダッシュボード共有Bean");
		m.put("Dashboard name", "ダッシュボード名");
		m.put("Dashboards", "ダッシュボード");
		m.put("Database Backup", "データベースバックアップ");
		m.put("Date", "日付");
		m.put("Date Time", "日時");
		m.put("Days Per Week", "週あたりの日数");
		m.put("Deactivate Subscription", "サブスクリプションを無効化");
		m.put("Deactivate Trial Subscription", "トライアルサブスクリプションを無効化");
		m.put("Default", "デフォルト");
		m.put("Default (Shell on Linux, Batch on Windows)", "デフォルト（Linuxではシェル、Windowsではバッチ）");
		m.put("Default Assignees", "デフォルトの担当者");
		m.put("Default Boards", "デフォルトのボード");
		m.put("Default Fixed Issue Filter", "デフォルトの解決済み課題フィルター");
		m.put("Default Fixed Issue Filters", "デフォルトの解決済み課題フィルター");
		m.put("Default Fixed Issue Filters Bean", "デフォルトの解決済み課題フィルターBean");
		m.put("Default Group", "デフォルトグループ");
		m.put("Default Issue Boards", "デフォルトの課題ボード");
		m.put("Default Merge Strategy", "デフォルトのマージ戦略");
		m.put("Default Multi Value Provider", "デフォルトの複数値プロバイダー");
		m.put("Default Project", "デフォルトプロジェクト");
		m.put("Default Project Setting", "デフォルトプロジェクト設定");
		m.put("Default Roles", "デフォルトの役割");
		m.put("Default Roles Bean", "デフォルトの役割Bean");
		m.put("Default Value", "デフォルト値");
		m.put("Default Value Provider", "デフォルト値プロバイダー");
		m.put("Default Values", "デフォルト値");
		m.put("Default branch", "デフォルトブランチ");
		m.put("Default branding settings restored", "デフォルトのブランディング設定が復元されました");
		m.put("Default fixed issue filters saved", "デフォルトの解決済み課題フィルターが保存されました");
		m.put("Default merge strategy", "デフォルトのマージ戦略");
		m.put("Default roles affect default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"デフォルトの役割は、システム内の全員に付与されるデフォルトの権限に影響を与えます。実際のデフォルトの権限は、このプロジェクトとそのすべての親プロジェクトのデフォルトの役割に含まれる<b class='text-warning'>すべての権限</b>になります");
		m.put("Define all custom issue fields here. Each project can decide to use all or a subset of these fields via its issue transition setting. <b class=\"text-warning\">NOTE: </b> Newly defined fields by default only appear in new issues. Batch edit existing issues from issue list page if you want them to have these new fields", 
			"ここでカスタム課題フィールドをすべて定義します。各プロジェクトは課題遷移設定を通じてこれらのフィールドをすべて使用するか、またはその一部を使用するかを決定できます。<b class=\"text-warning\">注意: </b>新しく定義されたフィールドはデフォルトで新しい課題にのみ表示されます。課題リストページから既存の課題を一括編集してこれらの新しいフィールドを持たせることができます");
		m.put("Define all custom issue states here. The first state will be used as initial state of created issues", 
			"ここでカスタム課題状態をすべて定義します。最初の状態は作成された課題の初期状態として使用されます");
		m.put("Define branch protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given branch and user, the first matching rule will take effect", 
			"ブランチ保護ルールを定義します。親プロジェクトで定義されたルールはここで定義されたルールの後に考慮されます。特定のブランチとユーザーに対して最初に一致するルールが適用されます");
		m.put("Define default issue boards for all projects here. A certain project can override this setting to define its own issue boards.", 
			"すべてのプロジェクトに対するデフォルトの課題ボードをここで定義します。特定のプロジェクトはこの設定を上書きして独自の課題ボードを定義できます");
		m.put("Define how issue states should be transited from one to another, either manually or automatically when some events happen. And the rule can be configured to apply to certain projects and issues via the applicable issues setting", 
			"課題状態がどのように遷移するべきかを定義します。手動で遷移するか、またはイベントが発生した際に自動的に遷移するかを選択できます。そしてルールは特定のプロジェクトや課題に適用されるように設定できます");
		m.put("Define issue templates here. When a new issue is created, the first matching template will be used.", 
			"課題テンプレートをここで定義します。新しい課題が作成されると、最初に一致するテンプレートが使用されます");
		m.put("Define labels to be assigned to project, build or pull request. For issues, custom fields can be used which is much more powerful than labels", 
			"プロジェクト、ビルド、またはプルリクエストに割り当てるラベルを定義します。課題にはラベルよりもはるかに強力なカスタムフィールドを使用できます");
		m.put("Define properties to be used in build spec. Properties will be inherited by child projects, and can be overridden by child properties with same name.", 
			"ビルド仕様で使用するプロパティを定義します。プロパティは子プロジェクトに継承され、同じ名前の子プロパティによって上書きされることがあります");
		m.put("Define rules to preserve builds. A build will be preserved as long as one rule defined here or in parent projects preserves it. All builds will be preserved if no rules are defined here and in parent projects", 
			"ビルドを保持するルールを定義します。ここまたは親プロジェクトで定義されたルールのいずれかがビルドを保持する限り、ビルドは保持されます。ここおよび親プロジェクトでルールが定義されていない場合、すべてのビルドが保持されます");
		m.put("Define tag protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given tag and user, the first matching rule will take effect", 
			"タグ保護ルールを定義します。親プロジェクトで定義されたルールはここで定義されたルールの後に考慮されます。特定のタグとユーザーに対して最初に一致するルールが適用されます");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"最初のリトライの遅延時間（秒）。その後のリトライの遅延時間はこの値に基づいて指数バックオフを使用して計算されます");
		m.put("Delete", "削除");
		m.put("Delete All", "すべて削除");
		m.put("Delete All Queried Builds", "クエリされたビルドをすべて削除");
		m.put("Delete All Queried Comments", "クエリされたコメントをすべて削除");
		m.put("Delete All Queried Issues", "クエリされた課題をすべて削除");
		m.put("Delete All Queried Packages", "クエリされたパッケージをすべて削除");
		m.put("Delete All Queried Projects", "クエリされたプロジェクトをすべて削除");
		m.put("Delete All Queried Pull Requests", "クエリされたプルリクエストをすべて削除");
		m.put("Delete All Queried Users", "クエリされたユーザーをすべて削除");
		m.put("Delete Build", "ビルドを削除");
		m.put("Delete Comment", "コメントを削除");
		m.put("Delete Pull Request", "プルリクエストを削除");
		m.put("Delete SSO account here to reconnect corresponding SSO subject upon next login. Note that SSO subject with verified email will be connected to user with same verified email automatically", 
			"次回ログイン時に対応するSSOサブジェクトを再接続するためにここでSSOアカウントを削除します。確認済みのメールを持つSSOサブジェクトは、同じ確認済みメールを持つユーザーに自動的に接続されます");
		m.put("Delete Selected", "選択したものを削除");
		m.put("Delete Selected Builds", "選択したビルドを削除");
		m.put("Delete Selected Comments", "選択したコメントを削除");
		m.put("Delete Selected Issues", "選択した課題を削除");
		m.put("Delete Selected Packages", "選択したパッケージを削除");
		m.put("Delete Selected Projects", "選択したプロジェクトを削除");
		m.put("Delete Selected Pull Requests", "選択したプルリクエストを削除");
		m.put("Delete Selected Users", "選択したユーザーを削除");
		m.put("Delete Source Branch", "ソースブランチを削除");
		m.put("Delete Source Branch After Merge", "マージ後にソースブランチを削除");
		m.put("Delete dashboard", "ダッシュボードを削除");
		m.put("Delete from branch {0}", "ブランチ {0} から削除");
		m.put("Delete this", "これを削除");
		m.put("Delete this GPG key", "このGPGキーを削除");
		m.put("Delete this access token", "このアクセストークンを削除");
		m.put("Delete this branch", "このブランチを削除");
		m.put("Delete this executor", "このエグゼキューターを削除");
		m.put("Delete this field", "このフィールドを削除");
		m.put("Delete this import", "このインポートを削除");
		m.put("Delete this iteration", "このイテレーションを削除");
		m.put("Delete this key", "このキーを削除");
		m.put("Delete this link", "このリンクを削除");
		m.put("Delete this rule", "このルールを削除");
		m.put("Delete this secret", "このシークレットを削除");
		m.put("Delete this state", "この状態を削除");
		m.put("Delete this tag", "このタグを削除");
		m.put("Delete this value", "この値を削除");
		m.put("Deleted source branch", "ソースブランチを削除済み");
		m.put("Deletion not allowed due to branch protection rule", "ブランチ保護ルールにより削除は許可されていません");
		m.put("Deletion not allowed due to tag protection rule", "タグ保護ルールにより削除は許可されていません");
		m.put("Deletions", "削除");
		m.put("Denied", "拒否");
		m.put("Dependencies & Services", "依存関係とサービス");
		m.put("Dependency Management", "依存関係管理");
		m.put("Dependency job finished", "依存ジョブが完了");
		m.put("Dependent Fields", "依存フィールド");
		m.put("Depends on <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>", "<a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>に依存");
		m.put("Descending", "降順");
		m.put("Description", "説明");
		m.put("Description Template", "説明テンプレート");
		m.put("Description Templates", "説明テンプレート一覧");
		m.put("Description too long", "説明が長すぎます");
		m.put("Destination Path", "宛先パス");
		m.put("Destinations", "宛先");
		m.put("Detect Licenses", "ライセンスを検出");
		m.put("Detect Secrets", "シークレットを検出");
		m.put("Detect Vulnerabilities", "脆弱性を検出");
		m.put("Diff is too large to be displayed.", "差分が大きすぎて表示できません。");
		m.put("Diff options", "差分オプション");
		m.put("Digest", "ダイジェスト");
		m.put("Digest invalid", "ダイジェストが無効");
		m.put("Directories to Skip", "スキップするディレクトリ");
		m.put("Directory", "ディレクトリ");
		m.put("Directory (tenant) ID", "ディレクトリ（テナント）ID");
		m.put("Disable", "無効化");
		m.put("Disable All Queried Users", "クエリされたすべてのユーザーを無効化");
		m.put("Disable Auto Update Check", "自動更新チェックを無効化");
		m.put("Disable Dashboard", "ダッシュボードを無効化");
		m.put("Disable Selected Users", "選択したユーザーを無効化");
		m.put("Disabled", "無効化済み");
		m.put("Disabled users and service accounts are excluded from user-month calculation", "無効なユーザーとサービスアカウントはユーザーマンス計算から除外されます");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"アカウントを無効化すると、パスワードがリセットされ、アクセストークンがクリアされ、過去の活動を除く他のエンティティからのすべての参照が削除されます。本当に続行しますか？");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"アカウントを無効化すると、パスワードがリセットされ、アクセストークンがクリアされ、過去の活動を除く他のエンティティからのすべての参照が削除されます。確認するには<code>yes</code>と入力してください");
		m.put("Disallowed File Types", "許可されていないファイルタイプ");
		m.put("Disallowed file type(s): {0}", "許可されていないファイルタイプ: {0}");
		m.put("Discard", "破棄");
		m.put("Discard All Queried Pull Requests", "クエリされたすべてのプルリクエストを破棄");
		m.put("Discard Selected Pull Requests", "選択したプルリクエストを破棄");
		m.put("Discarded", "破棄済み");
		m.put("Discarded pull request \"{0}\" ({1})", "プルリクエスト \"{0}\" ({1}) を破棄しました");
		m.put("Discord", "Discord");
		m.put("Discord Notifications", "Discord通知");
		m.put("Display Fields", "表示フィールド");
		m.put("Display Links", "表示リンク");
		m.put("Display Months", "表示月");
		m.put("Display Params", "表示パラメータ");
		m.put("Do Not Retrieve Groups", "グループを取得しない");
		m.put("Do not ignore", "無視しない");
		m.put("Do not ignore whitespace", "空白を無視しない");
		m.put("Do not retrieve", "取得しない");
		m.put("Do not retrieve groups", "グループを取得しない");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "\"{0}\"への招待を本当にキャンセルしますか？");
		m.put("Do you really want to cancel this build?", "このビルドを本当にキャンセルしますか？");
		m.put("Do you really want to change target branch to {0}?", "ターゲットブランチを{0}に変更してもよろしいですか？");
		m.put("Do you really want to delete \"{0}\"?", "\"{0}\"を本当に削除しますか？");
		m.put("Do you really want to delete SSO provider \"{0}\"?", "本当にSSOプロバイダー\"{0}\"を削除しますか？");
		m.put("Do you really want to delete board \"{0}\"?", "ボード\"{0}\"を本当に削除しますか？");
		m.put("Do you really want to delete build #{0}?", "ビルド#{0}を本当に削除しますか？");
		m.put("Do you really want to delete group \"{0}\"?", "グループ\"{0}\"を本当に削除しますか？");
		m.put("Do you really want to delete iteration \"{0}\"?", "イテレーション\"{0}\"を本当に削除しますか？");
		m.put("Do you really want to delete job secret \"{0}\"?", "ジョブシークレット\"{0}\"を本当に削除しますか？");
		m.put("Do you really want to delete pull request #{0}?", "プルリクエスト#{0}を本当に削除しますか？");
		m.put("Do you really want to delete role \"{0}\"?", "ロール\"{0}\"を本当に削除しますか？");
		m.put("Do you really want to delete selected query watches?", "選択したクエリウォッチを本当に削除しますか？");
		m.put("Do you really want to delete tag {0}?", "タグ{0}を本当に削除しますか？");
		m.put("Do you really want to delete this GPG key?", "このGPGキーを本当に削除しますか？");
		m.put("Do you really want to delete this SSH key?", "このSSHキーを本当に削除しますか？");
		m.put("Do you really want to delete this SSO account?", "本当にこのSSOアカウントを削除しますか？");
		m.put("Do you really want to delete this access token?", "このアクセストークンを本当に削除しますか？");
		m.put("Do you really want to delete this board?", "このボードを本当に削除しますか？");
		m.put("Do you really want to delete this build?", "このビルドを本当に削除しますか？");
		m.put("Do you really want to delete this code comment and all its replies?", "このコードコメントとそのすべての返信を本当に削除しますか？");
		m.put("Do you really want to delete this code comment?", "このコードコメントを本当に削除しますか？");
		m.put("Do you really want to delete this directory?", "このディレクトリを本当に削除しますか？");
		m.put("Do you really want to delete this email address?", "このメールアドレスを本当に削除しますか？");
		m.put("Do you really want to delete this executor?", "本当にこのエグゼキュータを削除しますか？");
		m.put("Do you really want to delete this field?", "本当にこのフィールドを削除しますか？");
		m.put("Do you really want to delete this file?", "本当にこのファイルを削除しますか？");
		m.put("Do you really want to delete this issue?", "本当にこの課題を削除しますか？");
		m.put("Do you really want to delete this link?", "本当にこのリンクを削除しますか？");
		m.put("Do you really want to delete this package?", "本当にこのパッケージを削除しますか？");
		m.put("Do you really want to delete this privilege?", "本当にこの権限を削除しますか？");
		m.put("Do you really want to delete this protection?", "本当にこの保護を削除しますか？");
		m.put("Do you really want to delete this pull request?", "本当にこのプルリクエストを削除しますか？");
		m.put("Do you really want to delete this reply?", "本当にこの返信を削除しますか？");
		m.put("Do you really want to delete this script?", "本当にこのスクリプトを削除しますか？");
		m.put("Do you really want to delete this state?", "本当にこの状態を削除しますか？");
		m.put("Do you really want to delete this template?", "本当にこのテンプレートを削除しますか？");
		m.put("Do you really want to delete this transition?", "本当にこの遷移を削除しますか？");
		m.put("Do you really want to delete timesheet \"{0}\"?", "本当にタイムシート「{0}」を削除しますか？");
		m.put("Do you really want to delete unused tokens?", "本当に未使用のトークンを削除しますか？");
		m.put("Do you really want to discard batched suggestions?", "本当にバッチ化された提案を破棄しますか？");
		m.put("Do you really want to enable this account?", "本当にこのアカウントを有効にしますか？");
		m.put("Do you really want to rebuild?", "本当に再構築しますか？");
		m.put("Do you really want to remove assignee \"{0}\"?", "本当に担当者「{0}」を削除しますか？");
		m.put("Do you really want to remove password of this user?", "本当にこのユーザーのパスワードを削除しますか？");
		m.put("Do you really want to remove the issue from iteration \"{0}\"?", "本当に課題をイテレーション「{0}」から削除しますか？");
		m.put("Do you really want to remove this account?", "本当にこのアカウントを削除しますか？");
		m.put("Do you really want to remove this agent?", "本当にこのエージェントを削除しますか？");
		m.put("Do you really want to remove this link?", "本当にこのリンクを削除しますか？");
		m.put("Do you really want to restart this agent?", "本当にこのエージェントを再起動しますか？");
		m.put("Do you really want to unauthorize user \"{0}\"?", "本当にユーザー「{0}」の認可を解除しますか？");
		m.put("Do you really want to use default template?", "本当にデフォルトテンプレートを使用しますか？");
		m.put("Docker", "Docker");
		m.put("Docker Executable", "Docker 実行ファイル");
		m.put("Docker Hub", "Docker Hub");
		m.put("Docker Image", "Docker イメージ");
		m.put("Docker Sock Path", "Docker Sock パス");
		m.put("Dockerfile", "Dockerfile");
		m.put("Documentation", "ドキュメント");
		m.put("Don't have an account yet?", "まだアカウントをお持ちでないですか？");
		m.put("Download", "ダウンロード");
		m.put("Download <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> or <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. A new agent token will be included in the package", 
			"<a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> または <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a> をダウンロードしてください。新しいエージェントトークンがパッケージに含まれます。");
		m.put("Download archive of this branch", "このブランチのアーカイブをダウンロード");
		m.put("Download full log", "完全なログをダウンロード");
		m.put("Download log", "ログをダウンロード");
		m.put("Download patch", "パッチをダウンロード");
		m.put("Download tag archive", "タグアーカイブをダウンロード");
		m.put("Dry Run", "ドライラン");
		m.put("Due Date", "期限日");
		m.put("Due Date Issue Field", "期限日課題フィールド");
		m.put("Due date", "期限日");
		m.put("Duplicate authorizations found: ", "重複する認可が見つかりました：");
		m.put("Duplicate authorizations found: {0}", "重複する認可が見つかりました：{0}");
		m.put("Duration", "期間");
		m.put("Durations", "期間");
		m.put("ESLint Report", "ESLint レポート");
		m.put("Edit", "編集");
		m.put("Edit <code>$HOME/.gem/credentials</code> to add a source", "ソースを追加するには <code>$HOME/.gem/credentials</code> を編集してください");
		m.put("Edit <code>$HOME/.pypirc</code> to add a package repository like below", "以下のようにパッケージリポジトリを追加するには <code>$HOME/.pypirc</code> を編集してください");
		m.put("Edit Avatar", "アバターを編集");
		m.put("Edit Estimated Time", "推定時間を編集");
		m.put("Edit Executor", "エグゼキュータを編集");
		m.put("Edit Iteration", "イテレーションを編集");
		m.put("Edit Job Secret", "ジョブシークレットを編集");
		m.put("Edit My Avatar", "自分のアバターを編集");
		m.put("Edit Rule", "ルールを編集");
		m.put("Edit Timesheet", "タイムシートを編集");
		m.put("Edit dashboard", "ダッシュボードを編集");
		m.put("Edit issue title", "課題タイトルを編集");
		m.put("Edit job", "ジョブを編集");
		m.put("Edit on branch {0}", "ブランチ {0} で編集");
		m.put("Edit on source branch", "ソースブランチで編集");
		m.put("Edit plain", "プレーンを編集");
		m.put("Edit saved queries", "保存されたクエリを編集");
		m.put("Edit this access token", "このアクセストークンを編集");
		m.put("Edit this executor", "このエグゼキュータを編集");
		m.put("Edit this iteration", "このイテレーションを編集");
		m.put("Edit this rule", "このルールを編集");
		m.put("Edit this secret", "このシークレットを編集");
		m.put("Edit this state", "この状態を編集");
		m.put("Edit title", "タイトルを編集");
		m.put("Edit with AI", "AIで編集");
		m.put("Edit {0}", "{0} を編集");
		m.put("Editable Issue Fields", "編集可能な課題フィールド");
		m.put("Editable Issue Links", "編集可能な課題リンク");
		m.put("Edited by {0} {1}", "{0} {1} によって編集されました");
		m.put("Editor", "エディター");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "ターゲットブランチまたはソースブランチに新しいコミットが今追加されました。再確認してください。");
		m.put("Email", "メール");
		m.put("Email Address", "メールアドレス");
		m.put("Email Address Verification", "メールアドレスの確認");
		m.put("Email Addresses", "メールアドレス");
		m.put("Email Templates", "メールテンプレート");
		m.put("Email Verification", "メール認証");
		m.put("Email Verification Template", "メール認証テンプレート");
		m.put("Email address", "メールアドレス");
		m.put("Email address \"{0}\" already used by another account", "メールアドレス\"{0}\"は他のアカウントで既に使用されています");
		m.put("Email address \"{0}\" used by account \"{1}\"", "メールアドレス\"{0}\"はアカウント\"{1}\"で使用されています");
		m.put("Email address \"{0}\" used by disabled account \"{1}\"", "メールアドレス\"{0}\"は無効なアカウント\"{1}\"で使用されています");
		m.put("Email address already in use: {0}", "既に使用されているメールアドレス：{0}");
		m.put("Email address already invited: {0}", "既に招待されているメールアドレス：{0}");
		m.put("Email address already used by another user", "別のユーザーによって既に使用されているメールアドレス");
		m.put("Email address already used: ", "メールアドレスは既に使用されています:");
		m.put("Email address to verify", "認証するメールアドレス");
		m.put("Email addresses with <span class=\"badge badge-warning badge-sm\">ineffective</span> mark are those not belong to or not verified by key owner", 
			"<span class=\"badge badge-warning badge-sm\">無効</span> のマークが付いたメールアドレスは、キー所有者に属していないか、認証されていないものです。");
		m.put("Email templates", "メールテンプレート");
		m.put("Empty file added.", "空のファイルが追加されました。");
		m.put("Empty file removed.", "空のファイルが削除されました。");
		m.put("Enable", "有効化");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets", 
			"このプロジェクトの進捗を追跡し、タイムシートを生成するために <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>タイムトラッキング</a> を有効にしてください。");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project", 
			"このプロジェクトの<a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>パッケージ管理</a>を有効にする");
		m.put("Enable Account Self Removal", "アカウントの自己削除を有効にする");
		m.put("Enable Account Self Sign-Up", "アカウントの自己登録を有効にする");
		m.put("Enable All Queried Users", "すべてのクエリされたユーザーを有効にする");
		m.put("Enable Anonymous Access", "匿名アクセスを有効にする");
		m.put("Enable Auto Backup", "自動バックアップを有効にする");
		m.put("Enable Html Report Publish", "HTMLレポート公開を有効化");
		m.put("Enable Selected Users", "選択されたユーザーを有効にする");
		m.put("Enable Site Publish", "サイト公開を有効化");
		m.put("Enable TTY Mode", "TTYモードを有効にする");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "<a wicket:id=\"addFile\" class=\"link-primary\"></a>によるビルドサポートを有効にする");
		m.put("Enable if visibility of this field depends on other fields", "このフィールドの表示が他のフィールドに依存する場合に有効にする");
		m.put("Enable if visibility of this param depends on other params", "このパラメータの表示が他のパラメータに依存する場合に有効にする");
		m.put("Enable this if the access token has same permissions as the owner", "アクセス トークンが所有者と同じ権限を持つ場合にこれを有効にする");
		m.put("Enable this option to merge the pull request automatically when ready (all reviewers approved, all required jobs passed etc.)", 
			"このオプションを有効にすると、準備が整ったときにプルリクエストを自動的にマージできます (すべてのレビューアが承認し、すべての必要なジョブが成功するなど)");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"HTMLレポート公開ステップを実行できるようにするにはこれを有効化してください。XSS攻撃を防ぐため、このエグゼキュータが信頼できるジョブでのみ使用されることを確認してください");
		m.put("Enable this to allow to run site publish step. OneDev will serve project site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"サイト公開ステップを実行できるようにするにはこれを有効にします。OneDevはプロジェクトサイトファイルをそのまま提供します。XSS攻撃を防ぐため、このエグゼキュータが信頼できるジョブでのみ使用されることを確認してください");
		m.put("Enable this to place intermediate files required by job execution on dynamically allocated persistent volume instead of emptyDir", 
			"emptyDirの代わりに動的に割り当てられた永続ボリュームにジョブ実行に必要な中間ファイルを配置するにはこれを有効にします");
		m.put("Enable this to process issue or pull request comments posted via email", "メール経由で投稿された問題やプルリクエストのコメントを処理するにはこれを有効にします");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"メール経由で投稿された問題やプルリクエストのコメントを処理するにはこれを有効にします。<b class='text-danger'>注意:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>サブアドレス</a>を上記のシステムメールアドレスに対して有効にする必要があります。OneDevはこれを使用して問題やプルリクエストのコンテキストを追跡します");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"メール経由で投稿された問題やプルリクエストのコメントを処理するにはこれを有効にします。<b class='text-danger'>注意:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>サブアドレス</a>を上記のシステムメールアドレスに対して有効にする必要があります。OneDevはこれを使用して問題やプルリクエストのコンテキストを追跡します");
		m.put("Enable to allow to upload build cache generated during CI/CD job. Uploaded cache can be used by subsequent builds of the project as long as cache key matches", 
			"CI/CDジョブ中に生成されたビルドキャッシュをアップロードできるようにするにはこれを有効にします。アップロードされたキャッシュは、キャッシュキーが一致する限りプロジェクトの後続のビルドで使用できます");
		m.put("End Point", "エンドポイント");
		m.put("Enforce Conventional Commits", "従来のコミットを強制する");
		m.put("Enforce Password Policy", "パスワードポリシーを強制");
		m.put("Enforce Two-factor Authentication", "二要素認証を強制する");
		m.put("Enforce password policy for new users", "新しいユーザーに対してパスワードポリシーを強制");
		m.put("Enter New Password", "新しいパスワードを入力");
		m.put("Enter description here", "ここに説明を入力してください");
		m.put("Enter your details to login to your account", "アカウントにログインするための詳細を入力してください");
		m.put("Enter your user name or email to reset password", "パスワードをリセットするには、ユーザー名またはメールを入力してください");
		m.put("Entries", "エントリ");
		m.put("Entry", "エントリ");
		m.put("Enumeration", "列挙");
		m.put("Env Var", "環境変数");
		m.put("Environment Variables", "環境変数");
		m.put("Environment variable <code>serverUrl</code> in above command is taken from OneDev server url specified in <i>Administration / System Setting</i>. Change it if necessary", 
			"上記のコマンド内の環境変数<code>serverUrl</code>は、<i>管理 / システム設定</i>で指定されたOneDevサーバーURLから取得されます。必要に応じて変更してください");
		m.put("Equal", "等しい");
		m.put("Error authenticating user", "ユーザー認証エラー");
		m.put("Error calculating commits: check log for details", "コミットの計算中にエラーが発生しました: 詳細はログを確認してください");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "{0}へのチェリーピック中にエラーが発生しました: マージコンフリクトが検出されました");
		m.put("Error cherry-picking to {0}: {1}", "{0}へのチェリーピック中にエラーが発生しました: {1}");
		m.put("Error detail of content type &quot;text/plain&quot;", "コンテンツタイプ&quot;text/plain&quot;のエラー詳細");
		m.put("Error discovering OIDC metadata", "OIDCメタデータの検出中にエラーが発生しました");
		m.put("Error executing task", "タスクの実行中にエラーが発生しました");
		m.put("Error parsing %sbase query: ", "%sbaseクエリの解析中にエラーが発生しました:");
		m.put("Error parsing %squery: ", "%squeryの解析中にエラーが発生しました:");
		m.put("Error parsing build spec", "ビルド仕様の解析中にエラーが発生しました");
		m.put("Error rendering widget, check server log for details", "ウィジェットのレンダリング中にエラーが発生しました。詳細はサーバーログを確認してください");
		m.put("Error reverting on {0}: Merge conflicts detected", "{0}でのリバート中にエラーが発生しました: マージコンフリクトが検出されました");
		m.put("Error reverting on {0}: {1}", "{0}でのリバート中にエラーが発生しました: {1}");
		m.put("Error validating auto merge commit message: {0}", "自動マージコミットメッセージの検証中にエラーが発生しました: {0}");
		m.put("Error validating build spec (location: {0}, error message: {1})", "ビルド仕様の検証中にエラーが発生しました (場所: {0}, エラーメッセージ: {1})");
		m.put("Error validating build spec: {0}", "ビルド仕様の検証中にエラーが発生しました: {0}");
		m.put("Error validating commit message of \"{0}\": {1}", "\"{0}\"のコミットメッセージの検証中にエラーが発生しました: {1}");
		m.put("Error validating commit message of <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}", 
			"<a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>のコミットメッセージの検証中にエラーが発生しました: {2}");
		m.put("Error verifying GPG signature", "GPG署名の検証エラー");
		m.put("Estimated Time", "推定時間");
		m.put("Estimated Time Edit Bean", "推定時間編集Bean");
		m.put("Estimated Time Issue Field", "推定時間問題フィールド");
		m.put("Estimated Time:", "推定時間:");
		m.put("Estimated time", "推定時間");
		m.put("Estimated/Spent time. Click for details", "推定/消費時間。詳細をクリックしてください");
		m.put("Evaluate script to get choices", "選択肢を取得するためにスクリプトを評価する");
		m.put("Evaluate script to get default value", "デフォルト値を取得するためにスクリプトを評価する");
		m.put("Evaluate script to get value or secret", "値または秘密を取得するためにスクリプトを評価する");
		m.put("Evaluate script to get values or secrets", "値または秘密を取得するためにスクリプトを評価する");
		m.put("Event Types", "イベントタイプ");
		m.put("Events", "イベント");
		m.put("Ever Used Since", "使用開始日");
		m.put("Everything inside this project and all child projects will be deleted and can not be recovered, please type project path <code>{0}</code> below to confirm deletion.", 
			"このプロジェクト内のすべての内容とすべての子プロジェクトが削除され、復元できなくなります。削除を確認するには、以下にプロジェクトパス<code>{0}</code>を入力してください。");
		m.put("Example", "例");
		m.put("Example Plugin Setting", "プラグイン設定の例");
		m.put("Example Property", "プロパティの例");
		m.put("Exclude Param Combos", "パラメータコンボを除外する");
		m.put("Exclude States", "状態を除外する");
		m.put("Excluded", "除外済み");
		m.put("Excluded Fields", "除外されたフィールド");
		m.put("Executable", "実行可能");
		m.put("Execute Commands", "コマンドを実行する");
		m.put("Execute Commands via SSH", "SSH経由でコマンドを実行する");
		m.put("Exit Impersonation", "なりすましを終了する");
		m.put("Exited impersonation", "なりすましを終了しました");
		m.put("Expand all", "すべて展開する");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", 
			"1つ以上の<tt>&lt;number&gt;(h|m)</tt>を期待します。例えば<tt>1h 1m</tt>は1時間と1分を表します");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", 
			"1つ以上の<tt>&lt;number&gt;(w|d|h|m)</tt>を期待します。例えば<tt>1w 1d 1h 1m</tt>は1週間({0}日)、1日({1}時間)、1時間、1分を表します");
		m.put("Expiration Date:", "有効期限:");
		m.put("Expire Date", "有効期限");
		m.put("Expired", "期限切れ");
		m.put("Explicit SSL (StartTLS)", "明示的SSL (StartTLS)");
		m.put("Export", "エクスポート");
		m.put("Export All Queried Issues To...", "クエリされたすべての問題をエクスポート...");
		m.put("Export CSV", "CSVをエクスポート");
		m.put("Export XLSX", "XLSXをエクスポート");
		m.put("Export as OCI layout", "OCIレイアウトとしてエクスポート");
		m.put("Extend Trial Subscription", "試用サブスクリプションを延長する");
		m.put("External Authentication", "外部認証");
		m.put("External Issue Transformers", "外部問題トランスフォーマー");
		m.put("External Participants", "外部参加者");
		m.put("External Password Authenticator", "外部パスワード認証");
		m.put("External System", "外部システム");
		m.put("External authenticator settings saved", "外部認証者設定が保存されました");
		m.put("External participants do not have accounts and involve in the issue via email", "外部参加者はアカウントを持たず、メールを介して問題に関与します");
		m.put("Extract the package into a folder. <b class=\"text-danger\">Warning:</b> On Mac OS X, do not extract to Mac managed folders such as Downloads, Desktop, Documents; otherwise you may encounter permission issues starting agent", 
			"パッケージをフォルダに抽出します。<b class=\"text-danger\">警告:</b> Mac OS Xでは、Downloads、Desktop、DocumentsなどのMac管理フォルダに抽出しないでください。そうしないと、エージェントの起動時に権限の問題が発生する可能性があります");
		m.put("FAILED", "失敗");
		m.put("Fail Threshold", "失敗閾値");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"指定された重大度レベル以上の脆弱性がある場合、ビルドを失敗させる");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"指定された重大度レベル以上の脆弱性がある場合、ビルドを失敗させる。これは、他のステップによってビルドが失敗していない場合にのみ有効です");
		m.put("Failed", "失敗しました");
		m.put("Failed to validate build spec import. Check server log for details", "ビルド仕様のインポートを検証できませんでした。詳細はサーバーログを確認してください");
		m.put("Failed to verify your email address", "メールアドレスの確認に失敗しました");
		m.put("Field Bean", "フィールドBean");
		m.put("Field Instance", "フィールドインスタンス");
		m.put("Field Name", "フィールド名");
		m.put("Field Spec", "フィールド仕様");
		m.put("Field Specs", "フィールド仕様一覧");
		m.put("Field Value", "フィールド値");
		m.put("Fields", "フィールド");
		m.put("Fields & Links", "フィールドとリンク");
		m.put("Fields And Links Bean", "フィールドとリンクのBean");
		m.put("Fields to Change", "変更するフィールド");
		m.put("File", "ファイル");
		m.put("File Changes", "ファイル変更");
		m.put("File Name", "ファイル名");
		m.put("File Name Patterns (separated by comma)", "ファイル名パターン（カンマ区切り）");
		m.put("File Path", "ファイルパス");
		m.put("File Patterns", "ファイルパターン");
		m.put("File Protection", "ファイル保護");
		m.put("File Protections", "ファイル保護一覧");
		m.put("File and Symbol Search", "ファイルとシンボル検索");
		m.put("File changes", "ファイル変更");
		m.put("File is too large to edit here", "ファイルが大きすぎてここでは編集できません");
		m.put("File missing or obsolete", "ファイルが欠落しているか廃止されています");
		m.put("File name", "ファイル名");
		m.put("File name patterns such as *.java, *.c", "ファイル名パターン（例: *.java, *.c）");
		m.put("Files", "ファイル一覧");
		m.put("Files to Be Analyzed", "分析対象のファイル");
		m.put("Filter", "フィルター");
		m.put("Filter Issues", "問題をフィルター");
		m.put("Filter actions", "アクションをフィルター");
		m.put("Filter backlog issues", "バックログの問題をフィルター");
		m.put("Filter branches...", "ブランチをフィルター...");
		m.put("Filter by name", "名前でフィルター");
		m.put("Filter by name or email address", "名前またはメールアドレスでフィルター");
		m.put("Filter by name...", "名前でフィルター...");
		m.put("Filter by path", "パスでフィルター");
		m.put("Filter by test suite", "テストスイートでフィルター");
		m.put("Filter date range", "日付範囲をフィルター");
		m.put("Filter files...", "ファイルをフィルター...");
		m.put("Filter groups...", "グループをフィルター...");
		m.put("Filter issues", "問題をフィルター");
		m.put("Filter pull requests", "プルリクエストをフィルター");
		m.put("Filter roles", "役割をフィルター");
		m.put("Filter tags...", "タグをフィルター...");
		m.put("Filter targets", "ターゲットをフィルター");
		m.put("Filter users", "ユーザーをフィルター");
		m.put("Filter...", "フィルター...");
		m.put("Filters", "フィルター一覧");
		m.put("Find branch", "ブランチを検索");
		m.put("Find or create branch", "ブランチを検索または作成");
		m.put("Find or create tag", "タグを検索または作成");
		m.put("Find tag", "タグを検索");
		m.put("Fingerprint", "フィンガープリント");
		m.put("Finish", "終了");
		m.put("First applicable executor", "最初に適用可能なエグゼキューター");
		m.put("Fix", "修正");
		m.put("Fix Type", "修正タイプ");
		m.put("Fix Undefined Field Values", "未定義フィールド値を修正");
		m.put("Fix Undefined Fields", "未定義フィールドを修正");
		m.put("Fix Undefined States", "未定義状態を修正");
		m.put("Fixed Issues", "修正済みの問題");
		m.put("Fixed issues since...", "修正済みの問題（...以降）");
		m.put("Fixing Builds", "ビルドの修正");
		m.put("Fixing Commits", "コミットの修正");
		m.put("Fixing...", "修正中...");
		m.put("Float", "浮動小数点");
		m.put("Follow below instructions to publish packages into this project", "以下の手順に従って、このプロジェクトにパッケージを公開してください");
		m.put("Follow below steps to install agent on remote machine (supports Linux/Windows/Mac OS X/FreeBSD):", 
			"以下の手順に従って、リモートマシンにエージェントをインストールしてください（Linux/Windows/Mac OS X/FreeBSD対応）:");
		m.put("For CI/CD job, add this gem to Gemfile like below", "CI/CDジョブの場合、以下のようにGemfileにこのgemを追加してください");
		m.put("For CI/CD job, add this package to requirements.txt and run below to install the package via command step", 
			"CI/CDジョブの場合、requirements.txtにこのパッケージを追加し、以下のコマンドステップでパッケージをインストールしてください");
		m.put("For CI/CD job, run below to add package repository via command step", "CI/CDジョブの場合、以下のコマンドステップでパッケージリポジトリを追加してください");
		m.put("For CI/CD job, run below to add package source via command step", "CI/CDジョブの場合、以下のコマンドステップでパッケージソースを追加してください");
		m.put("For CI/CD job, run below to add source via command step", "CI/CDジョブの場合、以下のコマンドステップでソースを追加してください");
		m.put("For CI/CD job, run below to install chart via command step", "CI/CDジョブの場合、以下のコマンドステップでチャートをインストールしてください");
		m.put("For CI/CD job, run below to publish package via command step", "CI/CDジョブの場合、以下のコマンドステップでパッケージを公開してください");
		m.put("For CI/CD job, run below to push chart to the repository via command step", "CI/CDジョブの場合、以下のコマンドステップでチャートをリポジトリにプッシュしてください");
		m.put("For CI/CD job, run below via a command step", "CI/CDジョブの場合、以下のコマンドステップを実行してください");
		m.put("For a particular project, the first matching entry will be used", "特定のプロジェクトの場合、最初に一致するエントリが使用されます");
		m.put("For all issues", "すべての問題に対して");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"デフォルトブランチから到達できないビルドコミットの場合、<a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a>を作成ブランチ権限付きのアクセストークンとして指定する必要があります");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"デフォルトブランチから到達できないビルドコミットの場合、<a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a>を作成タグ権限付きのアクセストークンとして指定する必要があります");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"デフォルトブランチから到達できないビルドコミットの場合、<a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a>を問題管理権限付きのアクセストークンとして指定する必要があります");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"Docker対応エグゼキューターの場合、このパスはコンテナ内にあり、絶対パスと相対パス（<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対する相対パス）を受け入れます。ホストマシンで直接実行されるシェル関連エグゼキューターの場合、相対パスのみが受け入れられます");
		m.put("For each build, OneDev calculates a list of fixed issues since previous build automatically. This setting provides a default query to further filter/order this list. For a given job, the first matching entry will be used.", 
			"各ビルドに対して、OneDevは前回のビルド以降に修正された問題のリストを自動的に計算します。この設定は、このリストをさらにフィルター/順序付けするためのデフォルトクエリを提供します。特定のジョブの場合、最初に一致するエントリが使用されます。");
		m.put("For each selected branch/tag, a separate build will be generated with branch/tag set to corresponding value", 
			"選択された各ブランチ/タグに対して、対応する値を設定した別々のビルドが生成されます");
		m.put("For issues matching: ", "一致する問題に対して:");
		m.put("For very large git repository, you may need to tune options here to reduce memory usage", 
			"非常に大きなGitリポジトリの場合、メモリ使用量を削減するためにここでオプションを調整する必要があるかもしれません");
		m.put("For web hooks defined here and in parent projects, OneDev will post event data in JSON format to specified URLs when subscribed events happen", 
			"ここおよび親プロジェクトで定義されたWebフックに対して、OneDevはJSON形式で指定されたURLにイベントデータを投稿します");
		m.put("Force", "強制");
		m.put("Force Garbage Collection", "ガベージコレクションを強制");
		m.put("Forgot Password?", "パスワードを忘れましたか？");
		m.put("Forgotten Password?", "パスワードを忘れましたか？");
		m.put("Fork Project", "フォークプロジェクト");
		m.put("Fork now", "今すぐフォーク");
		m.put("Forks Of", "フォーク元");
		m.put("Frequencies", "頻度");
		m.put("From Directory", "ディレクトリから");
		m.put("From States", "状態から");
		m.put("From extracted folder, run <code>bin\\agent.bat console</code> as administrator on Windows or <code>bin/agent.sh console</code> on other OS", 
			"抽出されたフォルダから、Windowsでは管理者として<code>bin\\agent.bat console</code>を、他のOSでは<code>bin/agent.sh console</code>を実行してください");
		m.put("From {0}", "{0}から");
		m.put("Full Name", "フルネーム");
		m.put("Furthest due date", "最も遠い期限日");
		m.put("GPG Keys", "GPGキー");
		m.put("GPG Public Key", "GPG公開鍵");
		m.put("GPG Signing Key", "GPG署名鍵");
		m.put("GPG Trusted Keys", "GPG信頼済み鍵");
		m.put("GPG key deleted", "GPGキーが削除されました");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "GPG公開鍵は「-----BEGIN PGP PUBLIC KEY BLOCK-----」で始まります");
		m.put("GPG signing key will be used to sign commits generated by OneDev, including pull request merge commits, user commits created via web UI or RESTful api.", 
			"GPG署名鍵は、OneDevによって生成されたコミット（プルリクエストのマージコミット、Web UIまたはRESTful APIを介して作成されたユーザーコミットを含む）を署名するために使用されます");
		m.put("Gem Info", "Gem情報");
		m.put("General", "一般");
		m.put("General Settings", "一般設定");
		m.put("General settings updated", "一般設定が更新されました");
		m.put("Generate", "生成");
		m.put("Generate File Checksum", "ファイルチェックサムを生成");
		m.put("Generate New", "新規生成");
		m.put("Generic LDAP", "汎用LDAP");
		m.put("Get", "取得");
		m.put("Get Groups Using Attribute", "属性を使用してグループを取得");
		m.put("Git", "Git");
		m.put("Git Command Line", "Gitコマンドライン");
		m.put("Git Credential", "Git資格情報");
		m.put("Git LFS Storage", "Git LFSストレージ");
		m.put("Git Lfs Lock", "Git LFSロック");
		m.put("Git Location", "Gitの場所");
		m.put("Git Pack Config", "Gitパック設定");
		m.put("Git Path", "Gitパス");
		m.put("Git email address will be used as git author/committer for commits created on web UI", 
			"Gitのメールアドレスは、Web UIで作成されたコミットのGit作成者/コミッターとして使用されます");
		m.put("Git pack config updated", "Gitパック設定が更新されました");
		m.put("GitHub", "GitHub");
		m.put("GitHub API URL", "GitHub API URL");
		m.put("GitHub Issue Label", "GitHub課題ラベル");
		m.put("GitHub Organization", "GitHub組織");
		m.put("GitHub Personal Access Token", "GitHub個人アクセストークン");
		m.put("GitHub Repositories to Import", "インポートするGitHubリポジトリ");
		m.put("GitHub Repository", "GitHubリポジトリ");
		m.put("GitHub personal access token should be generated with scope <b>repo</b> and <b>read:org</b>", 
			"GitHub個人アクセストークンは、<b>repo</b>および<b>read:org</b>のスコープで生成する必要があります");
		m.put("GitLab API URL", "GitLab API URL");
		m.put("GitLab Group", "GitLabグループ");
		m.put("GitLab Issue Label", "GitLab課題ラベル");
		m.put("GitLab Personal Access Token", "GitLab個人アクセストークン");
		m.put("GitLab Project", "GitLabプロジェクト");
		m.put("GitLab Projects to Import", "インポートするGitLabプロジェクト");
		m.put("GitLab personal access token should be generated with scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by user of specified access token will be listed", 
			"GitLab個人アクセストークンは、<b>read_api</b>、<b>read_user</b>、および<b>read_repository</b>のスコープで生成する必要があります。指定されたアクセストークンのユーザーが所有するグループ/プロジェクトのみがリストされます");
		m.put("Gitea API URL", "Gitea API URL");
		m.put("Gitea Issue Label", "Gitea課題ラベル");
		m.put("Gitea Organization", "Gitea組織");
		m.put("Gitea Personal Access Token", "Gitea個人アクセストークン");
		m.put("Gitea Repositories to Import", "インポートするGiteaリポジトリ");
		m.put("Gitea Repository", "Giteaリポジトリ");
		m.put("Github Access Token Secret", "GitHubアクセストークンシークレット");
		m.put("Global", "グローバル");
		m.put("Global Build Setting", "グローバルビルド設定");
		m.put("Global Issue Setting", "グローバル課題設定");
		m.put("Global Pack Setting", "グローバルパック設定");
		m.put("Global Views", "グローバルビュー");
		m.put("Gmail", "Gmail");
		m.put("Go Back", "戻る");
		m.put("Google Test Report", "Googleテストレポート");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Gpgキー");
		m.put("Great, your mail service configuration is working", "素晴らしい、メールサービスの設定が機能しています");
		m.put("Groovy Script", "Groovyスクリプト");
		m.put("Groovy Scripts", "Groovyスクリプト群");
		m.put("Groovy script to be evaluated. It should return a <i>Date</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。<i>Date</i>値を返す必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. It should return a <i>Float</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。<i>Float</i>値を返す必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. It should return a <i>Integer</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。<i>Integer</i>値を返す必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. It should return a <i>String</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。<i>String</i>値を返す必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. It should return a <i>boolean</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。<i>boolean</i>値を返す必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. It should return a <i>string</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。<i>string</i>値を返す必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. It should return name of a group. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。グループ名を返す必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. It should return string or list of string. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。文字列または文字列のリストを返す必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。返される値は選択肢として使用されるグループファサードオブジェクトのリストである必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. The return value should be a list of user login names to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。返される値は選択肢として使用されるユーザーログイン名のリストである必要があります。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy script to be evaluated. The return value should be a value to color map, for instance:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> if the value does not have a color. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"評価されるGroovyスクリプト。返される値は値と色のマップである必要があります。例えば:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>、値に色がない場合は<tt>null</tt>を使用してください。詳細は<a href='https://docs.onedev.io/appendix/scripting' target='_blank'>スクリプトヘルプ</a>を確認してください");
		m.put("Groovy scripts", "Groovyスクリプト群");
		m.put("Group", "グループ");
		m.put("Group \"{0}\" deleted", "グループ「{0}」が削除されました");
		m.put("Group Authorization Bean", "グループ認証Bean");
		m.put("Group Authorizations", "グループ認証");
		m.put("Group Authorizations Bean", "グループ認証Bean");
		m.put("Group By", "グループ化");
		m.put("Group Management", "グループ管理");
		m.put("Group Name Attribute", "グループ名属性");
		m.put("Group Retrieval", "グループ取得");
		m.put("Group Search Base", "グループ検索ベース");
		m.put("Group Search Filter", "グループ検索フィルター");
		m.put("Group authorizations updated", "グループ認証が更新されました");
		m.put("Group created", "グループが作成されました");
		m.put("Groups", "グループ群");
		m.put("Groups Claim", "グループクレーム");
		m.put("Guide Line", "ガイドライン");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "HTTP(S)クローンURL");
		m.put("Has Owner Permissions", "所有者権限を持つ");
		m.put("Has Running Builds", "実行中のビルドがあります");
		m.put("Heap Memory Usage", "ヒープメモリ使用量");
		m.put("Helm(s)", "Helm(s)");
		m.put("Help", "ヘルプ");
		m.put("Hide", "非表示");
		m.put("Hide Archived", "アーカイブを非表示");
		m.put("Hide comment", "コメントを非表示");
		m.put("Hide saved queries", "保存されたクエリを非表示");
		m.put("High", "高");
		m.put("High Availability & Scalability", "高可用性とスケーラビリティ");
		m.put("High Severity", "高い重大度");
		m.put("History", "履歴");
		m.put("History of comparing revisions is unrelated", "リビジョン比較の履歴は無関係です");
		m.put("History of target branch and source branch is unrelated", "ターゲットブランチとソースブランチの履歴は無関係です");
		m.put("Host name or ip address of remote machine to run commands via SSH", "SSHでコマンドを実行するリモートマシンのホスト名またはIPアドレス");
		m.put("Hours Per Day", "1日あたりの時間");
		m.put("How to Publish", "公開方法");
		m.put("Html Report", "HTMLレポート");
		m.put("Http Method", "HTTPメソッド");
		m.put("I didn't eat it. I swear!", "私は食べていません。誓います！");
		m.put("ID token was expired", "IDトークンが期限切れです");
		m.put("IMAP Host", "IMAPホスト");
		m.put("IMAP Password", "IMAPパスワード");
		m.put("IMAP User", "IMAPユーザー");
		m.put("IMPORTANT:", "重要:");
		m.put("IP Address", "IPアドレス");
		m.put("Id", "ID");
		m.put("Identify Field", "フィールドを識別");
		m.put("If enabled, scheduled backup will run on lead server which is <span wicket:id=\"leadServer\"></span> currently", 
			"有効化されている場合、スケジュールされたバックアップは現在<span wicket:id=\"leadServer\"></span>のリードサーバーで実行されます");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"有効化されている場合、プルリクエストをマージした後、ユーザーが権限を持っている場合、ソースブランチは自動的に削除されます");
		m.put("If specified, OneDev will only display iterations with this prefix", "指定されている場合、OneDevはこのプレフィックスを持つイテレーションのみを表示します");
		m.put("If specified, all public and internal projects imported from GitLab will use these as default roles. Private projects are not affected", 
			"指定された場合、GitLabからインポートされたすべての公開および内部プロジェクトはこれをデフォルトの役割として使用します。プライベートプロジェクトには影響しません");
		m.put("If specified, all public repositories imported from GitHub will use these as default roles. Private repositories are not affected", 
			"指定された場合、GitHubからインポートされたすべての公開リポジトリはこれをデフォルトの役割として使用します。プライベートリポジトリには影響しません");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"指定されている場合、課題の総推定/消費時間にはこのタイプのリンクされた課題も含まれます");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"このオプションが有効化されている場合、git lfsコマンドはOneDevサーバーにインストールされている必要があります（このステップが他のノードで実行される場合でも）");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"チェックされている場合、このフィールドで示されるグループは、時間追跡が有効化されている場合、対応する課題の推定時間を編集できます");
		m.put("Ignore", "無視");
		m.put("Ignore File", "ファイルを無視");
		m.put("Ignore activities irrelevant to me", "自分に関係のないアクティビティを無視");
		m.put("Ignore all", "すべてを無視");
		m.put("Ignore all whitespace", "すべての空白を無視");
		m.put("Ignore change", "変更を無視");
		m.put("Ignore change whitespace", "変更の空白を無視");
		m.put("Ignore leading", "先頭を無視");
		m.put("Ignore leading whitespace", "先頭の空白を無視");
		m.put("Ignore this field", "このフィールドを無視");
		m.put("Ignore this param", "このパラメータを無視");
		m.put("Ignore trailing", "末尾を無視");
		m.put("Ignore trailing whitespace", "末尾の空白を無視");
		m.put("Ignored Licenses", "無視されたライセンス");
		m.put("Image", "画像");
		m.put("Image Labels", "画像ラベル");
		m.put("Image Manifest", "画像マニフェスト");
		m.put("Image Size", "画像サイズ");
		m.put("Image Text", "画像テキスト");
		m.put("Image URL", "画像URL");
		m.put("Image URL should be specified", "画像URLを指定する必要があります");
		m.put("Imap Ssl Setting", "IMAP SSL設定");
		m.put("Imap With Ssl", "IMAP SSL使用");
		m.put("Impersonate", "偽装");
		m.put("Implicit SSL", "暗黙的SSL");
		m.put("Import", "インポート");
		m.put("Import All Projects", "すべてのプロジェクトをインポート");
		m.put("Import All Repositories", "すべてのリポジトリをインポート");
		m.put("Import Group", "グループをインポート");
		m.put("Import Issues", "課題をインポート");
		m.put("Import Option", "インポートオプション");
		m.put("Import Organization", "組織をインポート");
		m.put("Import Project", "プロジェクトをインポート");
		m.put("Import Projects", "プロジェクトをインポート");
		m.put("Import Repositories", "リポジトリをインポート");
		m.put("Import Repository", "リポジトリをインポート");
		m.put("Import Server", "サーバーをインポート");
		m.put("Import Workspace", "ワークスペースをインポート");
		m.put("Import build spec elements (jobs, services, step templates and properties) from other projects. Imported elements are treated as if they are defined locally. Locally defined elements will override imported elements with same name", 
			"他のプロジェクトからビルド仕様要素（ジョブ、サービス、ステップテンプレート、プロパティ）をインポートします。インポートされた要素はローカルで定義されたものとして扱われます。同じ名前の要素がある場合、ローカルで定義された要素がインポートされた要素を上書きします");
		m.put("Importing Issues from {0}", "{0}から課題をインポート中");
		m.put("Importing from {0}", "{0}からインポート中");
		m.put("Importing issues into currrent project. Please note that issue numbers will only be retained if the whole project fork graph does not have any issues to avoid duplicate issue numbers", 
			"現在のプロジェクトに課題をインポート中です。課題番号が重複しないようにするため、プロジェクトフォークグラフ全体に課題がない場合のみ課題番号が保持されます");
		m.put("Importing projects from {0}", "{0}からプロジェクトをインポート中");
		m.put("Imports", "インポート");
		m.put("In Projects", "プロジェクト内");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"IMAPホスト証明書が自己署名されている場合やCAルートが受け入れられていない場合、OneDevに証明書チェックをバイパスするよう指示できます。<b class='text-danger'>警告: </b>信頼できないネットワークでは、中間者攻撃につながる可能性があるため、代わりに<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>証明書をOneDevにインポート</a>する必要があります");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"SMTPホスト証明書が自己署名されている場合やCAルートが受け入れられていない場合、OneDevに証明書チェックをバイパスするよう指示できます。<b class='text-danger'>警告: </b>信頼できないネットワークでは、中間者攻撃につながる可能性があるため、代わりに<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>証明書をOneDevにインポート</a>する必要があります");
		m.put("In case anonymous access is disabled or anonymous user does not have enough permission for a resource operation, you will need to authenticate by providing user name and password (or access token) via http basic auth header", 
			"匿名アクセスが無効になっているか、匿名ユーザーがリソース操作のための十分な権限を持っていない場合、ユーザー名とパスワード（またはアクセストークン）をHTTP基本認証ヘッダーを介して提供して認証する必要があります");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"上記のキーでキャッシュがヒットしない場合、OneDevはここで定義されたロードキーを順番にループして、プロジェクト階層内で一致するキャッシュを見つけます。キャッシュはそのキーがロードキーで始まる場合、一致していると見なされます。複数のキャッシュが一致する場合、最も最近のキャッシュが返されます");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"キャッシュをアップロードする必要がある場合、このプロパティはアップロードのターゲットプロジェクトを指定します。現在のプロジェクトの場合は空白のままにしてください");
		m.put("In case the pull request status is out of sync with underlying repository, you may synchronize them manually here", 
			"プルリクエストのステータスが基盤となるリポジトリと同期していない場合、ここで手動で同期できます");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"ユーザーグループのメンバーシップがグループ側で維持されている場合、このプロパティはグループ検索のベースノードを指定します。例: <i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"ユーザーグループの関係がグループ側で維持されている場合、このフィルターは現在のユーザーの所属グループを決定するために使用されます。例: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>。この例では、<i>{0}</i>は現在のユーザーのDNを表します");
		m.put("In case you are using external issue tracker, you can define transformers to transform external issue references into external issue links in various places, such as commit messages and pull request descriptions", 
			"外部課題トラッカーを使用している場合、外部課題参照をコミットメッセージやプルリクエストの説明などのさまざまな場所で外部課題リンクに変換するトランスフォーマーを定義できます");
		m.put("In rare cases, your issues might be out of sync with workflow settings (undefined state/field etc.). Run integrity check below to find problems and get them fixed.", 
			"まれに、課題がワークフロー設定（未定義の状態/フィールドなど）と同期していない場合があります。以下の整合性チェックを実行して問題を見つけ、修正してください。");
		m.put("Inbox Poll Setting", "受信トレイポーリング設定");
		m.put("Include Child Projects", "子プロジェクトを含む");
		m.put("Include Disabled", "無効化されたものを含む");
		m.put("Include Forks", "フォークを含む");
		m.put("Include When Issue is Opened", "課題が開かれたときに含む");
		m.put("Incompatibilities", "非互換性");
		m.put("Inconsistent issuer in provider metadata and ID token", "プロバイダーメタデータとIDトークンの発行者が一致しない");
		m.put("Indicator", "インジケーター");
		m.put("Inherit from parent", "親から継承");
		m.put("Inherited", "継承済み");
		m.put("Input Spec", "入力仕様");
		m.put("Input URL", "入力URL");
		m.put("Input allowed CORS origin, hit ENTER to add", "許可されたCORSオリジンを入力し、ENTERを押して追加");
		m.put("Input revision", "リビジョンを入力");
		m.put("Input title", "タイトルを入力");
		m.put("Input title here", "ここにタイトルを入力");
		m.put("Input user search base. Hit ENTER to add", "ユーザー検索ベースを入力。ENTERを押して追加");
		m.put("Input user search bases. Hit ENTER to add", "ユーザー検索ベースを入力。ENTERを押して追加");
		m.put("Insert", "挿入");
		m.put("Insert Image", "画像を挿入");
		m.put("Insert Link", "リンクを挿入");
		m.put("Insert link to this file", "このファイルへのリンクを挿入");
		m.put("Insert this image", "この画像を挿入");
		m.put("Install Subscription Key", "サブスクリプションキーをインストール");
		m.put("Integer", "整数");
		m.put("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"実行中のジョブへのインタラクティブなウェブシェルアクセスはエンタープライズ機能です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料でお試しください</a>");
		m.put("Internal Database", "内部データベース");
		m.put("Interpreter", "インタープリター");
		m.put("Invalid GPG signature", "無効なGPG署名");
		m.put("Invalid PCRE syntax", "無効なPCRE構文");
		m.put("Invalid access token: {0}", "無効なアクセス トークン: {0}");
		m.put("Invalid credentials", "無効な資格情報");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "無効な日付範囲、\"yyyy-MM-dd to yyyy-MM-dd\"を期待しています");
		m.put("Invalid email address: {0}", "無効なメールアドレス: {0}");
		m.put("Invalid invitation code", "無効な招待コード");
		m.put("Invalid issue date of ID token", "IDトークンの発行日が無効です");
		m.put("Invalid issue number: {0}", "無効な発行番号: {0}");
		m.put("Invalid pull request number: {0}", "無効なプルリクエスト番号: {0}");
		m.put("Invalid request path", "無効なリクエストパス");
		m.put("Invalid selection, click for details", "無効な選択、詳細をクリックしてください");
		m.put("Invalid ssh signature", "無効なssh署名");
		m.put("Invalid state response", "無効な状態応答");
		m.put("Invalid state. Please make sure you are visiting OneDev using server url specified in system setting", 
			"無効な状態です。システム設定で指定されたサーバーURLを使用してOneDevを訪問していることを確認してください");
		m.put("Invalid subscription key", "無効なサブスクリプションキー");
		m.put("Invalid working period", "無効な作業期間");
		m.put("Invitation sent to \"{0}\"", "\"{0}\"に招待を送信しました");
		m.put("Invitation to \"{0}\" deleted", "\"{0}\"への招待を削除しました");
		m.put("Invitations", "招待");
		m.put("Invitations sent", "招待が送信されました");
		m.put("Invite", "招待する");
		m.put("Invite Users", "ユーザーを招待する");
		m.put("Is Site Admin", "サイト管理者である");
		m.put("Issue", "課題");
		m.put("Issue #{0} deleted", "課題 #{0} を削除しました");
		m.put("Issue Board", "課題ボード");
		m.put("Issue Boards", "課題ボード一覧");
		m.put("Issue Close States", "課題終了状態");
		m.put("Issue Creation Setting", "課題作成設定");
		m.put("Issue Creation Settings", "課題作成設定一覧");
		m.put("Issue Custom Fields", "課題カスタムフィールド");
		m.put("Issue Description", "課題説明");
		m.put("Issue Description Templates", "課題説明テンプレート");
		m.put("Issue Details", "課題詳細");
		m.put("Issue Field", "課題フィールド");
		m.put("Issue Field Mapping", "課題フィールドマッピング");
		m.put("Issue Field Mappings", "課題フィールドマッピング一覧");
		m.put("Issue Field Set", "課題フィールドセット");
		m.put("Issue Fields", "課題フィールド一覧");
		m.put("Issue Filter", "課題フィルター");
		m.put("Issue Import Option", "課題インポートオプション");
		m.put("Issue Label Mapping", "課題ラベルマッピング");
		m.put("Issue Label Mappings", "課題ラベルマッピング一覧");
		m.put("Issue Link", "課題リンク");
		m.put("Issue Link Mapping", "課題リンクマッピング");
		m.put("Issue Link Mappings", "課題リンクマッピング一覧");
		m.put("Issue Links", "課題リンク一覧");
		m.put("Issue Management", "課題管理");
		m.put("Issue Notification", "課題通知");
		m.put("Issue Notification Template", "課題通知テンプレート");
		m.put("Issue Notification Unsubscribed", "課題通知の購読解除");
		m.put("Issue Notification Unsubscribed Template", "課題通知購読解除テンプレート");
		m.put("Issue Pattern", "課題パターン");
		m.put("Issue Priority Mapping", "課題優先度マッピング");
		m.put("Issue Priority Mappings", "課題優先度マッピング一覧");
		m.put("Issue Query", "課題クエリ");
		m.put("Issue Settings", "課題設定");
		m.put("Issue State", "課題状態");
		m.put("Issue State Mapping", "課題状態マッピング");
		m.put("Issue State Mappings", "課題状態マッピング一覧");
		m.put("Issue State Transition", "課題状態遷移");
		m.put("Issue State Transitions", "課題状態遷移一覧");
		m.put("Issue States", "課題状態一覧");
		m.put("Issue Statistics", "課題統計");
		m.put("Issue Stats", "課題統計情報");
		m.put("Issue Status Mapping", "課題ステータスマッピング");
		m.put("Issue Status Mappings", "課題ステータスマッピング一覧");
		m.put("Issue Stopwatch Overdue", "課題ストップウォッチ期限切れ");
		m.put("Issue Stopwatch Overdue Notification Template", "課題ストップウォッチ期限切れ通知テンプレート");
		m.put("Issue Tag Mapping", "課題タグマッピング");
		m.put("Issue Tag Mappings", "課題タグマッピング一覧");
		m.put("Issue Template", "課題テンプレート");
		m.put("Issue Transition ({0} -> {1})", "課題遷移 ({0} -> {1})");
		m.put("Issue Type Mapping", "課題タイプマッピング");
		m.put("Issue Type Mappings", "課題タイプマッピング一覧");
		m.put("Issue Votes", "課題投票");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"プロジェクト内での課題管理権限、複数の課題に対する一括操作を含む");
		m.put("Issue count", "課題数");
		m.put("Issue in state", "状態にある課題");
		m.put("Issue list", "課題一覧");
		m.put("Issue management not enabled in this project", "このプロジェクトでは課題管理が有効ではありません");
		m.put("Issue management permission required to move issues", "課題を移動するには課題管理権限が必要です");
		m.put("Issue not exist or access denied", "課題が存在しないか、アクセスが拒否されました");
		m.put("Issue number", "課題番号");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"課題クエリのウォッチは新しい課題にのみ影響します。既存の課題のウォッチ状態を一括管理するには、課題ページでウォッチ状態で課題をフィルタリングし、適切な操作を行ってください");
		m.put("Issue state duration statistics", "課題状態の期間統計");
		m.put("Issue state frequency statistics", "課題状態の頻度統計");
		m.put("Issue state trend statistics", "課題状態の傾向統計");
		m.put("Issue statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"課題統計はエンタープライズ機能です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料でお試しください</a>");
		m.put("Issue workflow changed, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliation</a> needs to be performed to make data consistent. You may do this after making all necessary changes", 
			"課題ワークフローが変更されました。<a wicket:id=\"reconcile\" class=\"link-primary\">整合性を取る</a>必要があります。必要な変更をすべて行った後に実行できます");
		m.put("Issues", "課題一覧");
		m.put("Issues can be created in this project by sending email to this address", "このプロジェクトでは、このアドレスにメールを送信することで課題を作成できます");
		m.put("Issues copied", "課題をコピーしました");
		m.put("Issues moved", "課題を移動しました");
		m.put("Italic", "斜体");
		m.put("Iteration", "イテレーション");
		m.put("Iteration \"{0}\" closed", "イテレーション \"{0}\" を終了しました");
		m.put("Iteration \"{0}\" deleted", "イテレーション \"{0}\" を削除しました");
		m.put("Iteration \"{0}\" is closed", "イテレーション \"{0}\" は終了しています");
		m.put("Iteration \"{0}\" is reopened", "イテレーション「{0}」が再オープンされました");
		m.put("Iteration \"{0}\" reopened", "イテレーション「{0}」が再オープンされました");
		m.put("Iteration Edit Bean", "イテレーション編集ビーン");
		m.put("Iteration Name", "イテレーション名");
		m.put("Iteration Names", "イテレーション名一覧");
		m.put("Iteration Prefix", "イテレーション接頭辞");
		m.put("Iteration list", "イテレーションリスト");
		m.put("Iteration saved", "イテレーションが保存されました");
		m.put("Iteration spans too long to show burndown chart", "イテレーションが長すぎてバーンダウンチャートを表示できません");
		m.put("Iteration start and due date should be specified to show burndown chart", "バーンダウンチャートを表示するには、イテレーションの開始日と終了日を指定する必要があります");
		m.put("Iteration start date should be before due date", "イテレーションの開始日は終了日より前である必要があります");
		m.put("Iterations", "イテレーション一覧");
		m.put("Iterations Bean", "イテレーションビーン");
		m.put("JIRA Issue Priority", "JIRA課題の優先度");
		m.put("JIRA Issue Status", "JIRA課題のステータス");
		m.put("JIRA Issue Type", "JIRA課題の種類");
		m.put("JIRA Project", "JIRAプロジェクト");
		m.put("JIRA Projects to Import", "インポートするJIRAプロジェクト");
		m.put("JUnit Report", "JUnitレポート");
		m.put("JVM", "JVM");
		m.put("JaCoCo Coverage Report", "JaCoCoカバレッジレポート");
		m.put("Jest Coverage Report", "Jestカバレッジレポート");
		m.put("Jest Test Report", "Jestテストレポート");
		m.put("Job", "ジョブ");
		m.put("Job \"{0}\" associated with the build not found.", "ビルドに関連付けられたジョブ「{0}」が見つかりません");
		m.put("Job Authorization", "ジョブ認証");
		m.put("Job Cache Management", "ジョブキャッシュ管理");
		m.put("Job Dependencies", "ジョブ依存関係");
		m.put("Job Dependency", "ジョブ依存");
		m.put("Job Executor", "ジョブエグゼキューター");
		m.put("Job Executor Bean", "ジョブエグゼキュータービーン");
		m.put("Job Executors", "ジョブエグゼキューター一覧");
		m.put("Job Name", "ジョブ名");
		m.put("Job Names", "ジョブ名一覧");
		m.put("Job Param", "ジョブパラメータ");
		m.put("Job Parameters", "ジョブパラメータ");
		m.put("Job Privilege", "ジョブ権限");
		m.put("Job Privileges", "ジョブ権限一覧");
		m.put("Job Properties", "ジョブプロパティ");
		m.put("Job Properties Bean", "ジョブプロパティビーン");
		m.put("Job Property", "ジョブプロパティ");
		m.put("Job Secret", "ジョブシークレット");
		m.put("Job Secret Edit Bean", "ジョブシークレット編集ビーン");
		m.put("Job Secrets", "ジョブシークレット一覧");
		m.put("Job Trigger", "ジョブトリガー");
		m.put("Job Trigger Bean", "ジョブトリガービーン");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"ジョブの管理権限（ジョブのビルドを削除する権限を含む）。これには他のすべてのジョブ権限が含まれます");
		m.put("Job cache \"{0}\" deleted", "ジョブキャッシュ「{0}」が削除されました");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"ジョブ依存関係は、異なるジョブを実行する際の順序と並行性を決定します。また、上流ジョブから取得するアーティファクトを指定することもできます");
		m.put("Job executor tested successfully", "ジョブエグゼキューターが正常にテストされました");
		m.put("Job executors", "ジョブエグゼキューター一覧");
		m.put("Job name", "ジョブ名");
		m.put("Job properties saved", "ジョブプロパティが保存されました");
		m.put("Job secret \"{0}\" deleted", "ジョブシークレット「{0}」が削除されました");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission", 
			"ジョブシークレット「access-token」は、プロジェクトビルド設定でパッケージ${permission}権限を持つアクセストークンとして定義する必要があります");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package read permission", 
			"ジョブシークレット「access-token」は、プロジェクトビルド設定でパッケージ読み取り権限を持つアクセストークンとして定義する必要があります");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package write permission", 
			"ジョブシークレット「access-token」は、プロジェクトビルド設定でパッケージ書き込み権限を持つアクセストークンとして定義する必要があります");
		m.put("Job token", "ジョブトークン");
		m.put("Job will run on head commit of default branch", "ジョブはデフォルトブランチの最新コミットで実行されます");
		m.put("Job will run on head commit of target branch", "ジョブはターゲットブランチの最新コミットで実行されます");
		m.put("Job will run on merge commit of target branch and source branch", "ジョブはターゲットブランチとソースブランチのマージコミットで実行されます");
		m.put("Job will run on merge commit of target branch and source branch.<br><b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"ジョブはターゲットブランチとソースブランチのマージコミットで実行されます。<br><b class='text-info'>注意:</b> ブランチ保護ルールで必要とされない限り、このトリガーは<code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, または<code>[no job]</code>を含むメッセージのコミットを無視します");
		m.put("Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"ジョブはコードがコミットされたときに実行されます。<b class='text-info'>注意:</b> このトリガーは<code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, または<code>[no job]</code>を含むメッセージのコミットを無視します");
		m.put("Job workspace", "ジョブワークスペース");
		m.put("Jobs", "ジョブ一覧");
		m.put("Jobs marked with <span class=\"text-danger\">*</span> are required to be successful", 
			"<span class=\"text-danger\">*</span>でマークされたジョブは成功する必要があります");
		m.put("Jobs required to be successful on merge commit: ", "マージコミットで成功する必要があるジョブ:");
		m.put("Jobs required to be successful: ", "成功する必要があるジョブ:");
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"同じ順序グループとエグゼキューターを持つジョブは順次実行されます。例えば、同じエグゼキューターで実行され、現在のプロジェクトの本番環境にデプロイするジョブの競合を避けるために、このプロパティを<tt>@project_path@:prod</tt>として指定することができます");
		m.put("Key", "キー");
		m.put("Key Fingerprint", "キーのフィンガープリント");
		m.put("Key ID", "キーID");
		m.put("Key Secret", "キーシークレット");
		m.put("Key Type", "キーの種類");
		m.put("Kubectl Config File", "Kubectl設定ファイル");
		m.put("Kubernetes", "Kubernetes");
		m.put("Kubernetes Executor", "Kubernetesエグゼキューター");
		m.put("LDAP URL", "LDAP URL");
		m.put("Label", "ラベル");
		m.put("Label Management", "ラベル管理");
		m.put("Label Management Bean", "ラベル管理ビーン");
		m.put("Label Name", "ラベル名");
		m.put("Label Spec", "ラベル仕様");
		m.put("Label Value", "ラベル値");
		m.put("Labels", "ラベル一覧");
		m.put("Labels Bean", "ラベルビーン");
		m.put("Labels can be defined in Administration / Label Management", "ラベルは管理 / ラベル管理で定義できます");
		m.put("Labels have been updated", "ラベルが更新されました");
		m.put("Language", "言語");
		m.put("Last Accessed", "最終アクセス");
		m.put("Last Finished of Specified Job", "指定されたジョブの最終終了");
		m.put("Last Modified", "最終更新");
		m.put("Last Published", "最終公開");
		m.put("Last Update", "最終更新");
		m.put("Last commit", "最終コミット");
		m.put("Last commit hash", "最終コミットハッシュ");
		m.put("Last commit index version", "最終コミットインデックスバージョン");
		m.put("Leaf Projects", "リーフプロジェクト");
		m.put("Least branch coverage", "最小ブランチカバレッジ");
		m.put("Least line coverage", "最小ラインカバレッジ");
		m.put("Leave a comment", "コメントを残す");
		m.put("Leave a note", "メモを残す");
		m.put("Left", "左");
		m.put("Less", "少ない");
		m.put("License Agreement", "ライセンス契約");
		m.put("License Setting", "ライセンス設定");
		m.put("Licensed To", "ライセンス対象");
		m.put("Licensed To:", "ライセンス対象:");
		m.put("Line", "行");
		m.put("Line changes", "行の変更");
		m.put("Line: ", "行:");
		m.put("Lines", "行数");
		m.put("Link", "リンク");
		m.put("Link Existing User", "既存ユーザーをリンク");
		m.put("Link Spec", "リンク仕様");
		m.put("Link Spec Opposite", "リンク仕様の反対");
		m.put("Link Text", "リンクテキスト");
		m.put("Link URL", "リンクURL");
		m.put("Link URL should be specified", "リンクURLを指定する必要があります");
		m.put("Link User Bean", "ユーザービーンをリンク");
		m.put("Linkable Issues", "リンク可能な課題");
		m.put("Linkable Issues On the Other Side", "反対側のリンク可能な課題");
		m.put("Links", "リンク");
		m.put("Links can be used to associate different issues. For instance, an issue can be linked to sub issues or related issues", 
			"リンクを使用して異なる課題を関連付けることができます。例えば、課題をサブ課題や関連課題にリンクすることができます");
		m.put("List", "リスト");
		m.put("Literal", "リテラル");
		m.put("Literal default value", "リテラルのデフォルト値");
		m.put("Literal value", "リテラル値");
		m.put("Load Keys", "キーをロード");
		m.put("Loading emojis...", "絵文字をロード中...");
		m.put("Loading...", "ロード中...");
		m.put("Log", "ログ");
		m.put("Log Work", "作業ログ");
		m.put("Log not available for offline agent", "オフラインエージェントではログは利用できません");
		m.put("Log work", "作業ログ");
		m.put("Login Name", "ログイン名");
		m.put("Login and generate refresh token", "ログインしてリフレッシュトークンを生成");
		m.put("Login name already used by another account", "ログイン名は他のアカウントで既に使用されています");
		m.put("Login name or email", "ログイン名またはメール");
		m.put("Login name or email address", "ログイン名またはメールアドレス");
		m.put("Login to OneDev docker registry", "OneDev Dockerレジストリにログイン");
		m.put("Login to comment", "コメントするにはログインしてください");
		m.put("Login to comment on selection", "選択にコメントするにはログインしてください");
		m.put("Login to vote", "投票するにはログインしてください");
		m.put("Login user needs to have package write permission over the project below", "ログインユーザーは以下のプロジェクトに対するパッケージ書き込み権限を持つ必要があります");
		m.put("Login with {0}", "{0}でログイン");
		m.put("Logo for Dark Mode", "ダークモード用ロゴ");
		m.put("Logo for Light Mode", "ライトモード用ロゴ");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"上記アカウントの長期リフレッシュトークンは、Gmailにアクセスするためのアクセストークンを生成するために使用されます。<b class='text-info'>ヒント: </b>このフィールドの右側にあるボタンを使用してリフレッシュトークンを生成できます。クライアントID、クライアントシークレット、またはアカウント名が変更された場合は、リフレッシュトークンを再生成する必要があります");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"上記アカウントの長期リフレッシュトークンは、Office 365メールサーバーにアクセスするためのアクセストークンを生成するために使用されます。<b class='text-info'>ヒント: </b>このフィールドの右側にあるボタンを使用してOffice 365アカウントにログインし、リフレッシュトークンを生成できます。テナントID、クライアントID、クライアントシークレット、またはユーザープリンシパル名が変更された場合は、リフレッシュトークンを再生成する必要があります");
		m.put("Longest Duration First", "最長期間優先");
		m.put("Looks like a GPG signature but without necessary data", "必要なデータがないGPG署名のようです");
		m.put("Low", "低い");
		m.put("Low Severity", "低い重大度");
		m.put("MERGED", "マージ済み");
		m.put("MS Teams Notifications", "MS Teams通知");
		m.put("Mail", "メール");
		m.put("Mail Connector", "メールコネクタ");
		m.put("Mail Connector Bean", "メールコネクタビーン");
		m.put("Mail Service", "メールサービス");
		m.put("Mail Service Test", "メールサービステスト");
		m.put("Mail service not configured", "メールサービスが構成されていません");
		m.put("Mail service settings saved", "メールサービス設定が保存されました");
		m.put("Make sure <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 or higher</a> is installed", 
			"<a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11以上</a>がインストールされていることを確認してください");
		m.put("Make sure current user has permission to run docker containers", "現在のユーザーがDockerコンテナを実行する権限を持っていることを確認してください");
		m.put("Make sure docker engine is installed and docker command line is available in system path", 
			"Dockerエンジンがインストールされており、Dockerコマンドラインがシステムパスにあることを確認してください");
		m.put("Make sure git version 2.11.1 or higher is installed and available in system path", "Gitバージョン2.11.1以上がインストールされており、システムパスにあることを確認してください");
		m.put("Make sure git-lfs is installed and available in system path if you want to retrieve LFS files", 
			"Git-lfsがインストールされており、LFSファイルを取得する場合はシステムパスにあることを確認してください");
		m.put("Make sure the access token has package read permission over the project", "アクセストークンがプロジェクトに対するパッケージ読み取り権限を持っていることを確認してください");
		m.put("Make sure the access token has package write permission over the project", "アクセストークンがプロジェクトに対するパッケージ書き込み権限を持っていることを確認してください");
		m.put("Make sure the access token has package write permission over the project. Also make sure to run command <code>chmod 0600 $HOME/.gem/credentials</code> after creating the file", 
			"アクセストークンがプロジェクトに対するパッケージ書き込み権限を持っていることを確認してください。また、ファイル作成後に<code>chmod 0600 $HOME/.gem/credentials</code>コマンドを実行することを確認してください");
		m.put("Make sure the account has package ${permission} permission over the project", "アカウントがプロジェクトに対するパッケージ${permission}権限を持っていることを確認してください");
		m.put("Make sure the account has package read permission over the project", "アカウントがプロジェクトに対するパッケージ読み取り権限を持っていることを確認してください");
		m.put("Make sure the user has package write permission over the project", "ユーザーがプロジェクトに対するパッケージ書き込み権限を持っていることを確認してください");
		m.put("Malformed %sbase query", "不正な%sbaseクエリ");
		m.put("Malformed %squery", "不正な%sクエリ");
		m.put("Malformed build spec (import project: {0}, import revision: {1})", "不正なビルド仕様（インポートプロジェクト: {0}, インポートリビジョン: {1}）");
		m.put("Malformed email address", "不正なメールアドレス");
		m.put("Malformed filter", "不正なフィルター");
		m.put("Malformed name filter", "不正な名前フィルター");
		m.put("Malformed query", "不正なクエリ");
		m.put("Malformed ssh signature", "不正な形式のssh署名");
		m.put("Malformed test suite filter", "不正なテストスイートフィルター");
		m.put("Manage Job", "ジョブ管理");
		m.put("Manager DN", "マネージャーDN");
		m.put("Manager Password", "マネージャーパスワード");
		m.put("Manifest blob unknown", "不明なマニフェストブロブ");
		m.put("Manifest invalid", "無効なマニフェスト");
		m.put("Manifest unknown", "不明なマニフェスト");
		m.put("Many commands print outputs with ANSI colors in TTY mode to help identifying problems easily. However some commands running in this mode may wait for user input to cause build hanging. This can normally be fixed by adding extra options to the command", 
			"多くのコマンドはTTYモードでANSIカラーを使用して出力を表示し、問題を簡単に特定できるようにします。ただし、このモードで実行される一部のコマンドはユーザー入力を待機し、ビルドが停止する原因となる場合があります。通常、コマンドに追加オプションを付加することで修正できます");
		m.put("Mark a property archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived properties will not be shown by default", 
			"プロパティが現在のビルド仕様で使用されなくなった場合でも、古いビルドを再現するために存在する必要がある場合は、プロパティをアーカイブ済みとしてマークします。アーカイブ済みプロパティはデフォルトでは表示されません");
		m.put("Mark a secret archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived secrets will not be shown by default", 
			"秘密が現在のビルド仕様で使用されなくなった場合でも、古いビルドを再現するために存在する必要がある場合は、秘密をアーカイブ済みとしてマークします。アーカイブ済み秘密はデフォルトでは表示されません");
		m.put("Markdown", "Markdown");
		m.put("Markdown Report", "Markdownレポート");
		m.put("Markdown from file", "ファイルからのMarkdown");
		m.put("Maven(s)", "Maven(s)");
		m.put("Max Code Search Entries", "最大コード検索エントリ数");
		m.put("Max Commit Message Line Length", "最大コミットメッセージ行長");
		m.put("Max Git LFS File Size (MB)", "最大Git LFSファイルサイズ（MB）");
		m.put("Max Retries", "最大リトライ回数");
		m.put("Max Upload File Size (MB)", "最大アップロードファイルサイズ（MB）");
		m.put("Max Value", "最大値");
		m.put("Maximum number of entries to return when search code in repository", "リポジトリ内のコード検索時に返されるエントリの最大数");
		m.put("Maximum of retries before giving up", "諦める前の最大リトライ回数");
		m.put("May not be empty", "空であってはなりません");
		m.put("Medium", "中程度");
		m.put("Medium Severity", "中程度の重大度");
		m.put("Members", "メンバー");
		m.put("Memory", "メモリ");
		m.put("Memory Limit", "メモリ制限");
		m.put("Memory Request", "メモリ要求");
		m.put("Mention Someone", "誰かをメンションする");
		m.put("Mention someone", "誰かをメンションする");
		m.put("Merge", "マージ");
		m.put("Merge Strategy", "マージ戦略");
		m.put("Merge Target Branch into Source Branch", "ターゲットブランチをソースブランチにマージ");
		m.put("Merge branch \"{0}\" into branch \"{1}\"", "ブランチ「{0}」をブランチ「{1}」にマージ");
		m.put("Merge branch \"{0}\" of project \"{1}\" into branch \"{2}\"", "プロジェクト「{1}」のブランチ「{0}」をブランチ「{2}」にマージ");
		m.put("Merge preview not calculated yet", "マージプレビューはまだ計算されていません");
		m.put("Merged", "マージ済み");
		m.put("Merged pull request \"{0}\" ({1})", "プルリクエスト「{0}」({1})がマージされました");
		m.put("Merges pull request", "プルリクエストをマージします");
		m.put("Meta", "メタ");
		m.put("Meta Info", "メタ情報");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "最小値");
		m.put("Minimum length of the password", "パスワードの最小長");
		m.put("Missing Commit", "不足しているコミット");
		m.put("Missing Commits", "不足しているコミット");
		m.put("Month", "月");
		m.put("Months", "月");
		m.put("Months to Display", "表示する月数");
		m.put("More", "もっと見る");
		m.put("More Options", "その他のオプション");
		m.put("More Settings", "その他の設定");
		m.put("More commits", "さらにコミットを見る");
		m.put("More info", "詳細情報");
		m.put("More operations", "その他の操作");
		m.put("Most branch coverage", "最もブランチカバレッジが高い");
		m.put("Most line coverage", "最もラインカバレッジが高い");
		m.put("Most probably there are import errors in the <a wicket:id=\"buildSpec\">build spec</a>", 
			"<a wicket:id=\"buildSpec\">ビルド仕様</a>にインポートエラーがある可能性が高いです");
		m.put("Mount Docker Sock", "Dockerソケットをマウント");
		m.put("Move All Queried Issues To...", "すべてのクエリ済み課題を移動...");
		m.put("Move All Queried Projects To...", "すべてのクエリ済みプロジェクトを移動...");
		m.put("Move Selected Issues To...", "選択した課題を移動...");
		m.put("Move Selected Projects To...", "選択したプロジェクトを移動...");
		m.put("Multiple Lines", "複数行");
		m.put("Multiple On the Other Side", "反対側に複数");
		m.put("Must not be empty", "空であってはなりません");
		m.put("My Access Tokens", "私のアクセストークン");
		m.put("My Basic Settings", "私の基本設定");
		m.put("My Email Addresses", "私のメールアドレス");
		m.put("My GPG Keys", "私のGPGキー");
		m.put("My Profile", "私のプロフィール");
		m.put("My SSH Keys", "私のSSHキー");
		m.put("My SSO Accounts", "私のSSOアカウント");
		m.put("Mypy Report", "Mypyレポート");
		m.put("N/A", "N/A");
		m.put("NPM(s)", "NPM(s)");
		m.put("Name", "名前");
		m.put("Name Of Empty Value", "空の値の名前");
		m.put("Name On the Other Side", "反対側の名前");
		m.put("Name Prefix", "名前の接頭辞");
		m.put("Name already used by another access token of the owner", "所有者の別のアクセストークンで既に使用されている名前");
		m.put("Name already used by another link", "別のリンクで既に使用されている名前");
		m.put("Name and name on the other side should be different", "名前と反対側の名前は異なる必要があります");
		m.put("Name containing spaces or starting with dash needs to be quoted", "スペースを含む名前やダッシュで始まる名前は引用符で囲む必要があります");
		m.put("Name invalid", "名前が無効です");
		m.put("Name of the link", "リンクの名前");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"反対側のリンクの名前。例えば、名前が<tt>サブ課題</tt>の場合、反対側の名前は<tt>親課題</tt>になることがあります");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"プロバイダーの名前は2つの目的を果たします: <ul><li>ログインボタンに表示<li>認証コールバックURLを形成します。これは<i>&lt;サーバーURL&gt;/~sso/callback/&lt;名前&gt;</i>になります</ul>");
		m.put("Name reversely", "名前を逆に");
		m.put("Name unknown", "名前不明");
		m.put("Name your file", "ファイルに名前を付ける");
		m.put("Named Agent Queries Bean", "名前付きエージェントクエリBean");
		m.put("Named Agent Query", "名前付きエージェントクエリ");
		m.put("Named Build Queries Bean", "名前付きビルドクエリBean");
		m.put("Named Build Query", "名前付きビルドクエリ");
		m.put("Named Code Comment Queries Bean", "名前付きコードコメントクエリBean");
		m.put("Named Code Comment Query", "名前付きコードコメントクエリ");
		m.put("Named Commit Queries Bean", "名前付きコミットクエリBean");
		m.put("Named Commit Query", "名前付きコミットクエリ");
		m.put("Named Element", "名前付き要素");
		m.put("Named Issue Queries Bean", "名前付き課題クエリBean");
		m.put("Named Issue Query", "名前付き課題クエリ");
		m.put("Named Pack Queries Bean", "名前付きパッククエリBean");
		m.put("Named Pack Query", "名前付きパッククエリ");
		m.put("Named Project Queries Bean", "名前付きプロジェクトクエリBean");
		m.put("Named Project Query", "名前付きプロジェクトクエリ");
		m.put("Named Pull Request Queries Bean", "名前付きプルリクエストクエリBean");
		m.put("Named Pull Request Query", "名前付きプルリクエストクエリ");
		m.put("Named Query", "名前付きクエリ");
		m.put("Network Options", "ネットワークオプション");
		m.put("Never", "決して");
		m.put("Never expire", "期限切れなし");
		m.put("New Board", "新しいボード");
		m.put("New Invitation Bean", "新しい招待Bean");
		m.put("New Issue", "新しい課題");
		m.put("New Password", "新しいパスワード");
		m.put("New State", "新しい状態");
		m.put("New User Bean", "新しいユーザーBean");
		m.put("New Value", "新しい値");
		m.put("New issue board created", "新しい課題ボードが作成されました");
		m.put("New project created", "新しいプロジェクトが作成されました");
		m.put("New user created", "新しいユーザーが作成されました");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"新しいバージョンが利用可能です。セキュリティ/重要な更新は赤、バグ修正は黄色、機能更新は青です。変更を表示するにはクリックしてください。システム設定で無効化できます。");
		m.put("Next", "次へ");
		m.put("Next commit", "次のコミット");
		m.put("Next {0}", "次の{0}");
		m.put("No", "いいえ");
		m.put("No Activity Days", "アクティビティなしの日数");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"アカウントにSSHキーが設定されていません。<a wicket:id=\"sshKeys\" class=\"link-primary\">キーを追加</a>するか、<a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> URLに切り替えることができます。");
		m.put("No SSL", "SSLなし");
		m.put("No accessible reports", "アクセス可能なレポートなし");
		m.put("No activity for some time", "しばらくアクティビティなし");
		m.put("No agents to pause", "一時停止するエージェントなし");
		m.put("No agents to remove", "削除するエージェントなし");
		m.put("No agents to restart", "再起動するエージェントなし");
		m.put("No agents to resume", "再開するエージェントなし");
		m.put("No aggregation", "集計なし");
		m.put("No any", "何もなし");
		m.put("No any matches", "一致するものがありません");
		m.put("No applicable transitions or no permission to transit", "適用可能な遷移がないか、遷移する権限がありません");
		m.put("No attributes defined (can only be edited when agent is online)", "定義された属性なし（エージェントがオンラインの場合のみ編集可能）");
		m.put("No audits", "監査なし");
		m.put("No authorized job secret found (project: {0}, job secret: {1})", "認証済みのジョブシークレットが見つかりません（プロジェクト: {0}, ジョブシークレット: {1}）");
		m.put("No branch to cherry-pick to", "チェリーピックするブランチなし");
		m.put("No branch to revert on", "リバートするブランチなし");
		m.put("No branches Found", "ブランチが見つかりません");
		m.put("No branches found", "ブランチが見つかりません");
		m.put("No build in query context", "クエリコンテキストにビルドなし");
		m.put("No builds", "ビルドなし");
		m.put("No builds to cancel", "キャンセルするビルドなし");
		m.put("No builds to delete", "削除するビルドなし");
		m.put("No builds to re-run", "再実行するビルドなし");
		m.put("No comment", "コメントなし");
		m.put("No comments to delete", "削除するコメントなし");
		m.put("No comments to set as read", "既読に設定するコメントなし");
		m.put("No comments to set resolved", "解決済みに設定するコメントなし");
		m.put("No comments to set unresolved", "未解決に設定するコメントなし");
		m.put("No commit in query context", "クエリコンテキストにコミットなし");
		m.put("No config file", "設定ファイルなし");
		m.put("No current build in query context", "クエリコンテキストに現在のビルドなし");
		m.put("No current commit in query context", "クエリコンテキストに現在のコミットなし");
		m.put("No current pull request in query context", "クエリコンテキストに現在のプルリクエストなし");
		m.put("No data", "データなし");
		m.put("No default branch", "デフォルトブランチなし");
		m.put("No default group", "デフォルトグループなし");
		m.put("No default roles", "デフォルトの役割なし");
		m.put("No default value", "デフォルト値なし");
		m.put("No description", "説明なし");
		m.put("No diffs", "差分なし");
		m.put("No diffs to navigate", "ナビゲートする差分なし");
		m.put("No directories to skip", "スキップするディレクトリなし");
		m.put("No disallowed file types", "許可されていないファイルタイプはありません");
		m.put("No executors defined. Jobs will use auto-discovered executors instead", "エグゼキュータが定義されていません。ジョブは自動検出されたエグゼキュータを使用します");
		m.put("No external password authenticator", "外部パスワード認証がありません");
		m.put("No external password authenticator to authenticate user \"{0}\"", "ユーザー\"{0}\"を認証する外部パスワード認証がありません");
		m.put("No fields to prompt", "プロンプトするフィールドなし");
		m.put("No fields to remove", "削除するフィールドなし");
		m.put("No file attachments", "ファイル添付なし");
		m.put("No group by", "グループ化なし");
		m.put("No groups claim returned", "返されたグループクレームなし");
		m.put("No groups to remove from", "削除するグループがありません");
		m.put("No ignore file", "無視ファイルなし");
		m.put("No ignored licenses", "無視されたライセンスなし");
		m.put("No image attachments", "画像添付なし");
		m.put("No imports defined", "定義されたインポートなし");
		m.put("No issue boards defined", "定義された課題ボードなし");
		m.put("No issues in iteration", "イテレーション内の課題なし");
		m.put("No issues to copy", "コピーする課題なし");
		m.put("No issues to delete", "削除する課題なし");
		m.put("No issues to edit", "編集する課題なし");
		m.put("No issues to export", "エクスポートする課題なし");
		m.put("No issues to move", "移動する課題なし");
		m.put("No issues to set as read", "既読に設定する課題なし");
		m.put("No issues to sync estimated/spent time", "推定/使用時間を同期する課題なし");
		m.put("No issues to watch/unwatch", "ウォッチ/ウォッチ解除する課題なし");
		m.put("No jobs defined", "定義されたジョブなし");
		m.put("No jobs found", "ジョブが見つかりません");
		m.put("No limit", "制限なし");
		m.put("No mail service", "メールサービスなし");
		m.put("No obvious changes", "明らかな変更なし");
		m.put("No one", "誰もいない");
		m.put("No packages to delete", "削除するパッケージなし");
		m.put("No parent", "親なし");
		m.put("No previous successful build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a> to calculate fixed issues since", 
			"<a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">同じストリーム</a>で以前の成功したビルドがなく、修正された課題を計算できません。");
		m.put("No projects found", "プロジェクトが見つかりません");
		m.put("No projects to delete", "削除するプロジェクトなし");
		m.put("No projects to modify", "変更するプロジェクトなし");
		m.put("No projects to move", "移動するプロジェクトなし");
		m.put("No properties defined", "定義されたプロパティなし");
		m.put("No proxy", "プロキシなし");
		m.put("No pull request in query context", "クエリコンテキストにプルリクエストなし");
		m.put("No pull requests to delete", "削除するプルリクエストなし");
		m.put("No pull requests to discard", "破棄するプルリクエストなし");
		m.put("No pull requests to set as read", "既読に設定するプルリクエストなし");
		m.put("No pull requests to watch/unwatch", "ウォッチ/ウォッチ解除するプルリクエストなし");
		m.put("No refs to build on behalf of", "代理でビルドする参照なし");
		m.put("No required services", "必要なサービスなし");
		m.put("No response body", "レスポンスボディなし");
		m.put("No secret config", "シークレット設定なし");
		m.put("No services defined", "定義されたサービスなし");
		m.put("No start/due date", "開始/期限日なし");
		m.put("No step templates defined", "定義されたステップテンプレートなし");
		m.put("No suggestions", "提案はありません");
		m.put("No tags found", "タグが見つかりません");
		m.put("No timesheets defined", "定義されたタイムシートなし");
		m.put("No user found with login name or email: ", "ログイン名またはメールでユーザーが見つかりません:");
		m.put("No users to convert to service accounts", "サービスアカウントに変換するユーザーがいません");
		m.put("No users to delete", "削除するユーザーなし");
		m.put("No users to disable", "無効化するユーザーなし");
		m.put("No users to enable", "有効化するユーザーなし");
		m.put("No users to remove from group", "グループから削除するユーザーがいません");
		m.put("No valid query to show progress", "進捗を表示する有効なクエリがありません");
		m.put("No valid signature for head commit", "ヘッドコミットに有効な署名がありません");
		m.put("No valid signature for head commit of target branch", "ターゲットブランチのヘッドコミットに有効な署名がありません");
		m.put("No value", "値がありません");
		m.put("No verified primary email address", "確認済みのプライマリメールアドレスがありません");
		m.put("Node Selector", "ノードセレクター");
		m.put("Node Selector Entry", "ノードセレクターエントリー");
		m.put("None", "なし");
		m.put("Not Active Since", "以降アクティブではありません");
		m.put("Not Used Since", "以降使用されていません");
		m.put("Not a verified email of signing GPG key", "署名GPGキーの確認済みメールではありません");
		m.put("Not a verified email of signing ssh key owner", "署名sshキー所有者の確認済みメールではありません");
		m.put("Not allowed file type: {0}", "許可されていないファイルタイプ: {0}");
		m.put("Not assigned", "未割り当て");
		m.put("Not authorized to create project under \"{0}\"", "\"{0}\"の下にプロジェクトを作成する権限がありません");
		m.put("Not authorized to create root project", "ルートプロジェクトを作成する権限がありません");
		m.put("Not authorized to move project under this parent", "この親プロジェクトの下にプロジェクトを移動する権限がありません");
		m.put("Not authorized to set as root project", "ルートプロジェクトとして設定する権限がありません");
		m.put("Not covered", "カバーされていません");
		m.put("Not covered by any test", "いかなるテストでもカバーされていません");
		m.put("Not displaying any fields", "フィールドを表示していません");
		m.put("Not displaying any links", "リンクを表示していません");
		m.put("Not passed", "未合格");
		m.put("Not rendered in failsafe mode", "フェイルセーフモードでレンダリングされていません");
		m.put("Not run", "未実行");
		m.put("Not specified", "未指定");
		m.put("Note", "注記");
		m.put("Nothing to preview", "プレビューするものがありません");
		m.put("Notification", "通知");
		m.put("Notifications", "通知一覧");
		m.put("Notify Build Events", "ビルドイベントを通知");
		m.put("Notify Code Comment Events", "コードコメントイベントを通知");
		m.put("Notify Code Push Events", "コードプッシュイベントを通知");
		m.put("Notify Issue Events", "課題イベントを通知");
		m.put("Notify Own Events", "自身のイベントを通知");
		m.put("Notify Pull Request Events", "プルリクエストイベントを通知");
		m.put("Notify Users", "ユーザーに通知");
		m.put("Ntfy.sh Notifications", "Ntfy.sh通知");
		m.put("NuGet(s)", "NuGet(s)");
		m.put("NuSpec", "NuSpec");
		m.put("Number of CPU Cores", "CPUコア数");
		m.put("Number of SSH Keys", "SSHキー数");
		m.put("Number of builds to preserve", "保持するビルド数");
		m.put("Number of project replicas, including primary and backups", "プライマリおよびバックアップを含むプロジェクトレプリカ数");
		m.put("Number of recent months to show statistics for", "統計を表示する最近の月数");
		m.put("OAuth2 Client information | CLIENT ID", "OAuth2クライアント情報 | クライアントID");
		m.put("OAuth2 Client information | CLIENT SECRET", "OAuth2クライアント情報 | クライアントシークレット");
		m.put("OCI Layout Directory", "OCIレイアウトディレクトリ");
		m.put("OIDC error: Inconsistent sub in ID token and userinfo", "OIDCエラー: IDトークンとユーザー情報のサブが一致しません");
		m.put("OOPS! There Is An Error", "おっと！エラーがあります");
		m.put("OPEN", "オープン");
		m.put("OS", "OS");
		m.put("OS Arch", "OSアーキテクチャ");
		m.put("OS User Name", "OSユーザー名");
		m.put("OS Version", "OSバージョン");
		m.put("OS/ARCH", "OS/ARCH");
		m.put("Offline", "オフライン");
		m.put("Ok", "OK");
		m.put("Old Name", "旧名称");
		m.put("Old Password", "旧パスワード");
		m.put("On Behalf Of", "代理として");
		m.put("On Branches", "ブランチ上");
		m.put("OneDev Issue Field", "OneDev課題フィールド");
		m.put("OneDev Issue Link", "OneDev課題リンク");
		m.put("OneDev Issue State", "OneDev課題状態");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"OneDevはリポジトリファイルをコード検索、行統計、コード貢献統計のために分析します。この設定はどのファイルを分析するかを指定し、スペース区切りの<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスパターン</a>を期待します。パターンは「-」を付けて除外できます。例えば<code>-**/vendors/**</code>はパスにvendorsを含むすべてのファイルを除外します。<b>注意: </b>この設定を変更しても新しいコミットにのみ影響します。履歴コミットに変更を適用するには、サーバーを停止し、<a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>プロジェクトのストレージディレクトリ</a>内のフォルダ<code>index</code>および<code>info/commit</code>を削除してください。サーバーが起動するとリポジトリが再分析されます");
		m.put("OneDev configures git hooks to communicate with itself via curl", "OneDevはgitフックを設定してcurlを介して自身と通信します");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"OneDevはユーザーDNを検索して決定する必要があり、グループ取得が有効な場合はユーザーグループ情報も検索します。これらの操作が認証を必要とする場合は、このオプションを選択し、「マネージャー」DNとパスワードを指定してください");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"OneDevはリポジトリを管理するためにgitコマンドラインを必要とします。必要な最低バージョンは2.11.1です。また、ビルドジョブでLFSファイルを取得したい場合はgit-lfsがインストールされていることを確認してください");
		m.put("Online", "オンライン");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", 
			"ターゲットブランチがソースブランチに対して高速フォワードできない場合のみマージコミットを作成");
		m.put("Only projects manageable by access token owner can be authorized", "アクセストークン所有者が管理可能なプロジェクトのみが認可されます");
		m.put("Only system level audit events are displayed here. To view audit events for a specific project, please visit the project audit log page", 
			"ここにはシステムレベルの監査イベントのみが表示されます。特定のプロジェクトの監査イベントを表示するには、プロジェクト監査ログページをご覧ください");
		m.put("Only users able to authenticate via password can be linked", "パスワードで認証できるユーザーのみリンク可能");
		m.put("Open", "オープン");
		m.put("Open new pull request", "新しいプルリクエストを開く");
		m.put("Open terminal of current running step", "現在実行中のステップのターミナルを開く");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"OpenIDクライアント識別は、このOneDevインスタンスをクライアントアプリケーションとして登録する際にOpenIDプロバイダーによって割り当てられます");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"OpenIDクライアントシークレットは、このOneDevインスタンスをクライアントアプリケーションとして登録する際にOpenIDプロバイダーによって生成されます");
		m.put("OpenSSH Public Key", "OpenSSH公開鍵");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"OpenSSH公開鍵は「ssh-rsa」、「ecdsa-sha2-nistp256」、「ecdsa-sha2-nistp384」、「ecdsa-sha2-nistp521」、「ssh-ed25519」、「sk-ecdsa-sha2-nistp256@openssh.com」、または「sk-ssh-ed25519@openssh.com」で始まります");
		m.put("Opened issue \"{0}\" ({1})", "課題「{0}」({1})を開きました");
		m.put("Opened pull request \"{0}\" ({1})", "プルリクエスト「{0}」({1})を開きました");
		m.put("Operation", "操作");
		m.put("Operation Failed", "操作に失敗しました");
		m.put("Operation Successful", "操作が成功しました");
		m.put("Operations", "操作");
		m.put("Optional", "オプション");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"課題を作成するプロジェクトを任意で指定します。空欄の場合は現在のプロジェクトに作成されます");
		m.put("Optionally add new users to specified default group", "新しいユーザーを指定されたデフォルトグループに任意で追加");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"認証された新しいユーザーを、メンバーシップ情報が利用できない場合に指定されたグループに任意で追加");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"認証された新しいユーザーを、メンバーシップ情報が取得されない場合に指定されたグループに任意で追加");
		m.put("Optionally choose required builds. You may also input jobs not listed here, and press ENTER to add them", 
			"必要なビルドを任意で選択します。ここにリストされていないジョブを入力し、ENTERキーを押して追加することもできます");
		m.put("Optionally configure proxy to access remote repository. Proxy should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;", 
			"リモートリポジトリにアクセスするためのプロキシを任意で設定します。プロキシは&lt;プロキシホスト&gt;:&lt;プロキシポート&gt;の形式である必要があります");
		m.put("Optionally define a unique key for the project with two or more upper case letters. This key can be used to reference issues, builds, and pull requests with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of <code>&lt;project path&gt;#&lt;number&gt;</code>", 
			"プロジェクトの一意のキーを任意で定義します。このキーは、安定した短い形式<code>&lt;プロジェクトキー&gt;-&lt;番号&gt;</code>で課題、ビルド、プルリクエストを参照するために使用できます。<code>&lt;プロジェクトパス&gt;#&lt;番号&gt;</code>の代わりに使用されます");
		m.put("Optionally define parameter specifications of the job", "ジョブのパラメータ仕様を任意で定義");
		m.put("Optionally define parameter specifications of the step template", "ステップテンプレートのパラメータ仕様を任意で定義");
		m.put("Optionally describe the group", "グループを任意で説明");
		m.put("Optionally describes the custom field. Html tags are accepted", "カスタムフィールドを任意で説明します。HTMLタグが使用可能です");
		m.put("Optionally describes the param. Html tags are accepted.", "パラメータを任意で説明します。HTMLタグが使用可能です");
		m.put("Optionally filter builds", "ビルドを任意でフィルタリング");
		m.put("Optionally filter issues", "課題を任意でフィルタリング");
		m.put("Optionally filter pull requests", "プルリクエストを任意でフィルタリング");
		m.put("Optionally leave a note", "任意で注記を残す");
		m.put("Optionally mount directories or files under job workspace into container", "ジョブワークスペース内のディレクトリやファイルをコンテナにマウントすることを任意で選択");
		m.put("Optionally select fields to prompt when this button is pressed", "このボタンが押されたときにプロンプトするフィールドを任意で選択");
		m.put("Optionally select fields to remove when this transition happens", "この遷移が発生したときに削除するフィールドを任意で選択");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"ユーザーLDAPエントリ内の属性名を任意で指定します。この属性の値がユーザーのメールとして使用されます。このフィールドは通常RFC 2798に従って<i>mail</i>に設定されます");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"ユーザーLDAPエントリ内の属性名を任意で指定します。この属性の値がユーザーのフルネームとして使用されます。このフィールドは通常RFC 2798に従って<i>displayName</i>に設定されます。空欄の場合、ユーザーのフルネームは取得されません");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"オプションで、GitHubアクセス用トークンとして使用する<a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a>を指定してください。これはGitHubでホストされている依存関係のリリースノートを取得するために使用され、認証されたアクセスはより高いレート制限を得ることができます。");
		m.put("Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko", 
			"オプションで、kanikoの<a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>追加オプション</a>を指定してください。");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane", 
			"オプションで、craneの<a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>追加オプション</a>を指定してください。");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane", 
			"オプションで、craneの<a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>追加オプション</a>を指定してください。");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to build for platform of the node running the job", 
			"オプションで、ビルドするプラットフォームを<span class='text-info'>カンマ区切り</span>で指定してください。例: <tt>linux/amd64,linux/arm64</tt>。ジョブを実行しているノードのプラットフォームでビルドする場合は空のままにしてください。");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to scan, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to scan all platforms in OCI layout", 
			"オプションで、スキャンするプラットフォームを<span class='text-info'>カンマ区切り</span>で指定してください。例: <tt>linux/amd64,linux/arm64</tt>。OCIレイアウト内のすべてのプラットフォームをスキャンする場合は空のままにしてください。");
		m.put("Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use file <tt>Dockerfile</tt> under build path specified above", 
			"オプションで、<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対するDockerfileの相対パスを指定してください。上記で指定したビルドパスの下にある<tt>Dockerfile</tt>ファイルを使用する場合は空のままにしてください。");
		m.put("Optionally specify JavaScript config to be used by Renovate CLI", "オプションで、Renovate CLIで使用するJavaScript設定を指定してください。");
		m.put("Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. Leave empty to derive from server url", 
			"オプションで、SSHルートURLを指定してください。これはSSHプロトコルを介してプロジェクトクローンURLを構築するために使用されます。サーバーURLから派生する場合は空のままにしてください。");
		m.put("Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of the text input", 
			"オプションで、テキスト入力の有効な値のための<a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>正規表現パターン</a>を指定してください。");
		m.put("Optionally specify a OneDev project to be used as parent of imported projects. Leave empty to import as root projects", 
			"オプションで、インポートされたプロジェクトの親として使用するOneDevプロジェクトを指定してください。ルートプロジェクトとしてインポートする場合は空のままにしてください。");
		m.put("Optionally specify a OneDev project to be used as parent of imported repositories. Leave empty to import as root projects", 
			"オプションで、インポートされたリポジトリの親として使用するOneDevプロジェクトを指定してください。ルートプロジェクトとしてインポートする場合は空のままにしてください。");
		m.put("Optionally specify a base query for the list", "オプションで、リストの基本クエリを指定してください。");
		m.put("Optionally specify a base query to filter/order issues in backlog. Backlog issues are those not associating with current iteration", 
			"オプションで、バックログ内の課題をフィルタリング/並べ替えるための基本クエリを指定してください。バックログ課題は現在のイテレーションに関連付けられていないものです。");
		m.put("Optionally specify a base query to filter/order issues of the board", "オプションで、ボードの課題をフィルタリング/並べ替えるための基本クエリを指定してください。");
		m.put("Optionally specify a cron expression to schedule database auto-backup. The cron expression format is <em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>.For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz tutorial</a>.The backup files will be placed into <em>db-backup</em> folder under OneDev installation directory. In case multiple servers connect to form a cluster, auto-backup takes place on the <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>lead server</a>. Leave this property empty if you do not want to enable database auto backup.", 
			"オプションで、データベース自動バックアップをスケジュールするためのcron式を指定してください。cron式の形式は<em>&lt;秒&gt; &lt;分&gt; &lt;時&gt; &lt;日&gt; &lt;月&gt; &lt;曜日&gt;</em>です。例えば、<em>0 0 1 * * ?</em>は毎日午前1時を意味します。形式の詳細については、<a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartzチュートリアル</a>を参照してください。バックアップファイルはOneDevインストールディレクトリの<em>db-backup</em>フォルダに配置されます。複数のサーバーがクラスターを形成する場合、自動バックアップは<a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>リードサーバー</a>で行われます。このプロパティを空のままにしておくと、データベース自動バックアップは有効になりません。");
		m.put("Optionally specify a date field to hold due date information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"オプションで、期限情報を保持する日付フィールドを指定してください。<br><b>注意: </b>適切なオプションがない場合は、OneDev課題フィールドをカスタマイズすることができます。");
		m.put("Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to put retrieved artifacts. Leave empty to use job workspace itself", 
			"オプションで、取得したアーティファクトを配置する<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する相対パスを指定してください。ジョブワークスペース自体を使用する場合は空のままにしてください。");
		m.put("Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. <b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, as the volume is only used to hold temporary build files", 
			"オプションで、ビルドボリュームを動的に割り当てるためのストレージクラスを指定してください。デフォルトのストレージクラスを使用する場合は空のままにしてください。<b class='text-warning'>注意:</b> ストレージクラスの再利用ポリシーは<code>Delete</code>に設定する必要があります。ボリュームは一時的なビルドファイルを保持するためだけに使用されます。");
		m.put("Optionally specify a working period field to hold estimated time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"オプションで、推定時間情報を保持する作業期間フィールドを指定してください。<br><b>注意: </b>適切なオプションがない場合は、OneDev課題フィールドをカスタマイズすることができます。");
		m.put("Optionally specify a working period field to hold spent time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"オプションで、費やした時間情報を保持する作業期間フィールドを指定してください。<br><b>注意: </b>適切なオプションがない場合は、OneDev課題フィールドをカスタマイズすることができます。");
		m.put("Optionally specify a working period field to hold time estimate infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"オプションで、時間推定情報を保持する作業期間フィールドを指定してください。<br><b>注意: </b>適切なオプションがない場合は、OneDev課題フィールドをカスタマイズすることができます。");
		m.put("Optionally specify a working period field to hold time spent infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"オプションで、費やした時間情報を保持する作業期間フィールドを指定してください。<br><b>注意: </b>適切なオプションがない場合は、OneDev課題フィールドをカスタマイズすることができます。");
		m.put("Optionally specify additional options for buildx build command", "オプションで、buildxビルドコマンドの追加オプションを指定してください。");
		m.put("Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, the response header <code>Access-Control-Allow-Origin</code> will be set to the same value", 
			"オプションで、許可された<a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a>オリジンを指定してください。CORSのシンプルまたはプリフライトリクエストの場合、リクエストヘッダー<code>Origin</code>の値がここに含まれている場合、レスポンスヘッダー<code>Access-Control-Allow-Origin</code>は同じ値に設定されます。");
		m.put("Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match", 
			"オプションで、自己登録ユーザーの許可されたメールドメインを指定してください。パターンマッチには'*'または'?'を使用してください。");
		m.put("Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). Leave empty to all types", 
			"オプションで、コミットメッセージフッターのチェックに適用されるコミットタイプを指定してください（ENTERキーを押して値を追加）。すべてのタイプを適用する場合は空のままにしてください。");
		m.put("Optionally specify applicable jobs of this executor", "このエグゼキュータに適用可能なジョブを任意で指定してください");
		m.put("Optionally specify applicable users who pushed the change", "変更をプッシュした適用可能なユーザーを任意で指定");
		m.put("Optionally specify arguments to run above image", "オプションで、上記のイメージを実行するための引数を指定してください。");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"オプションで、依存関係から<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に取得するアーティファクトを指定してください。公開されたアーティファクト（アーティファクト公開ステップを介して）だけが取得可能です。アーティファクトを取得しない場合は空のままにしてください。");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"オプションで、このボタンを押すための認証済みロールを指定してください。指定されていない場合、すべてのユーザーが許可されます。");
		m.put("Optionally specify base query of the list", "オプションで、リストの基本クエリを指定してください。");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"オプションで、このシークレットにアクセスできるブランチ/ユーザー/グループを指定してください。空のままにすると、外部プルリクエストを介してトリガーされたジョブを含む、任意のジョブがこのシークレットにアクセスできます。");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"オプションで、<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対するビルドコンテキストパスを指定してください。ジョブワークスペース自体を使用する場合は空のままにしてください。<code>Dockerfile</code>ファイルはビルドコンテキストディレクトリに存在することが期待されますが、オプション<code>--dockerfile</code>で異なる場所を指定することもできます。");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"オプションで、<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対するビルドパスを指定してください。ジョブワークスペース自体を使用する場合は空のままにしてください。");
		m.put("Optionally specify cluster role the job pods service account binding to. This is necessary if you want to do things such as running other Kubernetes pods in job command", 
			"オプションで、ジョブポッドのサービスアカウントがバインドされるクラスター役割を指定してください。これは、ジョブコマンドで他のKubernetesポッドを実行するなどの操作を行いたい場合に必要です。");
		m.put("Optionally specify comma separated licenses to be ignored", "オプションで、無視するライセンスをカンマ区切りで指定してください。");
		m.put("Optionally specify container arguments separated by space. Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse this with container options which should be specified in executor setting", 
			"オプションで、スペースで区切られたコンテナ引数を指定してください。スペースを含む単一の引数は引用符で囲む必要があります。<b class='text-warning'>注意: </b>これはエグゼキューター設定で指定する必要があるコンテナオプションと混同しないでください。");
		m.put("Optionally specify cpu limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"オプションで、このエグゼキューターを使用する各ジョブ/サービスのCPU制限を指定してください。詳細については<a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetesリソース管理</a>を確認してください。");
		m.put("Optionally specify cpu limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> of relevant containers", 
			"オプションで、このエグゼキューターを使用する各ジョブ/サービスのCPU制限を指定してください。これは関連するコンテナのオプション<a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a>として使用されます。");
		m.put("Optionally specify criteria of issues which can be linked", "オプションで、リンク可能な課題の基準を指定してください。");
		m.put("Optionally specify criteria of issues which can be linked on the other side", "オプションで、他の側でリンク可能な課題の基準を指定してください。");
		m.put("Optionally specify custom fields allowed to edit when open new issues", "オプションで、新しい課題を開く際に編集を許可するカスタムフィールドを指定してください。");
		m.put("Optionally specify depth for a shallow clone in order to speed up source retrieval", 
			"オプションで、ソース取得を高速化するための浅いクローンの深さを指定してください。");
		m.put("Optionally specify description of the issue", "オプションで、課題の説明を指定してください。");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"オプションで、スキャンパス内のスキップするディレクトリまたはグロブパターンを指定してください。複数のスキップはスペースで区切る必要があります。");
		m.put("Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types", 
			"拡張子で許可されていないファイルタイプを指定します（値を追加するにはENTERを押します）、例えば <code>exe</code>, <code>bin</code>。すべてのファイルタイプを許可するには空のままにします。");
		m.put("Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. Leave empty to use docker executable in PATH", 
			"オプションで、Docker実行ファイルを指定してください。例: <i>/usr/local/bin/docker</i>。PATH内のDocker実行ファイルを使用する場合は空のままにしてください。");
		m.put("Optionally specify docker options to create network. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"オプションで、ネットワークを作成するためのDockerオプションを指定してください。複数のオプションはスペースで区切る必要があり、スペースを含む単一のオプションは引用符で囲む必要があります。");
		m.put("Optionally specify docker options to run container. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"オプションで、コンテナを実行するためのDockerオプションを指定してください。複数のオプションはスペースで区切る必要があり、スペースを含む単一のオプションは引用符で囲む必要があります。");
		m.put("Optionally specify docker sock to use. Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows", 
			"オプションで使用するDockerソックを指定してください。Linuxではデフォルトで<i>/var/run/docker.sock</i>、Windowsでは<i>//./pipe/docker_engine</i>です。");
		m.put("Optionally specify environment variables for the container", "オプションで、コンテナの環境変数を指定してください。");
		m.put("Optionally specify environment variables for this step", "オプションで、このステップの環境変数を指定してください。");
		m.put("Optionally specify environment variables of the service", "オプションで、サービスの環境変数を指定してください。");
		m.put("Optionally specify estimated time.", "オプションで、推定時間を指定してください。");
		m.put("Optionally specify executor for this job. Leave empty to use auto-discover executor", 
			"このジョブのエグゼキュータを任意で指定してください。空欄の場合、自動検出エグゼキュータを使用します");
		m.put("Optionally specify executor for this job. Leave empty to use first applicable executor", 
			"このジョブのエグゼキュータを任意で指定してください。空欄の場合、最初に適用可能なエグゼキュータを使用します");
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"オプションで、キャッシュ変更を検出する際に無視するキャッシュパスに対する相対ファイルを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'または'?'を使用してください。複数のファイルはスペースで区切る必要があり、スペースを含む単一のファイルは引用符で囲む必要があります。");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"オプションで、ユーザーのグループメンバーシップ情報を取得する場合のグループ検索ベースを指定してください。例: <i>cn=Users, dc=example, dc=com</i>。Active Directoryグループに適切な権限を付与するには、同じ名前のOneDevグループを定義する必要があります。OneDev側でグループメンバーシップを管理する場合は空のままにしてください。");
		m.put("Optionally specify issue links allowed to edit", "オプションで、編集を許可する課題リンクを指定してください。");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "オプションで、このテンプレートに適用される課題を指定してください。すべてに適用する場合は空のままにしてください。");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"オプションで、この遷移に適用される課題を指定してください。すべての課題に適用する場合は空のままにしてください。");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"オプションで、この遷移に適用される課題を指定してください。すべての課題に適用する場合は空のままにしてください。");
		m.put("Optionally specify jobs allowed to use this script", "オプションで、このスクリプトを使用することが許可されているジョブを指定してください。");
		m.put("Optionally specify memory limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"オプションで、このエグゼキューターを使用する各ジョブ/サービスのメモリ制限を指定してください。詳細については<a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetesリソース管理</a>を確認してください。");
		m.put("Optionally specify memory limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> of relevant containers", 
			"オプションで、このエグゼキューターを使用する各ジョブ/サービスのメモリ制限を指定してください。これは関連するコンテナのオプション<a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a>として使用されます。");
		m.put("Optionally specify merge strategy of created pull request. Leave empty to use default strategy of each project", 
			"オプションで、作成されたプルリクエストのマージ戦略を指定してください。各プロジェクトのデフォルト戦略を使用する場合は空のままにしてください。");
		m.put("Optionally specify message of the tag", "オプションで、タグのメッセージを指定してください。");
		m.put("Optionally specify name of the attribute inside the user LDAP entry whose values will be taken as user SSH keys. SSH keys will be managed by LDAP only if this field is set", 
			"オプションで、ユーザーLDAPエントリ内の属性名を指定してください。その値がユーザーSSHキーとして使用されます。このフィールドが設定されている場合、SSHキーはLDAPによってのみ管理されます。");
		m.put("Optionally specify node selector of the job pods", "オプションで、ジョブポッドのノードセレクターを指定してください。");
		m.put("Optionally specify options for docker builder prune command", "オプションで、Dockerビルダープルーンコマンドのオプションを指定してください。");
		m.put("Optionally specify options for scp command. Multiple options need to be separated with space", 
			"オプションで、scpコマンドのオプションを指定してください。複数のオプションはスペースで区切る必要があります。");
		m.put("Optionally specify options for ssh command. Multiple options need to be separated with space", 
			"オプションで、sshコマンドのオプションを指定してください。複数のオプションはスペースで区切る必要があります。");
		m.put("Optionally specify options passed to renovate cli. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"オプションで、Renovate CLIに渡されるオプションを指定してください。複数のオプションはスペースで区切る必要があり、スペースを含む単一のオプションは引用符で囲む必要があります。");
		m.put("Optionally specify osv scanner <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. You may ignore particular vulnerabilities via this file", 
			"オプションで、<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>内のosvスキャナー<a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>設定ファイル</a>を指定してください。このファイルを使用して特定の脆弱性を無視することができます。");
		m.put("Optionally specify path protection rules", "オプションで、パス保護ルールを指定してください。");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"オプションで、trivy<a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>無視ファイル</a>として使用する<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する相対パスを指定してください。");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"オプションで、trivy<a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>シークレット設定</a>として使用する<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する相対パスを指定してください。");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"オプションで、アーティファクトを公開するための<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する相対パスを指定してください。ジョブワークスペース自体を使用する場合は空のままにしてください。");
		m.put("Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. Leave empty to pull all platforms in image", 
			"オプションで、プルするプラットフォームを指定してください。例: <tt>linux/amd64</tt>。イメージ内のすべてのプラットフォームをプルする場合は空のままにしてください。");
		m.put("Optionally specify project to show builds of. Leave empty to show builds of all projects with permissions", 
			"オプションで、ビルドを表示するプロジェクトを指定してください。権限を持つすべてのプロジェクトのビルドを表示する場合は空のままにしてください。");
		m.put("Optionally specify project to show issues of. Leave empty to show issues of all accessible projects", 
			"オプションで、課題を表示するプロジェクトを指定してください。アクセス可能なすべてのプロジェクトの課題を表示する場合は空のままにしてください。");
		m.put("Optionally specify project to show packages of. Leave empty to show packages of all projects with permissions", 
			"オプションで、パッケージを表示するプロジェクトを指定してください。権限を持つすべてのプロジェクトのパッケージを表示する場合は空のままにしてください。");
		m.put("Optionally specify ref of above job, for instance <i>refs/heads/main</i>. Use * for wildcard match", 
			"オプションで、上記のジョブのリファレンスを指定してください。例: <i>refs/heads/main</i>。ワイルドカードマッチには*を使用してください。");
		m.put("Optionally specify registry logins to override those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token secret for password secret", 
			"オプションで、ジョブエグゼキューターで定義されたものを上書きするためのレジストリログインを指定してください。組み込みレジストリの場合、レジストリURLには<code>@server_url@</code>、ユーザー名には<code>@job_token@</code>、パスワードシークレットにはアクセストークンシークレットを使用してください。");
		m.put("Optionally specify relative directory to put uploaded files", "オプションで、アップロードされたファイルを配置する相対ディレクトリを指定してください。");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to clone code into. Leave empty to use job workspace itself", 
			"オプションで、コードをクローンするための<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する相対パスを指定してください。ジョブワークスペース自体を使用する場合は空のままにしてください。");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"オプションで、スキャンするための<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する相対パスを指定してください。ジョブワークスペース自体を使用する場合は空のままにしてください。");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"オプションで、依存関係の脆弱性をスキャンするための<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する相対パスを指定してください。複数のパスを指定することができ、スペースで区切る必要があります。ジョブワークスペース自体を使用する場合は空のままにしてください。");
		m.put("Optionally specify required reviewers for changes of specified branch", "オプションで、指定されたブランチの変更に対する必要なレビュアーを指定してください。");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"オプションで、ブランチを作成するためのリビジョンを指定してください。ビルドコミットから作成する場合は空のままにしてください。");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"オプションで、ビルドアーティファクトを保存するための別のディレクトリを指定してください。絶対ディレクトリでない場合、サイトディレクトリに対する相対ディレクトリと見なされます。");
		m.put("Optionally specify separate directory to store git lfs files. Non-absolute directory is considered to be relative to site directory", 
			"オプションで、Git LFSファイルを保存するための別のディレクトリを指定してください。絶対ディレクトリでない場合、サイトディレクトリに対する相対ディレクトリと見なされます。");
		m.put("Optionally specify separate directory to store package files. Non-absolute directory is considered to be relative to site directory", 
			"オプションで、パッケージファイルを保存するための別のディレクトリを指定してください。絶対ディレクトリでない場合、サイトディレクトリに対する相対ディレクトリと見なされます。");
		m.put("Optionally specify services required by this job. <b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors (server docker executor, remote docker executor, or kubernetes executor)", 
			"オプションで、このジョブに必要なサービスを指定してください。<b class='text-warning'>注意:</b> サービスはDocker対応エグゼキューター（サーバーDockerエグゼキューター、リモートDockerエグゼキューター、またはKubernetesエグゼキューター）のみでサポートされています。");
		m.put("Optionally specify space-separated branches applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"オプションで、この遷移に適用されるスペース区切りのブランチを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'または'?'を使用してください。'-'で除外を指定してください。すべてに一致する場合は空のままにしてください。");
		m.put("Optionally specify space-separated branches applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for default branch", 
			"オプションで、このトリガーに適用されるスペース区切りのブランチを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'または'?'を使用してください。'-'で除外を指定してください。デフォルトブランチの場合は空のままにしてください。");
		m.put("Optionally specify space-separated branches to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"オプションで、チェックするスペース区切りのブランチを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'または'?'を使用してください。'-'で除外を指定してください。すべてのブランチに一致する場合は空のままにしてください。");
		m.put("Optionally specify space-separated commit messages applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"この遷移に適用可能なスペース区切りのコミットメッセージを任意で指定します。ワイルドカードマッチには '*' または '?' を使用します。除外するには '-' をプレフィックスに付けます。すべてに一致させるには空のままにします");
		m.put("Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all files", 
			"オプションで、チェックするスペース区切りのファイルを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'または'?'を使用してください。'-'で除外を指定してください。すべてのファイルに一致する場合は空のままにしてください。");
		m.put("Optionally specify space-separated jobs applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"オプションで、この遷移に適用されるスペース区切りのジョブを指定してください。ワイルドカードマッチには'*'または'?'を使用してください。'-'で除外を指定してください。すべてに一致する場合は空のままにしてください。");
		m.put("Optionally specify space-separated projects applicable for this trigger. This is useful for instance when you want to prevent the job from being triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all projects", 
			"オプションで、このトリガーに適用されるスペース区切りのプロジェクトを指定してください。これは、例えばフォークされたプロジェクトでジョブがトリガーされるのを防ぎたい場合に便利です。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'または'?'を使用してください。'-'で除外を指定してください。すべてのプロジェクトに一致する場合は空のままにしてください。");
		m.put("Optionally specify space-separated projects to search in. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to search in all projects with code read permission", 
			"オプションで、検索するスペース区切りのプロジェクトを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'または'?'を使用してください。'-'で除外を指定してください。コード読み取り権限を持つすべてのプロジェクトを検索する場合は空のままにしてください。");
		m.put("Optionally specify space-separated reports. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"必要に応じてスペース区切りのレポートを指定します。ワイルドカードマッチには '*' または '?' を使用します。'-' を付けて除外します");
		m.put("Optionally specify space-separated service images applicable for this locator. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"必要に応じて、このロケーターに適用されるスペース区切りのサービスイメージを指定します。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には '**', '*' または '?' を使用します。'-' を付けて除外します。空欄の場合はすべて一致します");
		m.put("Optionally specify space-separated service names applicable for this locator. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"必要に応じて、このロケーターに適用されるスペース区切りのサービス名を指定します。ワイルドカードマッチには '*' または '?' を使用します。'-' を付けて除外します。空欄の場合はすべて一致します");
		m.put("Optionally specify space-separated tags to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all tags", 
			"必要に応じてチェックするスペース区切りのタグを指定します。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には '**', '*' または '?' を使用します。'-' を付けて除外します。空欄の場合はすべてのタグに一致します");
		m.put("Optionally specify space-separated target branches of the pull requests to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"必要に応じてチェックするプルリクエストのターゲットブランチをスペース区切りで指定します。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には '**', '*' または '?' を使用します。'-' を付けて除外します。空欄の場合はすべてのブランチに一致します");
		m.put("Optionally specify the OpenID claim to retrieve groups of authenticated user. Depending on the provider, you may need to request additional scopes above to make this claim available", 
			"必要に応じて認証ユーザーのグループを取得するOpenIDクレームを指定します。プロバイダーによっては、このクレームを利用可能にするために追加のスコープを要求する必要があります");
		m.put("Optionally specify the maximum value allowed.", "必要に応じて許容される最大値を指定します");
		m.put("Optionally specify the minimum value allowed.", "必要に応じて許容される最小値を指定します");
		m.put("Optionally specify the project to publish site files to. Leave empty to publish to current project", 
			"必要に応じてサイトファイルを公開するプロジェクトを指定します。空欄の場合は現在のプロジェクトに公開されます");
		m.put("Optionally specify uid:gid to run container as. <b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or using user namespace remapping", 
			"必要に応じてコンテナを実行するuid:gidを指定します。<b class='text-warning'>注意:</b> コンテナランタイムがルートレスまたはユーザー名前空間リマッピングを使用している場合、この設定は空欄にする必要があります");
		m.put("Optionally specify user name to access remote repository", "必要に応じてリモートリポジトリにアクセスするためのユーザー名を指定します");
		m.put("Optionally specify valid scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope", 
			"必要に応じて従来のコミットの有効なスコープを指定します（ENTERキーを押して値を追加）。空欄の場合は任意のスコープを許可します");
		m.put("Optionally specify valid types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type", 
			"必要に応じて従来のコミットの有効なタイプを指定します（ENTERキーを押して値を追加）。空欄の場合は任意のタイプを許可します");
		m.put("Optionally specify value of git config <code>pack.packSizeLimit</code> for the repository", 
			"必要に応じてリポジトリのgit config <code>pack.packSizeLimit</code> の値を指定します");
		m.put("Optionally specify value of git config <code>pack.threads</code> for the repository", 
			"必要に応じてリポジトリのgit config <code>pack.threads</code> の値を指定します");
		m.put("Optionally specify value of git config <code>pack.window</code> for the repository", 
			"必要に応じてリポジトリのgit config <code>pack.window</code> の値を指定します");
		m.put("Optionally specify value of git config <code>pack.windowMemory</code> for the repository", 
			"必要に応じてリポジトリのgit config <code>pack.windowMemory</code> の値を指定します");
		m.put("Optionally specify where to run service pods specified in job. The first matching locator will be used. If no any locators are found, node selector of the executor will be used", 
			"必要に応じてジョブで指定されたサービスポッドを実行する場所を指定します。最初に一致するロケーターが使用されます。ロケーターが見つからない場合、エグゼキューターのノードセレクターが使用されます");
		m.put("Optionally specify working directory of the container. Leave empty to use default working directory of the container", 
			"必要に応じてコンテナの作業ディレクトリを指定します。空欄の場合はコンテナのデフォルトの作業ディレクトリが使用されます");
		m.put("Options", "オプション");
		m.put("Or manually enter the secret key below in your authenticator app", "または、以下のシークレットキーを認証アプリに手動で入力してください");
		m.put("Order By", "並び替え");
		m.put("Order More User Months", "さらに多くのユーザーモンスを注文");
		m.put("Order Subscription", "サブスクリプションを注文");
		m.put("Ordered List", "順序付きリスト");
		m.put("Ordered list", "順序付きリスト");
		m.put("Osv License Scanner", "Osvライセンススキャナー");
		m.put("Osv Vulnerability Scanner", "Osv脆弱性スキャナー");
		m.put("Other", "その他");
		m.put("Outline", "アウトライン");
		m.put("Outline Search", "アウトライン検索");
		m.put("Output", "出力");
		m.put("Overall", "全体");
		m.put("Overall Estimated Time:", "全体の推定時間:");
		m.put("Overall Spent Time:", "全体の費やした時間:");
		m.put("Overview", "概要");
		m.put("Own:", "所有:");
		m.put("Ownered By", "所有者");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "PEM秘密鍵は '-----BEGIN RSA PRIVATE KEY-----' で始まります");
		m.put("PENDING", "保留中");
		m.put("PMD Report", "PMDレポート");
		m.put("Pack", "パック");
		m.put("Pack Notification", "パック通知");
		m.put("Pack Size Limit", "パックサイズ制限");
		m.put("Pack Type", "パックタイプ");
		m.put("Package", "パッケージ");
		m.put("Package Management", "パッケージ管理");
		m.put("Package Notification", "パッケージ通知");
		m.put("Package Notification Template", "パッケージ通知テンプレート");
		m.put("Package Privilege", "パッケージ権限");
		m.put("Package Storage", "パッケージストレージ");
		m.put("Package list", "パッケージリスト");
		m.put("Package {0} deleted", "パッケージ {0} が削除されました");
		m.put("Packages", "パッケージ");
		m.put("Page Not Found", "ページが見つかりません");
		m.put("Page is in error, reload to recover", "ページにエラーがあります。リロードして回復してください");
		m.put("Param Instance", "パラメータインスタンス");
		m.put("Param Instances", "パラメータインスタンス");
		m.put("Param Map", "パラメータマップ");
		m.put("Param Matrix", "パラメータマトリックス");
		m.put("Param Name", "パラメータ名");
		m.put("Param Spec", "パラメータ仕様");
		m.put("Param Spec Bean", "パラメータ仕様Bean");
		m.put("Parameter", "パラメータ");
		m.put("Parameter Specs", "パラメータ仕様");
		m.put("Params", "パラメータ");
		m.put("Params & Triggers", "パラメータとトリガー");
		m.put("Params to Display", "表示するパラメータ");
		m.put("Parent Bean", "親Bean");
		m.put("Parent OneDev Project", "親OneDevプロジェクト");
		m.put("Parent Project", "親プロジェクト");
		m.put("Parent project not found", "親プロジェクトが見つかりません");
		m.put("Parents", "親");
		m.put("Partially covered", "部分的にカバーされています");
		m.put("Partially covered by some tests", "一部のテストで部分的にカバーされています");
		m.put("Passcode", "パスコード");
		m.put("Passed", "合格");
		m.put("Password", "パスワード");
		m.put("Password Authenticator", "パスワード認証");
		m.put("Password Edit Bean", "パスワード編集Bean");
		m.put("Password Must Contain Digit", "パスワードには数字を含める必要があります");
		m.put("Password Must Contain Lowercase", "パスワードには小文字を含める必要があります");
		m.put("Password Must Contain Special Character", "パスワードには特殊文字を含める必要があります");
		m.put("Password Must Contain Uppercase", "パスワードには大文字を含める必要があります");
		m.put("Password Policy", "パスワードポリシー");
		m.put("Password Reset", "パスワードリセット");
		m.put("Password Reset Bean", "パスワードリセットBean");
		m.put("Password Reset Template", "パスワードリセットテンプレート");
		m.put("Password Secret", "パスワードシークレット");
		m.put("Password and its confirmation should be identical.", "パスワードとその確認は同一である必要があります。");
		m.put("Password changed. Please login with your new password", "パスワードが変更されました。新しいパスワードでログインしてください");
		m.put("Password has been changed", "パスワードが変更されました");
		m.put("Password has been removed", "パスワードが削除されました");
		m.put("Password has been set", "パスワードが設定されました");
		m.put("Password of the user", "ユーザーのパスワード");
		m.put("Password or Access Token for Remote Repository", "リモートリポジトリのパスワードまたはアクセストークン");
		m.put("Password reset request has been sent", "パスワードリセットリクエストが送信されました");
		m.put("Password reset url is invalid or obsolete", "パスワードリセットURLが無効または古くなっています");
		m.put("PasswordMinimum Length", "パスワードの最小長");
		m.put("Paste subscription key here", "ここにサブスクリプションキーを貼り付けてください");
		m.put("Path containing spaces or starting with dash needs to be quoted", "スペースを含むパスまたはダッシュで始まるパスは引用符で囲む必要があります");
		m.put("Path placeholder", "パスプレースホルダー");
		m.put("Path to kubectl", "kubectlへのパス");
		m.put("Paths", "パス");
		m.put("Pattern", "パターン");
		m.put("Pause", "一時停止");
		m.put("Pause All Queried Agents", "クエリされたすべてのエージェントを一時停止");
		m.put("Pause Selected Agents", "選択されたエージェントを一時停止");
		m.put("Paused", "一時停止中");
		m.put("Paused all queried agents", "クエリされたすべてのエージェントを一時停止しました");
		m.put("Paused selected agents", "選択されたエージェントを一時停止しました");
		m.put("Pem Private Key", "Pem秘密鍵");
		m.put("Pending", "保留中");
		m.put("Performance", "パフォーマンス");
		m.put("Performance Setting", "パフォーマンス設定");
		m.put("Performance Settings", "パフォーマンス設定");
		m.put("Performance settings have been saved", "パフォーマンス設定が保存されました");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\"", 
			"あいまい検索を実行中です。検索テキストを'~'で囲むことで条件を追加できます。例: ~検索するテキスト~ および \"状態\" が \"オープン\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"", 
			"あいまい検索を実行中です。検索テキストを'~'で囲むことで条件を追加できます。例: ~検索するテキスト~ および \"タイプ\" が \"NPM\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online", 
			"あいまい検索を実行中です。検索テキストを'~'で囲むことで条件を追加できます。例: ~検索するテキスト~ および オンライン");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"あいまい検索を実行中です。検索テキストを'~'で囲むことで条件を追加できます。例: ~検索するテキスト~ および オープン");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and owned by me", 
			"あいまい検索を実行中です。検索テキストを'~'で囲むことで条件を追加できます。例: ~検索するテキスト~ および 自分が所有");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved", 
			"あいまい検索を実行中です。検索テキストを'~'で囲むことで条件を追加できます。例: ~検索するテキスト~ および 未解決");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"あいまい検索を実行中です。検索テキストを'~'で囲むことで条件を追加できます。例: ~検索するテキスト~ 著者(robin)");
		m.put("Permanent link", "永久リンク");
		m.put("Permanent link of this selection", "この選択の永久リンク");
		m.put("Permission denied", "権限が拒否されました");
		m.put("Permission will be checked upon actual operation", "実際の操作時に権限が確認されます");
		m.put("Physical memory in mega bytes", "物理メモリ（メガバイト単位）");
		m.put("Pick Existing", "既存を選択");
		m.put("Pin this issue", "この問題をピン留め");
		m.put("Pipeline", "パイプライン");
		m.put("Placeholder", "プレースホルダー");
		m.put("Plain text expected", "プレーンテキストが期待されます");
		m.put("Platform", "プラットフォーム");
		m.put("Platforms", "プラットフォーム");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"以下のリカバリーコードを<a wicket:id=\"download\" class=\"font-weight-bolder\">ダウンロード</a>して秘密に保管してください。これらのコードは、認証アプリケーションにアクセスできない場合にアカウントへの一時的なアクセスを提供するために使用できます。これらは<b>再表示されません</b>");
		m.put("Please Confirm", "確認してください");
		m.put("Please Note", "注意してください");
		m.put("Please check your email for password reset instructions", "パスワードリセットの指示についてはメールを確認してください");
		m.put("Please choose revision to create branch from", "ブランチを作成するためのリビジョンを選択してください");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "まず<a wicket:id=\"mailSetting\">メール設定</a>を構成してください");
		m.put("Please confirm", "確認してください");
		m.put("Please confirm the password.", "パスワードを確認してください。");
		m.put("Please follow <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">this instruction</a> to resolve the conflicts", 
			"競合を解決するには<a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">この指示</a>に従ってください");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"二要素認証を有効にした際に保存したリカバリーコードのいずれかを入力してください");
		m.put("Please login to perform this operation", "この操作を実行するにはログインしてください");
		m.put("Please login to perform this query", "このクエリを実行するにはログインしてください");
		m.put("Please resolve undefined field values below", "以下の未定義フィールド値を解決してください");
		m.put("Please resolve undefined fields below", "以下の未定義フィールドを解決してください");
		m.put("Please resolve undefined states below. Note that if you select to delete an undefined state, all issues with that state will be deleted", 
			"以下の未定義状態を解決してください。未定義状態を削除することを選択した場合、その状態を持つすべての問題が削除されます");
		m.put("Please select agents to pause", "一時停止するエージェントを選択してください");
		m.put("Please select agents to remove", "削除するエージェントを選択してください");
		m.put("Please select agents to restart", "再起動するエージェントを選択してください");
		m.put("Please select agents to resume", "再開するエージェントを選択してください");
		m.put("Please select branches to create pull request", "プルリクエストを作成するブランチを選択してください");
		m.put("Please select builds to cancel", "キャンセルするビルドを選択してください");
		m.put("Please select builds to delete", "削除するビルドを選択してください");
		m.put("Please select builds to re-run", "再実行するビルドを選択してください");
		m.put("Please select comments to delete", "削除するコメントを選択してください");
		m.put("Please select comments to set resolved", "解決済みに設定するコメントを選択してください");
		m.put("Please select comments to set unresolved", "未解決に設定するコメントを選択してください");
		m.put("Please select different branches", "異なるブランチを選択してください");
		m.put("Please select fields to update", "更新するフィールドを選択してください");
		m.put("Please select groups to remove from", "削除するグループを選択してください");
		m.put("Please select issues to copy", "コピーする問題を選択してください");
		m.put("Please select issues to delete", "削除する問題を選択してください");
		m.put("Please select issues to edit", "編集する問題を選択してください");
		m.put("Please select issues to move", "移動する問題を選択してください");
		m.put("Please select issues to sync estimated/spent time", "推定/使用時間を同期する問題を選択してください");
		m.put("Please select packages to delete", "削除するパッケージを選択してください");
		m.put("Please select projects to delete", "削除するプロジェクトを選択してください");
		m.put("Please select projects to modify", "変更するプロジェクトを選択してください");
		m.put("Please select projects to move", "移動するプロジェクトを選択してください");
		m.put("Please select pull requests to delete", "削除するプルリクエストを選択してください");
		m.put("Please select pull requests to discard", "破棄するプルリクエストを選択してください");
		m.put("Please select pull requests to watch/unwatch", "ウォッチ/ウォッチ解除するプルリクエストを選択してください");
		m.put("Please select query watches to delete", "削除するクエリウォッチを選択してください");
		m.put("Please select revision to create tag from", "タグを作成するリビジョンを選択してください");
		m.put("Please select revisions to compare", "比較するリビジョンを選択してください");
		m.put("Please select users to convert to service accounts", "サービスアカウントに変換するユーザーを選択してください");
		m.put("Please select users to disable", "無効化するユーザーを選択してください");
		m.put("Please select users to enable", "有効化するユーザーを選択してください");
		m.put("Please select users to remove from group", "グループから削除するユーザーを選択してください");
		m.put("Please specify file name above before editing content", "コンテンツを編集する前に上記のファイル名を指定してください");
		m.put("Please switch to packages page of a particular project for the instructions", "特定のプロジェクトのパッケージページに切り替えて指示を確認してください");
		m.put("Please wait...", "お待ちください...");
		m.put("Please waiting...", "お待ちください...");
		m.put("Plugin metadata not found", "プラグインメタデータが見つかりません");
		m.put("Poll Interval", "ポーリング間隔");
		m.put("Populate Tag Mappings", "タグマッピングを入力");
		m.put("Port", "ポート");
		m.put("Post", "投稿");
		m.put("Post Build Action", "ビルド後のアクション");
		m.put("Post Build Action Bean", "ビルド後のアクションBean");
		m.put("Post Build Actions", "ビルド後のアクション");
		m.put("Post Url", "投稿URL");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "プレフィックスパターン");
		m.put("Prefix the title with <code>WIP</code> or <code>[WIP]</code> to mark the pull request as work in progress", 
			"プルリクエストを作業中としてマークするには、タイトルに <code>WIP</code> または <code>[WIP]</code> を付けてください");
		m.put("Prepend", "追加");
		m.put("Preserve Days", "保持日数");
		m.put("Preset Commit Message", "プリセットコミットメッセージ");
		m.put("Preset commit message updated", "プリセットコミットメッセージが更新されました");
		m.put("Press 'y' to get permalink", "永久リンクを取得するには'y'を押してください");
		m.put("Prev", "前へ");
		m.put("Prevent Creation", "作成を防止");
		m.put("Prevent Deletion", "削除を防止");
		m.put("Prevent Forced Push", "強制プッシュを防止");
		m.put("Prevent Update", "更新を防止");
		m.put("Preview", "プレビュー");
		m.put("Previous", "前へ");
		m.put("Previous Value", "以前の値");
		m.put("Previous commit", "以前のコミット");
		m.put("Previous {0}", "以前の{0}");
		m.put("Primary", "プライマリ");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "プライマリ<a wicket:id=\"noPrimaryAddressLink\">メールアドレス</a>が指定されていません");
		m.put("Primary Email", "プライマリメール");
		m.put("Primary email address not specified", "プライマリメールアドレスが指定されていません");
		m.put("Primary email address of your account is not specified yet", "アカウントの主メールアドレスはまだ指定されていません");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"主メールアドレスは通知の受信、Gravatarの表示（有効化されている場合）などに使用されます");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"上記アカウントの主メールアドレスまたはエイリアスメールアドレスは、各種メール通知の送信元アドレスとして使用されます。<code>Check Incoming Email</code>オプションが有効化されている場合、ユーザーはこのアドレスに返信して問題やプルリクエストのコメントをメールで投稿することもできます");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"Office 365メールサーバーにログインしてメールを送受信するためのアカウントのプリンシパル名。このアカウントが上記のアプリケーションIDで示される登録アプリケーションを<b>所有</b>していることを確認してください");
		m.put("Private Key Secret", "秘密鍵のシークレット");
		m.put("Private key regenerated and SSH server restarted", "秘密鍵が再生成され、SSHサーバーが再起動されました");
		m.put("Privilege", "権限");
		m.put("Privilege Settings", "権限設定");
		m.put("Product Version", "製品バージョン");
		m.put("Profile", "プロフィール");
		m.put("Programming language", "プログラミング言語");
		m.put("Project", "プロジェクト");
		m.put("Project \"{0}\" deleted", "プロジェクト「{0}」が削除されました");
		m.put("Project Authorization Bean", "プロジェクト認可Bean");
		m.put("Project Authorizations Bean", "プロジェクト認可Beans");
		m.put("Project Build Setting", "プロジェクトビルド設定");
		m.put("Project Dependencies", "プロジェクト依存関係");
		m.put("Project Dependency", "プロジェクト依存関係");
		m.put("Project Id", "プロジェクトID");
		m.put("Project Import Option", "プロジェクトインポートオプション");
		m.put("Project Issue Setting", "プロジェクト課題設定");
		m.put("Project Key", "プロジェクトキー");
		m.put("Project Management", "プロジェクト管理");
		m.put("Project Pack Setting", "プロジェクトパック設定");
		m.put("Project Path", "プロジェクトパス");
		m.put("Project Pull Request Setting", "プロジェクトプルリクエスト設定");
		m.put("Project Replicas", "プロジェクトレプリカ");
		m.put("Project authorizations updated", "プロジェクト認可が更新されました");
		m.put("Project does not have any code yet", "プロジェクトにはまだコードがありません");
		m.put("Project forked", "プロジェクトがフォークされました");
		m.put("Project id", "プロジェクトID");
		m.put("Project list", "プロジェクトリスト");
		m.put("Project manage privilege required to delete \"{0}\"", "「{0}」を削除するにはプロジェクト管理権限が必要です");
		m.put("Project manage privilege required to modify \"{0}\"", "「{0}」を変更するにはプロジェクト管理権限が必要です");
		m.put("Project manage privilege required to move \"{0}\"", "「{0}」を移動するにはプロジェクト管理権限が必要です");
		m.put("Project name", "プロジェクト名");
		m.put("Project not specified yet", "プロジェクトはまだ指定されていません");
		m.put("Project or revision not specified yet", "プロジェクトまたはリビジョンはまだ指定されていません");
		m.put("Project overview", "プロジェクト概要");
		m.put("Project path", "プロジェクトパス");
		m.put("Projects", "プロジェクト");
		m.put("Projects Bean", "プロジェクトBeans");
		m.put("Projects deleted", "プロジェクトが削除されました");
		m.put("Projects modified", "プロジェクトが変更されました");
		m.put("Projects moved", "プロジェクトが移動されました");
		m.put("Projects need to be redistributed when cluster members are added/removed. OneDev does not do this automatically as this is resource intensive, and you may only want to do it after cluster is finalized and stable.", 
			"クラスターメンバーが追加/削除された場合、プロジェクトを再分配する必要があります。OneDevはこれを自動的に行いません。これはリソースを多く消費するため、クラスタが確定し安定した後に行うことを推奨します。");
		m.put("Promotions", "プロモーション");
		m.put("Prompt Fields", "プロンプトフィールド");
		m.put("Properties", "プロパティ");
		m.put("Provide server id (guild id) to restrict access only to server members", "サーバーID（ギルドID）を指定して、サーバーメンバーのみのアクセスを制限します");
		m.put("Proxy", "プロキシ");
		m.put("Prune Builder Cache", "ビルダーキャッシュの削除");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"Docker buildxビルダーのイメージキャッシュを削除します。このステップは、サーバーDockerエグゼキューターまたはリモートDockerエグゼキューターで指定されたbuildxビルダーのキャッシュを削除するためにdocker builder pruneコマンドを呼び出します");
		m.put("Public", "公開");
		m.put("Public Key", "公開鍵");
		m.put("Public Roles", "公開の役割");
		m.put("Publish", "公開");
		m.put("Publish Coverage Report Step", "カバレッジレポート公開ステップ");
		m.put("Publish Problem Report Step", "問題レポート公開ステップ");
		m.put("Publish Report Step", "レポート公開ステップ");
		m.put("Publish Unit Test Report Step", "ユニットテストレポート公開ステップ");
		m.put("Published After", "公開後");
		m.put("Published At", "公開日時");
		m.put("Published Before", "公開前");
		m.put("Published By", "公開者");
		m.put("Published By Project", "プロジェクトによる公開");
		m.put("Published By User", "ユーザーによる公開");
		m.put("Published File", "公開ファイル");
		m.put("Pull Command", "プルコマンド");
		m.put("Pull Image", "イメージのプル");
		m.put("Pull Request", "プルリクエスト");
		m.put("Pull Request Branches", "プルリクエストブランチ");
		m.put("Pull Request Description", "プルリクエスト説明");
		m.put("Pull Request Filter", "プルリクエストフィルター");
		m.put("Pull Request Management", "プルリクエスト管理");
		m.put("Pull Request Markdown Report", "プルリクエストMarkdownレポート");
		m.put("Pull Request Notification", "プルリクエスト通知");
		m.put("Pull Request Notification Template", "プルリクエスト通知テンプレート");
		m.put("Pull Request Notification Unsubscribed", "プルリクエスト通知の購読解除");
		m.put("Pull Request Notification Unsubscribed Template", "プルリクエスト通知購読解除テンプレート");
		m.put("Pull Request Settings", "プルリクエスト設定");
		m.put("Pull Request Statistics", "プルリクエスト統計");
		m.put("Pull Request Title", "プルリクエストタイトル");
		m.put("Pull Requests", "プルリクエスト");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Craneを使用してOCIレイアウトとしてDockerイメージをプルします。このステップは、サーバーDockerエグゼキューター、リモートDockerエグゼキューター、またはKubernetesエグゼキューターによって実行される必要があります");
		m.put("Pull from Remote", "リモートからプル");
		m.put("Pull request", "プルリクエスト");
		m.put("Pull request #{0} already closed", "プルリクエスト#{0}はすでにクローズされています");
		m.put("Pull request #{0} deleted", "プルリクエスト#{0}が削除されました");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"プロジェクト内でのプルリクエスト管理権限、複数のプルリクエストに対するバッチ操作を含む");
		m.put("Pull request already closed", "プルリクエストはすでにクローズされています");
		m.put("Pull request already opened", "プルリクエストはすでにオープンされています");
		m.put("Pull request and code review", "プルリクエストとコードレビュー");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not finished yet", 
			"<a class=\"more-info d-inline link-primary\">必要なビルドの一部</a>がまだ完了していないため、現在プルリクエストをマージすることはできません");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not successful", 
			"<a class=\"more-info d-inline link-primary\">必要なビルドの一部</a>が成功していないため、現在プルリクエストをマージすることはできません");
		m.put("Pull request can not be merged now as it is <a class=\"more-info d-inline link-primary\">pending review</a>", 
			"<a class=\"more-info d-inline link-primary\">レビュー待ち</a>のため、現在プルリクエストをマージすることはできません");
		m.put("Pull request can not be merged now as it was <a class=\"more-info d-inline link-primary\">requested for changes</a>", 
			"<a class=\"more-info d-inline link-primary\">変更が要求されました</a>ため、現在プルリクエストをマージすることはできません");
		m.put("Pull request can not be merged now as valid signature is required for head commit", 
			"ヘッドコミットに有効な署名が必要なため、現在プルリクエストをマージすることはできません");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "すべてのレビュアーから承認を得た後にのみプルリクエストをマージできます");
		m.put("Pull request can only be merged by users with code write permission", "コード書き込み権限を持つユーザーのみがプルリクエストをマージできます");
		m.put("Pull request discard", "プルリクエストの破棄");
		m.put("Pull request duration statistics", "プルリクエスト期間統計");
		m.put("Pull request frequency statistics", "プルリクエスト頻度統計");
		m.put("Pull request is discarded", "プルリクエストが破棄されました");
		m.put("Pull request is in error: {0}", "プルリクエストにエラーがあります: {0}");
		m.put("Pull request is merged", "プルリクエストがマージされました");
		m.put("Pull request is opened", "プルリクエストがオープンされました");
		m.put("Pull request is still a work in progress", "プルリクエストはまだ作業中です");
		m.put("Pull request is work in progress", "プルリクエストは作業中です");
		m.put("Pull request list", "プルリクエスト一覧");
		m.put("Pull request merge", "プルリクエストのマージ");
		m.put("Pull request not exist or access denied", "プルリクエストが存在しないか、アクセスが拒否されました");
		m.put("Pull request not merged", "プルリクエストがマージされていません");
		m.put("Pull request number", "プルリクエスト番号");
		m.put("Pull request open or update", "プルリクエストのオープンまたは更新");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"プルリクエストクエリウォッチは新しいプルリクエストにのみ影響します。既存のプルリクエストのウォッチステータスを一括管理するには、プルリクエストページでウォッチステータスでフィルタリングし、適切なアクションを実行してください");
		m.put("Pull request settings updated", "プルリクエスト設定が更新されました");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"プルリクエスト統計はエンタープライズ機能です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料でお試しください</a>");
		m.put("Pull request synchronization submitted", "プルリクエスト同期が送信されました");
		m.put("Pull request will be merged automatically when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"プルリクエストは準備が整ったら自動的にマージされます。このオプションは、新しいコミットの追加、マージ戦略の変更、またはターゲットブランチの切り替え時に無効になります");
		m.put("Pull request will be merged automatically with a preset <a wicket:id=\"commitMessage\">commit message</a> when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"プルリクエストは準備が整ったらプリセットされた<a wicket:id=\"commitMessage\">コミットメッセージ</a>で自動的にマージされます。このオプションは、新しいコミットの追加、マージ戦略の変更、またはターゲットブランチの切り替え時に無効になります");
		m.put("Push Image", "イメージをプッシュ");
		m.put("Push chart to the repository", "リポジトリにチャートをプッシュ");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"OCIレイアウトからクレーンを介してDockerイメージをプッシュ。このステップは、サーバードッカーエグゼキュータ、リモートドッカーエグゼキュータ、またはKubernetesエグゼキュータによって実行される必要があります");
		m.put("Push to Remote", "リモートにプッシュ");
		m.put("Push to container registry", "コンテナレジストリにプッシュ");
		m.put("PyPI(s)", "PyPI(s)");
		m.put("Pylint Report", "Pylintレポート");
		m.put("Queries", "クエリ");
		m.put("Query", "クエリ");
		m.put("Query Parameters", "クエリパラメータ");
		m.put("Query Watches", "クエリウォッチ");
		m.put("Query commits", "クエリコミット");
		m.put("Query not submitted", "クエリが送信されていません");
		m.put("Query param", "クエリパラメータ");
		m.put("Query/order agents", "クエリ/注文エージェント");
		m.put("Query/order builds", "クエリ/注文ビルド");
		m.put("Query/order comments", "クエリ/注文コメント");
		m.put("Query/order issues", "クエリ/注文課題");
		m.put("Query/order packages", "クエリ/注文パッケージ");
		m.put("Query/order projects", "クエリ/注文プロジェクト");
		m.put("Query/order pull requests", "クエリ/注文プルリクエスト");
		m.put("Queueing Takes", "キューイング時間");
		m.put("Quick Search", "クイック検索");
		m.put("Quote", "引用");
		m.put("RESTful API", "RESTful API");
		m.put("RESTful API Help", "RESTful APIヘルプ");
		m.put("Ran On Agent", "エージェントで実行");
		m.put("Re-run All Queried Builds", "クエリされたすべてのビルドを再実行");
		m.put("Re-run Selected Builds", "選択されたビルドを再実行");
		m.put("Re-run request submitted", "再実行リクエストが送信されました");
		m.put("Re-run this build", "このビルドを再実行");
		m.put("Read", "読み取り");
		m.put("Read body", "本文を読み取る");
		m.put("Readiness Check Command", "準備チェックコマンド");
		m.put("Really want to delete this code comment?", "本当にこのコードコメントを削除しますか？");
		m.put("Rebase", "リベース");
		m.put("Rebase Source Branch Commits", "ソースブランチのコミットをリベース");
		m.put("Rebase all commits from source branch onto target branch", "ソースブランチのすべてのコミットをターゲットブランチにリベース");
		m.put("Rebase source branch commits", "ソースブランチのコミットをリベース");
		m.put("Rebuild manually", "手動で再構築");
		m.put("Receive Posted Email", "投稿されたメールを受信");
		m.put("Received test mail", "テストメールを受信");
		m.put("Receivers", "受信者");
		m.put("Recovery code", "リカバリーコード");
		m.put("Recursive", "再帰的");
		m.put("Redundant", "冗長");
		m.put("Ref", "参照");
		m.put("Ref Name", "参照名");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup", 
			"この<a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>チュートリアル</a>を参照してセットアップ例をご覧ください");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> for an example setup", 
			"この<a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>チュートリアル</a>を参照してセットアップ例をご覧ください");
		m.put("Reference", "参照");
		m.put("Reference Build", "参照ビルド");
		m.put("Reference Issue", "参照課題");
		m.put("Reference Pull Request", "参照プルリクエスト");
		m.put("Reference this {0} in markdown or commit message via below string.", "以下の文字列を使用してMarkdownまたはコミットメッセージでこの{0}を参照してください。");
		m.put("Refresh", "更新");
		m.put("Refresh Token", "更新トークン");
		m.put("Refs", "参照");
		m.put("Regenerate", "再生成");
		m.put("Regenerate Private Key", "秘密鍵を再生成");
		m.put("Regenerate this access token", "このアクセストークンを再生成");
		m.put("Registry Login", "レジストリログイン");
		m.put("Registry Logins", "レジストリログイン");
		m.put("Registry Url", "レジストリURL");
		m.put("Regular Expression", "正規表現");
		m.put("Remaining User Months", "残りのユーザーモンス");
		m.put("Remaining User Months:", "残りのユーザーモンス:");
		m.put("Remaining time", "残り時間");
		m.put("Remember Me", "ログイン状態を保持する");
		m.put("Remote Docker Executor", "リモートドッカーエグゼキュータ");
		m.put("Remote Machine", "リモートマシン");
		m.put("Remote Shell Executor", "リモートシェルエグゼキュータ");
		m.put("Remote URL", "リモートURL");
		m.put("Remote Url", "リモートURL");
		m.put("Remove", "削除");
		m.put("Remove All Queried Agents", "クエリされたすべてのエージェントを削除");
		m.put("Remove All Queried Users from Group", "クエリされたすべてのユーザーをグループから削除");
		m.put("Remove Fields", "フィールドを削除");
		m.put("Remove From Current Iteration", "現在のイテレーションから削除");
		m.put("Remove Selected Agents", "選択されたエージェントを削除");
		m.put("Remove Selected Users from Group", "選択されたユーザーをグループから削除");
		m.put("Remove from All Queried Groups", "クエリされたすべてのグループから削除");
		m.put("Remove from Selected Groups", "選択されたグループから削除");
		m.put("Remove from batch", "バッチから削除");
		m.put("Remove issue from this iteration", "このイテレーションから課題を削除");
		m.put("Remove this assignee", "この担当者を削除");
		m.put("Remove this external participant from issue", "課題からこの外部参加者を削除");
		m.put("Remove this file", "このファイルを削除");
		m.put("Remove this image", "この画像を削除");
		m.put("Remove this reviewer", "このレビュアーを削除");
		m.put("Removed all queried agents. Type <code>yes</code> below to confirm", "すべてのクエリ済みエージェントを削除しました。確認するには下に<code>yes</code>と入力してください");
		m.put("Removed selected agents. Type <code>yes</code> below to confirm", "選択したエージェントを削除しました。確認するには下に<code>yes</code>と入力してください");
		m.put("Rename {0}", "{0}を名前変更");
		m.put("Renew Subscription", "サブスクリプションを更新");
		m.put("Renovate CLI Options", "Renovate CLI オプション");
		m.put("Renovate JavaScript Config", "Renovate JavaScript 設定");
		m.put("Reopen", "再オープン");
		m.put("Reopen this iteration", "このイテレーションを再オープン");
		m.put("Reopened pull request \"{0}\" ({1})", "プルリクエスト\"{0}\"({1})を再オープンしました");
		m.put("Replace With", "置き換え");
		m.put("Replica Count", "レプリカ数");
		m.put("Replicas", "レプリカ");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "プロジェクト\"{1}\"のファイル\"{0}\"に対するコメントに返信しました");
		m.put("Reply", "返信");
		m.put("Report Name", "レポート名");
		m.put("Report format changed. You may re-run this build to generate the report in new format", 
			"レポート形式が変更されました。このビルドを再実行して新しい形式でレポートを生成できます");
		m.put("Repository Sync", "リポジトリ同期");
		m.put("Request Body", "リクエストボディ");
		m.put("Request For Changes", "変更のリクエスト");
		m.put("Request Scopes", "リクエストスコープ");
		m.put("Request Trial Subscription", "試用版サブスクリプションをリクエスト");
		m.put("Request review", "レビューをリクエスト");
		m.put("Request to sync", "同期をリクエスト");
		m.put("Requested For changes", "変更をリクエストしました");
		m.put("Requested changes to pull request \"{0}\" ({1})", "プルリクエスト\"{0}\"({1})に変更をリクエストしました");
		m.put("Requested for changes", "変更をリクエストしました");
		m.put("Requested to sync estimated/spent time", "推定/使用時間の同期をリクエストしました");
		m.put("Require Autentication", "認証が必要");
		m.put("Require Strict Pull Request Builds", "厳密なプルリクエストビルドが必要");
		m.put("Require Successful", "成功が必要");
		m.put("Required", "必須");
		m.put("Required Builds", "必要なビルド");
		m.put("Required Reviewers", "必要なレビュアー");
		m.put("Required Services", "必要なサービス");
		m.put("Resend Verification Email", "確認メールを再送信");
		m.put("Resend invitation", "招待を再送信");
		m.put("Reset", "リセット");
		m.put("Resolution", "解決");
		m.put("Resolved", "解決済み");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "プロジェクト\"{1}\"のファイル\"{0}\"に対するコメントを解決しました");
		m.put("Resource", "リソース");
		m.put("Resource Settings", "リソース設定");
		m.put("Resources", "リソース");
		m.put("Response", "レスポンス");
		m.put("Response Body", "レスポンスボディ");
		m.put("Restart", "再起動");
		m.put("Restart All Queried Agents", "すべてのクエリ済みエージェントを再起動");
		m.put("Restart Selected Agents", "選択したエージェントを再起動");
		m.put("Restart command issued", "再起動コマンドが発行されました");
		m.put("Restart command issued to all queried agents", "すべてのクエリ済みエージェントに再起動コマンドが発行されました");
		m.put("Restart command issued to selected agents", "選択したエージェントに再起動コマンドが発行されました");
		m.put("Restore Source Branch", "ソースブランチを復元");
		m.put("Restored source branch", "ソースブランチを復元しました");
		m.put("Resubmitted manually", "手動で再送信しました");
		m.put("Resume", "再開");
		m.put("Resume All Queried Agents", "すべてのクエリ済みエージェントを再開");
		m.put("Resume Selected Agents", "選択したエージェントを再開");
		m.put("Resumed all queried agents", "すべてのクエリ済みエージェントを再開しました");
		m.put("Resumed selected agents", "選択したエージェントを再開しました");
		m.put("Retried At", "再試行日時");
		m.put("Retrieve Groups", "グループを取得");
		m.put("Retrieve LFS Files", "LFSファイルを取得");
		m.put("Retrieve Submodules", "サブモジュールを取得");
		m.put("Retry Condition", "再試行条件");
		m.put("Retry Delay", "再試行遅延");
		m.put("Revert", "元に戻す");
		m.put("Reverted successfully", "正常に元に戻しました");
		m.put("Review required for deletion. Submit pull request instead", "削除にはレビューが必要です。代わりにプルリクエストを送信してください");
		m.put("Review required for this change. Please submit pull request instead", "この変更にはレビューが必要です。代わりにプルリクエストを提出してください。");
		m.put("Review required for this change. Submit pull request instead", "この変更にはレビューが必要です。代わりにプルリクエストを送信してください");
		m.put("Reviewers", "レビュアー");
		m.put("Revision", "リビジョン");
		m.put("Revision indexing in progress...", "リビジョンのインデックス作成中...");
		m.put("Revision indexing in progress... (symbol navigation in revisions will be accurate after indexed)", 
			"リビジョンのインデックス作成中...(リビジョン内のシンボルナビゲーションはインデックス作成後に正確になります)");
		m.put("Right", "右");
		m.put("Role", "役割");
		m.put("Role \"{0}\" deleted", "役割\"{0}\"が削除されました");
		m.put("Role \"{0}\" updated", "役割\"{0}\"が更新されました");
		m.put("Role Management", "役割管理");
		m.put("Role created", "役割が作成されました");
		m.put("Roles", "役割");
		m.put("Root Projects", "ルートプロジェクト");
		m.put("Roslynator Report", "Roslynator レポート");
		m.put("RubyGems(s)", "RubyGems(s)");
		m.put("Ruff Report", "Ruff レポート");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "タグを操作するユーザーがここで指定された条件に一致する場合にルールが適用されます");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"ブランチを変更するユーザーがここで指定された条件に一致する場合にのみルールが適用されます");
		m.put("Run As", "として実行");
		m.put("Run Buildx Image Tools", "Buildx イメージツールを実行");
		m.put("Run Docker Container", "Docker コンテナを実行");
		m.put("Run In Container", "コンテナ内で実行");
		m.put("Run Integrity Check", "整合性チェックを実行");
		m.put("Run Job", "ジョブを実行");
		m.put("Run Options", "オプションを実行");
		m.put("Run below commands from within your git repository:", "Gitリポジトリ内で以下のコマンドを実行してください:");
		m.put("Run below commands to install this gem", "このGemをインストールするために以下のコマンドを実行してください");
		m.put("Run below commands to install this package", "このパッケージをインストールするために以下のコマンドを実行してください");
		m.put("Run below commands to use this chart", "このチャートを使用するために以下のコマンドを実行してください");
		m.put("Run below commands to use this package", "このパッケージを使用するために以下のコマンドを実行してください");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"指定された引数でDocker Buildx Imagetoolsコマンドを実行します。このステップはサーバーDockerエグゼキューターまたはリモートDockerエグゼキューターによってのみ実行可能です");
		m.put("Run job", "ジョブを実行");
		m.put("Run job in another project", "別のプロジェクトでジョブを実行する");
		m.put("Run on Bare Metal/Virtual Machine", "ベアメタル/仮想マシンで実行");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"さまざまな<a href='https://deps.dev/' target='_blank'>依存関係</a>で使用されている違反ライセンスをスキャンするためにOSVスキャナーを実行します。Docker対応エグゼキューターによってのみ実行可能です");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"さまざまな<a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>ロックファイル</a>の脆弱性をスキャンするためにOSVスキャナーを実行します。Docker対応エグゼキューターによってのみ実行可能です");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"指定されたDockerコンテナを実行します。<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>がコンテナにマウントされ、そのパスが環境変数<code>ONEDEV_WORKSPACE</code>に配置されます。<b class='text-warning'>注意: </b>このステップはサーバーDockerエグゼキューターまたはリモートDockerエグゼキューターによってのみ実行可能です");
		m.put("Run specified step template", "指定されたステップテンプレートを実行");
		m.put("Run this job", "このジョブを実行");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by docker aware executor.", 
			"指定されたイメージの問題を見つけるためにtrivyコンテナイメージスキャナーを実行します。脆弱性については、さまざまな<a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>配布ファイル</a>をチェックします。これはdocker対応のエグゼキュータでのみ実行可能です。");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"Trivyファイルシステムスキャナーを実行して、さまざまな<a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>ロックファイル</a>をスキャンします。Docker対応のエグゼキューターでのみ実行可能であり、<span class='text-warning'>依存関係が解決された後</span>（npm installなど）に実行することを推奨します。OSVスキャナーと比較してセットアップが少し冗長ですが、より正確な結果を提供できます。");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"Trivy rootfsスキャナーを実行して、さまざまな<a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>配布ファイル</a>をスキャンします。Docker対応のエグゼキューターでのみ実行可能であり、プロジェクトのステージングエリアに対して実行することを推奨します。");
		m.put("Run via Docker Container", "Dockerコンテナ経由で実行");
		m.put("Running", "実行中");
		m.put("Running Takes", "実行時間");
		m.put("SLOC on {0}", "{0}のSLOC");
		m.put("SMTP Host", "SMTPホスト");
		m.put("SMTP Password", "SMTPパスワード");
		m.put("SMTP User", "SMTPユーザー");
		m.put("SMTP/IMAP", "SMTP/IMAP");
		m.put("SSH", "SSH");
		m.put("SSH & GPG Keys", "SSH & GPGキー");
		m.put("SSH Clone URL", "SSHクローンURL");
		m.put("SSH Keys", "SSHキー");
		m.put("SSH Root URL", "SSHルートURL");
		m.put("SSH Server Key", "SSHサーバーキー");
		m.put("SSH key deleted", "SSHキーが削除されました");
		m.put("SSH settings have been saved and SSH server restarted", "SSH設定が保存され、SSHサーバーが再起動されました");
		m.put("SSL Setting", "SSL設定");
		m.put("SSO Accounts", "SSOアカウント");
		m.put("SSO Providers", "SSOプロバイダー");
		m.put("SSO account deleted", "SSOアカウントが削除されました");
		m.put("SSO provider \"{0}\" deleted", "SSOプロバイダー\"{0}\"が削除されました");
		m.put("SSO provider created", "SSOプロバイダーが作成されました");
		m.put("SSO provider updated", "SSOプロバイダーが更新されました");
		m.put("SUCCESSFUL", "成功");
		m.put("Save", "保存");
		m.put("Save Query", "クエリを保存");
		m.put("Save Query Bean", "クエリBeanを保存");
		m.put("Save Settings", "設定を保存");
		m.put("Save Settings & Redistribute Projects", "設定を保存してプロジェクトを再配布");
		m.put("Save Template", "テンプレートを保存");
		m.put("Save as Mine", "自分用として保存");
		m.put("Saved Queries", "保存されたクエリ");
		m.put("Scan Path", "スキャンパス");
		m.put("Scan Paths", "スキャンパス");
		m.put("Scan below QR code with your TOTP authenticators", "以下のQRコードをTOTP認証アプリでスキャンしてください");
		m.put("Schedule Issues", "課題をスケジュール");
		m.put("Script Name", "スクリプト名");
		m.put("Scripting Value", "スクリプト値");
		m.put("Search", "検索");
		m.put("Search For", "検索対象");
		m.put("Search Groups Using Filter", "フィルターを使用してグループを検索");
		m.put("Search branch", "ブランチを検索");
		m.put("Search files, symbols and texts", "ファイル、シンボル、テキストを検索");
		m.put("Search for", "検索対象");
		m.put("Search inside current tree", "現在のツリー内を検索");
		m.put("Search is too general", "検索が一般的すぎます");
		m.put("Search job", "ジョブを検索");
		m.put("Search project", "プロジェクトを検索");
		m.put("Secret", "シークレット");
		m.put("Secret Config File", "シークレット設定ファイル");
		m.put("Secret Setting", "シークレット設定");
		m.put("Security", "セキュリティ");
		m.put("Security & Compliance", "セキュリティとコンプライアンス");
		m.put("Security Setting", "セキュリティ設定");
		m.put("Security Settings", "セキュリティ設定");
		m.put("Security settings have been updated", "セキュリティ設定が更新されました");
		m.put("Select", "選択");
		m.put("Select Branch to Cherry Pick to", "チェリーピック先のブランチを選択");
		m.put("Select Branch to Revert on", "リバート先のブランチを選択");
		m.put("Select Branch/Tag", "ブランチ/タグを選択");
		m.put("Select Existing", "既存のものを選択");
		m.put("Select Job", "ジョブを選択");
		m.put("Select Project", "プロジェクトを選択");
		m.put("Select below...", "以下を選択...");
		m.put("Select iteration to schedule issues into", "課題をスケジュールするためのイテレーションを選択");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"インポート元の組織を選択してください。空欄の場合、現在のアカウント下のリポジトリからインポートされます");
		m.put("Select project and revision first", "まずプロジェクトとリビジョンを選択してください");
		m.put("Select project first", "まずプロジェクトを選択してください");
		m.put("Select project to import from", "インポート元のプロジェクトを選択");
		m.put("Select project to sync to. Leave empty to sync to current project", "同期先のプロジェクトを選択してください。空欄の場合、現在のプロジェクトに同期されます");
		m.put("Select repository to import from", "インポート元のリポジトリを選択");
		m.put("Select users to send alert email upon events such as database auto-backup failure, cluster node unreachable etc", 
			"データベースの自動バックアップ失敗やクラスターノードの到達不能などのイベント時にアラートメールを送信するユーザーを選択");
		m.put("Select workspace to import from", "インポート元のワークスペースを選択");
		m.put("Send Notifications", "通知を送信");
		m.put("Send Pull Request", "プルリクエストを送信");
		m.put("Send notification", "通知を送信");
		m.put("SendGrid", "SendGrid");
		m.put("Sendgrid Webhook Setting", "SendGrid Webhook設定");
		m.put("Sending invitation to \"{0}\"...", "\"{0}\"への招待を送信中...");
		m.put("Sending test mail to {0}...", "{0}へのテストメールを送信中...");
		m.put("Sequential Group", "シーケンシャルグループ");
		m.put("Server", "サーバー");
		m.put("Server Docker Executor", "サーバーDockerエグゼキューター");
		m.put("Server Id", "サーバーID");
		m.put("Server Information", "サーバー情報");
		m.put("Server Log", "サーバーログ");
		m.put("Server Setup", "サーバーセットアップ");
		m.put("Server Shell Executor", "サーバーシェルエグゼキューター");
		m.put("Server URL", "サーバーURL");
		m.put("Server fingerprint", "サーバーフィンガープリント");
		m.put("Server host", "サーバーホスト");
		m.put("Server is Starting...", "サーバーを起動中...");
		m.put("Server url", "サーバーURL");
		m.put("Service", "サービス");
		m.put("Service Account", "サービスアカウント");
		m.put("Service Desk", "サービスデスク");
		m.put("Service Desk Email Address", "サービスデスクメールアドレス");
		m.put("Service Desk Issue Open Failed", "サービスデスク課題のオープンに失敗しました");
		m.put("Service Desk Issue Open Failed Template", "サービスデスク課題オープン失敗テンプレート");
		m.put("Service Desk Issue Opened", "サービスデスク課題がオープンされました");
		m.put("Service Desk Issue Opened Template", "サービスデスク課題オープンテンプレート");
		m.put("Service Desk Setting", "サービスデスク設定");
		m.put("Service Desk Setting Holder", "サービスデスク設定ホルダー");
		m.put("Service Desk Settings", "サービスデスク設定");
		m.put("Service Locator", "サービスロケーター");
		m.put("Service Locators", "サービスロケーター");
		m.put("Service account not allowed to login", "サービスアカウントはログインできません");
		m.put("Service desk setting", "サービスデスク設定");
		m.put("Service desk settings have been saved", "サービスデスク設定が保存されました");
		m.put("Services", "サービス");
		m.put("Session Timeout", "セッションタイムアウト");
		m.put("Set", "設定");
		m.put("Set All Queried As Root Projects", "すべてのクエリをルートプロジェクトとして設定");
		m.put("Set All Queried Comments as Read", "すべてのクエリされたコメントを既読として設定");
		m.put("Set All Queried Comments as Resolved", "すべてのクエリされたコメントを解決済みとして設定");
		m.put("Set All Queried Comments as Unresolved", "すべてのクエリされたコメントを未解決として設定");
		m.put("Set All Queried Issues as Read", "すべてのクエリされた課題を既読として設定");
		m.put("Set All Queried Pull Requests as Read", "すべてのクエリされたプルリクエストを既読として設定");
		m.put("Set As Primary", "プライマリとして設定");
		m.put("Set Build Description", "ビルド説明を設定");
		m.put("Set Build Version", "ビルドバージョンを設定");
		m.put("Set Resolved", "解決済みとして設定");
		m.put("Set Selected As Root Projects", "選択されたものをルートプロジェクトとして設定");
		m.put("Set Selected Comments as Resolved", "選択されたコメントを解決済みとして設定");
		m.put("Set Selected Comments as Unresolved", "選択されたコメントを未解決として設定");
		m.put("Set Unresolved", "未解決として設定");
		m.put("Set Up Cache", "キャッシュを設定");
		m.put("Set Up Renovate Cache", "Renovateキャッシュを設定");
		m.put("Set Up Trivy Cache", "Trivyキャッシュを設定");
		m.put("Set Up Your Account", "アカウントを設定する");
		m.put("Set as Private", "非公開として設定");
		m.put("Set as Public", "公開として設定");
		m.put("Set description", "説明を設定");
		m.put("Set reviewed", "レビュー済みとして設定");
		m.put("Set unreviewed", "未レビューとして設定");
		m.put("Set up Microsoft Teams notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url. ", 
			"Microsoft Teams通知設定を設定します。設定は子プロジェクトに継承され、同じWebhook URLで設定を定義することで上書きできます。");
		m.put("Set up discord notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Discord通知設定を設定します。設定は子プロジェクトに継承され、同じWebhook URLで設定を定義することで上書きできます。");
		m.put("Set up job cache to speed up job execution. Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> on how to use job cache", 
			"ジョブキャッシュを設定してジョブ実行を高速化します。ジョブキャッシュの使用方法については<a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>このチュートリアル</a>を確認してください。");
		m.put("Set up ntfy.sh notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"ntfy.sh通知設定を設定します。設定は子プロジェクトに継承され、同じWebhook URLで設定を定義することで上書きできます。");
		m.put("Set up slack notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Slack通知設定を設定します。設定は子プロジェクトに継承され、同じWebhook URLで設定を定義することで上書きできます。");
		m.put("Set up two-factor authentication", "二要素認証を設定");
		m.put("Setting", "設定");
		m.put("Setting has been saved", "設定が保存されました");
		m.put("Settings", "設定");
		m.put("Settings and permissions of parent project will be inherited by this project", "親プロジェクトの設定と権限がこのプロジェクトに継承されます");
		m.put("Settings saved", "設定を保存しました");
		m.put("Settings saved and project redistribution scheduled", "設定を保存し、プロジェクトの再分配をスケジュールしました");
		m.put("Settings updated", "設定が更新されました");
		m.put("Share dashboard", "ダッシュボードを共有");
		m.put("Share with Groups", "グループと共有");
		m.put("Share with Users", "ユーザーと共有");
		m.put("Shell", "シェル");
		m.put("Show Archived", "アーカイブを表示");
		m.put("Show Branch/Tag", "ブランチ/タグを表示");
		m.put("Show Build Status", "ビルドステータスを表示");
		m.put("Show Closed", "閉じたものを表示");
		m.put("Show Code Stats", "コード統計を表示");
		m.put("Show Command", "コマンドを表示");
		m.put("Show Condition", "条件を表示");
		m.put("Show Conditionally", "条件付きで表示");
		m.put("Show Description", "説明を表示");
		m.put("Show Duration", "期間を表示");
		m.put("Show Emojis", "絵文字を表示");
		m.put("Show Error Detail", "エラー詳細を表示");
		m.put("Show Issue Status", "課題ステータスを表示");
		m.put("Show Package Stats", "パッケージ統計を表示");
		m.put("Show Pull Request Stats", "プルリクエスト統計を表示");
		m.put("Show Saved Queries", "保存されたクエリを表示");
		m.put("Show States By", "状態を表示");
		m.put("Show Works Of", "作業を表示");
		m.put("Show changes", "変更を表示");
		m.put("Show commented code snippet", "コメントされたコードスニペットを表示");
		m.put("Show commit of this parent", "この親のコミットを表示");
		m.put("Show emojis", "絵文字を表示");
		m.put("Show in build list", "ビルドリストに表示");
		m.put("Show issues in list", "リスト内の課題を表示");
		m.put("Show issues not scheduled into current iteration", "現在のイテレーションにスケジュールされていない課題を表示");
		m.put("Show matching agents", "一致するエージェントを表示");
		m.put("Show more", "さらに表示");
		m.put("Show more lines", "さらに多くの行を表示");
		m.put("Show next match", "次の一致を表示");
		m.put("Show previous match", "前の一致を表示");
		m.put("Show test cases of this test suite", "このテストスイートのテストケースを表示");
		m.put("Show total estimated/spent time", "推定/使用時間の合計を表示");
		m.put("Showing first {0} files as there are too many", "ファイルが多すぎるため、最初の{0}ファイルを表示しています");
		m.put("Sign In", "サインイン");
		m.put("Sign In To", "サインイン先");
		m.put("Sign Out", "サインアウト");
		m.put("Sign Up", "サインアップ");
		m.put("Sign Up Bean", "サインアップBean");
		m.put("Sign Up!", "サインアップ！");
		m.put("Sign in", "サインイン");
		m.put("Signature required for this change, but no signing key is specified", "この変更には署名が必要ですが、署名キーが指定されていません。");
		m.put("Signature required for this change, please generate system GPG signing key first", "この変更には署名が必要です。まずシステムGPG署名キーを生成してください。");
		m.put("Signature verified successfully with OneDev GPG key", "OneDev GPGキーで署名が正常に検証されました");
		m.put("Signature verified successfully with committer's GPG key", "コミッターのGPGキーで署名が正常に検証されました");
		m.put("Signature verified successfully with committer's SSH key", "コミッターのSSHキーで署名が正常に検証されました");
		m.put("Signature verified successfully with tagger's GPG key", "タグ付け者のGPGキーで署名が正常に検証されました");
		m.put("Signature verified successfully with tagger's SSH key", "タグ付け者のSSHキーで署名が正常に検証されました");
		m.put("Signature verified successfully with trusted GPG key", "信頼されたGPGキーで署名が正常に検証されました");
		m.put("Signed with an unknown GPG key ", "不明なGPGキーで署名されています");
		m.put("Signed with an unknown ssh key", "未知のsshキーで署名されました");
		m.put("Signer Email Addresses", "署名者のメールアドレス");
		m.put("Signing Key ID", "署名キーID");
		m.put("Similar Issues", "類似の課題");
		m.put("Single Sign On", "シングルサインオン");
		m.put("Single Sign-On", "シングルサインオン");
		m.put("Single sign on via discord.com", "discord.comを介したシングルサインオン");
		m.put("Single sign on via twitch.tv", "twitch.tvを介したシングルサインオン");
		m.put("Site", "サイト");
		m.put("Size", "サイズ");
		m.put("Size invalid", "サイズが無効");
		m.put("Slack Notifications", "Slack通知");
		m.put("Smtp Ssl Setting", "Smtp SSL設定");
		m.put("Smtp With Ssl", "Smtp SSL使用");
		m.put("Some builds are {0}", "一部のビルドは{0}です");
		m.put("Some jobs are hidden due to permission policy", "許可ポリシーにより一部のジョブが非表示になっています");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "編集している内容が誰かによって変更されました。ページをリロードして再試行してください。");
		m.put("Some other pull requests are opening to this branch", "このブランチに対して他のプルリクエストが開かれています");
		m.put("Some projects might be hidden due to permission policy", "許可ポリシーにより一部のプロジェクトが非表示になっている可能性があります");
		m.put("Some related commits of the code comment is missing", "コードコメントに関連する一部のコミットが欠落しています");
		m.put("Some related commits of the pull request are missing", "プルリクエストに関連する一部のコミットが欠落しています");
		m.put("Some required builds not passed", "必要なビルドがいくつか通過していません");
		m.put("Someone made below change since you started editing", "編集を開始してから以下の変更が行われました。");
		m.put("Sort", "ソート");
		m.put("Source", "ソース");
		m.put("Source Docker Image", "ソース Docker イメージ");
		m.put("Source Lines", "ソース行");
		m.put("Source Path", "ソースパス");
		m.put("Source branch already exists", "ソースブランチはすでに存在します");
		m.put("Source branch already merged into target branch", "ソースブランチはターゲットブランチにすでにマージされています");
		m.put("Source branch commits will be rebased onto target branch", "ソースブランチのコミットはターゲットブランチにリベースされます");
		m.put("Source branch is default branch", "ソースブランチはデフォルトブランチです");
		m.put("Source branch is outdated", "ソースブランチは古くなっています");
		m.put("Source branch no longer exists", "ソースブランチはもう存在しません");
		m.put("Source branch updated successfully", "ソースブランチは正常に更新されました");
		m.put("Source project no longer exists", "ソースプロジェクトはもう存在しません");
		m.put("Specified Value", "指定された値");
		m.put("Specified choices", "指定された選択肢");
		m.put("Specified default value", "指定されたデフォルト値");
		m.put("Specified fields", "指定されたフィールド");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Active Directory サーバーの LDAP URL を指定します。例: <i>ldap://ad-server</i> または <i>ldaps://ad-server</i>。LDAP サーバーが自己署名証明書を使用して ldaps 接続を行う場合、<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>OneDev を構成して証明書を信頼するように設定する必要があります</a>");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"LDAP URL を指定します。例: <i>ldap://localhost</i> または <i>ldaps://localhost</i>。LDAP サーバーが自己署名証明書を使用して ldaps 接続を行う場合、<a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>OneDev を構成して証明書を信頼するように設定する必要があります</a>");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"ユーザー検索のベースノードを指定します。例: <i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"ユーザー LDAP エントリ内の属性名を指定します。この属性の値には所属グループの識別名が含まれます。例えば、いくつかの LDAP サーバーでは属性 <i>memberOf</i> を使用してグループをリストします");
		m.put("Specifies password of above manager DN", "上記のマネージャー DN のパスワードを指定します");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"見つかったグループ LDAP エントリ内のグループ名を含む属性を指定します。この属性の値は OneDev グループにマッピングされます。この属性は通常 <i>cn</i> に設定されます");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する .net TRX テスト結果ファイルを指定します。例: <tt>TestResults/*.trx</tt>。パターンマッチには * または ? を使用してください");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"<a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a>を指定します。この値はコード書き込み権限を持つアクセス トークンです。コミット、課題、プルリクエストはアクセス トークン所有者の名前で作成されます");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"<a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> の JSON 出力ファイルを <a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対して指定します。このファイルは clippy の JSON 出力オプションで生成できます。例: <code>cargo clippy --message-format json>check-result.json</code>。パターンマッチには * または ? を使用してください");
		m.put("Specify Build Options", "ビルドオプションを指定します");
		m.put("Specify CPD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する CPD 結果 XML ファイルを指定します。例: <tt>target/cpd.xml</tt>。パターンマッチには * または ? を使用してください");
		m.put("Specify Commit Message", "コミットメッセージを指定します");
		m.put("Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>内の checkstyle フォーマットで ESLint レポートファイルを指定します。このファイルは ESLint オプション <tt>'-f checkstyle'</tt> および <tt>'-o'</tt> を使用して生成できます。パターンマッチには * または ? を使用してください");
		m.put("Specify GitHub API url, for instance <tt>https://api.github.com</tt>", "GitHub API URL を指定します。例: <tt>https://api.github.com</tt>");
		m.put("Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>", "GitLab API URL を指定します。例: <tt>https://gitlab.example.com/api/v4</tt>");
		m.put("Specify Gitea API url, for instance <tt>https://gitea.example.com/api/v1</tt>", "Gitea API URL を指定します。例: <tt>https://gitea.example.com/api/v1</tt>");
		m.put("Specify GoogleTest XML result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This report can be generated with environment variable <tt>GTEST_OUTPUT</tt> when running tests, For instance, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する GoogleTest XML 結果ファイルを指定します。このレポートはテスト実行時に環境変数 <tt>GTEST_OUTPUT</tt> を使用して生成できます。例: <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>。パターンマッチには * または ? を使用してください");
		m.put("Specify IMAP user name.<br><b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system email address specified above", 
			"IMAP ユーザー名を指定します。<br><b class='text-danger'>注意: </b>このアカウントは上記で指定されたシステムメールアドレスに送信されたメールを受信できる必要があります");
		m.put("Specify JUnit test result file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する JUnit テスト結果ファイル (XML フォーマット) を指定します。例: <tt>target/surefire-reports/TEST-*.xml</tt>。パターンマッチには * または ? を使用してください");
		m.put("Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する JaCoCo カバレッジ XML レポートファイルを指定します。例: <tt>target/site/jacoco/jacoco.xml</tt>。パターンマッチには * または ? を使用してください");
		m.put("Specify Jest coverage report file in clover format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する Jest カバレッジレポートファイル (clover フォーマット) を指定します。例: <tt>coverage/clover.xml</tt>。このファイルは Jest オプション <tt>'--coverage'</tt> を使用して生成できます。パターンマッチには * または ? を使用してください");
		m.put("Specify Jest test result file in json format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する Jest テスト結果ファイル (JSON フォーマット) を指定します。このファイルは Jest オプション <tt>'--json'</tt> および <tt>'--outputFile'</tt> を使用して生成できます。パターンマッチには * または ? を使用してください");
		m.put("Specify OCI layout directory of the image to scan. This directory can be generated via build image step or pull image step. It should be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"スキャン対象のイメージの OCI レイアウトディレクトリを指定します。このディレクトリはイメージビルドステップまたはイメージプルステップで生成できます。<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対して相対的である必要があります");
		m.put("Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from", 
			"プッシュ元として使用する OCI レイアウトディレクトリを <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対して指定します");
		m.put("Specify OpenID scopes to request", "要求する OpenID スコープを指定します");
		m.put("Specify PMD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/pmd.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する PMD 結果 XML ファイルを指定します。例: <tt>target/pmd.xml</tt>。パターンマッチには * または ? を使用してください");
		m.put("Specify PowerShell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of your script<br>", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>内で実行する PowerShell コマンドを指定します。<br><b class='text-warning'>注意: </b>OneDev はスクリプトの終了コードをチェックしてステップが成功したかどうかを判断します。PowerShell はスクリプトエラーがあっても常に 0 を返すため、スクリプト内でエラーを処理して非ゼロコードで終了するか、スクリプトの冒頭に <code>$ErrorActionPreference = &quot;Stop&quot;</code> を追加してください<br>");
		m.put("Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with <i>-o</i> option. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する Roslynator 診断出力ファイル (XML フォーマット) を指定します。このファイルは <i>-o</i> オプションで生成できます。パターンマッチには * または ? を使用してください");
		m.put("Specify Shell/Batch Commands to Run", "実行するシェル/バッチコマンドを指定します");
		m.put("Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する SpotBugs 結果 XML ファイルを指定します。例: <tt>target/spotbugsXml.xml</tt>。パターンマッチには * または ? を使用してください");
		m.put("Specify System Settings", "システム設定を指定します");
		m.put("Specify URL of remote git repository. Only http/https protocol is supported", "リモート Git リポジトリの URL を指定します。http/https プロトコルのみサポートされています");
		m.put("Specify YouTrack login name. This account should have permission to:<ul><li>Read full information and issues of the projects you want to import<li>Read issue tags<li>Read user basic information</ul>", 
			"YouTrack のログイン名を指定します。このアカウントは以下の権限を持つ必要があります:<ul><li>インポートしたいプロジェクトの完全な情報と課題を読む権限<li>課題タグを読む権限<li>ユーザー基本情報を読む権限</ul>");
		m.put("Specify YouTrack password or access token for above user", "上記ユーザーの YouTrack パスワードまたはアクセス トークンを指定します");
		m.put("Specify a &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regular expression&lt;/a&gt; to match issue references. For instance:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;", 
			"課題参照を一致させるための &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;正規表現&lt;/a&gt; を指定します。例:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> after issue number", 
			"課題番号の後に一致する <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>正規表現</a> を指定します");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> before issue number", 
			"課題番号の前に一致する <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>正規表現</a> を指定します");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key", 
			"SSH プライベートキーとして使用する <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"アクセス トークンとして使用する <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"上記プロジェクトのビルド仕様をインポートするために使用するアクセス トークンとして <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します。このプロジェクトのコードが公開されていない場合に使用します");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"レジストリのパスワードまたはアクセス トークンとして使用する <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"リモートリポジトリにアクセスするためのパスワードまたはアクセス トークンとして使用する <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"SSH 認証用のプライベートキーとして使用する <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します。<b class='text-info'>注意:</b> パスフレーズ付きのプライベートキーはサポートされていません");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"上記ユーザーの SSH 認証用プライベートキーとして使用する <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します。<b class='text-info'>注意:</b> パスフレーズ付きのプライベートキーはサポートされていません");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"上記プロジェクトの管理権限を持つアクセス トークンとして使用する <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します。現在または子プロジェクトに同期し、ビルドコミットがデフォルトブランチから到達可能な場合はアクセス トークンは不要です");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"上記プロジェクトのキャッシュアップロード権限を持つアクセス トークンとして使用する <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>ジョブシークレット</a> を指定します。現在または子プロジェクトにキャッシュをアップロードし、ビルドコミットがデフォルトブランチから到達可能な場合、このプロパティは不要です");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"ジョブを自動的に実行するための <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron スケジュール</a> を指定します。<b class='text-info'>注意:</b> リソースを節約するため、cron 式の秒は無視され、最小スケジュール間隔は 1 分です");
		m.put("Specify a Docker Image to Test Against", "テスト対象の Docker イメージを指定します");
		m.put("Specify a custom field of Enum type", "Enum 型のカスタムフィールドを指定します");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "指定されたジョブの修正済み課題をフィルタリング/並べ替えるためのデフォルトクエリを指定します");
		m.put("Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into", 
			"チェックサムを書き込むためのファイルを <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対して指定します");
		m.put("Specify a multi-value user field to hold assignees information.<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"担当者情報を保持するための複数値ユーザーフィールドを指定します。<b>注意: </b>適切なオプションがない場合は OneDev の課題フィールドをカスタマイズすることができます");
		m.put("Specify a multi-value user field to hold assignees information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"担当者情報を保持するための複数値ユーザーフィールドを指定します。<br><b>注意: </b>適切なオプションがない場合は OneDev の課題フィールドをカスタマイズすることができます");
		m.put("Specify a path inside container to be used as mount target", "マウントターゲットとして使用するコンテナ内のパスを指定します");
		m.put("Specify a path relative to job workspace to be used as mount source. Leave empty to mount job workspace itself", 
			"マウントソースとして使用するジョブワークスペースに対するパスを指定します。空欄の場合、ジョブワークスペース自体をマウントします");
		m.put("Specify a secret to be used as access token to create issue in above project if it is not publicly accessible", 
			"上記プロジェクトで課題を作成するためのアクセス トークンとして使用するシークレットを指定します。このプロジェクトが公開されていない場合に使用します");
		m.put("Specify a secret to be used as access token to retrieve artifacts from above project. If not specified, project artifacts will be accessed anonymously", 
			"上記プロジェクトからアーティファクトを取得するためのアクセス トークンとして使用するシークレットを指定します。指定されていない場合、プロジェクトアーティファクトは匿名でアクセスされます");
		m.put("Specify a secret to be used as access token to trigger job in above project", "上記のプロジェクトでジョブをトリガーするためのアクセストークンとして使用するシークレットを指定します。");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"上記プロジェクトのキャッシュアップロード権限を持つアクセス トークンとして使用するシークレットを指定します。現在または子プロジェクトにキャッシュをアップロードし、ビルドコミットがデフォルトブランチから到達可能な場合、このプロパティは不要です");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"クラスターにアクセスするために kubectl が使用する設定ファイルの絶対パスを指定します。空欄の場合、kubectl はクラスターアクセス情報を自動的に決定します");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"kubectl ユーティリティの絶対パスを指定します。例: <i>/usr/bin/kubectl</i>。空欄の場合、OneDev はシステムパスからユーティリティを探します");
		m.put("Specify account name to login to Gmail to send/receive email", "Gmail にログインしてメールを送受信するためのアカウント名を指定します");
		m.put("Specify additional users able to access this confidential issue besides those granted via role. Users mentioned in the issue will be authorized automatically", 
			"役割を通じて付与されたユーザー以外でこの機密課題にアクセスできる追加ユーザーを指定します。課題に記載されたユーザーは自動的に承認されます");
		m.put("Specify agents applicable for this executor", "このエグゼキューターに適用可能なエージェントを指定します");
		m.put("Specify allowed <a href='https://spdx.org/licenses/' target='_blank'>spdx license identifiers</a> <span class='text-warning'>separated by comma</span>", 
			"<a href='https://spdx.org/licenses/' target='_blank'>spdx ライセンス識別子</a>を指定します <span class='text-warning'>カンマで区切る</span>");
		m.put("Specify an email address sharing same inbox as the system email address in mail setting definition. Emails sent to this address will be created as issues in this project. The default value takes form of <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>", 
			"メール設定定義のシステムメールアドレスと同じ受信トレイを共有するメールアドレスを指定します。このアドレスに送信されたメールはこのプロジェクトの課題として作成されます。デフォルト値は <tt>&lt;システムメールアドレス名&gt;+&lt;プロジェクトパス&gt;@&lt;システムメールアドレスドメイン&gt;</tt> の形式を取ります");
		m.put("Specify applicable projects for above option. Multiple projects should be separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"上記オプションに適用可能なプロジェクトを指定します。複数のプロジェクトはスペースで区切る必要があります。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には '**', '*' または '?' を使用してください。'-' を付けて除外します。空欄の場合、すべてのプロジェクトが対象となります");
		m.put("Specify applicable projects separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"スペースで区切られた適用可能なプロジェクトを指定します。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には '**', '*' または '?' を使用してください。'-' を付けて除外します。空欄の場合、すべてのプロジェクトが対象となります");
		m.put("Specify application (client) ID of the app registered in Entra ID", "Entra ID に登録されたアプリのアプリケーション (クライアント) ID を指定します");
		m.put("Specify arguments for imagetools. For instance <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>", 
			"イメージツールの引数を指定します。例: <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>");
		m.put("Specify artifacts to retrieve into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved.", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に取得するアーティファクトを指定します。公開されたアーティファクト (アーティファクト公開ステップを通じて) のみ取得可能です");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"少なくとも 10 文字の英数字をシークレットとして指定し、SendGrid 側でインバウンド解析エントリを追加します:<ul><li><code>送信先 URL</code> は <i>&lt;OneDev ルート URL&gt;/~sendgrid/&lt;シークレット&gt;</i> に設定する必要があります。例: <i>https://onedev.example.com/~sendgrid/1234567890</i>。本番環境では、<a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https を有効にする必要があります</a> シークレットを保護するため</li><li><code>受信ドメイン</code> は上記で指定されたシステムメールアドレスのドメイン部分と同じである必要があります</li><li>オプション <code>生の完全な MIME メッセージを POST</code> が有効になっています</li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"ユーザー検索のベースノードを指定します。例: <i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "提案された変更をコミットするブランチを指定します");
		m.put("Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"ジョブを実行するブランチを指定します。ブランチまたはタグのいずれかを指定できますが、両方は指定できません。両方が指定されていない場合はデフォルトのブランチが使用されます。");
		m.put("Specify branch, tag or commit in above project to import build spec from", "上記プロジェクトのビルド仕様をインポートするブランチ、タグ、またはコミットを指定します");
		m.put("Specify by Build Number", "ビルド番号で指定します");
		m.put("Specify cache upload strategy after build successful. <var>Upload If Not Hit</var> means to upload when cache is not found with cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files in cache path are changed", 
			"ビルド成功後のキャッシュアップロード戦略を指定します。<var>ヒットしない場合アップロード</var> はキャッシュキー (ロードキーではない) でキャッシュが見つからない場合にアップロードし、<var>変更された場合アップロード</var> はキャッシュパス内のファイルが変更された場合にアップロードします");
		m.put("Specify certificate to trust if you are using self-signed certificate for remote repository", 
			"リモートリポジトリに自己署名証明書を使用している場合に信頼する証明書を指定します");
		m.put("Specify certificates to trust if you are using self-signed certificates for your docker registries", 
			"Docker レジストリに自己署名証明書を使用している場合に信頼する証明書を指定します");
		m.put("Specify checkstyle result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/checkstyle-result.xml</tt>. Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> on how to generate the result xml file. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する checkstyle 結果 XML ファイルを指定します。例: <tt>target/checkstyle-result.xml</tt>。結果 XML ファイルの生成方法については <a href='https://checkstyle.org/'>checkstyle ドキュメント</a> を参照してください。パターンマッチには * または ? を使用してください");
		m.put("Specify client secret of the app registered in Entra ID", "Entra ID に登録されたアプリのクライアントシークレットを指定します");
		m.put("Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/clover/clover.xml</tt>. Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> on how to generate clover xml file. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する clover カバレッジ XML レポートファイルを指定します。例: <tt>target/site/clover/clover.xml</tt>。clover XML ファイルの生成方法については <a href='https://openclover.org/documentation'>OpenClover ドキュメント</a> を参照してください。パターンマッチには * または ? を使用してください");
		m.put("Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する cobertura カバレッジ XML レポートファイルを指定します。例: <tt>target/site/cobertura/coverage.xml</tt>。パターンマッチには * または ? を使用してください");
		m.put("Specify color of the state for displaying purpose", "表示目的の状態の色を指定します");
		m.put("Specify columns of the board. Each column corresponds to a value of the issue field specified above", 
			"ボードの列を指定します。各列は上記で指定された課題フィールドの値に対応します");
		m.put("Specify command to check readiness of the service. This command will be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be executed repeatedly until a zero code is returned to indicate service ready", 
			"サービスの準備状態を確認するコマンドを指定します。このコマンドは Windows イメージでは cmd.exe によって解釈され、Linux イメージではシェルによって解釈されます。ゼロコードが返されてサービス準備完了を示すまで繰り返し実行されます");
		m.put("Specify commands to be executed on remote machine. <b class='text-warning'>Note:</b> user environments will not be picked up when execute these commands, set up them explicitly in commands if necessary", 
			"リモートマシンで実行するコマンドを指定してください。<b class='text-warning'>注意:</b> ユーザー環境はこれらのコマンドを実行する際に引き継がれないため、必要に応じてコマンド内で明示的に設定してください。");
		m.put("Specify condition to retry build upon failure", "失敗時にビルドを再試行する条件を指定してください。");
		m.put("Specify configuration discovery url of your OpenID provider, for instance: <code>https://openid.example.com/.well-known/openid-configuration</code>. Make sure to use HTTPS protocol as OneDev relies on TLS encryption to ensure token validity", 
			"OpenIDプロバイダーの構成検出URLを指定してください。例: <code>https://openid.example.com/.well-known/openid-configuration</code>。OneDevはトークンの有効性を確保するためにTLS暗号化に依存しているため、HTTPSプロトコルを使用してください。");
		m.put("Specify container image to execute commands inside", "コマンドを実行するためのコンテナイメージを指定してください。");
		m.put("Specify container image to run", "実行するコンテナイメージを指定してください。");
		m.put("Specify cppcheck xml result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対するcppcheckのXML結果ファイルを指定してください。このファイルはcppcheckのXML出力オプションで生成できます。例: <code>cppcheck src --xml 2>check-result.xml</code>。パターンマッチには*や?を使用してください。");
		m.put("Specify cpu request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"このエグゼキューターを使用する各ジョブ/サービスのCPUリクエストを指定してください。詳細は<a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetesリソース管理</a>を確認してください。");
		m.put("Specify default assignees of pull requests submitted to this project. Only users with the write code permission to the project can be selected", 
			"このプロジェクトに提出されたプルリクエストのデフォルトの担当者を指定してください。プロジェクトにコード書き込み権限を持つユーザーのみ選択できます。");
		m.put("Specify default merge strategy of pull requests submitted to this project", "このプロジェクトに提出されたプルリクエストのデフォルトのマージ戦略を指定してください。");
		m.put("Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple destinations should be separated with space", 
			"送信先を指定してください。例: <tt>registry-server:5000/myorg/myrepo:latest</tt>。組み込みレジストリにプッシュする場合、システム設定のサーバーURLで指定された<b>同じホスト</b>を使用するか、単に<tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>の形式を使用してください。複数の送信先はスペースで区切ってください。");
		m.put("Specify directory (tenant) ID of the app registered in Entra ID", "Entra IDに登録されたアプリのディレクトリ（テナント）IDを指定してください。");
		m.put("Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対するOCIレイアウトを保存するディレクトリを指定してください。");
		m.put("Specify docker image of the service", "サービスのDockerイメージを指定してください。");
		m.put("Specify dockerx builder used to build docker image. OneDev will create the builder automatically if it does not exist. Check <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> on how to customize the builder for instance to allow publishing to insecure registries", 
			"Dockerイメージをビルドするために使用するdockerxビルダーを指定してください。OneDevはビルダーが存在しない場合、自動的に作成します。例えば、非セキュアなレジストリへの公開を許可するためにビルダーをカスタマイズする方法については<a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>このチュートリアル</a>を確認してください。");
		m.put("Specify email addresses to send invitations, with one per line", "招待を送るメールアドレスを指定してください。1行につき1つのアドレスを記載してください。");
		m.put("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\"", 
			"<b class='text-warning'>この課題のみ</b>の推定時間を指定してください。\"{0}\"は含まれません。");
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"依存関係の更新を調整するためにRenovateによって作成されたさまざまな課題のフィールドを指定してください。");
		m.put("Specify fields to be displayed in the issue list", "課題リストに表示するフィールドを指定してください。");
		m.put("Specify fields to display in board card", "ボードカードに表示するフィールドを指定してください。");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する公開するファイルを指定してください。パターンマッチには*や?を使用してください。");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"md5チェックサムを作成するファイルを指定してください。複数のファイルはスペースで区切ってください。<a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a>パターンが使用可能です。絶対パスでないファイルは<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対する相対パスとみなされます。");
		m.put("Specify files under above directory to be published", "上記のディレクトリ内で公開するファイルを指定");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be included in these files to be served as site start page", 
			"上記のディレクトリ内で公開するファイルを指定してください。パターンマッチには*や?を使用してください。<b>注意:</b> サイトの開始ページとして提供される<code>index.html</code>をこれらのファイルに含める必要があります。");
		m.put("Specify group to import from. Leave empty to import from projects under current account", 
			"インポート元のグループを指定してください。現在のアカウント下のプロジェクトからインポートする場合は空のままにしてください。");
		m.put("Specify how to map GitHub issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"GitHubの課題ラベルをOneDevのカスタムフィールドにマッピングする方法を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題フィールドをカスタマイズすることができます。");
		m.put("Specify how to map GitLab issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"GitLabの課題ラベルをOneDevのカスタムフィールドにマッピングする方法を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題フィールドをカスタマイズすることができます。");
		m.put("Specify how to map Gitea issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Giteaの課題ラベルをOneDevのカスタムフィールドにマッピングする方法を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題フィールドをカスタマイズすることができます。");
		m.put("Specify how to map JIRA issue priorities to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"JIRAの課題優先度をOneDevのカスタムフィールドにマッピングする方法を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題フィールドをカスタマイズすることができます。");
		m.put("Specify how to map JIRA issue statuses to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"JIRAの課題ステータスをOneDevのカスタムフィールドにマッピングする方法を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題ステートをカスタマイズすることができます。");
		m.put("Specify how to map JIRA issue types to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"JIRAの課題タイプをOneDevのカスタムフィールドにマッピングする方法を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題フィールドをカスタマイズすることができます。");
		m.put("Specify how to map YouTrack issue fields to OneDev. Unmapped fields will be reflected in issue description.<br><b>Note: </b><ul><li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, for instance <tt>Priority::Critical</tt><li>You may customize OneDev issue fields in case there is no appropriate option here</ul>", 
			"YouTrackの課題フィールドをOneDevにマッピングする方法を指定してください。マッピングされていないフィールドは課題の説明に反映されます。<br><b>注意:</b><ul><li>列挙型フィールドは<tt>&lt;フィールド名&gt;::&lt;フィールド値&gt;</tt>の形式でマッピングする必要があります。例: <tt>Priority::Critical</tt></li><li>適切なオプションがない場合は、OneDevの課題フィールドをカスタマイズすることができます。</li></ul>");
		m.put("Specify how to map YouTrack issue links to OneDev issue links.<br><b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here", 
			"YouTrackの課題リンクをOneDevの課題リンクにマッピングする方法を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題リンクをカスタマイズすることができます。");
		m.put("Specify how to map YouTrack issue state to OneDev issue state. Unmapped states will use the initial state in OneDev.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"YouTrackの課題ステートをOneDevの課題ステートにマッピングする方法を指定してください。マッピングされていないステートはOneDevの初期ステートを使用します。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題ステートをカスタマイズすることができます。");
		m.put("Specify how to map YouTrack issue tags to OneDev issue custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"YouTrackの課題タグをOneDevの課題カスタムフィールドにマッピングする方法を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの課題フィールドをカスタマイズすることができます。");
		m.put("Specify image on the login button", "ログインボタンの画像を指定してください。");
		m.put("Specify image tag to pull from, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to pull from built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"プルするイメージタグを指定してください。例: <tt>registry-server:5000/myorg/myrepo:latest</tt>。組み込みレジストリからプルする場合、システム設定のサーバーURLで指定された<b>同じホスト</b>を使用するか、単に<tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>の形式を使用してください。");
		m.put("Specify image tag to push to, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"プッシュするイメージタグを指定してください。例: <tt>registry-server:5000/myorg/myrepo:latest</tt>。組み込みレジストリにプッシュする場合、システム設定のサーバーURLで指定された<b>同じホスト</b>を使用するか、単に<tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>の形式を使用してください。");
		m.put("Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple tags should be separated with space", 
			"プッシュするイメージタグを指定してください。例: <tt>registry-server:5000/myorg/myrepo:latest</tt>。組み込みレジストリにプッシュする場合、システム設定のサーバーURLで指定された<b>同じホスト</b>を使用するか、単に<tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>の形式を使用してください。複数のタグはスペースで区切ってください。");
		m.put("Specify import option", "インポートオプションを指定してください。");
		m.put("Specify incoming email poll interval in seconds", "受信メールのポーリング間隔を秒単位で指定してください。");
		m.put("Specify issue creation settings. For a particular sender and project, the first matching entry will take effect. Issue creation will be disallowed if no matching entry found", 
			"課題作成設定を指定してください。特定の送信者とプロジェクトに対して、最初に一致するエントリが適用されます。一致するエントリが見つからない場合、課題作成は許可されません。");
		m.put("Specify issue field to identify different columns of the board. Only state and single-valued enumeration field can be used here", 
			"ボードの異なる列を識別するための課題フィールドを指定してください。ここではステートと単一値の列挙型フィールドのみ使用できます。");
		m.put("Specify links to be displayed in the issue list", "課題リストに表示するリンクを指定してください。");
		m.put("Specify links to display in board card", "ボードカードに表示するリンクを指定してください。");
		m.put("Specify manager DN to authenticate OneDev itself to Active Directory. The manager DN should be specified in form of <i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>manager@example.com</i>", 
			"OneDev自身をActive Directoryに認証するためのマネージャーDNを指定してください。マネージャーDNは<i>&lt;アカウント名&gt;@&lt;ドメイン&gt;</i>の形式で指定する必要があります。例: <i>manager@example.com</i>");
		m.put("Specify manager DN to authenticate OneDev itself to LDAP server", "OneDev自身をLDAPサーバーに認証するためのマネージャーDNを指定してください。");
		m.put("Specify markdown file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対する公開するMarkdownファイルを指定してください。");
		m.put("Specify max git LFS file size in mega bytes", "Git LFSファイルの最大サイズをメガバイト単位で指定してください。");
		m.put("Specify max number of CPU intensive tasks the server can run concurrently, such as Git repository pull/push, repository index, etc.", 
			"サーバーが同時に実行できるCPU集約型タスクの最大数を指定してください。例: Gitリポジトリのプル/プッシュ、リポジトリのインデックス作成など。");
		m.put("Specify max number of jobs this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"このエグゼキューターが各一致したエージェントで同時に実行できるジョブの最大数を指定してください。空欄の場合、エージェントのCPUコア数として設定されます。");
		m.put("Specify max number of jobs this executor can run concurrently. Leave empty to set as CPU cores", 
			"このエグゼキューターが同時に実行できるジョブの最大数を指定してください。空欄の場合、CPUコア数として設定されます。");
		m.put("Specify max number of jobs/services this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"このエグゼキューターが各一致したエージェントで同時に実行できるジョブ/サービスの最大数を指定してください。空欄の場合、エージェントのCPUコア数として設定されます。");
		m.put("Specify max number of jobs/services this executor can run concurrently. Leave empty to set as CPU cores", 
			"このエグゼキューターが同時に実行できるジョブ/サービスの最大数を指定してください。空欄の場合、CPUコア数として設定されます。");
		m.put("Specify max size of uploaded file in mega bytes via web interface. This applies to file uploaded to repository, markdown content (issue comment etc), and build artifacts", 
			"Webインターフェースを介してアップロードされるファイルの最大サイズをメガバイト単位で指定してください。これにはリポジトリにアップロードされるファイル、Markdownコンテンツ（課題コメントなど）、およびビルド成果物が含まれます。");
		m.put("Specify memory request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"このエグゼキューターを使用する各ジョブ/サービスのメモリリクエストを指定してください。詳細は<a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetesリソース管理</a>を確認してください。");
		m.put("Specify mypy output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated by redirecting mypy output <b>without option '--pretty'</b>, for instance <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対するmypy出力ファイルを指定してください。このファイルはmypy出力をリダイレクトすることで生成できます。<b>オプション'--pretty'を使用しない</b>例: <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>。パターンマッチには*や?を使用してください。");
		m.put("Specify name of the branch", "ブランチ名を指定してください。");
		m.put("Specify name of the environment variable", "環境変数の名前を指定してください。");
		m.put("Specify name of the iteration", "イテレーションの名前を指定してください。");
		m.put("Specify name of the job", "ジョブ名を指定してください。");
		m.put("Specify name of the report to be displayed in build detail page", "ビルド詳細ページに表示するレポート名を指定してください。");
		m.put("Specify name of the saved query", "保存されたクエリの名前を指定してください。");
		m.put("Specify name of the service, which will be used as host name to access the service", 
			"サービス名を指定してください。この名前はサービスにアクセスするためのホスト名として使用されます。");
		m.put("Specify name of the tag", "タグ名を指定してください。");
		m.put("Specify network timeout in seconds when authenticate through this system", "このシステムを通じて認証する際のネットワークタイムアウトを秒単位で指定してください。");
		m.put("Specify node selector of this locator", "このロケーターのノードセレクターを指定してください。");
		m.put("Specify password or access token of specified registry", "指定されたレジストリのパスワードまたはアクセストークンを指定してください。");
		m.put("Specify password to authenticate with", "認証に使用するパスワードを指定してください。");
		m.put("Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>", "curl実行ファイルへのパスを指定してください。例: <tt>/usr/bin/curl</tt>");
		m.put("Specify path to git executable, for instance: <tt>/usr/bin/git</tt>", "git実行ファイルへのパスを指定してください。例: <tt>/usr/bin/git</tt>");
		m.put("Specify powershell executable to be used", "使用するpowershell実行ファイルを指定してください。");
		m.put("Specify project to import build spec from", "ビルド仕様をインポートするプロジェクトを指定してください。");
		m.put("Specify project to import into at OneDev side", "OneDev側でインポートするプロジェクトを指定してください。");
		m.put("Specify project to retrieve artifacts from", "成果物を取得するプロジェクトを指定してください。");
		m.put("Specify project to run job in", "ジョブを実行するプロジェクトを指定します。");
		m.put("Specify projects", "プロジェクトを指定してください。");
		m.put("Specify projects to update dependencies. Leave empty for current project", "依存関係を更新するプロジェクトを指定してください。現在のプロジェクトの場合は空欄にしてください。");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対するpylintのJSON結果ファイルを指定してください。このファイルはpylintのJSON出力形式オプションで生成できます。例: <code>--exit-zero --output-format=json:pylint-result.json</code>。違反があってもpylintコマンドは失敗しません。このステップは設定された閾値に基づいてビルドを失敗させます。パターンマッチには*や?を使用してください。");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"必要に応じてレジストリログインを指定してください。組み込みレジストリの場合、レジストリURLには<code>@server_url@</code>、ユーザー名には<code>@job_token@</code>、パスワードにはアクセストークンを使用してください。");
		m.put("Specify registry url. Leave empty for official registry", "レジストリURLを指定してください。公式レジストリの場合は空欄にしてください。");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>に対するOCIレイアウトを保存する相対パスを指定してください。");
		m.put("Specify repositories", "リポジトリを指定してください。");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"指定されたパスが変更された場合に必要なレビュアーを指定してください。変更を提出したユーザーは自動的に変更をレビューしたとみなされます。");
		m.put("Specify root URL to access this server", "このサーバーにアクセスするためのルートURLを指定してください。");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対するruffのJSON結果ファイルを指定してください。このファイルはruffのJSON出力形式オプションで生成できます。例: <code>--exit-zero --output-format json --output-file ruff-result.json</code>。違反があってもruffコマンドは失敗しません。このステップは設定された閾値に基づいてビルドを失敗させます。パターンマッチには*や?を使用してください。");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>で実行するシェルコマンド（Linux/Unix）またはバッチコマンド（Windows）を指定してください。");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>で実行するシェルコマンドを指定してください。");
		m.put("Specify shell to be used", "使用するシェルを指定してください。");
		m.put("Specify source param for SCP command, for instance <code>app.tar.gz</code>", "SCPコマンドのソースパラメータを指定してください。例: <code>app.tar.gz</code>");
		m.put("Specify space separated refs to pull from remote. '*' can be used in ref name for wildcard match<br><b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update branches/tags via this step", 
			"リモートからプルするスペース区切りのリファレンスを指定してください。リファレンス名にはワイルドカードマッチのために'*'を使用できます。<br><b class='text-danger'>注意:</b> このステップでブランチ/タグを更新する際、ブランチ/タグ保護ルールは無視されます。");
		m.put("Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"保護するスペース区切りのブランチを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'、または'?'を使用してください。'-'で始めると除外されます。");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"スペース区切りのジョブを指定してください。ワイルドカードマッチには'*'または'?'を使用してください。'-'で始めると除外されます。");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts will be granted implicitly in matched jobs even if no other permissions are specified here", 
			"スペース区切りのジョブを指定してください。ワイルドカードマッチには'*'または'?'を使用してください。'-'で始めると除外されます。<b class='text-danger'>注意:</b> ここで一致したジョブでは、他の権限が指定されていなくてもビルド成果物へのアクセス権が暗黙的に付与されます。");
		m.put("Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"保護するスペース区切りのパスを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'、または'?'を使用してください。'-'で始めると除外されます。");
		m.put("Specify space-separated projects applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all projects", 
			"このエントリに適用されるスペース区切りのプロジェクトを指定してください。ワイルドカードマッチには'*'または'?'を使用してください。'-'で始めると除外されます。すべてのプロジェクトに一致させる場合は空欄にしてください。");
		m.put("Specify space-separated sender email addresses applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all senders", 
			"このエントリに適用されるスペース区切りの送信者メールアドレスを指定してください。ワイルドカードマッチには'*'または'?'を使用してください。'-'で始めると除外されます。すべての送信者に一致させる場合は空欄にしてください。");
		m.put("Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"保護するスペース区切りのタグを指定してください。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には'**'、'*'、または'?'を使用してください。'-'で始めると除外されます。");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対するレポートの開始ページを指定してください。例: <tt>manual/index.md</tt>");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>に対するレポートの開始ページを指定してください。例: api/index.html");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"ビルドボリュームのストレージサイズをリクエストするために指定してください。サイズは<a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetesリソース容量形式</a>に準拠する必要があります。例: <i>10Gi</i>");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"提供されたレポート内の問題の列値を計算するために使用されるタブ幅を指定してください。");
		m.put("Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"ジョブを実行するタグを指定します。ブランチまたはタグのいずれかを指定できますが、両方は指定できません。両方が指定されていない場合はデフォルトのブランチが使用されます。");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"SCPコマンドのターゲットパラメータを指定してください。例: <code>user@@host:/app</code>。<b class='text-info'>注意:</b> リモートホストにscpコマンドがインストールされていることを確認してください。");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"一致した問題参照を置き換えるテキストを指定してください。例: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;ここで$1と$2は、例の問題パターン内のキャプチャグループを表します（問題パターンのヘルプを参照）。");
		m.put("Specify the condition current build must satisfy to execute this action", "このアクションを実行するために現在のビルドが満たすべき条件を指定してください。");
		m.put("Specify the condition preserved builds must match", "保持されたビルドが一致する条件を指定してください。");
		m.put("Specify the private key (in PEM format) used by SSH server to establish connections with client", 
			"SSHサーバーがクライアントとの接続を確立するために使用する秘密鍵（PEM形式）を指定してください。");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"グループメンバーシップ情報を取得する戦略を指定してください。LDAPグループに適切な権限を付与するには、同じ名前のOneDevグループを定義する必要があります。グループメンバーシップをOneDev側で管理したい場合は、<tt>グループを取得しない</tt>戦略を使用してください。");
		m.put("Specify timeout in seconds when communicating with mail server", "メールサーバーと通信する際のタイムアウトを秒単位で指定してください。");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "ジョブが送信されてからのタイムアウトを秒単位で指定してください。");
		m.put("Specify title of the issue", "問題のタイトルを指定してください。");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "YouTrack APIのURLを指定してください。例: <tt>http://localhost:8080/api</tt>");
		m.put("Specify user name of above machine for SSH authentication", "SSH認証のための上記マシンのユーザー名を指定してください。");
		m.put("Specify user name of specified registry", "指定されたレジストリのユーザー名を指定してください。");
		m.put("Specify user name of the registry", "レジストリのユーザー名を指定してください。");
		m.put("Specify user name to authenticate with", "認証に使用するユーザー名を指定してください。");
		m.put("Specify value of the environment variable", "環境変数の値を指定してください。");
		m.put("Specify web UI session timeout in minutes. Existing sessions will not be affected after changing this value.", 
			"Web UIセッションのタイムアウトを分単位で指定します。この値を変更しても既存のセッションには影響しません。");
		m.put("Specify webhook url to post events", "イベントを投稿するためのWebhook URLを指定してください。");
		m.put("Specify which issue state to use for closed GitHub issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"閉じたGitHubの問題に使用する問題状態を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの問題状態をカスタマイズすることができます。");
		m.put("Specify which issue state to use for closed GitLab issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"閉じたGitLabの問題に使用する問題状態を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの問題状態をカスタマイズすることができます。");
		m.put("Specify which issue state to use for closed Gitea issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"閉じたGiteaの問題に使用する問題状態を指定してください。<br><b>注意:</b> 適切なオプションがない場合は、OneDevの問題状態をカスタマイズすることができます。");
		m.put("Specify which states are considered as closed for various issues created by Renovate to orchestrate dependency update. Additionally, when Renovate closes the issue, OneDev will transit the issue to first state specified here", 
			"依存関係の更新を調整するためにRenovateが作成したさまざまな問題で閉じたと見なされる状態を指定してください。さらに、Renovateが問題を閉じると、OneDevはここで指定された最初の状態に問題を移行します。");
		m.put("Specify working days per week. This will affect parsing and displaying of working periods. For instance <tt>1w</tt> is the same as <tt>5d</tt> if this property is set to <tt>5</tt>", 
			"週あたりの勤務日数を指定してください。これにより、勤務期間の解析と表示に影響します。例: <tt>1w</tt>は、このプロパティが<tt>5</tt>に設定されている場合、<tt>5d</tt>と同じです。");
		m.put("Specify working hours per day. This will affect parsing and displaying of working periods. For instance <tt>1d</tt> is the same as <tt>8h</tt> if this property is set to <tt>8</tt>", 
			"1日あたりの勤務時間を指定してください。これにより、勤務期間の解析と表示に影響します。例: <tt>1d</tt>は、このプロパティが<tt>8</tt>に設定されている場合、<tt>8h</tt>と同じです。");
		m.put("Spent", "消費済み");
		m.put("Spent Time", "消費時間");
		m.put("Spent Time Issue Field", "消費時間問題フィールド");
		m.put("Spent Time:", "消費時間:");
		m.put("Spent time / estimated time", "消費時間 / 推定時間");
		m.put("Split", "分割");
		m.put("Split view", "分割ビュー");
		m.put("SpotBugs Report", "SpotBugsレポート");
		m.put("Squash Source Branch Commits", "ソースブランチのコミットをスカッシュ");
		m.put("Squash all commits from source branch into a single commit in target branch", "ソースブランチのすべてのコミットをターゲットブランチの単一のコミットにまとめます。");
		m.put("Squash source branch commits", "ソースブランチのコミットをスカッシュ");
		m.put("Ssh", "Ssh");
		m.put("Ssh Key", "Sshキー");
		m.put("Ssh Setting", "Ssh設定");
		m.put("Ssl Setting", "Ssl設定");
		m.put("Sso Connector", "Ssoコネクタ");
		m.put("Sso Provider Bean", "Ssoプロバイダービーン");
		m.put("Start At", "開始時刻");
		m.put("Start Date", "開始日");
		m.put("Start Page", "開始ページ");
		m.put("Start agent on remote Linux machine by running below command:", "以下のコマンドを実行してリモートLinuxマシンでエージェントを開始してください:");
		m.put("Start date", "開始日");
		m.put("Start to watch once I am involved", "関与したら監視を開始");
		m.put("Start work", "作業を開始");
		m.put("Start/Due Date", "開始/期限日");
		m.put("State", "状態");
		m.put("State Durations", "状態の期間");
		m.put("State Frequencies", "状態の頻度");
		m.put("State Spec", "状態仕様");
		m.put("State Transitions", "状態の遷移");
		m.put("State Trends", "状態の傾向");
		m.put("State of an issue is transited", "課題の状態が遷移される");
		m.put("States", "状態");
		m.put("Statistics", "統計");
		m.put("Stats", "統計情報");
		m.put("Stats Group", "統計グループ");
		m.put("Status", "ステータス");
		m.put("Status Code", "ステータスコード");
		m.put("Status code", "ステータスコード");
		m.put("Status code other than 200 indicating the error type", "エラータイプを示す200以外のステータスコード");
		m.put("Step", "ステップ");
		m.put("Step Template", "ステップテンプレート");
		m.put("Step Templates", "ステップテンプレート");
		m.put("Step {0} of {1}: ", "{0}のステップ{1}:");
		m.put("Steps", "ステップ");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"ステップは同じノード上で直列に実行され、同じ<a href='https://docs.onedev.io/concepts#job-workspace'>ジョブワークスペース</a>を共有します。");
		m.put("Stop work", "作業を停止");
		m.put("Stopwatch Overdue", "ストップウォッチ期限切れ");
		m.put("Storage Settings", "ストレージ設定");
		m.put("Storage file missing", "ストレージファイルが見つかりません");
		m.put("Storage not found", "ストレージが見つかりません");
		m.put("Stored with Git LFS", "Git LFSで保存");
		m.put("Sub Keys", "サブキー");
		m.put("Subject", "件名");
		m.put("Submit", "送信");
		m.put("Submit Reason", "送信理由");
		m.put("Submit Support Request", "サポートリクエストを送信");
		m.put("Submitted After", "送信後");
		m.put("Submitted At", "送信時刻");
		m.put("Submitted Before", "送信前");
		m.put("Submitted By", "送信者");
		m.put("Submitted manually", "手動で送信");
		m.put("Submitter", "送信者");
		m.put("Subscription Key", "サブスクリプションキー");
		m.put("Subscription Management", "サブスクリプション管理");
		m.put("Subscription data", "サブスクリプションデータ");
		m.put("Subscription key installed successfully", "サブスクリプションキーが正常にインストールされました");
		m.put("Subscription key not applicable: this key is intended to activate a trial subscription", 
			"サブスクリプションキーが適用されません: このキーは試用サブスクリプションを有効化するためのものです");
		m.put("Subscription key not applicable: this key is intended to renew a user based subscription", 
			"サブスクリプションキーは適用されません: このキーはユーザーベースのサブスクリプションを更新するためのものです");
		m.put("Subscription key not applicable: this key is intended to renew an unlimited users subscription", 
			"サブスクリプションキーは適用されません: このキーは無制限ユーザーサブスクリプションを更新するためのものです");
		m.put("Subscription key not applicable: this key is intended to update licensee of an existing subscription", 
			"サブスクリプションキーが適用されません: このキーは既存のサブスクリプションのライセンシーを更新するためのものです");
		m.put("Success Rate", "成功率");
		m.put("Successful", "成功");
		m.put("Suffix Pattern", "接尾辞パターン");
		m.put("Suggest changes", "変更を提案");
		m.put("Suggested change", "提案された変更");
		m.put("Suggestion is outdated either due to code change or pull request close", "提案がコード変更またはプルリクエストのクローズにより古くなっています");
		m.put("Suggestions", "提案");
		m.put("Summary", "概要");
		m.put("Support & Bug Report", "サポート＆バグレポート");
		m.put("Support Request", "サポートリクエスト");
		m.put("Swap", "スワップ");
		m.put("Switch to HTTP(S)", "HTTP(S)に切り替え");
		m.put("Switch to SSH", "SSHに切り替え");
		m.put("Symbol Name", "シンボル名");
		m.put("Symbol name", "シンボル名");
		m.put("Symbols", "シンボル");
		m.put("Sync Replica Status and Back to Home", "レプリカステータスを同期してホームに戻る");
		m.put("Sync Repository", "リポジトリを同期");
		m.put("Sync Timing of All Queried Issues", "すべてのクエリされた課題の同期タイミング");
		m.put("Sync Timing of Selected Issues", "選択された課題の同期タイミング");
		m.put("Sync requested. Please check status after a while", "同期が要求されました。しばらくしてからステータスを確認してください");
		m.put("Synchronize", "同期する");
		m.put("System", "システム");
		m.put("System Alert", "システムアラート");
		m.put("System Alert Template", "システムアラートテンプレート");
		m.put("System Date", "システム日付");
		m.put("System Email Address", "システムメールアドレス");
		m.put("System Maintenance", "システムメンテナンス");
		m.put("System Setting", "システム設定");
		m.put("System Settings", "システム設定");
		m.put("System email address defined in mail setting should be used as recipient of such email, and project name may be appended to this address using to indicate where to create issues. For instance, if system email address is specified as <tt>support@example.com</tt>, sending email to <tt>support+myproject@example.com</tt> will create issue in <tt>myproject</tt>. If project name is not appended, OneDev will look up the project using project designation information below", 
			"メール設定で定義されたシステムメールアドレスは、そのようなメールの受信者として使用されるべきであり、プロジェクト名を追加して課題を作成する場所を示すためにこのアドレスに追加することができます。例えば、システムメールアドレスが<tt>support@example.com</tt>として指定されている場合、<tt>support+myproject@example.com</tt>にメールを送信すると<tt>myproject</tt>で課題が作成されます。プロジェクト名が追加されない場合、OneDevは以下のプロジェクト指定情報を使用してプロジェクトを検索します");
		m.put("System settings have been saved", "システム設定が保存されました");
		m.put("System uuid", "システムUUID");
		m.put("TIMED_OUT", "TIMED_OUT");
		m.put("TRX Report (.net unit test)", "TRXレポート（.netユニットテスト）");
		m.put("Tab Width", "タブ幅");
		m.put("Tag", "タグ");
		m.put("Tag \"{0}\" already exists, please choose a different name", "タグ「{0}」はすでに存在します。別の名前を選んでください");
		m.put("Tag \"{0}\" already exists, please choose a different name.", "タグ「{0}」はすでに存在します。別の名前を選んでください。");
		m.put("Tag \"{0}\" created", "タグ「{0}」が作成されました");
		m.put("Tag \"{0}\" deleted", "タグ「{0}」が削除されました");
		m.put("Tag Message", "タグメッセージ");
		m.put("Tag Name", "タグ名");
		m.put("Tag Protection", "タグ保護");
		m.put("Tag creation", "タグ作成");
		m.put("Tags", "タグ");
		m.put("Target", "ターゲット");
		m.put("Target Branches", "ターゲットブランチ");
		m.put("Target Docker Image", "ターゲットDockerイメージ");
		m.put("Target File", "ターゲットファイル");
		m.put("Target Path", "ターゲットパス");
		m.put("Target Project", "ターゲットプロジェクト");
		m.put("Target branch no longer exists", "ターゲットブランチはもう存在しません");
		m.put("Target branch was fast-forwarded to source branch", "ターゲットブランチはソースブランチにファストフォワードされました");
		m.put("Target branch will be fast-forwarded to source branch", "ターゲットブランチはソースブランチにファストフォワードされます");
		m.put("Target containing spaces or starting with dash needs to be quoted", "スペースを含む、またはダッシュで始まるターゲットは引用符で囲む必要があります");
		m.put("Target or source branch is updated. Please try again", "ターゲットまたはソースブランチが更新されました。もう一度試してください");
		m.put("Task List", "タスクリスト");
		m.put("Task list", "タスクリスト");
		m.put("Tell user to reset password", "ユーザーにパスワードリセットを指示する");
		m.put("Template Name", "テンプレート名");
		m.put("Template saved", "テンプレートが保存されました");
		m.put("Terminal close", "ターミナルを閉じる");
		m.put("Terminal input", "ターミナル入力");
		m.put("Terminal open", "ターミナルを開く");
		m.put("Terminal output", "ターミナル出力");
		m.put("Terminal ready", "ターミナル準備完了");
		m.put("Terminal resize", "ターミナルサイズ変更");
		m.put("Test", "テスト");
		m.put("Test Case", "テストケース");
		m.put("Test Cases", "テストケース");
		m.put("Test Settings", "テスト設定");
		m.put("Test Suite", "テストスイート");
		m.put("Test Suites", "テストスイート");
		m.put("Test importing from {0}", "{0}からのテストインポート");
		m.put("Test mail has been sent to {0}, please check your mail box", "{0}にテストメールが送信されました。メールボックスを確認してください");
		m.put("Test successful: authentication passed", "テスト成功：認証が通過しました");
		m.put("Test successful: authentication passed with below information retrieved:", "テスト成功：以下の情報が取得され、認証が通過しました：");
		m.put("Text", "テキスト");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "WebhookのPOSTリクエストを受信するサーバーエンドポイントのURL");
		m.put("The change contains disallowed file type(s): {0}", "変更には許可されていないファイルタイプが含まれています: {0}");
		m.put("The first board will be the default board", "最初のボードがデフォルトボードになります");
		m.put("The first timesheet will be the default timesheet", "最初のタイムシートがデフォルトタイムシートになります");
		m.put("The object you are deleting/disabling is still being used", "削除/無効化しようとしているオブジェクトはまだ使用されています");
		m.put("The password reset url is invalid or obsolete", "パスワードリセットURLが無効または古くなっています");
		m.put("The permission to access build log", "ビルドログにアクセスする権限");
		m.put("The permission to access build pipeline", "ビルドパイプラインにアクセスする権限");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"ジョブを手動で実行する権限。それはまた、ビルドログ、ビルドパイプライン、およびすべての公開されたレポートにアクセスする権限を含みます");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"POSTリクエストがOneDevから送信されたことを確認するための秘密鍵。秘密鍵を設定すると、WebhookのPOSTリクエストでX-OneDev-Signatureヘッダーを受け取ります");
		m.put("The service desk feature enables user to create issues by sending emails to OneDev. Issues can be discussed over email completely, without the need of logging to OneDev.", 
			"サービスデスク機能により、ユーザーはOneDevにメールを送信して課題を作成できます。課題は完全にメールで議論することができ、OneDevにログインする必要はありません。");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "TOTP認証アプリに表示されるパスコードを入力して確認してください");
		m.put("Then publish package from project directory like below", "以下のようにプロジェクトディレクトリからパッケージを公開してください");
		m.put("Then push gem to the source", "ソースにgemをプッシュしてください");
		m.put("Then push image to desired repository under specified project", "指定されたプロジェクトの下で希望するリポジトリにイメージをプッシュしてください");
		m.put("Then push package to the source", "ソースにパッケージをプッシュしてください");
		m.put("Then resolve dependency via command step", "コマンドステップを使用して依存関係を解決してください");
		m.put("Then upload package to the repository with twine", "twineを使用してリポジトリにパッケージをアップロードしてください");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"ブランチ<span wicket:id=\"branch\"></span>に対して<a wicket:id=\"openRequests\">オープンなプルリクエスト</a>があります。このブランチが削除されると、これらのプルリクエストは破棄されます。");
		m.put("There are incompatibilities since your upgraded version", "アップグレードされたバージョンには互換性の問題があります");
		m.put("There are merge conflicts", "マージコンフリクトがあります");
		m.put("There are merge conflicts.", "マージコンフリクトがあります。");
		m.put("There are merge conflicts. You can still create the pull request though", "マージコンフリクトがあります。ただし、プルリクエストを作成することは可能です");
		m.put("There are unsaved changes, discard and continue?", "保存されていない変更があります。破棄して続行しますか？");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"これらの認証アプリは通常、携帯電話で動作します。例としてはGoogle Authenticator、Microsoft Authenticator、Authy、1Passwordなどがあります。");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"この<span wicket:id=\"elementTypeName\"></span>は<a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>からインポートされました");
		m.put("This Month", "今月");
		m.put("This Week", "今週");
		m.put("This account is disabled", "このアカウントは無効です");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"このアドレスはSendGridで<code>送信者として確認済み</code>である必要があり、さまざまなメール通知の送信者アドレスとして使用されます。<code>投稿されたメールを受信</code>オプションが以下で有効になっている場合、このアドレスに返信して課題やプルリクエストのコメントを投稿することもできます");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"このアドレスはさまざまなメール通知の送信者アドレスとして使用されます。<code>受信メールを確認</code>オプションが以下で有効になっている場合、ユーザーはこのアドレスに返信してメールで課題やプルリクエストのコメントを投稿することもできます");
		m.put("This change is already opened for merge by pull request {0}", "この変更はすでにプルリクエスト{0}によってマージのために開かれています");
		m.put("This change is squashed/rebased onto base branch via a pull request", "この変更はプルリクエストを介してベースブランチにスカッシュ/リベースされました");
		m.put("This change is squashed/rebased onto base branch via pull request {0}", "この変更はプルリクエスト{0}を介してベースブランチにスカッシュ/リベースされました");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "この変更はいくつかのジョブによって確認される必要があります。代わりにプルリクエストを送信してください");
		m.put("This commit is rebased", "このコミットはリベースされました");
		m.put("This date is using <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 format</a>", 
			"この日付は<a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601形式</a>を使用しています");
		m.put("This email address is being used", "このメールアドレスは使用されています");
		m.put("This executor runs build jobs as docker containers on OneDev server", "このエグゼキュータは、OneDevサーバー上でDockerコンテナとしてビルドジョブを実行します");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"このエグゼキュータは、<a href='/~administration/agents' target='_blank'>エージェント</a>を介してリモートマシン上でDockerコンテナとしてビルドジョブを実行します");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"このエグゼキュータは、Kubernetesクラスター内のPodとしてビルドジョブを実行します。エージェントは不要です。<b class='text-danger'>注意:</b> ジョブPodがソースやアーティファクトをダウンロードするためにサーバーURLがシステム設定で正しく指定されていることを確認してください");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs", 
			"このエグゼキュータはOneDevサーバーのシェル機能を使用してビルドジョブを実行します。<br><b class='text-danger'>警告</b>: このエグゼキュータで実行されるジョブはOneDevサーバープロセスと同じ権限を持ちます。信頼できるジョブでのみ使用されることを確認してください");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. Make sure it can only be used by trusted jobs", 
			"このエグゼキュータは<a href='/~administration/agents' target='_blank'>エージェント</a>を介してリモートマシンのシェル機能を使用してビルドジョブを実行します。<br><b class='text-danger'>警告</b>: このエグゼキュータで実行されるジョブはOneDevエージェントプロセスと同じ権限を持ちます。信頼できるジョブでのみ使用されることを確認してください");
		m.put("This field is required", "このフィールドは必須です");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"このフィルターは現在のユーザーのLDAPエントリを決定するために使用されます。例: <i>(&(uid={0})(objectclass=person))</i>。この例では、<i>{0}</i>は現在のユーザーのログイン名を表します");
		m.put("This installation does not have an active subscription and runs as community edition. To access <a href=\"https://onedev.io/pricing\">enterprise features</a>, an active subscription is required", 
			"このインストールにはアクティブなサブスクリプションがなく、コミュニティエディションとして実行されています。<a href=\"https://onedev.io/pricing\">エンタープライズ機能</a>にアクセスするには、アクティブなサブスクリプションが必要です");
		m.put("This installation has a trial subscription and is now running as enterprise edition", 
			"このインストールには試用サブスクリプションがあり、現在エンタープライズエディションとして実行されています");
		m.put("This installation has an active subscription and runs as enterprise edition", "このインストールにはアクティブなサブスクリプションがあり、エンタープライズエディションとして実行されています");
		m.put("This installation has an expired subscription, and runs as community edition", "このインストールには期限切れのサブスクリプションがあり、コミュニティエディションとして実行されています");
		m.put("This installation has an unlimited users subscription and is now running as enterprise edition", 
			"このインストールは無制限ユーザーサブスクリプションを持ち、現在エンタープライズエディションとして実行されています");
		m.put("This installation's subscription has expired and is now running as the community edition", 
			"このインストールのサブスクリプションは期限切れで、現在コミュニティエディションとして実行されています");
		m.put("This is a Git LFS object, but the storage file is missing", "これはGit LFSオブジェクトですが、ストレージファイルがありません");
		m.put("This is a built-in role and can not be deleted", "これは組み込みのロールであり、削除することはできません");
		m.put("This is a disabled service account", "これは無効化されたサービスアカウントです");
		m.put("This is a layer cache. To use the cache, add below option to your docker buildx command", 
			"これはレイヤーキャッシュです。キャッシュを使用するには、以下のオプションをDocker buildxコマンドに追加してください");
		m.put("This is a service account for task automation purpose", "これはタスク自動化目的のサービスアカウントです");
		m.put("This is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"これはエンタープライズ機能です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料でお試しください</a>");
		m.put("This key has already been used by another project", "このキーは別のプロジェクトで既に使用されています");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"このキーは{0}に関連付けられていますが、このユーザーの確認済みメールアドレスではありません");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here.<br><b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated from these lock files with the <b>generate checksum step</b> defined before this step", 
			"このキーはプロジェクト階層内でキャッシュヒットがあるかどうかを判断するために使用されます（現在のプロジェクトからルートプロジェクトまで順に検索し、以下のロードキーも同様です）。キャッシュは、ここで定義されたキーと完全に一致する場合にヒットと見なされます。<br><b>注意:</b> プロジェクトにキャッシュ状態を表現できるロックファイル（package.json、pom.xmlなど）がある場合、このキーは&lt;cache name&gt;-@file:checksum.txt@として定義されるべきです。ここで、checksum.txtはこれらのロックファイルから<b>チェックサム生成ステップ</b>で生成され、このステップの前に定義されます");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"このキーはプロジェクト階層内でキャッシュをダウンロードおよびアップロードするために使用されます（現在のプロジェクトからルートプロジェクトまで順に検索）");
		m.put("This key or one of its sub key is already added", "このキーまたはそのサブキーのいずれかが既に追加されています");
		m.put("This key or one of its subkey is already in use", "このキーまたはそのサブキーのいずれかが既に使用されています");
		m.put("This line has confusable unicode character modification", "この行には紛らわしいUnicode文字の変更があります");
		m.put("This might happen when project points to a wrong git repository, or the commit is garbage collected.", 
			"これはプロジェクトが誤ったGitリポジトリを指している場合や、コミットがガベージコレクションされた場合に発生する可能性があります");
		m.put("This might happen when project points to a wrong git repository, or these commits are garbage collected.", 
			"これはプロジェクトが誤ったGitリポジトリを指している場合や、これらのコミットがガベージコレクションされた場合に発生する可能性があります");
		m.put("This name has already been used by another board", "この名前は別のボードで既に使用されています");
		m.put("This name has already been used by another group", "この名前は別のグループで既に使用されています");
		m.put("This name has already been used by another issue board in the project", "この名前はプロジェクト内の別の課題ボードで既に使用されています");
		m.put("This name has already been used by another job executor", "この名前は別のジョブエグゼキュータで既に使用されています");
		m.put("This name has already been used by another project", "この名前は別のプロジェクトで既に使用されています");
		m.put("This name has already been used by another provider", "この名前は別のプロバイダーで既に使用されています");
		m.put("This name has already been used by another role", "この名前は別のロールで既に使用されています");
		m.put("This name has already been used by another role.", "この名前は別のロールで既に使用されています。");
		m.put("This name has already been used by another script", "この名前は別のスクリプトで既に使用されています");
		m.put("This name has already been used by another state", "この名前は別の状態で既に使用されています");
		m.put("This operation is disallowed by branch protection rule", "この操作はブランチ保護ルールによって禁止されています");
		m.put("This page lists changes since previous build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a>", 
			"このページには<a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">同じストリーム</a>の前回のビルド以降の変更が一覧表示されます");
		m.put("This page lists recent commits fixing the issue", "このページには問題を修正する最近のコミットが一覧表示されます");
		m.put("This permission enables one to access confidential issues", "この権限により、機密事項にアクセスすることが可能になります");
		m.put("This permission enables one to schedule issues into iterations", "この権限により、イテレーションに課題をスケジュールすることが可能になります");
		m.put("This property is imported from {0}", "このプロパティは{0}からインポートされます");
		m.put("This pull request has been discarded", "このプルリクエストは破棄されました");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"このレポートは、プルリクエストによってビルドがトリガーされた場合、プルリクエスト概要ページに表示されます");
		m.put("This server is currently accessed via http protocol, please configure your docker daemon or buildx builder to <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">work with insecure registry</a>", 
			"このサーバーは現在HTTPプロトコルを介してアクセスされています。Dockerデーモンまたはbuildxビルダーを<a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">安全でないレジストリで動作するように構成してください</a>");
		m.put("This shows average duration of different states over time", "これは、時間の経過に伴う異なる状態の平均期間を示します");
		m.put("This shows average duration of merged pull requests over time", "これは、時間の経過に伴うマージされたプルリクエストの平均期間を示します");
		m.put("This shows number of <b>new</b> issues in different states over time", "これは、時間の経過に伴う<b>新しい</b>課題の数を示します");
		m.put("This shows number of issues in various states over time", "これは、時間の経過に伴うさまざまな状態の課題の数を示します");
		m.put("This shows number of open and merged pull requests over time", "これは、時間の経過に伴うオープンおよびマージされたプルリクエストの数を示します");
		m.put("This step can only be executed by a docker aware executor", "このステップはDocker対応エグゼキュータによってのみ実行可能です");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"このステップはDocker対応エグゼキュータによってのみ実行可能です。<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>ジョブワークスペース</a>で実行されます");
		m.put("This step copies files from job workspace to build artifacts directory, so that they can be accessed after job is completed", 
			"このステップは、ジョブのワークスペースからビルド成果物ディレクトリにファイルをコピーし、ジョブが完了した後にアクセスできるようにします");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"このステップは指定されたファイルをプロジェクトウェブサイトとして公開します。プロジェクトウェブサイトは<code>http://&lt;onedev base url&gt;/path/to/project/~site</code>で公開アクセス可能です");
		m.put("This step pulls specified refs from remote", "このステップでは、指定されたリファレンスをリモートからプルします");
		m.put("This step pushes current commit to same ref on remote", "このステップは現在のコミットをリモートの同じリファレンスにプッシュします");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"このステップはRenovateキャッシュをセットアップします。Renovateステップの前に配置すると使用できます");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"このステップはTrivy DBキャッシュをセットアップし、さまざまなスキャナーステップを高速化します。スキャナーステップの前に配置すると使用できます");
		m.put("This subscription key was already used", "このサブスクリプションキーは既に使用されています");
		m.put("This subscription key was expired", "このサブスクリプションキーは期限切れです");
		m.put("This tab shows the pipeline containing current build. Check <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">this article</a> to understand how build pipeline works", 
			"このタブには現在のビルドを含むパイプラインが表示されます。ビルドパイプラインの仕組みを理解するには<a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">この記事</a>を確認してください");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"このトリガーは、タグ付けされたコミットがここで指定されたブランチから到達可能な場合にのみ適用されます。複数のブランチはスペースで区切る必要があります。<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカードマッチ</a>には '**', '*' または '?' を使用してください。'-' を付けて除外します。すべてのブランチに一致させるには空白のままにします");
		m.put("This user is authenticating via external system.", "このユーザーは外部システムを介して認証しています");
		m.put("This user is authenticating via internal database.", "このユーザーは内部データベースを介して認証しています");
		m.put("This user is currently authenticating via external system. Setting password will switch to use internal database", 
			"このユーザーは現在外部システムを介して認証しています。パスワードを設定すると内部データベースの使用に切り替わります");
		m.put("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?", 
			"これにより現在のサブスクリプションが無効化され、すべてのエンタープライズ機能が無効になります。続行しますか？");
		m.put("This will discard all project specific boards, do you want to continue?", "これによりプロジェクト固有のすべてのボードが破棄されます。続行しますか？");
		m.put("This will restart SSH server. Do you want to continue?", "これによりSSHサーバーが再起動されます。続行しますか？");
		m.put("Threads", "スレッド");
		m.put("Time Estimate Issue Field", "時間見積もり課題フィールド");
		m.put("Time Range", "時間範囲");
		m.put("Time Spent Issue Field", "費やした時間課題フィールド");
		m.put("Time Tracking", "時間追跡");
		m.put("Time Tracking Setting", "時間追跡設定");
		m.put("Time Tracking Settings", "時間追跡設定");
		m.put("Time tracking settings have been saved", "時間追跡設定が保存されました");
		m.put("Timed out", "タイムアウトしました");
		m.put("Timeout", "タイムアウト");
		m.put("Timesheet", "タイムシート");
		m.put("Timesheet Setting", "タイムシート設定");
		m.put("Timesheets", "タイムシート");
		m.put("Timing", "タイミング");
		m.put("Title", "タイトル");
		m.put("To Everyone", "全員へ");
		m.put("To State", "状態へ");
		m.put("To States", "状態へ");
		m.put("To authenticate via internal database, <a wicket:id=\"setPasswordForUser\">set password for user</a> or <a wicket:id=\"tellUserToResetPassword\">tell user to reset password</a>", 
			"内部データベースを介して認証するには、<a wicket:id=\"setPasswordForUser\">ユーザーのパスワードを設定</a>するか、<a wicket:id=\"tellUserToResetPassword\">ユーザーにパスワードのリセットを依頼</a>してください");
		m.put("To avoid duplication, estimated/remaining time showing here does not include those aggregated from \"{0}\"", 
			"重複を避けるため、ここに表示される推定/残り時間には\"{0}\"から集計されたものは含まれていません");
		m.put("To avoid duplication, spent time showing here does not include those aggregated from \"{0}\"", 
			"重複を避けるため、ここに表示される費やした時間には\"{0}\"から集計されたものは含まれていません");
		m.put("Toggle change history", "変更履歴を切り替える");
		m.put("Toggle comments", "コメントを切り替える");
		m.put("Toggle commits", "コミットを切り替える");
		m.put("Toggle dark mode", "ダークモードを切り替える");
		m.put("Toggle detail message", "詳細メッセージを切り替える");
		m.put("Toggle fixed width font", "固定幅フォントを切り替える");
		m.put("Toggle full screen", "全画面表示を切り替える");
		m.put("Toggle matched contents", "一致した内容を切り替える");
		m.put("Toggle navigation", "ナビゲーションを切り替える");
		m.put("Toggle work log", "作業ログを切り替える");
		m.put("Tokens", "トークン");
		m.put("Too many commits to load", "読み込むコミットが多すぎます");
		m.put("Too many commits, displaying recent {0}", "コミットが多すぎます。最近の{0}を表示しています");
		m.put("Too many log entries, displaying recent {0}", "ログエントリが多すぎます。最近の{0}を表示しています");
		m.put("Too many problems, displaying first {0}", "問題が多すぎます。最初の{0}を表示しています");
		m.put("Toomanyrequests", "リクエストが多すぎます");
		m.put("Top", "トップ");
		m.put("Topo", "トポ");
		m.put("Total Heap Memory", "総ヒープメモリ");
		m.put("Total Number", "総数");
		m.put("Total Problems", "総問題数");
		m.put("Total Size", "総サイズ");
		m.put("Total Test Duration", "総テスト時間");
		m.put("Total estimated time", "総推定時間");
		m.put("Total spent time", "総費やした時間");
		m.put("Total spent time / total estimated time", "総費やした時間 / 総推定時間");
		m.put("Total time", "総時間");
		m.put("Total:", "合計:");
		m.put("Touched File", "変更されたファイル");
		m.put("Touched Files", "変更されたファイル群");
		m.put("Transfer LFS Files", "LFSファイルを転送");
		m.put("Transit manually", "手動で移行");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "課題\"{0}\"の状態を\"{1}\"に移行しました ({2})");
		m.put("Transition Edit Bean", "遷移編集ビーン");
		m.put("Transition Spec", "遷移仕様");
		m.put("Trial Expiration Date", "試用期限日");
		m.put("Trial subscription key not applicable for this installation", "このインストールには試用サブスクリプションキーは適用されません");
		m.put("Triggers", "トリガー");
		m.put("Trivy Container Image Scanner", "Trivyコンテナイメージスキャナー");
		m.put("Trivy Filesystem Scanner", "Trivyファイルシステムスキャナー");
		m.put("Trivy Rootfs Scanner", "Trivy Rootfsスキャナー");
		m.put("Try EE", "EEを試す");
		m.put("Try Enterprise Edition", "エンタープライズ版を試す");
		m.put("Twitch", "Twitch");
		m.put("Two Factor Authentication", "二要素認証");
		m.put("Two-factor Authentication", "二要素認証");
		m.put("Two-factor authentication already set up. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Request to set up again", 
			"二要素認証はすでに設定されています。<a wicket:id=\"requestToSetupAgain\"><wicket:t>再設定をリクエスト");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"二要素認証が有効です。TOTP認証アプリに表示されるパスコードを入力してください。問題が発生した場合は、OneDevサーバーとTOTP認証アプリを実行しているデバイスの時刻が同期していることを確認してください");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"セキュリティを強化するため、アカウントに二要素認証が強制されています。以下の手順に従って設定してください");
		m.put("Two-factor authentication is now configured", "二要素認証が設定されました");
		m.put("Two-factor authentication not enabled", "二要素認証が有効になっていません");
		m.put("Type", "タイプ");
		m.put("Type <code>yes</code> below to cancel all queried builds", "以下に<code>yes</code>と入力して、すべてのクエリされたビルドをキャンセルしてください");
		m.put("Type <code>yes</code> below to cancel selected builds", "以下に<code>yes</code>と入力して、選択されたビルドをキャンセルしてください");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "以下に<code>yes</code>と入力して、すべてのクエリされたユーザーを削除することを確認してください");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "以下に<code>yes</code>と入力して、選択されたユーザーを削除することを確認してください");
		m.put("Type <code>yes</code> below to copy all queried issues to project \"{0}\"", "以下に<code>yes</code>と入力して、すべてのクエリされた課題をプロジェクト\"{0}\"にコピーしてください");
		m.put("Type <code>yes</code> below to copy selected issues to project \"{0}\"", "以下に<code>yes</code>と入力して、選択された課題をプロジェクト\"{0}\"にコピーしてください");
		m.put("Type <code>yes</code> below to delete all queried builds", "以下に<code>yes</code>と入力して、すべてのクエリされたビルドを削除してください");
		m.put("Type <code>yes</code> below to delete all queried comments", "以下に<code>yes</code>と入力して、すべてのクエリされたコメントを削除してください");
		m.put("Type <code>yes</code> below to delete all queried issues", "以下に<code>yes</code>と入力して、すべてのクエリされた課題を削除してください");
		m.put("Type <code>yes</code> below to delete all queried packages", "以下に<code>yes</code>と入力して、すべてのクエリされたパッケージを削除してください");
		m.put("Type <code>yes</code> below to delete all queried projects", "以下に<code>yes</code>と入力して、すべてのクエリされたプロジェクトを削除してください");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "以下に<code>yes</code>と入力して、すべてのクエリされたプルリクエストを削除してください");
		m.put("Type <code>yes</code> below to delete selected builds", "以下に<code>yes</code>と入力して、選択されたビルドを削除してください");
		m.put("Type <code>yes</code> below to delete selected comments", "以下に<code>yes</code>と入力して、選択されたコメントを削除してください");
		m.put("Type <code>yes</code> below to delete selected issues", "以下に<code>yes</code>と入力して、選択された課題を削除してください");
		m.put("Type <code>yes</code> below to delete selected packages", "以下に<code>yes</code>と入力して、選択されたパッケージを削除してください");
		m.put("Type <code>yes</code> below to delete selected projects", "以下に<code>yes</code>と入力して、選択されたプロジェクトを削除してください");
		m.put("Type <code>yes</code> below to delete selected pull requests", "以下に<code>yes</code>と入力して、選択されたプルリクエストを削除してください");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "以下に<code>yes</code>と入力して、すべてのクエリされたプルリクエストを破棄してください");
		m.put("Type <code>yes</code> below to discard selected pull requests", "以下に<code>yes</code>と入力して、選択されたプルリクエストを破棄してください");
		m.put("Type <code>yes</code> below to move all queried issues to project \"{0}\"", "以下に<code>yes</code>と入力して、すべてのクエリされた課題をプロジェクト\"{0}\"に移動してください");
		m.put("Type <code>yes</code> below to move all queried projects to be under \"{0}\"", "以下に<code>yes</code>と入力して、すべてのクエリされたプロジェクトを\"{0}\"の下に移動してください");
		m.put("Type <code>yes</code> below to move selected issues to project \"{0}\"", "以下に<code>yes</code>と入力して、選択された課題をプロジェクト\"{0}\"に移動してください");
		m.put("Type <code>yes</code> below to move selected projects to be under \"{0}\"", "以下に<code>yes</code>と入力して、選択されたプロジェクトを\"{0}\"の下に移動してください");
		m.put("Type <code>yes</code> below to pause all queried agents", "以下に<code>yes</code>と入力して、すべてのクエリされたエージェントを一時停止してください");
		m.put("Type <code>yes</code> below to re-run all queried builds", "以下に<code>yes</code>と入力して、すべてのクエリされたビルドを再実行してください");
		m.put("Type <code>yes</code> below to re-run selected builds", "以下に<code>yes</code>と入力して、選択されたビルドを再実行してください");
		m.put("Type <code>yes</code> below to remove all queried users from group", "クエリされたすべてのユーザーをグループから削除するには、以下に<code>yes</code>と入力してください");
		m.put("Type <code>yes</code> below to remove from all queried groups", "クエリされたすべてのグループから削除するには、以下に<code>yes</code>と入力してください");
		m.put("Type <code>yes</code> below to remove from selected groups", "選択されたグループから削除するには、以下に<code>yes</code>と入力してください");
		m.put("Type <code>yes</code> below to remove selected users from group", "選択されたユーザーをグループから削除するには、以下に<code>yes</code>と入力してください");
		m.put("Type <code>yes</code> below to restart all queried agents", "以下に<code>yes</code>と入力して、すべてのクエリされたエージェントを再起動してください");
		m.put("Type <code>yes</code> below to restart selected agents", "以下に<code>yes</code>と入力して、選択されたエージェントを再起動してください");
		m.put("Type <code>yes</code> below to resume all queried agents", "以下に<code>yes</code>と入力して、すべてのクエリされたエージェントを再開してください");
		m.put("Type <code>yes</code> below to set all queried as root projects", "以下に<code>yes</code>と入力して、すべてのクエリされたプロジェクトをルートプロジェクトとして設定してください");
		m.put("Type <code>yes</code> below to set selected as root projects", "以下に<code>yes</code>と入力して、選択されたプロジェクトをルートプロジェクトとして設定してください");
		m.put("Type password here", "ここにパスワードを入力してください");
		m.put("Type to filter", "フィルターするために入力してください");
		m.put("Type to filter...", "フィルターするために入力してください...");
		m.put("URL", "URL");
		m.put("Unable To Delete/Disable Right Now", "今すぐ削除/無効化できません");
		m.put("Unable to apply change as otherwise you will not be able to manage this project", "このプロジェクトを管理できなくなるため、変更を適用できません");
		m.put("Unable to change password as you are authenticating via external system", "外部システムを介して認証しているため、パスワードを変更できません");
		m.put("Unable to comment", "コメントできません");
		m.put("Unable to connect to server", "サーバーに接続できません");
		m.put("Unable to create protected branch", "保護されたブランチを作成できません");
		m.put("Unable to create protected tag", "保護されたタグを作成できません");
		m.put("Unable to diff as some line is too long.", "一部の行が長すぎるため、差分を表示できません");
		m.put("Unable to diff as the file is too large.", "ファイルが大きすぎるため、差分を表示できません");
		m.put("Unable to find SSO provider: ", "SSOプロバイダーが見つかりません:");
		m.put("Unable to find agent {0}", "エージェント{0}が見つかりません");
		m.put("Unable to find build #{0} in project {1}", "プロジェクト{1}内でビルド#{0}が見つかりません");
		m.put("Unable to find commit to import build spec (import project: {0}, import revision: {1})", 
			"ビルド仕様をインポートするコミットが見つかりません (インポートプロジェクト: {0}, インポートリビジョン: {1})");
		m.put("Unable to find issue #{0} in project {1}", "プロジェクト{1}内で課題#{0}が見つかりません");
		m.put("Unable to find project to import build spec: {0}", "ビルド仕様をインポートするプロジェクトが見つかりません: {0}");
		m.put("Unable to find pull request #{0} in project {1}", "プロジェクト{1}内でプルリクエスト#{0}が見つかりません");
		m.put("Unable to find timesheet: ", "タイムシートが見つかりません:");
		m.put("Unable to get guilds info", "ギルド情報を取得できません");
		m.put("Unable to import build spec (import project: {0}, import revision: {1}): {2}", "ビルド仕様をインポートできません (インポートプロジェクト: {0}, インポートリビジョン: {1}): {2}");
		m.put("Unable to notify user as mail service is not configured", "メールサービスが構成されていないため、ユーザーに通知できません");
		m.put("Unable to send password reset email as mail service is not configured", "メールサービスが設定されていないため、パスワードリセットメールを送信できません");
		m.put("Unable to send verification email as mail service is not configured yet", "メールサービスがまだ構成されていないため、確認メールを送信できません");
		m.put("Unauthorize this user", "このユーザーの認証を解除する");
		m.put("Unauthorized", "認証されていません");
		m.put("Undefined", "未定義");
		m.put("Undefined Field Resolution", "未定義フィールド解決");
		m.put("Undefined Field Value Resolution", "未定義フィールド値解決");
		m.put("Undefined State Resolution", "未定義の状態解決");
		m.put("Undefined custom field: ", "未定義のカスタムフィールド:");
		m.put("Under which condition this step should run. <b>SUCCESSFUL</b> means all non-optional steps running before this step are successful", 
			"このステップが実行される条件。<b>SUCCESSFUL</b>は、このステップの前に実行されたすべての非オプションステップが成功したことを意味します");
		m.put("Unexpected setting: {0}", "予期しない設定: {0}");
		m.put("Unexpected ssh signature hash algorithm: ", "予期しないssh署名ハッシュアルゴリズム:");
		m.put("Unexpected ssh signature namespace: ", "予期しないssh署名ネームスペース:");
		m.put("Unified", "統一");
		m.put("Unified view", "統一ビュー");
		m.put("Unit Test Statistics", "単体テスト統計");
		m.put("Unlimited", "無制限");
		m.put("Unlink this issue", "この課題のリンクを解除");
		m.put("Unordered List", "順序なしリスト");
		m.put("Unordered list", "順序なしリスト");
		m.put("Unpin this issue", "この課題のピンを解除");
		m.put("Unresolved", "未解決");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "プロジェクト \"{1}\" のファイル \"{0}\" に未解決のコメント");
		m.put("Unscheduled", "未スケジュール");
		m.put("Unscheduled Issues", "未スケジュールの課題");
		m.put("Unsolicited OIDC authentication response", "未承諾のOIDC認証応答");
		m.put("Unsolicited OIDC response", "未承諾のOIDC応答");
		m.put("Unsolicited discord api response", "未承諾のDiscord API応答");
		m.put("Unspecified", "未指定");
		m.put("Unsupported", "非対応");
		m.put("Unsupported ssh signature algorithm: ", "サポートされていないssh署名アルゴリズム:");
		m.put("Unsupported ssh signature version: ", "サポートされていないssh署名バージョン:");
		m.put("Unverified", "未確認");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "未確認のメールアドレスは上記の機能には<b>適用されません</b>");
		m.put("Unvote", "投票を取り消す");
		m.put("Unwatched. Click to watch", "ウォッチ解除。クリックしてウォッチ");
		m.put("Update", "更新");
		m.put("Update Dependencies via Renovate", "Renovateを使用して依存関係を更新");
		m.put("Update Source Branch", "ソースブランチを更新");
		m.put("Update body", "本文を更新");
		m.put("Upload", "アップロード");
		m.put("Upload Access Token Secret", "アクセストークンシークレットをアップロード");
		m.put("Upload Cache", "キャッシュをアップロード");
		m.put("Upload Files", "ファイルをアップロード");
		m.put("Upload Project Path", "プロジェクトパスをアップロード");
		m.put("Upload Strategy", "アップロード戦略");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "ダークモード用のロゴとして使用する128x128の透明なPNGファイルをアップロード");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "ライトモード用のロゴとして使用する128x128の透明なPNGファイルをアップロード");
		m.put("Upload artifacts", "アーティファクトをアップロード");
		m.put("Upload avatar", "アバターをアップロード");
		m.put("Upload should be less than {0} Mb", "アップロードは{0}MB未満である必要があります");
		m.put("Upload to Project", "プロジェクトにアップロード");
		m.put("Uploaded Caches", "アップロードされたキャッシュ");
		m.put("Uploading file", "ファイルをアップロード中");
		m.put("Url", "URL");
		m.put("Use '*' for wildcard match", "ワイルドカード一致には'*'を使用");
		m.put("Use '*' or '?' for wildcard match. Prefix with '-' to exclude", "ワイルドカード一致には'*'または'?'を使用。'-'を付けて除外");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカード一致</a>には'**'、'*'または'?'を使用");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカード一致</a>には'**'、'*'または'?'を使用。'-'を付けて除外");
		m.put("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>パスワイルドカード一致</a>には'**'、'*'または'?'を使用");
		m.put("Use '\\' to escape brackets", "括弧をエスケープするには'\\'を使用");
		m.put("Use '\\' to escape quotes", "引用符をエスケープするには'\\'を使用");
		m.put("Use @@ to reference scope in job commands to avoid being interpreted as variable", "変数として解釈されるのを避けるためにジョブコマンドでスコープを参照するには@@を使用");
		m.put("Use Avatar Service", "アバターサービスを使用");
		m.put("Use Default", "デフォルトを使用");
		m.put("Use Default Boards", "デフォルトボードを使用");
		m.put("Use For Git Operations", "Git操作に使用");
		m.put("Use Git in System Path", "システムパス内のGitを使用");
		m.put("Use Hours And Minutes Only", "時間と分のみを使用");
		m.put("Use Specified Git", "指定されたGitを使用");
		m.put("Use Specified curl", "指定されたcurlを使用");
		m.put("Use Step Template", "ステップテンプレートを使用");
		m.put("Use curl in System Path", "システムパス内のcurlを使用");
		m.put("Use default", "デフォルトを使用");
		m.put("Use default storage class", "デフォルトのストレージクラスを使用");
		m.put("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages", 
			"ジョブトークンをユーザー名として使用し、OneDevがどのビルドが${permission.equals(\"write\")? \"デプロイしている\": \"使用している\"}パッケージかを認識できるようにする");
		m.put("Use job token to tell OneDev the build publishing the package", "ジョブトークンを使用して、OneDevにパッケージを公開しているビルドを伝える");
		m.put("Use job token to tell OneDev the build pushing the chart", "ジョブトークンを使用して、OneDevにチャートをプッシュしているビルドを伝える");
		m.put("Use job token to tell OneDev the build pushing the package", "ジョブトークンを使用して、OneDevにパッケージをプッシュしているビルドを伝える");
		m.put("Use job token to tell OneDev the build using the package", "ジョブトークンを使用して、OneDevにパッケージを使用しているビルドを伝える");
		m.put("Use project dependency to retrieve artifacts from other projects", "プロジェクト依存関係を使用して他のプロジェクトからアーティファクトを取得");
		m.put("Use specified choices", "指定された選択肢を使用");
		m.put("Use specified default value", "指定されたデフォルト値を使用");
		m.put("Use specified value or job secret", "指定された値またはジョブシークレットを使用");
		m.put("Use specified values or job secrets", "指定された値またはジョブシークレットを使用");
		m.put("Use triggers to run the job automatically under certain conditions", "特定の条件下でジョブを自動的に実行するためにトリガーを使用");
		m.put("Use value of specified parameter/secret", "指定されたパラメータ/シークレットの値を使用");
		m.put("Used Heap Memory", "使用済みヒープメモリ");
		m.put("User", "ユーザー");
		m.put("User \"{0}\" unauthorized", "ユーザー \"{0}\" は認可されていません");
		m.put("User Authorization Bean", "ユーザー認可Bean");
		m.put("User Authorizations", "ユーザー認可");
		m.put("User Authorizations Bean", "ユーザー認可Bean");
		m.put("User Count", "ユーザー数");
		m.put("User Email Attribute", "ユーザーのメール属性");
		m.put("User Full Name Attribute", "ユーザーのフルネーム属性");
		m.put("User Groups Attribute", "ユーザーグループ属性");
		m.put("User Invitation", "ユーザー招待");
		m.put("User Invitation Template", "ユーザー招待テンプレート");
		m.put("User Management", "ユーザー管理");
		m.put("User Match Criteria", "ユーザー一致条件");
		m.put("User Name", "ユーザー名");
		m.put("User Principal Name", "ユーザープリンシパル名");
		m.put("User Profile", "ユーザープロファイル");
		m.put("User SSH Key Attribute", "ユーザーSSHキー属性");
		m.put("User Search Bases", "ユーザー検索ベース");
		m.put("User Search Filter", "ユーザー検索フィルター");
		m.put("User added to group", "ユーザーがグループに追加されました");
		m.put("User authorizations updated", "ユーザー認可が更新されました");
		m.put("User authorized", "ユーザーが認可されました");
		m.put("User avatar will be requested by appending a hash to this url", "ユーザーアバターは、このURLにハッシュを追加してリクエストされます");
		m.put("User can sign up if this option is enabled", "このオプションが有効になっている場合、ユーザーはサインアップできます");
		m.put("User disabled", "ユーザーが無効化されました");
		m.put("User name", "ユーザー名");
		m.put("User name already used by another account", "ユーザー名はすでに別のアカウントで使用されています");
		m.put("Users", "ユーザー");
		m.put("Users converted to service accounts successfully", "ユーザーがサービスアカウントに正常に変換されました");
		m.put("Users deleted successfully", "ユーザーが正常に削除されました");
		m.put("Users disabled successfully", "ユーザーが正常に無効化されました");
		m.put("Users enabled successfully", "ユーザーが正常に有効化されました");
		m.put("Utilities", "ユーティリティ");
		m.put("Valid signature required for head commit of this branch per branch protection rule", 
			"ブランチ保護ルールに従い、このブランチのヘッドコミットには有効な署名が必要です");
		m.put("Value", "値");
		m.put("Value Matcher", "値マッチャー");
		m.put("Value Provider", "値プロバイダー");
		m.put("Values", "値");
		m.put("Values Provider", "値プロバイダー");
		m.put("Variable", "変数");
		m.put("Verification Code", "確認コード");
		m.put("Verification email sent, please check it", "確認メールが送信されました。確認してください");
		m.put("Verify", "確認する");
		m.put("View", "表示");
		m.put("View Source", "ソースを表示");
		m.put("View source", "ソースを表示");
		m.put("View statistics", "統計を表示");
		m.put("Viewer", "ビューアー");
		m.put("Volume Mount", "ボリュームマウント");
		m.put("Volume Mounts", "ボリュームマウント");
		m.put("Vote", "投票");
		m.put("Votes", "投票数");
		m.put("WAITING", "待機中");
		m.put("WARNING:", "警告:");
		m.put("Waiting", "待機中");
		m.put("Waiting for approvals", "承認待ち");
		m.put("Waiting for test mail to come back...", "テストメールの返信を待っています...");
		m.put("Watch", "ウォッチ");
		m.put("Watch Status", "ウォッチステータス");
		m.put("Watch if involved", "関与している場合ウォッチ");
		m.put("Watch if involved (default)", "関与している場合ウォッチ (デフォルト)");
		m.put("Watch status changed", "ウォッチステータスが変更されました");
		m.put("Watch/Unwatch All Queried Issues", "クエリされたすべての課題をウォッチ/アンウォッチ");
		m.put("Watch/Unwatch All Queried Pull Requests", "クエリされたすべてのプルリクエストをウォッチ/アンウォッチ");
		m.put("Watch/Unwatch Selected Pull Requests", "選択されたプルリクエストをウォッチ/アンウォッチ");
		m.put("Watched. Click to unwatch", "ウォッチ中。クリックしてアンウォッチ");
		m.put("Watchers", "ウォッチャー");
		m.put("Web Hook", "ウェブフック");
		m.put("Web Hooks", "ウェブフック");
		m.put("Web Hooks Bean", "ウェブフックBean");
		m.put("Web hooks saved", "ウェブフックが保存されました");
		m.put("Webhook Url", "ウェブフックURL");
		m.put("Week", "週");
		m.put("When", "いつ");
		m.put("When authorize a group, the group will also be authorized with the role for all child projects", 
			"グループを承認すると、そのグループはすべての子プロジェクトに対して役割で承認されます");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"プロジェクトを承認すると、すべての子プロジェクトが割り当てられた役割で承認されます");
		m.put("When authorize a user, the user will also be authorized with the role for all child projects", 
			"ユーザーを承認すると、そのユーザーはすべての子プロジェクトに対して役割で承認されます");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"ユーザーがGitコミットの著者/コミッターであるかを判断する際、ここにリストされたすべてのメールが確認されます");
		m.put("When evaluating this template, below variables will be available:", "このテンプレートを評価する際、以下の変数が利用可能です:");
		m.put("When login via OneDev's built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database", 
			"OneDevの組み込みフォームでログインする際、ここで定義された認証者に対して送信されたユーザー資格情報を内部データベースの他にチェックできます");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"プルリクエストのターゲットブランチに新しいコミットがある場合、プルリクエストのマージコミットが再計算されます。このオプションは、以前のマージコミットで実行されたプルリクエストビルドを受け入れるかどうかを指定します。有効にすると、新しいマージコミットで必要なビルドを再実行する必要があります。この設定は必要なビルドが指定されている場合にのみ有効です");
		m.put("When this work starts", "この作業が開始されるとき");
		m.put("When {0}", "{0}のとき");
		m.put("Whether or not created issue should be confidential", "作成された課題が機密であるべきかどうか");
		m.put("Whether or not multiple issues can be linked", "複数の課題をリンクできるかどうか");
		m.put("Whether or not multiple issues can be linked on the other side. For instance sub issues on the other side means parent issue, and multiple should be false on that side if only one parent is allowed", 
			"反対側で複数の課題をリンクできるかどうか。例えば、反対側のサブ課題は親課題を意味し、親が1つしか許可されていない場合は反対側で複数をfalseにする必要があります");
		m.put("Whether or not multiple values can be specified for this field", "このフィールドに複数の値を指定できるかどうか");
		m.put("Whether or not multiple values can be specified for this param", "このパラメータに複数の値を指定できるかどうか");
		m.put("Whether or not the issue should be confidential", "課題が機密であるべきかどうか");
		m.put("Whether or not the link is asymmetric. A asymmetric link has different meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric", 
			"リンクが非対称であるかどうか。非対称リンクは異なる側から異なる意味を持ちます。例えば、「親子」リンクは非対称ですが、「関連する」リンクは対称です");
		m.put("Whether or not this field accepts empty value", "このフィールドが空の値を受け入れるかどうか");
		m.put("Whether or not this param accepts empty value", "このパラメータが空の値を受け入れるかどうか");
		m.put("Whether or not this script can be used in CI/CD jobs", "このスクリプトがCI/CDジョブで使用できるかどうか");
		m.put("Whether or not this step is optional. Execution failure of an optional step will not cause the build to fail, and successful condition of subsequent steps will not take optional step into account", 
			"このステップがオプションであるかどうか。オプションステップの実行失敗はビルドの失敗を引き起こさず、後続のステップの成功条件はオプションステップを考慮しません");
		m.put("Whether or not to allow anonymous users to access this server", "匿名ユーザーがこのサーバーにアクセスできるかどうか");
		m.put("Whether or not to allow creating root projects (project without parent)", "ルートプロジェクト（親のないプロジェクト）を作成できるかどうか");
		m.put("Whether or not to also include children of above projects", "上記のプロジェクトの子も含めるかどうか");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same machine", 
			"コンテナを実行したりイメージをビルドしたりするときに常にイメージをプルするかどうか。このオプションを有効にすると、同じマシンで実行される悪意のあるジョブによってイメージが置き換えられるのを防ぐことができます");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same node", 
			"コンテナを実行したりイメージをビルドしたりするときに常にイメージをプルするかどうか。このオプションを有効にすると、同じノードで実行される悪意のあるジョブによってイメージが置き換えられるのを防ぐことができます");
		m.put("Whether or not to be able to access time tracking info of issues", "課題のタイムトラッキング情報にアクセスできるかどうか");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities", 
			"タスク自動化目的のサービスアカウントとして作成するかどうか。サービスアカウントにはパスワードとメールアドレスがなく、その活動に対する通知は生成されません");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"タスク自動化目的のサービスアカウントとして作成するかどうか。サービスアカウントにはパスワードとメールアドレスがなく、その活動に対する通知は生成されません。<b class='text-warning'>注意:</b>サービスアカウントはエンタープライズ機能です。<a href='https://onedev.io/pricing' target='_blank'>30日間無料で試す</a>");
		m.put("Whether or not to enable code management for the project", "プロジェクトのコード管理を有効にするかどうか");
		m.put("Whether or not to enable issue management for the project", "プロジェクトの課題管理を有効にするかどうか");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project.", 
			"プルリクエストが異なるプロジェクトから開かれた場合にLFSオブジェクトを取得するかどうか");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project. If this option is enabled, git lfs command needs to be installed on OneDev server", 
			"プルリクエストが異なるプロジェクトから開かれた場合にLFSオブジェクトを取得するかどうか。このオプションを有効にすると、OneDevサーバーにgit lfsコマンドをインストールする必要があります");
		m.put("Whether or not to import forked Bitbucket repositories", "フォークされたBitbucketリポジトリをインポートするかどうか");
		m.put("Whether or not to import forked GitHub repositories", "フォークされたGitHubリポジトリをインポートするかどうか");
		m.put("Whether or not to import forked GitLab projects", "フォークされたGitLabプロジェクトをインポートするかどうか");
		m.put("Whether or not to import forked Gitea repositories", "フォークされたGiteaリポジトリをインポートするかどうか");
		m.put("Whether or not to include forked repositories", "フォークされたリポジトリを含めるかどうか");
		m.put("Whether or not to include this field when issue is initially opened. If not, you may include this field later when issue is transited to other states via issue transition rule", 
			"課題が最初に開かれたときにこのフィールドを含めるかどうか。含めない場合は、課題が課題遷移ルールを介して他の状態に遷移したときに後でこのフィールドを含めることができます");
		m.put("Whether or not to input and display estimated/spent time in hours/minutes only", "推定時間/消費時間を時間/分単位でのみ入力および表示するかどうか");
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. Make sure this executor can only be used by trusted jobs if this option is enabled", 
			"ジョブコマンドでのDocker操作をサポートするために、Dockerソケットをジョブコンテナにマウントするかどうか<br><b class='text-danger'>警告</b>: 悪意のあるジョブがマウントされたDockerソケットを操作することでOneDev全体を制御する可能性があります。このオプションを有効にする場合、このエグゼキュータが信頼できるジョブでのみ使用されることを確認してください");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"次のページでタグマッピングを事前入力するかどうか。表示するタグが多すぎる場合はこれを無効にすることをお勧めします");
		m.put("Whether or not to require this dependency to be successful", "この依存関係が成功する必要があるかどうか");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"ログインユーザーのグループを取得するかどうか。このオプションを有効にする場合は、Entra IDに登録されたアプリのトークン構成を介してグループクレームを追加してください。この場合、グループクレームはさまざまなトークンタイプを介してデフォルトオプションでグループIDを返す必要があります");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"サブモジュールを取得するかどうか。サブモジュールを取得するためのクローン資格情報を設定する方法については、<a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>このチュートリアル</a>を参照してください");
		m.put("Whether or not to run this step inside container", "このステップをコンテナ内で実行するかどうか");
		m.put("Whether or not to scan recursively in above paths", "上記のパスで再帰的にスキャンするかどうか");
		m.put("Whether or not to send notifications for events generated by yourself", "自分自身が生成したイベントの通知を送信するかどうか");
		m.put("Whether or not to send notifications to issue watchers for this change", "この変更について課題ウォッチャーに通知を送信するかどうか");
		m.put("Whether or not to show branch/tag column", "ブランチ/タグ列を表示するかどうか");
		m.put("Whether or not to show duration column", "期間列を表示するかどうか");
		m.put("Whether or not to use user avatar from a public service", "公開サービスからユーザーアバターを使用するかどうか");
		m.put("Whether or not use force option to overwrite changes in case ref updating can not be fast-forwarded", 
			"参照の更新が高速フォワードできない場合に変更を上書きするために強制オプションを使用するかどうか");
		m.put("Whether or not user can remove own account", "ユーザーが自分のアカウントを削除できるかどうか");
		m.put("Whether the password must contain at least one lowercase letter", "パスワードに少なくとも1つの小文字を含める必要があるかどうか");
		m.put("Whether the password must contain at least one number", "パスワードに少なくとも1つの数字を含める必要があるかどうか");
		m.put("Whether the password must contain at least one special character", "パスワードに少なくとも1つの特殊文字を含める必要があるかどうか");
		m.put("Whether the password must contain at least one uppercase letter", "パスワードに少なくとも1つの大文字を含める必要があるかどうか");
		m.put("Whole Word", "全単語");
		m.put("Widget", "ウィジェット");
		m.put("Widget Tab", "ウィジェットタブ");
		m.put("Widget Timesheet Setting", "ウィジェットタイムシート設定");
		m.put("Will be prompted to set up two-factor authentication upon next login", "次回のログイン時に二要素認証の設定を求められます");
		m.put("Will be transcoded to UTF-8", "UTF-8にトランスコードされます");
		m.put("Window", "ウィンドウ");
		m.put("Window Memory", "ウィンドウメモリ");
		m.put("With current number of users ({0}), the subscription will be active until <b>{1}</b>", 
			"現在のユーザー数（{0}）で、サブスクリプションは<b>{1}</b>まで有効です");
		m.put("Workflow reconciliation completed", "ワークフローの調整が完了しました");
		m.put("Working Directory", "作業ディレクトリ");
		m.put("Write", "書き込み");
		m.put("YAML", "YAML");
		m.put("Yes", "はい");
		m.put("You are not member of discord server", "あなたはDiscordサーバーのメンバーではありません");
		m.put("You are rebasing source branch on top of target branch", "ソースブランチをターゲットブランチの上にリベースしています");
		m.put("You are viewing a subset of all changes. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">show all changes</a>", 
			"すべての変更のサブセットを表示しています。<a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">すべての変更を表示</a>");
		m.put("You can also achieve this by adding a build docker image step to your CI/CD job and configuring the built-in registry login with an access token secret that has package write permissions", 
			"CI/CDジョブにビルドDockerイメージステップを追加し、パッケージ書き込み権限を持つアクセストークンシークレットで組み込みレジストリログインを設定することでこれを達成できます");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "未確認の<a wicket:id=\"hasUnverifiedLink\">メールアドレス</a>があります");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "入力ボックスにファイル/画像をドロップするか、クリップボードから画像を貼り付けることもできます");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"プロジェクトを初期化するには、<a wicket:id=\"addFiles\" class=\"link-primary\">ファイルを追加</a>、<a wicket:id=\"setupBuildSpec\" class=\"link-primary\">ビルド仕様を設定</a>、または<a wicket:id=\"pushInstructions\" class=\"link-primary\">既存のリポジトリをプッシュ</a>してください");
		m.put("You selected to delete branch \"{0}\"", "ブランチ「{0}」を削除することを選択しました");
		m.put("You will be notified of any activities", "すべての活動について通知されます");
		m.put("You've been logged out", "ログアウトされました");
		m.put("YouTrack API URL", "YouTrack API URL");
		m.put("YouTrack Issue Field", "YouTrack課題フィールド");
		m.put("YouTrack Issue Link", "YouTrack課題リンク");
		m.put("YouTrack Issue State", "YouTrack課題状態");
		m.put("YouTrack Issue Tag", "YouTrack課題タグ");
		m.put("YouTrack Login Name", "YouTrackログイン名");
		m.put("YouTrack Password or Access Token", "YouTrackパスワードまたはアクセストークン");
		m.put("YouTrack Project", "YouTrackプロジェクト");
		m.put("YouTrack Projects to Import", "インポートするYouTrackプロジェクト");
		m.put("Your email address is now verified", "メールアドレスが確認されました");
		m.put("Your primary email address is not verified", "プライマリメールアドレスが確認されていません");
		m.put("[Any state]", "[任意の状態]");
		m.put("[Reset Password] Please Reset Your OneDev Password", "[パスワードリセット] OneDevパスワードをリセットしてください");
		m.put("a boolean indiciating whether or not topic comment can be created directly by replying the email", 
			"トピックコメントがメール返信によって直接作成可能かどうかを示すブール値");
		m.put("a new agent token will be generated each time this button is pressed", "このボタンを押すたびに新しいエージェントトークンが生成されます");
		m.put("a string representing body of the event. May be <code>null</code>", "イベントの本文を表す文字列。<code>null</code>の場合があります");
		m.put("a string representing event detail url", "イベント詳細URLを表す文字列");
		m.put("a string representing summary of the event", "イベントの概要を表す文字列");
		m.put("access [{0}]", "アクセス[{0}]");
		m.put("active", "アクティブ");
		m.put("add another order", "別の注文を追加");
		m.put("adding .onedev-buildspec.yml", ".onedev-buildspec.ymlを追加中");
		m.put("after specified date", "指定された日付以降");
		m.put("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information.  A <code>null</code> value means that the notification can not be unsubscribed", 
			"通知解除情報を保持する<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>オブジェクト</a>。<code>null</code>値は通知を解除できないことを意味します");
		m.put("and more", "さらに多く");
		m.put("archived", "アーカイブ済み");
		m.put("artifacts", "アーティファクト");
		m.put("assign to me", "自分に割り当てる");
		m.put("authored by", "作成者");
		m.put("backlog ", "バックログ");
		m.put("base", "ベース");
		m.put("before specified date", "指定された日付以前");
		m.put("branch the build commit is merged into", "ビルドコミットがマージされるブランチ");
		m.put("branch the job is running against", "ジョブが実行されているブランチ");
		m.put("branch {0}", "ブランチ{0}");
		m.put("branches", "ブランチ");
		m.put("build", "ビルド");
		m.put("build is successful for any job and branch", "任意のジョブとブランチでビルドが成功");
		m.put("build is successful for any job on branches \"{0}\"", "ブランチ「{0}」で任意のジョブでビルドが成功");
		m.put("build is successful for jobs \"{0}\" on any branch", "ジョブ「{0}」で任意のブランチでビルドが成功");
		m.put("build is successful for jobs \"{0}\" on branches \"{1}\"", "ジョブ「{0}」でブランチ「{1}」でビルドが成功");
		m.put("builds", "ビルド");
		m.put("cURL Example", "cURLの例");
		m.put("choose a color for this state", "この状態の色を選択してください");
		m.put("cluster:lead", "リード");
		m.put("cmd-k to show command palette", "cmd-kでコマンドパレットを表示");
		m.put("code commit", "コードコミット");
		m.put("code is committed", "コードがコミットされました");
		m.put("code is committed to branches \"{0}\"", "ブランチ「{0}」にコードがコミットされる");
		m.put("code is committed to branches \"{0}\" with message \"{1}\"", "コードがブランチ \"{0}\" にメッセージ \"{1}\" でコミットされました");
		m.put("code is committed with message \"{0}\"", "コードがメッセージ \"{0}\" でコミットされました");
		m.put("commit message contains", "コミットメッセージに含まれる");
		m.put("commits", "コミット");
		m.put("committed by", "作成者");
		m.put("common", "共通");
		m.put("common ancestor", "共通祖先");
		m.put("container:image", "イメージ");
		m.put("copy", "コピー");
		m.put("ctrl-k to show command palette", "ctrl-kでコマンドパレットを表示");
		m.put("curl Command Line", "curlコマンドライン");
		m.put("curl Path", "curlパス");
		m.put("default", "デフォルト");
		m.put("descending", "降順");
		m.put("disabled", "無効");
		m.put("does not have any value of", "いずれの値も持たない");
		m.put("duration", "期間");
		m.put("enclose with ~ to query hash/message", "ハッシュ/メッセージをクエリするには~で囲む");
		m.put("enclose with ~ to query job/version", "ジョブ/バージョンをクエリするには~で囲む");
		m.put("enclose with ~ to query name/ip/os", "名前/IP/OSをクエリするには~で囲む");
		m.put("enclose with ~ to query name/path", "名前/パスをクエリするには~で囲む");
		m.put("enclose with ~ to query name/version", "名前/バージョンをクエリするには~で囲む");
		m.put("enclose with ~ to query path/content/reply", "パス/コンテンツ/返信をクエリするには~で囲む");
		m.put("enclose with ~ to query title/description/comment", "タイトル/説明/コメントをクエリするには~で囲む");
		m.put("exclude", "除外");
		m.put("false", "false");
		m.put("files with ext \"{0}\"", "拡張子「{0}」のファイル");
		m.put("find build by number", "番号でビルドを検索");
		m.put("find build with this number", "この番号でビルドを検索");
		m.put("find issue by number", "番号で課題を検索");
		m.put("find pull request by number", "番号でプルリクエストを検索");
		m.put("find pull request with this number", "この番号でプルリクエストを検索");
		m.put("forked from <a wicket:id=\"forkedFrom\"></a>", "<a wicket:id=\"forkedFrom\"></a>からフォークされました");
		m.put("found 1 agent", "1 エージェントが見つかりました");
		m.put("found 1 build", "1 ビルドが見つかりました");
		m.put("found 1 comment", "1 コメントが見つかりました");
		m.put("found 1 issue", "1 課題が見つかりました");
		m.put("found 1 package", "1 パッケージが見つかりました");
		m.put("found 1 project", "1 プロジェクトが見つかりました");
		m.put("found 1 pull request", "1 プルリクエストが見つかりました");
		m.put("found 1 user", "1 ユーザーが見つかりました");
		m.put("found {0} agents", "{0} エージェントが見つかりました");
		m.put("found {0} builds", "{0} ビルドが見つかりました");
		m.put("found {0} comments", "{0} コメントが見つかりました");
		m.put("found {0} issues", "{0} 課題が見つかりました");
		m.put("found {0} packages", "{0} パッケージが見つかりました");
		m.put("found {0} projects", "{0} プロジェクトが見つかりました");
		m.put("found {0} pull requests", "{0} プルリクエストが見つかりました");
		m.put("found {0} users", "{0} ユーザーが見つかりました");
		m.put("has any value of", "任意の値を持っています");
		m.put("head", "ヘッド");
		m.put("in current commit", "現在のコミット内");
		m.put("ineffective", "無効");
		m.put("inherited", "継承済み");
		m.put("initial", "初期");
		m.put("is empty", "空です");
		m.put("is not empty", "空ではありません");
		m.put("issue", "課題");
		m.put("issue:Number", "番号");
		m.put("issues", "課題");
		m.put("job", "ジョブ");
		m.put("key ID: ", "キーID:");
		m.put("lines", "行");
		m.put("link:Multiple", "複数");
		m.put("log", "ログ");
		m.put("manage job", "ジョブを管理");
		m.put("markdown:heading", "見出し");
		m.put("markdown:image", "画像");
		m.put("may not be empty", "空であってはなりません");
		m.put("merged", "マージ済み");
		m.put("month:Apr", "4月");
		m.put("month:Aug", "8月");
		m.put("month:Dec", "12月");
		m.put("month:Feb", "2月");
		m.put("month:Jan", "1月");
		m.put("month:Jul", "7月");
		m.put("month:Jun", "6月");
		m.put("month:Mar", "3月");
		m.put("month:May", "5月");
		m.put("month:Nov", "11月");
		m.put("month:Oct", "10月");
		m.put("month:Sep", "9月");
		m.put("n/a", "該当なし");
		m.put("new field", "新しいフィールド");
		m.put("no activity for {0} days", "{0} 日間活動なし");
		m.put("on file {0}", "ファイル {0} 上");
		m.put("opened", "開かれました");
		m.put("opened <span wicket:id=\"submitDate\"></span>", "開かれました <span wicket:id=\"submitDate\"></span>");
		m.put("or match another value", "または別の値に一致");
		m.put("order more", "さらに注文");
		m.put("outdated", "古い情報");
		m.put("pack", "パック");
		m.put("package", "パッケージ");
		m.put("packages", "パッケージ");
		m.put("personal", "個人");
		m.put("pipeline", "パイプライン");
		m.put("project of the running job", "実行中のジョブのプロジェクト");
		m.put("property", "プロパティ");
		m.put("pull request", "プルリクエスト");
		m.put("pull request #{0}", "プルリクエスト #{0}");
		m.put("pull request and code review", "プルリクエストとコードレビュー");
		m.put("pull request to any branch is discarded", "任意のブランチへのプルリクエストは破棄されました");
		m.put("pull request to any branch is merged", "任意のブランチへのプルリクエストはマージされました");
		m.put("pull request to any branch is opened", "任意のブランチへのプルリクエストは開かれました");
		m.put("pull request to branches \"{0}\" is discarded", "ブランチ \"{0}\" へのプルリクエストは破棄されました");
		m.put("pull request to branches \"{0}\" is merged", "ブランチ \"{0}\" へのプルリクエストはマージされました");
		m.put("pull request to branches \"{0}\" is opened", "ブランチ \"{0}\" へのプルリクエストは開かれました");
		m.put("pull requests", "プルリクエスト");
		m.put("reconciliation (need administrator permission)", "調整 (管理者権限が必要)");
		m.put("reports", "レポート");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> object to be notified", 
			"通知される <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>ビルド</a> オブジェクトを表します");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk", 
			"サービスデスクを介して開かれる <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>課題</a> を表します");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified", 
			"通知される <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>課題</a> オブジェクトを表します");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> object to be notified", 
			"通知される <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>パッケージ</a> オブジェクトを表します");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified", 
			"通知される <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>プルリクエスト</a> オブジェクトを表します");
		m.put("represents the <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> object to be notified", 
			"通知される <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>コミット</a> オブジェクトを表します");
		m.put("represents the exception encountered when open issue via service desk", "サービスデスクを介して課題を開く際に発生した例外を表します");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>", 
			"購読解除された <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>課題</a> を表します");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>", 
			"購読解除された <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>プルリクエスト</a> を表します");
		m.put("request to change", "変更を要求");
		m.put("root", "ルート");
		m.put("root url of OneDev server", "OneDev サーバーのルート URL");
		m.put("run job", "ジョブを実行");
		m.put("search in this revision will be accurate after indexed", "このリビジョンでの検索はインデックス化後に正確になります");
		m.put("service", "サービス");
		m.put("severity:CRITICAL", "重大");
		m.put("severity:HIGH", "高");
		m.put("severity:LOW", "低");
		m.put("severity:MEDIUM", "中");
		m.put("skipped {0} lines", "{0} 行をスキップしました");
		m.put("space", "スペース");
		m.put("state of an issue is transited", "課題の状態が遷移される");
		m.put("step template", "ステップテンプレート");
		m.put("submit", "送信");
		m.put("tag the job is running against", "ジョブが実行されているタグ");
		m.put("tag {0}", "tag {0}");
		m.put("tags", "タグ");
		m.put("the url to set up user account", "ユーザーアカウントを設定するためのURL");
		m.put("time aggregation link", "時間集計リンク");
		m.put("touching specified path", "指定されたパスに触れる");
		m.put("transit manually by any user", "任意のユーザーによる手動遷移");
		m.put("transit manually by any user of roles \"{0}\"", "ロール\"{0}\"の任意のユーザーによる手動遷移");
		m.put("true", "true");
		m.put("true for html version, false for text version", "HTMLバージョンの場合はtrue、テキストバージョンの場合はfalse");
		m.put("up to date", "最新");
		m.put("url following which to verify email address", "メールアドレスを確認するためのURL");
		m.put("url to reset password", "パスワードをリセットするためのURL");
		m.put("value needs to be enclosed in brackets", "値は角括弧で囲む必要があります");
		m.put("value needs to be enclosed in parenthesis", "値は丸括弧で囲む必要があります");
		m.put("value should be quoted", "値は引用符で囲む必要があります");
		m.put("w%02d", "w%02d");
		m.put("week:Fri", "金");
		m.put("week:Mon", "月");
		m.put("week:Sat", "土");
		m.put("week:Sun", "日");
		m.put("week:Thu", "木");
		m.put("week:Tue", "火");
		m.put("week:Wed", "水");
		m.put("widget:Tabs", "タブ");
		m.put("you may show this page later via incompatibilities link in help menu", "ヘルプメニューの互換性リンクから後でこのページを表示できます");
		m.put("{0} Month(s)", "{0} ヶ月");
		m.put("{0} activities on {1}", "{0} の活動 {1} において");
		m.put("{0} additions & {1} deletions", "{0} 追加 & {1} 削除");
		m.put("{0} ahead", "{0} 先行");
		m.put("{0} behind", "{0} 遅延");
		m.put("{0} branches", "{0} ブランチ");
		m.put("{0} build(s)", "{0} ビルド");
		m.put("{0} child projects", "{0} 子プロジェクト");
		m.put("{0} commits", "{0} コミット");
		m.put("{0} commits ahead of base branch", "{0} ベースブランチより先行しているコミット");
		m.put("{0} commits behind of base branch", "{0} ベースブランチより遅れているコミット");
		m.put("{0} day", "{0} 日");
		m.put("{0} days", "{0} 日間");
		m.put("{0} edited {1}", "{0} が編集 {1}");
		m.put("{0} files", "{0} ファイル");
		m.put("{0} forks", "{0} フォーク");
		m.put("{0} hour", "{0} 時間");
		m.put("{0} hours", "{0} 時間");
		m.put("{0} inaccessible activities", "{0} アクセス不可の活動");
		m.put("{0} minute", "{0} 分");
		m.put("{0} minutes", "{0} 分間");
		m.put("{0} reviewed", "{0} がレビュー済み");
		m.put("{0} second", "{0} 秒");
		m.put("{0} seconds", "{0} 秒間");
		m.put("{0} tags", "{0} タグ");
		m.put("{0}d", "{0}日");
		m.put("{0}h", "{0}時間");
		m.put("{0}m", "{0}分");
		m.put("{0}s", "{0}秒");
		m.put("{0}w", "{0}週");
		m.put("{javax.validation.constraints.NotEmpty.message}", "{javax.validation.constraints.NotEmpty.message}");
		m.put("{javax.validation.constraints.NotNull.message}", "{javax.validation.constraints.NotNull.message}");
		m.put("{javax.validation.constraints.Size.message}", "{javax.validation.constraints.Size.message}");
	}
		
	@Override
	protected Map<String, String> getContents() {
		return m;		
	}
	
}
