package com.kharrat.mycontacts;


import android.Manifest;
import android.content.ContentResolver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements ContactItemClickListener.onContactItemClickListener{
    // TODO: End goal: add a button to save calls in case you needed it!
    private RecyclerView listContacts;
    private ContactListAdapter contactListAdapter;
    private TextView searchField;
    private HandlerThread dbThread;
    private Handler dbHandler;
    private ContentResolver contentResolver;

    private static final int REQUEST_READ_CONTACTS=1;
    private static final int REQUEST_MAKE_CALLS = 2;
    private static boolean MAKE_CALLS_GRANTED = false;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Check permission
        checkPermission();

        listContacts = findViewById(R.id.list_contacts);
        searchField = findViewById(R.id.search_contacts_field);

        // Load exisiting contacts
        String[] projection  = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts._ID, ContactsContract.Contacts.PHOTO_URI};
        contentResolver = getContentResolver();
        Cursor contactsCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection,null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

        // setting up adapter and recycler view
        contactListAdapter = new ContactListAdapter(this,contactsCursor);
        //contactListAdapter.changeCursor(contactsCursor);
        listContacts.setAdapter(contactListAdapter);
        listContacts.addOnItemTouchListener(new ContactItemClickListener(this, listContacts, this));
        listContacts.setLayoutManager(new LinearLayoutManager(this));

        // add TextChangedListener to searchField
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dbHandler.post(()-> queryContacts(charSequence.toString(),contentResolver));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDbThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDbThread();
    }

    public void startDbThread(){
        if(dbThread==null||!dbThread.isAlive()){
            dbThread = new HandlerThread("ContactsDatabase");
            dbThread.start();
            dbHandler = new Handler(dbThread.getLooper());
        }
    }

    public void stopDbThread(){
        if(dbThread!=null){
            dbHandler.removeCallbacksAndMessages(null);
            dbThread.quitSafely();
            try{
                dbThread.join();
                dbThread=null;
                dbHandler=null;
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    public void queryContacts(String s, ContentResolver cr){
        String[] projection  = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts._ID, ContactsContract.Contacts.PHOTO_URI};
        String sorting = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;
        String where = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ? AND "+ ContactsContract.Contacts.HAS_PHONE_NUMBER+" = ?";
        String[] whereCrit = {"%"+s+"%", "1"};
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, where, whereCrit,sorting);
        runOnUiThread(()->{
            contactListAdapter.changeCursor(cursor);
        });
    }

    private void checkPermission(){
        int hasContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int hasCallPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if(hasContactsPermission!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_CONTACTS},REQUEST_READ_CONTACTS);
        }
        if(hasCallPermission !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_MAKE_CALLS);
        }else{
            MAKE_CALLS_GRANTED = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_READ_CONTACTS:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d("PERMISSION", "read contacts permission granted");
                }else{
                    // handle
                }
                return;
            case REQUEST_MAKE_CALLS:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    MAKE_CALLS_GRANTED = true;
                }else{
                    //handle
                }
                return;
        }
    }

    @Override
    public void onItemCLick(View view, int position) {
        if(MAKE_CALLS_GRANTED){
            // make calls
            if(!contactListAdapter.cursor.moveToPosition(position)){
                return;
            }
            int contactId= contactListAdapter.cursor.getInt(contactListAdapter.cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String[] prj = {ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone._ID};
            String where = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?";
            String[] whereCrit = {contactId+""};
            Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,prj,where,whereCrit,null);
            if(phoneCursor.moveToNext()){

                Intent i = new Intent();
                i.setAction(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:" + phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))));
                startActivity(i);
            }
            //contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone._ID,ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null,null);
        }else{
            Toast.makeText(this, "Please enable make calls permission", Toast.LENGTH_LONG).show();
            checkPermission();
        }
    }

    @Override
    public void onItemLongCLick(View view, int position) {

    }
}