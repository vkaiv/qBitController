package dev.bartuzen.qbitcontroller.ui.torrent.tabs.overview.category

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.bartuzen.qbitcontroller.R
import dev.bartuzen.qbitcontroller.databinding.DialogTorrentCategoryBinding
import dev.bartuzen.qbitcontroller.ui.torrent.tabs.overview.TorrentOverviewFragment
import dev.bartuzen.qbitcontroller.utils.launchAndCollectIn
import dev.bartuzen.qbitcontroller.utils.launchAndCollectLatestIn
import dev.bartuzen.qbitcontroller.utils.setNegativeButton
import dev.bartuzen.qbitcontroller.utils.setPositiveButton
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class TorrentCategoryDialog() : DialogFragment() {
    private val viewModel: TorrentCategoryViewModel by viewModels()

    private val serverId get() = arguments?.getInt("serverId", -1).takeIf { it != -1 }!!
    private val currentCategory get() = arguments?.getString("currentCategory")

    private val parentFragment get() = (requireParentFragment() as TorrentOverviewFragment)

    constructor(serverId: Int, currentCategory: String?) : this() {
        arguments = bundleOf(
            "serverId" to serverId,
            "currentCategory" to currentCategory
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        val binding = DialogTorrentCategoryBinding.inflate(layoutInflater)
        setView(binding.root)
        setTitle(R.string.torrent_category_dialog_title)
        setPositiveButton { _, _ ->
            val selectedCategory = binding.chipGroupCategory.checkedChipId.let { id ->
                if (id != View.NO_ID) {
                    binding.chipGroupCategory.findViewById<Chip>(id).text.toString()
                } else {
                    null
                }
            }
            parentFragment.onCategoryDialogResult(selectedCategory)
        }
        setNegativeButton()

        if (!viewModel.isInitialLoadStarted) {
            viewModel.isInitialLoadStarted = true
            viewModel.updateCategories(serverId)
        }

        viewModel.categories.filterNotNull().launchAndCollectLatestIn(this@TorrentCategoryDialog) { categories ->
            if (binding.chipGroupCategory.isEmpty()) {
                categories.forEach { category ->
                    val chip = Chip(requireContext())
                    chip.text = category
                    chip.setEnsureMinTouchTargetSize(false)
                    chip.setChipBackgroundColorResource(R.color.torrent_category)
                    chip.ellipsize = TextUtils.TruncateAt.END
                    chip.isCheckable = true

                    if (category == currentCategory) {
                        chip.isChecked = true
                    }

                    binding.chipGroupCategory.addView(chip)
                }
            }

            binding.textNotFound.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
            binding.progressIndicator.visibility = View.GONE
            cancel()
        }

        viewModel.eventFlow.launchAndCollectIn(this@TorrentCategoryDialog) { event ->
            when (event) {
                is TorrentCategoryViewModel.Event.Error -> {
                    parentFragment.onCategoryDialogError(event.result)
                    dismiss()
                }
            }
        }
    }.create()
}
