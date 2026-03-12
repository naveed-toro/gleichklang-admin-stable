insert into email_template_mapping(template_name,template_text, template_description,template_language) VALUES ('admin_first_message.html.vm',
'Nachricht von Gleichklang
<body>
<div style="font-family:Tahoma,Verdana,Geneva,Lucida,Helvetica,Arial,sans-serif;font-size:16px;border:solid 1px #520010;padding:20px;line-height:140%">
Guten Tag ${senderName},

der Gleichklang-Support hat Ihnen eine wichtige Nachricht geschrieben zu Ihrem Account bei Gleichklang.

Die Nachricht finden Sie in Ihrem internen Gleichklang-Bereich.

Bitte loggen Sie sich bei Gleichklang ein, um die Nachricht zu lesen:
<a href="https://gleichklang.de/teilnehmer/?login">https://gleichklang.de/teilnehmer/?login</a>

Es grüßt herzlich${signature}', 'First Message from admin','DE');

 update email_template_mapping etm left join
       email_template_mapping etmf
       on etmf.template_description = 'footer' and etmf.template_language='DE'
    set etm.template_footer = etmf.id where etm.template_language = 'DE';

commit;


insert into email_template_mapping(template_name,template_text, template_description,template_language) VALUES ('admin_first_message.html.vm',
'Message from Gleichklang
<body>
<div style="font-family:Tahoma,Verdana,Geneva,Lucida,Helvetica,Arial,sans-serif;font-size:16px;border:solid 1px #520010;padding:20px;line-height:140%">
Dear ${senderName},

a member f the support team of has written to you an important message about your account.

The message you find in your internal login-area of Gleichklang.

Please log in at Gleichklang and read the message:
<a href="https://gleichklang.de/teilnehmer/?login">https://gleichklang.de/teilnehmer/?login</a>

Best regards${signature}', 'First Message from admin','EN');

 update email_template_mapping etm left join
       email_template_mapping etmf
       on etmf.template_description = 'footer' and etmf.template_language='EN'
    set etm.template_footer = etmf.id where etm.template_language = 'EN';

commit;