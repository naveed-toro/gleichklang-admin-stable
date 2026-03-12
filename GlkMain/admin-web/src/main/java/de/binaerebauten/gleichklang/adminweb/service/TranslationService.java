package de.binaerebauten.gleichklang.adminweb.service;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.I18NEntity_;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TranslationService
{
    private final I18NRepository i18NRepository;
    
    @Autowired
    public TranslationService(I18NRepository i18NRepository)
    {
        Objects.requireNonNull(i18NRepository);
        
        this.i18NRepository = i18NRepository;
    }
    
    @Transactional
    public void saveAndFlush(Collection<I18NEntity> i18NEntities)
    {
        final List<I18NEntity> savableEntities = i18NEntities.stream()
                .filter(i18NEntity -> !Strings.isNullOrEmpty(i18NEntity.getValue()))
                .collect(Collectors.toList());
        final List<I18NEntity> removableEntities = i18NEntities.stream()
                .filter(i18NEntity -> i18NEntity.getId() != null && Strings.isNullOrEmpty(i18NEntity.getValue()))
                .collect(Collectors.toList());
        
        i18NRepository.delete(removableEntities);
        i18NRepository.save(savableEntities);
        i18NRepository.flush();
    }
    
    public Page<I18NEntity> getI18NEntities(Specification<I18NEntity> spec, Pageable pageable)
    {
        return i18NRepository.findAll(spec, pageable);
    }
    
    public List<I18NEntity> getI18NEntitiesForAllLanguages(BaseName baseName, String i18nKey)
    {
        final List<I18NEntity> i18NEntities = i18NRepository.findByBaseNameAndKey(baseName, i18nKey);
    
        for(Language language : Language.values())
        {
            if(i18NEntities.stream().noneMatch(entity -> language.equals(entity.getLanguage())))
            {
                final I18NEntity newEntity = new I18NEntity();
                newEntity.setBaseName(baseName);
                newEntity.setKey(i18nKey);
                newEntity.setLanguage(language);
                i18NEntities.add(newEntity);
            }
        }
        
        return i18NEntities;
    }
}
