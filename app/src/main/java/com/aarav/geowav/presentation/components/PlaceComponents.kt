package com.aarav.geowav.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.aarav.geowav.ui.theme.manrope
import com.aarav.geowav.ui.theme.sora
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlin.math.roundToInt

@Preview(showBackground = true)
@Composable
fun GeofencePlaceCard(
    place: Place,
    onDeleteClick: (Place) -> Unit
) {
    GeoWavTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = CircleShape,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.navigation_arrow),
                        contentDescription = "navigation arrow",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(6.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiaryContainer)
                    )
                }


                Text(
                    text = place.placeName,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
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
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.trash),
                        contentDescription = "navigation arrow",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(6.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onErrorContainer)
                    )
                }

            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.address.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = sora,
                )

                Text(
                    text = "${place.radius} m • Lat: ${
                        place.latitude.toString().take(7)
                    }, Lng: ${place.latitude.toString().take(7)}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = sora,
                )

                Text(
                    text = place.addedOn,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = sora,
                )
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
                fontFamily = sora,
            )

            Text(
                text = prediction.getSecondaryText(null).toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = sora,
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

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            value = name,
            onValueChange = onValueChange,
            maxLines = 1,
            label = {
                Text(
                    text = labelText,
                    fontFamily = sora
                )
            },
            placeholder = {
                Text(
                    placeHolder,
                    fontFamily = sora,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
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

    GeoWavTheme {
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
                        label = { Text("${radius.roundToInt()} m", fontFamily = sora) },
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
                imageVector = Icons.Default.Check,
                contentDescription = "navigation arrow",
            )
        },
        onClick = {}, // disabled / not clickable
        label = { Text(label, fontFamily = sora) },
        enabled = true, // disables interaction
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            containerColor = MaterialTheme.colorScheme.secondary,
            labelColor = MaterialTheme.colorScheme.primary
        )
    )

}