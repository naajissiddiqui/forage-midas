package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TaskThreeTests {

    static final Logger logger = LoggerFactory.getLogger(TaskThreeTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    // 🔹 New: to read Waldorf’s balance from the DB
    @Autowired
    private UserRecordRepository userRecordRepository;

    @Test
    void task_three_verifier() throws InterruptedException {
        // 1) Load initial users into H2
        userPopulator.populate();

        // 2) Send all transactions to Kafka
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // 3) Give the Kafka listener a bit of time to consume and process
        Thread.sleep(2000);

        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("use your debugger or these logs to find out what waldorf's balance is after all transactions are processed");
        logger.info("kill this test once you find the answer");

        // 4) Keep the test alive so you can inspect/log the balance
        while (true) {
            UserRecord waldorf = userRecordRepository.findByName("waldorf");
            if (waldorf != null) {
                logger.info("waldorf balance = {}", waldorf.getBalance());
            } else {
                logger.info("waldorf not found in database yet");
            }

            Thread.sleep(20000);
            logger.info("...");
        }
    }
}
