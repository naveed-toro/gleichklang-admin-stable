package de.binaerebauten.gleichklang.memberweb.controller;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.user.EmailLink;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AudioRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.file.AudioService;
import de.binaerebauten.gleichklang.core.service.mail.EmailLinkService;
import de.binaerebauten.gleichklang.memberweb.controller.exception.WebExceptionHandling;
import de.binaerebauten.gleichklang.memberweb.view.I18N;
import de.binaerebauten.gleichklang.memberweb.view.popup.AudioGalleryPopup;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 12/17/2018.
 */

@RestController
public class AudioController extends WebExceptionHandling {

    private final AudioRepository repository;
    private User currentUser;
    private final UserRepository userRepository;
    public CheckBox friendship = new CheckBox(I18N.CATEGORY_FRIENDSHIP.msg());
    public CheckBox partnership = new CheckBox(I18N.CATEGORY_PARTNERSHIP.msg());

    @Value("${file_audio_path}")
    private String audioFilePath;

    @Value("${server.base_url}")
    private String serverPath;

    @Autowired
    public AudioController(UserRepository userRepository, AudioRepository repository) {
        this.userRepository = userRepository;
        this.repository = repository;
    }


    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST , consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadFile(HttpServletRequest request,@RequestParam("userId") Long userId ,@RequestParam("categoriesSelected") String[] categoriesSelected) {
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                currentUser = userRepository.findById(userId);
                List<FileItem> multiparts = new ServletFileUpload(
                        new DiskFileItemFactory()).parseRequest(request);
                for (FileItem item : multiparts) {
                    if (!item.isFormField()) {
                        String name = item.getName();
                        File tmpFile = fetchFile(name);
                        item.write(tmpFile);
                        UserAudio userAudio = new UserAudio();
                        userAudio.setName(name);
                        userAudio.setPath(tmpFile.toString());
                        userAudio.setAuthor(currentUser);
                        userAudio.setPartnership(ArrayUtils.contains(categoriesSelected, "Partnership") || ArrayUtils.contains(categoriesSelected, "Partnerschaft"));
                        userAudio.setFriendship(ArrayUtils.contains(categoriesSelected, "Friendship") || ArrayUtils.contains(categoriesSelected, "Freundschaft"));
                        repository.save(userAudio);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Error in uploading file", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Uploaded Audio file Successfuly", HttpStatus.OK);
    }

    private File fetchFile(String fileName) {
        String folderName = audioFilePath+"/"+ currentUser.getId();
        File dir = new File(folderName);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(folderName+"/"+fileName);
        return file;
    }
}