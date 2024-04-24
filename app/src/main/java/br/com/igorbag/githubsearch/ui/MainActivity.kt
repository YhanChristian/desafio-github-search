package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var nomeUsuario: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var listaRepositories: RecyclerView
    private lateinit var githubApi: GitHubService
    private lateinit var adapter : RepositoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
        try {
            getAllReposByUserName()
        } catch(e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    private fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            Log.i(TAG, "Btn confirmar clicado!")
            saveUserLocal()
            getAllReposByUserName()
        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        //@TODO 3 - Persistir o usuario preenchido na editText com a SharedPref no listener do botao salvar
        val sharedPreferences =  getPreferences(Context.MODE_PRIVATE) ?: return
        val username = nomeUsuario.text.toString()
        if(username.isEmpty()) {
            Toast.makeText(this, getString(R.string.failure_username_empty), Toast.LENGTH_SHORT).show()
            return
        }
        with(sharedPreferences.edit()) {
            putString(getString(R.string.saved_user_name), username)
            apply()
        }
    }

    private fun showUserName() {
        //@TODO 4- depois de persistir o usuario exibir sempre as informacoes no EditText  se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(getString(R.string.saved_user_name), null)
        username?.let { nomeUsuario.setText(it) }
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    private fun setupRetrofit() {
        /*
           @TODO 5 -  realizar a Configuracao base do retrofit
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
           URL_BASE da API do  GitHub= https://api.github.com/
           lembre-se de utilizar o GsonConverterFactory mostrado no curso
        */
        val retroft = Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        githubApi = retroft.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    private fun getAllReposByUserName() {
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(getString(R.string.saved_user_name), null)
        try {
            username?.let {
                val call = githubApi.getAllRepositoriesByUser(it)
                call.enqueue(object : Callback<List<Repository>> {
                    override fun onResponse(
                        call: Call<List<Repository>>,
                        response: Response<List<Repository>>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { repositories ->
                                Log.i(TAG, repositories.toString())
                                if(repositories.isNotEmpty()) {
                                    setupAdapter(repositories)
                                    successSearchRepository()
                                } else {
                                    Toast.makeText(this@MainActivity, getString(R.string.empty_repositories), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                        errorSearchRepository()
                    }
                })
            }
        } catch (e: Exception) {
            errorSearchRepository()
        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        /*
            @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo
            passando a listagem dos repositorios
         */
        adapter = RepositoryAdapter(list)
        listaRepositories.adapter = adapter

        with(adapter) {
            btnShareListener = {
                shareRepositoryLink(it.htmlUrl)
            }
            txtRepositorNameListener = {
                openBrowser(it.htmlUrl)
            }
        }
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter
    private fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - Colocar esse metodo no click item do adapter
    private fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

    private fun successSearchRepository() {
        Toast.makeText(this@MainActivity, R.string.success_to_get_repos, Toast.LENGTH_LONG).show()
        Log.i(TAG, "Sucesso ao buscar os repositorios")

    }

    private fun errorSearchRepository() {
        Toast.makeText(this@MainActivity, R.string.failure_to_get_repos, Toast.LENGTH_LONG).show()
        Log.i(TAG, "Erro ao buscar os repositorios")
        nomeUsuario.setText("")
    }
    companion object {
        const val TAG = "MainActivity"
        const val URL_BASE = "https://api.github.com/"
    }
}