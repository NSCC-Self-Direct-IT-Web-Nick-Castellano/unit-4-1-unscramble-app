package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class GameViewModel : ViewModel() {
    // the private properties ----------------------------------------
    private lateinit var currentWord: String

    private var usedWords: MutableSet<String> = mutableSetOf()

    // Backing property to avoid state updates from other classes
    // _uiState Mutable state flow allows mutation to the sate
    private val _uiState = MutableStateFlow(GameUiState())

    // uiState StateFlow is read only so you can show the values in your composables
    // this is safer for displaying content
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // private getters and setters ----------------------------------
    var userGuess by mutableStateOf("")
        private set

    fun getCurrentWord() : String {
        return currentWord
    }
    // the private helper methods --------------------------------------

    // pick a random word from the data
    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            // if the word is already used
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    // scramble the current word
    private fun shuffleCurrentWord(word: String): String {
        // have a temporary variable work with, meaning shuffle
        // the letters positions and compare
        val tempWord = word.toCharArray()

        // scramble the word
        tempWord.shuffle()
        // check if the shuffled string is the same as the original
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }

        return String(tempWord)
    }

    // update the game state, you only need the new score
    // the rest will be just reset to default
    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            // last round in the game, update isGameOver to true, dont pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            // normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    // increase current word count by 1
                    currentWordCount = currentState.currentWordCount.inc(),
                )
            }
        }
    }

    // the public helper methods --------------------------------------

    // update the guessword in the ui view widget text field
    fun updateUserGuess(guessWord: String) {
        userGuess = guessWord
    }

    // check user guess
    fun checkUserGuess() {
        // verify if the user guessed right
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // user score is correct, increase the score
            // we already have a score increase defined in our data model
            val updatedScrore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore = updatedScrore)
        } else {
            _uiState.update { currentState ->
                // we use copy because we want the rest of the app unchanged,
                // only have the isGuessedWordWrong field changed
                currentState.copy(isGuessedWordWrong = true)
            }
        }

        // reset user guess
        updateUserGuess("")
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }



    // the game reset and init handlers --------------------------------------
    // reset the game
    fun resetGame() {
        // clear the words array and pick the first random word to scramble
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    init {
        resetGame()
    }
}

