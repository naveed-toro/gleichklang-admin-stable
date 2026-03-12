package de.binaerebauten.gleichklang.core.utils;

import com.google.common.base.Strings;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class StringUtils
{
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
	private static final Pattern EMAIL_REGEX_WITH_MASKED_MAILS = Pattern.compile("[a-zA-Z0-9._%+-]+(@|&#64;|(\\s?[(\\[]|\\s)[aA][tT]([)\\]]\\s?|\\s))[a-zA-Z0-9.-]+(\\.|(\\s?[(\\[]|\\s)[dD][oO][tT]([)\\]]\\s?|\\s))[a-zA-Z0-9]{2,4}");
	private static final Pattern EMAIL_REGEX = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9]{2,4}");
	
	private StringUtils()
	{
	}
	
	public static String cutString(String value, int maxSize)
	{
		if (Strings.isNullOrEmpty(value)) return "";
		
		if (maxSize < 0 || value.length() <= maxSize) return value;
		
		if (maxSize == 0) return "";
		if (maxSize == 1) return "?";
		if (maxSize == 2) return "??";
		if (maxSize == 3) return "...";
		
		return value.substring(0, maxSize - 4) + "...";
	}
	
	public static String deSanitizeSpecialCharacters(String sanitizedString)
	{
		String result = sanitizedString;
		
		if (Strings.isNullOrEmpty(sanitizedString)) return "";
		
		if (result.indexOf("&amp;") != -1)
			result = result.replaceAll("&amp;", "&");
		if (result.indexOf("#34;") != -1)
			result = result.replaceAll("&#34;", "\"");
		if (result.indexOf("#64;") != -1)
			result = result.replaceAll("&#64;", "@");
		if (result.indexOf("lt;") != -1)
			result = result.replaceAll("&lt;", "<");
		if (result.indexOf("gt;") != -1)
			result = result.replaceAll("&gt;", ">");
		if (result.indexOf("#39;") != -1)
			result = result.replaceAll("&#39;", "'");
		if (result.indexOf("#43;") != -1)
			result = result.replaceAll("&#43;", "+");
		if (result.indexOf("#61;") != -1)
			result = result.replaceAll("&#61;", "=");
		if (result.indexOf("#96;") != -1)
			result = result.replaceAll("&#96;", "`");
		
		return result;
	}
	
	public static String timeToString(LocalDateTime time)
	{
		if (time == null) return "";
		
		return time.format(FORMATTER);
	}
	
	public static String replaceFileExtension(String filename, String newExtension)
	{
		final int dotIndex = filename.lastIndexOf(".");
		
		if (dotIndex != -1)
			return filename.substring(0, dotIndex + 1).concat(newExtension);
		
		return filename.concat("." + newExtension);
	}
	
	public static String durationToString(Duration duration)
	{
		if (duration == null) return "undefined";
		
		return durationToString(duration.getSeconds());
	}
	
	public static String durationToString(long seconds)
	{
		return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
	}
	
	public static String getPercentageString(Long dividend, Long divisor)
	{
		if (dividend == null || divisor == null || divisor == 0) return "0 %";
		
		final float value = (float) dividend / (float) divisor;
		
		if (value < 0) return "less than 0";
		if (value > 1) return "greater than 1";
		
		return Math.round(value * 10000) / 100.0 + " %";
	}
	
	public static String getEmailRegex(boolean withMaskedMails)
	{
		return withMaskedMails ? EMAIL_REGEX_WITH_MASKED_MAILS.pattern() : EMAIL_REGEX.pattern();
	}
	
	public static boolean isValidEmail(String email)
	{
		if (Strings.isNullOrEmpty(email)) return false;
		return EMAIL_REGEX.matcher(email).matches();
	}
	
	public static boolean containsValidEmail(String text, boolean withMaskedMails)
	{
		if (Strings.isNullOrEmpty(text)) return false;
		return (withMaskedMails ? EMAIL_REGEX_WITH_MASKED_MAILS : EMAIL_REGEX).matcher(text).find();
	}
	
	public static String removeInvalidFilePathSigns(String value)
	{
		return value.replaceAll("[?*<>.,\\\\+:=/\";\\[\\]|^²³]", "");
	}
}
