package com.aarav.geowav.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.presentation.components.SearchItem
import com.aarav.geowav.presentation.theme.manrope
import com.google.android.libraries.places.api.model.AutocompletePrediction


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewSearch(
    isDarkThemeEnabled: Boolean,
    predictions: List<AutocompletePrediction>,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onExpandedChange: (Boolean) -> Unit,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    textFieldState: TextFieldState,
    onPlaceSelected: (String) -> Unit
) {

    Box(
        modifier = modifier
            .background(if(expanded) MaterialTheme.colorScheme.surface else Color.Transparent)
            .fillMaxWidth()
            .padding(top = if(expanded) 12.dp else 62.dp)
            .padding(horizontal = if (!expanded) 12.dp else 0.dp)
    ) {
        SearchBar(
            shape = if (expanded) RoundedCornerShape(0.dp) else RoundedCornerShape(99.dp),
            shadowElevation = if (expanded) 0.dp else 6.dp,
            colors = SearchBarDefaults.colors(
                containerColor = if (expanded)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                dividerColor = MaterialTheme.colorScheme.outlineVariant
            ),
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier
                .fillMaxWidth(),
            inputField = {
                SearchBarDefaults.InputField(
                    state = textFieldState,
                    onSearch = {},
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = manrope),
                    leadingIcon = {
                        if (expanded) {
                            IconButton(onClick = {
                                onExpandedChange(false)
                                textFieldState.clearText()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.back),
                                    contentDescription = "back",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {},
                                enabled = false
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.map_pin),
                                    contentDescription = "location",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        if (expanded) {
                            if (textFieldState.text.isNotEmpty()) {
                                IconButton(onClick = { textFieldState.clearText() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = "clear",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            IconButton(
                                onClick = {},
                                enabled = false
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = "search",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },

                    placeholder = {
                        Text(
                            text = "Search places, addresses...",
                            fontFamily = manrope,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 15.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) {
            if(isLoading && expanded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            }
            else if(textFieldState.text.isEmpty() && expanded){
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Search for a place or address",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
            }
            else if (predictions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.gps),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Places Found",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(top = 8.dp)
                ) {
                    items(predictions) { prediction ->
                        SearchItem(prediction) {
                            onPlaceSelected(prediction.placeId)
                        }
                    }
                }
            }
        }
    }
}
