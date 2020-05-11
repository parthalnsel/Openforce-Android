package com.openforce.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.adapters.ChatAdapter;
import com.openforce.db.Tables;
import com.openforce.model.Conversation;
import com.openforce.model.Message;
import com.openforce.model.MessagesResponse;
import com.openforce.utils.RoundedTransformation;
import com.openforce.utils.UIUtils;
import com.openforce.utils.Utils;
import com.openforce.widget.EndlessRecyclerView;
import com.openforce.widget.TextDrawable;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatActivity extends BaseActivity {

    private static final String EXTRA_CHAT_INFO = "EXTRA_CHAT_INFO";

    private static final String TAG = "ChatActivity";

    private static final int ITEM_TO_LOAD = 10;

    public static final String EXTRA_RESULT_CONVERSATION = "EXTRA_RESULT_CONVERSATION_EDITED";

    private LinearLayout headerChat;
    private ImageView logoEmployer;
    private LinearLayout footerLayout;
    private EndlessRecyclerView messageList;
    private ImageView endJobButton;
    private ImageView checkinButton;
    private ImageView backButton;
    private TextView employerName;
    private ProgressBar loader;
    private ViewGroup contentContainer;
    private RatingBar ratingProgressBar;
    private TextView rateTitleAbove;
    private TextView rateTitleBelow;
    private ViewGroup rateLayout , request_payment_layout;
    private Button leaveReviewButton , requestWithdrawButton;

    private ChatAdapter chatAdapter;
    private Conversation conversation;

    private DocumentSnapshot lastDocumentSnap = null;

    private ListenerRegistration newMessagesListener = null;

    private boolean loadingMessages = false;
    private boolean hasMoreMessages = true;

    private boolean isConversationEdited = false;

    boolean isPaymentDone = false;

    private FirebaseFirestore firebaseFirestore;
    String status ="";
    public static Intent getIntent(Context context, Conversation conversation) {
        Intent intent = new Intent(context, ChatActivity.class);
        // add your extras here
        intent.putExtra(EXTRA_CHAT_INFO, conversation);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        conversation = getIntent().getParcelableExtra(EXTRA_CHAT_INFO);
        System.out.println("The Id is: " + conversation.id);
        firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference reference = firebaseFirestore.collection("jobs")
                .document(conversation.jobId)
                .collection("payment")
                .document(firebaseAuth.getCurrentUser().getUid());
        ;
        reference
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        status = document.getString("status");


                        if (conversation.ended != null &&conversation.ended && status != null){
                            if ( status.equals("accepted")){
                                isPaymentDone = true;
                                System.out.println("Status Accept Partha");
                                request_payment_layout.setVisibility(View.GONE);
                                rateLayout.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                });




        initView();
        bindView();

        loader.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);

        getMessagesOfConversation(true);
        if (conversation.unreadNotificationEmployee != 0) {
            apiClient.setNotificatiosnRead(conversation.id);
            conversation.unreadNotificationEmployee = 0;
            isConversationEdited = true;
        }

        setEmployerImage();
    }

    private void getMessagesOfConversation(final boolean shouldStartListeningForMessages) {
        loadingMessages = true;
        messageList.setRefreshing(true);
        apiClient.getMessagesOfConversation(conversation.id, this, lastDocumentSnap, ITEM_TO_LOAD, new OnSuccessListener<MessagesResponse>() {
            @Override
            public void onSuccess(MessagesResponse messagesResponse) {
                loader.setVisibility(View.GONE);
                contentContainer.setVisibility(View.VISIBLE);
                lastDocumentSnap = messagesResponse.getLastMessageDocSnap();
                chatAdapter.addMessages(messagesResponse.getMessages());
                if (shouldStartListeningForMessages) {
                    ChatActivity.this.startListeningForMessages();
                }
                loadingMessages = false;
                messageList.setRefreshing(false);
                if (messagesResponse.getMessages() == null || messagesResponse.getMessages().isEmpty() || messagesResponse.getMessages().size() % ITEM_TO_LOAD != 0) {
                    hasMoreMessages = false;
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loader.setVisibility(View.GONE);
                if (e instanceof FirebaseFunctionsException) {
                    Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(root, R.string.error_loading_messages, Snackbar.LENGTH_LONG).show();
                }
                Log.e(TAG, "ERROR loading messages", e);
                messageList.setRefreshing(false);
                loadingMessages = false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!chatAdapter.getMessageList().isEmpty()) {
            startListeningForMessages();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListeningForMessages();
    }

    private void startListeningForMessages() {
        FirebaseFirestore firebaseFirestore = OpenForceApplication.getInstance().getFirebaseFirestore();
        newMessagesListener = firebaseFirestore.collection(Tables.TABLE_CHATS).document(conversation.id).collection(Tables.TABLE_MESSAGES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    if (!ChatActivity.this.containsMessage(dc.getDocument().getId())) {
                                        Message message = dc.getDocument().toObject(Message.class);
                                        message.id = dc.getDocument().getId();
                                        chatAdapter.addMessage(message);
                                        conversation.lastMessage = message;
                                        isConversationEdited = true;
                                    }
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified message: " + dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed message: " + dc.getDocument().getData());
                                    break;
                            }
                        }
                    }
                });
    }

    private void stopListeningForMessages() {
        if (newMessagesListener != null) {
            newMessagesListener.remove();
        }
    }

    private boolean containsMessage(String messageId) {
        for (Message message : chatAdapter.getMessageList()) {
            if (message.id.equals(messageId)) {
                return true;
            }
        }
        return false;
    }

    private void initView() {
        headerChat = findViewById(R.id.header_chat);
        logoEmployer = findViewById(R.id.logo_employer);
        footerLayout = findViewById(R.id.footer_layout);
        messageList = findViewById(R.id.message_list);
        endJobButton = findViewById(R.id.end_job_button);
        checkinButton = findViewById(R.id.checkin_button);
        backButton = findViewById(R.id.back_button);
        employerName = findViewById(R.id.employer_name);
        loader = findViewById(R.id.loader_chat);
        contentContainer = findViewById(R.id.content_layout);
        ratingProgressBar = findViewById(R.id.rating_bar);
        rateTitleAbove = findViewById(R.id.title_rate);
        rateTitleBelow = findViewById(R.id.rate_card_title);

        request_payment_layout = findViewById(R.id.request_amount_layout);
        requestWithdrawButton = findViewById(R.id.request_withdraw_button);

        rateLayout = findViewById(R.id.review_layout);
        leaveReviewButton = findViewById(R.id.review_button);
    }

    private void bindView() {
        System.out.println("Status: " + status);
//        rateLayout.setVisibility(View.GONE);
        endJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = Utils.getConfirmDialog(ChatActivity.this.getString(R.string.end_job), ChatActivity.this.getString(R.string.end_job_message_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChatActivity.this.callEndJob();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do here;
                    }
                }, ChatActivity.this);
                alertDialog.show();
            }
        });
        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = Utils.getConfirmDialog(ChatActivity.this.getString(R.string.check_in), ChatActivity.this.getString(R.string.check_in_message_dialog),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ChatActivity.this.callCheckIn();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing to do here;
                            }
                        }, ChatActivity.this);
                alertDialog.show();
            }
        });
        boolean isJobEnded = conversation.ended != null ? conversation.ended : false;
        endJobButton.setEnabled(!isJobEnded);
        if (isJobEnded) {
            checkinButton.setEnabled(false);
        } else {
            if (conversation.lastCheckin != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(conversation.lastCheckin);
                checkinButton.setEnabled(!DateUtils.isToday(conversation.lastCheckin));
            } else {
                checkinButton.setEnabled(true);
            }
        }
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.this.onBackPressed();
            }
        });
        employerName.setText(conversation.employerName);

        chatAdapter = new ChatAdapter(new ArrayList<Message>());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        linearLayoutManager.setReverseLayout(true);
        messageList.setLayoutManager(linearLayoutManager);
        messageList.setAdapter(chatAdapter);

        messageList.setProgressView(R.layout.custom_loading_list_item);
        messageList.setThreshold(1);
        messageList.setPager(new EndlessRecyclerView.Pager() {
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
                getMessagesOfConversation(false);
            }
        });


        rateTitleAbove.setText(getString(R.string.rate_time, conversation.employerName));
        rateTitleBelow.setText(getString(R.string.review_title, conversation.employerName));
        leaveReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ratingProgressBar.getRating() == 0) {
                    Snackbar.make(root, R.string.select_rating_error, Snackbar.LENGTH_LONG).show();
                    return;
                }

                final ProgressDialog progressDialog = UIUtils.showProgress(ChatActivity.this, ChatActivity.this.getString(R.string.loading), null, true, false, null);
                apiClient.rateEmployer(ChatActivity.this, conversation.jobId, ratingProgressBar.getRating(), conversation.employerId, new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult __) {
                        progressDialog.dismiss();
                        Snackbar.make(root, R.string.job_reviewed_message, Snackbar.LENGTH_LONG).show();
                        rateLayout.setVisibility(View.GONE);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error leaving review to employer", e);
                        progressDialog.dismiss();
                        if (e instanceof FirebaseFunctionsException) {
                            Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(root, R.string.error_try_again, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        requestWithdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("JobId is: " + conversation.jobId);
                final ProgressDialog progressDialog = UIUtils.showProgress(ChatActivity.this, ChatActivity.this.getString(R.string.loading), null, true, false, null);
                apiClient.requestPayment(conversation.jobId, ChatActivity.this, new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        progressDialog.dismiss();
                        System.out.println(httpsCallableResult.getData().toString());
                        Snackbar.make(root, "Success", Snackbar.LENGTH_LONG).show();
                        request_payment_layout.setVisibility(View.GONE);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error: ", e);
                        progressDialog.dismiss();
                        Snackbar.make(root, R.string.error_try_again, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });


        if (conversation.ended != null && conversation.ended && !isPaymentDone ) {
            apiClient.shouldReviewEmployer(this, conversation.jobId, new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean shouldReviewEmployer) {
                    if (shouldReviewEmployer) {
//                    rateLayout.setVisibility(View.VISIBLE);
                        request_payment_layout.setVisibility(View.VISIBLE);
//                    rateLayout.setVisibility(View.GONE);
                    } else {
//                    rateLayout.setVisibility(View.GONE);
                        request_payment_layout.setVisibility(View.GONE);
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error on shouldReviewEmployer", e);
                    // don't do anything if it fails
                }
            });
        }else if (conversation.ended != null && conversation.ended && status.equals("accepted")){
            System.out.println("Status Accept Partha");
            request_payment_layout.setVisibility(View.GONE);
            rateLayout.setVisibility(View.VISIBLE);
        }

        if (isPaymentDone){
            rateLayout.setVisibility(View.VISIBLE);
            request_payment_layout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(isConversationEdited) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT_CONVERSATION, conversation);
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }

    private void callCheckIn() {
        final ProgressDialog progressDialog = UIUtils.showProgress(this, null, getString(R.string.checking_in), true, false, null);
        progressDialog.show();
        apiClient.checkin(conversation.jobId, Calendar.getInstance().getTimeInMillis(), this, new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                checkinButton.setEnabled(false);
                progressDialog.dismiss();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e(TAG, "Error checking job", e);
                if (e instanceof FirebaseFunctionsException) {
                    Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(root, "There has been an error checking in", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void callEndJob() {
        final ProgressDialog progressDialog = UIUtils.showProgress(this, null, getString(R.string.ending_job), true, false, null);
        progressDialog.show();
        apiClient.endJob(conversation.jobId, FirebaseAuth.getInstance().getUid(), this, new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                progressDialog.dismiss();
                endJobButton.setEnabled(false);
                checkinButton.setEnabled(false);
                rateLayout.setVisibility(View.GONE);
                request_payment_layout.setVisibility(View.VISIBLE);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e(TAG, "Error ending job", e);
                if (e instanceof FirebaseFunctionsException) {
                    Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(root, "There has been an error ending the job", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setEmployerImage() {
        final TextDrawable placeholderEmployer = Utils.getPlaceholderForProfile(conversation.employerName, this);
        logoEmployer.setImageDrawable(placeholderEmployer);

        if (!TextUtils.isEmpty(conversation.employerPublicIdProfileImage)) {
            setEmployerImageWhenPublicIdAvailable(placeholderEmployer, conversation.employerPublicIdProfileImage);
        } else {
            apiClient.getUserProfileImage(conversation.employerId, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String publicIdProfileImage) {
                    ChatActivity.this.setEmployerImageWhenPublicIdAvailable(placeholderEmployer, publicIdProfileImage);
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // nothing to do here
                }
            }, this);
        }
    }

    private void setEmployerImageWhenPublicIdAvailable(final Drawable placeholderEmployer, String publicIdProfileImage) {
        Transformation transformation = new Transformation();
        transformation.gravity("face");
        Url baseUrl = MediaManager.get().url().publicId(publicIdProfileImage).transformation(transformation).format("jpg").type("image");
        MediaManager.get().responsiveUrl(logoEmployer, baseUrl,
                ResponsiveUrl.Preset.AUTO_FILL, new ResponsiveUrl.Callback() {
                    @Override
                    public void onUrlReady(Url url) {
                        String urlGenerated = url.generate();
                        Picasso.get().load(urlGenerated)
                                .transform(new RoundedTransformation(ChatActivity.this.getResources().getDimensionPixelSize(R.dimen.avatar_employee_size), 0))
                                .placeholder(placeholderEmployer)
                                .into(logoEmployer);
                    }
                });
    }
}
