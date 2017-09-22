package com.viz.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.viz.App;

import java.io.File;

public class UploadFile {

    private static AmazonS3 mAmazonS3 = new AmazonS3Client(new MyAWSCredentials());
    private static TransferUtility mTransferUtility = new TransferUtility(mAmazonS3, App.getAppContext());
    private static String mBucketName ="viz-test-yael";

    public static void uploadFileToS3(String localFilePath ,TransferListener listener){

        File file = new File(localFilePath);
        if(!file.exists()){
            return;
        }

        final TransferObserver observer = mTransferUtility.upload(
                mBucketName,     /* The bucket to upload to */
                file.getName(),    /* The key for the uploaded object */
                file        /* The file where the data to upload exists */
        );

        observer.setTransferListener(listener);

    }

    static class MyAWSCredentials implements AWSCredentials {

        @Override
        public String getAWSAccessKeyId() {
            return "AKIAJXUR44V3LL5PFBJQ";
        }

        @Override
        public String getAWSSecretKey() {
            return "pG0p52JJitPI0waDBTUHfzh9rvbH2aTHGb1M3aGF";
        }
    }
}
