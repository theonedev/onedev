package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_fr extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init(m);
		Translation.watchUpdate(Translation_fr.class, () -> {
			init(m);
		});
	}

	@SystemPrompt("You are good at translating from English to French in DevOps software area.")
	public static void init(Map<String, String> m) {
		m.clear();
		m.put(" Project path can be omitted if reference from current project", "Le chemin du projet peut être omis si référencé depuis le projet actuel");
		m.put("'..' is not allowed in the directory", "'..' n'est pas autorisé dans le répertoire");
		m.put("(* = any string, ? = any character)", "(* = toute chaîne, ? = tout caractère)");
		m.put("(on behalf of <b>{0}</b>)", "(au nom de <b>{0}</b>)");
		m.put("** Enterprise edition is disabled as the subscription was expired. Renew to enable **", 
			"** L'édition Enterprise est désactivée car l'abonnement a expiré. Renouvelez pour activer **");
		m.put("** Enterprise edition is disabled as the trial subscription was expired, order subscription to enable or contact support@onedev.io if you need to extend your trial **", 
			"** L'édition Enterprise est désactivée car l'abonnement d'essai a expiré, commandez un abonnement pour l'activer ou contactez support@onedev.io si vous avez besoin de prolonger votre essai **");
		m.put("** Enterprise edition is disabled as there is no remaining user months. Order more to enable **", 
			"** L'édition Enterprise est désactivée car il n'y a plus de mois utilisateur restants. Commandez-en davantage pour l'activer **");
		m.put("1. To use this package, add below to project pom.xml", "1. Pour utiliser ce package, ajoutez ce qui suit au fichier pom.xml du projet");
		m.put("1. Use below repositories in project pom.xml", "1. Utilisez les dépôts ci-dessous dans le fichier pom.xml du projet");
		m.put("1w 1d 1h 1m", "1w 1d 1h 1m");
		m.put("2. Add below to <code>$HOME/.m2/settings.xml</code> if you want to deploy from command line", 
			"2. Ajoutez ce qui suit à <code>$HOME/.m2/settings.xml</code> si vous souhaitez déployer depuis la ligne de commande");
		m.put("2. Also add below to $HOME/.m2/settings.xml if you want to compile project from command line", 
			"2. Ajoutez également ce qui suit à $HOME/.m2/settings.xml si vous souhaitez compiler le projet depuis la ligne de commande");
		m.put("3. For CI/CD job, it is more convenient to use a custom settings.xml, for instance via below code in a command step:", 
			"3. Pour les tâches CI/CD, il est plus pratique d'utiliser un fichier settings.xml personnalisé, par exemple via le code ci-dessous dans une étape de commande :");
		m.put("6-digits passcode", "Code d'accès à 6 chiffres");
		m.put("7 days", "7 jours");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">utilisateur</a> pour réinitialiser le mot de passe");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">utilisateur</a> pour vérifier l'email");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">Markdown au format GitHub</a> est accepté, avec <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">support mermaid et katex</a>.");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>objet événement</a> déclenchant la notification");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alerte</a> à afficher");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a> overdue", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Chronomètre</a> en retard");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> a commis <span wicket:id=\"date\"></span>");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed with <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> a commis avec <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>");
		m.put("<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depends on me", "<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> dépend de moi");
		m.put("<a wicket:id=\"removePassword\">Remove password</a> to force the user to authenticate via external system", 
			"<a wicket:id=\"removePassword\">Supprimer le mot de passe</a> pour forcer l'utilisateur à s'authentifier via un système externe");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">Vérifier par code de récupération</a> si vous ne pouvez pas accéder à votre authentificateur TOTP");
		m.put("<b class='text-danger'>NOTE: </b> This requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTE : </b> Cela nécessite un abonnement Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTE : </b> Cette étape nécessite un abonnement Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTE : </b>L'intégration SendGrid est une fonctionnalité Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>NOTE : </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Le suivi du temps</a> est une fonctionnalité Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("<b>NOTE: </b> Service desk only takes effect if <a wicket:id=\"mailConnector\">mail service</a> is defined and its <tt>check incoming email</tt> option is enabled. Also <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>sub addressing</a> needs to be enabled for the system email address. Check <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>this tutorial</a> for details", 
			"<b>NOTE : </b> Le service desk ne prend effet que si <a wicket:id=\"mailConnector\">le service de messagerie</a> est défini et que son option <tt>vérifier les emails entrants</tt> est activée. De plus, <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>le sous-adressage</a> doit être activé pour l'adresse email système. Consultez <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>ce tutoriel</a> pour plus de détails");
		m.put("<b>NOTE:</b> Batch editing issues will not cause state transitions of other issues even if transition rule matches", 
			"<b>NOTE :</b> L'édition en lot des problèmes ne provoquera pas de transitions d'état d'autres problèmes même si la règle de transition correspond");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>Propriétaire du projet</b> est un rôle intégré avec une permission complète sur les projets");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>Conseils : </b> Tapez <tt>@</tt> pour <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insérer une variable</a>. Utilisez <tt>@@</tt> pour un <tt>@</tt> littéral");
		m.put("<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Rechercher des fichiers</span> <span class='font-size-sm text-muted'>dans la branche par défaut</span></div>");
		m.put("<div><span>Search Symbols</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Rechercher des symboles</span> <span class='font-size-sm text-muted'>dans la branche par défaut</span></div>");
		m.put("<div><span>Search Text</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Rechercher du texte</span> <span class='font-size-sm text-muted'>dans la branche par défaut</span></div>");
		m.put("<i>No Name</i>", "<i>Pas de nom</i>");
		m.put("<span class=\"keycap ml-3 mr-1\">esc</span> to close", "<span class=\"keycap ml-3 mr-1\">esc</span> pour fermer");
		m.put("<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> to move", 
			"<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> pour déplacer");
		m.put("<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> to navigate. <span class=\"keycap\">Esc</span> to close", 
			"<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> pour naviguer. <span class=\"keycap\">Esc</span> pour fermer");
		m.put("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete.", 
			"<span class='keycap'>Tab</span> ou <span class='keycap'>Entrée</span> pour compléter.");
		m.put("<span class='keycap'>Tab</span> to complete.", "<span class='keycap'>Tab</span> pour compléter.");
		m.put("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>", "<span class='text-nowrap'><span class='keycap'>Enter</span> pour aller</span>");
		m.put("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>", "<span class='text-nowrap'><span class='keycap'>Tab</span> pour rechercher</span>");
		m.put("<span wicket:id=\"activityCount\"></span> activities", "<span wicket:id=\"activityCount\"></span> activités");
		m.put("<svg class='icon mr-2'><use xlink:href='%s'/></svg> Define job secrets to be used in build spec. Secrets with <b>same name</b> can be defined. For a particular name, the first authorized secret with that name will be used (search in current project first, then search in parent projects). Note that secret value containing line breaks or less than <b>%d</b> characters will not be masked in build log", 
			"<svg class='icon mr-2'><use xlink:href='%s'/></svg> Définir les secrets de tâche à utiliser dans la spécification de build. Les secrets avec <b>le même nom</b> peuvent être définis. Pour un nom particulier, le premier secret autorisé avec ce nom sera utilisé (recherche dans le projet actuel d'abord, puis dans les projets parents). Notez que la valeur du secret contenant des sauts de ligne ou moins de <b>%d</b> caractères ne sera pas masquée dans le journal de build");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here", 
			"Un <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>modèle Java</a> est attendu ici");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"Une <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>expression régulière Java</a> pour valider le pied de page du message de commit");
		m.put("A child project with name \"{0}\" already exists under \"{1}\"", "Un projet enfant avec le nom \"{0}\" existe déjà sous \"{1}\"");
		m.put("A file exists where you’re trying to create a subdirectory. Choose a new path and try again..", 
			"Un fichier existe là où vous essayez de créer un sous-répertoire. Choisissez un nouveau chemin et réessayez.");
		m.put("A path with same name already exists.Please choose a different name and try again.", 
			"Un chemin avec le même nom existe déjà. Veuillez choisir un nom différent et réessayez.");
		m.put("A pull request is open for this change", "Une demande de tirage est ouverte pour ce changement");
		m.put("A root project with name \"{0}\" already exists", "Un projet racine avec le nom \"{0}\" existe déjà");
		m.put("A {0} used as body of address verification email", "Un {0} utilisé comme corps de l'email de vérification d'adresse");
		m.put("A {0} used as body of build notification email", "Un {0} utilisé comme corps de l'email de notification de build");
		m.put("A {0} used as body of commit notification email", "Un {0} utilisé comme corps de l'email de notification de commit");
		m.put("A {0} used as body of feedback email when failed to open issue via service desk", "Un {0} utilisé comme corps de l'email de retour lorsque l'ouverture d'un problème via le service desk a échoué");
		m.put("A {0} used as body of feedback email when issue is opened via service desk", "Un {0} utilisé comme corps de l'email de retour lorsque le problème est ouvert via le service desk");
		m.put("A {0} used as body of feedback email when unsubscribed from issue notification", "Un {0} utilisé comme corps de l'email de retour lors de la désinscription des notifications de problème");
		m.put("A {0} used as body of feedback email when unsubscribed from pull request notification", 
			"Un {0} utilisé comme corps de l'email de retour lors de la désinscription des notifications de demande de tirage");
		m.put("A {0} used as body of issue stopwatch overdue notification email", "Un {0} utilisé comme corps de l'email de notification de dépassement de chronomètre de problème");
		m.put("A {0} used as body of package notification email", "Un {0} utilisé comme corps de l'email de notification de package");
		m.put("A {0} used as body of password reset email", "Un {0} utilisé comme corps de l'email de réinitialisation de mot de passe");
		m.put("A {0} used as body of system alert email", "Un {0} utilisé comme corps de l'email d'alerte système");
		m.put("A {0} used as body of user invitation email", "Un {0} utilisé comme corps de l'email d'invitation utilisateur");
		m.put("A {0} used as body of various issue notification emails", "Un {0} utilisé comme corps des divers emails de notification de problème");
		m.put("A {0} used as body of various pull request notification emails", "Un {0} utilisé comme corps des divers emails de notification de demande de tirage");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"URL API de votre instance JIRA cloud, par exemple, <tt>https://your-domain.atlassian.net/rest/api/3</tt>");
		m.put("Able to merge without conflicts", "Capable de fusionner sans conflits");
		m.put("Absolute or relative url of the image", "URL absolue ou relative de l'image");
		m.put("Absolute or relative url of the link", "URL absolue ou relative du lien");
		m.put("Access Anonymously", "Accéder anonymement");
		m.put("Access Build Log", "Accéder au journal de build");
		m.put("Access Build Pipeline", "Accéder au pipeline de build");
		m.put("Access Build Reports", "Accéder aux rapports de build");
		m.put("Access Confidential Issues", "Accéder aux problèmes confidentiels");
		m.put("Access Time Tracking", "Accéder au suivi du temps");
		m.put("Access Token", "Jeton d'accès");
		m.put("Access Token Authorization Bean", "Bean d'autorisation de jeton d'accès");
		m.put("Access Token Edit Bean", "Bean d'édition de jeton d'accès");
		m.put("Access Token Secret", "Secret du jeton d'accès");
		m.put("Access Token for Target Project", "Jeton d'accès pour le projet cible");
		m.put("Access Tokens", "Jetons d'accès");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"Le jeton d'accès est destiné à l'accès API et au pull/push de dépôt. Il ne peut pas être utilisé pour se connecter à l'interface web");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"Le jeton d'accès est destiné à l'accès API ou au pull/push de dépôt. Il ne peut pas être utilisé pour se connecter à l'interface web");
		m.put("Access token regenerated successfully", "Le jeton d'accès a été régénéré avec succès");
		m.put("Access token regenerated, make sure to update the token at agent side", "Le jeton d'accès a été régénéré, assurez-vous de mettre à jour le jeton côté agent");
		m.put("Account Email", "Email du compte");
		m.put("Account Name", "Nom du compte");
		m.put("Account is disabled", "Le compte est désactivé");
		m.put("Account set up successfully", "Configuration du compte réussie");
		m.put("Active Directory", "Active Directory");
		m.put("Active Since", "Actif depuis");
		m.put("Activities", "Activités");
		m.put("Activity by type", "Activité par type");
		m.put("Add", "Ajouter");
		m.put("Add Executor", "Ajouter un exécuteur");
		m.put("Add GPG key", "Ajouter une clé GPG");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "Ajoutez ici des clés GPG pour vérifier les commits/tags signés par cet utilisateur");
		m.put("Add GPG keys here to verify commits/tags signed by you", "Ajoutez ici des clés GPG pour vérifier les commits/tags signés par vous");
		m.put("Add GPG public keys to be trusted here. Commits signed with trusted keys will be shown as verified.", 
			"Ajoutez ici des clés publiques GPG à faire confiance. Les commits signés avec des clés de confiance seront affichés comme vérifiés.");
		m.put("Add Issue...", "Ajouter un problème...");
		m.put("Add Issues to Iteration", "Ajouter des problèmes à l'itération");
		m.put("Add New", "Ajouter nouveau");
		m.put("Add New Board", "Ajouter un nouveau tableau");
		m.put("Add New Email Address", "Ajouter une nouvelle adresse email");
		m.put("Add New Timesheet", "Ajouter une nouvelle feuille de temps");
		m.put("Add Rule", "Ajouter une règle");
		m.put("Add SSH key", "Ajouter une clé SSH");
		m.put("Add SSO provider", "Ajouter un fournisseur SSO");
		m.put("Add Spent Time", "Ajouter du temps passé");
		m.put("Add Timesheet", "Ajouter une feuille de temps");
		m.put("Add Widget", "Ajouter un widget");
		m.put("Add a GPG Public Key", "Ajouter une clé publique GPG");
		m.put("Add a SSH Key", "Ajouter une clé SSH");
		m.put("Add a package source like below", "Ajouter une source de package comme ci-dessous");
		m.put("Add after", "Ajouter après");
		m.put("Add agent", "Ajouter un agent");
		m.put("Add all cards to specified iteration", "Ajouter toutes les cartes à l'itération spécifiée");
		m.put("Add all commits from source branch to target branch with a merge commit", "Ajouter tous les commits de la branche source à la branche cible avec un commit de fusion");
		m.put("Add assignee...", "Ajouter un assigné...");
		m.put("Add before", "Ajouter avant");
		m.put("Add below to allow accessing via http protocol in new Maven versions", "Ajouter ci-dessous pour permettre l'accès via le protocole HTTP dans les nouvelles versions de Maven");
		m.put("Add child project", "Ajouter un projet enfant");
		m.put("Add comment", "Ajouter un commentaire");
		m.put("Add comment on this selection", "Ajouter un commentaire sur cette sélection");
		m.put("Add custom field", "Ajouter un champ personnalisé");
		m.put("Add dashboard", "Ajouter un tableau de bord");
		m.put("Add default issue board", "Ajouter un tableau de bord par défaut");
		m.put("Add files to current directory", "Ajouter des fichiers au répertoire actuel");
		m.put("Add files via upload", "Ajouter des fichiers via téléchargement");
		m.put("Add groovy script", "Ajouter un script groovy");
		m.put("Add issue description template", "Ajouter un modèle de description de problème");
		m.put("Add issue link", "Ajouter un lien de problème");
		m.put("Add issue state", "Ajouter un état de problème");
		m.put("Add issue state transition", "Ajouter une transition d'état de problème");
		m.put("Add link", "Ajouter un lien");
		m.put("Add new", "Ajouter nouveau");
		m.put("Add new card to this column", "Ajouter une nouvelle carte à cette colonne");
		m.put("Add new file", "Ajouter un nouveau fichier");
		m.put("Add new import", "Ajouter une nouvelle importation");
		m.put("Add new issue creation setting", "Ajouter un nouveau paramètre de création de problème");
		m.put("Add new job dependency", "Ajouter une nouvelle dépendance de tâche");
		m.put("Add new param", "Ajouter un nouveau paramètre");
		m.put("Add new post-build action", "Ajouter une nouvelle action post-construction");
		m.put("Add new project dependency", "Ajouter une nouvelle dépendance de projet");
		m.put("Add new step", "Ajouter une nouvelle étape");
		m.put("Add new trigger", "Ajouter un nouveau déclencheur");
		m.put("Add project", "Ajouter un projet");
		m.put("Add reviewer...", "Ajouter un réviseur...");
		m.put("Add to batch to commit with other suggestions later", "Ajouter au lot pour valider avec d'autres suggestions plus tard");
		m.put("Add to group...", "Ajouter au groupe...");
		m.put("Add to iteration...", "Ajouter à l'itération...");
		m.put("Add user to group...", "Ajouter un utilisateur au groupe...");
		m.put("Add value", "Ajouter une valeur");
		m.put("Add {0}", "Ajouter {0}");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "Commit ajouté \"{0}\" (<i class='text-danger'>manquant dans le dépôt</i>)");
		m.put("Added commit \"{0}\" ({1})", "Commit ajouté \"{0}\" ({1})");
		m.put("Added to group", "Ajouté au groupe");
		m.put("Additions", "Ajouts");
		m.put("Administration", "Administration");
		m.put("Administrative permission over a project", "Permission administrative sur un projet");
		m.put("Advanced Search", "Recherche avancée");
		m.put("After modification", "Après modification");
		m.put("Agent", "Agent");
		m.put("Agent Attribute", "Attribut de l'agent");
		m.put("Agent Count", "Nombre d'agents");
		m.put("Agent Edit Bean", "Bean d'édition de l'agent");
		m.put("Agent Selector", "Sélecteur d'agent");
		m.put("Agent is designed to be maintenance free. Once connected to server, it will be updated automatically upon server upgrade", 
			"L'agent est conçu pour être sans maintenance. Une fois connecté au serveur, il sera mis à jour automatiquement lors de la mise à niveau du serveur");
		m.put("Agent removed", "Agent supprimé");
		m.put("Agent tokens are used to authorize agents. It should be configured via environment variable <tt>agentToken</tt> if agent runs as docker container, or property <tt>agentToken</tt> in file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> if agent runs on bare metal/virtual machine. A token will be in-use and removed from this list if agent using it connects to server", 
			"Les jetons d'agent sont utilisés pour autoriser les agents. Ils doivent être configurés via la variable d'environnement <tt>agentToken</tt> si l'agent s'exécute en tant que conteneur Docker, ou la propriété <tt>agentToken</tt> dans le fichier <tt>&lt;agent dir&gt;/conf/agent.properties</tt> si l'agent s'exécute sur une machine physique/virtuelle. Un jeton sera utilisé et supprimé de cette liste si l'agent qui l'utilise se connecte au serveur");
		m.put("Agents", "Agents");
		m.put("Agents can be used to execute jobs on remote machines. Once started it will update itself from server automatically when necessary", 
			"Les agents peuvent être utilisés pour exécuter des tâches sur des machines distantes. Une fois démarré, il se mettra à jour automatiquement depuis le serveur lorsque nécessaire");
		m.put("Aggregated from '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':", "Agrégé depuis '<span wicket:id=\"estimatedTimeAggregationLink\"></span>' :");
		m.put("Aggregated from '<span wicket:id=\"spentTimeAggregationLink\"></span>':", "Agrégé depuis '<span wicket:id=\"spentTimeAggregationLink\"></span>' :");
		m.put("Aggregation Link", "Lien d'agrégation");
		m.put("Alert", "Alerte");
		m.put("Alert Setting", "Paramètre d'alerte");
		m.put("Alert Settings", "Paramètres d'alerte");
		m.put("Alert settings have been updated", "Les paramètres d'alerte ont été mis à jour");
		m.put("Alerts", "Alertes");
		m.put("All", "Tout");
		m.put("All Issues", "Tous les problèmes");
		m.put("All RESTful Resources", "Toutes les ressources RESTful");
		m.put("All accessible", "Tout accessible");
		m.put("All builds", "Toutes les constructions");
		m.put("All changes", "Tous les changements");
		m.put("All except", "Tout sauf");
		m.put("All files", "Tous les fichiers");
		m.put("All groups", "Tous les groupes");
		m.put("All issues", "Tous les problèmes");
		m.put("All platforms in OCI layout", "Toutes les plateformes dans la disposition OCI");
		m.put("All platforms in image", "Toutes les plateformes dans l'image");
		m.put("All possible classes", "Toutes les classes possibles");
		m.put("All projects", "Tous les projets");
		m.put("All projects with code read permission", "Tous les projets avec permission de lecture de code");
		m.put("All pull requests", "Toutes les demandes de tirage");
		m.put("All users", "Tous les utilisateurs");
		m.put("Allow Empty", "Autoriser vide");
		m.put("Allow Empty Value", "Autoriser une valeur vide");
		m.put("Allow Multiple", "Autoriser multiple");
		m.put("Allowed Licenses", "Licences autorisées");
		m.put("Allowed Self Sign-Up Email Domain", "Domaine d'email autorisé pour l'inscription automatique");
		m.put("Always", "Toujours");
		m.put("Always Pull Image", "Toujours tirer l'image");
		m.put("An issue already linked for {0}. Unlink it first", "Un problème est déjà lié pour {0}. Dissociez-le d'abord");
		m.put("An unexpected exception occurred", "Une exception inattendue s'est produite");
		m.put("And configure auth token of the registry", "Et configurez le jeton d'authentification du registre");
		m.put("Another pull request already open for this change", "Une autre pull request est déjà ouverte pour ce changement");
		m.put("Any agent", "N'importe quel agent");
		m.put("Any branch", "N'importe quelle branche");
		m.put("Any commit message", "N'importe quel message de commit");
		m.put("Any domain", "N'importe quel domaine");
		m.put("Any file", "N'importe quel fichier");
		m.put("Any issue", "N'importe quel problème");
		m.put("Any job", "N'importe quel travail");
		m.put("Any project", "N'importe quel projet");
		m.put("Any ref", "N'importe quelle référence");
		m.put("Any sender", "N'importe quel expéditeur");
		m.put("Any state", "N'importe quel état");
		m.put("Any tag", "N'importe quelle étiquette");
		m.put("Any user", "N'importe quel utilisateur");
		m.put("Api Key", "Clé API");
		m.put("Api Token", "Jeton API");
		m.put("Api Url", "URL API");
		m.put("Append", "Ajouter");
		m.put("Applicable Branches", "Branches applicables");
		m.put("Applicable Builds", "Constructions applicables");
		m.put("Applicable Code Comments", "Commentaires de code applicables");
		m.put("Applicable Commit Messages", "Messages de commit applicables");
		m.put("Applicable Commits", "Commits applicables");
		m.put("Applicable Images", "Images applicables");
		m.put("Applicable Issues", "Problèmes applicables");
		m.put("Applicable Jobs", "Travaux applicables");
		m.put("Applicable Names", "Noms applicables");
		m.put("Applicable Projects", "Projets applicables");
		m.put("Applicable Pull Requests", "Pull requests applicables");
		m.put("Applicable Senders", "Expéditeurs applicables");
		m.put("Applicable Users", "Utilisateurs applicables");
		m.put("Application (client) ID", "ID de l'application (client)");
		m.put("Apply suggested change from code comment", "Appliquer le changement suggéré à partir du commentaire de code");
		m.put("Apply suggested changes from code comments", "Appliquer les changements suggérés à partir des commentaires de code");
		m.put("Approve", "Approuver");
		m.put("Approved", "Approuvé");
		m.put("Approved pull request \"{0}\" ({1})", "Pull request approuvée \"{0}\" ({1})");
		m.put("Arbitrary scope", "Portée arbitraire");
		m.put("Arbitrary type", "Type arbitraire");
		m.put("Arch Pull Command", "Commande de pull Arch");
		m.put("Archived", "Archivé");
		m.put("Arguments", "Arguments");
		m.put("Artifacts", "Artefacts");
		m.put("Artifacts to Retrieve", "Artefacts à récupérer");
		m.put("As long as a feature can be accessed via url, you can input part of the url to match and jump", 
			"Tant qu'une fonctionnalité est accessible via une URL, vous pouvez saisir une partie de l'URL pour correspondre et sauter");
		m.put("Ascending", "Ascendant");
		m.put("Assignees", "Assignés");
		m.put("Assignees Issue Field", "Champ des assignés du problème");
		m.put("Assignees are expected to merge the pull request", "Les assignés sont censés fusionner la pull request");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"Les assignés ont la permission d'écrire du code et seront responsables de la fusion de la pull request");
		m.put("Asymmetric", "Asymétrique");
		m.put("At least one branch or tag should be selected", "Au moins une branche ou une étiquette doit être sélectionnée");
		m.put("At least one choice need to be specified", "Au moins un choix doit être spécifié");
		m.put("At least one email address should be configured, please add a new one first", "Au moins une adresse e-mail doit être configurée, veuillez en ajouter une nouvelle d'abord");
		m.put("At least one email address should be specified", "Au moins une adresse e-mail doit être spécifiée");
		m.put("At least one entry should be specified", "Au moins une entrée doit être spécifiée");
		m.put("At least one event type needs to be selected", "Au moins un type d'événement doit être sélectionné");
		m.put("At least one field needs to be specified", "Au moins un champ doit être spécifié");
		m.put("At least one project should be authorized", "Au moins un projet doit être autorisé");
		m.put("At least one project should be selected", "Au moins un projet doit être sélectionné");
		m.put("At least one repository should be selected", "Au moins un dépôt doit être sélectionné");
		m.put("At least one role is required", "Au moins un rôle est requis");
		m.put("At least one role must be selected", "Au moins un rôle doit être sélectionné");
		m.put("At least one state should be specified", "Au moins un état doit être spécifié");
		m.put("At least one tab should be added", "Au moins un onglet doit être ajouté");
		m.put("At least one user search base should be specified", "Au moins une base de recherche utilisateur doit être spécifiée");
		m.put("At least one value needs to be specified", "Au moins une valeur doit être spécifiée");
		m.put("At least two columns need to be defined", "Au moins deux colonnes doivent être définies");
		m.put("Attachment", "Pièce jointe");
		m.put("Attributes", "Attributs");
		m.put("Attributes (can only be edited when agent is online)", "Attributs (peuvent être modifiés uniquement lorsque l'agent est en ligne)");
		m.put("Attributes saved", "Attributs enregistrés");
		m.put("Audit", "Audit");
		m.put("Audit Log", "Journal d'audit");
		m.put("Audit Setting", "Paramètre d'audit");
		m.put("Audit log will be preserved for the specified number of days. This setting applies to all audit events, including system level and project level", 
			"Le journal d'audit sera conservé pour le nombre de jours spécifié. Ce paramètre s'applique à tous les événements d'audit, y compris au niveau système et au niveau projet");
		m.put("Auth Source", "Source d'authentification");
		m.put("Authenticate to Bitbucket Cloud", "S'authentifier sur Bitbucket Cloud");
		m.put("Authenticate to GitHub", "S'authentifier sur GitHub");
		m.put("Authenticate to GitLab", "S'authentifier sur GitLab");
		m.put("Authenticate to Gitea", "S'authentifier sur Gitea");
		m.put("Authenticate to JIRA cloud", "S'authentifier sur JIRA Cloud");
		m.put("Authenticate to YouTrack", "S'authentifier sur YouTrack");
		m.put("Authentication", "Authentification");
		m.put("Authentication Required", "Authentification requise");
		m.put("Authentication Test", "Test d'authentification");
		m.put("Authentication Token", "Jeton d'authentification");
		m.put("Authenticator", "Authentificateur");
		m.put("Authenticator Bean", "Bean d'authentificateur");
		m.put("Author", "Auteur");
		m.put("Author date", "Date de l'auteur");
		m.put("Authored By", "Créé par");
		m.put("Authorization", "Autorisation");
		m.put("Authorizations", "Autorisations");
		m.put("Authorize user...", "Autoriser l'utilisateur...");
		m.put("Authorized Projects", "Projets autorisés");
		m.put("Authorized Roles", "Rôles autorisés");
		m.put("Auto Merge", "Fusion automatique");
		m.put("Auto Spec", "Spécification automatique");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"La vérification automatique des mises à jour est effectuée en demandant une image dans votre navigateur depuis onedev.io indiquant la disponibilité de la nouvelle version, avec une couleur indiquant la gravité de la mise à jour. Cela fonctionne de la même manière que les demandes d'images d'avatar de gravatar. Si désactivé, il est fortement recommandé de vérifier manuellement les mises à jour de temps en temps (cela peut être fait via le menu d'aide en bas à gauche de l'écran) pour voir s'il y a des correctifs de sécurité/critiques");
		m.put("Auto-discovered executor", "Exécuteur découvert automatiquement");
		m.put("Available Agent Tokens", "Jetons d'agent disponibles");
		m.put("Available Choices", "Choix disponibles");
		m.put("Avatar", "Avatar");
		m.put("Avatar Service Url", "URL du service d'avatar");
		m.put("Avatar and name", "Avatar et nom");
		m.put("Back To Home", "Retour à l'accueil");
		m.put("Backlog", "Arriéré");
		m.put("Backlog Base Query", "Requête de base de l'arriéré");
		m.put("Backup", "Sauvegarde");
		m.put("Backup Now", "Sauvegarder maintenant");
		m.put("Backup Schedule", "Planification de sauvegarde");
		m.put("Backup Setting", "Paramètre de sauvegarde");
		m.put("Backup Setting Holder", "Support de paramètre de sauvegarde");
		m.put("Backup settings updated", "Paramètres de sauvegarde mis à jour");
		m.put("Bare Metal", "Métal nu");
		m.put("Base", "Base");
		m.put("Base Gpg Key", "Clé Gpg de base");
		m.put("Base Query", "Requête de base");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Format PEM encodé en Base64, commençant par -----BEGIN CERTIFICATE----- et se terminant par -----END CERTIFICATE-----");
		m.put("Basic Info", "Informations de base");
		m.put("Basic Settings", "Paramètres de base");
		m.put("Basic settings updated", "Paramètres de base mis à jour");
		m.put("Batch Edit All Queried Issues", "Modifier en lot tous les problèmes interrogés");
		m.put("Batch Edit Selected Issues", "Modifier en lot les problèmes sélectionnés");
		m.put("Batch Editing {0} Issues", "Modification en lot de {0} problèmes");
		m.put("Batched suggestions", "Suggestions groupées");
		m.put("Before modification", "Avant modification");
		m.put("Belonging Groups", "Groupes d'appartenance");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"Ci-dessous quelques critères courants. Tapez dans la boîte de recherche ci-dessus pour voir la liste complète et les combinaisons disponibles.");
		m.put("Below content is restored from an unsaved change. Clear to discard", "Le contenu ci-dessous est restauré à partir d'une modification non enregistrée. Effacer pour abandonner");
		m.put("Below information will also be sent", "Les informations ci-dessous seront également envoyées");
		m.put("Binary file.", "Fichier binaire.");
		m.put("Bitbucket App Password", "Mot de passe de l'application Bitbucket");
		m.put("Bitbucket Login Name", "Nom de connexion Bitbucket");
		m.put("Bitbucket Repositories to Import", "Dépôts Bitbucket à importer");
		m.put("Bitbucket Workspace", "Espace de travail Bitbucket");
		m.put("Bitbucket app password should be generated with permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>", 
			"Le mot de passe de l'application Bitbucket doit être généré avec les permissions <b>account/read</b>, <b>repositories/read</b> et <b>issues:read</b>");
		m.put("Blame", "Blâme");
		m.put("Blob", "Blob");
		m.put("Blob hash", "Hash du blob");
		m.put("Blob index version", "Version de l'index du blob");
		m.put("Blob name", "Nom du blob");
		m.put("Blob path", "Chemin du blob");
		m.put("Blob primary symbols", "Symboles primaires du blob");
		m.put("Blob secondary symbols", "Symboles secondaires du blob");
		m.put("Blob symbol list", "Liste des symboles du blob");
		m.put("Blob text", "Texte du blob");
		m.put("Blob unknown", "Blob inconnu");
		m.put("Blob upload invalid", "Téléchargement du blob invalide");
		m.put("Blob upload unknown", "Téléchargement du blob inconnu");
		m.put("Board", "Tableau");
		m.put("Board Columns", "Colonnes du tableau");
		m.put("Board Spec", "Spécification du tableau");
		m.put("Boards", "Tableaux");
		m.put("Body", "Corps");
		m.put("Bold", "Gras");
		m.put("Both", "Les deux");
		m.put("Bottom", "Bas");
		m.put("Branch", "Branche");
		m.put("Branch \"{0}\" already exists, please choose a different name", "La branche \"{0}\" existe déjà, veuillez choisir un autre nom");
		m.put("Branch \"{0}\" created", "Branche \"{0}\" créée");
		m.put("Branch \"{0}\" deleted", "Branche \"{0}\" supprimée");
		m.put("Branch <a wicket:id=\"targetBranch\"></a> is up to date with all commits from <a wicket:id=\"sourceBranch\"></a>. Try <a wicket:id=\"swapBranches\">swap source and target</a> for the comparison.", 
			"La branche <a wicket:id=\"targetBranch\"></a> est à jour avec tous les commits de <a wicket:id=\"sourceBranch\"></a>. Essayez <a wicket:id=\"swapBranches\">d'inverser la source et la cible</a> pour la comparaison.");
		m.put("Branch Choice Bean", "Bean de choix de branche");
		m.put("Branch Name", "Nom de la branche");
		m.put("Branch Protection", "Protection de la branche");
		m.put("Branch Revision", "Révision de la branche");
		m.put("Branch update", "Mise à jour de la branche");
		m.put("Branches", "Branches");
		m.put("Brand Setting Edit Bean", "Bean d'édition des paramètres de marque");
		m.put("Branding", "Image de marque");
		m.put("Branding settings updated", "Paramètres de marque mis à jour");
		m.put("Browse Code", "Parcourir le code");
		m.put("Browse code", "Parcourir le code");
		m.put("Bug Report", "Rapport de bug");
		m.put("Build", "Construire");
		m.put("Build #{0} already finished", "Construction #{0} déjà terminée");
		m.put("Build #{0} deleted", "Construction #{0} supprimée");
		m.put("Build #{0} not finished yet", "Construction #{0} pas encore terminée");
		m.put("Build Artifact Storage", "Stockage des artefacts de construction");
		m.put("Build Commit", "Commit de construction");
		m.put("Build Context", "Contexte de construction");
		m.put("Build Description", "Description de la construction");
		m.put("Build Filter", "Filtre de construction");
		m.put("Build Image", "Image de construction");
		m.put("Build Image (Kaniko)", "Image de construction (Kaniko)");
		m.put("Build Management", "Gestion de la construction");
		m.put("Build Notification", "Notification de construction");
		m.put("Build Notification Template", "Modèle de notification de construction");
		m.put("Build Number", "Numéro de construction");
		m.put("Build On Behalf Of", "Construire au nom de");
		m.put("Build Path", "Chemin de construction");
		m.put("Build Preservation", "Préservation de la construction");
		m.put("Build Preservations", "Préservations de construction");
		m.put("Build Preservations Bean", "Bean de préservations de construction");
		m.put("Build Preserve Rules", "Règles de préservation de la construction");
		m.put("Build Provider", "Fournisseur de construction");
		m.put("Build Spec", "Spécification de construction");
		m.put("Build Statistics", "Statistiques de construction");
		m.put("Build Version", "Version de construction");
		m.put("Build Volume Storage Class", "Classe de stockage du volume de construction");
		m.put("Build Volume Storage Size", "Taille de stockage du volume de construction");
		m.put("Build administrative permission for all jobs inside a project, including batch operations over multiple builds", 
			"Permission administrative de construction pour tous les travaux à l'intérieur d'un projet, y compris les opérations en lot sur plusieurs constructions");
		m.put("Build docker image with docker buildx. This step can only be executed by server docker executor or remote docker executor, and it uses the buildx builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko step instead", 
			"Construire une image docker avec docker buildx. Cette étape ne peut être exécutée que par l'exécuteur docker du serveur ou l'exécuteur docker distant, et elle utilise le constructeur buildx spécifié dans ces exécuteurs pour effectuer le travail. Pour construire une image avec l'exécuteur Kubernetes, veuillez utiliser l'étape kaniko à la place");
		m.put("Build docker image with kaniko. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Construire une image docker avec kaniko. Cette étape doit être exécutée par l'exécuteur docker du serveur, l'exécuteur docker distant ou l'exécuteur Kubernetes");
		m.put("Build duration statistics", "Statistiques de durée de construction");
		m.put("Build frequency statistics", "Statistiques de fréquence de construction");
		m.put("Build is successful", "La construction est réussie");
		m.put("Build list", "Liste des constructions");
		m.put("Build not exist or access denied", "La construction n'existe pas ou l'accès est refusé");
		m.put("Build number", "Numéro de construction");
		m.put("Build preserve rules saved", "Règles de préservation de la construction enregistrées");
		m.put("Build required for deletion. Submit pull request instead", "Construction requise pour la suppression. Soumettez une demande de tirage à la place");
		m.put("Build required for this change. Please submit pull request instead", "Une construction est requise pour ce changement. Veuillez soumettre une demande de tirage à la place.");
		m.put("Build required for this change. Submit pull request instead", "Construction requise pour ce changement. Soumettez une demande de tirage à la place");
		m.put("Build spec not defined", "Spécification de construction non définie");
		m.put("Build spec not defined (import project: {0}, import revision: {1})", "Spécification de construction non définie (projet importé : {0}, révision importée : {1})");
		m.put("Build spec not found in commit of this build", "Spécification de construction introuvable dans le commit de cette construction");
		m.put("Build statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Les statistiques de construction sont une fonctionnalité d'entreprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("Build version", "Version de construction");
		m.put("Build with Persistent Volume", "Construction avec volume persistant");
		m.put("Builds", "Constructions");
		m.put("Builds are {0}", "Les builds sont {0}");
		m.put("Buildx Builder", "Buildx Builder");
		m.put("Built In Fields Bean", "Bean des champs intégrés");
		m.put("Burndown", "Burndown");
		m.put("Burndown chart", "Graphique de burndown");
		m.put("Button Image Url", "URL de l'image du bouton");
		m.put("By Group", "Par groupe");
		m.put("By User", "Par utilisateur");
		m.put("By day", "Par jour");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"Par défaut, le code est cloné via un identifiant auto-généré, qui n'a que des permissions de lecture sur le projet actuel. Si le travail nécessite de <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>pousser du code vers le serveur</a>, vous devez fournir ici un identifiant personnalisé avec les permissions appropriées");
		m.put("By default issues of parent and child projects will also be listed. Use query <code>&quot;Project&quot; is current</code> to show issues only belong to this project", 
			"Par défaut, les problèmes des projets parent et enfant seront également listés. Utilisez la requête <code>&quot;Project&quot; is current</code> pour afficher uniquement les problèmes appartenant à ce projet");
		m.put("By month", "Par mois");
		m.put("By week", "Par semaine");
		m.put("Bypass Certificate Check", "Contourner la vérification des certificats");
		m.put("CANCELLED", "ANNULÉ");
		m.put("CORS Allowed Origins", "Origines autorisées par CORS");
		m.put("CPD Report", "Rapport CPD");
		m.put("CPU", "CPU");
		m.put("CPU Intensive Task Concurrency", "Concurrence des tâches intensives en CPU");
		m.put("CPU capability in millis. This is normally (CPU cores)*1000", "Capacité CPU en millis. Cela correspond normalement à (cœurs CPU)*1000");
		m.put("Cache Key", "Clé de cache");
		m.put("Cache Management", "Gestion du cache");
		m.put("Cache Paths", "Chemins de cache");
		m.put("Cache Setting Bean", "Bean de configuration du cache");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "Le cache sera supprimé pour économiser de l'espace s'il n'est pas consulté pendant ce nombre de jours");
		m.put("Calculating merge preview...", "Calcul du prévisualisation de fusion...");
		m.put("Callback URL", "URL de rappel");
		m.put("Can Be Used By Jobs", "Peut être utilisé par les travaux");
		m.put("Can Create Root Projects", "Peut créer des projets racine");
		m.put("Can Edit Estimated Time", "Peut modifier le temps estimé");
		m.put("Can not convert root user to service account", "Impossible de convertir l'utilisateur root en compte de service");
		m.put("Can not convert yourself to service account", "Impossible de vous convertir en compte de service");
		m.put("Can not delete default branch", "Impossible de supprimer la branche par défaut");
		m.put("Can not delete root account", "Impossible de supprimer le compte racine");
		m.put("Can not delete yourself", "Impossible de vous supprimer");
		m.put("Can not disable root account", "Impossible de désactiver le compte racine");
		m.put("Can not disable yourself", "Impossible de vous désactiver");
		m.put("Can not find issue board: ", "Impossible de trouver le tableau des problèmes :");
		m.put("Can not move project \"{0}\" to be under itself or its descendants", "Impossible de déplacer le projet \"{0}\" pour qu'il soit sous lui-même ou ses descendants");
		m.put("Can not perform this operation now", "Impossible d'effectuer cette opération maintenant");
		m.put("Can not reset password for service account or disabled user", "Impossible de réinitialiser le mot de passe pour un compte de service ou un utilisateur désactivé");
		m.put("Can not reset password for user authenticating via external system", "Impossible de réinitialiser le mot de passe pour un utilisateur s'authentifiant via un système externe");
		m.put("Can not save malformed query", "Impossible d'enregistrer une requête malformée");
		m.put("Can not use current or descendant project as parent", "Impossible d'utiliser le projet actuel ou descendant comme parent");
		m.put("Can only compare with common ancestor when different projects are involved", "Peut seulement comparer avec un ancêtre commun lorsque différents projets sont impliqués");
		m.put("Cancel", "Annuler");
		m.put("Cancel All Queried Builds", "Annuler toutes les constructions interrogées");
		m.put("Cancel Selected Builds", "Annuler les constructions sélectionnées");
		m.put("Cancel invitation", "Annuler l'invitation");
		m.put("Cancel request submitted", "Demande d'annulation soumise");
		m.put("Cancel this build", "Annuler cette construction");
		m.put("Cancelled", "Annulé");
		m.put("Cancelled By", "Annulé par");
		m.put("Case Sensitive", "Sensible à la casse");
		m.put("Certificates to Trust", "Certificats à approuver");
		m.put("Change", "Changer");
		m.put("Change Detection Excludes", "Exclusions de détection de changement");
		m.put("Change My Password", "Changer mon mot de passe");
		m.put("Change To", "Changer en");
		m.put("Change already merged", "Changement déjà fusionné");
		m.put("Change not updated yet", "Changement pas encore mis à jour");
		m.put("Change property <code>serverUrl</code> in file <code>conf/agent.properties</code> if necessary. The default value is taken from OneDev server url specified in <i>Administration / System Setting</i>", 
			"Modifiez la propriété <code>serverUrl</code> dans le fichier <code>conf/agent.properties</code> si nécessaire. La valeur par défaut est prise à partir de l'URL du serveur OneDev spécifiée dans <i>Administration / Paramètres système</i>");
		m.put("Change to another field", "Changer pour un autre champ");
		m.put("Change to another state", "Changer pour un autre état");
		m.put("Change to another value", "Changer pour une autre valeur");
		m.put("Changes since last review", "Changements depuis la dernière révision");
		m.put("Changes since last visit", "Changements depuis la dernière visite");
		m.put("Changes since this action", "Changements depuis cette action");
		m.put("Changes since this comment", "Changements depuis ce commentaire");
		m.put("Channel Notification", "Notification de canal");
		m.put("Chart Metadata", "Métadonnées du graphique");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"Consultez <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">le guide de GitHub</a> sur la façon de générer et d'utiliser des clés GPG pour signer vos commits");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including instructions on how to run agent as service", 
			"Consultez <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">la gestion des agents</a> pour plus de détails, y compris les instructions sur la façon d'exécuter un agent en tant que service");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including list of supported environment variables", 
			"Consultez <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">la gestion des agents</a> pour plus de détails, y compris la liste des variables d'environnement prises en charge");
		m.put("Check Commit Message Footer", "Vérifier le pied de message de commit");
		m.put("Check Incoming Email", "Vérifier les emails entrants");
		m.put("Check Issue Integrity", "Vérifier l'intégrité des problèmes");
		m.put("Check Update", "Vérifier la mise à jour");
		m.put("Check Workflow Integrity", "Vérifier l'intégrité du workflow");
		m.put("Check out to local workspace", "Vérifier à l'espace de travail local");
		m.put("Check this to compare right side with common ancestor of left and right", "Cochez ceci pour comparer le côté droit avec l'ancêtre commun de gauche et de droite");
		m.put("Check this to enforce two-factor authentication for all users in the system", "Cochez ceci pour imposer l'authentification à deux facteurs pour tous les utilisateurs du système");
		m.put("Check this to enforce two-factor authentication for all users in this group", "Cochez ceci pour imposer l'authentification à deux facteurs pour tous les utilisateurs de ce groupe");
		m.put("Check this to prevent branch creation", "Cochez ceci pour empêcher la création de branches");
		m.put("Check this to prevent branch deletion", "Cochez ceci pour empêcher la suppression de branches");
		m.put("Check this to prevent forced push", "Cochez ceci pour empêcher les poussées forcées");
		m.put("Check this to prevent tag creation", "Cochez ceci pour empêcher la création de tags");
		m.put("Check this to prevent tag deletion", "Cochez ceci pour empêcher la suppression de tags");
		m.put("Check this to prevent tag update", "Cochez ceci pour empêcher la mise à jour des tags");
		m.put("Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>. Note this is applicable for non-merge commits", 
			"Cochez ceci pour exiger des <a href='https://www.conventionalcommits.org' target='_blank'>commits conventionnels</a>. Notez que cela s'applique aux commits non fusionnés");
		m.put("Check this to require valid signature of head commit", "Cochez ceci pour exiger une signature valide du commit principal");
		m.put("Check this to retrieve Git LFS files", "Cochez ceci pour récupérer les fichiers Git LFS");
		m.put("Checkbox", "Case à cocher");
		m.put("Checking field values...", "Vérification des valeurs des champs...");
		m.put("Checking fields...", "Vérification des champs...");
		m.put("Checking state and field ordinals...", "Vérification des états et des ordres des champs...");
		m.put("Checking state...", "Vérification de l'état...");
		m.put("Checkout Code", "Extraire le code");
		m.put("Checkout Path", "Chemin d'extraction");
		m.put("Checkout Pull Request Head", "Extraire la tête de la demande de tirage");
		m.put("Checkout Pull Request Merge Preview", "Extraire l'aperçu de fusion de la demande de tirage");
		m.put("Checkstyle Report", "Rapport Checkstyle");
		m.put("Cherry-Pick", "Cherry-Pick");
		m.put("Cherry-picked successfully", "Cherry-Pick réussi");
		m.put("Child Projects", "Projets enfants");
		m.put("Child Projects Of", "Projets enfants de");
		m.put("Choice Provider", "Fournisseur de choix");
		m.put("Choose", "Choisir");
		m.put("Choose JIRA project to import issues from", "Choisissez le projet JIRA pour importer des problèmes");
		m.put("Choose Revision", "Choisir la révision");
		m.put("Choose YouTrack project to import issues from", "Choisissez le projet YouTrack pour importer des problèmes");
		m.put("Choose a project...", "Choisissez un projet...");
		m.put("Choose a user...", "Choisissez un utilisateur...");
		m.put("Choose branch...", "Choisissez une branche...");
		m.put("Choose branches...", "Choisissez des branches...");
		m.put("Choose build...", "Choisissez une construction...");
		m.put("Choose file", "Choisissez un fichier");
		m.put("Choose group...", "Choisissez un groupe...");
		m.put("Choose groups...", "Choisissez des groupes...");
		m.put("Choose issue...", "Choisissez un problème...");
		m.put("Choose issues...", "Choisir des problèmes...");
		m.put("Choose iteration...", "Choisissez une itération...");
		m.put("Choose iterations...", "Choisissez des itérations...");
		m.put("Choose job...", "Choisissez un travail...");
		m.put("Choose jobs...", "Choisissez des travaux...");
		m.put("Choose project", "Choisissez un projet");
		m.put("Choose projects...", "Choisissez des projets...");
		m.put("Choose pull request...", "Choisissez une demande de tirage...");
		m.put("Choose repository", "Choisissez un dépôt");
		m.put("Choose role...", "Choisissez un rôle...");
		m.put("Choose roles...", "Choisissez des rôles...");
		m.put("Choose users...", "Choisissez des utilisateurs...");
		m.put("Choose...", "Choisissez...");
		m.put("Circular build spec imports ({0})", "Importations circulaires de spécifications de construction ({0})");
		m.put("Click to select a commit, or shift-click to select multiple commit", "Cliquez pour sélectionner un commit, ou maj-clic pour sélectionner plusieurs commits");
		m.put("Click to show comment of marked text", "Cliquez pour afficher le commentaire du texte marqué");
		m.put("Click to show issue details", "Cliquez pour afficher les détails du problème");
		m.put("Client ID of this OneDev instance registered in Google cloud", "ID client de cette instance OneDev enregistrée dans le cloud Google");
		m.put("Client Id", "ID client");
		m.put("Client Secret", "Secret client");
		m.put("Client secret of this OneDev instance registered in Google cloud", "Secret client de cette instance OneDev enregistrée dans le cloud Google");
		m.put("Clippy Report", "Rapport Clippy");
		m.put("Clone", "Cloner");
		m.put("Clone Credential", "Identifiant de clonage");
		m.put("Clone Depth", "Profondeur de clonage");
		m.put("Clone in IntelliJ", "Cloner dans IntelliJ");
		m.put("Clone in VSCode", "Cloner dans VSCode");
		m.put("Close", "Fermer");
		m.put("Close Iteration", "Fermer l'itération");
		m.put("Close this iteration", "Fermer cette itération");
		m.put("Closed", "Fermé");
		m.put("Closed Issue State", "État du problème fermé");
		m.put("Closest due date", "Date d'échéance la plus proche");
		m.put("Clover Coverage Report", "Rapport de couverture Clover");
		m.put("Cluster Role", "Rôle de cluster");
		m.put("Cluster Setting", "Paramètre de cluster");
		m.put("Cluster setting", "Paramètre de cluster");
		m.put("Clustered Servers", "Serveurs en cluster");
		m.put("Cobertura Coverage Report", "Rapport de couverture Cobertura");
		m.put("Code", "Code");
		m.put("Code Analysis", "Analyse de code");
		m.put("Code Analysis Setting", "Paramètre d'analyse de code");
		m.put("Code Analysis Settings", "Paramètres d'analyse de code");
		m.put("Code Changes", "Modifications de code");
		m.put("Code Comment", "Commentaire de code");
		m.put("Code Comment Management", "Gestion des commentaires de code");
		m.put("Code Comments", "Commentaires de code");
		m.put("Code Compare", "Comparaison de code");
		m.put("Code Contribution Statistics", "Statistiques de contribution de code");
		m.put("Code Coverage", "Couverture de code");
		m.put("Code Line Statistics", "Statistiques de lignes de code");
		m.put("Code Management", "Gestion du code");
		m.put("Code Privilege", "Privilège de code");
		m.put("Code Problem Statistics", "Statistiques des problèmes de code");
		m.put("Code Search", "Recherche de code");
		m.put("Code Statistics", "Statistiques de code");
		m.put("Code analysis settings updated", "Paramètres d'analyse de code mis à jour");
		m.put("Code changes since...", "Modifications de code depuis...");
		m.put("Code clone or download", "Cloner ou télécharger le code");
		m.put("Code comment", "Commentaire de code");
		m.put("Code comment #{0} deleted", "Commentaire de code #{0} supprimé");
		m.put("Code comment administrative permission inside a project, including batch operations over multiple code comments", 
			"Permission administrative des commentaires de code dans un projet, y compris les opérations en lot sur plusieurs commentaires de code");
		m.put("Code commit", "Commit de code");
		m.put("Code is committed", "Code est engagé");
		m.put("Code push", "Push de code");
		m.put("Code read permission is required to import build spec (import project: {0}, import revision: {1})", 
			"Permission de lecture de code requise pour importer la spécification de construction (projet importé : {0}, révision importée : {1})");
		m.put("Code suggestion", "Suggestion de code");
		m.put("Code write permission is required for this operation", "Permission d'écriture de code requise pour cette opération");
		m.put("Collapse all", "Réduire tout");
		m.put("Color", "Couleur");
		m.put("Columns", "Colonnes");
		m.put("Command Palette", "Palette de commandes");
		m.put("Commands", "Commandes");
		m.put("Comment", "Commentaire");
		m.put("Comment Content", "Contenu du commentaire");
		m.put("Comment on File", "Commenter sur le fichier");
		m.put("Comment too long", "Commentaire trop long");
		m.put("Commented code is outdated", "Le code commenté est obsolète");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "Commenté sur le fichier \"{0}\" dans le projet \"{1}\"");
		m.put("Commented on issue \"{0}\" ({1})", "Commenté sur le problème \"{0}\" ({1})");
		m.put("Commented on pull request \"{0}\" ({1})", "Commenté sur la demande de tirage \"{0}\" ({1})");
		m.put("Comments", "Commentaires");
		m.put("Commit", "Commit");
		m.put("Commit &amp; Insert", "Commit &amp; Insérer");
		m.put("Commit Batched Suggestions", "Commit des suggestions groupées");
		m.put("Commit Message", "Message de commit");
		m.put("Commit Message Bean", "Bean de message de commit");
		m.put("Commit Message Fix Patterns", "Modèles de correction de message de commit");
		m.put("Commit Message Footer Pattern", "Modèle de pied de page de message de commit");
		m.put("Commit Notification", "Notification de commit");
		m.put("Commit Notification Template", "Modèle de notification de commit");
		m.put("Commit Scopes", "Portées de commit");
		m.put("Commit Signature Required", "Signature de commit requise");
		m.put("Commit Suggestion", "Suggestion de commit");
		m.put("Commit Types", "Types de commit");
		m.put("Commit Types For Footer Check", "Types de commit pour vérification du pied de page");
		m.put("Commit Your Change", "Validez votre modification");
		m.put("Commit date", "Date de commit");
		m.put("Commit hash", "Hash de commit");
		m.put("Commit history of current path", "Historique des commits du chemin actuel");
		m.put("Commit index version", "Version de l'index de commit");
		m.put("Commit message can be used to fix issues by prefixing and suffixing issue number with specified pattern. Each line of the commit message will be matched against each entry defined here to find issues to be fixed", 
			"Le message de commit peut être utilisé pour corriger des problèmes en préfixant et suffixant le numéro du problème avec le modèle spécifié. Chaque ligne du message de commit sera comparée à chaque entrée définie ici pour trouver les problèmes à corriger");
		m.put("Commit not exist or access denied", "Le commit n'existe pas ou l'accès est refusé");
		m.put("Commit of the build is missing", "Le commit de la construction est manquant");
		m.put("Commit signature required but no GPG signing key specified", "Signature de commit requise mais aucune clé de signature GPG spécifiée");
		m.put("Commit suggestion", "Suggestion de commit");
		m.put("Commits", "Commits");
		m.put("Commits are taken from default branch of non-forked repositories", "Les commits sont pris à partir de la branche par défaut des dépôts non forkés");
		m.put("Commits generated by OneDev previously will be shown as unverified if this key is deleted. Type <code>yes</code> below if you want to continue.", 
			"Les commits générés précédemment par OneDev seront affichés comme non vérifiés si cette clé est supprimée. Tapez <code>yes</code> ci-dessous si vous souhaitez continuer.");
		m.put("Commits were merged into target branch", "Les commits ont été fusionnés dans la branche cible");
		m.put("Commits were merged into target branch outside of this pull request", "Les commits ont été fusionnés dans la branche cible en dehors de cette demande de tirage");
		m.put("Commits were rebased onto target branch", "Les commits ont été rebasés sur la branche cible");
		m.put("Commits were squashed into a single commit on target branch", "Les commits ont été écrasés en un seul commit sur la branche cible");
		m.put("Committed After", "Commit après");
		m.put("Committed Before", "Commit avant");
		m.put("Committed By", "Commit par");
		m.put("Committer", "Commetteur");
		m.put("Compare", "Comparer");
		m.put("Compare with base revision", "Comparer avec la révision de base");
		m.put("Compare with this parent", "Comparer avec ce parent");
		m.put("Concurrency", "Concurrence");
		m.put("Condition", "Condition");
		m.put("Confidential", "Confidentiel");
		m.put("Config File", "Fichier de configuration");
		m.put("Configuration Discovery Url", "URL de découverte de configuration");
		m.put("Configure your scope to use below registry", "Configurez votre portée pour utiliser le registre ci-dessous");
		m.put("Confirm Approve", "Confirmer l'approbation");
		m.put("Confirm Delete Source Branch", "Confirmer la suppression de la branche source");
		m.put("Confirm Discard", "Confirmer l'abandon");
		m.put("Confirm Reopen", "Confirmer la réouverture");
		m.put("Confirm Request For Changes", "Confirmer la demande de modifications");
		m.put("Confirm Restore Source Branch", "Confirmer la restauration de la branche source");
		m.put("Confirm password here", "Confirmez le mot de passe ici");
		m.put("Confirm your action", "Confirmez votre action");
		m.put("Connect New Agent", "Connecter un nouvel agent");
		m.put("Connect with your SSO account", "Connectez-vous avec votre compte SSO");
		m.put("Contact Email", "Email de contact");
		m.put("Contact Name", "Nom de contact");
		m.put("Container Image", "Image du conteneur");
		m.put("Container Image(s)", "Image(s) du conteneur");
		m.put("Container default", "Conteneur par défaut");
		m.put("Content", "Contenu");
		m.put("Content Type", "Type de contenu");
		m.put("Content is identical", "Le contenu est identique");
		m.put("Continue to add other user after create", "Continuer à ajouter d'autres utilisateurs après la création");
		m.put("Contributed settings", "Paramètres contribué");
		m.put("Contributions", "Contributions");
		m.put("Contributions to {0} branch, excluding merge commits", "Contributions à la branche {0}, en excluant les commits de fusion");
		m.put("Convert All Queried to Service Accounts", "Convertir tous les interrogés en comptes de service");
		m.put("Convert Selected to Service Accounts", "Convertir les sélectionnés en comptes de service");
		m.put("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm", 
			"La conversion en comptes de service supprimera le mot de passe, les adresses e-mail, toutes les affectations et les surveillances. Tapez <code>yes</code> pour confirmer");
		m.put("Copy", "Copier");
		m.put("Copy All Queried Issues To...", "Copier tous les problèmes interrogés vers...");
		m.put("Copy Files with SCP", "Copier les fichiers avec SCP");
		m.put("Copy Selected Issues To...", "Copier les problèmes sélectionnés vers...");
		m.put("Copy dashboard", "Copier le tableau de bord");
		m.put("Copy issue number and title", "Copier le numéro et le titre du problème");
		m.put("Copy public key", "Copier la clé publique");
		m.put("Copy selected text to clipboard", "Copier le texte sélectionné dans le presse-papiers");
		m.put("Copy to clipboard", "Copier dans le presse-papiers");
		m.put("Count", "Compter");
		m.put("Coverage Statistics", "Statistiques de couverture");
		m.put("Covered", "Couvert");
		m.put("Covered by tests", "Couvert par les tests");
		m.put("Cppcheck Report", "Rapport Cppcheck");
		m.put("Cpu Limit", "Limite CPU");
		m.put("Cpu Request", "Demande CPU");
		m.put("Create", "Créer");
		m.put("Create Administrator Account", "Créer un compte administrateur");
		m.put("Create Branch", "Créer une branche");
		m.put("Create Branch Bean", "Bean de création de branche");
		m.put("Create Branch Bean With Revision", "Bean de création de branche avec révision");
		m.put("Create Child Project", "Créer un projet enfant");
		m.put("Create Child Projects", "Créer des projets enfants");
		m.put("Create Issue", "Créer un problème");
		m.put("Create Iteration", "Créer une itération");
		m.put("Create Merge Commit", "Créer un commit de fusion");
		m.put("Create Merge Commit If Necessary", "Créer un commit de fusion si nécessaire");
		m.put("Create New", "Créer nouveau");
		m.put("Create New File", "Créer un nouveau fichier");
		m.put("Create New User", "Créer un nouvel utilisateur");
		m.put("Create Project", "Créer un projet");
		m.put("Create Pull Request", "Créer une demande de tirage");
		m.put("Create Pull Request for This Change", "Créer une demande de tirage pour ce changement");
		m.put("Create Tag", "Créer une étiquette");
		m.put("Create Tag Bean", "Créer un bean d'étiquette");
		m.put("Create Tag Bean With Revision", "Créer un bean d'étiquette avec révision");
		m.put("Create User", "Créer un utilisateur");
		m.put("Create body", "Créer le corps");
		m.put("Create branch <b>{0}</b> from {1}", "Créer une branche <b>{0}</b> à partir de {1}");
		m.put("Create child projects under a project", "Créer des projets enfants sous un projet");
		m.put("Create issue", "Créer un problème");
		m.put("Create merge commit", "Créer un commit de fusion");
		m.put("Create merge commit if necessary", "Créer un commit de fusion si nécessaire");
		m.put("Create new issue", "Créer un nouveau problème");
		m.put("Create tag", "Créer une étiquette");
		m.put("Create tag <b>{0}</b> from {1}", "Créer une étiquette <b>{0}</b> à partir de {1}");
		m.put("Created At", "Créé à");
		m.put("Creation of this branch is prohibited per branch protection rule", "La création de cette branche est interdite selon la règle de protection des branches");
		m.put("Critical", "Critique");
		m.put("Critical Severity", "Gravité critique");
		m.put("Cron Expression", "Expression Cron");
		m.put("Cron schedule", "Planification Cron");
		m.put("Curl Location", "Emplacement Curl");
		m.put("Current Iteration", "Itération actuelle");
		m.put("Current Value", "Valeur actuelle");
		m.put("Current avatar", "Avatar actuel");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"Le contexte actuel est différent du contexte lorsque ce commentaire a été ajouté, cliquez pour afficher le contexte du commentaire");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"Le contexte actuel est différent du contexte lorsque cette réponse a été ajoutée, cliquez pour afficher le contexte de la réponse");
		m.put("Current context is different from this action, click to show the comment context", "Le contexte actuel est différent de cette action, cliquez pour afficher le contexte du commentaire");
		m.put("Current platform", "Plateforme actuelle");
		m.put("Current project", "Projet actuel");
		m.put("Custom Linux Shell", "Shell Linux personnalisé");
		m.put("DISCARDED", "REJETÉ");
		m.put("Dashboard Share Bean", "Bean de partage de tableau de bord");
		m.put("Dashboard name", "Nom du tableau de bord");
		m.put("Dashboards", "Tableaux de bord");
		m.put("Database Backup", "Sauvegarde de base de données");
		m.put("Date", "Date");
		m.put("Date Time", "Date et heure");
		m.put("Days Per Week", "Jours par semaine");
		m.put("Deactivate Subscription", "Désactiver l'abonnement");
		m.put("Deactivate Trial Subscription", "Désactiver l'abonnement d'essai");
		m.put("Default", "Par défaut");
		m.put("Default (Shell on Linux, Batch on Windows)", "Par défaut (Shell sur Linux, Batch sur Windows)");
		m.put("Default Assignees", "Assignés par défaut");
		m.put("Default Boards", "Tableaux par défaut");
		m.put("Default Fixed Issue Filter", "Filtre de problème résolu par défaut");
		m.put("Default Fixed Issue Filters", "Filtres de problème résolu par défaut");
		m.put("Default Fixed Issue Filters Bean", "Bean de filtres de problème résolu par défaut");
		m.put("Default Group", "Groupe par défaut");
		m.put("Default Issue Boards", "Tableaux de problèmes par défaut");
		m.put("Default Merge Strategy", "Stratégie de fusion par défaut");
		m.put("Default Multi Value Provider", "Fournisseur de valeurs multiples par défaut");
		m.put("Default Project", "Projet par défaut");
		m.put("Default Project Setting", "Paramètre de projet par défaut");
		m.put("Default Roles", "Rôles par défaut");
		m.put("Default Roles Bean", "Bean des rôles par défaut");
		m.put("Default Value", "Valeur par défaut");
		m.put("Default Value Provider", "Fournisseur de valeur par défaut");
		m.put("Default Values", "Valeurs par défaut");
		m.put("Default branch", "Branche par défaut");
		m.put("Default branding settings restored", "Paramètres de marque par défaut restaurés");
		m.put("Default fixed issue filters saved", "Filtres de problème résolu par défaut enregistrés");
		m.put("Default merge strategy", "Stratégie de fusion par défaut");
		m.put("Default roles affect default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"Les rôles par défaut affectent les autorisations par défaut accordées à tout le monde dans le système. Les autorisations par défaut réelles seront <b class='text-warning'>toutes les autorisations</b> contenues dans les rôles par défaut de ce projet et de tous ses projets parents");
		m.put("Define all custom issue fields here. Each project can decide to use all or a subset of these fields via its issue transition setting. <b class=\"text-warning\">NOTE: </b> Newly defined fields by default only appear in new issues. Batch edit existing issues from issue list page if you want them to have these new fields", 
			"Définissez tous les champs de problème personnalisés ici. Chaque projet peut décider d'utiliser tous ou une partie de ces champs via son paramètre de transition de problème. <b class=\"text-warning\">NOTE : </b> Les champs nouvellement définis apparaissent par défaut uniquement dans les nouveaux problèmes. Modifiez en lot les problèmes existants depuis la page de liste des problèmes si vous souhaitez qu'ils aient ces nouveaux champs");
		m.put("Define all custom issue states here. The first state will be used as initial state of created issues", 
			"Définissez tous les états de problème personnalisés ici. Le premier état sera utilisé comme état initial des problèmes créés");
		m.put("Define branch protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given branch and user, the first matching rule will take effect", 
			"Définissez les règles de protection des branches. Les règles définies dans le projet parent sont considérées comme définies après les règles définies ici. Pour une branche et un utilisateur donnés, la première règle correspondante prendra effet");
		m.put("Define default issue boards for all projects here. A certain project can override this setting to define its own issue boards.", 
			"Définissez les tableaux de problèmes par défaut pour tous les projets ici. Un certain projet peut remplacer ce paramètre pour définir ses propres tableaux de problèmes.");
		m.put("Define how issue states should be transited from one to another, either manually or automatically when some events happen. And the rule can be configured to apply to certain projects and issues via the applicable issues setting", 
			"Définissez comment les états de problème doivent être transités d'un à l'autre, soit manuellement, soit automatiquement lorsque certains événements se produisent. Et la règle peut être configurée pour s'appliquer à certains projets et problèmes via le paramètre de problèmes applicables");
		m.put("Define issue templates here. When a new issue is created, the first matching template will be used.", 
			"Définissez les modèles de problème ici. Lorsqu'un nouveau problème est créé, le premier modèle correspondant sera utilisé.");
		m.put("Define labels to be assigned to project, build or pull request. For issues, custom fields can be used which is much more powerful than labels", 
			"Définissez les étiquettes à attribuer au projet, à la construction ou à la demande de tirage. Pour les problèmes, des champs personnalisés peuvent être utilisés, ce qui est beaucoup plus puissant que les étiquettes");
		m.put("Define properties to be used in build spec. Properties will be inherited by child projects, and can be overridden by child properties with same name.", 
			"Définissez les propriétés à utiliser dans la spécification de construction. Les propriétés seront héritées par les projets enfants et peuvent être remplacées par des propriétés enfants portant le même nom.");
		m.put("Define rules to preserve builds. A build will be preserved as long as one rule defined here or in parent projects preserves it. All builds will be preserved if no rules are defined here and in parent projects", 
			"Définissez les règles pour préserver les constructions. Une construction sera préservée tant qu'une règle définie ici ou dans les projets parents la préserve. Toutes les constructions seront préservées si aucune règle n'est définie ici et dans les projets parents");
		m.put("Define tag protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given tag and user, the first matching rule will take effect", 
			"Définissez les règles de protection des étiquettes. Les règles définies dans le projet parent sont considérées comme définies après les règles définies ici. Pour une étiquette et un utilisateur donnés, la première règle correspondante prendra effet");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"Délai pour le premier nouvel essai en secondes. Le délai des nouveaux essais suivants sera calculé en utilisant un retour exponentiel basé sur cette valeur");
		m.put("Delete", "Supprimer");
		m.put("Delete All", "Tout supprimer");
		m.put("Delete All Queried Builds", "Supprimer toutes les constructions interrogées");
		m.put("Delete All Queried Comments", "Supprimer tous les commentaires interrogés");
		m.put("Delete All Queried Issues", "Supprimer tous les problèmes interrogés");
		m.put("Delete All Queried Packages", "Supprimer tous les paquets interrogés");
		m.put("Delete All Queried Projects", "Supprimer tous les projets interrogés");
		m.put("Delete All Queried Pull Requests", "Supprimer toutes les demandes de tirage interrogées");
		m.put("Delete All Queried Users", "Supprimer tous les utilisateurs interrogés");
		m.put("Delete Build", "Supprimer la construction");
		m.put("Delete Comment", "Supprimer le commentaire");
		m.put("Delete Pull Request", "Supprimer la demande de tirage");
		m.put("Delete SSO account here to reconnect corresponding SSO subject upon next login. Note that SSO subject with verified email will be connected to user with same verified email automatically", 
			"Supprimez le compte SSO ici pour reconnecter le sujet SSO correspondant lors de la prochaine connexion. Notez que le sujet SSO avec un email vérifié sera automatiquement connecté à l'utilisateur avec le même email vérifié");
		m.put("Delete Selected", "Supprimer les éléments sélectionnés");
		m.put("Delete Selected Builds", "Supprimer les constructions sélectionnées");
		m.put("Delete Selected Comments", "Supprimer les commentaires sélectionnés");
		m.put("Delete Selected Issues", "Supprimer les problèmes sélectionnés");
		m.put("Delete Selected Packages", "Supprimer les packages sélectionnés");
		m.put("Delete Selected Projects", "Supprimer les projets sélectionnés");
		m.put("Delete Selected Pull Requests", "Supprimer les pull requests sélectionnées");
		m.put("Delete Selected Users", "Supprimer les utilisateurs sélectionnés");
		m.put("Delete Source Branch", "Supprimer la branche source");
		m.put("Delete Source Branch After Merge", "Supprimer la branche source après fusion");
		m.put("Delete dashboard", "Supprimer le tableau de bord");
		m.put("Delete from branch {0}", "Supprimer de la branche {0}");
		m.put("Delete this", "Supprimer ceci");
		m.put("Delete this GPG key", "Supprimer cette clé GPG");
		m.put("Delete this access token", "Supprimer ce jeton d'accès");
		m.put("Delete this branch", "Supprimer cette branche");
		m.put("Delete this executor", "Supprimer cet exécuteur");
		m.put("Delete this field", "Supprimer ce champ");
		m.put("Delete this import", "Supprimer cet import");
		m.put("Delete this iteration", "Supprimer cette itération");
		m.put("Delete this key", "Supprimer cette clé");
		m.put("Delete this link", "Supprimer ce lien");
		m.put("Delete this rule", "Supprimer cette règle");
		m.put("Delete this secret", "Supprimer ce secret");
		m.put("Delete this state", "Supprimer cet état");
		m.put("Delete this tag", "Supprimer ce tag");
		m.put("Delete this value", "Supprimer cette valeur");
		m.put("Deleted source branch", "Branche source supprimée");
		m.put("Deletion not allowed due to branch protection rule", "Suppression non autorisée en raison de la règle de protection de branche");
		m.put("Deletion not allowed due to tag protection rule", "Suppression non autorisée en raison de la règle de protection de tag");
		m.put("Deletions", "Suppressions");
		m.put("Denied", "Refusé");
		m.put("Dependencies & Services", "Dépendances et services");
		m.put("Dependency Management", "Gestion des dépendances");
		m.put("Dependency job finished", "Travail de dépendance terminé");
		m.put("Dependent Fields", "Champs dépendants");
		m.put("Depends on <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>", "Dépend de <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>");
		m.put("Descending", "Descendant");
		m.put("Description", "Description");
		m.put("Description Template", "Modèle de description");
		m.put("Description Templates", "Modèles de description");
		m.put("Description too long", "Description trop longue");
		m.put("Destination Path", "Chemin de destination");
		m.put("Destinations", "Destinations");
		m.put("Detect Licenses", "Détecter les licences");
		m.put("Detect Secrets", "Détecter les secrets");
		m.put("Detect Vulnerabilities", "Détecter les vulnérabilités");
		m.put("Diff is too large to be displayed.", "Le diff est trop grand pour être affiché.");
		m.put("Diff options", "Options de diff");
		m.put("Digest", "Digest");
		m.put("Digest invalid", "Digest invalide");
		m.put("Directories to Skip", "Répertoires à ignorer");
		m.put("Directory", "Répertoire");
		m.put("Directory (tenant) ID", "ID du répertoire (locataire)");
		m.put("Disable", "Désactiver");
		m.put("Disable All Queried Users", "Désactiver tous les utilisateurs interrogés");
		m.put("Disable Auto Update Check", "Désactiver la vérification de mise à jour automatique");
		m.put("Disable Dashboard", "Désactiver le tableau de bord");
		m.put("Disable Selected Users", "Désactiver les utilisateurs sélectionnés");
		m.put("Disabled", "Désactivé");
		m.put("Disabled users and service accounts are excluded from user-month calculation", "Les utilisateurs désactivés et les comptes de service sont exclus du calcul utilisateur-mois");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"Désactiver le compte réinitialisera le mot de passe, effacera les jetons d'accès et supprimera toutes les références des autres entités sauf les activités passées. Voulez-vous vraiment continuer ?");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"Désactiver les comptes réinitialisera le mot de passe, effacera les jetons d'accès et supprimera toutes les références des autres entités sauf les activités passées. Tapez <code>yes</code> pour confirmer");
		m.put("Disallowed File Types", "Types de fichiers non autorisés");
		m.put("Disallowed file type(s): {0}", "Type(s) de fichier non autorisé(s) : {0}");
		m.put("Discard", "Abandonner");
		m.put("Discard All Queried Pull Requests", "Abandonner toutes les pull requests interrogées");
		m.put("Discard Selected Pull Requests", "Abandonner les pull requests sélectionnées");
		m.put("Discarded", "Abandonné");
		m.put("Discarded pull request \"{0}\" ({1})", "Pull request abandonnée \"{0}\" ({1})");
		m.put("Discord", "Discord");
		m.put("Discord Notifications", "Notifications Discord");
		m.put("Display Fields", "Afficher les champs");
		m.put("Display Links", "Afficher les liens");
		m.put("Display Months", "Afficher les mois");
		m.put("Display Params", "Afficher les paramètres");
		m.put("Do Not Retrieve Groups", "Ne pas récupérer les groupes");
		m.put("Do not ignore", "Ne pas ignorer");
		m.put("Do not ignore whitespace", "Ne pas ignorer les espaces");
		m.put("Do not retrieve", "Ne pas récupérer");
		m.put("Do not retrieve groups", "Ne pas récupérer les groupes");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "Voulez-vous vraiment annuler l'invitation à \"{0}\" ?");
		m.put("Do you really want to cancel this build?", "Voulez-vous vraiment annuler cette construction ?");
		m.put("Do you really want to change target branch to {0}?", "Voulez-vous vraiment changer la branche cible en {0} ?");
		m.put("Do you really want to delete \"{0}\"?", "Voulez-vous vraiment supprimer \"{0}\" ?");
		m.put("Do you really want to delete SSO provider \"{0}\"?", "Voulez-vous vraiment supprimer le fournisseur SSO \"{0}\" ?");
		m.put("Do you really want to delete board \"{0}\"?", "Voulez-vous vraiment supprimer le tableau \"{0}\" ?");
		m.put("Do you really want to delete build #{0}?", "Voulez-vous vraiment supprimer la construction #{0} ?");
		m.put("Do you really want to delete group \"{0}\"?", "Voulez-vous vraiment supprimer le groupe \"{0}\" ?");
		m.put("Do you really want to delete iteration \"{0}\"?", "Voulez-vous vraiment supprimer l'itération \"{0}\" ?");
		m.put("Do you really want to delete job secret \"{0}\"?", "Voulez-vous vraiment supprimer le secret de travail \"{0}\" ?");
		m.put("Do you really want to delete pull request #{0}?", "Voulez-vous vraiment supprimer la pull request #{0} ?");
		m.put("Do you really want to delete role \"{0}\"?", "Voulez-vous vraiment supprimer le rôle \"{0}\" ?");
		m.put("Do you really want to delete selected query watches?", "Voulez-vous vraiment supprimer les surveillances de requêtes sélectionnées ?");
		m.put("Do you really want to delete tag {0}?", "Voulez-vous vraiment supprimer le tag {0} ?");
		m.put("Do you really want to delete this GPG key?", "Voulez-vous vraiment supprimer cette clé GPG ?");
		m.put("Do you really want to delete this SSH key?", "Voulez-vous vraiment supprimer cette clé SSH ?");
		m.put("Do you really want to delete this SSO account?", "Voulez-vous vraiment supprimer ce compte SSO ?");
		m.put("Do you really want to delete this access token?", "Voulez-vous vraiment supprimer ce jeton d'accès ?");
		m.put("Do you really want to delete this board?", "Voulez-vous vraiment supprimer ce tableau ?");
		m.put("Do you really want to delete this build?", "Voulez-vous vraiment supprimer cette construction ?");
		m.put("Do you really want to delete this code comment and all its replies?", "Voulez-vous vraiment supprimer ce commentaire de code et toutes ses réponses ?");
		m.put("Do you really want to delete this code comment?", "Voulez-vous vraiment supprimer ce commentaire de code ?");
		m.put("Do you really want to delete this directory?", "Voulez-vous vraiment supprimer ce répertoire ?");
		m.put("Do you really want to delete this email address?", "Voulez-vous vraiment supprimer cette adresse e-mail ?");
		m.put("Do you really want to delete this executor?", "Voulez-vous vraiment supprimer cet exécuteur ?");
		m.put("Do you really want to delete this field?", "Voulez-vous vraiment supprimer ce champ ?");
		m.put("Do you really want to delete this file?", "Voulez-vous vraiment supprimer ce fichier ?");
		m.put("Do you really want to delete this issue?", "Voulez-vous vraiment supprimer ce problème ?");
		m.put("Do you really want to delete this link?", "Voulez-vous vraiment supprimer ce lien ?");
		m.put("Do you really want to delete this package?", "Voulez-vous vraiment supprimer ce package ?");
		m.put("Do you really want to delete this privilege?", "Voulez-vous vraiment supprimer ce privilège ?");
		m.put("Do you really want to delete this protection?", "Voulez-vous vraiment supprimer cette protection ?");
		m.put("Do you really want to delete this pull request?", "Voulez-vous vraiment supprimer cette demande de tirage ?");
		m.put("Do you really want to delete this reply?", "Voulez-vous vraiment supprimer cette réponse ?");
		m.put("Do you really want to delete this script?", "Voulez-vous vraiment supprimer ce script ?");
		m.put("Do you really want to delete this state?", "Voulez-vous vraiment supprimer cet état ?");
		m.put("Do you really want to delete this template?", "Voulez-vous vraiment supprimer ce modèle ?");
		m.put("Do you really want to delete this transition?", "Voulez-vous vraiment supprimer cette transition ?");
		m.put("Do you really want to delete timesheet \"{0}\"?", "Voulez-vous vraiment supprimer la feuille de temps \"{0}\" ?");
		m.put("Do you really want to delete unused tokens?", "Voulez-vous vraiment supprimer les jetons inutilisés ?");
		m.put("Do you really want to discard batched suggestions?", "Voulez-vous vraiment abandonner les suggestions groupées ?");
		m.put("Do you really want to enable this account?", "Voulez-vous vraiment activer ce compte ?");
		m.put("Do you really want to rebuild?", "Voulez-vous vraiment reconstruire ?");
		m.put("Do you really want to remove assignee \"{0}\"?", "Voulez-vous vraiment retirer l'assigné \"{0}\" ?");
		m.put("Do you really want to remove password of this user?", "Voulez-vous vraiment supprimer le mot de passe de cet utilisateur ?");
		m.put("Do you really want to remove the issue from iteration \"{0}\"?", "Voulez-vous vraiment retirer le problème de l'itération \"{0}\" ?");
		m.put("Do you really want to remove this account?", "Voulez-vous vraiment supprimer ce compte ?");
		m.put("Do you really want to remove this agent?", "Voulez-vous vraiment supprimer cet agent ?");
		m.put("Do you really want to remove this link?", "Voulez-vous vraiment supprimer ce lien ?");
		m.put("Do you really want to restart this agent?", "Voulez-vous vraiment redémarrer cet agent ?");
		m.put("Do you really want to unauthorize user \"{0}\"?", "Voulez-vous vraiment désautoriser l'utilisateur \"{0}\" ?");
		m.put("Do you really want to use default template?", "Voulez-vous vraiment utiliser le modèle par défaut ?");
		m.put("Docker", "Docker");
		m.put("Docker Executable", "Exécutable Docker");
		m.put("Docker Hub", "Docker Hub");
		m.put("Docker Image", "Image Docker");
		m.put("Docker Sock Path", "Chemin du socket Docker");
		m.put("Dockerfile", "Dockerfile");
		m.put("Documentation", "Documentation");
		m.put("Don't have an account yet?", "Vous n'avez pas encore de compte ?");
		m.put("Download", "Télécharger");
		m.put("Download <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> or <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. A new agent token will be included in the package", 
			"Téléchargez <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> ou <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. Un nouveau jeton d'agent sera inclus dans le package");
		m.put("Download archive of this branch", "Télécharger l'archive de cette branche");
		m.put("Download full log", "Télécharger le journal complet");
		m.put("Download log", "Télécharger le journal");
		m.put("Download patch", "Télécharger le correctif");
		m.put("Download tag archive", "Télécharger l'archive du tag");
		m.put("Dry Run", "Exécution à blanc");
		m.put("Due Date", "Date d'échéance");
		m.put("Due Date Issue Field", "Champ de problème de date d'échéance");
		m.put("Due date", "Date d'échéance");
		m.put("Duplicate authorizations found: ", "Autorisations en double trouvées :");
		m.put("Duplicate authorizations found: {0}", "Autorisations en double trouvées : {0}");
		m.put("Duration", "Durée");
		m.put("Durations", "Durées");
		m.put("ESLint Report", "Rapport ESLint");
		m.put("Edit", "Modifier");
		m.put("Edit <code>$HOME/.gem/credentials</code> to add a source", "Modifiez <code>$HOME/.gem/credentials</code> pour ajouter une source");
		m.put("Edit <code>$HOME/.pypirc</code> to add a package repository like below", "Modifiez <code>$HOME/.pypirc</code> pour ajouter un dépôt de packages comme ci-dessous");
		m.put("Edit Avatar", "Modifier l'avatar");
		m.put("Edit Estimated Time", "Modifier le temps estimé");
		m.put("Edit Executor", "Modifier l'exécuteur");
		m.put("Edit Iteration", "Modifier l'itération");
		m.put("Edit Job Secret", "Modifier le secret du travail");
		m.put("Edit My Avatar", "Modifier mon avatar");
		m.put("Edit Rule", "Modifier la règle");
		m.put("Edit Timesheet", "Modifier la feuille de temps");
		m.put("Edit dashboard", "Modifier le tableau de bord");
		m.put("Edit issue title", "Modifier le titre du problème");
		m.put("Edit job", "Modifier le travail");
		m.put("Edit on branch {0}", "Modifier sur la branche {0}");
		m.put("Edit on source branch", "Modifier sur la branche source");
		m.put("Edit plain", "Modifier en texte brut");
		m.put("Edit saved queries", "Modifier les requêtes enregistrées");
		m.put("Edit this access token", "Modifier ce jeton d'accès");
		m.put("Edit this executor", "Modifier cet exécuteur");
		m.put("Edit this iteration", "Modifier cette itération");
		m.put("Edit this rule", "Modifier cette règle");
		m.put("Edit this secret", "Modifier ce secret");
		m.put("Edit this state", "Modifier cet état");
		m.put("Edit title", "Modifier le titre");
		m.put("Edit with AI", "Modifier avec l'IA");
		m.put("Edit {0}", "Modifier {0}");
		m.put("Editable Issue Fields", "Champs de problème modifiables");
		m.put("Editable Issue Links", "Liens de problème modifiables");
		m.put("Edited by {0} {1}", "Modifié par {0} {1}");
		m.put("Editor", "Éditeur");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "La branche cible ou la branche source a de nouveaux commits à l'instant, veuillez revérifier.");
		m.put("Email", "Email");
		m.put("Email Address", "Adresse email");
		m.put("Email Address Verification", "Vérification de l'adresse e-mail");
		m.put("Email Addresses", "Adresses email");
		m.put("Email Templates", "Modèles d'email");
		m.put("Email Verification", "Vérification de l'email");
		m.put("Email Verification Template", "Modèle de vérification de l'email");
		m.put("Email address", "Adresse email");
		m.put("Email address \"{0}\" already used by another account", "L'adresse email \"{0}\" est déjà utilisée par un autre compte");
		m.put("Email address \"{0}\" used by account \"{1}\"", "L'adresse email \"{0}\" est utilisée par le compte \"{1}\"");
		m.put("Email address \"{0}\" used by disabled account \"{1}\"", "L'adresse email \"{0}\" est utilisée par le compte désactivé \"{1}\"");
		m.put("Email address already in use: {0}", "Adresse email déjà utilisée : {0}");
		m.put("Email address already invited: {0}", "Adresse email déjà invitée : {0}");
		m.put("Email address already used by another user", "Adresse email déjà utilisée par un autre utilisateur");
		m.put("Email address already used: ", "Adresse e-mail déjà utilisée :");
		m.put("Email address to verify", "Adresse email à vérifier");
		m.put("Email addresses with <span class=\"badge badge-warning badge-sm\">ineffective</span> mark are those not belong to or not verified by key owner", 
			"Les adresses email avec la marque <span class=\"badge badge-warning badge-sm\">ineffective</span> sont celles qui n'appartiennent pas ou ne sont pas vérifiées par le propriétaire de la clé");
		m.put("Email templates", "Modèles d'email");
		m.put("Empty file added.", "Fichier vide ajouté.");
		m.put("Empty file removed.", "Fichier vide supprimé.");
		m.put("Enable", "Activer");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets", 
			"Activez <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>le suivi du temps</a> pour ce projet afin de suivre les progrès et de générer des feuilles de temps");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project", 
			"Activer la <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>gestion des packages</a> pour ce projet");
		m.put("Enable Account Self Removal", "Activer la suppression autonome du compte");
		m.put("Enable Account Self Sign-Up", "Activer l'inscription autonome au compte");
		m.put("Enable All Queried Users", "Activer tous les utilisateurs interrogés");
		m.put("Enable Anonymous Access", "Activer l'accès anonyme");
		m.put("Enable Auto Backup", "Activer la sauvegarde automatique");
		m.put("Enable Html Report Publish", "Activer la publication du rapport Html");
		m.put("Enable Selected Users", "Activer les utilisateurs sélectionnés");
		m.put("Enable Site Publish", "Activer la publication du site");
		m.put("Enable TTY Mode", "Activer le mode TTY");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "Activer la prise en charge de la construction par <a wicket:id=\"addFile\" class=\"link-primary\"></a>");
		m.put("Enable if visibility of this field depends on other fields", "Activer si la visibilité de ce champ dépend d'autres champs");
		m.put("Enable if visibility of this param depends on other params", "Activer si la visibilité de ce paramètre dépend d'autres paramètres");
		m.put("Enable this if the access token has same permissions as the owner", "Activer ceci si le jeton d'accès a les mêmes permissions que le propriétaire");
		m.put("Enable this option to merge the pull request automatically when ready (all reviewers approved, all required jobs passed etc.)", 
			"Activer cette option pour fusionner automatiquement la demande de tirage lorsqu'elle est prête (tous les examinateurs ont approuvé, tous les travaux requis ont réussi, etc.)");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Activez cette option pour permettre l'exécution de l'étape de publication du rapport html. Pour éviter les attaques XSS, assurez-vous que cet exécuteur ne peut être utilisé que par des travaux de confiance");
		m.put("Enable this to allow to run site publish step. OneDev will serve project site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Activer ceci pour permettre l'exécution de l'étape de publication du site. OneDev servira les fichiers du site du projet tels quels. Pour éviter les attaques XSS, assurez-vous que cet exécuteur ne peut être utilisé que par des travaux de confiance");
		m.put("Enable this to place intermediate files required by job execution on dynamically allocated persistent volume instead of emptyDir", 
			"Activer ceci pour placer les fichiers intermédiaires requis par l'exécution du travail sur un volume persistant alloué dynamiquement au lieu de emptyDir");
		m.put("Enable this to process issue or pull request comments posted via email", "Activer ceci pour traiter les commentaires sur les problèmes ou les demandes de tirage publiés par e-mail");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Activer ceci pour traiter les commentaires sur les problèmes ou les demandes de tirage publiés par e-mail. <b class='text-danger'>REMARQUE :</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Le sous-adressage</a> doit être activé pour l'adresse e-mail système ci-dessus, car OneDev l'utilise pour suivre les contextes des problèmes et des demandes de tirage");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Activer ceci pour traiter les commentaires sur les problèmes ou les demandes de tirage publiés par e-mail. <b class='text-danger'>REMARQUE :</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Le sous-adressage</a> doit être activé pour l'adresse e-mail système ci-dessus, car OneDev l'utilise pour suivre les contextes des problèmes et des demandes de tirage");
		m.put("Enable to allow to upload build cache generated during CI/CD job. Uploaded cache can be used by subsequent builds of the project as long as cache key matches", 
			"Activer pour permettre le téléchargement du cache de construction généré pendant le travail CI/CD. Le cache téléchargé peut être utilisé par les constructions suivantes du projet tant que la clé de cache correspond");
		m.put("End Point", "Point de terminaison");
		m.put("Enforce Conventional Commits", "Appliquer les commits conventionnels");
		m.put("Enforce Password Policy", "Appliquer la politique de mot de passe");
		m.put("Enforce Two-factor Authentication", "Appliquer l'authentification à deux facteurs");
		m.put("Enforce password policy for new users", "Appliquer la politique de mot de passe pour les nouveaux utilisateurs");
		m.put("Enter New Password", "Entrez un nouveau mot de passe");
		m.put("Enter description here", "Entrez la description ici");
		m.put("Enter your details to login to your account", "Entrez vos informations pour vous connecter à votre compte");
		m.put("Enter your user name or email to reset password", "Entrez votre nom d'utilisateur ou e-mail pour réinitialiser le mot de passe");
		m.put("Entries", "Entrées");
		m.put("Entry", "Entrée");
		m.put("Enumeration", "Énumération");
		m.put("Env Var", "Variable d'environnement");
		m.put("Environment Variables", "Variables d'environnement");
		m.put("Environment variable <code>serverUrl</code> in above command is taken from OneDev server url specified in <i>Administration / System Setting</i>. Change it if necessary", 
			"La variable d'environnement <code>serverUrl</code> dans la commande ci-dessus est tirée de l'URL du serveur OneDev spécifiée dans <i>Administration / Paramètre système</i>. Modifiez-la si nécessaire");
		m.put("Equal", "Égal");
		m.put("Error authenticating user", "Erreur d'authentification de l'utilisateur");
		m.put("Error calculating commits: check log for details", "Erreur de calcul des commits : vérifiez le journal pour plus de détails");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "Erreur lors du cherry-pick vers {0} : conflits de fusion détectés");
		m.put("Error cherry-picking to {0}: {1}", "Erreur lors du cherry-pick vers {0} : {1}");
		m.put("Error detail of content type &quot;text/plain&quot;", "Détail de l'erreur du type de contenu &quot;text/plain&quot;");
		m.put("Error discovering OIDC metadata", "Erreur lors de la découverte des métadonnées OIDC");
		m.put("Error executing task", "Erreur lors de l'exécution de la tâche");
		m.put("Error parsing %sbase query: ", "Erreur lors de l'analyse de la requête %sbase :");
		m.put("Error parsing %squery: ", "Erreur lors de l'analyse de la requête %squery :");
		m.put("Error parsing build spec", "Erreur lors de l'analyse de la spécification de construction");
		m.put("Error rendering widget, check server log for details", "Erreur lors du rendu du widget, vérifiez le journal du serveur pour plus de détails");
		m.put("Error reverting on {0}: Merge conflicts detected", "Erreur lors du retour sur {0} : conflits de fusion détectés");
		m.put("Error reverting on {0}: {1}", "Erreur lors du retour sur {0} : {1}");
		m.put("Error validating auto merge commit message: {0}", "Erreur lors de la validation du message de commit de fusion automatique : {0}");
		m.put("Error validating build spec (location: {0}, error message: {1})", "Erreur lors de la validation de la spécification de construction (emplacement : {0}, message d'erreur : {1})");
		m.put("Error validating build spec: {0}", "Erreur lors de la validation de la spécification de construction : {0}");
		m.put("Error validating commit message of \"{0}\": {1}", "Erreur lors de la validation du message de commit de \"{0}\" : {1}");
		m.put("Error validating commit message of <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}", 
			"Erreur lors de la validation du message de commit de <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a> : {2}");
		m.put("Error verifying GPG signature", "Erreur de vérification de la signature GPG");
		m.put("Estimated Time", "Temps estimé");
		m.put("Estimated Time Edit Bean", "Éditeur de temps estimé");
		m.put("Estimated Time Issue Field", "Champ de problème de temps estimé");
		m.put("Estimated Time:", "Temps estimé :");
		m.put("Estimated time", "Temps estimé");
		m.put("Estimated/Spent time. Click for details", "Temps estimé/dépensé. Cliquez pour plus de détails");
		m.put("Evaluate script to get choices", "Évaluer le script pour obtenir des choix");
		m.put("Evaluate script to get default value", "Évaluer le script pour obtenir une valeur par défaut");
		m.put("Evaluate script to get value or secret", "Évaluer le script pour obtenir une valeur ou un secret");
		m.put("Evaluate script to get values or secrets", "Évaluer le script pour obtenir des valeurs ou des secrets");
		m.put("Event Types", "Types d'événements");
		m.put("Events", "Événements");
		m.put("Ever Used Since", "Jamais utilisé depuis");
		m.put("Everything inside this project and all child projects will be deleted and can not be recovered, please type project path <code>{0}</code> below to confirm deletion.", 
			"Tout ce qui se trouve dans ce projet et tous les projets enfants seront supprimés et ne pourront pas être récupérés, veuillez taper le chemin du projet <code>{0}</code> ci-dessous pour confirmer la suppression.");
		m.put("Example", "Exemple");
		m.put("Example Plugin Setting", "Exemple de paramètre de plugin");
		m.put("Example Property", "Exemple de propriété");
		m.put("Exclude Param Combos", "Exclure les combinaisons de paramètres");
		m.put("Exclude States", "Exclure les états");
		m.put("Excluded", "Exclu");
		m.put("Excluded Fields", "Champs exclus");
		m.put("Executable", "Exécutable");
		m.put("Execute Commands", "Exécuter des commandes");
		m.put("Execute Commands via SSH", "Exécuter des commandes via SSH");
		m.put("Exit Impersonation", "Quitter l'imitation");
		m.put("Exited impersonation", "Imitation terminée");
		m.put("Expand all", "Développer tout");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", 
			"Attend un ou plusieurs <tt>&lt;nombre&gt;(h|m)</tt>. Par exemple, <tt>1h 1m</tt> représente 1 heure et 1 minute");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", 
			"Attend un ou plusieurs <tt>&lt;nombre&gt;(w|d|h|m)</tt>. Par exemple, <tt>1w 1d 1h 1m</tt> représente 1 semaine ({0} jours), 1 jour ({1} heures), 1 heure et 1 minute");
		m.put("Expiration Date:", "Date d'expiration :");
		m.put("Expire Date", "Date d'expiration");
		m.put("Expired", "Expiré");
		m.put("Explicit SSL (StartTLS)", "SSL explicite (StartTLS)");
		m.put("Export", "Exporter");
		m.put("Export All Queried Issues To...", "Exporter toutes les questions interrogées vers...");
		m.put("Export CSV", "Exporter CSV");
		m.put("Export XLSX", "Exporter XLSX");
		m.put("Export as OCI layout", "Exporter en tant que disposition OCI");
		m.put("Extend Trial Subscription", "Prolonger l'abonnement d'essai");
		m.put("External Authentication", "Authentification externe");
		m.put("External Issue Transformers", "Transformateurs de problèmes externes");
		m.put("External Participants", "Participants externes");
		m.put("External Password Authenticator", "Authentificateur de mot de passe externe");
		m.put("External System", "Système externe");
		m.put("External authenticator settings saved", "Paramètres de l'authentificateur externe enregistrés");
		m.put("External participants do not have accounts and involve in the issue via email", "Les participants externes n'ont pas de comptes et participent au problème par e-mail");
		m.put("Extract the package into a folder. <b class=\"text-danger\">Warning:</b> On Mac OS X, do not extract to Mac managed folders such as Downloads, Desktop, Documents; otherwise you may encounter permission issues starting agent", 
			"Extraire le package dans un dossier. <b class=\"text-danger\">Attention :</b> Sur Mac OS X, ne l'extrayez pas dans des dossiers gérés par Mac tels que Téléchargements, Bureau, Documents ; sinon, vous pourriez rencontrer des problèmes de permission lors du démarrage de l'agent");
		m.put("FAILED", "ÉCHOUÉ");
		m.put("Fail Threshold", "Seuil d'échec");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"Échouer la construction s'il y a des vulnérabilités avec une gravité spécifiée ou supérieure");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"Échouer la construction s'il y a des vulnérabilités avec une gravité spécifiée ou supérieure. Notez que cela ne prend effet que si la construction n'est pas échouée par d'autres étapes");
		m.put("Failed", "Échoué");
		m.put("Failed to validate build spec import. Check server log for details", "Échec de la validation de l'importation de la spécification de construction. Vérifiez le journal du serveur pour plus de détails");
		m.put("Failed to verify your email address", "Échec de la vérification de votre adresse e-mail");
		m.put("Field Bean", "Bean de champ");
		m.put("Field Instance", "Instance de champ");
		m.put("Field Name", "Nom du champ");
		m.put("Field Spec", "Spécification du champ");
		m.put("Field Specs", "Spécifications des champs");
		m.put("Field Value", "Valeur du champ");
		m.put("Fields", "Champs");
		m.put("Fields & Links", "Champs & Liens");
		m.put("Fields And Links Bean", "Bean des champs et des liens");
		m.put("Fields to Change", "Champs à modifier");
		m.put("File", "Fichier");
		m.put("File Changes", "Modifications du fichier");
		m.put("File Name", "Nom du fichier");
		m.put("File Name Patterns (separated by comma)", "Modèles de noms de fichiers (séparés par des virgules)");
		m.put("File Path", "Chemin du fichier");
		m.put("File Patterns", "Modèles de fichiers");
		m.put("File Protection", "Protection du fichier");
		m.put("File Protections", "Protections des fichiers");
		m.put("File and Symbol Search", "Recherche de fichiers et de symboles");
		m.put("File changes", "Modifications du fichier");
		m.put("File is too large to edit here", "Le fichier est trop volumineux pour être édité ici");
		m.put("File missing or obsolete", "Fichier manquant ou obsolète");
		m.put("File name", "Nom du fichier");
		m.put("File name patterns such as *.java, *.c", "Modèles de noms de fichiers tels que *.java, *.c");
		m.put("Files", "Fichiers");
		m.put("Files to Be Analyzed", "Fichiers à analyser");
		m.put("Filter", "Filtre");
		m.put("Filter Issues", "Filtrer les problèmes");
		m.put("Filter actions", "Filtrer les actions");
		m.put("Filter backlog issues", "Filtrer les problèmes en attente");
		m.put("Filter branches...", "Filtrer les branches...");
		m.put("Filter by name", "Filtrer par nom");
		m.put("Filter by name or email address", "Filtrer par nom ou adresse e-mail");
		m.put("Filter by name...", "Filtrer par nom...");
		m.put("Filter by path", "Filtrer par chemin");
		m.put("Filter by test suite", "Filtrer par suite de tests");
		m.put("Filter date range", "Filtrer la plage de dates");
		m.put("Filter files...", "Filtrer les fichiers...");
		m.put("Filter groups...", "Filtrer les groupes...");
		m.put("Filter issues", "Filtrer les problèmes");
		m.put("Filter pull requests", "Filtrer les demandes de tirage");
		m.put("Filter roles", "Filtrer les rôles");
		m.put("Filter tags...", "Filtrer les balises...");
		m.put("Filter targets", "Filtrer les cibles");
		m.put("Filter users", "Filtrer les utilisateurs");
		m.put("Filter...", "Filtrer...");
		m.put("Filters", "Filtres");
		m.put("Find branch", "Trouver une branche");
		m.put("Find or create branch", "Trouver ou créer une branche");
		m.put("Find or create tag", "Trouver ou créer une balise");
		m.put("Find tag", "Trouver une balise");
		m.put("Fingerprint", "Empreinte digitale");
		m.put("Finish", "Terminer");
		m.put("First applicable executor", "Premier exécuteur applicable");
		m.put("Fix", "Corriger");
		m.put("Fix Type", "Type de correction");
		m.put("Fix Undefined Field Values", "Corriger les valeurs de champs non définies");
		m.put("Fix Undefined Fields", "Corriger les champs non définis");
		m.put("Fix Undefined States", "Corriger les états non définis");
		m.put("Fixed Issues", "Problèmes corrigés");
		m.put("Fixed issues since...", "Problèmes corrigés depuis...");
		m.put("Fixing Builds", "Correction des builds");
		m.put("Fixing Commits", "Correction des commits");
		m.put("Fixing...", "Correction...");
		m.put("Float", "Flottant");
		m.put("Follow below instructions to publish packages into this project", "Suivez les instructions ci-dessous pour publier des packages dans ce projet");
		m.put("Follow below steps to install agent on remote machine (supports Linux/Windows/Mac OS X/FreeBSD):", 
			"Suivez les étapes ci-dessous pour installer l'agent sur une machine distante (compatible Linux/Windows/Mac OS X/FreeBSD) :");
		m.put("For CI/CD job, add this gem to Gemfile like below", "Pour un travail CI/CD, ajoutez ce gem au Gemfile comme ci-dessous");
		m.put("For CI/CD job, add this package to requirements.txt and run below to install the package via command step", 
			"Pour un travail CI/CD, ajoutez ce package à requirements.txt et exécutez la commande ci-dessous pour installer le package via une étape de commande");
		m.put("For CI/CD job, run below to add package repository via command step", "Pour un travail CI/CD, exécutez la commande ci-dessous pour ajouter le dépôt de packages via une étape de commande");
		m.put("For CI/CD job, run below to add package source via command step", "Pour un travail CI/CD, exécutez la commande ci-dessous pour ajouter la source du package via une étape de commande");
		m.put("For CI/CD job, run below to add source via command step", "Pour un travail CI/CD, exécutez la commande ci-dessous pour ajouter la source via une étape de commande");
		m.put("For CI/CD job, run below to install chart via command step", "Pour un travail CI/CD, exécutez la commande ci-dessous pour installer le chart via une étape de commande");
		m.put("For CI/CD job, run below to publish package via command step", "Pour un travail CI/CD, exécutez la commande ci-dessous pour publier le package via une étape de commande");
		m.put("For CI/CD job, run below to push chart to the repository via command step", "Pour un travail CI/CD, exécutez la commande ci-dessous pour pousser le chart vers le dépôt via une étape de commande");
		m.put("For CI/CD job, run below via a command step", "Pour un travail CI/CD, exécutez la commande ci-dessous via une étape de commande");
		m.put("For a particular project, the first matching entry will be used", "Pour un projet particulier, la première entrée correspondante sera utilisée");
		m.put("For all issues", "Pour tous les problèmes");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"Pour un commit de build non accessible depuis la branche par défaut, un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de travail</a> doit être spécifié comme jeton d'accès avec la permission de créer une branche");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"Pour un commit de build non accessible depuis la branche par défaut, un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de travail</a> doit être spécifié comme jeton d'accès avec la permission de créer une balise");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"Pour un commit de build non accessible depuis la branche par défaut, un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de travail</a> doit être spécifié comme jeton d'accès avec la permission de gérer les problèmes");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"Pour les exécuteurs compatibles avec Docker, ce chemin est à l'intérieur du conteneur et accepte à la fois les chemins absolus et relatifs (relatifs à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du travail</a>). Pour les exécuteurs liés au shell qui s'exécutent directement sur la machine hôte, seuls les chemins relatifs sont acceptés");
		m.put("For each build, OneDev calculates a list of fixed issues since previous build automatically. This setting provides a default query to further filter/order this list. For a given job, the first matching entry will be used.", 
			"Pour chaque build, OneDev calcule automatiquement une liste de problèmes corrigés depuis le build précédent. Ce paramètre fournit une requête par défaut pour filtrer/ordonner davantage cette liste. Pour un travail donné, la première entrée correspondante sera utilisée.");
		m.put("For each selected branch/tag, a separate build will be generated with branch/tag set to corresponding value", 
			"Pour chaque branche/balise sélectionnée, un build séparé sera généré avec la branche/balise définie à la valeur correspondante");
		m.put("For issues matching: ", "Pour les problèmes correspondant :");
		m.put("For very large git repository, you may need to tune options here to reduce memory usage", 
			"Pour un dépôt git très volumineux, vous devrez peut-être ajuster les options ici pour réduire l'utilisation de la mémoire");
		m.put("For web hooks defined here and in parent projects, OneDev will post event data in JSON format to specified URLs when subscribed events happen", 
			"Pour les webhooks définis ici et dans les projets parents, OneDev enverra les données d'événement au format JSON aux URL spécifiées lorsque les événements abonnés se produisent");
		m.put("Force", "Forcer");
		m.put("Force Garbage Collection", "Forcer la collecte des déchets");
		m.put("Forgot Password?", "Mot de passe oublié ?");
		m.put("Forgotten Password?", "Mot de passe oublié ?");
		m.put("Fork Project", "Forker le projet");
		m.put("Fork now", "Fork maintenant");
		m.put("Forks Of", "Forks de");
		m.put("Frequencies", "Fréquences");
		m.put("From Directory", "Depuis le répertoire");
		m.put("From States", "Depuis les états");
		m.put("From extracted folder, run <code>bin\\agent.bat console</code> as administrator on Windows or <code>bin/agent.sh console</code> on other OS", 
			"Depuis le dossier extrait, exécutez <code>bin\\agent.bat console</code> en tant qu'administrateur sur Windows ou <code>bin/agent.sh console</code> sur d'autres systèmes d'exploitation");
		m.put("From {0}", "Depuis {0}");
		m.put("Full Name", "Nom complet");
		m.put("Furthest due date", "Date d'échéance la plus éloignée");
		m.put("GPG Keys", "Clés GPG");
		m.put("GPG Public Key", "Clé publique GPG");
		m.put("GPG Signing Key", "Clé de signature GPG");
		m.put("GPG Trusted Keys", "Clés de confiance GPG");
		m.put("GPG key deleted", "Clé GPG supprimée");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "La clé publique GPG commence par '-----BEGIN PGP PUBLIC KEY BLOCK-----'");
		m.put("GPG signing key will be used to sign commits generated by OneDev, including pull request merge commits, user commits created via web UI or RESTful api.", 
			"La clé de signature GPG sera utilisée pour signer les commits générés par OneDev, y compris les commits de fusion des demandes de tirage, les commits utilisateur créés via l'interface web ou l'API RESTful.");
		m.put("Gem Info", "Informations sur la gemme");
		m.put("General", "Général");
		m.put("General Settings", "Paramètres généraux");
		m.put("General settings updated", "Paramètres généraux mis à jour");
		m.put("Generate", "Générer");
		m.put("Generate File Checksum", "Générer le checksum du fichier");
		m.put("Generate New", "Générer nouveau");
		m.put("Generic LDAP", "LDAP générique");
		m.put("Get", "Obtenir");
		m.put("Get Groups Using Attribute", "Obtenir des groupes en utilisant l'attribut");
		m.put("Git", "Git");
		m.put("Git Command Line", "Ligne de commande Git");
		m.put("Git Credential", "Identifiant Git");
		m.put("Git LFS Storage", "Stockage Git LFS");
		m.put("Git Lfs Lock", "Verrou Git LFS");
		m.put("Git Location", "Emplacement Git");
		m.put("Git Pack Config", "Configuration du pack Git");
		m.put("Git Path", "Chemin Git");
		m.put("Git email address will be used as git author/committer for commits created on web UI", 
			"L'adresse email Git sera utilisée comme auteur/committer Git pour les commits créés sur l'interface web");
		m.put("Git pack config updated", "Configuration du pack Git mise à jour");
		m.put("GitHub", "GitHub");
		m.put("GitHub API URL", "URL de l'API GitHub");
		m.put("GitHub Issue Label", "Étiquette de problème GitHub");
		m.put("GitHub Organization", "Organisation GitHub");
		m.put("GitHub Personal Access Token", "Jeton d'accès personnel GitHub");
		m.put("GitHub Repositories to Import", "Dépôts GitHub à importer");
		m.put("GitHub Repository", "Dépôt GitHub");
		m.put("GitHub personal access token should be generated with scope <b>repo</b> and <b>read:org</b>", 
			"Le jeton d'accès personnel GitHub doit être généré avec les portées <b>repo</b> et <b>read:org</b>");
		m.put("GitLab API URL", "URL de l'API GitLab");
		m.put("GitLab Group", "Groupe GitLab");
		m.put("GitLab Issue Label", "Étiquette de problème GitLab");
		m.put("GitLab Personal Access Token", "Jeton d'accès personnel GitLab");
		m.put("GitLab Project", "Projet GitLab");
		m.put("GitLab Projects to Import", "Projets GitLab à importer");
		m.put("GitLab personal access token should be generated with scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by user of specified access token will be listed", 
			"Le jeton d'accès personnel GitLab doit être généré avec les portées <b>read_api</b>, <b>read_user</b> et <b>read_repository</b>. Notez que seuls les groupes/projets appartenant à l'utilisateur du jeton d'accès spécifié seront listés");
		m.put("Gitea API URL", "URL de l'API Gitea");
		m.put("Gitea Issue Label", "Étiquette de problème Gitea");
		m.put("Gitea Organization", "Organisation Gitea");
		m.put("Gitea Personal Access Token", "Jeton d'accès personnel Gitea");
		m.put("Gitea Repositories to Import", "Dépôts Gitea à importer");
		m.put("Gitea Repository", "Dépôt Gitea");
		m.put("Github Access Token Secret", "Secret du jeton d'accès GitHub");
		m.put("Global", "Global");
		m.put("Global Build Setting", "Paramètre de construction global");
		m.put("Global Issue Setting", "Paramètre de problème global");
		m.put("Global Pack Setting", "Paramètre de pack global");
		m.put("Global Views", "Vues globales");
		m.put("Gmail", "Gmail");
		m.put("Go Back", "Retourner");
		m.put("Google Test Report", "Rapport de test Google");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Clé Gpg");
		m.put("Great, your mail service configuration is working", "Super, la configuration de votre service de messagerie fonctionne");
		m.put("Groovy Script", "Script Groovy");
		m.put("Groovy Scripts", "Scripts Groovy");
		m.put("Groovy script to be evaluated. It should return a <i>Date</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. Il doit retourner une valeur de type <i>Date</i>. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. It should return a <i>Float</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. Il doit retourner une valeur de type <i>Float</i>. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. It should return a <i>Integer</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. Il doit retourner une valeur de type <i>Integer</i>. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. It should return a <i>String</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. Il doit retourner une valeur de type <i>String</i>. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. It should return a <i>boolean</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. Il doit retourner une valeur de type <i>boolean</i>. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. It should return a <i>string</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. Il doit retourner une valeur de type <i>string</i>. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. It should return name of a group. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. Il doit retourner le nom d'un groupe. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. It should return string or list of string. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. Il doit retourner une chaîne ou une liste de chaînes. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. La valeur retournée doit être une liste d'objets façade de groupe à utiliser comme choix. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. The return value should be a list of user login names to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. La valeur retournée doit être une liste de noms de connexion utilisateur à utiliser comme choix. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy script to be evaluated. The return value should be a value to color map, for instance:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> if the value does not have a color. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy à évaluer. La valeur retournée doit être une carte de valeur à couleur, par exemple :<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Utilisez <tt>null</tt> si la valeur n'a pas de couleur. Consultez <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>l'aide au script</a> pour plus de détails");
		m.put("Groovy scripts", "Scripts Groovy");
		m.put("Group", "Groupe");
		m.put("Group \"{0}\" deleted", "Groupe \"{0}\" supprimé");
		m.put("Group Authorization Bean", "Bean d'autorisation de groupe");
		m.put("Group Authorizations", "Autorisations de groupe");
		m.put("Group Authorizations Bean", "Bean des autorisations de groupe");
		m.put("Group By", "Grouper par");
		m.put("Group Management", "Gestion des groupes");
		m.put("Group Name Attribute", "Attribut de nom de groupe");
		m.put("Group Retrieval", "Récupération de groupe");
		m.put("Group Search Base", "Base de recherche de groupe");
		m.put("Group Search Filter", "Filtre de recherche de groupe");
		m.put("Group authorizations updated", "Autorisations de groupe mises à jour");
		m.put("Group created", "Groupe créé");
		m.put("Groups", "Groupes");
		m.put("Groups Claim", "Revendication des groupes");
		m.put("Guide Line", "Ligne directrice");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "URL de clonage HTTP(S)");
		m.put("Has Owner Permissions", "A des permissions de propriétaire");
		m.put("Has Running Builds", "A des constructions en cours");
		m.put("Heap Memory Usage", "Utilisation de la mémoire du tas");
		m.put("Helm(s)", "Helm(s)");
		m.put("Help", "Aide");
		m.put("Hide", "Cacher");
		m.put("Hide Archived", "Cacher les archivés");
		m.put("Hide comment", "Cacher le commentaire");
		m.put("Hide saved queries", "Cacher les requêtes enregistrées");
		m.put("High", "Élevé");
		m.put("High Availability & Scalability", "Haute disponibilité et évolutivité");
		m.put("High Severity", "Gravité élevée");
		m.put("History", "Historique");
		m.put("History of comparing revisions is unrelated", "L'historique de la comparaison des révisions est sans rapport");
		m.put("History of target branch and source branch is unrelated", "L'historique de la branche cible et de la branche source est sans rapport");
		m.put("Host name or ip address of remote machine to run commands via SSH", "Nom d'hôte ou adresse IP de la machine distante pour exécuter des commandes via SSH");
		m.put("Hours Per Day", "Heures par jour");
		m.put("How to Publish", "Comment publier");
		m.put("Html Report", "Rapport HTML");
		m.put("Http Method", "Méthode Http");
		m.put("I didn't eat it. I swear!", "Je ne l'ai pas mangé. Je le jure !");
		m.put("ID token was expired", "Le jeton ID a expiré");
		m.put("IMAP Host", "Hôte IMAP");
		m.put("IMAP Password", "Mot de passe IMAP");
		m.put("IMAP User", "Utilisateur IMAP");
		m.put("IMPORTANT:", "IMPORTANT :");
		m.put("IP Address", "Adresse IP");
		m.put("Id", "Id");
		m.put("Identify Field", "Champ d'identification");
		m.put("If enabled, scheduled backup will run on lead server which is <span wicket:id=\"leadServer\"></span> currently", 
			"Si activé, la sauvegarde planifiée s'exécutera sur le serveur principal qui est <span wicket:id=\"leadServer\"></span> actuellement");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"Si activé, la branche source sera supprimée automatiquement après la fusion de la demande de tirage si l'utilisateur a la permission de le faire");
		m.put("If specified, OneDev will only display iterations with this prefix", "Si spécifié, OneDev n'affichera que les itérations avec ce préfixe");
		m.put("If specified, all public and internal projects imported from GitLab will use these as default roles. Private projects are not affected", 
			"Si spécifié, tous les projets publics et internes importés de GitLab utiliseront ceux-ci comme rôles par défaut. Les projets privés ne sont pas affectés");
		m.put("If specified, all public repositories imported from GitHub will use these as default roles. Private repositories are not affected", 
			"Si spécifié, tous les dépôts publics importés de GitHub utiliseront ceux-ci comme rôles par défaut. Les dépôts privés ne sont pas affectés");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"Si spécifié, le temps total estimé/passé d'un problème inclura également les problèmes liés de ce type");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"Si cette option est activée, la commande git lfs doit être installée sur le serveur OneDev (même si cette étape s'exécute sur un autre nœud)");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"Si coché, le groupe indiqué par ce champ pourra modifier le temps estimé des problèmes correspondants si le suivi du temps est activé");
		m.put("Ignore", "Ignorer");
		m.put("Ignore File", "Ignorer le fichier");
		m.put("Ignore activities irrelevant to me", "Ignorer les activités non pertinentes pour moi");
		m.put("Ignore all", "Tout ignorer");
		m.put("Ignore all whitespace", "Ignorer tous les espaces");
		m.put("Ignore change", "Ignorer le changement");
		m.put("Ignore change whitespace", "Ignorer les espaces de changement");
		m.put("Ignore leading", "Ignorer les espaces de début");
		m.put("Ignore leading whitespace", "Ignorer les espaces de début");
		m.put("Ignore this field", "Ignorer ce champ");
		m.put("Ignore this param", "Ignorer ce paramètre");
		m.put("Ignore trailing", "Ignorer les espaces de fin");
		m.put("Ignore trailing whitespace", "Ignorer les espaces de fin");
		m.put("Ignored Licenses", "Licences ignorées");
		m.put("Image", "Image");
		m.put("Image Labels", "Étiquettes d'image");
		m.put("Image Manifest", "Manifeste d'image");
		m.put("Image Size", "Taille de l'image");
		m.put("Image Text", "Texte de l'image");
		m.put("Image URL", "URL de l'image");
		m.put("Image URL should be specified", "L'URL de l'image doit être spécifiée");
		m.put("Imap Ssl Setting", "Paramètre SSL IMAP");
		m.put("Imap With Ssl", "IMAP avec SSL");
		m.put("Impersonate", "Usurper");
		m.put("Implicit SSL", "SSL implicite");
		m.put("Import", "Importer");
		m.put("Import All Projects", "Importer tous les projets");
		m.put("Import All Repositories", "Importer tous les dépôts");
		m.put("Import Group", "Importer le groupe");
		m.put("Import Issues", "Importer les problèmes");
		m.put("Import Option", "Option d'importation");
		m.put("Import Organization", "Importer l'organisation");
		m.put("Import Project", "Importer le projet");
		m.put("Import Projects", "Importer les projets");
		m.put("Import Repositories", "Importer les dépôts");
		m.put("Import Repository", "Importer le dépôt");
		m.put("Import Server", "Importer le serveur");
		m.put("Import Workspace", "Importer l'espace de travail");
		m.put("Import build spec elements (jobs, services, step templates and properties) from other projects. Imported elements are treated as if they are defined locally. Locally defined elements will override imported elements with same name", 
			"Importer les éléments de spécification de construction (emplois, services, modèles d'étape et propriétés) d'autres projets. Les éléments importés sont traités comme s'ils étaient définis localement. Les éléments définis localement remplaceront les éléments importés portant le même nom");
		m.put("Importing Issues from {0}", "Importation des problèmes depuis {0}");
		m.put("Importing from {0}", "Importation depuis {0}");
		m.put("Importing issues into currrent project. Please note that issue numbers will only be retained if the whole project fork graph does not have any issues to avoid duplicate issue numbers", 
			"Importation des problèmes dans le projet actuel. Veuillez noter que les numéros de problème ne seront conservés que si l'ensemble du graphe de fork du projet ne contient aucun problème pour éviter les doublons de numéros de problème");
		m.put("Importing projects from {0}", "Importation des projets depuis {0}");
		m.put("Imports", "Importations");
		m.put("In Projects", "Dans les projets");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"Dans le cas où le certificat de l'hôte IMAP est auto-signé ou que sa racine CA n'est pas acceptée, vous pouvez demander à OneDev de contourner la vérification du certificat. <b class='text-danger'>AVERTISSEMENT : </b> Dans un réseau non sécurisé, cela peut entraîner une attaque de type homme du milieu, et vous devriez plutôt <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>importer le certificat dans OneDev</a>");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"Dans le cas où le certificat de l'hôte SMTP est auto-signé ou que sa racine CA n'est pas acceptée, vous pouvez demander à OneDev de contourner la vérification du certificat. <b class='text-danger'>AVERTISSEMENT : </b> Dans un réseau non sécurisé, cela peut entraîner une attaque de type homme du milieu, et vous devriez plutôt <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>importer le certificat dans OneDev</a>");
		m.put("In case anonymous access is disabled or anonymous user does not have enough permission for a resource operation, you will need to authenticate by providing user name and password (or access token) via http basic auth header", 
			"En cas d'accès anonyme désactivé ou si l'utilisateur anonyme n'a pas suffisamment de permissions pour une opération de ressource, vous devrez vous authentifier en fournissant un nom d'utilisateur et un mot de passe (ou un jeton d'accès) via l'en-tête d'authentification basique http");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"Dans le cas où le cache n'est pas trouvé via la clé ci-dessus, OneDev parcourra les clés de chargement définies ici dans l'ordre jusqu'à ce qu'un cache correspondant soit trouvé dans la hiérarchie du projet. Un cache est considéré comme correspondant si sa clé est préfixée par la clé de chargement. Si plusieurs caches correspondent, le cache le plus récent sera retourné");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"Dans le cas où le cache doit être téléchargé, cette propriété spécifie le projet cible pour le téléchargement. Laissez vide pour le projet actuel");
		m.put("In case the pull request status is out of sync with underlying repository, you may synchronize them manually here", 
			"Dans le cas où l'état de la demande de tirage est désynchronisé avec le dépôt sous-jacent, vous pouvez les synchroniser manuellement ici");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"Dans le cas où l'appartenance au groupe d'utilisateurs est maintenue du côté du groupe, cette propriété spécifie le nœud de base pour la recherche de groupe. Par exemple : <i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"Dans le cas où la relation de groupe d'utilisateurs est maintenue du côté du groupe, ce filtre est utilisé pour déterminer les groupes auxquels appartient l'utilisateur actuel. Par exemple : <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. Dans cet exemple, <i>{0}</i> représente le DN de l'utilisateur actuel");
		m.put("In case you are using external issue tracker, you can define transformers to transform external issue references into external issue links in various places, such as commit messages and pull request descriptions", 
			"Dans le cas où vous utilisez un gestionnaire de problèmes externe, vous pouvez définir des transformateurs pour transformer les références de problèmes externes en liens de problèmes externes à divers endroits, tels que les messages de commit et les descriptions de demandes de tirage");
		m.put("In rare cases, your issues might be out of sync with workflow settings (undefined state/field etc.). Run integrity check below to find problems and get them fixed.", 
			"Dans de rares cas, vos problèmes peuvent être désynchronisés avec les paramètres de workflow (état/champ non défini, etc.). Exécutez une vérification d'intégrité ci-dessous pour trouver les problèmes et les résoudre.");
		m.put("Inbox Poll Setting", "Paramètre de sondage de la boîte de réception");
		m.put("Include Child Projects", "Inclure les projets enfants");
		m.put("Include Disabled", "Inclure les désactivés");
		m.put("Include Forks", "Inclure les forks");
		m.put("Include When Issue is Opened", "Inclure lorsque le problème est ouvert");
		m.put("Incompatibilities", "Incompatibilités");
		m.put("Inconsistent issuer in provider metadata and ID token", "Émetteur incohérent dans les métadonnées du fournisseur et le jeton ID");
		m.put("Indicator", "Indicateur");
		m.put("Inherit from parent", "Hériter du parent");
		m.put("Inherited", "Hérité");
		m.put("Input Spec", "Spécification d'entrée");
		m.put("Input URL", "URL d'entrée");
		m.put("Input allowed CORS origin, hit ENTER to add", "Saisissez l'origine CORS autorisée, appuyez sur ENTRÉE pour ajouter");
		m.put("Input revision", "Saisissez la révision");
		m.put("Input title", "Saisissez le titre");
		m.put("Input title here", "Saisissez le titre ici");
		m.put("Input user search base. Hit ENTER to add", "Saisissez la base de recherche utilisateur. Appuyez sur ENTRÉE pour ajouter");
		m.put("Input user search bases. Hit ENTER to add", "Saisissez les bases de recherche utilisateur. Appuyez sur ENTRÉE pour ajouter");
		m.put("Insert", "Insérer");
		m.put("Insert Image", "Insérer une image");
		m.put("Insert Link", "Insérer un lien");
		m.put("Insert link to this file", "Insérer un lien vers ce fichier");
		m.put("Insert this image", "Insérer cette image");
		m.put("Install Subscription Key", "Installer la clé d'abonnement");
		m.put("Integer", "Entier");
		m.put("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"L'accès interactif à la console web pour les tâches en cours est une fonctionnalité d'entreprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("Internal Database", "Base de données interne");
		m.put("Interpreter", "Interpréteur");
		m.put("Invalid GPG signature", "Signature GPG invalide");
		m.put("Invalid PCRE syntax", "Syntaxe PCRE invalide");
		m.put("Invalid access token: {0}", "Jeton d'accès invalide : {0}");
		m.put("Invalid credentials", "Identifiants invalides");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "Plage de dates invalide, attendu \"yyyy-MM-dd à yyyy-MM-dd\"");
		m.put("Invalid email address: {0}", "Adresse e-mail invalide : {0}");
		m.put("Invalid invitation code", "Code d'invitation invalide");
		m.put("Invalid issue date of ID token", "Date d'émission du jeton ID invalide");
		m.put("Invalid issue number: {0}", "Numéro d'émission invalide : {0}");
		m.put("Invalid pull request number: {0}", "Numéro de demande de tirage invalide : {0}");
		m.put("Invalid request path", "Chemin de requête invalide");
		m.put("Invalid selection, click for details", "Sélection invalide, cliquez pour plus de détails");
		m.put("Invalid ssh signature", "Signature ssh invalide");
		m.put("Invalid state response", "Réponse d'état invalide");
		m.put("Invalid state. Please make sure you are visiting OneDev using server url specified in system setting", 
			"État invalide. Veuillez vous assurer que vous visitez OneDev en utilisant l'URL du serveur spécifiée dans les paramètres système");
		m.put("Invalid subscription key", "Clé d'abonnement invalide");
		m.put("Invalid working period", "Période de travail invalide");
		m.put("Invitation sent to \"{0}\"", "Invitation envoyée à \"{0}\"");
		m.put("Invitation to \"{0}\" deleted", "Invitation à \"{0}\" supprimée");
		m.put("Invitations", "Invitations");
		m.put("Invitations sent", "Invitations envoyées");
		m.put("Invite", "Inviter");
		m.put("Invite Users", "Inviter des utilisateurs");
		m.put("Is Site Admin", "Est administrateur du site");
		m.put("Issue", "Problème");
		m.put("Issue #{0} deleted", "Problème #{0} supprimé");
		m.put("Issue Board", "Tableau des problèmes");
		m.put("Issue Boards", "Tableaux des problèmes");
		m.put("Issue Close States", "États de clôture des problèmes");
		m.put("Issue Creation Setting", "Paramètre de création de problème");
		m.put("Issue Creation Settings", "Paramètres de création de problème");
		m.put("Issue Custom Fields", "Champs personnalisés des problèmes");
		m.put("Issue Description", "Description du problème");
		m.put("Issue Description Templates", "Modèles de description de problème");
		m.put("Issue Details", "Détails du problème");
		m.put("Issue Field", "Champ de problème");
		m.put("Issue Field Mapping", "Mappage de champ de problème");
		m.put("Issue Field Mappings", "Mappages de champ de problème");
		m.put("Issue Field Set", "Ensemble de champs de problème");
		m.put("Issue Fields", "Champs de problème");
		m.put("Issue Filter", "Filtre de problème");
		m.put("Issue Import Option", "Option d'importation de problème");
		m.put("Issue Label Mapping", "Mappage d'étiquette de problème");
		m.put("Issue Label Mappings", "Mappages d'étiquette de problème");
		m.put("Issue Link", "Lien de problème");
		m.put("Issue Link Mapping", "Mappage de lien de problème");
		m.put("Issue Link Mappings", "Mappages de lien de problème");
		m.put("Issue Links", "Liens de problème");
		m.put("Issue Management", "Gestion des problèmes");
		m.put("Issue Notification", "Notification de problème");
		m.put("Issue Notification Template", "Modèle de notification de problème");
		m.put("Issue Notification Unsubscribed", "Notification de problème désabonnée");
		m.put("Issue Notification Unsubscribed Template", "Modèle de notification de problème désabonnée");
		m.put("Issue Pattern", "Modèle de problème");
		m.put("Issue Priority Mapping", "Mappage de priorité de problème");
		m.put("Issue Priority Mappings", "Mappages de priorité de problème");
		m.put("Issue Query", "Requête de problème");
		m.put("Issue Settings", "Paramètres de problème");
		m.put("Issue State", "État du problème");
		m.put("Issue State Mapping", "Mappage d'état de problème");
		m.put("Issue State Mappings", "Mappages d'état de problème");
		m.put("Issue State Transition", "Transition d'état de problème");
		m.put("Issue State Transitions", "Transitions d'état de problème");
		m.put("Issue States", "États de problème");
		m.put("Issue Statistics", "Statistiques des problèmes");
		m.put("Issue Stats", "Statistiques des problèmes");
		m.put("Issue Status Mapping", "Mappage de statut de problème");
		m.put("Issue Status Mappings", "Mappages de statut de problème");
		m.put("Issue Stopwatch Overdue", "Chronomètre de problème en retard");
		m.put("Issue Stopwatch Overdue Notification Template", "Modèle de notification de chronomètre de problème en retard");
		m.put("Issue Tag Mapping", "Mappage de balise de problème");
		m.put("Issue Tag Mappings", "Mappages de balise de problème");
		m.put("Issue Template", "Modèle de problème");
		m.put("Issue Transition ({0} -> {1})", "Transition de problème ({0} -> {1})");
		m.put("Issue Type Mapping", "Mappage de type de problème");
		m.put("Issue Type Mappings", "Mappages de type de problème");
		m.put("Issue Votes", "Votes sur les problèmes");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"Permission administrative de problème dans un projet, y compris les opérations en lot sur plusieurs problèmes");
		m.put("Issue count", "Nombre de problèmes");
		m.put("Issue in state", "Problème dans l'état");
		m.put("Issue list", "Liste des problèmes");
		m.put("Issue management not enabled in this project", "Gestion des problèmes non activée dans ce projet");
		m.put("Issue management permission required to move issues", "Permission de gestion des problèmes requise pour déplacer des problèmes");
		m.put("Issue not exist or access denied", "Problème inexistant ou accès refusé");
		m.put("Issue number", "Numéro de problème");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"La surveillance des requêtes de problème n'affecte que les nouveaux problèmes. Pour gérer le statut de surveillance des problèmes existants en lot, filtrez les problèmes par statut de surveillance sur la page des problèmes, puis prenez les mesures appropriées");
		m.put("Issue state duration statistics", "Statistiques de durée d'état de problème");
		m.put("Issue state frequency statistics", "Statistiques de fréquence d'état de problème");
		m.put("Issue state trend statistics", "Statistiques de tendance d'état de problème");
		m.put("Issue statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Les statistiques des problèmes sont une fonctionnalité d'entreprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("Issue workflow changed, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliation</a> needs to be performed to make data consistent. You may do this after making all necessary changes", 
			"Le workflow des problèmes a changé, <a wicket:id=\"reconcile\" class=\"link-primary\">une réconciliation</a> doit être effectuée pour rendre les données cohérentes. Vous pouvez le faire après avoir apporté toutes les modifications nécessaires");
		m.put("Issues", "Problèmes");
		m.put("Issues can be created in this project by sending email to this address", "Les problèmes peuvent être créés dans ce projet en envoyant un e-mail à cette adresse");
		m.put("Issues copied", "Problèmes copiés");
		m.put("Issues moved", "Problèmes déplacés");
		m.put("Italic", "Italique");
		m.put("Iteration", "Itération");
		m.put("Iteration \"{0}\" closed", "Itération \"{0}\" fermée");
		m.put("Iteration \"{0}\" deleted", "Itération \"{0}\" supprimée");
		m.put("Iteration \"{0}\" is closed", "Itération \"{0}\" est fermée");
		m.put("Iteration \"{0}\" is reopened", "Itération \"{0}\" est rouverte");
		m.put("Iteration \"{0}\" reopened", "Itération \"{0}\" rouverte");
		m.put("Iteration Edit Bean", "Itération Modifier Bean");
		m.put("Iteration Name", "Nom de l'itération");
		m.put("Iteration Names", "Noms des itérations");
		m.put("Iteration Prefix", "Préfixe de l'itération");
		m.put("Iteration list", "Liste des itérations");
		m.put("Iteration saved", "Itération enregistrée");
		m.put("Iteration spans too long to show burndown chart", "L'itération s'étend trop longtemps pour afficher le graphique de burndown");
		m.put("Iteration start and due date should be specified to show burndown chart", "La date de début et la date d'échéance de l'itération doivent être spécifiées pour afficher le graphique de burndown");
		m.put("Iteration start date should be before due date", "La date de début de l'itération doit être antérieure à la date d'échéance");
		m.put("Iterations", "Itérations");
		m.put("Iterations Bean", "Itérations Bean");
		m.put("JIRA Issue Priority", "Priorité des problèmes JIRA");
		m.put("JIRA Issue Status", "Statut des problèmes JIRA");
		m.put("JIRA Issue Type", "Type de problème JIRA");
		m.put("JIRA Project", "Projet JIRA");
		m.put("JIRA Projects to Import", "Projets JIRA à importer");
		m.put("JUnit Report", "Rapport JUnit");
		m.put("JVM", "JVM");
		m.put("JaCoCo Coverage Report", "Rapport de couverture JaCoCo");
		m.put("Jest Coverage Report", "Rapport de couverture Jest");
		m.put("Jest Test Report", "Rapport de test Jest");
		m.put("Job", "Tâche");
		m.put("Job \"{0}\" associated with the build not found.", "Tâche \"{0}\" associée à la construction introuvable.");
		m.put("Job Authorization", "Autorisation de tâche");
		m.put("Job Cache Management", "Gestion du cache de tâche");
		m.put("Job Dependencies", "Dépendances de tâche");
		m.put("Job Dependency", "Dépendance de tâche");
		m.put("Job Executor", "Exécuteur de tâche");
		m.put("Job Executor Bean", "Exécuteur de tâche Bean");
		m.put("Job Executors", "Exécuteurs de tâche");
		m.put("Job Name", "Nom de la tâche");
		m.put("Job Names", "Noms des tâches");
		m.put("Job Param", "Paramètre de travail");
		m.put("Job Parameters", "Paramètres de travail");
		m.put("Job Privilege", "Privilège de tâche");
		m.put("Job Privileges", "Privilèges de tâche");
		m.put("Job Properties", "Propriétés de tâche");
		m.put("Job Properties Bean", "Propriétés de tâche Bean");
		m.put("Job Property", "Propriété de tâche");
		m.put("Job Secret", "Secret de tâche");
		m.put("Job Secret Edit Bean", "Modifier le secret de tâche Bean");
		m.put("Job Secrets", "Secrets de tâche");
		m.put("Job Trigger", "Déclencheur de tâche");
		m.put("Job Trigger Bean", "Déclencheur de tâche Bean");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"Permission administrative de tâche, y compris la suppression des constructions de la tâche. Cela implique toutes les autres permissions de tâche");
		m.put("Job cache \"{0}\" deleted", "Cache de tâche \"{0}\" supprimé");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"Les dépendances de tâche déterminent l'ordre et la concurrence lors de l'exécution de différentes tâches. Vous pouvez également spécifier les artefacts à récupérer des tâches en amont");
		m.put("Job executor tested successfully", "Exécuteur de tâche testé avec succès");
		m.put("Job executors", "Exécuteurs de tâche");
		m.put("Job name", "Nom de la tâche");
		m.put("Job properties saved", "Propriétés de tâche enregistrées");
		m.put("Job secret \"{0}\" deleted", "Secret de tâche \"{0}\" supprimé");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission", 
			"Le secret de tâche 'access-token' doit être défini dans les paramètres de construction du projet comme un jeton d'accès avec la permission de package ${permission}");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package read permission", 
			"Le secret de tâche 'access-token' doit être défini dans les paramètres de construction du projet comme un jeton d'accès avec la permission de lecture de package");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package write permission", 
			"Le secret de tâche 'access-token' doit être défini dans les paramètres de construction du projet comme un jeton d'accès avec la permission d'écriture de package");
		m.put("Job token", "Jeton de tâche");
		m.put("Job will run on head commit of default branch", "La tâche s'exécutera sur le commit principal de la branche par défaut");
		m.put("Job will run on head commit of target branch", "La tâche s'exécutera sur le commit principal de la branche cible");
		m.put("Job will run on merge commit of target branch and source branch", "La tâche s'exécutera sur le commit de fusion de la branche cible et de la branche source");
		m.put("Job will run on merge commit of target branch and source branch.<br><b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"La tâche s'exécutera sur le commit de fusion de la branche cible et de la branche source.<br><b class='text-info'>NOTE :</b> À moins qu'une règle de protection de branche ne l'exige, ce déclencheur ignorera les commits contenant un message avec <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, ou <code>[no job]</code>");
		m.put("Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"La tâche s'exécutera lorsque du code est commis. <b class='text-info'>NOTE :</b> Ce déclencheur ignorera les commits contenant un message avec <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, ou <code>[no job]</code>");
		m.put("Job workspace", "Espace de travail de tâche");
		m.put("Jobs", "Tâches");
		m.put("Jobs marked with <span class=\"text-danger\">*</span> are required to be successful", 
			"Les tâches marquées avec <span class=\"text-danger\">*</span> doivent réussir");
		m.put("Jobs required to be successful on merge commit: ", "Tâches requises pour réussir sur le commit de fusion :");
		m.put("Jobs required to be successful: ", "Tâches requises pour réussir :");
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"Les tâches avec le même groupe séquentiel et exécuteur seront exécutées séquentiellement. Par exemple, vous pouvez spécifier cette propriété comme <tt>@project_path@:prod</tt> pour les tâches exécutées par le même exécuteur et déployant dans l'environnement de production du projet actuel afin d'éviter des déploiements conflictuels");
		m.put("Key", "Clé");
		m.put("Key Fingerprint", "Empreinte de clé");
		m.put("Key ID", "ID de clé");
		m.put("Key Secret", "Secret de clé");
		m.put("Key Type", "Type de clé");
		m.put("Kubectl Config File", "Fichier de configuration Kubectl");
		m.put("Kubernetes", "Kubernetes");
		m.put("Kubernetes Executor", "Exécuteur Kubernetes");
		m.put("LDAP URL", "URL LDAP");
		m.put("Label", "Étiquette");
		m.put("Label Management", "Gestion des étiquettes");
		m.put("Label Management Bean", "Gestion des étiquettes Bean");
		m.put("Label Name", "Nom de l'étiquette");
		m.put("Label Spec", "Spécification de l'étiquette");
		m.put("Label Value", "Valeur de l'étiquette");
		m.put("Labels", "Étiquettes");
		m.put("Labels Bean", "Étiquettes Bean");
		m.put("Labels can be defined in Administration / Label Management", "Les étiquettes peuvent être définies dans Administration / Gestion des étiquettes");
		m.put("Labels have been updated", "Les étiquettes ont été mises à jour");
		m.put("Language", "Langue");
		m.put("Last Accessed", "Dernier accès");
		m.put("Last Finished of Specified Job", "Dernière fin de tâche spécifiée");
		m.put("Last Modified", "Dernière modification");
		m.put("Last Published", "Dernière publication");
		m.put("Last Update", "Dernière mise à jour");
		m.put("Last commit", "Dernier commit");
		m.put("Last commit hash", "Hash du dernier commit");
		m.put("Last commit index version", "Version de l'index du dernier commit");
		m.put("Leaf Projects", "Projets feuilles");
		m.put("Least branch coverage", "Couverture de branche la plus faible");
		m.put("Least line coverage", "Couverture de ligne la plus faible");
		m.put("Leave a comment", "Laisser un commentaire");
		m.put("Leave a note", "Laisser une note");
		m.put("Left", "Gauche");
		m.put("Less", "Moins");
		m.put("License Agreement", "Accord de Licence");
		m.put("License Setting", "Paramètre de Licence");
		m.put("Licensed To", "Licencié À");
		m.put("Licensed To:", "Licencié À :");
		m.put("Line", "Ligne");
		m.put("Line changes", "Modifications de Ligne");
		m.put("Line: ", "Ligne :");
		m.put("Lines", "Lignes");
		m.put("Link", "Lien");
		m.put("Link Existing User", "Lier un utilisateur existant");
		m.put("Link Spec", "Spécification de Lien");
		m.put("Link Spec Opposite", "Spécification de Lien Opposé");
		m.put("Link Text", "Texte du Lien");
		m.put("Link URL", "URL du Lien");
		m.put("Link URL should be specified", "L'URL du lien doit être spécifiée");
		m.put("Link User Bean", "Lier l'utilisateur Bean");
		m.put("Linkable Issues", "Problèmes Liables");
		m.put("Linkable Issues On the Other Side", "Problèmes Liables de l'Autre Côté");
		m.put("Links", "Liens");
		m.put("Links can be used to associate different issues. For instance, an issue can be linked to sub issues or related issues", 
			"Les liens peuvent être utilisés pour associer différents problèmes. Par exemple, un problème peut être lié à des sous-problèmes ou à des problèmes connexes");
		m.put("List", "Liste");
		m.put("Literal", "Littéral");
		m.put("Literal default value", "Valeur par défaut littérale");
		m.put("Literal value", "Valeur littérale");
		m.put("Load Keys", "Charger les Clés");
		m.put("Loading emojis...", "Chargement des emojis...");
		m.put("Loading...", "Chargement...");
		m.put("Log", "Journal");
		m.put("Log Work", "Journal de Travail");
		m.put("Log not available for offline agent", "Journal non disponible pour l'agent hors ligne");
		m.put("Log work", "Journal de travail");
		m.put("Login Name", "Nom de Connexion");
		m.put("Login and generate refresh token", "Connexion et génération de jeton de rafraîchissement");
		m.put("Login name already used by another account", "Nom de connexion déjà utilisé par un autre compte");
		m.put("Login name or email", "Nom de connexion ou email");
		m.put("Login name or email address", "Nom de connexion ou adresse email");
		m.put("Login to OneDev docker registry", "Connexion au registre docker de OneDev");
		m.put("Login to comment", "Connexion pour commenter");
		m.put("Login to comment on selection", "Connexion pour commenter la sélection");
		m.put("Login to vote", "Connexion pour voter");
		m.put("Login user needs to have package write permission over the project below", "L'utilisateur connecté doit avoir l'autorisation d'écriture de package sur le projet ci-dessous");
		m.put("Login with {0}", "Connexion avec {0}");
		m.put("Logo for Dark Mode", "Logo pour le Mode Sombre");
		m.put("Logo for Light Mode", "Logo pour le Mode Clair");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"Jeton de rafraîchissement longue durée du compte ci-dessus qui sera utilisé pour générer un jeton d'accès pour accéder à Gmail. <b class='text-info'>CONSEILS : </b> vous pouvez utiliser le bouton à droite de ce champ pour générer un jeton de rafraîchissement. Notez que chaque fois que l'identifiant client, le secret client ou le nom du compte est modifié, le jeton de rafraîchissement doit être régénéré");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"Jeton de rafraîchissement longue durée du compte ci-dessus qui sera utilisé pour générer un jeton d'accès pour accéder au serveur de messagerie Office 365. <b class='text-info'>CONSEILS : </b> vous pouvez utiliser le bouton à droite de ce champ pour vous connecter à votre compte Office 365 et générer un jeton de rafraîchissement. Notez que chaque fois que l'identifiant du locataire, l'identifiant client, le secret client ou le nom principal de l'utilisateur est modifié, le jeton de rafraîchissement doit être régénéré");
		m.put("Longest Duration First", "Durée la Plus Longue en Premier");
		m.put("Looks like a GPG signature but without necessary data", "Ressemble à une signature GPG mais sans les données nécessaires");
		m.put("Low", "Faible");
		m.put("Low Severity", "Gravité Faible");
		m.put("MERGED", "FUSIONNÉ");
		m.put("MS Teams Notifications", "Notifications MS Teams");
		m.put("Mail", "Mail");
		m.put("Mail Connector", "Connecteur de Mail");
		m.put("Mail Connector Bean", "Bean de Connecteur de Mail");
		m.put("Mail Service", "Service de Messagerie");
		m.put("Mail Service Test", "Test du Service de Messagerie");
		m.put("Mail service not configured", "Service de messagerie non configuré");
		m.put("Mail service settings saved", "Paramètres du service de messagerie enregistrés");
		m.put("Make sure <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 or higher</a> is installed", 
			"Assurez-vous que <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 ou supérieur</a> est installé");
		m.put("Make sure current user has permission to run docker containers", "Assurez-vous que l'utilisateur actuel a l'autorisation d'exécuter des conteneurs docker");
		m.put("Make sure docker engine is installed and docker command line is available in system path", 
			"Assurez-vous que le moteur docker est installé et que la ligne de commande docker est disponible dans le chemin système");
		m.put("Make sure git version 2.11.1 or higher is installed and available in system path", "Assurez-vous que la version git 2.11.1 ou supérieure est installée et disponible dans le chemin système");
		m.put("Make sure git-lfs is installed and available in system path if you want to retrieve LFS files", 
			"Assurez-vous que git-lfs est installé et disponible dans le chemin système si vous souhaitez récupérer des fichiers LFS");
		m.put("Make sure the access token has package read permission over the project", "Assurez-vous que le jeton d'accès a l'autorisation de lecture de package sur le projet");
		m.put("Make sure the access token has package write permission over the project", "Assurez-vous que le jeton d'accès a l'autorisation d'écriture de package sur le projet");
		m.put("Make sure the access token has package write permission over the project. Also make sure to run command <code>chmod 0600 $HOME/.gem/credentials</code> after creating the file", 
			"Assurez-vous que le jeton d'accès a l'autorisation d'écriture de package sur le projet. Assurez-vous également d'exécuter la commande <code>chmod 0600 $HOME/.gem/credentials</code> après avoir créé le fichier");
		m.put("Make sure the account has package ${permission} permission over the project", "Assurez-vous que le compte a l'autorisation de package ${permission} sur le projet");
		m.put("Make sure the account has package read permission over the project", "Assurez-vous que le compte a l'autorisation de lecture de package sur le projet");
		m.put("Make sure the user has package write permission over the project", "Assurez-vous que l'utilisateur a l'autorisation d'écriture de package sur le projet");
		m.put("Malformed %sbase query", "Requête %sbase malformée");
		m.put("Malformed %squery", "Requête %s malformée");
		m.put("Malformed build spec (import project: {0}, import revision: {1})", "Spécification de build malformée (importer projet : {0}, importer révision : {1})");
		m.put("Malformed email address", "Adresse email malformée");
		m.put("Malformed filter", "Filtre malformé");
		m.put("Malformed name filter", "Filtre de nom malformé");
		m.put("Malformed query", "Requête malformée");
		m.put("Malformed ssh signature", "Signature ssh malformée");
		m.put("Malformed test suite filter", "Filtre de suite de tests malformé");
		m.put("Manage Job", "Gérer le Job");
		m.put("Manager DN", "DN du Manager");
		m.put("Manager Password", "Mot de Passe du Manager");
		m.put("Manifest blob unknown", "Blob de manifeste inconnu");
		m.put("Manifest invalid", "Manifeste invalide");
		m.put("Manifest unknown", "Manifeste inconnu");
		m.put("Many commands print outputs with ANSI colors in TTY mode to help identifying problems easily. However some commands running in this mode may wait for user input to cause build hanging. This can normally be fixed by adding extra options to the command", 
			"De nombreuses commandes impriment des sorties avec des couleurs ANSI en mode TTY pour aider à identifier facilement les problèmes. Cependant, certaines commandes exécutées dans ce mode peuvent attendre une entrée utilisateur, ce qui peut entraîner un blocage de la construction. Cela peut généralement être corrigé en ajoutant des options supplémentaires à la commande");
		m.put("Mark a property archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived properties will not be shown by default", 
			"Marquez une propriété comme archivée si elle n'est plus utilisée par la spécification de build actuelle, mais doit encore exister pour reproduire les anciens builds. Les propriétés archivées ne seront pas affichées par défaut");
		m.put("Mark a secret archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived secrets will not be shown by default", 
			"Marquez un secret comme archivé s'il n'est plus utilisé par la spécification de build actuelle, mais doit encore exister pour reproduire les anciens builds. Les secrets archivés ne seront pas affichés par défaut");
		m.put("Markdown", "Markdown");
		m.put("Markdown Report", "Rapport Markdown");
		m.put("Markdown from file", "Markdown à partir du fichier");
		m.put("Maven(s)", "Maven(s)");
		m.put("Max Code Search Entries", "Entrées Maximales de Recherche de Code");
		m.put("Max Commit Message Line Length", "Longueur Maximale de Ligne de Message de Commit");
		m.put("Max Git LFS File Size (MB)", "Taille Maximale de Fichier Git LFS (MB)");
		m.put("Max Retries", "Nombre Maximal de Tentatives");
		m.put("Max Upload File Size (MB)", "Taille Maximale de Fichier Téléchargé (MB)");
		m.put("Max Value", "Valeur Maximale");
		m.put("Maximum number of entries to return when search code in repository", "Nombre maximal d'entrées à retourner lors de la recherche de code dans le dépôt");
		m.put("Maximum of retries before giving up", "Nombre maximal de tentatives avant d'abandonner");
		m.put("May not be empty", "Ne peut pas être vide");
		m.put("Medium", "Moyen");
		m.put("Medium Severity", "Gravité Moyenne");
		m.put("Members", "Membres");
		m.put("Memory", "Mémoire");
		m.put("Memory Limit", "Limite de mémoire");
		m.put("Memory Request", "Requête de mémoire");
		m.put("Mention Someone", "Mentionner quelqu'un");
		m.put("Mention someone", "Mentionner quelqu'un");
		m.put("Merge", "Fusionner");
		m.put("Merge Strategy", "Stratégie de fusion");
		m.put("Merge Target Branch into Source Branch", "Fusionner la branche cible dans la branche source");
		m.put("Merge branch \"{0}\" into branch \"{1}\"", "Fusionner la branche \"{0}\" dans la branche \"{1}\"");
		m.put("Merge branch \"{0}\" of project \"{1}\" into branch \"{2}\"", "Fusionner la branche \"{0}\" du projet \"{1}\" dans la branche \"{2}\"");
		m.put("Merge preview not calculated yet", "Aperçu de la fusion non encore calculé");
		m.put("Merged", "Fusionné");
		m.put("Merged pull request \"{0}\" ({1})", "Requête de fusion \"{0}\" fusionnée ({1})");
		m.put("Merges pull request", "Fusionne la requête de tirage");
		m.put("Meta", "Méta");
		m.put("Meta Info", "Infos méta");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "Valeur minimale");
		m.put("Minimum length of the password", "Longueur minimale du mot de passe");
		m.put("Missing Commit", "Commit manquant");
		m.put("Missing Commits", "Commits manquants");
		m.put("Month", "Mois");
		m.put("Months", "Mois");
		m.put("Months to Display", "Mois à afficher");
		m.put("More", "Plus");
		m.put("More Options", "Plus d'options");
		m.put("More Settings", "Plus de paramètres");
		m.put("More commits", "Plus de commits");
		m.put("More info", "Plus d'infos");
		m.put("More operations", "Plus d'opérations");
		m.put("Most branch coverage", "Couverture de branche la plus élevée");
		m.put("Most line coverage", "Couverture de ligne la plus élevée");
		m.put("Most probably there are import errors in the <a wicket:id=\"buildSpec\">build spec</a>", 
			"Il y a probablement des erreurs d'importation dans le <a wicket:id=\"buildSpec\">build spec</a>");
		m.put("Mount Docker Sock", "Monter Docker Sock");
		m.put("Move All Queried Issues To...", "Déplacer toutes les issues interrogées vers...");
		m.put("Move All Queried Projects To...", "Déplacer tous les projets interrogés vers...");
		m.put("Move Selected Issues To...", "Déplacer les issues sélectionnées vers...");
		m.put("Move Selected Projects To...", "Déplacer les projets sélectionnés vers...");
		m.put("Multiple Lines", "Lignes multiples");
		m.put("Multiple On the Other Side", "Multiples de l'autre côté");
		m.put("Must not be empty", "Ne doit pas être vide");
		m.put("My Access Tokens", "Mes jetons d'accès");
		m.put("My Basic Settings", "Mes paramètres de base");
		m.put("My Email Addresses", "Mes adresses e-mail");
		m.put("My GPG Keys", "Mes clés GPG");
		m.put("My Profile", "Mon profil");
		m.put("My SSH Keys", "Mes clés SSH");
		m.put("My SSO Accounts", "Mes comptes SSO");
		m.put("Mypy Report", "Rapport Mypy");
		m.put("N/A", "N/A");
		m.put("NPM(s)", "NPM(s)");
		m.put("Name", "Nom");
		m.put("Name Of Empty Value", "Nom de la valeur vide");
		m.put("Name On the Other Side", "Nom de l'autre côté");
		m.put("Name Prefix", "Préfixe du nom");
		m.put("Name already used by another access token of the owner", "Nom déjà utilisé par un autre jeton d'accès du propriétaire");
		m.put("Name already used by another link", "Nom déjà utilisé par un autre lien");
		m.put("Name and name on the other side should be different", "Le nom et le nom de l'autre côté doivent être différents");
		m.put("Name containing spaces or starting with dash needs to be quoted", "Un nom contenant des espaces ou commençant par un tiret doit être entre guillemets");
		m.put("Name invalid", "Nom invalide");
		m.put("Name of the link", "Nom du lien");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"Nom du lien de l'autre côté. Par exemple, si le nom est <tt>sous-issues</tt>, le nom de l'autre côté peut être <tt>issue parent</tt>");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"Le nom du fournisseur servira à deux fins : <ul><li>Affichage sur le bouton de connexion<li>Formation de l'URL de rappel d'autorisation qui sera <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>");
		m.put("Name reversely", "Nom inversé");
		m.put("Name unknown", "Nom inconnu");
		m.put("Name your file", "Nommer votre fichier");
		m.put("Named Agent Queries Bean", "Bean de requêtes d'agent nommé");
		m.put("Named Agent Query", "Requête d'agent nommé");
		m.put("Named Build Queries Bean", "Bean de requêtes de build nommé");
		m.put("Named Build Query", "Requête de build nommé");
		m.put("Named Code Comment Queries Bean", "Bean de requêtes de commentaires de code nommé");
		m.put("Named Code Comment Query", "Requête de commentaire de code nommé");
		m.put("Named Commit Queries Bean", "Bean de requêtes de commit nommé");
		m.put("Named Commit Query", "Requête de commit nommé");
		m.put("Named Element", "Élément nommé");
		m.put("Named Issue Queries Bean", "Bean de requêtes d'issues nommé");
		m.put("Named Issue Query", "Requête d'issue nommé");
		m.put("Named Pack Queries Bean", "Bean de requêtes de pack nommé");
		m.put("Named Pack Query", "Requête de pack nommé");
		m.put("Named Project Queries Bean", "Bean de requêtes de projet nommé");
		m.put("Named Project Query", "Requête de projet nommé");
		m.put("Named Pull Request Queries Bean", "Bean de requêtes de pull request nommé");
		m.put("Named Pull Request Query", "Requête de pull request nommé");
		m.put("Named Query", "Requête nommée");
		m.put("Network Options", "Options réseau");
		m.put("Never", "Jamais");
		m.put("Never expire", "Ne jamais expirer");
		m.put("New Board", "Nouveau tableau");
		m.put("New Invitation Bean", "Bean d'invitation nouveau");
		m.put("New Issue", "Nouvelle issue");
		m.put("New Password", "Nouveau mot de passe");
		m.put("New State", "Nouvel état");
		m.put("New User Bean", "Bean d'utilisateur nouveau");
		m.put("New Value", "Nouvelle valeur");
		m.put("New issue board created", "Nouveau tableau d'issues créé");
		m.put("New project created", "Nouveau projet créé");
		m.put("New user created", "Nouvel utilisateur créé");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"Nouvelle version disponible. Rouge pour mise à jour de sécurité/critique, jaune pour correction de bug, bleu pour mise à jour de fonctionnalité. Cliquez pour afficher les changements. Désactiver dans les paramètres système");
		m.put("Next", "Suivant");
		m.put("Next commit", "Prochain commit");
		m.put("Next {0}", "Prochain {0}");
		m.put("No", "Non");
		m.put("No Activity Days", "Jours sans activité");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"Aucune clé SSH configurée dans votre compte. Vous pouvez <a wicket:id=\"sshKeys\" class=\"link-primary\">ajouter une clé</a> ou passer à l'url <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a>");
		m.put("No SSL", "Pas de SSL");
		m.put("No accessible reports", "Aucun rapport accessible");
		m.put("No activity for some time", "Aucune activité depuis un certain temps");
		m.put("No agents to pause", "Aucun agent à mettre en pause");
		m.put("No agents to remove", "Aucun agent à supprimer");
		m.put("No agents to restart", "Aucun agent à redémarrer");
		m.put("No agents to resume", "Aucun agent à reprendre");
		m.put("No aggregation", "Aucune agrégation");
		m.put("No any", "Aucun");
		m.put("No any matches", "Aucune correspondance");
		m.put("No applicable transitions or no permission to transit", "Aucune transition applicable ou aucune permission pour effectuer la transition");
		m.put("No attributes defined (can only be edited when agent is online)", "Aucun attribut défini (peut être modifié uniquement lorsque l'agent est en ligne)");
		m.put("No audits", "Aucun audit");
		m.put("No authorized job secret found (project: {0}, job secret: {1})", "Aucun secret de tâche autorisé trouvé (projet : {0}, secret de tâche : {1})");
		m.put("No branch to cherry-pick to", "Aucune branche pour effectuer un cherry-pick");
		m.put("No branch to revert on", "Aucune branche pour effectuer un revert");
		m.put("No branches Found", "Aucune branche trouvée");
		m.put("No branches found", "Aucune branche trouvée");
		m.put("No build in query context", "Aucune construction dans le contexte de la requête");
		m.put("No builds", "Aucune construction");
		m.put("No builds to cancel", "Aucune construction à annuler");
		m.put("No builds to delete", "Aucune construction à supprimer");
		m.put("No builds to re-run", "Aucune construction à relancer");
		m.put("No comment", "Aucun commentaire");
		m.put("No comments to delete", "Aucun commentaire à supprimer");
		m.put("No comments to set as read", "Aucun commentaire à marquer comme lu");
		m.put("No comments to set resolved", "Aucun commentaire à marquer comme résolu");
		m.put("No comments to set unresolved", "Aucun commentaire à marquer comme non résolu");
		m.put("No commit in query context", "Aucun commit dans le contexte de la requête");
		m.put("No config file", "Aucun fichier de configuration");
		m.put("No current build in query context", "Aucune construction actuelle dans le contexte de la requête");
		m.put("No current commit in query context", "Aucun commit actuel dans le contexte de la requête");
		m.put("No current pull request in query context", "Aucune pull request actuelle dans le contexte de la requête");
		m.put("No data", "Aucune donnée");
		m.put("No default branch", "Aucune branche par défaut");
		m.put("No default group", "Aucun groupe par défaut");
		m.put("No default roles", "Aucun rôle par défaut");
		m.put("No default value", "Aucune valeur par défaut");
		m.put("No description", "Aucune description");
		m.put("No diffs", "Aucune différence");
		m.put("No diffs to navigate", "Aucune différence à naviguer");
		m.put("No directories to skip", "Aucun répertoire à ignorer");
		m.put("No disallowed file types", "Aucun type de fichier non autorisé");
		m.put("No executors defined. Jobs will use auto-discovered executors instead", "Aucun exécuteur défini. Les travaux utiliseront des exécuteurs découverts automatiquement à la place");
		m.put("No external password authenticator", "Aucun authentificateur de mot de passe externe");
		m.put("No external password authenticator to authenticate user \"{0}\"", "Aucun authentificateur de mot de passe externe pour authentifier l'utilisateur \"{0}\"");
		m.put("No fields to prompt", "Aucun champ à demander");
		m.put("No fields to remove", "Aucun champ à supprimer");
		m.put("No file attachments", "Aucune pièce jointe de fichier");
		m.put("No group by", "Aucun regroupement");
		m.put("No groups claim returned", "Aucune revendication de groupe retournée");
		m.put("No groups to remove from", "Aucun groupe à retirer");
		m.put("No ignore file", "Aucun fichier d'ignorance");
		m.put("No ignored licenses", "Aucune licence ignorée");
		m.put("No image attachments", "Aucune pièce jointe d'image");
		m.put("No imports defined", "Aucune importation définie");
		m.put("No issue boards defined", "Aucun tableau de problèmes défini");
		m.put("No issues in iteration", "Aucun problème dans l'itération");
		m.put("No issues to copy", "Aucun problème à copier");
		m.put("No issues to delete", "Aucun problème à supprimer");
		m.put("No issues to edit", "Aucun problème à modifier");
		m.put("No issues to export", "Aucun problème à exporter");
		m.put("No issues to move", "Aucun problème à déplacer");
		m.put("No issues to set as read", "Aucun problème à marquer comme lu");
		m.put("No issues to sync estimated/spent time", "Aucun problème à synchroniser le temps estimé/passé");
		m.put("No issues to watch/unwatch", "Aucun problème à surveiller/ne pas surveiller");
		m.put("No jobs defined", "Aucune tâche définie");
		m.put("No jobs found", "Aucun job trouvé");
		m.put("No limit", "Aucune limite");
		m.put("No mail service", "Aucun service de messagerie");
		m.put("No obvious changes", "Aucun changement évident");
		m.put("No one", "Personne");
		m.put("No packages to delete", "Aucun paquet à supprimer");
		m.put("No parent", "Aucun parent");
		m.put("No previous successful build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a> to calculate fixed issues since", 
			"Aucune construction réussie précédente sur <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">le même flux</a> pour calculer les problèmes résolus depuis");
		m.put("No projects found", "Aucun projet trouvé");
		m.put("No projects to delete", "Aucun projet à supprimer");
		m.put("No projects to modify", "Aucun projet à modifier");
		m.put("No projects to move", "Aucun projet à déplacer");
		m.put("No properties defined", "Aucune propriété définie");
		m.put("No proxy", "Aucun proxy");
		m.put("No pull request in query context", "Aucune pull request dans le contexte de la requête");
		m.put("No pull requests to delete", "Aucune pull request à supprimer");
		m.put("No pull requests to discard", "Aucune pull request à rejeter");
		m.put("No pull requests to set as read", "Aucune pull request à marquer comme lue");
		m.put("No pull requests to watch/unwatch", "Aucune pull request à surveiller/ne pas surveiller");
		m.put("No refs to build on behalf of", "Aucune référence à construire au nom de");
		m.put("No required services", "Aucun service requis");
		m.put("No response body", "Aucun corps de réponse");
		m.put("No secret config", "Aucune configuration secrète");
		m.put("No services defined", "Aucun service défini");
		m.put("No start/due date", "Aucune date de début/d'échéance");
		m.put("No step templates defined", "Aucun modèle d'étape défini");
		m.put("No suggestions", "Aucune suggestion");
		m.put("No tags found", "Aucune étiquette trouvée");
		m.put("No timesheets defined", "Aucune feuille de temps définie");
		m.put("No user found with login name or email: ", "Aucun utilisateur trouvé avec le nom de connexion ou l'e-mail :");
		m.put("No users to convert to service accounts", "Aucun utilisateur à convertir en comptes de service");
		m.put("No users to delete", "Aucun utilisateur à supprimer");
		m.put("No users to disable", "Aucun utilisateur à désactiver");
		m.put("No users to enable", "Aucun utilisateur à activer");
		m.put("No users to remove from group", "Aucun utilisateur à retirer du groupe");
		m.put("No valid query to show progress", "Aucune requête valide pour afficher la progression");
		m.put("No valid signature for head commit", "Aucune signature valide pour le commit principal");
		m.put("No valid signature for head commit of target branch", "Aucune signature valide pour le commit principal de la branche cible");
		m.put("No value", "Aucune valeur");
		m.put("No verified primary email address", "Aucune adresse email principale vérifiée");
		m.put("Node Selector", "Sélecteur de nœud");
		m.put("Node Selector Entry", "Entrée du sélecteur de nœud");
		m.put("None", "Aucun");
		m.put("Not Active Since", "Non actif depuis");
		m.put("Not Used Since", "Non utilisé depuis");
		m.put("Not a verified email of signing GPG key", "Pas un email vérifié de la clé GPG de signature");
		m.put("Not a verified email of signing ssh key owner", "Pas un email vérifié du propriétaire de la clé ssh de signature");
		m.put("Not allowed file type: {0}", "Type de fichier non autorisé : {0}");
		m.put("Not assigned", "Non assigné");
		m.put("Not authorized to create project under \"{0}\"", "Non autorisé à créer un projet sous \"{0}\"");
		m.put("Not authorized to create root project", "Non autorisé à créer un projet racine");
		m.put("Not authorized to move project under this parent", "Non autorisé à déplacer le projet sous ce parent");
		m.put("Not authorized to set as root project", "Non autorisé à définir comme projet racine");
		m.put("Not covered", "Non couvert");
		m.put("Not covered by any test", "Non couvert par aucun test");
		m.put("Not displaying any fields", "Ne pas afficher de champs");
		m.put("Not displaying any links", "Ne pas afficher de liens");
		m.put("Not passed", "Non validé");
		m.put("Not rendered in failsafe mode", "Non rendu en mode de secours");
		m.put("Not run", "Non exécuté");
		m.put("Not specified", "Non spécifié");
		m.put("Note", "Note");
		m.put("Nothing to preview", "Rien à prévisualiser");
		m.put("Notification", "Notification");
		m.put("Notifications", "Notifications");
		m.put("Notify Build Events", "Notifier les événements de construction");
		m.put("Notify Code Comment Events", "Notifier les événements de commentaire de code");
		m.put("Notify Code Push Events", "Notifier les événements de push de code");
		m.put("Notify Issue Events", "Notifier les événements de problème");
		m.put("Notify Own Events", "Notifier ses propres événements");
		m.put("Notify Pull Request Events", "Notifier les événements de pull request");
		m.put("Notify Users", "Notifier les utilisateurs");
		m.put("Ntfy.sh Notifications", "Notifications Ntfy.sh");
		m.put("NuGet(s)", "NuGet(s)");
		m.put("NuSpec", "NuSpec");
		m.put("Number of CPU Cores", "Nombre de cœurs CPU");
		m.put("Number of SSH Keys", "Nombre de clés SSH");
		m.put("Number of builds to preserve", "Nombre de builds à conserver");
		m.put("Number of project replicas, including primary and backups", "Nombre de répliques de projet, y compris primaires et sauvegardes");
		m.put("Number of recent months to show statistics for", "Nombre de mois récents pour afficher les statistiques");
		m.put("OAuth2 Client information | CLIENT ID", "Informations du client OAuth2 | ID CLIENT");
		m.put("OAuth2 Client information | CLIENT SECRET", "Informations du client OAuth2 | SECRET CLIENT");
		m.put("OCI Layout Directory", "Répertoire de disposition OCI");
		m.put("OIDC error: Inconsistent sub in ID token and userinfo", "Erreur OIDC : Sub incohérent dans le jeton ID et les informations utilisateur");
		m.put("OOPS! There Is An Error", "OUPS ! Il y a une erreur");
		m.put("OPEN", "OUVERT");
		m.put("OS", "Système d'exploitation");
		m.put("OS Arch", "Architecture du système d'exploitation");
		m.put("OS User Name", "Nom d'utilisateur du système d'exploitation");
		m.put("OS Version", "Version du système d'exploitation");
		m.put("OS/ARCH", "OS/ARCH");
		m.put("Offline", "Hors ligne");
		m.put("Ok", "Ok");
		m.put("Old Name", "Ancien nom");
		m.put("Old Password", "Ancien mot de passe");
		m.put("On Behalf Of", "Au nom de");
		m.put("On Branches", "Sur les branches");
		m.put("OneDev Issue Field", "Champ de problème OneDev");
		m.put("OneDev Issue Link", "Lien de problème OneDev");
		m.put("OneDev Issue State", "État du problème OneDev");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"OneDev analyse les fichiers du dépôt pour la recherche de code, les statistiques de ligne et les statistiques de contribution au code. Ce paramètre indique quels fichiers doivent être analysés et attend des <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>modèles de chemin</a> séparés par des espaces. Un modèle peut être exclu en le préfixant par '-', par exemple <code>-**/vendors/**</code> exclura tous les fichiers avec vendors dans le chemin. <b>NOTE : </b> Modifier ce paramètre n'affecte que les nouveaux commits. Pour appliquer le changement aux commits historiques, veuillez arrêter le serveur et supprimer le dossier <code>index</code> et <code>info/commit</code> sous le <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>répertoire de stockage du projet</a>. Le dépôt sera ré-analysé lorsque le serveur sera démarré");
		m.put("OneDev configures git hooks to communicate with itself via curl", "OneDev configure des hooks git pour communiquer avec lui-même via curl");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"OneDev doit rechercher et déterminer le DN de l'utilisateur, ainsi que rechercher les informations de groupe de l'utilisateur si la récupération de groupe est activée. Cochez cette option et spécifiez le DN et le mot de passe du 'manager' si ces opérations doivent être authentifiées");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"OneDev nécessite la ligne de commande git pour gérer les dépôts. La version minimale requise est 2.11.1. Assurez-vous également que git-lfs est installé si vous souhaitez récupérer des fichiers LFS dans le job de construction");
		m.put("Online", "En ligne");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", 
			"Créer uniquement un commit de fusion si la branche cible ne peut pas être avancée rapidement vers la branche source");
		m.put("Only projects manageable by access token owner can be authorized", "Seuls les projets gérables par le propriétaire du jeton d'accès peuvent être autorisés");
		m.put("Only system level audit events are displayed here. To view audit events for a specific project, please visit the project audit log page", 
			"Seuls les événements d'audit au niveau système sont affichés ici. Pour voir les événements d'audit d'un projet spécifique, veuillez visiter la page du journal d'audit du projet");
		m.put("Only users able to authenticate via password can be linked", "Seuls les utilisateurs capables de s'authentifier via un mot de passe peuvent être liés");
		m.put("Open", "Ouvrir");
		m.put("Open new pull request", "Ouvrir une nouvelle pull request");
		m.put("Open terminal of current running step", "Ouvrir le terminal de l'étape en cours d'exécution");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"L'identification du client OpenID sera attribuée par votre fournisseur OpenID lors de l'enregistrement de cette instance OneDev en tant qu'application cliente");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"Le secret du client OpenID sera généré par votre fournisseur OpenID lors de l'enregistrement de cette instance OneDev en tant qu'application cliente");
		m.put("OpenSSH Public Key", "Clé publique OpenSSH");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"La clé publique OpenSSH commence par 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', ou 'sk-ssh-ed25519@openssh.com'");
		m.put("Opened issue \"{0}\" ({1})", "Problème ouvert \"{0}\" ({1})");
		m.put("Opened pull request \"{0}\" ({1})", "Pull request ouverte \"{0}\" ({1})");
		m.put("Operation", "Opération");
		m.put("Operation Failed", "Échec de l'opération");
		m.put("Operation Successful", "Opération réussie");
		m.put("Operations", "Opérations");
		m.put("Optional", "Optionnel");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"Spécifiez éventuellement le projet dans lequel créer le problème. Laissez vide pour créer dans le projet actuel");
		m.put("Optionally add new users to specified default group", "Ajoutez éventuellement de nouveaux utilisateurs au groupe par défaut spécifié");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"Ajoutez éventuellement l'utilisateur nouvellement authentifié au groupe spécifié si les informations d'appartenance ne sont pas disponibles");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"Ajoutez éventuellement l'utilisateur nouvellement authentifié au groupe spécifié si les informations d'appartenance ne sont pas récupérées");
		m.put("Optionally choose required builds. You may also input jobs not listed here, and press ENTER to add them", 
			"Choisissez éventuellement les builds requis. Vous pouvez également saisir des jobs non listés ici et appuyer sur ENTRÉE pour les ajouter");
		m.put("Optionally configure proxy to access remote repository. Proxy should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;", 
			"Configurez éventuellement un proxy pour accéder au dépôt distant. Le proxy doit être au format &lt;proxy host&gt;:&lt;proxy port&gt;");
		m.put("Optionally define a unique key for the project with two or more upper case letters. This key can be used to reference issues, builds, and pull requests with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of <code>&lt;project path&gt;#&lt;number&gt;</code>", 
			"Définissez éventuellement une clé unique pour le projet avec deux lettres majuscules ou plus. Cette clé peut être utilisée pour référencer des problèmes, des builds et des pull requests avec une forme stable et courte <code>&lt;project key&gt;-&lt;number&gt;</code> au lieu de <code>&lt;project path&gt;#&lt;number&gt;</code>");
		m.put("Optionally define parameter specifications of the job", "Définissez éventuellement les spécifications des paramètres du job");
		m.put("Optionally define parameter specifications of the step template", "Définissez éventuellement les spécifications des paramètres du modèle d'étape");
		m.put("Optionally describe the group", "Décrivez éventuellement le groupe");
		m.put("Optionally describes the custom field. Html tags are accepted", "Décrivez éventuellement le champ personnalisé. Les balises HTML sont acceptées");
		m.put("Optionally describes the param. Html tags are accepted.", "Décrivez éventuellement le paramètre. Les balises HTML sont acceptées.");
		m.put("Optionally filter builds", "Filtrez éventuellement les builds");
		m.put("Optionally filter issues", "Filtrez éventuellement les problèmes");
		m.put("Optionally filter pull requests", "Filtrez éventuellement les pull requests");
		m.put("Optionally leave a note", "Laissez éventuellement une note");
		m.put("Optionally mount directories or files under job workspace into container", "Montez éventuellement des répertoires ou des fichiers sous l'espace de travail du job dans le conteneur");
		m.put("Optionally select fields to prompt when this button is pressed", "Sélectionnez éventuellement les champs à afficher lorsque ce bouton est pressé");
		m.put("Optionally select fields to remove when this transition happens", "Sélectionnez éventuellement les champs à supprimer lorsque cette transition se produit");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"Spécifiez éventuellement le nom de l'attribut à l'intérieur de l'entrée LDAP utilisateur dont la valeur sera prise comme email utilisateur. Ce champ est normalement défini sur <i>mail</i> selon la RFC 2798");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"Spécifiez éventuellement le nom de l'attribut à l'intérieur de l'entrée LDAP utilisateur dont la valeur sera prise comme nom complet de l'utilisateur. Ce champ est normalement défini sur <i>displayName</i> selon la RFC 2798. Si laissé vide, le nom complet de l'utilisateur ne sera pas récupéré");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"Spécifiez éventuellement un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de tâche</a> à utiliser comme jeton d'accès GitHub. Cela est utilisé pour récupérer les notes de version des dépendances hébergées sur GitHub, et l'accès authentifié bénéficiera d'une limite de taux plus élevée.");
		m.put("Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko", 
			"Spécifiez éventuellement des <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>options supplémentaires</a> pour kaniko.");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane", 
			"Spécifiez éventuellement des <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>options supplémentaires</a> pour crane.");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane", 
			"Spécifiez éventuellement des <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>options supplémentaires</a> pour crane.");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to build for platform of the node running the job", 
			"Spécifiez éventuellement des <span class='text-info'>plateformes séparées par des virgules</span> à construire, par exemple <tt>linux/amd64,linux/arm64</tt>. Laissez vide pour construire pour la plateforme du nœud exécutant la tâche.");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to scan, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to scan all platforms in OCI layout", 
			"Spécifiez éventuellement des <span class='text-info'>plateformes séparées par des virgules</span> à analyser, par exemple <tt>linux/amd64,linux/arm64</tt>. Laissez vide pour analyser toutes les plateformes dans la disposition OCI.");
		m.put("Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use file <tt>Dockerfile</tt> under build path specified above", 
			"Spécifiez éventuellement le Dockerfile relatif à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail de la tâche</a>. Laissez vide pour utiliser le fichier <tt>Dockerfile</tt> sous le chemin de construction spécifié ci-dessus.");
		m.put("Optionally specify JavaScript config to be used by Renovate CLI", "Spécifiez éventuellement une configuration JavaScript à utiliser par Renovate CLI.");
		m.put("Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. Leave empty to derive from server url", 
			"Spécifiez éventuellement une URL racine SSH, qui sera utilisée pour construire l'URL de clonage du projet via le protocole SSH. Laissez vide pour dériver de l'URL du serveur.");
		m.put("Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of the text input", 
			"Spécifiez éventuellement un <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>modèle d'expression régulière</a> pour les valeurs valides de l'entrée texte.");
		m.put("Optionally specify a OneDev project to be used as parent of imported projects. Leave empty to import as root projects", 
			"Spécifiez éventuellement un projet OneDev à utiliser comme parent des projets importés. Laissez vide pour importer en tant que projets racine.");
		m.put("Optionally specify a OneDev project to be used as parent of imported repositories. Leave empty to import as root projects", 
			"Spécifiez éventuellement un projet OneDev à utiliser comme parent des dépôts importés. Laissez vide pour importer en tant que projets racine.");
		m.put("Optionally specify a base query for the list", "Spécifiez éventuellement une requête de base pour la liste.");
		m.put("Optionally specify a base query to filter/order issues in backlog. Backlog issues are those not associating with current iteration", 
			"Spécifiez éventuellement une requête de base pour filtrer/ordonner les problèmes dans le backlog. Les problèmes du backlog sont ceux qui ne sont pas associés à l'itération en cours.");
		m.put("Optionally specify a base query to filter/order issues of the board", "Spécifiez éventuellement une requête de base pour filtrer/ordonner les problèmes du tableau.");
		m.put("Optionally specify a cron expression to schedule database auto-backup. The cron expression format is <em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>.For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz tutorial</a>.The backup files will be placed into <em>db-backup</em> folder under OneDev installation directory. In case multiple servers connect to form a cluster, auto-backup takes place on the <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>lead server</a>. Leave this property empty if you do not want to enable database auto backup.", 
			"Spécifiez éventuellement une expression cron pour planifier la sauvegarde automatique de la base de données. Le format de l'expression cron est <em>&lt;secondes&gt; &lt;minutes&gt; &lt;heures&gt; &lt;jour-du-mois&gt; &lt;mois&gt; &lt;jour-de-la-semaine&gt;</em>. Par exemple, <em>0 0 1 * * ?</em> signifie 1h00 tous les jours. Pour plus de détails sur le format, consultez le <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>tutoriel Quartz</a>. Les fichiers de sauvegarde seront placés dans le dossier <em>db-backup</em> sous le répertoire d'installation de OneDev. Dans le cas où plusieurs serveurs se connectent pour former un cluster, la sauvegarde automatique a lieu sur le <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>serveur principal</a>. Laissez cette propriété vide si vous ne souhaitez pas activer la sauvegarde automatique de la base de données.");
		m.put("Optionally specify a date field to hold due date information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez éventuellement un champ de date pour contenir les informations de date d'échéance.<br><b>NOTE : </b> Vous pouvez personnaliser les champs de problème OneDev au cas où il n'y aurait pas d'option appropriée ici.");
		m.put("Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to put retrieved artifacts. Leave empty to use job workspace itself", 
			"Spécifiez éventuellement un chemin relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail de la tâche</a> pour placer les artefacts récupérés. Laissez vide pour utiliser l'espace de travail de la tâche lui-même.");
		m.put("Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. <b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, as the volume is only used to hold temporary build files", 
			"Spécifiez éventuellement une classe de stockage pour allouer dynamiquement le volume de construction. Laissez vide pour utiliser la classe de stockage par défaut. <b class='text-warning'>NOTE :</b> La politique de récupération de la classe de stockage doit être définie sur <code>Delete</code>, car le volume est uniquement utilisé pour contenir des fichiers de construction temporaires.");
		m.put("Optionally specify a working period field to hold estimated time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez éventuellement un champ de période de travail pour contenir les informations de temps estimé.<br><b>NOTE : </b> Vous pouvez personnaliser les champs de problème OneDev au cas où il n'y aurait pas d'option appropriée ici.");
		m.put("Optionally specify a working period field to hold spent time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez éventuellement un champ de période de travail pour contenir les informations de temps passé.<br><b>NOTE : </b> Vous pouvez personnaliser les champs de problème OneDev au cas où il n'y aurait pas d'option appropriée ici.");
		m.put("Optionally specify a working period field to hold time estimate infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez éventuellement un champ de période de travail pour contenir les informations d'estimation de temps.<br><b>NOTE : </b> Vous pouvez personnaliser les champs de problème OneDev au cas où il n'y aurait pas d'option appropriée ici.");
		m.put("Optionally specify a working period field to hold time spent infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez éventuellement un champ de période de travail pour contenir les informations de temps passé.<br><b>NOTE : </b> Vous pouvez personnaliser les champs de problème OneDev au cas où il n'y aurait pas d'option appropriée ici.");
		m.put("Optionally specify additional options for buildx build command", "Spécifiez éventuellement des options supplémentaires pour la commande de construction buildx.");
		m.put("Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, the response header <code>Access-Control-Allow-Origin</code> will be set to the same value", 
			"Spécifiez éventuellement les origines <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> autorisées. Pour une requête simple ou préliminaire CORS, si la valeur de l'en-tête de requête <code>Origin</code> est incluse ici, l'en-tête de réponse <code>Access-Control-Allow-Origin</code> sera défini sur la même valeur.");
		m.put("Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match", 
			"Spécifiez éventuellement le domaine de messagerie autorisé pour les utilisateurs en auto-inscription. Utilisez '*' ou '?' pour la correspondance de modèle.");
		m.put("Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). Leave empty to all types", 
			"Spécifiez éventuellement les types de commit applicables pour la vérification du pied de message de commit (appuyez sur ENTRÉE pour ajouter une valeur). Laissez vide pour tous les types.");
		m.put("Optionally specify applicable jobs of this executor", "Spécifiez éventuellement les travaux applicables à cet exécuteur");
		m.put("Optionally specify applicable users who pushed the change", "Spécifiez éventuellement les utilisateurs applicables qui ont poussé le changement");
		m.put("Optionally specify arguments to run above image", "Spécifiez éventuellement des arguments pour exécuter l'image ci-dessus.");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"Spécifiez éventuellement des artefacts à récupérer de la dépendance dans <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail de la tâche</a>. Seuls les artefacts publiés (via l'étape de publication d'artefacts) peuvent être récupérés. Laissez vide pour ne récupérer aucun artefact.");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"Spécifiez éventuellement les rôles autorisés à appuyer sur ce bouton. Si non spécifié, tous les utilisateurs sont autorisés.");
		m.put("Optionally specify base query of the list", "Spécifiez éventuellement la requête de base de la liste.");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"Spécifiez éventuellement les branches/utilisateurs/groupes autorisés à accéder à ce secret. Si laissé vide, toute tâche peut accéder à ce secret, y compris celles déclenchées via des demandes de tirage externes.");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"Spécifiez éventuellement le chemin du contexte de construction relatif à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail de la tâche</a>. Laissez vide pour utiliser l'espace de travail de la tâche lui-même. Le fichier <code>Dockerfile</code> est censé exister dans le répertoire du contexte de construction, sauf si vous spécifiez un emplacement différent avec l'option <code>--dockerfile</code>.");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"Spécifiez éventuellement le chemin de construction relatif à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail de la tâche</a>. Laissez vide pour utiliser l'espace de travail de la tâche lui-même.");
		m.put("Optionally specify cluster role the job pods service account binding to. This is necessary if you want to do things such as running other Kubernetes pods in job command", 
			"Spécifiez éventuellement le rôle de cluster auquel le compte de service des pods de tâche est lié. Cela est nécessaire si vous souhaitez effectuer des actions telles que l'exécution d'autres pods Kubernetes dans la commande de tâche.");
		m.put("Optionally specify comma separated licenses to be ignored", "Spécifiez éventuellement les licences séparées par des virgules à ignorer.");
		m.put("Optionally specify container arguments separated by space. Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse this with container options which should be specified in executor setting", 
			"Spécifiez éventuellement les arguments du conteneur séparés par des espaces. Un seul argument contenant un espace doit être entre guillemets. <b class='text-warning'>Note : </b> ne confondez pas cela avec les options du conteneur qui doivent être spécifiées dans les paramètres de l'exécuteur.");
		m.put("Optionally specify cpu limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Spécifiez éventuellement la limite de CPU pour chaque tâche/service utilisant cet exécuteur. Consultez <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>la gestion des ressources Kubernetes</a> pour plus de détails.");
		m.put("Optionally specify cpu limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> of relevant containers", 
			"Spécifiez éventuellement la limite de CPU de chaque tâche/service utilisant cet exécuteur. Cela sera utilisé comme option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> des conteneurs concernés.");
		m.put("Optionally specify criteria of issues which can be linked", "Spécifiez éventuellement les critères des problèmes qui peuvent être liés.");
		m.put("Optionally specify criteria of issues which can be linked on the other side", "Spécifiez éventuellement les critères des problèmes qui peuvent être liés de l'autre côté.");
		m.put("Optionally specify custom fields allowed to edit when open new issues", "Spécifiez éventuellement les champs personnalisés autorisés à modifier lors de l'ouverture de nouveaux problèmes.");
		m.put("Optionally specify depth for a shallow clone in order to speed up source retrieval", 
			"Spécifiez éventuellement la profondeur pour un clonage superficiel afin d'accélérer la récupération des sources.");
		m.put("Optionally specify description of the issue", "Spécifiez éventuellement la description du problème.");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"Spécifiez éventuellement les répertoires ou les modèles globaux à l'intérieur du chemin d'analyse à ignorer. Plusieurs exclusions doivent être séparées par des espaces.");
		m.put("Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types", 
			"Spécifiez éventuellement les types de fichiers non autorisés par extensions (appuyez sur ENTRÉE pour ajouter une valeur), par exemple <code>exe</code>, <code>bin</code>. Laissez vide pour autoriser tous les types de fichiers.");
		m.put("Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. Leave empty to use docker executable in PATH", 
			"Spécifiez éventuellement l'exécutable docker, par exemple <i>/usr/local/bin/docker</i>. Laissez vide pour utiliser l'exécutable docker dans PATH.");
		m.put("Optionally specify docker options to create network. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Spécifiez éventuellement les options docker pour créer un réseau. Plusieurs options doivent être séparées par des espaces, et une seule option contenant des espaces doit être entre guillemets.");
		m.put("Optionally specify docker options to run container. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Spécifiez éventuellement les options docker pour exécuter le conteneur. Plusieurs options doivent être séparées par des espaces, et une seule option contenant des espaces doit être entre guillemets.");
		m.put("Optionally specify docker sock to use. Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows", 
			"Spécifiez éventuellement le socket docker à utiliser. Par défaut <i>/var/run/docker.sock</i> sur Linux, et <i>//./pipe/docker_engine</i> sur Windows.");
		m.put("Optionally specify environment variables for the container", "Spécifiez éventuellement les variables d'environnement pour le conteneur.");
		m.put("Optionally specify environment variables for this step", "Spécifiez éventuellement les variables d'environnement pour cette étape.");
		m.put("Optionally specify environment variables of the service", "Spécifiez éventuellement les variables d'environnement du service.");
		m.put("Optionally specify estimated time.", "Spécifiez éventuellement le temps estimé.");
		m.put("Optionally specify executor for this job. Leave empty to use auto-discover executor", 
			"Spécifiez éventuellement un exécuteur pour ce travail. Laissez vide pour utiliser l'exécuteur découvert automatiquement");
		m.put("Optionally specify executor for this job. Leave empty to use first applicable executor", 
			"Spécifiez éventuellement un exécuteur pour ce travail. Laissez vide pour utiliser le premier exécuteur applicable");
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"Spécifiez éventuellement les fichiers relatifs au chemin de cache à ignorer lors de la détection des changements de cache. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Plusieurs fichiers doivent être séparés par des espaces, et un seul fichier contenant des espaces doit être entre guillemets.");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"Spécifiez éventuellement la base de recherche de groupe si vous souhaitez récupérer les informations d'appartenance au groupe de l'utilisateur. Par exemple : <i>cn=Users, dc=example, dc=com</i>. Pour donner des permissions appropriées à un groupe Active Directory, un groupe OneDev avec le même nom doit être défini. Laissez vide pour gérer les appartenances de groupe du côté OneDev.");
		m.put("Optionally specify issue links allowed to edit", "Spécifiez éventuellement les liens de problème autorisés à modifier.");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "Spécifiez éventuellement les problèmes applicables à ce modèle. Laissez vide pour tous.");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"Spécifiez éventuellement les problèmes applicables à cette transition. Laissez vide pour tous les problèmes.");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"Spécifiez éventuellement les problèmes applicables à cette transition. Laissez vide pour tous les problèmes.");
		m.put("Optionally specify jobs allowed to use this script", "Spécifiez éventuellement les tâches autorisées à utiliser ce script.");
		m.put("Optionally specify memory limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Spécifiez éventuellement la limite de mémoire pour chaque tâche/service utilisant cet exécuteur. Consultez <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>la gestion des ressources Kubernetes</a> pour plus de détails.");
		m.put("Optionally specify memory limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> of relevant containers", 
			"Spécifiez éventuellement la limite de mémoire de chaque tâche/service utilisant cet exécuteur. Cela sera utilisé comme option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> des conteneurs concernés.");
		m.put("Optionally specify merge strategy of created pull request. Leave empty to use default strategy of each project", 
			"Spécifiez éventuellement la stratégie de fusion de la demande de tirage créée. Laissez vide pour utiliser la stratégie par défaut de chaque projet.");
		m.put("Optionally specify message of the tag", "Spécifiez éventuellement le message de la balise.");
		m.put("Optionally specify name of the attribute inside the user LDAP entry whose values will be taken as user SSH keys. SSH keys will be managed by LDAP only if this field is set", 
			"Spécifiez éventuellement le nom de l'attribut à l'intérieur de l'entrée LDAP utilisateur dont les valeurs seront prises comme clés SSH utilisateur. Les clés SSH seront gérées par LDAP uniquement si ce champ est défini.");
		m.put("Optionally specify node selector of the job pods", "Spécifiez éventuellement le sélecteur de nœud des pods de tâche.");
		m.put("Optionally specify options for docker builder prune command", "Spécifiez éventuellement les options pour la commande docker builder prune.");
		m.put("Optionally specify options for scp command. Multiple options need to be separated with space", 
			"Spécifiez éventuellement les options pour la commande scp. Plusieurs options doivent être séparées par des espaces.");
		m.put("Optionally specify options for ssh command. Multiple options need to be separated with space", 
			"Spécifiez éventuellement les options pour la commande ssh. Plusieurs options doivent être séparées par des espaces.");
		m.put("Optionally specify options passed to renovate cli. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Spécifiez éventuellement les options passées à renovate cli. Plusieurs options doivent être séparées par des espaces, et une seule option contenant des espaces doit être entre guillemets.");
		m.put("Optionally specify osv scanner <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. You may ignore particular vulnerabilities via this file", 
			"Spécifiez éventuellement le fichier de configuration du scanner osv <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>sous</a> <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail de la tâche</a>. Vous pouvez ignorer certaines vulnérabilités via ce fichier.");
		m.put("Optionally specify path protection rules", "Spécifiez éventuellement les règles de protection de chemin.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"Spécifiez éventuellement le chemin relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail de la tâche</a> à utiliser comme fichier d'ignorance trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"Spécifiez éventuellement le chemin relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail de la tâche</a> à utiliser comme fichier de configuration de secret trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"Spécifiez éventuellement le chemin relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail de la tâche</a> pour publier des artefacts. Laissez vide pour utiliser l'espace de travail de la tâche lui-même.");
		m.put("Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. Leave empty to pull all platforms in image", 
			"Spécifiez éventuellement la plateforme à récupérer, par exemple <tt>linux/amd64</tt>. Laissez vide pour récupérer toutes les plateformes dans l'image.");
		m.put("Optionally specify project to show builds of. Leave empty to show builds of all projects with permissions", 
			"Spécifiez éventuellement le projet pour afficher les constructions. Laissez vide pour afficher les constructions de tous les projets avec permissions.");
		m.put("Optionally specify project to show issues of. Leave empty to show issues of all accessible projects", 
			"Spécifiez éventuellement le projet pour afficher les problèmes. Laissez vide pour afficher les problèmes de tous les projets accessibles.");
		m.put("Optionally specify project to show packages of. Leave empty to show packages of all projects with permissions", 
			"Spécifiez éventuellement le projet pour afficher les packages. Laissez vide pour afficher les packages de tous les projets avec permissions.");
		m.put("Optionally specify ref of above job, for instance <i>refs/heads/main</i>. Use * for wildcard match", 
			"Spécifiez éventuellement la référence de la tâche ci-dessus, par exemple <i>refs/heads/main</i>. Utilisez * pour la correspondance de modèle.");
		m.put("Optionally specify registry logins to override those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token secret for password secret", 
			"Spécifiez éventuellement les connexions au registre pour remplacer celles définies dans l'exécuteur de tâche. Pour le registre intégré, utilisez <code>@server_url@</code> pour l'URL du registre, <code>@job_token@</code> pour le nom d'utilisateur, et le secret du jeton d'accès pour le mot de passe.");
		m.put("Optionally specify relative directory to put uploaded files", "Spécifiez éventuellement le répertoire relatif pour placer les fichiers téléchargés.");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to clone code into. Leave empty to use job workspace itself", 
			"Spécifiez éventuellement le chemin relatif sous <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail de la tâche</a> pour cloner le code. Laissez vide pour utiliser l'espace de travail de la tâche lui-même.");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"Spécifiez éventuellement le chemin relatif sous <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail de la tâche</a> pour analyser. Laissez vide pour utiliser l'espace de travail de la tâche lui-même.");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"Spécifiez éventuellement les chemins relatifs sous <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail de la tâche</a> pour analyser les vulnérabilités des dépendances. Plusieurs chemins peuvent être spécifiés et doivent être séparés par des espaces. Laissez vide pour utiliser l'espace de travail de la tâche lui-même.");
		m.put("Optionally specify required reviewers for changes of specified branch", "Spécifiez éventuellement les réviseurs requis pour les modifications de la branche spécifiée.");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"Spécifiez éventuellement la révision à partir de laquelle créer la branche. Laissez vide pour créer à partir du commit de construction.");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"Spécifiez éventuellement un répertoire séparé pour stocker les artefacts de construction. Un répertoire non absolu est considéré comme relatif au répertoire du site.");
		m.put("Optionally specify separate directory to store git lfs files. Non-absolute directory is considered to be relative to site directory", 
			"Spécifiez éventuellement un répertoire séparé pour stocker les fichiers git lfs. Un répertoire non absolu est considéré comme relatif au répertoire du site.");
		m.put("Optionally specify separate directory to store package files. Non-absolute directory is considered to be relative to site directory", 
			"Spécifiez éventuellement un répertoire séparé pour stocker les fichiers de package. Un répertoire non absolu est considéré comme relatif au répertoire du site.");
		m.put("Optionally specify services required by this job. <b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors (server docker executor, remote docker executor, or kubernetes executor)", 
			"Spécifiez éventuellement les services requis par cette tâche. <b class='text-warning'>NOTE :</b> Les services ne sont pris en charge que par les exécuteurs compatibles avec docker (exécuteur docker serveur, exécuteur docker distant ou exécuteur Kubernetes).");
		m.put("Optionally specify space-separated branches applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Spécifiez éventuellement les branches séparées par des espaces applicables à cette transition. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à toutes.");
		m.put("Optionally specify space-separated branches applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for default branch", 
			"Spécifiez éventuellement les branches séparées par des espaces applicables à ce déclencheur. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour la branche par défaut.");
		m.put("Optionally specify space-separated branches to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Spécifiez éventuellement les branches séparées par des espaces à vérifier. Utilisez '**' ou '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à toutes les branches.");
		m.put("Optionally specify space-separated commit messages applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Spécifiez éventuellement des messages de commit séparés par des espaces applicables pour cette transition. Utilisez '*' ou '?' pour une correspondance générique. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à tous");
		m.put("Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all files", 
			"Spécifiez éventuellement les fichiers séparés par des espaces à vérifier. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à tous les fichiers.");
		m.put("Optionally specify space-separated jobs applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Spécifiez éventuellement les tâches séparées par des espaces applicables à cette transition. Utilisez '*' ou '?' pour la correspondance de modèle. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à toutes.");
		m.put("Optionally specify space-separated projects applicable for this trigger. This is useful for instance when you want to prevent the job from being triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Spécifiez éventuellement les projets séparés par des espaces applicables à ce déclencheur. Cela est utile par exemple lorsque vous souhaitez empêcher la tâche d'être déclenchée dans des projets forkés. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à tous les projets.");
		m.put("Optionally specify space-separated projects to search in. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to search in all projects with code read permission", 
			"Spécifiez éventuellement les projets séparés par des espaces dans lesquels rechercher. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour rechercher dans tous les projets avec permission de lecture de code.");
		m.put("Optionally specify space-separated reports. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Spécifiez éventuellement des rapports séparés par des espaces. Utilisez '*' ou '?' pour une correspondance avec des caractères génériques. Préfixez avec '-' pour exclure");
		m.put("Optionally specify space-separated service images applicable for this locator. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Spécifiez éventuellement des images de service séparées par des espaces applicables à ce localisateur. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>une correspondance avec des caractères génériques de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à tous");
		m.put("Optionally specify space-separated service names applicable for this locator. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Spécifiez éventuellement des noms de service séparés par des espaces applicables à ce localisateur. Utilisez '*' ou '?' pour une correspondance avec des caractères génériques. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à tous");
		m.put("Optionally specify space-separated tags to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all tags", 
			"Spécifiez éventuellement des balises séparées par des espaces à vérifier. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>une correspondance avec des caractères génériques de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à toutes les balises");
		m.put("Optionally specify space-separated target branches of the pull requests to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Spécifiez éventuellement des branches cibles séparées par des espaces des demandes de tirage à vérifier. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>une correspondance avec des caractères génériques de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à toutes les branches");
		m.put("Optionally specify the OpenID claim to retrieve groups of authenticated user. Depending on the provider, you may need to request additional scopes above to make this claim available", 
			"Spécifiez éventuellement la revendication OpenID pour récupérer les groupes d'utilisateur authentifié. Selon le fournisseur, vous devrez peut-être demander des portées supplémentaires ci-dessus pour rendre cette revendication disponible");
		m.put("Optionally specify the maximum value allowed.", "Spécifiez éventuellement la valeur maximale autorisée.");
		m.put("Optionally specify the minimum value allowed.", "Spécifiez éventuellement la valeur minimale autorisée.");
		m.put("Optionally specify the project to publish site files to. Leave empty to publish to current project", 
			"Spécifiez éventuellement le projet pour publier les fichiers du site. Laissez vide pour publier dans le projet actuel");
		m.put("Optionally specify uid:gid to run container as. <b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or using user namespace remapping", 
			"Spécifiez éventuellement uid:gid pour exécuter le conteneur. <b class='text-warning'>Remarque :</b> Ce paramètre doit être laissé vide si le runtime du conteneur est sans privilège ou utilise le remappage de l'espace utilisateur");
		m.put("Optionally specify user name to access remote repository", "Spécifiez éventuellement le nom d'utilisateur pour accéder au dépôt distant");
		m.put("Optionally specify valid scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope", 
			"Spécifiez éventuellement des portées valides pour les commits conventionnels (appuyez sur ENTRÉE pour ajouter une valeur). Laissez vide pour autoriser une portée arbitraire");
		m.put("Optionally specify valid types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type", 
			"Spécifiez éventuellement des types valides pour les commits conventionnels (appuyez sur ENTRÉE pour ajouter une valeur). Laissez vide pour autoriser un type arbitraire");
		m.put("Optionally specify value of git config <code>pack.packSizeLimit</code> for the repository", 
			"Spécifiez éventuellement la valeur de la configuration git <code>pack.packSizeLimit</code> pour le dépôt");
		m.put("Optionally specify value of git config <code>pack.threads</code> for the repository", 
			"Spécifiez éventuellement la valeur de la configuration git <code>pack.threads</code> pour le dépôt");
		m.put("Optionally specify value of git config <code>pack.window</code> for the repository", 
			"Spécifiez éventuellement la valeur de la configuration git <code>pack.window</code> pour le dépôt");
		m.put("Optionally specify value of git config <code>pack.windowMemory</code> for the repository", 
			"Spécifiez éventuellement la valeur de la configuration git <code>pack.windowMemory</code> pour le dépôt");
		m.put("Optionally specify where to run service pods specified in job. The first matching locator will be used. If no any locators are found, node selector of the executor will be used", 
			"Spécifiez éventuellement où exécuter les pods de service spécifiés dans le travail. Le premier localisateur correspondant sera utilisé. Si aucun localisateur n'est trouvé, le sélecteur de nœud de l'exécuteur sera utilisé");
		m.put("Optionally specify working directory of the container. Leave empty to use default working directory of the container", 
			"Spécifiez éventuellement le répertoire de travail du conteneur. Laissez vide pour utiliser le répertoire de travail par défaut du conteneur");
		m.put("Options", "Options");
		m.put("Or manually enter the secret key below in your authenticator app", "Ou entrez manuellement la clé secrète ci-dessous dans votre application d'authentification");
		m.put("Order By", "Trier par");
		m.put("Order More User Months", "Commander plus de mois utilisateur");
		m.put("Order Subscription", "Commander l'abonnement");
		m.put("Ordered List", "Liste ordonnée");
		m.put("Ordered list", "Liste ordonnée");
		m.put("Osv License Scanner", "Scanner de licence Osv");
		m.put("Osv Vulnerability Scanner", "Scanner de vulnérabilité Osv");
		m.put("Other", "Autre");
		m.put("Outline", "Contour");
		m.put("Outline Search", "Recherche de contour");
		m.put("Output", "Sortie");
		m.put("Overall", "Global");
		m.put("Overall Estimated Time:", "Temps estimé global :");
		m.put("Overall Spent Time:", "Temps passé global :");
		m.put("Overview", "Aperçu");
		m.put("Own:", "Propre :");
		m.put("Ownered By", "Possédé par");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "La clé privée PEM commence par '-----BEGIN RSA PRIVATE KEY-----'");
		m.put("PENDING", "EN ATTENTE");
		m.put("PMD Report", "Rapport PMD");
		m.put("Pack", "Pack");
		m.put("Pack Notification", "Notification de pack");
		m.put("Pack Size Limit", "Limite de taille de pack");
		m.put("Pack Type", "Type de pack");
		m.put("Package", "Paquet");
		m.put("Package Management", "Gestion des paquets");
		m.put("Package Notification", "Notification de paquet");
		m.put("Package Notification Template", "Modèle de notification de paquet");
		m.put("Package Privilege", "Privilège de paquet");
		m.put("Package Storage", "Stockage de paquet");
		m.put("Package list", "Liste de paquets");
		m.put("Package {0} deleted", "Paquet {0} supprimé");
		m.put("Packages", "Paquets");
		m.put("Page Not Found", "Page non trouvée");
		m.put("Page is in error, reload to recover", "La page est en erreur, rechargez pour récupérer");
		m.put("Param Instance", "Instance de paramètre");
		m.put("Param Instances", "Instances de paramètre");
		m.put("Param Map", "Carte de paramètre");
		m.put("Param Matrix", "Matrice de paramètre");
		m.put("Param Name", "Nom de paramètre");
		m.put("Param Spec", "Spécification de paramètre");
		m.put("Param Spec Bean", "Bean de spécification de paramètre");
		m.put("Parameter", "Paramètre");
		m.put("Parameter Specs", "Spécifications de paramètres");
		m.put("Params", "Paramètres");
		m.put("Params & Triggers", "Paramètres & Déclencheurs");
		m.put("Params to Display", "Paramètres à afficher");
		m.put("Parent Bean", "Bean parent");
		m.put("Parent OneDev Project", "Projet parent OneDev");
		m.put("Parent Project", "Projet parent");
		m.put("Parent project not found", "Projet parent introuvable");
		m.put("Parents", "Parents");
		m.put("Partially covered", "Partiellement couvert");
		m.put("Partially covered by some tests", "Partiellement couvert par certains tests");
		m.put("Passcode", "Code d'accès");
		m.put("Passed", "Réussi");
		m.put("Password", "Mot de passe");
		m.put("Password Authenticator", "Authentificateur de mot de passe");
		m.put("Password Edit Bean", "Bean d'édition de mot de passe");
		m.put("Password Must Contain Digit", "Le mot de passe doit contenir un chiffre");
		m.put("Password Must Contain Lowercase", "Le mot de passe doit contenir une minuscule");
		m.put("Password Must Contain Special Character", "Le mot de passe doit contenir un caractère spécial");
		m.put("Password Must Contain Uppercase", "Le mot de passe doit contenir une majuscule");
		m.put("Password Policy", "Politique de mot de passe");
		m.put("Password Reset", "Réinitialisation du mot de passe");
		m.put("Password Reset Bean", "Bean de réinitialisation du mot de passe");
		m.put("Password Reset Template", "Modèle de réinitialisation du mot de passe");
		m.put("Password Secret", "Secret du mot de passe");
		m.put("Password and its confirmation should be identical.", "Le mot de passe et sa confirmation doivent être identiques.");
		m.put("Password changed. Please login with your new password", "Mot de passe changé. Veuillez vous connecter avec votre nouveau mot de passe");
		m.put("Password has been changed", "Le mot de passe a été changé");
		m.put("Password has been removed", "Le mot de passe a été supprimé");
		m.put("Password has been set", "Le mot de passe a été défini");
		m.put("Password of the user", "Mot de passe de l'utilisateur");
		m.put("Password or Access Token for Remote Repository", "Mot de passe ou jeton d'accès pour le dépôt distant");
		m.put("Password reset request has been sent", "La demande de réinitialisation du mot de passe a été envoyée");
		m.put("Password reset url is invalid or obsolete", "L'URL de réinitialisation du mot de passe est invalide ou obsolète");
		m.put("PasswordMinimum Length", "Longueur minimale du mot de passe");
		m.put("Paste subscription key here", "Collez la clé d'abonnement ici");
		m.put("Path containing spaces or starting with dash needs to be quoted", "Le chemin contenant des espaces ou commençant par un tiret doit être entre guillemets");
		m.put("Path placeholder", "Placeholder de chemin");
		m.put("Path to kubectl", "Chemin vers kubectl");
		m.put("Paths", "Chemins");
		m.put("Pattern", "Modèle");
		m.put("Pause", "Pause");
		m.put("Pause All Queried Agents", "Mettre en pause tous les agents interrogés");
		m.put("Pause Selected Agents", "Mettre en pause les agents sélectionnés");
		m.put("Paused", "En pause");
		m.put("Paused all queried agents", "Mis en pause tous les agents interrogés");
		m.put("Paused selected agents", "Mis en pause les agents sélectionnés");
		m.put("Pem Private Key", "Clé privée Pem");
		m.put("Pending", "En attente");
		m.put("Performance", "Performance");
		m.put("Performance Setting", "Paramètre de performance");
		m.put("Performance Settings", "Paramètres de performance");
		m.put("Performance settings have been saved", "Les paramètres de performance ont été enregistrés");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\"", 
			"Exécution d'une requête floue. Entourez le texte de recherche avec '~' pour ajouter plus de conditions, par exemple : ~texte à rechercher~ et \"État\" est \"Ouvert\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"", 
			"Exécution d'une requête floue. Entourez le texte de recherche avec '~' pour ajouter plus de conditions, par exemple : ~texte à rechercher~ et \"Type\" est \"NPM\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online", 
			"Exécution d'une requête floue. Entourez le texte de recherche avec '~' pour ajouter plus de conditions, par exemple : ~texte à rechercher~ et en ligne");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"Exécution d'une requête floue. Entourez le texte de recherche avec '~' pour ajouter plus de conditions, par exemple : ~texte à rechercher~ et ouvert");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and owned by me", 
			"Exécution d'une requête floue. Entourez le texte de recherche avec '~' pour ajouter plus de conditions, par exemple : ~texte à rechercher~ et possédé par moi");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved", 
			"Exécution d'une requête floue. Entourez le texte de recherche avec '~' pour ajouter plus de conditions, par exemple : ~texte à rechercher~ et non résolu");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"Exécution d'une requête floue. Entourez le texte de recherche avec '~' pour ajouter plus de conditions, par exemple : ~texte à rechercher~ auteur(robin)");
		m.put("Permanent link", "Lien permanent");
		m.put("Permanent link of this selection", "Lien permanent de cette sélection");
		m.put("Permission denied", "Permission refusée");
		m.put("Permission will be checked upon actual operation", "La permission sera vérifiée lors de l'opération réelle");
		m.put("Physical memory in mega bytes", "Mémoire physique en mégaoctets");
		m.put("Pick Existing", "Choisir existant");
		m.put("Pin this issue", "Épingler ce problème");
		m.put("Pipeline", "Pipeline");
		m.put("Placeholder", "Espace réservé");
		m.put("Plain text expected", "Texte brut attendu");
		m.put("Platform", "Plateforme");
		m.put("Platforms", "Plateformes");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"Veuillez <a wicket:id=\"download\" class=\"font-weight-bolder\">télécharger</a> les codes de récupération ci-dessous et les garder secrets. Ces codes peuvent être utilisés pour fournir un accès unique à votre compte en cas d'impossibilité d'accéder à l'application d'authentification. Ils ne seront <b>PAS</b> affichés à nouveau");
		m.put("Please Confirm", "Veuillez confirmer");
		m.put("Please Note", "Veuillez noter");
		m.put("Please check your email for password reset instructions", "Veuillez vérifier votre e-mail pour les instructions de réinitialisation du mot de passe");
		m.put("Please choose revision to create branch from", "Veuillez choisir une révision pour créer une branche");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "Veuillez configurer <a wicket:id=\"mailSetting\">le paramètre de messagerie</a> d'abord");
		m.put("Please confirm", "Veuillez confirmer");
		m.put("Please confirm the password.", "Veuillez confirmer le mot de passe.");
		m.put("Please follow <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">this instruction</a> to resolve the conflicts", 
			"Veuillez suivre <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">ces instructions</a> pour résoudre les conflits");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"Veuillez saisir l'un de vos codes de récupération enregistrés lors de l'activation de l'authentification à deux facteurs");
		m.put("Please login to perform this operation", "Veuillez vous connecter pour effectuer cette opération");
		m.put("Please login to perform this query", "Veuillez vous connecter pour effectuer cette requête");
		m.put("Please resolve undefined field values below", "Veuillez résoudre les valeurs de champs non définies ci-dessous");
		m.put("Please resolve undefined fields below", "Veuillez résoudre les champs non définis ci-dessous");
		m.put("Please resolve undefined states below. Note that if you select to delete an undefined state, all issues with that state will be deleted", 
			"Veuillez résoudre les états non définis ci-dessous. Notez que si vous choisissez de supprimer un état non défini, tous les problèmes avec cet état seront supprimés");
		m.put("Please select agents to pause", "Veuillez sélectionner des agents à mettre en pause");
		m.put("Please select agents to remove", "Veuillez sélectionner des agents à supprimer");
		m.put("Please select agents to restart", "Veuillez sélectionner des agents à redémarrer");
		m.put("Please select agents to resume", "Veuillez sélectionner des agents à reprendre");
		m.put("Please select branches to create pull request", "Veuillez sélectionner des branches pour créer une demande de tirage");
		m.put("Please select builds to cancel", "Veuillez sélectionner des builds à annuler");
		m.put("Please select builds to delete", "Veuillez sélectionner des builds à supprimer");
		m.put("Please select builds to re-run", "Veuillez sélectionner des builds à relancer");
		m.put("Please select comments to delete", "Veuillez sélectionner des commentaires à supprimer");
		m.put("Please select comments to set resolved", "Veuillez sélectionner des commentaires à marquer comme résolus");
		m.put("Please select comments to set unresolved", "Veuillez sélectionner des commentaires à marquer comme non résolus");
		m.put("Please select different branches", "Veuillez sélectionner des branches différentes");
		m.put("Please select fields to update", "Veuillez sélectionner des champs à mettre à jour");
		m.put("Please select groups to remove from", "Veuillez sélectionner les groupes à retirer");
		m.put("Please select issues to copy", "Veuillez sélectionner des problèmes à copier");
		m.put("Please select issues to delete", "Veuillez sélectionner des problèmes à supprimer");
		m.put("Please select issues to edit", "Veuillez sélectionner des problèmes à modifier");
		m.put("Please select issues to move", "Veuillez sélectionner des problèmes à déplacer");
		m.put("Please select issues to sync estimated/spent time", "Veuillez sélectionner des problèmes pour synchroniser le temps estimé/passé");
		m.put("Please select packages to delete", "Veuillez sélectionner des packages à supprimer");
		m.put("Please select projects to delete", "Veuillez sélectionner des projets à supprimer");
		m.put("Please select projects to modify", "Veuillez sélectionner des projets à modifier");
		m.put("Please select projects to move", "Veuillez sélectionner des projets à déplacer");
		m.put("Please select pull requests to delete", "Veuillez sélectionner des demandes de tirage à supprimer");
		m.put("Please select pull requests to discard", "Veuillez sélectionner des demandes de tirage à rejeter");
		m.put("Please select pull requests to watch/unwatch", "Veuillez sélectionner des demandes de tirage à surveiller/ne pas surveiller");
		m.put("Please select query watches to delete", "Veuillez sélectionner des surveillances de requêtes à supprimer");
		m.put("Please select revision to create tag from", "Veuillez sélectionner une révision pour créer un tag");
		m.put("Please select revisions to compare", "Veuillez sélectionner des révisions à comparer");
		m.put("Please select users to convert to service accounts", "Veuillez sélectionner les utilisateurs à convertir en comptes de service");
		m.put("Please select users to disable", "Veuillez sélectionner des utilisateurs à désactiver");
		m.put("Please select users to enable", "Veuillez sélectionner des utilisateurs à activer");
		m.put("Please select users to remove from group", "Veuillez sélectionner les utilisateurs à retirer du groupe");
		m.put("Please specify file name above before editing content", "Veuillez spécifier un nom de fichier ci-dessus avant de modifier le contenu");
		m.put("Please switch to packages page of a particular project for the instructions", "Veuillez passer à la page des packages d'un projet particulier pour les instructions");
		m.put("Please wait...", "Veuillez patienter...");
		m.put("Please waiting...", "Veuillez attendre...");
		m.put("Plugin metadata not found", "Métadonnées du plugin introuvables");
		m.put("Poll Interval", "Intervalle de sondage");
		m.put("Populate Tag Mappings", "Remplir les mappages de tags");
		m.put("Port", "Port");
		m.put("Post", "Publier");
		m.put("Post Build Action", "Action post-build");
		m.put("Post Build Action Bean", "Bean d'action post-build");
		m.put("Post Build Actions", "Actions post-build");
		m.put("Post Url", "URL de publication");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "Modèle de préfixe");
		m.put("Prefix the title with <code>WIP</code> or <code>[WIP]</code> to mark the pull request as work in progress", 
			"Préfixez le titre avec <code>WIP</code> ou <code>[WIP]</code> pour marquer la pull request comme en cours");
		m.put("Prepend", "Ajouter au début");
		m.put("Preserve Days", "Conserver les jours");
		m.put("Preset Commit Message", "Message de commit prédéfini");
		m.put("Preset commit message updated", "Message de commit prédéfini mis à jour");
		m.put("Press 'y' to get permalink", "Appuyez sur 'y' pour obtenir le lien permanent");
		m.put("Prev", "Précédent");
		m.put("Prevent Creation", "Empêcher la création");
		m.put("Prevent Deletion", "Empêcher la suppression");
		m.put("Prevent Forced Push", "Empêcher le push forcé");
		m.put("Prevent Update", "Empêcher la mise à jour");
		m.put("Preview", "Aperçu");
		m.put("Previous", "Précédent");
		m.put("Previous Value", "Valeur précédente");
		m.put("Previous commit", "Commit précédent");
		m.put("Previous {0}", "Précédent {0}");
		m.put("Primary", "Principal");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "Adresse <a wicket:id=\"noPrimaryAddressLink\">email principale</a> non spécifiée");
		m.put("Primary Email", "Email principal");
		m.put("Primary email address not specified", "Adresse e-mail principale non spécifiée");
		m.put("Primary email address of your account is not specified yet", "L'adresse e-mail principale de votre compte n'est pas encore spécifiée");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"L'adresse e-mail principale sera utilisée pour recevoir des notifications, afficher le gravatar (si activé), etc.");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"L'adresse e-mail principale ou alias du compte ci-dessus sera utilisée comme adresse d'expéditeur pour diverses notifications par e-mail. L'utilisateur peut également répondre à cette adresse pour publier des commentaires sur les problèmes ou les demandes de tirage par e-mail si l'option <code>Check Incoming Email</code> est activée ci-dessous");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"Nom principal du compte pour se connecter au serveur de messagerie Office 365 afin d'envoyer/recevoir des e-mails. Assurez-vous que ce compte <b>possède</b> l'application enregistrée indiquée par l'identifiant de l'application ci-dessus");
		m.put("Private Key Secret", "Clé secrète privée");
		m.put("Private key regenerated and SSH server restarted", "Clé privée régénérée et serveur SSH redémarré");
		m.put("Privilege", "Privilège");
		m.put("Privilege Settings", "Paramètres de privilège");
		m.put("Product Version", "Version du produit");
		m.put("Profile", "Profil");
		m.put("Programming language", "Langage de programmation");
		m.put("Project", "Projet");
		m.put("Project \"{0}\" deleted", "Projet \"{0}\" supprimé");
		m.put("Project Authorization Bean", "Bean d'autorisation de projet");
		m.put("Project Authorizations Bean", "Beans d'autorisations de projet");
		m.put("Project Build Setting", "Paramètre de construction du projet");
		m.put("Project Dependencies", "Dépendances du projet");
		m.put("Project Dependency", "Dépendance du projet");
		m.put("Project Id", "Identifiant du projet");
		m.put("Project Import Option", "Option d'importation de projet");
		m.put("Project Issue Setting", "Paramètre des problèmes du projet");
		m.put("Project Key", "Clé du projet");
		m.put("Project Management", "Gestion de projet");
		m.put("Project Pack Setting", "Paramètre du pack de projet");
		m.put("Project Path", "Chemin du projet");
		m.put("Project Pull Request Setting", "Paramètre des demandes de tirage du projet");
		m.put("Project Replicas", "Répliques du projet");
		m.put("Project authorizations updated", "Autorisations de projet mises à jour");
		m.put("Project does not have any code yet", "Le projet n'a pas encore de code");
		m.put("Project forked", "Projet forké");
		m.put("Project id", "Identifiant du projet");
		m.put("Project list", "Liste des projets");
		m.put("Project manage privilege required to delete \"{0}\"", "Privilège de gestion de projet requis pour supprimer \"{0}\"");
		m.put("Project manage privilege required to modify \"{0}\"", "Privilège de gestion de projet requis pour modifier \"{0}\"");
		m.put("Project manage privilege required to move \"{0}\"", "Privilège de gestion de projet requis pour déplacer \"{0}\"");
		m.put("Project name", "Nom du projet");
		m.put("Project not specified yet", "Projet non encore spécifié");
		m.put("Project or revision not specified yet", "Projet ou révision non encore spécifié");
		m.put("Project overview", "Aperçu du projet");
		m.put("Project path", "Chemin du projet");
		m.put("Projects", "Projets");
		m.put("Projects Bean", "Bean des projets");
		m.put("Projects deleted", "Projets supprimés");
		m.put("Projects modified", "Projets modifiés");
		m.put("Projects moved", "Projets déplacés");
		m.put("Projects need to be redistributed when cluster members are added/removed. OneDev does not do this automatically as this is resource intensive, and you may only want to do it after cluster is finalized and stable.", 
			"Les projets doivent être redistribués lorsque des membres du cluster sont ajoutés/supprimés. OneDev ne le fait pas automatiquement car cela demande beaucoup de ressources, et vous pourriez vouloir le faire uniquement après que le cluster soit finalisé et stable.");
		m.put("Promotions", "Promotions");
		m.put("Prompt Fields", "Champs de saisie");
		m.put("Properties", "Propriétés");
		m.put("Provide server id (guild id) to restrict access only to server members", "Fournir l'identifiant du serveur (identifiant de guilde) pour restreindre l'accès uniquement aux membres du serveur");
		m.put("Proxy", "Proxy");
		m.put("Prune Builder Cache", "Nettoyer le cache du constructeur");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"Nettoyer le cache d'image du constructeur docker buildx. Cette étape appelle la commande docker builder prune pour supprimer le cache du constructeur buildx spécifié dans l'exécuteur docker du serveur ou l'exécuteur docker distant");
		m.put("Public", "Public");
		m.put("Public Key", "Clé publique");
		m.put("Public Roles", "Rôles publics");
		m.put("Publish", "Publier");
		m.put("Publish Coverage Report Step", "Étape de publication du rapport de couverture");
		m.put("Publish Problem Report Step", "Étape de publication du rapport de problème");
		m.put("Publish Report Step", "Étape de publication du rapport");
		m.put("Publish Unit Test Report Step", "Étape de publication du rapport de test unitaire");
		m.put("Published After", "Publié après");
		m.put("Published At", "Publié à");
		m.put("Published Before", "Publié avant");
		m.put("Published By", "Publié par");
		m.put("Published By Project", "Publié par le projet");
		m.put("Published By User", "Publié par l'utilisateur");
		m.put("Published File", "Fichier publié");
		m.put("Pull Command", "Commande de tirage");
		m.put("Pull Image", "Image de tirage");
		m.put("Pull Request", "Demande de tirage");
		m.put("Pull Request Branches", "Branches de demande de tirage");
		m.put("Pull Request Description", "Description de la demande de tirage");
		m.put("Pull Request Filter", "Filtre de demande de tirage");
		m.put("Pull Request Management", "Gestion des demandes de tirage");
		m.put("Pull Request Markdown Report", "Rapport Markdown de demande de tirage");
		m.put("Pull Request Notification", "Notification de demande de tirage");
		m.put("Pull Request Notification Template", "Modèle de notification de demande de tirage");
		m.put("Pull Request Notification Unsubscribed", "Notification de demande de tirage désabonnée");
		m.put("Pull Request Notification Unsubscribed Template", "Modèle de notification de demande de tirage désabonnée");
		m.put("Pull Request Settings", "Paramètres de demande de tirage");
		m.put("Pull Request Statistics", "Statistiques de demande de tirage");
		m.put("Pull Request Title", "Titre de la demande de tirage");
		m.put("Pull Requests", "Demandes de tirage");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Tirer l'image docker en tant que mise en page OCI via crane. Cette étape doit être exécutée par l'exécuteur docker du serveur, l'exécuteur docker distant ou l'exécuteur Kubernetes");
		m.put("Pull from Remote", "Tirer depuis le distant");
		m.put("Pull request", "Demande de tirage");
		m.put("Pull request #{0} already closed", "Demande de tirage #{0} déjà fermée");
		m.put("Pull request #{0} deleted", "Demande de tirage #{0} supprimée");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"Permission administrative de demande de tirage dans un projet, y compris les opérations par lot sur plusieurs demandes de tirage");
		m.put("Pull request already closed", "Demande de tirage déjà fermée");
		m.put("Pull request already opened", "Demande de tirage déjà ouverte");
		m.put("Pull request and code review", "Demande de tirage et revue de code");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not finished yet", 
			"La demande de tirage ne peut pas être fusionnée maintenant car <a class=\"more-info d-inline link-primary\">certaines constructions requises</a> ne sont pas encore terminées");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not successful", 
			"La demande de tirage ne peut pas être fusionnée maintenant car <a class=\"more-info d-inline link-primary\">certaines constructions requises</a> n'ont pas réussi");
		m.put("Pull request can not be merged now as it is <a class=\"more-info d-inline link-primary\">pending review</a>", 
			"La demande de tirage ne peut pas être fusionnée maintenant car elle est <a class=\"more-info d-inline link-primary\">en attente de révision</a>");
		m.put("Pull request can not be merged now as it was <a class=\"more-info d-inline link-primary\">requested for changes</a>", 
			"La demande de tirage ne peut pas être fusionnée maintenant car elle a été <a class=\"more-info d-inline link-primary\">demandée pour des modifications</a>");
		m.put("Pull request can not be merged now as valid signature is required for head commit", 
			"La demande de tirage ne peut pas être fusionnée maintenant car une signature valide est requise pour le commit principal");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "La demande de tirage ne peut être fusionnée qu'après avoir obtenu les approbations de tous les réviseurs");
		m.put("Pull request can only be merged by users with code write permission", "La demande de tirage ne peut être fusionnée que par des utilisateurs ayant la permission d'écriture de code");
		m.put("Pull request discard", "Demande de tirage abandonnée");
		m.put("Pull request duration statistics", "Statistiques de durée des pull requests");
		m.put("Pull request frequency statistics", "Statistiques de fréquence des pull requests");
		m.put("Pull request is discarded", "Le pull request est abandonné");
		m.put("Pull request is in error: {0}", "Le pull request est en erreur : {0}");
		m.put("Pull request is merged", "Le pull request est fusionné");
		m.put("Pull request is opened", "Le pull request est ouvert");
		m.put("Pull request is still a work in progress", "La pull request est encore en cours");
		m.put("Pull request is work in progress", "La pull request est en cours");
		m.put("Pull request list", "Liste des pull requests");
		m.put("Pull request merge", "Fusion du pull request");
		m.put("Pull request not exist or access denied", "Le pull request n'existe pas ou l'accès est refusé");
		m.put("Pull request not merged", "Le pull request n'est pas fusionné");
		m.put("Pull request number", "Numéro du pull request");
		m.put("Pull request open or update", "Ouverture ou mise à jour du pull request");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"La surveillance des requêtes de pull request n'affecte que les nouveaux pull requests. Pour gérer le statut de surveillance des pull requests existants en lot, filtrez les pull requests par statut de surveillance sur la page des pull requests, puis prenez les mesures appropriées");
		m.put("Pull request settings updated", "Paramètres du pull request mis à jour");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Les statistiques des pull requests sont une fonctionnalité d'entreprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("Pull request synchronization submitted", "Synchronisation du pull request soumise");
		m.put("Pull request will be merged automatically when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"Le pull request sera automatiquement fusionné lorsqu'il sera prêt. Cette option sera désactivée en cas d'ajout de nouveaux commits, de modification de la stratégie de fusion ou de changement de branche cible");
		m.put("Pull request will be merged automatically with a preset <a wicket:id=\"commitMessage\">commit message</a> when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"Le pull request sera automatiquement fusionné avec un <a wicket:id=\"commitMessage\">message de commit</a> prédéfini lorsqu'il sera prêt. Cette option sera désactivée en cas d'ajout de nouveaux commits, de modification de la stratégie de fusion ou de changement de branche cible");
		m.put("Push Image", "Push Image");
		m.put("Push chart to the repository", "Push du chart vers le dépôt");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Push de l'image Docker depuis la disposition OCI via crane. Cette étape doit être exécutée par l'exécuteur Docker du serveur, l'exécuteur Docker distant ou l'exécuteur Kubernetes");
		m.put("Push to Remote", "Push vers un dépôt distant");
		m.put("Push to container registry", "Push vers un registre de conteneurs");
		m.put("PyPI(s)", "PyPI(s)");
		m.put("Pylint Report", "Rapport Pylint");
		m.put("Queries", "Requêtes");
		m.put("Query", "Requête");
		m.put("Query Parameters", "Paramètres de requête");
		m.put("Query Watches", "Surveillances des requêtes");
		m.put("Query commits", "Requêtes de commits");
		m.put("Query not submitted", "Requête non soumise");
		m.put("Query param", "Paramètre de requête");
		m.put("Query/order agents", "Requête/ordre des agents");
		m.put("Query/order builds", "Requête/ordre des builds");
		m.put("Query/order comments", "Requête/ordre des commentaires");
		m.put("Query/order issues", "Requête/ordre des problèmes");
		m.put("Query/order packages", "Requête/ordre des packages");
		m.put("Query/order projects", "Requête/ordre des projets");
		m.put("Query/order pull requests", "Requête/ordre des pull requests");
		m.put("Queueing Takes", "Temps de mise en file d'attente");
		m.put("Quick Search", "Recherche rapide");
		m.put("Quote", "Citation");
		m.put("RESTful API", "API RESTful");
		m.put("RESTful API Help", "Aide API RESTful");
		m.put("Ran On Agent", "Exécuté sur l'agent");
		m.put("Re-run All Queried Builds", "Relancer tous les builds interrogés");
		m.put("Re-run Selected Builds", "Relancer les builds sélectionnés");
		m.put("Re-run request submitted", "Demande de relance soumise");
		m.put("Re-run this build", "Relancer ce build");
		m.put("Read", "Lire");
		m.put("Read body", "Lire le corps");
		m.put("Readiness Check Command", "Commande de vérification de disponibilité");
		m.put("Really want to delete this code comment?", "Voulez-vous vraiment supprimer ce commentaire de code ?");
		m.put("Rebase", "Rebase");
		m.put("Rebase Source Branch Commits", "Rebase des commits de la branche source");
		m.put("Rebase all commits from source branch onto target branch", "Rebase de tous les commits de la branche source sur la branche cible");
		m.put("Rebase source branch commits", "Rebase des commits de la branche source");
		m.put("Rebuild manually", "Reconstruire manuellement");
		m.put("Receive Posted Email", "Recevoir l'email posté");
		m.put("Received test mail", "Email de test reçu");
		m.put("Receivers", "Destinataires");
		m.put("Recovery code", "Code de récupération");
		m.put("Recursive", "Récursif");
		m.put("Redundant", "Redondant");
		m.put("Ref", "Référence");
		m.put("Ref Name", "Nom de la référence");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup", 
			"Consultez ce <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutoriel</a> pour un exemple de configuration");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> for an example setup", 
			"Consultez ce <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutoriel</a> pour un exemple de configuration");
		m.put("Reference", "Référence");
		m.put("Reference Build", "Build de référence");
		m.put("Reference Issue", "Problème de référence");
		m.put("Reference Pull Request", "Pull request de référence");
		m.put("Reference this {0} in markdown or commit message via below string.", "Référez-vous à ce {0} dans le markdown ou le message de commit via la chaîne ci-dessous.");
		m.put("Refresh", "Actualiser");
		m.put("Refresh Token", "Jeton de rafraîchissement");
		m.put("Refs", "Références");
		m.put("Regenerate", "Régénérer");
		m.put("Regenerate Private Key", "Régénérer la clé privée");
		m.put("Regenerate this access token", "Régénérer ce jeton d'accès");
		m.put("Registry Login", "Connexion au registre");
		m.put("Registry Logins", "Connexions au registre");
		m.put("Registry Url", "URL du registre");
		m.put("Regular Expression", "Expression régulière");
		m.put("Remaining User Months", "Mois utilisateur restants");
		m.put("Remaining User Months:", "Mois utilisateur restants :");
		m.put("Remaining time", "Temps restant");
		m.put("Remember Me", "Se souvenir de moi");
		m.put("Remote Docker Executor", "Exécuteur Docker distant");
		m.put("Remote Machine", "Machine distante");
		m.put("Remote Shell Executor", "Exécuteur de shell distant");
		m.put("Remote URL", "URL distante");
		m.put("Remote Url", "URL distante");
		m.put("Remove", "Supprimer");
		m.put("Remove All Queried Agents", "Supprimer tous les agents interrogés");
		m.put("Remove All Queried Users from Group", "Retirer tous les utilisateurs interrogés du groupe");
		m.put("Remove Fields", "Supprimer les champs");
		m.put("Remove From Current Iteration", "Supprimer de l'itération actuelle");
		m.put("Remove Selected Agents", "Supprimer les agents sélectionnés");
		m.put("Remove Selected Users from Group", "Retirer les utilisateurs sélectionnés du groupe");
		m.put("Remove from All Queried Groups", "Retirer de tous les groupes interrogés");
		m.put("Remove from Selected Groups", "Retirer des groupes sélectionnés");
		m.put("Remove from batch", "Supprimer du lot");
		m.put("Remove issue from this iteration", "Supprimer le problème de cette itération");
		m.put("Remove this assignee", "Supprimer cet assigné");
		m.put("Remove this external participant from issue", "Supprimer ce participant externe du problème");
		m.put("Remove this file", "Supprimer ce fichier");
		m.put("Remove this image", "Supprimer cette image");
		m.put("Remove this reviewer", "Supprimer cet examinateur");
		m.put("Removed all queried agents. Type <code>yes</code> below to confirm", "Supprimé tous les agents interrogés. Tapez <code>yes</code> ci-dessous pour confirmer");
		m.put("Removed selected agents. Type <code>yes</code> below to confirm", "Supprimé les agents sélectionnés. Tapez <code>yes</code> ci-dessous pour confirmer");
		m.put("Rename {0}", "Renommer {0}");
		m.put("Renew Subscription", "Renouveler l'abonnement");
		m.put("Renovate CLI Options", "Options CLI de Renovate");
		m.put("Renovate JavaScript Config", "Configuration JavaScript de Renovate");
		m.put("Reopen", "Rouvrir");
		m.put("Reopen this iteration", "Rouvrir cette itération");
		m.put("Reopened pull request \"{0}\" ({1})", "Requête de fusion rouverte \"{0}\" ({1})");
		m.put("Replace With", "Remplacer par");
		m.put("Replica Count", "Nombre de réplicas");
		m.put("Replicas", "Réplicas");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "Répondu au commentaire sur le fichier \"{0}\" dans le projet \"{1}\"");
		m.put("Reply", "Répondre");
		m.put("Report Name", "Nom du rapport");
		m.put("Report format changed. You may re-run this build to generate the report in new format", 
			"Format du rapport modifié. Vous pouvez relancer cette construction pour générer le rapport dans le nouveau format");
		m.put("Repository Sync", "Synchronisation du dépôt");
		m.put("Request Body", "Corps de la requête");
		m.put("Request For Changes", "Demande de modifications");
		m.put("Request Scopes", "Portées de la demande");
		m.put("Request Trial Subscription", "Demander un abonnement d'essai");
		m.put("Request review", "Demander une révision");
		m.put("Request to sync", "Demander une synchronisation");
		m.put("Requested For changes", "Demandé des modifications");
		m.put("Requested changes to pull request \"{0}\" ({1})", "Demandé des modifications à la requête de fusion \"{0}\" ({1})");
		m.put("Requested for changes", "Demandé des modifications");
		m.put("Requested to sync estimated/spent time", "Demandé de synchroniser le temps estimé/passé");
		m.put("Require Autentication", "Exiger une authentification");
		m.put("Require Strict Pull Request Builds", "Exiger des constructions strictes pour les requêtes de fusion");
		m.put("Require Successful", "Exiger un succès");
		m.put("Required", "Requis");
		m.put("Required Builds", "Constructions requises");
		m.put("Required Reviewers", "Examinateurs requis");
		m.put("Required Services", "Services requis");
		m.put("Resend Verification Email", "Renvoyer l'email de vérification");
		m.put("Resend invitation", "Renvoyer l'invitation");
		m.put("Reset", "Réinitialiser");
		m.put("Resolution", "Résolution");
		m.put("Resolved", "Résolu");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "Commentaire résolu sur le fichier \"{0}\" dans le projet \"{1}\"");
		m.put("Resource", "Ressource");
		m.put("Resource Settings", "Paramètres des ressources");
		m.put("Resources", "Ressources");
		m.put("Response", "Réponse");
		m.put("Response Body", "Corps de la réponse");
		m.put("Restart", "Redémarrer");
		m.put("Restart All Queried Agents", "Redémarrer tous les agents interrogés");
		m.put("Restart Selected Agents", "Redémarrer les agents sélectionnés");
		m.put("Restart command issued", "Commande de redémarrage émise");
		m.put("Restart command issued to all queried agents", "Commande de redémarrage émise à tous les agents interrogés");
		m.put("Restart command issued to selected agents", "Commande de redémarrage émise aux agents sélectionnés");
		m.put("Restore Source Branch", "Restaurer la branche source");
		m.put("Restored source branch", "Branche source restaurée");
		m.put("Resubmitted manually", "Soumis manuellement");
		m.put("Resume", "Reprendre");
		m.put("Resume All Queried Agents", "Reprendre tous les agents interrogés");
		m.put("Resume Selected Agents", "Reprendre les agents sélectionnés");
		m.put("Resumed all queried agents", "Repris tous les agents interrogés");
		m.put("Resumed selected agents", "Repris les agents sélectionnés");
		m.put("Retried At", "Retenté à");
		m.put("Retrieve Groups", "Récupérer les groupes");
		m.put("Retrieve LFS Files", "Récupérer les fichiers LFS");
		m.put("Retrieve Submodules", "Récupérer les sous-modules");
		m.put("Retry Condition", "Condition de nouvelle tentative");
		m.put("Retry Delay", "Délai de nouvelle tentative");
		m.put("Revert", "Rétablir");
		m.put("Reverted successfully", "Rétabli avec succès");
		m.put("Review required for deletion. Submit pull request instead", "Révision requise pour la suppression. Soumettez une requête de fusion à la place");
		m.put("Review required for this change. Please submit pull request instead", "Une révision est requise pour ce changement. Veuillez soumettre une demande de tirage à la place.");
		m.put("Review required for this change. Submit pull request instead", "Révision requise pour ce changement. Soumettez une requête de fusion à la place");
		m.put("Reviewers", "Examinateurs");
		m.put("Revision", "Révision");
		m.put("Revision indexing in progress...", "Indexation de la révision en cours...");
		m.put("Revision indexing in progress... (symbol navigation in revisions will be accurate after indexed)", 
			"Indexation de la révision en cours... (la navigation par symboles dans les révisions sera précise une fois indexée)");
		m.put("Right", "Droite");
		m.put("Role", "Rôle");
		m.put("Role \"{0}\" deleted", "Rôle \"{0}\" supprimé");
		m.put("Role \"{0}\" updated", "Rôle \"{0}\" mis à jour");
		m.put("Role Management", "Gestion des rôles");
		m.put("Role created", "Rôle créé");
		m.put("Roles", "Rôles");
		m.put("Root Projects", "Projets racine");
		m.put("Roslynator Report", "Rapport Roslynator");
		m.put("RubyGems(s)", "RubyGems(s)");
		m.put("Ruff Report", "Rapport Ruff");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "La règle s'appliquera si l'utilisateur opérant le tag correspond aux critères spécifiés ici");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"La règle s'appliquera uniquement si l'utilisateur modifiant la branche correspond aux critères spécifiés ici");
		m.put("Run As", "Exécuter en tant que");
		m.put("Run Buildx Image Tools", "Exécuter les outils d'image Buildx");
		m.put("Run Docker Container", "Exécuter le conteneur Docker");
		m.put("Run In Container", "Exécuter dans un conteneur");
		m.put("Run Integrity Check", "Exécuter la vérification d'intégrité");
		m.put("Run Job", "Exécuter le travail");
		m.put("Run Options", "Options d'exécution");
		m.put("Run below commands from within your git repository:", "Exécutez les commandes ci-dessous depuis votre dépôt git :");
		m.put("Run below commands to install this gem", "Exécutez les commandes ci-dessous pour installer ce gem");
		m.put("Run below commands to install this package", "Exécutez les commandes ci-dessous pour installer ce package");
		m.put("Run below commands to use this chart", "Exécutez les commandes ci-dessous pour utiliser ce chart");
		m.put("Run below commands to use this package", "Exécutez les commandes ci-dessous pour utiliser ce package");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"Exécutez la commande docker buildx imagetools avec les arguments spécifiés. Cette étape ne peut être exécutée que par un exécuteur docker serveur ou un exécuteur docker distant");
		m.put("Run job", "Exécuter le travail");
		m.put("Run job in another project", "Exécuter le travail dans un autre projet");
		m.put("Run on Bare Metal/Virtual Machine", "Exécuter sur Bare Metal/Machine Virtuelle");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"Exécutez le scanner osv pour analyser les licences violées utilisées par diverses <a href='https://deps.dev/' target='_blank'>dépendances</a>. Cela ne peut être exécuté que par un exécuteur compatible avec Docker.");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"Exécutez le scanner osv pour analyser les vulnérabilités dans <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>divers fichiers de verrouillage</a>. Cela ne peut être exécuté que par un exécuteur compatible avec Docker.");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"Exécutez le conteneur docker spécifié. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>L'espace de travail du travail</a> est monté dans le conteneur et son chemin est placé dans la variable d'environnement <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Remarque : </b> cette étape ne peut être exécutée que par un exécuteur docker serveur ou un exécuteur docker distant");
		m.put("Run specified step template", "Exécuter le modèle d'étape spécifié");
		m.put("Run this job", "Exécuter ce travail");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by docker aware executor.", 
			"Exécutez le scanner d'image de conteneur trivy pour trouver des problèmes dans l'image spécifiée. Pour les vulnérabilités, il vérifie divers <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>fichiers de distribution</a>. Il ne peut être exécuté que par un exécuteur compatible avec Docker.");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"Exécutez le scanner de système de fichiers trivy pour analyser divers <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>fichiers de verrouillage</a>. Il ne peut être exécuté que par un exécuteur compatible avec Docker et il est recommandé de l'exécuter <span class='text-warning'>après la résolution des dépendances</span> (npm install ou similaire). Comparé au scanner OSV, son installation est un peu plus complexe, mais peut fournir des résultats plus précis.");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"Exécutez le scanner rootfs trivy pour analyser divers <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>fichiers de distribution</a>. Il ne peut être exécuté que par un exécuteur compatible avec Docker et il est recommandé de l'exécuter sur la zone de staging de votre projet.");
		m.put("Run via Docker Container", "Exécuter via un conteneur Docker");
		m.put("Running", "En cours d'exécution");
		m.put("Running Takes", "Exécution prend");
		m.put("SLOC on {0}", "SLOC sur {0}");
		m.put("SMTP Host", "Hôte SMTP");
		m.put("SMTP Password", "Mot de passe SMTP");
		m.put("SMTP User", "Utilisateur SMTP");
		m.put("SMTP/IMAP", "SMTP/IMAP");
		m.put("SSH", "SSH");
		m.put("SSH & GPG Keys", "Clés SSH & GPG");
		m.put("SSH Clone URL", "URL de clonage SSH");
		m.put("SSH Keys", "Clés SSH");
		m.put("SSH Root URL", "URL racine SSH");
		m.put("SSH Server Key", "Clé serveur SSH");
		m.put("SSH key deleted", "Clé SSH supprimée");
		m.put("SSH settings have been saved and SSH server restarted", "Les paramètres SSH ont été enregistrés et le serveur SSH redémarré");
		m.put("SSL Setting", "Paramètre SSL");
		m.put("SSO Accounts", "Comptes SSO");
		m.put("SSO Providers", "Fournisseurs SSO");
		m.put("SSO account deleted", "Compte SSO supprimé");
		m.put("SSO provider \"{0}\" deleted", "Fournisseur SSO \"{0}\" supprimé");
		m.put("SSO provider created", "Fournisseur SSO créé");
		m.put("SSO provider updated", "Fournisseur SSO mis à jour");
		m.put("SUCCESSFUL", "RÉUSSI");
		m.put("Save", "Enregistrer");
		m.put("Save Query", "Enregistrer la requête");
		m.put("Save Query Bean", "Enregistrer le bean de requête");
		m.put("Save Settings", "Enregistrer les paramètres");
		m.put("Save Settings & Redistribute Projects", "Enregistrer les paramètres et redistribuer les projets");
		m.put("Save Template", "Enregistrer le modèle");
		m.put("Save as Mine", "Enregistrer comme mien");
		m.put("Saved Queries", "Requêtes enregistrées");
		m.put("Scan Path", "Chemin de scan");
		m.put("Scan Paths", "Chemins de scan");
		m.put("Scan below QR code with your TOTP authenticators", "Scannez le code QR ci-dessous avec vos authentificateurs TOTP");
		m.put("Schedule Issues", "Planifier les problèmes");
		m.put("Script Name", "Nom du script");
		m.put("Scripting Value", "Valeur de script");
		m.put("Search", "Rechercher");
		m.put("Search For", "Rechercher pour");
		m.put("Search Groups Using Filter", "Rechercher des groupes en utilisant un filtre");
		m.put("Search branch", "Rechercher une branche");
		m.put("Search files, symbols and texts", "Rechercher des fichiers, des symboles et des textes");
		m.put("Search for", "Rechercher pour");
		m.put("Search inside current tree", "Rechercher dans l'arborescence actuelle");
		m.put("Search is too general", "La recherche est trop générale");
		m.put("Search job", "Rechercher un travail");
		m.put("Search project", "Rechercher un projet");
		m.put("Secret", "Secret");
		m.put("Secret Config File", "Fichier de configuration secret");
		m.put("Secret Setting", "Paramètre secret");
		m.put("Security", "Sécurité");
		m.put("Security & Compliance", "Sécurité & Conformité");
		m.put("Security Setting", "Paramètre de sécurité");
		m.put("Security Settings", "Paramètres de sécurité");
		m.put("Security settings have been updated", "Les paramètres de sécurité ont été mis à jour");
		m.put("Select", "Sélectionner");
		m.put("Select Branch to Cherry Pick to", "Sélectionner une branche pour effectuer un cherry-pick");
		m.put("Select Branch to Revert on", "Sélectionner une branche pour effectuer un revert");
		m.put("Select Branch/Tag", "Sélectionner une branche/un tag");
		m.put("Select Existing", "Sélectionner existant");
		m.put("Select Job", "Sélectionner un job");
		m.put("Select Project", "Sélectionner un projet");
		m.put("Select below...", "Sélectionner ci-dessous...");
		m.put("Select iteration to schedule issues into", "Sélectionner une itération pour planifier les problèmes");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"Sélectionner une organisation à importer. Laisser vide pour importer depuis les dépôts sous le compte actuel");
		m.put("Select project and revision first", "Sélectionner d'abord un projet et une révision");
		m.put("Select project first", "Sélectionner d'abord un projet");
		m.put("Select project to import from", "Sélectionner un projet à importer");
		m.put("Select project to sync to. Leave empty to sync to current project", "Sélectionner un projet à synchroniser. Laisser vide pour synchroniser avec le projet actuel");
		m.put("Select repository to import from", "Sélectionner un dépôt à importer");
		m.put("Select users to send alert email upon events such as database auto-backup failure, cluster node unreachable etc", 
			"Sélectionner les utilisateurs pour envoyer un email d'alerte lors d'événements tels qu'un échec de sauvegarde automatique de la base de données, un nœud de cluster inaccessible, etc.");
		m.put("Select workspace to import from", "Sélectionner un espace de travail à importer");
		m.put("Send Notifications", "Envoyer des notifications");
		m.put("Send Pull Request", "Envoyer une pull request");
		m.put("Send notification", "Envoyer une notification");
		m.put("SendGrid", "SendGrid");
		m.put("Sendgrid Webhook Setting", "Paramètre de webhook SendGrid");
		m.put("Sending invitation to \"{0}\"...", "Envoi d'une invitation à \"{0}\"...");
		m.put("Sending test mail to {0}...", "Envoi d'un email de test à {0}...");
		m.put("Sequential Group", "Groupe séquentiel");
		m.put("Server", "Serveur");
		m.put("Server Docker Executor", "Exécuteur Docker du serveur");
		m.put("Server Id", "ID du serveur");
		m.put("Server Information", "Informations sur le serveur");
		m.put("Server Log", "Journal du serveur");
		m.put("Server Setup", "Configuration du serveur");
		m.put("Server Shell Executor", "Exécuteur shell du serveur");
		m.put("Server URL", "URL du serveur");
		m.put("Server fingerprint", "Empreinte digitale du serveur");
		m.put("Server host", "Hôte du serveur");
		m.put("Server is Starting...", "Le serveur démarre...");
		m.put("Server url", "URL du serveur");
		m.put("Service", "Service");
		m.put("Service Account", "Compte de service");
		m.put("Service Desk", "Service Desk");
		m.put("Service Desk Email Address", "Adresse email du Service Desk");
		m.put("Service Desk Issue Open Failed", "Échec de l'ouverture d'un problème dans le Service Desk");
		m.put("Service Desk Issue Open Failed Template", "Modèle d'échec d'ouverture de problème dans le Service Desk");
		m.put("Service Desk Issue Opened", "Problème ouvert dans le Service Desk");
		m.put("Service Desk Issue Opened Template", "Modèle de problème ouvert dans le Service Desk");
		m.put("Service Desk Setting", "Paramètre du Service Desk");
		m.put("Service Desk Setting Holder", "Support de paramètre du Service Desk");
		m.put("Service Desk Settings", "Paramètres du Service Desk");
		m.put("Service Locator", "Localisateur de service");
		m.put("Service Locators", "Localisateurs de service");
		m.put("Service account not allowed to login", "Compte de service non autorisé à se connecter");
		m.put("Service desk setting", "Paramètre du service desk");
		m.put("Service desk settings have been saved", "Les paramètres du service desk ont été enregistrés");
		m.put("Services", "Services");
		m.put("Session Timeout", "Expiration de session");
		m.put("Set", "Définir");
		m.put("Set All Queried As Root Projects", "Définir tous les projets interrogés comme projets racine");
		m.put("Set All Queried Comments as Read", "Marquer tous les commentaires interrogés comme lus");
		m.put("Set All Queried Comments as Resolved", "Marquer tous les commentaires interrogés comme résolus");
		m.put("Set All Queried Comments as Unresolved", "Marquer tous les commentaires interrogés comme non résolus");
		m.put("Set All Queried Issues as Read", "Marquer tous les problèmes interrogés comme lus");
		m.put("Set All Queried Pull Requests as Read", "Marquer toutes les demandes de tirage interrogées comme lues");
		m.put("Set As Primary", "Définir comme principal");
		m.put("Set Build Description", "Définir la description de la construction");
		m.put("Set Build Version", "Définir la version de la construction");
		m.put("Set Resolved", "Définir comme résolu");
		m.put("Set Selected As Root Projects", "Définir les projets sélectionnés comme projets racine");
		m.put("Set Selected Comments as Resolved", "Marquer les commentaires sélectionnés comme résolus");
		m.put("Set Selected Comments as Unresolved", "Marquer les commentaires sélectionnés comme non résolus");
		m.put("Set Unresolved", "Définir comme non résolu");
		m.put("Set Up Cache", "Configurer le cache");
		m.put("Set Up Renovate Cache", "Configurer le cache Renovate");
		m.put("Set Up Trivy Cache", "Configurer le cache Trivy");
		m.put("Set Up Your Account", "Configurez votre compte");
		m.put("Set as Private", "Définir comme privé");
		m.put("Set as Public", "Définir comme public");
		m.put("Set description", "Définir la description");
		m.put("Set reviewed", "Marquer comme examiné");
		m.put("Set unreviewed", "Marquer comme non examiné");
		m.put("Set up Microsoft Teams notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url. ", 
			"Configurer les paramètres de notification Microsoft Teams. Les paramètres seront hérités par les projets enfants et peuvent être remplacés en définissant des paramètres avec la même URL de webhook.");
		m.put("Set up discord notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurer les paramètres de notification Discord. Les paramètres seront hérités par les projets enfants et peuvent être remplacés en définissant des paramètres avec la même URL de webhook.");
		m.put("Set up job cache to speed up job execution. Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> on how to use job cache", 
			"Configurer le cache de tâches pour accélérer l'exécution des tâches. Consultez <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>ce tutoriel</a> pour savoir comment utiliser le cache de tâches.");
		m.put("Set up ntfy.sh notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurer les paramètres de notification ntfy.sh. Les paramètres seront hérités par les projets enfants et peuvent être remplacés en définissant des paramètres avec la même URL de webhook.");
		m.put("Set up slack notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurer les paramètres de notification Slack. Les paramètres seront hérités par les projets enfants et peuvent être remplacés en définissant des paramètres avec la même URL de webhook.");
		m.put("Set up two-factor authentication", "Configurer l'authentification à deux facteurs");
		m.put("Setting", "Paramètre");
		m.put("Setting has been saved", "Le paramètre a été enregistré");
		m.put("Settings", "Paramètres");
		m.put("Settings and permissions of parent project will be inherited by this project", "Les paramètres et permissions du projet parent seront hérités par ce projet");
		m.put("Settings saved", "Paramètres enregistrés");
		m.put("Settings saved and project redistribution scheduled", "Paramètres enregistrés et redistribution du projet planifiée");
		m.put("Settings updated", "Paramètres mis à jour");
		m.put("Share dashboard", "Partager le tableau de bord");
		m.put("Share with Groups", "Partager avec des groupes");
		m.put("Share with Users", "Partager avec des utilisateurs");
		m.put("Shell", "Shell");
		m.put("Show Archived", "Afficher les archivés");
		m.put("Show Branch/Tag", "Afficher la branche/étiquette");
		m.put("Show Build Status", "Afficher le statut de la construction");
		m.put("Show Closed", "Afficher les fermés");
		m.put("Show Code Stats", "Afficher les statistiques de code");
		m.put("Show Command", "Afficher la commande");
		m.put("Show Condition", "Afficher la condition");
		m.put("Show Conditionally", "Afficher conditionnellement");
		m.put("Show Description", "Afficher la description");
		m.put("Show Duration", "Afficher la durée");
		m.put("Show Emojis", "Afficher les emojis");
		m.put("Show Error Detail", "Afficher le détail de l'erreur");
		m.put("Show Issue Status", "Afficher le statut des problèmes");
		m.put("Show Package Stats", "Afficher les statistiques des packages");
		m.put("Show Pull Request Stats", "Afficher les statistiques des demandes de tirage");
		m.put("Show Saved Queries", "Afficher les requêtes enregistrées");
		m.put("Show States By", "Afficher les états par");
		m.put("Show Works Of", "Afficher les travaux de");
		m.put("Show changes", "Afficher les modifications");
		m.put("Show commented code snippet", "Afficher l'extrait de code commenté");
		m.put("Show commit of this parent", "Afficher le commit de ce parent");
		m.put("Show emojis", "Afficher les emojis");
		m.put("Show in build list", "Afficher dans la liste des constructions");
		m.put("Show issues in list", "Afficher les problèmes dans la liste");
		m.put("Show issues not scheduled into current iteration", "Afficher les problèmes non planifiés dans l'itération actuelle");
		m.put("Show matching agents", "Afficher les agents correspondants");
		m.put("Show more", "Afficher plus");
		m.put("Show more lines", "Afficher plus de lignes");
		m.put("Show next match", "Afficher la correspondance suivante");
		m.put("Show previous match", "Afficher la correspondance précédente");
		m.put("Show test cases of this test suite", "Afficher les cas de test de cette suite de tests");
		m.put("Show total estimated/spent time", "Afficher le temps total estimé/passé");
		m.put("Showing first {0} files as there are too many", "Affichage des premiers {0} fichiers car il y en a trop");
		m.put("Sign In", "Se connecter");
		m.put("Sign In To", "Se connecter à");
		m.put("Sign Out", "Se déconnecter");
		m.put("Sign Up", "S'inscrire");
		m.put("Sign Up Bean", "Inscription Bean");
		m.put("Sign Up!", "S'inscrire !");
		m.put("Sign in", "Connexion");
		m.put("Signature required for this change, but no signing key is specified", "Signature requise pour ce changement, mais aucune clé de signature n'est spécifiée");
		m.put("Signature required for this change, please generate system GPG signing key first", "Signature requise pour ce changement, veuillez d'abord générer la clé de signature GPG système");
		m.put("Signature verified successfully with OneDev GPG key", "Signature vérifiée avec succès avec la clé GPG de OneDev");
		m.put("Signature verified successfully with committer's GPG key", "Signature vérifiée avec succès avec la clé GPG du committer");
		m.put("Signature verified successfully with committer's SSH key", "Signature vérifiée avec succès avec la clé SSH du committer");
		m.put("Signature verified successfully with tagger's GPG key", "Signature vérifiée avec succès avec la clé GPG du tagger");
		m.put("Signature verified successfully with tagger's SSH key", "Signature vérifiée avec succès avec la clé SSH du tagger");
		m.put("Signature verified successfully with trusted GPG key", "Signature vérifiée avec succès avec une clé GPG de confiance");
		m.put("Signed with an unknown GPG key ", "Signé avec une clé GPG inconnue");
		m.put("Signed with an unknown ssh key", "Signé avec une clé ssh inconnue");
		m.put("Signer Email Addresses", "Adresses email du signataire");
		m.put("Signing Key ID", "ID de clé de signature");
		m.put("Similar Issues", "Problèmes similaires");
		m.put("Single Sign On", "Authentification unique");
		m.put("Single Sign-On", "Authentification unique");
		m.put("Single sign on via discord.com", "Authentification unique via discord.com");
		m.put("Single sign on via twitch.tv", "Authentification unique via twitch.tv");
		m.put("Site", "Site");
		m.put("Size", "Taille");
		m.put("Size invalid", "Taille invalide");
		m.put("Slack Notifications", "Notifications Slack");
		m.put("Smtp Ssl Setting", "Paramètre Smtp Ssl");
		m.put("Smtp With Ssl", "Smtp avec Ssl");
		m.put("Some builds are {0}", "Certains builds sont {0}");
		m.put("Some jobs are hidden due to permission policy", "Certains travaux sont masqués en raison de la politique de permission");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "Quelqu'un a modifié le contenu que vous éditez. Rechargez la page et réessayez.");
		m.put("Some other pull requests are opening to this branch", "D'autres demandes de tirage sont ouvertes vers cette branche");
		m.put("Some projects might be hidden due to permission policy", "Certains projets peuvent être masqués en raison de la politique de permission");
		m.put("Some related commits of the code comment is missing", "Certains commits liés au commentaire de code sont manquants");
		m.put("Some related commits of the pull request are missing", "Certains commits liés à la demande de tirage sont manquants");
		m.put("Some required builds not passed", "Certains builds requis n'ont pas réussi");
		m.put("Someone made below change since you started editing", "Quelqu'un a effectué le changement ci-dessous depuis que vous avez commencé à éditer");
		m.put("Sort", "Trier");
		m.put("Source", "Source");
		m.put("Source Docker Image", "Image Docker Source");
		m.put("Source Lines", "Lignes Source");
		m.put("Source Path", "Chemin Source");
		m.put("Source branch already exists", "La branche source existe déjà");
		m.put("Source branch already merged into target branch", "La branche source est déjà fusionnée dans la branche cible");
		m.put("Source branch commits will be rebased onto target branch", "Les commits de la branche source seront rebasés sur la branche cible");
		m.put("Source branch is default branch", "La branche source est la branche par défaut");
		m.put("Source branch is outdated", "La branche source est obsolète");
		m.put("Source branch no longer exists", "La branche source n'existe plus");
		m.put("Source branch updated successfully", "La branche source a été mise à jour avec succès");
		m.put("Source project no longer exists", "Le projet source n'existe plus");
		m.put("Specified Value", "Valeur spécifiée");
		m.put("Specified choices", "Choix spécifiés");
		m.put("Specified default value", "Valeur par défaut spécifiée");
		m.put("Specified fields", "Champs spécifiés");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Spécifie l'URL LDAP du serveur Active Directory, par exemple : <i>ldap://ad-server</i>, ou <i>ldaps://ad-server</i>. Si votre serveur LDAP utilise un certificat auto-signé pour la connexion ldaps, vous devrez <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configurer OneDev pour faire confiance au certificat</a>");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Spécifie l'URL LDAP, par exemple : <i>ldap://localhost</i>, ou <i>ldaps://localhost</i>. Si votre serveur LDAP utilise un certificat auto-signé pour la connexion ldaps, vous devrez <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configurer OneDev pour faire confiance au certificat</a>");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"Spécifie les nœuds de base pour la recherche des utilisateurs. Par exemple : <i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"Spécifie le nom de l'attribut dans l'entrée LDAP utilisateur dont la valeur contient les noms distincts des groupes auxquels il appartient. Par exemple, certains serveurs LDAP utilisent l'attribut <i>memberOf</i> pour lister les groupes");
		m.put("Specifies password of above manager DN", "Spécifie le mot de passe du DN du gestionnaire ci-dessus");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"Spécifie l'attribut contenant le nom du groupe dans l'entrée LDAP du groupe trouvé. La valeur de cet attribut sera mappée à un groupe OneDev. Cet attribut est normalement défini sur <i>cn</i>");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier de résultats de test .net TRX relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple <tt>TestResults/*.trx</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> dont la valeur est un jeton d'accès avec la permission d'écriture de code sur les projets ci-dessus. Les commits, les problèmes et les demandes de tirage seront également créés sous le nom du propriétaire du jeton d'accès");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"Spécifie le fichier de sortie json <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce fichier peut être généré avec l'option de sortie json de clippy, par exemple <code>cargo clippy --message-format json>check-result.json</code>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify Build Options", "Spécifie les options de construction");
		m.put("Specify CPD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier xml de résultats CPD relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple, <tt>target/cpd.xml</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify Commit Message", "Spécifie le message de commit");
		m.put("Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier de rapport ESLint au format checkstyle sous <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce fichier peut être généré avec l'option ESLint <tt>'-f checkstyle'</tt> et <tt>'-o'</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify GitHub API url, for instance <tt>https://api.github.com</tt>", "Spécifie l'URL de l'API GitHub, par exemple <tt>https://api.github.com</tt>");
		m.put("Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>", "Spécifie l'URL de l'API GitLab, par exemple <tt>https://gitlab.example.com/api/v4</tt>");
		m.put("Specify Gitea API url, for instance <tt>https://gitea.example.com/api/v1</tt>", "Spécifie l'URL de l'API Gitea, par exemple <tt>https://gitea.example.com/api/v1</tt>");
		m.put("Specify GoogleTest XML result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This report can be generated with environment variable <tt>GTEST_OUTPUT</tt> when running tests, For instance, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * or ? for pattern match", 
			"Spécifie le fichier XML de résultats GoogleTest relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce rapport peut être généré avec la variable d'environnement <tt>GTEST_OUTPUT</tt> lors de l'exécution des tests, par exemple, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify IMAP user name.<br><b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system email address specified above", 
			"Spécifie le nom d'utilisateur IMAP.<br><b class='text-danger'>NOTE : </b> Ce compte doit être capable de recevoir les emails envoyés à l'adresse email système spécifiée ci-dessus");
		m.put("Specify JUnit test result file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier de résultats de test JUnit au format XML relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple <tt>target/surefire-reports/TEST-*.xml</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier de rapport de couverture xml JaCoCo relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple, <tt>target/site/jacoco/jacoco.xml</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify Jest coverage report file in clover format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier de rapport de couverture Jest au format clover relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple <tt>coverage/clover.xml</tt>. Ce fichier peut être généré avec l'option Jest <tt>'--coverage'</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify Jest test result file in json format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier de résultats de test Jest au format json relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce fichier peut être généré via l'option Jest <tt>'--json'</tt> et <tt>'--outputFile'</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify OCI layout directory of the image to scan. This directory can be generated via build image step or pull image step. It should be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Spécifie le répertoire de mise en page OCI de l'image à analyser. Ce répertoire peut être généré via l'étape de construction d'image ou l'étape de récupération d'image. Il doit être relatif à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a>");
		m.put("Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from", 
			"Spécifie le répertoire de mise en page OCI relatif à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a> à partir duquel effectuer le push");
		m.put("Specify OpenID scopes to request", "Spécifie les scopes OpenID à demander");
		m.put("Specify PMD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/pmd.xml</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier xml de résultats PMD relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple, <tt>target/pmd.xml</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify PowerShell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of your script<br>", 
			"Spécifie les commandes PowerShell à exécuter sous <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a>.<br><b class='text-warning'>NOTE : </b> OneDev vérifie le code de sortie du script pour déterminer si l'étape est réussie. Étant donné que PowerShell retourne toujours 0 même en cas d'erreurs de script, vous devez gérer les erreurs dans le script et sortir avec un code non nul, ou ajouter la ligne <code>$ErrorActionPreference = &quot;Stop&quot;</code> au début de votre script<br>");
		m.put("Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with <i>-o</i> option. Use * or ? for pattern match", 
			"Spécifie le fichier de sortie des diagnostics Roslynator au format XML relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce fichier peut être généré avec l'option <i>-o</i>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify Shell/Batch Commands to Run", "Spécifie les commandes Shell/Batch à exécuter");
		m.put("Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier xml de résultats SpotBugs relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple, <tt>target/spotbugsXml.xml</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify System Settings", "Spécifie les paramètres système");
		m.put("Specify URL of remote git repository. Only http/https protocol is supported", "Spécifie l'URL du dépôt git distant. Seuls les protocoles http/https sont pris en charge");
		m.put("Specify YouTrack login name. This account should have permission to:<ul><li>Read full information and issues of the projects you want to import<li>Read issue tags<li>Read user basic information</ul>", 
			"Spécifie le nom de connexion YouTrack. Ce compte doit avoir la permission de :<ul><li>Lire les informations complètes et les problèmes des projets que vous souhaitez importer<li>Lire les tags des problèmes<li>Lire les informations de base des utilisateurs</ul>");
		m.put("Specify YouTrack password or access token for above user", "Spécifie le mot de passe ou le jeton d'accès YouTrack pour l'utilisateur ci-dessus");
		m.put("Specify a &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regular expression&lt;/a&gt; to match issue references. For instance:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;", 
			"Spécifie une &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;expression régulière&lt;/a&gt; pour correspondre aux références de problèmes. Par exemple:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> after issue number", 
			"Spécifie une <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>expression régulière</a> après le numéro de problème");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> before issue number", 
			"Spécifie une <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>expression régulière</a> avant le numéro de problème");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> à utiliser comme clé privée SSH");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> à utiliser comme jeton d'accès");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> à utiliser comme jeton d'accès pour importer la spécification de construction du projet ci-dessus si son code n'est pas accessible publiquement");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> à utiliser comme mot de passe ou jeton d'accès du registre");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> à utiliser comme mot de passe ou jeton d'accès pour accéder au dépôt distant");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> à utiliser comme clé privée pour l'authentification SSH. <b class='text-info'>NOTE :</b> Les clés privées avec passphrase ne sont pas prises en charge");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> à utiliser comme clé privée de l'utilisateur ci-dessus pour l'authentification SSH. <b class='text-info'>NOTE :</b> Les clés privées avec passphrase ne sont pas prises en charge");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> dont la valeur est un jeton d'accès avec la permission de gestion pour le projet ci-dessus. Notez que le jeton d'accès n'est pas requis si la synchronisation est effectuée vers le projet actuel ou un projet enfant et que le commit de construction est accessible depuis la branche par défaut");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Spécifie un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secret de job</a> dont la valeur est un jeton d'accès avec la permission de téléchargement de cache pour le projet ci-dessus. Notez que cette propriété n'est pas requise si le téléchargement de cache est effectué vers le projet actuel ou un projet enfant et que le commit de construction est accessible depuis la branche par défaut");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"Spécifie un <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> pour déclencher automatiquement le job. <b class='text-info'>Note :</b> Pour économiser des ressources, les secondes dans l'expression cron seront ignorées, et l'intervalle de planification minimum est d'une minute");
		m.put("Specify a Docker Image to Test Against", "Spécifie une image Docker à tester");
		m.put("Specify a custom field of Enum type", "Spécifie un champ personnalisé de type Enum");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "Spécifie une requête par défaut pour filtrer/ordonner les problèmes résolus des jobs spécifiés");
		m.put("Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into", 
			"Spécifie un fichier relatif à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a> pour écrire le checksum");
		m.put("Specify a multi-value user field to hold assignees information.<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifie un champ utilisateur multi-valeurs pour contenir les informations des assignés.<b>NOTE : </b> Vous pouvez personnaliser les champs de problème OneDev si aucune option appropriée n'est disponible ici");
		m.put("Specify a multi-value user field to hold assignees information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifie un champ utilisateur multi-valeurs pour contenir les informations des assignés.<br><b>NOTE : </b> Vous pouvez personnaliser les champs de problème OneDev si aucune option appropriée n'est disponible ici");
		m.put("Specify a path inside container to be used as mount target", "Spécifie un chemin à l'intérieur du conteneur à utiliser comme cible de montage");
		m.put("Specify a path relative to job workspace to be used as mount source. Leave empty to mount job workspace itself", 
			"Spécifie un chemin relatif à l'espace de travail du job à utiliser comme source de montage. Laissez vide pour monter l'espace de travail du job lui-même");
		m.put("Specify a secret to be used as access token to create issue in above project if it is not publicly accessible", 
			"Spécifie un secret à utiliser comme jeton d'accès pour créer un problème dans le projet ci-dessus s'il n'est pas accessible publiquement");
		m.put("Specify a secret to be used as access token to retrieve artifacts from above project. If not specified, project artifacts will be accessed anonymously", 
			"Spécifie un secret à utiliser comme jeton d'accès pour récupérer les artefacts du projet ci-dessus. Si non spécifié, les artefacts du projet seront accessibles anonymement");
		m.put("Specify a secret to be used as access token to trigger job in above project", "Spécifiez un secret à utiliser comme jeton d'accès pour déclencher le travail dans le projet ci-dessus");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Spécifie un secret dont la valeur est un jeton d'accès avec la permission de téléchargement de cache pour le projet ci-dessus. Notez que cette propriété n'est pas requise si le téléchargement de cache est effectué vers le projet actuel ou un projet enfant et que le commit de construction est accessible depuis la branche par défaut");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"Spécifie le chemin absolu vers le fichier de configuration utilisé par kubectl pour accéder au cluster. Laissez vide pour que kubectl détermine automatiquement les informations d'accès au cluster");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"Spécifie le chemin absolu vers l'utilitaire kubectl, par exemple : <i>/usr/bin/kubectl</i>. Si laissé vide, OneDev essaiera de trouver l'utilitaire dans le chemin système");
		m.put("Specify account name to login to Gmail to send/receive email", "Spécifie le nom de compte pour se connecter à Gmail pour envoyer/recevoir des emails");
		m.put("Specify additional users able to access this confidential issue besides those granted via role. Users mentioned in the issue will be authorized automatically", 
			"Spécifie des utilisateurs supplémentaires pouvant accéder à ce problème confidentiel en plus de ceux autorisés via le rôle. Les utilisateurs mentionnés dans le problème seront automatiquement autorisés");
		m.put("Specify agents applicable for this executor", "Spécifie les agents applicables à cet exécuteur");
		m.put("Specify allowed <a href='https://spdx.org/licenses/' target='_blank'>spdx license identifiers</a> <span class='text-warning'>separated by comma</span>", 
			"Spécifie les <a href='https://spdx.org/licenses/' target='_blank'>identifiants de licence spdx autorisés</a> <span class='text-warning'>séparés par des virgules</span>");
		m.put("Specify an email address sharing same inbox as the system email address in mail setting definition. Emails sent to this address will be created as issues in this project. The default value takes form of <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>", 
			"Spécifie une adresse email partageant la même boîte de réception que l'adresse email système dans la définition des paramètres de messagerie. Les emails envoyés à cette adresse seront créés comme problèmes dans ce projet. La valeur par défaut prend la forme de <tt>&lt;nom de l'adresse email système&gt;+&lt;chemin du projet&gt;@&lt;domaine de l'adresse email système&gt;</tt>");
		m.put("Specify applicable projects for above option. Multiple projects should be separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Spécifie les projets applicables pour l'option ci-dessus. Plusieurs projets doivent être séparés par un espace. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour tous les projets");
		m.put("Specify applicable projects separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Spécifie les projets applicables séparés par un espace. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de modèle de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour tous les projets");
		m.put("Specify application (client) ID of the app registered in Entra ID", "Spécifie l'ID de l'application (client) de l'application enregistrée dans Entra ID");
		m.put("Specify arguments for imagetools. For instance <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>", 
			"Spécifie les arguments pour imagetools. Par exemple <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>");
		m.put("Specify artifacts to retrieve into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved.", 
			"Spécifie les artefacts à récupérer dans <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Seuls les artefacts publiés (via l'étape de publication d'artefacts) peuvent être récupérés.");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"Spécifie au moins 10 caractères alphanumériques à utiliser comme secret, puis ajoutez une entrée de parse inbound côté SendGrid :<ul><li><code>URL de destination</code> doit être défini sur <i>&lt;URL racine OneDev&gt;/~sendgrid/&lt;secret&gt;</i>, par exemple, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Notez qu'en environnement de production, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https doit être activé</a> pour protéger le secret</li><li><code>Domaine de réception</code> doit être le même que la partie domaine de l'adresse email système spécifiée ci-dessus</li><li>L'option <code>POST le message MIME brut et complet</code> est activée</li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"Spécifie les nœuds de base pour la recherche des utilisateurs. Par exemple : <i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "Spécifie la branche pour commettre le changement suggéré");
		m.put("Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Spécifiez la branche pour exécuter le travail. Soit la branche, soit le tag peut être spécifié, mais pas les deux. La branche par défaut sera utilisée si aucun n'est spécifié.");
		m.put("Specify branch, tag or commit in above project to import build spec from", "Spécifie la branche, le tag ou le commit dans le projet ci-dessus pour importer la spécification de construction");
		m.put("Specify by Build Number", "Spécifie par numéro de construction");
		m.put("Specify cache upload strategy after build successful. <var>Upload If Not Hit</var> means to upload when cache is not found with cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files in cache path are changed", 
			"Spécifie la stratégie de téléchargement de cache après une construction réussie. <var>Upload If Not Hit</var> signifie télécharger lorsque le cache n'est pas trouvé avec la clé de cache (pas les clés de chargement), et <var>Upload If Changed</var> signifie télécharger si certains fichiers dans le chemin de cache sont modifiés");
		m.put("Specify certificate to trust if you are using self-signed certificate for remote repository", 
			"Spécifie le certificat à faire confiance si vous utilisez un certificat auto-signé pour le dépôt distant");
		m.put("Specify certificates to trust if you are using self-signed certificates for your docker registries", 
			"Spécifie les certificats à faire confiance si vous utilisez des certificats auto-signés pour vos registres Docker");
		m.put("Specify checkstyle result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/checkstyle-result.xml</tt>. Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> on how to generate the result xml file. Use * or ? for pattern match", 
			"Spécifie le fichier xml de résultats checkstyle relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple, <tt>target/checkstyle-result.xml</tt>. Consultez la <a href='https://checkstyle.org/'>documentation checkstyle</a> pour savoir comment générer le fichier xml de résultats. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify client secret of the app registered in Entra ID", "Spécifie le secret client de l'application enregistrée dans Entra ID");
		m.put("Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/clover/clover.xml</tt>. Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> on how to generate clover xml file. Use * or ? for pattern match", 
			"Spécifie le fichier de rapport de couverture xml clover relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple, <tt>target/site/clover/clover.xml</tt>. Consultez la <a href='https://openclover.org/documentation'>documentation OpenClover</a> pour savoir comment générer le fichier xml clover. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match", 
			"Spécifie le fichier de rapport de couverture xml cobertura relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple, <tt>target/site/cobertura/coverage.xml</tt>. Utilisez * ou ? pour la correspondance de modèle");
		m.put("Specify color of the state for displaying purpose", "Spécifie la couleur de l'état à des fins d'affichage");
		m.put("Specify columns of the board. Each column corresponds to a value of the issue field specified above", 
			"Spécifie les colonnes du tableau. Chaque colonne correspond à une valeur du champ de problème spécifié ci-dessus");
		m.put("Specify command to check readiness of the service. This command will be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be executed repeatedly until a zero code is returned to indicate service ready", 
			"Spécifie la commande pour vérifier la disponibilité du service. Cette commande sera interprétée par cmd.exe sur les images Windows, et par shell sur les images Linux. Elle sera exécutée de manière répétée jusqu'à ce qu'un code zéro soit retourné pour indiquer que le service est prêt");
		m.put("Specify commands to be executed on remote machine. <b class='text-warning'>Note:</b> user environments will not be picked up when execute these commands, set up them explicitly in commands if necessary", 
			"Spécifiez les commandes à exécuter sur la machine distante. <b class='text-warning'>Remarque :</b> les environnements utilisateur ne seront pas pris en compte lors de l'exécution de ces commandes, configurez-les explicitement dans les commandes si nécessaire");
		m.put("Specify condition to retry build upon failure", "Spécifiez la condition pour relancer la construction en cas d'échec");
		m.put("Specify configuration discovery url of your OpenID provider, for instance: <code>https://openid.example.com/.well-known/openid-configuration</code>. Make sure to use HTTPS protocol as OneDev relies on TLS encryption to ensure token validity", 
			"Spécifiez l'URL de découverte de configuration de votre fournisseur OpenID, par exemple : <code>https://openid.example.com/.well-known/openid-configuration</code>. Assurez-vous d'utiliser le protocole HTTPS car OneDev s'appuie sur le chiffrement TLS pour garantir la validité des jetons");
		m.put("Specify container image to execute commands inside", "Spécifiez l'image du conteneur pour exécuter les commandes");
		m.put("Specify container image to run", "Spécifiez l'image du conteneur à exécuter");
		m.put("Specify cppcheck xml result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match", 
			"Spécifiez le fichier de résultats XML de cppcheck relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce fichier peut être généré avec l'option de sortie XML de cppcheck, par exemple <code>cppcheck src --xml 2>check-result.xml</code>. Utilisez * ou ? pour correspondre au modèle");
		m.put("Specify cpu request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Spécifiez la demande de CPU pour chaque job/service utilisant cet exécuteur. Consultez <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>la gestion des ressources Kubernetes</a> pour plus de détails");
		m.put("Specify default assignees of pull requests submitted to this project. Only users with the write code permission to the project can be selected", 
			"Spécifiez les assignés par défaut des pull requests soumises à ce projet. Seuls les utilisateurs ayant la permission d'écriture de code pour le projet peuvent être sélectionnés");
		m.put("Specify default merge strategy of pull requests submitted to this project", "Spécifiez la stratégie de fusion par défaut des pull requests soumises à ce projet");
		m.put("Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple destinations should be separated with space", 
			"Spécifiez les destinations, par exemple <tt>registry-server:5000/myorg/myrepo:latest</tt>. Assurez-vous d'utiliser <b>le même hôte</b> que celui spécifié dans l'URL du serveur des paramètres système si vous souhaitez pousser vers le registre intégré, ou utilisez simplement la forme <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Les destinations multiples doivent être séparées par un espace");
		m.put("Specify directory (tenant) ID of the app registered in Entra ID", "Spécifiez l'ID du répertoire (tenant) de l'application enregistrée dans Entra ID");
		m.put("Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Spécifiez le répertoire relatif à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a> pour stocker la disposition OCI");
		m.put("Specify docker image of the service", "Spécifiez l'image Docker du service");
		m.put("Specify dockerx builder used to build docker image. OneDev will create the builder automatically if it does not exist. Check <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> on how to customize the builder for instance to allow publishing to insecure registries", 
			"Spécifiez le builder dockerx utilisé pour construire l'image Docker. OneDev créera automatiquement le builder s'il n'existe pas. Consultez <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>ce tutoriel</a> pour savoir comment personnaliser le builder, par exemple pour permettre la publication dans des registres non sécurisés");
		m.put("Specify email addresses to send invitations, with one per line", "Spécifiez les adresses email pour envoyer les invitations, une par ligne");
		m.put("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\"", 
			"Spécifiez le temps estimé <b class='text-warning'>uniquement pour ce problème</b>, sans compter \"{0}\"");
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"Spécifiez les champs des différents problèmes créés par Renovate pour orchestrer la mise à jour des dépendances");
		m.put("Specify fields to be displayed in the issue list", "Spécifiez les champs à afficher dans la liste des problèmes");
		m.put("Specify fields to display in board card", "Spécifiez les champs à afficher sur la carte du tableau");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"Spécifiez les fichiers relatifs à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a> à publier. Utilisez * ou ? pour correspondre au modèle");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Spécifiez les fichiers pour créer une somme de contrôle md5. Les fichiers multiples doivent être séparés par un espace. Les modèles <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> sont acceptés. Un fichier non absolu est considéré comme relatif à <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a>");
		m.put("Specify files under above directory to be published", "Spécifiez les fichiers sous le répertoire ci-dessus à publier");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be included in these files to be served as site start page", 
			"Spécifiez les fichiers sous le répertoire ci-dessus à publier. Utilisez * ou ? pour correspondre au modèle. <b>REMARQUE :</b> <code>index.html</code> doit être inclus dans ces fichiers pour être servi comme page de démarrage du site");
		m.put("Specify group to import from. Leave empty to import from projects under current account", 
			"Spécifiez le groupe à importer. Laissez vide pour importer depuis les projets sous le compte actuel");
		m.put("Specify how to map GitHub issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez comment mapper les étiquettes de problèmes GitHub aux champs personnalisés de OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les champs de problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify how to map GitLab issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez comment mapper les étiquettes de problèmes GitLab aux champs personnalisés de OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les champs de problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify how to map Gitea issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez comment mapper les étiquettes de problèmes Gitea aux champs personnalisés de OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les champs de problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify how to map JIRA issue priorities to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez comment mapper les priorités des problèmes JIRA aux champs personnalisés de OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les champs de problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify how to map JIRA issue statuses to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Spécifiez comment mapper les statuts des problèmes JIRA aux champs personnalisés de OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les états des problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify how to map JIRA issue types to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez comment mapper les types de problèmes JIRA aux champs personnalisés de OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les champs de problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify how to map YouTrack issue fields to OneDev. Unmapped fields will be reflected in issue description.<br><b>Note: </b><ul><li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, for instance <tt>Priority::Critical</tt><li>You may customize OneDev issue fields in case there is no appropriate option here</ul>", 
			"Spécifiez comment mapper les champs des problèmes YouTrack à OneDev. Les champs non mappés seront reflétés dans la description du problème.<br><b>Remarque :</b><ul><li>Un champ d'énumération doit être mappé sous la forme <tt>&lt;Nom du champ&gt;::&lt;Valeur du champ&gt;</tt>, par exemple <tt>Priorité::Critique</tt><li>Vous pouvez personnaliser les champs de problèmes OneDev au cas où aucune option appropriée ne serait disponible ici</ul>");
		m.put("Specify how to map YouTrack issue links to OneDev issue links.<br><b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here", 
			"Spécifiez comment mapper les liens des problèmes YouTrack aux liens des problèmes OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les liens des problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify how to map YouTrack issue state to OneDev issue state. Unmapped states will use the initial state in OneDev.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Spécifiez comment mapper l'état des problèmes YouTrack à l'état des problèmes OneDev. Les états non mappés utiliseront l'état initial dans OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les états des problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify how to map YouTrack issue tags to OneDev issue custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Spécifiez comment mapper les tags des problèmes YouTrack aux champs personnalisés des problèmes OneDev.<br><b>REMARQUE :</b> Vous pouvez personnaliser les champs de problèmes OneDev au cas où aucune option appropriée ne serait disponible ici");
		m.put("Specify image on the login button", "Spécifiez l'image sur le bouton de connexion");
		m.put("Specify image tag to pull from, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to pull from built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Spécifiez le tag de l'image à tirer, par exemple <tt>registry-server:5000/myorg/myrepo:latest</tt>. Assurez-vous d'utiliser <b>le même hôte</b> que celui spécifié dans l'URL du serveur des paramètres système si vous souhaitez tirer depuis le registre intégré, ou utilisez simplement la forme <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>");
		m.put("Specify image tag to push to, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Spécifiez le tag de l'image à pousser, par exemple <tt>registry-server:5000/myorg/myrepo:latest</tt>. Assurez-vous d'utiliser <b>le même hôte</b> que celui spécifié dans l'URL du serveur des paramètres système si vous souhaitez pousser vers le registre intégré, ou utilisez simplement la forme <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>");
		m.put("Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple tags should be separated with space", 
			"Spécifiez les tags d'image à pousser, par exemple <tt>registry-server:5000/myorg/myrepo:latest</tt>. Assurez-vous d'utiliser <b>le même hôte</b> que celui spécifié dans l'URL du serveur des paramètres système si vous souhaitez pousser vers le registre intégré, ou utilisez simplement la forme <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Les tags multiples doivent être séparés par un espace");
		m.put("Specify import option", "Spécifiez l'option d'importation");
		m.put("Specify incoming email poll interval in seconds", "Spécifiez l'intervalle de sondage des emails entrants en secondes");
		m.put("Specify issue creation settings. For a particular sender and project, the first matching entry will take effect. Issue creation will be disallowed if no matching entry found", 
			"Spécifiez les paramètres de création de problème. Pour un expéditeur et un projet particuliers, la première entrée correspondante prendra effet. La création de problème sera interdite si aucune entrée correspondante n'est trouvée");
		m.put("Specify issue field to identify different columns of the board. Only state and single-valued enumeration field can be used here", 
			"Spécifiez le champ de problème pour identifier les différentes colonnes du tableau. Seuls les champs d'état et d'énumération à valeur unique peuvent être utilisés ici");
		m.put("Specify links to be displayed in the issue list", "Spécifiez les liens à afficher dans la liste des problèmes");
		m.put("Specify links to display in board card", "Spécifiez les liens à afficher sur la carte du tableau");
		m.put("Specify manager DN to authenticate OneDev itself to Active Directory. The manager DN should be specified in form of <i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>manager@example.com</i>", 
			"Spécifiez le DN du gestionnaire pour authentifier OneDev lui-même auprès d'Active Directory. Le DN du gestionnaire doit être spécifié sous la forme <i>&lt;nom du compte&gt;@&lt;domaine&gt;</i>, par exemple : <i>manager@example.com</i>");
		m.put("Specify manager DN to authenticate OneDev itself to LDAP server", "Spécifiez le DN du gestionnaire pour authentifier OneDev lui-même auprès du serveur LDAP");
		m.put("Specify markdown file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published", 
			"Spécifiez le fichier markdown relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a> à publier");
		m.put("Specify max git LFS file size in mega bytes", "Spécifiez la taille maximale des fichiers Git LFS en mégaoctets");
		m.put("Specify max number of CPU intensive tasks the server can run concurrently, such as Git repository pull/push, repository index, etc.", 
			"Spécifiez le nombre maximal de tâches intensives en CPU que le serveur peut exécuter simultanément, telles que le pull/push de dépôt Git, l'indexation de dépôt, etc.");
		m.put("Specify max number of jobs this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Spécifiez le nombre maximal de jobs que cet exécuteur peut exécuter simultanément sur chaque agent correspondant. Laissez vide pour définir en fonction des cœurs CPU de l'agent");
		m.put("Specify max number of jobs this executor can run concurrently. Leave empty to set as CPU cores", 
			"Spécifiez le nombre maximal de jobs que cet exécuteur peut exécuter simultanément. Laissez vide pour définir en fonction des cœurs CPU");
		m.put("Specify max number of jobs/services this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Spécifiez le nombre maximal de jobs/services que cet exécuteur peut exécuter simultanément sur chaque agent correspondant. Laissez vide pour définir en fonction des cœurs CPU de l'agent");
		m.put("Specify max number of jobs/services this executor can run concurrently. Leave empty to set as CPU cores", 
			"Spécifiez le nombre maximal de jobs/services que cet exécuteur peut exécuter simultanément. Laissez vide pour définir en fonction des cœurs CPU");
		m.put("Specify max size of uploaded file in mega bytes via web interface. This applies to file uploaded to repository, markdown content (issue comment etc), and build artifacts", 
			"Spécifiez la taille maximale des fichiers téléchargés via l'interface web en mégaoctets. Cela s'applique aux fichiers téléchargés dans le dépôt, au contenu markdown (commentaire de problème, etc.) et aux artefacts de construction");
		m.put("Specify memory request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Spécifiez la demande de mémoire pour chaque job/service utilisant cet exécuteur. Consultez <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>la gestion des ressources Kubernetes</a> pour plus de détails");
		m.put("Specify mypy output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated by redirecting mypy output <b>without option '--pretty'</b>, for instance <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * or ? for pattern match", 
			"Spécifiez le fichier de sortie mypy relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce fichier peut être généré en redirigeant la sortie de mypy <b>sans l'option '--pretty'</b>, par exemple <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Utilisez * ou ? pour correspondre au modèle");
		m.put("Specify name of the branch", "Spécifiez le nom de la branche");
		m.put("Specify name of the environment variable", "Spécifiez le nom de la variable d'environnement");
		m.put("Specify name of the iteration", "Spécifiez le nom de l'itération");
		m.put("Specify name of the job", "Spécifiez le nom du job");
		m.put("Specify name of the report to be displayed in build detail page", "Spécifiez le nom du rapport à afficher sur la page de détail de la construction");
		m.put("Specify name of the saved query", "Spécifiez le nom de la requête enregistrée");
		m.put("Specify name of the service, which will be used as host name to access the service", 
			"Spécifiez le nom du service, qui sera utilisé comme nom d'hôte pour accéder au service");
		m.put("Specify name of the tag", "Spécifiez le nom du tag");
		m.put("Specify network timeout in seconds when authenticate through this system", "Spécifiez le délai d'attente réseau en secondes lors de l'authentification via ce système");
		m.put("Specify node selector of this locator", "Spécifiez le sélecteur de nœud de ce localisateur");
		m.put("Specify password or access token of specified registry", "Spécifiez le mot de passe ou le jeton d'accès du registre spécifié");
		m.put("Specify password to authenticate with", "Spécifiez le mot de passe pour s'authentifier");
		m.put("Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>", "Spécifiez le chemin vers l'exécutable curl, par exemple : <tt>/usr/bin/curl</tt>");
		m.put("Specify path to git executable, for instance: <tt>/usr/bin/git</tt>", "Spécifiez le chemin vers l'exécutable git, par exemple : <tt>/usr/bin/git</tt>");
		m.put("Specify powershell executable to be used", "Spécifiez l'exécutable powershell à utiliser");
		m.put("Specify project to import build spec from", "Spécifiez le projet à partir duquel importer la spécification de construction");
		m.put("Specify project to import into at OneDev side", "Spécifiez le projet à importer dans le côté OneDev");
		m.put("Specify project to retrieve artifacts from", "Spécifiez le projet à partir duquel récupérer les artefacts");
		m.put("Specify project to run job in", "Spécifiez le projet pour exécuter le travail");
		m.put("Specify projects", "Spécifiez les projets");
		m.put("Specify projects to update dependencies. Leave empty for current project", "Spécifiez les projets pour mettre à jour les dépendances. Laissez vide pour le projet actuel");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Spécifiez le fichier de résultats JSON pylint relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce fichier peut être généré avec l'option de format de sortie JSON pylint, par exemple <code>--exit-zero --output-format=json:pylint-result.json</code>. Notez que nous ne faisons pas échouer la commande pylint en cas de violations, car cette étape échouera la construction en fonction du seuil configuré. Utilisez * ou ? pour correspondre au modèle");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"Spécifiez les connexions au registre si nécessaire. Pour le registre intégré, utilisez <code>@server_url@</code> pour l'URL du registre, <code>@job_token@</code> pour le nom d'utilisateur et le jeton d'accès pour le mot de passe");
		m.put("Specify registry url. Leave empty for official registry", "Spécifiez l'URL du registre. Laissez vide pour le registre officiel");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Spécifiez le chemin relatif sous <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a> pour stocker la disposition OCI");
		m.put("Specify repositories", "Spécifiez les dépôts");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"Spécifiez les réviseurs requis si le chemin spécifié est modifié. Notez que l'utilisateur soumettant la modification est automatiquement considéré comme ayant révisé la modification");
		m.put("Specify root URL to access this server", "Spécifiez l'URL racine pour accéder à ce serveur");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Spécifiez le fichier de résultats JSON ruff relatif à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>. Ce fichier peut être généré avec l'option de format de sortie JSON ruff, par exemple <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Notez que nous ne faisons pas échouer la commande ruff en cas de violations, car cette étape échouera la construction en fonction du seuil configuré. Utilisez * ou ? pour correspondre au modèle");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Spécifiez les commandes shell (sur Linux/Unix) ou les commandes batch (sur Windows) à exécuter sous <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a>");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Spécifiez les commandes shell à exécuter sous <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail du job</a>");
		m.put("Specify shell to be used", "Spécifiez le shell à utiliser");
		m.put("Specify source param for SCP command, for instance <code>app.tar.gz</code>", "Spécifiez le paramètre source pour la commande SCP, par exemple <code>app.tar.gz</code>");
		m.put("Specify space separated refs to pull from remote. '*' can be used in ref name for wildcard match<br><b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update branches/tags via this step", 
			"Spécifiez les refs séparés par des espaces à tirer du distant. '*' peut être utilisé dans le nom du ref pour une correspondance par wildcard<br><b class='text-danger'>REMARQUE :</b> la règle de protection des branches/tags sera ignorée lors de la mise à jour des branches/tags via cette étape");
		m.put("Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Spécifiez les branches séparées par des espaces à protéger. Utilisez '**', '*' ou '?' pour une <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondance par wildcard de chemin</a>. Préfixez avec '-' pour exclure");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Spécifiez les jobs séparés par des espaces. Utilisez '*' ou '?' pour une correspondance par wildcard. Préfixez avec '-' pour exclure");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts will be granted implicitly in matched jobs even if no other permissions are specified here", 
			"Spécifiez les jobs séparés par des espaces. Utilisez '*' ou '?' pour une correspondance par wildcard. Préfixez avec '-' pour exclure. <b class='text-danger'>REMARQUE :</b> La permission d'accéder aux artefacts de construction sera accordée implicitement dans les jobs correspondants même si aucune autre permission n'est spécifiée ici");
		m.put("Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Spécifiez les chemins séparés par des espaces à protéger. Utilisez '**', '*' ou '?' pour une <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondance par wildcard de chemin</a>. Préfixez avec '-' pour exclure");
		m.put("Specify space-separated projects applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Spécifiez les projets séparés par des espaces applicables à cette entrée. Utilisez '*' ou '?' pour une correspondance par wildcard. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à tous les projets");
		m.put("Specify space-separated sender email addresses applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all senders", 
			"Spécifiez les adresses email d'expéditeur séparées par des espaces applicables à cette entrée. Utilisez '*' ou '?' pour une correspondance par wildcard. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à tous les expéditeurs");
		m.put("Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Spécifiez les tags séparés par des espaces à protéger. Utilisez '**', '*' ou '?' pour une <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondance par wildcard de chemin</a>. Préfixez avec '-' pour exclure");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>", 
			"Spécifiez la page de démarrage du rapport relative à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple : <tt>manual/index.md</tt>");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"Spécifiez la page de démarrage du rapport relative à <a href='https://docs.onedev.io/concepts#job-workspace'>l'espace de travail du job</a>, par exemple : api/index.html");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"Spécifiez la taille de stockage à demander pour le volume de construction. La taille doit être conforme au <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>format de capacité des ressources Kubernetes</a>, par exemple <i>10Gi</i>");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"Spécifiez la largeur de tabulation utilisée pour calculer la valeur de colonne des problèmes trouvés dans le rapport fourni");
		m.put("Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Spécifiez le tag pour exécuter le travail. Soit la branche, soit le tag peut être spécifié, mais pas les deux. La branche par défaut sera utilisée si aucun n'est spécifié.");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"Spécifiez le paramètre cible pour la commande SCP, par exemple <code>user@@host:/app</code>. <b class='text-info'>REMARQUE :</b> Assurez-vous que la commande scp est installée sur l'hôte distant");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"Spécifiez le texte pour remplacer les références aux problèmes correspondants, par exemple : &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Ici, $1 et $2 représentent les groupes capturés dans le modèle de problème exemple (voir l'aide sur le modèle de problème)");
		m.put("Specify the condition current build must satisfy to execute this action", "Spécifiez la condition que la construction actuelle doit satisfaire pour exécuter cette action");
		m.put("Specify the condition preserved builds must match", "Spécifiez la condition que les constructions conservées doivent respecter");
		m.put("Specify the private key (in PEM format) used by SSH server to establish connections with client", 
			"Spécifiez la clé privée (au format PEM) utilisée par le serveur SSH pour établir des connexions avec le client");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"Spécifiez la stratégie pour récupérer les informations d'appartenance au groupe. Pour attribuer des permissions appropriées à un groupe LDAP, un groupe OneDev portant le même nom doit être défini. Utilisez la stratégie <tt>Ne pas récupérer les groupes</tt> si vous souhaitez gérer les appartenances aux groupes du côté de OneDev");
		m.put("Specify timeout in seconds when communicating with mail server", "Spécifiez le délai d'attente en secondes lors de la communication avec le serveur de messagerie");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "Spécifiez le délai d'attente en secondes. Il commence à partir du moment où le travail est soumis");
		m.put("Specify title of the issue", "Spécifiez le titre du problème");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "Spécifiez l'URL de l'API YouTrack. Par exemple <tt>http://localhost:8080/api</tt>");
		m.put("Specify user name of above machine for SSH authentication", "Spécifiez le nom d'utilisateur de la machine ci-dessus pour l'authentification SSH");
		m.put("Specify user name of specified registry", "Spécifiez le nom d'utilisateur du registre spécifié");
		m.put("Specify user name of the registry", "Spécifiez le nom d'utilisateur du registre");
		m.put("Specify user name to authenticate with", "Spécifiez le nom d'utilisateur pour l'authentification");
		m.put("Specify value of the environment variable", "Spécifiez la valeur de la variable d'environnement");
		m.put("Specify web UI session timeout in minutes. Existing sessions will not be affected after changing this value.", 
			"Spécifiez le délai d'expiration de la session de l'interface utilisateur web en minutes. Les sessions existantes ne seront pas affectées après la modification de cette valeur.");
		m.put("Specify webhook url to post events", "Spécifiez l'URL du webhook pour publier des événements");
		m.put("Specify which issue state to use for closed GitHub issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Spécifiez l'état du problème à utiliser pour les problèmes GitHub fermés.<br><b>REMARQUE :</b> Vous pouvez personnaliser les états des problèmes OneDev si aucune option appropriée n'est disponible ici");
		m.put("Specify which issue state to use for closed GitLab issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Spécifiez l'état du problème à utiliser pour les problèmes GitLab fermés.<br><b>REMARQUE :</b> Vous pouvez personnaliser les états des problèmes OneDev si aucune option appropriée n'est disponible ici");
		m.put("Specify which issue state to use for closed Gitea issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Spécifiez l'état du problème à utiliser pour les problèmes Gitea fermés.<br><b>REMARQUE :</b> Vous pouvez personnaliser les états des problèmes OneDev si aucune option appropriée n'est disponible ici");
		m.put("Specify which states are considered as closed for various issues created by Renovate to orchestrate dependency update. Additionally, when Renovate closes the issue, OneDev will transit the issue to first state specified here", 
			"Spécifiez quels états sont considérés comme fermés pour divers problèmes créés par Renovate pour orchestrer la mise à jour des dépendances. De plus, lorsque Renovate ferme le problème, OneDev fera passer le problème au premier état spécifié ici");
		m.put("Specify working days per week. This will affect parsing and displaying of working periods. For instance <tt>1w</tt> is the same as <tt>5d</tt> if this property is set to <tt>5</tt>", 
			"Spécifiez les jours ouvrables par semaine. Cela affectera l'analyse et l'affichage des périodes de travail. Par exemple <tt>1w</tt> est équivalent à <tt>5d</tt> si cette propriété est définie sur <tt>5</tt>");
		m.put("Specify working hours per day. This will affect parsing and displaying of working periods. For instance <tt>1d</tt> is the same as <tt>8h</tt> if this property is set to <tt>8</tt>", 
			"Spécifiez les heures de travail par jour. Cela affectera l'analyse et l'affichage des périodes de travail. Par exemple <tt>1d</tt> est équivalent à <tt>8h</tt> si cette propriété est définie sur <tt>8</tt>");
		m.put("Spent", "Temps passé");
		m.put("Spent Time", "Temps passé");
		m.put("Spent Time Issue Field", "Champ de problème du temps passé");
		m.put("Spent Time:", "Temps passé :");
		m.put("Spent time / estimated time", "Temps passé / temps estimé");
		m.put("Split", "Diviser");
		m.put("Split view", "Vue fractionnée");
		m.put("SpotBugs Report", "Rapport SpotBugs");
		m.put("Squash Source Branch Commits", "Fusionner les commits de la branche source");
		m.put("Squash all commits from source branch into a single commit in target branch", "Fusionnez tous les commits de la branche source en un seul commit dans la branche cible");
		m.put("Squash source branch commits", "Fusionner les commits de la branche source");
		m.put("Ssh", "Ssh");
		m.put("Ssh Key", "Clé Ssh");
		m.put("Ssh Setting", "Paramètre Ssh");
		m.put("Ssl Setting", "Paramètre Ssl");
		m.put("Sso Connector", "Connecteur Sso");
		m.put("Sso Provider Bean", "Fournisseur SSO Bean");
		m.put("Start At", "Commencer à");
		m.put("Start Date", "Date de début");
		m.put("Start Page", "Page de démarrage");
		m.put("Start agent on remote Linux machine by running below command:", "Démarrez l'agent sur une machine Linux distante en exécutant la commande ci-dessous :");
		m.put("Start date", "Date de début");
		m.put("Start to watch once I am involved", "Commencez à surveiller une fois que je suis impliqué");
		m.put("Start work", "Commencer le travail");
		m.put("Start/Due Date", "Date de début/échéance");
		m.put("State", "État");
		m.put("State Durations", "Durées des états");
		m.put("State Frequencies", "Fréquences des états");
		m.put("State Spec", "Spécification des états");
		m.put("State Transitions", "Transitions des états");
		m.put("State Trends", "Tendances des états");
		m.put("State of an issue is transited", "L'état d'un problème est transité");
		m.put("States", "États");
		m.put("Statistics", "Statistiques");
		m.put("Stats", "Statistiques");
		m.put("Stats Group", "Groupe de statistiques");
		m.put("Status", "Statut");
		m.put("Status Code", "Code de statut");
		m.put("Status code", "Code de statut");
		m.put("Status code other than 200 indicating the error type", "Code de statut autre que 200 indiquant le type d'erreur");
		m.put("Step", "Étape");
		m.put("Step Template", "Modèle d'étape");
		m.put("Step Templates", "Modèles d'étape");
		m.put("Step {0} of {1}: ", "Étape {0} de {1} :");
		m.put("Steps", "Étapes");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"Les étapes seront exécutées séquentiellement sur le même nœud, partageant le même <a href='https://docs.onedev.io/concepts#job-workspace'>espace de travail du travail</a>");
		m.put("Stop work", "Arrêter le travail");
		m.put("Stopwatch Overdue", "Chronomètre en retard");
		m.put("Storage Settings", "Paramètres de stockage");
		m.put("Storage file missing", "Fichier de stockage manquant");
		m.put("Storage not found", "Stockage introuvable");
		m.put("Stored with Git LFS", "Stocké avec Git LFS");
		m.put("Sub Keys", "Sous-clés");
		m.put("Subject", "Sujet");
		m.put("Submit", "Soumettre");
		m.put("Submit Reason", "Raison de la soumission");
		m.put("Submit Support Request", "Soumettre une demande de support");
		m.put("Submitted After", "Soumis après");
		m.put("Submitted At", "Soumis à");
		m.put("Submitted Before", "Soumis avant");
		m.put("Submitted By", "Soumis par");
		m.put("Submitted manually", "Soumis manuellement");
		m.put("Submitter", "Soumetteur");
		m.put("Subscription Key", "Clé d'abonnement");
		m.put("Subscription Management", "Gestion des abonnements");
		m.put("Subscription data", "Données d'abonnement");
		m.put("Subscription key installed successfully", "Clé d'abonnement installée avec succès");
		m.put("Subscription key not applicable: this key is intended to activate a trial subscription", 
			"Clé d'abonnement non applicable : cette clé est destinée à activer un abonnement d'essai");
		m.put("Subscription key not applicable: this key is intended to renew a user based subscription", 
			"Clé d'abonnement non applicable : cette clé est destinée à renouveler un abonnement basé sur l'utilisateur");
		m.put("Subscription key not applicable: this key is intended to renew an unlimited users subscription", 
			"Clé d'abonnement non applicable : cette clé est destinée à renouveler un abonnement pour utilisateurs illimités");
		m.put("Subscription key not applicable: this key is intended to update licensee of an existing subscription", 
			"Clé d'abonnement non applicable : cette clé est destinée à mettre à jour le titulaire de licence d'un abonnement existant");
		m.put("Success Rate", "Taux de réussite");
		m.put("Successful", "Réussi");
		m.put("Suffix Pattern", "Modèle de suffixe");
		m.put("Suggest changes", "Suggérer des modifications");
		m.put("Suggested change", "Modification suggérée");
		m.put("Suggestion is outdated either due to code change or pull request close", "La suggestion est obsolète en raison d'un changement de code ou de la fermeture de la demande de tirage");
		m.put("Suggestions", "Suggestions");
		m.put("Summary", "Résumé");
		m.put("Support & Bug Report", "Support & Rapport de bug");
		m.put("Support Request", "Demande de support");
		m.put("Swap", "Échanger");
		m.put("Switch to HTTP(S)", "Passer à HTTP(S)");
		m.put("Switch to SSH", "Passer à SSH");
		m.put("Symbol Name", "Nom du symbole");
		m.put("Symbol name", "Nom du symbole");
		m.put("Symbols", "Symboles");
		m.put("Sync Replica Status and Back to Home", "Synchroniser le statut de la réplique et retourner à l'accueil");
		m.put("Sync Repository", "Synchroniser le dépôt");
		m.put("Sync Timing of All Queried Issues", "Synchroniser le timing de toutes les issues interrogées");
		m.put("Sync Timing of Selected Issues", "Synchroniser le timing des issues sélectionnées");
		m.put("Sync requested. Please check status after a while", "Synchronisation demandée. Veuillez vérifier le statut après un moment");
		m.put("Synchronize", "Synchroniser");
		m.put("System", "Système");
		m.put("System Alert", "Alerte système");
		m.put("System Alert Template", "Modèle d'alerte système");
		m.put("System Date", "Date système");
		m.put("System Email Address", "Adresse email système");
		m.put("System Maintenance", "Maintenance système");
		m.put("System Setting", "Paramètre système");
		m.put("System Settings", "Paramètres système");
		m.put("System email address defined in mail setting should be used as recipient of such email, and project name may be appended to this address using to indicate where to create issues. For instance, if system email address is specified as <tt>support@example.com</tt>, sending email to <tt>support+myproject@example.com</tt> will create issue in <tt>myproject</tt>. If project name is not appended, OneDev will look up the project using project designation information below", 
			"L'adresse email système définie dans les paramètres de messagerie doit être utilisée comme destinataire de cet email, et le nom du projet peut être ajouté à cette adresse pour indiquer où créer les issues. Par exemple, si l'adresse email système est spécifiée comme <tt>support@example.com</tt>, envoyer un email à <tt>support+myproject@example.com</tt> créera une issue dans <tt>myproject</tt>. Si le nom du projet n'est pas ajouté, OneDev recherchera le projet en utilisant les informations de désignation du projet ci-dessous");
		m.put("System settings have been saved", "Les paramètres système ont été enregistrés");
		m.put("System uuid", "UUID système");
		m.put("TIMED_OUT", "TIMED_OUT");
		m.put("TRX Report (.net unit test)", "Rapport TRX (test unitaire .net)");
		m.put("Tab Width", "Largeur de tabulation");
		m.put("Tag", "Étiquette");
		m.put("Tag \"{0}\" already exists, please choose a different name", "L'étiquette \"{0}\" existe déjà, veuillez choisir un autre nom");
		m.put("Tag \"{0}\" already exists, please choose a different name.", "L'étiquette \"{0}\" existe déjà, veuillez choisir un autre nom.");
		m.put("Tag \"{0}\" created", "L'étiquette \"{0}\" a été créée");
		m.put("Tag \"{0}\" deleted", "L'étiquette \"{0}\" a été supprimée");
		m.put("Tag Message", "Message de l'étiquette");
		m.put("Tag Name", "Nom de l'étiquette");
		m.put("Tag Protection", "Protection des étiquettes");
		m.put("Tag creation", "Création d'étiquette");
		m.put("Tags", "Étiquettes");
		m.put("Target", "Cible");
		m.put("Target Branches", "Branches cibles");
		m.put("Target Docker Image", "Image Docker cible");
		m.put("Target File", "Fichier cible");
		m.put("Target Path", "Chemin cible");
		m.put("Target Project", "Projet cible");
		m.put("Target branch no longer exists", "La branche cible n'existe plus");
		m.put("Target branch was fast-forwarded to source branch", "La branche cible a été avancée rapidement vers la branche source");
		m.put("Target branch will be fast-forwarded to source branch", "La branche cible sera avancée rapidement vers la branche source");
		m.put("Target containing spaces or starting with dash needs to be quoted", "Une cible contenant des espaces ou commençant par un tiret doit être entre guillemets");
		m.put("Target or source branch is updated. Please try again", "La branche cible ou source est mise à jour. Veuillez réessayer");
		m.put("Task List", "Liste des tâches");
		m.put("Task list", "Liste des tâches");
		m.put("Tell user to reset password", "Demander à l'utilisateur de réinitialiser le mot de passe");
		m.put("Template Name", "Nom du modèle");
		m.put("Template saved", "Modèle enregistré");
		m.put("Terminal close", "Fermeture du terminal");
		m.put("Terminal input", "Entrée du terminal");
		m.put("Terminal open", "Ouverture du terminal");
		m.put("Terminal output", "Sortie du terminal");
		m.put("Terminal ready", "Terminal prêt");
		m.put("Terminal resize", "Redimensionnement du terminal");
		m.put("Test", "Test");
		m.put("Test Case", "Cas de test");
		m.put("Test Cases", "Cas de test");
		m.put("Test Settings", "Paramètres de test");
		m.put("Test Suite", "Suite de tests");
		m.put("Test Suites", "Suites de tests");
		m.put("Test importing from {0}", "Importation de tests depuis {0}");
		m.put("Test mail has been sent to {0}, please check your mail box", "Un email de test a été envoyé à {0}, veuillez vérifier votre boîte mail");
		m.put("Test successful: authentication passed", "Test réussi : authentification validée");
		m.put("Test successful: authentication passed with below information retrieved:", "Test réussi : authentification validée avec les informations suivantes récupérées :");
		m.put("Text", "Texte");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "L'URL du point de terminaison du serveur qui recevra les requêtes POST du webhook");
		m.put("The change contains disallowed file type(s): {0}", "Le changement contient des types de fichiers non autorisés : {0}");
		m.put("The first board will be the default board", "Le premier tableau sera le tableau par défaut");
		m.put("The first timesheet will be the default timesheet", "La première feuille de temps sera la feuille de temps par défaut");
		m.put("The object you are deleting/disabling is still being used", "L'objet que vous supprimez/désactivez est toujours utilisé");
		m.put("The password reset url is invalid or obsolete", "L'URL de réinitialisation du mot de passe est invalide ou obsolète");
		m.put("The permission to access build log", "La permission d'accéder au journal de construction");
		m.put("The permission to access build pipeline", "La permission d'accéder au pipeline de construction");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"La permission d'exécuter un job manuellement. Cela implique également la permission d'accéder au journal de construction, au pipeline de construction et à tous les rapports publiés");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"Le secret qui vous permet de garantir que les requêtes POST envoyées à l'URL de charge utile proviennent de OneDev. Lorsque vous définissez un secret, vous recevrez l'en-tête X-OneDev-Signature dans la requête POST du webhook");
		m.put("The service desk feature enables user to create issues by sending emails to OneDev. Issues can be discussed over email completely, without the need of logging to OneDev.", 
			"La fonctionnalité de service desk permet à l'utilisateur de créer des issues en envoyant des emails à OneDev. Les issues peuvent être discutées entièrement par email, sans avoir besoin de se connecter à OneDev.");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "Ensuite, entrez le code affiché dans l'authentificateur TOTP pour vérifier");
		m.put("Then publish package from project directory like below", "Ensuite, publiez le package depuis le répertoire du projet comme ci-dessous");
		m.put("Then push gem to the source", "Ensuite, poussez le gem vers la source");
		m.put("Then push image to desired repository under specified project", "Ensuite, poussez l'image vers le dépôt souhaité sous le projet spécifié");
		m.put("Then push package to the source", "Ensuite, poussez le package vers la source");
		m.put("Then resolve dependency via command step", "Ensuite, résolvez la dépendance via l'étape de commande");
		m.put("Then upload package to the repository with twine", "Ensuite, téléchargez le package vers le dépôt avec twine");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"Il y a <a wicket:id=\"openRequests\">des pull requests ouvertes</a> contre la branche <span wicket:id=\"branch\"></span>. Ces pull requests seront abandonnées si la branche est supprimée.");
		m.put("There are incompatibilities since your upgraded version", "Il y a des incompatibilités depuis votre version mise à jour");
		m.put("There are merge conflicts", "Il y a des conflits de fusion");
		m.put("There are merge conflicts.", "Il y a des conflits de fusion.");
		m.put("There are merge conflicts. You can still create the pull request though", "Il y a des conflits de fusion. Vous pouvez tout de même créer la pull request");
		m.put("There are unsaved changes, discard and continue?", "Il y a des modifications non enregistrées, les abandonner et continuer ?");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"Ces authentificateurs fonctionnent généralement sur votre téléphone mobile, quelques exemples sont Google Authenticator, Microsoft Authenticator, Authy, 1Password, etc.");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"Ce <span wicket:id=\"elementTypeName\"></span> est importé depuis <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>");
		m.put("This Month", "Ce mois-ci");
		m.put("This Week", "Cette semaine");
		m.put("This account is disabled", "Ce compte est désactivé");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"Cette adresse doit être un <code>expéditeur vérifié</code> dans SendGrid et sera utilisée comme adresse d'expéditeur pour diverses notifications par email. On peut également répondre à cette adresse pour publier des commentaires sur des issues ou des pull requests si l'option <code>Recevoir les emails postés</code> est activée ci-dessous");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"Cette adresse sera utilisée comme adresse d'expéditeur pour diverses notifications par email. L'utilisateur peut également répondre à cette adresse pour publier des commentaires sur des issues ou des pull requests via email si l'option <code>Vérifier les emails entrants</code> est activée ci-dessous");
		m.put("This change is already opened for merge by pull request {0}", "Ce changement est déjà ouvert pour fusion par la pull request {0}");
		m.put("This change is squashed/rebased onto base branch via a pull request", "Ce changement est compressé/rebasé sur la branche de base via une pull request");
		m.put("This change is squashed/rebased onto base branch via pull request {0}", "Ce changement est compressé/rebasé sur la branche de base via la pull request {0}");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "Ce changement doit être vérifié par certains jobs. Soumettez une pull request à la place");
		m.put("This commit is rebased", "Ce commit est rebasé");
		m.put("This date is using <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 format</a>", 
			"Cette date utilise le <a href=\"https://www.w3.org/TR/NOTE-datetime\">format ISO 8601</a>");
		m.put("This email address is being used", "Cette adresse e-mail est déjà utilisée");
		m.put("This executor runs build jobs as docker containers on OneDev server", "Cet exécuteur exécute des tâches de construction en tant que conteneurs Docker sur le serveur OneDev");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"Cet exécuteur exécute des tâches de construction en tant que conteneurs Docker sur des machines distantes via <a href='/~administration/agents' target='_blank'>agents</a>");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"Cet exécuteur exécute des tâches de construction en tant que pods dans un cluster Kubernetes. Aucun agent n'est requis.<b class='text-danger'>Remarque :</b> Assurez-vous que l'URL du serveur est correctement spécifiée dans les paramètres système, car les pods de tâches doivent y accéder pour télécharger les sources et les artefacts");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs", 
			"Cet exécuteur exécute des travaux de construction avec la fonctionnalité shell du serveur OneDev.<br><b class='text-danger'>AVERTISSEMENT</b> : Les travaux exécutés avec cet exécuteur ont les mêmes permissions que le processus du serveur OneDev. Assurez-vous qu'il ne peut être utilisé que par des travaux de confiance");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. Make sure it can only be used by trusted jobs", 
			"Cet exécuteur exécute des travaux de construction avec la fonctionnalité shell des machines distantes via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>AVERTISSEMENT</b> : Les travaux exécutés avec cet exécuteur ont les mêmes permissions que le processus de l'agent OneDev. Assurez-vous qu'il ne peut être utilisé que par des travaux de confiance");
		m.put("This field is required", "Ce champ est requis");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"Ce filtre est utilisé pour déterminer l'entrée LDAP de l'utilisateur actuel. Par exemple : <i>(&(uid={0})(objectclass=person))</i>. Dans cet exemple, <i>{0}</i> représente le nom de connexion de l'utilisateur actuel.");
		m.put("This installation does not have an active subscription and runs as community edition. To access <a href=\"https://onedev.io/pricing\">enterprise features</a>, an active subscription is required", 
			"Cette installation n'a pas d'abonnement actif et fonctionne en tant qu'édition communautaire. Pour accéder aux <a href=\"https://onedev.io/pricing\">fonctionnalités d'entreprise</a>, un abonnement actif est requis");
		m.put("This installation has a trial subscription and is now running as enterprise edition", 
			"Cette installation dispose d'un abonnement d'essai et fonctionne actuellement en tant qu'édition d'entreprise");
		m.put("This installation has an active subscription and runs as enterprise edition", "Cette installation dispose d'un abonnement actif et fonctionne en tant qu'édition d'entreprise");
		m.put("This installation has an expired subscription, and runs as community edition", "Cette installation dispose d'un abonnement expiré et fonctionne en tant qu'édition communautaire");
		m.put("This installation has an unlimited users subscription and is now running as enterprise edition", 
			"Cette installation a un abonnement pour utilisateurs illimités et fonctionne maintenant en édition Enterprise");
		m.put("This installation's subscription has expired and is now running as the community edition", 
			"L'abonnement de cette installation a expiré et fonctionne maintenant en édition communautaire");
		m.put("This is a Git LFS object, but the storage file is missing", "Ceci est un objet Git LFS, mais le fichier de stockage est manquant");
		m.put("This is a built-in role and can not be deleted", "Ceci est un rôle intégré et ne peut pas être supprimé");
		m.put("This is a disabled service account", "Ceci est un compte de service désactivé");
		m.put("This is a layer cache. To use the cache, add below option to your docker buildx command", 
			"Ceci est un cache de couche. Pour utiliser le cache, ajoutez l'option ci-dessous à votre commande docker buildx");
		m.put("This is a service account for task automation purpose", "Ceci est un compte de service destiné à l'automatisation des tâches");
		m.put("This is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Ceci est une fonctionnalité Enterprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("This key has already been used by another project", "Cette clé a déjà été utilisée par un autre projet");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"Cette clé est associée à {0}, mais ce n'est PAS une adresse e-mail vérifiée de cet utilisateur");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here.<br><b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated from these lock files with the <b>generate checksum step</b> defined before this step", 
			"Cette clé est utilisée pour déterminer s'il y a un cache trouvé dans la hiérarchie du projet (recherche du projet actuel au projet racine dans l'ordre, même pour les clés de chargement ci-dessous). Un cache est considéré trouvé si sa clé est exactement la même que la clé définie ici.<br><b>REMARQUE :</b> Dans le cas où votre projet a des fichiers de verrouillage (package.json, pom.xml, etc.) capables de représenter l'état du cache, cette clé doit être définie comme &lt;nom du cache&gt;-@file:checksum.txt@, où checksum.txt est généré à partir de ces fichiers de verrouillage avec l'étape <b>générer le checksum</b> définie avant cette étape");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"Cette clé est utilisée pour télécharger et téléverser le cache dans la hiérarchie du projet (recherche du projet actuel au projet racine dans l'ordre)");
		m.put("This key or one of its sub key is already added", "Cette clé ou l'une de ses sous-clés est déjà ajoutée");
		m.put("This key or one of its subkey is already in use", "Cette clé ou l'une de ses sous-clés est déjà utilisée");
		m.put("This line has confusable unicode character modification", "Cette ligne contient une modification de caractère Unicode confus");
		m.put("This might happen when project points to a wrong git repository, or the commit is garbage collected.", 
			"Cela peut arriver lorsque le projet pointe vers un mauvais dépôt git ou que le commit est collecté comme déchet.");
		m.put("This might happen when project points to a wrong git repository, or these commits are garbage collected.", 
			"Cela peut arriver lorsque le projet pointe vers un mauvais dépôt git ou que ces commits sont collectés comme déchets.");
		m.put("This name has already been used by another board", "Ce nom a déjà été utilisé par un autre tableau");
		m.put("This name has already been used by another group", "Ce nom a déjà été utilisé par un autre groupe");
		m.put("This name has already been used by another issue board in the project", "Ce nom a déjà été utilisé par un autre tableau de problèmes dans le projet");
		m.put("This name has already been used by another job executor", "Ce nom a déjà été utilisé par un autre exécuteur de tâches");
		m.put("This name has already been used by another project", "Ce nom a déjà été utilisé par un autre projet");
		m.put("This name has already been used by another provider", "Ce nom a déjà été utilisé par un autre fournisseur");
		m.put("This name has already been used by another role", "Ce nom a déjà été utilisé par un autre rôle");
		m.put("This name has already been used by another role.", "Ce nom a déjà été utilisé par un autre rôle.");
		m.put("This name has already been used by another script", "Ce nom a déjà été utilisé par un autre script");
		m.put("This name has already been used by another state", "Ce nom a déjà été utilisé par un autre état");
		m.put("This operation is disallowed by branch protection rule", "Cette opération est interdite par la règle de protection de branche");
		m.put("This page lists changes since previous build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a>", 
			"Cette page liste les changements depuis la construction précédente sur <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">le même flux</a>");
		m.put("This page lists recent commits fixing the issue", "Cette page liste les commits récents corrigeant le problème");
		m.put("This permission enables one to access confidential issues", "Cette permission permet d'accéder aux problèmes confidentiels");
		m.put("This permission enables one to schedule issues into iterations", "Cette permission permet de planifier des problèmes dans des itérations");
		m.put("This property is imported from {0}", "Cette propriété est importée de {0}");
		m.put("This pull request has been discarded", "Cette demande de tirage a été abandonnée");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"Ce rapport sera affiché sur la page d'aperçu des demandes de tirage si la construction est déclenchée par une demande de tirage");
		m.put("This server is currently accessed via http protocol, please configure your docker daemon or buildx builder to <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">work with insecure registry</a>", 
			"Ce serveur est actuellement accessible via le protocole http, veuillez configurer votre démon Docker ou votre builder buildx pour <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">travailler avec un registre non sécurisé</a>");
		m.put("This shows average duration of different states over time", "Ceci montre la durée moyenne des différents états au fil du temps");
		m.put("This shows average duration of merged pull requests over time", "Ceci montre la durée moyenne des demandes de tirage fusionnées au fil du temps");
		m.put("This shows number of <b>new</b> issues in different states over time", "Ceci montre le nombre de <b>nouveaux</b> problèmes dans différents états au fil du temps");
		m.put("This shows number of issues in various states over time", "Ceci montre le nombre de problèmes dans divers états au fil du temps");
		m.put("This shows number of open and merged pull requests over time", "Ceci montre le nombre de demandes de tirage ouvertes et fusionnées au fil du temps");
		m.put("This step can only be executed by a docker aware executor", "Cette étape ne peut être exécutée que par un exécuteur compatible avec Docker");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Cette étape ne peut être exécutée que par un exécuteur compatible avec Docker. Elle s'exécute sous <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>l'espace de travail de la tâche</a>");
		m.put("This step copies files from job workspace to build artifacts directory, so that they can be accessed after job is completed", 
			"Cette étape copie les fichiers de l'espace de travail du job vers le répertoire des artefacts de construction, afin qu'ils puissent être accessibles après la fin du job");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"Cette étape publie les fichiers spécifiés pour être servis comme site web du projet. Le site web du projet peut être accessible publiquement via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>");
		m.put("This step pulls specified refs from remote", "Cette étape extrait les références spécifiées depuis le distant");
		m.put("This step pushes current commit to same ref on remote", "Cette étape pousse le commit actuel vers la même référence sur le distant");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"Cette étape configure le cache Renovate. Placez-la avant l'étape Renovate si vous souhaitez l'utiliser");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"Cette étape configure le cache de la base de données trivy pour accélérer diverses étapes de scanner. Placez-la avant les étapes de scanner si vous souhaitez l'utiliser");
		m.put("This subscription key was already used", "Cette clé d'abonnement a déjà été utilisée");
		m.put("This subscription key was expired", "Cette clé d'abonnement a expiré");
		m.put("This tab shows the pipeline containing current build. Check <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">this article</a> to understand how build pipeline works", 
			"Cet onglet montre le pipeline contenant la construction actuelle. Consultez <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">cet article</a> pour comprendre comment fonctionne le pipeline de construction");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Ce déclencheur ne sera applicable que si le commit tagué est accessible depuis les branches spécifiées ici. Plusieurs branches doivent être séparées par des espaces. Utilisez '**', '*' ou '?' pour <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>la correspondance de joker de chemin</a>. Préfixez avec '-' pour exclure. Laissez vide pour correspondre à toutes les branches");
		m.put("This user is authenticating via external system.", "Cet utilisateur s'authentifie via un système externe.");
		m.put("This user is authenticating via internal database.", "Cet utilisateur s'authentifie via la base de données interne.");
		m.put("This user is currently authenticating via external system. Setting password will switch to use internal database", 
			"Cet utilisateur s'authentifie actuellement via un système externe. Définir un mot de passe basculera vers l'utilisation de la base de données interne");
		m.put("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?", 
			"Cela désactivera l'abonnement actuel et toutes les fonctionnalités d'entreprise seront désactivées, voulez-vous continuer ?");
		m.put("This will discard all project specific boards, do you want to continue?", "Cela supprimera tous les tableaux spécifiques au projet, voulez-vous continuer ?");
		m.put("This will restart SSH server. Do you want to continue?", "Cela redémarrera le serveur SSH. Voulez-vous continuer ?");
		m.put("Threads", "Threads");
		m.put("Time Estimate Issue Field", "Champ de problème d'estimation de temps");
		m.put("Time Range", "Plage de temps");
		m.put("Time Spent Issue Field", "Champ de problème de temps passé");
		m.put("Time Tracking", "Suivi du temps");
		m.put("Time Tracking Setting", "Paramètre de suivi du temps");
		m.put("Time Tracking Settings", "Paramètres de suivi du temps");
		m.put("Time tracking settings have been saved", "Les paramètres de suivi du temps ont été enregistrés");
		m.put("Timed out", "Temps écoulé");
		m.put("Timeout", "Délai d'attente");
		m.put("Timesheet", "Feuille de temps");
		m.put("Timesheet Setting", "Paramètre de feuille de temps");
		m.put("Timesheets", "Feuilles de temps");
		m.put("Timing", "Chronométrage");
		m.put("Title", "Titre");
		m.put("To Everyone", "À tout le monde");
		m.put("To State", "À l'état");
		m.put("To States", "Aux états");
		m.put("To authenticate via internal database, <a wicket:id=\"setPasswordForUser\">set password for user</a> or <a wicket:id=\"tellUserToResetPassword\">tell user to reset password</a>", 
			"Pour s'authentifier via la base de données interne, <a wicket:id=\"setPasswordForUser\">définissez un mot de passe pour l'utilisateur</a> ou <a wicket:id=\"tellUserToResetPassword\">demandez à l'utilisateur de réinitialiser son mot de passe</a>");
		m.put("To avoid duplication, estimated/remaining time showing here does not include those aggregated from \"{0}\"", 
			"Pour éviter les doublons, le temps estimé/restant affiché ici n'inclut pas ceux agrégés depuis \"{0}\"");
		m.put("To avoid duplication, spent time showing here does not include those aggregated from \"{0}\"", 
			"Pour éviter les doublons, le temps passé affiché ici n'inclut pas ceux agrégés depuis \"{0}\"");
		m.put("Toggle change history", "Basculer l'historique des modifications");
		m.put("Toggle comments", "Basculer les commentaires");
		m.put("Toggle commits", "Basculer les commits");
		m.put("Toggle dark mode", "Basculer le mode sombre");
		m.put("Toggle detail message", "Basculer le message détaillé");
		m.put("Toggle fixed width font", "Basculer la police à largeur fixe");
		m.put("Toggle full screen", "Basculer en plein écran");
		m.put("Toggle matched contents", "Basculer les contenus correspondants");
		m.put("Toggle navigation", "Basculer la navigation");
		m.put("Toggle work log", "Basculer le journal de travail");
		m.put("Tokens", "Tokens");
		m.put("Too many commits to load", "Trop de commits à charger");
		m.put("Too many commits, displaying recent {0}", "Trop de commits, affichage des {0} récents");
		m.put("Too many log entries, displaying recent {0}", "Trop d'entrées de journal, affichage des {0} récents");
		m.put("Too many problems, displaying first {0}", "Trop de problèmes, affichage des {0} premiers");
		m.put("Toomanyrequests", "Tropdemandes");
		m.put("Top", "Haut");
		m.put("Topo", "Topo");
		m.put("Total Heap Memory", "Mémoire totale du tas");
		m.put("Total Number", "Nombre total");
		m.put("Total Problems", "Problèmes totaux");
		m.put("Total Size", "Taille totale");
		m.put("Total Test Duration", "Durée totale des tests");
		m.put("Total estimated time", "Temps estimé total");
		m.put("Total spent time", "Temps total passé");
		m.put("Total spent time / total estimated time", "Temps total passé / temps estimé total");
		m.put("Total time", "Temps total");
		m.put("Total:", "Total :");
		m.put("Touched File", "Fichier touché");
		m.put("Touched Files", "Fichiers touchés");
		m.put("Transfer LFS Files", "Transférer les fichiers LFS");
		m.put("Transit manually", "Transit manuel");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "État de l'incident \"{0}\" transité vers \"{1}\" ({2})");
		m.put("Transition Edit Bean", "Modifier le bean de transition");
		m.put("Transition Spec", "Spécification de transition");
		m.put("Trial Expiration Date", "Date d'expiration de l'essai");
		m.put("Trial subscription key not applicable for this installation", "Clé d'abonnement d'essai non applicable pour cette installation");
		m.put("Triggers", "Déclencheurs");
		m.put("Trivy Container Image Scanner", "Scanner d'image de conteneur Trivy");
		m.put("Trivy Filesystem Scanner", "Scanner de système de fichiers Trivy");
		m.put("Trivy Rootfs Scanner", "Scanner Rootfs Trivy");
		m.put("Try EE", "Essayer EE");
		m.put("Try Enterprise Edition", "Essayer l'édition entreprise");
		m.put("Twitch", "Twitch");
		m.put("Two Factor Authentication", "Authentification à deux facteurs");
		m.put("Two-factor Authentication", "Authentification à deux facteurs");
		m.put("Two-factor authentication already set up. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Request to set up again", 
			"Authentification à deux facteurs déjà configurée. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Demander à configurer à nouveau");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"L'authentification à deux facteurs est activée. Veuillez entrer le code affiché sur votre authentificateur TOTP. En cas de problème, assurez-vous que l'heure du serveur OneDev et de votre appareil exécutant l'authentificateur TOTP est synchronisée");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"L'authentification à deux facteurs est imposée pour votre compte afin d'améliorer la sécurité. Veuillez suivre la procédure ci-dessous pour la configurer");
		m.put("Two-factor authentication is now configured", "L'authentification à deux facteurs est maintenant configurée");
		m.put("Two-factor authentication not enabled", "Authentification à deux facteurs non activée");
		m.put("Type", "Type");
		m.put("Type <code>yes</code> below to cancel all queried builds", "Tapez <code>yes</code> ci-dessous pour annuler toutes les constructions interrogées");
		m.put("Type <code>yes</code> below to cancel selected builds", "Tapez <code>yes</code> ci-dessous pour annuler les constructions sélectionnées");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "Tapez <code>yes</code> ci-dessous pour confirmer la suppression de tous les utilisateurs interrogés");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "Tapez <code>yes</code> ci-dessous pour confirmer la suppression des utilisateurs sélectionnés");
		m.put("Type <code>yes</code> below to copy all queried issues to project \"{0}\"", "Tapez <code>yes</code> ci-dessous pour copier toutes les incidents interrogés vers le projet \"{0}\"");
		m.put("Type <code>yes</code> below to copy selected issues to project \"{0}\"", "Tapez <code>yes</code> ci-dessous pour copier les incidents sélectionnés vers le projet \"{0}\"");
		m.put("Type <code>yes</code> below to delete all queried builds", "Tapez <code>yes</code> ci-dessous pour supprimer toutes les constructions interrogées");
		m.put("Type <code>yes</code> below to delete all queried comments", "Tapez <code>yes</code> ci-dessous pour supprimer tous les commentaires interrogés");
		m.put("Type <code>yes</code> below to delete all queried issues", "Tapez <code>yes</code> ci-dessous pour supprimer tous les incidents interrogés");
		m.put("Type <code>yes</code> below to delete all queried packages", "Tapez <code>yes</code> ci-dessous pour supprimer tous les paquets interrogés");
		m.put("Type <code>yes</code> below to delete all queried projects", "Tapez <code>yes</code> ci-dessous pour supprimer tous les projets interrogés");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "Tapez <code>yes</code> ci-dessous pour supprimer toutes les demandes de tirage interrogées");
		m.put("Type <code>yes</code> below to delete selected builds", "Tapez <code>yes</code> ci-dessous pour supprimer les constructions sélectionnées");
		m.put("Type <code>yes</code> below to delete selected comments", "Tapez <code>yes</code> ci-dessous pour supprimer les commentaires sélectionnés");
		m.put("Type <code>yes</code> below to delete selected issues", "Tapez <code>yes</code> ci-dessous pour supprimer les incidents sélectionnés");
		m.put("Type <code>yes</code> below to delete selected packages", "Tapez <code>yes</code> ci-dessous pour supprimer les paquets sélectionnés");
		m.put("Type <code>yes</code> below to delete selected projects", "Tapez <code>yes</code> ci-dessous pour supprimer les projets sélectionnés");
		m.put("Type <code>yes</code> below to delete selected pull requests", "Tapez <code>yes</code> ci-dessous pour supprimer les demandes de tirage sélectionnées");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "Tapez <code>yes</code> ci-dessous pour abandonner toutes les demandes de tirage interrogées");
		m.put("Type <code>yes</code> below to discard selected pull requests", "Tapez <code>yes</code> ci-dessous pour abandonner les demandes de tirage sélectionnées");
		m.put("Type <code>yes</code> below to move all queried issues to project \"{0}\"", "Tapez <code>yes</code> ci-dessous pour déplacer toutes les incidents interrogés vers le projet \"{0}\"");
		m.put("Type <code>yes</code> below to move all queried projects to be under \"{0}\"", "Tapez <code>yes</code> ci-dessous pour déplacer tous les projets interrogés sous \"{0}\"");
		m.put("Type <code>yes</code> below to move selected issues to project \"{0}\"", "Tapez <code>yes</code> ci-dessous pour déplacer les incidents sélectionnés vers le projet \"{0}\"");
		m.put("Type <code>yes</code> below to move selected projects to be under \"{0}\"", "Tapez <code>yes</code> ci-dessous pour déplacer les projets sélectionnés sous \"{0}\"");
		m.put("Type <code>yes</code> below to pause all queried agents", "Tapez <code>yes</code> ci-dessous pour mettre en pause tous les agents interrogés");
		m.put("Type <code>yes</code> below to re-run all queried builds", "Tapez <code>yes</code> ci-dessous pour relancer toutes les constructions interrogées");
		m.put("Type <code>yes</code> below to re-run selected builds", "Tapez <code>yes</code> ci-dessous pour relancer les constructions sélectionnées");
		m.put("Type <code>yes</code> below to remove all queried users from group", "Tapez <code>yes</code> ci-dessous pour retirer tous les utilisateurs interrogés du groupe");
		m.put("Type <code>yes</code> below to remove from all queried groups", "Tapez <code>yes</code> ci-dessous pour retirer de tous les groupes interrogés");
		m.put("Type <code>yes</code> below to remove from selected groups", "Tapez <code>yes</code> ci-dessous pour retirer des groupes sélectionnés");
		m.put("Type <code>yes</code> below to remove selected users from group", "Tapez <code>yes</code> ci-dessous pour retirer les utilisateurs sélectionnés du groupe");
		m.put("Type <code>yes</code> below to restart all queried agents", "Tapez <code>yes</code> ci-dessous pour redémarrer tous les agents interrogés");
		m.put("Type <code>yes</code> below to restart selected agents", "Tapez <code>yes</code> ci-dessous pour redémarrer les agents sélectionnés");
		m.put("Type <code>yes</code> below to resume all queried agents", "Tapez <code>yes</code> ci-dessous pour reprendre tous les agents interrogés");
		m.put("Type <code>yes</code> below to set all queried as root projects", "Tapez <code>yes</code> ci-dessous pour définir tous les interrogés comme projets racines");
		m.put("Type <code>yes</code> below to set selected as root projects", "Tapez <code>yes</code> ci-dessous pour définir les sélectionnés comme projets racines");
		m.put("Type password here", "Tapez le mot de passe ici");
		m.put("Type to filter", "Tapez pour filtrer");
		m.put("Type to filter...", "Tapez pour filtrer...");
		m.put("URL", "URL");
		m.put("Unable To Delete/Disable Right Now", "Impossible de supprimer/désactiver pour le moment");
		m.put("Unable to apply change as otherwise you will not be able to manage this project", "Impossible d'appliquer le changement car vous ne pourrez pas gérer ce projet autrement");
		m.put("Unable to change password as you are authenticating via external system", "Impossible de changer le mot de passe car vous vous authentifiez via un système externe");
		m.put("Unable to comment", "Impossible de commenter");
		m.put("Unable to connect to server", "Impossible de se connecter au serveur");
		m.put("Unable to create protected branch", "Impossible de créer une branche protégée");
		m.put("Unable to create protected tag", "Impossible de créer une balise protégée");
		m.put("Unable to diff as some line is too long.", "Impossible de différencier car une ligne est trop longue.");
		m.put("Unable to diff as the file is too large.", "Impossible de différencier car le fichier est trop volumineux.");
		m.put("Unable to find SSO provider: ", "Impossible de trouver le fournisseur SSO :");
		m.put("Unable to find agent {0}", "Impossible de trouver l'agent {0}");
		m.put("Unable to find build #{0} in project {1}", "Impossible de trouver la construction #{0} dans le projet {1}");
		m.put("Unable to find commit to import build spec (import project: {0}, import revision: {1})", 
			"Impossible de trouver le commit pour importer la spécification de construction (projet d'importation : {0}, révision d'importation : {1})");
		m.put("Unable to find issue #{0} in project {1}", "Impossible de trouver l'incident #{0} dans le projet {1}");
		m.put("Unable to find project to import build spec: {0}", "Impossible de trouver le projet pour importer la spécification de construction : {0}");
		m.put("Unable to find pull request #{0} in project {1}", "Impossible de trouver la demande de tirage #{0} dans le projet {1}");
		m.put("Unable to find timesheet: ", "Impossible de trouver la feuille de temps :");
		m.put("Unable to get guilds info", "Impossible d'obtenir les informations des guildes");
		m.put("Unable to import build spec (import project: {0}, import revision: {1}): {2}", "Impossible d'importer la spécification de construction (projet d'importation : {0}, révision d'importation : {1}) : {2}");
		m.put("Unable to notify user as mail service is not configured", "Impossible de notifier l'utilisateur car le service de messagerie n'est pas configuré");
		m.put("Unable to send password reset email as mail service is not configured", "Impossible d'envoyer l'e-mail de réinitialisation du mot de passe car le service de messagerie n'est pas configuré");
		m.put("Unable to send verification email as mail service is not configured yet", "Impossible d'envoyer l'email de vérification car le service de messagerie n'est pas encore configuré");
		m.put("Unauthorize this user", "Déautoriser cet utilisateur");
		m.put("Unauthorized", "Non autorisé");
		m.put("Undefined", "Indéfini");
		m.put("Undefined Field Resolution", "Résolution de champ indéfini");
		m.put("Undefined Field Value Resolution", "Résolution de valeur de champ indéfini");
		m.put("Undefined State Resolution", "Résolution d'état indéfini");
		m.put("Undefined custom field: ", "Champ personnalisé indéfini :");
		m.put("Under which condition this step should run. <b>SUCCESSFUL</b> means all non-optional steps running before this step are successful", 
			"Sous quelle condition cette étape doit s'exécuter. <b>RÉUSSI</b> signifie que toutes les étapes non optionnelles exécutées avant cette étape sont réussies");
		m.put("Unexpected setting: {0}", "Paramètre inattendu : {0}");
		m.put("Unexpected ssh signature hash algorithm: ", "Algorithme de hachage de signature ssh inattendu :");
		m.put("Unexpected ssh signature namespace: ", "Espace de noms de signature ssh inattendu :");
		m.put("Unified", "Unifié");
		m.put("Unified view", "Vue unifiée");
		m.put("Unit Test Statistics", "Statistiques des tests unitaires");
		m.put("Unlimited", "Illimité");
		m.put("Unlink this issue", "Dissocier ce problème");
		m.put("Unordered List", "Liste non ordonnée");
		m.put("Unordered list", "Liste non ordonnée");
		m.put("Unpin this issue", "Détacher ce problème");
		m.put("Unresolved", "Non résolu");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "Commentaire non résolu sur le fichier \"{0}\" dans le projet \"{1}\"");
		m.put("Unscheduled", "Non planifié");
		m.put("Unscheduled Issues", "Problèmes non planifiés");
		m.put("Unsolicited OIDC authentication response", "Réponse d'authentification OIDC non sollicitée");
		m.put("Unsolicited OIDC response", "Réponse OIDC non sollicitée");
		m.put("Unsolicited discord api response", "Réponse API Discord non sollicitée");
		m.put("Unspecified", "Non spécifié");
		m.put("Unsupported", "Non pris en charge");
		m.put("Unsupported ssh signature algorithm: ", "Algorithme de signature ssh non pris en charge :");
		m.put("Unsupported ssh signature version: ", "Version de signature ssh non prise en charge :");
		m.put("Unverified", "Non vérifié");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "L'adresse e-mail non vérifiée <b>NE</b> s'applique pas aux fonctionnalités ci-dessus");
		m.put("Unvote", "Retirer le vote");
		m.put("Unwatched. Click to watch", "Non surveillé. Cliquez pour surveiller");
		m.put("Update", "Mettre à jour");
		m.put("Update Dependencies via Renovate", "Mettre à jour les dépendances via Renovate");
		m.put("Update Source Branch", "Mettre à jour la branche source");
		m.put("Update body", "Mettre à jour le corps");
		m.put("Upload", "Téléverser");
		m.put("Upload Access Token Secret", "Téléverser le secret du jeton d'accès");
		m.put("Upload Cache", "Téléverser le cache");
		m.put("Upload Files", "Téléverser des fichiers");
		m.put("Upload Project Path", "Téléverser le chemin du projet");
		m.put("Upload Strategy", "Stratégie de téléversement");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "Téléverser un fichier PNG transparent 128x128 à utiliser comme logo pour le mode sombre");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "Téléverser un fichier PNG transparent 128x128 à utiliser comme logo pour le mode clair");
		m.put("Upload artifacts", "Télécharger les artefacts");
		m.put("Upload avatar", "Téléverser un avatar");
		m.put("Upload should be less than {0} Mb", "Le téléversement doit être inférieur à {0} Mo");
		m.put("Upload to Project", "Téléverser vers le projet");
		m.put("Uploaded Caches", "Caches téléversés");
		m.put("Uploading file", "Téléversement de fichier");
		m.put("Url", "Url");
		m.put("Use '*' for wildcard match", "Utilisez '*' pour une correspondance générique");
		m.put("Use '*' or '?' for wildcard match. Prefix with '-' to exclude", "Utilisez '*' ou '?' pour une correspondance générique. Préfixez avec '-' pour exclure");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Utilisez '**', '*' ou '?' pour une <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondance générique de chemin</a>");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Utilisez '**', '*' ou '?' pour une <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondance générique de chemin</a>. Préfixez avec '-' pour exclure");
		m.put("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Utilisez '**', '*', ou '?' pour une <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondance générique de chemin</a>");
		m.put("Use '\\' to escape brackets", "Utilisez '\\' pour échapper les crochets");
		m.put("Use '\\' to escape quotes", "Utilisez '\\' pour échapper les guillemets");
		m.put("Use @@ to reference scope in job commands to avoid being interpreted as variable", "Utilisez @@ pour référencer la portée dans les commandes de travail afin d'éviter qu'elle soit interprétée comme une variable");
		m.put("Use Avatar Service", "Utiliser le service d'avatar");
		m.put("Use Default", "Utiliser par défaut");
		m.put("Use Default Boards", "Utiliser les tableaux par défaut");
		m.put("Use For Git Operations", "Utiliser pour les opérations Git");
		m.put("Use Git in System Path", "Utiliser Git dans le chemin système");
		m.put("Use Hours And Minutes Only", "Utiliser uniquement les heures et les minutes");
		m.put("Use Specified Git", "Utiliser Git spécifié");
		m.put("Use Specified curl", "Utiliser curl spécifié");
		m.put("Use Step Template", "Utiliser le modèle d'étape");
		m.put("Use curl in System Path", "Utiliser curl dans le chemin système");
		m.put("Use default", "Utiliser par défaut");
		m.put("Use default storage class", "Utiliser la classe de stockage par défaut");
		m.put("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages", 
			"Utiliser le jeton de travail comme nom d'utilisateur afin que OneDev puisse savoir quel build est ${permission.equals(\"write\")? \"en train de déployer\": \"en train d'utiliser\"} des packages");
		m.put("Use job token to tell OneDev the build publishing the package", "Utiliser le jeton de travail pour indiquer à OneDev le build publiant le package");
		m.put("Use job token to tell OneDev the build pushing the chart", "Utiliser le jeton de travail pour indiquer à OneDev le build poussant le chart");
		m.put("Use job token to tell OneDev the build pushing the package", "Utiliser le jeton de travail pour indiquer à OneDev le build poussant le package");
		m.put("Use job token to tell OneDev the build using the package", "Utiliser le jeton de travail pour indiquer à OneDev le build utilisant le package");
		m.put("Use project dependency to retrieve artifacts from other projects", "Utiliser la dépendance de projet pour récupérer des artefacts d'autres projets");
		m.put("Use specified choices", "Utiliser les choix spécifiés");
		m.put("Use specified default value", "Utiliser la valeur par défaut spécifiée");
		m.put("Use specified value or job secret", "Utiliser la valeur spécifiée ou le secret de travail");
		m.put("Use specified values or job secrets", "Utiliser les valeurs spécifiées ou les secrets de travail");
		m.put("Use triggers to run the job automatically under certain conditions", "Utiliser des déclencheurs pour exécuter le travail automatiquement sous certaines conditions");
		m.put("Use value of specified parameter/secret", "Utiliser la valeur du paramètre/secret spécifié");
		m.put("Used Heap Memory", "Mémoire heap utilisée");
		m.put("User", "Utilisateur");
		m.put("User \"{0}\" unauthorized", "Utilisateur \"{0}\" non autorisé");
		m.put("User Authorization Bean", "Bean d'autorisation utilisateur");
		m.put("User Authorizations", "Autorisations utilisateur");
		m.put("User Authorizations Bean", "Bean des autorisations utilisateur");
		m.put("User Count", "Nombre d'utilisateurs");
		m.put("User Email Attribute", "Attribut d'email utilisateur");
		m.put("User Full Name Attribute", "Attribut du nom complet de l'utilisateur");
		m.put("User Groups Attribute", "Attribut des groupes utilisateur");
		m.put("User Invitation", "Invitation utilisateur");
		m.put("User Invitation Template", "Modèle d'invitation utilisateur");
		m.put("User Management", "Gestion des utilisateurs");
		m.put("User Match Criteria", "Critères de correspondance utilisateur");
		m.put("User Name", "Nom d'utilisateur");
		m.put("User Principal Name", "Nom principal de l'utilisateur");
		m.put("User Profile", "Profil utilisateur");
		m.put("User SSH Key Attribute", "Attribut de clé SSH utilisateur");
		m.put("User Search Bases", "Bases de recherche utilisateur");
		m.put("User Search Filter", "Filtre de recherche utilisateur");
		m.put("User added to group", "Utilisateur ajouté au groupe");
		m.put("User authorizations updated", "Autorisations utilisateur mises à jour");
		m.put("User authorized", "Utilisateur autorisé");
		m.put("User avatar will be requested by appending a hash to this url", "L'avatar utilisateur sera demandé en ajoutant un hash à cette URL");
		m.put("User can sign up if this option is enabled", "L'utilisateur peut s'inscrire si cette option est activée");
		m.put("User disabled", "Utilisateur désactivé");
		m.put("User name", "Nom d'utilisateur");
		m.put("User name already used by another account", "Nom d'utilisateur déjà utilisé par un autre compte");
		m.put("Users", "Utilisateurs");
		m.put("Users converted to service accounts successfully", "Utilisateurs convertis en comptes de service avec succès");
		m.put("Users deleted successfully", "Utilisateurs supprimés avec succès");
		m.put("Users disabled successfully", "Utilisateurs désactivés avec succès");
		m.put("Users enabled successfully", "Utilisateurs activés avec succès");
		m.put("Utilities", "Utilitaires");
		m.put("Valid signature required for head commit of this branch per branch protection rule", 
			"Signature valide requise pour le commit principal de cette branche selon la règle de protection de branche");
		m.put("Value", "Valeur");
		m.put("Value Matcher", "Correspondance de valeur");
		m.put("Value Provider", "Fournisseur de valeur");
		m.put("Values", "Valeurs");
		m.put("Values Provider", "Fournisseur de valeurs");
		m.put("Variable", "Variable");
		m.put("Verification Code", "Code de vérification");
		m.put("Verification email sent, please check it", "Email de vérification envoyé, veuillez le vérifier");
		m.put("Verify", "Vérifier");
		m.put("View", "Voir");
		m.put("View Source", "Voir la source");
		m.put("View source", "Voir la source");
		m.put("View statistics", "Voir les statistiques");
		m.put("Viewer", "Spectateur");
		m.put("Volume Mount", "Montage de volume");
		m.put("Volume Mounts", "Montages de volume");
		m.put("Vote", "Vote");
		m.put("Votes", "Votes");
		m.put("WAITING", "EN ATTENTE");
		m.put("WARNING:", "AVERTISSEMENT :");
		m.put("Waiting", "En attente");
		m.put("Waiting for approvals", "En attente d'approbations");
		m.put("Waiting for test mail to come back...", "En attente du retour du mail de test...");
		m.put("Watch", "Surveiller");
		m.put("Watch Status", "Statut de surveillance");
		m.put("Watch if involved", "Surveiller si impliqué");
		m.put("Watch if involved (default)", "Surveiller si impliqué (par défaut)");
		m.put("Watch status changed", "Statut de surveillance modifié");
		m.put("Watch/Unwatch All Queried Issues", "Surveiller/Ne pas surveiller toutes les issues interrogées");
		m.put("Watch/Unwatch All Queried Pull Requests", "Surveiller/Ne pas surveiller toutes les pull requests interrogées");
		m.put("Watch/Unwatch Selected Pull Requests", "Surveiller/Ne pas surveiller les pull requests sélectionnées");
		m.put("Watched. Click to unwatch", "Surveillé. Cliquez pour ne plus surveiller");
		m.put("Watchers", "Surveillants");
		m.put("Web Hook", "Web Hook");
		m.put("Web Hooks", "Web Hooks");
		m.put("Web Hooks Bean", "Bean de Web Hooks");
		m.put("Web hooks saved", "Web hooks enregistrés");
		m.put("Webhook Url", "URL du Webhook");
		m.put("Week", "Semaine");
		m.put("When", "Quand");
		m.put("When authorize a group, the group will also be authorized with the role for all child projects", 
			"Lorsqu'un groupe est autorisé, le groupe sera également autorisé avec le rôle pour tous les projets enfants");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"Lorsqu'un projet est autorisé, tous les projets enfants seront également autorisés avec les rôles assignés");
		m.put("When authorize a user, the user will also be authorized with the role for all child projects", 
			"Lorsqu'un utilisateur est autorisé, l'utilisateur sera également autorisé avec le rôle pour tous les projets enfants");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"Lors de la détermination si l'utilisateur est auteur/committeur d'un commit git, tous les emails listés ici seront vérifiés");
		m.put("When evaluating this template, below variables will be available:", "Lors de l'évaluation de ce modèle, les variables ci-dessous seront disponibles :");
		m.put("When login via OneDev's built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database", 
			"Lors de la connexion via le formulaire intégré de OneDev, les informations d'identification de l'utilisateur soumises peuvent être vérifiées par rapport à l'authentificateur défini ici, en plus de la base de données interne");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"Lorsque la branche cible d'une pull request a de nouveaux commits, le commit de fusion de la pull request sera recalculé, et cette option indique si les builds de pull request exécutés sur le commit fusionné précédent doivent être acceptés ou non. Si activé, vous devrez relancer les builds requis sur le nouveau commit fusionné. Ce paramètre prend effet uniquement lorsque des builds requis sont spécifiés");
		m.put("When this work starts", "Quand ce travail commence");
		m.put("When {0}", "Quand {0}");
		m.put("Whether or not created issue should be confidential", "Si l'issue créée doit être confidentielle ou non");
		m.put("Whether or not multiple issues can be linked", "Si plusieurs issues peuvent être liées ou non");
		m.put("Whether or not multiple issues can be linked on the other side. For instance sub issues on the other side means parent issue, and multiple should be false on that side if only one parent is allowed", 
			"Si plusieurs issues peuvent être liées de l'autre côté ou non. Par exemple, des sous-issues de l'autre côté signifient une issue parent, et multiple devrait être faux de ce côté si seulement un parent est autorisé");
		m.put("Whether or not multiple values can be specified for this field", "Si plusieurs valeurs peuvent être spécifiées pour ce champ ou non");
		m.put("Whether or not multiple values can be specified for this param", "Si plusieurs valeurs peuvent être spécifiées pour ce paramètre ou non");
		m.put("Whether or not the issue should be confidential", "Si l'issue doit être confidentielle ou non");
		m.put("Whether or not the link is asymmetric. A asymmetric link has different meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric", 
			"Si le lien est asymétrique ou non. Un lien asymétrique a une signification différente selon le côté. Par exemple, un lien 'parent-enfant' est asymétrique, tandis qu'un lien 'lié à' est symétrique");
		m.put("Whether or not this field accepts empty value", "Si ce champ accepte une valeur vide ou non");
		m.put("Whether or not this param accepts empty value", "Si ce paramètre accepte une valeur vide ou non");
		m.put("Whether or not this script can be used in CI/CD jobs", "Si ce script peut être utilisé dans des jobs CI/CD ou non");
		m.put("Whether or not this step is optional. Execution failure of an optional step will not cause the build to fail, and successful condition of subsequent steps will not take optional step into account", 
			"Si cette étape est facultative ou non. L'échec d'exécution d'une étape facultative ne provoquera pas l'échec du build, et la condition de succès des étapes suivantes ne prendra pas en compte l'étape facultative");
		m.put("Whether or not to allow anonymous users to access this server", "Si les utilisateurs anonymes peuvent accéder à ce serveur ou non");
		m.put("Whether or not to allow creating root projects (project without parent)", "Si la création de projets racine (projet sans parent) est autorisée ou non");
		m.put("Whether or not to also include children of above projects", "Si les enfants des projets ci-dessus doivent également être inclus ou non");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same machine", 
			"Si l'image doit toujours être tirée lors de l'exécution du conteneur ou de la construction des images ou non. Cette option doit être activée pour éviter que les images ne soient remplacées par des jobs malveillants exécutés sur la même machine");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same node", 
			"Si l'image doit toujours être tirée lors de l'exécution du conteneur ou de la construction des images ou non. Cette option doit être activée pour éviter que les images ne soient remplacées par des jobs malveillants exécutés sur le même nœud");
		m.put("Whether or not to be able to access time tracking info of issues", "Si les informations de suivi du temps des issues peuvent être accessibles ou non");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities", 
			"Si le compte doit être créé comme un compte de service à des fins d'automatisation des tâches ou non. Le compte de service n'a pas de mot de passe ni d'adresses email, et ne générera pas de notifications pour ses activités");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Si le compte doit être créé comme un compte de service à des fins d'automatisation des tâches ou non. Le compte de service n'a pas de mot de passe ni d'adresses email, et ne générera pas de notifications pour ses activités. <b class='text-warning'>REMARQUE :</b> Le compte de service est une fonctionnalité d'entreprise. <a href='https://onedev.io/pricing' target='_blank'>Essayez gratuitement</a> pendant 30 jours");
		m.put("Whether or not to enable code management for the project", "Si la gestion du code doit être activée pour le projet ou non");
		m.put("Whether or not to enable issue management for the project", "Si la gestion des issues doit être activée pour le projet ou non");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project.", 
			"Si les objets LFS doivent être récupérés si la pull request est ouverte depuis un projet différent ou non");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project. If this option is enabled, git lfs command needs to be installed on OneDev server", 
			"Si les objets LFS doivent être récupérés si la pull request est ouverte depuis un projet différent ou non. Si cette option est activée, la commande git lfs doit être installée sur le serveur OneDev");
		m.put("Whether or not to import forked Bitbucket repositories", "Si les dépôts Bitbucket forkés doivent être importés ou non");
		m.put("Whether or not to import forked GitHub repositories", "Si les dépôts GitHub forkés doivent être importés ou non");
		m.put("Whether or not to import forked GitLab projects", "Si les projets GitLab forkés doivent être importés ou non");
		m.put("Whether or not to import forked Gitea repositories", "Si les dépôts Gitea forkés doivent être importés ou non");
		m.put("Whether or not to include forked repositories", "Si les dépôts forkés doivent être inclus ou non");
		m.put("Whether or not to include this field when issue is initially opened. If not, you may include this field later when issue is transited to other states via issue transition rule", 
			"Si ce champ doit être inclus lors de l'ouverture initiale de l'issue ou non. Sinon, vous pouvez inclure ce champ plus tard lorsque l'issue est transité vers d'autres états via une règle de transition d'issue");
		m.put("Whether or not to input and display estimated/spent time in hours/minutes only", "Si le temps estimé/dépensé doit être saisi et affiché uniquement en heures/minutes ou non");
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. Make sure this executor can only be used by trusted jobs if this option is enabled", 
			"Monter ou non le socket docker dans le conteneur de travail pour prendre en charge les opérations docker dans les commandes de travail<br><b class='text-danger'>AVERTISSEMENT</b> : Des travaux malveillants peuvent prendre le contrôle de l'ensemble de OneDev en opérant sur le socket docker monté. Assurez-vous que cet exécuteur ne peut être utilisé que par des travaux de confiance si cette option est activée");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"Si les mappages de tags doivent être pré-remplis dans la page suivante ou non. Vous pouvez vouloir désactiver cela s'il y a trop de tags à afficher");
		m.put("Whether or not to require this dependency to be successful", "Si cette dépendance doit être réussie ou non");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"Si les groupes de l'utilisateur connecté doivent être récupérés ou non. Assurez-vous d'ajouter la revendication de groupes via la configuration du token de l'application enregistrée dans Entra ID si cette option est activée. La revendication de groupes devrait retourner l'identifiant de groupe (l'option par défaut) via divers types de tokens dans ce cas");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"Si les sous-modules doivent être récupérés ou non. Référez-vous à <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>ce tutoriel</a> pour savoir comment configurer les identifiants de clonage ci-dessus pour récupérer les sous-modules");
		m.put("Whether or not to run this step inside container", "Si cette étape doit être exécutée à l'intérieur d'un conteneur ou non");
		m.put("Whether or not to scan recursively in above paths", "Si une analyse récursive dans les chemins ci-dessus doit être effectuée ou non");
		m.put("Whether or not to send notifications for events generated by yourself", "Si des notifications doivent être envoyées pour les événements générés par vous-même ou non");
		m.put("Whether or not to send notifications to issue watchers for this change", "Si des notifications doivent être envoyées aux surveillants de l'issue pour ce changement ou non");
		m.put("Whether or not to show branch/tag column", "Si la colonne branche/tag doit être affichée ou non");
		m.put("Whether or not to show duration column", "Si la colonne durée doit être affichée ou non");
		m.put("Whether or not to use user avatar from a public service", "Si l'avatar utilisateur doit être utilisé à partir d'un service public ou non");
		m.put("Whether or not use force option to overwrite changes in case ref updating can not be fast-forwarded", 
			"Si l'option de force doit être utilisée pour écraser les modifications en cas de mise à jour de la référence qui ne peut pas être avancée rapidement ou non");
		m.put("Whether or not user can remove own account", "Si l'utilisateur peut supprimer son propre compte ou non");
		m.put("Whether the password must contain at least one lowercase letter", "Si le mot de passe doit contenir au moins une lettre minuscule");
		m.put("Whether the password must contain at least one number", "Si le mot de passe doit contenir au moins un chiffre");
		m.put("Whether the password must contain at least one special character", "Si le mot de passe doit contenir au moins un caractère spécial");
		m.put("Whether the password must contain at least one uppercase letter", "Si le mot de passe doit contenir au moins une lettre majuscule");
		m.put("Whole Word", "Mot entier");
		m.put("Widget", "Widget");
		m.put("Widget Tab", "Onglet Widget");
		m.put("Widget Timesheet Setting", "Paramètre de feuille de temps du Widget");
		m.put("Will be prompted to set up two-factor authentication upon next login", "Une demande de configuration de l'authentification à deux facteurs sera faite lors de la prochaine connexion");
		m.put("Will be transcoded to UTF-8", "Sera transcodé en UTF-8");
		m.put("Window", "Fenêtre");
		m.put("Window Memory", "Mémoire de fenêtre");
		m.put("With current number of users ({0}), the subscription will be active until <b>{1}</b>", 
			"Avec le nombre actuel d'utilisateurs ({0}), l'abonnement sera actif jusqu'au <b>{1}</b>");
		m.put("Workflow reconciliation completed", "Réconciliation du workflow terminée");
		m.put("Working Directory", "Répertoire de travail");
		m.put("Write", "Écrire");
		m.put("YAML", "YAML");
		m.put("Yes", "Oui");
		m.put("You are not member of discord server", "Vous n'êtes pas membre du serveur Discord");
		m.put("You are rebasing source branch on top of target branch", "Vous êtes en train de rebaser la branche source sur la branche cible");
		m.put("You are viewing a subset of all changes. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">show all changes</a>", 
			"Vous visualisez un sous-ensemble de tous les changements. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">afficher tous les changements</a>");
		m.put("You can also achieve this by adding a build docker image step to your CI/CD job and configuring the built-in registry login with an access token secret that has package write permissions", 
			"Vous pouvez également atteindre cet objectif en ajoutant une étape de création d'image Docker à votre tâche CI/CD et en configurant la connexion au registre intégré avec un jeton d'accès ayant des permissions d'écriture sur les packages");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "Vous avez des <a wicket:id=\"hasUnverifiedLink\">adresses email non vérifiées</a>");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "Vous pouvez également déposer un fichier/une image dans la boîte de saisie ou coller une image depuis le presse-papiers");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"Vous pouvez initialiser le projet en <a wicket:id=\"addFiles\" class=\"link-primary\">ajoutant des fichiers</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">configurant les spécifications de construction</a>, ou <a wicket:id=\"pushInstructions\" class=\"link-primary\">poussant un dépôt existant</a>");
		m.put("You selected to delete branch \"{0}\"", "Vous avez sélectionné de supprimer la branche \"{0}\"");
		m.put("You will be notified of any activities", "Vous serez informé de toute activité");
		m.put("You've been logged out", "Vous avez été déconnecté");
		m.put("YouTrack API URL", "URL de l'API YouTrack");
		m.put("YouTrack Issue Field", "Champ de problème YouTrack");
		m.put("YouTrack Issue Link", "Lien de problème YouTrack");
		m.put("YouTrack Issue State", "État du problème YouTrack");
		m.put("YouTrack Issue Tag", "Tag de problème YouTrack");
		m.put("YouTrack Login Name", "Nom de connexion YouTrack");
		m.put("YouTrack Password or Access Token", "Mot de passe ou jeton d'accès YouTrack");
		m.put("YouTrack Project", "Projet YouTrack");
		m.put("YouTrack Projects to Import", "Projets YouTrack à importer");
		m.put("Your email address is now verified", "Votre adresse e-mail est maintenant vérifiée");
		m.put("Your primary email address is not verified", "Votre adresse e-mail principale n'est pas vérifiée");
		m.put("[Any state]", "[Tout état]");
		m.put("[Reset Password] Please Reset Your OneDev Password", "[Réinitialiser le mot de passe] Veuillez réinitialiser votre mot de passe OneDev");
		m.put("a boolean indiciating whether or not topic comment can be created directly by replying the email", 
			"un booléen indiquant si un commentaire de sujet peut être créé directement en répondant à l'email");
		m.put("a new agent token will be generated each time this button is pressed", "un nouveau jeton d'agent sera généré chaque fois que ce bouton est pressé");
		m.put("a string representing body of the event. May be <code>null</code>", "une chaîne représentant le corps de l'événement. Peut être <code>null</code>");
		m.put("a string representing event detail url", "une chaîne représentant l'URL des détails de l'événement");
		m.put("a string representing summary of the event", "une chaîne représentant le résumé de l'événement");
		m.put("access [{0}]", "accès [{0}]");
		m.put("active", "actif");
		m.put("add another order", "ajouter une autre commande");
		m.put("adding .onedev-buildspec.yml", "ajout de .onedev-buildspec.yml");
		m.put("after specified date", "après la date spécifiée");
		m.put("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information.  A <code>null</code> value means that the notification can not be unsubscribed", 
			"un <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>objet</a> contenant des informations de désabonnement. Une valeur <code>null</code> signifie que la notification ne peut pas être désabonnée");
		m.put("and more", "et plus");
		m.put("archived", "archivé");
		m.put("artifacts", "artéfacts");
		m.put("assign to me", "assigner à moi");
		m.put("authored by", "rédigé par");
		m.put("backlog ", "arriéré");
		m.put("base", "base");
		m.put("before specified date", "avant la date spécifiée");
		m.put("branch the build commit is merged into", "branche dans laquelle le commit de construction est fusionné");
		m.put("branch the job is running against", "branche sur laquelle le travail est en cours d'exécution");
		m.put("branch {0}", "branche {0}");
		m.put("branches", "branches");
		m.put("build", "construction");
		m.put("build is successful for any job and branch", "la construction est réussie pour tout travail et branche");
		m.put("build is successful for any job on branches \"{0}\"", "la construction est réussie pour tout travail sur les branches \"{0}\"");
		m.put("build is successful for jobs \"{0}\" on any branch", "la construction est réussie pour les travaux \"{0}\" sur n'importe quelle branche");
		m.put("build is successful for jobs \"{0}\" on branches \"{1}\"", "la construction est réussie pour les travaux \"{0}\" sur les branches \"{1}\"");
		m.put("builds", "constructions");
		m.put("cURL Example", "Exemple cURL");
		m.put("choose a color for this state", "choisissez une couleur pour cet état");
		m.put("cluster:lead", "chef");
		m.put("cmd-k to show command palette", "cmd-k pour afficher la palette de commandes");
		m.put("code commit", "commit de code");
		m.put("code is committed", "le code est commité");
		m.put("code is committed to branches \"{0}\"", "le code est engagé sur les branches \"{0}\"");
		m.put("code is committed to branches \"{0}\" with message \"{1}\"", "le code est commité sur les branches \"{0}\" avec le message \"{1}\"");
		m.put("code is committed with message \"{0}\"", "le code est commité avec le message \"{0}\"");
		m.put("commit message contains", "le message de commit contient");
		m.put("commits", "commits");
		m.put("committed by", "engagé par");
		m.put("common", "commun");
		m.put("common ancestor", "ancêtre commun");
		m.put("container:image", "Image");
		m.put("copy", "copier");
		m.put("ctrl-k to show command palette", "ctrl-k pour afficher la palette de commandes");
		m.put("curl Command Line", "Commande curl");
		m.put("curl Path", "Chemin curl");
		m.put("default", "par défaut");
		m.put("descending", "décroissant");
		m.put("disabled", "désactivé");
		m.put("does not have any value of", "n'a aucune valeur de");
		m.put("duration", "durée");
		m.put("enclose with ~ to query hash/message", "entourer avec ~ pour interroger le hash/message");
		m.put("enclose with ~ to query job/version", "entourer avec ~ pour interroger le travail/version");
		m.put("enclose with ~ to query name/ip/os", "entourer avec ~ pour interroger le nom/ip/os");
		m.put("enclose with ~ to query name/path", "entourer avec ~ pour interroger le nom/chemin");
		m.put("enclose with ~ to query name/version", "entourer avec ~ pour interroger le nom/version");
		m.put("enclose with ~ to query path/content/reply", "entourer avec ~ pour interroger le chemin/contenu/réponse");
		m.put("enclose with ~ to query title/description/comment", "entourer avec ~ pour interroger le titre/la description/le commentaire");
		m.put("exclude", "exclure");
		m.put("false", "faux");
		m.put("files with ext \"{0}\"", "fichiers avec l'extension \"{0}\"");
		m.put("find build by number", "trouver une construction par numéro");
		m.put("find build with this number", "trouver une construction avec ce numéro");
		m.put("find issue by number", "trouver un problème par numéro");
		m.put("find pull request by number", "trouver une demande de tirage par numéro");
		m.put("find pull request with this number", "trouver une demande de tirage avec ce numéro");
		m.put("forked from <a wicket:id=\"forkedFrom\"></a>", "forké de <a wicket:id=\"forkedFrom\"></a>");
		m.put("found 1 agent", "trouvé 1 agent");
		m.put("found 1 build", "trouvé 1 build");
		m.put("found 1 comment", "trouvé 1 commentaire");
		m.put("found 1 issue", "trouvé 1 problème");
		m.put("found 1 package", "trouvé 1 package");
		m.put("found 1 project", "trouvé 1 projet");
		m.put("found 1 pull request", "trouvé 1 pull request");
		m.put("found 1 user", "trouvé 1 utilisateur");
		m.put("found {0} agents", "trouvé {0} agents");
		m.put("found {0} builds", "trouvé {0} builds");
		m.put("found {0} comments", "trouvé {0} commentaires");
		m.put("found {0} issues", "trouvé {0} problèmes");
		m.put("found {0} packages", "trouvé {0} packages");
		m.put("found {0} projects", "trouvé {0} projets");
		m.put("found {0} pull requests", "trouvé {0} pull requests");
		m.put("found {0} users", "trouvé {0} utilisateurs");
		m.put("has any value of", "a une valeur quelconque de");
		m.put("head", "tête");
		m.put("in current commit", "dans le commit actuel");
		m.put("ineffective", "inefficace");
		m.put("inherited", "hérité");
		m.put("initial", "initial");
		m.put("is empty", "est vide");
		m.put("is not empty", "n'est pas vide");
		m.put("issue", "problème");
		m.put("issue:Number", "Numéro");
		m.put("issues", "problèmes");
		m.put("job", "travail");
		m.put("key ID: ", "ID de clé :");
		m.put("lines", "lignes");
		m.put("link:Multiple", "Multiple");
		m.put("log", "journal");
		m.put("manage job", "gérer le travail");
		m.put("markdown:heading", "Titre");
		m.put("markdown:image", "Image");
		m.put("may not be empty", "ne peut pas être vide");
		m.put("merged", "fusionné");
		m.put("month:Apr", "Avr");
		m.put("month:Aug", "Aoû");
		m.put("month:Dec", "Déc");
		m.put("month:Feb", "Fév");
		m.put("month:Jan", "Jan");
		m.put("month:Jul", "Juil");
		m.put("month:Jun", "Juin");
		m.put("month:Mar", "Mar");
		m.put("month:May", "Mai");
		m.put("month:Nov", "Nov");
		m.put("month:Oct", "Oct");
		m.put("month:Sep", "Sep");
		m.put("n/a", "n/a");
		m.put("new field", "nouveau champ");
		m.put("no activity for {0} days", "aucune activité depuis {0} jours");
		m.put("on file {0}", "sur le fichier {0}");
		m.put("opened", "ouvert");
		m.put("opened <span wicket:id=\"submitDate\"></span>", "ouvert <span wicket:id=\"submitDate\"></span>");
		m.put("or match another value", "ou correspond à une autre valeur");
		m.put("order more", "commander plus");
		m.put("outdated", "obsolète");
		m.put("pack", "pack");
		m.put("package", "package");
		m.put("packages", "packages");
		m.put("personal", "personnel");
		m.put("pipeline", "pipeline");
		m.put("project of the running job", "projet du travail en cours");
		m.put("property", "propriété");
		m.put("pull request", "pull request");
		m.put("pull request #{0}", "pull request #{0}");
		m.put("pull request and code review", "pull request et revue de code");
		m.put("pull request to any branch is discarded", "pull request vers n'importe quelle branche est rejetée");
		m.put("pull request to any branch is merged", "pull request vers n'importe quelle branche est fusionnée");
		m.put("pull request to any branch is opened", "pull request vers n'importe quelle branche est ouverte");
		m.put("pull request to branches \"{0}\" is discarded", "pull request vers les branches \"{0}\" est rejetée");
		m.put("pull request to branches \"{0}\" is merged", "pull request vers les branches \"{0}\" est fusionnée");
		m.put("pull request to branches \"{0}\" is opened", "pull request vers les branches \"{0}\" est ouverte");
		m.put("pull requests", "pull requests");
		m.put("reconciliation (need administrator permission)", "réconciliation (nécessite une autorisation administrateur)");
		m.put("reports", "rapports");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> object to be notified", 
			"représente l'objet <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> à notifier");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk", 
			"représente le <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problème</a> ouvert via le service desk");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified", 
			"représente l'objet <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problème</a> à notifier");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> object to be notified", 
			"représente l'objet <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> à notifier");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified", 
			"représente l'objet <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> à notifier");
		m.put("represents the <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> object to be notified", 
			"représente l'objet <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> à notifier");
		m.put("represents the exception encountered when open issue via service desk", "représente l'exception rencontrée lors de l'ouverture d'un problème via le service desk");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>", 
			"représente le <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problème</a> désabonné");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>", 
			"représente le <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> désabonné");
		m.put("request to change", "demande de modification");
		m.put("root", "racine");
		m.put("root url of OneDev server", "url racine du serveur OneDev");
		m.put("run job", "exécuter le travail");
		m.put("search in this revision will be accurate after indexed", "la recherche dans cette révision sera précise après indexation");
		m.put("service", "service");
		m.put("severity:CRITICAL", "Critique");
		m.put("severity:HIGH", "Élevé");
		m.put("severity:LOW", "Faible");
		m.put("severity:MEDIUM", "Moyen");
		m.put("skipped {0} lines", "passé {0} lignes");
		m.put("space", "espace");
		m.put("state of an issue is transited", "l'état d'un problème est transité");
		m.put("step template", "modèle d'étape");
		m.put("submit", "soumettre");
		m.put("tag the job is running against", "tag contre lequel le travail est en cours");
		m.put("tag {0}", "tag {0}");
		m.put("tags", "tags");
		m.put("the url to set up user account", "l'URL pour configurer le compte utilisateur");
		m.put("time aggregation link", "lien d'agrégation temporelle");
		m.put("touching specified path", "accéder au chemin spécifié");
		m.put("transit manually by any user", "transiter manuellement par tout utilisateur");
		m.put("transit manually by any user of roles \"{0}\"", "transiter manuellement par tout utilisateur des rôles \"{0}\"");
		m.put("true", "vrai");
		m.put("true for html version, false for text version", "vrai pour la version HTML, faux pour la version texte");
		m.put("up to date", "à jour");
		m.put("url following which to verify email address", "URL permettant de vérifier l'adresse e-mail");
		m.put("url to reset password", "URL pour réinitialiser le mot de passe");
		m.put("value needs to be enclosed in brackets", "la valeur doit être entourée de crochets");
		m.put("value needs to be enclosed in parenthesis", "la valeur doit être entourée de parenthèses");
		m.put("value should be quoted", "la valeur doit être entre guillemets");
		m.put("w%02d", "w%02d");
		m.put("week:Fri", "Ven");
		m.put("week:Mon", "Lun");
		m.put("week:Sat", "Sam");
		m.put("week:Sun", "Dim");
		m.put("week:Thu", "Jeu");
		m.put("week:Tue", "Mar");
		m.put("week:Wed", "Mer");
		m.put("widget:Tabs", "Onglets");
		m.put("you may show this page later via incompatibilities link in help menu", "vous pouvez afficher cette page plus tard via le lien des incompatibilités dans le menu d'aide");
		m.put("{0} Month(s)", "{0} Mois");
		m.put("{0} activities on {1}", "{0} activités sur {1}");
		m.put("{0} additions & {1} deletions", "{0} ajouts & {1} suppressions");
		m.put("{0} ahead", "{0} en avance");
		m.put("{0} behind", "{0} en retard");
		m.put("{0} branches", "{0} branches");
		m.put("{0} build(s)", "{0} construction(s)");
		m.put("{0} child projects", "{0} projets enfants");
		m.put("{0} commits", "{0} commits");
		m.put("{0} commits ahead of base branch", "{0} commits en avance sur la branche de base");
		m.put("{0} commits behind of base branch", "{0} commits en retard sur la branche de base");
		m.put("{0} day", "{0} jour");
		m.put("{0} days", "{0} jours");
		m.put("{0} edited {1}", "{0} a modifié {1}");
		m.put("{0} files", "{0} fichiers");
		m.put("{0} forks", "{0} forks");
		m.put("{0} hour", "{0} heure");
		m.put("{0} hours", "{0} heures");
		m.put("{0} inaccessible activities", "{0} activités inaccessibles");
		m.put("{0} minute", "{0} minute");
		m.put("{0} minutes", "{0} minutes");
		m.put("{0} reviewed", "{0} a examiné");
		m.put("{0} second", "{0} seconde");
		m.put("{0} seconds", "{0} secondes");
		m.put("{0} tags", "{0} tags");
		m.put("{0}d", "{0}j");
		m.put("{0}h", "{0}h");
		m.put("{0}m", "{0}m");
		m.put("{0}s", "{0}s");
		m.put("{0}w", "{0}sem");
		m.put("{javax.validation.constraints.NotEmpty.message}", "{javax.validation.constraints.NotEmpty.message}");
		m.put("{javax.validation.constraints.NotNull.message}", "{javax.validation.constraints.NotNull.message}");
		m.put("{javax.validation.constraints.Size.message}", "{javax.validation.constraints.Size.message}");
	}
		
	@Override
	protected Map<String, String> getContents() {
		return m;		
	}
	
}
