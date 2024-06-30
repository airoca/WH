package edu.skku.cs.wash

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.skku.cs.wash.databinding.FragmentUsingBinding

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsingFragment : Fragment() {

    private var _binding: FragmentUsingBinding? = null
    private val binding get() = _binding!!

    private var usingWasherList = mutableListOf<Washer>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUsingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = root.findViewById<RecyclerView>(R.id.using_washers)
        val layoutManager = LinearLayoutManager(requireContext())

        val navigateActivity = activity as? NavigateActivity
        val userid = navigateActivity?.getUserId()

        val toolbarTitle = root.findViewById<TextView>(R.id.toolBarTitle)
        toolbarTitle.text = "사용중"
        val toolbarDorm = root.findViewById<TextView>(R.id.toolBarDorm)
        toolbarDorm.text = ""
        val backBtn = root.findViewById<ImageButton>(R.id.toolBarBtn)
        backBtn.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        navigateActivity?.supportActionBar?.hide()

        if (userid != null) {
            val call: Call<List<Washer>> = RetrofitClient.instance.getWashersByUser(userid)

            call.enqueue(object : Callback<List<Washer>> {
                override fun onResponse(call: Call<List<Washer>>, response: Response<List<Washer>>) {
                    if (response.isSuccessful) {
                        val fetchedList = response.body()
                        fetchedList?.let {
                            usingWasherList.clear()
                            usingWasherList.addAll(it)
                        }
                        Log.d("UsingList", usingWasherList.toString())
                        recyclerView.adapter?.notifyDataSetChanged()
                    } else {
                        Log.d("UsingList", "Response not successful")
                    }
                }
                override fun onFailure(call: Call<List<Washer>>, t: Throwable) {
                    Log.e("UsingList", "Failed: ${t.message}")
                }
            })
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = UsingWasherAdapter(usingWasherList)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Show the action bar when the fragment is destroyed
        (activity as? NavigateActivity)?.supportActionBar?.show()
    }
}
