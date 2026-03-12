package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long>
{
}
