package com.example.tosha.punme;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.InputStream;
import android.R.anim;
import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PictureActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SELECT_PICTURE = 2;
    static final int MY_PERMISSIONS_REQUEST_READ_GALLERY = 5;

    String mCurrentPhotoPath;
    File photoFile;
    static final boolean DEBUG = false;
    String pun = "Base";
    ProgressBar progressBar;
    //TextView mTextView = (TextView) findViewById(R.id.textView);



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Context context = (Context) getApplicationContext();
        File storageDir = context.getFilesDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        System.out.println(storageDir.toString());

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        System.out.println(mCurrentPhotoPath);
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private File dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        File photoFile = null;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go

            try {
                photoFile = createImageFile();
//                this.photoFile = photoFile;
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.println("Error");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                System.out.println("URI: " + photoURI);
                Log.d("PictureActivity", "URI: " + photoURI);


                /* ADD IN THIS NEXT LINE FOR OUTPUT TO FILE IN SD, SAVES FULL IMAGE */

                this.photoFile = photoFile;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO); // can add makescenetransistionanimation here for animation
                int enter_anim = anim.fade_in;
                int exit_anim = anim.fade_out;
                overridePendingTransition(enter_anim, exit_anim);
            }
        }
        //System.out.println("Photofile: " + photoFile.toString());
        Log.d("PictureActivity", "Photofile: " + photoFile.toString());
        this.photoFile = photoFile;

        Log.d("PictureActivity", "Global: " + this.photoFile);
        return photoFile;
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        System.out.println("Changed");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  Camera Action Returns
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            System.out.println("Returned");
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            if (this.photoFile == null) {
//                try {
//                    this.photoFile = createImageFile();
//                } catch (Exception e) {
//                    ;
//                }
//            }
//            FileOutputStream stream = null;
//            try {
//                stream = new FileOutputStream(photoFile);
//            } catch (Exception e) {
//                System.out.println(e);
//            }
//            if (stream != null)
//                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            else
//                System.out.println("Stream is null");
            try {
                ImageView mImageView = (ImageView) findViewById(R.id.image_display);
//            mImageView.setImageBitmap(imageBitmap);

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath(), opts);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

//            mImageView.setImageBitmap(rotated);

            /* Open up new activity */
                Intent intent = new Intent(this, DisplayActivity.class);
                intent.putExtra("Picture Activity Pun", photoFile);
                startActivity(intent);
            } catch (Exception e) {
                System.out.println(e);
                Context context = getApplicationContext();
                CharSequence text = "Oops! We ran into an issue. Try again please.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }

//            new FetchData().execute();
        }
        else if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            System.out.println("Choosing from gallery");

            Uri selectedImageUri = data.getData();
//            String[] projection = {MediaStore.Images.Media.DATA};
//            String path;
//            Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
//            if (cursor != null) {
//                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                cursor.moveToFirst();
//                path = cursor.getString(column_index);
//                cursor.close();
//            }
//            else {
//                path = selectedImageUri.getPath();
//            }
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            } catch (Exception e) {
                System.out.println(e);
            }
            try {
                photoFile = createImageFile();
//                this.photoFile = photoFile;
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.println("Error");
            }
            if (bitmap != null) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            else
                System.out.println("Error in creating bitmap");

            /* Open up new activity */
            Intent intent = new Intent(this, DisplayActivity.class);
            intent.putExtra("Picture Activity Pun", photoFile);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        System.out.println("Pun = " + pun);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        progressBar = (ProgressBar) findViewById(R.id.loading_panel);
//        progressBar.setMax(8);
//        progressBar.setVisibility(View.GONE);

        Button fab = (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("PictureActivity", "Camera button pressed.");
                dispatchTakePictureIntent();
            }
        });

        Button gallery = (Button) findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Picture Activity", "Gallery button");
                pickPicture();
            }
        });


        /* Request permissions */
        // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.READ_CONTACTS)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        MY_PERMISSIONS_REQUEST_READ_GALLERY);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_READ_GALLERY: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }

    private void pickPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture, menu);
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

        return super.onOptionsItemSelected(item);
    }


    private class FetchData extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            TextView mTextView = (TextView) findViewById(R.id.textView);

            mTextView.setText("Loading...");

            /* Open up new activity */
//            Intent intent = new Intent(this, DisplayActivity.class);
//            intent.putExtra("Picture Activity Pun", message);
//            startActivity(intent);

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }


        @Override
        protected String doInBackground(Void...params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String pun = "Base";

            MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpg");
            OkHttpClient client = new OkHttpClient();

            publishProgress(1);

            if (photoFile == null)
                System.out.println("it is null");

            publishProgress(2);
            /* Compressing the file size for sending */
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            try {
                FileInputStream inputStream = new FileInputStream(photoFile);

                Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                publishProgress(3);

                FileOutputStream outputStream = new FileOutputStream(photoFile);
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();

                selectedBitmap.recycle();

            } catch (Exception e) {
                System.out.println("Exception in compression: " + e.toString());
                System.out.println("Failed in compression");
            }

            publishProgress(4);


            System.out.println("Creating multi-part data with photofile: " + photoFile.getName());

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", photoFile.getName(), RequestBody.create(MEDIA_TYPE_JPEG, photoFile))
                    .build();

            publishProgress(5);

            if (requestBody == null)
                    System.out.println("Error");

            Request request = new Request.Builder()
                    .url("http://demo-antonytoron.boxfuse.io:8080/pun")
                    .post(requestBody)
                    .build();
            Response response = null;

            publishProgress(6);

//            int count = 0;
//            int maxTries = 3;
//            boolean breakOut = false;
//            while (!breakOut) {
//                try {
//                    response = client.newCall(request).execute();
//                } catch (Exception e) {
//                    if (++count == maxTries)
//                        breakOut = true;
//                    System.out.println("Exception: " + e);
//                }
//            }
            try {
                response = client.newCall(request).execute();
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }

            publishProgress(7);

            try {
                if (response != null) {
                    ResponseBody rb = response.body();
                    // DO NOT PRINT HERE ?
//                    if (rb != null)
//                        System.out.println("Body: " + response.body().string());
                    pun = response.body().string();

                    /* Make sure that no error/exception was returned with no pun */
                    JSONObject json = new JSONObject(pun);
                    if (!json.has("pun")) {
                        pun = null;
                    }

                    return pun;
                }
                else
                    System.out.println("Didn't work");

            } catch (Exception e) {
                System.out.println("Exception2: " + e);
            }

            publishProgress(7);


            String returnValue = null;
            if (DEBUG) {
                try {

                    System.out.println("In loop");
                    URL url = new URL("http://demo-antonytoron.boxfuse.io:8080/test");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    OutputStream outputStream = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(outputStream, "UTF-8"));


                    InputStream in = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    String content = null;
                    if (in != null)
                        reader = new BufferedReader(new InputStreamReader(in));
                    else
                        System.out.println("Was null");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        System.out.println("Was empty");
                    }
                    System.out.println("Content : " + buffer.toString());

                    return buffer.toString();
//                urlConnection.connect();
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println(e.getMessage());
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            System.out.println("Error closing stream");
                        }
                    }
                }
            }

            publishProgress(8);

            /* Make sure that no error/exception was returned with no pun */
            try {
                JSONObject json = new JSONObject(pun);
                if (!json.has("pun")) {
                    pun = null;
                }
            } catch (Exception e) {
                System.out.println("Picture Activity: Ran into some issue on server side.");
            }

            return pun;
        }


        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            // Do after asynctask returns
            System.out.println("Json: " + s);
            String setText = s;

            if (s == null) {
                setText = "Oops! Try again please later. We seem to have run into some issue";
            }

            TextView mTextView = (TextView) findViewById(R.id.textView);

            mTextView.setText(setText);


            progressBar.setVisibility(View.GONE);
//            photoFile.delete();

            pun = setText;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);

        }
    }
}
