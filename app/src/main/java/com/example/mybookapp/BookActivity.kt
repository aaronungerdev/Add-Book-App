package com.example.mybookapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Binder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.example.mybookapp.databinding.ActivityBookBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.lang.Exception

class BookActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityBookBinding
    private  lateinit var  activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissonLauncher : ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    private lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityBookBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        registerLauncher()
        // Create Database
        database=this.openOrCreateDatabase("Books", MODE_PRIVATE,null)

        val intent = intent
        val info = intent.getStringExtra("info")
        if(info.equals("new")){
            binding.txtName.setText("")
            binding.txtAuthor.setText("")
            binding.txtYear.setText("")
            binding.imageView.setImageResource(R.drawable.add)
            binding.btnSave.visibility = View.VISIBLE
        }
        else
        {
            binding.btnSave.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)

            val cursor = database.rawQuery("SELECT * FROM books WHERE id=?", arrayOf(selectedId.toString()))
            val bookNameIx=cursor.getColumnIndex("bookname")
            val bookAuthorIx=cursor.getColumnIndex("bookauthor")
            val bookYearIx=cursor.getColumnIndex("bookyear")
            val imageIx=cursor.getColumnIndex("image")
            while (cursor.moveToNext()){
                binding.txtName.setText(cursor.getString(bookNameIx))
                binding.txtAuthor.setText(cursor.getString(bookAuthorIx))
                binding.txtYear.setText(cursor.getString(bookYearIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()



        }
    }

    // save button
    fun saveBook(view : View){
        val bookName= binding.txtName.text.toString()
        val bookAuthor= binding.txtAuthor.text.toString()
        val bookYear= binding.txtYear.text.toString()

        //Check to nullability selectedBitmap and call smallerBitmap fun
        if(selectedBitmap!=null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            //Convert image to binary
            val outputStream=ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50, outputStream)
            val byteArray=outputStream.toByteArray()

            try{
                //Insert items
                database.execSQL("CREATE TABLE IF NOT EXISTS books(id INTEGER PRIMARY KEY, bookname VARCHAR, bookauthor VARCHAR,bookyear VARCHAR, image BLOB)")
                val sqlString= "INSERT INTO books (bookname,bookauthor,bookyear,image) VALUES (?,?,?,?)"
                //Connect  rows
                val statement=database.compileStatement(sqlString)
                statement.bindString(1,bookName)
                statement.bindString(2,bookAuthor)
                statement.bindString(3,bookYear)
                statement.bindBlob(4,byteArray)
                statement.execute()
            }
            catch (e: Exception){
                e.printStackTrace()
            }

            //Close Activity
            val intent=Intent(this@BookActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }

    // Minimize Image
    private fun makeSmallerBitmap(image : Bitmap, maximumSize : Int) : Bitmap{
        //Ratio
        var width=image.width
        var height=image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if(bitmapRatio>1){
            //Landscape
            width=maximumSize
            val scaledSize= width/ bitmapRatio
            height=scaledSize.toInt()
        }
        else{
            //Portrait
            height=maximumSize
            val scaledSize=height/bitmapRatio
            width=scaledSize.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)

    }
    //addImage button
    //Permission
    fun addImage(view : View){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //Android +33 -> READ_MEDIA_IMAGES permission
            // Permission Granted
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //Show Rationale
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    //Added button for give permission
                    Snackbar.make(view,"Permission needed to gallery!",Snackbar.LENGTH_INDEFINITE).setAction("Give permission",View.OnClickListener {
                        //Request Permission
                        permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }
                else{
                    // Request Permission
                    permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            //Permission Denied
            else{
                // Go to gallery
                val intentToGallery= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }

        }
        else{
            //Android 32- -> READ_EXTERNAL_STORAGE permission
            // Permission Granted
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //Show Rationale
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //Added button for give permission
                    Snackbar.make(view,"Permission needed to gallery!",Snackbar.LENGTH_INDEFINITE).setAction("Give permission",View.OnClickListener {
                        //Request Permission
                        permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }
                else{
                    // Request Permission
                    permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            //Permission Denied
            else{
                // Go to gallery
                val intentToGallery= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
        }


    }
    //Go to gallery and get photo
    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if(result.resultCode == RESULT_OK){
                val intentFromResult =result.data
                if(intentFromResult != null){
                    val imageData = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)
                    // Need to convert bitmap and shrink
                    //Check to null for imageData
                    if(imageData !=null) {

                            try {
                                if(Build.VERSION.SDK_INT>=28) {
                                    val source = ImageDecoder.createSource(this@BookActivity.contentResolver, imageData)
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                }
                                else{
                                    selectedBitmap =MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }


                    }
                }
            }

        }
        // Request Permission
        permissonLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                //Permission Granted
                val intentToGallery= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else{
                //Permission Denied
                Toast.makeText(this@BookActivity,"Permission Needed!",Toast.LENGTH_LONG).show()
            }
        }

    }
}