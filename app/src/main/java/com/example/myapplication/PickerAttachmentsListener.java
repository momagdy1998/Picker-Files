package com.example.myapplication;

import android.net.Uri;

public interface PickerAttachmentsListener {
    void onFilePicked(Uri uri, String filePath, String fileType);
    void onFileCopyError(String errorMessage);
}
