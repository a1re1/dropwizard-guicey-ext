package ru.vyarus.guicey.gsp.views.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;
import io.dropwizard.views.View;
import ru.vyarus.guicey.gsp.app.filter.redirect.ErrorRedirect;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import java.nio.charset.Charset;

/**
 * View template rendering model. Must be used as base class for models instead of pure {@link View}.
 * <p>
 * Template name may be specified directly (within constructor) or automatically detected from {@link Template}
 * resource annotation. If template path starts with "/" it's considered absolute and searched directly
 * within classpath, otherwise template is considered relative to one of configured classpath locations.
 * Note that {@link Template} annotation defines templates relative to annotated class.
 * <p>
 * Provides additional information about server pages application and current request through
 * {@link #getContext()}. Error pages could access actual exception with {@link #getError()}.
 *
 * @author Vyacheslav Rusakov
 * @since 22.10.2018
 */
public class TemplateView extends View {

    private final TemplateContext context;
    private final WebApplicationException error;

    /**
     * Template obtained from {@link Template} annotation on resource.
     */
    public TemplateView() {
        this(null);
    }

    /**
     * If template name is null, it will be obtained from {@link Template} annotation on resource.
     *
     * @param templatePath template path or null (to use annotation value)
     */
    public TemplateView(@Nullable final String templatePath) {
        this(templatePath, null);
    }

    /**
     * If template name is null, it will be obtained from {@link Template} annotation on resource.
     *
     * @param templatePath template path or null (to use annotation value)
     * @param charset      charset or null
     */
    public TemplateView(@Nullable final String templatePath, @Nullable final Charset charset) {
        // template could be either absolute or relative
        super(TemplateContext.getInstance().lookupTemplatePath(templatePath), charset);
        this.context = TemplateContext.getInstance();
        this.error = ErrorRedirect.getContextError();
    }

    /**
     * Note that this object is the only way to get original request path because templates are always rendered
     * in rest endpoints after server redirect.
     *
     * @return additional info about current template.
     */
    @JsonIgnore
    public TemplateContext getContext() {
        return context;
    }

    /**
     * Returns exception object only during rendering of configured error page
     * (from {@link ru.vyarus.guicey.gsp.ServerPagesBundle.Builder#errorPage(int, String)}).
     * For all other cases (from error pages) method is useless.
     *
     * @return exception object or null (for normal template rendering)
     */
    @JsonIgnore
    public WebApplicationException getError() {
        return error;
    }

    /**
     * Shortcut for {@code getError().getResponse().getStatus()}. Shortcut created because direct expression
     * can't be used in freemarker expression.
     *
     * @return status code from context error or -1 if no context error
     * @see #getError()
     */
    @JsonIgnore
    public int getErrorCode() {
        return error != null ? error.getResponse().getStatus() : -1;
    }

    /**
     * Method intended to be used in very simple error pages in order to quickly show stacktrace.
     * <p>
     * Note that in case of direct error code return (404, 500 etc) exception will be "empty"
     * (exception instance will be created but not thrown).
     *
     * @return current stacktrace as string or null if no context error
     * @see #getError()
     */
    @JsonIgnore
    public String getErrorTrace() {
        return error != null ? Throwables.getStackTraceAsString(error) : null;
    }
}
