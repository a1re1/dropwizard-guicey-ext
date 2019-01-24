package ru.vyarus.guicey.gsp

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.test.spock.ConfigOverride
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 24.01.2019
 */
@UseDropwizardApp(value = App, configOverride = [
        @ConfigOverride(key = "server.rootPath", value = "/rest/*"),
        @ConfigOverride(key = "server.applicationContextPath", value = "/prefix/")
])
class NonRootIndexTemplate extends Specification {

    def "Check app mapped"() {

        when: "accessing root"
        String res = new URL("http://localhost:8080/prefix/").text
        then: "index page"
        res.contains("page: /")

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            // pure dropwizard bundle
            bootstrap.addBundle(ServerPagesBundle.app("app", "/app", "/")
                    .indexPage("template.ftl")
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}