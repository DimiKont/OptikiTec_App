package com.example.first_androidstudio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity
{

    // Username that is passed from LoginActivity
    private String username;
    private TextView textViewUsername;

    // Text Fields and Strings to get the values from the text fields
    private String widthStr;
    private String lengthStr;
    private String heightStr;
    private EditText editTextWidth;
    private EditText editTextLength;
    private EditText editTextHeight;

    // For the theme change
    SwitchCompat switchMode;
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    // The data that is going to be written in the PDF file,
    // and the name of the PDF file.
    private String data;
    private String filename;


    // All the button
    private Button buttonCreate;
    private Button buttonView;
    private Button buttonShare;
    private Button buttonClearAll;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Some code to prevent the app from crashing with the Share feature
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        // Initialize UI elements
        textViewUsername = findViewById(R.id.textViewUsername);
        editTextWidth = findViewById(R.id.editTextWidth);
        editTextLength = findViewById(R.id.editTextLength);
        editTextHeight = findViewById(R.id.editTextHeight);
        buttonCreate = findViewById(R.id.createButton);
        buttonView = findViewById(R.id.viewButton);
        buttonShare = findViewById(R.id.shareButton);
        buttonClearAll = findViewById(R.id.clearAllButton);
        switchMode = findViewById(R.id.switchMode);

        // Get the username from the LoginActivity, and set the text view to the username
        username = getIntent().getStringExtra("username");
        textViewUsername.setText(username);

        // Set onclick listeners
        switchMode.setOnClickListener(view -> changeTheme());
        buttonCreate.setOnClickListener(view -> createFile());
        buttonView.setOnClickListener(view -> openPDFActivity());
        buttonShare.setOnClickListener(view -> onShare());
        buttonClearAll.setOnClickListener(view -> resetAll(null));

        // Add text fields to text watcher listener
        editTextWidth.addTextChangedListener(textWatcher);
        editTextLength.addTextChangedListener(textWatcher);
        editTextHeight.addTextChangedListener(textWatcher);

        sharedPreferences = getSharedPreferences("nightModePrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        nightMode = getNightModeFromPrefs();
        switchMode.setChecked(nightMode);
        setAppNightMode(nightMode);
    }

    public void changeTheme()
    {
        nightMode = !nightMode;
        setAppNightMode(nightMode);

        editor.putBoolean("nightMode", nightMode);
        editor.apply();
    }

    private boolean getNightModeFromPrefs()
    {
        sharedPreferences = getSharedPreferences("nightModePrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("nightMode", false);
    }

    private void setAppNightMode(boolean isEnabled)
    {
        int nightModeFlag = isEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(nightModeFlag);
    }

    /**
     * Called when the user taps the View button
     */
    public void openPDFActivity()
    {
        Intent viewPDF = new Intent(MainActivity.this, ViewActivity.class);
        viewPDF.putExtra("filename", filename);
        startActivity(viewPDF);
    }

    /**
     * Called when the user taps the Create button. Creates a file and then enables the View button
     * Firstly, it gets the values from the text fields, then it creates a string with the data.
     */
    public void createFile()
    {
        getTextFields();

        data = "Width: " + widthStr + "\n" +
                "Length: " + lengthStr + "\n" +
                "Height: " + heightStr;

        // Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String dateStr = formatter.format(date);

        filename = username + "_" + dateStr + ".pdf";

        convertToPDF();
        buttonView.setEnabled(true);
        buttonShare.setEnabled(true);
    }


    public void convertToPDF()
    {
        String path = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + filename;
        File file = new File(path);

        boolean exists = file.exists();

        try
        {
            Document document = new Document(PageSize.A6);
            PdfWriter.getInstance(document, new FileOutputStream(file.getAbsoluteFile()));
            document.open();

            Paragraph paragraph = new Paragraph();
            paragraph.add(data);

            try
            {
                document.add(paragraph);
                document.close(); // Close the document here

                if (exists)
                {
                    Toast.makeText(this, "PDF Document has been overwritten", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, "PDF Document has been created", Toast.LENGTH_SHORT).show();
                }
            }
            catch (DocumentException de)
            {
                de.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        catch (DocumentException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Called when the user taps the Share button
     */
    public void onShare()
    {
        String path = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + filename;
        File file = new File(path);

        if(file.exists())
        {
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            share.setType("text/plain");
            startActivity(Intent.createChooser(share, "Share via"));
        }
    }

    private final TextWatcher textWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        /**
         * Overridden method to enable/disable button based on text field input
         */
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            updateButtonStates();
        }

        @Override
        public void afterTextChanged(Editable editable){}
    };

    public void getTextFields()
    {
        widthStr = editTextWidth.getText().toString().trim();
        lengthStr = editTextLength.getText().toString().trim();
        heightStr = editTextHeight.getText().toString().trim();
    }

    /**
     * Called when the user taps the Clear All button
     * @param view
     */
    public void resetAll(View view)
    {
        editTextWidth.getText().clear();
        editTextLength.getText().clear();
        editTextHeight.getText().clear();
    }

    /**
     * Called when the
     */
    private void updateButtonStates()
    {
        getTextFields();

        buttonCreate.setEnabled(!widthStr.isEmpty() && !lengthStr.isEmpty() && !heightStr.isEmpty());
        buttonClearAll.setEnabled(!widthStr.isEmpty() || !lengthStr.isEmpty() || !heightStr.isEmpty());
    }
}