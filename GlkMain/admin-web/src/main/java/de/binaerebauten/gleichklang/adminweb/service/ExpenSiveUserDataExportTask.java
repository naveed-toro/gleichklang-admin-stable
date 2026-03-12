package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.UserDataExportPopup;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class ExpenSiveUserDataExportTask implements Callable<Map<User,List<Object>>> {

    private final int start;
    private final int  end;
    private final List<User> userList;
    private final UserDataExportPopup  userDataExportPopup;
    private final UserDataExportService exportService;
    private static final Logger LOG = LoggerFactory.getLogger(ExpenSiveUserDataExportTask.class);

    public ExpenSiveUserDataExportTask(List<User> userList, int start, int end, UserDataExportPopup userDataExportPopup) {
        this.start = start;
        this.end=end;
        this.userList=userList;
        this.userDataExportPopup=userDataExportPopup;
        this.exportService= AppUI.getApplicationContext().getBean(UserDataExportService.class);
    }

    @Override
    public Map<User,List<Object>> call() throws Exception {

        Map<User,List<Object>> userAnswerListMap= new LinkedHashMap<>();
        LOG.info("ExpenSiveUserDataExportTask = "+start+"-----"+end);
        for(int i=start;i<end;i++)
        {
            User user=userList.get(i);
            List<String> answerList=new ArrayList<>();
            long startTime = System.nanoTime();
            String[] headerList=userDataExportPopup.getHeaders();
            for(String header:headerList)
            {
                if(header!=null && header.contains("#"))
                {
                    String qtype = header.split("#")[0];

                    if (qtype.contains(":"))
                    {
                        qtype = qtype.split(":")[0];

                    }
                    if ((qtype.equals("TextQuestion") || qtype.equals("NumberQuestion") || qtype.equals("ChoiceQuestion")))
                    {
                        if (qtype.equals("ChoiceQuestion") && header.split("::")[1].split(",").length == 2)
                        {
                            Long qid = Long.parseLong(header.split("::")[1].split(",")[0]);
                            Long cid = Long.parseLong(header.split("::")[1].split(",")[1]);

                            String answer = (String) userDataExportPopup.answerRepository.getChoiceAnswersForUserDataExportFromAdmin(user.getId(), qid, cid);

                            if (answer != null)
                            {
                                answerList.add(1 + "");
                            } else
                            {
                                answerList.add(0 + "");
                            }


                        } else if ((qtype.equals("TextQuestion") || qtype.equals("NumberQuestion"))) {
                            Long qid = Long.parseLong(header.split("::")[1]);
                            String answer = (String) userDataExportPopup.answerRepository.getAnswersForUserDataExportFromAdmin(user.getId(), qid);
                            answerList.add(answer);
                        }
                    }
                }

                //Logic for region search answers

                String[] answers = exportService.getRegionsHeadersAnswersByUser(user,userDataExportPopup.getlanguage());


                    if(header.contains("RegS:Worldwide"))
                    {
                        if(answers.length==0)
                        {
                            answerList.add(0 + "");
                        }
                        else
                        {
                            answerList.add(1 + "");
                        }
                    }
                    if(header.contains("RegSCont") || header.contains("RegSCntry") || header.contains("RegSState"))
                    {

                        boolean headerAnswerExist=false;

                        for (String answer : answers)
                        {

                            String finalHeader=header.substring(header.indexOf(":")+1,header.length());

                            if(answer.split(":").length==1)
                            {
                                answer=answer.split(":")[0];
                            }

                            if(answer.split(":").length==2)
                            {
                                answer=answer.split(":")[0]+":"+answer.split(":")[1];
                            }

                            if (finalHeader.equals(answer))
                            {
                                headerAnswerExist=true;
                            }

                        }

                        if (headerAnswerExist)
                        {
                            answerList.add(1 + "");
                        }
                        else
                        {
                            answerList.add(0 + "");
                        }
                    }

                if(header.contains("ProxS"))
                {
                    final Map<String,String> proximityHeadersMap = exportService.getProximityHeadersAndAnswersByUser(user,userDataExportPopup.getlanguage());
                    answerList.add(proximityHeadersMap.get(header));

                 }
            }

            List<Object> obJectList=userDataExportPopup.getRecords(user);
            answerList.forEach(item -> obJectList.add(item));
            userAnswerListMap.put(user,obJectList);
        }
        LOG.info("userAnswerListMap in ExpenSiveUserDataExportTask = "+userAnswerListMap.size());
        return userAnswerListMap;
    }

}