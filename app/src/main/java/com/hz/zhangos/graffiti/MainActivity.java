package com.hz.zhangos.graffiti;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hz.zhangos.graffiti.views.GraffitiView;

public class MainActivity extends AppCompatActivity {
    GraffitiView graffitiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graffitiView = findViewById(R.id.graffitiView);
    }

    public void undo(View view) {
        graffitiView.undo();
    }

    public void clearAll(View view) {
        graffitiView.clearAllPath();
    }

    public void red(View view) {
        graffitiView.setPaintColor(Color.RED);
    }

    public void blue(View view) {
        graffitiView.setPaintColor(Color.BLUE);
    }
}
