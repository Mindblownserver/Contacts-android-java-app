package com.kharrat.mycontacts;

import static android.Manifest.permission.READ_CONTACTS;

import android.Manifest;
import android.content.ContentResolver;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    private RecyclerView listContacts;
    private TextView searchField;

    private static final int REQUEST_READ_CONTACTS=1;
    private static final String CONTACT_PERMISSION = "Contact Permission";
    private static boolean READ_CONTACTS_GRANTED = false;
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
        ContentResolver contentResolver = getContentResolver();
        Cursor contactsCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection,null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

        // setting up adapter and recycler view
        ContactListAdapter contactListAdapter = new ContactListAdapter(this,contactsCursor);
        //contactListAdapter.changeCursor(contactsCursor);
        listContacts.setAdapter(contactListAdapter);
        listContacts.setLayoutManager(new LinearLayoutManager(this));

        // add TextChangedListener to searchField
        searchField.

    }
    private void checkPermission(){
        int hasContactsPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
        if(hasContactsPermission== PackageManager.PERMISSION_GRANTED){
            READ_CONTACTS_GRANTED=true;
            Log.d(CONTACT_PERMISSION, "Access granted");
        }else{
            ActivityCompat.requestPermissions(this,new String[]{READ_CONTACTS},REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_READ_CONTACTS:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    READ_CONTACTS_GRANTED = true;
                }else{
                    // handle
                }
                return;
        }
    }
}