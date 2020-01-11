/**
 * @date: 2019年12月13日 下午4:21:42
 */
package org.freeatalk.opencv.learning.controller;



import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
 
import org.apache.commons.io.FileUtils;
import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
/**
 * @ClassName: TestController.java
 * @version: v1.0.0
 * @author lqq
 * @date: 2019年12月13日 下午4:21:42 
 *
 */

@RestController
public class TestController {
    
    @Value("classpath:haarcascade_frontalface_alt2.xml")
    private Resource faceXml;
    
    @Value("classpath:haarcascade_eye.xml")
    private Resource eyeXml;
    
    
    
    // 初始化人脸探测器


    @PostMapping("/face")
    public void faceDetector(HttpServletResponse response, MultipartFile file) throws IOException {
        
        URL systemResource = ClassLoader.getSystemResource("lib/opencv_java411.dll");
        System.load(systemResource.getPath());
        
        System.out.println("人脸检测开始……");
 
        /*创建临时文件，因为boot打包后无法读取文件内的内容*/
        
        /*人脸识别器*/
        File targetFaceXmlFile = new File("src/" + faceXml.getFilename() + "");
        FileUtils.copyInputStreamToFile(faceXml.getInputStream(), targetFaceXmlFile);
        CascadeClassifier faceDetector = new CascadeClassifier(targetFaceXmlFile.toString());
        /*眼睛识别*/
        File targetEyeXmlFile = new File("src/" + eyeXml.getFilename() + "");
        FileUtils.copyInputStreamToFile(eyeXml.getInputStream(), targetEyeXmlFile);
        CascadeClassifier eyeDetector = new CascadeClassifier(targetEyeXmlFile.toString());
        
        if (faceDetector.empty()) {
            System.out.println("请引入文件……");
            return;
        }
        
        if (eyeDetector.empty()) {
            System.out.println("请引入文件……");
            return;
        }
        // 创建图片tempFile
        File tempFile = new File("src/" + file.getOriginalFilename() + "");
        FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
 
        // 读取创建的图片tempFile
        Mat image = Imgcodecs.imread(tempFile.toString());
        Mat image1 = new Mat();
        // 灰度化
        Imgproc.cvtColor(image, image1, Imgproc.COLOR_BGR2GRAY);
        MatOfRect faceDetections = new MatOfRect();
        // 进行人脸检测
        faceDetector.detectMultiScale(image1, faceDetections);
        
        System.out.println(String.format("检测到人脸： %s", faceDetections.toArray().length));
        Integer i = 1;
        // 制图将图填充到image中
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 3);
            
            Mat faceROI = new Mat(image, rect );
            MatOfRect eyesDetections = new MatOfRect();
            eyeDetector.detectMultiScale( faceROI, eyesDetections);
            System.err.println("检测到眼睛 ... "+ eyesDetections);
            System.err.println("眼睛的数量：" + eyesDetections.toArray().length);
            
            System.out.println("转存文件： " + i);
            imageCut(tempFile.toString(), i+".jpg", rect.x, rect.y, rect.width, rect.height);// 进行图片裁剪
            i++;
            
            for (Rect eyeRect : eyesDetections.toArray()) {
                Imgproc.rectangle(image, new Point(eyeRect.x, eyeRect.y), new Point(eyeRect.x + eyeRect.width, eyeRect.y + eyeRect.height),
                        new Scalar(255, 0, 0), 2);
            }
            
          
            
//          Imgproc.circle(img, new Point(rect.x + rect.width, rect.y + rect.height), cvRound((rect.width + rect.height) * 0.25),
//          new Scalar(0, 0, 255), 2);
            
        }
        // 下面部分是返回给页面
        String filename = file.getOriginalFilename();
        Imgcodecs.imwrite(filename, image);
        File imgFile = new File(filename);
        if (imgFile.exists()) {
            response.getOutputStream().write(toByteArray(imgFile));
            response.getOutputStream().close();
        }
 
        // 删除临时文件
        if (targetFaceXmlFile.exists() && targetFaceXmlFile.isFile()) {
            if (targetFaceXmlFile.delete()) {
                System.out.println("删除临时文件" + targetFaceXmlFile + "成功！");
            }
        }
        if (targetEyeXmlFile.exists() && targetEyeXmlFile.isFile()) {
            if (targetEyeXmlFile.delete()) {
                System.out.println("删除临时文件" + targetEyeXmlFile + "成功！");
            }
        }
        if (imgFile.exists() && imgFile.isFile()) {
            if (imgFile.delete()) {
                System.out.println("删除临时文件" + imgFile + "成功！");
            }
        }
        if (tempFile.exists() && tempFile.isFile()) {
            if (tempFile.delete()) {
                System.out.println("删除临时文件" + tempFile + "成功！");
            }
        }
    }
 
    public static void imageCut(String imagePath, String outFile, int posX, int posY, int width, int height) {
        
        
        // 原始图像
        Mat image = Imgcodecs.imread(imagePath);
        // 截取的区域：参数,坐标X,坐标Y,截图宽度,截图长度
        Rect rect = new Rect(posX, posY, width, height);
        // 两句效果一样
        Mat sub = image.submat(rect); // Mat sub = new Mat(image,rect);
        Mat mat = new Mat();
        Size size = new Size(width, height);
        Imgproc.resize(sub, mat, size);// 将人脸进行截图并保存
        Imgcodecs.imwrite(outFile, mat);
        System.out.println(String.format("图片裁切成功，裁切后图片文件为： %s", outFile));
 
    }
 
    public static byte[] toByteArray(File file) throws IOException {
        File f = file;
        if (!f.exists()) {
            throw new FileNotFoundException("file not exists");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bos.close();
        }
    }
    
    
 // 灰度化人脸
    public  Mat conv_Mat(String img) throws IOException {
        
        URL systemResource = ClassLoader.getSystemResource("lib/opencv_java411.dll");
        
        System.out.println(systemResource.getPath());
        
        System.load(systemResource.getPath());
        
        
//        Mat image0 = Imgcodecs.imread(img);
        Mat image0 = Imgcodecs.imread(img,Imgcodecs.CV_LOAD_IMAGE_COLOR);

        File targetXmlFile = new File("src/" + faceXml.getFilename() + "");
        FileUtils.copyInputStreamToFile(faceXml.getInputStream(), targetXmlFile);
        CascadeClassifier faceDetector = new CascadeClassifier(targetXmlFile.toString());
        
        Mat image1 = new Mat();
        // 灰度化
        Imgproc.cvtColor(image0, image1, Imgproc.COLOR_BGR2GRAY);
     // Convert to HSV
        Imgproc.cvtColor(image0, image1, Imgproc.COLOR_BGR2HSV);
        
        new File(img).getName();
        
        Imgcodecs.imwrite("qwer"+new File(img).getName(), image1);
        // 探测人脸
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image1, faceDetections);
        // rect中人脸图片的范围
        for (Rect rect : faceDetections.toArray()) {
            return new Mat(image1, rect);
        }
        return null;
        
        
    }

    public  double compare_image(String img_1, String img_2) throws IOException {
        URL systemResource = ClassLoader.getSystemResource("lib/opencv_java411.dll");
        System.load(systemResource.getPath());
        
        Mat mat_1 = conv_Mat(img_1);
        Mat mat_2 = conv_Mat(img_2);
        Mat hist_1 = new Mat();
        Mat hist_2 = new Mat();

        //颜色范围
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        //直方图大小， 越大匹配越精确 (越慢)
        MatOfInt histSize = new MatOfInt(1000*10000);

        Imgproc.calcHist(Arrays.asList(mat_1), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0), new Mat(), hist_2, histSize, ranges);

        // CORREL 相关系数CV_COMP_CORREL
        double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);
        return res;
    }
    
    @RequestMapping("rec")
    public double rec() throws IOException {
        URL systemResource = ClassLoader.getSystemResource("lib/opencv_java411.dll");
        System.load(systemResource.getPath());
        
        String path1 = "E:\\AllWorkspace\\eclipsePho\\opencv-learning-start-3-face-cut-plus\\7.jpg";
        String path2 = "E:\\AllWorkspace\\eclipsePho\\opencv-learning-start-3-face-cut-plus\\19999.jpg";
        
        File file = new File(path1);
        System.out.println(file.exists());
        
        File file2 = new File(path2);
        System.out.println(file2.exists());
        
//        double compareHist = compare_image(path1, path2);
        double compareHist =9527;
        System.out.println(compareHist);
        
        
        int ret;
        ret = compareHistogram(path1, path2);

        if (ret > 0) {
        System.out.println("相同.");
        } else {
        System.out.println("不同.");
        }
        
        return compareHist;
        
    }
    
    public static int compareHistogram(String filename1, String filename2) {
        int retVal = 0;

        long startTime = System.currentTimeMillis();

        URL systemResource = ClassLoader.getSystemResource("lib/opencv_java411.dll");
        System.load(systemResource.getPath());

        // Load images to compare
        Mat img1 = Imgcodecs.imread(filename1, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Mat img2 = Imgcodecs.imread(filename2, Imgcodecs.CV_LOAD_IMAGE_COLOR);

        Mat hsvImg1 = new Mat();
        Mat hsvImg2 = new Mat();

        // Convert to HSV
        Imgproc.cvtColor(img1, hsvImg1, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(img2, hsvImg2, Imgproc.COLOR_BGR2HSV);

            // Set configuration for calchist()
        List<Mat> listImg1 = new ArrayList<Mat>();
            List<Mat> listImg2 = new ArrayList<Mat>();
            
            listImg1.add(hsvImg1);
            listImg2.add(hsvImg2);
            
            MatOfFloat ranges = new MatOfFloat(0,255);
            MatOfInt histSize = new MatOfInt(50);
            MatOfInt channels = new MatOfInt(0);


            // Histograms
            Mat histImg1 = new Mat();
            Mat histImg2 = new Mat();
            
            // Calculate the histogram for the HSV imgaes
            Imgproc.calcHist(listImg1, channels, new Mat(), histImg1, histSize, ranges);
            Imgproc.calcHist(listImg2, channels, new Mat(), histImg2, histSize, ranges);
            
            Core.normalize(histImg1, histImg1, 0, 1, Core.NORM_MINMAX, -1, new Mat());
            Core.normalize(histImg2, histImg2, 0, 1, Core.NORM_MINMAX, -1, new Mat());


            // Apply the histogram comparison methods
            // 0 - correlation: the higher the metric, the more accurate the match "> 0.9"
            // 1 - chi-square: the lower the metric, the more accurate the match "< 0.1"
            // 2 - intersection: the higher the metric, the more accurate the match "> 1.5"
            // 3 - bhattacharyya: the lower the metric, the more accurate the match  "< 0.3"
            double result0, result1, result2, result3;
            result0 = Imgproc.compareHist(histImg1, histImg2, 0);
            result1 = Imgproc.compareHist(histImg1, histImg2, 1);
            result2 = Imgproc.compareHist(histImg1, histImg2, 2);
            result3 = Imgproc.compareHist(histImg1, histImg2, 3);
            
            System.out.println("Method [0] " + result0);
            System.out.println("Method [1] " + result1);
            System.out.println("Method [2] " + result2);
            System.out.println("Method [3] " + result3);
            
            // If the count that it is satisfied with the condition is over 3, two images is same.
            int count=0;
            if (result0 > 0.9) count++;
            if (result1 < 0.1) count++;
            if (result2 > 1.5) count++;
            if (result3 < 0.3) count++;
            System.out.println(count);
            if (count >= 3) retVal = 1;
            
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("estimatedTime=" + estimatedTime + "ms");

        return retVal;
        }
    
        public static void main(String[] args){
            int i=6,k=0;
            
            if(k==0){
             while(true){
             k++;
             System.out.print(i);
             }
            }
            else{
             System.out.print("error！");
           }

    }
}
