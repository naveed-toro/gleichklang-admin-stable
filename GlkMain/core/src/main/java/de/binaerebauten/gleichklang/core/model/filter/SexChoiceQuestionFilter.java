package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;

import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.SEX_CHOICE_QUESTION_FILTER;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class SexChoiceQuestionFilter extends UserFilter
{
    @XmlIDREF
    @ManyToOne
    @JoinColumn(name = "choice_id")
    @NotNull
    private Choice choice;

    public Choice getChoice()
    {
        return choice;
    }

    public void setChoice(Choice choice)
    {
        this.choice = choice;
    }

    @Override
    public <T> T accept(FilterVisitor<T> filterVisitor)
    {
        return filterVisitor.visit(this);
    }

    @Override
    public String getName()
    {
        return SEX_CHOICE_QUESTION_FILTER.toString() + ": " + getChoice().getName();
    }
}

