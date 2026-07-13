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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.DeleteDialog
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.locationsharing.itemShape
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope



@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontFamily = manrope,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.06.sp,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier.padding(horizontal = 4.dp)
    )
}


@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontFamily = manrope,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.05.sp,
        color = MaterialTheme.colorScheme.outline
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CircleScreen(
    viewModel: CircleVM,
    subscriptionVM: SubscriptionViewModel,
    back: () -> Unit,
    navigateToPaywall: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val plan by subscriptionVM.userPlan.collectAsState()
    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }

    upgradeContext?.let {
        CustomBottomSheet(onDismissRequest = { upgradeContext = null }) {
            UpgradeBottomSheetContent(
                context = it,
                onUpgradeClick = { upgradeContext = null; navigateToPaywall() },
                onDismiss = { upgradeContext = null }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CircleUiEvent.InviteSent -> SnackbarManager.showMessage("Invite sent")
                is CircleUiEvent.ShowError -> SnackbarManager.showMessage(event.message)
                is CircleUiEvent.InviteAccepted -> SnackbarManager.showMessage("Invite Accepted")
                is CircleUiEvent.MemberDeleted -> SnackbarManager.showMessage("Member deleted")
                is CircleUiEvent.ShowUpgrade -> upgradeContext = event.context
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        text = "Your Circle",
                        fontFamily = manrope,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { back() }) {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ContainedLoadingIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading your circle...",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            else -> CircleContent(
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
    var confirmDeleteFor by remember { mutableStateOf<String?>(null) }

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
            .imePadding()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 8.dp,
            bottom = 32.dp
        )
    ) {
        item {
            SectionLabel(
                text = "My circle",
                modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp)
            )
        }
        item {
            MyCircleSection(
                lovedOnesList = uiState.lovedOnes,
                deletingMemberId = uiState.deletingMemberId,
                onDeleteMember = onDeleteMember,
                confirmDelete = { confirmDeleteFor = it }
            )
        }

        item {
            ConnectionUsageCard(
                current = uiState.lovedOnes.size,
                plan = userPlan,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        item {
            SectionLabel(
                text = "Invite someone you trust",
                modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
            )
        }
        item {
            AddLovedOneCard(
                uiState = uiState,
                userPlan = userPlan,
                nameUpdate = updateName,
                emailUpdate = updateEmail,
                isLoading = uiState.sendingRequest,
                onSendInvite = onSendInvite
            )
        }

        if (uiState.pendingInvites.isNotEmpty()) {
            item {
                SectionLabel(
                    text = "Pending invites",
                    modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
                )
            }
            item {
                PendingInviteSection(
                    pendingInvites = uiState.pendingInvites,
                    acceptingInviteId = uiState.acceptingInviteId,
                    rejectingInviteId = uiState.rejectingInviteId,
                    acceptInvite = onAcceptInvite,
                    rejectInvite = onRejectInvite
                )
            }
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            val focusRequester = remember { FocusRequester() }


            Text(
                text = "Bring someone into your circle",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope
                ),
                fontSize = 16.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "They'll receive an email when you're ready to stay connected",
                fontFamily = manrope,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Name")
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { nameUpdate(it) },
                placeholder = {
                    Text(
                        "Enter their name",
                        fontFamily = manrope,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                isError = uiState.nameError != null,
                supportingText = {
                    if (uiState.nameError != null) {
                        Text(
                            text = uiState.nameError,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusRequester.requestFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.user),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            val focusManager = LocalFocusManager.current

            Spacer(Modifier.height(4.dp))

            FieldLabel("Email")
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { emailUpdate(it) },
                placeholder = {
                    Text(
                        "Enter their email",
                        fontFamily = manrope,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                isError = uiState.emailError != null,
                supportingText = {
                    if (uiState.emailError != null) {
                        Text(
                            text = uiState.emailError,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onSendInvite(uiState.email, uiState.name, userPlan)
                }),
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.email),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(8.dp))

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
    Button(
        enabled = isEnabled,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),

        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
    ) {
        if (isEnabled) {
            Text(
                "Send Invite",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.width(10.dp))
            Icon(
                painter = painterResource(R.drawable.send_invite),
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        } else {

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
            )
        }
    }
}


@Composable
fun MyCircleSection(
    lovedOnesList: List<CircleMember>,
    deletingMemberId: String?,
    onDeleteMember: () -> Unit,
    confirmDelete: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "People you care about",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                if (lovedOnesList.isNotEmpty()) {

                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${lovedOnesList.size} in circle",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (lovedOnesList.isEmpty()) {
                Text(
                    text = "Add loved ones to your circle",
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
            } else {
                lovedOnesList.forEachIndexed { index, member ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    LovedOneCardCircle(
                        connection = member,
                        index = index,
                        count = lovedOnesList.size,
                        deletingMemberId = deletingMemberId,
                        onDeleteMember = onDeleteMember,
                        confirmDelete = confirmDelete
                    )
                }
                Spacer(Modifier.height(4.dp))
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
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = pendingInvites.isNotEmpty()) { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Waiting on response",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                if (pendingInvites.isNotEmpty()) {

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryFixedDim)
                    ) {
                        Text(
                            text = pendingInvites.size.toString(),
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryFixed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Icon(
                    painter = painterResource(
                        if (expanded) R.drawable.up_arrow else R.drawable.down_arrow
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (pendingInvites.isEmpty()) {
                Text(
                    text = "No pending invites",
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )
            }

            if (expanded && pendingInvites.isNotEmpty()) {
                pendingInvites.forEachIndexed { index, invite ->
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    PendingInviteRow(
                        acceptingInviteId = acceptingInviteId,
                        rejectingInviteId = rejectingInviteId,
                        connection = invite,
                        index = index,
                        count = pendingInvites.size,
                        onAccept = acceptInvite,
                        onDecline = rejectInvite
                    )
                }
                Spacer(Modifier.height(4.dp))
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
    deletingMemberId: String? = null,
    onDeleteMember: () -> Unit,
    confirmDelete: (String) -> Unit
) {

    val (avatarBg, avatarFg) = when (index % 3) {
        0 -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        1 -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
    val displayName = connection.alias?.takeIf { it.isNotBlank() } ?: connection.profileName
    val presenceContext = "In your circle"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IdentityAvatar(
            avatarUrl = connection.avatarUrl,
            displayName = displayName,
            backgroundColor = avatarBg,
            contentColor = avatarFg,
            modifier = Modifier
                .size(44.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = presenceContext,
                color = MaterialTheme.colorScheme.outline,
                fontFamily = manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }

        val isDeleting = deletingMemberId == connection.id
        val isAnyDeleting = deletingMemberId != null

        if (isDeleting) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        } else {
            IconButton(
                onClick = {
                    onDeleteMember()
                    confirmDelete(connection.id)
                },
                enabled = !isAnyDeleting,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.trash),
                    contentDescription = "Remove member",
                    tint = if (isAnyDeleting)
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
                    else
                        MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
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
    val (avatarBg, avatarFg) = when (index % 3) {
        0 -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        1 -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    val isAccepting = acceptingInviteId == connection.senderId
    val isDeclining = rejectingInviteId == connection.senderId
    val isRowBusy = isAccepting || isDeclining
    val isAnyInviteBusy = acceptingInviteId != null || rejectingInviteId != null

    val isAcceptEnabled = !isAnyInviteBusy
    val acceptBg = when {
        isAccepting -> MaterialTheme.colorScheme.secondaryContainer
        isAnyInviteBusy -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val acceptFg = when {
        isAccepting -> MaterialTheme.colorScheme.onSecondaryContainer
        isAnyInviteBusy -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val isDeclineEnabled = !isAnyInviteBusy
    val declineBg = when {
        isDeclining -> MaterialTheme.colorScheme.errorContainer
        isAnyInviteBusy -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val declineFg = when {
        isDeclining -> MaterialTheme.colorScheme.onErrorContainer
        isAnyInviteBusy -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (connection.senderProfileName?.take(1) ?: "?").uppercase(),
                color = avatarFg,
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = connection.senderProfileName ?: "",
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )


        Surface(
            shape = RoundedCornerShape(10.dp),
            color = acceptBg,
            modifier = Modifier.clickable(enabled = isAcceptEnabled) {
                onAccept(connection.senderId)
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isAccepting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = acceptFg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Accept",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = acceptFg
                    )
                }
            }
        }

        Spacer(Modifier.width(6.dp))


        Surface(
            shape = RoundedCornerShape(10.dp),
            color = declineBg,
            modifier = Modifier.clickable(enabled = isDeclineEnabled) {
                onDecline(connection.senderId)
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isDeclining) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = declineFg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Decline",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = declineFg
                    )
                }
            }
        }
    }
}


@Composable
fun ConnectionUsageCard(
    current: Int,
    plan: UserPlan,
    textSize: TextUnit? = null,
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

    val usageText = if (isUnlimited) "$current connection" else "$current / $max connections used"


    val cardBg = if (isLimitReached)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceContainerHigh

    val cardFg = if (isLimitReached)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSurface

    val cardFgMuted = if (isLimitReached)
        cardFg.copy(alpha = 0.65f)
    else
        MaterialTheme.colorScheme.outline


    val badgeBg = if (isLimitReached)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.surfaceContainerLow

    val badgeFg = if (isLimitReached)
        MaterialTheme.colorScheme.onError
    else
        MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showPlanInfo) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = planText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = manrope,
                        color = cardFgMuted
                    )
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = if (isLimitReached) "Limit reached" else "Active",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope,
                            color = badgeFg
                        )
                    }
                }
            }

            Text(
                text = usageText,
                fontSize = textSize ?: 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = manrope,
                color = cardFg
            )

            if (!isUnlimited) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(cardFg.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(current.toFloat() / max)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                if (isLimitReached)
                                    cardFg
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }

            if (isLimitReached) {
                Text(
                    text = "Upgrade to add more connections",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = manrope,
                    color = cardFgMuted
                )
            }
        }
    }
}
