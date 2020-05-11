package com.openforce.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.activity.ChatActivity;
import com.openforce.adapters.ConversationAdapter;
import com.openforce.model.Conversation;
import com.openforce.model.ConversationsResponse;
import com.openforce.utils.ApiClient;
import com.openforce.widget.EndlessRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";

    private static final int REQUEST_CHAT_ACTIVITY = 1000;

    private static final int ITEM_TO_LOAD = 10;

    private EndlessRecyclerView listMessages;
    private ProgressBar progressMessages;

    private ConversationAdapter adapter;
    private List<Conversation> conversations = new ArrayList<>();

    private DocumentSnapshot lastDocSnap = null;
    private ApiClient apiClient;
    private boolean loadingMessages = false;
    private boolean hasMoreMessages = true;

    private HashMap<String, String> employerProfileImageMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = OpenForceApplication.getApiClient();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadConversations();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            listMessages.setVisibility(View.GONE);
            progressMessages.setVisibility(View.VISIBLE);
            lastDocSnap = null;
            adapter.clearConversations();
            loadConversations();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHAT_ACTIVITY && resultCode == RESULT_OK) {
            Conversation conversation = data.getParcelableExtra(ChatActivity.EXTRA_RESULT_CONVERSATION);
            int indexConversation = -1;
            List<Conversation> conversationList = adapter.getConversations();
            for (int i = 0; i < conversationList.size(); i++) {
                if (conversation.id.equals(conversationList.get(i).id)) {
                    indexConversation = i;
                    break;
                }
            }
            if (indexConversation != -1) {
                conversationList.set(indexConversation, conversation);
                adapter.setConversations(conversationList);
            }
        }
    }

    private void loadConversations() {
        loadingMessages = true;
        apiClient.getConversations(getActivity(), lastDocSnap, new OnSuccessListener<ConversationsResponse>() {
            @Override
            public void onSuccess(ConversationsResponse conversationsResponse) {
//            Log.d(TAG, "loadConversations: size" + conversationsResponse.getConversations().size());
//            Log.d(TAG, "loadConversations: " + conversationsResponse.getConversations().get(0).lastMessage.body.type);
                loadingMessages = false;
                progressMessages.setVisibility(View.GONE);
                listMessages.setVisibility(View.VISIBLE);
                lastDocSnap = conversationsResponse.getLastConversationDocSnap();
                ChatsFragment.this.retrieveProfileImageForConversations(conversationsResponse.getConversations());
                adapter.addConversations(conversationsResponse.getConversations());
                if (conversationsResponse.getConversations() == null || conversationsResponse.getConversations().isEmpty() || conversationsResponse.getConversations().size() % ITEM_TO_LOAD != 0) {
                    hasMoreMessages = false;
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingMessages = false;
                progressMessages.setVisibility(View.GONE);
                listMessages.setVisibility(View.GONE);
                Log.e(TAG, "Cannot load conversations list", e);
                Snackbar.make(ChatsFragment.this.getView(), R.string.cannot_load_conversations, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void retrieveProfileImageForConversations(List<Conversation> conversations) {
        final int[] counter = {conversations.size()};
        for (final Conversation conversation : conversations) {
            if (employerProfileImageMap.containsKey(conversation.employerId)) {
                conversation.employerPublicIdProfileImage = employerProfileImageMap.get(conversation.employerId);
                counter[0]--;
                if (counter[0] == 0) {
                    adapter.addPublicIdProfileImageToConversations(employerProfileImageMap);
                }
                Log.e(TAG, "COUNTER IMAGE: " + counter[0]);
            } else {
                apiClient.getUserProfileImage(conversation.employerId, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String publicIdImageProfile) {
                        counter[0]--;
                        employerProfileImageMap.put(conversation.employerId, publicIdImageProfile);
                        if (counter[0] == 0) {
                            adapter.addPublicIdProfileImageToConversations(employerProfileImageMap);
                        }
                        Log.e(TAG, "COUNTER IMAGE: " + counter[0]);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading image for employer: " + conversation.employerName, e);
                        counter[0]--;
                        if (counter[0] == 0) {
                            adapter.addPublicIdProfileImageToConversations(employerProfileImageMap);
                        }
                        Log.e(TAG, "COUNTER IMAGE: " + counter[0]);
                    }
                }, getActivity());
            }
        }
    }

    private void initView(View view) {
        listMessages = view.findViewById(R.id.list_messages);
        progressMessages = view.findViewById(R.id.progress_messages);

        progressMessages.setVisibility(View.VISIBLE);
        listMessages.setVisibility(View.INVISIBLE);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        listMessages.setLayoutManager(linearLayoutManager);
        adapter = new ConversationAdapter(conversations);
        listMessages.setAdapter(adapter);
        adapter.setItemClickListener(new ConversationAdapter.ConversationItemClickListener() {
            @Override
            public void onItemClick(Conversation conversation, int position) {
                ChatsFragment.this.startActivityForResult(ChatActivity.getIntent(ChatsFragment.this.getActivity(), conversation), REQUEST_CHAT_ACTIVITY);
            }
        });

        listMessages.setProgressView(R.layout.custom_loading_list_item);
        listMessages.setThreshold(1);
        listMessages.setPager(new EndlessRecyclerView.Pager() {
            @Override
            public boolean shouldLoad() {
                return hasMoreMessages;
            }

            @Override
            public boolean isLoading() {
                return loadingMessages;
            }

            @Override
            public void loadNextPage() {
                loadConversations();
            }
        });
    }
}
