package de.binaerebauten.gleichklang.core.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * This rest controller expose the {@link BuildInfo} as json, which can be used
 * as alive test.
 */
@RestController
@PropertySource("classpath:/build.properties")
public class AppInfoApi
{
    private static final String NO_BUILD_TIME = "No build time";

    private String buildTime;

    private String buildVersion;

    private String buildRevision;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init()
    {
        buildVersion = environment.getProperty("build.version");

        // fix for eclipse: can't replace property placeholder maven.build.time
        try
        {
            buildTime = environment.getProperty("build.time", NO_BUILD_TIME);
            buildRevision = environment.getProperty("build.revision");
        }
        catch (final IllegalArgumentException e)
        {
            buildTime = NO_BUILD_TIME;
            buildRevision = "undefined";
        }
    }

    /**
     * Returns the build info as json.
     *
     * @return the build info
     */
    @RequestMapping(value = "build")
    public BuildInfo getBuildInfo()
    {
        return new BuildInfo(buildTime, buildVersion, buildRevision);
    }

}
