package my.cardholder.util

sealed class Result<out R> {
    data class Error(val throwable: Throwable) : Result<Nothing>()
    data class Loading(val message: String) : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
}
