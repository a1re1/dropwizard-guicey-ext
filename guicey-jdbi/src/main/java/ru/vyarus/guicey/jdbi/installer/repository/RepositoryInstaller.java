package ru.vyarus.guicey.jdbi.installer.repository;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.skife.jdbi.v2.sqlobject.SqlObjectFactory;
import org.skife.jdbi.v2.sqlobject.UnitHandleDing;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.guice.ext.core.generator.DynamicClassGenerator;

import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;

/**
 * Recognize classes annotated with {@link JdbiRepository} and register them. Such classes may be then
 * injected as usual beans and used as usual daos. All daos will participate in the thread-bound transaction,
 * declared by transaction annotation, transaction template or manually with unit manager.
 * <p>
 * Dao may use any guice-related annotations because beans participate in guice aop. This is done by creating
 * special guice-managed proxy class (where guice could apply aop). These proxies delegate all method calls to
 * JDBI-managed proxies.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.jdbi.tx.InTransaction default annotation
 * @see ru.vyarus.guicey.jdbi.tx.TransactionTemplate for template usage
 * @see ru.vyarus.guicey.jdbi.unit.UnitManager for low level usage without transaction
 * @since 4.12.2016
 */
public class RepositoryInstaller implements FeatureInstaller<Object>, BindingInstaller {

    private final Reporter reporter = new Reporter(RepositoryInstaller.class, "repositories = ");
    private UnitHandleDing ding;

    @Override
    public boolean matches(final Class<?> type) {
        return type.getAnnotation(JdbiRepository.class) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void install(final Binder binder, final Class<? extends T> type, final boolean lazy) {
        Preconditions.checkState(!lazy, "@LazyBinding not supported");
        // jdbi on demand proxy with custom ding
        final T jdbiProxy = SqlObjectFactory.instance(type, getDing(binder));
        binder.requestInjection(jdbiProxy);
        // prepare non abstract class for implementation (instantiated by guice)
        final Class<? extends T> guiceType = DynamicClassGenerator.generate(type);
        binder.bind((Class<T>) type).to(guiceType).in(Singleton.class);
        // interceptor registered for each dao and redirect calls to actual jdbi proxy
        // (at this point all guice interceptors are already involved)
        binder.bindInterceptor(Matchers.subclassesOf(type), Matchers.any(), (MethodInterceptor) invocation -> {
            try {
                return invocation.getMethod().invoke(jdbiProxy, invocation.getArguments());
            } catch (InvocationTargetException th) {
                // avoid exception wrapping (simpler to handle outside)
                throw th.getCause();
            }
        });
        reporter.line(String.format("(%s)", type.getName()));
    }

    @Override
    public void report() {
        reporter.report();
    }

    private UnitHandleDing getDing(final Binder binder) {
        // have to use shared ding instance
        if (ding == null) {
            ding = new UnitHandleDing();
            binder.requestInjection(ding);
        }
        return ding;
    }
}