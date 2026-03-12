package de.binaerebauten.gleichklang.memberweb.service.print;

import com.lowagie.text.*;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.draw.LineSeparator;
import de.binaerebauten.gleichklang.core.model.FreeTextElement;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.service.AnswerService;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import de.binaerebauten.gleichklang.core.utils.ExtendedThemeResource;
import de.binaerebauten.gleichklang.core.utils.LocaleAware;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil.UserInfo;
import de.binaerebauten.gleichklang.core.utils.pdf.HTMLElement;
import de.binaerebauten.gleichklang.core.utils.pdf.Header;
import de.binaerebauten.gleichklang.core.utils.pdf.PdfDocumentFactory;
import de.binaerebauten.gleichklang.core.utils.pdf.StyledDocument;
import de.binaerebauten.gleichklang.core.utils.pdf.modify.SVGtoPNGPathConverter;
import de.binaerebauten.gleichklang.core.view.component.UserProfile;
import de.binaerebauten.gleichklang.memberweb.view.popup.I18N;
import de.binaerebauten.gleichklang.memberweb.view.popup.RelationshipPopup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

/**
 * Created by Domi on 21.09.2016.
 */
@Service
public class PrintService implements LocaleAware {

    private static final Logger LOG = LoggerFactory.getLogger(PrintService.class);

    private static final float USER_IMAGE_WIDTH = 150;
    private static final float USER_IMAGE_HEIHGT= 150;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AppUrlBuilder appUrlBuilder;


    /**
     * Generates Relationship user details PDF for print preview.
     *
     * @param relation User relationship
     * @param data relationship data
     * @return print preview as pdf document
     */
    public StyledDocument getRelationshipPrintout(Relationship relation, RelationshipPopup.RelationshipData data) {

        StyledDocument document = PdfDocumentFactory.userPrintProfile();

        for(int i=0;i<data.getUserProfileDataMap().size();i++){

            if(data.getUserProfileDataMap()!=null && data.getUserProfileDataMap().containsKey(RecommendationCategory.PARTNERSHIP)){


                List<FreeTextElement> freeText=data.getUserProfileDataMap().get(RecommendationCategory.PARTNERSHIP).getFreeText();
                for(int j = 0; j< freeText.size(); j++){
                    if(freeText.get(j).getContent()!=null && freeText.get(j).getContent().contains(".svg")){
                        String con = replaceBetween(freeText.get(i).getContent(),"img src",".svg",true,true,"");
                        freeText.get(j).setContent(con);
                    }
                }
            }

            else if(data.getUserProfileDataMap()!=null && data.getUserProfileDataMap().containsKey(RecommendationCategory.FRIENDSHIP)){
                List<FreeTextElement> freeText=data.getUserProfileDataMap().get(RecommendationCategory.FRIENDSHIP).getFreeText();
                for(int k = 0; k< freeText.size(); k++){
                    if(freeText.get(k).getContent()!=null && freeText.get(k).getContent().contains(".svg")){
                        String con = replaceBetween(freeText.get(k).getContent(),"img src",".svg",true,true,"");
                        freeText.get(k).setContent(con);
                    }
                }
            }
        }

        document.open();
        addUserHeader(document, data.getTargetUser());

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale());
        final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        addQuestionAnswer(document, I18N.RELATIONSHIPPOPUP_CAPTION_CREATE_DATE.msg(), relation.getCreateDate().format(dateTimeFormatter));

        if (relation.getTargetUserSex() != null)
            addQuestionAnswer(document, I18N.RELATIONSHIPPOPUP_SEX_QUESTION.msg(), relation.getTargetUserSex());

        addQuestionAnswer(document, I18N.RELATIONSHIPPOPUP_AGE_QUESTION.msg(), ""+data.getTargetUser().getAge());

        if (data.getTargetUser().getAddresses() != null && !data.getTargetUser().getAddresses().isEmpty())
            addQuestionAnswer(document, I18N.RELATIONSHIPPOPUP_ADDRESS_QUESTION.msg(), getAddress(data.getTargetUser())) ;

        if (data.getTargetUser().getStatusMessage() != null)
            addQuestionAnswer(document, I18N.RELATIONSHIPPOPUP_CAPTION_STATUS.msg(), data.getTargetUser().getStatusMessage());

        addQuestionAnswer(document, I18N.RELATIONSHIPPOPUP_CAPTION_RATING.msg(), data.getRating().getName());

        if (!data.getTargetUser().isCanceled())
            addUserDetails(document, data.getUserProfileDataMap().values().iterator().next(), data.getTargetUser().isCanceled());

        final ExtendedThemeResource partImage = new ExtendedThemeResource("img/icon_zurPerson_pU.png");

        // user infos
        if (!data.getTargetUser().isCanceled())
        {
            for(UserInfo userInfo : UserInfo.values())
            {
                final List<Answer> answers = data.getUserAnswers().get(userInfo);
                if(answers != null && !answers.isEmpty())
                {
                    addQuestionPart(document, userInfo.toString(), partImage, answers);
                }
            }
        }


        // message history
        addMessageHistory(document, relation.getSourceUser(), relation.getTargetUser());

        document.close();

        return document;
    }

    public static String replaceBetween(String input,
                                        String start, String end,
                                        boolean startInclusive,
                                        boolean endInclusive,
                                        String replaceWith) {
        start = Pattern.quote(start);
        end = Pattern.quote(end);
        return input.replaceAll("(" + start + ")" + ".*" + "(" + end + ")",
                (startInclusive ? "" : "$1") + replaceWith + (endInclusive ? "" : "$2"));
    }

    private String getAddress(User user)
    {
        StringBuilder result = new StringBuilder();

        final Set<Address> addresses = new LinkedHashSet<>(user.getAddresses());
        for (Address a : addresses)
        {
            nullSafe(() -> a.getZip().getZip())
                    .filter(zip -> zip.length() > 2)
                    .ifPresent(zip -> result
                            .append("PLZ ")
                            .append(zip.substring(0, 2))
                            .append(("..., ")));
            nullSafe(() -> a.getRegion().getName())
                    .ifPresent(region -> result
                            .append(region)
                            .append(", "));
            nullSafe(() -> a.getCountry().getName())
                    .ifPresent(country -> result
                            .append(country)
                            .append("; "));
        }

        return result.length() > 2 ? result.substring(0, result.length() - 2) : "";
    }


    /* === Document Parts === */

    /**
     * Adds user header
     * @param document pdf document
     * @param user user object
     */
    private void addUserHeader(StyledDocument document, User user) {
        try {
            final ExtendedThemeResource themeResource = new ExtendedThemeResource("img/icon_zurPerson.png");

            final Image categoryImage = Image.getInstance(themeResource.getAbsolutePath());
            Header aliasHeader = new Header(user.getAlias(), Header.HeaderType.H1);
            Paragraph headerParagraph = new Paragraph();
            headerParagraph.add(categoryImage);
            document.add(headerParagraph);
            document.add(aliasHeader);

        } catch (BadElementException e) {
            LOG.error("Wrong element.", e);
        } catch (IOException e) {
            LOG.error("File not found.", e);
        }

        final LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineWidth(0.5f);
        document.add(lineSeparator);
    }

    /**
     * Adds user details
     *
     * @param document pdf document
     * @param userProfileData user data.
     */
    private void addUserDetails(StyledDocument document, UserProfile.UserProfileData userProfileData, boolean isCanceledUser) {
        try {
            if (!isCanceledUser)
            {
                if (userProfileData.getAvatarFile() != null) {
                    final Image userImage = Image.getInstance(userProfileData.getAvatarFile().getAbsolutePath());
                    userImage.scaleToFit(USER_IMAGE_WIDTH, USER_IMAGE_HEIHGT);
                    document.add(userImage);
                }
            }

        } catch (BadElementException e) {
            LOG.error("Wrong element.", e);
        } catch (IOException e) {
            LOG.error("File not found", e);
        }

        userProfileData.getFreeText().forEach(freeText -> {
            addFreetext(document, freeText.getCompleteText());
        });

    }

    /**
     * Adds adds questions and answers section to pdf print preview
     *
     * @param document pdf document
     * @param title title of section
     * @param partImage section image
     * @param answers list of answers
     */
    private void addQuestionPart(StyledDocument document, String title, ExtendedThemeResource partImage, List<Answer> answers) {
        try {
            final Image categoryImage = Image.getInstance(partImage.getAbsolutePath());
            categoryImage.setSpacingBefore(5);
            document.add(categoryImage);
        } catch (BadElementException e) {
            LOG.error("Wrong element.", e);
        } catch (IOException e) {
            LOG.error("File not found", e);
        }

        document.add(new Paragraph(title));
        final LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineWidth(0.5f);
        document.add(lineSeparator);

        answers.forEach(answer -> {
            if (answer.isAnswered()) {
                addQuestionAnswer(document, answer.getQuestion().getName(), answer.getValue());
            }
        });
    }


    /**
     * Adds free text as HTML to pdf print preview.
     *
     * @param document pdf document
     * @param text HTML text
     */
    private void addFreetext(StyledDocument document, String text) {
        final HTMLElement htmlElement = new HTMLElement(text);
        document.add(htmlElement);
    }

    /**
     * Adds question/answer combination to pdf document.
     *
     * Question part will be formatted bold and answer
     * text in regular.
     *
     * @param document pdf document
     * @param questionText question part
     * @param answerText answer part
     */
    private void addQuestionAnswer(StyledDocument document, String questionText, String answerText) {
        final Paragraph paragraph = new Paragraph();
        final Font boldFont = new Font(document.getTemplate().getBaseFont());
        boldFont.setStyle(Font.BOLD);
        paragraph.add(new Chunk(questionText + ": ", boldFont));
        final String text = answerText == null ? "" : answerText;
        paragraph.add(new Chunk(text));

        document.add(paragraph);
    }


    /**
     * Adds message history for given user.
     *
     * @param document pdf document
     * @param user source user
     * @param targetUser target user
     */
    private void addMessageHistory(StyledDocument document, User user, User targetUser) {
        document.newPage();

        HashMap<String, String> imageStylingMap = new HashMap<>();
        imageStylingMap.put("height", "12px");
        imageStylingMap.put("width", "12px");
        imageStylingMap.put("display", "inline-block");
        imageStylingMap.put("padding-left", "1px");
        imageStylingMap.put("padding-right", "1px");

        StyleSheet styleSheet = new StyleSheet();
        styleSheet.loadTagStyle("img", imageStylingMap);

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(getLocale());

        Page<Message> messageList = messageRepository.findAllByUserAndTargetUser(user, targetUser, new PageRequest(0, 10, new Sort(Sort.Direction.ASC, "sendDate")));

        boolean finished = !messageList.hasContent();
        while (!finished) {
            messageList.getContent().stream().forEach(message -> addSingleMessage(document, message, styleSheet, dateTimeFormatter));
            finished = !messageList.hasNext();
            messageList = messageRepository.findAllByUserAndTargetUser(user, targetUser, messageList.nextPageable());
        }
    }

    /**
     * Adds a single message to a pdf document.
     *
     * @param document pdf document
     * @param message message to display
     * @param styleSheet stylesheet for message body
     */
    private void addSingleMessage(StyledDocument document, Message message, StyleSheet styleSheet, DateTimeFormatter dateTimeFormatter) {
        final Paragraph header = new Paragraph();
        final Font senderFont = new Font(document.getTemplate().getBaseFont());
        senderFont.setSize(12);
        senderFont.setStyle(Font.ITALIC);
        final Font titleFont = new Font(document.getTemplate().getBaseFont());
        titleFont.setSize(14);
        titleFont.setStyle(Font.BOLDITALIC);

        header.add(new Chunk(message.getSenderEnvelope().getUser().getAlias(), senderFont));
        header.add(new Chunk("\n"));
        header.add(new Chunk(message.getSubject(), titleFont));
        header.add(new Chunk("\n"));
        header.add(new Chunk(message.getSendDate().format(dateTimeFormatter), senderFont));

        HTMLElement messageBody = new HTMLElement(styleSheet);
        messageBody.addContentModifier(new SVGtoPNGPathConverter(appUrlBuilder));
        messageBody.addContent(message.getBody());

        final LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineWidth(0.3f);

        document.add(header);
        document.add(lineSeparator);
        document.add(messageBody);
        document.add(new Paragraph());
    }
}
