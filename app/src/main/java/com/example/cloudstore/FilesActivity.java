package com.example.cloudstore;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilesActivity extends AppCompatActivity {
    private static final String TAG = "FilesActivity";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    EditText textTitle, textDescription;
    private static final int FILE_RESULT = 42;
    FileInfo fileInfo;
    Button btnEncrypt, btnDecrypt;
    ImageView imageView;
    private String mTextDecrypt = "";
    private String mTextEncrypt = "";
    private static final int MY_PASSWORD_DIALOG_ID = 4;
    private EditText inputEncrypt,inputDecrypt;
    private String url;
    private HashMap<String, byte[]> mapUri;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FirebaseUtil.openFbReference("encryptedFiles");
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;

        textTitle = findViewById(R.id.ed_read_dialog);
        textDescription = findViewById(R.id.tvDescription);
        btnEncrypt = findViewById(R.id.btn_upload);
        btnDecrypt = findViewById(R.id.btn_read);
        imageView = findViewById(R.id.image);


        Intent intent = getIntent();
        FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("Files");

        if (fileInfo==null) {
            fileInfo = new FileInfo();
        }
        this.fileInfo = fileInfo;
        textTitle.setText(fileInfo.getTitle());
        textDescription.setText(fileInfo.getDescription());
        //showImage(fileInfo.getFileUrl());


        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEncrypt();

            }
        });

        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    dialogRead();
/*                    byte[] decrypted = decryptData(mapUri, mTextDecrypt);
                if (decrypted != null) {
                    Uri uriDecrypt = new Uri(decrypted);
                }*/

            }
        });

        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        btnEncrypt.setEnabled(true);
                        btnDecrypt.setEnabled(true);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Toast.makeText(FilesActivity.this, "You must enable permissions", Toast.LENGTH_SHORT).show();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_RESULT ) {
            Uri fileUri = null;
            if (data != null) {
                fileUri = data.getData();
                assert fileUri != null;
                final StorageReference reference = FirebaseUtil.storageReference.child(Objects.requireNonNull(fileUri.getLastPathSegment()));
                reference.putFile(fileUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
/*
                    String url = taskSnapshot.getStorage().getDownloadUrl().toString();
                    fileInfo.setFileUrl(url);
                    showImage(url);*/
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();
                                fileInfo.setFileUrl(uri.toString());
                                showImage(uri.toString());
                            }
                        });

                    }
                });
            }

        }

        if (requestCode == RESULT_OK ) {
            Uri fileUri = null;
            if (data != null) {
                fileUri = data.getData();
                assert fileUri != null;
                final StorageReference reference = FirebaseUtil.storageReference.child(Objects.requireNonNull(fileUri.getLastPathSegment()));
                reference.putFile(fileUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
/*
                    String url = taskSnapshot.getStorage().getDownloadUrl().toString();
                    fileInfo.setFileUrl(url);
                    showImage(url);*/
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                fileInfo.setFileUrl(uri.toString());
                                showImage(uri.toString());
                            }
                        });

                    }
                });
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id) {
            case R.id.save_menu:
                saveFile();
                Toast.makeText(this, "File Saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteFile();
                Toast.makeText(this, "File Info Deleted",Toast.LENGTH_SHORT).show();
                backToList();
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void saveFile() {
        fileInfo.setTitle(textTitle.getText().toString());
        fileInfo.setDescription(textDescription.getText().toString());


        if (fileInfo.getId() == null) {
            databaseReference.push().setValue(fileInfo);
        } else {
            databaseReference.child(fileInfo.getId()).setValue(fileInfo);
        }

    }

    private void deleteFile() {
        if (fileInfo==null) {
            Toast.makeText(this, "Please save the File Info before deleting",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        databaseReference.child(fileInfo.getId()).removeValue();
    }

    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent );
    }

    private void clean() {

        textTitle.setText("");
        textDescription.setText("");

        textTitle.requestFocus();
    }

    private void showImage(String url) {
        if (url != null && !url.isEmpty()){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width, width*3/3)
                    .centerCrop()
                    .into(imageView);
        }
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    public void dialogRead (){
        AlertDialog.Builder builder = new AlertDialog.Builder(FilesActivity.this);
        builder.setTitle("Enter Secret Code");

        // Set up the input
        inputDecrypt = new EditText(FilesActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputDecrypt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Set up the buttons
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                password =  fileInfo.getEncryptPassword();
                Log.d(TAG, "onCreate: password is " + password);
                if ((mTextDecrypt.equals(password))) {
                    //do something
                    //showImage(url);
                    showImage(fileInfo.getFileUrl());
                } else {
                    Toast.makeText(FilesActivity.this, "Wrong Password!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        inputDecrypt.setLayoutParams(lp);

        builder.setView(inputDecrypt);
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Initially disable the button
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                if (inputDecrypt.getText().toString().isEmpty()) {
                    inputDecrypt.setError("Field can't be empty!!");
                    ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });

        inputDecrypt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //validating input
                if (inputDecrypt.getText().toString().isEmpty()) {
                    inputDecrypt.setError("Field can't be empty!!");
                    // Disable ok button
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else if (inputDecrypt.getText().toString().length()<8 && !isValidPassword(inputDecrypt.getText().toString())) {
                    inputDecrypt.setError("Weak Password!!");
                    // Disable ok button
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Input validated. Enable the button.
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    mTextDecrypt = inputDecrypt.getText().toString();

                /*showImage(url);
                showImage(fileInfo.getFileUrl());*/
                }
            }
        });
    }

    public void dialogEncrypt (){
        AlertDialog.Builder builder = new AlertDialog.Builder(FilesActivity.this);
        builder.setTitle("Enter Secret Code");

        // Set up the input
        inputEncrypt = new EditText(FilesActivity.this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputEncrypt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Set up the buttons
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (inputEncrypt.length()<8 && inputEncrypt == null) {
                    Toast.makeText(FilesActivity.this, "Password Can't be empty!", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent,
                            "Insert File"), FILE_RESULT);
                }fileInfo.setEncryptPassword(inputEncrypt.getText().toString());



            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        inputEncrypt.setLayoutParams(lp);

        builder.setView(inputEncrypt);
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Initially disable the button
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (inputEncrypt.getText().toString().isEmpty()) {
                    inputEncrypt.setError("Field can't be empty!!");
                    ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });

        inputEncrypt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //validating input
                if (inputEncrypt.getText().toString().isEmpty()) {
                    inputEncrypt.setError("Field can't be empty!!");
                    // Disable ok button
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else if (inputEncrypt.getText().toString().length()<8 && !isValidPassword(inputEncrypt.getText().toString())) {
                    inputEncrypt.setError("Weak Password!!");
                    // Disable ok button
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Input validated. Enable the button.
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    mTextEncrypt = inputEncrypt.getText().toString();
                }
            }
        });
    }

    public void onClick(View view) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"franklinekihiu@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "Enter Subject Here");
        email.putExtra(Intent.EXTRA_TEXT, "Whats the message?");
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }
}
