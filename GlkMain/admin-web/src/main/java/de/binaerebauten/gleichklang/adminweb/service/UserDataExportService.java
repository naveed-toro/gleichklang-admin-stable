package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.UserDataExportPopup;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserStatistic;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionnaireRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserStatisticRepository;
import de.binaerebauten.gleichklang.core.service.LocatableService;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class UserDataExportService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDataExportService.class);

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public List<Map<User,List<Object>>> exportUserDataAdmin(List<User> userList, UserDataExportPopup userDataExportPopup) {


        LOG.info(String.format("Exporting %d users ...", userList.size()));

        final RequestExecutor requestExecutor = RequestExecutor.getExecutorService();
        List<Future<Map<User,List<Object>>>> answerList = null;
        List<Map<User,List<Object>>> answerListFinal = new ArrayList<>();

        try
        {
            int numThreads=10;
            int start=0;
            int end=numThreads;

            if(userList.size()>100)
            {
                numThreads=100;
                end=numThreads;
            }

            if(userList.size()>1000)
            {
                numThreads=1000;
                end=numThreads;
            }

            List<ExpenSiveUserDataExportTask> expenSiveUserDataExportTasks = new ArrayList<>();

            while(start<=(userList.size()))
            {
                if (((userList.size())-start) < numThreads)
                {
                    end = start + ((userList.size()) - start);
                }
                LOG.info("expenSiveUserDataExportTasks = "+start+"-----"+end);
                expenSiveUserDataExportTasks.add(new ExpenSiveUserDataExportTask(userList,start,end,userDataExportPopup));

                start+=numThreads;
                end+=numThreads;
            }

            try
            {
                answerList = requestExecutor.invokeAll(expenSiveUserDataExportTasks);
                answerList.forEach(mapFuture -> {
                   try
                   {
                       if(mapFuture.isDone())
                       {
                           answerListFinal.add(mapFuture.get());
                       }
                   }
                   catch (InterruptedException| ExecutionException e)
                   {
                       LOG.error("Error in Data export of users from User Controll", e);
                   }
                    mapFuture=null;
                });
                LOG.info("answerListFinal = "+answerListFinal.size());

            } catch (InterruptedException e)
            {
                LOG.error("Error in Data export of users from User Controll", e);
            }



        } catch (Exception e)
        {
            LOG.error("Error in Data export of users from User Controll", e);
        }
        finally
        {
            requestExecutor.destroy();
        }


        return answerListFinal;
    }

    public String[] getRegionsHeaders()
    {
        LocatableService locatableService = AppUI.getApplicationContext().getBean(LocatableService.class);
        List<String> finalHeaders=new ArrayList<>();
        finalHeaders.add("RegS:Worldwide");
        locatableService.getContinents().forEach(continent -> finalHeaders.add("RegSCont:"+continent.getName()));
        locatableService.getContinents().forEach(continent -> locatableService.getCountries(continent).forEach(country -> finalHeaders.add("RegSCntry:"+continent.getName()+":"+country.getName())));
        locatableService.getContinents().forEach(continent -> locatableService.getCountries(continent).forEach(country -> locatableService.getRegions(country).forEach(region ->finalHeaders.add("RegSState:"+continent.getName()+":"+country.getName()+":"+region.getName()))));

        return finalHeaders.toArray(new String[0]);
    }

    public String[] getRegionsHeadersAnswersByUser(User user,I18NEntity.Language language )
    {
        final AnswerRepository answerRepository = AppUI.getApplicationContext().getBean(AnswerRepository.class);

        List<Object> regionsHeaders = answerRepository.getRegionsHeadersAnswersByUser(user.getId(),language.name());

        return regionsHeaders.toArray(new String[0]);
    }



    public Map<String,String> getProximityHeadersAndAnswersByUser(User user,I18NEntity.Language language )
    {
        LocatableRepository locatableRepository = AppUI.getApplicationContext().getBean(LocatableRepository.class);
        AnswerRepository answerRepository = AppUI.getApplicationContext().getBean(AnswerRepository.class);
        I18NRepository i18NRepository = AppUI.getApplicationContext().getBean(I18NRepository.class);

        Map<String,String> proximityHeadersAndAnswersMap=new LinkedHashMap<>();
        List<Object> distRstrctcntryZip = answerRepository.getProximityHeadersAndAnswersByUser(user.getId());

        int i=0;
        for(Object distRstctcntryZip: distRstrctcntryZip) {

            Object contryContinent = locatableRepository.getProximityContriesContinentsByZipId(Long.valueOf(((BigInteger) ((Object[]) distRstctcntryZip)[3]).toString()));

            String[] cntryCntntS = ((String) contryContinent).split(":");

            proximityHeadersAndAnswersMap.put("ProxS"+i+":Continent",((I18NEntity)i18NRepository.findByKeyAndLanguage((String) cntryCntntS[0],language)).getValue());
            proximityHeadersAndAnswersMap.put("ProxS"+i+":Country",((I18NEntity)i18NRepository.findByKeyAndLanguage((String) cntryCntntS[1],language)).getValue());
            proximityHeadersAndAnswersMap.put("ProxS"+i+":ZipCode",((String) ((Object[]) distRstctcntryZip)[2]).toString());
            proximityHeadersAndAnswersMap.put("ProxS"+i+":Proximity_radius",((Integer) ((Object[]) distRstctcntryZip)[0]).toString());
            proximityHeadersAndAnswersMap.put("ProxS"+i+":Country_only",((Boolean) ((Object[]) distRstctcntryZip)[1]).toString().equals("true")?"1":"0");

            i++;

        }

        return proximityHeadersAndAnswersMap;
    }

    public List<Object> getHeadersForUserDataExportFromAdmin(List<Long> qids)
    {
        QuestionnaireRepository questionnaireRepository = AppUI.getApplicationContext().getBean(QuestionnaireRepository.class);
        List<Object> headerList = questionnaireRepository.getHeadersForUserDataExportFromAdmin(qids);
        return  headerList;
    }
}
