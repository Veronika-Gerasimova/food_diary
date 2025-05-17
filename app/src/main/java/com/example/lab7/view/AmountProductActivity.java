package com.example.lab7.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab7.R;
import com.example.lab7.model.Product;

public class AmountProductActivity extends AppCompatActivity {
        private TextView selectedProductTextView;
        private EditText quantityEditText;
        private Button saveButton;

        private Product selectedProduct;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_amount_product);

            selectedProductTextView = findViewById(R.id.selected_product_textview);
            quantityEditText = findViewById(R.id.quantity_edittext);
            saveButton = findViewById(R.id.save_button);

            selectedProduct = (Product) getIntent().getSerializableExtra("product");
            selectedProductTextView.setText(selectedProduct.getName());
        }

    public void saveProduct(View view) {
        double quantity = Double.parseDouble(quantityEditText.getText().toString());
        Intent intent = new Intent();
        intent.putExtra("product", selectedProduct);
        intent.putExtra("quantity", quantity);
        setResult(RESULT_OK, intent);
        finish();
    }
}
