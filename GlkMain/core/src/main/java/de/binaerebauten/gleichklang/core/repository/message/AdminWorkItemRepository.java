package de.binaerebauten.gleichklang.core.repository.message;

import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface AdminWorkItemRepository extends JpaRepository<AdminWorkItem, Long>, JpaSpecificationExecutor<AdminWorkItem>
{

    AdminWorkItem findByMessage(Message message);
}
