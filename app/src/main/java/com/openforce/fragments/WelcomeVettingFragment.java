package com.openforce.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.activity.VettingActivity;
import com.openforce.adapters.SlidePageAdapter;
import com.openforce.interfaces.PageSliderClick;
import com.openforce.utils.Utils;

public class WelcomeVettingFragment extends Fragment {

    private TextView vettingTitle;
    private RelativeLayout addExperienceSkillLayout;
    private ImageView fakeCheckboxExperience;
    private RelativeLayout addReferencesLayout;
    private ImageView fakeCheckboxReferences;

    private RelativeLayout addPaymentLayout;
    private ImageView fakeCheckboxPayment;

    private RelativeLayout secureAccountLayout;
    private ImageView fakeCheckboxSecure;
    private ImageView closeButton;
    private TextView vettingSubtitle;
    private ImageView addExperienceArrow;
    private ImageView addReferenceArrow;
    private ImageView secureAccountArrow;
    private ImageView paymentInfoArrow;
    private ViewPager viewPager;

    private PageSliderClick pageSliderClick;
    private VettingCallbacks vettingCallbacks;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_vetting, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof VettingCallbacks) {
            vettingCallbacks = (VettingCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement " + VettingCallbacks.class.getSimpleName());
        }

        if (activity instanceof PageSliderClick) {
            pageSliderClick = (PageSliderClick) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement " + PageSliderClick.class.getSimpleName());
        }
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addExperienceSkillLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                vettingCallbacks.onAddExperienceClick();
            }
        });

        addReferencesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                vettingCallbacks.onAddReferencesClick();
            }
        });

        addPaymentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                vettingCallbacks.onAddPaymentInfoClick();
            }
        });

        secureAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                vettingCallbacks.onSecureAccountClick();
            }
        });

        // only show the close button when it's attached to the vetting activity
        if (getActivity() instanceof VettingActivity) {
            closeButton.setVisibility(View.VISIBLE);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View __) {
                    vettingCallbacks.onWelcomeVettingCloseButtonClick();
                }
            });
        } else {
            closeButton.setVisibility(View.GONE);
        }

        SlidePageAdapter adapter = new SlidePageAdapter(getActivity());
        adapter.setOnItemClickListener(new SlidePageAdapter.OnPageClickedListener() {
            @Override
            public void onPageClicked(int position) {
                viewPager.setCurrentItem(position, true);
                switch (position) {
                    case 0:
                        // show experience and skills\
                        pageSliderClick.onSkillPageClicked();
                        break;
                    case 1:
                        // show identity & references
                        pageSliderClick.onIdentityReferencesPageClicked();
                        break;
                    case 2:
                        // show secure account
                        pageSliderClick.onSecureAccountPageClicked();
                        break;

                    case 3:
                        // show secure account
                        pageSliderClick.onAddPaymentPageClicked();
                        break;
                }
            }
        });
        viewPager.setAdapter(adapter);
        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.welcome_pager_margin));
        viewPager.setOffscreenPageLimit(3);
    }

    private void initView(View view) {
        vettingTitle = view.findViewById(R.id.vetting_title);
        addExperienceSkillLayout = view.findViewById(R.id.add_experience_skill_layout);
        fakeCheckboxExperience = view.findViewById(R.id.fake_checkbox_experience);
        addReferencesLayout = view.findViewById(R.id.add_references_layout);
        fakeCheckboxReferences = view.findViewById(R.id.fake_checkbox_references);

        addPaymentLayout = view.findViewById(R.id.add_payment_layout);
        fakeCheckboxPayment = view.findViewById(R.id.fake_checkbox_add_payment);

        secureAccountLayout = view.findViewById(R.id.secure_account_layout);
        fakeCheckboxSecure = view.findViewById(R.id.fake_checkbox_secure);
        closeButton = view.findViewById(R.id.vetting_close);
        vettingSubtitle = view.findViewById(R.id.vetting_subtitle);
        addExperienceArrow = view.findViewById(R.id.add_experience_arrow);
        addReferenceArrow = view.findViewById(R.id.add_reference_arrow);
        secureAccountArrow = view.findViewById(R.id.secure_account_arrow);
        paymentInfoArrow = view.findViewById(R.id.add_payment_arrow);
        viewPager = view.findViewById(R.id.view_pager);
    }

    @Override
    public void onResume() {
        super.onResume();
        User user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();

        System.out.println("userInfo" + user.toString());

        String nameOfUser = user.firstName;
        vettingTitle.setText(getString(R.string.vetting_welcome, nameOfUser));
        if (user.references != null && !user.references.isEmpty() && user.cvImageUrl !=null ) {
            fakeCheckboxReferences.setImageResource(R.drawable.ic_vetting_fake_checkbox_checked);
            addReferenceArrow.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(OpenForceApplication.getInstance().getFirebaseAuth().getCurrentUser().getPhoneNumber()) && !TextUtils.isEmpty(user.pin)) {
            fakeCheckboxSecure.setImageResource(R.drawable.ic_vetting_fake_checkbox_checked);
            secureAccountArrow.setVisibility(View.INVISIBLE);
        }

        if (user.stripe_info != null && !user.stripe_info.isEmpty()) {
            fakeCheckboxPayment.setImageResource(R.drawable.ic_vetting_fake_checkbox_checked);
            paymentInfoArrow.setVisibility(View.INVISIBLE);
        }

        if (user.skills != null && !user.skills.isEmpty()) {
            System.out.println("Partha Skill" + user.skills.toString());
            fakeCheckboxExperience.setImageResource(R.drawable.ic_vetting_fake_checkbox_checked);
            addExperienceArrow.setVisibility(View.INVISIBLE);
        }

        //Change it for Payment
//        if (user.skills != null && !user.skills.isEmpty()) {
//            fakeCheckboxExperience.setImageResource(R.drawable.ic_vetting_fake_checkbox_checked);
//            addExperienceArrow.setVisibility(View.INVISIBLE);
//        }


        if (Utils.isUserVetted(FirebaseAuth.getInstance(), OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo())) {
            vettingCallbacks.onUserFullyVetted();
        }
    }

    public interface VettingCallbacks {

        void onAddExperienceClick();

        void onAddReferencesClick();

        void onSecureAccountClick();

        void onAddPaymentInfoClick();

        void onWelcomeVettingCloseButtonClick();

        void onUserFullyVetted();
    }
}
