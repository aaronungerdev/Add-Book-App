package com.example.mybookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybookapp.databinding.ActivityBookBinding
import com.example.mybookapp.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainBinding
    private lateinit var bookList: ArrayList<Books>
    private lateinit var bookAdapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        // Define Arraylist for RecyclerView
        bookList = ArrayList<Books>()

        bookAdapter = BookAdapter(bookList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = bookAdapter

        //Pull data from database for RecyclerView
        try{
            //Get database
            val database=this.openOrCreateDatabase("Books", MODE_PRIVATE,null)
            val cursor=database.rawQuery("SELECT * FROM books",null)
            //Get Index id and name
            val bookNameIx=cursor.getColumnIndex("bookname")
            val idIx=cursor.getColumnIndex("id")

            while(cursor.moveToNext()){
                val name=cursor.getString(bookNameIx)
                val id=cursor.getInt(idIx)
                // Add book items in bookList
                val book=Books(name, id)
                bookList.add(book)
            }

            bookAdapter.notifyDataSetChanged()

            cursor.close()
        }
        catch (e: Exception){
            e.printStackTrace()
        }


    }

    //Create Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Inflater
        val menuInflater =menuInflater
        menuInflater.inflate(R.menu.book_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    //Menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //What activity does the add book element redirect to
        if(item.itemId==R.id.add_book_item){
            val intent= Intent(this@MainActivity,BookActivity::class.java)
            // Add a new book
            intent.putExtra("info","new")
            startActivity(intent)

        }

        return super.onOptionsItemSelected(item)
    }
}