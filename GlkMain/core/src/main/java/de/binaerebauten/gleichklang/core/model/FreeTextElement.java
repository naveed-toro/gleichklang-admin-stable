package de.binaerebauten.gleichklang.core.model;

import com.google.common.base.Strings;

/**
 * Created by Domi on 30.03.2017.
 */
public class FreeTextElement {

    private String header;
    private String content;

    public FreeTextElement() {    }

    public FreeTextElement(String header, String content) {
        this.header = header;
        this.content = content;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean isEmpty() {
        return Strings.isNullOrEmpty(header) && Strings.isNullOrEmpty(content);
    }

    public String getCompleteText() {
        final String printHeader = header != null ? "<b>" + header + "</b> " : "";
        return String.format("%s%s", printHeader , content != null ? content : "");
    }
}
