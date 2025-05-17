package com.example.lab7.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab7.R;
import com.example.lab7.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messageList;
    private final String currentUserLogin;

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_NUTRITIONIST = 2;

    public MessageAdapter(List<Message> messageList, String currentUserLogin) {
        this.messageList = messageList;
        this.currentUserLogin = currentUserLogin;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getIsAnswer() == 0) {  // Вопрос пользователя
            return VIEW_TYPE_USER;
        } else { // Ответ нутрициолога
            return VIEW_TYPE_NUTRITIONIST;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_nutritionist, parent, false);
            return new NutritionistMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).textView.setText(message.getMessageText());
        } else if (holder instanceof NutritionistMessageViewHolder) {
            NutritionistMessageViewHolder nutritionistHolder = (NutritionistMessageViewHolder) holder;
            nutritionistHolder.textView.setText(message.getMessageText());
            nutritionistHolder.nutritionistName.setText(message.getNutritionistName());
            // Загрузка фото нутрициолога, например, через Glide или Picasso
            // Glide.with(nutritionistHolder.itemView).load(message.getNutritionistPhotoPath()).into(nutritionistHolder.nutritionistPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_message);
        }
    }

    static class NutritionistMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView nutritionistName;
        ImageView nutritionistPhoto;

        NutritionistMessageViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_message);
            nutritionistName = itemView.findViewById(R.id.nutritionistName);
            nutritionistPhoto = itemView.findViewById(R.id.nutritionistPhoto);
        }
    }
}

