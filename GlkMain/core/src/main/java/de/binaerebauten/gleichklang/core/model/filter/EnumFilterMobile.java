package de.binaerebauten.gleichklang.core.model.filter;

//import javafx.scene.control.CheckBox;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@MappedSuperclass
public abstract class EnumFilterMobile<E extends Enum<E>> extends UserFilter
{
    public abstract E getEnumValue();
    public abstract boolean getCheckBoxvalue();

    public abstract void setEnumValue(E enumValue);
    public abstract void setCheckBoxVlue(boolean bool);

    @Override
    public String getName()
    {
        final UserFilterType userFilterType = UserFilterType.valueOf(getClass());
        final String userFilterTypeStr = userFilterType == null ? getClass().getSimpleName() : userFilterType.toString();

        return userFilterTypeStr + ": " + getEnumValue().toString() + ", Mobile" + ":" + getCheckBoxvalue();
    }
}
