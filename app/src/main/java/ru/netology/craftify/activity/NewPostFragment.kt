package ru.netology.craftify.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.R
import ru.netology.craftify.activity.NewMapsFragment.Companion.latArg
import ru.netology.craftify.activity.NewMapsFragment.Companion.longArg
import ru.netology.craftify.databinding.FragmentNewPostBinding
import ru.netology.craftify.util.AndroidUtils
import ru.netology.craftify.view.load
import ru.netology.craftify.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    private var fragmentBinding: FragmentNewPostBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                fragmentBinding?.let {
                    if (it.editContent.text.toString().isNotEmpty()) {
                        viewModel.changeContentPosts(it.editContent.text.toString())
                        viewModel.changeLinkPosts(it.editLink.text.toString())
                        viewModel.changeMentionList(it.editMentions.text.toString())
                        viewModel.changeCoordsPosts(
                            it.textCoordinateLat.text.toString(),
                            it.textCoordinateLong.text.toString()
                        )
                        viewModel.savePosts()
                        AndroidUtils.hideKeyboard(requireView())
                    }
                    else
                    {
                        Toast.makeText(
                            context,
                            getString(R.string.new_post_empty_content),
                            Toast.LENGTH_LONG
                        )
                            .show()
                        it.editContent.requestFocus()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        val editPost = viewModel.getEdit()
        binding.editContent.setText(editPost?.content)
        binding.editLink.setText(editPost?.link)
        binding.editMentions.setText(editPost?.mentionIds?.joinToString(", ",
            "",
            "",
            -1,
            "...",
            null))

        val lat = editPost?.coords?.lat
        val long = editPost?.coords?.long
        if (lat!=null && long!=null)
            viewModel.changeCoordinatesFromMap(lat, long)
        val attachment = editPost?.attachment
        if (attachment != null) viewModel.changePhoto(Uri.parse(attachment.url), null)
        if (attachment?.url != null) {
            binding.AttachmentImage.load(attachment.url)
            binding.AttachmentContainer.visibility = View.VISIBLE
        }

        binding.editContent.requestFocus()

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changePhoto(null, null)
        }

        binding.buttonLocationOn.setOnClickListener {
            findNavController().navigate(R.id.action_newPostFragment_to_newMapsFragment,
                Bundle().apply {
                    latArg = viewModel.coordinates.value?.lat?.toDouble() ?: coordinatesSaratov.latitude
                    longArg = viewModel.coordinates.value?.long?.toDouble() ?: coordinatesSaratov.longitude
                })
        }

        binding.buttonLocationOff.setOnClickListener {
            viewModel.changeCoordinatesFromMap("", "")
            viewModel.changeCoordsPosts("", "")
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.coordinates.observe(viewLifecycleOwner) {
            binding.textCoordinateLat.text = viewModel.coordinates.value?.lat
            binding.textCoordinateLong.text = viewModel.coordinates.value?.long
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it?.uri == null) {
                binding.AttachmentContainer.visibility = View.GONE
                return@observe
            }
            binding.AttachmentContainer.visibility = View.VISIBLE
            binding.AttachmentImage.setImageURI(it.uri)
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}