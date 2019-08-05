package com.shid.travelmantics;


import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    private static final int RC_SIGN_IN = 89;

    public static FirebaseDatabase mFirebaseDb;
    public static DatabaseReference mDbReference;
    private static FirebaseUtil firebaseUtil;
    public static FirebaseAuth firebaseAuth;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;
    public static FirebaseAuth.AuthStateListener authStateListener;

    public static ArrayList<TravelDeal> mDeals;
    public static ListActivity caller;
    public static boolean isAdmin;

    private FirebaseUtil() {
    }


    public static void openFbReference(String ref, final ListActivity callerActivity) {
        if (firebaseUtil == null) {
            firebaseUtil = new FirebaseUtil();
            mFirebaseDb = FirebaseDatabase.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    } else {
                        String userId = firebaseAuth.getUid();
                        Log.d("userID logged", userId);
                        checkAdmin(userId);
                    }

                    Toast.makeText(caller.getBaseContext(), "Welcome back", Toast.LENGTH_LONG).show();
                }
            };
            connectStorage();


        } else {
            String userId = firebaseAuth.getUid();
            Log.d("userID firebaseUti", userId);
            checkAdmin(userId);
        }
        mDeals = new ArrayList<>();
        mDbReference = mFirebaseDb.getReference().child(ref);
    }

    private static void checkAdmin(String uId) {
        FirebaseUtil.isAdmin = false;
        caller.showMenu();
        DatabaseReference reference = mFirebaseDb.getReference().child("administrators")
                .child(uId);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                caller.showMenu();
                Log.d("Admin", "You are an administrators");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        reference.addChildEventListener(listener);

    }

    public static void attachListener() {
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public static void detachListener() {
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public static void connectStorage() {
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference().child("deals_pictures");
    }

    public static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


        // Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);

    }
}
