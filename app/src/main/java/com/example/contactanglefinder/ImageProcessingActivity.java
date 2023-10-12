package com.example.contactanglefinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;

public class ImageProcessingActivity extends Activity {
    ImageView imgView;

    Uri imgUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);

        OpenCVLoader.initDebug();

        imgView = findViewById(R.id.imgView2);
        Intent intent = getIntent();
        imgUri = intent.getParcelableExtra("imgUri");

        imgView.setImageBitmap(processImage());
    }

    private Bitmap processImage() {
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);

            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);

            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);

            Utils.matToBitmap(mat, bitmap);

        } catch (IOException e) {
            Log.d("Error", "File Not Found");
        }

        return bitmap;
    }
}