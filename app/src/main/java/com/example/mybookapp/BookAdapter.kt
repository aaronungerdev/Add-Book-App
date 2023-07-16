package com.example.mybookapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mybookapp.databinding.RecyclerRowBinding


class BookAdapter (val bookList: ArrayList<Books>): RecyclerView.Adapter<BookAdapter.BooksHolder>() {

    class BooksHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksHolder {
        val binding =RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BooksHolder(binding)
    }

    override fun onBindViewHolder(holder: BooksHolder, position: Int) {
        //List names
        holder.binding.recyclerViewTextView.text = bookList.get(position).name
        //When click an item
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,BookActivity::class.java)
            // When clicked an item go to saved book
            intent.putExtra("info","old")
            intent.putExtra("info",bookList.get(position).id)
            holder.itemView.context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
        return bookList.size
    }

}