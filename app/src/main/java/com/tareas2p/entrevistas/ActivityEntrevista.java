package com.tareas2p.entrevistas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tareas2p.entrevistas.config.Entrevista;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityEntrevista extends AppCompatActivity {
    //uso del mic

    private static final int MICROPHONE_PERMISSION_CODE = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private static final int PERMISSION_REQUEST_CODE = 1;
    private MediaRecorder mediaRecorder;
    private String outputFile;
    //
    static final int REQUEST_IMAGE = 101;
    static final int ACCESS_CAMERA = 201;
    static final int ACCESS_AUDIO = 1001;

    String currentPhotoPath,descripcion2, periodista2, fecha2, urlImagen, urlAudio,currentAudioPath ;
    Button entrevistas, guardarEntrevista, tomarFoto, tomarAudio, detenerAudio;
    EditText descripcion,periodista, fecha;
    ImageView imageView;
    FirebaseFirestore db;
    File foto, audio;
    Uri photoURI, audioURI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrevista);
        db = FirebaseFirestore.getInstance();
        entrevistas = (Button) findViewById(R.id.btnEntrevistas);
        tomarFoto = (Button) findViewById(R.id.btnTomarFoto);
        tomarAudio = (Button) findViewById(R.id.btnIniciarAudio);
        detenerAudio = (Button) findViewById(R.id.btnStopAudio);
        guardarEntrevista = (Button) findViewById(R.id.btnGuardar);
        descripcion = (EditText) findViewById(R.id.txtDescripcion);
        periodista = (EditText) findViewById(R.id.txtPeriodista);
        fecha = (EditText) findViewById(R.id.txtFecha);
        imageView = (ImageView) findViewById(R.id.imageView);

        entrevistas.setOnClickListener(View -> {
            Intent intent = new Intent(getApplicationContext(), ActivityLista.class);
            startActivity(intent);
        });

        tomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermisosCamara();
            }
        });

        tomarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisosMic();
            }
        });

        detenerAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudio();
            }
        });

        tomarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisosMic();
            }
        });


        guardarEntrevista.setOnClickListener(View -> {
            descripcion2 = this.descripcion.getText().toString().trim();
            periodista2 = this.periodista.getText().toString().trim();
            fecha2 = this.fecha.getText().toString().trim();
            if (descripcion2.isEmpty() || periodista2.isEmpty() || fecha2.isEmpty() || foto==null) {
                Toast.makeText(this, "Llenar todos los campos.", Toast.LENGTH_LONG).show();
            } else {
                cargaStorage();
            }
        });


    }

    private String getFilePathAudio(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File audioDirectorio = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(audioDirectorio,"test" + ".mp3");
        return file.getPath();
    }
    private void stopAudio() {
       if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Toast.makeText(this, "Grabación iniciada", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRecording() {
        // Definir la ruta de salida para el archivo de audio
        File audioFile = null;
        try {
            audioFile = createAudioFile();
        } catch (IOException ex) {
            ex.toString();
        }
        audioURI = FileProvider.getUriForFile(this,
                "com.tareas2p.entrevistas.fileprovider",
                audioFile);
        // Configurar MediaRecorder y comenzar a grabar
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(this, "Grabación iniciada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargaStorage() {
        // Obtén una referencia al Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        //Obtener nombre de archivo de photoURI
        StorageReference imagesRef = storageRef.child("images").child(obtenerNombreArchivo(photoURI));
        //StorageReference audiosRef = storageRef.child("audios").child(obtenerNombreArchivo(audioURI));

        // Sube la imagen a Firebase Storage
        UploadTask uploadTask = imagesRef.putFile(photoURI);
        //UploadTask uploadTask2 = audiosRef.putFile(audioURI);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // La imagen se subió exitosamente
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String urlImagen = uri.toString();
                        Entrevista nuevaEntrevista = new Entrevista(descripcion2, periodista2, fecha2,urlImagen,"");
                        crear(nuevaEntrevista);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Hubo un error al subir la imagen
            }
        });

    }

    private void crear(Entrevista nuevaEntrevista) {
        db.collection("entrevista")
                .add(nuevaEntrevista)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // El documento ha sido creado exitosamente
                        Toast.makeText(getApplicationContext(), "Entrevista guardado exitosamente", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Ocurrió un error al intentar crear el documento
                        Toast.makeText(getApplicationContext(), "Error al crear entrevista: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void limpiarCampos() {
        descripcion.setText("");
        periodista.setText("");
        fecha.setText("");
        descripcion.setFocusableInTouchMode(true);
        descripcion.requestFocus();
        if(imageView!=null){
            imageView.setImageURI(null);
        }
    }

    private void permisosMic(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        } else {
            startRecording();
        }
    }

    private void PermisosCamara() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, ACCESS_CAMERA);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.toString();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                //Uri
                        photoURI = FileProvider.getUriForFile(this,
                        "com.tareas2p.entrevistas.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            }
        }
    }

    private File createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFileName = "AUD_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File audio = File.createTempFile(
                audioFileName,  /* prefix */
                ".mp3",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentAudioPath = audio.getAbsolutePath();
        return audio;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACCESS_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getApplicationContext(), "Se necesita permiso de la camara.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            try {
                foto = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile(foto));
            } catch (Exception ex) {
                ex.toString();
            }
        }
    }

    public String obtenerNombreArchivo(Uri videoUri) {
        String nombreArchivo = null;
        if (videoUri != null) {
            // Utiliza un ContentResolver para obtener el nombre del archivo desde la Uri
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(videoUri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    nombreArchivo = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return nombreArchivo;
    }

}