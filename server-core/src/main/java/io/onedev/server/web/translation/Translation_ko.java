package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_ko extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init(m);
		Translation.watchUpdate(Translation_ko.class, () -> {
			init(m);
		});
	}

	@SystemPrompt("You are good at translating from English to Korean in DevOps software area.")
	public static void init(Map<String, String> m) {
		m.clear();
		m.put(" Project path can be omitted if reference from current project", "프로젝트 경로는 현재 프로젝트에서 참조하는 경우 생략할 수 있습니다");
		m.put("'..' is not allowed in the directory", "'..'은 디렉토리에서 허용되지 않습니다");
		m.put("(* = any string, ? = any character)", "(* = 임의의 문자열, ? = 임의의 문자)");
		m.put("(on behalf of <b>{0}</b>)", "(<b>{0}</b>를 대신하여)");
		m.put("** Enterprise edition is disabled as the subscription was expired. Renew to enable **", 
			"** 구독이 만료되어 엔터프라이즈 에디션이 비활성화되었습니다. 활성화하려면 갱신하세요 **");
		m.put("** Enterprise edition is disabled as the trial subscription was expired, order subscription to enable or contact support@onedev.io if you need to extend your trial **", 
			"** 엔터프라이즈 에디션은 체험 구독이 만료되어 비활성화되었습니다. 활성화하려면 구독을 주문하거나 체험 연장을 원하시면 support@onedev.io로 문의하세요 **");
		m.put("** Enterprise edition is disabled as there is no remaining user months. Order more to enable **", 
			"** 엔터프라이즈 에디션은 남은 사용자 월이 없어 비활성화되었습니다. 활성화하려면 추가 주문하세요 **");
		m.put("1. To use this package, add below to project pom.xml", "1. 이 패키지를 사용하려면 아래 내용을 프로젝트 pom.xml에 추가하세요");
		m.put("1. Use below repositories in project pom.xml", "1. 프로젝트 pom.xml에 아래 저장소를 사용하세요");
		m.put("1w 1d 1h 1m", "1주 1일 1시간 1분");
		m.put("2. Add below to <code>$HOME/.m2/settings.xml</code> if you want to deploy from command line", 
			"2. 명령줄에서 배포하려면 <code>$HOME/.m2/settings.xml</code>에 아래 내용을 추가하세요");
		m.put("2. Also add below to $HOME/.m2/settings.xml if you want to compile project from command line", 
			"2. 명령줄에서 프로젝트를 컴파일하려면 $HOME/.m2/settings.xml에 아래 내용을 추가하세요");
		m.put("3. For CI/CD job, it is more convenient to use a custom settings.xml, for instance via below code in a command step:", 
			"3. CI/CD 작업에서는 사용자 정의 settings.xml을 사용하는 것이 더 편리합니다. 예를 들어 명령 단계에서 아래 코드를 통해 가능합니다:");
		m.put("6-digits passcode", "6자리 인증 코드");
		m.put("7 days", "7일");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">사용자</a>의 비밀번호를 재설정합니다");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">사용자</a>의 이메일을 확인합니다");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub 스타일 마크다운</a>이 허용되며, <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid 및 katex 지원</a>이 포함됩니다.");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>이벤트 객체</a>가 알림을 트리거합니다");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>알림</a>을 표시합니다");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a> overdue", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>스톱워치</a>가 기한을 초과했습니다");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a>가 <span wicket:id=\"date\"></span>에 커밋했습니다");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed with <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a>가 <a wicket:id=\"committer\" class=\"name link-gray\"></a>와 함께 <span wicket:id=\"date\"></span>에 커밋했습니다");
		m.put("<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depends on me", "<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a>가 나를 의존합니다");
		m.put("<a wicket:id=\"removePassword\">Remove password</a> to force the user to authenticate via external system", 
			"<a wicket:id=\"removePassword\">비밀번호 제거</a>를 통해 사용자가 외부 시스템을 통해 인증하도록 강제합니다");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">복구 코드로 확인</a>을 통해 TOTP 인증기에 접근할 수 없는 경우를 대비합니다");
		m.put("<b class='text-danger'>NOTE: </b> This requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>참고: </b> 이는 엔터프라이즈 구독이 필요합니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>을 시도해보세요");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>참고: </b> 이 단계는 엔터프라이즈 구독이 필요합니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>을 시도해보세요");
		m.put("<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>참고: </b>SendGrid 통합은 엔터프라이즈 기능입니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>을 시도해보세요");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>참고: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>시간 추적</a>은 엔터프라이즈 기능입니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>을 시도해보세요");
		m.put("<b>NOTE: </b> Service desk only takes effect if <a wicket:id=\"mailConnector\">mail service</a> is defined and its <tt>check incoming email</tt> option is enabled. Also <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>sub addressing</a> needs to be enabled for the system email address. Check <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>this tutorial</a> for details", 
			"<b>참고: </b> 서비스 데스크는 <a wicket:id=\"mailConnector\">메일 서비스</a>가 정의되고 <tt>수신 이메일 확인</tt> 옵션이 활성화된 경우에만 작동합니다. 또한 시스템 이메일 주소에 대해 <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>서브 어드레싱</a>이 활성화되어야 합니다. 자세한 내용은 <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>이 튜토리얼</a>을 확인하세요");
		m.put("<b>NOTE:</b> Batch editing issues will not cause state transitions of other issues even if transition rule matches", 
			"<b>참고:</b> 문제를 일괄 편집해도 다른 문제의 상태 전환은 규칙이 일치하더라도 발생하지 않습니다");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>프로젝트 소유자</b>는 프로젝트에 대한 전체 권한을 가진 내장 역할입니다");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>팁: </b> <tt>@</tt>를 입력하여 <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>변수를 삽입</a>하세요. <tt>@@</tt>는 리터럴 <tt>@</tt>를 사용합니다");
		m.put("<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>파일 검색</span> <span class='font-size-sm text-muted'>기본 브랜치에서</span></div>");
		m.put("<div><span>Search Symbols</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>심볼 검색</span> <span class='font-size-sm text-muted'>기본 브랜치에서</span></div>");
		m.put("<div><span>Search Text</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>텍스트 검색</span> <span class='font-size-sm text-muted'>기본 브랜치에서</span></div>");
		m.put("<i>No Name</i>", "<i>이름 없음</i>");
		m.put("<span class=\"keycap ml-3 mr-1\">esc</span> to close", "<span class=\"keycap ml-3 mr-1\">esc</span> 닫기");
		m.put("<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> to move", 
			"<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> 이동");
		m.put("<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> to navigate. <span class=\"keycap\">Esc</span> to close", 
			"<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span>로 탐색합니다. <span class=\"keycap\">Esc</span>로 닫습니다");
		m.put("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete.", 
			"<span class='keycap'>Tab</span> 또는 <span class='keycap'>Enter</span>로 완료합니다.");
		m.put("<span class='keycap'>Tab</span> to complete.", "<span class='keycap'>Tab</span>으로 완료합니다.");
		m.put("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>", "<span class='text-nowrap'><span class='keycap'>Enter</span> 이동</span>");
		m.put("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>", "<span class='text-nowrap'><span class='keycap'>Tab</span> 검색</span>");
		m.put("<span wicket:id=\"activityCount\"></span> activities", "<span wicket:id=\"activityCount\"></span> 활동");
		m.put("<svg class='icon mr-2'><use xlink:href='%s'/></svg> Define job secrets to be used in build spec. Secrets with <b>same name</b> can be defined. For a particular name, the first authorized secret with that name will be used (search in current project first, then search in parent projects). Note that secret value containing line breaks or less than <b>%d</b> characters will not be masked in build log", 
			"<svg class='icon mr-2'><use xlink:href='%s'/></svg> 빌드 사양에서 사용할 작업 비밀을 정의하세요. <b>같은 이름</b>의 비밀을 정의할 수 있습니다. 특정 이름에 대해, 해당 이름으로 승인된 첫 번째 비밀이 사용됩니다(현재 프로젝트에서 먼저 검색한 후 상위 프로젝트에서 검색). 비밀 값에 줄 바꿈이 포함되거나 <b>%d</b>자 미만인 경우 빌드 로그에서 마스킹되지 않습니다");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here", 
			"여기에 <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java 패턴</a>이 필요합니다");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"커밋 메시지 푸터를 검증하기 위한 <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java 정규 표현식</a>");
		m.put("A child project with name \"{0}\" already exists under \"{1}\"", "\"{1}\" 아래에 이름이 \"{0}\"인 하위 프로젝트가 이미 존재합니다");
		m.put("A file exists where you’re trying to create a subdirectory. Choose a new path and try again..", 
			"하위 디렉토리를 생성하려는 위치에 파일이 존재합니다. 새로운 경로를 선택하고 다시 시도하세요.");
		m.put("A path with same name already exists.Please choose a different name and try again.", 
			"같은 이름의 경로가 이미 존재합니다. 다른 이름을 선택하고 다시 시도하세요.");
		m.put("A pull request is open for this change", "이 변경에 대한 풀 리퀘스트가 열려 있습니다");
		m.put("A root project with name \"{0}\" already exists", "이름이 \"{0}\"인 루트 프로젝트가 이미 존재합니다");
		m.put("A {0} used as body of address verification email", "주소 확인 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of build notification email", "빌드 알림 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of commit notification email", "커밋 알림 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of feedback email when failed to open issue via service desk", "서비스 데스크를 통해 문제를 열지 못했을 때 피드백 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of feedback email when issue is opened via service desk", "서비스 데스크를 통해 문제가 열렸을 때 피드백 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of feedback email when unsubscribed from issue notification", "문제 알림에서 구독 취소 시 피드백 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of feedback email when unsubscribed from pull request notification", 
			"풀 리퀘스트 알림에서 구독 취소 시 피드백 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of issue stopwatch overdue notification email", "문제 스톱워치 기한 초과 알림 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of package notification email", "패키지 알림 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of password reset email", "비밀번호 재설정 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of system alert email", "시스템 경고 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of user invitation email", "사용자 초대 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of various issue notification emails", "다양한 문제 알림 이메일 본문으로 사용된 {0}");
		m.put("A {0} used as body of various pull request notification emails", "다양한 풀 리퀘스트 알림 이메일 본문으로 사용된 {0}");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"예를 들어, <tt>https://your-domain.atlassian.net/rest/api/3</tt>와 같은 JIRA 클라우드 인스턴스의 API URL");
		m.put("Able to merge without conflicts", "충돌 없이 병합 가능");
		m.put("Absolute or relative url of the image", "이미지의 절대 또는 상대 URL");
		m.put("Absolute or relative url of the link", "링크의 절대 또는 상대 URL");
		m.put("Access Anonymously", "익명으로 접근");
		m.put("Access Build Log", "빌드 로그 접근");
		m.put("Access Build Pipeline", "빌드 파이프라인 접근");
		m.put("Access Build Reports", "빌드 보고서 접근");
		m.put("Access Confidential Issues", "기밀 문제 접근");
		m.put("Access Time Tracking", "시간 추적 접근");
		m.put("Access Token", "액세스 토큰");
		m.put("Access Token Authorization Bean", "액세스 토큰 인증 빈");
		m.put("Access Token Edit Bean", "액세스 토큰 편집 빈");
		m.put("Access Token Secret", "액세스 토큰 비밀");
		m.put("Access Token for Target Project", "대상 프로젝트에 대한 액세스 토큰");
		m.put("Access Tokens", "액세스 토큰들");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"액세스 토큰은 API 접근 및 저장소 풀/푸시를 위한 것입니다. 웹 UI에 로그인하는 데 사용할 수 없습니다");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"액세스 토큰은 API 접근 또는 저장소 풀/푸시를 위한 것입니다. 웹 UI에 로그인하는 데 사용할 수 없습니다");
		m.put("Access token regenerated successfully", "액세스 토큰이 성공적으로 재생성되었습니다");
		m.put("Access token regenerated, make sure to update the token at agent side", "액세스 토큰이 재생성되었습니다. 에이전트 측에서 토큰을 업데이트해야 합니다");
		m.put("Account Email", "계정 이메일");
		m.put("Account Name", "계정 이름");
		m.put("Account is disabled", "계정이 비활성화되었습니다");
		m.put("Account set up successfully", "계정이 성공적으로 설정되었습니다");
		m.put("Active Directory", "액티브 디렉토리");
		m.put("Active Since", "활성화된 이후");
		m.put("Activities", "활동");
		m.put("Activity by type", "유형별 활동");
		m.put("Add", "추가");
		m.put("Add Executor", "실행자 추가");
		m.put("Add GPG key", "GPG 키 추가");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "이 사용자가 서명한 커밋/태그를 확인하려면 여기에 GPG 키를 추가하세요");
		m.put("Add GPG keys here to verify commits/tags signed by you", "본인이 서명한 커밋/태그를 확인하려면 여기에 GPG 키를 추가하세요");
		m.put("Add GPG public keys to be trusted here. Commits signed with trusted keys will be shown as verified.", 
			"신뢰할 수 있는 GPG 공개 키를 여기에 추가하세요. 신뢰할 수 있는 키로 서명된 커밋은 확인된 것으로 표시됩니다.");
		m.put("Add Issue...", "문제 추가...");
		m.put("Add Issues to Iteration", "반복에 문제 추가");
		m.put("Add New", "새로 추가");
		m.put("Add New Board", "새 보드 추가");
		m.put("Add New Email Address", "새 이메일 주소 추가");
		m.put("Add New Timesheet", "새로운 근무시간표 추가");
		m.put("Add Rule", "규칙 추가");
		m.put("Add SSH key", "SSH 키 추가");
		m.put("Add SSO provider", "SSO 제공자 추가");
		m.put("Add Spent Time", "소요 시간 추가");
		m.put("Add Timesheet", "근무시간표 추가");
		m.put("Add Widget", "위젯 추가");
		m.put("Add a GPG Public Key", "GPG 공개 키 추가");
		m.put("Add a SSH Key", "SSH 키 추가");
		m.put("Add a package source like below", "아래와 같은 패키지 소스 추가");
		m.put("Add after", "이후 추가");
		m.put("Add agent", "에이전트 추가");
		m.put("Add all cards to specified iteration", "모든 카드를 지정된 반복에 추가");
		m.put("Add all commits from source branch to target branch with a merge commit", "소스 브랜치의 모든 커밋을 병합 커밋으로 대상 브랜치에 추가");
		m.put("Add assignee...", "담당자 추가...");
		m.put("Add before", "이전에 추가");
		m.put("Add below to allow accessing via http protocol in new Maven versions", "새로운 Maven 버전에서 HTTP 프로토콜을 통해 액세스할 수 있도록 아래를 추가");
		m.put("Add child project", "하위 프로젝트 추가");
		m.put("Add comment", "댓글 추가");
		m.put("Add comment on this selection", "이 선택에 댓글 추가");
		m.put("Add custom field", "사용자 정의 필드 추가");
		m.put("Add dashboard", "대시보드 추가");
		m.put("Add default issue board", "기본 이슈 보드 추가");
		m.put("Add files to current directory", "현재 디렉토리에 파일 추가");
		m.put("Add files via upload", "업로드를 통해 파일 추가");
		m.put("Add groovy script", "Groovy 스크립트 추가");
		m.put("Add issue description template", "이슈 설명 템플릿 추가");
		m.put("Add issue link", "이슈 링크 추가");
		m.put("Add issue state", "이슈 상태 추가");
		m.put("Add issue state transition", "이슈 상태 전환 추가");
		m.put("Add link", "링크 추가");
		m.put("Add new", "새로 추가");
		m.put("Add new card to this column", "이 열에 새 카드 추가");
		m.put("Add new file", "새 파일 추가");
		m.put("Add new import", "새 가져오기 추가");
		m.put("Add new issue creation setting", "새 이슈 생성 설정 추가");
		m.put("Add new job dependency", "새 작업 종속성 추가");
		m.put("Add new param", "새 매개변수 추가");
		m.put("Add new post-build action", "새 후처리 작업 추가");
		m.put("Add new project dependency", "새 프로젝트 종속성 추가");
		m.put("Add new step", "새 단계 추가");
		m.put("Add new trigger", "새 트리거 추가");
		m.put("Add project", "프로젝트 추가");
		m.put("Add reviewer...", "검토자 추가...");
		m.put("Add to batch to commit with other suggestions later", "나중에 다른 제안과 함께 커밋할 배치에 추가");
		m.put("Add to group...", "그룹에 추가...");
		m.put("Add to iteration...", "반복에 추가...");
		m.put("Add user to group...", "그룹에 사용자 추가...");
		m.put("Add value", "값 추가");
		m.put("Add {0}", "{0} 추가");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "커밋 \"{0}\" 추가 (<i class='text-danger'>저장소에 없음</i>)");
		m.put("Added commit \"{0}\" ({1})", "커밋 \"{0}\" 추가 ({1})");
		m.put("Added to group", "그룹에 추가됨");
		m.put("Additions", "추가 사항");
		m.put("Administration", "관리");
		m.put("Administrative permission over a project", "프로젝트에 대한 관리 권한");
		m.put("Advanced Search", "고급 검색");
		m.put("After modification", "수정 후");
		m.put("Agent", "에이전트");
		m.put("Agent Attribute", "에이전트 속성");
		m.put("Agent Count", "에이전트 수");
		m.put("Agent Edit Bean", "에이전트 편집 빈");
		m.put("Agent Selector", "에이전트 선택기");
		m.put("Agent is designed to be maintenance free. Once connected to server, it will be updated automatically upon server upgrade", 
			"에이전트는 유지보수가 필요 없도록 설계되었습니다. 서버에 연결되면 서버 업그레이드 시 자동으로 업데이트됩니다");
		m.put("Agent removed", "에이전트 제거됨");
		m.put("Agent tokens are used to authorize agents. It should be configured via environment variable <tt>agentToken</tt> if agent runs as docker container, or property <tt>agentToken</tt> in file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> if agent runs on bare metal/virtual machine. A token will be in-use and removed from this list if agent using it connects to server", 
			"에이전트 토큰은 에이전트를 인증하는 데 사용됩니다. Docker 컨테이너로 실행되는 경우 환경 변수 <tt>agentToken</tt>을 통해, 또는 베어 메탈/가상 머신에서 실행되는 경우 파일 <tt>&lt;agent dir&gt;/conf/agent.properties</tt>의 속성 <tt>agentToken</tt>을 통해 구성해야 합니다. 에이전트가 서버에 연결되면 토큰이 사용 중으로 표시되고 이 목록에서 제거됩니다");
		m.put("Agents", "에이전트들");
		m.put("Agents can be used to execute jobs on remote machines. Once started it will update itself from server automatically when necessary", 
			"에이전트는 원격 머신에서 작업을 실행하는 데 사용할 수 있습니다. 시작되면 필요할 때 서버에서 자동으로 업데이트됩니다");
		m.put("Aggregated from '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':", "'<span wicket:id=\"estimatedTimeAggregationLink\"></span>'에서 집계됨:");
		m.put("Aggregated from '<span wicket:id=\"spentTimeAggregationLink\"></span>':", "'<span wicket:id=\"spentTimeAggregationLink\"></span>'에서 집계됨:");
		m.put("Aggregation Link", "집계 링크");
		m.put("Alert", "알림");
		m.put("Alert Setting", "알림 설정");
		m.put("Alert Settings", "알림 설정들");
		m.put("Alert settings have been updated", "알림 설정이 업데이트되었습니다");
		m.put("Alerts", "알림들");
		m.put("All", "전체");
		m.put("All Issues", "모든 이슈");
		m.put("All RESTful Resources", "모든 RESTful 리소스");
		m.put("All accessible", "모든 접근 가능");
		m.put("All builds", "모든 빌드");
		m.put("All changes", "모든 변경 사항");
		m.put("All except", "제외한 모든 것");
		m.put("All files", "모든 파일");
		m.put("All groups", "모든 그룹");
		m.put("All issues", "모든 이슈");
		m.put("All platforms in OCI layout", "OCI 레이아웃의 모든 플랫폼");
		m.put("All platforms in image", "이미지의 모든 플랫폼");
		m.put("All possible classes", "모든 가능한 클래스");
		m.put("All projects", "모든 프로젝트");
		m.put("All projects with code read permission", "코드 읽기 권한이 있는 모든 프로젝트");
		m.put("All pull requests", "모든 풀 요청");
		m.put("All users", "모든 사용자");
		m.put("Allow Empty", "빈 값 허용");
		m.put("Allow Empty Value", "빈 값 허용");
		m.put("Allow Multiple", "다중 허용");
		m.put("Allowed Licenses", "허용된 라이선스");
		m.put("Allowed Self Sign-Up Email Domain", "허용된 자체 가입 이메일 도메인");
		m.put("Always", "항상");
		m.put("Always Pull Image", "항상 이미지 가져오기");
		m.put("An issue already linked for {0}. Unlink it first", "{0}에 대해 이미 연결된 이슈가 있습니다. 먼저 연결을 해제하세요");
		m.put("An unexpected exception occurred", "예기치 않은 예외가 발생했습니다");
		m.put("And configure auth token of the registry", "레지스트리의 인증 토큰을 구성하세요");
		m.put("Another pull request already open for this change", "이 변경에 대한 다른 풀 요청이 이미 열려 있습니다");
		m.put("Any agent", "모든 에이전트");
		m.put("Any branch", "모든 브랜치");
		m.put("Any commit message", "커밋 메시지");
		m.put("Any domain", "모든 도메인");
		m.put("Any file", "모든 파일");
		m.put("Any issue", "모든 이슈");
		m.put("Any job", "모든 작업");
		m.put("Any project", "모든 프로젝트");
		m.put("Any ref", "모든 참조");
		m.put("Any sender", "모든 발신자");
		m.put("Any state", "모든 상태");
		m.put("Any tag", "모든 태그");
		m.put("Any user", "모든 사용자");
		m.put("Api Key", "API 키");
		m.put("Api Token", "API 토큰");
		m.put("Api Url", "API URL");
		m.put("Append", "추가");
		m.put("Applicable Branches", "적용 가능한 브랜치");
		m.put("Applicable Builds", "적용 가능한 빌드");
		m.put("Applicable Code Comments", "적용 가능한 코드 댓글");
		m.put("Applicable Commit Messages", "적용 가능한 커밋 메시지");
		m.put("Applicable Commits", "적용 가능한 커밋");
		m.put("Applicable Images", "적용 가능한 이미지");
		m.put("Applicable Issues", "적용 가능한 이슈");
		m.put("Applicable Jobs", "적용 가능한 작업");
		m.put("Applicable Names", "적용 가능한 이름");
		m.put("Applicable Projects", "적용 가능한 프로젝트");
		m.put("Applicable Pull Requests", "적용 가능한 풀 요청");
		m.put("Applicable Senders", "적용 가능한 발신자");
		m.put("Applicable Users", "적용 가능한 사용자");
		m.put("Application (client) ID", "애플리케이션(클라이언트) ID");
		m.put("Apply suggested change from code comment", "코드 댓글에서 제안된 변경 사항 적용");
		m.put("Apply suggested changes from code comments", "코드 댓글에서 제안된 변경 사항들 적용");
		m.put("Approve", "승인");
		m.put("Approved", "승인됨");
		m.put("Approved pull request \"{0}\" ({1})", "승인된 풀 요청 \"{0}\" ({1})");
		m.put("Arbitrary scope", "임의 범위");
		m.put("Arbitrary type", "임의 유형");
		m.put("Arch Pull Command", "Arch Pull 명령");
		m.put("Archived", "보관됨");
		m.put("Arguments", "인수");
		m.put("Artifacts", "아티팩트");
		m.put("Artifacts to Retrieve", "검색할 아티팩트");
		m.put("As long as a feature can be accessed via url, you can input part of the url to match and jump", 
			"기능이 URL을 통해 접근 가능하면 URL의 일부를 입력하여 일치하고 이동할 수 있습니다");
		m.put("Ascending", "오름차순");
		m.put("Assignees", "담당자");
		m.put("Assignees Issue Field", "담당자 이슈 필드");
		m.put("Assignees are expected to merge the pull request", "담당자는 풀 요청을 병합해야 합니다");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"담당자는 코드 쓰기 권한이 있으며 풀 요청 병합을 책임집니다");
		m.put("Asymmetric", "비대칭");
		m.put("At least one branch or tag should be selected", "최소한 하나의 브랜치 또는 태그를 선택해야 합니다");
		m.put("At least one choice need to be specified", "최소한 하나의 선택 항목을 지정해야 합니다");
		m.put("At least one email address should be configured, please add a new one first", "최소한 하나의 이메일 주소를 구성해야 합니다. 먼저 새 이메일 주소를 추가하세요");
		m.put("At least one email address should be specified", "최소한 하나의 이메일 주소를 지정해야 합니다");
		m.put("At least one entry should be specified", "최소한 하나의 항목을 지정해야 합니다");
		m.put("At least one event type needs to be selected", "최소한 하나의 이벤트 유형을 선택해야 합니다");
		m.put("At least one field needs to be specified", "최소한 하나의 필드를 지정해야 합니다");
		m.put("At least one project should be authorized", "최소한 하나의 프로젝트를 승인해야 합니다");
		m.put("At least one project should be selected", "최소한 하나의 프로젝트를 선택해야 합니다");
		m.put("At least one repository should be selected", "최소한 하나의 저장소를 선택해야 합니다");
		m.put("At least one role is required", "최소한 하나의 역할이 필요합니다");
		m.put("At least one role must be selected", "최소한 하나의 역할을 선택해야 합니다");
		m.put("At least one state should be specified", "최소한 하나의 상태를 지정해야 합니다");
		m.put("At least one tab should be added", "최소한 하나의 탭을 추가해야 합니다");
		m.put("At least one user search base should be specified", "최소한 하나의 사용자 검색 기준을 지정해야 합니다");
		m.put("At least one value needs to be specified", "최소한 하나의 값을 지정해야 합니다");
		m.put("At least two columns need to be defined", "최소한 두 개의 열을 정의해야 합니다");
		m.put("Attachment", "첨부 파일");
		m.put("Attributes", "속성");
		m.put("Attributes (can only be edited when agent is online)", "속성(에이전트가 온라인 상태일 때만 편집 가능)");
		m.put("Attributes saved", "속성 저장됨");
		m.put("Audit", "감사");
		m.put("Audit Log", "감사 로그");
		m.put("Audit Setting", "감사 설정");
		m.put("Audit log will be preserved for the specified number of days. This setting applies to all audit events, including system level and project level", 
			"감사 로그는 지정된 일수 동안 보존됩니다. 이 설정은 시스템 수준 및 프로젝트 수준을 포함한 모든 감사 이벤트에 적용됩니다");
		m.put("Auth Source", "인증 소스");
		m.put("Authenticate to Bitbucket Cloud", "Bitbucket Cloud에 인증");
		m.put("Authenticate to GitHub", "GitHub에 인증");
		m.put("Authenticate to GitLab", "GitLab에 인증");
		m.put("Authenticate to Gitea", "Gitea에 인증");
		m.put("Authenticate to JIRA cloud", "JIRA Cloud에 인증");
		m.put("Authenticate to YouTrack", "YouTrack에 인증");
		m.put("Authentication", "인증");
		m.put("Authentication Required", "인증 필요");
		m.put("Authentication Test", "인증 테스트");
		m.put("Authentication Token", "인증 토큰");
		m.put("Authenticator", "인증자");
		m.put("Authenticator Bean", "인증자 빈");
		m.put("Author", "작성자");
		m.put("Author date", "작성 날짜");
		m.put("Authored By", "작성자");
		m.put("Authorization", "권한 부여");
		m.put("Authorizations", "권한 부여 목록");
		m.put("Authorize user...", "사용자 승인...");
		m.put("Authorized Projects", "승인된 프로젝트");
		m.put("Authorized Roles", "승인된 역할");
		m.put("Auto Merge", "자동 병합");
		m.put("Auto Spec", "자동 사양");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"자동 업데이트 확인은 브라우저에서 onedev.io의 이미지를 요청하여 새 버전 가용성을 나타내며, 색상은 업데이트의 심각도를 나타냅니다. 이는 gravatar가 아바타 이미지를 요청하는 방식과 동일하게 작동합니다. 비활성화된 경우, 보안/중요 수정 사항이 있는지 확인하기 위해 수동으로 업데이트를 주기적으로 확인하는 것이 강력히 권장됩니다(화면 왼쪽 하단의 도움말 메뉴를 통해 수행 가능).");
		m.put("Auto-discovered executor", "자동 검색된 실행기");
		m.put("Available Agent Tokens", "사용 가능한 에이전트 토큰");
		m.put("Available Choices", "사용 가능한 선택 항목");
		m.put("Avatar", "아바타");
		m.put("Avatar Service Url", "아바타 서비스 URL");
		m.put("Avatar and name", "아바타와 이름");
		m.put("Back To Home", "홈으로 돌아가기");
		m.put("Backlog", "백로그");
		m.put("Backlog Base Query", "백로그 기본 쿼리");
		m.put("Backup", "백업");
		m.put("Backup Now", "지금 백업");
		m.put("Backup Schedule", "백업 일정");
		m.put("Backup Setting", "백업 설정");
		m.put("Backup Setting Holder", "백업 설정 홀더");
		m.put("Backup settings updated", "백업 설정이 업데이트되었습니다");
		m.put("Bare Metal", "베어 메탈");
		m.put("Base", "기본");
		m.put("Base Gpg Key", "기본 Gpg 키");
		m.put("Base Query", "기본 쿼리");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Base64로 인코딩된 PEM 형식, -----BEGIN CERTIFICATE-----로 시작하고 -----END CERTIFICATE-----로 끝남");
		m.put("Basic Info", "기본 정보");
		m.put("Basic Settings", "기본 설정");
		m.put("Basic settings updated", "기본 설정이 업데이트되었습니다");
		m.put("Batch Edit All Queried Issues", "모든 쿼리된 이슈 일괄 편집");
		m.put("Batch Edit Selected Issues", "선택된 이슈 일괄 편집");
		m.put("Batch Editing {0} Issues", "{0}개의 이슈를 일괄 편집 중");
		m.put("Batched suggestions", "일괄 제안");
		m.put("Before modification", "수정 전");
		m.put("Belonging Groups", "소속 그룹");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"아래는 일반적인 기준입니다. 위의 검색 상자에 입력하여 전체 목록과 사용 가능한 조합을 확인하세요.");
		m.put("Below content is restored from an unsaved change. Clear to discard", "아래 내용은 저장되지 않은 변경 사항에서 복원되었습니다. 삭제하려면 지우세요.");
		m.put("Below information will also be sent", "아래 정보도 전송됩니다");
		m.put("Binary file.", "바이너리 파일.");
		m.put("Bitbucket App Password", "Bitbucket 앱 비밀번호");
		m.put("Bitbucket Login Name", "Bitbucket 로그인 이름");
		m.put("Bitbucket Repositories to Import", "가져올 Bitbucket 저장소");
		m.put("Bitbucket Workspace", "Bitbucket 워크스페이스");
		m.put("Bitbucket app password should be generated with permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>", 
			"Bitbucket 앱 비밀번호는 <b>account/read</b>, <b>repositories/read</b>, <b>issues:read</b> 권한으로 생성되어야 합니다");
		m.put("Blame", "블레임");
		m.put("Blob", "블롭");
		m.put("Blob hash", "블롭 해시");
		m.put("Blob index version", "블롭 인덱스 버전");
		m.put("Blob name", "블롭 이름");
		m.put("Blob path", "블롭 경로");
		m.put("Blob primary symbols", "블롭 주요 심볼");
		m.put("Blob secondary symbols", "블롭 부차적 심볼");
		m.put("Blob symbol list", "블롭 심볼 목록");
		m.put("Blob text", "블롭 텍스트");
		m.put("Blob unknown", "알 수 없는 블롭");
		m.put("Blob upload invalid", "잘못된 블롭 업로드");
		m.put("Blob upload unknown", "알 수 없는 블롭 업로드");
		m.put("Board", "보드");
		m.put("Board Columns", "보드 열");
		m.put("Board Spec", "보드 사양");
		m.put("Boards", "보드들");
		m.put("Body", "본문");
		m.put("Bold", "굵게");
		m.put("Both", "둘 다");
		m.put("Bottom", "아래쪽");
		m.put("Branch", "브랜치");
		m.put("Branch \"{0}\" already exists, please choose a different name", "브랜치 \"{0}\"는 이미 존재합니다. 다른 이름을 선택하세요.");
		m.put("Branch \"{0}\" created", "브랜치 \"{0}\"가 생성되었습니다");
		m.put("Branch \"{0}\" deleted", "브랜치 \"{0}\"가 삭제되었습니다");
		m.put("Branch <a wicket:id=\"targetBranch\"></a> is up to date with all commits from <a wicket:id=\"sourceBranch\"></a>. Try <a wicket:id=\"swapBranches\">swap source and target</a> for the comparison.", 
			"브랜치 <a wicket:id=\"targetBranch\"></a>는 <a wicket:id=\"sourceBranch\"></a>의 모든 커밋과 최신 상태입니다. 비교를 위해 <a wicket:id=\"swapBranches\">소스와 대상 교환</a>을 시도하세요.");
		m.put("Branch Choice Bean", "브랜치 선택 빈");
		m.put("Branch Name", "브랜치 이름");
		m.put("Branch Protection", "브랜치 보호");
		m.put("Branch Revision", "브랜치 수정");
		m.put("Branch update", "브랜치 업데이트");
		m.put("Branches", "브랜치들");
		m.put("Brand Setting Edit Bean", "브랜드 설정 편집 빈");
		m.put("Branding", "브랜딩");
		m.put("Branding settings updated", "브랜딩 설정이 업데이트되었습니다");
		m.put("Browse Code", "코드 탐색");
		m.put("Browse code", "코드 탐색");
		m.put("Bug Report", "버그 보고");
		m.put("Build", "빌드");
		m.put("Build #{0} already finished", "빌드 #{0}는 이미 완료되었습니다");
		m.put("Build #{0} deleted", "빌드 #{0}가 삭제되었습니다");
		m.put("Build #{0} not finished yet", "빌드 #{0}는 아직 완료되지 않았습니다");
		m.put("Build Artifact Storage", "빌드 아티팩트 저장소");
		m.put("Build Commit", "빌드 커밋");
		m.put("Build Context", "빌드 컨텍스트");
		m.put("Build Description", "빌드 설명");
		m.put("Build Filter", "빌드 필터");
		m.put("Build Image", "빌드 이미지");
		m.put("Build Image (Kaniko)", "빌드 이미지 (Kaniko)");
		m.put("Build Management", "빌드 관리");
		m.put("Build Notification", "빌드 알림");
		m.put("Build Notification Template", "빌드 알림 템플릿");
		m.put("Build Number", "빌드 번호");
		m.put("Build On Behalf Of", "대리로 빌드");
		m.put("Build Path", "빌드 경로");
		m.put("Build Preservation", "빌드 보존");
		m.put("Build Preservations", "빌드 보존들");
		m.put("Build Preservations Bean", "빌드 보존 빈");
		m.put("Build Preserve Rules", "빌드 보존 규칙");
		m.put("Build Provider", "빌드 제공자");
		m.put("Build Spec", "빌드 사양");
		m.put("Build Statistics", "빌드 통계");
		m.put("Build Version", "빌드 버전");
		m.put("Build Volume Storage Class", "빌드 볼륨 저장소 클래스");
		m.put("Build Volume Storage Size", "빌드 볼륨 저장소 크기");
		m.put("Build administrative permission for all jobs inside a project, including batch operations over multiple builds", 
			"프로젝트 내 모든 작업에 대한 빌드 관리 권한, 여러 빌드에 대한 일괄 작업 포함");
		m.put("Build docker image with docker buildx. This step can only be executed by server docker executor or remote docker executor, and it uses the buildx builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko step instead", 
			"docker buildx로 도커 이미지를 빌드합니다. 이 단계는 서버 도커 실행기 또는 원격 도커 실행기에 의해 실행될 수 있으며, 이 실행기에서 지정된 buildx 빌더를 사용하여 작업을 수행합니다. Kubernetes 실행기로 이미지를 빌드하려면 kaniko 단계를 사용하세요.");
		m.put("Build docker image with kaniko. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"kaniko로 도커 이미지를 빌드합니다. 이 단계는 서버 도커 실행기, 원격 도커 실행기 또는 Kubernetes 실행기에 의해 실행되어야 합니다.");
		m.put("Build duration statistics", "빌드 지속 시간 통계");
		m.put("Build frequency statistics", "빌드 빈도 통계");
		m.put("Build is successful", "빌드가 성공했습니다");
		m.put("Build list", "빌드 목록");
		m.put("Build not exist or access denied", "빌드가 존재하지 않거나 접근이 거부되었습니다");
		m.put("Build number", "빌드 번호");
		m.put("Build preserve rules saved", "빌드 보존 규칙이 저장되었습니다");
		m.put("Build required for deletion. Submit pull request instead", "삭제를 위해 빌드가 필요합니다. 대신 풀 리퀘스트를 제출하세요");
		m.put("Build required for this change. Please submit pull request instead", "이 변경 사항에는 빌드가 필요합니다. 대신 풀 리퀘스트를 제출하세요.");
		m.put("Build required for this change. Submit pull request instead", "이 변경을 위해 빌드가 필요합니다. 대신 풀 리퀘스트를 제출하세요");
		m.put("Build spec not defined", "빌드 사양이 정의되지 않았습니다");
		m.put("Build spec not defined (import project: {0}, import revision: {1})", "빌드 사양이 정의되지 않았습니다 (프로젝트 가져오기: {0}, 리비전 가져오기: {1})");
		m.put("Build spec not found in commit of this build", "이 빌드의 커밋에서 빌드 사양을 찾을 수 없습니다");
		m.put("Build statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"빌드 통계는 엔터프라이즈 기능입니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>을 해보세요");
		m.put("Build version", "빌드 버전");
		m.put("Build with Persistent Volume", "Persistent Volume을 사용하는 빌드");
		m.put("Builds", "빌드들");
		m.put("Builds are {0}", "빌드는 {0} 상태입니다");
		m.put("Buildx Builder", "Buildx 빌더");
		m.put("Built In Fields Bean", "내장 필드 빈");
		m.put("Burndown", "번다운");
		m.put("Burndown chart", "번다운 차트");
		m.put("Button Image Url", "버튼 이미지 URL");
		m.put("By Group", "그룹별");
		m.put("By User", "사용자별");
		m.put("By day", "일별");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"기본적으로 코드는 자동 생성된 자격 증명을 통해 클론되며, 현재 프로젝트에 대한 읽기 권한만 있습니다. 작업이 <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>코드를 서버에 푸시</a>해야 하는 경우 적절한 권한을 가진 사용자 정의 자격 증명을 제공해야 합니다");
		m.put("By default issues of parent and child projects will also be listed. Use query <code>&quot;Project&quot; is current</code> to show issues only belong to this project", 
			"기본적으로 부모 및 자식 프로젝트의 문제도 나열됩니다. 이 프로젝트에만 속한 문제를 표시하려면 쿼리 <code>&quot;Project&quot; is current</code>를 사용하세요");
		m.put("By month", "월별");
		m.put("By week", "주별");
		m.put("Bypass Certificate Check", "인증서 확인 건너뛰기");
		m.put("CANCELLED", "취소됨");
		m.put("CORS Allowed Origins", "CORS 허용된 출처");
		m.put("CPD Report", "CPD 보고서");
		m.put("CPU", "CPU");
		m.put("CPU Intensive Task Concurrency", "CPU 집약적 작업 동시성");
		m.put("CPU capability in millis. This is normally (CPU cores)*1000", "밀리초 단위의 CPU 성능. 일반적으로 (CPU 코어)*1000입니다");
		m.put("Cache Key", "캐시 키");
		m.put("Cache Management", "캐시 관리");
		m.put("Cache Paths", "캐시 경로");
		m.put("Cache Setting Bean", "캐시 설정 빈");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "이 일수 동안 접근되지 않으면 공간 절약을 위해 캐시가 삭제됩니다");
		m.put("Calculating merge preview...", "병합 미리보기 계산 중...");
		m.put("Callback URL", "콜백 URL");
		m.put("Can Be Used By Jobs", "작업에서 사용할 수 있음");
		m.put("Can Create Root Projects", "루트 프로젝트 생성 가능");
		m.put("Can Edit Estimated Time", "예상 시간 수정 가능");
		m.put("Can not convert root user to service account", "루트 사용자를 서비스 계정으로 변환할 수 없습니다");
		m.put("Can not convert yourself to service account", "자신을 서비스 계정으로 변환할 수 없습니다");
		m.put("Can not delete default branch", "기본 브랜치를 삭제할 수 없습니다");
		m.put("Can not delete root account", "루트 계정을 삭제할 수 없습니다");
		m.put("Can not delete yourself", "자신을 삭제할 수 없습니다");
		m.put("Can not disable root account", "루트 계정을 비활성화할 수 없습니다");
		m.put("Can not disable yourself", "자신을 비활성화할 수 없습니다");
		m.put("Can not find issue board: ", "이슈 보드를 찾을 수 없습니다:");
		m.put("Can not move project \"{0}\" to be under itself or its descendants", "프로젝트 \"{0}\"를 자신 또는 하위 프로젝트 아래로 이동할 수 없습니다");
		m.put("Can not perform this operation now", "지금 이 작업을 수행할 수 없습니다");
		m.put("Can not reset password for service account or disabled user", "서비스 계정 또는 비활성화된 사용자의 비밀번호를 재설정할 수 없습니다");
		m.put("Can not reset password for user authenticating via external system", "외부 시스템을 통해 인증하는 사용자의 비밀번호를 재설정할 수 없습니다");
		m.put("Can not save malformed query", "잘못된 쿼리를 저장할 수 없습니다");
		m.put("Can not use current or descendant project as parent", "현재 또는 하위 프로젝트를 부모로 사용할 수 없습니다");
		m.put("Can only compare with common ancestor when different projects are involved", "다른 프로젝트가 포함된 경우 공통 조상과만 비교할 수 있습니다");
		m.put("Cancel", "취소");
		m.put("Cancel All Queried Builds", "쿼리된 모든 빌드 취소");
		m.put("Cancel Selected Builds", "선택된 빌드 취소");
		m.put("Cancel invitation", "초대 취소");
		m.put("Cancel request submitted", "요청 취소가 제출되었습니다");
		m.put("Cancel this build", "이 빌드 취소");
		m.put("Cancelled", "취소됨");
		m.put("Cancelled By", "취소한 사람");
		m.put("Case Sensitive", "대소문자 구분");
		m.put("Certificates to Trust", "신뢰할 인증서");
		m.put("Change", "변경");
		m.put("Change Detection Excludes", "변경 감지 제외");
		m.put("Change My Password", "내 비밀번호 변경");
		m.put("Change To", "다음으로 변경");
		m.put("Change already merged", "변경 사항이 이미 병합되었습니다");
		m.put("Change not updated yet", "변경 사항이 아직 업데이트되지 않았습니다");
		m.put("Change property <code>serverUrl</code> in file <code>conf/agent.properties</code> if necessary. The default value is taken from OneDev server url specified in <i>Administration / System Setting</i>", 
			"필요한 경우 파일 <code>conf/agent.properties</code>에서 속성 <code>serverUrl</code>을 변경하세요. 기본값은 <i>관리 / 시스템 설정</i>에서 지정된 OneDev 서버 URL에서 가져옵니다");
		m.put("Change to another field", "다른 필드로 변경");
		m.put("Change to another state", "다른 상태로 변경");
		m.put("Change to another value", "다른 값으로 변경");
		m.put("Changes since last review", "마지막 리뷰 이후 변경 사항");
		m.put("Changes since last visit", "마지막 방문 이후 변경 사항");
		m.put("Changes since this action", "이 작업 이후 변경 사항");
		m.put("Changes since this comment", "이 댓글 이후 변경 사항");
		m.put("Channel Notification", "채널 알림");
		m.put("Chart Metadata", "차트 메타데이터");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"커밋에 서명을 하기 위해 GPG 키를 생성하고 사용하는 방법에 대한 <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub 가이드</a>를 확인하세요");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including instructions on how to run agent as service", 
			"에이전트를 서비스로 실행하는 방법을 포함하여 자세한 내용은 <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">에이전트 관리</a>를 확인하세요");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including list of supported environment variables", 
			"지원되는 환경 변수 목록을 포함하여 자세한 내용은 <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">에이전트 관리</a>를 확인하세요");
		m.put("Check Commit Message Footer", "커밋 메시지 푸터 확인");
		m.put("Check Incoming Email", "수신 이메일 확인");
		m.put("Check Issue Integrity", "이슈 무결성 확인");
		m.put("Check Update", "업데이트 확인");
		m.put("Check Workflow Integrity", "워크플로 무결성 확인");
		m.put("Check out to local workspace", "로컬 작업 공간으로 체크아웃");
		m.put("Check this to compare right side with common ancestor of left and right", "왼쪽과 오른쪽의 공통 조상과 오른쪽을 비교하려면 이것을 선택하세요");
		m.put("Check this to enforce two-factor authentication for all users in the system", "시스템의 모든 사용자에 대해 이중 인증을 강제하려면 이것을 선택하세요");
		m.put("Check this to enforce two-factor authentication for all users in this group", "이 그룹의 모든 사용자에 대해 이중 인증을 강제하려면 이것을 선택하세요");
		m.put("Check this to prevent branch creation", "브랜치 생성을 방지하려면 이것을 선택하세요");
		m.put("Check this to prevent branch deletion", "브랜치 삭제를 방지하려면 이것을 선택하세요");
		m.put("Check this to prevent forced push", "강제 푸시를 방지하려면 이것을 선택하세요");
		m.put("Check this to prevent tag creation", "태그 생성을 방지하려면 이것을 선택하세요");
		m.put("Check this to prevent tag deletion", "태그 삭제를 방지하려면 이것을 선택하세요");
		m.put("Check this to prevent tag update", "태그 업데이트를 방지하려면 이것을 선택하세요");
		m.put("Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>. Note this is applicable for non-merge commits", 
			"이 옵션을 선택하면 <a href='https://www.conventionalcommits.org' target='_blank'>컨벤션 커밋</a>이 필요합니다. 이는 병합 커밋에는 적용되지 않습니다.");
		m.put("Check this to require valid signature of head commit", "헤드 커밋의 유효한 서명을 요구하려면 이 옵션을 선택하세요.");
		m.put("Check this to retrieve Git LFS files", "Git LFS 파일을 가져오려면 이 옵션을 선택하세요.");
		m.put("Checkbox", "체크박스");
		m.put("Checking field values...", "필드 값을 확인 중...");
		m.put("Checking fields...", "필드를 확인 중...");
		m.put("Checking state and field ordinals...", "상태 및 필드 순서를 확인 중...");
		m.put("Checking state...", "상태를 확인 중...");
		m.put("Checkout Code", "코드 체크아웃");
		m.put("Checkout Path", "체크아웃 경로");
		m.put("Checkout Pull Request Head", "풀 리퀘스트 헤드 체크아웃");
		m.put("Checkout Pull Request Merge Preview", "풀 리퀘스트 병합 미리보기 체크아웃");
		m.put("Checkstyle Report", "체크스타일 보고서");
		m.put("Cherry-Pick", "체리픽");
		m.put("Cherry-picked successfully", "체리픽 성공");
		m.put("Child Projects", "하위 프로젝트");
		m.put("Child Projects Of", "하위 프로젝트 목록");
		m.put("Choice Provider", "선택 제공자");
		m.put("Choose", "선택");
		m.put("Choose JIRA project to import issues from", "JIRA 프로젝트를 선택하여 이슈를 가져옵니다.");
		m.put("Choose Revision", "리비전을 선택하세요.");
		m.put("Choose YouTrack project to import issues from", "YouTrack 프로젝트를 선택하여 이슈를 가져옵니다.");
		m.put("Choose a project...", "프로젝트를 선택하세요...");
		m.put("Choose a user...", "사용자를 선택하세요...");
		m.put("Choose branch...", "브랜치를 선택하세요...");
		m.put("Choose branches...", "브랜치들을 선택하세요...");
		m.put("Choose build...", "빌드를 선택하세요...");
		m.put("Choose file", "파일을 선택하세요.");
		m.put("Choose group...", "그룹을 선택하세요...");
		m.put("Choose groups...", "그룹들을 선택하세요...");
		m.put("Choose issue...", "이슈를 선택하세요...");
		m.put("Choose issues...", "이슈 선택...");
		m.put("Choose iteration...", "반복을 선택하세요...");
		m.put("Choose iterations...", "반복들을 선택하세요...");
		m.put("Choose job...", "작업을 선택하세요...");
		m.put("Choose jobs...", "작업들을 선택하세요...");
		m.put("Choose project", "프로젝트를 선택하세요.");
		m.put("Choose projects...", "프로젝트들을 선택하세요...");
		m.put("Choose pull request...", "풀 리퀘스트를 선택하세요...");
		m.put("Choose repository", "저장소를 선택하세요.");
		m.put("Choose role...", "역할을 선택하세요...");
		m.put("Choose roles...", "역할들을 선택하세요...");
		m.put("Choose users...", "사용자들을 선택하세요...");
		m.put("Choose...", "선택...");
		m.put("Circular build spec imports ({0})", "순환 빌드 사양 가져오기 ({0})");
		m.put("Click to select a commit, or shift-click to select multiple commit", "커밋을 선택하려면 클릭하세요, 여러 커밋을 선택하려면 Shift-클릭하세요.");
		m.put("Click to show comment of marked text", "표시된 텍스트의 주석을 보려면 클릭하세요.");
		m.put("Click to show issue details", "이슈 세부 정보를 보려면 클릭하세요.");
		m.put("Client ID of this OneDev instance registered in Google cloud", "Google 클라우드에 등록된 이 OneDev 인스턴스의 클라이언트 ID");
		m.put("Client Id", "클라이언트 ID");
		m.put("Client Secret", "클라이언트 비밀");
		m.put("Client secret of this OneDev instance registered in Google cloud", "Google 클라우드에 등록된 이 OneDev 인스턴스의 클라이언트 비밀");
		m.put("Clippy Report", "Clippy 보고서");
		m.put("Clone", "클론");
		m.put("Clone Credential", "클론 자격 증명");
		m.put("Clone Depth", "클론 깊이");
		m.put("Clone in IntelliJ", "IntelliJ에서 클론");
		m.put("Clone in VSCode", "VSCode에서 클론");
		m.put("Close", "닫기");
		m.put("Close Iteration", "반복 닫기");
		m.put("Close this iteration", "이 반복을 닫기");
		m.put("Closed", "닫힘");
		m.put("Closed Issue State", "닫힌 이슈 상태");
		m.put("Closest due date", "가장 가까운 마감일");
		m.put("Clover Coverage Report", "Clover 커버리지 보고서");
		m.put("Cluster Role", "클러스터 역할");
		m.put("Cluster Setting", "클러스터 설정");
		m.put("Cluster setting", "클러스터 설정");
		m.put("Clustered Servers", "클러스터된 서버");
		m.put("Cobertura Coverage Report", "Cobertura 커버리지 보고서");
		m.put("Code", "코드");
		m.put("Code Analysis", "코드 분석");
		m.put("Code Analysis Setting", "코드 분석 설정");
		m.put("Code Analysis Settings", "코드 분석 설정들");
		m.put("Code Changes", "코드 변경 사항");
		m.put("Code Comment", "코드 주석");
		m.put("Code Comment Management", "코드 주석 관리");
		m.put("Code Comments", "코드 주석들");
		m.put("Code Compare", "코드 비교");
		m.put("Code Contribution Statistics", "코드 기여 통계");
		m.put("Code Coverage", "코드 커버리지");
		m.put("Code Line Statistics", "코드 라인 통계");
		m.put("Code Management", "코드 관리");
		m.put("Code Privilege", "코드 권한");
		m.put("Code Problem Statistics", "코드 문제 통계");
		m.put("Code Search", "코드 검색");
		m.put("Code Statistics", "코드 통계");
		m.put("Code analysis settings updated", "코드 분석 설정이 업데이트되었습니다.");
		m.put("Code changes since...", "이후 코드 변경 사항...");
		m.put("Code clone or download", "코드 클론 또는 다운로드");
		m.put("Code comment", "코드 주석");
		m.put("Code comment #{0} deleted", "코드 주석 #{0}이 삭제되었습니다.");
		m.put("Code comment administrative permission inside a project, including batch operations over multiple code comments", 
			"프로젝트 내에서 코드 주석 관리 권한, 여러 코드 주석에 대한 일괄 작업 포함");
		m.put("Code commit", "코드 커밋");
		m.put("Code is committed", "코드가 커밋되었습니다.");
		m.put("Code push", "코드 푸시");
		m.put("Code read permission is required to import build spec (import project: {0}, import revision: {1})", 
			"빌드 사양을 가져오려면 코드 읽기 권한이 필요합니다 (가져오기 프로젝트: {0}, 가져오기 리비전: {1})");
		m.put("Code suggestion", "코드 제안");
		m.put("Code write permission is required for this operation", "이 작업을 수행하려면 코드 쓰기 권한이 필요합니다.");
		m.put("Collapse all", "모두 축소");
		m.put("Color", "색상");
		m.put("Columns", "열");
		m.put("Command Palette", "명령 팔레트");
		m.put("Commands", "명령어");
		m.put("Comment", "댓글");
		m.put("Comment Content", "댓글 내용");
		m.put("Comment on File", "파일에 대한 댓글");
		m.put("Comment too long", "댓글이 너무 깁니다");
		m.put("Commented code is outdated", "댓글로 작성된 코드는 오래되었습니다");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "프로젝트 \"{1}\"의 파일 \"{0}\"에 댓글을 작성했습니다");
		m.put("Commented on issue \"{0}\" ({1})", "이슈 \"{0}\" ({1})에 댓글을 작성했습니다");
		m.put("Commented on pull request \"{0}\" ({1})", "풀 리퀘스트 \"{0}\" ({1})에 댓글을 작성했습니다");
		m.put("Comments", "댓글들");
		m.put("Commit", "커밋");
		m.put("Commit &amp; Insert", "커밋 &amp; 삽입");
		m.put("Commit Batched Suggestions", "배치된 제안 커밋");
		m.put("Commit Message", "커밋 메시지");
		m.put("Commit Message Bean", "커밋 메시지 빈");
		m.put("Commit Message Fix Patterns", "커밋 메시지 수정 패턴");
		m.put("Commit Message Footer Pattern", "커밋 메시지 푸터 패턴");
		m.put("Commit Notification", "커밋 알림");
		m.put("Commit Notification Template", "커밋 알림 템플릿");
		m.put("Commit Scopes", "커밋 범위");
		m.put("Commit Signature Required", "커밋 서명이 필요합니다");
		m.put("Commit Suggestion", "커밋 제안");
		m.put("Commit Types", "커밋 유형");
		m.put("Commit Types For Footer Check", "푸터 확인을 위한 커밋 유형");
		m.put("Commit Your Change", "변경 사항을 커밋하세요");
		m.put("Commit date", "커밋 날짜");
		m.put("Commit hash", "커밋 해시");
		m.put("Commit history of current path", "현재 경로의 커밋 기록");
		m.put("Commit index version", "커밋 인덱스 버전");
		m.put("Commit message can be used to fix issues by prefixing and suffixing issue number with specified pattern. Each line of the commit message will be matched against each entry defined here to find issues to be fixed", 
			"커밋 메시지는 지정된 패턴으로 이슈 번호를 접두사 및 접미사로 추가하여 문제를 해결하는 데 사용할 수 있습니다. 커밋 메시지의 각 줄은 여기 정의된 각 항목과 일치하여 해결할 문제를 찾습니다");
		m.put("Commit not exist or access denied", "커밋이 존재하지 않거나 접근이 거부되었습니다");
		m.put("Commit of the build is missing", "빌드의 커밋이 누락되었습니다");
		m.put("Commit signature required but no GPG signing key specified", "커밋 서명이 필요하지만 GPG 서명 키가 지정되지 않았습니다");
		m.put("Commit suggestion", "커밋 제안");
		m.put("Commits", "커밋들");
		m.put("Commits are taken from default branch of non-forked repositories", "포크되지 않은 저장소의 기본 브랜치에서 커밋이 가져옵니다");
		m.put("Commits generated by OneDev previously will be shown as unverified if this key is deleted. Type <code>yes</code> below if you want to continue.", 
			"이 키가 삭제되면 이전에 OneDev에서 생성된 커밋은 확인되지 않은 것으로 표시됩니다. 계속하려면 아래에 <code>yes</code>를 입력하세요.");
		m.put("Commits were merged into target branch", "커밋이 대상 브랜치에 병합되었습니다");
		m.put("Commits were merged into target branch outside of this pull request", "커밋이 이 풀 리퀘스트 외부에서 대상 브랜치에 병합되었습니다");
		m.put("Commits were rebased onto target branch", "커밋이 대상 브랜치에 리베이스되었습니다");
		m.put("Commits were squashed into a single commit on target branch", "커밋이 대상 브랜치에서 단일 커밋으로 스쿼시되었습니다");
		m.put("Committed After", "이후에 커밋됨");
		m.put("Committed Before", "이전에 커밋됨");
		m.put("Committed By", "커밋한 사람");
		m.put("Committer", "커밋자");
		m.put("Compare", "비교");
		m.put("Compare with base revision", "기본 수정본과 비교");
		m.put("Compare with this parent", "이 부모와 비교");
		m.put("Concurrency", "동시성");
		m.put("Condition", "조건");
		m.put("Confidential", "기밀");
		m.put("Config File", "구성 파일");
		m.put("Configuration Discovery Url", "구성 검색 URL");
		m.put("Configure your scope to use below registry", "아래 레지스트리를 사용하도록 범위를 구성하세요");
		m.put("Confirm Approve", "승인을 확인하세요");
		m.put("Confirm Delete Source Branch", "소스 브랜치 삭제를 확인하세요");
		m.put("Confirm Discard", "폐기를 확인하세요");
		m.put("Confirm Reopen", "다시 열기를 확인하세요");
		m.put("Confirm Request For Changes", "변경 요청을 확인하세요");
		m.put("Confirm Restore Source Branch", "소스 브랜치 복원을 확인하세요");
		m.put("Confirm password here", "여기에서 비밀번호를 확인하세요");
		m.put("Confirm your action", "작업을 확인하세요");
		m.put("Connect New Agent", "새 에이전트 연결");
		m.put("Connect with your SSO account", "SSO 계정으로 연결");
		m.put("Contact Email", "연락처 이메일");
		m.put("Contact Name", "연락처 이름");
		m.put("Container Image", "컨테이너 이미지");
		m.put("Container Image(s)", "컨테이너 이미지(들)");
		m.put("Container default", "컨테이너 기본값");
		m.put("Content", "내용");
		m.put("Content Type", "콘텐츠 유형");
		m.put("Content is identical", "내용이 동일합니다");
		m.put("Continue to add other user after create", "생성 후 다른 사용자를 계속 추가하세요");
		m.put("Contributed settings", "기여된 설정");
		m.put("Contributions", "기여");
		m.put("Contributions to {0} branch, excluding merge commits", "머지 커밋을 제외한 {0} 브랜치에 대한 기여");
		m.put("Convert All Queried to Service Accounts", "모든 조회된 항목을 서비스 계정으로 변환");
		m.put("Convert Selected to Service Accounts", "선택된 항목을 서비스 계정으로 변환");
		m.put("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm", 
			"서비스 계정으로 변환하면 비밀번호, 이메일 주소, 모든 할당 및 감시가 제거됩니다. 확인하려면 <code>yes</code>를 입력하세요");
		m.put("Copy", "복사");
		m.put("Copy All Queried Issues To...", "쿼리된 모든 이슈를 복사하여...");
		m.put("Copy Files with SCP", "SCP로 파일 복사");
		m.put("Copy Selected Issues To...", "선택된 이슈를 복사하여...");
		m.put("Copy dashboard", "대시보드 복사");
		m.put("Copy issue number and title", "이슈 번호와 제목 복사");
		m.put("Copy public key", "공개 키 복사");
		m.put("Copy selected text to clipboard", "선택된 텍스트를 클립보드에 복사");
		m.put("Copy to clipboard", "클립보드에 복사");
		m.put("Count", "개수");
		m.put("Coverage Statistics", "커버리지 통계");
		m.put("Covered", "커버됨");
		m.put("Covered by tests", "테스트로 커버됨");
		m.put("Cppcheck Report", "Cppcheck 보고서");
		m.put("Cpu Limit", "CPU 제한");
		m.put("Cpu Request", "CPU 요청");
		m.put("Create", "생성");
		m.put("Create Administrator Account", "관리자 계정 생성");
		m.put("Create Branch", "브랜치 생성");
		m.put("Create Branch Bean", "브랜치 빈 생성");
		m.put("Create Branch Bean With Revision", "수정본과 함께 브랜치 빈 생성");
		m.put("Create Child Project", "하위 프로젝트 생성");
		m.put("Create Child Projects", "하위 프로젝트들 생성");
		m.put("Create Issue", "이슈 생성");
		m.put("Create Iteration", "반복 생성");
		m.put("Create Merge Commit", "병합 커밋 생성");
		m.put("Create Merge Commit If Necessary", "필요한 경우 병합 커밋 생성");
		m.put("Create New", "새로 생성");
		m.put("Create New File", "새 파일 생성");
		m.put("Create New User", "새 사용자 생성");
		m.put("Create Project", "프로젝트 생성");
		m.put("Create Pull Request", "풀 리퀘스트 생성");
		m.put("Create Pull Request for This Change", "이 변경 사항에 대한 풀 리퀘스트 생성");
		m.put("Create Tag", "태그 생성");
		m.put("Create Tag Bean", "태그 빈 생성");
		m.put("Create Tag Bean With Revision", "리비전 포함 태그 빈 생성");
		m.put("Create User", "사용자 생성");
		m.put("Create body", "본문 생성");
		m.put("Create branch <b>{0}</b> from {1}", "{1}에서 <b>{0}</b> 브랜치 생성");
		m.put("Create child projects under a project", "프로젝트 아래에 하위 프로젝트 생성");
		m.put("Create issue", "이슈 생성");
		m.put("Create merge commit", "병합 커밋 생성");
		m.put("Create merge commit if necessary", "필요한 경우 병합 커밋 생성");
		m.put("Create new issue", "새 이슈 생성");
		m.put("Create tag", "태그 생성");
		m.put("Create tag <b>{0}</b> from {1}", "{1}에서 <b>{0}</b> 태그 생성");
		m.put("Created At", "생성일");
		m.put("Creation of this branch is prohibited per branch protection rule", "브랜치 보호 규칙에 따라 이 브랜치 생성이 금지됨");
		m.put("Critical", "중요");
		m.put("Critical Severity", "중요 심각도");
		m.put("Cron Expression", "Cron 표현식");
		m.put("Cron schedule", "Cron 일정");
		m.put("Curl Location", "Curl 위치");
		m.put("Current Iteration", "현재 반복");
		m.put("Current Value", "현재 값");
		m.put("Current avatar", "현재 아바타");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"현재 컨텍스트가 이 댓글이 추가된 컨텍스트와 다릅니다. 댓글 컨텍스트를 보려면 클릭하세요");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"현재 컨텍스트가 이 답글이 추가된 컨텍스트와 다릅니다. 답글 컨텍스트를 보려면 클릭하세요");
		m.put("Current context is different from this action, click to show the comment context", "현재 컨텍스트가 이 작업과 다릅니다. 댓글 컨텍스트를 보려면 클릭하세요");
		m.put("Current platform", "현재 플랫폼");
		m.put("Current project", "현재 프로젝트");
		m.put("Custom Linux Shell", "사용자 정의 Linux 셸");
		m.put("DISCARDED", "폐기됨");
		m.put("Dashboard Share Bean", "대시보드 공유 빈");
		m.put("Dashboard name", "대시보드 이름");
		m.put("Dashboards", "대시보드");
		m.put("Database Backup", "데이터베이스 백업");
		m.put("Date", "날짜");
		m.put("Date Time", "날짜 시간");
		m.put("Days Per Week", "주당 일수");
		m.put("Deactivate Subscription", "구독 비활성화");
		m.put("Deactivate Trial Subscription", "체험 구독 비활성화");
		m.put("Default", "기본값");
		m.put("Default (Shell on Linux, Batch on Windows)", "기본값 (Linux에서는 셸, Windows에서는 배치)");
		m.put("Default Assignees", "기본 담당자");
		m.put("Default Boards", "기본 보드");
		m.put("Default Fixed Issue Filter", "기본 고정 이슈 필터");
		m.put("Default Fixed Issue Filters", "기본 고정 이슈 필터들");
		m.put("Default Fixed Issue Filters Bean", "기본 고정 이슈 필터 빈");
		m.put("Default Group", "기본 그룹");
		m.put("Default Issue Boards", "기본 이슈 보드");
		m.put("Default Merge Strategy", "기본 병합 전략");
		m.put("Default Multi Value Provider", "기본 다중 값 제공자");
		m.put("Default Project", "기본 프로젝트");
		m.put("Default Project Setting", "기본 프로젝트 설정");
		m.put("Default Roles", "기본 역할");
		m.put("Default Roles Bean", "기본 역할 빈");
		m.put("Default Value", "기본값");
		m.put("Default Value Provider", "기본값 제공자");
		m.put("Default Values", "기본값들");
		m.put("Default branch", "기본 브랜치");
		m.put("Default branding settings restored", "기본 브랜딩 설정 복원됨");
		m.put("Default fixed issue filters saved", "기본 고정 이슈 필터 저장됨");
		m.put("Default merge strategy", "기본 병합 전략");
		m.put("Default roles affect default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"기본 역할은 시스템 내 모든 사용자에게 부여되는 기본 권한에 영향을 미칩니다. 실제 기본 권한은 이 프로젝트와 모든 상위 프로젝트의 기본 역할에 포함된 <b class='text-warning'>모든 권한</b>이 될 것입니다");
		m.put("Define all custom issue fields here. Each project can decide to use all or a subset of these fields via its issue transition setting. <b class=\"text-warning\">NOTE: </b> Newly defined fields by default only appear in new issues. Batch edit existing issues from issue list page if you want them to have these new fields", 
			"여기에서 모든 사용자 정의 이슈 필드를 정의하세요. 각 프로젝트는 이슈 전환 설정을 통해 이러한 필드의 전체 또는 일부를 사용할지 결정할 수 있습니다. <b class=\"text-warning\">참고: </b> 새로 정의된 필드는 기본적으로 새 이슈에만 나타납니다. 기존 이슈 목록 페이지에서 일괄 편집하여 새 필드를 포함하도록 설정할 수 있습니다");
		m.put("Define all custom issue states here. The first state will be used as initial state of created issues", 
			"여기에서 모든 사용자 정의 이슈 상태를 정의하세요. 첫 번째 상태는 생성된 이슈의 초기 상태로 사용됩니다");
		m.put("Define branch protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given branch and user, the first matching rule will take effect", 
			"브랜치 보호 규칙을 정의하세요. 상위 프로젝트에서 정의된 규칙은 여기에서 정의된 규칙 뒤에 정의된 것으로 간주됩니다. 특정 브랜치와 사용자에 대해 첫 번째 일치하는 규칙이 적용됩니다");
		m.put("Define default issue boards for all projects here. A certain project can override this setting to define its own issue boards.", 
			"모든 프로젝트에 대한 기본 이슈 보드를 여기에서 정의하세요. 특정 프로젝트는 이 설정을 재정의하여 자체 이슈 보드를 정의할 수 있습니다");
		m.put("Define how issue states should be transited from one to another, either manually or automatically when some events happen. And the rule can be configured to apply to certain projects and issues via the applicable issues setting", 
			"이슈 상태가 수동으로 또는 특정 이벤트가 발생할 때 자동으로 어떻게 전환되어야 하는지 정의하세요. 그리고 규칙은 특정 프로젝트와 이슈에 적용되도록 설정할 수 있습니다");
		m.put("Define issue templates here. When a new issue is created, the first matching template will be used.", 
			"이슈 템플릿을 여기에서 정의하세요. 새 이슈가 생성될 때 첫 번째 일치하는 템플릿이 사용됩니다");
		m.put("Define labels to be assigned to project, build or pull request. For issues, custom fields can be used which is much more powerful than labels", 
			"프로젝트, 빌드 또는 풀 리퀘스트에 할당할 레이블을 정의하세요. 이슈의 경우 레이블보다 훨씬 강력한 사용자 정의 필드를 사용할 수 있습니다");
		m.put("Define properties to be used in build spec. Properties will be inherited by child projects, and can be overridden by child properties with same name.", 
			"빌드 사양에서 사용할 속성을 정의하세요. 속성은 하위 프로젝트에 의해 상속되며 동일한 이름의 하위 속성에 의해 재정의될 수 있습니다");
		m.put("Define rules to preserve builds. A build will be preserved as long as one rule defined here or in parent projects preserves it. All builds will be preserved if no rules are defined here and in parent projects", 
			"빌드를 보존하기 위한 규칙을 정의하세요. 여기 또는 상위 프로젝트에서 정의된 규칙 중 하나라도 빌드를 보존하면 빌드는 보존됩니다. 여기와 상위 프로젝트에서 규칙이 정의되지 않은 경우 모든 빌드가 보존됩니다");
		m.put("Define tag protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given tag and user, the first matching rule will take effect", 
			"태그 보호 규칙을 정의하세요. 상위 프로젝트에서 정의된 규칙은 여기에서 정의된 규칙 뒤에 정의된 것으로 간주됩니다. 특정 태그와 사용자에 대해 첫 번째 일치하는 규칙이 적용됩니다");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"첫 번째 재시도 지연 시간(초). 이후 재시도의 지연 시간은 이 값을 기반으로 한 지수 백오프를 사용하여 계산됩니다");
		m.put("Delete", "삭제");
		m.put("Delete All", "모두 삭제");
		m.put("Delete All Queried Builds", "쿼리된 빌드 모두 삭제");
		m.put("Delete All Queried Comments", "쿼리된 댓글 모두 삭제");
		m.put("Delete All Queried Issues", "쿼리된 이슈 모두 삭제");
		m.put("Delete All Queried Packages", "쿼리된 패키지 모두 삭제");
		m.put("Delete All Queried Projects", "쿼리된 프로젝트 모두 삭제");
		m.put("Delete All Queried Pull Requests", "쿼리된 풀 리퀘스트 모두 삭제");
		m.put("Delete All Queried Users", "쿼리된 사용자 모두 삭제");
		m.put("Delete Build", "빌드 삭제");
		m.put("Delete Comment", "댓글 삭제");
		m.put("Delete Pull Request", "풀 리퀘스트 삭제");
		m.put("Delete SSO account here to reconnect corresponding SSO subject upon next login. Note that SSO subject with verified email will be connected to user with same verified email automatically", 
			"다음 로그인 시 해당 SSO 주제를 다시 연결하려면 여기서 SSO 계정을 삭제하세요. 인증된 이메일이 있는 SSO 주제는 동일한 인증된 이메일을 가진 사용자와 자동으로 연결됩니다");
		m.put("Delete Selected", "선택된 항목 삭제");
		m.put("Delete Selected Builds", "선택된 빌드 삭제");
		m.put("Delete Selected Comments", "선택된 댓글 삭제");
		m.put("Delete Selected Issues", "선택된 이슈 삭제");
		m.put("Delete Selected Packages", "선택한 패키지 삭제");
		m.put("Delete Selected Projects", "선택한 프로젝트 삭제");
		m.put("Delete Selected Pull Requests", "선택한 풀 리퀘스트 삭제");
		m.put("Delete Selected Users", "선택한 사용자 삭제");
		m.put("Delete Source Branch", "소스 브랜치 삭제");
		m.put("Delete Source Branch After Merge", "병합 후 소스 브랜치 삭제");
		m.put("Delete dashboard", "대시보드 삭제");
		m.put("Delete from branch {0}", "브랜치 {0}에서 삭제");
		m.put("Delete this", "이 항목 삭제");
		m.put("Delete this GPG key", "이 GPG 키 삭제");
		m.put("Delete this access token", "이 액세스 토큰 삭제");
		m.put("Delete this branch", "이 브랜치 삭제");
		m.put("Delete this executor", "이 실행기 삭제");
		m.put("Delete this field", "이 필드 삭제");
		m.put("Delete this import", "이 가져오기 삭제");
		m.put("Delete this iteration", "이 반복 삭제");
		m.put("Delete this key", "이 키 삭제");
		m.put("Delete this link", "이 링크 삭제");
		m.put("Delete this rule", "이 규칙 삭제");
		m.put("Delete this secret", "이 비밀 삭제");
		m.put("Delete this state", "이 상태 삭제");
		m.put("Delete this tag", "이 태그 삭제");
		m.put("Delete this value", "이 값 삭제");
		m.put("Deleted source branch", "소스 브랜치 삭제됨");
		m.put("Deletion not allowed due to branch protection rule", "브랜치 보호 규칙으로 인해 삭제가 허용되지 않음");
		m.put("Deletion not allowed due to tag protection rule", "태그 보호 규칙으로 인해 삭제가 허용되지 않음");
		m.put("Deletions", "삭제 항목");
		m.put("Denied", "거부됨");
		m.put("Dependencies & Services", "종속성 및 서비스");
		m.put("Dependency Management", "종속성 관리");
		m.put("Dependency job finished", "종속성 작업 완료");
		m.put("Dependent Fields", "종속 필드");
		m.put("Depends on <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>", "<a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>에 따라 다름");
		m.put("Descending", "내림차순");
		m.put("Description", "설명");
		m.put("Description Template", "설명 템플릿");
		m.put("Description Templates", "설명 템플릿들");
		m.put("Description too long", "설명이 너무 깁니다");
		m.put("Destination Path", "대상 경로");
		m.put("Destinations", "대상");
		m.put("Detect Licenses", "라이선스 감지");
		m.put("Detect Secrets", "비밀 감지");
		m.put("Detect Vulnerabilities", "취약점 감지");
		m.put("Diff is too large to be displayed.", "차이가 너무 커서 표시할 수 없습니다.");
		m.put("Diff options", "차이 옵션");
		m.put("Digest", "다이제스트");
		m.put("Digest invalid", "다이제스트가 유효하지 않음");
		m.put("Directories to Skip", "건너뛸 디렉터리");
		m.put("Directory", "디렉터리");
		m.put("Directory (tenant) ID", "디렉터리(테넌트) ID");
		m.put("Disable", "비활성화");
		m.put("Disable All Queried Users", "쿼리된 모든 사용자 비활성화");
		m.put("Disable Auto Update Check", "자동 업데이트 확인 비활성화");
		m.put("Disable Dashboard", "대시보드 비활성화");
		m.put("Disable Selected Users", "선택한 사용자 비활성화");
		m.put("Disabled", "비활성화됨");
		m.put("Disabled users and service accounts are excluded from user-month calculation", "비활성화된 사용자 및 서비스 계정은 사용자-월 계산에서 제외됩니다");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"계정을 비활성화하면 비밀번호가 재설정되고, 액세스 토큰이 삭제되며, 과거 활동을 제외한 다른 엔티티에서 모든 참조가 제거됩니다. 계속하시겠습니까?");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"계정을 비활성화하면 비밀번호가 재설정되고, 액세스 토큰이 삭제되며, 과거 활동을 제외한 다른 엔티티에서 모든 참조가 제거됩니다. 확인하려면 <code>yes</code>를 입력하세요");
		m.put("Disallowed File Types", "허용되지 않는 파일 유형");
		m.put("Disallowed file type(s): {0}", "허용되지 않는 파일 유형: {0}");
		m.put("Discard", "폐기");
		m.put("Discard All Queried Pull Requests", "쿼리된 모든 풀 리퀘스트 폐기");
		m.put("Discard Selected Pull Requests", "선택한 풀 리퀘스트 폐기");
		m.put("Discarded", "폐기됨");
		m.put("Discarded pull request \"{0}\" ({1})", "풀 리퀘스트 \"{0}\" ({1}) 폐기됨");
		m.put("Discord", "디스코드");
		m.put("Discord Notifications", "디스코드 알림");
		m.put("Display Fields", "표시 필드");
		m.put("Display Links", "표시 링크");
		m.put("Display Months", "표시 월");
		m.put("Display Params", "표시 매개변수");
		m.put("Do Not Retrieve Groups", "그룹 검색하지 않음");
		m.put("Do not ignore", "무시하지 않음");
		m.put("Do not ignore whitespace", "공백을 무시하지 않음");
		m.put("Do not retrieve", "검색하지 않음");
		m.put("Do not retrieve groups", "그룹 검색하지 않음");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "\"{0}\"에 대한 초대를 정말 취소하시겠습니까?");
		m.put("Do you really want to cancel this build?", "이 빌드를 정말 취소하시겠습니까?");
		m.put("Do you really want to change target branch to {0}?", "대상 브랜치를 {0}(으)로 변경하시겠습니까?");
		m.put("Do you really want to delete \"{0}\"?", "\"{0}\"을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete SSO provider \"{0}\"?", "SSO 제공자 \"{0}\"를 정말로 삭제하시겠습니까?");
		m.put("Do you really want to delete board \"{0}\"?", "보드 \"{0}\"을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete build #{0}?", "빌드 #{0}을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete group \"{0}\"?", "그룹 \"{0}\"을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete iteration \"{0}\"?", "반복 \"{0}\"을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete job secret \"{0}\"?", "작업 비밀 \"{0}\"을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete pull request #{0}?", "풀 리퀘스트 #{0}을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete role \"{0}\"?", "역할 \"{0}\"을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete selected query watches?", "선택한 쿼리 감시를 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete tag {0}?", "태그 {0}을(를) 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this GPG key?", "이 GPG 키를 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this SSH key?", "이 SSH 키를 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this SSO account?", "이 SSO 계정을 정말로 삭제하시겠습니까?");
		m.put("Do you really want to delete this access token?", "이 액세스 토큰을 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this board?", "이 보드를 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this build?", "이 빌드를 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this code comment and all its replies?", "이 코드 주석과 모든 답글을 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this code comment?", "이 코드 주석을 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this directory?", "이 디렉터리를 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this email address?", "이 이메일 주소를 정말 삭제하시겠습니까?");
		m.put("Do you really want to delete this executor?", "정말로 이 실행자를 삭제하시겠습니까?");
		m.put("Do you really want to delete this field?", "정말로 이 필드를 삭제하시겠습니까?");
		m.put("Do you really want to delete this file?", "정말로 이 파일을 삭제하시겠습니까?");
		m.put("Do you really want to delete this issue?", "정말로 이 이슈를 삭제하시겠습니까?");
		m.put("Do you really want to delete this link?", "정말로 이 링크를 삭제하시겠습니까?");
		m.put("Do you really want to delete this package?", "정말로 이 패키지를 삭제하시겠습니까?");
		m.put("Do you really want to delete this privilege?", "정말로 이 권한을 삭제하시겠습니까?");
		m.put("Do you really want to delete this protection?", "정말로 이 보호를 삭제하시겠습니까?");
		m.put("Do you really want to delete this pull request?", "정말로 이 풀 리퀘스트를 삭제하시겠습니까?");
		m.put("Do you really want to delete this reply?", "정말로 이 답변을 삭제하시겠습니까?");
		m.put("Do you really want to delete this script?", "정말로 이 스크립트를 삭제하시겠습니까?");
		m.put("Do you really want to delete this state?", "정말로 이 상태를 삭제하시겠습니까?");
		m.put("Do you really want to delete this template?", "정말로 이 템플릿을 삭제하시겠습니까?");
		m.put("Do you really want to delete this transition?", "정말로 이 전환을 삭제하시겠습니까?");
		m.put("Do you really want to delete timesheet \"{0}\"?", "정말로 타임시트 \"{0}\"를 삭제하시겠습니까?");
		m.put("Do you really want to delete unused tokens?", "정말로 사용되지 않는 토큰을 삭제하시겠습니까?");
		m.put("Do you really want to discard batched suggestions?", "정말로 배치된 제안을 폐기하시겠습니까?");
		m.put("Do you really want to enable this account?", "정말로 이 계정을 활성화하시겠습니까?");
		m.put("Do you really want to rebuild?", "정말로 다시 빌드하시겠습니까?");
		m.put("Do you really want to remove assignee \"{0}\"?", "정말로 담당자 \"{0}\"를 제거하시겠습니까?");
		m.put("Do you really want to remove password of this user?", "정말로 이 사용자의 비밀번호를 제거하시겠습니까?");
		m.put("Do you really want to remove the issue from iteration \"{0}\"?", "정말로 이슈를 반복 작업 \"{0}\"에서 제거하시겠습니까?");
		m.put("Do you really want to remove this account?", "정말로 이 계정을 제거하시겠습니까?");
		m.put("Do you really want to remove this agent?", "정말로 이 에이전트를 제거하시겠습니까?");
		m.put("Do you really want to remove this link?", "정말로 이 링크를 제거하시겠습니까?");
		m.put("Do you really want to restart this agent?", "정말로 이 에이전트를 재시작하시겠습니까?");
		m.put("Do you really want to unauthorize user \"{0}\"?", "정말로 사용자 \"{0}\"의 권한을 해제하시겠습니까?");
		m.put("Do you really want to use default template?", "정말로 기본 템플릿을 사용하시겠습니까?");
		m.put("Docker", "도커");
		m.put("Docker Executable", "도커 실행 파일");
		m.put("Docker Hub", "도커 허브");
		m.put("Docker Image", "도커 이미지");
		m.put("Docker Sock Path", "도커 소켓 경로");
		m.put("Dockerfile", "도커파일");
		m.put("Documentation", "문서");
		m.put("Don't have an account yet?", "아직 계정이 없으신가요?");
		m.put("Download", "다운로드");
		m.put("Download <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> or <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. A new agent token will be included in the package", 
			"<a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> 또는 <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>를 다운로드하세요. 패키지에 새로운 에이전트 토큰이 포함됩니다.");
		m.put("Download archive of this branch", "이 브랜치의 아카이브를 다운로드하세요.");
		m.put("Download full log", "전체 로그를 다운로드하세요.");
		m.put("Download log", "로그를 다운로드하세요.");
		m.put("Download patch", "패치 다운로드");
		m.put("Download tag archive", "태그 아카이브를 다운로드하세요.");
		m.put("Dry Run", "드라이 런");
		m.put("Due Date", "마감일");
		m.put("Due Date Issue Field", "마감일 이슈 필드");
		m.put("Due date", "마감일");
		m.put("Duplicate authorizations found: ", "중복된 권한이 발견되었습니다:");
		m.put("Duplicate authorizations found: {0}", "중복된 권한이 발견되었습니다: {0}");
		m.put("Duration", "기간");
		m.put("Durations", "기간들");
		m.put("ESLint Report", "ESLint 보고서");
		m.put("Edit", "편집");
		m.put("Edit <code>$HOME/.gem/credentials</code> to add a source", "<code>$HOME/.gem/credentials</code>를 편집하여 소스를 추가하세요.");
		m.put("Edit <code>$HOME/.pypirc</code> to add a package repository like below", "<code>$HOME/.pypirc</code>를 편집하여 아래와 같은 패키지 저장소를 추가하세요.");
		m.put("Edit Avatar", "아바타 편집");
		m.put("Edit Estimated Time", "예상 시간 편집");
		m.put("Edit Executor", "실행자 편집");
		m.put("Edit Iteration", "반복 작업 편집");
		m.put("Edit Job Secret", "작업 비밀 편집");
		m.put("Edit My Avatar", "내 아바타 편집");
		m.put("Edit Rule", "규칙 편집");
		m.put("Edit Timesheet", "타임시트 편집");
		m.put("Edit dashboard", "대시보드 편집");
		m.put("Edit issue title", "이슈 제목 편집");
		m.put("Edit job", "작업 편집");
		m.put("Edit on branch {0}", "브랜치 {0}에서 편집");
		m.put("Edit on source branch", "소스 브랜치에서 편집");
		m.put("Edit plain", "일반 편집");
		m.put("Edit saved queries", "저장된 쿼리 편집");
		m.put("Edit this access token", "이 액세스 토큰 편집");
		m.put("Edit this executor", "이 실행자 편집");
		m.put("Edit this iteration", "이 반복 작업 편집");
		m.put("Edit this rule", "이 규칙 편집");
		m.put("Edit this secret", "이 비밀 편집");
		m.put("Edit this state", "이 상태 편집");
		m.put("Edit title", "제목 편집");
		m.put("Edit with AI", "AI로 편집");
		m.put("Edit {0}", "{0} 편집");
		m.put("Editable Issue Fields", "편집 가능한 이슈 필드");
		m.put("Editable Issue Links", "편집 가능한 이슈 링크");
		m.put("Edited by {0} {1}", "{0} {1}에 의해 편집됨");
		m.put("Editor", "편집기");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "대상 브랜치 또는 소스 브랜치에 방금 새로운 커밋이 추가되었습니다. 다시 확인하세요.");
		m.put("Email", "이메일");
		m.put("Email Address", "이메일 주소");
		m.put("Email Address Verification", "이메일 주소 확인");
		m.put("Email Addresses", "이메일 주소들");
		m.put("Email Templates", "이메일 템플릿");
		m.put("Email Verification", "이메일 인증");
		m.put("Email Verification Template", "이메일 인증 템플릿");
		m.put("Email address", "이메일 주소");
		m.put("Email address \"{0}\" already used by another account", "이메일 주소 \"{0}\"는 다른 계정에서 이미 사용 중입니다");
		m.put("Email address \"{0}\" used by account \"{1}\"", "이메일 주소 \"{0}\"는 계정 \"{1}\"에서 사용 중입니다");
		m.put("Email address \"{0}\" used by disabled account \"{1}\"", "이메일 주소 \"{0}\"는 비활성화된 계정 \"{1}\"에서 사용 중입니다");
		m.put("Email address already in use: {0}", "이미 사용 중인 이메일 주소: {0}");
		m.put("Email address already invited: {0}", "이미 초대된 이메일 주소: {0}");
		m.put("Email address already used by another user", "다른 사용자가 이미 사용 중인 이메일 주소");
		m.put("Email address already used: ", "이미 사용된 이메일 주소:");
		m.put("Email address to verify", "인증할 이메일 주소");
		m.put("Email addresses with <span class=\"badge badge-warning badge-sm\">ineffective</span> mark are those not belong to or not verified by key owner", 
			"<span class=\"badge badge-warning badge-sm\">비효과적</span> 표시가 있는 이메일 주소는 키 소유자에 속하지 않거나 인증되지 않은 주소입니다.");
		m.put("Email templates", "이메일 템플릿");
		m.put("Empty file added.", "빈 파일이 추가되었습니다.");
		m.put("Empty file removed.", "빈 파일이 제거되었습니다.");
		m.put("Enable", "활성화");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets", 
			"이 프로젝트에 대한 <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>시간 추적</a>을 활성화하여 진행 상황을 추적하고 타임시트를 생성하세요.");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project", 
			"이 프로젝트에 대해 <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>패키지 관리</a>를 활성화합니다");
		m.put("Enable Account Self Removal", "계정 자가 삭제를 활성화합니다");
		m.put("Enable Account Self Sign-Up", "계정 자가 가입을 활성화합니다");
		m.put("Enable All Queried Users", "모든 조회된 사용자를 활성화합니다");
		m.put("Enable Anonymous Access", "익명 액세스를 활성화합니다");
		m.put("Enable Auto Backup", "자동 백업을 활성화합니다");
		m.put("Enable Html Report Publish", "HTML 보고서 게시 활성화");
		m.put("Enable Selected Users", "선택된 사용자를 활성화합니다");
		m.put("Enable Site Publish", "사이트 게시 활성화");
		m.put("Enable TTY Mode", "TTY 모드를 활성화합니다");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "<a wicket:id=\"addFile\" class=\"link-primary\"></a>를 통해 빌드 지원을 활성화합니다");
		m.put("Enable if visibility of this field depends on other fields", "이 필드의 가시성이 다른 필드에 따라 달라지는 경우 활성화합니다");
		m.put("Enable if visibility of this param depends on other params", "이 매개변수의 가시성이 다른 매개변수에 따라 달라지는 경우 활성화합니다");
		m.put("Enable this if the access token has same permissions as the owner", "액세스 토큰이 소유자와 동일한 권한을 가진 경우 이를 활성화합니다");
		m.put("Enable this option to merge the pull request automatically when ready (all reviewers approved, all required jobs passed etc.)", 
			"준비되었을 때(모든 리뷰어 승인, 모든 필수 작업 통과 등) 풀 리퀘스트를 자동으로 병합하도록 이 옵션을 활성화합니다");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"HTML 보고서 게시 단계를 실행할 수 있도록 활성화합니다. XSS 공격을 방지하려면 이 실행자가 신뢰할 수 있는 작업에서만 사용되도록 해야 합니다");
		m.put("Enable this to allow to run site publish step. OneDev will serve project site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"사이트 게시 단계를 실행할 수 있도록 허용하려면 이를 활성화합니다. OneDev는 프로젝트 사이트 파일을 그대로 제공합니다. XSS 공격을 방지하려면 이 실행자가 신뢰할 수 있는 작업에서만 사용되도록 해야 합니다");
		m.put("Enable this to place intermediate files required by job execution on dynamically allocated persistent volume instead of emptyDir", 
			"emptyDir 대신 동적으로 할당된 영구 볼륨에 작업 실행에 필요한 중간 파일을 배치하려면 이를 활성화합니다");
		m.put("Enable this to process issue or pull request comments posted via email", "이메일을 통해 게시된 이슈 또는 풀 리퀘스트 댓글을 처리하려면 이를 활성화합니다");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"이메일을 통해 게시된 이슈 또는 풀 리퀘스트 댓글을 처리하려면 이를 활성화합니다. <b class='text-danger'>참고:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>서브 어드레싱</a>이 위 시스템 이메일 주소에 대해 활성화되어야 합니다. OneDev는 이를 사용하여 이슈 및 풀 리퀘스트 컨텍스트를 추적합니다");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"이메일을 통해 게시된 이슈 또는 풀 리퀘스트 댓글을 처리하려면 이를 활성화합니다. <b class='text-danger'>참고:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>서브 어드레싱</a>이 위 시스템 이메일 주소에 대해 활성화되어야 합니다. OneDev는 이를 사용하여 이슈 및 풀 리퀘스트 컨텍스트를 추적합니다");
		m.put("Enable to allow to upload build cache generated during CI/CD job. Uploaded cache can be used by subsequent builds of the project as long as cache key matches", 
			"CI/CD 작업 중 생성된 빌드 캐시를 업로드할 수 있도록 허용하려면 이를 활성화합니다. 업로드된 캐시는 캐시 키가 일치하는 한 프로젝트의 후속 빌드에서 사용할 수 있습니다");
		m.put("End Point", "엔드 포인트");
		m.put("Enforce Conventional Commits", "컨벤션 커밋을 강제합니다");
		m.put("Enforce Password Policy", "비밀번호 정책 시행");
		m.put("Enforce Two-factor Authentication", "이중 인증을 강제합니다");
		m.put("Enforce password policy for new users", "새 사용자에 대한 비밀번호 정책 시행");
		m.put("Enter New Password", "새 비밀번호 입력");
		m.put("Enter description here", "여기에 설명을 입력하세요");
		m.put("Enter your details to login to your account", "계정에 로그인하려면 세부 정보를 입력하세요");
		m.put("Enter your user name or email to reset password", "비밀번호를 재설정하려면 사용자 이름 또는 이메일을 입력하세요");
		m.put("Entries", "항목들");
		m.put("Entry", "항목");
		m.put("Enumeration", "열거형");
		m.put("Env Var", "환경 변수");
		m.put("Environment Variables", "환경 변수들");
		m.put("Environment variable <code>serverUrl</code> in above command is taken from OneDev server url specified in <i>Administration / System Setting</i>. Change it if necessary", 
			"위 명령에서 <code>serverUrl</code> 환경 변수는 <i>관리 / 시스템 설정</i>에 지정된 OneDev 서버 URL에서 가져옵니다. 필요하면 변경하세요");
		m.put("Equal", "동일");
		m.put("Error authenticating user", "사용자 인증 오류");
		m.put("Error calculating commits: check log for details", "커밋 계산 오류: 세부 사항은 로그를 확인하세요");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "{0}로 체리픽킹 오류: 병합 충돌이 감지되었습니다");
		m.put("Error cherry-picking to {0}: {1}", "{0}로 체리픽킹 오류: {1}");
		m.put("Error detail of content type &quot;text/plain&quot;", "콘텐츠 유형 &quot;text/plain&quot;의 오류 세부 정보");
		m.put("Error discovering OIDC metadata", "OIDC 메타데이터 검색 오류");
		m.put("Error executing task", "작업 실행 오류");
		m.put("Error parsing %sbase query: ", "%s기본 쿼리 구문 분석 오류:");
		m.put("Error parsing %squery: ", "%s쿼리 구문 분석 오류:");
		m.put("Error parsing build spec", "빌드 사양 구문 분석 오류");
		m.put("Error rendering widget, check server log for details", "위젯 렌더링 오류, 서버 로그에서 세부 사항을 확인하세요");
		m.put("Error reverting on {0}: Merge conflicts detected", "{0}에서 되돌리기 오류: 병합 충돌이 감지되었습니다");
		m.put("Error reverting on {0}: {1}", "{0}에서 되돌리기 오류: {1}");
		m.put("Error validating auto merge commit message: {0}", "자동 병합 커밋 메시지 유효성 검사 오류: {0}");
		m.put("Error validating build spec (location: {0}, error message: {1})", "빌드 사양 유효성 검사 오류(위치: {0}, 오류 메시지: {1})");
		m.put("Error validating build spec: {0}", "빌드 사양 유효성 검사 오류: {0}");
		m.put("Error validating commit message of \"{0}\": {1}", "\"{0}\"의 커밋 메시지 유효성 검사 오류: {1}");
		m.put("Error validating commit message of <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}", 
			"<a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>의 커밋 메시지 유효성 검사 오류: {2}");
		m.put("Error verifying GPG signature", "GPG 서명 확인 오류");
		m.put("Estimated Time", "예상 시간");
		m.put("Estimated Time Edit Bean", "예상 시간 편집 빈");
		m.put("Estimated Time Issue Field", "예상 시간 이슈 필드");
		m.put("Estimated Time:", "예상 시간:");
		m.put("Estimated time", "예상 시간");
		m.put("Estimated/Spent time. Click for details", "예상/소요 시간. 세부 사항을 보려면 클릭하세요");
		m.put("Evaluate script to get choices", "선택지를 얻기 위해 스크립트를 평가합니다");
		m.put("Evaluate script to get default value", "기본값을 얻기 위해 스크립트를 평가합니다");
		m.put("Evaluate script to get value or secret", "값 또는 비밀을 얻기 위해 스크립트를 평가합니다");
		m.put("Evaluate script to get values or secrets", "값 또는 비밀들을 얻기 위해 스크립트를 평가합니다");
		m.put("Event Types", "이벤트 유형들");
		m.put("Events", "이벤트");
		m.put("Ever Used Since", "이후로 사용됨");
		m.put("Everything inside this project and all child projects will be deleted and can not be recovered, please type project path <code>{0}</code> below to confirm deletion.", 
			"이 프로젝트와 모든 하위 프로젝트 내의 모든 것이 삭제되며 복구할 수 없습니다. 삭제를 확인하려면 아래에 프로젝트 경로 <code>{0}</code>를 입력하세요");
		m.put("Example", "예제");
		m.put("Example Plugin Setting", "예제 플러그인 설정");
		m.put("Example Property", "예제 속성");
		m.put("Exclude Param Combos", "매개변수 조합 제외");
		m.put("Exclude States", "상태 제외");
		m.put("Excluded", "제외됨");
		m.put("Excluded Fields", "제외된 필드들");
		m.put("Executable", "실행 가능");
		m.put("Execute Commands", "명령 실행");
		m.put("Execute Commands via SSH", "SSH를 통해 명령 실행");
		m.put("Exit Impersonation", "가장 역할 종료");
		m.put("Exited impersonation", "가장 역할 종료됨");
		m.put("Expand all", "모두 확장");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", 
			"하나 이상의 <tt>&lt;number&gt;(h|m)</tt>을 기대합니다. 예를 들어 <tt>1h 1m</tt>은 1시간과 1분을 나타냅니다");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", 
			"하나 이상의 <tt>&lt;number&gt;(w|d|h|m)</tt>을 기대합니다. 예를 들어 <tt>1w 1d 1h 1m</tt>은 1주({0}일), 1일({1}시간), 1시간, 1분을 나타냅니다");
		m.put("Expiration Date:", "만료 날짜:");
		m.put("Expire Date", "만료 날짜");
		m.put("Expired", "만료됨");
		m.put("Explicit SSL (StartTLS)", "명시적 SSL (StartTLS)");
		m.put("Export", "내보내기");
		m.put("Export All Queried Issues To...", "모든 조회된 이슈를 내보내기...");
		m.put("Export CSV", "CSV 내보내기");
		m.put("Export XLSX", "XLSX 내보내기");
		m.put("Export as OCI layout", "OCI 레이아웃으로 내보내기");
		m.put("Extend Trial Subscription", "체험 구독 연장");
		m.put("External Authentication", "외부 인증");
		m.put("External Issue Transformers", "외부 이슈 변환기들");
		m.put("External Participants", "외부 참가자들");
		m.put("External Password Authenticator", "외부 비밀번호 인증기");
		m.put("External System", "외부 시스템");
		m.put("External authenticator settings saved", "외부 인증자 설정이 저장되었습니다");
		m.put("External participants do not have accounts and involve in the issue via email", "외부 참가자는 계정이 없으며 이메일을 통해 이슈에 참여합니다");
		m.put("Extract the package into a folder. <b class=\"text-danger\">Warning:</b> On Mac OS X, do not extract to Mac managed folders such as Downloads, Desktop, Documents; otherwise you may encounter permission issues starting agent", 
			"패키지를 폴더에 추출하세요. <b class=\"text-danger\">경고:</b> Mac OS X에서는 Downloads, Desktop, Documents와 같은 Mac 관리 폴더에 추출하지 마세요. 그렇지 않으면 에이전트를 시작할 때 권한 문제가 발생할 수 있습니다");
		m.put("FAILED", "실패");
		m.put("Fail Threshold", "실패 임계값");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"지정된 심각도 수준 이상의 취약점이 있는 경우 빌드를 실패로 처리합니다");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"지정된 심각도 수준 이상의 취약점이 있는 경우 빌드를 실패로 처리합니다. 이는 빌드가 다른 단계에 의해 실패하지 않은 경우에만 적용됩니다");
		m.put("Failed", "실패함");
		m.put("Failed to validate build spec import. Check server log for details", "빌드 사양 가져오기 유효성 검사 실패. 서버 로그에서 세부 사항을 확인하세요");
		m.put("Failed to verify your email address", "이메일 주소를 확인하지 못했습니다");
		m.put("Field Bean", "필드 빈");
		m.put("Field Instance", "필드 인스턴스");
		m.put("Field Name", "필드 이름");
		m.put("Field Spec", "필드 사양");
		m.put("Field Specs", "필드 사양들");
		m.put("Field Value", "필드 값");
		m.put("Fields", "필드들");
		m.put("Fields & Links", "필드 및 링크");
		m.put("Fields And Links Bean", "필드 및 링크 빈");
		m.put("Fields to Change", "변경할 필드들");
		m.put("File", "파일");
		m.put("File Changes", "파일 변경 사항");
		m.put("File Name", "파일 이름");
		m.put("File Name Patterns (separated by comma)", "파일 이름 패턴 (쉼표로 구분)");
		m.put("File Path", "파일 경로");
		m.put("File Patterns", "파일 패턴");
		m.put("File Protection", "파일 보호");
		m.put("File Protections", "파일 보호들");
		m.put("File and Symbol Search", "파일 및 심볼 검색");
		m.put("File changes", "파일 변경 사항");
		m.put("File is too large to edit here", "파일이 너무 커서 여기서 편집할 수 없습니다");
		m.put("File missing or obsolete", "파일 누락 또는 오래됨");
		m.put("File name", "파일 이름");
		m.put("File name patterns such as *.java, *.c", "파일 이름 패턴 예: *.java, *.c");
		m.put("Files", "파일들");
		m.put("Files to Be Analyzed", "분석할 파일들");
		m.put("Filter", "필터");
		m.put("Filter Issues", "문제 필터링");
		m.put("Filter actions", "작업 필터링");
		m.put("Filter backlog issues", "백로그 문제 필터링");
		m.put("Filter branches...", "브랜치 필터링...");
		m.put("Filter by name", "이름으로 필터링");
		m.put("Filter by name or email address", "이름 또는 이메일 주소로 필터링");
		m.put("Filter by name...", "이름으로 필터링...");
		m.put("Filter by path", "경로로 필터링");
		m.put("Filter by test suite", "테스트 스위트로 필터링");
		m.put("Filter date range", "날짜 범위 필터링");
		m.put("Filter files...", "파일 필터링...");
		m.put("Filter groups...", "그룹 필터링...");
		m.put("Filter issues", "문제 필터링");
		m.put("Filter pull requests", "풀 리퀘스트 필터링");
		m.put("Filter roles", "역할 필터링");
		m.put("Filter tags...", "태그 필터링...");
		m.put("Filter targets", "대상 필터링");
		m.put("Filter users", "사용자 필터링");
		m.put("Filter...", "필터링...");
		m.put("Filters", "필터들");
		m.put("Find branch", "브랜치 찾기");
		m.put("Find or create branch", "브랜치 찾기 또는 생성");
		m.put("Find or create tag", "태그 찾기 또는 생성");
		m.put("Find tag", "태그 찾기");
		m.put("Fingerprint", "지문");
		m.put("Finish", "완료");
		m.put("First applicable executor", "첫 번째 적용 가능한 실행기");
		m.put("Fix", "수정");
		m.put("Fix Type", "수정 유형");
		m.put("Fix Undefined Field Values", "정의되지 않은 필드 값 수정");
		m.put("Fix Undefined Fields", "정의되지 않은 필드 수정");
		m.put("Fix Undefined States", "정의되지 않은 상태 수정");
		m.put("Fixed Issues", "수정된 문제들");
		m.put("Fixed issues since...", "이후 수정된 문제들...");
		m.put("Fixing Builds", "빌드 수정 중");
		m.put("Fixing Commits", "커밋 수정 중");
		m.put("Fixing...", "수정 중...");
		m.put("Float", "부동");
		m.put("Follow below instructions to publish packages into this project", "아래 지침을 따라 이 프로젝트에 패키지를 게시하세요");
		m.put("Follow below steps to install agent on remote machine (supports Linux/Windows/Mac OS X/FreeBSD):", 
			"원격 머신에 에이전트를 설치하려면 아래 단계를 따르세요 (Linux/Windows/Mac OS X/FreeBSD 지원):");
		m.put("For CI/CD job, add this gem to Gemfile like below", "CI/CD 작업의 경우, 아래와 같이 Gemfile에 이 gem을 추가하세요");
		m.put("For CI/CD job, add this package to requirements.txt and run below to install the package via command step", 
			"CI/CD 작업의 경우, requirements.txt에 이 패키지를 추가하고 아래 명령 단계를 실행하여 패키지를 설치하세요");
		m.put("For CI/CD job, run below to add package repository via command step", "CI/CD 작업의 경우, 아래 명령 단계를 실행하여 패키지 저장소를 추가하세요");
		m.put("For CI/CD job, run below to add package source via command step", "CI/CD 작업의 경우, 아래 명령 단계를 실행하여 패키지 소스를 추가하세요");
		m.put("For CI/CD job, run below to add source via command step", "CI/CD 작업의 경우, 아래 명령 단계를 실행하여 소스를 추가하세요");
		m.put("For CI/CD job, run below to install chart via command step", "CI/CD 작업의 경우, 아래 명령 단계를 실행하여 차트를 설치하세요");
		m.put("For CI/CD job, run below to publish package via command step", "CI/CD 작업의 경우, 아래 명령 단계를 실행하여 패키지를 게시하세요");
		m.put("For CI/CD job, run below to push chart to the repository via command step", "CI/CD 작업의 경우, 아래 명령 단계를 실행하여 차트를 저장소에 푸시하세요");
		m.put("For CI/CD job, run below via a command step", "CI/CD 작업의 경우, 아래 명령 단계를 실행하세요");
		m.put("For a particular project, the first matching entry will be used", "특정 프로젝트의 경우, 첫 번째 일치 항목이 사용됩니다");
		m.put("For all issues", "모든 문제에 대해");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"기본 브랜치에서 접근할 수 없는 빌드 커밋의 경우, <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 생성 브랜치 권한이 있는 액세스 토큰으로 지정해야 합니다");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"기본 브랜치에서 접근할 수 없는 빌드 커밋의 경우, <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 생성 태그 권한이 있는 액세스 토큰으로 지정해야 합니다");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"기본 브랜치에서 접근할 수 없는 빌드 커밋의 경우, <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 문제 관리 권한이 있는 액세스 토큰으로 지정해야 합니다");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"도커 인식 실행기의 경우, 이 경로는 컨테이너 내부에 있으며 절대 경로와 상대 경로( <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 기준 상대 경로)를 모두 허용합니다. 호스트 머신에서 직접 실행되는 쉘 관련 실행기의 경우, 상대 경로만 허용됩니다");
		m.put("For each build, OneDev calculates a list of fixed issues since previous build automatically. This setting provides a default query to further filter/order this list. For a given job, the first matching entry will be used.", 
			"각 빌드마다 OneDev는 이전 빌드 이후 수정된 문제 목록을 자동으로 계산합니다. 이 설정은 이 목록을 추가로 필터링/정렬하기 위한 기본 쿼리를 제공합니다. 특정 작업의 경우, 첫 번째 일치 항목이 사용됩니다.");
		m.put("For each selected branch/tag, a separate build will be generated with branch/tag set to corresponding value", 
			"선택된 각 브랜치/태그에 대해 별도의 빌드가 생성되며 브랜치/태그는 해당 값으로 설정됩니다");
		m.put("For issues matching: ", "일치하는 문제에 대해:");
		m.put("For very large git repository, you may need to tune options here to reduce memory usage", 
			"매우 큰 Git 저장소의 경우, 메모리 사용량을 줄이기 위해 여기에서 옵션을 조정해야 할 수 있습니다");
		m.put("For web hooks defined here and in parent projects, OneDev will post event data in JSON format to specified URLs when subscribed events happen", 
			"여기와 상위 프로젝트에 정의된 웹 훅에 대해, OneDev는 구독된 이벤트가 발생할 때 지정된 URL로 JSON 형식의 이벤트 데이터를 게시합니다");
		m.put("Force", "강제");
		m.put("Force Garbage Collection", "강제 가비지 컬렉션");
		m.put("Forgot Password?", "비밀번호를 잊으셨나요?");
		m.put("Forgotten Password?", "비밀번호를 잊으셨나요?");
		m.put("Fork Project", "프로젝트 포크");
		m.put("Fork now", "지금 포크하기");
		m.put("Forks Of", "포크된 프로젝트");
		m.put("Frequencies", "빈도");
		m.put("From Directory", "디렉토리에서");
		m.put("From States", "상태에서");
		m.put("From extracted folder, run <code>bin\\agent.bat console</code> as administrator on Windows or <code>bin/agent.sh console</code> on other OS", 
			"추출된 폴더에서, Windows에서는 <code>bin\\agent.bat console</code>을 관리자 권한으로 실행하거나 다른 OS에서는 <code>bin/agent.sh console</code>을 실행하세요");
		m.put("From {0}", "{0}에서");
		m.put("Full Name", "전체 이름");
		m.put("Furthest due date", "가장 늦은 마감일");
		m.put("GPG Keys", "GPG 키");
		m.put("GPG Public Key", "GPG 공개 키");
		m.put("GPG Signing Key", "GPG 서명 키");
		m.put("GPG Trusted Keys", "GPG 신뢰 키");
		m.put("GPG key deleted", "GPG 키 삭제됨");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "GPG 공개 키는 '-----BEGIN PGP PUBLIC KEY BLOCK-----'로 시작합니다");
		m.put("GPG signing key will be used to sign commits generated by OneDev, including pull request merge commits, user commits created via web UI or RESTful api.", 
			"GPG 서명 키는 OneDev에서 생성된 커밋(풀 리퀘스트 병합 커밋, 웹 UI 또는 RESTful API를 통해 생성된 사용자 커밋 포함)을 서명하는 데 사용됩니다");
		m.put("Gem Info", "젬 정보");
		m.put("General", "일반");
		m.put("General Settings", "일반 설정");
		m.put("General settings updated", "일반 설정이 업데이트되었습니다");
		m.put("Generate", "생성");
		m.put("Generate File Checksum", "파일 체크섬 생성");
		m.put("Generate New", "새로 생성");
		m.put("Generic LDAP", "일반 LDAP");
		m.put("Get", "가져오기");
		m.put("Get Groups Using Attribute", "속성을 사용하여 그룹 가져오기");
		m.put("Git", "깃");
		m.put("Git Command Line", "깃 명령줄");
		m.put("Git Credential", "깃 자격 증명");
		m.put("Git LFS Storage", "깃 LFS 저장소");
		m.put("Git Lfs Lock", "깃 LFS 잠금");
		m.put("Git Location", "깃 위치");
		m.put("Git Pack Config", "깃 팩 구성");
		m.put("Git Path", "깃 경로");
		m.put("Git email address will be used as git author/committer for commits created on web UI", 
			"깃 이메일 주소는 웹 UI에서 생성된 커밋의 작성자/커미터로 사용됩니다");
		m.put("Git pack config updated", "깃 팩 구성이 업데이트되었습니다");
		m.put("GitHub", "깃허브");
		m.put("GitHub API URL", "깃허브 API URL");
		m.put("GitHub Issue Label", "깃허브 이슈 라벨");
		m.put("GitHub Organization", "깃허브 조직");
		m.put("GitHub Personal Access Token", "깃허브 개인 액세스 토큰");
		m.put("GitHub Repositories to Import", "가져올 깃허브 저장소");
		m.put("GitHub Repository", "깃허브 저장소");
		m.put("GitHub personal access token should be generated with scope <b>repo</b> and <b>read:org</b>", 
			"깃허브 개인 액세스 토큰은 <b>repo</b> 및 <b>read:org</b> 범위로 생성되어야 합니다");
		m.put("GitLab API URL", "깃랩 API URL");
		m.put("GitLab Group", "깃랩 그룹");
		m.put("GitLab Issue Label", "깃랩 이슈 라벨");
		m.put("GitLab Personal Access Token", "깃랩 개인 액세스 토큰");
		m.put("GitLab Project", "깃랩 프로젝트");
		m.put("GitLab Projects to Import", "가져올 깃랩 프로젝트");
		m.put("GitLab personal access token should be generated with scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by user of specified access token will be listed", 
			"깃랩 개인 액세스 토큰은 <b>read_api</b>, <b>read_user</b>, <b>read_repository</b> 범위로 생성되어야 합니다. 지정된 액세스 토큰의 사용자 소유 그룹/프로젝트만 나열됩니다");
		m.put("Gitea API URL", "기테아 API URL");
		m.put("Gitea Issue Label", "기테아 이슈 라벨");
		m.put("Gitea Organization", "기테아 조직");
		m.put("Gitea Personal Access Token", "기테아 개인 액세스 토큰");
		m.put("Gitea Repositories to Import", "가져올 기테아 저장소");
		m.put("Gitea Repository", "기테아 저장소");
		m.put("Github Access Token Secret", "깃허브 액세스 토큰 비밀");
		m.put("Global", "글로벌");
		m.put("Global Build Setting", "글로벌 빌드 설정");
		m.put("Global Issue Setting", "글로벌 이슈 설정");
		m.put("Global Pack Setting", "글로벌 팩 설정");
		m.put("Global Views", "글로벌 보기");
		m.put("Gmail", "지메일");
		m.put("Go Back", "돌아가기");
		m.put("Google Test Report", "구글 테스트 보고서");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Gpg 키");
		m.put("Great, your mail service configuration is working", "메일 서비스 구성이 정상적으로 작동합니다");
		m.put("Groovy Script", "그루비 스크립트");
		m.put("Groovy Scripts", "그루비 스크립트들");
		m.put("Groovy script to be evaluated. It should return a <i>Date</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. <i>Date</i> 값을 반환해야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. It should return a <i>Float</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. <i>Float</i> 값을 반환해야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. It should return a <i>Integer</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. <i>Integer</i> 값을 반환해야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. It should return a <i>String</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. <i>String</i> 값을 반환해야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. It should return a <i>boolean</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. <i>boolean</i> 값을 반환해야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. It should return a <i>string</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. <i>string</i> 값을 반환해야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. It should return name of a group. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. 그룹 이름을 반환해야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. It should return string or list of string. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. 문자열 또는 문자열 목록을 반환해야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. 반환 값은 선택 항목으로 사용할 그룹 파사드 객체 목록이어야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. The return value should be a list of user login names to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. 반환 값은 선택 항목으로 사용할 사용자 로그인 이름 목록이어야 합니다. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy script to be evaluated. The return value should be a value to color map, for instance:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> if the value does not have a color. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"평가할 그루비 스크립트입니다. 반환 값은 값-색상 맵이어야 합니다. 예를 들어:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, 값에 색상이 없는 경우 <tt>null</tt>을 사용하세요. 자세한 내용은 <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>스크립팅 도움말</a>을 확인하세요");
		m.put("Groovy scripts", "그루비 스크립트들");
		m.put("Group", "그룹");
		m.put("Group \"{0}\" deleted", "그룹 \"{0}\"이(가) 삭제되었습니다");
		m.put("Group Authorization Bean", "그룹 권한 부여 빈");
		m.put("Group Authorizations", "그룹 권한 부여");
		m.put("Group Authorizations Bean", "그룹 권한 부여 빈");
		m.put("Group By", "그룹별");
		m.put("Group Management", "그룹 관리");
		m.put("Group Name Attribute", "그룹 이름 속성");
		m.put("Group Retrieval", "그룹 검색");
		m.put("Group Search Base", "그룹 검색 기준");
		m.put("Group Search Filter", "그룹 검색 필터");
		m.put("Group authorizations updated", "그룹 권한 부여가 업데이트되었습니다");
		m.put("Group created", "그룹이 생성되었습니다");
		m.put("Groups", "그룹들");
		m.put("Groups Claim", "그룹 클레임");
		m.put("Guide Line", "가이드라인");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "HTTP(S) 클론 URL");
		m.put("Has Owner Permissions", "소유자 권한 있음");
		m.put("Has Running Builds", "실행 중인 빌드 있음");
		m.put("Heap Memory Usage", "힙 메모리 사용량");
		m.put("Helm(s)", "헬름(들)");
		m.put("Help", "도움말");
		m.put("Hide", "숨기기");
		m.put("Hide Archived", "보관된 항목 숨기기");
		m.put("Hide comment", "댓글 숨기기");
		m.put("Hide saved queries", "저장된 쿼리 숨기기");
		m.put("High", "높음");
		m.put("High Availability & Scalability", "고가용성 및 확장성");
		m.put("High Severity", "높은 심각도");
		m.put("History", "기록");
		m.put("History of comparing revisions is unrelated", "리비전 비교 기록이 관련이 없습니다");
		m.put("History of target branch and source branch is unrelated", "대상 브랜치와 소스 브랜치의 기록이 관련이 없습니다");
		m.put("Host name or ip address of remote machine to run commands via SSH", "SSH를 통해 명령을 실행할 원격 머신의 호스트 이름 또는 IP 주소");
		m.put("Hours Per Day", "하루당 시간");
		m.put("How to Publish", "게시 방법");
		m.put("Html Report", "HTML 보고서");
		m.put("Http Method", "Http 메서드");
		m.put("I didn't eat it. I swear!", "나는 그것을 먹지 않았어. 맹세해!");
		m.put("ID token was expired", "ID 토큰이 만료되었습니다");
		m.put("IMAP Host", "IMAP 호스트");
		m.put("IMAP Password", "IMAP 비밀번호");
		m.put("IMAP User", "IMAP 사용자");
		m.put("IMPORTANT:", "중요:");
		m.put("IP Address", "IP 주소");
		m.put("Id", "ID");
		m.put("Identify Field", "식별 필드");
		m.put("If enabled, scheduled backup will run on lead server which is <span wicket:id=\"leadServer\"></span> currently", 
			"활성화되면 예약된 백업이 현재 <span wicket:id=\"leadServer\"></span> 리드 서버에서 실행됩니다");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"활성화되면 사용자가 권한이 있는 경우 풀 리퀘스트 병합 후 소스 브랜치가 자동으로 삭제됩니다");
		m.put("If specified, OneDev will only display iterations with this prefix", "지정된 경우, OneDev는 이 접두사가 있는 반복만 표시합니다");
		m.put("If specified, all public and internal projects imported from GitLab will use these as default roles. Private projects are not affected", 
			"지정된 경우, GitLab에서 가져온 모든 공개 및 내부 프로젝트는 이를 기본 역할로 사용합니다. 비공개 프로젝트는 영향을 받지 않습니다");
		m.put("If specified, all public repositories imported from GitHub will use these as default roles. Private repositories are not affected", 
			"지정된 경우, GitHub에서 가져온 모든 공개 저장소는 이를 기본 역할로 사용합니다. 비공개 저장소는 영향을 받지 않습니다");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"지정된 경우, 문제의 총 예상/소요 시간에 이 유형의 연결된 문제도 포함됩니다");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"이 옵션이 활성화되면, git lfs 명령이 OneDev 서버에 설치되어야 합니다 (이 단계가 다른 노드에서 실행되더라도)");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"선택된 경우, 이 필드로 표시된 그룹은 시간 추적이 활성화된 경우 해당 문제의 예상 시간을 편집할 수 있습니다");
		m.put("Ignore", "무시");
		m.put("Ignore File", "무시 파일");
		m.put("Ignore activities irrelevant to me", "나와 관련 없는 활동 무시");
		m.put("Ignore all", "모두 무시");
		m.put("Ignore all whitespace", "모든 공백 무시");
		m.put("Ignore change", "변경 사항 무시");
		m.put("Ignore change whitespace", "변경 사항 공백 무시");
		m.put("Ignore leading", "선행 무시");
		m.put("Ignore leading whitespace", "선행 공백 무시");
		m.put("Ignore this field", "이 필드 무시");
		m.put("Ignore this param", "이 매개변수 무시");
		m.put("Ignore trailing", "후행 무시");
		m.put("Ignore trailing whitespace", "후행 공백 무시");
		m.put("Ignored Licenses", "무시된 라이선스");
		m.put("Image", "이미지");
		m.put("Image Labels", "이미지 레이블");
		m.put("Image Manifest", "이미지 매니페스트");
		m.put("Image Size", "이미지 크기");
		m.put("Image Text", "이미지 텍스트");
		m.put("Image URL", "이미지 URL");
		m.put("Image URL should be specified", "이미지 URL을 지정해야 합니다");
		m.put("Imap Ssl Setting", "IMAP SSL 설정");
		m.put("Imap With Ssl", "IMAP SSL 사용");
		m.put("Impersonate", "가장하다");
		m.put("Implicit SSL", "암시적 SSL");
		m.put("Import", "가져오기");
		m.put("Import All Projects", "모든 프로젝트 가져오기");
		m.put("Import All Repositories", "모든 저장소 가져오기");
		m.put("Import Group", "그룹 가져오기");
		m.put("Import Issues", "문제 가져오기");
		m.put("Import Option", "가져오기 옵션");
		m.put("Import Organization", "조직 가져오기");
		m.put("Import Project", "프로젝트 가져오기");
		m.put("Import Projects", "프로젝트들 가져오기");
		m.put("Import Repositories", "저장소들 가져오기");
		m.put("Import Repository", "저장소 가져오기");
		m.put("Import Server", "서버 가져오기");
		m.put("Import Workspace", "작업 공간 가져오기");
		m.put("Import build spec elements (jobs, services, step templates and properties) from other projects. Imported elements are treated as if they are defined locally. Locally defined elements will override imported elements with same name", 
			"다른 프로젝트에서 빌드 사양 요소(작업, 서비스, 단계 템플릿 및 속성)를 가져옵니다. 가져온 요소는 로컬에서 정의된 것처럼 처리됩니다. 로컬에서 정의된 요소는 동일한 이름의 가져온 요소를 덮어씁니다");
		m.put("Importing Issues from {0}", "{0}에서 문제 가져오는 중");
		m.put("Importing from {0}", "{0}에서 가져오는 중");
		m.put("Importing issues into currrent project. Please note that issue numbers will only be retained if the whole project fork graph does not have any issues to avoid duplicate issue numbers", 
			"현재 프로젝트로 문제를 가져오는 중입니다. 프로젝트 포크 그래프 전체에 문제가 없는 경우에만 문제 번호가 유지되며, 중복 문제 번호를 방지합니다");
		m.put("Importing projects from {0}", "{0}에서 프로젝트 가져오는 중");
		m.put("Imports", "가져오기");
		m.put("In Projects", "프로젝트 내");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"IMAP 호스트 인증서가 자체 서명되었거나 CA 루트가 허용되지 않는 경우, OneDev에 인증서 검사를 우회하도록 지시할 수 있습니다. <b class='text-danger'>경고: </b> 신뢰할 수 없는 네트워크에서는 중간자 공격으로 이어질 수 있으므로 대신 <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>OneDev에 인증서를 가져오십시오</a>");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"SMTP 호스트 인증서가 자체 서명되었거나 CA 루트가 허용되지 않는 경우, OneDev에 인증서 검사를 우회하도록 지시할 수 있습니다. <b class='text-danger'>경고: </b> 신뢰할 수 없는 네트워크에서는 중간자 공격으로 이어질 수 있으므로 대신 <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>OneDev에 인증서를 가져오십시오</a>");
		m.put("In case anonymous access is disabled or anonymous user does not have enough permission for a resource operation, you will need to authenticate by providing user name and password (or access token) via http basic auth header", 
			"익명 액세스가 비활성화되었거나 익명 사용자가 리소스 작업에 대한 충분한 권한이 없는 경우, 사용자 이름과 비밀번호(또는 액세스 토큰)를 제공하여 http 기본 인증 헤더를 통해 인증해야 합니다");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"위 키를 통해 캐시가 적중되지 않는 경우, OneDev는 프로젝트 계층에서 정의된 로드 키를 순서대로 반복하여 일치하는 캐시를 찾습니다. 캐시는 키가 로드 키로 시작하는 경우 일치하는 것으로 간주됩니다. 여러 캐시가 일치하면 가장 최근의 캐시가 반환됩니다");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"캐시를 업로드해야 하는 경우, 이 속성은 업로드 대상 프로젝트를 지정합니다. 현재 프로젝트는 비워 두십시오");
		m.put("In case the pull request status is out of sync with underlying repository, you may synchronize them manually here", 
			"풀 리퀘스트 상태가 기본 저장소와 동기화되지 않은 경우, 여기에서 수동으로 동기화할 수 있습니다");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"사용자 그룹 멤버십이 그룹 측에서 유지되는 경우, 이 속성은 그룹 검색을 위한 기본 노드를 지정합니다. 예: <i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"사용자 그룹 관계가 그룹 측에서 유지되는 경우, 이 필터는 현재 사용자의 소속 그룹을 결정하는 데 사용됩니다. 예: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. 이 예에서 <i>{0}</i>는 현재 사용자의 DN을 나타냅니다");
		m.put("In case you are using external issue tracker, you can define transformers to transform external issue references into external issue links in various places, such as commit messages and pull request descriptions", 
			"외부 문제 추적기를 사용하는 경우, 커밋 메시지 및 풀 리퀘스트 설명과 같은 다양한 위치에서 외부 문제 참조를 외부 문제 링크로 변환하기 위한 변환기를 정의할 수 있습니다");
		m.put("In rare cases, your issues might be out of sync with workflow settings (undefined state/field etc.). Run integrity check below to find problems and get them fixed.", 
			"드문 경우, 문제 상태/필드 등이 워크플로 설정과 동기화되지 않을 수 있습니다. 아래 무결성 검사를 실행하여 문제를 찾아 수정하십시오");
		m.put("Inbox Poll Setting", "받은 편지함 폴링 설정");
		m.put("Include Child Projects", "하위 프로젝트 포함");
		m.put("Include Disabled", "비활성 포함");
		m.put("Include Forks", "포크 포함");
		m.put("Include When Issue is Opened", "문제가 열릴 때 포함");
		m.put("Incompatibilities", "비호환성");
		m.put("Inconsistent issuer in provider metadata and ID token", "제공자 메타데이터와 ID 토큰의 발급자가 일치하지 않음");
		m.put("Indicator", "지표");
		m.put("Inherit from parent", "상위에서 상속");
		m.put("Inherited", "상속됨");
		m.put("Input Spec", "입력 사양");
		m.put("Input URL", "입력 URL");
		m.put("Input allowed CORS origin, hit ENTER to add", "허용된 CORS 원본 입력, ENTER를 눌러 추가");
		m.put("Input revision", "입력 수정");
		m.put("Input title", "입력 제목");
		m.put("Input title here", "여기에 제목 입력");
		m.put("Input user search base. Hit ENTER to add", "사용자 검색 기준 입력. ENTER를 눌러 추가");
		m.put("Input user search bases. Hit ENTER to add", "사용자 검색 기준들 입력. ENTER를 눌러 추가");
		m.put("Insert", "삽입");
		m.put("Insert Image", "이미지 삽입");
		m.put("Insert Link", "링크 삽입");
		m.put("Insert link to this file", "이 파일에 대한 링크 삽입");
		m.put("Insert this image", "이 이미지를 삽입");
		m.put("Install Subscription Key", "구독 키 설치");
		m.put("Integer", "정수");
		m.put("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"실행 중인 작업에 대한 대화형 웹 셸 액세스는 엔터프라이즈 기능입니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>");
		m.put("Internal Database", "내부 데이터베이스");
		m.put("Interpreter", "인터프리터");
		m.put("Invalid GPG signature", "유효하지 않은 GPG 서명");
		m.put("Invalid PCRE syntax", "잘못된 PCRE 구문");
		m.put("Invalid access token: {0}", "잘못된 액세스 토큰: {0}");
		m.put("Invalid credentials", "잘못된 자격 증명");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "잘못된 날짜 범위, \"yyyy-MM-dd to yyyy-MM-dd\" 형식이어야 합니다");
		m.put("Invalid email address: {0}", "잘못된 이메일 주소: {0}");
		m.put("Invalid invitation code", "초대 코드가 유효하지 않습니다");
		m.put("Invalid issue date of ID token", "ID 토큰의 발행 날짜가 잘못되었습니다");
		m.put("Invalid issue number: {0}", "잘못된 발행 번호: {0}");
		m.put("Invalid pull request number: {0}", "잘못된 풀 리퀘스트 번호: {0}");
		m.put("Invalid request path", "잘못된 요청 경로");
		m.put("Invalid selection, click for details", "잘못된 선택, 세부 정보 확인 클릭");
		m.put("Invalid ssh signature", "유효하지 않은 ssh 서명");
		m.put("Invalid state response", "잘못된 상태 응답");
		m.put("Invalid state. Please make sure you are visiting OneDev using server url specified in system setting", 
			"유효하지 않은 상태입니다. 시스템 설정에 지정된 서버 URL을 사용하여 OneDev를 방문하고 있는지 확인하세요");
		m.put("Invalid subscription key", "잘못된 구독 키");
		m.put("Invalid working period", "잘못된 작업 기간");
		m.put("Invitation sent to \"{0}\"", "\"{0}\"로 초대장이 발송되었습니다");
		m.put("Invitation to \"{0}\" deleted", "\"{0}\"에 대한 초대장이 삭제되었습니다");
		m.put("Invitations", "초대장");
		m.put("Invitations sent", "초대장이 발송되었습니다");
		m.put("Invite", "초대");
		m.put("Invite Users", "사용자 초대");
		m.put("Is Site Admin", "사이트 관리자 여부");
		m.put("Issue", "이슈");
		m.put("Issue #{0} deleted", "이슈 #{0}이 삭제되었습니다");
		m.put("Issue Board", "이슈 보드");
		m.put("Issue Boards", "이슈 보드들");
		m.put("Issue Close States", "이슈 종료 상태");
		m.put("Issue Creation Setting", "이슈 생성 설정");
		m.put("Issue Creation Settings", "이슈 생성 설정들");
		m.put("Issue Custom Fields", "이슈 사용자 정의 필드");
		m.put("Issue Description", "이슈 설명");
		m.put("Issue Description Templates", "이슈 설명 템플릿");
		m.put("Issue Details", "이슈 세부 정보");
		m.put("Issue Field", "이슈 필드");
		m.put("Issue Field Mapping", "이슈 필드 매핑");
		m.put("Issue Field Mappings", "이슈 필드 매핑들");
		m.put("Issue Field Set", "이슈 필드 세트");
		m.put("Issue Fields", "이슈 필드들");
		m.put("Issue Filter", "이슈 필터");
		m.put("Issue Import Option", "이슈 가져오기 옵션");
		m.put("Issue Label Mapping", "이슈 라벨 매핑");
		m.put("Issue Label Mappings", "이슈 라벨 매핑들");
		m.put("Issue Link", "이슈 링크");
		m.put("Issue Link Mapping", "이슈 링크 매핑");
		m.put("Issue Link Mappings", "이슈 링크 매핑들");
		m.put("Issue Links", "이슈 링크들");
		m.put("Issue Management", "이슈 관리");
		m.put("Issue Notification", "이슈 알림");
		m.put("Issue Notification Template", "이슈 알림 템플릿");
		m.put("Issue Notification Unsubscribed", "이슈 알림 구독 취소됨");
		m.put("Issue Notification Unsubscribed Template", "이슈 알림 구독 취소 템플릿");
		m.put("Issue Pattern", "이슈 패턴");
		m.put("Issue Priority Mapping", "이슈 우선순위 매핑");
		m.put("Issue Priority Mappings", "이슈 우선순위 매핑들");
		m.put("Issue Query", "이슈 쿼리");
		m.put("Issue Settings", "이슈 설정");
		m.put("Issue State", "이슈 상태");
		m.put("Issue State Mapping", "이슈 상태 매핑");
		m.put("Issue State Mappings", "이슈 상태 매핑들");
		m.put("Issue State Transition", "이슈 상태 전환");
		m.put("Issue State Transitions", "이슈 상태 전환들");
		m.put("Issue States", "이슈 상태들");
		m.put("Issue Statistics", "이슈 통계");
		m.put("Issue Stats", "이슈 통계 데이터");
		m.put("Issue Status Mapping", "이슈 상태 매핑");
		m.put("Issue Status Mappings", "이슈 상태 매핑들");
		m.put("Issue Stopwatch Overdue", "이슈 스톱워치 초과");
		m.put("Issue Stopwatch Overdue Notification Template", "이슈 스톱워치 초과 알림 템플릿");
		m.put("Issue Tag Mapping", "이슈 태그 매핑");
		m.put("Issue Tag Mappings", "이슈 태그 매핑들");
		m.put("Issue Template", "이슈 템플릿");
		m.put("Issue Transition ({0} -> {1})", "이슈 전환 ({0} -> {1})");
		m.put("Issue Type Mapping", "이슈 유형 매핑");
		m.put("Issue Type Mappings", "이슈 유형 매핑들");
		m.put("Issue Votes", "이슈 투표");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"프로젝트 내에서 여러 이슈에 대한 배치 작업을 포함한 이슈 관리 권한");
		m.put("Issue count", "이슈 개수");
		m.put("Issue in state", "상태에 있는 이슈");
		m.put("Issue list", "이슈 목록");
		m.put("Issue management not enabled in this project", "이 프로젝트에서 이슈 관리가 활성화되지 않았습니다");
		m.put("Issue management permission required to move issues", "이슈를 이동하려면 이슈 관리 권한이 필요합니다");
		m.put("Issue not exist or access denied", "이슈가 존재하지 않거나 액세스가 거부되었습니다");
		m.put("Issue number", "이슈 번호");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"이슈 쿼리 감시는 새 이슈에만 영향을 미칩니다. 기존 이슈의 감시 상태를 일괄적으로 관리하려면 이슈 페이지에서 감시 상태로 필터링한 후 적절한 작업을 수행하십시오");
		m.put("Issue state duration statistics", "이슈 상태 지속 시간 통계");
		m.put("Issue state frequency statistics", "이슈 상태 빈도 통계");
		m.put("Issue state trend statistics", "이슈 상태 추세 통계");
		m.put("Issue statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"이슈 통계는 엔터프라이즈 기능입니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>");
		m.put("Issue workflow changed, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliation</a> needs to be performed to make data consistent. You may do this after making all necessary changes", 
			"이슈 워크플로가 변경되었습니다. <a wicket:id=\"reconcile\" class=\"link-primary\">조정</a>을 수행하여 데이터를 일관되게 만들어야 합니다. 필요한 모든 변경을 완료한 후 수행할 수 있습니다");
		m.put("Issues", "이슈들");
		m.put("Issues can be created in this project by sending email to this address", "이 프로젝트에서 이 주소로 이메일을 보내 이슈를 생성할 수 있습니다");
		m.put("Issues copied", "이슈가 복사되었습니다");
		m.put("Issues moved", "이슈가 이동되었습니다");
		m.put("Italic", "이탤릭체");
		m.put("Iteration", "반복");
		m.put("Iteration \"{0}\" closed", "반복 \"{0}\"이 종료되었습니다");
		m.put("Iteration \"{0}\" deleted", "반복 \"{0}\"이 삭제되었습니다");
		m.put("Iteration \"{0}\" is closed", "반복 \"{0}\"이 종료되었습니다");
		m.put("Iteration \"{0}\" is reopened", "반복 \"{0}\"이(가) 다시 열렸습니다");
		m.put("Iteration \"{0}\" reopened", "반복 \"{0}\" 다시 열림");
		m.put("Iteration Edit Bean", "반복 편집 빈");
		m.put("Iteration Name", "반복 이름");
		m.put("Iteration Names", "반복 이름들");
		m.put("Iteration Prefix", "반복 접두사");
		m.put("Iteration list", "반복 목록");
		m.put("Iteration saved", "반복 저장됨");
		m.put("Iteration spans too long to show burndown chart", "반복 기간이 너무 길어 소진 차트를 표시할 수 없습니다");
		m.put("Iteration start and due date should be specified to show burndown chart", "반복 시작 및 종료 날짜를 지정해야 소진 차트를 표시할 수 있습니다");
		m.put("Iteration start date should be before due date", "반복 시작 날짜는 종료 날짜 이전이어야 합니다");
		m.put("Iterations", "반복들");
		m.put("Iterations Bean", "반복 빈");
		m.put("JIRA Issue Priority", "JIRA 이슈 우선순위");
		m.put("JIRA Issue Status", "JIRA 이슈 상태");
		m.put("JIRA Issue Type", "JIRA 이슈 유형");
		m.put("JIRA Project", "JIRA 프로젝트");
		m.put("JIRA Projects to Import", "가져올 JIRA 프로젝트들");
		m.put("JUnit Report", "JUnit 보고서");
		m.put("JVM", "JVM");
		m.put("JaCoCo Coverage Report", "JaCoCo 커버리지 보고서");
		m.put("Jest Coverage Report", "Jest 커버리지 보고서");
		m.put("Jest Test Report", "Jest 테스트 보고서");
		m.put("Job", "작업");
		m.put("Job \"{0}\" associated with the build not found.", "빌드와 연결된 작업 \"{0}\"을(를) 찾을 수 없습니다.");
		m.put("Job Authorization", "작업 권한");
		m.put("Job Cache Management", "작업 캐시 관리");
		m.put("Job Dependencies", "작업 종속성들");
		m.put("Job Dependency", "작업 종속성");
		m.put("Job Executor", "작업 실행기");
		m.put("Job Executor Bean", "작업 실행기 빈");
		m.put("Job Executors", "작업 실행기들");
		m.put("Job Name", "작업 이름");
		m.put("Job Names", "작업 이름들");
		m.put("Job Param", "작업 매개변수");
		m.put("Job Parameters", "작업 매개변수들");
		m.put("Job Privilege", "작업 권한");
		m.put("Job Privileges", "작업 권한들");
		m.put("Job Properties", "작업 속성들");
		m.put("Job Properties Bean", "작업 속성 빈");
		m.put("Job Property", "작업 속성");
		m.put("Job Secret", "작업 비밀");
		m.put("Job Secret Edit Bean", "작업 비밀 편집 빈");
		m.put("Job Secrets", "작업 비밀들");
		m.put("Job Trigger", "작업 트리거");
		m.put("Job Trigger Bean", "작업 트리거 빈");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"작업 관리 권한, 작업의 빌드 삭제를 포함합니다. 이는 다른 모든 작업 권한을 암시합니다");
		m.put("Job cache \"{0}\" deleted", "작업 캐시 \"{0}\" 삭제됨");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"작업 종속성은 다른 작업을 실행할 때 순서와 동시성을 결정합니다. 또한 상위 작업에서 가져올 아티팩트를 지정할 수 있습니다");
		m.put("Job executor tested successfully", "작업 실행기 테스트 성공");
		m.put("Job executors", "작업 실행기들");
		m.put("Job name", "작업 이름");
		m.put("Job properties saved", "작업 속성 저장됨");
		m.put("Job secret \"{0}\" deleted", "작업 비밀 \"{0}\" 삭제됨");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission", 
			"작업 비밀 'access-token'은 프로젝트 빌드 설정에서 패키지 ${permission} 권한을 가진 액세스 토큰으로 정의되어야 합니다");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package read permission", 
			"작업 비밀 'access-token'은 프로젝트 빌드 설정에서 패키지 읽기 권한을 가진 액세스 토큰으로 정의되어야 합니다");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package write permission", 
			"작업 비밀 'access-token'은 프로젝트 빌드 설정에서 패키지 쓰기 권한을 가진 액세스 토큰으로 정의되어야 합니다");
		m.put("Job token", "작업 토큰");
		m.put("Job will run on head commit of default branch", "작업은 기본 브랜치의 헤드 커밋에서 실행됩니다");
		m.put("Job will run on head commit of target branch", "작업은 대상 브랜치의 헤드 커밋에서 실행됩니다");
		m.put("Job will run on merge commit of target branch and source branch", "작업은 대상 브랜치와 소스 브랜치의 병합 커밋에서 실행됩니다");
		m.put("Job will run on merge commit of target branch and source branch.<br><b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"작업은 대상 브랜치와 소스 브랜치의 병합 커밋에서 실행됩니다.<br><b class='text-info'>참고:</b> 브랜치 보호 규칙에 의해 요구되지 않는 한, 이 트리거는 <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, 또는 <code>[no job]</code> 메시지가 포함된 커밋을 무시합니다");
		m.put("Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"작업은 코드가 커밋될 때 실행됩니다. <b class='text-info'>참고:</b> 이 트리거는 <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, 또는 <code>[no job]</code> 메시지가 포함된 커밋을 무시합니다");
		m.put("Job workspace", "작업 워크스페이스");
		m.put("Jobs", "작업들");
		m.put("Jobs marked with <span class=\"text-danger\">*</span> are required to be successful", 
			"<span class=\"text-danger\">*</span>로 표시된 작업은 성공해야 합니다");
		m.put("Jobs required to be successful on merge commit: ", "병합 커밋에서 성공해야 하는 작업들:");
		m.put("Jobs required to be successful: ", "성공해야 하는 작업들:");
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"동일한 순차 그룹과 실행기를 가진 작업은 순차적으로 실행됩니다. 예를 들어, 동일한 실행기로 실행되고 현재 프로젝트의 프로덕션 환경에 배포하는 작업에 대해 <tt>@project_path@:prod</tt>로 이 속성을 지정하여 충돌하는 배포를 방지할 수 있습니다");
		m.put("Key", "키");
		m.put("Key Fingerprint", "키 지문");
		m.put("Key ID", "키 ID");
		m.put("Key Secret", "키 비밀");
		m.put("Key Type", "키 유형");
		m.put("Kubectl Config File", "Kubectl 구성 파일");
		m.put("Kubernetes", "쿠버네티스");
		m.put("Kubernetes Executor", "쿠버네티스 실행기");
		m.put("LDAP URL", "LDAP URL");
		m.put("Label", "레이블");
		m.put("Label Management", "레이블 관리");
		m.put("Label Management Bean", "레이블 관리 빈");
		m.put("Label Name", "레이블 이름");
		m.put("Label Spec", "레이블 사양");
		m.put("Label Value", "레이블 값");
		m.put("Labels", "레이블들");
		m.put("Labels Bean", "레이블 빈");
		m.put("Labels can be defined in Administration / Label Management", "레이블은 관리 / 레이블 관리에서 정의할 수 있습니다");
		m.put("Labels have been updated", "레이블이 업데이트되었습니다");
		m.put("Language", "언어");
		m.put("Last Accessed", "마지막 접근");
		m.put("Last Finished of Specified Job", "지정된 작업의 마지막 완료");
		m.put("Last Modified", "마지막 수정");
		m.put("Last Published", "마지막 게시");
		m.put("Last Update", "마지막 업데이트");
		m.put("Last commit", "마지막 커밋");
		m.put("Last commit hash", "마지막 커밋 해시");
		m.put("Last commit index version", "마지막 커밋 인덱스 버전");
		m.put("Leaf Projects", "리프 프로젝트들");
		m.put("Least branch coverage", "최소 브랜치 커버리지");
		m.put("Least line coverage", "최소 라인 커버리지");
		m.put("Leave a comment", "댓글 남기기");
		m.put("Leave a note", "메모 남기기");
		m.put("Left", "왼쪽");
		m.put("Less", "적음");
		m.put("License Agreement", "라이선스 계약");
		m.put("License Setting", "라이선스 설정");
		m.put("Licensed To", "라이선스 사용자");
		m.put("Licensed To:", "라이선스 사용자:");
		m.put("Line", "라인");
		m.put("Line changes", "라인 변경");
		m.put("Line: ", "라인:");
		m.put("Lines", "라인들");
		m.put("Link", "링크");
		m.put("Link Existing User", "기존 사용자 연결");
		m.put("Link Spec", "링크 사양");
		m.put("Link Spec Opposite", "반대 링크 사양");
		m.put("Link Text", "링크 텍스트");
		m.put("Link URL", "링크 URL");
		m.put("Link URL should be specified", "링크 URL을 지정해야 합니다");
		m.put("Link User Bean", "사용자 빈 연결");
		m.put("Linkable Issues", "링크 가능한 이슈");
		m.put("Linkable Issues On the Other Side", "반대편의 링크 가능한 이슈");
		m.put("Links", "링크들");
		m.put("Links can be used to associate different issues. For instance, an issue can be linked to sub issues or related issues", 
			"링크는 서로 다른 이슈를 연결하는 데 사용할 수 있습니다. 예를 들어, 하나의 이슈를 하위 이슈 또는 관련 이슈에 연결할 수 있습니다");
		m.put("List", "목록");
		m.put("Literal", "리터럴");
		m.put("Literal default value", "리터럴 기본값");
		m.put("Literal value", "리터럴 값");
		m.put("Load Keys", "키 로드");
		m.put("Loading emojis...", "이모지 로드 중...");
		m.put("Loading...", "로드 중...");
		m.put("Log", "로그");
		m.put("Log Work", "작업 로그");
		m.put("Log not available for offline agent", "오프라인 에이전트에서는 로그를 사용할 수 없습니다");
		m.put("Log work", "작업 로그");
		m.put("Login Name", "로그인 이름");
		m.put("Login and generate refresh token", "로그인하고 새로 고침 토큰 생성");
		m.put("Login name already used by another account", "다른 계정에서 이미 사용 중인 로그인 이름");
		m.put("Login name or email", "로그인 이름 또는 이메일");
		m.put("Login name or email address", "로그인 이름 또는 이메일 주소");
		m.put("Login to OneDev docker registry", "OneDev 도커 레지스트리에 로그인");
		m.put("Login to comment", "댓글 작성하려면 로그인하세요");
		m.put("Login to comment on selection", "선택에 댓글 작성하려면 로그인하세요");
		m.put("Login to vote", "투표하려면 로그인하세요");
		m.put("Login user needs to have package write permission over the project below", "로그인 사용자는 아래 프로젝트에 대한 패키지 쓰기 권한이 필요합니다");
		m.put("Login with {0}", "{0}로 로그인");
		m.put("Logo for Dark Mode", "다크 모드용 로고");
		m.put("Logo for Light Mode", "라이트 모드용 로고");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"위 계정의 장기 새로 고침 토큰은 Gmail에 액세스하기 위한 액세스 토큰을 생성하는 데 사용됩니다. <b class='text-info'>팁: </b> 이 필드 오른쪽에 있는 버튼을 사용하여 새로 고침 토큰을 생성할 수 있습니다. 클라이언트 ID, 클라이언트 비밀 또는 계정 이름이 변경될 때마다 새로 고침 토큰을 다시 생성해야 합니다");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"위 계정의 장기 새로 고침 토큰은 Office 365 메일 서버에 액세스하기 위한 액세스 토큰을 생성하는 데 사용됩니다. <b class='text-info'>팁: </b> 이 필드 오른쪽에 있는 버튼을 사용하여 Office 365 계정에 로그인하고 새로 고침 토큰을 생성할 수 있습니다. 테넌트 ID, 클라이언트 ID, 클라이언트 비밀 또는 사용자 주체 이름이 변경될 때마다 새로 고침 토큰을 다시 생성해야 합니다");
		m.put("Longest Duration First", "가장 긴 지속 시간 우선");
		m.put("Looks like a GPG signature but without necessary data", "필요한 데이터가 없는 GPG 서명처럼 보입니다");
		m.put("Low", "낮음");
		m.put("Low Severity", "낮은 심각도");
		m.put("MERGED", "병합됨");
		m.put("MS Teams Notifications", "MS Teams 알림");
		m.put("Mail", "메일");
		m.put("Mail Connector", "메일 커넥터");
		m.put("Mail Connector Bean", "메일 커넥터 빈");
		m.put("Mail Service", "메일 서비스");
		m.put("Mail Service Test", "메일 서비스 테스트");
		m.put("Mail service not configured", "메일 서비스가 구성되지 않음");
		m.put("Mail service settings saved", "메일 서비스 설정이 저장됨");
		m.put("Make sure <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 or higher</a> is installed", 
			"<a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 이상</a>이 설치되어 있는지 확인하세요");
		m.put("Make sure current user has permission to run docker containers", "현재 사용자가 도커 컨테이너를 실행할 권한이 있는지 확인하세요");
		m.put("Make sure docker engine is installed and docker command line is available in system path", 
			"도커 엔진이 설치되어 있고 도커 명령줄이 시스템 경로에서 사용 가능한지 확인하세요");
		m.put("Make sure git version 2.11.1 or higher is installed and available in system path", "Git 버전 2.11.1 이상이 설치되어 있고 시스템 경로에서 사용 가능한지 확인하세요");
		m.put("Make sure git-lfs is installed and available in system path if you want to retrieve LFS files", 
			"Git-lfs가 설치되어 있고 시스템 경로에서 사용 가능한지 확인하세요. LFS 파일을 검색하려면 필요합니다");
		m.put("Make sure the access token has package read permission over the project", "액세스 토큰이 프로젝트에 대한 패키지 읽기 권한이 있는지 확인하세요");
		m.put("Make sure the access token has package write permission over the project", "액세스 토큰이 프로젝트에 대한 패키지 쓰기 권한이 있는지 확인하세요");
		m.put("Make sure the access token has package write permission over the project. Also make sure to run command <code>chmod 0600 $HOME/.gem/credentials</code> after creating the file", 
			"액세스 토큰이 프로젝트에 대한 패키지 쓰기 권한이 있는지 확인하세요. 또한 파일을 생성한 후 <code>chmod 0600 $HOME/.gem/credentials</code> 명령을 실행해야 합니다");
		m.put("Make sure the account has package ${permission} permission over the project", "계정이 프로젝트에 대한 패키지 ${permission} 권한이 있는지 확인하세요");
		m.put("Make sure the account has package read permission over the project", "계정이 프로젝트에 대한 패키지 읽기 권한이 있는지 확인하세요");
		m.put("Make sure the user has package write permission over the project", "사용자가 프로젝트에 대한 패키지 쓰기 권한이 있는지 확인하세요");
		m.put("Malformed %sbase query", "잘못된 %sbase 쿼리");
		m.put("Malformed %squery", "잘못된 %s쿼리");
		m.put("Malformed build spec (import project: {0}, import revision: {1})", "잘못된 빌드 사양 (프로젝트 가져오기: {0}, 리비전 가져오기: {1})");
		m.put("Malformed email address", "잘못된 이메일 주소");
		m.put("Malformed filter", "잘못된 필터");
		m.put("Malformed name filter", "잘못된 이름 필터");
		m.put("Malformed query", "잘못된 쿼리");
		m.put("Malformed ssh signature", "잘못된 ssh 서명");
		m.put("Malformed test suite filter", "잘못된 테스트 스위트 필터");
		m.put("Manage Job", "작업 관리");
		m.put("Manager DN", "관리자 DN");
		m.put("Manager Password", "관리자 비밀번호");
		m.put("Manifest blob unknown", "알 수 없는 매니페스트 블롭");
		m.put("Manifest invalid", "잘못된 매니페스트");
		m.put("Manifest unknown", "알 수 없는 매니페스트");
		m.put("Many commands print outputs with ANSI colors in TTY mode to help identifying problems easily. However some commands running in this mode may wait for user input to cause build hanging. This can normally be fixed by adding extra options to the command", 
			"많은 명령이 TTY 모드에서 ANSI 색상을 사용하여 출력을 출력하여 문제를 쉽게 식별할 수 있도록 합니다. 그러나 이 모드에서 실행되는 일부 명령은 사용자 입력을 기다려 빌드가 중단될 수 있습니다. 이는 일반적으로 명령에 추가 옵션을 추가하여 해결할 수 있습니다");
		m.put("Mark a property archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived properties will not be shown by default", 
			"현재 빌드 사양에서 더 이상 사용되지 않지만 이전 빌드를 재현하기 위해 여전히 필요하다면 속성을 보관 처리합니다. 보관된 속성은 기본적으로 표시되지 않습니다");
		m.put("Mark a secret archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived secrets will not be shown by default", 
			"현재 빌드 사양에서 더 이상 사용되지 않지만 이전 빌드를 재현하기 위해 여전히 필요하다면 비밀을 보관 처리합니다. 보관된 비밀은 기본적으로 표시되지 않습니다");
		m.put("Markdown", "마크다운");
		m.put("Markdown Report", "마크다운 보고서");
		m.put("Markdown from file", "파일에서 가져온 마크다운");
		m.put("Maven(s)", "Maven(s)");
		m.put("Max Code Search Entries", "최대 코드 검색 항목");
		m.put("Max Commit Message Line Length", "최대 커밋 메시지 라인 길이");
		m.put("Max Git LFS File Size (MB)", "최대 Git LFS 파일 크기 (MB)");
		m.put("Max Retries", "최대 재시도 횟수");
		m.put("Max Upload File Size (MB)", "최대 업로드 파일 크기 (MB)");
		m.put("Max Value", "최대 값");
		m.put("Maximum number of entries to return when search code in repository", "저장소에서 코드 검색 시 반환할 최대 항목 수");
		m.put("Maximum of retries before giving up", "포기하기 전에 최대 재시도 횟수");
		m.put("May not be empty", "비어 있을 수 없습니다");
		m.put("Medium", "중간");
		m.put("Medium Severity", "중간 심각도");
		m.put("Members", "멤버들");
		m.put("Memory", "메모리");
		m.put("Memory Limit", "메모리 제한");
		m.put("Memory Request", "메모리 요청");
		m.put("Mention Someone", "누군가를 언급하기");
		m.put("Mention someone", "누군가를 언급하기");
		m.put("Merge", "병합");
		m.put("Merge Strategy", "병합 전략");
		m.put("Merge Target Branch into Source Branch", "대상 브랜치를 소스 브랜치로 병합");
		m.put("Merge branch \"{0}\" into branch \"{1}\"", "브랜치 \"{0}\"를 브랜치 \"{1}\"로 병합");
		m.put("Merge branch \"{0}\" of project \"{1}\" into branch \"{2}\"", "프로젝트 \"{1}\"의 브랜치 \"{0}\"를 브랜치 \"{2}\"로 병합");
		m.put("Merge preview not calculated yet", "병합 미리보기가 아직 계산되지 않음");
		m.put("Merged", "병합됨");
		m.put("Merged pull request \"{0}\" ({1})", "병합된 풀 리퀘스트 \"{0}\" ({1})");
		m.put("Merges pull request", "풀 리퀘스트 병합");
		m.put("Meta", "메타");
		m.put("Meta Info", "메타 정보");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "최소 값");
		m.put("Minimum length of the password", "비밀번호의 최소 길이");
		m.put("Missing Commit", "누락된 커밋");
		m.put("Missing Commits", "누락된 커밋들");
		m.put("Month", "월");
		m.put("Months", "개월");
		m.put("Months to Display", "표시할 개월 수");
		m.put("More", "더 보기");
		m.put("More Options", "더 많은 옵션");
		m.put("More Settings", "더 많은 설정");
		m.put("More commits", "더 많은 커밋");
		m.put("More info", "더 많은 정보");
		m.put("More operations", "더 많은 작업");
		m.put("Most branch coverage", "최대 브랜치 커버리지");
		m.put("Most line coverage", "최대 라인 커버리지");
		m.put("Most probably there are import errors in the <a wicket:id=\"buildSpec\">build spec</a>", 
			"<a wicket:id=\"buildSpec\">빌드 스펙</a>에 가져오기 오류가 있을 가능성이 높습니다");
		m.put("Mount Docker Sock", "Docker Sock 마운트");
		m.put("Move All Queried Issues To...", "모든 조회된 이슈를 이동...");
		m.put("Move All Queried Projects To...", "모든 조회된 프로젝트를 이동...");
		m.put("Move Selected Issues To...", "선택된 이슈를 이동...");
		m.put("Move Selected Projects To...", "선택된 프로젝트를 이동...");
		m.put("Multiple Lines", "다중 라인");
		m.put("Multiple On the Other Side", "다른 쪽의 다중");
		m.put("Must not be empty", "비어 있으면 안 됨");
		m.put("My Access Tokens", "내 액세스 토큰");
		m.put("My Basic Settings", "내 기본 설정");
		m.put("My Email Addresses", "내 이메일 주소");
		m.put("My GPG Keys", "내 GPG 키");
		m.put("My Profile", "내 프로필");
		m.put("My SSH Keys", "내 SSH 키");
		m.put("My SSO Accounts", "내 SSO 계정");
		m.put("Mypy Report", "Mypy 보고서");
		m.put("N/A", "N/A");
		m.put("NPM(s)", "NPM(s)");
		m.put("Name", "이름");
		m.put("Name Of Empty Value", "빈 값의 이름");
		m.put("Name On the Other Side", "다른 쪽의 이름");
		m.put("Name Prefix", "이름 접두사");
		m.put("Name already used by another access token of the owner", "소유자의 다른 액세스 토큰에서 이미 사용된 이름");
		m.put("Name already used by another link", "다른 링크에서 이미 사용된 이름");
		m.put("Name and name on the other side should be different", "이름과 다른 쪽의 이름은 달라야 합니다");
		m.put("Name containing spaces or starting with dash needs to be quoted", "공백이 포함되거나 대시로 시작하는 이름은 인용이 필요합니다");
		m.put("Name invalid", "잘못된 이름");
		m.put("Name of the link", "링크의 이름");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"다른 쪽의 링크 이름. 예를 들어 이름이 <tt>하위 이슈</tt>라면, 다른 쪽의 이름은 <tt>상위 이슈</tt>일 수 있습니다");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"제공자의 이름은 두 가지 목적을 가집니다: <ul><li>로그인 버튼에 표시<li>인증 콜백 URL을 형성하며, 이는 <i>&lt;서버 URL&gt;/~sso/callback/&lt;name&gt;</i>입니다</ul>");
		m.put("Name reversely", "이름 반대로");
		m.put("Name unknown", "알 수 없는 이름");
		m.put("Name your file", "파일 이름 지정");
		m.put("Named Agent Queries Bean", "Named Agent Queries Bean");
		m.put("Named Agent Query", "Named Agent Query");
		m.put("Named Build Queries Bean", "Named Build Queries Bean");
		m.put("Named Build Query", "Named Build Query");
		m.put("Named Code Comment Queries Bean", "Named Code Comment Queries Bean");
		m.put("Named Code Comment Query", "Named Code Comment Query");
		m.put("Named Commit Queries Bean", "Named Commit Queries Bean");
		m.put("Named Commit Query", "Named Commit Query");
		m.put("Named Element", "Named Element");
		m.put("Named Issue Queries Bean", "Named Issue Queries Bean");
		m.put("Named Issue Query", "Named Issue Query");
		m.put("Named Pack Queries Bean", "Named Pack Queries Bean");
		m.put("Named Pack Query", "Named Pack Query");
		m.put("Named Project Queries Bean", "Named Project Queries Bean");
		m.put("Named Project Query", "Named Project Query");
		m.put("Named Pull Request Queries Bean", "Named Pull Request Queries Bean");
		m.put("Named Pull Request Query", "Named Pull Request Query");
		m.put("Named Query", "Named Query");
		m.put("Network Options", "네트워크 옵션");
		m.put("Never", "절대 없음");
		m.put("Never expire", "만료되지 않음");
		m.put("New Board", "새 보드");
		m.put("New Invitation Bean", "새 초대 Bean");
		m.put("New Issue", "새 이슈");
		m.put("New Password", "새 비밀번호");
		m.put("New State", "새 상태");
		m.put("New User Bean", "새 사용자 Bean");
		m.put("New Value", "새 값");
		m.put("New issue board created", "새 이슈 보드 생성됨");
		m.put("New project created", "새 프로젝트가 생성되었습니다");
		m.put("New user created", "새 사용자 생성됨");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"새 버전이 사용 가능합니다. 보안/중요 업데이트는 빨간색, 버그 수정은 노란색, 기능 업데이트는 파란색으로 표시됩니다. 변경 사항을 보려면 클릭하세요. 시스템 설정에서 비활성화할 수 있습니다.");
		m.put("Next", "다음");
		m.put("Next commit", "다음 커밋");
		m.put("Next {0}", "다음 {0}");
		m.put("No", "아니요");
		m.put("No Activity Days", "활동 없음 일수");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"계정에 SSH 키가 구성되어 있지 않습니다. <a wicket:id=\"sshKeys\" class=\"link-primary\">키를 추가</a>하거나 <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> URL로 전환할 수 있습니다.");
		m.put("No SSL", "SSL 없음");
		m.put("No accessible reports", "접근 가능한 보고서 없음");
		m.put("No activity for some time", "일정 시간 동안 활동 없음");
		m.put("No agents to pause", "일시 중지할 에이전트 없음");
		m.put("No agents to remove", "제거할 에이전트 없음");
		m.put("No agents to restart", "재시작할 에이전트 없음");
		m.put("No agents to resume", "재개할 에이전트 없음");
		m.put("No aggregation", "집계 없음");
		m.put("No any", "없음");
		m.put("No any matches", "일치하는 항목이 없습니다");
		m.put("No applicable transitions or no permission to transit", "적용 가능한 전환 없음 또는 전환 권한 없음");
		m.put("No attributes defined (can only be edited when agent is online)", "정의된 속성 없음 (에이전트가 온라인 상태일 때만 편집 가능)");
		m.put("No audits", "감사 없음");
		m.put("No authorized job secret found (project: {0}, job secret: {1})", "승인된 작업 비밀 없음 (프로젝트: {0}, 작업 비밀: {1})");
		m.put("No branch to cherry-pick to", "체리픽할 브랜치 없음");
		m.put("No branch to revert on", "되돌릴 브랜치 없음");
		m.put("No branches Found", "브랜치 없음");
		m.put("No branches found", "브랜치 없음");
		m.put("No build in query context", "쿼리 컨텍스트에 빌드 없음");
		m.put("No builds", "빌드 없음");
		m.put("No builds to cancel", "취소할 빌드 없음");
		m.put("No builds to delete", "삭제할 빌드 없음");
		m.put("No builds to re-run", "재실행할 빌드 없음");
		m.put("No comment", "댓글 없음");
		m.put("No comments to delete", "삭제할 댓글 없음");
		m.put("No comments to set as read", "읽음으로 설정할 댓글 없음");
		m.put("No comments to set resolved", "해결됨으로 설정할 댓글 없음");
		m.put("No comments to set unresolved", "미해결로 설정할 댓글 없음");
		m.put("No commit in query context", "쿼리 컨텍스트에 커밋 없음");
		m.put("No config file", "구성 파일 없음");
		m.put("No current build in query context", "쿼리 컨텍스트에 현재 빌드 없음");
		m.put("No current commit in query context", "쿼리 컨텍스트에 현재 커밋 없음");
		m.put("No current pull request in query context", "쿼리 컨텍스트에 현재 풀 리퀘스트 없음");
		m.put("No data", "데이터 없음");
		m.put("No default branch", "기본 브랜치 없음");
		m.put("No default group", "기본 그룹 없음");
		m.put("No default roles", "기본 역할 없음");
		m.put("No default value", "기본값 없음");
		m.put("No description", "설명 없음");
		m.put("No diffs", "차이 없음");
		m.put("No diffs to navigate", "탐색할 차이 없음");
		m.put("No directories to skip", "건너뛸 디렉터리 없음");
		m.put("No disallowed file types", "허용되지 않는 파일 유형이 없습니다.");
		m.put("No executors defined. Jobs will use auto-discovered executors instead", "정의된 실행자가 없습니다. 작업은 자동으로 검색된 실행자를 대신 사용합니다");
		m.put("No external password authenticator", "외부 비밀번호 인증기 없음");
		m.put("No external password authenticator to authenticate user \"{0}\"", "사용자 \"{0}\"를 인증할 외부 비밀번호 인증기가 없습니다");
		m.put("No fields to prompt", "프롬프트할 필드 없음");
		m.put("No fields to remove", "제거할 필드 없음");
		m.put("No file attachments", "파일 첨부 없음");
		m.put("No group by", "그룹화 없음");
		m.put("No groups claim returned", "반환된 그룹 클레임 없음");
		m.put("No groups to remove from", "제거할 그룹이 없습니다");
		m.put("No ignore file", "무시 파일 없음");
		m.put("No ignored licenses", "무시된 라이선스 없음");
		m.put("No image attachments", "이미지 첨부 없음");
		m.put("No imports defined", "정의된 가져오기 없음");
		m.put("No issue boards defined", "정의된 이슈 보드 없음");
		m.put("No issues in iteration", "반복에 이슈 없음");
		m.put("No issues to copy", "복사할 이슈 없음");
		m.put("No issues to delete", "삭제할 이슈 없음");
		m.put("No issues to edit", "편집할 이슈 없음");
		m.put("No issues to export", "내보낼 이슈 없음");
		m.put("No issues to move", "이동할 이슈 없음");
		m.put("No issues to set as read", "읽음으로 설정할 이슈 없음");
		m.put("No issues to sync estimated/spent time", "예상/소요 시간을 동기화할 이슈 없음");
		m.put("No issues to watch/unwatch", "관찰/관찰 취소할 이슈 없음");
		m.put("No jobs defined", "정의된 작업 없음");
		m.put("No jobs found", "작업이 없습니다");
		m.put("No limit", "제한 없음");
		m.put("No mail service", "메일 서비스 없음");
		m.put("No obvious changes", "명확한 변경 사항 없음");
		m.put("No one", "아무도 없음");
		m.put("No packages to delete", "삭제할 패키지 없음");
		m.put("No parent", "상위 없음");
		m.put("No previous successful build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a> to calculate fixed issues since", 
			"<a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">동일 스트림</a>에서 이전 성공적인 빌드 없음");
		m.put("No projects found", "프로젝트 없음");
		m.put("No projects to delete", "삭제할 프로젝트 없음");
		m.put("No projects to modify", "수정할 프로젝트 없음");
		m.put("No projects to move", "이동할 프로젝트 없음");
		m.put("No properties defined", "정의된 속성 없음");
		m.put("No proxy", "프록시 없음");
		m.put("No pull request in query context", "쿼리 컨텍스트에 풀 리퀘스트 없음");
		m.put("No pull requests to delete", "삭제할 풀 리퀘스트 없음");
		m.put("No pull requests to discard", "폐기할 풀 리퀘스트 없음");
		m.put("No pull requests to set as read", "읽음으로 설정할 풀 리퀘스트 없음");
		m.put("No pull requests to watch/unwatch", "관찰/관찰 취소할 풀 리퀘스트 없음");
		m.put("No refs to build on behalf of", "대신 빌드할 참조 없음");
		m.put("No required services", "필수 서비스 없음");
		m.put("No response body", "응답 본문 없음");
		m.put("No secret config", "비밀 구성 없음");
		m.put("No services defined", "정의된 서비스 없음");
		m.put("No start/due date", "시작/마감일 없음");
		m.put("No step templates defined", "정의된 단계 템플릿 없음");
		m.put("No suggestions", "제안 없음");
		m.put("No tags found", "태그 없음");
		m.put("No timesheets defined", "정의된 근무 시간표 없음");
		m.put("No user found with login name or email: ", "로그인 이름 또는 이메일로 사용자를 찾을 수 없습니다:");
		m.put("No users to convert to service accounts", "서비스 계정으로 변환할 사용자가 없습니다");
		m.put("No users to delete", "삭제할 사용자 없음");
		m.put("No users to disable", "비활성화할 사용자 없음");
		m.put("No users to enable", "활성화할 사용자 없음");
		m.put("No users to remove from group", "그룹에서 제거할 사용자가 없습니다");
		m.put("No valid query to show progress", "진행 상황을 표시할 유효한 쿼리가 없습니다");
		m.put("No valid signature for head commit", "헤드 커밋에 대한 유효한 서명이 없습니다");
		m.put("No valid signature for head commit of target branch", "대상 브랜치의 헤드 커밋에 대한 유효한 서명이 없습니다");
		m.put("No value", "값이 없습니다");
		m.put("No verified primary email address", "확인된 기본 이메일 주소가 없습니다");
		m.put("Node Selector", "노드 선택기");
		m.put("Node Selector Entry", "노드 선택기 항목");
		m.put("None", "없음");
		m.put("Not Active Since", "활성 상태가 아닌 이후");
		m.put("Not Used Since", "사용되지 않은 이후");
		m.put("Not a verified email of signing GPG key", "GPG 키 서명의 확인된 이메일이 아닙니다");
		m.put("Not a verified email of signing ssh key owner", "서명 ssh 키 소유자의 확인된 이메일이 아님");
		m.put("Not allowed file type: {0}", "허용되지 않는 파일 유형: {0}");
		m.put("Not assigned", "할당되지 않음");
		m.put("Not authorized to create project under \"{0}\"", "\"{0}\" 아래에 프로젝트를 생성할 권한이 없습니다.");
		m.put("Not authorized to create root project", "루트 프로젝트를 생성할 권한이 없습니다.");
		m.put("Not authorized to move project under this parent", "이 부모 아래로 프로젝트를 이동할 권한이 없습니다");
		m.put("Not authorized to set as root project", "루트 프로젝트로 설정할 권한이 없습니다");
		m.put("Not covered", "포함되지 않음");
		m.put("Not covered by any test", "어떤 테스트에도 포함되지 않음");
		m.put("Not displaying any fields", "어떤 필드도 표시되지 않음");
		m.put("Not displaying any links", "어떤 링크도 표시되지 않음");
		m.put("Not passed", "통과하지 않음");
		m.put("Not rendered in failsafe mode", "안전 모드에서 렌더링되지 않음");
		m.put("Not run", "실행되지 않음");
		m.put("Not specified", "지정되지 않음");
		m.put("Note", "노트");
		m.put("Nothing to preview", "미리 볼 내용이 없습니다");
		m.put("Notification", "알림");
		m.put("Notifications", "알림들");
		m.put("Notify Build Events", "빌드 이벤트 알림");
		m.put("Notify Code Comment Events", "코드 댓글 이벤트 알림");
		m.put("Notify Code Push Events", "코드 푸시 이벤트 알림");
		m.put("Notify Issue Events", "이슈 이벤트 알림");
		m.put("Notify Own Events", "자신의 이벤트 알림");
		m.put("Notify Pull Request Events", "풀 리퀘스트 이벤트 알림");
		m.put("Notify Users", "사용자 알림");
		m.put("Ntfy.sh Notifications", "Ntfy.sh 알림");
		m.put("NuGet(s)", "NuGet(s)");
		m.put("NuSpec", "NuSpec");
		m.put("Number of CPU Cores", "CPU 코어 수");
		m.put("Number of SSH Keys", "SSH 키 수");
		m.put("Number of builds to preserve", "보존할 빌드 수");
		m.put("Number of project replicas, including primary and backups", "기본 및 백업을 포함한 프로젝트 복제본 수");
		m.put("Number of recent months to show statistics for", "통계를 표시할 최근 몇 개월 수");
		m.put("OAuth2 Client information | CLIENT ID", "OAuth2 클라이언트 정보 | CLIENT ID");
		m.put("OAuth2 Client information | CLIENT SECRET", "OAuth2 클라이언트 정보 | CLIENT SECRET");
		m.put("OCI Layout Directory", "OCI 레이아웃 디렉토리");
		m.put("OIDC error: Inconsistent sub in ID token and userinfo", "OIDC 오류: ID 토큰과 사용자 정보의 하위가 일치하지 않음");
		m.put("OOPS! There Is An Error", "이런! 오류가 발생했습니다");
		m.put("OPEN", "열림");
		m.put("OS", "운영 체제");
		m.put("OS Arch", "운영 체제 아키텍처");
		m.put("OS User Name", "운영 체제 사용자 이름");
		m.put("OS Version", "운영 체제 버전");
		m.put("OS/ARCH", "운영 체제/아키텍처");
		m.put("Offline", "오프라인");
		m.put("Ok", "확인");
		m.put("Old Name", "이전 이름");
		m.put("Old Password", "이전 비밀번호");
		m.put("On Behalf Of", "대신하여");
		m.put("On Branches", "브랜치에서");
		m.put("OneDev Issue Field", "OneDev 이슈 필드");
		m.put("OneDev Issue Link", "OneDev 이슈 링크");
		m.put("OneDev Issue State", "OneDev 이슈 상태");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"OneDev는 코드 검색, 라인 통계 및 코드 기여 통계를 위해 저장소 파일을 분석합니다. 이 설정은 분석할 파일을 지정하며, 공백으로 구분된 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 패턴</a>을 기대합니다. 패턴은 '-'로 시작하여 제외할 수 있으며, 예를 들어 <code>-**/vendors/**</code>는 경로에 vendors가 포함된 모든 파일을 제외합니다. <b>참고: </b> 이 설정을 변경하면 새 커밋에만 영향을 미칩니다. 변경 사항을 히스토리 커밋에 적용하려면 서버를 중지하고 <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>프로젝트 저장소 디렉토리</a> 아래의 <code>index</code> 및 <code>info/commit</code> 폴더를 삭제하십시오. 서버가 시작되면 저장소가 다시 분석됩니다");
		m.put("OneDev configures git hooks to communicate with itself via curl", "OneDev는 git 훅을 구성하여 curl을 통해 자체적으로 통신합니다");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"OneDev는 사용자 DN을 검색하고 그룹 검색이 활성화된 경우 사용자 그룹 정보를 검색해야 합니다. 이 옵션을 선택하고 '관리자' DN 및 비밀번호를 지정하여 이러한 작업이 인증이 필요한 경우 설정하십시오");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"OneDev는 저장소를 관리하기 위해 git 명령줄을 필요로 합니다. 최소 요구 버전은 2.11.1입니다. 또한 빌드 작업에서 LFS 파일을 검색하려면 git-lfs가 설치되어 있는지 확인하십시오");
		m.put("Online", "온라인");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", 
			"대상 브랜치를 소스 브랜치로 빠르게 병합할 수 없는 경우에만 병합 커밋 생성");
		m.put("Only projects manageable by access token owner can be authorized", "액세스 토큰 소유자가 관리할 수 있는 프로젝트만 승인 가능");
		m.put("Only system level audit events are displayed here. To view audit events for a specific project, please visit the project audit log page", 
			"여기에는 시스템 수준 감사 이벤트만 표시됩니다. 특정 프로젝트의 감사 이벤트를 보려면 프로젝트 감사 로그 페이지를 방문하세요");
		m.put("Only users able to authenticate via password can be linked", "비밀번호로 인증할 수 있는 사용자만 연결할 수 있습니다");
		m.put("Open", "열기");
		m.put("Open new pull request", "새 풀 리퀘스트 열기");
		m.put("Open terminal of current running step", "현재 실행 중인 단계의 터미널 열기");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"OpenID 클라이언트 식별은 이 OneDev 인스턴스를 클라이언트 애플리케이션으로 등록할 때 OpenID 제공자가 할당합니다");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"OpenID 클라이언트 비밀은 이 OneDev 인스턴스를 클라이언트 애플리케이션으로 등록할 때 OpenID 제공자가 생성합니다");
		m.put("OpenSSH Public Key", "OpenSSH 공개 키");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"OpenSSH 공개 키는 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', 또는 'sk-ssh-ed25519@openssh.com'으로 시작합니다");
		m.put("Opened issue \"{0}\" ({1})", "열린 이슈 \"{0}\" ({1})");
		m.put("Opened pull request \"{0}\" ({1})", "열린 풀 리퀘스트 \"{0}\" ({1})");
		m.put("Operation", "작업");
		m.put("Operation Failed", "작업 실패");
		m.put("Operation Successful", "작업 성공");
		m.put("Operations", "작업");
		m.put("Optional", "선택 사항");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"이슈를 생성할 프로젝트를 선택적으로 지정하십시오. 현재 프로젝트에서 생성하려면 비워 두십시오");
		m.put("Optionally add new users to specified default group", "선택적으로 새 사용자를 지정된 기본 그룹에 추가하십시오");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"선택적으로 인증된 새 사용자를 지정된 그룹에 추가하십시오. 멤버십 정보가 없는 경우");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"선택적으로 인증된 새 사용자를 지정된 그룹에 추가하십시오. 멤버십 정보가 검색되지 않은 경우");
		m.put("Optionally choose required builds. You may also input jobs not listed here, and press ENTER to add them", 
			"선택적으로 필요한 빌드를 선택하십시오. 여기에 나열되지 않은 작업을 입력하고 ENTER를 눌러 추가할 수도 있습니다");
		m.put("Optionally configure proxy to access remote repository. Proxy should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;", 
			"선택적으로 원격 저장소에 액세스하기 위한 프록시를 구성하십시오. 프록시는 &lt;프록시 호스트&gt;:&lt;프록시 포트&gt; 형식이어야 합니다");
		m.put("Optionally define a unique key for the project with two or more upper case letters. This key can be used to reference issues, builds, and pull requests with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of <code>&lt;project path&gt;#&lt;number&gt;</code>", 
			"선택적으로 두 개 이상의 대문자로 구성된 프로젝트의 고유 키를 정의하십시오. 이 키는 <code>&lt;프로젝트 키&gt;-&lt;번호&gt;</code> 대신 <code>&lt;프로젝트 경로&gt;#&lt;번호&gt;</code>로 안정적이고 짧은 형식으로 이슈, 빌드 및 풀 리퀘스트를 참조하는 데 사용할 수 있습니다");
		m.put("Optionally define parameter specifications of the job", "선택적으로 작업의 매개변수 사양을 정의하십시오");
		m.put("Optionally define parameter specifications of the step template", "선택적으로 단계 템플릿의 매개변수 사양을 정의하십시오");
		m.put("Optionally describe the group", "선택적으로 그룹을 설명하십시오");
		m.put("Optionally describes the custom field. Html tags are accepted", "선택적으로 사용자 정의 필드를 설명합니다. Html 태그가 허용됩니다");
		m.put("Optionally describes the param. Html tags are accepted.", "선택적으로 매개변수를 설명합니다. Html 태그가 허용됩니다");
		m.put("Optionally filter builds", "선택적으로 빌드를 필터링하십시오");
		m.put("Optionally filter issues", "선택적으로 이슈를 필터링하십시오");
		m.put("Optionally filter pull requests", "선택적으로 풀 리퀘스트를 필터링하십시오");
		m.put("Optionally leave a note", "선택적으로 노트를 남기십시오");
		m.put("Optionally mount directories or files under job workspace into container", "선택적으로 작업 공간의 디렉토리 또는 파일을 컨테이너에 마운트하십시오");
		m.put("Optionally select fields to prompt when this button is pressed", "선택적으로 이 버튼을 눌렀을 때 프롬프트할 필드를 선택하십시오");
		m.put("Optionally select fields to remove when this transition happens", "선택적으로 이 전환이 발생할 때 제거할 필드를 선택하십시오");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"선택적으로 사용자 LDAP 항목 내부의 속성 이름을 지정하십시오. 해당 값은 사용자 이메일로 사용됩니다. 이 필드는 일반적으로 RFC 2798에 따라 <i>mail</i>로 설정됩니다");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"선택적으로 사용자 LDAP 항목 내부의 속성 이름을 지정하십시오. 해당 값은 사용자 전체 이름으로 사용됩니다. 이 필드는 일반적으로 RFC 2798에 따라 <i>displayName</i>로 설정됩니다. 비워 두면 사용자의 전체 이름이 검색되지 않습니다");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"선택적으로 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 GitHub 액세스 토큰으로 지정하세요. 이는 GitHub에 호스팅된 종속 항목의 릴리스 노트를 검색하는 데 사용되며, 인증된 액세스는 더 높은 속도 제한을 받습니다.");
		m.put("Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko", 
			"선택적으로 kaniko의 <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>추가 옵션</a>을 지정하세요.");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane", 
			"선택적으로 crane의 <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>추가 옵션</a>을 지정하세요.");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane", 
			"선택적으로 crane의 <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>추가 옵션</a>을 지정하세요.");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to build for platform of the node running the job", 
			"선택적으로 <span class='text-info'>쉼표로 구분된</span> 빌드 플랫폼을 지정하세요. 예를 들어 <tt>linux/amd64,linux/arm64</tt>. 작업을 실행하는 노드의 플랫폼으로 빌드하려면 비워 두세요.");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to scan, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to scan all platforms in OCI layout", 
			"선택적으로 <span class='text-info'>쉼표로 구분된</span> 스캔 플랫폼을 지정하세요. 예를 들어 <tt>linux/amd64,linux/arm64</tt>. OCI 레이아웃의 모든 플랫폼을 스캔하려면 비워 두세요.");
		m.put("Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use file <tt>Dockerfile</tt> under build path specified above", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 워크스페이스</a>에 상대적인 Dockerfile을 지정하세요. 위에서 지정한 빌드 경로 아래의 <tt>Dockerfile</tt> 파일을 사용하려면 비워 두세요.");
		m.put("Optionally specify JavaScript config to be used by Renovate CLI", "선택적으로 Renovate CLI에서 사용할 JavaScript 구성을 지정하세요.");
		m.put("Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. Leave empty to derive from server url", 
			"선택적으로 SSH 루트 URL을 지정하세요. 이는 SSH 프로토콜을 통해 프로젝트 클론 URL을 구성하는 데 사용됩니다. 서버 URL에서 파생하려면 비워 두세요.");
		m.put("Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of the text input", 
			"선택적으로 텍스트 입력의 유효한 값을 위한 <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>정규 표현식 패턴</a>을 지정하세요.");
		m.put("Optionally specify a OneDev project to be used as parent of imported projects. Leave empty to import as root projects", 
			"선택적으로 가져온 프로젝트의 상위 프로젝트로 사용할 OneDev 프로젝트를 지정하세요. 루트 프로젝트로 가져오려면 비워 두세요.");
		m.put("Optionally specify a OneDev project to be used as parent of imported repositories. Leave empty to import as root projects", 
			"선택적으로 가져온 저장소의 상위 프로젝트로 사용할 OneDev 프로젝트를 지정하세요. 루트 프로젝트로 가져오려면 비워 두세요.");
		m.put("Optionally specify a base query for the list", "선택적으로 목록에 대한 기본 쿼리를 지정하세요.");
		m.put("Optionally specify a base query to filter/order issues in backlog. Backlog issues are those not associating with current iteration", 
			"선택적으로 백로그의 문제를 필터링/정렬하기 위한 기본 쿼리를 지정하세요. 백로그 문제는 현재 반복과 연관되지 않은 문제입니다.");
		m.put("Optionally specify a base query to filter/order issues of the board", "선택적으로 보드의 문제를 필터링/정렬하기 위한 기본 쿼리를 지정하세요.");
		m.put("Optionally specify a cron expression to schedule database auto-backup. The cron expression format is <em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>.For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz tutorial</a>.The backup files will be placed into <em>db-backup</em> folder under OneDev installation directory. In case multiple servers connect to form a cluster, auto-backup takes place on the <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>lead server</a>. Leave this property empty if you do not want to enable database auto backup.", 
			"선택적으로 데이터베이스 자동 백업을 예약하기 위한 크론 표현식을 지정하세요. 크론 표현식 형식은 <em>&lt;초&gt; &lt;분&gt; &lt;시간&gt; &lt;일&gt; &lt;월&gt; &lt;요일&gt;</em>입니다. 예를 들어, <em>0 0 1 * * ?</em>는 매일 오전 1시를 의미합니다. 형식에 대한 자세한 내용은 <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz 튜토리얼</a>을 참조하세요. 백업 파일은 OneDev 설치 디렉터리 아래의 <em>db-backup</em> 폴더에 저장됩니다. 여러 서버가 클러스터를 형성하기 위해 연결된 경우, 자동 백업은 <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>리드 서버</a>에서 수행됩니다. 데이터베이스 자동 백업을 활성화하지 않으려면 이 속성을 비워 두세요.");
		m.put("Optionally specify a date field to hold due date information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"선택적으로 마감일 정보를 보유할 날짜 필드를 지정하세요.<br><b>참고: </b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to put retrieved artifacts. Leave empty to use job workspace itself", 
			"선택적으로 검색된 아티팩트를 저장할 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 워크스페이스</a>에 상대적인 경로를 지정하세요. 작업 워크스페이스 자체를 사용하려면 비워 두세요.");
		m.put("Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. <b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, as the volume is only used to hold temporary build files", 
			"선택적으로 빌드 볼륨을 동적으로 할당할 스토리지 클래스를 지정하세요. 기본 스토리지 클래스를 사용하려면 비워 두세요. <b class='text-warning'>참고:</b> 스토리지 클래스의 복구 정책은 <code>Delete</code>로 설정되어야 합니다. 볼륨은 임시 빌드 파일을 저장하는 데만 사용됩니다.");
		m.put("Optionally specify a working period field to hold estimated time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"선택적으로 예상 시간을 보유할 작업 기간 필드를 지정하세요.<br><b>참고: </b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Optionally specify a working period field to hold spent time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"선택적으로 소요 시간을 보유할 작업 기간 필드를 지정하세요.<br><b>참고: </b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Optionally specify a working period field to hold time estimate infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"선택적으로 시간 추정을 보유할 작업 기간 필드를 지정하세요.<br><b>참고: </b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Optionally specify a working period field to hold time spent infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"선택적으로 소요 시간을 보유할 작업 기간 필드를 지정하세요.<br><b>참고: </b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Optionally specify additional options for buildx build command", "선택적으로 buildx 빌드 명령의 추가 옵션을 지정하세요.");
		m.put("Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, the response header <code>Access-Control-Allow-Origin</code> will be set to the same value", 
			"선택적으로 허용된 <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> 출처를 지정하세요. CORS 간단 또는 사전 요청의 경우, 요청 헤더 <code>Origin</code>의 값이 여기에 포함되어 있으면 응답 헤더 <code>Access-Control-Allow-Origin</code>이 동일한 값으로 설정됩니다.");
		m.put("Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match", 
			"선택적으로 자체 가입 사용자에 대한 허용된 이메일 도메인을 지정하세요. 패턴 매칭을 위해 '*' 또는 '?'를 사용하세요.");
		m.put("Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). Leave empty to all types", 
			"선택적으로 커밋 메시지 풋터 확인을 위한 적용 가능한 커밋 유형을 지정하세요 (값을 추가하려면 ENTER를 누르세요). 모든 유형을 지정하려면 비워 두세요.");
		m.put("Optionally specify applicable jobs of this executor", "선택적으로 이 실행자에 적용 가능한 작업을 지정합니다");
		m.put("Optionally specify applicable users who pushed the change", "변경 사항을 푸시한 적용 가능한 사용자를 선택적으로 지정");
		m.put("Optionally specify arguments to run above image", "선택적으로 위 이미지 실행을 위한 인수를 지정하세요.");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 워크스페이스</a>로 종속 항목에서 검색할 아티팩트를 지정하세요. 게시된 아티팩트(아티팩트 게시 단계에서 게시된 것)만 검색할 수 있습니다. 아티팩트를 검색하지 않으려면 비워 두세요.");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"선택적으로 이 버튼을 누를 수 있는 권한이 있는 역할을 지정하세요. 지정하지 않으면 모든 사용자가 허용됩니다.");
		m.put("Optionally specify base query of the list", "선택적으로 목록의 기본 쿼리를 지정하세요.");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"선택적으로 이 비밀에 액세스할 수 있는 브랜치/사용자/그룹을 지정하세요. 비워 두면 외부 풀 요청을 통해 트리거된 작업을 포함하여 모든 작업이 이 비밀에 액세스할 수 있습니다.");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 워크스페이스</a>에 상대적인 빌드 컨텍스트 경로를 지정하세요. 작업 워크스페이스 자체를 사용하려면 비워 두세요. <code>Dockerfile</code> 파일은 빌드 컨텍스트 디렉터리에 존재해야 하며, 옵션 <code>--dockerfile</code>로 다른 위치를 지정하지 않는 한 그렇습니다.");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 워크스페이스</a>에 상대적인 빌드 경로를 지정하세요. 작업 워크스페이스 자체를 사용하려면 비워 두세요.");
		m.put("Optionally specify cluster role the job pods service account binding to. This is necessary if you want to do things such as running other Kubernetes pods in job command", 
			"선택적으로 작업 포드 서비스 계정이 바인딩되는 클러스터 역할을 지정하세요. 이는 작업 명령에서 다른 Kubernetes 포드를 실행하는 등의 작업을 수행하려는 경우 필요합니다.");
		m.put("Optionally specify comma separated licenses to be ignored", "선택적으로 무시할 쉼표로 구분된 라이센스를 지정하세요.");
		m.put("Optionally specify container arguments separated by space. Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse this with container options which should be specified in executor setting", 
			"선택적으로 공백으로 구분된 컨테이너 인수를 지정하세요. 공백이 포함된 단일 인수는 따옴표로 묶어야 합니다. <b class='text-warning'>참고: </b> 이는 실행자 설정에서 지정해야 하는 컨테이너 옵션과 혼동하지 마세요.");
		m.put("Optionally specify cpu limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"선택적으로 이 실행자를 사용하는 각 작업/서비스에 대한 CPU 제한을 지정하세요. 자세한 내용은 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes 리소스 관리</a>를 참조하세요.");
		m.put("Optionally specify cpu limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> of relevant containers", 
			"선택적으로 이 실행자를 사용하는 각 작업/서비스의 CPU 제한을 지정하세요. 이는 관련 컨테이너의 옵션 <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a>로 사용됩니다.");
		m.put("Optionally specify criteria of issues which can be linked", "선택적으로 연결할 수 있는 문제의 기준을 지정하세요.");
		m.put("Optionally specify criteria of issues which can be linked on the other side", "선택적으로 다른 쪽에서 연결할 수 있는 문제의 기준을 지정하세요.");
		m.put("Optionally specify custom fields allowed to edit when open new issues", "선택적으로 새 문제를 열 때 편집할 수 있는 사용자 정의 필드를 지정하세요.");
		m.put("Optionally specify depth for a shallow clone in order to speed up source retrieval", 
			"소스 검색 속도를 높이기 위해 얕은 클론의 깊이를 선택적으로 지정하세요.");
		m.put("Optionally specify description of the issue", "선택적으로 문제의 설명을 지정하세요.");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"선택적으로 스캔 경로 내부의 디렉터리 또는 글로브 패턴을 건너뛰세요. 여러 건너뛰기는 공백으로 구분해야 합니다.");
		m.put("Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types", 
			"확장자로 허용되지 않는 파일 유형을 선택적으로 지정하세요 (값을 추가하려면 ENTER를 누르세요), 예를 들어 <code>exe</code>, <code>bin</code>. 모든 파일 유형을 허용하려면 비워 두세요.");
		m.put("Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. Leave empty to use docker executable in PATH", 
			"선택적으로 Docker 실행 파일을 지정하세요. 예를 들어 <i>/usr/local/bin/docker</i>. PATH에 있는 Docker 실행 파일을 사용하려면 비워 두세요.");
		m.put("Optionally specify docker options to create network. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"선택적으로 네트워크를 생성하기 위한 Docker 옵션을 지정하세요. 여러 옵션은 공백으로 구분해야 하며, 공백이 포함된 단일 옵션은 따옴표로 묶어야 합니다.");
		m.put("Optionally specify docker options to run container. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"선택적으로 컨테이너를 실행하기 위한 Docker 옵션을 지정하세요. 여러 옵션은 공백으로 구분해야 하며, 공백이 포함된 단일 옵션은 따옴표로 묶어야 합니다.");
		m.put("Optionally specify docker sock to use. Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows", 
			"선택적으로 사용할 Docker 소켓을 지정하세요. 기본값은 Linux에서는 <i>/var/run/docker.sock</i>, Windows에서는 <i>//./pipe/docker_engine</i>입니다.");
		m.put("Optionally specify environment variables for the container", "선택적으로 컨테이너의 환경 변수를 지정하세요.");
		m.put("Optionally specify environment variables for this step", "선택적으로 이 단계의 환경 변수를 지정하세요.");
		m.put("Optionally specify environment variables of the service", "선택적으로 서비스의 환경 변수를 지정하세요.");
		m.put("Optionally specify estimated time.", "선택적으로 예상 시간을 지정하세요.");
		m.put("Optionally specify executor for this job. Leave empty to use auto-discover executor", 
			"선택적으로 이 작업에 대한 실행자를 지정합니다. 비워두면 자동 검색된 실행자를 사용합니다");
		m.put("Optionally specify executor for this job. Leave empty to use first applicable executor", 
			"선택적으로 이 작업에 대한 실행자를 지정합니다. 비워두면 첫 번째 적용 가능한 실행자를 사용합니다");
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"선택적으로 캐시 변경을 감지할 때 무시할 캐시 경로에 상대적인 파일을 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 여러 파일은 공백으로 구분해야 하며, 공백이 포함된 단일 파일은 따옴표로 묶어야 합니다.");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"선택적으로 사용자의 그룹 멤버십 정보를 검색하려면 그룹 검색 기준을 지정하세요. 예를 들어: <i>cn=Users, dc=example, dc=com</i>. Active Directory 그룹에 적절한 권한을 부여하려면 동일한 이름의 OneDev 그룹이 정의되어야 합니다. 그룹 멤버십을 OneDev 측에서 관리하려면 비워 두세요.");
		m.put("Optionally specify issue links allowed to edit", "선택적으로 편집할 수 있는 문제 링크를 지정하세요.");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "선택적으로 이 템플릿에 적용 가능한 문제를 지정하세요. 모든 문제를 지정하려면 비워 두세요.");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"선택적으로 이 전환에 적용 가능한 문제를 지정하세요. 모든 문제를 지정하려면 비워 두세요.");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"선택적으로 이 전환에 적용 가능한 문제를 지정하세요. 모든 문제를 지정하려면 비워 두세요.");
		m.put("Optionally specify jobs allowed to use this script", "선택적으로 이 스크립트를 사용할 수 있는 작업을 지정하세요.");
		m.put("Optionally specify memory limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"선택적으로 이 실행자를 사용하는 각 작업/서비스에 대한 메모리 제한을 지정하세요. 자세한 내용은 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes 리소스 관리</a>를 참조하세요.");
		m.put("Optionally specify memory limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> of relevant containers", 
			"선택적으로 이 실행자를 사용하는 각 작업/서비스의 메모리 제한을 지정하세요. 이는 관련 컨테이너의 옵션 <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a>로 사용됩니다.");
		m.put("Optionally specify merge strategy of created pull request. Leave empty to use default strategy of each project", 
			"선택적으로 생성된 풀 요청의 병합 전략을 지정하세요. 각 프로젝트의 기본 전략을 사용하려면 비워 두세요.");
		m.put("Optionally specify message of the tag", "선택적으로 태그 메시지를 지정하세요.");
		m.put("Optionally specify name of the attribute inside the user LDAP entry whose values will be taken as user SSH keys. SSH keys will be managed by LDAP only if this field is set", 
			"선택적으로 사용자 LDAP 항목 내부의 속성 이름을 지정하세요. 해당 값은 사용자 SSH 키로 사용됩니다. 이 필드가 설정된 경우 SSH 키는 LDAP에서만 관리됩니다.");
		m.put("Optionally specify node selector of the job pods", "선택적으로 작업 포드의 노드 선택기를 지정하세요.");
		m.put("Optionally specify options for docker builder prune command", "선택적으로 Docker 빌더 정리 명령의 옵션을 지정하세요.");
		m.put("Optionally specify options for scp command. Multiple options need to be separated with space", 
			"선택적으로 scp 명령의 옵션을 지정하세요. 여러 옵션은 공백으로 구분해야 합니다.");
		m.put("Optionally specify options for ssh command. Multiple options need to be separated with space", 
			"선택적으로 ssh 명령의 옵션을 지정하세요. 여러 옵션은 공백으로 구분해야 합니다.");
		m.put("Optionally specify options passed to renovate cli. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"선택적으로 Renovate CLI에 전달된 옵션을 지정하세요. 여러 옵션은 공백으로 구분해야 하며, 공백이 포함된 단일 옵션은 따옴표로 묶어야 합니다.");
		m.put("Optionally specify osv scanner <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. You may ignore particular vulnerabilities via this file", 
			"선택적으로 <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>OSV 스캐너 구성 파일</a>을 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 워크스페이스</a> 아래에 지정하세요. 이 파일을 통해 특정 취약점을 무시할 수 있습니다.");
		m.put("Optionally specify path protection rules", "선택적으로 경로 보호 규칙을 지정하세요.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 워크스페이스</a>에 상대적인 경로를 지정하여 trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>무시 파일</a>로 사용하세요.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 워크스페이스</a>에 상대적인 경로를 지정하여 trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>비밀 구성</a>으로 사용하세요.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 워크스페이스</a>에서 아티팩트를 게시할 경로를 지정하세요. 작업 워크스페이스 자체를 사용하려면 비워 두세요.");
		m.put("Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. Leave empty to pull all platforms in image", 
			"선택적으로 가져올 플랫폼을 지정하세요. 예를 들어 <tt>linux/amd64</tt>. 이미지의 모든 플랫폼을 가져오려면 비워 두세요.");
		m.put("Optionally specify project to show builds of. Leave empty to show builds of all projects with permissions", 
			"선택적으로 빌드를 표시할 프로젝트를 지정하세요. 권한이 있는 모든 프로젝트의 빌드를 표시하려면 비워 두세요.");
		m.put("Optionally specify project to show issues of. Leave empty to show issues of all accessible projects", 
			"선택적으로 문제를 표시할 프로젝트를 지정하세요. 접근 가능한 모든 프로젝트의 문제를 표시하려면 비워 두세요.");
		m.put("Optionally specify project to show packages of. Leave empty to show packages of all projects with permissions", 
			"선택적으로 패키지를 표시할 프로젝트를 지정하세요. 권한이 있는 모든 프로젝트의 패키지를 표시하려면 비워 두세요.");
		m.put("Optionally specify ref of above job, for instance <i>refs/heads/main</i>. Use * for wildcard match", 
			"선택적으로 위 작업의 참조를 지정하세요. 예를 들어 <i>refs/heads/main</i>. 와일드카드 매칭을 위해 *를 사용하세요.");
		m.put("Optionally specify registry logins to override those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token secret for password secret", 
			"선택적으로 작업 실행자에 정의된 레지스트리 로그인을 재정의하세요. 내장 레지스트리의 경우, 레지스트리 URL로 <code>@server_url@</code>, 사용자 이름으로 <code>@job_token@</code>, 비밀번호 비밀로 액세스 토큰 비밀을 사용하세요.");
		m.put("Optionally specify relative directory to put uploaded files", "선택적으로 업로드된 파일을 저장할 상대 디렉터리를 지정하세요.");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to clone code into. Leave empty to use job workspace itself", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 워크스페이스</a> 아래에 코드를 클론할 상대 경로를 지정하세요. 작업 워크스페이스 자체를 사용하려면 비워 두세요.");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 워크스페이스</a> 아래에 스캔할 상대 경로를 지정하세요. 작업 워크스페이스 자체를 사용하려면 비워 두세요.");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"선택적으로 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 워크스페이스</a> 아래에 종속성 취약점을 스캔할 상대 경로를 지정하세요. 여러 경로를 지정할 수 있으며 공백으로 구분해야 합니다. 작업 워크스페이스 자체를 사용하려면 비워 두세요.");
		m.put("Optionally specify required reviewers for changes of specified branch", "선택적으로 지정된 브랜치 변경 사항에 대한 필요한 검토자를 지정하세요.");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"선택적으로 브랜치를 생성할 리비전을 지정하세요. 빌드 커밋에서 생성하려면 비워 두세요.");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"선택적으로 빌드 아티팩트를 저장할 별도의 디렉터리를 지정하세요. 절대 디렉터리가 아닌 경우 사이트 디렉터리에 상대적으로 간주됩니다.");
		m.put("Optionally specify separate directory to store git lfs files. Non-absolute directory is considered to be relative to site directory", 
			"선택적으로 git lfs 파일을 저장할 별도의 디렉터리를 지정하세요. 절대 디렉터리가 아닌 경우 사이트 디렉터리에 상대적으로 간주됩니다.");
		m.put("Optionally specify separate directory to store package files. Non-absolute directory is considered to be relative to site directory", 
			"선택적으로 패키지 파일을 저장할 별도의 디렉터리를 지정하세요. 절대 디렉터리가 아닌 경우 사이트 디렉터리에 상대적으로 간주됩니다.");
		m.put("Optionally specify services required by this job. <b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors (server docker executor, remote docker executor, or kubernetes executor)", 
			"선택적으로 이 작업에 필요한 서비스를 지정하세요. <b class='text-warning'>참고:</b> 서비스는 Docker를 인식하는 실행자(서버 Docker 실행자, 원격 Docker 실행자 또는 Kubernetes 실행자)에서만 지원됩니다.");
		m.put("Optionally specify space-separated branches applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"선택적으로 이 전환에 적용 가능한 공백으로 구분된 브랜치를 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 모든 브랜치를 일치시키려면 비워 두세요.");
		m.put("Optionally specify space-separated branches applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for default branch", 
			"선택적으로 이 트리거에 적용 가능한 공백으로 구분된 브랜치를 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 기본 브랜치에 대해 비워 두세요.");
		m.put("Optionally specify space-separated branches to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"선택적으로 확인할 공백으로 구분된 브랜치를 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 모든 브랜치를 일치시키려면 비워 두세요.");
		m.put("Optionally specify space-separated commit messages applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"이 전환에 적용 가능한 공백으로 구분된 커밋 메시지를 선택적으로 지정하십시오. 와일드카드 일치를 위해 '*' 또는 '?'를 사용하십시오. 제외하려면 '-'로 시작하십시오. 모두 일치시키려면 비워 두십시오");
		m.put("Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all files", 
			"선택적으로 확인할 공백으로 구분된 파일을 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 모든 파일을 일치시키려면 비워 두세요.");
		m.put("Optionally specify space-separated jobs applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"선택적으로 이 전환에 적용 가능한 공백으로 구분된 작업을 지정하세요. 와일드카드 매칭을 위해 '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 모든 작업을 일치시키려면 비워 두세요.");
		m.put("Optionally specify space-separated projects applicable for this trigger. This is useful for instance when you want to prevent the job from being triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all projects", 
			"선택적으로 이 트리거에 적용 가능한 공백으로 구분된 프로젝트를 지정하세요. 예를 들어, 포크된 프로젝트에서 작업이 트리거되는 것을 방지하려는 경우 유용합니다. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 모든 프로젝트를 일치시키려면 비워 두세요.");
		m.put("Optionally specify space-separated projects to search in. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to search in all projects with code read permission", 
			"선택적으로 검색할 공백으로 구분된 프로젝트를 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 코드 읽기 권한이 있는 모든 프로젝트를 검색하려면 비워 두세요.");
		m.put("Optionally specify space-separated reports. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"선택적으로 공백으로 구분된 보고서를 지정하십시오. '*' 또는 '?'를 사용하여 와일드카드 매칭을 수행합니다. '-'로 시작하여 제외할 수 있습니다.");
		m.put("Optionally specify space-separated service images applicable for this locator. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"선택적으로 이 로케이터에 적용 가능한 공백으로 구분된 서비스 이미지를 지정하십시오. '**', '*' 또는 '?'를 사용하여 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 수행합니다. '-'로 시작하여 제외할 수 있습니다. 비워두면 모두 매칭됩니다.");
		m.put("Optionally specify space-separated service names applicable for this locator. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"선택적으로 이 로케이터에 적용 가능한 공백으로 구분된 서비스 이름을 지정하십시오. '*' 또는 '?'를 사용하여 와일드카드 매칭을 수행합니다. '-'로 시작하여 제외할 수 있습니다. 비워두면 모두 매칭됩니다.");
		m.put("Optionally specify space-separated tags to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all tags", 
			"선택적으로 확인할 공백으로 구분된 태그를 지정하십시오. '**', '*' 또는 '?'를 사용하여 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 수행합니다. '-'로 시작하여 제외할 수 있습니다. 비워두면 모든 태그가 매칭됩니다.");
		m.put("Optionally specify space-separated target branches of the pull requests to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"선택적으로 확인할 풀 리퀘스트의 대상 브랜치를 공백으로 구분하여 지정하십시오. '**', '*' 또는 '?'를 사용하여 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 수행합니다. '-'로 시작하여 제외할 수 있습니다. 비워두면 모든 브랜치가 매칭됩니다.");
		m.put("Optionally specify the OpenID claim to retrieve groups of authenticated user. Depending on the provider, you may need to request additional scopes above to make this claim available", 
			"선택적으로 인증된 사용자의 그룹을 가져오기 위한 OpenID 클레임을 지정하십시오. 제공자에 따라 이 클레임을 사용할 수 있도록 추가 범위를 요청해야 할 수 있습니다.");
		m.put("Optionally specify the maximum value allowed.", "선택적으로 허용되는 최대 값을 지정하십시오.");
		m.put("Optionally specify the minimum value allowed.", "선택적으로 허용되는 최소 값을 지정하십시오.");
		m.put("Optionally specify the project to publish site files to. Leave empty to publish to current project", 
			"선택적으로 사이트 파일을 게시할 프로젝트를 지정하십시오. 현재 프로젝트에 게시하려면 비워두십시오.");
		m.put("Optionally specify uid:gid to run container as. <b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or using user namespace remapping", 
			"선택적으로 컨테이너를 실행할 uid:gid를 지정하십시오. <b class='text-warning'>참고:</b> 컨테이너 런타임이 루트리스이거나 사용자 네임스페이스 리매핑을 사용하는 경우 이 설정을 비워두어야 합니다.");
		m.put("Optionally specify user name to access remote repository", "선택적으로 원격 저장소에 접근할 사용자 이름을 지정하십시오.");
		m.put("Optionally specify valid scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope", 
			"선택적으로 유효한 컨벤션 커밋 범위를 지정하십시오 (값을 추가하려면 ENTER를 누르십시오). 비워두면 임의의 범위를 허용합니다.");
		m.put("Optionally specify valid types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type", 
			"선택적으로 유효한 컨벤션 커밋 유형을 지정하십시오 (값을 추가하려면 ENTER를 누르십시오). 비워두면 임의의 유형을 허용합니다.");
		m.put("Optionally specify value of git config <code>pack.packSizeLimit</code> for the repository", 
			"선택적으로 저장소에 대한 git config <code>pack.packSizeLimit</code> 값을 지정하십시오.");
		m.put("Optionally specify value of git config <code>pack.threads</code> for the repository", 
			"선택적으로 저장소에 대한 git config <code>pack.threads</code> 값을 지정하십시오.");
		m.put("Optionally specify value of git config <code>pack.window</code> for the repository", 
			"선택적으로 저장소에 대한 git config <code>pack.window</code> 값을 지정하십시오.");
		m.put("Optionally specify value of git config <code>pack.windowMemory</code> for the repository", 
			"선택적으로 저장소에 대한 git config <code>pack.windowMemory</code> 값을 지정하십시오.");
		m.put("Optionally specify where to run service pods specified in job. The first matching locator will be used. If no any locators are found, node selector of the executor will be used", 
			"선택적으로 작업에서 지정된 서비스 포드를 실행할 위치를 지정하십시오. 첫 번째로 매칭되는 로케이터가 사용됩니다. 로케이터가 없으면 실행기의 노드 선택기가 사용됩니다.");
		m.put("Optionally specify working directory of the container. Leave empty to use default working directory of the container", 
			"선택적으로 컨테이너의 작업 디렉토리를 지정하십시오. 컨테이너의 기본 작업 디렉토리를 사용하려면 비워두십시오.");
		m.put("Options", "옵션");
		m.put("Or manually enter the secret key below in your authenticator app", "또는 아래의 비밀 키를 인증 앱에 수동으로 입력하세요");
		m.put("Order By", "정렬 기준");
		m.put("Order More User Months", "더 많은 사용자 월 정렬");
		m.put("Order Subscription", "구독 정렬");
		m.put("Ordered List", "정렬된 목록");
		m.put("Ordered list", "정렬된 목록");
		m.put("Osv License Scanner", "Osv 라이센스 스캐너");
		m.put("Osv Vulnerability Scanner", "Osv 취약점 스캐너");
		m.put("Other", "기타");
		m.put("Outline", "개요");
		m.put("Outline Search", "개요 검색");
		m.put("Output", "출력");
		m.put("Overall", "전체");
		m.put("Overall Estimated Time:", "전체 예상 시간:");
		m.put("Overall Spent Time:", "전체 소요 시간:");
		m.put("Overview", "개요");
		m.put("Own:", "소유:");
		m.put("Ownered By", "소유자");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "PEM 개인 키는 '-----BEGIN RSA PRIVATE KEY-----'로 시작합니다.");
		m.put("PENDING", "대기 중");
		m.put("PMD Report", "PMD 보고서");
		m.put("Pack", "패키지");
		m.put("Pack Notification", "패키지 알림");
		m.put("Pack Size Limit", "패키지 크기 제한");
		m.put("Pack Type", "패키지 유형");
		m.put("Package", "패키지");
		m.put("Package Management", "패키지 관리");
		m.put("Package Notification", "패키지 알림");
		m.put("Package Notification Template", "패키지 알림 템플릿");
		m.put("Package Privilege", "패키지 권한");
		m.put("Package Storage", "패키지 저장소");
		m.put("Package list", "패키지 목록");
		m.put("Package {0} deleted", "패키지 {0}이 삭제되었습니다.");
		m.put("Packages", "패키지들");
		m.put("Page Not Found", "페이지를 찾을 수 없습니다");
		m.put("Page is in error, reload to recover", "페이지에 오류가 있습니다. 복구하려면 다시 로드하십시오.");
		m.put("Param Instance", "매개변수 인스턴스");
		m.put("Param Instances", "매개변수 인스턴스들");
		m.put("Param Map", "매개변수 맵");
		m.put("Param Matrix", "매개변수 매트릭스");
		m.put("Param Name", "매개변수 이름");
		m.put("Param Spec", "매개변수 사양");
		m.put("Param Spec Bean", "매개변수 사양 빈");
		m.put("Parameter", "매개변수");
		m.put("Parameter Specs", "매개변수 사양들");
		m.put("Params", "매개변수들");
		m.put("Params & Triggers", "매개변수 및 트리거");
		m.put("Params to Display", "표시할 매개변수들");
		m.put("Parent Bean", "상위 빈");
		m.put("Parent OneDev Project", "상위 OneDev 프로젝트");
		m.put("Parent Project", "상위 프로젝트");
		m.put("Parent project not found", "상위 프로젝트를 찾을 수 없습니다.");
		m.put("Parents", "상위들");
		m.put("Partially covered", "부분적으로 커버됨");
		m.put("Partially covered by some tests", "일부 테스트로 부분적으로 커버됨");
		m.put("Passcode", "암호");
		m.put("Passed", "통과됨");
		m.put("Password", "비밀번호");
		m.put("Password Authenticator", "비밀번호 인증기");
		m.put("Password Edit Bean", "비밀번호 편집 빈");
		m.put("Password Must Contain Digit", "비밀번호는 숫자를 포함해야 합니다");
		m.put("Password Must Contain Lowercase", "비밀번호는 소문자를 포함해야 합니다");
		m.put("Password Must Contain Special Character", "비밀번호는 특수 문자를 포함해야 합니다");
		m.put("Password Must Contain Uppercase", "비밀번호는 대문자를 포함해야 합니다");
		m.put("Password Policy", "비밀번호 정책");
		m.put("Password Reset", "비밀번호 재설정");
		m.put("Password Reset Bean", "비밀번호 재설정 빈");
		m.put("Password Reset Template", "비밀번호 재설정 템플릿");
		m.put("Password Secret", "비밀번호 비밀");
		m.put("Password and its confirmation should be identical.", "비밀번호와 확인 비밀번호는 동일해야 합니다.");
		m.put("Password changed. Please login with your new password", "비밀번호가 변경되었습니다. 새 비밀번호로 로그인하세요");
		m.put("Password has been changed", "비밀번호가 변경되었습니다.");
		m.put("Password has been removed", "비밀번호가 제거되었습니다.");
		m.put("Password has been set", "비밀번호가 설정되었습니다.");
		m.put("Password of the user", "사용자의 비밀번호");
		m.put("Password or Access Token for Remote Repository", "원격 저장소를 위한 비밀번호 또는 액세스 토큰");
		m.put("Password reset request has been sent", "비밀번호 재설정 요청이 전송되었습니다.");
		m.put("Password reset url is invalid or obsolete", "비밀번호 재설정 URL이 유효하지 않거나 오래되었습니다");
		m.put("PasswordMinimum Length", "비밀번호 최소 길이");
		m.put("Paste subscription key here", "구독 키를 여기에 붙여넣으십시오.");
		m.put("Path containing spaces or starting with dash needs to be quoted", "공백이 포함되거나 대시로 시작하는 경로는 따옴표로 묶어야 합니다.");
		m.put("Path placeholder", "경로 자리표시자");
		m.put("Path to kubectl", "kubectl 경로");
		m.put("Paths", "경로들");
		m.put("Pattern", "패턴");
		m.put("Pause", "일시 중지");
		m.put("Pause All Queried Agents", "쿼리된 모든 에이전트 일시 중지");
		m.put("Pause Selected Agents", "선택된 에이전트 일시 중지");
		m.put("Paused", "일시 중지됨");
		m.put("Paused all queried agents", "쿼리된 모든 에이전트가 일시 중지되었습니다.");
		m.put("Paused selected agents", "선택된 에이전트가 일시 중지되었습니다.");
		m.put("Pem Private Key", "Pem 개인 키");
		m.put("Pending", "대기 중");
		m.put("Performance", "성능");
		m.put("Performance Setting", "성능 설정");
		m.put("Performance Settings", "성능 설정들");
		m.put("Performance settings have been saved", "성능 설정이 저장되었습니다");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\"", 
			"퍼지 검색을 수행 중입니다. 검색 텍스트를 '~'로 감싸 추가 조건을 추가하세요. 예: ~검색할 텍스트~ 및 \"상태\"가 \"열림\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"", 
			"퍼지 검색을 수행 중입니다. 검색 텍스트를 '~'로 감싸 추가 조건을 추가하세요. 예: ~검색할 텍스트~ 및 \"유형\"이 \"NPM\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online", 
			"퍼지 검색을 수행 중입니다. 검색 텍스트를 '~'로 감싸 추가 조건을 추가하세요. 예: ~검색할 텍스트~ 및 온라인");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"퍼지 검색을 수행 중입니다. 검색 텍스트를 '~'로 감싸 추가 조건을 추가하세요. 예: ~검색할 텍스트~ 및 열림");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and owned by me", 
			"퍼지 검색을 수행 중입니다. 검색 텍스트를 '~'로 감싸 추가 조건을 추가하세요. 예: ~검색할 텍스트~ 및 내가 소유한 항목");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved", 
			"퍼지 검색을 수행 중입니다. 검색 텍스트를 '~'로 감싸 추가 조건을 추가하세요. 예: ~검색할 텍스트~ 및 미해결");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"퍼지 검색을 수행 중입니다. 검색 텍스트를 '~'로 감싸 추가 조건을 추가하세요. 예: ~검색할 텍스트~ 작성자(robin)");
		m.put("Permanent link", "영구 링크");
		m.put("Permanent link of this selection", "이 선택의 영구 링크");
		m.put("Permission denied", "권한이 거부되었습니다");
		m.put("Permission will be checked upon actual operation", "실제 작업 시 권한이 확인됩니다");
		m.put("Physical memory in mega bytes", "메가바이트 단위의 물리적 메모리");
		m.put("Pick Existing", "기존 항목 선택");
		m.put("Pin this issue", "이 이슈를 고정");
		m.put("Pipeline", "파이프라인");
		m.put("Placeholder", "플레이스홀더");
		m.put("Plain text expected", "일반 텍스트 예상됨");
		m.put("Platform", "플랫폼");
		m.put("Platforms", "플랫폼들");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"아래 복구 코드를 <a wicket:id=\"download\" class=\"font-weight-bolder\">다운로드</a>하고 비밀로 유지하세요. 이 코드는 인증 애플리케이션에 접근할 수 없는 경우 계정에 일회성 접근을 제공하는 데 사용됩니다. 이 코드는 <b>다시 표시되지 않습니다</b>");
		m.put("Please Confirm", "확인해주세요");
		m.put("Please Note", "참고해주세요");
		m.put("Please check your email for password reset instructions", "비밀번호 재설정 지침을 이메일로 확인하세요");
		m.put("Please choose revision to create branch from", "브랜치를 생성할 리비전을 선택해주세요");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "먼저 <a wicket:id=\"mailSetting\">메일 설정</a>을 구성해주세요");
		m.put("Please confirm", "확인해주세요");
		m.put("Please confirm the password.", "비밀번호를 확인해주세요.");
		m.put("Please follow <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">this instruction</a> to resolve the conflicts", 
			"충돌을 해결하려면 <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">이 지침</a>을 따르세요");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"이중 인증을 활성화할 때 저장한 복구 코드 중 하나를 입력해주세요");
		m.put("Please login to perform this operation", "이 작업을 수행하려면 로그인해주세요");
		m.put("Please login to perform this query", "이 쿼리를 수행하려면 로그인해주세요");
		m.put("Please resolve undefined field values below", "아래 정의되지 않은 필드 값을 해결해주세요");
		m.put("Please resolve undefined fields below", "아래 정의되지 않은 필드를 해결해주세요");
		m.put("Please resolve undefined states below. Note that if you select to delete an undefined state, all issues with that state will be deleted", 
			"아래 정의되지 않은 상태를 해결해주세요. 정의되지 않은 상태를 삭제하도록 선택하면 해당 상태를 가진 모든 이슈가 삭제됩니다");
		m.put("Please select agents to pause", "일시 중지할 에이전트를 선택해주세요");
		m.put("Please select agents to remove", "제거할 에이전트를 선택해주세요");
		m.put("Please select agents to restart", "재시작할 에이전트를 선택해주세요");
		m.put("Please select agents to resume", "재개할 에이전트를 선택해주세요");
		m.put("Please select branches to create pull request", "풀 리퀘스트를 생성할 브랜치를 선택해주세요");
		m.put("Please select builds to cancel", "취소할 빌드를 선택해주세요");
		m.put("Please select builds to delete", "삭제할 빌드를 선택해주세요");
		m.put("Please select builds to re-run", "다시 실행할 빌드를 선택해주세요");
		m.put("Please select comments to delete", "삭제할 댓글을 선택해주세요");
		m.put("Please select comments to set resolved", "해결된 상태로 설정할 댓글을 선택해주세요");
		m.put("Please select comments to set unresolved", "미해결 상태로 설정할 댓글을 선택해주세요");
		m.put("Please select different branches", "다른 브랜치를 선택해주세요");
		m.put("Please select fields to update", "업데이트할 필드를 선택해주세요");
		m.put("Please select groups to remove from", "제거할 그룹을 선택하세요");
		m.put("Please select issues to copy", "복사할 이슈를 선택해주세요");
		m.put("Please select issues to delete", "삭제할 이슈를 선택해주세요");
		m.put("Please select issues to edit", "편집할 이슈를 선택해주세요");
		m.put("Please select issues to move", "이동할 이슈를 선택해주세요");
		m.put("Please select issues to sync estimated/spent time", "예상/소요 시간을 동기화할 이슈를 선택해주세요");
		m.put("Please select packages to delete", "삭제할 패키지를 선택해주세요");
		m.put("Please select projects to delete", "삭제할 프로젝트를 선택해주세요");
		m.put("Please select projects to modify", "수정할 프로젝트를 선택해주세요");
		m.put("Please select projects to move", "이동할 프로젝트를 선택해주세요");
		m.put("Please select pull requests to delete", "삭제할 풀 리퀘스트를 선택해주세요");
		m.put("Please select pull requests to discard", "폐기할 풀 리퀘스트를 선택해주세요");
		m.put("Please select pull requests to watch/unwatch", "관찰/관찰 해제할 풀 리퀘스트를 선택해주세요");
		m.put("Please select query watches to delete", "삭제할 쿼리 관찰을 선택해주세요");
		m.put("Please select revision to create tag from", "태그를 생성할 리비전을 선택해주세요");
		m.put("Please select revisions to compare", "비교할 리비전을 선택해주세요");
		m.put("Please select users to convert to service accounts", "서비스 계정으로 변환할 사용자를 선택하세요");
		m.put("Please select users to disable", "비활성화할 사용자를 선택해주세요");
		m.put("Please select users to enable", "활성화할 사용자를 선택해주세요");
		m.put("Please select users to remove from group", "그룹에서 제거할 사용자를 선택하세요");
		m.put("Please specify file name above before editing content", "내용을 편집하기 전에 위에 파일 이름을 지정해주세요");
		m.put("Please switch to packages page of a particular project for the instructions", "특정 프로젝트의 패키지 페이지로 이동하여 지침을 확인해주세요");
		m.put("Please wait...", "잠시 기다려주세요...");
		m.put("Please waiting...", "기다려주세요...");
		m.put("Plugin metadata not found", "플러그인 메타데이터를 찾을 수 없습니다");
		m.put("Poll Interval", "폴링 간격");
		m.put("Populate Tag Mappings", "태그 매핑 채우기");
		m.put("Port", "포트");
		m.put("Post", "게시");
		m.put("Post Build Action", "빌드 후 작업");
		m.put("Post Build Action Bean", "빌드 후 작업 빈");
		m.put("Post Build Actions", "빌드 후 작업들");
		m.put("Post Url", "게시 URL");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "접두사 패턴");
		m.put("Prefix the title with <code>WIP</code> or <code>[WIP]</code> to mark the pull request as work in progress", 
			"제목에 <code>WIP</code> 또는 <code>[WIP]</code>를 접두사로 추가하여 풀 리퀘스트를 진행 중인 작업으로 표시하십시오");
		m.put("Prepend", "앞에 추가");
		m.put("Preserve Days", "보존 일수");
		m.put("Preset Commit Message", "미리 설정된 커밋 메시지");
		m.put("Preset commit message updated", "미리 설정된 커밋 메시지가 업데이트되었습니다");
		m.put("Press 'y' to get permalink", "'y'를 눌러 영구 링크를 가져오세요");
		m.put("Prev", "이전");
		m.put("Prevent Creation", "생성을 방지");
		m.put("Prevent Deletion", "삭제를 방지");
		m.put("Prevent Forced Push", "강제 푸시를 방지");
		m.put("Prevent Update", "업데이트를 방지");
		m.put("Preview", "미리보기");
		m.put("Previous", "이전");
		m.put("Previous Value", "이전 값");
		m.put("Previous commit", "이전 커밋");
		m.put("Previous {0}", "이전 {0}");
		m.put("Primary", "기본");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "기본 <a wicket:id=\"noPrimaryAddressLink\">이메일 주소</a>가 지정되지 않았습니다");
		m.put("Primary Email", "기본 이메일");
		m.put("Primary email address not specified", "기본 이메일 주소가 지정되지 않았습니다");
		m.put("Primary email address of your account is not specified yet", "계정의 기본 이메일 주소가 아직 지정되지 않았습니다");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"기본 이메일 주소는 알림을 받거나, gravatar를 표시하는 데 사용됩니다 (활성화된 경우)");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"위 계정의 기본 또는 별칭 이메일 주소는 다양한 이메일 알림의 발신 주소로 사용됩니다. 아래에서 <code>수신 이메일 확인</code> 옵션이 활성화된 경우, 사용자는 이 주소로 문제 또는 풀 리퀘스트 댓글을 이메일로 답변할 수 있습니다");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"Office 365 메일 서버에 로그인하여 이메일을 송수신하기 위한 계정의 주 이름입니다. 이 계정이 위의 애플리케이션 ID로 표시된 등록된 애플리케이션을 <b>소유</b>하고 있는지 확인하세요");
		m.put("Private Key Secret", "개인 키 비밀");
		m.put("Private key regenerated and SSH server restarted", "개인 키가 재생성되고 SSH 서버가 재시작되었습니다");
		m.put("Privilege", "권한");
		m.put("Privilege Settings", "권한 설정");
		m.put("Product Version", "제품 버전");
		m.put("Profile", "프로필");
		m.put("Programming language", "프로그래밍 언어");
		m.put("Project", "프로젝트");
		m.put("Project \"{0}\" deleted", "프로젝트 \"{0}\"가 삭제되었습니다");
		m.put("Project Authorization Bean", "프로젝트 권한 빈");
		m.put("Project Authorizations Bean", "프로젝트 권한 빈들");
		m.put("Project Build Setting", "프로젝트 빌드 설정");
		m.put("Project Dependencies", "프로젝트 종속성");
		m.put("Project Dependency", "프로젝트 종속 항목");
		m.put("Project Id", "프로젝트 ID");
		m.put("Project Import Option", "프로젝트 가져오기 옵션");
		m.put("Project Issue Setting", "프로젝트 문제 설정");
		m.put("Project Key", "프로젝트 키");
		m.put("Project Management", "프로젝트 관리");
		m.put("Project Pack Setting", "프로젝트 팩 설정");
		m.put("Project Path", "프로젝트 경로");
		m.put("Project Pull Request Setting", "프로젝트 풀 리퀘스트 설정");
		m.put("Project Replicas", "프로젝트 복제본");
		m.put("Project authorizations updated", "프로젝트 권한이 업데이트되었습니다");
		m.put("Project does not have any code yet", "프로젝트에 아직 코드가 없습니다");
		m.put("Project forked", "프로젝트가 포크되었습니다");
		m.put("Project id", "프로젝트 ID");
		m.put("Project list", "프로젝트 목록");
		m.put("Project manage privilege required to delete \"{0}\"", "\"{0}\"를 삭제하려면 프로젝트 관리 권한이 필요합니다");
		m.put("Project manage privilege required to modify \"{0}\"", "\"{0}\"를 수정하려면 프로젝트 관리 권한이 필요합니다");
		m.put("Project manage privilege required to move \"{0}\"", "\"{0}\"를 이동하려면 프로젝트 관리 권한이 필요합니다");
		m.put("Project name", "프로젝트 이름");
		m.put("Project not specified yet", "프로젝트가 아직 지정되지 않았습니다");
		m.put("Project or revision not specified yet", "프로젝트 또는 리비전이 아직 지정되지 않았습니다");
		m.put("Project overview", "프로젝트 개요");
		m.put("Project path", "프로젝트 경로");
		m.put("Projects", "프로젝트들");
		m.put("Projects Bean", "프로젝트 빈들");
		m.put("Projects deleted", "프로젝트들이 삭제되었습니다");
		m.put("Projects modified", "프로젝트들이 수정되었습니다");
		m.put("Projects moved", "프로젝트들이 이동되었습니다");
		m.put("Projects need to be redistributed when cluster members are added/removed. OneDev does not do this automatically as this is resource intensive, and you may only want to do it after cluster is finalized and stable.", 
			"클러스터 멤버가 추가/제거될 때 프로젝트를 재배포해야 합니다. OneDev는 이 작업을 자동으로 수행하지 않으며, 이는 리소스를 많이 소모하므로 클러스터가 최종화되고 안정화된 후에만 수행하는 것이 좋습니다");
		m.put("Promotions", "프로모션");
		m.put("Prompt Fields", "프롬프트 필드");
		m.put("Properties", "속성");
		m.put("Provide server id (guild id) to restrict access only to server members", "서버 멤버만 접근할 수 있도록 서버 ID(길드 ID)를 제공하세요");
		m.put("Proxy", "프록시");
		m.put("Prune Builder Cache", "빌더 캐시 정리");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"도커 buildx 빌더의 이미지 캐시를 정리합니다. 이 단계는 서버 도커 실행자 또는 원격 도커 실행자에서 지정된 buildx 빌더의 캐시를 제거하기 위해 도커 빌더 정리 명령을 호출합니다");
		m.put("Public", "공개");
		m.put("Public Key", "공개 키");
		m.put("Public Roles", "공개 역할");
		m.put("Publish", "게시");
		m.put("Publish Coverage Report Step", "커버리지 보고서 게시 단계");
		m.put("Publish Problem Report Step", "문제 보고서 게시 단계");
		m.put("Publish Report Step", "보고서 게시 단계");
		m.put("Publish Unit Test Report Step", "단위 테스트 보고서 게시 단계");
		m.put("Published After", "게시 후");
		m.put("Published At", "게시 위치");
		m.put("Published Before", "게시 전");
		m.put("Published By", "게시자");
		m.put("Published By Project", "프로젝트에 의해 게시됨");
		m.put("Published By User", "사용자에 의해 게시됨");
		m.put("Published File", "게시된 파일");
		m.put("Pull Command", "풀 명령");
		m.put("Pull Image", "이미지 풀");
		m.put("Pull Request", "풀 리퀘스트");
		m.put("Pull Request Branches", "풀 리퀘스트 브랜치");
		m.put("Pull Request Description", "풀 리퀘스트 설명");
		m.put("Pull Request Filter", "풀 리퀘스트 필터");
		m.put("Pull Request Management", "풀 리퀘스트 관리");
		m.put("Pull Request Markdown Report", "풀 리퀘스트 마크다운 보고서");
		m.put("Pull Request Notification", "풀 리퀘스트 알림");
		m.put("Pull Request Notification Template", "풀 리퀘스트 알림 템플릿");
		m.put("Pull Request Notification Unsubscribed", "풀 리퀘스트 알림 구독 취소됨");
		m.put("Pull Request Notification Unsubscribed Template", "풀 리퀘스트 알림 구독 취소 템플릿");
		m.put("Pull Request Settings", "풀 리퀘스트 설정");
		m.put("Pull Request Statistics", "풀 리퀘스트 통계");
		m.put("Pull Request Title", "풀 리퀘스트 제목");
		m.put("Pull Requests", "풀 리퀘스트들");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"crane을 통해 도커 이미지를 OCI 레이아웃으로 풀합니다. 이 단계는 서버 도커 실행자, 원격 도커 실행자 또는 Kubernetes 실행자에 의해 실행되어야 합니다");
		m.put("Pull from Remote", "원격에서 풀");
		m.put("Pull request", "풀 리퀘스트");
		m.put("Pull request #{0} already closed", "풀 리퀘스트 #{0}가 이미 닫혔습니다");
		m.put("Pull request #{0} deleted", "풀 리퀘스트 #{0}가 삭제되었습니다");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"프로젝트 내에서 풀 리퀘스트 관리 권한, 여러 풀 리퀘스트에 대한 배치 작업 포함");
		m.put("Pull request already closed", "풀 리퀘스트가 이미 닫혔습니다");
		m.put("Pull request already opened", "풀 리퀘스트가 이미 열렸습니다");
		m.put("Pull request and code review", "풀 리퀘스트 및 코드 리뷰");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not finished yet", 
			"<a class=\"more-info d-inline link-primary\">필요한 빌드</a>가 아직 완료되지 않아 풀 리퀘스트를 지금 병합할 수 없습니다");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not successful", 
			"<a class=\"more-info d-inline link-primary\">필요한 빌드</a>가 성공하지 않아 풀 리퀘스트를 지금 병합할 수 없습니다");
		m.put("Pull request can not be merged now as it is <a class=\"more-info d-inline link-primary\">pending review</a>", 
			"풀 리퀘스트를 지금 병합할 수 없습니다. <a class=\"more-info d-inline link-primary\">검토 대기 중</a>입니다");
		m.put("Pull request can not be merged now as it was <a class=\"more-info d-inline link-primary\">requested for changes</a>", 
			"풀 리퀘스트를 지금 병합할 수 없습니다. <a class=\"more-info d-inline link-primary\">변경 요청됨</a>입니다");
		m.put("Pull request can not be merged now as valid signature is required for head commit", 
			"풀 리퀘스트를 지금 병합할 수 없습니다. 헤드 커밋에 유효한 서명이 필요합니다");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "모든 리뷰어의 승인을 받은 후에만 풀 리퀘스트를 병합할 수 있습니다");
		m.put("Pull request can only be merged by users with code write permission", "코드 쓰기 권한이 있는 사용자만 풀 리퀘스트를 병합할 수 있습니다");
		m.put("Pull request discard", "풀 리퀘스트 폐기");
		m.put("Pull request duration statistics", "풀 리퀘스트 지속 시간 통계");
		m.put("Pull request frequency statistics", "풀 리퀘스트 빈도 통계");
		m.put("Pull request is discarded", "풀 리퀘스트가 폐기됨");
		m.put("Pull request is in error: {0}", "풀 리퀘스트 오류 발생: {0}");
		m.put("Pull request is merged", "풀 리퀘스트가 병합됨");
		m.put("Pull request is opened", "풀 리퀘스트가 열림");
		m.put("Pull request is still a work in progress", "풀 리퀘스트가 아직 진행 중입니다");
		m.put("Pull request is work in progress", "풀 리퀘스트가 진행 중입니다");
		m.put("Pull request list", "풀 리퀘스트 목록");
		m.put("Pull request merge", "풀 리퀘스트 병합");
		m.put("Pull request not exist or access denied", "풀 리퀘스트가 존재하지 않거나 접근이 거부됨");
		m.put("Pull request not merged", "풀 리퀘스트가 병합되지 않음");
		m.put("Pull request number", "풀 리퀘스트 번호");
		m.put("Pull request open or update", "풀 리퀘스트 열기 또는 업데이트");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"풀 리퀘스트 쿼리 감시는 새로운 풀 리퀘스트에만 영향을 미칩니다. 기존 풀 리퀘스트의 감시 상태를 일괄적으로 관리하려면 풀 리퀘스트 페이지에서 감시 상태로 필터링한 후 적절한 작업을 수행하세요");
		m.put("Pull request settings updated", "풀 리퀘스트 설정이 업데이트됨");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"풀 리퀘스트 통계는 엔터프라이즈 기능입니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>을 이용해보세요");
		m.put("Pull request synchronization submitted", "풀 리퀘스트 동기화 제출됨");
		m.put("Pull request will be merged automatically when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"풀 리퀘스트는 준비되면 자동으로 병합됩니다. 새로운 커밋 추가, 병합 전략 변경, 또는 대상 브랜치 전환 시 이 옵션은 비활성화됩니다");
		m.put("Pull request will be merged automatically with a preset <a wicket:id=\"commitMessage\">commit message</a> when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"풀 리퀘스트는 준비되면 미리 설정된 <a wicket:id=\"commitMessage\">커밋 메시지</a>로 자동 병합됩니다. 새로운 커밋 추가, 병합 전략 변경, 또는 대상 브랜치 전환 시 이 옵션은 비활성화됩니다");
		m.put("Push Image", "이미지 푸시");
		m.put("Push chart to the repository", "차트를 저장소에 푸시");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Crane을 통해 OCI 레이아웃에서 도커 이미지를 푸시합니다. 이 단계는 서버 도커 실행기, 원격 도커 실행기, 또는 Kubernetes 실행기에 의해 실행되어야 합니다");
		m.put("Push to Remote", "원격으로 푸시");
		m.put("Push to container registry", "컨테이너 레지스트리에 푸시");
		m.put("PyPI(s)", "PyPI(s)");
		m.put("Pylint Report", "Pylint 보고서");
		m.put("Queries", "쿼리들");
		m.put("Query", "쿼리");
		m.put("Query Parameters", "쿼리 매개변수");
		m.put("Query Watches", "쿼리 감시");
		m.put("Query commits", "쿼리 커밋");
		m.put("Query not submitted", "쿼리가 제출되지 않음");
		m.put("Query param", "쿼리 매개변수");
		m.put("Query/order agents", "에이전트 쿼리/정렬");
		m.put("Query/order builds", "빌드 쿼리/정렬");
		m.put("Query/order comments", "댓글 쿼리/정렬");
		m.put("Query/order issues", "이슈 쿼리/정렬");
		m.put("Query/order packages", "패키지 쿼리/정렬");
		m.put("Query/order projects", "프로젝트 쿼리/정렬");
		m.put("Query/order pull requests", "풀 리퀘스트 쿼리/정렬");
		m.put("Queueing Takes", "대기 시간");
		m.put("Quick Search", "빠른 검색");
		m.put("Quote", "인용");
		m.put("RESTful API", "RESTful API");
		m.put("RESTful API Help", "RESTful API 도움말");
		m.put("Ran On Agent", "에이전트에서 실행됨");
		m.put("Re-run All Queried Builds", "쿼리된 모든 빌드 다시 실행");
		m.put("Re-run Selected Builds", "선택된 빌드 다시 실행");
		m.put("Re-run request submitted", "다시 실행 요청 제출됨");
		m.put("Re-run this build", "이 빌드 다시 실행");
		m.put("Read", "읽기");
		m.put("Read body", "본문 읽기");
		m.put("Readiness Check Command", "준비 상태 확인 명령");
		m.put("Really want to delete this code comment?", "정말로 이 코드 댓글을 삭제하시겠습니까?");
		m.put("Rebase", "리베이스");
		m.put("Rebase Source Branch Commits", "소스 브랜치 커밋 리베이스");
		m.put("Rebase all commits from source branch onto target branch", "소스 브랜치의 모든 커밋을 대상 브랜치에 리베이스");
		m.put("Rebase source branch commits", "소스 브랜치 커밋 리베이스");
		m.put("Rebuild manually", "수동으로 재빌드");
		m.put("Receive Posted Email", "게시된 이메일 수신");
		m.put("Received test mail", "테스트 메일 수신됨");
		m.put("Receivers", "수신자들");
		m.put("Recovery code", "복구 코드");
		m.put("Recursive", "재귀적");
		m.put("Redundant", "중복된");
		m.put("Ref", "참조");
		m.put("Ref Name", "참조 이름");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup", 
			"예제 설정은 이 <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>튜토리얼</a>을 참조하세요");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> for an example setup", 
			"예제 설정은 이 <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>튜토리얼</a>을 참조하세요");
		m.put("Reference", "참조");
		m.put("Reference Build", "참조 빌드");
		m.put("Reference Issue", "참조 이슈");
		m.put("Reference Pull Request", "참조 풀 리퀘스트");
		m.put("Reference this {0} in markdown or commit message via below string.", "아래 문자열을 사용하여 마크다운 또는 커밋 메시지에서 이 {0}을 참조하세요.");
		m.put("Refresh", "새로 고침");
		m.put("Refresh Token", "새로 고침 토큰");
		m.put("Refs", "참조들");
		m.put("Regenerate", "재생성");
		m.put("Regenerate Private Key", "개인 키 재생성");
		m.put("Regenerate this access token", "이 액세스 토큰 재생성");
		m.put("Registry Login", "레지스트리 로그인");
		m.put("Registry Logins", "레지스트리 로그인들");
		m.put("Registry Url", "레지스트리 URL");
		m.put("Regular Expression", "정규 표현식");
		m.put("Remaining User Months", "남은 사용자 월수");
		m.put("Remaining User Months:", "남은 사용자 월수:");
		m.put("Remaining time", "남은 시간");
		m.put("Remember Me", "날 기억해줘");
		m.put("Remote Docker Executor", "원격 도커 실행기");
		m.put("Remote Machine", "원격 머신");
		m.put("Remote Shell Executor", "원격 셸 실행기");
		m.put("Remote URL", "원격 URL");
		m.put("Remote Url", "원격 URL");
		m.put("Remove", "제거");
		m.put("Remove All Queried Agents", "쿼리된 모든 에이전트 제거");
		m.put("Remove All Queried Users from Group", "그룹에서 조회된 모든 사용자 제거");
		m.put("Remove Fields", "필드 제거");
		m.put("Remove From Current Iteration", "현재 반복에서 제거");
		m.put("Remove Selected Agents", "선택된 에이전트 제거");
		m.put("Remove Selected Users from Group", "그룹에서 선택된 사용자 제거");
		m.put("Remove from All Queried Groups", "조회된 모든 그룹에서 제거");
		m.put("Remove from Selected Groups", "선택된 그룹에서 제거");
		m.put("Remove from batch", "배치에서 제거");
		m.put("Remove issue from this iteration", "이 반복에서 이슈 제거");
		m.put("Remove this assignee", "이 담당자 제거");
		m.put("Remove this external participant from issue", "이슈에서 이 외부 참가자 제거");
		m.put("Remove this file", "이 파일 제거");
		m.put("Remove this image", "이 이미지를 제거하십시오");
		m.put("Remove this reviewer", "이 리뷰어를 제거하십시오");
		m.put("Removed all queried agents. Type <code>yes</code> below to confirm", "모든 조회된 에이전트를 제거했습니다. 아래에 <code>yes</code>를 입력하여 확인하십시오");
		m.put("Removed selected agents. Type <code>yes</code> below to confirm", "선택된 에이전트를 제거했습니다. 아래에 <code>yes</code>를 입력하여 확인하십시오");
		m.put("Rename {0}", "{0} 이름 변경");
		m.put("Renew Subscription", "구독 갱신");
		m.put("Renovate CLI Options", "Renovate CLI 옵션");
		m.put("Renovate JavaScript Config", "Renovate JavaScript 설정");
		m.put("Reopen", "다시 열기");
		m.put("Reopen this iteration", "이 반복을 다시 열기");
		m.put("Reopened pull request \"{0}\" ({1})", "풀 리퀘스트 \"{0}\" ({1})를 다시 열었습니다");
		m.put("Replace With", "교체");
		m.put("Replica Count", "복제 수");
		m.put("Replicas", "복제본");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "프로젝트 \"{1}\"의 파일 \"{0}\"에 대한 댓글에 답변했습니다");
		m.put("Reply", "답변");
		m.put("Report Name", "보고서 이름");
		m.put("Report format changed. You may re-run this build to generate the report in new format", 
			"보고서 형식이 변경되었습니다. 새 형식으로 보고서를 생성하려면 이 빌드를 다시 실행하십시오");
		m.put("Repository Sync", "저장소 동기화");
		m.put("Request Body", "요청 본문");
		m.put("Request For Changes", "변경 요청");
		m.put("Request Scopes", "요청 범위");
		m.put("Request Trial Subscription", "체험 구독 요청");
		m.put("Request review", "리뷰 요청");
		m.put("Request to sync", "동기화 요청");
		m.put("Requested For changes", "변경 요청됨");
		m.put("Requested changes to pull request \"{0}\" ({1})", "풀 리퀘스트 \"{0}\" ({1})에 대한 변경 요청");
		m.put("Requested for changes", "변경 요청됨");
		m.put("Requested to sync estimated/spent time", "예상/소요 시간을 동기화하도록 요청됨");
		m.put("Require Autentication", "인증 필요");
		m.put("Require Strict Pull Request Builds", "엄격한 풀 리퀘스트 빌드 필요");
		m.put("Require Successful", "성공 필요");
		m.put("Required", "필수");
		m.put("Required Builds", "필수 빌드");
		m.put("Required Reviewers", "필수 리뷰어");
		m.put("Required Services", "필수 서비스");
		m.put("Resend Verification Email", "확인 이메일 다시 보내기");
		m.put("Resend invitation", "초대 다시 보내기");
		m.put("Reset", "재설정");
		m.put("Resolution", "해결");
		m.put("Resolved", "해결됨");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "프로젝트 \"{1}\"의 파일 \"{0}\"에 대한 댓글을 해결했습니다");
		m.put("Resource", "리소스");
		m.put("Resource Settings", "리소스 설정");
		m.put("Resources", "리소스들");
		m.put("Response", "응답");
		m.put("Response Body", "응답 본문");
		m.put("Restart", "재시작");
		m.put("Restart All Queried Agents", "모든 조회된 에이전트 재시작");
		m.put("Restart Selected Agents", "선택된 에이전트 재시작");
		m.put("Restart command issued", "재시작 명령 발행됨");
		m.put("Restart command issued to all queried agents", "모든 조회된 에이전트에 재시작 명령 발행됨");
		m.put("Restart command issued to selected agents", "선택된 에이전트에 재시작 명령 발행됨");
		m.put("Restore Source Branch", "소스 브랜치 복원");
		m.put("Restored source branch", "소스 브랜치 복원됨");
		m.put("Resubmitted manually", "수동으로 다시 제출됨");
		m.put("Resume", "재개");
		m.put("Resume All Queried Agents", "모든 조회된 에이전트 재개");
		m.put("Resume Selected Agents", "선택된 에이전트 재개");
		m.put("Resumed all queried agents", "모든 조회된 에이전트 재개됨");
		m.put("Resumed selected agents", "선택된 에이전트 재개됨");
		m.put("Retried At", "재시도 시점");
		m.put("Retrieve Groups", "그룹 검색");
		m.put("Retrieve LFS Files", "LFS 파일 검색");
		m.put("Retrieve Submodules", "서브모듈 검색");
		m.put("Retry Condition", "재시도 조건");
		m.put("Retry Delay", "재시도 지연");
		m.put("Revert", "되돌리기");
		m.put("Reverted successfully", "성공적으로 되돌림");
		m.put("Review required for deletion. Submit pull request instead", "삭제를 위해 리뷰가 필요합니다. 대신 풀 리퀘스트를 제출하십시오");
		m.put("Review required for this change. Please submit pull request instead", "이 변경 사항에는 검토가 필요합니다. 대신 풀 리퀘스트를 제출하세요.");
		m.put("Review required for this change. Submit pull request instead", "이 변경을 위해 리뷰가 필요합니다. 대신 풀 리퀘스트를 제출하십시오");
		m.put("Reviewers", "리뷰어");
		m.put("Revision", "개정");
		m.put("Revision indexing in progress...", "개정 색인 생성 중...");
		m.put("Revision indexing in progress... (symbol navigation in revisions will be accurate after indexed)", 
			"개정 색인 생성 중... (개정의 기호 탐색은 색인 생성 후 정확해집니다)");
		m.put("Right", "오른쪽");
		m.put("Role", "역할");
		m.put("Role \"{0}\" deleted", "역할 \"{0}\" 삭제됨");
		m.put("Role \"{0}\" updated", "역할 \"{0}\" 업데이트됨");
		m.put("Role Management", "역할 관리");
		m.put("Role created", "역할 생성됨");
		m.put("Roles", "역할");
		m.put("Root Projects", "루트 프로젝트");
		m.put("Roslynator Report", "Roslynator 보고서");
		m.put("RubyGems(s)", "RubyGems(s)");
		m.put("Ruff Report", "Ruff 보고서");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "태그를 운영하는 사용자가 여기에서 지정된 기준과 일치하는 경우 규칙이 적용됩니다");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"브랜치를 변경하는 사용자가 여기에서 지정된 기준과 일치하는 경우에만 규칙이 적용됩니다");
		m.put("Run As", "다음으로 실행");
		m.put("Run Buildx Image Tools", "Buildx 이미지 도구 실행");
		m.put("Run Docker Container", "Docker 컨테이너 실행");
		m.put("Run In Container", "컨테이너에서 실행");
		m.put("Run Integrity Check", "무결성 검사 실행");
		m.put("Run Job", "작업 실행");
		m.put("Run Options", "실행 옵션");
		m.put("Run below commands from within your git repository:", "Git 저장소 내에서 아래 명령을 실행하십시오:");
		m.put("Run below commands to install this gem", "이 gem을 설치하려면 아래 명령을 실행하십시오");
		m.put("Run below commands to install this package", "이 패키지를 설치하려면 아래 명령을 실행하십시오");
		m.put("Run below commands to use this chart", "이 차트를 사용하려면 아래 명령을 실행하십시오");
		m.put("Run below commands to use this package", "이 패키지를 사용하려면 아래 명령을 실행하십시오");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"지정된 인수를 사용하여 docker buildx imagetools 명령을 실행하십시오. 이 단계는 서버 docker 실행기 또는 원격 docker 실행기에서만 실행할 수 있습니다");
		m.put("Run job", "작업 실행");
		m.put("Run job in another project", "다른 프로젝트에서 작업 실행");
		m.put("Run on Bare Metal/Virtual Machine", "베어 메탈/가상 머신에서 실행");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"다양한 <a href='https://deps.dev/' target='_blank'>종속성</a>에서 사용된 위반된 라이센스를 스캔하기 위해 osv 스캐너를 실행하십시오. 이는 Docker를 인식하는 실행기에서만 실행할 수 있습니다.");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"다양한 <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>잠금 파일</a>의 취약점을 스캔하기 위해 osv 스캐너를 실행하십시오. 이는 Docker를 인식하는 실행기에서만 실행할 수 있습니다.");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"지정된 Docker 컨테이너를 실행하십시오. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 워크스페이스</a>가 컨테이너에 마운트되고 해당 경로가 환경 변수 <code>ONEDEV_WORKSPACE</code>에 배치됩니다. <b class='text-warning'>참고: </b> 이 단계는 서버 Docker 실행기 또는 원격 Docker 실행기에서만 실행할 수 있습니다");
		m.put("Run specified step template", "지정된 단계 템플릿 실행");
		m.put("Run this job", "이 작업 실행");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by docker aware executor.", 
			"지정된 이미지에서 문제를 찾기 위해 trivy 컨테이너 이미지 스캐너를 실행합니다. 취약점에 대해서는 다양한 <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>배포 파일</a>을 확인합니다. 이는 도커 인식 실행자에 의해서만 실행될 수 있습니다.");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"trivy 파일 시스템 스캐너를 실행하여 다양한 <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>잠금 파일</a>을 스캔합니다. Docker를 인식하는 실행자만 실행할 수 있으며, <span class='text-warning'>종속성이 해결된 후</span>(npm install 등) 실행하는 것이 권장됩니다. OSV 스캐너와 비교하여 설정이 다소 복잡하지만 더 정확한 결과를 제공할 수 있습니다.");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"trivy rootfs 스캐너를 실행하여 다양한 <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>배포 파일</a>을 스캔합니다. Docker를 인식하는 실행자만 실행할 수 있으며 프로젝트의 스테이징 영역에 대해 실행하는 것이 권장됩니다.");
		m.put("Run via Docker Container", "Docker 컨테이너를 통해 실행");
		m.put("Running", "실행 중");
		m.put("Running Takes", "실행 소요 시간");
		m.put("SLOC on {0}", "{0}의 SLOC");
		m.put("SMTP Host", "SMTP 호스트");
		m.put("SMTP Password", "SMTP 비밀번호");
		m.put("SMTP User", "SMTP 사용자");
		m.put("SMTP/IMAP", "SMTP/IMAP");
		m.put("SSH", "SSH");
		m.put("SSH & GPG Keys", "SSH 및 GPG 키");
		m.put("SSH Clone URL", "SSH 클론 URL");
		m.put("SSH Keys", "SSH 키");
		m.put("SSH Root URL", "SSH 루트 URL");
		m.put("SSH Server Key", "SSH 서버 키");
		m.put("SSH key deleted", "SSH 키가 삭제되었습니다");
		m.put("SSH settings have been saved and SSH server restarted", "SSH 설정이 저장되었으며 SSH 서버가 재시작되었습니다");
		m.put("SSL Setting", "SSL 설정");
		m.put("SSO Accounts", "SSO 계정");
		m.put("SSO Providers", "SSO 제공자");
		m.put("SSO account deleted", "SSO 계정이 삭제되었습니다");
		m.put("SSO provider \"{0}\" deleted", "SSO 제공자 \"{0}\"가 삭제되었습니다");
		m.put("SSO provider created", "SSO 제공자가 생성되었습니다");
		m.put("SSO provider updated", "SSO 제공자가 업데이트되었습니다");
		m.put("SUCCESSFUL", "성공");
		m.put("Save", "저장");
		m.put("Save Query", "쿼리 저장");
		m.put("Save Query Bean", "쿼리 빈 저장");
		m.put("Save Settings", "설정 저장");
		m.put("Save Settings & Redistribute Projects", "설정 저장 및 프로젝트 재배포");
		m.put("Save Template", "템플릿 저장");
		m.put("Save as Mine", "내 것으로 저장");
		m.put("Saved Queries", "저장된 쿼리");
		m.put("Scan Path", "스캔 경로");
		m.put("Scan Paths", "스캔 경로들");
		m.put("Scan below QR code with your TOTP authenticators", "아래 QR 코드를 TOTP 인증기로 스캔하세요");
		m.put("Schedule Issues", "이슈 일정");
		m.put("Script Name", "스크립트 이름");
		m.put("Scripting Value", "스크립팅 값");
		m.put("Search", "검색");
		m.put("Search For", "검색 대상");
		m.put("Search Groups Using Filter", "필터를 사용하여 그룹 검색");
		m.put("Search branch", "브랜치 검색");
		m.put("Search files, symbols and texts", "파일, 심볼 및 텍스트 검색");
		m.put("Search for", "검색 대상");
		m.put("Search inside current tree", "현재 트리 내에서 검색");
		m.put("Search is too general", "검색이 너무 일반적입니다");
		m.put("Search job", "작업 검색");
		m.put("Search project", "프로젝트 검색");
		m.put("Secret", "비밀");
		m.put("Secret Config File", "비밀 구성 파일");
		m.put("Secret Setting", "비밀 설정");
		m.put("Security", "보안");
		m.put("Security & Compliance", "보안 및 컴플라이언스");
		m.put("Security Setting", "보안 설정");
		m.put("Security Settings", "보안 설정들");
		m.put("Security settings have been updated", "보안 설정이 업데이트되었습니다");
		m.put("Select", "선택");
		m.put("Select Branch to Cherry Pick to", "체리픽할 브랜치 선택");
		m.put("Select Branch to Revert on", "되돌릴 브랜치 선택");
		m.put("Select Branch/Tag", "브랜치/태그 선택");
		m.put("Select Existing", "기존 항목 선택");
		m.put("Select Job", "작업 선택");
		m.put("Select Project", "프로젝트 선택");
		m.put("Select below...", "아래 선택...");
		m.put("Select iteration to schedule issues into", "이슈를 일정에 추가할 반복 선택");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"가져올 조직 선택. 현재 계정의 저장소에서 가져오려면 비워 두세요");
		m.put("Select project and revision first", "먼저 프로젝트와 리비전을 선택하세요");
		m.put("Select project first", "먼저 프로젝트를 선택하세요");
		m.put("Select project to import from", "가져올 프로젝트 선택");
		m.put("Select project to sync to. Leave empty to sync to current project", "동기화할 프로젝트 선택. 현재 프로젝트와 동기화하려면 비워 두세요");
		m.put("Select repository to import from", "가져올 저장소 선택");
		m.put("Select users to send alert email upon events such as database auto-backup failure, cluster node unreachable etc", 
			"데이터베이스 자동 백업 실패, 클러스터 노드 연결 불가 등의 이벤트 발생 시 경고 이메일을 보낼 사용자 선택");
		m.put("Select workspace to import from", "가져올 작업 공간 선택");
		m.put("Send Notifications", "알림 보내기");
		m.put("Send Pull Request", "풀 리퀘스트 보내기");
		m.put("Send notification", "알림 보내기");
		m.put("SendGrid", "SendGrid");
		m.put("Sendgrid Webhook Setting", "SendGrid 웹훅 설정");
		m.put("Sending invitation to \"{0}\"...", "\"{0}\"에게 초대장을 보내는 중...");
		m.put("Sending test mail to {0}...", "{0}에게 테스트 메일을 보내는 중...");
		m.put("Sequential Group", "순차 그룹");
		m.put("Server", "서버");
		m.put("Server Docker Executor", "서버 Docker 실행자");
		m.put("Server Id", "서버 ID");
		m.put("Server Information", "서버 정보");
		m.put("Server Log", "서버 로그");
		m.put("Server Setup", "서버 설정");
		m.put("Server Shell Executor", "서버 쉘 실행자");
		m.put("Server URL", "서버 URL");
		m.put("Server fingerprint", "서버 지문");
		m.put("Server host", "서버 호스트");
		m.put("Server is Starting...", "서버 시작 중...");
		m.put("Server url", "서버 URL");
		m.put("Service", "서비스");
		m.put("Service Account", "서비스 계정");
		m.put("Service Desk", "서비스 데스크");
		m.put("Service Desk Email Address", "서비스 데스크 이메일 주소");
		m.put("Service Desk Issue Open Failed", "서비스 데스크 이슈 열기 실패");
		m.put("Service Desk Issue Open Failed Template", "서비스 데스크 이슈 열기 실패 템플릿");
		m.put("Service Desk Issue Opened", "서비스 데스크 이슈 열림");
		m.put("Service Desk Issue Opened Template", "서비스 데스크 이슈 열림 템플릿");
		m.put("Service Desk Setting", "서비스 데스크 설정");
		m.put("Service Desk Setting Holder", "서비스 데스크 설정 홀더");
		m.put("Service Desk Settings", "서비스 데스크 설정들");
		m.put("Service Locator", "서비스 로케이터");
		m.put("Service Locators", "서비스 로케이터들");
		m.put("Service account not allowed to login", "서비스 계정은 로그인할 수 없습니다");
		m.put("Service desk setting", "서비스 데스크 설정");
		m.put("Service desk settings have been saved", "서비스 데스크 설정이 저장되었습니다");
		m.put("Services", "서비스");
		m.put("Session Timeout", "세션 시간 초과");
		m.put("Set", "설정");
		m.put("Set All Queried As Root Projects", "쿼리된 모든 항목을 루트 프로젝트로 설정");
		m.put("Set All Queried Comments as Read", "쿼리된 모든 댓글을 읽음으로 설정");
		m.put("Set All Queried Comments as Resolved", "쿼리된 모든 댓글을 해결됨으로 설정");
		m.put("Set All Queried Comments as Unresolved", "쿼리된 모든 댓글을 미해결로 설정");
		m.put("Set All Queried Issues as Read", "쿼리된 모든 이슈를 읽음으로 설정");
		m.put("Set All Queried Pull Requests as Read", "쿼리된 모든 풀 리퀘스트를 읽음으로 설정");
		m.put("Set As Primary", "기본값으로 설정");
		m.put("Set Build Description", "빌드 설명 설정");
		m.put("Set Build Version", "빌드 버전 설정");
		m.put("Set Resolved", "해결됨으로 설정");
		m.put("Set Selected As Root Projects", "선택된 항목을 루트 프로젝트로 설정");
		m.put("Set Selected Comments as Resolved", "선택된 댓글을 해결됨으로 설정");
		m.put("Set Selected Comments as Unresolved", "선택된 댓글을 미해결로 설정");
		m.put("Set Unresolved", "미해결로 설정");
		m.put("Set Up Cache", "캐시 설정");
		m.put("Set Up Renovate Cache", "Renovate 캐시 설정");
		m.put("Set Up Trivy Cache", "Trivy 캐시 설정");
		m.put("Set Up Your Account", "계정을 설정하세요");
		m.put("Set as Private", "비공개로 설정");
		m.put("Set as Public", "공개로 설정");
		m.put("Set description", "설명 설정");
		m.put("Set reviewed", "검토됨으로 설정");
		m.put("Set unreviewed", "미검토로 설정");
		m.put("Set up Microsoft Teams notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url. ", 
			"Microsoft Teams 알림 설정을 구성합니다. 설정은 하위 프로젝트에 상속되며, 동일한 웹훅 URL로 설정을 정의하여 재정의할 수 있습니다.");
		m.put("Set up discord notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Discord 알림 설정을 구성합니다. 설정은 하위 프로젝트에 상속되며, 동일한 웹훅 URL로 설정을 정의하여 재정의할 수 있습니다.");
		m.put("Set up job cache to speed up job execution. Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> on how to use job cache", 
			"작업 캐시를 설정하여 작업 실행 속도를 높입니다. 작업 캐시 사용 방법은 <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>이 튜토리얼</a>을 확인하세요.");
		m.put("Set up ntfy.sh notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"ntfy.sh 알림 설정을 구성합니다. 설정은 하위 프로젝트에 상속되며, 동일한 웹훅 URL로 설정을 정의하여 재정의할 수 있습니다.");
		m.put("Set up slack notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Slack 알림 설정을 구성합니다. 설정은 하위 프로젝트에 상속되며, 동일한 웹훅 URL로 설정을 정의하여 재정의할 수 있습니다.");
		m.put("Set up two-factor authentication", "이중 인증 설정");
		m.put("Setting", "설정");
		m.put("Setting has been saved", "설정이 저장되었습니다");
		m.put("Settings", "설정");
		m.put("Settings and permissions of parent project will be inherited by this project", "상위 프로젝트의 설정과 권한이 이 프로젝트에 상속됩니다");
		m.put("Settings saved", "설정이 저장되었습니다");
		m.put("Settings saved and project redistribution scheduled", "설정이 저장되었으며 프로젝트 재분배가 예약되었습니다");
		m.put("Settings updated", "설정이 업데이트되었습니다");
		m.put("Share dashboard", "대시보드 공유");
		m.put("Share with Groups", "그룹과 공유");
		m.put("Share with Users", "사용자와 공유");
		m.put("Shell", "셸");
		m.put("Show Archived", "보관된 항목 보기");
		m.put("Show Branch/Tag", "브랜치/태그 보기");
		m.put("Show Build Status", "빌드 상태 보기");
		m.put("Show Closed", "닫힌 항목 보기");
		m.put("Show Code Stats", "코드 통계 보기");
		m.put("Show Command", "명령 보기");
		m.put("Show Condition", "조건 보기");
		m.put("Show Conditionally", "조건부로 보기");
		m.put("Show Description", "설명 보기");
		m.put("Show Duration", "기간 보기");
		m.put("Show Emojis", "이모지 보기");
		m.put("Show Error Detail", "오류 세부 정보 표시");
		m.put("Show Issue Status", "이슈 상태 보기");
		m.put("Show Package Stats", "패키지 통계 보기");
		m.put("Show Pull Request Stats", "풀 리퀘스트 통계 보기");
		m.put("Show Saved Queries", "저장된 쿼리 보기");
		m.put("Show States By", "상태별 보기");
		m.put("Show Works Of", "작업 보기");
		m.put("Show changes", "변경 사항 보기");
		m.put("Show commented code snippet", "댓글이 달린 코드 스니펫 보기");
		m.put("Show commit of this parent", "상위 커밋 보기");
		m.put("Show emojis", "이모지 보기");
		m.put("Show in build list", "빌드 목록에서 보기");
		m.put("Show issues in list", "목록에서 이슈 보기");
		m.put("Show issues not scheduled into current iteration", "현재 반복에 일정되지 않은 이슈 보기");
		m.put("Show matching agents", "일치하는 에이전트 보기");
		m.put("Show more", "더 보기");
		m.put("Show more lines", "더 많은 줄 보기");
		m.put("Show next match", "다음 일치 항목 보기");
		m.put("Show previous match", "이전 일치 항목 보기");
		m.put("Show test cases of this test suite", "이 테스트 스위트의 테스트 케이스 보기");
		m.put("Show total estimated/spent time", "총 예상/소요 시간 보기");
		m.put("Showing first {0} files as there are too many", "파일이 너무 많아 처음 {0}개 파일만 표시합니다");
		m.put("Sign In", "로그인");
		m.put("Sign In To", "로그인하기");
		m.put("Sign Out", "로그아웃");
		m.put("Sign Up", "회원가입");
		m.put("Sign Up Bean", "회원가입 Bean");
		m.put("Sign Up!", "회원가입!");
		m.put("Sign in", "로그인");
		m.put("Signature required for this change, but no signing key is specified", "이 변경 사항에는 서명이 필요하지만 서명 키가 지정되지 않았습니다.");
		m.put("Signature required for this change, please generate system GPG signing key first", "이 변경에 서명이 필요합니다. 먼저 시스템 GPG 서명 키를 생성하세요.");
		m.put("Signature verified successfully with OneDev GPG key", "OneDev GPG 키로 서명이 성공적으로 확인되었습니다");
		m.put("Signature verified successfully with committer's GPG key", "커미터의 GPG 키로 서명이 성공적으로 확인되었습니다");
		m.put("Signature verified successfully with committer's SSH key", "커미터의 SSH 키로 서명이 성공적으로 확인되었습니다");
		m.put("Signature verified successfully with tagger's GPG key", "태거의 GPG 키로 서명이 성공적으로 확인되었습니다");
		m.put("Signature verified successfully with tagger's SSH key", "태거의 SSH 키로 서명이 성공적으로 확인되었습니다");
		m.put("Signature verified successfully with trusted GPG key", "신뢰할 수 있는 GPG 키로 서명이 성공적으로 확인되었습니다");
		m.put("Signed with an unknown GPG key ", "알 수 없는 GPG 키로 서명됨");
		m.put("Signed with an unknown ssh key", "알 수 없는 ssh 키로 서명됨");
		m.put("Signer Email Addresses", "서명자 이메일 주소");
		m.put("Signing Key ID", "서명 키 ID");
		m.put("Similar Issues", "유사한 이슈");
		m.put("Single Sign On", "싱글 사인온");
		m.put("Single Sign-On", "싱글 사인온");
		m.put("Single sign on via discord.com", "discord.com을 통한 싱글 사인온");
		m.put("Single sign on via twitch.tv", "twitch.tv를 통한 싱글 사인온");
		m.put("Site", "사이트");
		m.put("Size", "크기");
		m.put("Size invalid", "크기가 유효하지 않음");
		m.put("Slack Notifications", "Slack 알림");
		m.put("Smtp Ssl Setting", "Smtp Ssl 설정");
		m.put("Smtp With Ssl", "Smtp Ssl 사용");
		m.put("Some builds are {0}", "일부 빌드는 {0} 상태입니다");
		m.put("Some jobs are hidden due to permission policy", "권한 정책으로 인해 일부 작업이 숨겨져 있습니다");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "편집 중인 내용이 다른 사람에 의해 변경되었습니다. 페이지를 새로고침하고 다시 시도하세요.");
		m.put("Some other pull requests are opening to this branch", "이 브랜치로 열려 있는 다른 풀 리퀘스트가 있습니다");
		m.put("Some projects might be hidden due to permission policy", "권한 정책으로 인해 일부 프로젝트가 숨겨질 수 있습니다");
		m.put("Some related commits of the code comment is missing", "코드 댓글과 관련된 일부 커밋이 누락되었습니다");
		m.put("Some related commits of the pull request are missing", "풀 리퀘스트와 관련된 일부 커밋이 누락되었습니다");
		m.put("Some required builds not passed", "필수 빌드 중 일부가 통과하지 못했습니다");
		m.put("Someone made below change since you started editing", "편집을 시작한 이후 아래 변경 사항이 있었습니다.");
		m.put("Sort", "정렬");
		m.put("Source", "소스");
		m.put("Source Docker Image", "소스 Docker 이미지");
		m.put("Source Lines", "소스 라인");
		m.put("Source Path", "소스 경로");
		m.put("Source branch already exists", "소스 브랜치가 이미 존재합니다");
		m.put("Source branch already merged into target branch", "소스 브랜치가 대상 브랜치에 이미 병합되었습니다");
		m.put("Source branch commits will be rebased onto target branch", "소스 브랜치 커밋이 대상 브랜치로 리베이스됩니다");
		m.put("Source branch is default branch", "소스 브랜치가 기본 브랜치입니다");
		m.put("Source branch is outdated", "소스 브랜치가 오래되었습니다");
		m.put("Source branch no longer exists", "소스 브랜치가 더 이상 존재하지 않습니다");
		m.put("Source branch updated successfully", "소스 브랜치가 성공적으로 업데이트되었습니다");
		m.put("Source project no longer exists", "소스 프로젝트가 더 이상 존재하지 않습니다");
		m.put("Specified Value", "지정된 값");
		m.put("Specified choices", "지정된 선택 항목");
		m.put("Specified default value", "지정된 기본값");
		m.put("Specified fields", "지정된 필드");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Active Directory 서버의 LDAP URL을 지정합니다. 예: <i>ldap://ad-server</i> 또는 <i>ldaps://ad-server</i>. ldaps 연결을 위한 자체 서명 인증서를 사용하는 경우, <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>OneDev를 구성하여 인증서를 신뢰하도록 설정</a>해야 합니다");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"LDAP URL을 지정합니다. 예: <i>ldap://localhost</i> 또는 <i>ldaps://localhost</i>. ldaps 연결을 위한 자체 서명 인증서를 사용하는 경우, <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>OneDev를 구성하여 인증서를 신뢰하도록 설정</a>해야 합니다");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"사용자 검색을 위한 기본 노드를 지정합니다. 예: <i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"사용자가 속한 그룹의 고유 이름을 포함하는 사용자 LDAP 항목 내부 속성 이름을 지정합니다. 예를 들어 일부 LDAP 서버는 그룹 목록을 나열하기 위해 <i>memberOf</i> 속성을 사용합니다");
		m.put("Specifies password of above manager DN", "위의 관리자 DN 비밀번호를 지정합니다");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"찾은 그룹 LDAP 항목 내부에서 그룹 이름을 포함하는 속성을 지정합니다. 이 속성의 값은 OneDev 그룹에 매핑됩니다. 일반적으로 이 속성은 <i>cn</i>으로 설정됩니다");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 .net TRX 테스트 결과 파일을 지정합니다. 예: <tt>TestResults/*.trx</tt>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"<a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정하여 위 프로젝트에 대한 코드 쓰기 권한이 있는 액세스 토큰 값을 사용합니다. 커밋, 이슈 및 풀 요청은 액세스 토큰 소유자의 이름으로 생성됩니다");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"<a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json 출력 파일을 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 지정합니다. 이 파일은 clippy json 출력 옵션으로 생성할 수 있습니다. 예: <code>cargo clippy --message-format json>check-result.json</code>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify Build Options", "빌드 옵션을 지정합니다");
		m.put("Specify CPD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 CPD 결과 xml 파일을 지정합니다. 예: <tt>target/cpd.xml</tt>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify Commit Message", "커밋 메시지를 지정합니다");
		m.put("Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 아래에서 체크스타일 형식의 ESLint 보고서 파일을 지정합니다. 이 파일은 ESLint 옵션 <tt>'-f checkstyle'</tt> 및 <tt>'-o'</tt>로 생성할 수 있습니다. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify GitHub API url, for instance <tt>https://api.github.com</tt>", "GitHub API URL을 지정합니다. 예: <tt>https://api.github.com</tt>");
		m.put("Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>", "GitLab API URL을 지정합니다. 예: <tt>https://gitlab.example.com/api/v4</tt>");
		m.put("Specify Gitea API url, for instance <tt>https://gitea.example.com/api/v1</tt>", "Gitea API URL을 지정합니다. 예: <tt>https://gitea.example.com/api/v1</tt>");
		m.put("Specify GoogleTest XML result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This report can be generated with environment variable <tt>GTEST_OUTPUT</tt> when running tests, For instance, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 GoogleTest XML 결과 파일을 지정합니다. 이 보고서는 테스트 실행 시 환경 변수 <tt>GTEST_OUTPUT</tt>로 생성할 수 있습니다. 예: <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify IMAP user name.<br><b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system email address specified above", 
			"IMAP 사용자 이름을 지정합니다.<br><b class='text-danger'>참고: </b> 이 계정은 위에 지정된 시스템 이메일 주소로 전송된 이메일을 받을 수 있어야 합니다");
		m.put("Specify JUnit test result file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 XML 형식의 JUnit 테스트 결과 파일을 지정합니다. 예: <tt>target/surefire-reports/TEST-*.xml</tt>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 JaCoCo 커버리지 xml 보고서 파일을 지정합니다. 예: <tt>target/site/jacoco/jacoco.xml</tt>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify Jest coverage report file in clover format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 클로버 형식의 Jest 커버리지 보고서 파일을 지정합니다. 예: <tt>coverage/clover.xml</tt>. 이 파일은 Jest 옵션 <tt>'--coverage'</tt>로 생성할 수 있습니다. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify Jest test result file in json format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 json 형식의 Jest 테스트 결과 파일을 지정합니다. 이 파일은 Jest 옵션 <tt>'--json'</tt> 및 <tt>'--outputFile'</tt>로 생성할 수 있습니다. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify OCI layout directory of the image to scan. This directory can be generated via build image step or pull image step. It should be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"스캔할 이미지의 OCI 레이아웃 디렉토리를 지정합니다. 이 디렉토리는 이미지 빌드 단계 또는 이미지 가져오기 단계에서 생성할 수 있습니다. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 상대 경로여야 합니다");
		m.put("Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from", 
			"푸시할 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 상대 경로의 OCI 레이아웃 디렉토리를 지정합니다");
		m.put("Specify OpenID scopes to request", "요청할 OpenID 범위를 지정합니다");
		m.put("Specify PMD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/pmd.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 PMD 결과 xml 파일을 지정합니다. 예: <tt>target/pmd.xml</tt>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify PowerShell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of your script<br>", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 아래에서 실행할 PowerShell 명령을 지정합니다.<br><b class='text-warning'>참고: </b> OneDev는 스크립트의 종료 코드를 확인하여 단계가 성공했는지 판단합니다. PowerShell은 스크립트 오류가 있어도 항상 0으로 종료되므로, 스크립트에서 오류를 처리하고 0이 아닌 코드로 종료하거나 스크립트 시작 부분에 <code>$ErrorActionPreference = &quot;Stop&quot;</code>를 추가해야 합니다<br>");
		m.put("Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with <i>-o</i> option. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 XML 형식의 Roslynator 진단 출력 파일을 지정합니다. 이 파일은 <i>-o</i> 옵션으로 생성할 수 있습니다. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify Shell/Batch Commands to Run", "실행할 Shell/Batch 명령을 지정합니다");
		m.put("Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 SpotBugs 결과 xml 파일을 지정합니다. 예: <tt>target/spotbugsXml.xml</tt>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify System Settings", "시스템 설정을 지정합니다");
		m.put("Specify URL of remote git repository. Only http/https protocol is supported", "원격 git 저장소의 URL을 지정합니다. http/https 프로토콜만 지원됩니다");
		m.put("Specify YouTrack login name. This account should have permission to:<ul><li>Read full information and issues of the projects you want to import<li>Read issue tags<li>Read user basic information</ul>", 
			"YouTrack 로그인 이름을 지정합니다. 이 계정은 다음 권한을 가져야 합니다:<ul><li>가져오려는 프로젝트의 전체 정보 및 이슈 읽기<li>이슈 태그 읽기<li>사용자 기본 정보 읽기</ul>");
		m.put("Specify YouTrack password or access token for above user", "위 사용자에 대한 YouTrack 비밀번호 또는 액세스 토큰을 지정합니다");
		m.put("Specify a &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regular expression&lt;/a&gt; to match issue references. For instance:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;", 
			"이슈 참조를 일치시키기 위한 &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;정규 표현식&lt;/a&gt;을 지정합니다. 예:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> after issue number", 
			"이슈 번호 뒤에 올 <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>정규 표현식</a>을 지정합니다");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> before issue number", 
			"이슈 번호 앞에 올 <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>정규 표현식</a>을 지정합니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key", 
			"SSH 개인 키로 사용할 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"액세스 토큰으로 사용할 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"위 프로젝트에서 빌드 사양을 가져오기 위해 액세스 토큰으로 사용할 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다. 코드가 공개적으로 접근 가능하지 않은 경우에만 필요합니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"레지스트리의 비밀번호 또는 액세스 토큰으로 사용할 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"원격 저장소에 접근하기 위한 비밀번호 또는 액세스 토큰으로 사용할 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"SSH 인증을 위한 개인 키로 사용할 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다. <b class='text-info'>참고:</b> 암호가 있는 개인 키는 지원되지 않습니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"위 사용자의 SSH 인증을 위한 개인 키로 사용할 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다. <b class='text-info'>참고:</b> 암호가 있는 개인 키는 지원되지 않습니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"위 프로젝트에 대한 관리 권한이 있는 액세스 토큰 값을 가진 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다. 현재 또는 하위 프로젝트로 동기화하고 기본 브랜치에서 빌드 커밋에 접근할 수 있는 경우 액세스 토큰이 필요하지 않습니다");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"위 프로젝트에 대한 캐시 업로드 권한이 있는 액세스 토큰 값을 가진 <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>작업 비밀</a>을 지정합니다. 현재 또는 하위 프로젝트로 캐시를 업로드하고 기본 브랜치에서 빌드 커밋에 접근할 수 있는 경우 이 속성이 필요하지 않습니다");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"작업을 자동으로 실행하기 위한 <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron 일정</a>을 지정합니다. <b class='text-info'>참고:</b> 리소스를 절약하기 위해 cron 표현식의 초는 무시되며 최소 일정 간격은 1분입니다");
		m.put("Specify a Docker Image to Test Against", "테스트할 Docker 이미지를 지정합니다");
		m.put("Specify a custom field of Enum type", "Enum 유형의 사용자 정의 필드를 지정합니다");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "지정된 작업의 고정된 이슈를 필터링/정렬하기 위한 기본 쿼리를 지정합니다");
		m.put("Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into", 
			"체크섬을 기록할 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 상대 경로의 파일을 지정합니다");
		m.put("Specify a multi-value user field to hold assignees information.<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"담당자 정보를 저장할 다중 값 사용자 필드를 지정합니다.<b>참고: </b> 적절한 옵션이 없는 경우 OneDev 이슈 필드를 사용자 정의할 수 있습니다");
		m.put("Specify a multi-value user field to hold assignees information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"담당자 정보를 저장할 다중 값 사용자 필드를 지정합니다.<br><b>참고: </b> 적절한 옵션이 없는 경우 OneDev 이슈 필드를 사용자 정의할 수 있습니다");
		m.put("Specify a path inside container to be used as mount target", "마운트 대상으로 사용할 컨테이너 내부 경로를 지정합니다");
		m.put("Specify a path relative to job workspace to be used as mount source. Leave empty to mount job workspace itself", 
			"마운트 소스로 사용할 작업 공간 상대 경로를 지정합니다. 비워두면 작업 공간 자체를 마운트합니다");
		m.put("Specify a secret to be used as access token to create issue in above project if it is not publicly accessible", 
			"위 프로젝트에서 이슈를 생성하기 위한 액세스 토큰으로 사용할 비밀을 지정합니다. 프로젝트가 공개적으로 접근 가능하지 않은 경우에만 필요합니다");
		m.put("Specify a secret to be used as access token to retrieve artifacts from above project. If not specified, project artifacts will be accessed anonymously", 
			"위 프로젝트에서 아티팩트를 가져오기 위한 액세스 토큰으로 사용할 비밀을 지정합니다. 지정하지 않으면 프로젝트 아티팩트는 익명으로 접근됩니다");
		m.put("Specify a secret to be used as access token to trigger job in above project", "위 프로젝트에서 작업을 트리거하기 위한 액세스 토큰으로 사용할 비밀을 지정하세요.");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"위 프로젝트에 대한 캐시 업로드 권한이 있는 액세스 토큰 값을 가진 비밀을 지정합니다. 현재 또는 하위 프로젝트로 캐시를 업로드하고 기본 브랜치에서 빌드 커밋에 접근할 수 있는 경우 이 속성이 필요하지 않습니다");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"클러스터에 접근하기 위해 kubectl이 사용하는 구성 파일의 절대 경로를 지정합니다. 비워두면 kubectl이 클러스터 접근 정보를 자동으로 결정합니다");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"kubectl 유틸리티의 절대 경로를 지정합니다. 예: <i>/usr/bin/kubectl</i>. 비워두면 OneDev가 시스템 경로에서 유틸리티를 찾으려고 시도합니다");
		m.put("Specify account name to login to Gmail to send/receive email", "이메일을 보내고/받기 위해 Gmail에 로그인할 계정 이름을 지정합니다");
		m.put("Specify additional users able to access this confidential issue besides those granted via role. Users mentioned in the issue will be authorized automatically", 
			"역할을 통해 권한이 부여된 사용자 외에 이 기밀 이슈에 접근할 수 있는 추가 사용자를 지정합니다. 이슈에 언급된 사용자는 자동으로 권한이 부여됩니다");
		m.put("Specify agents applicable for this executor", "이 실행기에 적용 가능한 에이전트를 지정합니다");
		m.put("Specify allowed <a href='https://spdx.org/licenses/' target='_blank'>spdx license identifiers</a> <span class='text-warning'>separated by comma</span>", 
			"허용된 <a href='https://spdx.org/licenses/' target='_blank'>spdx 라이선스 식별자</a>를 <span class='text-warning'>쉼표로 구분하여</span> 지정합니다");
		m.put("Specify an email address sharing same inbox as the system email address in mail setting definition. Emails sent to this address will be created as issues in this project. The default value takes form of <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>", 
			"메일 설정 정의에서 시스템 이메일 주소와 동일한 받은 편지함을 공유하는 이메일 주소를 지정합니다. 이 주소로 전송된 이메일은 이 프로젝트에서 이슈로 생성됩니다. 기본값은 <tt>&lt;시스템 이메일 주소 이름&gt;+&lt;프로젝트 경로&gt;@&lt;시스템 이메일 주소 도메인&gt;</tt> 형식을 따릅니다");
		m.put("Specify applicable projects for above option. Multiple projects should be separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"위 옵션에 적용 가능한 프로젝트를 지정합니다. 여러 프로젝트는 공백으로 구분해야 합니다. '**', '*' 또는 '?'를 사용하여 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 수행합니다. '-'로 시작하여 제외합니다. 비워두면 모든 프로젝트에 적용됩니다");
		m.put("Specify applicable projects separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"적용 가능한 프로젝트를 공백으로 구분하여 지정합니다. '**', '*' 또는 '?'를 사용하여 <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 수행합니다. '-'로 시작하여 제외합니다. 비워두면 모든 프로젝트에 적용됩니다");
		m.put("Specify application (client) ID of the app registered in Entra ID", "Entra ID에 등록된 앱의 애플리케이션(클라이언트) ID를 지정합니다");
		m.put("Specify arguments for imagetools. For instance <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>", 
			"imagetools에 대한 인수를 지정합니다. 예: <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>");
		m.put("Specify artifacts to retrieve into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved.", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a>으로 가져올 아티팩트를 지정합니다. 아티팩트는 아티팩트 게시 단계에서 게시된 것만 가져올 수 있습니다");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"비밀로 사용할 최소 10자리의 영숫자를 지정한 후 SendGrid 측에서 인바운드 파싱 항목을 추가합니다:<ul><li><code>Destination URL</code>은 <i>&lt;OneDev 루트 URL&gt;/~sendgrid/&lt;비밀&gt;</i>로 설정해야 합니다. 예: <i>https://onedev.example.com/~sendgrid/1234567890</i>. 프로덕션 환경에서는 <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https를 활성화</a>하여 비밀을 보호해야 합니다</li><li><code>Receiving domain</code>은 위에 지정된 시스템 이메일 주소의 도메인 부분과 동일해야 합니다</li><li>옵션 <code>POST the raw, full MIME message</code>가 활성화되어야 합니다</li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"사용자 검색을 위한 기본 노드를 지정합니다. 예: <i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "제안된 변경 사항을 커밋할 브랜치를 지정합니다");
		m.put("Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"작업을 실행할 브랜치를 지정하세요. 브랜치나 태그 중 하나만 지정할 수 있으며, 둘 다 지정할 수 없습니다. 둘 다 지정하지 않으면 기본 브랜치가 사용됩니다.");
		m.put("Specify branch, tag or commit in above project to import build spec from", "위 프로젝트에서 빌드 사양을 가져오기 위한 브랜치, 태그 또는 커밋을 지정합니다");
		m.put("Specify by Build Number", "빌드 번호로 지정합니다");
		m.put("Specify cache upload strategy after build successful. <var>Upload If Not Hit</var> means to upload when cache is not found with cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files in cache path are changed", 
			"빌드 성공 후 캐시 업로드 전략을 지정합니다. <var>Upload If Not Hit</var>은 캐시 키(로드 키가 아님)로 캐시를 찾을 수 없는 경우 업로드를 의미하며, <var>Upload If Changed</var>는 캐시 경로의 일부 파일이 변경된 경우 업로드를 의미합니다");
		m.put("Specify certificate to trust if you are using self-signed certificate for remote repository", 
			"원격 저장소에 대해 자체 서명 인증서를 사용하는 경우 신뢰할 인증서를 지정합니다");
		m.put("Specify certificates to trust if you are using self-signed certificates for your docker registries", 
			"도커 레지스트리에 대해 자체 서명 인증서를 사용하는 경우 신뢰할 인증서를 지정합니다");
		m.put("Specify checkstyle result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/checkstyle-result.xml</tt>. Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> on how to generate the result xml file. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 체크스타일 결과 xml 파일을 지정합니다. 예: <tt>target/checkstyle-result.xml</tt>. 체크스타일 결과 xml 파일을 생성하는 방법은 <a href='https://checkstyle.org/'>체크스타일 문서</a>를 참조하세요. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify client secret of the app registered in Entra ID", "Entra ID에 등록된 앱의 클라이언트 비밀을 지정합니다");
		m.put("Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/clover/clover.xml</tt>. Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> on how to generate clover xml file. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 클로버 커버리지 xml 보고서 파일을 지정합니다. 예: <tt>target/site/clover/clover.xml</tt>. 클로버 xml 파일을 생성하는 방법은 <a href='https://openclover.org/documentation'>OpenClover 문서</a>를 참조하세요. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 상대 경로로 cobertura 커버리지 xml 보고서 파일을 지정합니다. 예: <tt>target/site/cobertura/coverage.xml</tt>. 패턴 매칭을 위해 * 또는 ?를 사용하세요");
		m.put("Specify color of the state for displaying purpose", "표시 목적으로 상태의 색상을 지정합니다");
		m.put("Specify columns of the board. Each column corresponds to a value of the issue field specified above", 
			"보드의 열을 지정합니다. 각 열은 위에 지정된 이슈 필드의 값에 해당합니다");
		m.put("Specify command to check readiness of the service. This command will be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be executed repeatedly until a zero code is returned to indicate service ready", 
			"서비스 준비 상태를 확인하기 위한 명령을 지정합니다. 이 명령은 Windows 이미지에서는 cmd.exe, Linux 이미지에서는 셸에 의해 해석됩니다. 서비스 준비를 나타내는 0 코드가 반환될 때까지 반복적으로 실행됩니다");
		m.put("Specify commands to be executed on remote machine. <b class='text-warning'>Note:</b> user environments will not be picked up when execute these commands, set up them explicitly in commands if necessary", 
			"원격 머신에서 실행할 명령을 지정하세요. <b class='text-warning'>참고:</b> 사용자 환경은 이러한 명령을 실행할 때 적용되지 않으므로 필요시 명령에서 명시적으로 설정하세요.");
		m.put("Specify condition to retry build upon failure", "실패 시 빌드를 재시도할 조건을 지정하세요.");
		m.put("Specify configuration discovery url of your OpenID provider, for instance: <code>https://openid.example.com/.well-known/openid-configuration</code>. Make sure to use HTTPS protocol as OneDev relies on TLS encryption to ensure token validity", 
			"OpenID 제공자의 구성 검색 URL을 지정하세요. 예: <code>https://openid.example.com/.well-known/openid-configuration</code>. OneDev는 토큰 유효성을 보장하기 위해 TLS 암호화에 의존하므로 HTTPS 프로토콜을 사용해야 합니다.");
		m.put("Specify container image to execute commands inside", "명령을 실행할 컨테이너 이미지를 지정하세요.");
		m.put("Specify container image to run", "실행할 컨테이너 이미지를 지정하세요.");
		m.put("Specify cppcheck xml result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 기준으로 cppcheck XML 결과 파일을 지정하세요. 이 파일은 cppcheck XML 출력 옵션으로 생성할 수 있습니다. 예: <code>cppcheck src --xml 2>check-result.xml</code>. 패턴 매칭을 위해 * 또는 ?를 사용하세요.");
		m.put("Specify cpu request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"이 실행자를 사용하는 각 작업/서비스에 대한 CPU 요청을 지정하세요. 자세한 내용은 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes 리소스 관리</a>를 확인하세요.");
		m.put("Specify default assignees of pull requests submitted to this project. Only users with the write code permission to the project can be selected", 
			"이 프로젝트에 제출된 풀 요청의 기본 담당자를 지정하세요. 프로젝트에 대한 코드 쓰기 권한이 있는 사용자만 선택할 수 있습니다.");
		m.put("Specify default merge strategy of pull requests submitted to this project", "이 프로젝트에 제출된 풀 요청의 기본 병합 전략을 지정하세요.");
		m.put("Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple destinations should be separated with space", 
			"대상지를 지정하세요. 예: <tt>registry-server:5000/myorg/myrepo:latest</tt>. 내장 레지스트리에 푸시하려면 시스템 설정의 서버 URL에 지정된 <b>동일한 호스트</b>를 사용하거나 단순히 <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt> 형식을 사용하세요. 여러 대상은 공백으로 구분해야 합니다.");
		m.put("Specify directory (tenant) ID of the app registered in Entra ID", "Entra ID에 등록된 앱의 디렉터리(테넌트) ID를 지정하세요.");
		m.put("Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 기준으로 OCI 레이아웃을 저장할 디렉터리를 지정하세요.");
		m.put("Specify docker image of the service", "서비스의 도커 이미지를 지정하세요.");
		m.put("Specify dockerx builder used to build docker image. OneDev will create the builder automatically if it does not exist. Check <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> on how to customize the builder for instance to allow publishing to insecure registries", 
			"도커 이미지를 빌드하는 데 사용되는 dockerx 빌더를 지정하세요. OneDev는 빌더가 존재하지 않을 경우 자동으로 생성합니다. 예를 들어 비보안 레지스트리에 게시를 허용하도록 빌더를 사용자 정의하는 방법에 대한 <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>이 튜토리얼</a>을 확인하세요.");
		m.put("Specify email addresses to send invitations, with one per line", "초대장을 보낼 이메일 주소를 한 줄에 하나씩 지정하세요.");
		m.put("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\"", 
			"\"{0}\"을 제외하고 <b class='text-warning'>이 문제에 대해서만</b> 예상 시간을 지정하세요.");
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"종속성 업데이트를 조정하기 위해 Renovate가 생성한 다양한 문제의 필드를 지정하세요.");
		m.put("Specify fields to be displayed in the issue list", "문제 목록에 표시할 필드를 지정하세요.");
		m.put("Specify fields to display in board card", "보드 카드에 표시할 필드를 지정하세요.");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 기준으로 게시할 파일을 지정하세요. 패턴 매칭을 위해 * 또는 ?를 사용하세요.");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"md5 체크섬을 생성할 파일을 지정하세요. 여러 파일은 공백으로 구분해야 합니다. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> 패턴이 허용됩니다. 절대 경로가 아닌 파일은 <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 기준으로 간주됩니다.");
		m.put("Specify files under above directory to be published", "위 디렉토리 아래에 게시할 파일 지정");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be included in these files to be served as site start page", 
			"위 디렉터리 아래에서 게시할 파일을 지정하세요. 패턴 매칭을 위해 * 또는 ?를 사용하세요. <b>참고:</b> <code>index.html</code>은 사이트 시작 페이지로 제공되기 위해 이러한 파일에 포함되어야 합니다.");
		m.put("Specify group to import from. Leave empty to import from projects under current account", 
			"가져올 그룹을 지정하세요. 현재 계정 아래 프로젝트에서 가져오려면 비워 두세요.");
		m.put("Specify how to map GitHub issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"GitHub 문제 레이블을 OneDev 사용자 정의 필드에 매핑하는 방법을 지정하세요.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Specify how to map GitLab issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"GitLab 문제 레이블을 OneDev 사용자 정의 필드에 매핑하는 방법을 지정하세요.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Specify how to map Gitea issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Gitea 문제 레이블을 OneDev 사용자 정의 필드에 매핑하는 방법을 지정하세요.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Specify how to map JIRA issue priorities to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"JIRA 문제 우선순위를 OneDev 사용자 정의 필드에 매핑하는 방법을 지정하세요.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Specify how to map JIRA issue statuses to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"JIRA 문제 상태를 OneDev 사용자 정의 필드에 매핑하는 방법을 지정하세요.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 상태를 사용자 정의할 수 있습니다.");
		m.put("Specify how to map JIRA issue types to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"JIRA 문제 유형을 OneDev 사용자 정의 필드에 매핑하는 방법을 지정하세요.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Specify how to map YouTrack issue fields to OneDev. Unmapped fields will be reflected in issue description.<br><b>Note: </b><ul><li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, for instance <tt>Priority::Critical</tt><li>You may customize OneDev issue fields in case there is no appropriate option here</ul>", 
			"YouTrack 문제 필드를 OneDev에 매핑하는 방법을 지정하세요. 매핑되지 않은 필드는 문제 설명에 반영됩니다.<br><b>참고:</b><ul><li>열거형 필드는 <tt>&lt;필드 이름&gt;::&lt;필드 값&gt;</tt> 형식으로 매핑해야 합니다. 예: <tt>Priority::Critical</tt></li><li>적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.</ul>");
		m.put("Specify how to map YouTrack issue links to OneDev issue links.<br><b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here", 
			"YouTrack 문제 링크를 OneDev 문제 링크에 매핑하는 방법을 지정하세요.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 링크를 사용자 정의할 수 있습니다.");
		m.put("Specify how to map YouTrack issue state to OneDev issue state. Unmapped states will use the initial state in OneDev.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"YouTrack 문제 상태를 OneDev 문제 상태에 매핑하는 방법을 지정하세요. 매핑되지 않은 상태는 OneDev의 초기 상태를 사용합니다.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 상태를 사용자 정의할 수 있습니다.");
		m.put("Specify how to map YouTrack issue tags to OneDev issue custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"YouTrack 문제 태그를 OneDev 문제 사용자 정의 필드에 매핑하는 방법을 지정하세요.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 문제 필드를 사용자 정의할 수 있습니다.");
		m.put("Specify image on the login button", "로그인 버튼에 표시할 이미지를 지정하세요.");
		m.put("Specify image tag to pull from, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to pull from built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"가져올 이미지 태그를 지정하세요. 예: <tt>registry-server:5000/myorg/myrepo:latest</tt>. 내장 레지스트리에서 가져오려면 시스템 설정의 서버 URL에 지정된 <b>동일한 호스트</b>를 사용하거나 단순히 <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt> 형식을 사용하세요.");
		m.put("Specify image tag to push to, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"푸시할 이미지 태그를 지정하세요. 예: <tt>registry-server:5000/myorg/myrepo:latest</tt>. 내장 레지스트리에 푸시하려면 시스템 설정의 서버 URL에 지정된 <b>동일한 호스트</b>를 사용하거나 단순히 <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt> 형식을 사용하세요.");
		m.put("Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple tags should be separated with space", 
			"푸시할 이미지 태그를 지정하세요. 예: <tt>registry-server:5000/myorg/myrepo:latest</tt>. 내장 레지스트리에 푸시하려면 시스템 설정의 서버 URL에 지정된 <b>동일한 호스트</b>를 사용하거나 단순히 <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt> 형식을 사용하세요. 여러 태그는 공백으로 구분해야 합니다.");
		m.put("Specify import option", "가져오기 옵션을 지정하세요.");
		m.put("Specify incoming email poll interval in seconds", "수신 이메일 폴링 간격(초)을 지정하세요.");
		m.put("Specify issue creation settings. For a particular sender and project, the first matching entry will take effect. Issue creation will be disallowed if no matching entry found", 
			"문제 생성 설정을 지정하세요. 특정 발신자와 프로젝트에 대해 첫 번째 일치 항목이 적용됩니다. 일치 항목이 없으면 문제 생성이 허용되지 않습니다.");
		m.put("Specify issue field to identify different columns of the board. Only state and single-valued enumeration field can be used here", 
			"보드의 다른 열을 식별할 문제 필드를 지정하세요. 여기에는 상태 및 단일 값 열거형 필드만 사용할 수 있습니다.");
		m.put("Specify links to be displayed in the issue list", "문제 목록에 표시할 링크를 지정하세요.");
		m.put("Specify links to display in board card", "보드 카드에 표시할 링크를 지정하세요.");
		m.put("Specify manager DN to authenticate OneDev itself to Active Directory. The manager DN should be specified in form of <i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>manager@example.com</i>", 
			"OneDev 자체를 Active Directory에 인증하기 위한 관리자 DN을 지정하세요. 관리자 DN은 <i>&lt;계정 이름&gt;@&lt;도메인&gt;</i> 형식으로 지정해야 합니다. 예: <i>manager@example.com</i>.");
		m.put("Specify manager DN to authenticate OneDev itself to LDAP server", "OneDev 자체를 LDAP 서버에 인증하기 위한 관리자 DN을 지정하세요.");
		m.put("Specify markdown file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 기준으로 게시할 마크다운 파일을 지정하세요.");
		m.put("Specify max git LFS file size in mega bytes", "Git LFS 파일의 최대 크기(메가바이트)를 지정하세요.");
		m.put("Specify max number of CPU intensive tasks the server can run concurrently, such as Git repository pull/push, repository index, etc.", 
			"서버가 동시에 실행할 수 있는 CPU 집약적인 작업의 최대 수를 지정하세요. 예: Git 저장소 가져오기/푸시, 저장소 인덱스 등.");
		m.put("Specify max number of jobs this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"이 실행자가 각 일치하는 에이전트에서 동시에 실행할 수 있는 작업의 최대 수를 지정하세요. 에이전트 CPU 코어로 설정하려면 비워 두세요.");
		m.put("Specify max number of jobs this executor can run concurrently. Leave empty to set as CPU cores", 
			"이 실행자가 동시에 실행할 수 있는 작업의 최대 수를 지정하세요. CPU 코어로 설정하려면 비워 두세요.");
		m.put("Specify max number of jobs/services this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"이 실행자가 각 일치하는 에이전트에서 동시에 실행할 수 있는 작업/서비스의 최대 수를 지정하세요. 에이전트 CPU 코어로 설정하려면 비워 두세요.");
		m.put("Specify max number of jobs/services this executor can run concurrently. Leave empty to set as CPU cores", 
			"이 실행자가 동시에 실행할 수 있는 작업/서비스의 최대 수를 지정하세요. CPU 코어로 설정하려면 비워 두세요.");
		m.put("Specify max size of uploaded file in mega bytes via web interface. This applies to file uploaded to repository, markdown content (issue comment etc), and build artifacts", 
			"웹 인터페이스를 통해 업로드된 파일의 최대 크기(메가바이트)를 지정하세요. 이는 저장소에 업로드된 파일, 마크다운 콘텐츠(문제 댓글 등), 빌드 아티팩트에 적용됩니다.");
		m.put("Specify memory request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"이 실행자를 사용하는 각 작업/서비스에 대한 메모리 요청을 지정하세요. 자세한 내용은 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes 리소스 관리</a>를 확인하세요.");
		m.put("Specify mypy output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated by redirecting mypy output <b>without option '--pretty'</b>, for instance <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 기준으로 mypy 출력 파일을 지정하세요. 이 파일은 <b>'--pretty' 옵션 없이</b> mypy 출력을 리디렉션하여 생성할 수 있습니다. 예: <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. 패턴 매칭을 위해 * 또는 ?를 사용하세요.");
		m.put("Specify name of the branch", "브랜치 이름을 지정하세요.");
		m.put("Specify name of the environment variable", "환경 변수 이름을 지정하세요.");
		m.put("Specify name of the iteration", "반복 이름을 지정하세요.");
		m.put("Specify name of the job", "작업 이름을 지정하세요.");
		m.put("Specify name of the report to be displayed in build detail page", "빌드 세부 페이지에 표시할 보고서 이름을 지정하세요.");
		m.put("Specify name of the saved query", "저장된 쿼리 이름을 지정하세요.");
		m.put("Specify name of the service, which will be used as host name to access the service", 
			"서비스 이름을 지정하세요. 이는 서비스에 액세스하기 위한 호스트 이름으로 사용됩니다.");
		m.put("Specify name of the tag", "태그 이름을 지정하세요.");
		m.put("Specify network timeout in seconds when authenticate through this system", "이 시스템을 통해 인증할 때 네트워크 타임아웃(초)을 지정하세요.");
		m.put("Specify node selector of this locator", "이 로케이터의 노드 선택기를 지정하세요.");
		m.put("Specify password or access token of specified registry", "지정된 레지스트리의 비밀번호 또는 액세스 토큰을 지정하세요.");
		m.put("Specify password to authenticate with", "인증에 사용할 비밀번호를 지정하세요.");
		m.put("Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>", "curl 실행 파일 경로를 지정하세요. 예: <tt>/usr/bin/curl</tt>.");
		m.put("Specify path to git executable, for instance: <tt>/usr/bin/git</tt>", "git 실행 파일 경로를 지정하세요. 예: <tt>/usr/bin/git</tt>.");
		m.put("Specify powershell executable to be used", "사용할 powershell 실행 파일을 지정하세요.");
		m.put("Specify project to import build spec from", "빌드 사양을 가져올 프로젝트를 지정하세요.");
		m.put("Specify project to import into at OneDev side", "OneDev 측에서 가져올 프로젝트를 지정하세요.");
		m.put("Specify project to retrieve artifacts from", "아티팩트를 가져올 프로젝트를 지정하세요.");
		m.put("Specify project to run job in", "작업을 실행할 프로젝트를 지정하세요.");
		m.put("Specify projects", "프로젝트를 지정하세요.");
		m.put("Specify projects to update dependencies. Leave empty for current project", "종속성을 업데이트할 프로젝트를 지정하세요. 현재 프로젝트의 경우 비워 두세요.");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 기준으로 pylint JSON 결과 파일을 지정하세요. 이 파일은 pylint JSON 출력 형식 옵션으로 생성할 수 있습니다. 예: <code>--exit-zero --output-format=json:pylint-result.json</code>. 이 단계는 구성된 임계값에 따라 빌드를 실패시키므로 위반 시 pylint 명령을 실패시키지 않습니다. 패턴 매칭을 위해 * 또는 ?를 사용하세요.");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"필요한 경우 레지스트리 로그인을 지정하세요. 내장 레지스트리의 경우 레지스트리 URL에 <code>@server_url@</code>, 사용자 이름에 <code>@job_token@</code>, 비밀번호에 액세스 토큰을 사용하세요.");
		m.put("Specify registry url. Leave empty for official registry", "레지스트리 URL을 지정하세요. 공식 레지스트리의 경우 비워 두세요.");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 기준으로 OCI 레이아웃을 저장할 상대 경로를 지정하세요.");
		m.put("Specify repositories", "저장소를 지정하세요.");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"지정된 경로가 변경된 경우 필요한 검토자를 지정하세요. 변경을 제출한 사용자는 자동으로 변경을 검토한 것으로 간주됩니다.");
		m.put("Specify root URL to access this server", "이 서버에 액세스할 루트 URL을 지정하세요.");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 기준으로 ruff JSON 결과 파일을 지정하세요. 이 파일은 ruff JSON 출력 형식 옵션으로 생성할 수 있습니다. 예: <code>--exit-zero --output-format json --output-file ruff-result.json</code>. 이 단계는 구성된 임계값에 따라 빌드를 실패시키므로 위반 시 ruff 명령을 실패시키지 않습니다. 패턴 매칭을 위해 * 또는 ?를 사용하세요.");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 아래에서 실행할 셸 명령(Linux/Unix) 또는 배치 명령(Windows)을 지정하세요.");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"<a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 공간</a> 아래에서 실행할 셸 명령을 지정하세요.");
		m.put("Specify shell to be used", "사용할 셸을 지정하세요.");
		m.put("Specify source param for SCP command, for instance <code>app.tar.gz</code>", "SCP 명령의 소스 매개변수를 지정하세요. 예: <code>app.tar.gz</code>.");
		m.put("Specify space separated refs to pull from remote. '*' can be used in ref name for wildcard match<br><b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update branches/tags via this step", 
			"원격에서 가져올 공백으로 구분된 참조를 지정하세요. '*'는 참조 이름에서 와일드카드 매칭에 사용할 수 있습니다.<br><b class='text-danger'>참고:</b> 이 단계에서 브랜치/태그를 업데이트할 때 브랜치/태그 보호 규칙이 무시됩니다.");
		m.put("Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"보호할 공백으로 구분된 브랜치를 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요.");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"공백으로 구분된 작업을 지정하세요. 와일드카드 매칭을 위해 '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요.");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts will be granted implicitly in matched jobs even if no other permissions are specified here", 
			"공백으로 구분된 작업을 지정하세요. 와일드카드 매칭을 위해 '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. <b class='text-danger'>참고:</b> 여기에서 다른 권한이 지정되지 않은 경우에도 일치하는 작업에서 빌드 아티팩트에 액세스할 권한이 암시적으로 부여됩니다.");
		m.put("Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"보호할 공백으로 구분된 경로를 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요.");
		m.put("Specify space-separated projects applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all projects", 
			"이 항목에 적용 가능한 공백으로 구분된 프로젝트를 지정하세요. 와일드카드 매칭을 위해 '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 모든 프로젝트와 일치하려면 비워 두세요.");
		m.put("Specify space-separated sender email addresses applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all senders", 
			"이 항목에 적용 가능한 공백으로 구분된 발신자 이메일 주소를 지정하세요. 와일드카드 매칭을 위해 '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요. 모든 발신자와 일치하려면 비워 두세요.");
		m.put("Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"보호할 공백으로 구분된 태그를 지정하세요. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>을 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 접두어를 붙이세요.");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 기준으로 보고서의 시작 페이지를 지정하세요. 예: <tt>manual/index.md</tt>.");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"<a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a> 기준으로 보고서의 시작 페이지를 지정하세요. 예: api/index.html.");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"빌드 볼륨에 요청할 저장소 크기를 지정하세요. 크기는 <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes 리소스 용량 형식</a>을 따라야 합니다. 예: <i>10Gi</i>.");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"제공된 보고서에서 발견된 문제의 열 값을 계산하는 데 사용되는 탭 너비를 지정하세요.");
		m.put("Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"작업을 실행할 태그를 지정하세요. 브랜치나 태그 중 하나만 지정할 수 있으며, 둘 다 지정할 수 없습니다. 둘 다 지정하지 않으면 기본 브랜치가 사용됩니다.");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"SCP 명령의 대상 매개변수를 지정하십시오. 예를 들어 <code>user@@host:/app</code>. <b class='text-info'>참고:</b> 원격 호스트에 scp 명령이 설치되어 있는지 확인하십시오.");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"일치하는 이슈 참조를 대체할 텍스트를 지정하십시오. 예를 들어: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;여기서 $1과 $2는 예제 이슈 패턴의 캡처 그룹을 나타냅니다(이슈 패턴 도움말 참조).");
		m.put("Specify the condition current build must satisfy to execute this action", "현재 빌드가 이 작업을 실행하기 위해 충족해야 하는 조건을 지정하십시오.");
		m.put("Specify the condition preserved builds must match", "보존된 빌드가 일치해야 하는 조건을 지정하십시오.");
		m.put("Specify the private key (in PEM format) used by SSH server to establish connections with client", 
			"SSH 서버가 클라이언트와 연결을 설정하는 데 사용하는 개인 키(PEM 형식)를 지정하십시오.");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"그룹 멤버십 정보를 검색하는 전략을 지정하십시오. LDAP 그룹에 적절한 권한을 부여하려면 동일한 이름의 OneDev 그룹을 정의해야 합니다. 그룹 멤버십을 OneDev 측에서 관리하려면 <tt>그룹 검색 안 함</tt> 전략을 사용하십시오.");
		m.put("Specify timeout in seconds when communicating with mail server", "메일 서버와 통신할 때의 타임아웃(초)을 지정하십시오.");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "작업이 제출된 시점부터의 타임아웃(초)을 지정하십시오.");
		m.put("Specify title of the issue", "이슈 제목을 지정하십시오.");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "YouTrack API의 URL을 지정하십시오. 예를 들어 <tt>http://localhost:8080/api</tt>.");
		m.put("Specify user name of above machine for SSH authentication", "SSH 인증을 위한 위 머신의 사용자 이름을 지정하십시오.");
		m.put("Specify user name of specified registry", "지정된 레지스트리의 사용자 이름을 지정하십시오.");
		m.put("Specify user name of the registry", "레지스트리의 사용자 이름을 지정하십시오.");
		m.put("Specify user name to authenticate with", "인증에 사용할 사용자 이름을 지정하십시오.");
		m.put("Specify value of the environment variable", "환경 변수의 값을 지정하십시오.");
		m.put("Specify web UI session timeout in minutes. Existing sessions will not be affected after changing this value.", 
			"웹 UI 세션 시간 초과를 분 단위로 지정합니다. 이 값을 변경해도 기존 세션에는 영향을 미치지 않습니다.");
		m.put("Specify webhook url to post events", "이벤트를 게시할 웹훅 URL을 지정하십시오.");
		m.put("Specify which issue state to use for closed GitHub issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"닫힌 GitHub 이슈에 사용할 이슈 상태를 지정하십시오.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 이슈 상태를 사용자 정의할 수 있습니다.");
		m.put("Specify which issue state to use for closed GitLab issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"닫힌 GitLab 이슈에 사용할 이슈 상태를 지정하십시오.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 이슈 상태를 사용자 정의할 수 있습니다.");
		m.put("Specify which issue state to use for closed Gitea issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"닫힌 Gitea 이슈에 사용할 이슈 상태를 지정하십시오.<br><b>참고:</b> 적절한 옵션이 없는 경우 OneDev 이슈 상태를 사용자 정의할 수 있습니다.");
		m.put("Specify which states are considered as closed for various issues created by Renovate to orchestrate dependency update. Additionally, when Renovate closes the issue, OneDev will transit the issue to first state specified here", 
			"Renovate가 의존성 업데이트를 조정하기 위해 생성한 다양한 이슈에서 닫힌 것으로 간주되는 상태를 지정하십시오. 추가적으로, Renovate가 이슈를 닫을 때 OneDev는 이슈를 여기에서 지정된 첫 번째 상태로 전환합니다.");
		m.put("Specify working days per week. This will affect parsing and displaying of working periods. For instance <tt>1w</tt> is the same as <tt>5d</tt> if this property is set to <tt>5</tt>", 
			"주당 근무일을 지정하십시오. 이는 근무 기간의 분석 및 표시에 영향을 미칩니다. 예를 들어 <tt>1w</tt>는 이 속성이 <tt>5</tt>로 설정된 경우 <tt>5d</tt>와 동일합니다.");
		m.put("Specify working hours per day. This will affect parsing and displaying of working periods. For instance <tt>1d</tt> is the same as <tt>8h</tt> if this property is set to <tt>8</tt>", 
			"일일 근무 시간을 지정하십시오. 이는 근무 기간의 분석 및 표시에 영향을 미칩니다. 예를 들어 <tt>1d</tt>는 이 속성이 <tt>8</tt>로 설정된 경우 <tt>8h</tt>와 동일합니다.");
		m.put("Spent", "소요됨");
		m.put("Spent Time", "소요 시간");
		m.put("Spent Time Issue Field", "소요 시간 이슈 필드");
		m.put("Spent Time:", "소요 시간:");
		m.put("Spent time / estimated time", "소요 시간 / 예상 시간");
		m.put("Split", "분할");
		m.put("Split view", "분할 보기");
		m.put("SpotBugs Report", "SpotBugs 보고서");
		m.put("Squash Source Branch Commits", "소스 브랜치 커밋 병합");
		m.put("Squash all commits from source branch into a single commit in target branch", "소스 브랜치의 모든 커밋을 대상 브랜치의 단일 커밋으로 병합");
		m.put("Squash source branch commits", "소스 브랜치 커밋 병합");
		m.put("Ssh", "Ssh");
		m.put("Ssh Key", "Ssh 키");
		m.put("Ssh Setting", "Ssh 설정");
		m.put("Ssl Setting", "Ssl 설정");
		m.put("Sso Connector", "Sso 커넥터");
		m.put("Sso Provider Bean", "Sso 제공자 빈");
		m.put("Start At", "시작 시간");
		m.put("Start Date", "시작 날짜");
		m.put("Start Page", "시작 페이지");
		m.put("Start agent on remote Linux machine by running below command:", "아래 명령을 실행하여 원격 Linux 머신에서 에이전트를 시작하십시오:");
		m.put("Start date", "시작 날짜");
		m.put("Start to watch once I am involved", "내가 참여하면 감시를 시작");
		m.put("Start work", "작업 시작");
		m.put("Start/Due Date", "시작/마감 날짜");
		m.put("State", "상태");
		m.put("State Durations", "상태 지속 시간");
		m.put("State Frequencies", "상태 빈도");
		m.put("State Spec", "상태 사양");
		m.put("State Transitions", "상태 전환");
		m.put("State Trends", "상태 추세");
		m.put("State of an issue is transited", "이슈의 상태가 전환됨");
		m.put("States", "상태들");
		m.put("Statistics", "통계");
		m.put("Stats", "통계");
		m.put("Stats Group", "통계 그룹");
		m.put("Status", "상태");
		m.put("Status Code", "상태 코드");
		m.put("Status code", "상태 코드");
		m.put("Status code other than 200 indicating the error type", "오류 유형을 나타내는 200이 아닌 상태 코드");
		m.put("Step", "단계");
		m.put("Step Template", "단계 템플릿");
		m.put("Step Templates", "단계 템플릿들");
		m.put("Step {0} of {1}: ", "{0}단계 중 {1}단계:");
		m.put("Steps", "단계들");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"단계는 동일한 노드에서 직렬로 실행되며 동일한 <a href='https://docs.onedev.io/concepts#job-workspace'>작업 공간</a>을 공유합니다.");
		m.put("Stop work", "작업 중지");
		m.put("Stopwatch Overdue", "스톱워치 초과");
		m.put("Storage Settings", "스토리지 설정");
		m.put("Storage file missing", "스토리지 파일 누락");
		m.put("Storage not found", "스토리지를 찾을 수 없음");
		m.put("Stored with Git LFS", "Git LFS로 저장됨");
		m.put("Sub Keys", "하위 키");
		m.put("Subject", "제목");
		m.put("Submit", "제출");
		m.put("Submit Reason", "제출 이유");
		m.put("Submit Support Request", "지원 요청 제출");
		m.put("Submitted After", "이후 제출됨");
		m.put("Submitted At", "제출 시간");
		m.put("Submitted Before", "이전에 제출됨");
		m.put("Submitted By", "제출자");
		m.put("Submitted manually", "수동으로 제출됨");
		m.put("Submitter", "제출자");
		m.put("Subscription Key", "구독 키");
		m.put("Subscription Management", "구독 관리");
		m.put("Subscription data", "구독 데이터");
		m.put("Subscription key installed successfully", "구독 키가 성공적으로 설치됨");
		m.put("Subscription key not applicable: this key is intended to activate a trial subscription", 
			"구독 키가 적용되지 않음: 이 키는 체험 구독을 활성화하기 위한 것입니다.");
		m.put("Subscription key not applicable: this key is intended to renew a user based subscription", 
			"구독 키가 적용되지 않음: 이 키는 사용자 기반 구독을 갱신하기 위한 것입니다");
		m.put("Subscription key not applicable: this key is intended to renew an unlimited users subscription", 
			"구독 키가 적용되지 않음: 이 키는 무제한 사용자 구독을 갱신하기 위한 것입니다");
		m.put("Subscription key not applicable: this key is intended to update licensee of an existing subscription", 
			"구독 키가 적용되지 않음: 이 키는 기존 구독의 라이센시를 업데이트하기 위한 것입니다.");
		m.put("Success Rate", "성공률");
		m.put("Successful", "성공적");
		m.put("Suffix Pattern", "접미사 패턴");
		m.put("Suggest changes", "변경 사항 제안");
		m.put("Suggested change", "제안된 변경 사항");
		m.put("Suggestion is outdated either due to code change or pull request close", "제안이 코드 변경 또는 풀 리퀘스트 종료로 인해 오래됨");
		m.put("Suggestions", "제안들");
		m.put("Summary", "요약");
		m.put("Support & Bug Report", "지원 및 버그 보고");
		m.put("Support Request", "지원 요청");
		m.put("Swap", "스왑");
		m.put("Switch to HTTP(S)", "HTTP(S)로 전환");
		m.put("Switch to SSH", "SSH로 전환");
		m.put("Symbol Name", "심볼 이름");
		m.put("Symbol name", "심볼 이름");
		m.put("Symbols", "심볼");
		m.put("Sync Replica Status and Back to Home", "복제본 상태 동기화 및 홈으로 돌아가기");
		m.put("Sync Repository", "저장소 동기화");
		m.put("Sync Timing of All Queried Issues", "조회된 모든 이슈의 동기화 타이밍");
		m.put("Sync Timing of Selected Issues", "선택된 이슈의 동기화 타이밍");
		m.put("Sync requested. Please check status after a while", "동기화 요청됨. 잠시 후 상태를 확인하세요");
		m.put("Synchronize", "동기화");
		m.put("System", "시스템");
		m.put("System Alert", "시스템 알림");
		m.put("System Alert Template", "시스템 알림 템플릿");
		m.put("System Date", "시스템 날짜");
		m.put("System Email Address", "시스템 이메일 주소");
		m.put("System Maintenance", "시스템 유지보수");
		m.put("System Setting", "시스템 설정");
		m.put("System Settings", "시스템 설정");
		m.put("System email address defined in mail setting should be used as recipient of such email, and project name may be appended to this address using to indicate where to create issues. For instance, if system email address is specified as <tt>support@example.com</tt>, sending email to <tt>support+myproject@example.com</tt> will create issue in <tt>myproject</tt>. If project name is not appended, OneDev will look up the project using project designation information below", 
			"메일 설정에서 정의된 시스템 이메일 주소는 해당 이메일의 수신자로 사용되어야 하며, 프로젝트 이름을 추가하여 이슈를 생성할 위치를 나타낼 수 있습니다. 예를 들어, 시스템 이메일 주소가 <tt>support@example.com</tt>으로 지정된 경우, <tt>support+myproject@example.com</tt>으로 이메일을 보내면 <tt>myproject</tt>에서 이슈가 생성됩니다. 프로젝트 이름이 추가되지 않은 경우, OneDev는 아래 프로젝트 지정 정보를 사용하여 프로젝트를 검색합니다.");
		m.put("System settings have been saved", "시스템 설정이 저장되었습니다");
		m.put("System uuid", "시스템 UUID");
		m.put("TIMED_OUT", "시간 초과");
		m.put("TRX Report (.net unit test)", "TRX 보고서 (.net 단위 테스트)");
		m.put("Tab Width", "탭 너비");
		m.put("Tag", "태그");
		m.put("Tag \"{0}\" already exists, please choose a different name", "태그 \"{0}\"는 이미 존재합니다. 다른 이름을 선택하세요");
		m.put("Tag \"{0}\" already exists, please choose a different name.", "태그 \"{0}\"는 이미 존재합니다. 다른 이름을 선택하세요.");
		m.put("Tag \"{0}\" created", "태그 \"{0}\"가 생성되었습니다");
		m.put("Tag \"{0}\" deleted", "태그 \"{0}\"가 삭제되었습니다");
		m.put("Tag Message", "태그 메시지");
		m.put("Tag Name", "태그 이름");
		m.put("Tag Protection", "태그 보호");
		m.put("Tag creation", "태그 생성");
		m.put("Tags", "태그들");
		m.put("Target", "대상");
		m.put("Target Branches", "대상 브랜치");
		m.put("Target Docker Image", "대상 도커 이미지");
		m.put("Target File", "대상 파일");
		m.put("Target Path", "대상 경로");
		m.put("Target Project", "대상 프로젝트");
		m.put("Target branch no longer exists", "대상 브랜치가 더 이상 존재하지 않습니다");
		m.put("Target branch was fast-forwarded to source branch", "대상 브랜치가 소스 브랜치로 빠르게 병합되었습니다");
		m.put("Target branch will be fast-forwarded to source branch", "대상 브랜치가 소스 브랜치로 빠르게 병합될 예정입니다");
		m.put("Target containing spaces or starting with dash needs to be quoted", "공백이 포함되거나 대시로 시작하는 대상은 인용부호로 묶어야 합니다");
		m.put("Target or source branch is updated. Please try again", "대상 또는 소스 브랜치가 업데이트되었습니다. 다시 시도하세요");
		m.put("Task List", "작업 목록");
		m.put("Task list", "작업 목록");
		m.put("Tell user to reset password", "사용자에게 비밀번호 재설정을 요청하세요");
		m.put("Template Name", "템플릿 이름");
		m.put("Template saved", "템플릿이 저장되었습니다");
		m.put("Terminal close", "터미널 닫기");
		m.put("Terminal input", "터미널 입력");
		m.put("Terminal open", "터미널 열기");
		m.put("Terminal output", "터미널 출력");
		m.put("Terminal ready", "터미널 준비 완료");
		m.put("Terminal resize", "터미널 크기 조정");
		m.put("Test", "테스트");
		m.put("Test Case", "테스트 케이스");
		m.put("Test Cases", "테스트 케이스들");
		m.put("Test Settings", "테스트 설정");
		m.put("Test Suite", "테스트 스위트");
		m.put("Test Suites", "테스트 스위트들");
		m.put("Test importing from {0}", "{0}에서 테스트 가져오기");
		m.put("Test mail has been sent to {0}, please check your mail box", "{0}로 테스트 메일이 전송되었습니다. 메일함을 확인하세요");
		m.put("Test successful: authentication passed", "테스트 성공: 인증 통과");
		m.put("Test successful: authentication passed with below information retrieved:", "테스트 성공: 아래 정보가 검색된 상태로 인증 통과:");
		m.put("Text", "텍스트");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "웹훅 POST 요청을 받을 서버 엔드포인트의 URL");
		m.put("The change contains disallowed file type(s): {0}", "변경 사항에 허용되지 않는 파일 유형이 포함되어 있습니다: {0}");
		m.put("The first board will be the default board", "첫 번째 보드가 기본 보드가 됩니다");
		m.put("The first timesheet will be the default timesheet", "첫 번째 타임시트가 기본 타임시트가 됩니다");
		m.put("The object you are deleting/disabling is still being used", "삭제/비활성화하려는 객체가 여전히 사용 중입니다");
		m.put("The password reset url is invalid or obsolete", "비밀번호 재설정 URL이 유효하지 않거나 오래되었습니다");
		m.put("The permission to access build log", "빌드 로그에 접근할 권한");
		m.put("The permission to access build pipeline", "빌드 파이프라인에 접근할 권한");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"작업을 수동으로 실행할 권한. 이는 빌드 로그, 빌드 파이프라인 및 모든 게시된 보고서에 접근할 권한을 포함합니다");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"POST 요청이 OneDev에서 페이로드 URL로 전송되었는지 확인할 수 있는 비밀 키입니다. 비밀 키를 설정하면 웹훅 POST 요청에서 X-OneDev-Signature 헤더를 받게 됩니다");
		m.put("The service desk feature enables user to create issues by sending emails to OneDev. Issues can be discussed over email completely, without the need of logging to OneDev.", 
			"서비스 데스크 기능을 사용하면 사용자가 이메일을 통해 OneDev에 이슈를 생성할 수 있습니다. 이슈는 OneDev에 로그인하지 않고도 이메일로 완전히 논의될 수 있습니다.");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "TOTP 인증기에 표시된 패스코드를 입력하여 확인하세요");
		m.put("Then publish package from project directory like below", "아래와 같이 프로젝트 디렉토리에서 패키지를 게시하세요");
		m.put("Then push gem to the source", "소스에 gem을 푸시하세요");
		m.put("Then push image to desired repository under specified project", "지정된 프로젝트 아래 원하는 저장소로 이미지를 푸시하세요");
		m.put("Then push package to the source", "소스에 패키지를 푸시하세요");
		m.put("Then resolve dependency via command step", "명령 단계에서 종속성을 해결하세요");
		m.put("Then upload package to the repository with twine", "twine을 사용하여 저장소에 패키지를 업로드하세요");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"브랜치 <span wicket:id=\"branch\"></span>에 대해 <a wicket:id=\"openRequests\">열린 풀 요청</a>이 있습니다. 브랜치가 삭제되면 이러한 풀 요청은 폐기됩니다.");
		m.put("There are incompatibilities since your upgraded version", "업그레이드된 버전 이후로 호환되지 않는 부분이 있습니다");
		m.put("There are merge conflicts", "병합 충돌이 있습니다");
		m.put("There are merge conflicts.", "병합 충돌이 있습니다.");
		m.put("There are merge conflicts. You can still create the pull request though", "병합 충돌이 있습니다. 그래도 풀 요청을 생성할 수 있습니다");
		m.put("There are unsaved changes, discard and continue?", "저장되지 않은 변경 사항이 있습니다. 폐기하고 계속하시겠습니까?");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"이 인증기는 일반적으로 모바일 폰에서 실행됩니다. Google Authenticator, Microsoft Authenticator, Authy, 1Password 등이 예입니다.");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"이 <span wicket:id=\"elementTypeName\"></span>는 <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>에서 가져왔습니다");
		m.put("This Month", "이번 달");
		m.put("This Week", "이번 주");
		m.put("This account is disabled", "이 계정은 비활성화되었습니다");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"이 주소는 SendGrid에서 <code>확인된 발신자</code>로 설정되어야 하며 다양한 이메일 알림의 발신 주소로 사용됩니다. 아래에서 <code>게시된 이메일 수신</code> 옵션이 활성화된 경우, 이 주소로 회신하여 이슈 또는 풀 요청 댓글을 게시할 수도 있습니다.");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"이 주소는 다양한 이메일 알림의 발신 주소로 사용됩니다. 아래에서 <code>수신 이메일 확인</code> 옵션이 활성화된 경우, 이 주소로 회신하여 이메일을 통해 이슈 또는 풀 요청 댓글을 게시할 수도 있습니다.");
		m.put("This change is already opened for merge by pull request {0}", "이 변경 사항은 풀 요청 {0}에 의해 이미 병합이 열려 있습니다");
		m.put("This change is squashed/rebased onto base branch via a pull request", "이 변경 사항은 풀 요청을 통해 기본 브랜치에 스쿼시/리베이스되었습니다");
		m.put("This change is squashed/rebased onto base branch via pull request {0}", "이 변경 사항은 풀 요청 {0}을 통해 기본 브랜치에 스쿼시/리베이스되었습니다");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "이 변경 사항은 일부 작업에 의해 확인되어야 합니다. 대신 풀 요청을 제출하세요");
		m.put("This commit is rebased", "이 커밋은 리베이스되었습니다");
		m.put("This date is using <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 format</a>", 
			"이 날짜는 <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 형식</a>을 사용합니다");
		m.put("This email address is being used", "이 이메일 주소는 사용 중입니다");
		m.put("This executor runs build jobs as docker containers on OneDev server", "이 실행자는 OneDev 서버에서 Docker 컨테이너로 빌드 작업을 실행합니다");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"이 실행자는 <a href='/~administration/agents' target='_blank'>에이전트</a>를 통해 원격 머신에서 Docker 컨테이너로 빌드 작업을 실행합니다");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"이 실행자는 Kubernetes 클러스터에서 Pod로 빌드 작업을 실행합니다. 에이전트가 필요하지 않습니다.<b class='text-danger'>참고:</b> 작업 Pod가 소스 및 아티팩트를 다운로드하기 위해 시스템 설정에서 서버 URL이 올바르게 지정되었는지 확인하세요");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs", 
			"이 실행자는 OneDev 서버의 셸 기능을 사용하여 빌드 작업을 실행합니다.<br><b class='text-danger'>경고</b>: 이 실행자를 사용하는 작업은 OneDev 서버 프로세스와 동일한 권한을 가집니다. 신뢰할 수 있는 작업에서만 사용되도록 해야 합니다");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. Make sure it can only be used by trusted jobs", 
			"이 실행자는 <a href='/~administration/agents' target='_blank'>에이전트</a>를 통해 원격 머신의 셸 기능을 사용하여 빌드 작업을 실행합니다<br><b class='text-danger'>경고</b>: 이 실행자를 사용하는 작업은 OneDev 에이전트 프로세스와 동일한 권한을 가집니다. 신뢰할 수 있는 작업에서만 사용되도록 해야 합니다");
		m.put("This field is required", "이 필드는 필수입니다");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"이 필터는 현재 사용자의 LDAP 항목을 결정하는 데 사용됩니다. 예: <i>(&(uid={0})(objectclass=person))</i>. 이 예에서 <i>{0}</i>는 현재 사용자의 로그인 이름을 나타냅니다.");
		m.put("This installation does not have an active subscription and runs as community edition. To access <a href=\"https://onedev.io/pricing\">enterprise features</a>, an active subscription is required", 
			"이 설치는 활성 구독이 없으며 커뮤니티 에디션으로 실행됩니다. <a href=\"https://onedev.io/pricing\">엔터프라이즈 기능</a>에 액세스하려면 활성 구독이 필요합니다");
		m.put("This installation has a trial subscription and is now running as enterprise edition", 
			"이 설치는 체험 구독을 가지고 있으며 현재 엔터프라이즈 에디션으로 실행 중입니다");
		m.put("This installation has an active subscription and runs as enterprise edition", "이 설치는 활성 구독을 가지고 있으며 엔터프라이즈 에디션으로 실행됩니다");
		m.put("This installation has an expired subscription, and runs as community edition", "이 설치는 구독이 만료되었으며 커뮤니티 에디션으로 실행됩니다");
		m.put("This installation has an unlimited users subscription and is now running as enterprise edition", 
			"이 설치는 무제한 사용자 구독을 가지고 있으며 현재 엔터프라이즈 에디션으로 실행 중입니다");
		m.put("This installation's subscription has expired and is now running as the community edition", 
			"이 설치의 구독이 만료되어 현재 커뮤니티 에디션으로 실행 중입니다");
		m.put("This is a Git LFS object, but the storage file is missing", "이것은 Git LFS 객체이지만 저장 파일이 누락되었습니다");
		m.put("This is a built-in role and can not be deleted", "이것은 내장된 역할이며 삭제할 수 없습니다");
		m.put("This is a disabled service account", "이것은 비활성화된 서비스 계정입니다");
		m.put("This is a layer cache. To use the cache, add below option to your docker buildx command", 
			"이것은 레이어 캐시입니다. 캐시를 사용하려면 아래 옵션을 Docker buildx 명령에 추가하세요");
		m.put("This is a service account for task automation purpose", "이것은 작업 자동화를 위한 서비스 계정입니다");
		m.put("This is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"이것은 엔터프라이즈 기능입니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>");
		m.put("This key has already been used by another project", "이 키는 이미 다른 프로젝트에서 사용되었습니다");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"이 키는 {0}와 연결되어 있지만 이 사용자의 확인된 이메일 주소가 아닙니다");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here.<br><b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated from these lock files with the <b>generate checksum step</b> defined before this step", 
			"이 키는 프로젝트 계층 구조에서 캐시 적중 여부를 결정하는 데 사용됩니다 (현재 프로젝트에서 루트 프로젝트로 순서대로 검색, 아래 로드 키도 동일). 캐시는 여기에 정의된 키와 정확히 동일할 경우 적중된 것으로 간주됩니다.<br><b>참고:</b> 프로젝트에 캐시 상태를 나타낼 수 있는 잠금 파일(package.json, pom.xml 등)이 있는 경우, 이 키는 &lt;cache name&gt;-@file:checksum.txt@로 정의되어야 하며, checksum.txt는 이 단계 전에 정의된 <b>체크섬 생성 단계</b>를 통해 이러한 잠금 파일에서 생성됩니다.");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"이 키는 프로젝트 계층 구조에서 캐시를 다운로드하고 업로드하는 데 사용됩니다 (현재 프로젝트에서 루트 프로젝트까지 순서대로 검색)");
		m.put("This key or one of its sub key is already added", "이 키 또는 하위 키 중 하나가 이미 추가되었습니다");
		m.put("This key or one of its subkey is already in use", "이 키 또는 하위 키 중 하나가 이미 사용 중입니다");
		m.put("This line has confusable unicode character modification", "이 줄에는 혼동 가능한 유니코드 문자 수정이 포함되어 있습니다");
		m.put("This might happen when project points to a wrong git repository, or the commit is garbage collected.", 
			"이 문제는 프로젝트가 잘못된 Git 저장소를 가리키거나 커밋이 가비지 수집된 경우 발생할 수 있습니다.");
		m.put("This might happen when project points to a wrong git repository, or these commits are garbage collected.", 
			"이 문제는 프로젝트가 잘못된 Git 저장소를 가리키거나 이러한 커밋이 가비지 수집된 경우 발생할 수 있습니다.");
		m.put("This name has already been used by another board", "이 이름은 이미 다른 보드에서 사용되었습니다");
		m.put("This name has already been used by another group", "이 이름은 이미 다른 그룹에서 사용되었습니다");
		m.put("This name has already been used by another issue board in the project", "이 이름은 프로젝트의 다른 이슈 보드에서 이미 사용되었습니다");
		m.put("This name has already been used by another job executor", "이 이름은 이미 다른 작업 실행자에서 사용되었습니다");
		m.put("This name has already been used by another project", "이 이름은 이미 다른 프로젝트에서 사용되었습니다");
		m.put("This name has already been used by another provider", "이 이름은 이미 다른 제공자에서 사용되었습니다");
		m.put("This name has already been used by another role", "이 이름은 이미 다른 역할에서 사용되었습니다");
		m.put("This name has already been used by another role.", "이 이름은 이미 다른 역할에서 사용되었습니다.");
		m.put("This name has already been used by another script", "이 이름은 이미 다른 스크립트에서 사용되었습니다");
		m.put("This name has already been used by another state", "이 이름은 이미 다른 상태에서 사용되었습니다");
		m.put("This operation is disallowed by branch protection rule", "이 작업은 브랜치 보호 규칙에 의해 허용되지 않습니다");
		m.put("This page lists changes since previous build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a>", 
			"이 페이지는 <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">동일한 스트림</a>에서 이전 빌드 이후의 변경 사항을 나열합니다");
		m.put("This page lists recent commits fixing the issue", "이 페이지는 문제를 수정한 최근 커밋을 나열합니다");
		m.put("This permission enables one to access confidential issues", "이 권한은 기밀 이슈에 액세스할 수 있도록 합니다");
		m.put("This permission enables one to schedule issues into iterations", "이 권한은 이슈를 반복 작업에 일정으로 추가할 수 있도록 합니다");
		m.put("This property is imported from {0}", "이 속성은 {0}에서 가져왔습니다");
		m.put("This pull request has been discarded", "이 풀 리퀘스트는 폐기되었습니다");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"이 보고서는 빌드가 풀 리퀘스트에 의해 트리거된 경우 풀 리퀘스트 개요 페이지에 표시됩니다");
		m.put("This server is currently accessed via http protocol, please configure your docker daemon or buildx builder to <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">work with insecure registry</a>", 
			"이 서버는 현재 HTTP 프로토콜을 통해 액세스되고 있습니다. Docker 데몬 또는 buildx 빌더를 <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">비보안 레지스트리와 함께 작동하도록 구성</a>하세요");
		m.put("This shows average duration of different states over time", "이 그래프는 시간에 따른 다양한 상태의 평균 지속 시간을 보여줍니다");
		m.put("This shows average duration of merged pull requests over time", "이 그래프는 시간에 따른 병합된 풀 리퀘스트의 평균 지속 시간을 보여줍니다");
		m.put("This shows number of <b>new</b> issues in different states over time", "이 그래프는 시간에 따른 <b>새로운</b> 이슈의 수를 다양한 상태에서 보여줍니다");
		m.put("This shows number of issues in various states over time", "이 그래프는 시간에 따른 다양한 상태의 이슈 수를 보여줍니다");
		m.put("This shows number of open and merged pull requests over time", "이 그래프는 시간에 따른 열려 있는 풀 리퀘스트와 병합된 풀 리퀘스트의 수를 보여줍니다");
		m.put("This step can only be executed by a docker aware executor", "이 단계는 Docker를 인식하는 실행자만 실행할 수 있습니다");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"이 단계는 Docker를 인식하는 실행자만 실행할 수 있습니다. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>작업 워크스페이스</a>에서 실행됩니다");
		m.put("This step copies files from job workspace to build artifacts directory, so that they can be accessed after job is completed", 
			"이 단계는 작업 공간에서 빌드 아티팩트 디렉토리로 파일을 복사하여 작업이 완료된 후에도 액세스할 수 있도록 합니다");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"이 단계는 지정된 파일을 프로젝트 웹 사이트로 제공하도록 게시합니다. 프로젝트 웹 사이트는 <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>를 통해 공개적으로 액세스할 수 있습니다");
		m.put("This step pulls specified refs from remote", "이 단계는 원격에서 지정된 참조를 가져옵니다");
		m.put("This step pushes current commit to same ref on remote", "이 단계는 현재 커밋을 원격의 동일한 참조로 푸시합니다");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"이 단계는 Renovate 캐시를 설정합니다. Renovate 단계를 사용하려면 이 단계를 그 전에 배치하세요");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"이 단계는 다양한 스캐너 단계의 속도를 높이기 위해 trivy db 캐시를 설정합니다. 스캐너 단계를 사용하려면 이 단계를 그 전에 배치하세요");
		m.put("This subscription key was already used", "이 구독 키는 이미 사용되었습니다");
		m.put("This subscription key was expired", "이 구독 키는 만료되었습니다");
		m.put("This tab shows the pipeline containing current build. Check <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">this article</a> to understand how build pipeline works", 
			"이 탭은 현재 빌드를 포함하는 파이프라인을 보여줍니다. 빌드 파이프라인이 어떻게 작동하는지 이해하려면 <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">이 기사</a>를 확인하세요");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"이 트리거는 태그된 커밋이 여기에서 지정된 브랜치에서 도달 가능한 경우에만 적용됩니다. 여러 브랜치는 공백으로 구분해야 합니다. <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매치</a>를 위해 '**', '*' 또는 '?'를 사용하세요. 제외하려면 '-'로 시작하세요. 모든 브랜치를 매치하려면 비워 두세요");
		m.put("This user is authenticating via external system.", "이 사용자는 외부 시스템을 통해 인증 중입니다.");
		m.put("This user is authenticating via internal database.", "이 사용자는 내부 데이터베이스를 통해 인증 중입니다.");
		m.put("This user is currently authenticating via external system. Setting password will switch to use internal database", 
			"이 사용자는 현재 외부 시스템을 통해 인증 중입니다. 비밀번호를 설정하면 내부 데이터베이스를 사용하도록 전환됩니다");
		m.put("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?", 
			"이 작업은 현재 구독을 비활성화하며 모든 엔터프라이즈 기능이 비활성화됩니다. 계속하시겠습니까?");
		m.put("This will discard all project specific boards, do you want to continue?", "이 작업은 프로젝트별 보드를 모두 삭제합니다. 계속하시겠습니까?");
		m.put("This will restart SSH server. Do you want to continue?", "이 작업은 SSH 서버를 재시작합니다. 계속하시겠습니까?");
		m.put("Threads", "스레드");
		m.put("Time Estimate Issue Field", "시간 추정 이슈 필드");
		m.put("Time Range", "시간 범위");
		m.put("Time Spent Issue Field", "소요 시간 이슈 필드");
		m.put("Time Tracking", "시간 추적");
		m.put("Time Tracking Setting", "시간 추적 설정");
		m.put("Time Tracking Settings", "시간 추적 설정");
		m.put("Time tracking settings have been saved", "시간 추적 설정이 저장되었습니다");
		m.put("Timed out", "시간 초과됨");
		m.put("Timeout", "시간 초과");
		m.put("Timesheet", "타임시트");
		m.put("Timesheet Setting", "타임시트 설정");
		m.put("Timesheets", "타임시트");
		m.put("Timing", "타이밍");
		m.put("Title", "제목");
		m.put("To Everyone", "모두에게");
		m.put("To State", "상태로");
		m.put("To States", "상태들로");
		m.put("To authenticate via internal database, <a wicket:id=\"setPasswordForUser\">set password for user</a> or <a wicket:id=\"tellUserToResetPassword\">tell user to reset password</a>", 
			"내부 데이터베이스를 통해 인증하려면, <a wicket:id=\"setPasswordForUser\">사용자의 비밀번호를 설정</a>하거나 <a wicket:id=\"tellUserToResetPassword\">사용자에게 비밀번호 재설정을 요청</a>하세요");
		m.put("To avoid duplication, estimated/remaining time showing here does not include those aggregated from \"{0}\"", 
			"중복을 피하기 위해 여기 표시된 추정/남은 시간은 \"{0}\"에서 집계된 시간을 포함하지 않습니다");
		m.put("To avoid duplication, spent time showing here does not include those aggregated from \"{0}\"", 
			"중복을 피하기 위해 여기 표시된 소요 시간은 \"{0}\"에서 집계된 시간을 포함하지 않습니다");
		m.put("Toggle change history", "변경 기록 토글");
		m.put("Toggle comments", "댓글 토글");
		m.put("Toggle commits", "커밋 토글");
		m.put("Toggle dark mode", "다크 모드 토글");
		m.put("Toggle detail message", "상세 메시지 토글");
		m.put("Toggle fixed width font", "고정 폭 글꼴 토글");
		m.put("Toggle full screen", "전체 화면 토글");
		m.put("Toggle matched contents", "일치하는 내용 토글");
		m.put("Toggle navigation", "탐색 토글");
		m.put("Toggle work log", "작업 로그 토글");
		m.put("Tokens", "토큰");
		m.put("Too many commits to load", "로드할 커밋이 너무 많습니다");
		m.put("Too many commits, displaying recent {0}", "커밋이 너무 많아 최근 {0}개를 표시합니다");
		m.put("Too many log entries, displaying recent {0}", "로그 항목이 너무 많아 최근 {0}개를 표시합니다");
		m.put("Too many problems, displaying first {0}", "문제가 너무 많아 처음 {0}개를 표시합니다");
		m.put("Toomanyrequests", "요청이 너무 많습니다");
		m.put("Top", "상단");
		m.put("Topo", "지형");
		m.put("Total Heap Memory", "총 힙 메모리");
		m.put("Total Number", "총 개수");
		m.put("Total Problems", "총 문제");
		m.put("Total Size", "총 크기");
		m.put("Total Test Duration", "총 테스트 시간");
		m.put("Total estimated time", "총 예상 시간");
		m.put("Total spent time", "총 소요 시간");
		m.put("Total spent time / total estimated time", "총 소요 시간 / 총 예상 시간");
		m.put("Total time", "총 시간");
		m.put("Total:", "총합:");
		m.put("Touched File", "수정된 파일");
		m.put("Touched Files", "수정된 파일들");
		m.put("Transfer LFS Files", "LFS 파일 전송");
		m.put("Transit manually", "수동으로 전환");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "문제 \"{0}\"의 상태를 \"{1}\"로 전환했습니다 ({2})");
		m.put("Transition Edit Bean", "전환 편집 빈");
		m.put("Transition Spec", "전환 사양");
		m.put("Trial Expiration Date", "체험판 만료 날짜");
		m.put("Trial subscription key not applicable for this installation", "이 설치에 체험판 구독 키를 적용할 수 없습니다");
		m.put("Triggers", "트리거");
		m.put("Trivy Container Image Scanner", "Trivy 컨테이너 이미지 스캐너");
		m.put("Trivy Filesystem Scanner", "Trivy 파일 시스템 스캐너");
		m.put("Trivy Rootfs Scanner", "Trivy Rootfs 스캐너");
		m.put("Try EE", "EE 체험하기");
		m.put("Try Enterprise Edition", "엔터프라이즈 에디션 체험하기");
		m.put("Twitch", "Twitch");
		m.put("Two Factor Authentication", "이중 인증");
		m.put("Two-factor Authentication", "이중 인증");
		m.put("Two-factor authentication already set up. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Request to set up again", 
			"이중 인증이 이미 설정되었습니다. <a wicket:id=\"requestToSetupAgain\"><wicket:t>다시 설정 요청");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"이중 인증이 활성화되었습니다. TOTP 인증기에 표시된 암호를 입력하세요. 문제가 발생하면 OneDev 서버와 TOTP 인증기를 실행 중인 장치의 시간이 동기화되어 있는지 확인하세요");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"보안을 강화하기 위해 계정에 이중 인증이 적용됩니다. 아래 절차를 따라 설정하세요");
		m.put("Two-factor authentication is now configured", "이중 인증이 이제 구성되었습니다");
		m.put("Two-factor authentication not enabled", "이중 인증이 활성화되지 않음");
		m.put("Type", "유형");
		m.put("Type <code>yes</code> below to cancel all queried builds", "아래에 <code>yes</code>를 입력하여 모든 조회된 빌드를 취소하세요");
		m.put("Type <code>yes</code> below to cancel selected builds", "아래에 <code>yes</code>를 입력하여 선택된 빌드를 취소하세요");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "아래에 <code>yes</code>를 입력하여 모든 조회된 사용자를 삭제하는 것을 확인하세요");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "아래에 <code>yes</code>를 입력하여 선택된 사용자를 삭제하는 것을 확인하세요");
		m.put("Type <code>yes</code> below to copy all queried issues to project \"{0}\"", "아래에 <code>yes</code>를 입력하여 모든 조회된 문제를 프로젝트 \"{0}\"로 복사하세요");
		m.put("Type <code>yes</code> below to copy selected issues to project \"{0}\"", "아래에 <code>yes</code>를 입력하여 선택된 문제를 프로젝트 \"{0}\"로 복사하세요");
		m.put("Type <code>yes</code> below to delete all queried builds", "아래에 <code>yes</code>를 입력하여 모든 조회된 빌드를 삭제하세요");
		m.put("Type <code>yes</code> below to delete all queried comments", "아래에 <code>yes</code>를 입력하여 모든 조회된 댓글을 삭제하세요");
		m.put("Type <code>yes</code> below to delete all queried issues", "아래에 <code>yes</code>를 입력하여 모든 조회된 문제를 삭제하세요");
		m.put("Type <code>yes</code> below to delete all queried packages", "아래에 <code>yes</code>를 입력하여 모든 조회된 패키지를 삭제하세요");
		m.put("Type <code>yes</code> below to delete all queried projects", "아래에 <code>yes</code>를 입력하여 모든 조회된 프로젝트를 삭제하세요");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "아래에 <code>yes</code>를 입력하여 모든 조회된 풀 리퀘스트를 삭제하세요");
		m.put("Type <code>yes</code> below to delete selected builds", "아래에 <code>yes</code>를 입력하여 선택된 빌드를 삭제하세요");
		m.put("Type <code>yes</code> below to delete selected comments", "아래에 <code>yes</code>를 입력하여 선택된 댓글을 삭제하세요");
		m.put("Type <code>yes</code> below to delete selected issues", "아래에 <code>yes</code>를 입력하여 선택된 문제를 삭제하세요");
		m.put("Type <code>yes</code> below to delete selected packages", "아래에 <code>yes</code>를 입력하여 선택된 패키지를 삭제하세요");
		m.put("Type <code>yes</code> below to delete selected projects", "아래에 <code>yes</code>를 입력하여 선택된 프로젝트를 삭제하세요");
		m.put("Type <code>yes</code> below to delete selected pull requests", "아래에 <code>yes</code>를 입력하여 선택된 풀 리퀘스트를 삭제하세요");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "아래에 <code>yes</code>를 입력하여 모든 조회된 풀 리퀘스트를 폐기하세요");
		m.put("Type <code>yes</code> below to discard selected pull requests", "아래에 <code>yes</code>를 입력하여 선택된 풀 리퀘스트를 폐기하세요");
		m.put("Type <code>yes</code> below to move all queried issues to project \"{0}\"", "아래에 <code>yes</code>를 입력하여 모든 조회된 문제를 프로젝트 \"{0}\"로 이동하세요");
		m.put("Type <code>yes</code> below to move all queried projects to be under \"{0}\"", "아래에 <code>yes</code>를 입력하여 모든 조회된 프로젝트를 \"{0}\" 아래로 이동하세요");
		m.put("Type <code>yes</code> below to move selected issues to project \"{0}\"", "아래에 <code>yes</code>를 입력하여 선택된 문제를 프로젝트 \"{0}\"로 이동하세요");
		m.put("Type <code>yes</code> below to move selected projects to be under \"{0}\"", "아래에 <code>yes</code>를 입력하여 선택된 프로젝트를 \"{0}\" 아래로 이동하세요");
		m.put("Type <code>yes</code> below to pause all queried agents", "아래에 <code>yes</code>를 입력하여 모든 조회된 에이전트를 일시 중지하세요");
		m.put("Type <code>yes</code> below to re-run all queried builds", "아래에 <code>yes</code>를 입력하여 모든 조회된 빌드를 다시 실행하세요");
		m.put("Type <code>yes</code> below to re-run selected builds", "아래에 <code>yes</code>를 입력하여 선택된 빌드를 다시 실행하세요");
		m.put("Type <code>yes</code> below to remove all queried users from group", "그룹에서 조회된 모든 사용자를 제거하려면 아래에 <code>yes</code>를 입력하세요");
		m.put("Type <code>yes</code> below to remove from all queried groups", "조회된 모든 그룹에서 제거하려면 아래에 <code>yes</code>를 입력하세요");
		m.put("Type <code>yes</code> below to remove from selected groups", "선택된 그룹에서 제거하려면 아래에 <code>yes</code>를 입력하세요");
		m.put("Type <code>yes</code> below to remove selected users from group", "그룹에서 선택된 사용자를 제거하려면 아래에 <code>yes</code>를 입력하세요");
		m.put("Type <code>yes</code> below to restart all queried agents", "아래에 <code>yes</code>를 입력하여 모든 조회된 에이전트를 재시작하세요");
		m.put("Type <code>yes</code> below to restart selected agents", "아래에 <code>yes</code>를 입력하여 선택된 에이전트를 재시작하세요");
		m.put("Type <code>yes</code> below to resume all queried agents", "아래에 <code>yes</code>를 입력하여 모든 조회된 에이전트를 다시 시작하세요");
		m.put("Type <code>yes</code> below to set all queried as root projects", "아래에 <code>yes</code>를 입력하여 모든 조회된 프로젝트를 루트 프로젝트로 설정하세요");
		m.put("Type <code>yes</code> below to set selected as root projects", "아래에 <code>yes</code>를 입력하여 선택된 프로젝트를 루트 프로젝트로 설정하세요");
		m.put("Type password here", "여기에 비밀번호를 입력하세요");
		m.put("Type to filter", "필터링하려면 입력하세요");
		m.put("Type to filter...", "필터링하려면 입력하세요...");
		m.put("URL", "URL");
		m.put("Unable To Delete/Disable Right Now", "지금 삭제/비활성화할 수 없습니다");
		m.put("Unable to apply change as otherwise you will not be able to manage this project", "이 변경을 적용할 수 없습니다. 그렇지 않으면 이 프로젝트를 관리할 수 없게 됩니다");
		m.put("Unable to change password as you are authenticating via external system", "외부 시스템을 통해 인증 중이므로 비밀번호를 변경할 수 없습니다");
		m.put("Unable to comment", "댓글을 작성할 수 없습니다");
		m.put("Unable to connect to server", "서버에 연결할 수 없습니다");
		m.put("Unable to create protected branch", "보호된 브랜치를 생성할 수 없습니다");
		m.put("Unable to create protected tag", "보호된 태그를 생성할 수 없습니다");
		m.put("Unable to diff as some line is too long.", "일부 줄이 너무 길어 차이를 표시할 수 없습니다.");
		m.put("Unable to diff as the file is too large.", "파일이 너무 커서 차이를 표시할 수 없습니다.");
		m.put("Unable to find SSO provider: ", "SSO 제공자를 찾을 수 없습니다:");
		m.put("Unable to find agent {0}", "에이전트 {0}을(를) 찾을 수 없습니다");
		m.put("Unable to find build #{0} in project {1}", "프로젝트 {1}에서 빌드 #{0}을(를) 찾을 수 없습니다");
		m.put("Unable to find commit to import build spec (import project: {0}, import revision: {1})", 
			"빌드 사양을 가져올 커밋을 찾을 수 없습니다 (가져올 프로젝트: {0}, 가져올 리비전: {1})");
		m.put("Unable to find issue #{0} in project {1}", "프로젝트 {1}에서 문제 #{0}을(를) 찾을 수 없습니다");
		m.put("Unable to find project to import build spec: {0}", "빌드 사양을 가져올 프로젝트를 찾을 수 없습니다: {0}");
		m.put("Unable to find pull request #{0} in project {1}", "프로젝트 {1}에서 풀 리퀘스트 #{0}을(를) 찾을 수 없습니다");
		m.put("Unable to find timesheet: ", "타임시트를 찾을 수 없습니다:");
		m.put("Unable to get guilds info", "길드 정보를 가져올 수 없습니다");
		m.put("Unable to import build spec (import project: {0}, import revision: {1}): {2}", "빌드 사양을 가져올 수 없습니다 (가져올 프로젝트: {0}, 가져올 리비전: {1}): {2}");
		m.put("Unable to notify user as mail service is not configured", "메일 서비스가 구성되지 않아 사용자를 알릴 수 없습니다");
		m.put("Unable to send password reset email as mail service is not configured", "메일 서비스가 구성되지 않아 비밀번호 재설정 이메일을 보낼 수 없습니다");
		m.put("Unable to send verification email as mail service is not configured yet", "메일 서비스가 아직 구성되지 않아 확인 이메일을 보낼 수 없습니다");
		m.put("Unauthorize this user", "이 사용자를 인증 해제하세요");
		m.put("Unauthorized", "권한 없음");
		m.put("Undefined", "정의되지 않음");
		m.put("Undefined Field Resolution", "정의되지 않은 필드 해결");
		m.put("Undefined Field Value Resolution", "정의되지 않은 필드 값 해결");
		m.put("Undefined State Resolution", "정의되지 않은 상태 해결");
		m.put("Undefined custom field: ", "정의되지 않은 사용자 지정 필드:");
		m.put("Under which condition this step should run. <b>SUCCESSFUL</b> means all non-optional steps running before this step are successful", 
			"이 단계가 실행되어야 하는 조건. <b>성공</b>은 이 단계 이전에 실행된 모든 비선택적 단계가 성공적임을 의미합니다");
		m.put("Unexpected setting: {0}", "예상치 못한 설정: {0}");
		m.put("Unexpected ssh signature hash algorithm: ", "예상치 못한 ssh 서명 해시 알고리즘:");
		m.put("Unexpected ssh signature namespace: ", "예상치 못한 ssh 서명 네임스페이스:");
		m.put("Unified", "통합됨");
		m.put("Unified view", "통합 보기");
		m.put("Unit Test Statistics", "단위 테스트 통계");
		m.put("Unlimited", "무제한");
		m.put("Unlink this issue", "이 이슈의 연결 해제");
		m.put("Unordered List", "정렬되지 않은 목록");
		m.put("Unordered list", "정렬되지 않은 목록");
		m.put("Unpin this issue", "이 이슈 고정 해제");
		m.put("Unresolved", "미해결");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "프로젝트 \"{1}\"의 파일 \"{0}\"에 대한 미해결 댓글");
		m.put("Unscheduled", "일정 미지정");
		m.put("Unscheduled Issues", "일정 미지정 이슈");
		m.put("Unsolicited OIDC authentication response", "요청되지 않은 OIDC 인증 응답");
		m.put("Unsolicited OIDC response", "요청되지 않은 OIDC 응답");
		m.put("Unsolicited discord api response", "요청되지 않은 Discord API 응답");
		m.put("Unspecified", "미지정");
		m.put("Unsupported", "지원되지 않음");
		m.put("Unsupported ssh signature algorithm: ", "지원되지 않는 ssh 서명 알고리즘:");
		m.put("Unsupported ssh signature version: ", "지원되지 않는 ssh 서명 버전:");
		m.put("Unverified", "검증되지 않음");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "검증되지 않은 이메일 주소는 위 기능에 <b>적용되지 않습니다</b>");
		m.put("Unvote", "투표 취소");
		m.put("Unwatched. Click to watch", "관찰 취소됨. 클릭하여 관찰");
		m.put("Update", "업데이트");
		m.put("Update Dependencies via Renovate", "Renovate를 통해 종속성 업데이트");
		m.put("Update Source Branch", "소스 브랜치 업데이트");
		m.put("Update body", "본문 업데이트");
		m.put("Upload", "업로드");
		m.put("Upload Access Token Secret", "액세스 토큰 비밀 업로드");
		m.put("Upload Cache", "캐시 업로드");
		m.put("Upload Files", "파일 업로드");
		m.put("Upload Project Path", "프로젝트 경로 업로드");
		m.put("Upload Strategy", "업로드 전략");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "다크 모드 로고로 사용하기 위해 128x128 투명 png 파일 업로드");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "라이트 모드 로고로 사용하기 위해 128x128 투명 png 파일 업로드");
		m.put("Upload artifacts", "아티팩트 업로드");
		m.put("Upload avatar", "아바타 업로드");
		m.put("Upload should be less than {0} Mb", "업로드는 {0} Mb보다 작아야 합니다");
		m.put("Upload to Project", "프로젝트로 업로드");
		m.put("Uploaded Caches", "업로드된 캐시");
		m.put("Uploading file", "파일 업로드 중");
		m.put("Url", "URL");
		m.put("Use '*' for wildcard match", "와일드카드 매칭에 '*' 사용");
		m.put("Use '*' or '?' for wildcard match. Prefix with '-' to exclude", "와일드카드 매칭에 '*' 또는 '?' 사용. 제외하려면 '-'로 시작");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>에 '**', '*' 또는 '?' 사용");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>에 '**', '*' 또는 '?' 사용. 제외하려면 '-'로 시작");
		m.put("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>경로 와일드카드 매칭</a>에 '**', '*', 또는 '?' 사용");
		m.put("Use '\\' to escape brackets", "괄호를 이스케이프하려면 '\\' 사용");
		m.put("Use '\\' to escape quotes", "따옴표를 이스케이프하려면 '\\' 사용");
		m.put("Use @@ to reference scope in job commands to avoid being interpreted as variable", "변수로 해석되지 않도록 작업 명령에서 범위를 참조하려면 @@ 사용");
		m.put("Use Avatar Service", "아바타 서비스 사용");
		m.put("Use Default", "기본값 사용");
		m.put("Use Default Boards", "기본 보드 사용");
		m.put("Use For Git Operations", "Git 작업에 사용");
		m.put("Use Git in System Path", "시스템 경로에서 Git 사용");
		m.put("Use Hours And Minutes Only", "시간과 분만 사용");
		m.put("Use Specified Git", "지정된 Git 사용");
		m.put("Use Specified curl", "지정된 curl 사용");
		m.put("Use Step Template", "단계 템플릿 사용");
		m.put("Use curl in System Path", "시스템 경로에서 curl 사용");
		m.put("Use default", "기본값 사용");
		m.put("Use default storage class", "기본 스토리지 클래스 사용");
		m.put("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages", 
			"OneDev이 ${permission.equals(\"write\")? \"배포\": \"사용\"} 패키지를 알 수 있도록 사용자 이름으로 작업 토큰 사용");
		m.put("Use job token to tell OneDev the build publishing the package", "OneDev에 패키지를 게시하는 빌드를 알리기 위해 작업 토큰 사용");
		m.put("Use job token to tell OneDev the build pushing the chart", "OneDev에 차트를 푸시하는 빌드를 알리기 위해 작업 토큰 사용");
		m.put("Use job token to tell OneDev the build pushing the package", "OneDev에 패키지를 푸시하는 빌드를 알리기 위해 작업 토큰 사용");
		m.put("Use job token to tell OneDev the build using the package", "OneDev에 패키지를 사용하는 빌드를 알리기 위해 작업 토큰 사용");
		m.put("Use project dependency to retrieve artifacts from other projects", "다른 프로젝트에서 아티팩트를 검색하기 위해 프로젝트 종속성 사용");
		m.put("Use specified choices", "지정된 선택 항목 사용");
		m.put("Use specified default value", "지정된 기본값 사용");
		m.put("Use specified value or job secret", "지정된 값 또는 작업 비밀 사용");
		m.put("Use specified values or job secrets", "지정된 값 또는 작업 비밀 사용");
		m.put("Use triggers to run the job automatically under certain conditions", "특정 조건에서 작업을 자동으로 실행하기 위해 트리거 사용");
		m.put("Use value of specified parameter/secret", "지정된 매개변수/비밀의 값 사용");
		m.put("Used Heap Memory", "사용된 힙 메모리");
		m.put("User", "사용자");
		m.put("User \"{0}\" unauthorized", "사용자 \"{0}\" 권한 없음");
		m.put("User Authorization Bean", "사용자 인증 Bean");
		m.put("User Authorizations", "사용자 인증");
		m.put("User Authorizations Bean", "사용자 인증 Bean");
		m.put("User Count", "사용자 수");
		m.put("User Email Attribute", "사용자 이메일 속성");
		m.put("User Full Name Attribute", "사용자 전체 이름 속성");
		m.put("User Groups Attribute", "사용자 그룹 속성");
		m.put("User Invitation", "사용자 초대");
		m.put("User Invitation Template", "사용자 초대 템플릿");
		m.put("User Management", "사용자 관리");
		m.put("User Match Criteria", "사용자 매칭 기준");
		m.put("User Name", "사용자 이름");
		m.put("User Principal Name", "사용자 주 이름");
		m.put("User Profile", "사용자 프로필");
		m.put("User SSH Key Attribute", "사용자 SSH 키 속성");
		m.put("User Search Bases", "사용자 검색 기준");
		m.put("User Search Filter", "사용자 검색 필터");
		m.put("User added to group", "사용자가 그룹에 추가되었습니다");
		m.put("User authorizations updated", "사용자 인증 업데이트됨");
		m.put("User authorized", "사용자 인증됨");
		m.put("User avatar will be requested by appending a hash to this url", "이 URL에 해시를 추가하여 사용자 아바타 요청");
		m.put("User can sign up if this option is enabled", "이 옵션이 활성화된 경우 사용자가 가입할 수 있음");
		m.put("User disabled", "사용자 비활성화됨");
		m.put("User name", "사용자 이름");
		m.put("User name already used by another account", "사용자 이름이 이미 다른 계정에서 사용 중입니다");
		m.put("Users", "사용자들");
		m.put("Users converted to service accounts successfully", "사용자가 서비스 계정으로 성공적으로 변환되었습니다");
		m.put("Users deleted successfully", "사용자가 성공적으로 삭제되었습니다");
		m.put("Users disabled successfully", "사용자가 성공적으로 비활성화되었습니다");
		m.put("Users enabled successfully", "사용자가 성공적으로 활성화되었습니다");
		m.put("Utilities", "유틸리티");
		m.put("Valid signature required for head commit of this branch per branch protection rule", 
			"브랜치 보호 규칙에 따라 이 브랜치의 헤드 커밋에 유효한 서명이 필요합니다");
		m.put("Value", "값");
		m.put("Value Matcher", "값 매처");
		m.put("Value Provider", "값 제공자");
		m.put("Values", "값들");
		m.put("Values Provider", "값 제공자");
		m.put("Variable", "변수");
		m.put("Verification Code", "확인 코드");
		m.put("Verification email sent, please check it", "확인 이메일이 발송되었습니다. 확인해 주세요");
		m.put("Verify", "확인");
		m.put("View", "보기");
		m.put("View Source", "소스 보기");
		m.put("View source", "소스 보기");
		m.put("View statistics", "통계 보기");
		m.put("Viewer", "뷰어");
		m.put("Volume Mount", "볼륨 마운트");
		m.put("Volume Mounts", "볼륨 마운트들");
		m.put("Vote", "투표");
		m.put("Votes", "투표들");
		m.put("WAITING", "대기 중");
		m.put("WARNING:", "경고:");
		m.put("Waiting", "대기 중");
		m.put("Waiting for approvals", "승인을 기다리는 중");
		m.put("Waiting for test mail to come back...", "테스트 메일이 돌아오기를 기다리는 중...");
		m.put("Watch", "관찰");
		m.put("Watch Status", "관찰 상태");
		m.put("Watch if involved", "관련된 경우 관찰");
		m.put("Watch if involved (default)", "관련된 경우 관찰 (기본값)");
		m.put("Watch status changed", "관찰 상태가 변경되었습니다");
		m.put("Watch/Unwatch All Queried Issues", "쿼리된 모든 문제 관찰/관찰 취소");
		m.put("Watch/Unwatch All Queried Pull Requests", "쿼리된 모든 풀 리퀘스트 관찰/관찰 취소");
		m.put("Watch/Unwatch Selected Pull Requests", "선택된 풀 리퀘스트 관찰/관찰 취소");
		m.put("Watched. Click to unwatch", "관찰 중입니다. 클릭하여 관찰 취소");
		m.put("Watchers", "관찰자들");
		m.put("Web Hook", "웹 훅");
		m.put("Web Hooks", "웹 훅들");
		m.put("Web Hooks Bean", "웹 훅 빈");
		m.put("Web hooks saved", "웹 훅이 저장되었습니다");
		m.put("Webhook Url", "웹훅 URL");
		m.put("Week", "주");
		m.put("When", "언제");
		m.put("When authorize a group, the group will also be authorized with the role for all child projects", 
			"그룹을 승인하면 해당 그룹은 모든 하위 프로젝트에 대해 역할이 승인됩니다");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"프로젝트를 승인하면 모든 하위 프로젝트에 지정된 역할이 승인됩니다");
		m.put("When authorize a user, the user will also be authorized with the role for all child projects", 
			"사용자를 승인하면 해당 사용자는 모든 하위 프로젝트에 대해 역할이 승인됩니다");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"사용자가 Git 커밋의 작성자/커미터인지 확인할 때 여기에 나열된 모든 이메일이 확인됩니다");
		m.put("When evaluating this template, below variables will be available:", "이 템플릿을 평가할 때 아래 변수가 사용 가능합니다:");
		m.put("When login via OneDev's built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database", 
			"OneDev의 내장된 양식을 통해 로그인할 때, 제출된 사용자 자격 증명은 내부 데이터베이스 외에도 여기 정의된 인증기를 통해 확인할 수 있습니다");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"풀 리퀘스트의 대상 브랜치에 새 커밋이 있을 경우, 풀 리퀘스트의 병합 커밋이 재계산되며, 이전 병합 커밋에서 실행된 빌드를 수락할지 여부를 결정합니다. 활성화되면 새 병합 커밋에서 필요한 빌드를 다시 실행해야 합니다. 이 설정은 필요한 빌드가 지정된 경우에만 적용됩니다");
		m.put("When this work starts", "이 작업이 시작될 때");
		m.put("When {0}", "{0}일 때");
		m.put("Whether or not created issue should be confidential", "생성된 문제가 비공개인지 여부");
		m.put("Whether or not multiple issues can be linked", "여러 문제를 연결할 수 있는지 여부");
		m.put("Whether or not multiple issues can be linked on the other side. For instance sub issues on the other side means parent issue, and multiple should be false on that side if only one parent is allowed", 
			"다른 쪽에서 여러 문제를 연결할 수 있는지 여부. 예를 들어, 다른 쪽에서 하위 문제는 상위 문제를 의미하며, 상위 문제가 하나만 허용되는 경우 해당 쪽에서 다중 연결은 false여야 합니다");
		m.put("Whether or not multiple values can be specified for this field", "이 필드에 대해 여러 값을 지정할 수 있는지 여부");
		m.put("Whether or not multiple values can be specified for this param", "이 매개변수에 대해 여러 값을 지정할 수 있는지 여부");
		m.put("Whether or not the issue should be confidential", "문제가 비공개인지 여부");
		m.put("Whether or not the link is asymmetric. A asymmetric link has different meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric", 
			"링크가 비대칭인지 여부. 비대칭 링크는 다른 쪽에서 다른 의미를 가집니다. 예를 들어 '상위-하위' 링크는 비대칭이며, '관련 있음' 링크는 대칭입니다");
		m.put("Whether or not this field accepts empty value", "이 필드가 빈 값을 허용하는지 여부");
		m.put("Whether or not this param accepts empty value", "이 매개변수가 빈 값을 허용하는지 여부");
		m.put("Whether or not this script can be used in CI/CD jobs", "이 스크립트를 CI/CD 작업에서 사용할 수 있는지 여부");
		m.put("Whether or not this step is optional. Execution failure of an optional step will not cause the build to fail, and successful condition of subsequent steps will not take optional step into account", 
			"이 단계가 선택 사항인지 여부. 선택 사항 단계의 실행 실패는 빌드 실패를 초래하지 않으며, 이후 단계의 성공 조건은 선택 사항 단계를 고려하지 않습니다");
		m.put("Whether or not to allow anonymous users to access this server", "익명 사용자가 이 서버에 접근할 수 있는지 여부");
		m.put("Whether or not to allow creating root projects (project without parent)", "루트 프로젝트(상위 프로젝트가 없는 프로젝트)를 생성할 수 있는지 여부");
		m.put("Whether or not to also include children of above projects", "위 프로젝트의 하위 프로젝트를 포함할지 여부");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same machine", 
			"컨테이너를 실행하거나 이미지를 빌드할 때 항상 이미지를 가져올지 여부. 이 옵션을 활성화하면 동일한 머신에서 실행되는 악성 작업이 이미지를 교체하는 것을 방지할 수 있습니다");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same node", 
			"컨테이너를 실행하거나 이미지를 빌드할 때 항상 이미지를 가져올지 여부. 이 옵션을 활성화하면 동일한 노드에서 실행되는 악성 작업이 이미지를 교체하는 것을 방지할 수 있습니다");
		m.put("Whether or not to be able to access time tracking info of issues", "문제의 시간 추적 정보를 액세스할 수 있는지 여부");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities", 
			"작업 자동화 목적으로 서비스 계정으로 생성할지 여부. 서비스 계정은 비밀번호와 이메일 주소가 없으며, 활동에 대한 알림을 생성하지 않습니다");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"작업 자동화 목적으로 서비스 계정으로 생성할지 여부. 서비스 계정은 비밀번호와 이메일 주소가 없으며, 활동에 대한 알림을 생성하지 않습니다. <b class='text-warning'>참고:</b> 서비스 계정은 엔터프라이즈 기능입니다. <a href='https://onedev.io/pricing' target='_blank'>30일 무료 체험</a>");
		m.put("Whether or not to enable code management for the project", "프로젝트에 대한 코드 관리를 활성화할지 여부");
		m.put("Whether or not to enable issue management for the project", "프로젝트에 대한 문제 관리를 활성화할지 여부");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project.", 
			"풀 리퀘스트가 다른 프로젝트에서 열렸을 경우 LFS 객체를 가져올지 여부");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project. If this option is enabled, git lfs command needs to be installed on OneDev server", 
			"풀 리퀘스트가 다른 프로젝트에서 열렸을 경우 LFS 객체를 가져올지 여부. 이 옵션이 활성화되면 OneDev 서버에 git lfs 명령이 설치되어야 합니다");
		m.put("Whether or not to import forked Bitbucket repositories", "포크된 Bitbucket 저장소를 가져올지 여부");
		m.put("Whether or not to import forked GitHub repositories", "포크된 GitHub 저장소를 가져올지 여부");
		m.put("Whether or not to import forked GitLab projects", "포크된 GitLab 프로젝트를 가져올지 여부");
		m.put("Whether or not to import forked Gitea repositories", "포크된 Gitea 저장소를 가져올지 여부");
		m.put("Whether or not to include forked repositories", "포크된 저장소를 포함할지 여부");
		m.put("Whether or not to include this field when issue is initially opened. If not, you may include this field later when issue is transited to other states via issue transition rule", 
			"문제가 처음 열릴 때 이 필드를 포함할지 여부. 포함하지 않을 경우, 문제가 다른 상태로 전환될 때 문제 전환 규칙을 통해 나중에 이 필드를 포함할 수 있습니다");
		m.put("Whether or not to input and display estimated/spent time in hours/minutes only", "예상/소요 시간을 시간/분 단위로만 입력하고 표시할지 여부");
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. Make sure this executor can only be used by trusted jobs if this option is enabled", 
			"작업 명령에서 도커 작업을 지원하기 위해 도커 소켓을 작업 컨테이너에 마운트할지 여부<br><b class='text-danger'>경고</b>: 악의적인 작업은 마운트된 도커 소켓을 조작하여 전체 OneDev를 제어할 수 있습니다. 이 옵션이 활성화된 경우 이 실행자가 신뢰할 수 있는 작업에서만 사용되도록 해야 합니다");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"다음 페이지에서 태그 매핑을 미리 채울지 여부. 표시할 태그가 너무 많을 경우 이 옵션을 비활성화할 수 있습니다");
		m.put("Whether or not to require this dependency to be successful", "이 종속성이 성공해야 하는지 여부");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"로그인 사용자의 그룹을 검색할지 여부. 이 옵션이 활성화된 경우, Entra ID에 등록된 앱의 토큰 구성에서 그룹 클레임을 추가해야 합니다. 이 경우 그룹 클레임은 다양한 토큰 유형을 통해 기본 옵션으로 그룹 ID를 반환해야 합니다");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"하위 모듈을 검색할지 여부. 위의 클론 자격 증명을 설정하는 방법에 대한 <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>이 튜토리얼</a>을 참조하세요");
		m.put("Whether or not to run this step inside container", "이 단계를 컨테이너 내부에서 실행할지 여부");
		m.put("Whether or not to scan recursively in above paths", "위 경로에서 재귀적으로 스캔할지 여부");
		m.put("Whether or not to send notifications for events generated by yourself", "자신이 생성한 이벤트에 대한 알림을 보낼지 여부");
		m.put("Whether or not to send notifications to issue watchers for this change", "이 변경 사항에 대해 문제 관찰자에게 알림을 보낼지 여부");
		m.put("Whether or not to show branch/tag column", "브랜치/태그 열을 표시할지 여부");
		m.put("Whether or not to show duration column", "지속 시간 열을 표시할지 여부");
		m.put("Whether or not to use user avatar from a public service", "공개 서비스에서 사용자 아바타를 사용할지 여부");
		m.put("Whether or not use force option to overwrite changes in case ref updating can not be fast-forwarded", 
			"참조 업데이트가 빠르게 진행될 수 없는 경우 변경 사항을 덮어쓰도록 강제 옵션을 사용할지 여부");
		m.put("Whether or not user can remove own account", "사용자가 자신의 계정을 제거할 수 있는지 여부");
		m.put("Whether the password must contain at least one lowercase letter", "비밀번호에 최소한 하나의 소문자가 포함되어야 하는지 여부");
		m.put("Whether the password must contain at least one number", "비밀번호에 최소한 하나의 숫자가 포함되어야 하는지 여부");
		m.put("Whether the password must contain at least one special character", "비밀번호에 최소한 하나의 특수 문자가 포함되어야 하는지 여부");
		m.put("Whether the password must contain at least one uppercase letter", "비밀번호에 최소한 하나의 대문자가 포함되어야 하는지 여부");
		m.put("Whole Word", "전체 단어");
		m.put("Widget", "위젯");
		m.put("Widget Tab", "위젯 탭");
		m.put("Widget Timesheet Setting", "위젯 타임시트 설정");
		m.put("Will be prompted to set up two-factor authentication upon next login", "다음 로그인 시 이중 인증 설정을 요청받게 됩니다");
		m.put("Will be transcoded to UTF-8", "UTF-8로 트랜스코딩됩니다");
		m.put("Window", "창");
		m.put("Window Memory", "창 메모리");
		m.put("With current number of users ({0}), the subscription will be active until <b>{1}</b>", 
			"현재 사용자 수 ({0})로 구독은 <b>{1}</b>까지 활성화됩니다");
		m.put("Workflow reconciliation completed", "워크플로우 조정 완료");
		m.put("Working Directory", "작업 디렉토리");
		m.put("Write", "쓰기");
		m.put("YAML", "YAML");
		m.put("Yes", "예");
		m.put("You are not member of discord server", "디스코드 서버의 멤버가 아닙니다");
		m.put("You are rebasing source branch on top of target branch", "소스 브랜치를 대상 브랜치 위로 리베이스 중입니다");
		m.put("You are viewing a subset of all changes. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">show all changes</a>", 
			"모든 변경 사항의 일부를 보고 있습니다. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">모든 변경 사항 보기</a>");
		m.put("You can also achieve this by adding a build docker image step to your CI/CD job and configuring the built-in registry login with an access token secret that has package write permissions", 
			"CI/CD 작업에 빌드 도커 이미지 단계를 추가하고 패키지 쓰기 권한이 있는 액세스 토큰 비밀로 내장 레지스트리 로그인을 구성하여 이를 달성할 수 있습니다");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "확인되지 않은 <a wicket:id=\"hasUnverifiedLink\">이메일 주소</a>가 있습니다");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "파일/이미지를 입력 상자에 드롭하거나 클립보드에서 이미지를 붙여넣을 수도 있습니다");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"프로젝트를 <a wicket:id=\"addFiles\" class=\"link-primary\">파일 추가</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">빌드 사양 설정</a>, 또는 <a wicket:id=\"pushInstructions\" class=\"link-primary\">기존 저장소 푸시</a>로 초기화할 수 있습니다");
		m.put("You selected to delete branch \"{0}\"", "브랜치 \"{0}\" 삭제를 선택했습니다");
		m.put("You will be notified of any activities", "활동에 대한 알림을 받게 됩니다");
		m.put("You've been logged out", "로그아웃되었습니다");
		m.put("YouTrack API URL", "YouTrack API URL");
		m.put("YouTrack Issue Field", "YouTrack 이슈 필드");
		m.put("YouTrack Issue Link", "YouTrack 이슈 링크");
		m.put("YouTrack Issue State", "YouTrack 이슈 상태");
		m.put("YouTrack Issue Tag", "YouTrack 이슈 태그");
		m.put("YouTrack Login Name", "YouTrack 로그인 이름");
		m.put("YouTrack Password or Access Token", "YouTrack 비밀번호 또는 액세스 토큰");
		m.put("YouTrack Project", "YouTrack 프로젝트");
		m.put("YouTrack Projects to Import", "YouTrack 가져올 프로젝트");
		m.put("Your email address is now verified", "이메일 주소가 이제 확인되었습니다");
		m.put("Your primary email address is not verified", "기본 이메일 주소가 확인되지 않았습니다");
		m.put("[Any state]", "[어떤 상태든]");
		m.put("[Reset Password] Please Reset Your OneDev Password", "[비밀번호 재설정] OneDev 비밀번호를 재설정하세요");
		m.put("a boolean indiciating whether or not topic comment can be created directly by replying the email", 
			"이메일에 직접 답글을 달아 주제 댓글을 생성할 수 있는지 여부를 나타내는 불리언 값");
		m.put("a new agent token will be generated each time this button is pressed", "이 버튼을 누를 때마다 새 에이전트 토큰이 생성됩니다");
		m.put("a string representing body of the event. May be <code>null</code>", "이벤트 본문을 나타내는 문자열. <code>null</code>일 수 있음");
		m.put("a string representing event detail url", "이벤트 세부 URL을 나타내는 문자열");
		m.put("a string representing summary of the event", "이벤트 요약을 나타내는 문자열");
		m.put("access [{0}]", "액세스 [{0}]");
		m.put("active", "활성");
		m.put("add another order", "다른 주문 추가");
		m.put("adding .onedev-buildspec.yml", ".onedev-buildspec.yml 추가 중");
		m.put("after specified date", "지정된 날짜 이후");
		m.put("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information.  A <code>null</code> value means that the notification can not be unsubscribed", 
			"구독 취소 정보를 보유한 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>객체</a>. <code>null</code> 값은 알림을 구독 취소할 수 없음을 의미합니다");
		m.put("and more", "그리고 더 많은 것");
		m.put("archived", "보관됨");
		m.put("artifacts", "아티팩트");
		m.put("assign to me", "나에게 할당");
		m.put("authored by", "작성자");
		m.put("backlog ", "백로그");
		m.put("base", "기본");
		m.put("before specified date", "지정된 날짜 이전");
		m.put("branch the build commit is merged into", "빌드 커밋이 병합된 브랜치");
		m.put("branch the job is running against", "작업이 실행 중인 브랜치");
		m.put("branch {0}", "브랜치 {0}");
		m.put("branches", "브랜치들");
		m.put("build", "빌드");
		m.put("build is successful for any job and branch", "어떤 작업과 브랜치에서도 빌드가 성공적임");
		m.put("build is successful for any job on branches \"{0}\"", "브랜치 \"{0}\"에서 어떤 작업에서도 빌드가 성공적임");
		m.put("build is successful for jobs \"{0}\" on any branch", "브랜치에 관계없이 작업 \"{0}\"에서 빌드가 성공적임");
		m.put("build is successful for jobs \"{0}\" on branches \"{1}\"", "브랜치 \"{1}\"에서 작업 \"{0}\"의 빌드가 성공적임");
		m.put("builds", "빌드들");
		m.put("cURL Example", "cURL 예제");
		m.put("choose a color for this state", "이 상태에 대한 색상을 선택하세요");
		m.put("cluster:lead", "리드");
		m.put("cmd-k to show command palette", "cmd-k를 눌러 명령 팔레트를 표시하세요");
		m.put("code commit", "코드 커밋");
		m.put("code is committed", "코드가 커밋되었습니다");
		m.put("code is committed to branches \"{0}\"", "브랜치 \"{0}\"에 코드가 커밋됨");
		m.put("code is committed to branches \"{0}\" with message \"{1}\"", "코드가 \"{0}\" 브랜치에 \"{1}\" 메시지로 커밋되었습니다");
		m.put("code is committed with message \"{0}\"", "코드가 \"{0}\" 메시지로 커밋되었습니다");
		m.put("commit message contains", "커밋 메시지 포함");
		m.put("commits", "커밋들");
		m.put("committed by", "작성자");
		m.put("common", "공통");
		m.put("common ancestor", "공통 조상");
		m.put("container:image", "이미지");
		m.put("copy", "복사");
		m.put("ctrl-k to show command palette", "ctrl-k를 눌러 명령 팔레트를 표시하세요");
		m.put("curl Command Line", "curl 명령줄");
		m.put("curl Path", "curl 경로");
		m.put("default", "기본값");
		m.put("descending", "내림차순");
		m.put("disabled", "비활성화됨");
		m.put("does not have any value of", "어떤 값도 포함하지 않음");
		m.put("duration", "지속 시간");
		m.put("enclose with ~ to query hash/message", "해시/메시지를 쿼리하려면 ~로 감싸세요");
		m.put("enclose with ~ to query job/version", "작업/버전을 쿼리하려면 ~로 감싸세요");
		m.put("enclose with ~ to query name/ip/os", "이름/IP/OS를 쿼리하려면 ~로 감싸세요");
		m.put("enclose with ~ to query name/path", "이름/경로를 쿼리하려면 ~로 감싸세요");
		m.put("enclose with ~ to query name/version", "이름/버전을 쿼리하려면 ~로 감싸세요");
		m.put("enclose with ~ to query path/content/reply", "경로/내용/답글을 쿼리하려면 ~로 감싸세요");
		m.put("enclose with ~ to query title/description/comment", "제목/설명/댓글을 쿼리하려면 ~로 감싸세요");
		m.put("exclude", "제외");
		m.put("false", "거짓");
		m.put("files with ext \"{0}\"", "확장자가 \"{0}\"인 파일들");
		m.put("find build by number", "번호로 빌드 찾기");
		m.put("find build with this number", "이 번호로 빌드 찾기");
		m.put("find issue by number", "번호로 이슈 찾기");
		m.put("find pull request by number", "번호로 풀 리퀘스트 찾기");
		m.put("find pull request with this number", "이 번호로 풀 리퀘스트 찾기");
		m.put("forked from <a wicket:id=\"forkedFrom\"></a>", "<a wicket:id=\"forkedFrom\"></a>에서 포크됨");
		m.put("found 1 agent", "1개의 에이전트를 찾았습니다");
		m.put("found 1 build", "1개의 빌드를 찾았습니다");
		m.put("found 1 comment", "1개의 댓글을 찾았습니다");
		m.put("found 1 issue", "1개의 이슈를 찾았습니다");
		m.put("found 1 package", "1개의 패키지를 찾았습니다");
		m.put("found 1 project", "1개의 프로젝트를 찾았습니다");
		m.put("found 1 pull request", "1개의 풀 리퀘스트를 찾았습니다");
		m.put("found 1 user", "1명의 사용자를 찾았습니다");
		m.put("found {0} agents", "{0}개의 에이전트를 찾았습니다");
		m.put("found {0} builds", "{0}개의 빌드를 찾았습니다");
		m.put("found {0} comments", "{0}개의 댓글을 찾았습니다");
		m.put("found {0} issues", "{0}개의 이슈를 찾았습니다");
		m.put("found {0} packages", "{0}개의 패키지를 찾았습니다");
		m.put("found {0} projects", "{0}개의 프로젝트를 찾았습니다");
		m.put("found {0} pull requests", "{0}개의 풀 리퀘스트를 찾았습니다");
		m.put("found {0} users", "{0}명의 사용자를 찾았습니다");
		m.put("has any value of", "값이 존재합니다");
		m.put("head", "헤드");
		m.put("in current commit", "현재 커밋에서");
		m.put("ineffective", "비효율적");
		m.put("inherited", "상속됨");
		m.put("initial", "초기");
		m.put("is empty", "비어 있음");
		m.put("is not empty", "비어 있지 않음");
		m.put("issue", "이슈");
		m.put("issue:Number", "번호");
		m.put("issues", "이슈들");
		m.put("job", "작업");
		m.put("key ID: ", "키 ID:");
		m.put("lines", "라인");
		m.put("link:Multiple", "다중");
		m.put("log", "로그");
		m.put("manage job", "작업 관리");
		m.put("markdown:heading", "헤딩");
		m.put("markdown:image", "이미지");
		m.put("may not be empty", "비어 있을 수 없습니다");
		m.put("merged", "병합됨");
		m.put("month:Apr", "4월");
		m.put("month:Aug", "8월");
		m.put("month:Dec", "12월");
		m.put("month:Feb", "2월");
		m.put("month:Jan", "1월");
		m.put("month:Jul", "7월");
		m.put("month:Jun", "6월");
		m.put("month:Mar", "3월");
		m.put("month:May", "5월");
		m.put("month:Nov", "11월");
		m.put("month:Oct", "10월");
		m.put("month:Sep", "9월");
		m.put("n/a", "해당 없음");
		m.put("new field", "새 필드");
		m.put("no activity for {0} days", "{0}일 동안 활동 없음");
		m.put("on file {0}", "파일 {0}에서");
		m.put("opened", "열림");
		m.put("opened <span wicket:id=\"submitDate\"></span>", "<span wicket:id=\"submitDate\"></span>에 열림");
		m.put("or match another value", "또는 다른 값과 일치");
		m.put("order more", "더 주문하기");
		m.put("outdated", "구식");
		m.put("pack", "팩");
		m.put("package", "패키지");
		m.put("packages", "패키지들");
		m.put("personal", "개인");
		m.put("pipeline", "파이프라인");
		m.put("project of the running job", "실행 중인 작업의 프로젝트");
		m.put("property", "속성");
		m.put("pull request", "풀 리퀘스트");
		m.put("pull request #{0}", "풀 리퀘스트 #{0}");
		m.put("pull request and code review", "풀 리퀘스트 및 코드 리뷰");
		m.put("pull request to any branch is discarded", "모든 브랜치에 대한 풀 리퀘스트가 폐기됨");
		m.put("pull request to any branch is merged", "모든 브랜치에 대한 풀 리퀘스트가 병합됨");
		m.put("pull request to any branch is opened", "모든 브랜치에 대한 풀 리퀘스트가 열림");
		m.put("pull request to branches \"{0}\" is discarded", "브랜치 \"{0}\"에 대한 풀 리퀘스트가 폐기됨");
		m.put("pull request to branches \"{0}\" is merged", "브랜치 \"{0}\"에 대한 풀 리퀘스트가 병합됨");
		m.put("pull request to branches \"{0}\" is opened", "브랜치 \"{0}\"에 대한 풀 리퀘스트가 열림");
		m.put("pull requests", "풀 리퀘스트들");
		m.put("reconciliation (need administrator permission)", "조정 (관리자 권한 필요)");
		m.put("reports", "보고서");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> object to be notified", 
			"알림을 받을 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>빌드</a> 객체를 나타냅니다");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk", 
			"서비스 데스크를 통해 열리는 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>이슈</a>를 나타냅니다");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified", 
			"알림을 받을 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>이슈</a> 객체를 나타냅니다");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> object to be notified", 
			"알림을 받을 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>패키지</a> 객체를 나타냅니다");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified", 
			"알림을 받을 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>풀 리퀘스트</a> 객체를 나타냅니다");
		m.put("represents the <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> object to be notified", 
			"알림을 받을 <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>커밋</a> 객체를 나타냅니다");
		m.put("represents the exception encountered when open issue via service desk", "서비스 데스크를 통해 이슈를 열 때 발생한 예외를 나타냅니다");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>", 
			"구독 취소된 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>이슈</a>를 나타냅니다");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>", 
			"구독 취소된 <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>풀 리퀘스트</a>를 나타냅니다");
		m.put("request to change", "변경 요청");
		m.put("root", "루트");
		m.put("root url of OneDev server", "OneDev 서버의 루트 URL");
		m.put("run job", "작업 실행");
		m.put("search in this revision will be accurate after indexed", "이 리비전에 대한 검색은 인덱싱 후 정확해집니다");
		m.put("service", "서비스");
		m.put("severity:CRITICAL", "중요");
		m.put("severity:HIGH", "높음");
		m.put("severity:LOW", "낮음");
		m.put("severity:MEDIUM", "중간");
		m.put("skipped {0} lines", "{0}개의 라인을 건너뜀");
		m.put("space", "공간");
		m.put("state of an issue is transited", "이슈의 상태가 전환됨");
		m.put("step template", "단계 템플릿");
		m.put("submit", "제출");
		m.put("tag the job is running against", "작업이 실행 중인 태그");
		m.put("tag {0}", "tag {0}");
		m.put("tags", "태그");
		m.put("the url to set up user account", "사용자 계정을 설정하기 위한 URL");
		m.put("time aggregation link", "시간 집계 링크");
		m.put("touching specified path", "지정된 경로를 터치하는 중");
		m.put("transit manually by any user", "사용자가 수동으로 전환");
		m.put("transit manually by any user of roles \"{0}\"", "역할 \"{0}\"의 사용자가 수동으로 전환");
		m.put("true", "참");
		m.put("true for html version, false for text version", "HTML 버전은 참, 텍스트 버전은 거짓");
		m.put("up to date", "최신 상태");
		m.put("url following which to verify email address", "이메일 주소를 확인하기 위한 URL");
		m.put("url to reset password", "비밀번호를 재설정하기 위한 URL");
		m.put("value needs to be enclosed in brackets", "값은 대괄호로 감싸야 합니다");
		m.put("value needs to be enclosed in parenthesis", "값은 괄호로 감싸야 합니다");
		m.put("value should be quoted", "값은 따옴표로 감싸야 합니다");
		m.put("w%02d", "w%02d");
		m.put("week:Fri", "금");
		m.put("week:Mon", "월");
		m.put("week:Sat", "토");
		m.put("week:Sun", "일");
		m.put("week:Thu", "목");
		m.put("week:Tue", "화");
		m.put("week:Wed", "수");
		m.put("widget:Tabs", "탭");
		m.put("you may show this page later via incompatibilities link in help menu", "도움말 메뉴의 호환성 링크를 통해 나중에 이 페이지를 표시할 수 있습니다");
		m.put("{0} Month(s)", "{0} 개월");
		m.put("{0} activities on {1}", "{1}에서 {0} 활동");
		m.put("{0} additions & {1} deletions", "{0} 추가 및 {1} 삭제");
		m.put("{0} ahead", "{0} 앞서 있음");
		m.put("{0} behind", "{0} 뒤처짐");
		m.put("{0} branches", "{0} 브랜치");
		m.put("{0} build(s)", "{0} 빌드");
		m.put("{0} child projects", "{0} 하위 프로젝트");
		m.put("{0} commits", "{0} 커밋");
		m.put("{0} commits ahead of base branch", "기본 브랜치보다 {0} 커밋 앞서 있음");
		m.put("{0} commits behind of base branch", "기본 브랜치보다 {0} 커밋 뒤처짐");
		m.put("{0} day", "{0} 일");
		m.put("{0} days", "{0} 일들");
		m.put("{0} edited {1}", "{0}이(가) {1}을(를) 수정함");
		m.put("{0} files", "{0} 파일");
		m.put("{0} forks", "{0} 포크");
		m.put("{0} hour", "{0} 시간");
		m.put("{0} hours", "{0} 시간들");
		m.put("{0} inaccessible activities", "{0} 접근 불가능한 활동");
		m.put("{0} minute", "{0} 분");
		m.put("{0} minutes", "{0} 분들");
		m.put("{0} reviewed", "{0} 검토됨");
		m.put("{0} second", "{0} 초");
		m.put("{0} seconds", "{0} 초들");
		m.put("{0} tags", "{0} 태그들");
		m.put("{0}d", "{0}일");
		m.put("{0}h", "{0}시간");
		m.put("{0}m", "{0}분");
		m.put("{0}s", "{0}초");
		m.put("{0}w", "{0}주");
		m.put("{javax.validation.constraints.NotEmpty.message}", "{javax.validation.constraints.NotEmpty.message}");
		m.put("{javax.validation.constraints.NotNull.message}", "{javax.validation.constraints.NotNull.message}");
		m.put("{javax.validation.constraints.Size.message}", "{javax.validation.constraints.Size.message}");
	}
		
	@Override
	protected Map<String, String> getContents() {
		return m;		
	}
	
}
