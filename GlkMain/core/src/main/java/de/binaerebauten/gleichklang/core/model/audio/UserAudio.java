package de.binaerebauten.gleichklang.core.model.audio;


import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="audio")
public class UserAudio extends BaseEntity implements DeletableEntity<Long> {


    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "path")
    private String path;

    @Column(name = "for_partnership")
    private Boolean partnership;

    @Column(name = "for_friendship")
    private Boolean friendship;



    public UserAudio(){}

        /*public UserAudio(Long id, String name,  byte[] audio_file){
            this.id = id;
            this.name = name;
            this.audio_file = audio_file;
        }*/

       /* public UserAudio(Long id, String name,  byte[] audio_file,long authorId){
            this.id = id;
            this.name = name;
            this.audio_file = audio_file;
            this.authorId = authorId;
        } */

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

        /*public byte[] getAudio_file(){
            return this.audio_file;
        }

        public void setAudio_file(byte[] audio_file){
            this.audio_file = audio_file;
        } */

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getPartnership() {
        return partnership;
    }

    public void setPartnership(Boolean partnership) {
        this.partnership = partnership;
    }

    public Boolean getFriendship() {
        return friendship;
    }

    public void setFriendship(Boolean friendship) {
        this.friendship = friendship;
    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public void setDeleted(boolean deleted) {

    }
}
