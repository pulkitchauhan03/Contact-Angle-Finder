package com.example.contactanglefinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

import java.io.IOException;
import java.util.ArrayList;
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
    @SuppressLint("SetTextI18n")
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
        getResults(bitmap);

        leftContactAngleView.setText(Double.toString(leftContactAngle));
        rightContactAngleView.setText(Double.toString(rightContactAngle));
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

    private void getResults(Bitmap img) {
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

        // Get Ellipse Details
        RotatedRect box = getEllipseDetails(mat);

        // Get Base Line
        Line baseline = getBaseLine(mat);

        // Drawing Ellipse on the image
        Imgproc.ellipse(orig, box, new Scalar(255, 0, 0), 2);

        // Draw Base Line
        Point pt1 = new Point(0, Math.round(baseline.intercept));
        Point pt2 = new Point(imgSize.width, Math.round((baseline.slope*imgSize.width) + baseline.intercept));
        Imgproc.line(orig, pt1, pt2, new Scalar(0, 0, 255), 2, Imgproc.LINE_AA, 0);

        // Find Contact Angles
        findContactAngles(box, baseline);

        Utils.matToBitmap(orig, bitmap);
        resultImgView.setImageBitmap(bitmap);
    }

    private void findContactAngles(RotatedRect ellipse, Line baseline) {
        Log.d("Ellipse Angle", ellipse.toString());
        Log.d("Line", baseline.toString());
        ellipse.size.width = ellipse.size.width/2.0;
        ellipse.size.height = ellipse.size.height/2.0;
        ellipse.angle = (ellipse.angle * Math.PI) / 180.0;

        double c0, c1, c2, c3, c4, c5;
        double cos_sq = Math.pow(Math.cos(ellipse.angle), 2);
        double sin_sq = Math.pow(Math.sin(ellipse.angle), 2);
        double a_sq = Math.pow(ellipse.size.width, 2);
        double b_sq = Math.pow(ellipse.size.height, 2);
        double h_sq = Math.pow(ellipse.center.x, 2);
        double k_sq = Math.pow(ellipse.center.y, 2);
        double hk = ellipse.center.x * ellipse.center.y;

        c0 = cos_sq/a_sq + sin_sq/b_sq;
        c1 = cos_sq/b_sq + sin_sq/a_sq;
        c2 = (Math.sin((2*ellipse.angle))/a_sq) - (Math.sin((2*ellipse.angle))/b_sq);
        c3 = -((2 * ellipse.center.x * cos_sq) / a_sq) - ((ellipse.center.y * Math.sin(2*ellipse.angle)) / a_sq) - ((2 * ellipse.center.x * sin_sq) / b_sq) + ((ellipse.center.y * Math.sin(2*ellipse.angle)) / b_sq);
        c4 = -((2 * ellipse.center.y * cos_sq) / b_sq) - ((ellipse.center.x * Math.sin(2*ellipse.angle)) / a_sq) - ((2 * ellipse.center.y * sin_sq) / a_sq) + ((ellipse.center.x * Math.sin(2*ellipse.angle)) / b_sq);
        c5 = ((h_sq * cos_sq) / a_sq) + ((hk * Math.sin(2*ellipse.angle)) / a_sq) + ((k_sq * sin_sq) / a_sq) + ((h_sq * sin_sq) / b_sq) - ((hk * Math.sin(2*ellipse.angle)) / b_sq) + ((k_sq * cos_sq) / b_sq) - 1;

        Log.d("ellipse", Double.toString(c0) + " " + Double.toString(c1) + " " + Double.toString(c2) + " " + Double.toString(c3) + " " + Double.toString(c4) + " " + Double.toString(c5));

        double A = (c0) + (c1*baseline.slope*baseline.slope) + (c2*baseline.slope);
        double B = (2*c1*baseline.intercept*baseline.slope) + (baseline.intercept*c2) + c3 + (c4*baseline.slope);
        double C = (c1*baseline.intercept*baseline.intercept) + (c4*baseline.intercept) + c5;
        double D = Math.sqrt((B*B) - (4*A*C));

        Log.d("intersection", Double.toString(A) + " " + Double.toString(B) + " " + Double.toString(C));

        Point leftContactPoint = new Point();
        Point rightContactPoint = new Point();

        leftContactPoint.x = (-B - D) / (2*A);
        rightContactPoint.x = (-B + D) / (2*A);
        leftContactPoint.y = baseline.slope * leftContactPoint.x + baseline.intercept;
        rightContactPoint.y = baseline.slope * rightContactPoint.x + baseline.intercept;

        Log.d("left point", leftContactPoint.toString());
        Log.d("right point", rightContactPoint.toString());

        double slopeLeft = -(2*c0*leftContactPoint.x + c2*leftContactPoint.y + c3) / (2*c1*leftContactPoint.y + c2*leftContactPoint.x + c4);
        double slopeRight = -(2*c0*rightContactPoint.x + c2*rightContactPoint.y + c3) / (2*c1*rightContactPoint.y + c2*rightContactPoint.x + c4);

        leftContactAngle = -(Math.atan(slopeLeft) * 180.0) / Math.PI;
        rightContactAngle = (Math.atan(slopeRight) * 180.0) / Math.PI;
    }

    private Line getBaseLine(Mat mat) {
        Line baseline = new Line();
        Imgproc.Canny(mat, mat, 50, 200, 3, false);

        Mat lines = new Mat();
        Imgproc.HoughLines(mat, lines, 1, Math.PI/180, 150);

        double rho = lines.get(0, 0)[0],
                theta = lines.get(0, 0)[1];
        double a = Math.cos(theta), b = Math.sin(theta);
        double x0 = a*rho, y0 = b*rho;

        baseline.slope = -(a/b);
        baseline.intercept = rho/b;
        return baseline;
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

    private Mat getCroppedImage(@NonNull Mat mat) {
        Rect rect = new Rect(leftContact, 0, rightContact-leftContact+1, (int)imgSize.height);
        return mat.submat(rect);
    }

//        Imgproc.drawContours(orig, contours, -1, color, 2, Imgproc.LINE_8, hierarchy, 2, new Point() ) ;

//        Bitmap newBitmap = Bitmap.createBitmap(orig.cols(), orig.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(orig, newBitmap);
//        resultImgView.setImageBitmap(newBitmap);
}
