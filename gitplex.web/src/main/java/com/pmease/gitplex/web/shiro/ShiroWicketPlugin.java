package com.pmease.gitplex.web.shiro;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.aop.AuthenticatedAnnotationHandler;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.authz.aop.GuestAnnotationHandler;
import org.apache.shiro.authz.aop.PermissionAnnotationHandler;
import org.apache.shiro.authz.aop.RoleAnnotationHandler;
import org.apache.shiro.authz.aop.UserAnnotationHandler;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.ResetResponseException;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.util.lang.Args;


/**
 * Borrowed from:
 * https://github.com/55minutes/fiftyfive-wicket/blob/master/fiftyfive-wicket-shiro/src/main/java/fiftyfive/wicket/shiro/ShiroWicketPlugin.java
 * 
 * 
 * Enhances Wicket to integrate closely with the Apache Shiro security
 * framework. With the {@code ShiroWicketPlugin} installed in your Wicket
 * application, you will gain the following features:
 * <ul>
 * <li>You can use all of Shiro's authorization annotations
 *     (like
 *     {@link org.apache.shiro.authz.annotation.RequiresAuthentication @RequiresAuthentication}
 *     and
 *     {@link org.apache.shiro.authz.annotation.RequiresPermissions @RequiresPermissions})
 *     on Wicket Pages. The {@code ShiroWicketPlugin} will ensure that only
 *     authorized users can access these pages, and will show an appropriate
 *     error page or login page otherwise.
 *     See {@link #isInstantiationAuthorized isInstantiationAuthorized()}.
 * </li>
 * <li>You can also use the same Shiro annotations on individual components,
 *     like Links and Panels. The {@code ShiroWicketPlugin} will automatically
 *     hide these components from unauthorized users.
 *     See {@link #isActionAuthorized isActionAuthorized()}.
 * </li>
 * <li>You can access Shiro directly at any time in your Wicket code
 *     by calling
 *     {@link org.apache.shiro.SecurityUtils#getSubject SecurityUtils.getSubject()}.
 *     This gives you access to the rich set of security operations on the
 *     Shiro {@link org.apache.shiro.subject.Subject Subject} that represents
 *     the current user.
 * </li>
 * <li>Any uncaught Shiro
 *     {@link AuthorizationException AuthorizationExceptions}
 *     will be handled gracefully by redirecting the user to the
 *     login page or an unauthorized page (by default, the home page).
 *     This allows you to implement
 *     comprehensive security rules using Shiro at any tier of your
 *     application and be confident that your UI will handle them
 *     appropriately.
 *     See {@link #onException onException()}.
 * </li>
 * </ul>
 * <h2>Installation</h2>
 * Before you can use the {@code ShiroWicketPlugin}, you must have Shiro
 * properly added to your application's {@code web.xml} file. Refer to the
 * <a href="package-summary.html">package summary</a> of this Javadoc
 * for a brief tutorial.
 * <h3>{@code Application.init()}</h3>
 * Once Shiro itself is installed, adding {@code ShiroWicketPlugin} can be as
 * simple as adding one line to your Wicket application {@code init()}:
 * <pre class="example">
 * public class MyApplication extends WebApplication
 * {
 *     &#064;Override
 *     protected void init()
 *     {
 *         super.init();
 *         new ShiroWicketPlugin().install(this);
 *     }
 * }</pre>
 * Most developers will want to customize the login page.
 * The more complex real-world installation is thus:
 * <pre class="example">
 * public class MyApplication extends WebApplication
 * {
 *     &#064;Override
 *     protected void init()
 *     {
 *         super.init();
 *         new ShiroWicketPlugin()
 *             .mountLoginPage("login", MyLoginPage.class)
 *             .install(this);
 *     }
 * }</pre>
 * 
 */
@SuppressWarnings("serial")
public class ShiroWicketPlugin
    extends AbstractRequestCycleListener
    implements IAuthorizationStrategy,
               IUnauthorizedComponentInstantiationListener
{
    /**
     * The key that will be used to obtain a localized message
     * when access is denied due to the user be unauthorized;
     * for example, "you are not allowed to access that page".
     */
    public static final String UNAUTHORIZED_MESSAGE_KEY = "unauthorized";

    /**
     * The key that will be used to obtain a localized message
     * when access is denied due to the user be unauthenticated;
     * for example, "you need to be logged in to continue".
     */
    public static final String LOGIN_REQUIRED_MESSAGE_KEY = "loginRequired";

    /**
     * The key that will be used to obtain a localized message
     * when logout is performed; for example, "you have been logged out".
     */
    public static final String LOGGED_OUT_MESSAGE_KEY = "loggedOut";

	private static final MetaDataKey<AuthorizationException> EXCEPTION_KEY =
        new MetaDataKey<AuthorizationException>() {};
    
    private static final MetaDataKey<ShiroWicketPlugin> PLUGIN_KEY =
        new MetaDataKey<ShiroWicketPlugin>() {};

    private static final AuthorizingAnnotationHandler[] HANDLERS =
        new AuthorizingAnnotationHandler[] {
            new AuthenticatedAnnotationHandler(),
            new GuestAnnotationHandler(),
            new PermissionAnnotationHandler(),
            new RoleAnnotationHandler(),
            new UserAnnotationHandler()
        };

    /**
     * Returns the {@code ShiroWicketPlugin} instance that has been installed
     * in the current Wicket application. This is a convenience method that
     * only works within a Wicket thread, and it assumes that
     * {@link #install install()} has already been called.
     * 
     * @throws IllegalStateException if there is no Wicket application bound
     *                               to the current thread, or if a
     *                               {@code ShiroWicketPlugin} has not been
     *                               installed.
     */
    public static ShiroWicketPlugin get()
    {
        Application app = Application.get();
        if(null == app)
        {
            throw new IllegalStateException(
                "No wicket application is bound to the current thread."
            );
        }
        ShiroWicketPlugin plugin = app.getMetaData(PLUGIN_KEY);
        if(null == plugin)
        {
            throw new IllegalStateException(
                "A ShiroWicketPlugin has not been installed in this Wicket " +
                "application. You must call ShiroWicketPlugin.install() in " +
                "your application init()."
            );
        }
        return plugin;
    }
    
    /**
     * Register the specified {@code ShiroWicketPlugin} in the application so that
     * {@link #get} will work. You should never need to call this method directly, unless you
     * are subclassing and overriding the {@link #install} method.
     */
    public static void set(Application app, ShiroWicketPlugin plugin)
    {
        app.setMetaData(PLUGIN_KEY, plugin);
    }
    
    
    private String loginPath = "login";
    private String logoutPath = "logout";
    private Class<? extends Page> loginPage = LoginPage.class;
    private Class<? extends Page> logoutPage = LogoutPage.class;
    private Class<? extends Page> unauthorizedPage = null;
    private boolean unauthorizedRedirect = true;
    
    /**
     * The login page class as provided to {@link #mountLoginPage}; the default is
     * {@link LoginPage}.
     */
    public Class<? extends Page> getLoginPage()
    {
        return loginPage;
    }

    /**
     * The logout page class as provided to {@link #mountLogoutPage}; the default is
     * {@link LogoutPage}.
     */
    public Class<? extends Page> getLogoutPage()
    {
        return logoutPage;
    }

    /**
     * Set the bookmarkable page that will be displayed when an <em>unauthenticated</em> user
     * attempts to access a page that requires authentication.
     * 
     * @param mountPath The bookmarkable URI where the login page will be mounted when
     *                  {@link #install install} is called. The default is {@code "/login"}.
     *                  May be {@code null}, in which case the login page will not be mounted.
     *                  You would want to pass {@code null}, for example, if you use your home
     *                  page as the login page, since in that case the home page is already
     *                  implicitly mounted on {@code "/"}.
     * 
     * @param loginPage The page to use as the login page when the user needs to be
     *                  authenticated. Cannot be {@code null}. The default is a simple
     *                  out-of-the-box {@link LoginPage}.
     * 
     * @return {@code this} to allow chaining
     */
    public ShiroWicketPlugin mountLoginPage(String mountPath, Class<? extends Page> loginPage)
    {
        Args.notNull(loginPage, "loginPage");
        this.loginPath = mountPath;
        this.loginPage = loginPage;
        return this;
    }

    /**
     * Set the bookmarkable page that will be loaded to perform logout when the
     * {@link fiftyfive.wicket.shiro.markup.LogoutLink LogoutLink} is clicked.
     *
     * @param mountPath The bookmarkable URI where the logout page will be mounted when
     *                  {@link #install install} is called. The default is {@code "/logout"}.
     * 
     * @param logoutPage The page to load when the user clicks the
     *                   {@link fiftyfive.wicket.shiro.markup.LogoutLink LogoutLink}. This page
     *                   is responsible for actually logging the user out of the Shiro system.
     *                   Cannot be {@code null}. The default is {@link LogoutPage},
     *                   which should be sufficient for most applications.
     * 
     * @return {@code this} to allow chaining
     */
    public ShiroWicketPlugin mountLogoutPage(String mountPath, Class<? extends Page> logoutPage)
    {
        Args.notNull(logoutPage, "logoutPage");
        this.logoutPath = mountPath;
        this.logoutPage = logoutPage;
        return this;
    }
    
    /**
     * The page class that was set via {@link #setUnauthorizedPage}; otherwise the
     * application home page.
     */
    public Class<? extends Page> getUnauthorizedPage()
    {
        return unauthorizedPage != null ? unauthorizedPage : Application.get().getHomePage();
    }
    
    /**
     * The redirect flag that was set via {@link #setUnauthorizedPage}; {@code true} by default.
     */
    public boolean getUnauthorizedRedirect()
    {
        return unauthorizedRedirect;
    }
    
    /**
     * Set the bookmarkable page that will be displayed when an <em>authenticated</em> user
     * attempts to access a page that they are not allowed to see. By default it is
     * {@code null}, indicating that the application home page should be used.
     * 
     * @param page The page to display when the user is unauthorized; if {@code null}, the
     *             application home page will be used
     *
     * @param redirect If {@code true}, a 302 redirect will be performed to display the page;
     *                 if {@code false}, no redirect will occur and the URL will not change.
     *                 The latter is appropriate for an error page.
     * 
     * @return {@code this} to allow chaining
     */
    public ShiroWicketPlugin setUnauthorizedPage(Class<? extends Page> page, boolean redirect)
    {
        this.unauthorizedPage = page;
        this.unauthorizedRedirect = redirect;
        return this;
    }
    
    /**
     * The mount path for the login page as provided to {@link #mountLoginPage}; the default is
     * {@code "login"}.
     */
    public String getLoginPath()
    {
        return loginPath;
    }

    /**
     * The mount path for the logout action as provided to {@link #mountLogoutPage}; the default is
     * {@code "logout"}.
     */
    public String getLogoutPath()
    {
        return logoutPath;
    }

    /**
     * Installs this {@code ShiroWicketPlugin} by doing the following:
     * <ul>
     * <li>Sets itself as the {@link IAuthorizationStrategy}</li>
     * <li>And as the {@link IUnauthorizedComponentInstantiationListener}</li>
     * <li>And as an {@link IRequestCycleListener}</li>
     * <li>Mounts the login page</li>
     * <li>Mounts the logout page</li>
     * </ul>
     */
    public void install(WebApplication app)
    {
        Args.notNull(app, "app");
        
        ISecuritySettings settings = app.getSecuritySettings();
        settings.setAuthorizationStrategy(this);
        settings.setUnauthorizedComponentInstantiationListener(this);
        app.getRequestCycleListeners().add(this);
        
        // Mount bookmarkable URLs
        if(this.loginPath != null)
        {
            app.mount(new MountedMapper(this.loginPath, this.loginPage));
        }
        if(this.logoutPath != null)
        {
            app.mount(new MountedMapper(this.logoutPath, this.logoutPage));
        }
        
        // Install self in app metadata so that static get() can work
        ShiroWicketPlugin.set(app, this);
    }
    
    // Start feedback message callbacks --------------------------------------
    
    /**
     * Called by {@link LogoutPage} once the user has been logged out.
     * The default implementation adds a feedback message to the session that says
     * "you have been logged out". To override or localize this message,
     * define {@code loggedOut} in your application properties. You can disable the
     * message entirely by defining {@code loggedOut} as an empty string.
     */
    public void onLoggedOut()
    {
        String message = getLocalizedMessage(LOGGED_OUT_MESSAGE_KEY, "You have been logged out.");
        if(message != null && !message.matches("^\\s*$"))
        {
            // Invalidate current session and create a new one.
            // We need a new session because otherwise our feedback message won't "stick".
            Session session = Session.get();
            session.replaceSession();
        
            // Add localized "you have been logged out" message to session
            session.info(message);
        }
    }
    
    /**
     * Invoked by {@code ShiroWicketPlugin} when an anonymous or remembered user has tried to
     * access a page that requires authentication. The default implementation places a
     * "you need to be logged in to continue" feedback message in the session.
     * To override or localize this message,
     * define {@code loginRequired} in your application properties. You can disable the
     * message entirely by defining {@code loginRequired} as an empty string.
     */
    public void onLoginRequired()
    {
        String message = getLocalizedMessage(
            LOGIN_REQUIRED_MESSAGE_KEY,
            "You need to be logged in to continue.");
        
        if(message != null && !message.matches("^\\s*$"))
        {
            // We need a new session because otherwise our feedback message won't "stick".
            Session session = Session.get();
            session.bind();
        
            // Add localized "you have been logged out" message to session
            session.info(message);
        }
    }
    
    /**
     * Invoked by {@code ShiroWicketPlugin} when the user has tried to access a page
     * but lacks the necessary role or permission. The default implementation places a
     * "sorry, you are not allowed to access that page" feedback message in the session.
     * To override or localize this message,
     * define {@code unauthorized} in your application properties. You can disable the
     * message entirely by defining {@code unauthorized} as an empty string.
     */
    public void onUnauthorized()
    {
        String message = getLocalizedMessage(
            UNAUTHORIZED_MESSAGE_KEY,
            "Sorry, you are not allowed to access that page.");
        
        if(message != null && !message.matches("^\\s*$"))
        {
            // We need a new session because otherwise our feedback message won't "stick".
            Session session = Session.get();
            session.bind();
        
            // Add localized "sorry, you are not allowed to access that page" message to session
            session.error(message);
        }
    }
    
    // End feedback message callbacks ----------------------------------------
    
    // Start IRequestCycleListener methods -----------------------------------
    
    /**
     * React to an uncaught Exception by redirecting the browser to
     * the unauthorized page or login page if appropriate. This method will automatically be
     * called by Wicket if this plugin was installed by the standard {@link #install install()}
     * mechanism, via the {@link IRequestCycleListener} system.
     * This allows uncaught Shiro exceptions thrown by the backend to be
     * handled gracefully by the Wicket layer.
     * <p>
     * If the exception is a Shiro {@link AuthorizationException}, redirect
     * to the unauthorized page or login page depending on the type of error.
     * If the exception is not a Shiro {@link AuthorizationException}
     * return {@code null}.
     * 
     * @param cycle The current request cycle, as provided by Wicket.
     * 
     * @param error The exception to handle. If it is not a subclass of
     *              Shiro's {@link AuthorizationException}, this method will
     *              not have any effect.
     * 
     * @return A {@link RenderPageRequestHandler} redirect to the login
     *         page if the error is due to the user being
     *         <em>unauthenticated</em>;
     *         {@link RenderPageRequestHandler} to render the unauthorized page
     *         if the error is due to the user being
     *         <em>unauthorized</em>.
     */
    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception error)
    {
        Class<? extends Page> respondWithPage = null;
        RedirectPolicy redirectPolicy = RedirectPolicy.NEVER_REDIRECT;
        
        if(error instanceof AuthorizationException)
        {
            AuthorizationException ae = (AuthorizationException) error;
            if(authenticationNeeded(ae))
            {
                if(loginPage != null)
                {
                    onLoginRequired();
                    
                    // Create a RestartResponseAtInterceptPageException to set the intercept,
                    // even though we don't throw the exception. (The magic happens in the
                    // RestartResponseAtInterceptPageException constructor.)
                    new RestartResponseAtInterceptPageException(loginPage);
                    respondWithPage = loginPage;
                    redirectPolicy = RedirectPolicy.ALWAYS_REDIRECT;
                }
            }
            else
            {
                onUnauthorized();
                
                if(this.unauthorizedRedirect || (
                    cycle.getRequest() instanceof WebRequest &&
                    ((WebRequest) cycle.getRequest()).isAjax()))
                {
                    redirectPolicy = RedirectPolicy.ALWAYS_REDIRECT;
                }
                respondWithPage = getUnauthorizedPage();
            }
        }
        if(respondWithPage != null)
        {
            return new RenderPageRequestHandler(new PageProvider(respondWithPage), redirectPolicy);
        }
        return null;
    }

    // End IRequestCycleListener methods -------------------------------------

    // Start IUnauthorizedComponentInstantiationListener methods -------------

    /**
     * Determine what caused the unauthorized instantiation of the given
     * component. If access was denied due to being unauthenticated, and
     * the login page specified in the constructor was not {@code null},
     * call {@link #onLoginRequired} and redirect to the login page.
     * <p>
     * Otherwise, access was denied due to authorization failure (e.g. insufficient privileges),
     * call {@link #onUnauthorized} and render the unauthorized page (which is the home page by
     * default).
     * 
     * @param component The component that failed to initialize due to 
     *                  authorization or authentication failure
     * 
     * @throws {@link ResetResponseException} to render the login page or unauthorized page
     * 
     * @throws UnauthorizedInstantiationException the login page
     *                                            has not been configured (i.e. is {@code null})
     */
    @Override
	public void onUnauthorizedInstantiation(Component component)
    {
        AuthorizationException cause;
        RequestCycle rc = RequestCycle.get();
        cause = rc.getMetaData(EXCEPTION_KEY);
        
        // Show appropriate login or error page if possible
        IRequestHandler handler = onException(rc, cause);
        if(handler != null)
        {
            throw new ResetResponseException(handler) {};
        }
        
        // Otherwise bubble up the error
        UnauthorizedInstantiationException ex;
        ex = new UnauthorizedInstantiationException(component.getClass());
        ex.initCause(cause);
        throw ex;
    }

    // End IUnauthorizedComponentInstantiationListener methods ---------------

    // Start IAuthorizationStrategy methods ----------------------------------
    
    /**
     * Performs authorization checks for the {@link Component#RENDER RENDER}
     * action only. Other actions are always allowed.
     * <p>
     * If the action is {@code RENDER}, the component class <em>and its
     * superclasses</em> are checked for the presence of
     * {@link org.apache.shiro.authz.annotation Shiro annotations}.
     * <p>
     * The absence of any Shiro annotation means that the component may be
     * rendered, and {@code true} is returned. Otherwise, each annotation is
     * evaluated against the current Shiro Subject. If any of the requirements
     * dictated by the annotations fail, {@code false} is returned and
     * rendering for the component will be skipped.
     * <p>
     * For example, this link will be hidden if the user is already
     * authenticated:
     * <pre class="example">
     * &#064;RequiresGuest
     * public class LoginLink extends StatelessLink
     * {
     *     ...
     * }</pre>
     */
    @Override
	public boolean isActionAuthorized(Component component, Action action)
    {
        if(Component.RENDER.equals(action))
        {
            try
            {
                assertAuthorized(component.getClass());
            }
            catch(AuthorizationException ae)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * If {@code componentClass} is a subclass of {@link Page},
     * return {@code true} or {@code false} based on evaluation of any
     * {@link org.apache.shiro.authz.annotation Shiro annotations}
     * that are present on the page class declaration, <em>plus any annotations
     * present on its superclasses</em>.
     * <p>
     * The absence of any Shiro annotation means that the page can always be
     * instantiated, meaning {@code true} will always be returned. Otherwise,
     * each annotation is evaluated against the current Shiro Subject. If any
     * of the requirements dictated by the annotations fail, {@code false} will
     * be returned and instantiation will be denied.
     * <p>
     * For example, this page may only be instantiated if the user has
     * explictly authenticated (i.e. not just "remembered" via cookie) and
     * additionally has the "admin" role:
     * <pre class="example">
     * &#064;RequiresAuthentication
     * &#064;RequiresRoles("admin")
     * public class TopSecretPage extends WebPage
     * {
     *     ...
     * }</pre>
     * If {@code componentClass} is not a subclass of Page, always return
     * {@code true}. Non-page components may always be instantiated; however
     * their rendering can be controlled via annotations. See
     * {@link #isActionAuthorized isActionAuthorized()}.
     */
    @Override
	public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
        Class<T> componentClass)
    {
        if(Page.class.isAssignableFrom(componentClass))
        {
            try
            {
                assertAuthorized(componentClass);
            }
            catch(AuthorizationException ae)
            {
                // Store exception for use later in the request by onUnauthorizedInstantiation()
                RequestCycle.get().setMetaData(EXCEPTION_KEY, ae);
                return false;
            }
        }
        return true;
    }

    // End IAuthorizationStrategy methods ------------------------------------
    
    /**
     * Looks up a localized string from the application properties.
     */
    protected String getLocalizedMessage(String key, String theDefault)
    {
        return Application.get().getResourceSettings().getLocalizer().getString(
            key,
            null,
            null,
            theDefault);
    }

    /**
     * Returns {@code true} if the reason the user was denied access is
     * because she needs to authenticate.
     */
    protected boolean authenticationNeeded(AuthorizationException cause)
    {
        // IMPLEMENTATION NOTE - A simple solution would be:
        //   return ! SecurityUtils.getSubject().isAuthenticated();
        // In other words, the user needs to authenticate if she is not
        // already logged in (this is how it is done in Wicket auth-roles).
        // However this does not take into account the subtle difference in
        // Shiro between "authenticated" and "remembered" states. To ensure the
        // correct behavior we have to inspect the actual exception to see what
        // action to take.
        
        boolean needLogin = false;
        
        // Check if Shiro blocked access due to authentication
        if(cause instanceof UnauthenticatedException)
        {
            needLogin = true;
            
            // But... there is a rare case where Shiro can throw an
            // UnauthenticatedException even when the user is already logged
            // in. If the user is logged in and the page was annotated with
            // @RequiresGuest, Shiro throws an UnauthenticatedException, which
            // which is very misleading. Our only way to detect this scenario
            // is to parse the exception message. Yes, this is a hack.
            
            String msg = cause.getMessage();
            String guestError = "Attempting to perform a guest-only operation.";
            if(msg != null && msg.startsWith(guestError))
            {
                needLogin = false;
            }
        }
        return needLogin;
    }

    /**
     * @throws AuthorizationException if the given class, or any of its
     *         superclasses, has a Shiro annotation that fails its
     *         authorization check.
     */
    private void assertAuthorized(final Class<?> cls)
        throws AuthorizationException
    {
        Collection<Annotation> annotations = findAnnotations(cls);
        for(Annotation annot : annotations)
        {
            for(AuthorizingAnnotationHandler h : HANDLERS)
            {
                h.assertAuthorized(annot);
            }
        }
    }
    
    /**
     * Returns all annotations present on the given class and all of its
     * superclasses.
     */
    private Collection<Annotation> findAnnotations(final Class<?> cls)
    {
        List<Annotation> annots = new ArrayList<Annotation>(5);
        Class<?> currClass = cls;
        while(currClass != null)
        {
            annots.addAll(Arrays.asList(currClass.getDeclaredAnnotations()));
            currClass = currClass.getSuperclass();
        }
        return annots;
    }
}