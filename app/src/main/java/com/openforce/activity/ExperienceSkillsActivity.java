package com.openforce.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Predicate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.adapters.RoleAdapter;
import com.openforce.listeners.SimpleTextChangeListener;
import com.openforce.model.Role;
import com.openforce.model.Skill;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.UIUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ir.neo.stepbarview.StepBarView;

public class ExperienceSkillsActivity extends BaseActivity {

    private static final String TAG = "ExperienceSkillsActivit";

    private static final int STARTING_POINT_SKILL_LEVEL = 6;
    private static final float MINIMUM_WAGE = 7.83f;

    private EditText searchEditText;
    private ImageView closeScreen;
    private RecyclerView rolesList;
    private ViewGroup rolesListContainer;
    private TextView pickingRoleLabel;
    private TextView mostWantedJobsLabel;
    private RoleAdapter roleAdapter;
    private List<Role> listRoles = new ArrayList<>();
    private LinearLayout addExperienceLayout;
    private TextView roleName;
    private TextView currencySymbol;
    private TextView dayRateLayout;
    private StepBarView progressBar;
    private Button doneButton;
    private Role currentlySelectedRole;
    private EditText dayRateEdittext;

    private String previousSearch;

    private SecureSharedPreference secureSharedPreference;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, ExperienceSkillsActivity.class);
        // add your extras here
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiences_skills);
        initView();

        secureSharedPreference = OpenForceApplication.getInstance().getSecureSharedPreference();
        closeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                ExperienceSkillsActivity.this.finish();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rolesList.setLayoutManager(linearLayoutManager);
        roleAdapter = new RoleAdapter(listRoles);
        roleAdapter.setItemClickListener(new RoleAdapter.RoleItemClickListener() {
            @Override
            public void onItemClick(Role role, int position) {
                currentlySelectedRole = role;
                // Show/redirect to set skill level
                previousSearch = searchEditText.getText().toString();
                searchEditText.setText(role.getName());
                searchEditText.setEnabled(false);
                rolesListContainer.setVisibility(View.GONE);

                roleName.setText(role.getName());
                addExperienceLayout.setVisibility(View.VISIBLE);
                User user = secureSharedPreference.getUserInfo();
                ExperienceSkillsActivity.this.setupProgressBar();
                if (user.skills != null) {
                    Stream.of(user.skills)
                            .filter(new Predicate<Skill>() {
                                @Override
                                public boolean test(Skill value) {
                                    return value.getId().equals(currentlySelectedRole.getId());
                                }
                            })
                            .findFirst().ifPresentOrElse(new Consumer<Skill>() {
                        @Override
                        public void accept(Skill skill) {
                            progressBar.setReachedStep(skill.getLevel());
                            dayRateEdittext.setText(skill.getPayRate());
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setReachedStep(STARTING_POINT_SKILL_LEVEL);
                            dayRateEdittext.setText("");
                        }
                    });
                } else {
                    progressBar.setReachedStep(STARTING_POINT_SKILL_LEVEL);
                    dayRateEdittext.setText("");
                }
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExperienceSkillsActivity.this.onDoneClicked();
            }
        });
        rolesList.setAdapter(roleAdapter);

        pickingRoleLabel.setVisibility(View.GONE);
        mostWantedJobsLabel.setVisibility(View.GONE);
        apiClient.getListOfRoles(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                listRoles.clear();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    boolean featured = documentSnapshot.getBoolean("featured") == null ? false : documentSnapshot.getBoolean("featured");
                    Role role = new Role(documentSnapshot.getString("name"), documentSnapshot.getId(), featured);
                    listRoles.add(role);
                }
                pickingRoleLabel.setVisibility(View.VISIBLE);
                mostWantedJobsLabel.setVisibility(View.VISIBLE);
                roleAdapter.setRoles(ExperienceSkillsActivity.this.sortByFeaturedAndName(listRoles));

            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(root, R.string.error_loading_roles, Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "Error loading list of roles", e);
            }
        });

        searchEditText.addTextChangedListener(new SimpleTextChangeListener() {
            @Override
            public void afterTextChanged(final Editable s) {
                if (TextUtils.isEmpty(s)) {
                    pickingRoleLabel.setVisibility(View.VISIBLE);
                    mostWantedJobsLabel.setVisibility(View.VISIBLE);
                    roleAdapter.setRoles(sortByFeaturedAndName(listRoles));
                } else {
                    pickingRoleLabel.setVisibility(View.GONE);
                    mostWantedJobsLabel.setVisibility(View.GONE);
                    roleAdapter.setRoles(Stream.of(listRoles).filter(new Predicate<Role>() {
                        @Override
                        public boolean test(Role role) {
                            return role.getName().toLowerCase().contains(s.toString().toLowerCase());
                        }
                    }).toList());
                }
            }
        });

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    UIUtils.hideKeyboard(root);
                    searchEditText.clearFocus();
                    return true;
                }
                return false;
            }
        });

        dayRateEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    UIUtils.hideKeyboard(root);
                }
                return false;
            }
        });
    }

    private List<Role> sortByFeaturedAndName(List<Role> roles) {
        List<Role> tempList = new ArrayList<>(roles);

        return Stream.of(tempList).sorted(new Comparator<Role>() {
            @Override
            public int compare(Role o1, Role o2) {
                if (o1.equals(o2)) {
                    return 0;
                }

                if (o1.isFeatured() && !o2.isFeatured()) {
                    return -1;
                }

                if (!o1.isFeatured() && o2.isFeatured()) {
                    return 1;
                }

                if ((o1.isFeatured() && o2.isFeatured()) || (!o1.isFeatured() && !o2.isFeatured())) {
                    return o1.getName().compareTo(o2.getName());
                }

                return 0;
            }
        }).toList();
    }

    private void onDoneClicked() {
        if (TextUtils.isEmpty(dayRateEdittext.getText())) {
            Snackbar.make(root, R.string.empty_day_rate, Snackbar.LENGTH_LONG).show();
            return;
        }

        int dayRate = Integer.valueOf(dayRateEdittext.getText().toString());
        if (MINIMUM_WAGE * 8 > dayRate) {
            Snackbar.make(root, R.string.below_minimum_wage, Snackbar.LENGTH_LONG).show();
            return;
        }

        final Skill skill = new Skill(currentlySelectedRole.getId(),
                currentlySelectedRole.getName(),
                dayRateEdittext.getText().toString(), progressBar.getReachedStep());
        final ProgressDialog progressDialog = UIUtils.showProgress(this, getString(R.string.saving),
                null, true, false, null);
        apiClient.saveUserSkill(skill, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void __) {
                searchEditText.setText("");
                searchEditText.setEnabled(true);
                rolesListContainer.setVisibility(View.VISIBLE);
                addExperienceLayout.setVisibility(View.GONE);
                User userInfo = secureSharedPreference.getUserInfo();
                if (userInfo.skills == null) {
                    List<Skill> skills = new ArrayList<>();
                    skills.add(skill);
                    userInfo.skills = skills;
                } else {
                    boolean found = false;
                    for (int i = 0; i < userInfo.skills.size(); i++) {
                        Skill skillObj = userInfo.skills.get(i);
                        if (skillObj.getId().equals(skill.getId())) {
                            userInfo.skills.set(i, skill);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        userInfo.skills.add(skill);
                    }
                }
                secureSharedPreference.setUserInfo(userInfo);
                progressDialog.dismiss();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception __) {
                // show error
                progressDialog.dismiss();
                Snackbar.make(root, R.string.error_saving_skill, Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void setupProgressBar() {
        progressBar.setAllowSelectStep(new StepBarView.AllowSelectStep() {
            @Override
            public boolean allowSelectStep(int __) {
                return true;
            }
        });
    }

    private void initView() {
        searchEditText = findViewById(R.id.search_edit_text);
        closeScreen = findViewById(R.id.close_screen);
        rolesList = findViewById(R.id.roles_list);
        addExperienceLayout = findViewById(R.id.add_experience_layout);
        roleName = findViewById(R.id.role_name);
        currencySymbol = findViewById(R.id.currency_symbol);
        progressBar = findViewById(R.id.progress_bar);
        doneButton = findViewById(R.id.done_button);
        dayRateEdittext = findViewById(R.id.day_rate_edittext);
        rolesListContainer = findViewById(R.id.roles_list_container);
        pickingRoleLabel = findViewById(R.id.picking_role_label);
        mostWantedJobsLabel = findViewById(R.id.most_wanted_jobs_label);
    }

    @Override
    public void onBackPressed() {
        if (addExperienceLayout.getVisibility() == View.VISIBLE) {
            searchEditText.setText(previousSearch);
            searchEditText.setEnabled(true);
            rolesListContainer.setVisibility(View.VISIBLE);
            addExperienceLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
