package com.aarav.geowav.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.aarav.geowav.ui.theme.nunito
import com.google.android.libraries.places.api.model.Place
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PlaceModalSheet(
    place : Place?,
    sheetState: SheetState,
    showSheet : Boolean,
    onDismissRequest: () -> Unit,
    clearSearch: () -> Unit,
    onAddPlaceBtnClick: (String) -> Unit
) {
//    val sheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = false
//    )
//
//    var showSheet by remember {
//        mutableStateOf(true)
//    }

        AnimatedVisibility(showSheet) {
            ModalBottomSheet(
                //modifier = Modifier.fillMaxHeight(),
                onDismissRequest = onDismissRequest,
                sheetState = sheetState,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                SheetContent(place,
                    clearSearch,
                    onAddPlaceBtnClick)
            }
        }
    }

@Preview(showBackground = true)
@Composable
fun SheetContent(place: Place?,
                 clearSearch: () -> Unit,
                 onAddPlaceBtnClick: (String) -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Surface(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            ) {
                Icon(
                    painter = painterResource(R.drawable.navigation_arrow),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = place?.displayName ?: "Invalid Place",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = nunito,
                fontSize = 22.sp
            )
        }

        Text(
            text = place?.shortFormattedAddress ?: "Address not available",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = nunito,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Lat: ${place?.location?.latitude?.toString()?.take(7)}, Lng: ${place?.location?.longitude?.toString()?.take(7)}",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilledTonalButton(
            onClick = {
                onAddPlaceBtnClick(place?.id ?: "0")
                clearSearch()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            //Text("Add Place", fontFamily = nunito, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Place", fontFamily = nunito, fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp)

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.caret_circle_right),
                    contentDescription = "caret circle right",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

