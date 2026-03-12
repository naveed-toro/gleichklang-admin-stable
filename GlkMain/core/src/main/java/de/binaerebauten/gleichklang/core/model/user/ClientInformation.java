package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "client_information")
public class ClientInformation extends BaseEntity
{
	public enum Browser
	{
		CHROME,
		EDGE,
		FIREFOX,
		IE,
		OPERA,
		SAFARI,
		UNKNOWN
	}
	
	public enum OS
	{
		ANDROID,
		IOS,
		LINUX,
		MAC_OSX,
		WINDOWS,
		UNKNOWN
	}
	
	public enum Device
	{
		MOBILE,
		TABLET,
		DESKTOP;
		
		public static Device getDefault()
		{
			return DESKTOP;
		}
	}
	
	@NotNull
	@ManyToOne
	private User user;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private Browser browser = Browser.UNKNOWN;
	
	@NotNull
	@Column(name = "browser_major_version")
	private int browserMajorVersion;
	
	@NotNull
	@Column(name = "browser_minor_version")
	private int browserMinorVersion;
	
	@Column(name = "browser_outdated")
	private boolean browserOutdated;
	
	@Column(name = "browser_height")
	private int browserHeight;
	
	@Column(name = "browser_width")
	private int browserWidth;
	
	private String country;
	
	@Column(name = "touch_device")
	private boolean touchDevice;
	
	private String language;
	
	@Enumerated(EnumType.STRING)
	private Device layout;
	
	@Column(name = "screen_height")
	private int screenHeight;
	
	@Column(name = "screen_width")
	private int screenWidth;
	
	//excludes summertime
	@Column(name = "timezone_offset")
	private int timezoneOffset;

	@Column(name = "last_login")
	private boolean lastSeen;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private OS os = OS.UNKNOWN;
	
	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
	
	public Browser getBrowser()
	{
		return browser;
	}
	
	public void setBrowser(Browser browser)
	{
		this.browser = browser;
	}
	
	public int getBrowserMajorVersion()
	{
		return browserMajorVersion;
	}
	
	public void setBrowserMajorVersion(int browserMajorVersion)
	{
		this.browserMajorVersion = browserMajorVersion;
	}
	
	public int getBrowserMinorVersion()
	{
		return browserMinorVersion;
	}
	
	public void setBrowserMinorVersion(int browserMinorVersion)
	{
		this.browserMinorVersion = browserMinorVersion;
	}
	
	public boolean isBrowserOutdated()
	{
		return browserOutdated;
	}
	
	public void setBrowserOutdated(boolean browserOutdated)
	{
		this.browserOutdated = browserOutdated;
	}
	
	public int getBrowserHeight()
	{
		return browserHeight;
	}
	
	public void setBrowserHeight(int browserHeight)
	{
		this.browserHeight = browserHeight;
	}
	
	public int getBrowserWidth()
	{
		return browserWidth;
	}
	
	public void setBrowserWidth(int browserWidth)
	{
		this.browserWidth = browserWidth;
	}
	
	public String getCountry()
	{
		return country;
	}
	
	public void setCountry(String country)
	{
		this.country = country;
	}
	
	public boolean isTouchDevice()
	{
		return touchDevice;
	}
	
	public void setTouchDevice(boolean touchDevice)
	{
		this.touchDevice = touchDevice;
	}
	
	public String getLanguage()
	{
		return language;
	}
	
	public void setLanguage(String language)
	{
		this.language = language;
	}
	
	public Device getLayout()
	{
		return layout;
	}
	
	public void setLayout(Device layout)
	{
		this.layout = layout;
	}
	
	public int getScreenHeight()
	{
		return screenHeight;
	}
	
	public void setScreenHeight(int screenHeight)
	{
		this.screenHeight = screenHeight;
	}
	
	public int getScreenWidth()
	{
		return screenWidth;
	}
	
	public void setScreenWidth(int screenWidth)
	{
		this.screenWidth = screenWidth;
	}
	
	public int getTimezoneOffset()
	{
		return timezoneOffset;
	}
	
	public void setTimezoneOffset(int timezoneOffset)
	{
		this.timezoneOffset = timezoneOffset;
	}
	
	public OS getOs()
	{
		return os;
	}
	
	public void setOs(OS os)
	{
		this.os = os;
	}

	public boolean getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(boolean lastSeen) {
		this.lastSeen = lastSeen;
	}
}
