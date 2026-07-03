package com.nfctags.app.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendOkHttpClient
