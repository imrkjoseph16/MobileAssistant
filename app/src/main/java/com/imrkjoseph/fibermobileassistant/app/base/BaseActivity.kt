package com.imrkjoseph.fibermobileassistant.app.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB: ViewBinding> : AppCompatActivity() {

    lateinit var binding: VB

    abstract val bindingInflater: (LayoutInflater) -> VB

    protected open fun onViewsBound() = Unit

    protected open fun onViewModelBound() { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewBinding()
        onViewModelBound()
        onViewsBound()
    }

    private fun initViewBinding() {
        binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
    }
}