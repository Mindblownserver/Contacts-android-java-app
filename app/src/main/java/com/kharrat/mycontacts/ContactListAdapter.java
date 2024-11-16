package com.kharrat.mycontacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;

import java.util.Arrays;
import java.util.HashMap;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {
    public static final String CONTACT_LIST_ADAPTER = "ContactsListAdapter";

    public Cursor cursor;
    public Context ctx;
    private HashMap<Character,Integer> alphabetPositionMap = new HashMap<>(26);

    public ContactListAdapter(Context context, Cursor cursor){
        this.ctx = context;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.contact_item,parent,false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {

        //Log.d(CONTACT_LIST_ADAPTER, "cursor pos= "+position);
        if(cursor==null || !cursor.moveToPosition(position)){
            return;
        }
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
        char currentContactAlphabet = name.toUpperCase().charAt(0);
        String photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI));
        Integer newAlphaPosition = alphabetPositionMap.get(currentContactAlphabet);

        if(newAlphaPosition==null){
            alphabetPositionMap.put(currentContactAlphabet, position);
            holder.contactAlpha.setText(currentContactAlphabet+"");
            holder.contactAlpha.setBackgroundResource(R.drawable.new_alphabet_indicator);
            holder.contactAlpha.setTextSize(28);
            holder.contactCursorLayout.setBackgroundResource(R.drawable.first_alphabet_contact);
        }

        else if(newAlphaPosition==position){
            holder.contactAlpha.setText(currentContactAlphabet+"");
            holder.contactAlpha.setBackgroundResource(R.drawable.new_alphabet_indicator);
            holder.contactAlpha.setTextSize(28);
            holder.contactCursorLayout.setBackgroundResource(R.drawable.first_alphabet_contact);

        }
        else{
            holder.contactAlpha.setBackgroundResource(0);
            holder.contactAlpha.setTextSize(10);
            holder.contactCursorLayout.setBackgroundResource(0);
            holder.contactAlpha.setText("");
        }

        holder.contactCursName.setText(name);
        if(photoUri!=null){
            holder.displayContactPhoto(Uri.parse(photoUri), ctx.getContentResolver());
        }
        else{
            holder.contactCursPhoto.setImageResource(R.drawable.placeholder);
        }

    }

    @Override
    public int getItemCount() {
        return (cursor==null)?0:cursor.getCount();
    }

    public void changeCursor(Cursor newCursor){
        if(cursor!=null){
            cursor.close();
        }
        Log.d(CONTACT_LIST_ADAPTER, "Changed Cursor");
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        TextView contactAlpha;
        LinearLayout contactCursorLayout;
        TextView  contactCursName;
        ImageView contactCursPhoto;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactCursName = itemView.findViewById(R.id.contact_curs_name);
            contactCursPhoto = itemView.findViewById(R.id.profile_pic);
            contactAlpha = itemView.findViewById(R.id.contact_alpha);
            contactCursorLayout = itemView.findViewById(R.id.contact_curs_layout);

        }
        public void displayContactPhoto(Uri photoUri, ContentResolver contentResolver) {
            try {
                InputStream inputStream = contentResolver.openInputStream(photoUri);
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    contactCursPhoto.setImageBitmap(bitmap);
                    inputStream.close();
                }
            } catch (Exception e) {
                //Log.d(CONTACT_LIST_ADAPTER, "Couldn't load photo to bitmap: ",e);

            }
        }
    }

}
