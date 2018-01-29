package com.madongqiang.yeareffect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EntranceActivity extends AppCompatActivity {
    private Button btnEntrance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);

        btnEntrance = findViewById(R.id.btn_entrance);
        btnEntrance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EntranceActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.bottom_in,R.anim.top_out);
            }
        });
    }
}
