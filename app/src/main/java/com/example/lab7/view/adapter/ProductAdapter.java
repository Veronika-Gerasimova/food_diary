package com.example.lab7.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab7.R;
import com.example.lab7.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private int selectedPosition = RecyclerView.NO_POSITION;
    private List<Product> products;
    private List<Product> filteredProducts;
    private OnProductDeleteListener deleteListener;
  //  private ProductFilter productFilter;
    public ProductAdapter(List<Product> products) {
        this.products = products;
        this.filteredProducts = new ArrayList<>(products);
    }
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
    public int getSelectedPosition() {
        return selectedPosition;
    }

    // Обновление выбранной позиции
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

   /* public List<Product> getProducts() {
        return products;
    }*/
  /*  public void addProduct(Product product) {
        products.add(product);
        notifyItemInserted(products.size() - 1);
    }
*/
   /* public Filter getFilter() {
        if (productFilter == null) {
            productFilter = new ProductFilter();
        }
        return productFilter;
    }
    public void setFilter(String searchText) {
        products.clear();
        if (searchText.isEmpty()) {
            products.addAll(filteredProducts);
        } else {
            searchText = searchText.toLowerCase();
            for (Product product : filteredProducts) {
                if (product.getName().toLowerCase().contains(searchText)) {
                    products.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }*/

    private class ProductFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Product> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(products);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Product product : products) {
                    if (product.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(product);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredProducts.clear();
            filteredProducts.addAll((List) results.values);
            notifyDataSetChanged();
        }
    }
    private OnProductClickListener clickListener;

   /* public void setOnProductClickListener(OnProductClickListener listener) {
        this.clickListener = listener;
    }*/

    public interface OnProductDeleteListener {
        void onProductDelete(int position);
    }

   /* public void setOnProductDeleteListener(OnProductDeleteListener listener) {
        this.deleteListener = listener;
    }*/

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);

        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onProductDelete(position);
            }
            return true;
        });
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProductClick(product);
            }
        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView productNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.product_name);
        }

        public void bind(Product product) {
            productNameTextView.setText(product.getName());
        }

    }

}
