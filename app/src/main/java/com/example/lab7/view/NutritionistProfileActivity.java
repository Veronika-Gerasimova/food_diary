package com.example.lab7.view;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab7.R;
import com.example.lab7.controller.NutritionistProfileController;
import com.example.lab7.controller.NutritionistProfileCallback;

public class NutritionistProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private EditText nameEditText;
    private Uri imageUri;
    private String imagePath = "";
    private NutritionistProfileController nutritionistProfileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutritionist_profile);

        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.nameEditText);
        Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        Button saveAndContinueButton = findViewById(R.id.saveAndContinueButton);
        nutritionistProfileController = new NutritionistProfileController();

        uploadPhotoButton.setOnClickListener(v -> openGallery());
        saveAndContinueButton.setOnClickListener(v -> saveProfile());
        String login = getIntent().getStringExtra("login");
        if (login != null) {
            loadProfileIfExists(login);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            imagePath = getRealPathFromURI(imageUri);
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return "";
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void saveProfile() {
        String login = getIntent().getStringExtra("login");
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty() || imagePath.isEmpty()) {
            Toast.makeText(this, "Заполните все поля и выберите фото", Toast.LENGTH_SHORT).show();
            return;
        }
        nutritionistProfileController.saveProfile(this, login, name, imagePath, new NutritionistProfileCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(NutritionistProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(NutritionistProfileActivity.this, NutritionistActivity.class));
                    finish();
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(NutritionistProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onProfileLoaded(String name, String photoPath) {}
        });
    }

    private void loadProfileIfExists(String login) {
        nutritionistProfileController.loadProfileIfExists(this, login, new NutritionistProfileCallback() {
            @Override
            public void onSuccess(String message) {}
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(NutritionistProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onProfileLoaded(String name, String photoPath) {
                runOnUiThread(() -> {
                    nameEditText.setText(name);
                    profileImageView.setImageURI(Uri.parse(photoPath));
                    imagePath = photoPath;
                    Toast.makeText(NutritionistProfileActivity.this, "Профиль загружен", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
