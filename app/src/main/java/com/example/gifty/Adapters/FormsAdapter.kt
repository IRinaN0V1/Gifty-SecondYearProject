package com.example.gifty.Adapters

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import com.example.gifty.Form
import com.example.gifty.R

class FormsAdapter(
    private val context: Context,
    private var  forms: List<Form>,
    private val listener: OnItemClickListener,
) : RecyclerView.Adapter<FormsAdapter.FormViewHolder>() {

    inner class FormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val formName: TextView = itemView.findViewById(R.id.form_name)

        init {
            itemView.setOnLongClickListener {
                showPopupMenu(itemView, adapterPosition)
                true
            }
        }

        private fun showPopupMenu(view: View, position: Int) {
            val popupMenu = PopupMenu(context, view)
            val inflater: MenuInflater = popupMenu.menuInflater
            inflater.inflate(R.menu.form_actions_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        val form = forms[position]
                        listener.onEditClicked(form)
                        true
                    }
                    R.id.action_delete -> {
                        val form = forms[position]
                        listener.onDeleteClicked(form)
                        true
                    }
                    R.id.action_make_report -> {
                        val form = forms[position]
                        listener.onReportClicked(form)
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show() // Показываем меню
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forms_list_item, parent, false)
        return FormViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        val form = forms[position]
        holder.formName.text = form.name

        holder.itemView.setOnClickListener {
            listener.onItemClicked(form)
        }
    }


    override fun getItemCount(): Int = forms.size

    interface OnItemClickListener {
        fun onItemClicked(form: Form)
        fun onEditClicked(form: Form)
        fun onDeleteClicked(form: Form)
        fun onReportClicked(form: Form)
    }
}
