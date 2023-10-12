package com.example.contactanglefinder;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    ImageView imgView;
    Button btnChange;
    Button btnProceed;
    Uri imgUri;
    ActivityResultLauncher<Uri> contract = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            imgView.setImageURI(null);
            imgView.setImageURI(imgUri);

            btnProceed.setVisibility(View.VISIBLE);
            btnChange.setText("Retake");
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btnChange.getLayoutParams();
            params.endToStart = btnProceed.getId();
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            btnChange.setLayoutParams(params);
        }
    });;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imgView);
        btnChange = findViewById(R.id.btnChange);
        btnProceed = findViewById(R.id.btnProceed);

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
                startActivity(intent);
            }
        });
    }

    private Uri createImageUri() {
        File image = new File(getFilesDir(), "camera_photo.jpeg");
        return FileProvider.getUriForFile(getApplicationContext(), "com.example.contactanglefinder.fileProvider", image);
    }
}