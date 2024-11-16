package com.kharrat.mycontacts;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactItemClickListener extends RecyclerView.SimpleOnItemTouchListener {
    public interface onContactItemClickListener{
        void onItemCLick(View view, int position);
        void onItemLongCLick(View view, int position);
    }
    private final onContactItemClickListener listener;
    private final GestureDetector gestureDetector;

    public ContactItemClickListener(Context context, RecyclerView recyclerView, onContactItemClickListener l){
        this.listener = l;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {

                Log.d("Confirmed click", "You Clicked one time!");
                View contactItem = recyclerView.findChildViewUnder(e.getX(),e.getY());

                if(contactItem!=null && listener!=null){
                    listener.onItemCLick(contactItem, recyclerView.getChildAdapterPosition(contactItem));
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                Log.d("Long Click", "You took your sweet sweet time");
                View contactItem = recyclerView.findChildViewUnder(e.getX(),e.getY());
                if(contactItem!=null && listener!=null){
                    listener.onItemLongCLick(contactItem, recyclerView.getChildAdapterPosition(contactItem));
                }
                else{
                    Log.d("LongPress", "childView = null");
                }
                super.onLongPress(e);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        if(gestureDetector!=null){
            boolean result = gestureDetector.onTouchEvent(e);
            Log.d("Gesture Detector", ""+result);
            return result;
        }else{
            Log.d("Gesture Detector Null", "false");
            return false;
        }
    }
}
