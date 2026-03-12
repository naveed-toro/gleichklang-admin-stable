package de.binaerebauten.gleichklang.core.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.binaerebauten.gleichklang.core.model.reminder.AdminReminder;
import de.binaerebauten.gleichklang.core.model.reminder.AdminReminder_;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.repository.reminder.AdminReminderRepository;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

@Service
public class ReminderService {
	private static final Logger LOG = LoggerFactory.getLogger(ReminderService.class);
    @Lazy
    @Autowired
    private AdminReminderRepository adminReminderRepository;

    @Lazy
    @Autowired
    private UserRepository userRepository;

    public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminReminder> createAdminRemindersHandler()
    {
        return  adminReminderRepository::findAll;
    }

    public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminReminder> createAllAdminRemindersHandler(Admin admin)
    {
        LOG.info("Got reminders admin id" + admin.getId());
        final Specifications<AdminReminder> specs = Specifications.where(new Specification<AdminReminder>() {
            @Override
            public Predicate toPredicate(Root<AdminReminder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                /*return
                        cb.and(cb.notEqual(root.get(AdminReminder_.reminderStatus), AdminReminder.ReminderStatus.DONE), (cb.or(cb.equal(root.get(AdminReminder_.reminderRecurrence), AdminReminder.ReminderRecurrence.EVERYONE), (cb.equal(root.get(AdminReminder_.admin), admin.getId())))));
            */
                return
                        cb.and((cb.or(cb.equal(root.get(AdminReminder_.reminderRecurrence), AdminReminder.ReminderRecurrence.EVERYONE))));

            }
        });
        return (specification, pageable) -> adminReminderRepository.findAll(specs, pageable);

//        return  adminReminderRepository::findAll;
    }


    public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminReminder> createAdminRemindersHandler(Admin admin)
    {
        LOG.info("Got reminders admin id" + admin.getId());

        final Specifications<AdminReminder> specs = Specifications.where(new Specification<AdminReminder>() {
            @Override
            public Predicate toPredicate(Root<AdminReminder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                /*return
                        cb.and(cb.notEqual(root.get(AdminReminder_.reminderStatus), AdminReminder.ReminderStatus.DONE), (cb.or(cb.equal(root.get(AdminReminder_.reminderRecurrence), AdminReminder.ReminderRecurrence.EVERYONE), (cb.equal(root.get(AdminReminder_.admin), admin.getId())))));
            */
                return
                        cb.and(cb.equal(root.get(AdminReminder_.admin), admin) , cb.notEqual(root.get(AdminReminder_.reminderStatus), AdminReminder.ReminderStatus.DONE));
                        //cb.and(cb.equal(root.get(AdminReminder_.admin), admin) , cb.notEqual(root.get(AdminReminder_.reminderStatus), AdminReminder.ReminderStatus.DONE), (cb.or(cb.equal(root.get(AdminReminder_.reminderRecurrence), AdminReminder.ReminderRecurrence.EVERYONE))));


            }
        });
        return (specification, pageable) -> adminReminderRepository.findAll(specs, pageable);

//        return  adminReminderRepository::findAll;
    }

    public List<AdminReminder> getAllRemindersForAdmin(Admin admin)
    {
        return adminReminderRepository.findAll();

    }



    public boolean save(AdminReminder reminder, String nameOrAlias)
    {
        if(nameOrAlias!=null) {
            if (userRepository.findByEmailOrAlias(nameOrAlias) != null) {
                reminder.setUser(userRepository.findByEmailOrAlias(nameOrAlias));
            } else {
                MessageBox.show("Invalid Email Or Alias");
                return false;
            }
        }
            adminReminderRepository.saveAndFlush(reminder);
        return  true;
    }

    public void delete(Long reminderId)
    {
        adminReminderRepository.delete(reminderId);
    }


    @Scheduled(cron = "0 30 * * * *")
    public void deleteOldReminders()
    {
    	try {
            LOG.info("deleting old reminders with  due data older than "+LocalDateTime.now().minusMonths(6));
	        adminReminderRepository.deleteOldReminders(LocalDateTime.now().minusMonths(6));
    	}catch (Exception e) {
			LOG.error("deleteOldReminders:", e);
		}
    }
}
