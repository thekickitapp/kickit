package com.kickit.controller;

import com.kickit.domain.User;
import com.kickit.service.DDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/main")
public class KickItController {

    @Autowired
    DDBService userService;

    @GetMapping("/hello")
    public String helloWorld() {

        return "HelloWorld";
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

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUserByIdentifier(userId);

        return new ResponseEntity<String>("Deleted user", HttpStatus.OK);
    }
}
