package de.binaerebauten.gleichklang.memberweb.view;



import com.vaadin.ui.*;

import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.user.User;

import de.binaerebauten.gleichklang.core.service.file.AudioService;

import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.memberweb.presenter.I18N;
import de.binaerebauten.gleichklang.memberweb.view.popup.AudioGalleryPopup;


import javax.sound.sampled.*;
import java.io.*;


public class AudioUploader implements Upload.Receiver, Upload.SucceededListener {


    private String fileName;
    AudioGalleryPopup popup;

    private static Double maxFileSize = 1e+7;
    private ByteArrayOutputStream fileData = null;
    ByteArrayOutputStream out = null;


    public AudioUploader(AudioGalleryPopup popup) {
        this.popup = popup;
    }

    public OutputStream receiveUpload(String filename, String mimeType) {
        this.fileName = filename;
        popup.messageLayout.removeAllComponents();
        fileData = new ByteArrayOutputStream();
        return fileData;
    }

    public void uploadSucceeded(Upload.SucceededEvent event) {

        Button saveButton = new Button(de.binaerebauten.gleichklang.memberweb.view.I18N.SAVE.msg());
        saveButton.setDisableOnClick(true);
        popup.messageLayout.removeAllComponents();
        popup.buttonSection.removeAllComponents();
        if (fileData == null || fileData.size() == 0 || !isValidFileSize(fileData.size()))
        {
            // showError;
            popup.messageLayout.addComponent(new Label(I18N.SIZEEXCEPTION.msg()));
            saveButton.setVisible(false);
        }
        else if (!isValidFileType(fileName))
        {
            popup.messageLayout.addComponent(new Label(I18N.FILETYPEEXCEPTION.msg()));
            saveButton.setVisible(false);
        }

        else if (!checkFileExists()) {
            MessageBox.show(I18N.SAME_AUDIO_ALREADY_UPLOADED.msg());
            popup.close();
            return;
        }

        else {
            if (fileName.endsWith("wav") || fileName.endsWith("WAV")) {
                String folderName = popup.service.getAudioFilePath() + "/tmp/" + popup.user.getId();
                File dir = new File(folderName);
                if (!dir.exists()) dir.mkdirs();
                File tempFile = new File(folderName + "/" + fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(fileData.toByteArray());
                    fos.flush();
                    fos.close();

                    if (!isValidUploadFileType(tempFile.getPath())) {
                        popup.messageLayout.addComponent(new Label(I18N.FILETYPEEXCEPTION.msg()));
                        saveButton.setVisible(false);
                    } else {
                        popup.messageLayout.addComponent(new Label(I18N.CLICK_SAVE_TO_SAVE.msg() + fileName + I18N.TO_PROFILE.msg()));
                        saveButton.setVisible(true);
                        saveButton.setCaption(de.binaerebauten.gleichklang.memberweb.view.I18N.SAVE.msg());
                        saveButton.addClickListener((clickEvent) -> {
                            saveFile();
                            saveButton.setEnabled(true);
                        });
                        //popup.messageLayout.addComponent(new Label(fileName));
                        MessageBox.show(I18N.Before_Close.msg());
                        popup.buttonSection.addComponent(saveButton);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    tempFile.deleteOnExit();
                }
            }
            else {
                popup.messageLayout.addComponent(new Label(I18N.CLICK_SAVE_TO_SAVE.msg() + fileName + I18N.TO_PROFILE.msg()));
                saveButton.setVisible(true);
                saveButton.setCaption(de.binaerebauten.gleichklang.memberweb.view.I18N.SAVE.msg());
                saveButton.addClickListener((clickEvent) -> {
                    saveFile();
                    saveButton.setEnabled(true);
                });
                //popup.messageLayout.addComponent(new Label(fileName));
                MessageBox.show(I18N.Before_Close.msg());
                popup.buttonSection.addComponent(saveButton);
            }
        }
    }

    private boolean isValidUploadFileType(String filename)
    {
        File inFile;
        try {
            inFile = new File(filename);
            if (inFile==null){
                return false;
            }
            else {
                try {
                    if (AudioSystem.getAudioFileFormat(inFile).getFormat().getEncoding().toString()=="ULAW" || AudioSystem.getAudioFileFormat(inFile).getFormat().getEncoding().toString()=="PCM_SIGNED" ) {
                        return true;
                    }
                    else {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            System.out.println("Error: one of the converting");
            return false;
        }

        return false;
    }

    private boolean checkCategory()
    {
        if(!popup.friendship.getValue() && !popup.partnership.getValue()){
            return false;
        }
        return true;
    }

    private  boolean checkCategoryType()
    {

        if(popup.partnership.getValue() && popup.audioForPartnershipAdded)
            return false;

        if(popup.friendship.getValue() && popup.audioForFriendshipAdded)
            return false;

        return true;

    }

    private boolean checkFileExists()
    {

        if (fileName != null && popup.fileList != null && !popup.fileList.isEmpty())
        {

            for (UserAudio s:popup.fileList) {
                if (s.getName() != null && !s.getName().isEmpty() && s.getName().equalsIgnoreCase(fileName))
                    return false;
            }
        }
        return true;
    }



    public boolean saveFile()
    {

        if(!checkCategory())
        {
            popup.messageLayout.removeAllComponents();
            popup.messageLayout.addComponent(new Label(de.binaerebauten.gleichklang.memberweb.view.I18N.SELECT_CATEGORY.msg()));

            return false;
        }

        if(!checkCategoryType())
        {
            popup.messageLayout.removeAllComponents();
            popup.messageLayout.addComponent(new Label(I18N.REPEATCATEGORY.msg()));
            return false;
        }

        popup.upload.setEnabled(false);
        byte fileBytes[] = fileData.toByteArray();
        File f = fetchFile();
        UserAudio userAudio = new UserAudio();
        userAudio.setName(fileName);
        userAudio.setPath(f.toString());
        userAudio.setAuthor(popup.user);
        userAudio.setFriendship(popup.friendship.getValue());
        userAudio.setPartnership(popup.partnership.getValue());
        popup.service.save(userAudio);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(fileBytes);
            fos.flush();
            fos.close();
            popup.close();
            popup.callback.performAfterUpload(userAudio,AudioGalleryPopup.AudioGalleryPopupCallback.Operation.UPLOAD);

        }catch (Exception ex)
        {
            popup.service.deleteAudio(userAudio);
        }
        return true;
    }

    public class NoValidFileUploadedException extends Exception
    {

        public String toString()
        {
            return "Select a valid audio file";
        }
    }

    public class NoValidFileSizeException extends Exception
    {
        public String toString()
        {
            return "Select a valid audio file upto 10 MB";
        }

    }

    private boolean isValidFileType(String filename)
    {

        if(filename == null)
            return false;

        if(filename.endsWith("wav") || filename.endsWith("mp3") || filename.endsWith("MP3")|| filename.endsWith("WAV") || filename.endsWith("m4a") || filename.endsWith("M4A"))
            return true;

        return false;
    }

    private boolean isValidFileSize(long size)
    {

        if(size <= maxFileSize)
            return true;
        else
            return false;
    }

    private File fetchFile() {


        String folderName = popup.service.getAudioFilePath()+"/"+ popup.user.getId();
        System.out.print(popup.user);
        File dir = new File(folderName);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(folderName+"/"+fileName);

        return file;
    }

}