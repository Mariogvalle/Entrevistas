package com.tareas2p.entrevistas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tareas2p.entrevistas.adapter.ListAdapter;
import com.tareas2p.entrevistas.config.Entrevista;

import java.util.ArrayList;
import java.util.List;

public class ActivityLista extends AppCompatActivity {

    ListAdapter listAdapter;
    SearchView searchView;

    List<Entrevista> listEntrevista;
    private FirebaseFirestore db;

    Button eliminar, actualizar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        db = FirebaseFirestore.getInstance();

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.clearFocus();
        eliminar = (Button) findViewById(R.id.btnEliminar);
        actualizar=(Button) findViewById(R.id.btnActualizar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        eliminar.setOnClickListener(View ->{
            if (ListAdapter.getSelectedItem() != -1) {
                // Mostrar un diálogo de confirmación de eliminación
                alertaEliminar();
            } else {
                // Mostrar un mensaje si no se ha seleccionado ningún contacto
                Toast.makeText(ActivityLista.this, "Selecciona un contacto primero", Toast.LENGTH_SHORT).show();
            }
        });


        llenarLista();
    }

    private void llenarLista() {
        obtenerDatos();
        listAdapter=new ListAdapter(listEntrevista,this);

        RecyclerView recyclerView = findViewById(R.id.listRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);
    }
    private void obtenerDatos() {
        listEntrevista = new ArrayList<>();
        db.collection("entrevista")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                // se obtienen los los datos y agregar a la lista
                                String descripcion = document.getString("descripcion");
                                String periodista = document.getString("periodista");
                                String fecha = document.getString("fecha");
                                String imageUrl = document.getString("foto");
                                String documentId = document.getId();
                                listEntrevista.add(new Entrevista(documentId,descripcion,periodista,fecha,imageUrl,""));
                            }
                            // Notifica al adaptador que los datos han cambiado
                            listAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("Error obteniendo information", "");
                        }
                    }
                });
    }



    private void filter(String text) {
        List<Entrevista> filteredList = new ArrayList<>();
        for (Entrevista entrevista : listEntrevista) {
            String descripcion = entrevista.getDescripcion() + " " + entrevista.getPeriodista();
            if (descripcion.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(entrevista);
            }
        }

        if (!filteredList.isEmpty()) {
            listAdapter.setFilteredList(filteredList);
        }
    }

    private void alertaEliminar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLista.this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Desea eliminar los datos de la entrevista seleccionada?");

        // Agregar botón de actualizar
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtener el contacto seleccionado
                int selectedItemIndex = ListAdapter.getSelectedItem();
                if (selectedItemIndex != -1) {
                      onDeleteClick(selectedItemIndex);
//                    Entrevista entrevista = listEntrevista.get(selectedItemIndex);
//                    eliminarEntrevista(entrevista);
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Si el usuario cancela la eliminación, no hacer nada
            }
        });

        builder.show();
    }

    public void onDeleteClick(int position) {
        String entrevistaId = listEntrevista.get(position).getId();
        DocumentReference docRef = db.collection("entrevista").document(entrevistaId);

        // Eliminar el documento de la colección
        docRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listEntrevista.remove(position);
                        listAdapter.notifyItemRemoved(position);
                        Toast.makeText(getApplicationContext(), "Entrevista eliminada correctamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error al eliminar entrevista", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}