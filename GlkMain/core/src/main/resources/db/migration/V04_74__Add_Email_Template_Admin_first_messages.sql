update email_template_mapping etm
    set etm.template_text =
'Nachricht von Gleichklang
<body>
<div style="font-family:Tahoma,Verdana,Geneva,Lucida,Helvetica,Arial,sans-serif;font-size:16px;border:solid 1px #520010;padding:20px;line-height:140%">
Guten Tag ${senderName},

der Gleichklang-Support hat Ihnen eine wichtige Nachricht geschrieben zu Ihrem Account bei Gleichklang.

Die Nachricht finden Sie in Ihrem internen Gleichklang-Bereich.

#if(${memberStatus}==\'REGISTRATION\')

Sie finden Ihre Nachricht, wenn Sie nach dem Login oben rechts auf den kleinen Briefumschlag klicken.

#elseif(${memberStatus}==\'REGISTERED\')

Sie finden Ihre Nachricht, wenn Sie nach dem Login hierhin gehen: "Meine Teilnahme" >> "Nachrichten an Gleichklang".

#end

Bitte loggen Sie sich bei Gleichklang ein, um die Nachricht zu lesen:
<a href="https://gleichklang.de/teilnehmer/?login">https://gleichklang.de/teilnehmer/?login</a>

Es grüßt herzlich${signature}'

where etm.template_name='admin_first_message.html.vm' and etm.template_language = 'DE';

commit;

update email_template_mapping etm
    set etm.template_text =
'Message from Gleichklang
<body>
<div style="font-family:Tahoma,Verdana,Geneva,Lucida,Helvetica,Arial,sans-serif;font-size:16px;border:solid 1px #520010;padding:20px;line-height:140%">
Dear ${senderName},

a member of the support team of has written to you an important message about your account.

The message you find in your internal login-area of Gleichklang.

#if(${memberStatus}==\'REGISTRATION\')

 You find the message when you click on the small envelop on the right above after login.

#elseif(${memberStatus}==\'REGISTERED\')

 You find the message when you go after login to: "my account" >> "messages to Gleichklang"

#end

Please log in at Gleichklang and read the message:
<a href="https://gleichklang.de/teilnehmer/?login">https://gleichklang.de/teilnehmer/?login</a>

Best regards${signature}'

    where etm.template_name='admin_first_message.html.vm' and etm.template_language = 'EN';

commit;
