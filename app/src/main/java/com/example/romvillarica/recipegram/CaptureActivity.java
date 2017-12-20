package com.example.romvillarica.recipegram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class CaptureActivity extends AppCompatActivity {
    @BindView(R.id.cancel_button)
    Button cancelButton;
    @BindView(R.id.upload_picture1_button)
    Button loadPicture1Button;
    @BindView(R.id.upload_picture2_button)
    Button loadPicture2Button;
    @BindView(R.id.take_picture1_button)
    Button takePicture1Button;
    @BindView(R.id.take_picture2_button)
    Button takePicture2Button;
    @BindView(R.id.save_post_button)
    Button savePostButton;
    @BindView(R.id.picture1_frame)
    ImageView picture1Frame;
    @BindView(R.id.picture2_frame)
    ImageView picture2Frame;
    @BindView(R.id.instructions)
    EditText instructionsEditText;
    @BindView(R.id.description)
    EditText descriptionEditText;

    final int REQUEST_TAKE_PHOTO = 1;
    String picture1Name;
    String picture1FilePath;
    String picture2Name;
    String picture2FilePath;
    String username;
    int PICTURE_BEING_HANDLED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        ButterKnife.bind(this);
        username = getIntent().getStringExtra("username");

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),MainActivity.class);
                i.putExtra("username",username);
                startActivity(i);
            }
        });

        loadPicture1Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //intent.setType("image/*");
                startActivityForResult(intent, 821);
                PICTURE_BEING_HANDLED = 1;
            }
        });

        takePicture1Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PICTURE_BEING_HANDLED = 1;
                //this here takes a photo
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(v.getContext(),
                                "com.example.android.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

        loadPicture2Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, 821);
                PICTURE_BEING_HANDLED = 2;
            }
        });

        takePicture2Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PICTURE_BEING_HANDLED = 2;
                //this here takes a photo
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(v.getContext(),
                                "com.example.android.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

        savePostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //proceed only if there are filepaths
                if (picture1FilePath == null || picture2FilePath == null) {
                    Toast.makeText(v.getContext(), "Please upload a Before and After photo to make a post!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (instructionsEditText.getText().toString().equals("") || descriptionEditText.getText().toString().equals("") ) {
                    Toast.makeText(v.getContext(), "Please fill out the Description and Instructions fields!", Toast.LENGTH_LONG).show();
                    return;
                }
                final View v1 = v;
                final String image1Name = picture1Name;
                File file1 = new File(picture1FilePath); // file path on Android system
                final String image2Name = picture2Name;
                File file2 = new File(picture2FilePath); // file path on Android system
                RequestBody reqFile1 = RequestBody.create(MediaType.parse("image/*"), file1);
                final RequestBody reqFile2 = RequestBody.create(MediaType.parse("image/*"), file2);
                RedisService.getService().postImage(image1Name, reqFile1).enqueue(new Callback<RedisService.SetResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.SetResponse> call, retrofit2.Response<RedisService.SetResponse> response) {
                        RedisService.getService().postImage(image2Name, reqFile2).enqueue(new Callback<RedisService.SetResponse>() {
                            @Override
                            public void onResponse(Call<RedisService.SetResponse> call, retrofit2.Response<RedisService.SetResponse> response) {
                                // Adding .jpg to the end informs Webdis of the correct content encoding
                                ListItem lToUpload = new ListItem();
                                String uniqueId = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                lToUpload.setNameOfPoster(username);
                                lToUpload.setUniqueId(uniqueId);
                                lToUpload.setDate(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));
                                lToUpload.setDescription(descriptionEditText.getText().toString());
                                lToUpload.setInstructions(instructionsEditText.getText().toString());
                                String url1 = "http://ec2-54-210-25-34.compute-1.amazonaws.com/879a6589b75ce798f587e68ab76c8237a9/GET/" + image1Name + ".jpg";
                                String url2 = "http://ec2-54-210-25-34.compute-1.amazonaws.com/879a6589b75ce798f587e68ab76c8237a9/GET/" + image2Name + ".jpg";
                                lToUpload.setImage1Url(url1);
                                lToUpload.setImage2Url(url2);
                                UserPost up = new UserPost();
                                up.setListItem(lToUpload);
                                //upload the ListItem
                                RedisService.getService().makePost(uniqueId,up).enqueue(new Callback<RedisService.SetResponse>() {
                                    @Override
                                    public void onResponse(Call<RedisService.SetResponse> call, retrofit2.Response<RedisService.SetResponse> response) {
                                        Intent i = new Intent(v1.getContext(),MainActivity.class);
                                        i.putExtra("username",username);
                                        startActivity(i);
                                    }
                                    @Override
                                    public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                                        Toast.makeText(v1.getContext(), t.toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            @Override
                            public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                                Toast.makeText(v1.getContext(), t.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                        Toast.makeText(v1.getContext(), t.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 821 && data != null && PICTURE_BEING_HANDLED != 0) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Uri selectedImage = data.getData();
            if (PICTURE_BEING_HANDLED == 1) {
                picture1Frame.setImageURI(selectedImage);
                String[] temp = ImageFilePath.getPath(this,data.getData()).split("/");
                picture1Name = username + timeStamp + temp[temp.length-1].replace(".jpg","");
                //Toast.makeText(this, picture1Name, Toast.LENGTH_LONG).show();
                picture1FilePath = ImageFilePath.getPath(this, data.getData());
            }
            else if (PICTURE_BEING_HANDLED == 2) {
                picture2Frame.setImageURI(selectedImage);
                String[] temp = ImageFilePath.getPath(this,data.getData()).split("/");
                picture2Name = username + timeStamp + temp[temp.length-1].replace(".jpg","");
                //Toast.makeText(this, picture2Name, Toast.LENGTH_LONG).show();
                picture2FilePath = ImageFilePath.getPath(this, data.getData());
            }
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            ImageView pictureFrame;
            String photoFilePath;
            if (PICTURE_BEING_HANDLED == 1) {
                pictureFrame = picture1Frame;
                photoFilePath = picture1FilePath;
            }
            else {
                pictureFrame = picture2Frame;
                photoFilePath = picture2FilePath;

            }
            // Get the dimensions of the View
            int targetW = pictureFrame.getWidth();
            int targetH = pictureFrame.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoFilePath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath, bmOptions);
            pictureFrame.setImageBitmap(bitmap);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = username + timeStamp + "image";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        if (PICTURE_BEING_HANDLED == 1) {
            picture1Name = imageFileName;
            picture1FilePath = image.getAbsolutePath();
            return image;
        }
        else {
            picture2Name = imageFileName;
            picture2FilePath = image.getAbsolutePath();
            return image;
        }
    }
}
