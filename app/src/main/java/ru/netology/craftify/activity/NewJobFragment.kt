package ru.netology.craftify.activity

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.databinding.FragmentNewJobBinding
import ru.netology.craftify.viewmodel.PostViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


@AndroidEntryPoint
class NewJobFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private var fragmentBinding: FragmentNewJobBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        val editJob = viewModel.getEditJob()
        val cal = Calendar.getInstance()

        with(binding) {
            editStartDate.setText(
                SimpleDateFormat(date).format(
                    if (editJob?.start != "")
                        Date.from(
                            Instant.from(
                                DateTimeFormatter.ISO_INSTANT.parse(editJob?.start)
                            )
                        )
                    else
                        cal.time
                )
            )
            editFinishDate.setText(
                if (editJob?.finish != null )
                    SimpleDateFormat(date).format(
                        Date.from(
                            Instant.from(
                                DateTimeFormatter.ISO_INSTANT.parse(editJob.finish)
                            )
                        )
                    )
                else
                    ""
            )
            editName.setText(editJob?.name)
            editPosition.setText(editJob?.position)
            editLink.setText(editJob?.link)
        }

        val dateStartSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(date, Locale.getDefault())
                binding.editStartDate.setText(sdf.format(cal.time))
            }

        binding.buttonChangeStartDate.setOnClickListener {
            DatePickerDialog(
                binding.root.context, dateStartSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dateFinishSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(date, Locale.getDefault())
                binding.editFinishDate.setText(sdf.format(cal.time))
            }

        binding.buttonChangeFinishDate.setOnClickListener {
            DatePickerDialog(
                binding.root.context, dateFinishSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        viewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}