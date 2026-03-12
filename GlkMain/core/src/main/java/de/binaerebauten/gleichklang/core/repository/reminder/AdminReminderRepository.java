package de.binaerebauten.gleichklang.core.repository.reminder;

import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.reminder.AdminReminder;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface AdminReminderRepository extends JpaRepository<AdminReminder, Long>, JpaSpecificationExecutor<AdminReminder>
{

    AdminReminder findByAdmin(Admin admin);

    @Query("SELECT s FROM AdminReminder s WHERE s.reminderStatus = 'READ' AND s.dueDate >= :date")
    void deleteOldReminders(@Param("date") LocalDateTime date);



}
