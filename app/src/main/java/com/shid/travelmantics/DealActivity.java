package com.shid.travelmantics;


import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    private EditText txt_title;
    private EditText txt_price;
    private EditText txt_description;
    private TravelDeal deal;
    private ImageView imageView;
    private static final int PICTURE_RESULT =42;

    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mDbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        firebaseSetup();
        setUpUi();

        final Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        txt_title.setText(deal.getTitle());
        txt_description.setText(deal.getDescription());
        txt_price.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        Button btn_image = findViewById(R.id.btn_image);
        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/jpeg");
                intent1.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent1,PICTURE_RESULT);
            }
        });
    }

    private void setUpUi() {
        txt_title = findViewById(R.id.txt_title);
        txt_price = findViewById(R.id.txt_price);
        txt_description = findViewById(R.id.txt_description);
        imageView = findViewById(R.id.imageTravel);
    }

    private void firebaseSetup() {
        mFirebaseDb = FirebaseUtil.mFirebaseDb;
        mDbReference = FirebaseUtil.mDbReference;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(DealActivity.this, "Deal Saved!", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;

            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(DealActivity.this, "Deal Deleted!", Toast.LENGTH_LONG).show();
                backToList();
            default:
                return super.onOptionsItemSelected(item);
        }

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

    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDbReference.child(deal.getId()).removeValue();
        Log.d("Image Name", deal.getImageName());
        if (deal.getImageName() != null && !deal.getImageName().isEmpty()){
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image",e.getMessage());
                }
            });
        }
    }

    private void backToList() {
        Intent intent = new Intent(DealActivity.this, ListActivity.class);
        startActivity(intent);
    }

    private void clean() {
        txt_title.setText("");
        txt_description.setText("");
        txt_price.setText("");
        txt_title.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        txt_title.setEnabled(isEnabled);
        txt_description.setEnabled(isEnabled);
        txt_price.setEnabled(isEnabled);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);

        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.btn_image).setEnabled(true);

        } else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.btn_image).setEnabled(false);
        }
        return true;
    }
}