package com.example.theblindassist;


import android.content.Context;
import android.graphics.Bitmap;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.*;

public class Inference {
    public static String getDenominationType(Bitmap bitmap, Module module){
        //Here we reshape the image into 400*400
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        //Input Tensor
        final Tensor input = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
        );

        //Calling the forward of the model to run our input
        final Tensor output = module.forward(IValue.from(input)).toTensor();


        final float[] score_arr = output.getDataAsFloatArray();

        // Fetch the index of the value with maximum score
        float max_score = -Float.MAX_VALUE;
        int ms_ix = -1;
        for (int i = 0; i < score_arr.length; i++) {
            if (score_arr[i] > max_score) {
                max_score = score_arr[i];
                ms_ix = i;
            }
        }

        //Fetching the name from the list based on the index
        String detected_class = ModelClasses.MODEL_CLASSES[ms_ix];

        return detected_class;
    }
}
