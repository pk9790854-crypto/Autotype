package com.example.autotyper

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Snippet(val name: String, val text: String)

class SnippetStore(context: Context) {

    private val prefs = context.getSharedPreferences("autotyper_prefs", Context.MODE_PRIVATE)
    private val key = "snippets_json"

    fun getAll(): List<Snippet> {
        val raw = prefs.getString(key, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Snippet(obj.getString("name"), obj.getString("text"))
        }
    }

    fun saveAll(snippets: List<Snippet>) {
        val arr = JSONArray()
        snippets.forEach { s ->
            val obj = JSONObject()
            obj.put("name", s.name)
            obj.put("text", s.text)
            arr.put(obj)
        }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    fun add(snippet: Snippet) {
        val current = getAll().toMutableList()
        current.add(snippet)
        saveAll(current)
    }

    fun delete(index: Int) {
        val current = getAll().toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            saveAll(current)
        }
    }
}
