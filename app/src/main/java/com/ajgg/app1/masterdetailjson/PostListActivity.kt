package com.ajgg.app1.masterdetailjson

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.ajgg.app1.masterdetailjson.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_post_list.*
import kotlinx.android.synthetic.main.post_list.*
import kotlinx.android.synthetic.main.post_list_content.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import java.net.URL

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [PostDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class PostListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        if (post_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }
        var respuesta = ""
        waiting.visibility=VISIBLE

        doAsync {
            try {

                respuesta = URL("http://50.112.179.61/wp5/?rest_route=/wp/v2/posts/").readText()
                val array: JSONArray = JSONArray(respuesta)

                DummyContent.ITEMS = ArrayList()
                DummyContent.ITEM_MAP= HashMap()



                for (i in 0 until array.length()) {

                    DummyContent.addItem(DummyContent.DummyItem(array.getJSONObject(i).get("id").toString(),
                            array.getJSONObject(i).getJSONObject("title").getString("rendered"),
                            array.getJSONObject(i).getJSONObject("content").getString("rendered")))

                }
                uiThread {
                    waiting.visibility=INVISIBLE
                    setupRecyclerView(post_list)
                }
            } catch (e: Exception) {
                Log.d("TITULO", e.toString())
            }
        }

    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(private val parentActivity: PostListActivity,
                                        private val values: List<DummyContent.DummyItem>,
                                        private val twoPane: Boolean) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as DummyContent.DummyItem
                if (twoPane) {
                    val fragment = PostDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(PostDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.post_detail_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, PostDetailActivity::class.java).apply {
                        putExtra(PostDetailFragment.ARG_ITEM_ID, item.id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.id
            holder.contentView.text = item.content

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }
}
