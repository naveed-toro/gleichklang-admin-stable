package de.binaerebauten.gleichklang.core.service.report;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.CachedValueRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionGroupRepository;
import de.binaerebauten.gleichklang.core.utils.LocaleAware;
import de.binaerebauten.gleichklang.core.utils.ResultMapper;
import de.binaerebauten.gleichklang.core.utils.pdf.*;
import de.binaerebauten.gleichklang.core.utils.pdf.Header;
import de.binaerebauten.gleichklang.core.utils.pdf.chart.BarChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User Report Service
 *
 * Creates PDF File Resources for affinity questionaire results
 */
@Service
public class UserProfileReportService implements LocaleAware {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileReportService.class);

    // Value axis constants for bar charts
    private final static double VALUE_AXIS_MIN = -3;
    private final static double VALUE_AXIS_MAX = 3;
    private final static double NULL_VALUE = 0.1;

    // correction value for score values for bar chart representation
    private final static double SCORE_CORRECTION_VALUE = 4;

    // Color constants for bar charts
    private final static Color MAX_COLOR = new Color(222, 118, 97);
    private final static Color AVERAGE_COLOR = new Color(149, 214, 73);
    private final static Color MIN_COLOR = new Color(100, 153, 182);
    private final static Color plotBackground = new Color(206, 211, 198);

    private final AnswerRepository answerRepository;

    private final QuestionGroupRepository questionGroupRepository;

    private final DocumentFragmentService documentFragmentService;

    private final ResultMapper queryResultMapper;

    private final CachedValueRepository cachedValueRepository;

    @Autowired
    public UserProfileReportService(ResultMapper queryResultMapper, AnswerRepository answerRepository, DocumentFragmentService documentFragmentService, QuestionGroupRepository questionGroupRepository, CachedValueRepository cachedValueRepository) {
        this.queryResultMapper = queryResultMapper;
        this.answerRepository = answerRepository;
        this.documentFragmentService = documentFragmentService;
        this.questionGroupRepository = questionGroupRepository;
        this.cachedValueRepository = cachedValueRepository;
    }

    /**
     * Create Styled PDF document resource for 'Persoenlichkeitsprofil'.
     *
     * @param user User for whom the profile should be generated
     * @return styled PDF document resource
     */
    public StyledDocument getPersoenlichkeitProfileForUser(User user) {
        Objects.requireNonNull(user, "User is null");

        final StyledDocument document = PdfDocumentFactory.userProfileReport();
        document.open();
        document.setHeaderText(I18N.USERREPORTSERVICE_PERSOENLICHKEIT_PAGEHEADER.msg(user.getAlias()));

        addIntroduction(document, I18N.USERREPORTSERVICE_PERSOENLICHKEIT_PROFILE.msg(), user.getAlias());
        addDescription(document, I18N.USERREPORTSERVICE_PERSOENLICHKEIT_DESCRIPTION_HEADER, ReportFragmentTemplate.DESCRIPTION_PERSOENLICH);
        addAffinityResult(document, user, NaturalKeyEntity.NaturalKey.PROFILE_PERSOENLICHKEIT, I18N.USERREPORTSERVICE_CHART_TITLE_PERSOENLICH, ReportFragmentTemplate.DESCRIPRION_PERSOENLICH_DETAILS);

        document.close();
        return document;
    }


    /**
     * Create Styled PDF document resource for 'Partnerschaftsprofil'.
     *
     * @param user User for whom the profile should be generated
     * @return styled PDF document resource
     */
    public StyledDocument getPartnerschaftProfileForUser(User user) {
        Objects.requireNonNull(user, "User is null");

        final StyledDocument document = PdfDocumentFactory.userProfileReport();
        document.open();
        document.setHeaderText(I18N.USERREPORTSERVICE_PARTNERSCHAFT_PAGEHEADER.msg(user.getAlias()));

        addIntroduction(document, I18N.USERREPORTSERVICE_PARTNERSCHAFT_PROFILE.msg(), user.getAlias());
        addDescription(document, I18N.USERREPORTSERVICE_PARTNERSCHAFT_DESCRIPTION_HEADER, ReportFragmentTemplate.DESCRIPTION_PARTNERSCHAFT);
        addAffinityResult(document, user, NaturalKeyEntity.NaturalKey.PROFILE_PARTNERSCHAFT, I18N.USERREPORTSERVICE_CHART_TITLE_PARTNERSCHAFT, ReportFragmentTemplate.DESCRIPRION_PARTNERSCHAFT_DETAILS);

        document.close();
        return document;
    }


    /**
     * Create Styled PDF document resource for 'Freundschaftsprofil'.
     *
     * @param user User for whom the profile should be generated
     * @return styled PDF document resource
     */
    public StyledDocument getFreundschaftProfileForUser(User user) {
        Objects.requireNonNull(user, "User is null");

        final StyledDocument document = PdfDocumentFactory.userProfileReport();
        document.open();
        document.setHeaderText(I18N.USERREPORTSERVICE_FREUNDSCHAFT_PAGEHEADER.msg(user.getAlias()));

        addIntroduction(document, I18N.USERREPORTSERVICE_FREUNDSCHAFT_PROFILE.msg(), user.getAlias());
        addDescription(document, I18N.USERREPORTSERVICE_FREUNDSCHAFT_DESCRIPTION_HEADER, ReportFragmentTemplate.DESCRITPION_FREUNDSCHADT);
        addAffinityResult(document, user, NaturalKeyEntity.NaturalKey.PROFILE_FREUNDSCHAFT, I18N.USERREPORTSERVICE_CHART_TITLE_FREUNDSCHAFT, ReportFragmentTemplate.DESCRIPRION_FREUNDSCHAFT_DETAILS);

        document.close();
        return document;
    }

    /**
     * Create Styled PDF document resource for 'Gesellschaftsprofil'.
     *
     * @param user User for whom the profile should be generated
     * @return styled PDF document resource
     */
    public StyledDocument getGesellschaftProfileForUser(User user) {
        Objects.requireNonNull(user, "User is null");

        final StyledDocument document = PdfDocumentFactory.userProfileReport();
        document.open();
        document.setHeaderText(I18N.USERREPORTSERVICE_GESELLSCHAFT_PAGEHEADER.msg(user.getAlias()));

        addIntroduction(document, I18N.USERREPORTSERVICE_GESELLSCHAFT_PROFILE.msg(), user.getAlias());
        addDescription(document, I18N.USERREPORTSERVICE_GESELLSCHAFT_DESCRIPTION_HEADER, ReportFragmentTemplate.DESCRIPTION_GESELLSCHAFT);
        addAffinityResult(document, user, NaturalKeyEntity.NaturalKey.PROFILE_GESELLSCHAFT, I18N.USERREPORTSERVICE_CHART_TITLE_GESELLSCHAFT, ReportFragmentTemplate.DESCRIPRION_GESELLSCHAFT_DETAILS);

        document.close();
        return document;
    }

    /**
     * Creates a bar chart for the given dataset and title.
     *
     * @param dataset category and value dataset
     * @param title title of the chart
     * @return formatted bar chart object
     */
    private BarChart getBarchart(Map<String, Integer> dataset, String title) {
        DefaultCategoryDataset barChartDataset = new DefaultCategoryDataset();
        final Map<String, QuestionGroup> questionGroupMap = queryResultMapper.convertToQuestionGroupMap(questionGroupRepository.findByI18nKeyIn(dataset.keySet()));
        for (Map.Entry<String, Integer> entry : dataset.entrySet()) {
            double value = entry.getValue().doubleValue() - SCORE_CORRECTION_VALUE;
            if (value == 0) value = NULL_VALUE;
            barChartDataset.addValue(value, I18N.USERREPORTSERVICE_BARCHART_FEATURES.msg(),  questionGroupMap.get(entry.getKey()).getName());
        }
        BarChart barChart = new BarChart(barChartDataset);
        barChart.getChart().setBackgroundImageAlpha(1f);

        barChart.setWidth(500.0f);
        barChart.setHeight(360.0f);
//        barChart.setChartTitle(title);

        CategoryPlot plot = barChart.getChart().getCategoryPlot();

        // Number axis
        NumberAxis numberAxis = (NumberAxis)plot.getRangeAxis();
        numberAxis.setRange(VALUE_AXIS_MIN, VALUE_AXIS_MAX);
        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberAxis.setLabel(I18N.USERREPORTSERVICE_CHART_AXIS_VALUE_LABEL.msg());


        // Category axis
        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        categoryAxis.setLabel(I18N.USERREPORTSERVICE_CHART_AXIS_CATEGORY_LABEL.msg());

//        barChart.getChart().setBackgroundImageAlpha(0.5f);
//        barChart.getChart().setBackgroundPaint(plotBackground);
        barChart.getChart().setBorderVisible(false);
        barChart.getChart().getCategoryPlot().setBackgroundPaint(plotBackground);

        return barChart;
    }

    /**
     * Adds static introduction text to given document.
     *
     * @param document document in which the introduction should be inserted
     */
    private void addIntroduction(StyledDocument document, String profileType, String user) {
        VectorImage logoImage = new VectorImage("/img/logo.pdf");
        logoImage.setAlignment(VectorImage.Alignment.CENTER);
        logoImage.setWidth(document.getPageSize().getWidth() * 0.6f);
        logoImage.setHeight(70.0f);
        document.add(logoImage);

        Font titleFont = new Font(document.getTemplate().getBaseFont());
        titleFont.setSize(12.0f);
        titleFont.setColor(Color.gray);
        titleFont.setStyle(Font.BOLD);
        TextParagraph title = new TextParagraph(I18N.USERREPORTSERVICE_TITLE.msg(profileType), titleFont);
        Font titleUserFont = new Font(titleFont);
        titleUserFont.setStyle(Font.BOLDITALIC);
        title.add(new Chunk(" " + user, titleUserFont));
        title.setStyled(true);
        title.setSpacingBefore(100.0f);
        document.add(title);

        Font subTitleFont = new Font(document.getTemplate().getBaseFont());
        subTitleFont.setSize(9.0f);
        subTitleFont.setColor(Color.gray);
        TextParagraph subTitle = new TextParagraph(I18N.USERREPORTSERVICE_SUBTITLE.msg(), subTitleFont);
        subTitle.setStyled(true);
        subTitle.setSpacingAfter(30.0f);
        document.add(subTitle);


        Header header = new Header(I18N.USERREPORTSERVICE_GREETINGS_HEADER.msg(), Header.HeaderType.H1);
        Paragraph paragraph = new Paragraph(I18N.USERREPORTSERVICE_INTRO_PARAGRAPH.msg());

        document.add(header);
        document.add(documentFragmentService.createDocumentFragmentFromTemplate(ReportFragmentTemplate.INTRODUCTION, getLocale()));

        Anchor anchor = new Anchor("http://www.gleichklang.de");
        anchor.setReference("http://www.gleichklang.de");
        document.add(anchor);
        document.newPage();
    }

    /**
     * Adds description page to a given pdf document.
     *
     * @param document document where the page should be included
     * @param header header text for the description
     * @param template text fragment template of the description
     */
    private void addDescription(StyledDocument document, I18N header, ReportFragmentTemplate template) {
        document.add(new Header(header.msg(), Header.HeaderType.H2));
        document.add(documentFragmentService.createDocumentFragmentFromTemplate(template, getLocale()));
        document.add(getResultLegend());
        document.newPage();
    }

    /**
     * Adds the result of the affinity questions of the user as chart and detailed description to the given document.
     *
     * @param document document where affinity result should be included
     * @param user the user the results are based of
     * @param category name of the affinity questionnaire
     * @param chartTitle title of the chart
     * @param descriptionTemplate text fragment template of the detailed description
     */
    private void addAffinityResult(StyledDocument document, User user, NaturalKeyEntity.NaturalKey category, I18N chartTitle, ReportFragmentTemplate descriptionTemplate) {
        Map<String, Integer> affinityResult = getAffinityResult(user, category);
        if (affinityResult != null) {
            BarChart barChart = getBarchart(affinityResult, chartTitle.msg());
            document.add(barChart);

            Map<String, Object> textResult = createTemplateModel(affinityResult);
            List<HTMLElement> bulletListElements = documentFragmentService.createDetailDescription(descriptionTemplate, textResult, getLocale());
            com.lowagie.text.List bulletList = new com.lowagie.text.List();
            bulletListElements.forEach(htmlElement -> bulletList.add(new TextListItem(htmlElement)));
            document.add(bulletList);
        }
    }

    /**
     * Returns Legend.
     */
    private Element getResultLegend() {
        Map<String, Color> legendMap = new LinkedHashMap<>();
        legendMap.put(I18N.USERREPORTSERVICE_CHART_RESULT.msg(I18N.USERREPORTSERVICE_CHART_RESULT_MAX.msg()), MAX_COLOR);
        legendMap.put(I18N.USERREPORTSERVICE_CHART_RESULT.msg(I18N.USERREPORTSERVICE_CHART_RESULT_AVERAGE.msg()), AVERAGE_COLOR);
        legendMap.put(I18N.USERREPORTSERVICE_CHART_RESULT.msg(I18N.USERREPORTSERVICE_CHART_RESULT_MIN.msg()), MIN_COLOR);

        Paragraph legend = new Paragraph();
        legend.add(new BoldText(I18N.USERREPORTSERVICE_LEGEND_TITLE.msg() + ":"));
        for (Map.Entry<String, Color> entry : legendMap.entrySet()) {
            legend.add(new Chunk("\n"));
            Chunk labelColor = new Chunk("   ");
            labelColor.setBackground(entry.getValue());
            legend.add(labelColor);
            Chunk labelText = new Chunk(" " + entry.getKey());
            legend.add(labelText);
        }

        return legend;
    }


    /**
     * Converts the result from Affinity calculation to template model.
     *
     * @param affinityResult affinity calculation result
     * @return template model
     */
    private Map<String, Object> createTemplateModel(Map<String, Integer> affinityResult) {
        Map<String, Object> textResult = new HashMap<>();
        for (Map.Entry<String, Integer> entry : affinityResult.entrySet()) {
            textResult.put(entry.getKey().replace("$1", ""), transformValueToLabel(entry.getValue()));
        }

        return textResult;
    }

    /**
     * Transforms a given value to a specific text label.
     *
     * @param value value between 1 and 7
     * @return text label
     */
    private String transformValueToLabel(Integer value) {
        checkArgument(value >= 1 && value <= 7, "Value must be between 1 and 7");

        switch (value) {
            case 1: return I18N.USERREPORTSERVICE_SCORE_LABEL_1.msg();
            case 2: return I18N.USERREPORTSERVICE_SCORE_LABEL_2.msg();
            case 3: return I18N.USERREPORTSERVICE_SCORE_LABEL_3.msg();
            case 4: return I18N.USERREPORTSERVICE_SCORE_LABEL_4.msg();
            case 5: return I18N.USERREPORTSERVICE_SCORE_LABEL_5.msg();
            case 6: return I18N.USERREPORTSERVICE_SCORE_LABEL_6.msg();
            case 7: return I18N.USERREPORTSERVICE_SCORE_LABEL_7.msg();
        }

        return "";
    }


    /**
     * Calculates Affinity Score for given Affinity and User.
     *
     * @param user the user for whom the affinity score is calculated
     * @param naturalKey the affinity
     * @return affinity score
     */
    private Map<String, Integer> getAffinityResult(User user, NaturalKeyEntity.NaturalKey naturalKey) {
//        final List<AffinityResult> personalityResults = queryResultMapper.convertToAffinityResult(answerRepository.getAggregatedAffinityResultsForKey(naturalKey.naturalKey));
        final List<AffinityResult> personalityResults = queryResultMapper.convertToCachedAffinityResult(cachedValueRepository.findByCacheGroup(naturalKey.naturalKey));
        final List<AffinityUserResult> userResults = queryResultMapper.convertToAffinityUserResult(answerRepository.getAggregatedAffinityUserResultsForKey(user.getId(), naturalKey.naturalKey));

        if (personalityResults.size() != userResults.size()) {
            return null;
        }

        final Map<String, AffinityResult> calculatedResult = personalityResults.stream().collect(Collectors.toMap(a -> a.getCategory(), a -> a));
        final Map<String, Integer> sumUserResult = userResults.stream().collect(Collectors.toMap(a -> a.getCategory(), a -> a.getSum()));

        return calculateScore(sumUserResult, calculatedResult);
    }

    /**
     * Calculates the user score.
     *
     * @param userResult questionaire result of user
     * @param allResult questionaire result of all users
     * @return user score
     */
    private Map<String, Integer> calculateScore(Map<String, Integer> userResult, Map<String, AffinityResult> allResult) {
        HashMap<String, Integer> userScore = new HashMap<>();
        for (String category : allResult.keySet()) {
            AffinityResult affinityResult = allResult.get(category);
            double score = ((double)userResult.get(category) - affinityResult.getMean()) / affinityResult.getStandardDeviation();
            int result = 4 + (int)(score / 0.6);
            if (result < 1) result = 1;
            if (result > 7) result = 7;
            userScore.put(category, result);
        }

        return userScore;
    }
}
