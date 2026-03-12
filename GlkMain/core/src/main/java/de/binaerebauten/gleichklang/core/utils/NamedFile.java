package de.binaerebauten.gleichklang.core.utils;

import com.google.common.base.Strings;

import java.io.File;

/**
 * For GF-414 to prevent wrong images in the client through caching because of same file names
 */
public class NamedFile extends File
{
	private final String filename;
	
	public NamedFile(String pathname, String filename)
	{
		super(pathname);
		this.filename = Strings.nullToEmpty(filename);
	}
	
	@Override
	public String getName()
	{
		return filename;
	}
}
