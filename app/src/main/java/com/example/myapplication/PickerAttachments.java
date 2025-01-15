package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PickerAttachments {
    private PickerAttachmentsListener listener;

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
            byte[] bytes = new byte[8192]; // Default buffer size
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
}
