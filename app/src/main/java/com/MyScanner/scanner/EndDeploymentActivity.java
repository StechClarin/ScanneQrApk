package com.MyScanner.scanner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class EndDeploymentActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;
    private TextView textScannedData;
    private Button buttonConfirm;
    private boolean isScanned = false;
    private String scannedMatricule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_deployment);

        // Initialiser les composants
        barcodeView = findViewById(R.id.end_deployment_barcode_scanner);
        textScannedData = findViewById(R.id.text_scanned_data);
        buttonConfirm = findViewById(R.id.button_confirm);

        // Démarrer le scanner
        startBarcodeScanner();

        // Désactiver le bouton de confirmation tant qu'il n'y a pas de scan
        buttonConfirm.setEnabled(false);

        // Action lors de la confirmation des informations
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Envoyer les données à l'API
                sendDataToServer(scannedMatricule);

                // Réinitialiser le scanner après confirmation
                resetScanner();
            }
        });
    }

    private void startBarcodeScanner() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (isScanned) {
                    return;  // Empêcher un nouveau scan si déjà scanné
                }

                // Récupérer le contenu du QR code scanné
                String scannedData = result.getText();
                isScanned = true;  // Marquer comme scanné pour stopper le scan

                // Vérifier le format des données scannées
                if (scannedData.contains("n°serie") && scannedData.contains("Longueur") && scannedData.contains("Largeur") && scannedData.contains("Date")) {
                    // Extraire le numéro de série (matricule)
                    scannedMatricule = scannedData.substring(scannedData.indexOf("n°serie") + 7, scannedData.indexOf("Longueur:")).trim();

                    // Afficher les informations scannées dans le TextView
                    String message = "Matricule (n° série): " + scannedMatricule;
                    textScannedData.setText(message);

                    // Activer le bouton "Confirmer"
                    buttonConfirm.setEnabled(true);

                } else {
                    // Si le format est incorrect, afficher une erreur
                    Toast.makeText(EndDeploymentActivity.this, "Format de QR Code invalide", Toast.LENGTH_SHORT).show();
                    isScanned = false;  // Autoriser un nouveau scan si erreur
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                // Ne rien faire ici
            }
        });
    }

    // Fonction pour envoyer les données à l'API de fin de déploiement
    private void sendDataToServer(String matricule) {
        String url = "https://levirtuoz.alwaysdata.net/recuperation/end_deploy_bache.php";  // URL de l'API de fin de déploiement

        // Créer une requête POST avec Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                Toast.makeText(EndDeploymentActivity.this, "Statut mis à jour à disponible", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EndDeploymentActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(EndDeploymentActivity.this, "Erreur JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EndDeploymentActivity.this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("matricule", matricule);  // Envoyer le matricule à l'API
                return params;
            }
        };

        // Ajouter la requête à la file d'attente
        queue.add(postRequest);
    }

    // Fonction pour réinitialiser le scanner après confirmation
    private void resetScanner() {
        isScanned = false;  // Permettre un nouveau scan
        barcodeView.resume();  // Relancer le scanner
        buttonConfirm.setEnabled(false);  // Désactiver le bouton de confirmation
        textScannedData.setText("Aucune donnée scannée pour le moment");  // Réinitialiser le texte
