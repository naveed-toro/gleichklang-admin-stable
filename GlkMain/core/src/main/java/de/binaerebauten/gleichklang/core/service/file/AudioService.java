package de.binaerebauten.gleichklang.core.service.file;

import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio_;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AudioRepository;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

//@PropertySource(("classpath:custom-functional.properties"))
@Service
public class AudioService  {

    @Lazy
    @Autowired
    AudioRepository repository;

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    @Value("${file_audio_path}")
    private String audioFilePath;

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    @Value("${server.base_url}")
    private String serverPath;

    public String getConverter() {
        return converter;
    }

    public void setConverter(String converter) {
        this.converter = converter;
    }

    @Value("${converter_path}")
    public String converter;

    public void save(UserAudio audio)
    {
        repository.save(audio);
    }
    public void deleteAudio(UserAudio audio){
        try {
            repository.delete(audio);
            File file = new File(audio.getPath());
            if (file != null) {
                file.delete();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<UserAudio> createAudioHandler(User currentUser)
    {

        final Specifications<UserAudio> specs = Specifications.where((root, query, cb) ->
                cb.and(cb.equal(root.get(UserAudio_.author),currentUser)                )
        );

        return (specification, pageable) -> repository.findAll(specs.and(specification), pageable);
    }

    public int getCountByUser(User user)
    {

        final Specifications<UserAudio> specs = Specifications.where((root, query, cb) ->
                cb.and(cb.equal(root.get(UserAudio_.author),user)                )
        );

        List<UserAudio> list =  repository.findAll(specs);

        if(list !=null && !list.isEmpty())
        {
            return list.size();
        }

    else
        return 0;


}

public boolean isSingleAudioForBothCategories(long  authorId){
        UserAudio audio = repository.findByAuthorIdAndForFriendshipAndForPartnership(authorId);
        if(audio!=null){
            return true;
        }
        return  false;
}

    public boolean isAudioPresentForFriendship(User user)
    {
      UserAudio audio =   repository.findByAuthorIdAndForFriendship(user.getId());

      if(audio != null)
      {
          return true;
      }
          return false;
    }

    public boolean isAudioPresentForPartnership(User user)
    {
        UserAudio audio =   repository.findByAuthorIdAndForPartnerShip(user.getId());
        if(audio != null)
        {
            return true;
        }
        return false;
    }

    public boolean isSelectionAllowedForFriendship(User user)
    {
        if(user.getCategories().contains(RecommendationCategory.FRIENDSHIP) && !isAudioPresentForFriendship(user)){
            return true;
        }
        return false;
    }

    public boolean isSelectionAllowedForPartnership(User user)
    {
        if (user.getCategories().contains(RecommendationCategory.PARTNERSHIP) && !isAudioPresentForPartnership(user)) {
            return true;
        }
        return false;
    }

    public List<UserAudio> getFileList(User user)
    {
        return repository.findByAuthorId(user.getId());

    }

    public List<UserAudio> getUserAudios(long authorId)
    {
        return repository.findByAuthor(authorId);

    }

    public Set<RecommendationCategory> getMissingAudio(User user)
    {
        final Set<RecommendationCategory> categories = EnumSet.noneOf(RecommendationCategory.class);

        if (isSelectionAllowedForFriendship(user)){
            categories.add(RecommendationCategory.FRIENDSHIP);
        }
        if (isSelectionAllowedForPartnership(user)){
            categories.add(RecommendationCategory.PARTNERSHIP);
        }
        return categories;
    }

    public void updateAudioForFriendship(UserAudio userAudio){
        repository.changeAudioCategoryFriendship(userAudio);
    }

    public void updateAudioForPartnership(UserAudio userAudio){
        repository.changeAudioCategoryPartnership(userAudio);
    }

    public void updateAudio(UserAudio userAudio){
        repository.updateAudio(userAudio, userAudio.getPartnership(),userAudio.getFriendship());
    }
}
