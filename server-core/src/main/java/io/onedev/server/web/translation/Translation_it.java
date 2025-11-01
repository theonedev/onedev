package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_it extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init(m);
		Translation.watchUpdate(Translation_it.class, () -> {
			init(m);
		});
	}

	@SystemPrompt("You are good at translating from English to Italian in DevOps software area.")
	public static void init(Map<String, String> m) {
		m.clear();
		m.put(" Project path can be omitted if reference from current project", "Il percorso del progetto può essere omesso se si fa riferimento al progetto corrente");
		m.put("'..' is not allowed in the directory", "'..' non è consentito nella directory");
		m.put("(* = any string, ? = any character)", "(* = qualsiasi stringa, ? = qualsiasi carattere)");
		m.put("(on behalf of <b>{0}</b>)", "(per conto di <b>{0}</b>)");
		m.put("** Enterprise edition is disabled as the subscription was expired. Renew to enable **", 
			"** L'edizione Enterprise è disabilitata poiché l'abbonamento è scaduto. Rinnova per abilitare **");
		m.put("** Enterprise edition is disabled as the trial subscription was expired, order subscription to enable or contact support@onedev.io if you need to extend your trial **", 
			"** L'edizione Enterprise è disabilitata poiché l'abbonamento di prova è scaduto, ordina un abbonamento per abilitarla o contatta support@onedev.io se hai bisogno di estendere la prova **");
		m.put("** Enterprise edition is disabled as there is no remaining user months. Order more to enable **", 
			"** L'edizione Enterprise è disabilitata poiché non ci sono mesi utente rimanenti. Ordina di più per abilitarla **");
		m.put("1. To use this package, add below to project pom.xml", "1. Per utilizzare questo pacchetto, aggiungi quanto segue al file pom.xml del progetto");
		m.put("1. Use below repositories in project pom.xml", "1. Usa i seguenti repository nel file pom.xml del progetto");
		m.put("1w 1d 1h 1m", "1w 1d 1h 1m");
		m.put("2. Add below to <code>$HOME/.m2/settings.xml</code> if you want to deploy from command line", 
			"2. Aggiungi quanto segue a <code>$HOME/.m2/settings.xml</code> se vuoi eseguire il deploy dalla riga di comando");
		m.put("2. Also add below to $HOME/.m2/settings.xml if you want to compile project from command line", 
			"2. Aggiungi anche quanto segue a $HOME/.m2/settings.xml se vuoi compilare il progetto dalla riga di comando");
		m.put("3. For CI/CD job, it is more convenient to use a custom settings.xml, for instance via below code in a command step:", 
			"3. Per il lavoro CI/CD, è più conveniente utilizzare un file settings.xml personalizzato, ad esempio tramite il seguente codice in uno step di comando:");
		m.put("6-digits passcode", "Passcode a 6 cifre");
		m.put("7 days", "7 giorni");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">utente</a> per reimpostare la password");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">utente</a> per verificare l'email");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">Markdown stile GitHub</a> è accettato, con <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">supporto mermaid e katex</a>.");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>oggetto evento</a> che ha attivato la notifica");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> da visualizzare");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a> overdue", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>cronometro</a> scaduto");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> ha effettuato il commit <span wicket:id=\"date\"></span>");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed with <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> ha effettuato il commit con <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>");
		m.put("<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depends on me", "<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> dipende da me");
		m.put("<a wicket:id=\"removePassword\">Remove password</a> to force the user to authenticate via external system", 
			"<a wicket:id=\"removePassword\">Rimuovi password</a> per forzare l'utente ad autenticarsi tramite sistema esterno");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">Verifica tramite codice di recupero</a> se non puoi accedere al tuo autenticatore TOTP");
		m.put("<b class='text-danger'>NOTE: </b> This requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b> Questo richiede un abbonamento Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b> Questo step richiede un abbonamento Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b>L'integrazione con SendGrid è una funzionalità Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>NOTA: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Il tracciamento del tempo</a> è una funzionalità Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("<b>NOTE: </b> Service desk only takes effect if <a wicket:id=\"mailConnector\">mail service</a> is defined and its <tt>check incoming email</tt> option is enabled. Also <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>sub addressing</a> needs to be enabled for the system email address. Check <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>this tutorial</a> for details", 
			"<b>NOTA: </b> Il service desk ha effetto solo se <a wicket:id=\"mailConnector\">il servizio di posta</a> è definito e l'opzione <tt>controlla email in arrivo</tt> è abilitata. Inoltre, <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>il sub addressing</a> deve essere abilitato per l'indirizzo email del sistema. Consulta <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>questo tutorial</a> per i dettagli");
		m.put("<b>NOTE:</b> Batch editing issues will not cause state transitions of other issues even if transition rule matches", 
			"<b>NOTA:</b> La modifica in batch delle issue non causerà transizioni di stato di altre issue anche se la regola di transizione corrisponde");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>Proprietario del progetto</b> è un ruolo integrato con pieno permesso sui progetti");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>Consigli: </b> Digita <tt>@</tt> per <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>inserire una variabile</a>. Usa <tt>@@</tt> per il simbolo <tt>@</tt> letterale");
		m.put("<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Cerca file</span> <span class='font-size-sm text-muted'>nel branch predefinito</span></div>");
		m.put("<div><span>Search Symbols</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Cerca simboli</span> <span class='font-size-sm text-muted'>nel branch predefinito</span></div>");
		m.put("<div><span>Search Text</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Cerca testo</span> <span class='font-size-sm text-muted'>nel branch predefinito</span></div>");
		m.put("<i>No Name</i>", "<i>Nessun nome</i>");
		m.put("<span class=\"keycap ml-3 mr-1\">esc</span> to close", "<span class=\"keycap ml-3 mr-1\">esc</span> per chiudere");
		m.put("<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> to move", 
			"<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> per muovere");
		m.put("<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> to navigate. <span class=\"keycap\">Esc</span> to close", 
			"<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> per navigare. <span class=\"keycap\">Esc</span> per chiudere");
		m.put("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete.", 
			"<span class='keycap'>Tab</span> o <span class='keycap'>Enter</span> per completare.");
		m.put("<span class='keycap'>Tab</span> to complete.", "<span class='keycap'>Tab</span> per completare.");
		m.put("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>", "<span class='text-nowrap'><span class='keycap'>Enter</span> per andare</span>");
		m.put("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>", "<span class='text-nowrap'><span class='keycap'>Tab</span> per cercare</span>");
		m.put("<span wicket:id=\"activityCount\"></span> activities", "<span wicket:id=\"activityCount\"></span> attività");
		m.put("<svg class='icon mr-2'><use xlink:href='%s'/></svg> Define job secrets to be used in build spec. Secrets with <b>same name</b> can be defined. For a particular name, the first authorized secret with that name will be used (search in current project first, then search in parent projects). Note that secret value containing line breaks or less than <b>%d</b> characters will not be masked in build log", 
			"<svg class='icon mr-2'><use xlink:href='%s'/></svg> Definisci i segreti del lavoro da utilizzare nella specifica di build. I segreti con <b>lo stesso nome</b> possono essere definiti. Per un nome particolare, verrà utilizzato il primo segreto autorizzato con quel nome (cerca prima nel progetto corrente, poi nei progetti genitori). Nota che il valore del segreto contenente interruzioni di riga o meno di <b>%d</b> caratteri non sarà mascherato nel log di build");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here", 
			"Qui è previsto un <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>pattern Java</a>");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"Un <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>espressione regolare Java</a> per validare il footer del messaggio di commit");
		m.put("A child project with name \"{0}\" already exists under \"{1}\"", "Un progetto figlio con nome \"{0}\" esiste già sotto \"{1}\"");
		m.put("A file exists where you’re trying to create a subdirectory. Choose a new path and try again..", 
			"Esiste un file dove stai cercando di creare una sottodirectory. Scegli un nuovo percorso e riprova.");
		m.put("A path with same name already exists.Please choose a different name and try again.", 
			"Esiste già un percorso con lo stesso nome. Scegli un nome diverso e riprova.");
		m.put("A pull request is open for this change", "Una pull request è aperta per questa modifica");
		m.put("A root project with name \"{0}\" already exists", "Un progetto radice con nome \"{0}\" esiste già");
		m.put("A {0} used as body of address verification email", "Un {0} utilizzato come corpo dell'email di verifica dell'indirizzo");
		m.put("A {0} used as body of build notification email", "Un {0} utilizzato come corpo dell'email di notifica di build");
		m.put("A {0} used as body of commit notification email", "Un {0} utilizzato come corpo dell'email di notifica di commit");
		m.put("A {0} used as body of feedback email when failed to open issue via service desk", "Un {0} utilizzato come corpo dell'email di feedback quando non è stato possibile aprire un'issue tramite il service desk");
		m.put("A {0} used as body of feedback email when issue is opened via service desk", "Un {0} utilizzato come corpo dell'email di feedback quando un'issue è stata aperta tramite il service desk");
		m.put("A {0} used as body of feedback email when unsubscribed from issue notification", "Un {0} utilizzato come corpo dell'email di feedback quando si è annullata l'iscrizione alla notifica dell'issue");
		m.put("A {0} used as body of feedback email when unsubscribed from pull request notification", 
			"Un {0} utilizzato come corpo dell'email di feedback quando si è annullata l'iscrizione alla notifica della pull request");
		m.put("A {0} used as body of issue stopwatch overdue notification email", "Un {0} utilizzato come corpo dell'email di notifica di scadenza del cronometro dell'issue");
		m.put("A {0} used as body of package notification email", "Un {0} utilizzato come corpo dell'email di notifica del pacchetto");
		m.put("A {0} used as body of password reset email", "Un {0} utilizzato come corpo dell'email di reimpostazione della password");
		m.put("A {0} used as body of system alert email", "Un {0} utilizzato come corpo dell'email di avviso di sistema");
		m.put("A {0} used as body of user invitation email", "Un {0} utilizzato come corpo dell'email di invito utente");
		m.put("A {0} used as body of various issue notification emails", "Un {0} utilizzato come corpo delle varie email di notifica delle issue");
		m.put("A {0} used as body of various pull request notification emails", "Un {0} utilizzato come corpo delle varie email di notifica delle pull request");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"URL API della tua istanza cloud JIRA, ad esempio, <tt>https://your-domain.atlassian.net/rest/api/3</tt>");
		m.put("Able to merge without conflicts", "In grado di unire senza conflitti");
		m.put("Absolute or relative url of the image", "URL assoluto o relativo dell'immagine");
		m.put("Absolute or relative url of the link", "URL assoluto o relativo del link");
		m.put("Access Anonymously", "Accesso anonimo");
		m.put("Access Build Log", "Accesso al log di build");
		m.put("Access Build Pipeline", "Accesso alla pipeline di build");
		m.put("Access Build Reports", "Accesso ai report di build");
		m.put("Access Confidential Issues", "Accesso alle issue confidenziali");
		m.put("Access Time Tracking", "Accesso al tracciamento del tempo");
		m.put("Access Token", "Token di accesso");
		m.put("Access Token Authorization Bean", "Bean di autorizzazione del token di accesso");
		m.put("Access Token Edit Bean", "Bean di modifica del token di accesso");
		m.put("Access Token Secret", "Segreto del token di accesso");
		m.put("Access Token for Target Project", "Token di accesso per il progetto di destinazione");
		m.put("Access Tokens", "Token di accesso");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"Il token di accesso è destinato all'accesso API e al pull/push del repository. Non può essere utilizzato per accedere all'interfaccia web");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"Il token di accesso è destinato all'accesso API o al pull/push del repository. Non può essere utilizzato per accedere all'interfaccia web");
		m.put("Access token regenerated successfully", "Token di accesso rigenerato con successo");
		m.put("Access token regenerated, make sure to update the token at agent side", "Token di accesso rigenerato, assicurati di aggiornare il token sul lato agente");
		m.put("Account Email", "Email dell'account");
		m.put("Account Name", "Nome dell'account");
		m.put("Account is disabled", "L'account è disabilitato");
		m.put("Account set up successfully", "Account configurato con successo");
		m.put("Active Directory", "Active Directory");
		m.put("Active Since", "Attivo da");
		m.put("Activities", "Attività");
		m.put("Activity by type", "Attività per tipo");
		m.put("Add", "Aggiungi");
		m.put("Add Executor", "Aggiungi Executor");
		m.put("Add GPG key", "Aggiungi chiave GPG");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "Aggiungi qui le chiavi GPG per verificare i commit/tag firmati da questo utente");
		m.put("Add GPG keys here to verify commits/tags signed by you", "Aggiungi qui le chiavi GPG per verificare i commit/tag firmati da te");
		m.put("Add GPG public keys to be trusted here. Commits signed with trusted keys will be shown as verified.", 
			"Aggiungi qui le chiavi pubbliche GPG da considerare affidabili. I commit firmati con chiavi affidabili saranno mostrati come verificati.");
		m.put("Add Issue...", "Aggiungi Issue...");
		m.put("Add Issues to Iteration", "Aggiungi Issue all'iterazione");
		m.put("Add New", "Aggiungi nuovo");
		m.put("Add New Board", "Aggiungi nuova board");
		m.put("Add New Email Address", "Aggiungi nuovo indirizzo email");
		m.put("Add New Timesheet", "Aggiungi nuovo foglio presenze");
		m.put("Add Rule", "Aggiungi regola");
		m.put("Add SSH key", "Aggiungi chiave SSH");
		m.put("Add SSO provider", "Aggiungi provider SSO");
		m.put("Add Spent Time", "Aggiungi tempo speso");
		m.put("Add Timesheet", "Aggiungi foglio presenze");
		m.put("Add Widget", "Aggiungi widget");
		m.put("Add a GPG Public Key", "Aggiungi una chiave pubblica GPG");
		m.put("Add a SSH Key", "Aggiungi una chiave SSH");
		m.put("Add a package source like below", "Aggiungi una sorgente pacchetto come sotto");
		m.put("Add after", "Aggiungi dopo");
		m.put("Add agent", "Aggiungi agente");
		m.put("Add all cards to specified iteration", "Aggiungi tutte le schede all'iterazione specificata");
		m.put("Add all commits from source branch to target branch with a merge commit", "Aggiungi tutti i commit dal branch sorgente al branch di destinazione con un commit di merge");
		m.put("Add assignee...", "Aggiungi assegnatario...");
		m.put("Add before", "Aggiungi prima");
		m.put("Add below to allow accessing via http protocol in new Maven versions", "Aggiungi quanto segue per consentire l'accesso tramite protocollo http nelle nuove versioni di Maven");
		m.put("Add child project", "Aggiungi progetto figlio");
		m.put("Add comment", "Aggiungi commento");
		m.put("Add comment on this selection", "Aggiungi commento su questa selezione");
		m.put("Add custom field", "Aggiungi campo personalizzato");
		m.put("Add dashboard", "Aggiungi dashboard");
		m.put("Add default issue board", "Aggiungi bacheca predefinita per i problemi");
		m.put("Add files to current directory", "Aggiungi file alla directory corrente");
		m.put("Add files via upload", "Aggiungi file tramite upload");
		m.put("Add groovy script", "Aggiungi script groovy");
		m.put("Add issue description template", "Aggiungi modello di descrizione del problema");
		m.put("Add issue link", "Aggiungi collegamento al problema");
		m.put("Add issue state", "Aggiungi stato del problema");
		m.put("Add issue state transition", "Aggiungi transizione di stato del problema");
		m.put("Add link", "Aggiungi collegamento");
		m.put("Add new", "Aggiungi nuovo");
		m.put("Add new card to this column", "Aggiungi nuova scheda a questa colonna");
		m.put("Add new file", "Aggiungi nuovo file");
		m.put("Add new import", "Aggiungi nuova importazione");
		m.put("Add new issue creation setting", "Aggiungi nuova impostazione di creazione problema");
		m.put("Add new job dependency", "Aggiungi nuova dipendenza di lavoro");
		m.put("Add new param", "Aggiungi nuovo parametro");
		m.put("Add new post-build action", "Aggiungi nuova azione post-build");
		m.put("Add new project dependency", "Aggiungi nuova dipendenza di progetto");
		m.put("Add new step", "Aggiungi nuovo passaggio");
		m.put("Add new trigger", "Aggiungi nuovo trigger");
		m.put("Add project", "Aggiungi progetto");
		m.put("Add reviewer...", "Aggiungi revisore...");
		m.put("Add to batch to commit with other suggestions later", "Aggiungi al batch per il commit con altre proposte più tardi");
		m.put("Add to group...", "Aggiungi al gruppo...");
		m.put("Add to iteration...", "Aggiungi all'iterazione...");
		m.put("Add user to group...", "Aggiungi utente al gruppo...");
		m.put("Add value", "Aggiungi valore");
		m.put("Add {0}", "Aggiungi {0}");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "Commit aggiunto \"{0}\" (<i class='text-danger'>mancante nel repository</i>)");
		m.put("Added commit \"{0}\" ({1})", "Commit aggiunto \"{0}\" ({1})");
		m.put("Added to group", "Aggiunto al gruppo");
		m.put("Additions", "Aggiunte");
		m.put("Administration", "Amministrazione");
		m.put("Administrative permission over a project", "Permesso amministrativo su un progetto");
		m.put("Advanced Search", "Ricerca avanzata");
		m.put("After modification", "Dopo la modifica");
		m.put("Agent", "Agente");
		m.put("Agent Attribute", "Attributo agente");
		m.put("Agent Count", "Conteggio agenti");
		m.put("Agent Edit Bean", "Modifica bean agente");
		m.put("Agent Selector", "Selettore agente");
		m.put("Agent is designed to be maintenance free. Once connected to server, it will be updated automatically upon server upgrade", 
			"L'agente è progettato per essere senza manutenzione. Una volta connesso al server, verrà aggiornato automaticamente al momento dell'aggiornamento del server");
		m.put("Agent removed", "Agente rimosso");
		m.put("Agent tokens are used to authorize agents. It should be configured via environment variable <tt>agentToken</tt> if agent runs as docker container, or property <tt>agentToken</tt> in file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> if agent runs on bare metal/virtual machine. A token will be in-use and removed from this list if agent using it connects to server", 
			"I token degli agenti vengono utilizzati per autorizzare gli agenti. Dovrebbe essere configurato tramite variabile di ambiente <tt>agentToken</tt> se l'agente viene eseguito come container Docker, o proprietà <tt>agentToken</tt> nel file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> se l'agente viene eseguito su metallo nudo/macchina virtuale. Un token sarà in uso e rimosso da questa lista se l'agente che lo utilizza si connette al server");
		m.put("Agents", "Agenti");
		m.put("Agents can be used to execute jobs on remote machines. Once started it will update itself from server automatically when necessary", 
			"Gli agenti possono essere utilizzati per eseguire lavori su macchine remote. Una volta avviato, si aggiornerà automaticamente dal server quando necessario");
		m.put("Aggregated from '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':", "Aggregato da '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':");
		m.put("Aggregated from '<span wicket:id=\"spentTimeAggregationLink\"></span>':", "Aggregato da '<span wicket:id=\"spentTimeAggregationLink\"></span>':");
		m.put("Aggregation Link", "Collegamento di aggregazione");
		m.put("Alert", "Avviso");
		m.put("Alert Setting", "Impostazione avviso");
		m.put("Alert Settings", "Impostazioni avviso");
		m.put("Alert settings have been updated", "Le impostazioni degli avvisi sono state aggiornate");
		m.put("Alerts", "Avvisi");
		m.put("All", "Tutti");
		m.put("All Issues", "Tutti i problemi");
		m.put("All RESTful Resources", "Tutte le risorse RESTful");
		m.put("All accessible", "Tutti accessibili");
		m.put("All builds", "Tutte le build");
		m.put("All changes", "Tutte le modifiche");
		m.put("All except", "Tutti eccetto");
		m.put("All files", "Tutti i file");
		m.put("All groups", "Tutti i gruppi");
		m.put("All issues", "Tutti i problemi");
		m.put("All platforms in OCI layout", "Tutte le piattaforme nel layout OCI");
		m.put("All platforms in image", "Tutte le piattaforme nell'immagine");
		m.put("All possible classes", "Tutte le classi possibili");
		m.put("All projects", "Tutti i progetti");
		m.put("All projects with code read permission", "Tutti i progetti con permesso di lettura del codice");
		m.put("All pull requests", "Tutte le pull request");
		m.put("All users", "Tutti gli utenti");
		m.put("Allow Empty", "Consenti vuoto");
		m.put("Allow Empty Value", "Consenti valore vuoto");
		m.put("Allow Multiple", "Consenti multipli");
		m.put("Allowed Licenses", "Licenze consentite");
		m.put("Allowed Self Sign-Up Email Domain", "Dominio email per auto-iscrizione consentito");
		m.put("Always", "Sempre");
		m.put("Always Pull Image", "Scarica sempre immagine");
		m.put("An issue already linked for {0}. Unlink it first", "Un problema è già collegato per {0}. Scollegalo prima");
		m.put("An unexpected exception occurred", "Si è verificata un'eccezione imprevista");
		m.put("And configure auth token of the registry", "E configura il token di autenticazione del registro");
		m.put("Another pull request already open for this change", "Un'altra pull request già aperta per questa modifica");
		m.put("Any agent", "Qualsiasi agente");
		m.put("Any branch", "Qualsiasi branch");
		m.put("Any commit message", "Qualsiasi messaggio di commit");
		m.put("Any domain", "Qualsiasi dominio");
		m.put("Any file", "Qualsiasi file");
		m.put("Any issue", "Qualsiasi problema");
		m.put("Any job", "Qualsiasi lavoro");
		m.put("Any project", "Qualsiasi progetto");
		m.put("Any ref", "Qualsiasi riferimento");
		m.put("Any sender", "Qualsiasi mittente");
		m.put("Any state", "Qualsiasi stato");
		m.put("Any tag", "Qualsiasi tag");
		m.put("Any user", "Qualsiasi utente");
		m.put("Api Key", "Chiave API");
		m.put("Api Token", "Token API");
		m.put("Api Url", "URL API");
		m.put("Append", "Aggiungi");
		m.put("Applicable Branches", "Branch applicabili");
		m.put("Applicable Builds", "Build applicabili");
		m.put("Applicable Code Comments", "Commenti al codice applicabili");
		m.put("Applicable Commit Messages", "Messaggi di Commit Applicabili");
		m.put("Applicable Commits", "Commit applicabili");
		m.put("Applicable Images", "Immagini applicabili");
		m.put("Applicable Issues", "Problemi applicabili");
		m.put("Applicable Jobs", "Lavori applicabili");
		m.put("Applicable Names", "Nomi applicabili");
		m.put("Applicable Projects", "Progetti applicabili");
		m.put("Applicable Pull Requests", "Pull request applicabili");
		m.put("Applicable Senders", "Mittenti applicabili");
		m.put("Applicable Users", "Utenti applicabili");
		m.put("Application (client) ID", "ID applicazione (client)");
		m.put("Apply suggested change from code comment", "Applica la modifica suggerita dal commento al codice");
		m.put("Apply suggested changes from code comments", "Applica le modifiche suggerite dai commenti al codice");
		m.put("Approve", "Approva");
		m.put("Approved", "Approvato");
		m.put("Approved pull request \"{0}\" ({1})", "Pull request approvata \"{0}\" ({1})");
		m.put("Arbitrary scope", "Ambito arbitrario");
		m.put("Arbitrary type", "Tipo arbitrario");
		m.put("Arch Pull Command", "Comando di pull Arch");
		m.put("Archived", "Archiviato");
		m.put("Arguments", "Argomenti");
		m.put("Artifacts", "Artefatti");
		m.put("Artifacts to Retrieve", "Artefatti da recuperare");
		m.put("As long as a feature can be accessed via url, you can input part of the url to match and jump", 
			"Finché una funzionalità è accessibile tramite url, puoi inserire parte dell'url per corrispondere e saltare");
		m.put("Ascending", "Ascendente");
		m.put("Assignees", "Assegnatari");
		m.put("Assignees Issue Field", "Campo problema assegnatari");
		m.put("Assignees are expected to merge the pull request", "Gli assegnatari devono unire la pull request");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"Gli assegnatari hanno permessi di scrittura al codice e saranno responsabili dell'unione della pull request");
		m.put("Asymmetric", "Asimmetrico");
		m.put("At least one branch or tag should be selected", "Almeno un branch o tag deve essere selezionato");
		m.put("At least one choice need to be specified", "Almeno una scelta deve essere specificata");
		m.put("At least one email address should be configured, please add a new one first", "Almeno un indirizzo email deve essere configurato, aggiungine uno nuovo prima");
		m.put("At least one email address should be specified", "Almeno un indirizzo email deve essere specificato");
		m.put("At least one entry should be specified", "Almeno un elemento deve essere specificato");
		m.put("At least one event type needs to be selected", "Almeno un tipo di evento deve essere selezionato");
		m.put("At least one field needs to be specified", "Almeno un campo deve essere specificato");
		m.put("At least one project should be authorized", "Almeno un progetto deve essere autorizzato");
		m.put("At least one project should be selected", "Almeno un progetto deve essere selezionato");
		m.put("At least one repository should be selected", "Almeno un repository deve essere selezionato");
		m.put("At least one role is required", "Almeno un ruolo è richiesto");
		m.put("At least one role must be selected", "Almeno un ruolo deve essere selezionato");
		m.put("At least one state should be specified", "Almeno uno stato deve essere specificato");
		m.put("At least one tab should be added", "Almeno una scheda deve essere aggiunta");
		m.put("At least one user search base should be specified", "Almeno una base di ricerca utente deve essere specificata");
		m.put("At least one value needs to be specified", "Almeno un valore deve essere specificato");
		m.put("At least two columns need to be defined", "Almeno due colonne devono essere definite");
		m.put("Attachment", "Allegato");
		m.put("Attributes", "Attributi");
		m.put("Attributes (can only be edited when agent is online)", "Attributi (modificabili solo quando l'agente è online)");
		m.put("Attributes saved", "Attributi salvati");
		m.put("Audit", "Audit");
		m.put("Audit Log", "Registro di controllo");
		m.put("Audit Setting", "Impostazione di controllo");
		m.put("Audit log will be preserved for the specified number of days. This setting applies to all audit events, including system level and project level", 
			"Il registro di controllo sarà conservato per il numero di giorni specificato. Questa impostazione si applica a tutti gli eventi di controllo, inclusi quelli a livello di sistema e di progetto");
		m.put("Auth Source", "Sorgente di autenticazione");
		m.put("Authenticate to Bitbucket Cloud", "Autenticati su Bitbucket Cloud");
		m.put("Authenticate to GitHub", "Autenticati su GitHub");
		m.put("Authenticate to GitLab", "Autenticati su GitLab");
		m.put("Authenticate to Gitea", "Autenticati su Gitea");
		m.put("Authenticate to JIRA cloud", "Autenticati su JIRA Cloud");
		m.put("Authenticate to YouTrack", "Autenticati su YouTrack");
		m.put("Authentication", "Autenticazione");
		m.put("Authentication Required", "Autenticazione richiesta");
		m.put("Authentication Test", "Test di autenticazione");
		m.put("Authentication Token", "Token di autenticazione");
		m.put("Authenticator", "Autenticatore");
		m.put("Authenticator Bean", "Bean autenticatore");
		m.put("Author", "Autore");
		m.put("Author date", "Data dell'autore");
		m.put("Authored By", "Autore:");
		m.put("Authorization", "Autorizzazione");
		m.put("Authorizations", "Autorizzazioni");
		m.put("Authorize user...", "Autorizza utente...");
		m.put("Authorized Projects", "Progetti autorizzati");
		m.put("Authorized Roles", "Ruoli autorizzati");
		m.put("Auto Merge", "Unione automatica");
		m.put("Auto Spec", "Specifica automatica");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"Il controllo di aggiornamento automatico viene eseguito richiedendo un'immagine nel browser da onedev.io che indica la disponibilità di una nuova versione, con un colore che indica la gravità dell'aggiornamento. Funziona allo stesso modo delle richieste di immagini avatar di gravatar. Se disabilitato, si consiglia vivamente di controllare manualmente gli aggiornamenti di tanto in tanto (può essere fatto tramite il menu di aiuto in basso a sinistra dello schermo) per verificare se ci sono correzioni di sicurezza/critiche");
		m.put("Auto-discovered executor", "Esecutore auto-scoperto");
		m.put("Available Agent Tokens", "Token agente disponibili");
		m.put("Available Choices", "Scelte disponibili");
		m.put("Avatar", "Avatar");
		m.put("Avatar Service Url", "URL del servizio avatar");
		m.put("Avatar and name", "Avatar e nome");
		m.put("Back To Home", "Torna alla Home");
		m.put("Backlog", "Backlog");
		m.put("Backlog Base Query", "Query di base del backlog");
		m.put("Backup", "Backup");
		m.put("Backup Now", "Esegui backup ora");
		m.put("Backup Schedule", "Pianificazione del backup");
		m.put("Backup Setting", "Impostazioni del backup");
		m.put("Backup Setting Holder", "Titolare delle impostazioni del backup");
		m.put("Backup settings updated", "Impostazioni del backup aggiornate");
		m.put("Bare Metal", "Bare Metal");
		m.put("Base", "Base");
		m.put("Base Gpg Key", "Chiave Gpg di base");
		m.put("Base Query", "Query di base");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Formato PEM codificato in Base64, che inizia con -----BEGIN CERTIFICATE----- e termina con -----END CERTIFICATE-----");
		m.put("Basic Info", "Informazioni di base");
		m.put("Basic Settings", "Impostazioni di base");
		m.put("Basic settings updated", "Impostazioni di base aggiornate");
		m.put("Batch Edit All Queried Issues", "Modifica in batch di tutte le issue interrogate");
		m.put("Batch Edit Selected Issues", "Modifica in batch delle issue selezionate");
		m.put("Batch Editing {0} Issues", "Modifica in batch di {0} issue");
		m.put("Batched suggestions", "Suggerimenti in batch");
		m.put("Before modification", "Prima della modifica");
		m.put("Belonging Groups", "Gruppi di appartenenza");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"Di seguito sono riportati alcuni criteri comuni. Digita nella casella di ricerca sopra per visualizzare l'elenco completo e le combinazioni disponibili.");
		m.put("Below content is restored from an unsaved change. Clear to discard", "Il contenuto sottostante è stato ripristinato da una modifica non salvata. Cancella per scartare");
		m.put("Below information will also be sent", "Le informazioni sottostanti saranno anche inviate");
		m.put("Binary file.", "File binario.");
		m.put("Bitbucket App Password", "Password dell'app Bitbucket");
		m.put("Bitbucket Login Name", "Nome di accesso Bitbucket");
		m.put("Bitbucket Repositories to Import", "Repository Bitbucket da importare");
		m.put("Bitbucket Workspace", "Workspace Bitbucket");
		m.put("Bitbucket app password should be generated with permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>", 
			"La password dell'app Bitbucket deve essere generata con i permessi <b>account/read</b>, <b>repositories/read</b> e <b>issues:read</b>");
		m.put("Blame", "Blame");
		m.put("Blob", "Blob");
		m.put("Blob hash", "Hash del blob");
		m.put("Blob index version", "Versione dell'indice del blob");
		m.put("Blob name", "Nome del blob");
		m.put("Blob path", "Percorso del blob");
		m.put("Blob primary symbols", "Simboli primari del blob");
		m.put("Blob secondary symbols", "Simboli secondari del blob");
		m.put("Blob symbol list", "Elenco dei simboli del blob");
		m.put("Blob text", "Testo del blob");
		m.put("Blob unknown", "Blob sconosciuto");
		m.put("Blob upload invalid", "Caricamento del blob non valido");
		m.put("Blob upload unknown", "Caricamento del blob sconosciuto");
		m.put("Board", "Board");
		m.put("Board Columns", "Colonne del board");
		m.put("Board Spec", "Specifica del board");
		m.put("Boards", "Board");
		m.put("Body", "Corpo");
		m.put("Bold", "Grassetto");
		m.put("Both", "Entrambi");
		m.put("Bottom", "Inferiore");
		m.put("Branch", "Branch");
		m.put("Branch \"{0}\" already exists, please choose a different name", "Il branch \"{0}\" esiste già, scegli un nome diverso");
		m.put("Branch \"{0}\" created", "Branch \"{0}\" creato");
		m.put("Branch \"{0}\" deleted", "Branch \"{0}\" eliminato");
		m.put("Branch <a wicket:id=\"targetBranch\"></a> is up to date with all commits from <a wicket:id=\"sourceBranch\"></a>. Try <a wicket:id=\"swapBranches\">swap source and target</a> for the comparison.", 
			"Il branch <a wicket:id=\"targetBranch\"></a> è aggiornato con tutti i commit da <a wicket:id=\"sourceBranch\"></a>. Prova <a wicket:id=\"swapBranches\">scambia sorgente e destinazione</a> per il confronto.");
		m.put("Branch Choice Bean", "Bean di scelta del branch");
		m.put("Branch Name", "Nome del branch");
		m.put("Branch Protection", "Protezione del branch");
		m.put("Branch Revision", "Revisione del branch");
		m.put("Branch update", "Aggiornamento del branch");
		m.put("Branches", "Branch");
		m.put("Brand Setting Edit Bean", "Bean di modifica delle impostazioni del brand");
		m.put("Branding", "Branding");
		m.put("Branding settings updated", "Impostazioni di branding aggiornate");
		m.put("Browse Code", "Esplora codice");
		m.put("Browse code", "Esplora codice");
		m.put("Bug Report", "Segnalazione bug");
		m.put("Build", "Build");
		m.put("Build #{0} already finished", "Build #{0} già completata");
		m.put("Build #{0} deleted", "Build #{0} eliminata");
		m.put("Build #{0} not finished yet", "Build #{0} non ancora completata");
		m.put("Build Artifact Storage", "Storage degli artefatti di build");
		m.put("Build Commit", "Commit di build");
		m.put("Build Context", "Contesto di build");
		m.put("Build Description", "Descrizione della build");
		m.put("Build Filter", "Filtro di build");
		m.put("Build Image", "Immagine di build");
		m.put("Build Image (Kaniko)", "Immagine di build (Kaniko)");
		m.put("Build Management", "Gestione della build");
		m.put("Build Notification", "Notifica di build");
		m.put("Build Notification Template", "Template di notifica di build");
		m.put("Build Number", "Numero di build");
		m.put("Build On Behalf Of", "Build per conto di");
		m.put("Build Path", "Percorso di build");
		m.put("Build Preservation", "Preservazione della build");
		m.put("Build Preservations", "Preservazioni della build");
		m.put("Build Preservations Bean", "Bean di preservazioni della build");
		m.put("Build Preserve Rules", "Regole di preservazione della build");
		m.put("Build Provider", "Provider di build");
		m.put("Build Spec", "Specifica di build");
		m.put("Build Statistics", "Statistiche di build");
		m.put("Build Version", "Versione di build");
		m.put("Build Volume Storage Class", "Classe di storage del volume di build");
		m.put("Build Volume Storage Size", "Dimensione dello storage del volume di build");
		m.put("Build administrative permission for all jobs inside a project, including batch operations over multiple builds", 
			"Permesso amministrativo di build per tutti i job all'interno di un progetto, incluse operazioni batch su più build");
		m.put("Build docker image with docker buildx. This step can only be executed by server docker executor or remote docker executor, and it uses the buildx builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko step instead", 
			"Costruisci immagine docker con docker buildx. Questo step può essere eseguito solo da un executor docker server o un executor docker remoto, e utilizza il builder buildx specificato in questi executor per svolgere il lavoro. Per costruire immagini con l'executor Kubernetes, utilizza invece lo step kaniko");
		m.put("Build docker image with kaniko. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Costruisci immagine docker con kaniko. Questo step deve essere eseguito da un executor docker server, un executor docker remoto o un executor Kubernetes");
		m.put("Build duration statistics", "Statistiche sulla durata della build");
		m.put("Build frequency statistics", "Genera statistiche di frequenza di build");
		m.put("Build is successful", "La build è riuscita");
		m.put("Build list", "Elenco delle build");
		m.put("Build not exist or access denied", "La build non esiste o accesso negato");
		m.put("Build number", "Numero di build");
		m.put("Build preserve rules saved", "Regole di conservazione della build salvate");
		m.put("Build required for deletion. Submit pull request instead", "Build richiesta per l'eliminazione. Invia una pull request invece");
		m.put("Build required for this change. Please submit pull request instead", "Build richiesta per questa modifica. Si prega di inviare una pull request invece.");
		m.put("Build required for this change. Submit pull request instead", "Build richiesta per questa modifica. Invia una pull request invece");
		m.put("Build spec not defined", "Specifica della build non definita");
		m.put("Build spec not defined (import project: {0}, import revision: {1})", "Specifica della build non definita (importa progetto: {0}, importa revisione: {1})");
		m.put("Build spec not found in commit of this build", "Specifica della build non trovata nel commit di questa build");
		m.put("Build statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Le statistiche di build sono una funzionalità enterprise. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("Build version", "Versione della build");
		m.put("Build with Persistent Volume", "Build con Volume Persistente");
		m.put("Builds", "Build");
		m.put("Builds are {0}", "Le build sono {0}");
		m.put("Buildx Builder", "Buildx Builder");
		m.put("Built In Fields Bean", "Bean dei campi integrati");
		m.put("Burndown", "Burndown");
		m.put("Burndown chart", "Grafico Burndown");
		m.put("Button Image Url", "URL immagine del pulsante");
		m.put("By Group", "Per gruppo");
		m.put("By User", "Per utente");
		m.put("By day", "Per giorno");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"Per impostazione predefinita, il codice viene clonato tramite una credenziale generata automaticamente, che ha solo permessi di lettura sul progetto corrente. Nel caso in cui il job debba <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>inviare codice al server</a>, dovresti fornire una credenziale personalizzata con i permessi appropriati qui");
		m.put("By default issues of parent and child projects will also be listed. Use query <code>&quot;Project&quot; is current</code> to show issues only belong to this project", 
			"Per impostazione predefinita, verranno elencati anche i problemi dei progetti padre e figlio. Usa la query <code>&quot;Project&quot; is current</code> per mostrare solo i problemi appartenenti a questo progetto");
		m.put("By month", "Per mese");
		m.put("By week", "Per settimana");
		m.put("Bypass Certificate Check", "Ignora controllo certificato");
		m.put("CANCELLED", "CANCELLATO");
		m.put("CORS Allowed Origins", "Origini consentite CORS");
		m.put("CPD Report", "Rapporto CPD");
		m.put("CPU", "CPU");
		m.put("CPU Intensive Task Concurrency", "Concorrenza dei task intensivi per CPU");
		m.put("CPU capability in millis. This is normally (CPU cores)*1000", "Capacità della CPU in millis. Normalmente è (core CPU)*1000");
		m.put("Cache Key", "Chiave della cache");
		m.put("Cache Management", "Gestione della cache");
		m.put("Cache Paths", "Percorsi della cache");
		m.put("Cache Setting Bean", "Bean delle impostazioni della cache");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "La cache verrà eliminata per risparmiare spazio se non viene acceduta per questo numero di giorni");
		m.put("Calculating merge preview...", "Calcolo dell'anteprima di merge...");
		m.put("Callback URL", "URL di callback");
		m.put("Can Be Used By Jobs", "Può essere utilizzato dai job");
		m.put("Can Create Root Projects", "Può creare progetti root");
		m.put("Can Edit Estimated Time", "Può modificare il tempo stimato");
		m.put("Can not convert root user to service account", "Impossibile convertire l'utente root in account di servizio");
		m.put("Can not convert yourself to service account", "Impossibile convertirti in account di servizio");
		m.put("Can not delete default branch", "Non è possibile eliminare il branch predefinito");
		m.put("Can not delete root account", "Non è possibile eliminare l'account root");
		m.put("Can not delete yourself", "Non è possibile eliminare te stesso");
		m.put("Can not disable root account", "Non è possibile disabilitare l'account root");
		m.put("Can not disable yourself", "Non è possibile disabilitare te stesso");
		m.put("Can not find issue board: ", "Impossibile trovare la bacheca dei problemi:");
		m.put("Can not move project \"{0}\" to be under itself or its descendants", "Non è possibile spostare il progetto \"{0}\" sotto se stesso o i suoi discendenti");
		m.put("Can not perform this operation now", "Non è possibile eseguire questa operazione ora");
		m.put("Can not reset password for service account or disabled user", "Impossibile reimpostare la password per account di servizio o utente disabilitato");
		m.put("Can not reset password for user authenticating via external system", "Impossibile reimpostare la password per utente che si autentica tramite sistema esterno");
		m.put("Can not save malformed query", "Non è possibile salvare una query malformata");
		m.put("Can not use current or descendant project as parent", "Non è possibile utilizzare il progetto corrente o un discendente come genitore");
		m.put("Can only compare with common ancestor when different projects are involved", "È possibile confrontare solo con un antenato comune quando sono coinvolti progetti diversi");
		m.put("Cancel", "Annulla");
		m.put("Cancel All Queried Builds", "Annulla tutte le build interrogate");
		m.put("Cancel Selected Builds", "Annulla le build selezionate");
		m.put("Cancel invitation", "Annulla invito");
		m.put("Cancel request submitted", "Richiesta di annullamento inviata");
		m.put("Cancel this build", "Annulla questa build");
		m.put("Cancelled", "Annullato");
		m.put("Cancelled By", "Annullato da");
		m.put("Case Sensitive", "Sensibile alle maiuscole");
		m.put("Certificates to Trust", "Certificati da fidare");
		m.put("Change", "Modifica");
		m.put("Change Detection Excludes", "Esclusioni di rilevamento delle modifiche");
		m.put("Change My Password", "Cambia la mia password");
		m.put("Change To", "Cambia in");
		m.put("Change already merged", "Modifica già unita");
		m.put("Change not updated yet", "Modifica non ancora aggiornata");
		m.put("Change property <code>serverUrl</code> in file <code>conf/agent.properties</code> if necessary. The default value is taken from OneDev server url specified in <i>Administration / System Setting</i>", 
			"Modifica la proprietà <code>serverUrl</code> nel file <code>conf/agent.properties</code> se necessario. Il valore predefinito è preso dall'URL del server OneDev specificato in <i>Amministrazione / Impostazioni di sistema</i>");
		m.put("Change to another field", "Cambia in un altro campo");
		m.put("Change to another state", "Cambia in un altro stato");
		m.put("Change to another value", "Cambia in un altro valore");
		m.put("Changes since last review", "Modifiche dall'ultima revisione");
		m.put("Changes since last visit", "Modifiche dall'ultima visita");
		m.put("Changes since this action", "Modifiche da questa azione");
		m.put("Changes since this comment", "Modifiche da questo commento");
		m.put("Channel Notification", "Notifica del canale");
		m.put("Chart Metadata", "Metadati del grafico");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"Consulta <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">la guida di GitHub</a> su come generare e utilizzare le chiavi GPG per firmare i tuoi commit");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including instructions on how to run agent as service", 
			"Consulta <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">la gestione degli agenti</a> per i dettagli, incluse le istruzioni su come eseguire l'agente come servizio");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including list of supported environment variables", 
			"Consulta <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">la gestione degli agenti</a> per i dettagli, inclusa la lista delle variabili di ambiente supportate");
		m.put("Check Commit Message Footer", "Controlla il piè di pagina del messaggio di commit");
		m.put("Check Incoming Email", "Controlla email in arrivo");
		m.put("Check Issue Integrity", "Controlla l'integrità dei problemi");
		m.put("Check Update", "Controlla aggiornamento");
		m.put("Check Workflow Integrity", "Controlla l'integrità del workflow");
		m.put("Check out to local workspace", "Effettua il checkout nella workspace locale");
		m.put("Check this to compare right side with common ancestor of left and right", "Seleziona questa opzione per confrontare il lato destro con l'antenato comune di sinistra e destra");
		m.put("Check this to enforce two-factor authentication for all users in the system", "Seleziona questa opzione per imporre l'autenticazione a due fattori per tutti gli utenti del sistema");
		m.put("Check this to enforce two-factor authentication for all users in this group", "Seleziona questa opzione per imporre l'autenticazione a due fattori per tutti gli utenti di questo gruppo");
		m.put("Check this to prevent branch creation", "Seleziona questa opzione per impedire la creazione di branch");
		m.put("Check this to prevent branch deletion", "Seleziona questa opzione per impedire l'eliminazione di branch");
		m.put("Check this to prevent forced push", "Seleziona questa opzione per impedire il push forzato");
		m.put("Check this to prevent tag creation", "Seleziona questa opzione per impedire la creazione di tag");
		m.put("Check this to prevent tag deletion", "Seleziona questa opzione per impedire l'eliminazione di tag");
		m.put("Check this to prevent tag update", "Seleziona questa opzione per impedire l'aggiornamento di tag");
		m.put("Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>. Note this is applicable for non-merge commits", 
			"Seleziona questa opzione per richiedere <a href='https://www.conventionalcommits.org' target='_blank'>commit convenzionali</a>. Nota che è applicabile ai commit non di merge");
		m.put("Check this to require valid signature of head commit", "Seleziona questa opzione per richiedere una firma valida del commit principale");
		m.put("Check this to retrieve Git LFS files", "Seleziona questa opzione per recuperare i file Git LFS");
		m.put("Checkbox", "Casella di controllo");
		m.put("Checking field values...", "Verifica dei valori dei campi...");
		m.put("Checking fields...", "Verifica dei campi...");
		m.put("Checking state and field ordinals...", "Verifica dello stato e degli ordinali dei campi...");
		m.put("Checking state...", "Verifica dello stato...");
		m.put("Checkout Code", "Checkout del codice");
		m.put("Checkout Path", "Percorso di checkout");
		m.put("Checkout Pull Request Head", "Checkout della testa della pull request");
		m.put("Checkout Pull Request Merge Preview", "Anteprima di merge della pull request in checkout");
		m.put("Checkstyle Report", "Report di Checkstyle");
		m.put("Cherry-Pick", "Cherry-Pick");
		m.put("Cherry-picked successfully", "Cherry-pick completato con successo");
		m.put("Child Projects", "Progetti figli");
		m.put("Child Projects Of", "Progetti figli di");
		m.put("Choice Provider", "Provider di scelta");
		m.put("Choose", "Scegli");
		m.put("Choose JIRA project to import issues from", "Scegli il progetto JIRA da cui importare i problemi");
		m.put("Choose Revision", "Scegli la revisione");
		m.put("Choose YouTrack project to import issues from", "Scegli il progetto YouTrack da cui importare i problemi");
		m.put("Choose a project...", "Scegli un progetto...");
		m.put("Choose a user...", "Scegli un utente...");
		m.put("Choose branch...", "Scegli il branch...");
		m.put("Choose branches...", "Scegli i branch...");
		m.put("Choose build...", "Scegli la build...");
		m.put("Choose file", "Scegli il file");
		m.put("Choose group...", "Scegli il gruppo...");
		m.put("Choose groups...", "Scegli i gruppi...");
		m.put("Choose issue...", "Scegli il problema...");
		m.put("Choose issues...", "Scegli problemi...");
		m.put("Choose iteration...", "Scegli l'iterazione...");
		m.put("Choose iterations...", "Scegli le iterazioni...");
		m.put("Choose job...", "Scegli il job...");
		m.put("Choose jobs...", "Scegli i job...");
		m.put("Choose project", "Scegli il progetto");
		m.put("Choose projects...", "Scegli i progetti...");
		m.put("Choose pull request...", "Scegli la pull request...");
		m.put("Choose repository", "Scegli il repository");
		m.put("Choose role...", "Scegli il ruolo...");
		m.put("Choose roles...", "Scegli i ruoli...");
		m.put("Choose users...", "Scegli gli utenti...");
		m.put("Choose...", "Scegli...");
		m.put("Circular build spec imports ({0})", "Importazioni circolari delle specifiche di build ({0})");
		m.put("Click to select a commit, or shift-click to select multiple commit", "Clicca per selezionare un commit, o shift-clicca per selezionare più commit");
		m.put("Click to show comment of marked text", "Clicca per mostrare il commento del testo evidenziato");
		m.put("Click to show issue details", "Clicca per mostrare i dettagli del problema");
		m.put("Client ID of this OneDev instance registered in Google cloud", "ID client di questa istanza di OneDev registrata nel cloud di Google");
		m.put("Client Id", "ID client");
		m.put("Client Secret", "Segreto client");
		m.put("Client secret of this OneDev instance registered in Google cloud", "Segreto client di questa istanza di OneDev registrata nel cloud di Google");
		m.put("Clippy Report", "Report di Clippy");
		m.put("Clone", "Clona");
		m.put("Clone Credential", "Credenziali di clonazione");
		m.put("Clone Depth", "Profondità di clonazione");
		m.put("Clone in IntelliJ", "Clona in IntelliJ");
		m.put("Clone in VSCode", "Clona in VSCode");
		m.put("Close", "Chiudi");
		m.put("Close Iteration", "Chiudi iterazione");
		m.put("Close this iteration", "Chiudi questa iterazione");
		m.put("Closed", "Chiuso");
		m.put("Closed Issue State", "Stato del problema chiuso");
		m.put("Closest due date", "Data di scadenza più vicina");
		m.put("Clover Coverage Report", "Report di copertura Clover");
		m.put("Cluster Role", "Ruolo del cluster");
		m.put("Cluster Setting", "Impostazione del cluster");
		m.put("Cluster setting", "Impostazione del cluster");
		m.put("Clustered Servers", "Server clusterizzati");
		m.put("Cobertura Coverage Report", "Report di copertura Cobertura");
		m.put("Code", "Codice");
		m.put("Code Analysis", "Analisi del codice");
		m.put("Code Analysis Setting", "Impostazione dell'analisi del codice");
		m.put("Code Analysis Settings", "Impostazioni dell'analisi del codice");
		m.put("Code Changes", "Modifiche al codice");
		m.put("Code Comment", "Commento del codice");
		m.put("Code Comment Management", "Gestione dei commenti del codice");
		m.put("Code Comments", "Commenti del codice");
		m.put("Code Compare", "Confronto del codice");
		m.put("Code Contribution Statistics", "Statistiche di contributo al codice");
		m.put("Code Coverage", "Copertura del codice");
		m.put("Code Line Statistics", "Statistiche delle righe di codice");
		m.put("Code Management", "Gestione del codice");
		m.put("Code Privilege", "Privilegio del codice");
		m.put("Code Problem Statistics", "Statistiche dei problemi del codice");
		m.put("Code Search", "Ricerca del codice");
		m.put("Code Statistics", "Statistiche del codice");
		m.put("Code analysis settings updated", "Impostazioni dell'analisi del codice aggiornate");
		m.put("Code changes since...", "Modifiche al codice da...");
		m.put("Code clone or download", "Clona o scarica il codice");
		m.put("Code comment", "Commento del codice");
		m.put("Code comment #{0} deleted", "Commento del codice #{0} eliminato");
		m.put("Code comment administrative permission inside a project, including batch operations over multiple code comments", 
			"Permesso amministrativo per i commenti del codice all'interno di un progetto, inclusa l'operazione batch su più commenti del codice");
		m.put("Code commit", "Commit del codice");
		m.put("Code is committed", "Codice committato");
		m.put("Code push", "Push del codice");
		m.put("Code read permission is required to import build spec (import project: {0}, import revision: {1})", 
			"È richiesto il permesso di lettura del codice per importare la specifica di build (progetto importato: {0}, revisione importata: {1})");
		m.put("Code suggestion", "Suggerimento del codice");
		m.put("Code write permission is required for this operation", "È richiesto il permesso di scrittura del codice per questa operazione");
		m.put("Collapse all", "Comprimi tutto");
		m.put("Color", "Colore");
		m.put("Columns", "Colonne");
		m.put("Command Palette", "Palette dei Comandi");
		m.put("Commands", "Comandi");
		m.put("Comment", "Commento");
		m.put("Comment Content", "Contenuto del commento");
		m.put("Comment on File", "Commento sul file");
		m.put("Comment too long", "Commento troppo lungo");
		m.put("Commented code is outdated", "Il codice commentato è obsoleto");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "Commentato sul file \"{0}\" nel progetto \"{1}\"");
		m.put("Commented on issue \"{0}\" ({1})", "Commentato sul problema \"{0}\" ({1})");
		m.put("Commented on pull request \"{0}\" ({1})", "Commentato sulla pull request \"{0}\" ({1})");
		m.put("Comments", "Commenti");
		m.put("Commit", "Commit");
		m.put("Commit &amp; Insert", "Commit &amp; Inserisci");
		m.put("Commit Batched Suggestions", "Commit Suggerimenti in Batch");
		m.put("Commit Message", "Messaggio di Commit");
		m.put("Commit Message Bean", "Bean del Messaggio di Commit");
		m.put("Commit Message Fix Patterns", "Modelli di Correzione del Messaggio di Commit");
		m.put("Commit Message Footer Pattern", "Modello del Footer del Messaggio di Commit");
		m.put("Commit Notification", "Notifica di Commit");
		m.put("Commit Notification Template", "Template di Notifica di Commit");
		m.put("Commit Scopes", "Ambiti di Commit");
		m.put("Commit Signature Required", "Firma del Commit Necessaria");
		m.put("Commit Suggestion", "Suggerimento di Commit");
		m.put("Commit Types", "Tipi di Commit");
		m.put("Commit Types For Footer Check", "Tipi di Commit per Verifica del Footer");
		m.put("Commit Your Change", "Committa la tua Modifica");
		m.put("Commit date", "Data del Commit");
		m.put("Commit hash", "Hash del Commit");
		m.put("Commit history of current path", "Storico dei Commit del percorso corrente");
		m.put("Commit index version", "Versione dell'indice del Commit");
		m.put("Commit message can be used to fix issues by prefixing and suffixing issue number with specified pattern. Each line of the commit message will be matched against each entry defined here to find issues to be fixed", 
			"Il messaggio di commit può essere usato per correggere problemi prefissando e suffissando il numero del problema con il modello specificato. Ogni riga del messaggio di commit sarà confrontata con ogni voce definita qui per trovare i problemi da correggere");
		m.put("Commit not exist or access denied", "Commit inesistente o accesso negato");
		m.put("Commit of the build is missing", "Il commit della build è mancante");
		m.put("Commit signature required but no GPG signing key specified", "Firma del commit necessaria ma nessuna chiave di firma GPG specificata");
		m.put("Commit suggestion", "Suggerimento di commit");
		m.put("Commits", "Commit");
		m.put("Commits are taken from default branch of non-forked repositories", "I commit sono presi dal branch predefinito dei repository non forkati");
		m.put("Commits generated by OneDev previously will be shown as unverified if this key is deleted. Type <code>yes</code> below if you want to continue.", 
			"I commit generati precedentemente da OneDev saranno mostrati come non verificati se questa chiave viene eliminata. Digita <code>yes</code> qui sotto se vuoi continuare.");
		m.put("Commits were merged into target branch", "I commit sono stati uniti nel branch di destinazione");
		m.put("Commits were merged into target branch outside of this pull request", "I commit sono stati uniti nel branch di destinazione al di fuori di questa pull request");
		m.put("Commits were rebased onto target branch", "I commit sono stati rebaseati sul branch di destinazione");
		m.put("Commits were squashed into a single commit on target branch", "I commit sono stati compressi in un unico commit sul branch di destinazione");
		m.put("Committed After", "Commitato Dopo");
		m.put("Committed Before", "Commitato Prima");
		m.put("Committed By", "Commitato Da");
		m.put("Committer", "Committer");
		m.put("Compare", "Confronta");
		m.put("Compare with base revision", "Confronta con la revisione base");
		m.put("Compare with this parent", "Confronta con questo genitore");
		m.put("Concurrency", "Concorrenza");
		m.put("Condition", "Condizione");
		m.put("Confidential", "Confidenziale");
		m.put("Config File", "File di Configurazione");
		m.put("Configuration Discovery Url", "Url di Scoperta della Configurazione");
		m.put("Configure your scope to use below registry", "Configura il tuo ambito per usare il registro sottostante");
		m.put("Confirm Approve", "Conferma Approvazione");
		m.put("Confirm Delete Source Branch", "Conferma Eliminazione del Branch Sorgente");
		m.put("Confirm Discard", "Conferma Scarto");
		m.put("Confirm Reopen", "Conferma Riapertura");
		m.put("Confirm Request For Changes", "Conferma Richiesta di Modifiche");
		m.put("Confirm Restore Source Branch", "Conferma Ripristino del Branch Sorgente");
		m.put("Confirm password here", "Conferma la password qui");
		m.put("Confirm your action", "Conferma la tua azione");
		m.put("Connect New Agent", "Connetti Nuovo Agente");
		m.put("Connect with your SSO account", "Connettiti con il tuo account SSO");
		m.put("Contact Email", "Email di Contatto");
		m.put("Contact Name", "Nome di Contatto");
		m.put("Container Image", "Immagine del Container");
		m.put("Container Image(s)", "Immagine(i) del Container");
		m.put("Container default", "Container predefinito");
		m.put("Content", "Contenuto");
		m.put("Content Type", "Tipo di Contenuto");
		m.put("Content is identical", "Il contenuto è identico");
		m.put("Continue to add other user after create", "Continua ad aggiungere altri utenti dopo la creazione");
		m.put("Contributed settings", "Impostazioni Contribuite");
		m.put("Contributions", "Contributi");
		m.put("Contributions to {0} branch, excluding merge commits", "Contributi al branch {0}, esclusi i commit di merge");
		m.put("Convert All Queried to Service Accounts", "Converti Tutti i Richiesti in Account di Servizio");
		m.put("Convert Selected to Service Accounts", "Converti Selezionati in Account di Servizio");
		m.put("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm", 
			"Convertire in account di servizio rimuoverà password, indirizzi email, tutte le assegnazioni e le osservazioni. Digita <code>yes</code> per confermare");
		m.put("Copy", "Copia");
		m.put("Copy All Queried Issues To...", "Copia Tutti i Problemi Interrogati In...");
		m.put("Copy Files with SCP", "Copia File con SCP");
		m.put("Copy Selected Issues To...", "Copia Problemi Selezionati In...");
		m.put("Copy dashboard", "Copia dashboard");
		m.put("Copy issue number and title", "Copia numero e titolo del problema");
		m.put("Copy public key", "Copia chiave pubblica");
		m.put("Copy selected text to clipboard", "Copia testo selezionato negli appunti");
		m.put("Copy to clipboard", "Copia negli appunti");
		m.put("Count", "Conteggio");
		m.put("Coverage Statistics", "Statistiche di Copertura");
		m.put("Covered", "Coperto");
		m.put("Covered by tests", "Coperto dai test");
		m.put("Cppcheck Report", "Rapporto Cppcheck");
		m.put("Cpu Limit", "Limite CPU");
		m.put("Cpu Request", "Richiesta CPU");
		m.put("Create", "Crea");
		m.put("Create Administrator Account", "Crea Account Amministratore");
		m.put("Create Branch", "Crea Branch");
		m.put("Create Branch Bean", "Crea Bean del Branch");
		m.put("Create Branch Bean With Revision", "Crea Bean del Branch con Revisione");
		m.put("Create Child Project", "Crea Progetto Figlio");
		m.put("Create Child Projects", "Crea Progetti Figli");
		m.put("Create Issue", "Crea Problema");
		m.put("Create Iteration", "Crea Iterazione");
		m.put("Create Merge Commit", "Crea Commit di Merge");
		m.put("Create Merge Commit If Necessary", "Crea Commit di Merge Se Necessario");
		m.put("Create New", "Crea Nuovo");
		m.put("Create New File", "Crea Nuovo File");
		m.put("Create New User", "Crea Nuovo Utente");
		m.put("Create Project", "Crea Progetto");
		m.put("Create Pull Request", "Crea Pull Request");
		m.put("Create Pull Request for This Change", "Crea Pull Request per Questa Modifica");
		m.put("Create Tag", "Crea Tag");
		m.put("Create Tag Bean", "Crea Bean Tag");
		m.put("Create Tag Bean With Revision", "Crea Bean Tag con Revisione");
		m.put("Create User", "Crea Utente");
		m.put("Create body", "Crea corpo");
		m.put("Create branch <b>{0}</b> from {1}", "Crea branch <b>{0}</b> da {1}");
		m.put("Create child projects under a project", "Crea progetti figli sotto un progetto");
		m.put("Create issue", "Crea problema");
		m.put("Create merge commit", "Crea commit di merge");
		m.put("Create merge commit if necessary", "Crea commit di merge se necessario");
		m.put("Create new issue", "Crea nuovo problema");
		m.put("Create tag", "Crea tag");
		m.put("Create tag <b>{0}</b> from {1}", "Crea tag <b>{0}</b> da {1}");
		m.put("Created At", "Creato il");
		m.put("Creation of this branch is prohibited per branch protection rule", "La creazione di questo branch è vietata secondo la regola di protezione del branch");
		m.put("Critical", "Critico");
		m.put("Critical Severity", "Gravità Critica");
		m.put("Cron Expression", "Espressione Cron");
		m.put("Cron schedule", "Programma Cron");
		m.put("Curl Location", "Posizione Curl");
		m.put("Current Iteration", "Iterazione Corrente");
		m.put("Current Value", "Valore Corrente");
		m.put("Current avatar", "Avatar Corrente");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"Il contesto corrente è diverso dal contesto in cui è stato aggiunto questo commento, clicca per mostrare il contesto del commento");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"Il contesto corrente è diverso dal contesto in cui è stata aggiunta questa risposta, clicca per mostrare il contesto della risposta");
		m.put("Current context is different from this action, click to show the comment context", "Il contesto corrente è diverso da questa azione, clicca per mostrare il contesto del commento");
		m.put("Current platform", "Piattaforma Corrente");
		m.put("Current project", "Progetto Corrente");
		m.put("Custom Linux Shell", "Shell Linux Personalizzata");
		m.put("DISCARDED", "SCARTATO");
		m.put("Dashboard Share Bean", "Bean Condivisione Dashboard");
		m.put("Dashboard name", "Nome Dashboard");
		m.put("Dashboards", "Dashboard");
		m.put("Database Backup", "Backup Database");
		m.put("Date", "Data");
		m.put("Date Time", "Data e Ora");
		m.put("Days Per Week", "Giorni Per Settimana");
		m.put("Deactivate Subscription", "Disattiva Abbonamento");
		m.put("Deactivate Trial Subscription", "Disattiva Abbonamento di Prova");
		m.put("Default", "Predefinito");
		m.put("Default (Shell on Linux, Batch on Windows)", "Predefinito (Shell su Linux, Batch su Windows)");
		m.put("Default Assignees", "Assegnatari Predefiniti");
		m.put("Default Boards", "Bacheche Predefinite");
		m.put("Default Fixed Issue Filter", "Filtro Problemi Risolti Predefinito");
		m.put("Default Fixed Issue Filters", "Filtri Problemi Risolti Predefiniti");
		m.put("Default Fixed Issue Filters Bean", "Bean Filtri Problemi Risolti Predefiniti");
		m.put("Default Group", "Gruppo Predefinito");
		m.put("Default Issue Boards", "Bacheche Problemi Predefinite");
		m.put("Default Merge Strategy", "Strategia di Merge Predefinita");
		m.put("Default Multi Value Provider", "Provider Valore Multiplo Predefinito");
		m.put("Default Project", "Progetto Predefinito");
		m.put("Default Project Setting", "Impostazione Progetto Predefinita");
		m.put("Default Roles", "Ruoli Predefiniti");
		m.put("Default Roles Bean", "Bean Ruoli Predefiniti");
		m.put("Default Value", "Valore Predefinito");
		m.put("Default Value Provider", "Provider Valore Predefinito");
		m.put("Default Values", "Valori Predefiniti");
		m.put("Default branch", "Branch Predefinito");
		m.put("Default branding settings restored", "Impostazioni di branding predefinite ripristinate");
		m.put("Default fixed issue filters saved", "Filtri problemi risolti predefiniti salvati");
		m.put("Default merge strategy", "Strategia di merge predefinita");
		m.put("Default roles affect default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"I ruoli predefiniti influenzano i permessi predefiniti concessi a tutti nel sistema. I permessi predefiniti effettivi saranno <b class='text-warning'>tutti i permessi</b> contenuti nei ruoli predefiniti di questo progetto e di tutti i suoi progetti genitori");
		m.put("Define all custom issue fields here. Each project can decide to use all or a subset of these fields via its issue transition setting. <b class=\"text-warning\">NOTE: </b> Newly defined fields by default only appear in new issues. Batch edit existing issues from issue list page if you want them to have these new fields", 
			"Definisci qui tutti i campi personalizzati dei problemi. Ogni progetto può decidere di utilizzare tutti o un sottoinsieme di questi campi tramite la sua impostazione di transizione dei problemi. <b class=\"text-warning\">NOTA: </b> I campi appena definiti per impostazione predefinita appaiono solo nei nuovi problemi. Modifica in batch i problemi esistenti dalla pagina dell'elenco dei problemi se desideri che abbiano questi nuovi campi");
		m.put("Define all custom issue states here. The first state will be used as initial state of created issues", 
			"Definisci qui tutti gli stati personalizzati dei problemi. Il primo stato sarà utilizzato come stato iniziale dei problemi creati");
		m.put("Define branch protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given branch and user, the first matching rule will take effect", 
			"Definisci le regole di protezione del branch. Le regole definite nel progetto genitore sono considerate definite dopo le regole definite qui. Per un dato branch e utente, la prima regola corrispondente avrà effetto");
		m.put("Define default issue boards for all projects here. A certain project can override this setting to define its own issue boards.", 
			"Definisci qui le bacheche dei problemi predefinite per tutti i progetti. Un certo progetto può sovrascrivere questa impostazione per definire le proprie bacheche dei problemi.");
		m.put("Define how issue states should be transited from one to another, either manually or automatically when some events happen. And the rule can be configured to apply to certain projects and issues via the applicable issues setting", 
			"Definisci come gli stati dei problemi dovrebbero essere transitati da uno all'altro, manualmente o automaticamente quando si verificano alcuni eventi. E la regola può essere configurata per applicarsi a determinati progetti e problemi tramite l'impostazione dei problemi applicabili");
		m.put("Define issue templates here. When a new issue is created, the first matching template will be used.", 
			"Definisci qui i modelli di problemi. Quando viene creato un nuovo problema, verrà utilizzato il primo modello corrispondente.");
		m.put("Define labels to be assigned to project, build or pull request. For issues, custom fields can be used which is much more powerful than labels", 
			"Definisci etichette da assegnare a progetto, build o pull request. Per i problemi, possono essere utilizzati campi personalizzati che sono molto più potenti delle etichette");
		m.put("Define properties to be used in build spec. Properties will be inherited by child projects, and can be overridden by child properties with same name.", 
			"Definisci proprietà da utilizzare nella specifica di build. Le proprietà saranno ereditate dai progetti figli e possono essere sovrascritte dalle proprietà figlie con lo stesso nome.");
		m.put("Define rules to preserve builds. A build will be preserved as long as one rule defined here or in parent projects preserves it. All builds will be preserved if no rules are defined here and in parent projects", 
			"Definisci regole per preservare le build. Una build sarà preservata finché una regola definita qui o nei progetti genitori la preserva. Tutte le build saranno preservate se non sono definite regole qui e nei progetti genitori");
		m.put("Define tag protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given tag and user, the first matching rule will take effect", 
			"Definisci regole di protezione dei tag. Le regole definite nel progetto genitore sono considerate definite dopo le regole definite qui. Per un dato tag e utente, la prima regola corrispondente avrà effetto");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"Ritardo per il primo tentativo in secondi. Il ritardo dei tentativi successivi sarà calcolato utilizzando un back-off esponenziale basato su questo valore");
		m.put("Delete", "Elimina");
		m.put("Delete All", "Elimina Tutto");
		m.put("Delete All Queried Builds", "Elimina Tutte le Build Interrogate");
		m.put("Delete All Queried Comments", "Elimina Tutti i Commenti Interrogati");
		m.put("Delete All Queried Issues", "Elimina Tutti i Problemi Interrogati");
		m.put("Delete All Queried Packages", "Elimina Tutti i Pacchetti Interrogati");
		m.put("Delete All Queried Projects", "Elimina Tutti i Progetti Interrogati");
		m.put("Delete All Queried Pull Requests", "Elimina Tutte le Pull Request Interrogate");
		m.put("Delete All Queried Users", "Elimina Tutti gli Utenti Interrogati");
		m.put("Delete Build", "Elimina Build");
		m.put("Delete Comment", "Elimina Commento");
		m.put("Delete Pull Request", "Elimina Pull Request");
		m.put("Delete SSO account here to reconnect corresponding SSO subject upon next login. Note that SSO subject with verified email will be connected to user with same verified email automatically", 
			"Elimina l'account SSO qui per ricollegare il soggetto SSO corrispondente al prossimo accesso. Nota che il soggetto SSO con email verificata sarà collegato automaticamente all'utente con la stessa email verificata");
		m.put("Delete Selected", "Elimina Selezionati");
		m.put("Delete Selected Builds", "Elimina Build Selezionate");
		m.put("Delete Selected Comments", "Elimina Commenti Selezionati");
		m.put("Delete Selected Issues", "Elimina Problemi Selezionati");
		m.put("Delete Selected Packages", "Elimina i pacchetti selezionati");
		m.put("Delete Selected Projects", "Elimina i progetti selezionati");
		m.put("Delete Selected Pull Requests", "Elimina le richieste di pull selezionate");
		m.put("Delete Selected Users", "Elimina gli utenti selezionati");
		m.put("Delete Source Branch", "Elimina il branch sorgente");
		m.put("Delete Source Branch After Merge", "Elimina il branch sorgente dopo il merge");
		m.put("Delete dashboard", "Elimina la dashboard");
		m.put("Delete from branch {0}", "Elimina dal branch {0}");
		m.put("Delete this", "Elimina questo");
		m.put("Delete this GPG key", "Elimina questa chiave GPG");
		m.put("Delete this access token", "Elimina questo token di accesso");
		m.put("Delete this branch", "Elimina questo branch");
		m.put("Delete this executor", "Elimina questo executor");
		m.put("Delete this field", "Elimina questo campo");
		m.put("Delete this import", "Elimina questa importazione");
		m.put("Delete this iteration", "Elimina questa iterazione");
		m.put("Delete this key", "Elimina questa chiave");
		m.put("Delete this link", "Elimina questo collegamento");
		m.put("Delete this rule", "Elimina questa regola");
		m.put("Delete this secret", "Elimina questo segreto");
		m.put("Delete this state", "Elimina questo stato");
		m.put("Delete this tag", "Elimina questo tag");
		m.put("Delete this value", "Elimina questo valore");
		m.put("Deleted source branch", "Branch sorgente eliminato");
		m.put("Deletion not allowed due to branch protection rule", "Eliminazione non consentita a causa della regola di protezione del branch");
		m.put("Deletion not allowed due to tag protection rule", "Eliminazione non consentita a causa della regola di protezione del tag");
		m.put("Deletions", "Eliminazioni");
		m.put("Denied", "Negato");
		m.put("Dependencies & Services", "Dipendenze e servizi");
		m.put("Dependency Management", "Gestione delle dipendenze");
		m.put("Dependency job finished", "Job di dipendenza completato");
		m.put("Dependent Fields", "Campi dipendenti");
		m.put("Depends on <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>", "Dipende da <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>");
		m.put("Descending", "Discendente");
		m.put("Description", "Descrizione");
		m.put("Description Template", "Modello di descrizione");
		m.put("Description Templates", "Modelli di descrizione");
		m.put("Description too long", "Descrizione troppo lunga");
		m.put("Destination Path", "Percorso di destinazione");
		m.put("Destinations", "Destinazioni");
		m.put("Detect Licenses", "Rileva licenze");
		m.put("Detect Secrets", "Rileva segreti");
		m.put("Detect Vulnerabilities", "Rileva vulnerabilità");
		m.put("Diff is too large to be displayed.", "Il diff è troppo grande per essere visualizzato.");
		m.put("Diff options", "Opzioni diff");
		m.put("Digest", "Digest");
		m.put("Digest invalid", "Digest non valido");
		m.put("Directories to Skip", "Directory da saltare");
		m.put("Directory", "Directory");
		m.put("Directory (tenant) ID", "ID directory (tenant)");
		m.put("Disable", "Disabilita");
		m.put("Disable All Queried Users", "Disabilita tutti gli utenti interrogati");
		m.put("Disable Auto Update Check", "Disabilita il controllo di aggiornamento automatico");
		m.put("Disable Dashboard", "Disabilita la dashboard");
		m.put("Disable Selected Users", "Disabilita gli utenti selezionati");
		m.put("Disabled", "Disabilitato");
		m.put("Disabled users and service accounts are excluded from user-month calculation", "Gli utenti disabilitati e gli account di servizio sono esclusi dal calcolo utente-mese");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"Disabilitare l'account reimposterà la password, cancellerà i token di accesso e rimuoverà tutti i riferimenti da altre entità, eccetto le attività passate. Vuoi davvero continuare?");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"Disabilitare gli account reimposterà la password, cancellerà i token di accesso e rimuoverà tutti i riferimenti da altre entità, eccetto le attività passate. Digita <code>yes</code> per confermare");
		m.put("Disallowed File Types", "Tipi di file non consentiti");
		m.put("Disallowed file type(s): {0}", "Tipo(i) di file non consentito(i): {0}");
		m.put("Discard", "Scarta");
		m.put("Discard All Queried Pull Requests", "Scarta tutte le richieste di pull interrogate");
		m.put("Discard Selected Pull Requests", "Scarta le richieste di pull selezionate");
		m.put("Discarded", "Scartato");
		m.put("Discarded pull request \"{0}\" ({1})", "Richiesta di pull scartata \"{0}\" ({1})");
		m.put("Discord", "Discord");
		m.put("Discord Notifications", "Notifiche Discord");
		m.put("Display Fields", "Campi di visualizzazione");
		m.put("Display Links", "Collegamenti di visualizzazione");
		m.put("Display Months", "Mesi di visualizzazione");
		m.put("Display Params", "Parametri di visualizzazione");
		m.put("Do Not Retrieve Groups", "Non recuperare gruppi");
		m.put("Do not ignore", "Non ignorare");
		m.put("Do not ignore whitespace", "Non ignorare gli spazi");
		m.put("Do not retrieve", "Non recuperare");
		m.put("Do not retrieve groups", "Non recuperare gruppi");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "Vuoi davvero annullare l'invito a \"{0}\"?");
		m.put("Do you really want to cancel this build?", "Vuoi davvero annullare questa build?");
		m.put("Do you really want to change target branch to {0}?", "Vuoi davvero cambiare il branch di destinazione in {0}?");
		m.put("Do you really want to delete \"{0}\"?", "Vuoi davvero eliminare \"{0}\"?");
		m.put("Do you really want to delete SSO provider \"{0}\"?", "Vuoi davvero eliminare il provider SSO \"{0}\"?");
		m.put("Do you really want to delete board \"{0}\"?", "Vuoi davvero eliminare la board \"{0}\"?");
		m.put("Do you really want to delete build #{0}?", "Vuoi davvero eliminare la build #{0}?");
		m.put("Do you really want to delete group \"{0}\"?", "Vuoi davvero eliminare il gruppo \"{0}\"?");
		m.put("Do you really want to delete iteration \"{0}\"?", "Vuoi davvero eliminare l'iterazione \"{0}\"?");
		m.put("Do you really want to delete job secret \"{0}\"?", "Vuoi davvero eliminare il segreto del job \"{0}\"?");
		m.put("Do you really want to delete pull request #{0}?", "Vuoi davvero eliminare la richiesta di pull #{0}?");
		m.put("Do you really want to delete role \"{0}\"?", "Vuoi davvero eliminare il ruolo \"{0}\"?");
		m.put("Do you really want to delete selected query watches?", "Vuoi davvero eliminare le query selezionate?");
		m.put("Do you really want to delete tag {0}?", "Vuoi davvero eliminare il tag {0}?");
		m.put("Do you really want to delete this GPG key?", "Vuoi davvero eliminare questa chiave GPG?");
		m.put("Do you really want to delete this SSH key?", "Vuoi davvero eliminare questa chiave SSH?");
		m.put("Do you really want to delete this SSO account?", "Vuoi davvero eliminare questo account SSO?");
		m.put("Do you really want to delete this access token?", "Vuoi davvero eliminare questo token di accesso?");
		m.put("Do you really want to delete this board?", "Vuoi davvero eliminare questa board?");
		m.put("Do you really want to delete this build?", "Vuoi davvero eliminare questa build?");
		m.put("Do you really want to delete this code comment and all its replies?", "Vuoi davvero eliminare questo commento al codice e tutte le sue risposte?");
		m.put("Do you really want to delete this code comment?", "Vuoi davvero eliminare questo commento al codice?");
		m.put("Do you really want to delete this directory?", "Vuoi davvero eliminare questa directory?");
		m.put("Do you really want to delete this email address?", "Vuoi davvero eliminare questo indirizzo email?");
		m.put("Do you really want to delete this executor?", "Vuoi davvero eliminare questo executor?");
		m.put("Do you really want to delete this field?", "Vuoi davvero eliminare questo campo?");
		m.put("Do you really want to delete this file?", "Vuoi davvero eliminare questo file?");
		m.put("Do you really want to delete this issue?", "Vuoi davvero eliminare questo problema?");
		m.put("Do you really want to delete this link?", "Vuoi davvero eliminare questo collegamento?");
		m.put("Do you really want to delete this package?", "Vuoi davvero eliminare questo pacchetto?");
		m.put("Do you really want to delete this privilege?", "Vuoi davvero eliminare questo privilegio?");
		m.put("Do you really want to delete this protection?", "Vuoi davvero eliminare questa protezione?");
		m.put("Do you really want to delete this pull request?", "Vuoi davvero eliminare questa pull request?");
		m.put("Do you really want to delete this reply?", "Vuoi davvero eliminare questa risposta?");
		m.put("Do you really want to delete this script?", "Vuoi davvero eliminare questo script?");
		m.put("Do you really want to delete this state?", "Vuoi davvero eliminare questo stato?");
		m.put("Do you really want to delete this template?", "Vuoi davvero eliminare questo modello?");
		m.put("Do you really want to delete this transition?", "Vuoi davvero eliminare questa transizione?");
		m.put("Do you really want to delete timesheet \"{0}\"?", "Vuoi davvero eliminare il timesheet \"{0}\"?");
		m.put("Do you really want to delete unused tokens?", "Vuoi davvero eliminare i token inutilizzati?");
		m.put("Do you really want to discard batched suggestions?", "Vuoi davvero scartare i suggerimenti in batch?");
		m.put("Do you really want to enable this account?", "Vuoi davvero abilitare questo account?");
		m.put("Do you really want to rebuild?", "Vuoi davvero ricostruire?");
		m.put("Do you really want to remove assignee \"{0}\"?", "Vuoi davvero rimuovere l'assegnatario \"{0}\"?");
		m.put("Do you really want to remove password of this user?", "Vuoi davvero rimuovere la password di questo utente?");
		m.put("Do you really want to remove the issue from iteration \"{0}\"?", "Vuoi davvero rimuovere il problema dall'iterazione \"{0}\"?");
		m.put("Do you really want to remove this account?", "Vuoi davvero rimuovere questo account?");
		m.put("Do you really want to remove this agent?", "Vuoi davvero rimuovere questo agente?");
		m.put("Do you really want to remove this link?", "Vuoi davvero rimuovere questo collegamento?");
		m.put("Do you really want to restart this agent?", "Vuoi davvero riavviare questo agente?");
		m.put("Do you really want to unauthorize user \"{0}\"?", "Vuoi davvero non autorizzare l'utente \"{0}\"?");
		m.put("Do you really want to use default template?", "Vuoi davvero utilizzare il modello predefinito?");
		m.put("Docker", "Docker");
		m.put("Docker Executable", "Eseguibile Docker");
		m.put("Docker Hub", "Docker Hub");
		m.put("Docker Image", "Immagine Docker");
		m.put("Docker Sock Path", "Percorso Docker Sock");
		m.put("Dockerfile", "Dockerfile");
		m.put("Documentation", "Documentazione");
		m.put("Don't have an account yet?", "Non hai ancora un account?");
		m.put("Download", "Scarica");
		m.put("Download <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> or <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. A new agent token will be included in the package", 
			"Scarica <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> o <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. Un nuovo token agente sarà incluso nel pacchetto");
		m.put("Download archive of this branch", "Scarica l'archivio di questo branch");
		m.put("Download full log", "Scarica il log completo");
		m.put("Download log", "Scarica il log");
		m.put("Download patch", "Scarica patch");
		m.put("Download tag archive", "Scarica l'archivio del tag");
		m.put("Dry Run", "Esecuzione simulata");
		m.put("Due Date", "Data di scadenza");
		m.put("Due Date Issue Field", "Campo del problema per la data di scadenza");
		m.put("Due date", "Data di scadenza");
		m.put("Duplicate authorizations found: ", "Autorizzazioni duplicate trovate:");
		m.put("Duplicate authorizations found: {0}", "Autorizzazioni duplicate trovate: {0}");
		m.put("Duration", "Durata");
		m.put("Durations", "Durate");
		m.put("ESLint Report", "Rapporto ESLint");
		m.put("Edit", "Modifica");
		m.put("Edit <code>$HOME/.gem/credentials</code> to add a source", "Modifica <code>$HOME/.gem/credentials</code> per aggiungere una sorgente");
		m.put("Edit <code>$HOME/.pypirc</code> to add a package repository like below", "Modifica <code>$HOME/.pypirc</code> per aggiungere un repository di pacchetti come mostrato di seguito");
		m.put("Edit Avatar", "Modifica Avatar");
		m.put("Edit Estimated Time", "Modifica Tempo Stimato");
		m.put("Edit Executor", "Modifica Executor");
		m.put("Edit Iteration", "Modifica Iterazione");
		m.put("Edit Job Secret", "Modifica Segreto del Lavoro");
		m.put("Edit My Avatar", "Modifica il Mio Avatar");
		m.put("Edit Rule", "Modifica Regola");
		m.put("Edit Timesheet", "Modifica Timesheet");
		m.put("Edit dashboard", "Modifica dashboard");
		m.put("Edit issue title", "Modifica titolo del problema");
		m.put("Edit job", "Modifica lavoro");
		m.put("Edit on branch {0}", "Modifica sul branch {0}");
		m.put("Edit on source branch", "Modifica sul branch sorgente");
		m.put("Edit plain", "Modifica semplice");
		m.put("Edit saved queries", "Modifica query salvate");
		m.put("Edit this access token", "Modifica questo token di accesso");
		m.put("Edit this executor", "Modifica questo executor");
		m.put("Edit this iteration", "Modifica questa iterazione");
		m.put("Edit this rule", "Modifica questa regola");
		m.put("Edit this secret", "Modifica questo segreto");
		m.put("Edit this state", "Modifica questo stato");
		m.put("Edit title", "Modifica titolo");
		m.put("Edit with AI", "Modifica con AI");
		m.put("Edit {0}", "Modifica {0}");
		m.put("Editable Issue Fields", "Campi Modificabili del Problema");
		m.put("Editable Issue Links", "Collegamenti Modificabili del Problema");
		m.put("Edited by {0} {1}", "Modificato da {0} {1}");
		m.put("Editor", "Editor");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "Il branch di destinazione o il branch sorgente ha nuovi commit appena ora, per favore ricontrolla.");
		m.put("Email", "Email");
		m.put("Email Address", "Indirizzo Email");
		m.put("Email Address Verification", "Verifica Indirizzo Email");
		m.put("Email Addresses", "Indirizzi Email");
		m.put("Email Templates", "Modelli Email");
		m.put("Email Verification", "Verifica Email");
		m.put("Email Verification Template", "Modello di Verifica Email");
		m.put("Email address", "Indirizzo email");
		m.put("Email address \"{0}\" already used by another account", "L'indirizzo email \"{0}\" è già utilizzato da un altro account");
		m.put("Email address \"{0}\" used by account \"{1}\"", "L'indirizzo email \"{0}\" è utilizzato dall'account \"{1}\"");
		m.put("Email address \"{0}\" used by disabled account \"{1}\"", "L'indirizzo email \"{0}\" è utilizzato dall'account disabilitato \"{1}\"");
		m.put("Email address already in use: {0}", "Indirizzo email già in uso: {0}");
		m.put("Email address already invited: {0}", "Indirizzo email già invitato: {0}");
		m.put("Email address already used by another user", "Indirizzo email già utilizzato da un altro utente");
		m.put("Email address already used: ", "Indirizzo email già utilizzato:");
		m.put("Email address to verify", "Indirizzo email da verificare");
		m.put("Email addresses with <span class=\"badge badge-warning badge-sm\">ineffective</span> mark are those not belong to or not verified by key owner", 
			"Gli indirizzi email con il <span class=\"badge badge-warning badge-sm\">inefficace</span> segno sono quelli che non appartengono o non sono verificati dal proprietario della chiave");
		m.put("Email templates", "Modelli email");
		m.put("Empty file added.", "File vuoto aggiunto.");
		m.put("Empty file removed.", "File vuoto rimosso.");
		m.put("Enable", "Abilita");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets", 
			"Abilita <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>il monitoraggio del tempo</a> per questo progetto per monitorare i progressi e generare timesheet");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project", 
			"Abilita <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>gestione dei pacchetti</a> per questo progetto");
		m.put("Enable Account Self Removal", "Abilita Rimozione Autonoma dell'Account");
		m.put("Enable Account Self Sign-Up", "Abilita Registrazione Autonoma dell'Account");
		m.put("Enable All Queried Users", "Abilita Tutti gli Utenti Interrogati");
		m.put("Enable Anonymous Access", "Abilita Accesso Anonimo");
		m.put("Enable Auto Backup", "Abilita Backup Automatico");
		m.put("Enable Html Report Publish", "Abilita Pubblicazione Report Html");
		m.put("Enable Selected Users", "Abilita Utenti Selezionati");
		m.put("Enable Site Publish", "Abilita Pubblicazione Sito");
		m.put("Enable TTY Mode", "Abilita Modalità TTY");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "Abilita supporto build tramite <a wicket:id=\"addFile\" class=\"link-primary\"></a>");
		m.put("Enable if visibility of this field depends on other fields", "Abilita se la visibilità di questo campo dipende da altri campi");
		m.put("Enable if visibility of this param depends on other params", "Abilita se la visibilità di questo parametro dipende da altri parametri");
		m.put("Enable this if the access token has same permissions as the owner", "Abilita questa opzione se il token di accesso ha gli stessi permessi del proprietario");
		m.put("Enable this option to merge the pull request automatically when ready (all reviewers approved, all required jobs passed etc.)", 
			"Abilita questa opzione per unire automaticamente la pull request quando pronta (tutti i revisori hanno approvato, tutti i lavori richiesti sono passati, ecc.)");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Abilita questa opzione per consentire l'esecuzione del passaggio di pubblicazione del report html. Per evitare attacchi XSS, assicurati che questo executor possa essere utilizzato solo da lavori fidati");
		m.put("Enable this to allow to run site publish step. OneDev will serve project site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Abilita questa opzione per consentire l'esecuzione del passaggio di pubblicazione del sito. OneDev servirà i file del sito del progetto così come sono. Per evitare attacchi XSS, assicurati che questo executor possa essere utilizzato solo da lavori fidati");
		m.put("Enable this to place intermediate files required by job execution on dynamically allocated persistent volume instead of emptyDir", 
			"Abilita questa opzione per posizionare i file intermedi richiesti dall'esecuzione del lavoro su un volume persistente allocato dinamicamente invece di emptyDir");
		m.put("Enable this to process issue or pull request comments posted via email", "Abilita questa opzione per elaborare commenti su problemi o pull request inviati via email");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Abilita questa opzione per elaborare commenti su problemi o pull request inviati via email. <b class='text-danger'>NOTA:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Il sub addressing</a> deve essere abilitato per l'indirizzo email di sistema sopra, poiché OneDev lo utilizza per tracciare contesti di problemi e pull request");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Abilita questa opzione per elaborare commenti su problemi o pull request inviati via email. <b class='text-danger'>NOTA:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Il sub addressing</a> deve essere abilitato per l'indirizzo email di sistema sopra, poiché OneDev lo utilizza per tracciare contesti di problemi e pull request");
		m.put("Enable to allow to upload build cache generated during CI/CD job. Uploaded cache can be used by subsequent builds of the project as long as cache key matches", 
			"Abilita per consentire il caricamento della cache di build generata durante il lavoro CI/CD. La cache caricata può essere utilizzata dalle build successive del progetto finché la chiave della cache corrisponde");
		m.put("End Point", "Punto di Accesso");
		m.put("Enforce Conventional Commits", "Imponi Commit Convenzionali");
		m.put("Enforce Password Policy", "Imponi Politica della Password");
		m.put("Enforce Two-factor Authentication", "Imponi Autenticazione a Due Fattori");
		m.put("Enforce password policy for new users", "Imponi politica della password per i nuovi utenti");
		m.put("Enter New Password", "Inserisci Nuova Password");
		m.put("Enter description here", "Inserisci qui la descrizione");
		m.put("Enter your details to login to your account", "Inserisci i tuoi dati per accedere al tuo account");
		m.put("Enter your user name or email to reset password", "Inserisci il tuo nome utente o email per reimpostare la password");
		m.put("Entries", "Voci");
		m.put("Entry", "Voce");
		m.put("Enumeration", "Enumerazione");
		m.put("Env Var", "Var Ambiente");
		m.put("Environment Variables", "Variabili d'Ambiente");
		m.put("Environment variable <code>serverUrl</code> in above command is taken from OneDev server url specified in <i>Administration / System Setting</i>. Change it if necessary", 
			"La variabile d'ambiente <code>serverUrl</code> nel comando sopra è presa dall'URL del server OneDev specificato in <i>Amministrazione / Impostazioni di Sistema</i>. Modificala se necessario");
		m.put("Equal", "Uguale");
		m.put("Error authenticating user", "Errore nell'autenticazione dell'utente");
		m.put("Error calculating commits: check log for details", "Errore nel calcolo dei commit: controlla il log per i dettagli");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "Errore nel cherry-picking su {0}: Rilevati conflitti di merge");
		m.put("Error cherry-picking to {0}: {1}", "Errore nel cherry-picking su {0}: {1}");
		m.put("Error detail of content type &quot;text/plain&quot;", "Dettaglio errore del tipo di contenuto &quot;text/plain&quot;");
		m.put("Error discovering OIDC metadata", "Errore nella scoperta dei metadati OIDC");
		m.put("Error executing task", "Errore nell'esecuzione del compito");
		m.put("Error parsing %sbase query: ", "Errore nell'analisi della query %sbase:");
		m.put("Error parsing %squery: ", "Errore nell'analisi della query %squery:");
		m.put("Error parsing build spec", "Errore nell'analisi della specifica di build");
		m.put("Error rendering widget, check server log for details", "Errore nel rendering del widget, controlla il log del server per i dettagli");
		m.put("Error reverting on {0}: Merge conflicts detected", "Errore nel revert su {0}: Rilevati conflitti di merge");
		m.put("Error reverting on {0}: {1}", "Errore nel revert su {0}: {1}");
		m.put("Error validating auto merge commit message: {0}", "Errore nella validazione del messaggio di commit per l'auto merge: {0}");
		m.put("Error validating build spec (location: {0}, error message: {1})", "Errore nella validazione della specifica di build (posizione: {0}, messaggio di errore: {1})");
		m.put("Error validating build spec: {0}", "Errore nella validazione della specifica di build: {0}");
		m.put("Error validating commit message of \"{0}\": {1}", "Errore nella validazione del messaggio di commit di \"{0}\": {1}");
		m.put("Error validating commit message of <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}", 
			"Errore nella validazione del messaggio di commit di <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}");
		m.put("Error verifying GPG signature", "Errore nella verifica della firma GPG");
		m.put("Estimated Time", "Tempo Stimato");
		m.put("Estimated Time Edit Bean", "Bean di Modifica del Tempo Stimato");
		m.put("Estimated Time Issue Field", "Campo Problema Tempo Stimato");
		m.put("Estimated Time:", "Tempo Stimato:");
		m.put("Estimated time", "Tempo stimato");
		m.put("Estimated/Spent time. Click for details", "Tempo Stimato/Trascorso. Clicca per i dettagli");
		m.put("Evaluate script to get choices", "Valuta script per ottenere scelte");
		m.put("Evaluate script to get default value", "Valuta script per ottenere valore predefinito");
		m.put("Evaluate script to get value or secret", "Valuta script per ottenere valore o segreto");
		m.put("Evaluate script to get values or secrets", "Valuta script per ottenere valori o segreti");
		m.put("Event Types", "Tipi di Evento");
		m.put("Events", "Eventi");
		m.put("Ever Used Since", "Mai Usato Da");
		m.put("Everything inside this project and all child projects will be deleted and can not be recovered, please type project path <code>{0}</code> below to confirm deletion.", 
			"Tutto all'interno di questo progetto e tutti i progetti figli saranno eliminati e non potranno essere recuperati, per favore digita il percorso del progetto <code>{0}</code> qui sotto per confermare l'eliminazione.");
		m.put("Example", "Esempio");
		m.put("Example Plugin Setting", "Esempio di Impostazione Plugin");
		m.put("Example Property", "Esempio di Proprietà");
		m.put("Exclude Param Combos", "Escludi Combinazioni di Parametri");
		m.put("Exclude States", "Escludi Stati");
		m.put("Excluded", "Escluso");
		m.put("Excluded Fields", "Campi Esclusi");
		m.put("Executable", "Eseguibile");
		m.put("Execute Commands", "Esegui Comandi");
		m.put("Execute Commands via SSH", "Esegui Comandi tramite SSH");
		m.put("Exit Impersonation", "Esci dall'Impersonazione");
		m.put("Exited impersonation", "Uscito dall'impersonazione");
		m.put("Expand all", "Espandi tutto");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", 
			"Si aspetta uno o più <tt>&lt;numero&gt;(h|m)</tt>. Ad esempio <tt>1h 1m</tt> rappresenta 1 ora e 1 minuto");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", 
			"Si aspetta uno o più <tt>&lt;numero&gt;(w|d|h|m)</tt>. Ad esempio <tt>1w 1d 1h 1m</tt> rappresenta 1 settimana ({0} giorni), 1 giorno ({1} ore), 1 ora e 1 minuto");
		m.put("Expiration Date:", "Data di Scadenza:");
		m.put("Expire Date", "Data di Scadenza");
		m.put("Expired", "Scaduto");
		m.put("Explicit SSL (StartTLS)", "SSL Esplicito (StartTLS)");
		m.put("Export", "Esporta");
		m.put("Export All Queried Issues To...", "Esporta Tutti i Problemi Interrogati In...");
		m.put("Export CSV", "Esporta CSV");
		m.put("Export XLSX", "Esporta XLSX");
		m.put("Export as OCI layout", "Esporta come layout OCI");
		m.put("Extend Trial Subscription", "Estendi Abbonamento di Prova");
		m.put("External Authentication", "Autenticazione Esterna");
		m.put("External Issue Transformers", "Trasformatori di Problemi Esterni");
		m.put("External Participants", "Partecipanti Esterni");
		m.put("External Password Authenticator", "Autenticatore di Password Esterna");
		m.put("External System", "Sistema Esterno");
		m.put("External authenticator settings saved", "Impostazioni dell'autenticatore esterno salvate");
		m.put("External participants do not have accounts and involve in the issue via email", "I partecipanti esterni non hanno account e partecipano al problema tramite email");
		m.put("Extract the package into a folder. <b class=\"text-danger\">Warning:</b> On Mac OS X, do not extract to Mac managed folders such as Downloads, Desktop, Documents; otherwise you may encounter permission issues starting agent", 
			"Estrai il pacchetto in una cartella. <b class=\"text-danger\">Avviso:</b> Su Mac OS X, non estrarre in cartelle gestite da Mac come Downloads, Desktop, Documenti; altrimenti potresti incontrare problemi di permessi avviando l'agente");
		m.put("FAILED", "FALLITO");
		m.put("Fail Threshold", "Soglia di Fallimento");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"Fallisci la build se ci sono vulnerabilità con livello di gravità specificato o superiore");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"Fallisci la build se ci sono vulnerabilità con livello di gravità specificato o superiore. Nota che questo ha effetto solo se la build non è fallita per altri passaggi");
		m.put("Failed", "Fallito");
		m.put("Failed to validate build spec import. Check server log for details", "Fallito nella validazione dell'importazione della specifica di build. Controlla il log del server per i dettagli");
		m.put("Failed to verify your email address", "Verifica del tuo indirizzo email fallita");
		m.put("Field Bean", "Bean del Campo");
		m.put("Field Instance", "Istanza del Campo");
		m.put("Field Name", "Nome Campo");
		m.put("Field Spec", "Specifica Campo");
		m.put("Field Specs", "Specifiche Campo");
		m.put("Field Value", "Valore Campo");
		m.put("Fields", "Campi");
		m.put("Fields & Links", "Campi & Collegamenti");
		m.put("Fields And Links Bean", "Bean Campi e Collegamenti");
		m.put("Fields to Change", "Campi da Modificare");
		m.put("File", "File");
		m.put("File Changes", "Modifiche File");
		m.put("File Name", "Nome File");
		m.put("File Name Patterns (separated by comma)", "Modelli Nome File (separati da virgola)");
		m.put("File Path", "Percorso File");
		m.put("File Patterns", "Modelli File");
		m.put("File Protection", "Protezione File");
		m.put("File Protections", "Protezioni File");
		m.put("File and Symbol Search", "Ricerca di File e Simboli");
		m.put("File changes", "modifiche file");
		m.put("File is too large to edit here", "Il file è troppo grande per essere modificato qui");
		m.put("File missing or obsolete", "File mancante o obsoleto");
		m.put("File name", "nome file");
		m.put("File name patterns such as *.java, *.c", "modelli nome file come *.java, *.c");
		m.put("Files", "File");
		m.put("Files to Be Analyzed", "File da Analizzare");
		m.put("Filter", "Filtro");
		m.put("Filter Issues", "Filtra Problemi");
		m.put("Filter actions", "Filtra azioni");
		m.put("Filter backlog issues", "Filtra problemi arretrati");
		m.put("Filter branches...", "Filtra rami...");
		m.put("Filter by name", "Filtra per nome");
		m.put("Filter by name or email address", "Filtra per nome o indirizzo email");
		m.put("Filter by name...", "Filtra per nome...");
		m.put("Filter by path", "Filtra per percorso");
		m.put("Filter by test suite", "Filtra per suite di test");
		m.put("Filter date range", "Filtra intervallo di date");
		m.put("Filter files...", "Filtra file...");
		m.put("Filter groups...", "Filtra gruppi...");
		m.put("Filter issues", "Filtra problemi");
		m.put("Filter pull requests", "Filtra richieste di pull");
		m.put("Filter roles", "Filtra ruoli");
		m.put("Filter tags...", "Filtra tag...");
		m.put("Filter targets", "Filtra obiettivi");
		m.put("Filter users", "Filtra utenti");
		m.put("Filter...", "Filtra...");
		m.put("Filters", "Filtri");
		m.put("Find branch", "Trova ramo");
		m.put("Find or create branch", "Trova o crea ramo");
		m.put("Find or create tag", "Trova o crea tag");
		m.put("Find tag", "Trova tag");
		m.put("Fingerprint", "Impronta digitale");
		m.put("Finish", "Fine");
		m.put("First applicable executor", "Primo esecutore applicabile");
		m.put("Fix", "Correggi");
		m.put("Fix Type", "Tipo di Correzione");
		m.put("Fix Undefined Field Values", "Correggi Valori di Campo Non Definiti");
		m.put("Fix Undefined Fields", "Correggi Campi Non Definiti");
		m.put("Fix Undefined States", "Correggi Stati Non Definiti");
		m.put("Fixed Issues", "Problemi Risolti");
		m.put("Fixed issues since...", "Problemi risolti da...");
		m.put("Fixing Builds", "Correzione Build");
		m.put("Fixing Commits", "Correzione Commit");
		m.put("Fixing...", "Correzione...");
		m.put("Float", "Float");
		m.put("Follow below instructions to publish packages into this project", "Segui le istruzioni seguenti per pubblicare pacchetti in questo progetto");
		m.put("Follow below steps to install agent on remote machine (supports Linux/Windows/Mac OS X/FreeBSD):", 
			"Segui i passaggi seguenti per installare l'agente su una macchina remota (supporta Linux/Windows/Mac OS X/FreeBSD):");
		m.put("For CI/CD job, add this gem to Gemfile like below", "Per il lavoro CI/CD, aggiungi questa gemma a Gemfile come segue");
		m.put("For CI/CD job, add this package to requirements.txt and run below to install the package via command step", 
			"Per il lavoro CI/CD, aggiungi questo pacchetto a requirements.txt ed esegui il comando seguente per installare il pacchetto tramite il passaggio del comando");
		m.put("For CI/CD job, run below to add package repository via command step", "Per il lavoro CI/CD, esegui il comando seguente per aggiungere il repository del pacchetto tramite il passaggio del comando");
		m.put("For CI/CD job, run below to add package source via command step", "Per il lavoro CI/CD, esegui il comando seguente per aggiungere la sorgente del pacchetto tramite il passaggio del comando");
		m.put("For CI/CD job, run below to add source via command step", "Per il lavoro CI/CD, esegui il comando seguente per aggiungere la sorgente tramite il passaggio del comando");
		m.put("For CI/CD job, run below to install chart via command step", "Per il lavoro CI/CD, esegui il comando seguente per installare il chart tramite il passaggio del comando");
		m.put("For CI/CD job, run below to publish package via command step", "Per il lavoro CI/CD, esegui il comando seguente per pubblicare il pacchetto tramite il passaggio del comando");
		m.put("For CI/CD job, run below to push chart to the repository via command step", "Per il lavoro CI/CD, esegui il comando seguente per inviare il chart al repository tramite il passaggio del comando");
		m.put("For CI/CD job, run below via a command step", "Per il lavoro CI/CD, esegui il comando seguente tramite un passaggio del comando");
		m.put("For a particular project, the first matching entry will be used", "Per un progetto particolare, verrà utilizzata la prima voce corrispondente");
		m.put("For all issues", "Per tutti i problemi");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"Per il commit di build non raggiungibile dal ramo predefinito, un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> dovrebbe essere specificato come token di accesso con permesso di creare rami");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"Per il commit di build non raggiungibile dal ramo predefinito, un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> dovrebbe essere specificato come token di accesso con permesso di creare tag");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"Per il commit di build non raggiungibile dal ramo predefinito, un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> dovrebbe essere specificato come token di accesso con permesso di gestire problemi");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"Per gli esecutori consapevoli di Docker, questo percorso è all'interno del container e accetta sia percorsi assoluti che relativi (relativi a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>spazio di lavoro del lavoro</a>). Per gli esecutori correlati alla shell che funzionano direttamente sulla macchina host, è accettato solo il percorso relativo");
		m.put("For each build, OneDev calculates a list of fixed issues since previous build automatically. This setting provides a default query to further filter/order this list. For a given job, the first matching entry will be used.", 
			"Per ogni build, OneDev calcola automaticamente un elenco di problemi risolti dalla build precedente. Questa impostazione fornisce una query predefinita per filtrare/ordinare ulteriormente questo elenco. Per un determinato lavoro, verrà utilizzata la prima voce corrispondente.");
		m.put("For each selected branch/tag, a separate build will be generated with branch/tag set to corresponding value", 
			"Per ogni ramo/tag selezionato, verrà generata una build separata con ramo/tag impostato sul valore corrispondente");
		m.put("For issues matching: ", "Per problemi corrispondenti:");
		m.put("For very large git repository, you may need to tune options here to reduce memory usage", 
			"Per repository git molto grandi, potrebbe essere necessario regolare le opzioni qui per ridurre l'utilizzo della memoria");
		m.put("For web hooks defined here and in parent projects, OneDev will post event data in JSON format to specified URLs when subscribed events happen", 
			"Per i webhook definiti qui e nei progetti principali, OneDev invierà i dati dell'evento in formato JSON agli URL specificati quando si verificano eventi sottoscritti");
		m.put("Force", "Forza");
		m.put("Force Garbage Collection", "Forza Raccolta Garbage");
		m.put("Forgot Password?", "Password Dimenticata?");
		m.put("Forgotten Password?", "Password Dimenticata?");
		m.put("Fork Project", "Fork Progetto");
		m.put("Fork now", "Fai un Fork ora");
		m.put("Forks Of", "Fork di");
		m.put("Frequencies", "Frequenze");
		m.put("From Directory", "Da Directory");
		m.put("From States", "Da Stati");
		m.put("From extracted folder, run <code>bin\\agent.bat console</code> as administrator on Windows or <code>bin/agent.sh console</code> on other OS", 
			"Dalla cartella estratta, esegui <code>bin\\agent.bat console</code> come amministratore su Windows o <code>bin/agent.sh console</code> su altri OS");
		m.put("From {0}", "Da {0}");
		m.put("Full Name", "Nome Completo");
		m.put("Furthest due date", "Data di scadenza più lontana");
		m.put("GPG Keys", "Chiavi GPG");
		m.put("GPG Public Key", "Chiave Pubblica GPG");
		m.put("GPG Signing Key", "Chiave di Firma GPG");
		m.put("GPG Trusted Keys", "Chiavi GPG Fidate");
		m.put("GPG key deleted", "Chiave GPG eliminata");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "La chiave pubblica GPG inizia con '-----BEGIN PGP PUBLIC KEY BLOCK-----'");
		m.put("GPG signing key will be used to sign commits generated by OneDev, including pull request merge commits, user commits created via web UI or RESTful api.", 
			"La chiave di firma GPG verrà utilizzata per firmare i commit generati da OneDev, inclusi i commit di merge delle richieste di pull, i commit utente creati tramite l'interfaccia web o l'API RESTful.");
		m.put("Gem Info", "Informazioni Gemma");
		m.put("General", "Generale");
		m.put("General Settings", "Impostazioni Generali");
		m.put("General settings updated", "Impostazioni generali aggiornate");
		m.put("Generate", "Genera");
		m.put("Generate File Checksum", "Genera Checksum File");
		m.put("Generate New", "Genera Nuovo");
		m.put("Generic LDAP", "LDAP Generico");
		m.put("Get", "Ottieni");
		m.put("Get Groups Using Attribute", "Ottieni Gruppi Usando Attributo");
		m.put("Git", "Git");
		m.put("Git Command Line", "Linea di Comando Git");
		m.put("Git Credential", "Credenziale Git");
		m.put("Git LFS Storage", "Archiviazione Git LFS");
		m.put("Git Lfs Lock", "Blocco Git LFS");
		m.put("Git Location", "Posizione Git");
		m.put("Git Pack Config", "Configurazione Pacchetto Git");
		m.put("Git Path", "Percorso Git");
		m.put("Git email address will be used as git author/committer for commits created on web UI", 
			"L'indirizzo email di Git sarà utilizzato come autore/committente per i commit creati nell'interfaccia web");
		m.put("Git pack config updated", "Configurazione pacchetto Git aggiornata");
		m.put("GitHub", "GitHub");
		m.put("GitHub API URL", "URL API GitHub");
		m.put("GitHub Issue Label", "Etichetta Problema GitHub");
		m.put("GitHub Organization", "Organizzazione GitHub");
		m.put("GitHub Personal Access Token", "Token di Accesso Personale GitHub");
		m.put("GitHub Repositories to Import", "Repository GitHub da Importare");
		m.put("GitHub Repository", "Repository GitHub");
		m.put("GitHub personal access token should be generated with scope <b>repo</b> and <b>read:org</b>", 
			"Il token di accesso personale GitHub dovrebbe essere generato con ambito <b>repo</b> e <b>read:org</b>");
		m.put("GitLab API URL", "URL API GitLab");
		m.put("GitLab Group", "Gruppo GitLab");
		m.put("GitLab Issue Label", "Etichetta Problema GitLab");
		m.put("GitLab Personal Access Token", "Token di Accesso Personale GitLab");
		m.put("GitLab Project", "Progetto GitLab");
		m.put("GitLab Projects to Import", "Progetti GitLab da Importare");
		m.put("GitLab personal access token should be generated with scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by user of specified access token will be listed", 
			"Il token di accesso personale GitLab dovrebbe essere generato con ambito <b>read_api</b>, <b>read_user</b> e <b>read_repository</b>. Nota che solo i gruppi/progetti posseduti dall'utente del token di accesso specificato saranno elencati");
		m.put("Gitea API URL", "URL API Gitea");
		m.put("Gitea Issue Label", "Etichetta Problema Gitea");
		m.put("Gitea Organization", "Organizzazione Gitea");
		m.put("Gitea Personal Access Token", "Token di Accesso Personale Gitea");
		m.put("Gitea Repositories to Import", "Repository Gitea da Importare");
		m.put("Gitea Repository", "Repository Gitea");
		m.put("Github Access Token Secret", "Segreto Token di Accesso GitHub");
		m.put("Global", "Globale");
		m.put("Global Build Setting", "Impostazione di Build Globale");
		m.put("Global Issue Setting", "Impostazione di Problema Globale");
		m.put("Global Pack Setting", "Impostazione di Pacchetto Globale");
		m.put("Global Views", "Viste Globali");
		m.put("Gmail", "Gmail");
		m.put("Go Back", "Torna Indietro");
		m.put("Google Test Report", "Rapporto Test Google");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Chiave Gpg");
		m.put("Great, your mail service configuration is working", "Ottimo, la configurazione del servizio di posta funziona");
		m.put("Groovy Script", "Script Groovy");
		m.put("Groovy Scripts", "Script Groovy");
		m.put("Groovy script to be evaluated. It should return a <i>Date</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Dovrebbe restituire un valore <i>Date</i>. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. It should return a <i>Float</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Dovrebbe restituire un valore <i>Float</i>. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. It should return a <i>Integer</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Dovrebbe restituire un valore <i>Integer</i>. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. It should return a <i>String</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Dovrebbe restituire un valore <i>String</i>. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. It should return a <i>boolean</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Dovrebbe restituire un valore <i>boolean</i>. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. It should return a <i>string</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Dovrebbe restituire un valore <i>string</i>. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. It should return name of a group. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Dovrebbe restituire il nome di un gruppo. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. It should return string or list of string. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Dovrebbe restituire una stringa o un elenco di stringhe. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Il valore restituito dovrebbe essere un elenco di oggetti facciata di gruppo da utilizzare come scelte. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. The return value should be a list of user login names to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Il valore restituito dovrebbe essere un elenco di nomi di login utente da utilizzare come scelte. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy script to be evaluated. The return value should be a value to color map, for instance:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> if the value does not have a color. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy da valutare. Il valore restituito dovrebbe essere una mappa valore-colore, ad esempio:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Usa <tt>null</tt> se il valore non ha un colore. Controlla <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>aiuto scripting</a> per i dettagli");
		m.put("Groovy scripts", "Script Groovy");
		m.put("Group", "Gruppo");
		m.put("Group \"{0}\" deleted", "Gruppo \"{0}\" eliminato");
		m.put("Group Authorization Bean", "Bean di Autorizzazione Gruppo");
		m.put("Group Authorizations", "Autorizzazioni Gruppo");
		m.put("Group Authorizations Bean", "Bean di Autorizzazioni Gruppo");
		m.put("Group By", "Raggruppa Per");
		m.put("Group Management", "Gestione Gruppo");
		m.put("Group Name Attribute", "Attributo Nome Gruppo");
		m.put("Group Retrieval", "Recupero Gruppo");
		m.put("Group Search Base", "Base di Ricerca Gruppo");
		m.put("Group Search Filter", "Filtro di Ricerca Gruppo");
		m.put("Group authorizations updated", "Autorizzazioni gruppo aggiornate");
		m.put("Group created", "Gruppo creato");
		m.put("Groups", "Gruppi");
		m.put("Groups Claim", "Reclamo Gruppi");
		m.put("Guide Line", "Linea Guida");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "URL Clone HTTP(S)");
		m.put("Has Owner Permissions", "Ha Permessi Proprietario");
		m.put("Has Running Builds", "Ha Build in Esecuzione");
		m.put("Heap Memory Usage", "Utilizzo Memoria Heap");
		m.put("Helm(s)", "Helm(s)");
		m.put("Help", "Aiuto");
		m.put("Hide", "Nascondi");
		m.put("Hide Archived", "Nascondi Archiviati");
		m.put("Hide comment", "Nascondi commento");
		m.put("Hide saved queries", "Nascondi query salvate");
		m.put("High", "Alto");
		m.put("High Availability & Scalability", "Alta Disponibilità & Scalabilità");
		m.put("High Severity", "Alta Gravità");
		m.put("History", "Storia");
		m.put("History of comparing revisions is unrelated", "La storia del confronto delle revisioni è non correlata");
		m.put("History of target branch and source branch is unrelated", "La storia del ramo di destinazione e del ramo sorgente è non correlata");
		m.put("Host name or ip address of remote machine to run commands via SSH", "Nome host o indirizzo IP della macchina remota per eseguire comandi tramite SSH");
		m.put("Hours Per Day", "Ore al giorno");
		m.put("How to Publish", "Come pubblicare");
		m.put("Html Report", "Report HTML");
		m.put("Http Method", "Metodo Http");
		m.put("I didn't eat it. I swear!", "Non l'ho mangiato. Lo giuro!");
		m.put("ID token was expired", "Il token ID è scaduto");
		m.put("IMAP Host", "Host IMAP");
		m.put("IMAP Password", "Password IMAP");
		m.put("IMAP User", "Utente IMAP");
		m.put("IMPORTANT:", "IMPORTANTE:");
		m.put("IP Address", "Indirizzo IP");
		m.put("Id", "ID");
		m.put("Identify Field", "Campo di identificazione");
		m.put("If enabled, scheduled backup will run on lead server which is <span wicket:id=\"leadServer\"></span> currently", 
			"Se abilitato, il backup programmato verrà eseguito sul server principale che è attualmente <span wicket:id=\"leadServer\"></span>");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"Se abilitato, il branch sorgente verrà eliminato automaticamente dopo la fusione della pull request se l'utente ha il permesso di farlo");
		m.put("If specified, OneDev will only display iterations with this prefix", "Se specificato, OneDev mostrerà solo le iterazioni con questo prefisso");
		m.put("If specified, all public and internal projects imported from GitLab will use these as default roles. Private projects are not affected", 
			"Se specificato, tutti i progetti pubblici e interni importati da GitLab utilizzeranno questi come ruoli predefiniti. I progetti privati non sono interessati");
		m.put("If specified, all public repositories imported from GitHub will use these as default roles. Private repositories are not affected", 
			"Se specificato, tutti i repository pubblici importati da GitHub utilizzeranno questi come ruoli predefiniti. I repository privati non sono interessati");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"Se specificato, il tempo totale stimato/speso di un problema includerà anche i problemi collegati di questo tipo");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"Se questa opzione è abilitata, il comando git lfs deve essere installato sul server OneDev (anche se questo passaggio viene eseguito su un altro nodo)");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"Se selezionato, il gruppo indicato da questo campo sarà in grado di modificare il tempo stimato dei problemi corrispondenti se il tracciamento del tempo è abilitato");
		m.put("Ignore", "Ignora");
		m.put("Ignore File", "Ignora file");
		m.put("Ignore activities irrelevant to me", "Ignora attività non rilevanti per me");
		m.put("Ignore all", "Ignora tutto");
		m.put("Ignore all whitespace", "Ignora tutti gli spazi");
		m.put("Ignore change", "Ignora modifica");
		m.put("Ignore change whitespace", "Ignora spazi di modifica");
		m.put("Ignore leading", "Ignora iniziali");
		m.put("Ignore leading whitespace", "Ignora spazi iniziali");
		m.put("Ignore this field", "Ignora questo campo");
		m.put("Ignore this param", "Ignora questo parametro");
		m.put("Ignore trailing", "Ignora finali");
		m.put("Ignore trailing whitespace", "Ignora spazi finali");
		m.put("Ignored Licenses", "Licenze ignorate");
		m.put("Image", "Immagine");
		m.put("Image Labels", "Etichette immagine");
		m.put("Image Manifest", "Manifesto immagine");
		m.put("Image Size", "Dimensione immagine");
		m.put("Image Text", "Testo immagine");
		m.put("Image URL", "URL immagine");
		m.put("Image URL should be specified", "L'URL immagine deve essere specificato");
		m.put("Imap Ssl Setting", "Impostazione SSL IMAP");
		m.put("Imap With Ssl", "IMAP con SSL");
		m.put("Impersonate", "Impersonare");
		m.put("Implicit SSL", "SSL implicito");
		m.put("Import", "Importa");
		m.put("Import All Projects", "Importa tutti i progetti");
		m.put("Import All Repositories", "Importa tutti i repository");
		m.put("Import Group", "Importa gruppo");
		m.put("Import Issues", "Importa problemi");
		m.put("Import Option", "Opzione di importazione");
		m.put("Import Organization", "Importa organizzazione");
		m.put("Import Project", "Importa progetto");
		m.put("Import Projects", "Importa progetti");
		m.put("Import Repositories", "Importa repository");
		m.put("Import Repository", "Importa repository");
		m.put("Import Server", "Importa server");
		m.put("Import Workspace", "Importa spazio di lavoro");
		m.put("Import build spec elements (jobs, services, step templates and properties) from other projects. Imported elements are treated as if they are defined locally. Locally defined elements will override imported elements with same name", 
			"Importa elementi di specifica di build (job, servizi, modelli di step e proprietà) da altri progetti. Gli elementi importati sono trattati come se fossero definiti localmente. Gli elementi definiti localmente sovrascriveranno gli elementi importati con lo stesso nome");
		m.put("Importing Issues from {0}", "Importazione di problemi da {0}");
		m.put("Importing from {0}", "Importazione da {0}");
		m.put("Importing issues into currrent project. Please note that issue numbers will only be retained if the whole project fork graph does not have any issues to avoid duplicate issue numbers", 
			"Importazione di problemi nel progetto corrente. Si noti che i numeri dei problemi saranno mantenuti solo se l'intero grafico di fork del progetto non ha problemi per evitare numeri di problemi duplicati");
		m.put("Importing projects from {0}", "Importazione di progetti da {0}");
		m.put("Imports", "Importazioni");
		m.put("In Projects", "Nei progetti");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"Nel caso in cui il certificato host IMAP sia autofirmato o la sua radice CA non sia accettata, puoi dire a OneDev di ignorare il controllo del certificato. <b class='text-danger'>ATTENZIONE: </b> In una rete non affidabile, ciò potrebbe portare ad un attacco man-in-the-middle, e dovresti <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>importare il certificato in OneDev</a> invece");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"Nel caso in cui il certificato host SMTP sia autofirmato o la sua radice CA non sia accettata, puoi dire a OneDev di ignorare il controllo del certificato. <b class='text-danger'>ATTENZIONE: </b> In una rete non affidabile, ciò potrebbe portare ad un attacco man-in-the-middle, e dovresti <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>importare il certificato in OneDev</a> invece");
		m.put("In case anonymous access is disabled or anonymous user does not have enough permission for a resource operation, you will need to authenticate by providing user name and password (or access token) via http basic auth header", 
			"Nel caso in cui l'accesso anonimo sia disabilitato o l'utente anonimo non abbia abbastanza permessi per un'operazione di risorsa, sarà necessario autenticarsi fornendo nome utente e password (o token di accesso) tramite l'intestazione di autenticazione di base http");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"Nel caso in cui la cache non venga colpita tramite la chiave sopra, OneDev scorrerà le chiavi di caricamento definite qui in ordine fino a trovare una cache corrispondente nella gerarchia del progetto. Una cache è considerata corrispondente se la sua chiave è prefissata con la chiave di caricamento. Se più cache corrispondono, verrà restituita la cache più recente");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"Nel caso in cui la cache debba essere caricata, questa proprietà specifica il progetto di destinazione per il caricamento. Lascia vuoto per il progetto corrente");
		m.put("In case the pull request status is out of sync with underlying repository, you may synchronize them manually here", 
			"Nel caso in cui lo stato della pull request sia fuori sincronizzazione con il repository sottostante, puoi sincronizzarli manualmente qui");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"Nel caso in cui l'appartenenza al gruppo utente sia mantenuta dal lato del gruppo, questa proprietà specifica il nodo base per la ricerca del gruppo. Ad esempio: <i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"Nel caso in cui la relazione del gruppo utente sia mantenuta dal lato del gruppo, questo filtro viene utilizzato per determinare i gruppi di appartenenza dell'utente corrente. Ad esempio: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In questo esempio, <i>{0}</i> rappresenta il DN dell'utente corrente");
		m.put("In case you are using external issue tracker, you can define transformers to transform external issue references into external issue links in various places, such as commit messages and pull request descriptions", 
			"Nel caso in cui utilizzi un tracker di problemi esterno, puoi definire trasformatori per trasformare i riferimenti a problemi esterni in collegamenti a problemi esterni in vari luoghi, come messaggi di commit e descrizioni delle pull request");
		m.put("In rare cases, your issues might be out of sync with workflow settings (undefined state/field etc.). Run integrity check below to find problems and get them fixed.", 
			"In rari casi, i tuoi problemi potrebbero essere fuori sincronizzazione con le impostazioni del flusso di lavoro (stato/campo non definito ecc.). Esegui il controllo di integrità qui sotto per trovare problemi e risolverli.");
		m.put("Inbox Poll Setting", "Impostazione di polling della casella di posta");
		m.put("Include Child Projects", "Includi progetti figli");
		m.put("Include Disabled", "Includi disabilitati");
		m.put("Include Forks", "Includi fork");
		m.put("Include When Issue is Opened", "Includi quando il problema è aperto");
		m.put("Incompatibilities", "Incompatibilità");
		m.put("Inconsistent issuer in provider metadata and ID token", "Emittente incoerente nei metadati del provider e nel token ID");
		m.put("Indicator", "Indicatore");
		m.put("Inherit from parent", "Eredita dal genitore");
		m.put("Inherited", "Ereditato");
		m.put("Input Spec", "Specifica input");
		m.put("Input URL", "URL di input");
		m.put("Input allowed CORS origin, hit ENTER to add", "Inserisci origine CORS consentita, premi INVIO per aggiungere");
		m.put("Input revision", "Inserisci revisione");
		m.put("Input title", "Inserisci titolo");
		m.put("Input title here", "Inserisci qui il titolo");
		m.put("Input user search base. Hit ENTER to add", "Inserisci base di ricerca utente. Premi INVIO per aggiungere");
		m.put("Input user search bases. Hit ENTER to add", "Inserisci basi di ricerca utente. Premi INVIO per aggiungere");
		m.put("Insert", "Inserisci");
		m.put("Insert Image", "Inserisci immagine");
		m.put("Insert Link", "Inserisci collegamento");
		m.put("Insert link to this file", "Inserisci collegamento a questo file");
		m.put("Insert this image", "Inserisci questa immagine");
		m.put("Install Subscription Key", "Installa chiave di abbonamento");
		m.put("Integer", "Intero");
		m.put("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Accesso alla shell web interattiva per i lavori in esecuzione è una funzionalità aziendale. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("Internal Database", "Database Interno");
		m.put("Interpreter", "Interprete");
		m.put("Invalid GPG signature", "Firma GPG non valida");
		m.put("Invalid PCRE syntax", "Sintassi PCRE non valida");
		m.put("Invalid access token: {0}", "Token di accesso non valido: {0}");
		m.put("Invalid credentials", "Credenziali non valide");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "Intervallo di date non valido, previsto \"yyyy-MM-dd to yyyy-MM-dd\"");
		m.put("Invalid email address: {0}", "Indirizzo email non valido: {0}");
		m.put("Invalid invitation code", "Codice di invito non valido");
		m.put("Invalid issue date of ID token", "Data di emissione del token ID non valida");
		m.put("Invalid issue number: {0}", "Numero di emissione non valido: {0}");
		m.put("Invalid pull request number: {0}", "Numero di pull request non valido: {0}");
		m.put("Invalid request path", "Percorso della richiesta non valido");
		m.put("Invalid selection, click for details", "Selezione non valida, clicca per dettagli");
		m.put("Invalid ssh signature", "Firma ssh non valida");
		m.put("Invalid state response", "Risposta di stato non valida");
		m.put("Invalid state. Please make sure you are visiting OneDev using server url specified in system setting", 
			"Stato non valido. Assicurati di visitare OneDev utilizzando l'url del server specificato nelle impostazioni di sistema");
		m.put("Invalid subscription key", "Chiave di abbonamento non valida");
		m.put("Invalid working period", "Periodo di lavoro non valido");
		m.put("Invitation sent to \"{0}\"", "Invito inviato a \"{0}\"");
		m.put("Invitation to \"{0}\" deleted", "Invito a \"{0}\" eliminato");
		m.put("Invitations", "Inviti");
		m.put("Invitations sent", "Inviti inviati");
		m.put("Invite", "Invita");
		m.put("Invite Users", "Invita Utenti");
		m.put("Is Site Admin", "È Amministratore del Sito");
		m.put("Issue", "Problema");
		m.put("Issue #{0} deleted", "Problema #{0} eliminato");
		m.put("Issue Board", "Bacheca Problemi");
		m.put("Issue Boards", "Bacheche Problemi");
		m.put("Issue Close States", "Stati di Chiusura Problemi");
		m.put("Issue Creation Setting", "Impostazione Creazione Problemi");
		m.put("Issue Creation Settings", "Impostazioni Creazione Problemi");
		m.put("Issue Custom Fields", "Campi Personalizzati Problemi");
		m.put("Issue Description", "Descrizione Problema");
		m.put("Issue Description Templates", "Template Descrizione Problemi");
		m.put("Issue Details", "Dettagli Problema");
		m.put("Issue Field", "Campo Problema");
		m.put("Issue Field Mapping", "Mappatura Campo Problema");
		m.put("Issue Field Mappings", "Mappature Campo Problema");
		m.put("Issue Field Set", "Set di Campi Problema");
		m.put("Issue Fields", "Campi Problema");
		m.put("Issue Filter", "Filtro Problemi");
		m.put("Issue Import Option", "Opzione Importazione Problemi");
		m.put("Issue Label Mapping", "Mappatura Etichetta Problemi");
		m.put("Issue Label Mappings", "Mappature Etichetta Problemi");
		m.put("Issue Link", "Collegamento Problema");
		m.put("Issue Link Mapping", "Mappatura Collegamento Problema");
		m.put("Issue Link Mappings", "Mappature Collegamento Problema");
		m.put("Issue Links", "Collegamenti Problemi");
		m.put("Issue Management", "Gestione Problemi");
		m.put("Issue Notification", "Notifica Problema");
		m.put("Issue Notification Template", "Template Notifica Problema");
		m.put("Issue Notification Unsubscribed", "Notifica Problema Annullata");
		m.put("Issue Notification Unsubscribed Template", "Template Notifica Problema Annullata");
		m.put("Issue Pattern", "Pattern Problema");
		m.put("Issue Priority Mapping", "Mappatura Priorità Problemi");
		m.put("Issue Priority Mappings", "Mappature Priorità Problemi");
		m.put("Issue Query", "Query Problemi");
		m.put("Issue Settings", "Impostazioni Problemi");
		m.put("Issue State", "Stato Problema");
		m.put("Issue State Mapping", "Mappatura Stato Problema");
		m.put("Issue State Mappings", "Mappature Stato Problema");
		m.put("Issue State Transition", "Transizione Stato Problema");
		m.put("Issue State Transitions", "Transizioni Stato Problema");
		m.put("Issue States", "Stati Problemi");
		m.put("Issue Statistics", "Statistiche Problemi");
		m.put("Issue Stats", "Statistiche Problemi");
		m.put("Issue Status Mapping", "Mappatura Stato Problemi");
		m.put("Issue Status Mappings", "Mappature Stato Problemi");
		m.put("Issue Stopwatch Overdue", "Cronometro Problema Scaduto");
		m.put("Issue Stopwatch Overdue Notification Template", "Template Notifica Cronometro Problema Scaduto");
		m.put("Issue Tag Mapping", "Mappatura Tag Problemi");
		m.put("Issue Tag Mappings", "Mappature Tag Problemi");
		m.put("Issue Template", "Template Problema");
		m.put("Issue Transition ({0} -> {1})", "Transizione Problema ({0} -> {1})");
		m.put("Issue Type Mapping", "Mappatura Tipo Problema");
		m.put("Issue Type Mappings", "Mappature Tipo Problema");
		m.put("Issue Votes", "Voti Problemi");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"Permesso amministrativo per problemi all'interno di un progetto, inclusa l'operazione batch su più problemi");
		m.put("Issue count", "Conteggio Problemi");
		m.put("Issue in state", "Problema in stato");
		m.put("Issue list", "Lista Problemi");
		m.put("Issue management not enabled in this project", "Gestione Problemi non abilitata in questo progetto");
		m.put("Issue management permission required to move issues", "Permesso di gestione problemi richiesto per spostare problemi");
		m.put("Issue not exist or access denied", "Problema non esistente o accesso negato");
		m.put("Issue number", "Numero Problema");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"La query dei problemi osserva solo i nuovi problemi. Per gestire lo stato di osservazione dei problemi esistenti in batch, filtra i problemi per stato di osservazione nella pagina dei problemi e poi prendi l'azione appropriata");
		m.put("Issue state duration statistics", "Statistiche durata stato problema");
		m.put("Issue state frequency statistics", "Statistiche frequenza stato problema");
		m.put("Issue state trend statistics", "Statistiche trend stato problema");
		m.put("Issue statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Le statistiche dei problemi sono una funzionalità aziendale. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("Issue workflow changed, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliation</a> needs to be performed to make data consistent. You may do this after making all necessary changes", 
			"Il workflow dei problemi è cambiato, <a wicket:id=\"reconcile\" class=\"link-primary\">riconciliazione</a> deve essere eseguita per rendere i dati consistenti. Puoi farlo dopo aver apportato tutte le modifiche necessarie");
		m.put("Issues", "Problemi");
		m.put("Issues can be created in this project by sending email to this address", "I problemi possono essere creati in questo progetto inviando un'email a questo indirizzo");
		m.put("Issues copied", "Problemi copiati");
		m.put("Issues moved", "Problemi spostati");
		m.put("Italic", "Italico");
		m.put("Iteration", "Iterazione");
		m.put("Iteration \"{0}\" closed", "Iterazione \"{0}\" chiusa");
		m.put("Iteration \"{0}\" deleted", "Iterazione \"{0}\" eliminata");
		m.put("Iteration \"{0}\" is closed", "Iterazione \"{0}\" è chiusa");
		m.put("Iteration \"{0}\" is reopened", "Iterazione \"{0}\" riaperta");
		m.put("Iteration \"{0}\" reopened", "Iterazione \"{0}\" riaperta");
		m.put("Iteration Edit Bean", "Modifica Bean Iterazione");
		m.put("Iteration Name", "Nome Iterazione");
		m.put("Iteration Names", "Nomi Iterazione");
		m.put("Iteration Prefix", "Prefisso Iterazione");
		m.put("Iteration list", "Elenco Iterazioni");
		m.put("Iteration saved", "Iterazione salvata");
		m.put("Iteration spans too long to show burndown chart", "L'intervallo dell'iterazione è troppo lungo per mostrare il grafico burndown");
		m.put("Iteration start and due date should be specified to show burndown chart", "La data di inizio e di scadenza dell'iterazione devono essere specificate per mostrare il grafico burndown");
		m.put("Iteration start date should be before due date", "La data di inizio dell'iterazione deve essere precedente alla data di scadenza");
		m.put("Iterations", "Iterazioni");
		m.put("Iterations Bean", "Bean Iterazioni");
		m.put("JIRA Issue Priority", "Priorità Problema JIRA");
		m.put("JIRA Issue Status", "Stato Problema JIRA");
		m.put("JIRA Issue Type", "Tipo Problema JIRA");
		m.put("JIRA Project", "Progetto JIRA");
		m.put("JIRA Projects to Import", "Progetti JIRA da Importare");
		m.put("JUnit Report", "Report JUnit");
		m.put("JVM", "JVM");
		m.put("JaCoCo Coverage Report", "Report Copertura JaCoCo");
		m.put("Jest Coverage Report", "Report Copertura Jest");
		m.put("Jest Test Report", "Report Test Jest");
		m.put("Job", "Lavoro");
		m.put("Job \"{0}\" associated with the build not found.", "Lavoro \"{0}\" associato alla build non trovato.");
		m.put("Job Authorization", "Autorizzazione Lavoro");
		m.put("Job Cache Management", "Gestione Cache Lavoro");
		m.put("Job Dependencies", "Dipendenze Lavoro");
		m.put("Job Dependency", "Dipendenza Lavoro");
		m.put("Job Executor", "Esecutore Lavoro");
		m.put("Job Executor Bean", "Bean Esecutore Lavoro");
		m.put("Job Executors", "Esecutori Lavoro");
		m.put("Job Name", "Nome Lavoro");
		m.put("Job Names", "Nomi Lavoro");
		m.put("Job Param", "Parametro del lavoro");
		m.put("Job Parameters", "Parametri del lavoro");
		m.put("Job Privilege", "Privilegio Lavoro");
		m.put("Job Privileges", "Privilegi Lavoro");
		m.put("Job Properties", "Proprietà Lavoro");
		m.put("Job Properties Bean", "Bean Proprietà Lavoro");
		m.put("Job Property", "Proprietà Lavoro");
		m.put("Job Secret", "Segreto Lavoro");
		m.put("Job Secret Edit Bean", "Modifica Bean Segreto Lavoro");
		m.put("Job Secrets", "Segreti Lavoro");
		m.put("Job Trigger", "Trigger Lavoro");
		m.put("Job Trigger Bean", "Bean Trigger Lavoro");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"Permesso amministrativo del lavoro, inclusa la cancellazione delle build del lavoro. Implica tutti gli altri permessi del lavoro");
		m.put("Job cache \"{0}\" deleted", "Cache del lavoro \"{0}\" eliminata");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"Le dipendenze del lavoro determinano l'ordine e la concorrenza nell'esecuzione di lavori diversi. Puoi anche specificare artefatti da recuperare dai lavori a monte");
		m.put("Job executor tested successfully", "Esecutore del lavoro testato con successo");
		m.put("Job executors", "Esecutori Lavoro");
		m.put("Job name", "Nome Lavoro");
		m.put("Job properties saved", "Proprietà del lavoro salvate");
		m.put("Job secret \"{0}\" deleted", "Segreto del lavoro \"{0}\" eliminato");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission", 
			"Il segreto del lavoro 'access-token' deve essere definito nelle impostazioni di build del progetto come un token di accesso con permesso ${permission} per il pacchetto");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package read permission", 
			"Il segreto del lavoro 'access-token' deve essere definito nelle impostazioni di build del progetto come un token di accesso con permesso di lettura per il pacchetto");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package write permission", 
			"Il segreto del lavoro 'access-token' deve essere definito nelle impostazioni di build del progetto come un token di accesso con permesso di scrittura per il pacchetto");
		m.put("Job token", "Token Lavoro");
		m.put("Job will run on head commit of default branch", "Il lavoro verrà eseguito sull'ultimo commit della branch predefinita");
		m.put("Job will run on head commit of target branch", "Il lavoro verrà eseguito sull'ultimo commit della branch di destinazione");
		m.put("Job will run on merge commit of target branch and source branch", "Il lavoro verrà eseguito sul commit di merge della branch di destinazione e della branch sorgente");
		m.put("Job will run on merge commit of target branch and source branch.<br><b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"Il lavoro verrà eseguito sul commit di merge della branch di destinazione e della branch sorgente.<br><b class='text-info'>NOTA:</b> A meno che non sia richiesto dalla regola di protezione della branch, questo trigger ignorerà i commit con messaggi contenenti <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, o <code>[no job]</code>");
		m.put("Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"Il lavoro verrà eseguito quando il codice viene commesso. <b class='text-info'>NOTA:</b> Questo trigger ignorerà i commit con messaggi contenenti <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, o <code>[no job]</code>");
		m.put("Job workspace", "Workspace Lavoro");
		m.put("Jobs", "Lavori");
		m.put("Jobs marked with <span class=\"text-danger\">*</span> are required to be successful", 
			"I lavori contrassegnati con <span class=\"text-danger\">*</span> devono avere successo");
		m.put("Jobs required to be successful on merge commit: ", "Lavori richiesti per avere successo sul commit di merge:");
		m.put("Jobs required to be successful: ", "Lavori richiesti per avere successo:");
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"I lavori con lo stesso gruppo sequenziale e esecutore verranno eseguiti in sequenza. Ad esempio, puoi specificare questa proprietà come <tt>@project_path@:prod</tt> per lavori eseguiti dallo stesso esecutore e distribuiti nell'ambiente di produzione del progetto corrente per evitare distribuzioni in conflitto");
		m.put("Key", "Chiave");
		m.put("Key Fingerprint", "Impronta digitale della chiave");
		m.put("Key ID", "ID Chiave");
		m.put("Key Secret", "Segreto Chiave");
		m.put("Key Type", "Tipo di chiave");
		m.put("Kubectl Config File", "File Configurazione Kubectl");
		m.put("Kubernetes", "Kubernetes");
		m.put("Kubernetes Executor", "Esecutore Kubernetes");
		m.put("LDAP URL", "URL LDAP");
		m.put("Label", "Etichetta");
		m.put("Label Management", "Gestione Etichetta");
		m.put("Label Management Bean", "Bean Gestione Etichetta");
		m.put("Label Name", "Nome Etichetta");
		m.put("Label Spec", "Specifica Etichetta");
		m.put("Label Value", "Valore Etichetta");
		m.put("Labels", "Etichette");
		m.put("Labels Bean", "Bean Etichette");
		m.put("Labels can be defined in Administration / Label Management", "Le etichette possono essere definite in Amministrazione / Gestione Etichette");
		m.put("Labels have been updated", "Le etichette sono state aggiornate");
		m.put("Language", "Lingua");
		m.put("Last Accessed", "Ultimo Accesso");
		m.put("Last Finished of Specified Job", "Ultima Conclusione del Lavoro Specificato");
		m.put("Last Modified", "Ultima Modifica");
		m.put("Last Published", "Ultima Pubblicazione");
		m.put("Last Update", "Ultimo Aggiornamento");
		m.put("Last commit", "Ultimo Commit");
		m.put("Last commit hash", "Hash Ultimo Commit");
		m.put("Last commit index version", "Versione Indice Ultimo Commit");
		m.put("Leaf Projects", "Progetti Foglia");
		m.put("Least branch coverage", "Copertura Minima della Branch");
		m.put("Least line coverage", "Copertura Minima delle Linee");
		m.put("Leave a comment", "Lascia un commento");
		m.put("Leave a note", "Lascia una nota");
		m.put("Left", "Sinistra");
		m.put("Less", "Meno");
		m.put("License Agreement", "Accordo di Licenza");
		m.put("License Setting", "Impostazione della Licenza");
		m.put("Licensed To", "Licenziato a");
		m.put("Licensed To:", "Licenziato a:");
		m.put("Line", "Linea");
		m.put("Line changes", "Modifiche alla Linea");
		m.put("Line: ", "Linea:");
		m.put("Lines", "Linee");
		m.put("Link", "Collegamento");
		m.put("Link Existing User", "Collega Utente Esistente");
		m.put("Link Spec", "Specifica del Collegamento");
		m.put("Link Spec Opposite", "Specifica del Collegamento Opposto");
		m.put("Link Text", "Testo del Collegamento");
		m.put("Link URL", "URL del Collegamento");
		m.put("Link URL should be specified", "URL del Collegamento deve essere specificato");
		m.put("Link User Bean", "Collega User Bean");
		m.put("Linkable Issues", "Problemi Collegabili");
		m.put("Linkable Issues On the Other Side", "Problemi Collegabili dall'Altra Parte");
		m.put("Links", "Collegamenti");
		m.put("Links can be used to associate different issues. For instance, an issue can be linked to sub issues or related issues", 
			"I collegamenti possono essere utilizzati per associare diversi problemi. Ad esempio, un problema può essere collegato a sotto-problemi o problemi correlati");
		m.put("List", "Lista");
		m.put("Literal", "Letterale");
		m.put("Literal default value", "Valore predefinito letterale");
		m.put("Literal value", "Valore letterale");
		m.put("Load Keys", "Carica Chiavi");
		m.put("Loading emojis...", "Caricamento emoji...");
		m.put("Loading...", "Caricamento...");
		m.put("Log", "Registro");
		m.put("Log Work", "Registra Lavoro");
		m.put("Log not available for offline agent", "Registro non disponibile per l'agente offline");
		m.put("Log work", "Registra lavoro");
		m.put("Login Name", "Nome di Accesso");
		m.put("Login and generate refresh token", "Accedi e genera token di aggiornamento");
		m.put("Login name already used by another account", "Nome di accesso già utilizzato da un altro account");
		m.put("Login name or email", "Nome di accesso o email");
		m.put("Login name or email address", "Nome di accesso o indirizzo email");
		m.put("Login to OneDev docker registry", "Accedi al registro docker di OneDev");
		m.put("Login to comment", "Accedi per commentare");
		m.put("Login to comment on selection", "Accedi per commentare la selezione");
		m.put("Login to vote", "Accedi per votare");
		m.put("Login user needs to have package write permission over the project below", "L'utente che accede deve avere il permesso di scrittura del pacchetto sul progetto sottostante");
		m.put("Login with {0}", "Accedi con {0}");
		m.put("Logo for Dark Mode", "Logo per Modalità Scura");
		m.put("Logo for Light Mode", "Logo per Modalità Chiara");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"Token di aggiornamento a lunga durata dell'account sopra che verrà utilizzato per generare token di accesso per accedere a Gmail. <b class='text-info'>CONSIGLI: </b> puoi utilizzare il pulsante sul lato destro di questo campo per generare il token di aggiornamento. Nota che ogni volta che l'id client, il segreto client o il nome dell'account vengono modificati, il token di aggiornamento deve essere rigenerato");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"Token di aggiornamento a lunga durata dell'account sopra che verrà utilizzato per generare token di accesso per accedere al server di posta di Office 365. <b class='text-info'>CONSIGLI: </b> puoi utilizzare il pulsante sul lato destro di questo campo per accedere al tuo account Office 365 e generare il token di aggiornamento. Nota che ogni volta che l'id tenant, l'id client, il segreto client o il nome principale dell'utente vengono modificati, il token di aggiornamento deve essere rigenerato");
		m.put("Longest Duration First", "Durata più lunga prima");
		m.put("Looks like a GPG signature but without necessary data", "Sembra una firma GPG ma senza i dati necessari");
		m.put("Low", "Basso");
		m.put("Low Severity", "Gravità Bassa");
		m.put("MERGED", "UNITO");
		m.put("MS Teams Notifications", "Notifiche MS Teams");
		m.put("Mail", "Mail");
		m.put("Mail Connector", "Connettore Mail");
		m.put("Mail Connector Bean", "Bean Connettore Mail");
		m.put("Mail Service", "Servizio di Posta");
		m.put("Mail Service Test", "Test del Servizio di Posta");
		m.put("Mail service not configured", "Servizio di posta non configurato");
		m.put("Mail service settings saved", "Impostazioni del servizio di posta salvate");
		m.put("Make sure <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 or higher</a> is installed", 
			"Assicurati che <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 o superiore</a> sia installato");
		m.put("Make sure current user has permission to run docker containers", "Assicurati che l'utente corrente abbia il permesso di eseguire contenitori docker");
		m.put("Make sure docker engine is installed and docker command line is available in system path", 
			"Assicurati che il motore docker sia installato e che la linea di comando docker sia disponibile nel percorso di sistema");
		m.put("Make sure git version 2.11.1 or higher is installed and available in system path", "Assicurati che la versione di git 2.11.1 o superiore sia installata e disponibile nel percorso di sistema");
		m.put("Make sure git-lfs is installed and available in system path if you want to retrieve LFS files", 
			"Assicurati che git-lfs sia installato e disponibile nel percorso di sistema se desideri recuperare file LFS");
		m.put("Make sure the access token has package read permission over the project", "Assicurati che il token di accesso abbia il permesso di lettura del pacchetto sul progetto");
		m.put("Make sure the access token has package write permission over the project", "Assicurati che il token di accesso abbia il permesso di scrittura del pacchetto sul progetto");
		m.put("Make sure the access token has package write permission over the project. Also make sure to run command <code>chmod 0600 $HOME/.gem/credentials</code> after creating the file", 
			"Assicurati che il token di accesso abbia il permesso di scrittura del pacchetto sul progetto. Assicurati inoltre di eseguire il comando <code>chmod 0600 $HOME/.gem/credentials</code> dopo aver creato il file");
		m.put("Make sure the account has package ${permission} permission over the project", "Assicurati che l'account abbia il permesso di ${permission} del pacchetto sul progetto");
		m.put("Make sure the account has package read permission over the project", "Assicurati che l'account abbia il permesso di lettura del pacchetto sul progetto");
		m.put("Make sure the user has package write permission over the project", "Assicurati che l'utente abbia il permesso di scrittura del pacchetto sul progetto");
		m.put("Malformed %sbase query", "Query %sbase malformata");
		m.put("Malformed %squery", "Query %s malformata");
		m.put("Malformed build spec (import project: {0}, import revision: {1})", "Specifica di build malformata (importa progetto: {0}, importa revisione: {1})");
		m.put("Malformed email address", "Indirizzo email malformato");
		m.put("Malformed filter", "Filtro malformato");
		m.put("Malformed name filter", "Filtro nome malformato");
		m.put("Malformed query", "Query malformata");
		m.put("Malformed ssh signature", "Firma ssh malformata");
		m.put("Malformed test suite filter", "Filtro suite di test malformato");
		m.put("Manage Job", "Gestisci Lavoro");
		m.put("Manager DN", "DN del Manager");
		m.put("Manager Password", "Password del Manager");
		m.put("Manifest blob unknown", "Blob del manifesto sconosciuto");
		m.put("Manifest invalid", "Manifesto non valido");
		m.put("Manifest unknown", "Manifesto sconosciuto");
		m.put("Many commands print outputs with ANSI colors in TTY mode to help identifying problems easily. However some commands running in this mode may wait for user input to cause build hanging. This can normally be fixed by adding extra options to the command", 
			"Molti comandi stampano output con colori ANSI in modalità TTY per aiutare a identificare facilmente i problemi. Tuttavia, alcuni comandi eseguiti in questa modalità potrebbero attendere l'input dell'utente causando il blocco della build. Questo può essere normalmente risolto aggiungendo opzioni extra al comando");
		m.put("Mark a property archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived properties will not be shown by default", 
			"Segna una proprietà come archiviata se non è più utilizzata dalla specifica di build corrente, ma deve ancora esistere per riprodurre vecchie build. Le proprietà archiviate non verranno mostrate per impostazione predefinita");
		m.put("Mark a secret archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived secrets will not be shown by default", 
			"Segna un segreto come archiviato se non è più utilizzato dalla specifica di build corrente, ma deve ancora esistere per riprodurre vecchie build. I segreti archiviati non verranno mostrati per impostazione predefinita");
		m.put("Markdown", "Markdown");
		m.put("Markdown Report", "Report Markdown");
		m.put("Markdown from file", "Markdown da file");
		m.put("Maven(s)", "Maven(s)");
		m.put("Max Code Search Entries", "Massimo Numero di Voci per la Ricerca del Codice");
		m.put("Max Commit Message Line Length", "Lunghezza Massima della Linea del Messaggio di Commit");
		m.put("Max Git LFS File Size (MB)", "Dimensione Massima del File Git LFS (MB)");
		m.put("Max Retries", "Massimo Numero di Tentativi");
		m.put("Max Upload File Size (MB)", "Dimensione Massima del File di Upload (MB)");
		m.put("Max Value", "Valore Massimo");
		m.put("Maximum number of entries to return when search code in repository", "Numero massimo di voci da restituire durante la ricerca del codice nel repository");
		m.put("Maximum of retries before giving up", "Massimo numero di tentativi prima di arrendersi");
		m.put("May not be empty", "Non può essere vuoto");
		m.put("Medium", "Medio");
		m.put("Medium Severity", "Gravità Media");
		m.put("Members", "Membri");
		m.put("Memory", "Memoria");
		m.put("Memory Limit", "Limite di memoria");
		m.put("Memory Request", "Richiesta di memoria");
		m.put("Mention Someone", "Menziona qualcuno");
		m.put("Mention someone", "Menziona qualcuno");
		m.put("Merge", "Unisci");
		m.put("Merge Strategy", "Strategia di unione");
		m.put("Merge Target Branch into Source Branch", "Unisci il branch di destinazione nel branch di origine");
		m.put("Merge branch \"{0}\" into branch \"{1}\"", "Unisci il branch \"{0}\" nel branch \"{1}\"");
		m.put("Merge branch \"{0}\" of project \"{1}\" into branch \"{2}\"", "Unisci il branch \"{0}\" del progetto \"{1}\" nel branch \"{2}\"");
		m.put("Merge preview not calculated yet", "Anteprima di unione non ancora calcolata");
		m.put("Merged", "Unito");
		m.put("Merged pull request \"{0}\" ({1})", "Pull request unita \"{0}\" ({1})");
		m.put("Merges pull request", "Unisce pull request");
		m.put("Meta", "Meta");
		m.put("Meta Info", "Informazioni Meta");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "Valore minimo");
		m.put("Minimum length of the password", "Lunghezza minima della password");
		m.put("Missing Commit", "Commit mancante");
		m.put("Missing Commits", "Commit mancanti");
		m.put("Month", "Mese");
		m.put("Months", "Mesi");
		m.put("Months to Display", "Mesi da visualizzare");
		m.put("More", "Altro");
		m.put("More Options", "Altre opzioni");
		m.put("More Settings", "Altre impostazioni");
		m.put("More commits", "Altri commit");
		m.put("More info", "Altre informazioni");
		m.put("More operations", "Altre operazioni");
		m.put("Most branch coverage", "Copertura di branch più alta");
		m.put("Most line coverage", "Copertura di linea più alta");
		m.put("Most probably there are import errors in the <a wicket:id=\"buildSpec\">build spec</a>", 
			"Molto probabilmente ci sono errori di importazione nello <a wicket:id=\"buildSpec\">build spec</a>");
		m.put("Mount Docker Sock", "Montare Docker Sock");
		m.put("Move All Queried Issues To...", "Sposta tutte le issue interrogate in...");
		m.put("Move All Queried Projects To...", "Sposta tutti i progetti interrogati in...");
		m.put("Move Selected Issues To...", "Sposta le issue selezionate in...");
		m.put("Move Selected Projects To...", "Sposta i progetti selezionati in...");
		m.put("Multiple Lines", "Linee multiple");
		m.put("Multiple On the Other Side", "Multiplo dall'altra parte");
		m.put("Must not be empty", "Non deve essere vuoto");
		m.put("My Access Tokens", "I miei token di accesso");
		m.put("My Basic Settings", "Le mie impostazioni di base");
		m.put("My Email Addresses", "I miei indirizzi email");
		m.put("My GPG Keys", "Le mie chiavi GPG");
		m.put("My Profile", "Il mio profilo");
		m.put("My SSH Keys", "Le mie chiavi SSH");
		m.put("My SSO Accounts", "I Miei Account SSO");
		m.put("Mypy Report", "Rapporto Mypy");
		m.put("N/A", "N/D");
		m.put("NPM(s)", "NPM(s)");
		m.put("Name", "Nome");
		m.put("Name Of Empty Value", "Nome del valore vuoto");
		m.put("Name On the Other Side", "Nome dall'altra parte");
		m.put("Name Prefix", "Prefisso del nome");
		m.put("Name already used by another access token of the owner", "Nome già utilizzato da un altro token di accesso del proprietario");
		m.put("Name already used by another link", "Nome già utilizzato da un altro link");
		m.put("Name and name on the other side should be different", "Nome e nome dall'altra parte devono essere diversi");
		m.put("Name containing spaces or starting with dash needs to be quoted", "Il nome contenente spazi o che inizia con un trattino deve essere racchiuso tra virgolette");
		m.put("Name invalid", "Nome non valido");
		m.put("Name of the link", "Nome del link");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"Nome del link dall'altra parte. Ad esempio, se il nome è <tt>sub issues</tt>, il nome dall'altra parte può essere <tt>parent issue</tt>");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"Il nome del provider avrà due scopi: <ul><li>Visualizzazione sul pulsante di login<li>Formazione dell'URL di callback per l'autorizzazione che sarà <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>");
		m.put("Name reversely", "Nome inverso");
		m.put("Name unknown", "Nome sconosciuto");
		m.put("Name your file", "Nomina il tuo file");
		m.put("Named Agent Queries Bean", "Bean di query agenti nominati");
		m.put("Named Agent Query", "Query agente nominata");
		m.put("Named Build Queries Bean", "Bean di query build nominati");
		m.put("Named Build Query", "Query build nominata");
		m.put("Named Code Comment Queries Bean", "Bean di query commenti sul codice nominati");
		m.put("Named Code Comment Query", "Query commento sul codice nominata");
		m.put("Named Commit Queries Bean", "Bean di query commit nominati");
		m.put("Named Commit Query", "Query commit nominata");
		m.put("Named Element", "Elemento nominato");
		m.put("Named Issue Queries Bean", "Bean di query issue nominati");
		m.put("Named Issue Query", "Query issue nominata");
		m.put("Named Pack Queries Bean", "Bean di query pacchetti nominati");
		m.put("Named Pack Query", "Query pacchetto nominata");
		m.put("Named Project Queries Bean", "Bean di query progetti nominati");
		m.put("Named Project Query", "Query progetto nominata");
		m.put("Named Pull Request Queries Bean", "Bean di query pull request nominati");
		m.put("Named Pull Request Query", "Query pull request nominata");
		m.put("Named Query", "Query nominata");
		m.put("Network Options", "Opzioni di rete");
		m.put("Never", "Mai");
		m.put("Never expire", "Non scadere mai");
		m.put("New Board", "Nuova bacheca");
		m.put("New Invitation Bean", "Bean di invito nuovo");
		m.put("New Issue", "Nuova issue");
		m.put("New Password", "Nuova password");
		m.put("New State", "Nuovo stato");
		m.put("New User Bean", "Bean utente nuovo");
		m.put("New Value", "Nuovo valore");
		m.put("New issue board created", "Nuova bacheca di issue creata");
		m.put("New project created", "Nuovo progetto creato");
		m.put("New user created", "Nuovo utente creato");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"Nuova versione disponibile. Rosso per aggiornamenti di sicurezza/critici, giallo per correzioni di bug, blu per aggiornamenti di funzionalità. Clicca per mostrare le modifiche. Disabilita nelle impostazioni di sistema");
		m.put("Next", "Successivo");
		m.put("Next commit", "Prossimo commit");
		m.put("Next {0}", "Prossimo {0}");
		m.put("No", "No");
		m.put("No Activity Days", "Giorni senza attività");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"Nessuna chiave SSH configurata nel tuo account. Puoi <a wicket:id=\"sshKeys\" class=\"link-primary\">aggiungere una chiave</a> o passare all'url <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a>");
		m.put("No SSL", "Nessun SSL");
		m.put("No accessible reports", "Nessun report accessibile");
		m.put("No activity for some time", "Nessuna attività per un po' di tempo");
		m.put("No agents to pause", "Nessun agente da mettere in pausa");
		m.put("No agents to remove", "Nessun agente da rimuovere");
		m.put("No agents to restart", "Nessun agente da riavviare");
		m.put("No agents to resume", "Nessun agente da riprendere");
		m.put("No aggregation", "Nessuna aggregazione");
		m.put("No any", "Nessuno");
		m.put("No any matches", "Nessuna corrispondenza");
		m.put("No applicable transitions or no permission to transit", "Nessuna transizione applicabile o nessun permesso per transitare");
		m.put("No attributes defined (can only be edited when agent is online)", "Nessun attributo definito (modificabile solo quando l'agente è online)");
		m.put("No audits", "Nessun controllo");
		m.put("No authorized job secret found (project: {0}, job secret: {1})", "Nessun segreto autorizzato trovato (progetto: {0}, segreto: {1})");
		m.put("No branch to cherry-pick to", "Nessun branch su cui effettuare il cherry-pick");
		m.put("No branch to revert on", "Nessun branch su cui effettuare il revert");
		m.put("No branches Found", "Nessun branch trovato");
		m.put("No branches found", "Nessun branch trovato");
		m.put("No build in query context", "Nessuna build nel contesto della query");
		m.put("No builds", "Nessuna build");
		m.put("No builds to cancel", "Nessuna build da annullare");
		m.put("No builds to delete", "Nessuna build da eliminare");
		m.put("No builds to re-run", "Nessuna build da rieseguire");
		m.put("No comment", "Nessun commento");
		m.put("No comments to delete", "Nessun commento da eliminare");
		m.put("No comments to set as read", "Nessun commento da impostare come letto");
		m.put("No comments to set resolved", "Nessun commento da impostare come risolto");
		m.put("No comments to set unresolved", "Nessun commento da impostare come non risolto");
		m.put("No commit in query context", "Nessun commit nel contesto della query");
		m.put("No config file", "Nessun file di configurazione");
		m.put("No current build in query context", "Nessuna build corrente nel contesto della query");
		m.put("No current commit in query context", "Nessun commit corrente nel contesto della query");
		m.put("No current pull request in query context", "Nessuna pull request corrente nel contesto della query");
		m.put("No data", "Nessun dato");
		m.put("No default branch", "Nessun branch predefinito");
		m.put("No default group", "Nessun gruppo predefinito");
		m.put("No default roles", "Nessun ruolo predefinito");
		m.put("No default value", "Nessun valore predefinito");
		m.put("No description", "Nessuna descrizione");
		m.put("No diffs", "Nessuna differenza");
		m.put("No diffs to navigate", "Nessuna differenza da navigare");
		m.put("No directories to skip", "Nessuna directory da saltare");
		m.put("No disallowed file types", "Nessun tipo di file non consentito");
		m.put("No executors defined. Jobs will use auto-discovered executors instead", "Nessun executor definito. I lavori utilizzeranno executor rilevati automaticamente");
		m.put("No external password authenticator", "Nessun autenticatore di password esterna");
		m.put("No external password authenticator to authenticate user \"{0}\"", "Nessun autenticatore di password esterna per autenticare l'utente \"{0}\"");
		m.put("No fields to prompt", "Nessun campo da richiedere");
		m.put("No fields to remove", "Nessun campo da rimuovere");
		m.put("No file attachments", "Nessun allegato di file");
		m.put("No group by", "Nessun raggruppamento");
		m.put("No groups claim returned", "Nessuna rivendicazione di gruppi restituita");
		m.put("No groups to remove from", "Nessun gruppo da cui rimuovere");
		m.put("No ignore file", "Nessun file di ignoranza");
		m.put("No ignored licenses", "Nessuna licenza ignorata");
		m.put("No image attachments", "Nessun allegato di immagine");
		m.put("No imports defined", "Nessuna importazione definita");
		m.put("No issue boards defined", "Nessuna bacheca di problemi definita");
		m.put("No issues in iteration", "Nessun problema nell'iterazione");
		m.put("No issues to copy", "Nessun problema da copiare");
		m.put("No issues to delete", "Nessun problema da eliminare");
		m.put("No issues to edit", "Nessun problema da modificare");
		m.put("No issues to export", "Nessun problema da esportare");
		m.put("No issues to move", "Nessun problema da spostare");
		m.put("No issues to set as read", "Nessun problema da impostare come letto");
		m.put("No issues to sync estimated/spent time", "Nessun problema da sincronizzare tempo stimato/speso");
		m.put("No issues to watch/unwatch", "Nessun problema da osservare/non osservare");
		m.put("No jobs defined", "Nessun job definito");
		m.put("No jobs found", "Nessun job trovato");
		m.put("No limit", "Nessun limite");
		m.put("No mail service", "Nessun servizio di posta");
		m.put("No obvious changes", "Nessuna modifica evidente");
		m.put("No one", "Nessuno");
		m.put("No packages to delete", "Nessun pacchetto da eliminare");
		m.put("No parent", "Nessun genitore");
		m.put("No previous successful build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a> to calculate fixed issues since", 
			"Nessuna build precedente riuscita su <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">stesso stream</a> per calcolare i problemi risolti da allora");
		m.put("No projects found", "Nessun progetto trovato");
		m.put("No projects to delete", "Nessun progetto da eliminare");
		m.put("No projects to modify", "Nessun progetto da modificare");
		m.put("No projects to move", "Nessun progetto da spostare");
		m.put("No properties defined", "Nessuna proprietà definita");
		m.put("No proxy", "Nessun proxy");
		m.put("No pull request in query context", "Nessuna pull request nel contesto della query");
		m.put("No pull requests to delete", "Nessuna pull request da eliminare");
		m.put("No pull requests to discard", "Nessuna pull request da scartare");
		m.put("No pull requests to set as read", "Nessuna pull request da impostare come letta");
		m.put("No pull requests to watch/unwatch", "Nessuna pull request da osservare/non osservare");
		m.put("No refs to build on behalf of", "Nessun ref su cui costruire per conto di");
		m.put("No required services", "Nessun servizio richiesto");
		m.put("No response body", "Nessun corpo di risposta");
		m.put("No secret config", "Nessuna configurazione segreta");
		m.put("No services defined", "Nessun servizio definito");
		m.put("No start/due date", "Nessuna data di inizio/scadenza");
		m.put("No step templates defined", "Nessun modello di step definito");
		m.put("No suggestions", "Nessun suggerimento");
		m.put("No tags found", "Nessun tag trovato");
		m.put("No timesheets defined", "Nessun foglio ore definito");
		m.put("No user found with login name or email: ", "Nessun utente trovato con nome di login o email:");
		m.put("No users to convert to service accounts", "Nessun utente da convertire in account di servizio");
		m.put("No users to delete", "Nessun utente da eliminare");
		m.put("No users to disable", "Nessun utente da disabilitare");
		m.put("No users to enable", "Nessun utente da abilitare");
		m.put("No users to remove from group", "Nessun utente da rimuovere dal gruppo");
		m.put("No valid query to show progress", "Nessuna query valida per mostrare il progresso");
		m.put("No valid signature for head commit", "Nessuna firma valida per il commit principale");
		m.put("No valid signature for head commit of target branch", "Nessuna firma valida per il commit principale del branch di destinazione");
		m.put("No value", "Nessun valore");
		m.put("No verified primary email address", "Nessun indirizzo email primario verificato");
		m.put("Node Selector", "Selettore di Nodo");
		m.put("Node Selector Entry", "Voce del Selettore di Nodo");
		m.put("None", "Nessuno");
		m.put("Not Active Since", "Non attivo da");
		m.put("Not Used Since", "Non utilizzato da");
		m.put("Not a verified email of signing GPG key", "Email non verificata della chiave GPG di firma");
		m.put("Not a verified email of signing ssh key owner", "Email non verificata del proprietario della chiave ssh di firma");
		m.put("Not allowed file type: {0}", "Tipo di file non consentito: {0}");
		m.put("Not assigned", "Non assegnato");
		m.put("Not authorized to create project under \"{0}\"", "Non autorizzato a creare un progetto sotto \"{0}\"");
		m.put("Not authorized to create root project", "Non autorizzato a creare un progetto root");
		m.put("Not authorized to move project under this parent", "Non autorizzato a spostare il progetto sotto questo genitore");
		m.put("Not authorized to set as root project", "Non autorizzato a impostare come progetto radice");
		m.put("Not covered", "Non coperto");
		m.put("Not covered by any test", "Non coperto da alcun test");
		m.put("Not displaying any fields", "Non visualizza alcun campo");
		m.put("Not displaying any links", "Non visualizza alcun collegamento");
		m.put("Not passed", "Non superato");
		m.put("Not rendered in failsafe mode", "Non renderizzato in modalità failsafe");
		m.put("Not run", "Non eseguito");
		m.put("Not specified", "Non specificato");
		m.put("Note", "Nota");
		m.put("Nothing to preview", "Niente da visualizzare in anteprima");
		m.put("Notification", "Notifica");
		m.put("Notifications", "Notifiche");
		m.put("Notify Build Events", "Notifica Eventi di Build");
		m.put("Notify Code Comment Events", "Notifica Eventi di Commento al Codice");
		m.put("Notify Code Push Events", "Notifica Eventi di Push al Codice");
		m.put("Notify Issue Events", "Notifica Eventi di Problemi");
		m.put("Notify Own Events", "Notifica Eventi Propri");
		m.put("Notify Pull Request Events", "Notifica Eventi di Pull Request");
		m.put("Notify Users", "Notifica Utenti");
		m.put("Ntfy.sh Notifications", "Notifiche Ntfy.sh");
		m.put("NuGet(s)", "NuGet(s)");
		m.put("NuSpec", "NuSpec");
		m.put("Number of CPU Cores", "Numero di Core CPU");
		m.put("Number of SSH Keys", "Numero di Chiavi SSH");
		m.put("Number of builds to preserve", "Numero di build da preservare");
		m.put("Number of project replicas, including primary and backups", "Numero di repliche del progetto, incluse primarie e di backup");
		m.put("Number of recent months to show statistics for", "Numero di mesi recenti per mostrare le statistiche");
		m.put("OAuth2 Client information | CLIENT ID", "Informazioni Client OAuth2 | CLIENT ID");
		m.put("OAuth2 Client information | CLIENT SECRET", "Informazioni Client OAuth2 | CLIENT SECRET");
		m.put("OCI Layout Directory", "Directory Layout OCI");
		m.put("OIDC error: Inconsistent sub in ID token and userinfo", "Errore OIDC: Sub incoerente nel token ID e userinfo");
		m.put("OOPS! There Is An Error", "OOPS! C'è un errore");
		m.put("OPEN", "APERTA");
		m.put("OS", "Sistema Operativo");
		m.put("OS Arch", "Architettura OS");
		m.put("OS User Name", "Nome Utente OS");
		m.put("OS Version", "Versione OS");
		m.put("OS/ARCH", "OS/ARCH");
		m.put("Offline", "Offline");
		m.put("Ok", "Ok");
		m.put("Old Name", "Vecchio Nome");
		m.put("Old Password", "Vecchia Password");
		m.put("On Behalf Of", "Per Conto di");
		m.put("On Branches", "Su Branches");
		m.put("OneDev Issue Field", "Campo Problema OneDev");
		m.put("OneDev Issue Link", "Collegamento Problema OneDev");
		m.put("OneDev Issue State", "Stato Problema OneDev");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"OneDev analizza i file del repository per la ricerca del codice, statistiche di linea e statistiche di contributo al codice. Questa impostazione indica quali file devono essere analizzati e si aspetta <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>pattern di percorso</a> separati da spazi. Un pattern può essere escluso anteponendo '-', ad esempio <code>-**/vendors/**</code> escluderà tutti i file con vendors nel percorso. <b>NOTA: </b> Cambiare questa impostazione influisce solo sui nuovi commit. Per applicare la modifica ai commit storici, interrompere il server ed eliminare la cartella <code>index</code> e <code>info/commit</code> sotto la <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>directory di archiviazione del progetto</a>. Il repository sarà rianalizzato quando il server verrà avviato");
		m.put("OneDev configures git hooks to communicate with itself via curl", "OneDev configura i git hooks per comunicare con se stesso tramite curl");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"OneDev deve cercare e determinare il DN dell'utente, oltre a cercare informazioni sul gruppo utente se il recupero del gruppo è abilitato. Seleziona questa opzione e specifica il DN e la password del 'manager' se queste operazioni devono essere autenticate");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"OneDev richiede la linea di comando git per gestire i repository. La versione minima richiesta è 2.11.1. Assicurati anche che git-lfs sia installato se desideri recuperare file LFS nel job di build");
		m.put("Online", "Online");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", 
			"Crea il commit di merge solo se il branch di destinazione non può essere avanzato velocemente al branch sorgente");
		m.put("Only projects manageable by access token owner can be authorized", "Solo i progetti gestibili dal proprietario del token di accesso possono essere autorizzati");
		m.put("Only system level audit events are displayed here. To view audit events for a specific project, please visit the project audit log page", 
			"Qui vengono visualizzati solo gli eventi di controllo a livello di sistema. Per visualizzare gli eventi di controllo di un progetto specifico, visita la pagina del registro di controllo del progetto");
		m.put("Only users able to authenticate via password can be linked", "Solo gli utenti in grado di autenticarsi tramite password possono essere collegati");
		m.put("Open", "Apri");
		m.put("Open new pull request", "Apri nuova pull request");
		m.put("Open terminal of current running step", "Apri terminale del passo corrente in esecuzione");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"L'identificazione del client OpenID sarà assegnata dal tuo provider OpenID durante la registrazione di questa istanza OneDev come applicazione client");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"Il segreto del client OpenID sarà generato dal tuo provider OpenID durante la registrazione di questa istanza OneDev come applicazione client");
		m.put("OpenSSH Public Key", "Chiave Pubblica OpenSSH");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"La chiave pubblica OpenSSH inizia con 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com' o 'sk-ssh-ed25519@openssh.com'");
		m.put("Opened issue \"{0}\" ({1})", "Problema aperto \"{0}\" ({1})");
		m.put("Opened pull request \"{0}\" ({1})", "Pull request aperta \"{0}\" ({1})");
		m.put("Operation", "Operazione");
		m.put("Operation Failed", "Operazione Fallita");
		m.put("Operation Successful", "Operazione Riuscita");
		m.put("Operations", "Operazioni");
		m.put("Optional", "Opzionale");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"Specifica opzionalmente il progetto in cui creare il problema. Lascia vuoto per creare nel progetto corrente");
		m.put("Optionally add new users to specified default group", "Aggiungi opzionalmente nuovi utenti al gruppo predefinito specificato");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"Aggiungi opzionalmente il nuovo utente autenticato al gruppo specificato se le informazioni di appartenenza non sono disponibili");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"Aggiungi opzionalmente il nuovo utente autenticato al gruppo specificato se le informazioni di appartenenza non sono recuperate");
		m.put("Optionally choose required builds. You may also input jobs not listed here, and press ENTER to add them", 
			"Scegli opzionalmente i build richiesti. Puoi anche inserire job non elencati qui e premere INVIO per aggiungerli");
		m.put("Optionally configure proxy to access remote repository. Proxy should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;", 
			"Configura opzionalmente un proxy per accedere al repository remoto. Il proxy dovrebbe essere nel formato &lt;proxy host&gt;:&lt;proxy port&gt;");
		m.put("Optionally define a unique key for the project with two or more upper case letters. This key can be used to reference issues, builds, and pull requests with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of <code>&lt;project path&gt;#&lt;number&gt;</code>", 
			"Definisci opzionalmente una chiave unica per il progetto con due o più lettere maiuscole. Questa chiave può essere utilizzata per fare riferimento a problemi, build e pull request con una forma stabile e breve <code>&lt;project key&gt;-&lt;number&gt;</code> invece di <code>&lt;project path&gt;#&lt;number&gt;</code>");
		m.put("Optionally define parameter specifications of the job", "Definisci opzionalmente le specifiche dei parametri del job");
		m.put("Optionally define parameter specifications of the step template", "Definisci opzionalmente le specifiche dei parametri del modello di passo");
		m.put("Optionally describe the group", "Descrivi opzionalmente il gruppo");
		m.put("Optionally describes the custom field. Html tags are accepted", "Descrive opzionalmente il campo personalizzato. Sono accettati tag HTML");
		m.put("Optionally describes the param. Html tags are accepted.", "Descrive opzionalmente il parametro. Sono accettati tag HTML.");
		m.put("Optionally filter builds", "Filtra opzionalmente i build");
		m.put("Optionally filter issues", "Filtra opzionalmente i problemi");
		m.put("Optionally filter pull requests", "Filtra opzionalmente le pull request");
		m.put("Optionally leave a note", "Lascia opzionalmente una nota");
		m.put("Optionally mount directories or files under job workspace into container", "Monta opzionalmente directory o file sotto lo spazio di lavoro del job nel container");
		m.put("Optionally select fields to prompt when this button is pressed", "Seleziona opzionalmente i campi da visualizzare quando questo pulsante viene premuto");
		m.put("Optionally select fields to remove when this transition happens", "Seleziona opzionalmente i campi da rimuovere quando avviene questa transizione");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"Specifica opzionalmente il nome dell'attributo all'interno della voce LDAP dell'utente il cui valore sarà preso come email dell'utente. Questo campo è normalmente impostato su <i>mail</i> secondo RFC 2798");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"Specifica opzionalmente il nome dell'attributo all'interno della voce LDAP dell'utente il cui valore sarà preso come nome completo dell'utente. Questo campo è normalmente impostato su <i>displayName</i> secondo RFC 2798. Se lasciato vuoto, il nome completo dell'utente non sarà recuperato");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"Specifica facoltativamente <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>il segreto del job</a> da utilizzare come token di accesso a GitHub. Questo viene utilizzato per recuperare le note di rilascio delle dipendenze ospitate su GitHub, e l'accesso autenticato avrà un limite di velocità più alto");
		m.put("Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko", 
			"Specifica facoltativamente <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>opzioni aggiuntive</a> di kaniko");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane", 
			"Specifica facoltativamente <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>opzioni aggiuntive</a> di crane");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane", 
			"Specifica facoltativamente <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>opzioni aggiuntive</a> di crane");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to build for platform of the node running the job", 
			"Specifica facoltativamente <span class='text-info'>piattaforme separate da virgola</span> da costruire, ad esempio <tt>linux/amd64,linux/arm64</tt>. Lascia vuoto per costruire per la piattaforma del nodo che esegue il job");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to scan, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to scan all platforms in OCI layout", 
			"Specifica facoltativamente <span class='text-info'>piattaforme separate da virgola</span> da scansionare, ad esempio <tt>linux/amd64,linux/arm64</tt>. Lascia vuoto per scansionare tutte le piattaforme nel layout OCI");
		m.put("Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use file <tt>Dockerfile</tt> under build path specified above", 
			"Specifica facoltativamente il Dockerfile relativo a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Lascia vuoto per utilizzare il file <tt>Dockerfile</tt> sotto il percorso di build specificato sopra");
		m.put("Optionally specify JavaScript config to be used by Renovate CLI", "Specifica facoltativamente la configurazione JavaScript da utilizzare con la CLI di Renovate");
		m.put("Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. Leave empty to derive from server url", 
			"Specifica facoltativamente l'URL root SSH, che verrà utilizzato per costruire l'URL di clonazione del progetto tramite protocollo SSH. Lascia vuoto per derivarlo dall'URL del server");
		m.put("Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of the text input", 
			"Specifica facoltativamente un <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>modello di espressione regolare</a> per valori validi dell'input di testo");
		m.put("Optionally specify a OneDev project to be used as parent of imported projects. Leave empty to import as root projects", 
			"Specifica facoltativamente un progetto OneDev da utilizzare come genitore dei progetti importati. Lascia vuoto per importare come progetti root");
		m.put("Optionally specify a OneDev project to be used as parent of imported repositories. Leave empty to import as root projects", 
			"Specifica facoltativamente un progetto OneDev da utilizzare come genitore dei repository importati. Lascia vuoto per importare come progetti root");
		m.put("Optionally specify a base query for the list", "Specifica facoltativamente una query di base per la lista");
		m.put("Optionally specify a base query to filter/order issues in backlog. Backlog issues are those not associating with current iteration", 
			"Specifica facoltativamente una query di base per filtrare/ordinare i problemi nel backlog. I problemi nel backlog sono quelli non associati all'iterazione corrente");
		m.put("Optionally specify a base query to filter/order issues of the board", "Specifica facoltativamente una query di base per filtrare/ordinare i problemi della board");
		m.put("Optionally specify a cron expression to schedule database auto-backup. The cron expression format is <em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>.For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz tutorial</a>.The backup files will be placed into <em>db-backup</em> folder under OneDev installation directory. In case multiple servers connect to form a cluster, auto-backup takes place on the <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>lead server</a>. Leave this property empty if you do not want to enable database auto backup.", 
			"Specifica facoltativamente un'espressione cron per programmare il backup automatico del database. Il formato dell'espressione cron è <em>&lt;secondi&gt; &lt;minuti&gt; &lt;ore&gt; &lt;giorno-del-mese&gt; &lt;mese&gt; &lt;giorno-della-settimana&gt;</em>. Ad esempio, <em>0 0 1 * * ?</em> significa alle 1:00 di ogni giorno. Per i dettagli sul formato, consulta il <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>tutorial di Quartz</a>. I file di backup verranno posizionati nella cartella <em>db-backup</em> sotto la directory di installazione di OneDev. Nel caso in cui più server si connettano per formare un cluster, il backup automatico avviene sul <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>server principale</a>. Lascia questa proprietà vuota se non vuoi abilitare il backup automatico del database.");
		m.put("Optionally specify a date field to hold due date information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica facoltativamente un campo data per contenere informazioni sulla data di scadenza.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to put retrieved artifacts. Leave empty to use job workspace itself", 
			"Specifica facoltativamente un percorso relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> per posizionare gli artefatti recuperati. Lascia vuoto per utilizzare il job workspace stesso");
		m.put("Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. <b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, as the volume is only used to hold temporary build files", 
			"Specifica facoltativamente una classe di archiviazione per allocare dinamicamente il volume di build. Lascia vuoto per utilizzare la classe di archiviazione predefinita. <b class='text-warning'>NOTA:</b> La politica di recupero della classe di archiviazione dovrebbe essere impostata su <code>Delete</code>, poiché il volume viene utilizzato solo per contenere file di build temporanei");
		m.put("Optionally specify a working period field to hold estimated time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica facoltativamente un campo di periodo lavorativo per contenere informazioni sul tempo stimato.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Optionally specify a working period field to hold spent time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica facoltativamente un campo di periodo lavorativo per contenere informazioni sul tempo trascorso.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Optionally specify a working period field to hold time estimate infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica facoltativamente un campo di periodo lavorativo per contenere informazioni sulla stima del tempo.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Optionally specify a working period field to hold time spent infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica facoltativamente un campo di periodo lavorativo per contenere informazioni sul tempo trascorso.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Optionally specify additional options for buildx build command", "Specifica facoltativamente opzioni aggiuntive per il comando di buildx build");
		m.put("Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, the response header <code>Access-Control-Allow-Origin</code> will be set to the same value", 
			"Specifica facoltativamente origini <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> consentite. Per una richiesta CORS semplice o preflight, se il valore dell'intestazione della richiesta <code>Origin</code> è incluso qui, l'intestazione della risposta <code>Access-Control-Allow-Origin</code> verrà impostata sullo stesso valore");
		m.put("Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match", 
			"Specifica facoltativamente il dominio email consentito per gli utenti che si registrano autonomamente. Usa '*' o '?' per la corrispondenza del modello");
		m.put("Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). Leave empty to all types", 
			"Specifica facoltativamente i tipi di commit applicabili per il controllo del piè di pagina del messaggio di commit (premi INVIO per aggiungere un valore). Lascia vuoto per tutti i tipi");
		m.put("Optionally specify applicable jobs of this executor", "Specifica facoltativamente i lavori applicabili di questo executor");
		m.put("Optionally specify applicable users who pushed the change", "Specifica facoltativamente gli utenti applicabili che hanno effettuato il push della modifica");
		m.put("Optionally specify arguments to run above image", "Specifica facoltativamente gli argomenti per eseguire l'immagine sopra");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"Specifica facoltativamente gli artefatti da recuperare dalla dipendenza in <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Possono essere recuperati solo artefatti pubblicati (tramite il passaggio di pubblicazione degli artefatti). Lascia vuoto per non recuperare alcun artefatto");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"Specifica facoltativamente i ruoli autorizzati per premere questo pulsante. Se non specificato, tutti gli utenti sono autorizzati");
		m.put("Optionally specify base query of the list", "Specifica facoltativamente la query di base della lista");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"Specifica facoltativamente i rami/utenti/gruppi autorizzati ad accedere a questo segreto. Se lasciato vuoto, qualsiasi job può accedere a questo segreto, inclusi quelli attivati tramite richieste pull esterne");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"Specifica facoltativamente il percorso del contesto di build relativo a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Lascia vuoto per utilizzare il job workspace stesso. Il file <code>Dockerfile</code> dovrebbe esistere nella directory del contesto di build, a meno che tu non specifichi una posizione diversa con l'opzione <code>--dockerfile</code>");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"Specifica facoltativamente il percorso di build relativo a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Lascia vuoto per utilizzare il job workspace stesso");
		m.put("Optionally specify cluster role the job pods service account binding to. This is necessary if you want to do things such as running other Kubernetes pods in job command", 
			"Specifica facoltativamente il ruolo del cluster a cui è associato l'account di servizio dei pod del job. Questo è necessario se vuoi fare cose come eseguire altri pod Kubernetes nel comando del job");
		m.put("Optionally specify comma separated licenses to be ignored", "Specifica facoltativamente le licenze separate da virgola da ignorare");
		m.put("Optionally specify container arguments separated by space. Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse this with container options which should be specified in executor setting", 
			"Specifica facoltativamente gli argomenti del container separati da spazio. Un singolo argomento contenente spazio dovrebbe essere quotato. <b class='text-warning'>Nota: </b> non confondere questo con le opzioni del container che dovrebbero essere specificate nelle impostazioni dell'esecutore");
		m.put("Optionally specify cpu limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Specifica facoltativamente il limite di CPU per ogni job/servizio che utilizza questo esecutore. Consulta <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>la gestione delle risorse di Kubernetes</a> per i dettagli");
		m.put("Optionally specify cpu limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> of relevant containers", 
			"Specifica facoltativamente il limite di CPU di ogni job/servizio che utilizza questo esecutore. Questo verrà utilizzato come opzione <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> dei container rilevanti");
		m.put("Optionally specify criteria of issues which can be linked", "Specifica facoltativamente i criteri dei problemi che possono essere collegati");
		m.put("Optionally specify criteria of issues which can be linked on the other side", "Specifica facoltativamente i criteri dei problemi che possono essere collegati dall'altra parte");
		m.put("Optionally specify custom fields allowed to edit when open new issues", "Specifica facoltativamente i campi personalizzati consentiti per la modifica quando si aprono nuovi problemi");
		m.put("Optionally specify depth for a shallow clone in order to speed up source retrieval", 
			"Specifica facoltativamente la profondità per un clone superficiale al fine di velocizzare il recupero del codice sorgente");
		m.put("Optionally specify description of the issue", "Specifica facoltativamente la descrizione del problema");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"Specifica facoltativamente le directory o i modelli glob all'interno del percorso di scansione da saltare. Più salti dovrebbero essere separati da spazio");
		m.put("Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types", 
			"Specifica facoltativamente i tipi di file non consentiti tramite estensioni (premi INVIO per aggiungere valore), ad esempio <code>exe</code>, <code>bin</code>. Lascia vuoto per consentire tutti i tipi di file");
		m.put("Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. Leave empty to use docker executable in PATH", 
			"Specifica facoltativamente l'eseguibile docker, ad esempio <i>/usr/local/bin/docker</i>. Lascia vuoto per utilizzare l'eseguibile docker nel PATH");
		m.put("Optionally specify docker options to create network. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Specifica facoltativamente le opzioni docker per creare la rete. Più opzioni dovrebbero essere separate da spazio, e una singola opzione contenente spazi dovrebbe essere quotata");
		m.put("Optionally specify docker options to run container. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Specifica facoltativamente le opzioni docker per eseguire il container. Più opzioni dovrebbero essere separate da spazio, e una singola opzione contenente spazi dovrebbe essere quotata");
		m.put("Optionally specify docker sock to use. Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows", 
			"Specifica facoltativamente il socket docker da utilizzare. Predefinito è <i>/var/run/docker.sock</i> su Linux, e <i>//./pipe/docker_engine</i> su Windows");
		m.put("Optionally specify environment variables for the container", "Specifica facoltativamente le variabili d'ambiente per il container");
		m.put("Optionally specify environment variables for this step", "Specifica facoltativamente le variabili d'ambiente per questo passaggio");
		m.put("Optionally specify environment variables of the service", "Specifica facoltativamente le variabili d'ambiente del servizio");
		m.put("Optionally specify estimated time.", "Specifica facoltativamente il tempo stimato.");
		m.put("Optionally specify executor for this job. Leave empty to use auto-discover executor", 
			"Specifica facoltativamente l'executor per questo lavoro. Lascia vuoto per utilizzare l'executor rilevato automaticamente");
		m.put("Optionally specify executor for this job. Leave empty to use first applicable executor", 
			"Specifica facoltativamente l'executor per questo lavoro. Lascia vuoto per utilizzare il primo executor applicabile");
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"Specifica facoltativamente i file relativi al percorso della cache da ignorare quando si rilevano modifiche alla cache. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza dei modelli di percorso</a>. Più file dovrebbero essere separati da spazio, e un singolo file contenente spazi dovrebbe essere quotato");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"Specifica facoltativamente la base di ricerca del gruppo se vuoi recuperare informazioni sull'appartenenza al gruppo dell'utente. Ad esempio: <i>cn=Users, dc=example, dc=com</i>. Per assegnare permessi appropriati a un gruppo di Active Directory, dovrebbe essere definito un gruppo OneDev con lo stesso nome. Lascia vuoto per gestire le appartenenze ai gruppi sul lato OneDev");
		m.put("Optionally specify issue links allowed to edit", "Specifica facoltativamente i collegamenti ai problemi consentiti per la modifica");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "Specifica facoltativamente i problemi applicabili per questo modello. Lascia vuoto per tutti");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"Specifica facoltativamente i problemi applicabili per questa transizione. Lascia vuoto per tutti i problemi");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"Specifica facoltativamente i problemi applicabili per questa transizione. Lascia vuoto per tutti i problemi.");
		m.put("Optionally specify jobs allowed to use this script", "Specifica facoltativamente i job autorizzati a utilizzare questo script");
		m.put("Optionally specify memory limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Specifica facoltativamente il limite di memoria per ogni job/servizio che utilizza questo esecutore. Consulta <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>la gestione delle risorse di Kubernetes</a> per i dettagli");
		m.put("Optionally specify memory limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> of relevant containers", 
			"Specifica facoltativamente il limite di memoria di ogni job/servizio che utilizza questo esecutore. Questo verrà utilizzato come opzione <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> dei container rilevanti");
		m.put("Optionally specify merge strategy of created pull request. Leave empty to use default strategy of each project", 
			"Specifica facoltativamente la strategia di merge della pull request creata. Lascia vuoto per utilizzare la strategia predefinita di ogni progetto");
		m.put("Optionally specify message of the tag", "Specifica facoltativamente il messaggio del tag");
		m.put("Optionally specify name of the attribute inside the user LDAP entry whose values will be taken as user SSH keys. SSH keys will be managed by LDAP only if this field is set", 
			"Specifica facoltativamente il nome dell'attributo all'interno della voce LDAP dell'utente i cui valori verranno presi come chiavi SSH dell'utente. Le chiavi SSH verranno gestite da LDAP solo se questo campo è impostato");
		m.put("Optionally specify node selector of the job pods", "Specifica facoltativamente il selettore del nodo dei pod del job");
		m.put("Optionally specify options for docker builder prune command", "Specifica facoltativamente le opzioni per il comando docker builder prune");
		m.put("Optionally specify options for scp command. Multiple options need to be separated with space", 
			"Specifica facoltativamente le opzioni per il comando scp. Più opzioni devono essere separate da spazio");
		m.put("Optionally specify options for ssh command. Multiple options need to be separated with space", 
			"Specifica facoltativamente le opzioni per il comando ssh. Più opzioni devono essere separate da spazio");
		m.put("Optionally specify options passed to renovate cli. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Specifica facoltativamente le opzioni passate alla CLI di Renovate. Più opzioni dovrebbero essere separate da spazio, e una singola opzione contenente spazi dovrebbe essere quotata");
		m.put("Optionally specify osv scanner <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. You may ignore particular vulnerabilities via this file", 
			"Specifica facoltativamente il file di configurazione dello scanner osv <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> sotto <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Puoi ignorare particolari vulnerabilità tramite questo file");
		m.put("Optionally specify path protection rules", "Specifica facoltativamente le regole di protezione del percorso");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"Specifica facoltativamente il percorso relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> da utilizzare come file di ignoranza di trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"Specifica facoltativamente il percorso relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> da utilizzare come configurazione segreta di trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"Specifica facoltativamente il percorso relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> per pubblicare gli artefatti. Lascia vuoto per utilizzare il job workspace stesso");
		m.put("Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. Leave empty to pull all platforms in image", 
			"Specifica facoltativamente la piattaforma da pullare, ad esempio <tt>linux/amd64</tt>. Lascia vuoto per pullare tutte le piattaforme nell'immagine");
		m.put("Optionally specify project to show builds of. Leave empty to show builds of all projects with permissions", 
			"Specifica facoltativamente il progetto per mostrare i build. Lascia vuoto per mostrare i build di tutti i progetti con permessi");
		m.put("Optionally specify project to show issues of. Leave empty to show issues of all accessible projects", 
			"Specifica facoltativamente il progetto per mostrare i problemi. Lascia vuoto per mostrare i problemi di tutti i progetti accessibili");
		m.put("Optionally specify project to show packages of. Leave empty to show packages of all projects with permissions", 
			"Specifica facoltativamente il progetto per mostrare i pacchetti. Lascia vuoto per mostrare i pacchetti di tutti i progetti con permessi");
		m.put("Optionally specify ref of above job, for instance <i>refs/heads/main</i>. Use * for wildcard match", 
			"Specifica facoltativamente il ref del job sopra, ad esempio <i>refs/heads/main</i>. Usa * per la corrispondenza dei modelli");
		m.put("Optionally specify registry logins to override those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token secret for password secret", 
			"Specifica facoltativamente i login al registro per sovrascrivere quelli definiti nell'esecutore del job. Per il registro integrato, usa <code>@server_url@</code> per l'URL del registro, <code>@job_token@</code> per il nome utente e il segreto del token di accesso per la password");
		m.put("Optionally specify relative directory to put uploaded files", "Specifica facoltativamente la directory relativa per posizionare i file caricati");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to clone code into. Leave empty to use job workspace itself", 
			"Specifica facoltativamente il percorso relativo sotto <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> per clonare il codice. Lascia vuoto per utilizzare il job workspace stesso");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"Specifica facoltativamente il percorso relativo sotto <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> per scansionare. Lascia vuoto per utilizzare il job workspace stesso");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"Specifica facoltativamente i percorsi relativi sotto <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> per scansionare le vulnerabilità delle dipendenze. Più percorsi possono essere specificati e dovrebbero essere separati da spazio. Lascia vuoto per utilizzare il job workspace stesso");
		m.put("Optionally specify required reviewers for changes of specified branch", "Specifica facoltativamente i revisori richiesti per le modifiche del ramo specificato");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"Specifica facoltativamente la revisione da cui creare il ramo. Lascia vuoto per creare dal commit del build");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"Specifica facoltativamente una directory separata per archiviare gli artefatti di build. Una directory non assoluta è considerata relativa alla directory del sito");
		m.put("Optionally specify separate directory to store git lfs files. Non-absolute directory is considered to be relative to site directory", 
			"Specifica facoltativamente una directory separata per archiviare i file git lfs. Una directory non assoluta è considerata relativa alla directory del sito");
		m.put("Optionally specify separate directory to store package files. Non-absolute directory is considered to be relative to site directory", 
			"Specifica facoltativamente una directory separata per archiviare i file dei pacchetti. Una directory non assoluta è considerata relativa alla directory del sito");
		m.put("Optionally specify services required by this job. <b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors (server docker executor, remote docker executor, or kubernetes executor)", 
			"Specifica facoltativamente i servizi richiesti da questo job. <b class='text-warning'>NOTA:</b> I servizi sono supportati solo dagli esecutori consapevoli di docker (esecutore docker del server, esecutore docker remoto o esecutore Kubernetes)");
		m.put("Optionally specify space-separated branches applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Specifica facoltativamente i rami separati da spazio applicabili per questa transizione. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza dei modelli di percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti");
		m.put("Optionally specify space-separated branches applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for default branch", 
			"Specifica facoltativamente i rami separati da spazio applicabili per questo trigger. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza dei modelli di percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per il ramo predefinito");
		m.put("Optionally specify space-separated branches to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Specifica facoltativamente i rami separati da spazio da controllare. Usa '**' o '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza dei modelli di percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti i rami");
		m.put("Optionally specify space-separated commit messages applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Specifica facoltativamente messaggi di commit separati da spazi applicabili per questa transizione. Usa '*' o '?' per corrispondenza jolly. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti");
		m.put("Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all files", 
			"Specifica facoltativamente i file separati da spazio da controllare. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza dei modelli di percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti i file");
		m.put("Optionally specify space-separated jobs applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Specifica facoltativamente i job separati da spazio applicabili per questa transizione. Usa '*' o '?' per la corrispondenza dei modelli. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti");
		m.put("Optionally specify space-separated projects applicable for this trigger. This is useful for instance when you want to prevent the job from being triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Specifica facoltativamente i progetti separati da spazio applicabili per questo trigger. Questo è utile, ad esempio, quando vuoi impedire che il job venga attivato nei progetti forkati. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza dei modelli di percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti i progetti");
		m.put("Optionally specify space-separated projects to search in. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to search in all projects with code read permission", 
			"Specifica facoltativamente i progetti separati da spazio in cui cercare. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza dei modelli di percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per cercare in tutti i progetti con permesso di lettura del codice");
		m.put("Optionally specify space-separated reports. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Specifica facoltativamente report separati da spazio. Usa '*' o '?' per corrispondenza con caratteri jolly. Prefissa con '-' per escludere");
		m.put("Optionally specify space-separated service images applicable for this locator. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Specifica facoltativamente immagini di servizio separati da spazio applicabili per questo localizzatore. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>corrispondenza con caratteri jolly nel percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti");
		m.put("Optionally specify space-separated service names applicable for this locator. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Specifica facoltativamente nomi di servizio separati da spazio applicabili per questo localizzatore. Usa '*' o '?' per corrispondenza con caratteri jolly. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti");
		m.put("Optionally specify space-separated tags to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all tags", 
			"Specifica facoltativamente tag separati da spazio da controllare. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>corrispondenza con caratteri jolly nel percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti i tag");
		m.put("Optionally specify space-separated target branches of the pull requests to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Specifica facoltativamente rami di destinazione separati da spazio delle pull request da controllare. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>corrispondenza con caratteri jolly nel percorso</a>. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti i rami");
		m.put("Optionally specify the OpenID claim to retrieve groups of authenticated user. Depending on the provider, you may need to request additional scopes above to make this claim available", 
			"Specifica facoltativamente la richiesta OpenID per recuperare i gruppi dell'utente autenticato. A seconda del provider, potrebbe essere necessario richiedere ambiti aggiuntivi sopra per rendere disponibile questa richiesta");
		m.put("Optionally specify the maximum value allowed.", "Specifica facoltativamente il valore massimo consentito.");
		m.put("Optionally specify the minimum value allowed.", "Specifica facoltativamente il valore minimo consentito.");
		m.put("Optionally specify the project to publish site files to. Leave empty to publish to current project", 
			"Specifica facoltativamente il progetto a cui pubblicare i file del sito. Lascia vuoto per pubblicare nel progetto corrente");
		m.put("Optionally specify uid:gid to run container as. <b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or using user namespace remapping", 
			"Specifica facoltativamente uid:gid per eseguire il container. <b class='text-warning'>Nota:</b> Questa impostazione dovrebbe essere lasciata vuota se il runtime del container è rootless o utilizza il remapping dello spazio utente");
		m.put("Optionally specify user name to access remote repository", "Specifica facoltativamente il nome utente per accedere al repository remoto");
		m.put("Optionally specify valid scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope", 
			"Specifica facoltativamente ambiti validi per i commit convenzionali (premi INVIO per aggiungere un valore). Lascia vuoto per consentire ambiti arbitrari");
		m.put("Optionally specify valid types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type", 
			"Specifica facoltativamente tipi validi per i commit convenzionali (premi INVIO per aggiungere un valore). Lascia vuoto per consentire tipi arbitrari");
		m.put("Optionally specify value of git config <code>pack.packSizeLimit</code> for the repository", 
			"Specifica facoltativamente il valore di git config <code>pack.packSizeLimit</code> per il repository");
		m.put("Optionally specify value of git config <code>pack.threads</code> for the repository", 
			"Specifica facoltativamente il valore di git config <code>pack.threads</code> per il repository");
		m.put("Optionally specify value of git config <code>pack.window</code> for the repository", 
			"Specifica facoltativamente il valore di git config <code>pack.window</code> per il repository");
		m.put("Optionally specify value of git config <code>pack.windowMemory</code> for the repository", 
			"Specifica facoltativamente il valore di git config <code>pack.windowMemory</code> per il repository");
		m.put("Optionally specify where to run service pods specified in job. The first matching locator will be used. If no any locators are found, node selector of the executor will be used", 
			"Specifica facoltativamente dove eseguire i pod di servizio specificati nel job. Verrà utilizzato il primo localizzatore corrispondente. Se non vengono trovati localizzatori, verrà utilizzato il selettore di nodi dell'esecutore");
		m.put("Optionally specify working directory of the container. Leave empty to use default working directory of the container", 
			"Specifica facoltativamente la directory di lavoro del container. Lascia vuoto per utilizzare la directory di lavoro predefinita del container");
		m.put("Options", "Opzioni");
		m.put("Or manually enter the secret key below in your authenticator app", "Oppure inserisci manualmente la chiave segreta qui sotto nella tua app di autenticazione");
		m.put("Order By", "Ordina Per");
		m.put("Order More User Months", "Ordina Più Mesi Utente");
		m.put("Order Subscription", "Ordina Abbonamento");
		m.put("Ordered List", "Elenco Ordinato");
		m.put("Ordered list", "Elenco ordinato");
		m.put("Osv License Scanner", "Scanner di Licenze Osv");
		m.put("Osv Vulnerability Scanner", "Scanner di Vulnerabilità Osv");
		m.put("Other", "Altro");
		m.put("Outline", "Schema");
		m.put("Outline Search", "Ricerca Schema");
		m.put("Output", "Output");
		m.put("Overall", "Complessivo");
		m.put("Overall Estimated Time:", "Tempo Stimato Complessivo:");
		m.put("Overall Spent Time:", "Tempo Trascorso Complessivo:");
		m.put("Overview", "Panoramica");
		m.put("Own:", "Proprio:");
		m.put("Ownered By", "Di Proprietà Di");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "La chiave privata PEM inizia con '-----BEGIN RSA PRIVATE KEY-----'");
		m.put("PENDING", "IN ATTESA");
		m.put("PMD Report", "Report PMD");
		m.put("Pack", "Pacchetto");
		m.put("Pack Notification", "Notifica Pacchetto");
		m.put("Pack Size Limit", "Limite Dimensione Pacchetto");
		m.put("Pack Type", "Tipo di Pacchetto");
		m.put("Package", "Pacchetto");
		m.put("Package Management", "Gestione Pacchetti");
		m.put("Package Notification", "Notifica Pacchetto");
		m.put("Package Notification Template", "Template Notifica Pacchetto");
		m.put("Package Privilege", "Privilegio Pacchetto");
		m.put("Package Storage", "Storage Pacchetto");
		m.put("Package list", "Elenco Pacchetti");
		m.put("Package {0} deleted", "Pacchetto {0} eliminato");
		m.put("Packages", "Pacchetti");
		m.put("Page Not Found", "Pagina Non Trovata");
		m.put("Page is in error, reload to recover", "La pagina è in errore, ricarica per recuperare");
		m.put("Param Instance", "Istanza Parametro");
		m.put("Param Instances", "Istanze Parametro");
		m.put("Param Map", "Mappa Parametro");
		m.put("Param Matrix", "Matrice Parametro");
		m.put("Param Name", "Nome Parametro");
		m.put("Param Spec", "Specifica Parametro");
		m.put("Param Spec Bean", "Bean Specifica Parametro");
		m.put("Parameter", "Parametro");
		m.put("Parameter Specs", "Specifiche Parametro");
		m.put("Params", "Parametri");
		m.put("Params & Triggers", "Parametri & Trigger");
		m.put("Params to Display", "Parametri da Visualizzare");
		m.put("Parent Bean", "Bean Genitore");
		m.put("Parent OneDev Project", "Progetto OneDev Genitore");
		m.put("Parent Project", "Progetto Genitore");
		m.put("Parent project not found", "Progetto genitore non trovato");
		m.put("Parents", "Genitori");
		m.put("Partially covered", "Parzialmente coperto");
		m.put("Partially covered by some tests", "Parzialmente coperto da alcuni test");
		m.put("Passcode", "Codice di Accesso");
		m.put("Passed", "Superato");
		m.put("Password", "Password");
		m.put("Password Authenticator", "Autenticatore di Password");
		m.put("Password Edit Bean", "Bean Modifica Password");
		m.put("Password Must Contain Digit", "La password deve contenere una cifra");
		m.put("Password Must Contain Lowercase", "La password deve contenere una lettera minuscola");
		m.put("Password Must Contain Special Character", "La password deve contenere un carattere speciale");
		m.put("Password Must Contain Uppercase", "La password deve contenere una lettera maiuscola");
		m.put("Password Policy", "Politica della Password");
		m.put("Password Reset", "Reset Password");
		m.put("Password Reset Bean", "Bean Reset Password");
		m.put("Password Reset Template", "Template Reset Password");
		m.put("Password Secret", "Password Segreta");
		m.put("Password and its confirmation should be identical.", "La password e la sua conferma devono essere identiche.");
		m.put("Password changed. Please login with your new password", "Password cambiata. Effettua il login con la tua nuova password");
		m.put("Password has been changed", "La password è stata cambiata");
		m.put("Password has been removed", "La password è stata rimossa");
		m.put("Password has been set", "La password è stata impostata");
		m.put("Password of the user", "Password dell'utente");
		m.put("Password or Access Token for Remote Repository", "Password o Token di Accesso per Repository Remoto");
		m.put("Password reset request has been sent", "La richiesta di reset della password è stata inviata");
		m.put("Password reset url is invalid or obsolete", "L'url per reimpostare la password non è valido o è obsoleto");
		m.put("PasswordMinimum Length", "Lunghezza Minima della Password");
		m.put("Paste subscription key here", "Incolla qui la chiave di abbonamento");
		m.put("Path containing spaces or starting with dash needs to be quoted", "Il percorso contenente spazi o che inizia con un trattino deve essere racchiuso tra virgolette");
		m.put("Path placeholder", "Segnaposto Percorso");
		m.put("Path to kubectl", "Percorso a kubectl");
		m.put("Paths", "Percorsi");
		m.put("Pattern", "Modello");
		m.put("Pause", "Pausa");
		m.put("Pause All Queried Agents", "Metti in Pausa Tutti gli Agenti Interrogati");
		m.put("Pause Selected Agents", "Metti in Pausa Agenti Selezionati");
		m.put("Paused", "In Pausa");
		m.put("Paused all queried agents", "Messi in pausa tutti gli agenti interrogati");
		m.put("Paused selected agents", "Messi in pausa gli agenti selezionati");
		m.put("Pem Private Key", "Chiave Privata Pem");
		m.put("Pending", "In Attesa");
		m.put("Performance", "Prestazioni");
		m.put("Performance Setting", "Impostazione delle prestazioni");
		m.put("Performance Settings", "Impostazioni delle prestazioni");
		m.put("Performance settings have been saved", "Le impostazioni delle prestazioni sono state salvate");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\"", 
			"Esecuzione di una query fuzzy. Racchiudere il testo di ricerca con '~' per aggiungere più condizioni, ad esempio: ~testo da cercare~ e \"Stato\" è \"Aperto\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"", 
			"Esecuzione di una query fuzzy. Racchiudere il testo di ricerca con '~' per aggiungere più condizioni, ad esempio: ~testo da cercare~ e \"Tipo\" è \"NPM\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online", 
			"Esecuzione di una query fuzzy. Racchiudere il testo di ricerca con '~' per aggiungere più condizioni, ad esempio: ~testo da cercare~ e online");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"Esecuzione di una query fuzzy. Racchiudere il testo di ricerca con '~' per aggiungere più condizioni, ad esempio: ~testo da cercare~ e aperto");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and owned by me", 
			"Esecuzione di una query fuzzy. Racchiudere il testo di ricerca con '~' per aggiungere più condizioni, ad esempio: ~testo da cercare~ e di mia proprietà");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved", 
			"Esecuzione di una query fuzzy. Racchiudere il testo di ricerca con '~' per aggiungere più condizioni, ad esempio: ~testo da cercare~ e non risolto");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"Esecuzione di una query fuzzy. Racchiudere il testo di ricerca con '~' per aggiungere più condizioni, ad esempio: ~testo da cercare~ autore(robin)");
		m.put("Permanent link", "Collegamento permanente");
		m.put("Permanent link of this selection", "Collegamento permanente di questa selezione");
		m.put("Permission denied", "Permesso negato");
		m.put("Permission will be checked upon actual operation", "Il permesso sarà verificato durante l'operazione effettiva");
		m.put("Physical memory in mega bytes", "Memoria fisica in megabyte");
		m.put("Pick Existing", "Seleziona esistente");
		m.put("Pin this issue", "Fissa questa issue");
		m.put("Pipeline", "Pipeline");
		m.put("Placeholder", "Segnaposto");
		m.put("Plain text expected", "Testo semplice previsto");
		m.put("Platform", "Piattaforma");
		m.put("Platforms", "Piattaforme");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"Si prega di <a wicket:id=\"download\" class=\"font-weight-bolder\">scaricare</a> i codici di recupero qui sotto e di mantenerli segreti. Questi codici possono essere utilizzati per fornire accesso temporaneo al tuo account nel caso in cui non sia possibile accedere all'applicazione di autenticazione. Non saranno <b>MAI</b> visualizzati di nuovo");
		m.put("Please Confirm", "Si prega di confermare");
		m.put("Please Note", "Si prega di notare");
		m.put("Please check your email for password reset instructions", "Controlla la tua email per le istruzioni di reimpostazione della password");
		m.put("Please choose revision to create branch from", "Si prega di scegliere la revisione da cui creare il branch");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "Si prega di configurare prima <a wicket:id=\"mailSetting\">l'impostazione della mail</a>");
		m.put("Please confirm", "Si prega di confermare");
		m.put("Please confirm the password.", "Si prega di confermare la password.");
		m.put("Please follow <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">this instruction</a> to resolve the conflicts", 
			"Si prega di seguire <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">questa istruzione</a> per risolvere i conflitti");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"Si prega di inserire uno dei codici di recupero salvati quando è stata abilitata l'autenticazione a due fattori");
		m.put("Please login to perform this operation", "Si prega di effettuare il login per eseguire questa operazione");
		m.put("Please login to perform this query", "Si prega di effettuare il login per eseguire questa query");
		m.put("Please resolve undefined field values below", "Si prega di risolvere i valori dei campi non definiti qui sotto");
		m.put("Please resolve undefined fields below", "Si prega di risolvere i campi non definiti qui sotto");
		m.put("Please resolve undefined states below. Note that if you select to delete an undefined state, all issues with that state will be deleted", 
			"Si prega di risolvere gli stati non definiti qui sotto. Nota che se scegli di eliminare uno stato non definito, tutte le issue con quello stato saranno eliminate");
		m.put("Please select agents to pause", "Si prega di selezionare gli agenti da mettere in pausa");
		m.put("Please select agents to remove", "Si prega di selezionare gli agenti da rimuovere");
		m.put("Please select agents to restart", "Si prega di selezionare gli agenti da riavviare");
		m.put("Please select agents to resume", "Si prega di selezionare gli agenti da riprendere");
		m.put("Please select branches to create pull request", "Si prega di selezionare i branch per creare una pull request");
		m.put("Please select builds to cancel", "Si prega di selezionare i build da annullare");
		m.put("Please select builds to delete", "Si prega di selezionare i build da eliminare");
		m.put("Please select builds to re-run", "Si prega di selezionare i build da rieseguire");
		m.put("Please select comments to delete", "Si prega di selezionare i commenti da eliminare");
		m.put("Please select comments to set resolved", "Si prega di selezionare i commenti da impostare come risolti");
		m.put("Please select comments to set unresolved", "Si prega di selezionare i commenti da impostare come non risolti");
		m.put("Please select different branches", "Si prega di selezionare branch diversi");
		m.put("Please select fields to update", "Si prega di selezionare i campi da aggiornare");
		m.put("Please select groups to remove from", "Seleziona i gruppi da cui rimuovere");
		m.put("Please select issues to copy", "Si prega di selezionare le issue da copiare");
		m.put("Please select issues to delete", "Si prega di selezionare le issue da eliminare");
		m.put("Please select issues to edit", "Si prega di selezionare le issue da modificare");
		m.put("Please select issues to move", "Si prega di selezionare le issue da spostare");
		m.put("Please select issues to sync estimated/spent time", "Si prega di selezionare le issue per sincronizzare il tempo stimato/speso");
		m.put("Please select packages to delete", "Si prega di selezionare i pacchetti da eliminare");
		m.put("Please select projects to delete", "Si prega di selezionare i progetti da eliminare");
		m.put("Please select projects to modify", "Si prega di selezionare i progetti da modificare");
		m.put("Please select projects to move", "Si prega di selezionare i progetti da spostare");
		m.put("Please select pull requests to delete", "Si prega di selezionare le pull request da eliminare");
		m.put("Please select pull requests to discard", "Si prega di selezionare le pull request da scartare");
		m.put("Please select pull requests to watch/unwatch", "Si prega di selezionare le pull request da osservare/non osservare");
		m.put("Please select query watches to delete", "Si prega di selezionare le query osservate da eliminare");
		m.put("Please select revision to create tag from", "Si prega di selezionare la revisione da cui creare il tag");
		m.put("Please select revisions to compare", "Si prega di selezionare le revisioni da confrontare");
		m.put("Please select users to convert to service accounts", "Seleziona gli utenti da convertire in account di servizio");
		m.put("Please select users to disable", "Si prega di selezionare gli utenti da disabilitare");
		m.put("Please select users to enable", "Si prega di selezionare gli utenti da abilitare");
		m.put("Please select users to remove from group", "Seleziona gli utenti da rimuovere dal gruppo");
		m.put("Please specify file name above before editing content", "Si prega di specificare il nome del file sopra prima di modificare il contenuto");
		m.put("Please switch to packages page of a particular project for the instructions", "Si prega di passare alla pagina dei pacchetti di un progetto specifico per le istruzioni");
		m.put("Please wait...", "Attendere prego...");
		m.put("Please waiting...", "Attendere prego...");
		m.put("Plugin metadata not found", "Metadati del plugin non trovati");
		m.put("Poll Interval", "Intervallo di polling");
		m.put("Populate Tag Mappings", "Popola le mappature dei tag");
		m.put("Port", "Porta");
		m.put("Post", "Posta");
		m.put("Post Build Action", "Azione post-build");
		m.put("Post Build Action Bean", "Bean di azione post-build");
		m.put("Post Build Actions", "Azioni post-build");
		m.put("Post Url", "URL del post");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "Modello di prefisso");
		m.put("Prefix the title with <code>WIP</code> or <code>[WIP]</code> to mark the pull request as work in progress", 
			"Prefissa il titolo con <code>WIP</code> o <code>[WIP]</code> per contrassegnare la pull request come lavoro in corso");
		m.put("Prepend", "Prependi");
		m.put("Preserve Days", "Preserva giorni");
		m.put("Preset Commit Message", "Messaggio di commit preimpostato");
		m.put("Preset commit message updated", "Messaggio di commit preimpostato aggiornato");
		m.put("Press 'y' to get permalink", "Premi 'y' per ottenere il permalink");
		m.put("Prev", "Prec");
		m.put("Prevent Creation", "Impedisci la creazione");
		m.put("Prevent Deletion", "Impedisci l'eliminazione");
		m.put("Prevent Forced Push", "Impedisci il push forzato");
		m.put("Prevent Update", "Impedisci l'aggiornamento");
		m.put("Preview", "Anteprima");
		m.put("Previous", "Precedente");
		m.put("Previous Value", "Valore precedente");
		m.put("Previous commit", "Commit precedente");
		m.put("Previous {0}", "Precedente {0}");
		m.put("Primary", "Primario");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "Indirizzo email <a wicket:id=\"noPrimaryAddressLink\">primario</a> non specificato");
		m.put("Primary Email", "Email primaria");
		m.put("Primary email address not specified", "Indirizzo email principale non specificato");
		m.put("Primary email address of your account is not specified yet", "L'indirizzo email principale del tuo account non è ancora specificato");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"L'indirizzo email principale verrà utilizzato per ricevere notifiche, mostrare il gravatar (se abilitato), ecc.");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"L'indirizzo email principale o alias dell'account sopra indicato verrà utilizzato come indirizzo del mittente per varie notifiche email. L'utente può anche rispondere a questo indirizzo per pubblicare commenti su problemi o richieste di pull via email se l'opzione <code>Check Incoming Email</code> è abilitata qui sotto");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"Nome principale dell'account per accedere al server di posta di Office 365 per inviare/ricevere email. Assicurati che questo account <b>possegga</b> l'applicazione registrata indicata dall'ID applicazione sopra");
		m.put("Private Key Secret", "Chiave privata segreta");
		m.put("Private key regenerated and SSH server restarted", "Chiave privata rigenerata e server SSH riavviato");
		m.put("Privilege", "Privilegio");
		m.put("Privilege Settings", "Impostazioni dei privilegi");
		m.put("Product Version", "Versione del prodotto");
		m.put("Profile", "Profilo");
		m.put("Programming language", "Linguaggio di programmazione");
		m.put("Project", "Progetto");
		m.put("Project \"{0}\" deleted", "Progetto \"{0}\" eliminato");
		m.put("Project Authorization Bean", "Bean di autorizzazione del progetto");
		m.put("Project Authorizations Bean", "Bean di autorizzazioni del progetto");
		m.put("Project Build Setting", "Impostazione di build del progetto");
		m.put("Project Dependencies", "Dipendenze del progetto");
		m.put("Project Dependency", "Dipendenza del progetto");
		m.put("Project Id", "ID del progetto");
		m.put("Project Import Option", "Opzione di importazione del progetto");
		m.put("Project Issue Setting", "Impostazione dei problemi del progetto");
		m.put("Project Key", "Chiave del progetto");
		m.put("Project Management", "Gestione del progetto");
		m.put("Project Pack Setting", "Impostazione del pacchetto del progetto");
		m.put("Project Path", "Percorso del progetto");
		m.put("Project Pull Request Setting", "Impostazione delle richieste di pull del progetto");
		m.put("Project Replicas", "Repliche del progetto");
		m.put("Project authorizations updated", "Autorizzazioni del progetto aggiornate");
		m.put("Project does not have any code yet", "Il progetto non ha ancora alcun codice");
		m.put("Project forked", "Progetto biforcato");
		m.put("Project id", "ID del progetto");
		m.put("Project list", "Elenco dei progetti");
		m.put("Project manage privilege required to delete \"{0}\"", "Privilegio di gestione del progetto richiesto per eliminare \"{0}\"");
		m.put("Project manage privilege required to modify \"{0}\"", "Privilegio di gestione del progetto richiesto per modificare \"{0}\"");
		m.put("Project manage privilege required to move \"{0}\"", "Privilegio di gestione del progetto richiesto per spostare \"{0}\"");
		m.put("Project name", "Nome del progetto");
		m.put("Project not specified yet", "Progetto non ancora specificato");
		m.put("Project or revision not specified yet", "Progetto o revisione non ancora specificati");
		m.put("Project overview", "Panoramica del progetto");
		m.put("Project path", "Percorso del progetto");
		m.put("Projects", "Progetti");
		m.put("Projects Bean", "Bean dei progetti");
		m.put("Projects deleted", "Progetti eliminati");
		m.put("Projects modified", "Progetti modificati");
		m.put("Projects moved", "Progetti spostati");
		m.put("Projects need to be redistributed when cluster members are added/removed. OneDev does not do this automatically as this is resource intensive, and you may only want to do it after cluster is finalized and stable.", 
			"I progetti devono essere ridistribuiti quando i membri del cluster vengono aggiunti/rimossi. OneDev non lo fa automaticamente poiché è intensivo in termini di risorse, e potresti volerlo fare solo dopo che il cluster è stato finalizzato e stabilizzato.");
		m.put("Promotions", "Promozioni");
		m.put("Prompt Fields", "Campi di prompt");
		m.put("Properties", "Proprietà");
		m.put("Provide server id (guild id) to restrict access only to server members", "Fornire l'ID del server (ID della gilda) per limitare l'accesso solo ai membri del server");
		m.put("Proxy", "Proxy");
		m.put("Prune Builder Cache", "Cache del builder potata");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"Potare la cache delle immagini del builder docker buildx. Questo passaggio chiama il comando docker builder prune per rimuovere la cache del builder buildx specificato nell'executor docker del server o nell'executor docker remoto");
		m.put("Public", "Pubblico");
		m.put("Public Key", "Chiave pubblica");
		m.put("Public Roles", "Ruoli Pubblici");
		m.put("Publish", "Pubblica");
		m.put("Publish Coverage Report Step", "Passaggio di pubblicazione del rapporto di copertura");
		m.put("Publish Problem Report Step", "Passaggio di pubblicazione del rapporto dei problemi");
		m.put("Publish Report Step", "Passaggio di pubblicazione del rapporto");
		m.put("Publish Unit Test Report Step", "Passaggio di pubblicazione del rapporto dei test unitari");
		m.put("Published After", "Pubblicato dopo");
		m.put("Published At", "Pubblicato a");
		m.put("Published Before", "Pubblicato prima");
		m.put("Published By", "Pubblicato da");
		m.put("Published By Project", "Pubblicato dal progetto");
		m.put("Published By User", "Pubblicato dall'utente");
		m.put("Published File", "File pubblicato");
		m.put("Pull Command", "Comando di pull");
		m.put("Pull Image", "Immagine di pull");
		m.put("Pull Request", "Richiesta di pull");
		m.put("Pull Request Branches", "Branch delle richieste di pull");
		m.put("Pull Request Description", "Descrizione della richiesta di pull");
		m.put("Pull Request Filter", "Filtro delle richieste di pull");
		m.put("Pull Request Management", "Gestione delle richieste di pull");
		m.put("Pull Request Markdown Report", "Rapporto markdown della richiesta di pull");
		m.put("Pull Request Notification", "Notifica della richiesta di pull");
		m.put("Pull Request Notification Template", "Template di notifica della richiesta di pull");
		m.put("Pull Request Notification Unsubscribed", "Notifica della richiesta di pull annullata");
		m.put("Pull Request Notification Unsubscribed Template", "Template di notifica della richiesta di pull annullata");
		m.put("Pull Request Settings", "Impostazioni delle richieste di pull");
		m.put("Pull Request Statistics", "Statistiche delle richieste di pull");
		m.put("Pull Request Title", "Titolo della richiesta di pull");
		m.put("Pull Requests", "Richieste di pull");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Scarica l'immagine docker come layout OCI tramite crane. Questo passaggio deve essere eseguito dall'executor docker del server, dall'executor docker remoto o dall'executor Kubernetes");
		m.put("Pull from Remote", "Pull da remoto");
		m.put("Pull request", "Richiesta di pull");
		m.put("Pull request #{0} already closed", "Richiesta di pull #{0} già chiusa");
		m.put("Pull request #{0} deleted", "Richiesta di pull #{0} eliminata");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"Permesso amministrativo per le richieste di pull all'interno di un progetto, incluse operazioni batch su più richieste di pull");
		m.put("Pull request already closed", "Richiesta di pull già chiusa");
		m.put("Pull request already opened", "Richiesta di pull già aperta");
		m.put("Pull request and code review", "Richiesta di pull e revisione del codice");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not finished yet", 
			"La richiesta di pull non può essere unita ora poiché <a class=\"more-info d-inline link-primary\">alcune build richieste</a> non sono ancora terminate");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not successful", 
			"La richiesta di pull non può essere unita ora poiché <a class=\"more-info d-inline link-primary\">alcune build richieste</a> non hanno avuto successo");
		m.put("Pull request can not be merged now as it is <a class=\"more-info d-inline link-primary\">pending review</a>", 
			"La richiesta di pull non può essere unita ora poiché è <a class=\"more-info d-inline link-primary\">in attesa di revisione</a>");
		m.put("Pull request can not be merged now as it was <a class=\"more-info d-inline link-primary\">requested for changes</a>", 
			"La richiesta di pull non può essere unita ora poiché è stata <a class=\"more-info d-inline link-primary\">richiesta una modifica</a>");
		m.put("Pull request can not be merged now as valid signature is required for head commit", 
			"La richiesta di pull non può essere unita ora poiché è richiesta una firma valida per il commit principale");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "La richiesta di pull può essere unita solo dopo aver ottenuto l'approvazione di tutti i revisori");
		m.put("Pull request can only be merged by users with code write permission", "La richiesta di pull può essere unita solo dagli utenti con permesso di scrittura del codice");
		m.put("Pull request discard", "Richiesta di pull scartata");
		m.put("Pull request duration statistics", "Statistiche sulla durata delle pull request");
		m.put("Pull request frequency statistics", "Statistiche sulla frequenza delle pull request");
		m.put("Pull request is discarded", "La pull request è stata scartata");
		m.put("Pull request is in error: {0}", "La pull request è in errore: {0}");
		m.put("Pull request is merged", "La pull request è stata unita");
		m.put("Pull request is opened", "La pull request è stata aperta");
		m.put("Pull request is still a work in progress", "La pull request è ancora un lavoro in corso");
		m.put("Pull request is work in progress", "La pull request è un lavoro in corso");
		m.put("Pull request list", "Elenco delle pull request");
		m.put("Pull request merge", "Unione della pull request");
		m.put("Pull request not exist or access denied", "La pull request non esiste o l'accesso è negato");
		m.put("Pull request not merged", "La pull request non è stata unita");
		m.put("Pull request number", "Numero della pull request");
		m.put("Pull request open or update", "Apri o aggiorna la pull request");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"La query di osservazione delle pull request influisce solo sulle nuove pull request. Per gestire lo stato di osservazione delle pull request esistenti in batch, filtra le pull request per stato di osservazione nella pagina delle pull request e poi prendi l'azione appropriata");
		m.put("Pull request settings updated", "Impostazioni della pull request aggiornate");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Le statistiche delle pull request sono una funzionalità aziendale. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("Pull request synchronization submitted", "Sincronizzazione della pull request inviata");
		m.put("Pull request will be merged automatically when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"La pull request sarà unita automaticamente quando pronta. Questa opzione sarà disabilitata aggiungendo nuovi commit, cambiando la strategia di unione o cambiando il branch di destinazione");
		m.put("Pull request will be merged automatically with a preset <a wicket:id=\"commitMessage\">commit message</a> when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"La pull request sarà unita automaticamente con un <a wicket:id=\"commitMessage\">messaggio di commit</a> preimpostato quando pronta. Questa opzione sarà disabilitata aggiungendo nuovi commit, cambiando la strategia di unione o cambiando il branch di destinazione");
		m.put("Push Image", "Push Immagine");
		m.put("Push chart to the repository", "Push del grafico nel repository");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Push dell'immagine docker dal layout OCI tramite crane. Questo passaggio deve essere eseguito dall'executor docker del server, dall'executor docker remoto o dall'executor Kubernetes");
		m.put("Push to Remote", "Push su remoto");
		m.put("Push to container registry", "Push nel registro dei container");
		m.put("PyPI(s)", "PyPI(s)");
		m.put("Pylint Report", "Report Pylint");
		m.put("Queries", "Query");
		m.put("Query", "Query");
		m.put("Query Parameters", "Parametri di Query");
		m.put("Query Watches", "Osservazioni delle query");
		m.put("Query commits", "Query dei commit");
		m.put("Query not submitted", "Query non inviata");
		m.put("Query param", "Parametro della query");
		m.put("Query/order agents", "Query/ordina agenti");
		m.put("Query/order builds", "Query/ordina build");
		m.put("Query/order comments", "Query/ordina commenti");
		m.put("Query/order issues", "Query/ordina problemi");
		m.put("Query/order packages", "Query/ordina pacchetti");
		m.put("Query/order projects", "Query/ordina progetti");
		m.put("Query/order pull requests", "Query/ordina pull request");
		m.put("Queueing Takes", "Tempo di accodamento");
		m.put("Quick Search", "Ricerca rapida");
		m.put("Quote", "Citazione");
		m.put("RESTful API", "API RESTful");
		m.put("RESTful API Help", "Guida API RESTful");
		m.put("Ran On Agent", "Eseguito sull'agente");
		m.put("Re-run All Queried Builds", "Riesegui tutte le build interrogate");
		m.put("Re-run Selected Builds", "Riesegui le build selezionate");
		m.put("Re-run request submitted", "Richiesta di riesecuzione inviata");
		m.put("Re-run this build", "Riesegui questa build");
		m.put("Read", "Leggi");
		m.put("Read body", "Leggi il corpo");
		m.put("Readiness Check Command", "Comando di verifica della prontezza");
		m.put("Really want to delete this code comment?", "Vuoi davvero eliminare questo commento al codice?");
		m.put("Rebase", "Rebase");
		m.put("Rebase Source Branch Commits", "Rebase dei commit del branch sorgente");
		m.put("Rebase all commits from source branch onto target branch", "Rebase di tutti i commit dal branch sorgente al branch di destinazione");
		m.put("Rebase source branch commits", "Rebase dei commit del branch sorgente");
		m.put("Rebuild manually", "Ricostruisci manualmente");
		m.put("Receive Posted Email", "Ricevi email inviata");
		m.put("Received test mail", "Email di test ricevuta");
		m.put("Receivers", "Destinatari");
		m.put("Recovery code", "Codice di recupero");
		m.put("Recursive", "Ricorsivo");
		m.put("Redundant", "Redundante");
		m.put("Ref", "Ref");
		m.put("Ref Name", "Nome del ref");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup", 
			"Consulta questo <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> per un esempio di configurazione");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> for an example setup", 
			"Consulta questo <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> per un esempio di configurazione");
		m.put("Reference", "Riferimento");
		m.put("Reference Build", "Build di riferimento");
		m.put("Reference Issue", "Problema di riferimento");
		m.put("Reference Pull Request", "Pull request di riferimento");
		m.put("Reference this {0} in markdown or commit message via below string.", "Riferisci questo {0} in markdown o nel messaggio di commit tramite la stringa sottostante.");
		m.put("Refresh", "Aggiorna");
		m.put("Refresh Token", "Token di aggiornamento");
		m.put("Refs", "Refs");
		m.put("Regenerate", "Rigenera");
		m.put("Regenerate Private Key", "Rigenera chiave privata");
		m.put("Regenerate this access token", "Rigenera questo token di accesso");
		m.put("Registry Login", "Login al registro");
		m.put("Registry Logins", "Login ai registri");
		m.put("Registry Url", "URL del registro");
		m.put("Regular Expression", "Espressione regolare");
		m.put("Remaining User Months", "Mesi utente rimanenti");
		m.put("Remaining User Months:", "Mesi utente rimanenti:");
		m.put("Remaining time", "Tempo rimanente");
		m.put("Remember Me", "Ricordami");
		m.put("Remote Docker Executor", "Executor Docker remoto");
		m.put("Remote Machine", "Macchina remota");
		m.put("Remote Shell Executor", "Executor shell remoto");
		m.put("Remote URL", "URL remoto");
		m.put("Remote Url", "URL remoto");
		m.put("Remove", "Rimuovi");
		m.put("Remove All Queried Agents", "Rimuovi tutti gli agenti interrogati");
		m.put("Remove All Queried Users from Group", "Rimuovi tutti gli utenti interrogati dal gruppo");
		m.put("Remove Fields", "Rimuovi campi");
		m.put("Remove From Current Iteration", "Rimuovi dall'iterazione corrente");
		m.put("Remove Selected Agents", "Rimuovi agenti selezionati");
		m.put("Remove Selected Users from Group", "Rimuovi gli utenti selezionati dal gruppo");
		m.put("Remove from All Queried Groups", "Rimuovi da tutti i gruppi interrogati");
		m.put("Remove from Selected Groups", "Rimuovi dai gruppi selezionati");
		m.put("Remove from batch", "Rimuovi dal batch");
		m.put("Remove issue from this iteration", "Rimuovi il problema da questa iterazione");
		m.put("Remove this assignee", "Rimuovi questo assegnatario");
		m.put("Remove this external participant from issue", "Rimuovi questo partecipante esterno dal problema");
		m.put("Remove this file", "Rimuovi questo file");
		m.put("Remove this image", "Rimuovi questa immagine");
		m.put("Remove this reviewer", "Rimuovi questo revisore");
		m.put("Removed all queried agents. Type <code>yes</code> below to confirm", "Rimossi tutti gli agenti richiesti. Digita <code>yes</code> qui sotto per confermare");
		m.put("Removed selected agents. Type <code>yes</code> below to confirm", "Rimossi gli agenti selezionati. Digita <code>yes</code> qui sotto per confermare");
		m.put("Rename {0}", "Rinomina {0}");
		m.put("Renew Subscription", "Rinnova Abbonamento");
		m.put("Renovate CLI Options", "Opzioni CLI di Renovate");
		m.put("Renovate JavaScript Config", "Configurazione JavaScript di Renovate");
		m.put("Reopen", "Riapri");
		m.put("Reopen this iteration", "Riapri questa iterazione");
		m.put("Reopened pull request \"{0}\" ({1})", "Richiesta di pull riaperta \"{0}\" ({1})");
		m.put("Replace With", "Sostituisci con");
		m.put("Replica Count", "Conteggio delle repliche");
		m.put("Replicas", "Repliche");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "Risposto al commento sul file \"{0}\" nel progetto \"{1}\"");
		m.put("Reply", "Rispondi");
		m.put("Report Name", "Nome del report");
		m.put("Report format changed. You may re-run this build to generate the report in new format", 
			"Formato del report modificato. Puoi rieseguire questa build per generare il report nel nuovo formato");
		m.put("Repository Sync", "Sincronizzazione del repository");
		m.put("Request Body", "Corpo della Richiesta");
		m.put("Request For Changes", "Richiesta di modifiche");
		m.put("Request Scopes", "Ambiti di richiesta");
		m.put("Request Trial Subscription", "Richiedi abbonamento di prova");
		m.put("Request review", "Richiedi revisione");
		m.put("Request to sync", "Richiedi sincronizzazione");
		m.put("Requested For changes", "Richiesto per modifiche");
		m.put("Requested changes to pull request \"{0}\" ({1})", "Richieste modifiche alla richiesta di pull \"{0}\" ({1})");
		m.put("Requested for changes", "Richiesto per modifiche");
		m.put("Requested to sync estimated/spent time", "Richiesto di sincronizzare il tempo stimato/speso");
		m.put("Require Autentication", "Richiedi autenticazione");
		m.put("Require Strict Pull Request Builds", "Richiedi build rigorose per le richieste di pull");
		m.put("Require Successful", "Richiedi successo");
		m.put("Required", "Richiesto");
		m.put("Required Builds", "Build richieste");
		m.put("Required Reviewers", "Revisori richiesti");
		m.put("Required Services", "Servizi richiesti");
		m.put("Resend Verification Email", "Reinvia email di verifica");
		m.put("Resend invitation", "Reinvia invito");
		m.put("Reset", "Reimposta");
		m.put("Resolution", "Risoluzione");
		m.put("Resolved", "Risolto");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "Commento risolto sul file \"{0}\" nel progetto \"{1}\"");
		m.put("Resource", "Risorsa");
		m.put("Resource Settings", "Impostazioni delle risorse");
		m.put("Resources", "Risorse");
		m.put("Response", "Risposta");
		m.put("Response Body", "Corpo della Risposta");
		m.put("Restart", "Riavvia");
		m.put("Restart All Queried Agents", "Riavvia tutti gli agenti richiesti");
		m.put("Restart Selected Agents", "Riavvia gli agenti selezionati");
		m.put("Restart command issued", "Comando di riavvio emesso");
		m.put("Restart command issued to all queried agents", "Comando di riavvio emesso a tutti gli agenti richiesti");
		m.put("Restart command issued to selected agents", "Comando di riavvio emesso agli agenti selezionati");
		m.put("Restore Source Branch", "Ripristina il branch sorgente");
		m.put("Restored source branch", "Branch sorgente ripristinato");
		m.put("Resubmitted manually", "Inviato manualmente");
		m.put("Resume", "Riprendi");
		m.put("Resume All Queried Agents", "Riprendi tutti gli agenti richiesti");
		m.put("Resume Selected Agents", "Riprendi gli agenti selezionati");
		m.put("Resumed all queried agents", "Ripresi tutti gli agenti richiesti");
		m.put("Resumed selected agents", "Ripresi gli agenti selezionati");
		m.put("Retried At", "Ritentato a");
		m.put("Retrieve Groups", "Recupera gruppi");
		m.put("Retrieve LFS Files", "Recupera file LFS");
		m.put("Retrieve Submodules", "Recupera sottomoduli");
		m.put("Retry Condition", "Condizione di ritentativo");
		m.put("Retry Delay", "Ritardo di ritentativo");
		m.put("Revert", "Ripristina");
		m.put("Reverted successfully", "Ripristinato con successo");
		m.put("Review required for deletion. Submit pull request instead", "Revisione richiesta per l'eliminazione. Invia una richiesta di pull invece");
		m.put("Review required for this change. Please submit pull request instead", "Revisione richiesta per questa modifica. Si prega di inviare una pull request invece.");
		m.put("Review required for this change. Submit pull request instead", "Revisione richiesta per questa modifica. Invia una richiesta di pull invece");
		m.put("Reviewers", "Revisori");
		m.put("Revision", "Revisione");
		m.put("Revision indexing in progress...", "Indicizzazione della revisione in corso...");
		m.put("Revision indexing in progress... (symbol navigation in revisions will be accurate after indexed)", 
			"Indicizzazione della revisione in corso... (la navigazione simbolica nelle revisioni sarà accurata dopo l'indicizzazione)");
		m.put("Right", "Destra");
		m.put("Role", "Ruolo");
		m.put("Role \"{0}\" deleted", "Ruolo \"{0}\" eliminato");
		m.put("Role \"{0}\" updated", "Ruolo \"{0}\" aggiornato");
		m.put("Role Management", "Gestione dei ruoli");
		m.put("Role created", "Ruolo creato");
		m.put("Roles", "Ruoli");
		m.put("Root Projects", "Progetti radice");
		m.put("Roslynator Report", "Report di Roslynator");
		m.put("RubyGems(s)", "RubyGems(s)");
		m.put("Ruff Report", "Report di Ruff");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "La regola si applicherà se l'utente che opera il tag soddisfa i criteri specificati qui");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"La regola si applicherà solo se l'utente che modifica il branch soddisfa i criteri specificati qui");
		m.put("Run As", "Esegui come");
		m.put("Run Buildx Image Tools", "Esegui strumenti immagine Buildx");
		m.put("Run Docker Container", "Esegui contenitore Docker");
		m.put("Run In Container", "Esegui nel contenitore");
		m.put("Run Integrity Check", "Esegui controllo di integrità");
		m.put("Run Job", "Esegui lavoro");
		m.put("Run Options", "Opzioni di esecuzione");
		m.put("Run below commands from within your git repository:", "Esegui i seguenti comandi all'interno del tuo repository git:");
		m.put("Run below commands to install this gem", "Esegui i seguenti comandi per installare questa gemma");
		m.put("Run below commands to install this package", "Esegui i seguenti comandi per installare questo pacchetto");
		m.put("Run below commands to use this chart", "Esegui i seguenti comandi per utilizzare questo chart");
		m.put("Run below commands to use this package", "Esegui i seguenti comandi per utilizzare questo pacchetto");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"Esegui il comando docker buildx imagetools con gli argomenti specificati. Questo passaggio può essere eseguito solo dall'esecutore docker del server o dall'esecutore docker remoto");
		m.put("Run job", "Esegui lavoro");
		m.put("Run job in another project", "Esegui il job in un altro progetto");
		m.put("Run on Bare Metal/Virtual Machine", "Esegui su Bare Metal/Macchina Virtuale");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"Esegui lo scanner osv per analizzare le licenze violate utilizzate da varie <a href='https://deps.dev/' target='_blank'>dipendenze</a>. Può essere eseguito solo da un esecutore consapevole di Docker.");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"Esegui lo scanner osv per analizzare le vulnerabilità in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>vari file di blocco</a>. Può essere eseguito solo da un esecutore consapevole di Docker.");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"Esegui il contenitore Docker specificato. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Workspace del lavoro</a> è montato nel contenitore e il suo percorso è posizionato nella variabile di ambiente <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Nota: </b> questo passaggio può essere eseguito solo dall'esecutore docker del server o dall'esecutore docker remoto");
		m.put("Run specified step template", "Esegui il modello di passaggio specificato");
		m.put("Run this job", "Esegui questo lavoro");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by docker aware executor.", 
			"Esegui lo scanner di immagini del container trivy per trovare problemi nell'immagine specificata. Per le vulnerabilità, controlla vari <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>file di distribuzione</a>. Può essere eseguito solo da un esecutore consapevole di docker.");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"Esegui lo scanner del filesystem trivy per analizzare vari <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>file di blocco</a>. Può essere eseguito solo da un executor compatibile con Docker ed è consigliato eseguirlo <span class='text-warning'>dopo che le dipendenze sono state risolte</span> (npm install o simili). Rispetto allo scanner OSV, la configurazione è un po' più dettagliata, ma può fornire risultati più accurati.");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"Esegui lo scanner rootfs trivy per analizzare vari <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>file di distribuzione</a>. Può essere eseguito solo da un executor compatibile con Docker ed è consigliato eseguirlo nell'area di staging del tuo progetto.");
		m.put("Run via Docker Container", "Esegui tramite container Docker.");
		m.put("Running", "In esecuzione.");
		m.put("Running Takes", "In esecuzione richiede.");
		m.put("SLOC on {0}", "SLOC su {0}.");
		m.put("SMTP Host", "Host SMTP.");
		m.put("SMTP Password", "Password SMTP.");
		m.put("SMTP User", "Utente SMTP.");
		m.put("SMTP/IMAP", "SMTP/IMAP.");
		m.put("SSH", "SSH.");
		m.put("SSH & GPG Keys", "Chiavi SSH & GPG.");
		m.put("SSH Clone URL", "URL di clonazione SSH.");
		m.put("SSH Keys", "Chiavi SSH.");
		m.put("SSH Root URL", "URL root SSH.");
		m.put("SSH Server Key", "Chiave server SSH.");
		m.put("SSH key deleted", "Chiave SSH eliminata.");
		m.put("SSH settings have been saved and SSH server restarted", "Le impostazioni SSH sono state salvate e il server SSH è stato riavviato.");
		m.put("SSL Setting", "Impostazione SSL.");
		m.put("SSO Accounts", "Account SSO");
		m.put("SSO Providers", "Provider SSO");
		m.put("SSO account deleted", "Account SSO eliminato");
		m.put("SSO provider \"{0}\" deleted", "Provider SSO \"{0}\" eliminato");
		m.put("SSO provider created", "Provider SSO creato");
		m.put("SSO provider updated", "Provider SSO aggiornato");
		m.put("SUCCESSFUL", "RIUSCITO.");
		m.put("Save", "Salva.");
		m.put("Save Query", "Salva query.");
		m.put("Save Query Bean", "Salva query bean.");
		m.put("Save Settings", "Salva impostazioni.");
		m.put("Save Settings & Redistribute Projects", "Salva impostazioni e ridistribuisci progetti.");
		m.put("Save Template", "Salva modello.");
		m.put("Save as Mine", "Salva come mio.");
		m.put("Saved Queries", "Query salvate.");
		m.put("Scan Path", "Percorso di scansione.");
		m.put("Scan Paths", "Percorsi di scansione.");
		m.put("Scan below QR code with your TOTP authenticators", "Scansiona il codice QR sottostante con i tuoi autenticatori TOTP.");
		m.put("Schedule Issues", "Problemi di pianificazione.");
		m.put("Script Name", "Nome script.");
		m.put("Scripting Value", "Valore script.");
		m.put("Search", "Cerca.");
		m.put("Search For", "Cerca per.");
		m.put("Search Groups Using Filter", "Cerca gruppi utilizzando il filtro.");
		m.put("Search branch", "Cerca branch.");
		m.put("Search files, symbols and texts", "Cerca file, simboli e testi.");
		m.put("Search for", "Cerca per.");
		m.put("Search inside current tree", "Cerca all'interno dell'albero corrente");
		m.put("Search is too general", "La ricerca è troppo generica.");
		m.put("Search job", "Cerca lavoro.");
		m.put("Search project", "Cerca progetto.");
		m.put("Secret", "Segreto.");
		m.put("Secret Config File", "File di configurazione segreto.");
		m.put("Secret Setting", "Impostazione segreta.");
		m.put("Security", "Sicurezza.");
		m.put("Security & Compliance", "Sicurezza & Conformità.");
		m.put("Security Setting", "Impostazione di sicurezza.");
		m.put("Security Settings", "Impostazioni di sicurezza.");
		m.put("Security settings have been updated", "Le impostazioni di sicurezza sono state aggiornate.");
		m.put("Select", "Seleziona.");
		m.put("Select Branch to Cherry Pick to", "Seleziona branch per effettuare il cherry-pick.");
		m.put("Select Branch to Revert on", "Seleziona branch per effettuare il revert.");
		m.put("Select Branch/Tag", "Seleziona branch/tag.");
		m.put("Select Existing", "Seleziona esistente.");
		m.put("Select Job", "Seleziona job");
		m.put("Select Project", "Seleziona progetto.");
		m.put("Select below...", "Seleziona sotto...");
		m.put("Select iteration to schedule issues into", "Seleziona iterazione per pianificare i problemi.");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"Seleziona organizzazione da cui importare. Lascia vuoto per importare dai repository sotto l'account corrente.");
		m.put("Select project and revision first", "Seleziona prima progetto e revisione.");
		m.put("Select project first", "Seleziona prima progetto.");
		m.put("Select project to import from", "Seleziona progetto da cui importare.");
		m.put("Select project to sync to. Leave empty to sync to current project", "Seleziona progetto con cui sincronizzare. Lascia vuoto per sincronizzare con il progetto corrente.");
		m.put("Select repository to import from", "Seleziona repository da cui importare.");
		m.put("Select users to send alert email upon events such as database auto-backup failure, cluster node unreachable etc", 
			"Seleziona utenti a cui inviare email di avviso per eventi come il fallimento del backup automatico del database, nodo cluster non raggiungibile, ecc.");
		m.put("Select workspace to import from", "Seleziona workspace da cui importare.");
		m.put("Send Notifications", "Invia notifiche.");
		m.put("Send Pull Request", "Invia pull request.");
		m.put("Send notification", "Invia notifica.");
		m.put("SendGrid", "SendGrid.");
		m.put("Sendgrid Webhook Setting", "Impostazione webhook SendGrid.");
		m.put("Sending invitation to \"{0}\"...", "Invio invito a \"{0}\"...");
		m.put("Sending test mail to {0}...", "Invio email di test a {0}...");
		m.put("Sequential Group", "Gruppo sequenziale.");
		m.put("Server", "Server.");
		m.put("Server Docker Executor", "Executor Docker del server.");
		m.put("Server Id", "ID server.");
		m.put("Server Information", "Informazioni sul server.");
		m.put("Server Log", "Log del server.");
		m.put("Server Setup", "Configurazione del server.");
		m.put("Server Shell Executor", "Executor shell del server.");
		m.put("Server URL", "URL del server.");
		m.put("Server fingerprint", "Fingerprint del server.");
		m.put("Server host", "Host del server.");
		m.put("Server is Starting...", "Il server si sta avviando...");
		m.put("Server url", "URL del server.");
		m.put("Service", "Servizio.");
		m.put("Service Account", "Account di servizio.");
		m.put("Service Desk", "Service Desk.");
		m.put("Service Desk Email Address", "Indirizzo email del Service Desk.");
		m.put("Service Desk Issue Open Failed", "Apertura problema del Service Desk fallita.");
		m.put("Service Desk Issue Open Failed Template", "Template di apertura problema del Service Desk fallita.");
		m.put("Service Desk Issue Opened", "Problema del Service Desk aperto.");
		m.put("Service Desk Issue Opened Template", "Template di apertura problema del Service Desk.");
		m.put("Service Desk Setting", "Impostazione del Service Desk.");
		m.put("Service Desk Setting Holder", "Titolare dell'impostazione del Service Desk.");
		m.put("Service Desk Settings", "Impostazioni del Service Desk.");
		m.put("Service Locator", "Service Locator.");
		m.put("Service Locators", "Service Locators.");
		m.put("Service account not allowed to login", "Account di servizio non autorizzato ad accedere");
		m.put("Service desk setting", "Impostazione del service desk");
		m.put("Service desk settings have been saved", "Le impostazioni del service desk sono state salvate");
		m.put("Services", "Servizi");
		m.put("Session Timeout", "Timeout della sessione");
		m.put("Set", "Imposta");
		m.put("Set All Queried As Root Projects", "Imposta tutti i progetti interrogati come progetti radice");
		m.put("Set All Queried Comments as Read", "Imposta tutti i commenti interrogati come letti");
		m.put("Set All Queried Comments as Resolved", "Imposta tutti i commenti interrogati come risolti");
		m.put("Set All Queried Comments as Unresolved", "Imposta tutti i commenti interrogati come non risolti");
		m.put("Set All Queried Issues as Read", "Imposta tutte le problematiche interrogate come lette");
		m.put("Set All Queried Pull Requests as Read", "Imposta tutte le pull request interrogate come lette");
		m.put("Set As Primary", "Imposta come principale");
		m.put("Set Build Description", "Imposta descrizione build");
		m.put("Set Build Version", "Imposta versione build");
		m.put("Set Resolved", "Imposta come risolto");
		m.put("Set Selected As Root Projects", "Imposta i progetti selezionati come progetti radice");
		m.put("Set Selected Comments as Resolved", "Imposta i commenti selezionati come risolti");
		m.put("Set Selected Comments as Unresolved", "Imposta i commenti selezionati come non risolti");
		m.put("Set Unresolved", "Imposta come non risolto");
		m.put("Set Up Cache", "Configura cache");
		m.put("Set Up Renovate Cache", "Configura cache di Renovate");
		m.put("Set Up Trivy Cache", "Configura cache di Trivy");
		m.put("Set Up Your Account", "Configura il Tuo Account");
		m.put("Set as Private", "Imposta come privato");
		m.put("Set as Public", "Imposta come pubblico");
		m.put("Set description", "Imposta descrizione");
		m.put("Set reviewed", "Imposta come revisionato");
		m.put("Set unreviewed", "Imposta come non revisionato");
		m.put("Set up Microsoft Teams notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url. ", 
			"Configura le impostazioni di notifica di Microsoft Teams. Le impostazioni saranno ereditate dai progetti figli e possono essere sovrascritte definendo impostazioni con lo stesso URL webhook.");
		m.put("Set up discord notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configura le impostazioni di notifica di Discord. Le impostazioni saranno ereditate dai progetti figli e possono essere sovrascritte definendo impostazioni con lo stesso URL webhook.");
		m.put("Set up job cache to speed up job execution. Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> on how to use job cache", 
			"Configura la cache dei job per velocizzare l'esecuzione dei job. Consulta <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>questo tutorial</a> per sapere come utilizzare la cache dei job.");
		m.put("Set up ntfy.sh notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configura le impostazioni di notifica di ntfy.sh. Le impostazioni saranno ereditate dai progetti figli e possono essere sovrascritte definendo impostazioni con lo stesso URL webhook.");
		m.put("Set up slack notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configura le impostazioni di notifica di Slack. Le impostazioni saranno ereditate dai progetti figli e possono essere sovrascritte definendo impostazioni con lo stesso URL webhook.");
		m.put("Set up two-factor authentication", "Configura l'autenticazione a due fattori");
		m.put("Setting", "Impostazione");
		m.put("Setting has been saved", "L'impostazione è stata salvata");
		m.put("Settings", "Impostazioni");
		m.put("Settings and permissions of parent project will be inherited by this project", "Le impostazioni e i permessi del progetto padre saranno ereditati da questo progetto");
		m.put("Settings saved", "Impostazioni salvate");
		m.put("Settings saved and project redistribution scheduled", "Impostazioni salvate e ridistribuzione del progetto pianificata");
		m.put("Settings updated", "Impostazioni aggiornate");
		m.put("Share dashboard", "Condividi dashboard");
		m.put("Share with Groups", "Condividi con gruppi");
		m.put("Share with Users", "Condividi con utenti");
		m.put("Shell", "Shell");
		m.put("Show Archived", "Mostra archiviati");
		m.put("Show Branch/Tag", "Mostra branch/tag");
		m.put("Show Build Status", "Mostra stato build");
		m.put("Show Closed", "Mostra chiusi");
		m.put("Show Code Stats", "Mostra statistiche del codice");
		m.put("Show Command", "Mostra comando");
		m.put("Show Condition", "Mostra condizione");
		m.put("Show Conditionally", "Mostra condizionalmente");
		m.put("Show Description", "Mostra descrizione");
		m.put("Show Duration", "Mostra durata");
		m.put("Show Emojis", "Mostra emoji");
		m.put("Show Error Detail", "Mostra Dettaglio Errore");
		m.put("Show Issue Status", "Mostra stato delle problematiche");
		m.put("Show Package Stats", "Mostra statistiche dei pacchetti");
		m.put("Show Pull Request Stats", "Mostra statistiche delle pull request");
		m.put("Show Saved Queries", "Mostra query salvate");
		m.put("Show States By", "Mostra stati per");
		m.put("Show Works Of", "Mostra lavori di");
		m.put("Show changes", "Mostra modifiche");
		m.put("Show commented code snippet", "Mostra frammento di codice commentato");
		m.put("Show commit of this parent", "Mostra commit di questo padre");
		m.put("Show emojis", "Mostra emoji");
		m.put("Show in build list", "Mostra nella lista delle build");
		m.put("Show issues in list", "Mostra problematiche nella lista");
		m.put("Show issues not scheduled into current iteration", "Mostra problematiche non pianificate nell'iterazione corrente");
		m.put("Show matching agents", "Mostra agenti corrispondenti");
		m.put("Show more", "Mostra di più");
		m.put("Show more lines", "Mostra più righe");
		m.put("Show next match", "Mostra corrispondenza successiva");
		m.put("Show previous match", "Mostra corrispondenza precedente");
		m.put("Show test cases of this test suite", "Mostra casi di test di questa suite di test");
		m.put("Show total estimated/spent time", "Mostra tempo stimato/speso totale");
		m.put("Showing first {0} files as there are too many", "Mostrando i primi {0} file poiché ce ne sono troppi");
		m.put("Sign In", "Accedi");
		m.put("Sign In To", "Accedi a");
		m.put("Sign Out", "Esci");
		m.put("Sign Up", "Registrati");
		m.put("Sign Up Bean", "Registrati Bean");
		m.put("Sign Up!", "Registrati!");
		m.put("Sign in", "Accedi");
		m.put("Signature required for this change, but no signing key is specified", "Firma richiesta per questa modifica, ma non è specificata alcuna chiave di firma");
		m.put("Signature required for this change, please generate system GPG signing key first", "Firma richiesta per questa modifica, genera prima la chiave di firma GPG di sistema");
		m.put("Signature verified successfully with OneDev GPG key", "Firma verificata con successo con la chiave GPG di OneDev");
		m.put("Signature verified successfully with committer's GPG key", "Firma verificata con successo con la chiave GPG del committer");
		m.put("Signature verified successfully with committer's SSH key", "Firma verificata con successo con la chiave SSH del committer");
		m.put("Signature verified successfully with tagger's GPG key", "Firma verificata con successo con la chiave GPG del tagger");
		m.put("Signature verified successfully with tagger's SSH key", "Firma verificata con successo con la chiave SSH del tagger");
		m.put("Signature verified successfully with trusted GPG key", "Firma verificata con successo con la chiave GPG fidata");
		m.put("Signed with an unknown GPG key ", "Firmato con una chiave GPG sconosciuta");
		m.put("Signed with an unknown ssh key", "Firmato con una chiave ssh sconosciuta");
		m.put("Signer Email Addresses", "Indirizzi email del firmatario");
		m.put("Signing Key ID", "ID della chiave di firma");
		m.put("Similar Issues", "Problematiche simili");
		m.put("Single Sign On", "Single Sign-On");
		m.put("Single Sign-On", "Single Sign-On");
		m.put("Single sign on via discord.com", "Single Sign-On tramite discord.com");
		m.put("Single sign on via twitch.tv", "Single Sign-On tramite twitch.tv");
		m.put("Site", "Sito");
		m.put("Size", "Dimensione");
		m.put("Size invalid", "Dimensione non valida");
		m.put("Slack Notifications", "Notifiche Slack");
		m.put("Smtp Ssl Setting", "Impostazione Smtp Ssl");
		m.put("Smtp With Ssl", "Smtp con Ssl");
		m.put("Some builds are {0}", "Alcune build sono {0}");
		m.put("Some jobs are hidden due to permission policy", "Alcuni job sono nascosti a causa della politica dei permessi");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "Qualcuno ha modificato il contenuto che stai modificando. Ricarica la pagina e riprova.");
		m.put("Some other pull requests are opening to this branch", "Altre pull request sono aperte per questo branch");
		m.put("Some projects might be hidden due to permission policy", "Alcuni progetti potrebbero essere nascosti a causa della politica dei permessi");
		m.put("Some related commits of the code comment is missing", "Alcuni commit correlati al commento del codice mancano");
		m.put("Some related commits of the pull request are missing", "Alcuni commit correlati alla pull request mancano");
		m.put("Some required builds not passed", "Alcune build richieste non sono passate");
		m.put("Someone made below change since you started editing", "Qualcuno ha apportato la seguente modifica da quando hai iniziato a modificare");
		m.put("Sort", "Ordina");
		m.put("Source", "Sorgente");
		m.put("Source Docker Image", "Immagine Docker di origine");
		m.put("Source Lines", "Linee di origine");
		m.put("Source Path", "Percorso di origine");
		m.put("Source branch already exists", "Il branch di origine esiste già");
		m.put("Source branch already merged into target branch", "Il branch di origine è già stato unito al branch di destinazione");
		m.put("Source branch commits will be rebased onto target branch", "I commit del branch di origine saranno rebase sul branch di destinazione");
		m.put("Source branch is default branch", "Il branch di origine è il branch predefinito");
		m.put("Source branch is outdated", "Il branch di origine è obsoleto");
		m.put("Source branch no longer exists", "Il branch di origine non esiste più");
		m.put("Source branch updated successfully", "Il branch di origine è stato aggiornato con successo");
		m.put("Source project no longer exists", "Il progetto di origine non esiste più");
		m.put("Specified Value", "Valore specificato");
		m.put("Specified choices", "Scelte specificate");
		m.put("Specified default value", "Valore predefinito specificato");
		m.put("Specified fields", "Campi specificati");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Specifica l'URL LDAP del server Active Directory, ad esempio: <i>ldap://ad-server</i>, o <i>ldaps://ad-server</i>. Nel caso in cui il server LDAP utilizzi un certificato autofirmato per la connessione ldaps, sarà necessario <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configurare OneDev per fidarsi del certificato</a>");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Specifica l'URL LDAP, ad esempio: <i>ldap://localhost</i>, o <i>ldaps://localhost</i>. Nel caso in cui il server LDAP utilizzi un certificato autofirmato per la connessione ldaps, sarà necessario <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configurare OneDev per fidarsi del certificato</a>");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"Specifica i nodi base per la ricerca degli utenti. Ad esempio: <i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"Specifica il nome dell'attributo all'interno della voce LDAP dell'utente il cui valore contiene i nomi distinti dei gruppi di appartenenza. Ad esempio, alcuni server LDAP utilizzano l'attributo <i>memberOf</i> per elencare i gruppi");
		m.put("Specifies password of above manager DN", "Specifica la password del DN del manager sopra indicato");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"Specifica l'attributo contenente il nome del gruppo all'interno della voce LDAP del gruppo trovato. Il valore di questo attributo sarà mappato a un gruppo OneDev. Questo attributo è normalmente impostato su <i>cn</i>");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"Specifica il file di risultati del test .net TRX relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio <tt>TestResults/*.trx</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> il cui valore è un token di accesso con permesso di scrittura del codice sui progetti sopra indicati. I commit, i problemi e le richieste di pull saranno creati anche sotto il nome del proprietario del token di accesso");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"Specifica il file di output json di <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo file può essere generato con l'opzione di output json di clippy, ad esempio <code>cargo clippy --message-format json>check-result.json</code>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify Build Options", "Specifica le opzioni di build");
		m.put("Specify CPD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match", 
			"Specifica il file xml dei risultati CPD relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio, <tt>target/cpd.xml</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify Commit Message", "Specifica il messaggio di commit");
		m.put("Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. Use * or ? for pattern match", 
			"Specifica il file di report ESLint in formato checkstyle sotto <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo file può essere generato con l'opzione ESLint <tt>'-f checkstyle'</tt> e <tt>'-o'</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify GitHub API url, for instance <tt>https://api.github.com</tt>", "Specifica l'URL API di GitHub, ad esempio <tt>https://api.github.com</tt>");
		m.put("Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>", "Specifica l'URL API di GitLab, ad esempio <tt>https://gitlab.example.com/api/v4</tt>");
		m.put("Specify Gitea API url, for instance <tt>https://gitea.example.com/api/v1</tt>", "Specifica l'URL API di Gitea, ad esempio <tt>https://gitea.example.com/api/v1</tt>");
		m.put("Specify GoogleTest XML result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This report can be generated with environment variable <tt>GTEST_OUTPUT</tt> when running tests, For instance, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * or ? for pattern match", 
			"Specifica il file XML dei risultati di GoogleTest relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo report può essere generato con la variabile di ambiente <tt>GTEST_OUTPUT</tt> durante l'esecuzione dei test, ad esempio, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify IMAP user name.<br><b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system email address specified above", 
			"Specifica il nome utente IMAP.<br><b class='text-danger'>NOTA: </b> Questo account dovrebbe essere in grado di ricevere email inviate all'indirizzo email di sistema specificato sopra");
		m.put("Specify JUnit test result file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match", 
			"Specifica il file dei risultati del test JUnit in formato XML relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio <tt>target/surefire-reports/TEST-*.xml</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match", 
			"Specifica il file di report di copertura xml JaCoCo relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio, <tt>target/site/jacoco/jacoco.xml</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify Jest coverage report file in clover format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. Use * or ? for pattern match", 
			"Specifica il file di report di copertura Jest in formato clover relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio <tt>coverage/clover.xml</tt>. Questo file può essere generato con l'opzione Jest <tt>'--coverage'</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify Jest test result file in json format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match", 
			"Specifica il file dei risultati del test Jest in formato json relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo file può essere generato tramite l'opzione Jest <tt>'--json'</tt> e <tt>'--outputFile'</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify OCI layout directory of the image to scan. This directory can be generated via build image step or pull image step. It should be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Specifica la directory di layout OCI dell'immagine da scansionare. Questa directory può essere generata tramite il passaggio di build dell'immagine o il passaggio di pull dell'immagine. Dovrebbe essere relativa a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>");
		m.put("Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from", 
			"Specifica la directory di layout OCI relativa a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> da cui eseguire il push");
		m.put("Specify OpenID scopes to request", "Specifica gli scope OpenID da richiedere");
		m.put("Specify PMD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/pmd.xml</tt>. Use * or ? for pattern match", 
			"Specifica il file xml dei risultati PMD relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio, <tt>target/pmd.xml</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify PowerShell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of your script<br>", 
			"Specifica i comandi PowerShell da eseguire sotto <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTA: </b> OneDev controlla il codice di uscita dello script per determinare se il passaggio è riuscito. Poiché PowerShell esce sempre con 0 anche se ci sono errori nello script, dovresti gestire gli errori nello script e uscire con un codice diverso da zero, o aggiungere la riga <code>$ErrorActionPreference = &quot;Stop&quot;</code> all'inizio del tuo script<br>");
		m.put("Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with <i>-o</i> option. Use * or ? for pattern match", 
			"Specifica il file di output delle diagnosi Roslynator in formato XML relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo file può essere generato con l'opzione <i>-o</i>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify Shell/Batch Commands to Run", "Specifica i comandi Shell/Batch da eseguire");
		m.put("Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match", 
			"Specifica il file xml dei risultati SpotBugs relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio, <tt>target/spotbugsXml.xml</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify System Settings", "Specifica le impostazioni di sistema");
		m.put("Specify URL of remote git repository. Only http/https protocol is supported", "Specifica l'URL del repository git remoto. Sono supportati solo i protocolli http/https");
		m.put("Specify YouTrack login name. This account should have permission to:<ul><li>Read full information and issues of the projects you want to import<li>Read issue tags<li>Read user basic information</ul>", 
			"Specifica il nome di login di YouTrack. Questo account dovrebbe avere il permesso di:<ul><li>Leggere informazioni complete e problemi dei progetti che vuoi importare<li>Leggere i tag dei problemi<li>Leggere informazioni di base sugli utenti</ul>");
		m.put("Specify YouTrack password or access token for above user", "Specifica la password o il token di accesso di YouTrack per l'utente sopra indicato");
		m.put("Specify a &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regular expression&lt;/a&gt; to match issue references. For instance:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;", 
			"Specifica una &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;espressione regolare&lt;/a&gt; per corrispondere ai riferimenti ai problemi. Ad esempio:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> after issue number", 
			"Specifica una <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>espressione regolare</a> dopo il numero del problema");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> before issue number", 
			"Specifica una <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>espressione regolare</a> prima del numero del problema");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> da utilizzare come chiave privata SSH");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> da utilizzare come token di accesso");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> da utilizzare come token di accesso per importare la specifica di build dal progetto sopra indicato se il suo codice non è accessibile pubblicamente");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> da utilizzare come password o token di accesso del registro");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> da utilizzare come password o token di accesso per accedere al repository remoto");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> da utilizzare come chiave privata per l'autenticazione SSH. <b class='text-info'>NOTA:</b> La chiave privata con passphrase non è supportata");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> da utilizzare come chiave privata dell'utente sopra indicato per l'autenticazione SSH. <b class='text-info'>NOTA:</b> La chiave privata con passphrase non è supportata");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> il cui valore è un token di accesso con permesso di gestione per il progetto sopra indicato. Nota che il token di accesso non è richiesto se la sincronizzazione è con il progetto corrente o figlio e il commit di build è raggiungibile dal branch predefinito");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Specifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segreto di lavoro</a> il cui valore è un token di accesso con permesso di caricamento della cache per il progetto sopra indicato. Nota che questa proprietà non è richiesta se il caricamento della cache è nel progetto corrente o figlio e il commit di build è raggiungibile dal branch predefinito");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"Specifica un <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> per avviare automaticamente il lavoro. <b class='text-info'>Nota:</b> Per risparmiare risorse, i secondi nell'espressione cron saranno ignorati e l'intervallo minimo di pianificazione è di un minuto");
		m.put("Specify a Docker Image to Test Against", "Specifica un'immagine Docker da testare");
		m.put("Specify a custom field of Enum type", "Specifica un campo personalizzato di tipo Enum");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "Specifica una query predefinita per filtrare/ordinare i problemi risolti dei lavori specificati");
		m.put("Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into", 
			"Specifica un file relativo a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> in cui scrivere il checksum");
		m.put("Specify a multi-value user field to hold assignees information.<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica un campo utente multi-valore per contenere le informazioni sugli assegnatari.<b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso in cui non ci sia un'opzione appropriata qui");
		m.put("Specify a multi-value user field to hold assignees information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica un campo utente multi-valore per contenere le informazioni sugli assegnatari.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso in cui non ci sia un'opzione appropriata qui");
		m.put("Specify a path inside container to be used as mount target", "Specifica un percorso all'interno del container da utilizzare come target di montaggio");
		m.put("Specify a path relative to job workspace to be used as mount source. Leave empty to mount job workspace itself", 
			"Specifica un percorso relativo al job workspace da utilizzare come sorgente di montaggio. Lascia vuoto per montare il job workspace stesso");
		m.put("Specify a secret to be used as access token to create issue in above project if it is not publicly accessible", 
			"Specifica un segreto da utilizzare come token di accesso per creare un problema nel progetto sopra indicato se non è accessibile pubblicamente");
		m.put("Specify a secret to be used as access token to retrieve artifacts from above project. If not specified, project artifacts will be accessed anonymously", 
			"Specifica un segreto da utilizzare come token di accesso per recuperare gli artefatti dal progetto sopra indicato. Se non specificato, gli artefatti del progetto saranno accessibili in modo anonimo");
		m.put("Specify a secret to be used as access token to trigger job in above project", "Specifica un segreto da utilizzare come token di accesso per avviare il lavoro nel progetto sopra");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Specifica un segreto il cui valore è un token di accesso con permesso di caricamento della cache per il progetto sopra indicato. Nota che questa proprietà non è richiesta se il caricamento della cache è nel progetto corrente o figlio e il commit di build è raggiungibile dal branch predefinito");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"Specifica il percorso assoluto al file di configurazione utilizzato da kubectl per accedere al cluster. Lascia vuoto per consentire a kubectl di determinare automaticamente le informazioni di accesso al cluster");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"Specifica il percorso assoluto all'utilità kubectl, ad esempio: <i>/usr/bin/kubectl</i>. Se lasciato vuoto, OneDev cercherà di trovare l'utilità dal percorso di sistema");
		m.put("Specify account name to login to Gmail to send/receive email", "Specifica il nome dell'account per accedere a Gmail per inviare/ricevere email");
		m.put("Specify additional users able to access this confidential issue besides those granted via role. Users mentioned in the issue will be authorized automatically", 
			"Specifica utenti aggiuntivi in grado di accedere a questo problema confidenziale oltre a quelli autorizzati tramite ruolo. Gli utenti menzionati nel problema saranno autorizzati automaticamente");
		m.put("Specify agents applicable for this executor", "Specifica gli agenti applicabili per questo executor");
		m.put("Specify allowed <a href='https://spdx.org/licenses/' target='_blank'>spdx license identifiers</a> <span class='text-warning'>separated by comma</span>", 
			"Specifica gli identificatori di licenza <a href='https://spdx.org/licenses/' target='_blank'>spdx consentiti</a> <span class='text-warning'>separati da virgola</span>");
		m.put("Specify an email address sharing same inbox as the system email address in mail setting definition. Emails sent to this address will be created as issues in this project. The default value takes form of <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>", 
			"Specifica un indirizzo email che condivide la stessa casella di posta dell'indirizzo email di sistema nella definizione delle impostazioni di posta. Le email inviate a questo indirizzo saranno create come problemi in questo progetto. Il valore predefinito assume la forma di <tt>&lt;nome indirizzo email di sistema&gt;+&lt;percorso progetto&gt;@&lt;dominio indirizzo email di sistema&gt;</tt>");
		m.put("Specify applicable projects for above option. Multiple projects should be separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Specifica i progetti applicabili per l'opzione sopra indicata. Più progetti dovrebbero essere separati da spazio. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>corrispondenza del pattern del percorso</a>. Prefisso con '-' per escludere. Lascia vuoto per tutti i progetti");
		m.put("Specify applicable projects separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Specifica i progetti applicabili separati da spazio. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>corrispondenza del pattern del percorso</a>. Prefisso con '-' per escludere. Lascia vuoto per tutti i progetti");
		m.put("Specify application (client) ID of the app registered in Entra ID", "Specifica l'ID applicazione (client) dell'app registrata in Entra ID");
		m.put("Specify arguments for imagetools. For instance <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>", 
			"Specifica gli argomenti per imagetools. Ad esempio <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>");
		m.put("Specify artifacts to retrieve into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved.", 
			"Specifica gli artefatti da recuperare in <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Solo gli artefatti pubblicati (tramite il passaggio di pubblicazione degli artefatti) possono essere recuperati.");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"Specifica almeno 10 caratteri alfanumerici da utilizzare come segreto, e poi aggiungi una voce di analisi in entrata sul lato SendGrid:<ul><li><code>URL di destinazione</code> dovrebbe essere impostato su <i>&lt;URL root di OneDev&gt;/~sendgrid/&lt;segreto&gt;</i>, ad esempio, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Nota che in ambiente di produzione, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https dovrebbe essere abilitato</a> per proteggere il segreto</li><li><code>Dominio di ricezione</code> dovrebbe essere lo stesso della parte di dominio dell'indirizzo email di sistema specificato sopra</li><li>L'opzione <code>POST il messaggio MIME completo e grezzo</code> è abilitata</li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"Specifica i nodi base per la ricerca degli utenti. Ad esempio: <i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "Specifica il branch per il commit della modifica suggerita");
		m.put("Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Specifica il branch su cui eseguire il lavoro. Può essere specificato un branch o un tag, ma non entrambi. Il branch predefinito verrà utilizzato se entrambi non sono specificati");
		m.put("Specify branch, tag or commit in above project to import build spec from", "Specifica il branch, il tag o il commit nel progetto sopra indicato per importare la specifica di build");
		m.put("Specify by Build Number", "Specifica per numero di build");
		m.put("Specify cache upload strategy after build successful. <var>Upload If Not Hit</var> means to upload when cache is not found with cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files in cache path are changed", 
			"Specifica la strategia di caricamento della cache dopo il successo della build. <var>Carica se non trovato</var> significa caricare quando la cache non è trovata con la chiave della cache (non le chiavi di caricamento), e <var>Carica se modificato</var> significa caricare se alcuni file nel percorso della cache sono modificati");
		m.put("Specify certificate to trust if you are using self-signed certificate for remote repository", 
			"Specifica il certificato da fidarsi se stai utilizzando un certificato autofirmato per il repository remoto");
		m.put("Specify certificates to trust if you are using self-signed certificates for your docker registries", 
			"Specifica i certificati da fidarsi se stai utilizzando certificati autofirmati per i tuoi registri Docker");
		m.put("Specify checkstyle result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/checkstyle-result.xml</tt>. Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> on how to generate the result xml file. Use * or ? for pattern match", 
			"Specifica il file xml dei risultati checkstyle relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio, <tt>target/checkstyle-result.xml</tt>. Consulta la <a href='https://checkstyle.org/'>documentazione di checkstyle</a> su come generare il file xml dei risultati. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify client secret of the app registered in Entra ID", "Specifica il segreto client dell'app registrata in Entra ID");
		m.put("Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/clover/clover.xml</tt>. Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> on how to generate clover xml file. Use * or ? for pattern match", 
			"Specifica il file di report di copertura xml clover relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio, <tt>target/site/clover/clover.xml</tt>. Consulta la <a href='https://openclover.org/documentation'>documentazione di OpenClover</a> su come generare il file xml clover. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match", 
			"Specifica il file di report di copertura xml cobertura relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio, <tt>target/site/cobertura/coverage.xml</tt>. Usa * o ? per la corrispondenza del pattern");
		m.put("Specify color of the state for displaying purpose", "Specifica il colore dello stato per scopi di visualizzazione");
		m.put("Specify columns of the board. Each column corresponds to a value of the issue field specified above", 
			"Specifica le colonne della board. Ogni colonna corrisponde a un valore del campo problema specificato sopra");
		m.put("Specify command to check readiness of the service. This command will be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be executed repeatedly until a zero code is returned to indicate service ready", 
			"Specifica il comando per verificare la prontezza del servizio. Questo comando sarà interpretato da cmd.exe sulle immagini Windows e da shell sulle immagini Linux. Sarà eseguito ripetutamente fino a quando non verrà restituito un codice zero per indicare che il servizio è pronto");
		m.put("Specify commands to be executed on remote machine. <b class='text-warning'>Note:</b> user environments will not be picked up when execute these commands, set up them explicitly in commands if necessary", 
			"Specifica i comandi da eseguire sulla macchina remota. <b class='text-warning'>Nota:</b> gli ambienti utente non verranno rilevati durante l'esecuzione di questi comandi, configurali esplicitamente nei comandi se necessario");
		m.put("Specify condition to retry build upon failure", "Specifica la condizione per riprovare la build in caso di errore");
		m.put("Specify configuration discovery url of your OpenID provider, for instance: <code>https://openid.example.com/.well-known/openid-configuration</code>. Make sure to use HTTPS protocol as OneDev relies on TLS encryption to ensure token validity", 
			"Specifica l'URL di scoperta della configurazione del tuo provider OpenID, ad esempio: <code>https://openid.example.com/.well-known/openid-configuration</code>. Assicurati di utilizzare il protocollo HTTPS poiché OneDev si basa sulla crittografia TLS per garantire la validità del token");
		m.put("Specify container image to execute commands inside", "Specifica l'immagine del container per eseguire i comandi all'interno");
		m.put("Specify container image to run", "Specifica l'immagine del container da eseguire");
		m.put("Specify cppcheck xml result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match", 
			"Specifica il file di risultati xml di cppcheck relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo file può essere generato con l'opzione di output xml di cppcheck, ad esempio <code>cppcheck src --xml 2>check-result.xml</code>. Usa * o ? per la corrispondenza dei pattern");
		m.put("Specify cpu request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Specifica la richiesta di CPU per ogni job/servizio utilizzando questo executor. Controlla <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>la gestione delle risorse di Kubernetes</a> per i dettagli");
		m.put("Specify default assignees of pull requests submitted to this project. Only users with the write code permission to the project can be selected", 
			"Specifica gli assegnatari predefiniti delle pull request inviate a questo progetto. Solo gli utenti con il permesso di scrivere codice nel progetto possono essere selezionati");
		m.put("Specify default merge strategy of pull requests submitted to this project", "Specifica la strategia di merge predefinita delle pull request inviate a questo progetto");
		m.put("Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple destinations should be separated with space", 
			"Specifica le destinazioni, ad esempio <tt>registry-server:5000/myorg/myrepo:latest</tt>. Assicurati di utilizzare <b>lo stesso host</b> specificato nell'URL del server delle impostazioni di sistema se desideri inviare al registro integrato, oppure usa semplicemente la forma <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Le destinazioni multiple devono essere separate da uno spazio");
		m.put("Specify directory (tenant) ID of the app registered in Entra ID", "Specifica l'ID della directory (tenant) dell'app registrata in Entra ID");
		m.put("Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Specifica la directory relativa a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> per archiviare il layout OCI");
		m.put("Specify docker image of the service", "Specifica l'immagine Docker del servizio");
		m.put("Specify dockerx builder used to build docker image. OneDev will create the builder automatically if it does not exist. Check <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> on how to customize the builder for instance to allow publishing to insecure registries", 
			"Specifica il builder dockerx utilizzato per costruire l'immagine Docker. OneDev creerà automaticamente il builder se non esiste. Controlla <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>questo tutorial</a> su come personalizzare il builder, ad esempio per consentire la pubblicazione su registri non sicuri");
		m.put("Specify email addresses to send invitations, with one per line", "Specifica gli indirizzi email a cui inviare gli inviti, uno per riga");
		m.put("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\"", 
			"Specifica il tempo stimato <b class='text-warning'>solo per questo problema</b>, senza contare \"{0}\"");
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"Specifica i campi di vari problemi creati da Renovate per orchestrare l'aggiornamento delle dipendenze");
		m.put("Specify fields to be displayed in the issue list", "Specifica i campi da visualizzare nella lista dei problemi");
		m.put("Specify fields to display in board card", "Specifica i campi da visualizzare nella scheda della board");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"Specifica i file relativi a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> da pubblicare. Usa * o ? per la corrispondenza dei pattern");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Specifica i file da cui creare il checksum md5. I file multipli devono essere separati da uno spazio. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>I pattern Globstar</a> sono accettati. I file non assoluti sono considerati relativi a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>");
		m.put("Specify files under above directory to be published", "Specifica i file sotto la directory sopra da pubblicare");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be included in these files to be served as site start page", 
			"Specifica i file sotto la directory sopra da pubblicare. Usa * o ? per la corrispondenza dei pattern. <b>NOTA:</b> <code>index.html</code> deve essere incluso in questi file per essere servito come pagina iniziale del sito");
		m.put("Specify group to import from. Leave empty to import from projects under current account", 
			"Specifica il gruppo da importare. Lascia vuoto per importare dai progetti sotto l'account corrente");
		m.put("Specify how to map GitHub issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica come mappare le etichette dei problemi di GitHub ai campi personalizzati di OneDev.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify how to map GitLab issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica come mappare le etichette dei problemi di GitLab ai campi personalizzati di OneDev.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify how to map Gitea issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica come mappare le etichette dei problemi di Gitea ai campi personalizzati di OneDev.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify how to map JIRA issue priorities to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica come mappare le priorità dei problemi di JIRA ai campi personalizzati di OneDev.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify how to map JIRA issue statuses to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Specifica come mappare gli stati dei problemi di JIRA ai campi personalizzati di OneDev.<br><b>NOTA: </b> Puoi personalizzare gli stati dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify how to map JIRA issue types to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica come mappare i tipi di problemi di JIRA ai campi personalizzati di OneDev.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify how to map YouTrack issue fields to OneDev. Unmapped fields will be reflected in issue description.<br><b>Note: </b><ul><li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, for instance <tt>Priority::Critical</tt><li>You may customize OneDev issue fields in case there is no appropriate option here</ul>", 
			"Specifica come mappare i campi dei problemi di YouTrack a OneDev. I campi non mappati saranno riflessi nella descrizione del problema.<br><b>Nota: </b><ul><li>Il campo Enum deve essere mappato nella forma <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, ad esempio <tt>Priority::Critical</tt><li>Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui</ul>");
		m.put("Specify how to map YouTrack issue links to OneDev issue links.<br><b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here", 
			"Specifica come mappare i collegamenti dei problemi di YouTrack ai collegamenti dei problemi di OneDev.<br><b>NOTA: </b> Puoi personalizzare i collegamenti dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify how to map YouTrack issue state to OneDev issue state. Unmapped states will use the initial state in OneDev.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Specifica come mappare lo stato dei problemi di YouTrack allo stato dei problemi di OneDev. Gli stati non mappati utilizzeranno lo stato iniziale in OneDev.<br><b>NOTA: </b> Puoi personalizzare gli stati dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify how to map YouTrack issue tags to OneDev issue custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Specifica come mappare i tag dei problemi di YouTrack ai campi personalizzati dei problemi di OneDev.<br><b>NOTA: </b> Puoi personalizzare i campi dei problemi di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify image on the login button", "Specifica l'immagine sul pulsante di login");
		m.put("Specify image tag to pull from, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to pull from built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Specifica il tag dell'immagine da scaricare, ad esempio <tt>registry-server:5000/myorg/myrepo:latest</tt>. Assicurati di utilizzare <b>lo stesso host</b> specificato nell'URL del server delle impostazioni di sistema se desideri scaricare dal registro integrato, oppure usa semplicemente la forma <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>");
		m.put("Specify image tag to push to, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Specifica il tag dell'immagine da inviare, ad esempio <tt>registry-server:5000/myorg/myrepo:latest</tt>. Assicurati di utilizzare <b>lo stesso host</b> specificato nell'URL del server delle impostazioni di sistema se desideri inviare al registro integrato, oppure usa semplicemente la forma <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>");
		m.put("Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple tags should be separated with space", 
			"Specifica i tag delle immagini da inviare, ad esempio <tt>registry-server:5000/myorg/myrepo:latest</tt>. Assicurati di utilizzare <b>lo stesso host</b> specificato nell'URL del server delle impostazioni di sistema se desideri inviare al registro integrato, oppure usa semplicemente la forma <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. I tag multipli devono essere separati da uno spazio");
		m.put("Specify import option", "Specifica l'opzione di importazione");
		m.put("Specify incoming email poll interval in seconds", "Specifica l'intervallo di polling delle email in arrivo in secondi");
		m.put("Specify issue creation settings. For a particular sender and project, the first matching entry will take effect. Issue creation will be disallowed if no matching entry found", 
			"Specifica le impostazioni di creazione dei problemi. Per un particolare mittente e progetto, verrà applicata la prima voce corrispondente. La creazione dei problemi sarà disabilitata se non viene trovata alcuna voce corrispondente");
		m.put("Specify issue field to identify different columns of the board. Only state and single-valued enumeration field can be used here", 
			"Specifica il campo del problema per identificare le diverse colonne della board. Solo lo stato e il campo di enumerazione a valore singolo possono essere utilizzati qui");
		m.put("Specify links to be displayed in the issue list", "Specifica i collegamenti da visualizzare nella lista dei problemi");
		m.put("Specify links to display in board card", "Specifica i collegamenti da visualizzare nella scheda della board");
		m.put("Specify manager DN to authenticate OneDev itself to Active Directory. The manager DN should be specified in form of <i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>manager@example.com</i>", 
			"Specifica il DN del manager per autenticare OneDev stesso con Active Directory. Il DN del manager deve essere specificato nella forma <i>&lt;account name&gt;@&lt;domain&gt;</i>, ad esempio: <i>manager@example.com</i>");
		m.put("Specify manager DN to authenticate OneDev itself to LDAP server", "Specifica il DN del manager per autenticare OneDev stesso con il server LDAP");
		m.put("Specify markdown file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published", 
			"Specifica il file markdown relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> da pubblicare");
		m.put("Specify max git LFS file size in mega bytes", "Specifica la dimensione massima del file git LFS in megabyte");
		m.put("Specify max number of CPU intensive tasks the server can run concurrently, such as Git repository pull/push, repository index, etc.", 
			"Specifica il numero massimo di attività intensive per la CPU che il server può eseguire contemporaneamente, come il pull/push del repository Git, l'indicizzazione del repository, ecc.");
		m.put("Specify max number of jobs this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Specifica il numero massimo di job che questo executor può eseguire contemporaneamente su ciascun agente corrispondente. Lascia vuoto per impostare come core della CPU dell'agente");
		m.put("Specify max number of jobs this executor can run concurrently. Leave empty to set as CPU cores", 
			"Specifica il numero massimo di job che questo executor può eseguire contemporaneamente. Lascia vuoto per impostare come core della CPU");
		m.put("Specify max number of jobs/services this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Specifica il numero massimo di job/servizi che questo executor può eseguire contemporaneamente su ciascun agente corrispondente. Lascia vuoto per impostare come core della CPU dell'agente");
		m.put("Specify max number of jobs/services this executor can run concurrently. Leave empty to set as CPU cores", 
			"Specifica il numero massimo di job/servizi che questo executor può eseguire contemporaneamente. Lascia vuoto per impostare come core della CPU");
		m.put("Specify max size of uploaded file in mega bytes via web interface. This applies to file uploaded to repository, markdown content (issue comment etc), and build artifacts", 
			"Specifica la dimensione massima del file caricato in megabyte tramite l'interfaccia web. Questo si applica ai file caricati nel repository, al contenuto markdown (commenti sui problemi, ecc.) e agli artefatti di build");
		m.put("Specify memory request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Specifica la richiesta di memoria per ogni job/servizio utilizzando questo executor. Controlla <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>la gestione delle risorse di Kubernetes</a> per i dettagli");
		m.put("Specify mypy output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated by redirecting mypy output <b>without option '--pretty'</b>, for instance <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * or ? for pattern match", 
			"Specifica il file di output mypy relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo file può essere generato reindirizzando l'output di mypy <b>senza l'opzione '--pretty'</b>, ad esempio <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Usa * o ? per la corrispondenza dei pattern");
		m.put("Specify name of the branch", "Specifica il nome del branch");
		m.put("Specify name of the environment variable", "Specifica il nome della variabile di ambiente");
		m.put("Specify name of the iteration", "Specifica il nome dell'iterazione");
		m.put("Specify name of the job", "Specifica il nome del job");
		m.put("Specify name of the report to be displayed in build detail page", "Specifica il nome del report da visualizzare nella pagina dei dettagli della build");
		m.put("Specify name of the saved query", "Specifica il nome della query salvata");
		m.put("Specify name of the service, which will be used as host name to access the service", 
			"Specifica il nome del servizio, che verrà utilizzato come nome host per accedere al servizio");
		m.put("Specify name of the tag", "Specifica il nome del tag");
		m.put("Specify network timeout in seconds when authenticate through this system", "Specifica il timeout di rete in secondi durante l'autenticazione tramite questo sistema");
		m.put("Specify node selector of this locator", "Specifica il selettore di nodi di questo localizzatore");
		m.put("Specify password or access token of specified registry", "Specifica la password o il token di accesso del registro specificato");
		m.put("Specify password to authenticate with", "Specifica la password per autenticarsi");
		m.put("Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>", "Specifica il percorso dell'eseguibile curl, ad esempio: <tt>/usr/bin/curl</tt>");
		m.put("Specify path to git executable, for instance: <tt>/usr/bin/git</tt>", "Specifica il percorso dell'eseguibile git, ad esempio: <tt>/usr/bin/git</tt>");
		m.put("Specify powershell executable to be used", "Specifica l'eseguibile powershell da utilizzare");
		m.put("Specify project to import build spec from", "Specifica il progetto da cui importare la specifica della build");
		m.put("Specify project to import into at OneDev side", "Specifica il progetto da importare nel lato OneDev");
		m.put("Specify project to retrieve artifacts from", "Specifica il progetto da cui recuperare gli artefatti");
		m.put("Specify project to run job in", "Specifica il progetto in cui eseguire il lavoro");
		m.put("Specify projects", "Specifica i progetti");
		m.put("Specify projects to update dependencies. Leave empty for current project", "Specifica i progetti per aggiornare le dipendenze. Lascia vuoto per il progetto corrente");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Specifica il file di risultati json di pylint relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo file può essere generato con l'opzione di formato di output json di pylint, ad esempio <code>--exit-zero --output-format=json:pylint-result.json</code>. Nota che non falliamo il comando pylint in caso di violazioni, poiché questo passaggio fallirà la build in base alla soglia configurata. Usa * o ? per la corrispondenza dei pattern");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"Specifica i login del registro se necessario. Per il registro integrato, usa <code>@server_url@</code> per l'URL del registro, <code>@job_token@</code> per il nome utente e il token di accesso per la password");
		m.put("Specify registry url. Leave empty for official registry", "Specifica l'URL del registro. Lascia vuoto per il registro ufficiale");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Specifica il percorso relativo sotto <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> per archiviare il layout OCI");
		m.put("Specify repositories", "Specifica i repository");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"Specifica i revisori richiesti se il percorso specificato viene modificato. Nota che l'utente che invia la modifica è considerato automaticamente come revisore della modifica");
		m.put("Specify root URL to access this server", "Specifica l'URL radice per accedere a questo server");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Specifica il file di risultati json di ruff relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Questo file può essere generato con l'opzione di formato di output json di ruff, ad esempio <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Nota che non falliamo il comando ruff in caso di violazioni, poiché questo passaggio fallirà la build in base alla soglia configurata. Usa * o ? per la corrispondenza dei pattern");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Specifica i comandi shell (su Linux/Unix) o batch (su Windows) da eseguire sotto <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Specifica i comandi shell da eseguire sotto <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>");
		m.put("Specify shell to be used", "Specifica la shell da utilizzare");
		m.put("Specify source param for SCP command, for instance <code>app.tar.gz</code>", "Specifica il parametro di origine per il comando SCP, ad esempio <code>app.tar.gz</code>");
		m.put("Specify space separated refs to pull from remote. '*' can be used in ref name for wildcard match<br><b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update branches/tags via this step", 
			"Specifica i ref separati da spazio da scaricare dal remoto. '*' può essere utilizzato nel nome del ref per la corrispondenza con caratteri jolly<br><b class='text-danger'>NOTA:</b> la regola di protezione di branch/tag verrà ignorata durante l'aggiornamento di branch/tag tramite questo passaggio");
		m.put("Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Specifica i branch separati da spazio da proteggere. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza con caratteri jolly</a>. Prefissa con '-' per escludere");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Specifica i job separati da spazio. Usa '*' o '?' per la corrispondenza con caratteri jolly. Prefissa con '-' per escludere");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts will be granted implicitly in matched jobs even if no other permissions are specified here", 
			"Specifica i job separati da spazio. Usa '*' o '?' per la corrispondenza con caratteri jolly. Prefissa con '-' per escludere. <b class='text-danger'>NOTA: </b> Il permesso di accedere agli artefatti di build verrà concesso implicitamente nei job corrispondenti anche se non vengono specificati altri permessi qui");
		m.put("Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Specifica i percorsi separati da spazio da proteggere. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza con caratteri jolly</a>. Prefissa con '-' per escludere");
		m.put("Specify space-separated projects applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Specifica i progetti separati da spazio applicabili per questa voce. Usa '*' o '?' per la corrispondenza con caratteri jolly. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti i progetti");
		m.put("Specify space-separated sender email addresses applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all senders", 
			"Specifica gli indirizzi email del mittente separati da spazio applicabili per questa voce. Usa '*' o '?' per la corrispondenza con caratteri jolly. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti i mittenti");
		m.put("Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Specifica i tag separati da spazio da proteggere. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza con caratteri jolly</a>. Prefissa con '-' per escludere");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>", 
			"Specifica la pagina iniziale del report relativa a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio: <tt>manual/index.md</tt>");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"Specifica la pagina iniziale del report relativa a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, ad esempio: api/index.html");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"Specifica la dimensione di archiviazione da richiedere per il volume di build. La dimensione deve conformarsi al <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>formato di capacità delle risorse di Kubernetes</a>, ad esempio <i>10Gi</i>");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"Specifica la larghezza della tabulazione utilizzata per calcolare il valore della colonna dei problemi trovati nel report fornito");
		m.put("Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Specifica il tag su cui eseguire il lavoro. Può essere specificato un branch o un tag, ma non entrambi. Il branch predefinito verrà utilizzato se entrambi non sono specificati");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"Specifica il parametro target per il comando SCP, ad esempio <code>user@@host:/app</code>. <b class='text-info'>NOTA:</b> Assicurati che il comando scp sia installato sull'host remoto");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"Specifica il testo da sostituire con i riferimenti alle issue corrispondenti, ad esempio: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Qui $1 e $2 rappresentano i gruppi di cattura nel pattern di esempio delle issue (vedi aiuto sul pattern delle issue)");
		m.put("Specify the condition current build must satisfy to execute this action", "Specifica la condizione che l'attuale build deve soddisfare per eseguire questa azione");
		m.put("Specify the condition preserved builds must match", "Specifica la condizione che i build preservati devono soddisfare");
		m.put("Specify the private key (in PEM format) used by SSH server to establish connections with client", 
			"Specifica la chiave privata (in formato PEM) utilizzata dal server SSH per stabilire connessioni con il client");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"Specifica la strategia per recuperare informazioni sull'appartenenza ai gruppi. Per assegnare permessi appropriati a un gruppo LDAP, deve essere definito un gruppo OneDev con lo stesso nome. Usa la strategia <tt>Non Recuperare Gruppi</tt> se desideri gestire le appartenenze ai gruppi sul lato OneDev");
		m.put("Specify timeout in seconds when communicating with mail server", "Specifica il timeout in secondi durante la comunicazione con il server di posta");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "Specifica il timeout in secondi. Si conta dal momento in cui il job viene inviato");
		m.put("Specify title of the issue", "Specifica il titolo dell'issue");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "Specifica l'URL dell'API di YouTrack. Ad esempio <tt>http://localhost:8080/api</tt>");
		m.put("Specify user name of above machine for SSH authentication", "Specifica il nome utente della macchina sopra per l'autenticazione SSH");
		m.put("Specify user name of specified registry", "Specifica il nome utente del registro specificato");
		m.put("Specify user name of the registry", "Specifica il nome utente del registro");
		m.put("Specify user name to authenticate with", "Specifica il nome utente per l'autenticazione");
		m.put("Specify value of the environment variable", "Specifica il valore della variabile di ambiente");
		m.put("Specify web UI session timeout in minutes. Existing sessions will not be affected after changing this value.", 
			"Specifica il timeout della sessione dell'interfaccia web in minuti. Le sessioni esistenti non saranno influenzate dopo aver modificato questo valore.");
		m.put("Specify webhook url to post events", "Specifica l'URL del webhook per pubblicare eventi");
		m.put("Specify which issue state to use for closed GitHub issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Specifica quale stato dell'issue utilizzare per le issue chiuse su GitHub.<br><b>NOTA: </b> Puoi personalizzare gli stati delle issue di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify which issue state to use for closed GitLab issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Specifica quale stato dell'issue utilizzare per le issue chiuse su GitLab.<br><b>NOTA: </b> Puoi personalizzare gli stati delle issue di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify which issue state to use for closed Gitea issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Specifica quale stato dell'issue utilizzare per le issue chiuse su Gitea.<br><b>NOTA: </b> Puoi personalizzare gli stati delle issue di OneDev nel caso non ci sia un'opzione appropriata qui");
		m.put("Specify which states are considered as closed for various issues created by Renovate to orchestrate dependency update. Additionally, when Renovate closes the issue, OneDev will transit the issue to first state specified here", 
			"Specifica quali stati sono considerati chiusi per le varie issue create da Renovate per orchestrare l'aggiornamento delle dipendenze. Inoltre, quando Renovate chiude l'issue, OneDev transiterà l'issue al primo stato specificato qui");
		m.put("Specify working days per week. This will affect parsing and displaying of working periods. For instance <tt>1w</tt> is the same as <tt>5d</tt> if this property is set to <tt>5</tt>", 
			"Specifica i giorni lavorativi per settimana. Questo influenzerà l'analisi e la visualizzazione dei periodi lavorativi. Ad esempio <tt>1w</tt> è lo stesso di <tt>5d</tt> se questa proprietà è impostata su <tt>5</tt>");
		m.put("Specify working hours per day. This will affect parsing and displaying of working periods. For instance <tt>1d</tt> is the same as <tt>8h</tt> if this property is set to <tt>8</tt>", 
			"Specifica le ore lavorative per giorno. Questo influenzerà l'analisi e la visualizzazione dei periodi lavorativi. Ad esempio <tt>1d</tt> è lo stesso di <tt>8h</tt> se questa proprietà è impostata su <tt>8</tt>");
		m.put("Spent", "Trascorso");
		m.put("Spent Time", "Tempo Trascorso");
		m.put("Spent Time Issue Field", "Campo Issue Tempo Trascorso");
		m.put("Spent Time:", "Tempo Trascorso:");
		m.put("Spent time / estimated time", "Tempo trascorso / tempo stimato");
		m.put("Split", "Dividi");
		m.put("Split view", "Vista divisa");
		m.put("SpotBugs Report", "Rapporto SpotBugs");
		m.put("Squash Source Branch Commits", "Squash Commit del Branch Sorgente");
		m.put("Squash all commits from source branch into a single commit in target branch", "Unisci tutti i commit del branch sorgente in un unico commit nel branch di destinazione");
		m.put("Squash source branch commits", "Unisci i commit del branch sorgente");
		m.put("Ssh", "Ssh");
		m.put("Ssh Key", "Chiave Ssh");
		m.put("Ssh Setting", "Impostazione Ssh");
		m.put("Ssl Setting", "Impostazione Ssl");
		m.put("Sso Connector", "Connettore Sso");
		m.put("Sso Provider Bean", "Sso Provider Bean");
		m.put("Start At", "Inizia a");
		m.put("Start Date", "Data di Inizio");
		m.put("Start Page", "Pagina Iniziale");
		m.put("Start agent on remote Linux machine by running below command:", "Avvia l'agente su una macchina Linux remota eseguendo il comando seguente:");
		m.put("Start date", "Data di inizio");
		m.put("Start to watch once I am involved", "Inizia a osservare una volta che sono coinvolto");
		m.put("Start work", "Inizia il lavoro");
		m.put("Start/Due Date", "Data di Inizio/Scadenza");
		m.put("State", "Stato");
		m.put("State Durations", "Durate dello Stato");
		m.put("State Frequencies", "Frequenze dello Stato");
		m.put("State Spec", "Specifica dello Stato");
		m.put("State Transitions", "Transizioni dello Stato");
		m.put("State Trends", "Tendenze dello Stato");
		m.put("State of an issue is transited", "Lo stato di un problema è transitato");
		m.put("States", "Stati");
		m.put("Statistics", "Statistiche");
		m.put("Stats", "Statistiche");
		m.put("Stats Group", "Gruppo di Statistiche");
		m.put("Status", "Stato");
		m.put("Status Code", "Codice di Stato");
		m.put("Status code", "Codice di stato");
		m.put("Status code other than 200 indicating the error type", "Codice di stato diverso da 200 che indica il tipo di errore");
		m.put("Step", "Passaggio");
		m.put("Step Template", "Template del Passaggio");
		m.put("Step Templates", "Template dei Passaggi");
		m.put("Step {0} of {1}: ", "Passaggio {0} di {1}:");
		m.put("Steps", "Passaggi");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"I passaggi verranno eseguiti in serie sullo stesso nodo, condividendo lo stesso <a href='https://docs.onedev.io/concepts#job-workspace'>workspace del job</a>");
		m.put("Stop work", "Ferma il lavoro");
		m.put("Stopwatch Overdue", "Cronometro Scaduto");
		m.put("Storage Settings", "Impostazioni di archiviazione");
		m.put("Storage file missing", "File di archiviazione mancante");
		m.put("Storage not found", "Archiviazione non trovata");
		m.put("Stored with Git LFS", "Archiviato con Git LFS");
		m.put("Sub Keys", "Sottochiavi");
		m.put("Subject", "Oggetto");
		m.put("Submit", "Invia");
		m.put("Submit Reason", "Motivo dell'Invio");
		m.put("Submit Support Request", "Invia Richiesta di Supporto");
		m.put("Submitted After", "Inviato Dopo");
		m.put("Submitted At", "Inviato a");
		m.put("Submitted Before", "Inviato Prima");
		m.put("Submitted By", "Inviato da");
		m.put("Submitted manually", "Inviato manualmente");
		m.put("Submitter", "Mittente");
		m.put("Subscription Key", "Chiave di Abbonamento");
		m.put("Subscription Management", "Gestione Abbonamenti");
		m.put("Subscription data", "Dati di Abbonamento");
		m.put("Subscription key installed successfully", "Chiave di abbonamento installata con successo");
		m.put("Subscription key not applicable: this key is intended to activate a trial subscription", 
			"Chiave di abbonamento non applicabile: questa chiave è destinata ad attivare un abbonamento di prova");
		m.put("Subscription key not applicable: this key is intended to renew a user based subscription", 
			"Chiave di abbonamento non applicabile: questa chiave è destinata a rinnovare un abbonamento basato su utenti");
		m.put("Subscription key not applicable: this key is intended to renew an unlimited users subscription", 
			"Chiave di abbonamento non applicabile: questa chiave è destinata a rinnovare un abbonamento per utenti illimitati");
		m.put("Subscription key not applicable: this key is intended to update licensee of an existing subscription", 
			"Chiave di abbonamento non applicabile: questa chiave è destinata ad aggiornare il licenziatario di un abbonamento esistente");
		m.put("Success Rate", "Tasso di Successo");
		m.put("Successful", "Riuscito");
		m.put("Suffix Pattern", "Pattern Suffisso");
		m.put("Suggest changes", "Suggerisci modifiche");
		m.put("Suggested change", "Modifica suggerita");
		m.put("Suggestion is outdated either due to code change or pull request close", "Il suggerimento è obsoleto a causa di modifiche al codice o chiusura della pull request");
		m.put("Suggestions", "Suggerimenti");
		m.put("Summary", "Sommario");
		m.put("Support & Bug Report", "Supporto & Segnalazione Bug");
		m.put("Support Request", "Richiesta di Supporto");
		m.put("Swap", "Scambia");
		m.put("Switch to HTTP(S)", "Passa a HTTP(S)");
		m.put("Switch to SSH", "Passa a SSH");
		m.put("Symbol Name", "Nome Simbolo");
		m.put("Symbol name", "Nome simbolo");
		m.put("Symbols", "Simboli");
		m.put("Sync Replica Status and Back to Home", "Sincronizza Stato Replica e Torna alla Home");
		m.put("Sync Repository", "Sincronizza Repository");
		m.put("Sync Timing of All Queried Issues", "Sincronizza Tempistica di Tutte le Questioni Interrogate");
		m.put("Sync Timing of Selected Issues", "Sincronizza Tempistica delle Questioni Selezionate");
		m.put("Sync requested. Please check status after a while", "Sincronizzazione richiesta. Controlla lo stato dopo un po'");
		m.put("Synchronize", "Sincronizza");
		m.put("System", "Sistema");
		m.put("System Alert", "Avviso di Sistema");
		m.put("System Alert Template", "Template di Avviso di Sistema");
		m.put("System Date", "Data di Sistema");
		m.put("System Email Address", "Indirizzo Email di Sistema");
		m.put("System Maintenance", "Manutenzione del Sistema");
		m.put("System Setting", "Impostazione del Sistema");
		m.put("System Settings", "Impostazioni del Sistema");
		m.put("System email address defined in mail setting should be used as recipient of such email, and project name may be appended to this address using to indicate where to create issues. For instance, if system email address is specified as <tt>support@example.com</tt>, sending email to <tt>support+myproject@example.com</tt> will create issue in <tt>myproject</tt>. If project name is not appended, OneDev will look up the project using project designation information below", 
			"L'indirizzo email di sistema definito nelle impostazioni della posta dovrebbe essere utilizzato come destinatario di tale email, e il nome del progetto può essere aggiunto a questo indirizzo per indicare dove creare le questioni. Ad esempio, se l'indirizzo email di sistema è specificato come <tt>support@example.com</tt>, inviare un'email a <tt>support+myproject@example.com</tt> creerà una questione in <tt>myproject</tt>. Se il nome del progetto non è aggiunto, OneDev cercherà il progetto utilizzando le informazioni di designazione del progetto qui sotto");
		m.put("System settings have been saved", "Le impostazioni di sistema sono state salvate");
		m.put("System uuid", "UUID di sistema");
		m.put("TIMED_OUT", "TEMPO_SCADUTO");
		m.put("TRX Report (.net unit test)", "Rapporto TRX (test unità .net)");
		m.put("Tab Width", "Larghezza Tab");
		m.put("Tag", "Tag");
		m.put("Tag \"{0}\" already exists, please choose a different name", "Il tag \"{0}\" esiste già, scegli un nome diverso");
		m.put("Tag \"{0}\" already exists, please choose a different name.", "Il tag \"{0}\" esiste già, scegli un nome diverso.");
		m.put("Tag \"{0}\" created", "Tag \"{0}\" creato");
		m.put("Tag \"{0}\" deleted", "Tag \"{0}\" eliminato");
		m.put("Tag Message", "Messaggio del Tag");
		m.put("Tag Name", "Nome del Tag");
		m.put("Tag Protection", "Protezione del Tag");
		m.put("Tag creation", "Creazione del Tag");
		m.put("Tags", "Tag");
		m.put("Target", "Obiettivo");
		m.put("Target Branches", "Rami Obiettivo");
		m.put("Target Docker Image", "Immagine Docker Obiettivo");
		m.put("Target File", "File Obiettivo");
		m.put("Target Path", "Percorso Obiettivo");
		m.put("Target Project", "Progetto Obiettivo");
		m.put("Target branch no longer exists", "Il ramo obiettivo non esiste più");
		m.put("Target branch was fast-forwarded to source branch", "Il ramo obiettivo è stato avanzato velocemente al ramo sorgente");
		m.put("Target branch will be fast-forwarded to source branch", "Il ramo obiettivo sarà avanzato velocemente al ramo sorgente");
		m.put("Target containing spaces or starting with dash needs to be quoted", "Un obiettivo contenente spazi o che inizia con un trattino deve essere quotato");
		m.put("Target or source branch is updated. Please try again", "Il ramo obiettivo o sorgente è aggiornato. Riprova");
		m.put("Task List", "Lista di Attività");
		m.put("Task list", "Lista di attività");
		m.put("Tell user to reset password", "Indica all'utente di reimpostare la password");
		m.put("Template Name", "Nome del Template");
		m.put("Template saved", "Template salvato");
		m.put("Terminal close", "Chiusura del terminale");
		m.put("Terminal input", "Input del terminale");
		m.put("Terminal open", "Apertura del terminale");
		m.put("Terminal output", "Output del terminale");
		m.put("Terminal ready", "Terminale pronto");
		m.put("Terminal resize", "Ridimensionamento del terminale");
		m.put("Test", "Test");
		m.put("Test Case", "Caso di Test");
		m.put("Test Cases", "Casi di Test");
		m.put("Test Settings", "Impostazioni di Test");
		m.put("Test Suite", "Suite di Test");
		m.put("Test Suites", "Suite di Test");
		m.put("Test importing from {0}", "Importazione di test da {0}");
		m.put("Test mail has been sent to {0}, please check your mail box", "Un'email di test è stata inviata a {0}, controlla la tua casella di posta");
		m.put("Test successful: authentication passed", "Test riuscito: autenticazione superata");
		m.put("Test successful: authentication passed with below information retrieved:", "Test riuscito: autenticazione superata con le seguenti informazioni recuperate:");
		m.put("Text", "Testo");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "L'URL del server endpoint che riceverà le richieste POST del webhook");
		m.put("The change contains disallowed file type(s): {0}", "La modifica contiene tipo(i) di file non consentito(i): {0}");
		m.put("The first board will be the default board", "La prima bacheca sarà la bacheca predefinita");
		m.put("The first timesheet will be the default timesheet", "Il primo timesheet sarà il timesheet predefinito");
		m.put("The object you are deleting/disabling is still being used", "L'oggetto che stai eliminando/disabilitando è ancora in uso");
		m.put("The password reset url is invalid or obsolete", "L'url per reimpostare la password non è valido o è obsoleto");
		m.put("The permission to access build log", "Il permesso di accedere al log di build");
		m.put("The permission to access build pipeline", "Il permesso di accedere alla pipeline di build");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"Il permesso di eseguire manualmente un job. Implica anche il permesso di accedere al log di build, alla pipeline di build e a tutti i report pubblicati");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"Il segreto che ti permette di garantire che le richieste POST inviate all'URL del payload provengano da OneDev. Quando imposti un segreto, riceverai l'intestazione X-OneDev-Signature nella richiesta POST del webhook");
		m.put("The service desk feature enables user to create issues by sending emails to OneDev. Issues can be discussed over email completely, without the need of logging to OneDev.", 
			"La funzione del service desk consente agli utenti di creare questioni inviando email a OneDev. Le questioni possono essere discusse completamente via email, senza la necessità di accedere a OneDev.");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "Quindi inserisci il codice mostrato nell'autenticatore TOTP per verificare");
		m.put("Then publish package from project directory like below", "Quindi pubblica il pacchetto dalla directory del progetto come mostrato di seguito");
		m.put("Then push gem to the source", "Quindi invia il gem alla sorgente");
		m.put("Then push image to desired repository under specified project", "Quindi invia l'immagine al repository desiderato sotto il progetto specificato");
		m.put("Then push package to the source", "Quindi invia il pacchetto alla sorgente");
		m.put("Then resolve dependency via command step", "Quindi risolvi la dipendenza tramite il passaggio del comando");
		m.put("Then upload package to the repository with twine", "Quindi carica il pacchetto nel repository con twine");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"Ci sono <a wicket:id=\"openRequests\">richieste di pull aperte</a> contro il ramo <span wicket:id=\"branch\"></span>. Queste richieste di pull saranno scartate se il ramo viene eliminato.");
		m.put("There are incompatibilities since your upgraded version", "Ci sono incompatibilità dalla tua versione aggiornata");
		m.put("There are merge conflicts", "Ci sono conflitti di merge");
		m.put("There are merge conflicts.", "Ci sono conflitti di merge.");
		m.put("There are merge conflicts. You can still create the pull request though", "Ci sono conflitti di merge. Puoi comunque creare la richiesta di pull");
		m.put("There are unsaved changes, discard and continue?", "Ci sono modifiche non salvate, scartare e continuare?");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"Questi autenticatori normalmente funzionano sul tuo telefono cellulare, alcuni esempi sono Google Authenticator, Microsoft Authenticator, Authy, 1Password ecc.");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"Questo <span wicket:id=\"elementTypeName\"></span> è importato da <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>");
		m.put("This Month", "Questo Mese");
		m.put("This Week", "Questa Settimana");
		m.put("This account is disabled", "Questo account è disabilitato");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"Questo indirizzo dovrebbe essere <code>mittente verificato</code> in SendGrid e sarà utilizzato come indirizzo del mittente per varie notifiche email. Si può anche rispondere a questo indirizzo per pubblicare commenti su questioni o richieste di pull se l'opzione <code>Ricevi Email Pubblicate</code> è abilitata qui sotto");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"Questo indirizzo sarà utilizzato come indirizzo del mittente per varie notifiche email. L'utente può anche rispondere a questo indirizzo per pubblicare commenti su questioni o richieste di pull via email se l'opzione <code>Controlla Email in Arrivo</code> è abilitata qui sotto");
		m.put("This change is already opened for merge by pull request {0}", "Questa modifica è già aperta per il merge tramite la richiesta di pull {0}");
		m.put("This change is squashed/rebased onto base branch via a pull request", "Questa modifica è stata compressa/ribasata sul ramo base tramite una richiesta di pull");
		m.put("This change is squashed/rebased onto base branch via pull request {0}", "Questa modifica è stata compressa/ribasata sul ramo base tramite la richiesta di pull {0}");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "Questa modifica deve essere verificata da alcuni job. Invia una richiesta di pull invece");
		m.put("This commit is rebased", "Questo commit è stato ribasato");
		m.put("This date is using <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 format</a>", 
			"Questa data utilizza il <a href=\"https://www.w3.org/TR/NOTE-datetime\">formato ISO 8601</a>");
		m.put("This email address is being used", "Questo indirizzo email è già in uso");
		m.put("This executor runs build jobs as docker containers on OneDev server", "Questo executor esegue i lavori di build come container docker sul server OneDev");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"Questo executor esegue i lavori di build come container docker su macchine remote tramite <a href='/~administration/agents' target='_blank'>agenti</a>");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"Questo executor esegue i lavori di build come pod in un cluster Kubernetes. Non sono richiesti agenti.<b class='text-danger'>Nota:</b> Assicurati che l'URL del server sia specificato correttamente nelle impostazioni di sistema poiché i pod di lavoro devono accedervi per scaricare sorgenti e artefatti");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs", 
			"Questo executor esegue lavori di build con la funzionalità shell del server OneDev.<br><b class='text-danger'>ATTENZIONE</b>: I lavori eseguiti con questo executor hanno gli stessi permessi del processo del server OneDev. Assicurati che possa essere utilizzato solo da lavori fidati");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. Make sure it can only be used by trusted jobs", 
			"Questo executor esegue lavori di build con la funzionalità shell di macchine remote tramite <a href='/~administration/agents' target='_blank'>agenti</a><br><b class='text-danger'>ATTENZIONE</b>: I lavori eseguiti con questo executor hanno gli stessi permessi del processo dell'agente OneDev. Assicurati che possa essere utilizzato solo da lavori fidati");
		m.put("This field is required", "Questo campo è obbligatorio");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"Questo filtro viene utilizzato per determinare la voce LDAP per l'utente corrente. Ad esempio: <i>(&(uid={0})(objectclass=person))</i>. In questo esempio, <i>{0}</i> rappresenta il nome di login dell'utente corrente.");
		m.put("This installation does not have an active subscription and runs as community edition. To access <a href=\"https://onedev.io/pricing\">enterprise features</a>, an active subscription is required", 
			"Questa installazione non ha un abbonamento attivo e funziona come edizione comunitaria. Per accedere alle <a href=\"https://onedev.io/pricing\">funzionalità enterprise</a>, è necessario un abbonamento attivo");
		m.put("This installation has a trial subscription and is now running as enterprise edition", 
			"Questa installazione ha un abbonamento di prova ed è attualmente in esecuzione come edizione enterprise");
		m.put("This installation has an active subscription and runs as enterprise edition", "Questa installazione ha un abbonamento attivo e funziona come edizione enterprise");
		m.put("This installation has an expired subscription, and runs as community edition", "Questa installazione ha un abbonamento scaduto e funziona come edizione comunitaria");
		m.put("This installation has an unlimited users subscription and is now running as enterprise edition", 
			"Questa installazione ha un abbonamento per utenti illimitati ed è ora in esecuzione come edizione enterprise");
		m.put("This installation's subscription has expired and is now running as the community edition", 
			"L'abbonamento di questa installazione è scaduto ed è ora in esecuzione come edizione community");
		m.put("This is a Git LFS object, but the storage file is missing", "Questo è un oggetto Git LFS, ma il file di archiviazione è mancante");
		m.put("This is a built-in role and can not be deleted", "Questo è un ruolo integrato e non può essere eliminato");
		m.put("This is a disabled service account", "Questo è un account di servizio disabilitato");
		m.put("This is a layer cache. To use the cache, add below option to your docker buildx command", 
			"Questo è un cache di livello. Per utilizzare il cache, aggiungi l'opzione seguente al tuo comando docker buildx");
		m.put("This is a service account for task automation purpose", "Questo è un account di servizio per scopi di automazione delle attività");
		m.put("This is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Questa è una funzionalità enterprise. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("This key has already been used by another project", "Questa chiave è già stata utilizzata da un altro progetto");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"Questa chiave è associata a {0}, tuttavia NON è un indirizzo email verificato di questo utente");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here.<br><b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated from these lock files with the <b>generate checksum step</b> defined before this step", 
			"Questa chiave è utilizzata per determinare se c'è un colpo di cache nella gerarchia del progetto (ricerca dal progetto corrente al progetto radice in ordine, lo stesso per le chiavi di caricamento sotto). Una cache è considerata colpita se la sua chiave è esattamente la stessa della chiave definita qui.<br><b>NOTA:</b> Nel caso in cui il tuo progetto abbia file di blocco (package.json, pom.xml, ecc.) in grado di rappresentare lo stato della cache, questa chiave dovrebbe essere definita come &lt;nome cache&gt;-@file:checksum.txt@, dove checksum.txt è generato da questi file di blocco con il <b>passo di generazione checksum</b> definito prima di questo passo");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"Questa chiave viene utilizzata per scaricare e caricare il cache nella gerarchia del progetto (ricerca dal progetto corrente al progetto radice in ordine)");
		m.put("This key or one of its sub key is already added", "Questa chiave o una delle sue sottochiavi è già stata aggiunta");
		m.put("This key or one of its subkey is already in use", "Questa chiave o una delle sue sottochiavi è già in uso");
		m.put("This line has confusable unicode character modification", "Questa riga ha una modifica confondibile di caratteri unicode");
		m.put("This might happen when project points to a wrong git repository, or the commit is garbage collected.", 
			"Questo potrebbe accadere quando il progetto punta a un repository git errato o il commit è stato raccolto come spazzatura.");
		m.put("This might happen when project points to a wrong git repository, or these commits are garbage collected.", 
			"Questo potrebbe accadere quando il progetto punta a un repository git errato o questi commit sono stati raccolti come spazzatura.");
		m.put("This name has already been used by another board", "Questo nome è già stato utilizzato da un'altra bacheca");
		m.put("This name has already been used by another group", "Questo nome è già stato utilizzato da un altro gruppo");
		m.put("This name has already been used by another issue board in the project", "Questo nome è già stato utilizzato da un'altra bacheca di problemi nel progetto");
		m.put("This name has already been used by another job executor", "Questo nome è già stato utilizzato da un altro executor di lavoro");
		m.put("This name has already been used by another project", "Questo nome è già stato utilizzato da un altro progetto");
		m.put("This name has already been used by another provider", "Questo nome è già stato utilizzato da un altro provider");
		m.put("This name has already been used by another role", "Questo nome è già stato utilizzato da un altro ruolo");
		m.put("This name has already been used by another role.", "Questo nome è già stato utilizzato da un altro ruolo.");
		m.put("This name has already been used by another script", "Questo nome è già stato utilizzato da un altro script");
		m.put("This name has already been used by another state", "Questo nome è già stato utilizzato da un altro stato");
		m.put("This operation is disallowed by branch protection rule", "Questa operazione è vietata dalla regola di protezione del branch");
		m.put("This page lists changes since previous build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a>", 
			"Questa pagina elenca le modifiche rispetto alla build precedente sul <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">stesso stream</a>");
		m.put("This page lists recent commits fixing the issue", "Questa pagina elenca i commit recenti che risolvono il problema");
		m.put("This permission enables one to access confidential issues", "Questo permesso consente di accedere ai problemi confidenziali");
		m.put("This permission enables one to schedule issues into iterations", "Questo permesso consente di pianificare i problemi nelle iterazioni");
		m.put("This property is imported from {0}", "Questa proprietà è importata da {0}");
		m.put("This pull request has been discarded", "Questa pull request è stata scartata");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"Questo report verrà visualizzato nella pagina di panoramica della pull request se la build è attivata dalla pull request");
		m.put("This server is currently accessed via http protocol, please configure your docker daemon or buildx builder to <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">work with insecure registry</a>", 
			"Questo server è attualmente accessibile tramite protocollo http, configura il tuo demone docker o builder buildx per <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">lavorare con un registro non sicuro</a>");
		m.put("This shows average duration of different states over time", "Questo mostra la durata media dei diversi stati nel tempo");
		m.put("This shows average duration of merged pull requests over time", "Questo mostra la durata media delle pull request unite nel tempo");
		m.put("This shows number of <b>new</b> issues in different states over time", "Questo mostra il numero di <b>nuovi</b> problemi in diversi stati nel tempo");
		m.put("This shows number of issues in various states over time", "Questo mostra il numero di problemi in vari stati nel tempo");
		m.put("This shows number of open and merged pull requests over time", "Questo mostra il numero di pull request aperte e unite nel tempo");
		m.put("This step can only be executed by a docker aware executor", "Questo step può essere eseguito solo da un executor compatibile con docker");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Questo step può essere eseguito solo da un executor compatibile con docker. Viene eseguito sotto <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>workspace del lavoro</a>");
		m.put("This step copies files from job workspace to build artifacts directory, so that they can be accessed after job is completed", 
			"Questo passo copia i file dalla workspace del job alla directory degli artefatti di build, in modo che possano essere accessibili dopo il completamento del job");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"Questo step pubblica i file specificati per essere serviti come sito web del progetto. Il sito web del progetto può essere accessibile pubblicamente tramite <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>");
		m.put("This step pulls specified refs from remote", "Questo passaggio estrae i ref specificati dal remoto");
		m.put("This step pushes current commit to same ref on remote", "Questo step spinge il commit corrente allo stesso ref sul remoto");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"Questo step configura il cache di Renovate. Posizionalo prima dello step di Renovate se desideri utilizzarlo");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"Questo step configura il cache del database di trivy per velocizzare vari step dello scanner. Posizionalo prima degli step dello scanner se desideri utilizzarlo");
		m.put("This subscription key was already used", "Questa chiave di abbonamento è già stata utilizzata");
		m.put("This subscription key was expired", "Questa chiave di abbonamento è scaduta");
		m.put("This tab shows the pipeline containing current build. Check <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">this article</a> to understand how build pipeline works", 
			"Questa scheda mostra la pipeline contenente la build corrente. Consulta <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">questo articolo</a> per capire come funziona la pipeline di build");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Questo trigger sarà applicabile solo se il commit taggato è raggiungibile dai branch specificati qui. I branch multipli devono essere separati da spazi. Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>corrispondenza con caratteri jolly</a>. Prefissa con '-' per escludere. Lascia vuoto per corrispondere a tutti i branch");
		m.put("This user is authenticating via external system.", "Questo utente si sta autenticando tramite sistema esterno.");
		m.put("This user is authenticating via internal database.", "Questo utente si sta autenticando tramite database interno.");
		m.put("This user is currently authenticating via external system. Setting password will switch to use internal database", 
			"Questo utente si sta attualmente autenticando tramite sistema esterno. Impostare una password cambierà per utilizzare il database interno");
		m.put("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?", 
			"Questo disattiverà l'abbonamento corrente e tutte le funzionalità enterprise saranno disabilitate, vuoi continuare?");
		m.put("This will discard all project specific boards, do you want to continue?", "Questo scarterà tutte le bacheche specifiche del progetto, vuoi continuare?");
		m.put("This will restart SSH server. Do you want to continue?", "Questo riavvierà il server SSH. Vuoi continuare?");
		m.put("Threads", "Thread");
		m.put("Time Estimate Issue Field", "Campo Problema Stima Tempo");
		m.put("Time Range", "Intervallo di Tempo");
		m.put("Time Spent Issue Field", "Campo Problema Tempo Trascorso");
		m.put("Time Tracking", "Tracciamento del Tempo");
		m.put("Time Tracking Setting", "Impostazione Tracciamento del Tempo");
		m.put("Time Tracking Settings", "Impostazioni Tracciamento del Tempo");
		m.put("Time tracking settings have been saved", "Le impostazioni di tracciamento del tempo sono state salvate");
		m.put("Timed out", "Tempo scaduto");
		m.put("Timeout", "Timeout");
		m.put("Timesheet", "Foglio Presenze");
		m.put("Timesheet Setting", "Impostazione Foglio Presenze");
		m.put("Timesheets", "Fogli Presenze");
		m.put("Timing", "Temporizzazione");
		m.put("Title", "Titolo");
		m.put("To Everyone", "A Tutti");
		m.put("To State", "Allo Stato");
		m.put("To States", "Agli Stati");
		m.put("To authenticate via internal database, <a wicket:id=\"setPasswordForUser\">set password for user</a> or <a wicket:id=\"tellUserToResetPassword\">tell user to reset password</a>", 
			"Per autenticarsi tramite database interno, <a wicket:id=\"setPasswordForUser\">imposta una password per l'utente</a> o <a wicket:id=\"tellUserToResetPassword\">chiedi all'utente di reimpostare la password</a>");
		m.put("To avoid duplication, estimated/remaining time showing here does not include those aggregated from \"{0}\"", 
			"Per evitare duplicazioni, il tempo stimato/rimanente mostrato qui non include quelli aggregati da \"{0}\"");
		m.put("To avoid duplication, spent time showing here does not include those aggregated from \"{0}\"", 
			"Per evitare duplicazioni, il tempo trascorso mostrato qui non include quelli aggregati da \"{0}\"");
		m.put("Toggle change history", "Attiva/disattiva la cronologia delle modifiche");
		m.put("Toggle comments", "Attiva/disattiva i commenti");
		m.put("Toggle commits", "Attiva/disattiva i commit");
		m.put("Toggle dark mode", "Attiva/disattiva la modalità scura");
		m.put("Toggle detail message", "Attiva/disattiva il messaggio dettagliato");
		m.put("Toggle fixed width font", "Attiva/disattiva il font a larghezza fissa");
		m.put("Toggle full screen", "Attiva/disattiva lo schermo intero");
		m.put("Toggle matched contents", "Attiva/disattiva i contenuti corrispondenti");
		m.put("Toggle navigation", "Attiva/disattiva la navigazione");
		m.put("Toggle work log", "Attiva/disattiva il registro di lavoro");
		m.put("Tokens", "Token");
		m.put("Too many commits to load", "Troppi commit da caricare");
		m.put("Too many commits, displaying recent {0}", "Troppi commit, visualizzazione dei recenti {0}");
		m.put("Too many log entries, displaying recent {0}", "Troppi log, visualizzazione dei recenti {0}");
		m.put("Too many problems, displaying first {0}", "Troppi problemi, visualizzazione dei primi {0}");
		m.put("Toomanyrequests", "Troppe richieste");
		m.put("Top", "Top");
		m.put("Topo", "Topo");
		m.put("Total Heap Memory", "Memoria Heap Totale");
		m.put("Total Number", "Numero Totale");
		m.put("Total Problems", "Problemi Totali");
		m.put("Total Size", "Dimensione Totale");
		m.put("Total Test Duration", "Durata Totale dei Test");
		m.put("Total estimated time", "Tempo stimato totale");
		m.put("Total spent time", "Tempo totale trascorso");
		m.put("Total spent time / total estimated time", "Tempo totale trascorso / tempo stimato totale");
		m.put("Total time", "Tempo totale");
		m.put("Total:", "Totale:");
		m.put("Touched File", "File Modificato");
		m.put("Touched Files", "File Modificati");
		m.put("Transfer LFS Files", "Trasferisci File LFS");
		m.put("Transit manually", "Transito manuale");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "Stato del problema \"{0}\" transitato a \"{1}\" ({2})");
		m.put("Transition Edit Bean", "Modifica Bean di Transizione");
		m.put("Transition Spec", "Specifica di Transizione");
		m.put("Trial Expiration Date", "Data di Scadenza della Prova");
		m.put("Trial subscription key not applicable for this installation", "Chiave di abbonamento di prova non applicabile per questa installazione");
		m.put("Triggers", "Trigger");
		m.put("Trivy Container Image Scanner", "Scanner Immagine Contenitore Trivy");
		m.put("Trivy Filesystem Scanner", "Scanner File System Trivy");
		m.put("Trivy Rootfs Scanner", "Scanner Rootfs Trivy");
		m.put("Try EE", "Prova EE");
		m.put("Try Enterprise Edition", "Prova Enterprise Edition");
		m.put("Twitch", "Twitch");
		m.put("Two Factor Authentication", "Autenticazione a Due Fattori");
		m.put("Two-factor Authentication", "Autenticazione a due fattori");
		m.put("Two-factor authentication already set up. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Request to set up again", 
			"Autenticazione a due fattori già configurata. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Richiedi di configurare di nuovo");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"L'autenticazione a due fattori è abilitata. Inserisci il codice visualizzato sul tuo autenticatore TOTP. Se riscontri problemi, assicurati che l'orario del server OneDev e del tuo dispositivo con l'autenticatore TOTP siano sincronizzati");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"L'autenticazione a due fattori è obbligatoria per il tuo account per migliorare la sicurezza. Segui la procedura qui sotto per configurarla");
		m.put("Two-factor authentication is now configured", "L'autenticazione a due fattori è ora configurata");
		m.put("Two-factor authentication not enabled", "Autenticazione a due fattori non abilitata");
		m.put("Type", "Tipo");
		m.put("Type <code>yes</code> below to cancel all queried builds", "Digita <code>yes</code> qui sotto per annullare tutte le build interrogate");
		m.put("Type <code>yes</code> below to cancel selected builds", "Digita <code>yes</code> qui sotto per annullare le build selezionate");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "Digita <code>yes</code> qui sotto per confermare l'eliminazione di tutti gli utenti interrogati");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "Digita <code>yes</code> qui sotto per confermare l'eliminazione degli utenti selezionati");
		m.put("Type <code>yes</code> below to copy all queried issues to project \"{0}\"", "Digita <code>yes</code> qui sotto per copiare tutti i problemi interrogati nel progetto \"{0}\"");
		m.put("Type <code>yes</code> below to copy selected issues to project \"{0}\"", "Digita <code>yes</code> qui sotto per copiare i problemi selezionati nel progetto \"{0}\"");
		m.put("Type <code>yes</code> below to delete all queried builds", "Digita <code>yes</code> qui sotto per eliminare tutte le build interrogate");
		m.put("Type <code>yes</code> below to delete all queried comments", "Digita <code>yes</code> qui sotto per eliminare tutti i commenti interrogati");
		m.put("Type <code>yes</code> below to delete all queried issues", "Digita <code>yes</code> qui sotto per eliminare tutti i problemi interrogati");
		m.put("Type <code>yes</code> below to delete all queried packages", "Digita <code>yes</code> qui sotto per eliminare tutti i pacchetti interrogati");
		m.put("Type <code>yes</code> below to delete all queried projects", "Digita <code>yes</code> qui sotto per eliminare tutti i progetti interrogati");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "Digita <code>yes</code> qui sotto per eliminare tutte le richieste di pull interrogate");
		m.put("Type <code>yes</code> below to delete selected builds", "Digita <code>yes</code> qui sotto per eliminare le build selezionate");
		m.put("Type <code>yes</code> below to delete selected comments", "Digita <code>yes</code> qui sotto per eliminare i commenti selezionati");
		m.put("Type <code>yes</code> below to delete selected issues", "Digita <code>yes</code> qui sotto per eliminare i problemi selezionati");
		m.put("Type <code>yes</code> below to delete selected packages", "Digita <code>yes</code> qui sotto per eliminare i pacchetti selezionati");
		m.put("Type <code>yes</code> below to delete selected projects", "Digita <code>yes</code> qui sotto per eliminare i progetti selezionati");
		m.put("Type <code>yes</code> below to delete selected pull requests", "Digita <code>yes</code> qui sotto per eliminare le richieste di pull selezionate");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "Digita <code>yes</code> qui sotto per scartare tutte le richieste di pull interrogate");
		m.put("Type <code>yes</code> below to discard selected pull requests", "Digita <code>yes</code> qui sotto per scartare le richieste di pull selezionate");
		m.put("Type <code>yes</code> below to move all queried issues to project \"{0}\"", "Digita <code>yes</code> qui sotto per spostare tutti i problemi interrogati nel progetto \"{0}\"");
		m.put("Type <code>yes</code> below to move all queried projects to be under \"{0}\"", "Digita <code>yes</code> qui sotto per spostare tutti i progetti interrogati sotto \"{0}\"");
		m.put("Type <code>yes</code> below to move selected issues to project \"{0}\"", "Digita <code>yes</code> qui sotto per spostare i problemi selezionati nel progetto \"{0}\"");
		m.put("Type <code>yes</code> below to move selected projects to be under \"{0}\"", "Digita <code>yes</code> qui sotto per spostare i progetti selezionati sotto \"{0}\"");
		m.put("Type <code>yes</code> below to pause all queried agents", "Digita <code>yes</code> qui sotto per mettere in pausa tutti gli agenti interrogati");
		m.put("Type <code>yes</code> below to re-run all queried builds", "Digita <code>yes</code> qui sotto per rieseguire tutte le build interrogate");
		m.put("Type <code>yes</code> below to re-run selected builds", "Digita <code>yes</code> qui sotto per rieseguire le build selezionate");
		m.put("Type <code>yes</code> below to remove all queried users from group", "Digita <code>yes</code> qui sotto per rimuovere tutti gli utenti interrogati dal gruppo");
		m.put("Type <code>yes</code> below to remove from all queried groups", "Digita <code>yes</code> qui sotto per rimuovere da tutti i gruppi interrogati");
		m.put("Type <code>yes</code> below to remove from selected groups", "Digita <code>yes</code> qui sotto per rimuovere dai gruppi selezionati");
		m.put("Type <code>yes</code> below to remove selected users from group", "Digita <code>yes</code> qui sotto per rimuovere gli utenti selezionati dal gruppo");
		m.put("Type <code>yes</code> below to restart all queried agents", "Digita <code>yes</code> qui sotto per riavviare tutti gli agenti interrogati");
		m.put("Type <code>yes</code> below to restart selected agents", "Digita <code>yes</code> qui sotto per riavviare gli agenti selezionati");
		m.put("Type <code>yes</code> below to resume all queried agents", "Digita <code>yes</code> qui sotto per riprendere tutti gli agenti interrogati");
		m.put("Type <code>yes</code> below to set all queried as root projects", "Digita <code>yes</code> qui sotto per impostare tutti gli interrogati come progetti root");
		m.put("Type <code>yes</code> below to set selected as root projects", "Digita <code>yes</code> qui sotto per impostare i selezionati come progetti root");
		m.put("Type password here", "Digita la password qui");
		m.put("Type to filter", "Digita per filtrare");
		m.put("Type to filter...", "Digita per filtrare...");
		m.put("URL", "URL");
		m.put("Unable To Delete/Disable Right Now", "Impossibile Eliminare/Disabilitare Ora");
		m.put("Unable to apply change as otherwise you will not be able to manage this project", "Impossibile applicare la modifica poiché altrimenti non potrai gestire questo progetto");
		m.put("Unable to change password as you are authenticating via external system", "Impossibile cambiare la password poiché stai autenticandoti tramite un sistema esterno");
		m.put("Unable to comment", "Impossibile commentare");
		m.put("Unable to connect to server", "Impossibile connettersi al server");
		m.put("Unable to create protected branch", "Impossibile creare un branch protetto");
		m.put("Unable to create protected tag", "Impossibile creare un tag protetto");
		m.put("Unable to diff as some line is too long.", "Impossibile effettuare il diff poiché alcune righe sono troppo lunghe.");
		m.put("Unable to diff as the file is too large.", "Impossibile effettuare il diff poiché il file è troppo grande.");
		m.put("Unable to find SSO provider: ", "Impossibile trovare il provider SSO:");
		m.put("Unable to find agent {0}", "Impossibile trovare l'agente {0}");
		m.put("Unable to find build #{0} in project {1}", "Impossibile trovare la build #{0} nel progetto {1}");
		m.put("Unable to find commit to import build spec (import project: {0}, import revision: {1})", 
			"Impossibile trovare il commit per importare la specifica di build (progetto di importazione: {0}, revisione di importazione: {1})");
		m.put("Unable to find issue #{0} in project {1}", "Impossibile trovare il problema #{0} nel progetto {1}");
		m.put("Unable to find project to import build spec: {0}", "Impossibile trovare il progetto per importare la specifica di build: {0}");
		m.put("Unable to find pull request #{0} in project {1}", "Impossibile trovare la richiesta di pull #{0} nel progetto {1}");
		m.put("Unable to find timesheet: ", "Impossibile trovare il timesheet:");
		m.put("Unable to get guilds info", "Impossibile ottenere informazioni sulle gilde");
		m.put("Unable to import build spec (import project: {0}, import revision: {1}): {2}", "Impossibile importare la specifica di build (progetto di importazione: {0}, revisione di importazione: {1}): {2}");
		m.put("Unable to notify user as mail service is not configured", "Impossibile notificare l'utente poiché il servizio di posta non è configurato");
		m.put("Unable to send password reset email as mail service is not configured", "Impossibile inviare email di reimpostazione password poiché il servizio di posta non è configurato");
		m.put("Unable to send verification email as mail service is not configured yet", "Impossibile inviare l'email di verifica poiché il servizio di posta non è ancora configurato");
		m.put("Unauthorize this user", "Revoca autorizzazione a questo utente");
		m.put("Unauthorized", "Non autorizzato");
		m.put("Undefined", "Non definito");
		m.put("Undefined Field Resolution", "Risoluzione Campo Non Definito");
		m.put("Undefined Field Value Resolution", "Risoluzione Valore Campo Non Definito");
		m.put("Undefined State Resolution", "Risoluzione dello stato indefinito");
		m.put("Undefined custom field: ", "Campo personalizzato indefinito:");
		m.put("Under which condition this step should run. <b>SUCCESSFUL</b> means all non-optional steps running before this step are successful", 
			"Sotto quale condizione questo passo dovrebbe essere eseguito. <b>SUCCESSFUL</b> significa che tutti i passi non opzionali eseguiti prima di questo passo sono riusciti");
		m.put("Unexpected setting: {0}", "Impostazione imprevista: {0}");
		m.put("Unexpected ssh signature hash algorithm: ", "Algoritmo di hash della firma ssh inaspettato:");
		m.put("Unexpected ssh signature namespace: ", "Namespace della firma ssh inaspettato:");
		m.put("Unified", "Unificato");
		m.put("Unified view", "Vista unificata");
		m.put("Unit Test Statistics", "Statistiche dei test unitari");
		m.put("Unlimited", "Illimitato");
		m.put("Unlink this issue", "Rimuovi il collegamento a questo problema");
		m.put("Unordered List", "Elenco non ordinato");
		m.put("Unordered list", "Elenco non ordinato");
		m.put("Unpin this issue", "Rimuovi il pin da questo problema");
		m.put("Unresolved", "Non risolto");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "Commento non risolto sul file \"{0}\" nel progetto \"{1}\"");
		m.put("Unscheduled", "Non programmato");
		m.put("Unscheduled Issues", "Problemi non programmati");
		m.put("Unsolicited OIDC authentication response", "Risposta di autenticazione OIDC non richiesta");
		m.put("Unsolicited OIDC response", "Risposta OIDC non richiesta");
		m.put("Unsolicited discord api response", "Risposta API Discord non richiesta");
		m.put("Unspecified", "Non specificato");
		m.put("Unsupported", "Non supportato");
		m.put("Unsupported ssh signature algorithm: ", "Algoritmo di firma ssh non supportato:");
		m.put("Unsupported ssh signature version: ", "Versione della firma ssh non supportata:");
		m.put("Unverified", "Non verificato");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "L'indirizzo email non verificato <b>NON</b> è applicabile per le funzionalità sopra");
		m.put("Unvote", "Rimuovi voto");
		m.put("Unwatched. Click to watch", "Non osservato. Clicca per osservare");
		m.put("Update", "Aggiorna");
		m.put("Update Dependencies via Renovate", "Aggiorna dipendenze tramite Renovate");
		m.put("Update Source Branch", "Aggiorna il branch sorgente");
		m.put("Update body", "Aggiorna il corpo");
		m.put("Upload", "Carica");
		m.put("Upload Access Token Secret", "Carica il segreto del token di accesso");
		m.put("Upload Cache", "Carica la cache");
		m.put("Upload Files", "Carica file");
		m.put("Upload Project Path", "Carica il percorso del progetto");
		m.put("Upload Strategy", "Strategia di caricamento");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "Carica un file PNG trasparente 128x128 da utilizzare come logo per la modalità scura");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "Carica un file PNG trasparente 128x128 da utilizzare come logo per la modalità chiara");
		m.put("Upload artifacts", "Carica artefatti");
		m.put("Upload avatar", "Carica avatar");
		m.put("Upload should be less than {0} Mb", "Il caricamento deve essere inferiore a {0} Mb");
		m.put("Upload to Project", "Carica nel progetto");
		m.put("Uploaded Caches", "Cache caricate");
		m.put("Uploading file", "Caricamento file");
		m.put("Url", "Url");
		m.put("Use '*' for wildcard match", "Usa '*' per la corrispondenza wildcard");
		m.put("Use '*' or '?' for wildcard match. Prefix with '-' to exclude", "Usa '*' o '?' per la corrispondenza wildcard. Prefisso con '-' per escludere");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza wildcard del percorso</a>");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Usa '**', '*' o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza wildcard del percorso</a>. Prefisso con '-' per escludere");
		m.put("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Usa '**', '*', o '?' per <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la corrispondenza wildcard del percorso</a>");
		m.put("Use '\\' to escape brackets", "Usa '\\' per escludere parentesi");
		m.put("Use '\\' to escape quotes", "Usa '\\' per escludere virgolette");
		m.put("Use @@ to reference scope in job commands to avoid being interpreted as variable", "Usa @@ per fare riferimento all'ambito nei comandi di lavoro per evitare che venga interpretato come variabile");
		m.put("Use Avatar Service", "Usa il servizio Avatar");
		m.put("Use Default", "Usa predefinito");
		m.put("Use Default Boards", "Usa le bacheche predefinite");
		m.put("Use For Git Operations", "Usa per operazioni Git");
		m.put("Use Git in System Path", "Usa Git nel percorso di sistema");
		m.put("Use Hours And Minutes Only", "Usa solo ore e minuti");
		m.put("Use Specified Git", "Usa Git specificato");
		m.put("Use Specified curl", "Usa curl specificato");
		m.put("Use Step Template", "Usa il modello di passaggio");
		m.put("Use curl in System Path", "Usa curl nel percorso di sistema");
		m.put("Use default", "Usa predefinito");
		m.put("Use default storage class", "Usa la classe di archiviazione predefinita");
		m.put("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages", 
			"Usa il token di lavoro come nome utente in modo che OneDev possa sapere quale build sta ${permission.equals(\"write\")? \"distribuendo\": \"utilizzando\"} i pacchetti");
		m.put("Use job token to tell OneDev the build publishing the package", "Usa il token di lavoro per dire a OneDev la build che pubblica il pacchetto");
		m.put("Use job token to tell OneDev the build pushing the chart", "Usa il token di lavoro per dire a OneDev la build che spinge il grafico");
		m.put("Use job token to tell OneDev the build pushing the package", "Usa il token di lavoro per dire a OneDev la build che spinge il pacchetto");
		m.put("Use job token to tell OneDev the build using the package", "Usa il token di lavoro per dire a OneDev la build che utilizza il pacchetto");
		m.put("Use project dependency to retrieve artifacts from other projects", "Usa la dipendenza del progetto per recuperare artefatti da altri progetti");
		m.put("Use specified choices", "Usa le scelte specificate");
		m.put("Use specified default value", "Usa il valore predefinito specificato");
		m.put("Use specified value or job secret", "Usa il valore specificato o il segreto del lavoro");
		m.put("Use specified values or job secrets", "Usa i valori specificati o i segreti del lavoro");
		m.put("Use triggers to run the job automatically under certain conditions", "Usa i trigger per eseguire automaticamente il lavoro in determinate condizioni");
		m.put("Use value of specified parameter/secret", "Usa il valore del parametro/segreto specificato");
		m.put("Used Heap Memory", "Memoria heap utilizzata");
		m.put("User", "Utente");
		m.put("User \"{0}\" unauthorized", "Utente \"{0}\" non autorizzato");
		m.put("User Authorization Bean", "Bean di autorizzazione utente");
		m.put("User Authorizations", "Autorizzazioni utente");
		m.put("User Authorizations Bean", "Bean di autorizzazioni utente");
		m.put("User Count", "Conteggio utenti");
		m.put("User Email Attribute", "Attributo email utente");
		m.put("User Full Name Attribute", "Attributo nome completo utente");
		m.put("User Groups Attribute", "Attributo gruppi utente");
		m.put("User Invitation", "Invito utente");
		m.put("User Invitation Template", "Modello di invito utente");
		m.put("User Management", "Gestione utenti");
		m.put("User Match Criteria", "Criteri di corrispondenza utente");
		m.put("User Name", "Nome utente");
		m.put("User Principal Name", "Nome principale utente");
		m.put("User Profile", "Profilo utente");
		m.put("User SSH Key Attribute", "Attributo chiave SSH utente");
		m.put("User Search Bases", "Basi di ricerca utente");
		m.put("User Search Filter", "Filtro di ricerca utente");
		m.put("User added to group", "Utente aggiunto al gruppo");
		m.put("User authorizations updated", "Autorizzazioni utente aggiornate");
		m.put("User authorized", "Utente autorizzato");
		m.put("User avatar will be requested by appending a hash to this url", "L'avatar utente sarà richiesto aggiungendo un hash a questo url");
		m.put("User can sign up if this option is enabled", "L'utente può registrarsi se questa opzione è abilitata");
		m.put("User disabled", "Utente disabilitato");
		m.put("User name", "Nome utente");
		m.put("User name already used by another account", "Nome utente già utilizzato da un altro account");
		m.put("Users", "Utenti");
		m.put("Users converted to service accounts successfully", "Utenti convertiti in account di servizio con successo");
		m.put("Users deleted successfully", "Utenti eliminati con successo");
		m.put("Users disabled successfully", "Utenti disabilitati con successo");
		m.put("Users enabled successfully", "Utenti abilitati con successo");
		m.put("Utilities", "Utilità");
		m.put("Valid signature required for head commit of this branch per branch protection rule", 
			"Firma valida richiesta per il commit principale di questo branch secondo la regola di protezione del branch");
		m.put("Value", "Valore");
		m.put("Value Matcher", "Matcher di valore");
		m.put("Value Provider", "Provider di valore");
		m.put("Values", "Valori");
		m.put("Values Provider", "Provider di valori");
		m.put("Variable", "Variabile");
		m.put("Verification Code", "Codice di verifica");
		m.put("Verification email sent, please check it", "Email di verifica inviata, per favore controlla");
		m.put("Verify", "Verifica");
		m.put("View", "Visualizza");
		m.put("View Source", "Visualizza sorgente");
		m.put("View source", "Visualizza sorgente");
		m.put("View statistics", "Visualizza statistiche");
		m.put("Viewer", "Visualizzatore");
		m.put("Volume Mount", "Montaggio volume");
		m.put("Volume Mounts", "Montaggi volume");
		m.put("Vote", "Vota");
		m.put("Votes", "Voti");
		m.put("WAITING", "IN ATTESA");
		m.put("WARNING:", "AVVERTIMENTO:");
		m.put("Waiting", "In attesa");
		m.put("Waiting for approvals", "In attesa di approvazioni");
		m.put("Waiting for test mail to come back...", "In attesa che torni l'email di test...");
		m.put("Watch", "Osserva");
		m.put("Watch Status", "Stato di osservazione");
		m.put("Watch if involved", "Osserva se coinvolto");
		m.put("Watch if involved (default)", "Osserva se coinvolto (predefinito)");
		m.put("Watch status changed", "Stato di osservazione modificato");
		m.put("Watch/Unwatch All Queried Issues", "Osserva/Non osservare tutte le problematiche interrogate");
		m.put("Watch/Unwatch All Queried Pull Requests", "Osserva/Non osservare tutte le richieste di pull interrogate");
		m.put("Watch/Unwatch Selected Pull Requests", "Osserva/Non osservare le richieste di pull selezionate");
		m.put("Watched. Click to unwatch", "Osservato. Clicca per non osservare");
		m.put("Watchers", "Osservatori");
		m.put("Web Hook", "Web Hook");
		m.put("Web Hooks", "Web Hooks");
		m.put("Web Hooks Bean", "Bean di Web Hooks");
		m.put("Web hooks saved", "Web hooks salvati");
		m.put("Webhook Url", "URL del Webhook");
		m.put("Week", "Settimana");
		m.put("When", "Quando");
		m.put("When authorize a group, the group will also be authorized with the role for all child projects", 
			"Quando si autorizza un gruppo, il gruppo sarà anche autorizzato con il ruolo per tutti i progetti figli");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"Quando si autorizza un progetto, tutti i progetti figli saranno anche autorizzati con i ruoli assegnati");
		m.put("When authorize a user, the user will also be authorized with the role for all child projects", 
			"Quando si autorizza un utente, l'utente sarà anche autorizzato con il ruolo per tutti i progetti figli");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"Quando si determina se l'utente è autore/committente di un commit git, tutte le email elencate qui saranno verificate");
		m.put("When evaluating this template, below variables will be available:", "Quando si valuta questo template, le variabili sottostanti saranno disponibili:");
		m.put("When login via OneDev's built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database", 
			"Quando si accede tramite il modulo integrato di OneDev, le credenziali utente inviate possono essere verificate contro l'autenticatore definito qui, oltre al database interno");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"Quando il branch di destinazione di una richiesta di pull ha nuovi commit, il commit di merge della richiesta di pull sarà ricalcolato, e questa opzione indica se accettare o meno le build della richiesta di pull eseguite sul commit di merge precedente. Se abilitato, sarà necessario rieseguire le build richieste sul nuovo commit di merge. Questa impostazione ha effetto solo quando sono specificate build richieste");
		m.put("When this work starts", "Quando inizia questo lavoro");
		m.put("When {0}", "Quando {0}");
		m.put("Whether or not created issue should be confidential", "Se il problema creato deve essere confidenziale o meno");
		m.put("Whether or not multiple issues can be linked", "Se è possibile collegare più problemi o meno");
		m.put("Whether or not multiple issues can be linked on the other side. For instance sub issues on the other side means parent issue, and multiple should be false on that side if only one parent is allowed", 
			"Se è possibile collegare più problemi dall'altro lato o meno. Ad esempio, i sotto-problemi dall'altro lato significano problema genitore, e multiplo dovrebbe essere falso da quel lato se è consentito solo un genitore");
		m.put("Whether or not multiple values can be specified for this field", "Se è possibile specificare più valori per questo campo o meno");
		m.put("Whether or not multiple values can be specified for this param", "Se è possibile specificare più valori per questo parametro o meno");
		m.put("Whether or not the issue should be confidential", "Se il problema deve essere confidenziale o meno");
		m.put("Whether or not the link is asymmetric. A asymmetric link has different meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric", 
			"Se il collegamento è asimmetrico o meno. Un collegamento asimmetrico ha significati diversi da lati diversi. Ad esempio, un collegamento 'genitore-figlio' è asimmetrico, mentre un collegamento 'relativo a' è simmetrico");
		m.put("Whether or not this field accepts empty value", "Se questo campo accetta valori vuoti o meno");
		m.put("Whether or not this param accepts empty value", "Se questo parametro accetta valori vuoti o meno");
		m.put("Whether or not this script can be used in CI/CD jobs", "Se questo script può essere utilizzato nei lavori CI/CD o meno");
		m.put("Whether or not this step is optional. Execution failure of an optional step will not cause the build to fail, and successful condition of subsequent steps will not take optional step into account", 
			"Se questo passaggio è opzionale o meno. Il fallimento dell'esecuzione di un passaggio opzionale non causerà il fallimento della build, e la condizione di successo dei passaggi successivi non prenderà in considerazione il passaggio opzionale");
		m.put("Whether or not to allow anonymous users to access this server", "Se consentire o meno agli utenti anonimi di accedere a questo server");
		m.put("Whether or not to allow creating root projects (project without parent)", "Se consentire o meno la creazione di progetti radice (progetto senza genitore)");
		m.put("Whether or not to also include children of above projects", "Se includere o meno anche i figli dei progetti sopra");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same machine", 
			"Se sempre estrarre l'immagine quando si esegue il container o si costruiscono immagini o meno. Questa opzione dovrebbe essere abilitata per evitare che le immagini vengano sostituite da lavori dannosi eseguiti sulla stessa macchina");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same node", 
			"Se sempre estrarre l'immagine quando si esegue il container o si costruiscono immagini o meno. Questa opzione dovrebbe essere abilitata per evitare che le immagini vengano sostituite da lavori dannosi eseguiti sullo stesso nodo");
		m.put("Whether or not to be able to access time tracking info of issues", "Se essere in grado di accedere alle informazioni di tracciamento del tempo dei problemi o meno");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities", 
			"Se creare come account di servizio per scopi di automazione delle attività o meno. L'account di servizio non ha password e indirizzi email, e non genererà notifiche per le sue attività");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Se creare come account di servizio per scopi di automazione delle attività o meno. L'account di servizio non ha password e indirizzi email, e non genererà notifiche per le sue attività. <b class='text-warning'>NOTA:</b> L'account di servizio è una funzionalità aziendale. <a href='https://onedev.io/pricing' target='_blank'>Prova gratis</a> per 30 giorni");
		m.put("Whether or not to enable code management for the project", "Se abilitare la gestione del codice per il progetto o meno");
		m.put("Whether or not to enable issue management for the project", "Se abilitare la gestione dei problemi per il progetto o meno");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project.", 
			"Se recuperare oggetti LFS se la richiesta di pull è aperta da un progetto diverso o meno");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project. If this option is enabled, git lfs command needs to be installed on OneDev server", 
			"Se recuperare oggetti LFS se la richiesta di pull è aperta da un progetto diverso o meno. Se questa opzione è abilitata, il comando git lfs deve essere installato sul server OneDev");
		m.put("Whether or not to import forked Bitbucket repositories", "Se importare repository Bitbucket forkati o meno");
		m.put("Whether or not to import forked GitHub repositories", "Se importare repository GitHub forkati o meno");
		m.put("Whether or not to import forked GitLab projects", "Se importare progetti GitLab forkati o meno");
		m.put("Whether or not to import forked Gitea repositories", "Se importare repository Gitea forkati o meno");
		m.put("Whether or not to include forked repositories", "Se includere repository forkati o meno");
		m.put("Whether or not to include this field when issue is initially opened. If not, you may include this field later when issue is transited to other states via issue transition rule", 
			"Se includere questo campo quando il problema è inizialmente aperto o meno. In caso contrario, puoi includere questo campo successivamente quando il problema viene transitato ad altri stati tramite regola di transizione del problema");
		m.put("Whether or not to input and display estimated/spent time in hours/minutes only", "Se inserire e visualizzare il tempo stimato/speso solo in ore/minuti o meno");
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. Make sure this executor can only be used by trusted jobs if this option is enabled", 
			"Se montare o meno il socket docker nel container del lavoro per supportare le operazioni docker nei comandi del lavoro<br><b class='text-danger'>ATTENZIONE</b>: I lavori dannosi possono prendere il controllo dell'intero OneDev operando sul socket docker montato. Assicurati che questo executor possa essere utilizzato solo da lavori fidati se questa opzione è abilitata");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"Se pre-popolare le mappature dei tag nella pagina successiva o meno. Potresti voler disabilitare questo se ci sono troppi tag da visualizzare");
		m.put("Whether or not to require this dependency to be successful", "Se richiedere che questa dipendenza abbia successo o meno");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"Se recuperare i gruppi dell'utente di login o meno. Assicurati di aggiungere il claim dei gruppi tramite la configurazione del token dell'app registrata in Entra ID se questa opzione è abilitata. Il claim dei gruppi dovrebbe restituire l'id del gruppo (l'opzione predefinita) tramite vari tipi di token in questo caso");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"Se recuperare i sottomoduli o meno. Consulta <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>questo tutorial</a> su come configurare le credenziali di clone sopra per recuperare i sottomoduli");
		m.put("Whether or not to run this step inside container", "Se eseguire questo passaggio all'interno del container o meno");
		m.put("Whether or not to scan recursively in above paths", "Se eseguire la scansione ricorsiva nei percorsi sopra o meno");
		m.put("Whether or not to send notifications for events generated by yourself", "Se inviare notifiche per eventi generati da te stesso o meno");
		m.put("Whether or not to send notifications to issue watchers for this change", "Se inviare notifiche agli osservatori del problema per questa modifica o meno");
		m.put("Whether or not to show branch/tag column", "Se mostrare la colonna branch/tag o meno");
		m.put("Whether or not to show duration column", "Se mostrare la colonna durata o meno");
		m.put("Whether or not to use user avatar from a public service", "Se utilizzare l'avatar utente da un servizio pubblico o meno");
		m.put("Whether or not use force option to overwrite changes in case ref updating can not be fast-forwarded", 
			"Se utilizzare l'opzione forza per sovrascrivere le modifiche nel caso in cui l'aggiornamento del riferimento non possa essere avanzato rapidamente o meno");
		m.put("Whether or not user can remove own account", "Se l'utente può rimuovere il proprio account o meno");
		m.put("Whether the password must contain at least one lowercase letter", "Se la password deve contenere almeno una lettera minuscola");
		m.put("Whether the password must contain at least one number", "Se la password deve contenere almeno un numero");
		m.put("Whether the password must contain at least one special character", "Se la password deve contenere almeno un carattere speciale");
		m.put("Whether the password must contain at least one uppercase letter", "Se la password deve contenere almeno una lettera maiuscola");
		m.put("Whole Word", "Parola Intera");
		m.put("Widget", "Widget");
		m.put("Widget Tab", "Scheda Widget");
		m.put("Widget Timesheet Setting", "Impostazione del Foglio Orario Widget");
		m.put("Will be prompted to set up two-factor authentication upon next login", "Sarà richiesto di configurare l'autenticazione a due fattori al prossimo accesso");
		m.put("Will be transcoded to UTF-8", "Sarà transcodificato in UTF-8");
		m.put("Window", "Finestra");
		m.put("Window Memory", "Memoria Finestra");
		m.put("With current number of users ({0}), the subscription will be active until <b>{1}</b>", 
			"Con il numero attuale di utenti ({0}), l'abbonamento sarà attivo fino al <b>{1}</b>");
		m.put("Workflow reconciliation completed", "Riconciliazione del flusso di lavoro completata");
		m.put("Working Directory", "Directory di Lavoro");
		m.put("Write", "Scrivi");
		m.put("YAML", "YAML");
		m.put("Yes", "Sì");
		m.put("You are not member of discord server", "Non sei membro del server Discord");
		m.put("You are rebasing source branch on top of target branch", "Stai effettuando il rebase del branch sorgente sopra il branch di destinazione");
		m.put("You are viewing a subset of all changes. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">show all changes</a>", 
			"Stai visualizzando un sottoinsieme di tutte le modifiche. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">mostra tutte le modifiche</a>");
		m.put("You can also achieve this by adding a build docker image step to your CI/CD job and configuring the built-in registry login with an access token secret that has package write permissions", 
			"Puoi anche ottenere questo aggiungendo uno step di creazione dell'immagine Docker al tuo lavoro CI/CD e configurando il login al registro integrato con un token di accesso segreto che ha permessi di scrittura sui pacchetti");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "Hai indirizzi email <a wicket:id=\"hasUnverifiedLink\">non verificati</a>");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "Puoi anche trascinare file/immagini nella casella di input o incollare immagini dalla clipboard");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"Puoi inizializzare il progetto <a wicket:id=\"addFiles\" class=\"link-primary\">aggiungendo file</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">configurando le specifiche di build</a>, o <a wicket:id=\"pushInstructions\" class=\"link-primary\">spingendo un repository esistente</a>");
		m.put("You selected to delete branch \"{0}\"", "Hai selezionato di eliminare il branch \"{0}\"");
		m.put("You will be notified of any activities", "Sarai notificato di qualsiasi attività");
		m.put("You've been logged out", "Sei stato disconnesso");
		m.put("YouTrack API URL", "URL API di YouTrack");
		m.put("YouTrack Issue Field", "Campo Problema di YouTrack");
		m.put("YouTrack Issue Link", "Collegamento Problema di YouTrack");
		m.put("YouTrack Issue State", "Stato Problema di YouTrack");
		m.put("YouTrack Issue Tag", "Tag Problema di YouTrack");
		m.put("YouTrack Login Name", "Nome Login di YouTrack");
		m.put("YouTrack Password or Access Token", "Password o Token di Accesso di YouTrack");
		m.put("YouTrack Project", "Progetto di YouTrack");
		m.put("YouTrack Projects to Import", "Progetti di YouTrack da Importare");
		m.put("Your email address is now verified", "Il tuo indirizzo email è ora verificato");
		m.put("Your primary email address is not verified", "Il tuo indirizzo email principale non è verificato");
		m.put("[Any state]", "[Qualsiasi stato]");
		m.put("[Reset Password] Please Reset Your OneDev Password", "[Reset Password] Per favore reimposta la tua password di OneDev");
		m.put("a boolean indiciating whether or not topic comment can be created directly by replying the email", 
			"un booleano che indica se il commento del topic può essere creato direttamente rispondendo all'email");
		m.put("a new agent token will be generated each time this button is pressed", "un nuovo token agente sarà generato ogni volta che questo pulsante viene premuto");
		m.put("a string representing body of the event. May be <code>null</code>", "una stringa che rappresenta il corpo dell'evento. Può essere <code>null</code>");
		m.put("a string representing event detail url", "una stringa che rappresenta l'URL dei dettagli dell'evento");
		m.put("a string representing summary of the event", "una stringa che rappresenta il sommario dell'evento");
		m.put("access [{0}]", "accesso [{0}]");
		m.put("active", "attivo");
		m.put("add another order", "aggiungi un altro ordine");
		m.put("adding .onedev-buildspec.yml", "aggiungendo .onedev-buildspec.yml");
		m.put("after specified date", "dopo la data specificata");
		m.put("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information.  A <code>null</code> value means that the notification can not be unsubscribed", 
			"un <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>oggetto</a> che contiene informazioni di disiscrizione. Un valore <code>null</code> significa che la notifica non può essere disiscritta");
		m.put("and more", "e altro");
		m.put("archived", "archiviato");
		m.put("artifacts", "artefatti");
		m.put("assign to me", "assegna a me");
		m.put("authored by", "autore di");
		m.put("backlog ", "backlog");
		m.put("base", "base");
		m.put("before specified date", "prima della data specificata");
		m.put("branch the build commit is merged into", "branch in cui il commit di build è stato unito");
		m.put("branch the job is running against", "branch su cui il lavoro è in esecuzione");
		m.put("branch {0}", "branch {0}");
		m.put("branches", "branch");
		m.put("build", "build");
		m.put("build is successful for any job and branch", "la build è riuscita per qualsiasi lavoro e branch");
		m.put("build is successful for any job on branches \"{0}\"", "la build è riuscita per qualsiasi lavoro sui branch \"{0}\"");
		m.put("build is successful for jobs \"{0}\" on any branch", "la build è riuscita per i lavori \"{0}\" su qualsiasi branch");
		m.put("build is successful for jobs \"{0}\" on branches \"{1}\"", "la build è riuscita per i lavori \"{0}\" sui branch \"{1}\"");
		m.put("builds", "builds");
		m.put("cURL Example", "Esempio cURL");
		m.put("choose a color for this state", "scegli un colore per questo stato");
		m.put("cluster:lead", "lead");
		m.put("cmd-k to show command palette", "cmd-k per mostrare la palette dei comandi");
		m.put("code commit", "commit di codice");
		m.put("code is committed", "il codice è stato committato");
		m.put("code is committed to branches \"{0}\"", "il codice è stato commesso sui branch \"{0}\"");
		m.put("code is committed to branches \"{0}\" with message \"{1}\"", "il codice è stato committato nei rami \"{0}\" con il messaggio \"{1}\"");
		m.put("code is committed with message \"{0}\"", "il codice è stato committato con il messaggio \"{0}\"");
		m.put("commit message contains", "il messaggio del commit contiene");
		m.put("commits", "commit");
		m.put("committed by", "commesso da");
		m.put("common", "comune");
		m.put("common ancestor", "antenato comune");
		m.put("container:image", "Immagine");
		m.put("copy", "copia");
		m.put("ctrl-k to show command palette", "ctrl-k per mostrare la palette dei comandi");
		m.put("curl Command Line", "Comando Linea di curl");
		m.put("curl Path", "Percorso curl");
		m.put("default", "predefinito");
		m.put("descending", "discendente");
		m.put("disabled", "disabilitato");
		m.put("does not have any value of", "non ha alcun valore di");
		m.put("duration", "durata");
		m.put("enclose with ~ to query hash/message", "racchiudi con ~ per interrogare hash/messaggio");
		m.put("enclose with ~ to query job/version", "racchiudi con ~ per interrogare lavoro/versione");
		m.put("enclose with ~ to query name/ip/os", "racchiudi con ~ per interrogare nome/ip/os");
		m.put("enclose with ~ to query name/path", "racchiudi con ~ per interrogare nome/percorso");
		m.put("enclose with ~ to query name/version", "racchiudi con ~ per interrogare nome/versione");
		m.put("enclose with ~ to query path/content/reply", "racchiudi con ~ per interrogare percorso/contenuto/risposta");
		m.put("enclose with ~ to query title/description/comment", "racchiudi con ~ per interrogare titolo/descrizione/commento");
		m.put("exclude", "escludi");
		m.put("false", "false");
		m.put("files with ext \"{0}\"", "file con estensione \"{0}\"");
		m.put("find build by number", "trova build per numero");
		m.put("find build with this number", "trova build con questo numero");
		m.put("find issue by number", "trova problema per numero");
		m.put("find pull request by number", "trova richiesta di pull per numero");
		m.put("find pull request with this number", "trova richiesta di pull con questo numero");
		m.put("forked from <a wicket:id=\"forkedFrom\"></a>", "forkato da <a wicket:id=\"forkedFrom\"></a>");
		m.put("found 1 agent", "trovato 1 agente");
		m.put("found 1 build", "trovata 1 build");
		m.put("found 1 comment", "trovato 1 commento");
		m.put("found 1 issue", "trovato 1 problema");
		m.put("found 1 package", "trovato 1 pacchetto");
		m.put("found 1 project", "trovato 1 progetto");
		m.put("found 1 pull request", "trovata 1 pull request");
		m.put("found 1 user", "trovato 1 utente");
		m.put("found {0} agents", "trovati {0} agenti");
		m.put("found {0} builds", "trovate {0} build");
		m.put("found {0} comments", "trovati {0} commenti");
		m.put("found {0} issues", "trovati {0} problemi");
		m.put("found {0} packages", "trovati {0} pacchetti");
		m.put("found {0} projects", "trovati {0} progetti");
		m.put("found {0} pull requests", "trovate {0} pull request");
		m.put("found {0} users", "trovati {0} utenti");
		m.put("has any value of", "ha qualsiasi valore di");
		m.put("head", "testa");
		m.put("in current commit", "nel commit corrente");
		m.put("ineffective", "inefficace");
		m.put("inherited", "ereditato");
		m.put("initial", "iniziale");
		m.put("is empty", "è vuoto");
		m.put("is not empty", "non è vuoto");
		m.put("issue", "problema");
		m.put("issue:Number", "Numero");
		m.put("issues", "problemi");
		m.put("job", "lavoro");
		m.put("key ID: ", "ID chiave:");
		m.put("lines", "righe");
		m.put("link:Multiple", "Multiplo");
		m.put("log", "log");
		m.put("manage job", "gestisci lavoro");
		m.put("markdown:heading", "Intestazione");
		m.put("markdown:image", "Immagine");
		m.put("may not be empty", "non può essere vuoto");
		m.put("merged", "unito");
		m.put("month:Apr", "Apr");
		m.put("month:Aug", "Ago");
		m.put("month:Dec", "Dic");
		m.put("month:Feb", "Feb");
		m.put("month:Jan", "Gen");
		m.put("month:Jul", "Lug");
		m.put("month:Jun", "Giu");
		m.put("month:Mar", "Mar");
		m.put("month:May", "Mag");
		m.put("month:Nov", "Nov");
		m.put("month:Oct", "Ott");
		m.put("month:Sep", "Set");
		m.put("n/a", "n/d");
		m.put("new field", "nuovo campo");
		m.put("no activity for {0} days", "nessuna attività per {0} giorni");
		m.put("on file {0}", "nel file {0}");
		m.put("opened", "aperto");
		m.put("opened <span wicket:id=\"submitDate\"></span>", "aperto <span wicket:id=\"submitDate\"></span>");
		m.put("or match another value", "o corrisponde a un altro valore");
		m.put("order more", "ordina di più");
		m.put("outdated", "obsoleto");
		m.put("pack", "pacchetto");
		m.put("package", "pacchetto");
		m.put("packages", "pacchetti");
		m.put("personal", "personale");
		m.put("pipeline", "pipeline");
		m.put("project of the running job", "progetto del lavoro in esecuzione");
		m.put("property", "proprietà");
		m.put("pull request", "pull request");
		m.put("pull request #{0}", "pull request #{0}");
		m.put("pull request and code review", "pull request e revisione del codice");
		m.put("pull request to any branch is discarded", "la pull request su qualsiasi branch è scartata");
		m.put("pull request to any branch is merged", "la pull request su qualsiasi branch è unita");
		m.put("pull request to any branch is opened", "la pull request su qualsiasi branch è aperta");
		m.put("pull request to branches \"{0}\" is discarded", "la pull request sui branch \"{0}\" è scartata");
		m.put("pull request to branches \"{0}\" is merged", "la pull request sui branch \"{0}\" è unita");
		m.put("pull request to branches \"{0}\" is opened", "la pull request sui branch \"{0}\" è aperta");
		m.put("pull requests", "pull request");
		m.put("reconciliation (need administrator permission)", "riconciliazione (richiede permessi di amministratore)");
		m.put("reports", "report");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> object to be notified", 
			"rappresenta l'oggetto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> da notificare");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk", 
			"rappresenta il <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> aperto tramite il service desk");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified", 
			"rappresenta l'oggetto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> da notificare");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> object to be notified", 
			"rappresenta l'oggetto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>pacchetto</a> da notificare");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified", 
			"rappresenta l'oggetto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> da notificare");
		m.put("represents the <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> object to be notified", 
			"rappresenta l'oggetto <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> da notificare");
		m.put("represents the exception encountered when open issue via service desk", "rappresenta l'eccezione incontrata durante l'apertura del problema tramite il service desk");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>", 
			"rappresenta il <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> non sottoscritto");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>", 
			"rappresenta la <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> non sottoscritta");
		m.put("request to change", "richiesta di modifica");
		m.put("root", "radice");
		m.put("root url of OneDev server", "url radice del server OneDev");
		m.put("run job", "esegui lavoro");
		m.put("search in this revision will be accurate after indexed", "la ricerca in questa revisione sarà accurata dopo l'indicizzazione");
		m.put("service", "servizio");
		m.put("severity:CRITICAL", "Critico");
		m.put("severity:HIGH", "Alto");
		m.put("severity:LOW", "Basso");
		m.put("severity:MEDIUM", "Medio");
		m.put("skipped {0} lines", "saltate {0} righe");
		m.put("space", "spazio");
		m.put("state of an issue is transited", "lo stato di un problema è transitato");
		m.put("step template", "modello di step");
		m.put("submit", "invia");
		m.put("tag the job is running against", "tag contro cui il lavoro è in esecuzione");
		m.put("tag {0}", "tag {0}");
		m.put("tags", "tag");
		m.put("the url to set up user account", "l'URL per configurare l'account utente");
		m.put("time aggregation link", "link di aggregazione temporale");
		m.put("touching specified path", "toccare il percorso specificato");
		m.put("transit manually by any user", "transitare manualmente da qualsiasi utente");
		m.put("transit manually by any user of roles \"{0}\"", "transitare manualmente da qualsiasi utente con ruoli \"{0}\"");
		m.put("true", "vero");
		m.put("true for html version, false for text version", "vero per la versione HTML, falso per la versione testo");
		m.put("up to date", "aggiornato");
		m.put("url following which to verify email address", "URL da seguire per verificare l'indirizzo email");
		m.put("url to reset password", "URL per reimpostare la password");
		m.put("value needs to be enclosed in brackets", "il valore deve essere racchiuso tra parentesi quadre");
		m.put("value needs to be enclosed in parenthesis", "il valore deve essere racchiuso tra parentesi tonde");
		m.put("value should be quoted", "il valore deve essere racchiuso tra virgolette");
		m.put("w%02d", "w%02d");
		m.put("week:Fri", "Ven");
		m.put("week:Mon", "Lun");
		m.put("week:Sat", "Sab");
		m.put("week:Sun", "Dom");
		m.put("week:Thu", "Gio");
		m.put("week:Tue", "Mar");
		m.put("week:Wed", "Mer");
		m.put("widget:Tabs", "Tab");
		m.put("you may show this page later via incompatibilities link in help menu", "puoi mostrare questa pagina più tardi tramite il link delle incompatibilità nel menu di aiuto");
		m.put("{0} Month(s)", "{0} Mese/i");
		m.put("{0} activities on {1}", "{0} attività su {1}");
		m.put("{0} additions & {1} deletions", "{0} aggiunte & {1} eliminazioni");
		m.put("{0} ahead", "{0} avanti");
		m.put("{0} behind", "{0} indietro");
		m.put("{0} branches", "{0} branch");
		m.put("{0} build(s)", "{0} build");
		m.put("{0} child projects", "{0} progetti figli");
		m.put("{0} commits", "{0} commit");
		m.put("{0} commits ahead of base branch", "{0} commit avanti rispetto al branch base");
		m.put("{0} commits behind of base branch", "{0} commit indietro rispetto al branch base");
		m.put("{0} day", "{0} giorno");
		m.put("{0} days", "{0} giorni");
		m.put("{0} edited {1}", "{0} modificato {1}");
		m.put("{0} files", "{0} file");
		m.put("{0} forks", "{0} fork");
		m.put("{0} hour", "{0} ora");
		m.put("{0} hours", "{0} ore");
		m.put("{0} inaccessible activities", "{0} attività inaccessibili");
		m.put("{0} minute", "{0} minuto");
		m.put("{0} minutes", "{0} minuti");
		m.put("{0} reviewed", "{0} revisionato");
		m.put("{0} second", "{0} secondo");
		m.put("{0} seconds", "{0} secondi");
		m.put("{0} tags", "{0} tag");
		m.put("{0}d", "{0}g");
		m.put("{0}h", "{0}o");
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
