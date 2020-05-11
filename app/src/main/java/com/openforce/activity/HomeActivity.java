package com.openforce.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.fragments.MapJobFragment;
import com.openforce.fragments.ChatsFragment;
import com.openforce.fragments.VettedFragment;
import com.openforce.fragments.WelcomeVettingFragment;
import com.openforce.interfaces.PageSliderClick;
import com.openforce.utils.Utils;

public class HomeActivity extends BaseActivity implements WelcomeVettingFragment.VettingCallbacks, PageSliderClick, VettedFragment.VettedFragmentCallbacks {

    private BottomNavigationView bottomNavigation;
    private WelcomeVettingFragment welcomeVettingFragment;
    private VettedFragment vettedFragment;
    private MapJobFragment mapJobFragment;
    private ChatsFragment chatsFragment;
    private Fragment currentFragment;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        // add your extras here
        return intent;
    }

    public static Intent getIntentWithClearFlag(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        welcomeVettingFragment = new WelcomeVettingFragment();
        mapJobFragment = new MapJobFragment();
        vettedFragment = new VettedFragment();
        chatsFragment = new ChatsFragment();

        initView();
    }

    // Partha
    //Sidy
    private void initView() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (currentFragment != null) {
                    FragmentTransaction transaction = HomeActivity.this.getFragmentManager().beginTransaction();
                    transaction.hide(currentFragment).commit();
                }
                switch (item.getItemId()) {
                    case R.id.action_profile:
                        if (Utils.isUserVetted(firebaseAuth, OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo())) {
                            if (HomeActivity.this.getFragmentManager().findFragmentByTag(VettedFragment.class.getSimpleName()) == null) {
                                FragmentTransaction transaction = HomeActivity.this.getFragmentManager().beginTransaction();
                                transaction.add(R.id.main_content, vettedFragment, VettedFragment.class.getSimpleName())
                                        .commit();
                            } else {
                                FragmentTransaction transaction = HomeActivity.this.getFragmentManager().beginTransaction();
                                transaction.show(vettedFragment).commit();
                            }
                            currentFragment = vettedFragment;
                        } else {
                            if (HomeActivity.this.getFragmentManager().findFragmentByTag(WelcomeVettingFragment.class.getSimpleName()) == null) {
                                FragmentTransaction transaction = HomeActivity.this.getFragmentManager().beginTransaction();
                                transaction.add(R.id.main_content, welcomeVettingFragment, WelcomeVettingFragment.class.getSimpleName())
                                        .commit();
                            } else {
                                FragmentTransaction transaction = HomeActivity.this.getFragmentManager().beginTransaction();
                                transaction.show(welcomeVettingFragment).commit();
                            }
                            currentFragment = welcomeVettingFragment;
                        }
                        break;
                    case R.id.action_map:
                        if (HomeActivity.this.getFragmentManager().findFragmentByTag(MapJobFragment.class.getSimpleName()) == null) {
                            FragmentTransaction transaction2 = HomeActivity.this.getFragmentManager().beginTransaction();
                            transaction2.add(R.id.main_content, mapJobFragment, MapJobFragment.class.getSimpleName()).commit();
                        } else {
                            FragmentTransaction transaction = HomeActivity.this.getFragmentManager().beginTransaction();
                            transaction.show(mapJobFragment).commit();
                        }
                        currentFragment = mapJobFragment;
                        break;
                    case R.id.action_conversation:
                        if (HomeActivity.this.getFragmentManager().findFragmentByTag(ChatsFragment.class.getSimpleName()) == null) {
                            FragmentTransaction transaction3 = HomeActivity.this.getFragmentManager().beginTransaction();
                            transaction3.add(R.id.main_content, chatsFragment, ChatsFragment.class.getSimpleName())
                                    .commit();
                        } else {
                            FragmentTransaction transaction = HomeActivity.this.getFragmentManager().beginTransaction();
                            transaction.show(chatsFragment).commit();
                        }

                        currentFragment = chatsFragment;
                        break;
                }
                return true;
            }
        });
        bottomNavigation.setSelectedItemId(R.id.action_profile);

    }

    @Override
    public void onAddExperienceClick() {
        startActivity(ExperienceSkillsActivity.getIntent(this));
    }

    @Override
    public void onAddReferencesClick() {
        startActivity(IdentityReferencesActivity.getIntent(this));
    }

    @Override
    public void onSecureAccountClick() {
        startActivity(SecureAccountActivity.getIntent(this));
    }

    @Override
    public void onAddPaymentInfoClick() {
        startActivity(AddPaymentActivity.getIntent(this));
    }

    @Override
    public void onWelcomeVettingCloseButtonClick() {

    }

    @Override
    public void onUserFullyVetted() {
        bottomNavigation.setSelectedItemId(R.id.action_profile);
    }

    @Override
    public void onSkillPageClicked() {
        startActivity(ExperienceSkillsActivity.getIntent(this));
    }

    @Override
    public void onIdentityReferencesPageClicked() {
        startActivity(IdentityReferencesActivity.getIntent(this));
    }

    @Override
    public void onSecureAccountPageClicked() {
        startActivity(SecureAccountActivity.getIntent(this));
    }

    @Override
    public void onAddPaymentPageClicked() {
        startActivity(AddPaymentActivity.getIntent(this));
    }

    @Override
    public void onNoJobLayoutClick() {
        bottomNavigation.setSelectedItemId(R.id.action_map);
    }
}
