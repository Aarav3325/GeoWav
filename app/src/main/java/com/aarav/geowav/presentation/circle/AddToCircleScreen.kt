package com.aarav.geowav.presentation.circle

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.DeleteDialog
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.locationsharing.itemShape
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CircleScreen(
    viewModel: CircleVM,
    subscriptionVM: SubscriptionViewModel,
    back: () -> Unit,
    navigateToPaywall: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val plan by subscriptionVM.userPlan.collectAsState()


    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }

    upgradeContext?.let {
        CustomBottomSheet(
            onDismissRequest = {
                upgradeContext = null
            }
        ) {
            UpgradeBottomSheetContent(
                context = it,
                onUpgradeClick = {
                    upgradeContext = null
                    navigateToPaywall()
                },
                onDismiss = { upgradeContext = null }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CircleUiEvent.InviteSent ->
                    SnackbarManager.showMessage("Invite sent")

                is CircleUiEvent.ShowError ->
                    SnackbarManager.showMessage(event.message)

                is CircleUiEvent.InviteAccepted ->
                    SnackbarManager.showMessage("Invite Accepted")

                is CircleUiEvent.MemberDeleted ->
                    SnackbarManager.showMessage("Member deleted")

                is CircleUiEvent.ShowUpgrade ->
                    upgradeContext = event.context
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadLovedOnes()
        viewModel.loadPendingInvites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Circle",
                        fontFamily = manrope,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            back()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            uiState.isLoading -> {
                ContainedLoadingIndicator()
            }

            else -> {
                CircleContent(
                    modifier = Modifier.padding(padding),
                    uiState = uiState,
                    userPlan = plan,
                    updateName = viewModel::updateName,
                    updateEmail = viewModel::updateEmail,
                    onSendInvite = viewModel::sendInvite,
                    onAcceptInvite = viewModel::acceptInvite,
                    onRejectInvite = viewModel::rejectInvite,
                    onDeleteMember = viewModel::showDeleteDialog,
                    dismissDialog = viewModel::hideDeleteDialog,
                    deleteMember = viewModel::deleteMember
                )
            }
        }
    }
}

@Composable
fun CircleContent(
    modifier: Modifier = Modifier,
    uiState: CircleUiState,
    userPlan: UserPlan,
    updateName: (String) -> Unit,
    updateEmail: (String) -> Unit,
    onSendInvite: (String, String, UserPlan) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onRejectInvite: (String) -> Unit,
    onDeleteMember: () -> Unit,
    dismissDialog: () -> Unit,
    deleteMember: (String) -> Unit,
) {

    var confirmDeleteFor by remember {
        mutableStateOf<String?>(null)
    }

    DeleteDialog(
        shouldShowDialog = uiState.showDeleteDialog && confirmDeleteFor != null,
        onDismissRequest = dismissDialog,
        dismissButtonText = "Cancel",
        onDismissClick = dismissDialog,
        title = "Remove Member",
        icon = R.drawable.trash,
        message = "Are you sure you want to remove this member from your circle?",
        confirmButtonText = "Remove"
    ) {
        confirmDeleteFor?.let {
            deleteMember(it)
            dismissDialog()
        }
    }

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
            ConnectionUsageCard(
                current = uiState.lovedOnes.size,
                plan = userPlan,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }


        item {
            AddLovedOneCard(
                uiState,
                userPlan,
                nameUpdate = updateName,
                emailUpdate = updateEmail,
                isLoading = uiState.sendingRequest,
                onSendInvite = onSendInvite
            )
        }

        item {
            MyCircleSection(uiState.lovedOnes, onDeleteMember) {
                confirmDeleteFor = it
            }
        }

        item {
            PendingInviteSection(
                uiState.pendingInvites,
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
    userPlan: UserPlan,
    nameUpdate: (String) -> Unit,
    emailUpdate: (String) -> Unit,
    isLoading: Boolean,
    onSendInvite: (String, String, UserPlan) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope
                ),
                fontSize = 16.sp
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
                        fontFamily = manrope,
                        fontSize = 14.sp,
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
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(12.dp)
            )

            val focusManager = LocalFocusManager.current

            Spacer(Modifier.height(2.dp))

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
                        fontFamily = manrope,
                        fontSize = 14.sp,
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
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }, keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(), singleLine = true, leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.email),
                        contentDescription = "email icon",
                        modifier = Modifier.size(24.dp)
                    )
                }, colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(6.dp))

            SendInviteButton(!isLoading) {
                focusManager.clearFocus()
                onSendInvite(uiState.email, uiState.name, userPlan)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            .padding(vertical = 4.dp, horizontal = 0.dp)
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if(isEnabled) {
            Text(
                "Send Invite",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.width(12.dp))

            Icon(
                painter = painterResource(R.drawable.send_invite),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        else {
            CircularProgressIndicator(
                Modifier.size(36.dp)
            )
        }

    }
}


@Composable
fun MyCircleSection(
    lovedOnesList: List<CircleMember>,
    onDeleteMember: () -> Unit,
    confirmDelete: (String) -> Unit,
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
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


            Spacer(Modifier.height(16.dp))

            if (lovedOnesList.isEmpty()) {

                Text(
                    text = "Add loved ones to your circle",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = manrope
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp
                )
            } else {

                lovedOnesList.forEachIndexed { index, connection ->
                    LovedOneCardCircle(
                        connection = connection,
                        index = index,
                        count = lovedOnesList.size,
                        onDeleteMember,
                        confirmDelete
                    )
                }
            }
        }
    }
}

@Composable
fun PendingInviteSection(
    pendingInvites: List<PendingInvite>,
    acceptingInviteId: String?,
    rejectingInviteId: String?,
    acceptInvite: (String) -> Unit,
    rejectInvite: (String) -> Unit
) {

//    val lovedOnes = listOf(
//        CircleMember(
//            "1",
//            "",
//            "Akshat",
//            true
//        )
//    )

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier
                .animateContentSize()
                .clickable(
                    enabled = pendingInvites.isNotEmpty()
                ) {
                    expanded = !expanded
                }
        ) {
            Row(
                modifier = Modifier

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

                if (pendingInvites.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryFixedDim)
                    ) {
                        Text(
                            text = pendingInvites.size.toString(),
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryFixed,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(Modifier.width(8.dp))
                }

                Icon(
                    painter = painterResource(icon),
                    contentDescription = "icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (pendingInvites.isNotEmpty()) {
                if (expanded) {

                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                            top = 4.dp
                        )
                    ) {
                        pendingInvites.forEachIndexed { index, connection ->
                            PendingInviteRow(
                                acceptingInviteId,
                                rejectingInviteId,
                                connection,
                                index = index,
                                count = pendingInvites.size,
                                onAccept = acceptInvite,
                                onDecline = rejectInvite
                            )
                        }
                    }
                }
            } else {

                Text(
                    text = "No Pending Invites",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = manrope
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 16.dp,
                        ),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LovedOneCardCircle(
    connection: CircleMember,
    index: Int,
    count: Int,
    onDeleteMember: () -> Unit,
    confirmDelete: (String) -> Unit
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
                connection.alias?.take(1) ?: connection.profileName.take(1),
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
            connection.alias ?: connection.profileName,
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
            shadowElevation = 2.dp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    onDeleteMember()
                    confirmDelete(connection.id)
                }
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
    connection: PendingInvite,
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
                connection.senderProfileName?.take(1) ?: "",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = connection.senderProfileName ?: "",
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        TextButton(
            enabled = acceptingInviteId != connection.senderId,
            shape = RoundedCornerShape(16.dp),
            onClick = {
                onAccept(connection.senderId)
            }) {
            Text(
                "Accept",
                fontFamily = manrope,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.width(6.dp))

        TextButton(
            enabled = rejectingInviteId != connection.senderId,
            shape = RoundedCornerShape(16.dp),
            onClick = {
                onDecline(connection.senderId)
            }) {
            Text(
                "Decline",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}


@Composable
fun ConnectionUsageCard(
    current: Int,
    plan: UserPlan,
    showPlanInfo: Boolean = true,
    modifier: Modifier = Modifier
) {
    val max = FeatureAccess.maxConnections(plan)

    val isUnlimited = max == Int.MAX_VALUE
    val isLimitReached = !isUnlimited && current >= max

    val planText = when (plan) {
        UserPlan.FREE -> "GeoWav Free"
        UserPlan.PREMIUM -> "GeoWav Premium"
        UserPlan.PRO -> "GeoWav Pro"
    }

    val usageText = if (isUnlimited) {
        "$current connection"
    } else {
        "$current / $max connections used"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isLimitReached)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLimitReached)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            if(showPlanInfo) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = planText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Surface(
                        shape = RoundedCornerShape(25),
                        color = if (isLimitReached)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isLimitReached) "Limit Reached" else "Active",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = manrope,
                            color = if (isLimitReached)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = usageText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = manrope,
                color = if (isLimitReached)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onBackground
            )

            if (!isUnlimited) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(current.toFloat() / max)
                            .fillMaxHeight()
                            .background(
                                if (isLimitReached)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                    )
                }
            }

            if (isLimitReached) {
                Text(
                    text = "Upgrade to add more connections",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

//
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewCircleScreen() {
//    GeoWavTheme(
//    ) {
//        CircleScreen(hiltViewModel())
//    }
//}