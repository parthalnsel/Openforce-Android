package com.openforce.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.openforce.R;
import com.openforce.activity.AddCvDocActivity;
import com.openforce.activity.CscsCardActivity;

public class IdentityReferencesFragment extends Fragment {

    private ImageView closeButton;
    private LinearLayout addEmailReferencesLayout,add_cv_doc,add_cscs_card;;
    private IdentityReferencesCallbacks callbacks;
    private Button doneButton;
    private static final int STORAGE_PERMISSION_CODE=1001;

    private static final int WRITE_STORAGE_PERMISSION_CODE=3001;
    private static final int CAMERA_PERMISSION_CODE=2001;
    private boolean camera_check,storage_check , write_storage_check;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_identity_references, container, false);
        initView(view);


        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IdentityReferencesCallbacks) {
            callbacks = (IdentityReferencesCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement " + IdentityReferencesCallbacks.class.getSimpleName());
        }
    }

    private void initView(View view) {
        closeButton = view.findViewById(R.id.close_button);
        addEmailReferencesLayout = view.findViewById(R.id.add_email_references_layout);
        doneButton = view.findViewById(R.id.button_done);
        add_cv_doc=view.findViewById(R.id.add_cv_doc);
        add_cscs_card=view.findViewById(R.id.add_cscs_card);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onIdentityReferencesCloseButtonClick();
            }
        });
        addEmailReferencesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onAddEmailReferences();
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onDoneClicked();
            }
        });
        if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)){
            camera_check=true;
        }
        if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)){
            storage_check=true;

        }
        if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)){
            write_storage_check=true;

        }
        add_cv_doc.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                System.out.println("Permission: " + (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED));

                if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED)|| (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED)){

                    requestWriteStoragePermission();
                    requestStoragePermission();
                    cameraPermission();

                }else {
                    Intent ii=new Intent(getActivity(), AddCvDocActivity.class);
                    startActivity(ii);
                }

            }
        });

        add_cscs_card.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED)){

                    requestWriteStoragePermission();
                    requestStoragePermission();
                    cameraPermission();

                }else {
                    Intent ii=new Intent(getActivity(), CscsCardActivity.class);
                    startActivity(ii);
                }

            }
        });
    }

    public interface IdentityReferencesCallbacks {

        void onAddEmailReferences();

        void onIdentityReferencesCloseButtonClick();

        void onDoneClicked();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission()
    {
        if(ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
            return;
        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestWriteStoragePermission()
    {
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
            return;
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_STORAGE_PERMISSION_CODE);

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void cameraPermission(){
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED)
            return;
        requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==STORAGE_PERMISSION_CODE)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                storage_check=true;

                if (storage_check==true && write_storage_check==true&& camera_check==true){
                    Intent ii=new Intent(getActivity(), AddCvDocActivity.class);
                    startActivity(ii);
                }
            }
        }

        if(requestCode==WRITE_STORAGE_PERMISSION_CODE)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                write_storage_check=true;

                if (storage_check==true && write_storage_check==true&& camera_check==true){
                    Intent ii=new Intent(getActivity(), AddCvDocActivity.class);
                    startActivity(ii);
                }
            }
        }

        if(requestCode==CAMERA_PERMISSION_CODE)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {

                camera_check=true;

                if (storage_check==true && write_storage_check==true&& camera_check==true){
                    Intent ii=new Intent(getActivity(), AddCvDocActivity.class);
                    startActivity(ii);
                }
            }
        }
    }
}
