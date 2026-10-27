package com.seuapp.controledeprodutividade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class HomeActivity extends AppCompatActivity {

    private TextView tvUsuarioLogado;
    private Button btnSairHome;
    private FirebaseFirestore firestore;
    private ListenerRegistration sessionListener;
    private String usuarioRe;
    private String localDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
        boolean logado = preferences.getBoolean("LOGADO", false);

        if (!logado) {
            redirecionarLogin();
            return;
        }

        setContentView(R.layout.activity_home);

        firestore = FirebaseFirestore.getInstance();
        usuarioRe = preferences.getString("RE_USUARIO", "");
        localDeviceId = preferences.getString("DEVICE_ID", "");

        tvUsuarioLogado = findViewById(R.id.tvUsuarioLogado);
        btnSairHome = findViewById(R.id.btnSairHome);
        Button btnAbrirCadastro = findViewById(R.id.btnAbrirCadastro);
        Button btnInformarPecas = findViewById(R.id.btnInformarPecas);
        Button btnListarCadastrados = findViewById(R.id.btnListarCadastrados);

        String nomeUsuario = preferences.getString("NOME_USUARIO", "Usuário");
        tvUsuarioLogado.setText(getString(R.string.label_usuario_logado, nomeUsuario));

        btnAbrirCadastro.setOnClickListener(v -> selecionarSecao());
        btnInformarPecas.setOnClickListener(v -> startActivity(new Intent(this, PecasRestantesActivity.class)));
        btnListarCadastrados.setOnClickListener(v -> startActivity(new Intent(this, ListaActivity.class)));
        btnSairHome.setOnClickListener(v -> deslogarConfirmacao());

        iniciarMonitoramentoSessao();
    }

    private void iniciarMonitoramentoSessao() {
        if (usuarioRe.isEmpty()) return;

        sessionListener = firestore.collection("usuarios_producao")
                .whereEqualTo("re", usuarioRe)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || snapshots.isEmpty()) return;

                    Usuario uCloud = snapshots.getDocuments().get(0).toObject(Usuario.class);
                    if (uCloud != null && uCloud.lastDeviceId != null && !uCloud.lastDeviceId.equals(localDeviceId)) {
                        // Logout forçado se outro dispositivo conectou
                        forcarLogout();
                    }
                });
    }

    private void forcarLogout() {
        if (sessionListener != null) sessionListener.remove();
        getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE).edit().clear().apply();

        new AlertDialog.Builder(this)
                .setTitle("Sessão Encerrada")
                .setMessage("Sua conta foi conectada em outro dispositivo. Este acesso será encerrado.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> redirecionarLogin())
                .show();
    }

    private void deslogarConfirmacao() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    if (sessionListener != null) sessionListener.remove();
                    getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE).edit().clear().apply();
                    redirecionarLogin();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void redirecionarLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void selecionarSecao() {
        final String[] secoes = {"Ligação", "Solda", "Bandagem", "Amarração", "Prensa", "Acabamento"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_secao);
        builder.setItems(secoes, (dialog, which) -> {
            String secaoEscolhida = secoes[which];
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra("SECAO", secaoEscolhida);
            startActivity(intent);
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sessionListener != null) sessionListener.remove();
    }
}
