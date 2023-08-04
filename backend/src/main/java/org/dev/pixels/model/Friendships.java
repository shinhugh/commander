package org.dev.pixels.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Friendships {
    private List<Friendship> confirmedFriendships;
    private List<Friendship> outgoingRequests;
    private List<Friendship> incomingRequests;

    public List<Friendship> getConfirmedFriendships() {
        return confirmedFriendships;
    }

    public void setConfirmedFriendships(List<Friendship> confirmedFriendships) {
        this.confirmedFriendships = confirmedFriendships;
    }

    public List<Friendship> getOutgoingRequests() {
        return outgoingRequests;
    }

    public void setOutgoingRequests(List<Friendship> outgoingRequests) {
        this.outgoingRequests = outgoingRequests;
    }

    public List<Friendship> getIncomingRequests() {
        return incomingRequests;
    }

    public void setIncomingRequests(List<Friendship> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }
}
