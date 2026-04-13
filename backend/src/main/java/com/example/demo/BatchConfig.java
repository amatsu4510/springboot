package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

// @Configuration: このクラスに @Bean メソッドが含まれることを Spring に伝える
@Configuration
public class BatchConfig {

    // ─── Job 定義 ────────────────────────────────────────────────────────────
    // Job は 1 つ以上の Step を束ねたバッチ処理の単位
    @Bean
    Job sampleJob(JobRepository jobRepository, Step sampleStep) {
        return new JobBuilder("sampleJob", jobRepository)
                .start(sampleStep)   // 最初に実行する Step を指定
                .build();
    }

    // ─── Step 定義 ───────────────────────────────────────────────────────────
    // Step は Reader → Processor → Writer の 1 サイクルを定義する
    // chunk(5) = 5件ずつまとめて処理してからコミットする
    @Bean
    Step sampleStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager) {
        return new StepBuilder("sampleStep", jobRepository)
                .<String, String>chunk(5, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    // ─── ItemReader ──────────────────────────────────────────────────────────
    // 処理対象データを 1 件ずつ読み込む
    // ListItemReader: インメモリのリストからデータを順番に返す
    @Bean
    ListItemReader<String> itemReader() {
        List<String> items = List.of(
                "item1", "item2", "item3", "item4", "item5",
                "item6", "item7", "item8", "item9", "item10"
        );
        return new ListItemReader<>(items);
    }

    // ─── ItemProcessor ───────────────────────────────────────────────────────
    // 読み込んだデータを 1 件ずつ加工する
    // ここでは文字列を大文字に変換する（null を返すとその件はスキップされる）
    @Bean
    ItemProcessor<String, String> itemProcessor() {
        return item -> {
            String processed = item.toUpperCase();
            System.out.println("  [Processor] " + item + " → " + processed);
            return processed;
        };
    }

    // ─── ItemWriter ──────────────────────────────────────────────────────────
    // Processor の出力を chunk サイズ分まとめて受け取り、書き出す
    // ここではコンソールへ出力するだけのシンプルな実装
    @Bean
    ItemWriter<String> itemWriter() {
        return chunk -> {
            System.out.println("  [Writer] " + chunk.size() + "件を書き込み:");
            chunk.getItems().forEach(item -> System.out.println("    → " + item));
        };
    }
}
