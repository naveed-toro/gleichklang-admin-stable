package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.JsIncludePostfix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface JsIncludePostfixRepository extends JpaRepository<JsIncludePostfix, Long> {

    @Query(value = "select * from js_include_postfix order by id desc limit 1",nativeQuery = true)
    JsIncludePostfix findLastRecord();

    @Transactional
    @Modifying
    @Query(value = "update js_include_postfix set key_name = ?1", nativeQuery = true)
    public void updatePostfix(String key);

}
