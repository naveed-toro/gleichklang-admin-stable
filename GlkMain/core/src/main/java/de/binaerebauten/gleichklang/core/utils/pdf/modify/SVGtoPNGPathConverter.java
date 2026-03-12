package de.binaerebauten.gleichklang.core.utils.pdf.modify;


import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import de.binaerebauten.gleichklang.core.utils.pdf.HTMLElement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Domi on 01.02.2017.
 */
public class SVGtoPNGPathConverter implements HTMLElement.HTMLContentModifier {

    private static final Logger LOG = LoggerFactory.getLogger(SVGtoPNGPathConverter.class);

    private final AppUrlBuilder appUrlBuilder;

    public SVGtoPNGPathConverter(AppUrlBuilder appUrlBuilder) {
        this.appUrlBuilder = appUrlBuilder;
    }

    @Override
    public String modify(String content) {
        final Document doc = Jsoup.parse(content);

        final Elements imageElements = doc.select("img");
        imageElements.forEach(element -> {
            String source = element.attr("src");
            source = changeSuffix(source);

            try {
                source = toAbsolutePath(source);
            } catch (URISyntaxException e) {
                LOG.error(e.getLocalizedMessage());
            }
            element.attr("src", changeSuffix(source));
        });

        return doc.body().html();
    }


    /**
     * Changes the suffix from svg to png.
     *
     * @param filepath path to file
     * @return path with changed suffix
     */
    private String changeSuffix(String filepath) {
        return filepath.replace(".svg", ".png");
    }


    /**
     * Returns absolute path to image resource.
     *
     * @param filepath absolute or relative path to image resource
     * @return absolute path to image resource
     * @throws URISyntaxException
     */
    private String toAbsolutePath(String filepath) throws URISyntaxException {
        final URI fileUri = new URI(filepath);

        if (fileUri.isAbsolute()) {
            return filepath;
        }

        return appUrlBuilder.toAppPath(filepath).toString();
    }
}
