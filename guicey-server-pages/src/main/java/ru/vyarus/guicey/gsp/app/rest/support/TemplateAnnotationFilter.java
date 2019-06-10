package ru.vyarus.guicey.gsp.app.rest.support;

import ru.vyarus.guicey.gsp.views.template.ManualErrorHandling;
import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Request filter for {@link Template} annotated resources read configured template path (to be used in model).
 * Record matched resource class so relative templates could be checked relative to class even
 * when template path is specified directly into model.
 *
 * @author Vyacheslav Rusakov
 * @since 03.12.2018
 */
@Template
@Provider
public class TemplateAnnotationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo info;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final Class<?> resourceClass = info.getResourceClass();
        final Template template = resourceClass.getAnnotation(Template.class);
        if (template != null) {
            final TemplateContext context = TemplateContext.getInstance();
            // remember resource class to check relative templates
            context.setResourceClass(resourceClass);
            final String tpl = template.value();
            // could be empty when annotation used for marking resource only
            if (!tpl.isEmpty()) {
                context.setAnnotationTemplate(tpl);
            }
            final Method method = info.getResourceMethod();
            context.setManualErrorHandling(resourceClass.isAnnotationPresent(ManualErrorHandling.class)
                    || (method != null && method.isAnnotationPresent(ManualErrorHandling.class)));
        }
    }
}
