package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.user.CancelReason;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;
import jdk.nashorn.internal.codegen.types.BooleanType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;


@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class LastSeenBrowserFilter extends EnumFilterMobile<ClientInformation.Browser> {
    @XmlAttribute
    @Column(name = "enum_value")
    @Enumerated(EnumType.STRING)
    @NotNull
    private ClientInformation.Browser enumValue;

    @XmlAttribute
    private transient boolean checkBox;

    @Override
    public ClientInformation.Browser getEnumValue() {
        return enumValue;
    }

    @Override
    public boolean getCheckBoxvalue() {
        return checkBox;
    }

    @Override
    public void setEnumValue(ClientInformation.Browser enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public void setCheckBoxVlue(boolean checkBox) {
       this.checkBox = checkBox;
    }


    @Override
    public <T> T accept(FilterVisitor<T> filterVisitor) {
        return filterVisitor.visit(this);
    }
}
