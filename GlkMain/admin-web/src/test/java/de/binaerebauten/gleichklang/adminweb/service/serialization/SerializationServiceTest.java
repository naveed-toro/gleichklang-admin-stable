package de.binaerebauten.gleichklang.adminweb.service.serialization;

import de.binaerebauten.gleichklang.adminweb.config.AdminTestConfig;
import de.binaerebauten.gleichklang.adminweb.service.serialization.SerializationService.SerializationGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.questionnaire.TextQuestion;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.QuestionGroupRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionnaireRepository;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Domi on 26.09.2016.
 */
@ContextConfiguration(classes = { AdminTestConfig.class })
public class SerializationServiceTest extends BasePersistenceTest {

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private QuestionGroupRepository questionGroupRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SerializationService serializationService;

    @After
    public void teardown()
    {
        questionRepository.deleteAll();
        questionGroupRepository.deleteAll();
        questionnaireRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testQuestionnairePersistance() throws Exception {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setSortOrder(1);
        questionnaire.setI18nKey("testQuestinnaire");
//        questionnaire.setId(null);

        QuestionGroup questionGroup = new QuestionGroup();
        questionGroup.setI18nKey("testQuestionGroup");
        questionGroup.setSortOrder(1);

        TextQuestion question = new TextQuestion();
        question.setRepresentationType(TextQuestion.RepresentationType.DEFAULT);
        question.setMaxLength(200);
        question.setNumberOfLines(1);
        question.setI18nKey("testQuestion");
        question.setSortOrder(1);

        questionGroup.addQuestion(question);
        questionnaire.addQuestionGroup(questionGroup);

        assertNull(questionnaire.getId());
        assertNull(questionGroup.getId());
        assertNull(question.getId());

        questionGroupRepository.save(questionGroup);
        questionRepository.save(question);
        questionnaireRepository.save(questionnaire);

        assertNotNull(questionnaire.getId());
        assertNotNull(questionGroup.getId());
        assertNotNull(question.getId());
    }

    @Test
    public void exportData() throws IOException
    {
                final PipedInputStream pipeInput = new PipedInputStream();
                final BufferedOutputStream out = new BufferedOutputStream(new PipedOutputStream(pipeInput));

                        for (SerializationGroup type : SerializationGroup.values())
                    {
                                serializationService.exportData(type, out);
               }
            }
}
