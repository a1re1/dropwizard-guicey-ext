package ru.vyarus.guicey.gsp.error

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.ConfigOverride
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.support.app.SampleTemplateResource
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 29.01.2019
 */
@UseDropwizardApp(value = App, configOverride = [
        @ConfigOverride(key = "server.rootPath", value = "/rest/*")
])
class ShowTraceOnErrorPageTest extends Specification {

    def "Check error mapping"() {

        when: "accessing not existing asset"
        def res = new URL("http://localhost:8080/notexisting.html").text
        then: "error page"
        res.startsWith("ru.vyarus.guicey.gsp.app.filter.AssetError: Error serving asset /notexisting.html: 404")

        when: "accessing not existing template"
        res = new URL("http://localhost:8080/notexisting.ftl").text
        then: "error page"
        res.startsWith("javax.ws.rs.NotFoundException: Template notexisting.ftl not found")

        when: "accessing not existing path"
        res = new URL("http://localhost:8080/notexisting/").text
        then: "error page"
        res.startsWith("javax.ws.rs.NotFoundException: Template notexisting/ not found")

        when: "error processing template"
        res = new URL("http://localhost:8080/sample/error").text
        then: "error page"
        res.startsWith("javax.ws.rs.WebApplicationException: HTTP 500 Internal Server Error")

        when: "error processing template"
        res = new URL("http://localhost:8080/sample/error2").text
        then: "error page"
        res.startsWith("javax.ws.rs.WebApplicationException: error")

        when: "direct 404 rest response"
        res = new URL("http://localhost:8080/sample/notfound").text
        then: "error page"
        res.startsWith("ru.vyarus.guicey.gsp.app.rest.support.TemplateRestCodeError: Error processing template rest call app/sample/notfound: 404")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleTemplateResource)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .errorPage("error2.ftl")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
