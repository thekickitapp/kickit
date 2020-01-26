package com.kickit.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.kickit.domain.EventDetails;
import com.kickit.domain.User;
import com.kickit.domain.EventDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DDBService {

    @Autowired
    DynamoDBMapper mapper;

    public User saveOrUpdateUser(User user) {
        mapper.save(user);
        return findUserById(user.getUserId());
    }

    public User findUserById(String userId) {
        return mapper.load(User.class, userId);
    }

    public List<User> findAllUsers() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.setIndexName("userId-index");
        List<User> userList = mapper.scan(User.class, scanExpression);
        return userList;
    }

    public void deleteUserByIdentifier(String userId) {
        User user = findUserById(userId);
        if (user != null) {
            mapper.delete(user);
        }
    }

    public List<EventDetails> findAllEvents() {
        return null;
    }
}
