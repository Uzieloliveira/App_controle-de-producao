package com.seuapp.controledeprodutividade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private TextInputLayout layoutLoginNome;
    private TextInputLayout layoutLoginRE;
    private TextInputEditText editLoginNome;
    private TextInputEditText editLoginRE;
    private Button btnEntrarLogin;
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

        layoutLoginNome = findViewById(R.id.layoutLoginNome);
        layoutLoginRE = findViewById(R.id.layoutLoginRE);
        editLoginNome = findViewById(R.id.editLoginNome);
        editLoginRE = findViewById(R.id.editLoginRE);
        btnEntrarLogin = findViewById(R.id.btnEntrarLogin);
        btnSelecionarUsuario = findViewById(R.id.btnSelecionarUsuario);
        pbLogin = findViewById(R.id.pbLogin);

        btnEntrarLogin.setOnClickListener(v -> validarEEntrar());
        btnSelecionarUsuario.setOnClickListener(v -> sincronizarEMostrarUsuarios());
    }

    private void sincronizarEMostrarUsuarios() {
        setLoading(true);
        
        firestore.collection("usuarios").get().addOnCompleteListener(task -> {
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
                    Toast.makeText(this, "Erro ao processar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                List<Usuario> listaLocal = db.usuarioDao().listarTodos();
                if (listaLocal.isEmpty()) {
                    String erro = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                    Toast.makeText(this, "Falha ao buscar usuários: " + erro, Toast.LENGTH_LONG).show();
                } else {
                    mostrarDialogoSelecao(listaLocal);
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
            editLoginNome.setText(selecionado.nome);
            editLoginRE.setText(selecionado.re);
            layoutLoginNome.setError(null);
            layoutLoginRE.setError(null);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void validarEEntrar() {
        layoutLoginNome.setError(null);
        layoutLoginRE.setError(null);

        String nome = editLoginNome.getText() != null ? editLoginNome.getText().toString().trim() : "";
        String re = editLoginRE.getText() != null ? editLoginRE.getText().toString().trim() : "";

        if (nome.isEmpty()) {
            layoutLoginNome.setError(getString(R.string.error_nome_usuario));
            return;
        }
        if (re.isEmpty() || re.length() > 5) {
            layoutLoginRE.setError(getString(R.string.error_num_cadastro));
            return;
        }

        setLoading(true);

        firestore.collection("usuarios").whereEqualTo("re", re).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Usuario uCloud = queryDocumentSnapshots.getDocuments().get(0).toObject(Usuario.class);
                        if (uCloud != null && !uCloud.nome.equalsIgnoreCase(nome)) {
                            setLoading(false);
                            layoutLoginRE.setError("Este RE já pertence a: " + uCloud.nome);
                            return;
                        }
                    } else {
                        Usuario novo = new Usuario(nome, re);
                        firestore.collection("usuarios").add(novo)
                                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar na nuvem", Toast.LENGTH_SHORT).show());
                    }

                    if (db.usuarioDao().buscarPorRE(re) == null) {
                        db.usuarioDao().inserir(new Usuario(nome, re));
                    }
                    setLoading(false);
                    realizarLogin(nome, re);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro de conexão: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Permite login offline se o usuário já existir localmente
                    Usuario local = db.usuarioDao().buscarPorRE(re);
                    if (local != null && local.nome.equalsIgnoreCase(nome)) {
                        realizarLogin(nome, re);
                    }
                });
    }

    private void setLoading(boolean loading) {
        pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnEntrarLogin.setEnabled(!loading);
        btnSelecionarUsuario.setEnabled(!loading);
        editLoginNome.setEnabled(!loading);
        editLoginRE.setEnabled(!loading);
    }

    private void realizarLogin(String nome, String re) {
        SharedPreferences preferences = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("NOME_USUARIO", nome);
        editor.putString("RE_USUARIO", re);
        editor.putBoolean("LOGADO", true);
        editor.apply();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
