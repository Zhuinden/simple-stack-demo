package com.zhuinden.simplestackdemoexamplemvp.features.taskdetail

import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestackdemoexamplemvp.core.mvp.BasePresenter
import com.zhuinden.simplestackdemoexamplemvp.core.navigation.getKey
import com.zhuinden.simplestackdemoexamplemvp.data.models.Task
import com.zhuinden.simplestackdemoexamplemvp.data.repository.TaskRepository
import com.zhuinden.simplestackdemoexamplemvp.features.addoredittask.AddOrEditTaskKey
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Owner on 2017. 01. 27..
 */

class TaskDetailPresenter(
    private val taskRepository: TaskRepository,
    private val backstack: Backstack
) : BasePresenter<TaskDetailView>(), TaskDetailView.Presenter {
    lateinit var taskDetailKey: TaskDetailKey

    var taskId: String = ""

    var task: Task? = null

    override fun onAttach(view: TaskDetailView) {
        taskDetailKey = Backstack.getKey(view.context)
        this.taskId = taskDetailKey.taskId
        taskRepository.findTask(taskId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { taskOptional ->
                val task = taskOptional.takeIf { it.isPresent }?.get()
                this.task = task

                if (task != null) {
                    view.showTask(task)
                } else {
                    view.showMissingTask()
                }
            }
    }

    override fun onDetach(view: TaskDetailView) {
    }

    override fun onTaskEditButtonClicked() {
        if (taskId.isEmpty()) {
            view!!.showMissingTask()
            return
        }
        backstack.goTo(AddOrEditTaskKey.EditTaskKey(view!!.getKey(), taskId))
    }

    fun completeTask(task: Task) {
        taskRepository.setTaskCompleted(task)
    }

    fun activateTask(task: Task) {
        taskRepository.setTaskActive(task)
    }

    override fun onTaskChecked(task: Task, checked: Boolean) {
        if (checked) {
            completeTask(task)
        } else {
            activateTask(task)
        }
    }

    override fun onTaskDeleteButtonClicked() {
        if (task != null) {
            taskRepository.deleteTask(task!!)
            backstack.goBack()
        }
    }
}
