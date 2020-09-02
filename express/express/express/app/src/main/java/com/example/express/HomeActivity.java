package com.example.express;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private Button AddnewProduct;
    private EditText InputProductName,InputProductPrice,InputProductDescription;
    private ImageView InputProductImage;
    private String Description,Pname,Price,saveCurrentDate,saveCurrentTime;

    private static final int GalleryPick = 1;
    private Uri ImageUri;
    private String productRandomKey,downloadImageuri;
    private StorageReference ProductImagesRef;
    private DatabaseReference productReference;
    private ProgressDialog loadingBar ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ProductImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productReference =FirebaseDatabase.getInstance().getReference().child("Items");


        AddnewProduct = (Button)findViewById(R.id.new_add_product);
        InputProductImage = (ImageView)findViewById(R.id.select_product_image);
        InputProductName =(EditText)findViewById(R.id.product_name);
        InputProductDescription = (EditText)findViewById(R.id.product_description);
        InputProductPrice = (EditText)findViewById(R.id.product_price);
        loadingBar = new ProgressDialog(this);

        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                OpenGallery();
            }
        });

        AddnewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
             ValidateProductData();

            }


        });


    }



    private  void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            InputProductImage.setImageURI(ImageUri);

        }
    }
    private void ValidateProductData()
    {
        Description = InputProductDescription.getText().toString();
        Pname = InputProductName.getText().toString();
        Price = InputProductPrice.getText().toString();

        if(ImageUri == null)
        {
            Toast.makeText(this, "Product Image is Necessary", Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "PLease write product description", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Price))
        {
            Toast.makeText(this, "PLease write product Price", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Pname))
        {
            Toast.makeText(this, "PLease write product Name", Toast.LENGTH_SHORT).show();
        }
        else
        {
            StoreProductInformation();
        }










    }

private void StoreProductInformation()
{
    loadingBar.setTitle("Adding new Item");
    loadingBar.setMessage("Please wait,while we are Adding The New Item");
    loadingBar.setCanceledOnTouchOutside(false);
    loadingBar.show();
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");

    saveCurrentDate = currentDate.format((calendar).getTime());
    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss  a");
    saveCurrentTime = currentTime.format(calendar.getTime());

    productRandomKey = Pname + Price;


    final StorageReference  filePath = ProductImagesRef.child(ImageUri.getLastPathSegment()+productRandomKey+".jpg");
    final UploadTask uploadTask =  filePath.putFile(ImageUri);

    uploadTask.addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e)
        {
            String message = e.toString();
            Toast.makeText(HomeActivity.this, "Error"+ message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }
    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
        {
            Toast.makeText(HomeActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();

                    }
                    downloadImageuri = filePath.getDownloadUrl().toString();
                    return  filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if(task.isSuccessful())
                    {
                        downloadImageuri =task.getResult().toString();
                        Toast.makeText(HomeActivity.this, "Getting product Image url succesfully", Toast.LENGTH_SHORT).show();
                        SaveProductInfoToDatabase();
                    }
                }

            });
        }
    });


}

    private void SaveProductInfoToDatabase()
    {
        HashMap<String,Object>productMap = new HashMap<>();
        productMap.put("pid",productRandomKey);
        productMap.put("date",saveCurrentDate);
        productMap.put("time",saveCurrentTime);
        productMap.put("description",Description);
        productMap.put("image",downloadImageuri);
        productMap.put("price",Price);
        productMap.put("pname",Pname);
        productReference.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                       if(task.isSuccessful())
                       {
                           Intent intent = new Intent(HomeActivity.this,ShopKeeperDashBoardActivity.class);
                           startActivity(intent);
                           loadingBar.dismiss();
                           Toast.makeText(HomeActivity.this, "Item has been added succesfully", Toast.LENGTH_SHORT).show();
                       }
                       else
                       {
                           loadingBar.dismiss();
                           String message =task.getException().toString();
                           Toast.makeText(HomeActivity.this, "Error:"+message, Toast.LENGTH_SHORT).show();

                       }
                    }
                });
    }

}


