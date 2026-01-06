package com.aarav.geowav.presentation.place

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aarav.geowav.presentation.components.SearchItem
import com.aarav.geowav.ui.theme.sora
import com.aarav.geowav.ui.theme.surfaceContainerLowDarkHighContrast
import com.aarav.geowav.ui.theme.surfaceContainerLowestLightHighContrast
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSearch(
    context: Context,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onExpandedChange: (Boolean) -> Unit,
    textFieldState: TextFieldState,
    placeViewModel: PlaceViewModel,
    onPlaceSelected: (Place) -> Unit
) {

    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    // Fetch predictions based on query
    LaunchedEffect(textFieldState.text) {
        if (textFieldState.text.length > 2) {
            placeViewModel.searchPlaces(context, textFieldState.text.toString()) { list ->
                predictions = list
            }
        } else predictions = emptyList()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (expanded) 56.dp else 66.dp)
            .padding(horizontal = if (!expanded) 12.dp else 0.dp)
    ) {
        SearchBar(
            shadowElevation = 16.dp,
            shape = RoundedCornerShape(16.dp),
            colors = SearchBarDefaults.colors(
                containerColor = if (!isSystemInDarkTheme()) surfaceContainerLowestLightHighContrast else surfaceContainerLowDarkHighContrast,
                dividerColor = MaterialTheme.colorScheme.primary
            ),
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            inputField = {
                SearchBarDefaults.InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    query = textFieldState.text.toString(),
                    onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                    onSearch = {},
                    leadingIcon = {
                        if (expanded) {
                            IconButton(onClick = { onExpandedChange(false) }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "back",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        } else {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "location",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        if (expanded) {
                            IconButton(onClick = { textFieldState.clearText() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "clear",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        } else {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "search",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    },
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    placeholder = { Text("Search here", fontFamily = sora) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(predictions) { prediction ->
                    SearchItem(prediction) {
                        placeViewModel.fetchPlace(prediction.placeId, context) { place ->
                            onPlaceSelected(place)
                            onExpandedChange(false)
                        }
                    }
                }
            }
        }
    }
}
