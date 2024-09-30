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

public class ReceptionActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;
    private TextView textScannedData;
    private Button buttonConfirm;
    private boolean isScanned = false;
    private String scannedMatricule;
    private String scannedLongueur;
    private String scannedLargeur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception);

        // Initialiser les composants
        barcodeView = findViewById(R.id.reception_barcode_scanner);
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
                // Envoyer à l'api pour update
                updateStatusToEnLocation(scannedMatricule);
                // Envoyer les données à l'API pour insertion
                sendDataToServer(scannedMatricule, scannedLongueur, scannedLargeur);

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

                // Afficher le contenu scanné complet pour le debug
                Toast.makeText(ReceptionActivity.this, "Contenu scanné : " + scannedData, Toast.LENGTH_LONG).show();

                // Vérifier le format des données scannées
                if (scannedData.contains("n°serie") && scannedData.contains("Longueur") && scannedData.contains("Largeur") && scannedData.contains("Date")) {
                    // Extraire le numéro de série (matricule)
                    scannedMatricule = scannedData.substring(scannedData.indexOf("n°serie") + 7, scannedData.indexOf("Longueur:")).trim();

                    // Extraire la longueur et la largeur
                    scannedLongueur = scannedData.substring(scannedData.indexOf("Longueur: ") + 10, scannedData.indexOf("cm, Largeur")).trim();
                    scannedLargeur = scannedData.substring(scannedData.indexOf("Largeur: ") + 9, scannedData.indexOf("cm, Date")).trim();

                    // Afficher les informations scannées dans le TextView
                    String message = "Matricule (n° série): " + scannedMatricule + "\nLongueur: " + scannedLongueur + " cm\nLargeur: " + scannedLargeur + " cm";
                    textScannedData.setText(message);

                    // Activer le bouton "Confirmer"
                    buttonConfirm.setEnabled(true);

                } else {
                    // Si le format est incorrect, afficher une erreur
                    Toast.makeText(ReceptionActivity.this, "Format de QR Code invalide", Toast.LENGTH_SHORT).show();
                    isScanned = false;  // Autoriser un nouveau scan si erreur
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                // Ne rien faire ici
            }
        });
    }
    // Fonction pour la mise à jour de status de bache
    private void updateStatusToEnLocation(String matricule) {
        String url = "https://levirtuoz.alwaysdata.net/update_status_web.php";

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
                                Toast.makeText(ReceptionActivity.this, "Statut mis à jour: en location", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ReceptionActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(ReceptionActivity.this, "Erreur JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ReceptionActivity.this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("matricule", matricule);
                return params;
            }
        };

        // Ajouter la requête à la file d'attente
        queue.add(postRequest);
    }


    // Fonction pour envoyer les données à l'API
    private void sendDataToServer(String matricule, String longueur, String largeur) {
        String url = "https://levirtuoz.alwaysdata.net/recuperation/insert_bache_mobile.php";

        // Créer une requête POST avec Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {
                                Toast.makeText(ReceptionActivity.this, "Succès: " + message, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ReceptionActivity.this, "Erreur: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(ReceptionActivity.this, "Erreur JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ReceptionActivity.this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("matricule", matricule);
                params.put("longueur", longueur);
                params.put("largeur", largeur);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
