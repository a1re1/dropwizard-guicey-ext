package ru.vyarus.guicey.spa

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2017
 */
@UseDropwizardApp(value = App, config = 'src/test/resources/flat.yml')
class FlatAdminMappingTest extends AbstractTest {

    def "Check spa mapped"() {

        when: "accessing app"
        String res = get("http://localhost:8080/admin/app")
        then: "index page"
        res.contains("Sample page")

        when: "accessing not existing page"
        res = get("http://localhost:8080/admin/app/some")
        then: "error"
        res.contains("Sample page")

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(SpaBundle.adminApp("app", "/app", "/app").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}