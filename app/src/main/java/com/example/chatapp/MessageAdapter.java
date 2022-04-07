package com.example.chatapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {

    private List<Message> messages;
    private Activity activity;

    public MessageAdapter(Activity context, int resource, List<Message> messages) {
        super(context, resource, messages);
        this.messages = messages;
        this.activity = context;
    }

    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message2 = getItem(position);
        int layoutResource = 0;
        int viewType = getItemViewType(position);

        if (viewType == 0){
            layoutResource = R.layout.my_message_item;
        } else {
            layoutResource = R.layout.your_message_item;
        }

        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = layoutInflater.inflate(layoutResource,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        boolean isText = message2.getImageUrl() == null;
        if (isText){
            viewHolder.textViewMessage.setVisibility(View.VISIBLE);
            viewHolder.imgViewPhoto.setVisibility(View.GONE);
            viewHolder.textViewMessage.setText(message2.getText());
        } else {
            viewHolder.textViewMessage.setVisibility(View.GONE);
            viewHolder.imgViewPhoto.setVisibility(View.VISIBLE);
            Glide.with(viewHolder.imgViewPhoto.getContext())
                    .load(message2.getImageUrl())
                    .into(viewHolder.imgViewPhoto);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        Message message = messages.get(position);
        if (message.isMine()){
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private class ViewHolder{
        private TextView textViewMessage;
        private ImageView imgViewPhoto;

        public ViewHolder(View view){
            imgViewPhoto = view.findViewById(R.id.imgViewPhoto);
            textViewMessage = view.findViewById(R.id.textViewMessage);
        }
    }

}
