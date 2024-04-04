package com.example.datto

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.AccountResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileEdit.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileEdit : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val requestCode = 70

    private lateinit var avatar: ImageView
    private lateinit var username: com.google.android.material.textfield.TextInputEditText
    private lateinit var fullName: com.google.android.material.textfield.TextInputEditText
    private lateinit var dob: com.google.android.material.textfield.TextInputEditText

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener{
            Thread {
                try {
                    // Get avatar as multipart file
                    val avatarBitmap = (avatar.drawable as BitmapDrawable).bitmap
                    val byteArrayOutStream = ByteArrayOutputStream()
                    avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutStream)
                    val requestBody = RequestBody.create(MediaType.parse("image/*"), byteArrayOutStream.toByteArray())
                    val multipartBody = MultipartBody.Part.createFormData("file", "avatar.jpg", requestBody)

                    APIService().doPutMultipart<Any>("files", multipartBody, object :
                        APICallback<Any> {
                        override fun onSuccess(data: Any) {
                            Log.d("API_SERVICE", "Data: $data")
                        }

                        override fun onError(error: Throwable) {
                            Log.e("API_SERVICE", "Error: ${error.message}")
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()

            true
        }

        appBar.title = "Edit Profile"
    }

    private fun destroyTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.setIcon(null)

        appBar.title = "Title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        configTopAppBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assign id to each element
        avatar = requireActivity().findViewById(R.id.profile_edit_avatar)
        username = requireActivity().findViewById(R.id.profile_edit_username)
        fullName = requireActivity().findViewById(R.id.profile_edit_fullName)
        dob = requireActivity().findViewById(R.id.profile_edit_dob)

        APIService().doGet<AccountResponse>("accounts/660ca8b9cba91f0ee182605e", object :
            APICallback<Any> {
            override fun onSuccess(data: Any) {
                Log.d("API_SERVICE", "Data: $data")

                // Cast data to Account
                data as AccountResponse

                // Format date of birth
                val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                val targetFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val date = originalFormat.parse(data.profile.dob)

                // Set text to each element\
                username.setText(data.username)
                fullName.setText(data.profile.fullName)
                dob.setText(targetFormat.format(date!!))

                // Load image with Picasso and new thread
                Thread {
                    try {
                        activity?.runOnUiThread {
                            Picasso.get().load(GlobalVariable.BASE_URL + "files/" + data.profile.avatar).into(avatar)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }

            override fun onError(error: Throwable) {
                Log.e("API_SERVICE", "Error: ${error.message}")
            }
        })

        // Change avatar option
        avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, this.requestCode);
        }

        // Set date picker on click
        dob.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener {
                val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                dob.setText(date.format(formatter))
            }
            datePicker.show(parentFragmentManager, "date_picker")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == this.requestCode && resultCode == RESULT_OK && data != null) {
            Picasso.get().load(data.data).into(avatar)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyTopAppBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_edit, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileEdit.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileEdit().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}