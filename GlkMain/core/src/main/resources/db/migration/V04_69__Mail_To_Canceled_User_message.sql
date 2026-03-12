insert into email_template_mapping(template_name,template_text, template_description,template_language) VALUES ('canceled_user_mail.html.vm',
'Re:${subject}
<body>
<div style="font-family:Tahoma,Verdana,Geneva,Lucida,Helvetica,Arial,sans-serif;font-size:16px;border:solid 1px #520010;padding:20px;line-height:140%">
Sie haben sich an den Gleichklang-Support mit einer Anfrage gewandt. Diese Antfrage möchten wir Ihnen hiermit beantworten.

<b>Unsere Antwort</b>
${messagetext} <b>Ihre ursprüngliche Anfrage lautete</b>
${oldMessage}

${signature}', 'Message from admin to canceled user','DE');

 update email_template_mapping etm left join
       email_template_mapping etmf
       on etmf.template_description = 'footer' and etmf.template_language='DE'
    set etm.template_footer = etmf.id where etm.template_language = 'DE';

commit;


insert into email_template_mapping(template_name,template_text, template_description,template_language) VALUES ('canceled_user_mail.html.vm',
'Re:${subject}
<body>
<div style="font-family:Tahoma,Verdana,Geneva,Lucida,Helvetica,Arial,sans-serif;font-size:16px;border:solid 1px #520010;padding:20px;line-height:140%">
You have written to the support team of Gleichklang. We would like to send you the following answer

<b>Unsere Antwort</b>
${messagetext} <b>Ihre ursprüngliche Anfrage lautete</b>
${oldMessage}

${signature}', 'Message from admin to canceled user','EN');

 update email_template_mapping etm left join
       email_template_mapping etmf
       on etmf.template_description = 'footer' and etmf.template_language='EN'
    set etm.template_footer = etmf.id where etm.template_language = 'EN';

commit;
