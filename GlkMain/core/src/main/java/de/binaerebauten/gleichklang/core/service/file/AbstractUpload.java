package de.binaerebauten.gleichklang.core.service.file;

import java.io.OutputStream;

public abstract class AbstractUpload
{
	public enum UploadResult
	{
		SUCCESS,
		FAILED,
		VIRUS
	}
	
	/**
	 * Called from {@link de.binaerebauten.gleichklang.core.view.component.UploadComponent}
	 * for the {@link com.vaadin.ui.Upload.Receiver}-Interface.
	 *
	 * @param filename Name of the file
	 * @param mimeType mimeType of the File comes from the browser and could be
	 *                 wrong
	 * @return the output stream for the upload, null if no filename was set
	 */
	public abstract OutputStream startUpload(String filename, String mimeType);
	
	/**
	 * Called from {@link de.binaerebauten.gleichklang.core.view.component.UploadComponent}
	 * for the {@link com.vaadin.ui.Upload.SucceededListener}-Interface.
	 *
	 * @return an UploadResult that represent "success" or "virus"
	 */
	public UploadResult onUploadSucceeded()
	{
		return UploadResult.SUCCESS;
	}
	
	/**
	 * Called from {@link de.binaerebauten.gleichklang.core.view.component.UploadComponent}
	 * for the {@link com.vaadin.ui.Upload.FailedListener}-Interface.
	 *
	 * @return an UploadResult that represent "failed"
	 */
	public UploadResult onUploadFailed()
	{
		return UploadResult.FAILED;
	}
}
