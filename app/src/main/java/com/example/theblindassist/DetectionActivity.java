package com.example.theblindassist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.google.common.util.concurrent.ListenableFuture;
import org.pytorch.Module;

import java.io.*;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DetectionActivity extends AppCompatActivity {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    public final static String MESSAGE_KEY ="com.example.theblindassist.MESSAGE";
    private MediaPlayer mediaPlayer;

    PreviewView mPreviewView;
    Button captureImage;
    private Module module;
    private TextToSpeech tts;
    private Bitmap myBitmap;
    private int language;

    //Function to fetch the absolute path of Model.pt file from asset folder
    private static String fetchModelFile(Context context, String modelName) throws IOException {
        File file = new File(context.getFilesDir(), modelName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(modelName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        mPreviewView = findViewById(R.id.viewFinder);
        captureImage = findViewById(R.id.camera_capture_button);

        Intent gotIntent = getIntent();
        language = gotIntent.getIntExtra(MESSAGE_KEY,1);

        //Loading torchscript serialized module "model.pt" from assets folder
        try {
            module = Module.load(fetchModelFile(this, "model.pt"));
        } catch (IOException e) {
            Log.e("MainActivity", "Error loading module from assets", e);
            finish();
        }

//        String welcome_note;
        if(language==0){
            mediaPlayer = MediaPlayer.create(DetectionActivity.this,R.raw.hindi_detection_welcome);
            mediaPlayer.start();
        }
        else{
            mediaPlayer = MediaPlayer.create(DetectionActivity.this,R.raw.english_detection_welcome);
            mediaPlayer.start();
        }

        if(hasCameraPermission()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            requestPermission();
            if(hasCameraPermission()){
                startCamera();
            }
        }
    }


    private void startCamera() {
        //Requesting a camera provider
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        ImageCapture imageCapture = builder.build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);

        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback () {
                    @Override
                    public void onCaptureSuccess(ImageProxy image){
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        myBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);
                        if(myBitmap != null){
                            String denominationType = Inference.getDenominationType(myBitmap, module);
                            String toSpeak;
                            if(language == 0){
                                if(denominationType.equals("none")){
                                    toSpeak = "कोई भारतीय मुद्रा नहीं मिली, पुनः प्रयास करें";
                                }
                                else{
                                    toSpeak = "यह " + denominationType + " रुपये का नोट है";
                                }
                            }
                            else{
                                if(denominationType.equals("none")){
                                    toSpeak = "No Indian currency found, Try again";
                                }
                                else{
                                    toSpeak = "This is a " + denominationType + " rupee note";
                                }
                            }
                            tts = new TextToSpeech(DetectionActivity.this, new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    if (status == TextToSpeech.SUCCESS) {
                                        int result = tts.setLanguage(new Locale("hi_IN"));
                                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                            Log.e("MainActivity", "This Language is not supported");
                                        }
                                        int speakstatus= tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                                        Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                                        if(speakstatus == TextToSpeech.ERROR){
                                            Log.e("TTS", "Error in converting message to speech");
                                        }
                                    }
                                }
                            });
                            if(!denominationType.equals("none")){
                                addData(denominationType);
                            }
                        }
                        image.close();
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Log.e("MainActivity", "Image has not been captured", e);
                    }
                });
            }
        });
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Log.d("MainActivity", "Permission granted");
                startCamera();
            }
            else{
                Log.d("MainActivity", "Camera permission denied");
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    @Override
    public void onBackPressed() {
        tts.stop();
        tts.shutdown();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp(){
        tts.stop();
        tts.shutdown();
        Intent backIntent = new Intent(DetectionActivity.this, HomeActivity.class);
        backIntent.putExtra(MESSAGE_KEY,language);
        startActivity(backIntent);
        finish();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addData(String denominationType){
        DBHelper DB = new DBHelper(this);

//       final View myview = getLayoutInflater().inflate(R.layout.row_add_history,null,false);
//       layoutList.addView(myview);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");

        LocalDateTime now = LocalDateTime.now();
        String currentDate = df.format(now);
        String currentTime = tf.format(now);

        Boolean checkinsertdata = DB.insertuserdata(currentDate,currentTime,denominationType);

        DB.close();
    }
}