package de.binaerebauten.gleichklang.core.model.questionnaire;

//import com.mysql.jdbc.StringUtils;
import com.mysql.cj.util.StringUtils;
import de.binaerebauten.gleichklang.core.security.SanitizeContent;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class TextAnswer extends Answer
{
	@SanitizeContent
	@Column(name = "text_value")
	private String textValue;

	public String getTextValue()
	{
		return textValue;
	}

	public void setTextValue(String textValue)
	{
		this.textValue = removeSpaces(textValue); //remove starting space from member
	}

	@Override
	public String getValue()
	{
		return textValue=removeSpaces(textValue); //remove starting space from member
	}

	@Override
	public boolean isAnswered()
	{
		return !StringUtils.isNullOrEmpty(textValue);
	}
	//using remove the starting spaces from member profileprivate
	 String removeSpaces(String str){
		 if(str != null && !str.equals("")){
			 if(str.startsWith("<div><br /></div>") || str.startsWith("<div><br></div>")){
				 str = str.replaceAll("<div><br /></div>","").replaceAll("<div><br></div>","");
			 }
			 else{

				 str = str.replaceAll("<div><br /></div><div><br /></div>","<div><br /></div>");
			 }
		 }
		 return str;
	}

}
