package com.MyScanner.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button buttonReceive, buttonReturn, buttonDeployment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les boutons
        buttonReceive = findViewById(R.id.button_receive);
        buttonReturn = findViewById(R.id.button_return);
        buttonDeployment = findViewById(R.id.button_deployment);

        // Rediriger vers l'activité de réception
        buttonReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReceptionActivity.class);
                startActivity(intent);
            }
        });

        // Rediriger vers l'activité de retour
        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReturnActivity.class);
                startActivity(intent);
            }
        });

        // Rediriger vers l'activité de déploiement
        buttonDeployment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeploymentActivity.class);
                startActivity(intent);
            }
        });
    }
}
