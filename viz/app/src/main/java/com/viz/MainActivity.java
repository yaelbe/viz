package com.viz;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.viz.model.EmotionData;
import com.viz.model.FaceRectangle;
import com.viz.utils.UploadFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private boolean check = true;
    private Button SelectImageGallery, UploadImageServer;
    private ImageView imageView;
    private ProgressDialog progressDialog;
    private Uri localPath;
    private String imageUrl = "http://s3.amazonaws.com/viz-test-yael/%s";
    private boolean noEmotions = false;

    private static String IP_ADDRESS = "ipAddress";
    private final static String AUDIO_RECORDER_FOLDER = "Android" + File.separator + "data";
    private final static String PACKAGE_NAME = App.getAppContext().getPackageName();
    private static final String TEMP_FILES_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + AUDIO_RECORDER_FOLDER +
            File.separator + PACKAGE_NAME;

    private String serverIPInput;
    private static final int SERVERPORT = 8888;
    private String SERVER_IP = "169.254.241.28";
    private Socket socket;
    private List<EmotionData> emotionList = new ArrayList<EmotionData>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = MainActivity.this.getSharedPreferences(
                "com.viz", Context.MODE_PRIVATE);
        serverIPInput = prefs.getString(SERVER_IP,"");

        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        SelectImageGallery = (Button) findViewById(R.id.buttonSelect);

        UploadImageServer = (Button) findViewById(R.id.buttonUpload);

        SelectImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();

                intent.setType("image/*");

                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);

                imageView.setImageResource(android.R.color.transparent);
                emotionList = new ArrayList<EmotionData>();
                noEmotions = false;

            }
        });


        UploadImageServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bitmap != null) {
                    ImageUploadToServerFunction();
                }else{
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setTitle("Ooops");
                    AlertDialog.Builder builder = alertBuilder.setMessage("You didn't select an image");
                    builder.show();
                }
            }
        });

        if (Build.VERSION.SDK_INT > 22) {
            String[] permissions = new String[]{Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

            ActivityCompat.requestPermissions(this, permissions, 123);
        }
        if(serverIPInput == null || serverIPInput.isEmpty()) {
            getServerIP();
        }else {
            new Thread(new ClientThread()).start();
        }

    }

    @Override
    protected void onActivityResult(int RC, int RQC, Intent I) {

        super.onActivityResult(RC, RQC, I);

        if (RC == 1 && RQC == RESULT_OK && I != null && I.getData() != null) {

            Uri uri = I.getData();
            localPath = uri;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
               drawRec();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    public void ImageUploadToServerFunction() {

        class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();
                progressDialog = ProgressDialog.show(MainActivity.this, "Image is Uploading", "Please Wait", false, false);
            }

            @Override
            protected void onPostExecute(String string1) {
                progressDialog.setMessage("Processing...");
                super.onPostExecute(string1);

            }

            @Override
            protected String doInBackground(Void... params) {

                InputStream inputStream;
                OutputStream outputStream;

                try {
                    ContentResolver content = App.getAppContext().getContentResolver();
                    inputStream = content.openInputStream(localPath);

                    File dir = new File(TEMP_FILES_PATH);
                    if (!dir.exists())
                        dir.mkdir();

                    String filename = System.currentTimeMillis() + ".jpg";
                    File file = new File(dir.getAbsolutePath(), File.separator + filename);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    outputStream = new FileOutputStream(file);

                    byte[] buffer = new byte[1000];
                    int bytesRead = 0;
                    while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                        outputStream.write(buffer, 0, buffer.length);
                    }

                    ImageTransferListener listener = new ImageTransferListener();
                    listener.fileName = filename;
                    UploadFile.uploadFileToS3(file.getPath(), listener);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "123";
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();

        AsyncTaskUploadClassOBJ.execute();
    }
    void drawRec (){
        if(progressDialog != null)
            progressDialog.dismiss();
        //Create a new image bitmap and attach a brand new canvas to it
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);

        //Draw the image bitmap into the cavas
        tempCanvas.drawBitmap(bitmap, 0, 0, null);

        if(emotionList!= null && emotionList.size()>0) {

            for(EmotionData current: emotionList) {
                Paint paint = new Paint();
                paint.setColor(Color.YELLOW);
                paint.setStrokeWidth(6);
                paint.setStyle(Paint.Style.STROKE);
                paint.setAntiAlias(true);
                paint.setTextSize(62f);

                FaceRectangle faceREctangle = current.getFaceRectangle();

                float top = Float.valueOf(faceREctangle.getTop());
                float left = Float.valueOf(faceREctangle.getLeft());
                float width = Float.valueOf(faceREctangle.getWidth());
                float height = Float.valueOf(faceREctangle.getHeight());
                tempCanvas.drawRect(new RectF(left,
                        top,
                        left+width,
                        top+height),
                        paint);

                paint.setColor(Color.GREEN);
                paint.setStrokeWidth(2);
                paint.setStyle(Paint.Style.FILL);
                tempCanvas.drawText(current.getScores().getStrongestEmotion(),
                        Float.valueOf(faceREctangle.getLeft()),
                        Float.valueOf(faceREctangle.getTop())-10f,
                        paint);
            }
        }
        else if(noEmotions){
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setTextSize(62f);
            tempCanvas.drawText("No emotion detected",
                    Float.valueOf(bitmap.getWidth()/4),
                    Float.valueOf(bitmap.getHeight()/2),
                    paint);
        }
        //Attach the canvas to the ImageView
        imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    private void getServerIP(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Were is your server ip?");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                serverIPInput = input.getText().toString();
                SharedPreferences prefs = MainActivity.this.getSharedPreferences(
                        "com.viz", Context.MODE_PRIVATE);
                prefs.edit().putString(SERVER_IP,serverIPInput).commit();

                new Thread(new ClientThread()).start();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

    }

    class ImageTransferListener implements TransferListener {
        String fileName;

        @Override
        public void onStateChanged(int id, TransferState state) {
            if (state.equals(TransferState.COMPLETED)) {

                imageView.setImageResource(android.R.color.transparent);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            String str = String.format(imageUrl, fileName);
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(str);

                            String responce = null;
                            InputStream input = socket.getInputStream();
                            int lockSeconds = 10 * 1000;

                            long lockThreadCheckpoint = System.currentTimeMillis();
                            int availableBytes = input.available();
                            while (availableBytes <= 0 && (System.currentTimeMillis() < lockThreadCheckpoint + lockSeconds)) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException ie) {
                                    ie.printStackTrace();
                                }
                                availableBytes = input.available();
                            }

                            byte[] buffer = new byte[availableBytes];
                            input.read(buffer, 0, availableBytes);
                            responce = new String(buffer);
                            responce = responce.replaceAll("\\\\", "");
                            responce = responce.replaceFirst("\"","");
                            Log.d("socket", responce);

                            Gson gson = new Gson();
                            JsonReader reader = new JsonReader(new StringReader(responce));
                            reader.setLenient(true);
                            Type type = new TypeToken<List<EmotionData>>(){}.getType();
                            emotionList = gson.fromJson(reader, type);


                            Log.d("socket", "responce " + responce);

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if(emotionList.isEmpty())
                                        noEmotions = true;
                                    drawRec();

                                }
                            });

                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }


        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            if (bytesTotal > 0) {
                float present = (bytesCurrent * 100 / bytesTotal);
                progressDialog.setMessage("Load " + String.format("%.2f", present) + "%");
                Log.d("Viz", "S3 load " + String.format("%.2f", present) + "%");
            } else {
                Log.d("Viz", "S3 load " + " size is zero");
            }
        }

        @Override
        public void onError(int id, Exception ex) {
            Log.d("Viz", "S3 load Erroe" + ex.toString());

        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {
            try {
//                socket = new Socket(SERVER_IP, SERVERPORT);
                socket = new Socket(serverIPInput, SERVERPORT);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}





