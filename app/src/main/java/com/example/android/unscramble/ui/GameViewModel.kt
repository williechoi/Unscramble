package com.example.android.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.android.unscramble.data.MAX_NO_OF_WORDS
import com.example.android.unscramble.data.SCORE_INCREASE
import com.example.android.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


/**
 * ViewModel containing the app data and methods to process the data
 */
class GameViewModel : ViewModel() {

    // Game UI state
    private val _uiState = MutableStateFlow(GameUiState())

    // Backing property to avoid state updates from other classes
    // UI can only gain access to uiState via this variable (read-only)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // userGuess is a mutable variable,
    // but changing its value is only allowed inside this Model class
    var userGuess by mutableStateOf("")
        private set

    // the current scrambled word.
    private lateinit var currentWord: String

    // Set of words used in the game.
    private var usedWords: MutableSet<String> = mutableSetOf()

    init {
        resetGame()
    }

    /**
     * Re-initializes the game data to restart the game.
     */
    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    /**
     * Update the user's guess
     */
    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    /**
     * Checks if the user's guess is correct.
     * Increases the score accordingly.
     */
    fun checkUserGuess() {
        // If user's guess is correct, increase the score.
        // and call updateGameState() to prepare the game for the next round.

        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)

        // If user's guess is wrong, show an error.
        } else {

            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // userGuess always set to empty String, be it correct or incorrect.
        updateUserGuess("")
    }

    /**
     * Skip to next word
     */
    fun skipWord() {
        updateGameState(_uiState.value.score)

        // Reset user guess.
        updateUserGuess("")
    }

    /**
     * Picks a new currentWord and currentScrambledWord and updates UiState according to
     * current game state.
     */
    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            // Last round in the game, update isGameOver to true, don't pick a new word.
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            // Normal round in the game.
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    currentWordCount = currentState.currentWordCount.inc(),
                    score = updatedScore
                )

            }

        }
    }

    /**
     * helper method that shuffles the given word.
     * If the shuffled word is the same as the original word,
     * do shuffling again until the result is different from the original.
     */
    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()

        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    /**
     * helper method that picks a random word from the list and shuffle it.
     */
    private fun pickRandomWordAndShuffle(): String {

        // Continue picking up a new random word until you get one that hasn't been used before.
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

}

