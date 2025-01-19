package com.example.mygrocerystore;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();

        view.findViewById(R.id.loginButton).setOnClickListener(v -> {
            String email = ((EditText) view.findViewById(R.id.emailInput)).getText().toString();
            String password = ((EditText) view.findViewById(R.id.passwordInput)).getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToShopping();
                    } else {
                        Toast.makeText(getContext(), "Login failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        view.findViewById(R.id.registerRedirectButton).setOnClickListener(v -> navigateToRegister());

        return view;
    }

    private void navigateToShopping() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ShoppingFragment())
                .commit();
    }

    private void navigateToRegister() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment())
                .commit();
    }
}
