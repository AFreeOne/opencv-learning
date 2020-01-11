/**
 * @date: 2019年12月13日 下午3:38:42
 */
package org.freeatalk.opencv.learning.runner;

import java.net.URL;

import org.opencv.core.Mat;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;
import static org.opencv.imgproc.Imgproc.equalizeHist;

import static org.opencv.imgcodecs.Imgcodecs.imread;

import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.equalizeHist;

/**
 * @ClassName: OpencvRunner.java
 * @version: v1.0.0
 * @author lqq
 * @date: 2019年12月13日 下午3:38:42 
 *
 */
//@Component
public class OpencvRunner implements ApplicationRunner {

    /* (non-Javadoc)
     * @see org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments)
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        
        /*-Djava.awt.headless=false*/
        URL systemResource = ClassLoader.getSystemResource("lib/opencv_java411.dll");
        System.load(systemResource.getPath());
     
        Mat image = imread("C:\\Users\\HL\\Pictures\\hzxc.jpg", 1);
        if (image.empty()){
            throw new Exception("image is empty!");
        }
        imshow("Original Image", image);
        List<Mat> imageRGB = new ArrayList<>();
        split(image, imageRGB);
        for (int i = 0; i < 3; i++) {
            equalizeHist(imageRGB.get(i), imageRGB.get(i));
        }
        merge(imageRGB, image);
        imshow("Processed Image", image);
        waitKey();
    }

}
