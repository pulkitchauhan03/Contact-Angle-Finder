package com.example.contactanglefinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultsActivity extends Activity {
    Uri imgUri;
    int leftContact, rightContact;
    double leftContactAngle, rightContactAngle;
    Bitmap bitmap;
    ImageView resultImgView;
    TextView leftContactAngleView, rightContactAngleView;
    Size imgSize;
    Mat orig;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        OpenCVLoader.initDebug();

        resultImgView = findViewById(R.id.resultImgView);
        leftContactAngleView = findViewById(R.id.leftContactAngleView);
        rightContactAngleView = findViewById(R.id.rightContactAngleView);

        Intent intent = getIntent();
        imgUri = intent.getParcelableExtra("imgUri");
        leftContact = intent.getIntExtra("leftContactPoint", 0);
        rightContact = intent.getIntExtra("rightContactPoint", 0);

        bitmap = getImage();
        findContactAngle(bitmap);
    }

    private Bitmap getImage() {
        Bitmap imgbitmap = null;

        try {
            imgbitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
        } catch (IOException e) {
            Log.d("Error", "File Not Found");
        }

        return imgbitmap;
    }

    private void findContactAngle(Bitmap img) {
        Mat mat = new Mat();
        orig = new Mat();
        Utils.bitmapToMat(img, orig);
        imgSize = orig.size();

        Imgproc.cvtColor(orig, mat, Imgproc.COLOR_RGB2GRAY);

        // Blurring the Image
        Size gaussianKernelSize = new Size();
        gaussianKernelSize.height = 7;
        gaussianKernelSize.width = 7;
        Imgproc.blur(mat, mat, gaussianKernelSize);

        // Thresholding the image
        Imgproc.threshold(mat, mat, 127, 255, 0);
        Core.bitwise_not(mat, mat);

        // Drawing Ellipse on the image
        RotatedRect box = getEllipseDetails(mat);
        Imgproc.ellipse(orig, box, new Scalar(255, 0, 0), 2);

        Utils.matToBitmap(orig, bitmap);
        resultImgView.setImageBitmap(bitmap);
    }

    private RotatedRect getEllipseDetails(Mat mat) {
        Mat croppedImg = getCroppedImage(mat);

        // Finding Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(croppedImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        // Finding Convex hull of the contour
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contours.get(0), hull);

        Point[] contourArray = contours.get(0).toArray();
        Point[] hullPoints = new Point[hull.rows()];
        List<Integer> hullContourIdxList = hull.toList();
        for (int i = 0; i < hullContourIdxList.size(); i++) {
            hullPoints[i] = contourArray[hullContourIdxList.get(i)];
        }

        // Fitting the ellipse to get Ellipse Details
        RotatedRect box = Imgproc.fitEllipse(new MatOfPoint2f(hullPoints));

        // Adding the Crop Offset
        box.center.x = box.center.x + leftContact;
        return box;
    }

    private Mat getCroppedImage(Mat mat) {
        Rect rect = new Rect(leftContact, 0, rightContact-leftContact+1, (int)imgSize.height);
        return mat.submat(rect);
    }

//        Imgproc.drawContours(orig, contours, -1, color, 2, Imgproc.LINE_8, hierarchy, 2, new Point() ) ;

//        Bitmap newBitmap = Bitmap.createBitmap(orig.cols(), orig.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(orig, newBitmap);
//        resultImgView.setImageBitmap(newBitmap);
}
