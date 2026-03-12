package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.utils.DatabaseResourceBundleControl;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@XmlTransient
@MappedSuperclass
public abstract class LocalizedEntity extends BaseEntity implements DefaultI18N, NaturalKeyEntity<Long>
{
	private static final Logger LOG = LoggerFactory.getLogger(LocalizedEntity.class);

	private static ResourceBundle.Control CONTROL = new DatabaseResourceBundleControl();


	@XmlAttribute
	@Column(name = "i18n_key")
	private String i18nKey;

	protected abstract BaseName doGetBaseName();

    /**
     * Unique XML Key.
     *
     * Returns a unique key for xml serialization as combination of class name and i18nKey.
     *
     * @return unique XML key
     */
    @XmlID
	@XmlAttribute(name = "uniqueKey")
	public String getUniqueXmlKey() {
		return getClass().getSimpleName() + "_" + getI18nKey();
	}

	@Override
	public final String getBaseName()
	{
		return doGetBaseName().name();
	}

	@Override
	public String getI18nKey()
	{
		return i18nKey;
	}

	@Override
	public void setI18nKey(String i18nKey)
	{
		this.i18nKey = i18nKey;
	}

	/**
	 * Returns the localized message with the given arguments.
	 *
	 * @param args the message arguments
	 * @return the localized message
	 */
	public String msg(BaseName baseName, Object... args)
	{
		final String key = getI18nKey();
		String msg = "!" + key;
		try
		{
			//LOG.info("baseName============" +baseName + "key=======" +key + "msg======="+msg);
			final ResourceBundle bundle = getBundle(baseName.name());
			//LOG.info("baseName.name============" +baseName.name() +" bundle==="+ bundle + "key=======" +key + "msg======="+msg);

			if(!hasKey(key, baseName)){
				//LOG.warn("Translation for {} key {} wasn't found", baseName, key);
			}
			else{
				msg = bundle.getString(key);
				msg = MessageFormat.format(msg, args);
				//LOG.info("msg1============" +bundle.getString(key));
				//LOG.info("msg2============" +MessageFormat.format(msg,args));

			}

		}
		catch (MissingResourceException ex)
		{
//			LOG.error("Missing resource exception for key {} (Bundle doesn't exists): {}", key, ex.getLocalizedMessage());
		}
		catch(Exception ex){
			LOG.error("Unexpected exception for translation ({})", key, ex);
		}
		return msg;
	}

	public boolean hasKey(String key, BaseName baseName)
	{
		final ResourceBundle bundle = getBundle(baseName.name());
		if(bundle!=null) {
			return bundle.containsKey(key);
		}
		return false;
	}

	@Override
	public String msg(Object... args)
	{
		return msg(doGetBaseName(), args);
	}

	@Override
	public ResourceBundle.Control getControl()
	{
		return CONTROL;
	}

	public static void setControl(ResourceBundle.Control control)
	{
		LocalizedEntity.CONTROL = control;
	}
}
