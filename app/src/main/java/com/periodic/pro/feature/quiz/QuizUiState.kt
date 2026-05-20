package com.periodic.pro.feature.quiz

import com.periodic.pro.data.element.model.Element

/**
 * 测验题目类型。
 */
enum class QuizType { SYMBOL, NAME, CATEGORY, ATOMIC_NUMBER }

/**
 * 单道测验题目。
 */
data class QuizQuestion(
    val element: Element,
    val zhName: String,
    val type: QuizType,
    val options: List<String>,
    val correctAnswer: String,
)

/**
 * 错题记录。
 */
data class WrongAnswer(
    val questionText: String,
    val userAnswer: String,
    val correctAnswer: String,
)

/**
 * 根据题目类型生成问题文本。
 *
 * @param formatSymbol 格式字符串：\"%s\" 的元素符号是什么？
 * @param formatName 格式字符串：元素符号 \"%s\" 代表哪个元素？
 * @param formatCategory 格式字符串：\"%s (%s)\" 属于哪一类？
 * @param formatAtomicNumber 格式字符串：\"%s (%s)\" 的原子序数是多少？
 */
fun questionText(
    q: QuizQuestion,
    formatSymbol: String = "\"%s\" 的元素符号是什么？",
    formatName: String = "元素符号 \"%s\" 代表哪个元素？",
    formatCategory: String = "\"%s (%s)\" 属于哪一类？",
    formatAtomicNumber: String = "\"%s (%s)\" 的原子序数是多少？",
): String = when (q.type) {
    QuizType.SYMBOL -> formatSymbol.format(q.zhName)
    QuizType.NAME -> formatName.format(q.element.symbol)
    QuizType.CATEGORY -> formatCategory.format(q.zhName, q.element.symbol)
    QuizType.ATOMIC_NUMBER -> formatAtomicNumber.format(q.zhName, q.element.symbol)
}

/**
 * 测验页面 UI 状态。
 */
data class QuizUiState(
    val currentQuestion: QuizQuestion? = null,
    val score: Int = 0,
    val total: Int = 0,
    val streak: Int = 0,
    val wrongAnswers: List<WrongAnswer> = emptyList(),
    val selectedAnswer: String? = null,
    val showResult: Boolean = false,
    val showEndScreen: Boolean = false,
    val showWrongReview: Boolean = false,
    val isLoading: Boolean = true,
)
