package com.tareas2p.entrevistas.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.tareas2p.entrevistas.R;
import com.tareas2p.entrevistas.config.Entrevista;

import java.io.File;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<Entrevista> datos;
    private LayoutInflater inflater;
    private Context context;
    public static int selectedItem = -1;

    public ListAdapter(List<Entrevista> itemList, Context context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.datos = itemList;
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    public static int getSelectedItem() {
        return selectedItem;
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewtype) {
        View view = inflater.inflate(R.layout.disenio, null);
        return new ListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, final int position) {
        holder.bindData(datos.get(position));

        //Funcionamiento del selector
        final int currentPosition = position;
        holder.itemView.setSelected(position == selectedItem);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedItem = currentPosition;
                // Notificar al adaptador de los cambios
                notifyDataSetChanged();
            }
        });
    }

    public void setItems(List<Entrevista> items) {
        datos = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descripcion, periodista, fecha;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView2);
            descripcion = (TextView) itemView.findViewById(R.id.txtDescripcion2);
            periodista=(TextView) itemView.findViewById(R.id.txtPeriodista2);
            fecha=(TextView) itemView.findViewById(R.id.txtFecha2);
        }

        void bindData(final Entrevista entrevista) {
            descripcion.setText(entrevista.getDescripcion());
            periodista.setText(entrevista.getPeriodista());
            fecha.setText(entrevista.getFecha());
            String imageUrl = entrevista.getFoto();
        //    File audio=new File(entrevista.getAudio());
            //imageView.setImageURI(Uri.parse(imageUrl));
            //Glide.with(this).load(imageUrl).into(imageView);
            Picasso.get().load(imageUrl).into(imageView);
        }
    }

    public void setFilteredList(List<Entrevista> filteredList) {
        this.datos = filteredList;
        notifyDataSetChanged();
    }
}