package de.binaerebauten.gleichklang.memberweb.presenter;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
//import de.binaerebauten.gleichklang.core.repository.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.service.RelationshipService;
import de.binaerebauten.gleichklang.core.service.UserDataService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.memberweb.view.StatisticView;
import de.binaerebauten.gleichklang.memberweb.view.StatisticView.StatisticViewListener;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class StatisticPresenter extends NavigatePresenter implements StatisticViewListener
{
	private final RelationshipRepository relationshipRepository;
	private final MessageRepository messageRepository;

	private final User user;
	private final StatisticView view;
	private final RelationshipService relationshipService;

	public StatisticPresenter(ApplicationContext ctx, StatisticView view)
	{
		super(view);

		this.view = view;

		relationshipRepository = ctx.getBean(RelationshipRepository.class);
		messageRepository = ctx.getBean(MessageRepository.class);
		relationshipService = ctx.getBean(RelationshipService.class);

		final UserService userService = ctx.getBean(UserService.class);
		user = userService.getCurrentUser();

		view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		this.view.setNoOfMatches(UserDataService.createEnumMap(user, relationshipRepository::countBySourceUserAndCategory));
		this.view.setNoOfReceivedMessages(UserDataService.createEnumMap(user, messageRepository::countIncomingsByUser));
		this.view.setNoOfSentMessages(UserDataService.createEnumMap(user, messageRepository::countOutgoingsByUser));
		this.view.setStatistics(relationshipService.getStatistics(user,false));
	}


}