package com.aarav.geowav.presentation.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.presentation.components.GeofencePlaceCard
import com.aarav.geowav.ui.theme.manrope
import com.aarav.geowav.ui.theme.sora

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun YourPlacesScreen(
    placeViewModel: PlaceViewModel,
    navigateToMap: () -> Unit
) {

    val placesList by placeViewModel.allPlaces.collectAsState()

    val new = listOf<Place>(
//        Place(
//            latitude = 40.7128,
//            longitude = -74.0060,
//            radius = 200F,
//            placeId = "new_york",
//            customName = "new_york",
//            placeName = "new_york",
//            address = "TODO()",
//            addedOn = "06/11/25",
//        )
    )
    //val empty = emptyList<Nothing>()
//    Scaffold(
//        modifier = Modifier.fillMaxSize(),
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Your Places",
//                        fontSize = 24.sp,
//                        fontFamily = manrope,
//                        fontWeight = FontWeight.SemiBold,
//                        color = MaterialTheme.colorScheme.onBackground
//                    )
//                },
//                navigationIcon = {
////                    IconButton(
////                        onClick = {
////                            navigateToMap()
////                        }
////                    ) {
////                        Icon(
////                            imageVector = Icons.Default.ArrowBack,
////                            contentDescription = null
////                        )
////                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            AddLocationFAB()
//        }
//    ) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            //.padding(it)
            .fillMaxSize()
    ) {


        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = "Your Places",
                fontSize = 24.sp,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 54.dp)
            )

            if (placesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(132.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.tray),
                                contentDescription = "tray",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Text(
                            text = "Add a location to start tracking your zones",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = sora,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                items(placesList) { place ->
                    GeofencePlaceCard(place, placeViewModel)
                }
            }
        }

        AddLocationFAB(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(vertical = 16.dp, horizontal = 12.dp),
            navigateToMap
        )
    }
    //}
}

@Preview(showBackground = true)
@Composable
fun AddLocationFAB(modifier: Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
        onClick = {
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "add location",
        )
    }
}