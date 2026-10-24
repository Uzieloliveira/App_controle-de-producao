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

public class HomeActivity extends AppCompatActivity {

    private TextView tvUsuarioLogado;
    private Button btnSairHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar se o usuário está logado
        SharedPreferences preferences = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
        boolean logado = preferences.getBoolean("LOGADO", false);

        if (!logado) {
            // Se não estiver logado, redireciona para a tela de Login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        tvUsuarioLogado = findViewById(R.id.tvUsuarioLogado);
        btnSairHome = findViewById(R.id.btnSairHome);
        Button btnAbrirCadastro = findViewById(R.id.btnAbrirCadastro);
        Button btnListarCadastrados = findViewById(R.id.btnListarCadastrados);

        // Exibir o nome do usuário logado
        String nomeUsuario = preferences.getString("NOME_USUARIO", "Usuário");
        tvUsuarioLogado.setText(getString(R.string.label_usuario_logado, nomeUsuario));

        btnAbrirCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarSecao();
            }
        });

        btnListarCadastrados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ListaActivity.class);
                startActivity(intent);
            }
        });

        btnSairHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deslogar();
            }
        });
    }

    private void deslogar() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair da sua conta?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    SharedPreferences preferences = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void selecionarSecao() {
        final String[] secoes = {"Bandagem"};

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
}
