package de.binaerebauten.gleichklang.core.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import de.binaerebauten.gleichklang.core.model.user.User;

import java.util.List;

@org.springframework.stereotype.Repository
public interface CompleteUserRepository extends Repository<User, Long>, JpaSpecificationExecutor<User>
{
	@EntityGraph(value = "User.complete", type = EntityGraphType.LOAD)
	User findById(Long id);

	@Override
	@EntityGraph(value = "User.complete", type = EntityGraphType.LOAD)
	User findOne(Specification<User> spec);

	@Override
	@EntityGraph(value = "User.complete", type = EntityGraphType.LOAD)
	List<User> findAll(Specification<User> spec);

	@Override
	@EntityGraph(value = "User.complete", type = EntityGraphType.LOAD)
	Page<User> findAll(Specification<User> spec, Pageable pageable);

	@Override
	@EntityGraph(value = "User.complete", type = EntityGraphType.LOAD)
	List<User> findAll(Specification<User> spec, Sort sort);
}
