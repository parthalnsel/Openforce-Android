package com.openforce.utils;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.openforce.activity.AddCvDocActivity;
import com.openforce.model.StripeInfo;
import com.openforce.model.User;
import com.openforce.db.Fields;
import com.openforce.db.Tables;
import com.openforce.interfaces.OnLoginCallback;
import com.openforce.model.BoundingBoxCoordinate;
import com.openforce.model.Conversation;
import com.openforce.model.ConversationsResponse;
import com.openforce.model.CurrentJobResponse;
import com.openforce.model.JobsResponse;
import com.openforce.model.Message;
import com.openforce.model.MessagesResponse;
import com.openforce.model.PastJobResponse;
import com.openforce.model.Reference;
import com.openforce.model.Skill;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public final class ApiClient{

    private static final String TAG = "ApiClient";

    private static final String SAVE_PIN_FUNCTION = "savePin";
    private static final String GET_EMPLOYEE_USER_INFO = "getEmployeeUser";
    private static final String GET_JOBS = "getJobsByLocation";
    private static final String APPLY_JOB = "applyJob";
    private static final String REQUEST_PAYMENT = "paymentRequest";
    private static final String WITHDRAW_APPLICATION = "withdrawApplication";
    private static final String END_JOB = "endJob";
    private static final String CHECK_IN = "checkin";
    private static final String GET_PAST_JOB = "getPastJobForUser";
    private static final String GET_CURRENT_JOB = "getCurrentJob";
    private static final String RATE_EMPLOYER = "rateEmployer";
    private static final String USER_PROFILE_IMAGE = "getProfileImageOfUser";
    private static final String SHOULD_LEAVE_REVIEW_TO_EMPLOYER = "shouldLeaveReviewToEmployer";
    private static final String CV_UPLOAD_DOC = "uploadCvImage";

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseFunctions firebaseFunctions;
    private Gson gson;

    public ApiClient(FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore,
                     FirebaseFunctions firebaseFunctions, Gson gson) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
        this.firebaseFunctions = firebaseFunctions;
        this.gson = gson;
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public void signInWithEmailAndPassword(String email, String password,
                                           final OnLoginCallback loginCallback) {

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                ApiClient.this.getCurrentUserInfo(new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        User user = null;
                        try {
                            user = User.fromJSON(new JSONObject(httpsCallableResult.getData() + ""), gson);
                            loginCallback.onSuccessfulLogin(user);
                        } catch (JSONException e) {
                            firebaseAuth.signOut();
                            loginCallback.onErrorLogin(e);
                        }

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        firebaseAuth.signOut();
                        loginCallback.onErrorLogin(e);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loginCallback.onErrorLogin(e);
            }
        });
    }

    public void getCurrentUserInfo(OnSuccessListener<HttpsCallableResult> successListener,
                                   OnFailureListener failureListener) {
        firebaseFunctions
                .getHttpsCallable(GET_EMPLOYEE_USER_INFO)
                .call()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void setUserReference(List<Reference> references, OnSuccessListener<Void> successListener,
                          OnFailureListener failureListener) {
        if (firebaseAuth.getCurrentUser() != null) {
            Map<String, Object> updateMap = new HashMap<>();
            Map<String, Object> anotherMap = new HashMap<>();
            for (int i = 0; i < references.size(); i++) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("name", references.get(i).name);
                objectMap.put("email", references.get(i).email);
                anotherMap.put(i + "", objectMap);
            }

            updateMap.put(Fields.FIELD_USER_REFERENCE, anotherMap);
            firebaseFirestore.collection(Tables.TABLE_USER).document(firebaseAuth.getCurrentUser().getUid())
                    .collection(Tables.TABLE_USER_REFERENCES)
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .set(updateMap)
                    .addOnSuccessListener(successListener).addOnFailureListener(failureListener);
        }
    }


    public void setStripeInfo(StripeInfo info, OnSuccessListener<Void> successListener,
                              OnFailureListener failureListener) {
        if (firebaseAuth.getCurrentUser() != null) {
            Map<String, Object> updateMap = new HashMap<>();
            Map<String, Object> anotherMap = new HashMap<>();
//            for (int i = 0; i < stripeInfos.size(); i++) {
//                Map<String, Object> objectMap = new HashMap<>();
//                objectMap.put("name", stripeInfos.get(i).id);
//                objectMap.put("user_id", stripeInfos.get(i).user_id);
//                objectMap.put("token", stripeInfos.get(i).access_token);
//                anotherMap.put(i + "", objectMap);
//            }

            updateMap.put(Fields.FIELD_USER_PAYMENT_INFO, anotherMap);
            firebaseFirestore.collection(Tables.TABLE_USER).document(firebaseAuth.getCurrentUser().getUid())
                    .collection(Tables.STRIPE_INFO)
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .set(info)
                    .addOnSuccessListener(successListener).addOnFailureListener(failureListener);
        }
    }


    public void saveUserPin(String pin, OnSuccessListener<HttpsCallableResult> onSuccessListener, OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("pin", pin);

        firebaseFunctions
                .getHttpsCallable(SAVE_PIN_FUNCTION)
                .call(data)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(failureListener);
    }

    //****************************  Doc Image Changes *************************//


    public void saveDocImage (String pin, OnSuccessListener<HttpsCallableResult> onSuccessListener, OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("image", pin);

        firebaseFunctions
                .getHttpsCallable("uploadProfileImage")
                .call(data)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(failureListener);
    }

    //************************** Doc Image ****************************//

    public Task<QuerySnapshot> getListOfRoles(OnSuccessListener<QuerySnapshot> successListener, OnFailureListener onFailureListener) {
        return firebaseFirestore.collection(Tables.TABLE_ROLES)
                .orderBy("name", Query.Direction.ASCENDING)
                .get().addOnSuccessListener(successListener)
                .addOnFailureListener(onFailureListener);
    }

    public void saveUserSkill(Skill skill, OnSuccessListener<Void> successListener, OnFailureListener onFailureListener) {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore.collection(Tables.TABLE_USER).document(firebaseAuth.getCurrentUser().getUid())
                    .collection("skills").document(skill.getId()).set(skill)
                    .addOnSuccessListener(successListener).addOnFailureListener(onFailureListener);
        }
    }

    public void saveFirebaseMessagginToken(String token, String uid, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        Map<String, Object> update = new HashMap<>();
        update.put("firebaseToken", token);
        firebaseFirestore.collection(Tables.TABLE_USER).document(uid)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(successListener).addOnFailureListener(failureListener);
    }

    public void getMapJobs(BoundingBoxCoordinate boundingBoxCoordinate, long unixStartDate,
                           final OnSuccessListener<JobsResponse> successListener,
                           OnFailureListener onFailureListener, Activity activity) {

        Map<String, Object> data = new HashMap<>();
        data.put("maxLatitude", boundingBoxCoordinate.getBottomRight().latitude);
        data.put("minLatitude", boundingBoxCoordinate.getBottomLeft().latitude);
        data.put("minLongitude", boundingBoxCoordinate.getBottomLeft().longitude);
        data.put("maxLongitude", boundingBoxCoordinate.getTopLeft().longitude);
        data.put("startDate", "1580428800000"); // unixStartDate);

        firebaseFunctions
                .getHttpsCallable(GET_JOBS)
                .call(data)
                .addOnFailureListener(activity, onFailureListener)
                .addOnSuccessListener(activity, new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        Log.e("AAAA", httpsCallableResult.getData() + "");
                        JobsResponse response = gson.fromJson(httpsCallableResult.getData() + "", JobsResponse.class);
                        Log.e("AAAA", response.toString());
                        successListener.onSuccess(response);
                    }
                });
    }

    public void applyJob(String jobId, Activity activity, OnSuccessListener<HttpsCallableResult> onSuccessListener,
                         OnFailureListener failureListener) {

        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);

        firebaseFunctions.getHttpsCallable(APPLY_JOB)
                .call(data)
                .addOnSuccessListener(activity, onSuccessListener)
                .addOnFailureListener(activity, failureListener);
    }

    public void requestPayment(String jobId, Activity activity, OnSuccessListener<HttpsCallableResult> onSuccessListener,
                         OnFailureListener failureListener) {

        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);

        firebaseFunctions.getHttpsCallable(REQUEST_PAYMENT)
                .call(data)
                .addOnSuccessListener(activity, onSuccessListener)
                .addOnFailureListener(activity, failureListener);
    }

    public void isUserAlreadyAppliedForJob(String userId, String jobId, Activity activity,
                                           OnSuccessListener<DocumentSnapshot> onSuccessListener,
                                           OnFailureListener onFailureListener) {
        firebaseFirestore.collection(Tables.TABLE_USER)
                .document(userId)
                .collection(Tables.TABLE_JOBS)
                .document(jobId)
                .get()
                .addOnSuccessListener(activity, onSuccessListener)
                .addOnFailureListener(activity, onFailureListener);
    }

    public void withdrawApplication(String jobId, Activity activity,
                                    OnSuccessListener<HttpsCallableResult> onSuccessListener,
                                    OnFailureListener onFailureListener) {

        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);

        firebaseFunctions.getHttpsCallable(WITHDRAW_APPLICATION)
                .call(data)
                .addOnSuccessListener(activity, onSuccessListener)
                .addOnFailureListener(activity, onFailureListener);

    }

    public void getConversations(Activity activity, @Nullable DocumentSnapshot lastDocSnap,
                                 final OnSuccessListener<ConversationsResponse> onSuccessListener,
                                 OnFailureListener onFailureListener) {

        Query query = firebaseFirestore.collection(Tables.TABLE_CHATS)
//                .whereEqualTo(FieldPath.of("employeeId"), FirebaseAuth.getInstance().getCurrentUser().getUid())
                .whereEqualTo("employeeId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderBy("lastUpdateTime", Query.Direction.DESCENDING)
                .limit(20);

        if (lastDocSnap != null) {
            query = query.startAfter(lastDocSnap);
        }

        query.get()
                .addOnSuccessListener(activity, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Conversation> response = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            Conversation conversation = documentSnapshot.toObject(Conversation.class);
                            if (conversation != null) {
                                conversation.id = documentSnapshot.getId();
                                response.add(conversation);
                            }
                        }
                        DocumentSnapshot documentSnapshot = null;
                        if (!queryDocumentSnapshots.isEmpty()) {
                            documentSnapshot = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.getDocuments().size() - 1);
                        }
                        ConversationsResponse conversationsResponse = new ConversationsResponse(documentSnapshot, response);
                        onSuccessListener.onSuccess(conversationsResponse);
                    }
                })
                .addOnFailureListener(activity, onFailureListener);
    }

    public void endJob(String jobId, String employeeId, Activity activity,
                       OnSuccessListener<HttpsCallableResult> onSuccessListener,
                       OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        data.put("employeeId", employeeId);

        firebaseFunctions.getHttpsCallable(END_JOB)
                .call(data)
                .addOnSuccessListener(activity, onSuccessListener)
                .addOnFailureListener(activity, failureListener);
    }

    public void checkin(String jobId, Long checkinTimestamp, Activity activity,
                        OnSuccessListener<HttpsCallableResult> onSuccessListener,
                        OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        data.put("checkinTimestamp", checkinTimestamp);

        firebaseFunctions.getHttpsCallable(CHECK_IN)
                .call(data)
                .addOnSuccessListener(activity, onSuccessListener)
                .addOnFailureListener(activity, failureListener);
    }

    public void getMessagesOfConversation(String chatId, Activity activity, @Nullable DocumentSnapshot lastDocSnap, int limitTo, final OnSuccessListener<MessagesResponse> onSuccessListener,
                                 OnFailureListener onFailureListener) {

        Query query = firebaseFirestore.collection(Tables.TABLE_CHATS).document(chatId).collection(Tables.TABLE_MESSAGES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limitTo);

//        System.out.println("ParthaQuery" + query.get().getResult().size());

        if (lastDocSnap != null) {
            query = query.startAfter(lastDocSnap);
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document.getId());
                    }
                    Log.d(TAG, list.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error getting documents: ");
            }
        });

        query.get()
                .addOnSuccessListener(activity, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        System.out.println("Partha Success");
                        List<Message> response = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            Message message = documentSnapshot.toObject(Message.class);
                            System.out.println("Partha: " + message.body);
                            message.id = documentSnapshot.getId();
                            response.add(message);
                        }
                        DocumentSnapshot documentSnapshot = null;
                        if (!queryDocumentSnapshots.isEmpty()) {
                            documentSnapshot = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.getDocuments().size() - 1);
                        }
                        MessagesResponse conversationsResponse = new MessagesResponse(documentSnapshot, response);
                        onSuccessListener.onSuccess(conversationsResponse);
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Partha Failure");
                    }
                });
    }

    public void getPastJob(Activity activity, final OnSuccessListener<PastJobResponse> onSuccessListener,
                           OnFailureListener failureListener) {

            firebaseFunctions.getHttpsCallable(GET_PAST_JOB)
                .call()
                .addOnFailureListener(activity, failureListener)
                .addOnSuccessListener(activity, new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        PastJobResponse response = gson.fromJson(httpsCallableResult.getData() + "", PastJobResponse.class);
                        onSuccessListener.onSuccess(response);


                    }
                });
    }

    public void getCurrentJob(final OnSuccessListener<CurrentJobResponse> onSuccessListener,
                              OnFailureListener failureListener) {
        firebaseFunctions.getHttpsCallable(GET_CURRENT_JOB)
                .call()
                .addOnFailureListener(failureListener)
                .addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        CurrentJobResponse response = gson.fromJson(httpsCallableResult.getData() + "", CurrentJobResponse.class);
                        onSuccessListener.onSuccess(response);
                    }
                });
    }

    public void shouldReviewEmployer(Activity activity, String jobId, final OnSuccessListener<Boolean> onSuccessListener,
                                     final OnFailureListener failureListener) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        firebaseFunctions.getHttpsCallable(SHOULD_LEAVE_REVIEW_TO_EMPLOYER)
                .call(data)
                .addOnFailureListener(activity, failureListener)
                .addOnSuccessListener(activity, new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        JSONObject object;
                        try {
                            object = new JSONObject(httpsCallableResult.getData() + "");
                            onSuccessListener.onSuccess(object.optBoolean("shouldReview", false));
                        } catch (JSONException e) {
                            failureListener.onFailure(e);
                        }
                    }
                });
    }

    public void rateEmployer(Activity activity, String jobId, float rating, String employerId, OnSuccessListener<HttpsCallableResult> onSuccessListener,
                                     OnFailureListener failureListener) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        data.put("review", rating);
        data.put("employerId", employerId);

        firebaseFunctions.getHttpsCallable(RATE_EMPLOYER)
                .call(data)
                .addOnFailureListener(activity, failureListener)
                .addOnSuccessListener(activity, onSuccessListener);
    }

    public void resetPassword(Activity activity, String email,
                              OnSuccessListener<Void> onSuccessListener,
                              OnFailureListener failureListener) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(activity, onSuccessListener)
                .addOnFailureListener(activity, failureListener);
    }


    public void saveProfileImage(String url, String imagePublicId, OnSuccessListener<Void> successListener, OnFailureListener onFailureListener) {
        if (firebaseAuth.getCurrentUser() != null) {
            HashMap<String, String> map = new HashMap<>();
            map.put("profileImg", url);
            map.put("imagePublicId", imagePublicId);
            firebaseFirestore.collection(Tables.TABLE_USER).document(firebaseAuth.getCurrentUser().getUid())
                    .set(map, SetOptions.merge())
                    .addOnSuccessListener(successListener).addOnFailureListener(onFailureListener);
        }
    }

    public void setNotificatiosnRead(String chatId) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("unreadNotificationEmployee", 0);
        firebaseFirestore.collection(Tables.TABLE_CHATS).document(chatId)
                .set(map, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Notification unread count reset to 0");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error resetting unread count", e);
            }
        });
    }

    public Task<HttpsCallableResult> getUserProfileImage(String uid, final OnSuccessListener<String> onSuccessListener,
                                                         final OnFailureListener onFailureListener, Activity activity) {
        HashMap<String, String> map = new HashMap<>();
        map.put("uid", uid);

        OnSuccessListener<HttpsCallableResult> successListener = new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                try {
                    JSONObject jsonObject = new JSONObject(httpsCallableResult.getData() + "");
                    onSuccessListener.onSuccess(jsonObject.optString("publicId", ""));
                } catch (JSONException e) {
                    onFailureListener.onFailure(e);
                }
            }
        };

        if (activity != null) {
            return firebaseFunctions.getHttpsCallable(USER_PROFILE_IMAGE)
                    .call(map)
                    .addOnFailureListener(activity, onFailureListener)
                    .addOnSuccessListener(activity, successListener);
        } else {
            return firebaseFunctions.getHttpsCallable(USER_PROFILE_IMAGE)
                    .call(map)
                    .addOnFailureListener(onFailureListener)
                    .addOnSuccessListener(successListener);
        }
    }

    public void cvEmploye(String image, OnSuccessListener<HttpsCallableResult> onSuccessListener,
                          OnFailureListener failureListener) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("image", image);
        firebaseFunctions.getHttpsCallable(CV_UPLOAD_DOC)
                .call(data)
                .addOnFailureListener( failureListener)
                .addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {

                        System.out.println("ProofileUpload***" + httpsCallableResult.getData());
                        JSONObject cvObj = null;
                        try {
                            cvObj = new JSONObject(httpsCallableResult.getData().toString());
                            String cvImgUrl = cvObj.optString("cvImageUrl");

                            System.out.println("ProofileUploadedCV***" + cvImgUrl);
                            ConstantUtil.CvImage = cvImgUrl;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        AddCvDocActivity.Mymethod();
                        //                    Log.d(TAG, "ProfileUpload: " + httpsCallableResult.getData().toString());
                   /* JSONObject object;
                    try {
                        object = new JSONObject(httpsCallableResult.getData() + "");

                    } catch (JSONException e) {
                        failureListener.onFailure(e);
                    }*/
                    }
                });
    }

    public void signOut(){
        firebaseAuth.signOut();
    }
}
