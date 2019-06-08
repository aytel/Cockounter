package com.example.cockounter.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.cockounter.R
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class EditLibraryViewModel() : ViewModel() {
    var name: String = ""
    var source: String = ""

    constructor(name: String, source: String) : this() {
        this.name = name
        this.source = source
    }
}

/**
 * Activity for editing a lua library
 */
class EditLibraryActivity : AppCompatActivity() {

    companion object {
        /**
         * Key for library name passed to the activity
         */
        const val ARG_NAME = "ARG_NAME"
        /**
         * Key for library source passed to the activity
         */
        const val ARG_SOURCE = "ARG_SOURCE"
        /**
         * Key for library name returned to the previous activity
         */
        const val RETURN_NAME = "RETURN_NAME"
        /**
         * Key for library source returned to the previous activity
         */
        const val RETURN_SOURCE = "RETURN_SOURCE"
    }

    private lateinit var viewModel: EditLibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra(ARG_NAME)
        val source = intent.getStringExtra(ARG_SOURCE)
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return EditLibraryViewModel(name, source) as T
            }

        }).get(EditLibraryViewModel::class.java)
        EditLibraryUI(viewModel.name, viewModel.source).setContentView(this)
    }

    private fun save() {
        val result = Intent()
        result.run {
            putExtra(RETURN_NAME, viewModel.name)
            putExtra(RETURN_SOURCE, viewModel.source)
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onBackPressed() {
        alert {
            message = "Save changes?"
            yesButton {
                save()
            }
            noButton {
                finish()
            }
        }.show()
    }

    private fun updateName(name: String) {
        viewModel.name = name
    }

    private fun updateSource(source: String) {
        viewModel.source = source
    }

    private class EditLibraryUI(val name: String?, val source: String?) : AnkoComponent<EditLibraryActivity> {
        override fun createView(ui: AnkoContext<EditLibraryActivity>): View = with(ui) {
            coordinatorLayout {
                appBarLayout {
                    lparams(matchParent, wrapContent) {
                    }
                    toolbar {
                        title = "Edit library"
                        menu.apply {
                            add("Save").apply {
                                setIcon(R.drawable.ic_done_black_24dp)
                                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                                setOnMenuItemClickListener {
                                    owner.save()
                                    true
                                }
                            }
                        }
                    }.lparams(width = matchParent, height = wrapContent) {
                        scrollFlags = 0
                    }
                }
                scrollView {
                    verticalLayout {
                        editText(name ?: "") {
                            hint = "Name"
                            textChangedListener {
                                onTextChanged { chars, _, _, _ ->
                                    owner.updateName(chars.toString())
                                }
                            }
                        }
                        editText(source ?: "") {
                            textChangedListener {
                                onTextChanged { chars, _, _, _ ->
                                    owner.updateSource(chars.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
