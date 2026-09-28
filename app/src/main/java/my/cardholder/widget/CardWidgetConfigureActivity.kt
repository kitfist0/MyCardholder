package my.cardholder.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.ActivityCardWidgetConfigureBinding
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CardWidgetConfigureActivity : AppCompatActivity() {

    private val viewModel: CardWidgetConfigureViewModel by viewModels()

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent.extras
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val adapter = CardWidgetSelectAdapter { cardId ->
            viewModel.onCardSelected(appWidgetId, cardId)
        }

        val binding = ActivityCardWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardWidgetConfigureToolbar.setNavigationOnClickListener { finish() }

        binding.cardWidgetConfigureRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CardWidgetConfigureActivity)
            this.adapter = adapter
        }

        collectWhenStarted(viewModel.cards) { cards ->
            adapter.submitList(cards)
        }
        collectWhenStarted(viewModel.widgetConfiguredEvent) {
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
