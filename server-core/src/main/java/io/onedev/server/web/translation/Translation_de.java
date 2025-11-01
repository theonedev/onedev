package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_de extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init(m);
		Translation.watchUpdate(Translation_de.class, () -> {
			init(m);
		});
	}
	
	@SystemPrompt("You are good at translating from English to German in DevOps software area.")
	public static void init(Map<String, String> m) {
		m.clear();
		m.put(" Project path can be omitted if reference from current project", "Projektpfad kann weggelassen werden, wenn er sich auf das aktuelle Projekt bezieht");
		m.put("'..' is not allowed in the directory", "'..' ist im Verzeichnis nicht erlaubt");
		m.put("(* = any string, ? = any character)", "(* = beliebiger String, ? = beliebiges Zeichen)");
		m.put("(on behalf of <b>{0}</b>)", "(im Namen von <b>{0}</b>)");
		m.put("** Enterprise edition is disabled as the subscription was expired. Renew to enable **", 
			"** Enterprise-Edition ist deaktiviert, da das Abonnement abgelaufen ist. Erneuern, um zu aktivieren **");
		m.put("** Enterprise edition is disabled as the trial subscription was expired, order subscription to enable or contact support@onedev.io if you need to extend your trial **", 
			"** Enterprise-Edition ist deaktiviert, da das Testabonnement abgelaufen ist. Bestellen Sie ein Abonnement, um es zu aktivieren, oder kontaktieren Sie support@onedev.io, wenn Sie Ihre Testphase verlängern möchten **");
		m.put("** Enterprise edition is disabled as there is no remaining user months. Order more to enable **", 
			"** Enterprise-Edition ist deaktiviert, da keine verbleibenden Benutzer-Monate vorhanden sind. Bestellen Sie mehr, um sie zu aktivieren **");
		m.put("1. To use this package, add below to project pom.xml", "1. Um dieses Paket zu verwenden, fügen Sie Folgendes zu project pom.xml hinzu");
		m.put("1. Use below repositories in project pom.xml", "1. Verwenden Sie die folgenden Repositories in project pom.xml");
		m.put("1w 1d 1h 1m", "1w 1d 1h 1m");
		m.put("2. Add below to <code>$HOME/.m2/settings.xml</code> if you want to deploy from command line", 
			"2. Fügen Sie Folgendes zu <code>$HOME/.m2/settings.xml</code> hinzu, wenn Sie von der Befehlszeile aus bereitstellen möchten");
		m.put("2. Also add below to $HOME/.m2/settings.xml if you want to compile project from command line", 
			"2. Fügen Sie auch Folgendes zu $HOME/.m2/settings.xml hinzu, wenn Sie das Projekt von der Befehlszeile aus kompilieren möchten");
		m.put("3. For CI/CD job, it is more convenient to use a custom settings.xml, for instance via below code in a command step:", 
			"3. Für CI/CD-Jobs ist es praktischer, eine benutzerdefinierte settings.xml zu verwenden, beispielsweise über den folgenden Code in einem Befehls-Schritt:");
		m.put("6-digits passcode", "6-stelliger Passcode");
		m.put("7 days", "7 Tage");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">Benutzer</a>, um das Passwort zurückzusetzen");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">Benutzer</a>, um die E-Mail zu verifizieren");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub-flavored Markdown</a> wird akzeptiert, mit <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">Mermaid- und KaTeX-Unterstützung</a>.");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>Ereignisobjekt</a>, das die Benachrichtigung auslöst");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>Alarm</a> zur Anzeige");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a> overdue", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stoppuhr</a> überfällig");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> hat <span wicket:id=\"date\"></span> committet");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed with <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> hat mit <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span> committet");
		m.put("<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depends on me", "<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> hängt von mir ab");
		m.put("<a wicket:id=\"removePassword\">Remove password</a> to force the user to authenticate via external system", 
			"<a wicket:id=\"removePassword\">Passwort entfernen</a>, um den Benutzer zur Authentifizierung über ein externes System zu zwingen");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">Mit Wiederherstellungscode verifizieren</a>, wenn Sie keinen Zugriff auf Ihren TOTP-Authenticator haben");
		m.put("<b class='text-danger'>NOTE: </b> This requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>HINWEIS: </b> Dies erfordert ein Enterprise-Abonnement. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>HINWEIS: </b> Dieser Schritt erfordert ein Enterprise-Abonnement. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>HINWEIS: </b>SendGrid-Integration ist eine Enterprise-Funktion. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>HINWEIS: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Zeiterfassung</a> ist eine Enterprise-Funktion. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("<b>NOTE: </b> Service desk only takes effect if <a wicket:id=\"mailConnector\">mail service</a> is defined and its <tt>check incoming email</tt> option is enabled. Also <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>sub addressing</a> needs to be enabled for the system email address. Check <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>this tutorial</a> for details", 
			"<b>HINWEIS: </b> Der Service Desk funktioniert nur, wenn <a wicket:id=\"mailConnector\">Mail-Dienst</a> definiert ist und die Option <tt>Eingehende E-Mails prüfen</tt> aktiviert ist. Außerdem muss <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Subaddressing</a> für die System-E-Mail-Adresse aktiviert sein. Weitere Informationen finden Sie in <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>diesem Tutorial</a>.");
		m.put("<b>NOTE:</b> Batch editing issues will not cause state transitions of other issues even if transition rule matches", 
			"<b>HINWEIS:</b> Das Batch-Bearbeiten von Problemen führt nicht zu Statusübergängen anderer Probleme, selbst wenn die Übergangsregel übereinstimmt");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>Projektbesitzer</b> ist eine integrierte Rolle mit vollständigen Berechtigungen für Projekte");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>Tipps: </b> Geben Sie <tt>@</tt> ein, um <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>Variable einzufügen</a>. Verwenden Sie <tt>@@</tt> für ein literales <tt>@</tt>");
		m.put("<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Dateien suchen</span> <span class='font-size-sm text-muted'>im Standard-Branch</span></div>");
		m.put("<div><span>Search Symbols</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Symbole suchen</span> <span class='font-size-sm text-muted'>im Standard-Branch</span></div>");
		m.put("<div><span>Search Text</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Text suchen</span> <span class='font-size-sm text-muted'>im Standard-Branch</span></div>");
		m.put("<i>No Name</i>", "<i>Kein Name</i>");
		m.put("<span class=\"keycap ml-3 mr-1\">esc</span> to close", "<span class=\"keycap ml-3 mr-1\">esc</span> zum Schließen");
		m.put("<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> to move", 
			"<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> zum Bewegen");
		m.put("<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> to navigate. <span class=\"keycap\">Esc</span> to close", 
			"<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> zum Navigieren. <span class=\"keycap\">Esc</span> zum Schließen");
		m.put("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete.", 
			"<span class='keycap'>Tab</span> oder <span class='keycap'>Enter</span> zum Abschließen.");
		m.put("<span class='keycap'>Tab</span> to complete.", "<span class='keycap'>Tab</span> zum Abschließen.");
		m.put("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>", "<span class='text-nowrap'><span class='keycap'>Enter</span> zum Gehen</span>");
		m.put("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>", "<span class='text-nowrap'><span class='keycap'>Tab</span> zum Suchen</span>");
		m.put("<span wicket:id=\"activityCount\"></span> activities", "<span wicket:id=\"activityCount\"></span> Aktivitäten");
		m.put("<svg class='icon mr-2'><use xlink:href='%s'/></svg> Define job secrets to be used in build spec. Secrets with <b>same name</b> can be defined. For a particular name, the first authorized secret with that name will be used (search in current project first, then search in parent projects). Note that secret value containing line breaks or less than <b>%d</b> characters will not be masked in build log", 
			"<svg class='icon mr-2'><use xlink:href='%s'/></svg> Definieren Sie Job-Geheimnisse, die in der Build-Spezifikation verwendet werden sollen. Geheimnisse mit <b>gleichem Namen</b> können definiert werden. Für einen bestimmten Namen wird das erste autorisierte Geheimnis mit diesem Namen verwendet (zuerst im aktuellen Projekt suchen, dann in übergeordneten Projekten suchen). Beachten Sie, dass Geheimniswerte, die Zeilenumbrüche enthalten oder weniger als <b>%d</b> Zeichen haben, im Build-Log nicht maskiert werden");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here", 
			"Ein <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java-Muster</a> wird hier erwartet");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"Ein <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java-Regulärer Ausdruck</a>, um die Commit-Nachrichten-Fußzeile zu validieren");
		m.put("A child project with name \"{0}\" already exists under \"{1}\"", "Ein untergeordnetes Projekt mit dem Namen \"{0}\" existiert bereits unter \"{1}\"");
		m.put("A file exists where you’re trying to create a subdirectory. Choose a new path and try again..", 
			"Eine Datei existiert dort, wo Sie versuchen, ein Unterverzeichnis zu erstellen. Wählen Sie einen neuen Pfad und versuchen Sie es erneut.");
		m.put("A path with same name already exists.Please choose a different name and try again.", 
			"Ein Pfad mit demselben Namen existiert bereits. Bitte wählen Sie einen anderen Namen und versuchen Sie es erneut.");
		m.put("A pull request is open for this change", "Ein Pull-Request ist für diese Änderung geöffnet");
		m.put("A root project with name \"{0}\" already exists", "Ein Stammprojekt mit dem Namen \"{0}\" existiert bereits");
		m.put("A {0} used as body of address verification email", "Ein {0}, der als Körper der Adressverifizierungs-E-Mail verwendet wird");
		m.put("A {0} used as body of build notification email", "Ein {0}, der als Körper der Build-Benachrichtigungs-E-Mail verwendet wird");
		m.put("A {0} used as body of commit notification email", "Ein {0}, der als Körper der Commit-Benachrichtigungs-E-Mail verwendet wird");
		m.put("A {0} used as body of feedback email when failed to open issue via service desk", "Ein {0}, der als Körper der Feedback-E-Mail verwendet wird, wenn das Öffnen eines Problems über den Service Desk fehlschlägt");
		m.put("A {0} used as body of feedback email when issue is opened via service desk", "Ein {0}, der als Körper der Feedback-E-Mail verwendet wird, wenn ein Problem über den Service Desk geöffnet wird");
		m.put("A {0} used as body of feedback email when unsubscribed from issue notification", "Ein {0}, der als Körper der Feedback-E-Mail verwendet wird, wenn die Benachrichtigung über ein Problem abbestellt wird");
		m.put("A {0} used as body of feedback email when unsubscribed from pull request notification", 
			"Ein {0}, der als Körper der Feedback-E-Mail verwendet wird, wenn die Benachrichtigung über einen Pull-Request abbestellt wird");
		m.put("A {0} used as body of issue stopwatch overdue notification email", "Ein {0}, der als Körper der Benachrichtigungs-E-Mail für überfällige Problem-Stoppuhren verwendet wird");
		m.put("A {0} used as body of package notification email", "Ein {0}, der als Körper der Paket-Benachrichtigungs-E-Mail verwendet wird");
		m.put("A {0} used as body of password reset email", "Ein {0}, der als Körper der Passwort-Zurücksetzungs-E-Mail verwendet wird");
		m.put("A {0} used as body of system alert email", "Ein {0}, der als Körper der System-Alarm-E-Mail verwendet wird");
		m.put("A {0} used as body of user invitation email", "Ein {0}, der als Körper der Benutzer-Einladungs-E-Mail verwendet wird");
		m.put("A {0} used as body of various issue notification emails", "Ein {0}, der als Körper verschiedener Problem-Benachrichtigungs-E-Mails verwendet wird");
		m.put("A {0} used as body of various pull request notification emails", "Ein {0}, der als Körper verschiedener Pull-Request-Benachrichtigungs-E-Mails verwendet wird");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"API-URL Ihrer JIRA-Cloud-Instanz, beispielsweise <tt>https://your-domain.atlassian.net/rest/api/3</tt>");
		m.put("Able to merge without conflicts", "Kann ohne Konflikte zusammengeführt werden");
		m.put("Absolute or relative url of the image", "Absolute oder relative URL des Bildes");
		m.put("Absolute or relative url of the link", "Absolute oder relative URL des Links");
		m.put("Access Anonymously", "Anonym zugreifen");
		m.put("Access Build Log", "Build-Log zugreifen");
		m.put("Access Build Pipeline", "Build-Pipeline zugreifen");
		m.put("Access Build Reports", "Build-Berichte zugreifen");
		m.put("Access Confidential Issues", "Vertrauliche Probleme zugreifen");
		m.put("Access Time Tracking", "Zeiterfassung zugreifen");
		m.put("Access Token", "Zugriffstoken");
		m.put("Access Token Authorization Bean", "Zugriffstoken-Autorisierungs-Bean");
		m.put("Access Token Edit Bean", "Zugriffstoken-Bearbeitungs-Bean");
		m.put("Access Token Secret", "Zugriffstoken-Geheimnis");
		m.put("Access Token for Target Project", "Zugriffstoken für Zielprojekt");
		m.put("Access Tokens", "Zugriffstoken");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"Das Zugriffstoken ist für API-Zugriff und Repository-Pull/Push vorgesehen. Es kann nicht verwendet werden, um sich bei der Web-Benutzeroberfläche anzumelden");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"Das Zugriffstoken ist für API-Zugriff oder Repository-Pull/Push vorgesehen. Es kann nicht verwendet werden, um sich bei der Web-Benutzeroberfläche anzumelden");
		m.put("Access token regenerated successfully", "Zugriffstoken erfolgreich regeneriert");
		m.put("Access token regenerated, make sure to update the token at agent side", "Zugriffstoken regeneriert, stellen Sie sicher, dass Sie das Token auf der Agentenseite aktualisieren");
		m.put("Account Email", "Konten-E-Mail");
		m.put("Account Name", "Kontenname");
		m.put("Account is disabled", "Konto ist deaktiviert");
		m.put("Account set up successfully", "Konto erfolgreich eingerichtet");
		m.put("Active Directory", "Active Directory");
		m.put("Active Since", "Aktiv seit");
		m.put("Activities", "Aktivitäten");
		m.put("Activity by type", "Aktivität nach Typ");
		m.put("Add", "Hinzufügen");
		m.put("Add Executor", "Executor hinzufügen");
		m.put("Add GPG key", "GPG-Schlüssel hinzufügen");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "Fügen Sie hier GPG-Schlüssel hinzu, um Commits/Tags zu verifizieren, die von diesem Benutzer signiert wurden");
		m.put("Add GPG keys here to verify commits/tags signed by you", "Fügen Sie hier GPG-Schlüssel hinzu, um Commits/Tags zu verifizieren, die von Ihnen signiert wurden");
		m.put("Add GPG public keys to be trusted here. Commits signed with trusted keys will be shown as verified.", 
			"Fügen Sie hier GPG-öffentliche Schlüssel hinzu, die vertrauenswürdig sind. Commits, die mit vertrauenswürdigen Schlüsseln signiert sind, werden als verifiziert angezeigt.");
		m.put("Add Issue...", "Problem hinzufügen...");
		m.put("Add Issues to Iteration", "Probleme zur Iteration hinzufügen");
		m.put("Add New", "Neu hinzufügen");
		m.put("Add New Board", "Neues Board hinzufügen");
		m.put("Add New Email Address", "Neue E-Mail-Adresse hinzufügen");
		m.put("Add New Timesheet", "Neue Zeiterfassung hinzufügen");
		m.put("Add Rule", "Regel hinzufügen");
		m.put("Add SSH key", "SSH-Schlüssel hinzufügen");
		m.put("Add SSO provider", "SSO-Anbieter hinzufügen");
		m.put("Add Spent Time", "Verbrauchte Zeit hinzufügen");
		m.put("Add Timesheet", "Zeiterfassung hinzufügen");
		m.put("Add Widget", "Widget hinzufügen");
		m.put("Add a GPG Public Key", "Einen GPG-öffentlichen Schlüssel hinzufügen");
		m.put("Add a SSH Key", "Einen SSH-Schlüssel hinzufügen");
		m.put("Add a package source like below", "Eine Paketquelle wie unten hinzufügen");
		m.put("Add after", "Nach hinzufügen");
		m.put("Add agent", "Agent hinzufügen");
		m.put("Add all cards to specified iteration", "Alle Karten zur angegebenen Iteration hinzufügen");
		m.put("Add all commits from source branch to target branch with a merge commit", "Alle Commits vom Quellbranch zum Zielbranch mit einem Merge-Commit hinzufügen");
		m.put("Add assignee...", "Zuweisung hinzufügen...");
		m.put("Add before", "Vor hinzufügen");
		m.put("Add below to allow accessing via http protocol in new Maven versions", "Unten hinzufügen, um Zugriff über das HTTP-Protokoll in neuen Maven-Versionen zu ermöglichen");
		m.put("Add child project", "Unterprojekt hinzufügen");
		m.put("Add comment", "Kommentar hinzufügen");
		m.put("Add comment on this selection", "Kommentar zu dieser Auswahl hinzufügen");
		m.put("Add custom field", "Benutzerdefiniertes Feld hinzufügen");
		m.put("Add dashboard", "Dashboard hinzufügen");
		m.put("Add default issue board", "Standard-Issue-Board hinzufügen");
		m.put("Add files to current directory", "Dateien zum aktuellen Verzeichnis hinzufügen");
		m.put("Add files via upload", "Dateien per Upload hinzufügen");
		m.put("Add groovy script", "Groovy-Skript hinzufügen");
		m.put("Add issue description template", "Vorlage für Issue-Beschreibung hinzufügen");
		m.put("Add issue link", "Issue-Link hinzufügen");
		m.put("Add issue state", "Issue-Status hinzufügen");
		m.put("Add issue state transition", "Übergang des Issue-Status hinzufügen");
		m.put("Add link", "Link hinzufügen");
		m.put("Add new", "Neu hinzufügen");
		m.put("Add new card to this column", "Neue Karte zu dieser Spalte hinzufügen");
		m.put("Add new file", "Neue Datei hinzufügen");
		m.put("Add new import", "Neuen Import hinzufügen");
		m.put("Add new issue creation setting", "Neue Einstellung für Issue-Erstellung hinzufügen");
		m.put("Add new job dependency", "Neue Job-Abhängigkeit hinzufügen");
		m.put("Add new param", "Neuen Parameter hinzufügen");
		m.put("Add new post-build action", "Neue Post-Build-Aktion hinzufügen");
		m.put("Add new project dependency", "Neue Projekt-Abhängigkeit hinzufügen");
		m.put("Add new step", "Neuen Schritt hinzufügen");
		m.put("Add new trigger", "Neuen Trigger hinzufügen");
		m.put("Add project", "Projekt hinzufügen");
		m.put("Add reviewer...", "Reviewer hinzufügen...");
		m.put("Add to batch to commit with other suggestions later", "Zur Batch hinzufügen, um später mit anderen Vorschlägen zu committen");
		m.put("Add to group...", "Zur Gruppe hinzufügen...");
		m.put("Add to iteration...", "Zur Iteration hinzufügen...");
		m.put("Add user to group...", "Benutzer zur Gruppe hinzufügen...");
		m.put("Add value", "Wert hinzufügen");
		m.put("Add {0}", "{0} hinzufügen");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "Commit \"{0}\" hinzugefügt (<i class='text-danger'>im Repository fehlt</i>)");
		m.put("Added commit \"{0}\" ({1})", "Commit \"{0}\" hinzugefügt ({1})");
		m.put("Added to group", "Zur Gruppe hinzugefügt");
		m.put("Additions", "Ergänzungen");
		m.put("Administration", "Verwaltung");
		m.put("Administrative permission over a project", "Administrative Berechtigung für ein Projekt");
		m.put("Advanced Search", "Erweiterte Suche");
		m.put("After modification", "Nach Änderung");
		m.put("Agent", "Agent");
		m.put("Agent Attribute", "Agent-Attribut");
		m.put("Agent Count", "Agent-Anzahl");
		m.put("Agent Edit Bean", "Agent-Bearbeitungs-Bean");
		m.put("Agent Selector", "Agent-Selektor");
		m.put("Agent is designed to be maintenance free. Once connected to server, it will be updated automatically upon server upgrade", 
			"Der Agent ist wartungsfrei konzipiert. Sobald er mit dem Server verbunden ist, wird er bei einem Server-Upgrade automatisch aktualisiert");
		m.put("Agent removed", "Agent entfernt");
		m.put("Agent tokens are used to authorize agents. It should be configured via environment variable <tt>agentToken</tt> if agent runs as docker container, or property <tt>agentToken</tt> in file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> if agent runs on bare metal/virtual machine. A token will be in-use and removed from this list if agent using it connects to server", 
			"Agent-Tokens werden verwendet, um Agents zu autorisieren. Sie sollten über die Umgebungsvariable <tt>agentToken</tt> konfiguriert werden, wenn der Agent als Docker-Container läuft, oder über die Eigenschaft <tt>agentToken</tt> in der Datei <tt>&lt;agent dir&gt;/conf/agent.properties</tt>, wenn der Agent auf Bare-Metal/virtueller Maschine läuft. Ein Token wird verwendet und aus dieser Liste entfernt, wenn der Agent, der es verwendet, sich mit dem Server verbindet");
		m.put("Agents", "Agents");
		m.put("Agents can be used to execute jobs on remote machines. Once started it will update itself from server automatically when necessary", 
			"Agents können verwendet werden, um Jobs auf Remote-Maschinen auszuführen. Sobald sie gestartet sind, aktualisieren sie sich bei Bedarf automatisch vom Server");
		m.put("Aggregated from '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':", "Aggregiert von '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':");
		m.put("Aggregated from '<span wicket:id=\"spentTimeAggregationLink\"></span>':", "Aggregiert von '<span wicket:id=\"spentTimeAggregationLink\"></span>':");
		m.put("Aggregation Link", "Aggregations-Link");
		m.put("Alert", "Alarm");
		m.put("Alert Setting", "Alarm-Einstellung");
		m.put("Alert Settings", "Alarm-Einstellungen");
		m.put("Alert settings have been updated", "Alarm-Einstellungen wurden aktualisiert");
		m.put("Alerts", "Alarme");
		m.put("All", "Alle");
		m.put("All Issues", "Alle Issues");
		m.put("All RESTful Resources", "Alle RESTful-Ressourcen");
		m.put("All accessible", "Alle zugänglich");
		m.put("All builds", "Alle Builds");
		m.put("All changes", "Alle Änderungen");
		m.put("All except", "Alle außer");
		m.put("All files", "Alle Dateien");
		m.put("All groups", "Alle Gruppen");
		m.put("All issues", "Alle Issues");
		m.put("All platforms in OCI layout", "Alle Plattformen im OCI-Layout");
		m.put("All platforms in image", "Alle Plattformen im Image");
		m.put("All possible classes", "Alle möglichen Klassen");
		m.put("All projects", "Alle Projekte");
		m.put("All projects with code read permission", "Alle Projekte mit Leseberechtigung für Code");
		m.put("All pull requests", "Alle Pull-Requests");
		m.put("All users", "Alle Benutzer");
		m.put("Allow Empty", "Leere erlauben");
		m.put("Allow Empty Value", "Leeren Wert erlauben");
		m.put("Allow Multiple", "Mehrfach erlauben");
		m.put("Allowed Licenses", "Zulässige Lizenzen");
		m.put("Allowed Self Sign-Up Email Domain", "Zulässige E-Mail-Domain für Selbstregistrierung");
		m.put("Always", "Immer");
		m.put("Always Pull Image", "Image immer ziehen");
		m.put("An issue already linked for {0}. Unlink it first", "Ein Issue ist bereits für {0} verlinkt. Entfernen Sie es zuerst");
		m.put("An unexpected exception occurred", "Es ist eine unerwartete Ausnahme aufgetreten");
		m.put("And configure auth token of the registry", "Und konfigurieren Sie das Auth-Token des Registrierungsdienstes");
		m.put("Another pull request already open for this change", "Ein weiterer Pull-Request ist bereits für diese Änderung geöffnet");
		m.put("Any agent", "Beliebiger Agent");
		m.put("Any branch", "Beliebiger Branch");
		m.put("Any commit message", "Beliebige Commit-Nachricht");
		m.put("Any domain", "Beliebige Domäne");
		m.put("Any file", "Beliebige Datei");
		m.put("Any issue", "Beliebiges Problem");
		m.put("Any job", "Beliebiger Job");
		m.put("Any project", "Beliebiges Projekt");
		m.put("Any ref", "Beliebiger Ref");
		m.put("Any sender", "Beliebiger Absender");
		m.put("Any state", "Beliebiger Zustand");
		m.put("Any tag", "Beliebiges Tag");
		m.put("Any user", "Beliebiger Benutzer");
		m.put("Api Key", "API-Schlüssel");
		m.put("Api Token", "API-Token");
		m.put("Api Url", "API-URL");
		m.put("Append", "Anfügen");
		m.put("Applicable Branches", "Anwendbare Branches");
		m.put("Applicable Builds", "Anwendbare Builds");
		m.put("Applicable Code Comments", "Anwendbare Code-Kommentare");
		m.put("Applicable Commit Messages", "Anwendbare Commit-Nachrichten");
		m.put("Applicable Commits", "Anwendbare Commits");
		m.put("Applicable Images", "Anwendbare Bilder");
		m.put("Applicable Issues", "Anwendbare Probleme");
		m.put("Applicable Jobs", "Anwendbare Jobs");
		m.put("Applicable Names", "Anwendbare Namen");
		m.put("Applicable Projects", "Anwendbare Projekte");
		m.put("Applicable Pull Requests", "Anwendbare Pull-Requests");
		m.put("Applicable Senders", "Anwendbare Absender");
		m.put("Applicable Users", "Anwendbare Benutzer");
		m.put("Application (client) ID", "Anwendungs-(Client)-ID");
		m.put("Apply suggested change from code comment", "Vorgeschlagene Änderung aus Code-Kommentar übernehmen");
		m.put("Apply suggested changes from code comments", "Vorgeschlagene Änderungen aus Code-Kommentaren übernehmen");
		m.put("Approve", "Genehmigen");
		m.put("Approved", "Genehmigt");
		m.put("Approved pull request \"{0}\" ({1})", "Genehmigter Pull-Request \"{0}\" ({1})");
		m.put("Arbitrary scope", "Beliebiger Bereich");
		m.put("Arbitrary type", "Beliebiger Typ");
		m.put("Arch Pull Command", "Arch-Pull-Befehl");
		m.put("Archived", "Archiviert");
		m.put("Arguments", "Argumente");
		m.put("Artifacts", "Artefakte");
		m.put("Artifacts to Retrieve", "Zu holende Artefakte");
		m.put("As long as a feature can be accessed via url, you can input part of the url to match and jump", 
			"Solange eine Funktion über die URL zugänglich ist, können Sie einen Teil der URL eingeben, um zu suchen und zu springen");
		m.put("Ascending", "Aufsteigend");
		m.put("Assignees", "Zuweisungen");
		m.put("Assignees Issue Field", "Zuweisungen im Problemfeld");
		m.put("Assignees are expected to merge the pull request", "Zuweisungen sollen den Pull-Request zusammenführen");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"Zuweisungen haben Schreibrechte für Code und sind verantwortlich für das Zusammenführen des Pull-Requests");
		m.put("Asymmetric", "Asymmetrisch");
		m.put("At least one branch or tag should be selected", "Mindestens ein Branch oder Tag sollte ausgewählt werden");
		m.put("At least one choice need to be specified", "Mindestens eine Auswahl muss angegeben werden");
		m.put("At least one email address should be configured, please add a new one first", "Mindestens eine E-Mail-Adresse sollte konfiguriert werden, bitte zuerst eine neue hinzufügen");
		m.put("At least one email address should be specified", "Mindestens eine E-Mail-Adresse sollte angegeben werden");
		m.put("At least one entry should be specified", "Mindestens ein Eintrag sollte angegeben werden");
		m.put("At least one event type needs to be selected", "Mindestens ein Ereignistyp muss ausgewählt werden");
		m.put("At least one field needs to be specified", "Mindestens ein Feld muss angegeben werden");
		m.put("At least one project should be authorized", "Mindestens ein Projekt sollte autorisiert werden");
		m.put("At least one project should be selected", "Mindestens ein Projekt sollte ausgewählt werden");
		m.put("At least one repository should be selected", "Mindestens ein Repository sollte ausgewählt werden");
		m.put("At least one role is required", "Mindestens eine Rolle ist erforderlich");
		m.put("At least one role must be selected", "Mindestens eine Rolle muss ausgewählt werden");
		m.put("At least one state should be specified", "Mindestens ein Zustand sollte angegeben werden");
		m.put("At least one tab should be added", "Mindestens ein Tab sollte hinzugefügt werden");
		m.put("At least one user search base should be specified", "Mindestens eine Benutzer-Suchbasis sollte angegeben werden");
		m.put("At least one value needs to be specified", "Mindestens ein Wert muss angegeben werden");
		m.put("At least two columns need to be defined", "Mindestens zwei Spalten müssen definiert werden");
		m.put("Attachment", "Anhang");
		m.put("Attributes", "Attribute");
		m.put("Attributes (can only be edited when agent is online)", "Attribute (können nur bearbeitet werden, wenn der Agent online ist)");
		m.put("Attributes saved", "Attribute gespeichert");
		m.put("Audit", "Audit");
		m.put("Audit Log", "Audit-Log");
		m.put("Audit Setting", "Audit-Einstellung");
		m.put("Audit log will be preserved for the specified number of days. This setting applies to all audit events, including system level and project level", 
			"Das Audit-Log wird für die angegebene Anzahl von Tagen aufbewahrt. Diese Einstellung gilt für alle Audit-Ereignisse, einschließlich System- und Projektebene");
		m.put("Auth Source", "Auth-Quelle");
		m.put("Authenticate to Bitbucket Cloud", "Authentifizierung bei Bitbucket Cloud");
		m.put("Authenticate to GitHub", "Authentifizierung bei GitHub");
		m.put("Authenticate to GitLab", "Authentifizierung bei GitLab");
		m.put("Authenticate to Gitea", "Authentifizierung bei Gitea");
		m.put("Authenticate to JIRA cloud", "Authentifizierung bei JIRA Cloud");
		m.put("Authenticate to YouTrack", "Authentifizierung bei YouTrack");
		m.put("Authentication", "Authentifizierung");
		m.put("Authentication Required", "Authentifizierung erforderlich");
		m.put("Authentication Test", "Authentifizierungstest");
		m.put("Authentication Token", "Authentifizierungs-Token");
		m.put("Authenticator", "Authentifizierer");
		m.put("Authenticator Bean", "Authentifizierer-Bean");
		m.put("Author", "Autor");
		m.put("Author date", "Autoren-Datum");
		m.put("Authored By", "Verfasst von");
		m.put("Authorization", "Autorisierung");
		m.put("Authorizations", "Autorisierungen");
		m.put("Authorize user...", "Benutzer autorisieren...");
		m.put("Authorized Projects", "Autorisierte Projekte");
		m.put("Authorized Roles", "Autorisierte Rollen");
		m.put("Auto Merge", "Automatisches Zusammenführen");
		m.put("Auto Spec", "Automatische Spezifikation");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"Die automatische Update-Prüfung wird durchgeführt, indem ein Bild in Ihrem Browser von onedev.io angefordert wird, das die Verfügbarkeit neuer Versionen anzeigt, wobei die Farbe die Schwere des Updates angibt. Es funktioniert genauso wie die Anforderung von Avatar-Bildern durch Gravatar. Wenn deaktiviert, wird dringend empfohlen, das Update von Zeit zu Zeit manuell zu überprüfen (kann über das Hilfemenü unten links auf dem Bildschirm erfolgen), um zu sehen, ob Sicherheits-/kritische Fixes verfügbar sind");
		m.put("Auto-discovered executor", "Automatisch entdeckter Executor");
		m.put("Available Agent Tokens", "Verfügbare Agent-Tokens");
		m.put("Available Choices", "Verfügbare Auswahlmöglichkeiten");
		m.put("Avatar", "Avatar");
		m.put("Avatar Service Url", "Avatar-Service-URL");
		m.put("Avatar and name", "Avatar und Name");
		m.put("Back To Home", "Zurück zur Startseite");
		m.put("Backlog", "Backlog");
		m.put("Backlog Base Query", "Backlog-Basisabfrage");
		m.put("Backup", "Backup");
		m.put("Backup Now", "Jetzt sichern");
		m.put("Backup Schedule", "Sicherungszeitplan");
		m.put("Backup Setting", "Sicherungseinstellungen");
		m.put("Backup Setting Holder", "Sicherungseinstellungsplatzhalter");
		m.put("Backup settings updated", "Sicherungseinstellungen aktualisiert");
		m.put("Bare Metal", "Bare Metal");
		m.put("Base", "Basis");
		m.put("Base Gpg Key", "Basis-GPG-Schlüssel");
		m.put("Base Query", "Basisabfrage");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Base64-codiertes PEM-Format, beginnend mit -----BEGIN CERTIFICATE----- und endend mit -----END CERTIFICATE-----");
		m.put("Basic Info", "Grundlegende Informationen");
		m.put("Basic Settings", "Grundeinstellungen");
		m.put("Basic settings updated", "Grundeinstellungen aktualisiert");
		m.put("Batch Edit All Queried Issues", "Alle abgefragten Probleme im Batch bearbeiten");
		m.put("Batch Edit Selected Issues", "Ausgewählte Probleme im Batch bearbeiten");
		m.put("Batch Editing {0} Issues", "Batch-Bearbeitung von {0} Problemen");
		m.put("Batched suggestions", "Batch-Vorschläge");
		m.put("Before modification", "Vor der Änderung");
		m.put("Belonging Groups", "Zugehörige Gruppen");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"Unten sind einige häufige Kriterien. Geben Sie im Suchfeld oben ein, um die vollständige Liste und verfügbare Kombinationen anzuzeigen.");
		m.put("Below content is restored from an unsaved change. Clear to discard", "Der untenstehende Inhalt wurde aus einer nicht gespeicherten Änderung wiederhergestellt. Löschen, um zu verwerfen");
		m.put("Below information will also be sent", "Die untenstehenden Informationen werden ebenfalls gesendet");
		m.put("Binary file.", "Binärdatei.");
		m.put("Bitbucket App Password", "Bitbucket-App-Passwort");
		m.put("Bitbucket Login Name", "Bitbucket-Anmeldename");
		m.put("Bitbucket Repositories to Import", "Zu importierende Bitbucket-Repositories");
		m.put("Bitbucket Workspace", "Bitbucket-Arbeitsbereich");
		m.put("Bitbucket app password should be generated with permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>", 
			"Das Bitbucket-App-Passwort sollte mit den Berechtigungen <b>account/read</b>, <b>repositories/read</b> und <b>issues:read</b> generiert werden");
		m.put("Blame", "Blame");
		m.put("Blob", "Blob");
		m.put("Blob hash", "Blob-Hash");
		m.put("Blob index version", "Blob-Indexversion");
		m.put("Blob name", "Blob-Name");
		m.put("Blob path", "Blob-Pfad");
		m.put("Blob primary symbols", "Blob-Primärsymbole");
		m.put("Blob secondary symbols", "Blob-Sekundärsymbole");
		m.put("Blob symbol list", "Blob-Symbolliste");
		m.put("Blob text", "Blob-Text");
		m.put("Blob unknown", "Blob unbekannt");
		m.put("Blob upload invalid", "Blob-Upload ungültig");
		m.put("Blob upload unknown", "Blob-Upload unbekannt");
		m.put("Board", "Board");
		m.put("Board Columns", "Board-Spalten");
		m.put("Board Spec", "Board-Spezifikation");
		m.put("Boards", "Boards");
		m.put("Body", "Body");
		m.put("Bold", "Fett");
		m.put("Both", "Beide");
		m.put("Bottom", "Unten");
		m.put("Branch", "Branch");
		m.put("Branch \"{0}\" already exists, please choose a different name", "Branch \"{0}\" existiert bereits, bitte wählen Sie einen anderen Namen");
		m.put("Branch \"{0}\" created", "Branch \"{0}\" erstellt");
		m.put("Branch \"{0}\" deleted", "Branch \"{0}\" gelöscht");
		m.put("Branch <a wicket:id=\"targetBranch\"></a> is up to date with all commits from <a wicket:id=\"sourceBranch\"></a>. Try <a wicket:id=\"swapBranches\">swap source and target</a> for the comparison.", 
			"Branch <a wicket:id=\"targetBranch\"></a> ist auf dem neuesten Stand mit allen Commits von <a wicket:id=\"sourceBranch\"></a>. Versuchen Sie <a wicket:id=\"swapBranches\">Quell- und Ziel-Branch tauschen</a> für den Vergleich.");
		m.put("Branch Choice Bean", "Branch-Auswahl-Bean");
		m.put("Branch Name", "Branch-Name");
		m.put("Branch Protection", "Branch-Schutz");
		m.put("Branch Revision", "Branch-Revision");
		m.put("Branch update", "Branch-Aktualisierung");
		m.put("Branches", "Branches");
		m.put("Brand Setting Edit Bean", "Markeneinstellungs-Bearbeitungs-Bean");
		m.put("Branding", "Branding");
		m.put("Branding settings updated", "Branding-Einstellungen aktualisiert");
		m.put("Browse Code", "Code durchsuchen");
		m.put("Browse code", "Code durchsuchen");
		m.put("Bug Report", "Fehlerbericht");
		m.put("Build", "Build");
		m.put("Build #{0} already finished", "Build #{0} bereits abgeschlossen");
		m.put("Build #{0} deleted", "Build #{0} gelöscht");
		m.put("Build #{0} not finished yet", "Build #{0} noch nicht abgeschlossen");
		m.put("Build Artifact Storage", "Build-Artefakt-Speicher");
		m.put("Build Commit", "Build-Commit");
		m.put("Build Context", "Build-Kontext");
		m.put("Build Description", "Build-Beschreibung");
		m.put("Build Filter", "Build-Filter");
		m.put("Build Image", "Build-Image");
		m.put("Build Image (Kaniko)", "Build-Image (Kaniko)");
		m.put("Build Management", "Build-Management");
		m.put("Build Notification", "Build-Benachrichtigung");
		m.put("Build Notification Template", "Build-Benachrichtigungsvorlage");
		m.put("Build Number", "Build-Nummer");
		m.put("Build On Behalf Of", "Build im Auftrag von");
		m.put("Build Path", "Build-Pfad");
		m.put("Build Preservation", "Build-Erhaltung");
		m.put("Build Preservations", "Build-Erhaltungen");
		m.put("Build Preservations Bean", "Build-Erhaltungs-Bean");
		m.put("Build Preserve Rules", "Build-Erhaltungsregeln");
		m.put("Build Provider", "Build-Anbieter");
		m.put("Build Spec", "Build-Spezifikation");
		m.put("Build Statistics", "Build-Statistiken");
		m.put("Build Version", "Build-Version");
		m.put("Build Volume Storage Class", "Build-Volume-Speicherklasse");
		m.put("Build Volume Storage Size", "Build-Volume-Speichergröße");
		m.put("Build administrative permission for all jobs inside a project, including batch operations over multiple builds", 
			"Build-Administrationsberechtigung für alle Jobs innerhalb eines Projekts, einschließlich Batch-Operationen über mehrere Builds");
		m.put("Build docker image with docker buildx. This step can only be executed by server docker executor or remote docker executor, and it uses the buildx builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko step instead", 
			"Build-Docker-Image mit Docker Buildx. Dieser Schritt kann nur vom Server-Docker-Executor oder Remote-Docker-Executor ausgeführt werden und verwendet den in diesen Executoren angegebenen Buildx-Builder, um die Aufgabe auszuführen. Um ein Image mit Kubernetes-Executor zu erstellen, verwenden Sie bitte stattdessen den Kaniko-Schritt");
		m.put("Build docker image with kaniko. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Build-Docker-Image mit Kaniko. Dieser Schritt muss vom Server-Docker-Executor, Remote-Docker-Executor oder Kubernetes-Executor ausgeführt werden");
		m.put("Build duration statistics", "Build-Dauerstatistiken");
		m.put("Build frequency statistics", "Erstelle Häufigkeitsstatistiken");
		m.put("Build is successful", "Build ist erfolgreich");
		m.put("Build list", "Build-Liste");
		m.put("Build not exist or access denied", "Build existiert nicht oder Zugriff verweigert");
		m.put("Build number", "Build-Nummer");
		m.put("Build preserve rules saved", "Build-Erhaltungsregeln gespeichert");
		m.put("Build required for deletion. Submit pull request instead", "Build erforderlich für Löschung. Pull-Request stattdessen einreichen");
		m.put("Build required for this change. Please submit pull request instead", "Build erforderlich für diese Änderung. Bitte Pull-Request einreichen.");
		m.put("Build required for this change. Submit pull request instead", "Build erforderlich für diese Änderung. Pull-Request stattdessen einreichen");
		m.put("Build spec not defined", "Build-Spezifikation nicht definiert");
		m.put("Build spec not defined (import project: {0}, import revision: {1})", "Build-Spezifikation nicht definiert (Importprojekt: {0}, Importrevision: {1})");
		m.put("Build spec not found in commit of this build", "Build-Spezifikation im Commit dieses Builds nicht gefunden");
		m.put("Build statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Build-Statistiken sind eine Enterprise-Funktion. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("Build version", "Build-Version");
		m.put("Build with Persistent Volume", "Build mit Persistent Volume");
		m.put("Builds", "Builds");
		m.put("Builds are {0}", "Builds sind {0}");
		m.put("Buildx Builder", "Buildx Builder");
		m.put("Built In Fields Bean", "Eingebaute Felder-Bean");
		m.put("Burndown", "Burndown");
		m.put("Burndown chart", "Burndown-Diagramm");
		m.put("Button Image Url", "Button-Bild-URL");
		m.put("By Group", "Nach Gruppe");
		m.put("By User", "Nach Benutzer");
		m.put("By day", "Nach Tag");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"Standardmäßig wird der Code über eine automatisch generierte Berechtigung geklont, die nur Leserechte für das aktuelle Projekt hat. Falls der Job <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>Code auf den Server pushen</a> muss, sollten Sie hier benutzerdefinierte Berechtigungen mit entsprechenden Rechten bereitstellen");
		m.put("By default issues of parent and child projects will also be listed. Use query <code>&quot;Project&quot; is current</code> to show issues only belong to this project", 
			"Standardmäßig werden auch Probleme von übergeordneten und untergeordneten Projekten aufgelistet. Verwenden Sie die Abfrage <code>&quot;Project&quot; is current</code>, um nur Probleme dieses Projekts anzuzeigen");
		m.put("By month", "Nach Monat");
		m.put("By week", "Nach Woche");
		m.put("Bypass Certificate Check", "Zertifikatsprüfung umgehen");
		m.put("CANCELLED", "ABGEBROCHEN");
		m.put("CORS Allowed Origins", "CORS erlaubte Ursprünge");
		m.put("CPD Report", "CPD-Bericht");
		m.put("CPU", "CPU");
		m.put("CPU Intensive Task Concurrency", "CPU-intensive Aufgaben-Konkurrenz");
		m.put("CPU capability in millis. This is normally (CPU cores)*1000", "CPU-Kapazität in Millis. Dies ist normalerweise (CPU-Kerne)*1000");
		m.put("Cache Key", "Cache-Schlüssel");
		m.put("Cache Management", "Cache-Verwaltung");
		m.put("Cache Paths", "Cache-Pfade");
		m.put("Cache Setting Bean", "Cache-Einstellungs-Bean");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "Cache wird gelöscht, um Platz zu sparen, wenn er für diese Anzahl von Tagen nicht verwendet wird");
		m.put("Calculating merge preview...", "Berechnung der Merge-Vorschau...");
		m.put("Callback URL", "Callback-URL");
		m.put("Can Be Used By Jobs", "Kann von Jobs verwendet werden");
		m.put("Can Create Root Projects", "Kann Root-Projekte erstellen");
		m.put("Can Edit Estimated Time", "Kann geschätzte Zeit bearbeiten");
		m.put("Can not convert root user to service account", "Kann Root-Benutzer nicht in ein Dienstkonto umwandeln");
		m.put("Can not convert yourself to service account", "Kann sich nicht selbst in ein Dienstkonto umwandeln");
		m.put("Can not delete default branch", "Standard-Branch kann nicht gelöscht werden");
		m.put("Can not delete root account", "Root-Konto kann nicht gelöscht werden");
		m.put("Can not delete yourself", "Sie können sich nicht selbst löschen");
		m.put("Can not disable root account", "Root-Konto kann nicht deaktiviert werden");
		m.put("Can not disable yourself", "Sie können sich nicht selbst deaktivieren");
		m.put("Can not find issue board: ", "Kann Issue-Board nicht finden:");
		m.put("Can not move project \"{0}\" to be under itself or its descendants", "Projekt \"{0}\" kann nicht unter sich selbst oder seinen Nachkommen verschoben werden");
		m.put("Can not perform this operation now", "Diese Operation kann jetzt nicht durchgeführt werden");
		m.put("Can not reset password for service account or disabled user", "Passwort kann nicht für Dienstkonto oder deaktivierten Benutzer zurückgesetzt werden");
		m.put("Can not reset password for user authenticating via external system", "Passwort kann nicht für Benutzer zurückgesetzt werden, der sich über ein externes System authentifiziert");
		m.put("Can not save malformed query", "Fehlerhafte Abfrage kann nicht gespeichert werden");
		m.put("Can not use current or descendant project as parent", "Aktuelles oder Nachkommenprojekt kann nicht als übergeordnetes Projekt verwendet werden");
		m.put("Can only compare with common ancestor when different projects are involved", "Vergleich nur mit gemeinsamem Vorfahren möglich, wenn verschiedene Projekte beteiligt sind");
		m.put("Cancel", "Abbrechen");
		m.put("Cancel All Queried Builds", "Alle abgefragten Builds abbrechen");
		m.put("Cancel Selected Builds", "Ausgewählte Builds abbrechen");
		m.put("Cancel invitation", "Einladung abbrechen");
		m.put("Cancel request submitted", "Anfrage zum Abbrechen eingereicht");
		m.put("Cancel this build", "Diesen Build abbrechen");
		m.put("Cancelled", "Abgebrochen");
		m.put("Cancelled By", "Abgebrochen von");
		m.put("Case Sensitive", "Groß-/Kleinschreibung beachten");
		m.put("Certificates to Trust", "Zertifikate zum Vertrauen");
		m.put("Change", "Ändern");
		m.put("Change Detection Excludes", "Änderungserkennung ausschließen");
		m.put("Change My Password", "Mein Passwort ändern");
		m.put("Change To", "Ändern zu");
		m.put("Change already merged", "Änderung bereits zusammengeführt");
		m.put("Change not updated yet", "Änderung noch nicht aktualisiert");
		m.put("Change property <code>serverUrl</code> in file <code>conf/agent.properties</code> if necessary. The default value is taken from OneDev server url specified in <i>Administration / System Setting</i>", 
			"Eigenschaft <code>serverUrl</code> in Datei <code>conf/agent.properties</code> bei Bedarf ändern. Der Standardwert wird aus der OneDev-Server-URL übernommen, die unter <i>Administration / System Setting</i> angegeben ist");
		m.put("Change to another field", "Zu einem anderen Feld wechseln");
		m.put("Change to another state", "Zu einem anderen Zustand wechseln");
		m.put("Change to another value", "Zu einem anderen Wert wechseln");
		m.put("Changes since last review", "Änderungen seit der letzten Überprüfung");
		m.put("Changes since last visit", "Änderungen seit dem letzten Besuch");
		m.put("Changes since this action", "Änderungen seit dieser Aktion");
		m.put("Changes since this comment", "Änderungen seit diesem Kommentar");
		m.put("Channel Notification", "Kanalbenachrichtigung");
		m.put("Chart Metadata", "Diagramm-Metadaten");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"Sehen Sie sich <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHubs Anleitung</a> an, wie Sie GPG-Schlüssel generieren und verwenden, um Ihre Commits zu signieren");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including instructions on how to run agent as service", 
			"Sehen Sie sich <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">Agent-Verwaltung</a> für Details an, einschließlich Anweisungen, wie man Agent als Dienst ausführt");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including list of supported environment variables", 
			"Sehen Sie sich <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">Agent-Verwaltung</a> für Details an, einschließlich Liste unterstützter Umgebungsvariablen");
		m.put("Check Commit Message Footer", "Commit-Nachrichten-Footer überprüfen");
		m.put("Check Incoming Email", "Eingehende E-Mail überprüfen");
		m.put("Check Issue Integrity", "Issue-Integrität überprüfen");
		m.put("Check Update", "Update überprüfen");
		m.put("Check Workflow Integrity", "Workflow-Integrität überprüfen");
		m.put("Check out to local workspace", "In lokalen Arbeitsbereich auschecken");
		m.put("Check this to compare right side with common ancestor of left and right", "Markieren Sie dies, um die rechte Seite mit dem gemeinsamen Vorfahren von links und rechts zu vergleichen");
		m.put("Check this to enforce two-factor authentication for all users in the system", "Markieren Sie dies, um Zwei-Faktor-Authentifizierung für alle Benutzer im System zu erzwingen");
		m.put("Check this to enforce two-factor authentication for all users in this group", "Markieren Sie dies, um Zwei-Faktor-Authentifizierung für alle Benutzer in dieser Gruppe zu erzwingen");
		m.put("Check this to prevent branch creation", "Markieren Sie dies, um das Erstellen von Branches zu verhindern");
		m.put("Check this to prevent branch deletion", "Markieren Sie dies, um das Löschen von Branches zu verhindern");
		m.put("Check this to prevent forced push", "Markieren Sie dies, um das erzwungene Pushen zu verhindern");
		m.put("Check this to prevent tag creation", "Markieren Sie dies, um das Erstellen von Tags zu verhindern");
		m.put("Check this to prevent tag deletion", "Markieren Sie dies, um das Löschen von Tags zu verhindern");
		m.put("Check this to prevent tag update", "Markieren Sie dies, um das Aktualisieren von Tags zu verhindern");
		m.put("Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>. Note this is applicable for non-merge commits", 
			"Aktivieren Sie dies, um <a href='https://www.conventionalcommits.org' target='_blank'>konventionelle Commits</a> zu verlangen. Beachten Sie, dass dies für Nicht-Merge-Commits gilt");
		m.put("Check this to require valid signature of head commit", "Aktivieren Sie dies, um eine gültige Signatur des Head-Commits zu verlangen");
		m.put("Check this to retrieve Git LFS files", "Aktivieren Sie dies, um Git-LFS-Dateien abzurufen");
		m.put("Checkbox", "Checkbox");
		m.put("Checking field values...", "Feldwerte werden überprüft...");
		m.put("Checking fields...", "Felder werden überprüft...");
		m.put("Checking state and field ordinals...", "Status und Feldordinale werden überprüft...");
		m.put("Checking state...", "Status wird überprüft...");
		m.put("Checkout Code", "Code auschecken");
		m.put("Checkout Path", "Pfad auschecken");
		m.put("Checkout Pull Request Head", "Pull-Request-Head auschecken");
		m.put("Checkout Pull Request Merge Preview", "Pull-Request-Merge-Vorschau auschecken");
		m.put("Checkstyle Report", "Checkstyle-Bericht");
		m.put("Cherry-Pick", "Cherry-Pick");
		m.put("Cherry-picked successfully", "Erfolgreich Cherry-gepickt");
		m.put("Child Projects", "Unterprojekte");
		m.put("Child Projects Of", "Unterprojekte von");
		m.put("Choice Provider", "Auswahlanbieter");
		m.put("Choose", "Wählen");
		m.put("Choose JIRA project to import issues from", "Wählen Sie das JIRA-Projekt aus, aus dem Probleme importiert werden sollen");
		m.put("Choose Revision", "Revision wählen");
		m.put("Choose YouTrack project to import issues from", "Wählen Sie das YouTrack-Projekt aus, aus dem Probleme importiert werden sollen");
		m.put("Choose a project...", "Ein Projekt wählen...");
		m.put("Choose a user...", "Einen Benutzer wählen...");
		m.put("Choose branch...", "Branch wählen...");
		m.put("Choose branches...", "Branches wählen...");
		m.put("Choose build...", "Build wählen...");
		m.put("Choose file", "Datei wählen");
		m.put("Choose group...", "Gruppe wählen...");
		m.put("Choose groups...", "Gruppen wählen...");
		m.put("Choose issue...", "Problem wählen...");
		m.put("Choose issues...", "Wählen Sie Probleme aus...");
		m.put("Choose iteration...", "Iteration wählen...");
		m.put("Choose iterations...", "Iterationen wählen...");
		m.put("Choose job...", "Job wählen...");
		m.put("Choose jobs...", "Jobs wählen...");
		m.put("Choose project", "Projekt wählen");
		m.put("Choose projects...", "Projekte wählen...");
		m.put("Choose pull request...", "Pull-Request wählen...");
		m.put("Choose repository", "Repository wählen");
		m.put("Choose role...", "Rolle wählen...");
		m.put("Choose roles...", "Rollen wählen...");
		m.put("Choose users...", "Benutzer wählen...");
		m.put("Choose...", "Wählen...");
		m.put("Circular build spec imports ({0})", "Zirkuläre Build-Spec-Importe ({0})");
		m.put("Click to select a commit, or shift-click to select multiple commit", "Klicken, um einen Commit auszuwählen, oder Shift-Klick, um mehrere Commits auszuwählen");
		m.put("Click to show comment of marked text", "Klicken, um den Kommentar des markierten Textes anzuzeigen");
		m.put("Click to show issue details", "Klicken, um Problemdetails anzuzeigen");
		m.put("Client ID of this OneDev instance registered in Google cloud", "Client-ID dieser OneDev-Instanz, die in der Google-Cloud registriert ist");
		m.put("Client Id", "Client-ID");
		m.put("Client Secret", "Client-Secret");
		m.put("Client secret of this OneDev instance registered in Google cloud", "Client-Secret dieser OneDev-Instanz, die in der Google-Cloud registriert ist");
		m.put("Clippy Report", "Clippy-Bericht");
		m.put("Clone", "Klonen");
		m.put("Clone Credential", "Klon-Anmeldeinformationen");
		m.put("Clone Depth", "Klon-Tiefe");
		m.put("Clone in IntelliJ", "Klonen in IntelliJ");
		m.put("Clone in VSCode", "Klonen in VSCode");
		m.put("Close", "Schließen");
		m.put("Close Iteration", "Iteration schließen");
		m.put("Close this iteration", "Diese Iteration schließen");
		m.put("Closed", "Geschlossen");
		m.put("Closed Issue State", "Status des geschlossenen Problems");
		m.put("Closest due date", "Nächstes Fälligkeitsdatum");
		m.put("Clover Coverage Report", "Clover-Abdeckungsbericht");
		m.put("Cluster Role", "Cluster-Rolle");
		m.put("Cluster Setting", "Cluster-Einstellung");
		m.put("Cluster setting", "Cluster-Einstellung");
		m.put("Clustered Servers", "Cluster-Server");
		m.put("Cobertura Coverage Report", "Cobertura-Abdeckungsbericht");
		m.put("Code", "Code");
		m.put("Code Analysis", "Code-Analyse");
		m.put("Code Analysis Setting", "Code-Analyse-Einstellung");
		m.put("Code Analysis Settings", "Code-Analyse-Einstellungen");
		m.put("Code Changes", "Code-Änderungen");
		m.put("Code Comment", "Code-Kommentar");
		m.put("Code Comment Management", "Code-Kommentar-Verwaltung");
		m.put("Code Comments", "Code-Kommentare");
		m.put("Code Compare", "Code-Vergleich");
		m.put("Code Contribution Statistics", "Code-Beitragsstatistiken");
		m.put("Code Coverage", "Code-Abdeckung");
		m.put("Code Line Statistics", "Code-Zeilenstatistiken");
		m.put("Code Management", "Code-Verwaltung");
		m.put("Code Privilege", "Code-Berechtigung");
		m.put("Code Problem Statistics", "Code-Problemstatistiken");
		m.put("Code Search", "Code-Suche");
		m.put("Code Statistics", "Code-Statistiken");
		m.put("Code analysis settings updated", "Code-Analyse-Einstellungen aktualisiert");
		m.put("Code changes since...", "Code-Änderungen seit...");
		m.put("Code clone or download", "Code klonen oder herunterladen");
		m.put("Code comment", "Code-Kommentar");
		m.put("Code comment #{0} deleted", "Code-Kommentar #{0} gelöscht");
		m.put("Code comment administrative permission inside a project, including batch operations over multiple code comments", 
			"Administrative Berechtigung für Code-Kommentare innerhalb eines Projekts, einschließlich Batch-Operationen über mehrere Code-Kommentare");
		m.put("Code commit", "Code-Commit");
		m.put("Code is committed", "Code wurde committet");
		m.put("Code push", "Code-Push");
		m.put("Code read permission is required to import build spec (import project: {0}, import revision: {1})", 
			"Leseberechtigung für Code ist erforderlich, um Build-Spec zu importieren (Importprojekt: {0}, Importrevision: {1})");
		m.put("Code suggestion", "Code-Vorschlag");
		m.put("Code write permission is required for this operation", "Schreibberechtigung für Code ist für diese Operation erforderlich");
		m.put("Collapse all", "Alle einklappen");
		m.put("Color", "Farbe");
		m.put("Columns", "Spalten");
		m.put("Command Palette", "Befehls-Palette");
		m.put("Commands", "Befehle");
		m.put("Comment", "Kommentar");
		m.put("Comment Content", "Kommentarinhalt");
		m.put("Comment on File", "Kommentar zur Datei");
		m.put("Comment too long", "Kommentar zu lang");
		m.put("Commented code is outdated", "Kommentierter Code ist veraltet");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "Kommentiert in Datei \"{0}\" im Projekt \"{1}\"");
		m.put("Commented on issue \"{0}\" ({1})", "Kommentiert im Issue \"{0}\" ({1})");
		m.put("Commented on pull request \"{0}\" ({1})", "Kommentiert im Pull-Request \"{0}\" ({1})");
		m.put("Comments", "Kommentare");
		m.put("Commit", "Commit");
		m.put("Commit &amp; Insert", "Commit &amp; Einfügen");
		m.put("Commit Batched Suggestions", "Commit gebündelter Vorschläge");
		m.put("Commit Message", "Commit-Nachricht");
		m.put("Commit Message Bean", "Commit-Nachricht Bean");
		m.put("Commit Message Fix Patterns", "Commit-Nachricht Fix-Muster");
		m.put("Commit Message Footer Pattern", "Commit-Nachricht Fußzeilen-Muster");
		m.put("Commit Notification", "Commit-Benachrichtigung");
		m.put("Commit Notification Template", "Commit-Benachrichtigungsvorlage");
		m.put("Commit Scopes", "Commit-Bereiche");
		m.put("Commit Signature Required", "Commit-Signatur erforderlich");
		m.put("Commit Suggestion", "Commit-Vorschlag");
		m.put("Commit Types", "Commit-Typen");
		m.put("Commit Types For Footer Check", "Commit-Typen für Fußzeilenprüfung");
		m.put("Commit Your Change", "Commit Ihrer Änderung");
		m.put("Commit date", "Commit-Datum");
		m.put("Commit hash", "Commit-Hash");
		m.put("Commit history of current path", "Commit-Historie des aktuellen Pfads");
		m.put("Commit index version", "Commit-Index-Version");
		m.put("Commit message can be used to fix issues by prefixing and suffixing issue number with specified pattern. Each line of the commit message will be matched against each entry defined here to find issues to be fixed", 
			"Commit-Nachricht kann verwendet werden, um Probleme zu beheben, indem die Problemnummer mit einem angegebenen Muster vorangestellt und angehängt wird. Jede Zeile der Commit-Nachricht wird mit jedem hier definierten Eintrag abgeglichen, um zu findende Probleme zu beheben");
		m.put("Commit not exist or access denied", "Commit existiert nicht oder Zugriff verweigert");
		m.put("Commit of the build is missing", "Commit des Builds fehlt");
		m.put("Commit signature required but no GPG signing key specified", "Commit-Signatur erforderlich, aber kein GPG-Signierschlüssel angegeben");
		m.put("Commit suggestion", "Commit-Vorschlag");
		m.put("Commits", "Commits");
		m.put("Commits are taken from default branch of non-forked repositories", "Commits stammen aus dem Standard-Branch von nicht geforkten Repositories");
		m.put("Commits generated by OneDev previously will be shown as unverified if this key is deleted. Type <code>yes</code> below if you want to continue.", 
			"Commits, die zuvor von OneDev generiert wurden, werden als nicht verifiziert angezeigt, wenn dieser Schlüssel gelöscht wird. Geben Sie <code>yes</code> unten ein, wenn Sie fortfahren möchten.");
		m.put("Commits were merged into target branch", "Commits wurden in den Ziel-Branch zusammengeführt");
		m.put("Commits were merged into target branch outside of this pull request", "Commits wurden außerhalb dieses Pull-Requests in den Ziel-Branch zusammengeführt");
		m.put("Commits were rebased onto target branch", "Commits wurden auf den Ziel-Branch umgestellt");
		m.put("Commits were squashed into a single commit on target branch", "Commits wurden zu einem einzigen Commit im Ziel-Branch zusammengefasst");
		m.put("Committed After", "Commit nach");
		m.put("Committed Before", "Commit vor");
		m.put("Committed By", "Commit von");
		m.put("Committer", "Committer");
		m.put("Compare", "Vergleichen");
		m.put("Compare with base revision", "Vergleichen mit Basisrevision");
		m.put("Compare with this parent", "Vergleichen mit diesem Elternteil");
		m.put("Concurrency", "Parallelität");
		m.put("Condition", "Bedingung");
		m.put("Confidential", "Vertraulich");
		m.put("Config File", "Konfigurationsdatei");
		m.put("Configuration Discovery Url", "Konfigurations-Erkennungs-URL");
		m.put("Configure your scope to use below registry", "Konfigurieren Sie Ihren Bereich, um untenstehendes Registry zu verwenden");
		m.put("Confirm Approve", "Genehmigung bestätigen");
		m.put("Confirm Delete Source Branch", "Löschen des Quell-Branches bestätigen");
		m.put("Confirm Discard", "Verwerfen bestätigen");
		m.put("Confirm Reopen", "Wiedereröffnung bestätigen");
		m.put("Confirm Request For Changes", "Änderungsanforderung bestätigen");
		m.put("Confirm Restore Source Branch", "Wiederherstellung des Quell-Branches bestätigen");
		m.put("Confirm password here", "Passwort hier bestätigen");
		m.put("Confirm your action", "Aktion bestätigen");
		m.put("Connect New Agent", "Neuen Agent verbinden");
		m.put("Connect with your SSO account", "Mit Ihrem SSO-Konto verbinden");
		m.put("Contact Email", "Kontakt-E-Mail");
		m.put("Contact Name", "Kontaktname");
		m.put("Container Image", "Container-Image");
		m.put("Container Image(s)", "Container-Image(s)");
		m.put("Container default", "Container-Standard");
		m.put("Content", "Inhalt");
		m.put("Content Type", "Inhaltstyp");
		m.put("Content is identical", "Inhalt ist identisch");
		m.put("Continue to add other user after create", "Weiterhin andere Benutzer nach Erstellung hinzufügen");
		m.put("Contributed settings", "Beigetragene Einstellungen");
		m.put("Contributions", "Beiträge");
		m.put("Contributions to {0} branch, excluding merge commits", "Beiträge zum {0}-Branch, ohne Merge-Commits");
		m.put("Convert All Queried to Service Accounts", "Alle Abgefragten in Dienstkonten umwandeln");
		m.put("Convert Selected to Service Accounts", "Ausgewählte in Dienstkonten umwandeln");
		m.put("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm", 
			"Die Umwandlung in Dienstkonten entfernt Passwort, E-Mail-Adressen, alle Zuweisungen und Beobachtungen. Geben Sie <code>yes</code> ein, um zu bestätigen");
		m.put("Copy", "Kopieren");
		m.put("Copy All Queried Issues To...", "Alle abgefragten Issues kopieren nach...");
		m.put("Copy Files with SCP", "Dateien mit SCP kopieren");
		m.put("Copy Selected Issues To...", "Ausgewählte Issues kopieren nach...");
		m.put("Copy dashboard", "Dashboard kopieren");
		m.put("Copy issue number and title", "Issue-Nummer und Titel kopieren");
		m.put("Copy public key", "Öffentlichen Schlüssel kopieren");
		m.put("Copy selected text to clipboard", "Ausgewählten Text in die Zwischenablage kopieren");
		m.put("Copy to clipboard", "In die Zwischenablage kopieren");
		m.put("Count", "Zählen");
		m.put("Coverage Statistics", "Abdeckungsstatistiken");
		m.put("Covered", "Abgedeckt");
		m.put("Covered by tests", "Durch Tests abgedeckt");
		m.put("Cppcheck Report", "Cppcheck-Bericht");
		m.put("Cpu Limit", "CPU-Limit");
		m.put("Cpu Request", "CPU-Anforderung");
		m.put("Create", "Erstellen");
		m.put("Create Administrator Account", "Administrator-Konto erstellen");
		m.put("Create Branch", "Branch erstellen");
		m.put("Create Branch Bean", "Branch Bean erstellen");
		m.put("Create Branch Bean With Revision", "Branch Bean mit Revision erstellen");
		m.put("Create Child Project", "Unterprojekt erstellen");
		m.put("Create Child Projects", "Unterprojekte erstellen");
		m.put("Create Issue", "Erstelle Problem");
		m.put("Create Iteration", "Erstelle Iteration");
		m.put("Create Merge Commit", "Erstelle Merge-Commit");
		m.put("Create Merge Commit If Necessary", "Erstelle Merge-Commit, falls erforderlich");
		m.put("Create New", "Erstelle Neu");
		m.put("Create New File", "Erstelle Neue Datei");
		m.put("Create New User", "Neuen Benutzer erstellen");
		m.put("Create Project", "Erstelle Projekt");
		m.put("Create Pull Request", "Erstelle Pull-Request");
		m.put("Create Pull Request for This Change", "Erstelle Pull-Request für diese Änderung");
		m.put("Create Tag", "Erstelle Tag");
		m.put("Create Tag Bean", "Erstelle Tag-Bean");
		m.put("Create Tag Bean With Revision", "Erstelle Tag-Bean mit Revision");
		m.put("Create User", "Erstelle Benutzer");
		m.put("Create body", "Erstelle Inhalt");
		m.put("Create branch <b>{0}</b> from {1}", "Erstelle Branch <b>{0}</b> von {1}");
		m.put("Create child projects under a project", "Erstelle Unterprojekte unter einem Projekt");
		m.put("Create issue", "Erstelle Problem");
		m.put("Create merge commit", "Erstelle Merge-Commit");
		m.put("Create merge commit if necessary", "Erstelle Merge-Commit, falls erforderlich");
		m.put("Create new issue", "Erstelle neues Problem");
		m.put("Create tag", "Erstelle Tag");
		m.put("Create tag <b>{0}</b> from {1}", "Erstelle Tag <b>{0}</b> von {1}");
		m.put("Created At", "Erstellt am");
		m.put("Creation of this branch is prohibited per branch protection rule", "Das Erstellen dieses Branches ist gemäß der Branch-Schutzregel untersagt");
		m.put("Critical", "Kritisch");
		m.put("Critical Severity", "Kritische Schwere");
		m.put("Cron Expression", "Cron-Ausdruck");
		m.put("Cron schedule", "Cron-Zeitplan");
		m.put("Curl Location", "Curl-Standort");
		m.put("Current Iteration", "Aktuelle Iteration");
		m.put("Current Value", "Aktueller Wert");
		m.put("Current avatar", "Aktueller Avatar");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"Der aktuelle Kontext unterscheidet sich vom Kontext, als dieser Kommentar hinzugefügt wurde. Klicken Sie, um den Kommentar-Kontext anzuzeigen");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"Der aktuelle Kontext unterscheidet sich vom Kontext, als diese Antwort hinzugefügt wurde. Klicken Sie, um den Antwort-Kontext anzuzeigen");
		m.put("Current context is different from this action, click to show the comment context", "Der aktuelle Kontext unterscheidet sich von dieser Aktion. Klicken Sie, um den Kommentar-Kontext anzuzeigen");
		m.put("Current platform", "Aktuelle Plattform");
		m.put("Current project", "Aktuelles Projekt");
		m.put("Custom Linux Shell", "Benutzerdefinierte Linux-Shell");
		m.put("DISCARDED", "VERWORFEN");
		m.put("Dashboard Share Bean", "Dashboard-Share-Bean");
		m.put("Dashboard name", "Dashboard-Name");
		m.put("Dashboards", "Dashboards");
		m.put("Database Backup", "Datenbank-Backup");
		m.put("Date", "Datum");
		m.put("Date Time", "Datum und Uhrzeit");
		m.put("Days Per Week", "Tage pro Woche");
		m.put("Deactivate Subscription", "Abonnement deaktivieren");
		m.put("Deactivate Trial Subscription", "Testabonnement deaktivieren");
		m.put("Default", "Standard");
		m.put("Default (Shell on Linux, Batch on Windows)", "Standard (Shell unter Linux, Batch unter Windows)");
		m.put("Default Assignees", "Standard-Zuweisungen");
		m.put("Default Boards", "Standard-Boards");
		m.put("Default Fixed Issue Filter", "Standard-Filter für behobene Probleme");
		m.put("Default Fixed Issue Filters", "Standard-Filter für behobene Probleme");
		m.put("Default Fixed Issue Filters Bean", "Standard-Filter für behobene Probleme Bean");
		m.put("Default Group", "Standardgruppe");
		m.put("Default Issue Boards", "Standard-Issue-Boards");
		m.put("Default Merge Strategy", "Standard-Merge-Strategie");
		m.put("Default Multi Value Provider", "Standard-Mehrwertanbieter");
		m.put("Default Project", "Standardprojekt");
		m.put("Default Project Setting", "Standardprojekteinstellung");
		m.put("Default Roles", "Standardrollen");
		m.put("Default Roles Bean", "Standardrollen Bean");
		m.put("Default Value", "Standardwert");
		m.put("Default Value Provider", "Standardwertanbieter");
		m.put("Default Values", "Standardwerte");
		m.put("Default branch", "Standard-Branch");
		m.put("Default branding settings restored", "Standard-Branding-Einstellungen wiederhergestellt");
		m.put("Default fixed issue filters saved", "Standard-Filter für behobene Probleme gespeichert");
		m.put("Default merge strategy", "Standard-Merge-Strategie");
		m.put("Default roles affect default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"Standardrollen beeinflussen die Standardberechtigungen, die jedem im System gewährt werden. Die tatsächlichen Standardberechtigungen werden <b class='text-warning'>alle Berechtigungen</b> sein, die in den Standardrollen dieses Projekts und aller übergeordneten Projekte enthalten sind");
		m.put("Define all custom issue fields here. Each project can decide to use all or a subset of these fields via its issue transition setting. <b class=\"text-warning\">NOTE: </b> Newly defined fields by default only appear in new issues. Batch edit existing issues from issue list page if you want them to have these new fields", 
			"Definieren Sie hier alle benutzerdefinierten Problemfelder. Jedes Projekt kann entscheiden, ob es alle oder einen Teil dieser Felder über seine Problemübergangseinstellung verwendet. <b class=\"text-warning\">HINWEIS: </b> Neu definierte Felder erscheinen standardmäßig nur in neuen Problemen. Bearbeiten Sie vorhandene Probleme im Batch von der Problemlisten-Seite, wenn Sie möchten, dass sie diese neuen Felder haben");
		m.put("Define all custom issue states here. The first state will be used as initial state of created issues", 
			"Definieren Sie hier alle benutzerdefinierten Problemzustände. Der erste Zustand wird als Anfangszustand für erstellte Probleme verwendet");
		m.put("Define branch protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given branch and user, the first matching rule will take effect", 
			"Definieren Sie Branch-Schutzregeln. Regeln, die im übergeordneten Projekt definiert sind, gelten als nach den hier definierten Regeln definiert. Für einen bestimmten Branch und Benutzer gilt die erste übereinstimmende Regel");
		m.put("Define default issue boards for all projects here. A certain project can override this setting to define its own issue boards.", 
			"Definieren Sie hier die Standard-Issue-Boards für alle Projekte. Ein bestimmtes Projekt kann diese Einstellung überschreiben, um seine eigenen Issue-Boards zu definieren.");
		m.put("Define how issue states should be transited from one to another, either manually or automatically when some events happen. And the rule can be configured to apply to certain projects and issues via the applicable issues setting", 
			"Definieren Sie, wie Problemzustände von einem zum anderen übergehen sollen, entweder manuell oder automatisch, wenn bestimmte Ereignisse eintreten. Und die Regel kann so konfiguriert werden, dass sie für bestimmte Projekte und Probleme über die Einstellung anwendbarer Probleme gilt");
		m.put("Define issue templates here. When a new issue is created, the first matching template will be used.", 
			"Definieren Sie hier Problemvorlagen. Wenn ein neues Problem erstellt wird, wird die erste übereinstimmende Vorlage verwendet.");
		m.put("Define labels to be assigned to project, build or pull request. For issues, custom fields can be used which is much more powerful than labels", 
			"Definieren Sie Labels, die Projekten, Builds oder Pull-Requests zugewiesen werden sollen. Für Probleme können benutzerdefinierte Felder verwendet werden, die viel leistungsfähiger sind als Labels");
		m.put("Define properties to be used in build spec. Properties will be inherited by child projects, and can be overridden by child properties with same name.", 
			"Definieren Sie Eigenschaften, die in der Build-Spezifikation verwendet werden sollen. Eigenschaften werden von untergeordneten Projekten geerbt und können von untergeordneten Eigenschaften mit demselben Namen überschrieben werden.");
		m.put("Define rules to preserve builds. A build will be preserved as long as one rule defined here or in parent projects preserves it. All builds will be preserved if no rules are defined here and in parent projects", 
			"Definieren Sie Regeln zum Beibehalten von Builds. Ein Build wird beibehalten, solange eine hier oder in übergeordneten Projekten definierte Regel ihn beibehält. Alle Builds werden beibehalten, wenn hier und in übergeordneten Projekten keine Regeln definiert sind");
		m.put("Define tag protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given tag and user, the first matching rule will take effect", 
			"Definieren Sie Tag-Schutzregeln. Regeln, die im übergeordneten Projekt definiert sind, gelten als nach den hier definierten Regeln definiert. Für einen bestimmten Tag und Benutzer gilt die erste übereinstimmende Regel");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"Verzögerung für den ersten Wiederholungsversuch in Sekunden. Die Verzögerung nachfolgender Wiederholungen wird basierend auf diesem Wert mit einem exponentiellen Backoff berechnet");
		m.put("Delete", "Löschen");
		m.put("Delete All", "Alle löschen");
		m.put("Delete All Queried Builds", "Alle abgefragten Builds löschen");
		m.put("Delete All Queried Comments", "Alle abgefragten Kommentare löschen");
		m.put("Delete All Queried Issues", "Alle abgefragten Probleme löschen");
		m.put("Delete All Queried Packages", "Alle abgefragten Pakete löschen");
		m.put("Delete All Queried Projects", "Alle abgefragten Projekte löschen");
		m.put("Delete All Queried Pull Requests", "Alle abgefragten Pull-Requests löschen");
		m.put("Delete All Queried Users", "Alle abgefragten Benutzer löschen");
		m.put("Delete Build", "Build löschen");
		m.put("Delete Comment", "Kommentar löschen");
		m.put("Delete Pull Request", "Pull-Request löschen");
		m.put("Delete SSO account here to reconnect corresponding SSO subject upon next login. Note that SSO subject with verified email will be connected to user with same verified email automatically", 
			"Löschen Sie hier das SSO-Konto, um das entsprechende SSO-Subjekt beim nächsten Login erneut zu verbinden. Beachten Sie, dass ein SSO-Subjekt mit verifizierter E-Mail automatisch mit einem Benutzer mit derselben verifizierten E-Mail verbunden wird");
		m.put("Delete Selected", "Ausgewählte löschen");
		m.put("Delete Selected Builds", "Ausgewählte Builds löschen");
		m.put("Delete Selected Comments", "Ausgewählte Kommentare löschen");
		m.put("Delete Selected Issues", "Ausgewählte Probleme löschen");
		m.put("Delete Selected Packages", "Ausgewählte Pakete löschen");
		m.put("Delete Selected Projects", "Ausgewählte Projekte löschen");
		m.put("Delete Selected Pull Requests", "Ausgewählte Pull Requests löschen");
		m.put("Delete Selected Users", "Ausgewählte Benutzer löschen");
		m.put("Delete Source Branch", "Quellbranch löschen");
		m.put("Delete Source Branch After Merge", "Quellbranch nach dem Merge löschen");
		m.put("Delete dashboard", "Dashboard löschen");
		m.put("Delete from branch {0}", "Von Branch {0} löschen");
		m.put("Delete this", "Dies löschen");
		m.put("Delete this GPG key", "Diesen GPG-Schlüssel löschen");
		m.put("Delete this access token", "Diesen Zugriffstoken löschen");
		m.put("Delete this branch", "Diesen Branch löschen");
		m.put("Delete this executor", "Diesen Executor löschen");
		m.put("Delete this field", "Diesen Feld löschen");
		m.put("Delete this import", "Diesen Import löschen");
		m.put("Delete this iteration", "Diese Iteration löschen");
		m.put("Delete this key", "Diesen Schlüssel löschen");
		m.put("Delete this link", "Diesen Link löschen");
		m.put("Delete this rule", "Diese Regel löschen");
		m.put("Delete this secret", "Diesen Geheimnis löschen");
		m.put("Delete this state", "Diesen Zustand löschen");
		m.put("Delete this tag", "Diesen Tag löschen");
		m.put("Delete this value", "Diesen Wert löschen");
		m.put("Deleted source branch", "Quellbranch gelöscht");
		m.put("Deletion not allowed due to branch protection rule", "Löschen nicht erlaubt aufgrund der Branch-Schutzregel");
		m.put("Deletion not allowed due to tag protection rule", "Löschen nicht erlaubt aufgrund der Tag-Schutzregel");
		m.put("Deletions", "Löschungen");
		m.put("Denied", "Verweigert");
		m.put("Dependencies & Services", "Abhängigkeiten & Dienste");
		m.put("Dependency Management", "Abhängigkeitsmanagement");
		m.put("Dependency job finished", "Abhängigkeitsjob abgeschlossen");
		m.put("Dependent Fields", "Abhängige Felder");
		m.put("Depends on <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>", "Abhängig von <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>");
		m.put("Descending", "Absteigend");
		m.put("Description", "Beschreibung");
		m.put("Description Template", "Beschreibungsvorlage");
		m.put("Description Templates", "Beschreibungsvorlagen");
		m.put("Description too long", "Beschreibung zu lang");
		m.put("Destination Path", "Zielpfad");
		m.put("Destinations", "Ziele");
		m.put("Detect Licenses", "Lizenzen erkennen");
		m.put("Detect Secrets", "Geheimnisse erkennen");
		m.put("Detect Vulnerabilities", "Schwachstellen erkennen");
		m.put("Diff is too large to be displayed.", "Diff ist zu groß, um angezeigt zu werden.");
		m.put("Diff options", "Diff-Optionen");
		m.put("Digest", "Digest");
		m.put("Digest invalid", "Digest ungültig");
		m.put("Directories to Skip", "Zu überspringende Verzeichnisse");
		m.put("Directory", "Verzeichnis");
		m.put("Directory (tenant) ID", "Verzeichnis (Mandanten)-ID");
		m.put("Disable", "Deaktivieren");
		m.put("Disable All Queried Users", "Alle abgefragten Benutzer deaktivieren");
		m.put("Disable Auto Update Check", "Automatische Update-Prüfung deaktivieren");
		m.put("Disable Dashboard", "Dashboard deaktivieren");
		m.put("Disable Selected Users", "Ausgewählte Benutzer deaktivieren");
		m.put("Disabled", "Deaktiviert");
		m.put("Disabled users and service accounts are excluded from user-month calculation", "Deaktivierte Benutzer und Dienstkonten sind von der Benutzer-Monats-Berechnung ausgeschlossen");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"Das Deaktivieren des Kontos setzt das Passwort zurück, löscht Zugriffstoken und entfernt alle Referenzen von anderen Entitäten, außer für vergangene Aktivitäten. Möchten Sie wirklich fortfahren?");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"Das Deaktivieren von Konten setzt das Passwort zurück, löscht Zugriffstoken und entfernt alle Referenzen von anderen Entitäten, außer für vergangene Aktivitäten. Geben Sie <code>yes</code> ein, um zu bestätigen");
		m.put("Disallowed File Types", "Nicht erlaubte Dateitypen");
		m.put("Disallowed file type(s): {0}", "Nicht erlaubte Dateitypen: {0}");
		m.put("Discard", "Verwerfen");
		m.put("Discard All Queried Pull Requests", "Alle abgefragten Pull Requests verwerfen");
		m.put("Discard Selected Pull Requests", "Ausgewählte Pull Requests verwerfen");
		m.put("Discarded", "Verworfen");
		m.put("Discarded pull request \"{0}\" ({1})", "Pull Request \"{0}\" ({1}) verworfen");
		m.put("Discord", "Discord");
		m.put("Discord Notifications", "Discord-Benachrichtigungen");
		m.put("Display Fields", "Anzeigefelder");
		m.put("Display Links", "Anzeigelinks");
		m.put("Display Months", "Anzeigemonate");
		m.put("Display Params", "Anzeigeparameter");
		m.put("Do Not Retrieve Groups", "Gruppen nicht abrufen");
		m.put("Do not ignore", "Nicht ignorieren");
		m.put("Do not ignore whitespace", "Leerzeichen nicht ignorieren");
		m.put("Do not retrieve", "Nicht abrufen");
		m.put("Do not retrieve groups", "Gruppen nicht abrufen");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "Möchten Sie die Einladung an \"{0}\" wirklich stornieren?");
		m.put("Do you really want to cancel this build?", "Möchten Sie diesen Build wirklich abbrechen?");
		m.put("Do you really want to change target branch to {0}?", "Möchten Sie das Zielbranch wirklich auf {0} ändern?");
		m.put("Do you really want to delete \"{0}\"?", "Möchten Sie \"{0}\" wirklich löschen?");
		m.put("Do you really want to delete SSO provider \"{0}\"?", "Möchten Sie den SSO-Anbieter \"{0}\" wirklich löschen?");
		m.put("Do you really want to delete board \"{0}\"?", "Möchten Sie das Board \"{0}\" wirklich löschen?");
		m.put("Do you really want to delete build #{0}?", "Möchten Sie den Build #{0} wirklich löschen?");
		m.put("Do you really want to delete group \"{0}\"?", "Möchten Sie die Gruppe \"{0}\" wirklich löschen?");
		m.put("Do you really want to delete iteration \"{0}\"?", "Möchten Sie die Iteration \"{0}\" wirklich löschen?");
		m.put("Do you really want to delete job secret \"{0}\"?", "Möchten Sie das Job-Geheimnis \"{0}\" wirklich löschen?");
		m.put("Do you really want to delete pull request #{0}?", "Möchten Sie den Pull Request #{0} wirklich löschen?");
		m.put("Do you really want to delete role \"{0}\"?", "Möchten Sie die Rolle \"{0}\" wirklich löschen?");
		m.put("Do you really want to delete selected query watches?", "Möchten Sie die ausgewählten Abfrageüberwachungen wirklich löschen?");
		m.put("Do you really want to delete tag {0}?", "Möchten Sie den Tag {0} wirklich löschen?");
		m.put("Do you really want to delete this GPG key?", "Möchten Sie diesen GPG-Schlüssel wirklich löschen?");
		m.put("Do you really want to delete this SSH key?", "Möchten Sie diesen SSH-Schlüssel wirklich löschen?");
		m.put("Do you really want to delete this SSO account?", "Möchten Sie dieses SSO-Konto wirklich löschen?");
		m.put("Do you really want to delete this access token?", "Möchten Sie diesen Zugriffstoken wirklich löschen?");
		m.put("Do you really want to delete this board?", "Möchten Sie dieses Board wirklich löschen?");
		m.put("Do you really want to delete this build?", "Möchten Sie diesen Build wirklich löschen?");
		m.put("Do you really want to delete this code comment and all its replies?", "Möchten Sie diesen Code-Kommentar und alle seine Antworten wirklich löschen?");
		m.put("Do you really want to delete this code comment?", "Möchten Sie diesen Code-Kommentar wirklich löschen?");
		m.put("Do you really want to delete this directory?", "Möchten Sie dieses Verzeichnis wirklich löschen?");
		m.put("Do you really want to delete this email address?", "Möchten Sie diese E-Mail-Adresse wirklich löschen?");
		m.put("Do you really want to delete this executor?", "Möchten Sie diesen Executor wirklich löschen?");
		m.put("Do you really want to delete this field?", "Möchten Sie dieses Feld wirklich löschen?");
		m.put("Do you really want to delete this file?", "Möchten Sie diese Datei wirklich löschen?");
		m.put("Do you really want to delete this issue?", "Möchten Sie dieses Problem wirklich löschen?");
		m.put("Do you really want to delete this link?", "Möchten Sie diesen Link wirklich löschen?");
		m.put("Do you really want to delete this package?", "Möchten Sie dieses Paket wirklich löschen?");
		m.put("Do you really want to delete this privilege?", "Möchten Sie dieses Privileg wirklich löschen?");
		m.put("Do you really want to delete this protection?", "Möchten Sie diesen Schutz wirklich löschen?");
		m.put("Do you really want to delete this pull request?", "Möchten Sie diese Pull-Anfrage wirklich löschen?");
		m.put("Do you really want to delete this reply?", "Möchten Sie diese Antwort wirklich löschen?");
		m.put("Do you really want to delete this script?", "Möchten Sie dieses Skript wirklich löschen?");
		m.put("Do you really want to delete this state?", "Möchten Sie diesen Status wirklich löschen?");
		m.put("Do you really want to delete this template?", "Möchten Sie diese Vorlage wirklich löschen?");
		m.put("Do you really want to delete this transition?", "Möchten Sie diese Transition wirklich löschen?");
		m.put("Do you really want to delete timesheet \"{0}\"?", "Möchten Sie die Zeiterfassung \"{0}\" wirklich löschen?");
		m.put("Do you really want to delete unused tokens?", "Möchten Sie ungenutzte Tokens wirklich löschen?");
		m.put("Do you really want to discard batched suggestions?", "Möchten Sie die gebündelten Vorschläge wirklich verwerfen?");
		m.put("Do you really want to enable this account?", "Möchten Sie dieses Konto wirklich aktivieren?");
		m.put("Do you really want to rebuild?", "Möchten Sie wirklich neu erstellen?");
		m.put("Do you really want to remove assignee \"{0}\"?", "Möchten Sie den Zuweiser \"{0}\" wirklich entfernen?");
		m.put("Do you really want to remove password of this user?", "Möchten Sie das Passwort dieses Benutzers wirklich entfernen?");
		m.put("Do you really want to remove the issue from iteration \"{0}\"?", "Möchten Sie das Problem wirklich aus der Iteration \"{0}\" entfernen?");
		m.put("Do you really want to remove this account?", "Möchten Sie dieses Konto wirklich entfernen?");
		m.put("Do you really want to remove this agent?", "Möchten Sie diesen Agenten wirklich entfernen?");
		m.put("Do you really want to remove this link?", "Möchten Sie diesen Link wirklich entfernen?");
		m.put("Do you really want to restart this agent?", "Möchten Sie diesen Agenten wirklich neu starten?");
		m.put("Do you really want to unauthorize user \"{0}\"?", "Möchten Sie den Benutzer \"{0}\" wirklich deautorisieren?");
		m.put("Do you really want to use default template?", "Möchten Sie die Standardvorlage wirklich verwenden?");
		m.put("Docker", "Docker");
		m.put("Docker Executable", "Docker ausführbar");
		m.put("Docker Hub", "Docker Hub");
		m.put("Docker Image", "Docker-Image");
		m.put("Docker Sock Path", "Docker-Sock-Pfad");
		m.put("Dockerfile", "Dockerfile");
		m.put("Documentation", "Dokumentation");
		m.put("Don't have an account yet?", "Haben Sie noch kein Konto?");
		m.put("Download", "Herunterladen");
		m.put("Download <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> or <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. A new agent token will be included in the package", 
			"Laden Sie <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> oder <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a> herunter. Ein neuer Agent-Token wird im Paket enthalten sein.");
		m.put("Download archive of this branch", "Archiv dieses Branches herunterladen");
		m.put("Download full log", "Vollständiges Protokoll herunterladen");
		m.put("Download log", "Protokoll herunterladen");
		m.put("Download patch", "Patch herunterladen");
		m.put("Download tag archive", "Tag-Archiv herunterladen");
		m.put("Dry Run", "Trockener Lauf");
		m.put("Due Date", "Fälligkeitsdatum");
		m.put("Due Date Issue Field", "Fälligkeitsdatum-Feld");
		m.put("Due date", "Fälligkeitsdatum");
		m.put("Duplicate authorizations found: ", "Doppelte Autorisierungen gefunden:");
		m.put("Duplicate authorizations found: {0}", "Doppelte Autorisierungen gefunden: {0}");
		m.put("Duration", "Dauer");
		m.put("Durations", "Dauern");
		m.put("ESLint Report", "ESLint-Bericht");
		m.put("Edit", "Bearbeiten");
		m.put("Edit <code>$HOME/.gem/credentials</code> to add a source", "Bearbeiten Sie <code>$HOME/.gem/credentials</code>, um eine Quelle hinzuzufügen.");
		m.put("Edit <code>$HOME/.pypirc</code> to add a package repository like below", "Bearbeiten Sie <code>$HOME/.pypirc</code>, um ein Paket-Repository wie unten hinzuzufügen.");
		m.put("Edit Avatar", "Avatar bearbeiten");
		m.put("Edit Estimated Time", "Geschätzte Zeit bearbeiten");
		m.put("Edit Executor", "Executor bearbeiten");
		m.put("Edit Iteration", "Iteration bearbeiten");
		m.put("Edit Job Secret", "Job-Geheimnis bearbeiten");
		m.put("Edit My Avatar", "Meinen Avatar bearbeiten");
		m.put("Edit Rule", "Regel bearbeiten");
		m.put("Edit Timesheet", "Zeiterfassung bearbeiten");
		m.put("Edit dashboard", "Dashboard bearbeiten");
		m.put("Edit issue title", "Problemtitel bearbeiten");
		m.put("Edit job", "Job bearbeiten");
		m.put("Edit on branch {0}", "Bearbeiten im Branch {0}");
		m.put("Edit on source branch", "Bearbeiten im Quell-Branch");
		m.put("Edit plain", "Einfach bearbeiten");
		m.put("Edit saved queries", "Gespeicherte Abfragen bearbeiten");
		m.put("Edit this access token", "Diesen Zugriffstoken bearbeiten");
		m.put("Edit this executor", "Diesen Executor bearbeiten");
		m.put("Edit this iteration", "Diese Iteration bearbeiten");
		m.put("Edit this rule", "Diese Regel bearbeiten");
		m.put("Edit this secret", "Dieses Geheimnis bearbeiten");
		m.put("Edit this state", "Diesen Status bearbeiten");
		m.put("Edit title", "Titel bearbeiten");
		m.put("Edit with AI", "Mit KI bearbeiten");
		m.put("Edit {0}", "{0} bearbeiten");
		m.put("Editable Issue Fields", "Bearbeitbare Problemfelder");
		m.put("Editable Issue Links", "Bearbeitbare Problemlink");
		m.put("Edited by {0} {1}", "Bearbeitet von {0} {1}");
		m.put("Editor", "Editor");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "Entweder der Ziel-Branch oder der Quell-Branch hat gerade neue Commits, bitte erneut überprüfen.");
		m.put("Email", "E-Mail");
		m.put("Email Address", "E-Mail-Adresse");
		m.put("Email Address Verification", "E-Mail-Adressüberprüfung");
		m.put("Email Addresses", "E-Mail-Adressen");
		m.put("Email Templates", "E-Mail-Vorlagen");
		m.put("Email Verification", "E-Mail-Verifizierung");
		m.put("Email Verification Template", "E-Mail-Verifizierungsvorlage");
		m.put("Email address", "E-Mail-Adresse");
		m.put("Email address \"{0}\" already used by another account", "E-Mail-Adresse \"{0}\" wird bereits von einem anderen Konto verwendet");
		m.put("Email address \"{0}\" used by account \"{1}\"", "E-Mail-Adresse \"{0}\" wird von Konto \"{1}\" verwendet");
		m.put("Email address \"{0}\" used by disabled account \"{1}\"", "E-Mail-Adresse \"{0}\" wird von deaktiviertem Konto \"{1}\" verwendet");
		m.put("Email address already in use: {0}", "E-Mail-Adresse bereits verwendet: {0}");
		m.put("Email address already invited: {0}", "E-Mail-Adresse bereits eingeladen: {0}");
		m.put("Email address already used by another user", "E-Mail-Adresse wird bereits von einem anderen Benutzer verwendet");
		m.put("Email address already used: ", "E-Mail-Adresse bereits verwendet:");
		m.put("Email address to verify", "E-Mail-Adresse zur Verifizierung");
		m.put("Email addresses with <span class=\"badge badge-warning badge-sm\">ineffective</span> mark are those not belong to or not verified by key owner", 
			"E-Mail-Adressen mit <span class=\"badge badge-warning badge-sm\">ineffektiv</span>-Markierung gehören nicht zum Schlüsselinhaber oder wurden nicht von ihm verifiziert.");
		m.put("Email templates", "E-Mail-Vorlagen");
		m.put("Empty file added.", "Leere Datei hinzugefügt.");
		m.put("Empty file removed.", "Leere Datei entfernt.");
		m.put("Enable", "Aktivieren");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets", 
			"Aktivieren Sie <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Zeiterfassung</a> für dieses Projekt, um Fortschritte zu verfolgen und Zeiterfassungen zu erstellen.");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project", 
			"Aktivieren Sie <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>Paketverwaltung</a> für dieses Projekt");
		m.put("Enable Account Self Removal", "Aktivieren Sie die Selbstentfernung des Kontos");
		m.put("Enable Account Self Sign-Up", "Aktivieren Sie die Selbstregistrierung des Kontos");
		m.put("Enable All Queried Users", "Aktivieren Sie alle abgefragten Benutzer");
		m.put("Enable Anonymous Access", "Aktivieren Sie anonymen Zugriff");
		m.put("Enable Auto Backup", "Aktivieren Sie die automatische Sicherung");
		m.put("Enable Html Report Publish", "HTML-Berichtveröffentlichung aktivieren");
		m.put("Enable Selected Users", "Aktivieren Sie ausgewählte Benutzer");
		m.put("Enable Site Publish", "Site-Veröffentlichung aktivieren");
		m.put("Enable TTY Mode", "Aktivieren Sie den TTY-Modus");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "Aktivieren Sie Build-Unterstützung durch <a wicket:id=\"addFile\" class=\"link-primary\"></a>");
		m.put("Enable if visibility of this field depends on other fields", "Aktivieren Sie dies, wenn die Sichtbarkeit dieses Feldes von anderen Feldern abhängt");
		m.put("Enable if visibility of this param depends on other params", "Aktivieren Sie dies, wenn die Sichtbarkeit dieses Parameters von anderen Parametern abhängt");
		m.put("Enable this if the access token has same permissions as the owner", "Aktivieren Sie dies, wenn das Zugriffstoken dieselben Berechtigungen wie der Besitzer hat");
		m.put("Enable this option to merge the pull request automatically when ready (all reviewers approved, all required jobs passed etc.)", 
			"Aktivieren Sie diese Option, um den Pull-Request automatisch zu mergen, wenn er bereit ist (alle Gutachter haben zugestimmt, alle erforderlichen Jobs sind abgeschlossen usw.)");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Aktivieren Sie dies, um den Schritt zur Veröffentlichung des HTML-Berichts auszuführen. Um XSS-Angriffe zu vermeiden, stellen Sie sicher, dass dieser Executor nur von vertrauenswürdigen Jobs verwendet werden kann");
		m.put("Enable this to allow to run site publish step. OneDev will serve project site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Aktivieren Sie dies, um den Site-Veröffentlichungsschritt auszuführen. OneDev wird Projekt-Site-Dateien unverändert bereitstellen. Um XSS-Angriffe zu vermeiden, stellen Sie sicher, dass dieser Executor nur von vertrauenswürdigen Jobs verwendet werden kann");
		m.put("Enable this to place intermediate files required by job execution on dynamically allocated persistent volume instead of emptyDir", 
			"Aktivieren Sie dies, um Zwischendateien, die für die Jobausführung erforderlich sind, auf einem dynamisch zugewiesenen persistenten Volume anstelle von emptyDir zu platzieren");
		m.put("Enable this to process issue or pull request comments posted via email", "Aktivieren Sie dies, um Kommentare zu Problemen oder Pull-Requests zu verarbeiten, die per E-Mail gepostet wurden");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Aktivieren Sie dies, um Kommentare zu Problemen oder Pull-Requests zu verarbeiten, die per E-Mail gepostet wurden. <b class='text-danger'>HINWEIS:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub-Addressing</a> muss für die oben angegebene System-E-Mail-Adresse aktiviert sein, da OneDev diese verwendet, um Problem- und Pull-Request-Kontexte zu verfolgen");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Aktivieren Sie dies, um Kommentare zu Problemen oder Pull-Requests zu verarbeiten, die per E-Mail gepostet wurden. <b class='text-danger'>HINWEIS:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub-Addressing</a> muss für die oben angegebene System-E-Mail-Adresse aktiviert sein, da OneDev diese verwendet, um Problem- und Pull-Request-Kontexte zu verfolgen");
		m.put("Enable to allow to upload build cache generated during CI/CD job. Uploaded cache can be used by subsequent builds of the project as long as cache key matches", 
			"Aktivieren Sie dies, um den Upload des während des CI/CD-Jobs generierten Build-Caches zu ermöglichen. Hochgeladener Cache kann von nachfolgenden Builds des Projekts verwendet werden, solange der Cache-Schlüssel übereinstimmt");
		m.put("End Point", "Endpunkt");
		m.put("Enforce Conventional Commits", "Erzwingen Sie konventionelle Commits");
		m.put("Enforce Password Policy", "Passwortrichtlinie durchsetzen");
		m.put("Enforce Two-factor Authentication", "Erzwingen Sie die Zwei-Faktor-Authentifizierung");
		m.put("Enforce password policy for new users", "Passwortrichtlinie für neue Benutzer durchsetzen");
		m.put("Enter New Password", "Neues Passwort eingeben");
		m.put("Enter description here", "Geben Sie hier eine Beschreibung ein");
		m.put("Enter your details to login to your account", "Geben Sie Ihre Daten ein, um sich in Ihrem Konto anzumelden");
		m.put("Enter your user name or email to reset password", "Geben Sie Ihren Benutzernamen oder Ihre E-Mail ein, um das Passwort zurückzusetzen");
		m.put("Entries", "Einträge");
		m.put("Entry", "Eintrag");
		m.put("Enumeration", "Aufzählung");
		m.put("Env Var", "Umgebungsvariable");
		m.put("Environment Variables", "Umgebungsvariablen");
		m.put("Environment variable <code>serverUrl</code> in above command is taken from OneDev server url specified in <i>Administration / System Setting</i>. Change it if necessary", 
			"Die Umgebungsvariable <code>serverUrl</code> im obigen Befehl wird aus der OneDev-Server-URL übernommen, die in <i>Administration / System Setting</i> angegeben ist. Ändern Sie sie bei Bedarf");
		m.put("Equal", "Gleich");
		m.put("Error authenticating user", "Fehler bei der Authentifizierung des Benutzers");
		m.put("Error calculating commits: check log for details", "Fehler beim Berechnen der Commits: Überprüfen Sie das Protokoll für Details");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "Fehler beim Cherry-Picking nach {0}: Merge-Konflikte erkannt");
		m.put("Error cherry-picking to {0}: {1}", "Fehler beim Cherry-Picking nach {0}: {1}");
		m.put("Error detail of content type &quot;text/plain&quot;", "Fehlerdetails des Inhaltstyps &quot;text/plain&quot;");
		m.put("Error discovering OIDC metadata", "Fehler beim Entdecken von OIDC-Metadaten");
		m.put("Error executing task", "Fehler beim Ausführen der Aufgabe");
		m.put("Error parsing %sbase query: ", "Fehler beim Parsen der %sbase-Abfrage:");
		m.put("Error parsing %squery: ", "Fehler beim Parsen der %squery:");
		m.put("Error parsing build spec", "Fehler beim Parsen der Build-Spezifikation");
		m.put("Error rendering widget, check server log for details", "Fehler beim Rendern des Widgets, überprüfen Sie das Serverprotokoll für Details");
		m.put("Error reverting on {0}: Merge conflicts detected", "Fehler beim Revertieren auf {0}: Merge-Konflikte erkannt");
		m.put("Error reverting on {0}: {1}", "Fehler beim Revertieren auf {0}: {1}");
		m.put("Error validating auto merge commit message: {0}", "Fehler beim Validieren der Auto-Merge-Commit-Nachricht: {0}");
		m.put("Error validating build spec (location: {0}, error message: {1})", "Fehler beim Validieren der Build-Spezifikation (Ort: {0}, Fehlermeldung: {1})");
		m.put("Error validating build spec: {0}", "Fehler beim Validieren der Build-Spezifikation: {0}");
		m.put("Error validating commit message of \"{0}\": {1}", "Fehler beim Validieren der Commit-Nachricht von \"{0}\": {1}");
		m.put("Error validating commit message of <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}", 
			"Fehler beim Validieren der Commit-Nachricht von <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}");
		m.put("Error verifying GPG signature", "Fehler beim Überprüfen der GPG-Signatur");
		m.put("Estimated Time", "Geschätzte Zeit");
		m.put("Estimated Time Edit Bean", "Bearbeitungs-Bean für geschätzte Zeit");
		m.put("Estimated Time Issue Field", "Feld für geschätzte Zeit bei Problemen");
		m.put("Estimated Time:", "Geschätzte Zeit:");
		m.put("Estimated time", "Geschätzte Zeit");
		m.put("Estimated/Spent time. Click for details", "Geschätzte/Verbrauchte Zeit. Klicken Sie für Details");
		m.put("Evaluate script to get choices", "Script auswerten, um Auswahlmöglichkeiten zu erhalten");
		m.put("Evaluate script to get default value", "Script auswerten, um Standardwert zu erhalten");
		m.put("Evaluate script to get value or secret", "Script auswerten, um Wert oder Geheimnis zu erhalten");
		m.put("Evaluate script to get values or secrets", "Script auswerten, um Werte oder Geheimnisse zu erhalten");
		m.put("Event Types", "Ereignistypen");
		m.put("Events", "Ereignisse");
		m.put("Ever Used Since", "Seit jeher verwendet");
		m.put("Everything inside this project and all child projects will be deleted and can not be recovered, please type project path <code>{0}</code> below to confirm deletion.", 
			"Alles innerhalb dieses Projekts und aller untergeordneten Projekte wird gelöscht und kann nicht wiederhergestellt werden. Bitte geben Sie den Projektpfad <code>{0}</code> unten ein, um die Löschung zu bestätigen.");
		m.put("Example", "Beispiel");
		m.put("Example Plugin Setting", "Beispiel-Plugin-Einstellung");
		m.put("Example Property", "Beispiel-Eigenschaft");
		m.put("Exclude Param Combos", "Param-Kombinationen ausschließen");
		m.put("Exclude States", "Zustände ausschließen");
		m.put("Excluded", "Ausgeschlossen");
		m.put("Excluded Fields", "Ausgeschlossene Felder");
		m.put("Executable", "Ausführbar");
		m.put("Execute Commands", "Befehle ausführen");
		m.put("Execute Commands via SSH", "Befehle über SSH ausführen");
		m.put("Exit Impersonation", "Impersonation beenden");
		m.put("Exited impersonation", "Impersonation beendet");
		m.put("Expand all", "Alle erweitern");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", 
			"Erwartet eine oder mehrere <tt>&lt;number&gt;(h|m)</tt>. Zum Beispiel repräsentiert <tt>1h 1m</tt> 1 Stunde und 1 Minute");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", 
			"Erwartet eine oder mehrere <tt>&lt;number&gt;(w|d|h|m)</tt>. Zum Beispiel repräsentiert <tt>1w 1d 1h 1m</tt> 1 Woche ({0} Tage), 1 Tag ({1} Stunden), 1 Stunde und 1 Minute");
		m.put("Expiration Date:", "Ablaufdatum:");
		m.put("Expire Date", "Ablaufdatum");
		m.put("Expired", "Abgelaufen");
		m.put("Explicit SSL (StartTLS)", "Explizites SSL (StartTLS)");
		m.put("Export", "Exportieren");
		m.put("Export All Queried Issues To...", "Alle abgefragten Probleme exportieren nach...");
		m.put("Export CSV", "CSV exportieren");
		m.put("Export XLSX", "XLSX exportieren");
		m.put("Export as OCI layout", "Als OCI-Layout exportieren");
		m.put("Extend Trial Subscription", "Testabonnement verlängern");
		m.put("External Authentication", "Externe Authentifizierung");
		m.put("External Issue Transformers", "Externe Problemtransformatoren");
		m.put("External Participants", "Externe Teilnehmer");
		m.put("External Password Authenticator", "Externer Passwort-Authenticator");
		m.put("External System", "Externes System");
		m.put("External authenticator settings saved", "Einstellungen des externen Authentifikators gespeichert");
		m.put("External participants do not have accounts and involve in the issue via email", "Externe Teilnehmer haben keine Konten und sind über E-Mail an dem Problem beteiligt");
		m.put("Extract the package into a folder. <b class=\"text-danger\">Warning:</b> On Mac OS X, do not extract to Mac managed folders such as Downloads, Desktop, Documents; otherwise you may encounter permission issues starting agent", 
			"Extrahieren Sie das Paket in einen Ordner. <b class=\"text-danger\">Warnung:</b> Unter Mac OS X extrahieren Sie nicht in von Mac verwaltete Ordner wie Downloads, Desktop, Dokumente; andernfalls können Berechtigungsprobleme beim Starten des Agents auftreten");
		m.put("FAILED", "FEHLGESCHLAGEN");
		m.put("Fail Threshold", "Fehlschwelle");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"Build fehlschlagen, wenn es Schwachstellen mit oder schwerer als der angegebenen Schweregradstufe gibt");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"Build fehlschlagen, wenn es Schwachstellen mit oder schwerer als der angegebenen Schweregradstufe gibt. Beachten Sie, dass dies nur wirksam ist, wenn der Build nicht durch andere Schritte fehlgeschlagen ist");
		m.put("Failed", "Fehlgeschlagen");
		m.put("Failed to validate build spec import. Check server log for details", "Fehler beim Validieren des Build-Spezifikationsimports. Überprüfen Sie das Serverprotokoll für Details");
		m.put("Failed to verify your email address", "Fehler bei der Überprüfung Ihrer E-Mail-Adresse");
		m.put("Field Bean", "Feld-Bean");
		m.put("Field Instance", "Feldinstanz");
		m.put("Field Name", "Feldname");
		m.put("Field Spec", "Feldspezifikation");
		m.put("Field Specs", "Feldspezifikationen");
		m.put("Field Value", "Feldwert");
		m.put("Fields", "Felder");
		m.put("Fields & Links", "Felder & Links");
		m.put("Fields And Links Bean", "Felder und Links Bean");
		m.put("Fields to Change", "Zu ändernde Felder");
		m.put("File", "Datei");
		m.put("File Changes", "Dateiänderungen");
		m.put("File Name", "Dateiname");
		m.put("File Name Patterns (separated by comma)", "Dateinamenmuster (durch Komma getrennt)");
		m.put("File Path", "Dateipfad");
		m.put("File Patterns", "Dateimuster");
		m.put("File Protection", "Dateischutz");
		m.put("File Protections", "Dateischutzmaßnahmen");
		m.put("File and Symbol Search", "Datei- und Symbolsuche");
		m.put("File changes", "Dateiänderungen");
		m.put("File is too large to edit here", "Datei ist zu groß, um hier bearbeitet zu werden");
		m.put("File missing or obsolete", "Datei fehlt oder ist veraltet");
		m.put("File name", "Dateiname");
		m.put("File name patterns such as *.java, *.c", "Dateinamenmuster wie *.java, *.c");
		m.put("Files", "Dateien");
		m.put("Files to Be Analyzed", "Zu analysierende Dateien");
		m.put("Filter", "Filter");
		m.put("Filter Issues", "Filterprobleme");
		m.put("Filter actions", "Aktionen filtern");
		m.put("Filter backlog issues", "Filter Rückstandprobleme");
		m.put("Filter branches...", "Filter Zweige...");
		m.put("Filter by name", "Filter nach Name");
		m.put("Filter by name or email address", "Filter nach Name oder E-Mail-Adresse");
		m.put("Filter by name...", "Filter nach Name...");
		m.put("Filter by path", "Filter nach Pfad");
		m.put("Filter by test suite", "Filter nach Test-Suite");
		m.put("Filter date range", "Datumsbereich filtern");
		m.put("Filter files...", "Filter Dateien...");
		m.put("Filter groups...", "Filter Gruppen...");
		m.put("Filter issues", "Filter Probleme");
		m.put("Filter pull requests", "Filter Pull-Requests");
		m.put("Filter roles", "Filter Rollen");
		m.put("Filter tags...", "Filter Tags...");
		m.put("Filter targets", "Filter Ziele");
		m.put("Filter users", "Benutzer filtern");
		m.put("Filter...", "Filter...");
		m.put("Filters", "Filter");
		m.put("Find branch", "Zweig finden");
		m.put("Find or create branch", "Zweig finden oder erstellen");
		m.put("Find or create tag", "Tag finden oder erstellen");
		m.put("Find tag", "Tag finden");
		m.put("Fingerprint", "Fingerabdruck");
		m.put("Finish", "Fertigstellen");
		m.put("First applicable executor", "Erster anwendbarer Executor");
		m.put("Fix", "Beheben");
		m.put("Fix Type", "Behebungstyp");
		m.put("Fix Undefined Field Values", "Undefinierte Feldwerte beheben");
		m.put("Fix Undefined Fields", "Undefinierte Felder beheben");
		m.put("Fix Undefined States", "Undefinierte Zustände beheben");
		m.put("Fixed Issues", "Behobene Probleme");
		m.put("Fixed issues since...", "Behobene Probleme seit...");
		m.put("Fixing Builds", "Builds beheben");
		m.put("Fixing Commits", "Commits beheben");
		m.put("Fixing...", "Beheben...");
		m.put("Float", "Fließkommazahl");
		m.put("Follow below instructions to publish packages into this project", "Befolgen Sie die unten stehenden Anweisungen, um Pakete in dieses Projekt zu veröffentlichen");
		m.put("Follow below steps to install agent on remote machine (supports Linux/Windows/Mac OS X/FreeBSD):", 
			"Befolgen Sie die unten stehenden Schritte, um den Agenten auf einer Remote-Maschine zu installieren (unterstützt Linux/Windows/Mac OS X/FreeBSD):");
		m.put("For CI/CD job, add this gem to Gemfile like below", "Für CI/CD-Job, fügen Sie dieses Gem wie unten gezeigt zur Gemfile hinzu");
		m.put("For CI/CD job, add this package to requirements.txt and run below to install the package via command step", 
			"Für CI/CD-Job, fügen Sie dieses Paket zu requirements.txt hinzu und führen Sie unten aus, um das Paket über den Befehls-Schritt zu installieren");
		m.put("For CI/CD job, run below to add package repository via command step", "Für CI/CD-Job, führen Sie unten aus, um das Paket-Repository über den Befehls-Schritt hinzuzufügen");
		m.put("For CI/CD job, run below to add package source via command step", "Für CI/CD-Job, führen Sie unten aus, um die Paketquelle über den Befehls-Schritt hinzuzufügen");
		m.put("For CI/CD job, run below to add source via command step", "Für CI/CD-Job, führen Sie unten aus, um die Quelle über den Befehls-Schritt hinzuzufügen");
		m.put("For CI/CD job, run below to install chart via command step", "Für CI/CD-Job, führen Sie unten aus, um das Chart über den Befehls-Schritt zu installieren");
		m.put("For CI/CD job, run below to publish package via command step", "Für CI/CD-Job, führen Sie unten aus, um das Paket über den Befehls-Schritt zu veröffentlichen");
		m.put("For CI/CD job, run below to push chart to the repository via command step", "Für CI/CD-Job, führen Sie unten aus, um das Chart über den Befehls-Schritt in das Repository zu pushen");
		m.put("For CI/CD job, run below via a command step", "Für CI/CD-Job, führen Sie unten über einen Befehls-Schritt aus");
		m.put("For a particular project, the first matching entry will be used", "Für ein bestimmtes Projekt wird der erste passende Eintrag verwendet");
		m.put("For all issues", "Für alle Probleme");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"Für Build-Commit, der vom Standardzweig nicht erreichbar ist, sollte ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> als Zugriffstoken mit Berechtigung zum Erstellen von Zweigen angegeben werden");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"Für Build-Commit, der vom Standardzweig nicht erreichbar ist, sollte ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> als Zugriffstoken mit Berechtigung zum Erstellen von Tags angegeben werden");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"Für Build-Commit, der vom Standardzweig nicht erreichbar ist, sollte ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> als Zugriffstoken mit Berechtigung zum Verwalten von Problemen angegeben werden");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"Für Docker-fähige Executor befindet sich dieser Pfad im Container und akzeptiert sowohl absolute als auch relative Pfade (relativ zu <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a>). Für Shell-bezogene Executor, die direkt auf der Host-Maschine laufen, werden nur relative Pfade akzeptiert");
		m.put("For each build, OneDev calculates a list of fixed issues since previous build automatically. This setting provides a default query to further filter/order this list. For a given job, the first matching entry will be used.", 
			"Für jeden Build berechnet OneDev automatisch eine Liste von behobenen Problemen seit dem vorherigen Build. Diese Einstellung bietet eine Standardabfrage, um diese Liste weiter zu filtern/zu ordnen. Für einen bestimmten Job wird der erste passende Eintrag verwendet.");
		m.put("For each selected branch/tag, a separate build will be generated with branch/tag set to corresponding value", 
			"Für jeden ausgewählten Zweig/Tag wird ein separater Build generiert, wobei der Zweig/Tag auf den entsprechenden Wert gesetzt wird");
		m.put("For issues matching: ", "Für Probleme, die übereinstimmen:");
		m.put("For very large git repository, you may need to tune options here to reduce memory usage", 
			"Für sehr große Git-Repositories müssen Sie möglicherweise Optionen hier anpassen, um den Speicherverbrauch zu reduzieren");
		m.put("For web hooks defined here and in parent projects, OneDev will post event data in JSON format to specified URLs when subscribed events happen", 
			"Für Webhooks, die hier und in übergeordneten Projekten definiert sind, sendet OneDev Ereignisdaten im JSON-Format an die angegebenen URLs, wenn abonnierte Ereignisse auftreten");
		m.put("Force", "Erzwingen");
		m.put("Force Garbage Collection", "Müllsammlung erzwingen");
		m.put("Forgot Password?", "Passwort vergessen?");
		m.put("Forgotten Password?", "Passwort vergessen?");
		m.put("Fork Project", "Projekt forken");
		m.put("Fork now", "Jetzt forken");
		m.put("Forks Of", "Forks von");
		m.put("Frequencies", "Frequenzen");
		m.put("From Directory", "Aus Verzeichnis");
		m.put("From States", "Aus Zuständen");
		m.put("From extracted folder, run <code>bin\\agent.bat console</code> as administrator on Windows or <code>bin/agent.sh console</code> on other OS", 
			"Aus dem extrahierten Ordner führen Sie <code>bin\\agent.bat console</code> als Administrator unter Windows oder <code>bin/agent.sh console</code> unter anderen Betriebssystemen aus");
		m.put("From {0}", "Von {0}");
		m.put("Full Name", "Vollständiger Name");
		m.put("Furthest due date", "Spätestes Fälligkeitsdatum");
		m.put("GPG Keys", "GPG-Schlüssel");
		m.put("GPG Public Key", "GPG-Öffentlicher Schlüssel");
		m.put("GPG Signing Key", "GPG-Signaturschlüssel");
		m.put("GPG Trusted Keys", "GPG-Vertrauenswürdige Schlüssel");
		m.put("GPG key deleted", "GPG-Schlüssel gelöscht");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "GPG-Öffentlicher Schlüssel beginnt mit '-----BEGIN PGP PUBLIC KEY BLOCK-----'");
		m.put("GPG signing key will be used to sign commits generated by OneDev, including pull request merge commits, user commits created via web UI or RESTful api.", 
			"GPG-Signaturschlüssel wird verwendet, um von OneDev generierte Commits zu signieren, einschließlich Pull-Request-Merge-Commits, Benutzer-Commits, die über die Web-UI oder RESTful-API erstellt wurden.");
		m.put("Gem Info", "Edelstein Info");
		m.put("General", "Allgemein");
		m.put("General Settings", "Allgemeine Einstellungen");
		m.put("General settings updated", "Allgemeine Einstellungen aktualisiert");
		m.put("Generate", "Generieren");
		m.put("Generate File Checksum", "Datei-Prüfsumme generieren");
		m.put("Generate New", "Neu generieren");
		m.put("Generic LDAP", "Generisches LDAP");
		m.put("Get", "Holen");
		m.put("Get Groups Using Attribute", "Gruppen mit Attribut abrufen");
		m.put("Git", "Git");
		m.put("Git Command Line", "Git-Befehlszeile");
		m.put("Git Credential", "Git-Anmeldeinformationen");
		m.put("Git LFS Storage", "Git LFS-Speicher");
		m.put("Git Lfs Lock", "Git LFS-Sperre");
		m.put("Git Location", "Git-Standort");
		m.put("Git Pack Config", "Git Pack-Konfiguration");
		m.put("Git Path", "Git-Pfad");
		m.put("Git email address will be used as git author/committer for commits created on web UI", 
			"Git-E-Mail-Adresse wird als Git-Autor/Committer für auf der Web-UI erstellte Commits verwendet");
		m.put("Git pack config updated", "Git Pack-Konfiguration aktualisiert");
		m.put("GitHub", "GitHub");
		m.put("GitHub API URL", "GitHub API-URL");
		m.put("GitHub Issue Label", "GitHub-Issue-Label");
		m.put("GitHub Organization", "GitHub-Organisation");
		m.put("GitHub Personal Access Token", "GitHub-Persönlicher Zugriffstoken");
		m.put("GitHub Repositories to Import", "GitHub-Repositorys zum Importieren");
		m.put("GitHub Repository", "GitHub-Repository");
		m.put("GitHub personal access token should be generated with scope <b>repo</b> and <b>read:org</b>", 
			"GitHub-Persönlicher Zugriffstoken sollte mit den Berechtigungen <b>repo</b> und <b>read:org</b> generiert werden");
		m.put("GitLab API URL", "GitLab API-URL");
		m.put("GitLab Group", "GitLab-Gruppe");
		m.put("GitLab Issue Label", "GitLab-Issue-Label");
		m.put("GitLab Personal Access Token", "GitLab-Persönlicher Zugriffstoken");
		m.put("GitLab Project", "GitLab-Projekt");
		m.put("GitLab Projects to Import", "GitLab-Projekte zum Importieren");
		m.put("GitLab personal access token should be generated with scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by user of specified access token will be listed", 
			"GitLab-Persönlicher Zugriffstoken sollte mit den Berechtigungen <b>read_api</b>, <b>read_user</b> und <b>read_repository</b> generiert werden. Beachten Sie, dass nur Gruppen/Projekte des Benutzers des angegebenen Zugriffstokens aufgelistet werden");
		m.put("Gitea API URL", "Gitea API-URL");
		m.put("Gitea Issue Label", "Gitea-Issue-Label");
		m.put("Gitea Organization", "Gitea-Organisation");
		m.put("Gitea Personal Access Token", "Gitea-Persönlicher Zugriffstoken");
		m.put("Gitea Repositories to Import", "Gitea-Repositorys zum Importieren");
		m.put("Gitea Repository", "Gitea-Repository");
		m.put("Github Access Token Secret", "GitHub-Zugriffstoken-Geheimnis");
		m.put("Global", "Global");
		m.put("Global Build Setting", "Globale Build-Einstellung");
		m.put("Global Issue Setting", "Globale Issue-Einstellung");
		m.put("Global Pack Setting", "Globale Pack-Einstellung");
		m.put("Global Views", "Globale Ansichten");
		m.put("Gmail", "Gmail");
		m.put("Go Back", "Zurückgehen");
		m.put("Google Test Report", "Google-Testbericht");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Gpg-Schlüssel");
		m.put("Great, your mail service configuration is working", "Großartig, Ihre Mail-Dienst-Konfiguration funktioniert");
		m.put("Groovy Script", "Groovy-Skript");
		m.put("Groovy Scripts", "Groovy-Skripte");
		m.put("Groovy script to be evaluated. It should return a <i>Date</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Es sollte einen <i>Date</i>-Wert zurückgeben. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. It should return a <i>Float</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Es sollte einen <i>Float</i>-Wert zurückgeben. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. It should return a <i>Integer</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Es sollte einen <i>Integer</i>-Wert zurückgeben. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. It should return a <i>String</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Es sollte einen <i>String</i>-Wert zurückgeben. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. It should return a <i>boolean</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Es sollte einen <i>boolean</i>-Wert zurückgeben. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. It should return a <i>string</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Es sollte einen <i>string</i>-Wert zurückgeben. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. It should return name of a group. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Es sollte den Namen einer Gruppe zurückgeben. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. It should return string or list of string. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Es sollte einen String oder eine Liste von Strings zurückgeben. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Der Rückgabewert sollte eine Liste von Gruppenfassadenobjekten sein, die als Auswahlmöglichkeiten verwendet werden. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. The return value should be a list of user login names to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Der Rückgabewert sollte eine Liste von Benutzeranmeldenamen sein, die als Auswahlmöglichkeiten verwendet werden. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy script to be evaluated. The return value should be a value to color map, for instance:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> if the value does not have a color. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Groovy-Skript zur Auswertung. Der Rückgabewert sollte eine Wert-zu-Farb-Zuordnung sein, zum Beispiel:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>. Verwenden Sie <tt>null</tt>, wenn der Wert keine Farbe hat. Weitere Details finden Sie in der <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>Scripting-Hilfe</a>");
		m.put("Groovy scripts", "Groovy-Skripte");
		m.put("Group", "Gruppe");
		m.put("Group \"{0}\" deleted", "Gruppe \"{0}\" gelöscht");
		m.put("Group Authorization Bean", "Gruppen-Autorisierungs-Bean");
		m.put("Group Authorizations", "Gruppen-Autorisierungen");
		m.put("Group Authorizations Bean", "Gruppen-Autorisierungs-Bean");
		m.put("Group By", "Gruppieren nach");
		m.put("Group Management", "Gruppenverwaltung");
		m.put("Group Name Attribute", "Gruppenname-Attribut");
		m.put("Group Retrieval", "Gruppenabruf");
		m.put("Group Search Base", "Gruppensuchbasis");
		m.put("Group Search Filter", "Gruppensuchfilter");
		m.put("Group authorizations updated", "Gruppen-Autorisierungen aktualisiert");
		m.put("Group created", "Gruppe erstellt");
		m.put("Groups", "Gruppen");
		m.put("Groups Claim", "Gruppenanspruch");
		m.put("Guide Line", "Richtlinie");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "HTTP(S)-Klon-URL");
		m.put("Has Owner Permissions", "Hat Besitzerberechtigungen");
		m.put("Has Running Builds", "Hat laufende Builds");
		m.put("Heap Memory Usage", "Heap-Speichernutzung");
		m.put("Helm(s)", "Helm(s)");
		m.put("Help", "Hilfe");
		m.put("Hide", "Ausblenden");
		m.put("Hide Archived", "Archivierte ausblenden");
		m.put("Hide comment", "Kommentar ausblenden");
		m.put("Hide saved queries", "Gespeicherte Abfragen ausblenden");
		m.put("High", "Hoch");
		m.put("High Availability & Scalability", "Hohe Verfügbarkeit & Skalierbarkeit");
		m.put("High Severity", "Hohe Schwere");
		m.put("History", "Verlauf");
		m.put("History of comparing revisions is unrelated", "Verlauf des Vergleichs von Revisionen ist nicht zusammenhängend");
		m.put("History of target branch and source branch is unrelated", "Verlauf des Ziel- und Quellzweigs ist nicht zusammenhängend");
		m.put("Host name or ip address of remote machine to run commands via SSH", "Hostname oder IP-Adresse der entfernten Maschine, um Befehle über SSH auszuführen");
		m.put("Hours Per Day", "Stunden pro Tag");
		m.put("How to Publish", "Wie veröffentlichen");
		m.put("Html Report", "HTML-Bericht");
		m.put("Http Method", "HTTP-Methode");
		m.put("I didn't eat it. I swear!", "Ich habe es nicht gegessen. Ich schwöre!");
		m.put("ID token was expired", "ID-Token ist abgelaufen");
		m.put("IMAP Host", "IMAP-Host");
		m.put("IMAP Password", "IMAP-Passwort");
		m.put("IMAP User", "IMAP-Benutzer");
		m.put("IMPORTANT:", "WICHTIG:");
		m.put("IP Address", "IP-Adresse");
		m.put("Id", "ID");
		m.put("Identify Field", "Feld identifizieren");
		m.put("If enabled, scheduled backup will run on lead server which is <span wicket:id=\"leadServer\"></span> currently", 
			"Wenn aktiviert, wird das geplante Backup auf dem Hauptserver ausgeführt, der <span wicket:id=\"leadServer\"></span> derzeit ist");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"Wenn aktiviert, wird der Quellzweig automatisch gelöscht, nachdem der Pull-Request zusammengeführt wurde, wenn der Benutzer die Berechtigung dazu hat");
		m.put("If specified, OneDev will only display iterations with this prefix", "Wenn angegeben, zeigt OneDev nur Iterationen mit diesem Präfix an");
		m.put("If specified, all public and internal projects imported from GitLab will use these as default roles. Private projects are not affected", 
			"Wenn angegeben, werden alle öffentlichen und internen Projekte, die von GitLab importiert werden, diese als Standardrollen verwenden. Private Projekte sind nicht betroffen");
		m.put("If specified, all public repositories imported from GitHub will use these as default roles. Private repositories are not affected", 
			"Wenn angegeben, werden alle öffentlichen Repositories, die von GitHub importiert werden, diese als Standardrollen verwenden. Private Repositories sind nicht betroffen");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"Wenn angegeben, wird die geschätzte/aufgewendete Gesamtzeit eines Problems auch verknüpfte Probleme dieses Typs einschließen");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"Wenn diese Option aktiviert ist, muss der Befehl git lfs auf dem OneDev-Server installiert sein (auch wenn dieser Schritt auf einem anderen Knoten ausgeführt wird)");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"Wenn aktiviert, kann die Gruppe, die durch dieses Feld angegeben wird, die geschätzte Zeit der entsprechenden Probleme bearbeiten, wenn die Zeiterfassung aktiviert ist");
		m.put("Ignore", "Ignorieren");
		m.put("Ignore File", "Datei ignorieren");
		m.put("Ignore activities irrelevant to me", "Aktivitäten ignorieren, die für mich irrelevant sind");
		m.put("Ignore all", "Alles ignorieren");
		m.put("Ignore all whitespace", "Alle Leerzeichen ignorieren");
		m.put("Ignore change", "Änderung ignorieren");
		m.put("Ignore change whitespace", "Änderungs-Leerzeichen ignorieren");
		m.put("Ignore leading", "Führende ignorieren");
		m.put("Ignore leading whitespace", "Führende Leerzeichen ignorieren");
		m.put("Ignore this field", "Dieses Feld ignorieren");
		m.put("Ignore this param", "Diesen Parameter ignorieren");
		m.put("Ignore trailing", "Nachfolgende ignorieren");
		m.put("Ignore trailing whitespace", "Nachfolgende Leerzeichen ignorieren");
		m.put("Ignored Licenses", "Ignorierte Lizenzen");
		m.put("Image", "Bild");
		m.put("Image Labels", "Bildbeschriftungen");
		m.put("Image Manifest", "Bildmanifest");
		m.put("Image Size", "Bildgröße");
		m.put("Image Text", "Bildtext");
		m.put("Image URL", "Bild-URL");
		m.put("Image URL should be specified", "Bild-URL sollte angegeben werden");
		m.put("Imap Ssl Setting", "IMAP-SSL-Einstellung");
		m.put("Imap With Ssl", "IMAP mit SSL");
		m.put("Impersonate", "Imitieren");
		m.put("Implicit SSL", "Implizites SSL");
		m.put("Import", "Importieren");
		m.put("Import All Projects", "Alle Projekte importieren");
		m.put("Import All Repositories", "Alle Repositories importieren");
		m.put("Import Group", "Gruppe importieren");
		m.put("Import Issues", "Probleme importieren");
		m.put("Import Option", "Importoption");
		m.put("Import Organization", "Organisation importieren");
		m.put("Import Project", "Projekt importieren");
		m.put("Import Projects", "Projekte importieren");
		m.put("Import Repositories", "Repositories importieren");
		m.put("Import Repository", "Repository importieren");
		m.put("Import Server", "Server importieren");
		m.put("Import Workspace", "Arbeitsbereich importieren");
		m.put("Import build spec elements (jobs, services, step templates and properties) from other projects. Imported elements are treated as if they are defined locally. Locally defined elements will override imported elements with same name", 
			"Build-Spezifikationselemente (Jobs, Dienste, Schrittvorlagen und Eigenschaften) aus anderen Projekten importieren. Importierte Elemente werden behandelt, als ob sie lokal definiert wären. Lokal definierte Elemente überschreiben importierte Elemente mit demselben Namen");
		m.put("Importing Issues from {0}", "Probleme werden aus {0} importiert");
		m.put("Importing from {0}", "Importieren aus {0}");
		m.put("Importing issues into currrent project. Please note that issue numbers will only be retained if the whole project fork graph does not have any issues to avoid duplicate issue numbers", 
			"Probleme werden in das aktuelle Projekt importiert. Bitte beachten Sie, dass die Problemnummern nur beibehalten werden, wenn der gesamte Projekt-Fork-Graph keine Probleme hat, um doppelte Problemnummern zu vermeiden");
		m.put("Importing projects from {0}", "Projekte werden aus {0} importiert");
		m.put("Imports", "Importe");
		m.put("In Projects", "In Projekten");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"Falls das IMAP-Host-Zertifikat selbstsigniert ist oder dessen CA-Root nicht akzeptiert wird, können Sie OneDev anweisen, die Zertifikatsprüfung zu umgehen. <b class='text-danger'>WARNUNG: </b> In einem unzuverlässigen Netzwerk kann dies zu einem Man-in-the-Middle-Angriff führen, und Sie sollten stattdessen <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>das Zertifikat in OneDev importieren</a>");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"Falls das SMTP-Host-Zertifikat selbstsigniert ist oder dessen CA-Root nicht akzeptiert wird, können Sie OneDev anweisen, die Zertifikatsprüfung zu umgehen. <b class='text-danger'>WARNUNG: </b> In einem unzuverlässigen Netzwerk kann dies zu einem Man-in-the-Middle-Angriff führen, und Sie sollten stattdessen <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>das Zertifikat in OneDev importieren</a>");
		m.put("In case anonymous access is disabled or anonymous user does not have enough permission for a resource operation, you will need to authenticate by providing user name and password (or access token) via http basic auth header", 
			"Falls anonymer Zugriff deaktiviert ist oder der anonyme Benutzer nicht genügend Berechtigungen für eine Ressourcenoperation hat, müssen Sie sich authentifizieren, indem Sie Benutzername und Passwort (oder Zugriffstoken) über den HTTP-Basic-Auth-Header bereitstellen");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"Falls der Cache über den oben genannten Schlüssel nicht getroffen wird, wird OneDev die hier definierten Lade-Schlüssel durchlaufen, bis ein passender Cache in der Projekt-Hierarchie gefunden wird. Ein Cache wird als passend angesehen, wenn sein Schlüssel mit dem Lade-Schlüssel beginnt. Wenn mehrere Caches passen, wird der neueste Cache zurückgegeben");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"Falls der Cache hochgeladen werden muss, gibt diese Eigenschaft das Zielprojekt für den Upload an. Leer lassen für aktuelles Projekt");
		m.put("In case the pull request status is out of sync with underlying repository, you may synchronize them manually here", 
			"Falls der Status des Pull-Requests nicht mit dem zugrunde liegenden Repository synchron ist, können Sie sie hier manuell synchronisieren");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"Falls die Benutzergruppenmitgliedschaft auf der Gruppenseite verwaltet wird, gibt diese Eigenschaft den Basisknoten für die Gruppensuche an. Zum Beispiel: <i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"Falls die Benutzergruppenbeziehung auf der Gruppenseite verwaltet wird, wird dieser Filter verwendet, um die zugehörigen Gruppen des aktuellen Benutzers zu bestimmen. Zum Beispiel: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In diesem Beispiel stellt <i>{0}</i> die DN des aktuellen Benutzers dar");
		m.put("In case you are using external issue tracker, you can define transformers to transform external issue references into external issue links in various places, such as commit messages and pull request descriptions", 
			"Falls Sie einen externen Problem-Tracker verwenden, können Sie Transformatoren definieren, um externe Problemreferenzen in externe Problemlink in verschiedenen Bereichen zu transformieren, wie Commit-Nachrichten und Pull-Request-Beschreibungen");
		m.put("In rare cases, your issues might be out of sync with workflow settings (undefined state/field etc.). Run integrity check below to find problems and get them fixed.", 
			"In seltenen Fällen können Ihre Probleme nicht mit den Workflow-Einstellungen synchron sein (undefinierter Zustand/Feld usw.). Führen Sie unten eine Integritätsprüfung durch, um Probleme zu finden und zu beheben.");
		m.put("Inbox Poll Setting", "Posteingang-Abfrageeinstellung");
		m.put("Include Child Projects", "Unterprojekte einbeziehen");
		m.put("Include Disabled", "Deaktivierte einbeziehen");
		m.put("Include Forks", "Forks einbeziehen");
		m.put("Include When Issue is Opened", "Einbeziehen, wenn das Problem geöffnet wird");
		m.put("Incompatibilities", "Inkompatibilitäten");
		m.put("Inconsistent issuer in provider metadata and ID token", "Inkonsistenter Aussteller in Providermetadaten und ID-Token");
		m.put("Indicator", "Indikator");
		m.put("Inherit from parent", "Vom Elternteil erben");
		m.put("Inherited", "Geerbt");
		m.put("Input Spec", "Eingabe-Spezifikation");
		m.put("Input URL", "Eingabe-URL");
		m.put("Input allowed CORS origin, hit ENTER to add", "Geben Sie die erlaubte CORS-Ursprung ein, drücken Sie ENTER, um hinzuzufügen");
		m.put("Input revision", "Eingabe-Revision");
		m.put("Input title", "Eingabe-Titel");
		m.put("Input title here", "Eingabe-Titel hier");
		m.put("Input user search base. Hit ENTER to add", "Geben Sie die Benutzer-Suchbasis ein. Drücken Sie ENTER, um hinzuzufügen");
		m.put("Input user search bases. Hit ENTER to add", "Geben Sie die Benutzer-Suchbasen ein. Drücken Sie ENTER, um hinzuzufügen");
		m.put("Insert", "Einfügen");
		m.put("Insert Image", "Bild einfügen");
		m.put("Insert Link", "Link einfügen");
		m.put("Insert link to this file", "Link zu dieser Datei einfügen");
		m.put("Insert this image", "Dieses Bild einfügen");
		m.put("Install Subscription Key", "Abonnement-Schlüssel installieren");
		m.put("Integer", "Ganzzahl");
		m.put("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Interaktiver Web-Shell-Zugriff auf laufende Jobs ist eine Enterprise-Funktion. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("Internal Database", "Interne Datenbank");
		m.put("Interpreter", "Interpreter");
		m.put("Invalid GPG signature", "Ungültige GPG-Signatur");
		m.put("Invalid PCRE syntax", "Ungültige PCRE-Syntax");
		m.put("Invalid access token: {0}", "Ungültiges Zugriffstoken: {0}");
		m.put("Invalid credentials", "Ungültige Anmeldedaten");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "Ungültiger Datumsbereich, erwartet \"yyyy-MM-dd bis yyyy-MM-dd\"");
		m.put("Invalid email address: {0}", "Ungültige E-Mail-Adresse: {0}");
		m.put("Invalid invitation code", "Ungültiger Einladungscode");
		m.put("Invalid issue date of ID token", "Ungültiges Ausstellungsdatum des ID-Tokens");
		m.put("Invalid issue number: {0}", "Ungültige Ausgabenummer: {0}");
		m.put("Invalid pull request number: {0}", "Ungültige Pull-Request-Nummer: {0}");
		m.put("Invalid request path", "Ungültiger Anforderungspfad");
		m.put("Invalid selection, click for details", "Ungültige Auswahl, klicken Sie für Details");
		m.put("Invalid ssh signature", "Ungültige SSH-Signatur");
		m.put("Invalid state response", "Ungültige Statusantwort");
		m.put("Invalid state. Please make sure you are visiting OneDev using server url specified in system setting", 
			"Ungültiger Zustand. Bitte stellen Sie sicher, dass Sie OneDev über die in den Systemeinstellungen angegebene Server-URL besuchen");
		m.put("Invalid subscription key", "Ungültiger Abonnement-Schlüssel");
		m.put("Invalid working period", "Ungültiger Arbeitszeitraum");
		m.put("Invitation sent to \"{0}\"", "Einladung gesendet an \"{0}\"");
		m.put("Invitation to \"{0}\" deleted", "Einladung zu \"{0}\" gelöscht");
		m.put("Invitations", "Einladungen");
		m.put("Invitations sent", "Einladungen gesendet");
		m.put("Invite", "Einladen");
		m.put("Invite Users", "Benutzer einladen");
		m.put("Is Site Admin", "Ist Site-Admin");
		m.put("Issue", "Problem");
		m.put("Issue #{0} deleted", "Problem #{0} gelöscht");
		m.put("Issue Board", "Problem-Board");
		m.put("Issue Boards", "Problem-Boards");
		m.put("Issue Close States", "Problem-Abschlussstatus");
		m.put("Issue Creation Setting", "Problem-Erstellungseinstellung");
		m.put("Issue Creation Settings", "Problem-Erstellungseinstellungen");
		m.put("Issue Custom Fields", "Problem-Benutzerdefinierte Felder");
		m.put("Issue Description", "Problem-Beschreibung");
		m.put("Issue Description Templates", "Problem-Beschreibungsvorlagen");
		m.put("Issue Details", "Problem-Details");
		m.put("Issue Field", "Problem-Feld");
		m.put("Issue Field Mapping", "Problem-Feldzuordnung");
		m.put("Issue Field Mappings", "Problem-Feldzuordnungen");
		m.put("Issue Field Set", "Problem-Feldsatz");
		m.put("Issue Fields", "Problem-Felder");
		m.put("Issue Filter", "Problem-Filter");
		m.put("Issue Import Option", "Problem-Importoption");
		m.put("Issue Label Mapping", "Problem-Label-Zuordnung");
		m.put("Issue Label Mappings", "Problem-Label-Zuordnungen");
		m.put("Issue Link", "Problem-Link");
		m.put("Issue Link Mapping", "Problem-Link-Zuordnung");
		m.put("Issue Link Mappings", "Problem-Link-Zuordnungen");
		m.put("Issue Links", "Problem-Links");
		m.put("Issue Management", "Problem-Management");
		m.put("Issue Notification", "Problem-Benachrichtigung");
		m.put("Issue Notification Template", "Problem-Benachrichtigungsvorlage");
		m.put("Issue Notification Unsubscribed", "Problem-Benachrichtigung abbestellt");
		m.put("Issue Notification Unsubscribed Template", "Problem-Benachrichtigung abbestellt Vorlage");
		m.put("Issue Pattern", "Problem-Muster");
		m.put("Issue Priority Mapping", "Problem-Prioritätszuordnung");
		m.put("Issue Priority Mappings", "Problem-Prioritätszuordnungen");
		m.put("Issue Query", "Problem-Abfrage");
		m.put("Issue Settings", "Problem-Einstellungen");
		m.put("Issue State", "Problem-Status");
		m.put("Issue State Mapping", "Problem-Statuszuordnung");
		m.put("Issue State Mappings", "Problem-Statuszuordnungen");
		m.put("Issue State Transition", "Problem-Statusübergang");
		m.put("Issue State Transitions", "Problem-Statusübergänge");
		m.put("Issue States", "Problem-Status");
		m.put("Issue Statistics", "Problem-Statistiken");
		m.put("Issue Stats", "Problem-Statistiken");
		m.put("Issue Status Mapping", "Problem-Statuszuordnung");
		m.put("Issue Status Mappings", "Problem-Statuszuordnungen");
		m.put("Issue Stopwatch Overdue", "Problem-Stoppuhr überfällig");
		m.put("Issue Stopwatch Overdue Notification Template", "Problem-Stoppuhr überfällig Benachrichtigungsvorlage");
		m.put("Issue Tag Mapping", "Problem-Tag-Zuordnung");
		m.put("Issue Tag Mappings", "Problem-Tag-Zuordnungen");
		m.put("Issue Template", "Problem-Vorlage");
		m.put("Issue Transition ({0} -> {1})", "Problem-Übergang ({0} -> {1})");
		m.put("Issue Type Mapping", "Problem-Typ-Zuordnung");
		m.put("Issue Type Mappings", "Problem-Typ-Zuordnungen");
		m.put("Issue Votes", "Problem-Stimmen");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"Problem-Administrationsberechtigung innerhalb eines Projekts, einschließlich Stapeloperationen über mehrere Probleme");
		m.put("Issue count", "Problemanzahl");
		m.put("Issue in state", "Problem im Status");
		m.put("Issue list", "Problemliste");
		m.put("Issue management not enabled in this project", "Problem-Management in diesem Projekt nicht aktiviert");
		m.put("Issue management permission required to move issues", "Problem-Management-Berechtigung erforderlich, um Probleme zu verschieben");
		m.put("Issue not exist or access denied", "Problem existiert nicht oder Zugriff verweigert");
		m.put("Issue number", "Problemnummer");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"Problemabfrage-Watch betrifft nur neue Probleme. Um den Watch-Status bestehender Probleme in Stapeln zu verwalten, filtern Sie Probleme nach Watch-Status auf der Problemseite und führen Sie dann die entsprechende Aktion aus");
		m.put("Issue state duration statistics", "Problem-Statusdauer-Statistiken");
		m.put("Issue state frequency statistics", "Problem-Statushäufigkeits-Statistiken");
		m.put("Issue state trend statistics", "Problem-Statustrend-Statistiken");
		m.put("Issue statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Problem-Statistiken sind eine Enterprise-Funktion. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("Issue workflow changed, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliation</a> needs to be performed to make data consistent. You may do this after making all necessary changes", 
			"Problem-Workflow geändert, <a wicket:id=\"reconcile\" class=\"link-primary\">Abgleich</a> muss durchgeführt werden, um die Daten konsistent zu machen. Sie können dies nach allen notwendigen Änderungen tun");
		m.put("Issues", "Probleme");
		m.put("Issues can be created in this project by sending email to this address", "Probleme können in diesem Projekt erstellt werden, indem eine E-Mail an diese Adresse gesendet wird");
		m.put("Issues copied", "Probleme kopiert");
		m.put("Issues moved", "Probleme verschoben");
		m.put("Italic", "Kursiv");
		m.put("Iteration", "Iteration");
		m.put("Iteration \"{0}\" closed", "Iteration \"{0}\" geschlossen");
		m.put("Iteration \"{0}\" deleted", "Iteration \"{0}\" gelöscht");
		m.put("Iteration \"{0}\" is closed", "Iteration \"{0}\" ist geschlossen");
		m.put("Iteration \"{0}\" is reopened", "Iteration \"{0}\" wurde wieder geöffnet");
		m.put("Iteration \"{0}\" reopened", "Iteration \"{0}\" wieder geöffnet");
		m.put("Iteration Edit Bean", "Iteration Bearbeiten Bean");
		m.put("Iteration Name", "Iterationsname");
		m.put("Iteration Names", "Iterationsnamen");
		m.put("Iteration Prefix", "Iterationspräfix");
		m.put("Iteration list", "Iterationsliste");
		m.put("Iteration saved", "Iteration gespeichert");
		m.put("Iteration spans too long to show burndown chart", "Iteration dauert zu lange, um das Burndown-Diagramm anzuzeigen");
		m.put("Iteration start and due date should be specified to show burndown chart", "Start- und Fälligkeitsdatum der Iteration müssen angegeben werden, um das Burndown-Diagramm anzuzeigen");
		m.put("Iteration start date should be before due date", "Startdatum der Iteration muss vor dem Fälligkeitsdatum liegen");
		m.put("Iterations", "Iterationen");
		m.put("Iterations Bean", "Iterationen Bean");
		m.put("JIRA Issue Priority", "JIRA-Issue-Priorität");
		m.put("JIRA Issue Status", "JIRA-Issue-Status");
		m.put("JIRA Issue Type", "JIRA-Issue-Typ");
		m.put("JIRA Project", "JIRA-Projekt");
		m.put("JIRA Projects to Import", "Zu importierende JIRA-Projekte");
		m.put("JUnit Report", "JUnit-Bericht");
		m.put("JVM", "JVM");
		m.put("JaCoCo Coverage Report", "JaCoCo-Abdeckungsbericht");
		m.put("Jest Coverage Report", "Jest-Abdeckungsbericht");
		m.put("Jest Test Report", "Jest-Testbericht");
		m.put("Job", "Job");
		m.put("Job \"{0}\" associated with the build not found.", "Job \"{0}\", der mit dem Build verknüpft ist, wurde nicht gefunden.");
		m.put("Job Authorization", "Job-Autorisierung");
		m.put("Job Cache Management", "Job-Cache-Verwaltung");
		m.put("Job Dependencies", "Job-Abhängigkeiten");
		m.put("Job Dependency", "Job-Abhängigkeit");
		m.put("Job Executor", "Job-Executor");
		m.put("Job Executor Bean", "Job-Executor Bean");
		m.put("Job Executors", "Job-Executors");
		m.put("Job Name", "Job-Name");
		m.put("Job Names", "Job-Namen");
		m.put("Job Param", "Job-Parameter");
		m.put("Job Parameters", "Job-Parameter");
		m.put("Job Privilege", "Job-Berechtigung");
		m.put("Job Privileges", "Job-Berechtigungen");
		m.put("Job Properties", "Job-Eigenschaften");
		m.put("Job Properties Bean", "Job-Eigenschaften Bean");
		m.put("Job Property", "Job-Eigenschaft");
		m.put("Job Secret", "Job-Geheimnis");
		m.put("Job Secret Edit Bean", "Job-Geheimnis Bearbeiten Bean");
		m.put("Job Secrets", "Job-Geheimnisse");
		m.put("Job Trigger", "Job-Trigger");
		m.put("Job Trigger Bean", "Job-Trigger Bean");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"Job-Administrationsberechtigung, einschließlich Löschen von Builds des Jobs. Dies impliziert alle anderen Job-Berechtigungen");
		m.put("Job cache \"{0}\" deleted", "Job-Cache \"{0}\" gelöscht");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"Job-Abhängigkeiten bestimmen die Reihenfolge und Parallelität beim Ausführen verschiedener Jobs. Sie können auch Artefakte von Upstream-Jobs abrufen");
		m.put("Job executor tested successfully", "Job-Executor erfolgreich getestet");
		m.put("Job executors", "Job-Executors");
		m.put("Job name", "Job-Name");
		m.put("Job properties saved", "Job-Eigenschaften gespeichert");
		m.put("Job secret \"{0}\" deleted", "Job-Geheimnis \"{0}\" gelöscht");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission", 
			"Job-Geheimnis 'access-token' sollte in den Projekteinstellungen als Zugriffstoken mit Paketberechtigung ${permission} definiert werden");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package read permission", 
			"Job-Geheimnis 'access-token' sollte in den Projekteinstellungen als Zugriffstoken mit Leseberechtigung für Pakete definiert werden");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package write permission", 
			"Job-Geheimnis 'access-token' sollte in den Projekteinstellungen als Zugriffstoken mit Schreibberechtigung für Pakete definiert werden");
		m.put("Job token", "Job-Token");
		m.put("Job will run on head commit of default branch", "Job wird auf dem Head-Commit des Standardzweigs ausgeführt");
		m.put("Job will run on head commit of target branch", "Job wird auf dem Head-Commit des Zielzweigs ausgeführt");
		m.put("Job will run on merge commit of target branch and source branch", "Job wird auf dem Merge-Commit des Zielzweigs und des Quellzweigs ausgeführt");
		m.put("Job will run on merge commit of target branch and source branch.<br><b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"Job wird auf dem Merge-Commit des Zielzweigs und des Quellzweigs ausgeführt.<br><b class='text-info'>HINWEIS:</b> Sofern nicht durch die Branch-Schutzregel erforderlich, ignoriert dieser Trigger Commits mit Nachrichten, die <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code> oder <code>[no job]</code> enthalten");
		m.put("Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"Job wird ausgeführt, wenn Code committet wird. <b class='text-info'>HINWEIS:</b> Dieser Trigger ignoriert Commits mit Nachrichten, die <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code> oder <code>[no job]</code> enthalten");
		m.put("Job workspace", "Job-Arbeitsbereich");
		m.put("Jobs", "Jobs");
		m.put("Jobs marked with <span class=\"text-danger\">*</span> are required to be successful", 
			"Jobs, die mit <span class=\"text-danger\">*</span> markiert sind, müssen erfolgreich sein");
		m.put("Jobs required to be successful on merge commit: ", "Jobs, die auf dem Merge-Commit erfolgreich sein müssen:");
		m.put("Jobs required to be successful: ", "Jobs, die erfolgreich sein müssen:");
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"Jobs mit derselben sequentiellen Gruppe und demselben Executor werden nacheinander ausgeführt. Beispielsweise können Sie diese Eigenschaft als <tt>@project_path@:prod</tt> für Jobs angeben, die vom selben Executor ausgeführt werden und in der Produktionsumgebung des aktuellen Projekts bereitgestellt werden, um Konflikte bei Bereitstellungen zu vermeiden");
		m.put("Key", "Schlüssel");
		m.put("Key Fingerprint", "Schlüssel-Fingerabdruck");
		m.put("Key ID", "Schlüssel-ID");
		m.put("Key Secret", "Schlüssel-Geheimnis");
		m.put("Key Type", "Schlüsseltyp");
		m.put("Kubectl Config File", "Kubectl-Konfigurationsdatei");
		m.put("Kubernetes", "Kubernetes");
		m.put("Kubernetes Executor", "Kubernetes-Executor");
		m.put("LDAP URL", "LDAP-URL");
		m.put("Label", "Label");
		m.put("Label Management", "Label-Verwaltung");
		m.put("Label Management Bean", "Label-Verwaltung Bean");
		m.put("Label Name", "Label-Name");
		m.put("Label Spec", "Label-Spezifikation");
		m.put("Label Value", "Label-Wert");
		m.put("Labels", "Labels");
		m.put("Labels Bean", "Labels Bean");
		m.put("Labels can be defined in Administration / Label Management", "Labels können unter Administration / Label-Verwaltung definiert werden");
		m.put("Labels have been updated", "Labels wurden aktualisiert");
		m.put("Language", "Sprache");
		m.put("Last Accessed", "Zuletzt zugegriffen");
		m.put("Last Finished of Specified Job", "Zuletzt abgeschlossen für den angegebenen Job");
		m.put("Last Modified", "Zuletzt geändert");
		m.put("Last Published", "Zuletzt veröffentlicht");
		m.put("Last Update", "Letztes Update");
		m.put("Last commit", "Letzter Commit");
		m.put("Last commit hash", "Letzter Commit-Hash");
		m.put("Last commit index version", "Letzte Commit-Indexversion");
		m.put("Leaf Projects", "Blattprojekte");
		m.put("Least branch coverage", "Geringste Branch-Abdeckung");
		m.put("Least line coverage", "Geringste Zeilenabdeckung");
		m.put("Leave a comment", "Einen Kommentar hinterlassen");
		m.put("Leave a note", "Eine Notiz hinterlassen");
		m.put("Left", "Links");
		m.put("Less", "Weniger");
		m.put("License Agreement", "Lizenzvereinbarung");
		m.put("License Setting", "Lizenzeinstellung");
		m.put("Licensed To", "Lizenziert für");
		m.put("Licensed To:", "Lizenziert für:");
		m.put("Line", "Zeile");
		m.put("Line changes", "Zeilenänderungen");
		m.put("Line: ", "Zeile:");
		m.put("Lines", "Zeilen");
		m.put("Link", "Link");
		m.put("Link Existing User", "Bestehenden Benutzer verknüpfen");
		m.put("Link Spec", "Link-Spezifikation");
		m.put("Link Spec Opposite", "Link-Spezifikation gegenüberliegend");
		m.put("Link Text", "Link-Text");
		m.put("Link URL", "Link-URL");
		m.put("Link URL should be specified", "Link-URL muss angegeben werden");
		m.put("Link User Bean", "Benutzer-Bean verknüpfen");
		m.put("Linkable Issues", "Verknüpfbare Probleme");
		m.put("Linkable Issues On the Other Side", "Verknüpfbare Probleme auf der anderen Seite");
		m.put("Links", "Links");
		m.put("Links can be used to associate different issues. For instance, an issue can be linked to sub issues or related issues", 
			"Links können verwendet werden, um verschiedene Probleme zu verknüpfen. Beispielsweise kann ein Problem mit Unterproblemen oder verwandten Problemen verknüpft werden");
		m.put("List", "Liste");
		m.put("Literal", "Literal");
		m.put("Literal default value", "Standardwert für Literal");
		m.put("Literal value", "Literalwert");
		m.put("Load Keys", "Schlüssel laden");
		m.put("Loading emojis...", "Emojis werden geladen...");
		m.put("Loading...", "Laden...");
		m.put("Log", "Protokoll");
		m.put("Log Work", "Arbeitsprotokoll");
		m.put("Log not available for offline agent", "Protokoll für Offline-Agent nicht verfügbar");
		m.put("Log work", "Arbeitsprotokoll");
		m.put("Login Name", "Anmeldename");
		m.put("Login and generate refresh token", "Anmelden und Aktualisierungstoken generieren");
		m.put("Login name already used by another account", "Anmeldename wird bereits von einem anderen Konto verwendet");
		m.put("Login name or email", "Anmeldename oder E-Mail");
		m.put("Login name or email address", "Anmeldename oder E-Mail-Adresse");
		m.put("Login to OneDev docker registry", "Anmelden beim OneDev-Docker-Registry");
		m.put("Login to comment", "Anmelden, um zu kommentieren");
		m.put("Login to comment on selection", "Anmelden, um die Auswahl zu kommentieren");
		m.put("Login to vote", "Anmelden, um abzustimmen");
		m.put("Login user needs to have package write permission over the project below", "Der angemeldete Benutzer benötigt Schreibberechtigung für Pakete im untenstehenden Projekt");
		m.put("Login with {0}", "Anmelden mit {0}");
		m.put("Logo for Dark Mode", "Logo für den Dunkelmodus");
		m.put("Logo for Light Mode", "Logo für den Hellmodus");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"Langfristiger Aktualisierungstoken des oben genannten Kontos, der verwendet wird, um Zugriffstoken für den Zugriff auf Gmail zu generieren. <b class='text-info'>TIPPS: </b> Sie können die Schaltfläche auf der rechten Seite dieses Feldes verwenden, um einen Aktualisierungstoken zu generieren. Beachten Sie, dass der Aktualisierungstoken neu generiert werden sollte, wenn die Client-ID, das Client-Geheimnis oder der Kontoname geändert wird");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"Langfristiger Aktualisierungstoken des oben genannten Kontos, der verwendet wird, um Zugriffstoken für den Zugriff auf den Office 365-Mailserver zu generieren. <b class='text-info'>TIPPS: </b> Sie können die Schaltfläche auf der rechten Seite dieses Feldes verwenden, um sich bei Ihrem Office 365-Konto anzumelden und einen Aktualisierungstoken zu generieren. Beachten Sie, dass der Aktualisierungstoken neu generiert werden sollte, wenn die Mandanten-ID, die Client-ID, das Client-Geheimnis oder der Benutzerprinzipalname geändert wird");
		m.put("Longest Duration First", "Längste Dauer zuerst");
		m.put("Looks like a GPG signature but without necessary data", "Sieht aus wie eine GPG-Signatur, aber ohne notwendige Daten");
		m.put("Low", "Niedrig");
		m.put("Low Severity", "Niedrige Schwere");
		m.put("MERGED", "VERMISCHT");
		m.put("MS Teams Notifications", "MS Teams-Benachrichtigungen");
		m.put("Mail", "Mail");
		m.put("Mail Connector", "Mail-Connector");
		m.put("Mail Connector Bean", "Mail-Connector-Bean");
		m.put("Mail Service", "Mail-Dienst");
		m.put("Mail Service Test", "Mail-Dienst-Test");
		m.put("Mail service not configured", "Mail-Dienst nicht konfiguriert");
		m.put("Mail service settings saved", "Mail-Dienst-Einstellungen gespeichert");
		m.put("Make sure <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 or higher</a> is installed", 
			"Stellen Sie sicher, dass <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 oder höher</a> installiert ist");
		m.put("Make sure current user has permission to run docker containers", "Stellen Sie sicher, dass der aktuelle Benutzer die Berechtigung hat, Docker-Container auszuführen");
		m.put("Make sure docker engine is installed and docker command line is available in system path", 
			"Stellen Sie sicher, dass die Docker-Engine installiert ist und die Docker-Befehlszeile im Systempfad verfügbar ist");
		m.put("Make sure git version 2.11.1 or higher is installed and available in system path", "Stellen Sie sicher, dass Git Version 2.11.1 oder höher installiert und im Systempfad verfügbar ist");
		m.put("Make sure git-lfs is installed and available in system path if you want to retrieve LFS files", 
			"Stellen Sie sicher, dass Git-LFS installiert und im Systempfad verfügbar ist, wenn Sie LFS-Dateien abrufen möchten");
		m.put("Make sure the access token has package read permission over the project", "Stellen Sie sicher, dass das Zugriffstoken Leseberechtigung für Pakete im Projekt hat");
		m.put("Make sure the access token has package write permission over the project", "Stellen Sie sicher, dass das Zugriffstoken Schreibberechtigung für Pakete im Projekt hat");
		m.put("Make sure the access token has package write permission over the project. Also make sure to run command <code>chmod 0600 $HOME/.gem/credentials</code> after creating the file", 
			"Stellen Sie sicher, dass das Zugriffstoken Schreibberechtigung für Pakete im Projekt hat. Stellen Sie außerdem sicher, dass Sie den Befehl <code>chmod 0600 $HOME/.gem/credentials</code> ausführen, nachdem Sie die Datei erstellt haben");
		m.put("Make sure the account has package ${permission} permission over the project", "Stellen Sie sicher, dass das Konto Paketberechtigung ${permission} im Projekt hat");
		m.put("Make sure the account has package read permission over the project", "Stellen Sie sicher, dass das Konto Leseberechtigung für Pakete im Projekt hat");
		m.put("Make sure the user has package write permission over the project", "Stellen Sie sicher, dass der Benutzer Schreibberechtigung für Pakete im Projekt hat");
		m.put("Malformed %sbase query", "Fehlerhafte %sbase-Abfrage");
		m.put("Malformed %squery", "Fehlerhafte %squery");
		m.put("Malformed build spec (import project: {0}, import revision: {1})", "Fehlerhafte Build-Spezifikation (Importprojekt: {0}, Importrevision: {1})");
		m.put("Malformed email address", "Fehlerhafte E-Mail-Adresse");
		m.put("Malformed filter", "Fehlerhafter Filter");
		m.put("Malformed name filter", "Fehlerhafter Namensfilter");
		m.put("Malformed query", "Fehlerhafte Abfrage");
		m.put("Malformed ssh signature", "Fehlerhafte SSH-Signatur");
		m.put("Malformed test suite filter", "Fehlerhafter Test-Suite-Filter");
		m.put("Manage Job", "Job verwalten");
		m.put("Manager DN", "Manager-DN");
		m.put("Manager Password", "Manager-Passwort");
		m.put("Manifest blob unknown", "Manifest-Blob unbekannt");
		m.put("Manifest invalid", "Manifest ungültig");
		m.put("Manifest unknown", "Manifest unbekannt");
		m.put("Many commands print outputs with ANSI colors in TTY mode to help identifying problems easily. However some commands running in this mode may wait for user input to cause build hanging. This can normally be fixed by adding extra options to the command", 
			"Viele Befehle drucken Ausgaben mit ANSI-Farben im TTY-Modus, um Probleme leicht zu identifizieren. Einige Befehle, die in diesem Modus ausgeführt werden, können jedoch auf Benutzereingaben warten und dadurch den Build blockieren. Dies kann normalerweise durch Hinzufügen zusätzlicher Optionen zum Befehl behoben werden");
		m.put("Mark a property archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived properties will not be shown by default", 
			"Markieren Sie eine Eigenschaft als archiviert, wenn sie von der aktuellen Build-Spezifikation nicht mehr verwendet wird, aber weiterhin existieren muss, um alte Builds zu reproduzieren. Archivierte Eigenschaften werden standardmäßig nicht angezeigt");
		m.put("Mark a secret archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived secrets will not be shown by default", 
			"Markieren Sie ein Geheimnis als archiviert, wenn es von der aktuellen Build-Spezifikation nicht mehr verwendet wird, aber weiterhin existieren muss, um alte Builds zu reproduzieren. Archivierte Geheimnisse werden standardmäßig nicht angezeigt");
		m.put("Markdown", "Markdown");
		m.put("Markdown Report", "Markdown-Bericht");
		m.put("Markdown from file", "Markdown aus Datei");
		m.put("Maven(s)", "Maven(s)");
		m.put("Max Code Search Entries", "Maximale Code-Sucheinträge");
		m.put("Max Commit Message Line Length", "Maximale Zeilenlänge der Commit-Nachricht");
		m.put("Max Git LFS File Size (MB)", "Maximale Git-LFS-Dateigröße (MB)");
		m.put("Max Retries", "Maximale Wiederholungen");
		m.put("Max Upload File Size (MB)", "Maximale Upload-Dateigröße (MB)");
		m.put("Max Value", "Maximalwert");
		m.put("Maximum number of entries to return when search code in repository", "Maximale Anzahl von Einträgen, die bei der Codesuche im Repository zurückgegeben werden");
		m.put("Maximum of retries before giving up", "Maximale Anzahl von Wiederholungen, bevor aufgegeben wird");
		m.put("May not be empty", "Darf nicht leer sein");
		m.put("Medium", "Mittel");
		m.put("Medium Severity", "Mittlere Schwere");
		m.put("Members", "Mitglieder");
		m.put("Memory", "Speicher");
		m.put("Memory Limit", "Speicherlimit");
		m.put("Memory Request", "Speicheranforderung");
		m.put("Mention Someone", "Jemanden erwähnen");
		m.put("Mention someone", "Jemanden erwähnen");
		m.put("Merge", "Zusammenführen");
		m.put("Merge Strategy", "Zusammenführungsstrategie");
		m.put("Merge Target Branch into Source Branch", "Zielbranch in Quellbranch zusammenführen");
		m.put("Merge branch \"{0}\" into branch \"{1}\"", "Branch \"{0}\" in Branch \"{1}\" zusammenführen");
		m.put("Merge branch \"{0}\" of project \"{1}\" into branch \"{2}\"", "Branch \"{0}\" des Projekts \"{1}\" in Branch \"{2}\" zusammenführen");
		m.put("Merge preview not calculated yet", "Zusammenführungsvorschau noch nicht berechnet");
		m.put("Merged", "Zusammengeführt");
		m.put("Merged pull request \"{0}\" ({1})", "Zusammengeführter Pull-Request \"{0}\" ({1})");
		m.put("Merges pull request", "Pull-Request zusammenführen");
		m.put("Meta", "Meta");
		m.put("Meta Info", "Meta-Info");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "Minimalwert");
		m.put("Minimum length of the password", "Minimale Länge des Passworts");
		m.put("Missing Commit", "Fehlender Commit");
		m.put("Missing Commits", "Fehlende Commits");
		m.put("Month", "Monat");
		m.put("Months", "Monate");
		m.put("Months to Display", "Anzuzeigende Monate");
		m.put("More", "Mehr");
		m.put("More Options", "Weitere Optionen");
		m.put("More Settings", "Weitere Einstellungen");
		m.put("More commits", "Weitere Commits");
		m.put("More info", "Weitere Informationen");
		m.put("More operations", "Weitere Operationen");
		m.put("Most branch coverage", "Meiste Branch-Abdeckung");
		m.put("Most line coverage", "Meiste Zeilenabdeckung");
		m.put("Most probably there are import errors in the <a wicket:id=\"buildSpec\">build spec</a>", 
			"Wahrscheinlich gibt es Importfehler in der <a wicket:id=\"buildSpec\">Build-Spezifikation</a>");
		m.put("Mount Docker Sock", "Docker-Sock mounten");
		m.put("Move All Queried Issues To...", "Alle abgefragten Issues verschieben nach...");
		m.put("Move All Queried Projects To...", "Alle abgefragten Projekte verschieben nach...");
		m.put("Move Selected Issues To...", "Ausgewählte Issues verschieben nach...");
		m.put("Move Selected Projects To...", "Ausgewählte Projekte verschieben nach...");
		m.put("Multiple Lines", "Mehrere Zeilen");
		m.put("Multiple On the Other Side", "Mehrere auf der anderen Seite");
		m.put("Must not be empty", "Darf nicht leer sein");
		m.put("My Access Tokens", "Meine Zugriffstoken");
		m.put("My Basic Settings", "Meine Grundeinstellungen");
		m.put("My Email Addresses", "Meine E-Mail-Adressen");
		m.put("My GPG Keys", "Meine GPG-Schlüssel");
		m.put("My Profile", "Mein Profil");
		m.put("My SSH Keys", "Meine SSH-Schlüssel");
		m.put("My SSO Accounts", "Meine SSO-Konten");
		m.put("Mypy Report", "Mypy-Bericht");
		m.put("N/A", "N/A");
		m.put("NPM(s)", "NPM(s)");
		m.put("Name", "Name");
		m.put("Name Of Empty Value", "Name des leeren Werts");
		m.put("Name On the Other Side", "Name auf der anderen Seite");
		m.put("Name Prefix", "Name-Präfix");
		m.put("Name already used by another access token of the owner", "Name wird bereits von einem anderen Zugriffstoken des Besitzers verwendet");
		m.put("Name already used by another link", "Name wird bereits von einem anderen Link verwendet");
		m.put("Name and name on the other side should be different", "Name und Name auf der anderen Seite sollten unterschiedlich sein");
		m.put("Name containing spaces or starting with dash needs to be quoted", "Name mit Leerzeichen oder beginnend mit einem Bindestrich muss in Anführungszeichen gesetzt werden");
		m.put("Name invalid", "Name ungültig");
		m.put("Name of the link", "Name des Links");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"Name des Links auf der anderen Seite. Zum Beispiel, wenn der Name <tt>Unter-Tasks</tt> ist, kann der Name auf der anderen Seite <tt>Eltern-Task</tt> sein");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"Name des Anbieters hat zwei Zwecke: <ul><li>Anzeigen auf der Login-Schaltfläche<li>Bildung der Autorisierungs-Callback-URL, die <i>&lt;Server-URL&gt;/~sso/callback/&lt;Name&gt;</i> sein wird</ul>");
		m.put("Name reversely", "Name umgekehrt");
		m.put("Name unknown", "Name unbekannt");
		m.put("Name your file", "Datei benennen");
		m.put("Named Agent Queries Bean", "Benannte Agentenabfragen-Bean");
		m.put("Named Agent Query", "Benannte Agentenabfrage");
		m.put("Named Build Queries Bean", "Benannte Build-Abfragen-Bean");
		m.put("Named Build Query", "Benannte Build-Abfrage");
		m.put("Named Code Comment Queries Bean", "Benannte Code-Kommentar-Abfragen-Bean");
		m.put("Named Code Comment Query", "Benannte Code-Kommentar-Abfrage");
		m.put("Named Commit Queries Bean", "Benannte Commit-Abfragen-Bean");
		m.put("Named Commit Query", "Benannte Commit-Abfrage");
		m.put("Named Element", "Benanntes Element");
		m.put("Named Issue Queries Bean", "Benannte Issue-Abfragen-Bean");
		m.put("Named Issue Query", "Benannte Issue-Abfrage");
		m.put("Named Pack Queries Bean", "Benannte Pack-Abfragen-Bean");
		m.put("Named Pack Query", "Benannte Pack-Abfrage");
		m.put("Named Project Queries Bean", "Benannte Projekt-Abfragen-Bean");
		m.put("Named Project Query", "Benannte Projekt-Abfrage");
		m.put("Named Pull Request Queries Bean", "Benannte Pull-Request-Abfragen-Bean");
		m.put("Named Pull Request Query", "Benannte Pull-Request-Abfrage");
		m.put("Named Query", "Benannte Abfrage");
		m.put("Network Options", "Netzwerkoptionen");
		m.put("Never", "Niemals");
		m.put("Never expire", "Niemals ablaufen");
		m.put("New Board", "Neues Board");
		m.put("New Invitation Bean", "Neue Einladung-Bean");
		m.put("New Issue", "Neues Issue");
		m.put("New Password", "Neues Passwort");
		m.put("New State", "Neuer Status");
		m.put("New User Bean", "Neue Benutzer-Bean");
		m.put("New Value", "Neuer Wert");
		m.put("New issue board created", "Neues Issue-Board erstellt");
		m.put("New project created", "Neues Projekt erstellt");
		m.put("New user created", "Neuer Benutzer erstellt");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"Neue Version verfügbar. Rot für Sicherheits-/kritisches Update, Gelb für Fehlerbehebung, Blau für Funktionsupdate. Klicken, um Änderungen anzuzeigen. Deaktivieren in den Systemeinstellungen");
		m.put("Next", "Weiter");
		m.put("Next commit", "Nächster Commit");
		m.put("Next {0}", "Nächster {0}");
		m.put("No", "Nein");
		m.put("No Activity Days", "Keine Aktivitätstage");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"Keine SSH-Schlüssel in Ihrem Konto konfiguriert. Sie können <a wicket:id=\"sshKeys\" class=\"link-primary\">einen Schlüssel hinzufügen</a> oder zu <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a>-URL wechseln");
		m.put("No SSL", "Kein SSL");
		m.put("No accessible reports", "Keine zugänglichen Berichte");
		m.put("No activity for some time", "Keine Aktivität seit einiger Zeit");
		m.put("No agents to pause", "Keine Agenten zum Pausieren");
		m.put("No agents to remove", "Keine Agenten zum Entfernen");
		m.put("No agents to restart", "Keine Agenten zum Neustarten");
		m.put("No agents to resume", "Keine Agenten zum Fortsetzen");
		m.put("No aggregation", "Keine Aggregation");
		m.put("No any", "Kein Einziger");
		m.put("No any matches", "Keine Übereinstimmungen");
		m.put("No applicable transitions or no permission to transit", "Keine anwendbaren Übergänge oder keine Berechtigung zum Übergang");
		m.put("No attributes defined (can only be edited when agent is online)", "Keine Attribute definiert (können nur bearbeitet werden, wenn der Agent online ist)");
		m.put("No audits", "Keine Audits");
		m.put("No authorized job secret found (project: {0}, job secret: {1})", "Kein autorisiertes Job-Geheimnis gefunden (Projekt: {0}, Job-Geheimnis: {1})");
		m.put("No branch to cherry-pick to", "Kein Branch zum Cherry-Picking");
		m.put("No branch to revert on", "Kein Branch zum Zurücksetzen");
		m.put("No branches Found", "Keine Branches gefunden");
		m.put("No branches found", "Keine Branches gefunden");
		m.put("No build in query context", "Kein Build im Abfragekontext");
		m.put("No builds", "Keine Builds");
		m.put("No builds to cancel", "Keine Builds zum Abbrechen");
		m.put("No builds to delete", "Keine Builds zum Löschen");
		m.put("No builds to re-run", "Keine Builds zum Wiederholen");
		m.put("No comment", "Kein Kommentar");
		m.put("No comments to delete", "Keine Kommentare zum Löschen");
		m.put("No comments to set as read", "Keine Kommentare zum Markieren als gelesen");
		m.put("No comments to set resolved", "Keine Kommentare zum Markieren als gelöst");
		m.put("No comments to set unresolved", "Keine Kommentare zum Markieren als ungelöst");
		m.put("No commit in query context", "Kein Commit im Abfragekontext");
		m.put("No config file", "Keine Konfigurationsdatei");
		m.put("No current build in query context", "Kein aktueller Build im Abfragekontext");
		m.put("No current commit in query context", "Kein aktueller Commit im Abfragekontext");
		m.put("No current pull request in query context", "Kein aktueller Pull-Request im Abfragekontext");
		m.put("No data", "Keine Daten");
		m.put("No default branch", "Kein Standard-Branch");
		m.put("No default group", "Keine Standardgruppe");
		m.put("No default roles", "Keine Standardrollen");
		m.put("No default value", "Kein Standardwert");
		m.put("No description", "Keine Beschreibung");
		m.put("No diffs", "Keine Unterschiede");
		m.put("No diffs to navigate", "Keine Unterschiede zum Navigieren");
		m.put("No directories to skip", "Keine Verzeichnisse zum Überspringen");
		m.put("No disallowed file types", "Keine nicht erlaubten Dateitypen");
		m.put("No executors defined. Jobs will use auto-discovered executors instead", "Keine Executor definiert. Jobs verwenden stattdessen automatisch erkannte Executor");
		m.put("No external password authenticator", "Kein externer Passwort-Authenticator");
		m.put("No external password authenticator to authenticate user \"{0}\"", "Kein externer Passwort-Authenticator zur Authentifizierung des Benutzers \"{0}\"");
		m.put("No fields to prompt", "Keine Felder zum Abfragen");
		m.put("No fields to remove", "Keine Felder zum Entfernen");
		m.put("No file attachments", "Keine Dateianhänge");
		m.put("No group by", "Keine Gruppierung");
		m.put("No groups claim returned", "Keine Gruppenansprüche zurückgegeben");
		m.put("No groups to remove from", "Keine Gruppen zum Entfernen");
		m.put("No ignore file", "Keine Ignorierdatei");
		m.put("No ignored licenses", "Keine ignorierten Lizenzen");
		m.put("No image attachments", "Keine Bildanhänge");
		m.put("No imports defined", "Keine Importe definiert");
		m.put("No issue boards defined", "Keine Issue-Boards definiert");
		m.put("No issues in iteration", "Keine Issues in der Iteration");
		m.put("No issues to copy", "Keine Issues zum Kopieren");
		m.put("No issues to delete", "Keine Issues zum Löschen");
		m.put("No issues to edit", "Keine Issues zum Bearbeiten");
		m.put("No issues to export", "Keine Issues zum Exportieren");
		m.put("No issues to move", "Keine Issues zum Verschieben");
		m.put("No issues to set as read", "Keine Issues zum Markieren als gelesen");
		m.put("No issues to sync estimated/spent time", "Keine Issues zum Synchronisieren von geschätzter/verbrachter Zeit");
		m.put("No issues to watch/unwatch", "Keine Issues zum Beobachten/Nicht-Beobachten");
		m.put("No jobs defined", "Keine Jobs definiert");
		m.put("No jobs found", "Keine Jobs gefunden");
		m.put("No limit", "Kein Limit");
		m.put("No mail service", "Kein Mail-Service");
		m.put("No obvious changes", "Keine offensichtlichen Änderungen");
		m.put("No one", "Niemand");
		m.put("No packages to delete", "Keine Pakete zum Löschen");
		m.put("No parent", "Kein Elternteil");
		m.put("No previous successful build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a> to calculate fixed issues since", 
			"Kein vorheriger erfolgreicher Build auf <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">dem gleichen Stream</a>, um behobene Issues seitdem zu berechnen");
		m.put("No projects found", "Keine Projekte gefunden");
		m.put("No projects to delete", "Keine Projekte zum Löschen");
		m.put("No projects to modify", "Keine Projekte zum Modifizieren");
		m.put("No projects to move", "Keine Projekte zum Verschieben");
		m.put("No properties defined", "Keine Eigenschaften definiert");
		m.put("No proxy", "Kein Proxy");
		m.put("No pull request in query context", "Kein Pull-Request im Abfragekontext");
		m.put("No pull requests to delete", "Keine Pull-Requests zum Löschen");
		m.put("No pull requests to discard", "Keine Pull-Requests zum Verwerfen");
		m.put("No pull requests to set as read", "Keine Pull-Requests zum Markieren als gelesen");
		m.put("No pull requests to watch/unwatch", "Keine Pull-Requests zum Beobachten/Nicht-Beobachten");
		m.put("No refs to build on behalf of", "Keine Refs zum Build im Auftrag von");
		m.put("No required services", "Keine erforderlichen Dienste");
		m.put("No response body", "Kein Antwortinhalt");
		m.put("No secret config", "Keine Geheimkonfiguration");
		m.put("No services defined", "Keine Dienste definiert");
		m.put("No start/due date", "Kein Start-/Fälligkeitsdatum");
		m.put("No step templates defined", "Keine Schrittvorlagen definiert");
		m.put("No suggestions", "Keine Vorschläge");
		m.put("No tags found", "Keine Tags gefunden");
		m.put("No timesheets defined", "Keine Zeiterfassungen definiert");
		m.put("No user found with login name or email: ", "Kein Benutzer mit Anmeldenamen oder E-Mail gefunden:");
		m.put("No users to convert to service accounts", "Keine Benutzer zum Umwandeln in Dienstkonten");
		m.put("No users to delete", "Keine Benutzer zum Löschen");
		m.put("No users to disable", "Keine Benutzer zum Deaktivieren");
		m.put("No users to enable", "Keine Benutzer zum Aktivieren");
		m.put("No users to remove from group", "Keine Benutzer zum Entfernen aus der Gruppe");
		m.put("No valid query to show progress", "Keine gültige Abfrage, um Fortschritt anzuzeigen");
		m.put("No valid signature for head commit", "Keine gültige Signatur für Head-Commit");
		m.put("No valid signature for head commit of target branch", "Keine gültige Signatur für Head-Commit des Zielbranches");
		m.put("No value", "Kein Wert");
		m.put("No verified primary email address", "Keine verifizierte primäre E-Mail-Adresse");
		m.put("Node Selector", "Node-Selektor");
		m.put("Node Selector Entry", "Node-Selektor-Eintrag");
		m.put("None", "Keine");
		m.put("Not Active Since", "Nicht aktiv seit");
		m.put("Not Used Since", "Nicht verwendet seit");
		m.put("Not a verified email of signing GPG key", "Keine verifizierte E-Mail des signierenden GPG-Schlüssels");
		m.put("Not a verified email of signing ssh key owner", "Nicht eine verifizierte E-Mail des Besitzers des signierenden SSH-Schlüssels");
		m.put("Not allowed file type: {0}", "Nicht erlaubter Dateityp: {0}");
		m.put("Not assigned", "Nicht zugewiesen");
		m.put("Not authorized to create project under \"{0}\"", "Nicht berechtigt, ein Projekt unter \"{0}\" zu erstellen");
		m.put("Not authorized to create root project", "Nicht berechtigt, ein Root-Projekt zu erstellen");
		m.put("Not authorized to move project under this parent", "Keine Berechtigung, Projekt unter diesem Elternteil zu verschieben");
		m.put("Not authorized to set as root project", "Keine Berechtigung, als Root-Projekt festzulegen");
		m.put("Not covered", "Nicht abgedeckt");
		m.put("Not covered by any test", "Nicht durch einen Test abgedeckt");
		m.put("Not displaying any fields", "Keine Felder angezeigt");
		m.put("Not displaying any links", "Keine Links angezeigt");
		m.put("Not passed", "Nicht bestanden");
		m.put("Not rendered in failsafe mode", "Nicht im Failsafe-Modus gerendert");
		m.put("Not run", "Nicht ausgeführt");
		m.put("Not specified", "Nicht angegeben");
		m.put("Note", "Notiz");
		m.put("Nothing to preview", "Nichts zu Vorschau anzeigen");
		m.put("Notification", "Benachrichtigung");
		m.put("Notifications", "Benachrichtigungen");
		m.put("Notify Build Events", "Build-Ereignisse benachrichtigen");
		m.put("Notify Code Comment Events", "Code-Kommentar-Ereignisse benachrichtigen");
		m.put("Notify Code Push Events", "Code-Push-Ereignisse benachrichtigen");
		m.put("Notify Issue Events", "Issue-Ereignisse benachrichtigen");
		m.put("Notify Own Events", "Eigene Ereignisse benachrichtigen");
		m.put("Notify Pull Request Events", "Pull-Request-Ereignisse benachrichtigen");
		m.put("Notify Users", "Benutzer benachrichtigen");
		m.put("Ntfy.sh Notifications", "Ntfy.sh-Benachrichtigungen");
		m.put("NuGet(s)", "NuGet(s)");
		m.put("NuSpec", "NuSpec");
		m.put("Number of CPU Cores", "Anzahl der CPU-Kerne");
		m.put("Number of SSH Keys", "Anzahl der SSH-Schlüssel");
		m.put("Number of builds to preserve", "Anzahl der Builds, die erhalten bleiben sollen");
		m.put("Number of project replicas, including primary and backups", "Anzahl der Projekt-Replikate, einschließlich primärer und Backup-Kopien");
		m.put("Number of recent months to show statistics for", "Anzahl der letzten Monate, für die Statistiken angezeigt werden sollen");
		m.put("OAuth2 Client information | CLIENT ID", "OAuth2-Client-Informationen | CLIENT-ID");
		m.put("OAuth2 Client information | CLIENT SECRET", "OAuth2-Client-Informationen | CLIENT-SECRET");
		m.put("OCI Layout Directory", "OCI-Layout-Verzeichnis");
		m.put("OIDC error: Inconsistent sub in ID token and userinfo", "OIDC-Fehler: Inkonsistenter Sub in ID-Token und Benutzerinfo");
		m.put("OOPS! There Is An Error", "OOPS! Es gibt einen Fehler");
		m.put("OPEN", "OFFEN");
		m.put("OS", "Betriebssystem");
		m.put("OS Arch", "Betriebssystem-Architektur");
		m.put("OS User Name", "Betriebssystem-Benutzername");
		m.put("OS Version", "Betriebssystem-Version");
		m.put("OS/ARCH", "OS/ARCH");
		m.put("Offline", "Offline");
		m.put("Ok", "Ok");
		m.put("Old Name", "Alter Name");
		m.put("Old Password", "Altes Passwort");
		m.put("On Behalf Of", "Im Namen von");
		m.put("On Branches", "Auf Branches");
		m.put("OneDev Issue Field", "OneDev-Issue-Feld");
		m.put("OneDev Issue Link", "OneDev-Issue-Link");
		m.put("OneDev Issue State", "OneDev-Issue-Status");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"OneDev analysiert Repository-Dateien für Codesuche, Zeilenstatistiken und Code-Beitragsstatistiken. Diese Einstellung gibt an, welche Dateien analysiert werden sollen, und erwartet durch Leerzeichen getrennte <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Muster</a>. Ein Muster kann durch Voranstellen von '-' ausgeschlossen werden, beispielsweise <code>-**/vendors/**</code> schließt alle Dateien mit \"vendors\" im Pfad aus. <b>HINWEIS: </b> Änderungen dieser Einstellung betreffen nur neue Commits. Um die Änderung auf historische Commits anzuwenden, stoppen Sie bitte den Server und löschen Sie den Ordner <code>index</code> und <code>info/commit</code> im <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>Projekt-Speicherverzeichnis</a>. Das Repository wird neu analysiert, wenn der Server gestartet wird.");
		m.put("OneDev configures git hooks to communicate with itself via curl", "OneDev konfiguriert Git-Hooks, um über Curl mit sich selbst zu kommunizieren");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"OneDev muss Benutzer-DN suchen und bestimmen sowie Benutzergruppeninformationen abrufen, wenn die Gruppenabfrage aktiviert ist. Aktivieren Sie diese Option und geben Sie 'Manager'-DN und Passwort an, wenn diese Vorgänge authentifiziert werden müssen");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"OneDev benötigt die Git-Befehlszeile, um Repositories zu verwalten. Die minimal erforderliche Version ist 2.11.1. Stellen Sie außerdem sicher, dass Git-LFS installiert ist, wenn Sie LFS-Dateien im Build-Job abrufen möchten");
		m.put("Online", "Online");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", 
			"Erstelle nur einen Merge-Commit, wenn der Zielbranch nicht auf den Quellbranch vorwärtsgeführt werden kann");
		m.put("Only projects manageable by access token owner can be authorized", "Nur Projekte, die vom Besitzer des Zugriffstokens verwaltet werden können, dürfen autorisiert werden");
		m.put("Only system level audit events are displayed here. To view audit events for a specific project, please visit the project audit log page", 
			"Hier werden nur Audit-Ereignisse auf Systemebene angezeigt. Um Audit-Ereignisse für ein bestimmtes Projekt anzuzeigen, besuchen Sie bitte die Audit-Log-Seite des Projekts");
		m.put("Only users able to authenticate via password can be linked", "Nur Benutzer, die sich über ein Passwort authentifizieren können, können verknüpft werden");
		m.put("Open", "Öffnen");
		m.put("Open new pull request", "Neue Pull-Request öffnen");
		m.put("Open terminal of current running step", "Terminal des aktuell laufenden Schritts öffnen");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"OpenID-Client-Identifikation wird von Ihrem OpenID-Anbieter zugewiesen, wenn Sie diese OneDev-Instanz als Client-Anwendung registrieren");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"OpenID-Client-Secret wird von Ihrem OpenID-Anbieter generiert, wenn Sie diese OneDev-Instanz als Client-Anwendung registrieren");
		m.put("OpenSSH Public Key", "OpenSSH-Öffentlicher Schlüssel");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"OpenSSH-Öffentlicher Schlüssel beginnt mit 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com' oder 'sk-ssh-ed25519@openssh.com'");
		m.put("Opened issue \"{0}\" ({1})", "Geöffnetes Issue \"{0}\" ({1})");
		m.put("Opened pull request \"{0}\" ({1})", "Geöffneter Pull-Request \"{0}\" ({1})");
		m.put("Operation", "Operation");
		m.put("Operation Failed", "Operation fehlgeschlagen");
		m.put("Operation Successful", "Operation erfolgreich");
		m.put("Operations", "Operationen");
		m.put("Optional", "Optional");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"Optional Projekt angeben, in dem das Issue erstellt werden soll. Leer lassen, um im aktuellen Projekt zu erstellen");
		m.put("Optionally add new users to specified default group", "Optional neue Benutzer zur angegebenen Standardgruppe hinzufügen");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"Optional neu authentifizierten Benutzer zur angegebenen Gruppe hinzufügen, wenn keine Mitgliedschaftsinformationen verfügbar sind");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"Optional neu authentifizierten Benutzer zur angegebenen Gruppe hinzufügen, wenn keine Mitgliedschaftsinformationen abgerufen werden");
		m.put("Optionally choose required builds. You may also input jobs not listed here, and press ENTER to add them", 
			"Optional erforderliche Builds auswählen. Sie können auch Jobs eingeben, die hier nicht aufgeführt sind, und die Eingabetaste drücken, um sie hinzuzufügen");
		m.put("Optionally configure proxy to access remote repository. Proxy should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;", 
			"Optional Proxy konfigurieren, um auf das Remote-Repository zuzugreifen. Proxy sollte im Format &lt;Proxy-Host&gt;:&lt;Proxy-Port&gt; sein");
		m.put("Optionally define a unique key for the project with two or more upper case letters. This key can be used to reference issues, builds, and pull requests with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of <code>&lt;project path&gt;#&lt;number&gt;</code>", 
			"Optional einen eindeutigen Schlüssel für das Projekt mit zwei oder mehr Großbuchstaben definieren. Dieser Schlüssel kann verwendet werden, um Issues, Builds und Pull-Requests mit einer stabilen und kurzen Form <code>&lt;Projekt-Schlüssel&gt;-&lt;Nummer&gt;</code> anstelle von <code>&lt;Projekt-Pfad&gt;#&lt;Nummer&gt;</code> zu referenzieren");
		m.put("Optionally define parameter specifications of the job", "Optional Parameter-Spezifikationen des Jobs definieren");
		m.put("Optionally define parameter specifications of the step template", "Optional Parameter-Spezifikationen der Schrittvorlage definieren");
		m.put("Optionally describe the group", "Optional die Gruppe beschreiben");
		m.put("Optionally describes the custom field. Html tags are accepted", "Optional das benutzerdefinierte Feld beschreiben. Html-Tags werden akzeptiert");
		m.put("Optionally describes the param. Html tags are accepted.", "Optional den Parameter beschreiben. Html-Tags werden akzeptiert");
		m.put("Optionally filter builds", "Optional Builds filtern");
		m.put("Optionally filter issues", "Optional Issues filtern");
		m.put("Optionally filter pull requests", "Optional Pull-Requests filtern");
		m.put("Optionally leave a note", "Optional eine Notiz hinterlassen");
		m.put("Optionally mount directories or files under job workspace into container", "Optional Verzeichnisse oder Dateien unter dem Job-Arbeitsbereich in den Container einbinden");
		m.put("Optionally select fields to prompt when this button is pressed", "Optional Felder auswählen, die angezeigt werden sollen, wenn diese Schaltfläche gedrückt wird");
		m.put("Optionally select fields to remove when this transition happens", "Optional Felder auswählen, die entfernt werden sollen, wenn dieser Übergang stattfindet");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"Optional den Namen des Attributs innerhalb des Benutzer-LDAP-Eintrags angeben, dessen Wert als Benutzer-E-Mail verwendet wird. Dieses Feld wird normalerweise gemäß RFC 2798 auf <i>mail</i> gesetzt");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"Optional den Namen des Attributs innerhalb des Benutzer-LDAP-Eintrags angeben, dessen Wert als vollständiger Benutzername verwendet wird. Dieses Feld wird normalerweise gemäß RFC 2798 auf <i>displayName</i> gesetzt. Wenn leer gelassen, wird der vollständige Name des Benutzers nicht abgerufen");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"Optional geben Sie <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, das als GitHub-Zugriffstoken verwendet werden soll. Dies wird verwendet, um Release-Notizen von Abhängigkeiten abzurufen, die auf GitHub gehostet werden, und der authentifizierte Zugriff erhält ein höheres Ratenlimit");
		m.put("Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko", 
			"Optional geben Sie <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>zusätzliche Optionen</a> von Kaniko an");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane", 
			"Optional geben Sie <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>zusätzliche Optionen</a> von Crane an");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane", 
			"Optional geben Sie <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>zusätzliche Optionen</a> von Crane an");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to build for platform of the node running the job", 
			"Optional geben Sie <span class='text-info'>kommagetrennte</span> Plattformen zum Erstellen an, beispielsweise <tt>linux/amd64,linux/arm64</tt>. Lassen Sie das Feld leer, um die Plattform des Knotens zu verwenden, der den Job ausführt");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to scan, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to scan all platforms in OCI layout", 
			"Optional geben Sie <span class='text-info'>kommagetrennte</span> Plattformen zum Scannen an, beispielsweise <tt>linux/amd64,linux/arm64</tt>. Lassen Sie das Feld leer, um alle Plattformen im OCI-Layout zu scannen");
		m.put("Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use file <tt>Dockerfile</tt> under build path specified above", 
			"Optional geben Sie die Dockerfile relativ zu <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> an. Lassen Sie das Feld leer, um die Datei <tt>Dockerfile</tt> unter dem oben angegebenen Build-Pfad zu verwenden");
		m.put("Optionally specify JavaScript config to be used by Renovate CLI", "Optional geben Sie die JavaScript-Konfiguration an, die von der Renovate-CLI verwendet werden soll");
		m.put("Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. Leave empty to derive from server url", 
			"Optional geben Sie die SSH-Root-URL an, die verwendet wird, um die Projekt-Klon-URL über das SSH-Protokoll zu erstellen. Lassen Sie das Feld leer, um es von der Server-URL abzuleiten");
		m.put("Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of the text input", 
			"Optional geben Sie ein <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>Regulärer Ausdrucksmuster</a> für gültige Werte der Texteingabe an");
		m.put("Optionally specify a OneDev project to be used as parent of imported projects. Leave empty to import as root projects", 
			"Optional geben Sie ein OneDev-Projekt an, das als übergeordnetes Projekt der importierten Projekte verwendet werden soll. Lassen Sie das Feld leer, um als Root-Projekte zu importieren");
		m.put("Optionally specify a OneDev project to be used as parent of imported repositories. Leave empty to import as root projects", 
			"Optional geben Sie ein OneDev-Projekt an, das als übergeordnetes Projekt der importierten Repositories verwendet werden soll. Lassen Sie das Feld leer, um als Root-Projekte zu importieren");
		m.put("Optionally specify a base query for the list", "Optional geben Sie eine Basisabfrage für die Liste an");
		m.put("Optionally specify a base query to filter/order issues in backlog. Backlog issues are those not associating with current iteration", 
			"Optional geben Sie eine Basisabfrage an, um Probleme im Backlog zu filtern/zu sortieren. Backlog-Probleme sind diejenigen, die nicht mit der aktuellen Iteration verbunden sind");
		m.put("Optionally specify a base query to filter/order issues of the board", "Optional geben Sie eine Basisabfrage an, um Probleme des Boards zu filtern/zu sortieren");
		m.put("Optionally specify a cron expression to schedule database auto-backup. The cron expression format is <em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>.For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz tutorial</a>.The backup files will be placed into <em>db-backup</em> folder under OneDev installation directory. In case multiple servers connect to form a cluster, auto-backup takes place on the <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>lead server</a>. Leave this property empty if you do not want to enable database auto backup.", 
			"Optional geben Sie einen Cron-Ausdruck an, um die automatische Datenbanksicherung zu planen. Das Cron-Ausdrucksformat ist <em>&lt;Sekunden&gt; &lt;Minuten&gt; &lt;Stunden&gt; &lt;Tag-des-Monats&gt; &lt;Monat&gt; &lt;Tag-der-Woche&gt;</em>. Zum Beispiel bedeutet <em>0 0 1 * * ?</em> 1:00 Uhr morgens jeden Tag. Einzelheiten zum Format finden Sie im <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz-Tutorial</a>. Die Sicherungsdateien werden im Ordner <em>db-backup</em> unter dem OneDev-Installationsverzeichnis abgelegt. Wenn mehrere Server verbunden sind, um einen Cluster zu bilden, erfolgt die automatische Sicherung auf dem <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>Leitserver</a>. Lassen Sie diese Eigenschaft leer, wenn Sie die automatische Datenbanksicherung nicht aktivieren möchten.");
		m.put("Optionally specify a date field to hold due date information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Optional geben Sie ein Datumsfeld an, um Informationen zum Fälligkeitsdatum zu speichern.<br><b>HINWEIS: </b> Sie können die OneDev-Probleme-Felder anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to put retrieved artifacts. Leave empty to use job workspace itself", 
			"Optional geben Sie einen Pfad relativ zu <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, um abgerufene Artefakte abzulegen. Lassen Sie das Feld leer, um den Job-Arbeitsbereich selbst zu verwenden");
		m.put("Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. <b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, as the volume is only used to hold temporary build files", 
			"Optional geben Sie eine Speicherklasse an, um das Build-Volume dynamisch zuzuweisen. Lassen Sie das Feld leer, um die Standard-Speicherklasse zu verwenden. <b class='text-warning'>HINWEIS:</b> Die Rückgewinnungsrichtlinie der Speicherklasse sollte auf <code>Delete</code> gesetzt werden, da das Volume nur zum Speichern temporärer Build-Dateien verwendet wird");
		m.put("Optionally specify a working period field to hold estimated time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Optional geben Sie ein Arbeitszeitfeld an, um geschätzte Zeitinformationen zu speichern.<br><b>HINWEIS: </b> Sie können die OneDev-Probleme-Felder anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Optionally specify a working period field to hold spent time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Optional geben Sie ein Arbeitszeitfeld an, um aufgewendete Zeitinformationen zu speichern.<br><b>HINWEIS: </b> Sie können die OneDev-Probleme-Felder anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Optionally specify a working period field to hold time estimate infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Optional geben Sie ein Arbeitszeitfeld an, um Zeitabschätzungsinformationen zu speichern.<br><b>HINWEIS: </b> Sie können die OneDev-Probleme-Felder anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Optionally specify a working period field to hold time spent infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Optional geben Sie ein Arbeitszeitfeld an, um aufgewendete Zeitinformationen zu speichern.<br><b>HINWEIS: </b> Sie können die OneDev-Probleme-Felder anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Optionally specify additional options for buildx build command", "Optional geben Sie zusätzliche Optionen für den Buildx-Build-Befehl an");
		m.put("Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, the response header <code>Access-Control-Allow-Origin</code> will be set to the same value", 
			"Optional geben Sie erlaubte <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a>-Ursprünge an. Für eine einfache oder vorab genehmigte CORS-Anfrage wird der Wert des Anfrage-Headers <code>Origin</code> hier eingeschlossen, und der Antwort-Header <code>Access-Control-Allow-Origin</code> wird auf denselben Wert gesetzt");
		m.put("Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match", 
			"Optional geben Sie erlaubte E-Mail-Domains für Benutzer mit Selbstanmeldung an. Verwenden Sie '*' oder '?' für Musterabgleich");
		m.put("Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). Leave empty to all types", 
			"Optional geben Sie anwendbare Commit-Typen für die Überprüfung der Commit-Nachrichten-Fußzeile an (drücken Sie ENTER, um einen Wert hinzuzufügen). Lassen Sie das Feld leer für alle Typen");
		m.put("Optionally specify applicable jobs of this executor", "Optional anwendbare Jobs für diesen Executor angeben");
		m.put("Optionally specify applicable users who pushed the change", "Geben Sie optional an, welche Benutzer die Änderung gepusht haben");
		m.put("Optionally specify arguments to run above image", "Optional geben Sie Argumente an, um das obige Image auszuführen");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"Optional geben Sie Artefakte an, die aus der Abhängigkeit in den <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> abgerufen werden sollen. Nur veröffentlichte Artefakte (über den Artefakt-Veröffentlichungsschritt) können abgerufen werden. Lassen Sie das Feld leer, um keine Artefakte abzurufen");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"Optional geben Sie autorisierte Rollen an, um diese Schaltfläche zu drücken. Wenn nicht angegeben, sind alle Benutzer berechtigt");
		m.put("Optionally specify base query of the list", "Optional geben Sie die Basisabfrage der Liste an");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"Optional geben Sie Zweige/Benutzer/Gruppen an, die Zugriff auf dieses Geheimnis haben. Wenn leer gelassen, kann jeder Job auf dieses Geheimnis zugreifen, einschließlich derjenigen, die über externe Pull-Anfragen ausgelöst werden");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"Optional geben Sie den Build-Kontextpfad relativ zu <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> an. Lassen Sie das Feld leer, um den Job-Arbeitsbereich selbst zu verwenden. Die Datei <code>Dockerfile</code> wird im Build-Kontextverzeichnis erwartet, es sei denn, Sie geben einen anderen Speicherort mit der Option <code>--dockerfile</code> an");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"Optional geben Sie den Build-Pfad relativ zu <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> an. Lassen Sie das Feld leer, um den Job-Arbeitsbereich selbst zu verwenden");
		m.put("Optionally specify cluster role the job pods service account binding to. This is necessary if you want to do things such as running other Kubernetes pods in job command", 
			"Optional geben Sie die Cluster-Rolle an, an die das Service-Konto der Job-Pods gebunden ist. Dies ist erforderlich, wenn Sie Dinge wie das Ausführen anderer Kubernetes-Pods im Job-Befehl ausführen möchten");
		m.put("Optionally specify comma separated licenses to be ignored", "Optional geben Sie kommagetrennte Lizenzen an, die ignoriert werden sollen");
		m.put("Optionally specify container arguments separated by space. Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse this with container options which should be specified in executor setting", 
			"Optional geben Sie Container-Argumente an, die durch Leerzeichen getrennt sind. Einzelne Argumente, die Leerzeichen enthalten, sollten in Anführungszeichen gesetzt werden. <b class='text-warning'>Hinweis: </b> Verwechseln Sie dies nicht mit Container-Optionen, die in der Executor-Einstellung angegeben werden sollten");
		m.put("Optionally specify cpu limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Optional geben Sie das CPU-Limit für jeden Job/Service an, der diesen Executor verwendet. Überprüfen Sie <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes-Ressourcenmanagement</a> für Details");
		m.put("Optionally specify cpu limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> of relevant containers", 
			"Optional geben Sie das CPU-Limit für jeden Job/Service an, der diesen Executor verwendet. Dies wird als Option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> der relevanten Container verwendet");
		m.put("Optionally specify criteria of issues which can be linked", "Optional geben Sie Kriterien für Probleme an, die verknüpft werden können");
		m.put("Optionally specify criteria of issues which can be linked on the other side", "Optional geben Sie Kriterien für Probleme an, die auf der anderen Seite verknüpft werden können");
		m.put("Optionally specify custom fields allowed to edit when open new issues", "Optional geben Sie benutzerdefinierte Felder an, die beim Öffnen neuer Probleme bearbeitet werden dürfen");
		m.put("Optionally specify depth for a shallow clone in order to speed up source retrieval", 
			"Optional geben Sie die Tiefe für einen flachen Klon an, um die Quellabrufgeschwindigkeit zu erhöhen");
		m.put("Optionally specify description of the issue", "Optional geben Sie die Beschreibung des Problems an");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"Optional geben Sie Verzeichnisse oder Glob-Muster innerhalb des Scan-Pfads an, die übersprungen werden sollen. Mehrere Übersprünge sollten durch Leerzeichen getrennt werden");
		m.put("Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types", 
			"Optional nicht erlaubte Dateitypen durch Erweiterungen angeben (drücken Sie ENTER, um den Wert hinzuzufügen), zum Beispiel <code>exe</code>, <code>bin</code>. Leer lassen, um alle Dateitypen zuzulassen");
		m.put("Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. Leave empty to use docker executable in PATH", 
			"Optional geben Sie die Docker-Ausführungsdatei an, beispielsweise <i>/usr/local/bin/docker</i>. Lassen Sie das Feld leer, um die Docker-Ausführungsdatei im PATH zu verwenden");
		m.put("Optionally specify docker options to create network. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Optional geben Sie Docker-Optionen an, um ein Netzwerk zu erstellen. Mehrere Optionen sollten durch Leerzeichen getrennt werden, und einzelne Optionen, die Leerzeichen enthalten, sollten in Anführungszeichen gesetzt werden");
		m.put("Optionally specify docker options to run container. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Optional geben Sie Docker-Optionen an, um Container auszuführen. Mehrere Optionen sollten durch Leerzeichen getrennt werden, und einzelne Optionen, die Leerzeichen enthalten, sollten in Anführungszeichen gesetzt werden");
		m.put("Optionally specify docker sock to use. Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows", 
			"Optional geben Sie den Docker-Sock an, der verwendet werden soll. Standardmäßig <i>/var/run/docker.sock</i> unter Linux und <i>//./pipe/docker_engine</i> unter Windows");
		m.put("Optionally specify environment variables for the container", "Optional geben Sie Umgebungsvariablen für den Container an");
		m.put("Optionally specify environment variables for this step", "Optional geben Sie Umgebungsvariablen für diesen Schritt an");
		m.put("Optionally specify environment variables of the service", "Optional geben Sie Umgebungsvariablen des Dienstes an");
		m.put("Optionally specify estimated time.", "Optional geben Sie die geschätzte Zeit an.");
		m.put("Optionally specify executor for this job. Leave empty to use auto-discover executor", 
			"Optional Executor für diesen Job angeben. Leer lassen, um automatisch erkannten Executor zu verwenden");
		m.put("Optionally specify executor for this job. Leave empty to use first applicable executor", 
			"Optional Executor für diesen Job angeben. Leer lassen, um den ersten anwendbaren Executor zu verwenden");
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"Optional geben Sie Dateien relativ zum Cache-Pfad an, die ignoriert werden sollen, wenn Cache-Änderungen erkannt werden. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Mehrere Dateien sollten durch Leerzeichen getrennt werden, und einzelne Dateien, die Leerzeichen enthalten, sollten in Anführungszeichen gesetzt werden");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"Optional geben Sie die Gruppen-Suchbasis an, wenn Sie Gruppenmitgliedschaftsinformationen des Benutzers abrufen möchten. Zum Beispiel: <i>cn=Users, dc=example, dc=com</i>. Um einer Active Directory-Gruppe entsprechende Berechtigungen zu erteilen, sollte eine OneDev-Gruppe mit demselben Namen definiert werden. Lassen Sie das Feld leer, um Gruppenmitgliedschaften auf der OneDev-Seite zu verwalten");
		m.put("Optionally specify issue links allowed to edit", "Optional geben Sie Problemverknüpfungen an, die bearbeitet werden dürfen");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "Optional geben Sie Probleme an, die für diese Vorlage anwendbar sind. Lassen Sie das Feld leer für alle");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"Optional geben Sie Probleme an, die für diesen Übergang anwendbar sind. Lassen Sie das Feld leer für alle Probleme");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"Optional geben Sie Probleme an, die für diesen Übergang anwendbar sind. Lassen Sie das Feld leer für alle Probleme.");
		m.put("Optionally specify jobs allowed to use this script", "Optional geben Sie Jobs an, die dieses Skript verwenden dürfen");
		m.put("Optionally specify memory limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Optional geben Sie das Speicherlimit für jeden Job/Service an, der diesen Executor verwendet. Überprüfen Sie <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes-Ressourcenmanagement</a> für Details");
		m.put("Optionally specify memory limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> of relevant containers", 
			"Optional geben Sie das Speicherlimit für jeden Job/Service an, der diesen Executor verwendet. Dies wird als Option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> der relevanten Container verwendet");
		m.put("Optionally specify merge strategy of created pull request. Leave empty to use default strategy of each project", 
			"Optional geben Sie die Zusammenführungsstrategie des erstellten Pull-Requests an. Lassen Sie das Feld leer, um die Standardstrategie jedes Projekts zu verwenden");
		m.put("Optionally specify message of the tag", "Optional geben Sie die Nachricht des Tags an");
		m.put("Optionally specify name of the attribute inside the user LDAP entry whose values will be taken as user SSH keys. SSH keys will be managed by LDAP only if this field is set", 
			"Optional geben Sie den Namen des Attributs innerhalb des Benutzer-LDAP-Eintrags an, dessen Werte als Benutzer-SSH-Schlüssel verwendet werden. SSH-Schlüssel werden nur von LDAP verwaltet, wenn dieses Feld gesetzt ist");
		m.put("Optionally specify node selector of the job pods", "Optional geben Sie den Node-Selector der Job-Pods an");
		m.put("Optionally specify options for docker builder prune command", "Optional geben Sie Optionen für den Docker-Builder-Prune-Befehl an");
		m.put("Optionally specify options for scp command. Multiple options need to be separated with space", 
			"Optional geben Sie Optionen für den SCP-Befehl an. Mehrere Optionen müssen durch Leerzeichen getrennt werden");
		m.put("Optionally specify options for ssh command. Multiple options need to be separated with space", 
			"Optional geben Sie Optionen für den SSH-Befehl an. Mehrere Optionen müssen durch Leerzeichen getrennt werden");
		m.put("Optionally specify options passed to renovate cli. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Optional geben Sie Optionen an, die an die Renovate-CLI übergeben werden. Mehrere Optionen sollten durch Leerzeichen getrennt werden, und einzelne Optionen, die Leerzeichen enthalten, sollten in Anführungszeichen gesetzt werden");
		m.put("Optionally specify osv scanner <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. You may ignore particular vulnerabilities via this file", 
			"Optional geben Sie die OSV-Scanner-<a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>Konfigurationsdatei</a> unter <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> an. Sie können bestimmte Schwachstellen über diese Datei ignorieren");
		m.put("Optionally specify path protection rules", "Optional geben Sie Pfadschutzregeln an");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"Optional geben Sie einen Pfad relativ zu <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, der als Trivy-<a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>Ignore-Datei</a> verwendet werden soll");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"Optional geben Sie einen Pfad relativ zu <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, der als Trivy-<a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>Geheimnis-Konfiguration</a> verwendet werden soll");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"Optional geben Sie einen Pfad relativ zu <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, um Artefakte zu veröffentlichen. Lassen Sie das Feld leer, um den Job-Arbeitsbereich selbst zu verwenden");
		m.put("Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. Leave empty to pull all platforms in image", 
			"Optional geben Sie die Plattform zum Abrufen an, beispielsweise <tt>linux/amd64</tt>. Lassen Sie das Feld leer, um alle Plattformen im Image abzurufen");
		m.put("Optionally specify project to show builds of. Leave empty to show builds of all projects with permissions", 
			"Optional geben Sie das Projekt an, um Builds anzuzeigen. Lassen Sie das Feld leer, um Builds aller Projekte mit Berechtigungen anzuzeigen");
		m.put("Optionally specify project to show issues of. Leave empty to show issues of all accessible projects", 
			"Optional geben Sie das Projekt an, um Probleme anzuzeigen. Lassen Sie das Feld leer, um Probleme aller zugänglichen Projekte anzuzeigen");
		m.put("Optionally specify project to show packages of. Leave empty to show packages of all projects with permissions", 
			"Optional geben Sie das Projekt an, um Pakete anzuzeigen. Lassen Sie das Feld leer, um Pakete aller Projekte mit Berechtigungen anzuzeigen");
		m.put("Optionally specify ref of above job, for instance <i>refs/heads/main</i>. Use * for wildcard match", 
			"Optional geben Sie den Ref des oben genannten Jobs an, beispielsweise <i>refs/heads/main</i>. Verwenden Sie * für Wildcard-Abgleich");
		m.put("Optionally specify registry logins to override those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token secret for password secret", 
			"Optional geben Sie Registrierungsanmeldungen an, um die im Job-Executor definierten zu überschreiben. Für die integrierte Registrierung verwenden Sie <code>@server_url@</code> für die Registrierungs-URL, <code>@job_token@</code> für den Benutzernamen und das Zugriffstoken-Geheimnis für das Passwort-Geheimnis");
		m.put("Optionally specify relative directory to put uploaded files", "Optional geben Sie ein relatives Verzeichnis an, um hochgeladene Dateien abzulegen");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to clone code into. Leave empty to use job workspace itself", 
			"Optional geben Sie einen relativen Pfad unter <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, um Code zu klonen. Lassen Sie das Feld leer, um den Job-Arbeitsbereich selbst zu verwenden");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"Optional geben Sie einen relativen Pfad unter <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, um zu scannen. Lassen Sie das Feld leer, um den Job-Arbeitsbereich selbst zu verwenden");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"Optional geben Sie relative Pfade unter <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, um Schwachstellen in Abhängigkeiten zu scannen. Mehrere Pfade können angegeben und sollten durch Leerzeichen getrennt werden. Lassen Sie das Feld leer, um den Job-Arbeitsbereich selbst zu verwenden");
		m.put("Optionally specify required reviewers for changes of specified branch", "Optional geben Sie erforderliche Prüfer für Änderungen des angegebenen Zweigs an");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"Optional geben Sie die Revision an, um den Zweig zu erstellen. Lassen Sie das Feld leer, um vom Build-Commit zu erstellen");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"Optional geben Sie ein separates Verzeichnis an, um Build-Artefakte zu speichern. Nicht absolute Verzeichnisse werden als relativ zum Site-Verzeichnis betrachtet");
		m.put("Optionally specify separate directory to store git lfs files. Non-absolute directory is considered to be relative to site directory", 
			"Optional geben Sie ein separates Verzeichnis an, um Git-LFS-Dateien zu speichern. Nicht absolute Verzeichnisse werden als relativ zum Site-Verzeichnis betrachtet");
		m.put("Optionally specify separate directory to store package files. Non-absolute directory is considered to be relative to site directory", 
			"Optional geben Sie ein separates Verzeichnis an, um Paketdateien zu speichern. Nicht absolute Verzeichnisse werden als relativ zum Site-Verzeichnis betrachtet");
		m.put("Optionally specify services required by this job. <b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors (server docker executor, remote docker executor, or kubernetes executor)", 
			"Optional geben Sie Dienste an, die für diesen Job erforderlich sind. <b class='text-warning'>HINWEIS:</b> Dienste werden nur von Docker-fähigen Executoren unterstützt (Server-Docker-Executor, Remote-Docker-Executor oder Kubernetes-Executor)");
		m.put("Optionally specify space-separated branches applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Optional geben Sie durch Leerzeichen getrennte Zweige an, die für diesen Übergang anwendbar sind. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-' zum Ausschließen. Lassen Sie das Feld leer, um alle zu entsprechen");
		m.put("Optionally specify space-separated branches applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for default branch", 
			"Optional geben Sie durch Leerzeichen getrennte Zweige an, die für diesen Trigger anwendbar sind. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-' zum Ausschließen. Lassen Sie das Feld leer für den Standardzweig");
		m.put("Optionally specify space-separated branches to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Optional geben Sie durch Leerzeichen getrennte Zweige an, die überprüft werden sollen. Verwenden Sie '**' oder '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-' zum Ausschließen. Lassen Sie das Feld leer, um alle Zweige zu entsprechen");
		m.put("Optionally specify space-separated commit messages applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Optional geben Sie durch Leerzeichen getrennte Commit-Nachrichten an, die für diesen Übergang anwendbar sind. Verwenden Sie '*' oder '?' für Platzhalter. Mit '-' voranstellen, um auszuschließen. Leer lassen, um alle zu matchen");
		m.put("Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all files", 
			"Optional geben Sie durch Leerzeichen getrennte Dateien an, die überprüft werden sollen. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-' zum Ausschließen. Lassen Sie das Feld leer, um alle Dateien zu entsprechen");
		m.put("Optionally specify space-separated jobs applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Optional geben Sie durch Leerzeichen getrennte Jobs an, die für diesen Übergang anwendbar sind. Verwenden Sie '*' oder '?' für Wildcard-Abgleich. Präfix mit '-' zum Ausschließen. Lassen Sie das Feld leer, um alle zu entsprechen");
		m.put("Optionally specify space-separated projects applicable for this trigger. This is useful for instance when you want to prevent the job from being triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Optional geben Sie durch Leerzeichen getrennte Projekte an, die für diesen Trigger anwendbar sind. Dies ist nützlich, wenn Sie beispielsweise verhindern möchten, dass der Job in geforkten Projekten ausgelöst wird. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-' zum Ausschließen. Lassen Sie das Feld leer, um alle Projekte zu entsprechen");
		m.put("Optionally specify space-separated projects to search in. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to search in all projects with code read permission", 
			"Optional geben Sie durch Leerzeichen getrennte Projekte an, in denen gesucht werden soll. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-' zum Ausschließen. Lassen Sie das Feld leer, um in allen Projekten mit Leseberechtigung für Code zu suchen");
		m.put("Optionally specify space-separated reports. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Optional geben Sie durch Leerzeichen getrennte Berichte an. Verwenden Sie '*' oder '?' für Platzhalter. Mit '-' voranstellen, um auszuschließen");
		m.put("Optionally specify space-separated service images applicable for this locator. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Optional geben Sie durch Leerzeichen getrennte Service-Images an, die für diesen Locator gelten. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Platzhalter</a>. Mit '-' voranstellen, um auszuschließen. Leer lassen, um alle zuzuordnen");
		m.put("Optionally specify space-separated service names applicable for this locator. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Optional geben Sie durch Leerzeichen getrennte Servicenamen an, die für diesen Locator gelten. Verwenden Sie '*' oder '?' für Platzhalter. Mit '-' voranstellen, um auszuschließen. Leer lassen, um alle zuzuordnen");
		m.put("Optionally specify space-separated tags to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all tags", 
			"Optional geben Sie durch Leerzeichen getrennte Tags an, die überprüft werden sollen. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Platzhalter</a>. Mit '-' voranstellen, um auszuschließen. Leer lassen, um alle Tags zuzuordnen");
		m.put("Optionally specify space-separated target branches of the pull requests to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Optional geben Sie durch Leerzeichen getrennte Zielzweige der Pull-Requests an, die überprüft werden sollen. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Platzhalter</a>. Mit '-' voranstellen, um auszuschließen. Leer lassen, um alle Zweige zuzuordnen");
		m.put("Optionally specify the OpenID claim to retrieve groups of authenticated user. Depending on the provider, you may need to request additional scopes above to make this claim available", 
			"Optional geben Sie den OpenID-Anspruch an, um Gruppen des authentifizierten Benutzers abzurufen. Je nach Anbieter müssen Sie möglicherweise zusätzliche Berechtigungen anfordern, um diesen Anspruch verfügbar zu machen");
		m.put("Optionally specify the maximum value allowed.", "Optional geben Sie den maximal zulässigen Wert an.");
		m.put("Optionally specify the minimum value allowed.", "Optional geben Sie den minimal zulässigen Wert an.");
		m.put("Optionally specify the project to publish site files to. Leave empty to publish to current project", 
			"Optional geben Sie das Projekt an, in dem die Website-Dateien veröffentlicht werden sollen. Leer lassen, um im aktuellen Projekt zu veröffentlichen");
		m.put("Optionally specify uid:gid to run container as. <b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or using user namespace remapping", 
			"Optional geben Sie uid:gid an, um den Container auszuführen. <b class='text-warning'>Hinweis:</b> Diese Einstellung sollte leer gelassen werden, wenn die Container-Laufzeit rootless ist oder Benutzer-Namespace-Remapping verwendet");
		m.put("Optionally specify user name to access remote repository", "Optional geben Sie den Benutzernamen für den Zugriff auf das Remote-Repository an");
		m.put("Optionally specify valid scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope", 
			"Optional geben Sie gültige Bereiche für konventionelle Commits an (drücken Sie ENTER, um einen Wert hinzuzufügen). Leer lassen, um beliebige Bereiche zuzulassen");
		m.put("Optionally specify valid types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type", 
			"Optional geben Sie gültige Typen für konventionelle Commits an (drücken Sie ENTER, um einen Wert hinzuzufügen). Leer lassen, um beliebige Typen zuzulassen");
		m.put("Optionally specify value of git config <code>pack.packSizeLimit</code> for the repository", 
			"Optional geben Sie den Wert der Git-Konfiguration <code>pack.packSizeLimit</code> für das Repository an");
		m.put("Optionally specify value of git config <code>pack.threads</code> for the repository", 
			"Optional geben Sie den Wert der Git-Konfiguration <code>pack.threads</code> für das Repository an");
		m.put("Optionally specify value of git config <code>pack.window</code> for the repository", 
			"Optional geben Sie den Wert der Git-Konfiguration <code>pack.window</code> für das Repository an");
		m.put("Optionally specify value of git config <code>pack.windowMemory</code> for the repository", 
			"Optional geben Sie den Wert der Git-Konfiguration <code>pack.windowMemory</code> für das Repository an");
		m.put("Optionally specify where to run service pods specified in job. The first matching locator will be used. If no any locators are found, node selector of the executor will be used", 
			"Optional geben Sie an, wo die in der Aufgabe angegebenen Service-Pods ausgeführt werden sollen. Der erste übereinstimmende Locator wird verwendet. Wenn keine Locator gefunden werden, wird der Node-Selector des Executors verwendet");
		m.put("Optionally specify working directory of the container. Leave empty to use default working directory of the container", 
			"Optional geben Sie das Arbeitsverzeichnis des Containers an. Leer lassen, um das Standard-Arbeitsverzeichnis des Containers zu verwenden");
		m.put("Options", "Optionen");
		m.put("Or manually enter the secret key below in your authenticator app", "Oder geben Sie den geheimen Schlüssel unten manuell in Ihrer Authentifizierungs-App ein");
		m.put("Order By", "Sortieren nach");
		m.put("Order More User Months", "Bestellen Sie mehr Benutzer-Monate");
		m.put("Order Subscription", "Abonnement bestellen");
		m.put("Ordered List", "Geordnete Liste");
		m.put("Ordered list", "Geordnete Liste");
		m.put("Osv License Scanner", "Osv-Lizenzscanner");
		m.put("Osv Vulnerability Scanner", "Osv-Schwachstellenscanner");
		m.put("Other", "Andere");
		m.put("Outline", "Umriss");
		m.put("Outline Search", "Umrisssuche");
		m.put("Output", "Ausgabe");
		m.put("Overall", "Gesamt");
		m.put("Overall Estimated Time:", "Gesamtschätzzeit:");
		m.put("Overall Spent Time:", "Gesamtaufgewendete Zeit:");
		m.put("Overview", "Übersicht");
		m.put("Own:", "Eigene:");
		m.put("Ownered By", "Besitz von");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "PEM-Privatschlüssel beginnt mit '-----BEGIN RSA PRIVATE KEY-----'");
		m.put("PENDING", "AUSSTEHEND");
		m.put("PMD Report", "PMD-Bericht");
		m.put("Pack", "Paket");
		m.put("Pack Notification", "Paketbenachrichtigung");
		m.put("Pack Size Limit", "Paketgrößenlimit");
		m.put("Pack Type", "Pakettyp");
		m.put("Package", "Paket");
		m.put("Package Management", "Paketverwaltung");
		m.put("Package Notification", "Paketbenachrichtigung");
		m.put("Package Notification Template", "Paketbenachrichtigungsvorlage");
		m.put("Package Privilege", "Paketberechtigung");
		m.put("Package Storage", "Paketspeicher");
		m.put("Package list", "Paketliste");
		m.put("Package {0} deleted", "Paket {0} gelöscht");
		m.put("Packages", "Pakete");
		m.put("Page Not Found", "Seite nicht gefunden");
		m.put("Page is in error, reload to recover", "Seite ist fehlerhaft, neu laden, um sich zu erholen");
		m.put("Param Instance", "Parameterinstanz");
		m.put("Param Instances", "Parameterinstanzen");
		m.put("Param Map", "Parameterkarte");
		m.put("Param Matrix", "Parametermatrix");
		m.put("Param Name", "Parametername");
		m.put("Param Spec", "Parameterspezifikation");
		m.put("Param Spec Bean", "Parameterspezifikations-Bean");
		m.put("Parameter", "Parameter");
		m.put("Parameter Specs", "Parameterspezifikationen");
		m.put("Params", "Parameter");
		m.put("Params & Triggers", "Parameter & Trigger");
		m.put("Params to Display", "Anzuzeigende Parameter");
		m.put("Parent Bean", "Übergeordnete Bean");
		m.put("Parent OneDev Project", "Übergeordnetes OneDev-Projekt");
		m.put("Parent Project", "Übergeordnetes Projekt");
		m.put("Parent project not found", "Übergeordnetes Projekt nicht gefunden");
		m.put("Parents", "Eltern");
		m.put("Partially covered", "Teilweise abgedeckt");
		m.put("Partially covered by some tests", "Teilweise durch einige Tests abgedeckt");
		m.put("Passcode", "Passcode");
		m.put("Passed", "Bestanden");
		m.put("Password", "Passwort");
		m.put("Password Authenticator", "Passwort-Authenticator");
		m.put("Password Edit Bean", "Passwort-Bearbeitungs-Bean");
		m.put("Password Must Contain Digit", "Passwort muss Ziffer enthalten");
		m.put("Password Must Contain Lowercase", "Passwort muss Kleinbuchstaben enthalten");
		m.put("Password Must Contain Special Character", "Passwort muss Sonderzeichen enthalten");
		m.put("Password Must Contain Uppercase", "Passwort muss Großbuchstaben enthalten");
		m.put("Password Policy", "Passwortrichtlinie");
		m.put("Password Reset", "Passwort zurücksetzen");
		m.put("Password Reset Bean", "Passwort-Zurücksetz-Bean");
		m.put("Password Reset Template", "Passwort-Zurücksetzvorlage");
		m.put("Password Secret", "Passwort-Geheimnis");
		m.put("Password and its confirmation should be identical.", "Passwort und dessen Bestätigung sollten identisch sein.");
		m.put("Password changed. Please login with your new password", "Passwort geändert. Bitte melden Sie sich mit Ihrem neuen Passwort an");
		m.put("Password has been changed", "Passwort wurde geändert");
		m.put("Password has been removed", "Passwort wurde entfernt");
		m.put("Password has been set", "Passwort wurde gesetzt");
		m.put("Password of the user", "Passwort des Benutzers");
		m.put("Password or Access Token for Remote Repository", "Passwort oder Zugriffstoken für Remote-Repository");
		m.put("Password reset request has been sent", "Anfrage zum Zurücksetzen des Passworts wurde gesendet");
		m.put("Password reset url is invalid or obsolete", "Passwort-Reset-URL ist ungültig oder veraltet");
		m.put("PasswordMinimum Length", "Minimale Passwortlänge");
		m.put("Paste subscription key here", "Abonnement-Schlüssel hier einfügen");
		m.put("Path containing spaces or starting with dash needs to be quoted", "Pfad mit Leerzeichen oder beginnend mit einem Bindestrich muss in Anführungszeichen gesetzt werden");
		m.put("Path placeholder", "Pfad-Platzhalter");
		m.put("Path to kubectl", "Pfad zu kubectl");
		m.put("Paths", "Pfade");
		m.put("Pattern", "Muster");
		m.put("Pause", "Pause");
		m.put("Pause All Queried Agents", "Alle abgefragten Agenten pausieren");
		m.put("Pause Selected Agents", "Ausgewählte Agenten pausieren");
		m.put("Paused", "Pausiert");
		m.put("Paused all queried agents", "Alle abgefragten Agenten pausiert");
		m.put("Paused selected agents", "Ausgewählte Agenten pausiert");
		m.put("Pem Private Key", "Pem-Privatschlüssel");
		m.put("Pending", "Ausstehend");
		m.put("Performance", "Leistung");
		m.put("Performance Setting", "Leistungseinstellung");
		m.put("Performance Settings", "Leistungseinstellungen");
		m.put("Performance settings have been saved", "Leistungseinstellungen wurden gespeichert");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\"", 
			"Unscharfe Abfrage wird durchgeführt. Suchtext mit '~' einschließen, um weitere Bedingungen hinzuzufügen, z. B.: ~Suchtext~ und \"Status\" ist \"Offen\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"", 
			"Unscharfe Abfrage wird durchgeführt. Suchtext mit '~' einschließen, um weitere Bedingungen hinzuzufügen, z. B.: ~Suchtext~ und \"Typ\" ist \"NPM\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online", 
			"Unscharfe Abfrage wird durchgeführt. Suchtext mit '~' einschließen, um weitere Bedingungen hinzuzufügen, z. B.: ~Suchtext~ und online");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"Unscharfe Abfrage wird durchgeführt. Suchtext mit '~' einschließen, um weitere Bedingungen hinzuzufügen, z. B.: ~Suchtext~ und offen");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and owned by me", 
			"Unscharfe Abfrage wird durchgeführt. Suchtext mit '~' einschließen, um weitere Bedingungen hinzuzufügen, z. B.: ~Suchtext~ und mir zugeordnet");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved", 
			"Unscharfe Abfrage wird durchgeführt. Suchtext mit '~' einschließen, um weitere Bedingungen hinzuzufügen, z. B.: ~Suchtext~ und ungelöst");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"Unscharfe Abfrage wird durchgeführt. Suchtext mit '~' einschließen, um weitere Bedingungen hinzuzufügen, z. B.: ~Suchtext~ Autor(robin)");
		m.put("Permanent link", "Permanenter Link");
		m.put("Permanent link of this selection", "Permanenter Link dieser Auswahl");
		m.put("Permission denied", "Zugriff verweigert");
		m.put("Permission will be checked upon actual operation", "Berechtigung wird bei der tatsächlichen Operation überprüft");
		m.put("Physical memory in mega bytes", "Physischer Speicher in Megabyte");
		m.put("Pick Existing", "Vorhandenes auswählen");
		m.put("Pin this issue", "Dieses Problem anheften");
		m.put("Pipeline", "Pipeline");
		m.put("Placeholder", "Platzhalter");
		m.put("Plain text expected", "Reiner Text erwartet");
		m.put("Platform", "Plattform");
		m.put("Platforms", "Plattformen");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"Bitte <a wicket:id=\"download\" class=\"font-weight-bolder\">laden</a> Sie die Wiederherstellungscodes unten herunter und bewahren Sie sie geheim. Diese Codes können verwendet werden, um einmaligen Zugriff auf Ihr Konto zu gewähren, falls Sie keinen Zugriff auf die Authentifizierungsanwendung haben. Sie werden <b>NICHT</b> erneut angezeigt");
		m.put("Please Confirm", "Bitte bestätigen");
		m.put("Please Note", "Bitte beachten");
		m.put("Please check your email for password reset instructions", "Bitte überprüfen Sie Ihre E-Mail für Anweisungen zum Zurücksetzen des Passworts");
		m.put("Please choose revision to create branch from", "Bitte wählen Sie eine Revision aus, um einen Branch zu erstellen");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "Bitte konfigurieren Sie zuerst die <a wicket:id=\"mailSetting\">E-Mail-Einstellungen</a>");
		m.put("Please confirm", "Bitte bestätigen");
		m.put("Please confirm the password.", "Bitte bestätigen Sie das Passwort.");
		m.put("Please follow <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">this instruction</a> to resolve the conflicts", 
			"Bitte folgen Sie <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">dieser Anleitung</a>, um die Konflikte zu lösen");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"Bitte geben Sie einen Ihrer Wiederherstellungscodes ein, die beim Aktivieren der Zwei-Faktor-Authentifizierung gespeichert wurden");
		m.put("Please login to perform this operation", "Bitte melden Sie sich an, um diese Operation auszuführen");
		m.put("Please login to perform this query", "Bitte melden Sie sich an, um diese Abfrage auszuführen");
		m.put("Please resolve undefined field values below", "Bitte beheben Sie die unten undefinierten Feldwerte");
		m.put("Please resolve undefined fields below", "Bitte beheben Sie die unten undefinierten Felder");
		m.put("Please resolve undefined states below. Note that if you select to delete an undefined state, all issues with that state will be deleted", 
			"Bitte beheben Sie die unten undefinierten Zustände. Beachten Sie, dass beim Löschen eines undefinierten Zustands alle Probleme mit diesem Zustand gelöscht werden");
		m.put("Please select agents to pause", "Bitte wählen Sie Agenten aus, die pausiert werden sollen");
		m.put("Please select agents to remove", "Bitte wählen Sie Agenten aus, die entfernt werden sollen");
		m.put("Please select agents to restart", "Bitte wählen Sie Agenten aus, die neu gestartet werden sollen");
		m.put("Please select agents to resume", "Bitte wählen Sie Agenten aus, die fortgesetzt werden sollen");
		m.put("Please select branches to create pull request", "Bitte wählen Sie Branches aus, um eine Pull-Anfrage zu erstellen");
		m.put("Please select builds to cancel", "Bitte wählen Sie Builds aus, die abgebrochen werden sollen");
		m.put("Please select builds to delete", "Bitte wählen Sie Builds aus, die gelöscht werden sollen");
		m.put("Please select builds to re-run", "Bitte wählen Sie Builds aus, die erneut ausgeführt werden sollen");
		m.put("Please select comments to delete", "Bitte wählen Sie Kommentare aus, die gelöscht werden sollen");
		m.put("Please select comments to set resolved", "Bitte wählen Sie Kommentare aus, die als gelöst markiert werden sollen");
		m.put("Please select comments to set unresolved", "Bitte wählen Sie Kommentare aus, die als ungelöst markiert werden sollen");
		m.put("Please select different branches", "Bitte wählen Sie verschiedene Branches aus");
		m.put("Please select fields to update", "Bitte wählen Sie Felder aus, die aktualisiert werden sollen");
		m.put("Please select groups to remove from", "Bitte wählen Sie Gruppen zum Entfernen aus");
		m.put("Please select issues to copy", "Bitte wählen Sie Probleme aus, die kopiert werden sollen");
		m.put("Please select issues to delete", "Bitte wählen Sie Probleme aus, die gelöscht werden sollen");
		m.put("Please select issues to edit", "Bitte wählen Sie Probleme aus, die bearbeitet werden sollen");
		m.put("Please select issues to move", "Bitte wählen Sie Probleme aus, die verschoben werden sollen");
		m.put("Please select issues to sync estimated/spent time", "Bitte wählen Sie Probleme aus, um geschätzte/aufgewendete Zeit zu synchronisieren");
		m.put("Please select packages to delete", "Bitte wählen Sie Pakete aus, die gelöscht werden sollen");
		m.put("Please select projects to delete", "Bitte wählen Sie Projekte aus, die gelöscht werden sollen");
		m.put("Please select projects to modify", "Bitte wählen Sie Projekte aus, die geändert werden sollen");
		m.put("Please select projects to move", "Bitte wählen Sie Projekte aus, die verschoben werden sollen");
		m.put("Please select pull requests to delete", "Bitte wählen Sie Pull-Anfragen aus, die gelöscht werden sollen");
		m.put("Please select pull requests to discard", "Bitte wählen Sie Pull-Anfragen aus, die verworfen werden sollen");
		m.put("Please select pull requests to watch/unwatch", "Bitte wählen Sie Pull-Anfragen aus, die beobachtet/nicht beobachtet werden sollen");
		m.put("Please select query watches to delete", "Bitte wählen Sie Abfragebeobachtungen aus, die gelöscht werden sollen");
		m.put("Please select revision to create tag from", "Bitte wählen Sie eine Revision aus, um einen Tag zu erstellen");
		m.put("Please select revisions to compare", "Bitte wählen Sie Revisionen aus, die verglichen werden sollen");
		m.put("Please select users to convert to service accounts", "Bitte wählen Sie Benutzer zum Umwandeln in Dienstkonten aus");
		m.put("Please select users to disable", "Bitte wählen Sie Benutzer aus, die deaktiviert werden sollen");
		m.put("Please select users to enable", "Bitte wählen Sie Benutzer aus, die aktiviert werden sollen");
		m.put("Please select users to remove from group", "Bitte wählen Sie Benutzer zum Entfernen aus der Gruppe aus");
		m.put("Please specify file name above before editing content", "Bitte geben Sie oben einen Dateinamen an, bevor Sie den Inhalt bearbeiten");
		m.put("Please switch to packages page of a particular project for the instructions", "Bitte wechseln Sie zur Paketseite eines bestimmten Projekts für die Anweisungen");
		m.put("Please wait...", "Bitte warten...");
		m.put("Please waiting...", "Bitte warten...");
		m.put("Plugin metadata not found", "Plugin-Metadaten nicht gefunden");
		m.put("Poll Interval", "Abfrageintervall");
		m.put("Populate Tag Mappings", "Tag-Zuordnungen ausfüllen");
		m.put("Port", "Port");
		m.put("Post", "Posten");
		m.put("Post Build Action", "Post-Build-Aktion");
		m.put("Post Build Action Bean", "Post-Build-Aktions-Bean");
		m.put("Post Build Actions", "Post-Build-Aktionen");
		m.put("Post Url", "Post-URL");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "Präfix-Muster");
		m.put("Prefix the title with <code>WIP</code> or <code>[WIP]</code> to mark the pull request as work in progress", 
			"Präfixieren Sie den Titel mit <code>WIP</code> oder <code>[WIP]</code>, um die Pull-Anfrage als in Bearbeitung zu kennzeichnen");
		m.put("Prepend", "Voranstellen");
		m.put("Preserve Days", "Tage bewahren");
		m.put("Preset Commit Message", "Voreingestellte Commit-Nachricht");
		m.put("Preset commit message updated", "Voreingestellte Commit-Nachricht aktualisiert");
		m.put("Press 'y' to get permalink", "Drücken Sie 'y', um einen Permalink zu erhalten");
		m.put("Prev", "Zurück");
		m.put("Prevent Creation", "Erstellung verhindern");
		m.put("Prevent Deletion", "Löschung verhindern");
		m.put("Prevent Forced Push", "Erzwungenes Pushen verhindern");
		m.put("Prevent Update", "Aktualisierung verhindern");
		m.put("Preview", "Vorschau");
		m.put("Previous", "Vorherige");
		m.put("Previous Value", "Vorheriger Wert");
		m.put("Previous commit", "Vorheriger Commit");
		m.put("Previous {0}", "Vorherige {0}");
		m.put("Primary", "Primär");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "Primäre <a wicket:id=\"noPrimaryAddressLink\">E-Mail-Adresse</a> nicht angegeben");
		m.put("Primary Email", "Primäre E-Mail");
		m.put("Primary email address not specified", "Primäre E-Mail-Adresse nicht angegeben");
		m.put("Primary email address of your account is not specified yet", "Die primäre E-Mail-Adresse Ihres Kontos ist noch nicht angegeben");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"Die primäre E-Mail-Adresse wird verwendet, um Benachrichtigungen zu erhalten, Gravatar anzuzeigen (falls aktiviert) usw.");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"Primäre oder Alias-E-Mail-Adresse des oben genannten Kontos, die als Absenderadresse für verschiedene E-Mail-Benachrichtigungen verwendet wird. Der Benutzer kann auch auf diese Adresse antworten, um Kommentare zu Problemen oder Pull-Anfragen per E-Mail zu posten, wenn die Option <code>Check Incoming Email</code> unten aktiviert ist");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"Hauptname des Kontos, um sich beim Office 365-Mailserver anzumelden, um E-Mails zu senden/empfangen. Stellen Sie sicher, dass dieses Konto die registrierte Anwendung besitzt, die durch die oben angegebene Anwendungs-ID angezeigt wird");
		m.put("Private Key Secret", "Privater Schlüssel-Geheimnis");
		m.put("Private key regenerated and SSH server restarted", "Privater Schlüssel wurde regeneriert und SSH-Server neu gestartet");
		m.put("Privilege", "Berechtigung");
		m.put("Privilege Settings", "Berechtigungseinstellungen");
		m.put("Product Version", "Produktversion");
		m.put("Profile", "Profil");
		m.put("Programming language", "Programmiersprache");
		m.put("Project", "Projekt");
		m.put("Project \"{0}\" deleted", "Projekt \"{0}\" gelöscht");
		m.put("Project Authorization Bean", "Projekt-Autorisierungs-Bean");
		m.put("Project Authorizations Bean", "Projekt-Autorisierungen-Bean");
		m.put("Project Build Setting", "Projekt-Build-Einstellung");
		m.put("Project Dependencies", "Projekt-Abhängigkeiten");
		m.put("Project Dependency", "Projekt-Abhängigkeit");
		m.put("Project Id", "Projekt-ID");
		m.put("Project Import Option", "Projekt-Import-Option");
		m.put("Project Issue Setting", "Projekt-Issue-Einstellung");
		m.put("Project Key", "Projekt-Schlüssel");
		m.put("Project Management", "Projektmanagement");
		m.put("Project Pack Setting", "Projekt-Pack-Einstellung");
		m.put("Project Path", "Projekt-Pfad");
		m.put("Project Pull Request Setting", "Projekt-Pull-Request-Einstellung");
		m.put("Project Replicas", "Projekt-Replikate");
		m.put("Project authorizations updated", "Projekt-Autorisierungen aktualisiert");
		m.put("Project does not have any code yet", "Projekt hat noch keinen Code");
		m.put("Project forked", "Projekt geforkt");
		m.put("Project id", "Projekt-ID");
		m.put("Project list", "Projektliste");
		m.put("Project manage privilege required to delete \"{0}\"", "Projekt-Verwaltungsberechtigung erforderlich, um \"{0}\" zu löschen");
		m.put("Project manage privilege required to modify \"{0}\"", "Projekt-Verwaltungsberechtigung erforderlich, um \"{0}\" zu ändern");
		m.put("Project manage privilege required to move \"{0}\"", "Projekt-Verwaltungsberechtigung erforderlich, um \"{0}\" zu verschieben");
		m.put("Project name", "Projektname");
		m.put("Project not specified yet", "Projekt noch nicht angegeben");
		m.put("Project or revision not specified yet", "Projekt oder Revision noch nicht angegeben");
		m.put("Project overview", "Projektübersicht");
		m.put("Project path", "Projektpfad");
		m.put("Projects", "Projekte");
		m.put("Projects Bean", "Projekte-Bean");
		m.put("Projects deleted", "Projekte gelöscht");
		m.put("Projects modified", "Projekte geändert");
		m.put("Projects moved", "Projekte verschoben");
		m.put("Projects need to be redistributed when cluster members are added/removed. OneDev does not do this automatically as this is resource intensive, and you may only want to do it after cluster is finalized and stable.", 
			"Projekte müssen neu verteilt werden, wenn Cluster-Mitglieder hinzugefügt/entfernt werden. OneDev macht dies nicht automatisch, da dies ressourcenintensiv ist, und Sie möchten dies möglicherweise erst tun, nachdem der Cluster finalisiert und stabil ist.");
		m.put("Promotions", "Promotionen");
		m.put("Prompt Fields", "Eingabefelder");
		m.put("Properties", "Eigenschaften");
		m.put("Provide server id (guild id) to restrict access only to server members", "Server-ID (Guild-ID) angeben, um den Zugriff nur auf Servermitglieder zu beschränken");
		m.put("Proxy", "Proxy");
		m.put("Prune Builder Cache", "Builder-Cache bereinigen");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"Bild-Cache des Docker-Buildx-Builders bereinigen. Dieser Schritt ruft den Docker-Builder-Prune-Befehl auf, um den Cache des angegebenen Buildx-Builders im Server-Docker-Executor oder Remote-Docker-Executor zu entfernen");
		m.put("Public", "Öffentlich");
		m.put("Public Key", "Öffentlicher Schlüssel");
		m.put("Public Roles", "Öffentliche Rollen");
		m.put("Publish", "Veröffentlichen");
		m.put("Publish Coverage Report Step", "Schritt zur Veröffentlichung des Abdeckungsberichts");
		m.put("Publish Problem Report Step", "Schritt zur Veröffentlichung des Problemberichts");
		m.put("Publish Report Step", "Schritt zur Veröffentlichung des Berichts");
		m.put("Publish Unit Test Report Step", "Schritt zur Veröffentlichung des Unit-Test-Berichts");
		m.put("Published After", "Veröffentlicht nach");
		m.put("Published At", "Veröffentlicht am");
		m.put("Published Before", "Veröffentlicht vor");
		m.put("Published By", "Veröffentlicht von");
		m.put("Published By Project", "Veröffentlicht durch Projekt");
		m.put("Published By User", "Veröffentlicht durch Benutzer");
		m.put("Published File", "Veröffentlicht Datei");
		m.put("Pull Command", "Pull-Befehl");
		m.put("Pull Image", "Bild abrufen");
		m.put("Pull Request", "Pull-Request");
		m.put("Pull Request Branches", "Pull-Request-Zweige");
		m.put("Pull Request Description", "Pull-Request-Beschreibung");
		m.put("Pull Request Filter", "Pull-Request-Filter");
		m.put("Pull Request Management", "Pull-Request-Management");
		m.put("Pull Request Markdown Report", "Pull-Request-Markdown-Bericht");
		m.put("Pull Request Notification", "Pull-Request-Benachrichtigung");
		m.put("Pull Request Notification Template", "Pull-Request-Benachrichtigungsvorlage");
		m.put("Pull Request Notification Unsubscribed", "Pull-Request-Benachrichtigung abgemeldet");
		m.put("Pull Request Notification Unsubscribed Template", "Pull-Request-Benachrichtigung abgemeldete Vorlage");
		m.put("Pull Request Settings", "Pull-Request-Einstellungen");
		m.put("Pull Request Statistics", "Pull-Request-Statistiken");
		m.put("Pull Request Title", "Pull-Request-Titel");
		m.put("Pull Requests", "Pull-Requests");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Docker-Bild als OCI-Layout über Crane abrufen. Dieser Schritt muss vom Server-Docker-Executor, Remote-Docker-Executor oder Kubernetes-Executor ausgeführt werden");
		m.put("Pull from Remote", "Von Remote abrufen");
		m.put("Pull request", "Pull-Request");
		m.put("Pull request #{0} already closed", "Pull-Request #{0} bereits geschlossen");
		m.put("Pull request #{0} deleted", "Pull-Request #{0} gelöscht");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"Pull-Request-Administrationsberechtigung innerhalb eines Projekts, einschließlich Batch-Operationen über mehrere Pull-Requests");
		m.put("Pull request already closed", "Pull-Request bereits geschlossen");
		m.put("Pull request already opened", "Pull-Request bereits geöffnet");
		m.put("Pull request and code review", "Pull-Request und Code-Review");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not finished yet", 
			"Pull-Request kann jetzt nicht zusammengeführt werden, da <a class=\"more-info d-inline link-primary\">einige erforderliche Builds</a> noch nicht abgeschlossen sind");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not successful", 
			"Pull-Request kann jetzt nicht zusammengeführt werden, da <a class=\"more-info d-inline link-primary\">einige erforderliche Builds</a> nicht erfolgreich sind");
		m.put("Pull request can not be merged now as it is <a class=\"more-info d-inline link-primary\">pending review</a>", 
			"Pull-Request kann jetzt nicht zusammengeführt werden, da es <a class=\"more-info d-inline link-primary\">auf Überprüfung wartet</a>");
		m.put("Pull request can not be merged now as it was <a class=\"more-info d-inline link-primary\">requested for changes</a>", 
			"Pull-Request kann jetzt nicht zusammengeführt werden, da <a class=\"more-info d-inline link-primary\">Änderungen angefordert</a> wurden");
		m.put("Pull request can not be merged now as valid signature is required for head commit", 
			"Pull-Request kann jetzt nicht zusammengeführt werden, da eine gültige Signatur für den Head-Commit erforderlich ist");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "Pull-Request kann nur zusammengeführt werden, nachdem alle Gutachter ihre Zustimmung gegeben haben");
		m.put("Pull request can only be merged by users with code write permission", "Pull-Request kann nur von Benutzern mit Schreibberechtigung für den Code zusammengeführt werden");
		m.put("Pull request discard", "Pull-Request verwerfen");
		m.put("Pull request duration statistics", "Statistiken zur Dauer von Pull-Requests");
		m.put("Pull request frequency statistics", "Statistiken zur Häufigkeit von Pull-Requests");
		m.put("Pull request is discarded", "Pull-Request wird verworfen");
		m.put("Pull request is in error: {0}", "Pull-Request ist fehlerhaft: {0}");
		m.put("Pull request is merged", "Pull-Request wird zusammengeführt");
		m.put("Pull request is opened", "Pull-Request wird geöffnet");
		m.put("Pull request is still a work in progress", "Pull-Anfrage ist noch in Bearbeitung");
		m.put("Pull request is work in progress", "Pull-Anfrage ist in Bearbeitung");
		m.put("Pull request list", "Pull-Request-Liste");
		m.put("Pull request merge", "Pull-Request-Zusammenführung");
		m.put("Pull request not exist or access denied", "Pull-Request existiert nicht oder Zugriff verweigert");
		m.put("Pull request not merged", "Pull-Request nicht zusammengeführt");
		m.put("Pull request number", "Pull-Request-Nummer");
		m.put("Pull request open or update", "Pull-Request öffnen oder aktualisieren");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"Pull-Request-Abfrageüberwachung betrifft nur neue Pull-Requests. Um den Überwachungsstatus bestehender Pull-Requests in Batch zu verwalten, filtern Sie Pull-Requests nach Überwachungsstatus auf der Pull-Requests-Seite und führen Sie dann die entsprechende Aktion aus");
		m.put("Pull request settings updated", "Einstellungen für Pull-Requests aktualisiert");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Statistiken zu Pull-Requests sind eine Enterprise-Funktion. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("Pull request synchronization submitted", "Pull-Request-Synchronisierung eingereicht");
		m.put("Pull request will be merged automatically when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"Pull-Request wird automatisch zusammengeführt, wenn bereit. Diese Option wird deaktiviert, wenn neue Commits hinzugefügt, die Zusammenführungsstrategie geändert oder der Zielbranch gewechselt wird");
		m.put("Pull request will be merged automatically with a preset <a wicket:id=\"commitMessage\">commit message</a> when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"Pull-Request wird automatisch mit einer voreingestellten <a wicket:id=\"commitMessage\">Commit-Nachricht</a> zusammengeführt, wenn bereit. Diese Option wird deaktiviert, wenn neue Commits hinzugefügt, die Zusammenführungsstrategie geändert oder der Zielbranch gewechselt wird");
		m.put("Push Image", "Image pushen");
		m.put("Push chart to the repository", "Chart in das Repository pushen");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Docker-Image aus OCI-Layout über Crane pushen. Dieser Schritt muss vom Server-Docker-Executor, Remote-Docker-Executor oder Kubernetes-Executor ausgeführt werden");
		m.put("Push to Remote", "Auf Remote pushen");
		m.put("Push to container registry", "In Container-Registry pushen");
		m.put("PyPI(s)", "PyPI(s)");
		m.put("Pylint Report", "Pylint-Bericht");
		m.put("Queries", "Abfragen");
		m.put("Query", "Abfrage");
		m.put("Query Parameters", "Abfrageparameter");
		m.put("Query Watches", "Abfrageüberwachungen");
		m.put("Query commits", "Commits abfragen");
		m.put("Query not submitted", "Abfrage nicht eingereicht");
		m.put("Query param", "Abfrageparameter");
		m.put("Query/order agents", "Agenten abfragen/ordnen");
		m.put("Query/order builds", "Builds abfragen/ordnen");
		m.put("Query/order comments", "Kommentare abfragen/ordnen");
		m.put("Query/order issues", "Issues abfragen/ordnen");
		m.put("Query/order packages", "Pakete abfragen/ordnen");
		m.put("Query/order projects", "Projekte abfragen/ordnen");
		m.put("Query/order pull requests", "Pull-Requests abfragen/ordnen");
		m.put("Queueing Takes", "Warteschlangen-Dauer");
		m.put("Quick Search", "Schnellsuche");
		m.put("Quote", "Zitat");
		m.put("RESTful API", "RESTful API");
		m.put("RESTful API Help", "RESTful API-Hilfe");
		m.put("Ran On Agent", "Auf Agent ausgeführt");
		m.put("Re-run All Queried Builds", "Alle abgefragten Builds erneut ausführen");
		m.put("Re-run Selected Builds", "Ausgewählte Builds erneut ausführen");
		m.put("Re-run request submitted", "Anfrage zur erneuten Ausführung eingereicht");
		m.put("Re-run this build", "Diesen Build erneut ausführen");
		m.put("Read", "Lesen");
		m.put("Read body", "Body lesen");
		m.put("Readiness Check Command", "Befehl zur Bereitschaftsprüfung");
		m.put("Really want to delete this code comment?", "Möchten Sie diesen Code-Kommentar wirklich löschen?");
		m.put("Rebase", "Rebase");
		m.put("Rebase Source Branch Commits", "Commits des Quellbranches neu basieren");
		m.put("Rebase all commits from source branch onto target branch", "Alle Commits des Quellbranches auf den Zielbranch neu basieren");
		m.put("Rebase source branch commits", "Commits des Quellbranches neu basieren");
		m.put("Rebuild manually", "Manuell neu erstellen");
		m.put("Receive Posted Email", "Gesendete E-Mail empfangen");
		m.put("Received test mail", "Testmail empfangen");
		m.put("Receivers", "Empfänger");
		m.put("Recovery code", "Wiederherstellungscode");
		m.put("Recursive", "Rekursiv");
		m.put("Redundant", "Redundant");
		m.put("Ref", "Ref");
		m.put("Ref Name", "Ref-Name");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup", 
			"Siehe dieses <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>Tutorial</a> für ein Beispiel-Setup");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> for an example setup", 
			"Siehe dieses <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>Tutorial</a> für ein Beispiel-Setup");
		m.put("Reference", "Referenz");
		m.put("Reference Build", "Referenz-Build");
		m.put("Reference Issue", "Referenz-Issue");
		m.put("Reference Pull Request", "Referenz-Pull-Request");
		m.put("Reference this {0} in markdown or commit message via below string.", "Referenzieren Sie dieses {0} in Markdown oder Commit-Nachricht über den untenstehenden String.");
		m.put("Refresh", "Aktualisieren");
		m.put("Refresh Token", "Aktualisierungstoken");
		m.put("Refs", "Refs");
		m.put("Regenerate", "Neu generieren");
		m.put("Regenerate Private Key", "Privaten Schlüssel neu generieren");
		m.put("Regenerate this access token", "Diesen Zugriffstoken neu generieren");
		m.put("Registry Login", "Registry-Login");
		m.put("Registry Logins", "Registry-Logins");
		m.put("Registry Url", "Registry-URL");
		m.put("Regular Expression", "Regulärer Ausdruck");
		m.put("Remaining User Months", "Verbleibende Benutzer-Monate");
		m.put("Remaining User Months:", "Verbleibende Benutzer-Monate:");
		m.put("Remaining time", "Verbleibende Zeit");
		m.put("Remember Me", "Angemeldet bleiben");
		m.put("Remote Docker Executor", "Remote-Docker-Executor");
		m.put("Remote Machine", "Remote-Maschine");
		m.put("Remote Shell Executor", "Remote-Shell-Executor");
		m.put("Remote URL", "Remote-URL");
		m.put("Remote Url", "Remote-URL");
		m.put("Remove", "Entfernen");
		m.put("Remove All Queried Agents", "Alle abgefragten Agenten entfernen");
		m.put("Remove All Queried Users from Group", "Alle abgefragten Benutzer aus der Gruppe entfernen");
		m.put("Remove Fields", "Felder entfernen");
		m.put("Remove From Current Iteration", "Aus aktueller Iteration entfernen");
		m.put("Remove Selected Agents", "Ausgewählte Agenten entfernen");
		m.put("Remove Selected Users from Group", "Ausgewählte Benutzer aus der Gruppe entfernen");
		m.put("Remove from All Queried Groups", "Aus allen abgefragten Gruppen entfernen");
		m.put("Remove from Selected Groups", "Aus ausgewählten Gruppen entfernen");
		m.put("Remove from batch", "Aus Batch entfernen");
		m.put("Remove issue from this iteration", "Issue aus dieser Iteration entfernen");
		m.put("Remove this assignee", "Diesen Zuweiser entfernen");
		m.put("Remove this external participant from issue", "Diesen externen Teilnehmer aus Issue entfernen");
		m.put("Remove this file", "Diese Datei entfernen");
		m.put("Remove this image", "Entfernen Sie dieses Bild");
		m.put("Remove this reviewer", "Entfernen Sie diesen Prüfer");
		m.put("Removed all queried agents. Type <code>yes</code> below to confirm", "Alle abgefragten Agenten entfernt. Geben Sie unten <code>yes</code> ein, um zu bestätigen");
		m.put("Removed selected agents. Type <code>yes</code> below to confirm", "Ausgewählte Agenten entfernt. Geben Sie unten <code>yes</code> ein, um zu bestätigen");
		m.put("Rename {0}", "Umbenennen {0}");
		m.put("Renew Subscription", "Abonnement erneuern");
		m.put("Renovate CLI Options", "Renovate CLI-Optionen");
		m.put("Renovate JavaScript Config", "Renovate JavaScript-Konfiguration");
		m.put("Reopen", "Wieder öffnen");
		m.put("Reopen this iteration", "Diese Iteration wieder öffnen");
		m.put("Reopened pull request \"{0}\" ({1})", "Pull-Request \"{0}\" ({1}) wieder geöffnet");
		m.put("Replace With", "Ersetzen durch");
		m.put("Replica Count", "Replikanzahl");
		m.put("Replicas", "Replikate");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "Auf Kommentar zur Datei \"{0}\" im Projekt \"{1}\" geantwortet");
		m.put("Reply", "Antworten");
		m.put("Report Name", "Berichtsname");
		m.put("Report format changed. You may re-run this build to generate the report in new format", 
			"Berichtsformat geändert. Sie können diesen Build erneut ausführen, um den Bericht im neuen Format zu erstellen");
		m.put("Repository Sync", "Repository-Synchronisation");
		m.put("Request Body", "Anfrageinhalt");
		m.put("Request For Changes", "Änderungen anfordern");
		m.put("Request Scopes", "Anforderungsbereiche");
		m.put("Request Trial Subscription", "Testabonnement anfordern");
		m.put("Request review", "Überprüfung anfordern");
		m.put("Request to sync", "Synchronisation anfordern");
		m.put("Requested For changes", "Änderungen angefordert");
		m.put("Requested changes to pull request \"{0}\" ({1})", "Änderungen am Pull-Request \"{0}\" ({1}) angefordert");
		m.put("Requested for changes", "Änderungen angefordert");
		m.put("Requested to sync estimated/spent time", "Angefordert, geschätzte/aufgewendete Zeit zu synchronisieren");
		m.put("Require Autentication", "Authentifizierung erforderlich");
		m.put("Require Strict Pull Request Builds", "Strikte Pull-Request-Builds erforderlich");
		m.put("Require Successful", "Erfolgreich erforderlich");
		m.put("Required", "Erforderlich");
		m.put("Required Builds", "Erforderliche Builds");
		m.put("Required Reviewers", "Erforderliche Prüfer");
		m.put("Required Services", "Erforderliche Dienste");
		m.put("Resend Verification Email", "Bestätigungs-E-Mail erneut senden");
		m.put("Resend invitation", "Einladung erneut senden");
		m.put("Reset", "Zurücksetzen");
		m.put("Resolution", "Auflösung");
		m.put("Resolved", "Gelöst");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "Kommentar zur Datei \"{0}\" im Projekt \"{1}\" gelöst");
		m.put("Resource", "Ressource");
		m.put("Resource Settings", "Ressourceneinstellungen");
		m.put("Resources", "Ressourcen");
		m.put("Response", "Antwort");
		m.put("Response Body", "Antwortinhalt");
		m.put("Restart", "Neustart");
		m.put("Restart All Queried Agents", "Alle abgefragten Agenten neu starten");
		m.put("Restart Selected Agents", "Ausgewählte Agenten neu starten");
		m.put("Restart command issued", "Neustartbefehl ausgeführt");
		m.put("Restart command issued to all queried agents", "Neustartbefehl für alle abgefragten Agenten ausgeführt");
		m.put("Restart command issued to selected agents", "Neustartbefehl für ausgewählte Agenten ausgeführt");
		m.put("Restore Source Branch", "Quellzweig wiederherstellen");
		m.put("Restored source branch", "Quellzweig wiederhergestellt");
		m.put("Resubmitted manually", "Manuell erneut eingereicht");
		m.put("Resume", "Fortsetzen");
		m.put("Resume All Queried Agents", "Alle abgefragten Agenten fortsetzen");
		m.put("Resume Selected Agents", "Ausgewählte Agenten fortsetzen");
		m.put("Resumed all queried agents", "Alle abgefragten Agenten fortgesetzt");
		m.put("Resumed selected agents", "Ausgewählte Agenten fortgesetzt");
		m.put("Retried At", "Erneut versucht um");
		m.put("Retrieve Groups", "Gruppen abrufen");
		m.put("Retrieve LFS Files", "LFS-Dateien abrufen");
		m.put("Retrieve Submodules", "Submodule abrufen");
		m.put("Retry Condition", "Wiederholungsbedingung");
		m.put("Retry Delay", "Wiederholungsverzögerung");
		m.put("Revert", "Rückgängig machen");
		m.put("Reverted successfully", "Erfolgreich rückgängig gemacht");
		m.put("Review required for deletion. Submit pull request instead", "Überprüfung erforderlich für Löschung. Reichen Sie stattdessen einen Pull-Request ein");
		m.put("Review required for this change. Please submit pull request instead", "Überprüfung erforderlich für diese Änderung. Bitte Pull-Request einreichen.");
		m.put("Review required for this change. Submit pull request instead", "Überprüfung erforderlich für diese Änderung. Reichen Sie stattdessen einen Pull-Request ein");
		m.put("Reviewers", "Prüfer");
		m.put("Revision", "Revision");
		m.put("Revision indexing in progress...", "Revisionsindizierung läuft...");
		m.put("Revision indexing in progress... (symbol navigation in revisions will be accurate after indexed)", 
			"Revisionsindizierung läuft... (Symbolnavigation in Revisionen wird nach der Indizierung genau sein)");
		m.put("Right", "Rechts");
		m.put("Role", "Rolle");
		m.put("Role \"{0}\" deleted", "Rolle \"{0}\" gelöscht");
		m.put("Role \"{0}\" updated", "Rolle \"{0}\" aktualisiert");
		m.put("Role Management", "Rollenverwaltung");
		m.put("Role created", "Rolle erstellt");
		m.put("Roles", "Rollen");
		m.put("Root Projects", "Root-Projekte");
		m.put("Roslynator Report", "Roslynator-Bericht");
		m.put("RubyGems(s)", "RubyGems(s)");
		m.put("Ruff Report", "Ruff-Bericht");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "Regel wird angewendet, wenn der Benutzer, der das Tag bedient, die hier angegebenen Kriterien erfüllt");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"Regel wird nur angewendet, wenn der Benutzer, der den Zweig ändert, die hier angegebenen Kriterien erfüllt");
		m.put("Run As", "Ausführen als");
		m.put("Run Buildx Image Tools", "Buildx Image Tools ausführen");
		m.put("Run Docker Container", "Docker-Container ausführen");
		m.put("Run In Container", "Im Container ausführen");
		m.put("Run Integrity Check", "Integritätsprüfung ausführen");
		m.put("Run Job", "Job ausführen");
		m.put("Run Options", "Ausführungsoptionen");
		m.put("Run below commands from within your git repository:", "Führen Sie die folgenden Befehle innerhalb Ihres Git-Repositorys aus:");
		m.put("Run below commands to install this gem", "Führen Sie die folgenden Befehle aus, um dieses Gem zu installieren");
		m.put("Run below commands to install this package", "Führen Sie die folgenden Befehle aus, um dieses Paket zu installieren");
		m.put("Run below commands to use this chart", "Führen Sie die folgenden Befehle aus, um dieses Chart zu verwenden");
		m.put("Run below commands to use this package", "Führen Sie die folgenden Befehle aus, um dieses Paket zu verwenden");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"Führen Sie den Docker-Buildx-Imagetools-Befehl mit den angegebenen Argumenten aus. Dieser Schritt kann nur vom Server-Docker-Executor oder Remote-Docker-Executor ausgeführt werden");
		m.put("Run job", "Job ausführen");
		m.put("Run job in another project", "Job in einem anderen Projekt ausführen");
		m.put("Run on Bare Metal/Virtual Machine", "Auf Bare Metal/virtueller Maschine ausführen");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"Führen Sie den OSV-Scanner aus, um verletzte Lizenzen zu scannen, die von verschiedenen <a href='https://deps.dev/' target='_blank'>Abhängigkeiten</a> verwendet werden. Es kann nur von einem Docker-fähigen Executor ausgeführt werden.");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"Führen Sie den OSV-Scanner aus, um Schwachstellen in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>verschiedenen Sperrdateien</a> zu scannen. Es kann nur von einem Docker-fähigen Executor ausgeführt werden.");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"Führen Sie den angegebenen Docker-Container aus. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> wird in den Container eingebunden und sein Pfad wird in der Umgebungsvariable <code>ONEDEV_WORKSPACE</code> platziert. <b class='text-warning'>Hinweis: </b> Dieser Schritt kann nur vom Server-Docker-Executor oder Remote-Docker-Executor ausgeführt werden");
		m.put("Run specified step template", "Ausgewählte Schrittvorlage ausführen");
		m.put("Run this job", "Diesen Job ausführen");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by docker aware executor.", 
			"Führen Sie den Trivy-Container-Image-Scanner aus, um Probleme im angegebenen Image zu finden. Bei Schwachstellen überprüft er verschiedene <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>Distributionsdateien</a>. Er kann nur von einem Docker-bewussten Executor ausgeführt werden.");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"Führen Sie den Trivy-Dateisystem-Scanner aus, um verschiedene <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>Lock-Dateien</a> zu scannen. Er kann nur von einem Docker-fähigen Executor ausgeführt werden und sollte <span class='text-warning'>nach der Auflösung der Abhängigkeiten</span> (npm install oder ähnliches) ausgeführt werden. Im Vergleich zum OSV-Scanner ist die Einrichtung etwas ausführlicher, kann jedoch genauere Ergebnisse liefern.");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"Führen Sie den Trivy-Rootfs-Scanner aus, um verschiedene <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>Distributionsdateien</a> zu scannen. Er kann nur von einem Docker-fähigen Executor ausgeführt werden und sollte gegen den Staging-Bereich Ihres Projekts ausgeführt werden.");
		m.put("Run via Docker Container", "Ausführung über Docker-Container");
		m.put("Running", "Wird ausgeführt");
		m.put("Running Takes", "Ausführung dauert");
		m.put("SLOC on {0}", "SLOC auf {0}");
		m.put("SMTP Host", "SMTP-Host");
		m.put("SMTP Password", "SMTP-Passwort");
		m.put("SMTP User", "SMTP-Benutzer");
		m.put("SMTP/IMAP", "SMTP/IMAP");
		m.put("SSH", "SSH");
		m.put("SSH & GPG Keys", "SSH- & GPG-Schlüssel");
		m.put("SSH Clone URL", "SSH-Klon-URL");
		m.put("SSH Keys", "SSH-Schlüssel");
		m.put("SSH Root URL", "SSH-Root-URL");
		m.put("SSH Server Key", "SSH-Server-Schlüssel");
		m.put("SSH key deleted", "SSH-Schlüssel gelöscht");
		m.put("SSH settings have been saved and SSH server restarted", "SSH-Einstellungen wurden gespeichert und der SSH-Server neu gestartet");
		m.put("SSL Setting", "SSL-Einstellung");
		m.put("SSO Accounts", "SSO-Konten");
		m.put("SSO Providers", "SSO-Anbieter");
		m.put("SSO account deleted", "SSO-Konto gelöscht");
		m.put("SSO provider \"{0}\" deleted", "SSO-Anbieter \"{0}\" gelöscht");
		m.put("SSO provider created", "SSO-Anbieter erstellt");
		m.put("SSO provider updated", "SSO-Anbieter aktualisiert");
		m.put("SUCCESSFUL", "ERFOLGREICH");
		m.put("Save", "Speichern");
		m.put("Save Query", "Abfrage speichern");
		m.put("Save Query Bean", "Abfrage-Bean speichern");
		m.put("Save Settings", "Einstellungen speichern");
		m.put("Save Settings & Redistribute Projects", "Einstellungen speichern & Projekte neu verteilen");
		m.put("Save Template", "Vorlage speichern");
		m.put("Save as Mine", "Als meine speichern");
		m.put("Saved Queries", "Gespeicherte Abfragen");
		m.put("Scan Path", "Scan-Pfad");
		m.put("Scan Paths", "Scan-Pfade");
		m.put("Scan below QR code with your TOTP authenticators", "Scannen Sie den unten stehenden QR-Code mit Ihren TOTP-Authentifikatoren");
		m.put("Schedule Issues", "Probleme planen");
		m.put("Script Name", "Skriptname");
		m.put("Scripting Value", "Skriptwert");
		m.put("Search", "Suchen");
		m.put("Search For", "Suchen nach");
		m.put("Search Groups Using Filter", "Gruppen mit Filter suchen");
		m.put("Search branch", "Branch suchen");
		m.put("Search files, symbols and texts", "Dateien, Symbole und Texte durchsuchen");
		m.put("Search for", "Suchen nach");
		m.put("Search inside current tree", "Suche im aktuellen Baum");
		m.put("Search is too general", "Suche ist zu allgemein");
		m.put("Search job", "Job suchen");
		m.put("Search project", "Projekt suchen");
		m.put("Secret", "Geheimnis");
		m.put("Secret Config File", "Geheime Konfigurationsdatei");
		m.put("Secret Setting", "Geheime Einstellung");
		m.put("Security", "Sicherheit");
		m.put("Security & Compliance", "Sicherheit & Compliance");
		m.put("Security Setting", "Sicherheitseinstellung");
		m.put("Security Settings", "Sicherheitseinstellungen");
		m.put("Security settings have been updated", "Sicherheitseinstellungen wurden aktualisiert");
		m.put("Select", "Auswählen");
		m.put("Select Branch to Cherry Pick to", "Branch auswählen, zu dem Cherry-Pick erfolgen soll");
		m.put("Select Branch to Revert on", "Branch auswählen, auf dem zurückgesetzt werden soll");
		m.put("Select Branch/Tag", "Branch/Tag auswählen");
		m.put("Select Existing", "Vorhandenes auswählen");
		m.put("Select Job", "Job auswählen");
		m.put("Select Project", "Projekt auswählen");
		m.put("Select below...", "Unten auswählen...");
		m.put("Select iteration to schedule issues into", "Iteration auswählen, in die Probleme eingeplant werden sollen");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"Organisation auswählen, aus der importiert werden soll. Leer lassen, um aus Repositories unter dem aktuellen Konto zu importieren");
		m.put("Select project and revision first", "Zuerst Projekt und Revision auswählen");
		m.put("Select project first", "Zuerst Projekt auswählen");
		m.put("Select project to import from", "Projekt auswählen, aus dem importiert werden soll");
		m.put("Select project to sync to. Leave empty to sync to current project", "Projekt auswählen, mit dem synchronisiert werden soll. Leer lassen, um mit dem aktuellen Projekt zu synchronisieren");
		m.put("Select repository to import from", "Repository auswählen, aus dem importiert werden soll");
		m.put("Select users to send alert email upon events such as database auto-backup failure, cluster node unreachable etc", 
			"Benutzer auswählen, die bei Ereignissen wie Datenbank-Auto-Backup-Fehler, nicht erreichbarer Cluster-Knoten usw. eine Alarm-E-Mail erhalten sollen");
		m.put("Select workspace to import from", "Arbeitsbereich auswählen, aus dem importiert werden soll");
		m.put("Send Notifications", "Benachrichtigungen senden");
		m.put("Send Pull Request", "Pull-Request senden");
		m.put("Send notification", "Benachrichtigung senden");
		m.put("SendGrid", "SendGrid");
		m.put("Sendgrid Webhook Setting", "SendGrid-Webhook-Einstellung");
		m.put("Sending invitation to \"{0}\"...", "Einladung an \"{0}\" wird gesendet...");
		m.put("Sending test mail to {0}...", "Testmail wird an {0} gesendet...");
		m.put("Sequential Group", "Sequentielle Gruppe");
		m.put("Server", "Server");
		m.put("Server Docker Executor", "Server-Docker-Executor");
		m.put("Server Id", "Server-ID");
		m.put("Server Information", "Server-Informationen");
		m.put("Server Log", "Server-Log");
		m.put("Server Setup", "Server-Setup");
		m.put("Server Shell Executor", "Server-Shell-Executor");
		m.put("Server URL", "Server-URL");
		m.put("Server fingerprint", "Server-Fingerabdruck");
		m.put("Server host", "Server-Host");
		m.put("Server is Starting...", "Server wird gestartet...");
		m.put("Server url", "Server-URL");
		m.put("Service", "Dienst");
		m.put("Service Account", "Dienstkonto");
		m.put("Service Desk", "Service Desk");
		m.put("Service Desk Email Address", "Service-Desk-E-Mail-Adresse");
		m.put("Service Desk Issue Open Failed", "Service-Desk-Problem konnte nicht geöffnet werden");
		m.put("Service Desk Issue Open Failed Template", "Vorlage für fehlgeschlagenes Öffnen von Service-Desk-Problemen");
		m.put("Service Desk Issue Opened", "Service-Desk-Problem geöffnet");
		m.put("Service Desk Issue Opened Template", "Vorlage für geöffnetes Service-Desk-Problem");
		m.put("Service Desk Setting", "Service-Desk-Einstellung");
		m.put("Service Desk Setting Holder", "Service-Desk-Einstellungsinhaber");
		m.put("Service Desk Settings", "Service-Desk-Einstellungen");
		m.put("Service Locator", "Service-Locator");
		m.put("Service Locators", "Service-Locators");
		m.put("Service account not allowed to login", "Dienstkonto darf sich nicht anmelden");
		m.put("Service desk setting", "Service-Desk-Einstellung");
		m.put("Service desk settings have been saved", "Service-Desk-Einstellungen wurden gespeichert");
		m.put("Services", "Dienste");
		m.put("Session Timeout", "Sitzungs-Timeout");
		m.put("Set", "Festlegen");
		m.put("Set All Queried As Root Projects", "Alle abgefragten als Root-Projekte festlegen");
		m.put("Set All Queried Comments as Read", "Alle abgefragten Kommentare als gelesen markieren");
		m.put("Set All Queried Comments as Resolved", "Alle abgefragten Kommentare als gelöst markieren");
		m.put("Set All Queried Comments as Unresolved", "Alle abgefragten Kommentare als ungelöst markieren");
		m.put("Set All Queried Issues as Read", "Alle abgefragten Probleme als gelesen markieren");
		m.put("Set All Queried Pull Requests as Read", "Alle abgefragten Pull-Requests als gelesen markieren");
		m.put("Set As Primary", "Als primär festlegen");
		m.put("Set Build Description", "Build-Beschreibung festlegen");
		m.put("Set Build Version", "Build-Version festlegen");
		m.put("Set Resolved", "Als gelöst markieren");
		m.put("Set Selected As Root Projects", "Ausgewählte als Root-Projekte festlegen");
		m.put("Set Selected Comments as Resolved", "Ausgewählte Kommentare als gelöst markieren");
		m.put("Set Selected Comments as Unresolved", "Ausgewählte Kommentare als ungelöst markieren");
		m.put("Set Unresolved", "Als ungelöst markieren");
		m.put("Set Up Cache", "Cache einrichten");
		m.put("Set Up Renovate Cache", "Renovate-Cache einrichten");
		m.put("Set Up Trivy Cache", "Trivy-Cache einrichten");
		m.put("Set Up Your Account", "Richten Sie Ihr Konto ein");
		m.put("Set as Private", "Als privat festlegen");
		m.put("Set as Public", "Als öffentlich festlegen");
		m.put("Set description", "Beschreibung festlegen");
		m.put("Set reviewed", "Als überprüft markieren");
		m.put("Set unreviewed", "Als nicht überprüft markieren");
		m.put("Set up Microsoft Teams notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url. ", 
			"Microsoft Teams-Benachrichtigungseinstellungen einrichten. Einstellungen werden von untergeordneten Projekten geerbt und können durch Definition von Einstellungen mit derselben Webhook-URL überschrieben werden.");
		m.put("Set up discord notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Discord-Benachrichtigungseinstellungen einrichten. Einstellungen werden von untergeordneten Projekten geerbt und können durch Definition von Einstellungen mit derselben Webhook-URL überschrieben werden.");
		m.put("Set up job cache to speed up job execution. Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> on how to use job cache", 
			"Job-Cache einrichten, um die Ausführung von Jobs zu beschleunigen. Siehe <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>dieses Tutorial</a>, wie man den Job-Cache verwendet.");
		m.put("Set up ntfy.sh notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"ntfy.sh-Benachrichtigungseinstellungen einrichten. Einstellungen werden von untergeordneten Projekten geerbt und können durch Definition von Einstellungen mit derselben Webhook-URL überschrieben werden.");
		m.put("Set up slack notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Slack-Benachrichtigungseinstellungen einrichten. Einstellungen werden von untergeordneten Projekten geerbt und können durch Definition von Einstellungen mit derselben Webhook-URL überschrieben werden.");
		m.put("Set up two-factor authentication", "Zwei-Faktor-Authentifizierung einrichten");
		m.put("Setting", "Einstellung");
		m.put("Setting has been saved", "Einstellung wurde gespeichert");
		m.put("Settings", "Einstellungen");
		m.put("Settings and permissions of parent project will be inherited by this project", "Einstellungen und Berechtigungen des übergeordneten Projekts werden von diesem Projekt geerbt");
		m.put("Settings saved", "Einstellungen gespeichert");
		m.put("Settings saved and project redistribution scheduled", "Einstellungen gespeichert und Projektumverteilung geplant");
		m.put("Settings updated", "Einstellungen aktualisiert");
		m.put("Share dashboard", "Dashboard teilen");
		m.put("Share with Groups", "Mit Gruppen teilen");
		m.put("Share with Users", "Mit Benutzern teilen");
		m.put("Shell", "Shell");
		m.put("Show Archived", "Archivierte anzeigen");
		m.put("Show Branch/Tag", "Branch/Tag anzeigen");
		m.put("Show Build Status", "Build-Status anzeigen");
		m.put("Show Closed", "Geschlossene anzeigen");
		m.put("Show Code Stats", "Code-Statistiken anzeigen");
		m.put("Show Command", "Befehl anzeigen");
		m.put("Show Condition", "Bedingung anzeigen");
		m.put("Show Conditionally", "Bedingt anzeigen");
		m.put("Show Description", "Beschreibung anzeigen");
		m.put("Show Duration", "Dauer anzeigen");
		m.put("Show Emojis", "Emojis anzeigen");
		m.put("Show Error Detail", "Fehlerdetails anzeigen");
		m.put("Show Issue Status", "Problemstatus anzeigen");
		m.put("Show Package Stats", "Paketstatistiken anzeigen");
		m.put("Show Pull Request Stats", "Pull-Request-Statistiken anzeigen");
		m.put("Show Saved Queries", "Gespeicherte Abfragen anzeigen");
		m.put("Show States By", "Status nach anzeigen");
		m.put("Show Works Of", "Arbeiten von anzeigen");
		m.put("Show changes", "Änderungen anzeigen");
		m.put("Show commented code snippet", "Kommentierten Code-Schnipsel anzeigen");
		m.put("Show commit of this parent", "Commit dieses Elternteils anzeigen");
		m.put("Show emojis", "Emojis anzeigen");
		m.put("Show in build list", "In der Build-Liste anzeigen");
		m.put("Show issues in list", "Probleme in der Liste anzeigen");
		m.put("Show issues not scheduled into current iteration", "Probleme anzeigen, die nicht in die aktuelle Iteration eingeplant sind");
		m.put("Show matching agents", "Passende Agenten anzeigen");
		m.put("Show more", "Mehr anzeigen");
		m.put("Show more lines", "Mehr Zeilen anzeigen");
		m.put("Show next match", "Nächste Übereinstimmung anzeigen");
		m.put("Show previous match", "Vorherige Übereinstimmung anzeigen");
		m.put("Show test cases of this test suite", "Testfälle dieser Test-Suite anzeigen");
		m.put("Show total estimated/spent time", "Gesamte geschätzte/aufgewendete Zeit anzeigen");
		m.put("Showing first {0} files as there are too many", "Zeige die ersten {0} Dateien, da es zu viele gibt");
		m.put("Sign In", "Anmelden");
		m.put("Sign In To", "Anmelden bei");
		m.put("Sign Out", "Abmelden");
		m.put("Sign Up", "Registrieren");
		m.put("Sign Up Bean", "Registrierungs-Bean");
		m.put("Sign Up!", "Registrieren!");
		m.put("Sign in", "Anmelden");
		m.put("Signature required for this change, but no signing key is specified", "Signatur erforderlich für diese Änderung, aber kein Signaturschlüssel angegeben");
		m.put("Signature required for this change, please generate system GPG signing key first", "Signatur erforderlich für diese Änderung, bitte zuerst den System-GPG-Signierschlüssel generieren");
		m.put("Signature verified successfully with OneDev GPG key", "Signatur erfolgreich mit OneDev GPG-Schlüssel verifiziert");
		m.put("Signature verified successfully with committer's GPG key", "Signatur erfolgreich mit dem GPG-Schlüssel des Committers verifiziert");
		m.put("Signature verified successfully with committer's SSH key", "Signatur erfolgreich mit dem SSH-Schlüssel des Committers verifiziert");
		m.put("Signature verified successfully with tagger's GPG key", "Signatur erfolgreich mit dem GPG-Schlüssel des Taggers verifiziert");
		m.put("Signature verified successfully with tagger's SSH key", "Signatur erfolgreich mit dem SSH-Schlüssel des Taggers verifiziert");
		m.put("Signature verified successfully with trusted GPG key", "Signatur erfolgreich mit einem vertrauenswürdigen GPG-Schlüssel verifiziert");
		m.put("Signed with an unknown GPG key ", "Mit einem unbekannten GPG-Schlüssel signiert");
		m.put("Signed with an unknown ssh key", "Mit einem unbekannten SSH-Schlüssel signiert");
		m.put("Signer Email Addresses", "Signer-E-Mail-Adressen");
		m.put("Signing Key ID", "Signierschlüssel-ID");
		m.put("Similar Issues", "Ähnliche Probleme");
		m.put("Single Sign On", "Single Sign-On");
		m.put("Single Sign-On", "Single Sign-On");
		m.put("Single sign on via discord.com", "Single Sign-On über discord.com");
		m.put("Single sign on via twitch.tv", "Single Sign-On über twitch.tv");
		m.put("Site", "Website");
		m.put("Size", "Größe");
		m.put("Size invalid", "Ungültige Größe");
		m.put("Slack Notifications", "Slack-Benachrichtigungen");
		m.put("Smtp Ssl Setting", "Smtp-SSL-Einstellung");
		m.put("Smtp With Ssl", "Smtp mit SSL");
		m.put("Some builds are {0}", "Einige Builds sind {0}");
		m.put("Some jobs are hidden due to permission policy", "Einige Jobs sind aufgrund der Berechtigungsrichtlinie ausgeblendet");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "Jemand hat den Inhalt geändert, den Sie bearbeiten. Laden Sie die Seite neu und versuchen Sie es erneut.");
		m.put("Some other pull requests are opening to this branch", "Einige andere Pull-Requests werden für diesen Branch geöffnet");
		m.put("Some projects might be hidden due to permission policy", "Einige Projekte könnten aufgrund der Berechtigungsrichtlinie ausgeblendet sein");
		m.put("Some related commits of the code comment is missing", "Einige zugehörige Commits des Code-Kommentars fehlen");
		m.put("Some related commits of the pull request are missing", "Einige zugehörige Commits des Pull-Requests fehlen");
		m.put("Some required builds not passed", "Einige erforderliche Builds sind nicht bestanden");
		m.put("Someone made below change since you started editing", "Jemand hat die folgende Änderung vorgenommen, seit Sie mit der Bearbeitung begonnen haben");
		m.put("Sort", "Sortieren");
		m.put("Source", "Quelle");
		m.put("Source Docker Image", "Quell-Docker-Image");
		m.put("Source Lines", "Quellzeilen");
		m.put("Source Path", "Quellpfad");
		m.put("Source branch already exists", "Quellbranch existiert bereits");
		m.put("Source branch already merged into target branch", "Quellbranch wurde bereits in Zielbranch zusammengeführt");
		m.put("Source branch commits will be rebased onto target branch", "Commits des Quellbranches werden auf den Zielbranch umgebased");
		m.put("Source branch is default branch", "Quellbranch ist der Standardbranch");
		m.put("Source branch is outdated", "Quellbranch ist veraltet");
		m.put("Source branch no longer exists", "Quellbranch existiert nicht mehr");
		m.put("Source branch updated successfully", "Quellbranch wurde erfolgreich aktualisiert");
		m.put("Source project no longer exists", "Quellprojekt existiert nicht mehr");
		m.put("Specified Value", "Angegebener Wert");
		m.put("Specified choices", "Angegebene Auswahlmöglichkeiten");
		m.put("Specified default value", "Angegebener Standardwert");
		m.put("Specified fields", "Angegebene Felder");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Gibt die LDAP-URL des Active Directory-Servers an, z. B. <i>ldap://ad-server</i> oder <i>ldaps://ad-server</i>. Falls Ihr LDAP-Server ein selbstsigniertes Zertifikat für die LDAPS-Verbindung verwendet, müssen Sie <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>OneDev konfigurieren, um das Zertifikat zu vertrauen</a>");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Gibt die LDAP-URL an, z. B. <i>ldap://localhost</i> oder <i>ldaps://localhost</i>. Falls Ihr LDAP-Server ein selbstsigniertes Zertifikat für die LDAPS-Verbindung verwendet, müssen Sie <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>OneDev konfigurieren, um das Zertifikat zu vertrauen</a>");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"Gibt Basisknoten für die Benutzersuche an. Zum Beispiel: <i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"Gibt den Namen des Attributs innerhalb des Benutzer-LDAP-Eintrags an, dessen Wert die Distinguished Names der zugehörigen Gruppen enthält. Beispielsweise verwenden einige LDAP-Server das Attribut <i>memberOf</i>, um Gruppen aufzulisten");
		m.put("Specifies password of above manager DN", "Gibt das Passwort des oben genannten Manager-DN an");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"Gibt das Attribut an, das den Gruppennamen innerhalb des gefundenen Gruppen-LDAP-Eintrags enthält. Der Wert dieses Attributs wird einer OneDev-Gruppe zugeordnet. Dieses Attribut wird normalerweise auf <i>cn</i> gesetzt");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"Gibt die relative .net TRX-Testergebnisdatei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>TestResults/*.trx</tt>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, dessen Wert ein Zugriffstoken mit Schreibberechtigung für Code über die oben genannten Projekte ist. Commits, Issues und Pull-Requests werden auch unter dem Namen des Zugriffstoken-Besitzers erstellt");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"Gibt die relative <a href='https://github.com/rust-lang/rust-clippy'>Rust Clippy</a>-JSON-Ausgabedatei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Diese Datei kann mit der Clippy-JSON-Ausgabeoption generiert werden, z. B. <code>cargo clippy --message-format json>check-result.json</code>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify Build Options", "Gibt Build-Optionen an");
		m.put("Specify CPD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match", 
			"Gibt die relative CPD-Ergebnis-XML-Datei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>target/cpd.xml</tt>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify Commit Message", "Gibt die Commit-Nachricht an");
		m.put("Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. Use * or ? for pattern match", 
			"Gibt die ESLint-Berichtsdatei im Checkstyle-Format unter <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Diese Datei kann mit der ESLint-Option <tt>'-f checkstyle'</tt> und <tt>'-o'</tt> generiert werden. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify GitHub API url, for instance <tt>https://api.github.com</tt>", "Gibt die GitHub-API-URL an, z. B. <tt>https://api.github.com</tt>");
		m.put("Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>", "Gibt die GitLab-API-URL an, z. B. <tt>https://gitlab.example.com/api/v4</tt>");
		m.put("Specify Gitea API url, for instance <tt>https://gitea.example.com/api/v1</tt>", "Gibt die Gitea-API-URL an, z. B. <tt>https://gitea.example.com/api/v1</tt>");
		m.put("Specify GoogleTest XML result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This report can be generated with environment variable <tt>GTEST_OUTPUT</tt> when running tests, For instance, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * or ? for pattern match", 
			"Gibt die relative GoogleTest-XML-Ergebnisdatei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Dieser Bericht kann mit der Umgebungsvariable <tt>GTEST_OUTPUT</tt> beim Testen generiert werden, z. B. <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify IMAP user name.<br><b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system email address specified above", 
			"Gibt den IMAP-Benutzernamen an.<br><b class='text-danger'>HINWEIS: </b> Dieses Konto sollte in der Lage sein, E-Mails zu empfangen, die an die oben angegebene System-E-Mail-Adresse gesendet werden");
		m.put("Specify JUnit test result file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match", 
			"Gibt die JUnit-Testergebnisdatei im XML-Format relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>target/surefire-reports/TEST-*.xml</tt>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match", 
			"Gibt die relative JaCoCo-Abdeckungs-XML-Berichtsdatei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>target/site/jacoco/jacoco.xml</tt>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify Jest coverage report file in clover format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. Use * or ? for pattern match", 
			"Gibt die relative Jest-Abdeckungs-Berichtsdatei im Clover-Format zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>coverage/clover.xml</tt>. Diese Datei kann mit der Jest-Option <tt>'--coverage'</tt> generiert werden. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify Jest test result file in json format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match", 
			"Gibt die relative Jest-Testergebnisdatei im JSON-Format zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Diese Datei kann über die Jest-Option <tt>'--json'</tt> und <tt>'--outputFile'</tt> generiert werden. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify OCI layout directory of the image to scan. This directory can be generated via build image step or pull image step. It should be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Gibt das OCI-Layout-Verzeichnis des zu scannenden Images an. Dieses Verzeichnis kann über den Build-Image-Schritt oder den Pull-Image-Schritt generiert werden. Es sollte relativ zum <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> sein");
		m.put("Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from", 
			"Gibt das relative OCI-Layout-Verzeichnis zum <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> an, von dem aus gepusht werden soll");
		m.put("Specify OpenID scopes to request", "Gibt die OpenID-Bereiche an, die angefordert werden sollen");
		m.put("Specify PMD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/pmd.xml</tt>. Use * or ? for pattern match", 
			"Gibt die relative PMD-Ergebnis-XML-Datei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>target/pmd.xml</tt>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify PowerShell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of your script<br>", 
			"Gibt die PowerShell-Befehle an, die unter dem <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> ausgeführt werden sollen.<br><b class='text-warning'>HINWEIS: </b> OneDev überprüft den Exit-Code des Skripts, um festzustellen, ob der Schritt erfolgreich ist. Da PowerShell immer mit 0 beendet wird, auch wenn Skriptfehler vorliegen, sollten Sie Fehler im Skript behandeln und mit einem Nicht-Null-Code beenden oder die Zeile <code>$ErrorActionPreference = &quot;Stop&quot;</code> am Anfang Ihres Skripts hinzufügen<br>");
		m.put("Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with <i>-o</i> option. Use * or ? for pattern match", 
			"Gibt die relative Roslynator-Diagnose-Ausgabedatei im XML-Format zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Diese Datei kann mit der Option <i>-o</i> generiert werden. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify Shell/Batch Commands to Run", "Gibt Shell-/Batch-Befehle zum Ausführen an");
		m.put("Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match", 
			"Gibt die relative SpotBugs-Ergebnis-XML-Datei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>target/spotbugsXml.xml</tt>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify System Settings", "Gibt Systemeinstellungen an");
		m.put("Specify URL of remote git repository. Only http/https protocol is supported", "Gibt die URL des Remote-Git-Repositorys an. Es wird nur das http/https-Protokoll unterstützt");
		m.put("Specify YouTrack login name. This account should have permission to:<ul><li>Read full information and issues of the projects you want to import<li>Read issue tags<li>Read user basic information</ul>", 
			"Gibt den YouTrack-Anmeldenamen an. Dieses Konto sollte die Berechtigung haben:<ul><li>Vollständige Informationen und Issues der Projekte, die Sie importieren möchten, lesen<li>Issue-Tags lesen<li>Grundlegende Benutzerinformationen lesen</ul>");
		m.put("Specify YouTrack password or access token for above user", "Gibt das YouTrack-Passwort oder Zugriffstoken für den oben genannten Benutzer an");
		m.put("Specify a &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regular expression&lt;/a&gt; to match issue references. For instance:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;", 
			"Gibt einen &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regulären Ausdruck&lt;/a&gt; an, um Issue-Referenzen zu finden. Zum Beispiel:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> after issue number", 
			"Gibt einen <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regulären Ausdruck</a> nach der Issue-Nummer an");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> before issue number", 
			"Gibt einen <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regulären Ausdruck</a> vor der Issue-Nummer an");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, das als privater SSH-Schlüssel verwendet werden soll");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, das als Zugriffstoken verwendet werden soll");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, das als Zugriffstoken verwendet werden soll, um die Build-Spezifikation aus dem oben genannten Projekt zu importieren, falls dessen Code nicht öffentlich zugänglich ist");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, das als Passwort oder Zugriffstoken des Registrierungsdienstes verwendet werden soll");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, das als Passwort oder Zugriffstoken für den Zugriff auf das Remote-Repository verwendet werden soll");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, das als privater Schlüssel für die SSH-Authentifizierung verwendet werden soll. <b class='text-info'>HINWEIS:</b> Privater Schlüssel mit Passphrase wird nicht unterstützt");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, das als privater Schlüssel des oben genannten Benutzers für die SSH-Authentifizierung verwendet werden soll. <b class='text-info'>HINWEIS:</b> Privater Schlüssel mit Passphrase wird nicht unterstützt");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, dessen Wert ein Zugriffstoken mit Verwaltungsberechtigung für das oben genannte Projekt ist. Beachten Sie, dass das Zugriffstoken nicht erforderlich ist, wenn die Synchronisierung mit dem aktuellen oder einem untergeordneten Projekt erfolgt und der Build-Commit vom Standardbranch erreichbar ist");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Gibt ein <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>Job-Geheimnis</a> an, dessen Wert ein Zugriffstoken mit Berechtigung zum Hochladen von Cache für das oben genannte Projekt ist. Beachten Sie, dass diese Eigenschaft nicht erforderlich ist, wenn der Cache in das aktuelle oder ein untergeordnetes Projekt hochgeladen wird und der Build-Commit vom Standardbranch erreichbar ist");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"Gibt einen <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>Cron-Zeitplan</a> an, um den Job automatisch auszulösen. <b class='text-info'>Hinweis:</b> Um Ressourcen zu sparen, werden Sekunden in der Cron-Ausdruck ignoriert, und das minimale Zeitplanintervall beträgt eine Minute");
		m.put("Specify a Docker Image to Test Against", "Gibt ein Docker-Image an, gegen das getestet werden soll");
		m.put("Specify a custom field of Enum type", "Gibt ein benutzerdefiniertes Feld vom Typ Enum an");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "Gibt eine Standardabfrage an, um behobene Issues der angegebenen Jobs zu filtern/zu sortieren");
		m.put("Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into", 
			"Gibt eine Datei relativ zum <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> an, in die die Prüfsumme geschrieben werden soll");
		m.put("Specify a multi-value user field to hold assignees information.<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Gibt ein mehrwertiges Benutzerfeld an, um Informationen zu den Zuweisungen zu speichern.<b>HINWEIS: </b> Sie können die OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Specify a multi-value user field to hold assignees information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Gibt ein mehrwertiges Benutzerfeld an, um Informationen zu den Zuweisungen zu speichern.<br><b>HINWEIS: </b> Sie können die OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Specify a path inside container to be used as mount target", "Gibt einen Pfad innerhalb des Containers an, der als Mount-Ziel verwendet werden soll");
		m.put("Specify a path relative to job workspace to be used as mount source. Leave empty to mount job workspace itself", 
			"Gibt einen Pfad relativ zum Job-Arbeitsbereich an, der als Mount-Quelle verwendet werden soll. Lassen Sie das Feld leer, um den Job-Arbeitsbereich selbst zu mounten");
		m.put("Specify a secret to be used as access token to create issue in above project if it is not publicly accessible", 
			"Gibt ein Geheimnis an, das als Zugriffstoken verwendet werden soll, um ein Issue im oben genannten Projekt zu erstellen, falls es nicht öffentlich zugänglich ist");
		m.put("Specify a secret to be used as access token to retrieve artifacts from above project. If not specified, project artifacts will be accessed anonymously", 
			"Gibt ein Geheimnis an, das als Zugriffstoken verwendet werden soll, um Artefakte aus dem oben genannten Projekt abzurufen. Falls nicht angegeben, werden Projektartefakte anonym abgerufen");
		m.put("Specify a secret to be used as access token to trigger job in above project", "Geben Sie ein Geheimnis an, das als Zugriffstoken verwendet werden soll, um den Job im obigen Projekt auszulösen");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Gibt ein Geheimnis an, dessen Wert ein Zugriffstoken mit Berechtigung zum Hochladen von Cache für das oben genannte Projekt ist. Beachten Sie, dass diese Eigenschaft nicht erforderlich ist, wenn der Cache in das aktuelle oder ein untergeordnetes Projekt hochgeladen wird und der Build-Commit vom Standardbranch erreichbar ist");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"Gibt den absoluten Pfad zur Konfigurationsdatei an, die von kubectl verwendet wird, um auf den Cluster zuzugreifen. Lassen Sie das Feld leer, damit kubectl die Cluster-Zugriffsinformationen automatisch bestimmt");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"Gibt den absoluten Pfad zum kubectl-Dienstprogramm an, z. B. <i>/usr/bin/kubectl</i>. Falls leer gelassen, versucht OneDev, das Dienstprogramm aus dem Systempfad zu finden");
		m.put("Specify account name to login to Gmail to send/receive email", "Gibt den Kontonamen an, um sich bei Gmail anzumelden, um E-Mails zu senden/empfangen");
		m.put("Specify additional users able to access this confidential issue besides those granted via role. Users mentioned in the issue will be authorized automatically", 
			"Gibt zusätzliche Benutzer an, die auf dieses vertrauliche Issue zugreifen können, zusätzlich zu denjenigen, die über die Rolle berechtigt sind. Benutzer, die im Issue erwähnt werden, werden automatisch autorisiert");
		m.put("Specify agents applicable for this executor", "Gibt Agenten an, die für diesen Executor geeignet sind");
		m.put("Specify allowed <a href='https://spdx.org/licenses/' target='_blank'>spdx license identifiers</a> <span class='text-warning'>separated by comma</span>", 
			"Gibt erlaubte <a href='https://spdx.org/licenses/' target='_blank'>SPDX-Lizenzkennungen</a> <span class='text-warning'>durch Komma getrennt</span> an");
		m.put("Specify an email address sharing same inbox as the system email address in mail setting definition. Emails sent to this address will be created as issues in this project. The default value takes form of <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>", 
			"Gibt eine E-Mail-Adresse an, die denselben Posteingang wie die System-E-Mail-Adresse in der Mail-Einstellung-Definition teilt. E-Mails, die an diese Adresse gesendet werden, werden als Issues in diesem Projekt erstellt. Der Standardwert hat die Form <tt>&lt;System-E-Mail-Adresse-Name&gt;+&lt;Projektpfad&gt;@&lt;System-E-Mail-Adresse-Domain&gt;</tt>");
		m.put("Specify applicable projects for above option. Multiple projects should be separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Gibt anwendbare Projekte für die oben genannte Option an. Mehrere Projekte sollten durch Leerzeichen getrennt werden. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Mit '-' vorangestellt, um auszuschließen. Lassen Sie das Feld leer für alle Projekte");
		m.put("Specify applicable projects separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Gibt anwendbare Projekte durch Leerzeichen getrennt an. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Mit '-' vorangestellt, um auszuschließen. Lassen Sie das Feld leer für alle Projekte");
		m.put("Specify application (client) ID of the app registered in Entra ID", "Gibt die Anwendungs-(Client)-ID der in Entra ID registrierten App an");
		m.put("Specify arguments for imagetools. For instance <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>", 
			"Gibt Argumente für Imagetools an. Zum Beispiel <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>");
		m.put("Specify artifacts to retrieve into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved.", 
			"Gibt Artefakte an, die in den <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> abgerufen werden sollen. Nur veröffentlichte Artefakte (über den Artefakt-Veröffentlichungsschritt) können abgerufen werden");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"Gibt mindestens 10 alphanumerische Zeichen an, die als Geheimnis verwendet werden sollen, und fügt dann einen eingehenden Parse-Eintrag auf der SendGrid-Seite hinzu:<ul><li><code>Destination URL</code> sollte auf <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i> gesetzt werden, z. B. <i>https://onedev.example.com/~sendgrid/1234567890</i>. Beachten Sie, dass in einer Produktionsumgebung <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>HTTPS aktiviert sein sollte</a>, um das Geheimnis zu schützen</li><li><code>Receiving domain</code> sollte mit dem Domain-Teil der oben angegebenen System-E-Mail-Adresse übereinstimmen</li><li>Option <code>POST the raw, full MIME message</code> ist aktiviert</li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"Gibt Basisknoten für die Benutzersuche an. Zum Beispiel: <i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "Gibt den Branch an, um die vorgeschlagene Änderung zu committen");
		m.put("Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Geben Sie den Branch an, gegen den der Job ausgeführt werden soll. Entweder Branch oder Tag kann angegeben werden, aber nicht beides. Der Standard-Branch wird verwendet, wenn beides nicht angegeben ist");
		m.put("Specify branch, tag or commit in above project to import build spec from", "Gibt den Branch, Tag oder Commit im oben genannten Projekt an, um die Build-Spezifikation zu importieren");
		m.put("Specify by Build Number", "Gibt nach Build-Nummer an");
		m.put("Specify cache upload strategy after build successful. <var>Upload If Not Hit</var> means to upload when cache is not found with cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files in cache path are changed", 
			"Gibt die Cache-Upload-Strategie nach erfolgreichem Build an. <var>Upload If Not Hit</var> bedeutet, hochzuladen, wenn der Cache mit dem Cache-Schlüssel (nicht Lade-Schlüssel) nicht gefunden wird, und <var>Upload If Changed</var> bedeutet, hochzuladen, wenn sich einige Dateien im Cache-Pfad geändert haben");
		m.put("Specify certificate to trust if you are using self-signed certificate for remote repository", 
			"Gibt das Zertifikat an, dem vertraut werden soll, falls Sie ein selbstsigniertes Zertifikat für das Remote-Repository verwenden");
		m.put("Specify certificates to trust if you are using self-signed certificates for your docker registries", 
			"Gibt Zertifikate an, denen vertraut werden soll, falls Sie selbstsignierte Zertifikate für Ihre Docker-Registrierungen verwenden");
		m.put("Specify checkstyle result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/checkstyle-result.xml</tt>. Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> on how to generate the result xml file. Use * or ? for pattern match", 
			"Gibt die relative Checkstyle-Ergebnis-XML-Datei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>target/checkstyle-result.xml</tt>. Siehe <a href='https://checkstyle.org/'>Checkstyle-Dokumentation</a>, wie die Ergebnis-XML-Datei generiert wird. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify client secret of the app registered in Entra ID", "Gibt das Client-Geheimnis der in Entra ID registrierten App an");
		m.put("Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/clover/clover.xml</tt>. Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> on how to generate clover xml file. Use * or ? for pattern match", 
			"Gibt die relative Clover-Abdeckungs-XML-Berichtsdatei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>target/site/clover/clover.xml</tt>. Siehe <a href='https://openclover.org/documentation'>OpenClover-Dokumentation</a>, wie die Clover-XML-Datei generiert wird. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match", 
			"Gibt die relative Cobertura-Abdeckungs-XML-Berichtsdatei zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, z. B. <tt>target/site/cobertura/coverage.xml</tt>. Verwenden Sie * oder ? für Musterabgleich");
		m.put("Specify color of the state for displaying purpose", "Gibt die Farbe des Status für Anzeigezwecke an");
		m.put("Specify columns of the board. Each column corresponds to a value of the issue field specified above", 
			"Gibt die Spalten des Boards an. Jede Spalte entspricht einem Wert des oben angegebenen Issue-Felds");
		m.put("Specify command to check readiness of the service. This command will be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be executed repeatedly until a zero code is returned to indicate service ready", 
			"Gibt den Befehl an, um die Bereitschaft des Dienstes zu überprüfen. Dieser Befehl wird unter Windows-Images von cmd.exe und unter Linux-Images von Shell interpretiert. Er wird wiederholt ausgeführt, bis ein Null-Code zurückgegeben wird, um anzuzeigen, dass der Dienst bereit ist");
		m.put("Specify commands to be executed on remote machine. <b class='text-warning'>Note:</b> user environments will not be picked up when execute these commands, set up them explicitly in commands if necessary", 
			"Geben Sie Befehle an, die auf der Remote-Maschine ausgeführt werden sollen. <b class='text-warning'>Hinweis:</b> Benutzerumgebungen werden beim Ausführen dieser Befehle nicht übernommen, richten Sie sie bei Bedarf explizit in den Befehlen ein.");
		m.put("Specify condition to retry build upon failure", "Geben Sie eine Bedingung an, um den Build bei einem Fehler erneut zu versuchen.");
		m.put("Specify configuration discovery url of your OpenID provider, for instance: <code>https://openid.example.com/.well-known/openid-configuration</code>. Make sure to use HTTPS protocol as OneDev relies on TLS encryption to ensure token validity", 
			"Geben Sie die Konfigurations-Discovery-URL Ihres OpenID-Anbieters an, beispielsweise: <code>https://openid.example.com/.well-known/openid-configuration</code>. Stellen Sie sicher, dass Sie das HTTPS-Protokoll verwenden, da OneDev auf TLS-Verschlüsselung angewiesen ist, um die Token-Gültigkeit zu gewährleisten.");
		m.put("Specify container image to execute commands inside", "Geben Sie ein Container-Image an, um Befehle darin auszuführen.");
		m.put("Specify container image to run", "Geben Sie ein Container-Image zum Ausführen an.");
		m.put("Specify cppcheck xml result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match", 
			"Geben Sie die cppcheck-XML-Ergebnisdatei relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Diese Datei kann mit der cppcheck-XML-Ausgabeoption generiert werden, beispielsweise <code>cppcheck src --xml 2>check-result.xml</code>. Verwenden Sie * oder ? für Musterabgleich.");
		m.put("Specify cpu request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Geben Sie die CPU-Anforderung für jeden Job/Dienst an, der diesen Executor verwendet. Weitere Informationen finden Sie unter <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes-Ressourcenmanagement</a>.");
		m.put("Specify default assignees of pull requests submitted to this project. Only users with the write code permission to the project can be selected", 
			"Geben Sie die Standardzuweisungen für Pull-Requests an, die an dieses Projekt übermittelt werden. Es können nur Benutzer ausgewählt werden, die über die Berechtigung zum Schreiben von Code für das Projekt verfügen.");
		m.put("Specify default merge strategy of pull requests submitted to this project", "Geben Sie die Standard-Merge-Strategie für Pull-Requests an, die an dieses Projekt übermittelt werden.");
		m.put("Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple destinations should be separated with space", 
			"Geben Sie Ziele an, beispielsweise <tt>registry-server:5000/myorg/myrepo:latest</tt>. Stellen Sie sicher, dass Sie <b>denselben Host</b> verwenden, wie in der Server-URL der Systemeinstellungen angegeben, wenn Sie in das integrierte Registry pushen möchten, oder verwenden Sie einfach die Form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Mehrere Ziele sollten durch Leerzeichen getrennt werden.");
		m.put("Specify directory (tenant) ID of the app registered in Entra ID", "Geben Sie die Verzeichnis-(Mandanten-)ID der in Entra ID registrierten App an.");
		m.put("Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Geben Sie ein Verzeichnis relativ zum <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> an, um das OCI-Layout zu speichern.");
		m.put("Specify docker image of the service", "Geben Sie das Docker-Image des Dienstes an.");
		m.put("Specify dockerx builder used to build docker image. OneDev will create the builder automatically if it does not exist. Check <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> on how to customize the builder for instance to allow publishing to insecure registries", 
			"Geben Sie den dockerx-Builder an, der zum Erstellen des Docker-Images verwendet wird. OneDev erstellt den Builder automatisch, wenn er nicht existiert. Weitere Informationen zur Anpassung des Builders, beispielsweise um das Veröffentlichen in unsicheren Registries zu ermöglichen, finden Sie in <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>diesem Tutorial</a>.");
		m.put("Specify email addresses to send invitations, with one per line", "Geben Sie E-Mail-Adressen an, um Einladungen zu senden, jeweils eine pro Zeile.");
		m.put("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\"", 
			"Geben Sie die geschätzte Zeit <b class='text-warning'>nur für dieses Problem</b> an, ohne \"{0}\" zu berücksichtigen.");
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"Geben Sie Felder für verschiedene Probleme an, die von Renovate erstellt wurden, um die Abhängigkeitsaktualisierung zu orchestrieren.");
		m.put("Specify fields to be displayed in the issue list", "Geben Sie die Felder an, die in der Problemliste angezeigt werden sollen.");
		m.put("Specify fields to display in board card", "Geben Sie die Felder an, die auf der Board-Karte angezeigt werden sollen.");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"Geben Sie Dateien relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, die veröffentlicht werden sollen. Verwenden Sie * oder ? für Musterabgleich.");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Geben Sie Dateien an, aus denen eine MD5-Prüfsumme erstellt werden soll. Mehrere Dateien sollten durch Leerzeichen getrennt werden. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a>-Muster werden akzeptiert. Nicht absolute Dateien gelten als relativ zum <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a>.");
		m.put("Specify files under above directory to be published", "Geben Sie Dateien im obigen Verzeichnis an, die veröffentlicht werden sollen");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be included in these files to be served as site start page", 
			"Geben Sie Dateien unter dem oben genannten Verzeichnis an, die veröffentlicht werden sollen. Verwenden Sie * oder ? für Musterabgleich. <b>HINWEIS:</b> <code>index.html</code> sollte in diesen Dateien enthalten sein, um als Startseite der Website zu dienen.");
		m.put("Specify group to import from. Leave empty to import from projects under current account", 
			"Geben Sie die Gruppe an, aus der importiert werden soll. Lassen Sie das Feld leer, um aus Projekten unter dem aktuellen Konto zu importieren.");
		m.put("Specify how to map GitHub issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Geben Sie an, wie GitHub-Issue-Labels auf OneDev-Benutzerdefinierte Felder abgebildet werden sollen.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify how to map GitLab issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Geben Sie an, wie GitLab-Issue-Labels auf OneDev-Benutzerdefinierte Felder abgebildet werden sollen.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify how to map Gitea issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Geben Sie an, wie Gitea-Issue-Labels auf OneDev-Benutzerdefinierte Felder abgebildet werden sollen.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify how to map JIRA issue priorities to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Geben Sie an, wie JIRA-Issue-Prioritäten auf OneDev-Benutzerdefinierte Felder abgebildet werden sollen.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify how to map JIRA issue statuses to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Geben Sie an, wie JIRA-Issue-Status auf OneDev-Benutzerdefinierte Felder abgebildet werden sollen.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Zustände anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify how to map JIRA issue types to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Geben Sie an, wie JIRA-Issue-Typen auf OneDev-Benutzerdefinierte Felder abgebildet werden sollen.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify how to map YouTrack issue fields to OneDev. Unmapped fields will be reflected in issue description.<br><b>Note: </b><ul><li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, for instance <tt>Priority::Critical</tt><li>You may customize OneDev issue fields in case there is no appropriate option here</ul>", 
			"Geben Sie an, wie YouTrack-Issue-Felder auf OneDev abgebildet werden sollen. Nicht abgebildete Felder werden in der Problembeschreibung reflektiert.<br><b>Hinweis: </b><ul><li>Enum-Felder müssen in der Form <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt> abgebildet werden, beispielsweise <tt>Priority::Critical</tt><li>Sie können OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist</ul>.");
		m.put("Specify how to map YouTrack issue links to OneDev issue links.<br><b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here", 
			"Geben Sie an, wie YouTrack-Issue-Links auf OneDev-Issue-Links abgebildet werden sollen.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Links anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify how to map YouTrack issue state to OneDev issue state. Unmapped states will use the initial state in OneDev.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Geben Sie an, wie YouTrack-Issue-Zustände auf OneDev-Issue-Zustände abgebildet werden sollen. Nicht abgebildete Zustände verwenden den Anfangszustand in OneDev.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Zustände anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify how to map YouTrack issue tags to OneDev issue custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Geben Sie an, wie YouTrack-Issue-Tags auf OneDev-Benutzerdefinierte Felder abgebildet werden sollen.<br><b>HINWEIS: </b> Sie können OneDev-Issue-Felder anpassen, falls hier keine geeignete Option vorhanden ist.");
		m.put("Specify image on the login button", "Geben Sie das Bild auf der Login-Schaltfläche an.");
		m.put("Specify image tag to pull from, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to pull from built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Geben Sie den Image-Tag an, der abgerufen werden soll, beispielsweise <tt>registry-server:5000/myorg/myrepo:latest</tt>. Stellen Sie sicher, dass Sie <b>denselben Host</b> verwenden, wie in der Server-URL der Systemeinstellungen angegeben, wenn Sie aus dem integrierten Registry abrufen möchten, oder verwenden Sie einfach die Form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>.");
		m.put("Specify image tag to push to, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Geben Sie den Image-Tag an, der gepusht werden soll, beispielsweise <tt>registry-server:5000/myorg/myrepo:latest</tt>. Stellen Sie sicher, dass Sie <b>denselben Host</b> verwenden, wie in der Server-URL der Systemeinstellungen angegeben, wenn Sie in das integrierte Registry pushen möchten, oder verwenden Sie einfach die Form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>.");
		m.put("Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple tags should be separated with space", 
			"Geben Sie die Image-Tags an, die gepusht werden sollen, beispielsweise <tt>registry-server:5000/myorg/myrepo:latest</tt>. Stellen Sie sicher, dass Sie <b>denselben Host</b> verwenden, wie in der Server-URL der Systemeinstellungen angegeben, wenn Sie in das integrierte Registry pushen möchten, oder verwenden Sie einfach die Form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Mehrere Tags sollten durch Leerzeichen getrennt werden.");
		m.put("Specify import option", "Geben Sie die Importoption an.");
		m.put("Specify incoming email poll interval in seconds", "Geben Sie das Intervall für das Abrufen eingehender E-Mails in Sekunden an.");
		m.put("Specify issue creation settings. For a particular sender and project, the first matching entry will take effect. Issue creation will be disallowed if no matching entry found", 
			"Geben Sie die Einstellungen zur Problemerstellung an. Für einen bestimmten Absender und ein Projekt wird der erste übereinstimmende Eintrag wirksam. Die Problemerstellung wird verweigert, wenn kein übereinstimmender Eintrag gefunden wird.");
		m.put("Specify issue field to identify different columns of the board. Only state and single-valued enumeration field can be used here", 
			"Geben Sie das Issue-Feld an, um verschiedene Spalten des Boards zu identifizieren. Es können nur Zustands- und Einzelwert-Enum-Felder verwendet werden.");
		m.put("Specify links to be displayed in the issue list", "Geben Sie Links an, die in der Problemliste angezeigt werden sollen.");
		m.put("Specify links to display in board card", "Geben Sie Links an, die auf der Board-Karte angezeigt werden sollen.");
		m.put("Specify manager DN to authenticate OneDev itself to Active Directory. The manager DN should be specified in form of <i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>manager@example.com</i>", 
			"Geben Sie den Manager-DN an, um OneDev selbst gegenüber Active Directory zu authentifizieren. Der Manager-DN sollte in der Form <i>&lt;account name&gt;@&lt;domain&gt;</i> angegeben werden, beispielsweise: <i>manager@example.com</i>.");
		m.put("Specify manager DN to authenticate OneDev itself to LDAP server", "Geben Sie den Manager-DN an, um OneDev selbst gegenüber dem LDAP-Server zu authentifizieren.");
		m.put("Specify markdown file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published", 
			"Geben Sie die Markdown-Datei relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, die veröffentlicht werden soll.");
		m.put("Specify max git LFS file size in mega bytes", "Geben Sie die maximale Git-LFS-Dateigröße in Megabyte an.");
		m.put("Specify max number of CPU intensive tasks the server can run concurrently, such as Git repository pull/push, repository index, etc.", 
			"Geben Sie die maximale Anzahl von CPU-intensiven Aufgaben an, die der Server gleichzeitig ausführen kann, wie das Abrufen/Pushen von Git-Repositories, Repository-Indexierung usw.");
		m.put("Specify max number of jobs this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Geben Sie die maximale Anzahl von Jobs an, die dieser Executor gleichzeitig auf jedem übereinstimmenden Agent ausführen kann. Lassen Sie das Feld leer, um es als Agent-CPU-Kerne festzulegen.");
		m.put("Specify max number of jobs this executor can run concurrently. Leave empty to set as CPU cores", 
			"Geben Sie die maximale Anzahl von Jobs an, die dieser Executor gleichzeitig ausführen kann. Lassen Sie das Feld leer, um es als CPU-Kerne festzulegen.");
		m.put("Specify max number of jobs/services this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Geben Sie die maximale Anzahl von Jobs/Diensten an, die dieser Executor gleichzeitig auf jedem übereinstimmenden Agent ausführen kann. Lassen Sie das Feld leer, um es als Agent-CPU-Kerne festzulegen.");
		m.put("Specify max number of jobs/services this executor can run concurrently. Leave empty to set as CPU cores", 
			"Geben Sie die maximale Anzahl von Jobs/Diensten an, die dieser Executor gleichzeitig ausführen kann. Lassen Sie das Feld leer, um es als CPU-Kerne festzulegen.");
		m.put("Specify max size of uploaded file in mega bytes via web interface. This applies to file uploaded to repository, markdown content (issue comment etc), and build artifacts", 
			"Geben Sie die maximale Größe der hochgeladenen Datei in Megabyte über die Weboberfläche an. Dies gilt für Dateien, die in das Repository hochgeladen werden, Markdown-Inhalte (Issue-Kommentare usw.) und Build-Artefakte.");
		m.put("Specify memory request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Geben Sie die Speicheranforderung für jeden Job/Dienst an, der diesen Executor verwendet. Weitere Informationen finden Sie unter <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes-Ressourcenmanagement</a>.");
		m.put("Specify mypy output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated by redirecting mypy output <b>without option '--pretty'</b>, for instance <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * or ? for pattern match", 
			"Geben Sie die mypy-Ausgabedatei relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Diese Datei kann durch Umleitung der mypy-Ausgabe <b>ohne die Option '--pretty'</b> generiert werden, beispielsweise <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Verwenden Sie * oder ? für Musterabgleich.");
		m.put("Specify name of the branch", "Geben Sie den Namen des Branches an.");
		m.put("Specify name of the environment variable", "Geben Sie den Namen der Umgebungsvariable an.");
		m.put("Specify name of the iteration", "Geben Sie den Namen der Iteration an.");
		m.put("Specify name of the job", "Geben Sie den Namen des Jobs an.");
		m.put("Specify name of the report to be displayed in build detail page", "Geben Sie den Namen des Berichts an, der auf der Build-Detailseite angezeigt werden soll.");
		m.put("Specify name of the saved query", "Geben Sie den Namen der gespeicherten Abfrage an.");
		m.put("Specify name of the service, which will be used as host name to access the service", 
			"Geben Sie den Namen des Dienstes an, der als Hostname verwendet wird, um auf den Dienst zuzugreifen.");
		m.put("Specify name of the tag", "Geben Sie den Namen des Tags an.");
		m.put("Specify network timeout in seconds when authenticate through this system", "Geben Sie die Netzwerk-Timeout-Zeit in Sekunden an, wenn Sie sich über dieses System authentifizieren.");
		m.put("Specify node selector of this locator", "Geben Sie den Node-Selector dieses Locators an.");
		m.put("Specify password or access token of specified registry", "Geben Sie das Passwort oder den Zugriffstoken des angegebenen Registrys an.");
		m.put("Specify password to authenticate with", "Geben Sie das Passwort zur Authentifizierung an.");
		m.put("Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>", "Geben Sie den Pfad zur curl-Ausführungsdatei an, beispielsweise: <tt>/usr/bin/curl</tt>.");
		m.put("Specify path to git executable, for instance: <tt>/usr/bin/git</tt>", "Geben Sie den Pfad zur git-Ausführungsdatei an, beispielsweise: <tt>/usr/bin/git</tt>.");
		m.put("Specify powershell executable to be used", "Geben Sie die zu verwendende PowerShell-Ausführungsdatei an.");
		m.put("Specify project to import build spec from", "Geben Sie das Projekt an, aus dem die Build-Spezifikation importiert werden soll.");
		m.put("Specify project to import into at OneDev side", "Geben Sie das Projekt an, in das auf der OneDev-Seite importiert werden soll.");
		m.put("Specify project to retrieve artifacts from", "Geben Sie das Projekt an, aus dem Artefakte abgerufen werden sollen.");
		m.put("Specify project to run job in", "Geben Sie das Projekt an, in dem der Job ausgeführt werden soll");
		m.put("Specify projects", "Geben Sie Projekte an.");
		m.put("Specify projects to update dependencies. Leave empty for current project", "Geben Sie Projekte an, deren Abhängigkeiten aktualisiert werden sollen. Lassen Sie das Feld leer für das aktuelle Projekt.");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Geben Sie die pylint-JSON-Ergebnisdatei relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Diese Datei kann mit der pylint-JSON-Ausgabeformatoption generiert werden, beispielsweise <code>--exit-zero --output-format=json:pylint-result.json</code>. Beachten Sie, dass wir den pylint-Befehl bei Verstößen nicht fehlschlagen lassen, da dieser Schritt den Build basierend auf der konfigurierten Schwelle fehlschlagen lässt. Verwenden Sie * oder ? für Musterabgleich.");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"Geben Sie Registry-Logins an, falls erforderlich. Für das integrierte Registry verwenden Sie <code>@server_url@</code> für die Registry-URL, <code>@job_token@</code> für den Benutzernamen und den Zugriffstoken für das Passwort.");
		m.put("Specify registry url. Leave empty for official registry", "Geben Sie die Registry-URL an. Lassen Sie das Feld leer für das offizielle Registry.");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Geben Sie den relativen Pfad unter <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> an, um das OCI-Layout zu speichern.");
		m.put("Specify repositories", "Geben Sie Repositories an.");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"Geben Sie die erforderlichen Reviewer an, falls der angegebene Pfad geändert wird. Beachten Sie, dass der Benutzer, der die Änderung übermittelt, automatisch als Reviewer der Änderung gilt.");
		m.put("Specify root URL to access this server", "Geben Sie die Root-URL an, um auf diesen Server zuzugreifen.");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Geben Sie die ruff-JSON-Ergebnisdatei relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an. Diese Datei kann mit der ruff-JSON-Ausgabeformatoption generiert werden, beispielsweise <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Beachten Sie, dass wir den ruff-Befehl bei Verstößen nicht fehlschlagen lassen, da dieser Schritt den Build basierend auf der konfigurierten Schwelle fehlschlagen lässt. Verwenden Sie * oder ? für Musterabgleich.");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Geben Sie Shell-Befehle (auf Linux/Unix) oder Batch-Befehle (auf Windows) an, die unter dem <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> ausgeführt werden sollen.");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Geben Sie Shell-Befehle an, die unter dem <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a> ausgeführt werden sollen.");
		m.put("Specify shell to be used", "Geben Sie die zu verwendende Shell an.");
		m.put("Specify source param for SCP command, for instance <code>app.tar.gz</code>", "Geben Sie die Quellparameter für den SCP-Befehl an, beispielsweise <code>app.tar.gz</code>.");
		m.put("Specify space separated refs to pull from remote. '*' can be used in ref name for wildcard match<br><b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update branches/tags via this step", 
			"Geben Sie durch Leerzeichen getrennte Refs an, die vom Remote abgerufen werden sollen. '*' kann im Ref-Namen für Wildcard-Abgleich verwendet werden.<br><b class='text-danger'>HINWEIS:</b> Die Regel zum Schutz von Branches/Tags wird ignoriert, wenn Branches/Tags über diesen Schritt aktualisiert werden.");
		m.put("Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Geben Sie durch Leerzeichen getrennte Branches an, die geschützt werden sollen. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-', um auszuschließen.");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Geben Sie durch Leerzeichen getrennte Jobs an. Verwenden Sie '*' oder '?' für Wildcard-Abgleich. Präfix mit '-', um auszuschließen.");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts will be granted implicitly in matched jobs even if no other permissions are specified here", 
			"Geben Sie durch Leerzeichen getrennte Jobs an. Verwenden Sie '*' oder '?' für Wildcard-Abgleich. Präfix mit '-', um auszuschließen. <b class='text-danger'>HINWEIS: </b> Die Berechtigung zum Zugriff auf Build-Artefakte wird implizit in übereinstimmenden Jobs gewährt, auch wenn hier keine anderen Berechtigungen angegeben sind.");
		m.put("Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Geben Sie durch Leerzeichen getrennte Pfade an, die geschützt werden sollen. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-', um auszuschließen.");
		m.put("Specify space-separated projects applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Geben Sie durch Leerzeichen getrennte Projekte an, die für diesen Eintrag gelten. Verwenden Sie '*' oder '?' für Wildcard-Abgleich. Präfix mit '-', um auszuschließen. Lassen Sie das Feld leer, um alle Projekte zuzuordnen.");
		m.put("Specify space-separated sender email addresses applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all senders", 
			"Geben Sie durch Leerzeichen getrennte Absender-E-Mail-Adressen an, die für diesen Eintrag gelten. Verwenden Sie '*' oder '?' für Wildcard-Abgleich. Präfix mit '-', um auszuschließen. Lassen Sie das Feld leer, um alle Absender zuzuordnen.");
		m.put("Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Geben Sie durch Leerzeichen getrennte Tags an, die geschützt werden sollen. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Abgleich</a>. Präfix mit '-', um auszuschließen.");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>", 
			"Geben Sie die Startseite des Berichts relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, beispielsweise: <tt>manual/index.md</tt>.");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"Geben Sie die Startseite des Berichts relativ zum <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a> an, beispielsweise: api/index.html.");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"Geben Sie die Speichergröße an, die für das Build-Volume angefordert werden soll. Die Größe sollte dem <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes-Ressourcenkapazitätsformat</a> entsprechen, beispielsweise <i>10Gi</i>.");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"Geben Sie die Tab-Breite an, die zur Berechnung des Spaltenwerts der gefundenen Probleme im bereitgestellten Bericht verwendet wird.");
		m.put("Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Geben Sie das Tag an, gegen das der Job ausgeführt werden soll. Entweder Branch oder Tag kann angegeben werden, aber nicht beides. Der Standard-Branch wird verwendet, wenn beides nicht angegeben ist");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"Geben Sie das Zielparameter für den SCP-Befehl an, zum Beispiel <code>user@@host:/app</code>. <b class='text-info'>HINWEIS:</b> Stellen Sie sicher, dass der SCP-Befehl auf dem Remote-Host installiert ist");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"Geben Sie den Text an, der mit den übereinstimmenden Problemreferenzen ersetzt werden soll, zum Beispiel: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Hier repräsentieren $1 und $2 die Erfassungsgruppen im Beispielproblem-Muster (siehe Hilfe zum Problem-Muster)");
		m.put("Specify the condition current build must satisfy to execute this action", "Geben Sie die Bedingung an, die der aktuelle Build erfüllen muss, um diese Aktion auszuführen");
		m.put("Specify the condition preserved builds must match", "Geben Sie die Bedingung an, die erhaltene Builds erfüllen müssen");
		m.put("Specify the private key (in PEM format) used by SSH server to establish connections with client", 
			"Geben Sie den privaten Schlüssel (im PEM-Format) an, der vom SSH-Server verwendet wird, um Verbindungen mit dem Client herzustellen");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"Geben Sie die Strategie an, um Informationen zur Gruppenmitgliedschaft abzurufen. Um einer LDAP-Gruppe entsprechende Berechtigungen zu erteilen, sollte eine OneDev-Gruppe mit demselben Namen definiert werden. Verwenden Sie die Strategie <tt>Gruppen nicht abrufen</tt>, wenn Sie die Gruppenmitgliedschaften auf der OneDev-Seite verwalten möchten");
		m.put("Specify timeout in seconds when communicating with mail server", "Geben Sie das Timeout in Sekunden an, wenn mit dem Mailserver kommuniziert wird");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "Geben Sie das Timeout in Sekunden an. Es zählt ab dem Zeitpunkt, an dem der Job eingereicht wird");
		m.put("Specify title of the issue", "Geben Sie den Titel des Problems an");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "Geben Sie die URL der YouTrack-API an. Zum Beispiel <tt>http://localhost:8080/api</tt>");
		m.put("Specify user name of above machine for SSH authentication", "Geben Sie den Benutzernamen der oben genannten Maschine für die SSH-Authentifizierung an");
		m.put("Specify user name of specified registry", "Geben Sie den Benutzernamen des angegebenen Registrierungsdienstes an");
		m.put("Specify user name of the registry", "Geben Sie den Benutzernamen der Registrierung an");
		m.put("Specify user name to authenticate with", "Geben Sie den Benutzernamen zur Authentifizierung an");
		m.put("Specify value of the environment variable", "Geben Sie den Wert der Umgebungsvariablen an");
		m.put("Specify web UI session timeout in minutes. Existing sessions will not be affected after changing this value.", 
			"Geben Sie das Timeout für die Web-UI-Sitzung in Minuten an. Bestehende Sitzungen werden nach Änderung dieses Wertes nicht beeinflusst.");
		m.put("Specify webhook url to post events", "Geben Sie die Webhook-URL an, um Ereignisse zu posten");
		m.put("Specify which issue state to use for closed GitHub issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Geben Sie den Problemstatus an, der für geschlossene GitHub-Probleme verwendet werden soll.<br><b>HINWEIS: </b> Sie können die OneDev-Problemstatus anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Specify which issue state to use for closed GitLab issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Geben Sie den Problemstatus an, der für geschlossene GitLab-Probleme verwendet werden soll.<br><b>HINWEIS: </b> Sie können die OneDev-Problemstatus anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Specify which issue state to use for closed Gitea issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Geben Sie den Problemstatus an, der für geschlossene Gitea-Probleme verwendet werden soll.<br><b>HINWEIS: </b> Sie können die OneDev-Problemstatus anpassen, falls hier keine geeignete Option vorhanden ist");
		m.put("Specify which states are considered as closed for various issues created by Renovate to orchestrate dependency update. Additionally, when Renovate closes the issue, OneDev will transit the issue to first state specified here", 
			"Geben Sie an, welche Status als geschlossen für verschiedene Probleme betrachtet werden, die von Renovate zur Orchestrierung von Abhängigkeitsaktualisierungen erstellt wurden. Zusätzlich wird OneDev das Problem in den ersten hier angegebenen Status versetzen, wenn Renovate das Problem schließt");
		m.put("Specify working days per week. This will affect parsing and displaying of working periods. For instance <tt>1w</tt> is the same as <tt>5d</tt> if this property is set to <tt>5</tt>", 
			"Geben Sie die Arbeitstage pro Woche an. Dies wirkt sich auf das Parsen und Anzeigen von Arbeitsperioden aus. Zum Beispiel ist <tt>1w</tt> dasselbe wie <tt>5d</tt>, wenn diese Eigenschaft auf <tt>5</tt> gesetzt ist");
		m.put("Specify working hours per day. This will affect parsing and displaying of working periods. For instance <tt>1d</tt> is the same as <tt>8h</tt> if this property is set to <tt>8</tt>", 
			"Geben Sie die Arbeitsstunden pro Tag an. Dies wirkt sich auf das Parsen und Anzeigen von Arbeitsperioden aus. Zum Beispiel ist <tt>1d</tt> dasselbe wie <tt>8h</tt>, wenn diese Eigenschaft auf <tt>8</tt> gesetzt ist");
		m.put("Spent", "Verbraucht");
		m.put("Spent Time", "Verbrauchte Zeit");
		m.put("Spent Time Issue Field", "Feld für verbrauchte Zeit im Problem");
		m.put("Spent Time:", "Verbrauchte Zeit:");
		m.put("Spent time / estimated time", "Verbrauchte Zeit / geschätzte Zeit");
		m.put("Split", "Teilen");
		m.put("Split view", "Geteilte Ansicht");
		m.put("SpotBugs Report", "SpotBugs-Bericht");
		m.put("Squash Source Branch Commits", "Quellbranch-Commits zusammenführen");
		m.put("Squash all commits from source branch into a single commit in target branch", "Alle Commits aus dem Quellbranch zu einem einzigen Commit im Zielbranch zusammenführen");
		m.put("Squash source branch commits", "Quellbranch-Commits zusammenführen");
		m.put("Ssh", "Ssh");
		m.put("Ssh Key", "Ssh-Schlüssel");
		m.put("Ssh Setting", "Ssh-Einstellung");
		m.put("Ssl Setting", "Ssl-Einstellung");
		m.put("Sso Connector", "Sso-Connector");
		m.put("Sso Provider Bean", "Sso-Anbieter-Bean");
		m.put("Start At", "Starten um");
		m.put("Start Date", "Startdatum");
		m.put("Start Page", "Startseite");
		m.put("Start agent on remote Linux machine by running below command:", "Starten Sie den Agenten auf einer Remote-Linux-Maschine, indem Sie den folgenden Befehl ausführen:");
		m.put("Start date", "Startdatum");
		m.put("Start to watch once I am involved", "Beginnen Sie zu beobachten, sobald ich involviert bin");
		m.put("Start work", "Arbeit beginnen");
		m.put("Start/Due Date", "Start-/Fälligkeitsdatum");
		m.put("State", "Status");
		m.put("State Durations", "Status-Dauern");
		m.put("State Frequencies", "Status-Häufigkeiten");
		m.put("State Spec", "Status-Spezifikation");
		m.put("State Transitions", "Status-Übergänge");
		m.put("State Trends", "Status-Trends");
		m.put("State of an issue is transited", "Der Status eines Problems wird übertragen");
		m.put("States", "Status");
		m.put("Statistics", "Statistiken");
		m.put("Stats", "Statistiken");
		m.put("Stats Group", "Statistikgruppe");
		m.put("Status", "Status");
		m.put("Status Code", "Statuscode");
		m.put("Status code", "Statuscode");
		m.put("Status code other than 200 indicating the error type", "Statuscode ungleich 200, der den Fehlertyp angibt");
		m.put("Step", "Schritt");
		m.put("Step Template", "Schrittvorlage");
		m.put("Step Templates", "Schrittvorlagen");
		m.put("Step {0} of {1}: ", "Schritt {0} von {1}:");
		m.put("Steps", "Schritte");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"Schritte werden seriell auf demselben Knoten ausgeführt und teilen sich denselben <a href='https://docs.onedev.io/concepts#job-workspace'>Job-Arbeitsbereich</a>");
		m.put("Stop work", "Arbeit beenden");
		m.put("Stopwatch Overdue", "Stoppuhr überfällig");
		m.put("Storage Settings", "Speichereinstellungen");
		m.put("Storage file missing", "Speicherdatei fehlt");
		m.put("Storage not found", "Speicher nicht gefunden");
		m.put("Stored with Git LFS", "Mit Git LFS gespeichert");
		m.put("Sub Keys", "Untergeordnete Schlüssel");
		m.put("Subject", "Betreff");
		m.put("Submit", "Einreichen");
		m.put("Submit Reason", "Einreichungsgrund");
		m.put("Submit Support Request", "Supportanfrage einreichen");
		m.put("Submitted After", "Eingereicht nach");
		m.put("Submitted At", "Eingereicht um");
		m.put("Submitted Before", "Eingereicht vor");
		m.put("Submitted By", "Eingereicht von");
		m.put("Submitted manually", "Manuell eingereicht");
		m.put("Submitter", "Einreicher");
		m.put("Subscription Key", "Abonnement-Schlüssel");
		m.put("Subscription Management", "Abonnement-Verwaltung");
		m.put("Subscription data", "Abonnement-Daten");
		m.put("Subscription key installed successfully", "Abonnement-Schlüssel erfolgreich installiert");
		m.put("Subscription key not applicable: this key is intended to activate a trial subscription", 
			"Abonnement-Schlüssel nicht anwendbar: Dieser Schlüssel ist dazu gedacht, ein Testabonnement zu aktivieren");
		m.put("Subscription key not applicable: this key is intended to renew a user based subscription", 
			"Abonnementschlüssel nicht anwendbar: Dieser Schlüssel ist zur Erneuerung eines benutzerbasierten Abonnements gedacht");
		m.put("Subscription key not applicable: this key is intended to renew an unlimited users subscription", 
			"Abonnementschlüssel nicht anwendbar: Dieser Schlüssel ist zur Erneuerung eines unbegrenzten Benutzerabonnements gedacht");
		m.put("Subscription key not applicable: this key is intended to update licensee of an existing subscription", 
			"Abonnement-Schlüssel nicht anwendbar: Dieser Schlüssel ist dazu gedacht, den Lizenznehmer eines bestehenden Abonnements zu aktualisieren");
		m.put("Success Rate", "Erfolgsrate");
		m.put("Successful", "Erfolgreich");
		m.put("Suffix Pattern", "Suffix-Muster");
		m.put("Suggest changes", "Änderungen vorschlagen");
		m.put("Suggested change", "Vorgeschlagene Änderung");
		m.put("Suggestion is outdated either due to code change or pull request close", "Vorschlag ist veraltet, entweder aufgrund von Codeänderungen oder Schließung der Pull-Anfrage");
		m.put("Suggestions", "Vorschläge");
		m.put("Summary", "Zusammenfassung");
		m.put("Support & Bug Report", "Support & Fehlerbericht");
		m.put("Support Request", "Supportanfrage");
		m.put("Swap", "Wechseln");
		m.put("Switch to HTTP(S)", "Zu HTTP(S) wechseln");
		m.put("Switch to SSH", "Zu SSH wechseln");
		m.put("Symbol Name", "Symbolname");
		m.put("Symbol name", "Symbolname");
		m.put("Symbols", "Symbole");
		m.put("Sync Replica Status and Back to Home", "Synchronisieren Sie den Replikatstatus und kehren Sie zur Startseite zurück");
		m.put("Sync Repository", "Repository synchronisieren");
		m.put("Sync Timing of All Queried Issues", "Synchronisierungszeitpunkt aller abgefragten Issues");
		m.put("Sync Timing of Selected Issues", "Synchronisierungszeitpunkt ausgewählter Issues");
		m.put("Sync requested. Please check status after a while", "Synchronisierung angefordert. Bitte überprüfen Sie den Status nach einer Weile");
		m.put("Synchronize", "Synchronisieren");
		m.put("System", "System");
		m.put("System Alert", "Systemwarnung");
		m.put("System Alert Template", "Systemwarnungsvorlage");
		m.put("System Date", "Systemdatum");
		m.put("System Email Address", "System-E-Mail-Adresse");
		m.put("System Maintenance", "Systemwartung");
		m.put("System Setting", "Systemeinstellung");
		m.put("System Settings", "Systemeinstellungen");
		m.put("System email address defined in mail setting should be used as recipient of such email, and project name may be appended to this address using to indicate where to create issues. For instance, if system email address is specified as <tt>support@example.com</tt>, sending email to <tt>support+myproject@example.com</tt> will create issue in <tt>myproject</tt>. If project name is not appended, OneDev will look up the project using project designation information below", 
			"Die in den Mail-Einstellungen definierte System-E-Mail-Adresse sollte als Empfänger solcher E-Mails verwendet werden, und der Projektname kann an diese Adresse angehängt werden, um anzugeben, wo Issues erstellt werden sollen. Wenn beispielsweise die System-E-Mail-Adresse als <tt>support@example.com</tt> angegeben ist, wird durch das Senden einer E-Mail an <tt>support+myproject@example.com</tt> ein Issue in <tt>myproject</tt> erstellt. Wenn der Projektname nicht angehängt wird, sucht OneDev das Projekt anhand der unten angegebenen Projektbezeichnungsinformationen.");
		m.put("System settings have been saved", "Systemeinstellungen wurden gespeichert");
		m.put("System uuid", "System-UUID");
		m.put("TIMED_OUT", "Zeitüberschreitung");
		m.put("TRX Report (.net unit test)", "TRX-Bericht (.net Unit-Test)");
		m.put("Tab Width", "Tabulatorbreite");
		m.put("Tag", "Tag");
		m.put("Tag \"{0}\" already exists, please choose a different name", "Tag \"{0}\" existiert bereits, bitte wählen Sie einen anderen Namen");
		m.put("Tag \"{0}\" already exists, please choose a different name.", "Tag \"{0}\" existiert bereits, bitte wählen Sie einen anderen Namen.");
		m.put("Tag \"{0}\" created", "Tag \"{0}\" erstellt");
		m.put("Tag \"{0}\" deleted", "Tag \"{0}\" gelöscht");
		m.put("Tag Message", "Tag-Nachricht");
		m.put("Tag Name", "Tag-Name");
		m.put("Tag Protection", "Tag-Schutz");
		m.put("Tag creation", "Tag-Erstellung");
		m.put("Tags", "Tags");
		m.put("Target", "Ziel");
		m.put("Target Branches", "Ziel-Branches");
		m.put("Target Docker Image", "Ziel-Docker-Image");
		m.put("Target File", "Zieldatei");
		m.put("Target Path", "Zielpfad");
		m.put("Target Project", "Zielprojekt");
		m.put("Target branch no longer exists", "Ziel-Branch existiert nicht mehr");
		m.put("Target branch was fast-forwarded to source branch", "Ziel-Branch wurde auf den Quell-Branch vorwärtsgeführt");
		m.put("Target branch will be fast-forwarded to source branch", "Ziel-Branch wird auf den Quell-Branch vorwärtsgeführt");
		m.put("Target containing spaces or starting with dash needs to be quoted", "Ziel mit Leerzeichen oder beginnend mit einem Bindestrich muss in Anführungszeichen gesetzt werden");
		m.put("Target or source branch is updated. Please try again", "Ziel- oder Quell-Branch wurde aktualisiert. Bitte versuchen Sie es erneut");
		m.put("Task List", "Aufgabenliste");
		m.put("Task list", "Aufgabenliste");
		m.put("Tell user to reset password", "Benutzer auffordern, das Passwort zurückzusetzen");
		m.put("Template Name", "Vorlagenname");
		m.put("Template saved", "Vorlage gespeichert");
		m.put("Terminal close", "Terminal schließen");
		m.put("Terminal input", "Terminaleingabe");
		m.put("Terminal open", "Terminal öffnen");
		m.put("Terminal output", "Terminalausgabe");
		m.put("Terminal ready", "Terminal bereit");
		m.put("Terminal resize", "Terminalgröße ändern");
		m.put("Test", "Test");
		m.put("Test Case", "Testfall");
		m.put("Test Cases", "Testfälle");
		m.put("Test Settings", "Testeinstellungen");
		m.put("Test Suite", "Test-Suite");
		m.put("Test Suites", "Test-Suites");
		m.put("Test importing from {0}", "Test wird importiert von {0}");
		m.put("Test mail has been sent to {0}, please check your mail box", "Testmail wurde an {0} gesendet, bitte überprüfen Sie Ihr Postfach");
		m.put("Test successful: authentication passed", "Test erfolgreich: Authentifizierung bestanden");
		m.put("Test successful: authentication passed with below information retrieved:", "Test erfolgreich: Authentifizierung bestanden mit den unten abgerufenen Informationen:");
		m.put("Text", "Text");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "Die URL des Serverendpunkts, der die Webhook-POST-Anfragen empfängt");
		m.put("The change contains disallowed file type(s): {0}", "Die Änderung enthält nicht erlaubte Dateitypen: {0}");
		m.put("The first board will be the default board", "Das erste Board wird das Standard-Board sein");
		m.put("The first timesheet will be the default timesheet", "Die erste Zeiterfassung wird die Standard-Zeiterfassung sein");
		m.put("The object you are deleting/disabling is still being used", "Das Objekt, das Sie löschen/deaktivieren, wird noch verwendet");
		m.put("The password reset url is invalid or obsolete", "Die Passwort-Reset-URL ist ungültig oder veraltet");
		m.put("The permission to access build log", "Die Berechtigung, auf das Build-Log zuzugreifen");
		m.put("The permission to access build pipeline", "Die Berechtigung, auf die Build-Pipeline zuzugreifen");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"Die Berechtigung, einen Job manuell auszuführen. Dies impliziert auch die Berechtigung, auf das Build-Log, die Build-Pipeline und alle veröffentlichten Berichte zuzugreifen");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"Das Geheimnis, mit dem Sie sicherstellen können, dass die an die Payload-URL gesendeten POST-Anfragen von OneDev stammen. Wenn Sie ein Geheimnis festlegen, erhalten Sie den X-OneDev-Signature-Header in der Webhook-POST-Anfrage");
		m.put("The service desk feature enables user to create issues by sending emails to OneDev. Issues can be discussed over email completely, without the need of logging to OneDev.", 
			"Die Service-Desk-Funktion ermöglicht es Benutzern, Issues zu erstellen, indem sie E-Mails an OneDev senden. Issues können vollständig per E-Mail diskutiert werden, ohne dass eine Anmeldung bei OneDev erforderlich ist.");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "Geben Sie dann den im TOTP-Authenticator angezeigten Passcode ein, um zu verifizieren");
		m.put("Then publish package from project directory like below", "Veröffentlichen Sie dann das Paket aus dem Projektverzeichnis wie unten gezeigt");
		m.put("Then push gem to the source", "Drücken Sie dann das Gem auf die Quelle");
		m.put("Then push image to desired repository under specified project", "Drücken Sie dann das Image auf das gewünschte Repository unter dem angegebenen Projekt");
		m.put("Then push package to the source", "Drücken Sie dann das Paket auf die Quelle");
		m.put("Then resolve dependency via command step", "Lösen Sie dann die Abhängigkeit über den Befehls-Schritt");
		m.put("Then upload package to the repository with twine", "Laden Sie dann das Paket mit Twine in das Repository hoch");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"Es gibt <a wicket:id=\"openRequests\">offene Pull-Requests</a> gegen den Branch <span wicket:id=\"branch\"></span>. Diese Pull-Requests werden verworfen, wenn der Branch gelöscht wird.");
		m.put("There are incompatibilities since your upgraded version", "Es gibt Inkompatibilitäten seit Ihrer aktualisierten Version");
		m.put("There are merge conflicts", "Es gibt Merge-Konflikte");
		m.put("There are merge conflicts.", "Es gibt Merge-Konflikte.");
		m.put("There are merge conflicts. You can still create the pull request though", "Es gibt Merge-Konflikte. Sie können den Pull-Request jedoch trotzdem erstellen");
		m.put("There are unsaved changes, discard and continue?", "Es gibt ungespeicherte Änderungen, verwerfen und fortfahren?");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"Diese Authenticatoren laufen normalerweise auf Ihrem Mobiltelefon, einige Beispiele sind Google Authenticator, Microsoft Authenticator, Authy, 1Password usw.");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"Dieses <span wicket:id=\"elementTypeName\"></span> wird importiert von <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>");
		m.put("This Month", "Diesen Monat");
		m.put("This Week", "Diese Woche");
		m.put("This account is disabled", "Dieses Konto ist deaktiviert");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"Diese Adresse sollte ein <code>verifizierter Absender</code> in SendGrid sein und wird als Absenderadresse für verschiedene E-Mail-Benachrichtigungen verwendet. Man kann auch auf diese Adresse antworten, um Issue- oder Pull-Request-Kommentare zu posten, wenn die Option <code>Eingehende E-Mail empfangen</code> unten aktiviert ist.");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"Diese Adresse wird als Absenderadresse für verschiedene E-Mail-Benachrichtigungen verwendet. Benutzer können auch auf diese Adresse antworten, um Issue- oder Pull-Request-Kommentare per E-Mail zu posten, wenn die Option <code>Eingehende E-Mail überprüfen</code> unten aktiviert ist.");
		m.put("This change is already opened for merge by pull request {0}", "Diese Änderung ist bereits durch Pull-Request {0} zum Merge geöffnet");
		m.put("This change is squashed/rebased onto base branch via a pull request", "Diese Änderung wird durch einen Pull-Request auf den Basis-Branch zusammengeführt/umgeschrieben");
		m.put("This change is squashed/rebased onto base branch via pull request {0}", "Diese Änderung wird durch Pull-Request {0} auf den Basis-Branch zusammengeführt/umgeschrieben");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "Diese Änderung muss durch einige Jobs überprüft werden. Reichen Sie stattdessen einen Pull-Request ein");
		m.put("This commit is rebased", "Dieser Commit wird umgeschrieben");
		m.put("This date is using <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 format</a>", 
			"Dieses Datum verwendet das <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601-Format</a>");
		m.put("This email address is being used", "Diese E-Mail-Adresse wird bereits verwendet");
		m.put("This executor runs build jobs as docker containers on OneDev server", "Dieser Executor führt Build-Jobs als Docker-Container auf dem OneDev-Server aus");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"Dieser Executor führt Build-Jobs als Docker-Container auf Remote-Maschinen über <a href='/~administration/agents' target='_blank'>Agents</a> aus");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"Dieser Executor führt Build-Jobs als Pods in einem Kubernetes-Cluster aus. Es werden keine Agents benötigt.<b class='text-danger'>Hinweis:</b> Stellen Sie sicher, dass die Server-URL in den Systemeinstellungen korrekt angegeben ist, da Job-Pods darauf zugreifen müssen, um Quellcode und Artefakte herunterzuladen");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs", 
			"Dieser Executor führt Build-Jobs mit der Shell-Funktionalität des OneDev-Servers aus.<br><b class='text-danger'>WARNUNG</b>: Jobs, die mit diesem Executor ausgeführt werden, haben dieselben Berechtigungen wie der OneDev-Serverprozess. Stellen Sie sicher, dass er nur von vertrauenswürdigen Jobs verwendet werden kann");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. Make sure it can only be used by trusted jobs", 
			"Dieser Executor führt Build-Jobs mit der Shell-Funktionalität von Remote-Maschinen über <a href='/~administration/agents' target='_blank'>Agents</a> aus.<br><b class='text-danger'>WARNUNG</b>: Jobs, die mit diesem Executor ausgeführt werden, haben dieselben Berechtigungen wie der OneDev-Agent-Prozess. Stellen Sie sicher, dass er nur von vertrauenswürdigen Jobs verwendet werden kann");
		m.put("This field is required", "Dieses Feld ist erforderlich");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"Dieser Filter wird verwendet, um den LDAP-Eintrag für den aktuellen Benutzer zu bestimmen. Zum Beispiel: <i>(&(uid={0})(objectclass=person))</i>. In diesem Beispiel repräsentiert <i>{0}</i> den Anmeldenamen des aktuellen Benutzers.");
		m.put("This installation does not have an active subscription and runs as community edition. To access <a href=\"https://onedev.io/pricing\">enterprise features</a>, an active subscription is required", 
			"Diese Installation hat kein aktives Abonnement und läuft als Community-Edition. Um auf <a href=\"https://onedev.io/pricing\">Enterprise-Funktionen</a> zuzugreifen, ist ein aktives Abonnement erforderlich");
		m.put("This installation has a trial subscription and is now running as enterprise edition", 
			"Diese Installation hat ein Testabonnement und läuft derzeit als Enterprise-Edition");
		m.put("This installation has an active subscription and runs as enterprise edition", "Diese Installation hat ein aktives Abonnement und läuft als Enterprise-Edition");
		m.put("This installation has an expired subscription, and runs as community edition", "Diese Installation hat ein abgelaufenes Abonnement und läuft als Community-Edition");
		m.put("This installation has an unlimited users subscription and is now running as enterprise edition", 
			"Diese Installation hat ein unbegrenztes Benutzerabonnement und läuft jetzt als Enterprise-Edition");
		m.put("This installation's subscription has expired and is now running as the community edition", 
			"Das Abonnement dieser Installation ist abgelaufen und läuft jetzt als Community-Edition");
		m.put("This is a Git LFS object, but the storage file is missing", "Dies ist ein Git-LFS-Objekt, aber die Speicherdatei fehlt");
		m.put("This is a built-in role and can not be deleted", "Dies ist eine integrierte Rolle und kann nicht gelöscht werden");
		m.put("This is a disabled service account", "Dies ist ein deaktiviertes Servicekonto");
		m.put("This is a layer cache. To use the cache, add below option to your docker buildx command", 
			"Dies ist ein Layer-Cache. Um den Cache zu verwenden, fügen Sie die folgende Option zu Ihrem Docker-Buildx-Befehl hinzu");
		m.put("This is a service account for task automation purpose", "Dies ist ein Servicekonto für Automatisierungsaufgaben");
		m.put("This is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Dies ist eine Enterprise-Funktion. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("This key has already been used by another project", "Dieser Schlüssel wurde bereits von einem anderen Projekt verwendet");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"Dieser Schlüssel ist mit {0} verknüpft, jedoch ist er KEINE verifizierte E-Mail-Adresse dieses Benutzers");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here.<br><b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated from these lock files with the <b>generate checksum step</b> defined before this step", 
			"Dieser Schlüssel wird verwendet, um festzustellen, ob es einen Cache-Treffer in der Projekt-Hierarchie gibt (Suche vom aktuellen Projekt zum Root-Projekt in der Reihenfolge, gleiches gilt für die unten stehenden Ladeschlüssel). Ein Cache wird als Treffer betrachtet, wenn sein Schlüssel genau mit dem hier definierten Schlüssel übereinstimmt.<br><b>HINWEIS:</b> Falls Ihr Projekt Sperrdateien (package.json, pom.xml, etc.) hat, die den Cache-Zustand darstellen können, sollte dieser Schlüssel als &lt;cache name&gt;-@file:checksum.txt@ definiert werden, wobei checksum.txt aus diesen Sperrdateien mit dem <b>Generieren des Checksummen-Schritts</b> erstellt wird, der vor diesem Schritt definiert ist");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"Dieser Schlüssel wird verwendet, um Cache in der Projekt-Hierarchie herunterzuladen und hochzuladen (Suche vom aktuellen Projekt bis zum Stammprojekt in Reihenfolge)");
		m.put("This key or one of its sub key is already added", "Dieser Schlüssel oder einer seiner Unter-Schlüssel wurde bereits hinzugefügt");
		m.put("This key or one of its subkey is already in use", "Dieser Schlüssel oder einer seiner Unter-Schlüssel wird bereits verwendet");
		m.put("This line has confusable unicode character modification", "Diese Zeile enthält eine verwechselbare Unicode-Zeichenmodifikation");
		m.put("This might happen when project points to a wrong git repository, or the commit is garbage collected.", 
			"Dies könnte passieren, wenn das Projekt auf ein falsches Git-Repository verweist oder der Commit durch Garbage Collection entfernt wurde.");
		m.put("This might happen when project points to a wrong git repository, or these commits are garbage collected.", 
			"Dies könnte passieren, wenn das Projekt auf ein falsches Git-Repository verweist oder diese Commits durch Garbage Collection entfernt wurden.");
		m.put("This name has already been used by another board", "Dieser Name wurde bereits von einem anderen Board verwendet");
		m.put("This name has already been used by another group", "Dieser Name wurde bereits von einer anderen Gruppe verwendet");
		m.put("This name has already been used by another issue board in the project", "Dieser Name wurde bereits von einem anderen Issue-Board im Projekt verwendet");
		m.put("This name has already been used by another job executor", "Dieser Name wurde bereits von einem anderen Job-Executor verwendet");
		m.put("This name has already been used by another project", "Dieser Name wurde bereits von einem anderen Projekt verwendet");
		m.put("This name has already been used by another provider", "Dieser Name wurde bereits von einem anderen Anbieter verwendet");
		m.put("This name has already been used by another role", "Dieser Name wurde bereits von einer anderen Rolle verwendet");
		m.put("This name has already been used by another role.", "Dieser Name wurde bereits von einer anderen Rolle verwendet.");
		m.put("This name has already been used by another script", "Dieser Name wurde bereits von einem anderen Skript verwendet");
		m.put("This name has already been used by another state", "Dieser Name wurde bereits von einem anderen Zustand verwendet");
		m.put("This operation is disallowed by branch protection rule", "Diese Operation ist durch die Branch-Schutzregel nicht erlaubt");
		m.put("This page lists changes since previous build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a>", 
			"Diese Seite listet Änderungen seit dem vorherigen Build auf <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">dem gleichen Stream</a> auf");
		m.put("This page lists recent commits fixing the issue", "Diese Seite listet die letzten Commits auf, die das Problem beheben");
		m.put("This permission enables one to access confidential issues", "Diese Berechtigung ermöglicht den Zugriff auf vertrauliche Issues");
		m.put("This permission enables one to schedule issues into iterations", "Diese Berechtigung ermöglicht das Planen von Issues in Iterationen");
		m.put("This property is imported from {0}", "Diese Eigenschaft wird aus {0} importiert");
		m.put("This pull request has been discarded", "Dieser Pull-Request wurde verworfen");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"Dieser Bericht wird auf der Übersichtsseite des Pull-Requests angezeigt, wenn der Build durch den Pull-Request ausgelöst wird");
		m.put("This server is currently accessed via http protocol, please configure your docker daemon or buildx builder to <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">work with insecure registry</a>", 
			"Dieser Server wird derzeit über das HTTP-Protokoll aufgerufen. Bitte konfigurieren Sie Ihren Docker-Daemon oder Buildx-Builder, um <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">mit einem unsicheren Registry</a> zu arbeiten");
		m.put("This shows average duration of different states over time", "Dies zeigt die durchschnittliche Dauer verschiedener Zustände im Zeitverlauf");
		m.put("This shows average duration of merged pull requests over time", "Dies zeigt die durchschnittliche Dauer von zusammengeführten Pull-Requests im Zeitverlauf");
		m.put("This shows number of <b>new</b> issues in different states over time", "Dies zeigt die Anzahl der <b>neuen</b> Issues in verschiedenen Zuständen im Zeitverlauf");
		m.put("This shows number of issues in various states over time", "Dies zeigt die Anzahl der Issues in verschiedenen Zuständen im Zeitverlauf");
		m.put("This shows number of open and merged pull requests over time", "Dies zeigt die Anzahl der offenen und zusammengeführten Pull-Requests im Zeitverlauf");
		m.put("This step can only be executed by a docker aware executor", "Dieser Schritt kann nur von einem Docker-fähigen Executor ausgeführt werden");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Dieser Schritt kann nur von einem Docker-fähigen Executor ausgeführt werden. Er läuft unter <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job-Arbeitsbereich</a>");
		m.put("This step copies files from job workspace to build artifacts directory, so that they can be accessed after job is completed", 
			"Dieser Schritt kopiert Dateien vom Job-Arbeitsbereich in das Verzeichnis der Build-Artefakte, damit sie nach Abschluss des Jobs zugänglich sind");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"Dieser Schritt veröffentlicht die angegebenen Dateien, um als Projekt-Webseite bereitgestellt zu werden. Die Projekt-Webseite kann öffentlich über <code>http://&lt;onedev base url&gt;/path/to/project/~site</code> aufgerufen werden");
		m.put("This step pulls specified refs from remote", "Dieser Schritt zieht die angegebenen Refs vom Remote");
		m.put("This step pushes current commit to same ref on remote", "Dieser Schritt pusht den aktuellen Commit auf denselben Ref im Remote");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"Dieser Schritt richtet den Renovate-Cache ein. Platzieren Sie ihn vor dem Renovate-Schritt, wenn Sie ihn verwenden möchten");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"Dieser Schritt richtet den Trivy-DB-Cache ein, um verschiedene Scanner-Schritte zu beschleunigen. Platzieren Sie ihn vor den Scanner-Schritten, wenn Sie ihn verwenden möchten");
		m.put("This subscription key was already used", "Dieser Abonnement-Schlüssel wurde bereits verwendet");
		m.put("This subscription key was expired", "Dieser Abonnement-Schlüssel ist abgelaufen");
		m.put("This tab shows the pipeline containing current build. Check <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">this article</a> to understand how build pipeline works", 
			"Dieser Tab zeigt die Pipeline, die den aktuellen Build enthält. Lesen Sie <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">diesen Artikel</a>, um zu verstehen, wie die Build-Pipeline funktioniert");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Dieser Trigger ist nur anwendbar, wenn der getaggte Commit von hier angegebenen Branches erreichbar ist. Mehrere Branches sollten durch Leerzeichen getrennt werden. Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Matching</a>. Mit '-' ausschließen. Leer lassen, um alle Branches zu matchen");
		m.put("This user is authenticating via external system.", "Dieser Benutzer authentifiziert sich über ein externes System.");
		m.put("This user is authenticating via internal database.", "Dieser Benutzer authentifiziert sich über die interne Datenbank.");
		m.put("This user is currently authenticating via external system. Setting password will switch to use internal database", 
			"Dieser Benutzer authentifiziert sich derzeit über ein externes System. Das Festlegen eines Passworts wechselt zur Verwendung der internen Datenbank");
		m.put("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?", 
			"Dies wird das aktuelle Abonnement deaktivieren und alle Enterprise-Funktionen werden deaktiviert. Möchten Sie fortfahren?");
		m.put("This will discard all project specific boards, do you want to continue?", "Dies wird alle projektspezifischen Boards verwerfen. Möchten Sie fortfahren?");
		m.put("This will restart SSH server. Do you want to continue?", "Dies wird den SSH-Server neu starten. Möchten Sie fortfahren?");
		m.put("Threads", "Threads");
		m.put("Time Estimate Issue Field", "Zeitschätzungs-Issue-Feld");
		m.put("Time Range", "Zeitraum");
		m.put("Time Spent Issue Field", "Verbrachte Zeit-Issue-Feld");
		m.put("Time Tracking", "Zeiterfassung");
		m.put("Time Tracking Setting", "Zeiterfassungseinstellung");
		m.put("Time Tracking Settings", "Zeiterfassungseinstellungen");
		m.put("Time tracking settings have been saved", "Zeiterfassungseinstellungen wurden gespeichert");
		m.put("Timed out", "Zeitüberschreitung");
		m.put("Timeout", "Timeout");
		m.put("Timesheet", "Zeiterfassung");
		m.put("Timesheet Setting", "Zeiterfassungseinstellung");
		m.put("Timesheets", "Zeiterfassungen");
		m.put("Timing", "Timing");
		m.put("Title", "Titel");
		m.put("To Everyone", "An alle");
		m.put("To State", "Zum Zustand");
		m.put("To States", "Zu Zuständen");
		m.put("To authenticate via internal database, <a wicket:id=\"setPasswordForUser\">set password for user</a> or <a wicket:id=\"tellUserToResetPassword\">tell user to reset password</a>", 
			"Um sich über die interne Datenbank zu authentifizieren, <a wicket:id=\"setPasswordForUser\">setzen Sie ein Passwort für den Benutzer</a> oder <a wicket:id=\"tellUserToResetPassword\">weisen Sie den Benutzer an, das Passwort zurückzusetzen</a>");
		m.put("To avoid duplication, estimated/remaining time showing here does not include those aggregated from \"{0}\"", 
			"Um Duplikate zu vermeiden, wird die hier angezeigte geschätzte/verbleibende Zeit nicht mit der aus \"{0}\" aggregierten Zeit kombiniert");
		m.put("To avoid duplication, spent time showing here does not include those aggregated from \"{0}\"", 
			"Um Duplikate zu vermeiden, wird die hier angezeigte verbrachte Zeit nicht mit der aus \"{0}\" aggregierten Zeit kombiniert");
		m.put("Toggle change history", "Änderungsverlauf umschalten");
		m.put("Toggle comments", "Kommentare umschalten");
		m.put("Toggle commits", "Commits umschalten");
		m.put("Toggle dark mode", "Dunkelmodus umschalten");
		m.put("Toggle detail message", "Detailnachricht umschalten");
		m.put("Toggle fixed width font", "Festbreitenschrift umschalten");
		m.put("Toggle full screen", "Vollbild umschalten");
		m.put("Toggle matched contents", "Übereinstimmende Inhalte umschalten");
		m.put("Toggle navigation", "Navigation umschalten");
		m.put("Toggle work log", "Arbeitsprotokoll umschalten");
		m.put("Tokens", "Tokens");
		m.put("Too many commits to load", "Zu viele Commits zum Laden");
		m.put("Too many commits, displaying recent {0}", "Zu viele Commits, zeige die letzten {0}");
		m.put("Too many log entries, displaying recent {0}", "Zu viele Logeinträge, zeige die letzten {0}");
		m.put("Too many problems, displaying first {0}", "Zu viele Probleme, zeige die ersten {0}");
		m.put("Toomanyrequests", "Zu viele Anfragen");
		m.put("Top", "Oben");
		m.put("Topo", "Topo");
		m.put("Total Heap Memory", "Gesamter Heap-Speicher");
		m.put("Total Number", "Gesamtanzahl");
		m.put("Total Problems", "Gesamtprobleme");
		m.put("Total Size", "Gesamtgröße");
		m.put("Total Test Duration", "Gesamte Testdauer");
		m.put("Total estimated time", "Geschätzte Gesamtzeit");
		m.put("Total spent time", "Gesamte aufgewendete Zeit");
		m.put("Total spent time / total estimated time", "Gesamte aufgewendete Zeit / geschätzte Gesamtzeit");
		m.put("Total time", "Gesamtzeit");
		m.put("Total:", "Gesamt:");
		m.put("Touched File", "Berührte Datei");
		m.put("Touched Files", "Berührte Dateien");
		m.put("Transfer LFS Files", "LFS-Dateien übertragen");
		m.put("Transit manually", "Manuell übertragen");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "Status des Problems \"{0}\" in \"{1}\" ({2}) überführt");
		m.put("Transition Edit Bean", "Übergangs-Bearbeitungs-Bean");
		m.put("Transition Spec", "Übergangsspezifikation");
		m.put("Trial Expiration Date", "Ablaufdatum der Testversion");
		m.put("Trial subscription key not applicable for this installation", "Testabonnement-Schlüssel für diese Installation nicht anwendbar");
		m.put("Triggers", "Auslöser");
		m.put("Trivy Container Image Scanner", "Trivy Container-Image-Scanner");
		m.put("Trivy Filesystem Scanner", "Trivy Dateisystem-Scanner");
		m.put("Trivy Rootfs Scanner", "Trivy Rootfs-Scanner");
		m.put("Try EE", "EE ausprobieren");
		m.put("Try Enterprise Edition", "Enterprise Edition ausprobieren");
		m.put("Twitch", "Twitch");
		m.put("Two Factor Authentication", "Zwei-Faktor-Authentifizierung");
		m.put("Two-factor Authentication", "Zwei-Faktor-Authentifizierung");
		m.put("Two-factor authentication already set up. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Request to set up again", 
			"Zwei-Faktor-Authentifizierung bereits eingerichtet. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Anfrage zur erneuten Einrichtung");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"Zwei-Faktor-Authentifizierung ist aktiviert. Bitte geben Sie den Passcode ein, der auf Ihrem TOTP-Authenticator angezeigt wird. Wenn Sie Probleme haben, stellen Sie sicher, dass die Zeit des OneDev-Servers und Ihres Geräts mit dem TOTP-Authenticator synchronisiert ist.");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"Zwei-Faktor-Authentifizierung wird für Ihr Konto erzwungen, um die Sicherheit zu erhöhen. Bitte folgen Sie dem untenstehenden Verfahren, um sie einzurichten.");
		m.put("Two-factor authentication is now configured", "Zwei-Faktor-Authentifizierung ist jetzt konfiguriert.");
		m.put("Two-factor authentication not enabled", "Zwei-Faktor-Authentifizierung nicht aktiviert");
		m.put("Type", "Typ");
		m.put("Type <code>yes</code> below to cancel all queried builds", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Builds abzubrechen.");
		m.put("Type <code>yes</code> below to cancel selected builds", "Geben Sie <code>yes</code> unten ein, um ausgewählte Builds abzubrechen.");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "Geben Sie <code>yes</code> unten ein, um das Löschen aller abgefragten Benutzer zu bestätigen.");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "Geben Sie <code>yes</code> unten ein, um das Löschen ausgewählter Benutzer zu bestätigen.");
		m.put("Type <code>yes</code> below to copy all queried issues to project \"{0}\"", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Probleme in das Projekt \"{0}\" zu kopieren.");
		m.put("Type <code>yes</code> below to copy selected issues to project \"{0}\"", "Geben Sie <code>yes</code> unten ein, um ausgewählte Probleme in das Projekt \"{0}\" zu kopieren.");
		m.put("Type <code>yes</code> below to delete all queried builds", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Builds zu löschen.");
		m.put("Type <code>yes</code> below to delete all queried comments", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Kommentare zu löschen.");
		m.put("Type <code>yes</code> below to delete all queried issues", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Probleme zu löschen.");
		m.put("Type <code>yes</code> below to delete all queried packages", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Pakete zu löschen.");
		m.put("Type <code>yes</code> below to delete all queried projects", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Projekte zu löschen.");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Pull-Requests zu löschen.");
		m.put("Type <code>yes</code> below to delete selected builds", "Geben Sie <code>yes</code> unten ein, um ausgewählte Builds zu löschen.");
		m.put("Type <code>yes</code> below to delete selected comments", "Geben Sie <code>yes</code> unten ein, um ausgewählte Kommentare zu löschen.");
		m.put("Type <code>yes</code> below to delete selected issues", "Geben Sie <code>yes</code> unten ein, um ausgewählte Probleme zu löschen.");
		m.put("Type <code>yes</code> below to delete selected packages", "Geben Sie <code>yes</code> unten ein, um ausgewählte Pakete zu löschen.");
		m.put("Type <code>yes</code> below to delete selected projects", "Geben Sie <code>yes</code> unten ein, um ausgewählte Projekte zu löschen.");
		m.put("Type <code>yes</code> below to delete selected pull requests", "Geben Sie <code>yes</code> unten ein, um ausgewählte Pull-Requests zu löschen.");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Pull-Requests zu verwerfen.");
		m.put("Type <code>yes</code> below to discard selected pull requests", "Geben Sie <code>yes</code> unten ein, um ausgewählte Pull-Requests zu verwerfen.");
		m.put("Type <code>yes</code> below to move all queried issues to project \"{0}\"", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Probleme in das Projekt \"{0}\" zu verschieben.");
		m.put("Type <code>yes</code> below to move all queried projects to be under \"{0}\"", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Projekte unter \"{0}\" zu verschieben.");
		m.put("Type <code>yes</code> below to move selected issues to project \"{0}\"", "Geben Sie <code>yes</code> unten ein, um ausgewählte Probleme in das Projekt \"{0}\" zu verschieben.");
		m.put("Type <code>yes</code> below to move selected projects to be under \"{0}\"", "Geben Sie <code>yes</code> unten ein, um ausgewählte Projekte unter \"{0}\" zu verschieben.");
		m.put("Type <code>yes</code> below to pause all queried agents", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Agenten zu pausieren.");
		m.put("Type <code>yes</code> below to re-run all queried builds", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Builds erneut auszuführen.");
		m.put("Type <code>yes</code> below to re-run selected builds", "Geben Sie <code>yes</code> unten ein, um ausgewählte Builds erneut auszuführen.");
		m.put("Type <code>yes</code> below to remove all queried users from group", "Geben Sie unten <code>yes</code> ein, um alle abgefragten Benutzer aus der Gruppe zu entfernen");
		m.put("Type <code>yes</code> below to remove from all queried groups", "Geben Sie unten <code>yes</code> ein, um aus allen abgefragten Gruppen zu entfernen");
		m.put("Type <code>yes</code> below to remove from selected groups", "Geben Sie unten <code>yes</code> ein, um aus ausgewählten Gruppen zu entfernen");
		m.put("Type <code>yes</code> below to remove selected users from group", "Geben Sie unten <code>yes</code> ein, um ausgewählte Benutzer aus der Gruppe zu entfernen");
		m.put("Type <code>yes</code> below to restart all queried agents", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Agenten neu zu starten.");
		m.put("Type <code>yes</code> below to restart selected agents", "Geben Sie <code>yes</code> unten ein, um ausgewählte Agenten neu zu starten.");
		m.put("Type <code>yes</code> below to resume all queried agents", "Geben Sie <code>yes</code> unten ein, um alle abgefragten Agenten fortzusetzen.");
		m.put("Type <code>yes</code> below to set all queried as root projects", "Geben Sie <code>yes</code> unten ein, um alle abgefragten als Root-Projekte festzulegen.");
		m.put("Type <code>yes</code> below to set selected as root projects", "Geben Sie <code>yes</code> unten ein, um ausgewählte als Root-Projekte festzulegen.");
		m.put("Type password here", "Geben Sie hier das Passwort ein.");
		m.put("Type to filter", "Geben Sie ein, um zu filtern.");
		m.put("Type to filter...", "Geben Sie ein, um zu filtern...");
		m.put("URL", "URL");
		m.put("Unable To Delete/Disable Right Now", "Kann derzeit nicht gelöscht/deaktiviert werden");
		m.put("Unable to apply change as otherwise you will not be able to manage this project", "Änderung konnte nicht angewendet werden, da Sie sonst dieses Projekt nicht verwalten können.");
		m.put("Unable to change password as you are authenticating via external system", "Passwort konnte nicht geändert werden, da Sie sich über ein externes System authentifizieren.");
		m.put("Unable to comment", "Kommentar nicht möglich.");
		m.put("Unable to connect to server", "Verbindung zum Server nicht möglich.");
		m.put("Unable to create protected branch", "Geschützten Branch konnte nicht erstellt werden.");
		m.put("Unable to create protected tag", "Geschütztes Tag konnte nicht erstellt werden.");
		m.put("Unable to diff as some line is too long.", "Diff nicht möglich, da einige Zeilen zu lang sind.");
		m.put("Unable to diff as the file is too large.", "Diff nicht möglich, da die Datei zu groß ist.");
		m.put("Unable to find SSO provider: ", "SSO-Anbieter nicht gefunden:");
		m.put("Unable to find agent {0}", "Agent {0} konnte nicht gefunden werden.");
		m.put("Unable to find build #{0} in project {1}", "Build #{0} im Projekt {1} konnte nicht gefunden werden.");
		m.put("Unable to find commit to import build spec (import project: {0}, import revision: {1})", 
			"Commit zum Importieren der Build-Spezifikation konnte nicht gefunden werden (Importprojekt: {0}, Importrevision: {1}).");
		m.put("Unable to find issue #{0} in project {1}", "Problem #{0} im Projekt {1} konnte nicht gefunden werden.");
		m.put("Unable to find project to import build spec: {0}", "Projekt zum Importieren der Build-Spezifikation konnte nicht gefunden werden: {0}.");
		m.put("Unable to find pull request #{0} in project {1}", "Pull-Request #{0} im Projekt {1} konnte nicht gefunden werden.");
		m.put("Unable to find timesheet: ", "Zeiterfassung konnte nicht gefunden werden:");
		m.put("Unable to get guilds info", "Guild-Informationen konnten nicht abgerufen werden.");
		m.put("Unable to import build spec (import project: {0}, import revision: {1}): {2}", "Build-Spezifikation konnte nicht importiert werden (Importprojekt: {0}, Importrevision: {1}): {2}.");
		m.put("Unable to notify user as mail service is not configured", "Benutzer konnte nicht benachrichtigt werden, da der Mail-Service nicht konfiguriert ist.");
		m.put("Unable to send password reset email as mail service is not configured", "Passwort-Reset-E-Mail kann nicht gesendet werden, da der Maildienst nicht konfiguriert ist");
		m.put("Unable to send verification email as mail service is not configured yet", "Bestätigungs-E-Mail konnte nicht gesendet werden, da der Mail-Service noch nicht konfiguriert ist.");
		m.put("Unauthorize this user", "Diesen Benutzer deautorisieren.");
		m.put("Unauthorized", "Unbefugt");
		m.put("Undefined", "Undefiniert");
		m.put("Undefined Field Resolution", "Undefinierte Feldauflösung");
		m.put("Undefined Field Value Resolution", "Undefinierte Feldwertauflösung");
		m.put("Undefined State Resolution", "Unbestimmte Zustandsauflösung");
		m.put("Undefined custom field: ", "Unbestimmtes benutzerdefiniertes Feld:");
		m.put("Under which condition this step should run. <b>SUCCESSFUL</b> means all non-optional steps running before this step are successful", 
			"Unter welcher Bedingung dieser Schritt ausgeführt werden soll. <b>ERFOLGREICH</b> bedeutet, dass alle nicht optionalen Schritte, die vor diesem Schritt ausgeführt werden, erfolgreich sind");
		m.put("Unexpected setting: {0}", "Unerwartete Einstellung: {0}");
		m.put("Unexpected ssh signature hash algorithm: ", "Unerwarteter SSH-Signatur-Hash-Algorithmus:");
		m.put("Unexpected ssh signature namespace: ", "Unerwarteter SSH-Signatur-Namespace:");
		m.put("Unified", "Vereinheitlicht");
		m.put("Unified view", "Vereinheitlichte Ansicht");
		m.put("Unit Test Statistics", "Unit-Test-Statistiken");
		m.put("Unlimited", "Unbegrenzt");
		m.put("Unlink this issue", "Dieses Problem entfernen");
		m.put("Unordered List", "Ungeordnete Liste");
		m.put("Unordered list", "Ungeordnete Liste");
		m.put("Unpin this issue", "Dieses Problem lösen");
		m.put("Unresolved", "Ungelöst");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "Ungelöster Kommentar zur Datei \"{0}\" im Projekt \"{1}\"");
		m.put("Unscheduled", "Ungeplant");
		m.put("Unscheduled Issues", "Ungeplante Probleme");
		m.put("Unsolicited OIDC authentication response", "Unaufgeforderte OIDC-Authentifizierungsantwort");
		m.put("Unsolicited OIDC response", "Unaufgeforderte OIDC-Antwort");
		m.put("Unsolicited discord api response", "Unaufgeforderte Discord-API-Antwort");
		m.put("Unspecified", "Nicht spezifiziert");
		m.put("Unsupported", "Nicht unterstützt");
		m.put("Unsupported ssh signature algorithm: ", "Nicht unterstützter SSH-Signatur-Algorithmus:");
		m.put("Unsupported ssh signature version: ", "Nicht unterstützte SSH-Signatur-Version:");
		m.put("Unverified", "Unverifiziert");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "Unverifizierte E-Mail-Adresse ist für die oben genannten Funktionen <b>NICHT</b> anwendbar");
		m.put("Unvote", "Abstimmung zurückziehen");
		m.put("Unwatched. Click to watch", "Nicht beobachtet. Klicken, um zu beobachten");
		m.put("Update", "Aktualisieren");
		m.put("Update Dependencies via Renovate", "Abhängigkeiten über Renovate aktualisieren");
		m.put("Update Source Branch", "Quellzweig aktualisieren");
		m.put("Update body", "Inhalt aktualisieren");
		m.put("Upload", "Hochladen");
		m.put("Upload Access Token Secret", "Access-Token-Geheimnis hochladen");
		m.put("Upload Cache", "Cache hochladen");
		m.put("Upload Files", "Dateien hochladen");
		m.put("Upload Project Path", "Projektpfad hochladen");
		m.put("Upload Strategy", "Upload-Strategie");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "Laden Sie eine 128x128 transparente PNG-Datei hoch, die als Logo für den Dunkelmodus verwendet werden soll");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "Laden Sie eine 128x128 transparente PNG-Datei hoch, die als Logo für den Hellmodus verwendet werden soll");
		m.put("Upload artifacts", "Artefakte hochladen");
		m.put("Upload avatar", "Avatar hochladen");
		m.put("Upload should be less than {0} Mb", "Upload sollte weniger als {0} MB betragen");
		m.put("Upload to Project", "Zum Projekt hochladen");
		m.put("Uploaded Caches", "Hochgeladene Caches");
		m.put("Uploading file", "Datei wird hochgeladen");
		m.put("Url", "URL");
		m.put("Use '*' for wildcard match", "Verwenden Sie '*' für Wildcard-Matching");
		m.put("Use '*' or '?' for wildcard match. Prefix with '-' to exclude", "Verwenden Sie '*' oder '?' für Wildcard-Matching. Mit '-' voranstellen, um auszuschließen");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Matching</a>");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Verwenden Sie '**', '*' oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Matching</a>. Mit '-' voranstellen, um auszuschließen");
		m.put("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Verwenden Sie '**', '*', oder '?' für <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>Pfad-Wildcard-Matching</a>");
		m.put("Use '\\' to escape brackets", "Verwenden Sie '\\' zum Escapen von Klammern");
		m.put("Use '\\' to escape quotes", "Verwenden Sie '\\' zum Escapen von Anführungszeichen");
		m.put("Use @@ to reference scope in job commands to avoid being interpreted as variable", "Verwenden Sie @@, um den Scope in Job-Befehlen zu referenzieren, um zu vermeiden, dass er als Variable interpretiert wird");
		m.put("Use Avatar Service", "Avatar-Dienst verwenden");
		m.put("Use Default", "Standard verwenden");
		m.put("Use Default Boards", "Standard-Boards verwenden");
		m.put("Use For Git Operations", "Für Git-Operationen verwenden");
		m.put("Use Git in System Path", "Git im Systempfad verwenden");
		m.put("Use Hours And Minutes Only", "Nur Stunden und Minuten verwenden");
		m.put("Use Specified Git", "Festgelegtes Git verwenden");
		m.put("Use Specified curl", "Festgelegtes curl verwenden");
		m.put("Use Step Template", "Schrittvorlage verwenden");
		m.put("Use curl in System Path", "curl im Systempfad verwenden");
		m.put("Use default", "Standard verwenden");
		m.put("Use default storage class", "Standard-Speicherklasse verwenden");
		m.put("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages", 
			"Verwenden Sie das Job-Token als Benutzernamen, damit OneDev weiß, welcher Build ${permission.equals(\"write\")? \"bereitstellt\": \"verwendet\"} Pakete");
		m.put("Use job token to tell OneDev the build publishing the package", "Verwenden Sie das Job-Token, um OneDev den Build mitzuteilen, der das Paket veröffentlicht");
		m.put("Use job token to tell OneDev the build pushing the chart", "Verwenden Sie das Job-Token, um OneDev den Build mitzuteilen, der das Chart pusht");
		m.put("Use job token to tell OneDev the build pushing the package", "Verwenden Sie das Job-Token, um OneDev den Build mitzuteilen, der das Paket pusht");
		m.put("Use job token to tell OneDev the build using the package", "Verwenden Sie das Job-Token, um OneDev den Build mitzuteilen, der das Paket verwendet");
		m.put("Use project dependency to retrieve artifacts from other projects", "Projektabhängigkeit verwenden, um Artefakte aus anderen Projekten abzurufen");
		m.put("Use specified choices", "Festgelegte Auswahlmöglichkeiten verwenden");
		m.put("Use specified default value", "Festgelegten Standardwert verwenden");
		m.put("Use specified value or job secret", "Festgelegten Wert oder Job-Geheimnis verwenden");
		m.put("Use specified values or job secrets", "Festgelegte Werte oder Job-Geheimnisse verwenden");
		m.put("Use triggers to run the job automatically under certain conditions", "Trigger verwenden, um den Job automatisch unter bestimmten Bedingungen auszuführen");
		m.put("Use value of specified parameter/secret", "Wert des festgelegten Parameters/Geheimnisses verwenden");
		m.put("Used Heap Memory", "Verwendeter Heap-Speicher");
		m.put("User", "Benutzer");
		m.put("User \"{0}\" unauthorized", "Benutzer \"{0}\" nicht autorisiert");
		m.put("User Authorization Bean", "Benutzerautorisierungs-Bean");
		m.put("User Authorizations", "Benutzerautorisierungen");
		m.put("User Authorizations Bean", "Benutzerautorisierungs-Bean");
		m.put("User Count", "Benutzeranzahl");
		m.put("User Email Attribute", "Benutzer-E-Mail-Attribut");
		m.put("User Full Name Attribute", "Benutzer-Vollname-Attribut");
		m.put("User Groups Attribute", "Benutzergruppen-Attribut");
		m.put("User Invitation", "Benutzereinladung");
		m.put("User Invitation Template", "Benutzereinladungsvorlage");
		m.put("User Management", "Benutzerverwaltung");
		m.put("User Match Criteria", "Kriterien für Benutzermatching");
		m.put("User Name", "Benutzername");
		m.put("User Principal Name", "Benutzerprinzipalname");
		m.put("User Profile", "Benutzerprofil");
		m.put("User SSH Key Attribute", "Benutzer-SSH-Schlüssel-Attribut");
		m.put("User Search Bases", "Benutzersuchbasen");
		m.put("User Search Filter", "Benutzersuchfilter");
		m.put("User added to group", "Benutzer zur Gruppe hinzugefügt");
		m.put("User authorizations updated", "Benutzerautorisierungen aktualisiert");
		m.put("User authorized", "Benutzer autorisiert");
		m.put("User avatar will be requested by appending a hash to this url", "Benutzeravatar wird durch Anhängen eines Hashes an diese URL angefordert");
		m.put("User can sign up if this option is enabled", "Benutzer kann sich anmelden, wenn diese Option aktiviert ist");
		m.put("User disabled", "Benutzer deaktiviert");
		m.put("User name", "Benutzername");
		m.put("User name already used by another account", "Benutzername wird bereits von einem anderen Konto verwendet");
		m.put("Users", "Benutzer");
		m.put("Users converted to service accounts successfully", "Benutzer erfolgreich in Dienstkonten umgewandelt");
		m.put("Users deleted successfully", "Benutzer erfolgreich gelöscht");
		m.put("Users disabled successfully", "Benutzer erfolgreich deaktiviert");
		m.put("Users enabled successfully", "Benutzer erfolgreich aktiviert");
		m.put("Utilities", "Hilfsprogramme");
		m.put("Valid signature required for head commit of this branch per branch protection rule", 
			"Gültige Signatur für Head-Commit dieses Branches gemäß Branch-Schutzregel erforderlich");
		m.put("Value", "Wert");
		m.put("Value Matcher", "Wert-Matcher");
		m.put("Value Provider", "Wert-Anbieter");
		m.put("Values", "Werte");
		m.put("Values Provider", "Werte-Anbieter");
		m.put("Variable", "Variable");
		m.put("Verification Code", "Verifizierungscode");
		m.put("Verification email sent, please check it", "Verifizierungs-E-Mail gesendet, bitte überprüfen");
		m.put("Verify", "Verifizieren");
		m.put("View", "Ansehen");
		m.put("View Source", "Quelle anzeigen");
		m.put("View source", "Quelle anzeigen");
		m.put("View statistics", "Statistiken anzeigen");
		m.put("Viewer", "Betrachter");
		m.put("Volume Mount", "Volume-Mount");
		m.put("Volume Mounts", "Volume-Mounts");
		m.put("Vote", "Abstimmen");
		m.put("Votes", "Stimmen");
		m.put("WAITING", "WARTEN");
		m.put("WARNING:", "WARNUNG:");
		m.put("Waiting", "Warten");
		m.put("Waiting for approvals", "Warten auf Genehmigungen");
		m.put("Waiting for test mail to come back...", "Warten auf Rückmeldung der Test-Mail...");
		m.put("Watch", "Beobachten");
		m.put("Watch Status", "Beobachtungsstatus");
		m.put("Watch if involved", "Beobachten, wenn beteiligt");
		m.put("Watch if involved (default)", "Beobachten, wenn beteiligt (Standard)");
		m.put("Watch status changed", "Beobachtungsstatus geändert");
		m.put("Watch/Unwatch All Queried Issues", "Alle abgefragten Probleme beobachten/nicht beobachten");
		m.put("Watch/Unwatch All Queried Pull Requests", "Alle abgefragten Pull-Requests beobachten/nicht beobachten");
		m.put("Watch/Unwatch Selected Pull Requests", "Ausgewählte Pull-Requests beobachten/nicht beobachten");
		m.put("Watched. Click to unwatch", "Beobachtet. Klicken, um nicht mehr zu beobachten");
		m.put("Watchers", "Beobachter");
		m.put("Web Hook", "Web-Hook");
		m.put("Web Hooks", "Web-Hooks");
		m.put("Web Hooks Bean", "Web-Hooks-Bean");
		m.put("Web hooks saved", "Web-Hooks gespeichert");
		m.put("Webhook Url", "Webhook-URL");
		m.put("Week", "Woche");
		m.put("When", "Wann");
		m.put("When authorize a group, the group will also be authorized with the role for all child projects", 
			"Wenn eine Gruppe autorisiert wird, wird die Gruppe auch mit der Rolle für alle untergeordneten Projekte autorisiert");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"Wenn ein Projekt autorisiert wird, werden alle untergeordneten Projekte ebenfalls mit zugewiesenen Rollen autorisiert");
		m.put("When authorize a user, the user will also be authorized with the role for all child projects", 
			"Wenn ein Benutzer autorisiert wird, wird der Benutzer auch mit der Rolle für alle untergeordneten Projekte autorisiert");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"Wenn überprüft wird, ob der Benutzer Autor/Committer eines Git-Commits ist, werden alle hier aufgeführten E-Mails überprüft");
		m.put("When evaluating this template, below variables will be available:", "Beim Auswerten dieser Vorlage stehen folgende Variablen zur Verfügung:");
		m.put("When login via OneDev's built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database", 
			"Beim Login über das integrierte Formular von OneDev können die eingegebenen Benutzeranmeldedaten zusätzlich zur internen Datenbank gegen den hier definierten Authenticator überprüft werden");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"Wenn der Ziel-Branch eines Pull-Requests neue Commits hat, wird der Merge-Commit des Pull-Requests neu berechnet, und diese Option gibt an, ob Pull-Request-Builds auf dem vorherigen Merge-Commit akzeptiert werden sollen oder nicht. Wenn aktiviert, müssen die erforderlichen Builds auf dem neuen Merge-Commit erneut ausgeführt werden. Diese Einstellung gilt nur, wenn erforderliche Builds angegeben sind");
		m.put("When this work starts", "Wenn diese Arbeit beginnt");
		m.put("When {0}", "Wenn {0}");
		m.put("Whether or not created issue should be confidential", "Ob das erstellte Problem vertraulich sein soll oder nicht");
		m.put("Whether or not multiple issues can be linked", "Ob mehrere Probleme verknüpft werden können oder nicht");
		m.put("Whether or not multiple issues can be linked on the other side. For instance sub issues on the other side means parent issue, and multiple should be false on that side if only one parent is allowed", 
			"Ob mehrere Probleme auf der anderen Seite verknüpft werden können oder nicht. Beispielsweise bedeutet Unterprobleme auf der anderen Seite ein übergeordnetes Problem, und mehrere sollten auf dieser Seite falsch sein, wenn nur ein übergeordnetes Problem erlaubt ist");
		m.put("Whether or not multiple values can be specified for this field", "Ob mehrere Werte für dieses Feld angegeben werden können oder nicht");
		m.put("Whether or not multiple values can be specified for this param", "Ob mehrere Werte für diesen Parameter angegeben werden können oder nicht");
		m.put("Whether or not the issue should be confidential", "Ob das Problem vertraulich sein soll oder nicht");
		m.put("Whether or not the link is asymmetric. A asymmetric link has different meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric", 
			"Ob der Link asymmetrisch ist oder nicht. Ein asymmetrischer Link hat unterschiedliche Bedeutungen von verschiedenen Seiten. Beispielsweise ist ein 'Eltern-Kind'-Link asymmetrisch, während ein 'Verwandt mit'-Link symmetrisch ist");
		m.put("Whether or not this field accepts empty value", "Ob dieses Feld leere Werte akzeptiert oder nicht");
		m.put("Whether or not this param accepts empty value", "Ob dieser Parameter leere Werte akzeptiert oder nicht");
		m.put("Whether or not this script can be used in CI/CD jobs", "Ob dieses Skript in CI/CD-Jobs verwendet werden kann oder nicht");
		m.put("Whether or not this step is optional. Execution failure of an optional step will not cause the build to fail, and successful condition of subsequent steps will not take optional step into account", 
			"Ob dieser Schritt optional ist oder nicht. Ein Ausführungsfehler eines optionalen Schritts führt nicht dazu, dass der Build fehlschlägt, und die Erfolgsbedingung nachfolgender Schritte berücksichtigt den optionalen Schritt nicht");
		m.put("Whether or not to allow anonymous users to access this server", "Ob anonymen Benutzern der Zugriff auf diesen Server erlaubt werden soll oder nicht");
		m.put("Whether or not to allow creating root projects (project without parent)", "Ob das Erstellen von Root-Projekten (Projekte ohne Eltern) erlaubt werden soll oder nicht");
		m.put("Whether or not to also include children of above projects", "Ob auch die Kinder der oben genannten Projekte einbezogen werden sollen oder nicht");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same machine", 
			"Ob das Image immer gezogen werden soll, wenn ein Container ausgeführt oder Images erstellt werden. Diese Option sollte aktiviert werden, um zu vermeiden, dass Images durch bösartige Jobs auf derselben Maschine ersetzt werden");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same node", 
			"Ob das Image immer gezogen werden soll, wenn ein Container ausgeführt oder Images erstellt werden. Diese Option sollte aktiviert werden, um zu vermeiden, dass Images durch bösartige Jobs auf demselben Node ersetzt werden");
		m.put("Whether or not to be able to access time tracking info of issues", "Ob auf Zeitverfolgungsinformationen von Problemen zugegriffen werden kann oder nicht");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities", 
			"Ob als Service-Konto für Aufgabenautomatisierungszwecke erstellt werden soll oder nicht. Service-Konten haben kein Passwort und keine E-Mail-Adressen und generieren keine Benachrichtigungen für ihre Aktivitäten");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Ob als Service-Konto für Aufgabenautomatisierungszwecke erstellt werden soll oder nicht. Service-Konten haben kein Passwort und keine E-Mail-Adressen und generieren keine Benachrichtigungen für ihre Aktivitäten. <b class='text-warning'>HINWEIS:</b> Service-Konto ist eine Enterprise-Funktion. <a href='https://onedev.io/pricing' target='_blank'>30 Tage kostenlos testen</a>");
		m.put("Whether or not to enable code management for the project", "Ob die Codeverwaltung für das Projekt aktiviert werden soll oder nicht");
		m.put("Whether or not to enable issue management for the project", "Ob die Problemverwaltung für das Projekt aktiviert werden soll oder nicht");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project.", 
			"Ob LFS-Objekte abgerufen werden sollen, wenn der Pull-Request von einem anderen Projekt geöffnet wird");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project. If this option is enabled, git lfs command needs to be installed on OneDev server", 
			"Ob LFS-Objekte abgerufen werden sollen, wenn der Pull-Request von einem anderen Projekt geöffnet wird. Wenn diese Option aktiviert ist, muss der Git-LFS-Befehl auf dem OneDev-Server installiert sein");
		m.put("Whether or not to import forked Bitbucket repositories", "Ob geforkte Bitbucket-Repositories importiert werden sollen oder nicht");
		m.put("Whether or not to import forked GitHub repositories", "Ob geforkte GitHub-Repositories importiert werden sollen oder nicht");
		m.put("Whether or not to import forked GitLab projects", "Ob geforkte GitLab-Projekte importiert werden sollen oder nicht");
		m.put("Whether or not to import forked Gitea repositories", "Ob geforkte Gitea-Repositories importiert werden sollen oder nicht");
		m.put("Whether or not to include forked repositories", "Ob geforkte Repositories einbezogen werden sollen oder nicht");
		m.put("Whether or not to include this field when issue is initially opened. If not, you may include this field later when issue is transited to other states via issue transition rule", 
			"Ob dieses Feld einbezogen werden soll, wenn das Problem ursprünglich geöffnet wird oder nicht. Wenn nicht, können Sie dieses Feld später einbeziehen, wenn das Problem durch eine Problemübergangsregel in andere Zustände überführt wird");
		m.put("Whether or not to input and display estimated/spent time in hours/minutes only", "Ob geschätzte/aufgewendete Zeit nur in Stunden/Minuten eingegeben und angezeigt werden soll oder nicht");
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. Make sure this executor can only be used by trusted jobs if this option is enabled", 
			"Ob der Docker-Sock in den Job-Container eingebunden werden soll, um Docker-Operationen in Job-Befehlen zu unterstützen<br><b class='text-danger'>WARNUNG</b>: Bösartige Jobs können die Kontrolle über das gesamte OneDev übernehmen, indem sie den eingebundenen Docker-Sock verwenden. Stellen Sie sicher, dass dieser Executor nur von vertrauenswürdigen Jobs verwendet werden kann, wenn diese Option aktiviert ist");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"Ob Tag-Zuordnungen auf der nächsten Seite vorab ausgefüllt werden sollen oder nicht. Sie möchten dies möglicherweise deaktivieren, wenn zu viele Tags angezeigt werden sollen");
		m.put("Whether or not to require this dependency to be successful", "Ob diese Abhängigkeit erfolgreich sein muss oder nicht");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"Ob Gruppen des Login-Benutzers abgerufen werden sollen oder nicht. Stellen Sie sicher, dass Sie Gruppenansprüche über die Token-Konfiguration der in Entra ID registrierten App hinzufügen, wenn diese Option aktiviert ist. Der Gruppenanspruch sollte in diesem Fall Gruppen-ID (die Standardoption) über verschiedene Token-Typen zurückgeben");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"Ob Untermodule abgerufen werden sollen oder nicht. Siehe <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>dieses Tutorial</a>, wie Sie oben Anmeldeinformationen für das Klonen einrichten, um Untermodule abzurufen");
		m.put("Whether or not to run this step inside container", "Ob dieser Schritt innerhalb eines Containers ausgeführt werden soll oder nicht");
		m.put("Whether or not to scan recursively in above paths", "Ob in den oben genannten Pfaden rekursiv gescannt werden soll oder nicht");
		m.put("Whether or not to send notifications for events generated by yourself", "Ob Benachrichtigungen für Ereignisse, die von Ihnen selbst generiert wurden, gesendet werden sollen oder nicht");
		m.put("Whether or not to send notifications to issue watchers for this change", "Ob Benachrichtigungen an Problembeobachter für diese Änderung gesendet werden sollen oder nicht");
		m.put("Whether or not to show branch/tag column", "Ob die Spalte für Branch/Tag angezeigt werden soll oder nicht");
		m.put("Whether or not to show duration column", "Ob die Spalte für Dauer angezeigt werden soll oder nicht");
		m.put("Whether or not to use user avatar from a public service", "Ob Benutzer-Avatare von einem öffentlichen Dienst verwendet werden sollen oder nicht");
		m.put("Whether or not use force option to overwrite changes in case ref updating can not be fast-forwarded", 
			"Ob die Option \"Force\" verwendet werden soll, um Änderungen zu überschreiben, falls die Ref-Aktualisierung nicht vorwärtsgeführt werden kann");
		m.put("Whether or not user can remove own account", "Ob Benutzer ihr eigenes Konto entfernen können oder nicht");
		m.put("Whether the password must contain at least one lowercase letter", "Ob das Passwort mindestens einen Kleinbuchstaben enthalten muss");
		m.put("Whether the password must contain at least one number", "Ob das Passwort mindestens eine Zahl enthalten muss");
		m.put("Whether the password must contain at least one special character", "Ob das Passwort mindestens ein Sonderzeichen enthalten muss");
		m.put("Whether the password must contain at least one uppercase letter", "Ob das Passwort mindestens einen Großbuchstaben enthalten muss");
		m.put("Whole Word", "Ganzes Wort");
		m.put("Widget", "Widget");
		m.put("Widget Tab", "Widget-Tab");
		m.put("Widget Timesheet Setting", "Widget-Zeiterfassungseinstellungen");
		m.put("Will be prompted to set up two-factor authentication upon next login", "Wird beim nächsten Login aufgefordert, die Zwei-Faktor-Authentifizierung einzurichten");
		m.put("Will be transcoded to UTF-8", "Wird in UTF-8 transkodiert");
		m.put("Window", "Fenster");
		m.put("Window Memory", "Fensterspeicher");
		m.put("With current number of users ({0}), the subscription will be active until <b>{1}</b>", 
			"Mit der aktuellen Anzahl von Benutzern ({0}) bleibt das Abonnement bis <b>{1}</b> aktiv");
		m.put("Workflow reconciliation completed", "Workflow-Abgleich abgeschlossen");
		m.put("Working Directory", "Arbeitsverzeichnis");
		m.put("Write", "Schreiben");
		m.put("YAML", "YAML");
		m.put("Yes", "Ja");
		m.put("You are not member of discord server", "Sie sind kein Mitglied des Discord-Servers");
		m.put("You are rebasing source branch on top of target branch", "Sie setzen den Quellzweig auf den Zielzweig zurück");
		m.put("You are viewing a subset of all changes. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">show all changes</a>", 
			"Sie sehen einen Teil aller Änderungen. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">alle Änderungen anzeigen</a>");
		m.put("You can also achieve this by adding a build docker image step to your CI/CD job and configuring the built-in registry login with an access token secret that has package write permissions", 
			"Sie können dies auch erreichen, indem Sie einen Schritt zum Erstellen eines Docker-Images zu Ihrem CI/CD-Job hinzufügen und die integrierte Registry-Anmeldung mit einem Zugriffstoken-Secret konfigurieren, das Schreibberechtigungen für Pakete hat");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "Sie haben nicht verifizierte <a wicket:id=\"hasUnverifiedLink\">E-Mail-Adressen</a>");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "Sie können auch eine Datei/Bild in das Eingabefeld ziehen oder ein Bild aus der Zwischenablage einfügen");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"Sie können das Projekt initialisieren, indem Sie <a wicket:id=\"addFiles\" class=\"link-primary\">Dateien hinzufügen</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">Build-Spezifikationen einrichten</a> oder <a wicket:id=\"pushInstructions\" class=\"link-primary\">ein vorhandenes Repository pushen</a>");
		m.put("You selected to delete branch \"{0}\"", "Sie haben ausgewählt, den Zweig \"{0}\" zu löschen");
		m.put("You will be notified of any activities", "Sie werden über alle Aktivitäten benachrichtigt");
		m.put("You've been logged out", "Sie wurden abgemeldet");
		m.put("YouTrack API URL", "YouTrack-API-URL");
		m.put("YouTrack Issue Field", "YouTrack-Issue-Feld");
		m.put("YouTrack Issue Link", "YouTrack-Issue-Link");
		m.put("YouTrack Issue State", "YouTrack-Issue-Status");
		m.put("YouTrack Issue Tag", "YouTrack-Issue-Tag");
		m.put("YouTrack Login Name", "YouTrack-Anmeldename");
		m.put("YouTrack Password or Access Token", "YouTrack-Passwort oder Zugriffstoken");
		m.put("YouTrack Project", "YouTrack-Projekt");
		m.put("YouTrack Projects to Import", "YouTrack-Projekte zum Importieren");
		m.put("Your email address is now verified", "Ihre E-Mail-Adresse ist jetzt verifiziert");
		m.put("Your primary email address is not verified", "Ihre primäre E-Mail-Adresse ist nicht verifiziert");
		m.put("[Any state]", "[Beliebiger Status]");
		m.put("[Reset Password] Please Reset Your OneDev Password", "[Passwort zurücksetzen] Bitte setzen Sie Ihr OneDev-Passwort zurück");
		m.put("a boolean indiciating whether or not topic comment can be created directly by replying the email", 
			"Ein boolescher Wert, der angibt, ob ein Themenkommentar direkt durch Antworten auf die E-Mail erstellt werden kann");
		m.put("a new agent token will be generated each time this button is pressed", "Jedes Mal, wenn diese Schaltfläche gedrückt wird, wird ein neuer Agent-Token generiert");
		m.put("a string representing body of the event. May be <code>null</code>", "Eine Zeichenkette, die den Ereigniskörper darstellt. Kann <code>null</code> sein");
		m.put("a string representing event detail url", "Eine Zeichenkette, die die URL der Ereignisdetails darstellt");
		m.put("a string representing summary of the event", "Eine Zeichenkette, die die Zusammenfassung des Ereignisses darstellt");
		m.put("access [{0}]", "Zugriff [{0}]");
		m.put("active", "aktiv");
		m.put("add another order", "eine weitere Bestellung hinzufügen");
		m.put("adding .onedev-buildspec.yml", "Hinzufügen von .onedev-buildspec.yml");
		m.put("after specified date", "nach dem angegebenen Datum");
		m.put("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information.  A <code>null</code> value means that the notification can not be unsubscribed", 
			"Ein <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>Objekt</a>, das Abmeldeinformationen enthält. Ein <code>null</code>-Wert bedeutet, dass die Benachrichtigung nicht abgemeldet werden kann");
		m.put("and more", "und mehr");
		m.put("archived", "archiviert");
		m.put("artifacts", "Artefakte");
		m.put("assign to me", "mir zuweisen");
		m.put("authored by", "verfasst von");
		m.put("backlog ", "Rückstand");
		m.put("base", "Basis");
		m.put("before specified date", "vor dem angegebenen Datum");
		m.put("branch the build commit is merged into", "Zweig, in den der Build-Commit integriert wird");
		m.put("branch the job is running against", "Zweig, gegen den der Job ausgeführt wird");
		m.put("branch {0}", "Zweig {0}");
		m.put("branches", "Zweige");
		m.put("build", "Build");
		m.put("build is successful for any job and branch", "Build ist für jeden Job und Zweig erfolgreich");
		m.put("build is successful for any job on branches \"{0}\"", "Build ist für jeden Job auf den Zweigen \"{0}\" erfolgreich");
		m.put("build is successful for jobs \"{0}\" on any branch", "Build ist für die Jobs \"{0}\" auf jedem Zweig erfolgreich");
		m.put("build is successful for jobs \"{0}\" on branches \"{1}\"", "Build ist für die Jobs \"{0}\" auf den Zweigen \"{1}\" erfolgreich");
		m.put("builds", "Builds");
		m.put("cURL Example", "cURL-Beispiel");
		m.put("choose a color for this state", "Wählen Sie eine Farbe für diesen Status");
		m.put("cluster:lead", "Leiter");
		m.put("cmd-k to show command palette", "cmd-k, um die Befehlsübersicht anzuzeigen");
		m.put("code commit", "Code-Commit");
		m.put("code is committed", "Code ist committet");
		m.put("code is committed to branches \"{0}\"", "Code wird in die Zweige \"{0}\" eingetragen");
		m.put("code is committed to branches \"{0}\" with message \"{1}\"", "Code ist in die Zweige \"{0}\" mit der Nachricht \"{1}\" committet");
		m.put("code is committed with message \"{0}\"", "Code ist mit der Nachricht \"{0}\" committet");
		m.put("commit message contains", "Commit-Nachricht enthält");
		m.put("commits", "Commits");
		m.put("committed by", "eingetragen von");
		m.put("common", "gemeinsam");
		m.put("common ancestor", "gemeinsamer Vorfahre");
		m.put("container:image", "Image");
		m.put("copy", "kopieren");
		m.put("ctrl-k to show command palette", "ctrl-k, um die Befehlsübersicht anzuzeigen");
		m.put("curl Command Line", "curl-Befehlszeile");
		m.put("curl Path", "curl-Pfad");
		m.put("default", "Standard");
		m.put("descending", "absteigend");
		m.put("disabled", "deaktiviert");
		m.put("does not have any value of", "hat keinen Wert von");
		m.put("duration", "Dauer");
		m.put("enclose with ~ to query hash/message", "Mit ~ umschließen, um Hash/Nachricht abzufragen");
		m.put("enclose with ~ to query job/version", "Mit ~ umschließen, um Job/Version abzufragen");
		m.put("enclose with ~ to query name/ip/os", "Mit ~ umschließen, um Name/IP/OS abzufragen");
		m.put("enclose with ~ to query name/path", "Mit ~ umschließen, um Name/Pfad abzufragen");
		m.put("enclose with ~ to query name/version", "Mit ~ umschließen, um Name/Version abzufragen");
		m.put("enclose with ~ to query path/content/reply", "Mit ~ umschließen, um Pfad/Inhalt/Antwort abzufragen");
		m.put("enclose with ~ to query title/description/comment", "Mit ~ umschließen, um Titel/Beschreibung/Kommentar abzufragen");
		m.put("exclude", "ausschließen");
		m.put("false", "falsch");
		m.put("files with ext \"{0}\"", "Dateien mit der Erweiterung \"{0}\"");
		m.put("find build by number", "Build nach Nummer suchen");
		m.put("find build with this number", "Build mit dieser Nummer suchen");
		m.put("find issue by number", "Issue nach Nummer suchen");
		m.put("find pull request by number", "Pull-Request nach Nummer suchen");
		m.put("find pull request with this number", "Pull-Request mit dieser Nummer suchen");
		m.put("forked from <a wicket:id=\"forkedFrom\"></a>", "Abgeleitet von <a wicket:id=\"forkedFrom\"></a>");
		m.put("found 1 agent", "1 Agent gefunden");
		m.put("found 1 build", "1 Build gefunden");
		m.put("found 1 comment", "1 Kommentar gefunden");
		m.put("found 1 issue", "1 Problem gefunden");
		m.put("found 1 package", "1 Paket gefunden");
		m.put("found 1 project", "1 Projekt gefunden");
		m.put("found 1 pull request", "1 Pull-Request gefunden");
		m.put("found 1 user", "1 Benutzer gefunden");
		m.put("found {0} agents", "{0} Agenten gefunden");
		m.put("found {0} builds", "{0} Builds gefunden");
		m.put("found {0} comments", "{0} Kommentare gefunden");
		m.put("found {0} issues", "{0} Probleme gefunden");
		m.put("found {0} packages", "{0} Pakete gefunden");
		m.put("found {0} projects", "{0} Projekte gefunden");
		m.put("found {0} pull requests", "{0} Pull-Requests gefunden");
		m.put("found {0} users", "{0} Benutzer gefunden");
		m.put("has any value of", "hat einen beliebigen Wert von");
		m.put("head", "Kopf");
		m.put("in current commit", "im aktuellen Commit");
		m.put("ineffective", "unwirksam");
		m.put("inherited", "geerbt");
		m.put("initial", "initial");
		m.put("is empty", "ist leer");
		m.put("is not empty", "ist nicht leer");
		m.put("issue", "Problem");
		m.put("issue:Number", "Anzahl");
		m.put("issues", "Probleme");
		m.put("job", "Job");
		m.put("key ID: ", "Schlüssel-ID:");
		m.put("lines", "Zeilen");
		m.put("link:Multiple", "Mehrere");
		m.put("log", "Protokoll");
		m.put("manage job", "Job verwalten");
		m.put("markdown:heading", "Überschrift");
		m.put("markdown:image", "Bild");
		m.put("may not be empty", "darf nicht leer sein");
		m.put("merged", "zusammengeführt");
		m.put("month:Apr", "Apr");
		m.put("month:Aug", "Aug");
		m.put("month:Dec", "Dez");
		m.put("month:Feb", "Feb");
		m.put("month:Jan", "Jan");
		m.put("month:Jul", "Jul");
		m.put("month:Jun", "Jun");
		m.put("month:Mar", "Mär");
		m.put("month:May", "Mai");
		m.put("month:Nov", "Nov");
		m.put("month:Oct", "Okt");
		m.put("month:Sep", "Sep");
		m.put("n/a", "k.A.");
		m.put("new field", "neues Feld");
		m.put("no activity for {0} days", "keine Aktivität für {0} Tage");
		m.put("on file {0}", "in Datei {0}");
		m.put("opened", "geöffnet");
		m.put("opened <span wicket:id=\"submitDate\"></span>", "geöffnet <span wicket:id=\"submitDate\"></span>");
		m.put("or match another value", "oder mit einem anderen Wert übereinstimmen");
		m.put("order more", "mehr bestellen");
		m.put("outdated", "veraltet");
		m.put("pack", "Paket");
		m.put("package", "Paket");
		m.put("packages", "Pakete");
		m.put("personal", "persönlich");
		m.put("pipeline", "Pipeline");
		m.put("project of the running job", "Projekt des laufenden Jobs");
		m.put("property", "Eigenschaft");
		m.put("pull request", "Pull-Request");
		m.put("pull request #{0}", "Pull-Request #{0}");
		m.put("pull request and code review", "Pull-Request und Code-Review");
		m.put("pull request to any branch is discarded", "Pull-Request zu einem beliebigen Branch wird verworfen");
		m.put("pull request to any branch is merged", "Pull-Request zu einem beliebigen Branch wird zusammengeführt");
		m.put("pull request to any branch is opened", "Pull-Request zu einem beliebigen Branch wird geöffnet");
		m.put("pull request to branches \"{0}\" is discarded", "Pull-Request zu Branches \"{0}\" wird verworfen");
		m.put("pull request to branches \"{0}\" is merged", "Pull-Request zu Branches \"{0}\" wird zusammengeführt");
		m.put("pull request to branches \"{0}\" is opened", "Pull-Request zu Branches \"{0}\" wird geöffnet");
		m.put("pull requests", "Pull-Requests");
		m.put("reconciliation (need administrator permission)", "Abgleich (Administratorberechtigung erforderlich)");
		m.put("reports", "Berichte");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> object to be notified", 
			"repräsentiert das <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>Build</a>-Objekt, das benachrichtigt werden soll");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk", 
			"repräsentiert das <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>Problem</a>, das über den Service Desk geöffnet wird");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified", 
			"repräsentiert das <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>Problem</a>-Objekt, das benachrichtigt werden soll");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> object to be notified", 
			"repräsentiert das <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>Paket</a>-Objekt, das benachrichtigt werden soll");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified", 
			"repräsentiert das <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>Pull-Request</a>-Objekt, das benachrichtigt werden soll");
		m.put("represents the <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> object to be notified", 
			"repräsentiert das <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>Commit</a>-Objekt, das benachrichtigt werden soll");
		m.put("represents the exception encountered when open issue via service desk", "repräsentiert die Ausnahme, die beim Öffnen eines Problems über den Service Desk aufgetreten ist");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>", 
			"repräsentiert das abgemeldete <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>Problem</a>");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>", 
			"repräsentiert den abgemeldeten <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>Pull-Request</a>");
		m.put("request to change", "Anfrage zur Änderung");
		m.put("root", "Root");
		m.put("root url of OneDev server", "Root-URL des OneDev-Servers");
		m.put("run job", "Job ausführen");
		m.put("search in this revision will be accurate after indexed", "Suche in dieser Revision wird nach der Indexierung genau sein");
		m.put("service", "Dienst");
		m.put("severity:CRITICAL", "Kritisch");
		m.put("severity:HIGH", "Hoch");
		m.put("severity:LOW", "Niedrig");
		m.put("severity:MEDIUM", "Mittel");
		m.put("skipped {0} lines", "{0} Zeilen übersprungen");
		m.put("space", "Platz");
		m.put("state of an issue is transited", "der Status eines Problems wird übertragen");
		m.put("step template", "Schrittvorlage");
		m.put("submit", "Absenden");
		m.put("tag the job is running against", "Tag, gegen das der Job läuft");
		m.put("tag {0}", "tag {0}");
		m.put("tags", "Tags");
		m.put("the url to set up user account", "die URL zur Einrichtung eines Benutzerkontos");
		m.put("time aggregation link", "Link zur Zeitaggregation");
		m.put("touching specified path", "Berühren des angegebenen Pfads");
		m.put("transit manually by any user", "manuell durch jeden Benutzer übertragen");
		m.put("transit manually by any user of roles \"{0}\"", "manuell durch jeden Benutzer mit Rollen \"{0}\" übertragen");
		m.put("true", "wahr");
		m.put("true for html version, false for text version", "wahr für HTML-Version, falsch für Textversion");
		m.put("up to date", "auf dem neuesten Stand");
		m.put("url following which to verify email address", "URL, über die die E-Mail-Adresse überprüft werden kann");
		m.put("url to reset password", "URL zum Zurücksetzen des Passworts");
		m.put("value needs to be enclosed in brackets", "Wert muss in Klammern eingeschlossen werden");
		m.put("value needs to be enclosed in parenthesis", "Wert muss in runden Klammern eingeschlossen werden");
		m.put("value should be quoted", "Wert sollte in Anführungszeichen gesetzt werden");
		m.put("w%02d", "w%02d");
		m.put("week:Fri", "Fr");
		m.put("week:Mon", "Mo");
		m.put("week:Sat", "Sa");
		m.put("week:Sun", "So");
		m.put("week:Thu", "Do");
		m.put("week:Tue", "Di");
		m.put("week:Wed", "Mi");
		m.put("widget:Tabs", "Tabs");
		m.put("you may show this page later via incompatibilities link in help menu", "Sie können diese Seite später über den Link \"Inkompatibilitäten\" im Hilfemenü anzeigen.");
		m.put("{0} Month(s)", "{0} Monat(e)");
		m.put("{0} activities on {1}", "{0} Aktivitäten am {1}");
		m.put("{0} additions & {1} deletions", "{0} Hinzufügungen & {1} Löschungen");
		m.put("{0} ahead", "{0} voraus");
		m.put("{0} behind", "{0} zurück");
		m.put("{0} branches", "{0} Branches");
		m.put("{0} build(s)", "{0} Build(s)");
		m.put("{0} child projects", "{0} Unterprojekte");
		m.put("{0} commits", "{0} Commits");
		m.put("{0} commits ahead of base branch", "{0} Commits vor dem Basis-Branch");
		m.put("{0} commits behind of base branch", "{0} Commits hinter dem Basis-Branch");
		m.put("{0} day", "{0} Tag");
		m.put("{0} days", "{0} Tage");
		m.put("{0} edited {1}", "{0} bearbeitet {1}");
		m.put("{0} files", "{0} Dateien");
		m.put("{0} forks", "{0} Forks");
		m.put("{0} hour", "{0} Stunde");
		m.put("{0} hours", "{0} Stunden");
		m.put("{0} inaccessible activities", "{0} nicht zugängliche Aktivitäten");
		m.put("{0} minute", "{0} Minute");
		m.put("{0} minutes", "{0} Minuten");
		m.put("{0} reviewed", "{0} überprüft");
		m.put("{0} second", "{0} Sekunde");
		m.put("{0} seconds", "{0} Sekunden");
		m.put("{0} tags", "{0} Tags");
		m.put("{0}d", "{0}d");
		m.put("{0}h", "{0}h");
		m.put("{0}m", "{0}m");
		m.put("{0}s", "{0}s");
		m.put("{0}w", "{0}w");
		m.put("{javax.validation.constraints.NotEmpty.message}", "{javax.validation.constraints.NotEmpty.message}");
		m.put("{javax.validation.constraints.NotNull.message}", "{javax.validation.constraints.NotNull.message}");
		m.put("{javax.validation.constraints.Size.message}", "{javax.validation.constraints.Size.message}");
	}
			
	@Override
	protected Map<String, String> getContents() {
		return m;		
	}
	
}
