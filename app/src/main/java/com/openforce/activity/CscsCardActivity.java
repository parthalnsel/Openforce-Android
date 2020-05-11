package com.openforce.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.openforce.R;
import com.openforce.widget.CameraPreview;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class CscsCardActivity extends AppCompatActivity {

    private ImageView back,img_camera,cv_img;
    private CameraPreview mPreview;
    private Context myContext;
    private RelativeLayout cameraPreview;
    private static final String IMAGE_DIRECTORY = "/CustomImage";
    private Camera mCamera;
    private Camera.PictureCallback mPicture;
    public static Bitmap bitmap;
    private boolean cameraFront = false;
    private RelativeLayout rl_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cscs_card);

        intView();
    }


    private void intView(){

        myContext = this;
        back=(ImageView)findViewById(R.id.back);
        img_camera=(ImageView)findViewById(R.id.img_camera);
        cameraPreview = (RelativeLayout) findViewById(R.id.cPreview);
        cv_img=(ImageView)findViewById(R.id.cv_img);
        rl_image=(RelativeLayout)findViewById(R.id.rl_image);

        if (!cameraFront) {
            int cameraId = findBackFacingCamera();

            System.out.println("Value==="+cameraId);
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mPicture = getPictureCallback();
                mPreview = new CameraPreview(myContext, mCamera);
                cameraPreview.addView(mPreview);

            }
        }





        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        img_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, mPicture);
                //  mPreview.refreshCamera(mCamera);

            }
        });
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                System.out.println("ImageBit=="+bitmap);
                rl_image.setVisibility(View.GONE);
                cv_img.setVisibility(View.VISIBLE);
                // cv_img.setImageBitmap(bitmap);
                //cv_img.setRotation(180);
                mPreview.refreshCamera(mCamera);
                saveImage(bitmap);
                // String usrProfileImage = getStringImage(bitmap);

                //  System.out.println("Base64==="+usrProfileImage);

                //  BitMapToString(bitmap);
            }
        };
        return picture;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;

            }

        }
        return cameraId;
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs());
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());
            Picasso.get().load("file:" + f.getAbsolutePath()).into(cv_img);

            long fileSizeInBytes = f.getAbsolutePath().length();
// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            long fileSizeInKB = fileSizeInBytes / 1024;
            System.out.println("FileSizeL===="+fileSizeInBytes);
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            BitMapToString(bitmap);
            //  String usrProfileImage = getStringImage(bitmap);
            //  System.out.println("Base64==="+usrProfileImage);
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";

    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);

        System.out.println("Base64==="+temp);
        return temp;
    }
}
