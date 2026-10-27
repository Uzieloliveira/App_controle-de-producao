package com.seuapp.controledeprodutividade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutLoginRE;
    private TextInputEditText editLoginRE;
    private Button btnEntrarLogin;
    private Button btnIrParaCadastro;
    private Button btnSelecionarUsuario;
    private ProgressBar pbLogin;
    private AppDatabase db;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        firestore = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        layoutLoginRE = findViewById(R.id.layoutLoginRE);
        editLoginRE = findViewById(R.id.editLoginRE);
        btnEntrarLogin = findViewById(R.id.btnEntrarLogin);
        btnIrParaCadastro = findViewById(R.id.btnIrParaCadastro);
        btnSelecionarUsuario = findViewById(R.id.btnSelecionarUsuario);
        pbLogin = findViewById(R.id.pbLogin);

        btnEntrarLogin.setOnClickListener(v -> validarEEntrar());
        
        btnIrParaCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        btnSelecionarUsuario.setOnClickListener(v -> sincronizarEMostrarUsuarios());
    }

    private void sincronizarEMostrarUsuarios() {
        setLoading(true);
        firestore.collection("usuarios_producao").get().addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful() && task.getResult() != null) {
                List<Usuario> listaCloud = new ArrayList<>();
                try {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Usuario u = document.toObject(Usuario.class);
                        listaCloud.add(u);
                        if (db.usuarioDao().buscarPorRE(u.re) == null) {
                            db.usuarioDao().inserir(u);
                        }
                    }
                    mostrarDialogoSelecao(listaCloud);
                } catch (Exception e) {
                    Toast.makeText(this, "Erro ao processar dados", Toast.LENGTH_SHORT).show();
                }
            } else {
                List<Usuario> listaLocal = db.usuarioDao().listarTodos();
                if (!listaLocal.isEmpty()) {
                    mostrarDialogoSelecao(listaLocal);
                } else {
                    Toast.makeText(this, "Nenhum usuário encontrado.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mostrarDialogoSelecao(List<Usuario> usuarios) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione um Usuário");
        ArrayAdapter<Usuario> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuarios);
        builder.setAdapter(adapter, (dialog, which) -> {
            Usuario selecionado = usuarios.get(which);
            editLoginRE.setText(selecionado.re);
            validarEEntrar();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void validarEEntrar() {
        layoutLoginRE.setError(null);
        String re = editLoginRE.getText() != null ? editLoginRE.getText().toString().trim() : "";
        if (re.isEmpty() || re.length() > 5) {
            layoutLoginRE.setError(getString(R.string.error_num_cadastro));
            return;
        }

        setLoading(true);
        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        firestore.collection("usuarios_producao").whereEqualTo("re", re).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        Usuario uCloud = doc.toObject(Usuario.class);
                        if (uCloud != null) {
                            // Atualizar ID do dispositivo para garantir sessão única
                            firestore.collection("usuarios_producao").document(doc.getId())
                                    .update("lastDeviceId", currentDeviceId)
                                    .addOnSuccessListener(aVoid -> {
                                        uCloud.lastDeviceId = currentDeviceId;
                                        if (db.usuarioDao().buscarPorRE(re) == null) {
                                            db.usuarioDao().inserir(uCloud);
                                        }
                                        setLoading(false);
                                        realizarLogin(uCloud.nome, uCloud.re, uCloud.departamento, currentDeviceId);
                                    })
                                    .addOnFailureListener(e -> {
                                        setLoading(false);
                                        Toast.makeText(this, "Erro ao atualizar sessão", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        setLoading(false);
                        layoutLoginRE.setError("Usuário não encontrado. Cadastre-se primeiro.");
                    }
                })
                .addOnFailureListener(e -> {
                    Usuario local = db.usuarioDao().buscarPorRE(re);
                    setLoading(false);
                    if (local != null) {
                        realizarLogin(local.nome, local.re, local.departamento, currentDeviceId);
                    } else {
                        Toast.makeText(this, "Erro de conexão.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnEntrarLogin.setEnabled(!loading);
        btnIrParaCadastro.setEnabled(!loading);
        btnSelecionarUsuario.setEnabled(!loading);
        editLoginRE.setEnabled(!loading);
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
