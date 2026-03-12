package de.binaerebauten.gleichklang.adminweb.presenter;

public enum CsvSeparator
{
	COMMA(','),
	SPACE(' '),
	SEMICOLON(';');
	
	private final char separator;
	
	CsvSeparator(char separator)
	{
		this.separator = separator;
	}
	
	public char getSeparator()
	{
		return separator;
	}
}
