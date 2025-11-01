package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_pt extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init(m);
		Translation.watchUpdate(Translation_pt.class, () -> {
			init(m);
		});
	}

	@SystemPrompt("You are good at translating from English to Portuguese in DevOps software area.")
	public static void init(Map<String, String> m) {
		m.clear();
		m.put(" Project path can be omitted if reference from current project", "O caminho do projeto pode ser omitido se referenciado a partir do projeto atual");
		m.put("'..' is not allowed in the directory", "'..' não é permitido no diretório");
		m.put("(* = any string, ? = any character)", "(* = qualquer string, ? = qualquer caractere)");
		m.put("(on behalf of <b>{0}</b>)", "(em nome de <b>{0}</b>)");
		m.put("** Enterprise edition is disabled as the subscription was expired. Renew to enable **", 
			"** A edição Enterprise está desativada pois a assinatura expirou. Renove para habilitar **");
		m.put("** Enterprise edition is disabled as the trial subscription was expired, order subscription to enable or contact support@onedev.io if you need to extend your trial **", 
			"** A edição Enterprise está desativada porque a assinatura de teste expirou, solicite uma assinatura para ativar ou entre em contato com support@onedev.io se precisar estender seu teste **");
		m.put("** Enterprise edition is disabled as there is no remaining user months. Order more to enable **", 
			"** A edição Enterprise está desativada porque não há meses de usuário restantes. Solicite mais para ativar **");
		m.put("1. To use this package, add below to project pom.xml", "1. Para usar este pacote, adicione o seguinte ao pom.xml do projeto");
		m.put("1. Use below repositories in project pom.xml", "1. Use os repositórios abaixo no pom.xml do projeto");
		m.put("1w 1d 1h 1m", "1s 1d 1h 1m");
		m.put("2. Add below to <code>$HOME/.m2/settings.xml</code> if you want to deploy from command line", 
			"2. Adicione o seguinte ao <code>$HOME/.m2/settings.xml</code> se quiser implantar a partir da linha de comando");
		m.put("2. Also add below to $HOME/.m2/settings.xml if you want to compile project from command line", 
			"2. Também adicione o seguinte ao $HOME/.m2/settings.xml se quiser compilar o projeto a partir da linha de comando");
		m.put("3. For CI/CD job, it is more convenient to use a custom settings.xml, for instance via below code in a command step:", 
			"3. Para trabalho de CI/CD, é mais conveniente usar um settings.xml personalizado, por exemplo, via o código abaixo em um passo de comando:");
		m.put("6-digits passcode", "Código de acesso de 6 dígitos");
		m.put("7 days", "7 dias");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">usuário</a> para redefinir a senha");
		m.put("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for", 
			"<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">usuário</a> para verificar o e-mail");
		m.put("<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">GitHub flavored markdown</a> is accepted, with <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">mermaid and katex support</a>.", 
			"<a href=\"https://guides.github.com/features/mastering-markdown/\" target=\"_blank\">Markdown com estilo GitHub</a> é aceito, com <a href=\"https://docs.onedev.io/appendix/markdown-syntax\" target=\"_blank\">suporte a mermaid e katex</a>.");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>objeto de evento</a> que dispara a notificação");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alerta</a> para exibir");
		m.put("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Stopwatch</a> overdue", 
			"<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Stopwatch.java'>Cronômetro</a> vencido");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> fez o commit <span wicket:id=\"date\"></span>");
		m.put("<a wicket:id=\"author\" class=\"name link-gray\"></a> committed with <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>", 
			"<a wicket:id=\"author\" class=\"name link-gray\"></a> fez o commit com <a wicket:id=\"committer\" class=\"name link-gray\"></a> <span wicket:id=\"date\"></span>");
		m.put("<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depends on me", "<a wicket:id=\"dependents\"><span wicket:id=\"label\"></span></a> depende de mim");
		m.put("<a wicket:id=\"removePassword\">Remove password</a> to force the user to authenticate via external system", 
			"<a wicket:id=\"removePassword\">Remover senha</a> para forçar o usuário a autenticar via sistema externo");
		m.put("<a wicket:id=\"verifyRecoveryCode\">Verify by recovery code</a> if you can not access your TOTP authenticator", 
			"<a wicket:id=\"verifyRecoveryCode\">Verificar por código de recuperação</a> se você não puder acessar seu autenticador TOTP");
		m.put("<b class='text-danger'>NOTE: </b> This requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b> Isso requer uma assinatura empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("<b class='text-danger'>NOTE: </b> This step requires an enterprise subscription. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b> Este passo requer uma assinatura empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-danger'>NOTA: </b>A integração com SendGrid é um recurso empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("<b class='text-warning'>NOTE: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Time tracking</a> is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"<b class='text-warning'>NOTA: </b><a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>Rastreamento de tempo</a> é um recurso empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("<b>NOTE: </b> Service desk only takes effect if <a wicket:id=\"mailConnector\">mail service</a> is defined and its <tt>check incoming email</tt> option is enabled. Also <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>sub addressing</a> needs to be enabled for the system email address. Check <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>this tutorial</a> for details", 
			"<b>NOTA: </b> O service desk só tem efeito se <a wicket:id=\"mailConnector\">serviço de e-mail</a> estiver definido e sua opção <tt>verificar e-mail recebido</tt> estiver ativada. Além disso, <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>subendereçamento</a> precisa estar ativado para o endereço de e-mail do sistema. Confira <a href=\"https://medium.com/p/e56d62c27e57\" target='_blank'>este tutorial</a> para mais detalhes");
		m.put("<b>NOTE:</b> Batch editing issues will not cause state transitions of other issues even if transition rule matches", 
			"<b>NOTA:</b> A edição em lote de problemas não causará transições de estado de outros problemas, mesmo que a regra de transição corresponda");
		m.put("<b>Project Owner</b> is a built-in role with full permission over projects", "<b>Proprietário do Projeto</b> é um papel embutido com permissão total sobre os projetos");
		m.put("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>insert variable</a>. Use <tt>@@</tt> for literal <tt>@</tt>", 
			"<b>Dicas: </b> Digite <tt>@</tt> para <a href='https://docs.onedev.io/appendix/job-variables' target='_blank' tabindex='-1'>inserir variável</a>. Use <tt>@@</tt> para literal <tt>@</tt>");
		m.put("<div><span>Search Files</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Buscar Arquivos</span> <span class='font-size-sm text-muted'>no branch padrão</span></div>");
		m.put("<div><span>Search Symbols</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Buscar Símbolos</span> <span class='font-size-sm text-muted'>no branch padrão</span></div>");
		m.put("<div><span>Search Text</span> <span class='font-size-sm text-muted'>in default branch</span></div>", 
			"<div><span>Buscar Texto</span> <span class='font-size-sm text-muted'>no branch padrão</span></div>");
		m.put("<i>No Name</i>", "<i>Sem Nome</i>");
		m.put("<span class=\"keycap ml-3 mr-1\">esc</span> to close", "<span class=\"keycap ml-3 mr-1\">esc</span> para fechar");
		m.put("<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> to move", 
			"<span class=\"keycap mr-1\">&uarr;</span> <span class=\"keycap mr-1\">&darr;</span> para mover");
		m.put("<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> to navigate. <span class=\"keycap\">Esc</span> to close", 
			"<span class=\"keycap mr-1\">&uarr;</span><span class=\"keycap\">&darr;</span> para navegar. <span class=\"keycap\">Esc</span> para fechar");
		m.put("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete.", 
			"<span class='keycap'>Tab</span> ou <span class='keycap'>Enter</span> para completar.");
		m.put("<span class='keycap'>Tab</span> to complete.", "<span class='keycap'>Tab</span> para completar.");
		m.put("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>", "<span class='text-nowrap'><span class='keycap'>Enter</span> para ir</span>");
		m.put("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>", "<span class='text-nowrap'><span class='keycap'>Tab</span> para pesquisar</span>");
		m.put("<span wicket:id=\"activityCount\"></span> activities", "<span wicket:id=\"activityCount\"></span> atividades");
		m.put("<svg class='icon mr-2'><use xlink:href='%s'/></svg> Define job secrets to be used in build spec. Secrets with <b>same name</b> can be defined. For a particular name, the first authorized secret with that name will be used (search in current project first, then search in parent projects). Note that secret value containing line breaks or less than <b>%d</b> characters will not be masked in build log", 
			"<svg class='icon mr-2'><use xlink:href='%s'/></svg> Defina segredos de trabalho para serem usados na especificação de build. Segredos com <b>mesmo nome</b> podem ser definidos. Para um nome específico, o primeiro segredo autorizado com esse nome será usado (busca primeiro no projeto atual, depois nos projetos pai). Note que valores de segredos contendo quebras de linha ou menos de <b>%d</b> caracteres não serão mascarados no log de build");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here", 
			"Um <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>padrão Java</a> é esperado aqui");
		m.put("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> to validate commit message footer", 
			"Uma <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>expressão regular Java</a> para validar o rodapé da mensagem de commit");
		m.put("A child project with name \"{0}\" already exists under \"{1}\"", "Um projeto filho com o nome \"{0}\" já existe sob \"{1}\"");
		m.put("A file exists where you’re trying to create a subdirectory. Choose a new path and try again..", 
			"Um arquivo existe onde você está tentando criar um subdiretório. Escolha um novo caminho e tente novamente.");
		m.put("A path with same name already exists.Please choose a different name and try again.", 
			"Um caminho com o mesmo nome já existe. Por favor, escolha um nome diferente e tente novamente.");
		m.put("A pull request is open for this change", "Um pull request está aberto para esta alteração");
		m.put("A root project with name \"{0}\" already exists", "Um projeto raiz com o nome \"{0}\" já existe");
		m.put("A {0} used as body of address verification email", "Um {0} usado como corpo do e-mail de verificação de endereço");
		m.put("A {0} used as body of build notification email", "Um {0} usado como corpo do e-mail de notificação de build");
		m.put("A {0} used as body of commit notification email", "Um {0} usado como corpo do e-mail de notificação de commit");
		m.put("A {0} used as body of feedback email when failed to open issue via service desk", "Um {0} usado como corpo do e-mail de feedback ao falhar em abrir um problema via service desk");
		m.put("A {0} used as body of feedback email when issue is opened via service desk", "Um {0} usado como corpo do e-mail de feedback ao abrir um problema via service desk");
		m.put("A {0} used as body of feedback email when unsubscribed from issue notification", "Um {0} usado como corpo do e-mail de feedback ao cancelar inscrição na notificação de problema");
		m.put("A {0} used as body of feedback email when unsubscribed from pull request notification", 
			"Um {0} usado como corpo do e-mail de feedback ao cancelar inscrição na notificação de pull request");
		m.put("A {0} used as body of issue stopwatch overdue notification email", "Um {0} usado como corpo do e-mail de notificação de cronômetro de problema vencido");
		m.put("A {0} used as body of package notification email", "Um {0} usado como corpo do e-mail de notificação de pacote");
		m.put("A {0} used as body of password reset email", "Um {0} usado como corpo do e-mail de redefinição de senha");
		m.put("A {0} used as body of system alert email", "Um {0} usado como corpo do e-mail de alerta do sistema");
		m.put("A {0} used as body of user invitation email", "Um {0} usado como corpo do e-mail de convite de usuário");
		m.put("A {0} used as body of various issue notification emails", "Um {0} usado como corpo de vários e-mails de notificação de problema");
		m.put("A {0} used as body of various pull request notification emails", "Um {0} usado como corpo de vários e-mails de notificação de pull request");
		m.put("API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>", 
			"URL da API da sua instância JIRA cloud, por exemplo, <tt>https://your-domain.atlassian.net/rest/api/3</tt>");
		m.put("Able to merge without conflicts", "Capaz de mesclar sem conflitos");
		m.put("Absolute or relative url of the image", "URL absoluta ou relativa da imagem");
		m.put("Absolute or relative url of the link", "URL absoluta ou relativa do link");
		m.put("Access Anonymously", "Acessar Anonimamente");
		m.put("Access Build Log", "Acessar Log de Build");
		m.put("Access Build Pipeline", "Acessar Pipeline de Build");
		m.put("Access Build Reports", "Acessar Relatórios de Build");
		m.put("Access Confidential Issues", "Acessar Problemas Confidenciais");
		m.put("Access Time Tracking", "Acessar Rastreamento de Tempo");
		m.put("Access Token", "Token de Acesso");
		m.put("Access Token Authorization Bean", "Bean de Autorização de Token de Acesso");
		m.put("Access Token Edit Bean", "Bean de Edição de Token de Acesso");
		m.put("Access Token Secret", "Segredo do Token de Acesso");
		m.put("Access Token for Target Project", "Token de Acesso para Projeto Alvo");
		m.put("Access Tokens", "Tokens de Acesso");
		m.put("Access token is intended for api access and repository pull/push. It can not be used to sign in to web ui", 
			"O token de acesso é destinado ao acesso à API e ao pull/push de repositório. Ele não pode ser usado para fazer login na interface web");
		m.put("Access token is intended for api access or repository pull/push. It can not be used to sign in to web ui", 
			"O token de acesso é destinado ao acesso à API ou ao pull/push de repositório. Ele não pode ser usado para fazer login na interface web");
		m.put("Access token regenerated successfully", "Token de acesso regenerado com sucesso");
		m.put("Access token regenerated, make sure to update the token at agent side", "Token de acesso regenerado, certifique-se de atualizar o token no lado do agente");
		m.put("Account Email", "E-mail da Conta");
		m.put("Account Name", "Nome da Conta");
		m.put("Account is disabled", "Conta está desativada");
		m.put("Account set up successfully", "Conta configurada com sucesso");
		m.put("Active Directory", "Active Directory");
		m.put("Active Since", "Ativo Desde");
		m.put("Activities", "Atividades");
		m.put("Activity by type", "Atividade por tipo");
		m.put("Add", "Adicionar");
		m.put("Add Executor", "Adicionar Executor");
		m.put("Add GPG key", "Adicionar chave GPG");
		m.put("Add GPG keys here to verify commits/tags signed by this user", "Adicione chaves GPG aqui para verificar commits/tags assinados por este usuário");
		m.put("Add GPG keys here to verify commits/tags signed by you", "Adicione chaves GPG aqui para verificar commits/tags assinados por você");
		m.put("Add GPG public keys to be trusted here. Commits signed with trusted keys will be shown as verified.", 
			"Adicione chaves públicas GPG confiáveis aqui. Commits assinados com chaves confiáveis serão exibidos como verificados.");
		m.put("Add Issue...", "Adicionar Problema...");
		m.put("Add Issues to Iteration", "Adicionar Problemas à Iteração");
		m.put("Add New", "Adicionar Novo");
		m.put("Add New Board", "Adicionar Novo Quadro");
		m.put("Add New Email Address", "Adicionar Novo Endereço de E-mail");
		m.put("Add New Timesheet", "Adicionar Nova Folha de Horas");
		m.put("Add Rule", "Adicionar Regra");
		m.put("Add SSH key", "Adicionar chave SSH");
		m.put("Add SSO provider", "Adicionar provedor SSO");
		m.put("Add Spent Time", "Adicionar Tempo Gasto");
		m.put("Add Timesheet", "Adicionar Folha de Horas");
		m.put("Add Widget", "Adicionar Widget");
		m.put("Add a GPG Public Key", "Adicionar uma Chave Pública GPG");
		m.put("Add a SSH Key", "Adicionar uma Chave SSH");
		m.put("Add a package source like below", "Adicionar uma fonte de pacote como abaixo");
		m.put("Add after", "Adicionar depois");
		m.put("Add agent", "Adicionar agente");
		m.put("Add all cards to specified iteration", "Adicionar todos os cartões à iteração especificada");
		m.put("Add all commits from source branch to target branch with a merge commit", "Adicionar todos os commits da branch de origem à branch de destino com um commit de merge");
		m.put("Add assignee...", "Adicionar responsável...");
		m.put("Add before", "Adicionar antes");
		m.put("Add below to allow accessing via http protocol in new Maven versions", "Adicionar abaixo para permitir acesso via protocolo http em novas versões do Maven");
		m.put("Add child project", "Adicionar projeto filho");
		m.put("Add comment", "Adicionar comentário");
		m.put("Add comment on this selection", "Adicionar comentário nesta seleção");
		m.put("Add custom field", "Adicionar campo personalizado");
		m.put("Add dashboard", "Adicionar painel");
		m.put("Add default issue board", "Adicionar quadro de problemas padrão");
		m.put("Add files to current directory", "Adicionar arquivos ao diretório atual");
		m.put("Add files via upload", "Adicionar arquivos via upload");
		m.put("Add groovy script", "Adicionar script groovy");
		m.put("Add issue description template", "Adicionar modelo de descrição de problema");
		m.put("Add issue link", "Adicionar link de problema");
		m.put("Add issue state", "Adicionar estado do problema");
		m.put("Add issue state transition", "Adicionar transição de estado do problema");
		m.put("Add link", "Adicionar link");
		m.put("Add new", "Adicionar novo");
		m.put("Add new card to this column", "Adicionar novo cartão a esta coluna");
		m.put("Add new file", "Adicionar novo arquivo");
		m.put("Add new import", "Adicionar nova importação");
		m.put("Add new issue creation setting", "Adicionar nova configuração de criação de problema");
		m.put("Add new job dependency", "Adicionar nova dependência de trabalho");
		m.put("Add new param", "Adicionar novo parâmetro");
		m.put("Add new post-build action", "Adicionar nova ação pós-construção");
		m.put("Add new project dependency", "Adicionar nova dependência de projeto");
		m.put("Add new step", "Adicionar nova etapa");
		m.put("Add new trigger", "Adicionar novo gatilho");
		m.put("Add project", "Adicionar projeto");
		m.put("Add reviewer...", "Adicionar revisor...");
		m.put("Add to batch to commit with other suggestions later", "Adicionar ao lote para confirmar com outras sugestões mais tarde");
		m.put("Add to group...", "Adicionar ao grupo...");
		m.put("Add to iteration...", "Adicionar à iteração...");
		m.put("Add user to group...", "Adicionar usuário ao grupo...");
		m.put("Add value", "Adicionar valor");
		m.put("Add {0}", "Adicionar {0}");
		m.put("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)", "Commit adicionado \"{0}\" (<i class='text-danger'>ausente no repositório</i>)");
		m.put("Added commit \"{0}\" ({1})", "Commit adicionado \"{0}\" ({1})");
		m.put("Added to group", "Adicionado ao grupo");
		m.put("Additions", "Adições");
		m.put("Administration", "Administração");
		m.put("Administrative permission over a project", "Permissão administrativa sobre um projeto");
		m.put("Advanced Search", "Pesquisa Avançada");
		m.put("After modification", "Após modificação");
		m.put("Agent", "Agente");
		m.put("Agent Attribute", "Atributo do Agente");
		m.put("Agent Count", "Contagem de Agentes");
		m.put("Agent Edit Bean", "Editar Bean do Agente");
		m.put("Agent Selector", "Seletor de Agente");
		m.put("Agent is designed to be maintenance free. Once connected to server, it will be updated automatically upon server upgrade", 
			"O agente foi projetado para ser livre de manutenção. Uma vez conectado ao servidor, será atualizado automaticamente após a atualização do servidor");
		m.put("Agent removed", "Agente removido");
		m.put("Agent tokens are used to authorize agents. It should be configured via environment variable <tt>agentToken</tt> if agent runs as docker container, or property <tt>agentToken</tt> in file <tt>&lt;agent dir&gt;/conf/agent.properties</tt> if agent runs on bare metal/virtual machine. A token will be in-use and removed from this list if agent using it connects to server", 
			"Tokens de agente são usados para autorizar agentes. Deve ser configurado via variável de ambiente <tt>agentToken</tt> se o agente estiver sendo executado como contêiner docker, ou propriedade <tt>agentToken</tt> no arquivo <tt>&lt;agent dir&gt;/conf/agent.properties</tt> se o agente estiver sendo executado em máquina física/virtual. Um token estará em uso e será removido desta lista se o agente que o utiliza se conectar ao servidor");
		m.put("Agents", "Agentes");
		m.put("Agents can be used to execute jobs on remote machines. Once started it will update itself from server automatically when necessary", 
			"Agentes podem ser usados para executar trabalhos em máquinas remotas. Uma vez iniciado, ele se atualizará automaticamente a partir do servidor quando necessário");
		m.put("Aggregated from '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':", "Agregado de '<span wicket:id=\"estimatedTimeAggregationLink\"></span>':");
		m.put("Aggregated from '<span wicket:id=\"spentTimeAggregationLink\"></span>':", "Agregado de '<span wicket:id=\"spentTimeAggregationLink\"></span>':");
		m.put("Aggregation Link", "Link de Agregação");
		m.put("Alert", "Alerta");
		m.put("Alert Setting", "Configuração de Alerta");
		m.put("Alert Settings", "Configurações de Alerta");
		m.put("Alert settings have been updated", "As configurações de alerta foram atualizadas");
		m.put("Alerts", "Alertas");
		m.put("All", "Todos");
		m.put("All Issues", "Todos os Problemas");
		m.put("All RESTful Resources", "Todos os Recursos RESTful");
		m.put("All accessible", "Todos acessíveis");
		m.put("All builds", "Todos os builds");
		m.put("All changes", "Todas as alterações");
		m.put("All except", "Todos exceto");
		m.put("All files", "Todos os arquivos");
		m.put("All groups", "Todos os grupos");
		m.put("All issues", "Todos os problemas");
		m.put("All platforms in OCI layout", "Todas as plataformas no layout OCI");
		m.put("All platforms in image", "Todas as plataformas na imagem");
		m.put("All possible classes", "Todas as classes possíveis");
		m.put("All projects", "Todos os projetos");
		m.put("All projects with code read permission", "Todos os projetos com permissão de leitura de código");
		m.put("All pull requests", "Todos os pull requests");
		m.put("All users", "Todos os usuários");
		m.put("Allow Empty", "Permitir Vazio");
		m.put("Allow Empty Value", "Permitir Valor Vazio");
		m.put("Allow Multiple", "Permitir Múltiplos");
		m.put("Allowed Licenses", "Licenças Permitidas");
		m.put("Allowed Self Sign-Up Email Domain", "Domínio de Email Permitido para Auto Cadastro");
		m.put("Always", "Sempre");
		m.put("Always Pull Image", "Sempre Puxar Imagem");
		m.put("An issue already linked for {0}. Unlink it first", "Um problema já está vinculado a {0}. Desvincule-o primeiro");
		m.put("An unexpected exception occurred", "Ocorreu uma exceção inesperada");
		m.put("And configure auth token of the registry", "E configure o token de autenticação do registro");
		m.put("Another pull request already open for this change", "Já existe uma solicitação de pull aberta para esta alteração");
		m.put("Any agent", "Qualquer agente");
		m.put("Any branch", "Qualquer branch");
		m.put("Any commit message", "Qualquer mensagem de commit");
		m.put("Any domain", "Qualquer domínio");
		m.put("Any file", "Qualquer arquivo");
		m.put("Any issue", "Qualquer problema");
		m.put("Any job", "Qualquer trabalho");
		m.put("Any project", "Qualquer projeto");
		m.put("Any ref", "Qualquer referência");
		m.put("Any sender", "Qualquer remetente");
		m.put("Any state", "Qualquer estado");
		m.put("Any tag", "Qualquer tag");
		m.put("Any user", "Qualquer usuário");
		m.put("Api Key", "Chave da API");
		m.put("Api Token", "Token da API");
		m.put("Api Url", "URL da API");
		m.put("Append", "Anexar");
		m.put("Applicable Branches", "Branches aplicáveis");
		m.put("Applicable Builds", "Builds aplicáveis");
		m.put("Applicable Code Comments", "Comentários de código aplicáveis");
		m.put("Applicable Commit Messages", "Mensagens de Commit Aplicáveis");
		m.put("Applicable Commits", "Commits aplicáveis");
		m.put("Applicable Images", "Imagens aplicáveis");
		m.put("Applicable Issues", "Problemas aplicáveis");
		m.put("Applicable Jobs", "Trabalhos aplicáveis");
		m.put("Applicable Names", "Nomes aplicáveis");
		m.put("Applicable Projects", "Projetos aplicáveis");
		m.put("Applicable Pull Requests", "Solicitações de pull aplicáveis");
		m.put("Applicable Senders", "Remetentes aplicáveis");
		m.put("Applicable Users", "Usuários aplicáveis");
		m.put("Application (client) ID", "ID da aplicação (cliente)");
		m.put("Apply suggested change from code comment", "Aplicar alteração sugerida do comentário de código");
		m.put("Apply suggested changes from code comments", "Aplicar alterações sugeridas dos comentários de código");
		m.put("Approve", "Aprovar");
		m.put("Approved", "Aprovado");
		m.put("Approved pull request \"{0}\" ({1})", "Solicitação de pull aprovada \"{0}\" ({1})");
		m.put("Arbitrary scope", "Escopo arbitrário");
		m.put("Arbitrary type", "Tipo arbitrário");
		m.put("Arch Pull Command", "Comando de Pull Arch");
		m.put("Archived", "Arquivado");
		m.put("Arguments", "Argumentos");
		m.put("Artifacts", "Artefatos");
		m.put("Artifacts to Retrieve", "Artefatos para recuperar");
		m.put("As long as a feature can be accessed via url, you can input part of the url to match and jump", 
			"Desde que uma funcionalidade possa ser acessada via url, você pode inserir parte da url para corresponder e pular");
		m.put("Ascending", "Ascendente");
		m.put("Assignees", "Atribuídos");
		m.put("Assignees Issue Field", "Campo de problema dos atribuídos");
		m.put("Assignees are expected to merge the pull request", "Os atribuídos devem mesclar a solicitação de pull");
		m.put("Assignees have code write permission and will be responsible for merging the pull request", 
			"Os atribuídos têm permissão de escrita de código e serão responsáveis por mesclar a solicitação de pull");
		m.put("Asymmetric", "Assimétrico");
		m.put("At least one branch or tag should be selected", "Pelo menos um branch ou tag deve ser selecionado");
		m.put("At least one choice need to be specified", "Pelo menos uma escolha precisa ser especificada");
		m.put("At least one email address should be configured, please add a new one first", "Pelo menos um endereço de e-mail deve ser configurado, por favor adicione um novo primeiro");
		m.put("At least one email address should be specified", "Pelo menos um endereço de e-mail deve ser especificado");
		m.put("At least one entry should be specified", "Pelo menos uma entrada deve ser especificada");
		m.put("At least one event type needs to be selected", "Pelo menos um tipo de evento precisa ser selecionado");
		m.put("At least one field needs to be specified", "Pelo menos um campo precisa ser especificado");
		m.put("At least one project should be authorized", "Pelo menos um projeto deve ser autorizado");
		m.put("At least one project should be selected", "Pelo menos um projeto deve ser selecionado");
		m.put("At least one repository should be selected", "Pelo menos um repositório deve ser selecionado");
		m.put("At least one role is required", "Pelo menos um papel é necessário");
		m.put("At least one role must be selected", "Pelo menos um papel deve ser selecionado");
		m.put("At least one state should be specified", "Pelo menos um estado deve ser especificado");
		m.put("At least one tab should be added", "Pelo menos uma aba deve ser adicionada");
		m.put("At least one user search base should be specified", "Pelo menos uma base de pesquisa de usuário deve ser especificada");
		m.put("At least one value needs to be specified", "Pelo menos um valor precisa ser especificado");
		m.put("At least two columns need to be defined", "Pelo menos duas colunas precisam ser definidas");
		m.put("Attachment", "Anexo");
		m.put("Attributes", "Atributos");
		m.put("Attributes (can only be edited when agent is online)", "Atributos (só podem ser editados quando o agente está online)");
		m.put("Attributes saved", "Atributos salvos");
		m.put("Audit", "Auditoria");
		m.put("Audit Log", "Log de Auditoria");
		m.put("Audit Setting", "Configuração de Auditoria");
		m.put("Audit log will be preserved for the specified number of days. This setting applies to all audit events, including system level and project level", 
			"O log de auditoria será preservado pelo número especificado de dias. Esta configuração se aplica a todos os eventos de auditoria, incluindo nível de sistema e nível de projeto");
		m.put("Auth Source", "Fonte de autenticação");
		m.put("Authenticate to Bitbucket Cloud", "Autenticar no Bitbucket Cloud");
		m.put("Authenticate to GitHub", "Autenticar no GitHub");
		m.put("Authenticate to GitLab", "Autenticar no GitLab");
		m.put("Authenticate to Gitea", "Autenticar no Gitea");
		m.put("Authenticate to JIRA cloud", "Autenticar no JIRA Cloud");
		m.put("Authenticate to YouTrack", "Autenticar no YouTrack");
		m.put("Authentication", "Autenticação");
		m.put("Authentication Required", "Autenticação necessária");
		m.put("Authentication Test", "Teste de autenticação");
		m.put("Authentication Token", "Token de autenticação");
		m.put("Authenticator", "Autenticador");
		m.put("Authenticator Bean", "Bean do autenticador");
		m.put("Author", "Autor");
		m.put("Author date", "Data do autor");
		m.put("Authored By", "Autorizado por");
		m.put("Authorization", "Autorização");
		m.put("Authorizations", "Autorizações");
		m.put("Authorize user...", "Autorizar usuário...");
		m.put("Authorized Projects", "Projetos autorizados");
		m.put("Authorized Roles", "Papéis autorizados");
		m.put("Auto Merge", "Mesclagem automática");
		m.put("Auto Spec", "Especificação automática");
		m.put("Auto update check is performed by requesting an image in your browser from onedev.io indicating new version availability, with color indicating severity of the update. It works the same way as how gravatar requests avatar images. If disabled, you are highly recommended to check update manually from time to time (can be done via help menu on left bottom of the screen) to see if there are any security/critical fixes", 
			"A verificação de atualização automática é realizada solicitando uma imagem no seu navegador de onedev.io indicando a disponibilidade de nova versão, com cor indicando a gravidade da atualização. Funciona da mesma forma que como o gravatar solicita imagens de avatar. Se desativado, é altamente recomendado verificar atualizações manualmente de tempos em tempos (pode ser feito via menu de ajuda no canto inferior esquerdo da tela) para ver se há correções de segurança/críticas");
		m.put("Auto-discovered executor", "Executor descoberto automaticamente");
		m.put("Available Agent Tokens", "Tokens de agente disponíveis");
		m.put("Available Choices", "Escolhas disponíveis");
		m.put("Avatar", "Avatar");
		m.put("Avatar Service Url", "URL do serviço de avatar");
		m.put("Avatar and name", "Avatar e nome");
		m.put("Back To Home", "Voltar Para a Página Inicial");
		m.put("Backlog", "Backlog");
		m.put("Backlog Base Query", "Consulta Base do Backlog");
		m.put("Backup", "Backup");
		m.put("Backup Now", "Fazer Backup Agora");
		m.put("Backup Schedule", "Agendamento de Backup");
		m.put("Backup Setting", "Configuração de Backup");
		m.put("Backup Setting Holder", "Placeholder de Configuração de Backup");
		m.put("Backup settings updated", "Configurações de backup atualizadas");
		m.put("Bare Metal", "Bare Metal");
		m.put("Base", "Base");
		m.put("Base Gpg Key", "Chave Gpg Base");
		m.put("Base Query", "Consulta Base");
		m.put("Base64 encoded PEM format, starting with -----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			"Formato PEM codificado em Base64, começando com -----BEGIN CERTIFICATE----- e terminando com -----END CERTIFICATE-----");
		m.put("Basic Info", "Informações Básicas");
		m.put("Basic Settings", "Configurações Básicas");
		m.put("Basic settings updated", "Configurações básicas atualizadas");
		m.put("Batch Edit All Queried Issues", "Edição em Lote de Todas as Issues Consultadas");
		m.put("Batch Edit Selected Issues", "Edição em Lote das Issues Selecionadas");
		m.put("Batch Editing {0} Issues", "Editando em Lote {0} Issues");
		m.put("Batched suggestions", "Sugestões em Lote");
		m.put("Before modification", "Antes da modificação");
		m.put("Belonging Groups", "Grupos Pertencentes");
		m.put("Below are some common criterias. Type in search box above to view the complete list and available combinations.", 
			"Abaixo estão alguns critérios comuns. Digite na caixa de pesquisa acima para visualizar a lista completa e combinações disponíveis.");
		m.put("Below content is restored from an unsaved change. Clear to discard", "O conteúdo abaixo foi restaurado de uma alteração não salva. Limpar para descartar");
		m.put("Below information will also be sent", "As informações abaixo também serão enviadas");
		m.put("Binary file.", "Arquivo binário.");
		m.put("Bitbucket App Password", "Senha do App Bitbucket");
		m.put("Bitbucket Login Name", "Nome de Login do Bitbucket");
		m.put("Bitbucket Repositories to Import", "Repositórios do Bitbucket para Importar");
		m.put("Bitbucket Workspace", "Workspace do Bitbucket");
		m.put("Bitbucket app password should be generated with permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>", 
			"A senha do app Bitbucket deve ser gerada com permissão <b>account/read</b>, <b>repositories/read</b> e <b>issues:read</b>");
		m.put("Blame", "Responsabilizar");
		m.put("Blob", "Blob");
		m.put("Blob hash", "Hash do Blob");
		m.put("Blob index version", "Versão do Índice do Blob");
		m.put("Blob name", "Nome do Blob");
		m.put("Blob path", "Caminho do Blob");
		m.put("Blob primary symbols", "Símbolos Primários do Blob");
		m.put("Blob secondary symbols", "Símbolos Secundários do Blob");
		m.put("Blob symbol list", "Lista de Símbolos do Blob");
		m.put("Blob text", "Texto do Blob");
		m.put("Blob unknown", "Blob desconhecido");
		m.put("Blob upload invalid", "Upload de Blob inválido");
		m.put("Blob upload unknown", "Upload de Blob desconhecido");
		m.put("Board", "Quadro");
		m.put("Board Columns", "Colunas do Quadro");
		m.put("Board Spec", "Especificação do Quadro");
		m.put("Boards", "Quadros");
		m.put("Body", "Corpo");
		m.put("Bold", "Negrito");
		m.put("Both", "Ambos");
		m.put("Bottom", "Inferior");
		m.put("Branch", "Ramo");
		m.put("Branch \"{0}\" already exists, please choose a different name", "O ramo \"{0}\" já existe, por favor escolha um nome diferente");
		m.put("Branch \"{0}\" created", "Ramo \"{0}\" criado");
		m.put("Branch \"{0}\" deleted", "Ramo \"{0}\" excluído");
		m.put("Branch <a wicket:id=\"targetBranch\"></a> is up to date with all commits from <a wicket:id=\"sourceBranch\"></a>. Try <a wicket:id=\"swapBranches\">swap source and target</a> for the comparison.", 
			"O ramo <a wicket:id=\"targetBranch\"></a> está atualizado com todos os commits do <a wicket:id=\"sourceBranch\"></a>. Tente <a wicket:id=\"swapBranches\">trocar origem e destino</a> para a comparação.");
		m.put("Branch Choice Bean", "Bean de Escolha de Ramo");
		m.put("Branch Name", "Nome do Ramo");
		m.put("Branch Protection", "Proteção de Ramo");
		m.put("Branch Revision", "Revisão do Ramo");
		m.put("Branch update", "Atualização de Ramo");
		m.put("Branches", "Ramos");
		m.put("Brand Setting Edit Bean", "Bean de Edição de Configuração de Marca");
		m.put("Branding", "Branding");
		m.put("Branding settings updated", "Configurações de branding atualizadas");
		m.put("Browse Code", "Navegar Código");
		m.put("Browse code", "Navegar código");
		m.put("Bug Report", "Relatório de Bug");
		m.put("Build", "Build");
		m.put("Build #{0} already finished", "Build #{0} já finalizado");
		m.put("Build #{0} deleted", "Build #{0} excluído");
		m.put("Build #{0} not finished yet", "Build #{0} ainda não finalizado");
		m.put("Build Artifact Storage", "Armazenamento de Artefatos de Build");
		m.put("Build Commit", "Commit de Build");
		m.put("Build Context", "Contexto de Build");
		m.put("Build Description", "Descrição de Build");
		m.put("Build Filter", "Filtro de Build");
		m.put("Build Image", "Imagem de Build");
		m.put("Build Image (Kaniko)", "Imagem de Build (Kaniko)");
		m.put("Build Management", "Gerenciamento de Build");
		m.put("Build Notification", "Notificação de Build");
		m.put("Build Notification Template", "Template de Notificação de Build");
		m.put("Build Number", "Número de Build");
		m.put("Build On Behalf Of", "Build em Nome de");
		m.put("Build Path", "Caminho de Build");
		m.put("Build Preservation", "Preservação de Build");
		m.put("Build Preservations", "Preservações de Build");
		m.put("Build Preservations Bean", "Bean de Preservações de Build");
		m.put("Build Preserve Rules", "Regras de Preservação de Build");
		m.put("Build Provider", "Provedor de Build");
		m.put("Build Spec", "Especificação de Build");
		m.put("Build Statistics", "Estatísticas de Build");
		m.put("Build Version", "Versão de Build");
		m.put("Build Volume Storage Class", "Classe de Armazenamento de Volume de Build");
		m.put("Build Volume Storage Size", "Tamanho de Armazenamento de Volume de Build");
		m.put("Build administrative permission for all jobs inside a project, including batch operations over multiple builds", 
			"Permissão administrativa de build para todos os jobs dentro de um projeto, incluindo operações em lote sobre múltiplos builds");
		m.put("Build docker image with docker buildx. This step can only be executed by server docker executor or remote docker executor, and it uses the buildx builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko step instead", 
			"Build de imagem docker com docker buildx. Esta etapa só pode ser executada pelo executor docker do servidor ou executor docker remoto, e utiliza o builder buildx especificado nesses executores para realizar o trabalho. Para build de imagem com executor Kubernetes, use a etapa kaniko");
		m.put("Build docker image with kaniko. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Build de imagem docker com kaniko. Esta etapa precisa ser executada pelo executor docker do servidor, executor docker remoto ou executor Kubernetes");
		m.put("Build duration statistics", "Estatísticas de duração de build");
		m.put("Build frequency statistics", "Estatísticas de frequência de builds");
		m.put("Build is successful", "Build foi bem-sucedido");
		m.put("Build list", "Lista de builds");
		m.put("Build not exist or access denied", "Build não existe ou acesso negado");
		m.put("Build number", "Número do build");
		m.put("Build preserve rules saved", "Regras de preservação de builds salvas");
		m.put("Build required for deletion. Submit pull request instead", "Build necessário para exclusão. Envie uma pull request em vez disso");
		m.put("Build required for this change. Please submit pull request instead", "Compilação necessária para esta alteração. Por favor, envie um pull request em vez disso.");
		m.put("Build required for this change. Submit pull request instead", "Build necessário para esta alteração. Envie uma pull request em vez disso");
		m.put("Build spec not defined", "Especificação de build não definida");
		m.put("Build spec not defined (import project: {0}, import revision: {1})", "Especificação de build não definida (importar projeto: {0}, importar revisão: {1})");
		m.put("Build spec not found in commit of this build", "Especificação de build não encontrada no commit deste build");
		m.put("Build statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Estatísticas de build são um recurso empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("Build version", "Versão do build");
		m.put("Build with Persistent Volume", "Build com Volume Persistente");
		m.put("Builds", "Builds");
		m.put("Builds are {0}", "Builds estão {0}");
		m.put("Buildx Builder", "Buildx Builder");
		m.put("Built In Fields Bean", "Campos Integrados Bean");
		m.put("Burndown", "Burndown");
		m.put("Burndown chart", "Gráfico de Burndown");
		m.put("Button Image Url", "URL da Imagem do Botão");
		m.put("By Group", "Por Grupo");
		m.put("By User", "Por Usuário");
		m.put("By day", "Por dia");
		m.put("By default code is cloned via an auto-generated credential, which only has read permission over current project. In case the job needs to <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>push code to server</a>, you should supply custom credential with appropriate permissions here", 
			"Por padrão, o código é clonado via uma credencial gerada automaticamente, que possui apenas permissão de leitura sobre o projeto atual. Caso o trabalho precise <a href='https://docs.onedev.io/tutorials/cicd/commit-and-push' target='_blank'>enviar código para o servidor</a>, você deve fornecer uma credencial personalizada com permissões apropriadas aqui");
		m.put("By default issues of parent and child projects will also be listed. Use query <code>&quot;Project&quot; is current</code> to show issues only belong to this project", 
			"Por padrão, os problemas dos projetos pai e filho também serão listados. Use a consulta <code>&quot;Project&quot; is current</code> para mostrar apenas os problemas pertencentes a este projeto");
		m.put("By month", "Por mês");
		m.put("By week", "Por semana");
		m.put("Bypass Certificate Check", "Ignorar Verificação de Certificado");
		m.put("CANCELLED", "CANCELADO");
		m.put("CORS Allowed Origins", "Origens Permitidas pelo CORS");
		m.put("CPD Report", "Relatório CPD");
		m.put("CPU", "CPU");
		m.put("CPU Intensive Task Concurrency", "Concorrência de Tarefa Intensiva de CPU");
		m.put("CPU capability in millis. This is normally (CPU cores)*1000", "Capacidade de CPU em millis. Normalmente é (núcleos de CPU)*1000");
		m.put("Cache Key", "Chave de Cache");
		m.put("Cache Management", "Gerenciamento de Cache");
		m.put("Cache Paths", "Caminhos de Cache");
		m.put("Cache Setting Bean", "Configuração de Cache Bean");
		m.put("Cache will be deleted to save space if not accessed for this number of days", "O cache será excluído para economizar espaço se não for acessado por este número de dias");
		m.put("Calculating merge preview...", "Calculando pré-visualização de merge...");
		m.put("Callback URL", "URL de Callback");
		m.put("Can Be Used By Jobs", "Pode Ser Usado Por Trabalhos");
		m.put("Can Create Root Projects", "Pode Criar Projetos Raiz");
		m.put("Can Edit Estimated Time", "Pode Editar Tempo Estimado");
		m.put("Can not convert root user to service account", "Não é possível converter o usuário root para conta de serviço");
		m.put("Can not convert yourself to service account", "Não é possível converter você mesmo para conta de serviço");
		m.put("Can not delete default branch", "Não é possível excluir a branch padrão");
		m.put("Can not delete root account", "Não é possível excluir a conta raiz");
		m.put("Can not delete yourself", "Não é possível excluir a si mesmo");
		m.put("Can not disable root account", "Não é possível desativar a conta raiz");
		m.put("Can not disable yourself", "Não é possível desativar a si mesmo");
		m.put("Can not find issue board: ", "Não é possível encontrar o quadro de problemas:");
		m.put("Can not move project \"{0}\" to be under itself or its descendants", "Não é possível mover o projeto \"{0}\" para estar sob ele mesmo ou seus descendentes");
		m.put("Can not perform this operation now", "Não é possível realizar esta operação agora");
		m.put("Can not reset password for service account or disabled user", "Não é possível redefinir a senha para conta de serviço ou usuário desativado");
		m.put("Can not reset password for user authenticating via external system", "Não é possível redefinir a senha para usuário autenticando via sistema externo");
		m.put("Can not save malformed query", "Não é possível salvar consulta malformada");
		m.put("Can not use current or descendant project as parent", "Não é possível usar o projeto atual ou descendente como pai");
		m.put("Can only compare with common ancestor when different projects are involved", "Só é possível comparar com o ancestral comum quando projetos diferentes estão envolvidos");
		m.put("Cancel", "Cancelar");
		m.put("Cancel All Queried Builds", "Cancelar Todos os Builds Consultados");
		m.put("Cancel Selected Builds", "Cancelar Builds Selecionados");
		m.put("Cancel invitation", "Cancelar convite");
		m.put("Cancel request submitted", "Solicitação de cancelamento enviada");
		m.put("Cancel this build", "Cancelar este build");
		m.put("Cancelled", "Cancelado");
		m.put("Cancelled By", "Cancelado Por");
		m.put("Case Sensitive", "Sensível a Maiúsculas e Minúsculas");
		m.put("Certificates to Trust", "Certificados para Confiar");
		m.put("Change", "Alterar");
		m.put("Change Detection Excludes", "Exclusões de Detecção de Alteração");
		m.put("Change My Password", "Alterar Minha Senha");
		m.put("Change To", "Alterar Para");
		m.put("Change already merged", "Alteração já mesclada");
		m.put("Change not updated yet", "Alteração ainda não atualizada");
		m.put("Change property <code>serverUrl</code> in file <code>conf/agent.properties</code> if necessary. The default value is taken from OneDev server url specified in <i>Administration / System Setting</i>", 
			"Altere a propriedade <code>serverUrl</code> no arquivo <code>conf/agent.properties</code> se necessário. O valor padrão é obtido da URL do servidor OneDev especificada em <i>Administração / Configuração do Sistema</i>");
		m.put("Change to another field", "Alterar para outro campo");
		m.put("Change to another state", "Alterar para outro estado");
		m.put("Change to another value", "Alterar para outro valor");
		m.put("Changes since last review", "Alterações desde a última revisão");
		m.put("Changes since last visit", "Alterações desde a última visita");
		m.put("Changes since this action", "Alterações desde esta ação");
		m.put("Changes since this comment", "Alterações desde este comentário");
		m.put("Channel Notification", "Notificação de Canal");
		m.put("Chart Metadata", "Metadados do Gráfico");
		m.put("Check <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">GitHub's guide</a> on how to generate and use GPG keys to sign your commits", 
			"Confira o <a href=\"https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification\" target=\"_blank\">guia do GitHub</a> sobre como gerar e usar chaves GPG para assinar seus commits");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including instructions on how to run agent as service", 
			"Confira <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">gerenciamento de agentes</a> para detalhes, incluindo instruções sobre como executar o agente como serviço");
		m.put("Check <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">agent management</a> for details, including list of supported environment variables", 
			"Confira <a href=\"https://docs.onedev.io/administration-guide/agent-management\" target=\"_blank\">gerenciamento de agentes</a> para detalhes, incluindo lista de variáveis de ambiente suportadas");
		m.put("Check Commit Message Footer", "Verificar Rodapé da Mensagem de Commit");
		m.put("Check Incoming Email", "Verificar Email de Entrada");
		m.put("Check Issue Integrity", "Verificar Integridade de Problemas");
		m.put("Check Update", "Verificar Atualização");
		m.put("Check Workflow Integrity", "Verificar Integridade do Workflow");
		m.put("Check out to local workspace", "Fazer checkout para espaço de trabalho local");
		m.put("Check this to compare right side with common ancestor of left and right", "Marque isto para comparar o lado direito com o ancestral comum do lado esquerdo e direito");
		m.put("Check this to enforce two-factor authentication for all users in the system", "Marque isto para impor autenticação de dois fatores para todos os usuários no sistema");
		m.put("Check this to enforce two-factor authentication for all users in this group", "Marque isto para impor autenticação de dois fatores para todos os usuários deste grupo");
		m.put("Check this to prevent branch creation", "Marque isto para impedir a criação de branches");
		m.put("Check this to prevent branch deletion", "Marque isto para impedir a exclusão de branches");
		m.put("Check this to prevent forced push", "Marque isto para impedir push forçado");
		m.put("Check this to prevent tag creation", "Marque isto para impedir a criação de tags");
		m.put("Check this to prevent tag deletion", "Marque isto para impedir a exclusão de tags");
		m.put("Check this to prevent tag update", "Marque isto para impedir a atualização de tags");
		m.put("Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>. Note this is applicable for non-merge commits", 
			"Marque isto para exigir <a href='https://www.conventionalcommits.org' target='_blank'>commits convencionais</a>. Observe que isso é aplicável para commits que não sejam de merge");
		m.put("Check this to require valid signature of head commit", "Marque isto para exigir assinatura válida do commit principal");
		m.put("Check this to retrieve Git LFS files", "Marque isto para recuperar arquivos Git LFS");
		m.put("Checkbox", "Caixa de seleção");
		m.put("Checking field values...", "Verificando valores dos campos...");
		m.put("Checking fields...", "Verificando campos...");
		m.put("Checking state and field ordinals...", "Verificando estado e ordens dos campos...");
		m.put("Checking state...", "Verificando estado...");
		m.put("Checkout Code", "Fazer checkout do código");
		m.put("Checkout Path", "Caminho de checkout");
		m.put("Checkout Pull Request Head", "Fazer checkout da cabeça do pull request");
		m.put("Checkout Pull Request Merge Preview", "Fazer checkout da prévia de merge do pull request");
		m.put("Checkstyle Report", "Relatório do Checkstyle");
		m.put("Cherry-Pick", "Cherry-Pick");
		m.put("Cherry-picked successfully", "Cherry-pick realizado com sucesso");
		m.put("Child Projects", "Projetos filhos");
		m.put("Child Projects Of", "Projetos filhos de");
		m.put("Choice Provider", "Provedor de escolha");
		m.put("Choose", "Escolher");
		m.put("Choose JIRA project to import issues from", "Escolha o projeto JIRA para importar problemas");
		m.put("Choose Revision", "Escolher revisão");
		m.put("Choose YouTrack project to import issues from", "Escolha o projeto YouTrack para importar problemas");
		m.put("Choose a project...", "Escolha um projeto...");
		m.put("Choose a user...", "Escolha um usuário...");
		m.put("Choose branch...", "Escolha um branch...");
		m.put("Choose branches...", "Escolha branches...");
		m.put("Choose build...", "Escolha uma build...");
		m.put("Choose file", "Escolha um arquivo");
		m.put("Choose group...", "Escolha um grupo...");
		m.put("Choose groups...", "Escolha grupos...");
		m.put("Choose issue...", "Escolha um problema...");
		m.put("Choose issues...", "Escolher problemas...");
		m.put("Choose iteration...", "Escolha uma iteração...");
		m.put("Choose iterations...", "Escolha iterações...");
		m.put("Choose job...", "Escolha um trabalho...");
		m.put("Choose jobs...", "Escolha trabalhos...");
		m.put("Choose project", "Escolha um projeto");
		m.put("Choose projects...", "Escolha projetos...");
		m.put("Choose pull request...", "Escolha um pull request...");
		m.put("Choose repository", "Escolha um repositório");
		m.put("Choose role...", "Escolha um papel...");
		m.put("Choose roles...", "Escolha papéis...");
		m.put("Choose users...", "Escolha usuários...");
		m.put("Choose...", "Escolha...");
		m.put("Circular build spec imports ({0})", "Importações circulares de especificação de build ({0})");
		m.put("Click to select a commit, or shift-click to select multiple commit", "Clique para selecionar um commit, ou shift-clique para selecionar múltiplos commits");
		m.put("Click to show comment of marked text", "Clique para mostrar o comentário do texto marcado");
		m.put("Click to show issue details", "Clique para mostrar detalhes do problema");
		m.put("Client ID of this OneDev instance registered in Google cloud", "ID do cliente desta instância do OneDev registrada na nuvem do Google");
		m.put("Client Id", "ID do cliente");
		m.put("Client Secret", "Segredo do cliente");
		m.put("Client secret of this OneDev instance registered in Google cloud", "Segredo do cliente desta instância do OneDev registrada na nuvem do Google");
		m.put("Clippy Report", "Relatório do Clippy");
		m.put("Clone", "Clonar");
		m.put("Clone Credential", "Credencial de clonagem");
		m.put("Clone Depth", "Profundidade de clonagem");
		m.put("Clone in IntelliJ", "Clonar no IntelliJ");
		m.put("Clone in VSCode", "Clonar no VSCode");
		m.put("Close", "Fechar");
		m.put("Close Iteration", "Fechar iteração");
		m.put("Close this iteration", "Fechar esta iteração");
		m.put("Closed", "Fechado");
		m.put("Closed Issue State", "Estado de problema fechado");
		m.put("Closest due date", "Data de vencimento mais próxima");
		m.put("Clover Coverage Report", "Relatório de cobertura do Clover");
		m.put("Cluster Role", "Papel do cluster");
		m.put("Cluster Setting", "Configuração do cluster");
		m.put("Cluster setting", "Configuração do cluster");
		m.put("Clustered Servers", "Servidores agrupados");
		m.put("Cobertura Coverage Report", "Relatório de cobertura do Cobertura");
		m.put("Code", "Código");
		m.put("Code Analysis", "Análise de código");
		m.put("Code Analysis Setting", "Configuração de análise de código");
		m.put("Code Analysis Settings", "Configurações de análise de código");
		m.put("Code Changes", "Alterações de código");
		m.put("Code Comment", "Comentário de código");
		m.put("Code Comment Management", "Gerenciamento de comentários de código");
		m.put("Code Comments", "Comentários de código");
		m.put("Code Compare", "Comparação de código");
		m.put("Code Contribution Statistics", "Estatísticas de contribuição de código");
		m.put("Code Coverage", "Cobertura de código");
		m.put("Code Line Statistics", "Estatísticas de linhas de código");
		m.put("Code Management", "Gerenciamento de código");
		m.put("Code Privilege", "Privilégio de código");
		m.put("Code Problem Statistics", "Estatísticas de problemas de código");
		m.put("Code Search", "Busca de código");
		m.put("Code Statistics", "Estatísticas de código");
		m.put("Code analysis settings updated", "Configurações de análise de código atualizadas");
		m.put("Code changes since...", "Alterações de código desde...");
		m.put("Code clone or download", "Clonar ou baixar código");
		m.put("Code comment", "Comentário de código");
		m.put("Code comment #{0} deleted", "Comentário de código #{0} excluído");
		m.put("Code comment administrative permission inside a project, including batch operations over multiple code comments", 
			"Permissão administrativa de comentário de código dentro de um projeto, incluindo operações em lote sobre múltiplos comentários de código");
		m.put("Code commit", "Commit de código");
		m.put("Code is committed", "Código foi commitado");
		m.put("Code push", "Push de código");
		m.put("Code read permission is required to import build spec (import project: {0}, import revision: {1})", 
			"Permissão de leitura de código é necessária para importar especificação de build (projeto de importação: {0}, revisão de importação: {1})");
		m.put("Code suggestion", "Sugestão de código");
		m.put("Code write permission is required for this operation", "Permissão de escrita de código é necessária para esta operação");
		m.put("Collapse all", "Colapsar tudo");
		m.put("Color", "Cor");
		m.put("Columns", "Colunas");
		m.put("Command Palette", "Paleta de Comandos");
		m.put("Commands", "Comandos");
		m.put("Comment", "Comentário");
		m.put("Comment Content", "Conteúdo do Comentário");
		m.put("Comment on File", "Comentar no Arquivo");
		m.put("Comment too long", "Comentário muito longo");
		m.put("Commented code is outdated", "Código comentado está desatualizado");
		m.put("Commented on file \"{0}\" in project \"{1}\"", "Comentado no arquivo \"{0}\" no projeto \"{1}\"");
		m.put("Commented on issue \"{0}\" ({1})", "Comentado na issue \"{0}\" ({1})");
		m.put("Commented on pull request \"{0}\" ({1})", "Comentado no pull request \"{0}\" ({1})");
		m.put("Comments", "Comentários");
		m.put("Commit", "Commit");
		m.put("Commit &amp; Insert", "Commit &amp; Inserir");
		m.put("Commit Batched Suggestions", "Commit de Sugestões em Lote");
		m.put("Commit Message", "Mensagem de Commit");
		m.put("Commit Message Bean", "Bean de Mensagem de Commit");
		m.put("Commit Message Fix Patterns", "Padrões de Correção de Mensagem de Commit");
		m.put("Commit Message Footer Pattern", "Padrão de Rodapé de Mensagem de Commit");
		m.put("Commit Notification", "Notificação de Commit");
		m.put("Commit Notification Template", "Template de Notificação de Commit");
		m.put("Commit Scopes", "Escopos de Commit");
		m.put("Commit Signature Required", "Assinatura de Commit Necessária");
		m.put("Commit Suggestion", "Sugestão de Commit");
		m.put("Commit Types", "Tipos de Commit");
		m.put("Commit Types For Footer Check", "Tipos de Commit para Verificação de Rodapé");
		m.put("Commit Your Change", "Commit Sua Alteração");
		m.put("Commit date", "Data do Commit");
		m.put("Commit hash", "Hash do Commit");
		m.put("Commit history of current path", "Histórico de Commit do caminho atual");
		m.put("Commit index version", "Versão do índice de Commit");
		m.put("Commit message can be used to fix issues by prefixing and suffixing issue number with specified pattern. Each line of the commit message will be matched against each entry defined here to find issues to be fixed", 
			"A mensagem de commit pode ser usada para corrigir problemas prefixando e sufixando o número do problema com o padrão especificado. Cada linha da mensagem de commit será comparada com cada entrada definida aqui para encontrar problemas a serem corrigidos");
		m.put("Commit not exist or access denied", "Commit não existe ou acesso negado");
		m.put("Commit of the build is missing", "Commit da build está ausente");
		m.put("Commit signature required but no GPG signing key specified", "Assinatura de commit necessária, mas nenhuma chave de assinatura GPG especificada");
		m.put("Commit suggestion", "Sugestão de commit");
		m.put("Commits", "Commits");
		m.put("Commits are taken from default branch of non-forked repositories", "Commits são retirados do branch padrão de repositórios não-forkados");
		m.put("Commits generated by OneDev previously will be shown as unverified if this key is deleted. Type <code>yes</code> below if you want to continue.", 
			"Commits gerados anteriormente pelo OneDev serão exibidos como não verificados se esta chave for excluída. Digite <code>yes</code> abaixo se desejar continuar.");
		m.put("Commits were merged into target branch", "Commits foram mesclados no branch de destino");
		m.put("Commits were merged into target branch outside of this pull request", "Commits foram mesclados no branch de destino fora deste pull request");
		m.put("Commits were rebased onto target branch", "Commits foram rebaseados no branch de destino");
		m.put("Commits were squashed into a single commit on target branch", "Commits foram compactados em um único commit no branch de destino");
		m.put("Committed After", "Commitado Após");
		m.put("Committed Before", "Commitado Antes");
		m.put("Committed By", "Commitado Por");
		m.put("Committer", "Committer");
		m.put("Compare", "Comparar");
		m.put("Compare with base revision", "Comparar com revisão base");
		m.put("Compare with this parent", "Comparar com este pai");
		m.put("Concurrency", "Concorrência");
		m.put("Condition", "Condição");
		m.put("Confidential", "Confidencial");
		m.put("Config File", "Arquivo de Configuração");
		m.put("Configuration Discovery Url", "Url de Descoberta de Configuração");
		m.put("Configure your scope to use below registry", "Configure seu escopo para usar o registro abaixo");
		m.put("Confirm Approve", "Confirmar Aprovação");
		m.put("Confirm Delete Source Branch", "Confirmar Exclusão do Branch de Origem");
		m.put("Confirm Discard", "Confirmar Descartar");
		m.put("Confirm Reopen", "Confirmar Reabrir");
		m.put("Confirm Request For Changes", "Confirmar Solicitação de Alterações");
		m.put("Confirm Restore Source Branch", "Confirmar Restauração do Branch de Origem");
		m.put("Confirm password here", "Confirme a senha aqui");
		m.put("Confirm your action", "Confirme sua ação");
		m.put("Connect New Agent", "Conectar Novo Agente");
		m.put("Connect with your SSO account", "Conecte-se com sua conta SSO");
		m.put("Contact Email", "Email de Contato");
		m.put("Contact Name", "Nome de Contato");
		m.put("Container Image", "Imagem do Container");
		m.put("Container Image(s)", "Imagem(s) do Container");
		m.put("Container default", "Container padrão");
		m.put("Content", "Conteúdo");
		m.put("Content Type", "Tipo de Conteúdo");
		m.put("Content is identical", "Conteúdo é idêntico");
		m.put("Continue to add other user after create", "Continuar adicionando outro usuário após criar");
		m.put("Contributed settings", "Configurações Contribuídas");
		m.put("Contributions", "Contribuições");
		m.put("Contributions to {0} branch, excluding merge commits", "Contribuições para o branch {0}, excluindo commits de merge");
		m.put("Convert All Queried to Service Accounts", "Converter Todos Consultados para Contas de Serviço");
		m.put("Convert Selected to Service Accounts", "Converter Selecionados para Contas de Serviço");
		m.put("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm", 
			"Converter para contas de serviço removerá senha, endereços de email, todas as atribuições e observações. Digite <code>yes</code> para confirmar");
		m.put("Copy", "Copiar");
		m.put("Copy All Queried Issues To...", "Copiar Todas as Issues Consultadas Para...");
		m.put("Copy Files with SCP", "Copiar Arquivos com SCP");
		m.put("Copy Selected Issues To...", "Copiar Issues Selecionadas Para...");
		m.put("Copy dashboard", "Copiar dashboard");
		m.put("Copy issue number and title", "Copiar número e título da issue");
		m.put("Copy public key", "Copiar chave pública");
		m.put("Copy selected text to clipboard", "Copiar texto selecionado para a área de transferência");
		m.put("Copy to clipboard", "Copiar para a área de transferência");
		m.put("Count", "Contar");
		m.put("Coverage Statistics", "Estatísticas de Cobertura");
		m.put("Covered", "Coberto");
		m.put("Covered by tests", "Coberto por testes");
		m.put("Cppcheck Report", "Relatório do Cppcheck");
		m.put("Cpu Limit", "Limite de CPU");
		m.put("Cpu Request", "Requisição de CPU");
		m.put("Create", "Criar");
		m.put("Create Administrator Account", "Criar Conta de Administrador");
		m.put("Create Branch", "Criar Branch");
		m.put("Create Branch Bean", "Bean de Criação de Branch");
		m.put("Create Branch Bean With Revision", "Bean de Criação de Branch com Revisão");
		m.put("Create Child Project", "Criar Projeto Filho");
		m.put("Create Child Projects", "Criar Projetos Filhos");
		m.put("Create Issue", "Criar Problema");
		m.put("Create Iteration", "Criar Iteração");
		m.put("Create Merge Commit", "Criar Commit de Mesclagem");
		m.put("Create Merge Commit If Necessary", "Criar Commit de Mesclagem se Necessário");
		m.put("Create New", "Criar Novo");
		m.put("Create New File", "Criar Novo Arquivo");
		m.put("Create New User", "Criar Novo Usuário");
		m.put("Create Project", "Criar Projeto");
		m.put("Create Pull Request", "Criar Solicitação de Pull");
		m.put("Create Pull Request for This Change", "Criar Solicitação de Pull para Esta Alteração");
		m.put("Create Tag", "Criar Tag");
		m.put("Create Tag Bean", "Criar Bean de Tag");
		m.put("Create Tag Bean With Revision", "Criar Bean de Tag com Revisão");
		m.put("Create User", "Criar Usuário");
		m.put("Create body", "Criar corpo");
		m.put("Create branch <b>{0}</b> from {1}", "Criar branch <b>{0}</b> de {1}");
		m.put("Create child projects under a project", "Criar projetos filhos dentro de um projeto");
		m.put("Create issue", "Criar problema");
		m.put("Create merge commit", "Criar commit de mesclagem");
		m.put("Create merge commit if necessary", "Criar commit de mesclagem se necessário");
		m.put("Create new issue", "Criar novo problema");
		m.put("Create tag", "Criar tag");
		m.put("Create tag <b>{0}</b> from {1}", "Criar tag <b>{0}</b> de {1}");
		m.put("Created At", "Criado Em");
		m.put("Creation of this branch is prohibited per branch protection rule", "A criação desta branch é proibida pela regra de proteção de branch");
		m.put("Critical", "Crítico");
		m.put("Critical Severity", "Severidade Crítica");
		m.put("Cron Expression", "Expressão Cron");
		m.put("Cron schedule", "Agenda Cron");
		m.put("Curl Location", "Localização Curl");
		m.put("Current Iteration", "Iteração Atual");
		m.put("Current Value", "Valor Atual");
		m.put("Current avatar", "Avatar atual");
		m.put("Current context is different from the context when this comment is added, click to show the comment context", 
			"O contexto atual é diferente do contexto quando este comentário foi adicionado, clique para mostrar o contexto do comentário");
		m.put("Current context is different from the context when this reply is added, click to show the reply context", 
			"O contexto atual é diferente do contexto quando esta resposta foi adicionada, clique para mostrar o contexto da resposta");
		m.put("Current context is different from this action, click to show the comment context", "O contexto atual é diferente desta ação, clique para mostrar o contexto do comentário");
		m.put("Current platform", "Plataforma Atual");
		m.put("Current project", "Projeto Atual");
		m.put("Custom Linux Shell", "Shell Linux Personalizado");
		m.put("DISCARDED", "DESCARTADO");
		m.put("Dashboard Share Bean", "Bean de Compartilhamento de Painel");
		m.put("Dashboard name", "Nome do Painel");
		m.put("Dashboards", "Painéis");
		m.put("Database Backup", "Backup de Banco de Dados");
		m.put("Date", "Data");
		m.put("Date Time", "Data e Hora");
		m.put("Days Per Week", "Dias Por Semana");
		m.put("Deactivate Subscription", "Desativar Assinatura");
		m.put("Deactivate Trial Subscription", "Desativar Assinatura de Teste");
		m.put("Default", "Padrão");
		m.put("Default (Shell on Linux, Batch on Windows)", "Padrão (Shell no Linux, Batch no Windows)");
		m.put("Default Assignees", "Atribuídos Padrão");
		m.put("Default Boards", "Quadros Padrão");
		m.put("Default Fixed Issue Filter", "Filtro de Problemas Resolvidos Padrão");
		m.put("Default Fixed Issue Filters", "Filtros de Problemas Resolvidos Padrão");
		m.put("Default Fixed Issue Filters Bean", "Bean de Filtros de Problemas Resolvidos Padrão");
		m.put("Default Group", "Grupo Padrão");
		m.put("Default Issue Boards", "Quadros de Problemas Padrão");
		m.put("Default Merge Strategy", "Estratégia de Mesclagem Padrão");
		m.put("Default Multi Value Provider", "Provedor de Valor Múltiplo Padrão");
		m.put("Default Project", "Projeto Padrão");
		m.put("Default Project Setting", "Configuração de Projeto Padrão");
		m.put("Default Roles", "Funções Padrão");
		m.put("Default Roles Bean", "Bean de Funções Padrão");
		m.put("Default Value", "Valor Padrão");
		m.put("Default Value Provider", "Provedor de Valor Padrão");
		m.put("Default Values", "Valores Padrão");
		m.put("Default branch", "Branch Padrão");
		m.put("Default branding settings restored", "Configurações de marca padrão restauradas");
		m.put("Default fixed issue filters saved", "Filtros de problemas resolvidos padrão salvos");
		m.put("Default merge strategy", "Estratégia de mesclagem padrão");
		m.put("Default roles affect default permissions granted to everyone in the system. The actual default permissions will be <b class='text-warning'>all permissions</b> contained in default roles of this project and all its parent projects", 
			"Funções padrão afetam permissões padrão concedidas a todos no sistema. As permissões padrão reais serão <b class='text-warning'>todas as permissões</b> contidas nas funções padrão deste projeto e de todos os seus projetos pai");
		m.put("Define all custom issue fields here. Each project can decide to use all or a subset of these fields via its issue transition setting. <b class=\"text-warning\">NOTE: </b> Newly defined fields by default only appear in new issues. Batch edit existing issues from issue list page if you want them to have these new fields", 
			"Defina todos os campos personalizados de problemas aqui. Cada projeto pode decidir usar todos ou um subconjunto desses campos por meio de sua configuração de transição de problemas. <b class=\"text-warning\">NOTA: </b> Campos recém-definidos por padrão só aparecem em novos problemas. Edite em lote problemas existentes na página de lista de problemas se desejar que eles tenham esses novos campos");
		m.put("Define all custom issue states here. The first state will be used as initial state of created issues", 
			"Defina todos os estados personalizados de problemas aqui. O primeiro estado será usado como estado inicial dos problemas criados");
		m.put("Define branch protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given branch and user, the first matching rule will take effect", 
			"Defina regras de proteção de branch. Regras definidas no projeto pai são consideradas definidas após as regras definidas aqui. Para uma determinada branch e usuário, a primeira regra correspondente terá efeito");
		m.put("Define default issue boards for all projects here. A certain project can override this setting to define its own issue boards.", 
			"Defina quadros de problemas padrão para todos os projetos aqui. Um determinado projeto pode substituir essa configuração para definir seus próprios quadros de problemas.");
		m.put("Define how issue states should be transited from one to another, either manually or automatically when some events happen. And the rule can be configured to apply to certain projects and issues via the applicable issues setting", 
			"Defina como os estados de problemas devem ser transitados de um para outro, manualmente ou automaticamente quando alguns eventos ocorrerem. E a regra pode ser configurada para se aplicar a determinados projetos e problemas por meio da configuração de problemas aplicáveis");
		m.put("Define issue templates here. When a new issue is created, the first matching template will be used.", 
			"Defina modelos de problemas aqui. Quando um novo problema é criado, o primeiro modelo correspondente será usado.");
		m.put("Define labels to be assigned to project, build or pull request. For issues, custom fields can be used which is much more powerful than labels", 
			"Defina rótulos para serem atribuídos ao projeto, build ou solicitação de pull. Para problemas, campos personalizados podem ser usados, o que é muito mais poderoso do que rótulos");
		m.put("Define properties to be used in build spec. Properties will be inherited by child projects, and can be overridden by child properties with same name.", 
			"Defina propriedades para serem usadas na especificação de build. As propriedades serão herdadas por projetos filhos e podem ser substituídas por propriedades filhos com o mesmo nome.");
		m.put("Define rules to preserve builds. A build will be preserved as long as one rule defined here or in parent projects preserves it. All builds will be preserved if no rules are defined here and in parent projects", 
			"Defina regras para preservar builds. Um build será preservado enquanto uma regra definida aqui ou em projetos pai o preservar. Todos os builds serão preservados se nenhuma regra for definida aqui e em projetos pai");
		m.put("Define tag protection rules. Rules defined in parent project are considered to be defined after rules defined here. For a given tag and user, the first matching rule will take effect", 
			"Defina regras de proteção de tag. Regras definidas no projeto pai são consideradas definidas após as regras definidas aqui. Para uma determinada tag e usuário, a primeira regra correspondente terá efeito");
		m.put("Delay for the first retry in seconds. Delay of subsequent retries will be calculated using an exponential back-off based on this value", 
			"Atraso para a primeira tentativa em segundos. O atraso das tentativas subsequentes será calculado usando um back-off exponencial baseado neste valor");
		m.put("Delete", "Excluir");
		m.put("Delete All", "Excluir Tudo");
		m.put("Delete All Queried Builds", "Excluir Todos os Builds Consultados");
		m.put("Delete All Queried Comments", "Excluir Todos os Comentários Consultados");
		m.put("Delete All Queried Issues", "Excluir Todos os Problemas Consultados");
		m.put("Delete All Queried Packages", "Excluir Todos os Pacotes Consultados");
		m.put("Delete All Queried Projects", "Excluir Todos os Projetos Consultados");
		m.put("Delete All Queried Pull Requests", "Excluir Todas as Solicitações de Pull Consultadas");
		m.put("Delete All Queried Users", "Excluir Todos os Usuários Consultados");
		m.put("Delete Build", "Excluir Build");
		m.put("Delete Comment", "Excluir Comentário");
		m.put("Delete Pull Request", "Excluir Solicitação de Pull");
		m.put("Delete SSO account here to reconnect corresponding SSO subject upon next login. Note that SSO subject with verified email will be connected to user with same verified email automatically", 
			"Exclua a conta SSO aqui para reconectar o sujeito SSO correspondente no próximo login. Note que o sujeito SSO com email verificado será conectado ao usuário com o mesmo email verificado automaticamente");
		m.put("Delete Selected", "Excluir Selecionados");
		m.put("Delete Selected Builds", "Excluir Builds Selecionados");
		m.put("Delete Selected Comments", "Excluir Comentários Selecionados");
		m.put("Delete Selected Issues", "Excluir Problemas Selecionados");
		m.put("Delete Selected Packages", "Excluir pacotes selecionados");
		m.put("Delete Selected Projects", "Excluir projetos selecionados");
		m.put("Delete Selected Pull Requests", "Excluir solicitações de pull selecionadas");
		m.put("Delete Selected Users", "Excluir usuários selecionados");
		m.put("Delete Source Branch", "Excluir branch de origem");
		m.put("Delete Source Branch After Merge", "Excluir branch de origem após a mesclagem");
		m.put("Delete dashboard", "Excluir painel");
		m.put("Delete from branch {0}", "Excluir da branch {0}");
		m.put("Delete this", "Excluir isto");
		m.put("Delete this GPG key", "Excluir esta chave GPG");
		m.put("Delete this access token", "Excluir este token de acesso");
		m.put("Delete this branch", "Excluir esta branch");
		m.put("Delete this executor", "Excluir este executor");
		m.put("Delete this field", "Excluir este campo");
		m.put("Delete this import", "Excluir esta importação");
		m.put("Delete this iteration", "Excluir esta iteração");
		m.put("Delete this key", "Excluir esta chave");
		m.put("Delete this link", "Excluir este link");
		m.put("Delete this rule", "Excluir esta regra");
		m.put("Delete this secret", "Excluir este segredo");
		m.put("Delete this state", "Excluir este estado");
		m.put("Delete this tag", "Excluir esta tag");
		m.put("Delete this value", "Excluir este valor");
		m.put("Deleted source branch", "Branch de origem excluída");
		m.put("Deletion not allowed due to branch protection rule", "Exclusão não permitida devido à regra de proteção de branch");
		m.put("Deletion not allowed due to tag protection rule", "Exclusão não permitida devido à regra de proteção de tag");
		m.put("Deletions", "Exclusões");
		m.put("Denied", "Negado");
		m.put("Dependencies & Services", "Dependências e Serviços");
		m.put("Dependency Management", "Gerenciamento de Dependências");
		m.put("Dependency job finished", "Job de dependência finalizado");
		m.put("Dependent Fields", "Campos Dependentes");
		m.put("Depends on <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>", "Depende de <a wicket:id=\"dependencies\"><span wicket:id=\"label\"></span></a>");
		m.put("Descending", "Descendente");
		m.put("Description", "Descrição");
		m.put("Description Template", "Modelo de Descrição");
		m.put("Description Templates", "Modelos de Descrição");
		m.put("Description too long", "Descrição muito longa");
		m.put("Destination Path", "Caminho de Destino");
		m.put("Destinations", "Destinos");
		m.put("Detect Licenses", "Detectar Licenças");
		m.put("Detect Secrets", "Detectar Segredos");
		m.put("Detect Vulnerabilities", "Detectar Vulnerabilidades");
		m.put("Diff is too large to be displayed.", "Diff é muito grande para ser exibido.");
		m.put("Diff options", "Opções de Diff");
		m.put("Digest", "Digest");
		m.put("Digest invalid", "Digest inválido");
		m.put("Directories to Skip", "Diretórios a Ignorar");
		m.put("Directory", "Diretório");
		m.put("Directory (tenant) ID", "ID do Diretório (locatário)");
		m.put("Disable", "Desativar");
		m.put("Disable All Queried Users", "Desativar Todos os Usuários Consultados");
		m.put("Disable Auto Update Check", "Desativar Verificação de Atualização Automática");
		m.put("Disable Dashboard", "Desativar Painel");
		m.put("Disable Selected Users", "Desativar Usuários Selecionados");
		m.put("Disabled", "Desativado");
		m.put("Disabled users and service accounts are excluded from user-month calculation", "Usuários desativados e contas de serviço são excluídos do cálculo de usuário-mês");
		m.put("Disabling account will reset password, clear access tokens, and remove all references from other entities except for past activities. Do you really want to continue?", 
			"Desativar a conta irá redefinir a senha, limpar tokens de acesso e remover todas as referências de outras entidades, exceto atividades passadas. Você realmente deseja continuar?");
		m.put("Disabling accounts will reset password, clear access tokens, and remove all references from other entities except for past activities. Type <code>yes</code> to confirm", 
			"Desativar contas irá redefinir a senha, limpar tokens de acesso e remover todas as referências de outras entidades, exceto atividades passadas. Digite <code>yes</code> para confirmar");
		m.put("Disallowed File Types", "Tipos de Arquivo Não Permitidos");
		m.put("Disallowed file type(s): {0}", "Tipo(s) de arquivo não permitido(s): {0}");
		m.put("Discard", "Descartar");
		m.put("Discard All Queried Pull Requests", "Descartar Todas as Solicitações de Pull Consultadas");
		m.put("Discard Selected Pull Requests", "Descartar Solicitações de Pull Selecionadas");
		m.put("Discarded", "Descartado");
		m.put("Discarded pull request \"{0}\" ({1})", "Solicitação de pull descartada \"{0}\" ({1})");
		m.put("Discord", "Discord");
		m.put("Discord Notifications", "Notificações do Discord");
		m.put("Display Fields", "Exibir Campos");
		m.put("Display Links", "Exibir Links");
		m.put("Display Months", "Exibir Meses");
		m.put("Display Params", "Exibir Parâmetros");
		m.put("Do Not Retrieve Groups", "Não Recuperar Grupos");
		m.put("Do not ignore", "Não ignorar");
		m.put("Do not ignore whitespace", "Não ignorar espaços em branco");
		m.put("Do not retrieve", "Não recuperar");
		m.put("Do not retrieve groups", "Não recuperar grupos");
		m.put("Do you really want to cancel invitation to \"{0}\"?", "Você realmente deseja cancelar o convite para \"{0}\"?");
		m.put("Do you really want to cancel this build?", "Você realmente deseja cancelar esta build?");
		m.put("Do you really want to change target branch to {0}?", "Você realmente deseja alterar a branch de destino para {0}?");
		m.put("Do you really want to delete \"{0}\"?", "Você realmente deseja excluir \"{0}\"?");
		m.put("Do you really want to delete SSO provider \"{0}\"?", "Você realmente deseja excluir o provedor SSO \"{0}\"?");
		m.put("Do you really want to delete board \"{0}\"?", "Você realmente deseja excluir o quadro \"{0}\"?");
		m.put("Do you really want to delete build #{0}?", "Você realmente deseja excluir a build #{0}?");
		m.put("Do you really want to delete group \"{0}\"?", "Você realmente deseja excluir o grupo \"{0}\"?");
		m.put("Do you really want to delete iteration \"{0}\"?", "Você realmente deseja excluir a iteração \"{0}\"?");
		m.put("Do you really want to delete job secret \"{0}\"?", "Você realmente deseja excluir o segredo do job \"{0}\"?");
		m.put("Do you really want to delete pull request #{0}?", "Você realmente deseja excluir a solicitação de pull #{0}?");
		m.put("Do you really want to delete role \"{0}\"?", "Você realmente deseja excluir o papel \"{0}\"?");
		m.put("Do you really want to delete selected query watches?", "Você realmente deseja excluir as consultas selecionadas?");
		m.put("Do you really want to delete tag {0}?", "Você realmente deseja excluir a tag {0}?");
		m.put("Do you really want to delete this GPG key?", "Você realmente deseja excluir esta chave GPG?");
		m.put("Do you really want to delete this SSH key?", "Você realmente deseja excluir esta chave SSH?");
		m.put("Do you really want to delete this SSO account?", "Você realmente deseja excluir esta conta SSO?");
		m.put("Do you really want to delete this access token?", "Você realmente deseja excluir este token de acesso?");
		m.put("Do you really want to delete this board?", "Você realmente deseja excluir este quadro?");
		m.put("Do you really want to delete this build?", "Você realmente deseja excluir esta build?");
		m.put("Do you really want to delete this code comment and all its replies?", "Você realmente deseja excluir este comentário de código e todas as suas respostas?");
		m.put("Do you really want to delete this code comment?", "Você realmente deseja excluir este comentário de código?");
		m.put("Do you really want to delete this directory?", "Você realmente deseja excluir este diretório?");
		m.put("Do you really want to delete this email address?", "Você realmente deseja excluir este endereço de e-mail?");
		m.put("Do you really want to delete this executor?", "Você realmente deseja excluir este executor?");
		m.put("Do you really want to delete this field?", "Você realmente deseja excluir este campo?");
		m.put("Do you really want to delete this file?", "Você realmente deseja excluir este arquivo?");
		m.put("Do you really want to delete this issue?", "Você realmente deseja excluir este problema?");
		m.put("Do you really want to delete this link?", "Você realmente deseja excluir este link?");
		m.put("Do you really want to delete this package?", "Você realmente deseja excluir este pacote?");
		m.put("Do you really want to delete this privilege?", "Você realmente deseja excluir este privilégio?");
		m.put("Do you really want to delete this protection?", "Você realmente deseja excluir esta proteção?");
		m.put("Do you really want to delete this pull request?", "Você realmente deseja excluir este pull request?");
		m.put("Do you really want to delete this reply?", "Você realmente deseja excluir esta resposta?");
		m.put("Do you really want to delete this script?", "Você realmente deseja excluir este script?");
		m.put("Do you really want to delete this state?", "Você realmente deseja excluir este estado?");
		m.put("Do you really want to delete this template?", "Você realmente deseja excluir este modelo?");
		m.put("Do you really want to delete this transition?", "Você realmente deseja excluir esta transição?");
		m.put("Do you really want to delete timesheet \"{0}\"?", "Você realmente deseja excluir a folha de horas \"{0}\"?");
		m.put("Do you really want to delete unused tokens?", "Você realmente deseja excluir tokens não utilizados?");
		m.put("Do you really want to discard batched suggestions?", "Você realmente deseja descartar sugestões agrupadas?");
		m.put("Do you really want to enable this account?", "Você realmente deseja ativar esta conta?");
		m.put("Do you really want to rebuild?", "Você realmente deseja reconstruir?");
		m.put("Do you really want to remove assignee \"{0}\"?", "Você realmente deseja remover o responsável \"{0}\"?");
		m.put("Do you really want to remove password of this user?", "Você realmente deseja remover a senha deste usuário?");
		m.put("Do you really want to remove the issue from iteration \"{0}\"?", "Você realmente deseja remover o problema da iteração \"{0}\"?");
		m.put("Do you really want to remove this account?", "Você realmente deseja remover esta conta?");
		m.put("Do you really want to remove this agent?", "Você realmente deseja remover este agente?");
		m.put("Do you really want to remove this link?", "Você realmente deseja remover este link?");
		m.put("Do you really want to restart this agent?", "Você realmente deseja reiniciar este agente?");
		m.put("Do you really want to unauthorize user \"{0}\"?", "Você realmente deseja desautorizar o usuário \"{0}\"?");
		m.put("Do you really want to use default template?", "Você realmente deseja usar o modelo padrão?");
		m.put("Docker", "Docker");
		m.put("Docker Executable", "Executável Docker");
		m.put("Docker Hub", "Docker Hub");
		m.put("Docker Image", "Imagem Docker");
		m.put("Docker Sock Path", "Caminho do Docker Sock");
		m.put("Dockerfile", "Dockerfile");
		m.put("Documentation", "Documentação");
		m.put("Don't have an account yet?", "Ainda não tem uma conta?");
		m.put("Download", "Baixar");
		m.put("Download <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> or <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. A new agent token will be included in the package", 
			"Baixe <a wicket:id=\"agentZip\" class=\"link-primary\">agent.zip</a> ou <a wicket:id=\"agentTgz\" class=\"link-primary\">agent.tar.gz</a>. Um novo token de agente será incluído no pacote");
		m.put("Download archive of this branch", "Baixar arquivo desta branch");
		m.put("Download full log", "Baixar log completo");
		m.put("Download log", "Baixar log");
		m.put("Download patch", "Baixar patch");
		m.put("Download tag archive", "Baixar arquivo de tag");
		m.put("Dry Run", "Execução Simulada");
		m.put("Due Date", "Data de Vencimento");
		m.put("Due Date Issue Field", "Campo de Problema com Data de Vencimento");
		m.put("Due date", "Data de vencimento");
		m.put("Duplicate authorizations found: ", "Autorizações duplicadas encontradas:");
		m.put("Duplicate authorizations found: {0}", "Autorizações duplicadas encontradas: {0}");
		m.put("Duration", "Duração");
		m.put("Durations", "Durações");
		m.put("ESLint Report", "Relatório ESLint");
		m.put("Edit", "Editar");
		m.put("Edit <code>$HOME/.gem/credentials</code> to add a source", "Edite <code>$HOME/.gem/credentials</code> para adicionar uma fonte");
		m.put("Edit <code>$HOME/.pypirc</code> to add a package repository like below", "Edite <code>$HOME/.pypirc</code> para adicionar um repositório de pacotes como abaixo");
		m.put("Edit Avatar", "Editar Avatar");
		m.put("Edit Estimated Time", "Editar Tempo Estimado");
		m.put("Edit Executor", "Editar Executor");
		m.put("Edit Iteration", "Editar Iteração");
		m.put("Edit Job Secret", "Editar Segredo do Trabalho");
		m.put("Edit My Avatar", "Editar Meu Avatar");
		m.put("Edit Rule", "Editar Regra");
		m.put("Edit Timesheet", "Editar Folha de Horas");
		m.put("Edit dashboard", "Editar painel");
		m.put("Edit issue title", "Editar título do problema");
		m.put("Edit job", "Editar trabalho");
		m.put("Edit on branch {0}", "Editar na branch {0}");
		m.put("Edit on source branch", "Editar na branch de origem");
		m.put("Edit plain", "Editar simples");
		m.put("Edit saved queries", "Editar consultas salvas");
		m.put("Edit this access token", "Editar este token de acesso");
		m.put("Edit this executor", "Editar este executor");
		m.put("Edit this iteration", "Editar esta iteração");
		m.put("Edit this rule", "Editar esta regra");
		m.put("Edit this secret", "Editar este segredo");
		m.put("Edit this state", "Editar este estado");
		m.put("Edit title", "Editar título");
		m.put("Edit with AI", "Editar com IA");
		m.put("Edit {0}", "Editar {0}");
		m.put("Editable Issue Fields", "Campos Editáveis de Problema");
		m.put("Editable Issue Links", "Links Editáveis de Problema");
		m.put("Edited by {0} {1}", "Editado por {0} {1}");
		m.put("Editor", "Editor");
		m.put("Either target branch or source branch has new commits just now, please re-check.", "Ou a branch de destino ou a branch de origem tem novos commits agora, por favor, verifique novamente.");
		m.put("Email", "Email");
		m.put("Email Address", "Endereço de Email");
		m.put("Email Address Verification", "Verificação de Endereço de Email");
		m.put("Email Addresses", "Endereços de Email");
		m.put("Email Templates", "Modelos de Email");
		m.put("Email Verification", "Verificação de Email");
		m.put("Email Verification Template", "Modelo de Verificação de Email");
		m.put("Email address", "Endereço de email");
		m.put("Email address \"{0}\" already used by another account", "Endereço de email \"{0}\" já utilizado por outra conta");
		m.put("Email address \"{0}\" used by account \"{1}\"", "Endereço de email \"{0}\" utilizado pela conta \"{1}\"");
		m.put("Email address \"{0}\" used by disabled account \"{1}\"", "Endereço de email \"{0}\" utilizado pela conta desativada \"{1}\"");
		m.put("Email address already in use: {0}", "Endereço de email já em uso: {0}");
		m.put("Email address already invited: {0}", "Endereço de email já convidado: {0}");
		m.put("Email address already used by another user", "Endereço de email já usado por outro usuário");
		m.put("Email address already used: ", "Endereço de email já utilizado:");
		m.put("Email address to verify", "Endereço de email para verificar");
		m.put("Email addresses with <span class=\"badge badge-warning badge-sm\">ineffective</span> mark are those not belong to or not verified by key owner", 
			"Endereços de email com <span class=\"badge badge-warning badge-sm\">ineficaz</span> são aqueles que não pertencem ou não foram verificados pelo proprietário da chave");
		m.put("Email templates", "Modelos de email");
		m.put("Empty file added.", "Arquivo vazio adicionado.");
		m.put("Empty file removed.", "Arquivo vazio removido.");
		m.put("Enable", "Ativar");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>time tracking</a> for this project to track progress and generate timesheets", 
			"Ative <a href='https://docs.onedev.io/tutorials/issue/time-tracking' target='_blank'>rastreamento de tempo</a> para este projeto para acompanhar o progresso e gerar folhas de horas");
		m.put("Enable <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>package management</a> for this project", 
			"Habilitar <a href='https://docs.onedev.io/tutorials/package/working-with-packages' target='_blank'>gerenciamento de pacotes</a> para este projeto");
		m.put("Enable Account Self Removal", "Habilitar Auto-Remoção de Conta");
		m.put("Enable Account Self Sign-Up", "Habilitar Auto-Cadastro de Conta");
		m.put("Enable All Queried Users", "Habilitar Todos os Usuários Consultados");
		m.put("Enable Anonymous Access", "Habilitar Acesso Anônimo");
		m.put("Enable Auto Backup", "Habilitar Backup Automático");
		m.put("Enable Html Report Publish", "Habilitar Publicação de Relatório Html");
		m.put("Enable Selected Users", "Habilitar Usuários Selecionados");
		m.put("Enable Site Publish", "Habilitar Publicação de Site");
		m.put("Enable TTY Mode", "Habilitar Modo TTY");
		m.put("Enable build support by <a wicket:id=\"addFile\" class=\"link-primary\"></a>", "Habilitar suporte de build por <a wicket:id=\"addFile\" class=\"link-primary\"></a>");
		m.put("Enable if visibility of this field depends on other fields", "Habilitar se a visibilidade deste campo depende de outros campos");
		m.put("Enable if visibility of this param depends on other params", "Habilitar se a visibilidade deste parâmetro depende de outros parâmetros");
		m.put("Enable this if the access token has same permissions as the owner", "Habilitar isto se o token de acesso tiver as mesmas permissões que o proprietário");
		m.put("Enable this option to merge the pull request automatically when ready (all reviewers approved, all required jobs passed etc.)", 
			"Habilitar esta opção para mesclar automaticamente o pull request quando estiver pronto (todos os revisores aprovados, todos os trabalhos necessários concluídos, etc.)");
		m.put("Enable this to allow to run html report publish step. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Habilite isso para permitir a execução da etapa de publicação de relatório html. Para evitar ataques XSS, certifique-se de que este executor só possa ser usado por trabalhos confiáveis");
		m.put("Enable this to allow to run site publish step. OneDev will serve project site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs", 
			"Habilitar isto para permitir a execução da etapa de publicação de site. O OneDev servirá os arquivos do site do projeto como estão. Para evitar ataques XSS, certifique-se de que este executor só possa ser usado por trabalhos confiáveis");
		m.put("Enable this to place intermediate files required by job execution on dynamically allocated persistent volume instead of emptyDir", 
			"Habilitar isto para colocar arquivos intermediários necessários para a execução do trabalho em volume persistente alocado dinamicamente em vez de emptyDir");
		m.put("Enable this to process issue or pull request comments posted via email", "Habilitar isto para processar comentários de problemas ou pull requests postados via e-mail");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Habilitar isto para processar comentários de problemas ou pull requests postados via e-mail. <b class='text-danger'>NOTA:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Subendereçamento</a> precisa ser habilitado para o endereço de e-mail do sistema acima, pois o OneDev o utiliza para rastrear contextos de problemas e pull requests");
		m.put("Enable this to process issue or pull request comments posted via email. <b class='text-danger'>NOTE:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Sub addressing</a> needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts", 
			"Habilitar isto para processar comentários de problemas ou pull requests postados via e-mail. <b class='text-danger'>NOTA:</b> <a href='https://en.wikipedia.org/wiki/Email_address#Subaddressing' target='_blank'>Subendereçamento</a> precisa ser habilitado para o endereço de e-mail do sistema acima, pois o OneDev o utiliza para rastrear contextos de problemas e pull requests");
		m.put("Enable to allow to upload build cache generated during CI/CD job. Uploaded cache can be used by subsequent builds of the project as long as cache key matches", 
			"Habilitar para permitir o upload de cache de build gerado durante o trabalho de CI/CD. O cache enviado pode ser usado por builds subsequentes do projeto, desde que a chave do cache corresponda");
		m.put("End Point", "Ponto de Extremidade");
		m.put("Enforce Conventional Commits", "Impor Commits Convencionais");
		m.put("Enforce Password Policy", "Impor Política de Senha");
		m.put("Enforce Two-factor Authentication", "Impor Autenticação de Dois Fatores");
		m.put("Enforce password policy for new users", "Impor política de senha para novos usuários");
		m.put("Enter New Password", "Digite a Nova Senha");
		m.put("Enter description here", "Insira a descrição aqui");
		m.put("Enter your details to login to your account", "Insira seus dados para fazer login na sua conta");
		m.put("Enter your user name or email to reset password", "Digite seu nome de usuário ou email para redefinir a senha");
		m.put("Entries", "Entradas");
		m.put("Entry", "Entrada");
		m.put("Enumeration", "Enumeração");
		m.put("Env Var", "Var de Ambiente");
		m.put("Environment Variables", "Variáveis de Ambiente");
		m.put("Environment variable <code>serverUrl</code> in above command is taken from OneDev server url specified in <i>Administration / System Setting</i>. Change it if necessary", 
			"A variável de ambiente <code>serverUrl</code> no comando acima é retirada da URL do servidor OneDev especificada em <i>Administração / Configuração do Sistema</i>. Altere se necessário");
		m.put("Equal", "Igual");
		m.put("Error authenticating user", "Erro ao autenticar usuário");
		m.put("Error calculating commits: check log for details", "Erro ao calcular commits: verifique o log para detalhes");
		m.put("Error cherry-picking to {0}: Merge conflicts detected", "Erro ao aplicar cherry-pick em {0}: Conflitos de mesclagem detectados");
		m.put("Error cherry-picking to {0}: {1}", "Erro ao aplicar cherry-pick em {0}: {1}");
		m.put("Error detail of content type &quot;text/plain&quot;", "Detalhe do erro do tipo de conteúdo &quot;text/plain&quot;");
		m.put("Error discovering OIDC metadata", "Erro ao descobrir metadados OIDC");
		m.put("Error executing task", "Erro ao executar tarefa");
		m.put("Error parsing %sbase query: ", "Erro ao analisar consulta %sbase:");
		m.put("Error parsing %squery: ", "Erro ao analisar consulta %squery:");
		m.put("Error parsing build spec", "Erro ao analisar especificação de build");
		m.put("Error rendering widget, check server log for details", "Erro ao renderizar widget, verifique o log do servidor para detalhes");
		m.put("Error reverting on {0}: Merge conflicts detected", "Erro ao reverter em {0}: Conflitos de mesclagem detectados");
		m.put("Error reverting on {0}: {1}", "Erro ao reverter em {0}: {1}");
		m.put("Error validating auto merge commit message: {0}", "Erro ao validar mensagem de commit de mesclagem automática: {0}");
		m.put("Error validating build spec (location: {0}, error message: {1})", "Erro ao validar especificação de build (localização: {0}, mensagem de erro: {1})");
		m.put("Error validating build spec: {0}", "Erro ao validar especificação de build: {0}");
		m.put("Error validating commit message of \"{0}\": {1}", "Erro ao validar mensagem de commit de \"{0}\": {1}");
		m.put("Error validating commit message of <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}", 
			"Erro ao validar mensagem de commit de <a href=\"{0}\" class='text-monospace font-size-sm'>{1}</a>: {2}");
		m.put("Error verifying GPG signature", "Erro ao verificar assinatura GPG");
		m.put("Estimated Time", "Tempo Estimado");
		m.put("Estimated Time Edit Bean", "Editar Bean de Tempo Estimado");
		m.put("Estimated Time Issue Field", "Campo de Problema de Tempo Estimado");
		m.put("Estimated Time:", "Tempo Estimado:");
		m.put("Estimated time", "Tempo estimado");
		m.put("Estimated/Spent time. Click for details", "Tempo estimado/gasto. Clique para detalhes");
		m.put("Evaluate script to get choices", "Avaliar script para obter escolhas");
		m.put("Evaluate script to get default value", "Avaliar script para obter valor padrão");
		m.put("Evaluate script to get value or secret", "Avaliar script para obter valor ou segredo");
		m.put("Evaluate script to get values or secrets", "Avaliar script para obter valores ou segredos");
		m.put("Event Types", "Tipos de Evento");
		m.put("Events", "Eventos");
		m.put("Ever Used Since", "Já Usado Desde");
		m.put("Everything inside this project and all child projects will be deleted and can not be recovered, please type project path <code>{0}</code> below to confirm deletion.", 
			"Tudo dentro deste projeto e todos os projetos filhos serão excluídos e não poderão ser recuperados, por favor digite o caminho do projeto <code>{0}</code> abaixo para confirmar a exclusão.");
		m.put("Example", "Exemplo");
		m.put("Example Plugin Setting", "Configuração de Plugin de Exemplo");
		m.put("Example Property", "Propriedade de Exemplo");
		m.put("Exclude Param Combos", "Excluir Combinações de Parâmetros");
		m.put("Exclude States", "Excluir Estados");
		m.put("Excluded", "Excluído");
		m.put("Excluded Fields", "Campos Excluídos");
		m.put("Executable", "Executável");
		m.put("Execute Commands", "Executar Comandos");
		m.put("Execute Commands via SSH", "Executar Comandos via SSH");
		m.put("Exit Impersonation", "Sair da Impersonação");
		m.put("Exited impersonation", "Impersonação encerrada");
		m.put("Expand all", "Expandir tudo");
		m.put("Expects one or more <tt>&lt;number&gt;(h|m)</tt>. For instance <tt>1h 1m</tt> represents 1 hour and 1 minute", 
			"Espera-se um ou mais <tt>&lt;número&gt;(h|m)</tt>. Por exemplo, <tt>1h 1m</tt> representa 1 hora e 1 minuto");
		m.put("Expects one or more <tt>&lt;number&gt;(w|d|h|m)</tt>. For instance <tt>1w 1d 1h 1m</tt> represents 1 week ({0} days), 1 day ({1} hours), 1 hour, and 1 minute", 
			"Espera-se um ou mais <tt>&lt;número&gt;(w|d|h|m)</tt>. Por exemplo, <tt>1w 1d 1h 1m</tt> representa 1 semana ({0} dias), 1 dia ({1} horas), 1 hora e 1 minuto");
		m.put("Expiration Date:", "Data de Expiração:");
		m.put("Expire Date", "Data de Expiração");
		m.put("Expired", "Expirado");
		m.put("Explicit SSL (StartTLS)", "SSL Explícito (StartTLS)");
		m.put("Export", "Exportar");
		m.put("Export All Queried Issues To...", "Exportar Todas as Questões Consultadas Para...");
		m.put("Export CSV", "Exportar CSV");
		m.put("Export XLSX", "Exportar XLSX");
		m.put("Export as OCI layout", "Exportar como layout OCI");
		m.put("Extend Trial Subscription", "Estender Assinatura de Avaliação");
		m.put("External Authentication", "Autenticação Externa");
		m.put("External Issue Transformers", "Transformadores de Problemas Externos");
		m.put("External Participants", "Participantes Externos");
		m.put("External Password Authenticator", "Autenticador de Senha Externa");
		m.put("External System", "Sistema Externo");
		m.put("External authenticator settings saved", "Configurações do autenticador externo salvas");
		m.put("External participants do not have accounts and involve in the issue via email", "Participantes externos não têm contas e se envolvem no problema via e-mail");
		m.put("Extract the package into a folder. <b class=\"text-danger\">Warning:</b> On Mac OS X, do not extract to Mac managed folders such as Downloads, Desktop, Documents; otherwise you may encounter permission issues starting agent", 
			"Extraia o pacote em uma pasta. <b class=\"text-danger\">Aviso:</b> No Mac OS X, não extraia para pastas gerenciadas pelo Mac, como Downloads, Desktop, Documentos; caso contrário, você pode encontrar problemas de permissão ao iniciar o agente");
		m.put("FAILED", "FALHOU");
		m.put("Fail Threshold", "Limite de Falha");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level", 
			"Falhar build se houver vulnerabilidades com gravidade igual ou superior ao nível especificado");
		m.put("Fail build if there are vulnerabilities with or severer than specified severity level. Note that this only takes effect if build is not failed by other steps", 
			"Falhar build se houver vulnerabilidades com gravidade igual ou superior ao nível especificado. Observe que isso só terá efeito se o build não falhar por outras etapas");
		m.put("Failed", "Falhou");
		m.put("Failed to validate build spec import. Check server log for details", "Falha ao validar importação de especificação de build. Verifique o log do servidor para detalhes");
		m.put("Failed to verify your email address", "Falha ao verificar seu endereço de email");
		m.put("Field Bean", "Bean de Campo");
		m.put("Field Instance", "Instância de Campo");
		m.put("Field Name", "Nome do Campo");
		m.put("Field Spec", "Especificação do Campo");
		m.put("Field Specs", "Especificações do Campo");
		m.put("Field Value", "Valor do Campo");
		m.put("Fields", "Campos");
		m.put("Fields & Links", "Campos & Links");
		m.put("Fields And Links Bean", "Bean de Campos e Links");
		m.put("Fields to Change", "Campos para Alterar");
		m.put("File", "Arquivo");
		m.put("File Changes", "Alterações no Arquivo");
		m.put("File Name", "Nome do Arquivo");
		m.put("File Name Patterns (separated by comma)", "Padrões de Nome de Arquivo (separados por vírgula)");
		m.put("File Path", "Caminho do Arquivo");
		m.put("File Patterns", "Padrões de Arquivo");
		m.put("File Protection", "Proteção de Arquivo");
		m.put("File Protections", "Proteções de Arquivo");
		m.put("File and Symbol Search", "Busca de Arquivo e Símbolo");
		m.put("File changes", "alterações no arquivo");
		m.put("File is too large to edit here", "Arquivo muito grande para editar aqui");
		m.put("File missing or obsolete", "Arquivo ausente ou obsoleto");
		m.put("File name", "nome do arquivo");
		m.put("File name patterns such as *.java, *.c", "Padrões de nome de arquivo como *.java, *.c");
		m.put("Files", "Arquivos");
		m.put("Files to Be Analyzed", "Arquivos a Serem Analisados");
		m.put("Filter", "Filtro");
		m.put("Filter Issues", "Filtrar Problemas");
		m.put("Filter actions", "Filtrar ações");
		m.put("Filter backlog issues", "Filtrar problemas pendentes");
		m.put("Filter branches...", "Filtrar branches...");
		m.put("Filter by name", "Filtrar por nome");
		m.put("Filter by name or email address", "Filtrar por nome ou endereço de email");
		m.put("Filter by name...", "Filtrar por nome...");
		m.put("Filter by path", "Filtrar por caminho");
		m.put("Filter by test suite", "Filtrar por suíte de testes");
		m.put("Filter date range", "Filtrar intervalo de datas");
		m.put("Filter files...", "Filtrar arquivos...");
		m.put("Filter groups...", "Filtrar grupos...");
		m.put("Filter issues", "Filtrar problemas");
		m.put("Filter pull requests", "Filtrar pull requests");
		m.put("Filter roles", "Filtrar funções");
		m.put("Filter tags...", "Filtrar tags...");
		m.put("Filter targets", "Filtrar alvos");
		m.put("Filter users", "Filtrar usuários");
		m.put("Filter...", "Filtrar...");
		m.put("Filters", "Filtros");
		m.put("Find branch", "Encontrar branch");
		m.put("Find or create branch", "Encontrar ou criar branch");
		m.put("Find or create tag", "Encontrar ou criar tag");
		m.put("Find tag", "Encontrar tag");
		m.put("Fingerprint", "Impressão Digital");
		m.put("Finish", "Finalizar");
		m.put("First applicable executor", "Primeiro executor aplicável");
		m.put("Fix", "Corrigir");
		m.put("Fix Type", "Tipo de Correção");
		m.put("Fix Undefined Field Values", "Corrigir Valores de Campos Indefinidos");
		m.put("Fix Undefined Fields", "Corrigir Campos Indefinidos");
		m.put("Fix Undefined States", "Corrigir Estados Indefinidos");
		m.put("Fixed Issues", "Problemas Corrigidos");
		m.put("Fixed issues since...", "Problemas corrigidos desde...");
		m.put("Fixing Builds", "Corrigindo Builds");
		m.put("Fixing Commits", "Corrigindo Commits");
		m.put("Fixing...", "Corrigindo...");
		m.put("Float", "Float");
		m.put("Follow below instructions to publish packages into this project", "Siga as instruções abaixo para publicar pacotes neste projeto");
		m.put("Follow below steps to install agent on remote machine (supports Linux/Windows/Mac OS X/FreeBSD):", 
			"Siga os passos abaixo para instalar o agente na máquina remota (suporta Linux/Windows/Mac OS X/FreeBSD):");
		m.put("For CI/CD job, add this gem to Gemfile like below", "Para trabalho CI/CD, adicione esta gem ao Gemfile como abaixo");
		m.put("For CI/CD job, add this package to requirements.txt and run below to install the package via command step", 
			"Para trabalho CI/CD, adicione este pacote ao requirements.txt e execute o comando abaixo para instalar o pacote via etapa de comando");
		m.put("For CI/CD job, run below to add package repository via command step", "Para trabalho CI/CD, execute o comando abaixo para adicionar o repositório de pacotes via etapa de comando");
		m.put("For CI/CD job, run below to add package source via command step", "Para trabalho CI/CD, execute o comando abaixo para adicionar a fonte do pacote via etapa de comando");
		m.put("For CI/CD job, run below to add source via command step", "Para trabalho CI/CD, execute o comando abaixo para adicionar a fonte via etapa de comando");
		m.put("For CI/CD job, run below to install chart via command step", "Para trabalho CI/CD, execute o comando abaixo para instalar o chart via etapa de comando");
		m.put("For CI/CD job, run below to publish package via command step", "Para trabalho CI/CD, execute o comando abaixo para publicar o pacote via etapa de comando");
		m.put("For CI/CD job, run below to push chart to the repository via command step", "Para trabalho CI/CD, execute o comando abaixo para enviar o chart ao repositório via etapa de comando");
		m.put("For CI/CD job, run below via a command step", "Para trabalho CI/CD, execute o comando abaixo via uma etapa de comando");
		m.put("For a particular project, the first matching entry will be used", "Para um projeto específico, a primeira entrada correspondente será usada");
		m.put("For all issues", "Para todos os problemas");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission", 
			"Para commit de build não alcançável a partir do branch padrão, um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo de trabalho</a> deve ser especificado como token de acesso com permissão para criar branch");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission", 
			"Para commit de build não alcançável a partir do branch padrão, um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo de trabalho</a> deve ser especificado como token de acesso com permissão para criar tag");
		m.put("For build commit not reachable from default branch, a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission", 
			"Para commit de build não alcançável a partir do branch padrão, um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo de trabalho</a> deve ser especificado como token de acesso com permissão para gerenciar problema");
		m.put("For docker aware executors, this path is inside container, and accepts both absolute path and relative path (relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>). For shell related executors which runs on host machine directly, only relative path is accepted", 
			"Para executores conscientes de docker, este caminho está dentro do contêiner e aceita tanto caminho absoluto quanto relativo (relativo ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>). Para executores relacionados a shell que executam diretamente na máquina host, apenas caminho relativo é aceito");
		m.put("For each build, OneDev calculates a list of fixed issues since previous build automatically. This setting provides a default query to further filter/order this list. For a given job, the first matching entry will be used.", 
			"Para cada build, o OneDev calcula automaticamente uma lista de problemas corrigidos desde o build anterior. Esta configuração fornece uma consulta padrão para filtrar/ordenar ainda mais esta lista. Para um trabalho específico, a primeira entrada correspondente será usada.");
		m.put("For each selected branch/tag, a separate build will be generated with branch/tag set to corresponding value", 
			"Para cada branch/tag selecionado, um build separado será gerado com branch/tag definido para o valor correspondente");
		m.put("For issues matching: ", "Para problemas correspondentes:");
		m.put("For very large git repository, you may need to tune options here to reduce memory usage", 
			"Para repositório git muito grande, você pode precisar ajustar opções aqui para reduzir o uso de memória");
		m.put("For web hooks defined here and in parent projects, OneDev will post event data in JSON format to specified URLs when subscribed events happen", 
			"Para web hooks definidos aqui e em projetos pai, o OneDev enviará dados de eventos em formato JSON para URLs especificadas quando eventos assinados ocorrerem");
		m.put("Force", "Forçar");
		m.put("Force Garbage Collection", "Forçar Coleta de Lixo");
		m.put("Forgot Password?", "Esqueceu a Senha?");
		m.put("Forgotten Password?", "Esqueceu a Senha?");
		m.put("Fork Project", "Forkar Projeto");
		m.put("Fork now", "Fazer Fork agora");
		m.put("Forks Of", "Forks de");
		m.put("Frequencies", "Frequências");
		m.put("From Directory", "Do Diretório");
		m.put("From States", "Dos Estados");
		m.put("From extracted folder, run <code>bin\\agent.bat console</code> as administrator on Windows or <code>bin/agent.sh console</code> on other OS", 
			"Da pasta extraída, execute <code>bin\\agent.bat console</code> como administrador no Windows ou <code>bin/agent.sh console</code> em outros sistemas operacionais");
		m.put("From {0}", "De {0}");
		m.put("Full Name", "Nome Completo");
		m.put("Furthest due date", "Data de vencimento mais distante");
		m.put("GPG Keys", "Chaves GPG");
		m.put("GPG Public Key", "Chave Pública GPG");
		m.put("GPG Signing Key", "Chave de Assinatura GPG");
		m.put("GPG Trusted Keys", "Chaves GPG Confiáveis");
		m.put("GPG key deleted", "Chave GPG excluída");
		m.put("GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'", "Chave pública GPG começa com '-----BEGIN PGP PUBLIC KEY BLOCK-----'");
		m.put("GPG signing key will be used to sign commits generated by OneDev, including pull request merge commits, user commits created via web UI or RESTful api.", 
			"A chave de assinatura GPG será usada para assinar commits gerados pelo OneDev, incluindo commits de merge de pull request, commits de usuário criados via interface web ou API RESTful.");
		m.put("Gem Info", "Informações da Gem");
		m.put("General", "Geral");
		m.put("General Settings", "Configurações Gerais");
		m.put("General settings updated", "Configurações gerais atualizadas");
		m.put("Generate", "Gerar");
		m.put("Generate File Checksum", "Gerar Checksum do Arquivo");
		m.put("Generate New", "Gerar Novo");
		m.put("Generic LDAP", "LDAP Genérico");
		m.put("Get", "Obter");
		m.put("Get Groups Using Attribute", "Obter Grupos Usando Atributo");
		m.put("Git", "Git");
		m.put("Git Command Line", "Linha de Comando do Git");
		m.put("Git Credential", "Credencial do Git");
		m.put("Git LFS Storage", "Armazenamento Git LFS");
		m.put("Git Lfs Lock", "Bloqueio Git LFS");
		m.put("Git Location", "Localização do Git");
		m.put("Git Pack Config", "Configuração de Pacote do Git");
		m.put("Git Path", "Caminho do Git");
		m.put("Git email address will be used as git author/committer for commits created on web UI", 
			"O endereço de e-mail do Git será usado como autor/committer para commits criados na interface web");
		m.put("Git pack config updated", "Configuração de pacote do Git atualizada");
		m.put("GitHub", "GitHub");
		m.put("GitHub API URL", "URL da API do GitHub");
		m.put("GitHub Issue Label", "Etiqueta de Problema do GitHub");
		m.put("GitHub Organization", "Organização do GitHub");
		m.put("GitHub Personal Access Token", "Token de Acesso Pessoal do GitHub");
		m.put("GitHub Repositories to Import", "Repositórios do GitHub para Importar");
		m.put("GitHub Repository", "Repositório do GitHub");
		m.put("GitHub personal access token should be generated with scope <b>repo</b> and <b>read:org</b>", 
			"O token de acesso pessoal do GitHub deve ser gerado com escopo <b>repo</b> e <b>read:org</b>");
		m.put("GitLab API URL", "URL da API do GitLab");
		m.put("GitLab Group", "Grupo do GitLab");
		m.put("GitLab Issue Label", "Etiqueta de Problema do GitLab");
		m.put("GitLab Personal Access Token", "Token de Acesso Pessoal do GitLab");
		m.put("GitLab Project", "Projeto do GitLab");
		m.put("GitLab Projects to Import", "Projetos do GitLab para Importar");
		m.put("GitLab personal access token should be generated with scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by user of specified access token will be listed", 
			"O token de acesso pessoal do GitLab deve ser gerado com escopo <b>read_api</b>, <b>read_user</b> e <b>read_repository</b>. Note que apenas grupos/projetos pertencentes ao usuário do token de acesso especificado serão listados");
		m.put("Gitea API URL", "URL da API do Gitea");
		m.put("Gitea Issue Label", "Etiqueta de Problema do Gitea");
		m.put("Gitea Organization", "Organização do Gitea");
		m.put("Gitea Personal Access Token", "Token de Acesso Pessoal do Gitea");
		m.put("Gitea Repositories to Import", "Repositórios do Gitea para Importar");
		m.put("Gitea Repository", "Repositório do Gitea");
		m.put("Github Access Token Secret", "Segredo do Token de Acesso do GitHub");
		m.put("Global", "Global");
		m.put("Global Build Setting", "Configuração Global de Build");
		m.put("Global Issue Setting", "Configuração Global de Problemas");
		m.put("Global Pack Setting", "Configuração Global de Pacotes");
		m.put("Global Views", "Visualizações Globais");
		m.put("Gmail", "Gmail");
		m.put("Go Back", "Voltar");
		m.put("Google Test Report", "Relatório de Teste do Google");
		m.put("Gpg", "Gpg");
		m.put("Gpg Key", "Chave Gpg");
		m.put("Great, your mail service configuration is working", "Ótimo, a configuração do serviço de e-mail está funcionando");
		m.put("Groovy Script", "Script Groovy");
		m.put("Groovy Scripts", "Scripts Groovy");
		m.put("Groovy script to be evaluated. It should return a <i>Date</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. Deve retornar um valor <i>Date</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. It should return a <i>Float</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. Deve retornar um valor <i>Float</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. It should return a <i>Integer</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. Deve retornar um valor <i>Integer</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. It should return a <i>String</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. Deve retornar um valor <i>String</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. It should return a <i>boolean</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. Deve retornar um valor <i>boolean</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. It should return a <i>string</i> value. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. Deve retornar um valor <i>string</i>. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. It should return name of a group. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. Deve retornar o nome de um grupo. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. It should return string or list of string. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. Deve retornar uma string ou lista de strings. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. O valor retornado deve ser uma lista de objetos de fachada de grupo para ser usada como escolhas. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. The return value should be a list of user login names to be used as choices. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. O valor retornado deve ser uma lista de nomes de login de usuários para ser usada como escolhas. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy script to be evaluated. The return value should be a value to color map, for instance:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> if the value does not have a color. Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details", 
			"Script Groovy a ser avaliado. O valor retornado deve ser um mapa de valor para cor, por exemplo:<br><code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, Use <tt>null</tt> se o valor não tiver uma cor. Consulte <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>ajuda de script</a> para detalhes");
		m.put("Groovy scripts", "Scripts Groovy");
		m.put("Group", "Grupo");
		m.put("Group \"{0}\" deleted", "Grupo \"{0}\" excluído");
		m.put("Group Authorization Bean", "Bean de Autorização de Grupo");
		m.put("Group Authorizations", "Autorizações de Grupo");
		m.put("Group Authorizations Bean", "Bean de Autorizações de Grupo");
		m.put("Group By", "Agrupar Por");
		m.put("Group Management", "Gerenciamento de Grupo");
		m.put("Group Name Attribute", "Atributo de Nome do Grupo");
		m.put("Group Retrieval", "Recuperação de Grupo");
		m.put("Group Search Base", "Base de Pesquisa de Grupo");
		m.put("Group Search Filter", "Filtro de Pesquisa de Grupo");
		m.put("Group authorizations updated", "Autorizações de grupo atualizadas");
		m.put("Group created", "Grupo criado");
		m.put("Groups", "Grupos");
		m.put("Groups Claim", "Reivindicação de Grupos");
		m.put("Guide Line", "Diretriz");
		m.put("HTTP(S)", "HTTP(S)");
		m.put("HTTP(S) Clone URL", "URL de Clone HTTP(S)");
		m.put("Has Owner Permissions", "Tem Permissões de Proprietário");
		m.put("Has Running Builds", "Tem Builds em Execução");
		m.put("Heap Memory Usage", "Uso de Memória Heap");
		m.put("Helm(s)", "Helm(s)");
		m.put("Help", "Ajuda");
		m.put("Hide", "Ocultar");
		m.put("Hide Archived", "Ocultar Arquivados");
		m.put("Hide comment", "Ocultar comentário");
		m.put("Hide saved queries", "Ocultar consultas salvas");
		m.put("High", "Alta");
		m.put("High Availability & Scalability", "Alta Disponibilidade e Escalabilidade");
		m.put("High Severity", "Alta Gravidade");
		m.put("History", "Histórico");
		m.put("History of comparing revisions is unrelated", "O histórico de comparação de revisões é não relacionado");
		m.put("History of target branch and source branch is unrelated", "O histórico do branch de destino e do branch de origem é não relacionado");
		m.put("Host name or ip address of remote machine to run commands via SSH", "Nome do host ou endereço IP da máquina remota para executar comandos via SSH");
		m.put("Hours Per Day", "Horas Por Dia");
		m.put("How to Publish", "Como Publicar");
		m.put("Html Report", "Relatório Html");
		m.put("Http Method", "Método Http");
		m.put("I didn't eat it. I swear!", "Eu não comi. Eu juro!");
		m.put("ID token was expired", "Token de ID expirou");
		m.put("IMAP Host", "Host IMAP");
		m.put("IMAP Password", "Senha IMAP");
		m.put("IMAP User", "Usuário IMAP");
		m.put("IMPORTANT:", "IMPORTANTE:");
		m.put("IP Address", "Endereço IP");
		m.put("Id", "ID");
		m.put("Identify Field", "Campo de Identificação");
		m.put("If enabled, scheduled backup will run on lead server which is <span wicket:id=\"leadServer\"></span> currently", 
			"Se habilitado, o backup agendado será executado no servidor principal que é <span wicket:id=\"leadServer\"></span> atualmente");
		m.put("If enabled, source branch will be deleted automatically after merge the pull request if user has permission to do that", 
			"Se habilitado, a branch de origem será excluída automaticamente após a mesclagem do pull request, se o usuário tiver permissão para isso");
		m.put("If specified, OneDev will only display iterations with this prefix", "Se especificado, o OneDev exibirá apenas iterações com este prefixo");
		m.put("If specified, all public and internal projects imported from GitLab will use these as default roles. Private projects are not affected", 
			"Se especificado, todos os projetos públicos e internos importados do GitLab usarão estas como funções padrão. Projetos privados não são afetados");
		m.put("If specified, all public repositories imported from GitHub will use these as default roles. Private repositories are not affected", 
			"Se especificado, todos os repositórios públicos importados do GitHub usarão estas como funções padrão. Repositórios privados não são afetados");
		m.put("If specified, total estimated/spent time of an issue will also include linked issues of this type", 
			"Se especificado, o tempo total estimado/gasto de um problema também incluirá problemas vinculados deste tipo");
		m.put("If this option is enabled, git lfs command needs to be installed on OneDev server (even this step runs on other node)", 
			"Se esta opção estiver habilitada, o comando git lfs precisa estar instalado no servidor OneDev (mesmo que esta etapa seja executada em outro nó)");
		m.put("If ticked, group indicated by this field will be able to edit estimated time of corresponding issues if time tracking is enabled", 
			"Se marcado, o grupo indicado por este campo poderá editar o tempo estimado dos problemas correspondentes, se o rastreamento de tempo estiver habilitado");
		m.put("Ignore", "Ignorar");
		m.put("Ignore File", "Ignorar Arquivo");
		m.put("Ignore activities irrelevant to me", "Ignorar atividades irrelevantes para mim");
		m.put("Ignore all", "Ignorar tudo");
		m.put("Ignore all whitespace", "Ignorar todos os espaços em branco");
		m.put("Ignore change", "Ignorar alteração");
		m.put("Ignore change whitespace", "Ignorar espaços em branco na alteração");
		m.put("Ignore leading", "Ignorar início");
		m.put("Ignore leading whitespace", "Ignorar espaços em branco no início");
		m.put("Ignore this field", "Ignorar este campo");
		m.put("Ignore this param", "Ignorar este parâmetro");
		m.put("Ignore trailing", "Ignorar final");
		m.put("Ignore trailing whitespace", "Ignorar espaços em branco no final");
		m.put("Ignored Licenses", "Licenças Ignoradas");
		m.put("Image", "Imagem");
		m.put("Image Labels", "Rótulos de Imagem");
		m.put("Image Manifest", "Manifesto de Imagem");
		m.put("Image Size", "Tamanho da Imagem");
		m.put("Image Text", "Texto da Imagem");
		m.put("Image URL", "URL da Imagem");
		m.put("Image URL should be specified", "URL da Imagem deve ser especificado");
		m.put("Imap Ssl Setting", "Configuração de SSL do IMAP");
		m.put("Imap With Ssl", "IMAP com SSL");
		m.put("Impersonate", "Personificar");
		m.put("Implicit SSL", "SSL Implícito");
		m.put("Import", "Importar");
		m.put("Import All Projects", "Importar Todos os Projetos");
		m.put("Import All Repositories", "Importar Todos os Repositórios");
		m.put("Import Group", "Importar Grupo");
		m.put("Import Issues", "Importar Problemas");
		m.put("Import Option", "Opção de Importação");
		m.put("Import Organization", "Importar Organização");
		m.put("Import Project", "Importar Projeto");
		m.put("Import Projects", "Importar Projetos");
		m.put("Import Repositories", "Importar Repositórios");
		m.put("Import Repository", "Importar Repositório");
		m.put("Import Server", "Importar Servidor");
		m.put("Import Workspace", "Importar Espaço de Trabalho");
		m.put("Import build spec elements (jobs, services, step templates and properties) from other projects. Imported elements are treated as if they are defined locally. Locally defined elements will override imported elements with same name", 
			"Importar elementos de especificação de build (jobs, serviços, templates de etapas e propriedades) de outros projetos. Elementos importados são tratados como se fossem definidos localmente. Elementos definidos localmente substituirão elementos importados com o mesmo nome");
		m.put("Importing Issues from {0}", "Importando Problemas de {0}");
		m.put("Importing from {0}", "Importando de {0}");
		m.put("Importing issues into currrent project. Please note that issue numbers will only be retained if the whole project fork graph does not have any issues to avoid duplicate issue numbers", 
			"Importando problemas para o projeto atual. Observe que os números dos problemas só serão mantidos se todo o gráfico de fork do projeto não tiver nenhum problema para evitar números duplicados");
		m.put("Importing projects from {0}", "Importando projetos de {0}");
		m.put("Imports", "Importações");
		m.put("In Projects", "Nos Projetos");
		m.put("In case IMAP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"Caso o certificado do host IMAP seja autoassinado ou sua raiz CA não seja aceita, você pode instruir o OneDev a ignorar a verificação do certificado. <b class='text-danger'>AVISO: </b> Em uma rede não confiável, isso pode levar a um ataque man-in-the-middle, e você deve <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>importar o certificado para o OneDev</a> em vez disso");
		m.put("In case SMTP host certificate is self-signed or its CA root is not accepted, you may tell OneDev to bypass certificate check. <b class='text-danger'>WARNING: </b> In an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the certificate into OneDev</a> instead", 
			"Caso o certificado do host SMTP seja autoassinado ou sua raiz CA não seja aceita, você pode instruir o OneDev a ignorar a verificação do certificado. <b class='text-danger'>AVISO: </b> Em uma rede não confiável, isso pode levar a um ataque man-in-the-middle, e você deve <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>importar o certificado para o OneDev</a> em vez disso");
		m.put("In case anonymous access is disabled or anonymous user does not have enough permission for a resource operation, you will need to authenticate by providing user name and password (or access token) via http basic auth header", 
			"No caso de acesso anônimo estar desativado ou o usuário anônimo não ter permissão suficiente para uma operação de recurso, você precisará autenticar fornecendo nome de usuário e senha (ou token de acesso) via cabeçalho de autenticação básica http");
		m.put("In case cache is not hit via above key, OneDev will loop through load keys defined here in order until a matching cache is found in project hierarchy. A cache is considered matching if its key is prefixed with the load key. If multiple caches matches, the most recent cache will be returned", 
			"Caso o cache não seja encontrado via a chave acima, o OneDev percorrerá as chaves de carregamento definidas aqui em ordem até encontrar um cache correspondente na hierarquia do projeto. Um cache é considerado correspondente se sua chave for prefixada com a chave de carregamento. Se vários caches corresponderem, o cache mais recente será retornado");
		m.put("In case cache needs to be uploaded, this property specifies target project for the upload. Leave empty for current project", 
			"Caso o cache precise ser carregado, esta propriedade especifica o projeto de destino para o upload. Deixe vazio para o projeto atual");
		m.put("In case the pull request status is out of sync with underlying repository, you may synchronize them manually here", 
			"Caso o status do pull request esteja fora de sincronização com o repositório subjacente, você pode sincronizá-los manualmente aqui");
		m.put("In case user group membership maintained at group side, this property specifies base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>", 
			"Caso a associação de grupo de usuários seja mantida no lado do grupo, esta propriedade especifica o nó base para a busca de grupos. Por exemplo: <i>ou=groups, dc=example, dc=com</i>");
		m.put("In case user group relationship maintained at group side, this filter is used to determine belonging groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In this example, <i>{0}</i> represents DN of current user", 
			"Caso a relação de grupo de usuários seja mantida no lado do grupo, este filtro é usado para determinar os grupos pertencentes ao usuário atual. Por exemplo: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. Neste exemplo, <i>{0}</i> representa o DN do usuário atual");
		m.put("In case you are using external issue tracker, you can define transformers to transform external issue references into external issue links in various places, such as commit messages and pull request descriptions", 
			"Caso você esteja usando um rastreador de problemas externo, você pode definir transformadores para transformar referências de problemas externos em links de problemas externos em vários lugares, como mensagens de commit e descrições de pull request");
		m.put("In rare cases, your issues might be out of sync with workflow settings (undefined state/field etc.). Run integrity check below to find problems and get them fixed.", 
			"Em casos raros, seus problemas podem estar fora de sincronização com as configurações de fluxo de trabalho (estado/campo indefinido etc.). Execute a verificação de integridade abaixo para encontrar problemas e corrigi-los.");
		m.put("Inbox Poll Setting", "Configuração de Poll da Caixa de Entrada");
		m.put("Include Child Projects", "Incluir Projetos Filhos");
		m.put("Include Disabled", "Incluir Desativados");
		m.put("Include Forks", "Incluir Forks");
		m.put("Include When Issue is Opened", "Incluir Quando o Problema for Aberto");
		m.put("Incompatibilities", "Incompatibilidades");
		m.put("Inconsistent issuer in provider metadata and ID token", "Emissor inconsistente nos metadados do provedor e no token de ID");
		m.put("Indicator", "Indicador");
		m.put("Inherit from parent", "Herdar do pai");
		m.put("Inherited", "Herdado");
		m.put("Input Spec", "Especificação de Entrada");
		m.put("Input URL", "URL de Entrada");
		m.put("Input allowed CORS origin, hit ENTER to add", "Insira a origem CORS permitida, pressione ENTER para adicionar");
		m.put("Input revision", "Insira a revisão");
		m.put("Input title", "Insira o título");
		m.put("Input title here", "Insira o título aqui");
		m.put("Input user search base. Hit ENTER to add", "Insira a base de busca de usuários. Pressione ENTER para adicionar");
		m.put("Input user search bases. Hit ENTER to add", "Insira as bases de busca de usuários. Pressione ENTER para adicionar");
		m.put("Insert", "Inserir");
		m.put("Insert Image", "Inserir Imagem");
		m.put("Insert Link", "Inserir Link");
		m.put("Insert link to this file", "Inserir link para este arquivo");
		m.put("Insert this image", "Inserir esta imagem");
		m.put("Install Subscription Key", "Instalar Chave de Assinatura");
		m.put("Integer", "Inteiro");
		m.put("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Acesso ao shell web interativo para trabalhos em execução é um recurso empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("Internal Database", "Banco de Dados Interno");
		m.put("Interpreter", "Interpretador");
		m.put("Invalid GPG signature", "Assinatura GPG inválida");
		m.put("Invalid PCRE syntax", "Sintaxe PCRE inválida");
		m.put("Invalid access token: {0}", "Token de acesso inválido: {0}");
		m.put("Invalid credentials", "Credenciais inválidas");
		m.put("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"", "Intervalo de datas inválido, esperado \"yyyy-MM-dd a yyyy-MM-dd\"");
		m.put("Invalid email address: {0}", "Endereço de email inválido: {0}");
		m.put("Invalid invitation code", "Código de convite inválido");
		m.put("Invalid issue date of ID token", "Data de emissão do token de ID inválida");
		m.put("Invalid issue number: {0}", "Número de emissão inválido: {0}");
		m.put("Invalid pull request number: {0}", "Número de pull request inválido: {0}");
		m.put("Invalid request path", "Caminho de solicitação inválido");
		m.put("Invalid selection, click for details", "Seleção inválida, clique para detalhes");
		m.put("Invalid ssh signature", "Assinatura ssh inválida");
		m.put("Invalid state response", "Resposta de estado inválida");
		m.put("Invalid state. Please make sure you are visiting OneDev using server url specified in system setting", 
			"Estado inválido. Por favor, certifique-se de que está visitando o OneDev usando a URL do servidor especificada na configuração do sistema");
		m.put("Invalid subscription key", "Chave de assinatura inválida");
		m.put("Invalid working period", "Período de trabalho inválido");
		m.put("Invitation sent to \"{0}\"", "Convite enviado para \"{0}\"");
		m.put("Invitation to \"{0}\" deleted", "Convite para \"{0}\" excluído");
		m.put("Invitations", "Convites");
		m.put("Invitations sent", "Convites enviados");
		m.put("Invite", "Convidar");
		m.put("Invite Users", "Convidar Usuários");
		m.put("Is Site Admin", "É Administrador do Site");
		m.put("Issue", "Problema");
		m.put("Issue #{0} deleted", "Problema #{0} excluído");
		m.put("Issue Board", "Quadro de Problemas");
		m.put("Issue Boards", "Quadros de Problemas");
		m.put("Issue Close States", "Estados de Fechamento de Problemas");
		m.put("Issue Creation Setting", "Configuração de Criação de Problemas");
		m.put("Issue Creation Settings", "Configurações de Criação de Problemas");
		m.put("Issue Custom Fields", "Campos Personalizados de Problemas");
		m.put("Issue Description", "Descrição do Problema");
		m.put("Issue Description Templates", "Modelos de Descrição de Problemas");
		m.put("Issue Details", "Detalhes do Problema");
		m.put("Issue Field", "Campo de Problema");
		m.put("Issue Field Mapping", "Mapeamento de Campo de Problema");
		m.put("Issue Field Mappings", "Mapeamentos de Campo de Problema");
		m.put("Issue Field Set", "Conjunto de Campos de Problema");
		m.put("Issue Fields", "Campos de Problema");
		m.put("Issue Filter", "Filtro de Problema");
		m.put("Issue Import Option", "Opção de Importação de Problema");
		m.put("Issue Label Mapping", "Mapeamento de Etiqueta de Problema");
		m.put("Issue Label Mappings", "Mapeamentos de Etiqueta de Problema");
		m.put("Issue Link", "Link de Problema");
		m.put("Issue Link Mapping", "Mapeamento de Link de Problema");
		m.put("Issue Link Mappings", "Mapeamentos de Link de Problema");
		m.put("Issue Links", "Links de Problema");
		m.put("Issue Management", "Gerenciamento de Problemas");
		m.put("Issue Notification", "Notificação de Problema");
		m.put("Issue Notification Template", "Modelo de Notificação de Problema");
		m.put("Issue Notification Unsubscribed", "Notificação de Problema Cancelada");
		m.put("Issue Notification Unsubscribed Template", "Modelo de Notificação de Problema Cancelada");
		m.put("Issue Pattern", "Padrão de Problema");
		m.put("Issue Priority Mapping", "Mapeamento de Prioridade de Problema");
		m.put("Issue Priority Mappings", "Mapeamentos de Prioridade de Problema");
		m.put("Issue Query", "Consulta de Problema");
		m.put("Issue Settings", "Configurações de Problema");
		m.put("Issue State", "Estado do Problema");
		m.put("Issue State Mapping", "Mapeamento de Estado de Problema");
		m.put("Issue State Mappings", "Mapeamentos de Estado de Problema");
		m.put("Issue State Transition", "Transição de Estado de Problema");
		m.put("Issue State Transitions", "Transições de Estado de Problema");
		m.put("Issue States", "Estados de Problema");
		m.put("Issue Statistics", "Estatísticas de Problema");
		m.put("Issue Stats", "Estatísticas de Problema");
		m.put("Issue Status Mapping", "Mapeamento de Status de Problema");
		m.put("Issue Status Mappings", "Mapeamentos de Status de Problema");
		m.put("Issue Stopwatch Overdue", "Cronômetro de Problema Atrasado");
		m.put("Issue Stopwatch Overdue Notification Template", "Modelo de Notificação de Cronômetro de Problema Atrasado");
		m.put("Issue Tag Mapping", "Mapeamento de Tag de Problema");
		m.put("Issue Tag Mappings", "Mapeamentos de Tag de Problema");
		m.put("Issue Template", "Modelo de Problema");
		m.put("Issue Transition ({0} -> {1})", "Transição de Problema ({0} -> {1})");
		m.put("Issue Type Mapping", "Mapeamento de Tipo de Problema");
		m.put("Issue Type Mappings", "Mapeamentos de Tipo de Problema");
		m.put("Issue Votes", "Votos de Problema");
		m.put("Issue administrative permission inside a project, including batch operations over multiple issues", 
			"Permissão administrativa de problema dentro de um projeto, incluindo operações em lote sobre múltiplos problemas");
		m.put("Issue count", "Contagem de Problemas");
		m.put("Issue in state", "Problema em estado");
		m.put("Issue list", "Lista de Problemas");
		m.put("Issue management not enabled in this project", "Gerenciamento de problemas não habilitado neste projeto");
		m.put("Issue management permission required to move issues", "Permissão de gerenciamento de problemas necessária para mover problemas");
		m.put("Issue not exist or access denied", "Problema não existe ou acesso negado");
		m.put("Issue number", "Número do Problema");
		m.put("Issue query watch only affects new issues. To manage watch status of existing issues in batch, filter issues by watch status in issues page, and then take appropriate action", 
			"Consulta de problema assistida afeta apenas novos problemas. Para gerenciar o status de acompanhamento de problemas existentes em lote, filtre problemas pelo status de acompanhamento na página de problemas e tome a ação apropriada");
		m.put("Issue state duration statistics", "Estatísticas de duração de estado de problema");
		m.put("Issue state frequency statistics", "Estatísticas de frequência de estado de problema");
		m.put("Issue state trend statistics", "Estatísticas de tendência de estado de problema");
		m.put("Issue statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Estatísticas de problema são um recurso empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("Issue workflow changed, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliation</a> needs to be performed to make data consistent. You may do this after making all necessary changes", 
			"Fluxo de trabalho de problema alterado, <a wicket:id=\"reconcile\" class=\"link-primary\">reconciliação</a> precisa ser realizada para tornar os dados consistentes. Você pode fazer isso após realizar todas as alterações necessárias");
		m.put("Issues", "Problemas");
		m.put("Issues can be created in this project by sending email to this address", "Problemas podem ser criados neste projeto enviando email para este endereço");
		m.put("Issues copied", "Problemas copiados");
		m.put("Issues moved", "Problemas movidos");
		m.put("Italic", "Itálico");
		m.put("Iteration", "Iteração");
		m.put("Iteration \"{0}\" closed", "Iteração \"{0}\" encerrada");
		m.put("Iteration \"{0}\" deleted", "Iteração \"{0}\" excluída");
		m.put("Iteration \"{0}\" is closed", "Iteração \"{0}\" está encerrada");
		m.put("Iteration \"{0}\" is reopened", "Iteração \"{0}\" foi reaberta");
		m.put("Iteration \"{0}\" reopened", "Iteração \"{0}\" reaberta");
		m.put("Iteration Edit Bean", "Editar Bean de Iteração");
		m.put("Iteration Name", "Nome da Iteração");
		m.put("Iteration Names", "Nomes das Iterações");
		m.put("Iteration Prefix", "Prefixo da Iteração");
		m.put("Iteration list", "Lista de Iterações");
		m.put("Iteration saved", "Iteração salva");
		m.put("Iteration spans too long to show burndown chart", "Iteração abrange um período muito longo para exibir o gráfico de burndown");
		m.put("Iteration start and due date should be specified to show burndown chart", "Data de início e vencimento da Iteração devem ser especificadas para exibir o gráfico de burndown");
		m.put("Iteration start date should be before due date", "A data de início da Iteração deve ser anterior à data de vencimento");
		m.put("Iterations", "Iterações");
		m.put("Iterations Bean", "Bean de Iterações");
		m.put("JIRA Issue Priority", "Prioridade de Issue do JIRA");
		m.put("JIRA Issue Status", "Status de Issue do JIRA");
		m.put("JIRA Issue Type", "Tipo de Issue do JIRA");
		m.put("JIRA Project", "Projeto JIRA");
		m.put("JIRA Projects to Import", "Projetos JIRA para Importar");
		m.put("JUnit Report", "Relatório JUnit");
		m.put("JVM", "JVM");
		m.put("JaCoCo Coverage Report", "Relatório de Cobertura JaCoCo");
		m.put("Jest Coverage Report", "Relatório de Cobertura Jest");
		m.put("Jest Test Report", "Relatório de Teste Jest");
		m.put("Job", "Job");
		m.put("Job \"{0}\" associated with the build not found.", "Job \"{0}\" associado à build não encontrado.");
		m.put("Job Authorization", "Autorização de Job");
		m.put("Job Cache Management", "Gerenciamento de Cache de Job");
		m.put("Job Dependencies", "Dependências de Job");
		m.put("Job Dependency", "Dependência de Job");
		m.put("Job Executor", "Executor de Job");
		m.put("Job Executor Bean", "Bean de Executor de Job");
		m.put("Job Executors", "Executores de Job");
		m.put("Job Name", "Nome do Job");
		m.put("Job Names", "Nomes de Job");
		m.put("Job Param", "Parâmetro de Trabalho");
		m.put("Job Parameters", "Parâmetros de Trabalho");
		m.put("Job Privilege", "Privilégio de Job");
		m.put("Job Privileges", "Privilégios de Job");
		m.put("Job Properties", "Propriedades de Job");
		m.put("Job Properties Bean", "Bean de Propriedades de Job");
		m.put("Job Property", "Propriedade de Job");
		m.put("Job Secret", "Segredo de Job");
		m.put("Job Secret Edit Bean", "Editar Bean de Segredo de Job");
		m.put("Job Secrets", "Segredos de Job");
		m.put("Job Trigger", "Trigger de Job");
		m.put("Job Trigger Bean", "Bean de Trigger de Job");
		m.put("Job administrative permission, including deleting builds of the job. It implies all other job permissions", 
			"Permissão administrativa de Job, incluindo exclusão de builds do Job. Implica todas as outras permissões de Job");
		m.put("Job cache \"{0}\" deleted", "Cache de Job \"{0}\" excluído");
		m.put("Job dependencies determines the order and concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs", 
			"Dependências de Job determinam a ordem e concorrência ao executar diferentes Jobs. Você também pode especificar artefatos para recuperar de Jobs upstream");
		m.put("Job executor tested successfully", "Executor de Job testado com sucesso");
		m.put("Job executors", "Executores de Job");
		m.put("Job name", "Nome do Job");
		m.put("Job properties saved", "Propriedades de Job salvas");
		m.put("Job secret \"{0}\" deleted", "Segredo de Job \"{0}\" excluído");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package ${permission} permission", 
			"O segredo de Job 'access-token' deve ser definido na configuração de build do projeto como um token de acesso com permissão de pacote ${permission}");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package read permission", 
			"O segredo de Job 'access-token' deve ser definido na configuração de build do projeto como um token de acesso com permissão de leitura de pacote");
		m.put("Job secret 'access-token' should be defined in project build setting as an access token with package write permission", 
			"O segredo de Job 'access-token' deve ser definido na configuração de build do projeto como um token de acesso com permissão de escrita de pacote");
		m.put("Job token", "Token de Job");
		m.put("Job will run on head commit of default branch", "O Job será executado no commit principal do branch padrão");
		m.put("Job will run on head commit of target branch", "O Job será executado no commit principal do branch alvo");
		m.put("Job will run on merge commit of target branch and source branch", "O Job será executado no commit de merge do branch alvo e do branch de origem");
		m.put("Job will run on merge commit of target branch and source branch.<br><b class='text-info'>NOTE:</b> Unless required by branch protection rule, this trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"O Job será executado no commit de merge do branch alvo e do branch de origem.<br><b class='text-info'>NOTA:</b> A menos que exigido pela regra de proteção de branch, este trigger ignorará commits com mensagens contendo <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, ou <code>[no job]</code>");
		m.put("Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>", 
			"O Job será executado quando o código for commitado. <b class='text-info'>NOTA:</b> Este trigger ignorará commits com mensagens contendo <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, <code>[skip job]</code>, <code>[job skip]</code>, ou <code>[no job]</code>");
		m.put("Job workspace", "Workspace de Job");
		m.put("Jobs", "Jobs");
		m.put("Jobs marked with <span class=\"text-danger\">*</span> are required to be successful", 
			"Jobs marcados com <span class=\"text-danger\">*</span> são obrigados a serem bem-sucedidos");
		m.put("Jobs required to be successful on merge commit: ", "Jobs obrigados a serem bem-sucedidos no commit de merge:");
		m.put("Jobs required to be successful: ", "Jobs obrigados a serem bem-sucedidos:");
		m.put("Jobs with same sequential group and executor will be executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for jobs executing by same executor and deploying to prod environment of current project to avoid conflicting deployments", 
			"Jobs com o mesmo grupo sequencial e executor serão executados sequencialmente. Por exemplo, você pode especificar esta propriedade como <tt>@project_path@:prod</tt> para Jobs executados pelo mesmo executor e implantando no ambiente de produção do projeto atual para evitar implantações conflitantes");
		m.put("Key", "Chave");
		m.put("Key Fingerprint", "Impressão Digital da Chave");
		m.put("Key ID", "ID da Chave");
		m.put("Key Secret", "Segredo da Chave");
		m.put("Key Type", "Tipo de Chave");
		m.put("Kubectl Config File", "Arquivo de Configuração do Kubectl");
		m.put("Kubernetes", "Kubernetes");
		m.put("Kubernetes Executor", "Executor Kubernetes");
		m.put("LDAP URL", "URL LDAP");
		m.put("Label", "Etiqueta");
		m.put("Label Management", "Gerenciamento de Etiquetas");
		m.put("Label Management Bean", "Bean de Gerenciamento de Etiquetas");
		m.put("Label Name", "Nome da Etiqueta");
		m.put("Label Spec", "Especificação da Etiqueta");
		m.put("Label Value", "Valor da Etiqueta");
		m.put("Labels", "Etiquetas");
		m.put("Labels Bean", "Bean de Etiquetas");
		m.put("Labels can be defined in Administration / Label Management", "Etiquetas podem ser definidas em Administração / Gerenciamento de Etiquetas");
		m.put("Labels have been updated", "Etiquetas foram atualizadas");
		m.put("Language", "Idioma");
		m.put("Last Accessed", "Último Acesso");
		m.put("Last Finished of Specified Job", "Última Finalização do Job Especificado");
		m.put("Last Modified", "Última Modificação");
		m.put("Last Published", "Última Publicação");
		m.put("Last Update", "Última Atualização");
		m.put("Last commit", "Último commit");
		m.put("Last commit hash", "Hash do último commit");
		m.put("Last commit index version", "Versão do índice do último commit");
		m.put("Leaf Projects", "Projetos Folha");
		m.put("Least branch coverage", "Menor cobertura de branch");
		m.put("Least line coverage", "Menor cobertura de linha");
		m.put("Leave a comment", "Deixe um comentário");
		m.put("Leave a note", "Deixe uma nota");
		m.put("Left", "Esquerda");
		m.put("Less", "Menos");
		m.put("License Agreement", "Acordo de Licença");
		m.put("License Setting", "Configuração de Licença");
		m.put("Licensed To", "Licenciado Para");
		m.put("Licensed To:", "Licenciado Para:");
		m.put("Line", "Linha");
		m.put("Line changes", "Alterações de linha");
		m.put("Line: ", "Linha:");
		m.put("Lines", "Linhas");
		m.put("Link", "Link");
		m.put("Link Existing User", "Vincular Usuário Existente");
		m.put("Link Spec", "Especificação de Link");
		m.put("Link Spec Opposite", "Especificação de Link Oposta");
		m.put("Link Text", "Texto do Link");
		m.put("Link URL", "URL do Link");
		m.put("Link URL should be specified", "URL do Link deve ser especificado");
		m.put("Link User Bean", "Vincular Usuário Bean");
		m.put("Linkable Issues", "Problemas Linkáveis");
		m.put("Linkable Issues On the Other Side", "Problemas Linkáveis do Outro Lado");
		m.put("Links", "Links");
		m.put("Links can be used to associate different issues. For instance, an issue can be linked to sub issues or related issues", 
			"Links podem ser usados para associar diferentes problemas. Por exemplo, um problema pode ser vinculado a subproblemas ou problemas relacionados");
		m.put("List", "Lista");
		m.put("Literal", "Literal");
		m.put("Literal default value", "Valor padrão literal");
		m.put("Literal value", "Valor literal");
		m.put("Load Keys", "Carregar Chaves");
		m.put("Loading emojis...", "Carregando emojis...");
		m.put("Loading...", "Carregando...");
		m.put("Log", "Log");
		m.put("Log Work", "Registrar Trabalho");
		m.put("Log not available for offline agent", "Log não disponível para agente offline");
		m.put("Log work", "Registrar trabalho");
		m.put("Login Name", "Nome de Login");
		m.put("Login and generate refresh token", "Faça login e gere o token de atualização");
		m.put("Login name already used by another account", "Nome de login já usado por outra conta");
		m.put("Login name or email", "Nome de login ou email");
		m.put("Login name or email address", "Nome de login ou endereço de email");
		m.put("Login to OneDev docker registry", "Login no registro docker do OneDev");
		m.put("Login to comment", "Login para comentar");
		m.put("Login to comment on selection", "Login para comentar na seleção");
		m.put("Login to vote", "Login para votar");
		m.put("Login user needs to have package write permission over the project below", "O usuário de login precisa ter permissão de escrita de pacote sobre o projeto abaixo");
		m.put("Login with {0}", "Login com {0}");
		m.put("Logo for Dark Mode", "Logo para Modo Escuro");
		m.put("Logo for Light Mode", "Logo para Modo Claro");
		m.put("Long-live refresh token of above account which will be used to generate access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to generate refresh token. Note that whenever client id, client secret, or account name is changed, refresh token should be re-generated", 
			"Token de atualização de longa duração da conta acima que será usado para gerar token de acesso ao Gmail. <b class='text-info'>DICAS: </b> você pode usar o botão no lado direito deste campo para gerar o token de atualização. Observe que sempre que o id do cliente, segredo do cliente ou nome da conta for alterado, o token de atualização deve ser regenerado");
		m.put("Long-live refresh token of above account which will be used to generate access token to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right side of this field to login to your office 365 account and generate refresh token. Note that whenever tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated", 
			"Token de atualização de longa duração da conta acima que será usado para gerar token de acesso ao servidor de email do Office 365. <b class='text-info'>DICAS: </b> você pode usar o botão no lado direito deste campo para fazer login na sua conta do Office 365 e gerar o token de atualização. Observe que sempre que o id do locatário, id do cliente, segredo do cliente ou nome principal do usuário for alterado, o token de atualização deve ser regenerado");
		m.put("Longest Duration First", "Maior Duração Primeiro");
		m.put("Looks like a GPG signature but without necessary data", "Parece uma assinatura GPG, mas sem os dados necessários");
		m.put("Low", "Baixo");
		m.put("Low Severity", "Baixa Severidade");
		m.put("MERGED", "FUNDIDO");
		m.put("MS Teams Notifications", "Notificações do MS Teams");
		m.put("Mail", "Correio");
		m.put("Mail Connector", "Conector de Correio");
		m.put("Mail Connector Bean", "Bean do Conector de Correio");
		m.put("Mail Service", "Serviço de Email");
		m.put("Mail Service Test", "Teste do Serviço de Email");
		m.put("Mail service not configured", "Serviço de email não configurado");
		m.put("Mail service settings saved", "Configurações do serviço de email salvas");
		m.put("Make sure <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 or higher</a> is installed", 
			"Certifique-se de que <a href=\"https://openjdk.java.net\" target=\"_blank\">Java 11 ou superior</a> está instalado");
		m.put("Make sure current user has permission to run docker containers", "Certifique-se de que o usuário atual tem permissão para executar contêineres docker");
		m.put("Make sure docker engine is installed and docker command line is available in system path", 
			"Certifique-se de que o motor docker está instalado e a linha de comando docker está disponível no caminho do sistema");
		m.put("Make sure git version 2.11.1 or higher is installed and available in system path", "Certifique-se de que a versão do git 2.11.1 ou superior está instalada e disponível no caminho do sistema");
		m.put("Make sure git-lfs is installed and available in system path if you want to retrieve LFS files", 
			"Certifique-se de que o git-lfs está instalado e disponível no caminho do sistema se você quiser recuperar arquivos LFS");
		m.put("Make sure the access token has package read permission over the project", "Certifique-se de que o token de acesso tem permissão de leitura de pacote sobre o projeto");
		m.put("Make sure the access token has package write permission over the project", "Certifique-se de que o token de acesso tem permissão de escrita de pacote sobre o projeto");
		m.put("Make sure the access token has package write permission over the project. Also make sure to run command <code>chmod 0600 $HOME/.gem/credentials</code> after creating the file", 
			"Certifique-se de que o token de acesso tem permissão de escrita de pacote sobre o projeto. Além disso, certifique-se de executar o comando <code>chmod 0600 $HOME/.gem/credentials</code> após criar o arquivo");
		m.put("Make sure the account has package ${permission} permission over the project", "Certifique-se de que a conta tem permissão de pacote ${permission} sobre o projeto");
		m.put("Make sure the account has package read permission over the project", "Certifique-se de que a conta tem permissão de leitura de pacote sobre o projeto");
		m.put("Make sure the user has package write permission over the project", "Certifique-se de que o usuário tem permissão de escrita de pacote sobre o projeto");
		m.put("Malformed %sbase query", "Consulta %sbase malformada");
		m.put("Malformed %squery", "Consulta %s malformada");
		m.put("Malformed build spec (import project: {0}, import revision: {1})", "Especificação de build malformada (importar projeto: {0}, importar revisão: {1})");
		m.put("Malformed email address", "Endereço de email malformado");
		m.put("Malformed filter", "Filtro malformado");
		m.put("Malformed name filter", "Filtro de nome malformado");
		m.put("Malformed query", "Consulta malformada");
		m.put("Malformed ssh signature", "Assinatura ssh malformada");
		m.put("Malformed test suite filter", "Filtro de suíte de teste malformado");
		m.put("Manage Job", "Gerenciar Trabalho");
		m.put("Manager DN", "DN do Gerente");
		m.put("Manager Password", "Senha do Gerente");
		m.put("Manifest blob unknown", "Blob de manifesto desconhecido");
		m.put("Manifest invalid", "Manifesto inválido");
		m.put("Manifest unknown", "Manifesto desconhecido");
		m.put("Many commands print outputs with ANSI colors in TTY mode to help identifying problems easily. However some commands running in this mode may wait for user input to cause build hanging. This can normally be fixed by adding extra options to the command", 
			"Muitos comandos imprimem saídas com cores ANSI no modo TTY para ajudar a identificar problemas facilmente. No entanto, alguns comandos executados neste modo podem aguardar entrada do usuário, causando travamento na construção. Isso normalmente pode ser corrigido adicionando opções extras ao comando");
		m.put("Mark a property archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived properties will not be shown by default", 
			"Marque uma propriedade como arquivada se ela não for mais usada pela especificação de build atual, mas ainda precisar existir para reproduzir builds antigos. Propriedades arquivadas não serão exibidas por padrão");
		m.put("Mark a secret archived if it is no longer used by current build spec, but still need to exist to reproduce old builds. Archived secrets will not be shown by default", 
			"Marque um segredo como arquivado se ele não for mais usado pela especificação de build atual, mas ainda precisar existir para reproduzir builds antigos. Segredos arquivados não serão exibidos por padrão");
		m.put("Markdown", "Markdown");
		m.put("Markdown Report", "Relatório Markdown");
		m.put("Markdown from file", "Markdown de arquivo");
		m.put("Maven(s)", "Maven(s)");
		m.put("Max Code Search Entries", "Máximo de Entradas de Pesquisa de Código");
		m.put("Max Commit Message Line Length", "Comprimento Máximo da Linha da Mensagem de Commit");
		m.put("Max Git LFS File Size (MB)", "Tamanho Máximo de Arquivo Git LFS (MB)");
		m.put("Max Retries", "Máximo de Tentativas");
		m.put("Max Upload File Size (MB)", "Tamanho Máximo de Arquivo para Upload (MB)");
		m.put("Max Value", "Valor Máximo");
		m.put("Maximum number of entries to return when search code in repository", "Número máximo de entradas a retornar ao pesquisar código no repositório");
		m.put("Maximum of retries before giving up", "Máximo de tentativas antes de desistir");
		m.put("May not be empty", "Não pode estar vazio");
		m.put("Medium", "Médio");
		m.put("Medium Severity", "Severidade Média");
		m.put("Members", "Membros");
		m.put("Memory", "Memória");
		m.put("Memory Limit", "Limite de Memória");
		m.put("Memory Request", "Requisição de Memória");
		m.put("Mention Someone", "Mencionar Alguém");
		m.put("Mention someone", "Mencionar alguém");
		m.put("Merge", "Mesclar");
		m.put("Merge Strategy", "Estratégia de Mesclagem");
		m.put("Merge Target Branch into Source Branch", "Mesclar o Branch de Destino no Branch de Origem");
		m.put("Merge branch \"{0}\" into branch \"{1}\"", "Mesclar o branch \"{0}\" no branch \"{1}\"");
		m.put("Merge branch \"{0}\" of project \"{1}\" into branch \"{2}\"", "Mesclar o branch \"{0}\" do projeto \"{1}\" no branch \"{2}\"");
		m.put("Merge preview not calculated yet", "Pré-visualização de mesclagem ainda não calculada");
		m.put("Merged", "Mesclado");
		m.put("Merged pull request \"{0}\" ({1})", "Pull request mesclado \"{0}\" ({1})");
		m.put("Merges pull request", "Mescla pull request");
		m.put("Meta", "Meta");
		m.put("Meta Info", "Informações Meta");
		m.put("Microsoft 365", "Microsoft 365");
		m.put("Microsoft Entra ID", "Microsoft Entra ID");
		m.put("Min Value", "Valor Mínimo");
		m.put("Minimum length of the password", "Comprimento mínimo da senha");
		m.put("Missing Commit", "Commit Ausente");
		m.put("Missing Commits", "Commits Ausentes");
		m.put("Month", "Mês");
		m.put("Months", "Meses");
		m.put("Months to Display", "Meses para Exibir");
		m.put("More", "Mais");
		m.put("More Options", "Mais Opções");
		m.put("More Settings", "Mais Configurações");
		m.put("More commits", "Mais commits");
		m.put("More info", "Mais informações");
		m.put("More operations", "Mais operações");
		m.put("Most branch coverage", "Maior cobertura de branch");
		m.put("Most line coverage", "Maior cobertura de linha");
		m.put("Most probably there are import errors in the <a wicket:id=\"buildSpec\">build spec</a>", 
			"Provavelmente há erros de importação na <a wicket:id=\"buildSpec\">especificação de build</a>");
		m.put("Mount Docker Sock", "Montar Docker Sock");
		m.put("Move All Queried Issues To...", "Mover Todas as Issues Consultadas Para...");
		m.put("Move All Queried Projects To...", "Mover Todos os Projetos Consultados Para...");
		m.put("Move Selected Issues To...", "Mover Issues Selecionadas Para...");
		m.put("Move Selected Projects To...", "Mover Projetos Selecionados Para...");
		m.put("Multiple Lines", "Linhas Múltiplas");
		m.put("Multiple On the Other Side", "Múltiplos do Outro Lado");
		m.put("Must not be empty", "Não deve estar vazio");
		m.put("My Access Tokens", "Meus Tokens de Acesso");
		m.put("My Basic Settings", "Minhas Configurações Básicas");
		m.put("My Email Addresses", "Meus Endereços de Email");
		m.put("My GPG Keys", "Minhas Chaves GPG");
		m.put("My Profile", "Meu Perfil");
		m.put("My SSH Keys", "Minhas Chaves SSH");
		m.put("My SSO Accounts", "Minhas Contas SSO");
		m.put("Mypy Report", "Relatório Mypy");
		m.put("N/A", "N/A");
		m.put("NPM(s)", "NPM(s)");
		m.put("Name", "Nome");
		m.put("Name Of Empty Value", "Nome do Valor Vazio");
		m.put("Name On the Other Side", "Nome do Outro Lado");
		m.put("Name Prefix", "Prefixo do Nome");
		m.put("Name already used by another access token of the owner", "Nome já usado por outro token de acesso do proprietário");
		m.put("Name already used by another link", "Nome já usado por outro link");
		m.put("Name and name on the other side should be different", "Nome e nome do outro lado devem ser diferentes");
		m.put("Name containing spaces or starting with dash needs to be quoted", "Nome contendo espaços ou começando com traço precisa ser citado");
		m.put("Name invalid", "Nome inválido");
		m.put("Name of the link", "Nome do link");
		m.put("Name of the link on the other side. For instance if name is <tt>sub issues</tt>, name on the other side can be <tt>parent issue</tt>", 
			"Nome do link do outro lado. Por exemplo, se o nome for <tt>sub issues</tt>, o nome do outro lado pode ser <tt>parent issue</tt>");
		m.put("Name of the provider will serve two purpose: <ul><li>Display on login button<li>Form the authorization callback url which will be <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>", 
			"O nome do provedor terá dois propósitos: <ul><li>Exibir no botão de login<li>Formar a URL de callback de autorização que será <i>&lt;server url&gt;/~sso/callback/&lt;name&gt;</i></ul>");
		m.put("Name reversely", "Nome inversamente");
		m.put("Name unknown", "Nome desconhecido");
		m.put("Name your file", "Nomeie seu arquivo");
		m.put("Named Agent Queries Bean", "Bean de Consultas de Agente Nomeado");
		m.put("Named Agent Query", "Consulta de Agente Nomeado");
		m.put("Named Build Queries Bean", "Bean de Consultas de Build Nomeado");
		m.put("Named Build Query", "Consulta de Build Nomeado");
		m.put("Named Code Comment Queries Bean", "Bean de Consultas de Comentário de Código Nomeado");
		m.put("Named Code Comment Query", "Consulta de Comentário de Código Nomeado");
		m.put("Named Commit Queries Bean", "Bean de Consultas de Commit Nomeado");
		m.put("Named Commit Query", "Consulta de Commit Nomeado");
		m.put("Named Element", "Elemento Nomeado");
		m.put("Named Issue Queries Bean", "Bean de Consultas de Issue Nomeado");
		m.put("Named Issue Query", "Consulta de Issue Nomeado");
		m.put("Named Pack Queries Bean", "Bean de Consultas de Pacote Nomeado");
		m.put("Named Pack Query", "Consulta de Pacote Nomeado");
		m.put("Named Project Queries Bean", "Bean de Consultas de Projeto Nomeado");
		m.put("Named Project Query", "Consulta de Projeto Nomeado");
		m.put("Named Pull Request Queries Bean", "Bean de Consultas de Pull Request Nomeado");
		m.put("Named Pull Request Query", "Consulta de Pull Request Nomeado");
		m.put("Named Query", "Consulta Nomeada");
		m.put("Network Options", "Opções de Rede");
		m.put("Never", "Nunca");
		m.put("Never expire", "Nunca expirar");
		m.put("New Board", "Novo Quadro");
		m.put("New Invitation Bean", "Bean de Convite Novo");
		m.put("New Issue", "Nova Issue");
		m.put("New Password", "Nova Senha");
		m.put("New State", "Novo Estado");
		m.put("New User Bean", "Bean de Usuário Novo");
		m.put("New Value", "Novo Valor");
		m.put("New issue board created", "Novo quadro de issues criado");
		m.put("New project created", "Novo projeto criado");
		m.put("New user created", "Novo usuário criado");
		m.put("New version available. Red for security/critical update, yellow for bug fix, blue for feature update. Click to show changes. Disable in system setting", 
			"Nova versão disponível. Vermelho para atualização de segurança/crítica, amarelo para correção de bugs, azul para atualização de recursos. Clique para mostrar alterações. Desative nas configurações do sistema");
		m.put("Next", "Próximo");
		m.put("Next commit", "Próximo commit");
		m.put("Next {0}", "Próximo {0}");
		m.put("No", "Não");
		m.put("No Activity Days", "Sem dias de atividade");
		m.put("No SSH keys configured in your account. You may <a wicket:id=\"sshKeys\" class=\"link-primary\">add a key</a> or switch to <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a> url", 
			"Nenhuma chave SSH configurada na sua conta. Você pode <a wicket:id=\"sshKeys\" class=\"link-primary\">adicionar uma chave</a> ou alternar para a url <a wicket:id=\"useHttp\" class=\"link-primary\">HTTP(S)</a>");
		m.put("No SSL", "Sem SSL");
		m.put("No accessible reports", "Sem relatórios acessíveis");
		m.put("No activity for some time", "Sem atividade por algum tempo");
		m.put("No agents to pause", "Sem agentes para pausar");
		m.put("No agents to remove", "Sem agentes para remover");
		m.put("No agents to restart", "Sem agentes para reiniciar");
		m.put("No agents to resume", "Sem agentes para retomar");
		m.put("No aggregation", "Sem agregação");
		m.put("No any", "Nenhum");
		m.put("No any matches", "Nenhuma correspondência");
		m.put("No applicable transitions or no permission to transit", "Sem transições aplicáveis ou sem permissão para transitar");
		m.put("No attributes defined (can only be edited when agent is online)", "Sem atributos definidos (só podem ser editados quando o agente está online)");
		m.put("No audits", "Sem auditorias");
		m.put("No authorized job secret found (project: {0}, job secret: {1})", "Nenhum segredo de trabalho autorizado encontrado (projeto: {0}, segredo de trabalho: {1})");
		m.put("No branch to cherry-pick to", "Sem branch para cherry-pick");
		m.put("No branch to revert on", "Sem branch para reverter");
		m.put("No branches Found", "Nenhum branch encontrado");
		m.put("No branches found", "Nenhum branch encontrado");
		m.put("No build in query context", "Sem build no contexto de consulta");
		m.put("No builds", "Sem builds");
		m.put("No builds to cancel", "Sem builds para cancelar");
		m.put("No builds to delete", "Sem builds para excluir");
		m.put("No builds to re-run", "Sem builds para reexecutar");
		m.put("No comment", "Sem comentário");
		m.put("No comments to delete", "Sem comentários para excluir");
		m.put("No comments to set as read", "Sem comentários para marcar como lidos");
		m.put("No comments to set resolved", "Sem comentários para marcar como resolvidos");
		m.put("No comments to set unresolved", "Sem comentários para marcar como não resolvidos");
		m.put("No commit in query context", "Sem commit no contexto de consulta");
		m.put("No config file", "Sem arquivo de configuração");
		m.put("No current build in query context", "Sem build atual no contexto de consulta");
		m.put("No current commit in query context", "Sem commit atual no contexto de consulta");
		m.put("No current pull request in query context", "Sem pull request atual no contexto de consulta");
		m.put("No data", "Sem dados");
		m.put("No default branch", "Sem branch padrão");
		m.put("No default group", "Sem grupo padrão");
		m.put("No default roles", "Sem funções padrão");
		m.put("No default value", "Sem valor padrão");
		m.put("No description", "Sem descrição");
		m.put("No diffs", "Sem diferenças");
		m.put("No diffs to navigate", "Sem diferenças para navegar");
		m.put("No directories to skip", "Sem diretórios para ignorar");
		m.put("No disallowed file types", "Nenhum tipo de arquivo não permitido");
		m.put("No executors defined. Jobs will use auto-discovered executors instead", "Nenhum executor definido. Os trabalhos usarão executores descobertos automaticamente");
		m.put("No external password authenticator", "Nenhum autenticador de senha externa");
		m.put("No external password authenticator to authenticate user \"{0}\"", "Nenhum autenticador de senha externa para autenticar o usuário \"{0}\"");
		m.put("No fields to prompt", "Sem campos para solicitar");
		m.put("No fields to remove", "Sem campos para remover");
		m.put("No file attachments", "Sem anexos de arquivo");
		m.put("No group by", "Sem agrupamento");
		m.put("No groups claim returned", "Sem reivindicação de grupos retornada");
		m.put("No groups to remove from", "Sem grupos para remover");
		m.put("No ignore file", "Sem arquivo de ignorar");
		m.put("No ignored licenses", "Sem licenças ignoradas");
		m.put("No image attachments", "Sem anexos de imagem");
		m.put("No imports defined", "Sem importações definidas");
		m.put("No issue boards defined", "Sem quadros de problemas definidos");
		m.put("No issues in iteration", "Sem problemas na iteração");
		m.put("No issues to copy", "Sem problemas para copiar");
		m.put("No issues to delete", "Sem problemas para excluir");
		m.put("No issues to edit", "Sem problemas para editar");
		m.put("No issues to export", "Sem problemas para exportar");
		m.put("No issues to move", "Sem problemas para mover");
		m.put("No issues to set as read", "Sem problemas para marcar como lidos");
		m.put("No issues to sync estimated/spent time", "Sem problemas para sincronizar tempo estimado/gasto");
		m.put("No issues to watch/unwatch", "Sem problemas para observar/desobservar");
		m.put("No jobs defined", "Sem trabalhos definidos");
		m.put("No jobs found", "Nenhum job encontrado");
		m.put("No limit", "Sem limite");
		m.put("No mail service", "Sem serviço de e-mail");
		m.put("No obvious changes", "Sem mudanças óbvias");
		m.put("No one", "Ninguém");
		m.put("No packages to delete", "Sem pacotes para excluir");
		m.put("No parent", "Sem pai");
		m.put("No previous successful build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a> to calculate fixed issues since", 
			"Sem build anterior bem-sucedido no <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">mesmo stream</a> para calcular problemas corrigidos desde então");
		m.put("No projects found", "Sem projetos encontrados");
		m.put("No projects to delete", "Sem projetos para excluir");
		m.put("No projects to modify", "Sem projetos para modificar");
		m.put("No projects to move", "Sem projetos para mover");
		m.put("No properties defined", "Sem propriedades definidas");
		m.put("No proxy", "Sem proxy");
		m.put("No pull request in query context", "Sem pull request no contexto de consulta");
		m.put("No pull requests to delete", "Sem pull requests para excluir");
		m.put("No pull requests to discard", "Sem pull requests para descartar");
		m.put("No pull requests to set as read", "Sem pull requests para marcar como lidos");
		m.put("No pull requests to watch/unwatch", "Sem pull requests para observar/desobservar");
		m.put("No refs to build on behalf of", "Sem refs para build em nome de");
		m.put("No required services", "Sem serviços necessários");
		m.put("No response body", "Sem corpo de resposta");
		m.put("No secret config", "Sem configuração secreta");
		m.put("No services defined", "Sem serviços definidos");
		m.put("No start/due date", "Sem data de início/vencimento");
		m.put("No step templates defined", "Sem modelos de etapa definidos");
		m.put("No suggestions", "Sem sugestões");
		m.put("No tags found", "Sem tags encontradas");
		m.put("No timesheets defined", "Sem folhas de ponto definidas");
		m.put("No user found with login name or email: ", "Nenhum usuário encontrado com nome de login ou email:");
		m.put("No users to convert to service accounts", "Não há usuários para converter para contas de serviço");
		m.put("No users to delete", "Sem usuários para excluir");
		m.put("No users to disable", "Sem usuários para desativar");
		m.put("No users to enable", "Sem usuários para ativar");
		m.put("No users to remove from group", "Sem usuários para remover do grupo");
		m.put("No valid query to show progress", "Nenhuma consulta válida para mostrar progresso");
		m.put("No valid signature for head commit", "Nenhuma assinatura válida para o commit principal");
		m.put("No valid signature for head commit of target branch", "Nenhuma assinatura válida para o commit principal da branch de destino");
		m.put("No value", "Nenhum valor");
		m.put("No verified primary email address", "Nenhum endereço de email primário verificado");
		m.put("Node Selector", "Seletor de Nó");
		m.put("Node Selector Entry", "Entrada do Seletor de Nó");
		m.put("None", "Nenhum");
		m.put("Not Active Since", "Não ativo desde");
		m.put("Not Used Since", "Não usado desde");
		m.put("Not a verified email of signing GPG key", "Email não verificado da chave GPG de assinatura");
		m.put("Not a verified email of signing ssh key owner", "Não é um email verificado do proprietário da chave ssh de assinatura");
		m.put("Not allowed file type: {0}", "Tipo de arquivo não permitido: {0}");
		m.put("Not assigned", "Não atribuído");
		m.put("Not authorized to create project under \"{0}\"", "Não autorizado a criar projeto em \"{0}\"");
		m.put("Not authorized to create root project", "Não autorizado a criar projeto raiz");
		m.put("Not authorized to move project under this parent", "Não autorizado a mover o projeto para este pai");
		m.put("Not authorized to set as root project", "Não autorizado a definir como projeto raiz");
		m.put("Not covered", "Não coberto");
		m.put("Not covered by any test", "Não coberto por nenhum teste");
		m.put("Not displaying any fields", "Não exibindo nenhum campo");
		m.put("Not displaying any links", "Não exibindo nenhum link");
		m.put("Not passed", "Não aprovado");
		m.put("Not rendered in failsafe mode", "Não renderizado no modo de segurança");
		m.put("Not run", "Não executado");
		m.put("Not specified", "Não especificado");
		m.put("Note", "Nota");
		m.put("Nothing to preview", "Nada para visualizar");
		m.put("Notification", "Notificação");
		m.put("Notifications", "Notificações");
		m.put("Notify Build Events", "Notificar Eventos de Build");
		m.put("Notify Code Comment Events", "Notificar Eventos de Comentário de Código");
		m.put("Notify Code Push Events", "Notificar Eventos de Push de Código");
		m.put("Notify Issue Events", "Notificar Eventos de Problemas");
		m.put("Notify Own Events", "Notificar Próprios Eventos");
		m.put("Notify Pull Request Events", "Notificar Eventos de Pull Request");
		m.put("Notify Users", "Notificar Usuários");
		m.put("Ntfy.sh Notifications", "Notificações Ntfy.sh");
		m.put("NuGet(s)", "NuGet(s)");
		m.put("NuSpec", "NuSpec");
		m.put("Number of CPU Cores", "Número de Núcleos de CPU");
		m.put("Number of SSH Keys", "Número de Chaves SSH");
		m.put("Number of builds to preserve", "Número de builds a preservar");
		m.put("Number of project replicas, including primary and backups", "Número de réplicas do projeto, incluindo primárias e backups");
		m.put("Number of recent months to show statistics for", "Número de meses recentes para mostrar estatísticas");
		m.put("OAuth2 Client information | CLIENT ID", "Informações do Cliente OAuth2 | CLIENT ID");
		m.put("OAuth2 Client information | CLIENT SECRET", "Informações do Cliente OAuth2 | CLIENT SECRET");
		m.put("OCI Layout Directory", "Diretório de Layout OCI");
		m.put("OIDC error: Inconsistent sub in ID token and userinfo", "Erro OIDC: Sub inconsistente no token ID e userinfo");
		m.put("OOPS! There Is An Error", "OOPS! Há Um Erro");
		m.put("OPEN", "ABERTO");
		m.put("OS", "Sistema Operacional");
		m.put("OS Arch", "Arquitetura do Sistema Operacional");
		m.put("OS User Name", "Nome do Usuário do Sistema Operacional");
		m.put("OS Version", "Versão do Sistema Operacional");
		m.put("OS/ARCH", "SO/ARQUITETURA");
		m.put("Offline", "Offline");
		m.put("Ok", "Ok");
		m.put("Old Name", "Nome Antigo");
		m.put("Old Password", "Senha Antiga");
		m.put("On Behalf Of", "Em Nome de");
		m.put("On Branches", "Nas Branches");
		m.put("OneDev Issue Field", "Campo de Problema do OneDev");
		m.put("OneDev Issue Link", "Link de Problema do OneDev");
		m.put("OneDev Issue State", "Estado de Problema do OneDev");
		m.put("OneDev analyzes repository files for code search, line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The repository will be re-analyzed when server is started", 
			"O OneDev analisa arquivos do repositório para busca de código, estatísticas de linha e estatísticas de contribuição de código. Esta configuração indica quais arquivos devem ser analisados e espera <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>padrões de caminho</a> separados por espaço. Um padrão pode ser excluído prefixando com '-', por exemplo <code>-**/vendors/**</code> excluirá todos os arquivos com vendors no caminho. <b>NOTA: </b> Alterar esta configuração afeta apenas novos commits. Para aplicar a alteração a commits históricos, pare o servidor e exclua a pasta <code>index</code> e <code>info/commit</code> no <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>diretório de armazenamento do projeto</a>. O repositório será reanalisado quando o servidor for iniciado");
		m.put("OneDev configures git hooks to communicate with itself via curl", "O OneDev configura hooks do git para se comunicar consigo mesmo via curl");
		m.put("OneDev needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated", 
			"O OneDev precisa buscar e determinar o DN do usuário, bem como buscar informações do grupo do usuário se a recuperação de grupo estiver habilitada. Marque esta opção e especifique o DN e senha do 'gerente' se essas operações precisarem ser autenticadas");
		m.put("OneDev requires git command line to manage repositories. The minimum required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve LFS files in build job", 
			"O OneDev requer a linha de comando do git para gerenciar repositórios. A versão mínima necessária é 2.11.1. Certifique-se também de que o git-lfs esteja instalado se você quiser recuperar arquivos LFS no trabalho de build");
		m.put("Online", "Online");
		m.put("Only create merge commit if target branch can not be fast-forwarded to source branch", 
			"Somente criar commit de merge se a branch de destino não puder ser avançada rapidamente para a branch de origem");
		m.put("Only projects manageable by access token owner can be authorized", "Somente projetos gerenciáveis pelo proprietário do token de acesso podem ser autorizados");
		m.put("Only system level audit events are displayed here. To view audit events for a specific project, please visit the project audit log page", 
			"Somente eventos de auditoria de nível de sistema são exibidos aqui. Para visualizar eventos de auditoria de um projeto específico, visite a página de log de auditoria do projeto");
		m.put("Only users able to authenticate via password can be linked", "Apenas usuários capazes de autenticar via senha podem ser vinculados");
		m.put("Open", "Abrir");
		m.put("Open new pull request", "Abrir nova pull request");
		m.put("Open terminal of current running step", "Abrir terminal do passo em execução atual");
		m.put("OpenID", "OpenID");
		m.put("OpenID client identification will be assigned by your OpenID provider when registering this OneDev instance as client application", 
			"A identificação do cliente OpenID será atribuída pelo seu provedor OpenID ao registrar esta instância do OneDev como aplicação cliente");
		m.put("OpenID client secret will be generated by your OpenID provider when registering this OneDev instance as client application", 
			"O segredo do cliente OpenID será gerado pelo seu provedor OpenID ao registrar esta instância do OneDev como aplicação cliente");
		m.put("OpenSSH Public Key", "Chave Pública OpenSSH");
		m.put("OpenSSH public key begins with 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'", 
			"A chave pública OpenSSH começa com 'ssh-rsa', 'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', 'sk-ecdsa-sha2-nistp256@openssh.com' ou 'sk-ssh-ed25519@openssh.com'");
		m.put("Opened issue \"{0}\" ({1})", "Problema aberto \"{0}\" ({1})");
		m.put("Opened pull request \"{0}\" ({1})", "Pull request aberta \"{0}\" ({1})");
		m.put("Operation", "Operação");
		m.put("Operation Failed", "Falha na Operação");
		m.put("Operation Successful", "Operação Bem-Sucedida");
		m.put("Operations", "Operações");
		m.put("Optional", "Opcional");
		m.put("Optionally Specify project to create issue in. Leave empty to create in current project", 
			"Opcionalmente especifique o projeto para criar o problema. Deixe vazio para criar no projeto atual");
		m.put("Optionally add new users to specified default group", "Opcionalmente adicione novos usuários ao grupo padrão especificado");
		m.put("Optionally add newly authenticated user to specified group if membership information is not available", 
			"Opcionalmente adicione o usuário autenticado ao grupo especificado se as informações de associação não estiverem disponíveis");
		m.put("Optionally add newly authenticated user to specified group if membership information is not retrieved", 
			"Opcionalmente adicione o usuário autenticado ao grupo especificado se as informações de associação não forem recuperadas");
		m.put("Optionally choose required builds. You may also input jobs not listed here, and press ENTER to add them", 
			"Opcionalmente escolha builds necessários. Você também pode inserir trabalhos não listados aqui e pressionar ENTER para adicioná-los");
		m.put("Optionally configure proxy to access remote repository. Proxy should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;", 
			"Opcionalmente configure o proxy para acessar o repositório remoto. O proxy deve estar no formato &lt;proxy host&gt;:&lt;proxy port&gt;");
		m.put("Optionally define a unique key for the project with two or more upper case letters. This key can be used to reference issues, builds, and pull requests with a stable and short form <code>&lt;project key&gt;-&lt;number&gt;</code> instead of <code>&lt;project path&gt;#&lt;number&gt;</code>", 
			"Opcionalmente defina uma chave única para o projeto com duas ou mais letras maiúsculas. Esta chave pode ser usada para referenciar problemas, builds e pull requests com uma forma estável e curta <code>&lt;project key&gt;-&lt;number&gt;</code> em vez de <code>&lt;project path&gt;#&lt;number&gt;</code>");
		m.put("Optionally define parameter specifications of the job", "Opcionalmente defina especificações de parâmetros do trabalho");
		m.put("Optionally define parameter specifications of the step template", "Opcionalmente defina especificações de parâmetros do modelo de etapa");
		m.put("Optionally describe the group", "Opcionalmente descreva o grupo");
		m.put("Optionally describes the custom field. Html tags are accepted", "Opcionalmente descreve o campo personalizado. Tags HTML são aceitas");
		m.put("Optionally describes the param. Html tags are accepted.", "Opcionalmente descreve o parâmetro. Tags HTML são aceitas.");
		m.put("Optionally filter builds", "Opcionalmente filtre builds");
		m.put("Optionally filter issues", "Opcionalmente filtre problemas");
		m.put("Optionally filter pull requests", "Opcionalmente filtre pull requests");
		m.put("Optionally leave a note", "Opcionalmente deixe uma nota");
		m.put("Optionally mount directories or files under job workspace into container", "Opcionalmente monte diretórios ou arquivos no espaço de trabalho do trabalho dentro do contêiner");
		m.put("Optionally select fields to prompt when this button is pressed", "Opcionalmente selecione campos para solicitar quando este botão for pressionado");
		m.put("Optionally select fields to remove when this transition happens", "Opcionalmente selecione campos para remover quando esta transição ocorrer");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user email. This field is normally set to <i>mail</i> according to RFC 2798", 
			"Opcionalmente especifica o nome do atributo dentro da entrada LDAP do usuário cujo valor será usado como email do usuário. Este campo normalmente é definido como <i>mail</i> de acordo com o RFC 2798");
		m.put("Optionally specifies name of the attribute inside the user LDAP entry whose value will be taken as user full name. This field is normally set to <i>displayName</i> according to RFC 2798. If left empty, full name of the user will not be retrieved", 
			"Opcionalmente especifica o nome do atributo dentro da entrada LDAP do usuário cujo valor será usado como nome completo do usuário. Este campo normalmente é definido como <i>displayName</i> de acordo com o RFC 2798. Se deixado vazio, o nome completo do usuário não será recuperado");
		m.put("Optionally specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as GitHub access token. This is used to retrieve release notes of dependencies hosted on GitHub, and the authenticated access will get a higher rate limit", 
			"Opcionalmente especifique <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do trabalho</a> para ser usado como token de acesso ao GitHub. Isso é usado para recuperar notas de lançamento de dependências hospedadas no GitHub, e o acesso autenticado terá um limite de taxa mais alto");
		m.put("Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko", 
			"Opcionalmente especifique <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>opções adicionais</a> do kaniko");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>additional options</a> of crane", 
			"Opcionalmente especifique <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_pull.md' target='_blank'>opções adicionais</a> do crane");
		m.put("Optionally specify <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>additional options</a> of crane", 
			"Opcionalmente especifique <a href='https://github.com/google/go-containerregistry/blob/main/cmd/crane/doc/crane_push.md' target='_blank'>opções adicionais</a> do crane");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to build for platform of the node running the job", 
			"Opcionalmente especifique <span class='text-info'>plataformas separadas por vírgula</span> para construir, por exemplo <tt>linux/amd64,linux/arm64</tt>. Deixe vazio para construir para a plataforma do nó que executa o trabalho");
		m.put("Optionally specify <span class='text-info'>comma separated</span> platforms to scan, for instance <tt>linux/amd64,linux/arm64</tt>. Leave empty to scan all platforms in OCI layout", 
			"Opcionalmente especifique <span class='text-info'>plataformas separadas por vírgula</span> para escanear, por exemplo <tt>linux/amd64,linux/arm64</tt>. Deixe vazio para escanear todas as plataformas no layout OCI");
		m.put("Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use file <tt>Dockerfile</tt> under build path specified above", 
			"Opcionalmente especifique o Dockerfile relativo ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>. Deixe vazio para usar o arquivo <tt>Dockerfile</tt> no caminho de construção especificado acima");
		m.put("Optionally specify JavaScript config to be used by Renovate CLI", "Opcionalmente especifique a configuração JavaScript a ser usada pelo Renovate CLI");
		m.put("Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. Leave empty to derive from server url", 
			"Opcionalmente especifique a URL raiz SSH, que será usada para construir a URL de clonagem do projeto via protocolo SSH. Deixe vazio para derivar da URL do servidor");
		m.put("Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of the text input", 
			"Opcionalmente especifique um <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>padrão de expressão regular</a> para valores válidos da entrada de texto");
		m.put("Optionally specify a OneDev project to be used as parent of imported projects. Leave empty to import as root projects", 
			"Opcionalmente especifique um projeto OneDev para ser usado como pai dos projetos importados. Deixe vazio para importar como projetos raiz");
		m.put("Optionally specify a OneDev project to be used as parent of imported repositories. Leave empty to import as root projects", 
			"Opcionalmente especifique um projeto OneDev para ser usado como pai dos repositórios importados. Deixe vazio para importar como projetos raiz");
		m.put("Optionally specify a base query for the list", "Opcionalmente especifique uma consulta base para a lista");
		m.put("Optionally specify a base query to filter/order issues in backlog. Backlog issues are those not associating with current iteration", 
			"Opcionalmente especifique uma consulta base para filtrar/ordenar problemas no backlog. Problemas no backlog são aqueles que não estão associados à iteração atual");
		m.put("Optionally specify a base query to filter/order issues of the board", "Opcionalmente especifique uma consulta base para filtrar/ordenar problemas do quadro");
		m.put("Optionally specify a cron expression to schedule database auto-backup. The cron expression format is <em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>.For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>Quartz tutorial</a>.The backup files will be placed into <em>db-backup</em> folder under OneDev installation directory. In case multiple servers connect to form a cluster, auto-backup takes place on the <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>lead server</a>. Leave this property empty if you do not want to enable database auto backup.", 
			"Opcionalmente especifique uma expressão cron para agendar backup automático do banco de dados. O formato da expressão cron é <em>&lt;segundos&gt; &lt;minutos&gt; &lt;horas&gt; &lt;dia-do-mês&gt; &lt;mês&gt; &lt;dia-da-semana&gt;</em>. Por exemplo, <em>0 0 1 * * ?</em> significa 1:00 da manhã todos os dias. Para detalhes do formato, consulte o <a href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format' target='_blank'>tutorial do Quartz</a>. Os arquivos de backup serão colocados na pasta <em>db-backup</em> no diretório de instalação do OneDev. Caso vários servidores se conectem para formar um cluster, o backup automático ocorrerá no <a href='https://docs.onedev.io/concepts#lead-server' target='_blank'>servidor líder</a>. Deixe esta propriedade vazia se você não quiser habilitar o backup automático do banco de dados.");
		m.put("Optionally specify a date field to hold due date information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique um campo de data para armazenar informações de data de vencimento.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to put retrieved artifacts. Leave empty to use job workspace itself", 
			"Opcionalmente especifique um caminho relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> para colocar artefatos recuperados. Deixe vazio para usar o próprio espaço de trabalho do trabalho");
		m.put("Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. <b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, as the volume is only used to hold temporary build files", 
			"Opcionalmente especifique uma classe de armazenamento para alocar dinamicamente o volume de construção. Deixe vazio para usar a classe de armazenamento padrão. <b class='text-warning'>NOTA:</b> A política de recuperação da classe de armazenamento deve ser configurada como <code>Delete</code>, pois o volume é usado apenas para armazenar arquivos temporários de construção");
		m.put("Optionally specify a working period field to hold estimated time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique um campo de período de trabalho para armazenar informações de tempo estimado.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Optionally specify a working period field to hold spent time infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique um campo de período de trabalho para armazenar informações de tempo gasto.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Optionally specify a working period field to hold time estimate infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique um campo de período de trabalho para armazenar informações de estimativa de tempo.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Optionally specify a working period field to hold time spent infomration.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Opcionalmente especifique um campo de período de trabalho para armazenar informações de tempo gasto.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Optionally specify additional options for buildx build command", "Opcionalmente especifique opções adicionais para o comando de construção buildx");
		m.put("Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, the response header <code>Access-Control-Allow-Origin</code> will be set to the same value", 
			"Opcionalmente especifique origens <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> permitidas. Para uma solicitação simples ou prévia de CORS, se o valor do cabeçalho de solicitação <code>Origin</code> estiver incluído aqui, o cabeçalho de resposta <code>Access-Control-Allow-Origin</code> será configurado com o mesmo valor");
		m.put("Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match", 
			"Opcionalmente especifique o domínio de email permitido para usuários de autoinscrição. Use '*' ou '?' para correspondência de padrão");
		m.put("Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). Leave empty to all types", 
			"Opcionalmente especifique tipos de commit aplicáveis para verificação de rodapé de mensagem de commit (pressione ENTER para adicionar valor). Deixe vazio para todos os tipos");
		m.put("Optionally specify applicable jobs of this executor", "Opcionalmente, especifique os trabalhos aplicáveis deste executor");
		m.put("Optionally specify applicable users who pushed the change", "Opcionalmente especifique usuários aplicáveis que fizeram o push da alteração");
		m.put("Optionally specify arguments to run above image", "Opcionalmente especifique argumentos para executar a imagem acima");
		m.put("Optionally specify artifacts to retrieve from the dependency into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not retrieve any artifacts", 
			"Opcionalmente especifique artefatos para recuperar da dependência no <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a>. Apenas artefatos publicados (via etapa de publicação de artefatos) podem ser recuperados. Deixe vazio para não recuperar nenhum artefato");
		m.put("Optionally specify authorized roles to press this button. If not specified, all users are allowed", 
			"Opcionalmente especifique funções autorizadas para pressionar este botão. Se não especificado, todos os usuários são permitidos");
		m.put("Optionally specify base query of the list", "Opcionalmente especifique consulta base da lista");
		m.put("Optionally specify branches/users/groups allowed to access this secret. If left empty, any job can access this secret, including those triggered via external pull requests", 
			"Opcionalmente especifique branches/usuários/grupos permitidos para acessar este segredo. Se deixado vazio, qualquer trabalho pode acessar este segredo, incluindo aqueles acionados via pull requests externos");
		m.put("Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context directory, unless you specify a different location with option <code>--dockerfile</code>", 
			"Opcionalmente especifique o caminho do contexto de construção relativo ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>. Deixe vazio para usar o próprio espaço de trabalho do trabalho. O arquivo <code>Dockerfile</code> é esperado no diretório do contexto de construção, a menos que você especifique um local diferente com a opção <code>--dockerfile</code>");
		m.put("Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. Leave empty to use job workspace itself", 
			"Opcionalmente especifique o caminho de construção relativo ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>. Deixe vazio para usar o próprio espaço de trabalho do trabalho");
		m.put("Optionally specify cluster role the job pods service account binding to. This is necessary if you want to do things such as running other Kubernetes pods in job command", 
			"Opcionalmente especifique a função de cluster à qual a conta de serviço dos pods de trabalho está vinculada. Isso é necessário se você quiser executar outras pods do Kubernetes no comando do trabalho");
		m.put("Optionally specify comma separated licenses to be ignored", "Opcionalmente especifique licenças separadas por vírgula para serem ignoradas");
		m.put("Optionally specify container arguments separated by space. Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse this with container options which should be specified in executor setting", 
			"Opcionalmente especifique argumentos do contêiner separados por espaço. Argumento único contendo espaço deve ser citado. <b class='text-warning'>Nota: </b> não confunda isso com opções de contêiner que devem ser especificadas na configuração do executor");
		m.put("Optionally specify cpu limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Opcionalmente especifique o limite de CPU para cada trabalho/serviço usando este executor. Verifique <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>gerenciamento de recursos do Kubernetes</a> para detalhes");
		m.put("Optionally specify cpu limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> of relevant containers", 
			"Opcionalmente especifique o limite de CPU de cada trabalho/serviço usando este executor. Isso será usado como opção <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> dos contêineres relevantes");
		m.put("Optionally specify criteria of issues which can be linked", "Opcionalmente especifique critérios de problemas que podem ser vinculados");
		m.put("Optionally specify criteria of issues which can be linked on the other side", "Opcionalmente especifique critérios de problemas que podem ser vinculados do outro lado");
		m.put("Optionally specify custom fields allowed to edit when open new issues", "Opcionalmente especifique campos personalizados permitidos para editar ao abrir novos problemas");
		m.put("Optionally specify depth for a shallow clone in order to speed up source retrieval", 
			"Opcionalmente especifique profundidade para um clone superficial a fim de acelerar a recuperação de código-fonte");
		m.put("Optionally specify description of the issue", "Opcionalmente especifique a descrição do problema");
		m.put("Optionally specify directories or glob patterns inside scan path to skip. Multiple skips should be separated by space", 
			"Opcionalmente especifique diretórios ou padrões glob dentro do caminho de escaneamento para ignorar. Múltiplos ignorados devem ser separados por espaço");
		m.put("Optionally specify disallowed file types by extensions (hit ENTER to add value), for instance <code>exe</code>, <code>bin</code>. Leave empty to allow all file types", 
			"Opcionalmente, especifique tipos de arquivo não permitidos por extensões (pressione ENTER para adicionar valor), por exemplo, <code>exe</code>, <code>bin</code>. Deixe vazio para permitir todos os tipos de arquivo");
		m.put("Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. Leave empty to use docker executable in PATH", 
			"Opcionalmente especifique o executável do docker, por exemplo <i>/usr/local/bin/docker</i>. Deixe vazio para usar o executável do docker no PATH");
		m.put("Optionally specify docker options to create network. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Opcionalmente especifique opções do docker para criar rede. Múltiplas opções devem ser separadas por espaço, e opção única contendo espaços deve ser citada");
		m.put("Optionally specify docker options to run container. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Opcionalmente especifique opções do docker para executar contêiner. Múltiplas opções devem ser separadas por espaço, e opção única contendo espaços deve ser citada");
		m.put("Optionally specify docker sock to use. Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows", 
			"Opcionalmente especifique o socket do docker a ser usado. Padrão é <i>/var/run/docker.sock</i> no Linux, e <i>//./pipe/docker_engine</i> no Windows");
		m.put("Optionally specify environment variables for the container", "Opcionalmente especifique variáveis de ambiente para o contêiner");
		m.put("Optionally specify environment variables for this step", "Opcionalmente especifique variáveis de ambiente para esta etapa");
		m.put("Optionally specify environment variables of the service", "Opcionalmente especifique variáveis de ambiente do serviço");
		m.put("Optionally specify estimated time.", "Opcionalmente especifique tempo estimado.");
		m.put("Optionally specify executor for this job. Leave empty to use auto-discover executor", 
			"Opcionalmente, especifique o executor para este trabalho. Deixe vazio para usar o executor descoberto automaticamente");
		m.put("Optionally specify executor for this job. Leave empty to use first applicable executor", 
			"Opcionalmente, especifique o executor para este trabalho. Deixe vazio para usar o primeiro executor aplicável");
		m.put("Optionally specify files relative to cache path to ignore when detect cache changes. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Multiple files should be separated by space, and single file containing space should be quoted", 
			"Opcionalmente especifique arquivos relativos ao caminho de cache para ignorar ao detectar mudanças no cache. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Múltiplos arquivos devem ser separados por espaço, e arquivo único contendo espaço deve ser citado");
		m.put("Optionally specify group search base if you want to retrieve group membership information of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate permissions to a Active Directory group, a OneDev group with same name should be defined. Leave empty to manage group memberships at OneDev side", 
			"Opcionalmente especifique base de pesquisa de grupo se você quiser recuperar informações de associação de grupo do usuário. Por exemplo: <i>cn=Users, dc=example, dc=com</i>. Para dar permissões apropriadas a um grupo do Active Directory, um grupo do OneDev com o mesmo nome deve ser definido. Deixe vazio para gerenciar associações de grupo no lado do OneDev");
		m.put("Optionally specify issue links allowed to edit", "Opcionalmente especifique links de problemas permitidos para editar");
		m.put("Optionally specify issues applicable for this template. Leave empty for all", "Opcionalmente especifique problemas aplicáveis para este modelo. Deixe vazio para todos");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues", 
			"Opcionalmente especifique problemas aplicáveis para esta transição. Deixe vazio para todos os problemas");
		m.put("Optionally specify issues applicable for this transition. Leave empty for all issues. ", 
			"Opcionalmente especifique problemas aplicáveis para esta transição. Deixe vazio para todos os problemas.");
		m.put("Optionally specify jobs allowed to use this script", "Opcionalmente especifique trabalhos permitidos para usar este script");
		m.put("Optionally specify memory limit for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Opcionalmente especifique limite de memória para cada trabalho/serviço usando este executor. Verifique <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>gerenciamento de recursos do Kubernetes</a> para detalhes");
		m.put("Optionally specify memory limit of each job/service using this executor. This will be used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> of relevant containers", 
			"Opcionalmente especifique limite de memória de cada trabalho/serviço usando este executor. Isso será usado como opção <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> dos contêineres relevantes");
		m.put("Optionally specify merge strategy of created pull request. Leave empty to use default strategy of each project", 
			"Opcionalmente especifique estratégia de mesclagem do pull request criado. Deixe vazio para usar a estratégia padrão de cada projeto");
		m.put("Optionally specify message of the tag", "Opcionalmente especifique mensagem da tag");
		m.put("Optionally specify name of the attribute inside the user LDAP entry whose values will be taken as user SSH keys. SSH keys will be managed by LDAP only if this field is set", 
			"Opcionalmente especifique o nome do atributo dentro da entrada LDAP do usuário cujos valores serão usados como chaves SSH do usuário. Chaves SSH serão gerenciadas pelo LDAP apenas se este campo for configurado");
		m.put("Optionally specify node selector of the job pods", "Opcionalmente especifique o seletor de nó dos pods de trabalho");
		m.put("Optionally specify options for docker builder prune command", "Opcionalmente especifique opções para o comando docker builder prune");
		m.put("Optionally specify options for scp command. Multiple options need to be separated with space", 
			"Opcionalmente especifique opções para o comando scp. Múltiplas opções precisam ser separadas por espaço");
		m.put("Optionally specify options for ssh command. Multiple options need to be separated with space", 
			"Opcionalmente especifique opções para o comando ssh. Múltiplas opções precisam ser separadas por espaço");
		m.put("Optionally specify options passed to renovate cli. Multiple options should be separated by space, and single option containing spaces should be quoted", 
			"Opcionalmente especifique opções passadas para o renovate cli. Múltiplas opções devem ser separadas por espaço, e opção única contendo espaços deve ser citada");
		m.put("Optionally specify osv scanner <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. You may ignore particular vulnerabilities via this file", 
			"Opcionalmente especifique o arquivo de configuração do scanner osv <a href='https://google.github.io/osv-scanner/configuration/' target='_blank'>config file</a> no <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>. Você pode ignorar vulnerabilidades específicas através deste arquivo");
		m.put("Optionally specify path protection rules", "Opcionalmente especifique regras de proteção de caminho");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>", 
			"Opcionalmente especifique caminho relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> para ser usado como arquivo de ignorar do trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/configuration/filtering/#by-finding-ids' target='_blank'>ignore file</a>");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be used as trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>", 
			"Opcionalmente especifique caminho relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> para ser usado como configuração de segredo do trivy <a href='https://aquasecurity.github.io/trivy/v0.50/docs/scanner/secret/#configuration' target='_blank'>secret config</a>");
		m.put("Optionally specify path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to publish artifacts from. Leave empty to use job workspace itself", 
			"Opcionalmente especifique caminho relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> para publicar artefatos. Deixe vazio para usar o próprio espaço de trabalho do trabalho");
		m.put("Optionally specify platform to pull, for instance <tt>linux/amd64</tt>. Leave empty to pull all platforms in image", 
			"Opcionalmente especifique plataforma para puxar, por exemplo <tt>linux/amd64</tt>. Deixe vazio para puxar todas as plataformas na imagem");
		m.put("Optionally specify project to show builds of. Leave empty to show builds of all projects with permissions", 
			"Opcionalmente especifique projeto para mostrar construções. Deixe vazio para mostrar construções de todos os projetos com permissões");
		m.put("Optionally specify project to show issues of. Leave empty to show issues of all accessible projects", 
			"Opcionalmente especifique projeto para mostrar problemas. Deixe vazio para mostrar problemas de todos os projetos acessíveis");
		m.put("Optionally specify project to show packages of. Leave empty to show packages of all projects with permissions", 
			"Opcionalmente especifique projeto para mostrar pacotes. Deixe vazio para mostrar pacotes de todos os projetos com permissões");
		m.put("Optionally specify ref of above job, for instance <i>refs/heads/main</i>. Use * for wildcard match", 
			"Opcionalmente especifique referência do trabalho acima, por exemplo <i>refs/heads/main</i>. Use * para correspondência de curinga");
		m.put("Optionally specify registry logins to override those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token secret for password secret", 
			"Opcionalmente especifique logins de registro para substituir aqueles definidos no executor do trabalho. Para registro interno, use <code>@server_url@</code> para URL do registro, <code>@job_token@</code> para nome de usuário, e segredo de token de acesso para senha secreta");
		m.put("Optionally specify relative directory to put uploaded files", "Opcionalmente especifique diretório relativo para colocar arquivos enviados");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to clone code into. Leave empty to use job workspace itself", 
			"Opcionalmente especifique caminho relativo sob <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> para clonar código. Deixe vazio para usar o próprio espaço de trabalho do trabalho");
		m.put("Optionally specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan. Leave empty to use job workspace itself", 
			"Opcionalmente especifique caminho relativo sob <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> para escanear. Deixe vazio para usar o próprio espaço de trabalho do trabalho");
		m.put("Optionally specify relative paths under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to scan dependency vulnerabilities. Multiple paths can be specified and should be separated with space. Leave empty to use job workspace itself", 
			"Opcionalmente especifique caminhos relativos sob <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> para escanear vulnerabilidades de dependência. Múltiplos caminhos podem ser especificados e devem ser separados por espaço. Deixe vazio para usar o próprio espaço de trabalho do trabalho");
		m.put("Optionally specify required reviewers for changes of specified branch", "Opcionalmente especifique revisores necessários para mudanças no branch especificado");
		m.put("Optionally specify revision to create branch from. Leave empty to create from build commit", 
			"Opcionalmente especifique revisão para criar branch. Deixe vazio para criar a partir do commit de construção");
		m.put("Optionally specify separate directory to store build artifacts. Non-absolute directory is considered to be relative to site directory", 
			"Opcionalmente especifique diretório separado para armazenar artefatos de construção. Diretório não absoluto é considerado relativo ao diretório do site");
		m.put("Optionally specify separate directory to store git lfs files. Non-absolute directory is considered to be relative to site directory", 
			"Opcionalmente especifique diretório separado para armazenar arquivos git lfs. Diretório não absoluto é considerado relativo ao diretório do site");
		m.put("Optionally specify separate directory to store package files. Non-absolute directory is considered to be relative to site directory", 
			"Opcionalmente especifique diretório separado para armazenar arquivos de pacote. Diretório não absoluto é considerado relativo ao diretório do site");
		m.put("Optionally specify services required by this job. <b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors (server docker executor, remote docker executor, or kubernetes executor)", 
			"Opcionalmente especifique serviços necessários para este trabalho. <b class='text-warning'>NOTA:</b> Serviços são suportados apenas por executores compatíveis com docker (executor docker do servidor, executor docker remoto, ou executor kubernetes)");
		m.put("Optionally specify space-separated branches applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifique branches separados por espaço aplicáveis para esta transição. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos");
		m.put("Optionally specify space-separated branches applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for default branch", 
			"Opcionalmente especifique branches separados por espaço aplicáveis para este gatilho. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para o branch padrão");
		m.put("Optionally specify space-separated branches to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Opcionalmente especifique branches separados por espaço para verificar. Use '**' ou '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos os branches");
		m.put("Optionally specify space-separated commit messages applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente, especifique mensagens de commit separadas por espaço aplicáveis para esta transição. Use '*' ou '?' para correspondência curinga. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos");
		m.put("Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all files", 
			"Opcionalmente especifique arquivos separados por espaço para verificar. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos os arquivos");
		m.put("Optionally specify space-separated jobs applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifique trabalhos separados por espaço aplicáveis para esta transição. Use '*' ou '?' para correspondência de curinga. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos");
		m.put("Optionally specify space-separated projects applicable for this trigger. This is useful for instance when you want to prevent the job from being triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Opcionalmente especifique projetos separados por espaço aplicáveis para este gatilho. Isso é útil, por exemplo, quando você deseja impedir que o trabalho seja acionado em projetos bifurcados. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos os projetos");
		m.put("Optionally specify space-separated projects to search in. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to search in all projects with code read permission", 
			"Opcionalmente especifique projetos separados por espaço para pesquisar. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para pesquisar em todos os projetos com permissão de leitura de código");
		m.put("Optionally specify space-separated reports. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Opcionalmente especifique relatórios separados por espaço. Use '*' ou '?' para correspondência de caracteres curinga. Prefixe com '-' para excluir");
		m.put("Optionally specify space-separated service images applicable for this locator. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifique imagens de serviço separadas por espaço aplicáveis para este localizador. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de caracteres curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos");
		m.put("Optionally specify space-separated service names applicable for this locator. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all", 
			"Opcionalmente especifique nomes de serviço separados por espaço aplicáveis para este localizador. Use '*' ou '?' para correspondência de caracteres curinga. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos");
		m.put("Optionally specify space-separated tags to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all tags", 
			"Opcionalmente especifique tags separadas por espaço para verificar. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de caracteres curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para corresponder a todas as tags");
		m.put("Optionally specify space-separated target branches of the pull requests to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Opcionalmente especifique os ramos de destino separados por espaço dos pull requests para verificar. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de caracteres curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos os ramos");
		m.put("Optionally specify the OpenID claim to retrieve groups of authenticated user. Depending on the provider, you may need to request additional scopes above to make this claim available", 
			"Opcionalmente especifique a reivindicação OpenID para recuperar grupos de usuários autenticados. Dependendo do provedor, pode ser necessário solicitar escopos adicionais acima para tornar esta reivindicação disponível");
		m.put("Optionally specify the maximum value allowed.", "Opcionalmente especifique o valor máximo permitido.");
		m.put("Optionally specify the minimum value allowed.", "Opcionalmente especifique o valor mínimo permitido.");
		m.put("Optionally specify the project to publish site files to. Leave empty to publish to current project", 
			"Opcionalmente especifique o projeto para publicar arquivos do site. Deixe vazio para publicar no projeto atual");
		m.put("Optionally specify uid:gid to run container as. <b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or using user namespace remapping", 
			"Opcionalmente especifique uid:gid para executar o contêiner. <b class='text-warning'>Nota:</b> Esta configuração deve ser deixada vazia se o runtime do contêiner for rootless ou estiver usando remapeamento de namespace de usuário");
		m.put("Optionally specify user name to access remote repository", "Opcionalmente especifique o nome de usuário para acessar o repositório remoto");
		m.put("Optionally specify valid scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope", 
			"Opcionalmente especifique escopos válidos de commits convencionais (pressione ENTER para adicionar valor). Deixe vazio para permitir escopo arbitrário");
		m.put("Optionally specify valid types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type", 
			"Opcionalmente especifique tipos válidos de commits convencionais (pressione ENTER para adicionar valor). Deixe vazio para permitir tipo arbitrário");
		m.put("Optionally specify value of git config <code>pack.packSizeLimit</code> for the repository", 
			"Opcionalmente especifique o valor da configuração git <code>pack.packSizeLimit</code> para o repositório");
		m.put("Optionally specify value of git config <code>pack.threads</code> for the repository", 
			"Opcionalmente especifique o valor da configuração git <code>pack.threads</code> para o repositório");
		m.put("Optionally specify value of git config <code>pack.window</code> for the repository", 
			"Opcionalmente especifique o valor da configuração git <code>pack.window</code> para o repositório");
		m.put("Optionally specify value of git config <code>pack.windowMemory</code> for the repository", 
			"Opcionalmente especifique o valor da configuração git <code>pack.windowMemory</code> para o repositório");
		m.put("Optionally specify where to run service pods specified in job. The first matching locator will be used. If no any locators are found, node selector of the executor will be used", 
			"Opcionalmente especifique onde executar pods de serviço especificados no trabalho. O primeiro localizador correspondente será usado. Se nenhum localizador for encontrado, o seletor de nó do executor será usado");
		m.put("Optionally specify working directory of the container. Leave empty to use default working directory of the container", 
			"Opcionalmente especifique o diretório de trabalho do contêiner. Deixe vazio para usar o diretório de trabalho padrão do contêiner");
		m.put("Options", "Opções");
		m.put("Or manually enter the secret key below in your authenticator app", "Ou insira manualmente a chave secreta abaixo no seu aplicativo autenticador");
		m.put("Order By", "Ordenar Por");
		m.put("Order More User Months", "Ordenar Mais Meses de Usuário");
		m.put("Order Subscription", "Ordenar Assinatura");
		m.put("Ordered List", "Lista Ordenada");
		m.put("Ordered list", "Lista ordenada");
		m.put("Osv License Scanner", "Scanner de Licença Osv");
		m.put("Osv Vulnerability Scanner", "Scanner de Vulnerabilidade Osv");
		m.put("Other", "Outro");
		m.put("Outline", "Esboço");
		m.put("Outline Search", "Busca de Esboço");
		m.put("Output", "Saída");
		m.put("Overall", "Geral");
		m.put("Overall Estimated Time:", "Tempo Estimado Geral:");
		m.put("Overall Spent Time:", "Tempo Gasto Geral:");
		m.put("Overview", "Visão Geral");
		m.put("Own:", "Próprio:");
		m.put("Ownered By", "Propriedade de");
		m.put("PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'", "Chave privada PEM começa com '-----BEGIN RSA PRIVATE KEY-----'");
		m.put("PENDING", "PENDENTE");
		m.put("PMD Report", "Relatório PMD");
		m.put("Pack", "Pacote");
		m.put("Pack Notification", "Notificação de Pacote");
		m.put("Pack Size Limit", "Limite de Tamanho do Pacote");
		m.put("Pack Type", "Tipo de Pacote");
		m.put("Package", "Pacote");
		m.put("Package Management", "Gerenciamento de Pacotes");
		m.put("Package Notification", "Notificação de Pacote");
		m.put("Package Notification Template", "Template de Notificação de Pacote");
		m.put("Package Privilege", "Privilégio de Pacote");
		m.put("Package Storage", "Armazenamento de Pacote");
		m.put("Package list", "Lista de Pacotes");
		m.put("Package {0} deleted", "Pacote {0} excluído");
		m.put("Packages", "Pacotes");
		m.put("Page Not Found", "Página Não Encontrada");
		m.put("Page is in error, reload to recover", "A página está com erro, recarregue para recuperar");
		m.put("Param Instance", "Instância de Parâmetro");
		m.put("Param Instances", "Instâncias de Parâmetro");
		m.put("Param Map", "Mapa de Parâmetro");
		m.put("Param Matrix", "Matriz de Parâmetro");
		m.put("Param Name", "Nome do Parâmetro");
		m.put("Param Spec", "Especificação de Parâmetro");
		m.put("Param Spec Bean", "Bean de Especificação de Parâmetro");
		m.put("Parameter", "Parâmetro");
		m.put("Parameter Specs", "Especificações de Parâmetro");
		m.put("Params", "Parâmetros");
		m.put("Params & Triggers", "Parâmetros & Gatilhos");
		m.put("Params to Display", "Parâmetros para Exibir");
		m.put("Parent Bean", "Bean Pai");
		m.put("Parent OneDev Project", "Projeto Pai OneDev");
		m.put("Parent Project", "Projeto Pai");
		m.put("Parent project not found", "Projeto pai não encontrado");
		m.put("Parents", "Pais");
		m.put("Partially covered", "Parcialmente coberto");
		m.put("Partially covered by some tests", "Parcialmente coberto por alguns testes");
		m.put("Passcode", "Código de Acesso");
		m.put("Passed", "Aprovado");
		m.put("Password", "Senha");
		m.put("Password Authenticator", "Autenticador de Senha");
		m.put("Password Edit Bean", "Bean de Edição de Senha");
		m.put("Password Must Contain Digit", "A Senha Deve Conter Dígito");
		m.put("Password Must Contain Lowercase", "A Senha Deve Conter Letra Minúscula");
		m.put("Password Must Contain Special Character", "A Senha Deve Conter Caractere Especial");
		m.put("Password Must Contain Uppercase", "A Senha Deve Conter Letra Maiúscula");
		m.put("Password Policy", "Política de Senha");
		m.put("Password Reset", "Redefinição de Senha");
		m.put("Password Reset Bean", "Bean de Redefinição de Senha");
		m.put("Password Reset Template", "Template de Redefinição de Senha");
		m.put("Password Secret", "Segredo de Senha");
		m.put("Password and its confirmation should be identical.", "A senha e sua confirmação devem ser idênticas.");
		m.put("Password changed. Please login with your new password", "Senha alterada. Por favor, faça login com sua nova senha");
		m.put("Password has been changed", "A senha foi alterada");
		m.put("Password has been removed", "A senha foi removida");
		m.put("Password has been set", "A senha foi definida");
		m.put("Password of the user", "Senha do usuário");
		m.put("Password or Access Token for Remote Repository", "Senha ou Token de Acesso para Repositório Remoto");
		m.put("Password reset request has been sent", "Solicitação de redefinição de senha foi enviada");
		m.put("Password reset url is invalid or obsolete", "A URL de redefinição de senha é inválida ou obsoleta");
		m.put("PasswordMinimum Length", "Comprimento Mínimo da Senha");
		m.put("Paste subscription key here", "Cole a chave de assinatura aqui");
		m.put("Path containing spaces or starting with dash needs to be quoted", "Caminho contendo espaços ou começando com traço precisa ser citado");
		m.put("Path placeholder", "Placeholder de Caminho");
		m.put("Path to kubectl", "Caminho para kubectl");
		m.put("Paths", "Caminhos");
		m.put("Pattern", "Padrão");
		m.put("Pause", "Pausar");
		m.put("Pause All Queried Agents", "Pausar Todos os Agentes Consultados");
		m.put("Pause Selected Agents", "Pausar Agentes Selecionados");
		m.put("Paused", "Pausado");
		m.put("Paused all queried agents", "Todos os agentes consultados foram pausados");
		m.put("Paused selected agents", "Agentes selecionados foram pausados");
		m.put("Pem Private Key", "Chave Privada Pem");
		m.put("Pending", "Pendente");
		m.put("Performance", "Desempenho");
		m.put("Performance Setting", "Configuração de Desempenho");
		m.put("Performance Settings", "Configurações de Desempenho");
		m.put("Performance settings have been saved", "As configurações de desempenho foram salvas");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\"", 
			"Realizando consulta aproximada. Envolvendo o texto de busca com '~' para adicionar mais condições, por exemplo: ~texto para buscar~ e \"Estado\" é \"Aberto\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"", 
			"Realizando consulta aproximada. Envolvendo o texto de busca com '~' para adicionar mais condições, por exemplo: ~texto para buscar~ e \"Tipo\" é \"NPM\"");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online", 
			"Realizando consulta aproximada. Envolvendo o texto de busca com '~' para adicionar mais condições, por exemplo: ~texto para buscar~ e online");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open", 
			"Realizando consulta aproximada. Envolvendo o texto de busca com '~' para adicionar mais condições, por exemplo: ~texto para buscar~ e aberto");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and owned by me", 
			"Realizando consulta aproximada. Envolvendo o texto de busca com '~' para adicionar mais condições, por exemplo: ~texto para buscar~ e de minha propriedade");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved", 
			"Realizando consulta aproximada. Envolvendo o texto de busca com '~' para adicionar mais condições, por exemplo: ~texto para buscar~ e não resolvido");
		m.put("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ author(robin)", 
			"Realizando consulta aproximada. Envolvendo o texto de busca com '~' para adicionar mais condições, por exemplo: ~texto para buscar~ autor(robin)");
		m.put("Permanent link", "Link permanente");
		m.put("Permanent link of this selection", "Link permanente desta seleção");
		m.put("Permission denied", "Permissão negada");
		m.put("Permission will be checked upon actual operation", "A permissão será verificada na operação real");
		m.put("Physical memory in mega bytes", "Memória física em megabytes");
		m.put("Pick Existing", "Selecionar Existente");
		m.put("Pin this issue", "Fixar este problema");
		m.put("Pipeline", "Pipeline");
		m.put("Placeholder", "Espaço Reservado");
		m.put("Plain text expected", "Texto simples esperado");
		m.put("Platform", "Plataforma");
		m.put("Platforms", "Plataformas");
		m.put("Please <a wicket:id=\"download\" class=\"font-weight-bolder\">download</a> recovery codes below and keep them secret. These codes can be used to provide one-time access to your account in case you can not access the authentication application. They will <b>NOT</b> be displayed again", 
			"Por favor, <a wicket:id=\"download\" class=\"font-weight-bolder\">baixe</a> os códigos de recuperação abaixo e mantenha-os em segredo. Esses códigos podem ser usados para fornecer acesso único à sua conta caso você não consiga acessar o aplicativo de autenticação. Eles <b>NÃO</b> serão exibidos novamente");
		m.put("Please Confirm", "Por favor, confirme");
		m.put("Please Note", "Por favor, observe");
		m.put("Please check your email for password reset instructions", "Por favor, verifique seu email para instruções de redefinição de senha");
		m.put("Please choose revision to create branch from", "Por favor, escolha a revisão para criar o branch");
		m.put("Please configure <a wicket:id=\"mailSetting\">mail setting</a> first", "Por favor, configure <a wicket:id=\"mailSetting\">a configuração de e-mail</a> primeiro");
		m.put("Please confirm", "Por favor, confirme");
		m.put("Please confirm the password.", "Por favor, confirme a senha.");
		m.put("Please follow <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">this instruction</a> to resolve the conflicts", 
			"Por favor, siga <a wicket:id=\"resolveInstructions\" class=\"link-primary\" href=\"javascript:void(0);\">esta instrução</a> para resolver os conflitos");
		m.put("Please input one of your recovery codes saved when enable two-factor authentication", 
			"Por favor, insira um dos seus códigos de recuperação salvos ao habilitar a autenticação de dois fatores");
		m.put("Please login to perform this operation", "Por favor, faça login para realizar esta operação");
		m.put("Please login to perform this query", "Por favor, faça login para realizar esta consulta");
		m.put("Please resolve undefined field values below", "Por favor, resolva os valores de campos indefinidos abaixo");
		m.put("Please resolve undefined fields below", "Por favor, resolva os campos indefinidos abaixo");
		m.put("Please resolve undefined states below. Note that if you select to delete an undefined state, all issues with that state will be deleted", 
			"Por favor, resolva os estados indefinidos abaixo. Observe que, se você optar por excluir um estado indefinido, todos os problemas com esse estado serão excluídos");
		m.put("Please select agents to pause", "Por favor, selecione os agentes para pausar");
		m.put("Please select agents to remove", "Por favor, selecione os agentes para remover");
		m.put("Please select agents to restart", "Por favor, selecione os agentes para reiniciar");
		m.put("Please select agents to resume", "Por favor, selecione os agentes para retomar");
		m.put("Please select branches to create pull request", "Por favor, selecione os branches para criar uma pull request");
		m.put("Please select builds to cancel", "Por favor, selecione as builds para cancelar");
		m.put("Please select builds to delete", "Por favor, selecione as builds para excluir");
		m.put("Please select builds to re-run", "Por favor, selecione as builds para reexecutar");
		m.put("Please select comments to delete", "Por favor, selecione os comentários para excluir");
		m.put("Please select comments to set resolved", "Por favor, selecione os comentários para marcar como resolvidos");
		m.put("Please select comments to set unresolved", "Por favor, selecione os comentários para marcar como não resolvidos");
		m.put("Please select different branches", "Por favor, selecione branches diferentes");
		m.put("Please select fields to update", "Por favor, selecione os campos para atualizar");
		m.put("Please select groups to remove from", "Por favor, selecione os grupos para remover");
		m.put("Please select issues to copy", "Por favor, selecione os problemas para copiar");
		m.put("Please select issues to delete", "Por favor, selecione os problemas para excluir");
		m.put("Please select issues to edit", "Por favor, selecione os problemas para editar");
		m.put("Please select issues to move", "Por favor, selecione os problemas para mover");
		m.put("Please select issues to sync estimated/spent time", "Por favor, selecione os problemas para sincronizar o tempo estimado/gasto");
		m.put("Please select packages to delete", "Por favor, selecione os pacotes para excluir");
		m.put("Please select projects to delete", "Por favor, selecione os projetos para excluir");
		m.put("Please select projects to modify", "Por favor, selecione os projetos para modificar");
		m.put("Please select projects to move", "Por favor, selecione os projetos para mover");
		m.put("Please select pull requests to delete", "Por favor, selecione as pull requests para excluir");
		m.put("Please select pull requests to discard", "Por favor, selecione as pull requests para descartar");
		m.put("Please select pull requests to watch/unwatch", "Por favor, selecione as pull requests para observar/desobservar");
		m.put("Please select query watches to delete", "Por favor, selecione as consultas observadas para excluir");
		m.put("Please select revision to create tag from", "Por favor, selecione a revisão para criar uma tag");
		m.put("Please select revisions to compare", "Por favor, selecione as revisões para comparar");
		m.put("Please select users to convert to service accounts", "Por favor, selecione usuários para converter para contas de serviço");
		m.put("Please select users to disable", "Por favor, selecione os usuários para desativar");
		m.put("Please select users to enable", "Por favor, selecione os usuários para ativar");
		m.put("Please select users to remove from group", "Por favor, selecione os usuários para remover do grupo");
		m.put("Please specify file name above before editing content", "Por favor, especifique o nome do arquivo acima antes de editar o conteúdo");
		m.put("Please switch to packages page of a particular project for the instructions", "Por favor, mude para a página de pacotes de um projeto específico para as instruções");
		m.put("Please wait...", "Por favor, aguarde...");
		m.put("Please waiting...", "Aguardando...");
		m.put("Plugin metadata not found", "Metadados do plugin não encontrados");
		m.put("Poll Interval", "Intervalo de Polling");
		m.put("Populate Tag Mappings", "Preencher Mapeamentos de Tags");
		m.put("Port", "Porta");
		m.put("Post", "Postar");
		m.put("Post Build Action", "Ação Pós-Build");
		m.put("Post Build Action Bean", "Bean de Ação Pós-Build");
		m.put("Post Build Actions", "Ações Pós-Build");
		m.put("Post Url", "URL de Postagem");
		m.put("PowerShell", "PowerShell");
		m.put("Prefix Pattern", "Padrão de Prefixo");
		m.put("Prefix the title with <code>WIP</code> or <code>[WIP]</code> to mark the pull request as work in progress", 
			"Prefixe o título com <code>WIP</code> ou <code>[WIP]</code> para marcar o pull request como trabalho em andamento");
		m.put("Prepend", "Adicionar ao Início");
		m.put("Preserve Days", "Preservar Dias");
		m.put("Preset Commit Message", "Mensagem de Commit Pré-definida");
		m.put("Preset commit message updated", "Mensagem de commit pré-definida atualizada");
		m.put("Press 'y' to get permalink", "Pressione 'y' para obter o link permanente");
		m.put("Prev", "Anterior");
		m.put("Prevent Creation", "Prevenir Criação");
		m.put("Prevent Deletion", "Prevenir Exclusão");
		m.put("Prevent Forced Push", "Prevenir Push Forçado");
		m.put("Prevent Update", "Prevenir Atualização");
		m.put("Preview", "Pré-visualizar");
		m.put("Previous", "Anterior");
		m.put("Previous Value", "Valor Anterior");
		m.put("Previous commit", "Commit Anterior");
		m.put("Previous {0}", "Anterior {0}");
		m.put("Primary", "Primário");
		m.put("Primary <a wicket:id=\"noPrimaryAddressLink\">email address</a> not specified", "Endereço de <a wicket:id=\"noPrimaryAddressLink\">e-mail primário</a> não especificado");
		m.put("Primary Email", "E-mail Primário");
		m.put("Primary email address not specified", "Endereço de email principal não especificado");
		m.put("Primary email address of your account is not specified yet", "O endereço de e-mail principal da sua conta ainda não foi especificado");
		m.put("Primary email address will be used to receive notifications, show gravatar (if enabled) etc.", 
			"O endereço de e-mail principal será usado para receber notificações, exibir gravatar (se habilitado), etc.");
		m.put("Primary or alias email address of above account to be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"Endereço de e-mail principal ou alias da conta acima a ser usado como endereço remetente de várias notificações por e-mail. O usuário também pode responder a este endereço para postar comentários de problemas ou solicitações de pull por e-mail se a opção <code>Check Incoming Email</code> estiver habilitada abaixo");
		m.put("Principal name of the account to login into office 365 mail server to send/receive emails. Make sure this account <b>owns</b> the registered application indicated by application id above", 
			"Nome principal da conta para fazer login no servidor de e-mail do Office 365 para enviar/receber e-mails. Certifique-se de que esta conta <b>possui</b> o aplicativo registrado indicado pelo ID do aplicativo acima");
		m.put("Private Key Secret", "Chave Privada Secreta");
		m.put("Private key regenerated and SSH server restarted", "Chave privada regenerada e servidor SSH reiniciado");
		m.put("Privilege", "Privilégio");
		m.put("Privilege Settings", "Configurações de Privilégio");
		m.put("Product Version", "Versão do Produto");
		m.put("Profile", "Perfil");
		m.put("Programming language", "Linguagem de Programação");
		m.put("Project", "Projeto");
		m.put("Project \"{0}\" deleted", "Projeto \"{0}\" excluído");
		m.put("Project Authorization Bean", "Bean de Autorização de Projeto");
		m.put("Project Authorizations Bean", "Beans de Autorizações de Projeto");
		m.put("Project Build Setting", "Configuração de Build do Projeto");
		m.put("Project Dependencies", "Dependências do Projeto");
		m.put("Project Dependency", "Dependência do Projeto");
		m.put("Project Id", "ID do Projeto");
		m.put("Project Import Option", "Opção de Importação de Projeto");
		m.put("Project Issue Setting", "Configuração de Problemas do Projeto");
		m.put("Project Key", "Chave do Projeto");
		m.put("Project Management", "Gerenciamento de Projeto");
		m.put("Project Pack Setting", "Configuração de Pacote do Projeto");
		m.put("Project Path", "Caminho do Projeto");
		m.put("Project Pull Request Setting", "Configuração de Solicitação de Pull do Projeto");
		m.put("Project Replicas", "Réplicas do Projeto");
		m.put("Project authorizations updated", "Autorizações de projeto atualizadas");
		m.put("Project does not have any code yet", "O projeto ainda não possui nenhum código");
		m.put("Project forked", "Projeto bifurcado");
		m.put("Project id", "ID do projeto");
		m.put("Project list", "Lista de Projetos");
		m.put("Project manage privilege required to delete \"{0}\"", "Privilégio de gerenciamento de projeto necessário para excluir \"{0}\"");
		m.put("Project manage privilege required to modify \"{0}\"", "Privilégio de gerenciamento de projeto necessário para modificar \"{0}\"");
		m.put("Project manage privilege required to move \"{0}\"", "Privilégio de gerenciamento de projeto necessário para mover \"{0}\"");
		m.put("Project name", "Nome do Projeto");
		m.put("Project not specified yet", "Projeto ainda não especificado");
		m.put("Project or revision not specified yet", "Projeto ou revisão ainda não especificado");
		m.put("Project overview", "Visão Geral do Projeto");
		m.put("Project path", "Caminho do Projeto");
		m.put("Projects", "Projetos");
		m.put("Projects Bean", "Bean de Projetos");
		m.put("Projects deleted", "Projetos excluídos");
		m.put("Projects modified", "Projetos modificados");
		m.put("Projects moved", "Projetos movidos");
		m.put("Projects need to be redistributed when cluster members are added/removed. OneDev does not do this automatically as this is resource intensive, and you may only want to do it after cluster is finalized and stable.", 
			"Os projetos precisam ser redistribuídos quando membros do cluster são adicionados/removidos. O OneDev não faz isso automaticamente, pois é intensivo em recursos, e você pode querer fazer isso apenas após o cluster estar finalizado e estável.");
		m.put("Promotions", "Promoções");
		m.put("Prompt Fields", "Campos de Prompt");
		m.put("Properties", "Propriedades");
		m.put("Provide server id (guild id) to restrict access only to server members", "Forneça o ID do servidor (ID do guild) para restringir o acesso apenas aos membros do servidor");
		m.put("Proxy", "Proxy");
		m.put("Prune Builder Cache", "Limpar Cache do Builder");
		m.put("Prune image cache of docker buildx builder. This step calls docker builder prune command to remove cache of buildx builder specified in server docker executor or remote docker executor", 
			"Limpar cache de imagem do builder do docker buildx. Esta etapa chama o comando docker builder prune para remover o cache do builder buildx especificado no executor docker do servidor ou executor docker remoto");
		m.put("Public", "Público");
		m.put("Public Key", "Chave Pública");
		m.put("Public Roles", "Funções Públicas");
		m.put("Publish", "Publicar");
		m.put("Publish Coverage Report Step", "Etapa de Publicação de Relatório de Cobertura");
		m.put("Publish Problem Report Step", "Etapa de Publicação de Relatório de Problemas");
		m.put("Publish Report Step", "Etapa de Publicação de Relatório");
		m.put("Publish Unit Test Report Step", "Etapa de Publicação de Relatório de Teste Unitário");
		m.put("Published After", "Publicado Após");
		m.put("Published At", "Publicado Em");
		m.put("Published Before", "Publicado Antes");
		m.put("Published By", "Publicado Por");
		m.put("Published By Project", "Publicado Pelo Projeto");
		m.put("Published By User", "Publicado Pelo Usuário");
		m.put("Published File", "Arquivo Publicado");
		m.put("Pull Command", "Comando de Pull");
		m.put("Pull Image", "Imagem de Pull");
		m.put("Pull Request", "Solicitação de Pull");
		m.put("Pull Request Branches", "Ramos de Solicitação de Pull");
		m.put("Pull Request Description", "Descrição da Solicitação de Pull");
		m.put("Pull Request Filter", "Filtro de Solicitação de Pull");
		m.put("Pull Request Management", "Gerenciamento de Solicitação de Pull");
		m.put("Pull Request Markdown Report", "Relatório Markdown de Solicitação de Pull");
		m.put("Pull Request Notification", "Notificação de Solicitação de Pull");
		m.put("Pull Request Notification Template", "Template de Notificação de Solicitação de Pull");
		m.put("Pull Request Notification Unsubscribed", "Notificação de Solicitação de Pull Cancelada");
		m.put("Pull Request Notification Unsubscribed Template", "Template de Notificação de Solicitação de Pull Cancelada");
		m.put("Pull Request Settings", "Configurações de Solicitação de Pull");
		m.put("Pull Request Statistics", "Estatísticas de Solicitação de Pull");
		m.put("Pull Request Title", "Título da Solicitação de Pull");
		m.put("Pull Requests", "Solicitações de Pull");
		m.put("Pull docker image as OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Fazer pull da imagem docker como layout OCI via crane. Esta etapa precisa ser executada pelo executor docker do servidor, executor docker remoto ou executor Kubernetes");
		m.put("Pull from Remote", "Pull do Remoto");
		m.put("Pull request", "Solicitação de Pull");
		m.put("Pull request #{0} already closed", "Solicitação de Pull #{0} já fechada");
		m.put("Pull request #{0} deleted", "Solicitação de Pull #{0} excluída");
		m.put("Pull request administrative permission inside a project, including batch operations over multiple pull requests", 
			"Permissão administrativa de solicitação de pull dentro de um projeto, incluindo operações em lote sobre múltiplas solicitações de pull");
		m.put("Pull request already closed", "Solicitação de Pull já fechada");
		m.put("Pull request already opened", "Solicitação de Pull já aberta");
		m.put("Pull request and code review", "Solicitação de Pull e revisão de código");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not finished yet", 
			"A solicitação de pull não pode ser mesclada agora, pois <a class=\"more-info d-inline link-primary\">algumas builds necessárias</a> ainda não foram concluídas");
		m.put("Pull request can not be merged now as <a class=\"more-info d-inline link-primary\">some required builds</a> are not successful", 
			"A solicitação de pull não pode ser mesclada agora, pois <a class=\"more-info d-inline link-primary\">algumas builds necessárias</a> não foram bem-sucedidas");
		m.put("Pull request can not be merged now as it is <a class=\"more-info d-inline link-primary\">pending review</a>", 
			"A solicitação de pull não pode ser mesclada agora, pois está <a class=\"more-info d-inline link-primary\">pendente de revisão</a>");
		m.put("Pull request can not be merged now as it was <a class=\"more-info d-inline link-primary\">requested for changes</a>", 
			"A solicitação de pull não pode ser mesclada agora, pois foi <a class=\"more-info d-inline link-primary\">solicitada para alterações</a>");
		m.put("Pull request can not be merged now as valid signature is required for head commit", 
			"A solicitação de pull não pode ser mesclada agora, pois é necessária uma assinatura válida para o commit principal");
		m.put("Pull request can only be merged after getting approvals from all reviewers", "A solicitação de pull só pode ser mesclada após obter aprovações de todos os revisores");
		m.put("Pull request can only be merged by users with code write permission", "A solicitação de pull só pode ser mesclada por usuários com permissão de escrita de código");
		m.put("Pull request discard", "Descartar solicitação de pull");
		m.put("Pull request duration statistics", "Estatísticas de duração de pull request");
		m.put("Pull request frequency statistics", "Estatísticas de frequência de pull request");
		m.put("Pull request is discarded", "Pull request descartado");
		m.put("Pull request is in error: {0}", "Pull request está com erro: {0}");
		m.put("Pull request is merged", "Pull request foi mesclado");
		m.put("Pull request is opened", "Pull request foi aberto");
		m.put("Pull request is still a work in progress", "O pull request ainda está em andamento");
		m.put("Pull request is work in progress", "O pull request está em andamento");
		m.put("Pull request list", "Lista de pull requests");
		m.put("Pull request merge", "Mesclar pull request");
		m.put("Pull request not exist or access denied", "Pull request não existe ou acesso negado");
		m.put("Pull request not merged", "Pull request não mesclado");
		m.put("Pull request number", "Número do pull request");
		m.put("Pull request open or update", "Abrir ou atualizar pull request");
		m.put("Pull request query watch only affects new pull requests. To manage watch status of existing pull requests in batch, filter pull requests by watch status in pull requests page, and then take appropriate action", 
			"A consulta de pull request watch afeta apenas novos pull requests. Para gerenciar o status de watch de pull requests existentes em lote, filtre os pull requests pelo status de watch na página de pull requests e tome a ação apropriada");
		m.put("Pull request settings updated", "Configurações de pull request atualizadas");
		m.put("Pull request statistics is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Estatísticas de pull request são um recurso empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("Pull request synchronization submitted", "Sincronização de pull request enviada");
		m.put("Pull request will be merged automatically when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"Pull request será mesclado automaticamente quando estiver pronto. Esta opção será desativada ao adicionar novos commits, alterar a estratégia de mesclagem ou mudar o branch de destino");
		m.put("Pull request will be merged automatically with a preset <a wicket:id=\"commitMessage\">commit message</a> when ready. This option will be disabled upon adding new commits, changing merge strategy, or switching target branch", 
			"Pull request será mesclado automaticamente com uma <a wicket:id=\"commitMessage\">mensagem de commit</a> predefinida quando estiver pronto. Esta opção será desativada ao adicionar novos commits, alterar a estratégia de mesclagem ou mudar o branch de destino");
		m.put("Push Image", "Enviar imagem");
		m.put("Push chart to the repository", "Enviar gráfico para o repositório");
		m.put("Push docker image from OCI layout via crane. This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor", 
			"Enviar imagem docker do layout OCI via crane. Esta etapa precisa ser executada pelo executor docker do servidor, executor docker remoto ou executor Kubernetes");
		m.put("Push to Remote", "Enviar para remoto");
		m.put("Push to container registry", "Enviar para registro de contêiner");
		m.put("PyPI(s)", "PyPI(s)");
		m.put("Pylint Report", "Relatório Pylint");
		m.put("Queries", "Consultas");
		m.put("Query", "Consulta");
		m.put("Query Parameters", "Parâmetros de Consulta");
		m.put("Query Watches", "Consultas Watch");
		m.put("Query commits", "Consultar commits");
		m.put("Query not submitted", "Consulta não enviada");
		m.put("Query param", "Parâmetro de consulta");
		m.put("Query/order agents", "Consultar/ordenar agentes");
		m.put("Query/order builds", "Consultar/ordenar builds");
		m.put("Query/order comments", "Consultar/ordenar comentários");
		m.put("Query/order issues", "Consultar/ordenar issues");
		m.put("Query/order packages", "Consultar/ordenar pacotes");
		m.put("Query/order projects", "Consultar/ordenar projetos");
		m.put("Query/order pull requests", "Consultar/ordenar pull requests");
		m.put("Queueing Takes", "Tempo de fila");
		m.put("Quick Search", "Busca rápida");
		m.put("Quote", "Citação");
		m.put("RESTful API", "API RESTful");
		m.put("RESTful API Help", "Ajuda da API RESTful");
		m.put("Ran On Agent", "Executado no agente");
		m.put("Re-run All Queried Builds", "Reexecutar todos os builds consultados");
		m.put("Re-run Selected Builds", "Reexecutar builds selecionados");
		m.put("Re-run request submitted", "Solicitação de reexecução enviada");
		m.put("Re-run this build", "Reexecutar este build");
		m.put("Read", "Ler");
		m.put("Read body", "Ler corpo");
		m.put("Readiness Check Command", "Comando de verificação de prontidão");
		m.put("Really want to delete this code comment?", "Realmente deseja excluir este comentário de código?");
		m.put("Rebase", "Rebase");
		m.put("Rebase Source Branch Commits", "Rebase dos commits do branch de origem");
		m.put("Rebase all commits from source branch onto target branch", "Rebase de todos os commits do branch de origem no branch de destino");
		m.put("Rebase source branch commits", "Rebase dos commits do branch de origem");
		m.put("Rebuild manually", "Reconstruir manualmente");
		m.put("Receive Posted Email", "Receber e-mail postado");
		m.put("Received test mail", "E-mail de teste recebido");
		m.put("Receivers", "Destinatários");
		m.put("Recovery code", "Código de recuperação");
		m.put("Recursive", "Recursivo");
		m.put("Redundant", "Redundante");
		m.put("Ref", "Ref");
		m.put("Ref Name", "Nome da ref");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup", 
			"Consulte este <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> para um exemplo de configuração");
		m.put("Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> for an example setup", 
			"Consulte este <a href='https://docs.onedev.io/tutorials/security/sso-with-okta' target='_blank'>tutorial</a> para um exemplo de configuração");
		m.put("Reference", "Referência");
		m.put("Reference Build", "Build de referência");
		m.put("Reference Issue", "Issue de referência");
		m.put("Reference Pull Request", "Pull request de referência");
		m.put("Reference this {0} in markdown or commit message via below string.", "Referencie este {0} em markdown ou mensagem de commit via string abaixo.");
		m.put("Refresh", "Atualizar");
		m.put("Refresh Token", "Token de atualização");
		m.put("Refs", "Refs");
		m.put("Regenerate", "Regenerar");
		m.put("Regenerate Private Key", "Regenerar chave privada");
		m.put("Regenerate this access token", "Regenerar este token de acesso");
		m.put("Registry Login", "Login no registro");
		m.put("Registry Logins", "Logins no registro");
		m.put("Registry Url", "URL do registro");
		m.put("Regular Expression", "Expressão regular");
		m.put("Remaining User Months", "Meses de usuário restantes");
		m.put("Remaining User Months:", "Meses de usuário restantes:");
		m.put("Remaining time", "Tempo restante");
		m.put("Remember Me", "Lembrar-me");
		m.put("Remote Docker Executor", "Executor Docker remoto");
		m.put("Remote Machine", "Máquina remota");
		m.put("Remote Shell Executor", "Executor de shell remoto");
		m.put("Remote URL", "URL remoto");
		m.put("Remote Url", "URL remoto");
		m.put("Remove", "Remover");
		m.put("Remove All Queried Agents", "Remover todos os agentes consultados");
		m.put("Remove All Queried Users from Group", "Remover todos os usuários consultados do grupo");
		m.put("Remove Fields", "Remover campos");
		m.put("Remove From Current Iteration", "Remover da iteração atual");
		m.put("Remove Selected Agents", "Remover agentes selecionados");
		m.put("Remove Selected Users from Group", "Remover usuários selecionados do grupo");
		m.put("Remove from All Queried Groups", "Remover de todos os grupos consultados");
		m.put("Remove from Selected Groups", "Remover dos grupos selecionados");
		m.put("Remove from batch", "Remover do lote");
		m.put("Remove issue from this iteration", "Remover issue desta iteração");
		m.put("Remove this assignee", "Remover este responsável");
		m.put("Remove this external participant from issue", "Remover este participante externo da issue");
		m.put("Remove this file", "Remover este arquivo");
		m.put("Remove this image", "Remover esta imagem");
		m.put("Remove this reviewer", "Remover este revisor");
		m.put("Removed all queried agents. Type <code>yes</code> below to confirm", "Todos os agentes consultados foram removidos. Digite <code>yes</code> abaixo para confirmar");
		m.put("Removed selected agents. Type <code>yes</code> below to confirm", "Agentes selecionados foram removidos. Digite <code>yes</code> abaixo para confirmar");
		m.put("Rename {0}", "Renomear {0}");
		m.put("Renew Subscription", "Renovar Assinatura");
		m.put("Renovate CLI Options", "Opções do CLI Renovate");
		m.put("Renovate JavaScript Config", "Configuração JavaScript do Renovate");
		m.put("Reopen", "Reabrir");
		m.put("Reopen this iteration", "Reabrir esta iteração");
		m.put("Reopened pull request \"{0}\" ({1})", "Solicitação de pull reaberta \"{0}\" ({1})");
		m.put("Replace With", "Substituir por");
		m.put("Replica Count", "Contagem de réplicas");
		m.put("Replicas", "Réplicas");
		m.put("Replied to comment on file \"{0}\" in project \"{1}\"", "Respondeu ao comentário no arquivo \"{0}\" no projeto \"{1}\"");
		m.put("Reply", "Responder");
		m.put("Report Name", "Nome do relatório");
		m.put("Report format changed. You may re-run this build to generate the report in new format", 
			"Formato do relatório alterado. Você pode executar novamente esta build para gerar o relatório no novo formato");
		m.put("Repository Sync", "Sincronização do repositório");
		m.put("Request Body", "Corpo da Solicitação");
		m.put("Request For Changes", "Solicitação de alterações");
		m.put("Request Scopes", "Escopos de solicitação");
		m.put("Request Trial Subscription", "Solicitar assinatura de teste");
		m.put("Request review", "Solicitar revisão");
		m.put("Request to sync", "Solicitar sincronização");
		m.put("Requested For changes", "Alterações solicitadas");
		m.put("Requested changes to pull request \"{0}\" ({1})", "Alterações solicitadas na solicitação de pull \"{0}\" ({1})");
		m.put("Requested for changes", "Alterações solicitadas");
		m.put("Requested to sync estimated/spent time", "Solicitado para sincronizar tempo estimado/gasto");
		m.put("Require Autentication", "Exigir autenticação");
		m.put("Require Strict Pull Request Builds", "Exigir builds rigorosos de solicitação de pull");
		m.put("Require Successful", "Exigir sucesso");
		m.put("Required", "Obrigatório");
		m.put("Required Builds", "Builds necessários");
		m.put("Required Reviewers", "Revisores necessários");
		m.put("Required Services", "Serviços necessários");
		m.put("Resend Verification Email", "Reenviar e-mail de verificação");
		m.put("Resend invitation", "Reenviar convite");
		m.put("Reset", "Redefinir");
		m.put("Resolution", "Resolução");
		m.put("Resolved", "Resolvido");
		m.put("Resolved comment on file \"{0}\" in project \"{1}\"", "Comentário resolvido no arquivo \"{0}\" no projeto \"{1}\"");
		m.put("Resource", "Recurso");
		m.put("Resource Settings", "Configurações de recursos");
		m.put("Resources", "Recursos");
		m.put("Response", "Resposta");
		m.put("Response Body", "Corpo da Resposta");
		m.put("Restart", "Reiniciar");
		m.put("Restart All Queried Agents", "Reiniciar todos os agentes consultados");
		m.put("Restart Selected Agents", "Reiniciar agentes selecionados");
		m.put("Restart command issued", "Comando de reinício emitido");
		m.put("Restart command issued to all queried agents", "Comando de reinício emitido para todos os agentes consultados");
		m.put("Restart command issued to selected agents", "Comando de reinício emitido para agentes selecionados");
		m.put("Restore Source Branch", "Restaurar branch de origem");
		m.put("Restored source branch", "Branch de origem restaurado");
		m.put("Resubmitted manually", "Reenviado manualmente");
		m.put("Resume", "Retomar");
		m.put("Resume All Queried Agents", "Retomar todos os agentes consultados");
		m.put("Resume Selected Agents", "Retomar agentes selecionados");
		m.put("Resumed all queried agents", "Todos os agentes consultados foram retomados");
		m.put("Resumed selected agents", "Agentes selecionados foram retomados");
		m.put("Retried At", "Repetido em");
		m.put("Retrieve Groups", "Recuperar grupos");
		m.put("Retrieve LFS Files", "Recuperar arquivos LFS");
		m.put("Retrieve Submodules", "Recuperar submódulos");
		m.put("Retry Condition", "Condição de repetição");
		m.put("Retry Delay", "Atraso de repetição");
		m.put("Revert", "Reverter");
		m.put("Reverted successfully", "Revertido com sucesso");
		m.put("Review required for deletion. Submit pull request instead", "Revisão necessária para exclusão. Envie uma solicitação de pull em vez disso");
		m.put("Review required for this change. Please submit pull request instead", "Revisão necessária para esta alteração. Por favor, envie um pull request em vez disso.");
		m.put("Review required for this change. Submit pull request instead", "Revisão necessária para esta alteração. Envie uma solicitação de pull em vez disso");
		m.put("Reviewers", "Revisores");
		m.put("Revision", "Revisão");
		m.put("Revision indexing in progress...", "Indexação de revisão em andamento...");
		m.put("Revision indexing in progress... (symbol navigation in revisions will be accurate after indexed)", 
			"Indexação de revisão em andamento... (a navegação por símbolos nas revisões será precisa após a indexação)");
		m.put("Right", "Direita");
		m.put("Role", "Função");
		m.put("Role \"{0}\" deleted", "Função \"{0}\" excluída");
		m.put("Role \"{0}\" updated", "Função \"{0}\" atualizada");
		m.put("Role Management", "Gerenciamento de funções");
		m.put("Role created", "Função criada");
		m.put("Roles", "Funções");
		m.put("Root Projects", "Projetos raiz");
		m.put("Roslynator Report", "Relatório Roslynator");
		m.put("RubyGems(s)", "RubyGems(s)");
		m.put("Ruff Report", "Relatório Ruff");
		m.put("Rule will apply if user operating the tag matches criteria specified here", "A regra será aplicada se o usuário que opera a tag corresponder aos critérios especificados aqui");
		m.put("Rule will apply only if the user changing the branch matches criteria specified here", 
			"A regra será aplicada apenas se o usuário que altera o branch corresponder aos critérios especificados aqui");
		m.put("Run As", "Executar como");
		m.put("Run Buildx Image Tools", "Executar ferramentas de imagem Buildx");
		m.put("Run Docker Container", "Executar contêiner Docker");
		m.put("Run In Container", "Executar no contêiner");
		m.put("Run Integrity Check", "Executar verificação de integridade");
		m.put("Run Job", "Executar trabalho");
		m.put("Run Options", "Opções de execução");
		m.put("Run below commands from within your git repository:", "Execute os comandos abaixo dentro do seu repositório git:");
		m.put("Run below commands to install this gem", "Execute os comandos abaixo para instalar este gem");
		m.put("Run below commands to install this package", "Execute os comandos abaixo para instalar este pacote");
		m.put("Run below commands to use this chart", "Execute os comandos abaixo para usar este gráfico");
		m.put("Run below commands to use this package", "Execute os comandos abaixo para usar este pacote");
		m.put("Run docker buildx imagetools command with specified arguments. This step can only be executed by server docker executor or remote docker executor", 
			"Execute o comando docker buildx imagetools com os argumentos especificados. Esta etapa só pode ser executada pelo executor docker do servidor ou executor docker remoto");
		m.put("Run job", "Executar trabalho");
		m.put("Run job in another project", "Executar trabalho em outro projeto");
		m.put("Run on Bare Metal/Virtual Machine", "Executar em Bare Metal/Máquina Virtual");
		m.put("Run osv scanner to scan violated licenses used by various <a href='https://deps.dev/' target='_blank'>dependencies</a>. It can only be executed by docker aware executor.", 
			"Execute o scanner osv para verificar licenças violadas usadas por várias <a href='https://deps.dev/' target='_blank'>dependências</a>. Ele só pode ser executado por um executor compatível com Docker.");
		m.put("Run osv scanner to scan vulnerabilities in <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>various lock files</a>. It can only be executed by docker aware executor.", 
			"Execute o scanner osv para verificar vulnerabilidades em <a href='https://google.github.io/osv-scanner/supported-languages-and-lockfiles/' target='_blank'>vários arquivos de bloqueio</a>. Ele só pode ser executado por um executor compatível com Docker.");
		m.put("Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote docker executor", 
			"Execute o contêiner Docker especificado. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>O espaço de trabalho do trabalho</a> é montado no contêiner e seu caminho é colocado na variável de ambiente <code>ONEDEV_WORKSPACE</code>. <b class='text-warning'>Nota: </b> esta etapa só pode ser executada pelo executor docker do servidor ou executor docker remoto");
		m.put("Run specified step template", "Executar modelo de etapa especificado");
		m.put("Run this job", "Executar este trabalho");
		m.put("Run trivy container image scanner to find issues in specified image. For vulnerabilities, it checks various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by docker aware executor.", 
			"Execute o scanner de imagem de contêiner trivy para encontrar problemas na imagem especificada. Para vulnerabilidades, verifica vários <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>arquivos de distribuição</a>. Só pode ser executado por executor compatível com docker.");
		m.put("Run trivy filesystem scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>lock files</a>. It can only be executed by a docker aware executor, and is recommended to run <span class='text-warning'>after dependencies are resolved</span> (npm install or alike). Compared to OSV scanner, its setup is a bit verbose, but can provide more accurate result", 
			"Execute o scanner de sistema de arquivos Trivy para verificar vários <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>arquivos de bloqueio</a>. Só pode ser executado por um executor compatível com Docker e é recomendado executar <span class='text-warning'>após a resolução de dependências</span> (npm install ou similar). Comparado ao scanner OSV, sua configuração é um pouco mais detalhada, mas pode fornecer resultados mais precisos.");
		m.put("Run trivy rootfs scanner to scan various <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>distribution files</a>. It can only be executed by a docker aware executor and is recommended to run against staging area of your project", 
			"Execute o scanner rootfs Trivy para verificar vários <a href='https://aquasecurity.github.io/trivy/v0.50/docs/coverage/language/#supported-languages' target='_blank'>arquivos de distribuição</a>. Só pode ser executado por um executor compatível com Docker e é recomendado executar na área de preparação do seu projeto.");
		m.put("Run via Docker Container", "Executar via contêiner Docker");
		m.put("Running", "Executando");
		m.put("Running Takes", "Executando leva");
		m.put("SLOC on {0}", "SLOC em {0}");
		m.put("SMTP Host", "Host SMTP");
		m.put("SMTP Password", "Senha SMTP");
		m.put("SMTP User", "Usuário SMTP");
		m.put("SMTP/IMAP", "SMTP/IMAP");
		m.put("SSH", "SSH");
		m.put("SSH & GPG Keys", "Chaves SSH & GPG");
		m.put("SSH Clone URL", "URL de clone SSH");
		m.put("SSH Keys", "Chaves SSH");
		m.put("SSH Root URL", "URL raiz SSH");
		m.put("SSH Server Key", "Chave do servidor SSH");
		m.put("SSH key deleted", "Chave SSH excluída");
		m.put("SSH settings have been saved and SSH server restarted", "Configurações SSH foram salvas e o servidor SSH reiniciado");
		m.put("SSL Setting", "Configuração SSL");
		m.put("SSO Accounts", "Contas SSO");
		m.put("SSO Providers", "Provedores SSO");
		m.put("SSO account deleted", "Conta SSO excluída");
		m.put("SSO provider \"{0}\" deleted", "Provedor SSO \"{0}\" excluído");
		m.put("SSO provider created", "Provedor SSO criado");
		m.put("SSO provider updated", "Provedor SSO atualizado");
		m.put("SUCCESSFUL", "SUCESSO");
		m.put("Save", "Salvar");
		m.put("Save Query", "Salvar consulta");
		m.put("Save Query Bean", "Salvar consulta Bean");
		m.put("Save Settings", "Salvar configurações");
		m.put("Save Settings & Redistribute Projects", "Salvar configurações e redistribuir projetos");
		m.put("Save Template", "Salvar modelo");
		m.put("Save as Mine", "Salvar como meu");
		m.put("Saved Queries", "Consultas salvas");
		m.put("Scan Path", "Caminho de varredura");
		m.put("Scan Paths", "Caminhos de varredura");
		m.put("Scan below QR code with your TOTP authenticators", "Escaneie o código QR abaixo com seus autenticadores TOTP");
		m.put("Schedule Issues", "Agendar problemas");
		m.put("Script Name", "Nome do script");
		m.put("Scripting Value", "Valor do script");
		m.put("Search", "Pesquisar");
		m.put("Search For", "Pesquisar por");
		m.put("Search Groups Using Filter", "Pesquisar grupos usando filtro");
		m.put("Search branch", "Pesquisar branch");
		m.put("Search files, symbols and texts", "Pesquisar arquivos, símbolos e textos");
		m.put("Search for", "Pesquisar por");
		m.put("Search inside current tree", "Buscar dentro da árvore atual");
		m.put("Search is too general", "A pesquisa é muito geral");
		m.put("Search job", "Pesquisar trabalho");
		m.put("Search project", "Pesquisar projeto");
		m.put("Secret", "Segredo");
		m.put("Secret Config File", "Arquivo de configuração de segredo");
		m.put("Secret Setting", "Configuração de segredo");
		m.put("Security", "Segurança");
		m.put("Security & Compliance", "Segurança & Conformidade");
		m.put("Security Setting", "Configuração de segurança");
		m.put("Security Settings", "Configurações de segurança");
		m.put("Security settings have been updated", "As configurações de segurança foram atualizadas");
		m.put("Select", "Selecionar");
		m.put("Select Branch to Cherry Pick to", "Selecionar branch para aplicar Cherry Pick");
		m.put("Select Branch to Revert on", "Selecionar branch para reverter");
		m.put("Select Branch/Tag", "Selecionar branch/tag");
		m.put("Select Existing", "Selecionar existente");
		m.put("Select Job", "Selecionar Job");
		m.put("Select Project", "Selecionar projeto");
		m.put("Select below...", "Selecionar abaixo...");
		m.put("Select iteration to schedule issues into", "Selecionar iteração para agendar problemas");
		m.put("Select organization to import from. Leave empty to import from repositories under current account", 
			"Selecionar organização para importar. Deixe vazio para importar de repositórios na conta atual");
		m.put("Select project and revision first", "Selecionar projeto e revisão primeiro");
		m.put("Select project first", "Selecionar projeto primeiro");
		m.put("Select project to import from", "Selecionar projeto para importar");
		m.put("Select project to sync to. Leave empty to sync to current project", "Selecionar projeto para sincronizar. Deixe vazio para sincronizar com o projeto atual");
		m.put("Select repository to import from", "Selecionar repositório para importar");
		m.put("Select users to send alert email upon events such as database auto-backup failure, cluster node unreachable etc", 
			"Selecionar usuários para enviar e-mail de alerta em eventos como falha de backup automático do banco de dados, nó do cluster inacessível, etc.");
		m.put("Select workspace to import from", "Selecionar espaço de trabalho para importar");
		m.put("Send Notifications", "Enviar notificações");
		m.put("Send Pull Request", "Enviar Pull Request");
		m.put("Send notification", "Enviar notificação");
		m.put("SendGrid", "SendGrid");
		m.put("Sendgrid Webhook Setting", "Configuração de webhook SendGrid");
		m.put("Sending invitation to \"{0}\"...", "Enviando convite para \"{0}\"...");
		m.put("Sending test mail to {0}...", "Enviando e-mail de teste para {0}...");
		m.put("Sequential Group", "Grupo sequencial");
		m.put("Server", "Servidor");
		m.put("Server Docker Executor", "Executor Docker do servidor");
		m.put("Server Id", "ID do servidor");
		m.put("Server Information", "Informações do servidor");
		m.put("Server Log", "Log do servidor");
		m.put("Server Setup", "Configuração do servidor");
		m.put("Server Shell Executor", "Executor Shell do servidor");
		m.put("Server URL", "URL do servidor");
		m.put("Server fingerprint", "Impressão digital do servidor");
		m.put("Server host", "Host do servidor");
		m.put("Server is Starting...", "Servidor está iniciando...");
		m.put("Server url", "URL do servidor");
		m.put("Service", "Serviço");
		m.put("Service Account", "Conta de serviço");
		m.put("Service Desk", "Central de atendimento");
		m.put("Service Desk Email Address", "Endereço de e-mail da central de atendimento");
		m.put("Service Desk Issue Open Failed", "Falha ao abrir problema na central de atendimento");
		m.put("Service Desk Issue Open Failed Template", "Modelo de falha ao abrir problema na central de atendimento");
		m.put("Service Desk Issue Opened", "Problema aberto na central de atendimento");
		m.put("Service Desk Issue Opened Template", "Modelo de problema aberto na central de atendimento");
		m.put("Service Desk Setting", "Configuração da central de atendimento");
		m.put("Service Desk Setting Holder", "Placeholder de configuração da central de atendimento");
		m.put("Service Desk Settings", "Configurações da central de atendimento");
		m.put("Service Locator", "Localizador de serviço");
		m.put("Service Locators", "Localizadores de serviço");
		m.put("Service account not allowed to login", "Conta de serviço não permitida para login");
		m.put("Service desk setting", "Configuração do service desk");
		m.put("Service desk settings have been saved", "As configurações do service desk foram salvas");
		m.put("Services", "Serviços");
		m.put("Session Timeout", "Tempo de Sessão Expirado");
		m.put("Set", "Definir");
		m.put("Set All Queried As Root Projects", "Definir Todos Consultados Como Projetos Raiz");
		m.put("Set All Queried Comments as Read", "Definir Todos Comentários Consultados como Lidos");
		m.put("Set All Queried Comments as Resolved", "Definir Todos Comentários Consultados como Resolvidos");
		m.put("Set All Queried Comments as Unresolved", "Definir Todos Comentários Consultados como Não Resolvidos");
		m.put("Set All Queried Issues as Read", "Definir Todas Questões Consultadas como Lidas");
		m.put("Set All Queried Pull Requests as Read", "Definir Todas Solicitações de Pull Consultadas como Lidas");
		m.put("Set As Primary", "Definir Como Primário");
		m.put("Set Build Description", "Definir Descrição da Build");
		m.put("Set Build Version", "Definir Versão da Build");
		m.put("Set Resolved", "Definir Resolvido");
		m.put("Set Selected As Root Projects", "Definir Selecionados Como Projetos Raiz");
		m.put("Set Selected Comments as Resolved", "Definir Comentários Selecionados como Resolvidos");
		m.put("Set Selected Comments as Unresolved", "Definir Comentários Selecionados como Não Resolvidos");
		m.put("Set Unresolved", "Definir Não Resolvido");
		m.put("Set Up Cache", "Configurar Cache");
		m.put("Set Up Renovate Cache", "Configurar Cache do Renovate");
		m.put("Set Up Trivy Cache", "Configurar Cache do Trivy");
		m.put("Set Up Your Account", "Configure Sua Conta");
		m.put("Set as Private", "Definir como Privado");
		m.put("Set as Public", "Definir como Público");
		m.put("Set description", "Definir descrição");
		m.put("Set reviewed", "Definir como Revisado");
		m.put("Set unreviewed", "Definir como Não Revisado");
		m.put("Set up Microsoft Teams notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url. ", 
			"Configurar notificações do Microsoft Teams. As configurações serão herdadas por projetos filhos e podem ser substituídas definindo configurações com a mesma URL de webhook.");
		m.put("Set up discord notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurar notificações do Discord. As configurações serão herdadas por projetos filhos e podem ser substituídas definindo configurações com a mesma URL de webhook.");
		m.put("Set up job cache to speed up job execution. Check <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>this tutorial</a> on how to use job cache", 
			"Configurar cache de trabalho para acelerar a execução de tarefas. Confira <a href='https://docs.onedev.io/tutorials/cicd/job-cache' target='_blank'>este tutorial</a> sobre como usar o cache de trabalho.");
		m.put("Set up ntfy.sh notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurar notificações do ntfy.sh. As configurações serão herdadas por projetos filhos e podem ser substituídas definindo configurações com a mesma URL de webhook.");
		m.put("Set up slack notification settings. Settings will be inherited by child projects, and can be overridden by defining settings with same webhook url", 
			"Configurar notificações do Slack. As configurações serão herdadas por projetos filhos e podem ser substituídas definindo configurações com a mesma URL de webhook.");
		m.put("Set up two-factor authentication", "Configurar autenticação de dois fatores");
		m.put("Setting", "Configuração");
		m.put("Setting has been saved", "A configuração foi salva");
		m.put("Settings", "Configurações");
		m.put("Settings and permissions of parent project will be inherited by this project", "Configurações e permissões do projeto pai serão herdadas por este projeto");
		m.put("Settings saved", "Configurações salvas");
		m.put("Settings saved and project redistribution scheduled", "Configurações salvas e redistribuição de projeto agendada");
		m.put("Settings updated", "Configurações atualizadas");
		m.put("Share dashboard", "Compartilhar painel");
		m.put("Share with Groups", "Compartilhar com Grupos");
		m.put("Share with Users", "Compartilhar com Usuários");
		m.put("Shell", "Shell");
		m.put("Show Archived", "Mostrar Arquivados");
		m.put("Show Branch/Tag", "Mostrar Ramificação/Tag");
		m.put("Show Build Status", "Mostrar Status da Build");
		m.put("Show Closed", "Mostrar Fechados");
		m.put("Show Code Stats", "Mostrar Estatísticas de Código");
		m.put("Show Command", "Mostrar Comando");
		m.put("Show Condition", "Mostrar Condição");
		m.put("Show Conditionally", "Mostrar Condicionalmente");
		m.put("Show Description", "Mostrar Descrição");
		m.put("Show Duration", "Mostrar Duração");
		m.put("Show Emojis", "Mostrar Emojis");
		m.put("Show Error Detail", "Mostrar Detalhe do Erro");
		m.put("Show Issue Status", "Mostrar Status da Questão");
		m.put("Show Package Stats", "Mostrar Estatísticas de Pacotes");
		m.put("Show Pull Request Stats", "Mostrar Estatísticas de Solicitações de Pull");
		m.put("Show Saved Queries", "Mostrar Consultas Salvas");
		m.put("Show States By", "Mostrar Estados Por");
		m.put("Show Works Of", "Mostrar Trabalhos De");
		m.put("Show changes", "Mostrar alterações");
		m.put("Show commented code snippet", "Mostrar trecho de código comentado");
		m.put("Show commit of this parent", "Mostrar commit deste pai");
		m.put("Show emojis", "Mostrar emojis");
		m.put("Show in build list", "Mostrar na lista de builds");
		m.put("Show issues in list", "Mostrar questões na lista");
		m.put("Show issues not scheduled into current iteration", "Mostrar questões não agendadas na iteração atual");
		m.put("Show matching agents", "Mostrar agentes correspondentes");
		m.put("Show more", "Mostrar mais");
		m.put("Show more lines", "Mostrar mais linhas");
		m.put("Show next match", "Mostrar próxima correspondência");
		m.put("Show previous match", "Mostrar correspondência anterior");
		m.put("Show test cases of this test suite", "Mostrar casos de teste desta suíte de testes");
		m.put("Show total estimated/spent time", "Mostrar tempo total estimado/gasto");
		m.put("Showing first {0} files as there are too many", "Mostrando os primeiros {0} arquivos, pois há muitos");
		m.put("Sign In", "Entrar");
		m.put("Sign In To", "Entrar em");
		m.put("Sign Out", "Sair");
		m.put("Sign Up", "Registrar-se");
		m.put("Sign Up Bean", "Registrar-se Bean");
		m.put("Sign Up!", "Registrar-se!");
		m.put("Sign in", "Entrar");
		m.put("Signature required for this change, but no signing key is specified", "Assinatura necessária para esta alteração, mas nenhuma chave de assinatura foi especificada");
		m.put("Signature required for this change, please generate system GPG signing key first", "Assinatura necessária para esta alteração, por favor, gere primeiro a chave de assinatura GPG do sistema");
		m.put("Signature verified successfully with OneDev GPG key", "Assinatura verificada com sucesso com a chave GPG do OneDev");
		m.put("Signature verified successfully with committer's GPG key", "Assinatura verificada com sucesso com a chave GPG do autor do commit");
		m.put("Signature verified successfully with committer's SSH key", "Assinatura verificada com sucesso com a chave SSH do autor do commit");
		m.put("Signature verified successfully with tagger's GPG key", "Assinatura verificada com sucesso com a chave GPG do etiquetador");
		m.put("Signature verified successfully with tagger's SSH key", "Assinatura verificada com sucesso com a chave SSH do etiquetador");
		m.put("Signature verified successfully with trusted GPG key", "Assinatura verificada com sucesso com a chave GPG confiável");
		m.put("Signed with an unknown GPG key ", "Assinado com uma chave GPG desconhecida");
		m.put("Signed with an unknown ssh key", "Assinado com uma chave ssh desconhecida");
		m.put("Signer Email Addresses", "Endereços de Email do Assinante");
		m.put("Signing Key ID", "ID da Chave de Assinatura");
		m.put("Similar Issues", "Questões Similares");
		m.put("Single Sign On", "Login Único");
		m.put("Single Sign-On", "Single Sign-On");
		m.put("Single sign on via discord.com", "Login único via discord.com");
		m.put("Single sign on via twitch.tv", "Login único via twitch.tv");
		m.put("Site", "Site");
		m.put("Size", "Tamanho");
		m.put("Size invalid", "Tamanho inválido");
		m.put("Slack Notifications", "Notificações do Slack");
		m.put("Smtp Ssl Setting", "Configuração de Smtp Ssl");
		m.put("Smtp With Ssl", "Smtp com Ssl");
		m.put("Some builds are {0}", "Alguns builds estão {0}");
		m.put("Some jobs are hidden due to permission policy", "Alguns trabalhos estão ocultos devido à política de permissão");
		m.put("Some one changed the content you are editing. Reload the page and try again.", "Alguém alterou o conteúdo que você está editando. Recarregue a página e tente novamente.");
		m.put("Some other pull requests are opening to this branch", "Outras solicitações de pull estão abertas para este branch");
		m.put("Some projects might be hidden due to permission policy", "Alguns projetos podem estar ocultos devido à política de permissão");
		m.put("Some related commits of the code comment is missing", "Alguns commits relacionados ao comentário de código estão faltando");
		m.put("Some related commits of the pull request are missing", "Alguns commits relacionados à solicitação de pull estão faltando");
		m.put("Some required builds not passed", "Algumas builds obrigatórias não foram aprovadas");
		m.put("Someone made below change since you started editing", "Alguém fez a alteração abaixo desde que você começou a editar");
		m.put("Sort", "Ordenar");
		m.put("Source", "Fonte");
		m.put("Source Docker Image", "Imagem Docker de Origem");
		m.put("Source Lines", "Linhas de Origem");
		m.put("Source Path", "Caminho de Origem");
		m.put("Source branch already exists", "O branch de origem já existe");
		m.put("Source branch already merged into target branch", "O branch de origem já foi mesclado no branch de destino");
		m.put("Source branch commits will be rebased onto target branch", "Os commits do branch de origem serão rebaseados no branch de destino");
		m.put("Source branch is default branch", "O branch de origem é o branch padrão");
		m.put("Source branch is outdated", "O branch de origem está desatualizado");
		m.put("Source branch no longer exists", "O branch de origem não existe mais");
		m.put("Source branch updated successfully", "Branch de origem atualizado com sucesso");
		m.put("Source project no longer exists", "O projeto de origem não existe mais");
		m.put("Specified Value", "Valor Especificado");
		m.put("Specified choices", "Escolhas Especificadas");
		m.put("Specified default value", "Valor padrão especificado");
		m.put("Specified fields", "Campos Especificados");
		m.put("Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>. In case your ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Especifica a URL LDAP do servidor Active Directory, por exemplo: <i>ldap://ad-server</i>, ou <i>ldaps://ad-server</i>. Caso seu servidor LDAP esteja usando um certificado autoassinado para conexão ldaps, será necessário <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configurar o OneDev para confiar no certificado</a>");
		m.put("Specifies LDAP URL, for example: <i>ldap://localhost</i>, or <i>ldaps://localhost</i>. In caseyour ldap server is using a self-signed certificate for ldaps connection, you will need to <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configure OneDev to trust the certificate</a>", 
			"Especifica a URL LDAP, por exemplo: <i>ldap://localhost</i>, ou <i>ldaps://localhost</i>. Caso seu servidor LDAP esteja usando um certificado autoassinado para conexão ldaps, será necessário <a href='https://docs.onedev.io/administration-guide/trust-self-signed-certificates' target='_blank'>configurar o OneDev para confiar no certificado</a>");
		m.put("Specifies base nodes for user search. For example: <i>ou=users, dc=example, dc=com</i>", 
			"Especifica os nós base para busca de usuários. Por exemplo: <i>ou=users, dc=example, dc=com</i>");
		m.put("Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups", 
			"Especifica o nome do atributo dentro da entrada LDAP do usuário cujo valor contém os nomes distintos dos grupos pertencentes. Por exemplo, alguns servidores LDAP usam o atributo <i>memberOf</i> para listar grupos");
		m.put("Specifies password of above manager DN", "Especifica a senha do DN do gerente acima");
		m.put("Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>", 
			"Especifica o atributo que contém o nome do grupo dentro da entrada LDAP do grupo encontrado. O valor deste atributo será mapeado para um grupo do OneDev. Este atributo normalmente é definido como <i>cn</i>");
		m.put("Specify .net TRX test result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>TestResults/*.trx</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo de resultado de teste .net TRX relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo <tt>TestResults/*.trx</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is access token with code write permission over above projects. Commits, issues, and pull requests will also be created under name of the access token owner", 
			"Especifique o <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> cujo valor é um token de acesso com permissão de escrita de código sobre os projetos acima. Commits, issues e pull requests também serão criados sob o nome do proprietário do token de acesso");
		m.put("Specify <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> json output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with clippy json output option, for instance <code>cargo clippy --message-format json>check-result.json</code>. Use * or ? for pattern match", 
			"Especifique o arquivo de saída json do <a href='https://github.com/rust-lang/rust-clippy'>rust clippy</a> relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>. Este arquivo pode ser gerado com a opção de saída json do clippy, por exemplo <code>cargo clippy --message-format json>check-result.json</code>. Use * ou ? para correspondência de padrão");
		m.put("Specify Build Options", "Especifique Opções de Build");
		m.put("Specify CPD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo xml de resultado CPD relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo, <tt>target/cpd.xml</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify Commit Message", "Especifique a Mensagem de Commit");
		m.put("Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo de relatório ESLint no formato checkstyle no <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>. Este arquivo pode ser gerado com a opção ESLint <tt>'-f checkstyle'</tt> e <tt>'-o'</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify GitHub API url, for instance <tt>https://api.github.com</tt>", "Especifique a URL da API do GitHub, por exemplo <tt>https://api.github.com</tt>");
		m.put("Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>", "Especifique a URL da API do GitLab, por exemplo <tt>https://gitlab.example.com/api/v4</tt>");
		m.put("Specify Gitea API url, for instance <tt>https://gitea.example.com/api/v1</tt>", "Especifique a URL da API do Gitea, por exemplo <tt>https://gitea.example.com/api/v1</tt>");
		m.put("Specify GoogleTest XML result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This report can be generated with environment variable <tt>GTEST_OUTPUT</tt> when running tests, For instance, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * or ? for pattern match", 
			"Especifique o arquivo XML de resultado do GoogleTest relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>. Este relatório pode ser gerado com a variável de ambiente <tt>GTEST_OUTPUT</tt> ao executar testes, por exemplo, <code>export GTEST_OUTPUT=&quot;xml:gtest-result.xml&quot;</code>. Use * ou ? para correspondência de padrão");
		m.put("Specify IMAP user name.<br><b class='text-danger'>NOTE: </b> This account should be able to receive emails sent to system email address specified above", 
			"Especifique o nome de usuário IMAP.<br><b class='text-danger'>NOTA: </b> Esta conta deve ser capaz de receber emails enviados para o endereço de email do sistema especificado acima");
		m.put("Specify JUnit test result file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo de resultado de teste JUnit no formato XML relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo <tt>target/surefire-reports/TEST-*.xml</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo de relatório de cobertura xml do JaCoCo relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo, <tt>target/site/jacoco/jacoco.xml</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify Jest coverage report file in clover format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo de relatório de cobertura do Jest no formato clover relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo <tt>coverage/clover.xml</tt>. Este arquivo pode ser gerado com a opção Jest <tt>'--coverage'</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify Jest test result file in json format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo de resultado de teste do Jest no formato json relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>. Este arquivo pode ser gerado via opção Jest <tt>'--json'</tt> e <tt>'--outputFile'</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify OCI layout directory of the image to scan. This directory can be generated via build image step or pull image step. It should be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Especifique o diretório de layout OCI da imagem para escanear. Este diretório pode ser gerado via etapa de build de imagem ou etapa de pull de imagem. Deve ser relativo ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>workspace do job</a>");
		m.put("Specify OCI layout directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to push from", 
			"Especifique o diretório de layout OCI relativo ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>workspace do job</a> para fazer o push");
		m.put("Specify OpenID scopes to request", "Especifique os escopos OpenID a serem solicitados");
		m.put("Specify PMD result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/pmd.xml</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo xml de resultado PMD relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo, <tt>target/pmd.xml</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify PowerShell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br><b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of your script<br>", 
			"Especifique os comandos PowerShell para executar no <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>workspace do job</a>.<br><b class='text-warning'>NOTA: </b> O OneDev verifica o código de saída do script para determinar se a etapa foi bem-sucedida. Como o PowerShell sempre sai com 0 mesmo que haja erros no script, você deve lidar com erros no script e sair com um código diferente de zero, ou adicionar a linha <code>$ErrorActionPreference = &quot;Stop&quot;</code> no início do seu script<br>");
		m.put("Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with <i>-o</i> option. Use * or ? for pattern match", 
			"Especifique o arquivo de saída de diagnósticos Roslynator no formato XML relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>. Este arquivo pode ser gerado com a opção <i>-o</i>. Use * ou ? para correspondência de padrão");
		m.put("Specify Shell/Batch Commands to Run", "Especifique Comandos Shell/Batch para Executar");
		m.put("Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo xml de resultado SpotBugs relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo, <tt>target/spotbugsXml.xml</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify System Settings", "Especifique Configurações do Sistema");
		m.put("Specify URL of remote git repository. Only http/https protocol is supported", "Especifique a URL do repositório git remoto. Apenas o protocolo http/https é suportado");
		m.put("Specify YouTrack login name. This account should have permission to:<ul><li>Read full information and issues of the projects you want to import<li>Read issue tags<li>Read user basic information</ul>", 
			"Especifique o nome de login do YouTrack. Esta conta deve ter permissão para:<ul><li>Ler informações completas e issues dos projetos que você deseja importar<li>Ler tags de issues<li>Ler informações básicas de usuários</ul>");
		m.put("Specify YouTrack password or access token for above user", "Especifique a senha ou token de acesso do usuário acima");
		m.put("Specify a &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;regular expression&lt;/a&gt; to match issue references. For instance:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;", 
			"Especifique uma &lt;a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'&gt;expressão regular&lt;/a&gt; para corresponder referências de issues. Por exemplo:&lt;br&gt; &lt;em&gt;(^|\\W)([A-Z][A-Z]+-\\d+)(?=\\W|$)&lt;/em&gt;");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> after issue number", 
			"Especifique uma <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>expressão regular</a> após o número da issue");
		m.put("Specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression</a> before issue number", 
			"Especifique uma <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>expressão regular</a> antes do número da issue");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> para ser usado como chave privada SSH");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> para ser usado como token de acesso");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec from above project if its code is not publicly accessible", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> para ser usado como token de acesso para importar a especificação de build do projeto acima, caso seu código não seja publicamente acessível");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> para ser usado como senha ou token de acesso do registro");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token to access remote repository", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> para ser usado como senha ou token de acesso para acessar o repositório remoto");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> para ser usado como chave privada para autenticação SSH. <b class='text-info'>NOTA:</b> Chave privada com frase secreta não é suportada");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key of above user for SSH authentication. <b class='text-info'>NOTE:</b> Private key with passphrase is not supported", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> para ser usado como chave privada do usuário acima para autenticação SSH. <b class='text-info'>NOTA:</b> Chave privada com frase secreta não é suportada");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with management permission for above project. Note that access token is not required if sync to current or child project and build commit is reachable from default branch", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> cujo valor é um token de acesso com permissão de gerenciamento para o projeto acima. Note que o token de acesso não é necessário se a sincronização for para o projeto atual ou filho e o commit de build for alcançável a partir do branch padrão");
		m.put("Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Especifique um <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>segredo do job</a> cujo valor é um token de acesso com permissão de upload de cache para o projeto acima. Note que esta propriedade não é necessária se o upload de cache for para o projeto atual ou filho e o commit de build for alcançável a partir do branch padrão");
		m.put("Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute", 
			"Especifique um <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> para disparar o job automaticamente. <b class='text-info'>Nota:</b> Para economizar recursos, os segundos na expressão cron serão ignorados, e o intervalo mínimo de agendamento é de um minuto");
		m.put("Specify a Docker Image to Test Against", "Especifique uma Imagem Docker para Testar");
		m.put("Specify a custom field of Enum type", "Especifique um campo personalizado do tipo Enum");
		m.put("Specify a default query to filter/order fixed issues of specified jobs", "Especifique uma consulta padrão para filtrar/ordenar issues resolvidas dos jobs especificados");
		m.put("Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into", 
			"Especifique um arquivo relativo ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>workspace do job</a> para gravar o checksum");
		m.put("Specify a multi-value user field to hold assignees information.<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique um campo de usuário multi-valor para armazenar informações de responsáveis.<b>NOTA: </b> Você pode personalizar os campos de issues do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify a multi-value user field to hold assignees information.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique um campo de usuário multi-valor para armazenar informações de responsáveis.<br><b>NOTA: </b> Você pode personalizar os campos de issues do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify a path inside container to be used as mount target", "Especifique um caminho dentro do container para ser usado como destino de montagem");
		m.put("Specify a path relative to job workspace to be used as mount source. Leave empty to mount job workspace itself", 
			"Especifique um caminho relativo ao workspace do job para ser usado como fonte de montagem. Deixe vazio para montar o próprio workspace do job");
		m.put("Specify a secret to be used as access token to create issue in above project if it is not publicly accessible", 
			"Especifique um segredo para ser usado como token de acesso para criar uma issue no projeto acima, caso não seja publicamente acessível");
		m.put("Specify a secret to be used as access token to retrieve artifacts from above project. If not specified, project artifacts will be accessed anonymously", 
			"Especifique um segredo para ser usado como token de acesso para recuperar artefatos do projeto acima. Se não especificado, os artefatos do projeto serão acessados anonimamente");
		m.put("Specify a secret to be used as access token to trigger job in above project", "Especifique um segredo para ser usado como token de acesso para acionar o trabalho no projeto acima");
		m.put("Specify a secret whose value is an access token with upload cache permission for above project. Note that this property is not required if upload cache to current or child project and build commit is reachable from default branch", 
			"Especifique um segredo cujo valor é um token de acesso com permissão de upload de cache para o projeto acima. Note que esta propriedade não é necessária se o upload de cache for para o projeto atual ou filho e o commit de build for alcançável a partir do branch padrão");
		m.put("Specify absolute path to the config file used by kubectl to access the cluster. Leave empty to have kubectl determining cluster access information automatically", 
			"Especifique o caminho absoluto para o arquivo de configuração usado pelo kubectl para acessar o cluster. Deixe vazio para que o kubectl determine automaticamente as informações de acesso ao cluster");
		m.put("Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. If left empty, OneDev will try to find the utility from system path", 
			"Especifique o caminho absoluto para a utilidade kubectl, por exemplo: <i>/usr/bin/kubectl</i>. Se deixado vazio, o OneDev tentará encontrar a utilidade no caminho do sistema");
		m.put("Specify account name to login to Gmail to send/receive email", "Especifique o nome da conta para fazer login no Gmail para enviar/receber email");
		m.put("Specify additional users able to access this confidential issue besides those granted via role. Users mentioned in the issue will be authorized automatically", 
			"Especifique usuários adicionais capazes de acessar esta issue confidencial além daqueles concedidos via função. Usuários mencionados na issue serão autorizados automaticamente");
		m.put("Specify agents applicable for this executor", "Especifique agentes aplicáveis para este executor");
		m.put("Specify allowed <a href='https://spdx.org/licenses/' target='_blank'>spdx license identifiers</a> <span class='text-warning'>separated by comma</span>", 
			"Especifique identificadores de licença <a href='https://spdx.org/licenses/' target='_blank'>spdx permitidos</a> <span class='text-warning'>separados por vírgula</span>");
		m.put("Specify an email address sharing same inbox as the system email address in mail setting definition. Emails sent to this address will be created as issues in this project. The default value takes form of <tt>&lt;system email address name&gt;+&lt;project path&gt;@&lt;system email address domain&gt;</tt>", 
			"Especifique um endereço de email que compartilhe a mesma caixa de entrada que o endereço de email do sistema na definição de configuração de email. Emails enviados para este endereço serão criados como issues neste projeto. O valor padrão assume a forma de <tt>&lt;nome do endereço de email do sistema&gt;+&lt;caminho do projeto&gt;@&lt;domínio do endereço de email do sistema&gt;</tt>");
		m.put("Specify applicable projects for above option. Multiple projects should be separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Especifique projetos aplicáveis para a opção acima. Múltiplos projetos devem ser separados por espaço. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de padrão de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para todos os projetos");
		m.put("Specify applicable projects separated by space. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty for all projects", 
			"Especifique projetos aplicáveis separados por espaço. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de padrão de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para todos os projetos");
		m.put("Specify application (client) ID of the app registered in Entra ID", "Especifique o ID do aplicativo (cliente) do app registrado no Entra ID");
		m.put("Specify arguments for imagetools. For instance <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>", 
			"Especifique argumentos para imagetools. Por exemplo <code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>");
		m.put("Specify artifacts to retrieve into <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. Only published artifacts (via artifact publish step) can be retrieved.", 
			"Especifique artefatos para recuperar no <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>. Apenas artefatos publicados (via etapa de publicação de artefatos) podem ser recuperados.");
		m.put("Specify at least 10 alphanumeric chars to be used as secret, and then add an inbound parse entry at SendGrid side:<ul><li><code>Destination URL</code> should be set to <i>&lt;OneDev root url&gt;/~sendgrid/&lt;secret&gt;</i>, for instance, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note that in production environment, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https should be enabled</a> to protect the secret</li><li><code>Receiving domain</code> should be the same as domain part of system email address specified above</li><li>Option <code>POST the raw, full MIME message</code> is enabled</li></ul>", 
			"Especifique pelo menos 10 caracteres alfanuméricos para serem usados como segredo e, em seguida, adicione uma entrada de análise de entrada no lado do SendGrid:<ul><li><code>URL de Destino</code> deve ser configurado como <i>&lt;URL raiz do OneDev&gt;/~sendgrid/&lt;segredo&gt;</i>, por exemplo, <i>https://onedev.example.com/~sendgrid/1234567890</i>. Note que em ambiente de produção, <a href='https://docs.onedev.io/administration-guide/https-setup' target='_blank'>https deve ser habilitado</a> para proteger o segredo</li><li><code>Domínio de Recebimento</code> deve ser o mesmo que a parte do domínio do endereço de email do sistema especificado acima</li><li>Opção <code>POST the raw, full MIME message</code> está habilitada</li></ul>");
		m.put("Specify base nodes for user search. For example: <i>cn=Users, dc=example, dc=com</i>", 
			"Especifique os nós base para busca de usuários. Por exemplo: <i>cn=Users, dc=example, dc=com</i>");
		m.put("Specify branch to commit suggested change", "Especifique o branch para fazer commit da alteração sugerida");
		m.put("Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Especifique o branch para executar o trabalho. Branch ou tag podem ser especificados, mas não ambos. O branch padrão será usado se ambos não forem especificados");
		m.put("Specify branch, tag or commit in above project to import build spec from", "Especifique o branch, tag ou commit no projeto acima para importar a especificação de build");
		m.put("Specify by Build Number", "Especifique por Número de Build");
		m.put("Specify cache upload strategy after build successful. <var>Upload If Not Hit</var> means to upload when cache is not found with cache key (not load keys), and <var>Upload If Changed</var> means to upload if some files in cache path are changed", 
			"Especifique a estratégia de upload de cache após o build bem-sucedido. <var>Upload If Not Hit</var> significa fazer upload quando o cache não for encontrado com a chave de cache (não chaves de carregamento), e <var>Upload If Changed</var> significa fazer upload se alguns arquivos no caminho do cache forem alterados");
		m.put("Specify certificate to trust if you are using self-signed certificate for remote repository", 
			"Especifique o certificado para confiar caso esteja usando certificado autoassinado para o repositório remoto");
		m.put("Specify certificates to trust if you are using self-signed certificates for your docker registries", 
			"Especifique os certificados para confiar caso esteja usando certificados autoassinados para seus registros docker");
		m.put("Specify checkstyle result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/checkstyle-result.xml</tt>. Refer to <a href='https://checkstyle.org/'>checkstyle documentation</a> on how to generate the result xml file. Use * or ? for pattern match", 
			"Especifique o arquivo xml de resultado checkstyle relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo, <tt>target/checkstyle-result.xml</tt>. Consulte a <a href='https://checkstyle.org/'>documentação do checkstyle</a> sobre como gerar o arquivo xml de resultado. Use * ou ? para correspondência de padrão");
		m.put("Specify client secret of the app registered in Entra ID", "Especifique o segredo do cliente do app registrado no Entra ID");
		m.put("Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/clover/clover.xml</tt>. Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> on how to generate clover xml file. Use * or ? for pattern match", 
			"Especifique o arquivo de relatório de cobertura xml do clover relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo, <tt>target/site/clover/clover.xml</tt>. Consulte a <a href='https://openclover.org/documentation'>documentação do OpenClover</a> sobre como gerar o arquivo xml do clover. Use * ou ? para correspondência de padrão");
		m.put("Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match", 
			"Especifique o arquivo de relatório de cobertura xml do cobertura relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>workspace do job</a>, por exemplo, <tt>target/site/cobertura/coverage.xml</tt>. Use * ou ? para correspondência de padrão");
		m.put("Specify color of the state for displaying purpose", "Especifique a cor do estado para fins de exibição");
		m.put("Specify columns of the board. Each column corresponds to a value of the issue field specified above", 
			"Especifique as colunas do quadro. Cada coluna corresponde a um valor do campo de issue especificado acima");
		m.put("Specify command to check readiness of the service. This command will be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be executed repeatedly until a zero code is returned to indicate service ready", 
			"Especifique o comando para verificar a prontidão do serviço. Este comando será interpretado pelo cmd.exe em imagens do Windows e pelo shell em imagens do Linux. Ele será executado repetidamente até que um código zero seja retornado para indicar que o serviço está pronto");
		m.put("Specify commands to be executed on remote machine. <b class='text-warning'>Note:</b> user environments will not be picked up when execute these commands, set up them explicitly in commands if necessary", 
			"Especifique os comandos a serem executados na máquina remota. <b class='text-warning'>Nota:</b> os ambientes do usuário não serão capturados ao executar esses comandos, configure-os explicitamente nos comandos, se necessário");
		m.put("Specify condition to retry build upon failure", "Especifique a condição para tentar novamente a construção em caso de falha");
		m.put("Specify configuration discovery url of your OpenID provider, for instance: <code>https://openid.example.com/.well-known/openid-configuration</code>. Make sure to use HTTPS protocol as OneDev relies on TLS encryption to ensure token validity", 
			"Especifique a URL de descoberta de configuração do seu provedor OpenID, por exemplo: <code>https://openid.example.com/.well-known/openid-configuration</code>. Certifique-se de usar o protocolo HTTPS, pois o OneDev depende da criptografia TLS para garantir a validade do token");
		m.put("Specify container image to execute commands inside", "Especifique a imagem do contêiner para executar os comandos");
		m.put("Specify container image to run", "Especifique a imagem do contêiner para executar");
		m.put("Specify cppcheck xml result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match", 
			"Especifique o arquivo de resultados xml do cppcheck relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a>. Este arquivo pode ser gerado com a opção de saída xml do cppcheck, por exemplo <code>cppcheck src --xml 2>check-result.xml</code>. Use * ou ? para correspondência de padrão");
		m.put("Specify cpu request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Especifique a solicitação de CPU para cada trabalho/serviço usando este executor. Verifique <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>gerenciamento de recursos do Kubernetes</a> para detalhes");
		m.put("Specify default assignees of pull requests submitted to this project. Only users with the write code permission to the project can be selected", 
			"Especifique os responsáveis padrão pelos pull requests enviados a este projeto. Somente usuários com permissão para escrever código no projeto podem ser selecionados");
		m.put("Specify default merge strategy of pull requests submitted to this project", "Especifique a estratégia de mesclagem padrão dos pull requests enviados a este projeto");
		m.put("Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple destinations should be separated with space", 
			"Especifique os destinos, por exemplo <tt>registry-server:5000/myorg/myrepo:latest</tt>. Certifique-se de usar <b>o mesmo host</b> especificado na URL do servidor nas configurações do sistema se quiser enviar para o registro interno, ou simplesmente use o formato <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Múltiplos destinos devem ser separados por espaço");
		m.put("Specify directory (tenant) ID of the app registered in Entra ID", "Especifique o ID do diretório (tenant) do aplicativo registrado no Entra ID");
		m.put("Specify directory relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Especifique o diretório relativo ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a> para armazenar o layout OCI");
		m.put("Specify docker image of the service", "Especifique a imagem do docker do serviço");
		m.put("Specify dockerx builder used to build docker image. OneDev will create the builder automatically if it does not exist. Check <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> on how to customize the builder for instance to allow publishing to insecure registries", 
			"Especifique o builder dockerx usado para construir a imagem do docker. O OneDev criará o builder automaticamente se ele não existir. Verifique <a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>este tutorial</a> sobre como personalizar o builder, por exemplo, para permitir a publicação em registros inseguros");
		m.put("Specify email addresses to send invitations, with one per line", "Especifique os endereços de email para enviar convites, com um por linha");
		m.put("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\"", 
			"Especifique o tempo estimado <b class='text-warning'>somente para este problema</b>, sem contar \"{0}\"");
		m.put("Specify fields of various issues created by Renovate to orchestrate the dependency update", 
			"Especifique os campos de vários problemas criados pelo Renovate para orquestrar a atualização de dependências");
		m.put("Specify fields to be displayed in the issue list", "Especifique os campos a serem exibidos na lista de problemas");
		m.put("Specify fields to display in board card", "Especifique os campos a serem exibidos no cartão do quadro");
		m.put("Specify files relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. Use * or ? for pattern match", 
			"Especifique os arquivos relativos ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> a serem publicados. Use * ou ? para correspondência de padrão");
		m.put("Specify files to create md5 checksum from. Multiple files should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Especifique os arquivos para criar checksum md5. Múltiplos arquivos devem ser separados por espaço. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Padrões Globstar</a> são aceitos. Arquivos não absolutos são considerados relativos ao <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>");
		m.put("Specify files under above directory to be published", "Especifique arquivos no diretório acima para serem publicados");
		m.put("Specify files under above directory to be published. Use * or ? for pattern match. <b>NOTE:</b> <code>index.html</code> should be included in these files to be served as site start page", 
			"Especifique os arquivos no diretório acima a serem publicados. Use * ou ? para correspondência de padrão. <b>NOTA:</b> <code>index.html</code> deve ser incluído nesses arquivos para ser servido como página inicial do site");
		m.put("Specify group to import from. Leave empty to import from projects under current account", 
			"Especifique o grupo para importar. Deixe vazio para importar dos projetos da conta atual");
		m.put("Specify how to map GitHub issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique como mapear os rótulos de problemas do GitHub para os campos personalizados do OneDev.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify how to map GitLab issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique como mapear os rótulos de problemas do GitLab para os campos personalizados do OneDev.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify how to map Gitea issue labels to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique como mapear os rótulos de problemas do Gitea para os campos personalizados do OneDev.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify how to map JIRA issue priorities to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique como mapear as prioridades de problemas do JIRA para os campos personalizados do OneDev.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify how to map JIRA issue statuses to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique como mapear os estados de problemas do JIRA para os campos personalizados do OneDev.<br><b>NOTA: </b> Você pode personalizar os estados de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify how to map JIRA issue types to OneDev custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique como mapear os tipos de problemas do JIRA para os campos personalizados do OneDev.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify how to map YouTrack issue fields to OneDev. Unmapped fields will be reflected in issue description.<br><b>Note: </b><ul><li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, for instance <tt>Priority::Critical</tt><li>You may customize OneDev issue fields in case there is no appropriate option here</ul>", 
			"Especifique como mapear os campos de problemas do YouTrack para o OneDev. Campos não mapeados serão refletidos na descrição do problema.<br><b>Nota: </b><ul><li>O campo de enumeração precisa ser mapeado na forma de <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, por exemplo <tt>Priority::Critical</tt><li>Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui</ul>");
		m.put("Specify how to map YouTrack issue links to OneDev issue links.<br><b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here", 
			"Especifique como mapear os links de problemas do YouTrack para os links de problemas do OneDev.<br><b>NOTA: </b> Você pode personalizar os links de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify how to map YouTrack issue state to OneDev issue state. Unmapped states will use the initial state in OneDev.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique como mapear o estado de problemas do YouTrack para o estado de problemas do OneDev. Estados não mapeados usarão o estado inicial no OneDev.<br><b>NOTA: </b> Você pode personalizar os estados de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify how to map YouTrack issue tags to OneDev issue custom fields.<br><b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here", 
			"Especifique como mapear as tags de problemas do YouTrack para os campos personalizados de problemas do OneDev.<br><b>NOTA: </b> Você pode personalizar os campos de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify image on the login button", "Especifique a imagem no botão de login");
		m.put("Specify image tag to pull from, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to pull from built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Especifique a tag da imagem para puxar, por exemplo <tt>registry-server:5000/myorg/myrepo:latest</tt>. Certifique-se de usar <b>o mesmo host</b> especificado na URL do servidor nas configurações do sistema se quiser puxar do registro interno, ou simplesmente use o formato <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>");
		m.put("Specify image tag to push to, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>", 
			"Especifique a tag da imagem para enviar, por exemplo <tt>registry-server:5000/myorg/myrepo:latest</tt>. Certifique-se de usar <b>o mesmo host</b> especificado na URL do servidor nas configurações do sistema se quiser enviar para o registro interno, ou simplesmente use o formato <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>");
		m.put("Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple tags should be separated with space", 
			"Especifique as tags da imagem para enviar, por exemplo <tt>registry-server:5000/myorg/myrepo:latest</tt>. Certifique-se de usar <b>o mesmo host</b> especificado na URL do servidor nas configurações do sistema se quiser enviar para o registro interno, ou simplesmente use o formato <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Múltiplas tags devem ser separadas por espaço");
		m.put("Specify import option", "Especifique a opção de importação");
		m.put("Specify incoming email poll interval in seconds", "Especifique o intervalo de pesquisa de email recebido em segundos");
		m.put("Specify issue creation settings. For a particular sender and project, the first matching entry will take effect. Issue creation will be disallowed if no matching entry found", 
			"Especifique as configurações de criação de problemas. Para um remetente e projeto específicos, a primeira entrada correspondente terá efeito. A criação de problemas será desativada se nenhuma entrada correspondente for encontrada");
		m.put("Specify issue field to identify different columns of the board. Only state and single-valued enumeration field can be used here", 
			"Especifique o campo de problema para identificar diferentes colunas do quadro. Somente estado e campo de enumeração de valor único podem ser usados aqui");
		m.put("Specify links to be displayed in the issue list", "Especifique os links a serem exibidos na lista de problemas");
		m.put("Specify links to display in board card", "Especifique os links a serem exibidos no cartão do quadro");
		m.put("Specify manager DN to authenticate OneDev itself to Active Directory. The manager DN should be specified in form of <i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>manager@example.com</i>", 
			"Especifique o DN do gerente para autenticar o próprio OneDev no Active Directory. O DN do gerente deve ser especificado na forma de <i>&lt;account name&gt;@&lt;domain&gt;</i>, por exemplo: <i>manager@example.com</i>");
		m.put("Specify manager DN to authenticate OneDev itself to LDAP server", "Especifique o DN do gerente para autenticar o próprio OneDev no servidor LDAP");
		m.put("Specify markdown file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published", 
			"Especifique o arquivo markdown relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a> a ser publicado");
		m.put("Specify max git LFS file size in mega bytes", "Especifique o tamanho máximo do arquivo git LFS em megabytes");
		m.put("Specify max number of CPU intensive tasks the server can run concurrently, such as Git repository pull/push, repository index, etc.", 
			"Especifique o número máximo de tarefas intensivas de CPU que o servidor pode executar simultaneamente, como pull/push de repositório Git, indexação de repositório, etc.");
		m.put("Specify max number of jobs this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Especifique o número máximo de trabalhos que este executor pode executar simultaneamente em cada agente correspondente. Deixe vazio para definir como núcleos de CPU do agente");
		m.put("Specify max number of jobs this executor can run concurrently. Leave empty to set as CPU cores", 
			"Especifique o número máximo de trabalhos que este executor pode executar simultaneamente. Deixe vazio para definir como núcleos de CPU");
		m.put("Specify max number of jobs/services this executor can run concurrently on each matched agent. Leave empty to set as agent CPU cores", 
			"Especifique o número máximo de trabalhos/serviços que este executor pode executar simultaneamente em cada agente correspondente. Deixe vazio para definir como núcleos de CPU do agente");
		m.put("Specify max number of jobs/services this executor can run concurrently. Leave empty to set as CPU cores", 
			"Especifique o número máximo de trabalhos/serviços que este executor pode executar simultaneamente. Deixe vazio para definir como núcleos de CPU");
		m.put("Specify max size of uploaded file in mega bytes via web interface. This applies to file uploaded to repository, markdown content (issue comment etc), and build artifacts", 
			"Especifique o tamanho máximo do arquivo enviado via interface web em megabytes. Isso se aplica ao arquivo enviado para o repositório, conteúdo markdown (comentário de problema etc.) e artefatos de construção");
		m.put("Specify memory request for each job/service using this executor. Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details", 
			"Especifique a solicitação de memória para cada trabalho/serviço usando este executor. Verifique <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>gerenciamento de recursos do Kubernetes</a> para detalhes");
		m.put("Specify mypy output file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated by redirecting mypy output <b>without option '--pretty'</b>, for instance <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * or ? for pattern match", 
			"Especifique o arquivo de saída do mypy relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a>. Este arquivo pode ser gerado redirecionando a saída do mypy <b>sem a opção '--pretty'</b>, por exemplo <code>mypy --exclude=.git --exclude=.venv . > mypy-output</code>. Use * ou ? para correspondência de padrão");
		m.put("Specify name of the branch", "Especifique o nome do branch");
		m.put("Specify name of the environment variable", "Especifique o nome da variável de ambiente");
		m.put("Specify name of the iteration", "Especifique o nome da iteração");
		m.put("Specify name of the job", "Especifique o nome do trabalho");
		m.put("Specify name of the report to be displayed in build detail page", "Especifique o nome do relatório a ser exibido na página de detalhes da construção");
		m.put("Specify name of the saved query", "Especifique o nome da consulta salva");
		m.put("Specify name of the service, which will be used as host name to access the service", 
			"Especifique o nome do serviço, que será usado como nome do host para acessar o serviço");
		m.put("Specify name of the tag", "Especifique o nome da tag");
		m.put("Specify network timeout in seconds when authenticate through this system", "Especifique o tempo limite de rede em segundos ao autenticar através deste sistema");
		m.put("Specify node selector of this locator", "Especifique o seletor de nó deste localizador");
		m.put("Specify password or access token of specified registry", "Especifique a senha ou token de acesso do registro especificado");
		m.put("Specify password to authenticate with", "Especifique a senha para autenticar");
		m.put("Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>", "Especifique o caminho para o executável curl, por exemplo: <tt>/usr/bin/curl</tt>");
		m.put("Specify path to git executable, for instance: <tt>/usr/bin/git</tt>", "Especifique o caminho para o executável git, por exemplo: <tt>/usr/bin/git</tt>");
		m.put("Specify powershell executable to be used", "Especifique o executável powershell a ser usado");
		m.put("Specify project to import build spec from", "Especifique o projeto para importar a especificação de construção");
		m.put("Specify project to import into at OneDev side", "Especifique o projeto para importar no lado do OneDev");
		m.put("Specify project to retrieve artifacts from", "Especifique o projeto para recuperar artefatos");
		m.put("Specify project to run job in", "Especifique o projeto para executar o trabalho");
		m.put("Specify projects", "Especifique os projetos");
		m.put("Specify projects to update dependencies. Leave empty for current project", "Especifique os projetos para atualizar dependências. Deixe vazio para o projeto atual");
		m.put("Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Especifique o arquivo de resultados json do pylint relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a>. Este arquivo pode ser gerado com a opção de formato de saída json do pylint, por exemplo <code>--exit-zero --output-format=json:pylint-result.json</code>. Observe que não falhamos o comando pylint em caso de violações, pois esta etapa falhará na construção com base no limite configurado. Use * ou ? para correspondência de padrão");
		m.put("Specify registry logins if necessary. For built-in registry, use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password", 
			"Especifique os logins do registro, se necessário. Para o registro interno, use <code>@server_url@</code> para a URL do registro, <code>@job_token@</code> para o nome de usuário e o token de acesso para a senha");
		m.put("Specify registry url. Leave empty for official registry", "Especifique a URL do registro. Deixe vazio para o registro oficial");
		m.put("Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout", 
			"Especifique o caminho relativo sob o <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a> para armazenar o layout OCI");
		m.put("Specify repositories", "Especifique os repositórios");
		m.put("Specify required reviewers if specified path is changed. Note that the user submitting the change is considered to reviewed the change automatically", 
			"Especifique os revisores necessários se o caminho especificado for alterado. Observe que o usuário que enviou a alteração é considerado automaticamente como tendo revisado a alteração");
		m.put("Specify root URL to access this server", "Especifique a URL raiz para acessar este servidor");
		m.put("Specify ruff json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. This file can be generated with ruff json output format option, for instance <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Note that we do not fail ruff command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match", 
			"Especifique o arquivo de resultados json do ruff relativo ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a>. Este arquivo pode ser gerado com a opção de formato de saída json do ruff, por exemplo <code>--exit-zero --output-format json --output-file ruff-result.json</code>. Observe que não falhamos o comando ruff em caso de violações, pois esta etapa falhará na construção com base no limite configurado. Use * ou ? para correspondência de padrão");
		m.put("Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Especifique os comandos shell (em Linux/Unix) ou comandos batch (em Windows) para executar sob o <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>");
		m.put("Specify shell commands to execute under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Especifique os comandos shell para executar sob o <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>");
		m.put("Specify shell to be used", "Especifique o shell a ser usado");
		m.put("Specify source param for SCP command, for instance <code>app.tar.gz</code>", "Especifique o parâmetro de origem para o comando SCP, por exemplo <code>app.tar.gz</code>");
		m.put("Specify space separated refs to pull from remote. '*' can be used in ref name for wildcard match<br><b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update branches/tags via this step", 
			"Especifique os refs separados por espaço para puxar do remoto. '*' pode ser usado no nome do ref para correspondência de curinga<br><b class='text-danger'>NOTA:</b> a regra de proteção de branch/tag será ignorada ao atualizar branches/tags através desta etapa");
		m.put("Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Especifique os branches separados por espaço a serem protegidos. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude", 
			"Especifique os trabalhos separados por espaço. Use '*' ou '?' para correspondência de curinga. Prefixe com '-' para excluir");
		m.put("Specify space-separated jobs. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts will be granted implicitly in matched jobs even if no other permissions are specified here", 
			"Especifique os trabalhos separados por espaço. Use '*' ou '?' para correspondência de curinga. Prefixe com '-' para excluir. <b class='text-danger'>NOTA: </b> Permissão para acessar artefatos de construção será concedida implicitamente nos trabalhos correspondentes, mesmo que nenhuma outra permissão seja especificada aqui");
		m.put("Specify space-separated paths to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Especifique os caminhos separados por espaço a serem protegidos. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir");
		m.put("Specify space-separated projects applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all projects", 
			"Especifique os projetos separados por espaço aplicáveis para esta entrada. Use '*' ou '?' para correspondência de curinga. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos os projetos");
		m.put("Specify space-separated sender email addresses applicable for this entry. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all senders", 
			"Especifique os endereços de email do remetente separados por espaço aplicáveis para esta entrada. Use '*' ou '?' para correspondência de curinga. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos os remetentes");
		m.put("Specify space-separated tags to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Especifique as tags separadas por espaço a serem protegidas. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>", 
			"Especifique a página inicial do relatório relativa ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a>, por exemplo: <tt>manual/index.md</tt>");
		m.put("Specify start page of the report relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html", 
			"Especifique a página inicial do relatório relativa ao <a href='https://docs.onedev.io/concepts#job-workspace'>espaço de trabalho do trabalho</a>, por exemplo: api/index.html");
		m.put("Specify storage size to request for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, for instance <i>10Gi</i>", 
			"Especifique o tamanho de armazenamento a ser solicitado para o volume de construção. O tamanho deve estar em conformidade com o <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>formato de capacidade de recurso do Kubernetes</a>, por exemplo <i>10Gi</i>");
		m.put("Specify tab width used to calculate column value of found problems in provided report", 
			"Especifique a largura da tabulação usada para calcular o valor da coluna dos problemas encontrados no relatório fornecido");
		m.put("Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified", 
			"Especifique a tag para executar o trabalho. Branch ou tag podem ser especificados, mas não ambos. O branch padrão será usado se ambos não forem especificados");
		m.put("Specify target param for SCP command, for instance <code>user@@host:/app</code>. <b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host", 
			"Especifique o parâmetro de destino para o comando SCP, por exemplo <code>user@@host:/app</code>. <b class='text-info'>NOTA:</b> Certifique-se de que o comando scp está instalado no host remoto");
		m.put("Specify text to replace matched issue references with, for instance: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Here $1 and $2 represent catpure groups in the example issue pattern (see issue pattern help)", 
			"Especifique o texto para substituir as referências de problemas correspondentes, por exemplo: &lt;br&gt;&lt;em&gt;$1&amp;lt;a href='http://track.example.com/issues/$2'&amp;gt;$2&amp;lt;/a&amp;gt;&lt;/em&gt; &lt;br&gt;Aqui $1 e $2 representam grupos capturados no padrão de problema de exemplo (veja a ajuda do padrão de problema)");
		m.put("Specify the condition current build must satisfy to execute this action", "Especifique a condição que a build atual deve satisfazer para executar esta ação");
		m.put("Specify the condition preserved builds must match", "Especifique a condição que as builds preservadas devem corresponder");
		m.put("Specify the private key (in PEM format) used by SSH server to establish connections with client", 
			"Especifique a chave privada (em formato PEM) usada pelo servidor SSH para estabelecer conexões com o cliente");
		m.put("Specify the strategy to retrieve group membership information. To give appropriate permissions to a LDAP group, a OneDev group with same name should be defined. Use strategy <tt>Do Not Retrieve Groups</tt> if you want to manage group memberships at OneDev side", 
			"Especifique a estratégia para recuperar informações de associação de grupo. Para conceder permissões apropriadas a um grupo LDAP, um grupo OneDev com o mesmo nome deve ser definido. Use a estratégia <tt>Não Recuperar Grupos</tt> se desejar gerenciar associações de grupo no lado do OneDev");
		m.put("Specify timeout in seconds when communicating with mail server", "Especifique o tempo limite em segundos ao se comunicar com o servidor de e-mail");
		m.put("Specify timeout in seconds. It counts from the time when job is submitted", "Especifique o tempo limite em segundos. Ele conta a partir do momento em que o trabalho é submetido");
		m.put("Specify title of the issue", "Especifique o título do problema");
		m.put("Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>", "Especifique a URL da API do YouTrack. Por exemplo <tt>http://localhost:8080/api</tt>");
		m.put("Specify user name of above machine for SSH authentication", "Especifique o nome de usuário da máquina acima para autenticação SSH");
		m.put("Specify user name of specified registry", "Especifique o nome de usuário do registro especificado");
		m.put("Specify user name of the registry", "Especifique o nome de usuário do registro");
		m.put("Specify user name to authenticate with", "Especifique o nome de usuário para autenticação");
		m.put("Specify value of the environment variable", "Especifique o valor da variável de ambiente");
		m.put("Specify web UI session timeout in minutes. Existing sessions will not be affected after changing this value.", 
			"Especificar o tempo limite da sessão da interface web em minutos. Sessões existentes não serão afetadas após a alteração deste valor.");
		m.put("Specify webhook url to post events", "Especifique a URL do webhook para postar eventos");
		m.put("Specify which issue state to use for closed GitHub issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique qual estado de problema usar para problemas fechados no GitHub.<br><b>NOTA: </b> Você pode personalizar os estados de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify which issue state to use for closed GitLab issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique qual estado de problema usar para problemas fechados no GitLab.<br><b>NOTA: </b> Você pode personalizar os estados de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify which issue state to use for closed Gitea issues.<br><b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here", 
			"Especifique qual estado de problema usar para problemas fechados no Gitea.<br><b>NOTA: </b> Você pode personalizar os estados de problemas do OneDev caso não haja uma opção apropriada aqui");
		m.put("Specify which states are considered as closed for various issues created by Renovate to orchestrate dependency update. Additionally, when Renovate closes the issue, OneDev will transit the issue to first state specified here", 
			"Especifique quais estados são considerados como fechados para vários problemas criados pelo Renovate para orquestrar a atualização de dependências. Além disso, quando o Renovate fecha o problema, o OneDev irá transitar o problema para o primeiro estado especificado aqui");
		m.put("Specify working days per week. This will affect parsing and displaying of working periods. For instance <tt>1w</tt> is the same as <tt>5d</tt> if this property is set to <tt>5</tt>", 
			"Especifique os dias úteis por semana. Isso afetará a análise e exibição de períodos de trabalho. Por exemplo, <tt>1w</tt> é o mesmo que <tt>5d</tt> se esta propriedade estiver definida como <tt>5</tt>");
		m.put("Specify working hours per day. This will affect parsing and displaying of working periods. For instance <tt>1d</tt> is the same as <tt>8h</tt> if this property is set to <tt>8</tt>", 
			"Especifique as horas de trabalho por dia. Isso afetará a análise e exibição de períodos de trabalho. Por exemplo, <tt>1d</tt> é o mesmo que <tt>8h</tt> se esta propriedade estiver definida como <tt>8</tt>");
		m.put("Spent", "Gasto");
		m.put("Spent Time", "Tempo Gasto");
		m.put("Spent Time Issue Field", "Campo de Problema de Tempo Gasto");
		m.put("Spent Time:", "Tempo Gasto:");
		m.put("Spent time / estimated time", "Tempo gasto / tempo estimado");
		m.put("Split", "Dividir");
		m.put("Split view", "Visualização Dividida");
		m.put("SpotBugs Report", "Relatório SpotBugs");
		m.put("Squash Source Branch Commits", "Squash Commits da Branch de Origem");
		m.put("Squash all commits from source branch into a single commit in target branch", "Unir todos os commits da branch de origem em um único commit na branch de destino");
		m.put("Squash source branch commits", "Unir commits da branch de origem");
		m.put("Ssh", "Ssh");
		m.put("Ssh Key", "Chave Ssh");
		m.put("Ssh Setting", "Configuração Ssh");
		m.put("Ssl Setting", "Configuração Ssl");
		m.put("Sso Connector", "Conector Sso");
		m.put("Sso Provider Bean", "Provedor Sso Bean");
		m.put("Start At", "Iniciar Em");
		m.put("Start Date", "Data de Início");
		m.put("Start Page", "Página Inicial");
		m.put("Start agent on remote Linux machine by running below command:", "Inicie o agente em uma máquina Linux remota executando o comando abaixo:");
		m.put("Start date", "Data de início");
		m.put("Start to watch once I am involved", "Começar a assistir assim que eu estiver envolvido");
		m.put("Start work", "Iniciar trabalho");
		m.put("Start/Due Date", "Data de Início/Entrega");
		m.put("State", "Estado");
		m.put("State Durations", "Durações de Estado");
		m.put("State Frequencies", "Frequências de Estado");
		m.put("State Spec", "Especificação de Estado");
		m.put("State Transitions", "Transições de Estado");
		m.put("State Trends", "Tendências de Estado");
		m.put("State of an issue is transited", "O estado de um problema é transitado");
		m.put("States", "Estados");
		m.put("Statistics", "Estatísticas");
		m.put("Stats", "Estatísticas");
		m.put("Stats Group", "Grupo de Estatísticas");
		m.put("Status", "Status");
		m.put("Status Code", "Código de Status");
		m.put("Status code", "Código de status");
		m.put("Status code other than 200 indicating the error type", "Código de status diferente de 200 indicando o tipo de erro");
		m.put("Step", "Etapa");
		m.put("Step Template", "Template de Etapa");
		m.put("Step Templates", "Templates de Etapas");
		m.put("Step {0} of {1}: ", "Etapa {0} de {1}:");
		m.put("Steps", "Etapas");
		m.put("Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>", 
			"As etapas serão executadas sequencialmente no mesmo nó, compartilhando o mesmo <a href='https://docs.onedev.io/concepts#job-workspace'>workspace de trabalho</a>");
		m.put("Stop work", "Parar trabalho");
		m.put("Stopwatch Overdue", "Cronômetro Atrasado");
		m.put("Storage Settings", "Configurações de Armazenamento");
		m.put("Storage file missing", "Arquivo de armazenamento ausente");
		m.put("Storage not found", "Armazenamento não encontrado");
		m.put("Stored with Git LFS", "Armazenado com Git LFS");
		m.put("Sub Keys", "Subchaves");
		m.put("Subject", "Assunto");
		m.put("Submit", "Enviar");
		m.put("Submit Reason", "Motivo do Envio");
		m.put("Submit Support Request", "Enviar Solicitação de Suporte");
		m.put("Submitted After", "Enviado Após");
		m.put("Submitted At", "Enviado Em");
		m.put("Submitted Before", "Enviado Antes");
		m.put("Submitted By", "Enviado Por");
		m.put("Submitted manually", "Enviado manualmente");
		m.put("Submitter", "Remetente");
		m.put("Subscription Key", "Chave de Assinatura");
		m.put("Subscription Management", "Gerenciamento de Assinatura");
		m.put("Subscription data", "Dados de Assinatura");
		m.put("Subscription key installed successfully", "Chave de assinatura instalada com sucesso");
		m.put("Subscription key not applicable: this key is intended to activate a trial subscription", 
			"Chave de assinatura não aplicável: esta chave destina-se a ativar uma assinatura de teste");
		m.put("Subscription key not applicable: this key is intended to renew a user based subscription", 
			"Chave de assinatura não aplicável: esta chave é destinada a renovar uma assinatura baseada em usuário");
		m.put("Subscription key not applicable: this key is intended to renew an unlimited users subscription", 
			"Chave de assinatura não aplicável: esta chave é destinada a renovar uma assinatura de usuários ilimitados");
		m.put("Subscription key not applicable: this key is intended to update licensee of an existing subscription", 
			"Chave de assinatura não aplicável: esta chave destina-se a atualizar o licenciado de uma assinatura existente");
		m.put("Success Rate", "Taxa de Sucesso");
		m.put("Successful", "Bem-sucedido");
		m.put("Suffix Pattern", "Padrão de Sufixo");
		m.put("Suggest changes", "Sugerir alterações");
		m.put("Suggested change", "Alteração sugerida");
		m.put("Suggestion is outdated either due to code change or pull request close", "A sugestão está desatualizada devido a alteração de código ou fechamento do pull request");
		m.put("Suggestions", "Sugestões");
		m.put("Summary", "Resumo");
		m.put("Support & Bug Report", "Suporte & Relatório de Bug");
		m.put("Support Request", "Solicitação de Suporte");
		m.put("Swap", "Trocar");
		m.put("Switch to HTTP(S)", "Alternar para HTTP(S)");
		m.put("Switch to SSH", "Alternar para SSH");
		m.put("Symbol Name", "Nome do Símbolo");
		m.put("Symbol name", "Nome do símbolo");
		m.put("Symbols", "Símbolos");
		m.put("Sync Replica Status and Back to Home", "Sincronizar Status da Réplica e Voltar para a Página Inicial");
		m.put("Sync Repository", "Sincronizar Repositório");
		m.put("Sync Timing of All Queried Issues", "Sincronizar Tempo de Todas as Questões Consultadas");
		m.put("Sync Timing of Selected Issues", "Sincronizar Tempo das Questões Selecionadas");
		m.put("Sync requested. Please check status after a while", "Sincronização solicitada. Por favor, verifique o status após algum tempo");
		m.put("Synchronize", "Sincronizar");
		m.put("System", "Sistema");
		m.put("System Alert", "Alerta do Sistema");
		m.put("System Alert Template", "Template de Alerta do Sistema");
		m.put("System Date", "Data do Sistema");
		m.put("System Email Address", "Endereço de Email do Sistema");
		m.put("System Maintenance", "Manutenção do Sistema");
		m.put("System Setting", "Configuração do Sistema");
		m.put("System Settings", "Configurações do Sistema");
		m.put("System email address defined in mail setting should be used as recipient of such email, and project name may be appended to this address using to indicate where to create issues. For instance, if system email address is specified as <tt>support@example.com</tt>, sending email to <tt>support+myproject@example.com</tt> will create issue in <tt>myproject</tt>. If project name is not appended, OneDev will look up the project using project designation information below", 
			"O endereço de email do sistema definido na configuração de email deve ser usado como destinatário de tal email, e o nome do projeto pode ser anexado a este endereço para indicar onde criar questões. Por exemplo, se o endereço de email do sistema for especificado como <tt>support@example.com</tt>, enviar email para <tt>support+myproject@example.com</tt> criará uma questão em <tt>myproject</tt>. Se o nome do projeto não for anexado, o OneDev buscará o projeto usando as informações de designação do projeto abaixo");
		m.put("System settings have been saved", "As configurações do sistema foram salvas");
		m.put("System uuid", "UUID do sistema");
		m.put("TIMED_OUT", "TIMED_OUT");
		m.put("TRX Report (.net unit test)", "Relatório TRX (teste de unidade .net)");
		m.put("Tab Width", "Largura da Aba");
		m.put("Tag", "Tag");
		m.put("Tag \"{0}\" already exists, please choose a different name", "A tag \"{0}\" já existe, por favor escolha um nome diferente");
		m.put("Tag \"{0}\" already exists, please choose a different name.", "A tag \"{0}\" já existe, por favor escolha um nome diferente.");
		m.put("Tag \"{0}\" created", "A tag \"{0}\" foi criada");
		m.put("Tag \"{0}\" deleted", "A tag \"{0}\" foi deletada");
		m.put("Tag Message", "Mensagem da Tag");
		m.put("Tag Name", "Nome da Tag");
		m.put("Tag Protection", "Proteção de Tag");
		m.put("Tag creation", "Criação de Tag");
		m.put("Tags", "Tags");
		m.put("Target", "Alvo");
		m.put("Target Branches", "Ramos Alvo");
		m.put("Target Docker Image", "Imagem Docker Alvo");
		m.put("Target File", "Arquivo Alvo");
		m.put("Target Path", "Caminho Alvo");
		m.put("Target Project", "Projeto Alvo");
		m.put("Target branch no longer exists", "O ramo alvo não existe mais");
		m.put("Target branch was fast-forwarded to source branch", "O ramo alvo foi avançado rapidamente para o ramo de origem");
		m.put("Target branch will be fast-forwarded to source branch", "O ramo alvo será avançado rapidamente para o ramo de origem");
		m.put("Target containing spaces or starting with dash needs to be quoted", "O alvo contendo espaços ou começando com traço precisa ser citado");
		m.put("Target or source branch is updated. Please try again", "O ramo alvo ou de origem foi atualizado. Por favor, tente novamente");
		m.put("Task List", "Lista de Tarefas");
		m.put("Task list", "Lista de tarefas");
		m.put("Tell user to reset password", "Informar ao usuário para redefinir a senha");
		m.put("Template Name", "Nome do Template");
		m.put("Template saved", "Template salvo");
		m.put("Terminal close", "Fechar terminal");
		m.put("Terminal input", "Entrada do terminal");
		m.put("Terminal open", "Abrir terminal");
		m.put("Terminal output", "Saída do terminal");
		m.put("Terminal ready", "Terminal pronto");
		m.put("Terminal resize", "Redimensionar terminal");
		m.put("Test", "Teste");
		m.put("Test Case", "Caso de Teste");
		m.put("Test Cases", "Casos de Teste");
		m.put("Test Settings", "Configurações de Teste");
		m.put("Test Suite", "Suite de Teste");
		m.put("Test Suites", "Suites de Teste");
		m.put("Test importing from {0}", "Importando teste de {0}");
		m.put("Test mail has been sent to {0}, please check your mail box", "O email de teste foi enviado para {0}, por favor verifique sua caixa de entrada");
		m.put("Test successful: authentication passed", "Teste bem-sucedido: autenticação aprovada");
		m.put("Test successful: authentication passed with below information retrieved:", "Teste bem-sucedido: autenticação aprovada com as informações abaixo recuperadas:");
		m.put("Text", "Texto");
		m.put("The URL of the server endpoint that will receive the webhook POST requests", "A URL do endpoint do servidor que receberá as solicitações POST do webhook");
		m.put("The change contains disallowed file type(s): {0}", "A alteração contém tipo(s) de arquivo não permitido(s): {0}");
		m.put("The first board will be the default board", "O primeiro quadro será o quadro padrão");
		m.put("The first timesheet will be the default timesheet", "A primeira folha de horas será a folha padrão");
		m.put("The object you are deleting/disabling is still being used", "O objeto que você está excluindo/desativando ainda está sendo usado");
		m.put("The password reset url is invalid or obsolete", "A URL de redefinição de senha é inválida ou obsoleta");
		m.put("The permission to access build log", "A permissão para acessar o log de construção");
		m.put("The permission to access build pipeline", "A permissão para acessar o pipeline de construção");
		m.put("The permission to run job manually. It also implies the permission to access build log, build pipeline and all published reports", 
			"A permissão para executar o trabalho manualmente. Também implica a permissão para acessar o log de construção, o pipeline de construção e todos os relatórios publicados");
		m.put("The secret which allows you to ensure that POST requests sent to the payload URL are from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request", 
			"O segredo que permite garantir que as solicitações POST enviadas para a URL de payload sejam do OneDev. Quando você define um segredo, receberá o cabeçalho X-OneDev-Signature na solicitação POST do webhook");
		m.put("The service desk feature enables user to create issues by sending emails to OneDev. Issues can be discussed over email completely, without the need of logging to OneDev.", 
			"O recurso de service desk permite ao usuário criar questões enviando emails para o OneDev. As questões podem ser discutidas completamente por email, sem a necessidade de fazer login no OneDev.");
		m.put("Then enter the passcode shown in the TOTP authenticator to verify", "Então insira o código mostrado no autenticador TOTP para verificar");
		m.put("Then publish package from project directory like below", "Então publique o pacote do diretório do projeto como abaixo");
		m.put("Then push gem to the source", "Então envie o gem para a origem");
		m.put("Then push image to desired repository under specified project", "Então envie a imagem para o repositório desejado sob o projeto especificado");
		m.put("Then push package to the source", "Então envie o pacote para a origem");
		m.put("Then resolve dependency via command step", "Então resolva a dependência via etapa de comando");
		m.put("Then upload package to the repository with twine", "Então envie o pacote para o repositório com twine");
		m.put("There are <a wicket:id=\"openRequests\">open pull requests</a> against branch <span wicket:id=\"branch\"></span>. These pull requests will be discarded if the branch is deleted.", 
			"Existem <a wicket:id=\"openRequests\">solicitações de pull abertas</a> contra o ramo <span wicket:id=\"branch\"></span>. Essas solicitações de pull serão descartadas se o ramo for deletado.");
		m.put("There are incompatibilities since your upgraded version", "Existem incompatibilidades desde a sua versão atualizada");
		m.put("There are merge conflicts", "Existem conflitos de mesclagem");
		m.put("There are merge conflicts.", "Existem conflitos de mesclagem.");
		m.put("There are merge conflicts. You can still create the pull request though", "Existem conflitos de mesclagem. Você ainda pode criar a solicitação de pull, no entanto");
		m.put("There are unsaved changes, discard and continue?", "Existem alterações não salvas, descartar e continuar?");
		m.put("These authenticators normally run on your mobile phone, some examples are Google Authenticator, Microsoft Authenticator, Authy, 1Password etc.", 
			"Esses autenticadores normalmente funcionam no seu celular, alguns exemplos são Google Authenticator, Microsoft Authenticator, Authy, 1Password, etc.");
		m.put("This <span wicket:id=\"elementTypeName\"></span> is imported from <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>", 
			"Este <span wicket:id=\"elementTypeName\"></span> foi importado de <a wicket:id=\"link\" class=\"alert-link\"><span wicket:id=\"label\"></span></a>");
		m.put("This Month", "Este mês");
		m.put("This Week", "Esta semana");
		m.put("This account is disabled", "Esta conta está desativada");
		m.put("This address should be <code>verified sender</code> in SendGrid and will be used as sender address of various email notifications. One can also reply to this address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below", 
			"Este endereço deve ser <code>remetente verificado</code> no SendGrid e será usado como endereço do remetente de várias notificações por email. Também é possível responder a este endereço para postar comentários de questões ou solicitações de pull se a opção <code>Receber Email Postado</code> estiver habilitada abaixo");
		m.put("This address will be used as sender address of various email notifications. User can also reply to this address to post issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below", 
			"Este endereço será usado como endereço do remetente de várias notificações por email. O usuário também pode responder a este endereço para postar comentários de questões ou solicitações de pull via email se a opção <code>Verificar Email Recebido</code> estiver habilitada abaixo");
		m.put("This change is already opened for merge by pull request {0}", "Esta alteração já está aberta para mesclagem pela solicitação de pull {0}");
		m.put("This change is squashed/rebased onto base branch via a pull request", "Esta alteração foi compactada/rebaseada no ramo base via uma solicitação de pull");
		m.put("This change is squashed/rebased onto base branch via pull request {0}", "Esta alteração foi compactada/rebaseada no ramo base via solicitação de pull {0}");
		m.put("This change needs to be verified by some jobs. Submit pull request instead", "Esta alteração precisa ser verificada por alguns trabalhos. Envie uma solicitação de pull em vez disso");
		m.put("This commit is rebased", "Este commit foi rebaseado");
		m.put("This date is using <a href=\"https://www.w3.org/TR/NOTE-datetime\">ISO 8601 format</a>", 
			"Esta data está usando o <a href=\"https://www.w3.org/TR/NOTE-datetime\">formato ISO 8601</a>");
		m.put("This email address is being used", "Este endereço de email está sendo usado");
		m.put("This executor runs build jobs as docker containers on OneDev server", "Este executor executa trabalhos de build como contêineres Docker no servidor OneDev");
		m.put("This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>", 
			"Este executor executa trabalhos de build como contêineres Docker em máquinas remotas via <a href='/~administration/agents' target='_blank'>agentes</a>");
		m.put("This executor runs build jobs as pods in a kubernetes cluster. No any agents are required.<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system settings as job pods need to access it to download source and artifacts", 
			"Este executor executa trabalhos de build como pods em um cluster Kubernetes. Nenhum agente é necessário.<b class='text-danger'>Nota:</b> Certifique-se de que a URL do servidor está especificada corretamente nas configurações do sistema, pois os pods de trabalho precisam acessá-la para baixar o código-fonte e os artefatos");
		m.put("This executor runs build jobs with OneDev server's shell facility.<br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev server process. Make sure it can only be used by trusted jobs", 
			"Este executor executa trabalhos de construção com a funcionalidade de shell do servidor OneDev.<br><b class='text-danger'>AVISO</b>: Trabalhos executados com este executor têm as mesmas permissões que o processo do servidor OneDev. Certifique-se de que ele só possa ser usado por trabalhos confiáveis");
		m.put("This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br><b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. Make sure it can only be used by trusted jobs", 
			"Este executor executa trabalhos de construção com a funcionalidade de shell de máquinas remotas via <a href='/~administration/agents' target='_blank'>agentes</a><br><b class='text-danger'>AVISO</b>: Trabalhos executados com este executor têm as mesmas permissões que o processo do agente OneDev. Certifique-se de que ele só possa ser usado por trabalhos confiáveis");
		m.put("This field is required", "Este campo é obrigatório");
		m.put("This filter is used to determine the LDAP entry for current user. For example: <i>(&(uid={0})(objectclass=person))</i>. In this example, <i>{0}</i> represents login name of current user.", 
			"Este filtro é usado para determinar a entrada LDAP do usuário atual. Por exemplo: <i>(&(uid={0})(objectclass=person))</i>. Neste exemplo, <i>{0}</i> representa o nome de login do usuário atual.");
		m.put("This installation does not have an active subscription and runs as community edition. To access <a href=\"https://onedev.io/pricing\">enterprise features</a>, an active subscription is required", 
			"Esta instalação não possui uma assinatura ativa e está sendo executada como edição comunitária. Para acessar <a href=\"https://onedev.io/pricing\">recursos empresariais</a>, é necessária uma assinatura ativa");
		m.put("This installation has a trial subscription and is now running as enterprise edition", 
			"Esta instalação possui uma assinatura de teste e está sendo executada como edição empresarial");
		m.put("This installation has an active subscription and runs as enterprise edition", "Esta instalação possui uma assinatura ativa e está sendo executada como edição empresarial");
		m.put("This installation has an expired subscription, and runs as community edition", "Esta instalação possui uma assinatura expirada e está sendo executada como edição comunitária");
		m.put("This installation has an unlimited users subscription and is now running as enterprise edition", 
			"Esta instalação tem uma assinatura de usuários ilimitados e agora está operando como edição enterprise");
		m.put("This installation's subscription has expired and is now running as the community edition", 
			"A assinatura desta instalação expirou e agora está operando como a edição comunitária");
		m.put("This is a Git LFS object, but the storage file is missing", "Este é um objeto Git LFS, mas o arquivo de armazenamento está ausente");
		m.put("This is a built-in role and can not be deleted", "Este é um papel integrado e não pode ser excluído");
		m.put("This is a disabled service account", "Esta é uma conta de serviço desativada");
		m.put("This is a layer cache. To use the cache, add below option to your docker buildx command", 
			"Este é um cache de camada. Para usar o cache, adicione a opção abaixo ao seu comando docker buildx");
		m.put("This is a service account for task automation purpose", "Esta é uma conta de serviço para fins de automação de tarefas");
		m.put("This is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Este é um recurso enterprise. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("This key has already been used by another project", "Esta chave já foi usada por outro projeto");
		m.put("This key is associated with {0}, however it is NOT a verified email address of this user", 
			"Esta chave está associada a {0}, no entanto, NÃO é um endereço de email verificado deste usuário");
		m.put("This key is used to determine if there is a cache hit in project hierarchy (search from current project to root project in order, same for load keys below). A cache is considered hit if its key is exactly the same as the key defined here.<br><b>NOTE:</b> In case your project has lock files(package.json, pom.xml, etc.) able to represent cache state, this key should be defined as &lt;cache name&gt;-@file:checksum.txt@, where checksum.txt is generated from these lock files with the <b>generate checksum step</b> defined before this step", 
			"Esta chave é usada para determinar se há um acerto de cache na hierarquia do projeto (pesquisa do projeto atual até o projeto raiz em ordem, o mesmo para carregar chaves abaixo). Um cache é considerado acertado se sua chave for exatamente a mesma que a chave definida aqui.<br><b>NOTA:</b> Caso seu projeto tenha arquivos de bloqueio (package.json, pom.xml, etc.) capazes de representar o estado do cache, esta chave deve ser definida como &lt;nome do cache&gt;-@file:checksum.txt@, onde checksum.txt é gerado a partir desses arquivos de bloqueio com o <b>passo de gerar checksum</b> definido antes deste passo");
		m.put("This key is used to download and upload cache in project hierarchy (search from current project to root project in order)", 
			"Esta chave é usada para baixar e carregar o cache na hierarquia do projeto (pesquisa do projeto atual até o projeto raiz, na ordem)");
		m.put("This key or one of its sub key is already added", "Esta chave ou uma de suas subchaves já foi adicionada");
		m.put("This key or one of its subkey is already in use", "Esta chave ou uma de suas subchaves já está em uso");
		m.put("This line has confusable unicode character modification", "Esta linha possui modificação de caracteres Unicode confusos");
		m.put("This might happen when project points to a wrong git repository, or the commit is garbage collected.", 
			"Isso pode acontecer quando o projeto aponta para um repositório Git errado ou o commit foi coletado como lixo.");
		m.put("This might happen when project points to a wrong git repository, or these commits are garbage collected.", 
			"Isso pode acontecer quando o projeto aponta para um repositório Git errado ou esses commits foram coletados como lixo.");
		m.put("This name has already been used by another board", "Este nome já foi usado por outro quadro");
		m.put("This name has already been used by another group", "Este nome já foi usado por outro grupo");
		m.put("This name has already been used by another issue board in the project", "Este nome já foi usado por outro quadro de problemas no projeto");
		m.put("This name has already been used by another job executor", "Este nome já foi usado por outro executor de trabalho");
		m.put("This name has already been used by another project", "Este nome já foi usado por outro projeto");
		m.put("This name has already been used by another provider", "Este nome já foi usado por outro provedor");
		m.put("This name has already been used by another role", "Este nome já foi usado por outro papel");
		m.put("This name has already been used by another role.", "Este nome já foi usado por outro papel.");
		m.put("This name has already been used by another script", "Este nome já foi usado por outro script");
		m.put("This name has already been used by another state", "Este nome já foi usado por outro estado");
		m.put("This operation is disallowed by branch protection rule", "Esta operação é proibida pela regra de proteção de branch");
		m.put("This page lists changes since previous build on <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">same stream</a>", 
			"Esta página lista as alterações desde a build anterior no <a href=\"https://docs.onedev.io/concepts#build-stream\" class=\"link-primary\" target=\"_blank\">mesmo fluxo</a>");
		m.put("This page lists recent commits fixing the issue", "Esta página lista commits recentes que corrigem o problema");
		m.put("This permission enables one to access confidential issues", "Esta permissão permite acessar problemas confidenciais");
		m.put("This permission enables one to schedule issues into iterations", "Esta permissão permite agendar problemas em iterações");
		m.put("This property is imported from {0}", "Esta propriedade é importada de {0}");
		m.put("This pull request has been discarded", "Este pull request foi descartado");
		m.put("This report will be displayed in pull request overview page if build is triggered by pull request", 
			"Este relatório será exibido na página de visão geral do pull request se a build for acionada por um pull request");
		m.put("This server is currently accessed via http protocol, please configure your docker daemon or buildx builder to <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">work with insecure registry</a>", 
			"Este servidor está atualmente acessado via protocolo http, configure seu daemon Docker ou builder buildx para <a href=\"https://docs.onedev.io/tutorials/cicd/insecure-docker-registry\" target=\"_blank\">trabalhar com registro inseguro</a>");
		m.put("This shows average duration of different states over time", "Isso mostra a duração média de diferentes estados ao longo do tempo");
		m.put("This shows average duration of merged pull requests over time", "Isso mostra a duração média de pull requests mesclados ao longo do tempo");
		m.put("This shows number of <b>new</b> issues in different states over time", "Isso mostra o número de <b>novos</b> problemas em diferentes estados ao longo do tempo");
		m.put("This shows number of issues in various states over time", "Isso mostra o número de problemas em vários estados ao longo do tempo");
		m.put("This shows number of open and merged pull requests over time", "Isso mostra o número de pull requests abertos e mesclados ao longo do tempo");
		m.put("This step can only be executed by a docker aware executor", "Esta etapa só pode ser executada por um executor compatível com Docker");
		m.put("This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>", 
			"Esta etapa só pode ser executada por um executor compatível com Docker. Ela é executada sob <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>espaço de trabalho do trabalho</a>");
		m.put("This step copies files from job workspace to build artifacts directory, so that they can be accessed after job is completed", 
			"Esta etapa copia arquivos do espaço de trabalho do job para o diretório de artefatos de build, para que possam ser acessados após a conclusão do job");
		m.put("This step publishes specified files to be served as project web site. Project web site can be accessed publicly via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>", 
			"Esta etapa publica os arquivos especificados para serem servidos como site web do projeto. O site web do projeto pode ser acessado publicamente via <code>http://&lt;onedev base url&gt;/path/to/project/~site</code>");
		m.put("This step pulls specified refs from remote", "Esta etapa puxa refs especificadas do remoto");
		m.put("This step pushes current commit to same ref on remote", "Esta etapa empurra o commit atual para o mesmo ref no remoto");
		m.put("This step sets up Renovate cache. Place it before Renovate step if you want to use it", 
			"Esta etapa configura o cache do Renovate. Coloque-a antes da etapa do Renovate se quiser usá-lo");
		m.put("This step sets up trivy db cache to speed up various scanner steps. Place it before scanner steps if you want to use it", 
			"Esta etapa configura o cache do banco de dados do Trivy para acelerar várias etapas de scanner. Coloque-a antes das etapas de scanner se quiser usá-lo");
		m.put("This subscription key was already used", "Esta chave de assinatura já foi usada");
		m.put("This subscription key was expired", "Esta chave de assinatura expirou");
		m.put("This tab shows the pipeline containing current build. Check <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">this article</a> to understand how build pipeline works", 
			"Esta aba mostra o pipeline contendo a build atual. Confira <a href=\"https://robinshen.medium.com/understanding-onedev-pipeline-db0bb0e54aa7\" target=\"_blank\">este artigo</a> para entender como funciona o pipeline de build");
		m.put("This trigger will only be applicable if tagged commit is reachable from branches specified here. Multiple branches should be separated with spaces. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. Leave empty to match all branches", 
			"Este gatilho só será aplicável se o commit marcado for alcançável a partir dos branches especificados aqui. Múltiplos branches devem ser separados por espaços. Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir. Deixe vazio para corresponder a todos os branches");
		m.put("This user is authenticating via external system.", "Este usuário está se autenticando via sistema externo.");
		m.put("This user is authenticating via internal database.", "Este usuário está se autenticando via banco de dados interno.");
		m.put("This user is currently authenticating via external system. Setting password will switch to use internal database", 
			"Este usuário está atualmente se autenticando via sistema externo. Definir uma senha mudará para usar o banco de dados interno");
		m.put("This will deactivate current subscription and all enterprise features will be disabled, do you want to continue?", 
			"Isso desativará a assinatura atual e todos os recursos empresariais serão desativados, deseja continuar?");
		m.put("This will discard all project specific boards, do you want to continue?", "Isso descartará todos os quadros específicos do projeto, deseja continuar?");
		m.put("This will restart SSH server. Do you want to continue?", "Isso reiniciará o servidor SSH. Deseja continuar?");
		m.put("Threads", "Tópicos");
		m.put("Time Estimate Issue Field", "Campo de Estimativa de Tempo do Problema");
		m.put("Time Range", "Intervalo de Tempo");
		m.put("Time Spent Issue Field", "Campo de Tempo Gasto do Problema");
		m.put("Time Tracking", "Rastreamento de Tempo");
		m.put("Time Tracking Setting", "Configuração de Rastreamento de Tempo");
		m.put("Time Tracking Settings", "Configurações de Rastreamento de Tempo");
		m.put("Time tracking settings have been saved", "As configurações de rastreamento de tempo foram salvas");
		m.put("Timed out", "Tempo esgotado");
		m.put("Timeout", "Tempo limite");
		m.put("Timesheet", "Planilha de horas");
		m.put("Timesheet Setting", "Configuração de Planilha de Horas");
		m.put("Timesheets", "Planilhas de horas");
		m.put("Timing", "Cronometragem");
		m.put("Title", "Título");
		m.put("To Everyone", "Para Todos");
		m.put("To State", "Para Estado");
		m.put("To States", "Para Estados");
		m.put("To authenticate via internal database, <a wicket:id=\"setPasswordForUser\">set password for user</a> or <a wicket:id=\"tellUserToResetPassword\">tell user to reset password</a>", 
			"Para autenticar via banco de dados interno, <a wicket:id=\"setPasswordForUser\">defina uma senha para o usuário</a> ou <a wicket:id=\"tellUserToResetPassword\">peça ao usuário para redefinir a senha</a>");
		m.put("To avoid duplication, estimated/remaining time showing here does not include those aggregated from \"{0}\"", 
			"Para evitar duplicação, o tempo estimado/restante mostrado aqui não inclui aqueles agregados de \"{0}\"");
		m.put("To avoid duplication, spent time showing here does not include those aggregated from \"{0}\"", 
			"Para evitar duplicação, o tempo gasto mostrado aqui não inclui aqueles agregados de \"{0}\"");
		m.put("Toggle change history", "Alternar histórico de alterações");
		m.put("Toggle comments", "Alternar comentários");
		m.put("Toggle commits", "Alternar commits");
		m.put("Toggle dark mode", "Alternar modo escuro");
		m.put("Toggle detail message", "Alternar mensagem detalhada");
		m.put("Toggle fixed width font", "Alternar fonte de largura fixa");
		m.put("Toggle full screen", "Alternar tela cheia");
		m.put("Toggle matched contents", "Alternar conteúdos correspondentes");
		m.put("Toggle navigation", "Alternar navegação");
		m.put("Toggle work log", "Alternar registro de trabalho");
		m.put("Tokens", "Tokens");
		m.put("Too many commits to load", "Commits demais para carregar");
		m.put("Too many commits, displaying recent {0}", "Muitos commits, exibindo os últimos {0}");
		m.put("Too many log entries, displaying recent {0}", "Muitas entradas de log, exibindo os últimos {0}");
		m.put("Too many problems, displaying first {0}", "Muitos problemas, exibindo os primeiros {0}");
		m.put("Toomanyrequests", "Muitas solicitações");
		m.put("Top", "Topo");
		m.put("Topo", "Topo");
		m.put("Total Heap Memory", "Memória Total do Heap");
		m.put("Total Number", "Número Total");
		m.put("Total Problems", "Total de Problemas");
		m.put("Total Size", "Tamanho Total");
		m.put("Total Test Duration", "Duração Total do Teste");
		m.put("Total estimated time", "Tempo estimado total");
		m.put("Total spent time", "Tempo total gasto");
		m.put("Total spent time / total estimated time", "Tempo total gasto / tempo estimado total");
		m.put("Total time", "Tempo total");
		m.put("Total:", "Total:");
		m.put("Touched File", "Arquivo Modificado");
		m.put("Touched Files", "Arquivos Modificados");
		m.put("Transfer LFS Files", "Transferir Arquivos LFS");
		m.put("Transit manually", "Transitar manualmente");
		m.put("Transited state of issue \"{0}\" to \"{1}\" ({2})", "Estado do problema \"{0}\" transitado para \"{1}\" ({2})");
		m.put("Transition Edit Bean", "Editar Bean de Transição");
		m.put("Transition Spec", "Especificação de Transição");
		m.put("Trial Expiration Date", "Data de Expiração do Teste");
		m.put("Trial subscription key not applicable for this installation", "Chave de assinatura de teste não aplicável para esta instalação");
		m.put("Triggers", "Gatilhos");
		m.put("Trivy Container Image Scanner", "Scanner de Imagem de Contêiner Trivy");
		m.put("Trivy Filesystem Scanner", "Scanner de Sistema de Arquivos Trivy");
		m.put("Trivy Rootfs Scanner", "Scanner Rootfs Trivy");
		m.put("Try EE", "Experimente EE");
		m.put("Try Enterprise Edition", "Experimente a Edição Enterprise");
		m.put("Twitch", "Twitch");
		m.put("Two Factor Authentication", "Autenticação de Dois Fatores");
		m.put("Two-factor Authentication", "Autenticação de Dois Fatores");
		m.put("Two-factor authentication already set up. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Request to set up again", 
			"Autenticação de dois fatores já configurada. <a wicket:id=\"requestToSetupAgain\"><wicket:t>Solicitar configuração novamente");
		m.put("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync", 
			"A autenticação de dois fatores está ativada. Insira o código exibido no seu autenticador TOTP. Se encontrar problemas, certifique-se de que o horário do servidor OneDev e do seu dispositivo com o autenticador TOTP estão sincronizados");
		m.put("Two-factor authentication is enforced for your account to enhance security. Please follow below procedure to set it up", 
			"A autenticação de dois fatores é obrigatória para sua conta para aumentar a segurança. Siga o procedimento abaixo para configurá-la");
		m.put("Two-factor authentication is now configured", "A autenticação de dois fatores está agora configurada");
		m.put("Two-factor authentication not enabled", "Autenticação de dois fatores não habilitada");
		m.put("Type", "Tipo");
		m.put("Type <code>yes</code> below to cancel all queried builds", "Digite <code>yes</code> abaixo para cancelar todas as builds consultadas");
		m.put("Type <code>yes</code> below to cancel selected builds", "Digite <code>yes</code> abaixo para cancelar as builds selecionadas");
		m.put("Type <code>yes</code> below to confirm deleting all queried users", "Digite <code>yes</code> abaixo para confirmar a exclusão de todos os usuários consultados");
		m.put("Type <code>yes</code> below to confirm deleting selected users", "Digite <code>yes</code> abaixo para confirmar a exclusão dos usuários selecionados");
		m.put("Type <code>yes</code> below to copy all queried issues to project \"{0}\"", "Digite <code>yes</code> abaixo para copiar todos os problemas consultados para o projeto \"{0}\"");
		m.put("Type <code>yes</code> below to copy selected issues to project \"{0}\"", "Digite <code>yes</code> abaixo para copiar os problemas selecionados para o projeto \"{0}\"");
		m.put("Type <code>yes</code> below to delete all queried builds", "Digite <code>yes</code> abaixo para excluir todas as builds consultadas");
		m.put("Type <code>yes</code> below to delete all queried comments", "Digite <code>yes</code> abaixo para excluir todos os comentários consultados");
		m.put("Type <code>yes</code> below to delete all queried issues", "Digite <code>yes</code> abaixo para excluir todos os problemas consultados");
		m.put("Type <code>yes</code> below to delete all queried packages", "Digite <code>yes</code> abaixo para excluir todos os pacotes consultados");
		m.put("Type <code>yes</code> below to delete all queried projects", "Digite <code>yes</code> abaixo para excluir todos os projetos consultados");
		m.put("Type <code>yes</code> below to delete all queried pull requests", "Digite <code>yes</code> abaixo para excluir todas as solicitações de pull consultadas");
		m.put("Type <code>yes</code> below to delete selected builds", "Digite <code>yes</code> abaixo para excluir as builds selecionadas");
		m.put("Type <code>yes</code> below to delete selected comments", "Digite <code>yes</code> abaixo para excluir os comentários selecionados");
		m.put("Type <code>yes</code> below to delete selected issues", "Digite <code>yes</code> abaixo para excluir os problemas selecionados");
		m.put("Type <code>yes</code> below to delete selected packages", "Digite <code>yes</code> abaixo para excluir os pacotes selecionados");
		m.put("Type <code>yes</code> below to delete selected projects", "Digite <code>yes</code> abaixo para excluir os projetos selecionados");
		m.put("Type <code>yes</code> below to delete selected pull requests", "Digite <code>yes</code> abaixo para excluir as solicitações de pull selecionadas");
		m.put("Type <code>yes</code> below to discard all queried pull requests", "Digite <code>yes</code> abaixo para descartar todas as solicitações de pull consultadas");
		m.put("Type <code>yes</code> below to discard selected pull requests", "Digite <code>yes</code> abaixo para descartar as solicitações de pull selecionadas");
		m.put("Type <code>yes</code> below to move all queried issues to project \"{0}\"", "Digite <code>yes</code> abaixo para mover todos os problemas consultados para o projeto \"{0}\"");
		m.put("Type <code>yes</code> below to move all queried projects to be under \"{0}\"", "Digite <code>yes</code> abaixo para mover todos os projetos consultados para estar sob \"{0}\"");
		m.put("Type <code>yes</code> below to move selected issues to project \"{0}\"", "Digite <code>yes</code> abaixo para mover os problemas selecionados para o projeto \"{0}\"");
		m.put("Type <code>yes</code> below to move selected projects to be under \"{0}\"", "Digite <code>yes</code> abaixo para mover os projetos selecionados para estar sob \"{0}\"");
		m.put("Type <code>yes</code> below to pause all queried agents", "Digite <code>yes</code> abaixo para pausar todos os agentes consultados");
		m.put("Type <code>yes</code> below to re-run all queried builds", "Digite <code>yes</code> abaixo para reexecutar todas as builds consultadas");
		m.put("Type <code>yes</code> below to re-run selected builds", "Digite <code>yes</code> abaixo para reexecutar as builds selecionadas");
		m.put("Type <code>yes</code> below to remove all queried users from group", "Digite <code>yes</code> abaixo para remover todos os usuários consultados do grupo");
		m.put("Type <code>yes</code> below to remove from all queried groups", "Digite <code>yes</code> abaixo para remover de todos os grupos consultados");
		m.put("Type <code>yes</code> below to remove from selected groups", "Digite <code>yes</code> abaixo para remover dos grupos selecionados");
		m.put("Type <code>yes</code> below to remove selected users from group", "Digite <code>yes</code> abaixo para remover usuários selecionados do grupo");
		m.put("Type <code>yes</code> below to restart all queried agents", "Digite <code>yes</code> abaixo para reiniciar todos os agentes consultados");
		m.put("Type <code>yes</code> below to restart selected agents", "Digite <code>yes</code> abaixo para reiniciar os agentes selecionados");
		m.put("Type <code>yes</code> below to resume all queried agents", "Digite <code>yes</code> abaixo para retomar todos os agentes consultados");
		m.put("Type <code>yes</code> below to set all queried as root projects", "Digite <code>yes</code> abaixo para definir todos os consultados como projetos raiz");
		m.put("Type <code>yes</code> below to set selected as root projects", "Digite <code>yes</code> abaixo para definir os selecionados como projetos raiz");
		m.put("Type password here", "Digite a senha aqui");
		m.put("Type to filter", "Digite para filtrar");
		m.put("Type to filter...", "Digite para filtrar...");
		m.put("URL", "URL");
		m.put("Unable To Delete/Disable Right Now", "Incapaz de Excluir/Desativar Agora");
		m.put("Unable to apply change as otherwise you will not be able to manage this project", "Não foi possível aplicar a alteração, pois você não poderá gerenciar este projeto");
		m.put("Unable to change password as you are authenticating via external system", "Não foi possível alterar a senha, pois você está se autenticando via sistema externo");
		m.put("Unable to comment", "Não foi possível comentar");
		m.put("Unable to connect to server", "Não foi possível conectar ao servidor");
		m.put("Unable to create protected branch", "Não foi possível criar branch protegida");
		m.put("Unable to create protected tag", "Não foi possível criar tag protegida");
		m.put("Unable to diff as some line is too long.", "Não foi possível gerar diff, pois alguma linha é muito longa.");
		m.put("Unable to diff as the file is too large.", "Não foi possível gerar diff, pois o arquivo é muito grande.");
		m.put("Unable to find SSO provider: ", "Não foi possível encontrar o provedor SSO:");
		m.put("Unable to find agent {0}", "Não foi possível encontrar o agente {0}");
		m.put("Unable to find build #{0} in project {1}", "Não foi possível encontrar a build #{0} no projeto {1}");
		m.put("Unable to find commit to import build spec (import project: {0}, import revision: {1})", 
			"Não foi possível encontrar o commit para importar a especificação de build (projeto de importação: {0}, revisão de importação: {1})");
		m.put("Unable to find issue #{0} in project {1}", "Não foi possível encontrar o problema #{0} no projeto {1}");
		m.put("Unable to find project to import build spec: {0}", "Não foi possível encontrar o projeto para importar a especificação de build: {0}");
		m.put("Unable to find pull request #{0} in project {1}", "Não foi possível encontrar a solicitação de pull #{0} no projeto {1}");
		m.put("Unable to find timesheet: ", "Não foi possível encontrar a folha de ponto:");
		m.put("Unable to get guilds info", "Não foi possível obter informações de guildas");
		m.put("Unable to import build spec (import project: {0}, import revision: {1}): {2}", "Não foi possível importar a especificação de build (projeto de importação: {0}, revisão de importação: {1}): {2}");
		m.put("Unable to notify user as mail service is not configured", "Não foi possível notificar o usuário, pois o serviço de e-mail não está configurado");
		m.put("Unable to send password reset email as mail service is not configured", "Incapaz de enviar email de redefinição de senha pois o serviço de email não está configurado");
		m.put("Unable to send verification email as mail service is not configured yet", "Não foi possível enviar o e-mail de verificação, pois o serviço de e-mail ainda não está configurado");
		m.put("Unauthorize this user", "Desautorizar este usuário");
		m.put("Unauthorized", "Não autorizado");
		m.put("Undefined", "Indefinido");
		m.put("Undefined Field Resolution", "Resolução de Campo Indefinido");
		m.put("Undefined Field Value Resolution", "Resolução de Valor de Campo Indefinido");
		m.put("Undefined State Resolution", "Resolução de Estado Indefinido");
		m.put("Undefined custom field: ", "Campo personalizado indefinido:");
		m.put("Under which condition this step should run. <b>SUCCESSFUL</b> means all non-optional steps running before this step are successful", 
			"Sob qual condição esta etapa deve ser executada. <b>SUCESSO</b> significa que todas as etapas não opcionais executadas antes desta etapa foram bem-sucedidas");
		m.put("Unexpected setting: {0}", "Configuração inesperada: {0}");
		m.put("Unexpected ssh signature hash algorithm: ", "Algoritmo de hash de assinatura ssh inesperado:");
		m.put("Unexpected ssh signature namespace: ", "Namespace de assinatura ssh inesperado:");
		m.put("Unified", "Unificado");
		m.put("Unified view", "Visão unificada");
		m.put("Unit Test Statistics", "Estatísticas de Teste de Unidade");
		m.put("Unlimited", "Ilimitado");
		m.put("Unlink this issue", "Desvincular este problema");
		m.put("Unordered List", "Lista não ordenada");
		m.put("Unordered list", "Lista não ordenada");
		m.put("Unpin this issue", "Desafixar este problema");
		m.put("Unresolved", "Não resolvido");
		m.put("Unresolved comment on file \"{0}\" in project \"{1}\"", "Comentário não resolvido no arquivo \"{0}\" no projeto \"{1}\"");
		m.put("Unscheduled", "Não agendado");
		m.put("Unscheduled Issues", "Problemas não agendados");
		m.put("Unsolicited OIDC authentication response", "Resposta de autenticação OIDC não solicitada");
		m.put("Unsolicited OIDC response", "Resposta OIDC não solicitada");
		m.put("Unsolicited discord api response", "Resposta da API do Discord não solicitada");
		m.put("Unspecified", "Não especificado");
		m.put("Unsupported", "Não suportado");
		m.put("Unsupported ssh signature algorithm: ", "Algoritmo de assinatura ssh não suportado:");
		m.put("Unsupported ssh signature version: ", "Versão de assinatura ssh não suportada:");
		m.put("Unverified", "Não verificado");
		m.put("Unverified email address is <b>NOT</b> applicable for above functionalities", "Endereço de e-mail não verificado <b>NÃO</b> é aplicável para as funcionalidades acima");
		m.put("Unvote", "Remover voto");
		m.put("Unwatched. Click to watch", "Não assistido. Clique para assistir");
		m.put("Update", "Atualizar");
		m.put("Update Dependencies via Renovate", "Atualizar Dependências via Renovate");
		m.put("Update Source Branch", "Atualizar Ramificação de Origem");
		m.put("Update body", "Atualizar corpo");
		m.put("Upload", "Enviar");
		m.put("Upload Access Token Secret", "Enviar Token de Acesso Secreto");
		m.put("Upload Cache", "Enviar Cache");
		m.put("Upload Files", "Enviar Arquivos");
		m.put("Upload Project Path", "Enviar Caminho do Projeto");
		m.put("Upload Strategy", "Estratégia de Envio");
		m.put("Upload a 128x128 transparent png file to be used as logo for dark mode", "Envie um arquivo PNG transparente 128x128 para ser usado como logotipo no modo escuro");
		m.put("Upload a 128x128 transparent png file to be used as logo for light mode", "Envie um arquivo PNG transparente 128x128 para ser usado como logotipo no modo claro");
		m.put("Upload artifacts", "Carregar artefatos");
		m.put("Upload avatar", "Enviar avatar");
		m.put("Upload should be less than {0} Mb", "O envio deve ser menor que {0} Mb");
		m.put("Upload to Project", "Enviar para o Projeto");
		m.put("Uploaded Caches", "Caches Enviados");
		m.put("Uploading file", "Enviando arquivo");
		m.put("Url", "Url");
		m.put("Use '*' for wildcard match", "Use '*' para correspondência de curinga");
		m.put("Use '*' or '?' for wildcard match. Prefix with '-' to exclude", "Use '*' ou '?' para correspondência de curinga. Prefixe com '-' para excluir");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>");
		m.put("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude", 
			"Use '**', '*' ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>. Prefixe com '-' para excluir");
		m.put("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>", 
			"Use '**', '*', ou '?' para <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>correspondência de curinga de caminho</a>");
		m.put("Use '\\' to escape brackets", "Use '\\' para escapar colchetes");
		m.put("Use '\\' to escape quotes", "Use '\\' para escapar aspas");
		m.put("Use @@ to reference scope in job commands to avoid being interpreted as variable", "Use @@ para referenciar escopo em comandos de trabalho para evitar ser interpretado como variável");
		m.put("Use Avatar Service", "Usar Serviço de Avatar");
		m.put("Use Default", "Usar Padrão");
		m.put("Use Default Boards", "Usar Quadros Padrão");
		m.put("Use For Git Operations", "Usar Para Operações Git");
		m.put("Use Git in System Path", "Usar Git no Caminho do Sistema");
		m.put("Use Hours And Minutes Only", "Usar Apenas Horas e Minutos");
		m.put("Use Specified Git", "Usar Git Especificado");
		m.put("Use Specified curl", "Usar curl Especificado");
		m.put("Use Step Template", "Usar Modelo de Etapa");
		m.put("Use curl in System Path", "Usar curl no Caminho do Sistema");
		m.put("Use default", "Usar padrão");
		m.put("Use default storage class", "Usar classe de armazenamento padrão");
		m.put("Use job token as user name so that OneDev can know which build is ${permission.equals(\"write\")? \"deploying\": \"using\"} packages", 
			"Usar token de trabalho como nome de usuário para que o OneDev saiba qual build está ${permission.equals(\"write\")? \"implantando\": \"usando\"} pacotes");
		m.put("Use job token to tell OneDev the build publishing the package", "Usar token de trabalho para informar ao OneDev o build que está publicando o pacote");
		m.put("Use job token to tell OneDev the build pushing the chart", "Usar token de trabalho para informar ao OneDev o build que está enviando o gráfico");
		m.put("Use job token to tell OneDev the build pushing the package", "Usar token de trabalho para informar ao OneDev o build que está enviando o pacote");
		m.put("Use job token to tell OneDev the build using the package", "Usar token de trabalho para informar ao OneDev o build que está usando o pacote");
		m.put("Use project dependency to retrieve artifacts from other projects", "Usar dependência de projeto para recuperar artefatos de outros projetos");
		m.put("Use specified choices", "Usar escolhas especificadas");
		m.put("Use specified default value", "Usar valor padrão especificado");
		m.put("Use specified value or job secret", "Usar valor especificado ou segredo de trabalho");
		m.put("Use specified values or job secrets", "Usar valores especificados ou segredos de trabalho");
		m.put("Use triggers to run the job automatically under certain conditions", "Usar gatilhos para executar o trabalho automaticamente sob certas condições");
		m.put("Use value of specified parameter/secret", "Usar valor do parâmetro/segredo especificado");
		m.put("Used Heap Memory", "Memória Heap Usada");
		m.put("User", "Usuário");
		m.put("User \"{0}\" unauthorized", "Usuário \"{0}\" não autorizado");
		m.put("User Authorization Bean", "Bean de Autorização de Usuário");
		m.put("User Authorizations", "Autorizações de Usuário");
		m.put("User Authorizations Bean", "Bean de Autorizações de Usuário");
		m.put("User Count", "Contagem de Usuários");
		m.put("User Email Attribute", "Atributo de E-mail do Usuário");
		m.put("User Full Name Attribute", "Atributo de Nome Completo do Usuário");
		m.put("User Groups Attribute", "Atributo de Grupos de Usuário");
		m.put("User Invitation", "Convite de Usuário");
		m.put("User Invitation Template", "Modelo de Convite de Usuário");
		m.put("User Management", "Gerenciamento de Usuário");
		m.put("User Match Criteria", "Critérios de Correspondência de Usuário");
		m.put("User Name", "Nome de Usuário");
		m.put("User Principal Name", "Nome Principal de Usuário");
		m.put("User Profile", "Perfil de Usuário");
		m.put("User SSH Key Attribute", "Atributo de Chave SSH do Usuário");
		m.put("User Search Bases", "Bases de Pesquisa de Usuário");
		m.put("User Search Filter", "Filtro de Pesquisa de Usuário");
		m.put("User added to group", "Usuário adicionado ao grupo");
		m.put("User authorizations updated", "Autorizações de usuário atualizadas");
		m.put("User authorized", "Usuário autorizado");
		m.put("User avatar will be requested by appending a hash to this url", "O avatar do usuário será solicitado anexando um hash a esta url");
		m.put("User can sign up if this option is enabled", "O usuário pode se inscrever se esta opção estiver habilitada");
		m.put("User disabled", "Usuário desativado");
		m.put("User name", "Nome de usuário");
		m.put("User name already used by another account", "Nome de usuário já utilizado por outra conta");
		m.put("Users", "Usuários");
		m.put("Users converted to service accounts successfully", "Usuários convertidos para contas de serviço com sucesso");
		m.put("Users deleted successfully", "Usuários excluídos com sucesso");
		m.put("Users disabled successfully", "Usuários desativados com sucesso");
		m.put("Users enabled successfully", "Usuários ativados com sucesso");
		m.put("Utilities", "Utilitários");
		m.put("Valid signature required for head commit of this branch per branch protection rule", 
			"Assinatura válida necessária para o commit principal desta branch conforme regra de proteção de branch");
		m.put("Value", "Valor");
		m.put("Value Matcher", "Corresponder de Valor");
		m.put("Value Provider", "Provedor de Valor");
		m.put("Values", "Valores");
		m.put("Values Provider", "Provedor de Valores");
		m.put("Variable", "Variável");
		m.put("Verification Code", "Código de Verificação");
		m.put("Verification email sent, please check it", "Email de verificação enviado, por favor verifique");
		m.put("Verify", "Verificar");
		m.put("View", "Visualizar");
		m.put("View Source", "Visualizar Fonte");
		m.put("View source", "Visualizar fonte");
		m.put("View statistics", "Visualizar estatísticas");
		m.put("Viewer", "Visualizador");
		m.put("Volume Mount", "Montagem de Volume");
		m.put("Volume Mounts", "Montagens de Volume");
		m.put("Vote", "Votar");
		m.put("Votes", "Votos");
		m.put("WAITING", "ESPERANDO");
		m.put("WARNING:", "AVISO:");
		m.put("Waiting", "Esperando");
		m.put("Waiting for approvals", "Aguardando aprovações");
		m.put("Waiting for test mail to come back...", "Aguardando o retorno do email de teste...");
		m.put("Watch", "Assistir");
		m.put("Watch Status", "Status de Assistir");
		m.put("Watch if involved", "Assistir se envolvido");
		m.put("Watch if involved (default)", "Assistir se envolvido (padrão)");
		m.put("Watch status changed", "Status de assistir alterado");
		m.put("Watch/Unwatch All Queried Issues", "Assistir/Não assistir Todas as Issues Consultadas");
		m.put("Watch/Unwatch All Queried Pull Requests", "Assistir/Não assistir Todas as Pull Requests Consultadas");
		m.put("Watch/Unwatch Selected Pull Requests", "Assistir/Não assistir Pull Requests Selecionadas");
		m.put("Watched. Click to unwatch", "Assistido. Clique para não assistir");
		m.put("Watchers", "Observadores");
		m.put("Web Hook", "Web Hook");
		m.put("Web Hooks", "Web Hooks");
		m.put("Web Hooks Bean", "Bean de Web Hooks");
		m.put("Web hooks saved", "Web hooks salvos");
		m.put("Webhook Url", "URL do Webhook");
		m.put("Week", "Semana");
		m.put("When", "Quando");
		m.put("When authorize a group, the group will also be authorized with the role for all child projects", 
			"Ao autorizar um grupo, o grupo também será autorizado com o papel para todos os projetos filhos");
		m.put("When authorize a project, all child projects will also be authorized with assigned roles", 
			"Ao autorizar um projeto, todos os projetos filhos também serão autorizados com os papéis atribuídos");
		m.put("When authorize a user, the user will also be authorized with the role for all child projects", 
			"Ao autorizar um usuário, o usuário também será autorizado com o papel para todos os projetos filhos");
		m.put("When determine if the user is author/committer of a git commit, all emails listed here will be checked", 
			"Ao determinar se o usuário é autor/committer de um commit git, todos os emails listados aqui serão verificados");
		m.put("When evaluating this template, below variables will be available:", "Ao avaliar este template, as variáveis abaixo estarão disponíveis:");
		m.put("When login via OneDev's built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database", 
			"Ao fazer login via o formulário interno do OneDev, as credenciais do usuário enviadas podem ser verificadas contra o autenticador definido aqui, além do banco de dados interno");
		m.put("When target branch of a pull request has new commits, merge commit of the pull request will be recalculated, and this option tells whether or not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run required builds on the new merge commit. This setting takes effect only when required builds are specified", 
			"Quando a branch alvo de um pull request tiver novos commits, o commit de merge do pull request será recalculado, e esta opção indica se deve ou não aceitar builds de pull request executados no commit de merge anterior. Se habilitado, você precisará reexecutar os builds necessários no novo commit de merge. Esta configuração só tem efeito quando builds necessários são especificados");
		m.put("When this work starts", "Quando este trabalho começar");
		m.put("When {0}", "Quando {0}");
		m.put("Whether or not created issue should be confidential", "Se a issue criada deve ser confidencial ou não");
		m.put("Whether or not multiple issues can be linked", "Se múltiplas issues podem ser vinculadas ou não");
		m.put("Whether or not multiple issues can be linked on the other side. For instance sub issues on the other side means parent issue, and multiple should be false on that side if only one parent is allowed", 
			"Se múltiplas issues podem ser vinculadas do outro lado ou não. Por exemplo, sub-issues do outro lado significam issue pai, e múltiplas devem ser falsas desse lado se apenas um pai for permitido");
		m.put("Whether or not multiple values can be specified for this field", "Se múltiplos valores podem ser especificados para este campo ou não");
		m.put("Whether or not multiple values can be specified for this param", "Se múltiplos valores podem ser especificados para este parâmetro ou não");
		m.put("Whether or not the issue should be confidential", "Se a issue deve ser confidencial ou não");
		m.put("Whether or not the link is asymmetric. A asymmetric link has different meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric", 
			"Se o link é assimétrico ou não. Um link assimétrico tem significados diferentes de lados diferentes. Por exemplo, um link 'pai-filho' é assimétrico, enquanto um link 'relacionado a' é simétrico");
		m.put("Whether or not this field accepts empty value", "Se este campo aceita valor vazio ou não");
		m.put("Whether or not this param accepts empty value", "Se este parâmetro aceita valor vazio ou não");
		m.put("Whether or not this script can be used in CI/CD jobs", "Se este script pode ser usado em jobs de CI/CD ou não");
		m.put("Whether or not this step is optional. Execution failure of an optional step will not cause the build to fail, and successful condition of subsequent steps will not take optional step into account", 
			"Se esta etapa é opcional ou não. Falha de execução de uma etapa opcional não causará falha na build, e a condição de sucesso de etapas subsequentes não levará em conta a etapa opcional");
		m.put("Whether or not to allow anonymous users to access this server", "Se permitir ou não que usuários anônimos acessem este servidor");
		m.put("Whether or not to allow creating root projects (project without parent)", "Se permitir ou não a criação de projetos raiz (projeto sem pai)");
		m.put("Whether or not to also include children of above projects", "Se incluir ou não os filhos dos projetos acima");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same machine", 
			"Se sempre puxar a imagem ao executar o container ou construir imagens ou não. Esta opção deve ser habilitada para evitar que imagens sejam substituídas por jobs maliciosos executados na mesma máquina");
		m.put("Whether or not to always pull image when run container or build images. This option should be enabled to avoid images being replaced by malicious jobs running on same node", 
			"Se sempre puxar a imagem ao executar o container ou construir imagens ou não. Esta opção deve ser habilitada para evitar que imagens sejam substituídas por jobs maliciosos executados no mesmo nó");
		m.put("Whether or not to be able to access time tracking info of issues", "Se será possível acessar informações de rastreamento de tempo das issues ou não");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities", 
			"Se criar como uma conta de serviço para fins de automação de tarefas ou não. Conta de serviço não tem senha e endereços de email, e não gerará notificações para suas atividades");
		m.put("Whether or not to create as a service account for task automation purpose. Service account does not have password and email addresses, and will not generate notifications for its activities. <b class='text-warning'>NOTE:</b> Service account is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days", 
			"Se criar como uma conta de serviço para fins de automação de tarefas ou não. Conta de serviço não tem senha e endereços de email, e não gerará notificações para suas atividades. <b class='text-warning'>NOTA:</b> Conta de serviço é um recurso empresarial. <a href='https://onedev.io/pricing' target='_blank'>Experimente grátis</a> por 30 dias");
		m.put("Whether or not to enable code management for the project", "Se habilitar gerenciamento de código para o projeto ou não");
		m.put("Whether or not to enable issue management for the project", "Se habilitar gerenciamento de issues para o projeto ou não");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project.", 
			"Se buscar objetos LFS se o pull request for aberto de um projeto diferente ou não");
		m.put("Whether or not to fetch LFS objects if pull request is opened from a different project. If this option is enabled, git lfs command needs to be installed on OneDev server", 
			"Se buscar objetos LFS se o pull request for aberto de um projeto diferente ou não. Se esta opção estiver habilitada, o comando git lfs precisa estar instalado no servidor OneDev");
		m.put("Whether or not to import forked Bitbucket repositories", "Se importar repositórios Bitbucket bifurcados ou não");
		m.put("Whether or not to import forked GitHub repositories", "Se importar repositórios GitHub bifurcados ou não");
		m.put("Whether or not to import forked GitLab projects", "Se importar projetos GitLab bifurcados ou não");
		m.put("Whether or not to import forked Gitea repositories", "Se importar repositórios Gitea bifurcados ou não");
		m.put("Whether or not to include forked repositories", "Se incluir repositórios bifurcados ou não");
		m.put("Whether or not to include this field when issue is initially opened. If not, you may include this field later when issue is transited to other states via issue transition rule", 
			"Se incluir este campo ao abrir a issue inicialmente ou não. Caso contrário, você pode incluir este campo mais tarde quando a issue for transicionada para outros estados via regra de transição de issue");
		m.put("Whether or not to input and display estimated/spent time in hours/minutes only", "Se inserir e exibir tempo estimado/gasto apenas em horas/minutos ou não");
		m.put("Whether or not to mount docker sock into job container to support docker operations in job commands<br><b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev by operating the mounted docker sock. Make sure this executor can only be used by trusted jobs if this option is enabled", 
			"Se deve ou não montar o docker sock no contêiner do trabalho para suportar operações docker nos comandos do trabalho<br><b class='text-danger'>AVISO</b>: Trabalhos maliciosos podem assumir o controle de todo o OneDev operando o docker sock montado. Certifique-se de que este executor só possa ser usado por trabalhos confiáveis se esta opção estiver habilitada");
		m.put("Whether or not to pre-populate tag mappings in next page. You may want to disable this if there are too many tags to display", 
			"Se pré-preencher mapeamentos de tags na próxima página ou não. Você pode querer desabilitar isso se houver muitas tags para exibir");
		m.put("Whether or not to require this dependency to be successful", "Se exigir que esta dependência seja bem-sucedida ou não");
		m.put("Whether or not to retrieve groups of login user. Make sure to add groups claim via token configuration of the app registered in Entra ID if this option is enabled. The groups claim should return group id (the default option) via various token types in this case", 
			"Se recuperar grupos do usuário de login ou não. Certifique-se de adicionar a reivindicação de grupos via configuração de token do aplicativo registrado no Entra ID se esta opção estiver habilitada. A reivindicação de grupos deve retornar o id do grupo (a opção padrão) via vários tipos de token neste caso");
		m.put("Whether or not to retrieve submodules. Refer to <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on how to set up clone credential above to retrieve submodules", 
			"Se recuperar submódulos ou não. Consulte <a href='https://docs.onedev.io/tutorials/cicd/clone-submodules' target='_blank'>este tutorial</a> sobre como configurar a credencial de clone acima para recuperar submódulos");
		m.put("Whether or not to run this step inside container", "Se executar esta etapa dentro do container ou não");
		m.put("Whether or not to scan recursively in above paths", "Se escanear recursivamente nos caminhos acima ou não");
		m.put("Whether or not to send notifications for events generated by yourself", "Se enviar notificações para eventos gerados por você mesmo ou não");
		m.put("Whether or not to send notifications to issue watchers for this change", "Se enviar notificações para observadores da issue sobre esta alteração ou não");
		m.put("Whether or not to show branch/tag column", "Se exibir a coluna de branch/tag ou não");
		m.put("Whether or not to show duration column", "Se exibir a coluna de duração ou não");
		m.put("Whether or not to use user avatar from a public service", "Se usar avatar de usuário de um serviço público ou não");
		m.put("Whether or not use force option to overwrite changes in case ref updating can not be fast-forwarded", 
			"Se usar a opção de força para sobrescrever alterações caso a atualização de referência não possa ser avançada rapidamente ou não");
		m.put("Whether or not user can remove own account", "Se o usuário pode remover sua própria conta ou não");
		m.put("Whether the password must contain at least one lowercase letter", "Se a senha deve conter pelo menos uma letra minúscula");
		m.put("Whether the password must contain at least one number", "Se a senha deve conter pelo menos um número");
		m.put("Whether the password must contain at least one special character", "Se a senha deve conter pelo menos um caractere especial");
		m.put("Whether the password must contain at least one uppercase letter", "Se a senha deve conter pelo menos uma letra maiúscula");
		m.put("Whole Word", "Palavra Inteira");
		m.put("Widget", "Widget");
		m.put("Widget Tab", "Aba do Widget");
		m.put("Widget Timesheet Setting", "Configuração de Timesheet do Widget");
		m.put("Will be prompted to set up two-factor authentication upon next login", "Será solicitado a configurar a autenticação de dois fatores no próximo login");
		m.put("Will be transcoded to UTF-8", "Será transcodificado para UTF-8");
		m.put("Window", "Janela");
		m.put("Window Memory", "Memória da Janela");
		m.put("With current number of users ({0}), the subscription will be active until <b>{1}</b>", 
			"Com o número atual de usuários ({0}), a assinatura estará ativa até <b>{1}</b>");
		m.put("Workflow reconciliation completed", "Reconciliação de workflow concluída");
		m.put("Working Directory", "Diretório de Trabalho");
		m.put("Write", "Escrever");
		m.put("YAML", "YAML");
		m.put("Yes", "Sim");
		m.put("You are not member of discord server", "Você não é membro do servidor Discord");
		m.put("You are rebasing source branch on top of target branch", "Você está rebaseando o branch de origem no topo do branch de destino");
		m.put("You are viewing a subset of all changes. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">show all changes</a>", 
			"Você está visualizando um subconjunto de todas as alterações. <a wicket:id=\"fullChanges\" class=\"link-primary ml-2\">mostrar todas as alterações</a>");
		m.put("You can also achieve this by adding a build docker image step to your CI/CD job and configuring the built-in registry login with an access token secret that has package write permissions", 
			"Você também pode alcançar isso adicionando um passo de construção de imagem Docker ao seu trabalho CI/CD e configurando o login do registro embutido com um segredo de token de acesso que tenha permissões de escrita de pacote");
		m.put("You have unverified <a wicket:id=\"hasUnverifiedLink\">email addresses</a>", "Você tem <a wicket:id=\"hasUnverifiedLink\">endereços de email não verificados</a>");
		m.put("You may also drop file/image to the input box, or paste image from clipboard", "Você também pode soltar arquivo/imagem na caixa de entrada ou colar imagem da área de transferência");
		m.put("You may initialize the project by <a wicket:id=\"addFiles\" class=\"link-primary\">adding files</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">setting up build spec</a>, or <a wicket:id=\"pushInstructions\" class=\"link-primary\">pushing an existing repository</a>", 
			"Você pode inicializar o projeto <a wicket:id=\"addFiles\" class=\"link-primary\">adicionando arquivos</a>, <a wicket:id=\"setupBuildSpec\" class=\"link-primary\">configurando o build spec</a>, ou <a wicket:id=\"pushInstructions\" class=\"link-primary\">enviando um repositório existente</a>");
		m.put("You selected to delete branch \"{0}\"", "Você selecionou excluir o branch \"{0}\"");
		m.put("You will be notified of any activities", "Você será notificado de quaisquer atividades");
		m.put("You've been logged out", "Você foi desconectado");
		m.put("YouTrack API URL", "URL da API do YouTrack");
		m.put("YouTrack Issue Field", "Campo de Problema do YouTrack");
		m.put("YouTrack Issue Link", "Link de Problema do YouTrack");
		m.put("YouTrack Issue State", "Estado do Problema do YouTrack");
		m.put("YouTrack Issue Tag", "Tag de Problema do YouTrack");
		m.put("YouTrack Login Name", "Nome de Login do YouTrack");
		m.put("YouTrack Password or Access Token", "Senha ou Token de Acesso do YouTrack");
		m.put("YouTrack Project", "Projeto do YouTrack");
		m.put("YouTrack Projects to Import", "Projetos do YouTrack para Importar");
		m.put("Your email address is now verified", "Seu endereço de email agora está verificado");
		m.put("Your primary email address is not verified", "Seu endereço de email principal não está verificado");
		m.put("[Any state]", "[Qualquer estado]");
		m.put("[Reset Password] Please Reset Your OneDev Password", "[Redefinir Senha] Por favor, redefina sua senha do OneDev");
		m.put("a boolean indiciating whether or not topic comment can be created directly by replying the email", 
			"um booleano indicando se o comentário do tópico pode ser criado diretamente respondendo ao email");
		m.put("a new agent token will be generated each time this button is pressed", "um novo token de agente será gerado cada vez que este botão for pressionado");
		m.put("a string representing body of the event. May be <code>null</code>", "uma string representando o corpo do evento. Pode ser <code>null</code>");
		m.put("a string representing event detail url", "uma string representando a URL de detalhes do evento");
		m.put("a string representing summary of the event", "uma string representando o resumo do evento");
		m.put("access [{0}]", "acesso [{0}]");
		m.put("active", "ativo");
		m.put("add another order", "adicionar outro pedido");
		m.put("adding .onedev-buildspec.yml", "adicionando .onedev-buildspec.yml");
		m.put("after specified date", "após a data especificada");
		m.put("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information.  A <code>null</code> value means that the notification can not be unsubscribed", 
			"um <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>objeto</a> contendo informações de cancelamento de inscrição. Um valor <code>null</code> significa que a notificação não pode ser cancelada");
		m.put("and more", "e mais");
		m.put("archived", "arquivado");
		m.put("artifacts", "artefatos");
		m.put("assign to me", "atribuir a mim");
		m.put("authored by", "autorado por");
		m.put("backlog ", "backlog");
		m.put("base", "base");
		m.put("before specified date", "antes da data especificada");
		m.put("branch the build commit is merged into", "branch no qual o commit de build foi mesclado");
		m.put("branch the job is running against", "branch no qual o trabalho está sendo executado");
		m.put("branch {0}", "branch {0}");
		m.put("branches", "branches");
		m.put("build", "build");
		m.put("build is successful for any job and branch", "build bem-sucedido para qualquer trabalho e branch");
		m.put("build is successful for any job on branches \"{0}\"", "build bem-sucedido para qualquer trabalho nos branches \"{0}\"");
		m.put("build is successful for jobs \"{0}\" on any branch", "build bem-sucedido para os trabalhos \"{0}\" em qualquer branch");
		m.put("build is successful for jobs \"{0}\" on branches \"{1}\"", "build bem-sucedido para os trabalhos \"{0}\" nos branches \"{1}\"");
		m.put("builds", "builds");
		m.put("cURL Example", "Exemplo de cURL");
		m.put("choose a color for this state", "escolha uma cor para este estado");
		m.put("cluster:lead", "líder");
		m.put("cmd-k to show command palette", "cmd-k para mostrar o painel de comandos");
		m.put("code commit", "commit de código");
		m.put("code is committed", "o código está comprometido");
		m.put("code is committed to branches \"{0}\"", "código é commitado nos branches \"{0}\"");
		m.put("code is committed to branches \"{0}\" with message \"{1}\"", "o código está comprometido nos branches \"{0}\" com a mensagem \"{1}\"");
		m.put("code is committed with message \"{0}\"", "o código está comprometido com a mensagem \"{0}\"");
		m.put("commit message contains", "mensagem de commit contém");
		m.put("commits", "commits");
		m.put("committed by", "commitado por");
		m.put("common", "comum");
		m.put("common ancestor", "ancestral comum");
		m.put("container:image", "Imagem");
		m.put("copy", "copiar");
		m.put("ctrl-k to show command palette", "ctrl-k para mostrar o painel de comandos");
		m.put("curl Command Line", "Comando de Linha curl");
		m.put("curl Path", "Caminho curl");
		m.put("default", "padrão");
		m.put("descending", "descendente");
		m.put("disabled", "desabilitado");
		m.put("does not have any value of", "não possui nenhum valor de");
		m.put("duration", "duração");
		m.put("enclose with ~ to query hash/message", "envolva com ~ para consultar hash/mensagem");
		m.put("enclose with ~ to query job/version", "envolva com ~ para consultar trabalho/versão");
		m.put("enclose with ~ to query name/ip/os", "envolva com ~ para consultar nome/ip/os");
		m.put("enclose with ~ to query name/path", "envolva com ~ para consultar nome/caminho");
		m.put("enclose with ~ to query name/version", "envolva com ~ para consultar nome/versão");
		m.put("enclose with ~ to query path/content/reply", "envolva com ~ para consultar caminho/conteúdo/resposta");
		m.put("enclose with ~ to query title/description/comment", "envolva com ~ para consultar título/descrição/comentário");
		m.put("exclude", "excluir");
		m.put("false", "falso");
		m.put("files with ext \"{0}\"", "arquivos com extensão \"{0}\"");
		m.put("find build by number", "encontrar build pelo número");
		m.put("find build with this number", "encontrar build com este número");
		m.put("find issue by number", "encontrar problema pelo número");
		m.put("find pull request by number", "encontrar pull request pelo número");
		m.put("find pull request with this number", "encontrar pull request com este número");
		m.put("forked from <a wicket:id=\"forkedFrom\"></a>", "forked de <a wicket:id=\"forkedFrom\"></a>");
		m.put("found 1 agent", "encontrado 1 agente");
		m.put("found 1 build", "encontrado 1 build");
		m.put("found 1 comment", "encontrado 1 comentário");
		m.put("found 1 issue", "encontrado 1 problema");
		m.put("found 1 package", "encontrado 1 pacote");
		m.put("found 1 project", "encontrado 1 projeto");
		m.put("found 1 pull request", "encontrado 1 pull request");
		m.put("found 1 user", "encontrado 1 usuário");
		m.put("found {0} agents", "encontrados {0} agentes");
		m.put("found {0} builds", "encontrados {0} builds");
		m.put("found {0} comments", "encontrados {0} comentários");
		m.put("found {0} issues", "encontrados {0} problemas");
		m.put("found {0} packages", "encontrados {0} pacotes");
		m.put("found {0} projects", "encontrados {0} projetos");
		m.put("found {0} pull requests", "encontrados {0} pull requests");
		m.put("found {0} users", "encontrados {0} usuários");
		m.put("has any value of", "tem qualquer valor de");
		m.put("head", "cabeçalho");
		m.put("in current commit", "no commit atual");
		m.put("ineffective", "ineficaz");
		m.put("inherited", "herdado");
		m.put("initial", "inicial");
		m.put("is empty", "está vazio");
		m.put("is not empty", "não está vazio");
		m.put("issue", "problema");
		m.put("issue:Number", "Número");
		m.put("issues", "problemas");
		m.put("job", "tarefa");
		m.put("key ID: ", "ID da chave:");
		m.put("lines", "linhas");
		m.put("link:Multiple", "Múltiplo");
		m.put("log", "log");
		m.put("manage job", "gerenciar tarefa");
		m.put("markdown:heading", "Título");
		m.put("markdown:image", "Imagem");
		m.put("may not be empty", "não pode estar vazio");
		m.put("merged", "mesclado");
		m.put("month:Apr", "Abr");
		m.put("month:Aug", "Ago");
		m.put("month:Dec", "Dez");
		m.put("month:Feb", "Fev");
		m.put("month:Jan", "Jan");
		m.put("month:Jul", "Jul");
		m.put("month:Jun", "Jun");
		m.put("month:Mar", "Mar");
		m.put("month:May", "Mai");
		m.put("month:Nov", "Nov");
		m.put("month:Oct", "Out");
		m.put("month:Sep", "Set");
		m.put("n/a", "n/d");
		m.put("new field", "novo campo");
		m.put("no activity for {0} days", "sem atividade por {0} dias");
		m.put("on file {0}", "no arquivo {0}");
		m.put("opened", "aberto");
		m.put("opened <span wicket:id=\"submitDate\"></span>", "aberto <span wicket:id=\"submitDate\"></span>");
		m.put("or match another value", "ou corresponder a outro valor");
		m.put("order more", "ordenar mais");
		m.put("outdated", "desatualizado");
		m.put("pack", "pacote");
		m.put("package", "pacote");
		m.put("packages", "pacotes");
		m.put("personal", "pessoal");
		m.put("pipeline", "pipeline");
		m.put("project of the running job", "projeto da tarefa em execução");
		m.put("property", "propriedade");
		m.put("pull request", "pull request");
		m.put("pull request #{0}", "pull request #{0}");
		m.put("pull request and code review", "pull request e revisão de código");
		m.put("pull request to any branch is discarded", "pull request para qualquer branch é descartado");
		m.put("pull request to any branch is merged", "pull request para qualquer branch é mesclado");
		m.put("pull request to any branch is opened", "pull request para qualquer branch está aberto");
		m.put("pull request to branches \"{0}\" is discarded", "pull request para branches \"{0}\" é descartado");
		m.put("pull request to branches \"{0}\" is merged", "pull request para branches \"{0}\" é mesclado");
		m.put("pull request to branches \"{0}\" is opened", "pull request para branches \"{0}\" está aberto");
		m.put("pull requests", "pull requests");
		m.put("reconciliation (need administrator permission)", "reconciliação (necessita permissão de administrador)");
		m.put("reports", "relatórios");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> object to be notified", 
			"representa o objeto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Build.java' target='_blank'>build</a> a ser notificado");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk", 
			"representa o <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> sendo aberto via service desk");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified", 
			"representa o objeto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> a ser notificado");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>package</a> object to be notified", 
			"representa o objeto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Pack.java' target='_blank'>pacote</a> a ser notificado");
		m.put("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified", 
			"representa o objeto <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> a ser notificado");
		m.put("represents the <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> object to be notified", 
			"representa o objeto <a href='https://javadoc.io/static/org.eclipse.jgit/org.eclipse.jgit/5.13.0.202109080827-r/org/eclipse/jgit/revwalk/RevCommit.html' target='_blank'>commit</a> a ser notificado");
		m.put("represents the exception encountered when open issue via service desk", "representa a exceção encontrada ao abrir problema via service desk");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>", 
			"representa o <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>problema</a> não inscrito");
		m.put("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>", 
			"representa o <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> não inscrito");
		m.put("request to change", "solicitação de alteração");
		m.put("root", "raiz");
		m.put("root url of OneDev server", "url raiz do servidor OneDev");
		m.put("run job", "executar tarefa");
		m.put("search in this revision will be accurate after indexed", "a pesquisa nesta revisão será precisa após indexada");
		m.put("service", "serviço");
		m.put("severity:CRITICAL", "Crítico");
		m.put("severity:HIGH", "Alto");
		m.put("severity:LOW", "Baixo");
		m.put("severity:MEDIUM", "Médio");
		m.put("skipped {0} lines", "puladas {0} linhas");
		m.put("space", "espaço");
		m.put("state of an issue is transited", "o estado de um problema é transitado");
		m.put("step template", "modelo de etapa");
		m.put("submit", "enviar");
		m.put("tag the job is running against", "tag contra a qual a tarefa está sendo executada");
		m.put("tag {0}", "tag {0}");
		m.put("tags", "tags");
		m.put("the url to set up user account", "a URL para configurar a conta do usuário");
		m.put("time aggregation link", "link de agregação de tempo");
		m.put("touching specified path", "tocando o caminho especificado");
		m.put("transit manually by any user", "transitar manualmente por qualquer usuário");
		m.put("transit manually by any user of roles \"{0}\"", "transitar manualmente por qualquer usuário com os papéis \"{0}\"");
		m.put("true", "verdadeiro");
		m.put("true for html version, false for text version", "verdadeiro para a versão HTML, falso para a versão de texto");
		m.put("up to date", "atualizado");
		m.put("url following which to verify email address", "URL para verificar o endereço de e-mail");
		m.put("url to reset password", "URL para redefinir a senha");
		m.put("value needs to be enclosed in brackets", "o valor precisa estar entre colchetes");
		m.put("value needs to be enclosed in parenthesis", "o valor precisa estar entre parênteses");
		m.put("value should be quoted", "o valor deve ser citado");
		m.put("w%02d", "w%02d");
		m.put("week:Fri", "Sex");
		m.put("week:Mon", "Seg");
		m.put("week:Sat", "Sáb");
		m.put("week:Sun", "Dom");
		m.put("week:Thu", "Qui");
		m.put("week:Tue", "Ter");
		m.put("week:Wed", "Qua");
		m.put("widget:Tabs", "Abas");
		m.put("you may show this page later via incompatibilities link in help menu", "você pode mostrar esta página mais tarde via link de incompatibilidades no menu de ajuda");
		m.put("{0} Month(s)", "{0} Mês(es)");
		m.put("{0} activities on {1}", "{0} atividades em {1}");
		m.put("{0} additions & {1} deletions", "{0} adições & {1} exclusões");
		m.put("{0} ahead", "{0} à frente");
		m.put("{0} behind", "{0} atrás");
		m.put("{0} branches", "{0} branches");
		m.put("{0} build(s)", "{0} build(s)");
		m.put("{0} child projects", "{0} projetos filhos");
		m.put("{0} commits", "{0} commits");
		m.put("{0} commits ahead of base branch", "{0} commits à frente da branch base");
		m.put("{0} commits behind of base branch", "{0} commits atrás da branch base");
		m.put("{0} day", "{0} dia");
		m.put("{0} days", "{0} dias");
		m.put("{0} edited {1}", "{0} editou {1}");
		m.put("{0} files", "{0} arquivos");
		m.put("{0} forks", "{0} forks");
		m.put("{0} hour", "{0} hora");
		m.put("{0} hours", "{0} horas");
		m.put("{0} inaccessible activities", "{0} atividades inacessíveis");
		m.put("{0} minute", "{0} minuto");
		m.put("{0} minutes", "{0} minutos");
		m.put("{0} reviewed", "{0} revisado");
		m.put("{0} second", "{0} segundo");
		m.put("{0} seconds", "{0} segundos");
		m.put("{0} tags", "{0} tags");
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
