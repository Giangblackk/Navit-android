package com.giangblackk.helloble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {
    int number = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        TextView textview = findViewById(R.id.textView);
        textview.setText(String.valueOf(number));
        button.setOnClickListener(v -> {
            number = number + 1;
            textview.setText(String.valueOf(number));
        });
    }
}