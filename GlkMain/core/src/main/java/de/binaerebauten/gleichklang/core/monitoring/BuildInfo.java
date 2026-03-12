package de.binaerebauten.gleichklang.core.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is used to report the build version, time and revision as json
 * via {@AppInfoApi}.
 */
public class BuildInfo
{
	@JsonProperty("build_time")
	private String buildTime;

	@JsonProperty("build_version")
	private String buildVersion;

	@JsonProperty("build_revision")
	private String buildRevision;

	/**
	 * Required for jackson json serailization.
	 */
	public BuildInfo()
	{
	}

	public BuildInfo(String buildTime, String buildVersion, String buildRevision)
	{
		this.buildTime = buildTime;
		this.buildVersion = buildVersion;
		this.buildRevision = buildRevision;
	}

	public String getBuildTime()
	{
		return buildTime;
	}

	public String getBuildVersion()
	{
		return buildVersion;
	}

	public String getBuildRevision()
	{
		return buildRevision;
	}
}
