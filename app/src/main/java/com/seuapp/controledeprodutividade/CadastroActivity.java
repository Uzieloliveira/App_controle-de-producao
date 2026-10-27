package com.seuapp.controledeprodutividade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroActivity extends AppCompatActivity {

    private TextInputLayout layoutCadastroNome, layoutCadastroRE, layoutCadastroDepto;
    private TextInputEditText editCadastroNome, editCadastroRE;
    private AutoCompleteTextView autoCompleteCadastroDepto;
    private Button btnFinalizarCadastro, btnVoltarLogin;
    private ProgressBar pbCadastro;
    private AppDatabase db;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        db = AppDatabase.getInstance(this);
        firestore = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        layoutCadastroNome = findViewById(R.id.layoutCadastroNome);
        layoutCadastroRE = findViewById(R.id.layoutCadastroRE);
        layoutCadastroDepto = findViewById(R.id.layoutCadastroDepto);
        editCadastroNome = findViewById(R.id.editCadastroNome);
        editCadastroRE = findViewById(R.id.editCadastroRE);
        autoCompleteCadastroDepto = findViewById(R.id.autoCompleteCadastroDepto);
        btnFinalizarCadastro = findViewById(R.id.btnFinalizarCadastro);
        btnVoltarLogin = findViewById(R.id.btnVoltarLogin);
        pbCadastro = findViewById(R.id.pbCadastro);

        String[] departamentos = {"Bobinagem III B"};
        ArrayAdapter<String> adapterDepto = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departamentos);
        autoCompleteCadastroDepto.setAdapter(adapterDepto);

        btnFinalizarCadastro.setOnClickListener(v -> validarECadastrar());
        btnVoltarLogin.setOnClickListener(v -> finish());
    }

    private void validarECadastrar() {
        layoutCadastroNome.setError(null);
        layoutCadastroRE.setError(null);
        layoutCadastroDepto.setError(null);

        String nome = editCadastroNome.getText().toString().trim();
        String re = editCadastroRE.getText().toString().trim();
        String depto = autoCompleteCadastroDepto.getText().toString().trim();

        if (nome.isEmpty()) {
            layoutCadastroNome.setError("Informe seu nome");
            return;
        }
        if (re.isEmpty() || re.length() > 5) {
            layoutCadastroRE.setError("RE inválido (máx 5 dígitos)");
            return;
        }
        if (depto.isEmpty()) {
            layoutCadastroDepto.setError("Selecione o departamento");
            return;
        }

        setLoading(true);
        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Verificar na nova coleção "usuarios_producao"
        firestore.collection("usuarios_producao").whereEqualTo("re", re).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        setLoading(false);
                        layoutCadastroRE.setError("Este RE já está cadastrado.");
                    } else {
                        Usuario novo = new Usuario(nome, re, depto);
                        novo.lastDeviceId = currentDeviceId;
                        firestore.collection("usuarios_producao").add(novo)
                                .addOnSuccessListener(documentReference -> {
                                    db.usuarioDao().inserir(novo);
                                    setLoading(false);
                                    realizarLogin(nome, re, depto, currentDeviceId);
                                })
                                .addOnFailureListener(e -> {
                                    setLoading(false);
                                    Toast.makeText(this, "Erro ao cadastrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean loading) {
        pbCadastro.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnFinalizarCadastro.setEnabled(!loading);
        btnVoltarLogin.setEnabled(!loading);
    }

    private void realizarLogin(String nome, String re, String depto, String deviceId) {
        SharedPreferences preferences = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("NOME_USUARIO", nome);
        editor.putString("RE_USUARIO", re);
        editor.putString("DEPTO_USUARIO", depto);
        editor.putString("DEVICE_ID", deviceId);
        editor.putBoolean("LOGADO", true);
        editor.apply();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
