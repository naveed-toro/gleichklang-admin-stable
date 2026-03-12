package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.StreamVariable;
import com.vaadin.ui.*;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import de.binaerebauten.gleichklang.core.service.file.AbstractUpload;
import de.binaerebauten.gleichklang.core.service.file.AbstractUpload.UploadResult;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.io.OutputStream;

public class UploadComponent<T extends AbstractUpload> extends CustomComponent
{
	public interface UploadFileChangedListener<T extends AbstractUpload>
	{
		void uploadFileChanged(T uploadFile, UploadResult uploadResult);
	}
	
	private final Upload upload;
	private T uploadFile = null;
	private UploadFileChangedListener<T> uploadFileChangedListener;
	
	public UploadComponent(String caption, String buttonCaption)
	{
		this(caption, buttonCaption, null);
	}
	
	public UploadComponent(String caption, String buttonCaption, T uploadFile)
	{
		upload = new Upload();
		upload.setCaption(caption);
		upload.setStyleName(CssStyle.FILE_UPLOAD.getStyleName());
		upload.addSucceededListener(event -> uploadSucceeded());
		upload.addFailedListener(event -> uploadFailed());
		
		upload.setButtonCaption(buttonCaption);
		upload.setImmediate(true);
		
		setUploadFile(uploadFile);
		
		setCompositionRoot(upload);
	}
	
	public Component createDragAndDrop(Component root)
	{
		final DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(root);
		dragAndDropWrapper.setDropHandler(new DropHandler()
		{
			@Override
			public void drop(DragAndDropEvent event)
			{
				onDragAndDrop(event);
			}
			
			@Override
			public AcceptCriterion getAcceptCriterion()
			{
				return AcceptAll.get();
			}
		});
		
		return dragAndDropWrapper;
	}
	
	private void onDragAndDrop(DragAndDropEvent event)
	{
		final WrapperTransferable tr = (WrapperTransferable) event.getTransferable();
		final Html5File[] files = tr.getFiles();
		if (files != null && files.length == 1)
		{
			for (final Html5File html5File : files)
			{
				final OutputStream stream = uploadFile.startUpload(html5File.getFileName(), html5File.getType());
				
				final StreamVariable streamVariable = new StreamVariable()
				{
					public OutputStream getOutputStream()
					{
						return stream;
					}
					
					public boolean listenProgress()
					{
						return false;
					}
					
					public void onProgress(StreamingProgressEvent event)
					{
					
					}
					
					public void streamingStarted(StreamingStartEvent event)
					{
					}
					
					public void streamingFinished(StreamingEndEvent event)
					{
						uploadSucceeded();
					}
					
					public void streamingFailed(StreamingErrorEvent event)
					{
						uploadFailed();
					}
					
					public boolean isInterrupted()
					{
						return false;
					}
				};
				
				html5File.setStreamVariable(streamVariable);
			}
		}
	}
	
	public T getUploadFile()
	{
		return uploadFile;
	}
	
	public void setUploadFile(T uploadFile)
	{
		this.setEnabled(uploadFile != null);
		this.uploadFile = uploadFile;
		upload.setReceiver(uploadFile != null ? uploadFile::startUpload : null);
	}
	
	public void setUploadFileChangedListener(UploadFileChangedListener<T> uploadFileChangedListener)
	{
		this.uploadFileChangedListener = uploadFileChangedListener;
	}
	
	private void uploadSucceeded()
	{
		if (uploadFile == null) return;
		
		final UploadResult result = uploadFile.onUploadSucceeded();
		
		if (uploadFileChangedListener != null)
			uploadFileChangedListener.uploadFileChanged(uploadFile, result);
	}
	
	private void uploadFailed()
	{
		if (uploadFile == null) return;
		
		final UploadResult result = uploadFile.onUploadFailed();
		
		if (uploadFileChangedListener != null)
			uploadFileChangedListener.uploadFileChanged(uploadFile, result);
	}
}
