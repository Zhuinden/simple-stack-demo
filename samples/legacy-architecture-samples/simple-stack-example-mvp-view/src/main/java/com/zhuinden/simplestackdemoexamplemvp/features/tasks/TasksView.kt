package com.zhuinden.simplestackdemoexamplemvp.features.tasks

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestackdemoexamplemvp.R
import com.zhuinden.simplestackdemoexamplemvp.application.MainActivity
import com.zhuinden.simplestackdemoexamplemvp.core.mvp.MvpPresenter
import com.zhuinden.simplestackdemoexamplemvp.data.models.Task
import com.zhuinden.simplestackdemoexamplemvp.databinding.PathTasksBinding
import com.zhuinden.simplestackdemoexamplemvp.util.*
import java.util.*

/**
 * Created by Owner on 2017. 01. 26..
 */

class TasksView : ScrollChildSwipeRefreshLayout, MainActivity.OptionsItemSelectedListener, StateChanger, MessageQueue.Receiver {
    companion object {
        const val CONTROLLER_TAG = "TasksView.Presenter"
    }

    interface Presenter: MvpPresenter<TasksView> {
        fun onTaskCheckClicked(task: Task)

        fun onTaskRowClicked(task: Task)

        fun onNoTasksAddButtonClicked()

        fun onFilterActiveSelected()

        fun onFilterCompletedSelected()

        fun onFilterAllSelected()

        fun onClearCompletedClicked()

        fun onRefreshClicked()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    lateinit var binding: PathTasksBinding

    private val tasksPresenter by lazy { Navigator.lookupService<Presenter>(context, CONTROLLER_TAG) }

    lateinit var tasksAdapter: TasksAdapter

    private val taskItemListener: TasksAdapter.TaskItemListener = object : TasksAdapter.TaskItemListener {
        override fun onTaskCheckClicked(task: Task) {
            tasksPresenter.onTaskCheckClicked(task)
        }

        override fun onTaskRowClicked(task: Task) {
            tasksPresenter.onTaskRowClicked(task)
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_filter -> {
                showFilteringPopupMenu()
                return true
            }
            R.id.menu_clear -> {
                tasksPresenter.onClearCompletedClicked()
                return true
            }
            R.id.menu_refresh -> {
                refresh()
                return true
            }
        }
        return false
    }

    override fun handleStateChange(stateChange: StateChange, completionCallback: StateChanger.Callback) {
        // hack fix from  http://stackoverflow.com/a/27073879/2413303 to fix view staying on screen
        isRefreshing = false
        destroyDrawingCache()
        clearAnimation()
        // end
        completionCallback.stateChangeComplete()
    }

    class SavedSuccessfullyMessage

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = PathTasksBinding.bind(this)

        binding.buttonNoTasksAdd.setOnClickListener {
            tasksPresenter.onNoTasksAddButtonClicked()
        }

        tasksAdapter = TasksAdapter(ArrayList(0), taskItemListener)
        binding.recyclerTasks.adapter = tasksAdapter
        binding.recyclerTasks.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        setColorSchemeColors(ContextCompat.getColor(this.context, R.color.colorPrimary),
            ContextCompat.getColor(this.context, R.color.colorAccent),
            ContextCompat.getColor(this.context, R.color.colorPrimaryDark))
        // Set the scrolling view in the custom SwipeRefreshLayout.
        setScrollUpChild(binding.recyclerTasks)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        tasksPresenter.attachView(this)
        setOnRefreshListener { this.refresh() }
    }

    override fun onDetachedFromWindow() {
        setOnRefreshListener(null)
        tasksPresenter.detachView(this)
        super.onDetachedFromWindow()
    }

    override fun receiveMessage(message: Any) {
        if (message is SavedSuccessfullyMessage) {
            showSuccessfullySavedMessage()
        }
    }

    fun calculateDiff(tasks: List<Task>): Pair<DiffUtil.DiffResult, List<Task>> =
        Pair(DiffUtil.calculateDiff(TasksDiffCallback(tasksAdapter.data, tasks)), tasks)

    fun hideEmptyViews() {
        binding.tasksView.show()
        binding.viewNoTasks.hide()
    }

    fun showTasks(pairOfDiffResultAndTasks: Pair<DiffUtil.DiffResult, List<Task>>, filterType: TasksFilterType) {
        val diffResult = pairOfDiffResultAndTasks.first
        val tasks = pairOfDiffResultAndTasks.second
        tasksAdapter.data = tasks
        diffResult.dispatchUpdatesTo(tasksAdapter)
        if (tasks.isEmpty()) {
            filterType.showEmptyViews(this)
        } else {
            hideEmptyViews()
        }
    }

    fun showFilteringPopupMenu() {
        val popup = PopupMenu(this.context, context.findActivity<Activity>().findViewById(R.id.menu_filter))
        popup.menuInflater.inflate(R.menu.filter_tasks, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.active -> tasksPresenter.onFilterActiveSelected()
                R.id.completed -> tasksPresenter.onFilterCompletedSelected()
                else -> tasksPresenter.onFilterAllSelected()
            }
            //loadTasks(false); // reactive data source ftw
            true
        }

        popup.show()
    }

    @SuppressLint("CheckResult")
    fun refresh() {
        tasksPresenter.onRefreshClicked()
    }

    fun showNoActiveTasks() {
        showNoTasksViews(context.getString(R.string.no_tasks_active), R.drawable.ic_check_circle_24dp, false)
    }

    fun showNoTasks() {
        showNoTasksViews(context.getString(R.string.no_tasks_all), R.drawable.ic_assignment_turned_in_24dp, false)
    }

    fun showNoCompletedTasks() {
        showNoTasksViews(context.getString(R.string.no_tasks_completed), R.drawable.ic_verified_user_24dp, false)
    }

    fun showTaskMarkedComplete() {
        showMessage(context.getString(R.string.task_marked_complete))
    }

    fun showTaskMarkedActive() {
        showMessage(context.getString(R.string.task_marked_active))
    }

    fun showCompletedTasksCleared() {
        showMessage(context.getString(R.string.completed_tasks_cleared))
    }

    fun showLoadingTasksError() {
        showMessage(context.getString(R.string.loading_tasks_error))
    }

    fun showSuccessfullySavedMessage() {
        showMessage(context.getString(R.string.successfully_saved_task_message))
    }

    private fun showMessage(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showNoTasksViews(mainText: String, iconRes: Int, showAddView: Boolean) {
        binding.tasksView.hide()
        binding.viewNoTasks.show()

        binding.textNoTasksMain.text = mainText
        binding.imageNoTasksIcon.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
        binding.buttonNoTasksAdd.showIf { showAddView }
    }

    fun setFilterLabelText(filterText: Int) {
        binding.textFilterLabel.setText(filterText)
    }

    fun openAddNewTask() {
        tasksPresenter.onNoTasksAddButtonClicked()
    }
}
