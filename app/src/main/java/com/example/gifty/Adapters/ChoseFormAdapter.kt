package com.example.gifty.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gifty.Adapters.FormsAdapter.FormViewHolder
import com.example.gifty.Form
import com.example.gifty.R

class ChoseFormAdapter(
    private val context: Context,
    private var  forms: List<Form>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ChoseFormAdapter.ChoseFormViewHolder>()  {
    inner class ChoseFormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val formName: TextView = itemView.findViewById(R.id.form_name)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoseFormViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forms_list_item, parent, false)
        return ChoseFormViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChoseFormViewHolder, position: Int) {
        val form = forms[position]
        holder.formName.text = form.name

        holder.itemView.setOnClickListener {
            listener.onItemClicked(form)
        }
    }
    override fun getItemCount(): Int = forms.size
    interface OnItemClickListener {
        fun onItemClicked(form: Form)
    }
}

