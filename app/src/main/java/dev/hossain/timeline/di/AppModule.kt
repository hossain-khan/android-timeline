package dev.hossain.timeline.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Provides

@ContributesTo(AppScope::class)
@dagger.Module
class AppModule {
    @Provides
    fun provideParser(): String = "Parser-TBA"
}
