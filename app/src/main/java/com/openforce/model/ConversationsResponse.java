package com.openforce.model;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class ConversationsResponse {

    private DocumentSnapshot lastConversationDocSnap;
    private List<Conversation> conversations;

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public DocumentSnapshot getLastConversationDocSnap() {
        return lastConversationDocSnap;
    }

    public void setLastConversationDocSnap(DocumentSnapshot lastConversationDocSnap) {
        this.lastConversationDocSnap = lastConversationDocSnap;
    }

    public ConversationsResponse(DocumentSnapshot lastConversationDocSnap, List<Conversation> conversations) {
        this.lastConversationDocSnap = lastConversationDocSnap;
        this.conversations = conversations;
    }

    @Override
    public String toString() {
        return "ConversationsResponse{" +
                "conversations=" + conversations +
                '}';
    }
}
