package com.kickit.domain;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface EventDetailsRepository extends CrudRepository<EventDetails, String> {

    List<EventDetails> findAll();
}
