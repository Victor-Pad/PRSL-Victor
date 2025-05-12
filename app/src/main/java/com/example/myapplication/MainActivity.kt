package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

import androidx.compose.ui.unit.Dp
import com.example.myapplication.ui.theme.MyApplicationTheme

import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.skydoves.landscapist.glide.GlideImage
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                GifSearchScreen(context = this)
            }
        }
    }
}

@Suppress("DEPRECATION")
@Deprecated(
    message = "Use overload which takes inputField as a parameter",
    replaceWith =
    ReplaceWith(
        "SearchBar(\n" +
                "    inputField = {\n" +
                "        SearchBarDefaults.InputField(\n" +
                "            query = query,\n" +
                "            onQueryChange = onQueryChange,\n" +
                "            onSearch = onSearch,\n" +
                "            expanded = active,\n" +
                "            onExpandedChange = onActiveChange,\n" +
                "            enabled = enabled,\n" +
                "            placeholder = placeholder,\n" +
                "            leadingIcon = leadingIcon,\n" +
                "            trailingIcon = trailingIcon,\n" +
                "            colors = colors.inputFieldColors,\n" +
                "            interactionSource = interactionSource,\n" +
                "        )\n" +
                "    },\n" +
                "    expanded = active,\n" +
                "    onExpandedChange = onActiveChange,\n" +
                "    modifier = modifier,\n" +
                "    shape = shape,\n" +
                "    colors = colors,\n" +
                "    tonalElevation = tonalElevation,\n" +
                "    shadowElevation = shadowElevation,\n" +
                "    windowInsets = windowInsets,\n" +
                "    content = content,\n" +
                ")"
    ),
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    shadowElevation: Dp = SearchBarDefaults.ShadowElevation,
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = active,
        onActiveChange = onActiveChange,
        modifier = modifier,
        enabled = enabled,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        windowInsets = windowInsets,
        interactionSource = interactionSource,
    ) {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            expanded = active,
            onExpandedChange = onActiveChange,
            enabled = enabled,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            colors = colors.inputFieldColors,
            interactionSource = interactionSource,
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GifSearchScreen(context: Context) {
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    var gifResId by remember { mutableStateOf<Int?>(null) }
    var displayedGifName by rememberSaveable { mutableStateOf("") }

    val resources = context.resources
    val packageName = context.packageName

    // Speech recognizer launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            query = matches[0] // Update the query with voice input
            active = false
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Search Bar for searching GIFs
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    active = false // Hide the keyboard once the search is done

                    // Normalize and clean the query to match drawable file names
                    val normalizedTerm = query
                        .lowercase()
                        .replace(" ", "_")  // Replace spaces with underscores
                        .normalize()  // Remove diacritics
                        .replace(Regex("[^a-z0-9_]"), "")  // Remove special characters

                    // Search for the drawable resource ID
                    val resId = resources.getIdentifier(normalizedTerm, "drawable", packageName)

                    // Set the resource ID to show the GIF or null if not found
                    gifResId = if (resId != 0) {
                        // Save the display name when a valid GIF is found
                        displayedGifName = if (query.isNotEmpty()) {
                            query.first().uppercaseChar() + query.substring(1).lowercase()
                        } else {
                            query
                        }
                        resId
                    } else null
                },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search your GIFs") },
                leadingIcon = {
                    if (active) {
                        IconButton(onClick = { active = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear text",
                            modifier = Modifier.clickable { query = "" }
                        )
                    } else {
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                            }
                            speechRecognizerLauncher.launch(intent)
                        }) {
                            Icon(Icons.Filled.Mic, contentDescription = "Mic")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {}

            // If a valid resource ID is found, display the GIF using XML CardView
            gifResId?.let { resId ->
                // This is where we embed the XML layout in Compose
                AndroidView(
                    factory = { ctx ->
                        // Inflate the XML CardView layout
                        val view = android.view.LayoutInflater.from(ctx)
                            .inflate(R.layout.item_gif, null)

                        // Set the layout parameters
                        view.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        view
                    },
                    update = { view ->
                        // Get references to views within the inflated layout
                        val imageView = view.findViewById<ImageView>(R.id.imageViewGif)
                        val textView = view.findViewById<android.widget.TextView>(R.id.textViewGifName)

                        // Update the ImageView with the GIF using Glide
                        Glide.with(view)
                            .load(resId)
                            .into(imageView)

                        // Use the persistent displayedGifName instead of directly using query
                        textView.text = displayedGifName
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } ?: run {
                // If no GIF found, display a placeholder message
                Text(
                    text = "No GIF found for \"$query\"",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


// Normalize extension function
fun String.normalize(): String {
    return java.text.Normalizer
        .normalize(this, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}
