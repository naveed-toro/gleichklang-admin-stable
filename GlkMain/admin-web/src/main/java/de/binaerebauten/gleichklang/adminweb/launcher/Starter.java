package de.binaerebauten.gleichklang.adminweb.launcher;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

public class Starter
{
	private static String webappDirLocation = "src/main/webapp/";
	
	private Starter()
	{}
	
	public static void main(String[] args) throws Exception
	{
		start().await();
	}

	public static Server start()
			throws IOException, ServletException, LifecycleException
	{
		final String contextPath = "/gkadm";
		final Tomcat tomcat = new Tomcat();

		// The port that we should run on can be set into an environment
		// variable
		// Look for that variable and default to 8080 if it isn't there.
		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty())
		{
			webPort = "8081";
		}
		tomcat.setPort(Integer.valueOf(webPort));
		tomcat.addWebapp(contextPath, new File(webappDirLocation).getAbsolutePath());

		tomcat.start();
		return tomcat.getServer();
	}

}
