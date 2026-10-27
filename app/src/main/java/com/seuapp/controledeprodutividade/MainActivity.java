package com.seuapp.controledeprodutividade;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout layoutOperador;
    private TextInputLayout layoutValor;
    private TextInputLayout layoutAnotacao;
    private TextInputEditText editOperador;
    private TextInputEditText editValor;
    private TextInputEditText editAnotacao;
    private TextView tvDataSelecionada;
    private LinearLayout layoutHeader;
    private Button btnSalvar;
    private Button btnVerLista;
    private String dataCadastro;
    private String secaoEscolhida;
    private AppDatabase db;
    private String usuarioRe;
    private String usuarioDepto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        SharedPreferences preferences = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
        usuarioRe = preferences.getString("RE_USUARIO", "");
        usuarioDepto = preferences.getString("DEPTO_USUARIO", "");

        if (getIntent() != null && getIntent().hasExtra("SECAO")) {
            secaoEscolhida = getIntent().getStringExtra("SECAO");
        } else {
            secaoEscolhida = "Bandagem";
        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dataCadastro = df.format(c.getTime());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.btn_novo_cadastro);
        }

        layoutHeader = findViewById(R.id.layoutHeader);
        tvDataSelecionada = findViewById(R.id.tvDataSelecionada);
        layoutOperador = findViewById(R.id.layoutOperador);
        layoutValor = findViewById(R.id.layoutValor);
        layoutAnotacao = findViewById(R.id.layoutAnotacao);
        editOperador = findViewById(R.id.editOperador);
        editValor = findViewById(R.id.editValor);
        editAnotacao = findViewById(R.id.editAnotacao);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnVerLista = findViewById(R.id.btnVerLista);

        // Forçar CAIXA ALTA no operador
        editOperador.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        atualizarCabecalho();

        layoutHeader.setOnClickListener(v -> mudarSecao());
        btnSalvar.setOnClickListener(v -> validarESalvar());
        btnVerLista.setOnClickListener(v -> startActivity(new Intent(this, ListaActivity.class)));
    }

    private void atualizarCabecalho() {
        tvDataSelecionada.setText(getString(R.string.label_data_exibicao, dataCadastro, secaoEscolhida));
    }

    private void mudarSecao() {
        final String[] secoes = {"Ligação", "Solda", "Bandagem", "Amarração", "Prensa", "Acabamento"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_secao));
        builder.setItems(secoes, (dialog, which) -> {
            secaoEscolhida = secoes[which];
            atualizarCabecalho();
        });
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validarESalvar() {
        layoutOperador.setError(null);
        layoutValor.setError(null);

        String operador = editOperador.getText() != null ? editOperador.getText().toString().trim() : "";
        String valorStr = editValor.getText() != null ? editValor.getText().toString().trim() : "";
        String anotacao = editAnotacao.getText() != null ? editAnotacao.getText().toString().trim() : "";

        if (operador.length() < 2) {
            layoutOperador.setError(getString(R.string.error_operador));
            return;
        }

        int valor;
        try {
            valor = Integer.parseInt(valorStr);
            if (valor <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            layoutValor.setError(getString(R.string.error_valor));
            return;
        }

        // Salvar apenas no SQLite local
        Producao novaProducao = new Producao(operador, valor, dataCadastro, secaoEscolhida, anotacao, usuarioRe, usuarioDepto);
        db.producaoDao().inserir(novaProducao);
        
        Toast.makeText(this, R.string.success_save, Toast.LENGTH_SHORT).show();
        editOperador.setText("");
        editValor.setText("");
        editAnotacao.setText("");
        editOperador.requestFocus();
    }
}
