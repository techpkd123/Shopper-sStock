package com.example.express;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProductDetailsActivity extends AppCompatActivity {
    private Button  Delbtn;
    private ImageView imageView;
    private FloatingActionButton add;
    private TextView productPrice,productDescriptioin,productName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        imageView =(ImageView)findViewById(R.id.product_image_details);

                Delbtn=(Button)findViewById(R.id.del_btn);
        add=(FloatingActionButton)findViewById(R.id.addbtn);
                productPrice=(TextView )findViewById(R.id.productdet);
        productDescriptioin=(TextView )findViewById(R.id.proddes);
                productName=(TextView )findViewById(R.id.product_price);
                Delbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

    }
}
