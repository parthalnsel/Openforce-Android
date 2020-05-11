package com.openforce.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openforce.R;
import com.openforce.utils.PinTextWatcher;
import com.openforce.utils.UIUtils;

public class PinFragment extends Fragment implements PinTextWatcher.PinCallbacks {

    private static final String EXTRA_TITLE = "EXTRA_" + PinFragment.class.getSimpleName() + ".TITLE";
    private static final String EXTRA_BACK_VISIBLE = "EXTRA_" + PinFragment.class.getSimpleName() + ".BACK_VISIBLE";

    private LinearLayout navigationHeaderContainer;
    private ImageView backButton;
    private AppCompatEditText pinInput1;
    private AppCompatEditText pinInput2;
    private AppCompatEditText pinInput3;
    private AppCompatEditText pinInput4;
    private TextView titleHeader;

    private PinTextWatcher pinTextWatcher;
    private PinFragmentCallbacks callbacks;

    public static PinFragment newInstance(String title, boolean backButtonVisible) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        args.putBoolean(EXTRA_BACK_VISIBLE, backButtonVisible);
        PinFragment fragment = new PinFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof PinFragmentCallbacks) {
            callbacks = (PinFragmentCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement " + PinFragment.PinFragmentCallbacks.class.getSimpleName());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pin, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pinTextWatcher = new PinTextWatcher(pinInput1, pinInput2, pinInput3, pinInput4, this);
        pinInput1.addTextChangedListener(pinTextWatcher);
        pinInput2.addTextChangedListener(pinTextWatcher);
        pinInput3.addTextChangedListener(pinTextWatcher);
        pinInput4.addTextChangedListener(pinTextWatcher);

        Bundle bundle = getArguments();
        String title = bundle.getString(EXTRA_TITLE);
        boolean backVisible = bundle.getBoolean(EXTRA_BACK_VISIBLE, false);
        titleHeader.setText(title);
        backButton.setVisibility(backVisible ? View.VISIBLE : View.GONE);
    }

    private void initView(View view) {
        navigationHeaderContainer = view.findViewById(R.id.navigation_header_container);
        backButton = view.findViewById(R.id.back_button);
        pinInput1 = view.findViewById(R.id.pin_input_1);
        pinInput2 = view.findViewById(R.id.pin_input_2);
        pinInput3 = view.findViewById(R.id.pin_input_3);
        pinInput4 = view.findViewById(R.id.pin_input_4);
        titleHeader = view.findViewById(R.id.title_header);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onBackClicked();
            }
        });

        pinInput1.requestFocus();
        pinInput1.postDelayed(new Runnable() {
            @Override
            public void run() {
                UIUtils.showKeyboard(pinInput1);
            }
        }, 250);

    }

    @Override
    public void onPinComplete(String pin) {
        callbacks.onPinComplete(pin);
    }

    public void clearPin() {
        pinInput4.setText("");
        pinInput3.setText("");
        pinInput2.setText("");
        pinInput1.setText("");
    }

    public interface PinFragmentCallbacks {

        void onPinComplete(String pin);

        void onBackClicked();
    }
}
