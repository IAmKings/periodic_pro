package com.periodic.pro.feature.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.periodic.pro.R
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.forCategory
import com.periodic.pro.ui.components.PeriodicButton
import kotlin.random.Random
import org.koin.java.KoinJavaComponent.get

enum class QuizType { SYMBOL, NAME, CATEGORY, ATOMIC_NUMBER }

private data class QuizQuestion(
    val element: Element,
    val zhName: String,
    val type: QuizType,
    val options: List<String>,
    val correctAnswer: String,
)

/** 错题记录 */
private data class WrongAnswer(
    val questionText: String,
    val userAnswer: String,
    val correctAnswer: String,
)

/** 根据题目类型生成问题文本 */
private fun questionText(q: QuizQuestion): String = when (q.type) {
    QuizType.SYMBOL -> "\"${q.zhName}\" 的元素符号是什么？"
    QuizType.NAME -> "元素符号 \"${q.element.symbol}\" 代表哪个元素？"
    QuizType.CATEGORY -> "\"${q.zhName} (${q.element.symbol})\" 属于哪一类？"
    QuizType.ATOMIC_NUMBER -> "\"${q.zhName} (${q.element.symbol})\" 的原子序数是多少？"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val elementRepo = remember { get<ElementRepository>(ElementRepository::class.java) }

    // 预加载所有元素 + 中文名
    val allElements = remember { elementRepo.getAll() }
    val zhMap = remember {
        allElements.mapNotNull { el ->
            val zh = elementRepo.getZh(el.atomicNumber)
            zh?.let { el.atomicNumber to zh.nameZh }
        }.toMap()
    }

    var currentQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var streak by remember { mutableIntStateOf(0) }
    var wrongAnswers by remember { mutableStateOf<List<WrongAnswer>>(emptyList()) }
    var showEndScreen by remember { mutableStateOf(false) }
    var showWrongReview by remember { mutableStateOf(false) }

    fun generate(): QuizQuestion {
        val el = allElements.random()
        val zhName = zhMap[el.atomicNumber] ?: el.name
        val type = QuizType.entries.random()
        val correct: String
        val opts = mutableListOf<String>()

        when (type) {
            QuizType.SYMBOL -> {
                correct = el.symbol
                opts.add(correct)
                while (opts.size < 4) {
                    val r = allElements.random().symbol
                    if (r !in opts) opts.add(r)
                }
            }
            QuizType.NAME -> {
                correct = zhName
                opts.add(correct)
                while (opts.size < 4) {
                    val r = zhMap[allElements.random().atomicNumber] ?: continue
                    if (r !in opts) opts.add(r)
                }
            }
            QuizType.CATEGORY -> {
                correct = el.category.displayName
                opts.addAll(Category.entries.map { it.displayName }.shuffled().take(4))
                if (correct !in opts) opts[0] = correct
            }
            QuizType.ATOMIC_NUMBER -> {
                correct = el.atomicNumber.toString()
                opts.add(correct)
                while (opts.size < 4) {
                    val r = Random.nextInt(1, 119).toString()
                    if (r !in opts) opts.add(r)
                }
            }
        }
        return QuizQuestion(el, zhName, type, opts.shuffled(), correct)
    }

    LaunchedEffect(Unit) { currentQuestion = generate() }

    fun handle(answer: String) {
        if (showResult) return
        selectedAnswer = answer
        showResult = true
        total++
        if (answer == currentQuestion?.correctAnswer) { score++; streak++ }
        else {
            streak = 0
            val q = currentQuestion ?: return
            val qText = questionText(q)
            wrongAnswers = wrongAnswers + WrongAnswer(
                questionText = qText,
                userAnswer = answer,
                correctAnswer = q.correctAnswer,
            )
        }
    }

    fun next() { selectedAnswer = null; showResult = false; currentQuestion = generate() }

    fun endQuiz() {
        showEndScreen = true
        showResult = false
    }

    fun reset() {
        score = 0; total = 0; streak = 0
        wrongAnswers = emptyList()
        showEndScreen = false
        showWrongReview = false
        next()
    }

    fun retry() = reset()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (showEndScreen) stringResource(R.string.quiz_result_title)
                        else stringResource(R.string.quiz_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        val q = currentQuestion

        if (showEndScreen) {
            // === 测验结束页面 ===
            QuizResultContent(
                score = score,
                total = total,
                wrongCount = wrongAnswers.size,
                onReviewWrong = { showWrongReview = true },
                onRetry = { retry() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimensions.Dp16),
            )

            // 错题回顾 overlay
            if (showWrongReview) {
                WrongReviewContent(
                    wrongAnswers = wrongAnswers,
                    onBack = { showWrongReview = false },
                    onRetry = {
                        showWrongReview = false
                        retry()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }
        } else {
            // === 答题页面 ===
            if (q == null) return@Scaffold
            val categoryColors = LocalCategoryColors.current
            val catColor = categoryColors.forCategory(q.element.category)

            val question = questionText(q)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimensions.Dp16),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(Dimensions.Dp8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                "$score / $total",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        AnimatedVisibility(visible = streak > 1) {
                            val pulse by animateFloatAsState(
                                if (streak > 1) 1.1f else 1f,
                                tween(500),
                                label = "pulse",
                            )
                            Box(
                                modifier = Modifier
                                    .scale(pulse)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    "$streak ${stringResource(R.string.quiz_streak)}",
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8)) {
                        TextButton(onClick = { reset() }) {
                            Text(
                                stringResource(R.string.quiz_restart),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { endQuiz() }) {
                            Text(
                                stringResource(R.string.quiz_end),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.Dp24))

                // 元素卡片
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(catColor.copy(alpha = 0.85f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (q.type == QuizType.ATOMIC_NUMBER) "?" else q.element.atomicNumber.toString(),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                        )
                        Text(
                            if (q.type == QuizType.SYMBOL) "?" else q.element.symbol,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            if (q.type == QuizType.NAME) "?" else q.zhName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.Dp24))
                Text(
                    question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(Dimensions.Dp24))

                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Dp12)) {
                    q.options.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12)) {
                            row.forEach { opt ->
                                val correct = opt == q.correctAnswer
                                val selected = opt == selectedAnswer
                                val bg = when {
                                    showResult && correct -> Color(0xFF22C55E).copy(alpha = 0.2f)
                                    showResult && selected -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val bd = when {
                                    showResult && correct -> Color(0xFF22C55E).copy(alpha = 0.6f)
                                    showResult && selected -> Color(0xFFEF4444).copy(alpha = 0.6f)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                }
                                val tc = when {
                                    showResult && correct -> Color(0xFF22C55E)
                                    showResult && selected -> Color(0xFFEF4444)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bg)
                                        .border(1.dp, bd, RoundedCornerShape(12.dp))
                                        .clickable(enabled = !showResult) { handle(opt) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        opt,
                                        color = tc,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.Dp8))
                if (showResult) {
                    val isCorrect = selectedAnswer == q.correctAnswer
                    Text(
                        if (isCorrect) stringResource(R.string.quiz_correct)
                        else stringResource(R.string.quiz_wrong, q.correctAnswer),
                        color = if (isCorrect) Color(0xFF22C55E) else Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Dp8))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { next() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.quiz_next),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 测验结果页面。
 */
@Composable
private fun QuizResultContent(
    score: Int,
    total: Int,
    wrongCount: Int,
    onReviewWrong: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPerfect = score == total && total > 0

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 分数展示
        Text(
            text = stringResource(R.string.quiz_score, score, total),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = if (isPerfect) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp16))

        // 满分庆祝
        if (isPerfect) {
            Text(
                text = stringResource(R.string.quiz_perfect),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22C55E),
            )
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp32))

        // 错题回顾按钮（有错题时才显示）
        if (wrongCount > 0) {
            PeriodicButton(
                onClick = onReviewWrong,
                text = stringResource(R.string.quiz_review_wrong),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(Dimensions.Dp12))
        }

        // 再测一次按钮
        PeriodicButton(
            onClick = onRetry,
            text = stringResource(R.string.quiz_retry),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * 错题回顾列表。
 */
@Composable
private fun WrongReviewContent(
    wrongAnswers: List<WrongAnswer>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // 顶部操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Dp16, vertical = Dimensions.Dp8),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(
                    stringResource(R.string.quiz_result_title),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            TextButton(onClick = onRetry) {
                Text(
                    stringResource(R.string.quiz_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // 标题
        Text(
            text = stringResource(R.string.quiz_wrong_title, wrongAnswers.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp8))

        // 错题列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Dimensions.Dp16,
                vertical = Dimensions.Dp8,
            ),
        ) {
            itemsIndexed(wrongAnswers) { index, wrong ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(Dimensions.Dp16),
                ) {
                    Text(
                        text = stringResource(R.string.quiz_wrong_review_label, index + 1),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Dp4))
                    Text(
                        text = wrong.questionText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Dp8))
                    Text(
                        text = stringResource(R.string.quiz_wrong_user_answer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = wrong.userAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEF4444),
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Dp4))
                    Text(
                        text = stringResource(R.string.quiz_wrong_correct_answer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = wrong.correctAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
