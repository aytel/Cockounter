package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra(ARG_NAME)
        val source = intent.getStringExtra(ARG_SOURCE)
        EditLibraryUI(name, source).setContentView(this)
    }

    /**
     * Returns the edited or new library to previous activity
     */
    fun save(name: String, source: String) {
        val result = Intent()
        result.run {
            putExtra(RETURN_NAME, name)
            putExtra(RETURN_SOURCE, source)
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}

/**
 * UI for [EditLibraryActivity]
 */
private class EditLibraryUI(val name: String?, val source: String?) : AnkoComponent<EditLibraryActivity> {
    override fun createView(ui: AnkoContext<EditLibraryActivity>): View = with(ui) {
        scrollView {
            verticalLayout {
                val nameText = editText(name ?: "") {
                    hint = "Name"
                }
                val sourceText = editText(source ?: "") {

                }
                button("Save") {
                    onClick {
                        owner.save(nameText.text.toString(), sourceText.text.toString())
                    }
                }
            }
        }
    }
}
