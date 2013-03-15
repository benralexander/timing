package timtest

import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Results)
class ResultsTests {

    void testRestBuilderInstalled() {
        RestBuilder restBuilder = new  RestBuilder()

        def resp = restBuilder.get("http://bard.nih.gov/api/v15/assays")

        assert resp!=null
    }
}
