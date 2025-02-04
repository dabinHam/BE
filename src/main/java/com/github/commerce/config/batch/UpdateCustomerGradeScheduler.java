package com.github.commerce.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class UpdateCustomerGradeScheduler { //매월 1일 오전 12:00:00 구매 금액에 따른 회원 등급 조정

    @Autowired
    private JobLauncher jobLauncher; //Scheduling할 때 따로 AutoWired 해주어야함.

    @Autowired
    private Job updateCustomerGradeJob;

    //TODO. 일단은 매일 오전 12시로 해놓고 시연할 때는 30초로 간격 줄이기
    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul") //초 분 시 일 월 요일 (*: 매번) - 매월 1일 오전 12:00:00 구매 금액에 따른 회원 등급 조정
    public void updateCustomerGradeJobRun() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        JobParameters jobParameters = new JobParameters(
                Collections.singletonMap("requestTime", new JobParameter(System.currentTimeMillis()))
        );

        jobLauncher.run(updateCustomerGradeJob, jobParameters);
    }
}
