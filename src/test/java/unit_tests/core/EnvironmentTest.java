package unit_tests.core;

import core.Environment;
import org.testng.annotations.Test;
import utils.assertions.iAssert;

@Test(groups = {"unit"})
public class EnvironmentTest {

    @Test
    public void stripsSubdomainPrefixFromUrl() {
        iAssert.equalsTo(Environment.normalizeRootUrl("www.example.com"), "example.com",
                "single-label subdomain prefix should be stripped");
    }

    @Test
    public void stripsMultiSegmentSubdomainPrefix() {
        iAssert.equalsTo(Environment.normalizeRootUrl("env1.staging.example.com"), "staging.example.com",
                "only the first label should be stripped, leaving the rest of the host");
    }

    @Test
    public void stripsPortAlongWithSubdomainPrefix() {
        iAssert.equalsTo(Environment.normalizeRootUrl("www.example.com:8080"), "example.com",
                "port should be stripped together with the subdomain prefix");
    }

    @Test
    public void stripsTrailingSlashAlongWithSubdomainPrefix() {
        iAssert.equalsTo(Environment.normalizeRootUrl("www.example.com/"), "example.com",
                "trailing slash should be stripped together with the subdomain prefix");
    }

    @Test
    public void absoluteUrlWithSchemePassesThroughUnchanged() {
        iAssert.equalsTo(Environment.normalizeRootUrl("https://example.com"), "https://example.com",
                "URL with a scheme (https://) does not match the subdomain pattern and should pass through");
    }

    @Test
    public void absoluteUrlTrailingSlashIsTrimmed() {
        iAssert.equalsTo(Environment.normalizeRootUrl("https://example.com/"), "https://example.com",
                "trailing slash on an absolute URL should be trimmed by cutExtraEndSlashes");
    }
}
