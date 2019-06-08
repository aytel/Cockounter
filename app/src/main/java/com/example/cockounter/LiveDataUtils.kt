package com.example.cockounter

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.notify() {
    this.postValue(this.value)
}

class EditableList<T> {
    private val list: MutableList<T> = mutableListOf()
    val liveData = MutableLiveData<MutableList<T>>()

    init {
        liveData.value = list
    }

    fun add(element: T) {
        list.add(element)
        liveData.notify()
    }

    fun removeAt(index: Int) {
        list.removeAt(index)
        liveData.notify()
    }

    fun addAll(items: Collection<T>) {
        list.addAll(items)
        liveData.notify()
    }

    operator fun get(index: Int) = list[index]

    operator fun set(index: Int, element: T) {
        list[index] = element
        liveData.notify()
    }

    val data
    get() = list

}