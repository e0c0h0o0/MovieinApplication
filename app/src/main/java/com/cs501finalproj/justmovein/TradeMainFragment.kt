package com.cs501finalproj.justmovein

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs501finalproj.justmovein.util.UiUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TradeMainFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var searchBar: EditText
    private lateinit var tabLayout: TabLayout
    private var itemList: MutableList<Item> = mutableListOf()  // Use MutableList for easy updates
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: ItemAdapter
    private lateinit var imgNoResults: ImageView
    private lateinit var txtNoResults: TextView
    private lateinit var categorySpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trade_main, container, false)
        setupViews(view)
        initializeRecyclerView()
        setupSearchBar()
        setupFab()
        setupTabLayout()
        return view
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        fabAddItem = view.findViewById(R.id.fab_add_item)
        searchBar = view.findViewById(R.id.search_bar)
        tabLayout = view.findViewById(R.id.tabs)
        imgNoResults = view.findViewById(R.id.imgNoResults)
        txtNoResults = view.findViewById(R.id.txtNoResults)
        databaseReference = FirebaseDatabase.getInstance().getReference("Items")
        categorySpinner = view.findViewById(R.id.category_spinner)

        val categoryArray = resources.getStringArray(R.array.category_array)
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryArray)

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorySpinner.adapter = categoryAdapter
        categorySpinner.prompt = getString(R.string.category_prompt)

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position).toString()
                filterItemsByCategory(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapter.updateItemList(itemList)
                toggleNoResultsView(itemList.isEmpty())
            }
        }
    }

    private fun initializeRecyclerView() {
        adapter = ItemAdapter(mutableListOf(), "grid")
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter
    }

    private fun setupSearchBar() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterItems(s.toString())
            }
        })
    }

    private fun setupFab() {
        fabAddItem.setOnClickListener {
            val intent = Intent(activity, ListItemActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupTabLayout() {
        tabLayout.apply {
            addTab(this.newTab().setText("Browse"))
            addTab(this.newTab().setText("Liked"))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    fetchItemsFromDatabase(tab?.text.toString())
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    fetchItemsFromDatabase(tab?.text.toString())
                }
            })
        }
        tabLayout.getTabAt(0)?.select()
    }

    private fun fetchItemsFromDatabase(tab: String) {
        val query = when (tab) {
            "Liked" -> databaseReference.orderByChild("likedBy/${FirebaseAuth.getInstance().currentUser?.uid}").equalTo(true)
            else -> databaseReference.orderByChild("active").equalTo(true)
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                snapshot.children.forEach { childSnapshot ->
                    childSnapshot.getValue(Item::class.java)?.let {
                        if (it.active == true) itemList.add(it)
                    }
                }
                adapter.updateItemList(itemList)
                filterItems(searchBar.text.toString())  // Refilter items every time the data changes
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterItems(query: String) {
        val filteredList = if (query.isNotEmpty()) {
            itemList.filter {
                it.title?.contains(query, ignoreCase = true) == true
            }.toMutableList()
        } else {
            itemList
        }

        adapter.updateItemList(filteredList)
        toggleNoResultsView(filteredList.isEmpty())
    }

    private fun filterItemsByCategory(category: String) {
        val filteredList = if (category != getString(R.string.category_prompt)) {
            itemList.filter { it.category == category }.toMutableList()
        } else {
            itemList
        }
        adapter.updateItemList(filteredList)
        adapter.notifyDataSetChanged() // Notify adapter of data change
        toggleNoResultsView(filteredList.isEmpty())
    }

    private fun toggleNoResultsView(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            imgNoResults.visibility = View.VISIBLE
            txtNoResults.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            imgNoResults.visibility = View.GONE
            txtNoResults.visibility = View.GONE
        }
    }
}