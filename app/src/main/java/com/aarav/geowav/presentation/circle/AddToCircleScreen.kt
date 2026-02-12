package com.aarav.geowav.presentation.circle

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aarav.geowav.R
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.presentation.locationsharing.LovedOneUi
import com.aarav.geowav.presentation.locationsharing.itemShape
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleScreen(
    viewModel: CircleVM
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CircleUiEvent.InviteSent ->
                    snackbarHostState.showSnackbar("Invite sent")

                is CircleUiEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)

                is CircleUiEvent.InviteAccepted ->
                    snackbarHostState.showSnackbar("Invite Accepted")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadLovedOnes()
    }

    GeoWavTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Your Circle",
                            fontFamily = manrope,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                        )
                    }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            CircleContent(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                updateName = viewModel::updateName,
                updateEmail = viewModel::updateEmail,
                onSendInvite = viewModel::sendInvite,
                onAcceptInvite = viewModel::acceptInvite,
                onRejectInvite = viewModel::rejectInvite
            )
        }
    }
}

@Composable
fun CircleContent(
    modifier: Modifier = Modifier,
    uiState: CircleUiState,
    updateName: (String) -> Unit,
    updateEmail: (String) -> Unit,
    onSendInvite: (String, String) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onRejectInvite: (String) -> Unit
) {
    val lovedOnes = listOf(
        LovedOneUi("1", "Mom", true),
        LovedOneUi("2", "Dad", true),
        LovedOneUi("3", "Brother", true)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
//
//        item {
//            Text(
//                text = "Your Circle",
//                fontFamily = manrope,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(top = 24.dp, start = 16.dp)
//            )
//        }

        item {
            AddLovedOneCard(
                uiState,
                nameUpdate = updateName,
                emailUpdate = updateEmail,
                isLoading = uiState.isLoading,
                onSendInvite = onSendInvite
            )
        }

        item {
            MyCircleSection(uiState.lovedOnes)
        }

        item {
            PendingInviteSection(
                acceptingInviteId = uiState.acceptingInviteId,
                rejectingInviteId = uiState.rejectingInviteId,
                onAcceptInvite,
                onRejectInvite
            )
        }


        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}


@Composable
fun AddLovedOneCard(
    uiState: CircleUiState,
    nameUpdate: (String) -> Unit,
    emailUpdate: (String) -> Unit,
    isLoading: Boolean,
    onSendInvite: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                text = "Add Loved One",
                fontFamily = manrope,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Name",
                fontFamily = manrope,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = {
                    nameUpdate(it)
                },
                placeholder = {
                    Text(
                        "Enter a Loved One’s Name",
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.inverseSurface
                    )
                },
                isError = uiState.nameError != null,
                supportingText = {
                    if (uiState.nameError != null) {
                        Text(
                            text = uiState.nameError,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(), singleLine = true, leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.user),
                        contentDescription = "email icon",
                        modifier = Modifier.size(24.dp)
                    )
                }, colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(12.dp)
            )



            Spacer(Modifier.height(12.dp))

            Text(
                text = "Email",
                fontFamily = manrope,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = {
                    emailUpdate(it)
                },
                placeholder = {
                    Text(
                        "Enter a Loved One’s Email",
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.inverseSurface
                    )
                },
                isError = uiState.emailError != null,
                supportingText = {
                    if (uiState.emailError != null) {
                        Text(
                            text = uiState.emailError,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = sora,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(), singleLine = true, leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.email),
                        contentDescription = "email icon",
                        modifier = Modifier.size(24.dp)
                    )
                }, colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            SendInviteButton(!isLoading) {
                onSendInvite(uiState.email, uiState.name)
            }
        }
    }
}


@Composable
fun SendInviteButton(
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        enabled = isEnabled,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 0.dp)
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            "Send Invite",
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.width(12.dp))

        Icon(
            painter = painterResource(R.drawable.send_invite),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

    }
}


@Composable
fun MyCircleSection(
    lovedOnesList: List<CircleMember>
) {

//    val lovedOnes = listOf(
//        CircleMember(
//            "1",
//            "",
//            "Mom",
//            true
//        ),
//        CircleMember(
//            "2",
//            "",
//            "Dad",
//            true
//        ),
//        CircleMember(
//            "3",
//            "",
//            "Brother",
//            true
//        ),
//    )

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Circle",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            lovedOnesList.forEachIndexed { index, connection ->
                LovedOneCardCircle(
                    connection = connection,
                    index = index,
                    count = lovedOnesList.size
                )
            }
        }
    }
}

@Composable
fun PendingInviteSection(
    acceptingInviteId: String?,
    rejectingInviteId: String?,
    acceptInvite: (String) -> Unit,
    rejectInvite: (String) -> Unit
) {

    val lovedOnes = listOf(
        CircleMember(
            "1",
            "",
            "Akshat",
            true
        )
    )

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        expanded = !expanded
                    }
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending Invites",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                val icon = if (expanded) R.drawable.up_arrow else R.drawable.down_arrow

                Icon(
                    painter = painterResource(icon),
                    contentDescription = "icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (expanded) {

                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 4.dp
                    )
                ) {
                    lovedOnes.forEachIndexed { index, connection ->
                        PendingInviteRow(
                            acceptingInviteId,
                            rejectingInviteId,
                            connection,
                            index = index,
                            count = lovedOnes.size,
                            onAccept = acceptInvite,
                            onDecline = rejectInvite
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LovedOneCardCircle(
    connection: CircleMember,
    index: Int,
    count: Int
) {

    val shape = itemShape(index, count)

    Row(
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.onPrimary,
                            MaterialTheme.colorScheme.inversePrimary
                        )
                    )
                )
        ) {
            Text(
                connection.alias?.take(1) ?: "",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = manrope
                ),
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            connection.alias ?: "",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = manrope
            ),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            shadowElevation = 2.dp
        ) {
            Icon(
                painter = painterResource(R.drawable.trash),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .size(36.dp)
                    .padding(6.dp)
            )
        }

    }

}

@Composable
fun PendingInviteRow(
    acceptingInviteId: String?,
    rejectingInviteId: String?,
    connection: CircleMember,
    index: Int,
    count: Int,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    val shape = itemShape(index, count)

    Row(
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Avatar (same as LovedOne)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.onPrimary,
                            MaterialTheme.colorScheme.inversePrimary
                        )
                    )
                )
        ) {
            Text(
                connection.alias?.take(1) ?: "",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = connection.alias ?: "",
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        TextButton(
            enabled = acceptingInviteId != connection.id,
            onClick = {
                onAccept(connection.id)
            }) {
            Text(
                "Accept",
                fontFamily = manrope,
                color = MaterialTheme.colorScheme.primary
            )
        }

        TextButton(
            enabled = rejectingInviteId != connection.id,
            onClick = {
                onDecline(connection.id)
            }) {
            Text(
                "Decline",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCircleScreen() {
    GeoWavTheme(
    ) {
        CircleScreen(hiltViewModel())
    }
}