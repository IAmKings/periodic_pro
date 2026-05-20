package com.periodic.pro.feature.quiz

/**
 * 测验页面用户意图。
 */
sealed interface QuizIntent {
    /** 用户选择了一个选项 */
    data class SelectOption(val answer: String) : QuizIntent

    /** 用户点击下一题 */
    data object NextQuestion : QuizIntent

    /** 用户查看错题回顾 */
    data object ShowReview : QuizIntent

    /** 用户关闭错题回顾 */
    data object DismissReview : QuizIntent

    /** 用户结束测验 */
    data object EndQuiz : QuizIntent

    /** 用户重新开始 */
    data object Retry : QuizIntent
}
