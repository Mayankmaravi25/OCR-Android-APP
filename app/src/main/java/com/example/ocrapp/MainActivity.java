package com.example.ocrapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Locale;
import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private ImageView imageView;
    private Bitmap imageBitmap;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_view);
        Button buttonCapture = findViewById(R.id.button_capture);





        buttonCapture.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION);

            }
            else {
                captureImage();
            }
        });
        textToSpeech =new TextToSpeech(this, status ->{
            if(status == TextToSpeech.SUCCESS)
            {
                textToSpeech.setLanguage(Locale.US);

            }
            else {
                Log.e("TTS", "Initialization failed");
            }


        });

    }
    private void captureImage()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            recognizeTextFromImage();
        }
    }

    private void recognizeTextFromImage() {

        InputImage image = InputImage.fromBitmap(imageBitmap, 0);

        //change
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image).addOnSuccessListener(this::processTextRecognitionResult).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "failed to recognize text", Toast.LENGTH_SHORT).show();
            Log.e("OCR","Text recongnition failed: "+e.getMessage());

        });


    }
    private void processTextRecognitionResult(com.google.mlkit.vision.text.Text result) {
        String recognizedText = result.getText();
        if(!recognizedText.isEmpty())
        {
            textToSpeech.speak(recognizedText,TextToSpeech.QUEUE_FLUSH,null,null);

        }
        else {
            Toast.makeText(this,"No text found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if(textToSpeech != null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}