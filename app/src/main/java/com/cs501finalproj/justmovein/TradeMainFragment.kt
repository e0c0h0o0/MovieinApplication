package com.cs501finalproj.justmovein

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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
    private var itemList: List<Item> = listOf()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: ItemAdapter
    private lateinit var imgNoResults: ImageView
    private lateinit var txtNoResults: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initially fetch items for the default tab
        fetchItemsFromDatabase("Browse")
        UiUtil.setApplicationLocale(requireContext(), UiUtil.getLocaleCode(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trade_main, container, false)

        databaseReference = FirebaseDatabase.getInstance().getReference("Items")

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = ItemAdapter(mutableListOf(), "grid")
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter

        fabAddItem = view.findViewById(R.id.fab_add_item)
        fabAddItem.setOnClickListener {
            val intent = Intent(activity, ListItemActivity::class.java)
            startActivity(intent)
        }

        searchBar = view.findViewById(R.id.search_bar)

        tabLayout = view.findViewById(R.id.tabs)
        tabLayout.apply {
            addTab(this.newTab().setText("Browse"))
            addTab(this.newTab().setText("Liked"))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> fetchItemsFromDatabase("Browse")
                        1 -> fetchItemsFromDatabase("Liked")
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // Optionally handle tab unselected
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // Optionally handle tab reselection
                }
            })
        }

        // Manually trigger the first tab selection
        tabLayout.getTabAt(0)?.select()

        imgNoResults = view.findViewById(R.id.imgNoResults)
        txtNoResults = view.findViewById(R.id.txtNoResults)

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterItems(s.toString())
            }
        })


        return view
    }

    private fun fetchItemsFromDatabase(tab: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val query = databaseReference.orderByChild("active").equalTo(true)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedItems = mutableListOf<Item>()
                snapshot.children.forEach { childSnapshot ->
                    val item = childSnapshot.getValue(Item::class.java)
                    if (item != null && item.active == true) {
                        if (tab == "Liked") {
                            // Log for debugging
                            Log.d("TradeMainFragment", "Item ${item.id} likedBy: ${item.likedBy}")
                            if (item.likedBy?.containsKey(userId) == true) {
                                fetchedItems.add(item)
                            }
                        } else if (tab == "Browse") {
                            fetchedItems.add(item)
                        }
                    }
                }
                Log.d("TradeMainFragment", "Fetched ${fetchedItems.size} items for tab $tab")
                adapter.updateItemList(fetchedItems)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    context,
                    getString(R.string.database_error) + ": " + databaseError.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun filterItems(query: String) {
        val filteredList = if (query.isNotEmpty()) {
            itemList.filter { it.title?.contains(query, ignoreCase = true) == true }.toMutableList()
        } else {
            itemList
        }

        adapter.updateItemList(filteredList)
        if (filteredList.isEmpty()) {
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