package truns.objectdetector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by trio_pu on 12/11/17.
 */

public class theDetectorObj2 {
    //Batas atas dan bawah untuk kisaran warna HSV
    private Scalar lowerBound = new Scalar(0);
    private Scalar upperBound = new Scalar(0);

    //Minimal luas kontur dalam persen untuk penyaringan kontur
    private static double minContourArea = 0.1;

    //Jari-jari warna untuk kisaran warna HSV
    private Scalar colorRadius  = new Scalar(25,50,50,0);
    private Mat theSpectrum     = new Mat();
    private List<MatOfPoint> theContours = new ArrayList<MatOfPoint>();

    //Cache
    Mat pyrDownMat      = new Mat();
    Mat hsvMat          = new Mat();
    Mat theMask         = new Mat();
    Mat dilatedMask     = new Mat();
    Mat theHierarchy    = new Mat();

    public void setColorRadius(Scalar radius){
        colorRadius = radius;
    }

    public void setMinContourArea(double area){
        minContourArea = area;
    }

    public Mat getSpectrum(){
        return theSpectrum;
    }

    public List<MatOfPoint>getContours(){
        return theContours;
    }

    public void setHsvColor(Scalar hsvColor){
        double minH = (hsvColor.val[0] >= colorRadius.val[0]) ? hsvColor.val[0]-colorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+colorRadius.val[0] <= 255) ? hsvColor.val[0]+colorRadius.val[0] : 255;

        lowerBound.val[0] = minH;
        upperBound.val[0] = maxH;

        lowerBound.val[1] = hsvColor.val[1] - colorRadius.val[1];
        upperBound.val[1] = hsvColor.val[1] + colorRadius.val[1];

        lowerBound.val[2] = hsvColor.val[2] - colorRadius.val[2];
        upperBound.val[2] = hsvColor.val[2] + colorRadius.val[2];

        lowerBound.val[3] = 0;
        upperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, theSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public void process(Mat rgbaImage){
        Imgproc.pyrDown(rgbaImage, pyrDownMat);
        Imgproc.pyrDown(pyrDownMat, pyrDownMat);

        Imgproc.cvtColor(pyrDownMat, hsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(hsvMat, lowerBound, upperBound, theMask);
        Imgproc.dilate(theMask, dilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(dilatedMask, contours, theHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        // Mencari luasan Kontur
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        //Saring kontur dengan luasan dan ubah ukuran agar pas dengan ukuran gambar asli
        theContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > minContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                theContours.add(contour);
            }
        }
    }
}
