package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// @Component: このクラスを Spring Bean として登録する
@Component
public class BatchScheduler {

    // JobLauncher: Job を実行するためのインターフェース
    @Autowired
    private JobLauncher jobLauncher;

    // BatchConfig で定義した Job を注入する
    @Autowired
    private Job sampleJob;

    // 30秒ごとに Spring Batch の Job を起動する
    // fixedRate: 前回の実行開始から指定ミリ秒後に次を実行
    @Scheduled(fixedRate = 30000)
    public void runBatchJob() {
        try {
            // JobParameters: Job の実行ごとに異なるパラメータを渡す
            // time を含めることで同じ Job を何度でも再実行できる
            // （同一パラメータの Job は Spring Batch が重複実行を防ぐため）
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            System.out.println("[BatchScheduler] Spring Batch Job を起動します");
            jobLauncher.run(sampleJob, params);
            System.out.println("[BatchScheduler] Spring Batch Job が完了しました");

        } catch (Exception e) {
            System.err.println("[BatchScheduler] Job 実行中にエラーが発生しました: " + e.getMessage());
        }
    }
}
