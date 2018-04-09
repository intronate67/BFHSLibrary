package net.huntersharpe.bfhslibrary;


import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

class DatabaseManager {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbRef = database.getReference();

    private boolean result = false;
    private boolean existence = false;
    private boolean accountExists = false;

    String gUid = "";

    void checkoutBook(String barcode){
        if(!exists(barcode, "cob")){
            Log.i("Checkout", "Running");
            dbRef.child("cob").child(barcode).child("uid").setValue(gUid);
            DateFormat format = new SimpleDateFormat("MMddyyyy", Locale.US);
            String today = format.format(Calendar.getInstance().getTime());
            dbRef.child("cob").child(barcode).child("date").setValue(today);
        }else{
            Log.i("Checkout Existence", "Found!");
            return;
        }
        result = true;
    }

    void reserveBook(String barcode){
        if(!exists(barcode, "reservations")){
            Log.i("Reservation", gUid);
            dbRef.child("reservations").child(barcode).child("uid").setValue(gUid);
            DateFormat format = new SimpleDateFormat("ddMMyyyy", Locale.US);
            String today = format.format(Calendar.getInstance().getTime());
            dbRef.child("reservations").child(barcode).child("date").setValue(today);
            result = true;
        }else{
            Log.i("Reservation Existence", "Found!");
            result = false;
        }
    }

    void releaseBook(String barcode){
        if(exists(barcode, "reservations")){
            dbRef.child("reservations").child(barcode).removeValue();
            result = true;
        }else{
            result = false;
        }
    }

    int count = 0;
    private String[] dueBooks;
    String[] getDueBooks(){
        DatabaseReference ref = dbRef.child("profiles").child(gUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i = 0; i < Math.toIntExact(dataSnapshot.child("ccob").getChildrenCount()); i++){
                    dueBooks = new String[Math.toIntExact(dataSnapshot.child("ccob").getChildrenCount())];
                    dueBooks[i] = dataSnapshot.getChildren().iterator().next().getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return dueBooks;
    }

    void turnInBook(String barcode){

    }

    DatabaseReference getDbRef(){
        return dbRef;
    }

    boolean exists(final String value, final String path){
        Log.i("Existence", value);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.child(path).getChildren()){
                    existence = data.child(value).exists();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        return existence;
    }

    boolean exists(final String value, final String child, final String child1){
        Log.i("Existence", value);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.child(child).child(child1).getChildren()){
                    existence = data.child(value).exists();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        return existence;
    }

    boolean getResult(){
        return result;
    }
    void setUid(Context context){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        gUid = account.getId();
    }

    void setUpInDb(final GoogleSignInAccount account){
        DatabaseReference profRef = dbRef.child("profiles");
        profRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                accountExists = dataSnapshot.child(account.getId()).exists();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(!accountExists){
            profRef.child(account.getId()).child("ccob").setValue("none");
            profRef.child(account.getId()).child("email").setValue(account.getEmail());
        }
    }

}
