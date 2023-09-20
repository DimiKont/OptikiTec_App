package com.example.first_androidstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class ViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        PDFView pdfView = findViewById(R.id.pdfView);

        String filename = getIntent().getStringExtra("filename");

        String path = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + filename;
        File file = new File(path);

        pdfView.fromFile(file)
                .swipeHorizontal(true)
                .enableDoubletap(true)
                .enableAnnotationRendering(true)
                .defaultPage(0)
                .scrollHandle(null)
                .password(null)
                .load();

    }
}