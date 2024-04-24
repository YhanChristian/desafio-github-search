package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

      //Callback listener botão
    var btnShareListener: (Repository) -> Unit = {}
    var txtRepositorNameListener: (Repository) -> Unit = {}

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Pega o conteudo da view e troca pela informacao de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //@TODO 8 -  Realizar o bind do viewHolder
        val repository = repositories[position]

        holder.repositoryName.text = repository.name
        holder.btnRepository.setOnClickListener {
            btnShareListener(repository)
        }
        holder.repositoryName.setOnClickListener {
            txtRepositorNameListener(repository)
        }
    }

    // Pega a quantidade de repositorios da lista
    //@TODO 9 - realizar a contagem da lista
    override fun getItemCount(): Int = repositories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val repositoryName: TextView
        val btnRepository: ImageView
        init {
            view.apply {
                repositoryName = findViewById(R.id.tv_repository_name)
                btnRepository = findViewById(R.id.iv_share)
            }
        }
    }
}


