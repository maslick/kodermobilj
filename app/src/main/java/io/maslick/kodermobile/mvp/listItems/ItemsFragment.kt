package io.maslick.kodermobile.mvp.listItems

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.kennyc.bottomsheet.BottomSheet
import com.kennyc.bottomsheet.BottomSheetListener
import io.maslick.kodermobile.R
import io.maslick.kodermobile.di.Properties.EDIT_ITEM_ID
import io.maslick.kodermobile.helper.Helper.showSnackBar
import io.maslick.kodermobile.model.Item
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemActivity
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemActivity.Companion.ADD_ITEM_REQUEST_CODE
import io.maslick.kodermobile.mvp.listItems.ItemsActivity.Companion.AUTHORIZATION_REQUEST_CODE
import io.maslick.kodermobile.mvp.login.LoginActivity
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.setProperty

class ItemsFragment : Fragment(), ItemsContract.View {

    override val presenter by inject<ItemsContract.Presenter>()
    override var isActive: Boolean = false
        get() = isAdded

    private val lineAdapter = LineAdapter(object : ItemListener {
        override fun onEditItem(item: Item) { presenter.openItemDetail(item)}
        override fun onDeleteItem(item: Item) { presenter.removeItem(item)}
    })

    override fun onResume() {
        super.onResume()
        presenter.view = this
        presenter.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.result(requestCode, resultCode)
    }

    override fun startAuthActivity() {
        startActivityForResult(Intent(activity, LoginActivity::class.java), AUTHORIZATION_REQUEST_CODE)
    }

    override fun showAuthOk(user: String) {
        view?.showSnackBar("Signed in as $user", Snackbar.LENGTH_LONG)
    }

    override fun showAuthError() {
        view?.showSnackBar("Could not sign in :(", Snackbar.LENGTH_LONG)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.items_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.addNewItem -> presenter.addNewItem()
            R.id.logout -> presenter.logout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.items_frag, container, false)

        with(root) {
            val fabBtn = activity!!.findViewById<FloatingActionButton>(R.id.fab_add_item)

            findViewById<RecyclerView>(R.id.recyclerFragment).apply {
                layoutManager = LinearLayoutManager(activity)
                addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
                adapter = lineAdapter
                addOnScrollListener(object: RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                        if (dy > 0) fabBtn.hide() else fabBtn.show()
                    }
                })
            }

            findViewById<SwipeRefreshLayout>(R.id.swiper).apply {
                setOnRefreshListener { presenter.loadItems() }
            }

            fabBtn.apply {
                setImageResource(R.drawable.ic_add)
                setOnClickListener { presenter.addNewItem() }
            }
        }

        setHasOptionsMenu(true)
        return root
    }

    override fun showItems(items: List<Item>) {
        lineAdapter.items = items.sortedByDescending { it.id }
    }

    override fun showLoadingItemsError(message: String) {
        view?.showSnackBar("Error while loading items$message", Snackbar.LENGTH_LONG)
    }

    override fun showNoItems() {
        view?.showSnackBar("No items...", Snackbar.LENGTH_LONG)
    }

    class LineAdapter(val callbacks: ItemListener) : RecyclerView.Adapter<LineHolder>() {
        var items = listOf<Item>()
            set(items) {
                field = items
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
            return LineHolder(view)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: LineHolder, position: Int) {
            holder.bind(items[position], callbacks)
        }
    }

    class LineHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textTitle)
        val description: TextView = itemView.findViewById(R.id.textDescription)
        val code: TextView = itemView.findViewById(R.id.textCode)
        val quantity: TextView = itemView.findViewById(R.id.textQuantity)

        @SuppressLint("SetTextI18n")
        fun bind(item: Item, callbacks: ItemListener) {
            quantity.text = item.quantity.toString()
            title.text = item.title
            description.text = item.description
            code.text = item.barcode
            itemView.setOnClickListener { callbacks.onEditItem(item) }
            itemView.setOnLongClickListener {
                BottomSheet.Builder(itemView.context)
                    .setSheet(R.menu.dropup_menu)
                    .setListener(object : BottomSheetListener {
                        override fun onSheetItemSelected(sheet: BottomSheet, menuItem: MenuItem, obj: Any?) {
                            when (menuItem.title) {
                                "Edit" -> callbacks.onEditItem(item)
                                "Remove" -> callbacks.onDeleteItem(item)
                            }
                        }
                        override fun onSheetDismissed(p0: BottomSheet, p1: Any?, p2: Int) {}
                        override fun onSheetShown(p0: BottomSheet, p1: Any?) {}
                    })
                    .show()
                true
            }
        }
    }

    override fun showAddItem() {
        val intent = Intent(context, AddEditItemActivity::class.java)
        setProperty(EDIT_ITEM_ID, Item())
        startActivityForResult(intent, ADD_ITEM_REQUEST_CODE)
    }

    override fun showItemDetailUi(item: Item) {
        val intent = Intent(context, AddEditItemActivity::class.java)
        setProperty(EDIT_ITEM_ID, item)
        startActivityForResult(intent, ADD_ITEM_REQUEST_CODE)
    }

    override fun setLoadingIndicator(active: Boolean) {
        view?.findViewById<SwipeRefreshLayout>(R.id.swiper)?.apply {
            this.isRefreshing = active
        }
    }

    override fun showSuccessfullySavedItem() {
        view?.showSnackBar("Item saved", Snackbar.LENGTH_LONG)
        presenter.loadItems()
    }

    override fun showDeleteOk(message: String) {
        view?.showSnackBar(message, Snackbar.LENGTH_LONG)
    }

    override fun showErrorDeletingItem() {
        view?.showSnackBar("Could not delete item", Snackbar.LENGTH_LONG)
    }

    override fun logoutOk() {
        Toast.makeText(context, "Successfully logged out", Toast.LENGTH_LONG).show()
        startActivityForResult(Intent(activity, LoginActivity::class.java), AUTHORIZATION_REQUEST_CODE)
    }

    override fun logoutError(message: String?) {
        view?.showSnackBar("Error while logging out: are you offline?", Snackbar.LENGTH_LONG)
    }

    interface ItemListener {
        fun onEditItem(item: Item)
        fun onDeleteItem(item: Item)
    }
}