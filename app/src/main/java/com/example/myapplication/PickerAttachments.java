package com.example.myapplication;

import static com.example.myapplication.Constants.BUFFER_SIZE;
import static com.example.myapplication.Constants.SAVED_FILES_PREFIX;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsProvider;
import android.webkit.MimeTypeMap;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PickerAttachments {
    private final PickerAttachmentsListener listener;

    // Constructor to initialize the listener
    public PickerAttachments(PickerAttachmentsListener listener) {
        this.listener = listener;
    }
    public void chooseMedia(AppCompatActivity activity, int requestCode) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                "text/plain",
                "application/pdf",
                "application/zip", "application/vnd.android.package-archive"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // Launch the picker screen
        activity.startActivityForResult(intent, requestCode);
    }
    public String getFileType(Uri uri, Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeType = contentResolver.getType(uri);
        return mimeTypeMap.getExtensionFromMimeType(mimeType);
    }
    public void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            byte[] bytes = new byte[BUFFER_SIZE]; // Default buffer size
            int read;
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.onFileCopyError(e.getMessage());
            }
        }
    }
    public void handlePickedFile(Uri uri, String fileType,Context context) {
        try {
            // Create a temporary file to hold the content of the actual file
            File file = File.createTempFile("file_", fileType);

            // Copy content from the actual file to the temporary file using InputStream
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                copyInputStreamToFile(inputStream, file);
                inputStream.close(); // Close the input stream after use
            }

            // Callback to notify that the file is picked successfully
            if (listener != null) {
                listener.onFilePicked(uri, file.getAbsolutePath(), fileType);
            }

        } catch (IOException e) {
            if (listener != null) {
                listener.onFileCopyError("Failed to copy file: " + e.getMessage());
            }
        }
    }
    public String saveFileToAppFolder(Context context ,Uri uri) {
        try {
            // Get the content resolver
            ContentResolver contentResolver = context.getContentResolver();

            // Open input stream for the picked file
            InputStream inputStream = contentResolver.openInputStream(uri);

            if (inputStream != null) {
                // Prepare output stream for the app's internal storage
                File appFolder = new File(context.getFilesDir(), SAVED_FILES_PREFIX);
                if (!appFolder.exists()) {
                    appFolder.mkdir(); // Create directory if it doesn't exist
                }

                // Create a unique file name
                String fileName = "picked_file_" + System.currentTimeMillis() +"."+ getFileType(uri,context); // You can choose file extension based on the file type
                File outputFile = new File(appFolder, fileName);
                OutputStream outputStream = new FileOutputStream(outputFile);

                // Buffer to read and write the file data
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                // Close streams
                inputStream.close();
                outputStream.close();

                // Return the saved file path
                return outputFile.getAbsolutePath();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Return null if there was an error
    }
}
