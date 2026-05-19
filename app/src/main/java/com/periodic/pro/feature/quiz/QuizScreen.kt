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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.forCategory
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(onBack: () -> Unit) {
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
        else streak = 0
    }

    fun next() { selectedAnswer = null; showResult = false; currentQuestion = generate() }
    fun reset() { score = 0; total = 0; streak = 0; next() }

    val q = currentQuestion ?: return
    val categoryColors = LocalCategoryColors.current
    val catColor = categoryColors.forCategory(q.element.category)

    val questionText = when (q.type) {
        QuizType.SYMBOL -> "\"${q.zhName}\" 的元素符号是什么？"
        QuizType.NAME -> "元素符号 \"${q.element.symbol}\" 代表哪个元素？"
        QuizType.CATEGORY -> "\"${q.zhName} (${q.element.symbol})\" 属于哪一类？"
        QuizType.ATOMIC_NUMBER -> "\"${q.zhName} (${q.element.symbol})\" 的原子序数是多少？"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("元素测试", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = Dimensions.Dp16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8)) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("$score / $total", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                    }
                    AnimatedVisibility(visible = streak > 1) {
                        val pulse by animateFloatAsState(if (streak > 1) 1.1f else 1f, tween(500), label = "pulse")
                        Box(modifier = Modifier.scale(pulse).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.tertiaryContainer).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text("$streak 连击!", color = MaterialTheme.colorScheme.onTertiaryContainer, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { reset() }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("重新开始", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.Dp24))

            // 元素卡片
            Box(modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)).background(catColor.copy(alpha = 0.85f)).border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (q.type == QuizType.ATOMIC_NUMBER) "?" else q.element.atomicNumber.toString(), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(if (q.type == QuizType.SYMBOL) "?" else q.element.symbol, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(if (q.type == QuizType.NAME) "?" else q.zhName, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.Dp24))
            Text(questionText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(Dimensions.Dp24))

            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Dp12)) {
                q.options.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12)) {
                        row.forEach { opt ->
                            val correct = opt == q.correctAnswer; val selected = opt == selectedAnswer
                            val bg = when { showResult && correct -> Color(0xFF22C55E).copy(alpha = 0.2f); showResult && selected -> Color(0xFFEF4444).copy(alpha = 0.2f); else -> MaterialTheme.colorScheme.surfaceVariant }
                            val bd = when { showResult && correct -> Color(0xFF22C55E).copy(alpha = 0.6f); showResult && selected -> Color(0xFFEF4444).copy(alpha = 0.6f); else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) }
                            val tc = when { showResult && correct -> Color(0xFF22C55E); showResult && selected -> Color(0xFFEF4444); else -> MaterialTheme.colorScheme.onSurface }
                            Box(modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp)).background(bg).border(1.dp, bd, RoundedCornerShape(12.dp)).clickable(enabled = !showResult) { handle(opt) }, contentAlignment = Alignment.Center) {
                                Text(opt, color = tc, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            if (showResult) {
                val isCorrect = selectedAnswer == q.correctAnswer
                Text(if (isCorrect) "✅ 正确!" else "❌ 错误，正确答案：${q.correctAnswer}", color = if (isCorrect) Color(0xFF22C55E) else Color(0xFFEF4444), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(Dimensions.Dp8))
                Box(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary).clickable { next() }, contentAlignment = Alignment.Center) {
                    Text("下一题", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
