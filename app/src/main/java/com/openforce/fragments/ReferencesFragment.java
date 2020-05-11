package com.openforce.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.model.Reference;
import com.openforce.utils.ValidationUtils;

import java.util.Arrays;
import java.util.List;

public class ReferencesFragment extends Fragment {

    private ReferencesCallbacks callbacks;
    private EditText inputNameRefer1;
    private EditText inputEmailRefer1;
    private EditText inputNameRefer2;
    private EditText inputEmailRefer2;
    private Button saveButton;
    private ImageView backButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_references, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();
        if (user.references != null && user.references.size() > 0) {
            Reference reference1 = user.references.get(0);
            inputNameRefer1.setText(reference1.name);
            inputEmailRefer1.setText(reference1.email);
            if (user.references.size() > 1) {
                Reference reference2 = user.references.get(1);
                inputNameRefer2.setText(reference2.name);
                inputEmailRefer2.setText(reference2.email);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ReferencesCallbacks) {
            callbacks = (ReferencesCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement " + ReferencesCallbacks.class.getSimpleName());
        }
    }

    private void initView(final View view) {
        inputNameRefer1 = view.findViewById(R.id.input_name_refer_1);
        inputEmailRefer1 = view.findViewById(R.id.input_email_refer_1);
        inputNameRefer2 = view.findViewById(R.id.input_name_refer_2);
        inputEmailRefer2 = view.findViewById(R.id.input_email_refer_2);
        saveButton = view.findViewById(R.id.save_button);
        backButton = view.findViewById(R.id.back_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputEmailRefer1.getText().toString().equals(inputEmailRefer2.getText().toString())) {
                    Snackbar.make(view, "Reference Email Must be different", Snackbar.LENGTH_LONG).show();
                } else if (!TextUtils.isEmpty(inputNameRefer1.getText())
                        && ValidationUtils.isValidEmail(inputEmailRefer1.getText().toString())
                        && !TextUtils.isEmpty(inputNameRefer2.getText()) &&
                        ValidationUtils.isValidEmail(inputEmailRefer2.getText().toString())) {
                    // TODO add check to see if the user changed anything. if didn't then no need to save the references.
                    Reference reference1 = new Reference(inputNameRefer1.getText().toString(), inputEmailRefer1.getText().toString());
                    Reference reference2 = new Reference(inputNameRefer2.getText().toString(), inputEmailRefer2.getText().toString());
                    callbacks.onSaveReferences(Arrays.asList(reference1, reference2));
                } else {
                    Snackbar.make(view, R.string.invalid_reference, Snackbar.LENGTH_LONG).show();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onReferencesBackArrowClicked();
            }
        });
    }


    public interface ReferencesCallbacks {

        void onSaveReferences(List<Reference> references);

        void onReferencesBackArrowClicked();

    }
}
