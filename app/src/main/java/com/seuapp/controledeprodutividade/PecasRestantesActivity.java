package com.seuapp.controledeprodutividade;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PecasRestantesActivity extends AppCompatActivity {

    private TextInputEditText editLigacao, editSolda, editBandagem, editAmarracao, editPrensa, editAcabamento;
    private Button btnEnviarPecas;
    private ProgressBar pbPecas;
    private AppDatabase db;
    private FirebaseFirestore firestore;
    private String usuarioRe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pecas_restantes);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Peças Restantes");
        }

        db = AppDatabase.getInstance(this);
        firestore = FirebaseFirestore.getInstance();

        SharedPreferences preferences = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
        usuarioRe = preferences.getString("RE_USUARIO", "");

        editLigacao = findViewById(R.id.editLigacao);
        editSolda = findViewById(R.id.editSolda);
        editBandagem = findViewById(R.id.editBandagem);
        editAmarracao = findViewById(R.id.editAmarracao);
        editPrensa = findViewById(R.id.editPrensa);
        editAcabamento = findViewById(R.id.editAcabamento);
        btnEnviarPecas = findViewById(R.id.btnEnviarPecas);
        pbPecas = findViewById(R.id.pbPecas);

        btnEnviarPecas.setOnClickListener(v -> validarEEnviar());
    }

    private void validarEEnviar() {
        String sLigacao = editLigacao.getText().toString().trim();
        String sSolda = editSolda.getText().toString().trim();
        String sBandagem = editBandagem.getText().toString().trim();
        String sAmarracao = editAmarracao.getText().toString().trim();
        String sPrensa = editPrensa.getText().toString().trim();
        String sAcabamento = editAcabamento.getText().toString().trim();

        if (sLigacao.isEmpty() || sSolda.isEmpty() || sBandagem.isEmpty() || sAmarracao.isEmpty() || sPrensa.isEmpty() || sAcabamento.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        int nLigacao = Integer.parseInt(sLigacao);
        int nSolda = Integer.parseInt(sSolda);
        int nBandagem = Integer.parseInt(sBandagem);
        int nAmarracao = Integer.parseInt(sAmarracao);
        int nPrensa = Integer.parseInt(sPrensa);
        int nAcabamento = Integer.parseInt(sAcabamento);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String dataAtual = df.format(Calendar.getInstance().getTime());

        PecasRestantes pecas = new PecasRestantes(dataAtual, usuarioRe, nLigacao, nSolda, nBandagem, nAmarracao, nPrensa, nAcabamento);

        // Enviar para o Firebase
        firestore.collection("pecas_restantes").add(pecas)
                .addOnSuccessListener(documentReference -> {
                    // Salvar no SQLite local
                    db.pecasRestantesDao().inserir(pecas);
                    setLoading(false);
                    Toast.makeText(this, "Dados enviados com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro ao enviar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        pbPecas.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnEnviarPecas.setEnabled(!loading);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
