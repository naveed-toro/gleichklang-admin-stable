package de.binaerebauten.gleichklang.core.service;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailTemplateMapping;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailTemplateRepository;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.service.template.VelocityTemplate;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@Service
public class DynamicContentTemplateService   {


    @Autowired
    EmailTemplateRepository repository;

    public String getContentForTemplateTest(String templateName, I18NEntity.Language language, Map<String, Object> model )
    {
        String templateText = (String) model.get("template.test.text");
        Long footerId = new Long((Long)model.get("template.test.footer"));
        String templateData = getTemplateData(templateName, templateText,model);
        List<EmailTemplateMapping> footerLst = repository.findById(footerId);

        if(footerLst != null && !footerLst.isEmpty())
        {
            String footerText = footerLst.get(0).getTemplateText();

            templateData = templateData.concat( footerText );

        }
        return templateData;
    }


    public String getContentForTemplate(String templateName, I18NEntity.Language language, Map<String, Object> model)
    {

        String mailContentReturned = getDbDrivenContentForTemplate(templateName,language, model);
            return  mailContentReturned;
        }
        // TODO : remove System.out.printlns when the app is stable
    private String getDbDrivenContentForTemplate(String templateName, I18NEntity.Language language,  Map<String, Object> model)
    {

        //service.findAll(null);


        List<EmailTemplateMapping> templateList = null, footerLst = null;

        //templateList = repository.findByTemplateNameAndTemplateLanguageAndActiveTrue(templateName, language.toLocale().getLanguage());
        String strLanguage;
        if(language == null || language.toLocale() == null || language.toLocale().getLanguage() == null)
            strLanguage = "DE";
        else
            strLanguage = language.toLocale().getLanguage();

        templateList = repository.findByTemplateNameAndTemplateLanguageAndActiveTrue(templateName, strLanguage);

        if(templateList != null && !templateList.isEmpty()  && templateList.get(0) != null)
        {
            EmailTemplateMapping mapping = templateList.get(0);
            String templateData = getTemplateData(templateName, mapping.getTemplateText(),model);

            if(templateData != null && mapping.getTemplateFooter() != null && mapping.getTemplateFooter() != 0)
            {
                Long footerId = mapping.getTemplateFooter();
                footerLst = repository.findById(footerId);


                if(footerLst != null && !footerLst.isEmpty() && model.get("signatureCheck")==null)
                {
                    String footerText = footerLst.get(0).getTemplateText();

                    templateData = templateData.concat( footerText );

                }
            }
            return templateData;
        }

        return null;
    }

    private String getTemplateData(String templateName, String templateText,  Map<String, Object> model)
    {

        RuntimeServices velocityRuntime;
        StringReader dbTemplateReader;
        SimpleNode sn;
        Template t;
        VelocityContext vc;
        StringWriter mergedContent;
        try {


            velocityRuntime = RuntimeSingleton.getRuntimeServices();
            dbTemplateReader = new StringReader(templateText);

            sn = velocityRuntime.parse(dbTemplateReader, templateName);

            t = new Template();
            t.setRuntimeServices(velocityRuntime);
            t.setData(sn);
            t.initDocument();

            vc = new VelocityContext(model);

            mergedContent = new StringWriter();
            t.merge(vc, mergedContent);


            String mergedTemplateText = mergedContent.toString();
            return mergedTemplateText;

        }catch (Exception ex)
        {
            System.out.print(ex.getMessage());

        }
        finally {
            velocityRuntime = null;
            dbTemplateReader = null;
            sn = null;
            t = null;
            vc = null;
            mergedContent = null;
        }
        return null;

    }


}
