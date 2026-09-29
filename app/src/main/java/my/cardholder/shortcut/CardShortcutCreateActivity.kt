package my.cardholder.shortcut

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.ActivityCardShortcutCreateBinding
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

/** Shown by the launcher when the user adds the app's shortcut from the widgets list. */
@AndroidEntryPoint
class CardShortcutCreateActivity : AppCompatActivity() {

    private val viewModel: CardShortcutCreateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val adapter = CardShortcutSelectAdapter { cardId ->
            viewModel.onCardSelected(cardId)
        }

        val binding = ActivityCardShortcutCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets(bottom = false)
            cardShortcutCreateToolbar.setNavigationOnClickListener { finish() }

            cardShortcutCreateRecyclerView.apply {
                updateVerticalPaddingAfterApplyingWindowInsets(top = false)
                layoutManager = LinearLayoutManager(this@CardShortcutCreateActivity)
                this.adapter = adapter
            }
        }

        collectWhenStarted(viewModel.cards) { cards ->
            adapter.submitList(cards)
        }
        collectWhenStarted(viewModel.shortcutCreatedEvent) { resultIntent ->
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
