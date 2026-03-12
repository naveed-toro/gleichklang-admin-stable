package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Browser;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.OS;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientInformationRepository extends JpaRepository<ClientInformation, Long>,JpaSpecificationExecutor<ClientInformation>
{
	List<ClientInformation> findByUser(User user);
	
	@Query("FROM ClientInformation WHERE "
			+ "user = :user AND "
			+ "browser = :browser AND "
			+ "browserMajorVersion = :majorVersion AND "
			+ "browserMinorVersion = :minorVersion AND "
			+ "os = :os")
	ClientInformation findByKey
			(
					@Param("user") User user,
					@Param("browser") Browser browser,
					@Param("majorVersion") int browserMajorVersion,
					@Param("minorVersion") int browserMinorVersion,
					@Param("os") OS os
			);



	@Query("FROM ClientInformation ci WHERE ci.user = ?1 and ci.lastSeen=true ORDER BY ci.changeDate DESC")
	List<ClientInformation> findByIdAndLaseSeen(User user, Pageable pageable);
}
