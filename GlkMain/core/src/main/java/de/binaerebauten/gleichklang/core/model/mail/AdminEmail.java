package de.binaerebauten.gleichklang.core.model.mail;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.mail.Message;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminEmail  extends BaseEntity {

    public enum ProcessingState
    {
        UNPROCESSED,
        INPROCESS,
        ATTACHMENTPENDING,
        PROCESSED
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }



    public boolean messageTypeMime;

    public boolean isMessageTypeMime() {
        return messageTypeMime;
    }

    public void setMessageTypeMime(boolean messageTypeMime) {
        this.messageTypeMime = messageTypeMime;
    }

    public void setAge(int age) {
        this.age = age;
    }

    String name;
    int age;

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(int attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    int attachmentCount=0;

    public AdminEmail(String name, int age)
    {
        this.name = name;
        this.age = age;
    }

    public AdminEmail()
    {

    }

    String sender;

    String receiver;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    String status;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    String subject;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    String text;

    Date sentDate;

    Object mailData;

    public boolean isMailDataHTML() {
        return mailDataHTML;
    }

    public void setMailDataHTML(boolean mailDataHTML) {
        this.mailDataHTML = mailDataHTML;
    }

    private boolean mailDataHTML;

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public Object getMailData() {
        return mailData;
    }

    public String getProcessedMailData() {
        return processedMailData;
    }

    public void setProcessedMailData(String processedMailData) {
        this.processedMailData = processedMailData;
    }

    private String processedMailData;

    public void setMailData(Object mailData) {
        this.mailData = mailData;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }

    private List<File> attachments = new ArrayList<>();


    public ProcessingState getProcessingState() {
        return processingState;
    }

    public void setProcessingState(ProcessingState processingState) {
        this.processingState = processingState;
    }

    private ProcessingState processingState = ProcessingState.UNPROCESSED;




}