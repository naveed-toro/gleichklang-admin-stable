package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;

public class CustomNotification
{
	public enum Type
	{
		WARNING_TRAY_NOTIFICATION("warning_tray");
		
		private final String style;
		
		Type(String style)
		{
			this.style = style;
		}
		
		public String getStyle()
		{
			return style;
		}
	}
	
	private CustomNotification()
	{
	}
	
	/**
	 * Shows a notification message on the middle of the current page. The
	 * message automatically disappears ("humanized message").
	 * <p>
	 * The caption is rendered as plain text with HTML automatically escaped.
	 *
	 * @param caption The message
	 * @see Notification#Notification(String)
	 * @see Notification#show(Page)
	 */
	public static void show(String caption)
	{
		Notification.show(caption);
	}
	
	/**
	 * Shows a notification message the current page. The position and behavior
	 * of the message depends on the type, which is one of the basic types
	 * defined in {@link Notification}, for instance
	 * Notification.TYPE_WARNING_MESSAGE.
	 * <p>
	 * The caption is rendered as plain text with HTML automatically escaped.
	 *
	 * @param caption The message
	 * @param type    The message type
	 * @see Notification#Notification(String, com.vaadin.ui.Notification.Type)
	 * @see Notification#show(Page)
	 */
	public static void show(String caption, Notification.Type type)
	{
		new Notification(caption, type).show(Page.getCurrent());
	}
	
	/**
	 * Shows a notification message the current page. The position and behavior
	 * of the message depends on the type, which is one of the basic types
	 * defined in {@link Notification}, for instance
	 * Notification.TYPE_WARNING_MESSAGE.
	 * <p>
	 * The caption is rendered as plain text with HTML automatically escaped.
	 *
	 * @param caption     The message
	 * @param description The message description
	 * @param type        The message type
	 * @see Notification#Notification(String, Notification.Type)
	 * @see Notification#show(Page)
	 */
	public static void show(String caption, String description, Notification.Type type)
	{
		new Notification(caption, description, type).show(Page.getCurrent());
	}
	
	/**
	 * Shows a notification message the current page. The position and behavior
	 * of the message depends on the type, which is one of the basic types
	 * defined in {@link CustomNotification}.
	 * <p>
	 * The caption is rendered as plain text with HTML automatically escaped.
	 *
	 * @param caption The message
	 * @param type    The message type
	 */
	public static void show(String caption, Type type)
	{
		final Notification notification = new Notification(caption);
		setType(type, notification);
		notification.show(Page.getCurrent());
	}
	
	/**
	 * Shows a notification message the current page. The position and behavior
	 * of the message depends on the type, which is one of the basic types
	 * defined in {@link CustomNotification}.
	 * <p>
	 * The caption is rendered as plain text with HTML automatically escaped.
	 *
	 * @param caption     The message
	 * @param description The message description
	 * @param type        The message type
	 */
	public static void show(String caption, String description, Type type)
	{
		final Notification notification = new Notification(caption, description);
		setType(type, notification);
		notification.show(Page.getCurrent());
	}
	
	private static void setType(Type type, Notification notification)
	{
		notification.setStyleName(type.getStyle());
		switch (type) {
			case WARNING_TRAY_NOTIFICATION:
				notification.setDelayMsec(3000);
				notification.setPosition(Position.BOTTOM_RIGHT);
				break;
			default:
				break;
		}
	}
}
