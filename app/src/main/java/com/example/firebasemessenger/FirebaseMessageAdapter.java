package com.example.firebasemessenger;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

// ArrayAdapter является адаптером, который предназначен для работы с элементами списка типа ListView и им подобным.
public class FirebaseMessageAdapter extends ArrayAdapter<MessageModel> {
    public FirebaseMessageAdapter(Context context, int resource,
                                  List<MessageModel> messages) {
        super(context, resource, messages);
    }

/*     В методе getView() устанавливается отображение элемента списка
     ConvertView - View нашего элемента MessageModel */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity)getContext()).getLayoutInflater().inflate
                    (R.layout.message_item, parent, false);
        }

        ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
        TextView textTextView = convertView.findViewById(R.id.textTextView);
        TextView nameTextView = convertView.findViewById(R.id.nameTextView);

        MessageModel message = getItem(position);

        boolean isText = message.getImageUrl() == null;

        if (isText) {
            textTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            textTextView.setText(message.getText());
        } else {
            textTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext()).load(message.getImageUrl())
                    .into(photoImageView);
        }

        nameTextView.setText(message.getName());

        return convertView;
    }
}
