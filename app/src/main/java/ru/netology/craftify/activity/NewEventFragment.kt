package ru.netology.craftify.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
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
import ru.netology.craftify.databinding.FragmentNewEventBinding
import ru.netology.craftify.type.EventType
import ru.netology.craftify.view.load
import ru.netology.craftify.viewmodel.PostViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


@AndroidEntryPoint
class NewEventFragment : Fragment() {
    private var fragmentBinding: FragmentNewEventBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostViewModel by activityViewModels()

        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        val editEvent = viewModel.getEditEvent()
        binding.editContent.setText(editEvent?.content)
        binding.editLink.setText(editEvent?.link)
        val lat = editEvent?.coordinates?.lat
        val long = editEvent?.coordinates?.long
        if (lat != null && long != null)
            viewModel.changeCoordinatesFromMap(lat, long)
        val attachment = editEvent?.attachment
        if (attachment != null) viewModel.changePhoto(Uri.parse(attachment.url), null)
        if (attachment?.url != null) {
            binding.AttachmentImage.load(attachment.url)
            binding.AttachmentContainer.visibility = View.VISIBLE
        }

        val cal = Calendar.getInstance()

        binding.editTextDate.setText(
            SimpleDateFormat(date).format(
                if (editEvent?.datetime != "")
                    Date.from(
                        Instant.from(
                            DateTimeFormatter.ISO_INSTANT.parse(editEvent!!.datetime)
                        )
                    )
                else
                    cal.time
            )
        )

        binding.editTextTime.setText(
            SimpleDateFormat(time).format(
                if (editEvent.datetime != "")
                    Date.from(
                        Instant.from(
                            DateTimeFormatter.ISO_INSTANT.parse(editEvent.datetime)
                        )
                    )
                else
                    cal.time
            )
        )

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(date, Locale.getDefault())
                binding.editTextDate.setText(sdf.format(cal.time))
            }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            val sdf = SimpleDateFormat(time, Locale.getDefault())
            binding.editTextTime.setText(sdf.format(cal.time))
        }

        binding.buttonChangeDate.setOnClickListener {
            DatePickerDialog(
                binding.root.context, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.buttonChangeTime.setOnClickListener {
            TimePickerDialog(
                binding.root.context, timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(binding.root.context)
            ).show()
        }

        binding.radioOnline.isChecked = editEvent.type == EventType.ONLINE
        binding.radioOffline.isChecked = editEvent.type == EventType.OFFLINE

        binding.editSpeakers.setText(
            editEvent.speakerIds?.joinToString(
                ", ",
                "",
                "",
                -1,
                "...",
                null
            )
        )

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
            findNavController().navigate(R.id.action_newEventFragment_to_newMapsFragment,
                Bundle().apply {
                    latArg = viewModel.coordinates.value?.lat?.toDouble() ?: coordinatesSaratov.latitude
                    longArg =
                        viewModel.coordinates.value?.long?.toDouble() ?: coordinatesSaratov.longitude
                })
        }

        binding.buttonLocationOff.setOnClickListener {
            viewModel.changeCoordinatesFromMap("", "")
            viewModel.changeCoordinatesEvent("", "")
        }


        viewModel.eventCreated.observe(viewLifecycleOwner) {
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