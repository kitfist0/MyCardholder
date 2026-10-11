package my.cardholder.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.ActivityCardWidgetConfigureBinding
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

/**
 * Shown by the launcher when the user adds the cards widget or reconfigures it, and when the user
 * taps a widget without cards.
 */
@AndroidEntryPoint
class CardWidgetConfigureActivity : AppCompatActivity() {

    private val viewModel: CardWidgetConfigureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resultIntent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, viewModel.widgetId)
        // Leaving without saving makes the launcher drop a widget that is just being added.
        setResult(RESULT_CANCELED, resultIntent)
        if (viewModel.widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val adapter = CardWidgetSelectAdapter { cardId ->
            viewModel.onCardClicked(cardId)
        }

        val binding = ActivityCardWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            cardWidgetConfigureToolbar.setNavigationOnClickListener { finish() }
            cardWidgetConfigureRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@CardWidgetConfigureActivity)
                this.adapter = adapter
            }
            cardWidgetConfigureSaveFab.setOnClickListener { viewModel.onSaveClicked() }
        }

        collectWhenStarted(viewModel.state) { state ->
            adapter.submitList(state.items)
            binding.cardWidgetConfigureNoCardsText.isVisible = !state.isLoading && state.items.isEmpty()
            binding.cardWidgetConfigureSaveFab.isEnabled = state.isSaveEnabled
        }
        collectWhenStarted(viewModel.savedEvent) {
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
