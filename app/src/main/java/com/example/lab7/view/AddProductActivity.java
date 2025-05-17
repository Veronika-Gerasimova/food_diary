package com.example.lab7.view;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab7.R;
import com.example.lab7.model.Product;
import com.example.lab7.controller.AddProductController;
import com.example.lab7.controller.AddProductCallback;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {
    private List<Product> products;
    private EditText inputCategoryId;
    private EditText inputDishName;
    private EditText inputCalories;
    private EditText inputProteins;
    private EditText inputFats;
    private EditText inputCarbohydrates;
    private EditText inputAmount;
    private AddProductController addProductController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        products = new ArrayList<>();
        inputDishName = findViewById(R.id.input_dish_name);
        inputCategoryId = findViewById(R.id.input_category_id_name);
        inputCalories = findViewById(R.id.input_calories);
        inputProteins = findViewById(R.id.input_proteins);
        inputFats = findViewById(R.id.input_fats);
        inputCarbohydrates = findViewById(R.id.input_carbohydrates);
        inputAmount = findViewById(R.id.input_amount);
        addProductController = new AddProductController();
    }

    public void addProductClick(View view) {
        addProductToDatabase();
    }

    private void addProductToDatabase() {
        String name = inputDishName.getText().toString();
        String caloriesStr = inputCalories.getText().toString();
        String proteinsStr = inputProteins.getText().toString();
        String fatsStr = inputFats.getText().toString();
        String carbohydratesStr = inputCarbohydrates.getText().toString();
        String amountStr = inputAmount.getText().toString();
        String categoryIdStr = inputCategoryId.getText().toString();

        if (caloriesStr.isEmpty() || proteinsStr.isEmpty() || fatsStr.isEmpty() ||
                carbohydratesStr.isEmpty() || amountStr.isEmpty() || categoryIdStr.isEmpty()) {
            return;
        }

        double calories = Double.parseDouble(caloriesStr);
        double proteins = Double.parseDouble(proteinsStr);
        double fats = Double.parseDouble(fatsStr);
        double carbohydrates = Double.parseDouble(carbohydratesStr);
        double amount = Double.parseDouble(amountStr);
        int categoryId = Integer.parseInt(categoryIdStr);

        addProductController.addProduct(this, name, categoryId, calories, proteins, fats, carbohydrates, amount, new AddProductCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    finish();
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    // Можно показать Toast с ошибкой
                });
            }
        });
    }
}