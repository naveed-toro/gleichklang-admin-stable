package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MatrixValueRepository extends JpaRepository<MatrixValue, Long> {

    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(mv.id, CONCAT(mv.matrix.name, ':', mv.sourceChoice.i18nKey, ':', mv.targetChoice.i18nKey)) FROM MatrixValue mv")
    List<IdKeyPairResult> getAllAsIdKeyPair();
}
