package de.binaerebauten.gleichklang.core.service.mail;


import com.google.common.collect.Lists;
import de.binaerebauten.gleichklang.core.model.mail.AdminEmail;
import de.binaerebauten.gleichklang.core.service.file.FileService;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import de.binaerebauten.gleichklang.core.model.mail.AdminEmail.ProcessingState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@PropertySource(("classpath:custom-functional.properties"))
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MailReceiveService {

    private  LinkedList<AdminEmail> adminEmails = new LinkedList<>();

    private static final Logger LOG = LoggerFactory.getLogger(MailReceiveService.class);

    @Value("${external_mail_host}")
    private  String mailHost;

    @Value("${external_mail_user}")
    private  String emailUser;

    @Value("${external_mail_password}")
    private  String emailPassword;


    @Value("${external_mail_protocol}")
    private  String emailProtocol;

    public  String getMailHost() {
        return mailHost;
    }

    public  void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public  String getEmailUser() {
        return emailUser;
    }

    public  void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public  String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getEmailProtocol() {
        return emailProtocol;
    }

    public void setEmailProtocol(String emailProtocol) {
        this.emailProtocol = emailProtocol;
    }

    public String getMailFolder() {
        return mailFolder;
    }

    public void setMailFolder(String mailFolder) {
        this.mailFolder = mailFolder;
    }

    public int getMailCacheCount() {
        return mailCacheCount;
    }

    public void setMailCacheCount(int mailCacheCount) {
        this.mailCacheCount = mailCacheCount;
    }

    @Value("${external_mail_folder}")
    private  String mailFolder;



    @Value("${external_mail_cache_count}")
    private  int mailCacheCount;

    @Autowired
    private FileService fileService;

    @Autowired
    private MailSendService mailSendservice;


    public enum SupportedSearchTypes
    {
        SENDER,
        SUBJECT,
        DATE
    }


    private final AtomicInteger fetchedStartIndex = new AtomicInteger(1);
    public void createUpdateAdminEmailCache()
    {
        Message[] messages = null;
        try {
            Properties props = System.getProperties();
           // props.setProperty("mail.imap.ssl.enable", "true");
            Session session = Session.getInstance(props);
            Store store = session.getStore(emailProtocol);
            store.connect(mailHost, emailUser, emailPassword);
            Folder inbox = store.getFolder(mailFolder);
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.CONTENT_INFO);
            profile.add(FetchProfile.Item.FLAGS);
            profile.add(FetchProfile.Item.ENVELOPE);

            UIDFolder uf = (UIDFolder)inbox;
            inbox.open(Folder.READ_ONLY);
            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            int size = inbox.getMessageCount();

            // Do Intiatialization Tasks
            if(fetchedStartIndex.get() == 1)
            {
                if(size >mailCacheCount)
                    fetchedStartIndex.set(size-mailCacheCount);
            }

            if((size - fetchedStartIndex.get() +1) <= 0)
            {
                //all messgaes have been fetched so return; size - fetchedStartIndex.get() +1
                return;
            }
            messages = inbox.getMessages(fetchedStartIndex.get(),size);
            fetchedStartIndex.set(size+1);
            // Sort messages from recent to oldest
            if(messages != null && messages.length !=0)
            {
                Arrays.sort( messages, (m1, m2 ) -> {
                    try {
                        return m1.getSentDate().compareTo( m2.getSentDate() );
                    } catch ( MessagingException e ) {
                        LOG.error("Messaging Exception: {}", e.getMessage());
                        return 0;
                    }
                } );

                AdminEmail email = null;
                Address[] temp ;
                try {
                    for (Message message : messages) {
                        email = new AdminEmail();
                        email.setSubject(message.getSubject());
                        email.setText(message.getDescription());
                        email.setMailData(message.getContent());
                        email.setSentDate(message.getReceivedDate());
                        if(message.getFlags() != null && message.getFlags().contains(Flags.Flag.SEEN))
                            email.setStatus("SEEN");
                        if(message.getFlags() != null && message.getFlags().contains(Flags.Flag.ANSWERED))
                            email.setStatus("ANSWERED "+email.getStatus());

                        if(message.getFrom()  != null && message.getFrom().length != 0)
                        {   temp = message.getFrom();
                            email.setSender(temp[0].toString());
                        }
                        email.setId(uf.getUID(message));
                        if (message instanceof MimeMessage) {
                            email.setMessageTypeMime(true);
                        }
                        synchronized (adminEmails) {
                            adminEmails.addFirst(email);
                       }

                       /* Process email data*/
                        try {
                            email.setProcessingState(ProcessingState.UNPROCESSED);
                            downloadEmailAsynchronously(email);


                        }catch (Exception ex)
                        {
                            LOG.error("Exception in Receive mail: ",ex);
                        }

                    }

                }catch (Exception ex)
                {
                    LOG.error("Exception in Receive mail: ",ex);
                }}

        } catch (Exception ex) {
            LOG.error("Exception in Receive mail: ",ex);
        }
    }



    public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminEmail> createMailDisplayHandler()
    {


        return (specification, pageable) -> new PageImpl<AdminEmail>(Lists.newArrayList(adminEmails));
        /*return (specification, pageable) ->
        {
            PageImpl page = new PageImpl(adminEmails, pageable,adminEmails.size());
            return page;
        };*/

    }


    public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminEmail> createFilteredMailHandler(SupportedSearchTypes searchType, String paramValue, Date date)
    {

        List<AdminEmail> sourceList = Lists.newArrayList(adminEmails);
        final  List<AdminEmail> targetList;

        switch (searchType)
        {
        case DATE:
            targetList= sourceList.stream().filter((email) ->
               (DateUtils.isSameDay(email.getSentDate(),date)==true)).collect(Collectors.toList());
            break;

        case SENDER:
            targetList =  sourceList.stream().filter((email) ->
                    (email.getSender().contains(paramValue) == true)).collect(toList());

        break;

        case SUBJECT:
            targetList=   sourceList.stream().filter((email) ->
                    (email.getSubject().contains(paramValue) == true)).collect(toList());
            break;
        default:
            targetList = new ArrayList<>();
        break;
    }

        return (specification, pageable) -> new PageImpl<>(Lists.newArrayList(targetList));
    }


    private void processMimeMultiPart(
            MimeMultipart mimeMultipart, StringBuilder resultSb, AdminEmail email)  throws Exception {
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
                processPart(bodyPart,resultSb,email);
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                processMimeMultiPart((MimeMultipart) bodyPart.getContent(), resultSb, email);
            }
        }
    }


    private void processPart( BodyPart part,  StringBuilder resultSb, AdminEmail email) throws Exception
    {
        BodyPart textPartData = null;
        BodyPart htmlPartData = null;
        if (part.isMimeType("text/plain")) {
                    textPartData = part;
                } else if (part.isMimeType("text/html")) {
                    email.setMailDataHTML(true);
                    htmlPartData = part;
                }
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) &&
                        !StringUtils.isBlank(part.getFileName())) {
                    try {

                        CompletableFuture.supplyAsync(() -> {

                            try {
                                email.setProcessingState(ProcessingState.ATTACHMENTPENDING);
                                InputStream is = part.getInputStream();
                                Path path = fileService.createTempFilePath(part.getFileName());
                                // File f = new File(path.toString()+"/"+part.getFileName());
                                File f = path.toFile();
                                FileOutputStream fos = new FileOutputStream(f);

                                byte[] buf = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = is.read(buf)) != -1) {
                                    fos.write(buf, 0, bytesRead);
                                }

                                return f;
                            }catch (Exception ex){
                                ex.printStackTrace();

                                return  null;}
                        }).thenApply(file -> { //check for virus once the file has been fully downloaded
                            try {
                                if (file != null && fileService.isVirusFree(file.toPath())) {
                                    email.getAttachments().add(file);
                                    if(email.getAttachments().size() == email.getAttachmentCount()) {
                                        //all attachments have been processed set it to processed
                                        email.setProcessingState(ProcessingState.PROCESSED);
                                    }
                                } else {

                                    System.out.print("Virus");
                                    //TODO : throw new Virus found in attachment exception; not closed flow with client
                                }
                                return file;
                            }catch (Exception ex)
                            {
                                ex.printStackTrace();
                                return  null;}
                        });
                    } catch (Exception ex) {
                        //TODO: handle attachment exception
                    }
                }
                if (textPartData != null) {
                    resultSb.append("\n");
                    resultSb.append(textPartData.getContent().toString());
                } else if (htmlPartData != null) {
                    resultSb.append("<BR>");
                    resultSb.append(htmlPartData.getContent().toString());
                }
        }

    public  String processEmailData(AdminEmail email)
    {
        if(email.getProcessingState().equals(ProcessingState.PROCESSED) || email.getProcessingState().equals(ProcessingState.ATTACHMENTPENDING))
        {
            return email.getProcessedMailData();
        }
        StringBuilder resultSb = new StringBuilder("");

        int attachmentCount=0;



            try
            {
            if (email.isMessageTypeMime()) {
                Object contentObject = email.getMailData();

                if (contentObject instanceof Multipart) {
                   Multipart content = (Multipart) contentObject;
                    int count = content.getCount();
                    // count number of attachments

                    for (int i = 0; i < count; ++i) {
                        BodyPart part = content.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
                            attachmentCount++;
                        email.setAttachmentCount(attachmentCount);
                    }

                    // process individual parts
                    for (int i = 0; i < count; i++) {
                        BodyPart part = content.getBodyPart(i);
                        if (part.isMimeType("multipart/*")) {
                            MimeMultipart mimeMultipart = (MimeMultipart) contentObject;
                            processMimeMultiPart(mimeMultipart, resultSb, email);
                        } else {
                            processPart(part, resultSb, email);
                        }
                    }
               } else if (contentObject instanceof String) // a simple text message
                {
                    resultSb.append((String) contentObject);
                }
            }

            }catch (Exception ex){
                ex.printStackTrace();
            }
            if (!email.getProcessingState().equals(ProcessingState.ATTACHMENTPENDING)) {
                email.setProcessingState(ProcessingState.PROCESSED);
            }
            email.setProcessedMailData(resultSb.toString());
            return resultSb.toString();

    }

    public boolean markDeleted(AdminEmail email)
    {

        try {
            Properties props = System.getProperties();
            Session session = Session.getInstance(props);
            Store store = session.getStore(emailProtocol);
            store.connect(mailHost, emailUser, emailPassword);
            Folder inbox = store.getFolder(mailFolder);
            UIDFolder uf = (UIDFolder) inbox;
            inbox.open(Folder.READ_WRITE);
            Message message = uf.getMessageByUID(email.getId());
            message.setFlag(Flags.Flag.DELETED, true);
            synchronized (adminEmails) {
                adminEmails.remove(email);
            }

        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return true;
    }

   public boolean markRead(AdminEmail email)
    {
        try {
            Properties props = System.getProperties();
           // props.setProperty("mail.imap.ssl.enable", "true");
            Session session = Session.getInstance(props);
            Store store = session.getStore(emailProtocol);
            store.connect(mailHost, emailUser, emailPassword);
            Folder inbox = store.getFolder(mailFolder);
            UIDFolder uf = (UIDFolder) inbox;
            inbox.open(Folder.READ_WRITE);
            Message message = uf.getMessageByUID(email.getId());
            message.setFlag(Flags.Flag.SEEN, true);
            inbox.close(true);

        }
        catch (Exception ex){}
        return true;
    }
    public boolean sendReply(AdminEmail reply)
    {
        try {
            mailSendservice.sendAdminReply(reply, reply.getReceiver());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return true;
    }



    private void downloadEmailAsynchronously(AdminEmail adminEmail ) {

        try {
            adminEmail.setProcessingState(ProcessingState.INPROCESS);
            processEmailData(adminEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File createAttachmentFile(byte fileBytes[], String fileName)
    {


        File file = null;
        try {
            Path path = fileService.createTempFilePath(fileName);
            file = path.toFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileBytes);
            fos.flush();
            fos.close();
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }  //check for virus once the file has been fully downloaded


        try {
                if (file != null && fileService.isVirusFree(file.toPath())) {
                    return file;
                } else {
                    System.out.print("Virus");
                    return null;
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
                return  null;
            }
    }

}