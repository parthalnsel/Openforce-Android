package com.openforce.model;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class MessagesResponse {

    private DocumentSnapshot lastMessageDocSnap;
    private List<Message> messages;

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public DocumentSnapshot getLastMessageDocSnap() {
        return lastMessageDocSnap;
    }

    public void setLastMessageDocSnap(DocumentSnapshot lastMessageDocSnap) {
        this.lastMessageDocSnap = lastMessageDocSnap;
    }

    public MessagesResponse(DocumentSnapshot lastConversationDocSnap, List<Message> conversations) {
        this.lastMessageDocSnap = lastConversationDocSnap;
        this.messages = conversations;
    }
}
