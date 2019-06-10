package ru.vyarus.guicey.gsp.error

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.test.spock.ConfigOverride
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.views.template.ManualErrorHandling
import ru.vyarus.guicey.gsp.views.template.Template
import spock.lang.Specification

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper

/**
 * @author Vyacheslav Rusakov
 * @since 09.06.2019
 */
@UseDropwizardApp(value = App, configOverride = [
        @ConfigOverride(key = "server.rootPath", value = "/rest/*")
])
class ExceptionMapperInterceptionTest extends Specification {

    def "Check error mapping"() {

        when: "accessing throwing resource"
        def res = new URL("http://localhost:8080/err").text
        then: "gsp error page"
        res == "Error: WebApplicationException"

        when: "accessing throwing resource with disabled error mechanism"
        res = new URL("http://localhost:8080/err2").text
        then: "manual error handling"
        res == "handled!"
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(ServerPagesBundle.builder().build())

            // pure dropwizard bundle
            bootstrap.addBundle(ServerPagesBundle.app("test.app", "/app", "/")
                    .errorPage("error.ftl")
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(ErrRest)
            environment.jersey().register(ExHandler)
        }
    }


    @Path("/test.app/")
    @Template
    public static class ErrRest {

        @Path("/err")
        @GET
        public String get() {
            throw new IllegalArgumentException("Sample error")
        }

        @ManualErrorHandling
        @Path("/err2")
        @GET
        public String get2() {
            throw new IllegalArgumentException("Sample error")
        }
    }

    public static class ExHandler implements ExceptionMapper<IllegalArgumentException> {
        @Override
        Response toResponse(IllegalArgumentException exception) {
            return Response.ok("handled!").build()
        }
    }
}