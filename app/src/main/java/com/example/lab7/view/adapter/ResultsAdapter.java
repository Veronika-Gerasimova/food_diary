package com.example.lab7.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab7.R;
import com.example.lab7.view.ViewResultsActivity;
import com.example.lab7.view.WeightLossChartDialog;
import com.example.lab7.model.WeightLossResult;

import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

    private final List<WeightLossResult> results;
    private final ViewResultsActivity activity;

    public ResultsAdapter(List<WeightLossResult> results, ViewResultsActivity activity) {
        this.results = results;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result_with_recommendation, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        WeightLossResult result = results.get(position);

        holder.userLogin.setText("Пользователь: " + result.getUserLogin());
        holder.weightLoss.setText("Потеря веса: " + result.getWeightLoss() + " кг");
        holder.waistLoss.setText("Уменьшение талии: " + result.getWaistLoss() + " см");
        holder.otherNotes.setText("Примечания: " + result.getOtherNotes());

        holder.sendRecommendationButton.setOnClickListener(v -> {
            String recommendation = holder.recommendationInput.getText().toString().trim();
            if (!recommendation.isEmpty()) {
                // Вызов метода sendRecommendation из ViewResultsActivity
                activity.sendRecommendation(result.getId(), result.getUserLogin(), recommendation);
                holder.recommendationInput.setText(""); // Очищаем поле после отправки
            } else {
                Toast.makeText(activity, "Введите рекомендацию", Toast.LENGTH_SHORT).show();
            }
        });
        holder.viewChartButton.setOnClickListener(v -> showChartDialog(result.getUserLogin()));
    }
    private void showChartDialog(String userLogin) {
        WeightLossChartDialog chartDialog = new WeightLossChartDialog(activity, userLogin);
        chartDialog.show();
    }
    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView userLogin, weightLoss, waistLoss, otherNotes;
        EditText recommendationInput;
        Button sendRecommendationButton, viewChartButton;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            userLogin = itemView.findViewById(R.id.text_user_login);
            weightLoss = itemView.findViewById(R.id.text_weight_loss);
            waistLoss = itemView.findViewById(R.id.text_waist_loss);
            otherNotes = itemView.findViewById(R.id.text_other_notes);
            recommendationInput = itemView.findViewById(R.id.edit_recommendation);
            sendRecommendationButton = itemView.findViewById(R.id.button_send_recommendation);
            viewChartButton = itemView.findViewById(R.id.button_view_chart);
        }
    }
}
