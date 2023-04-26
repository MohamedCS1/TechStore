package com.example.techstore.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.domain.model.ProductsResponse
import com.example.domain.model.ProductsResponseItem
import com.example.techstore.databinding.ActivityMainBinding
import com.example.techstore.util.BitmapUtil.getBitmap
import com.example.techstore.util.ConnectionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val productsViewModel: ProductsViewModel by viewModels()

    lateinit var binding:ActivityMainBinding
    lateinit var productAdapter: ProductAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productAdapter = ProductAdapter()

        initRecyclerView()

        productsViewModel.getProducts()

        lifecycleScope.launch {
            productsViewModel.products.collect { productsResponse ->
                if (productsResponse == null) Toast.makeText(this@MainActivity ,"Something wrong please try again" ,Toast.LENGTH_SHORT).show()
                else
                {
                    productAdapter.submitProductResponse(productsResponse?: ProductsResponse())
                    productsResponse.forEach {
                        product->
                        val productImage = async {getBitmap(this@MainActivity ,product.image?:"")}
                        productsViewModel.insertProduct(ProductsResponseItem(product.category ,product.description ,product.id ,null,productImage.await() ,product.price ,product.rating ,product.title))
                    }
                }
            }
        }

        lifecycleScope.launch {
            productsViewModel.localeProducts.collect{
                productAdapter.setConnectionState(ConnectionState.DISCONNECTED)
                productAdapter.submitProductResponse(it!!)
            }
        }


    }

    fun initRecyclerView() {
        binding.recyclerViewProducts.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(this@MainActivity ,2)
        }
    }


}