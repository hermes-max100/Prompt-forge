package com.aistudio.promptforge.abcd.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    // AutoForge Packs
    @Query("SELECT * FROM autoforge_packs ORDER BY createdAt DESC")
    fun getAllAutoForgePacks(): Flow<List<AutoForgePack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoForgePack(pack: AutoForgePack)

    @Query("DELETE FROM autoforge_packs WHERE id = :id")
    suspend fun deleteAutoForgePack(id: String)

    // Saved Skills
    @Query("SELECT * FROM saved_skills ORDER BY createdAt DESC")
    fun getAllSavedSkills(): Flow<List<SavedSkill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSkill(skill: SavedSkill)

    @Query("DELETE FROM saved_skills WHERE id = :id")
    suspend fun deleteSavedSkill(id: String)

    // Saved MCPs
    @Query("SELECT * FROM saved_mcps ORDER BY createdAt DESC")
    fun getAllSavedMcps(): Flow<List<SavedMcp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMcp(mcp: SavedMcp)

    @Query("DELETE FROM saved_mcps WHERE id = :id")
    suspend fun deleteSavedMcp(id: String)

    // Saved Prompts
    @Query("SELECT * FROM saved_prompts ORDER BY createdAt DESC")
    fun getAllSavedPrompts(): Flow<List<SavedPrompt>>

    @Query("SELECT * FROM saved_prompts WHERE title LIKE '%' || :query || '%' OR assembled LIKE '%' || :query || '%' OR frameworkId LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchSavedPrompts(query: String): Flow<List<SavedPrompt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPrompt(prompt: SavedPrompt)

    @Query("DELETE FROM saved_prompts WHERE id = :id")
    suspend fun deleteSavedPrompt(id: String)

    // Playground Runs
    @Query("SELECT * FROM playground_runs ORDER BY at DESC")
    fun getAllPlaygroundRuns(): Flow<List<PlaygroundRun>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaygroundRun(run: PlaygroundRun)

    @Query("DELETE FROM playground_runs")
    suspend fun clearPlaygroundRuns()

    // Eval Cases
    @Query("SELECT * FROM eval_cases")
    fun getAllEvalCases(): Flow<List<EvalCase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvalCase(evalCase: EvalCase)

    @Query("DELETE FROM eval_cases WHERE id = :id")
    suspend fun deleteEvalCase(id: String)

    // Favorite Prompts
    @Query("SELECT promptId FROM favorite_prompts")
    fun getAllFavoritePromptIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePrompt)

    @Query("DELETE FROM favorite_prompts WHERE promptId = :promptId")
    suspend fun deleteFavorite(promptId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_prompts WHERE promptId = :promptId)")
    suspend fun isFavorite(promptId: String): Boolean

    // Prompt Statistics
    @Query("SELECT * FROM prompt_stats")
    fun getAllPromptStats(): Flow<List<PromptStat>>

    @Query("SELECT * FROM prompt_stats WHERE promptId = :promptId")
    suspend fun getPromptStat(promptId: String): PromptStat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePromptStat(stat: PromptStat)
}
