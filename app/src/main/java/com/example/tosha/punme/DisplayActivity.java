package com.example.tosha.punme;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DisplayActivity extends AppCompatActivity {
    ProgressBar progressBar;
    File photoFile;
    Bitmap photoFileBitmap;
    boolean photoFileDeleted = false;
    TextView textView;
    String pun;
    final static boolean DEBUG = false;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        photoFileDeleted = false;

        progressBar = (ProgressBar) findViewById(R.id.loading_panel);
        progressBar.setMax(8);
        progressBar.setVisibility(View.GONE);

        this.photoFile = (File) getIntent().getExtras().get("Picture Activity Pun");

        Button saveButton = (Button) findViewById(R.id.save_image);
        Button retryButton = (Button) findViewById(R.id.try_again);

        saveButton.setEnabled(false);
        retryButton.setEnabled(false);

        final Context context = getApplicationContext();

        this.textView = (TextView) findViewById(R.id.pun_display);
        this.imageView = (ImageView) findViewById(R.id.image_display);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DisplayActivity", "Save button pressed.");
                saveImage(photoFile, view, textView, imageView, context);
            }
        });

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DisplayActivity", "Retry button pressed.");
                retry(photoFile, view);
            }
        });


        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission granted");
            } else {
                System.out.println("Permission revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            System.out.println("Permission granted");
        }






        new FetchData().execute();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //resume tasks needing this permission
            System.out.println("Permission granted after result");
            return;
        }
    }

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
        return image;
    }

    private void saveImage(File photoFile, View view, TextView textView, ImageView imageView, Context context) {
        System.out.println("Trying to save");

        int width = 0;
        int height = 0;
        Bitmap cs;

        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);
        Bitmap background = this.photoFileBitmap;
        if (textView == null)
            System.out.println("It's null");

        System.out.println(textView.getText().toString());

        textView.setDrawingCacheEnabled(true);
        textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
        textView.buildDrawingCache(true);

        Bitmap foreground = Bitmap.createBitmap(textView.getDrawingCache());
        textView.setDrawingCacheEnabled(false);
        comboImage.drawBitmap(background, 0, 0, null);
        comboImage.drawBitmap(foreground, 0, 0, null);

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        FileOutputStream saveOutput = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File saveFile = new File(context.getFilesDir(), imageFileName);

            if (!saveFile.exists())
                saveFile.getParentFile().mkdirs();


            saveOutput = new FileOutputStream(saveFile);
            cs.compress(Bitmap.CompressFormat.JPEG, 100, saveOutput);
            saveOutput.close();

//            Uri contentUri = Uri.fromFile(this.photoFile);
//            Uri contentUri = Uri.fromFile(myPath);
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    saveFile);

            System.out.println(photoURI.toString());

            mediaScanIntent.setData(photoURI);
            mediaScanIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            DisplayActivity.this.setResult(Activity.RESULT_OK, mediaScanIntent);
            this.sendBroadcast(mediaScanIntent);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(saveFile), "image/jpeg");
            mediaScanIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            DisplayActivity.this.setResult(Activity.RESULT_OK, mediaScanIntent);
            startActivity(intent);

        } catch (Exception e) {
            System.out.println("Save to internal storage" + e);
        } finally {
            try {
                saveOutput.close();
            } catch (Exception e) {
                System.out.println("Error" + e);
            }
        }

        /*
        try {
            this.photoFile = createImageFile();
//                this.photoFile = photoFile;
        } catch (IOException ex) {
            // Error occurred while creating the File
            System.out.println("Error");
        }
        // Continue only if the File was successfully created
        if (this.photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    this.photoFile);
            System.out.println("URI: " + photoURI);
            Log.d("PictureActivity", "URI: " + photoURI);
        }
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
*/

        /* Saving the file */
        /*
        try {

            FileOutputStream outputStream = new FileOutputStream(this.photoFile);
            cs.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Uri contentUri = Uri.fromFile(this.photoFile);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            MediaStore.Images.Media.insertImage(getContentResolver(), cs, this.photoFile.getName(), timeStamp);


        } catch (Exception e) {
            System.out.println("Display Activity: Issue saving to file");
        }
        */

        Context appContext = getApplicationContext();
        CharSequence text = "Your photo has been saved in " + photoFile.getPath() + "!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(appContext, text, duration);
        toast.show();

        textView.setVisibility(view.GONE);
        imageView.setVisibility(view.GONE);


        this.finish();
    }

    private void retry(File photoFile, View view) {
        System.out.println("Trying to reset");


        this.finish();

//        dispatchTakePictureIntent();
    }


    /* No need for below code so far */

    private File dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                System.out.println("URI: " + photoURI);
                Log.d("DisplayActivity", "URI: " + photoURI);


                /* ADD IN THIS NEXT LINE FOR OUTPUT TO FILE IN SD, SAVES FULL IMAGE */

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO); // can add makescenetransistionanimation here for animation
            }
        }
        Log.d("DisplayActivity", "Photofile: " + photoFile.toString());

        return photoFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  Camera Action Returns
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                ImageView mImageView = (ImageView) findViewById(R.id.image_display);
                TextView mTextView = (TextView) findViewById(R.id.pun_display);
//            mImageView.setImageBitmap(imageBitmap);


            /* Hide while loading */
                mImageView.setVisibility(View.GONE);
                mTextView.setVisibility(View.GONE);


                new FetchData().execute();
            } catch (Exception e) {
                System.out.println("Display Activity: " + e.toString());
                Context context = getApplicationContext();
                CharSequence text = "Oops! We ran into an issue. Try again please.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }



    /* In case need to post at this screen */
    private class FetchData extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);

            Button saveButton = (Button) findViewById(R.id.save_image);
            Button retryButton = (Button) findViewById(R.id.try_again);

            saveButton.setEnabled(false);
            retryButton.setEnabled(false);
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
                System.out.println("Exception in compression in display: " + e.toString());
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

            /* Create the textView */
            TextView punDisplay = (TextView) findViewById(R.id.pun_display);
//            for (int index = setText.indexOf())

            punDisplay.setText(setText);




            punDisplay.setVisibility(View.VISIBLE);




            ImageView imageDisplay = (ImageView) findViewById(R.id.image_display);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath(), opts);


            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            /* Save the photofile bitmap */
            photoFileBitmap = rotated;
            imageDisplay.setImageBitmap(rotated);

            progressBar.setVisibility(View.GONE);
            imageDisplay.setVisibility(View.VISIBLE);

            Button saveButton = (Button) findViewById(R.id.save_image);
            Button retryButton = (Button) findViewById(R.id.try_again);

            saveButton.setEnabled(true);
            retryButton.setEnabled(true);
//            photoFile.delete();

            /* Delete the photo file */
            if (!photoFileDeleted) {
                photoFile.delete();
                photoFileDeleted = true;
            }

            pun = setText;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);

        }
    }



}
