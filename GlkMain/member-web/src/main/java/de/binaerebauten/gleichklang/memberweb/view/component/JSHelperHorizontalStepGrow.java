package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.ui.JavaScript;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Helper Class for step-wise resizing HTML Elements for a base size.
 */
public class JSHelperHorizontalStepGrow {

    private static final String ELEMENT_PLACEHOLDER = "${element}";
    private static final String STEPSIZE_PLACEHOLDER = "${size}";

    private static final String jsCode = "!function(){var a=document.getElementsByClassName(\"${element}\");for(index=0;index<a.length;index++){var b=a[index];b.style.height=\"auto\";var c=b.offsetHeight%${size},d=b.offsetHeight+(${size}-c);b.style.height=d+\"px\"}}();";

    private final Map<String, Integer> resizableElements = new HashMap<>();

    public JSHelperHorizontalStepGrow(String element, Integer size) {
        Objects.nonNull(element);
        Objects.nonNull(size);

        resizableElements.put(element, size);
    }

    /**
     * Recalculate elements set new height step-wise.
     *
     * Anonymous JavaScript function gets called and resizes all configured html elements.
     */
    public void update() {
        for (Map.Entry<String, Integer> element : resizableElements.entrySet()) {
            excuteJS(element.getKey(), element.getValue());
        }
    }

    /**
     * Send configures JavaScript code and sends it the client.
     *
     * @param element html class name
     * @param size base height size
     */
    private void excuteJS(String element, Integer size) {
        final String jsCommand = jsCode.replace(ELEMENT_PLACEHOLDER, element).replace(STEPSIZE_PLACEHOLDER, size.toString());

        JavaScript.getCurrent().execute(jsCommand);
    }

    public void addElement(String element, Integer size) {
        if (element == null || size == null) {
            return;
        }
        resizableElements.put(element, size);
    }
}
