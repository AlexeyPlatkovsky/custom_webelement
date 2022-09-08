package utils.properties;

import utils.properties.annotations.FilePath;
import utils.properties.annotations.Property;

@FilePath(value = "./src/main/resources/webelement.properties")
public class WebElementProperties {
    @Property(value = "webelement.border.highlight")
    public static String WEBELEMENT_BORDER_SHOULD_BE_HIGHLIGHTED;

    @Property(value = "webelement.border.highlight.width")
    public static String WEBELEMENT_BORDER_WIDTH;

    @Property(value = "webelement.border.highlight.color")
    public static String WEBELEMENT_BORDER_COLOR;

    @Property(value = "webelement.background.highlight")
    public static String WEBELEMENT_BACKGROUND_SHOULD_BE_HIGHLIGHTED;

    @Property(value = "webelement.background.highlight.color")
    public static String WEBELEMENT_BACKGROUND_COLOR;
}
