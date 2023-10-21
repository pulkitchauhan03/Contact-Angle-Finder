package com.example.contactanglefinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import java.io.IOException;

public class ImageProcessingActivity extends Activity {
    ImageView imgView;
    SeekBar seekBarOne, seekBarTwo;
    Button nextBtn;
    Uri imgUri;
    int imgWidth;
    int contactPointsSelectorState;
    double contactLeft, contactRight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);
        findViewById(R.id.seekBarOne).setPadding(0,0,0,0);
        findViewById(R.id.seekBarTwo).setPadding(0,0,0,0);

        imgView = findViewById(R.id.imgView2);
        seekBarOne = findViewById(R.id.seekBarOne);
        seekBarTwo = findViewById(R.id.seekBarTwo);
        nextBtn = findViewById(R.id.nextBtn);

        contactPointsSelectorState = 0;

        Intent intent = getIntent();
        imgUri = intent.getParcelableExtra("imgUri");

        imgView.setImageBitmap(getImage());

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(contactPointsSelectorState == 0) {
                    seekBarTwo.setVisibility(View.VISIBLE);
                    contactPointsSelectorState = 2;
                } else if(contactPointsSelectorState == 1) {
                    contactPointsSelectorState = 2;
                } else {
                    Intent intent = new Intent(ImageProcessingActivity.this, ResultsActivity.class);
                    intent.putExtra("imgUri", imgUri);
                    int l = (int)Math.ceil(imgWidth*contactLeft);
                    int r = (int)Math.floor(imgWidth*contactRight);
                    intent.putExtra("leftContactPoint", l);
                    intent.putExtra("rightContactPoint", r);
                    startActivity(intent);
                }
            }
        });

        seekBarOne.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                contactLeft = progress / 1000f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarTwo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                contactRight = progress / 1000f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private Bitmap getImage() {
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
            imgWidth = bitmap.getWidth();
        } catch (IOException e) {
            Log.d("Error", "File Not Found");
        }

        return bitmap;
    }
}