package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AudioRepository extends JpaRepository<UserAudio, Long>, JpaSpecificationExecutor<UserAudio>, DeleteRepository<UserAudio, Long>{

    UserAudio findById(int id);

    @Query("FROM UserAudio where author_id = ?1")
    List<UserAudio> findByAuthor(long author);

    List<UserAudio> findByAuthorId(long authorId);

    List<String> findNameByAuthor(User author);

    @Query("FROM UserAudio where author_id = ?1 and for_partnership = true ")
    UserAudio findByAuthorIdAndForPartnerShip(long authorId);

    @Query("FROM UserAudio where author_id = ?1 and for_friendship = true ")
    UserAudio findByAuthorIdAndForFriendship(long authorId);

    @Query("FROM UserAudio where author_id = ?1 and for_friendship = true and for_partnership = true ")
    UserAudio findByAuthorIdAndForFriendshipAndForPartnership(long authorId);

    @Transactional
    @Modifying
    @Query("UPDATE UserAudio au set au.friendship = true, au.partnership = false where au = ?1")
    void changeAudioCategoryFriendship(UserAudio userAudio);

    @Transactional
    @Modifying
    @Query("Update UserAudio au set au.friendship = false, au.partnership = true where au = ?1")
    void changeAudioCategoryPartnership(UserAudio userAudio);

    @Transactional
    @Modifying
    @Query("Update UserAudio au set au.friendship = ?3, au.partnership = ?2 where au = ?1")
    void updateAudio(UserAudio userAudio,Boolean partnership, Boolean friendship);

}