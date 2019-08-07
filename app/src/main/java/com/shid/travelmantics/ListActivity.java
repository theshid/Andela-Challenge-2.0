package com.shid.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.thekhaeng.recyclerviewmargin.LayoutMarginDecoration;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    /*
    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mDbReference;
    private ChildEventListener mChildListener;
    */
    ScrollView rootLayout;
    private TravelDeal travelDeal;
    private static final int PICTURE_RESULT =43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        rootLayout = findViewById(R.id.root_layout);

    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFbReference("traveldeals", this);
        RecyclerView rvDeals = findViewById(R.id.rvDeals);

        rvDeals.addItemDecoration(new LayoutMarginDecoration(1, 20));
        final DealAdapter adapter = new DealAdapter();
        rvDeals.setAdapter(adapter);
        LinearLayoutManager dealsLayoutManager =
                //Check
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvDeals.setLayoutManager(dealsLayoutManager);
        FirebaseUtil.attachListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);
        MenuItem insertMenu = menu.findItem(R.id.insert_menu);
        if (FirebaseUtil.isAdmin) {
            insertMenu.setVisible(true);
        } else {

            insertMenu.setVisible(false);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_menu:
               // showNewDealDialog();
                Intent intent = new Intent(ListActivity.this, NewDealActivity.class);
                startActivity(intent);
                return true;

            case R.id.logout_menu:
                logOut();
                FirebaseUtil.detachListener();


                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle("Logging out");

        alertDialogBuilder
                .setMessage(getString(R.string.dialog_log_out_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_log_out_btn_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AuthUI.getInstance()
                                .signOut(ListActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // ...
                                        Log.d("Logout", "User Logged out");
                                        FirebaseUtil.attachListener();
                                    }
                                });
                    }
                })
                .setNegativeButton(getString(R.string.dialog_log_out_btn_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void showMenu() {
        invalidateOptionsMenu();
    }

    private void showNewDealDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ADD NEW DEAL");
        dialog.setMessage("Please enter travel deal information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View travelDeal_layout = inflater.inflate(R.layout.layout_new_deal, null);

        final MaterialEditText edtTitle = travelDeal_layout.findViewById(R.id.edtTitle);
        final MaterialEditText edtDescription = travelDeal_layout.findViewById(R.id.edtDescription);
        final MaterialEditText edtPrice = travelDeal_layout.findViewById(R.id.edtPrice);
        final Button btnDeal = travelDeal_layout.findViewById(R.id.btn_image);


        travelDeal = new TravelDeal();

        btnDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/jpeg");
                intent1.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent1,PICTURE_RESULT);
            }
        });

        dialog.setView(travelDeal_layout);

        dialog.setPositiveButton("Add New Deal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //

                if (TextUtils.isEmpty(edtTitle.getText().toString())
                || TextUtils.isEmpty(edtDescription.getText().toString())
                || TextUtils.isEmpty(edtPrice.getText().toString())) {
                    Toast.makeText(getApplicationContext(),"Please do not let field empty",Toast.LENGTH_LONG)
                            .show();


                }

                /*
                if (TextUtils.isEmpty(edtDescription.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter the description", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }

                if (TextUtils.isEmpty(edtPrice.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter the price", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }
                */
                dialogInterface.dismiss();
            }
        });


        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
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
                    travelDeal.setImageName(pictureName);
                    Log.d("Name",pictureName);
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            Log.d("Url",url);
                            travelDeal.setImageUrl(url);
                          //  showImage(url);
                        }
                    });

                }
            });
        }
    }

  /*  private void showImage(String url){
        if (url != null && !url.isEmpty()){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into();

        }
    }
    */
}
