package de.binaerebauten.gleichklang.core.repository.matching;

import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchStatisticRepository extends JpaRepository<MatchStatistic, Long>, JpaSpecificationExecutor<MatchStatistic>
{
}
