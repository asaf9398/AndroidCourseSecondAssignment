package com.example.mygrocerystore;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShoppingFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button addItemButton, logoutButton;
    private ShoppingAdapter adapter;
    private List<Item> itemList;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        addItemButton = view.findViewById(R.id.addItemButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("ShoppingList");

        itemList = new ArrayList<>();
        adapter = new ShoppingAdapter(itemList, databaseReference);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        addItemButton.setOnClickListener(v -> openAddItemDialog());
        logoutButton.setOnClickListener(v -> logoutUser());

        loadItems();

        return view;
    }

    private void loadItems() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        itemList.add(item);
                        Log.d("ShoppingFragment", "Item loaded: " + item.getName() + ", Quantity: " + item.getQuantity());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText itemNameInput = dialogView.findViewById(R.id.itemNameInput);
        EditText itemQuantityInput = dialogView.findViewById(R.id.itemQuantityInput);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = itemNameInput.getText().toString().trim();
            String quantityText = itemQuantityInput.getText().toString().trim();

            if (name.isEmpty() || quantityText.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityText);
                String id = databaseReference.push().getKey();
                if (id == null) {
                    Toast.makeText(getContext(), "Failed to generate item ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                Item newItem = new Item(id, name, quantity);
                databaseReference.child(id).setValue(newItem).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to add item: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .addToBackStack(null)
                .commit();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

}
