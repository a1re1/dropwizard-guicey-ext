package ru.vyarus.guicey.jdbi.dbi;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

/**
 * Helper for implementing lazy initialization. Useful in initialization part where bundles are configured.
 * For example, to construct some dropwizard integration object and use it in guice integrations later.
 *
 * @param <T> provided object type
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
@FunctionalInterface
public interface ConfigAwareProvider<T> {

    /**
     * Called to provide required object.
     *
     * @param environment   environment instance
     * @param configuration configuration instance
     * @return object instance
     */
    T get(Environment environment, Configuration configuration);
}
