package com.aistudio.promptforge.abcd.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM saved_prompts ORDER BY createdAt DESC")
    fun getAllSavedPrompts(): Flow<List<SavedPrompt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPrompt(prompt: SavedPrompt)

    @Query("DELETE FROM saved_prompts WHERE id = :id")
    suspend fun deleteSavedPrompt(id: String)

    @Query("SELECT * FROM playground_runs ORDER BY at DESC")
    fun getAllPlaygroundRuns(): Flow<List<PlaygroundRun>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaygroundRun(run: PlaygroundRun)

    @Query("DELETE FROM playground_runs")
    suspend fun clearPlaygroundRuns()

    @Query("SELECT * FROM eval_cases")
    fun getAllEvalCases(): Flow<List<EvalCase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvalCase(evalCase: EvalCase)

    @Query("DELETE FROM eval_cases WHERE id = :id")
    suspend fun deleteEvalCase(id: String)
}
