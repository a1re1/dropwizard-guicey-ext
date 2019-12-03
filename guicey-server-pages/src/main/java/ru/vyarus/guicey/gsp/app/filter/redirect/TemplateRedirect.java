package ru.vyarus.guicey.gsp.app.filter.redirect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.gsp.app.asset.AssetLookup;
import ru.vyarus.guicey.gsp.app.rest.DirectTemplateResource;
import ru.vyarus.guicey.gsp.app.rest.mapping.ViewRestLookup;
import ru.vyarus.guicey.gsp.app.rest.support.TemplateAnnotationFilter;
import ru.vyarus.guicey.gsp.app.util.PathUtils;
import ru.vyarus.guicey.gsp.app.util.TemplateRequest;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Performs redirection of template request into rest context. Note that even if no special rest
 * mapped for template, it would be rendered with the default {@link DirectTemplateResource}.
 * <p>
 * Rest resource convention: /[rest context]/[prefix]/[path from request], where
 * [prefix] is application registration name by default (but may be configured). Additional
 * mappings may be configured to sub url, so redirection to different rest "branches" may be performed,
 * depending on called url.
 * <p>
 * If resource is annotated with {@link ru.vyarus.guicey.gsp.views.template.Template} annotation (it should!) then
 * {@link TemplateAnnotationFilter} will detect it and set specified template into context {@link TemplateContext}.
 * <p>
 * Important: resources must use {@link ru.vyarus.guicey.gsp.views.template.TemplateView} as base template model class
 * in order to properly support {@link ru.vyarus.guicey.gsp.views.template.Template} annotation.
 *
 * @author Vyacheslav Rusakov
 * @since 03.12.2018
 */
public class TemplateRedirect {

    private static final ThreadLocal<TemplateContext> CONTEXT_TEMPLATE = new ThreadLocal<>();

    private final Logger logger = LoggerFactory.getLogger(TemplateRedirect.class);

    private final Servlet restServlet;
    private final String app;
    private final String mapping;
    private final ViewRestLookup views;
    private final AssetLookup assets;
    private final ErrorRedirect errorRedirect;

    // full rest path (applicationContextPath + rootPath)
    private String rootPath;
    // server.applicationContextPath
    private String restContextPath;
    // server.rootPath
    private String restServletMapping;

    public TemplateRedirect(final Servlet restServlet,
                            final String app,
                            final String mapping,
                            final ViewRestLookup views,
                            final AssetLookup assets,
                            final ErrorRedirect errorRedirect) {
        this.restServlet = restServlet;
        this.app = app;
        this.mapping = mapping;
        this.assets = assets;
        this.views = views;
        this.errorRedirect = errorRedirect;
    }

    /**
     * @param contextPath    main context mapping path
     * @param servletMapping rest servlet mapping path
     */
    public void setRootPath(final String contextPath, final String servletMapping) {
        this.restContextPath = contextPath;
        this.restServletMapping = servletMapping;
        this.rootPath = PathUtils.path(contextPath, servletMapping);
    }

    /**
     * Redirect template request into rest resource. Jersey will select appropriate resource by path, or
     * default {@link DirectTemplateResource} will be used.
     *
     * @param request  template request
     * @param response template response
     * @param page     requested template path (cleared for matching)
     * @throws IOException      on dispatching errors
     * @throws ServletException on dispatching errors
     */
    public void redirect(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final String page) throws IOException, ServletException {
        CONTEXT_TEMPLATE.set(new TemplateContext(app,
                mapping,
                assets,
                errorRedirect,
                request,
                response));
        try {
            final String path = PathUtils.path(rootPath, views.lookup(page));
            logger.debug("Rendering template path: {}", path);
            // this moment is especially important for admin apps where context could be radically different
            restServlet.service(
                    new TemplateRequest(request, path, restContextPath, restServletMapping), response);

        } finally {
            CONTEXT_TEMPLATE.remove();
        }
    }

    /**
     * @return custom error pages support
     */
    public ErrorRedirect getErrorRedirect() {
        return errorRedirect;
    }

    /**
     * @return root rest mapping path
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * @return thread bound template context or null
     */
    public static TemplateContext templateContext() {
        return CONTEXT_TEMPLATE.get();
    }
}
