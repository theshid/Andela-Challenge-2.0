package com.shid.travelmantics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class NewDealActivity extends AppCompatActivity {

    private EditText txt_title;
    private EditText txt_price;
    private EditText txt_description;
    private TravelDeal deal;
    private ImageView imageView;
    private Button btn_image;
    private Button btn_save;

    private static final int PICTURE_RESULT =42;

    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mDbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_deal);

        this.setFinishOnTouchOutside(false);

        firebaseSetup();
        setUpUi();

        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/jpeg");
                intent1.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent1,PICTURE_RESULT);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate()){
                    saveFailed();
                    return;
                }
                saveDeal();
                Toast.makeText(NewDealActivity.this,getString(R.string.deal_saved) , Toast.LENGTH_LONG).show();
                clean();
                backToList();
            }
        });
    }

    private void firebaseSetup() {
        mFirebaseDb = FirebaseUtil.mFirebaseDb;
        mDbReference = FirebaseUtil.mDbReference;
    }

    private void setUpUi() {
        txt_title = findViewById(R.id.edtTitle);
        txt_price = findViewById(R.id.edtPrice);
        txt_description = findViewById(R.id.edtDescription);
        imageView = findViewById(R.id.imageTravel);
        btn_image = findViewById(R.id.btn_image);
        btn_save = findViewById(R.id.btn_save);
        deal = new TravelDeal();
    }

    private void saveDeal() {

        deal.setTitle(txt_title.getText().toString());
        deal.setPrice(txt_price.getText().toString());
        deal.setDescription(txt_description.getText().toString());
        if (deal.getId() == null) {
            mDbReference.push().setValue(deal);
        } else {
            mDbReference.child(deal.getId()).setValue(deal);
        }

    }

    public boolean validate() {
        boolean valid = true;

        String title = txt_title.getText().toString();
        String description = txt_description.getText().toString();
        String price = txt_price.getText().toString();

        if (title.isEmpty() ) {
            txt_title.setError("Do not let field empty");
            valid = false;
        } else {
            txt_title.setError(null);
        }


        if (description.isEmpty() ) {
            txt_description.setError("Do not let field empty");
            valid = false;
        } else {
            txt_description.setError(null);
        }

        if (price.isEmpty() ) {
            txt_price.setError("Do not let field empty");
            valid = false;
        } else {
            txt_price.setError(null);
        }

        return valid;
    }

    private void backToList() {
        Intent intent = new Intent(NewDealActivity.this, ListActivity.class);
        startActivity(intent);
    }

    private void clean() {
        txt_title.setText("");
        txt_description.setText("");
        txt_price.setText("");
        txt_title.requestFocus();
    }

    private void showImage(String url){
        if (url != null && !url.isEmpty()){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(imageView);

        }
    }

    private void saveFailed(){
        Toast.makeText(this,"Save failed check errors",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final StorageReference reference = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            reference.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageName(pictureName);
                    Log.d("Name",pictureName);
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            Log.d("Url",url);
                            deal.setImageUrl(url);
                            showImage(url);
                        }
                    });

                }
            });
        }
    }

}
