package com.kickit.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Test {

    @Autowired
    EventDetailsRepository repository;

    public List<EventDetails> findAllEvents() {
        return repository.findAll();
    }
}
