package com.kickit.domain;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.List;

public class EventInvite {

    private EventDetails eventDetails;

    @CreatedBy
    private String requester;

    @CreatedDate
    Date createdDate;

    @LastModifiedBy
    String lastModifiedBy;

    @LastModifiedDate
    Date lastModifiedDate;
    List<String> invitees;
    List<String> acceptedInvitees;
    List<String> declinedInvitees;
    List<String> maybeInvitees;
}
