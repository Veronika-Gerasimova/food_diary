package com.example.lab7.view.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab7.R;
import com.example.lab7.model.Message;
import com.example.lab7.controller.UnansweredQuestionsController;
import com.example.lab7.controller.UnansweredQuestionsCallback;

import java.sql.Connection;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private final List<Message> questions;
    private final Connection connection;
    private final Runnable onAnsweredCallback;
    private Context context;
    private UnansweredQuestionsController controller;

    public QuestionAdapter(List<Message> questions, Connection connection, Runnable onAnsweredCallback) {
        this.questions = questions;
        this.connection = connection;
        this.onAnsweredCallback = onAnsweredCallback;
        this.controller = new UnansweredQuestionsController();
    }

    @NonNull
    @Override
    public QuestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionAdapter.ViewHolder holder, int position) {
        Message message = questions.get(position);
        holder.usernameText.setText("От: " + message.getUserLogin());
        holder.questionText.setText(message.getMessageText());

        holder.itemView.setOnClickListener(v -> showAnswerDialog(v.getContext(), message));
    }

    private void showAnswerDialog(Context context, Message message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ответ пользователю: " + message.getUserLogin());

        final EditText input = new EditText(context);
        input.setHint("Введите ответ");
        builder.setView(input);

        builder.setPositiveButton("Отправить", (dialog, which) -> {
            String answerText = input.getText().toString();
            if (!answerText.isEmpty()) {
                // Получаем имя и фото нутрициолога из SharedPreferences
                android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
                String nutritionistName = prefs.getString("username", "");
                String nutritionistPhotoPath = prefs.getString("photo_path", "");
                controller.answerQuestion(context, message.getId(), answerText, nutritionistName, nutritionistPhotoPath, new UnansweredQuestionsCallback() {
                    @Override
                    public void onQuestionsLoaded(List<Message> questions) {}
                    @Override
                    public void onError(String errorMessage) {
                        ((AppCompatActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Ошибка при ответе: " + errorMessage, Toast.LENGTH_SHORT).show()
                        );
                    }
                    @Override
                    public void onAnswered() {
                        ((AppCompatActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Ответ отправлен", Toast.LENGTH_SHORT).show()
                        );
                        if (onAnsweredCallback != null) {
                            onAnsweredCallback.run();
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, questionText;

        ViewHolder(View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.textUserLogin);
            questionText = itemView.findViewById(R.id.textQuestion);
        }
    }
}
