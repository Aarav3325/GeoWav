package com.aarav.geowav.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.presentation.theme.manrope
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlin.math.roundToInt

@Preview(showBackground = true)
@Composable
fun GeofencePlaceCard(
    place: Place,
    onDeleteClick: (Place) -> Unit,
    onEditRadiusClick: (Place) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.navigation_arrow),
                        contentDescription = "navigation arrow",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(7.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                    )
                }


                Text(
                    text = place.customName.ifBlank { place.placeName },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .weight(1f)
                )

                Spacer(modifier = Modifier.weight(0.1f))


                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            onDeleteClick(place)
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.trash),
                        contentDescription = "Delete Place",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(6.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

            }

            Spacer(Modifier.height(12.dp))


            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {

                Text(
                    text = place.address.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    AssistChip(
                        onClick = { onEditRadiusClick(place) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        label = {
                            Text(
                                text = "${place.radius.roundToInt()}m Awareness Zone",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = manrope,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun SearchItem(prediction: AutocompletePrediction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = CircleShape,
        ) {
            Image(
                painter = painterResource(id = R.drawable.map_trifold),
                contentDescription = "navigation arrow",
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = prediction.getPrimaryText(null).toString(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = manrope,
            )

            Text(
                text = prediction.getSecondaryText(null).toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = manrope,
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(0.3f)),
            )
        }
    }
}


@Composable
fun PlaceTextField(
    labelText: String,
    placeHolder: String,
    infoText: String,
    name: String,
    onValueChange: (String) -> Unit
) {

    Column {

        Text(
            text = infoText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = manrope,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            value = name,
            onValueChange = onValueChange,
            singleLine = true,
            label = {
                Text(
                    text = labelText,
                    fontFamily = manrope
                )
            },
            placeholder = {
                Text(
                    placeHolder,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
            )
        )
    }
}

@Composable
fun RadiusChipGroup(
    chips: List<Float>,
    selectedRadius: Float,
    onRadiusSelected: (Float) -> Unit
) {

    Column {
        Text(
            text = "Select Radius",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = manrope,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            chips.forEach { radius ->
                FilterChip(
                    selected = selectedRadius == radius,
                    onClick = {
                        onRadiusSelected(radius)
                    },
                    label = { Text("${radius.roundToInt()} m", fontFamily = manrope) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomChip(
    label: String
) {
    FilterChip(
        selected = true,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.check),
                modifier = Modifier.size(FilterChipDefaults.IconSize),
                contentDescription = "navigation arrow",
            )
        },
        onClick = {}, // disabled / not clickable
        label = { Text(label, fontFamily = manrope) },
        enabled = true, // disables interaction
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            containerColor = MaterialTheme.colorScheme.secondary,
            labelColor = MaterialTheme.colorScheme.primary
        )
    )

}