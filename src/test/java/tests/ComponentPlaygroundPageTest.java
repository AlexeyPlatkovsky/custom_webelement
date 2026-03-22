package tests;

import org.testng.annotations.Test;
import pages.ComponentPlaygroundPage;
import utils.assertions.iAssert;

import java.util.List;
import java.util.Set;

@Test(groups = {"ui"}, singleThreaded = true)
public class ComponentPlaygroundPageTest extends LocalFixtureBaseTest {

    @Test
    public void topLevelInputStoresEnteredValueTest() {
        ComponentPlaygroundPage page = new ComponentPlaygroundPage();
        String text = "local component fixture";

        page.openPage();
        page.enterGlobalText(text);

        iAssert.equalsTo(page.getGlobalText(), text, "global input does not contain entered value");
    }

    @Test(description = "Compare performance of cached and non-cached elements on the local fixture")
    public void compareCachedAndNonCachedElementsPerformanceTest() {
        ComponentPlaygroundPage page = new ComponentPlaygroundPage();
        String text = "cache benchmark";

        page.openPage();
        page.enterGlobalText(text);
        long cached = page.getCachedElementFindTime();

        page.openPage();
        page.enterGlobalText(text);
        long nonCached = page.getNonCachedElementFindTime();

        iAssert.isTrue(cached < nonCached, "cached lookup is slower than non-cached lookup");
    }

    @Test
    public void resultItemsRemainUniqueTest() {
        ComponentPlaygroundPage page = new ComponentPlaygroundPage();

        page.openPage();
        List<String> items = page.getAllResultItems();

        iAssert.isTrue(Set.copyOf(items).size() == items.size(),
                "fixture result list contains duplicate entries");
    }

    @Test
    public void classLevelFindByScopesComponentUiLookupTest() {
        ComponentPlaygroundPage page = new ComponentPlaygroundPage();
        String text = "class scoped value";

        page.openPage();
        page.enterClassScopedText(text);

        iAssert.equalsTo(page.getClassScopedValue(), text,
                "class-level component scope does not resolve the expected section");
    }

    @Test
    public void fieldLevelFindByScopesComponentUiLookupTest() {
        ComponentPlaygroundPage page = new ComponentPlaygroundPage();
        String text = "field scoped value";

        page.openPage();
        page.enterFieldScopedText(text);

        iAssert.equalsTo(page.getFieldScopedValue(), text,
                "field-level component scope does not resolve the expected section");
    }

    @Test
    public void fieldLevelFindByOverridesClassLevelScopeInUiTest() {
        ComponentPlaygroundPage page = new ComponentPlaygroundPage();
        String text = "field override value";

        page.openPage();
        page.enterFieldOverrideText(text);

        iAssert.equalsTo(page.getFieldOverrideValue(), text,
                "field-level component scope does not override the class-level scope");
    }
}
