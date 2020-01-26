package com.kickit.controller;

import com.kickit.domain.EventDetails;
import com.kickit.domain.Test;
import com.kickit.domain.User;
import com.kickit.service.DDBService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.InitialPositionInStream;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@RestController
@RequestMapping("/main")
public class KickItController {

    private static final InitialPositionInStream SAMPLE_APPLICATION_INITIAL_POSITION_IN_STREAM =
            InitialPositionInStream.LATEST;

    private static final Logger log = LoggerFactory.getLogger(KickItController.class);


    @Autowired
    DDBService userService;

    @Autowired
    Test testService;

    private static final Region region = Region.US_WEST_2;
    KinesisAsyncClient kinesisAsyncClient = KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(this.region));


    @GetMapping("/hello")
    public String helloWorld() {



        ScheduledExecutorService producerExecutor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> producerFuture = producerExecutor.scheduleAtFixedRate(this::publishRecord, 10, 1, TimeUnit.SECONDS);

        DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder().region(region).build();
        CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().region(region).build();
        ConfigsBuilder configsBuilder = new ConfigsBuilder("firstStream", "firstStream", kinesisAsyncClient, dynamoClient, cloudWatchClient, UUID.randomUUID().toString(), new KickItRecordProcessorFactory());

        Scheduler scheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                configsBuilder.retrievalConfig()
        );

        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.setDaemon(true);
        schedulerThread.start();
        System.out.println("Press enter to shutdown");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (IOException ioex) {
            log.error("Caught exception while waiting for confirm. Shutting down.", ioex);
        }

        log.info("Cancelling producer, and shutting down executor.");
        producerFuture.cancel(true);
        producerExecutor.shutdownNow();

        Future<Boolean> gracefulShutdownFuture = scheduler.startGracefulShutdown();
        log.info("Waiting up to 20 seconds for shutdown to complete.");
        try {
            gracefulShutdownFuture.get(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.info("Interrupted while waiting for graceful shutdown. Continuing.");
        } catch (ExecutionException e) {
            log.error("Exception while executing graceful shutdown.", e);
        } catch (TimeoutException e) {
            log.error("Timeout while waiting for shutdown. Scheduler may not have exited.");
        }
        log.info("Completed, shutting down now.");

        return "HelloWorld";
    }
    private void publishRecord() {
        PutRecordRequest request = PutRecordRequest.builder()
                .partitionKey(RandomStringUtils.randomAlphabetic(5, 20))
                .streamName("firstStream")
                .data(SdkBytes.fromByteArray(RandomUtils.nextBytes(10)))
                .build();
        try {
            kinesisAsyncClient.putRecord(request).get();
        } catch (InterruptedException e) {
            log.info("Interrupted, assuming shutdown.");
        } catch (ExecutionException e) {
            log.error("Exception while sending data to Kinesis. Will try again next cycle.", e);
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createNewUser(@Valid @RequestBody User user, BindingResult result) {

        User createdUser = userService.saveOrUpdateUser(user);
        return new ResponseEntity<User>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> findUserById(@PathVariable String userId) {

        User user = userService.findUserById(userId);
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @GetMapping("/all")
    public List<User> findAllUsers() {

        return userService.findAllUsers();
    }

    @GetMapping("/events/all")
    public List<EventDetails> findAllEvents() {

        return testService.findAllEvents();
    }

    @GetMapping("/events/put")
    public List<EventDetails> putRecord() {

        KinesisClient client = KinesisClient.builder().region(Region.US_WEST_2).build();
        List <PutRecordsRequestEntry> putRecordsRequestEntryList  = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            PutRecordsRequestEntry putRecordsRequestEntry  = PutRecordsRequestEntry.builder().data(SdkBytes.fromUtf8String(String.valueOf(i)))
                    .partitionKey(String.format("partitionKey-%d", i))
                    .build();
            putRecordsRequestEntryList.add(putRecordsRequestEntry);
        }
        PutRecordsRequest putRecordsRequest  = PutRecordsRequest.builder().streamName("firstStream")
                .records(putRecordsRequestEntryList)
                .build();

        PutRecordsResponse putRecordsResult  = client.putRecords(putRecordsRequest);
        System.out.println("Put Result" + putRecordsResult);
        return testService.findAllEvents();
    }

    @GetMapping("/events/ga")
    public String get(){


        return "Hi";
    }

    @GetMapping("/events/g")
    public List<EventDetails> getRecord() {

        KinesisClient client = KinesisClient.builder().region(Region.US_WEST_2).build();
        List<Record> records;
        String shardIterator;
        GetShardIteratorRequest getShardIteratorRequest = GetShardIteratorRequest.builder()
                .streamName("firstStream")
                .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                .shardId("shardId-000000000001")
                .build();

        GetShardIteratorResponse getShardIteratorResult = client.getShardIterator(getShardIteratorRequest);
        shardIterator = getShardIteratorResult.shardIterator();
        while (true) {

            // Create a new getRecordsRequest with an existing shardIterator
            // Set the maximum records to return to 25
            GetRecordsRequest getRecordsRequest = GetRecordsRequest.builder().shardIterator(shardIterator).limit(1000).build();

            GetRecordsResponse result = client.getRecords(getRecordsRequest);

            // Put the result into record list. The result can be empty.
            records = result.records();

            for(Record record : records){
                System.out.println(record.data().toString());
            }

            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }

            shardIterator = result.nextShardIterator();
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUserByIdentifier(userId);

        return new ResponseEntity<String>("Deleted user", HttpStatus.OK);
    }
}
