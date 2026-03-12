package de.binaerebauten.gleichklang.core.service.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class InMemoryUpload extends AbstractUpload
{
	private ByteArrayOutputStream byteArrayOutputStream = null;
	
	@Override
	public OutputStream startUpload(String filename, String mimeType)
	{
		byteArrayOutputStream = new ByteArrayOutputStream();
		return byteArrayOutputStream;
	}
	
	@Override
	public UploadResult onUploadSucceeded()
	{
		return byteArrayOutputStream != null && byteArrayOutputStream.size() > 0 ? UploadResult.SUCCESS : UploadResult.FAILED;
	}
	
	public ByteArrayOutputStream getByteArrayOutputStream()
	{
		return byteArrayOutputStream;
	}
	
	public InputStreamReader createStreamReader()
	{
		return new InputStreamReader(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
	}
}
