package com.elenakozachenko.instaphoto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

const val NICKNAME = "Alena"
const val CODE_PERMISSION_CAMERA = 100
const val CODE_DATA_CAMERA = 101
const val CODE_PERMISSION_GALLERY = 200
const val CODE_DATA_GALLERY = 201

class MainActivity : AppCompatActivity(), PhotosAdapter.OnItemClickListener {

    lateinit var refresher: SwipeRefreshLayout
    lateinit var rvPhotos: RecyclerView
    lateinit var adapter: PhotosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(NICKNAME)
        refresher = findViewById(R.id.refresher)
        rvPhotos = findViewById(R.id.rv_photos)
        rvPhotos.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = PhotosAdapter(this)
        rvPhotos.adapter = adapter
        loadPhotos()

        refresher.setOnRefreshListener {
            loadPhotos()
        }

        findViewById<FloatingActionButton>(R.id.b_camera).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CODE_PERMISSION_CAMERA)
            }
        }

        findViewById<FloatingActionButton>(R.id.b_gallery).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        CODE_PERMISSION_GALLERY
                )
            }
        }
    }

    fun loadPhotos() {
        refresher.isRefreshing = true
        api.getPhotos().enqueue(object : Callback<MutableList<Photo>> {
            override fun onResponse(call: Call<MutableList<Photo>>, response: Response<MutableList<Photo>>) {
                val photos = response.body() ?: mutableListOf()
                adapter.setPhotos(photos)
                refresher.isRefreshing = false
            }

            override fun onFailure(call: Call<MutableList<Photo>>, t: Throwable) {
                showError()
                refresher.isRefreshing = false
            }
        })
    }

    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CODE_DATA_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, CODE_DATA_GALLERY)
    }

    fun uploadPhoto(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val photoRequest = RequestBody.create(MediaType.parse("image/jpeg"), stream.toByteArray())
        val body = MultipartBody.Part.createFormData(
                "image", "my_image.jpg", photoRequest
        ).body()
        val bodyRequest = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.png", body)
                .addFormDataPart("author", NICKNAME)
                .build()

        api.addPhoto(bodyRequest).enqueue(object : Callback<Photo> {
            override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                val photo = response.body()
                if (photo == null) {
                    return
                }
                adapter.addPhoto(photo)
                rvPhotos.smoothScrollToPosition(adapter.itemCount - 1)
            }

            override fun onFailure(call: Call<Photo>, t: Throwable) {
                showError()
            }
        })
    }

    override fun likePhoto(id: Long, position: Int) {
        api.likePhoto(id, NICKNAME).enqueue(object : Callback<Photo> {
            override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                val photo = response.body()
                if (photo == null) {
                    return
                }
                adapter.updatePhoto(photo, position)
            }

            override fun onFailure(call: Call<Photo>, t: Throwable) {
                showError()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent == null) {
            return
        }
        if (requestCode == CODE_DATA_CAMERA) {
            val bitmap = intent.extras.get("data") as Bitmap
            uploadPhoto(bitmap)
            return
        }
        if (requestCode == CODE_DATA_GALLERY) {
            val uri = intent.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            uploadPhoto(bitmap)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODE_PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }
            CODE_PERMISSION_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                }
            }
        }
    }

    private fun showError() {
        Toast.makeText(this, getString(R.string.error_response), Toast.LENGTH_LONG).show()
    }
}

