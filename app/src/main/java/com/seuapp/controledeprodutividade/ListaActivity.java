package com.seuapp.controledeprodutividade;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;

public class ListaActivity extends AppCompatActivity implements ProducaoAdapter.OnEditClickListener {

    private RecyclerView rvProducao;
    private ProducaoAdapter adapter;
    private TextView tvEmpty;
    private Button btnEnviar;
    private ProgressBar pbLista;
    private AppDatabase db;
    private String usuarioRe;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_lista);
        }

        db = AppDatabase.getInstance(this);
        firestore = FirebaseFirestore.getInstance();

        SharedPreferences preferences = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
        usuarioRe = preferences.getString("RE_USUARIO", "");

        rvProducao = findViewById(R.id.rvProducao);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnEnviar = findViewById(R.id.btnEnviar);
        pbLista = findViewById(R.id.pbLista);

        rvProducao.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProducaoAdapter(this);
        rvProducao.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> enviarDadosParaFirebase());

        carregarDados();
    }

    private void carregarDados() {
        try {
            List<Producao> lista = db.producaoDao().listarNaoEnviadasPorUsuario(usuarioRe);
            if (lista.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvProducao.setVisibility(View.GONE);
                btnEnviar.setEnabled(false);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvProducao.setVisibility(View.VISIBLE);
                btnEnviar.setEnabled(true);
                adapter.setLista(lista);
            }
        } catch (Exception e) {
            mostrarErro("Erro ao carregar dados locais: " + e.getMessage());
        }
    }

    @Override
    public void onEditClick(Producao producao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Quantidade");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(producao.quantidade));
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            try {
                String novoValorStr = input.getText().toString();
                if (!novoValorStr.isEmpty()) {
                    producao.quantidade = Integer.parseInt(novoValorStr);
                    db.producaoDao().atualizar(producao);
                    carregarDados();
                    Toast.makeText(this, "Quantidade atualizada!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                mostrarErro("Erro ao atualizar quantidade: " + e.getMessage());
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void enviarDadosParaFirebase() {
        final List<Producao> listaParaEnviar = db.producaoDao().listarNaoEnviadasPorUsuario(usuarioRe);
        
        if (listaParaEnviar.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Sincronizar")
                .setMessage("Enviar " + listaParaEnviar.size() + " registros para a nuvem?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    setLoading(true);
                    
                    WriteBatch batch = firestore.batch();
                    for (Producao p : listaParaEnviar) {
                        p.enviado = true; 
                        batch.set(firestore.collection("producoes").document(), p);
                    }

                    batch.commit().addOnSuccessListener(aVoid -> {
                        try {
                            db.producaoDao().marcarTodasComoEnviadasPorUsuario(usuarioRe);
                            carregarDados();
                            setLoading(false);
                            Toast.makeText(this, "Sincronização concluída!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            setLoading(false);
                            mostrarErro("Dados enviados, mas falha ao atualizar banco local: " + e.getMessage());
                        }
                    }).addOnFailureListener(e -> {
                        setLoading(false);
                        mostrarErro("Falha na rede ou servidor: " + e.getMessage());
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void setLoading(boolean loading) {
        pbLista.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnEnviar.setEnabled(!loading);
        rvProducao.setAlpha(loading ? 0.5f : 1.0f);
    }

    private void mostrarErro(String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle("Ops! Algo deu errado")
                .setMessage(mensagem)
                .setPositiveButton("Entendi", null)
                .show();
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
