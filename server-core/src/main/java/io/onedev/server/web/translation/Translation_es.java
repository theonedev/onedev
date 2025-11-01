package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_es extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init(m);
		Translation.watchUpdate(Translation_es.class, () -> {
			init(m);
		});
	}

	@SystemPrompt("You are good at translating from English to Spanish in DevOps software area.")
	public static void init(Map<String, String> m) {
		m.clear();
		m.put(" Project path can be omitted if reference from current project", "La ruta del proyecto puede omitirse si se hace referencia desde el proyecto actual");
		m.put("'..' is not allowed in the directory", "'..' no está permitido en el directorio");
		m.put("(* = any string, ? = any character)", "(* = cualquier cadena, ? = cualquier carácter)");
		m.put("(on behalf of <b>{0}</b>)", "(en nombre de <b>{0}</b>)");
		m.put("** Enterprise edition is disabled as the subscription was expired. Renew to enable **", 
			"** La edición Enterprise está deshabilitada ya que la suscripción ha expirado. Renueve para habilitar **");
		m.put("** Enterprise edition is disabled as the trial subscription was expired, order subscription to enable or contact support@onedev.io if you need to extend your trial **", 
			"** La edición empresarial está deshabilitada porque la suscripción de prueba expiró, ordene una suscripción para habilitarla o contacte a support@onedev.io si necesita extender su prueba **");
		m.put("** Enterprise edition is disabled as there is no remaining user months. Order more to enable **", 
			"** La edición empresarial está deshabilitada porque no hay meses de usuario restantes. Ordene más para habilitarla **");
		m.put("1. To use this package, add below to project pom.xml", "1. Para usar este paquete, agregue lo siguiente al archivo pom.xml del proyecto");
		m.put("1. Use below repositories in project pom.xml", "1. Use los siguientes repositorios en el archivo pom.xml del proyecto");
		m.put("1w 1d 1h 1m", "1s 1d 1h 1m");
		m.put("2. Add below to <code>$HOME/.m2/settings.xml</code> if you want to deploy from command line", 
			"2. Agregue lo siguiente a <code>$HOME/.m2/settings.xml</code> si desea desplegar desde la línea de comandos");
		m.put("2. Also add below to $HOME/.m2/settings.xml if you want to compile project from command line", 
			"2. También agregue lo siguiente a $HOME/.m2/settings.xml si desea compilar el proyecto desde la línea de comandos");
		m.put("3. For CI/CD job, it is more convenient to use a custom settings.xml, for instance via below code in a command step:", 
			"3. Para trabajos de CI/CD, es más conveniente usar un settings.xml personalizado, por ejemplo, mediante el siguiente código en un paso de comando:");
		m.put("6-digits passcode", "Código de acceso de 6 dígitos");
		m.put("7 days", "7 días");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">usuario</a> para restablecer la contraseña");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">usuario</a> para verificar el correo electrónico");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">Markdown con estilo GitHub</a> es aceptado, con <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">soporte para mermaid y katex</a>.");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>objeto de evento</a> que desencadena la notificación");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alerta</a> para mostrar");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a> overdue", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>cronómetro</a> vencido");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> realizó un commit <span wicket:id=\"date\"></span>");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed with <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> realizó un commit con <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>");
		m.put("<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depends on me", "<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depende de mí");
		m.put("<a wicket:id=\"removePassword\">Remove password</a> to force the user to authenticate via external system", 
			"<a wicket:id=\"removePassword\">Eliminar contraseña</a> para forzar al usuario a autenticarse mediante un sistema externo");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">Verificar con código de recuperación</a> si no puede acceder a su autenticador TOTP");
		m.put("<b class='text-danger'>NOTE: </b> This requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b> Esto requiere una suscripción empresarial. <a href='https://onedev.io/pricing' target='_blank'>Pruebe gratis</a> durante 30 días");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b> Este paso requiere una suscripción empresarial. <a href='https://onedev.io/pricing' target='_blank'>Pruebe gratis</a> durante 30 días");
		m.put("<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b>La integración con SendGrid es una característica empresarial. <a href='https://onedev.io/pricing' target='_blank'>Pruebe gratis</a> durante 30 días");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>NOTA: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>El seguimiento de tiempo</a> es una característica empresarial. <a href='https://onedev.io/pricing' target='_blank'>Pruebe gratis</a> durante 30 días");
		m.put("<b>NOTE: </b> Service desk only takes effect if <a wicket:id=\"mailConnector\">mail service</a> is defined and its <tt>check incoming email</tt> option is enabled. Also <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>sub addressing</a> needs to be enabled for the system email address. Check <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>this tutorial</a> for details", 
			"<b>NOTA: </b> El servicio de mesa de ayuda solo tiene efecto si <a wicket:id=\"mailConnector\">el servicio de correo</a> está definido y su opción <tt>verificar correo entrante</tt> está habilitada. También se necesita habilitar <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>subdireccionamiento</a> para la dirección de correo del sistema. Consulte <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>este tutorial</a> para más detalles");
		m.put("<b>NOTE:</b> Batch editing issues will not cause state transitions of other issues even if transition rule matches", 
			"<b>NOTA:</b> La edición masiva de problemas no causará transiciones de estado en otros problemas, incluso si la regla de transición coincide");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>Propietario del Proyecto</b> es un rol integrado con permiso completo sobre los proyectos");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>Consejos: </b> Escriba <tt>@</tt> para <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insertar una variable</a>. Use <tt>@@</tt> para un <tt>@</tt> literal");
		m.put("<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Buscar Archivos</span> <span class='font-size-sm text-muted'>en la rama predeterminada</span></div>");
		m.put("<div><span>Search Symbols</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Buscar Símbolos</span> <span class='font-size-sm text-muted'>en la rama predeterminada</span></div>");
		m.put("<div><span>Search Text</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Buscar Texto</span> <span class='font-size-sm text-muted'>en la rama predeterminada</span></div>");
		m.put("<i>No Name</i>", "<i>Sin Nombre</i>");
		m.put("<span class=\"keycap ml-3 mr-1\">esc</span> to close", "<span class=\"keycap ml-3 mr-1\">esc</span> para cerrar");
		m.put("<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> to move", 
			"<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> para mover");
		m.put("<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> to navigate. <span class=\"keycap\">Esc</span> to close", 
			"<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> para navegar. <span class=\"keycap\">Esc</span> para cerrar");
		m.put("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete.", 
			"<span class='keycap'>Tab</span> o <span class='keycap'>Enter</span> para completar.");
		m.put("<span class='keycap'>Tab</span> to complete.", "<span class='keycap'>Tab</span> para completar.");
		m.put("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>", "<span class='text-nowrap'><span class='keycap'>Enter</span> para ir</span>");
		m.put("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>", "<span class='text-nowrap'><span class='keycap'>Tab</span> para buscar</span>");
		m.put("<span wicket:id=\"activityCount\"></span> activities", "<span wicket:id=\"activityCount\"></span> actividades");
		m.put("<svg class='icon mr-2'><use xlink:href='%s'/></svg> Define job secrets to be used in build spec. Secrets with <b>same name</b> can be defined. For a particular name, the first authorized secret with that name will be used (search in current project first, then search in parent projects). Note that secret value containing line breaks or less than <b>%d</b> characters will not be masked in build log", 
			"<svg class='icon mr-2'><use xlink:href='%s'/></svg> Defina secretos de trabajo para ser usados en la especificación de construcción. Se pueden definir secretos con <b>el mismo nombre</b>. Para un nombre en particular, se usará el primer secreto autorizado con ese nombre (busque primero en el proyecto actual, luego en los proyectos principales). Tenga en cuenta que el valor del secreto que contenga saltos de línea o menos de <b>%d</b> caracteres no será enmascarado en el registro de construcción");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here", 
			"Se espera aquí un <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>patrón de Java</a>");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"Una <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>expresión regular de Java</a> para validar el pie de mensaje de commit");
		m.put("A child project with name \"{0}\" already exists under \"{1}\"", "Ya existe un proyecto hijo con el nombre \"{0}\" bajo \"{1}\"");
		m.put("A file exists where you’re trying to create a subdirectory. Choose a new path and try again..", 
			"Un archivo existe donde estás intentando crear un subdirectorio. Elige una nueva ruta e inténtalo de nuevo.");
		m.put("A path with same name already exists.Please choose a different name and try again.", 
			"Ya existe una ruta con el mismo nombre. Por favor, elige un nombre diferente e inténtalo de nuevo.");
		m.put("A pull request is open for this change", "Hay una solicitud de extracción abierta para este cambio");
		m.put("A root project with name \"{0}\" already exists", "Ya existe un proyecto raíz con el nombre \"{0}\"");
		m.put("A {0} used as body of address verification email", "Un {0} usado como cuerpo del correo de verificación de dirección");
		m.put("A {0} used as body of build notification email", "Un {0} usado como cuerpo del correo de notificación de construcción");
		m.put("A {0} used as body of commit notification email", "Un {0} usado como cuerpo del correo de notificación de commit");
		m.put("A {0} used as body of feedback email when failed to open issue via service desk", "Un {0} usado como cuerpo del correo de retroalimentación cuando no se pudo abrir un problema a través de la mesa de ayuda");
		m.put("A {0} used as body of feedback email when issue is opened via service desk", "Un {0} usado como cuerpo del correo de retroalimentación cuando se abre un problema a través de la mesa de ayuda");
		m.put("A {0} used as body of feedback email when unsubscribed from issue notification", "Un {0} usado como cuerpo del correo de retroalimentación cuando se cancela la suscripción a la notificación de problemas");
		m.put("A {0} used as body of feedback email when unsubscribed from pull request notification", 
			"Un {0} usado como cuerpo del correo de retroalimentación cuando se cancela la suscripción a la notificación de solicitudes de extracción");
		m.put("A {0} used as body of issue stopwatch overdue notification email", "Un {0} usado como cuerpo del correo de notificación de cronómetro de problemas vencido");
		m.put("A {0} used as body of package notification email", "Un {0} usado como cuerpo del correo de notificación de paquete");
		m.put("A {0} used as body of password reset email", "Un {0} usado como cuerpo del correo de restablecimiento de contraseña");
		m.put("A {0} used as body of system alert email", "Un {0} usado como cuerpo del correo de alerta del sistema");
		m.put("A {0} used as body of user invitation email", "Un {0} usado como cuerpo del correo de invitación de usuario");
		m.put("A {0} used as body of various issue notification emails", "Un {0} usado como cuerpo de varios correos de notificación de problemas");
		m.put("A {0} used as body of various pull request notification emails", "Un {0} usado como cuerpo de varios correos de notificación de solicitudes de extracción");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"URL de la API de su instancia de JIRA en la nube, por ejemplo, <tt>https://your-domain.atlassian.net/rest/api/3</tt>");
		m.put("Able to merge without conflicts", "Capaz de fusionar sin conflictos");
		m.put("Absolute or relative url of the image", "URL absoluta o relativa de la imagen");
		m.put("Absolute or relative url of the link", "URL absoluta o relativa del enlace");
		m.put("Access Anonymously", "Acceso Anónimo");
		m.put("Access Build Log", "Acceso al Registro de Construcción");
		m.put("Access Build Pipeline", "Acceso al Pipeline de Construcción");
		m.put("Access Build Reports", "Acceso a los Informes de Construcción");
		m.put("Access Confidential Issues", "Acceso a Problemas Confidenciales");
		m.put("Access Time Tracking", "Acceso al Seguimiento de Tiempo");
		m.put("Access Token", "Token de Acceso");
		m.put("Access Token Authorization Bean", "Bean de Autorización de Token de Acceso");
		m.put("Access Token Edit Bean", "Bean de Edición de Token de Acceso");
		m.put("Access Token Secret", "Secreto del Token de Acceso");
		m.put("Access Token for Target Project", "Token de Acceso para el Proyecto Objetivo");
		m.put("Access Tokens", "Tokens de Acceso");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"El token de acceso está destinado para acceso a la API y extracción/push de repositorios. No puede usarse para iniciar sesión en la interfaz web");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"El token de acceso está destinado para acceso a la API o extracción/push de repositorios. No puede usarse para iniciar sesión en la interfaz web");
		m.put("Access token regenerated successfully", "Token de acceso regenerado exitosamente");
		m.put("Access token regenerated, make sure to update the token at agent side", "Token de acceso regenerado, asegúrese de actualizar el token en el lado del agente");
		m.put("Account Email", "Correo Electrónico de la Cuenta");
		m.put("Account Name", "Nombre de la Cuenta");
		m.put("Account is disabled", "La cuenta está deshabilitada");
		m.put("Account set up successfully", "Configuración de cuenta exitosa");
		m.put("Active Directory", "Active Directory");
		m.put("Active Since", "Activo Desde");
		m.put("Activities", "Actividades");
		m.put("Activity by type", "Actividad por tipo");
		m.put("Add", "Agregar");
		m.put("Add Executor", "Agregar Ejecutor");
		m.put("Add GPG key", "Agregar clave GPG");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "Agregue claves GPG aquí para verificar commits/etiquetas firmados por este usuario");
		m.put("Add GPG keys here to verify commits/tags signed by you", "Agregue claves GPG aquí para verificar commits/etiquetas firmados por usted");
		m.put("Add GPG public keys to be trusted here. Commits signed with trusted keys will be shown as verified.", 
			"Agregue claves públicas GPG confiables aquí. Los commits firmados con claves confiables se mostrarán como verificados.");
		m.put("Add Issue...", "Agregar Problema...");
		m.put("Add Issues to Iteration", "Agregar Problemas a la Iteración");
		m.put("Add New", "Agregar Nuevo");
		m.put("Add New Board", "Agregar Nuevo Tablero");
		m.put("Add New Email Address", "Agregar Nueva Dirección de Correo Electrónico");
		m.put("Add New Timesheet", "Añadir nueva hoja de tiempo");
		m.put("Add Rule", "Añadir regla");
		m.put("Add SSH key", "Añadir clave SSH");
		m.put("Add SSO provider", "Añadir proveedor SSO");
		m.put("Add Spent Time", "Añadir tiempo dedicado");
		m.put("Add Timesheet", "Añadir hoja de tiempo");
		m.put("Add Widget", "Añadir widget");
		m.put("Add a GPG Public Key", "Añadir una clave pública GPG");
		m.put("Add a SSH Key", "Añadir una clave SSH");
		m.put("Add a package source like below", "Añadir una fuente de paquetes como se muestra abajo");
		m.put("Add after", "Añadir después");
		m.put("Add agent", "Añadir agente");
		m.put("Add all cards to specified iteration", "Añadir todas las tarjetas a la iteración especificada");
		m.put("Add all commits from source branch to target branch with a merge commit", "Añadir todos los commits de la rama fuente a la rama destino con un commit de fusión");
		m.put("Add assignee...", "Añadir asignado...");
		m.put("Add before", "Añadir antes");
		m.put("Add below to allow accessing via http protocol in new Maven versions", "Añadir lo siguiente para permitir el acceso mediante el protocolo http en las nuevas versiones de Maven");
		m.put("Add child project", "Añadir proyecto hijo");
		m.put("Add comment", "Añadir comentario");
		m.put("Add comment on this selection", "Añadir comentario en esta selección");
		m.put("Add custom field", "Añadir campo personalizado");
		m.put("Add dashboard", "Añadir tablero");
		m.put("Add default issue board", "Añadir tablero de problemas predeterminado");
		m.put("Add files to current directory", "Añadir archivos al directorio actual");
		m.put("Add files via upload", "Añadir archivos mediante carga");
		m.put("Add groovy script", "Añadir script groovy");
		m.put("Add issue description template", "Añadir plantilla de descripción de problema");
		m.put("Add issue link", "Añadir enlace de problema");
		m.put("Add issue state", "Añadir estado de problema");
		m.put("Add issue state transition", "Añadir transición de estado de problema");
		m.put("Add link", "Añadir enlace");
		m.put("Add new", "Añadir nuevo");
		m.put("Add new card to this column", "Añadir nueva tarjeta a esta columna");
		m.put("Add new file", "Añadir nuevo archivo");
		m.put("Add new import", "Añadir nueva importación");
		m.put("Add new issue creation setting", "Añadir nueva configuración de creación de problemas");
		m.put("Add new job dependency", "Añadir nueva dependencia de trabajo");
		m.put("Add new param", "Añadir nuevo parámetro");
		m.put("Add new post-build action", "Añadir nueva acción posterior a la construcción");
		m.put("Add new project dependency", "Añadir nueva dependencia de proyecto");
		m.put("Add new step", "Añadir nuevo paso");
		m.put("Add new trigger", "Añadir nuevo disparador");
		m.put("Add project", "Añadir proyecto");
		m.put("Add reviewer...", "Añadir revisor...");
		m.put("Add to batch to commit with other suggestions later", "Añadir al lote para confirmar con otras sugerencias más tarde");
		m.put("Add to group...", "Añadir al grupo...");
		m.put("Add to iteration...", "Añadir a la iteración...");
		m.put("Add user to group...", "Añadir usuario al grupo...");
		m.put("Add value", "Añadir valor");
		m.put("Add {0}", "Añadir {0}");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "Commit añadido \"{0}\" (<i class='text-danger'>faltante en el repositorio</i>)");
		m.put("Added commit \"{0}\" ({1})", "Commit añadido \"{0}\" ({1})");
		m.put("Added to group", "Añadido al grupo");
		m.put("Additions", "Adiciones");
		m.put("Administration", "Administración");
		m.put("Administrative permission over a project", "Permiso administrativo sobre un proyecto");
		m.put("Advanced Search", "Búsqueda avanzada");
		m.put("After modification", "Después de la modificación");
		m.put("Agent", "Agente");
		m.put("Agent Attribute", "Atributo del agente");
		m.put("Agent Count", "Conteo de agentes");
		m.put("Agent Edit Bean", "Editar bean del agente");
		m.put("Agent Selector", "Selector de agente");
		m.put("Agent is designed to be maintenance free. Once connected to server, it will be updated automatically upon server upgrade", 
			"El agente está diseñado para ser libre de mantenimiento. Una vez conectado al servidor, se actualizará automáticamente tras la actualización del servidor");
		m.put("Agent removed", "Agente eliminado");
		m.put("Agent tokens are used to authorize agents. It should be configured via environment variable <tt>agentToken</tt> if agent runs as docker container, or property <tt>agentToken</tt> in file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> if agent runs on bare metal/virtual machine. A token will be in-use and removed from this list if agent using it connects to server", 
			"Los tokens de agente se utilizan para autorizar agentes. Deben configurarse mediante la variable de entorno <tt>agentToken</tt> si el agente se ejecuta como contenedor Docker, o la propiedad <tt>agentToken</tt> en el archivo <tt>&lt;agent dir&gt;/conf/agent.properties</tt> si el agente se ejecuta en metal desnudo/máquina virtual. Un token estará en uso y se eliminará de esta lista si el agente que lo utiliza se conecta al servidor");
		m.put("Agents", "Agentes");
		m.put("Agents can be used to execute jobs on remote machines. Once started it will update itself from server automatically when necessary", 
			"Los agentes pueden ser utilizados para ejecutar trabajos en máquinas remotas. Una vez iniciados, se actualizarán automáticamente desde el servidor cuando sea necesario");
		m.put("Aggregated from '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':", "Agregado desde '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':");
		m.put("Aggregated from '<span wicket:id=\"spentTimeAggregationLink\"></span>':", "Agregado desde '<span wicket:id=\"spentTimeAggregationLink\"></span>':");
		m.put("Aggregation Link", "Enlace de agregación");
		m.put("Alert", "Alerta");
		m.put("Alert Setting", "Configuración de alerta");
		m.put("Alert Settings", "Configuraciones de alerta");
		m.put("Alert settings have been updated", "Las configuraciones de alerta han sido actualizadas");
		m.put("Alerts", "Alertas");
		m.put("All", "Todo");
		m.put("All Issues", "Todos los problemas");
		m.put("All RESTful Resources", "Todos los recursos RESTful");
		m.put("All accessible", "Todo accesible");
		m.put("All builds", "Todos los builds");
		m.put("All changes", "Todos los cambios");
		m.put("All except", "Todo excepto");
		m.put("All files", "Todos los archivos");
		m.put("All groups", "Todos los grupos");
		m.put("All issues", "Todos los problemas");
		m.put("All platforms in OCI layout", "Todas las plataformas en el diseño OCI");
		m.put("All platforms in image", "Todas las plataformas en la imagen");
		m.put("All possible classes", "Todas las clases posibles");
		m.put("All projects", "Todos los proyectos");
		m.put("All projects with code read permission", "Todos los proyectos con permiso de lectura de código");
		m.put("All pull requests", "Todos los pull requests");
		m.put("All users", "Todos los usuarios");
		m.put("Allow Empty", "Permitir vacío");
		m.put("Allow Empty Value", "Permitir valor vacío");
		m.put("Allow Multiple", "Permitir múltiples");
		m.put("Allowed Licenses", "Licencias permitidas");
		m.put("Allowed Self Sign-Up Email Domain", "Dominio de correo electrónico permitido para auto registro");
		m.put("Always", "Siempre");
		m.put("Always Pull Image", "Siempre extraer imagen");
		m.put("An issue already linked for {0}. Unlink it first", "Ya hay un problema vinculado para {0}. Desvincúlalo primero");
		m.put("An unexpected exception occurred", "Ocurrió una excepción inesperada");
		m.put("And configure auth token of the registry", "Y configura el token de autenticación del registro");
		m.put("Another pull request already open for this change", "Ya hay una solicitud de extracción abierta para este cambio");
		m.put("Any agent", "Cualquier agente");
		m.put("Any branch", "Cualquier rama");
		m.put("Any commit message", "Cualquier mensaje de commit");
		m.put("Any domain", "Cualquier dominio");
		m.put("Any file", "Cualquier archivo");
		m.put("Any issue", "Cualquier problema");
		m.put("Any job", "Cualquier trabajo");
		m.put("Any project", "Cualquier proyecto");
		m.put("Any ref", "Cualquier referencia");
		m.put("Any sender", "Cualquier remitente");
		m.put("Any state", "Cualquier estado");
		m.put("Any tag", "Cualquier etiqueta");
		m.put("Any user", "Cualquier usuario");
		m.put("Api Key", "Clave API");
		m.put("Api Token", "Token API");
		m.put("Api Url", "URL API");
		m.put("Append", "Anexar");
		m.put("Applicable Branches", "Ramas aplicables");
		m.put("Applicable Builds", "Compilaciones aplicables");
		m.put("Applicable Code Comments", "Comentarios de código aplicables");
		m.put("Applicable Commit Messages", "Mensajes de Commit Aplicables");
		m.put("Applicable Commits", "Commits aplicables");
		m.put("Applicable Images", "Imágenes aplicables");
		m.put("Applicable Issues", "Problemas aplicables");
		m.put("Applicable Jobs", "Trabajos aplicables");
		m.put("Applicable Names", "Nombres aplicables");
		m.put("Applicable Projects", "Proyectos aplicables");
		m.put("Applicable Pull Requests", "Solicitudes de extracción aplicables");
		m.put("Applicable Senders", "Remitentes aplicables");
		m.put("Applicable Users", "Usuarios aplicables");
		m.put("Application (client) ID", "ID de aplicación (cliente)");
		m.put("Apply suggested change from code comment", "Aplicar el cambio sugerido del comentario de código");
		m.put("Apply suggested changes from code comments", "Aplicar los cambios sugeridos de los comentarios de código");
		m.put("Approve", "Aprobar");
		m.put("Approved", "Aprobado");
		m.put("Approved pull request \"{0}\" ({1})", "Solicitud de extracción aprobada \"{0}\" ({1})");
		m.put("Arbitrary scope", "Ámbito arbitrario");
		m.put("Arbitrary type", "Tipo arbitrario");
		m.put("Arch Pull Command", "Comando de extracción de Arch");
		m.put("Archived", "Archivado");
		m.put("Arguments", "Argumentos");
		m.put("Artifacts", "Artefactos");
		m.put("Artifacts to Retrieve", "Artefactos para recuperar");
		m.put("As long as a feature can be accessed via url, you can input part of the url to match and jump", 
			"Siempre que una función pueda ser accedida a través de una url, puedes ingresar parte de la url para coincidir y saltar");
		m.put("Ascending", "Ascendente");
		m.put("Assignees", "Asignados");
		m.put("Assignees Issue Field", "Campo de problema de asignados");
		m.put("Assignees are expected to merge the pull request", "Se espera que los asignados fusionen la solicitud de extracción");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"Los asignados tienen permiso de escritura de código y serán responsables de fusionar la solicitud de extracción");
		m.put("Asymmetric", "Asimétrico");
		m.put("At least one branch or tag should be selected", "Debe seleccionarse al menos una rama o etiqueta");
		m.put("At least one choice need to be specified", "Debe especificarse al menos una opción");
		m.put("At least one email address should be configured, please add a new one first", "Debe configurarse al menos una dirección de correo electrónico, por favor agregue una nueva primero");
		m.put("At least one email address should be specified", "Debe especificarse al menos una dirección de correo electrónico");
		m.put("At least one entry should be specified", "Debe especificarse al menos una entrada");
		m.put("At least one event type needs to be selected", "Debe seleccionarse al menos un tipo de evento");
		m.put("At least one field needs to be specified", "Debe especificarse al menos un campo");
		m.put("At least one project should be authorized", "Debe autorizarse al menos un proyecto");
		m.put("At least one project should be selected", "Debe seleccionarse al menos un proyecto");
		m.put("At least one repository should be selected", "Debe seleccionarse al menos un repositorio");
		m.put("At least one role is required", "Se requiere al menos un rol");
		m.put("At least one role must be selected", "Debe seleccionarse al menos un rol");
		m.put("At least one state should be specified", "Debe especificarse al menos un estado");
		m.put("At least one tab should be added", "Debe agregarse al menos una pestaña");
		m.put("At least one user search base should be specified", "Debe especificarse al menos una base de búsqueda de usuarios");
		m.put("At least one value needs to be specified", "Debe especificarse al menos un valor");
		m.put("At least two columns need to be defined", "Debe definirse al menos dos columnas");
		m.put("Attachment", "Adjunto");
		m.put("Attributes", "Atributos");
		m.put("Attributes (can only be edited when agent is online)", "Atributos (solo se pueden editar cuando el agente está en línea)");
		m.put("Attributes saved", "Atributos guardados");
		m.put("Audit", "Auditoría");
		m.put("Audit Log", "Registro de auditoría");
		m.put("Audit Setting", "Configuración de auditoría");
		m.put("Audit log will be preserved for the specified number of days. This setting applies to all audit events, including system level and project level", 
			"El registro de auditoría se conservará durante el número de días especificado. Esta configuración se aplica a todos los eventos de auditoría, incluidos los de nivel de sistema y nivel de proyecto");
		m.put("Auth Source", "Fuente de autenticación");
		m.put("Authenticate to Bitbucket Cloud", "Autenticarse en Bitbucket Cloud");
		m.put("Authenticate to GitHub", "Autenticarse en GitHub");
		m.put("Authenticate to GitLab", "Autenticarse en GitLab");
		m.put("Authenticate to Gitea", "Autenticarse en Gitea");
		m.put("Authenticate to JIRA cloud", "Autenticarse en JIRA Cloud");
		m.put("Authenticate to YouTrack", "Autenticarse en YouTrack");
		m.put("Authentication", "Autenticación");
		m.put("Authentication Required", "Autenticación requerida");
		m.put("Authentication Test", "Prueba de autenticación");
		m.put("Authentication Token", "Token de autenticación");
		m.put("Authenticator", "Autenticador");
		m.put("Authenticator Bean", "Bean de autenticador");
		m.put("Author", "Autor");
		m.put("Author date", "Fecha del autor");
		m.put("Authored By", "Autorizado por");
		m.put("Authorization", "Autorización");
		m.put("Authorizations", "Autorizaciones");
		m.put("Authorize user...", "Autorizar usuario...");
		m.put("Authorized Projects", "Proyectos autorizados");
		m.put("Authorized Roles", "Roles autorizados");
		m.put("Auto Merge", "Fusión automática");
		m.put("Auto Spec", "Especificación automática");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"La verificación de actualización automática se realiza solicitando una imagen en su navegador desde onedev.io que indica la disponibilidad de una nueva versión, con un color que indica la gravedad de la actualización. Funciona de la misma manera que las solicitudes de gravatar para imágenes de avatar. Si está deshabilitado, se recomienda encarecidamente verificar la actualización manualmente de vez en cuando (puede hacerse a través del menú de ayuda en la parte inferior izquierda de la pantalla) para ver si hay correcciones de seguridad/críticas");
		m.put("Auto-discovered executor", "Ejecutor descubierto automáticamente");
		m.put("Available Agent Tokens", "Tokens de agente disponibles");
		m.put("Available Choices", "Opciones disponibles");
		m.put("Avatar", "Avatar");
		m.put("Avatar Service Url", "URL del servicio de avatar");
		m.put("Avatar and name", "Avatar y nombre");
		m.put("Back To Home", "Volver a Inicio");
		m.put("Backlog", "Backlog");
		m.put("Backlog Base Query", "Consulta base del Backlog");
		m.put("Backup", "Respaldo");
		m.put("Backup Now", "Respaldar ahora");
		m.put("Backup Schedule", "Programación de respaldo");
		m.put("Backup Setting", "Configuración de respaldo");
		m.put("Backup Setting Holder", "Contenedor de configuración de respaldo");
		m.put("Backup settings updated", "Configuración de respaldo actualizada");
		m.put("Bare Metal", "Metal desnudo");
		m.put("Base", "Base");
		m.put("Base Gpg Key", "Clave Gpg base");
		m.put("Base Query", "Consulta base");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Formato PEM codificado en Base64, comenzando con -----BEGIN CERTIFICATE----- y terminando con -----END CERTIFICATE-----");
		m.put("Basic Info", "Información básica");
		m.put("Basic Settings", "Configuraciones básicas");
		m.put("Basic settings updated", "Configuraciones básicas actualizadas");
		m.put("Batch Edit All Queried Issues", "Editar en lote todos los problemas consultados");
		m.put("Batch Edit Selected Issues", "Editar en lote los problemas seleccionados");
		m.put("Batch Editing {0} Issues", "Editando en lote {0} problemas");
		m.put("Batched suggestions", "Sugerencias agrupadas");
		m.put("Before modification", "Antes de la modificación");
		m.put("Belonging Groups", "Grupos pertenecientes");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"A continuación se muestran algunos criterios comunes. Escriba en el cuadro de búsqueda arriba para ver la lista completa y las combinaciones disponibles.");
		m.put("Below content is restored from an unsaved change. Clear to discard", "El contenido a continuación se restauró de un cambio no guardado. Borrar para descartar");
		m.put("Below information will also be sent", "La información a continuación también será enviada");
		m.put("Binary file.", "Archivo binario.");
		m.put("Bitbucket App Password", "Contraseña de aplicación Bitbucket");
		m.put("Bitbucket Login Name", "Nombre de usuario de Bitbucket");
		m.put("Bitbucket Repositories to Import", "Repositorios de Bitbucket para importar");
		m.put("Bitbucket Workspace", "Espacio de trabajo de Bitbucket");
		m.put("Bitbucket app password should be generated with permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>", 
			"La contraseña de la aplicación Bitbucket debe generarse con permiso <b>account/read</b>, <b>repositories/read</b> y <b>issues:read</b>");
		m.put("Blame", "Culpar");
		m.put("Blob", "Blob");
		m.put("Blob hash", "Hash de blob");
		m.put("Blob index version", "Versión del índice de blob");
		m.put("Blob name", "Nombre de blob");
		m.put("Blob path", "Ruta de blob");
		m.put("Blob primary symbols", "Símbolos primarios de blob");
		m.put("Blob secondary symbols", "Símbolos secundarios de blob");
		m.put("Blob symbol list", "Lista de símbolos de blob");
		m.put("Blob text", "Texto de blob");
		m.put("Blob unknown", "Blob desconocido");
		m.put("Blob upload invalid", "Carga de blob inválida");
		m.put("Blob upload unknown", "Carga de blob desconocida");
		m.put("Board", "Tablero");
		m.put("Board Columns", "Columnas del tablero");
		m.put("Board Spec", "Especificación del tablero");
		m.put("Boards", "Tableros");
		m.put("Body", "Cuerpo");
		m.put("Bold", "Negrita");
		m.put("Both", "Ambos");
		m.put("Bottom", "Inferior");
		m.put("Branch", "Rama");
		m.put("Branch \"{0}\" already exists, please choose a different name", "La rama \"{0}\" ya existe, por favor elija un nombre diferente");
		m.put("Branch \"{0}\" created", "Rama \"{0}\" creada");
		m.put("Branch \"{0}\" deleted", "Rama \"{0}\" eliminada");
		m.put("Branch <a wicket:id=\"targetBranch\"></a> is up to date with all commits from <a wicket:id=\"sourceBranch\"></a>. Try <a wicket:id=\"swapBranches\">swap source and target</a> for the comparison.", 
			"La rama <a wicket:id=\"targetBranch\"></a> está actualizada con todos los commits de <a wicket:id=\"sourceBranch\"></a>. Intente <a wicket:id=\"swapBranches\">intercambiar origen y destino</a> para la comparación.");
		m.put("Branch Choice Bean", "Bean de elección de rama");
		m.put("Branch Name", "Nombre de la rama");
		m.put("Branch Protection", "Protección de rama");
		m.put("Branch Revision", "Revisión de rama");
		m.put("Branch update", "Actualización de rama");
		m.put("Branches", "Ramas");
		m.put("Brand Setting Edit Bean", "Bean de edición de configuración de marca");
		m.put("Branding", "Marca");
		m.put("Branding settings updated", "Configuraciones de marca actualizadas");
		m.put("Browse Code", "Explorar código");
		m.put("Browse code", "Explorar código");
		m.put("Bug Report", "Informe de errores");
		m.put("Build", "Construcción");
		m.put("Build #{0} already finished", "Construcción #{0} ya finalizada");
		m.put("Build #{0} deleted", "Construcción #{0} eliminada");
		m.put("Build #{0} not finished yet", "Construcción #{0} aún no finalizada");
		m.put("Build Artifact Storage", "Almacenamiento de artefactos de construcción");
		m.put("Build Commit", "Commit de construcción");
		m.put("Build Context", "Contexto de construcción");
		m.put("Build Description", "Descripción de construcción");
		m.put("Build Filter", "Filtro de construcción");
		m.put("Build Image", "Imagen de construcción");
		m.put("Build Image (Kaniko)", "Imagen de construcción (Kaniko)");
		m.put("Build Management", "Gestión de construcción");
		m.put("Build Notification", "Notificación de construcción");
		m.put("Build Notification Template", "Plantilla de notificación de construcción");
		m.put("Build Number", "Número de construcción");
		m.put("Build On Behalf Of", "Construcción en nombre de");
		m.put("Build Path", "Ruta de construcción");
		m.put("Build Preservation", "Preservación de construcción");
		m.put("Build Preservations", "Preservaciones de construcción");
		m.put("Build Preservations Bean", "Bean de preservaciones de construcción");
		m.put("Build Preserve Rules", "Reglas de preservación de construcción");
		m.put("Build Provider", "Proveedor de construcción");
		m.put("Build Spec", "Especificación de construcción");
		m.put("Build Statistics", "Estadísticas de construcción");
		m.put("Build Version", "Versión de construcción");
		m.put("Build Volume Storage Class", "Clase de almacenamiento de volumen de construcción");
		m.put("Build Volume Storage Size", "Tamaño de almacenamiento de volumen de construcción");
		m.put("Build administrative permission for all jobs inside a project, including batch operations over multiple builds", 
			"Permiso administrativo de construcción para todos los trabajos dentro de un proyecto, incluidas las operaciones en lote sobre múltiples construcciones");
		m.put("Build docker image with docker buildx. This step can only be executed by server docker executor or remote docker executor, and it uses the buildx builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko step instead", 
			"Construir imagen de Docker con docker buildx. Este paso solo puede ser ejecutado por el ejecutor de Docker del servidor o el ejecutor de Docker remoto, y utiliza el constructor buildx especificado en estos ejecutores para realizar el trabajo. Para construir una imagen con el ejecutor de Kubernetes, utilice el paso kaniko en su lugar");
		m.put("Build docker image with kaniko. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Construir imagen de Docker con kaniko. Este paso necesita ser ejecutado por el ejecutor de Docker del servidor, el ejecutor de Docker remoto o el ejecutor de Kubernetes");
		m.put("Build duration statistics", "Estadísticas de duración de construcción");
		m.put("Build frequency statistics", "Estadísticas de frecuencia de compilación");
		m.put("Build is successful", "La compilación es exitosa");
		m.put("Build list", "Lista de compilaciones");
		m.put("Build not exist or access denied", "La compilación no existe o el acceso está denegado");
		m.put("Build number", "Número de compilación");
		m.put("Build preserve rules saved", "Reglas de preservación de compilación guardadas");
		m.put("Build required for deletion. Submit pull request instead", "Se requiere compilación para la eliminación. Envíe una solicitud de extracción en su lugar");
		m.put("Build required for this change. Please submit pull request instead", "Se requiere una compilación para este cambio. Por favor, envía una solicitud de extracción en su lugar.");
		m.put("Build required for this change. Submit pull request instead", "Se requiere compilación para este cambio. Envíe una solicitud de extracción en su lugar");
		m.put("Build spec not defined", "Especificación de compilación no definida");
		m.put("Build spec not defined (import project: {0}, import revision: {1})", "Especificación de compilación no definida (proyecto de importación: {0}, revisión de importación: {1})");
		m.put("Build spec not found in commit of this build", "Especificación de compilación no encontrada en el commit de esta compilación");
		m.put("Build statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Las estadísticas de compilación son una función empresarial. <a href='https://onedev.io/pricing' target='_blank'>Pruebe gratis</a> durante 30 días");
		m.put("Build version", "Versión de compilación");
		m.put("Build with Persistent Volume", "Compilación con Volumen Persistente");
		m.put("Builds", "Compilaciones");
		m.put("Builds are {0}", "Las compilaciones están {0}");
		m.put("Buildx Builder", "Constructor Buildx");
		m.put("Built In Fields Bean", "Bean de Campos Integrados");
		m.put("Burndown", "Burndown");
		m.put("Burndown chart", "Gráfico de Burndown");
		m.put("Button Image Url", "URL de Imagen del Botón");
		m.put("By Group", "Por Grupo");
		m.put("By User", "Por Usuario");
		m.put("By day", "Por día");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"Por defecto, el código se clona mediante una credencial generada automáticamente, que solo tiene permiso de lectura sobre el proyecto actual. En caso de que el trabajo necesite <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>enviar código al servidor</a>, debe proporcionar una credencial personalizada con los permisos adecuados aquí");
		m.put("By default issues of parent and child projects will also be listed. Use query <code>&quot;Project&quot; is current</code> to show issues only belong to this project", 
			"Por defecto, también se enumerarán los problemas de los proyectos padre e hijo. Use la consulta <code>&quot;Project&quot; is current</code> para mostrar solo los problemas que pertenecen a este proyecto");
		m.put("By month", "Por mes");
		m.put("By week", "Por semana");
		m.put("Bypass Certificate Check", "Omitir Verificación de Certificado");
		m.put("CANCELLED", "CANCELADO");
		m.put("CORS Allowed Origins", "Orígenes Permitidos por CORS");
		m.put("CPD Report", "Informe CPD");
		m.put("CPU", "CPU");
		m.put("CPU Intensive Task Concurrency", "Concurrencia de Tareas Intensivas de CPU");
		m.put("CPU capability in millis. This is normally (CPU cores)*1000", "Capacidad de CPU en milisegundos. Esto normalmente es (núcleos de CPU)*1000");
		m.put("Cache Key", "Clave de Caché");
		m.put("Cache Management", "Gestión de Caché");
		m.put("Cache Paths", "Rutas de Caché");
		m.put("Cache Setting Bean", "Bean de Configuración de Caché");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "La caché se eliminará para ahorrar espacio si no se accede durante este número de días");
		m.put("Calculating merge preview...", "Calculando vista previa de la fusión...");
		m.put("Callback URL", "URL de Callback");
		m.put("Can Be Used By Jobs", "Puede ser usado por trabajos");
		m.put("Can Create Root Projects", "Puede Crear Proyectos Raíz");
		m.put("Can Edit Estimated Time", "Puede Editar Tiempo Estimado");
		m.put("Can not convert root user to service account", "No se puede convertir el usuario root a cuenta de servicio");
		m.put("Can not convert yourself to service account", "No se puede convertir a sí mismo a cuenta de servicio");
		m.put("Can not delete default branch", "No se puede eliminar la rama predeterminada");
		m.put("Can not delete root account", "No se puede eliminar la cuenta raíz");
		m.put("Can not delete yourself", "No se puede eliminar a sí mismo");
		m.put("Can not disable root account", "No se puede deshabilitar la cuenta raíz");
		m.put("Can not disable yourself", "No se puede deshabilitar a sí mismo");
		m.put("Can not find issue board: ", "No se puede encontrar el tablero de problemas:");
		m.put("Can not move project \"{0}\" to be under itself or its descendants", "No se puede mover el proyecto \"{0}\" para que esté bajo sí mismo o sus descendientes");
		m.put("Can not perform this operation now", "No se puede realizar esta operación ahora");
		m.put("Can not reset password for service account or disabled user", "No se puede restablecer la contraseña para la cuenta de servicio o usuario deshabilitado");
		m.put("Can not reset password for user authenticating via external system", "No se puede restablecer la contraseña para el usuario que se autentica a través de un sistema externo");
		m.put("Can not save malformed query", "No se puede guardar una consulta mal formada");
		m.put("Can not use current or descendant project as parent", "No se puede usar el proyecto actual o descendiente como padre");
		m.put("Can only compare with common ancestor when different projects are involved", "Solo se puede comparar con el ancestro común cuando están involucrados diferentes proyectos");
		m.put("Cancel", "Cancelar");
		m.put("Cancel All Queried Builds", "Cancelar Todas las Compilaciones Consultadas");
		m.put("Cancel Selected Builds", "Cancelar Compilaciones Seleccionadas");
		m.put("Cancel invitation", "Cancelar invitación");
		m.put("Cancel request submitted", "Solicitud de cancelación enviada");
		m.put("Cancel this build", "Cancelar esta compilación");
		m.put("Cancelled", "Cancelado");
		m.put("Cancelled By", "Cancelado Por");
		m.put("Case Sensitive", "Sensible a Mayúsculas y Minúsculas");
		m.put("Certificates to Trust", "Certificados de Confianza");
		m.put("Change", "Cambiar");
		m.put("Change Detection Excludes", "Exclusiones de Detección de Cambios");
		m.put("Change My Password", "Cambiar Mi Contraseña");
		m.put("Change To", "Cambiar A");
		m.put("Change already merged", "Cambio ya fusionado");
		m.put("Change not updated yet", "Cambio aún no actualizado");
		m.put("Change property <code>serverUrl</code> in file <code>conf/agent.properties</code> if necessary. The default value is taken from OneDev server url specified in <i>Administration / System Setting</i>", 
			"Cambie la propiedad <code>serverUrl</code> en el archivo <code>conf/agent.properties</code> si es necesario. El valor predeterminado se toma de la URL del servidor OneDev especificada en <i>Administración / Configuración del Sistema</i>");
		m.put("Change to another field", "Cambiar a otro campo");
		m.put("Change to another state", "Cambiar a otro estado");
		m.put("Change to another value", "Cambiar a otro valor");
		m.put("Changes since last review", "Cambios desde la última revisión");
		m.put("Changes since last visit", "Cambios desde la última visita");
		m.put("Changes since this action", "Cambios desde esta acción");
		m.put("Changes since this comment", "Cambios desde este comentario");
		m.put("Channel Notification", "Notificación de Canal");
		m.put("Chart Metadata", "Metadatos del Gráfico");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"Consulte <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">la guía de GitHub</a> sobre cómo generar y usar claves GPG para firmar sus commits");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including instructions on how to run agent as service", 
			"Consulte <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">la gestión de agentes</a> para obtener detalles, incluidas instrucciones sobre cómo ejecutar el agente como servicio");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including list of supported environment variables", 
			"Consulte <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">la gestión de agentes</a> para obtener detalles, incluida la lista de variables de entorno compatibles");
		m.put("Check Commit Message Footer", "Verificar Pie de Mensaje de Commit");
		m.put("Check Incoming Email", "Verificar Correo Electrónico Entrante");
		m.put("Check Issue Integrity", "Verificar Integridad de Problemas");
		m.put("Check Update", "Verificar Actualización");
		m.put("Check Workflow Integrity", "Verificar Integridad del Flujo de Trabajo");
		m.put("Check out to local workspace", "Revisar al espacio de trabajo local");
		m.put("Check this to compare right side with common ancestor of left and right", "Marque esto para comparar el lado derecho con el ancestro común de izquierda y derecha");
		m.put("Check this to enforce two-factor authentication for all users in the system", "Marque esto para aplicar autenticación de dos factores a todos los usuarios del sistema");
		m.put("Check this to enforce two-factor authentication for all users in this group", "Marque esto para aplicar autenticación de dos factores a todos los usuarios de este grupo");
		m.put("Check this to prevent branch creation", "Marque esto para evitar la creación de ramas");
		m.put("Check this to prevent branch deletion", "Marque esto para evitar la eliminación de ramas");
		m.put("Check this to prevent forced push", "Marque esto para evitar el push forzado");
		m.put("Check this to prevent tag creation", "Marque esto para evitar la creación de etiquetas");
		m.put("Check this to prevent tag deletion", "Marque esto para evitar la eliminación de etiquetas");
		m.put("Check this to prevent tag update", "Marque esto para evitar la actualización de etiquetas");
		m.put("Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>. Note this is applicable for non-merge commits", 
			"Marque esto para requerir <a href='https://www.conventionalcommits.org' target='_blank'>commits convencionales</a>. Nota: esto es aplicable para commits que no sean de fusión");
		m.put("Check this to require valid signature of head commit", "Marque esto para requerir una firma válida del commit principal");
		m.put("Check this to retrieve Git LFS files", "Marque esto para recuperar archivos Git LFS");
		m.put("Checkbox", "Casilla de verificación");
		m.put("Checking field values...", "Comprobando valores de los campos...");
		m.put("Checking fields...", "Comprobando campos...");
		m.put("Checking state and field ordinals...", "Comprobando estado y ordinales de los campos...");
		m.put("Checking state...", "Comprobando estado...");
		m.put("Checkout Code", "Revisar Código");
		m.put("Checkout Path", "Ruta de Revisión");
		m.put("Checkout Pull Request Head", "Revisar Cabeza de Solicitud de Extracción");
		m.put("Checkout Pull Request Merge Preview", "Revisar Vista Previa de Fusión de Solicitud de Extracción");
		m.put("Checkstyle Report", "Informe de Checkstyle");
		m.put("Cherry-Pick", "Cherry-Pick");
		m.put("Cherry-picked successfully", "Cherry-Pick realizado con éxito");
		m.put("Child Projects", "Proyectos Hijos");
		m.put("Child Projects Of", "Proyectos Hijos De");
		m.put("Choice Provider", "Proveedor de Opciones");
		m.put("Choose", "Elegir");
		m.put("Choose JIRA project to import issues from", "Elija el proyecto JIRA para importar problemas");
		m.put("Choose Revision", "Elija Revisión");
		m.put("Choose YouTrack project to import issues from", "Elija el proyecto YouTrack para importar problemas");
		m.put("Choose a project...", "Elija un proyecto...");
		m.put("Choose a user...", "Elija un usuario...");
		m.put("Choose branch...", "Elija rama...");
		m.put("Choose branches...", "Elija ramas...");
		m.put("Choose build...", "Elija compilación...");
		m.put("Choose file", "Elija archivo");
		m.put("Choose group...", "Elija grupo...");
		m.put("Choose groups...", "Elija grupos...");
		m.put("Choose issue...", "Elija problema...");
		m.put("Choose issues...", "Elige problemas...");
		m.put("Choose iteration...", "Elija iteración...");
		m.put("Choose iterations...", "Elija iteraciones...");
		m.put("Choose job...", "Elija trabajo...");
		m.put("Choose jobs...", "Elija trabajos...");
		m.put("Choose project", "Elija proyecto");
		m.put("Choose projects...", "Elija proyectos...");
		m.put("Choose pull request...", "Elija solicitud de extracción...");
		m.put("Choose repository", "Elija repositorio");
		m.put("Choose role...", "Elija rol...");
		m.put("Choose roles...", "Elija roles...");
		m.put("Choose users...", "Elija usuarios...");
		m.put("Choose...", "Elija...");
		m.put("Circular build spec imports ({0})", "Importaciones circulares de especificaciones de compilación ({0})");
		m.put("Click to select a commit, or shift-click to select multiple commit", "Haga clic para seleccionar un commit, o shift-clic para seleccionar múltiples commits");
		m.put("Click to show comment of marked text", "Haga clic para mostrar el comentario del texto marcado");
		m.put("Click to show issue details", "Haga clic para mostrar detalles del problema");
		m.put("Client ID of this OneDev instance registered in Google cloud", "ID de cliente de esta instancia de OneDev registrada en Google Cloud");
		m.put("Client Id", "ID de Cliente");
		m.put("Client Secret", "Secreto de Cliente");
		m.put("Client secret of this OneDev instance registered in Google cloud", "Secreto de cliente de esta instancia de OneDev registrada en Google Cloud");
		m.put("Clippy Report", "Informe de Clippy");
		m.put("Clone", "Clonar");
		m.put("Clone Credential", "Credencial de Clonación");
		m.put("Clone Depth", "Profundidad de Clonación");
		m.put("Clone in IntelliJ", "Clonar en IntelliJ");
		m.put("Clone in VSCode", "Clonar en VSCode");
		m.put("Close", "Cerrar");
		m.put("Close Iteration", "Cerrar Iteración");
		m.put("Close this iteration", "Cerrar esta iteración");
		m.put("Closed", "Cerrado");
		m.put("Closed Issue State", "Estado de Problema Cerrado");
		m.put("Closest due date", "Fecha de vencimiento más cercana");
		m.put("Clover Coverage Report", "Informe de Cobertura Clover");
		m.put("Cluster Role", "Rol de Clúster");
		m.put("Cluster Setting", "Configuración de Clúster");
		m.put("Cluster setting", "Configuración de clúster");
		m.put("Clustered Servers", "Servidores Agrupados");
		m.put("Cobertura Coverage Report", "Informe de Cobertura Cobertura");
		m.put("Code", "Código");
		m.put("Code Analysis", "Análisis de Código");
		m.put("Code Analysis Setting", "Configuración de Análisis de Código");
		m.put("Code Analysis Settings", "Configuraciones de Análisis de Código");
		m.put("Code Changes", "Cambios de Código");
		m.put("Code Comment", "Comentario de Código");
		m.put("Code Comment Management", "Gestión de Comentarios de Código");
		m.put("Code Comments", "Comentarios de Código");
		m.put("Code Compare", "Comparación de Código");
		m.put("Code Contribution Statistics", "Estadísticas de Contribución de Código");
		m.put("Code Coverage", "Cobertura de Código");
		m.put("Code Line Statistics", "Estadísticas de Líneas de Código");
		m.put("Code Management", "Gestión de Código");
		m.put("Code Privilege", "Privilegio de Código");
		m.put("Code Problem Statistics", "Estadísticas de Problemas de Código");
		m.put("Code Search", "Búsqueda de Código");
		m.put("Code Statistics", "Estadísticas de Código");
		m.put("Code analysis settings updated", "Configuraciones de análisis de código actualizadas");
		m.put("Code changes since...", "Cambios de código desde...");
		m.put("Code clone or download", "Clonar o descargar código");
		m.put("Code comment", "Comentario de código");
		m.put("Code comment #{0} deleted", "Comentario de código #{0} eliminado");
		m.put("Code comment administrative permission inside a project, including batch operations over multiple code comments", 
			"Permiso administrativo de comentarios de código dentro de un proyecto, incluyendo operaciones en lote sobre múltiples comentarios de código");
		m.put("Code commit", "Commit de código");
		m.put("Code is committed", "El código está comprometido");
		m.put("Code push", "Push de código");
		m.put("Code read permission is required to import build spec (import project: {0}, import revision: {1})", 
			"Se requiere permiso de lectura de código para importar especificaciones de compilación (proyecto de importación: {0}, revisión de importación: {1})");
		m.put("Code suggestion", "Sugerencia de código");
		m.put("Code write permission is required for this operation", "Se requiere permiso de escritura de código para esta operación");
		m.put("Collapse all", "Colapsar todo");
		m.put("Color", "Color");
		m.put("Columns", "Columnas");
		m.put("Command Palette", "Paleta de Comandos");
		m.put("Commands", "Comandos");
		m.put("Comment", "Comentario");
		m.put("Comment Content", "Contenido del Comentario");
		m.put("Comment on File", "Comentar en el Archivo");
		m.put("Comment too long", "Comentario demasiado largo");
		m.put("Commented code is outdated", "El código comentado está desactualizado");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "Comentado en el archivo \"{0}\" en el proyecto \"{1}\"");
		m.put("Commented on issue \"{0}\" ({1})", "Comentado en el problema \"{0}\" ({1})");
		m.put("Commented on pull request \"{0}\" ({1})", "Comentado en la solicitud de extracción \"{0}\" ({1})");
		m.put("Comments", "Comentarios");
		m.put("Commit", "Commit");
		m.put("Commit &amp; Insert", "Commit &amp; Insertar");
		m.put("Commit Batched Suggestions", "Commit de Sugerencias Agrupadas");
		m.put("Commit Message", "Mensaje de Commit");
		m.put("Commit Message Bean", "Bean de Mensaje de Commit");
		m.put("Commit Message Fix Patterns", "Patrones de Corrección de Mensaje de Commit");
		m.put("Commit Message Footer Pattern", "Patrón de Pie de Mensaje de Commit");
		m.put("Commit Notification", "Notificación de Commit");
		m.put("Commit Notification Template", "Plantilla de Notificación de Commit");
		m.put("Commit Scopes", "Ámbitos de Commit");
		m.put("Commit Signature Required", "Firma de Commit Requerida");
		m.put("Commit Suggestion", "Sugerencia de Commit");
		m.put("Commit Types", "Tipos de Commit");
		m.put("Commit Types For Footer Check", "Tipos de Commit para Verificación de Pie");
		m.put("Commit Your Change", "Confirma tu Cambio");
		m.put("Commit date", "Fecha de Commit");
		m.put("Commit hash", "Hash de Commit");
		m.put("Commit history of current path", "Historial de Commit de la ruta actual");
		m.put("Commit index version", "Versión de índice de Commit");
		m.put("Commit message can be used to fix issues by prefixing and suffixing issue number with specified pattern. Each line of the commit message will be matched against each entry defined here to find issues to be fixed", 
			"El mensaje de commit puede usarse para corregir problemas al prefijar y sufijar el número de problema con el patrón especificado. Cada línea del mensaje de commit se comparará con cada entrada definida aquí para encontrar problemas a corregir");
		m.put("Commit not exist or access denied", "El commit no existe o el acceso fue denegado");
		m.put("Commit of the build is missing", "Falta el commit de la compilación");
		m.put("Commit signature required but no GPG signing key specified", "Se requiere firma de commit pero no se especificó una clave de firma GPG");
		m.put("Commit suggestion", "Sugerencia de Commit");
		m.put("Commits", "Commits");
		m.put("Commits are taken from default branch of non-forked repositories", "Los commits se toman de la rama predeterminada de repositorios no bifurcados");
		m.put("Commits generated by OneDev previously will be shown as unverified if this key is deleted. Type <code>yes</code> below if you want to continue.", 
			"Los commits generados previamente por OneDev se mostrarán como no verificados si esta clave se elimina. Escribe <code>yes</code> abajo si deseas continuar.");
		m.put("Commits were merged into target branch", "Los commits fueron fusionados en la rama objetivo");
		m.put("Commits were merged into target branch outside of this pull request", "Los commits fueron fusionados en la rama objetivo fuera de esta solicitud de extracción");
		m.put("Commits were rebased onto target branch", "Los commits fueron rebasados en la rama objetivo");
		m.put("Commits were squashed into a single commit on target branch", "Los commits fueron combinados en un solo commit en la rama objetivo");
		m.put("Committed After", "Commit Después");
		m.put("Committed Before", "Commit Antes");
		m.put("Committed By", "Commit Por");
		m.put("Committer", "Autor del Commit");
		m.put("Compare", "Comparar");
		m.put("Compare with base revision", "Comparar con la revisión base");
		m.put("Compare with this parent", "Comparar con este padre");
		m.put("Concurrency", "Concurrencia");
		m.put("Condition", "Condición");
		m.put("Confidential", "Confidencial");
		m.put("Config File", "Archivo de Configuración");
		m.put("Configuration Discovery Url", "Url de Descubrimiento de Configuración");
		m.put("Configure your scope to use below registry", "Configura tu ámbito para usar el registro de abajo");
		m.put("Confirm Approve", "Confirmar Aprobación");
		m.put("Confirm Delete Source Branch", "Confirmar Eliminar Rama Fuente");
		m.put("Confirm Discard", "Confirmar Descartar");
		m.put("Confirm Reopen", "Confirmar Reabrir");
		m.put("Confirm Request For Changes", "Confirmar Solicitud de Cambios");
		m.put("Confirm Restore Source Branch", "Confirmar Restaurar Rama Fuente");
		m.put("Confirm password here", "Confirma la contraseña aquí");
		m.put("Confirm your action", "Confirma tu acción");
		m.put("Connect New Agent", "Conectar Nuevo Agente");
		m.put("Connect with your SSO account", "Conéctate con tu cuenta SSO");
		m.put("Contact Email", "Correo Electrónico de Contacto");
		m.put("Contact Name", "Nombre de Contacto");
		m.put("Container Image", "Imagen del Contenedor");
		m.put("Container Image(s)", "Imagen(es) del Contenedor");
		m.put("Container default", "Contenedor predeterminado");
		m.put("Content", "Contenido");
		m.put("Content Type", "Tipo de contenido");
		m.put("Content is identical", "El contenido es idéntico");
		m.put("Continue to add other user after create", "Continuar agregando otro usuario después de crear");
		m.put("Contributed settings", "Configuraciones Contribuidas");
		m.put("Contributions", "Contribuciones");
		m.put("Contributions to {0} branch, excluding merge commits", "Contribuciones a la rama {0}, excluyendo commits de fusión");
		m.put("Convert All Queried to Service Accounts", "Convertir Todos los Consultados a Cuentas de Servicio");
		m.put("Convert Selected to Service Accounts", "Convertir Seleccionados a Cuentas de Servicio");
		m.put("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm", 
			"Convertir a cuentas de servicio eliminará la contraseña, direcciones de correo electrónico, todas las asignaciones y seguimientos. Escriba <code>yes</code> para confirmar");
		m.put("Copy", "Copiar");
		m.put("Copy All Queried Issues To...", "Copiar Todos los Problemas Consultados A...");
		m.put("Copy Files with SCP", "Copiar Archivos con SCP");
		m.put("Copy Selected Issues To...", "Copiar Problemas Seleccionados A...");
		m.put("Copy dashboard", "Copiar tablero");
		m.put("Copy issue number and title", "Copiar número y título del problema");
		m.put("Copy public key", "Copiar clave pública");
		m.put("Copy selected text to clipboard", "Copiar texto seleccionado al portapapeles");
		m.put("Copy to clipboard", "Copiar al portapapeles");
		m.put("Count", "Contar");
		m.put("Coverage Statistics", "Estadísticas de Cobertura");
		m.put("Covered", "Cubierto");
		m.put("Covered by tests", "Cubierto por pruebas");
		m.put("Cppcheck Report", "Informe de Cppcheck");
		m.put("Cpu Limit", "Límite de CPU");
		m.put("Cpu Request", "Solicitud de CPU");
		m.put("Create", "Crear");
		m.put("Create Administrator Account", "Crear Cuenta de Administrador");
		m.put("Create Branch", "Crear Rama");
		m.put("Create Branch Bean", "Bean de Crear Rama");
		m.put("Create Branch Bean With Revision", "Bean de Crear Rama con Revisión");
		m.put("Create Child Project", "Crear Proyecto Hijo");
		m.put("Create Child Projects", "Crear Proyectos Hijos");
		m.put("Create Issue", "Crear problema");
		m.put("Create Iteration", "Crear iteración");
		m.put("Create Merge Commit", "Crear commit de fusión");
		m.put("Create Merge Commit If Necessary", "Crear commit de fusión si es necesario");
		m.put("Create New", "Crear nuevo");
		m.put("Create New File", "Crear nuevo archivo");
		m.put("Create New User", "Crear Nuevo Usuario");
		m.put("Create Project", "Crear proyecto");
		m.put("Create Pull Request", "Crear solicitud de extracción");
		m.put("Create Pull Request for This Change", "Crear solicitud de extracción para este cambio");
		m.put("Create Tag", "Crear etiqueta");
		m.put("Create Tag Bean", "Crear bean de etiqueta");
		m.put("Create Tag Bean With Revision", "Crear bean de etiqueta con revisión");
		m.put("Create User", "Crear usuario");
		m.put("Create body", "Crear cuerpo");
		m.put("Create branch <b>{0}</b> from {1}", "Crear rama <b>{0}</b> desde {1}");
		m.put("Create child projects under a project", "Crear proyectos secundarios dentro de un proyecto");
		m.put("Create issue", "Crear problema");
		m.put("Create merge commit", "Crear commit de fusión");
		m.put("Create merge commit if necessary", "Crear commit de fusión si es necesario");
		m.put("Create new issue", "Crear nuevo problema");
		m.put("Create tag", "Crear etiqueta");
		m.put("Create tag <b>{0}</b> from {1}", "Crear etiqueta <b>{0}</b> desde {1}");
		m.put("Created At", "Creado en");
		m.put("Creation of this branch is prohibited per branch protection rule", "La creación de esta rama está prohibida según la regla de protección de ramas");
		m.put("Critical", "Crítico");
		m.put("Critical Severity", "Severidad crítica");
		m.put("Cron Expression", "Expresión Cron");
		m.put("Cron schedule", "Programación Cron");
		m.put("Curl Location", "Ubicación Curl");
		m.put("Current Iteration", "Iteración actual");
		m.put("Current Value", "Valor actual");
		m.put("Current avatar", "Avatar actual");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"El contexto actual es diferente del contexto cuando se agregó este comentario, haga clic para mostrar el contexto del comentario");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"El contexto actual es diferente del contexto cuando se agregó esta respuesta, haga clic para mostrar el contexto de la respuesta");
		m.put("Current context is different from this action, click to show the comment context", "El contexto actual es diferente de esta acción, haga clic para mostrar el contexto del comentario");
		m.put("Current platform", "Plataforma actual");
		m.put("Current project", "Proyecto actual");
		m.put("Custom Linux Shell", "Shell personalizado de Linux");
		m.put("DISCARDED", "DESCARTADO");
		m.put("Dashboard Share Bean", "Bean de compartir tablero");
		m.put("Dashboard name", "Nombre del tablero");
		m.put("Dashboards", "Tableros");
		m.put("Database Backup", "Respaldo de base de datos");
		m.put("Date", "Fecha");
		m.put("Date Time", "Fecha y hora");
		m.put("Days Per Week", "Días por semana");
		m.put("Deactivate Subscription", "Desactivar suscripción");
		m.put("Deactivate Trial Subscription", "Desactivar suscripción de prueba");
		m.put("Default", "Predeterminado");
		m.put("Default (Shell on Linux, Batch on Windows)", "Predeterminado (Shell en Linux, Batch en Windows)");
		m.put("Default Assignees", "Asignados predeterminados");
		m.put("Default Boards", "Tableros predeterminados");
		m.put("Default Fixed Issue Filter", "Filtro de problemas solucionados predeterminado");
		m.put("Default Fixed Issue Filters", "Filtros de problemas solucionados predeterminados");
		m.put("Default Fixed Issue Filters Bean", "Bean de filtros de problemas solucionados predeterminados");
		m.put("Default Group", "Grupo predeterminado");
		m.put("Default Issue Boards", "Tableros de problemas predeterminados");
		m.put("Default Merge Strategy", "Estrategia de fusión predeterminada");
		m.put("Default Multi Value Provider", "Proveedor de valores múltiples predeterminado");
		m.put("Default Project", "Proyecto predeterminado");
		m.put("Default Project Setting", "Configuración predeterminada del proyecto");
		m.put("Default Roles", "Roles Predeterminados");
		m.put("Default Roles Bean", "Bean de Roles Predeterminados");
		m.put("Default Value", "Valor predeterminado");
		m.put("Default Value Provider", "Proveedor de valor predeterminado");
		m.put("Default Values", "Valores predeterminados");
		m.put("Default branch", "Rama predeterminada");
		m.put("Default branding settings restored", "Configuraciones de marca predeterminadas restauradas");
		m.put("Default fixed issue filters saved", "Filtros de problemas solucionados predeterminados guardados");
		m.put("Default merge strategy", "Estrategia de fusión predeterminada");
		m.put("Default roles affect default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"Los roles predeterminados afectan los permisos predeterminados otorgados a todos en el sistema. Los permisos predeterminados reales serán <b class='text-warning'>todos los permisos</b> contenidos en los roles predeterminados de este proyecto y todos sus proyectos padres");
		m.put("Define all custom issue fields here. Each project can decide to use all or a subset of these fields via its issue transition setting. <b class=\"text-warning\">NOTE: </b> Newly defined fields by default only appear in new issues. Batch edit existing issues from issue list page if you want them to have these new fields", 
			"Defina todos los campos personalizados de problemas aquí. Cada proyecto puede decidir usar todos o un subconjunto de estos campos a través de su configuración de transición de problemas. <b class=\"text-warning\">NOTA: </b> Los campos recién definidos por defecto solo aparecen en nuevos problemas. Edite en lote los problemas existentes desde la página de lista de problemas si desea que tengan estos nuevos campos");
		m.put("Define all custom issue states here. The first state will be used as initial state of created issues", 
			"Defina todos los estados personalizados de problemas aquí. El primer estado se usará como estado inicial de los problemas creados");
		m.put("Define branch protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given branch and user, the first matching rule will take effect", 
			"Defina reglas de protección de ramas. Las reglas definidas en el proyecto principal se consideran definidas después de las reglas definidas aquí. Para una rama y usuario determinados, la primera regla coincidente tendrá efecto");
		m.put("Define default issue boards for all projects here. A certain project can override this setting to define its own issue boards.", 
			"Defina tableros de problemas predeterminados para todos los proyectos aquí. Un proyecto determinado puede anular esta configuración para definir sus propios tableros de problemas.");
		m.put("Define how issue states should be transited from one to another, either manually or automatically when some events happen. And the rule can be configured to apply to certain projects and issues via the applicable issues setting", 
			"Defina cómo los estados de problemas deben transitar de uno a otro, ya sea manualmente o automáticamente cuando ocurran algunos eventos. Y la regla se puede configurar para aplicarse a ciertos proyectos y problemas a través de la configuración de problemas aplicables");
		m.put("Define issue templates here. When a new issue is created, the first matching template will be used.", 
			"Defina plantillas de problemas aquí. Cuando se crea un nuevo problema, se usará la primera plantilla coincidente.");
		m.put("Define labels to be assigned to project, build or pull request. For issues, custom fields can be used which is much more powerful than labels", 
			"Defina etiquetas para asignar a proyectos, compilaciones o solicitudes de extracción. Para problemas, se pueden usar campos personalizados que son mucho más poderosos que las etiquetas");
		m.put("Define properties to be used in build spec. Properties will be inherited by child projects, and can be overridden by child properties with same name.", 
			"Defina propiedades para usar en la especificación de compilación. Las propiedades serán heredadas por proyectos secundarios y pueden ser anuladas por propiedades secundarias con el mismo nombre.");
		m.put("Define rules to preserve builds. A build will be preserved as long as one rule defined here or in parent projects preserves it. All builds will be preserved if no rules are defined here and in parent projects", 
			"Defina reglas para preservar compilaciones. Una compilación se preservará mientras una regla definida aquí o en proyectos principales la preserve. Todas las compilaciones se preservarán si no se definen reglas aquí ni en proyectos principales");
		m.put("Define tag protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given tag and user, the first matching rule will take effect", 
			"Defina reglas de protección de etiquetas. Las reglas definidas en el proyecto principal se consideran definidas después de las reglas definidas aquí. Para una etiqueta y usuario determinados, la primera regla coincidente tendrá efecto");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"Retraso para el primer reintento en segundos. El retraso de los reintentos posteriores se calculará utilizando un retroceso exponencial basado en este valor");
		m.put("Delete", "Eliminar");
		m.put("Delete All", "Eliminar todo");
		m.put("Delete All Queried Builds", "Eliminar todas las compilaciones consultadas");
		m.put("Delete All Queried Comments", "Eliminar todos los comentarios consultados");
		m.put("Delete All Queried Issues", "Eliminar todos los problemas consultados");
		m.put("Delete All Queried Packages", "Eliminar todos los paquetes consultados");
		m.put("Delete All Queried Projects", "Eliminar todos los proyectos consultados");
		m.put("Delete All Queried Pull Requests", "Eliminar todas las solicitudes de extracción consultadas");
		m.put("Delete All Queried Users", "Eliminar todos los usuarios consultados");
		m.put("Delete Build", "Eliminar compilación");
		m.put("Delete Comment", "Eliminar comentario");
		m.put("Delete Pull Request", "Eliminar solicitud de extracción");
		m.put("Delete SSO account here to reconnect corresponding SSO subject upon next login. Note that SSO subject with verified email will be connected to user with same verified email automatically", 
			"Elimina la cuenta SSO aquí para reconectar el sujeto SSO correspondiente en el próximo inicio de sesión. Ten en cuenta que el sujeto SSO con correo electrónico verificado se conectará automáticamente al usuario con el mismo correo electrónico verificado");
		m.put("Delete Selected", "Eliminar seleccionados");
		m.put("Delete Selected Builds", "Eliminar compilaciones seleccionadas");
		m.put("Delete Selected Comments", "Eliminar comentarios seleccionados");
		m.put("Delete Selected Issues", "Eliminar problemas seleccionados");
		m.put("Delete Selected Packages", "Eliminar paquetes seleccionados");
		m.put("Delete Selected Projects", "Eliminar proyectos seleccionados");
		m.put("Delete Selected Pull Requests", "Eliminar solicitudes de extracción seleccionadas");
		m.put("Delete Selected Users", "Eliminar usuarios seleccionados");
		m.put("Delete Source Branch", "Eliminar rama fuente");
		m.put("Delete Source Branch After Merge", "Eliminar rama fuente después de la fusión");
		m.put("Delete dashboard", "Eliminar tablero");
		m.put("Delete from branch {0}", "Eliminar desde la rama {0}");
		m.put("Delete this", "Eliminar esto");
		m.put("Delete this GPG key", "Eliminar esta clave GPG");
		m.put("Delete this access token", "Eliminar este token de acceso");
		m.put("Delete this branch", "Eliminar esta rama");
		m.put("Delete this executor", "Eliminar este ejecutor");
		m.put("Delete this field", "Eliminar este campo");
		m.put("Delete this import", "Eliminar esta importación");
		m.put("Delete this iteration", "Eliminar esta iteración");
		m.put("Delete this key", "Eliminar esta clave");
		m.put("Delete this link", "Eliminar este enlace");
		m.put("Delete this rule", "Eliminar esta regla");
		m.put("Delete this secret", "Eliminar este secreto");
		m.put("Delete this state", "Eliminar este estado");
		m.put("Delete this tag", "Eliminar esta etiqueta");
		m.put("Delete this value", "Eliminar este valor");
		m.put("Deleted source branch", "Rama fuente eliminada");
		m.put("Deletion not allowed due to branch protection rule", "Eliminación no permitida debido a la regla de protección de ramas");
		m.put("Deletion not allowed due to tag protection rule", "Eliminación no permitida debido a la regla de protección de etiquetas");
		m.put("Deletions", "Eliminaciones");
		m.put("Denied", "Denegado");
		m.put("Dependencies & Services", "Dependencias y servicios");
		m.put("Dependency Management", "Gestión de dependencias");
		m.put("Dependency job finished", "Trabajo de dependencia finalizado");
		m.put("Dependent Fields", "Campos dependientes");
		m.put("Depends on <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>", "Depende de <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>");
		m.put("Descending", "Descendente");
		m.put("Description", "Descripción");
		m.put("Description Template", "Plantilla de descripción");
		m.put("Description Templates", "Plantillas de descripción");
		m.put("Description too long", "Descripción demasiado larga");
		m.put("Destination Path", "Ruta de destino");
		m.put("Destinations", "Destinos");
		m.put("Detect Licenses", "Detectar licencias");
		m.put("Detect Secrets", "Detectar secretos");
		m.put("Detect Vulnerabilities", "Detectar vulnerabilidades");
		m.put("Diff is too large to be displayed.", "La diferencia es demasiado grande para mostrarse.");
		m.put("Diff options", "Opciones de diferencia");
		m.put("Digest", "Resumen");
		m.put("Digest invalid", "Resumen inválido");
		m.put("Directories to Skip", "Directorios para omitir");
		m.put("Directory", "Directorio");
		m.put("Directory (tenant) ID", "ID de directorio (tenant)");
		m.put("Disable", "Deshabilitar");
		m.put("Disable All Queried Users", "Deshabilitar todos los usuarios consultados");
		m.put("Disable Auto Update Check", "Deshabilitar verificación de actualización automática");
		m.put("Disable Dashboard", "Deshabilitar tablero");
		m.put("Disable Selected Users", "Deshabilitar usuarios seleccionados");
		m.put("Disabled", "Deshabilitado");
		m.put("Disabled users and service accounts are excluded from user-month calculation", "Los usuarios deshabilitados y las cuentas de servicio están excluidos del cálculo de usuario-mes");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"Deshabilitar la cuenta restablecerá la contraseña, eliminará los tokens de acceso y eliminará todas las referencias de otras entidades excepto las actividades pasadas. ¿Realmente desea continuar?");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"Deshabilitar cuentas restablecerá la contraseña, eliminará los tokens de acceso y eliminará todas las referencias de otras entidades excepto las actividades pasadas. Escriba <code>yes</code> para confirmar");
		m.put("Disallowed File Types", "Tipos de archivos no permitidos");
		m.put("Disallowed file type(s): {0}", "Tipo(s) de archivo no permitido(s): {0}");
		m.put("Discard", "Descartar");
		m.put("Discard All Queried Pull Requests", "Descartar todas las solicitudes de extracción consultadas");
		m.put("Discard Selected Pull Requests", "Descartar solicitudes de extracción seleccionadas");
		m.put("Discarded", "Descartado");
		m.put("Discarded pull request \"{0}\" ({1})", "Solicitud de extracción descartada \"{0}\" ({1})");
		m.put("Discord", "Discord");
		m.put("Discord Notifications", "Notificaciones de Discord");
		m.put("Display Fields", "Mostrar campos");
		m.put("Display Links", "Mostrar enlaces");
		m.put("Display Months", "Mostrar meses");
		m.put("Display Params", "Mostrar parámetros");
		m.put("Do Not Retrieve Groups", "No recuperar grupos");
		m.put("Do not ignore", "No ignorar");
		m.put("Do not ignore whitespace", "No ignorar espacios en blanco");
		m.put("Do not retrieve", "No recuperar");
		m.put("Do not retrieve groups", "No recuperar grupos");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "¿Realmente desea cancelar la invitación a \"{0}\"?");
		m.put("Do you really want to cancel this build?", "¿Realmente desea cancelar esta compilación?");
		m.put("Do you really want to change target branch to {0}?", "¿Realmente desea cambiar la rama de destino a {0}?");
		m.put("Do you really want to delete \"{0}\"?", "¿Realmente desea eliminar \"{0}\"?");
		m.put("Do you really want to delete SSO provider \"{0}\"?", "¿Realmente deseas eliminar el proveedor SSO \"{0}\"?");
		m.put("Do you really want to delete board \"{0}\"?", "¿Realmente desea eliminar el tablero \"{0}\"?");
		m.put("Do you really want to delete build #{0}?", "¿Realmente desea eliminar la compilación #{0}?");
		m.put("Do you really want to delete group \"{0}\"?", "¿Realmente desea eliminar el grupo \"{0}\"?");
		m.put("Do you really want to delete iteration \"{0}\"?", "¿Realmente desea eliminar la iteración \"{0}\"?");
		m.put("Do you really want to delete job secret \"{0}\"?", "¿Realmente desea eliminar el secreto del trabajo \"{0}\"?");
		m.put("Do you really want to delete pull request #{0}?", "¿Realmente desea eliminar la solicitud de extracción #{0}?");
		m.put("Do you really want to delete role \"{0}\"?", "¿Realmente desea eliminar el rol \"{0}\"?");
		m.put("Do you really want to delete selected query watches?", "¿Realmente desea eliminar las consultas seleccionadas?");
		m.put("Do you really want to delete tag {0}?", "¿Realmente desea eliminar la etiqueta {0}?");
		m.put("Do you really want to delete this GPG key?", "¿Realmente desea eliminar esta clave GPG?");
		m.put("Do you really want to delete this SSH key?", "¿Realmente desea eliminar esta clave SSH?");
		m.put("Do you really want to delete this SSO account?", "¿Realmente deseas eliminar esta cuenta SSO?");
		m.put("Do you really want to delete this access token?", "¿Realmente desea eliminar este token de acceso?");
		m.put("Do you really want to delete this board?", "¿Realmente desea eliminar este tablero?");
		m.put("Do you really want to delete this build?", "¿Realmente desea eliminar esta compilación?");
		m.put("Do you really want to delete this code comment and all its replies?", "¿Realmente desea eliminar este comentario de código y todas sus respuestas?");
		m.put("Do you really want to delete this code comment?", "¿Realmente desea eliminar este comentario de código?");
		m.put("Do you really want to delete this directory?", "¿Realmente desea eliminar este directorio?");
		m.put("Do you really want to delete this email address?", "¿Realmente desea eliminar esta dirección de correo electrónico?");
		m.put("Do you really want to delete this executor?", "¿Realmente desea eliminar este ejecutor?");
		m.put("Do you really want to delete this field?", "¿Realmente desea eliminar este campo?");
		m.put("Do you really want to delete this file?", "¿Realmente desea eliminar este archivo?");
		m.put("Do you really want to delete this issue?", "¿Realmente desea eliminar este problema?");
		m.put("Do you really want to delete this link?", "¿Realmente desea eliminar este enlace?");
		m.put("Do you really want to delete this package?", "¿Realmente desea eliminar este paquete?");
		m.put("Do you really want to delete this privilege?", "¿Realmente desea eliminar este privilegio?");
		m.put("Do you really want to delete this protection?", "¿Realmente desea eliminar esta protección?");
		m.put("Do you really want to delete this pull request?", "¿Realmente desea eliminar esta solicitud de extracción?");
		m.put("Do you really want to delete this reply?", "¿Realmente desea eliminar esta respuesta?");
		m.put("Do you really want to delete this script?", "¿Realmente desea eliminar este script?");
		m.put("Do you really want to delete this state?", "¿Realmente desea eliminar este estado?");
		m.put("Do you really want to delete this template?", "¿Realmente desea eliminar esta plantilla?");
		m.put("Do you really want to delete this transition?", "¿Realmente desea eliminar esta transición?");
		m.put("Do you really want to delete timesheet \"{0}\"?", "¿Realmente desea eliminar la hoja de tiempo \"{0}\"?");
		m.put("Do you really want to delete unused tokens?", "¿Realmente desea eliminar los tokens no utilizados?");
		m.put("Do you really want to discard batched suggestions?", "¿Realmente desea descartar las sugerencias agrupadas?");
		m.put("Do you really want to enable this account?", "¿Realmente desea habilitar esta cuenta?");
		m.put("Do you really want to rebuild?", "¿Realmente desea reconstruir?");
		m.put("Do you really want to remove assignee \"{0}\"?", "¿Realmente desea eliminar al asignado \"{0}\"?");
		m.put("Do you really want to remove password of this user?", "¿Realmente desea eliminar la contraseña de este usuario?");
		m.put("Do you really want to remove the issue from iteration \"{0}\"?", "¿Realmente desea eliminar el problema de la iteración \"{0}\"?");
		m.put("Do you really want to remove this account?", "¿Realmente desea eliminar esta cuenta?");
		m.put("Do you really want to remove this agent?", "¿Realmente desea eliminar este agente?");
		m.put("Do you really want to remove this link?", "¿Realmente desea eliminar este enlace?");
		m.put("Do you really want to restart this agent?", "¿Realmente desea reiniciar este agente?");
		m.put("Do you really want to unauthorize user \"{0}\"?", "¿Realmente desea desautorizar al usuario \"{0}\"?");
		m.put("Do you really want to use default template?", "¿Realmente desea usar la plantilla predeterminada?");
		m.put("Docker", "Docker");
		m.put("Docker Executable", "Ejecutable de Docker");
		m.put("Docker Hub", "Docker Hub");
		m.put("Docker Image", "Imagen de Docker");
		m.put("Docker Sock Path", "Ruta del socket de Docker");
		m.put("Dockerfile", "Dockerfile");
		m.put("Documentation", "Documentación");
		m.put("Don't have an account yet?", "¿Aún no tiene una cuenta?");
		m.put("Download", "Descargar");
		m.put("Download <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> or <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. A new agent token will be included in the package", 
			"Descargue <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> o <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. Se incluirá un nuevo token de agente en el paquete");
		m.put("Download archive of this branch", "Descargar archivo de esta rama");
		m.put("Download full log", "Descargar registro completo");
		m.put("Download log", "Descargar registro");
		m.put("Download patch", "Descargar parche");
		m.put("Download tag archive", "Descargar archivo de etiqueta");
		m.put("Dry Run", "Prueba en seco");
		m.put("Due Date", "Fecha de vencimiento");
		m.put("Due Date Issue Field", "Campo de problema de fecha de vencimiento");
		m.put("Due date", "Fecha de vencimiento");
		m.put("Duplicate authorizations found: ", "Autorizaciones duplicadas encontradas:");
		m.put("Duplicate authorizations found: {0}", "Autorizaciones duplicadas encontradas: {0}");
		m.put("Duration", "Duración");
		m.put("Durations", "Duraciones");
		m.put("ESLint Report", "Informe de ESLint");
		m.put("Edit", "Editar");
		m.put("Edit <code>$HOME/.gem/credentials</code> to add a source", "Edite <code>$HOME/.gem/credentials</code> para agregar una fuente");
		m.put("Edit <code>$HOME/.pypirc</code> to add a package repository like below", "Edite <code>$HOME/.pypirc</code> para agregar un repositorio de paquetes como el siguiente");
		m.put("Edit Avatar", "Editar avatar");
		m.put("Edit Estimated Time", "Editar tiempo estimado");
		m.put("Edit Executor", "Editar ejecutor");
		m.put("Edit Iteration", "Editar iteración");
		m.put("Edit Job Secret", "Editar secreto de trabajo");
		m.put("Edit My Avatar", "Editar mi avatar");
		m.put("Edit Rule", "Editar regla");
		m.put("Edit Timesheet", "Editar hoja de tiempo");
		m.put("Edit dashboard", "Editar tablero");
		m.put("Edit issue title", "Editar título del problema");
		m.put("Edit job", "Editar trabajo");
		m.put("Edit on branch {0}", "Editar en la rama {0}");
		m.put("Edit on source branch", "Editar en la rama fuente");
		m.put("Edit plain", "Editar texto plano");
		m.put("Edit saved queries", "Editar consultas guardadas");
		m.put("Edit this access token", "Editar este token de acceso");
		m.put("Edit this executor", "Editar este ejecutor");
		m.put("Edit this iteration", "Editar esta iteración");
		m.put("Edit this rule", "Editar esta regla");
		m.put("Edit this secret", "Editar este secreto");
		m.put("Edit this state", "Editar este estado");
		m.put("Edit title", "Editar título");
		m.put("Edit with AI", "Editar con IA");
		m.put("Edit {0}", "Editar {0}");
		m.put("Editable Issue Fields", "Campos editables de problemas");
		m.put("Editable Issue Links", "Enlaces editables de problemas");
		m.put("Edited by {0} {1}", "Editado por {0} {1}");
		m.put("Editor", "Editor");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "La rama objetivo o la rama fuente tienen nuevos commits justo ahora, por favor vuelva a verificar.");
		m.put("Email", "Correo electrónico");
		m.put("Email Address", "Dirección de correo electrónico");
		m.put("Email Address Verification", "Verificación de dirección de correo electrónico");
		m.put("Email Addresses", "Direcciones de correo electrónico");
		m.put("Email Templates", "Plantillas de correo electrónico");
		m.put("Email Verification", "Verificación de correo electrónico");
		m.put("Email Verification Template", "Plantilla de verificación de correo electrónico");
		m.put("Email address", "Dirección de correo electrónico");
		m.put("Email address \"{0}\" already used by another account", "La dirección de correo electrónico \"{0}\" ya está utilizada por otra cuenta");
		m.put("Email address \"{0}\" used by account \"{1}\"", "La dirección de correo electrónico \"{0}\" es utilizada por la cuenta \"{1}\"");
		m.put("Email address \"{0}\" used by disabled account \"{1}\"", "La dirección de correo electrónico \"{0}\" es utilizada por la cuenta deshabilitada \"{1}\"");
		m.put("Email address already in use: {0}", "Dirección de correo electrónico ya en uso: {0}");
		m.put("Email address already invited: {0}", "Dirección de correo electrónico ya invitada: {0}");
		m.put("Email address already used by another user", "Dirección de correo electrónico ya utilizada por otro usuario");
		m.put("Email address already used: ", "Dirección de correo electrónico ya utilizada:");
		m.put("Email address to verify", "Dirección de correo electrónico para verificar");
		m.put("Email addresses with <span class=\"badge badge-warning badge-sm\">ineffective</span> mark are those not belong to or not verified by key owner", 
			"Las direcciones de correo electrónico con <span class=\"badge badge-warning badge-sm\">inefectivo</span> son aquellas que no pertenecen o no han sido verificadas por el propietario de la clave");
		m.put("Email templates", "Plantillas de correo electrónico");
		m.put("Empty file added.", "Archivo vacío agregado.");
		m.put("Empty file removed.", "Archivo vacío eliminado.");
		m.put("Enable", "Habilitar");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets", 
			"Habilite <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>seguimiento de tiempo</a> para este proyecto para rastrear el progreso y generar hojas de tiempo");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project", 
			"Habilitar <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>gestión de paquetes</a> para este proyecto");
		m.put("Enable Account Self Removal", "Habilitar Autoeliminación de Cuenta");
		m.put("Enable Account Self Sign-Up", "Habilitar Registro Automático de Cuenta");
		m.put("Enable All Queried Users", "Habilitar Todos los Usuarios Consultados");
		m.put("Enable Anonymous Access", "Habilitar Acceso Anónimo");
		m.put("Enable Auto Backup", "Habilitar Copia de Seguridad Automática");
		m.put("Enable Html Report Publish", "Habilitar la publicación del informe Html");
		m.put("Enable Selected Users", "Habilitar Usuarios Seleccionados");
		m.put("Enable Site Publish", "Habilitar la publicación del sitio");
		m.put("Enable TTY Mode", "Habilitar Modo TTY");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "Habilitar soporte de compilación mediante <a wicket:id=\"addFile\" class=\"link-primary\"></a>");
		m.put("Enable if visibility of this field depends on other fields", "Habilitar si la visibilidad de este campo depende de otros campos");
		m.put("Enable if visibility of this param depends on other params", "Habilitar si la visibilidad de este parámetro depende de otros parámetros");
		m.put("Enable this if the access token has same permissions as the owner", "Habilitar esto si el token de acceso tiene los mismos permisos que el propietario");
		m.put("Enable this option to merge the pull request automatically when ready (all reviewers approved, all required jobs passed etc.)", 
			"Habilitar esta opción para fusionar automáticamente la solicitud de extracción cuando esté lista (todos los revisores aprobaron, todos los trabajos requeridos pasaron, etc.)");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Habilite esto para permitir ejecutar el paso de publicación del informe html. Para evitar ataques XSS, asegúrese de que este ejecutor solo pueda ser utilizado por trabajos confiables");
		m.put("Enable this to allow to run site publish step. OneDev will serve project site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Habilitar esto para permitir ejecutar el paso de publicación del sitio. OneDev servirá los archivos del sitio del proyecto tal como están. Para evitar ataques XSS, asegúrese de que este ejecutor solo pueda ser utilizado por trabajos confiables");
		m.put("Enable this to place intermediate files required by job execution on dynamically allocated persistent volume instead of emptyDir", 
			"Habilitar esto para colocar archivos intermedios requeridos por la ejecución del trabajo en un volumen persistente asignado dinámicamente en lugar de emptyDir");
		m.put("Enable this to process issue or pull request comments posted via email", "Habilitar esto para procesar comentarios de problemas o solicitudes de extracción publicados por correo electrónico");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Habilitar esto para procesar comentarios de problemas o solicitudes de extracción publicados por correo electrónico. <b class='text-danger'>NOTA:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>El subdireccionamiento</a> debe estar habilitado para la dirección de correo electrónico del sistema anterior, ya que OneDev lo utiliza para rastrear contextos de problemas y solicitudes de extracción");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Habilitar esto para procesar comentarios de problemas o solicitudes de extracción publicados por correo electrónico. <b class='text-danger'>NOTA:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>El subdireccionamiento</a> debe estar habilitado para la dirección de correo electrónico del sistema anterior, ya que OneDev lo utiliza para rastrear contextos de problemas y solicitudes de extracción");
		m.put("Enable to allow to upload build cache generated during CI/CD job. Uploaded cache can be used by subsequent builds of the project as long as cache key matches", 
			"Habilitar para permitir cargar la caché de compilación generada durante el trabajo de CI/CD. La caché cargada puede ser utilizada por compilaciones posteriores del proyecto siempre que coincida la clave de caché");
		m.put("End Point", "Punto final");
		m.put("Enforce Conventional Commits", "Aplicar Commits Convencionales");
		m.put("Enforce Password Policy", "Aplicar Política de Contraseña");
		m.put("Enforce Two-factor Authentication", "Aplicar Autenticación de Dos Factores");
		m.put("Enforce password policy for new users", "Aplicar política de contraseña para nuevos usuarios");
		m.put("Enter New Password", "Ingrese nueva contraseña");
		m.put("Enter description here", "Ingrese la descripción aquí");
		m.put("Enter your details to login to your account", "Ingrese sus datos para iniciar sesión en su cuenta");
		m.put("Enter your user name or email to reset password", "Ingrese su nombre de usuario o correo electrónico para restablecer la contraseña");
		m.put("Entries", "Entradas");
		m.put("Entry", "Entrada");
		m.put("Enumeration", "Enumeración");
		m.put("Env Var", "Var de Entorno");
		m.put("Environment Variables", "Variables de Entorno");
		m.put("Environment variable <code>serverUrl</code> in above command is taken from OneDev server url specified in <i>Administration / System Setting</i>. Change it if necessary", 
			"La variable de entorno <code>serverUrl</code> en el comando anterior se toma de la URL del servidor OneDev especificada en <i>Administración / Configuración del Sistema</i>. Cámbiela si es necesario");
		m.put("Equal", "Igual");
		m.put("Error authenticating user", "Error al autenticar al usuario");
		m.put("Error calculating commits: check log for details", "Error al calcular commits: revise el registro para más detalles");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "Error al aplicar cherry-pick a {0}: Se detectaron conflictos de fusión");
		m.put("Error cherry-picking to {0}: {1}", "Error al aplicar cherry-pick a {0}: {1}");
		m.put("Error detail of content type &quot;text/plain&quot;", "Detalle de error del tipo de contenido &quot;text/plain&quot;");
		m.put("Error discovering OIDC metadata", "Error al descubrir metadatos OIDC");
		m.put("Error executing task", "Error al ejecutar la tarea");
		m.put("Error parsing %sbase query: ", "Error al analizar la consulta base %s:");
		m.put("Error parsing %squery: ", "Error al analizar la consulta %s:");
		m.put("Error parsing build spec", "Error al analizar la especificación de compilación");
		m.put("Error rendering widget, check server log for details", "Error al renderizar el widget, revise el registro del servidor para más detalles");
		m.put("Error reverting on {0}: Merge conflicts detected", "Error al revertir en {0}: Se detectaron conflictos de fusión");
		m.put("Error reverting on {0}: {1}", "Error al revertir en {0}: {1}");
		m.put("Error validating auto merge commit message: {0}", "Error al validar el mensaje de commit de fusión automática: {0}");
		m.put("Error validating build spec (location: {0}, error message: {1})", "Error al validar la especificación de compilación (ubicación: {0}, mensaje de error: {1})");
		m.put("Error validating build spec: {0}", "Error al validar la especificación de compilación: {0}");
		m.put("Error validating commit message of \"{0}\": {1}", "Error al validar el mensaje de commit de \"{0}\": {1}");
		m.put("Error validating commit message of <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}", 
			"Error al validar el mensaje de commit de <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}");
		m.put("Error verifying GPG signature", "Error al verificar la firma GPG");
		m.put("Estimated Time", "Tiempo Estimado");
		m.put("Estimated Time Edit Bean", "Bean de Edición de Tiempo Estimado");
		m.put("Estimated Time Issue Field", "Campo de Problema de Tiempo Estimado");
		m.put("Estimated Time:", "Tiempo Estimado:");
		m.put("Estimated time", "Tiempo estimado");
		m.put("Estimated/Spent time. Click for details", "Tiempo estimado/gastado. Haga clic para más detalles");
		m.put("Evaluate script to get choices", "Evaluar script para obtener opciones");
		m.put("Evaluate script to get default value", "Evaluar script para obtener valor predeterminado");
		m.put("Evaluate script to get value or secret", "Evaluar script para obtener valor o secreto");
		m.put("Evaluate script to get values or secrets", "Evaluar script para obtener valores o secretos");
		m.put("Event Types", "Tipos de Eventos");
		m.put("Events", "Eventos");
		m.put("Ever Used Since", "Usado Alguna Vez Desde");
		m.put("Everything inside this project and all child projects will be deleted and can not be recovered, please type project path <code>{0}</code> below to confirm deletion.", 
			"Todo dentro de este proyecto y todos los proyectos secundarios serán eliminados y no podrán ser recuperados, por favor escriba la ruta del proyecto <code>{0}</code> abajo para confirmar la eliminación.");
		m.put("Example", "Ejemplo");
		m.put("Example Plugin Setting", "Configuración de Plugin de Ejemplo");
		m.put("Example Property", "Propiedad de Ejemplo");
		m.put("Exclude Param Combos", "Excluir Combinaciones de Parámetros");
		m.put("Exclude States", "Excluir Estados");
		m.put("Excluded", "Excluido");
		m.put("Excluded Fields", "Campos Excluidos");
		m.put("Executable", "Ejecutable");
		m.put("Execute Commands", "Ejecutar Comandos");
		m.put("Execute Commands via SSH", "Ejecutar Comandos vía SSH");
		m.put("Exit Impersonation", "Salir de la Suplantación");
		m.put("Exited impersonation", "Suplantación terminada");
		m.put("Expand all", "Expandir todo");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", 
			"Se espera uno o más <tt>&lt;número&gt;(h|m)</tt>. Por ejemplo, <tt>1h 1m</tt> representa 1 hora y 1 minuto");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", 
			"Se espera uno o más <tt>&lt;número&gt;(w|d|h|m)</tt>. Por ejemplo, <tt>1w 1d 1h 1m</tt> representa 1 semana ({0} días), 1 día ({1} horas), 1 hora y 1 minuto");
		m.put("Expiration Date:", "Fecha de Expiración:");
		m.put("Expire Date", "Fecha de Expiración");
		m.put("Expired", "Expirado");
		m.put("Explicit SSL (StartTLS)", "SSL Explícito (StartTLS)");
		m.put("Export", "Exportar");
		m.put("Export All Queried Issues To...", "Exportar Todos los Problemas Consultados A...");
		m.put("Export CSV", "Exportar CSV");
		m.put("Export XLSX", "Exportar XLSX");
		m.put("Export as OCI layout", "Exportar como diseño OCI");
		m.put("Extend Trial Subscription", "Extender Suscripción de Prueba");
		m.put("External Authentication", "Autenticación Externa");
		m.put("External Issue Transformers", "Transformadores de Problemas Externos");
		m.put("External Participants", "Participantes Externos");
		m.put("External Password Authenticator", "Autenticador de Contraseña Externa");
		m.put("External System", "Sistema Externo");
		m.put("External authenticator settings saved", "Configuraciones del autenticador externo guardadas");
		m.put("External participants do not have accounts and involve in the issue via email", "Los participantes externos no tienen cuentas y participan en el problema vía correo electrónico");
		m.put("Extract the package into a folder. <b class=\"text-danger\">Warning:</b> On Mac OS X, do not extract to Mac managed folders such as Downloads, Desktop, Documents; otherwise you may encounter permission issues starting agent", 
			"Extraiga el paquete en una carpeta. <b class=\"text-danger\">Advertencia:</b> En Mac OS X, no extraiga a carpetas administradas por Mac como Descargas, Escritorio, Documentos; de lo contrario, puede encontrar problemas de permisos al iniciar el agente");
		m.put("FAILED", "FALLADO");
		m.put("Fail Threshold", "Umbral de Falla");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"Fallar la compilación si hay vulnerabilidades con un nivel de gravedad especificado o mayor");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"Fallar la compilación si hay vulnerabilidades con un nivel de gravedad especificado o mayor. Tenga en cuenta que esto solo tiene efecto si la compilación no falla por otros pasos");
		m.put("Failed", "Fallido");
		m.put("Failed to validate build spec import. Check server log for details", "Error al validar la importación de la especificación de compilación. Revise el registro del servidor para más detalles");
		m.put("Failed to verify your email address", "No se pudo verificar su dirección de correo electrónico");
		m.put("Field Bean", "Bean de Campo");
		m.put("Field Instance", "Instancia de Campo");
		m.put("Field Name", "Nombre del Campo");
		m.put("Field Spec", "Especificación del Campo");
		m.put("Field Specs", "Especificaciones del Campo");
		m.put("Field Value", "Valor del Campo");
		m.put("Fields", "Campos");
		m.put("Fields & Links", "Campos y Enlaces");
		m.put("Fields And Links Bean", "Campos y Enlaces Bean");
		m.put("Fields to Change", "Campos a Cambiar");
		m.put("File", "Archivo");
		m.put("File Changes", "Cambios en el Archivo");
		m.put("File Name", "Nombre del Archivo");
		m.put("File Name Patterns (separated by comma)", "Patrones de Nombre de Archivo (separados por coma)");
		m.put("File Path", "Ruta del Archivo");
		m.put("File Patterns", "Patrones de Archivo");
		m.put("File Protection", "Protección de Archivo");
		m.put("File Protections", "Protecciones de Archivo");
		m.put("File and Symbol Search", "Búsqueda de Archivos y Símbolos");
		m.put("File changes", "Cambios en el Archivo");
		m.put("File is too large to edit here", "El archivo es demasiado grande para editar aquí");
		m.put("File missing or obsolete", "Archivo faltante u obsoleto");
		m.put("File name", "Nombre del archivo");
		m.put("File name patterns such as *.java, *.c", "Patrones de nombre de archivo como *.java, *.c");
		m.put("Files", "Archivos");
		m.put("Files to Be Analyzed", "Archivos a Analizar");
		m.put("Filter", "Filtro");
		m.put("Filter Issues", "Filtrar Problemas");
		m.put("Filter actions", "Filtrar acciones");
		m.put("Filter backlog issues", "Filtrar problemas pendientes");
		m.put("Filter branches...", "Filtrar ramas...");
		m.put("Filter by name", "Filtrar por nombre");
		m.put("Filter by name or email address", "Filtrar por nombre o dirección de correo electrónico");
		m.put("Filter by name...", "Filtrar por nombre...");
		m.put("Filter by path", "Filtrar por ruta");
		m.put("Filter by test suite", "Filtrar por suite de pruebas");
		m.put("Filter date range", "Filtrar rango de fechas");
		m.put("Filter files...", "Filtrar archivos...");
		m.put("Filter groups...", "Filtrar grupos...");
		m.put("Filter issues", "Filtrar problemas");
		m.put("Filter pull requests", "Filtrar solicitudes de extracción");
		m.put("Filter roles", "Filtrar roles");
		m.put("Filter tags...", "Filtrar etiquetas...");
		m.put("Filter targets", "Filtrar objetivos");
		m.put("Filter users", "Filtrar usuarios");
		m.put("Filter...", "Filtrar...");
		m.put("Filters", "Filtros");
		m.put("Find branch", "Buscar rama");
		m.put("Find or create branch", "Buscar o crear rama");
		m.put("Find or create tag", "Buscar o crear etiqueta");
		m.put("Find tag", "Buscar etiqueta");
		m.put("Fingerprint", "Huella Digital");
		m.put("Finish", "Finalizar");
		m.put("First applicable executor", "Primer ejecutor aplicable");
		m.put("Fix", "Corregir");
		m.put("Fix Type", "Tipo de Corrección");
		m.put("Fix Undefined Field Values", "Corregir Valores de Campos Indefinidos");
		m.put("Fix Undefined Fields", "Corregir Campos Indefinidos");
		m.put("Fix Undefined States", "Corregir Estados Indefinidos");
		m.put("Fixed Issues", "Problemas Corregidos");
		m.put("Fixed issues since...", "Problemas corregidos desde...");
		m.put("Fixing Builds", "Corrigiendo Construcciones");
		m.put("Fixing Commits", "Corrigiendo Confirmaciones");
		m.put("Fixing...", "Corrigiendo...");
		m.put("Float", "Flotante");
		m.put("Follow below instructions to publish packages into this project", "Siga las instrucciones a continuación para publicar paquetes en este proyecto");
		m.put("Follow below steps to install agent on remote machine (supports Linux/Windows/Mac OS X/FreeBSD):", 
			"Siga los pasos a continuación para instalar el agente en una máquina remota (compatible con Linux/Windows/Mac OS X/FreeBSD):");
		m.put("For CI/CD job, add this gem to Gemfile like below", "Para trabajos de CI/CD, agregue esta gema al Gemfile como se muestra a continuación");
		m.put("For CI/CD job, add this package to requirements.txt and run below to install the package via command step", 
			"Para trabajos de CI/CD, agregue este paquete a requirements.txt y ejecute lo siguiente para instalar el paquete mediante el paso de comando");
		m.put("For CI/CD job, run below to add package repository via command step", "Para trabajos de CI/CD, ejecute lo siguiente para agregar el repositorio de paquetes mediante el paso de comando");
		m.put("For CI/CD job, run below to add package source via command step", "Para trabajos de CI/CD, ejecute lo siguiente para agregar la fuente del paquete mediante el paso de comando");
		m.put("For CI/CD job, run below to add source via command step", "Para trabajos de CI/CD, ejecute lo siguiente para agregar la fuente mediante el paso de comando");
		m.put("For CI/CD job, run below to install chart via command step", "Para trabajos de CI/CD, ejecute lo siguiente para instalar el gráfico mediante el paso de comando");
		m.put("For CI/CD job, run below to publish package via command step", "Para trabajos de CI/CD, ejecute lo siguiente para publicar el paquete mediante el paso de comando");
		m.put("For CI/CD job, run below to push chart to the repository via command step", "Para trabajos de CI/CD, ejecute lo siguiente para enviar el gráfico al repositorio mediante el paso de comando");
		m.put("For CI/CD job, run below via a command step", "Para trabajos de CI/CD, ejecute lo siguiente mediante un paso de comando");
		m.put("For a particular project, the first matching entry will be used", "Para un proyecto en particular, se usará la primera entrada coincidente");
		m.put("For all issues", "Para todos los problemas");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"Para confirmaciones de construcción no alcanzables desde la rama predeterminada, se debe especificar un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secreto de trabajo</a> como token de acceso con permiso para crear ramas");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"Para confirmaciones de construcción no alcanzables desde la rama predeterminada, se debe especificar un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secreto de trabajo</a> como token de acceso con permiso para crear etiquetas");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"Para confirmaciones de construcción no alcanzables desde la rama predeterminada, se debe especificar un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secreto de trabajo</a> como token de acceso con permiso para gestionar problemas");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"Para ejecutores conscientes de Docker, esta ruta está dentro del contenedor y acepta tanto rutas absolutas como relativas (relativas a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>el espacio de trabajo del trabajo</a>). Para ejecutores relacionados con shell que se ejecutan directamente en la máquina host, solo se acepta la ruta relativa");
		m.put("For each build, OneDev calculates a list of fixed issues since previous build automatically. This setting provides a default query to further filter/order this list. For a given job, the first matching entry will be used.", 
			"Para cada construcción, OneDev calcula automáticamente una lista de problemas corregidos desde la construcción anterior. Esta configuración proporciona una consulta predeterminada para filtrar/ordenar aún más esta lista. Para un trabajo dado, se usará la primera entrada coincidente.");
		m.put("For each selected branch/tag, a separate build will be generated with branch/tag set to corresponding value", 
			"Para cada rama/etiqueta seleccionada, se generará una construcción separada con la rama/etiqueta configurada al valor correspondiente");
		m.put("For issues matching: ", "Para problemas que coincidan:");
		m.put("For very large git repository, you may need to tune options here to reduce memory usage", 
			"Para repositorios git muy grandes, es posible que necesite ajustar las opciones aquí para reducir el uso de memoria");
		m.put("For web hooks defined here and in parent projects, OneDev will post event data in JSON format to specified URLs when subscribed events happen", 
			"Para los web hooks definidos aquí y en proyectos principales, OneDev enviará datos de eventos en formato JSON a las URL especificadas cuando ocurran eventos suscritos");
		m.put("Force", "Forzar");
		m.put("Force Garbage Collection", "Forzar Recolección de Basura");
		m.put("Forgot Password?", "¿Olvidó su Contraseña?");
		m.put("Forgotten Password?", "¿Contraseña olvidada?");
		m.put("Fork Project", "Crear bifurcación del proyecto");
		m.put("Fork now", "Bifurcar ahora");
		m.put("Forks Of", "Bifurcaciones de");
		m.put("Frequencies", "Frecuencias");
		m.put("From Directory", "Desde el Directorio");
		m.put("From States", "Desde Estados");
		m.put("From extracted folder, run <code>bin\\agent.bat console</code> as administrator on Windows or <code>bin/agent.sh console</code> on other OS", 
			"Desde la carpeta extraída, ejecute <code>bin\\agent.bat console</code> como administrador en Windows o <code>bin/agent.sh console</code> en otros sistemas operativos");
		m.put("From {0}", "Desde {0}");
		m.put("Full Name", "Nombre Completo");
		m.put("Furthest due date", "Fecha de vencimiento más lejana");
		m.put("GPG Keys", "Claves GPG");
		m.put("GPG Public Key", "Clave Pública GPG");
		m.put("GPG Signing Key", "Clave de Firma GPG");
		m.put("GPG Trusted Keys", "Claves de Confianza GPG");
		m.put("GPG key deleted", "Clave GPG eliminada");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "La clave pública GPG comienza con '-----BEGIN PGP PUBLIC KEY BLOCK-----'");
		m.put("GPG signing key will be used to sign commits generated by OneDev, including pull request merge commits, user commits created via web UI or RESTful api.", 
			"La clave de firma GPG se utilizará para firmar confirmaciones generadas por OneDev, incluidas las confirmaciones de fusión de solicitudes de extracción, confirmaciones de usuario creadas a través de la interfaz web o la API RESTful.");
		m.put("Gem Info", "Información de Gemas");
		m.put("General", "General");
		m.put("General Settings", "Configuraciones Generales");
		m.put("General settings updated", "Configuraciones generales actualizadas");
		m.put("Generate", "Generar");
		m.put("Generate File Checksum", "Generar Checksum de Archivo");
		m.put("Generate New", "Generar Nuevo");
		m.put("Generic LDAP", "LDAP Genérico");
		m.put("Get", "Obtener");
		m.put("Get Groups Using Attribute", "Obtener Grupos Usando Atributo");
		m.put("Git", "Git");
		m.put("Git Command Line", "Línea de Comandos de Git");
		m.put("Git Credential", "Credencial de Git");
		m.put("Git LFS Storage", "Almacenamiento Git LFS");
		m.put("Git Lfs Lock", "Bloqueo Git LFS");
		m.put("Git Location", "Ubicación de Git");
		m.put("Git Pack Config", "Configuración de Paquete Git");
		m.put("Git Path", "Ruta de Git");
		m.put("Git email address will be used as git author/committer for commits created on web UI", 
			"La dirección de correo de Git se usará como autor/committer de Git para los commits creados en la interfaz web");
		m.put("Git pack config updated", "Configuración de paquete Git actualizada");
		m.put("GitHub", "GitHub");
		m.put("GitHub API URL", "URL de API de GitHub");
		m.put("GitHub Issue Label", "Etiqueta de Incidencia de GitHub");
		m.put("GitHub Organization", "Organización de GitHub");
		m.put("GitHub Personal Access Token", "Token de Acceso Personal de GitHub");
		m.put("GitHub Repositories to Import", "Repositorios de GitHub para Importar");
		m.put("GitHub Repository", "Repositorio de GitHub");
		m.put("GitHub personal access token should be generated with scope <b>repo</b> and <b>read:org</b>", 
			"El token de acceso personal de GitHub debe generarse con el alcance <b>repo</b> y <b>read:org</b>");
		m.put("GitLab API URL", "URL de API de GitLab");
		m.put("GitLab Group", "Grupo de GitLab");
		m.put("GitLab Issue Label", "Etiqueta de Incidencia de GitLab");
		m.put("GitLab Personal Access Token", "Token de Acceso Personal de GitLab");
		m.put("GitLab Project", "Proyecto de GitLab");
		m.put("GitLab Projects to Import", "Proyectos de GitLab para Importar");
		m.put("GitLab personal access token should be generated with scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by user of specified access token will be listed", 
			"El token de acceso personal de GitLab debe generarse con el alcance <b>read_api</b>, <b>read_user</b> y <b>read_repository</b>. Tenga en cuenta que solo se listarán los grupos/proyectos propiedad del usuario del token de acceso especificado");
		m.put("Gitea API URL", "URL de API de Gitea");
		m.put("Gitea Issue Label", "Etiqueta de Incidencia de Gitea");
		m.put("Gitea Organization", "Organización de Gitea");
		m.put("Gitea Personal Access Token", "Token de Acceso Personal de Gitea");
		m.put("Gitea Repositories to Import", "Repositorios de Gitea para Importar");
		m.put("Gitea Repository", "Repositorio de Gitea");
		m.put("Github Access Token Secret", "Secreto del Token de Acceso de GitHub");
		m.put("Global", "Global");
		m.put("Global Build Setting", "Configuración Global de Construcción");
		m.put("Global Issue Setting", "Configuración Global de Incidencias");
		m.put("Global Pack Setting", "Configuración Global de Paquetes");
		m.put("Global Views", "Vistas Globales");
		m.put("Gmail", "Gmail");
		m.put("Go Back", "Regresar");
		m.put("Google Test Report", "Informe de Prueba de Google");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Clave Gpg");
		m.put("Great, your mail service configuration is working", "Genial, la configuración de su servicio de correo está funcionando");
		m.put("Groovy Script", "Script Groovy");
		m.put("Groovy Scripts", "Scripts Groovy");
		m.put("Groovy script to be evaluated. It should return a <i>Date</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. Debe devolver un valor de tipo <i>Date</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. It should return a <i>Float</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. Debe devolver un valor de tipo <i>Float</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. It should return a <i>Integer</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. Debe devolver un valor de tipo <i>Integer</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. It should return a <i>String</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. Debe devolver un valor de tipo <i>String</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. It should return a <i>boolean</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. Debe devolver un valor de tipo <i>boolean</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. It should return a <i>string</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. Debe devolver un valor de tipo <i>string</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. It should return name of a group. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. Debe devolver el nombre de un grupo. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. It should return string or list of string. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. Debe devolver una cadena o lista de cadenas. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. El valor devuelto debe ser una lista de objetos fachada de grupo para usar como opciones. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. The return value should be a list of user login names to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. El valor devuelto debe ser una lista de nombres de inicio de sesión de usuario para usar como opciones. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy script to be evaluated. The return value should be a value to color map, for instance:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> if the value does not have a color. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy para evaluar. El valor devuelto debe ser un mapa de valor a color, por ejemplo:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> si el valor no tiene un color. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ayuda de scripting</a> para más detalles");
		m.put("Groovy scripts", "Scripts Groovy");
		m.put("Group", "Grupo");
		m.put("Group \"{0}\" deleted", "Grupo \"{0}\" eliminado");
		m.put("Group Authorization Bean", "Bean de Autorización de Grupo");
		m.put("Group Authorizations", "Autorizaciones de Grupo");
		m.put("Group Authorizations Bean", "Bean de Autorizaciones de Grupo");
		m.put("Group By", "Agrupar Por");
		m.put("Group Management", "Gestión de Grupos");
		m.put("Group Name Attribute", "Atributo de Nombre de Grupo");
		m.put("Group Retrieval", "Recuperación de Grupo");
		m.put("Group Search Base", "Base de Búsqueda de Grupo");
		m.put("Group Search Filter", "Filtro de Búsqueda de Grupo");
		m.put("Group authorizations updated", "Autorizaciones de grupo actualizadas");
		m.put("Group created", "Grupo creado");
		m.put("Groups", "Grupos");
		m.put("Groups Claim", "Reclamación de Grupos");
		m.put("Guide Line", "Línea Guía");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "URL de Clonación HTTP(S)");
		m.put("Has Owner Permissions", "Tiene Permisos de Propietario");
		m.put("Has Running Builds", "Tiene Construcciones en Ejecución");
		m.put("Heap Memory Usage", "Uso de Memoria Heap");
		m.put("Helm(s)", "Helm(s)");
		m.put("Help", "Ayuda");
		m.put("Hide", "Ocultar");
		m.put("Hide Archived", "Ocultar Archivados");
		m.put("Hide comment", "Ocultar comentario");
		m.put("Hide saved queries", "Ocultar consultas guardadas");
		m.put("High", "Alta");
		m.put("High Availability & Scalability", "Alta Disponibilidad y Escalabilidad");
		m.put("High Severity", "Alta Severidad");
		m.put("History", "Historia");
		m.put("History of comparing revisions is unrelated", "La historia de comparación de revisiones no está relacionada");
		m.put("History of target branch and source branch is unrelated", "La historia de la rama objetivo y la rama fuente no está relacionada");
		m.put("Host name or ip address of remote machine to run commands via SSH", "Nombre de host o dirección IP de la máquina remota para ejecutar comandos vía SSH");
		m.put("Hours Per Day", "Horas por día");
		m.put("How to Publish", "Cómo publicar");
		m.put("Html Report", "Informe HTML");
		m.put("Http Method", "Método Http");
		m.put("I didn't eat it. I swear!", "No me lo comí. ¡Lo juro!");
		m.put("ID token was expired", "El token de ID ha expirado");
		m.put("IMAP Host", "Host IMAP");
		m.put("IMAP Password", "Contraseña IMAP");
		m.put("IMAP User", "Usuario IMAP");
		m.put("IMPORTANT:", "IMPORTANTE:");
		m.put("IP Address", "Dirección IP");
		m.put("Id", "ID");
		m.put("Identify Field", "Campo de identificación");
		m.put("If enabled, scheduled backup will run on lead server which is <span wicket:id=\"leadServer\"></span> currently", 
			"Si está habilitado, la copia de seguridad programada se ejecutará en el servidor principal que es <span wicket:id=\"leadServer\"></span> actualmente");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"Si está habilitado, la rama fuente se eliminará automáticamente después de fusionar la solicitud de extracción si el usuario tiene permiso para hacerlo");
		m.put("If specified, OneDev will only display iterations with this prefix", "Si se especifica, OneDev solo mostrará iteraciones con este prefijo");
		m.put("If specified, all public and internal projects imported from GitLab will use these as default roles. Private projects are not affected", 
			"Si se especifica, todos los proyectos públicos e internos importados de GitLab usarán estos como roles predeterminados. Los proyectos privados no se ven afectados");
		m.put("If specified, all public repositories imported from GitHub will use these as default roles. Private repositories are not affected", 
			"Si se especifica, todos los repositorios públicos importados de GitHub usarán estos como roles predeterminados. Los repositorios privados no se ven afectados");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"Si se especifica, el tiempo total estimado/gastado de un problema también incluirá problemas vinculados de este tipo");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"Si esta opción está habilitada, el comando git lfs necesita estar instalado en el servidor OneDev (incluso si este paso se ejecuta en otro nodo)");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"Si está marcado, el grupo indicado por este campo podrá editar el tiempo estimado de los problemas correspondientes si el seguimiento de tiempo está habilitado");
		m.put("Ignore", "Ignorar");
		m.put("Ignore File", "Ignorar archivo");
		m.put("Ignore activities irrelevant to me", "Ignorar actividades irrelevantes para mí");
		m.put("Ignore all", "Ignorar todo");
		m.put("Ignore all whitespace", "Ignorar todos los espacios en blanco");
		m.put("Ignore change", "Ignorar cambio");
		m.put("Ignore change whitespace", "Ignorar cambio de espacios en blanco");
		m.put("Ignore leading", "Ignorar inicial");
		m.put("Ignore leading whitespace", "Ignorar espacios en blanco iniciales");
		m.put("Ignore this field", "Ignorar este campo");
		m.put("Ignore this param", "Ignorar este parámetro");
		m.put("Ignore trailing", "Ignorar final");
		m.put("Ignore trailing whitespace", "Ignorar espacios en blanco finales");
		m.put("Ignored Licenses", "Licencias ignoradas");
		m.put("Image", "Imagen");
		m.put("Image Labels", "Etiquetas de imagen");
		m.put("Image Manifest", "Manifesto de imagen");
		m.put("Image Size", "Tamaño de imagen");
		m.put("Image Text", "Texto de imagen");
		m.put("Image URL", "URL de imagen");
		m.put("Image URL should be specified", "Se debe especificar la URL de la imagen");
		m.put("Imap Ssl Setting", "Configuración SSL de IMAP");
		m.put("Imap With Ssl", "IMAP con SSL");
		m.put("Impersonate", "Suplantar");
		m.put("Implicit SSL", "SSL implícito");
		m.put("Import", "Importar");
		m.put("Import All Projects", "Importar todos los proyectos");
		m.put("Import All Repositories", "Importar todos los repositorios");
		m.put("Import Group", "Importar grupo");
		m.put("Import Issues", "Importar problemas");
		m.put("Import Option", "Opción de importación");
		m.put("Import Organization", "Importar organización");
		m.put("Import Project", "Importar proyecto");
		m.put("Import Projects", "Importar proyectos");
		m.put("Import Repositories", "Importar repositorios");
		m.put("Import Repository", "Importar repositorio");
		m.put("Import Server", "Importar servidor");
		m.put("Import Workspace", "Importar espacio de trabajo");
		m.put("Import build spec elements (jobs, services, step templates and properties) from other projects. Imported elements are treated as if they are defined locally. Locally defined elements will override imported elements with same name", 
			"Importar elementos de especificación de compilación (trabajos, servicios, plantillas de pasos y propiedades) de otros proyectos. Los elementos importados se tratan como si estuvieran definidos localmente. Los elementos definidos localmente sobrescribirán los elementos importados con el mismo nombre");
		m.put("Importing Issues from {0}", "Importando problemas desde {0}");
		m.put("Importing from {0}", "Importando desde {0}");
		m.put("Importing issues into currrent project. Please note that issue numbers will only be retained if the whole project fork graph does not have any issues to avoid duplicate issue numbers", 
			"Importando problemas en el proyecto actual. Tenga en cuenta que los números de problemas solo se conservarán si todo el gráfico de bifurcación del proyecto no tiene problemas para evitar números de problemas duplicados");
		m.put("Importing projects from {0}", "Importando proyectos desde {0}");
		m.put("Imports", "Importaciones");
		m.put("In Projects", "En proyectos");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"En caso de que el certificado del host IMAP sea autofirmado o su raíz CA no sea aceptada, puede indicar a OneDev que omita la verificación del certificado. <b class='text-danger'>ADVERTENCIA: </b> En una red no confiable, esto puede llevar a un ataque de intermediario, y debería <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>importar el certificado en OneDev</a> en su lugar");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"En caso de que el certificado del host SMTP sea autofirmado o su raíz CA no sea aceptada, puede indicar a OneDev que omita la verificación del certificado. <b class='text-danger'>ADVERTENCIA: </b> En una red no confiable, esto puede llevar a un ataque de intermediario, y debería <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>importar el certificado en OneDev</a> en su lugar");
		m.put("In case anonymous access is disabled or anonymous user does not have enough permission for a resource operation, you will need to authenticate by providing user name and password (or access token) via http basic auth header", 
			"En caso de que el acceso anónimo esté deshabilitado o el usuario anónimo no tenga suficiente permiso para una operación de recurso, necesitará autenticarse proporcionando nombre de usuario y contraseña (o token de acceso) a través del encabezado de autenticación básica http");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"En caso de que la caché no se encuentre mediante la clave anterior, OneDev recorrerá las claves de carga definidas aquí en orden hasta que se encuentre una caché coincidente en la jerarquía del proyecto. Una caché se considera coincidente si su clave tiene el prefijo de la clave de carga. Si varias cachés coinciden, se devolverá la caché más reciente");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"En caso de que sea necesario cargar la caché, esta propiedad especifica el proyecto objetivo para la carga. Déjelo vacío para el proyecto actual");
		m.put("In case the pull request status is out of sync with underlying repository, you may synchronize them manually here", 
			"En caso de que el estado de la solicitud de extracción esté desincronizado con el repositorio subyacente, puede sincronizarlos manualmente aquí");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"En caso de que la membresía del grupo de usuarios se mantenga en el lado del grupo, esta propiedad especifica el nodo base para la búsqueda del grupo. Por ejemplo: <i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"En caso de que la relación del grupo de usuarios se mantenga en el lado del grupo, este filtro se utiliza para determinar los grupos a los que pertenece el usuario actual. Por ejemplo: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. En este ejemplo, <i>{0}</i> representa el DN del usuario actual");
		m.put("In case you are using external issue tracker, you can define transformers to transform external issue references into external issue links in various places, such as commit messages and pull request descriptions", 
			"En caso de que esté utilizando un rastreador de problemas externo, puede definir transformadores para transformar referencias de problemas externos en enlaces de problemas externos en varios lugares, como mensajes de confirmación y descripciones de solicitudes de extracción");
		m.put("In rare cases, your issues might be out of sync with workflow settings (undefined state/field etc.). Run integrity check below to find problems and get them fixed.", 
			"En casos raros, sus problemas pueden estar desincronizados con la configuración del flujo de trabajo (estado/campo indefinido, etc.). Ejecute la verificación de integridad a continuación para encontrar problemas y solucionarlos");
		m.put("Inbox Poll Setting", "Configuración de sondeo de bandeja de entrada");
		m.put("Include Child Projects", "Incluir proyectos secundarios");
		m.put("Include Disabled", "Incluir deshabilitados");
		m.put("Include Forks", "Incluir bifurcaciones");
		m.put("Include When Issue is Opened", "Incluir cuando el problema esté abierto");
		m.put("Incompatibilities", "Incompatibilidades");
		m.put("Inconsistent issuer in provider metadata and ID token", "Emisor inconsistente en los metadatos del proveedor y el token de ID");
		m.put("Indicator", "Indicador");
		m.put("Inherit from parent", "Heredar del padre");
		m.put("Inherited", "Heredado");
		m.put("Input Spec", "Especificación de entrada");
		m.put("Input URL", "URL de entrada");
		m.put("Input allowed CORS origin, hit ENTER to add", "Ingrese el origen permitido de CORS, presione ENTER para agregar");
		m.put("Input revision", "Ingrese la revisión");
		m.put("Input title", "Ingrese el título");
		m.put("Input title here", "Ingrese el título aquí");
		m.put("Input user search base. Hit ENTER to add", "Ingrese la base de búsqueda de usuarios. Presione ENTER para agregar");
		m.put("Input user search bases. Hit ENTER to add", "Ingrese las bases de búsqueda de usuarios. Presione ENTER para agregar");
		m.put("Insert", "Insertar");
		m.put("Insert Image", "Insertar imagen");
		m.put("Insert Link", "Insertar enlace");
		m.put("Insert link to this file", "Insertar enlace a este archivo");
		m.put("Insert this image", "Insertar esta imagen");
		m.put("Install Subscription Key", "Instalar clave de suscripción");
		m.put("Integer", "Entero");
		m.put("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"El acceso interactivo a la shell web para trabajos en ejecución es una característica empresarial. <a href='https://onedev.io/pricing' target='_blank'>Prueba gratis</a> por 30 días");
		m.put("Internal Database", "Base de datos interna");
		m.put("Interpreter", "Intérprete");
		m.put("Invalid GPG signature", "Firma GPG inválida");
		m.put("Invalid PCRE syntax", "Sintaxis PCRE inválida");
		m.put("Invalid access token: {0}", "Token de acceso inválido: {0}");
		m.put("Invalid credentials", "Credenciales inválidas");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "Rango de fechas inválido, se espera \"yyyy-MM-dd a yyyy-MM-dd\"");
		m.put("Invalid email address: {0}", "Dirección de correo electrónico inválida: {0}");
		m.put("Invalid invitation code", "Código de invitación inválido");
		m.put("Invalid issue date of ID token", "Fecha de emisión del token de ID inválida");
		m.put("Invalid issue number: {0}", "Número de emisión inválido: {0}");
		m.put("Invalid pull request number: {0}", "Número de solicitud de extracción inválido: {0}");
		m.put("Invalid request path", "Ruta de solicitud inválida");
		m.put("Invalid selection, click for details", "Selección inválida, haga clic para más detalles");
		m.put("Invalid ssh signature", "Firma ssh inválida");
		m.put("Invalid state response", "Respuesta de estado inválida");
		m.put("Invalid state. Please make sure you are visiting OneDev using server url specified in system setting", 
			"Estado inválido. Por favor asegúrese de estar visitando OneDev usando la URL del servidor especificada en la configuración del sistema");
		m.put("Invalid subscription key", "Clave de suscripción inválida");
		m.put("Invalid working period", "Período de trabajo inválido");
		m.put("Invitation sent to \"{0}\"", "Invitación enviada a \"{0}\"");
		m.put("Invitation to \"{0}\" deleted", "Invitación a \"{0}\" eliminada");
		m.put("Invitations", "Invitaciones");
		m.put("Invitations sent", "Invitaciones enviadas");
		m.put("Invite", "Invitar");
		m.put("Invite Users", "Invitar usuarios");
		m.put("Is Site Admin", "Es administrador del sitio");
		m.put("Issue", "Problema");
		m.put("Issue #{0} deleted", "Problema #{0} eliminado");
		m.put("Issue Board", "Tablero de problemas");
		m.put("Issue Boards", "Tableros de problemas");
		m.put("Issue Close States", "Estados de cierre de problemas");
		m.put("Issue Creation Setting", "Configuración de creación de problemas");
		m.put("Issue Creation Settings", "Configuraciones de creación de problemas");
		m.put("Issue Custom Fields", "Campos personalizados de problemas");
		m.put("Issue Description", "Descripción del problema");
		m.put("Issue Description Templates", "Plantillas de descripción de problemas");
		m.put("Issue Details", "Detalles del problema");
		m.put("Issue Field", "Campo del problema");
		m.put("Issue Field Mapping", "Mapeo de campo del problema");
		m.put("Issue Field Mappings", "Mapeos de campo del problema");
		m.put("Issue Field Set", "Conjunto de campos del problema");
		m.put("Issue Fields", "Campos del problema");
		m.put("Issue Filter", "Filtro de problemas");
		m.put("Issue Import Option", "Opción de importación de problemas");
		m.put("Issue Label Mapping", "Mapeo de etiquetas de problemas");
		m.put("Issue Label Mappings", "Mapeos de etiquetas de problemas");
		m.put("Issue Link", "Enlace de problemas");
		m.put("Issue Link Mapping", "Mapeo de enlace de problemas");
		m.put("Issue Link Mappings", "Mapeos de enlace de problemas");
		m.put("Issue Links", "Enlaces de problemas");
		m.put("Issue Management", "Gestión de problemas");
		m.put("Issue Notification", "Notificación de problemas");
		m.put("Issue Notification Template", "Plantilla de notificación de problemas");
		m.put("Issue Notification Unsubscribed", "Notificación de problemas cancelada");
		m.put("Issue Notification Unsubscribed Template", "Plantilla de notificación de problemas cancelada");
		m.put("Issue Pattern", "Patrón de problemas");
		m.put("Issue Priority Mapping", "Mapeo de prioridad de problemas");
		m.put("Issue Priority Mappings", "Mapeos de prioridad de problemas");
		m.put("Issue Query", "Consulta de problemas");
		m.put("Issue Settings", "Configuraciones de problemas");
		m.put("Issue State", "Estado del problema");
		m.put("Issue State Mapping", "Mapeo de estado del problema");
		m.put("Issue State Mappings", "Mapeos de estado del problema");
		m.put("Issue State Transition", "Transición de estado del problema");
		m.put("Issue State Transitions", "Transiciones de estado del problema");
		m.put("Issue States", "Estados de problemas");
		m.put("Issue Statistics", "Estadísticas de problemas");
		m.put("Issue Stats", "Estadísticas de problemas");
		m.put("Issue Status Mapping", "Mapeo de estado de problemas");
		m.put("Issue Status Mappings", "Mapeos de estado de problemas");
		m.put("Issue Stopwatch Overdue", "Cronómetro de problemas vencido");
		m.put("Issue Stopwatch Overdue Notification Template", "Plantilla de notificación de cronómetro de problemas vencido");
		m.put("Issue Tag Mapping", "Mapeo de etiquetas de problemas");
		m.put("Issue Tag Mappings", "Mapeos de etiquetas de problemas");
		m.put("Issue Template", "Plantilla de problemas");
		m.put("Issue Transition ({0} -> {1})", "Transición de problema ({0} -> {1})");
		m.put("Issue Type Mapping", "Mapeo de tipo de problema");
		m.put("Issue Type Mappings", "Mapeos de tipo de problema");
		m.put("Issue Votes", "Votos de problemas");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"Permiso administrativo de problemas dentro de un proyecto, incluyendo operaciones en lote sobre múltiples problemas");
		m.put("Issue count", "Conteo de problemas");
		m.put("Issue in state", "Problema en estado");
		m.put("Issue list", "Lista de problemas");
		m.put("Issue management not enabled in this project", "La gestión de problemas no está habilitada en este proyecto");
		m.put("Issue management permission required to move issues", "Se requiere permiso de gestión de problemas para mover problemas");
		m.put("Issue not exist or access denied", "El problema no existe o el acceso fue denegado");
		m.put("Issue number", "Número de problema");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"La consulta de problemas solo afecta a nuevos problemas. Para gestionar el estado de observación de problemas existentes en lote, filtre los problemas por estado de observación en la página de problemas y luego tome la acción adecuada");
		m.put("Issue state duration statistics", "Estadísticas de duración de estado de problemas");
		m.put("Issue state frequency statistics", "Estadísticas de frecuencia de estado de problemas");
		m.put("Issue state trend statistics", "Estadísticas de tendencia de estado de problemas");
		m.put("Issue statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Las estadísticas de problemas son una característica empresarial. <a href='https://onedev.io/pricing' target='_blank'>Prueba gratis</a> por 30 días");
		m.put("Issue workflow changed, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliation</a> needs to be performed to make data consistent. You may do this after making all necessary changes", 
			"El flujo de trabajo de problemas ha cambiado, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliación</a> necesita ser realizada para hacer que los datos sean consistentes. Puede hacer esto después de realizar todos los cambios necesarios");
		m.put("Issues", "Problemas");
		m.put("Issues can be created in this project by sending email to this address", "Los problemas pueden ser creados en este proyecto enviando un correo electrónico a esta dirección");
		m.put("Issues copied", "Problemas copiados");
		m.put("Issues moved", "Problemas movidos");
		m.put("Italic", "Cursiva");
		m.put("Iteration", "Iteración");
		m.put("Iteration \"{0}\" closed", "Iteración \"{0}\" cerrada");
		m.put("Iteration \"{0}\" deleted", "Iteración \"{0}\" eliminada");
		m.put("Iteration \"{0}\" is closed", "Iteración \"{0}\" está cerrada");
		m.put("Iteration \"{0}\" is reopened", "Iteración \"{0}\" reabierta");
		m.put("Iteration \"{0}\" reopened", "Iteración \"{0}\" reabierta");
		m.put("Iteration Edit Bean", "Editar Bean de Iteración");
		m.put("Iteration Name", "Nombre de Iteración");
		m.put("Iteration Names", "Nombres de Iteración");
		m.put("Iteration Prefix", "Prefijo de Iteración");
		m.put("Iteration list", "Lista de Iteraciones");
		m.put("Iteration saved", "Iteración guardada");
		m.put("Iteration spans too long to show burndown chart", "La duración de la iteración es demasiado larga para mostrar el gráfico de burndown");
		m.put("Iteration start and due date should be specified to show burndown chart", "Se deben especificar la fecha de inicio y vencimiento de la iteración para mostrar el gráfico de burndown");
		m.put("Iteration start date should be before due date", "La fecha de inicio de la iteración debe ser anterior a la fecha de vencimiento");
		m.put("Iterations", "Iteraciones");
		m.put("Iterations Bean", "Bean de Iteraciones");
		m.put("JIRA Issue Priority", "Prioridad de Issue en JIRA");
		m.put("JIRA Issue Status", "Estado de Issue en JIRA");
		m.put("JIRA Issue Type", "Tipo de Issue en JIRA");
		m.put("JIRA Project", "Proyecto JIRA");
		m.put("JIRA Projects to Import", "Proyectos JIRA para Importar");
		m.put("JUnit Report", "Informe JUnit");
		m.put("JVM", "JVM");
		m.put("JaCoCo Coverage Report", "Informe de Cobertura JaCoCo");
		m.put("Jest Coverage Report", "Informe de Cobertura Jest");
		m.put("Jest Test Report", "Informe de Pruebas Jest");
		m.put("Job", "Trabajo");
		m.put("Job \"{0}\" associated with the build not found.", "Trabajo \"{0}\" asociado con la compilación no encontrado.");
		m.put("Job Authorization", "Autorización de Trabajo");
		m.put("Job Cache Management", "Gestión de Caché de Trabajo");
		m.put("Job Dependencies", "Dependencias de Trabajo");
		m.put("Job Dependency", "Dependencia de Trabajo");
		m.put("Job Executor", "Ejecutor de Trabajo");
		m.put("Job Executor Bean", "Bean de Ejecutor de Trabajo");
		m.put("Job Executors", "Ejecutores de Trabajo");
		m.put("Job Name", "Nombre de Trabajo");
		m.put("Job Names", "Nombres de Trabajo");
		m.put("Job Param", "Parámetro de Trabajo");
		m.put("Job Parameters", "Parámetros de Trabajo");
		m.put("Job Privilege", "Privilegio de Trabajo");
		m.put("Job Privileges", "Privilegios de Trabajo");
		m.put("Job Properties", "Propiedades de Trabajo");
		m.put("Job Properties Bean", "Bean de Propiedades de Trabajo");
		m.put("Job Property", "Propiedad de Trabajo");
		m.put("Job Secret", "Secreto de Trabajo");
		m.put("Job Secret Edit Bean", "Editar Bean de Secreto de Trabajo");
		m.put("Job Secrets", "Secretos de Trabajo");
		m.put("Job Trigger", "Disparador de Trabajo");
		m.put("Job Trigger Bean", "Bean de Disparador de Trabajo");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"Permiso administrativo de trabajo, incluyendo eliminar compilaciones del trabajo. Implica todos los demás permisos de trabajo");
		m.put("Job cache \"{0}\" deleted", "Caché de trabajo \"{0}\" eliminado");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"Las dependencias de trabajo determinan el orden y la concurrencia al ejecutar diferentes trabajos. También puede especificar artefactos para recuperar de trabajos anteriores");
		m.put("Job executor tested successfully", "Ejecutor de trabajo probado exitosamente");
		m.put("Job executors", "Ejecutores de trabajo");
		m.put("Job name", "Nombre de trabajo");
		m.put("Job properties saved", "Propiedades de trabajo guardadas");
		m.put("Job secret \"{0}\" deleted", "Secreto de trabajo \"{0}\" eliminado");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission", 
			"El secreto de trabajo 'access-token' debe definirse en la configuración de compilación del proyecto como un token de acceso con permiso de paquete ${permission}");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package read permission", 
			"El secreto de trabajo 'access-token' debe definirse en la configuración de compilación del proyecto como un token de acceso con permiso de lectura de paquete");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package write permission", 
			"El secreto de trabajo 'access-token' debe definirse en la configuración de compilación del proyecto como un token de acceso con permiso de escritura de paquete");
		m.put("Job token", "Token de trabajo");
		m.put("Job will run on head commit of default branch", "El trabajo se ejecutará en el commit principal de la rama predeterminada");
		m.put("Job will run on head commit of target branch", "El trabajo se ejecutará en el commit principal de la rama objetivo");
		m.put("Job will run on merge commit of target branch and source branch", "El trabajo se ejecutará en el commit de fusión de la rama objetivo y la rama fuente");
		m.put("Job will run on merge commit of target branch and source branch.<br><b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"El trabajo se ejecutará en el commit de fusión de la rama objetivo y la rama fuente.<br><b class='text-info'>NOTA:</b> A menos que sea requerido por la regla de protección de rama, este disparador ignorará commits con mensajes que contengan <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, o <code>[no job]</code>");
		m.put("Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"El trabajo se ejecutará cuando se comprometa código. <b class='text-info'>NOTA:</b> Este disparador ignorará commits con mensajes que contengan <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, o <code>[no job]</code>");
		m.put("Job workspace", "Espacio de trabajo de trabajo");
		m.put("Jobs", "Trabajos");
		m.put("Jobs marked with <span class=\"text-danger\">*</span> are required to be successful", 
			"Los trabajos marcados con <span class=\"text-danger\">*</span> deben ser exitosos");
		m.put("Jobs required to be successful on merge commit: ", "Trabajos requeridos para ser exitosos en el commit de fusión:");
		m.put("Jobs required to be successful: ", "Trabajos requeridos para ser exitosos:");
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"Los trabajos con el mismo grupo secuencial y ejecutor se ejecutarán secuencialmente. Por ejemplo, puede especificar esta propiedad como <tt>@project_path@:prod</tt> para trabajos ejecutados por el mismo ejecutor y desplegados en el entorno de producción del proyecto actual para evitar despliegues conflictivos");
		m.put("Key", "Clave");
		m.put("Key Fingerprint", "Huella digital de la clave");
		m.put("Key ID", "ID de Clave");
		m.put("Key Secret", "Secreto de Clave");
		m.put("Key Type", "Tipo de clave");
		m.put("Kubectl Config File", "Archivo de Configuración de Kubectl");
		m.put("Kubernetes", "Kubernetes");
		m.put("Kubernetes Executor", "Ejecutor de Kubernetes");
		m.put("LDAP URL", "URL de LDAP");
		m.put("Label", "Etiqueta");
		m.put("Label Management", "Gestión de Etiquetas");
		m.put("Label Management Bean", "Bean de Gestión de Etiquetas");
		m.put("Label Name", "Nombre de Etiqueta");
		m.put("Label Spec", "Especificación de Etiqueta");
		m.put("Label Value", "Valor de Etiqueta");
		m.put("Labels", "Etiquetas");
		m.put("Labels Bean", "Bean de Etiquetas");
		m.put("Labels can be defined in Administration / Label Management", "Las etiquetas pueden definirse en Administración / Gestión de Etiquetas");
		m.put("Labels have been updated", "Las etiquetas han sido actualizadas");
		m.put("Language", "Idioma");
		m.put("Last Accessed", "Último Acceso");
		m.put("Last Finished of Specified Job", "Última Finalización del Trabajo Especificado");
		m.put("Last Modified", "Última Modificación");
		m.put("Last Published", "Última Publicación");
		m.put("Last Update", "Última Actualización");
		m.put("Last commit", "Último commit");
		m.put("Last commit hash", "Hash del último commit");
		m.put("Last commit index version", "Versión de índice del último commit");
		m.put("Leaf Projects", "Proyectos Hoja");
		m.put("Least branch coverage", "Menor cobertura de ramas");
		m.put("Least line coverage", "Menor cobertura de líneas");
		m.put("Leave a comment", "Dejar un comentario");
		m.put("Leave a note", "Dejar una nota");
		m.put("Left", "Izquierda");
		m.put("Less", "Menos");
		m.put("License Agreement", "Acuerdo de Licencia");
		m.put("License Setting", "Configuración de Licencia");
		m.put("Licensed To", "Licenciado a");
		m.put("Licensed To:", "Licenciado a:");
		m.put("Line", "Línea");
		m.put("Line changes", "Cambios en la línea");
		m.put("Line: ", "Línea:");
		m.put("Lines", "Líneas");
		m.put("Link", "Enlace");
		m.put("Link Existing User", "Vincular Usuario Existente");
		m.put("Link Spec", "Especificación de Enlace");
		m.put("Link Spec Opposite", "Especificación de Enlace Opuesto");
		m.put("Link Text", "Texto del Enlace");
		m.put("Link URL", "URL del Enlace");
		m.put("Link URL should be specified", "Se debe especificar la URL del enlace");
		m.put("Link User Bean", "Vincular Usuario Bean");
		m.put("Linkable Issues", "Problemas Vinculables");
		m.put("Linkable Issues On the Other Side", "Problemas Vinculables en el Otro Lado");
		m.put("Links", "Enlaces");
		m.put("Links can be used to associate different issues. For instance, an issue can be linked to sub issues or related issues", 
			"Los enlaces pueden usarse para asociar diferentes problemas. Por ejemplo, un problema puede estar vinculado a subproblemas o problemas relacionados");
		m.put("List", "Lista");
		m.put("Literal", "Literal");
		m.put("Literal default value", "Valor predeterminado literal");
		m.put("Literal value", "Valor literal");
		m.put("Load Keys", "Cargar Claves");
		m.put("Loading emojis...", "Cargando emojis...");
		m.put("Loading...", "Cargando...");
		m.put("Log", "Registro");
		m.put("Log Work", "Registrar Trabajo");
		m.put("Log not available for offline agent", "El registro no está disponible para el agente sin conexión");
		m.put("Log work", "Registrar trabajo");
		m.put("Login Name", "Nombre de Inicio de Sesión");
		m.put("Login and generate refresh token", "Iniciar sesión y generar token de actualización");
		m.put("Login name already used by another account", "El nombre de inicio de sesión ya está en uso por otra cuenta");
		m.put("Login name or email", "Nombre de inicio de sesión o correo electrónico");
		m.put("Login name or email address", "Nombre de inicio de sesión o dirección de correo electrónico");
		m.put("Login to OneDev docker registry", "Iniciar sesión en el registro de Docker de OneDev");
		m.put("Login to comment", "Iniciar sesión para comentar");
		m.put("Login to comment on selection", "Iniciar sesión para comentar sobre la selección");
		m.put("Login to vote", "Iniciar sesión para votar");
		m.put("Login user needs to have package write permission over the project below", "El usuario que inicia sesión necesita tener permiso de escritura de paquetes sobre el proyecto a continuación");
		m.put("Login with {0}", "Iniciar sesión con {0}");
		m.put("Logo for Dark Mode", "Logo para Modo Oscuro");
		m.put("Logo for Light Mode", "Logo para Modo Claro");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"Token de actualización de larga duración de la cuenta anterior que se utilizará para generar un token de acceso para acceder a Gmail. <b class='text-info'>CONSEJOS: </b> puede usar el botón en el lado derecho de este campo para generar el token de actualización. Tenga en cuenta que cada vez que se cambie el id del cliente, el secreto del cliente o el nombre de la cuenta, se debe regenerar el token de actualización");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"Token de actualización de larga duración de la cuenta anterior que se utilizará para generar un token de acceso para acceder al servidor de correo de Office 365. <b class='text-info'>CONSEJOS: </b> puede usar el botón en el lado derecho de este campo para iniciar sesión en su cuenta de Office 365 y generar el token de actualización. Tenga en cuenta que cada vez que se cambie el id del inquilino, el id del cliente, el secreto del cliente o el nombre principal del usuario, se debe regenerar el token de actualización");
		m.put("Longest Duration First", "Mayor Duración Primero");
		m.put("Looks like a GPG signature but without necessary data", "Parece una firma GPG pero sin los datos necesarios");
		m.put("Low", "Bajo");
		m.put("Low Severity", "Severidad Baja");
		m.put("MERGED", "FUSIONADO");
		m.put("MS Teams Notifications", "Notificaciones de MS Teams");
		m.put("Mail", "Correo");
		m.put("Mail Connector", "Conector de Correo");
		m.put("Mail Connector Bean", "Bean de Conector de Correo");
		m.put("Mail Service", "Servicio de Correo");
		m.put("Mail Service Test", "Prueba del Servicio de Correo");
		m.put("Mail service not configured", "Servicio de correo no configurado");
		m.put("Mail service settings saved", "Configuraciones del servicio de correo guardadas");
		m.put("Make sure <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 or higher</a> is installed", 
			"Asegúrese de que <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 o superior</a> esté instalado");
		m.put("Make sure current user has permission to run docker containers", "Asegúrese de que el usuario actual tenga permiso para ejecutar contenedores de Docker");
		m.put("Make sure docker engine is installed and docker command line is available in system path", 
			"Asegúrese de que el motor de Docker esté instalado y que la línea de comandos de Docker esté disponible en la ruta del sistema");
		m.put("Make sure git version 2.11.1 or higher is installed and available in system path", "Asegúrese de que la versión de Git 2.11.1 o superior esté instalada y disponible en la ruta del sistema");
		m.put("Make sure git-lfs is installed and available in system path if you want to retrieve LFS files", 
			"Asegúrese de que git-lfs esté instalado y disponible en la ruta del sistema si desea recuperar archivos LFS");
		m.put("Make sure the access token has package read permission over the project", "Asegúrese de que el token de acceso tenga permiso de lectura de paquetes sobre el proyecto");
		m.put("Make sure the access token has package write permission over the project", "Asegúrese de que el token de acceso tenga permiso de escritura de paquetes sobre el proyecto");
		m.put("Make sure the access token has package write permission over the project. Also make sure to run command <code>chmod 0600 $HOME/.gem/credentials</code> after creating the file", 
			"Asegúrese de que el token de acceso tenga permiso de escritura de paquetes sobre el proyecto. También asegúrese de ejecutar el comando <code>chmod 0600 $HOME/.gem/credentials</code> después de crear el archivo");
		m.put("Make sure the account has package ${permission} permission over the project", "Asegúrese de que la cuenta tenga permiso de paquete ${permission} sobre el proyecto");
		m.put("Make sure the account has package read permission over the project", "Asegúrese de que la cuenta tenga permiso de lectura de paquetes sobre el proyecto");
		m.put("Make sure the user has package write permission over the project", "Asegúrese de que el usuario tenga permiso de escritura de paquetes sobre el proyecto");
		m.put("Malformed %sbase query", "Consulta %sbase malformada");
		m.put("Malformed %squery", "Consulta %s malformada");
		m.put("Malformed build spec (import project: {0}, import revision: {1})", "Especificación de construcción malformada (importar proyecto: {0}, importar revisión: {1})");
		m.put("Malformed email address", "Dirección de correo electrónico malformada");
		m.put("Malformed filter", "Filtro malformado");
		m.put("Malformed name filter", "Filtro de nombre malformado");
		m.put("Malformed query", "Consulta malformada");
		m.put("Malformed ssh signature", "Firma ssh malformada");
		m.put("Malformed test suite filter", "Filtro de suite de pruebas malformado");
		m.put("Manage Job", "Gestionar Trabajo");
		m.put("Manager DN", "DN del Administrador");
		m.put("Manager Password", "Contraseña del Administrador");
		m.put("Manifest blob unknown", "Blob del manifiesto desconocido");
		m.put("Manifest invalid", "Manifiesto inválido");
		m.put("Manifest unknown", "Manifiesto desconocido");
		m.put("Many commands print outputs with ANSI colors in TTY mode to help identifying problems easily. However some commands running in this mode may wait for user input to cause build hanging. This can normally be fixed by adding extra options to the command", 
			"Muchos comandos imprimen salidas con colores ANSI en modo TTY para ayudar a identificar problemas fácilmente. Sin embargo, algunos comandos que se ejecutan en este modo pueden esperar la entrada del usuario y causar que la construcción se cuelgue. Esto normalmente se puede solucionar agregando opciones adicionales al comando");
		m.put("Mark a property archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived properties will not be shown by default", 
			"Marque una propiedad como archivada si ya no se utiliza en la especificación de construcción actual, pero aún necesita existir para reproducir construcciones antiguas. Las propiedades archivadas no se mostrarán por defecto");
		m.put("Mark a secret archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived secrets will not be shown by default", 
			"Marque un secreto como archivado si ya no se utiliza en la especificación de construcción actual, pero aún necesita existir para reproducir construcciones antiguas. Los secretos archivados no se mostrarán por defecto");
		m.put("Markdown", "Markdown");
		m.put("Markdown Report", "Informe Markdown");
		m.put("Markdown from file", "Markdown desde archivo");
		m.put("Maven(s)", "Maven(s)");
		m.put("Max Code Search Entries", "Máximo de Entradas de Búsqueda de Código");
		m.put("Max Commit Message Line Length", "Longitud Máxima de Línea de Mensaje de Confirmación");
		m.put("Max Git LFS File Size (MB)", "Tamaño Máximo de Archivo Git LFS (MB)");
		m.put("Max Retries", "Máximos Reintentos");
		m.put("Max Upload File Size (MB)", "Tamaño Máximo de Archivo de Carga (MB)");
		m.put("Max Value", "Valor Máximo");
		m.put("Maximum number of entries to return when search code in repository", "Número máximo de entradas a devolver al buscar código en el repositorio");
		m.put("Maximum of retries before giving up", "Máximo de reintentos antes de rendirse");
		m.put("May not be empty", "No puede estar vacío");
		m.put("Medium", "Medio");
		m.put("Medium Severity", "Severidad Media");
		m.put("Members", "Miembros");
		m.put("Memory", "Memoria");
		m.put("Memory Limit", "Límite de memoria");
		m.put("Memory Request", "Solicitud de memoria");
		m.put("Mention Someone", "Mencionar a alguien");
		m.put("Mention someone", "Mencionar a alguien");
		m.put("Merge", "Fusionar");
		m.put("Merge Strategy", "Estrategia de fusión");
		m.put("Merge Target Branch into Source Branch", "Fusionar la rama de destino en la rama fuente");
		m.put("Merge branch \"{0}\" into branch \"{1}\"", "Fusionar la rama \"{0}\" en la rama \"{1}\"");
		m.put("Merge branch \"{0}\" of project \"{1}\" into branch \"{2}\"", "Fusionar la rama \"{0}\" del proyecto \"{1}\" en la rama \"{2}\"");
		m.put("Merge preview not calculated yet", "Vista previa de fusión aún no calculada");
		m.put("Merged", "Fusionado");
		m.put("Merged pull request \"{0}\" ({1})", "Solicitud de extracción fusionada \"{0}\" ({1})");
		m.put("Merges pull request", "Fusiona solicitud de extracción");
		m.put("Meta", "Meta");
		m.put("Meta Info", "Información meta");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "Valor mínimo");
		m.put("Minimum length of the password", "Longitud mínima de la contraseña");
		m.put("Missing Commit", "Commit faltante");
		m.put("Missing Commits", "Commits faltantes");
		m.put("Month", "Mes");
		m.put("Months", "Meses");
		m.put("Months to Display", "Meses para mostrar");
		m.put("More", "Más");
		m.put("More Options", "Más opciones");
		m.put("More Settings", "Más configuraciones");
		m.put("More commits", "Más commits");
		m.put("More info", "Más información");
		m.put("More operations", "Más operaciones");
		m.put("Most branch coverage", "Mayor cobertura de ramas");
		m.put("Most line coverage", "Mayor cobertura de líneas");
		m.put("Most probably there are import errors in the <a wicket:id=\"buildSpec\">build spec</a>", 
			"Probablemente hay errores de importación en el <a wicket:id=\"buildSpec\">especificación de construcción</a>");
		m.put("Mount Docker Sock", "Montar Docker Sock");
		m.put("Move All Queried Issues To...", "Mover todas las incidencias consultadas a...");
		m.put("Move All Queried Projects To...", "Mover todos los proyectos consultados a...");
		m.put("Move Selected Issues To...", "Mover las incidencias seleccionadas a...");
		m.put("Move Selected Projects To...", "Mover los proyectos seleccionados a...");
		m.put("Multiple Lines", "Líneas múltiples");
		m.put("Multiple On the Other Side", "Múltiples en el otro lado");
		m.put("Must not be empty", "No debe estar vacío");
		m.put("My Access Tokens", "Mis tokens de acceso");
		m.put("My Basic Settings", "Mis configuraciones básicas");
		m.put("My Email Addresses", "Mis direcciones de correo electrónico");
		m.put("My GPG Keys", "Mis claves GPG");
		m.put("My Profile", "Mi perfil");
		m.put("My SSH Keys", "Mis claves SSH");
		m.put("My SSO Accounts", "Mis Cuentas SSO");
		m.put("Mypy Report", "Informe Mypy");
		m.put("N/A", "N/A");
		m.put("NPM(s)", "NPM(s)");
		m.put("Name", "Nombre");
		m.put("Name Of Empty Value", "Nombre del valor vacío");
		m.put("Name On the Other Side", "Nombre en el otro lado");
		m.put("Name Prefix", "Prefijo de nombre");
		m.put("Name already used by another access token of the owner", "Nombre ya utilizado por otro token de acceso del propietario");
		m.put("Name already used by another link", "Nombre ya utilizado por otro enlace");
		m.put("Name and name on the other side should be different", "El nombre y el nombre en el otro lado deben ser diferentes");
		m.put("Name containing spaces or starting with dash needs to be quoted", "El nombre que contiene espacios o comienza con un guion debe estar entre comillas");
		m.put("Name invalid", "Nombre inválido");
		m.put("Name of the link", "Nombre del enlace");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"Nombre del enlace en el otro lado. Por ejemplo, si el nombre es <tt>sub issues</tt>, el nombre en el otro lado puede ser <tt>parent issue</tt>");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"El nombre del proveedor tendrá dos propósitos: <ul><li>Mostrar en el botón de inicio de sesión<li>Formar la URL de callback de autorización que será <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>");
		m.put("Name reversely", "Nombre inversamente");
		m.put("Name unknown", "Nombre desconocido");
		m.put("Name your file", "Nombrar tu archivo");
		m.put("Named Agent Queries Bean", "Consultas de agentes nombradas Bean");
		m.put("Named Agent Query", "Consulta de agente nombrada");
		m.put("Named Build Queries Bean", "Consultas de construcción nombradas Bean");
		m.put("Named Build Query", "Consulta de construcción nombrada");
		m.put("Named Code Comment Queries Bean", "Consultas de comentarios de código nombradas Bean");
		m.put("Named Code Comment Query", "Consulta de comentario de código nombrada");
		m.put("Named Commit Queries Bean", "Consultas de commits nombradas Bean");
		m.put("Named Commit Query", "Consulta de commit nombrada");
		m.put("Named Element", "Elemento nombrado");
		m.put("Named Issue Queries Bean", "Consultas de incidencias nombradas Bean");
		m.put("Named Issue Query", "Consulta de incidencia nombrada");
		m.put("Named Pack Queries Bean", "Consultas de paquetes nombradas Bean");
		m.put("Named Pack Query", "Consulta de paquete nombrada");
		m.put("Named Project Queries Bean", "Consultas de proyectos nombradas Bean");
		m.put("Named Project Query", "Consulta de proyecto nombrada");
		m.put("Named Pull Request Queries Bean", "Consultas de solicitudes de extracción nombradas Bean");
		m.put("Named Pull Request Query", "Consulta de solicitud de extracción nombrada");
		m.put("Named Query", "Consulta nombrada");
		m.put("Network Options", "Opciones de red");
		m.put("Never", "Nunca");
		m.put("Never expire", "Nunca expira");
		m.put("New Board", "Nuevo tablero");
		m.put("New Invitation Bean", "Nuevo Bean de invitación");
		m.put("New Issue", "Nueva incidencia");
		m.put("New Password", "Nueva contraseña");
		m.put("New State", "Nuevo estado");
		m.put("New User Bean", "Nuevo Bean de usuario");
		m.put("New Value", "Nuevo valor");
		m.put("New issue board created", "Nuevo tablero de incidencias creado");
		m.put("New project created", "Nuevo proyecto creado");
		m.put("New user created", "Nuevo usuario creado");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"Nueva versión disponible. Rojo para actualización de seguridad/crítica, amarillo para corrección de errores, azul para actualización de características. Haga clic para mostrar los cambios. Deshabilitar en la configuración del sistema");
		m.put("Next", "Siguiente");
		m.put("Next commit", "Siguiente commit");
		m.put("Next {0}", "Siguiente {0}");
		m.put("No", "No");
		m.put("No Activity Days", "Días sin actividad");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"No hay claves SSH configuradas en su cuenta. Puede <a wicket:id=\"sshKeys\" class=\"link-primary\">agregar una clave</a> o cambiar a la url <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a>");
		m.put("No SSL", "No SSL");
		m.put("No accessible reports", "No hay informes accesibles");
		m.put("No activity for some time", "No hay actividad por algún tiempo");
		m.put("No agents to pause", "No hay agentes para pausar");
		m.put("No agents to remove", "No hay agentes para eliminar");
		m.put("No agents to restart", "No hay agentes para reiniciar");
		m.put("No agents to resume", "No hay agentes para reanudar");
		m.put("No aggregation", "No hay agregación");
		m.put("No any", "No hay ninguno");
		m.put("No any matches", "No hay coincidencias");
		m.put("No applicable transitions or no permission to transit", "No hay transiciones aplicables o no hay permiso para transitar");
		m.put("No attributes defined (can only be edited when agent is online)", "No hay atributos definidos (solo se pueden editar cuando el agente está en línea)");
		m.put("No audits", "Sin auditorías");
		m.put("No authorized job secret found (project: {0}, job secret: {1})", "No se encontró un secreto de trabajo autorizado (proyecto: {0}, secreto de trabajo: {1})");
		m.put("No branch to cherry-pick to", "No hay rama para hacer cherry-pick");
		m.put("No branch to revert on", "No hay rama para revertir");
		m.put("No branches Found", "No se encontraron ramas");
		m.put("No branches found", "No se encontraron ramas");
		m.put("No build in query context", "No hay compilación en el contexto de consulta");
		m.put("No builds", "No hay compilaciones");
		m.put("No builds to cancel", "No hay compilaciones para cancelar");
		m.put("No builds to delete", "No hay compilaciones para eliminar");
		m.put("No builds to re-run", "No hay compilaciones para volver a ejecutar");
		m.put("No comment", "No hay comentarios");
		m.put("No comments to delete", "No hay comentarios para eliminar");
		m.put("No comments to set as read", "No hay comentarios para marcar como leídos");
		m.put("No comments to set resolved", "No hay comentarios para marcar como resueltos");
		m.put("No comments to set unresolved", "No hay comentarios para marcar como no resueltos");
		m.put("No commit in query context", "No hay commit en el contexto de consulta");
		m.put("No config file", "No hay archivo de configuración");
		m.put("No current build in query context", "No hay compilación actual en el contexto de consulta");
		m.put("No current commit in query context", "No hay commit actual en el contexto de consulta");
		m.put("No current pull request in query context", "No hay solicitud de extracción actual en el contexto de consulta");
		m.put("No data", "No hay datos");
		m.put("No default branch", "No hay rama predeterminada");
		m.put("No default group", "No hay grupo predeterminado");
		m.put("No default roles", "No hay roles predeterminados");
		m.put("No default value", "No hay valor predeterminado");
		m.put("No description", "No hay descripción");
		m.put("No diffs", "No hay diferencias");
		m.put("No diffs to navigate", "No hay diferencias para navegar");
		m.put("No directories to skip", "No hay directorios para omitir");
		m.put("No disallowed file types", "No hay tipos de archivos no permitidos");
		m.put("No executors defined. Jobs will use auto-discovered executors instead", "No se han definido ejecutores. Los trabajos usarán ejecutores descubiertos automáticamente");
		m.put("No external password authenticator", "No hay autenticador de contraseña externa");
		m.put("No external password authenticator to authenticate user \"{0}\"", "No hay autenticador de contraseña externa para autenticar al usuario \"{0}\"");
		m.put("No fields to prompt", "No hay campos para solicitar");
		m.put("No fields to remove", "No hay campos para eliminar");
		m.put("No file attachments", "No hay archivos adjuntos");
		m.put("No group by", "No hay agrupación");
		m.put("No groups claim returned", "No se devolvió reclamación de grupos");
		m.put("No groups to remove from", "No hay grupos de los que eliminar");
		m.put("No ignore file", "No hay archivo de ignorar");
		m.put("No ignored licenses", "No hay licencias ignoradas");
		m.put("No image attachments", "No hay imágenes adjuntas");
		m.put("No imports defined", "No hay importaciones definidas");
		m.put("No issue boards defined", "No hay tableros de problemas definidos");
		m.put("No issues in iteration", "No hay problemas en la iteración");
		m.put("No issues to copy", "No hay problemas para copiar");
		m.put("No issues to delete", "No hay problemas para eliminar");
		m.put("No issues to edit", "No hay problemas para editar");
		m.put("No issues to export", "No hay problemas para exportar");
		m.put("No issues to move", "No hay problemas para mover");
		m.put("No issues to set as read", "No hay problemas para marcar como leídos");
		m.put("No issues to sync estimated/spent time", "No hay problemas para sincronizar tiempo estimado/gastado");
		m.put("No issues to watch/unwatch", "No hay problemas para observar/dejar de observar");
		m.put("No jobs defined", "No hay trabajos definidos");
		m.put("No jobs found", "No se encontraron trabajos");
		m.put("No limit", "No hay límite");
		m.put("No mail service", "No hay servicio de correo");
		m.put("No obvious changes", "No hay cambios evidentes");
		m.put("No one", "No hay nadie");
		m.put("No packages to delete", "No hay paquetes para eliminar");
		m.put("No parent", "No hay padre");
		m.put("No previous successful build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a> to calculate fixed issues since", 
			"No hay compilación exitosa previa en <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">la misma secuencia</a> para calcular problemas solucionados desde entonces");
		m.put("No projects found", "No se encontraron proyectos");
		m.put("No projects to delete", "No hay proyectos para eliminar");
		m.put("No projects to modify", "No hay proyectos para modificar");
		m.put("No projects to move", "No hay proyectos para mover");
		m.put("No properties defined", "No hay propiedades definidas");
		m.put("No proxy", "No hay proxy");
		m.put("No pull request in query context", "No hay solicitud de extracción en el contexto de consulta");
		m.put("No pull requests to delete", "No hay solicitudes de extracción para eliminar");
		m.put("No pull requests to discard", "No hay solicitudes de extracción para descartar");
		m.put("No pull requests to set as read", "No hay solicitudes de extracción para marcar como leídas");
		m.put("No pull requests to watch/unwatch", "No hay solicitudes de extracción para observar/dejar de observar");
		m.put("No refs to build on behalf of", "No hay referencias para construir en nombre de");
		m.put("No required services", "No hay servicios requeridos");
		m.put("No response body", "Sin cuerpo de respuesta");
		m.put("No secret config", "No hay configuración secreta");
		m.put("No services defined", "No hay servicios definidos");
		m.put("No start/due date", "No hay fecha de inicio/vencimiento");
		m.put("No step templates defined", "No hay plantillas de pasos definidas");
		m.put("No suggestions", "Sin sugerencias");
		m.put("No tags found", "No se encontraron etiquetas");
		m.put("No timesheets defined", "No hay hojas de tiempo definidas");
		m.put("No user found with login name or email: ", "No se encontró usuario con nombre de inicio de sesión o correo electrónico:");
		m.put("No users to convert to service accounts", "No hay usuarios para convertir a cuentas de servicio");
		m.put("No users to delete", "No hay usuarios para eliminar");
		m.put("No users to disable", "No hay usuarios para deshabilitar");
		m.put("No users to enable", "No hay usuarios para habilitar");
		m.put("No users to remove from group", "No hay usuarios para eliminar del grupo");
		m.put("No valid query to show progress", "No hay consulta válida para mostrar progreso");
		m.put("No valid signature for head commit", "No hay firma válida para el commit principal");
		m.put("No valid signature for head commit of target branch", "No hay firma válida para el commit principal de la rama objetivo");
		m.put("No value", "Sin valor");
		m.put("No verified primary email address", "No hay dirección de correo electrónico principal verificada");
		m.put("Node Selector", "Selector de Nodo");
		m.put("Node Selector Entry", "Entrada del Selector de Nodo");
		m.put("None", "Ninguno");
		m.put("Not Active Since", "No activo desde");
		m.put("Not Used Since", "No usado desde");
		m.put("Not a verified email of signing GPG key", "No es un correo electrónico verificado de la clave GPG de firma");
		m.put("Not a verified email of signing ssh key owner", "No es un correo electrónico verificado del propietario de la clave ssh de firma");
		m.put("Not allowed file type: {0}", "Tipo de archivo no permitido: {0}");
		m.put("Not assigned", "No asignado");
		m.put("Not authorized to create project under \"{0}\"", "No autorizado para crear un proyecto bajo \"{0}\"");
		m.put("Not authorized to create root project", "No autorizado para crear un proyecto raíz");
		m.put("Not authorized to move project under this parent", "No autorizado para mover el proyecto bajo este padre");
		m.put("Not authorized to set as root project", "No autorizado para establecer como proyecto raíz");
		m.put("Not covered", "No cubierto");
		m.put("Not covered by any test", "No cubierto por ninguna prueba");
		m.put("Not displaying any fields", "No mostrando ningún campo");
		m.put("Not displaying any links", "No mostrando ningún enlace");
		m.put("Not passed", "No aprobado");
		m.put("Not rendered in failsafe mode", "No renderizado en modo de seguridad");
		m.put("Not run", "No ejecutado");
		m.put("Not specified", "No especificado");
		m.put("Note", "Nota");
		m.put("Nothing to preview", "Nada para previsualizar");
		m.put("Notification", "Notificación");
		m.put("Notifications", "Notificaciones");
		m.put("Notify Build Events", "Notificar eventos de construcción");
		m.put("Notify Code Comment Events", "Notificar eventos de comentarios de código");
		m.put("Notify Code Push Events", "Notificar eventos de empuje de código");
		m.put("Notify Issue Events", "Notificar eventos de problemas");
		m.put("Notify Own Events", "Notificar eventos propios");
		m.put("Notify Pull Request Events", "Notificar eventos de solicitudes de extracción");
		m.put("Notify Users", "Notificar usuarios");
		m.put("Ntfy.sh Notifications", "Notificaciones de Ntfy.sh");
		m.put("NuGet(s)", "NuGet(s)");
		m.put("NuSpec", "NuSpec");
		m.put("Number of CPU Cores", "Número de núcleos de CPU");
		m.put("Number of SSH Keys", "Número de claves SSH");
		m.put("Number of builds to preserve", "Número de construcciones a preservar");
		m.put("Number of project replicas, including primary and backups", "Número de réplicas del proyecto, incluyendo primarias y de respaldo");
		m.put("Number of recent months to show statistics for", "Número de meses recientes para mostrar estadísticas");
		m.put("OAuth2 Client information | CLIENT ID", "Información del cliente OAuth2 | ID DEL CLIENTE");
		m.put("OAuth2 Client information | CLIENT SECRET", "Información del cliente OAuth2 | SECRETO DEL CLIENTE");
		m.put("OCI Layout Directory", "Directorio de diseño OCI");
		m.put("OIDC error: Inconsistent sub in ID token and userinfo", "Error OIDC: Sub inconsistente en el token ID y la información del usuario");
		m.put("OOPS! There Is An Error", "¡UPS! Hay un error");
		m.put("OPEN", "ABIERTO");
		m.put("OS", "Sistema Operativo");
		m.put("OS Arch", "Arquitectura del Sistema Operativo");
		m.put("OS User Name", "Nombre de Usuario del Sistema Operativo");
		m.put("OS Version", "Versión del Sistema Operativo");
		m.put("OS/ARCH", "Sistema Operativo/ARQUITECTURA");
		m.put("Offline", "Desconectado");
		m.put("Ok", "Ok");
		m.put("Old Name", "Nombre Antiguo");
		m.put("Old Password", "Contraseña Antigua");
		m.put("On Behalf Of", "En Nombre De");
		m.put("On Branches", "En Ramas");
		m.put("OneDev Issue Field", "Campo de Problema de OneDev");
		m.put("OneDev Issue Link", "Enlace de Problema de OneDev");
		m.put("OneDev Issue State", "Estado de Problema de OneDev");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"OneDev analiza archivos del repositorio para búsqueda de código, estadísticas de líneas y estadísticas de contribución de código. Esta configuración indica qué archivos deben ser analizados y espera patrones de ruta separados por espacios <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. Un patrón puede ser excluido prefijándolo con '-', por ejemplo <code>-**/vendors/**</code> excluirá todos los archivos con vendors en la ruta. <b>NOTA: </b> Cambiar esta configuración solo afecta a nuevos commits. Para aplicar el cambio a commits históricos, detenga el servidor y elimine la carpeta <code>index</code> y <code>info/commit</code> bajo el <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>directorio de almacenamiento del proyecto</a>. El repositorio será reanalizado cuando se inicie el servidor");
		m.put("OneDev configures git hooks to communicate with itself via curl", "OneDev configura hooks de git para comunicarse consigo mismo vía curl");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"OneDev necesita buscar y determinar el DN del usuario, así como buscar información del grupo de usuarios si la recuperación de grupos está habilitada. Marque esta opción y especifique el DN del 'manager' y la contraseña si estas operaciones necesitan autenticación");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"OneDev requiere la línea de comandos de git para gestionar repositorios. La versión mínima requerida es 2.11.1. También asegúrese de que git-lfs esté instalado si desea recuperar archivos LFS en el trabajo de construcción");
		m.put("Online", "Conectado");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", 
			"Solo crear commit de fusión si la rama objetivo no puede avanzar rápidamente hacia la rama fuente");
		m.put("Only projects manageable by access token owner can be authorized", "Solo proyectos gestionables por el propietario del token de acceso pueden ser autorizados");
		m.put("Only system level audit events are displayed here. To view audit events for a specific project, please visit the project audit log page", 
			"Solo se muestran aquí los eventos de auditoría de nivel de sistema. Para ver los eventos de auditoría de un proyecto específico, por favor visite la página de registro de auditoría del proyecto");
		m.put("Only users able to authenticate via password can be linked", "Solo los usuarios que pueden autenticarse mediante contraseña pueden ser vinculados");
		m.put("Open", "Abrir");
		m.put("Open new pull request", "Abrir nueva solicitud de extracción");
		m.put("Open terminal of current running step", "Abrir terminal del paso en ejecución actual");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"La identificación del cliente OpenID será asignada por su proveedor de OpenID al registrar esta instancia de OneDev como aplicación cliente");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"El secreto del cliente OpenID será generado por su proveedor de OpenID al registrar esta instancia de OneDev como aplicación cliente");
		m.put("OpenSSH Public Key", "Clave Pública OpenSSH");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"La clave pública OpenSSH comienza con 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', o 'sk-ssh-ed25519@openssh.com'");
		m.put("Opened issue \"{0}\" ({1})", "Problema abierto \"{0}\" ({1})");
		m.put("Opened pull request \"{0}\" ({1})", "Solicitud de extracción abierta \"{0}\" ({1})");
		m.put("Operation", "Operación");
		m.put("Operation Failed", "Operación fallida");
		m.put("Operation Successful", "Operación exitosa");
		m.put("Operations", "Operaciones");
		m.put("Optional", "Opcional");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"Opcionalmente especifique el proyecto para crear el problema. Déjelo vacío para crear en el proyecto actual");
		m.put("Optionally add new users to specified default group", "Opcionalmente agregue nuevos usuarios al grupo predeterminado especificado");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"Opcionalmente agregue al usuario autenticado recientemente al grupo especificado si la información de membresía no está disponible");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"Opcionalmente agregue al usuario autenticado recientemente al grupo especificado si la información de membresía no se recupera");
		m.put("Optionally choose required builds. You may also input jobs not listed here, and press ENTER to add them", 
			"Opcionalmente elija construcciones requeridas. También puede ingresar trabajos no listados aquí y presionar ENTER para agregarlos");
		m.put("Optionally configure proxy to access remote repository. Proxy should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;", 
			"Opcionalmente configure un proxy para acceder al repositorio remoto. El proxy debe estar en el formato de &lt;proxy host&gt;:&lt;proxy port&gt;");
		m.put("Optionally define a unique key for the project with two or more upper case letters. This key can be used to reference issues, builds, and pull requests with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of <code>&lt;project path&gt;#&lt;number&gt;</code>", 
			"Opcionalmente defina una clave única para el proyecto con dos o más letras mayúsculas. Esta clave puede usarse para referenciar problemas, construcciones y solicitudes de extracción con una forma estable y corta <code>&lt;project key&gt;-&lt;number&gt;</code> en lugar de <code>&lt;project path&gt;#&lt;number&gt;</code>");
		m.put("Optionally define parameter specifications of the job", "Opcionalmente defina especificaciones de parámetros del trabajo");
		m.put("Optionally define parameter specifications of the step template", "Opcionalmente defina especificaciones de parámetros de la plantilla de paso");
		m.put("Optionally describe the group", "Opcionalmente describa el grupo");
		m.put("Optionally describes the custom field. Html tags are accepted", "Opcionalmente describa el campo personalizado. Se aceptan etiquetas HTML");
		m.put("Optionally describes the param. Html tags are accepted.", "Opcionalmente describa el parámetro. Se aceptan etiquetas HTML.");
		m.put("Optionally filter builds", "Opcionalmente filtre construcciones");
		m.put("Optionally filter issues", "Opcionalmente filtre problemas");
		m.put("Optionally filter pull requests", "Opcionalmente filtre solicitudes de extracción");
		m.put("Optionally leave a note", "Opcionalmente deje una nota");
		m.put("Optionally mount directories or files under job workspace into container", "Opcionalmente monte directorios o archivos bajo el espacio de trabajo del trabajo dentro del contenedor");
		m.put("Optionally select fields to prompt when this button is pressed", "Opcionalmente seleccione campos para mostrar cuando se presione este botón");
		m.put("Optionally select fields to remove when this transition happens", "Opcionalmente seleccione campos para eliminar cuando ocurra esta transición");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"Opcionalmente especifique el nombre del atributo dentro de la entrada LDAP del usuario cuyo valor se tomará como correo electrónico del usuario. Este campo normalmente se establece en <i>mail</i> según RFC 2798");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"Opcionalmente especifique el nombre del atributo dentro de la entrada LDAP del usuario cuyo valor se tomará como nombre completo del usuario. Este campo normalmente se establece en <i>displayName</i> según RFC 2798. Si se deja vacío, no se recuperará el nombre completo del usuario");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"Opcionalmente especifique <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>secreto de trabajo</a> para usar como token de acceso de GitHub. Esto se utiliza para recuperar notas de lanzamiento de dependencias alojadas en GitHub, y el acceso autenticado tendrá un límite de tasa más alto.");
		m.put("Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko", 
			"Opcionalmente especifique <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>opciones adicionales</a> de kaniko.");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane", 
			"Opcionalmente especifique <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>opciones adicionales</a> de crane.");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane", 
			"Opcionalmente especifique <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>opciones adicionales</a> de crane.");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to build for platform of the node running the job", 
			"Opcionalmente especifique <span class='text-info'>plataformas separadas por comas</span> para construir, por ejemplo <tt>linux/amd64,linux/arm64</tt>. Déjelo vacío para construir en la plataforma del nodo que ejecuta el trabajo.");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to scan, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to scan all platforms in OCI layout", 
			"Opcionalmente especifique <span class='text-info'>plataformas separadas por comas</span> para escanear, por ejemplo <tt>linux/amd64,linux/arm64</tt>. Déjelo vacío para escanear todas las plataformas en el diseño OCI.");
		m.put("Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use file <tt>Dockerfile</tt> under build path specified above", 
			"Opcionalmente especifique el Dockerfile relativo al <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a>. Déjelo vacío para usar el archivo <tt>Dockerfile</tt> bajo la ruta de construcción especificada anteriormente.");
		m.put("Optionally specify JavaScript config to be used by Renovate CLI", "Opcionalmente especifique la configuración de JavaScript para usar con Renovate CLI.");
		m.put("Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. Leave empty to derive from server url", 
			"Opcionalmente especifique la URL raíz de SSH, que se utilizará para construir la URL de clonación del proyecto mediante el protocolo SSH. Déjelo vacío para derivarlo de la URL del servidor.");
		m.put("Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of the text input", 
			"Opcionalmente especifique un <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>patrón de expresión regular</a> para valores válidos de la entrada de texto.");
		m.put("Optionally specify a OneDev project to be used as parent of imported projects. Leave empty to import as root projects", 
			"Opcionalmente especifique un proyecto de OneDev para usar como padre de los proyectos importados. Déjelo vacío para importar como proyectos raíz.");
		m.put("Optionally specify a OneDev project to be used as parent of imported repositories. Leave empty to import as root projects", 
			"Opcionalmente especifique un proyecto de OneDev para usar como padre de los repositorios importados. Déjelo vacío para importar como proyectos raíz.");
		m.put("Optionally specify a base query for the list", "Opcionalmente especifique una consulta base para la lista.");
		m.put("Optionally specify a base query to filter/order issues in backlog. Backlog issues are those not associating with current iteration", 
			"Opcionalmente especifique una consulta base para filtrar/ordenar problemas en el backlog. Los problemas del backlog son aquellos que no están asociados con la iteración actual.");
		m.put("Optionally specify a base query to filter/order issues of the board", "Opcionalmente especifique una consulta base para filtrar/ordenar problemas del tablero.");
		m.put("Optionally specify a cron expression to schedule database auto-backup. The cron expression format is <em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>.For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz tutorial</a>.The backup files will be placed into <em>db-backup</em> folder under OneDev installation directory. In case multiple servers connect to form a cluster, auto-backup takes place on the <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>lead server</a>. Leave this property empty if you do not want to enable database auto backup.", 
			"Opcionalmente especifique una expresión cron para programar la copia de seguridad automática de la base de datos. El formato de la expresión cron es <em>&lt;segundos&gt; &lt;minutos&gt; &lt;horas&gt; &lt;día-del-mes&gt; &lt;mes&gt; &lt;día-de-la-semana&gt;</em>. Por ejemplo, <em>0 0 1 * * ?</em> significa 1:00am todos los días. Para detalles del formato, consulte el <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>tutorial de Quartz</a>. Los archivos de copia de seguridad se colocarán en la carpeta <em>db-backup</em> bajo el directorio de instalación de OneDev. En caso de que varios servidores se conecten para formar un clúster, la copia de seguridad automática se realiza en el <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>servidor principal</a>. Deje esta propiedad vacía si no desea habilitar la copia de seguridad automática de la base de datos.");
		m.put("Optionally specify a date field to hold due date information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique un campo de fecha para contener información de fecha de vencimiento.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí.");
		m.put("Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to put retrieved artifacts. Leave empty to use job workspace itself", 
			"Opcionalmente especifique una ruta relativa al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para colocar artefactos recuperados. Déjelo vacío para usar el espacio de trabajo del trabajo en sí.");
		m.put("Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. <b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, as the volume is only used to hold temporary build files", 
			"Opcionalmente especifique una clase de almacenamiento para asignar dinámicamente el volumen de construcción. Déjelo vacío para usar la clase de almacenamiento predeterminada. <b class='text-warning'>NOTA:</b> La política de recuperación de la clase de almacenamiento debe configurarse en <code>Delete</code>, ya que el volumen solo se utiliza para contener archivos temporales de construcción.");
		m.put("Optionally specify a working period field to hold estimated time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique un campo de período de trabajo para contener información de tiempo estimado.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí.");
		m.put("Optionally specify a working period field to hold spent time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique un campo de período de trabajo para contener información de tiempo gastado.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí.");
		m.put("Optionally specify a working period field to hold time estimate infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique un campo de período de trabajo para contener información de estimación de tiempo.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí.");
		m.put("Optionally specify a working period field to hold time spent infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique un campo de período de trabajo para contener información de tiempo gastado.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí.");
		m.put("Optionally specify additional options for buildx build command", "Opcionalmente especifique opciones adicionales para el comando de construcción buildx.");
		m.put("Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, the response header <code>Access-Control-Allow-Origin</code> will be set to the same value", 
			"Opcionalmente especifique orígenes <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> permitidos. Para una solicitud CORS simple o de preflight, si el valor del encabezado de solicitud <code>Origin</code> está incluido aquí, el encabezado de respuesta <code>Access-Control-Allow-Origin</code> se establecerá con el mismo valor.");
		m.put("Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match", 
			"Opcionalmente especifique el dominio de correo electrónico permitido para usuarios de auto registro. Use '*' o '?' para coincidencia de patrones.");
		m.put("Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). Leave empty to all types", 
			"Opcionalmente especifique tipos de confirmación aplicables para la verificación del pie de mensaje de confirmación (presione ENTER para agregar valor). Déjelo vacío para todos los tipos.");
		m.put("Optionally specify applicable jobs of this executor", "Especifique opcionalmente los trabajos aplicables de este ejecutor");
		m.put("Optionally specify applicable users who pushed the change", "Opcionalmente especifica los usuarios aplicables que realizaron el cambio");
		m.put("Optionally specify arguments to run above image", "Opcionalmente especifique argumentos para ejecutar la imagen anterior.");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"Opcionalmente especifique artefactos para recuperar de la dependencia en el <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a>. Solo se pueden recuperar artefactos publicados (a través del paso de publicación de artefactos). Déjelo vacío para no recuperar ningún artefacto.");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"Opcionalmente especifique roles autorizados para presionar este botón. Si no se especifica, todos los usuarios están permitidos.");
		m.put("Optionally specify base query of the list", "Opcionalmente especifique la consulta base de la lista.");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"Opcionalmente especifique ramas/usuarios/grupos permitidos para acceder a este secreto. Si se deja vacío, cualquier trabajo puede acceder a este secreto, incluidos aquellos activados a través de solicitudes de extracción externas.");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"Opcionalmente especifique la ruta de contexto de construcción relativa al <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a>. Déjelo vacío para usar el espacio de trabajo del trabajo en sí. Se espera que el archivo <code>Dockerfile</code> exista en el directorio de contexto de construcción, a menos que especifique una ubicación diferente con la opción <code>--dockerfile</code>.");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"Opcionalmente especifique la ruta de construcción relativa al <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a>. Déjelo vacío para usar el espacio de trabajo del trabajo en sí.");
		m.put("Optionally specify cluster role the job pods service account binding to. This is necessary if you want to do things such as running other Kubernetes pods in job command", 
			"Opcionalmente especifique el rol de clúster al que se vincula la cuenta de servicio de los pods del trabajo. Esto es necesario si desea realizar acciones como ejecutar otros pods de Kubernetes en el comando del trabajo.");
		m.put("Optionally specify comma separated licenses to be ignored", "Opcionalmente especifique licencias separadas por comas para ignorar.");
		m.put("Optionally specify container arguments separated by space. Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse this with container options which should be specified in executor setting", 
			"Opcionalmente especifique argumentos del contenedor separados por espacio. Un único argumento que contenga espacio debe estar entre comillas. <b class='text-warning'>Nota: </b> no confunda esto con las opciones del contenedor que deben especificarse en la configuración del ejecutor.");
		m.put("Optionally specify cpu limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Opcionalmente especifique el límite de CPU para cada trabajo/servicio que use este ejecutor. Consulte <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>gestión de recursos de Kubernetes</a> para más detalles.");
		m.put("Optionally specify cpu limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> of relevant containers", 
			"Opcionalmente especifique el límite de CPU de cada trabajo/servicio que use este ejecutor. Esto se utilizará como opción <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> de los contenedores relevantes.");
		m.put("Optionally specify criteria of issues which can be linked", "Opcionalmente especifique criterios de problemas que se pueden vincular.");
		m.put("Optionally specify criteria of issues which can be linked on the other side", "Opcionalmente especifique criterios de problemas que se pueden vincular en el otro lado.");
		m.put("Optionally specify custom fields allowed to edit when open new issues", "Opcionalmente especifique campos personalizados permitidos para editar al abrir nuevos problemas.");
		m.put("Optionally specify depth for a shallow clone in order to speed up source retrieval", 
			"Opcionalmente especifique la profundidad para un clon superficial con el fin de acelerar la recuperación de la fuente.");
		m.put("Optionally specify description of the issue", "Opcionalmente especifique la descripción del problema.");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"Opcionalmente especifique directorios o patrones glob dentro de la ruta de escaneo para omitir. Los múltiples elementos para omitir deben estar separados por espacio.");
		m.put("Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types", 
			"Opcionalmente especifica tipos de archivos no permitidos por extensiones (presiona ENTER para agregar valor), por ejemplo <code>exe</code>, <code>bin</code>. Deja vacío para permitir todos los tipos de archivos");
		m.put("Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. Leave empty to use docker executable in PATH", 
			"Opcionalmente especifique el ejecutable de Docker, por ejemplo <i>/usr/local/bin/docker</i>. Déjelo vacío para usar el ejecutable de Docker en PATH.");
		m.put("Optionally specify docker options to create network. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Opcionalmente especifique opciones de Docker para crear la red. Las múltiples opciones deben estar separadas por espacio, y una única opción que contenga espacios debe estar entre comillas.");
		m.put("Optionally specify docker options to run container. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Opcionalmente especifique opciones de Docker para ejecutar el contenedor. Las múltiples opciones deben estar separadas por espacio, y una única opción que contenga espacios debe estar entre comillas.");
		m.put("Optionally specify docker sock to use. Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows", 
			"Opcionalmente especifique el socket de Docker a usar. Por defecto es <i>/var/run/docker.sock</i> en Linux, y <i>//./pipe/docker_engine</i> en Windows.");
		m.put("Optionally specify environment variables for the container", "Opcionalmente especifique variables de entorno para el contenedor.");
		m.put("Optionally specify environment variables for this step", "Opcionalmente especifique variables de entorno para este paso.");
		m.put("Optionally specify environment variables of the service", "Opcionalmente especifique variables de entorno del servicio.");
		m.put("Optionally specify estimated time.", "Opcionalmente especifique el tiempo estimado.");
		m.put("Optionally specify executor for this job. Leave empty to use auto-discover executor", 
			"Especifique opcionalmente el ejecutor para este trabajo. Déjelo vacío para usar el ejecutor descubierto automáticamente");
		m.put("Optionally specify executor for this job. Leave empty to use first applicable executor", 
			"Especifique opcionalmente el ejecutor para este trabajo. Déjelo vacío para usar el primer ejecutor aplicable");
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"Opcionalmente especifique archivos relativos a la ruta de caché para ignorar al detectar cambios en la caché. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Los múltiples archivos deben estar separados por espacio, y un único archivo que contenga espacio debe estar entre comillas.");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"Opcionalmente especifique la base de búsqueda de grupos si desea recuperar información de membresía de grupo del usuario. Por ejemplo: <i>cn=Users, dc=example, dc=com</i>. Para otorgar permisos apropiados a un grupo de Active Directory, se debe definir un grupo de OneDev con el mismo nombre. Déjelo vacío para gestionar las membresías de grupo en el lado de OneDev.");
		m.put("Optionally specify issue links allowed to edit", "Opcionalmente especifique enlaces de problemas permitidos para editar.");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "Opcionalmente especifique problemas aplicables para esta plantilla. Déjelo vacío para todos.");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"Opcionalmente especifique problemas aplicables para esta transición. Déjelo vacío para todos los problemas.");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"Opcionalmente especifique problemas aplicables para esta transición. Déjelo vacío para todos los problemas.");
		m.put("Optionally specify jobs allowed to use this script", "Opcionalmente especifique trabajos permitidos para usar este script.");
		m.put("Optionally specify memory limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Opcionalmente especifique el límite de memoria para cada trabajo/servicio que use este ejecutor. Consulte <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>gestión de recursos de Kubernetes</a> para más detalles.");
		m.put("Optionally specify memory limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> of relevant containers", 
			"Opcionalmente especifique el límite de memoria de cada trabajo/servicio que use este ejecutor. Esto se utilizará como opción <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> de los contenedores relevantes.");
		m.put("Optionally specify merge strategy of created pull request. Leave empty to use default strategy of each project", 
			"Opcionalmente especifique la estrategia de fusión de la solicitud de extracción creada. Déjelo vacío para usar la estrategia predeterminada de cada proyecto.");
		m.put("Optionally specify message of the tag", "Opcionalmente especifique el mensaje de la etiqueta.");
		m.put("Optionally specify name of the attribute inside the user LDAP entry whose values will be taken as user SSH keys. SSH keys will be managed by LDAP only if this field is set", 
			"Opcionalmente especifique el nombre del atributo dentro de la entrada LDAP del usuario cuyos valores se tomarán como claves SSH del usuario. Las claves SSH serán gestionadas por LDAP solo si este campo está configurado.");
		m.put("Optionally specify node selector of the job pods", "Opcionalmente especifique el selector de nodo de los pods del trabajo.");
		m.put("Optionally specify options for docker builder prune command", "Opcionalmente especifique opciones para el comando docker builder prune.");
		m.put("Optionally specify options for scp command. Multiple options need to be separated with space", 
			"Opcionalmente especifique opciones para el comando scp. Las múltiples opciones deben estar separadas por espacio.");
		m.put("Optionally specify options for ssh command. Multiple options need to be separated with space", 
			"Opcionalmente especifique opciones para el comando ssh. Las múltiples opciones deben estar separadas por espacio.");
		m.put("Optionally specify options passed to renovate cli. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Opcionalmente especifique opciones pasadas a renovate cli. Las múltiples opciones deben estar separadas por espacio, y una única opción que contenga espacios debe estar entre comillas.");
		m.put("Optionally specify osv scanner <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. You may ignore particular vulnerabilities via this file", 
			"Opcionalmente especifique el archivo de configuración del escáner osv <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> bajo el <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a>. Puede ignorar vulnerabilidades particulares a través de este archivo.");
		m.put("Optionally specify path protection rules", "Opcionalmente especifique reglas de protección de ruta.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"Opcionalmente especifique la ruta relativa al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para usar como archivo de ignorar de trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"Opcionalmente especifique la ruta relativa al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para usar como configuración de secretos de trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>.");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"Opcionalmente especifique la ruta relativa al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para publicar artefactos. Déjelo vacío para usar el espacio de trabajo del trabajo en sí.");
		m.put("Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. Leave empty to pull all platforms in image", 
			"Opcionalmente especifique la plataforma para extraer, por ejemplo <tt>linux/amd64</tt>. Déjelo vacío para extraer todas las plataformas en la imagen.");
		m.put("Optionally specify project to show builds of. Leave empty to show builds of all projects with permissions", 
			"Opcionalmente especifique el proyecto para mostrar las construcciones. Déjelo vacío para mostrar las construcciones de todos los proyectos con permisos.");
		m.put("Optionally specify project to show issues of. Leave empty to show issues of all accessible projects", 
			"Opcionalmente especifique el proyecto para mostrar los problemas. Déjelo vacío para mostrar los problemas de todos los proyectos accesibles.");
		m.put("Optionally specify project to show packages of. Leave empty to show packages of all projects with permissions", 
			"Opcionalmente especifique el proyecto para mostrar los paquetes. Déjelo vacío para mostrar los paquetes de todos los proyectos con permisos.");
		m.put("Optionally specify ref of above job, for instance <i>refs/heads/main</i>. Use * for wildcard match", 
			"Opcionalmente especifique la referencia del trabajo anterior, por ejemplo <i>refs/heads/main</i>. Use * para coincidencia de comodines.");
		m.put("Optionally specify registry logins to override those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token secret for password secret", 
			"Opcionalmente especifique inicios de sesión de registro para anular los definidos en el ejecutor del trabajo. Para el registro integrado, use <code>@server_url@</code> para la URL del registro, <code>@job_token@</code> para el nombre de usuario y el secreto del token de acceso para el secreto de contraseña.");
		m.put("Optionally specify relative directory to put uploaded files", "Opcionalmente especifique el directorio relativo para colocar los archivos subidos.");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to clone code into. Leave empty to use job workspace itself", 
			"Opcionalmente especifique la ruta relativa bajo el <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para clonar el código. Déjelo vacío para usar el espacio de trabajo del trabajo en sí.");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"Opcionalmente especifique la ruta relativa bajo el <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para escanear. Déjelo vacío para usar el espacio de trabajo del trabajo en sí.");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"Opcionalmente especifique rutas relativas bajo el <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para escanear vulnerabilidades de dependencias. Se pueden especificar múltiples rutas y deben estar separadas por espacio. Déjelo vacío para usar el espacio de trabajo del trabajo en sí.");
		m.put("Optionally specify required reviewers for changes of specified branch", "Opcionalmente especifique revisores requeridos para cambios en la rama especificada.");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"Opcionalmente especifique la revisión para crear la rama. Déjelo vacío para crear desde el commit de construcción.");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"Opcionalmente especifique un directorio separado para almacenar artefactos de construcción. Un directorio no absoluto se considera relativo al directorio del sitio.");
		m.put("Optionally specify separate directory to store git lfs files. Non-absolute directory is considered to be relative to site directory", 
			"Opcionalmente especifique un directorio separado para almacenar archivos git lfs. Un directorio no absoluto se considera relativo al directorio del sitio.");
		m.put("Optionally specify separate directory to store package files. Non-absolute directory is considered to be relative to site directory", 
			"Opcionalmente especifique un directorio separado para almacenar archivos de paquetes. Un directorio no absoluto se considera relativo al directorio del sitio.");
		m.put("Optionally specify services required by this job. <b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors (server docker executor, remote docker executor, or kubernetes executor)", 
			"Opcionalmente especifique servicios requeridos por este trabajo. <b class='text-warning'>NOTA:</b> Los servicios solo son compatibles con ejecutores conscientes de Docker (ejecutor de Docker del servidor, ejecutor de Docker remoto o ejecutor de Kubernetes).");
		m.put("Optionally specify space-separated branches applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifique ramas separadas por espacio aplicables para esta transición. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefijo con '-' para excluir. Déjelo vacío para coincidir con todas.");
		m.put("Optionally specify space-separated branches applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for default branch", 
			"Opcionalmente especifique ramas separadas por espacio aplicables para este disparador. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefijo con '-' para excluir. Déjelo vacío para la rama predeterminada.");
		m.put("Optionally specify space-separated branches to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Opcionalmente especifique ramas separadas por espacio para verificar. Use '**' o '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefijo con '-' para excluir. Déjelo vacío para coincidir con todas las ramas.");
		m.put("Optionally specify space-separated commit messages applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifica mensajes de commit separados por espacios aplicables para esta transición. Usa '*' o '?' para coincidencia de comodines. Prefija con '-' para excluir. Deja vacío para coincidir con todos");
		m.put("Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all files", 
			"Opcionalmente especifique archivos separados por espacio para verificar. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefijo con '-' para excluir. Déjelo vacío para coincidir con todos los archivos.");
		m.put("Optionally specify space-separated jobs applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifique trabajos separados por espacio aplicables para esta transición. Use '*' o '?' para coincidencia de comodines. Prefijo con '-' para excluir. Déjelo vacío para coincidir con todos.");
		m.put("Optionally specify space-separated projects applicable for this trigger. This is useful for instance when you want to prevent the job from being triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Opcionalmente especifique proyectos separados por espacio aplicables para este disparador. Esto es útil, por ejemplo, cuando desea evitar que el trabajo se active en proyectos bifurcados. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefijo con '-' para excluir. Déjelo vacío para coincidir con todos los proyectos.");
		m.put("Optionally specify space-separated projects to search in. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to search in all projects with code read permission", 
			"Opcionalmente especifique proyectos separados por espacio para buscar. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefijo con '-' para excluir. Déjelo vacío para buscar en todos los proyectos con permiso de lectura de código.");
		m.put("Optionally specify space-separated reports. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Opcionalmente especifique informes separados por espacios. Use '*' o '?' para coincidencia de comodines. Prefije con '-' para excluir");
		m.put("Optionally specify space-separated service images applicable for this locator. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifique imágenes de servicio separadas por espacios aplicables para este localizador. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefije con '-' para excluir. Deje vacío para coincidir con todos");
		m.put("Optionally specify space-separated service names applicable for this locator. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifique nombres de servicio separados por espacios aplicables para este localizador. Use '*' o '?' para coincidencia de comodines. Prefije con '-' para excluir. Deje vacío para coincidir con todos");
		m.put("Optionally specify space-separated tags to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all tags", 
			"Opcionalmente especifique etiquetas separadas por espacios para verificar. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefije con '-' para excluir. Deje vacío para coincidir con todas las etiquetas");
		m.put("Optionally specify space-separated target branches of the pull requests to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Opcionalmente especifique ramas objetivo separadas por espacios de las solicitudes de extracción para verificar. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefije con '-' para excluir. Deje vacío para coincidir con todas las ramas");
		m.put("Optionally specify the OpenID claim to retrieve groups of authenticated user. Depending on the provider, you may need to request additional scopes above to make this claim available", 
			"Opcionalmente especifique el reclamo de OpenID para recuperar grupos de usuarios autenticados. Dependiendo del proveedor, es posible que necesite solicitar alcances adicionales arriba para que este reclamo esté disponible");
		m.put("Optionally specify the maximum value allowed.", "Opcionalmente especifique el valor máximo permitido.");
		m.put("Optionally specify the minimum value allowed.", "Opcionalmente especifique el valor mínimo permitido.");
		m.put("Optionally specify the project to publish site files to. Leave empty to publish to current project", 
			"Opcionalmente especifique el proyecto para publicar archivos del sitio. Deje vacío para publicar en el proyecto actual");
		m.put("Optionally specify uid:gid to run container as. <b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or using user namespace remapping", 
			"Opcionalmente especifique uid:gid para ejecutar el contenedor como. <b class='text-warning'>Nota:</b> Esta configuración debe dejarse vacía si el tiempo de ejecución del contenedor es sin raíz o utiliza remapeo de espacio de nombres de usuario");
		m.put("Optionally specify user name to access remote repository", "Opcionalmente especifique el nombre de usuario para acceder al repositorio remoto");
		m.put("Optionally specify valid scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope", 
			"Opcionalmente especifique alcances válidos de commits convencionales (presione ENTER para agregar valor). Deje vacío para permitir alcances arbitrarios");
		m.put("Optionally specify valid types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type", 
			"Opcionalmente especifique tipos válidos de commits convencionales (presione ENTER para agregar valor). Deje vacío para permitir tipos arbitrarios");
		m.put("Optionally specify value of git config <code>pack.packSizeLimit</code> for the repository", 
			"Opcionalmente especifique el valor de la configuración de git <code>pack.packSizeLimit</code> para el repositorio");
		m.put("Optionally specify value of git config <code>pack.threads</code> for the repository", 
			"Opcionalmente especifique el valor de la configuración de git <code>pack.threads</code> para el repositorio");
		m.put("Optionally specify value of git config <code>pack.window</code> for the repository", 
			"Opcionalmente especifique el valor de la configuración de git <code>pack.window</code> para el repositorio");
		m.put("Optionally specify value of git config <code>pack.windowMemory</code> for the repository", 
			"Opcionalmente especifique el valor de la configuración de git <code>pack.windowMemory</code> para el repositorio");
		m.put("Optionally specify where to run service pods specified in job. The first matching locator will be used. If no any locators are found, node selector of the executor will be used", 
			"Opcionalmente especifique dónde ejecutar los pods de servicio especificados en el trabajo. Se utilizará el primer localizador coincidente. Si no se encuentran localizadores, se utilizará el selector de nodos del ejecutor");
		m.put("Optionally specify working directory of the container. Leave empty to use default working directory of the container", 
			"Opcionalmente especifique el directorio de trabajo del contenedor. Deje vacío para usar el directorio de trabajo predeterminado del contenedor");
		m.put("Options", "Opciones");
		m.put("Or manually enter the secret key below in your authenticator app", "O ingrese manualmente la clave secreta a continuación en su aplicación de autenticación");
		m.put("Order By", "Ordenar Por");
		m.put("Order More User Months", "Ordenar Más Meses de Usuario");
		m.put("Order Subscription", "Ordenar Suscripción");
		m.put("Ordered List", "Lista Ordenada");
		m.put("Ordered list", "Lista ordenada");
		m.put("Osv License Scanner", "Escáner de Licencias Osv");
		m.put("Osv Vulnerability Scanner", "Escáner de Vulnerabilidades Osv");
		m.put("Other", "Otro");
		m.put("Outline", "Esquema");
		m.put("Outline Search", "Búsqueda de Esquema");
		m.put("Output", "Salida");
		m.put("Overall", "General");
		m.put("Overall Estimated Time:", "Tiempo Estimado General:");
		m.put("Overall Spent Time:", "Tiempo General Gastado:");
		m.put("Overview", "Resumen");
		m.put("Own:", "Propio:");
		m.put("Ownered By", "Propiedad de");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "La clave privada PEM comienza con '-----BEGIN RSA PRIVATE KEY-----'");
		m.put("PENDING", "PENDIENTE");
		m.put("PMD Report", "Informe PMD");
		m.put("Pack", "Empaquetar");
		m.put("Pack Notification", "Notificación de Empaquetado");
		m.put("Pack Size Limit", "Límite de Tamaño de Empaquetado");
		m.put("Pack Type", "Tipo de Empaquetado");
		m.put("Package", "Paquete");
		m.put("Package Management", "Gestión de Paquetes");
		m.put("Package Notification", "Notificación de Paquete");
		m.put("Package Notification Template", "Plantilla de Notificación de Paquete");
		m.put("Package Privilege", "Privilegio de Paquete");
		m.put("Package Storage", "Almacenamiento de Paquetes");
		m.put("Package list", "Lista de Paquetes");
		m.put("Package {0} deleted", "Paquete {0} eliminado");
		m.put("Packages", "Paquetes");
		m.put("Page Not Found", "Página no encontrada");
		m.put("Page is in error, reload to recover", "La página tiene un error, recargue para recuperar");
		m.put("Param Instance", "Instancia de Parámetro");
		m.put("Param Instances", "Instancias de Parámetro");
		m.put("Param Map", "Mapa de Parámetros");
		m.put("Param Matrix", "Matriz de Parámetros");
		m.put("Param Name", "Nombre de Parámetro");
		m.put("Param Spec", "Especificación de Parámetro");
		m.put("Param Spec Bean", "Bean de Especificación de Parámetro");
		m.put("Parameter", "Parámetro");
		m.put("Parameter Specs", "Especificaciones de Parámetro");
		m.put("Params", "Parámetros");
		m.put("Params & Triggers", "Parámetros y Disparadores");
		m.put("Params to Display", "Parámetros para Mostrar");
		m.put("Parent Bean", "Bean Padre");
		m.put("Parent OneDev Project", "Proyecto Padre de OneDev");
		m.put("Parent Project", "Proyecto Padre");
		m.put("Parent project not found", "Proyecto padre no encontrado");
		m.put("Parents", "Padres");
		m.put("Partially covered", "Parcialmente cubierto");
		m.put("Partially covered by some tests", "Parcialmente cubierto por algunas pruebas");
		m.put("Passcode", "Código de Acceso");
		m.put("Passed", "Aprobado");
		m.put("Password", "Contraseña");
		m.put("Password Authenticator", "Autenticador de Contraseña");
		m.put("Password Edit Bean", "Bean de Edición de Contraseña");
		m.put("Password Must Contain Digit", "La contraseña debe contener un dígito");
		m.put("Password Must Contain Lowercase", "La contraseña debe contener una letra minúscula");
		m.put("Password Must Contain Special Character", "La contraseña debe contener un carácter especial");
		m.put("Password Must Contain Uppercase", "La contraseña debe contener una letra mayúscula");
		m.put("Password Policy", "Política de Contraseña");
		m.put("Password Reset", "Restablecimiento de Contraseña");
		m.put("Password Reset Bean", "Bean de Restablecimiento de Contraseña");
		m.put("Password Reset Template", "Plantilla de Restablecimiento de Contraseña");
		m.put("Password Secret", "Secreto de Contraseña");
		m.put("Password and its confirmation should be identical.", "La contraseña y su confirmación deben ser idénticas.");
		m.put("Password changed. Please login with your new password", "Contraseña cambiada. Por favor inicie sesión con su nueva contraseña");
		m.put("Password has been changed", "La contraseña ha sido cambiada");
		m.put("Password has been removed", "La contraseña ha sido eliminada");
		m.put("Password has been set", "La contraseña ha sido establecida");
		m.put("Password of the user", "Contraseña del usuario");
		m.put("Password or Access Token for Remote Repository", "Contraseña o Token de Acceso para el Repositorio Remoto");
		m.put("Password reset request has been sent", "Se ha enviado la solicitud de restablecimiento de contraseña");
		m.put("Password reset url is invalid or obsolete", "La URL de restablecimiento de contraseña es inválida o obsoleta");
		m.put("PasswordMinimum Length", "Longitud Mínima de la Contraseña");
		m.put("Paste subscription key here", "Pegue la clave de suscripción aquí");
		m.put("Path containing spaces or starting with dash needs to be quoted", "La ruta que contiene espacios o comienza con guion necesita ser entrecomillada");
		m.put("Path placeholder", "Marcador de posición de ruta");
		m.put("Path to kubectl", "Ruta a kubectl");
		m.put("Paths", "Rutas");
		m.put("Pattern", "Patrón");
		m.put("Pause", "Pausa");
		m.put("Pause All Queried Agents", "Pausar Todos los Agentes Consultados");
		m.put("Pause Selected Agents", "Pausar Agentes Seleccionados");
		m.put("Paused", "Pausado");
		m.put("Paused all queried agents", "Se han pausado todos los agentes consultados");
		m.put("Paused selected agents", "Se han pausado los agentes seleccionados");
		m.put("Pem Private Key", "Clave Privada Pem");
		m.put("Pending", "Pendiente");
		m.put("Performance", "Rendimiento");
		m.put("Performance Setting", "Configuración de rendimiento");
		m.put("Performance Settings", "Configuraciones de rendimiento");
		m.put("Performance settings have been saved", "Las configuraciones de rendimiento han sido guardadas");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\"", 
			"Realizando consulta difusa. Encierre el texto de búsqueda con '~' para agregar más condiciones, por ejemplo: ~texto a buscar~ y \"Estado\" es \"Abierto\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"", 
			"Realizando consulta difusa. Encierre el texto de búsqueda con '~' para agregar más condiciones, por ejemplo: ~texto a buscar~ y \"Tipo\" es \"NPM\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online", 
			"Realizando consulta difusa. Encierre el texto de búsqueda con '~' para agregar más condiciones, por ejemplo: ~texto a buscar~ y en línea");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"Realizando consulta difusa. Encierre el texto de búsqueda con '~' para agregar más condiciones, por ejemplo: ~texto a buscar~ y abierto");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and owned by me", 
			"Realizando consulta difusa. Encierre el texto de búsqueda con '~' para agregar más condiciones, por ejemplo: ~texto a buscar~ y propiedad mía");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved", 
			"Realizando consulta difusa. Encierre el texto de búsqueda con '~' para agregar más condiciones, por ejemplo: ~texto a buscar~ y sin resolver");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"Realizando consulta difusa. Encierre el texto de búsqueda con '~' para agregar más condiciones, por ejemplo: ~texto a buscar~ autor(robin)");
		m.put("Permanent link", "Enlace permanente");
		m.put("Permanent link of this selection", "Enlace permanente de esta selección");
		m.put("Permission denied", "Permiso denegado");
		m.put("Permission will be checked upon actual operation", "El permiso será verificado durante la operación real");
		m.put("Physical memory in mega bytes", "Memoria física en megabytes");
		m.put("Pick Existing", "Seleccionar existente");
		m.put("Pin this issue", "Anclar este problema");
		m.put("Pipeline", "Pipeline");
		m.put("Placeholder", "Marcador de posición");
		m.put("Plain text expected", "Se espera texto plano");
		m.put("Platform", "Plataforma");
		m.put("Platforms", "Plataformas");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"Por favor, <a wicket:id=\"download\" class=\"font-weight-bolder\">descargue</a> los códigos de recuperación a continuación y manténgalos en secreto. Estos códigos pueden ser utilizados para proporcionar acceso único a su cuenta en caso de que no pueda acceder a la aplicación de autenticación. No se mostrarán <b>NUNCA</b> nuevamente");
		m.put("Please Confirm", "Por favor confirme");
		m.put("Please Note", "Por favor tenga en cuenta");
		m.put("Please check your email for password reset instructions", "Por favor revise su correo electrónico para las instrucciones de restablecimiento de contraseña");
		m.put("Please choose revision to create branch from", "Por favor elija la revisión para crear la rama");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "Por favor configure primero la <a wicket:id=\"mailSetting\">configuración de correo</a>");
		m.put("Please confirm", "Por favor confirme");
		m.put("Please confirm the password.", "Por favor confirme la contraseña.");
		m.put("Please follow <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">this instruction</a> to resolve the conflicts", 
			"Por favor siga <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">esta instrucción</a> para resolver los conflictos");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"Por favor ingrese uno de sus códigos de recuperación guardados al habilitar la autenticación de dos factores");
		m.put("Please login to perform this operation", "Por favor inicie sesión para realizar esta operación");
		m.put("Please login to perform this query", "Por favor inicie sesión para realizar esta consulta");
		m.put("Please resolve undefined field values below", "Por favor resuelva los valores de campos indefinidos a continuación");
		m.put("Please resolve undefined fields below", "Por favor resuelva los campos indefinidos a continuación");
		m.put("Please resolve undefined states below. Note that if you select to delete an undefined state, all issues with that state will be deleted", 
			"Por favor resuelva los estados indefinidos a continuación. Tenga en cuenta que si selecciona eliminar un estado indefinido, todos los problemas con ese estado serán eliminados");
		m.put("Please select agents to pause", "Por favor seleccione agentes para pausar");
		m.put("Please select agents to remove", "Por favor seleccione agentes para eliminar");
		m.put("Please select agents to restart", "Por favor seleccione agentes para reiniciar");
		m.put("Please select agents to resume", "Por favor seleccione agentes para reanudar");
		m.put("Please select branches to create pull request", "Por favor seleccione ramas para crear una solicitud de extracción");
		m.put("Please select builds to cancel", "Por favor seleccione compilaciones para cancelar");
		m.put("Please select builds to delete", "Por favor seleccione compilaciones para eliminar");
		m.put("Please select builds to re-run", "Por favor seleccione compilaciones para volver a ejecutar");
		m.put("Please select comments to delete", "Por favor seleccione comentarios para eliminar");
		m.put("Please select comments to set resolved", "Por favor seleccione comentarios para marcar como resueltos");
		m.put("Please select comments to set unresolved", "Por favor seleccione comentarios para marcar como no resueltos");
		m.put("Please select different branches", "Por favor seleccione ramas diferentes");
		m.put("Please select fields to update", "Por favor seleccione campos para actualizar");
		m.put("Please select groups to remove from", "Por favor seleccione los grupos de los que eliminar");
		m.put("Please select issues to copy", "Por favor seleccione problemas para copiar");
		m.put("Please select issues to delete", "Por favor seleccione problemas para eliminar");
		m.put("Please select issues to edit", "Por favor seleccione problemas para editar");
		m.put("Please select issues to move", "Por favor seleccione problemas para mover");
		m.put("Please select issues to sync estimated/spent time", "Por favor seleccione problemas para sincronizar tiempo estimado/gastado");
		m.put("Please select packages to delete", "Por favor seleccione paquetes para eliminar");
		m.put("Please select projects to delete", "Por favor seleccione proyectos para eliminar");
		m.put("Please select projects to modify", "Por favor seleccione proyectos para modificar");
		m.put("Please select projects to move", "Por favor seleccione proyectos para mover");
		m.put("Please select pull requests to delete", "Por favor seleccione solicitudes de extracción para eliminar");
		m.put("Please select pull requests to discard", "Por favor seleccione solicitudes de extracción para descartar");
		m.put("Please select pull requests to watch/unwatch", "Por favor seleccione solicitudes de extracción para observar/no observar");
		m.put("Please select query watches to delete", "Por favor seleccione observaciones de consulta para eliminar");
		m.put("Please select revision to create tag from", "Por favor seleccione la revisión para crear una etiqueta");
		m.put("Please select revisions to compare", "Por favor seleccione revisiones para comparar");
		m.put("Please select users to convert to service accounts", "Por favor seleccione usuarios para convertir a cuentas de servicio");
		m.put("Please select users to disable", "Por favor seleccione usuarios para deshabilitar");
		m.put("Please select users to enable", "Por favor seleccione usuarios para habilitar");
		m.put("Please select users to remove from group", "Por favor seleccione los usuarios para eliminar del grupo");
		m.put("Please specify file name above before editing content", "Por favor especifique el nombre del archivo arriba antes de editar el contenido");
		m.put("Please switch to packages page of a particular project for the instructions", "Por favor cambie a la página de paquetes de un proyecto en particular para las instrucciones");
		m.put("Please wait...", "Por favor espere...");
		m.put("Please waiting...", "Por favor esperando...");
		m.put("Plugin metadata not found", "Metadatos del plugin no encontrados");
		m.put("Poll Interval", "Intervalo de sondeo");
		m.put("Populate Tag Mappings", "Rellenar mapeos de etiquetas");
		m.put("Port", "Puerto");
		m.put("Post", "Publicar");
		m.put("Post Build Action", "Acción posterior a la compilación");
		m.put("Post Build Action Bean", "Bean de acción posterior a la compilación");
		m.put("Post Build Actions", "Acciones posteriores a la compilación");
		m.put("Post Url", "URL de publicación");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "Patrón de prefijo");
		m.put("Prefix the title with <code>WIP</code> or <code>[WIP]</code> to mark the pull request as work in progress", 
			"Prefija el título con <code>WIP</code> o <code>[WIP]</code> para marcar la solicitud de extracción como trabajo en progreso");
		m.put("Prepend", "Anteponer");
		m.put("Preserve Days", "Preservar días");
		m.put("Preset Commit Message", "Mensaje de confirmación preestablecido");
		m.put("Preset commit message updated", "Mensaje de confirmación preestablecido actualizado");
		m.put("Press 'y' to get permalink", "Presione 'y' para obtener el enlace permanente");
		m.put("Prev", "Anterior");
		m.put("Prevent Creation", "Prevenir creación");
		m.put("Prevent Deletion", "Prevenir eliminación");
		m.put("Prevent Forced Push", "Prevenir empuje forzado");
		m.put("Prevent Update", "Prevenir actualización");
		m.put("Preview", "Vista previa");
		m.put("Previous", "Anterior");
		m.put("Previous Value", "Valor anterior");
		m.put("Previous commit", "Confirmación anterior");
		m.put("Previous {0}", "Anterior {0}");
		m.put("Primary", "Primario");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "Dirección de correo electrónico <a wicket:id=\"noPrimaryAddressLink\">principal</a> no especificada");
		m.put("Primary Email", "Correo electrónico principal");
		m.put("Primary email address not specified", "Dirección de correo electrónico principal no especificada");
		m.put("Primary email address of your account is not specified yet", "La dirección de correo electrónico principal de tu cuenta aún no está especificada");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"La dirección de correo electrónico principal se utilizará para recibir notificaciones, mostrar gravatar (si está habilitado), etc.");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"La dirección de correo electrónico principal o alias de la cuenta anterior se utilizará como dirección del remitente de varias notificaciones por correo electrónico. El usuario también puede responder a esta dirección para publicar comentarios de problemas o solicitudes de extracción por correo electrónico si la opción <code>Check Incoming Email</code> está habilitada a continuación");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"Nombre principal de la cuenta para iniciar sesión en el servidor de correo de Office 365 para enviar/recibir correos electrónicos. Asegúrate de que esta cuenta <b>posea</b> la aplicación registrada indicada por el id de aplicación anterior");
		m.put("Private Key Secret", "Clave privada secreta");
		m.put("Private key regenerated and SSH server restarted", "Clave privada regenerada y servidor SSH reiniciado");
		m.put("Privilege", "Privilegio");
		m.put("Privilege Settings", "Configuración de privilegios");
		m.put("Product Version", "Versión del producto");
		m.put("Profile", "Perfil");
		m.put("Programming language", "Lenguaje de programación");
		m.put("Project", "Proyecto");
		m.put("Project \"{0}\" deleted", "Proyecto \"{0}\" eliminado");
		m.put("Project Authorization Bean", "Bean de autorización del proyecto");
		m.put("Project Authorizations Bean", "Beans de autorizaciones del proyecto");
		m.put("Project Build Setting", "Configuración de construcción del proyecto");
		m.put("Project Dependencies", "Dependencias del proyecto");
		m.put("Project Dependency", "Dependencia del proyecto");
		m.put("Project Id", "Id del proyecto");
		m.put("Project Import Option", "Opción de importación del proyecto");
		m.put("Project Issue Setting", "Configuración de problemas del proyecto");
		m.put("Project Key", "Clave del proyecto");
		m.put("Project Management", "Gestión del proyecto");
		m.put("Project Pack Setting", "Configuración del paquete del proyecto");
		m.put("Project Path", "Ruta del proyecto");
		m.put("Project Pull Request Setting", "Configuración de solicitudes de extracción del proyecto");
		m.put("Project Replicas", "Réplicas del proyecto");
		m.put("Project authorizations updated", "Autorizaciones del proyecto actualizadas");
		m.put("Project does not have any code yet", "El proyecto aún no tiene ningún código");
		m.put("Project forked", "Proyecto bifurcado");
		m.put("Project id", "Id del proyecto");
		m.put("Project list", "Lista de proyectos");
		m.put("Project manage privilege required to delete \"{0}\"", "Se requiere privilegio de gestión del proyecto para eliminar \"{0}\"");
		m.put("Project manage privilege required to modify \"{0}\"", "Se requiere privilegio de gestión del proyecto para modificar \"{0}\"");
		m.put("Project manage privilege required to move \"{0}\"", "Se requiere privilegio de gestión del proyecto para mover \"{0}\"");
		m.put("Project name", "Nombre del proyecto");
		m.put("Project not specified yet", "El proyecto aún no está especificado");
		m.put("Project or revision not specified yet", "El proyecto o la revisión aún no están especificados");
		m.put("Project overview", "Resumen del proyecto");
		m.put("Project path", "Ruta del proyecto");
		m.put("Projects", "Proyectos");
		m.put("Projects Bean", "Beans de proyectos");
		m.put("Projects deleted", "Proyectos eliminados");
		m.put("Projects modified", "Proyectos modificados");
		m.put("Projects moved", "Proyectos movidos");
		m.put("Projects need to be redistributed when cluster members are added/removed. OneDev does not do this automatically as this is resource intensive, and you may only want to do it after cluster is finalized and stable.", 
			"Los proyectos necesitan ser redistribuidos cuando se agregan/eliminan miembros del clúster. OneDev no hace esto automáticamente ya que es intensivo en recursos, y es posible que solo desees hacerlo después de que el clúster esté finalizado y estable.");
		m.put("Promotions", "Promociones");
		m.put("Prompt Fields", "Campos de solicitud");
		m.put("Properties", "Propiedades");
		m.put("Provide server id (guild id) to restrict access only to server members", "Proporciona el id del servidor (id del gremio) para restringir el acceso solo a los miembros del servidor");
		m.put("Proxy", "Proxy");
		m.put("Prune Builder Cache", "Limpiar caché del constructor");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"Limpiar la caché de imágenes del constructor de docker buildx. Este paso ejecuta el comando docker builder prune para eliminar la caché del constructor buildx especificado en el ejecutor docker del servidor o el ejecutor docker remoto");
		m.put("Public", "Público");
		m.put("Public Key", "Clave pública");
		m.put("Public Roles", "Roles Públicos");
		m.put("Publish", "Publicar");
		m.put("Publish Coverage Report Step", "Paso de publicación del informe de cobertura");
		m.put("Publish Problem Report Step", "Paso de publicación del informe de problemas");
		m.put("Publish Report Step", "Paso de publicación del informe");
		m.put("Publish Unit Test Report Step", "Paso de publicación del informe de pruebas unitarias");
		m.put("Published After", "Publicado después");
		m.put("Published At", "Publicado en");
		m.put("Published Before", "Publicado antes");
		m.put("Published By", "Publicado por");
		m.put("Published By Project", "Publicado por proyecto");
		m.put("Published By User", "Publicado por usuario");
		m.put("Published File", "Archivo publicado");
		m.put("Pull Command", "Comando de extracción");
		m.put("Pull Image", "Extraer imagen");
		m.put("Pull Request", "Solicitud de extracción");
		m.put("Pull Request Branches", "Ramas de solicitud de extracción");
		m.put("Pull Request Description", "Descripción de la solicitud de extracción");
		m.put("Pull Request Filter", "Filtro de solicitud de extracción");
		m.put("Pull Request Management", "Gestión de solicitudes de extracción");
		m.put("Pull Request Markdown Report", "Informe de markdown de solicitud de extracción");
		m.put("Pull Request Notification", "Notificación de solicitud de extracción");
		m.put("Pull Request Notification Template", "Plantilla de notificación de solicitud de extracción");
		m.put("Pull Request Notification Unsubscribed", "Notificación de solicitud de extracción cancelada");
		m.put("Pull Request Notification Unsubscribed Template", "Plantilla de notificación de solicitud de extracción cancelada");
		m.put("Pull Request Settings", "Configuración de solicitud de extracción");
		m.put("Pull Request Statistics", "Estadísticas de solicitud de extracción");
		m.put("Pull Request Title", "Título de solicitud de extracción");
		m.put("Pull Requests", "Solicitudes de extracción");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Extraer imagen de docker como diseño OCI a través de crane. Este paso necesita ser ejecutado por el ejecutor docker del servidor, el ejecutor docker remoto o el ejecutor de Kubernetes");
		m.put("Pull from Remote", "Extraer desde remoto");
		m.put("Pull request", "Solicitud de extracción");
		m.put("Pull request #{0} already closed", "La solicitud de extracción #{0} ya está cerrada");
		m.put("Pull request #{0} deleted", "La solicitud de extracción #{0} eliminada");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"Permiso administrativo de solicitud de extracción dentro de un proyecto, incluyendo operaciones en lote sobre múltiples solicitudes de extracción");
		m.put("Pull request already closed", "La solicitud de extracción ya está cerrada");
		m.put("Pull request already opened", "La solicitud de extracción ya está abierta");
		m.put("Pull request and code review", "Solicitud de extracción y revisión de código");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not finished yet", 
			"La solicitud de extracción no se puede fusionar ahora ya que <a class=\"more-info d-inline link-primary\">algunas construcciones requeridas</a> aún no están terminadas");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not successful", 
			"La solicitud de extracción no se puede fusionar ahora ya que <a class=\"more-info d-inline link-primary\">algunas construcciones requeridas</a> no son exitosas");
		m.put("Pull request can not be merged now as it is <a class=\"more-info d-inline link-primary\">pending review</a>", 
			"La solicitud de extracción no se puede fusionar ahora ya que está <a class=\"more-info d-inline link-primary\">pendiente de revisión</a>");
		m.put("Pull request can not be merged now as it was <a class=\"more-info d-inline link-primary\">requested for changes</a>", 
			"La solicitud de extracción no se puede fusionar ahora ya que se <a class=\"more-info d-inline link-primary\">solicitaron cambios</a>");
		m.put("Pull request can not be merged now as valid signature is required for head commit", 
			"La solicitud de extracción no se puede fusionar ahora ya que se requiere una firma válida para el commit principal");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "La solicitud de extracción solo se puede fusionar después de obtener aprobaciones de todos los revisores");
		m.put("Pull request can only be merged by users with code write permission", "La solicitud de extracción solo puede ser fusionada por usuarios con permiso de escritura de código");
		m.put("Pull request discard", "Descartar solicitud de extracción");
		m.put("Pull request duration statistics", "Estadísticas de duración de solicitudes de extracción");
		m.put("Pull request frequency statistics", "Estadísticas de frecuencia de solicitudes de extracción");
		m.put("Pull request is discarded", "La solicitud de extracción se descarta");
		m.put("Pull request is in error: {0}", "La solicitud de extracción tiene un error: {0}");
		m.put("Pull request is merged", "La solicitud de extracción se fusiona");
		m.put("Pull request is opened", "La solicitud de extracción se abre");
		m.put("Pull request is still a work in progress", "La solicitud de extracción aún está en progreso");
		m.put("Pull request is work in progress", "La solicitud de extracción está en progreso");
		m.put("Pull request list", "Lista de solicitudes de extracción");
		m.put("Pull request merge", "Fusión de solicitudes de extracción");
		m.put("Pull request not exist or access denied", "La solicitud de extracción no existe o el acceso está denegado");
		m.put("Pull request not merged", "La solicitud de extracción no se fusiona");
		m.put("Pull request number", "Número de solicitud de extracción");
		m.put("Pull request open or update", "Abrir o actualizar solicitud de extracción");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"La consulta de solicitudes de extracción solo afecta a nuevas solicitudes. Para gestionar el estado de observación de solicitudes existentes en lote, filtra las solicitudes por estado de observación en la página de solicitudes y luego toma la acción adecuada");
		m.put("Pull request settings updated", "Configuraciones de solicitudes de extracción actualizadas");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Las estadísticas de solicitudes de extracción son una función empresarial. <a href='https://onedev.io/pricing' target='_blank'>Prueba gratis</a> durante 30 días");
		m.put("Pull request synchronization submitted", "Sincronización de solicitudes de extracción enviada");
		m.put("Pull request will be merged automatically when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"La solicitud de extracción se fusionará automáticamente cuando esté lista. Esta opción se desactivará al agregar nuevos commits, cambiar la estrategia de fusión o cambiar la rama objetivo");
		m.put("Pull request will be merged automatically with a preset <a wicket:id=\"commitMessage\">commit message</a> when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"La solicitud de extracción se fusionará automáticamente con un <a wicket:id=\"commitMessage\">mensaje de commit</a> preestablecido cuando esté lista. Esta opción se desactivará al agregar nuevos commits, cambiar la estrategia de fusión o cambiar la rama objetivo");
		m.put("Push Image", "Empujar imagen");
		m.put("Push chart to the repository", "Empujar gráfico al repositorio");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Empujar imagen de Docker desde el diseño OCI a través de crane. Este paso debe ejecutarse por el ejecutor de Docker del servidor, el ejecutor de Docker remoto o el ejecutor de Kubernetes");
		m.put("Push to Remote", "Empujar a remoto");
		m.put("Push to container registry", "Empujar al registro de contenedores");
		m.put("PyPI(s)", "PyPI(s)");
		m.put("Pylint Report", "Informe de Pylint");
		m.put("Queries", "Consultas");
		m.put("Query", "Consulta");
		m.put("Query Parameters", "Parámetros de consulta");
		m.put("Query Watches", "Observaciones de consulta");
		m.put("Query commits", "Consultar commits");
		m.put("Query not submitted", "Consulta no enviada");
		m.put("Query param", "Parámetro de consulta");
		m.put("Query/order agents", "Consultar/ordenar agentes");
		m.put("Query/order builds", "Consultar/ordenar compilaciones");
		m.put("Query/order comments", "Consultar/ordenar comentarios");
		m.put("Query/order issues", "Consultar/ordenar problemas");
		m.put("Query/order packages", "Consultar/ordenar paquetes");
		m.put("Query/order projects", "Consultar/ordenar proyectos");
		m.put("Query/order pull requests", "Consultar/ordenar solicitudes de extracción");
		m.put("Queueing Takes", "Tiempo de espera");
		m.put("Quick Search", "Búsqueda rápida");
		m.put("Quote", "Cita");
		m.put("RESTful API", "API RESTful");
		m.put("RESTful API Help", "Ayuda de API RESTful");
		m.put("Ran On Agent", "Ejecutado en agente");
		m.put("Re-run All Queried Builds", "Reejecutar todas las compilaciones consultadas");
		m.put("Re-run Selected Builds", "Reejecutar compilaciones seleccionadas");
		m.put("Re-run request submitted", "Solicitud de reejecución enviada");
		m.put("Re-run this build", "Reejecutar esta compilación");
		m.put("Read", "Leer");
		m.put("Read body", "Leer cuerpo");
		m.put("Readiness Check Command", "Comando de verificación de preparación");
		m.put("Really want to delete this code comment?", "¿Realmente quieres eliminar este comentario de código?");
		m.put("Rebase", "Rebase");
		m.put("Rebase Source Branch Commits", "Rebase de commits de la rama fuente");
		m.put("Rebase all commits from source branch onto target branch", "Rebase de todos los commits de la rama fuente en la rama objetivo");
		m.put("Rebase source branch commits", "Rebase de commits de la rama fuente");
		m.put("Rebuild manually", "Reconstruir manualmente");
		m.put("Receive Posted Email", "Recibir correo electrónico publicado");
		m.put("Received test mail", "Correo de prueba recibido");
		m.put("Receivers", "Receptores");
		m.put("Recovery code", "Código de recuperación");
		m.put("Recursive", "Recursivo");
		m.put("Redundant", "Redundante");
		m.put("Ref", "Ref");
		m.put("Ref Name", "Nombre de referencia");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup", 
			"Consulta este <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> para un ejemplo de configuración");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> for an example setup", 
			"Consulta este <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> para un ejemplo de configuración");
		m.put("Reference", "Referencia");
		m.put("Reference Build", "Compilación de referencia");
		m.put("Reference Issue", "Problema de referencia");
		m.put("Reference Pull Request", "Solicitud de extracción de referencia");
		m.put("Reference this {0} in markdown or commit message via below string.", "Referencia este {0} en markdown o mensaje de commit mediante la cadena a continuación.");
		m.put("Refresh", "Actualizar");
		m.put("Refresh Token", "Token de actualización");
		m.put("Refs", "Refs");
		m.put("Regenerate", "Regenerar");
		m.put("Regenerate Private Key", "Regenerar clave privada");
		m.put("Regenerate this access token", "Regenerar este token de acceso");
		m.put("Registry Login", "Inicio de sesión en el registro");
		m.put("Registry Logins", "Inicios de sesión en el registro");
		m.put("Registry Url", "URL del registro");
		m.put("Regular Expression", "Expresión regular");
		m.put("Remaining User Months", "Meses de usuario restantes");
		m.put("Remaining User Months:", "Meses de usuario restantes:");
		m.put("Remaining time", "Tiempo restante");
		m.put("Remember Me", "Recuérdame");
		m.put("Remote Docker Executor", "Ejecutor de Docker remoto");
		m.put("Remote Machine", "Máquina remota");
		m.put("Remote Shell Executor", "Ejecutor de shell remoto");
		m.put("Remote URL", "URL remota");
		m.put("Remote Url", "URL remota");
		m.put("Remove", "Eliminar");
		m.put("Remove All Queried Agents", "Eliminar todos los agentes consultados");
		m.put("Remove All Queried Users from Group", "Eliminar todos los usuarios consultados del grupo");
		m.put("Remove Fields", "Eliminar campos");
		m.put("Remove From Current Iteration", "Eliminar de la iteración actual");
		m.put("Remove Selected Agents", "Eliminar agentes seleccionados");
		m.put("Remove Selected Users from Group", "Eliminar usuarios seleccionados del grupo");
		m.put("Remove from All Queried Groups", "Eliminar de todos los grupos consultados");
		m.put("Remove from Selected Groups", "Eliminar de los grupos seleccionados");
		m.put("Remove from batch", "Eliminar del lote");
		m.put("Remove issue from this iteration", "Eliminar problema de esta iteración");
		m.put("Remove this assignee", "Eliminar este asignado");
		m.put("Remove this external participant from issue", "Eliminar este participante externo del problema");
		m.put("Remove this file", "Eliminar este archivo");
		m.put("Remove this image", "Eliminar esta imagen");
		m.put("Remove this reviewer", "Eliminar este revisor");
		m.put("Removed all queried agents. Type <code>yes</code> below to confirm", "Se eliminaron todos los agentes consultados. Escriba <code>yes</code> abajo para confirmar");
		m.put("Removed selected agents. Type <code>yes</code> below to confirm", "Se eliminaron los agentes seleccionados. Escriba <code>yes</code> abajo para confirmar");
		m.put("Rename {0}", "Renombrar {0}");
		m.put("Renew Subscription", "Renovar Suscripción");
		m.put("Renovate CLI Options", "Opciones de Renovate CLI");
		m.put("Renovate JavaScript Config", "Configuración de Renovate JavaScript");
		m.put("Reopen", "Reabrir");
		m.put("Reopen this iteration", "Reabrir esta iteración");
		m.put("Reopened pull request \"{0}\" ({1})", "Solicitud de extracción reabierta \"{0}\" ({1})");
		m.put("Replace With", "Reemplazar con");
		m.put("Replica Count", "Conteo de réplicas");
		m.put("Replicas", "Réplicas");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "Respondió al comentario en el archivo \"{0}\" en el proyecto \"{1}\"");
		m.put("Reply", "Responder");
		m.put("Report Name", "Nombre del informe");
		m.put("Report format changed. You may re-run this build to generate the report in new format", 
			"Formato del informe cambiado. Puede volver a ejecutar esta compilación para generar el informe en el nuevo formato");
		m.put("Repository Sync", "Sincronización del repositorio");
		m.put("Request Body", "Cuerpo de la solicitud");
		m.put("Request For Changes", "Solicitud de cambios");
		m.put("Request Scopes", "Ámbitos de solicitud");
		m.put("Request Trial Subscription", "Solicitar suscripción de prueba");
		m.put("Request review", "Solicitar revisión");
		m.put("Request to sync", "Solicitar sincronización");
		m.put("Requested For changes", "Solicitado para cambios");
		m.put("Requested changes to pull request \"{0}\" ({1})", "Solicitó cambios en la solicitud de extracción \"{0}\" ({1})");
		m.put("Requested for changes", "Solicitado para cambios");
		m.put("Requested to sync estimated/spent time", "Solicitado sincronizar tiempo estimado/gastado");
		m.put("Require Autentication", "Requiere autenticación");
		m.put("Require Strict Pull Request Builds", "Requiere compilaciones estrictas de solicitudes de extracción");
		m.put("Require Successful", "Requiere éxito");
		m.put("Required", "Requerido");
		m.put("Required Builds", "Compilaciones requeridas");
		m.put("Required Reviewers", "Revisores requeridos");
		m.put("Required Services", "Servicios requeridos");
		m.put("Resend Verification Email", "Reenviar correo electrónico de verificación");
		m.put("Resend invitation", "Reenviar invitación");
		m.put("Reset", "Restablecer");
		m.put("Resolution", "Resolución");
		m.put("Resolved", "Resuelto");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "Comentario resuelto en el archivo \"{0}\" en el proyecto \"{1}\"");
		m.put("Resource", "Recurso");
		m.put("Resource Settings", "Configuraciones de recursos");
		m.put("Resources", "Recursos");
		m.put("Response", "Respuesta");
		m.put("Response Body", "Cuerpo de la respuesta");
		m.put("Restart", "Reiniciar");
		m.put("Restart All Queried Agents", "Reiniciar todos los agentes consultados");
		m.put("Restart Selected Agents", "Reiniciar agentes seleccionados");
		m.put("Restart command issued", "Comando de reinicio emitido");
		m.put("Restart command issued to all queried agents", "Comando de reinicio emitido a todos los agentes consultados");
		m.put("Restart command issued to selected agents", "Comando de reinicio emitido a los agentes seleccionados");
		m.put("Restore Source Branch", "Restaurar rama fuente");
		m.put("Restored source branch", "Rama fuente restaurada");
		m.put("Resubmitted manually", "Reenviado manualmente");
		m.put("Resume", "Reanudar");
		m.put("Resume All Queried Agents", "Reanudar todos los agentes consultados");
		m.put("Resume Selected Agents", "Reanudar agentes seleccionados");
		m.put("Resumed all queried agents", "Se reanudaron todos los agentes consultados");
		m.put("Resumed selected agents", "Se reanudaron los agentes seleccionados");
		m.put("Retried At", "Reintentado en");
		m.put("Retrieve Groups", "Recuperar grupos");
		m.put("Retrieve LFS Files", "Recuperar archivos LFS");
		m.put("Retrieve Submodules", "Recuperar submódulos");
		m.put("Retry Condition", "Condición de reintento");
		m.put("Retry Delay", "Retraso de reintento");
		m.put("Revert", "Revertir");
		m.put("Reverted successfully", "Revertido exitosamente");
		m.put("Review required for deletion. Submit pull request instead", "Revisión requerida para eliminación. Envíe una solicitud de extracción en su lugar");
		m.put("Review required for this change. Please submit pull request instead", "Revisión requerida para este cambio. Por favor, envía una solicitud de extracción en su lugar.");
		m.put("Review required for this change. Submit pull request instead", "Revisión requerida para este cambio. Envíe una solicitud de extracción en su lugar");
		m.put("Reviewers", "Revisores");
		m.put("Revision", "Revisión");
		m.put("Revision indexing in progress...", "Indexación de revisión en progreso...");
		m.put("Revision indexing in progress... (symbol navigation in revisions will be accurate after indexed)", 
			"Indexación de revisión en progreso... (la navegación por símbolos en las revisiones será precisa después de indexada)");
		m.put("Right", "Derecha");
		m.put("Role", "Rol");
		m.put("Role \"{0}\" deleted", "Rol \"{0}\" eliminado");
		m.put("Role \"{0}\" updated", "Rol \"{0}\" actualizado");
		m.put("Role Management", "Gestión de roles");
		m.put("Role created", "Rol creado");
		m.put("Roles", "Roles");
		m.put("Root Projects", "Proyectos raíz");
		m.put("Roslynator Report", "Informe de Roslynator");
		m.put("RubyGems(s)", "RubyGems(s)");
		m.put("Ruff Report", "Informe de Ruff");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "La regla se aplicará si el usuario que opera la etiqueta cumple con los criterios especificados aquí");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"La regla se aplicará solo si el usuario que cambia la rama cumple con los criterios especificados aquí");
		m.put("Run As", "Ejecutar como");
		m.put("Run Buildx Image Tools", "Ejecutar herramientas de imagen Buildx");
		m.put("Run Docker Container", "Ejecutar contenedor Docker");
		m.put("Run In Container", "Ejecutar en contenedor");
		m.put("Run Integrity Check", "Ejecutar verificación de integridad");
		m.put("Run Job", "Ejecutar trabajo");
		m.put("Run Options", "Opciones de ejecución");
		m.put("Run below commands from within your git repository:", "Ejecute los siguientes comandos desde dentro de su repositorio git:");
		m.put("Run below commands to install this gem", "Ejecute los siguientes comandos para instalar este gem");
		m.put("Run below commands to install this package", "Ejecute los siguientes comandos para instalar este paquete");
		m.put("Run below commands to use this chart", "Ejecute los siguientes comandos para usar este gráfico");
		m.put("Run below commands to use this package", "Ejecute los siguientes comandos para usar este paquete");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"Ejecute el comando docker buildx imagetools con los argumentos especificados. Este paso solo puede ser ejecutado por el ejecutor docker del servidor o el ejecutor docker remoto");
		m.put("Run job", "Ejecutar trabajo");
		m.put("Run job in another project", "Ejecutar trabajo en otro proyecto");
		m.put("Run on Bare Metal/Virtual Machine", "Ejecutar en Metal Desnudo/Máquina Virtual");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"Ejecute el escáner osv para escanear licencias violadas utilizadas por varias <a href='https://deps.dev/' target='_blank'>dependencias</a>. Solo puede ser ejecutado por un ejecutor compatible con Docker.");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"Ejecute el escáner osv para escanear vulnerabilidades en <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>varios archivos de bloqueo</a>. Solo puede ser ejecutado por un ejecutor compatible con Docker.");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"Ejecute el contenedor Docker especificado. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>El espacio de trabajo del trabajo</a> se monta en el contenedor y su ruta se coloca en la variable de entorno <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Nota: </b> este paso solo puede ser ejecutado por el ejecutor docker del servidor o el ejecutor docker remoto");
		m.put("Run specified step template", "Ejecutar plantilla de paso especificada");
		m.put("Run this job", "Ejecutar este trabajo");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by docker aware executor.", 
			"Ejecuta el escáner de imágenes de contenedor trivy para encontrar problemas en la imagen especificada. Para vulnerabilidades, verifica varios <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>archivos de distribución</a>. Solo puede ser ejecutado por un ejecutor con conocimiento de docker.");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"Ejecuta el escáner de sistema de archivos trivy para analizar varios <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>archivos de bloqueo</a>. Solo puede ser ejecutado por un ejecutor compatible con Docker y se recomienda ejecutarlo <span class='text-warning'>después de resolver las dependencias</span> (npm install o similar). En comparación con el escáner OSV, su configuración es un poco más detallada, pero puede proporcionar resultados más precisos.");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"Ejecuta el escáner rootfs trivy para analizar varios <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>archivos de distribución</a>. Solo puede ser ejecutado por un ejecutor compatible con Docker y se recomienda ejecutarlo en el área de preparación de tu proyecto.");
		m.put("Run via Docker Container", "Ejecutar mediante contenedor Docker.");
		m.put("Running", "Ejecutando.");
		m.put("Running Takes", "Ejecutando toma.");
		m.put("SLOC on {0}", "SLOC en {0}.");
		m.put("SMTP Host", "Host SMTP.");
		m.put("SMTP Password", "Contraseña SMTP.");
		m.put("SMTP User", "Usuario SMTP.");
		m.put("SMTP/IMAP", "SMTP/IMAP.");
		m.put("SSH", "SSH.");
		m.put("SSH & GPG Keys", "Claves SSH y GPG.");
		m.put("SSH Clone URL", "URL de clonación SSH.");
		m.put("SSH Keys", "Claves SSH.");
		m.put("SSH Root URL", "URL raíz SSH.");
		m.put("SSH Server Key", "Clave del servidor SSH.");
		m.put("SSH key deleted", "Clave SSH eliminada.");
		m.put("SSH settings have been saved and SSH server restarted", "La configuración SSH se ha guardado y el servidor SSH se ha reiniciado.");
		m.put("SSL Setting", "Configuración SSL.");
		m.put("SSO Accounts", "Cuentas SSO");
		m.put("SSO Providers", "Proveedores SSO");
		m.put("SSO account deleted", "Cuenta SSO eliminada");
		m.put("SSO provider \"{0}\" deleted", "Proveedor SSO \"{0}\" eliminado");
		m.put("SSO provider created", "Proveedor SSO creado");
		m.put("SSO provider updated", "Proveedor SSO actualizado");
		m.put("SUCCESSFUL", "EXITOSO.");
		m.put("Save", "Guardar.");
		m.put("Save Query", "Guardar consulta.");
		m.put("Save Query Bean", "Guardar consulta Bean.");
		m.put("Save Settings", "Guardar configuración.");
		m.put("Save Settings & Redistribute Projects", "Guardar configuración y redistribuir proyectos.");
		m.put("Save Template", "Guardar plantilla.");
		m.put("Save as Mine", "Guardar como mío.");
		m.put("Saved Queries", "Consultas guardadas.");
		m.put("Scan Path", "Ruta de escaneo.");
		m.put("Scan Paths", "Rutas de escaneo.");
		m.put("Scan below QR code with your TOTP authenticators", "Escanea el código QR a continuación con tus autenticadores TOTP.");
		m.put("Schedule Issues", "Programar problemas.");
		m.put("Script Name", "Nombre del script.");
		m.put("Scripting Value", "Valor del script.");
		m.put("Search", "Buscar.");
		m.put("Search For", "Buscar por.");
		m.put("Search Groups Using Filter", "Buscar grupos usando filtro.");
		m.put("Search branch", "Buscar rama.");
		m.put("Search files, symbols and texts", "Buscar archivos, símbolos y textos.");
		m.put("Search for", "Buscar por.");
		m.put("Search inside current tree", "Buscar dentro del árbol actual");
		m.put("Search is too general", "La búsqueda es demasiado general.");
		m.put("Search job", "Buscar trabajo.");
		m.put("Search project", "Buscar proyecto.");
		m.put("Secret", "Secreto.");
		m.put("Secret Config File", "Archivo de configuración secreta.");
		m.put("Secret Setting", "Configuración secreta.");
		m.put("Security", "Seguridad.");
		m.put("Security & Compliance", "Seguridad y cumplimiento.");
		m.put("Security Setting", "Configuración de seguridad.");
		m.put("Security Settings", "Configuraciones de seguridad.");
		m.put("Security settings have been updated", "Las configuraciones de seguridad se han actualizado.");
		m.put("Select", "Seleccionar.");
		m.put("Select Branch to Cherry Pick to", "Seleccionar rama para aplicar cherry-pick.");
		m.put("Select Branch to Revert on", "Seleccionar rama para revertir.");
		m.put("Select Branch/Tag", "Seleccionar rama/etiqueta.");
		m.put("Select Existing", "Seleccionar existente.");
		m.put("Select Job", "Seleccionar trabajo");
		m.put("Select Project", "Seleccionar proyecto.");
		m.put("Select below...", "Seleccionar a continuación...");
		m.put("Select iteration to schedule issues into", "Seleccionar iteración para programar problemas.");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"Seleccionar organización para importar. Dejar vacío para importar desde repositorios bajo la cuenta actual.");
		m.put("Select project and revision first", "Seleccionar primero proyecto y revisión.");
		m.put("Select project first", "Seleccionar primero proyecto.");
		m.put("Select project to import from", "Seleccionar proyecto para importar.");
		m.put("Select project to sync to. Leave empty to sync to current project", "Seleccionar proyecto para sincronizar. Dejar vacío para sincronizar con el proyecto actual.");
		m.put("Select repository to import from", "Seleccionar repositorio para importar.");
		m.put("Select users to send alert email upon events such as database auto-backup failure, cluster node unreachable etc", 
			"Seleccionar usuarios para enviar correos de alerta ante eventos como fallos de auto-respaldo de base de datos, nodo de clúster inalcanzable, etc.");
		m.put("Select workspace to import from", "Seleccionar espacio de trabajo para importar.");
		m.put("Send Notifications", "Enviar notificaciones.");
		m.put("Send Pull Request", "Enviar solicitud de extracción.");
		m.put("Send notification", "Enviar notificación.");
		m.put("SendGrid", "SendGrid.");
		m.put("Sendgrid Webhook Setting", "Configuración de webhook de SendGrid.");
		m.put("Sending invitation to \"{0}\"...", "Enviando invitación a \"{0}\"...");
		m.put("Sending test mail to {0}...", "Enviando correo de prueba a {0}...");
		m.put("Sequential Group", "Grupo secuencial.");
		m.put("Server", "Servidor.");
		m.put("Server Docker Executor", "Ejecutor Docker del servidor.");
		m.put("Server Id", "ID del servidor.");
		m.put("Server Information", "Información del servidor.");
		m.put("Server Log", "Registro del servidor.");
		m.put("Server Setup", "Configuración del servidor.");
		m.put("Server Shell Executor", "Ejecutor de shell del servidor.");
		m.put("Server URL", "URL del servidor.");
		m.put("Server fingerprint", "Huella digital del servidor.");
		m.put("Server host", "Host del servidor.");
		m.put("Server is Starting...", "El servidor está iniciando...");
		m.put("Server url", "URL del servidor.");
		m.put("Service", "Servicio.");
		m.put("Service Account", "Cuenta de servicio.");
		m.put("Service Desk", "Mesa de servicio.");
		m.put("Service Desk Email Address", "Dirección de correo electrónico de la mesa de servicio.");
		m.put("Service Desk Issue Open Failed", "Error al abrir problema en la mesa de servicio.");
		m.put("Service Desk Issue Open Failed Template", "Plantilla de error al abrir problema en la mesa de servicio.");
		m.put("Service Desk Issue Opened", "Problema abierto en la mesa de servicio.");
		m.put("Service Desk Issue Opened Template", "Plantilla de problema abierto en la mesa de servicio.");
		m.put("Service Desk Setting", "Configuración de la mesa de servicio.");
		m.put("Service Desk Setting Holder", "Portador de configuración de la mesa de servicio.");
		m.put("Service Desk Settings", "Configuraciones de la mesa de servicio.");
		m.put("Service Locator", "Localizador de servicio.");
		m.put("Service Locators", "Localizadores de servicio.");
		m.put("Service account not allowed to login", "Cuenta de servicio no permitida para iniciar sesión");
		m.put("Service desk setting", "Configuración del servicio de mesa");
		m.put("Service desk settings have been saved", "Las configuraciones del servicio de mesa han sido guardadas");
		m.put("Services", "Servicios");
		m.put("Session Timeout", "Tiempo de Espera de Sesión");
		m.put("Set", "Establecer");
		m.put("Set All Queried As Root Projects", "Establecer todos los proyectos consultados como proyectos raíz");
		m.put("Set All Queried Comments as Read", "Marcar todos los comentarios consultados como leídos");
		m.put("Set All Queried Comments as Resolved", "Marcar todos los comentarios consultados como resueltos");
		m.put("Set All Queried Comments as Unresolved", "Marcar todos los comentarios consultados como no resueltos");
		m.put("Set All Queried Issues as Read", "Marcar todos los problemas consultados como leídos");
		m.put("Set All Queried Pull Requests as Read", "Marcar todas las solicitudes de extracción consultadas como leídas");
		m.put("Set As Primary", "Establecer como principal");
		m.put("Set Build Description", "Establecer descripción de compilación");
		m.put("Set Build Version", "Establecer versión de compilación");
		m.put("Set Resolved", "Marcar como resuelto");
		m.put("Set Selected As Root Projects", "Establecer los proyectos seleccionados como proyectos raíz");
		m.put("Set Selected Comments as Resolved", "Marcar los comentarios seleccionados como resueltos");
		m.put("Set Selected Comments as Unresolved", "Marcar los comentarios seleccionados como no resueltos");
		m.put("Set Unresolved", "Marcar como no resuelto");
		m.put("Set Up Cache", "Configurar caché");
		m.put("Set Up Renovate Cache", "Configurar caché de Renovate");
		m.put("Set Up Trivy Cache", "Configurar caché de Trivy");
		m.put("Set Up Your Account", "Configure su cuenta");
		m.put("Set as Private", "Establecer como privado");
		m.put("Set as Public", "Establecer como público");
		m.put("Set description", "Establecer descripción");
		m.put("Set reviewed", "Marcar como revisado");
		m.put("Set unreviewed", "Marcar como no revisado");
		m.put("Set up Microsoft Teams notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url. ", 
			"Configurar las notificaciones de Microsoft Teams. Las configuraciones serán heredadas por los proyectos hijos y pueden ser sobrescritas definiendo configuraciones con la misma URL de webhook.");
		m.put("Set up discord notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurar las notificaciones de Discord. Las configuraciones serán heredadas por los proyectos hijos y pueden ser sobrescritas definiendo configuraciones con la misma URL de webhook.");
		m.put("Set up job cache to speed up job execution. Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> on how to use job cache", 
			"Configurar caché de trabajos para acelerar la ejecución de trabajos. Consulta <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>este tutorial</a> sobre cómo usar el caché de trabajos.");
		m.put("Set up ntfy.sh notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurar las notificaciones de ntfy.sh. Las configuraciones serán heredadas por los proyectos hijos y pueden ser sobrescritas definiendo configuraciones con la misma URL de webhook.");
		m.put("Set up slack notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurar las notificaciones de Slack. Las configuraciones serán heredadas por los proyectos hijos y pueden ser sobrescritas definiendo configuraciones con la misma URL de webhook.");
		m.put("Set up two-factor authentication", "Configurar autenticación de dos factores");
		m.put("Setting", "Configuración");
		m.put("Setting has been saved", "La configuración ha sido guardada");
		m.put("Settings", "Configuraciones");
		m.put("Settings and permissions of parent project will be inherited by this project", "Las configuraciones y permisos del proyecto padre serán heredados por este proyecto");
		m.put("Settings saved", "Configuraciones guardadas");
		m.put("Settings saved and project redistribution scheduled", "Configuraciones guardadas y redistribución del proyecto programada");
		m.put("Settings updated", "Configuraciones actualizadas");
		m.put("Share dashboard", "Compartir tablero");
		m.put("Share with Groups", "Compartir con grupos");
		m.put("Share with Users", "Compartir con usuarios");
		m.put("Shell", "Shell");
		m.put("Show Archived", "Mostrar archivados");
		m.put("Show Branch/Tag", "Mostrar rama/etiqueta");
		m.put("Show Build Status", "Mostrar estado de compilación");
		m.put("Show Closed", "Mostrar cerrados");
		m.put("Show Code Stats", "Mostrar estadísticas de código");
		m.put("Show Command", "Mostrar comando");
		m.put("Show Condition", "Mostrar condición");
		m.put("Show Conditionally", "Mostrar condicionalmente");
		m.put("Show Description", "Mostrar descripción");
		m.put("Show Duration", "Mostrar duración");
		m.put("Show Emojis", "Mostrar emojis");
		m.put("Show Error Detail", "Mostrar detalle de error");
		m.put("Show Issue Status", "Mostrar estado de problemas");
		m.put("Show Package Stats", "Mostrar estadísticas de paquetes");
		m.put("Show Pull Request Stats", "Mostrar estadísticas de solicitudes de extracción");
		m.put("Show Saved Queries", "Mostrar consultas guardadas");
		m.put("Show States By", "Mostrar estados por");
		m.put("Show Works Of", "Mostrar trabajos de");
		m.put("Show changes", "Mostrar cambios");
		m.put("Show commented code snippet", "Mostrar fragmento de código comentado");
		m.put("Show commit of this parent", "Mostrar commit de este padre");
		m.put("Show emojis", "Mostrar emojis");
		m.put("Show in build list", "Mostrar en la lista de compilaciones");
		m.put("Show issues in list", "Mostrar problemas en la lista");
		m.put("Show issues not scheduled into current iteration", "Mostrar problemas no programados en la iteración actual");
		m.put("Show matching agents", "Mostrar agentes coincidentes");
		m.put("Show more", "Mostrar más");
		m.put("Show more lines", "Mostrar más líneas");
		m.put("Show next match", "Mostrar siguiente coincidencia");
		m.put("Show previous match", "Mostrar coincidencia anterior");
		m.put("Show test cases of this test suite", "Mostrar casos de prueba de esta suite de pruebas");
		m.put("Show total estimated/spent time", "Mostrar tiempo total estimado/gastado");
		m.put("Showing first {0} files as there are too many", "Mostrando los primeros {0} archivos ya que hay demasiados");
		m.put("Sign In", "Iniciar sesión");
		m.put("Sign In To", "Iniciar sesión en");
		m.put("Sign Out", "Cerrar sesión");
		m.put("Sign Up", "Registrarse");
		m.put("Sign Up Bean", "Registro Bean");
		m.put("Sign Up!", "¡Regístrate!");
		m.put("Sign in", "Iniciar sesión");
		m.put("Signature required for this change, but no signing key is specified", "Se requiere firma para este cambio, pero no se especifica una clave de firma");
		m.put("Signature required for this change, please generate system GPG signing key first", "Se requiere firma para este cambio, por favor genera primero la clave de firma GPG del sistema");
		m.put("Signature verified successfully with OneDev GPG key", "Firma verificada exitosamente con la clave GPG de OneDev");
		m.put("Signature verified successfully with committer's GPG key", "Firma verificada exitosamente con la clave GPG del autor");
		m.put("Signature verified successfully with committer's SSH key", "Firma verificada exitosamente con la clave SSH del autor");
		m.put("Signature verified successfully with tagger's GPG key", "Firma verificada exitosamente con la clave GPG del etiquetador");
		m.put("Signature verified successfully with tagger's SSH key", "Firma verificada exitosamente con la clave SSH del etiquetador");
		m.put("Signature verified successfully with trusted GPG key", "Firma verificada exitosamente con la clave GPG de confianza");
		m.put("Signed with an unknown GPG key ", "Firmado con una clave GPG desconocida");
		m.put("Signed with an unknown ssh key", "Firmado con una clave ssh desconocida");
		m.put("Signer Email Addresses", "Direcciones de correo electrónico del firmante");
		m.put("Signing Key ID", "ID de clave de firma");
		m.put("Similar Issues", "Problemas similares");
		m.put("Single Sign On", "Inicio de sesión único");
		m.put("Single Sign-On", "Inicio de Sesión Único");
		m.put("Single sign on via discord.com", "Inicio de sesión único a través de discord.com");
		m.put("Single sign on via twitch.tv", "Inicio de sesión único a través de twitch.tv");
		m.put("Site", "Sitio");
		m.put("Size", "Tamaño");
		m.put("Size invalid", "Tamaño inválido");
		m.put("Slack Notifications", "Notificaciones de Slack");
		m.put("Smtp Ssl Setting", "Configuración de Smtp Ssl");
		m.put("Smtp With Ssl", "Smtp con Ssl");
		m.put("Some builds are {0}", "Algunas compilaciones están {0}");
		m.put("Some jobs are hidden due to permission policy", "Algunos trabajos están ocultos debido a la política de permisos");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "Alguien cambió el contenido que estás editando. Recarga la página e inténtalo de nuevo.");
		m.put("Some other pull requests are opening to this branch", "Algunas otras solicitudes de extracción están abiertas para esta rama");
		m.put("Some projects might be hidden due to permission policy", "Algunos proyectos podrían estar ocultos debido a la política de permisos");
		m.put("Some related commits of the code comment is missing", "Faltan algunos commits relacionados con el comentario de código");
		m.put("Some related commits of the pull request are missing", "Faltan algunos commits relacionados con la solicitud de extracción");
		m.put("Some required builds not passed", "Algunas compilaciones requeridas no pasaron");
		m.put("Someone made below change since you started editing", "Alguien hizo el siguiente cambio desde que comenzaste a editar");
		m.put("Sort", "Ordenar");
		m.put("Source", "Fuente");
		m.put("Source Docker Image", "Imagen Docker de origen");
		m.put("Source Lines", "Líneas de origen");
		m.put("Source Path", "Ruta de origen");
		m.put("Source branch already exists", "La rama de origen ya existe");
		m.put("Source branch already merged into target branch", "La rama de origen ya se ha fusionado con la rama objetivo");
		m.put("Source branch commits will be rebased onto target branch", "Los commits de la rama de origen se rebasarán en la rama objetivo");
		m.put("Source branch is default branch", "La rama de origen es la rama predeterminada");
		m.put("Source branch is outdated", "La rama de origen está desactualizada");
		m.put("Source branch no longer exists", "La rama de origen ya no existe");
		m.put("Source branch updated successfully", "La rama de origen se actualizó correctamente");
		m.put("Source project no longer exists", "El proyecto de origen ya no existe");
		m.put("Specified Value", "Valor especificado");
		m.put("Specified choices", "Opciones especificadas");
		m.put("Specified default value", "Valor predeterminado especificado");
		m.put("Specified fields", "Campos especificados");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Especifica la URL LDAP del servidor de Active Directory, por ejemplo: <i>ldap://ad-server</i>, o <i>ldaps://ad-server</i>. En caso de que tu servidor LDAP esté utilizando un certificado autofirmado para la conexión ldaps, necesitarás <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configurar OneDev para confiar en el certificado</a>");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Especifica la URL LDAP, por ejemplo: <i>ldap://localhost</i>, o <i>ldaps://localhost</i>. En caso de que tu servidor LDAP esté utilizando un certificado autofirmado para la conexión ldaps, necesitarás <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configurar OneDev para confiar en el certificado</a>");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"Especifica los nodos base para la búsqueda de usuarios. Por ejemplo: <i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"Especifica el nombre del atributo dentro de la entrada LDAP del usuario cuyo valor contiene los nombres distinguidos de los grupos a los que pertenece. Por ejemplo, algunos servidores LDAP utilizan el atributo <i>memberOf</i> para listar grupos");
		m.put("Specifies password of above manager DN", "Especifica la contraseña del DN del administrador mencionado anteriormente");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"Especifica el atributo que contiene el nombre del grupo dentro de la entrada LDAP del grupo encontrado. El valor de este atributo se mapeará a un grupo de OneDev. Este atributo normalmente se establece en <i>cn</i>");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"Especifica el archivo de resultados de prueba .net TRX relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo <tt>TestResults/*.trx</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"Especifica <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> cuyo valor es un token de acceso con permiso de escritura de código sobre los proyectos mencionados anteriormente. Los commits, problemas y solicitudes de extracción también se crearán bajo el nombre del propietario del token de acceso");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"Especifica el archivo de salida json de <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Este archivo puede generarse con la opción de salida json de clippy, por ejemplo <code>cargo clippy --message-format json>check-result.json</code>. Usa * o ? para coincidencia de patrones");
		m.put("Specify Build Options", "Especifica las opciones de compilación");
		m.put("Specify CPD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match", 
			"Especifica el archivo xml de resultados de CPD relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo, <tt>target/cpd.xml</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify Commit Message", "Especifica el mensaje de commit");
		m.put("Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. Use * or ? for pattern match", 
			"Especifica el archivo de informe ESLint en formato checkstyle bajo <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Este archivo puede generarse con la opción de ESLint <tt>'-f checkstyle'</tt> y <tt>'-o'</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify GitHub API url, for instance <tt>https://api.github.com</tt>", "Especifica la URL de la API de GitHub, por ejemplo <tt>https://api.github.com</tt>");
		m.put("Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>", "Especifica la URL de la API de GitLab, por ejemplo <tt>https://gitlab.example.com/api/v4</tt>");
		m.put("Specify Gitea API url, for instance <tt>https://gitea.example.com/api/v1</tt>", "Especifica la URL de la API de Gitea, por ejemplo <tt>https://gitea.example.com/api/v1</tt>");
		m.put("Specify GoogleTest XML result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This report can be generated with environment variable <tt>GTEST_OUTPUT</tt> when running tests, For instance, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * or ? for pattern match", 
			"Especifica el archivo XML de resultados de GoogleTest relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Este informe puede generarse con la variable de entorno <tt>GTEST_OUTPUT</tt> al ejecutar pruebas, por ejemplo, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Usa * o ? para coincidencia de patrones");
		m.put("Specify IMAP user name.<br><b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system email address specified above", 
			"Especifica el nombre de usuario IMAP.<br><b class='text-danger'>NOTA: </b> Esta cuenta debería poder recibir correos enviados a la dirección de correo del sistema especificada anteriormente");
		m.put("Specify JUnit test result file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match", 
			"Especifica el archivo de resultados de prueba JUnit en formato XML relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo <tt>target/surefire-reports/TEST-*.xml</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match", 
			"Especifica el archivo de informe de cobertura xml de JaCoCo relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo, <tt>target/site/jacoco/jacoco.xml</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify Jest coverage report file in clover format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. Use * or ? for pattern match", 
			"Especifica el archivo de informe de cobertura de Jest en formato clover relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo <tt>coverage/clover.xml</tt>. Este archivo puede generarse con la opción de Jest <tt>'--coverage'</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify Jest test result file in json format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match", 
			"Especifica el archivo de resultados de prueba de Jest en formato json relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Este archivo puede generarse mediante la opción de Jest <tt>'--json'</tt> y <tt>'--outputFile'</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify OCI layout directory of the image to scan. This directory can be generated via build image step or pull image step. It should be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Especifica el directorio de diseño OCI de la imagen para escanear. Este directorio puede generarse mediante el paso de construcción de imagen o el paso de extracción de imagen. Debe ser relativo a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>");
		m.put("Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from", 
			"Especifica el directorio de diseño OCI relativo a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> para realizar el push");
		m.put("Specify OpenID scopes to request", "Especifica los alcances de OpenID a solicitar");
		m.put("Specify PMD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/pmd.xml</tt>. Use * or ? for pattern match", 
			"Especifica el archivo xml de resultados de PMD relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo, <tt>target/pmd.xml</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify PowerShell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of your script<br>", 
			"Especifica los comandos de PowerShell para ejecutar bajo <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTA: </b> OneDev verifica el código de salida del script para determinar si el paso es exitoso. Dado que PowerShell siempre sale con 0 incluso si hay errores en el script, deberías manejar los errores en el script y salir con un código distinto de cero, o agregar la línea <code>$ErrorActionPreference = &quot;Stop&quot;</code> al inicio de tu script<br>");
		m.put("Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with <i>-o</i> option. Use * or ? for pattern match", 
			"Especifica el archivo de salida de diagnósticos de Roslynator en formato XML relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Este archivo puede generarse con la opción <i>-o</i>. Usa * o ? para coincidencia de patrones");
		m.put("Specify Shell/Batch Commands to Run", "Especifica los comandos Shell/Batch para ejecutar");
		m.put("Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match", 
			"Especifica el archivo xml de resultados de SpotBugs relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo, <tt>target/spotbugsXml.xml</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify System Settings", "Especifica la configuración del sistema");
		m.put("Specify URL of remote git repository. Only http/https protocol is supported", "Especifica la URL del repositorio git remoto. Solo se admite el protocolo http/https");
		m.put("Specify YouTrack login name. This account should have permission to:<ul><li>Read full information and issues of the projects you want to import<li>Read issue tags<li>Read user basic information</ul>", 
			"Especifica el nombre de inicio de sesión de YouTrack. Esta cuenta debería tener permiso para:<ul><li>Leer información completa y problemas de los proyectos que deseas importar<li>Leer etiquetas de problemas<li>Leer información básica de usuarios</ul>");
		m.put("Specify YouTrack password or access token for above user", "Especifica la contraseña o el token de acceso de YouTrack para el usuario mencionado anteriormente");
		m.put("Specify a &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regular expression&lt;/a&gt; to match issue references. For instance:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;", 
			"Especifica una &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;expresión regular&lt;/a&gt; para coincidir con referencias de problemas. Por ejemplo:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> after issue number", 
			"Especifica una <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>expresión regular</a> después del número de problema");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> before issue number", 
			"Especifica una <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>expresión regular</a> antes del número de problema");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> para usar como clave privada SSH");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> para usar como token de acceso");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> para usar como token de acceso para importar la especificación de compilación del proyecto mencionado anteriormente si su código no es accesible públicamente");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> para usar como contraseña o token de acceso del registro");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> para usar como contraseña o token de acceso para acceder al repositorio remoto");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> para usar como clave privada para la autenticación SSH. <b class='text-info'>NOTA:</b> No se admite la clave privada con frase de contraseña");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> para usar como clave privada del usuario mencionado anteriormente para la autenticación SSH. <b class='text-info'>NOTA:</b> No se admite la clave privada con frase de contraseña");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> cuyo valor es un token de acceso con permiso de gestión para el proyecto mencionado anteriormente. Ten en cuenta que no se requiere el token de acceso si se sincroniza con el proyecto actual o hijo y el commit de compilación es alcanzable desde la rama predeterminada");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Especifica un <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> cuyo valor es un token de acceso con permiso para cargar caché para el proyecto mencionado anteriormente. Ten en cuenta que esta propiedad no es necesaria si se carga caché al proyecto actual o hijo y el commit de compilación es alcanzable desde la rama predeterminada");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"Especifica un <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> para ejecutar automáticamente el trabajo. <b class='text-info'>Nota:</b> Para ahorrar recursos, se ignorarán los segundos en la expresión cron, y el intervalo mínimo de programación es de un minuto");
		m.put("Specify a Docker Image to Test Against", "Especifica una imagen Docker para probar");
		m.put("Specify a custom field of Enum type", "Especifica un campo personalizado de tipo Enum");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "Especifica una consulta predeterminada para filtrar/ordenar problemas solucionados de los trabajos especificados");
		m.put("Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into", 
			"Especifica un archivo relativo a <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> para escribir el checksum");
		m.put("Specify a multi-value user field to hold assignees information.<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifica un campo de usuario de múltiples valores para mantener la información de los asignados.<b>NOTA: </b> Puedes personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify a multi-value user field to hold assignees information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifica un campo de usuario de múltiples valores para mantener la información de los asignados.<br><b>NOTA: </b> Puedes personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify a path inside container to be used as mount target", "Especifica una ruta dentro del contenedor para usar como destino de montaje");
		m.put("Specify a path relative to job workspace to be used as mount source. Leave empty to mount job workspace itself", 
			"Especifica una ruta relativa al espacio de trabajo del trabajo para usar como fuente de montaje. Déjalo vacío para montar el espacio de trabajo del trabajo en sí");
		m.put("Specify a secret to be used as access token to create issue in above project if it is not publicly accessible", 
			"Especifica un secreto para usar como token de acceso para crear problemas en el proyecto mencionado anteriormente si no es accesible públicamente");
		m.put("Specify a secret to be used as access token to retrieve artifacts from above project. If not specified, project artifacts will be accessed anonymously", 
			"Especifica un secreto para usar como token de acceso para recuperar artefactos del proyecto mencionado anteriormente. Si no se especifica, los artefactos del proyecto se accederán de forma anónima");
		m.put("Specify a secret to be used as access token to trigger job in above project", "Especifica un secreto para ser usado como token de acceso para activar el trabajo en el proyecto mencionado");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Especifica un secreto cuyo valor es un token de acceso con permiso para cargar caché para el proyecto mencionado anteriormente. Ten en cuenta que esta propiedad no es necesaria si se carga caché al proyecto actual o hijo y el commit de compilación es alcanzable desde la rama predeterminada");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"Especifica la ruta absoluta al archivo de configuración utilizado por kubectl para acceder al clúster. Déjalo vacío para que kubectl determine automáticamente la información de acceso al clúster");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"Especifica la ruta absoluta a la utilidad kubectl, por ejemplo: <i>/usr/bin/kubectl</i>. Si se deja vacío, OneDev intentará encontrar la utilidad desde la ruta del sistema");
		m.put("Specify account name to login to Gmail to send/receive email", "Especifica el nombre de cuenta para iniciar sesión en Gmail para enviar/recibir correos");
		m.put("Specify additional users able to access this confidential issue besides those granted via role. Users mentioned in the issue will be authorized automatically", 
			"Especifica usuarios adicionales que pueden acceder a este problema confidencial además de aquellos autorizados mediante roles. Los usuarios mencionados en el problema se autorizarán automáticamente");
		m.put("Specify agents applicable for this executor", "Especifica agentes aplicables para este ejecutor");
		m.put("Specify allowed <a href='https://spdx.org/licenses/' target='_blank'>spdx license identifiers</a> <span class='text-warning'>separated by comma</span>", 
			"Especifica identificadores de licencia <a href='https://spdx.org/licenses/' target='_blank'>spdx permitidos</a> <span class='text-warning'>separados por comas</span>");
		m.put("Specify an email address sharing same inbox as the system email address in mail setting definition. Emails sent to this address will be created as issues in this project. The default value takes form of <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>", 
			"Especifica una dirección de correo electrónico que comparte la misma bandeja de entrada que la dirección de correo del sistema en la definición de configuración de correo. Los correos enviados a esta dirección se crearán como problemas en este proyecto. El valor predeterminado toma la forma de <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>");
		m.put("Specify applicable projects for above option. Multiple projects should be separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Especifica proyectos aplicables para la opción mencionada anteriormente. Múltiples proyectos deben separarse por espacio. Usa '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de patrones de ruta</a>. Prefija con '-' para excluir. Déjalo vacío para todos los proyectos");
		m.put("Specify applicable projects separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Especifica proyectos aplicables separados por espacio. Usa '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de patrones de ruta</a>. Prefija con '-' para excluir. Déjalo vacío para todos los proyectos");
		m.put("Specify application (client) ID of the app registered in Entra ID", "Especifica el ID de aplicación (cliente) de la aplicación registrada en Entra ID");
		m.put("Specify arguments for imagetools. For instance <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>", 
			"Especifica argumentos para imagetools. Por ejemplo <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>");
		m.put("Specify artifacts to retrieve into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved.", 
			"Especifica artefactos para recuperar en <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Solo se pueden recuperar artefactos publicados (mediante el paso de publicación de artefactos).");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"Especifica al menos 10 caracteres alfanuméricos para usar como secreto, y luego agrega una entrada de análisis de entrada en el lado de SendGrid:<ul><li><code>Destination URL</code> debe configurarse como <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, por ejemplo, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Ten en cuenta que en un entorno de producción, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>debería habilitarse https</a> para proteger el secreto</li><li><code>Receiving domain</code> debe ser el mismo que la parte del dominio de la dirección de correo del sistema especificada anteriormente</li><li>La opción <code>POST the raw, full MIME message</code> está habilitada</li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"Especifica los nodos base para la búsqueda de usuarios. Por ejemplo: <i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "Especifica la rama para realizar el commit del cambio sugerido");
		m.put("Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Especifica la rama para ejecutar el trabajo. Se puede especificar una rama o una etiqueta, pero no ambas. Se usará la rama predeterminada si no se especifica ninguna.");
		m.put("Specify branch, tag or commit in above project to import build spec from", "Especifica la rama, etiqueta o commit en el proyecto mencionado anteriormente para importar la especificación de compilación");
		m.put("Specify by Build Number", "Especifica por número de compilación");
		m.put("Specify cache upload strategy after build successful. <var>Upload If Not Hit</var> means to upload when cache is not found with cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files in cache path are changed", 
			"Especifica la estrategia de carga de caché después de una compilación exitosa. <var>Upload If Not Hit</var> significa cargar cuando no se encuentra la caché con la clave de caché (no claves de carga), y <var>Upload If Changed</var> significa cargar si algunos archivos en la ruta de caché han cambiado");
		m.put("Specify certificate to trust if you are using self-signed certificate for remote repository", 
			"Especifica el certificado para confiar si estás utilizando un certificado autofirmado para el repositorio remoto");
		m.put("Specify certificates to trust if you are using self-signed certificates for your docker registries", 
			"Especifica los certificados para confiar si estás utilizando certificados autofirmados para tus registros de Docker");
		m.put("Specify checkstyle result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/checkstyle-result.xml</tt>. Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> on how to generate the result xml file. Use * or ? for pattern match", 
			"Especifica el archivo xml de resultados de checkstyle relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo, <tt>target/checkstyle-result.xml</tt>. Consulta la <a href='https://checkstyle.org/'>documentación de checkstyle</a> sobre cómo generar el archivo xml de resultados. Usa * o ? para coincidencia de patrones");
		m.put("Specify client secret of the app registered in Entra ID", "Especifica el secreto del cliente de la aplicación registrada en Entra ID");
		m.put("Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/clover/clover.xml</tt>. Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> on how to generate clover xml file. Use * or ? for pattern match", 
			"Especifica el archivo de informe de cobertura xml de clover relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo, <tt>target/site/clover/clover.xml</tt>. Consulta la <a href='https://openclover.org/documentation'>documentación de OpenClover</a> sobre cómo generar el archivo xml de clover. Usa * o ? para coincidencia de patrones");
		m.put("Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match", 
			"Especifica el archivo de informe de cobertura xml de cobertura relativo a <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, por ejemplo, <tt>target/site/cobertura/coverage.xml</tt>. Usa * o ? para coincidencia de patrones");
		m.put("Specify color of the state for displaying purpose", "Especifica el color del estado para fines de visualización");
		m.put("Specify columns of the board. Each column corresponds to a value of the issue field specified above", 
			"Especifica las columnas del tablero. Cada columna corresponde a un valor del campo de problema especificado anteriormente");
		m.put("Specify command to check readiness of the service. This command will be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be executed repeatedly until a zero code is returned to indicate service ready", 
			"Especifica el comando para verificar la disponibilidad del servicio. Este comando será interpretado por cmd.exe en imágenes de Windows, y por shell en imágenes de Linux. Se ejecutará repetidamente hasta que se devuelva un código cero para indicar que el servicio está listo");
		m.put("Specify commands to be executed on remote machine. <b class='text-warning'>Note:</b> user environments will not be picked up when execute these commands, set up them explicitly in commands if necessary", 
			"Especifique los comandos para ejecutar en la máquina remota. <b class='text-warning'>Nota:</b> los entornos de usuario no se recogerán al ejecutar estos comandos, configúrelos explícitamente en los comandos si es necesario");
		m.put("Specify condition to retry build upon failure", "Especifique la condición para reintentar la compilación en caso de fallo");
		m.put("Specify configuration discovery url of your OpenID provider, for instance: <code>https://openid.example.com/.well-known/openid-configuration</code>. Make sure to use HTTPS protocol as OneDev relies on TLS encryption to ensure token validity", 
			"Especifique la URL de descubrimiento de configuración de su proveedor OpenID, por ejemplo: <code>https://openid.example.com/.well-known/openid-configuration</code>. Asegúrese de usar el protocolo HTTPS ya que OneDev depende del cifrado TLS para garantizar la validez del token");
		m.put("Specify container image to execute commands inside", "Especifique la imagen del contenedor para ejecutar los comandos dentro");
		m.put("Specify container image to run", "Especifique la imagen del contenedor para ejecutar");
		m.put("Specify cppcheck xml result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match", 
			"Especifique el archivo de resultados xml de cppcheck relativo al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a>. Este archivo puede generarse con la opción de salida xml de cppcheck, por ejemplo <code>cppcheck src --xml 2>check-result.xml</code>. Use * o ? para coincidencia de patrones");
		m.put("Specify cpu request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Especifique la solicitud de CPU para cada trabajo/servicio que use este ejecutor. Consulte <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>gestión de recursos de Kubernetes</a> para más detalles");
		m.put("Specify default assignees of pull requests submitted to this project. Only users with the write code permission to the project can be selected", 
			"Especifique los asignados predeterminados de las solicitudes de extracción enviadas a este proyecto. Solo los usuarios con permiso para escribir código en el proyecto pueden ser seleccionados");
		m.put("Specify default merge strategy of pull requests submitted to this project", "Especifique la estrategia de fusión predeterminada de las solicitudes de extracción enviadas a este proyecto");
		m.put("Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple destinations should be separated with space", 
			"Especifique los destinos, por ejemplo <tt>registry-server:5000/myorg/myrepo:latest</tt>. Asegúrese de usar <b>el mismo host</b> especificado en la URL del servidor de la configuración del sistema si desea enviar al registro integrado, o simplemente use el formato <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Los destinos múltiples deben separarse con espacios");
		m.put("Specify directory (tenant) ID of the app registered in Entra ID", "Especifique el ID de directorio (tenant) de la aplicación registrada en Entra ID");
		m.put("Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Especifique el directorio relativo al <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a> para almacenar el diseño OCI");
		m.put("Specify docker image of the service", "Especifique la imagen de Docker del servicio");
		m.put("Specify dockerx builder used to build docker image. OneDev will create the builder automatically if it does not exist. Check <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> on how to customize the builder for instance to allow publishing to insecure registries", 
			"Especifique el constructor dockerx utilizado para construir la imagen de Docker. OneDev creará el constructor automáticamente si no existe. Consulte <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>este tutorial</a> sobre cómo personalizar el constructor, por ejemplo, para permitir la publicación en registros inseguros");
		m.put("Specify email addresses to send invitations, with one per line", "Especifique las direcciones de correo electrónico para enviar invitaciones, una por línea");
		m.put("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\"", 
			"Especifique el tiempo estimado <b class='text-warning'>solo para este problema</b>, sin contar \"{0}\"");
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"Especifique los campos de varios problemas creados por Renovate para orquestar la actualización de dependencias");
		m.put("Specify fields to be displayed in the issue list", "Especifique los campos que se mostrarán en la lista de problemas");
		m.put("Specify fields to display in board card", "Especifique los campos para mostrar en la tarjeta del tablero");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"Especifique los archivos relativos al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para publicar. Use * o ? para coincidencia de patrones");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Especifique los archivos para crear el checksum md5. Los archivos múltiples deben separarse por espacios. Se aceptan patrones <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a>. Los archivos no absolutos se consideran relativos al <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a>");
		m.put("Specify files under above directory to be published", "Especifica los archivos bajo el directorio mencionado para ser publicados");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be included in these files to be served as site start page", 
			"Especifique los archivos bajo el directorio anterior para publicar. Use * o ? para coincidencia de patrones. <b>NOTA:</b> <code>index.html</code> debe incluirse en estos archivos para servir como página de inicio del sitio");
		m.put("Specify group to import from. Leave empty to import from projects under current account", 
			"Especifique el grupo para importar. Déjelo vacío para importar desde proyectos bajo la cuenta actual");
		m.put("Specify how to map GitHub issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique cómo mapear las etiquetas de problemas de GitHub a los campos personalizados de OneDev.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify how to map GitLab issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique cómo mapear las etiquetas de problemas de GitLab a los campos personalizados de OneDev.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify how to map Gitea issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique cómo mapear las etiquetas de problemas de Gitea a los campos personalizados de OneDev.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify how to map JIRA issue priorities to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique cómo mapear las prioridades de problemas de JIRA a los campos personalizados de OneDev.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify how to map JIRA issue statuses to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique cómo mapear los estados de problemas de JIRA a los campos personalizados de OneDev.<br><b>NOTA: </b> Puede personalizar los estados de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify how to map JIRA issue types to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique cómo mapear los tipos de problemas de JIRA a los campos personalizados de OneDev.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify how to map YouTrack issue fields to OneDev. Unmapped fields will be reflected in issue description.<br><b>Note: </b><ul><li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, for instance <tt>Priority::Critical</tt><li>You may customize OneDev issue fields in case there is no appropriate option here</ul>", 
			"Especifique cómo mapear los campos de problemas de YouTrack a OneDev. Los campos no mapeados se reflejarán en la descripción del problema.<br><b>Nota: </b><ul><li>El campo de enumeración necesita ser mapeado en forma de <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, por ejemplo <tt>Priority::Critical</tt><li>Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí</ul>");
		m.put("Specify how to map YouTrack issue links to OneDev issue links.<br><b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here", 
			"Especifique cómo mapear los enlaces de problemas de YouTrack a los enlaces de problemas de OneDev.<br><b>NOTA: </b> Puede personalizar los enlaces de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify how to map YouTrack issue state to OneDev issue state. Unmapped states will use the initial state in OneDev.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique cómo mapear el estado de problemas de YouTrack al estado de problemas de OneDev. Los estados no mapeados usarán el estado inicial en OneDev.<br><b>NOTA: </b> Puede personalizar los estados de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify how to map YouTrack issue tags to OneDev issue custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique cómo mapear las etiquetas de problemas de YouTrack a los campos personalizados de problemas de OneDev.<br><b>NOTA: </b> Puede personalizar los campos de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify image on the login button", "Especifique la imagen en el botón de inicio de sesión");
		m.put("Specify image tag to pull from, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to pull from built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Especifique la etiqueta de imagen para extraer, por ejemplo <tt>registry-server:5000/myorg/myrepo:latest</tt>. Asegúrese de usar <b>el mismo host</b> especificado en la URL del servidor de la configuración del sistema si desea extraer del registro integrado, o simplemente use el formato <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>");
		m.put("Specify image tag to push to, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Especifique la etiqueta de imagen para enviar, por ejemplo <tt>registry-server:5000/myorg/myrepo:latest</tt>. Asegúrese de usar <b>el mismo host</b> especificado en la URL del servidor de la configuración del sistema si desea enviar al registro integrado, o simplemente use el formato <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>");
		m.put("Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple tags should be separated with space", 
			"Especifique las etiquetas de imagen para enviar, por ejemplo <tt>registry-server:5000/myorg/myrepo:latest</tt>. Asegúrese de usar <b>el mismo host</b> especificado en la URL del servidor de la configuración del sistema si desea enviar al registro integrado, o simplemente use el formato <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Las etiquetas múltiples deben separarse con espacios");
		m.put("Specify import option", "Especifique la opción de importación");
		m.put("Specify incoming email poll interval in seconds", "Especifique el intervalo de sondeo de correo electrónico entrante en segundos");
		m.put("Specify issue creation settings. For a particular sender and project, the first matching entry will take effect. Issue creation will be disallowed if no matching entry found", 
			"Especifique la configuración de creación de problemas. Para un remitente y proyecto en particular, la primera entrada coincidente tendrá efecto. La creación de problemas será desautorizada si no se encuentra ninguna entrada coincidente");
		m.put("Specify issue field to identify different columns of the board. Only state and single-valued enumeration field can be used here", 
			"Especifique el campo de problemas para identificar las diferentes columnas del tablero. Solo se pueden usar aquí los campos de estado y de enumeración de valor único");
		m.put("Specify links to be displayed in the issue list", "Especifique los enlaces para mostrar en la lista de problemas");
		m.put("Specify links to display in board card", "Especifique los enlaces para mostrar en la tarjeta del tablero");
		m.put("Specify manager DN to authenticate OneDev itself to Active Directory. The manager DN should be specified in form of <i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>manager@example.com</i>", 
			"Especifique el DN del administrador para autenticar OneDev a Active Directory. El DN del administrador debe especificarse en forma de <i>&lt;account name&gt;@&lt;domain&gt;</i>, por ejemplo: <i>manager@example.com</i>");
		m.put("Specify manager DN to authenticate OneDev itself to LDAP server", "Especifique el DN del administrador para autenticar OneDev al servidor LDAP");
		m.put("Specify markdown file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published", 
			"Especifique el archivo markdown relativo al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a> para publicar");
		m.put("Specify max git LFS file size in mega bytes", "Especifique el tamaño máximo de archivo git LFS en megabytes");
		m.put("Specify max number of CPU intensive tasks the server can run concurrently, such as Git repository pull/push, repository index, etc.", 
			"Especifique el número máximo de tareas intensivas de CPU que el servidor puede ejecutar simultáneamente, como extracción/push de repositorio Git, indexación de repositorio, etc.");
		m.put("Specify max number of jobs this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Especifique el número máximo de trabajos que este ejecutor puede ejecutar simultáneamente en cada agente coincidente. Déjelo vacío para establecer como núcleos de CPU del agente");
		m.put("Specify max number of jobs this executor can run concurrently. Leave empty to set as CPU cores", 
			"Especifique el número máximo de trabajos que este ejecutor puede ejecutar simultáneamente. Déjelo vacío para establecer como núcleos de CPU");
		m.put("Specify max number of jobs/services this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Especifique el número máximo de trabajos/servicios que este ejecutor puede ejecutar simultáneamente en cada agente coincidente. Déjelo vacío para establecer como núcleos de CPU del agente");
		m.put("Specify max number of jobs/services this executor can run concurrently. Leave empty to set as CPU cores", 
			"Especifique el número máximo de trabajos/servicios que este ejecutor puede ejecutar simultáneamente. Déjelo vacío para establecer como núcleos de CPU");
		m.put("Specify max size of uploaded file in mega bytes via web interface. This applies to file uploaded to repository, markdown content (issue comment etc), and build artifacts", 
			"Especifique el tamaño máximo de archivo subido en megabytes a través de la interfaz web. Esto se aplica a los archivos subidos al repositorio, contenido markdown (comentarios de problemas, etc.) y artefactos de compilación");
		m.put("Specify memory request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Especifique la solicitud de memoria para cada trabajo/servicio que use este ejecutor. Consulte <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>gestión de recursos de Kubernetes</a> para más detalles");
		m.put("Specify mypy output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated by redirecting mypy output <b>without option '--pretty'</b>, for instance <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * or ? for pattern match", 
			"Especifique el archivo de salida de mypy relativo al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a>. Este archivo puede generarse redirigiendo la salida de mypy <b>sin la opción '--pretty'</b>, por ejemplo <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * o ? para coincidencia de patrones");
		m.put("Specify name of the branch", "Especifique el nombre de la rama");
		m.put("Specify name of the environment variable", "Especifique el nombre de la variable de entorno");
		m.put("Specify name of the iteration", "Especifique el nombre de la iteración");
		m.put("Specify name of the job", "Especifique el nombre del trabajo");
		m.put("Specify name of the report to be displayed in build detail page", "Especifique el nombre del informe para mostrar en la página de detalles de la compilación");
		m.put("Specify name of the saved query", "Especifique el nombre de la consulta guardada");
		m.put("Specify name of the service, which will be used as host name to access the service", 
			"Especifique el nombre del servicio, que se usará como nombre de host para acceder al servicio");
		m.put("Specify name of the tag", "Especifique el nombre de la etiqueta");
		m.put("Specify network timeout in seconds when authenticate through this system", "Especifique el tiempo de espera de red en segundos al autenticar a través de este sistema");
		m.put("Specify node selector of this locator", "Especifique el selector de nodos de este localizador");
		m.put("Specify password or access token of specified registry", "Especifique la contraseña o el token de acceso del registro especificado");
		m.put("Specify password to authenticate with", "Especifique la contraseña para autenticar");
		m.put("Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>", "Especifique la ruta al ejecutable curl, por ejemplo: <tt>/usr/bin/curl</tt>");
		m.put("Specify path to git executable, for instance: <tt>/usr/bin/git</tt>", "Especifique la ruta al ejecutable git, por ejemplo: <tt>/usr/bin/git</tt>");
		m.put("Specify powershell executable to be used", "Especifique el ejecutable de powershell a utilizar");
		m.put("Specify project to import build spec from", "Especifique el proyecto para importar la especificación de compilación");
		m.put("Specify project to import into at OneDev side", "Especifique el proyecto para importar en el lado de OneDev");
		m.put("Specify project to retrieve artifacts from", "Especifique el proyecto para recuperar artefactos");
		m.put("Specify project to run job in", "Especifica el proyecto para ejecutar el trabajo");
		m.put("Specify projects", "Especifique los proyectos");
		m.put("Specify projects to update dependencies. Leave empty for current project", "Especifique los proyectos para actualizar dependencias. Déjelo vacío para el proyecto actual");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Especifique el archivo de resultados json de pylint relativo al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a>. Este archivo puede generarse con la opción de formato de salida json de pylint, por ejemplo <code>--exit-zero --output-format=json:pylint-result.json</code>. Tenga en cuenta que no fallamos el comando pylint ante violaciones, ya que este paso fallará la compilación según el umbral configurado. Use * o ? para coincidencia de patrones");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"Especifique los inicios de sesión del registro si es necesario. Para el registro integrado, use <code>@server_url@</code> para la URL del registro, <code>@job_token@</code> para el nombre de usuario y el token de acceso para la contraseña");
		m.put("Specify registry url. Leave empty for official registry", "Especifique la URL del registro. Déjelo vacío para el registro oficial");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Especifique la ruta relativa bajo el <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a> para almacenar el diseño OCI");
		m.put("Specify repositories", "Especifique los repositorios");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"Especifique los revisores requeridos si se cambia la ruta especificada. Tenga en cuenta que el usuario que envía el cambio se considera que ha revisado el cambio automáticamente");
		m.put("Specify root URL to access this server", "Especifique la URL raíz para acceder a este servidor");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Especifique el archivo de resultados json de ruff relativo al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a>. Este archivo puede generarse con la opción de formato de salida json de ruff, por ejemplo <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Tenga en cuenta que no fallamos el comando ruff ante violaciones, ya que este paso fallará la compilación según el umbral configurado. Use * o ? para coincidencia de patrones");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Especifique los comandos shell (en Linux/Unix) o comandos batch (en Windows) para ejecutar bajo el <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a>");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Especifique los comandos shell para ejecutar bajo el <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espacio de trabajo del trabajo</a>");
		m.put("Specify shell to be used", "Especifique el shell a utilizar");
		m.put("Specify source param for SCP command, for instance <code>app.tar.gz</code>", "Especifique el parámetro de origen para el comando SCP, por ejemplo <code>app.tar.gz</code>");
		m.put("Specify space separated refs to pull from remote. '*' can be used in ref name for wildcard match<br><b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update branches/tags via this step", 
			"Especifique las referencias separadas por espacios para extraer del remoto. '*' puede usarse en el nombre de la referencia para coincidencia de patrones<br><b class='text-danger'>NOTA:</b> la regla de protección de ramas/etiquetas será ignorada al actualizar ramas/etiquetas mediante este paso");
		m.put("Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Especifique las ramas separadas por espacios para proteger. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de patrones de ruta</a>. Prefijo con '-' para excluir");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Especifique los trabajos separados por espacios. Use '*' o '?' para coincidencia de patrones. Prefijo con '-' para excluir");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts will be granted implicitly in matched jobs even if no other permissions are specified here", 
			"Especifique los trabajos separados por espacios. Use '*' o '?' para coincidencia de patrones. Prefijo con '-' para excluir. <b class='text-danger'>NOTA: </b> El permiso para acceder a los artefactos de compilación se otorgará implícitamente en los trabajos coincidentes incluso si no se especifican otros permisos aquí");
		m.put("Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Especifique las rutas separadas por espacios para proteger. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de patrones de ruta</a>. Prefijo con '-' para excluir");
		m.put("Specify space-separated projects applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Especifique los proyectos separados por espacios aplicables para esta entrada. Use '*' o '?' para coincidencia de patrones. Prefijo con '-' para excluir. Déjelo vacío para coincidir con todos los proyectos");
		m.put("Specify space-separated sender email addresses applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all senders", 
			"Especifique las direcciones de correo electrónico del remitente separadas por espacios aplicables para esta entrada. Use '*' o '?' para coincidencia de patrones. Prefijo con '-' para excluir. Déjelo vacío para coincidir con todos los remitentes");
		m.put("Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Especifique las etiquetas separadas por espacios para proteger. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de patrones de ruta</a>. Prefijo con '-' para excluir");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>", 
			"Especifique la página de inicio del informe relativa al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a>, por ejemplo: <tt>manual/index.md</tt>");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"Especifique la página de inicio del informe relativa al <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a>, por ejemplo: api/index.html");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"Especifique el tamaño de almacenamiento para solicitar el volumen de compilación. El tamaño debe cumplir con el <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>formato de capacidad de recursos de Kubernetes</a>, por ejemplo <i>10Gi</i>");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"Especifique el ancho de tabulación utilizado para calcular el valor de columna de los problemas encontrados en el informe proporcionado");
		m.put("Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Especifica la etiqueta para ejecutar el trabajo. Se puede especificar una rama o una etiqueta, pero no ambas. Se usará la rama predeterminada si no se especifica ninguna.");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"Especifique el parámetro objetivo para el comando SCP, por ejemplo <code>user@@host:/app</code>. <b class='text-info'>NOTA:</b> Asegúrese de que el comando scp esté instalado en el host remoto");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"Especifique el texto para reemplazar las referencias de problemas coincidentes, por ejemplo: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Aquí $1 y $2 representan grupos capturados en el patrón de problemas de ejemplo (ver ayuda de patrón de problemas)");
		m.put("Specify the condition current build must satisfy to execute this action", "Especifique la condición que debe cumplir la compilación actual para ejecutar esta acción");
		m.put("Specify the condition preserved builds must match", "Especifique la condición que deben coincidir las compilaciones preservadas");
		m.put("Specify the private key (in PEM format) used by SSH server to establish connections with client", 
			"Especifique la clave privada (en formato PEM) utilizada por el servidor SSH para establecer conexiones con el cliente");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"Especifique la estrategia para recuperar información de membresía de grupo. Para otorgar permisos apropiados a un grupo LDAP, se debe definir un grupo de OneDev con el mismo nombre. Use la estrategia <tt>No Recuperar Grupos</tt> si desea gestionar las membresías de grupo en el lado de OneDev");
		m.put("Specify timeout in seconds when communicating with mail server", "Especifique el tiempo de espera en segundos al comunicarse con el servidor de correo");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "Especifique el tiempo de espera en segundos. Cuenta desde el momento en que se envía el trabajo");
		m.put("Specify title of the issue", "Especifique el título del problema");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "Especifique la URL de la API de YouTrack. Por ejemplo <tt>http://localhost:8080/api</tt>");
		m.put("Specify user name of above machine for SSH authentication", "Especifique el nombre de usuario de la máquina anterior para la autenticación SSH");
		m.put("Specify user name of specified registry", "Especifique el nombre de usuario del registro especificado");
		m.put("Specify user name of the registry", "Especifique el nombre de usuario del registro");
		m.put("Specify user name to authenticate with", "Especifique el nombre de usuario para autenticar");
		m.put("Specify value of the environment variable", "Especifique el valor de la variable de entorno");
		m.put("Specify web UI session timeout in minutes. Existing sessions will not be affected after changing this value.", 
			"Especificar el tiempo de espera de la sesión de la interfaz web en minutos. Las sesiones existentes no se verán afectadas después de cambiar este valor.");
		m.put("Specify webhook url to post events", "Especifique la URL del webhook para publicar eventos");
		m.put("Specify which issue state to use for closed GitHub issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique qué estado de problema usar para problemas cerrados de GitHub.<br><b>NOTA: </b> Puede personalizar los estados de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify which issue state to use for closed GitLab issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique qué estado de problema usar para problemas cerrados de GitLab.<br><b>NOTA: </b> Puede personalizar los estados de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify which issue state to use for closed Gitea issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique qué estado de problema usar para problemas cerrados de Gitea.<br><b>NOTA: </b> Puede personalizar los estados de problemas de OneDev en caso de que no haya una opción adecuada aquí");
		m.put("Specify which states are considered as closed for various issues created by Renovate to orchestrate dependency update. Additionally, when Renovate closes the issue, OneDev will transit the issue to first state specified here", 
			"Especifique qué estados se consideran cerrados para varios problemas creados por Renovate para orquestar la actualización de dependencias. Además, cuando Renovate cierre el problema, OneDev transitará el problema al primer estado especificado aquí");
		m.put("Specify working days per week. This will affect parsing and displaying of working periods. For instance <tt>1w</tt> is the same as <tt>5d</tt> if this property is set to <tt>5</tt>", 
			"Especifique los días laborales por semana. Esto afectará el análisis y la visualización de los períodos laborales. Por ejemplo, <tt>1w</tt> es lo mismo que <tt>5d</tt> si esta propiedad se establece en <tt>5</tt>");
		m.put("Specify working hours per day. This will affect parsing and displaying of working periods. For instance <tt>1d</tt> is the same as <tt>8h</tt> if this property is set to <tt>8</tt>", 
			"Especifique las horas laborales por día. Esto afectará el análisis y la visualización de los períodos laborales. Por ejemplo, <tt>1d</tt> es lo mismo que <tt>8h</tt> si esta propiedad se establece en <tt>8</tt>");
		m.put("Spent", "Gastado");
		m.put("Spent Time", "Tiempo Gastado");
		m.put("Spent Time Issue Field", "Campo de Problema de Tiempo Gastado");
		m.put("Spent Time:", "Tiempo Gastado:");
		m.put("Spent time / estimated time", "Tiempo gastado / tiempo estimado");
		m.put("Split", "Dividir");
		m.put("Split view", "Vista dividida");
		m.put("SpotBugs Report", "Informe de SpotBugs");
		m.put("Squash Source Branch Commits", "Combinar Commits de Rama Fuente");
		m.put("Squash all commits from source branch into a single commit in target branch", "Combinar todos los commits de la rama fuente en un solo commit en la rama objetivo");
		m.put("Squash source branch commits", "Combinar commits de la rama fuente");
		m.put("Ssh", "Ssh");
		m.put("Ssh Key", "Clave Ssh");
		m.put("Ssh Setting", "Configuración Ssh");
		m.put("Ssl Setting", "Configuración Ssl");
		m.put("Sso Connector", "Conector Sso");
		m.put("Sso Provider Bean", "Proveedor Sso Bean");
		m.put("Start At", "Comenzar En");
		m.put("Start Date", "Fecha de Inicio");
		m.put("Start Page", "Página de Inicio");
		m.put("Start agent on remote Linux machine by running below command:", "Inicie el agente en una máquina Linux remota ejecutando el siguiente comando:");
		m.put("Start date", "Fecha de inicio");
		m.put("Start to watch once I am involved", "Comenzar a observar una vez que esté involucrado");
		m.put("Start work", "Comenzar trabajo");
		m.put("Start/Due Date", "Fecha de Inicio/Vencimiento");
		m.put("State", "Estado");
		m.put("State Durations", "Duraciones de Estado");
		m.put("State Frequencies", "Frecuencias de Estado");
		m.put("State Spec", "Especificación de Estado");
		m.put("State Transitions", "Transiciones de Estado");
		m.put("State Trends", "Tendencias de Estado");
		m.put("State of an issue is transited", "El estado de un problema es transitado");
		m.put("States", "Estados");
		m.put("Statistics", "Estadísticas");
		m.put("Stats", "Estadísticas");
		m.put("Stats Group", "Grupo de Estadísticas");
		m.put("Status", "Estado");
		m.put("Status Code", "Código de estado");
		m.put("Status code", "Código de estado");
		m.put("Status code other than 200 indicating the error type", "Código de estado distinto de 200 indicando el tipo de error");
		m.put("Step", "Paso");
		m.put("Step Template", "Plantilla de Paso");
		m.put("Step Templates", "Plantillas de Paso");
		m.put("Step {0} of {1}: ", "Paso {0} de {1}:");
		m.put("Steps", "Pasos");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"Los pasos se ejecutarán en serie en el mismo nodo, compartiendo el mismo <a href='https://docs.onedev.io/concepts#job-workspace'>espacio de trabajo del trabajo</a>");
		m.put("Stop work", "Detener trabajo");
		m.put("Stopwatch Overdue", "Cronómetro Vencido");
		m.put("Storage Settings", "Configuración de almacenamiento");
		m.put("Storage file missing", "Archivo de almacenamiento faltante");
		m.put("Storage not found", "Almacenamiento no encontrado");
		m.put("Stored with Git LFS", "Almacenado con Git LFS");
		m.put("Sub Keys", "Subclaves");
		m.put("Subject", "Asunto");
		m.put("Submit", "Enviar");
		m.put("Submit Reason", "Razón de Envío");
		m.put("Submit Support Request", "Enviar Solicitud de Soporte");
		m.put("Submitted After", "Enviado Después");
		m.put("Submitted At", "Enviado En");
		m.put("Submitted Before", "Enviado Antes");
		m.put("Submitted By", "Enviado Por");
		m.put("Submitted manually", "Enviado manualmente");
		m.put("Submitter", "Remitente");
		m.put("Subscription Key", "Clave de Suscripción");
		m.put("Subscription Management", "Gestión de Suscripción");
		m.put("Subscription data", "Datos de Suscripción");
		m.put("Subscription key installed successfully", "Clave de suscripción instalada exitosamente");
		m.put("Subscription key not applicable: this key is intended to activate a trial subscription", 
			"Clave de suscripción no aplicable: esta clave está destinada a activar una suscripción de prueba");
		m.put("Subscription key not applicable: this key is intended to renew a user based subscription", 
			"Clave de suscripción no aplicable: esta clave está destinada a renovar una suscripción basada en usuarios");
		m.put("Subscription key not applicable: this key is intended to renew an unlimited users subscription", 
			"Clave de suscripción no aplicable: esta clave está destinada a renovar una suscripción de usuarios ilimitados");
		m.put("Subscription key not applicable: this key is intended to update licensee of an existing subscription", 
			"Clave de suscripción no aplicable: esta clave está destinada a actualizar el licenciatario de una suscripción existente");
		m.put("Success Rate", "Tasa de Éxito");
		m.put("Successful", "Exitoso");
		m.put("Suffix Pattern", "Patrón de Sufijo");
		m.put("Suggest changes", "Sugerir cambios");
		m.put("Suggested change", "Cambio sugerido");
		m.put("Suggestion is outdated either due to code change or pull request close", "La sugerencia está desactualizada debido a un cambio de código o cierre de solicitud de extracción");
		m.put("Suggestions", "Sugerencias");
		m.put("Summary", "Resumen");
		m.put("Support & Bug Report", "Soporte y Reporte de Errores");
		m.put("Support Request", "Solicitud de Soporte");
		m.put("Swap", "Intercambiar");
		m.put("Switch to HTTP(S)", "Cambiar a HTTP(S)");
		m.put("Switch to SSH", "Cambiar a SSH");
		m.put("Symbol Name", "Nombre del Símbolo");
		m.put("Symbol name", "Nombre del símbolo");
		m.put("Symbols", "Símbolos");
		m.put("Sync Replica Status and Back to Home", "Sincronizar estado de réplica y volver a inicio");
		m.put("Sync Repository", "Sincronizar Repositorio");
		m.put("Sync Timing of All Queried Issues", "Sincronizar el Tiempo de Todas las Incidencias Consultadas");
		m.put("Sync Timing of Selected Issues", "Sincronizar el Tiempo de las Incidencias Seleccionadas");
		m.put("Sync requested. Please check status after a while", "Sincronización solicitada. Por favor, verifica el estado después de un tiempo");
		m.put("Synchronize", "Sincronizar");
		m.put("System", "Sistema");
		m.put("System Alert", "Alerta del Sistema");
		m.put("System Alert Template", "Plantilla de Alerta del Sistema");
		m.put("System Date", "Fecha del Sistema");
		m.put("System Email Address", "Dirección de Correo Electrónico del Sistema");
		m.put("System Maintenance", "Mantenimiento del Sistema");
		m.put("System Setting", "Configuración del Sistema");
		m.put("System Settings", "Configuraciones del Sistema");
		m.put("System email address defined in mail setting should be used as recipient of such email, and project name may be appended to this address using to indicate where to create issues. For instance, if system email address is specified as <tt>support@example.com</tt>, sending email to <tt>support+myproject@example.com</tt> will create issue in <tt>myproject</tt>. If project name is not appended, OneDev will look up the project using project designation information below", 
			"La dirección de correo electrónico del sistema definida en la configuración de correo debe usarse como destinatario de dicho correo, y el nombre del proyecto puede agregarse a esta dirección para indicar dónde crear incidencias. Por ejemplo, si la dirección de correo electrónico del sistema se especifica como <tt>support@example.com</tt>, enviar un correo a <tt>support+myproject@example.com</tt> creará una incidencia en <tt>myproject</tt>. Si no se agrega el nombre del proyecto, OneDev buscará el proyecto utilizando la información de designación del proyecto a continuación");
		m.put("System settings have been saved", "Las configuraciones del sistema han sido guardadas");
		m.put("System uuid", "UUID del sistema");
		m.put("TIMED_OUT", "TIEMPO_AGOTADO");
		m.put("TRX Report (.net unit test)", "Informe TRX (prueba unitaria .net)");
		m.put("Tab Width", "Ancho de Pestaña");
		m.put("Tag", "Etiqueta");
		m.put("Tag \"{0}\" already exists, please choose a different name", "La etiqueta \"{0}\" ya existe, por favor elige un nombre diferente");
		m.put("Tag \"{0}\" already exists, please choose a different name.", "La etiqueta \"{0}\" ya existe, por favor elige un nombre diferente.");
		m.put("Tag \"{0}\" created", "Etiqueta \"{0}\" creada");
		m.put("Tag \"{0}\" deleted", "Etiqueta \"{0}\" eliminada");
		m.put("Tag Message", "Mensaje de Etiqueta");
		m.put("Tag Name", "Nombre de la Etiqueta");
		m.put("Tag Protection", "Protección de Etiqueta");
		m.put("Tag creation", "Creación de Etiqueta");
		m.put("Tags", "Etiquetas");
		m.put("Target", "Objetivo");
		m.put("Target Branches", "Ramas Objetivo");
		m.put("Target Docker Image", "Imagen Docker Objetivo");
		m.put("Target File", "Archivo Objetivo");
		m.put("Target Path", "Ruta Objetivo");
		m.put("Target Project", "Proyecto Objetivo");
		m.put("Target branch no longer exists", "La rama objetivo ya no existe");
		m.put("Target branch was fast-forwarded to source branch", "La rama objetivo se adelantó rápidamente a la rama fuente");
		m.put("Target branch will be fast-forwarded to source branch", "La rama objetivo se adelantará rápidamente a la rama fuente");
		m.put("Target containing spaces or starting with dash needs to be quoted", "El objetivo que contiene espacios o comienza con un guion necesita ser citado");
		m.put("Target or source branch is updated. Please try again", "La rama objetivo o fuente se ha actualizado. Por favor, inténtalo de nuevo");
		m.put("Task List", "Lista de Tareas");
		m.put("Task list", "Lista de tareas");
		m.put("Tell user to reset password", "Indicar al usuario que restablezca la contraseña");
		m.put("Template Name", "Nombre de la Plantilla");
		m.put("Template saved", "Plantilla guardada");
		m.put("Terminal close", "Cierre del terminal");
		m.put("Terminal input", "Entrada del terminal");
		m.put("Terminal open", "Apertura del terminal");
		m.put("Terminal output", "Salida del terminal");
		m.put("Terminal ready", "Terminal listo");
		m.put("Terminal resize", "Redimensionar terminal");
		m.put("Test", "Prueba");
		m.put("Test Case", "Caso de Prueba");
		m.put("Test Cases", "Casos de Prueba");
		m.put("Test Settings", "Configuraciones de Prueba");
		m.put("Test Suite", "Suite de Pruebas");
		m.put("Test Suites", "Suites de Pruebas");
		m.put("Test importing from {0}", "Importando prueba desde {0}");
		m.put("Test mail has been sent to {0}, please check your mail box", "Se ha enviado un correo de prueba a {0}, por favor verifica tu buzón");
		m.put("Test successful: authentication passed", "Prueba exitosa: autenticación aprobada");
		m.put("Test successful: authentication passed with below information retrieved:", "Prueba exitosa: autenticación aprobada con la siguiente información recuperada:");
		m.put("Text", "Texto");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "La URL del punto final del servidor que recibirá las solicitudes POST del webhook");
		m.put("The change contains disallowed file type(s): {0}", "El cambio contiene tipo(s) de archivo no permitido(s): {0}");
		m.put("The first board will be the default board", "El primer tablero será el tablero predeterminado");
		m.put("The first timesheet will be the default timesheet", "La primera hoja de tiempo será la hoja de tiempo predeterminada");
		m.put("The object you are deleting/disabling is still being used", "El objeto que está eliminando/deshabilitando todavía está siendo utilizado");
		m.put("The password reset url is invalid or obsolete", "La URL de restablecimiento de contraseña es inválida o obsoleta");
		m.put("The permission to access build log", "El permiso para acceder al registro de compilación");
		m.put("The permission to access build pipeline", "El permiso para acceder a la canalización de compilación");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"El permiso para ejecutar trabajos manualmente. También implica el permiso para acceder al registro de compilación, la canalización de compilación y todos los informes publicados");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"El secreto que te permite asegurarte de que las solicitudes POST enviadas a la URL de carga útil provienen de OneDev. Cuando configures un secreto, recibirás el encabezado X-OneDev-Signature en la solicitud POST del webhook");
		m.put("The service desk feature enables user to create issues by sending emails to OneDev. Issues can be discussed over email completely, without the need of logging to OneDev.", 
			"La función de mesa de servicio permite al usuario crear incidencias enviando correos electrónicos a OneDev. Las incidencias pueden discutirse completamente por correo electrónico, sin necesidad de iniciar sesión en OneDev.");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "Luego ingresa el código mostrado en el autenticador TOTP para verificar");
		m.put("Then publish package from project directory like below", "Luego publica el paquete desde el directorio del proyecto como se muestra a continuación");
		m.put("Then push gem to the source", "Luego empuja el gem al origen");
		m.put("Then push image to desired repository under specified project", "Luego empuja la imagen al repositorio deseado bajo el proyecto especificado");
		m.put("Then push package to the source", "Luego empuja el paquete al origen");
		m.put("Then resolve dependency via command step", "Luego resuelve la dependencia mediante el paso de comando");
		m.put("Then upload package to the repository with twine", "Luego sube el paquete al repositorio con twine");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"Hay <a wicket:id=\"openRequests\">solicitudes de extracción abiertas</a> contra la rama <span wicket:id=\"branch\"></span>. Estas solicitudes de extracción serán descartadas si se elimina la rama.");
		m.put("There are incompatibilities since your upgraded version", "Hay incompatibilidades desde su versión actualizada");
		m.put("There are merge conflicts", "Hay conflictos de fusión");
		m.put("There are merge conflicts.", "Hay conflictos de fusión.");
		m.put("There are merge conflicts. You can still create the pull request though", "Hay conflictos de fusión. Sin embargo, aún puedes crear la solicitud de extracción");
		m.put("There are unsaved changes, discard and continue?", "Hay cambios no guardados, ¿descartar y continuar?");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"Estos autenticadores normalmente se ejecutan en tu teléfono móvil, algunos ejemplos son Google Authenticator, Microsoft Authenticator, Authy, 1Password, etc.");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"Este <span wicket:id=\"elementTypeName\"></span> se importa desde <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>");
		m.put("This Month", "Este Mes");
		m.put("This Week", "Esta Semana");
		m.put("This account is disabled", "Esta cuenta está deshabilitada");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"Esta dirección debe ser <code>remitente verificado</code> en SendGrid y se usará como dirección del remitente de varias notificaciones por correo electrónico. También se puede responder a esta dirección para publicar comentarios de incidencias o solicitudes de extracción si la opción <code>Recibir Correo Publicado</code> está habilitada a continuación");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"Esta dirección se usará como dirección del remitente de varias notificaciones por correo electrónico. El usuario también puede responder a esta dirección para publicar comentarios de incidencias o solicitudes de extracción por correo electrónico si la opción <code>Verificar Correo Entrante</code> está habilitada a continuación");
		m.put("This change is already opened for merge by pull request {0}", "Este cambio ya está abierto para fusión por la solicitud de extracción {0}");
		m.put("This change is squashed/rebased onto base branch via a pull request", "Este cambio se ha compactado/rebaseado en la rama base mediante una solicitud de extracción");
		m.put("This change is squashed/rebased onto base branch via pull request {0}", "Este cambio se ha compactado/rebaseado en la rama base mediante la solicitud de extracción {0}");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "Este cambio necesita ser verificado por algunos trabajos. Envía una solicitud de extracción en su lugar");
		m.put("This commit is rebased", "Este commit ha sido rebaseado");
		m.put("This date is using <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 format</a>", 
			"Esta fecha está usando <a href=\"https://www.w3.org/TR/NOTE-datetime\">formato ISO 8601</a>");
		m.put("This email address is being used", "Esta dirección de correo electrónico está siendo utilizada");
		m.put("This executor runs build jobs as docker containers on OneDev server", "Este ejecutor ejecuta trabajos de construcción como contenedores de Docker en el servidor de OneDev");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"Este ejecutor ejecuta trabajos de construcción como contenedores de Docker en máquinas remotas a través de <a href='/~administration/agents' target='_blank'>agentes</a>");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"Este ejecutor ejecuta trabajos de construcción como pods en un clúster de Kubernetes. No se requieren agentes.<b class='text-danger'>Nota:</b> Asegúrese de que la URL del servidor esté especificada correctamente en la configuración del sistema, ya que los pods de trabajo necesitan acceder a ella para descargar el código fuente y los artefactos");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs", 
			"Este ejecutor ejecuta trabajos de construcción con la instalación de shell del servidor de OneDev.<br><b class='text-danger'>ADVERTENCIA</b>: Los trabajos que se ejecutan con este ejecutor tienen los mismos permisos que el proceso del servidor de OneDev. Asegúrese de que solo pueda ser utilizado por trabajos confiables");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. Make sure it can only be used by trusted jobs", 
			"Este ejecutor ejecuta trabajos de construcción con la instalación de shell de máquinas remotas a través de <a href='/~administration/agents' target='_blank'>agentes</a><br><b class='text-danger'>ADVERTENCIA</b>: Los trabajos que se ejecutan con este ejecutor tienen los mismos permisos que el proceso del agente de OneDev. Asegúrese de que solo pueda ser utilizado por trabajos confiables");
		m.put("This field is required", "Este campo es obligatorio");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"Este filtro se utiliza para determinar la entrada LDAP del usuario actual. Por ejemplo: <i>(&(uid={0})(objectclass=person))</i>. En este ejemplo, <i>{0}</i> representa el nombre de inicio de sesión del usuario actual.");
		m.put("This installation does not have an active subscription and runs as community edition. To access <a href=\"https://onedev.io/pricing\">enterprise features</a>, an active subscription is required", 
			"Esta instalación no tiene una suscripción activa y se ejecuta como edición comunitaria. Para acceder a <a href=\"https://onedev.io/pricing\">funciones empresariales</a>, se requiere una suscripción activa");
		m.put("This installation has a trial subscription and is now running as enterprise edition", 
			"Esta instalación tiene una suscripción de prueba y ahora se ejecuta como edición empresarial");
		m.put("This installation has an active subscription and runs as enterprise edition", "Esta instalación tiene una suscripción activa y se ejecuta como edición empresarial");
		m.put("This installation has an expired subscription, and runs as community edition", "Esta instalación tiene una suscripción vencida y se ejecuta como edición comunitaria");
		m.put("This installation has an unlimited users subscription and is now running as enterprise edition", 
			"Esta instalación tiene una suscripción de usuarios ilimitados y ahora se ejecuta como edición enterprise");
		m.put("This installation's subscription has expired and is now running as the community edition", 
			"La suscripción de esta instalación ha expirado y ahora se ejecuta como la edición comunitaria");
		m.put("This is a Git LFS object, but the storage file is missing", "Este es un objeto Git LFS, pero falta el archivo de almacenamiento");
		m.put("This is a built-in role and can not be deleted", "Este es un rol integrado y no se puede eliminar");
		m.put("This is a disabled service account", "Esta es una cuenta de servicio deshabilitada");
		m.put("This is a layer cache. To use the cache, add below option to your docker buildx command", 
			"Este es un caché de capas. Para usar el caché, agregue la siguiente opción a su comando docker buildx");
		m.put("This is a service account for task automation purpose", "Esta es una cuenta de servicio para propósitos de automatización de tareas");
		m.put("This is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Esta es una característica enterprise. <a href='https://onedev.io/pricing' target='_blank'>Pruebe gratis</a> por 30 días");
		m.put("This key has already been used by another project", "Esta clave ya ha sido utilizada por otro proyecto");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"Esta clave está asociada con {0}, sin embargo, NO es una dirección de correo electrónico verificada de este usuario");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here.<br><b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated from these lock files with the <b>generate checksum step</b> defined before this step", 
			"Esta clave se utiliza para determinar si hay un acierto de caché en la jerarquía del proyecto (buscar desde el proyecto actual hasta el proyecto raíz en orden, lo mismo para las claves de carga a continuación). Se considera que hay un acierto de caché si su clave es exactamente la misma que la clave definida aquí.<br><b>NOTA:</b> En caso de que tu proyecto tenga archivos de bloqueo (package.json, pom.xml, etc.) capaces de representar el estado de la caché, esta clave debe definirse como &lt;nombre de caché&gt;-@file:checksum.txt@, donde checksum.txt se genera a partir de estos archivos de bloqueo con el <b>paso de generación de checksum</b> definido antes de este paso");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"Esta clave se utiliza para descargar y cargar caché en la jerarquía del proyecto (buscar desde el proyecto actual hasta el proyecto raíz en orden)");
		m.put("This key or one of its sub key is already added", "Esta clave o una de sus subclaves ya está agregada");
		m.put("This key or one of its subkey is already in use", "Esta clave o una de sus subclaves ya está en uso");
		m.put("This line has confusable unicode character modification", "Esta línea tiene una modificación de caracteres Unicode confusos");
		m.put("This might happen when project points to a wrong git repository, or the commit is garbage collected.", 
			"Esto podría suceder cuando el proyecto apunta a un repositorio git incorrecto o el commit ha sido recolectado como basura.");
		m.put("This might happen when project points to a wrong git repository, or these commits are garbage collected.", 
			"Esto podría suceder cuando el proyecto apunta a un repositorio git incorrecto o estos commits han sido recolectados como basura.");
		m.put("This name has already been used by another board", "Este nombre ya ha sido utilizado por otro tablero");
		m.put("This name has already been used by another group", "Este nombre ya ha sido utilizado por otro grupo");
		m.put("This name has already been used by another issue board in the project", "Este nombre ya ha sido utilizado por otro tablero de problemas en el proyecto");
		m.put("This name has already been used by another job executor", "Este nombre ya ha sido utilizado por otro ejecutor de trabajos");
		m.put("This name has already been used by another project", "Este nombre ya ha sido utilizado por otro proyecto");
		m.put("This name has already been used by another provider", "Este nombre ya ha sido utilizado por otro proveedor");
		m.put("This name has already been used by another role", "Este nombre ya ha sido utilizado por otro rol");
		m.put("This name has already been used by another role.", "Este nombre ya ha sido utilizado por otro rol.");
		m.put("This name has already been used by another script", "Este nombre ya ha sido utilizado por otro script");
		m.put("This name has already been used by another state", "Este nombre ya ha sido utilizado por otro estado");
		m.put("This operation is disallowed by branch protection rule", "Esta operación está prohibida por la regla de protección de ramas");
		m.put("This page lists changes since previous build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a>", 
			"Esta página lista los cambios desde la construcción anterior en <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">la misma corriente</a>");
		m.put("This page lists recent commits fixing the issue", "Esta página lista los commits recientes que solucionan el problema");
		m.put("This permission enables one to access confidential issues", "Este permiso permite acceder a problemas confidenciales");
		m.put("This permission enables one to schedule issues into iterations", "Este permiso permite programar problemas en iteraciones");
		m.put("This property is imported from {0}", "Esta propiedad se importa desde {0}");
		m.put("This pull request has been discarded", "Esta solicitud de extracción ha sido descartada");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"Este informe se mostrará en la página de resumen de la solicitud de extracción si la construcción es activada por una solicitud de extracción");
		m.put("This server is currently accessed via http protocol, please configure your docker daemon or buildx builder to <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">work with insecure registry</a>", 
			"Este servidor está siendo accedido actualmente a través del protocolo http, configure su demonio de Docker o constructor buildx para <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">trabajar con un registro inseguro</a>");
		m.put("This shows average duration of different states over time", "Esto muestra la duración promedio de diferentes estados a lo largo del tiempo");
		m.put("This shows average duration of merged pull requests over time", "Esto muestra la duración promedio de solicitudes de extracción fusionadas a lo largo del tiempo");
		m.put("This shows number of <b>new</b> issues in different states over time", "Esto muestra el número de <b>nuevos</b> problemas en diferentes estados a lo largo del tiempo");
		m.put("This shows number of issues in various states over time", "Esto muestra el número de problemas en varios estados a lo largo del tiempo");
		m.put("This shows number of open and merged pull requests over time", "Esto muestra el número de solicitudes de extracción abiertas y fusionadas a lo largo del tiempo");
		m.put("This step can only be executed by a docker aware executor", "Este paso solo puede ser ejecutado por un ejecutor compatible con Docker");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Este paso solo puede ser ejecutado por un ejecutor compatible con Docker. Se ejecuta bajo <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>el espacio de trabajo del trabajo</a>");
		m.put("This step copies files from job workspace to build artifacts directory, so that they can be accessed after job is completed", 
			"Este paso copia archivos desde el espacio de trabajo del trabajo al directorio de artefactos de construcción, para que puedan ser accedidos después de que el trabajo se complete");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"Este paso publica los archivos especificados para ser servidos como sitio web del proyecto. El sitio web del proyecto puede ser accedido públicamente a través de <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>");
		m.put("This step pulls specified refs from remote", "Este paso extrae las referencias especificadas del remoto");
		m.put("This step pushes current commit to same ref on remote", "Este paso empuja el commit actual a la misma referencia en el remoto");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"Este paso configura el caché de Renovate. Colóquelo antes del paso de Renovate si desea usarlo");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"Este paso configura el caché de la base de datos de trivy para acelerar varios pasos de escaneo. Colóquelo antes de los pasos de escaneo si desea usarlo");
		m.put("This subscription key was already used", "Esta clave de suscripción ya fue utilizada");
		m.put("This subscription key was expired", "Esta clave de suscripción ha expirado");
		m.put("This tab shows the pipeline containing current build. Check <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">this article</a> to understand how build pipeline works", 
			"Esta pestaña muestra el pipeline que contiene la construcción actual. Consulte <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">este artículo</a> para entender cómo funciona el pipeline de construcción");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Este disparador solo será aplicable si el commit etiquetado es alcanzable desde las ramas especificadas aquí. Las ramas múltiples deben separarse con espacios. Use '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefijo con '-' para excluir. Déjelo vacío para coincidir con todas las ramas");
		m.put("This user is authenticating via external system.", "Este usuario se está autenticando a través de un sistema externo.");
		m.put("This user is authenticating via internal database.", "Este usuario se está autenticando a través de la base de datos interna.");
		m.put("This user is currently authenticating via external system. Setting password will switch to use internal database", 
			"Este usuario se está autenticando actualmente a través de un sistema externo. Configurar una contraseña cambiará a usar la base de datos interna");
		m.put("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?", 
			"Esto desactivará la suscripción actual y todas las funciones empresariales serán deshabilitadas, ¿desea continuar?");
		m.put("This will discard all project specific boards, do you want to continue?", "Esto descartará todos los tableros específicos del proyecto, ¿desea continuar?");
		m.put("This will restart SSH server. Do you want to continue?", "Esto reiniciará el servidor SSH. ¿Desea continuar?");
		m.put("Threads", "Hilos");
		m.put("Time Estimate Issue Field", "Campo de Problema de Estimación de Tiempo");
		m.put("Time Range", "Rango de Tiempo");
		m.put("Time Spent Issue Field", "Campo de Problema de Tiempo Gastado");
		m.put("Time Tracking", "Seguimiento de Tiempo");
		m.put("Time Tracking Setting", "Configuración de Seguimiento de Tiempo");
		m.put("Time Tracking Settings", "Configuraciones de Seguimiento de Tiempo");
		m.put("Time tracking settings have been saved", "Las configuraciones de seguimiento de tiempo han sido guardadas");
		m.put("Timed out", "Tiempo agotado");
		m.put("Timeout", "Tiempo límite");
		m.put("Timesheet", "Hoja de tiempo");
		m.put("Timesheet Setting", "Configuración de Hoja de Tiempo");
		m.put("Timesheets", "Hojas de tiempo");
		m.put("Timing", "Sincronización");
		m.put("Title", "Título");
		m.put("To Everyone", "Para Todos");
		m.put("To State", "Al Estado");
		m.put("To States", "A los Estados");
		m.put("To authenticate via internal database, <a wicket:id=\"setPasswordForUser\">set password for user</a> or <a wicket:id=\"tellUserToResetPassword\">tell user to reset password</a>", 
			"Para autenticarse a través de la base de datos interna, <a wicket:id=\"setPasswordForUser\">configure una contraseña para el usuario</a> o <a wicket:id=\"tellUserToResetPassword\">indique al usuario que restablezca la contraseña</a>");
		m.put("To avoid duplication, estimated/remaining time showing here does not include those aggregated from \"{0}\"", 
			"Para evitar duplicaciones, el tiempo estimado/restante que se muestra aquí no incluye aquellos agregados desde \"{0}\"");
		m.put("To avoid duplication, spent time showing here does not include those aggregated from \"{0}\"", 
			"Para evitar duplicaciones, el tiempo gastado que se muestra aquí no incluye aquellos agregados desde \"{0}\"");
		m.put("Toggle change history", "Alternar historial de cambios");
		m.put("Toggle comments", "Alternar comentarios");
		m.put("Toggle commits", "Alternar commits");
		m.put("Toggle dark mode", "Alternar modo oscuro");
		m.put("Toggle detail message", "Alternar mensaje detallado");
		m.put("Toggle fixed width font", "Alternar fuente de ancho fijo");
		m.put("Toggle full screen", "Alternar pantalla completa");
		m.put("Toggle matched contents", "Alternar contenidos coincidentes");
		m.put("Toggle navigation", "Alternar navegación");
		m.put("Toggle work log", "Alternar registro de trabajo");
		m.put("Tokens", "Tokens");
		m.put("Too many commits to load", "Demasiados commits para cargar");
		m.put("Too many commits, displaying recent {0}", "Demasiados commits, mostrando los más recientes {0}");
		m.put("Too many log entries, displaying recent {0}", "Demasiadas entradas de registro, mostrando las más recientes {0}");
		m.put("Too many problems, displaying first {0}", "Demasiados problemas, mostrando los primeros {0}");
		m.put("Toomanyrequests", "Demasiadas solicitudes");
		m.put("Top", "Superior");
		m.put("Topo", "Topo");
		m.put("Total Heap Memory", "Memoria total del heap");
		m.put("Total Number", "Número total");
		m.put("Total Problems", "Problemas totales");
		m.put("Total Size", "Tamaño total");
		m.put("Total Test Duration", "Duración total de pruebas");
		m.put("Total estimated time", "Tiempo estimado total");
		m.put("Total spent time", "Tiempo total gastado");
		m.put("Total spent time / total estimated time", "Tiempo total gastado / tiempo estimado total");
		m.put("Total time", "Tiempo total");
		m.put("Total:", "Total:");
		m.put("Touched File", "Archivo modificado");
		m.put("Touched Files", "Archivos modificados");
		m.put("Transfer LFS Files", "Transferir archivos LFS");
		m.put("Transit manually", "Transitar manualmente");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "Estado de problema \"{0}\" transitado a \"{1}\" ({2})");
		m.put("Transition Edit Bean", "Editar transición Bean");
		m.put("Transition Spec", "Especificación de transición");
		m.put("Trial Expiration Date", "Fecha de expiración de prueba");
		m.put("Trial subscription key not applicable for this installation", "Clave de suscripción de prueba no aplicable para esta instalación");
		m.put("Triggers", "Disparadores");
		m.put("Trivy Container Image Scanner", "Escáner de imágenes de contenedor Trivy");
		m.put("Trivy Filesystem Scanner", "Escáner de sistema de archivos Trivy");
		m.put("Trivy Rootfs Scanner", "Escáner de rootfs Trivy");
		m.put("Try EE", "Probar EE");
		m.put("Try Enterprise Edition", "Probar la edición empresarial");
		m.put("Twitch", "Twitch");
		m.put("Two Factor Authentication", "Autenticación de Dos Factores");
		m.put("Two-factor Authentication", "Autenticación de dos factores");
		m.put("Two-factor authentication already set up. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Request to set up again", 
			"Autenticación de dos factores ya configurada. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Solicitar configurar de nuevo");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"La autenticación de dos factores está habilitada. Por favor, ingrese el código mostrado en su autenticador TOTP. Si encuentra problemas, asegúrese de que el tiempo del servidor OneDev y su dispositivo con el autenticador TOTP estén sincronizados");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"La autenticación de dos factores es obligatoria para su cuenta para mejorar la seguridad. Por favor, siga el procedimiento a continuación para configurarla");
		m.put("Two-factor authentication is now configured", "La autenticación de dos factores está ahora configurada");
		m.put("Two-factor authentication not enabled", "Autenticación de dos factores no habilitada");
		m.put("Type", "Tipo");
		m.put("Type <code>yes</code> below to cancel all queried builds", "Escriba <code>yes</code> abajo para cancelar todas las compilaciones consultadas");
		m.put("Type <code>yes</code> below to cancel selected builds", "Escriba <code>yes</code> abajo para cancelar las compilaciones seleccionadas");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "Escriba <code>yes</code> abajo para confirmar la eliminación de todos los usuarios consultados");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "Escriba <code>yes</code> abajo para confirmar la eliminación de los usuarios seleccionados");
		m.put("Type <code>yes</code> below to copy all queried issues to project \"{0}\"", "Escriba <code>yes</code> abajo para copiar todos los problemas consultados al proyecto \"{0}\"");
		m.put("Type <code>yes</code> below to copy selected issues to project \"{0}\"", "Escriba <code>yes</code> abajo para copiar los problemas seleccionados al proyecto \"{0}\"");
		m.put("Type <code>yes</code> below to delete all queried builds", "Escriba <code>yes</code> abajo para eliminar todas las compilaciones consultadas");
		m.put("Type <code>yes</code> below to delete all queried comments", "Escriba <code>yes</code> abajo para eliminar todos los comentarios consultados");
		m.put("Type <code>yes</code> below to delete all queried issues", "Escriba <code>yes</code> abajo para eliminar todos los problemas consultados");
		m.put("Type <code>yes</code> below to delete all queried packages", "Escriba <code>yes</code> abajo para eliminar todos los paquetes consultados");
		m.put("Type <code>yes</code> below to delete all queried projects", "Escriba <code>yes</code> abajo para eliminar todos los proyectos consultados");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "Escriba <code>yes</code> abajo para eliminar todas las solicitudes de extracción consultadas");
		m.put("Type <code>yes</code> below to delete selected builds", "Escriba <code>yes</code> abajo para eliminar las compilaciones seleccionadas");
		m.put("Type <code>yes</code> below to delete selected comments", "Escriba <code>yes</code> abajo para eliminar los comentarios seleccionados");
		m.put("Type <code>yes</code> below to delete selected issues", "Escriba <code>yes</code> abajo para eliminar los problemas seleccionados");
		m.put("Type <code>yes</code> below to delete selected packages", "Escriba <code>yes</code> abajo para eliminar los paquetes seleccionados");
		m.put("Type <code>yes</code> below to delete selected projects", "Escriba <code>yes</code> abajo para eliminar los proyectos seleccionados");
		m.put("Type <code>yes</code> below to delete selected pull requests", "Escriba <code>yes</code> abajo para eliminar las solicitudes de extracción seleccionadas");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "Escriba <code>yes</code> abajo para descartar todas las solicitudes de extracción consultadas");
		m.put("Type <code>yes</code> below to discard selected pull requests", "Escriba <code>yes</code> abajo para descartar las solicitudes de extracción seleccionadas");
		m.put("Type <code>yes</code> below to move all queried issues to project \"{0}\"", "Escriba <code>yes</code> abajo para mover todos los problemas consultados al proyecto \"{0}\"");
		m.put("Type <code>yes</code> below to move all queried projects to be under \"{0}\"", "Escriba <code>yes</code> abajo para mover todos los proyectos consultados bajo \"{0}\"");
		m.put("Type <code>yes</code> below to move selected issues to project \"{0}\"", "Escriba <code>yes</code> abajo para mover los problemas seleccionados al proyecto \"{0}\"");
		m.put("Type <code>yes</code> below to move selected projects to be under \"{0}\"", "Escriba <code>yes</code> abajo para mover los proyectos seleccionados bajo \"{0}\"");
		m.put("Type <code>yes</code> below to pause all queried agents", "Escriba <code>yes</code> abajo para pausar todos los agentes consultados");
		m.put("Type <code>yes</code> below to re-run all queried builds", "Escriba <code>yes</code> abajo para volver a ejecutar todas las compilaciones consultadas");
		m.put("Type <code>yes</code> below to re-run selected builds", "Escriba <code>yes</code> abajo para volver a ejecutar las compilaciones seleccionadas");
		m.put("Type <code>yes</code> below to remove all queried users from group", "Escriba <code>yes</code> abajo para eliminar todos los usuarios consultados del grupo");
		m.put("Type <code>yes</code> below to remove from all queried groups", "Escriba <code>yes</code> abajo para eliminar de todos los grupos consultados");
		m.put("Type <code>yes</code> below to remove from selected groups", "Escriba <code>yes</code> abajo para eliminar de los grupos seleccionados");
		m.put("Type <code>yes</code> below to remove selected users from group", "Escriba <code>yes</code> abajo para eliminar los usuarios seleccionados del grupo");
		m.put("Type <code>yes</code> below to restart all queried agents", "Escriba <code>yes</code> abajo para reiniciar todos los agentes consultados");
		m.put("Type <code>yes</code> below to restart selected agents", "Escriba <code>yes</code> abajo para reiniciar los agentes seleccionados");
		m.put("Type <code>yes</code> below to resume all queried agents", "Escriba <code>yes</code> abajo para reanudar todos los agentes consultados");
		m.put("Type <code>yes</code> below to set all queried as root projects", "Escriba <code>yes</code> abajo para establecer todos los consultados como proyectos raíz");
		m.put("Type <code>yes</code> below to set selected as root projects", "Escriba <code>yes</code> abajo para establecer los seleccionados como proyectos raíz");
		m.put("Type password here", "Escriba la contraseña aquí");
		m.put("Type to filter", "Escriba para filtrar");
		m.put("Type to filter...", "Escriba para filtrar...");
		m.put("URL", "URL");
		m.put("Unable To Delete/Disable Right Now", "No se puede eliminar/deshabilitar en este momento");
		m.put("Unable to apply change as otherwise you will not be able to manage this project", "No se puede aplicar el cambio ya que de otro modo no podrá gestionar este proyecto");
		m.put("Unable to change password as you are authenticating via external system", "No se puede cambiar la contraseña ya que está autenticándose a través de un sistema externo");
		m.put("Unable to comment", "No se puede comentar");
		m.put("Unable to connect to server", "No se puede conectar al servidor");
		m.put("Unable to create protected branch", "No se puede crear una rama protegida");
		m.put("Unable to create protected tag", "No se puede crear una etiqueta protegida");
		m.put("Unable to diff as some line is too long.", "No se puede realizar la comparación ya que alguna línea es demasiado larga.");
		m.put("Unable to diff as the file is too large.", "No se puede realizar la comparación ya que el archivo es demasiado grande.");
		m.put("Unable to find SSO provider: ", "No se puede encontrar el proveedor SSO:");
		m.put("Unable to find agent {0}", "No se puede encontrar el agente {0}");
		m.put("Unable to find build #{0} in project {1}", "No se puede encontrar la compilación #{0} en el proyecto {1}");
		m.put("Unable to find commit to import build spec (import project: {0}, import revision: {1})", 
			"No se puede encontrar el commit para importar la especificación de compilación (proyecto de importación: {0}, revisión de importación: {1})");
		m.put("Unable to find issue #{0} in project {1}", "No se puede encontrar el problema #{0} en el proyecto {1}");
		m.put("Unable to find project to import build spec: {0}", "No se puede encontrar el proyecto para importar la especificación de compilación: {0}");
		m.put("Unable to find pull request #{0} in project {1}", "No se puede encontrar la solicitud de extracción #{0} en el proyecto {1}");
		m.put("Unable to find timesheet: ", "No se puede encontrar la hoja de tiempo:");
		m.put("Unable to get guilds info", "No se puede obtener información de gremios");
		m.put("Unable to import build spec (import project: {0}, import revision: {1}): {2}", "No se puede importar la especificación de compilación (proyecto de importación: {0}, revisión de importación: {1}): {2}");
		m.put("Unable to notify user as mail service is not configured", "No se puede notificar al usuario ya que el servicio de correo no está configurado");
		m.put("Unable to send password reset email as mail service is not configured", "No se puede enviar el correo electrónico de restablecimiento de contraseña ya que el servicio de correo no está configurado");
		m.put("Unable to send verification email as mail service is not configured yet", "No se puede enviar el correo de verificación ya que el servicio de correo aún no está configurado");
		m.put("Unauthorize this user", "Desautorizar a este usuario");
		m.put("Unauthorized", "No autorizado");
		m.put("Undefined", "Indefinido");
		m.put("Undefined Field Resolution", "Resolución de campo indefinido");
		m.put("Undefined Field Value Resolution", "Resolución de valor de campo indefinido");
		m.put("Undefined State Resolution", "Resolución de Estado Indefinido");
		m.put("Undefined custom field: ", "Campo personalizado indefinido:");
		m.put("Under which condition this step should run. <b>SUCCESSFUL</b> means all non-optional steps running before this step are successful", 
			"Bajo qué condición debe ejecutarse este paso. <b>EXITOSO</b> significa que todos los pasos no opcionales que se ejecutan antes de este paso son exitosos");
		m.put("Unexpected setting: {0}", "Configuración inesperada: {0}");
		m.put("Unexpected ssh signature hash algorithm: ", "Algoritmo de hash de firma ssh inesperado:");
		m.put("Unexpected ssh signature namespace: ", "Espacio de nombres de firma ssh inesperado:");
		m.put("Unified", "Unificado");
		m.put("Unified view", "Vista unificada");
		m.put("Unit Test Statistics", "Estadísticas de Pruebas Unitarias");
		m.put("Unlimited", "Ilimitado");
		m.put("Unlink this issue", "Desvincular este problema");
		m.put("Unordered List", "Lista desordenada");
		m.put("Unordered list", "Lista desordenada");
		m.put("Unpin this issue", "Desanclar este problema");
		m.put("Unresolved", "Sin resolver");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "Comentario sin resolver en el archivo \"{0}\" del proyecto \"{1}\"");
		m.put("Unscheduled", "Sin programar");
		m.put("Unscheduled Issues", "Problemas sin programar");
		m.put("Unsolicited OIDC authentication response", "Respuesta de autenticación OIDC no solicitada");
		m.put("Unsolicited OIDC response", "Respuesta OIDC no solicitada");
		m.put("Unsolicited discord api response", "Respuesta de la API de Discord no solicitada");
		m.put("Unspecified", "No especificado");
		m.put("Unsupported", "No compatible");
		m.put("Unsupported ssh signature algorithm: ", "Algoritmo de firma ssh no compatible:");
		m.put("Unsupported ssh signature version: ", "Versión de firma ssh no compatible:");
		m.put("Unverified", "No verificado");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "La dirección de correo electrónico no verificada <b>NO</b> es aplicable para las funcionalidades anteriores");
		m.put("Unvote", "Quitar voto");
		m.put("Unwatched. Click to watch", "No observado. Haz clic para observar");
		m.put("Update", "Actualizar");
		m.put("Update Dependencies via Renovate", "Actualizar dependencias mediante Renovate");
		m.put("Update Source Branch", "Actualizar rama fuente");
		m.put("Update body", "Actualizar cuerpo");
		m.put("Upload", "Subir");
		m.put("Upload Access Token Secret", "Subir secreto del token de acceso");
		m.put("Upload Cache", "Subir caché");
		m.put("Upload Files", "Subir archivos");
		m.put("Upload Project Path", "Subir ruta del proyecto");
		m.put("Upload Strategy", "Estrategia de subida");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "Subir un archivo PNG transparente de 128x128 para usar como logo en modo oscuro");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "Subir un archivo PNG transparente de 128x128 para usar como logo en modo claro");
		m.put("Upload artifacts", "Subir artefactos");
		m.put("Upload avatar", "Subir avatar");
		m.put("Upload should be less than {0} Mb", "La subida debe ser menor a {0} Mb");
		m.put("Upload to Project", "Subir al proyecto");
		m.put("Uploaded Caches", "Cachés subidos");
		m.put("Uploading file", "Subiendo archivo");
		m.put("Url", "Url");
		m.put("Use '*' for wildcard match", "Usa '*' para coincidencia de comodines");
		m.put("Use '*' or '?' for wildcard match. Prefix with '-' to exclude", "Usa '*' o '?' para coincidencia de comodines. Prefija con '-' para excluir");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Usa '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Usa '**', '*' o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>. Prefija con '-' para excluir");
		m.put("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Usa '**', '*', o '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>coincidencia de comodines de ruta</a>");
		m.put("Use '\\' to escape brackets", "Usa '\\' para escapar corchetes");
		m.put("Use '\\' to escape quotes", "Usa '\\' para escapar comillas");
		m.put("Use @@ to reference scope in job commands to avoid being interpreted as variable", "Usa @@ para referenciar el alcance en comandos de trabajo y evitar que se interprete como variable");
		m.put("Use Avatar Service", "Usar servicio de avatar");
		m.put("Use Default", "Usar predeterminado");
		m.put("Use Default Boards", "Usar tableros predeterminados");
		m.put("Use For Git Operations", "Usar para operaciones Git");
		m.put("Use Git in System Path", "Usar Git en la ruta del sistema");
		m.put("Use Hours And Minutes Only", "Usar solo horas y minutos");
		m.put("Use Specified Git", "Usar Git especificado");
		m.put("Use Specified curl", "Usar curl especificado");
		m.put("Use Step Template", "Usar plantilla de paso");
		m.put("Use curl in System Path", "Usar curl en la ruta del sistema");
		m.put("Use default", "Usar predeterminado");
		m.put("Use default storage class", "Usar clase de almacenamiento predeterminada");
		m.put("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages", 
			"Usar token de trabajo como nombre de usuario para que OneDev sepa qué compilación está ${permission.equals(\"write\")? \"implementando\": \"usando\"} paquetes");
		m.put("Use job token to tell OneDev the build publishing the package", "Usar token de trabajo para decirle a OneDev la compilación que publica el paquete");
		m.put("Use job token to tell OneDev the build pushing the chart", "Usar token de trabajo para decirle a OneDev la compilación que empuja el gráfico");
		m.put("Use job token to tell OneDev the build pushing the package", "Usar token de trabajo para decirle a OneDev la compilación que empuja el paquete");
		m.put("Use job token to tell OneDev the build using the package", "Usar token de trabajo para decirle a OneDev la compilación que usa el paquete");
		m.put("Use project dependency to retrieve artifacts from other projects", "Usar dependencia del proyecto para recuperar artefactos de otros proyectos");
		m.put("Use specified choices", "Usar opciones especificadas");
		m.put("Use specified default value", "Usar valor predeterminado especificado");
		m.put("Use specified value or job secret", "Usar valor especificado o secreto de trabajo");
		m.put("Use specified values or job secrets", "Usar valores especificados o secretos de trabajo");
		m.put("Use triggers to run the job automatically under certain conditions", "Usar disparadores para ejecutar el trabajo automáticamente bajo ciertas condiciones");
		m.put("Use value of specified parameter/secret", "Usar valor del parámetro/secreto especificado");
		m.put("Used Heap Memory", "Memoria del montón utilizada");
		m.put("User", "Usuario");
		m.put("User \"{0}\" unauthorized", "Usuario \"{0}\" no autorizado");
		m.put("User Authorization Bean", "Bean de autorización de usuario");
		m.put("User Authorizations", "Autorizaciones de usuario");
		m.put("User Authorizations Bean", "Bean de autorizaciones de usuario");
		m.put("User Count", "Conteo de usuarios");
		m.put("User Email Attribute", "Atributo de correo electrónico del usuario");
		m.put("User Full Name Attribute", "Atributo de nombre completo del usuario");
		m.put("User Groups Attribute", "Atributo de grupos de usuario");
		m.put("User Invitation", "Invitación de usuario");
		m.put("User Invitation Template", "Plantilla de invitación de usuario");
		m.put("User Management", "Gestión de usuarios");
		m.put("User Match Criteria", "Criterios de coincidencia de usuario");
		m.put("User Name", "Nombre de usuario");
		m.put("User Principal Name", "Nombre principal de usuario");
		m.put("User Profile", "Perfil de usuario");
		m.put("User SSH Key Attribute", "Atributo de clave SSH del usuario");
		m.put("User Search Bases", "Bases de búsqueda de usuario");
		m.put("User Search Filter", "Filtro de búsqueda de usuario");
		m.put("User added to group", "Usuario añadido al grupo");
		m.put("User authorizations updated", "Autorizaciones de usuario actualizadas");
		m.put("User authorized", "Usuario autorizado");
		m.put("User avatar will be requested by appending a hash to this url", "El avatar del usuario será solicitado añadiendo un hash a esta url");
		m.put("User can sign up if this option is enabled", "El usuario puede registrarse si esta opción está habilitada");
		m.put("User disabled", "Usuario deshabilitado");
		m.put("User name", "Nombre de usuario");
		m.put("User name already used by another account", "Nombre de usuario ya utilizado por otra cuenta");
		m.put("Users", "Usuarios");
		m.put("Users converted to service accounts successfully", "Usuarios convertidos a cuentas de servicio exitosamente");
		m.put("Users deleted successfully", "Usuarios eliminados exitosamente");
		m.put("Users disabled successfully", "Usuarios deshabilitados exitosamente");
		m.put("Users enabled successfully", "Usuarios habilitados exitosamente");
		m.put("Utilities", "Utilidades");
		m.put("Valid signature required for head commit of this branch per branch protection rule", 
			"Firma válida requerida para el commit principal de esta rama según la regla de protección de ramas");
		m.put("Value", "Valor");
		m.put("Value Matcher", "Coincidencia de Valor");
		m.put("Value Provider", "Proveedor de Valor");
		m.put("Values", "Valores");
		m.put("Values Provider", "Proveedor de Valores");
		m.put("Variable", "Variable");
		m.put("Verification Code", "Código de Verificación");
		m.put("Verification email sent, please check it", "Correo de verificación enviado, por favor revísalo");
		m.put("Verify", "Verificar");
		m.put("View", "Ver");
		m.put("View Source", "Ver Fuente");
		m.put("View source", "Ver fuente");
		m.put("View statistics", "Ver estadísticas");
		m.put("Viewer", "Visualizador");
		m.put("Volume Mount", "Montaje de Volumen");
		m.put("Volume Mounts", "Montajes de Volumen");
		m.put("Vote", "Votar");
		m.put("Votes", "Votos");
		m.put("WAITING", "ESPERANDO");
		m.put("WARNING:", "ADVERTENCIA:");
		m.put("Waiting", "Esperando");
		m.put("Waiting for approvals", "Esperando aprobaciones");
		m.put("Waiting for test mail to come back...", "Esperando que regrese el correo de prueba...");
		m.put("Watch", "Observar");
		m.put("Watch Status", "Estado de Observación");
		m.put("Watch if involved", "Observar si está involucrado");
		m.put("Watch if involved (default)", "Observar si está involucrado (por defecto)");
		m.put("Watch status changed", "Estado de observación cambiado");
		m.put("Watch/Unwatch All Queried Issues", "Observar/Dejar de observar todos los problemas consultados");
		m.put("Watch/Unwatch All Queried Pull Requests", "Observar/Dejar de observar todas las solicitudes de extracción consultadas");
		m.put("Watch/Unwatch Selected Pull Requests", "Observar/Dejar de observar las solicitudes de extracción seleccionadas");
		m.put("Watched. Click to unwatch", "Observado. Haz clic para dejar de observar");
		m.put("Watchers", "Observadores");
		m.put("Web Hook", "Web Hook");
		m.put("Web Hooks", "Web Hooks");
		m.put("Web Hooks Bean", "Bean de Web Hooks");
		m.put("Web hooks saved", "Web hooks guardados");
		m.put("Webhook Url", "URL del Webhook");
		m.put("Week", "Semana");
		m.put("When", "Cuando");
		m.put("When authorize a group, the group will also be authorized with the role for all child projects", 
			"Cuando se autoriza un grupo, el grupo también será autorizado con el rol para todos los proyectos hijos");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"Cuando se autoriza un proyecto, todos los proyectos hijos también serán autorizados con los roles asignados");
		m.put("When authorize a user, the user will also be authorized with the role for all child projects", 
			"Cuando se autoriza un usuario, el usuario también será autorizado con el rol para todos los proyectos hijos");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"Cuando se determina si el usuario es autor/committer de un commit de git, se verificarán todos los correos electrónicos listados aquí");
		m.put("When evaluating this template, below variables will be available:", "Al evaluar esta plantilla, las siguientes variables estarán disponibles:");
		m.put("When login via OneDev's built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database", 
			"Cuando inicies sesión a través del formulario incorporado de OneDev, las credenciales de usuario enviadas pueden ser verificadas contra el autenticador definido aquí, además de la base de datos interna");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"Cuando la rama objetivo de una solicitud de extracción tiene nuevos commits, el commit de fusión de la solicitud de extracción se recalculará, y esta opción indica si se aceptan o no las compilaciones de solicitudes de extracción ejecutadas en el commit de fusión anterior. Si está habilitado, necesitarás volver a ejecutar las compilaciones requeridas en el nuevo commit de fusión. Esta configuración solo tiene efecto cuando se especifican compilaciones requeridas");
		m.put("When this work starts", "Cuando comienza este trabajo");
		m.put("When {0}", "Cuando {0}");
		m.put("Whether or not created issue should be confidential", "Si el problema creado debe ser confidencial o no");
		m.put("Whether or not multiple issues can be linked", "Si se pueden vincular múltiples problemas o no");
		m.put("Whether or not multiple issues can be linked on the other side. For instance sub issues on the other side means parent issue, and multiple should be false on that side if only one parent is allowed", 
			"Si se pueden vincular múltiples problemas en el otro lado o no. Por ejemplo, los subproblemas en el otro lado significan problema padre, y múltiples deben ser falsos en ese lado si solo se permite un padre");
		m.put("Whether or not multiple values can be specified for this field", "Si se pueden especificar múltiples valores para este campo o no");
		m.put("Whether or not multiple values can be specified for this param", "Si se pueden especificar múltiples valores para este parámetro o no");
		m.put("Whether or not the issue should be confidential", "Si el problema debe ser confidencial o no");
		m.put("Whether or not the link is asymmetric. A asymmetric link has different meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric", 
			"Si el enlace es asimétrico o no. Un enlace asimétrico tiene un significado diferente desde cada lado. Por ejemplo, un enlace 'padre-hijo' es asimétrico, mientras que un enlace 'relacionado con' es simétrico");
		m.put("Whether or not this field accepts empty value", "Si este campo acepta valores vacíos o no");
		m.put("Whether or not this param accepts empty value", "Si este parámetro acepta valores vacíos o no");
		m.put("Whether or not this script can be used in CI/CD jobs", "Si este script puede ser usado en trabajos de CI/CD o no");
		m.put("Whether or not this step is optional. Execution failure of an optional step will not cause the build to fail, and successful condition of subsequent steps will not take optional step into account", 
			"Si este paso es opcional o no. El fallo de ejecución de un paso opcional no causará que la compilación falle, y la condición exitosa de los pasos posteriores no tomará en cuenta el paso opcional");
		m.put("Whether or not to allow anonymous users to access this server", "Si se permite o no que usuarios anónimos accedan a este servidor");
		m.put("Whether or not to allow creating root projects (project without parent)", "Si se permite o no crear proyectos raíz (proyecto sin padre)");
		m.put("Whether or not to also include children of above projects", "Si se incluye o no a los hijos de los proyectos anteriores");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same machine", 
			"Si siempre se debe extraer la imagen al ejecutar el contenedor o construir imágenes o no. Esta opción debe estar habilitada para evitar que las imágenes sean reemplazadas por trabajos maliciosos que se ejecuten en la misma máquina");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same node", 
			"Si siempre se debe extraer la imagen al ejecutar el contenedor o construir imágenes o no. Esta opción debe estar habilitada para evitar que las imágenes sean reemplazadas por trabajos maliciosos que se ejecuten en el mismo nodo");
		m.put("Whether or not to be able to access time tracking info of issues", "Si se puede acceder o no a la información de seguimiento de tiempo de los problemas");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities", 
			"Si se crea como una cuenta de servicio para propósitos de automatización de tareas o no. La cuenta de servicio no tiene contraseña ni direcciones de correo electrónico, y no generará notificaciones por sus actividades");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Si se crea como una cuenta de servicio para propósitos de automatización de tareas o no. La cuenta de servicio no tiene contraseña ni direcciones de correo electrónico, y no generará notificaciones por sus actividades. <b class='text-warning'>NOTA:</b> La cuenta de servicio es una característica empresarial. <a href='https://onedev.io/pricing' target='_blank'>Prueba gratis</a> por 30 días");
		m.put("Whether or not to enable code management for the project", "Si se habilita o no la gestión de código para el proyecto");
		m.put("Whether or not to enable issue management for the project", "Si se habilita o no la gestión de problemas para el proyecto");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project.", 
			"Si se deben obtener objetos LFS si la solicitud de extracción se abre desde un proyecto diferente o no");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project. If this option is enabled, git lfs command needs to be installed on OneDev server", 
			"Si se deben obtener objetos LFS si la solicitud de extracción se abre desde un proyecto diferente o no. Si esta opción está habilitada, el comando git lfs necesita estar instalado en el servidor de OneDev");
		m.put("Whether or not to import forked Bitbucket repositories", "Si se deben importar repositorios bifurcados de Bitbucket o no");
		m.put("Whether or not to import forked GitHub repositories", "Si se deben importar repositorios bifurcados de GitHub o no");
		m.put("Whether or not to import forked GitLab projects", "Si se deben importar proyectos bifurcados de GitLab o no");
		m.put("Whether or not to import forked Gitea repositories", "Si se deben importar repositorios bifurcados de Gitea o no");
		m.put("Whether or not to include forked repositories", "Si se deben incluir repositorios bifurcados o no");
		m.put("Whether or not to include this field when issue is initially opened. If not, you may include this field later when issue is transited to other states via issue transition rule", 
			"Si se debe incluir este campo cuando el problema se abre inicialmente o no. Si no, puedes incluir este campo más tarde cuando el problema se transite a otros estados mediante la regla de transición de problemas");
		m.put("Whether or not to input and display estimated/spent time in hours/minutes only", "Si se debe ingresar y mostrar el tiempo estimado/gastado solo en horas/minutos o no");
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. Make sure this executor can only be used by trusted jobs if this option is enabled", 
			"Si se debe montar o no el socket de Docker en el contenedor del trabajo para admitir operaciones de Docker en los comandos del trabajo<br><b class='text-danger'>ADVERTENCIA</b>: Los trabajos maliciosos pueden tomar el control de todo OneDev operando el socket de Docker montado. Asegúrese de que este ejecutor solo pueda ser utilizado por trabajos confiables si esta opción está habilitada");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"Si se deben pre-poblar las asignaciones de etiquetas en la siguiente página o no. Es posible que desees deshabilitar esto si hay demasiadas etiquetas para mostrar");
		m.put("Whether or not to require this dependency to be successful", "Si se requiere que esta dependencia sea exitosa o no");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"Si se deben recuperar los grupos del usuario de inicio de sesión o no. Asegúrate de agregar la reclamación de grupos mediante la configuración de tokens de la aplicación registrada en Entra ID si esta opción está habilitada. La reclamación de grupos debe devolver el ID del grupo (la opción predeterminada) mediante varios tipos de tokens en este caso");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"Si se deben recuperar submódulos o no. Consulta <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>este tutorial</a> sobre cómo configurar la credencial de clonación anterior para recuperar submódulos");
		m.put("Whether or not to run this step inside container", "Si este paso se ejecutará dentro de un contenedor o no");
		m.put("Whether or not to scan recursively in above paths", "Si se debe escanear de forma recursiva en las rutas anteriores o no");
		m.put("Whether or not to send notifications for events generated by yourself", "Si se deben enviar notificaciones por eventos generados por ti mismo o no");
		m.put("Whether or not to send notifications to issue watchers for this change", "Si se deben enviar notificaciones a los observadores del problema por este cambio o no");
		m.put("Whether or not to show branch/tag column", "Si se debe mostrar la columna de rama/etiqueta o no");
		m.put("Whether or not to show duration column", "Si se debe mostrar la columna de duración o no");
		m.put("Whether or not to use user avatar from a public service", "Si se debe usar el avatar del usuario desde un servicio público o no");
		m.put("Whether or not use force option to overwrite changes in case ref updating can not be fast-forwarded", 
			"Si se debe usar la opción de fuerza para sobrescribir cambios en caso de que la actualización de la referencia no pueda ser avanzada rápidamente o no");
		m.put("Whether or not user can remove own account", "Si el usuario puede eliminar su propia cuenta o no");
		m.put("Whether the password must contain at least one lowercase letter", "Si la contraseña debe contener al menos una letra minúscula");
		m.put("Whether the password must contain at least one number", "Si la contraseña debe contener al menos un número");
		m.put("Whether the password must contain at least one special character", "Si la contraseña debe contener al menos un carácter especial");
		m.put("Whether the password must contain at least one uppercase letter", "Si la contraseña debe contener al menos una letra mayúscula");
		m.put("Whole Word", "Palabra Completa");
		m.put("Widget", "Widget");
		m.put("Widget Tab", "Pestaña de Widget");
		m.put("Widget Timesheet Setting", "Configuración de Hoja de Tiempo del Widget");
		m.put("Will be prompted to set up two-factor authentication upon next login", "Se le pedirá configurar la autenticación de dos factores en el próximo inicio de sesión");
		m.put("Will be transcoded to UTF-8", "Será transcodificado a UTF-8");
		m.put("Window", "Ventana");
		m.put("Window Memory", "Memoria de Ventana");
		m.put("With current number of users ({0}), the subscription will be active until <b>{1}</b>", 
			"Con el número actual de usuarios ({0}), la suscripción estará activa hasta <b>{1}</b>");
		m.put("Workflow reconciliation completed", "Reconciliación del flujo de trabajo completada");
		m.put("Working Directory", "Directorio de Trabajo");
		m.put("Write", "Escribir");
		m.put("YAML", "YAML");
		m.put("Yes", "Sí");
		m.put("You are not member of discord server", "No eres miembro del servidor de Discord");
		m.put("You are rebasing source branch on top of target branch", "Estás rebasando la rama fuente sobre la rama objetivo");
		m.put("You are viewing a subset of all changes. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">show all changes</a>", 
			"Estás viendo un subconjunto de todos los cambios. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">mostrar todos los cambios</a>");
		m.put("You can also achieve this by adding a build docker image step to your CI/CD job and configuring the built-in registry login with an access token secret that has package write permissions", 
			"También puedes lograr esto agregando un paso de construcción de imagen Docker a tu trabajo CI/CD y configurando el inicio de sesión del registro integrado con un secreto de token de acceso que tenga permisos de escritura de paquetes");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "Tienes <a wicket:id=\"hasUnverifiedLink\">direcciones de correo electrónico</a> no verificadas");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "También puedes soltar un archivo/imagen en el cuadro de entrada, o pegar una imagen desde el portapapeles");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"Puedes inicializar el proyecto <a wicket:id=\"addFiles\" class=\"link-primary\">agregando archivos</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">configurando la especificación de construcción</a>, o <a wicket:id=\"pushInstructions\" class=\"link-primary\">empujando un repositorio existente</a>");
		m.put("You selected to delete branch \"{0}\"", "Seleccionaste eliminar la rama \"{0}\"");
		m.put("You will be notified of any activities", "Serás notificado de cualquier actividad");
		m.put("You've been logged out", "Has cerrado sesión");
		m.put("YouTrack API URL", "URL de la API de YouTrack");
		m.put("YouTrack Issue Field", "Campo de Problema de YouTrack");
		m.put("YouTrack Issue Link", "Enlace de Problema de YouTrack");
		m.put("YouTrack Issue State", "Estado de Problema de YouTrack");
		m.put("YouTrack Issue Tag", "Etiqueta de Problema de YouTrack");
		m.put("YouTrack Login Name", "Nombre de Usuario de YouTrack");
		m.put("YouTrack Password or Access Token", "Contraseña o Token de Acceso de YouTrack");
		m.put("YouTrack Project", "Proyecto de YouTrack");
		m.put("YouTrack Projects to Import", "Proyectos de YouTrack para Importar");
		m.put("Your email address is now verified", "Su dirección de correo electrónico ahora está verificada");
		m.put("Your primary email address is not verified", "Su dirección de correo electrónico principal no está verificada");
		m.put("[Any state]", "[Cualquier estado]");
		m.put("[Reset Password] Please Reset Your OneDev Password", "[Restablecer Contraseña] Por favor, restablece tu contraseña de OneDev");
		m.put("a boolean indiciating whether or not topic comment can be created directly by replying the email", 
			"un booleano que indica si el comentario del tema puede ser creado directamente respondiendo al correo electrónico");
		m.put("a new agent token will be generated each time this button is pressed", "se generará un nuevo token de agente cada vez que se presione este botón");
		m.put("a string representing body of the event. May be <code>null</code>", "una cadena que representa el cuerpo del evento. Puede ser <code>null</code>");
		m.put("a string representing event detail url", "una cadena que representa la URL de detalle del evento");
		m.put("a string representing summary of the event", "una cadena que representa el resumen del evento");
		m.put("access [{0}]", "acceso [{0}]");
		m.put("active", "activo");
		m.put("add another order", "agregar otro pedido");
		m.put("adding .onedev-buildspec.yml", "agregando .onedev-buildspec.yml");
		m.put("after specified date", "después de la fecha especificada");
		m.put("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information.  A <code>null</code> value means that the notification can not be unsubscribed", 
			"un <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>objeto</a> que contiene información de cancelación de suscripción. Un valor <code>null</code> significa que la notificación no puede ser cancelada");
		m.put("and more", "y más");
		m.put("archived", "archivado");
		m.put("artifacts", "artefactos");
		m.put("assign to me", "asignar a mí");
		m.put("authored by", "autorizado por");
		m.put("backlog ", "pendiente");
		m.put("base", "base");
		m.put("before specified date", "antes de la fecha especificada");
		m.put("branch the build commit is merged into", "rama en la que se fusiona el commit de construcción");
		m.put("branch the job is running against", "rama contra la que se ejecuta el trabajo");
		m.put("branch {0}", "rama {0}");
		m.put("branches", "ramas");
		m.put("build", "construcción");
		m.put("build is successful for any job and branch", "la construcción es exitosa para cualquier trabajo y rama");
		m.put("build is successful for any job on branches \"{0}\"", "la construcción es exitosa para cualquier trabajo en las ramas \"{0}\"");
		m.put("build is successful for jobs \"{0}\" on any branch", "la construcción es exitosa para los trabajos \"{0}\" en cualquier rama");
		m.put("build is successful for jobs \"{0}\" on branches \"{1}\"", "la construcción es exitosa para los trabajos \"{0}\" en las ramas \"{1}\"");
		m.put("builds", "construcciones");
		m.put("cURL Example", "Ejemplo de cURL");
		m.put("choose a color for this state", "elige un color para este estado");
		m.put("cluster:lead", "líder");
		m.put("cmd-k to show command palette", "cmd-k para mostrar el paleta de comandos");
		m.put("code commit", "commit de código");
		m.put("code is committed", "el código está comprometido");
		m.put("code is committed to branches \"{0}\"", "el código se compromete a las ramas \"{0}\"");
		m.put("code is committed to branches \"{0}\" with message \"{1}\"", "el código está comprometido en las ramas \"{0}\" con el mensaje \"{1}\"");
		m.put("code is committed with message \"{0}\"", "el código está comprometido con el mensaje \"{0}\"");
		m.put("commit message contains", "el mensaje del commit contiene");
		m.put("commits", "commits");
		m.put("committed by", "comprometido por");
		m.put("common", "común");
		m.put("common ancestor", "ancestro común");
		m.put("container:image", "Imagen");
		m.put("copy", "copiar");
		m.put("ctrl-k to show command palette", "ctrl-k para mostrar el paleta de comandos");
		m.put("curl Command Line", "Línea de Comando de curl");
		m.put("curl Path", "Ruta de curl");
		m.put("default", "predeterminado");
		m.put("descending", "descendente");
		m.put("disabled", "deshabilitado");
		m.put("does not have any value of", "no tiene ningún valor de");
		m.put("duration", "duración");
		m.put("enclose with ~ to query hash/message", "encierra con ~ para consultar hash/mensaje");
		m.put("enclose with ~ to query job/version", "encierra con ~ para consultar trabajo/versión");
		m.put("enclose with ~ to query name/ip/os", "encierra con ~ para consultar nombre/ip/os");
		m.put("enclose with ~ to query name/path", "encierra con ~ para consultar nombre/ruta");
		m.put("enclose with ~ to query name/version", "encierra con ~ para consultar nombre/versión");
		m.put("enclose with ~ to query path/content/reply", "encierra con ~ para consultar ruta/contenido/respuesta");
		m.put("enclose with ~ to query title/description/comment", "encierra con ~ para consultar título/descripción/comentario");
		m.put("exclude", "excluir");
		m.put("false", "falso");
		m.put("files with ext \"{0}\"", "archivos con extensión \"{0}\"");
		m.put("find build by number", "buscar construcción por número");
		m.put("find build with this number", "buscar construcción con este número");
		m.put("find issue by number", "buscar problema por número");
		m.put("find pull request by number", "buscar solicitud de extracción por número");
		m.put("find pull request with this number", "buscar solicitud de extracción con este número");
		m.put("forked from <a wicket:id=\"forkedFrom\"></a>", "bifurcado de <a wicket:id=\"forkedFrom\"></a>");
		m.put("found 1 agent", "encontrado 1 agente");
		m.put("found 1 build", "encontrada 1 compilación");
		m.put("found 1 comment", "encontrado 1 comentario");
		m.put("found 1 issue", "encontrado 1 problema");
		m.put("found 1 package", "encontrado 1 paquete");
		m.put("found 1 project", "encontrado 1 proyecto");
		m.put("found 1 pull request", "encontrada 1 solicitud de extracción");
		m.put("found 1 user", "encontrado 1 usuario");
		m.put("found {0} agents", "encontrados {0} agentes");
		m.put("found {0} builds", "encontradas {0} compilaciones");
		m.put("found {0} comments", "encontrados {0} comentarios");
		m.put("found {0} issues", "encontrados {0} problemas");
		m.put("found {0} packages", "encontrados {0} paquetes");
		m.put("found {0} projects", "encontrados {0} proyectos");
		m.put("found {0} pull requests", "encontradas {0} solicitudes de extracción");
		m.put("found {0} users", "encontrados {0} usuarios");
		m.put("has any value of", "tiene algún valor de");
		m.put("head", "cabecera");
		m.put("in current commit", "en el commit actual");
		m.put("ineffective", "ineficaz");
		m.put("inherited", "heredado");
		m.put("initial", "inicial");
		m.put("is empty", "está vacío");
		m.put("is not empty", "no está vacío");
		m.put("issue", "problema");
		m.put("issue:Number", "Número");
		m.put("issues", "problemas");
		m.put("job", "trabajo");
		m.put("key ID: ", "ID de clave:");
		m.put("lines", "líneas");
		m.put("link:Multiple", "Múltiples");
		m.put("log", "registro");
		m.put("manage job", "gestionar trabajo");
		m.put("markdown:heading", "Título");
		m.put("markdown:image", "Imagen");
		m.put("may not be empty", "no puede estar vacío");
		m.put("merged", "fusionado");
		m.put("month:Apr", "Abr");
		m.put("month:Aug", "Ago");
		m.put("month:Dec", "Dic");
		m.put("month:Feb", "Feb");
		m.put("month:Jan", "Ene");
		m.put("month:Jul", "Jul");
		m.put("month:Jun", "Jun");
		m.put("month:Mar", "Mar");
		m.put("month:May", "May");
		m.put("month:Nov", "Nov");
		m.put("month:Oct", "Oct");
		m.put("month:Sep", "Sep");
		m.put("n/a", "n/a");
		m.put("new field", "nuevo campo");
		m.put("no activity for {0} days", "sin actividad durante {0} días");
		m.put("on file {0}", "en el archivo {0}");
		m.put("opened", "abierto");
		m.put("opened <span wicket:id=\"submitDate\"></span>", "abierto <span wicket:id=\"submitDate\"></span>");
		m.put("or match another value", "o coincidir con otro valor");
		m.put("order more", "ordenar más");
		m.put("outdated", "desactualizado");
		m.put("pack", "paquete");
		m.put("package", "paquete");
		m.put("packages", "paquetes");
		m.put("personal", "personal");
		m.put("pipeline", "pipeline");
		m.put("project of the running job", "proyecto del trabajo en ejecución");
		m.put("property", "propiedad");
		m.put("pull request", "solicitud de extracción");
		m.put("pull request #{0}", "solicitud de extracción #{0}");
		m.put("pull request and code review", "solicitud de extracción y revisión de código");
		m.put("pull request to any branch is discarded", "solicitud de extracción a cualquier rama es descartada");
		m.put("pull request to any branch is merged", "solicitud de extracción a cualquier rama es fusionada");
		m.put("pull request to any branch is opened", "solicitud de extracción a cualquier rama es abierta");
		m.put("pull request to branches \"{0}\" is discarded", "solicitud de extracción a las ramas \"{0}\" es descartada");
		m.put("pull request to branches \"{0}\" is merged", "solicitud de extracción a las ramas \"{0}\" es fusionada");
		m.put("pull request to branches \"{0}\" is opened", "solicitud de extracción a las ramas \"{0}\" es abierta");
		m.put("pull requests", "solicitudes de extracción");
		m.put("reconciliation (need administrator permission)", "reconciliación (requiere permiso de administrador)");
		m.put("reports", "informes");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> object to be notified", 
			"representa el objeto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>compilación</a> que será notificado");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk", 
			"representa el <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> que se abre a través del servicio de asistencia");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified", 
			"representa el objeto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> que será notificado");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> object to be notified", 
			"representa el objeto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>paquete</a> que será notificado");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified", 
			"representa el objeto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>solicitud de extracción</a> que será notificado");
		m.put("represents the <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> object to be notified", 
			"representa el objeto <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> que será notificado");
		m.put("represents the exception encountered when open issue via service desk", "representa la excepción encontrada al abrir un problema a través del servicio de asistencia");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>", 
			"representa el <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> no suscrito");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>", 
			"representa la <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>solicitud de extracción</a> no suscrita");
		m.put("request to change", "solicitud de cambio");
		m.put("root", "raíz");
		m.put("root url of OneDev server", "url raíz del servidor OneDev");
		m.put("run job", "ejecutar trabajo");
		m.put("search in this revision will be accurate after indexed", "la búsqueda en esta revisión será precisa después de ser indexada");
		m.put("service", "servicio");
		m.put("severity:CRITICAL", "Crítico");
		m.put("severity:HIGH", "Alto");
		m.put("severity:LOW", "Bajo");
		m.put("severity:MEDIUM", "Medio");
		m.put("skipped {0} lines", "saltadas {0} líneas");
		m.put("space", "espacio");
		m.put("state of an issue is transited", "el estado de un problema es transitado");
		m.put("step template", "plantilla de paso");
		m.put("submit", "enviar");
		m.put("tag the job is running against", "etiquetar el trabajo que se está ejecutando");
		m.put("tag {0}", "etiqueta {0}");
		m.put("tags", "etiquetas");
		m.put("the url to set up user account", "la URL para configurar la cuenta de usuario");
		m.put("time aggregation link", "enlace de agregación de tiempo");
		m.put("touching specified path", "tocando la ruta especificada");
		m.put("transit manually by any user", "transitar manualmente por cualquier usuario");
		m.put("transit manually by any user of roles \"{0}\"", "transitar manualmente por cualquier usuario de roles \"{0}\"");
		m.put("true", "verdadero");
		m.put("true for html version, false for text version", "verdadero para la versión HTML, falso para la versión de texto");
		m.put("up to date", "actualizado");
		m.put("url following which to verify email address", "URL para verificar la dirección de correo electrónico");
		m.put("url to reset password", "URL para restablecer la contraseña");
		m.put("value needs to be enclosed in brackets", "el valor debe estar entre corchetes");
		m.put("value needs to be enclosed in parenthesis", "el valor debe estar entre paréntesis");
		m.put("value should be quoted", "el valor debe estar entre comillas");
		m.put("w%02d", "w%02d");
		m.put("week:Fri", "Vie");
		m.put("week:Mon", "Lun");
		m.put("week:Sat", "Sáb");
		m.put("week:Sun", "Dom");
		m.put("week:Thu", "Jue");
		m.put("week:Tue", "Mar");
		m.put("week:Wed", "Mié");
		m.put("widget:Tabs", "Pestañas");
		m.put("you may show this page later via incompatibilities link in help menu", "puede mostrar esta página más tarde a través del enlace de incompatibilidades en el menú de ayuda");
		m.put("{0} Month(s)", "{0} Mes(es)");
		m.put("{0} activities on {1}", "{0} actividades en {1}");
		m.put("{0} additions & {1} deletions", "{0} adiciones & {1} eliminaciones");
		m.put("{0} ahead", "{0} adelante");
		m.put("{0} behind", "{0} atrás");
		m.put("{0} branches", "{0} ramas");
		m.put("{0} build(s)", "{0} compilación(es)");
		m.put("{0} child projects", "{0} proyectos hijos");
		m.put("{0} commits", "{0} commits");
		m.put("{0} commits ahead of base branch", "{0} commits adelante de la rama base");
		m.put("{0} commits behind of base branch", "{0} commits atrás de la rama base");
		m.put("{0} day", "{0} día");
		m.put("{0} days", "{0} días");
		m.put("{0} edited {1}", "{0} editó {1}");
		m.put("{0} files", "{0} archivos");
		m.put("{0} forks", "{0} bifurcaciones");
		m.put("{0} hour", "{0} hora");
		m.put("{0} hours", "{0} horas");
		m.put("{0} inaccessible activities", "{0} actividades inaccesibles");
		m.put("{0} minute", "{0} minuto");
		m.put("{0} minutes", "{0} minutos");
		m.put("{0} reviewed", "{0} revisado");
		m.put("{0} second", "{0} segundo");
		m.put("{0} seconds", "{0} segundos");
		m.put("{0} tags", "{0} etiquetas");
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
