package com.example.contactanglefinder;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ImageView imgView;
    Button btnChange, btnProceed, selectImg;
    Uri imgUri;
    ActivityResultLauncher<Uri> contract = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            imgView.setImageURI(null);
            imgView.setImageURI(imgUri);

            enableProceedButton();
        }
    });

    ActivityResultLauncher<Intent> selectImageContract = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                imgUri = data.getData();
                imgView.setImageURI(imgUri);

                enableProceedButton();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imgView);
        btnChange = findViewById(R.id.btnChange);
        btnProceed = findViewById(R.id.btnProceed);
        selectImg = findViewById(R.id.selectImg);

        imgUri = createImageUri();
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contract.launch(imgUri);
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ImageProcessingActivity.class);
                intent.putExtra("imgUri", imgUri);
                startActivity(intent);
            }
        });

        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                selectImageContract.launch(intent);
            }
        });
    }

    private Uri createImageUri() {
        File image = new File(getFilesDir(), "camera_photo.jpeg");
        return FileProvider.getUriForFile(getApplicationContext(), "com.example.contactanglefinder.fileProvider", image);
    }

    private void enableProceedButton() {
        btnProceed.setVisibility(View.VISIBLE);
        btnChange.setText(R.string.retake);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) selectImg.getLayoutParams();
        params.endToStart = btnProceed.getId();
        params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
        selectImg.setLayoutParams(params);
    }
}