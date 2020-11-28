package com.zhuinden.simplestackdemoexamplemvp.features.tasks

import android.view.View
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestack.navigator.ViewChangeHandler
import com.zhuinden.simplestack.navigator.changehandlers.SegueViewChangeHandler
import com.zhuinden.simplestackdemoexamplemvp.R
import com.zhuinden.simplestackdemoexamplemvp.core.navigation.ViewKey
import com.zhuinden.simplestackdemoexamplemvp.data.repository.TaskRepository
import com.zhuinden.simplestackdemoexamplemvp.util.MessageQueue
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import com.zhuinden.simplestackextensions.servicesktx.lookup
import kotlinx.parcelize.Parcelize

@Parcelize
data class TasksKey(val placeholder: String) : ViewKey, DefaultServiceProvider.HasServices {
    override fun getScopeTag(): String = "Tasks"

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            addService(TasksView.CONTROLLER_TAG, TasksPresenter(
                backstack,
                lookup<TaskRepository>(),
                lookup<MessageQueue>()
            ))
        }
    }

    constructor() : this("")

    override fun layout(): Int = R.layout.path_tasks

    override val isFabVisible: Boolean
        get() = true

    override fun viewChangeHandler(): ViewChangeHandler = SegueViewChangeHandler()

    override fun menu(): Int = R.menu.tasks_fragment_menu

    override fun navigationViewId(): Int = R.id.list_navigation_menu_item

    override fun shouldShowUp(): Boolean = false

    override fun fabClickListener(view: View): View.OnClickListener =
        View.OnClickListener { v ->
            val tasksView = view as TasksView
            tasksView.openAddNewTask()
        }

    override fun fabDrawableIcon(): Int = R.drawable.ic_add
}
