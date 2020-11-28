package com.zhuinden.simplestacktutorials.steps.step_8.features.main

import androidx.fragment.app.Fragment
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.get
import com.zhuinden.simplestackextensions.servicesktx.rebind
import com.zhuinden.simplestacktutorials.steps.step_8.core.navigation.FragmentKey
import com.zhuinden.simplestacktutorials.steps.step_8.features.form.FormViewModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MainKey(private val placeholder: String = "") : FragmentKey() {
    override fun instantiateFragment(): Fragment = MainFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(MainViewModel())
            rebind<FormViewModel.ResultHandler>(get<MainViewModel>())
        }
    }
}