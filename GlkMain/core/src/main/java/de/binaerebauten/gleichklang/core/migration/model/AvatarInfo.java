package de.binaerebauten.gleichklang.core.migration.model;

/**
 * Created by rgoerner on 16.05.17.
 */
public class AvatarInfo
{
    private Long id;
    private Long fileId;
    private Long userId;
    private String name;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id) {this.id = id;}

    public void setFileId(Long id)
    {
        this.id = id;
    }

    public Long getFileId()
    {
        return id;
    }

    public void setUserId(Long userId) {this.userId = userId;}

    public Long getUserId() {return  this.userId;}

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
