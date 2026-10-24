package com.seuapp.controledeprodutividade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProducaoAdapter extends RecyclerView.Adapter<ProducaoAdapter.ProducaoViewHolder> {

    private List<Producao> listaProducao = new ArrayList<>();
    private OnEditClickListener listener;

    public interface OnEditClickListener {
        void onEditClick(Producao producao);
    }

    public ProducaoAdapter(OnEditClickListener listener) {
        this.listener = listener;
    }

    public void setLista(List<Producao> lista) {
        this.listaProducao = lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProducaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producao, parent, false);
        return new ProducaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProducaoViewHolder holder, int position) {
        Producao producao = listaProducao.get(position);
        
        holder.tvOperador.setText(producao.operador + " [" + producao.secao + "]");
        
        String labelQtd = holder.itemView.getContext().getString(R.string.label_valor);
        holder.tvQuantidade.setText(labelQtd + ": " + producao.quantidade);
        holder.tvData.setText(producao.data);

        // Exibir anotação se existir
        if (producao.anotacao != null && !producao.anotacao.trim().isEmpty()) {
            holder.tvAnotacao.setText("Obs: " + producao.anotacao);
            holder.tvAnotacao.setVisibility(View.VISIBLE);
        } else {
            holder.tvAnotacao.setVisibility(View.GONE);
        }

        // Ao clicar no item, abre a edição
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(producao);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProducao.size();
    }

    static class ProducaoViewHolder extends RecyclerView.ViewHolder {
        TextView tvOperador, tvQuantidade, tvData, tvAnotacao;

        public ProducaoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOperador = itemView.findViewById(R.id.tvItemOperador);
            tvQuantidade = itemView.findViewById(R.id.tvItemQuantidade);
            tvData = itemView.findViewById(R.id.tvItemData);
            tvAnotacao = itemView.findViewById(R.id.tvItemAnotacao);
        }
    }
}
