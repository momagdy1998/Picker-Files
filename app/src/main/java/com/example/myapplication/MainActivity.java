package com.example.myapplication;

import static com.example.myapplication.Constants.PERMISSION_REQUEST_CODE;
import static com.example.myapplication.Constants.REQUEST_CODE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity implements PickerAttachmentsListener {
    private Button btnPickFile, btnLoadImage;
    private TextView tvFilePath;
    private ImageView imageView;
    private PickerAttachments pickerAttachments;
    private Uri selectedFileUri; // Store the selected file URI for later use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickerAttachments = new PickerAttachments(this);
        initViews();
        handleClicks();
    }

    private void initViews() {
        btnPickFile = findViewById(R.id.btn_picked_file);
        tvFilePath = findViewById(R.id.text_Path);
        imageView = findViewById(R.id.imageView);
        btnLoadImage = findViewById(R.id.btn_load_image);
    }

    // Check if READ_EXTERNAL_STORAGE permission is granted (for older versions)
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // For devices below Android 6.0 (API 23), no need for runtime permission request
    }

    private void handleClicks() {
        btnPickFile.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 (API 29) and above, use Storage Access Framework (SAF)
                openFilePickerUsingSAF();
            } else if (checkStoragePermission()) {
                // For devices below Android 10, check permission and open file picker
                pickerAttachments.chooseMedia(this, REQUEST_CODE);
            } else {
                // Request permission if not granted
                requestStoragePermission();
            }
        });
        btnLoadImage.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                // Use Glide to load the image from the URI into the ImageView
                loadImageIntoImageView(selectedFileUri);
            } else {
                tvFilePath.setText("No image selected.");
            }
        });

    }

    // Request READ_EXTERNAL_STORAGE permission
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission is granted, proceed to pick file
                pickerAttachments.chooseMedia(this, REQUEST_CODE);
            } else {
                // If permission is denied, show message
                tvFilePath.setText("Permission denied to read storage");
            }
        }
    }

    // Open file picker using SAF (for Android 10 and above)
    private void openFilePickerUsingSAF() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(Constants.TYPE);  // Open all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Log.d("PICKER-TEST", "openFilePickerUsingSAF: ");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get URI pointing to the file that was selected by the user
            Uri uri = data.getData();

            if (uri != null) {
                selectedFileUri = uri; // Store the selected file URI
                // Get file type (extension)
                String fileType = pickerAttachments.getFileType(uri, this);
                String filePath = pickerAttachments.saveFileToAppFolder(MainActivity.this, uri);

                Log.d("FIle-PATH", "FILE PATH IS = " + filePath);


                // Handle the picked file by copying it to a temporary file
                pickerAttachments.handlePickedFile(uri, fileType, this);
            }
        }
    }


    @Override
    public void onFilePicked(Uri uri, String filePath, String fileType) {
        tvFilePath.setText("File Path: " + filePath);
        Log.d("FILE-TYPE", "onFileTypePicked: " + fileType);
    }

    @Override
    public void onFileCopyError(String errorMessage) {
        tvFilePath.setText("Error: " + errorMessage);
    }

    private void loadImageIntoImageView(Uri uri) {
        // Using Glide to load the image into the ImageView
        Glide.with(this)
                .load(uri) // Load image from URI
                .into(imageView); // Set it into the ImageView
    }
}




