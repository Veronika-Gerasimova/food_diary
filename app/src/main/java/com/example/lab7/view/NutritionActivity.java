package com.example.lab7.view;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;

import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab7.R;
import com.example.lab7.model.Category;
import com.example.lab7.model.Product;
import com.example.lab7.database.ConnectionClass;
import com.example.lab7.database.DatabaseCleanupReceiver;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NutritionActivity extends AppCompatActivity {
    private TextView caloriesFirst;
    private TextView consumedСalories;
    private TextView kbzhuTextView;
    private TextView rskTextView;
    private TextView count;
    private TableLayout tableLayout;
    private List<Product> addedProducts = new ArrayList<>();
    private Spinner categorySpinner;
    private Spinner productSpinner;
    private TextView summary;
    private PieChart pieChart;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        caloriesFirst = findViewById(R.id.left_calories_textView);
        kbzhuTextView = findViewById(R.id.kbzhu_textView);
        rskTextView = findViewById(R.id.rsk_textview);
        count = findViewById(R.id.title_textView);
        tableLayout = findViewById(R.id.product_table);
        categorySpinner = findViewById(R.id.category_spinner);
        productSpinner = findViewById(R.id.product_spinner);
        consumedСalories = findViewById(R.id.consumed_calories_textView);
        summary = findViewById(R.id.summary_textView);

        pieChart = findViewById(R.id.pieChart);

        double calories = getIntent().getDoubleExtra("calories", 0.0);
        double protein = getIntent().getDoubleExtra("protein", 0.0);
        double fat = getIntent().getDoubleExtra("fat", 0.0);
        double carbs = getIntent().getDoubleExtra("carbs", 0.0);

        //caloriesFirst.setText(String.format("Осталось калорий: %.2f", calories));
        rskTextView.setText(String.format("РСК: %.2f", calories));
        count.setText(String.format("К: %.2f; Б: %.2f г; Ж: %.2f г; У: %.2f г", calories, protein, fat, carbs));

        loadCategories();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        setDailyDatabaseCleanup(this);
    }


    private void loadCategories() {
        AsyncTask<Void, Void, List<Category>> task = new AsyncTask<Void, Void, List<Category>>() {
            @Override
            protected List<Category> doInBackground(Void... voids) {
                List<Category> categories = new ArrayList<>();
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    ConnectionClass connectionClass = new ConnectionClass();
                    con = connectionClass.CONN();

                    if (con != null) {
                        String sql = "SELECT * FROM categories";
                        statement = con.prepareStatement(sql);
                        resultSet = statement.executeQuery();

                        while (resultSet.next()) {
                            int id = resultSet.getInt("id");
                            String name = resultSet.getString("name");
                            Category category = new Category(id, name);
                            categories.add(category);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                        if (statement != null) {
                            statement.close();
                        }
                        if (con != null) {
                            con.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return categories;
            }

            @Override
            protected void onPostExecute(List<Category> categories) {
                super.onPostExecute(categories);
                List<String> categoryNames = new ArrayList<>();
                categoryNames.add("");
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(NutritionActivity.this, android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
                categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String categoryName = (String) parent.getItemAtPosition(position); // Получить имя выбранной категории
                        Category selectedCategory = findCategoryByName(categoryName); // Найти объект Category по имени
                        if (selectedCategory != null) {
                            loadProducts(selectedCategory);
                        }
                    }
                    private Category findCategoryByName(String categoryName) {
                        for (Category category : categories) {
                            if (category.getName().equals(categoryName)) {
                                return category;
                            }
                        }
                        return null; // Handle the case where the category is not found
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });
            }
        };

        task.execute();
    }
    private void loadProducts(Category selectedCategory) {
        AsyncTask<Category, Void, List<Product>> task = new AsyncTask<Category, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(Category... categories) {
                Category category = categories[0];
                List<Product> products = new ArrayList<>();
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    ConnectionClass connectionClass = new ConnectionClass();
                    con = connectionClass.CONN();

                    if (con != null) {
                        String sql = "SELECT * FROM products WHERE category_id = ?";
                        statement = con.prepareStatement(sql);
                        statement.setInt(1, category.getId());
                        resultSet = statement.executeQuery();

                        while (resultSet.next()) {
                            String productName = resultSet.getString("name");
                            double calories = resultSet.getDouble("calories");
                            double protein = resultSet.getDouble("protein");
                            double fat = resultSet.getDouble("fat");
                            double carbohydrates = resultSet.getDouble("carbohydrates");
                            double amount = resultSet.getDouble("amount");

                            Product product = new Product(productName, calories, protein, fat, carbohydrates, amount);
                            products.add(product);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                        if (statement != null) {
                            statement.close();
                        }
                        if (con != null) {
                            con.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return products;
            }

            @Override
            protected void onPostExecute(List<Product> products) {
                super.onPostExecute(products);
                List<String> productNames = new ArrayList<>();
                productNames.add("");
                for (Product product : products) {
                    productNames.add(product.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(NutritionActivity.this, android.R.layout.simple_spinner_item, productNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                productSpinner.setAdapter(adapter);

                productSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String productName = (String) parent.getItemAtPosition(position);
                        Product selectedProduct = findProductByName(productName);
                        if (selectedProduct != null) {
                            // Проверяем, что продукт выбран, и только после этого открываем AmountProductActivity
                            openAmountActivity(selectedProduct);
                        }
                    }

                    private Product findProductByName(String productName) {
                        for (Product product : products) {
                            if (product.getName().equals(productName)) {
                                return product;
                            }
                        }
                        return null;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });
            }
        };

        task.execute(selectedCategory);
    }
    private static final int REQUEST_ADD_PRODUCT = 1;

    private void openAmountActivity(Product selectedProduct) {
        Intent intent = new Intent(this, AmountProductActivity.class);
        intent.putExtra("product", selectedProduct);
        startActivityForResult(intent, REQUEST_ADD_PRODUCT);
    }

    private void updateSummary() {
        double totalKcal = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCarbs = 0;

        for (Product product : addedProducts) {
            totalKcal += product.getKcal() * product.getAmount() / 100;
            totalProtein += product.getProtein() * product.getAmount() / 100;
            totalFat += product.getFat() * product.getAmount() / 100;
            totalCarbs += product.getCarbs() * product.getAmount() / 100;
        }

        kbzhuTextView.setText(String.format("К: %.2f г; Б: %.2f г; Ж: %.2f г; У: %.2f г", totalKcal, totalProtein, totalFat, totalCarbs));
        consumedСalories.setText(String.format("Употреблено: %.2f", totalKcal));

        try {
            // Получаем числовые значения из текстовых полей и заменяем запятую на точку
            String rskStr = rskTextView.getText().toString().split(":")[1].trim().replace(",", ".");
            String consumedStr = consumedСalories.getText().toString().split(":")[1].trim().replace(",", ".");

            double rsk = Double.parseDouble(rskStr);
            double consumed = Double.parseDouble(consumedStr);

            // Вычисляем оставшиеся калории
            double remainingCalories = rsk - consumed;
            caloriesFirst.setText(String.format("Осталось калорий: %.2f", remainingCalories));

            if (remainingCalories < 0) {
                summary.setText("Вы превысили дневную норму калорий");
                summary.setTextColor(Color.RED);
            } else {
                summary.setText("");
                summary.setTextColor(Color.BLACK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            summary.setText("Ошибка при вычислении калорий");
            summary.setTextColor(Color.RED);
        }

        updatePieChart(pieChart);
    }
    public void addProduct(Product product, double quantity) {
        product.setAmount(quantity);
        addedProducts.add(product);
        updateSummary();
        saveDailyNutritionToDatabase(product, quantity);
        //updateTable(product, quantity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Product product = (Product) data.getSerializableExtra("product");
            double quantity = data.getDoubleExtra("quantity", 0.0);
            addProduct(product, quantity);
            updateTable(product, quantity);
        }
    }
    private void updateTable(Product product, double quantity) {
        TableLayout tableLayout = findViewById(R.id.product_table);
        TableRow row = new TableRow(this);

        // Создаем TextView для каждого столбца
        TextView name = new TextView(this);
        name.setText(product.getName());
        name.setGravity(Gravity.CENTER);
        name.setPadding(8, 8, 8, 8);
        row.addView(name);

        TextView kcal = new TextView(this);
        kcal.setText(String.format("%.2f", product.getKcal() * quantity / 100));
        kcal.setGravity(Gravity.CENTER);
        kcal.setPadding(8, 8, 8, 8);
        row.addView(kcal);

        TextView protein = new TextView(this);
        protein.setText(String.format("%.2f", product.getProtein() * quantity / 100));
        protein.setGravity(Gravity.CENTER);
        protein.setPadding(8, 8, 8, 8);
        row.addView(protein);

        TextView fat = new TextView(this);
        fat.setText(String.format("%.2f", product.getFat() * quantity / 100));
        fat.setGravity(Gravity.CENTER);
        fat.setPadding(8, 8, 8, 8);
        row.addView(fat);

        TextView carbs = new TextView(this);
        carbs.setText(String.format("%.2f", product.getCarbs() * quantity / 100));
        carbs.setGravity(Gravity.CENTER);
        carbs.setPadding(8, 8, 8, 8);
        row.addView(carbs);

        TextView amount = new TextView(this);
        amount.setText(String.valueOf(quantity));
        amount.setGravity(Gravity.CENTER);
        amount.setPadding(8, 8, 8, 8);
        row.addView(amount);

        // Устанавливаем границы для строки
        row.setBackgroundResource(R.drawable.table_row_border);

        // Добавляем обработчик нажатия для удаления строки
        row.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Удалить продукт")
                    .setMessage("Вы уверены, что хотите удалить продукт?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        // Удаляем строку из таблицы
                        tableLayout.removeView(row);
                        // Удаляем продукт из списка
                        addedProducts.remove(product);
                        // Удаляем из базы данных
                        deleteProductFromDatabase(product);
                        // Обновляем сводку
                        updateSummary();
                        // Обновляем диаграмму
                        updatePieChart(pieChart);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        // Добавляем строку в таблицу
        tableLayout.addView(row);
    }

    // Метод для удаления из базы данных
    private void deleteProductFromDatabase(Product product) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userLogin = prefs.getString("username", null);

        if (userLogin == null) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Connection con = null;
            PreparedStatement statement = null;
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                con = connectionClass.CONN();

                if (con == null) {
                    runOnUiThread(() -> Toast.makeText(NutritionActivity.this, "Нет соединения с базой данных", Toast.LENGTH_SHORT).show());
                    return;
                }

                String sql = "DELETE FROM daily_nutrition WHERE user_login = ? AND product_name = ? AND date = CURDATE()";
                statement = con.prepareStatement(sql);
                statement.setString(1, userLogin);
                statement.setString(2, product.getName());

                int result = statement.executeUpdate();
                if (result > 0) {
                    runOnUiThread(() -> {
                        Toast.makeText(NutritionActivity.this, "Продукт удален", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(NutritionActivity.this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } finally {
                try {
                    if (statement != null) statement.close();
                    if (con != null) con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void buttonClick(View view) {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    private void updatePieChart(PieChart pieChart) {
        double totalProtein = 0;
        double totalFat = 0;
        double totalCarbs = 0;

        for (Product product : addedProducts) {
            totalProtein += product.getProtein() * product.getAmount() / 100;
            totalFat += product.getFat() * product.getAmount() / 100;
            totalCarbs += product.getCarbs() * product.getAmount() / 100;
        }

        double total = totalProtein + totalFat + totalCarbs;

        float proteinPercent = (float) ((totalProtein / total) * 100);
        float fatPercent = (float) ((totalFat / total) * 100);
        float carbsPercent = (float) ((totalCarbs / total) * 100);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(proteinPercent, "Белки"));
        entries.add(new PieEntry(fatPercent, "Жиры"));
        entries.add(new PieEntry(carbsPercent, "Углеводы"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setValueFormatter(new PercentFormatter(pieChart)); // Форматирование значения в процентах
        dataSet.setValueTextSize(16f); // Увеличение размера текста
        // dataSet.setValueTextColor(Color.BLACK); // Изменение цвета текста

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("БЖУ");
        pieChart.animateY(1000);
        pieChart.setExtraOffsets(20f, 0f, 20f, 0f); // Увеличение размера диаграммы
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(15f); // Увеличение размера текста легенды
        pieChart.invalidate();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.nav_profile) {
                        startActivity(new Intent(NutritionActivity.this, ProfileUserActivity.class));
                        return true;
                    } else if (id == R.id.nav_ask_question) {
                        startActivity(new Intent(NutritionActivity.this, ChartActivity.class));
                        return true;
                    } else if (id == R.id.nav_add_weight_loss) {
                        startActivity(new Intent(NutritionActivity.this, WeightLossActivity.class));
                        return true;
                    }
                    return false;
                }

            };

    public void saveDailyNutritionToDatabase(Product product, double quantity) {
        // Получаем логин пользователя из SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userLogin = prefs.getString("username", null);

        if (userLogin == null) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Connection con = null;
            PreparedStatement statement = null;
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                con = connectionClass.CONN();

                if (con == null) {
                    runOnUiThread(() -> Toast.makeText(NutritionActivity.this, "Нет соединения с базой данных", Toast.LENGTH_SHORT).show());
                    return;
                }


                String sql = "INSERT INTO daily_nutrition (user_login, product_name, calories, protein, fat, carbs, amount, date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE())";

                statement = con.prepareStatement(sql);
                statement.setString(1, userLogin); // Используем полученный логин
                statement.setString(2, product.getName());
                statement.setDouble(3, product.getCalories());
                statement.setDouble(4, product.getProtein());
                statement.setDouble(5, product.getFat());
                statement.setDouble(6, product.getCarbs());
                statement.setDouble(7, quantity);


                int result = statement.executeUpdate();
                if (result > 0) {
                    runOnUiThread(() -> {
                        Toast.makeText(NutritionActivity.this, "Данные успешно сохранены", Toast.LENGTH_SHORT).show();
                        Log.d("Database", "Продукт сохранен: " + product.getName() + " для пользователя: " + userLogin);
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(NutritionActivity.this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Database", "Ошибка SQL: " + e.getMessage());
                });
            } finally {
                try {
                    if (statement != null) statement.close();
                    if (con != null) con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void setDailyDatabaseCleanup(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DatabaseCleanupReceiver.class); // Создаем приемник для очистки базы данных


        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE // Используем FLAG_IMMUTABLE, если PendingIntent не нужно изменять
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0); // Устанавливаем час (полночь)
        calendar.set(Calendar.MINUTE, 0); // Устанавливаем минуту
        calendar.set(Calendar.SECOND, 0); // Устанавливаем секунду

        // Настроим повторяющийся alarm, который будет срабатывать каждый день в полночь
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProducts();  // Загрузить продукты пользователя при каждом возвращении на активность
        loadRecommendedKBZHU();
    }

    private void loadUserProducts() {
        // Получаем логин пользователя из SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userLogin = prefs.getString("username", null);

        if (userLogin == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        // Загружаем продукты для конкретного пользователя
        AsyncTask<String, Void, List<Product>> task = new AsyncTask<String, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(String... strings) {
                String login = strings[0];
                List<Product> products = new ArrayList<>();
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet resultSet = null;

                try {
                    ConnectionClass connectionClass = new ConnectionClass();
                    con = connectionClass.CONN();

                    if (con != null) {
                        String sql = "SELECT * FROM daily_nutrition WHERE user_login = ?";
                        statement = con.prepareStatement(sql);
                        statement.setString(1, login);
                        resultSet = statement.executeQuery();

                        while (resultSet.next()) {
                            String productName = resultSet.getString("product_name");
                            double calories = resultSet.getDouble("calories");
                            double protein = resultSet.getDouble("protein");
                            double fat = resultSet.getDouble("fat");
                            double carbs = resultSet.getDouble("carbs");
                            double amount = resultSet.getDouble("amount");

                            Product product = new Product(productName, calories, protein, fat, carbs, amount);
                            products.add(product);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                        if (statement != null) {
                            statement.close();
                        }
                        if (con != null) {
                            con.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return products;
            }

            @Override
            protected void onPostExecute(List<Product> products) {
                super.onPostExecute(products);
                addedProducts.clear(); // Очистить список старых продуктов
                addedProducts.addAll(products); // Добавить новые продукты

                // Обновить таблицу и summary
                updateTableWithUserProducts();
                updateSummary();
            }
        };

        task.execute(userLogin);
    }

    private void updateTableWithUserProducts() {
        // Очищаем таблицу перед обновлением
        tableLayout.removeAllViews();

        // Для каждого продукта из списка добавляем строку в таблицу
        for (Product product : addedProducts) {
            TableRow row = new TableRow(this);

            TextView name = new TextView(this);
            name.setText(product.getName());
            name.setGravity(Gravity.CENTER);
            name.setPadding(8, 8, 8, 8);
            row.addView(name);

            TextView kcal = new TextView(this);
            kcal.setText(String.format("%.2f", product.getKcal() * product.getAmount() / 100));
            kcal.setGravity(Gravity.CENTER);
            kcal.setPadding(8, 8, 8, 8);
            row.addView(kcal);

            TextView protein = new TextView(this);
            protein.setText(String.format("%.2f", product.getProtein() * product.getAmount() / 100));
            protein.setGravity(Gravity.CENTER);
            protein.setPadding(8, 8, 8, 8);
            row.addView(protein);

            TextView fat = new TextView(this);
            fat.setText(String.format("%.2f", product.getFat() * product.getAmount() / 100));
            fat.setGravity(Gravity.CENTER);
            fat.setPadding(8, 8, 8, 8);
            row.addView(fat);

            TextView carbs = new TextView(this);
            carbs.setText(String.format("%.2f", product.getCarbs() * product.getAmount() / 100));
            carbs.setGravity(Gravity.CENTER);
            carbs.setPadding(8, 8, 8, 8);
            row.addView(carbs);

            TextView amount = new TextView(this);
            amount.setText(String.valueOf(product.getAmount()));
            amount.setGravity(Gravity.CENTER);
            amount.setPadding(8, 8, 8, 8);
            row.addView(amount);

            row.setBackgroundResource(R.drawable.table_row_border);

            // Добавляем тот же обработчик нажатия
            row.setOnClickListener(v -> {
                new AlertDialog.Builder(NutritionActivity.this)
                        .setTitle("Удалить продукт")
                        .setMessage("Вы уверены, что хотите удалить продукт?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            tableLayout.removeView(row);
                            addedProducts.remove(product);
                            deleteProductFromDatabase(product);
                            updateSummary();
                            updatePieChart(pieChart);
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });

            tableLayout.addView(row);
        }
    }

    public void loadRecommendedKBZHU() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userLogin = prefs.getString("username", null);

        if (userLogin == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (Connection con = new ConnectionClass().CONN();
                 PreparedStatement stmt = con.prepareStatement(
                         "SELECT recommended_kcal, recommended_protein, recommended_fat, recommended_carbs " +
                                 "FROM usersKBZHU WHERE user_login = ?")) {

                stmt.setString(1, userLogin);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double kcal = rs.getDouble("recommended_kcal");
                    double protein = rs.getDouble("recommended_protein");
                    double fat = rs.getDouble("recommended_fat");
                    double carbs = rs.getDouble("recommended_carbs");

                    runOnUiThread(() -> {
                        // Обновляем TextView с рекомендациями (rskTextView и count)
                        rskTextView.setText(String.format("РСК: %.2f", kcal));
                        count.setText(String.format("К: %.2f; Б: %.2f г; Ж: %.2f г; У: %.2f г", kcal, protein, fat, carbs));

                        // После загрузки рекомендаций обновляем summary
                        updateSummary();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(NutritionActivity.this,
                                    "Рекомендации по КБЖУ не найдены",
                                    Toast.LENGTH_SHORT).show());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(NutritionActivity.this,
                                "Ошибка загрузки рекомендаций: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        });
    }
}