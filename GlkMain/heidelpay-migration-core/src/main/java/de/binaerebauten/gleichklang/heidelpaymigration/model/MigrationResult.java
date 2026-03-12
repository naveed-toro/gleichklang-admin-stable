package de.binaerebauten.gleichklang.heidelpaymigration.model;

/**
 * Contains migration result with an optional additional error information.
 */
public class MigrationResult
{
	private String id;
	
	private boolean passed;
	
	private String msg;
	
	public MigrationResult(String id, boolean passed)
	{
		this(id, passed, null);
	}
	
	public MigrationResult(String id, boolean passed, String msg)
	{
		this.id = id;
		this.passed = passed;
		this.msg = msg;
	}
	
	public String getId()
	{
		return id;
	}
	
	public boolean isPassed()
	{
		return passed;
	}
	
	public String getMsg()
	{
		return msg;
	}
	
	public String toString()
	{
		if (passed)
		{
			return String.format("SUCCESS: %s", id);
		}
		else
		{
			return String.format("FAIL: %s (%s)", id, msg);
		}
	}
}
