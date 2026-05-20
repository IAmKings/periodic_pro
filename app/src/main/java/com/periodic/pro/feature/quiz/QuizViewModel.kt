package com.periodic.pro.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuizViewModel(
    private val elementRepo: ElementRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizUiState(isLoading = true))
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var allElements: List<Element> = emptyList()
    private var zhMap: Map<Int, String> = emptyMap()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            allElements = elementRepo.getAll()
            zhMap = allElements.mapNotNull { el ->
                elementRepo.getZh(el.atomicNumber)?.let { el.atomicNumber to it.nameZh }
            }.toMap()
            _state.update {
                it.copy(isLoading = false, currentQuestion = generate())
            }
        }
    }

    fun handle(intent: QuizIntent) {
        when (intent) {
            is QuizIntent.SelectOption -> selectOption(intent.answer)
            QuizIntent.NextQuestion -> nextQuestion()
            QuizIntent.ShowReview -> _state.update { it.copy(showWrongReview = true) }
            QuizIntent.DismissReview -> _state.update { it.copy(showWrongReview = false) }
            QuizIntent.EndQuiz -> _state.update {
                it.copy(showEndScreen = true, showResult = false)
            }
            QuizIntent.Retry -> retry()
        }
    }

    private fun selectOption(answer: String) {
        val cur = _state.value
        if (cur.showResult) return
        val q = cur.currentQuestion ?: return

        val isCorrect = answer == q.correctAnswer
        val newScore = if (isCorrect) cur.score + 1 else cur.score
        val newStreak = if (isCorrect) cur.streak + 1 else 0
        val newWrongs = if (isCorrect) {
            cur.wrongAnswers
        } else {
            cur.wrongAnswers + WrongAnswer(
                questionText = questionText(q),
                userAnswer = answer,
                correctAnswer = q.correctAnswer,
            )
        }

        _state.update {
            it.copy(
                selectedAnswer = answer,
                showResult = true,
                total = it.total + 1,
                score = newScore,
                streak = newStreak,
                wrongAnswers = newWrongs,
            )
        }
    }

    private fun nextQuestion() {
        _state.update {
            it.copy(
                selectedAnswer = null,
                showResult = false,
                currentQuestion = generate(),
            )
        }
    }

    private fun retry() {
        _state.update {
            QuizUiState(isLoading = false, currentQuestion = generate())
        }
    }

    private fun generate(): QuizQuestion {
        val elements = allElements
        val localZhMap = zhMap
        val el = elements.random()
        val zhName = localZhMap[el.atomicNumber] ?: el.name
        val type = QuizType.entries.random()
        val correct: String
        val opts = mutableListOf<String>()

        when (type) {
            QuizType.SYMBOL -> {
                correct = el.symbol
                opts.add(correct)
                while (opts.size < 4) {
                    val r = elements.random().symbol
                    if (r !in opts) opts.add(r)
                }
            }
            QuizType.NAME -> {
                correct = zhName
                opts.add(correct)
                while (opts.size < 4) {
                    val r = localZhMap[elements.random().atomicNumber] ?: continue
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
}
