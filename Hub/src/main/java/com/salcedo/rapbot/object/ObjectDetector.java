package com.salcedo.rapbot.object;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.Arrays;

public final class ObjectDetector {
    public static void main( String[] args )
    {
        System.out.println(Arrays.toString(System.getProperty("java.library.path").split(":")));
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
        System.out.println( "mat = " + mat.dump() );
    }
}
